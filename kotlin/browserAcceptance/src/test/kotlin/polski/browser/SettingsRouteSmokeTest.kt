package polski.browser

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Page
import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.assertions.LocatorAssertions
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList

internal enum class BrowserEngine(val versionPrefix: String, val cacheRevision: String) {
    CHROMIUM("153.0.8010.12", "chromium-1243"),
    FIREFOX("155.0", "firefox-1543"),
    WEBKIT("26.6", "webkit-2359");

    fun type(playwright: Playwright): BrowserType = when (this) {
        CHROMIUM -> playwright.chromium()
        FIREFOX -> playwright.firefox()
        WEBKIT -> playwright.webkit()
    }
}

/** An external, physical-browser assertion. The independent expected heading lives here. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SettingsRouteSmokeTest {
    private lateinit var distribution: BrowserDistribution
    private lateinit var worktree: Path

    @BeforeAll
    fun inspectSelectedBuild() {
        worktree = Path.of(System.getProperty("polski.browser.worktree")
            ?: error("No worktree property; use browserJsTest or browserWasmTest"))
        distribution = BrowserDistribution.inspect(
            worktree,
            Path.of(System.getProperty("polski.browser.dist")
                ?: error("No distribution property; use a branch browser task")),
            System.getProperty("polski.browser.branch")
                ?: error("No branch property; use a branch browser task"),
        )
        NoJsonReadiness.require(worktree, distribution)
    }

    @ParameterizedTest(name = "settings route in {0}")
    @EnumSource(BrowserEngine::class)
    fun trainingNavigationOpensSettings(engine: BrowserEngine) {
        val pageErrors = CopyOnWriteArrayList<String>()
        val consoleErrors = CopyOnWriteArrayList<String>()
        val assetResponses = CopyOnWriteArrayList<com.microsoft.playwright.Response>()
        var browserVersion = ""
        DistributionServer(distribution).use { server ->
            server.start()
            Playwright.create().use { playwright ->
                val launch = BrowserType.LaunchOptions().setHeadless(true).setTimeout(20_000.0)
                if (System.getProperty("polski.browser.denyEngine") == engine.name) {
                    launch.setExecutablePath(Path.of("/missing-pinned-browser-for-negative-control"))
                }
                engine.type(playwright).launch(launch).use { browser ->
                    assertTrue(browser.version().startsWith(engine.versionPrefix),
                        "Pinned $engine version changed: ${browser.version()}")
                    browserVersion = browser.version()
                    browser.newContext(Browser.NewContextOptions()
                        .setLocale("pl-PL").setTimezoneId("Europe/Warsaw")
                        .setViewportSize(320, 800)).use { context ->
                        // Observe writes as booleans in the browser. No application document is
                        // moved through Playwright's machine protocol or persisted as a trace.
                        context.addInitScript("""
                            (() => {
                              let suspiciousWrites = 0;
                              const original = Storage.prototype.setItem;
                              Storage.prototype.setItem = function(key, value) {
                                if (this === localStorage && /^\s*[\[{]/.test(String(value))) suspiciousWrites++;
                                return original.call(this, key, value);
                              };
                              Object.defineProperty(window, '__polskiSuspiciousWrites', {
                                get: () => suspiciousWrites
                              });
                            })();
                        """.trimIndent())
                        val page = context.newPage()
                        page.onPageError { pageErrors += it }
                        page.onConsoleMessage { if (it.type() == "error") consoleErrors += it.text() }
                        page.onResponse { response ->
                            val path = java.net.URI.create(response.url()).path
                            if (path.endsWith("/composeApp.js") ||
                                path.endsWith("/${distribution.applicationWasm?.fileName}")) {
                                assetResponses += response
                            }
                        }
                        page.navigate("${server.baseUrl}/#/training", Page.NavigateOptions().setTimeout(20_000.0))
                        page.getByRole(AriaRole.BUTTON,
                            Page.GetByRoleOptions().setName("Настройки"))
                            .click(Locator.ClickOptions().setTimeout(15_000.0))
                        assertThat(page.getByRole(AriaRole.HEADING,
                            Page.GetByRoleOptions().setName("Настройки обучения")))
                            .isVisible(LocatorAssertions.IsVisibleOptions().setTimeout(15_000.0))
                        val bundleResponse = assetResponses.singleOrNull {
                            java.net.URI.create(it.url()).path.endsWith("/composeApp.js")
                        } ?: error("The selected composeApp.js was not served")
                        assertEquals(200, bundleResponse.status())
                        assertEquals(distribution.bundleSha256, sha256(bundleResponse.body()),
                            "Served bundle differs from the selected $distribution branch")
                        distribution.applicationWasm?.let { wasm ->
                            val wasmResponse = assetResponses.singleOrNull {
                                java.net.URI.create(it.url()).path.endsWith("/${wasm.fileName}")
                            } ?: error("Kotlin application Wasm response is absent")
                            assertEquals(200, wasmResponse.status())
                            assertEquals(distribution.applicationWasmSha256, sha256(wasmResponse.body()))
                        }
                        assertEquals(0, page.evaluate("() => window.__polskiSuspiciousWrites"),
                            "Application wrote JSON-shaped storage bytes")
                    }
                }
            }
        }
        assertTrue(pageErrors.isEmpty(), "Unexpected page errors through teardown: $pageErrors")
        assertTrue(consoleErrors.isEmpty(), "Unexpected console errors through teardown: $consoleErrors")
        println("BROWSER_RECEIPT branch=${distribution.branch} engine=$engine " +
            "browser=$browserVersion index=${distribution.indexSha256} " +
            "bundle=${distribution.bundleSha256} appWasm=${distribution.applicationWasmSha256 ?: "none"} " +
            "pageErrors=0 consoleErrors=0 suspiciousStorageWrites=0 teardown=closed")
    }
}

/** Fail closed until the product-wide text-wire transition has removed application JSON. */
internal object NoJsonReadiness {
    fun require(worktree: Path, distribution: BrowserDistribution) {
        val repository = worktree.resolve("kotlin/shared/src/commonMain/kotlin/polski/progress/ProgressRepository.kt")
        check(!Files.readString(repository).contains("ProgressCodec.encodeLegacyV1(document)")) {
            "Old progress writer still emits JSON on fresh browser startup"
        }
        check(!Files.readString(distribution.bundle).contains("application/json")) {
            "Selected browser bundle still contains an application JSON export/import path"
        }
        Files.walk(distribution.root).use { paths ->
            check(paths.noneMatch { it.fileName.toString().endsWith(".map") }) {
                "Selected browser distribution still includes JSON-format source maps"
            }
        }
        val gate = ProcessBuilder("python3", "scripts/check-no-app-json-tracked.py", "--summary")
            .directory(worktree.toFile()).redirectErrorStream(true).start()
        if (!gate.waitFor(15, java.util.concurrent.TimeUnit.SECONDS)) {
            gate.destroyForcibly()
            error("Product no-JSON tracked-path gate timed out")
        }
        val output = gate.inputStream.bufferedReader().readText().trim()
        check(gate.exitValue() == 0) { "Product no-JSON tracked-path gate is RED: $output" }
    }
}
