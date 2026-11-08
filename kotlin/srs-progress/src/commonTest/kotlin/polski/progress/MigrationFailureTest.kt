package polski.progress

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import polski.srs.Rating
import polski.srs.SchedulePreview
import polski.srs.Scheduler
import polski.srs.StoredCard
import polski.srs.SrsCard

/** Exercises every raw operation in the migration protocol, including read-back. */
class MigrationFailureTest {
    private val at = Instant.parse("2026-03-29T01:30:00Z")
    private val scheduler = object : Scheduler {
        override fun newCard(skillId: String, at: Instant) = StoredCard(skillId, SrsCard(at))
        override fun preview(card: StoredCard, at: Instant) = SchedulePreview(at, at, at, at)
        override fun review(card: StoredCard, rating: Rating, at: Instant) = card
        override fun isDue(card: StoredCard, at: Instant) = false
    }

    @Test
    fun everySingleOperationFailurePreservesLegacyAndCanBeRetried() {
        val legacy = ProgressCodec.encodeLegacyV1(
            ProgressCodec.fresh(listOf("known"), at, "2026-03-28", scheduler),
        )
        val operationCount = recordingMigration(legacy).operations.size
        assertTrue(operationCount >= 11, "Migration must exercise backup, preview and marker read-backs")

        for (failedOperation in 1..operationCount) {
            val store = FaultStore(mutableMapOf(RawProgressRepository.LEGACY_KEY to legacy))
            val repository = RawProgressRepository(store, scheduler)
            store.failAt = failedOperation
            assertIs<MigrationResult.Unavailable>(immediate {
                repository.migrateLegacy(at, "2026-03-29", listOf("known", "new"))
            }, "operation $failedOperation: ${store.operations.lastOrNull()}")
            assertEquals(legacy, store.values[RawProgressRepository.LEGACY_KEY], "operation $failedOperation")
            assertEquals(null, store.legacyWrites, "operation $failedOperation")

            store.failAt = null
            store.operations.clear()
            val retry = immediate { repository.migrateLegacy(at, "2026-03-29", listOf("known", "new")) }
            when (retry) {
                is MigrationResult.Migrated -> assertEquals(2, retry.document.progress.cards.size)
                is MigrationResult.AlreadyMigrated -> assertEquals(2, retry.document.progress.cards.size)
                else -> error("Operation $failedOperation failed recovery: $retry")
            }
            assertEquals(legacy, store.values[RawProgressRepository.BACKUP_KEY], "operation $failedOperation")
            assertIs<LoadResult.Loaded>(immediate { repository.load() }, "operation $failedOperation")
        }
    }

    @Test
    fun silentBackupPreviewAndMarkerMismatchesStopBeforeCompletion() {
        val legacy = ProgressCodec.encodeLegacyV1(
            ProgressCodec.fresh(listOf("known"), at, "2026-03-28", scheduler),
        )
        for (key in listOf(
            RawProgressRepository.BACKUP_KEY,
            RawProgressRepository.PREVIEW_KEY,
            RawProgressRepository.MARKER_KEY,
        )) {
            val store = FaultStore(mutableMapOf(RawProgressRepository.LEGACY_KEY to legacy))
            store.corruptPut = key
            val result = immediate {
                RawProgressRepository(store, scheduler).migrateLegacy(at, "2026-03-29", listOf("known"))
            }
            assertIs<MigrationResult.RecoveryRequired>(result, key)
            assertEquals(legacy, store.values[RawProgressRepository.LEGACY_KEY], key)
            if (key != RawProgressRepository.MARKER_KEY) {
                assertNull(store.values[RawProgressRepository.MARKER_KEY], key)
            }
        }
    }

    private fun recordingMigration(legacy: String): FaultStore {
        val store = FaultStore(mutableMapOf(RawProgressRepository.LEGACY_KEY to legacy))
        assertIs<MigrationResult.Migrated>(immediate {
            RawProgressRepository(store, scheduler).migrateLegacy(at, "2026-03-29", listOf("known", "new"))
        })
        return store
    }

    private class FaultStore(val values: MutableMap<String, String>) : RawKeyValueStore {
        val operations = mutableListOf<String>()
        var failAt: Int? = null
        var corruptPut: String? = null
        var legacyWrites: Int? = null

        override fun get(key: String): String? {
            operations += "get:$key"
            if (operations.size == failAt) error("injected get failure")
            return values[key]
        }

        override fun put(key: String, value: String) {
            operations += "put:$key"
            if (key == RawProgressRepository.LEGACY_KEY) legacyWrites = (legacyWrites ?: 0) + 1
            if (operations.size == failAt) error("injected put failure")
            values[key] = if (key == corruptPut) "corrupted" else value
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
