package polski.core.engine

import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * UniversalCorePlan.md §5.1/§5.2/§12 UC-07: [ConstructionRealizer] replaces the hand-written
 * `nounPhrase`/`verbForm` composition in `GrammarEngine.kt` with a data-driven `order`+`agree`+
 * `gov` template (§5.2) over the existing [Morphology] lookup — no lexeme text and no Polish word
 * ever appears here, only feature/case/gender identifiers already used across `:core-model`.
 */
class ConstructionRealizerTest {
    private val morphology: Morphology = TableMorphology(
        mapOf(
            "noun:wife" to mapOf(
                bundle("Number" to "sg", "Case" to "nom") to "żona",
                bundle("Number" to "sg", "Case" to "acc") to "żonę",
                bundle("Number" to "sg", "Case" to "gen") to "żony",
            ),
            "adjective:beautiful" to mapOf(
                bundle("Number" to "sg", "Gender" to "f", "Case" to "nom") to "piękna",
                bundle("Number" to "sg", "Gender" to "f", "Case" to "acc") to "piękną",
                bundle("Number" to "sg", "Gender" to "f", "Case" to "gen") to "piękną", // deliberately wrong gen form: unused by this test
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

    private fun bundle(vararg pairs: Pair<String, String>) = pairs.associate { (k, v) -> FeatureKey(k) to FeatureValue(v) }

    @Test fun realizesAnOrderedPhraseWithAgreementTakenFromTheNounSlot() {
        val realizer = ConstructionRealizer(mapOf("core.argument.case-role" to nounPhraseTemplate), morphology, lexemeFeatures)
        val realized = realizer.realize(
            "core.argument.case-role",
            bundle("Number" to "sg", "Case" to "acc"),
            mapOf("owner" to "my", "adjective" to "beautiful", "noun" to "wife"),
        )
        assertEquals("moją piękną żonę", realized.text)
        assertEquals(0 until 4, realized.slotSpans.getValue("owner"))
        assertEquals(5 until 11, realized.slotSpans.getValue("adjective"))
        assertEquals(12 until 16, realized.slotSpans.getValue("noun"))
    }

    @Test fun optionalSlotIsSkippedWhenNoLexemeIsSupplied() {
        val realizer = ConstructionRealizer(mapOf("core.argument.case-role" to nounPhraseTemplate), morphology, lexemeFeatures)
        val realized = realizer.realize(
            "core.argument.case-role",
            bundle("Number" to "sg", "Case" to "nom"),
            mapOf("adjective" to "beautiful", "noun" to "wife"),
        )
        assertEquals("piękna żona", realized.text)
    }

    @Test fun governmentPicksTheCaseFromTheBundleWhenTheConditionMatches() {
        val polarity = ConstructionTemplate(
            slots = listOf(SlotTemplate("noun", category = "noun", requiredFeatures = listOf("Number", "Case"))),
            govFeature = "Case", govDefault = "acc", govWhen = mapOf("Polarity=neg" to "gen"),
        )
        val realizer = ConstructionRealizer(mapOf("core.sentence.polarity" to polarity), morphology, lexemeFeatures)
        val negated = realizer.realize("core.sentence.polarity", bundle("Number" to "sg", "Polarity" to "neg"), mapOf("noun" to "wife"))
        assertEquals("żony", negated.text)
        val positive = realizer.realize("core.sentence.polarity", bundle("Number" to "sg", "Polarity" to "pos"), mapOf("noun" to "wife"))
        assertEquals("żonę", positive.text)
    }

    @Test fun missingRequiredSlotOrUnknownConstructionErrorsRatherThanGuessing() {
        val realizer = ConstructionRealizer(mapOf("core.argument.case-role" to nounPhraseTemplate), morphology, lexemeFeatures)
        assertFailsWith<IllegalStateException> {
            realizer.realize("core.argument.case-role", bundle("Number" to "sg", "Case" to "acc"), mapOf("owner" to "my"))
        }
        assertFailsWith<IllegalStateException> {
            realizer.realize("core.unknown", bundle("Number" to "sg", "Case" to "acc"), mapOf("noun" to "wife"))
        }
    }
}
