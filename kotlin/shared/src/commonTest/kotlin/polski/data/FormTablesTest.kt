package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.data.rows.plru.forms.BundleFeatureRow
import polski.data.rows.plru.forms.FormLexemeRow
import polski.data.rows.plru.forms.FormRow
import polski.model.Gender
import polski.model.GramCase
import polski.model.NumberGram
import polski.model.Person

/**
 * R1.12: the layout of the derived rows as the maps `TableMorphology` takes, on hand-built rows; the real table by its counts and against the typed lexicon rows
 * (the forms of a noun, an adjective, a possessive and the present of a verb are those of their rows; the golden forms are the vitest test of the derive).
 */
class FormTablesTest {
    private fun bundle(vararg pairs: Pair<String, String>) = pairs.associate { FeatureKey(it.first) to FeatureValue(it.second) }
    private fun form(text: String, vararg pairs: Pair<String, String>) = FormRow(pairs.map { BundleFeatureRow(it.first, it.second) }, text)

    @Test fun aLexemeKeepsItsFormsUnderTheSetOfTheFeaturesOfTheirBundleAndAnEmptyBundleIsABundle() {
        val table = listOf(
            FormLexemeRow("noun:wife", listOf(form("żona", "Number" to "sg", "Case" to "nom"), form("żony", "Case" to "gen", "Number" to "sg"))),
            FormLexemeRow("aux:do", listOf(form("did", "Tense" to "Past"), form("do"))),
        ).toFormTable()
        assertEquals(listOf("noun:wife", "aux:do"), table.keys.toList())
        assertEquals("żona", table.getValue("noun:wife").getValue(bundle("Case" to "nom", "Number" to "sg")))
        assertEquals("żony", table.getValue("noun:wife").getValue(bundle("Number" to "sg", "Case" to "gen")))
        assertEquals("did", table.getValue("aux:do").getValue(bundle("Tense" to "Past")))
        assertEquals("do", table.getValue("aux:do").getValue(emptyMap()))
    }

    @Test fun aBundleWrittenTwiceInALexemeAFeatureTwiceInABundleAndALexemeTwiceAreRefused() {
        val twice = listOf(FormLexemeRow("l", listOf(form("a", "Case" to "nom"), form("b", "Case" to "nom"))))
        val feature = listOf(FormLexemeRow("l", listOf(form("a", "Case" to "nom", "Case" to "gen"))))
        val lexeme = listOf(FormLexemeRow("l", emptyList()), FormLexemeRow("l", emptyList()))
        for (rows in listOf(twice, feature, lexeme)) assertFailsWith<IllegalArgumentException>("$rows") { rows.toFormTable() }
    }

    private val table = generatedPlRuFormTable
    private fun forms(prefix: String) = table.filterKeys { it.startsWith("$prefix:") }

    @Test fun theRealTableHasTheThirtyNineLexemesOfTheLexiconAndTheirFormsInTheOrderNounsAdjectivesPossessivesVerbs() {
        assertEquals(
            listOf(14, 7, 7, 11),
            listOf(forms("noun").size, forms("adjective").size, forms("possessive").size, forms("verb").size),
        )
        assertEquals(listOf(196, 490, 490, 384), listOf("noun", "adjective", "possessive", "verb").map { prefix -> forms(prefix).values.sumOf { it.size } })
        assertEquals(listOf("noun", "adjective", "possessive", "verb"), table.keys.map { it.substringBefore(':') }.distinct())
        assertEquals(PlRuTypedPack.nouns.map { "noun:${it.id}" }, forms("noun").keys.toList())
        assertEquals(PlRuTypedPack.verbs.map { "verb:${it.id}" }, forms("verb").keys.toList())
    }

    @Test fun everyFormOfANounAnAdjectiveAndAPossessiveIsTheOneOfItsLexiconRow() {
        for (noun in PlRuTypedPack.nouns) for (number in NumberGram.entries) for (case in GramCase.entries) {
            assertEquals(noun.forms.getValue(number).getValue(case), table.getValue("noun:${noun.id}").getValue(bundle("Number" to number.id, "Case" to case.id)), "${noun.id} $number $case")
        }
        for (adjective in PlRuTypedPack.adjectives) for (number in NumberGram.entries) for (gender in Gender.entries) for (case in GramCase.entries) {
            val expected = adjective.forms.getValue(number).getValue(gender).getValue(case)
            assertEquals(expected, table.getValue("adjective:${adjective.id}").getValue(bundle("Number" to number.id, "Gender" to gender.id, "Case" to case.id)), "${adjective.id} $number $gender $case")
        }
        for ((id, rows) in PlRuTypedPack.possessiveForms) for (number in NumberGram.entries) for (gender in Gender.entries) for (case in GramCase.entries) {
            val expected = when (rows) {
                is CoursePossessiveForms.Invariant -> rows.value
                is CoursePossessiveForms.Declined ->
                    if (number == NumberGram.SG) rows.singular.getValue(gender).getValue(case)
                    else rows.plural.getValue(if (gender == Gender.M_PERSONAL) Gender.M_PERSONAL.id else "other").getValue(case)
            }
            assertEquals(expected, table.getValue("possessive:${id.id}").getValue(bundle("Number" to number.id, "Gender" to gender.id, "Case" to case.id)), "${id.id} $number $gender $case")
        }
    }

    @Test fun thePresentOfAnImperfectiveVerbAndTheFutureOfTheAuxiliaryAreTheOnesOfTheirRows() {
        val auxiliary = requireNotNull(PlRuTypedPack.futureAuxiliary)
        for (verb in PlRuTypedPack.verbs) {
            val entries = table.getValue("verb:${verb.id}")
            for (number in NumberGram.entries) for (person in Person.entries) {
                val present = bundle("Tense" to "present", "Person" to person.id.toString(), "Number" to number.id)
                if (verb.aspect == polski.model.Aspect.IMPERFECTIVE) assertEquals(verb.present?.getValue(number)?.getValue(person), entries[present], "${verb.id} present $number $person")
                else assertEquals(null, entries[present], "${verb.id} has no present")
                if (verb.id == auxiliary.verbId) {
                    assertEquals(auxiliary.forms.getValue(number).getValue(person), entries.getValue(bundle("Tense" to "future", "Person" to person.id.toString(), "Number" to number.id)))
                }
            }
        }
    }
}
