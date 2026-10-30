package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.data.rows.enru.*
import polski.data.rows.enru.forms.BundleFeatureRow
import polski.data.rows.enru.forms.FormLexemeRow
import polski.data.rows.enru.forms.FormRow

/**
 * R1.16, R1.17: the en-ru form table, derived from the lexicon rows by dsl/derive-forms.mjs (the rules and the table against the sealed lang/en/forms.generated.json, sha256 a0796ddf..., are the vitest tests
 * of the derive), here as the maps `TableMorphology` takes: its size and order, each form against its lexicon row, and the paradigms written out. The pack takes this table since R1.17.
 */
class EnRuFormTablesTest {
    private val table = generatedEnRuFormTable
    private fun bundle(vararg pairs: Pair<String, String>) = pairs.associate { FeatureKey(it.first) to FeatureValue(it.second) }
    private fun form(lexeme: String, vararg pairs: Pair<String, String>) = table.getValue(lexeme)[bundle(*pairs)]
    private fun forms(prefix: String) = table.filterKeys { it.startsWith("$prefix:") }
    /** The forms of a tense in the order 1sg, 1pl, 2sg, 2pl, 3sg, 3pl, under the tense id `tense` of the shape that asks for it. */
    private fun cells(lexeme: String, tense: String) = listOf("1", "2", "3").flatMap { person -> listOf("sg", "pl").map { number -> form(lexeme, "Tense" to tense, "Person" to person, "Number" to number) } }

    @Test fun theTableHasTheFortyNineLexemesAndThreeHundredTwentyNineFormsOfTheEnglishLexiconInTheOrderOfTheRows() {
        val prefixes = listOf("noun", "adjective", "possessive", "pronoun", "neg", "prep", "verb", "aux")
        assertEquals(49, table.size)
        assertEquals(329, table.values.sumOf { it.size })
        assertEquals(prefixes, table.keys.map { it.substringBefore(':') }.distinct())
        assertEquals(listOf(14, 7, 7, 7, 1, 1, 9, 3), prefixes.map { forms(it).size })
        assertEquals(listOf(28, 7, 7, 7, 1, 7, 252, 20), prefixes.map { forms(it).values.sumOf { entries -> entries.size } })
        assertEquals(EN_NOUN_ROW_ROWS.map { "noun:${it.id}" }, forms("noun").keys.toList())
        assertEquals(EN_ADJECTIVE_ROW_ROWS.map { "adjective:${it.id}" }, forms("adjective").keys.toList())
        assertEquals(POSSESSIVE_ROW_ROWS.map { "possessive:${it.id}" }, forms("possessive").keys.toList())
        assertEquals(EN_PRONOUN_ROW_ROWS.map { "pronoun:${it.id}" }, forms("pronoun").keys.toList())
        assertEquals(EN_VERB_ROW_ROWS.filter { it.past != null }.map { "verb:${it.id}" }, forms("verb").keys.toList())
    }

    @Test fun everyFormOfANounAnAdjectiveAPossessiveAndAPronounIsTheOneOfItsRow() {
        for (noun in EN_NOUN_ROW_ROWS) {
            assertEquals(noun.forms.sg, form("noun:${noun.id}", "Number" to "sg"), noun.id)
            assertEquals(noun.forms.pl, form("noun:${noun.id}", "Number" to "pl"), noun.id)
        }
        for (adjective in EN_ADJECTIVE_ROW_ROWS) assertEquals(adjective.forms.invariant, form("adjective:${adjective.id}"), adjective.id)
        for (possessive in POSSESSIVE_ROW_ROWS) assertEquals(possessive.forms.value, form("possessive:${possessive.id}"), possessive.id)
        for (pronoun in EN_PRONOUN_ROW_ROWS) assertEquals(pronoun.forms.Subj, form("pronoun:${pronoun.id}"), pronoun.id)
        assertEquals("not", form("neg:not"))
    }

    @Test fun thePrepositionHasAFormForEveryRoleAndTheSubjectAndTheObjectNeedNone() {
        val roles = table.getValue("prep:role")
        assertEquals(listOf("Subj", "Obj", "In", "With", "To", "About", "Of"), roles.keys.map { it.getValue(FeatureKey("Case")).name })
        assertEquals(listOf("", "", "in", "with", "to", "about", "of"), roles.values.toList())
    }

    @Test fun theParadigmOfBeIsAmAreIsWasWereAndItsCollapsedFormsAreTheLemmaAndBeing() {
        assertEquals(listOf("am", "are", "are", "are", "is", "are"), cells("verb:be", "Pres"))
        assertEquals(listOf("am", "are", "are", "are", "is", "are"), cells("verb:be", "present"))
        assertEquals(listOf("was", "were", "were", "were", "was", "were"), cells("verb:be", "past"))
        assertEquals(List(6) { "will be" }, cells("verb:be", "future"))
        assertEquals(listOf("was", "be", "being", "be"), listOf(form("verb:be", "Tense" to "Past"), form("verb:be", "Tense" to "Fut"), form("verb:be", "Aspect" to "Continuous"), form("verb:be")))
        assertEquals(28, table.getValue("verb:be").size)
    }

    @Test fun aRegularVerbHasItsLemmaItsThirdPersonItsPastAndItsIngFormAndAnIrregularOneItsOwn() {
        assertEquals(listOf("walk", "walk", "walk", "walk", "walks", "walk"), cells("verb:walk", "Pres"))
        assertEquals(List(6) { "walked" }, cells("verb:walk", "past"))
        assertEquals(List(6) { "went" }, cells("verb:go", "past"))
        assertEquals(listOf("have", "have", "have", "have", "has", "have"), cells("verb:have", "Pres"))
        assertEquals(listOf("does", "did", "doing"), listOf(form("verb:do", "Tense" to "Pres", "Person" to "3", "Number" to "sg"), form("verb:do", "Tense" to "Past"), form("verb:do", "Aspect" to "Continuous")))
        assertEquals(listOf("walked", "walk", "walking", "walk"), listOf(form("verb:walk", "Tense" to "Past"), form("verb:walk", "Tense" to "Fut"), form("verb:walk", "Aspect" to "Continuous"), form("verb:walk")))
    }

    @Test fun onlyDoBeAndWillAreAuxiliariesAndWillHasNoVerbOfItsOwn() {
        assertEquals(listOf("aux:do", "aux:be", "aux:will"), forms("aux").keys.toList())
        assertNull(table["verb:will"])
        assertEquals(listOf(7, 7, 6), forms("aux").values.map { it.size })
        assertEquals(listOf("does", "did"), listOf(form("aux:do", "Tense" to "Pres", "Person" to "3", "Number" to "sg"), form("aux:do", "Tense" to "Past")))
        assertEquals(listOf("am", "was"), listOf(form("aux:be", "Tense" to "Pres", "Person" to "1", "Number" to "sg"), form("aux:be", "Tense" to "Past")))
        assertNull(form("aux:be", "Tense" to "Fut"))
        assertEquals(List(6) { "will" }, cells("aux:will", "Fut"))
        assertNull(form("aux:will", "Tense" to "Pres", "Person" to "3", "Number" to "sg"))
    }

    @Test fun aBundleWrittenTwiceInALexemeIsRefusedByTheEnglishCopyOfTheSharedMapper() {
        val entry = FormRow(listOf(BundleFeatureRow("Case", "In")), "in")
        assertFailsWith<IllegalArgumentException> { listOf(FormLexemeRow("prep:role", listOf(entry, entry.copy(form = "on")))).toFormTable() }
    }
}
