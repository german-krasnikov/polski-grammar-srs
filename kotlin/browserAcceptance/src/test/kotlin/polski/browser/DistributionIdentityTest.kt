package polski.browser

import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class DistributionIdentityTest {
    @Test
    fun branchesHaveDifferentBundlesAndTheWasmBranchHasAnApplicationPayload() {
        val worktree = Path.of(System.getProperty("polski.browser.worktree"))
        fun inspect(branch: String) = BrowserDistribution.inspect(
            worktree,
            worktree.resolve("kotlin/composeApp/build/dist/$branch/productionExecutable"),
            branch,
        )
        val js = inspect("js")
        val wasm = inspect("wasmJs")
        assertNotEquals(js.bundleSha256, wasm.bundleSha256)
        assertNotNull(wasm.applicationWasm)
        println("DIST_IDENTITY jsIndex=${js.indexSha256} jsBundle=${js.bundleSha256} " +
            "wasmIndex=${wasm.indexSha256} wasmBundle=${wasm.bundleSha256} " +
            "applicationWasm=${wasm.applicationWasm?.fileName} " +
            "applicationWasmSha256=${wasm.applicationWasmSha256}")
    }
}
