package polski.desktop

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant
import polski.progress.LoadResult
import polski.progress.ProgressCodec
import polski.progress.SaveResult
import polski.srs.FsrsScheduler
import java.nio.file.Files

class DesktopProgressRepositoryTest {
    private val at = Instant.parse("2026-02-03T12:00:00Z")
    private val scheduler = FsrsScheduler()

    @Test
    fun saveSurvivesReopenAndInvalidBytesAreNeverOverwritten() = runBlocking {
        val directory = Files.createTempDirectory("polski-desktop-test-")
        try {
            val repository = DesktopProgressRepository(directory, scheduler)
            val document = ProgressCodec.fresh(listOf("case.acc.f"), at, "2026-02-03", scheduler)
            assertIs<LoadResult.Missing>(repository.load())
            assertIs<SaveResult.Saved>(repository.save(document))
            val reopened = DesktopProgressRepository(directory, scheduler)
            assertEquals(document.progress, assertIs<LoadResult.Loaded>(reopened.load()).document.progress)

            Files.writeString(directory.resolve("progress-v1.json"), "{")
            assertIs<LoadResult.Invalid>(reopened.load())
            assertIs<SaveResult.WriteFailed>(reopened.save(document))
            assertEquals("{", Files.readString(directory.resolve("progress-v1.json")))
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    @Test
    fun importValidBrowserJsonBacksUpOldSaveAndRejectsMalformedInput() = runBlocking {
        val directory = Files.createTempDirectory("polski-desktop-test-")
        try {
            val repository = DesktopProgressRepository(directory, scheduler)
            val old = ProgressCodec.fresh(listOf("case.acc.f"), at, "2026-02-03", scheduler)
            val imported = old.copy(progress = old.progress.copy(totalReviews = 7))
            val oldRaw = ProgressCodec.encodeLegacyV1(old)
            val importedRaw = ProgressCodec.encodeLegacyV1(imported)
            assertIs<SaveResult.Saved>(repository.save(old))
            assertIs<DesktopImportResult.Invalid>(repository.importJson("{"))
            assertEquals(oldRaw, Files.readString(directory.resolve("progress-v1.json")))
            assertIs<DesktopImportResult.Imported>(repository.importJson(importedRaw))
            assertEquals(7, assertIs<LoadResult.Loaded>(repository.load()).document.progress.totalReviews)
            val backups = Files.list(directory.resolve("backups")).use { it.toList() }
            assertEquals(1, backups.size)
            assertEquals(oldRaw, Files.readString(backups.single()))
            assertTrue(Files.readString(directory.resolve("progress-v1.json")).contains("\"totalReviews\":7"))
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    @Test
    fun reactBaselineProgressFixtureImportsAndExportsWithAllSkills() = runBlocking {
        val directory = Files.createTempDirectory("polski-desktop-react-")
        try {
            // Copied from P-fresh in tests/fixtures/kotlin-parity/progress.json.
            val raw = checkNotNull(javaClass.getResource("/react-progress-v1.json")).readText()
            val repository = DesktopProgressRepository(directory, scheduler)
            assertIs<DesktopImportResult.Imported>(repository.importJson(raw))
            val loaded = assertIs<LoadResult.Loaded>(repository.load()).document
            assertEquals(16, loaded.progress.cards.size)
            assertEquals(16, loaded.progress.stats.size)
            assertEquals(0, loaded.progress.totalReviews)
            assertEquals(loaded.progress, assertIs<polski.progress.DecodeResult.Valid>(
                ProgressCodec.decode(ProgressCodec.encodeLegacyV1(loaded)),
            ).document.progress)
        } finally {
            directory.toFile().deleteRecursively()
        }
    }
}
