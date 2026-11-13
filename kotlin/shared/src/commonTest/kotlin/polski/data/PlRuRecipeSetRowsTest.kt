package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.core.engine.ChainStepRecipe
import polski.core.engine.ChangeSpec
import polski.core.engine.ConstantSlot
import polski.core.engine.OwnerDraw
import polski.core.engine.ReasonSpec
import polski.core.engine.SkillRecipe
import polski.core.engine.StaticExercise
import polski.core.engine.TextSpec
import polski.core.engine.TextValue
import polski.data.rows.plru.*

/**
 * R1.11b: the recipes, the owner draws, the static exercise and the chain of the rows, on hand-built rows with expected values written out, and the
 * real pl-ru recipe set against values read off the sealed source (lang/pl/exercise-recipes.json sha256 8ef622c5...) before it was transcribed.
 */
class PlRuRecipeSetRowsTest {
    private val copy = TextValueRow(TextValueRowKind.COPY, key = "k")
    private val direct = TextSpecRow(TextSpecRowKind.DIRECT, value = copy)
    private val change = ChangeSpecRow(copy, copy, ReasonSpecRow(ReasonSpecRowKind.FIXED, key = "why"))
    private val spec = TextSpec.Direct(TextValue.Copy("k"))
    private val engineChange = ChangeSpec(TextValue.Copy("k"), TextValue.Copy("k"), ReasonSpec.Fixed("why"))
    private val draw = OwnerDrawRow(listOf(OwnerChoiceRow("your", "ownerYour"), OwnerChoiceRow("his", "ownerHis")), "pre", "suf")

    private fun recipe() = SkillRecipeRow("s", direct, direct, "p", "e", listOf(change), null, listOf("t"))

    @Test fun aConstructedRecipeKeepsEveryFieldAndDefaultsOnlyWhatTheEngineTypeDefaults() {
        val expected = SkillRecipe("s", spec, spec, "p", "e", listOf(engineChange), emptyList(), listOf("t"), false, null, null, null)
        assertEquals(expected, recipe().toSkillRecipe())
        val full = recipe().copy(accepted = listOf(direct, direct), includeNounGenderTag = true, numberOverride = "pl", ownerDraw = draw)
        assertEquals(expected.copy(accepted = listOf(spec, spec), includeNounGenderTag = true, numberOverride = "pl", ownerDraw = draw.toOwnerDraw()), full.toSkillRecipe())
        assertEquals(false, recipe().copy(includeNounGenderTag = false).toSkillRecipe().includeNounGenderTag)
    }

    @Test fun aConstructedRecipeWithoutWhatItNeedsIsRefusedAndAPromptCanComeFromTheDrawAlone() {
        val lacking = listOf(
            recipe().copy(source = null), recipe().copy(expected = null), recipe().copy(explanationKey = null), recipe().copy(changes = null), recipe().copy(tags = null),
            recipe().copy(promptKey = null),
        )
        for (row in lacking) assertFailsWith<IllegalArgumentException>("$row") { row.toSkillRecipe() }
        assertEquals("", recipe().copy(promptKey = null, ownerDraw = draw).toSkillRecipe().promptKey)
        assertEquals("p", recipe().copy(ownerDraw = draw).toSkillRecipe().promptKey)
    }

    private val static = StaticExerciseRow("src", "pr", "ex", "expl", listOf("a1", "a2"), "from", "to", "why", "noun", "adj", listOf("t1"))

    @Test fun aStaticRecipeIsItsExerciseAloneAndAnyOtherFieldIsRefused() {
        val exercise = StaticExercise("src", "pr", "ex", "expl", listOf("a1", "a2"), "from", "to", "why", "noun", "adj", listOf("t1"))
        assertEquals(SkillRecipe("a", staticOverride = exercise), SkillRecipeRow("a", staticOverride = static).toSkillRecipe())
        for (row in listOf(SkillRecipeRow("a", staticOverride = static, tags = emptyList()), SkillRecipeRow("a", direct, staticOverride = static), SkillRecipeRow("a", numberOverride = "pl", staticOverride = static))) {
            assertFailsWith<IllegalArgumentException>("$row") { row.toSkillRecipe() }
        }
    }

    @Test fun aStaticPoolIsNonemptyAndExclusiveWithOtherRecipeShapes() {
        val expected = StaticExercise("src", "pr", "ex", "expl", listOf("a1", "a2"), "from", "to", "why", "noun", "adj", listOf("t1"))
        assertEquals(SkillRecipe("a", staticPool = listOf(expected, expected)), SkillRecipeRow("a", staticPool = listOf(static, static)).toSkillRecipe())
        for (row in listOf(
            SkillRecipeRow("a", staticPool = emptyList()),
            SkillRecipeRow("a", staticOverride = static, staticPool = listOf(static)),
            recipe().copy(staticPool = listOf(static)),
        )) assertFailsWith<IllegalArgumentException>("$row") { row.toSkillRecipe() }
    }

    @Test fun anOwnerDrawKeepsThePoolOrderAndTheLabelOfEachOwner() {
        assertEquals(OwnerDraw(listOf("your", "his"), mapOf("your" to "ownerYour", "his" to "ownerHis"), "pre", "suf"), draw.toOwnerDraw())
        assertFailsWith<IllegalArgumentException> { draw.copy(owners = draw.owners + draw.owners[0]).toOwnerDraw() }
        assertFailsWith<IllegalArgumentException> { draw.copy(owners = emptyList()).toOwnerDraw() }
    }

    @Test fun aChainStepWithoutASkillHasTheEmptyOneAndKeepsItsTextsInTheirPlaces() {
        val other = TextSpecRow(TextSpecRowKind.CONCAT, parts = listOf(copy))
        val row = ChainStepRecipeRow(null, direct, other, "p", "e", listOf(change), listOf("t"), "their")
        assertEquals(ChainStepRecipe("", spec, TextSpec.Concat(listOf(TextValue.Copy("k"))), "p", "e", listOf(engineChange), listOf("t"), "their"), row.toChainStep())
        assertEquals("verb.past", row.copy(skill = "verb.past").toChainStep().skillId)
    }

    private val defaults = RecipeDefaultsRow("my", "go", listOf(GenderSkillRow("f", "s.f"), GenderSkillRow("default", "s.m")))
    private val step = ChainStepRecipeRow(null, direct, direct, "p", "e", emptyList(), emptyList(), "my")

    @Test fun aRecipeSetKeepsTheOrderOfTheRecipesTheDefaultsAndTheConstantSlotsInOrder() {
        val slots = listOf(ConstantSlotRow("aux", "do", "Polarity", listOf("Neg")), ConstantSlotRow("subject", "you"), ConstantSlotRow("aux", "will", "Tense", listOf("Fut")))
        val set = recipeSetOf(listOf(recipe().copy(skill = "b"), recipe().copy(skill = "a")), listOf(step), defaults.copy(constantSlots = slots))
        assertEquals(listOf("b", "a"), set.skills.keys.toList())
        assertEquals(mapOf("f" to "s.f", "default" to "s.m"), set.firstSkillByGender)
        assertEquals("my" to "go", set.defaultOwnerLexeme to set.verbLexeme)
        assertEquals(listOf(ConstantSlot("aux", "do", "Polarity", listOf("Neg")), ConstantSlot("subject", "you"), ConstantSlot("aux", "will", "Tense", listOf("Fut"))), set.constantSlots)
        assertEquals(emptyList(), recipeSetOf(listOf(recipe()), listOf(step), defaults).constantSlots)
    }

    @Test fun aRecipeSetRefusesTwoRecipesForOneSkillAHalfDefaultAndASkilllessLaterStep() {
        assertFailsWith<IllegalArgumentException> { recipeSetOf(listOf(recipe(), recipe()), listOf(step), defaults) }
        assertFailsWith<IllegalArgumentException> { recipeSetOf(listOf(recipe()), listOf(step), defaults.copy(firstSkillByGender = defaults.firstSkillByGender.take(1))) }
        assertFailsWith<IllegalArgumentException> { recipeSetOf(listOf(recipe()), listOf(step, step), defaults) }
        recipeSetOf(listOf(recipe()), listOf(step, step.copy(skill = "s")), defaults)
    }

    @Test fun theRealRecipeSetHasTheSixteenSkillsInOrderAndTheFiveChainSteps() {
        val set = generatedPlRuRecipes
        assertEquals(
            listOf("case.acc.n", "case.acc.f", "case.acc.m", "case.gen.neg", "case.inst", "case.loc", "case.dat", "agreement.my", "verb.present", "verb.past", "verb.future", "aspect", "pronouns", "sentence.question", "sentence.plural", "mixed", "degree.comparison", "verb.participles", "voice.impersonal", "wordform.nouns", "numeral.agreement", "wordform.adjectives", "sentence.connectives", "sentence.rephrase", "case.valency", "verb.mood"),
            set.skills.keys.toList(),
        )
        assertEquals(set.skills.keys, generatedPlRuSkillSpecs.map { it.id }.toSet())
        assertEquals(listOf("", "verb.past", "case.gen.neg", "agreement.my", "case.loc"), set.chain.map { it.skillId })
        assertEquals(listOf("my", "my", "my", "their", "their"), set.chain.map { it.ownerOut })
        assertEquals(mapOf("f" to "case.acc.f", "n" to "case.acc.n", "default" to "case.acc.m"), set.firstSkillByGender)
        assertEquals("my" to "go", set.defaultOwnerLexeme to set.verbLexeme)
        assertEquals(emptyList(), set.constantSlots)
    }

    @Test fun theRealSpecialRecipesAreTheOwnerDrawTheStaticExerciseAndThePluralOverride() {
        val skills = generatedPlRuRecipes.skills
        assertEquals(listOf("agreement.my"), skills.filterValues { it.ownerDraw != null }.keys.toList())
        assertEquals(listOf("your", "his", "her", "our", "yourPlural", "their"), skills.getValue("agreement.my").ownerDraw?.pool)
        assertEquals(listOf("aspect"), skills.filterValues { it.staticOverride != null }.keys.toList())
        assertEquals(mapOf("sentence.plural" to "pl"), skills.mapNotNull { (id, r) -> r.numberOverride?.let { id to it } }.toMap())
        assertEquals(listOf("verb.future", "mixed"), skills.filterValues { it.accepted.isNotEmpty() }.keys.toList())
        assertEquals(listOf("case.acc.n", "case.acc.f", "case.acc.m"), skills.filterValues { it.includeNounGenderTag }.keys.toList())
        assertEquals(2, skills.getValue("mixed").changes.size)
    }
}
