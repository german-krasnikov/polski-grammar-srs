package polski.browser

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.time.Duration
import java.util.concurrent.Executors

internal fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
    .digest(bytes).joinToString("") { "%02x".format(it) }

internal fun sha256(path: Path): String = sha256(Files.readAllBytes(path))

/** A branch-specific, freshly built artifact. No request may escape this exact distribution. */
internal class BrowserDistribution private constructor(
    val root: Path,
    val branch: String,
    val index: Path,
    val bundle: Path,
    val applicationWasm: Path?,
) {
    val indexSha256: String = sha256(index)
    val bundleSha256: String = sha256(bundle)
    val applicationWasmSha256: String? = applicationWasm?.let(::sha256)

    companion object {
        fun inspect(worktree: Path, suppliedRoot: Path, branch: String): BrowserDistribution {
            require(branch == "js" || branch == "wasmJs") { "Unknown browser branch: $branch" }
            val expectedInput = worktree.toAbsolutePath().normalize()
                .resolve("kotlin/composeApp/build/dist/$branch/productionExecutable")
            require(suppliedRoot.toAbsolutePath().normalize() == expectedInput) {
                "Distribution override is not the selected $branch build: $suppliedRoot"
            }
            val worktreeReal = worktree.toRealPath()
            val expected = worktreeReal.resolve("kotlin/composeApp/build/dist/$branch/productionExecutable")
            val root = suppliedRoot.toRealPath()
            require(root.startsWith(worktreeReal) && root == expected.toRealPath()) {
                "Distribution resolves outside the worktree: $root"
            }
            val index = requireFile(root, "index.html")
            val bundle = requireFile(root, "composeApp.js")
            val processedIndex = requireFile(worktreeReal,
                "kotlin/composeApp/build/processedResources/$branch/main/index.html")
            val webpackBundle = requireFile(worktreeReal,
                "kotlin/composeApp/build/kotlin-webpack/$branch/productionExecutable/composeApp.js")
            require(sha256(index) == sha256(processedIndex)) {
                "Distribution index.html differs from this branch's processed resource"
            }
            require(sha256(bundle) == sha256(webpackBundle)) {
                "Distribution composeApp.js differs from this branch's Webpack output"
            }
            val applicationWasm = if (branch == "wasmJs") {
                val compiled = requireFile(
                    worktreeReal,
                    "kotlin/composeApp/build/compileSync/wasmJs/main/productionExecutable/optimized/" +
                        "polski-grammar-kotlin-composeApp.wasm",
                )
                val compiledHash = sha256(compiled)
                val matches = Files.list(root).use { paths ->
                    paths.filter { it.fileName.toString().endsWith(".wasm") && sha256(it) == compiledHash }
                        .toList()
                }
                require(matches.size == 1) { "Expected one Kotlin application Wasm matching compiled output; found ${matches.size}" }
                require(Files.readString(bundle).contains(matches.single().fileName.toString())) {
                    "The selected bundle does not reference the Kotlin application Wasm"
                }
                matches.single()
            } else null
            return BrowserDistribution(root, branch, index, bundle, applicationWasm)
        }

        private fun requireFile(root: Path, relative: String): Path {
            val path = root.resolve(relative)
            require(Files.isRegularFile(path) && path.toRealPath().startsWith(root)) { "Missing or escaped artifact: $path" }
            return path
        }
    }
}

/** Owns a loopback HTTP server and its thread pool for one test case. */
internal class DistributionServer(private val distribution: BrowserDistribution) : AutoCloseable {
    private val executor = Executors.newSingleThreadExecutor { task ->
        Thread(task, "browser-distribution-server").apply { isDaemon = true }
    }
    private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/") { exchange -> serve(exchange) }
        setExecutor(executor)
    }
    val baseUrl: String get() = "http://127.0.0.1:${server.address.port}"

    fun start() {
        server.start()
        val request = HttpRequest.newBuilder(URI.create("$baseUrl/index.html"))
            .timeout(Duration.ofSeconds(10)).GET().build()
        val response = HttpClient.newHttpClient().use {
            it.send(request, HttpResponse.BodyHandlers.ofByteArray())
        }
        check(response.statusCode() == 200 && sha256(response.body()) == distribution.indexSha256) {
            "Fresh distribution server did not serve the selected index.html"
        }
    }

    private fun serve(exchange: HttpExchange) {
        try {
            if (exchange.requestMethod != "GET") {
                exchange.sendResponseHeaders(405, -1)
                return
            }
            val rawPath = exchange.requestURI.rawPath
            val relative = if (rawPath == "/") "index.html" else rawPath.removePrefix("/")
            val candidate = distribution.root.resolve(relative).normalize()
            if (!candidate.startsWith(distribution.root) || !Files.isRegularFile(candidate) ||
                !candidate.toRealPath().startsWith(distribution.root)) {
                exchange.sendResponseHeaders(404, -1)
                return
            }
            exchange.responseHeaders.set("Content-Type", when {
                relative.endsWith(".html") -> "text/html; charset=utf-8"
                relative.endsWith(".js") || relative.endsWith(".mjs") -> "text/javascript; charset=utf-8"
                relative.endsWith(".css") -> "text/css; charset=utf-8"
                relative.endsWith(".wasm") -> "application/wasm"
                relative.endsWith(".png") -> "image/png"
                relative.endsWith(".riv") -> "application/octet-stream"
                else -> "application/octet-stream"
            })
            val bytes = Files.readAllBytes(candidate)
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        } finally {
            exchange.close()
        }
    }

    override fun close() {
        server.stop(0)
        executor.shutdownNow()
        check(executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
            "Distribution server executor did not terminate"
        }
    }
}
