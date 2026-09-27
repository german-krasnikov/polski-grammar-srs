package polski.ios

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import polski.data.presentationBySkillId
import polski.presentation.AppUiState
import polski.presentation.AnswerMode
import polski.presentation.CardPhase
import polski.presentation.StyleId
import polski.presentation.StyleRegistry
import polski.training.ExerciseFactory
import polski.training.ExerciseIdFactory
import polski.training.RandomSource
import polski.training.sentenceSeeds

class IosMethodCycleSnapshotTest {
    @Test
    fun introQuestionAndFeedbackExposeOnlyTheirOwnMethodCopy() {
        val exercise = ExerciseFactory(RandomSource { 0.1 }, ExerciseIdFactory { "ios-cycle" })
            .generateChain(sentenceSeeds.first()).first()
        val presentation = presentationBySkillId(exercise.primarySkill)
        val base = AppUiState(exercise = exercise, chain = listOf(exercise), phase = CardPhase.Question,
            styleId = StyleId.RuleFirst, introPending = true)

        val intro = Json.parseToJsonElement(snapshot(base)).jsonObject
        assertTrue(intro.getValue("introPending").jsonPrimitive.content.toBoolean())
        assertEquals(presentation.logic.introduce,
            intro.getValue("exercise").jsonObject.getValue("methodIntroduce").jsonPrimitive.content)
        assertEquals(exercise.source, intro.getValue("exercise").jsonObject.getValue("source").jsonPrimitive.content)
        assertFalse(intro.getValue("exercise").jsonObject.containsKey("expected"))

        val question = Json.parseToJsonElement(snapshot(base.copy(introPending = false, draft = "próba"))).jsonObject
        assertEquals("próba", question.getValue("draft").jsonPrimitive.content)
        assertEquals(presentation.logic.retrieve,
            question.getValue("exercise").jsonObject.getValue("methodRetrieve").jsonPrimitive.content)
        assertFalse(question.getValue("exercise").jsonObject.containsKey("expected"))

        val revealed = Json.parseToJsonElement(snapshot(base.copy(introPending = false,
            phase = CardPhase.Revealed, styleId = StyleId.SituationFirst))).jsonObject
        val card = revealed.getValue("exercise").jsonObject
        assertEquals(presentation.situations.feedback, card.getValue("methodFeedback").jsonPrimitive.content)
        assertEquals(presentation.situations.review, card.getValue("methodReview").jsonPrimitive.content)
        assertEquals(exercise.expected, card.getValue("expected").jsonPrimitive.content)

        for (styleId in StyleRegistry.recipes.keys) {
            val frozen = Json.parseToJsonElement(snapshot(base.copy(introPending = false,
                phase = CardPhase.Revealed, answerMode = AnswerMode.Typed,
                draft = "Moja proba", frozenAnswer = "Moja proba", styleId = styleId))).jsonObject
            assertEquals("Moja proba", frozen.getValue("exercise").jsonObject.getValue("frozenAnswer").jsonPrimitive.content)
        }
    }
}
