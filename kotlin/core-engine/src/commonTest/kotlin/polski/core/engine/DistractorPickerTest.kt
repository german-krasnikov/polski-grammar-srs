package polski.core.engine

import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** AC6/AC7 from CLOZE-EXERCISE-PLAN.md */
class DistractorPickerTest {
    private val morphology = TableMorphology(
        mapOf(
            "noun:wife" to mapOf(
                bundle("Number" to "sg", "Case" to "nom") to "żona",
                bundle("Number" to "sg", "Case" to "acc") to "żonę",
                bundle("Number" to "sg", "Case" to "gen") to "żony",
            ),
            "noun:small" to mapOf(
                // All forms identical — full syncretism
                bundle("Number" to "sg", "Case" to "nom") to "mały",
                bundle("Number" to "sg", "Case" to "acc") to "mały",
            ),
        ),
    )

    private fun bundle(vararg pairs: Pair<String, String>) =
        pairs.associate { (k, v) -> FeatureKey(k) to FeatureValue(v) }

    private val deterministicRandom = RandomSource { 0.0 }

    @Test fun neverReturnsCorrectForm() {
        val distractors = pickDistractors(morphology, "noun:wife", "żonę", 2, deterministicRandom)
        assertFalse("żonę" in distractors, "correct form must not be a distractor")
    }

    @Test fun returnsUpToCountDistinctForms() {
        val distractors = pickDistractors(morphology, "noun:wife", "żonę", 2, deterministicRandom)
        assertEquals(2, distractors.size)
        assertEquals(distractors.distinct().size, distractors.size, "all distractors must be distinct")
    }

    @Test fun returnsFewerThanCountWhenParadigmTooSmall() {
        // noun:wife has 3 forms total; correct is "żona"; only 2 others remain
        val distractors = pickDistractors(morphology, "noun:wife", "żona", 5, deterministicRandom)
        assertTrue(distractors.size < 5, "must return fewer than count when paradigm is small")
        assertFalse("żona" in distractors)
    }

    @Test fun handlesFullSyncretismWithNoValidCandidates() {
        // noun:small has only "mały" — after removing correctForm, candidates are empty
        val distractors = pickDistractors(morphology, "noun:small", "mały", 2, deterministicRandom)
        assertTrue(distractors.isEmpty(), "no candidates after syncretism filter")
    }

    @Test fun returnsAllCandidatesWhenFewerThanCount() {
        // noun:wife has 2 non-"żona" forms: "żonę", "żony"
        val distractors = pickDistractors(morphology, "noun:wife", "żona", 10, deterministicRandom)
        assertEquals(2, distractors.size)
        assertTrue("żonę" in distractors)
        assertTrue("żony" in distractors)
    }
}
