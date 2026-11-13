package polski.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * SB-1 blueprint §2 AC1.1/AC1.2: [TileExercise]/[TileSegment]/[ScaffoldingLevel]/[TileAnswer]/
 * [TileError]/[TileSelection] compile and behave as plain `commonMain` data — no platform import,
 * no language literal. [CoreExercise]'s two new fields ([CoreExercise.tileTargetSlots]/
 * [CoreExercise.tilePhrase]) default so every pre-existing call site is unaffected.
 */
class TileExerciseTest {
    @Test fun coreExerciseDefaultsTileFieldsToEmptyAndNullForUnmodifiedCallers() {
        val exercise = CoreExercise(
            id = "x", primarySkill = "case.acc.f", source = "s", prompt = "p", expected = "e",
            explanation = "exp", tags = listOf("acc"), changes = emptyList(),
        )
        assertEquals(emptyList(), exercise.tileTargetSlots)
        assertNull(exercise.tilePhrase)
    }

    @Test fun coreExerciseWithExplicitTileFieldsEqualsAnotherWithTheSameFields() {
        val phrase = TilePhrase("core.argument.case-role", mapOf("Case" to "acc"), "piękną żonę", mapOf("adjective" to (0 until 7)))
        val a = CoreExercise(
            id = "x", primarySkill = "case.acc.f", source = "s", prompt = "p", expected = "e",
            explanation = "exp", tags = emptyList(), changes = emptyList(),
            tileTargetSlots = listOf("adjective", "noun"), tilePhrase = phrase,
        )
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(listOf("adjective", "noun"), a.tileTargetSlots)
        assertEquals(phrase, a.tilePhrase)
    }

    @Test fun tileExerciseHoldsSegmentsInOrderAndScaffoldingLevel() {
        val segments = listOf(
            TileSegment.Locked(position = 0, text = "Widzę"),
            TileSegment.Choice(
                position = 1, correctForm = "piękną", lexeme = "beautiful",
                targetFeature = FeatureKey("Case"), alternatives = listOf("piękna", "pięknej"),
            ),
        )
        val exercise = TileExercise(segments, ScaffoldingLevel.Partial)
        assertEquals(2, exercise.segments.size)
        assertEquals(ScaffoldingLevel.Partial, exercise.scaffolding)
        val choice = exercise.segments[1] as TileSegment.Choice
        assertEquals("beautiful", choice.lexeme)
        assertTrue("piękna" in choice.alternatives)
    }

    @Test fun tileAnswerCarriesSelectionsCorrectnessAndErrorsWithNullReasonByDefault() {
        val error = TileError(position = 1, selected = "piękna", expected = "piękną", feature = FeatureKey("Case"))
        assertNull(error.reason)
        val answer = TileAnswer(selections = listOf(TileSelection(1, "piękna")), correct = false, errors = listOf(error))
        assertEquals(false, answer.correct)
        assertEquals(1, answer.errors.size)
    }
}
