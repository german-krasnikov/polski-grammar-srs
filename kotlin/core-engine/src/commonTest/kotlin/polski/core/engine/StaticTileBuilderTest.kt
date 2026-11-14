package polski.core.engine

import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.core.model.ScaffoldingLevel
import polski.core.model.TileSegment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Gap B Phase 1 (tile-coverage plan Fix 3): [StaticTileBuilder] builds tiles for pre-authored
 * expected strings via morphology reverse-lookup — a word that is a known form of the exercise's
 * noun or adjective becomes a [TileSegment.Choice]; everything else becomes [TileSegment.Locked].
 */
class StaticTileBuilderTest {

    private fun bundle(vararg pairs: Pair<String, String>) = pairs.associate { (k, v) -> FeatureKey(k) to FeatureValue(v) }

    private val morphology = TableMorphology(
        mapOf(
            "noun:wife" to mapOf(
                bundle("Number" to "sg", "Case" to "nom") to "żona",
                bundle("Number" to "sg", "Case" to "acc") to "żonę",
                bundle("Number" to "sg", "Case" to "gen") to "żony",
            ),
            "adjective:beautiful" to mapOf(
                bundle("Number" to "sg", "Gender" to "f", "Case" to "nom") to "piękna",
                bundle("Number" to "sg", "Gender" to "f", "Case" to "acc") to "piękną",
                bundle("Number" to "sg", "Gender" to "f", "Case" to "gen") to "pięknej",
            ),
        ),
    )

    // AC3.1: an expected string containing a known noun form returns a TileExercise with at least one Choice.
    @Test fun knownNounFormInExpectedProducesAtLeastOneChoiceSegment() {
        val result = StaticTileBuilder.fromStaticExercise(
            expected = "Widzę żonę.",
            nounId = "wife", adjectiveId = "beautiful",
            morphology = morphology, scaffolding = ScaffoldingLevel.None,
            random = RandomSource { 0.0 },
        )
        assertNotNull(result)
        assertTrue(result.segments.any { it is TileSegment.Choice && it.correctForm == "żonę" })
    }

    // AC3.2: every Choice segment's alternatives are real morphological forms, none identical to the correct form.
    @Test fun alternativesAreRealFormsNotIdenticalToTheCorrectForm() {
        val result = StaticTileBuilder.fromStaticExercise(
            expected = "Widzę żonę",
            nounId = "wife", adjectiveId = "beautiful",
            morphology = morphology, scaffolding = ScaffoldingLevel.None,
            random = RandomSource { 0.0 },
        )
        assertNotNull(result)
        val choice = result.segments.filterIsInstance<TileSegment.Choice>().single { it.correctForm == "żonę" }
        assertEquals("żonę", choice.correctForm)
        val allNounForms = morphology.formsOf("noun:wife").values.toSet()
        assertTrue(choice.alternatives.all { it in allNounForms }, "all alternatives must be real morphological forms")
        assertTrue(choice.alternatives.none { it == choice.correctForm }, "alternatives must not include the correct form")
    }

    // AC3.3: fewer than 2 tokens → null.
    @Test fun singleTokenExpectedReturnsNull() {
        val result = StaticTileBuilder.fromStaticExercise(
            expected = "żonę",
            nounId = "wife", adjectiveId = "beautiful",
            morphology = morphology, scaffolding = ScaffoldingLevel.None,
            random = RandomSource { 0.0 },
        )
        assertNull(result)
    }

    // Locked segments preserve words not in the morphology paradigm.
    @Test fun unknownWordsBecomeLocked() {
        val result = StaticTileBuilder.fromStaticExercise(
            expected = "Widzę żonę.",
            nounId = "wife", adjectiveId = "beautiful",
            morphology = morphology, scaffolding = ScaffoldingLevel.None,
            random = RandomSource { 0.0 },
        )
        assertNotNull(result)
        // "Widzę" and "." are not noun/adjective forms — only "żonę" is a Choice
        val locked = result.segments.filterIsInstance<TileSegment.Locked>()
        assertTrue(locked.any { it.text.contains("Widzę") || it.text == "." || it.text.contains("Widzę.") })
    }

    // Adjacent words correctly recognized: both adjective and noun forms become Choice.
    @Test fun bothAdjectiveAndNounFormsInExpectedBecomeChoice() {
        val result = StaticTileBuilder.fromStaticExercise(
            expected = "piękną żonę",
            nounId = "wife", adjectiveId = "beautiful",
            morphology = morphology, scaffolding = ScaffoldingLevel.None,
            random = RandomSource { 0.0 },
        )
        assertNotNull(result)
        val choices = result.segments.filterIsInstance<TileSegment.Choice>()
        assertEquals(2, choices.size)
        assertTrue(choices.any { it.correctForm == "piękną" })
        assertTrue(choices.any { it.correctForm == "żonę" })
    }
}
