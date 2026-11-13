package polski.core

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import polski.core.engine.ConstructionRealizer
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.core.model.ScaffoldingLevel
import polski.core.model.TilePhrase
import polski.core.model.TileSegment
import polski.data.generatedEnRuConstructionTemplates
import polski.data.selectActiveCoursePack

/**
 * SB-2 blueprint §3c AC2.6 (reviewer-flagged gap): nothing exercised [PackEngine.tileGenerator]/
 * [plTileGenerator] for the en-ru pack — every other `TileGenerator` test ran against pl-ru. This
 * mirrors [EnPackEngineRealFormsTest]'s own pattern: select en-ru as the real, process-wide active
 * pack and call the exact production entrypoint ([plTileGenerator]) every host would call, so the
 * real en-ru form table/construction templates are exercised end to end, nothing faked.
 *
 * No en-ru recipe opts into `tileTargetSlots` yet (same reason as `PlExerciseEngineTileTest`'s own
 * KDoc: every constructed en-ru skill is one of `TrainingParityEnTest`'s pinned parity cases), so
 * the opt-in is applied to a real generated [polski.core.model.CoreExercise] via `.copy(...)`,
 * with a [TilePhrase] computed from the *same* [ConstructionRealizer]/real form table
 * [plTileGenerator] itself would use — `en:possessive.my`'s own `core.agreement.possessive`
 * phrase (no `prep` slot, so no constant-slot separator subtlety to fake).
 */
class EnPackEngineTileGeneratorTest {
    @AfterTest
    fun restorePlRu() {
        selectActiveCoursePack("pl-ru")
    }

    @Test
    fun plTileGeneratorProducesRealTilesForAnEnRuExerciseThatOptsIn() {
        selectActiveCoursePack("en-ru")
        val generator = plExerciseGenerator(RandomSource { 0.4 }, ExerciseIdFactory { "en-tile-test" })
        val exercise = generator.generateForSkill("en:possessive.my")

        // Exactly what ExerciseGenerator.tilePhraseFor would compute, via the pack's own real realizer/morphology (no hand-authored fixture text).
        val realizer = ConstructionRealizer(generatedEnRuConstructionTemplates, plMorphology, plLexemeFeatures)
        val bundle = mapOf(FeatureKey("Number") to FeatureValue("sg"))
        val realized = realizer.realize("core.agreement.possessive", bundle, exercise.slots)
        assertTrue(exercise.expected.indexOf(realized.text) >= 0, "the real en-ru pattern must embed the realized phrase verbatim in expected")

        val optedIn = exercise.copy(
            tileTargetSlots = listOf("noun"),
            tilePhrase = TilePhrase("core.agreement.possessive", mapOf("Number" to "sg"), realized.text, realized.slotSpans),
        )

        val tiles = plTileGenerator(RandomSource { 0.5 }).fromExercise(optedIn, ScaffoldingLevel.None)
        assertNotNull(tiles, "plTileGenerator must build real tiles for en-ru via the identical TileGenerator class, not null")
        assertTrue(tiles.segments.any { it is TileSegment.Choice }, "the opted-in noun slot must become a real Choice tile")
    }
}
