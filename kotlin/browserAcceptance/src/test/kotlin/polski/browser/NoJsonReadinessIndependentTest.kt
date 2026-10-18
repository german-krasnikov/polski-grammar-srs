package polski.browser

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

internal class NoJsonReadinessIndependentTest {
    @TempDir lateinit var worktree: Path

    private fun fakeDistribution(bundleText: String = "test bundle"): BrowserDistribution {
        val root = worktree.resolve("kotlin/composeApp/build/dist/js/productionExecutable")
        Files.createDirectories(root)
        Files.writeString(root.resolve("index.html"), "<html>test</html>")
        Files.writeString(root.resolve("composeApp.js"), bundleText)
        val processed = worktree.resolve("kotlin/composeApp/build/processedResources/js/main")
        Files.createDirectories(processed)
        Files.copy(root.resolve("index.html"), processed.resolve("index.html"))
        val webpack = worktree.resolve("kotlin/composeApp/build/kotlin-webpack/js/productionExecutable")
        Files.createDirectories(webpack)
        Files.copy(root.resolve("composeApp.js"), webpack.resolve("composeApp.js"))
        return BrowserDistribution.inspect(worktree, root, "js")
    }

    @Test
    fun legacyFreshStartWriterFailsBeforeAnyBrowserLaunch() {
        val distribution = fakeDistribution()
        val repository = worktree.resolve("kotlin/shared/src/commonMain/kotlin/polski/progress/ProgressRepository.kt")
        Files.createDirectories(repository.parent)
        Files.writeString(repository, "ProgressCodec.encodeLegacyV1(document)")
        val failure = assertThrows(IllegalStateException::class.java) {
            NoJsonReadiness.require(worktree, distribution)
        }
        assertTrue(failure.message.orEmpty().contains("Old progress writer still emits JSON"))
    }

    @Test
    fun generatedJsonSourceMapFailsBeforeAnyBrowserLaunch() {
        val distribution = fakeDistribution()
        val repository = worktree.resolve("kotlin/shared/src/commonMain/kotlin/polski/progress/ProgressRepository.kt")
        Files.createDirectories(repository.parent)
        Files.writeString(repository, "text-wire save")
        Files.writeString(distribution.root.resolve("composeApp.js.map"), "synthetic map placeholder")
        val failure = assertThrows(IllegalStateException::class.java) {
            NoJsonReadiness.require(worktree, distribution)
        }
        assertTrue(failure.message.orEmpty().contains("JSON-format source maps"))
    }

    @Test
    fun applicationJsonMediaTypeInBundleFailsBeforeAnyBrowserLaunch() {
        val distribution = fakeDistribution("application/json")
        val repository = worktree.resolve("kotlin/shared/src/commonMain/kotlin/polski/progress/ProgressRepository.kt")
        Files.createDirectories(repository.parent)
        Files.writeString(repository, "text-wire save")
        val failure = assertThrows(IllegalStateException::class.java) {
            NoJsonReadiness.require(worktree, distribution)
        }
        assertTrue(failure.message.orEmpty().contains("application JSON export/import path"))
    }
}
