package polski.vocabulary

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant
import polski.srs.FsrsScheduler
import polski.srs.Rating

class VocabularySessionTest {
    private val at = Instant.parse("2026-09-23T12:00:00Z")

    @Test
    fun savesRatingsOnlyAfterRevealAndKeepsDirectionsSeparate() = runTest {
        val repository = MemoryVocabularyRepository()
        val session = VocabularySession(repository, FsrsScheduler(), { at }, { "user.00000000-0000-4000-8000-000000000001" })
        session.start()
        assertTrue(session.select("noun.wife", true))
        assertEquals("noun.wife", session.state.value.currentId)
        assertFalse(session.rate(Rating.Good))
        assertTrue(session.reveal())
        assertTrue(session.rate(Rating.Good))
        assertTrue(VocabularyCodec.cardKey("noun.wife", StudyDirection.RussianToPolish) in session.state.value.document.cards)
        session.setDirection(StudyDirection.PolishToRussian)
        assertEquals("noun.wife", session.state.value.currentId)
        assertTrue(session.reveal())
        assertTrue(session.rate(Rating.Again))
        assertTrue(VocabularyCodec.cardKey("noun.wife", StudyDirection.PolishToRussian) in session.state.value.document.cards)
        assertEquals(session.state.value.document, VocabularyCodec.decode(repository.raw!!))
    }

    @Test
    fun failedWriteLeavesRevealedCardAndHistoryUntouched() = runTest {
        val repository = MemoryVocabularyRepository()
        val session = VocabularySession(repository, FsrsScheduler(), { at }, { "user.00000000-0000-4000-8000-000000000001" })
        session.start()
        session.select("noun.wife", true)
        session.reveal()
        repository.failWrite = true
        assertFalse(session.rate(Rating.Good))
        assertTrue(session.state.value.revealed)
        assertEquals("noun.wife", session.state.value.currentId)
        assertTrue(session.state.value.document.cards.isEmpty())
    }

    @Test
    fun invalidBytesRequireRecoveryAndAreBackedUpBeforeImport() = runTest {
        val repository = MemoryVocabularyRepository("{broken")
        val session = VocabularySession(repository, FsrsScheduler(), { at }, { "user.00000000-0000-4000-8000-000000000001" })
        session.start()
        assertEquals(VocabularyLoadStatus.RecoveryRequired, session.state.value.loadStatus)
        assertEquals("{broken", session.exportJson())
        assertFalse(session.select("noun.wife", true))
        assertEquals("{broken", repository.raw)
        assertTrue(session.importJson(VocabularyCodec.encode(VocabularyDocument(selectedIds = listOf("noun.wife")))))
        assertEquals(listOf("{broken"), repository.backups)
        assertEquals(VocabularyLoadStatus.Ready, session.state.value.loadStatus)
    }

    @Test
    fun customEditsKeepIdentityAndDeleteKeepsReviewHistory() = runTest {
        val repository = MemoryVocabularyRepository()
        val session = VocabularySession(repository, FsrsScheduler(), { at }, { "user.00000000-0000-4000-8000-000000000001" })
        session.start()
        assertTrue(session.saveCustom("żubr", "зубр", "żubra", "Widzę żubra.", "A2"))
        val id = session.state.value.document.custom.single().id
        assertTrue(session.select(id, true))
        assertTrue(session.reveal())
        assertTrue(session.rate(Rating.Good))
        val key = VocabularyCodec.cardKey(id, StudyDirection.RussianToPolish)
        assertTrue(session.saveCustom("żubr", "европейский зубр", "żubra", "Widzę żubra.", "A2", id))
        assertEquals(id, session.state.value.document.custom.single().id)
        assertTrue(key in session.state.value.document.cards)
        assertTrue(session.deleteCustom(id))
        assertTrue(session.state.value.document.custom.isEmpty())
        assertTrue(session.state.value.document.selectedIds.isEmpty())
        assertTrue(key in session.state.value.document.cards)
    }
}

private class MemoryVocabularyRepository(var raw: String? = null) : VocabularyRepository {
    var failWrite = false
    val backups = mutableListOf<String>()
    override suspend fun loadRaw(): String? = raw
    override suspend fun saveRaw(value: String, backupCurrent: Boolean) {
        if (failWrite) error("storage failed")
        if (backupCurrent && raw != null) backups += raw!!
        raw = value
    }
}
