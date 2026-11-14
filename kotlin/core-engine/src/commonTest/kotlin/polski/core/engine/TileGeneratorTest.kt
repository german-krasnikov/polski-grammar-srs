package polski.core.engine

import polski.core.engine.policy.Policy
import polski.core.engine.policy.data.TILE_REVEAL_OUTCOME_ROW_ROWS
import polski.core.model.CoreExercise
import polski.core.model.FeatureFocus
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.core.model.ScaffoldingLevel
import polski.core.model.SkillSpec
import polski.core.model.TilePhrase
import polski.core.model.TileSegment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * SB-2 blueprint §3c AC2.1-2.6: [TileGenerator] over the exact `core.argument.case-role`
 * construction/fixture shape [ConstructionRealizerTest] already exercises (owner optional,
 * adjective+noun agreeing on the noun's Gender), plus a fuller paradigm so distractors are real.
 */
class TileGeneratorTest {
    private class FixedDraws(private val values: List<Double>) : RandomSource {
        private var index = 0
        override fun nextDouble(): Double = values[(index++).coerceAtMost(values.lastIndex)]
    }

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
            "possessive:my" to mapOf(
                bundle("Number" to "sg", "Gender" to "f", "Case" to "nom") to "moja",
                bundle("Number" to "sg", "Gender" to "f", "Case" to "acc") to "moją",
            ),
        ),
    )
    private val lexemeFeatures = LexemeFeatures { id -> if (id == "wife") mapOf(FeatureKey("Gender") to FeatureValue("f")) else emptyMap() }

    private val nounPhraseTemplate = ConstructionTemplate(
        slots = listOf(
            SlotTemplate("owner", category = "possessive", optional = true, requiredFeatures = listOf("Number", "Gender", "Case"), agreementSource = "noun"),
            SlotTemplate("adjective", category = "adjective", requiredFeatures = listOf("Number", "Gender", "Case"), agreementSource = "noun"),
            SlotTemplate("noun", category = "noun", requiredFeatures = listOf("Number", "Case")),
        ),
    )
    private val realizer = ConstructionRealizer(mapOf("core.argument.case-role" to nounPhraseTemplate), morphology, lexemeFeatures)
    private val skills = mapOf(
        "case.acc.f" to SkillSpec(
            id = "case.acc.f", construction = "core.argument.case-role",
            focus = FeatureFocus(FeatureKey("Case"), FeatureValue("nom"), FeatureValue("acc")),
        ),
    )

    // "moją piękną żonę" realized by core.argument.case-role under Number=sg/Case=acc, same spans ConstructionRealizerTest pins.
    private val tilePhrase = TilePhrase(
        construction = "core.argument.case-role",
        bundle = mapOf("Number" to "sg", "Case" to "acc"),
        text = "moją piękną żonę",
        slotSpans = mapOf("owner" to (0 until 4), "adjective" to (5 until 11), "noun" to (12 until 16)),
    )
    private val slots = mapOf("owner" to "my", "adjective" to "beautiful", "noun" to "wife")

    private fun exercise(expected: String = "Widzę moją piękną żonę.", tileTargetSlots: List<String> = listOf("adjective", "noun"), phrase: TilePhrase? = tilePhrase) =
        CoreExercise(
            id = "x", primarySkill = "case.acc.f", source = "s", prompt = "p", expected = expected, explanation = "exp",
            tags = emptyList(), changes = emptyList(), slots = slots, tileTargetSlots = tileTargetSlots, tilePhrase = phrase,
        )

    private fun generator(draws: List<Double> = listOf(0.1, 0.5, 0.9)) = TileGenerator(realizer, morphology, skills, FixedDraws(draws))

    @Test fun lockedAndChoiceSegmentsReproduceExpectedExactlyInPositionOrder() {
        val tiles = generator().fromExercise(exercise(), ScaffoldingLevel.None)!!
        val reconstructed = tiles.segments.sortedBy { it.position }.joinToString("") {
            when (it) {
                is TileSegment.Locked -> it.text
                is TileSegment.Choice -> it.correctForm
            }
        }
        assertEquals("Widzę moją piękną żonę.", reconstructed)
        assertTrue(tiles.segments.any { it is TileSegment.Choice && it.correctForm == "piękną" })
        assertTrue(tiles.segments.any { it is TileSegment.Choice && it.correctForm == "żonę" })
        // positions are a contiguous 0..n-1 sequence in the returned order
        assertEquals(tiles.segments.indices.toList(), tiles.segments.map { it.position })
    }

    @Test fun everyAlternativeIsARealFormDrawnFromTheLexemesOwnParadigm() {
        val tiles = generator().fromExercise(exercise(), ScaffoldingLevel.None)!!
        val choices = tiles.segments.filterIsInstance<TileSegment.Choice>()
        val adjectiveChoice = choices.first { it.lexeme == "beautiful" }
        val nounChoice = choices.first { it.lexeme == "wife" }
        val adjectiveForms = morphology.formsOf("adjective:beautiful").values.toSet()
        val nounForms = morphology.formsOf("noun:wife").values.toSet()
        assertTrue(adjectiveChoice.alternatives.all { it in adjectiveForms })
        assertTrue(nounChoice.alternatives.all { it in nounForms })
        assertTrue(adjectiveChoice.correctForm !in adjectiveChoice.alternatives)
        assertTrue(nounChoice.correctForm !in nounChoice.alternatives)
        assertEquals(FeatureKey("Case"), adjectiveChoice.targetFeature)
    }

    @Test fun emptyTileTargetSlotsReturnsNullRatherThanThrowing() {
        assertNull(generator().fromExercise(exercise(tileTargetSlots = emptyList()), ScaffoldingLevel.None))
    }

    @Test fun nullTilePhraseReturnsNullRatherThanThrowing() {
        assertNull(generator().fromExercise(exercise(phrase = null), ScaffoldingLevel.None))
    }

    @Test fun phraseTextNotFoundInExpectedReturnsNullRatherThanThrowing() {
        val mismatched = tilePhrase.copy(text = "nie ma takiego tekstu")
        assertNull(generator().fromExercise(exercise(phrase = mismatched), ScaffoldingLevel.None))
    }

    @Test fun fullScaffoldingDemotesMostTargetSlotsToLocked() {
        val tiles = generator().fromExercise(exercise(), ScaffoldingLevel.Full)!!
        val choiceCount = tiles.segments.count { it is TileSegment.Choice }
        assertEquals((2 + 1) / 3, choiceCount) // (tileTargetSlots.size + 1) / 3, per blueprint's placeholder ratio
    }

    // dsl/learning/tile-reveal.logic + tile-reveal-outcomes.core-data (DSL-LOGIC-MIGRATION.md #4): the
    // outcome row's (numerator, addend, divisor) must reproduce `(size+1)/3` bit-for-bit, not a generic
    // `ceil(size/3)` — the two diverge at size=1 (0 vs 1) under integer division.
    @Test fun fullRatioOutcomeRowReproducesTheOriginalSizePlusOneOverThreeFormulaExactly() {
        val disposition = Policy.disposition(Policy.DispositionInput(scaffolding = "full"))
        val ratio = TILE_REVEAL_OUTCOME_ROW_ROWS.single { it.disposition == disposition }
        for (size in listOf(0, 1, 2, 3, 4, 5, 10)) {
            val count = (size * ratio.numerator + ratio.addend) / ratio.divisor
            assertEquals((size + 1) / 3, count, "size=$size")
        }
    }
}
