package polski.cloze

import polski.core.engine.RandomSource
import polski.core.engine.TableMorphology
import polski.core.model.CoreExercise
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.core.model.TilePhrase
import kotlin.test.Test
import kotlin.test.assertNull

class ErrorHuntGeneratorTest {
    private fun bundle(vararg pairs: Pair<String, String>) =
        pairs.associate { (k, v) -> FeatureKey(k) to FeatureValue(v) }

    private val morphology = TableMorphology(
        mapOf(
            "noun:wife" to mapOf(
                bundle("Number" to "sg", "Case" to "nom") to "żona",
                bundle("Number" to "sg", "Case" to "acc") to "żonę",
                bundle("Number" to "sg", "Case" to "gen") to "żony",
            ),
        ),
    )

    private val tilePhrase = TilePhrase(
        construction = "test",
        bundle = mapOf("Number" to "sg", "Case" to "acc"),
        text = "żonę",
        slotSpans = mapOf("noun" to (0 until 4)),
    )

    private fun exercise(accepted: List<String> = emptyList()) = CoreExercise(
        id = "ex-1", primarySkill = "case.acc", source = "s", prompt = "p",
        expected = "Widzę żonę tutaj.",
        accepted = accepted,
        explanation = "", tags = emptyList(), changes = emptyList(),
        slots = mapOf("noun" to "wife"),
        tileTargetSlots = listOf("noun"),
        tilePhrase = tilePhrase,
    )

    private val deterministicRandom = RandomSource { 0.0 }
    private val generator = ErrorHuntGenerator(morphology, deterministicRandom)

    /** Distractor "żona" substituted → "Widzę żona tutaj." is in accepted → must be rejected.
     *  With only "żony" valid and 1 valid < 2 required, generator must return null. */
    @Test fun rejectsDistractorThatProducesAcceptedSentence() {
        // "żona" substituted into expected → "Widzę żona tutaj." — if that's an accepted sentence, reject it
        val ex = exercise(accepted = listOf("Widzę żona tutaj."))
        // Both distractors are "żona" (nom) and "żony" (gen); "żona" produces an accepted sentence.
        // With 1 valid distractor (żony) < 2 required → null.
        assertNull(generator.fromCoreExercise(ex))
    }
}
