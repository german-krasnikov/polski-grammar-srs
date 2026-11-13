package polski.training

import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.core.model.TilePhrase
import polski.core.model.TileSegment
import polski.model.SentenceSeed
import polski.presentation.tileBuilderStateFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * SB-4/SB-5 blueprint §5/§6 AC4.2/AC4.3 end to end against the real pl-ru pack's [PlExerciseEngine]
 * / [tileBuilderStateFor].
 *
 * **Pilot opt-in (this task):** `case.acc.f`, `case.acc.m` and `case.gen.neg` now set
 * `tileTargetSlots: [adjective noun]` in `courses-biome/pl-ru/recipes.core-data` — the blueprint's
 * KDoc previously here explained why no skill was opted in yet (it would break
 * `TrainingParityTest`'s pinned `Exercise` literals, which predate `tileTargetSlots`/`tilePhrase`);
 * that oracle has since been regenerated (`tests/fixtures/nj03-exercise-oracle/requirements.core-data`
 * + `scripts/nj03/generate-exercise-oracle-test.py` now carry the two fields for those three
 * skills' `E-*` cases) so the pinned literals agree with the real generator output asserted below.
 */
class PlExerciseEngineTileTest {
    private class Draws(private val values: List<Double>) : RandomSource {
        private var index = 0
        override fun nextDouble(): Double = values[(index++).coerceAtMost(values.lastIndex)]
    }
    private fun factory(draws: List<Double>) = PlExerciseEngine(Draws(draws), ExerciseIdFactory { "generated" })

    @Test fun caseAccFOptsIntoTilesViaTheRealRecipe() {
        val exercise = factory(listOf(0.1)).generateForSkill("case.acc.f", SentenceSeed("wife", "beautiful"))
        assertEquals(listOf("adjective", "noun"), exercise.tileTargetSlots)
        assertEquals(
            TilePhrase(
                construction = "core.argument.case-role", bundle = mapOf("Number" to "sg", "Case" to "acc"),
                text = "moją piękną żonę", slotSpans = mapOf("owner" to (0..3), "adjective" to (5..10), "noun" to (12..15)),
            ),
            exercise.tilePhrase,
        )
    }

    @Test fun skillWithoutTileTargetSlotsYieldsNoTileBuilderState() {
        val exercise = factory(listOf(0.1)).generateForSkill("verb.past", SentenceSeed("wife", "beautiful"))
        assertEquals(emptyList(), exercise.tileTargetSlots)
        assertNull(tileBuilderStateFor(exercise, Draws(listOf(0.1, 0.5, 0.9))))
    }

    @Test fun realPackEngineBuildsATileBuilderStateWhenAnExerciseOptsIn() {
        val exercise = factory(listOf(0.1)).generateForSkill("case.acc.f", SentenceSeed("wife", "beautiful"))
        val state = tileBuilderStateFor(exercise, Draws(listOf(0.1, 0.5, 0.9)))
        assertNotNull(state)
        val reconstructed = state.exercise.segments.sortedBy { it.position }.joinToString("") {
            when (it) {
                is TileSegment.Locked -> it.text
                is TileSegment.Choice -> it.correctForm
            }
        }
        assertEquals(exercise.expected, reconstructed)
        assertTrue(state.exercise.segments.any { it is TileSegment.Choice })
        assertEquals(emptyList(), state.placed)
        assertNull(state.result)
    }
}
