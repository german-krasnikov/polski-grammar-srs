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

class ProgressRepositoryTest {
    private val at = Instant.parse("2026-03-28T23:30:00Z")
    private val scheduler = object : Scheduler {
        override fun newCard(skillId: String, at: Instant) = StoredCard(skillId, SrsCard(at))
        override fun preview(card: StoredCard, at: Instant) = SchedulePreview(at, at, at, at)
        override fun review(card: StoredCard, rating: Rating, at: Instant) = card
        override fun isDue(card: StoredCard, at: Instant) = false
    }

    @Test
    fun migrationBacksUpExactRawAndRetryAfterMarkerFailureKeepsPreview() {
        val store = FakeStore()
        val legacy = legacyRaw()
        store.values[RawProgressRepository.LEGACY_KEY] = legacy
        store.failPut = RawProgressRepository.MARKER_KEY
        val repository = RawProgressRepository(store, scheduler)

        assertIs<MigrationResult.Unavailable>(immediate { repository.migrateLegacy(at, "2026-03-29", listOf("known", "new")) })
        assertEquals(legacy, store.values[RawProgressRepository.LEGACY_KEY])
        assertEquals(legacy, store.values[RawProgressRepository.BACKUP_KEY])
        assertNull(store.values[RawProgressRepository.MARKER_KEY])
        val preview = store.values.getValue(RawProgressRepository.PREVIEW_KEY)
        store.failPut = null

        val resumed = assertIs<MigrationResult.Migrated>(immediate {
            repository.migrateLegacy(Instant.parse("2026-03-30T12:00:00Z"), "2026-03-30", listOf("known", "new"))
        })
        assertEquals(preview, store.values[RawProgressRepository.PREVIEW_KEY])
        assertEquals("2026-03-29", resumed.document.progress.lastDay)
        assertEquals(listOf("known", "new"), resumed.document.progress.cards.map { it.skillId })
        assertIs<MigrationResult.AlreadyMigrated>(immediate {
            repository.migrateLegacy(at, "2026-03-29", listOf("known", "new"))
        })
    }

    @Test
    fun invalidAndUnsupportedLegacyRemainRecoverableWithoutPreview() {
        for (raw in listOf("{", legacyRaw().replace("\"version\":1", "\"version\":99"))) {
            val store = FakeStore()
            store.values[RawProgressRepository.LEGACY_KEY] = raw
            val result = immediate { RawProgressRepository(store, scheduler).migrateLegacy(at, "2026-03-29", listOf("known")) }
            assertIs<MigrationResult.RecoveryRequired>(result)
            assertEquals(raw, store.values[RawProgressRepository.BACKUP_KEY])
            assertNull(store.values[RawProgressRepository.PREVIEW_KEY])
            assertNull(store.values[RawProgressRepository.MARKER_KEY])
        }
    }

    @Test
    fun writeFailureRetainsCurrentExportAndResetTouchesOnlyPreview() {
        val store = FakeStore()
        val repository = RawProgressRepository(store, scheduler)
        val document = ProgressCodec.fresh(listOf("known"), at, "2026-03-29", scheduler)
        store.failPut = RawProgressRepository.PREVIEW_KEY
        assertIs<SaveResult.WriteFailed>(immediate { repository.save(document) })
        assertIs<DecodeResult.Valid>(ProgressCodec.decode(ProgressCodec.encodeLegacyV1(document)))
        assertEquals(0, document.progress.totalReviews)
        store.failPut = null
        store.values[RawProgressRepository.LEGACY_KEY] = legacyRaw()
        assertIs<SaveResult.WriteFailed>(immediate { repository.resetConfirmed(at, "2026-03-29", listOf("known")) })
        assertNull(store.values[RawProgressRepository.PREVIEW_KEY])
        assertIs<MigrationResult.Migrated>(immediate { repository.migrateLegacy(at, "2026-03-29", listOf("known")) })
        assertEquals(SaveResult.Saved, immediate { repository.resetConfirmed(at, "2026-03-29", listOf("known")) })
        assertEquals(legacyRaw(), store.values[RawProgressRepository.LEGACY_KEY])
        assertEquals(legacyRaw(), store.values[RawProgressRepository.BACKUP_KEY])
        assertIs<LoadResult.Loaded>(immediate { repository.load() })
    }

    @Test
    fun readFailureIsUnavailableAndMarkedPreviewCorruptionRequiresRecovery() {
        val store = FakeStore()
        val repository = RawProgressRepository(store, scheduler)
        store.failGet = RawProgressRepository.PREVIEW_KEY
        assertIs<LoadResult.Unavailable>(immediate { repository.load() })
        store.failGet = null
        store.values[RawProgressRepository.LEGACY_KEY] = legacyRaw()
        assertIs<MigrationResult.Migrated>(immediate { repository.migrateLegacy(at, "2026-03-29", listOf("known")) })
        store.values[RawProgressRepository.PREVIEW_KEY] = "{"
        assertIs<MigrationResult.RecoveryRequired>(immediate { repository.migrateLegacy(at, "2026-03-29", listOf("known")) })
        assertEquals(legacyRaw(), store.values[RawProgressRepository.BACKUP_KEY])
    }

    @Test
    fun loadSignalsLegacyOrPartialMigrationBeforeFreshPreviewWrite() {
        val store = FakeStore()
        val repository = RawProgressRepository(store, scheduler)
        assertEquals(LoadResult.Missing, immediate { repository.load() })
        val legacy = legacyRaw()
        store.values[RawProgressRepository.LEGACY_KEY] = legacy
        assertEquals(LoadResult.LegacyAvailable(legacy), immediate { repository.load() })
        val fresh = ProgressCodec.fresh(listOf("known"), at, "2026-03-29", scheduler)
        assertIs<SaveResult.WriteFailed>(immediate { repository.save(fresh) })
        assertNull(store.values[RawProgressRepository.PREVIEW_KEY])
        store.values.remove(RawProgressRepository.LEGACY_KEY)
        store.values[RawProgressRepository.BACKUP_KEY] = legacy
        assertIs<LoadResult.RecoveryRequired>(immediate { repository.load() })
        assertNull(store.values[RawProgressRepository.PREVIEW_KEY])

        store.values[RawProgressRepository.LEGACY_KEY] = legacy
        store.values[RawProgressRepository.PREVIEW_KEY] = legacy
        assertIs<LoadResult.RecoveryRequired>(immediate { repository.load() })
        store.values[RawProgressRepository.MARKER_KEY] = "invalid-marker"
        assertIs<LoadResult.RecoveryRequired>(immediate { repository.load() })
    }

    private fun legacyRaw(): String = ProgressCodec.encodeLegacyV1(
        ProgressCodec.fresh(listOf("known"), at, "2026-03-28", scheduler),
    )

    private class FakeStore : RawKeyValueStore {
        val values = mutableMapOf<String, String>()
        var failGet: String? = null
        var failPut: String? = null
        override fun get(key: String): String? {
            if (key == failGet) error("Read blocked")
            return values[key]
        }
        override fun put(key: String, value: String) {
            if (key == failPut) error("Write blocked")
            values[key] = value
        }
    }

    private fun <T> immediate(block: suspend () -> T): T {
        var completion: Result<T>? = null
        block.startCoroutine(object : Continuation<T> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) { completion = result }
        })
        assertTrue(completion != null, "Repository unexpectedly suspended")
        return requireNotNull(completion).getOrThrow()
    }
}
