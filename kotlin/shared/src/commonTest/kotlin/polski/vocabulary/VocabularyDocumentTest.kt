package polski.vocabulary

import kotlinx.serialization.json.Json
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

    /** UC-04: [VocabularyCodec.key]/[VocabularyCodec.cardKey]/[VocabularyCodec.encode] now read
     *  `packRegistry.active.pairId` instead of a `"pl-ru"` literal — pin the exact wire bytes so
     *  that refactor cannot silently change the storage key or the document shape. */
    @Test
    fun goldenDocumentRoundTripsByteIdentical() {
        val reviewed = VocabularyCodec.review(
            VocabularyCodec.select(VocabularyDocument(), "noun.wife", true),
            "noun.wife", StudyDirection.RussianToPolish, Rating.Good, scheduler, at,
        )
        val golden = """{"version":1,"pair":"pl-ru","selectedIds":["noun.wife"],"custom":[],""" +
            """"cards":{"pl-ru:vocabulary:ru-pl:noun.wife":{"due":"2026-09-23T12:10:00.000Z",""" +
            """"stability":2.3065,"difficulty":2.11810397,"elapsed_days":0,"scheduled_days":0,""" +
            """"reps":1,"lapses":0,"learning_steps":1,"state":1,"last_review":"2026-09-23T12:00:00.000Z"}}}"""
        assertEquals(golden, VocabularyCodec.encode(reviewed), "encode() drifted from the pinned golden bytes")
        assertEquals(golden, VocabularyCodec.encode(VocabularyCodec.decode(golden)), "decode -> encode is not byte-identical")
    }

    /** The exact React export fixture Playwright feeds the Kotlin/Wasm app
     *  (`tests/fixtures/vocabulary-four-cards.json`) still decodes unchanged: pack-scoped card
     *  keys stay literally `pl-ru:...` (today's [polski.data.CoursePack.pairId]) even though
     *  they are no longer a hardcoded string in the codec. */
    @Test
    fun reactExportFixtureDecodesAndRoundTripsUnchanged() {
        val raw = """{"version":1,"pair":"pl-ru","selectedIds":["noun.wife","user.00000000-0000-4000-8000-000000000001"],""" +
            """"custom":[{"id":"user.00000000-0000-4000-8000-000000000001","lemma":"szkoła","translation":"школа",""" +
            """"form":"szkoła · род. szkoły","example":"To jest moja szkoła.","level":"A1","frequencyRank":null,"custom":true}],""" +
            """"cards":{"pl-ru:vocabulary:ru-pl:noun.wife":{"due":"2099-01-01T00:00:00.000Z","stability":2.3065,""" +
            """"difficulty":2.11810397,"elapsed_days":0,"scheduled_days":0,"reps":1,"lapses":0,"learning_steps":1,"state":1,""" +
            """"last_review":"2026-09-23T12:00:00.000Z"},"pl-ru:vocabulary:pl-ru:noun.wife":{"due":"2099-01-01T00:00:00.000Z",""" +
            """"stability":3.3065,"difficulty":2.21810397,"elapsed_days":0,"scheduled_days":0,"reps":1,"lapses":0,"learning_steps":1,""" +
            """"state":1,"last_review":"2026-09-23T12:00:00.000Z"},""" +
            """"pl-ru:vocabulary:ru-pl:user.00000000-0000-4000-8000-000000000001":{"due":"2099-01-01T00:00:00.000Z",""" +
            """"stability":4.3065,"difficulty":2.31810397,"elapsed_days":0,"scheduled_days":0,"reps":1,"lapses":0,"learning_steps":1,""" +
            """"state":1,"last_review":"2026-09-23T12:00:00.000Z"},""" +
            """"pl-ru:vocabulary:pl-ru:user.00000000-0000-4000-8000-000000000001":{"due":"2099-01-01T00:00:00.000Z",""" +
            """"stability":5.3065,"difficulty":2.4181039699999998,"elapsed_days":0,"scheduled_days":0,"reps":1,"lapses":0,"learning_steps":1,""" +
            """"state":1,"last_review":"2026-09-23T12:00:00.000Z"}}}"""
        val decoded = VocabularyCodec.decode(raw)
        assertEquals(setOf(
            "pl-ru:vocabulary:ru-pl:noun.wife", "pl-ru:vocabulary:pl-ru:noun.wife",
            "pl-ru:vocabulary:ru-pl:user.00000000-0000-4000-8000-000000000001",
            "pl-ru:vocabulary:pl-ru:user.00000000-0000-4000-8000-000000000001",
        ), decoded.cards.keys)
        assertEquals(Json.parseToJsonElement(raw), Json.parseToJsonElement(VocabularyCodec.encode(decoded)),
            "React export must round-trip to the same document, ignoring only formatting")
    }

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
