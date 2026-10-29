package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.core.engine.ChangeSpec
import polski.core.engine.PhraseSpec
import polski.core.engine.ReasonRule
import polski.core.engine.ReasonSpec
import polski.core.engine.TextSpec
import polski.core.engine.TextValue
import polski.data.rows.plru.*

/** R1.11a: the hand-written part of the recipe expressions, on hand-built rows with a different text in every slot. The expected values are written out, not read from the mapper. */
class PlRuRecipeRowsTest {
    private val bundle = listOf(FeatureValueRow("Number", "sg"), FeatureValueRow("Case", "nom"))
    private val phraseRow = PhraseSpecRow("core.c", bundle)
    private val phrase = PhraseSpec("core.c", mapOf("Number" to "sg", "Case" to "nom"))
    private fun copyRow(key: String = "k") = TextValueRow(TextValueRowKind.COPY, key = key)

    @Test fun aCopyAndALiteralKeepTheirTextAndAnEmptyLiteralIsData() {
        assertEquals(TextValue.Copy("accPrompt"), copyRow("accPrompt").toTextValue())
        assertEquals(TextValue.Literal("don't"), TextValueRow(TextValueRowKind.LITERAL, value = "don't").toTextValue())
        assertEquals(TextValue.Literal(""), TextValueRow(TextValueRowKind.LITERAL, value = "").toTextValue())
    }

    @Test fun aPhraseKeepsTheConstructionTheFeaturesAndTheOwner() {
        assertEquals(TextValue.Phrase(phrase), TextValueRow(TextValueRowKind.PHRASE, spec = phraseRow).toTextValue())
        val owned = TextValueRow(TextValueRowKind.PHRASE, spec = phraseRow.copy(ownerLexeme = "their")).toTextValue()
        assertEquals(TextValue.Phrase(phrase.copy(ownerLexeme = "their")), owned)
        assertEquals(TextValue.Phrase(PhraseSpec("core.c", emptyMap())), TextValueRow(TextValueRowKind.PHRASE, spec = PhraseSpecRow("core.c", emptyList())).toTextValue())
    }

    @Test fun aFeatureWrittenTwiceInABundleIsRefusedNotOverwritten() {
        val twice = phraseRow.copy(bundle = bundle + FeatureValueRow("Number", "pl"))
        assertFailsWith<IllegalArgumentException> { TextValueRow(TextValueRowKind.PHRASE, spec = twice).toTextValue() }
    }

    @Test fun aPronounMapsEachGenderToItsPronounAndNeedsTheDefault() {
        val genders = listOf(GenderPronounRow("f", "ona"), GenderPronounRow("n", "ono"), GenderPronounRow("default", "on"))
        assertEquals(
            TextValue.Pronoun(mapOf("f" to "ona", "n" to "ono", "default" to "on"), "acc"),
            TextValueRow(TextValueRowKind.PRONOUN, genderToKey = genders, caseId = "acc").toTextValue(),
        )
        assertFailsWith<IllegalArgumentException> { TextValueRow(TextValueRowKind.PRONOUN, genderToKey = genders.take(2), caseId = "acc").toTextValue() }
        assertFailsWith<IllegalArgumentException> { TextValueRow(TextValueRowKind.PRONOUN, genderToKey = genders + genders[0], caseId = "acc").toTextValue() }
    }

    @Test fun capitalizedWrapsAnyKindAndFalseOrAbsentDoesNot() {
        assertEquals(TextValue.Capitalized(TextValue.Phrase(phrase)), TextValueRow(TextValueRowKind.PHRASE, true, spec = phraseRow).toTextValue())
        assertEquals(TextValue.Capitalized(TextValue.Copy("k")), copyRow().copy(capitalized = true).toTextValue())
        assertEquals(TextValue.Copy("k"), copyRow().copy(capitalized = false).toTextValue())
    }

    @Test fun aKindWithoutItsFieldOrWithAFieldOfAnotherKindIsRefused() {
        val bad = listOf(
            TextValueRow(TextValueRowKind.COPY), TextValueRow(TextValueRowKind.LITERAL, key = "k"), copyRow().copy(value = "v"),
            TextValueRow(TextValueRowKind.PRONOUN, caseId = "acc"), TextValueRow(TextValueRowKind.PHRASE, spec = phraseRow, caseId = "acc"),
        )
        for (row in bad) assertFailsWith<IllegalArgumentException>("$row") { row.toTextValue() }
    }

    private val v1 = copyRow("one")
    private val v2 = TextValueRow(TextValueRowKind.LITERAL, value = ".")

    @Test fun eachTextSpecKindKeepsItsFieldsInTheirPlaces() {
        assertEquals(TextSpec.Direct(TextValue.Copy("one")), TextSpecRow(TextSpecRowKind.DIRECT, value = v1).toTextSpec())
        assertEquals(TextSpec.Concat(listOf(TextValue.Copy("one"), TextValue.Literal("."))), TextSpecRow(TextSpecRowKind.CONCAT, parts = listOf(v1, v2)).toTextSpec())
        assertEquals(
            TextSpec.Pattern("seen", mapOf("acc" to TextValue.Copy("one"), "gen" to TextValue.Literal("."))),
            TextSpecRow(TextSpecRowKind.PATTERN, key = "seen", values = listOf(PatternValueRow("acc", v1), PatternValueRow("gen", v2))).toTextSpec(),
        )
        assertEquals(
            TextSpec.Prefixed(phrase, "nom", "!"),
            TextSpecRow(TextSpecRowKind.PREFIXED, spec = phraseRow, prefixCase = "nom", punct = "!").toTextSpec(),
        )
    }

    @Test fun aPatternValueNamedTwiceIsRefusedAndAnEmptyTextIsNotAbsence() {
        val twice = TextSpecRow(TextSpecRowKind.PATTERN, key = "seen", values = listOf(PatternValueRow("acc", v1), PatternValueRow("acc", v2)))
        assertFailsWith<IllegalArgumentException> { twice.toTextSpec() }
        assertEquals(TextSpec.Prefixed(phrase, "nom", ""), TextSpecRow(TextSpecRowKind.PREFIXED, spec = phraseRow, prefixCase = "nom", punct = "").toTextSpec())
    }

    @Test fun aTextSpecKindWithoutAFieldOrWithAnotherKindsFieldIsRefused() {
        val bad = listOf(
            TextSpecRow(TextSpecRowKind.DIRECT), TextSpecRow(TextSpecRowKind.DIRECT, value = v1, key = "k"), TextSpecRow(TextSpecRowKind.CONCAT, value = v1),
            TextSpecRow(TextSpecRowKind.PATTERN, key = "seen"), TextSpecRow(TextSpecRowKind.PREFIXED, spec = phraseRow, prefixCase = "nom"),
        )
        for (row in bad) assertFailsWith<IllegalArgumentException>("$row") { row.toTextSpec() }
    }

    @Test fun aReasonIsOneKeyOrRulesInOrderWithTheDefaultOneAmongThem() {
        assertEquals(ReasonSpec.Fixed("why"), ReasonSpecRow(ReasonSpecRowKind.FIXED, key = "why").toReasonSpec())
        val rules = listOf(ReasonRuleRow("unchanged", "a"), ReasonRuleRow("default", "b"))
        assertEquals(ReasonSpec.Classified(listOf(ReasonRule("unchanged", "a"), ReasonRule("default", "b"))), ReasonSpecRow(ReasonSpecRowKind.CLASSIFIED, rules = rules).toReasonSpec())
        for (bad in listOf(ReasonSpecRow(ReasonSpecRowKind.CLASSIFIED, rules = rules.take(1)), ReasonSpecRow(ReasonSpecRowKind.FIXED), ReasonSpecRow(ReasonSpecRowKind.FIXED, key = "k", rules = rules))) {
            assertFailsWith<IllegalArgumentException>("$bad") { bad.toReasonSpec() }
        }
    }

    @Test fun aChangeKeepsItsTwoTextsAndItsReason() {
        val change = ChangeSpecRow(v1, TextValueRow(TextValueRowKind.PHRASE, spec = phraseRow), ReasonSpecRow(ReasonSpecRowKind.FIXED, key = "why"))
        assertEquals(ChangeSpec(TextValue.Copy("one"), TextValue.Phrase(phrase), ReasonSpec.Fixed("why")), change.toChangeSpec())
    }
}
