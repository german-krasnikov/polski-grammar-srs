package polski.ios

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant
import platform.Foundation.NSUserDefaults
import polski.data.skills
import polski.progress.LoadResult
import polski.progress.ProgressCodec
import polski.progress.SaveResult
import polski.srs.FsrsScheduler

class IosProgressRepositoryTest {
    @Test
    fun invalidImportPreservesSavedProgressAndValidImportKeepsBackup() = runBlocking {
        val suite = "polski-ios-test-${kotlin.random.Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val scheduler = FsrsScheduler()
            val repository = IosProgressRepository(scheduler, defaults)
            val at = Instant.parse("2026-09-23T10:00:00Z")
            val first = ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler)
            assertIs<LoadResult.Missing>(repository.load())
            assertIs<SaveResult.Saved>(repository.save(first))
            assertEquals("Expected JSON object", repository.importJson("{"))
            assertEquals(first.progress, assertIs<LoadResult.Loaded>(repository.load()).document.progress)

            val second = first.copy(progress = first.progress.copy(totalReviews = 7))
            val raw = ProgressCodec.encodeLegacyV1(second)
            assertNull(repository.importJson(raw))
            assertEquals(7, assertIs<LoadResult.Loaded>(repository.load()).document.progress.totalReviews)
            val backups = defaults.dictionaryRepresentation().keys.filter { it.toString().startsWith("polski-progress-import-backup-") }
            assertEquals(1, backups.size)
            assertEquals(ProgressCodec.encodeLegacyV1(first), defaults.stringForKey(backups.single().toString()))
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }

    @Test
    fun repeatedValidImportsKeepOnlyTheLatestBackup() = runBlocking {
        val suite = "polski-ios-test-${kotlin.random.Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val scheduler = FsrsScheduler()
            val repository = IosProgressRepository(scheduler, defaults)
            val at = Instant.parse("2026-09-23T10:00:00Z")
            val first = ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler)
            assertIs<SaveResult.Saved>(repository.save(first))

            val second = first.copy(progress = first.progress.copy(totalReviews = 7))
            assertNull(repository.importJson(ProgressCodec.encodeLegacyV1(second)))
            val third = second.copy(progress = second.progress.copy(totalReviews = 11))
            assertNull(repository.importJson(ProgressCodec.encodeLegacyV1(third)))

            val backups = defaults.dictionaryRepresentation().keys.filter { it.toString().startsWith("polski-progress-import-backup-") }
            assertEquals(1, backups.size, "Only the most recent pre-import backup should be kept, not one per import")
            assertEquals(ProgressCodec.encodeLegacyV1(second), defaults.stringForKey(backups.single().toString()))
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }
}
