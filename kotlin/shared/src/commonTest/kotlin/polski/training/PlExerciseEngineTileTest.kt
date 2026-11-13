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
 * **Deviation from the blueprint's own example skill, flagged for the main agent/content owner:**
 * every *constructed* pl-ru recipe (`case.acc.f` among them) is one of `TrainingParityTest`'s 112
 * pinned `assertEquals(Exercise(<13 literal args>), actual)` cases — a full-object equality check
 * against a literal that predates `Exercise.tileTargetSlots`/`tilePhrase` and so always defaults
 * them to empty/null. Opting any constructed skill into `tileTargetSlots` in
 * `courses-biome/pl-ru/recipes.core-data` therefore breaks that skill's pinned parity case (verified:
 * setting it on `case.acc.f` failed `E-case.acc.f-preferred`), and every static/pooled recipe is
 * ineligible for tiles by design (§6). No pl-ru production recipe is opted in by this blueprint's
 * implementation for that reason — `AC5.2`'s mechanism is proven here against the real pack's
 * [ConstructionRealizer]/[polski.core.engine.TableMorphology] via a hand-built [TilePhrase] (the
 * exact `core.argument.case-role`/`case.acc.f` realization `ExerciseGenerator` would itself
 * produce if `tileTargetSlots` were set), not by mutating production content. Wiring an actual
 * opt-in recipe is deferred to whoever owns updating/regenerating `TrainingParityTest`'s oracle.
 */
class PlExerciseEngineTileTest {
    private class Draws(private val values: List<Double>) : RandomSource {
        private var index = 0
        override fun nextDouble(): Double = values[(index++).coerceAtMost(values.lastIndex)]
    }
    private fun factory(draws: List<Double>) = PlExerciseEngine(Draws(draws), ExerciseIdFactory { "generated" })

    @Test fun noPlRuSkillOptsIntoTilesYet() {
        val exercise = factory(listOf(0.1)).generateForSkill("case.acc.f", SentenceSeed("wife", "beautiful"))
        assertEquals(emptyList(), exercise.tileTargetSlots)
        assertNull(exercise.tilePhrase)
    }

    @Test fun skillWithoutTileTargetSlotsYieldsNoTileBuilderState() {
        val exercise = factory(listOf(0.1)).generateForSkill("verb.past", SentenceSeed("wife", "beautiful"))
        assertEquals(emptyList(), exercise.tileTargetSlots)
        assertNull(tileBuilderStateFor(exercise, Draws(listOf(0.1, 0.5, 0.9))))
    }

    @Test fun realPackEngineBuildsATileBuilderStateWhenAnExerciseOptsIn() {
        val exercise = factory(listOf(0.1)).generateForSkill("case.acc.f", SentenceSeed("wife", "beautiful"))
        // The exact TilePhrase ExerciseGenerator would itself compute for case.acc.f/"wife"+"beautiful" if tileTargetSlots were set (see class KDoc).
        val optedIn = exercise.copy(
            tileTargetSlots = listOf("adjective", "noun"),
            tilePhrase = TilePhrase(
                construction = "core.argument.case-role", bundle = mapOf("Number" to "sg", "Case" to "acc"),
                text = "moją piękną żonę", slotSpans = mapOf("owner" to (0..3), "adjective" to (5..10), "noun" to (12..15)),
            ),
        )
        val state = tileBuilderStateFor(optedIn, Draws(listOf(0.1, 0.5, 0.9)))
        assertNotNull(state)
        val reconstructed = state.exercise.segments.sortedBy { it.position }.joinToString("") {
            when (it) {
                is TileSegment.Locked -> it.text
                is TileSegment.Choice -> it.correctForm
            }
        }
        assertEquals(optedIn.expected, reconstructed)
        assertTrue(state.exercise.segments.any { it is TileSegment.Choice })
        assertEquals(emptyList(), state.placed)
        assertNull(state.result)
    }
}
