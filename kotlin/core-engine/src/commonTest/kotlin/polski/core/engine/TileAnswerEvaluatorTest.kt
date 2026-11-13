package polski.core.engine

import polski.core.model.FeatureKey
import polski.core.model.ScaffoldingLevel
import polski.core.model.TileExercise
import polski.core.model.TileSegment
import polski.core.model.TileSelection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** SB-3 blueprint §4 AC3.1-3.4: [evaluateTileAnswer] exact-match grading over an already-resolved [TileExercise]. */
class TileAnswerEvaluatorTest {
    private val caseFeature = FeatureKey("Case")
    private val exercise = TileExercise(
        segments = listOf(
            TileSegment.Locked(0, "Widzę"),
            TileSegment.Choice(1, correctForm = "piękną", lexeme = "beautiful", targetFeature = caseFeature, alternatives = listOf("piękna", "pięknej")),
            TileSegment.Choice(2, correctForm = "żonę", lexeme = "wife", targetFeature = caseFeature, alternatives = listOf("żona", "żony")),
        ),
        scaffolding = ScaffoldingLevel.None,
    )

    @Test fun everyChoiceFilledCorrectlyIsCorrectWithNoErrors() {
        val answer = evaluateTileAnswer(exercise, listOf(TileSelection(1, "piękną"), TileSelection(2, "żonę")))
        assertTrue(answer.correct)
        assertEquals(emptyList(), answer.errors)
    }

    @Test fun oneWrongButListedAlternativeProducesExactlyOneErrorAtThatPosition() {
        val answer = evaluateTileAnswer(exercise, listOf(TileSelection(1, "piękna"), TileSelection(2, "żonę")))
        assertEquals(false, answer.correct)
        assertEquals(1, answer.errors.size)
        val error = answer.errors.single()
        assertEquals(1, error.position)
        assertEquals("piękna", error.selected)
        assertEquals("piękną", error.expected)
        assertEquals(caseFeature, error.feature)
        assertNull(error.reason)
    }

    @Test fun missingChoiceSelectionIsIncorrectWithNoSpuriousError() {
        val answer = evaluateTileAnswer(exercise, listOf(TileSelection(1, "piękną")))
        assertEquals(false, answer.correct)
        assertEquals(emptyList(), answer.errors)
        assertTrue(answer.selections.size < exercise.segments.count { it is TileSegment.Choice })
    }

    @Test fun lockedSegmentsNeverProduceErrorsRegardlessOfSelections() {
        val answer = evaluateTileAnswer(exercise, listOf(TileSelection(0, "anything"), TileSelection(1, "piękną"), TileSelection(2, "żonę")))
        assertTrue(answer.correct)
        assertEquals(emptyList(), answer.errors)
    }
}
