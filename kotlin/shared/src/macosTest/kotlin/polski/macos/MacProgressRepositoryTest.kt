package polski.macos

import kotlinx.coroutines.runBlocking
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.time.Instant
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import platform.Foundation.writeToFile
import polski.progress.LoadResult
import polski.progress.ProgressCodec
import polski.progress.SaveResult
import polski.srs.FsrsScheduler
import polski.srs.Rating

@OptIn(ExperimentalForeignApi::class)
class MacProgressRepositoryTest {
    @Test
    fun reviewedFsrsJsonSurvivesRepositoryRestart() { runBlocking {
        val directory = NSTemporaryDirectory() + "polski-mac-progress-${NSUUID().UUIDString}"
        try {
            val scheduler = FsrsScheduler()
            val at = Instant.parse("2026-03-28T23:30:00Z")
            val repository = MacProgressRepository(directory, scheduler)
            val fresh = ProgressCodec.fresh(listOf("G-POSS"), at, "2026-03-29", scheduler)
            val reviewed = scheduler.review(fresh.progress.cards.single(), Rating.Good, at)
            val document = fresh.copy(progress = fresh.progress.copy(cards = listOf(reviewed), totalReviews = 1))
            val expectedRaw = ProgressCodec.encodeLegacyV1(document)

            assertEquals(SaveResult.Saved, repository.save(document))
            assertEquals(expectedRaw, repository.recoveryRaw())
            val reopened = MacProgressRepository(directory, scheduler)
            val loaded = assertIs<LoadResult.Loaded>(reopened.load())
            assertEquals(expectedRaw, ProgressCodec.encodeLegacyV1(loaded.document))
            assertEquals(reviewed, loaded.document.progress.cards.single())
            assertEquals(1, loaded.document.progress.totalReviews)
            assertNotNull(loaded.document.progress.cards.single().card.lastReview)
        } finally {
            NSFileManager.defaultManager.removeItemAtPath(directory, null)
        }
    } }

    @Test
    fun corruptAndNewerFilesStayRecoverableUntilExplicitValidImport() { runBlocking {
        val directory = NSTemporaryDirectory() + "polski-mac-recovery-${NSUUID().UUIDString}"
        val manager = NSFileManager.defaultManager
        try {
            manager.createDirectoryAtPath(directory, true, null, null)
            val file = "$directory/progress-v1.json"
            val scheduler = FsrsScheduler()
            val repository = MacProgressRepository(directory, scheduler)
            val at = Instant.parse("2026-03-28T23:30:00Z")
            val valid = ProgressCodec.fresh(listOf("G-POSS"), at, "2026-03-29", scheduler)
            val raw = ProgressCodec.encodeLegacyV1(valid)
            for (invalid in listOf("{", raw.replace("\"version\":1", "\"version\":99"))) {
                (invalid as NSString).writeToFile(file, true, NSUTF8StringEncoding, null)
                val load = repository.load()
                if (invalid == "{") assertIs<LoadResult.Invalid>(load)
                else assertIs<LoadResult.Unsupported>(load)
                assertIs<SaveResult.WriteFailed>(repository.save(valid))
                assertEquals(invalid, repository.recoveryRaw())
            }
            assertEquals(null, repository.importJson(raw))
            assertEquals(raw, repository.recoveryRaw())
            val names = manager.contentsOfDirectoryAtPath("$directory/backups", null).orEmpty()
            assertEquals(1, names.size)
        } finally { manager.removeItemAtPath(directory, null) }
    } }
}
