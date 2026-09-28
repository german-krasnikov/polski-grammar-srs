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

    /** EN-09 (gap F, `Plans/Kotlin/EnRuPackPlan.md` §5): [StudyDirection] is now string-backed, so a
     *  second pack's pair builds its own direction without any Kotlin change here — while pl-ru's
     *  built-in constants keep their exact `"ru-pl"/"pl-ru"` wire bytes. */
    @Test
    fun opensToASecondPacksDirectionWithoutTouchingTheBuiltIns() {
        val enToRu = StudyDirection("en-ru")
        val ruToEn = StudyDirection("ru-en")
        assertEquals("en-ru", enToRu.wire)
        assertEquals("ru-en", ruToEn.wire)
        assertEquals("pl-ru:vocabulary:en-ru:noun.wife", VocabularyCodec.cardKey("noun.wife", enToRu))
        assertEquals("pl-ru:vocabulary:ru-en:noun.wife", VocabularyCodec.cardKey("noun.wife", ruToEn))
        assertEquals(builtInStudyDirections, listOf(StudyDirection.RussianToPolish, StudyDirection.PolishToRussian))
        assertEquals("ru-pl", StudyDirection.RussianToPolish.wire)
        assertEquals("pl-ru", StudyDirection.PolishToRussian.wire)
    }

    /** EnRuAcceptance-2026-09-28.md §7 item 4: [activeStudyDirections] generalizes
     *  [builtInStudyDirections] to whichever pack is active, and a card reviewed under one of an
     *  en-ru session's own directions must survive [VocabularyCodec.encode]'s [VocabularyCodec.validate]
     *  call — before this fix, `validate` accepted only the literal `ru-pl`/`pl-ru` wires, so rating
     *  any en-ru card threw. */
    @Test
    fun activeStudyDirectionsTracksTheActivePackAndSurvivesEncode() {
        assertEquals(builtInStudyDirections, activeStudyDirections)
        try {
            polski.data.selectCoursePack("en-ru")
            assertEquals(listOf(StudyDirection("ru-en"), StudyDirection("en-ru")), activeStudyDirections)
            val direction = activeStudyDirections.first()
            val reviewed = VocabularyCodec.review(
                VocabularyCodec.select(VocabularyDocument(), "noun.wife", true),
                "noun.wife", direction, Rating.Good, scheduler, at,
            )
            assertEquals("en-ru:vocabulary:ru-en:noun.wife", VocabularyCodec.cardKey("noun.wife", direction))
            assertEquals(reviewed, VocabularyCodec.decode(VocabularyCodec.encode(reviewed)))
        } finally {
            polski.data.selectCoursePack("pl-ru")
        }
        assertEquals(builtInStudyDirections, activeStudyDirections)
    }

    /** EnRuAcceptance-2026-09-28.md §7 item 4: a vocabulary direction picker must offer any pack's
     *  own pair, not a hardcoded pl-ru 2-case list — [studyDirectionOptions] derives both
     *  directions (and their display labels) from a bare target/native code pair. pl-ru's own
     *  option wires/order stay byte-identical to [builtInStudyDirections]. */
    @Test
    fun derivesBothDirectionsAndLabelsForAnyPacksTargetAndNative() {
        val plRu = studyDirectionOptions("pl", "ru")
        assertEquals(listOf(StudyDirection.RussianToPolish, StudyDirection.PolishToRussian), plRu.map { it.direction })
        assertEquals(listOf("Русский → польский", "Польский → русский"), plRu.map { it.label })

        val enRu = studyDirectionOptions("en", "ru")
        assertEquals(listOf(StudyDirection("ru-en"), StudyDirection("en-ru")), enRu.map { it.direction })
        assertEquals(listOf("Русский → английский", "Английский → русский"), enRu.map { it.label })
    }

    /** EnRuAcceptance-2026-09-28.md §7 item 4: a card host needs exactly one boolean (never a raw
     *  `"ru-pl"` string compare, which only ever matched pl-ru) to know whether [item] the learner
     *  must recall is [VocabularyItem.lemma] (the target word) or [VocabularyItem.translation] (the
     *  native one), plus a ready-made "Вспомни по-…" caption and the revealed answer's own language
     *  name — for any pack's target/native pair, not only pl-ru's. */
    @Test
    fun recallHelpersGeneralizeBeyondPlRusHardcodedWires() {
        assertEquals(true, StudyDirection.RussianToPolish.recallsTarget("pl", "ru"))
        assertEquals(false, StudyDirection.PolishToRussian.recallsTarget("pl", "ru"))
        assertEquals("Вспомни по-польски", StudyDirection.RussianToPolish.recallCaption("pl", "ru"))
        assertEquals("Вспомни по-русски", StudyDirection.PolishToRussian.recallCaption("pl", "ru"))
        assertEquals("польский", StudyDirection.RussianToPolish.answerLanguageLabel("pl", "ru"))
        assertEquals("русский", StudyDirection.PolishToRussian.answerLanguageLabel("pl", "ru"))

        val ruToEn = StudyDirection("ru-en")
        val enToRu = StudyDirection("en-ru")
        assertEquals(true, ruToEn.recallsTarget("en", "ru"))
        assertEquals(false, enToRu.recallsTarget("en", "ru"))
        assertEquals("Вспомни по-английски", ruToEn.recallCaption("en", "ru"))
        assertEquals("Вспомни по-русски", enToRu.recallCaption("en", "ru"))
        assertEquals("английский", ruToEn.answerLanguageLabel("en", "ru"))
        assertEquals("русский", enToRu.answerLanguageLabel("en", "ru"))
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

    /** EnRuAcceptance-2026-09-28.md §7 item 2: unlike [cardKey]/[decode]/[encode] (plain `fun`s that
     *  already read [polski.data.packRegistry] fresh), [VocabularyCodec.key] was still `by lazy` —
     *  frozen to whichever pack was active the first time any test in this process touched it. A
     *  host's vocabulary repository reads this key to load/save, so a frozen key would keep reading
     *  and writing the pl-ru document forever, even after [polski.data.selectCoursePack] switches
     *  the active pack to en-ru. */
    @Test
    fun keyTracksTheActivePackAcrossASwitch() {
        assertEquals("polski-vocabulary-pl-ru-v1", VocabularyCodec.key)
        try {
            polski.data.selectCoursePack("en-ru")
            assertEquals("polski-vocabulary-en-ru-v1", VocabularyCodec.key)
        } finally {
            polski.data.selectCoursePack("pl-ru")
        }
        assertEquals("polski-vocabulary-pl-ru-v1", VocabularyCodec.key)
    }

    /** EnRuAcceptance-2026-09-28.md §7 item 4: [validate] hardcoded pl-ru's own "ru-pl"/"pl-ru"
     *  direction wires, so a review under any other pack's own directions (en-ru's "ru-en"/"en-ru")
     *  failed `require` even though [StudyDirection] itself has been open-typed since EN-09 — the
     *  UI had nowhere safe to route a non-pl-ru direction to. [StudyDirection.nativeToTarget]/
     *  [StudyDirection.targetToNative] generalize [StudyDirection.RussianToPolish]/[PolishToRussian]
     *  (themselves that exact pair for pl-ru's own native=ru/target=pl) to any pack. */
    @Test
    fun reviewsAndRoundTripsUnderANonPlRuActivePacksOwnDirections() {
        try {
            polski.data.selectCoursePack("en-ru")
            val pack = polski.data.packRegistry.active
            val forward = StudyDirection.nativeToTarget(pack.targetLanguage, pack.nativeLanguage)
            val backward = StudyDirection.targetToNative(pack.targetLanguage, pack.nativeLanguage)
            assertEquals("ru-en", forward.wire)
            assertEquals("en-ru", backward.wire)
            val selected = VocabularyCodec.select(VocabularyDocument(), "noun.wife", true)
            val reviewed = VocabularyCodec.review(selected, "noun.wife", forward, Rating.Good, scheduler, at)
            assertEquals(1, reviewed.cards.getValue(VocabularyCodec.cardKey("noun.wife", forward)).reps)
            assertEquals(reviewed, VocabularyCodec.decode(VocabularyCodec.encode(reviewed)))
            val alsoBackward = VocabularyCodec.review(reviewed, "noun.wife", backward, Rating.Good, scheduler, at)
            assertEquals(alsoBackward, VocabularyCodec.decode(VocabularyCodec.encode(alsoBackward)))
        } finally {
            polski.data.selectCoursePack("pl-ru")
        }
    }
}
