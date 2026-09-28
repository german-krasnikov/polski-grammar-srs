package polski.ios

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import polski.data.referenceChainRows
import polski.data.referenceSystemCards
import polski.data.referencePipeline
import polski.data.referenceRussianSupport
import polski.data.referenceCaseTeaching
import polski.data.referenceVerbTeaching
import polski.grammar.verbForm
import polski.model.Tense
import polski.data.comparisonNounIds
import polski.data.nounById
import polski.model.GramCase
import polski.model.NumberGram
import polski.data.referenceTenseRows
import polski.data.referenceAspectRows
import polski.data.maleAccRows
import polski.data.enPersonalPronouns
import polski.data.enVerbForm
import polski.presentation.AppUiState

class IosMatrixSnapshotTest {
    @Test
    fun everyGeneratedMatrixValueHasALosslessPairAcrossBothNumbers() {
        for (number in listOf("sg", "pl")) {
            val state = AppUiState(matrixSelection = polski.presentation.MatrixSelection(numberId = number))
            val matrix = Json.parseToJsonElement(snapshot(state)).jsonObject.getValue("matrix").jsonObject
            fun checkPair(pair: kotlinx.serialization.json.JsonObject, expected: String) {
                assertEquals(expected, pair.getValue("to").jsonPrimitive.content)
                assertEquals(pair.getValue("from").jsonPrimitive.content,
                    pair.getValue("beforeParts").jsonArray.joinToString("") { it.jsonObject.getValue("text").jsonPrimitive.content })
                assertEquals(expected,
                    pair.getValue("afterParts").jsonArray.joinToString("") { it.jsonObject.getValue("text").jsonPrimitive.content })
            }
            matrix.getValue("cases").jsonArray.forEach { element ->
                val row = element.jsonObject
                listOf("phrase", "sentence").forEach { key ->
                    checkPair(row.getValue("${key}Pair").jsonObject, row.getValue(key).jsonPrimitive.content)
                }
            }
            matrix.getValue("comparison").jsonArray.forEach { element ->
                val row = element.jsonObject
                GramCase.entries.forEach { gramCase ->
                    checkPair(row.getValue("${gramCase.name}pair").jsonObject,
                        row.getValue(gramCase.name).jsonPrimitive.content)
                }
            }
            matrix.getValue("verbsRows").jsonArray.forEach { element ->
                val row = element.jsonObject
                Tense.entries.forEach { tense ->
                    checkPair(row.getValue("${tense.id}Pair").jsonObject, row.getValue(tense.id).jsonPrimitive.content)
                }
            }
            for (group in listOf("pronouns", "possessives")) {
                matrix.getValue(group).jsonArray.forEach { element ->
                    val row = element.jsonObject
                    val contexts = if (group == "pronouns") matrix.getValue("pronounContexts") else matrix.getValue("possessiveCases")
                    contexts.jsonArray.forEach { context ->
                        val id = context.jsonObject.getValue("id").jsonPrimitive.content
                        checkPair(row.getValue("${id}pair").jsonObject, row.getValue(id).jsonPrimitive.content)
                    }
                }
            }
        }
    }

    @Test
    fun generatedMatrixCellsCarryPolishBeforeAndAfter() {
        val matrix = Json.parseToJsonElement(snapshot(AppUiState())).jsonObject.getValue("matrix").jsonObject
        val case = matrix.getValue("cases").jsonArray[1].jsonObject
        val phrase = case.getValue("phrasePair").jsonObject
        assertEquals(matrix.getValue("cases").jsonArray[0].jsonObject.getValue("phrase").jsonPrimitive.content,
            phrase.getValue("from").jsonPrimitive.content)
        assertEquals(case.getValue("phrase").jsonPrimitive.content, phrase.getValue("to").jsonPrimitive.content)
        assertEquals(phrase.getValue("from").jsonPrimitive.content,
            phrase.getValue("beforeParts").jsonArray.joinToString("") { it.jsonObject.getValue("text").jsonPrimitive.content })
        assertEquals(phrase.getValue("to").jsonPrimitive.content,
            phrase.getValue("afterParts").jsonArray.joinToString("") { it.jsonObject.getValue("text").jsonPrimitive.content })

        val verb = matrix.getValue("verbsRows").jsonArray.first().jsonObject.getValue("presentPair").jsonObject
        assertEquals("robić", verb.getValue("from").jsonPrimitive.content)
        val pronoun = matrix.getValue("pronouns").jsonArray[3].jsonObject.getValue("accpair").jsonObject
        assertEquals("ona", pronoun.getValue("from").jsonPrimitive.content)
        val possessive = matrix.getValue("possessives").jsonArray.first().jsonObject.getValue("accpair").jsonObject
        assertEquals(matrix.getValue("possessives").jsonArray.first().jsonObject.getValue("nom").jsonPrimitive.content,
            possessive.getValue("from").jsonPrimitive.content)
    }

    @Test
    fun nativeMatrixReceivesNineCompactVerbRowsAndTeachingCopy() {
        for (feminine in listOf(false, true)) {
            val state = AppUiState(matrixSelection = polski.presentation.MatrixSelection(feminineGroup = feminine))
            val matrix = Json.parseToJsonElement(snapshot(state)).jsonObject.getValue("matrix").jsonObject
            val teaching = referenceVerbTeaching
            assertEquals(teaching.genderControlLabel.compact, matrix.getValue("verbGenderControlLabel").jsonPrimitive.content)
            assertEquals(teaching.compactFutureExplanation, matrix.getValue("verbFutureExplanation").jsonPrimitive.content)
            assertEquals(teaching.genderOptions.map { it.label.compact },
                matrix.getValue("verbGenderOptions").jsonArray.map { it.jsonObject.getValue("title").jsonPrimitive.content })
            Tense.entries.forEach { tense ->
                assertEquals(teaching.tenseLabels.getValue(tense).compact,
                    matrix.getValue("verbTenseLabels").jsonObject.getValue(tense.id).jsonPrimitive.content)
            }
            val rows = matrix.getValue("verbsRows").jsonArray
            assertEquals(9, rows.size)
            rows.forEachIndexed { index, element ->
                val subject = teaching.subjects[index]
                val row = element.jsonObject
                assertEquals(subject.label.compact, row.getValue("title").jsonPrimitive.content)
                Tense.entries.forEach { tense ->
                    assertEquals(verbForm("do", tense, subject.person, subject.number, subject.gender(feminine)),
                        row.getValue(tense.id).jsonPrimitive.content)
                }
            }
        }
    }

    @Test
    fun nativeMatrixReceivesTheCompactCaseNoteAndSevenOrderedComputedNouns() {
        val matrix = Json.parseToJsonElement(snapshot(AppUiState())).jsonObject.getValue("matrix").jsonObject
        assertEquals(referenceCaseTeaching.compactNote, matrix.getValue("caseNote").jsonPrimitive.content)
        val rows = matrix.getValue("comparison").jsonArray
        assertEquals(comparisonNounIds.size, rows.size)
        rows.forEachIndexed { index, element ->
            val row = element.jsonObject
            val noun = nounById(comparisonNounIds[index])
            assertEquals(noun.lemma, row.getValue("title").jsonPrimitive.content)
            GramCase.entries.forEach { gramCase ->
                assertEquals(noun.forms.getValue(NumberGram.SG).getValue(gramCase),
                    row.getValue(gramCase.name).jsonPrimitive.content)
            }
        }
    }

    @Test
    fun nativeMatrixReceivesFourCompactRussianSupportLines() {
        val matrix = Json.parseToJsonElement(snapshot(AppUiState())).jsonObject.getValue("matrix").jsonObject
        assertEquals(referenceRussianSupport.compactTitle, matrix.getValue("supportTitle").jsonPrimitive.content)
        assertEquals(referenceRussianSupport.rows.map { it.mobileLine },
            matrix.getValue("supportLines").jsonArray.map { it.jsonPrimitive.content })
    }

    @Test
    fun nativeMatrixReceivesTheExistingCompactPipelineCopy() {
        val matrix = Json.parseToJsonElement(snapshot(AppUiState())).jsonObject.getValue("matrix").jsonObject
        assertEquals(referencePipeline.title, matrix.getValue("pipelineTitle").jsonPrimitive.content)
        assertEquals(referencePipeline.compactSummary, matrix.getValue("pipelineSummary").jsonPrimitive.content)
        assertEquals(referencePipeline.compactExample, matrix.getValue("pipelineExample").jsonPrimitive.content)
    }

    @Test
    fun nativeMatrixReceivesOrderedSystemCards() {
        val root = Json.parseToJsonElement(snapshot(AppUiState())).jsonObject
        val cards = root.getValue("matrix").jsonObject.getValue("systemCards").jsonArray
        assertEquals(referenceSystemCards.size, cards.size)
        cards.forEachIndexed { index, element ->
            val card = element.jsonObject
            assertEquals(referenceSystemCards[index].id, card.getValue("id").jsonPrimitive.content)
            assertEquals(referenceSystemCards[index].title, card.getValue("title").jsonPrimitive.content)
            assertEquals(referenceSystemCards[index].explanation, card.getValue("explanation").jsonPrimitive.content)
            assertEquals(referenceSystemCards[index].example, card.getValue("example").jsonPrimitive.content)
            val steps = card.getValue("steps").jsonArray
            assertEquals(referenceSystemCards[index].steps.size - 1, steps.size)
            steps.forEachIndexed { stepIndex, stepElement ->
                val pair = stepElement.jsonObject
                val from = referenceSystemCards[index].steps[stepIndex]
                val to = referenceSystemCards[index].steps[stepIndex + 1]
                assertEquals(from, pair.getValue("from").jsonPrimitive.content)
                assertEquals(to, pair.getValue("to").jsonPrimitive.content)
                assertEquals(from, pair.getValue("beforeParts").jsonArray.joinToString("") { it.jsonObject.getValue("text").jsonPrimitive.content })
                assertEquals(to, pair.getValue("afterParts").jsonArray.joinToString("") { it.jsonObject.getValue("text").jsonPrimitive.content })
            }
        }
    }

    // EmphasisUXAudit correction on ef66020: lossless reconstruction alone cannot catch a wrong
    // morpheme highlighted (E5/S3's bug pattern reintroduced via the steps->ContrastPair wiring).
    // This inspects which spans are actually marked `changed` for the "modifiers" card, whose
    // steps are a particle insertion ("Widzę…"→"Nie widzę…") followed by a particle replacement
    // plus trailing punctuation only ("Nie widzę…"→"Czy widzę…?").
    @Test
    fun nativeMatrixHighlightsOnlyTheInsertedOrReplacedParticleOnTheModifiersCard() {
        val root = Json.parseToJsonElement(snapshot(AppUiState())).jsonObject
        val cards = root.getValue("matrix").jsonObject.getValue("systemCards").jsonArray
        val modifiers = cards.first { it.jsonObject.getValue("id").jsonPrimitive.content == "modifiers" }.jsonObject
        val steps = modifiers.getValue("steps").jsonArray
        fun changedText(parts: kotlinx.serialization.json.JsonArray) = parts
            .filter { it.jsonObject.getValue("changed").jsonPrimitive.content == "true" }
            .joinToString("") { it.jsonObject.getValue("text").jsonPrimitive.content }

        val insertion = steps[0].jsonObject
        assertEquals("", changedText(insertion.getValue("beforeParts").jsonArray), "insertion must not leak a before-side span")
        assertEquals("Nie", changedText(insertion.getValue("afterParts").jsonArray))

        val replacement = steps[1].jsonObject
        assertEquals("Nie", changedText(replacement.getValue("beforeParts").jsonArray))
        assertEquals("Czy", changedText(replacement.getValue("afterParts").jsonArray), "trailing punctuation must not be marked changed")
    }

    @Test
    fun nativeMatrixReceivesBothSidesOfEveryAuthoredTransition() {
        val root = Json.parseToJsonElement(snapshot(AppUiState())).jsonObject
        val rows = root.getValue("matrix").jsonObject.getValue("chainRows").jsonArray
        assertEquals(referenceChainRows.size, rows.size)
        rows.forEachIndexed { index, element ->
            val row = element.jsonObject
            assertEquals(referenceChainRows[index].from, row.getValue("from").jsonPrimitive.content)
            assertEquals(referenceChainRows[index].to, row.getValue("to").jsonPrimitive.content)
            assertTrue(row.getValue("beforeParts").jsonArray.any { it.jsonObject.getValue("changed").jsonPrimitive.content == "true" })
            assertTrue(row.getValue("afterParts").jsonArray.any { it.jsonObject.getValue("changed").jsonPrimitive.content == "true" })
        }
    }

    @Test
    fun nativeMatrixReceivesAuthoredTenseComparisons() {
        val root = Json.parseToJsonElement(snapshot(AppUiState())).jsonObject
        val rows = root.getValue("matrix").jsonObject.getValue("tenseRows").jsonArray
        assertEquals(referenceTenseRows.size, rows.size)
        rows.forEachIndexed { index, element ->
            val row = element.jsonObject
            assertEquals(referenceTenseRows[index].from, row.getValue("from").jsonPrimitive.content)
            assertEquals(referenceTenseRows[index].to, row.getValue("to").jsonPrimitive.content)
            // Rows 2 ("będzie" inserted), 3 ("nie" inserted) and 4 ("Czy" inserted, plus an
            // unrelated trailing-punctuation-only change on the last word) are pure single-word
            // insertions: the Emphasis contract requires no before-side span for those, only
            // after (C2 review blocker: punctuation must not defeat the single-word alignment).
            val isPureInsertion = index == 2 || index == 3 || index == 4
            if (index > 0) {
                assertEquals(!isPureInsertion,
                    row.getValue("beforeParts").jsonArray.any { it.jsonObject.getValue("changed").jsonPrimitive.content == "true" })
                assertTrue(row.getValue("afterParts").jsonArray.any { it.jsonObject.getValue("changed").jsonPrimitive.content == "true" })
            }
        }
    }

    @Test
    fun nativeMatrixReceivesAuthoredAspectForms() {
        val root = Json.parseToJsonElement(snapshot(AppUiState())).jsonObject
        val rows = root.getValue("matrix").jsonObject.getValue("aspectRows").jsonArray
        assertEquals(referenceAspectRows.size, rows.size)
        rows.forEachIndexed { index, element ->
            val aspect = referenceAspectRows[index]
            val row = element.jsonObject
            assertEquals(aspect.from, row.getValue("from").jsonPrimitive.content)
            val entries = row.getValue("entries").jsonArray
            assertEquals(3, entries.size)
            assertEquals(aspect.present != null, entries[0].jsonObject.getValue("available").jsonPrimitive.content == "true")
            assertEquals(aspect.past, entries[1].jsonObject.getValue("to").jsonPrimitive.content)
            assertEquals(aspect.future, entries[2].jsonObject.getValue("to").jsonPrimitive.content)
            entries.filter { it.jsonObject.getValue("available").jsonPrimitive.content == "true" }.forEach { entry ->
                val before = entry.jsonObject.getValue("from").jsonPrimitive.content
                val after = entry.jsonObject.getValue("to").jsonPrimitive.content
                // "będę " compound future ("robić"→"będę robić") is a pure insertion: the
                // Emphasis contract requires no before-side span for those, only after.
                val isPureInsertion = after == "będę $before"
                assertEquals(!isPureInsertion, entry.jsonObject.getValue("beforeParts").jsonArray.any {
                    it.jsonObject.getValue("changed").jsonPrimitive.content == "true"
                }, "Unexpected old-highlight state for $before → $after")
                assertTrue(entry.jsonObject.getValue("afterParts").jsonArray.any {
                    it.jsonObject.getValue("changed").jsonPrimitive.content == "true"
                })
            }
        }
    }

    // EN-24 (UC-09 part 2/2, ios lane, EnRuPackPlan.md §5 gap H / §6): the iOS matrix host's own
    // live English table, mirroring the web host's `kotlin-en-matrix.spec.ts` — real, irregular
    // `forms.generated.json(en)` values (not a mock), read through the same [enVerbForm] the web
    // host now also calls (promoted to `:shared` commonMain by this task, not duplicated).
    @Test
    fun nativeMatrixReceivesTheEnglishExampleVerbAndDoSupportRows() {
        val matrix = Json.parseToJsonElement(snapshot(AppUiState())).jsonObject.getValue("matrix").jsonObject
        assertEquals("see", matrix.getValue("englishExampleLemma").jsonPrimitive.content)

        val verbRows = matrix.getValue("englishVerbsRows").jsonArray
        assertEquals(enPersonalPronouns.size, verbRows.size)
        verbRows.forEachIndexed { index, element ->
            val row = element.jsonObject
            assertEquals(enPersonalPronouns[index].subject, row.getValue("title").jsonPrimitive.content)
            listOf("present", "past", "future").forEach { tenseId ->
                val pair = row.getValue("${tenseId}Pair").jsonObject
                assertEquals("see", pair.getValue("from").jsonPrimitive.content)
            }
        }
        fun rowFor(subject: String) = verbRows.first { it.jsonObject.getValue("title").jsonPrimitive.content == subject }.jsonObject
        assertEquals("see", rowFor("I").getValue("presentPair").jsonObject.getValue("to").jsonPrimitive.content)
        assertEquals("saw", rowFor("I").getValue("pastPair").jsonObject.getValue("to").jsonPrimitive.content)
        assertEquals("will see", rowFor("I").getValue("futurePair").jsonObject.getValue("to").jsonPrimitive.content)
        assertEquals("sees", rowFor("he").getValue("presentPair").jsonObject.getValue("to").jsonPrimitive.content)
        assertEquals("saw", rowFor("he").getValue("pastPair").jsonObject.getValue("to").jsonPrimitive.content)
        assertEquals("see", rowFor("we").getValue("presentPair").jsonObject.getValue("to").jsonPrimitive.content)

        val doRows = matrix.getValue("englishDoSupportRows").jsonArray
        assertEquals(enPersonalPronouns.size, doRows.size)
        fun doRowFor(subject: String) = doRows.first { it.jsonObject.getValue("title").jsonPrimitive.content == subject }.jsonObject
        assertEquals("do", doRowFor("I").getValue("presentPair").jsonObject.getValue("to").jsonPrimitive.content)
        assertEquals("does", doRowFor("he").getValue("presentPair").jsonObject.getValue("to").jsonPrimitive.content)
        assertEquals("did", doRowFor("I").getValue("pastPair").jsonObject.getValue("to").jsonPrimitive.content)
        assertEquals("did", doRowFor("he").getValue("pastPair").jsonObject.getValue("to").jsonPrimitive.content)
        // No future do-support (plan §5 gap H): the do-support rows carry no `futurePair` at all.
        doRows.forEach { assertTrue(!it.jsonObject.containsKey("futurePair")) }

        // Cross-check against the exact function both hosts share, not a re-derived expectation.
        assertEquals(enVerbForm("see", Tense.PAST, "he"), rowFor("he").getValue("pastPair").jsonObject.getValue("to").jsonPrimitive.content)
    }

    @Test
    fun nativeMatrixReceivesAuthoredMaleAccusativeExamples() {
        val root = Json.parseToJsonElement(snapshot(AppUiState())).jsonObject
        val rows = root.getValue("matrix").jsonObject.getValue("maleAccRows").jsonArray
        assertEquals(maleAccRows.size, rows.size)
        rows.forEachIndexed { index, element ->
            val examples = element.jsonObject.getValue("examples").jsonArray
            assertEquals(maleAccRows[index].examples.size, examples.size)
            examples.forEachIndexed { exampleIndex, source ->
                val example = source.jsonObject
                assertEquals(maleAccRows[index].examples[exampleIndex].from, example.getValue("from").jsonPrimitive.content)
                assertEquals(maleAccRows[index].examples[exampleIndex].to, example.getValue("to").jsonPrimitive.content)
            }
        }
    }
}
