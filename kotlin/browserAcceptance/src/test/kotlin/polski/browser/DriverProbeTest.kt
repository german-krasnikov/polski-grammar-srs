package polski.browser

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.nio.file.Files
import java.nio.file.Path

/** Installation/launch diagnostic only: no application page or assertion executes here. */
internal class DriverProbeTest {
    @ParameterizedTest(name = "pinned {0} can launch")
    @EnumSource(BrowserEngine::class)
    fun pinnedDriverLaunches(engine: BrowserEngine) {
        Playwright.create().use { playwright ->
            val type = engine.type(playwright)
            val executable = Path.of(type.executablePath())
            assertTrue(Files.isExecutable(executable), "Missing pinned browser executable: $executable")
            assertTrue(executable.toString().contains(engine.cacheRevision),
                "Unexpected browser cache revision: $executable")
            val options = BrowserType.LaunchOptions().setHeadless(true).setTimeout(20_000.0)
            if (System.getProperty("polski.browser.denyEngine") == engine.name) {
                options.setExecutablePath(Path.of("/missing-pinned-browser-for-negative-control"))
            }
            type.launch(options).use { browser ->
                assertTrue(browser.version().startsWith(engine.versionPrefix),
                    "Unexpected $engine version: ${browser.version()}")
                val jar = Path.of(Playwright::class.java.protectionDomain.codeSource.location.toURI())
                println("DRIVER_PROBE engine=$engine version=${browser.version()} " +
                    "executable=$executable playwrightJarSha256=${sha256(jar)} " +
                    "os=${System.getProperty("os.name")}/${System.getProperty("os.arch")} " +
                    "jdk=${System.getProperty("java.version")}")
            }
        }
    }
}
