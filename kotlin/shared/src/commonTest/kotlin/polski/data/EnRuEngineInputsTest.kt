package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.core.engine.ConstantSlot

/**
 * R1.15: the English engine inputs from the rows, against the JSON that the engine still reads through EnRuJsonBridge.kt (R1.17 points the pack at the rows, deletes the
 * bridge and the three parity tests here), and against values read off the sealed sources (lang/en/exercise-recipes.json sha256 05757efb..., lang/en/realization.json sha256
 * 50f846ca..., lang/en/curriculum.json sha256 4c8f1c21...) before they were transcribed. The mappers are the generated EnRu... copies of the pl-ru ones.
 */
class EnRuEngineInputsTest {
    @Test fun theSpecsOfTheRowsEqualTheParsedCurriculumInOrder() {
        assertEquals(16, generatedEnRuSkillSpecs.size)
        assertEquals(bridgedEnRuSkillSpecs, generatedEnRuSkillSpecs)
    }

    @Test fun theRecipesOfTheRowsEqualTheParsedRecipesInOrder() {
        assertEquals(bridgedEnRuRecipes, generatedEnRuRecipes)
        assertEquals(bridgedEnRuRecipes.toString(), generatedEnRuRecipes.toString())
    }

    @Test fun theConstructionsOfTheRowsEqualTheParsedRealizationInOrder() {
        assertEquals(bridgedEnRuConstructionTemplates, generatedEnRuConstructionTemplates)
        assertEquals(bridgedEnRuConstructionTemplates.toString(), generatedEnRuConstructionTemplates.toString())
    }

    @Test fun theSixteenRecipesAreThoseOfTheSkillsAndTheChainHasItsFiveSteps() {
        val set = generatedEnRuRecipes
        assertEquals(
            listOf(
                "en:role.object", "en:verb.presentSimple", "en:possessive.my", "en:mood.question", "en:role.location", "en:role.instrument", "en:polarity.present", "en:verb.pastSimple",
                "en:role.recipient", "en:number.plural", "en:polarity.past", "en:verb.futureSimple", "en:pronouns", "en:verb.presentContinuous", "en:tense.contrast", "en:mixed",
            ),
            set.skills.keys.toList(),
        )
        assertEquals(set.skills.keys, generatedEnRuSkillSpecs.map { it.id }.toSet())
        assertEquals(listOf("", "en:verb.pastSimple", "en:polarity.past", "en:possessive.my", "en:role.location"), set.chain.map { it.skillId })
        assertEquals(listOf("my", "my", "my", "her", "her"), set.chain.map { it.ownerOut })
        assertEquals(mapOf("default" to "en:role.object"), set.firstSkillByGender)
        assertEquals("my" to "walk", set.defaultOwnerLexeme to set.verbLexeme)
    }

    @Test fun theConstantSlotsAreTheSevenOfTheSourceInOrderWithTheLaterAuxiliariesAfterTheEarlier() {
        assertEquals(
            listOf(
                ConstantSlot("prep", "role", "Case", listOf("In", "With", "To", "About", "Of")), ConstantSlot("aux", "do", "Polarity", listOf("Neg")),
                ConstantSlot("neg", "not", "Polarity", listOf("Neg")), ConstantSlot("aux", "will", "Tense", listOf("Fut")), ConstantSlot("aux", "be", "Aspect", listOf("Continuous")),
                ConstantSlot("aux", "do", "Mood", listOf("YesNoQ")), ConstantSlot("subject", "you"),
            ),
            generatedEnRuRecipes.constantSlots,
        )
    }

    @Test fun theSpecialRecipesAreTheTwoOwnerDrawsTheStaticContrastThePluralOverrideAndTheTwoAcceptedTexts() {
        val skills = generatedEnRuRecipes.skills
        assertEquals(listOf("en:possessive.my", "en:mixed"), skills.filterValues { it.ownerDraw != null }.keys.toList())
        assertEquals(listOf("your", "his", "her", "its", "our", "their"), skills.getValue("en:possessive.my").ownerDraw?.pool)
        assertEquals("mixedPrompt", skills.getValue("en:mixed").promptKey)
        assertEquals("", skills.getValue("en:possessive.my").promptKey)
        assertEquals(listOf("en:tense.contrast"), skills.filterValues { it.staticOverride != null }.keys.toList())
        assertEquals(mapOf("en:number.plural" to "pl"), skills.mapNotNull { (id, r) -> r.numberOverride?.let { id to it } }.toMap())
        assertEquals(listOf("en:polarity.present", "en:polarity.past"), skills.filterValues { it.accepted.isNotEmpty() }.keys.toList())
        assertEquals(2, skills.getValue("en:mixed").changes.size)
    }

    @Test fun theSixConstructionsHaveTheirSlotsAndTheQuestionMovesTheAuxiliaryFirst() {
        val templates = generatedEnRuConstructionTemplates
        assertEquals(
            listOf("core.argument.case-role", "core.agreement.possessive", "core.number", "core.verb.tense", "core.sentence.polarity", "core.sentence.mood"),
            templates.keys.toList(),
        )
        assertEquals(listOf("aux", "verb"), templates.getValue("core.verb.tense").slots.map { it.name })
        val question = templates.getValue("core.sentence.mood")
        assertEquals(listOf("subject", "aux", "verb"), question.slots.map { it.name })
        assertEquals(listOf("aux", "subject", "verb"), question.orderWhen.getValue("Mood=YesNoQ").map { it.name })
    }
}
