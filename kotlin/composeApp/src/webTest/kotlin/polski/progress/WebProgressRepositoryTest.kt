package polski.progress

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.time.Instant
import kotlinx.browser.window
import polski.platform.WebProgressRepository
import polski.srs.Rating
import polski.srs.SchedulePreview
import polski.srs.Scheduler
import polski.srs.StoredCard
import polski.srs.SrsCard

/** Runs against the browser's actual LocalStorage in both JS and Wasm test bundles. */
class WebProgressRepositoryTest {
    private val keys = listOf(
        RawProgressRepository.LEGACY_KEY,
        RawProgressRepository.PREVIEW_KEY,
        RawProgressRepository.BACKUP_KEY,
        RawProgressRepository.MARKER_KEY,
    )
    private val at = Instant.parse("2026-03-29T01:30:00Z")
    private val scheduler = object : Scheduler {
        override fun newCard(skillId: String, at: Instant) = StoredCard(skillId, SrsCard(at))
        override fun preview(card: StoredCard, at: Instant) = SchedulePreview(at, at, at, at)
        override fun review(card: StoredCard, rating: Rating, at: Instant) = card
        override fun isDue(card: StoredCard, at: Instant) = false
    }

    @Test
    fun browserStorageMigratesReloadsAndResetsWithoutChangingLegacy() {
        val original = keys.associateWith { window.localStorage.getItem(it) }
        try {
            keys.forEach(window.localStorage::removeItem)
            val legacy = ProgressCodec.encodeLegacyV1(ProgressCodec.fresh(listOf("known"), at, "2026-03-28", scheduler))
            window.localStorage.setItem(RawProgressRepository.LEGACY_KEY, legacy)
            val repository = WebProgressRepository(scheduler)

            val migrated = assertIs<MigrationResult.Migrated>(immediate {
                repository.migrateLegacy(at, "2026-03-29", listOf("known", "new"))
            })
            assertEquals(legacy, window.localStorage.getItem(RawProgressRepository.LEGACY_KEY))
            assertEquals(legacy, window.localStorage.getItem(RawProgressRepository.BACKUP_KEY))
            assertNotNull(window.localStorage.getItem(RawProgressRepository.MARKER_KEY))
            assertEquals(2, migrated.document.progress.cards.size)
            assertIs<LoadResult.Loaded>(immediate { WebProgressRepository(scheduler).load() })

            val revised = migrated.document.copy(progress = migrated.document.progress.copy(totalReviews = 8))
            assertEquals(SaveResult.Saved, immediate { repository.save(revised) })
            assertEquals(8, assertIs<LoadResult.Loaded>(immediate { repository.load() }).document.progress.totalReviews)
            assertEquals(SaveResult.Saved, immediate { repository.resetConfirmed(at, "2026-03-29", listOf("known")) })
            assertEquals(0, assertIs<LoadResult.Loaded>(immediate { repository.load() }).document.progress.totalReviews)
            assertEquals(legacy, window.localStorage.getItem(RawProgressRepository.LEGACY_KEY))
            assertEquals(legacy, window.localStorage.getItem(RawProgressRepository.BACKUP_KEY))
        } finally {
            for ((key, value) in original) {
                if (value == null) window.localStorage.removeItem(key) else window.localStorage.setItem(key, value)
            }
        }
    }

    private fun <T> immediate(block: suspend () -> T): T {
        var completion: Result<T>? = null
        block.startCoroutine(object : Continuation<T> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) { completion = result }
        })
        return requireNotNull(completion) { "Repository unexpectedly suspended" }.getOrThrow()
    }
}
