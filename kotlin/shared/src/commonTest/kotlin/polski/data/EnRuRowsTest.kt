package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import polski.data.rows.enru.*
import polski.model.PossessiveId

/**
 * R1.14a: the en-ru lexicon mapper on hand-built rows with a different text in every slot, and the real rows against values read off the sealed
 * lang/en/lexicon.json (sha256 f96da7fc...) before it was transcribed.
 */
class EnRuRowsTest {
    private fun noun(id: String, lemma: String = "l-$id") = EnNounRow(id, lemma, "m-$id", EnNounFormsRow("sg-$id", "pl-$id"))
    private fun pronoun(id: String) = EnPronounRow(id, EnPronounFormsRow("subj-$id", "obj-$id"))

    @Test fun nounLabelsPairTheLemmaWithTheMeaningInTheOrderOfTheRows() {
        val labels = listOf(noun("work", lemma = "job"), noun("wife")).toNounLabels()
        assertEquals(mapOf("work" to ("job" to "m-work"), "wife" to ("l-wife" to "m-wife")), labels)
        assertEquals(listOf("work", "wife"), labels.keys.toList())
    }

    @Test fun aVerbKeepsItsIdItsLemmaAndItsMeaningInTheirPlaces() {
        val verb = EnVerbRow("be", "lemma", "meaning", present3sg = "is")
        assertEquals(listOf(EnVerb("be", "lemma", "meaning")), listOf(verb).toEnVerbs())
    }

    @Test fun aPronounIsItsSubjectFormAndItsRawFormsKeepSubjAndObjUnderTheirCaseIds() {
        val rows = listOf(pronoun("he"), pronoun("she"))
        assertEquals(listOf(EnPronoun("he", "subj-he"), EnPronoun("she", "subj-she")), rows.toEnPronouns())
        assertEquals(mapOf("he" to mapOf("Subj" to "subj-he", "Obj" to "obj-he"), "she" to mapOf("Subj" to "subj-she", "Obj" to "obj-she")), rows.toRawForms())
    }

    @Test fun aPossessiveIsItsIdAndItsLabel() {
        val row = PossessiveRow("their", "label", PossessiveFormsRow(PossessiveFormsRowKind.INVARIANT, value = "their"))
        assertEquals(listOf(Possessive(PossessiveId.THEIR, "label")), listOf(row).toPossessives())
    }

    @Test fun theRealLexiconHasTheNounsVerbsPronounsAndPossessivesOfTheSource() {
        assertEquals(
            listOf("wife", "woman", "book", "work", "husband", "friend", "son", "dog", "cat", "car", "house", "phone", "child", "window"),
            generatedEnRuNounLabels.keys.toList(),
        )
        assertEquals("job" to "работа", generatedEnRuNounLabels.getValue("work"))
        assertEquals(listOf("walk", "see", "talk", "go", "like", "read", "have", "do", "be", "will"), generatedEnVerbs.map { it.id })
        assertEquals(listOf("I", "you", "he", "she", "it", "we", "they"), generatedEnPronouns.map { it.id })
        assertEquals(mapOf("Subj" to "I", "Obj" to "me"), generatedEnRuPersonalPronounRawForms.getValue("I"))
        assertEquals(listOf("my", "your", "his", "her", "its", "our", "their"), generatedEnRuPossessives.map { it.id.id })
    }

    @Test fun theOptionalFormsOfTheRealVerbsAreWrittenOnlyWhereTheSourceHasThem() {
        val verbs = EN_VERB_ROW_ROWS.associateBy { it.id }
        assertEquals(Triple("am", "are", "were"), verbs.getValue("be").let { Triple(it.presentSg1, it.presentPl, it.pastPl) })
        assertNull(verbs.getValue("walk").presentSg1)
        assertNull(verbs.getValue("will").past)
        assertNull(verbs.getValue("will").ing)
        assertEquals("will", verbs.getValue("will").present3sg)
        assertEquals("went", verbs.getValue("go").past)
    }
}
