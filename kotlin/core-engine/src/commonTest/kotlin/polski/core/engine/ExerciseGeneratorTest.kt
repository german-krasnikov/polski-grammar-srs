package polski.core.engine

import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.core.model.SkillSpec
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * EnRuPackPlan.md §5 gap A / task EN-02: a synthetic (not pl, not en) construction whose lexical
 * slot is a pack-level constant that is present or absent depending on the *resolved*
 * [polski.core.model.FeatureBundle] of each phrase — never from the drilled seed. This is the
 * capability [ConstantSlot] adds; `pl`'s own recipes pass none and are unaffected (see the golden
 * `TrainingParityTest`/`GrammarParityTest`).
 */
class ExerciseGeneratorTest {
    private val morphology: Morphology = TableMorphology(
        mapOf(
            "noun:thing" to mapOf(bundle("Number" to "sg") to "thing"),
            "particle:not" to mapOf(emptyMap<FeatureKey, FeatureValue>() to "not"),
        ),
    )

    private fun bundle(vararg pairs: Pair<String, String>) = pairs.associate { (k, v) -> FeatureKey(k) to FeatureValue(v) }

    private val template = ConstructionTemplate(
        slots = listOf(
            SlotTemplate("particle", category = "particle", optional = true),
            SlotTemplate("noun", category = "noun", requiredFeatures = listOf("Number")),
        ),
    )

    private fun generator(constantSlots: List<ConstantSlot>): ExerciseGenerator {
        val realizer = ConstructionRealizer(mapOf("test.slot-presence" to template), morphology, LexemeFeatures { emptyMap() })
        val skill = SkillSpec(id = "test.skill", construction = "test.slot-presence")
        val recipe = SkillRecipe(
            skillId = "test.skill",
            source = TextSpec.Direct(TextValue.Phrase(PhraseSpec("test.slot-presence", mapOf("Number" to "sg", "Polarity" to "Pos")))),
            expected = TextSpec.Direct(TextValue.Phrase(PhraseSpec("test.slot-presence", mapOf("Number" to "sg", "Polarity" to "Neg")))),
        )
        return ExerciseGenerator(
            realizer = realizer, morphology = morphology, lexemeFeatures = LexemeFeatures { emptyMap() },
            skills = mapOf(skill.id to skill), recipes = mapOf(recipe.skillId to recipe),
            seeds = listOf(mapOf("noun" to "thing", "adjective" to "unused")),
            random = RandomSource { 0.0 }, ids = ExerciseIdFactory { "generated" },
            copy = PackCopy { it }, pattern = PackPattern { _, _ -> error("not used") },
            casePrefix = CasePrefix { _, _ -> error("not used") }, pronouns = PronounForms { _, _ -> error("not used") },
            textCase = TextCase { it }, defaultOwnerLexeme = "unused-owner", verbLexeme = "unused-verb",
            constantSlots = constantSlots,
        )
    }

    @Test fun constantSlotIsPresentOnlyInPhrasesWhoseResolvedBundleMatchesItsCondition() {
        val generator = generator(listOf(ConstantSlot(slot = "particle", lexeme = "not", whenFeature = "Polarity", whenValues = listOf("Neg"))))
        val exercise = generator.generateForSkill("test.skill")
        assertEquals("thing", exercise.source, "Polarity=Pos must not draw the conditional constant slot")
        assertEquals("not thing", exercise.expected, "Polarity=Neg must draw the conditional constant slot")
    }

    @Test fun constantSlotWithNoConditionIsPresentInEveryPhrase() {
        val generator = generator(listOf(ConstantSlot(slot = "particle", lexeme = "not")))
        val exercise = generator.generateForSkill("test.skill")
        assertEquals("not thing", exercise.source)
        assertEquals("not thing", exercise.expected)
    }

    @Test fun noConstantSlotsLeavesTheOptionalSlotAbsentAsBefore() {
        val generator = generator(emptyList())
        val exercise = generator.generateForSkill("test.skill")
        assertEquals("thing", exercise.source)
        assertEquals("thing", exercise.expected)
    }
}
