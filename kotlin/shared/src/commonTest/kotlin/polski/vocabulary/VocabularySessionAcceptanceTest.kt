package polski.vocabulary

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import polski.data.VocabularyItem
import kotlin.time.Instant
import polski.srs.FsrsScheduler
import polski.srs.Rating

class VocabularySessionAcceptanceTest {
    private val at = Instant.parse("2026-09-24T12:00:00Z")
    private val validId = "user.00000000-0000-4000-8000-000000000001"

    @Test
    fun readBackMismatchReconcilesCommittedReviewBeforeAnotherRating() = runTest {
        val repository = FaultRepository()
        val session = VocabularySession(repository, FsrsScheduler(), { at }, { validId })
        session.start()
        assertTrue(session.select("noun.wife", true))
        assertTrue(session.reveal())
        repository.readBackMismatch = true

        assertTrue(session.rate(Rating.Good))
        assertTrue(session.state.value.document.cards.isNotEmpty())
        assertFalse(session.rate(Rating.Good))
        assertFalse(session.state.value.busy)
        assertEquals(repository.raw, VocabularyCodec.encode(session.state.value.document))
    }

    @Test
    fun unreadablePostWriteStorageBlocksRetryUntilRecovery() = runTest {
        val repository = FaultRepository()
        val session = VocabularySession(repository, FsrsScheduler(), { at }, { validId })
        session.start()
        assertTrue(session.select("noun.wife", true))
        assertTrue(session.reveal())
        repository.readBackFailure = true

        assertFalse(session.rate(Rating.Good))
        assertEquals(VocabularyLoadStatus.Unavailable, session.state.value.loadStatus)
        assertFalse(session.state.value.busy)
        assertFalse(session.rate(Rating.Good))
        assertNotNull(session.state.value.error)
        repository.readBackFailure = false
        val reopened = VocabularySession(repository, FsrsScheduler(), { at }, { validId })
        reopened.start()
        assertTrue(reopened.state.value.document.cards.isNotEmpty())
    }

    @Test
    fun cancellationAfterWriteDoesNotStrandBusyStateOrDuplicateReview() = runTest {
        val repository = FaultRepository()
        val session = VocabularySession(repository, FsrsScheduler(), { at }, { validId })
        session.start()
        assertTrue(session.select("noun.wife", true))
        assertTrue(session.reveal())
        repository.cancelAfterWrite = true

        assertFailsWith<CancellationException> { session.rate(Rating.Good) }
        assertFalse(session.state.value.busy)
        assertTrue(session.state.value.document.cards.isNotEmpty())
        assertFalse(session.rate(Rating.Good))
    }

    @Test
    fun cancelledInFlightWriteCanBeSafelyRetried() = runTest {
        val repository = FaultRepository()
        val session = VocabularySession(repository, FsrsScheduler(), { at }, { validId })
        session.start()
        assertTrue(session.select("noun.wife", true))
        assertTrue(session.reveal())
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        repository.writeStarted = started
        repository.stallBeforeWrite = release

        val job = launch { session.rate(Rating.Good) }
        started.await()
        job.cancelAndJoin()
        assertFalse(session.state.value.busy)
        assertEquals(VocabularyLoadStatus.Ready, session.state.value.loadStatus)
        assertTrue(session.state.value.document.cards.isEmpty())
        repository.stallBeforeWrite = null
        assertTrue(session.rate(Rating.Good))
        assertTrue(session.state.value.document.cards.isNotEmpty())
    }

    @Test
    fun invalidImportDoesNotBackUpOrReplaceExistingDocument() = runTest {
        val repository = FaultRepository()
        val session = VocabularySession(repository, FsrsScheduler(), { at }, { validId })
        session.start()
        assertTrue(session.select("noun.wife", true))
        val beforeRaw = repository.raw
        val before = session.state.value.document

        assertFalse(session.importJson("{broken"))
        assertEquals(beforeRaw, repository.raw)
        assertEquals(before, session.state.value.document)
        assertEquals(0, repository.backupRequests)
        assertFalse(session.state.value.busy)
    }

    @Test
    fun conflictingImportedCustomLemmaFailsBeforeAnyWrite() = runTest {
        val first = VocabularyItem(validId, "szkoła", "школа", "szkoła", "To jest szkoła.", "A1", null, true)
        val current = VocabularyDocument(selectedIds = listOf(validId), custom = listOf(first))
        val repository = FaultRepository().apply { raw = VocabularyCodec.encode(current) }
        val session = VocabularySession(repository, FsrsScheduler(), { at }, { validId })
        session.start()
        val beforeRaw = repository.raw
        val imported = VocabularyDocument(custom = listOf(first.copy(
            id = "user.00000000-0000-4000-8000-000000000002", lemma = " SZKOŁA ",
        )))

        assertFalse(session.importJson(VocabularyCodec.encode(imported)))
        assertEquals(beforeRaw, repository.raw)
        assertEquals(current, session.state.value.document)
        assertEquals(0, repository.backupRequests)
        assertTrue(session.state.value.error?.contains("уже есть") == true)
    }

    @Test
    fun recoveryRestoresLegacyHomonymAndFourHistoriesOnlyAfterValidImport() = runTest {
        val repository = FaultRepository().apply { raw = "{broken" }
        val session = VocabularySession(repository, FsrsScheduler(), { at }, { validId })
        session.start()
        assertEquals(VocabularyLoadStatus.RecoveryRequired, session.state.value.loadStatus)
        assertEquals("{broken", session.exportJson())
        val custom = VocabularyItem(validId, "żona", "супруга", "żona", "Moja żona czyta.", "A1", null, true)
        val selected = VocabularyDocument(selectedIds = listOf("noun.wife", validId), custom = listOf(custom))
        val scheduler = FsrsScheduler()
        val repaired = listOf("noun.wife", validId).fold(selected) { document, id ->
            listOf(StudyDirection.RussianToPolish, StudyDirection.PolishToRussian).fold(document) { next, direction ->
                VocabularyCodec.review(next, id, direction, Rating.Good, scheduler, at)
            }
        }
        val validRaw = VocabularyCodec.encode(repaired)

        assertFalse(session.importJson(validRaw.replace("\"pair\":\"pl-ru\"", "\"pair\":\"de-ru\"")))
        assertFalse(session.importJson("x".repeat(10_000_001)))
        assertEquals("{broken", repository.raw)
        assertEquals(0, repository.backupRequests)
        assertTrue(session.importJson(validRaw))
        assertEquals(1, repository.backupRequests)
        assertEquals(repaired, session.state.value.document)
        assertEquals(4, session.state.value.document.cards.size)
        assertEquals(repaired, VocabularyCodec.decode(repository.raw!!))
    }

    @Test
    fun cleanProfileImportsLegacyShippedHomonymWithBothReviewDirections() = runTest {
        val repository = FaultRepository()
        val session = VocabularySession(repository, FsrsScheduler(), { at }, { validId })
        session.start()
        val custom = VocabularyItem(validId, "żona", "супруга", "żona", "Moja żona czyta.", "A1", null, true)
        val selected = VocabularyDocument(selectedIds = listOf("noun.wife", validId), custom = listOf(custom))
        val scheduler = FsrsScheduler()
        val backup = listOf("noun.wife", validId).fold(selected) { document, id ->
            listOf(StudyDirection.RussianToPolish, StudyDirection.PolishToRussian).fold(document) { next, direction ->
                VocabularyCodec.review(next, id, direction, Rating.Good, scheduler, at)
            }
        }

        assertTrue(session.importJson(VocabularyCodec.encode(backup)))
        assertEquals(backup, session.state.value.document)
        assertEquals(backup, VocabularyCodec.decode(repository.raw!!))
        assertEquals(4, session.state.value.document.cards.size)
    }

    @Test
    fun directionSwitchClearsRevealAndDraftWithoutWritingOrReviewing() = runTest {
        val repository = FaultRepository()
        val session = VocabularySession(repository, FsrsScheduler(), { at }, { validId })
        session.start()
        assertTrue(session.select("noun.wife", true))
        session.setTyped(true)
        session.setDraft("żona")
        assertTrue(session.reveal())
        val saved = repository.saves

        session.setDirection(StudyDirection.PolishToRussian)

        assertEquals("noun.wife", session.state.value.currentId)
        assertFalse(session.state.value.revealed)
        assertEquals("", session.state.value.draft)
        assertEquals(saved, repository.saves)
        assertTrue(session.state.value.document.cards.isEmpty())
        assertFalse(session.rate(Rating.Good))
    }

    private class FaultRepository : VocabularyRepository {
        var raw: String? = null
        var saves = 0
        var backupRequests = 0
        var readBackMismatch = false
        var readBackFailure = false
        var cancelAfterWrite = false
        var stallBeforeWrite: CompletableDeferred<Unit>? = null
        var writeStarted: CompletableDeferred<Unit>? = null
        private var mismatchPending = false

        override suspend fun loadRaw(): String? {
            if (readBackFailure) error("Disk unavailable")
            if (mismatchPending) {
                mismatchPending = false
                return "{mismatched"
            }
            return raw
        }

        override suspend fun saveRaw(value: String, backupCurrent: Boolean) {
            saves++
            if (backupCurrent) backupRequests++
            stallBeforeWrite?.let { gate -> writeStarted?.complete(Unit); gate.await() }
            raw = value
            if (cancelAfterWrite) throw CancellationException("Host closed")
            if (readBackMismatch) mismatchPending = true
        }
    }
}
