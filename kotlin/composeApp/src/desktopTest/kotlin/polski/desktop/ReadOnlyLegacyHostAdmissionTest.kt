package polski.desktop

import java.nio.file.Files
import java.security.MessageDigest
import kotlinx.coroutines.runBlocking
import polski.progress.LoadResult
import polski.srs.FsrsScheduler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** Seeds one exact-path sealed old-user input; production repository calls are read-only. */
class ReadOnlyLegacyHostAdmissionTest {
    private fun resource(name: String): ByteArray =
        checkNotNull(javaClass.classLoader.getResourceAsStream(name)) { "Missing test-only fixture $name" }
            .use { it.readBytes() }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes).joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }

    @Test
    fun desktopProgressLoadReadsSealedOldBytesWithoutWriting() = runBlocking {
        val inputPath = "inputs/legacy-representability-progress.input"
        val input = resource(inputPath)
        val seal = resource("legacy-representability.sha256").decodeToString()
        val sealedRow = seal.lineSequence().first()
        assertEquals("${sha256(input)}  tests/fixtures/textwire/$inputPath", sealedRow)
        val directory = Files.createTempDirectory("polski-nj05-readonly-")
        try {
            val file = directory.resolve("progress-v1.json")
            Files.write(file, input) // Input-only fixture injection, never an app codec writer.
            val before = sha256(Files.readAllBytes(file))
            assertEquals(sha256(input), before)
            assertIs<LoadResult.Loaded>(DesktopProgressRepository(directory, FsrsScheduler()).load())
            assertEquals(before, sha256(Files.readAllBytes(file)))
            Files.list(directory).use { entries -> assertEquals(1L, entries.count()) }
        } finally {
            directory.toFile().deleteRecursively()
        }
    }
}
