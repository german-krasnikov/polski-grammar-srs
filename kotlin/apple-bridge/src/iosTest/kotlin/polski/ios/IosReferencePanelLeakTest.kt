package polski.ios

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import polski.grammar.caseRows
import polski.grammar.nounPhrase
import polski.model.GramCase
import polski.presentation.AppUiState
import polski.presentation.CardPhase
import polski.training.PlExerciseEngine
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.training.sentenceSeeds

// ContrastHighlightPlan.md §5 ("панелей «под рукой»: целевая строка не подсвечивается, её «стало»
// скрыто") / web fix 804f6c6: the reference table under the card must not leak this exercise's
// own target-case form before reveal. Mirrors kotlin-reference-panel-leak.spec.ts for iOS.
class IosReferencePanelLeakTest {
    @Test
    fun referenceRowMatchingTargetCaseHidesItsFormBeforeReveal() {
        val exercise = PlExerciseEngine(RandomSource { 0.1 }, ExerciseIdFactory { "ios-ref-leak" })
            .generateChain(sentenceSeeds.first()).first { it.tags.any { tag -> GramCase.entries.any { it.id == tag } } }
        val targetCase = GramCase.entries.first { it.id in exercise.tags }
        val expectedForm = nounPhrase(exercise.nounId, targetCase, exercise.number, exercise.adjectiveId, exercise.possessive)
        val base = AppUiState(exercise = exercise, chain = listOf(exercise), phase = CardPhase.Question, showReference = true)

        val beforeReveal = Json.parseToJsonElement(snapshot(base)).jsonObject
        val targetRowBefore = beforeReveal.getValue("referenceRows").jsonArray
            .map { it.jsonObject }.first { it.getValue("title").jsonPrimitive.content == "${caseRows.first { row -> row.id == targetCase }.pl} · ${caseRows.first { row -> row.id == targetCase }.ru}" }
        assertNotEquals(expectedForm, targetRowBefore.getValue("text").jsonPrimitive.content,
            "target row's own form must stay hidden before reveal")
        assertNotEquals(expectedForm, targetRowBefore.getValue("pair").jsonObject.getValue("to").jsonPrimitive.content,
            "target row's contrast pair must not carry the answer before reveal")

        val revealed = Json.parseToJsonElement(snapshot(base.copy(phase = CardPhase.Revealed))).jsonObject
        val targetRowAfter = revealed.getValue("referenceRows").jsonArray
            .map { it.jsonObject }.first { it.getValue("title").jsonPrimitive.content == targetRowBefore.getValue("title").jsonPrimitive.content }
        assertEquals(expectedForm, targetRowAfter.getValue("text").jsonPrimitive.content,
            "target row's form becomes visible once revealed")
    }
}
