package polski.cloze

import polski.core.engine.RandomSource
import polski.core.engine.TableMorphology
import polski.core.model.CoreExercise
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.core.model.TilePhrase
import kotlin.test.Test
import kotlin.test.assertNotNull
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

    /** Morphology with only 2 forms: pickDistractors returns at most 1 candidate → null. */
    private val morphologyTwoForms = TableMorphology(
        mapOf(
            "noun:wife" to mapOf(
                bundle("Number" to "sg", "Case" to "nom") to "żona",
                bundle("Number" to "sg", "Case" to "acc") to "żonę",
            ),
        ),
    )

    private val tilePhrase = TilePhrase(
        construction = "test",
        bundle = mapOf("Number" to "sg", "Case" to "acc"),
        text = "żonę",
        slotSpans = mapOf("noun" to (0 until 4)),
    )

    private fun exercise(
        accepted: List<String> = emptyList(),
        tileTargetSlots: List<String> = listOf("noun"),
    ) = CoreExercise(
        id = "ex-1", primarySkill = "case.acc", source = "s", prompt = "p",
        expected = "Widzę żonę tutaj.",
        accepted = accepted,
        explanation = "", tags = emptyList(), changes = emptyList(),
        slots = mapOf("noun" to "wife"),
        tileTargetSlots = tileTargetSlots,
        tilePhrase = tilePhrase,
    )

    private val deterministicRandom = RandomSource { 0.0 }
    private val generator = ErrorHuntGenerator(morphology, deterministicRandom)

    // AC-EH-02: returns null for empty tileTargetSlots (extractSlot precondition).
    @Test fun returnsNullForEmptyTileTargetSlots() {
        assertNull(generator.fromCoreExercise(exercise(tileTargetSlots = emptyList())))
    }

    // AC-EH-02: returns null when fewer than 2 valid distractors survive the validity guard.
    // Here: 2-form paradigm → pickDistractors yields 1 candidate → size < 2 → null.
    @Test fun returnsNullWhenFewerThanTwoDistractorsAvailable() {
        val gen2 = ErrorHuntGenerator(morphologyTwoForms, deterministicRandom)
        assertNull(gen2.fromCoreExercise(exercise()))
    }

    // AC-EH-02: returns null when 2 distractors exist but one produces an accepted sentence
    // after normalization, leaving < 2 valid → null.
    /** Distractor "żona" substituted → "Widzę żona tutaj." is in accepted → must be rejected.
     *  With only "żony" valid and 1 valid < 2 required, generator must return null. */
    @Test fun rejectsDistractorThatProducesAcceptedSentence() {
        val ex = exercise(accepted = listOf("Widzę żona tutaj."))
        assertNull(generator.fromCoreExercise(ex))
    }

    // Positive: 3-form morphology + no accepted-sentence collision → non-null exercise.
    @Test fun returnsnNonNullWithValidDistractors() {
        assertNotNull(generator.fromCoreExercise(exercise()))
    }
}
