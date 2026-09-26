package polski.vocabulary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Instant
import polski.srs.FsrsScheduler
import polski.srs.Rating
import polski.data.VocabularyItem

class VocabularyDocumentTest {
    private val at = Instant.parse("2026-09-23T12:00:00Z")
    private val scheduler = FsrsScheduler()

    @Test
    fun schedulesDirectionsIndependentlyAndPreservesHistoryWhenDeselected() {
        val selected = VocabularyCodec.select(VocabularyDocument(), "noun.wife", true)
        assertEquals(listOf("noun.wife"), VocabularyCodec.dueIds(selected, StudyDirection.RussianToPolish, scheduler, at))
        val reviewed = VocabularyCodec.review(selected, "noun.wife", StudyDirection.RussianToPolish, Rating.Good, scheduler, at)
        assertEquals(1, reviewed.cards.getValue(VocabularyCodec.cardKey("noun.wife", StudyDirection.RussianToPolish)).reps)
        assertNull(reviewed.cards[VocabularyCodec.cardKey("noun.wife", StudyDirection.PolishToRussian)])
        assertEquals(listOf("noun.wife"), VocabularyCodec.dueIds(reviewed, StudyDirection.PolishToRussian, scheduler, at))
        val removed = VocabularyCodec.select(reviewed, "noun.wife", false)
        assertEquals(emptyList(), VocabularyCodec.dueIds(removed, StudyDirection.RussianToPolish, scheduler, at))
        assertEquals(reviewed.cards, removed.cards)
        assertEquals(removed, VocabularyCodec.decode(VocabularyCodec.encode(removed)))
    }

    @Test
    fun previewMatchesWhatARealReviewWouldScheduleWithoutWritingAnything() {
        val selected = VocabularyCodec.select(VocabularyDocument(), "noun.wife", true)
        val preview = VocabularyCodec.preview(selected, "noun.wife", StudyDirection.RussianToPolish, scheduler, at)
        assertEquals(0, selected.cards.size) // preview must not write anything
        listOf(Rating.Again, Rating.Good).forEach { rating ->
            val reviewed = VocabularyCodec.review(selected, "noun.wife", StudyDirection.RussianToPolish, rating, scheduler, at)
            assertEquals(reviewed.cards.getValue(VocabularyCodec.cardKey("noun.wife", StudyDirection.RussianToPolish)).due, preview[rating])
        }
        // A never-selected/never-reviewed id still previews (routes through scheduler.newCard, like dueIds does).
        VocabularyCodec.preview(VocabularyDocument(), "noun.wife", StudyDirection.RussianToPolish, scheduler, at)
    }

    @Test
    fun rejectsMalformedAndUnknownSelections() {
        assertFails { VocabularyCodec.decode("""{"version":1,"pair":"pl-ru","selectedIds":["missing"],"custom":[],"cards":{}}""") }
        assertFails { VocabularyCodec.select(VocabularyDocument(), "missing", true) }
        assertFails { VocabularyCodec.validate(VocabularyDocument(custom = listOf(
            VocabularyItem("user.invalid", "dom", "дом", "domu", "To jest dom.", "A1", null, true),
        ))) }
    }

    @Test
    fun mergeRejectsNewCustomLemmaCollisionAndKeepsLegacyShippedHomonym() {
        val firstId = "user.00000000-0000-4000-8000-000000000001"
        val secondId = "user.00000000-0000-4000-8000-000000000002"
        val custom = VocabularyItem(firstId, "szkoła", "школа", "szkoła", "To jest szkoła.", "A1", null, true)
        val current = VocabularyDocument(selectedIds = listOf(firstId), custom = listOf(custom))
        val incoming = VocabularyDocument(custom = listOf(custom.copy(id = secondId, lemma = " SZKOŁA ")))
        assertFailsWith<IllegalArgumentException> { VocabularyCodec.merge(current, incoming) }
        assertEquals(current, VocabularyCodec.validate(current))
        assertFailsWith<IllegalArgumentException> { VocabularyCodec.merge(current,
            VocabularyDocument(custom = listOf(custom.copy(id = secondId, lemma = "ŻONA")))) }

        val legacy = current.copy(custom = listOf(custom.copy(lemma = "żona")))
        assertEquals(legacy, VocabularyCodec.decode(VocabularyCodec.encode(legacy)))
        assertEquals(legacy, VocabularyCodec.merge(legacy, VocabularyDocument()))
        val deferred = custom.copy(id = secondId, lemma = "w")
        assertEquals(legacy.custom + deferred, VocabularyCodec.merge(legacy,
            VocabularyDocument(custom = listOf(deferred))).custom)
    }

    @Test
    fun shippedAndCustomCardsKeepFourIndependentHistoryKeys() {
        val customId = "user.00000000-0000-4000-8000-000000000001"
        val custom = VocabularyItem(customId, "szkoła", "школа", "szkoła", "To jest szkoła.", "A1", null, true)
        val selected = VocabularyDocument(selectedIds = listOf("noun.wife", customId), custom = listOf(custom))
        val directions = listOf(StudyDirection.RussianToPolish, StudyDirection.PolishToRussian)
        val reviewed = listOf("noun.wife", customId).fold(selected) { document, id ->
            directions.fold(document) { next, direction ->
                VocabularyCodec.review(next, id, direction, Rating.Good, scheduler, at)
            }
        }
        val keys = listOf("noun.wife", customId).flatMap { id -> directions.map { VocabularyCodec.cardKey(id, it) } }
        assertEquals(keys.toSet(), reviewed.cards.keys)
        assertEquals(selected.selectedIds, reviewed.selectedIds)
        assertEquals(selected.custom, reviewed.custom)
        assertEquals(reviewed, VocabularyCodec.decode(VocabularyCodec.encode(reviewed)))
        val legacy = reviewed.copy(custom = listOf(custom.copy(lemma = "żona")))
        assertEquals(legacy, VocabularyCodec.merge(VocabularyDocument(), legacy))
    }

    @Test
    fun importedBackupCannotContainTwoCustomIdsWithOneLemma() {
        val first = VocabularyItem("user.00000000-0000-4000-8000-000000000001", "szkoła", "школа",
            "szkoła", "To jest szkoła.", "A1", null, true)
        val second = first.copy(id = "user.00000000-0000-4000-8000-000000000002", lemma = " SZKOŁA ")
        assertFailsWith<IllegalArgumentException> {
            VocabularyCodec.merge(VocabularyDocument(), VocabularyDocument(custom = listOf(first, second)))
        }
    }
}
