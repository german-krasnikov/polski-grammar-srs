package polski.browser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path

internal class DistributionServerTest {
    @TempDir lateinit var worktree: Path

    private fun fakeJsDistribution(): Path {
        val root = worktree.resolve("kotlin/composeApp/build/dist/js/productionExecutable")
        Files.createDirectories(root)
        Files.writeString(root.resolve("index.html"), "<html>test</html>")
        Files.writeString(root.resolve("composeApp.js"), "test bundle")
        val processed = worktree.resolve("kotlin/composeApp/build/processedResources/js/main")
        Files.createDirectories(processed)
        Files.copy(root.resolve("index.html"), processed.resolve("index.html"))
        val webpack = worktree.resolve("kotlin/composeApp/build/kotlin-webpack/js/productionExecutable")
        Files.createDirectories(webpack)
        Files.copy(root.resolve("composeApp.js"), webpack.resolve("composeApp.js"))
        return root
    }

    @Test
    fun exactDistributionServesBytesAndDeniesTraversal() {
        val root = fakeJsDistribution()
        val distribution = BrowserDistribution.inspect(worktree, root, "js")
        Files.write(root.resolve("renderer.wasm"), byteArrayOf(0, 97, 115, 109))
        val outside = worktree.resolve("secret.txt")
        Files.writeString(outside, "secret")
        Files.createSymbolicLink(root.resolve("escape.txt"), outside)
        DistributionServer(distribution).use { server ->
            server.start()
            HttpClient.newHttpClient().use { client ->
                fun get(path: String): HttpResponse<ByteArray> = client.send(
                    HttpRequest.newBuilder(URI.create("${server.baseUrl}$path")).GET().build(),
                    HttpResponse.BodyHandlers.ofByteArray(),
                )
                assertEquals(200, get("/composeApp.js").statusCode())
                assertEquals(distribution.bundleSha256, sha256(get("/composeApp.js").body()))
                assertEquals("application/wasm", get("/renderer.wasm").headers().firstValue("Content-Type").orElse(null))
                assertEquals(404, get("/%2e%2e/secret").statusCode())
                assertEquals(404, get("/escape.txt").statusCode())
                assertEquals(404, get("/missing.js").statusCode())
                assertEquals(405, client.send(
                    HttpRequest.newBuilder(URI.create("${server.baseUrl}/index.html"))
                        .POST(HttpRequest.BodyPublishers.noBody()).build(),
                    HttpResponse.BodyHandlers.discarding(),
                ).statusCode())
            }
        }
    }

    @Test
    fun outsideOrMissingArtifactFailsBeforeBrowser() {
        val root = fakeJsDistribution()
        assertThrows(IllegalArgumentException::class.java) {
            BrowserDistribution.inspect(worktree, worktree.resolve("elsewhere"), "js")
        }
        Files.delete(root.resolve("composeApp.js"))
        val missing = assertThrows(IllegalArgumentException::class.java) {
            BrowserDistribution.inspect(worktree, root, "js")
        }
        assertTrue(missing.message.orEmpty().contains("Missing or escaped artifact"))
    }

    @Test
    fun changedDistributionBundleFailsBeforeBrowser() {
        val root = fakeJsDistribution()
        Files.writeString(root.resolve("composeApp.js"), "corrupted distribution")
        val failure = assertThrows(IllegalArgumentException::class.java) {
            BrowserDistribution.inspect(worktree, root, "js")
        }
        assertTrue(failure.message.orEmpty().contains("differs from this branch's Webpack output"))
    }
}
