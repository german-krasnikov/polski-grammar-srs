package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.data.rows.plru.*
import polski.model.Gender
import polski.model.GramCase

/** R1.6: the hand-written part of the pronoun and possessive path, on hand-built rows with a different text in every slot. */
class PlRuPronounRowsTest {
    private fun cases(tag: String) = CaseFormsRow("$tag-nom", "$tag-gen", "$tag-dat", "$tag-acc", "$tag-inst", "$tag-loc", "$tag-voc")
    private val genders = GenderFormsRow(cases("mp"), cases("ma"), cases("mi"), cases("f"), cases("n"))
    private fun possessive(kind: PossessiveFormsRowKind, value: String? = null, sg: GenderFormsRow? = null, pl: PossessivePluralRow? = null) =
        PossessiveRow("my", "label", PossessiveFormsRow(kind, value, sg, pl))

    @Test fun aPronounKeepsItsSevenCasesInTheOrderOfTheModel() {
        val forms = PronounRow("ja", cases("p")).forms.toCases()
        assertEquals(GramCase.entries, forms.keys.toList())
        assertEquals(GramCase.entries.map { "p-${it.id}" }, forms.values.toList())
    }

    @Test fun theRawFormsAreTheSameTextsUnderTheCaseIds() {
        assertEquals(9, generatedPlRuPersonalPronouns.size)
        for ((id, forms) in generatedPlRuPersonalPronouns) {
            assertEquals(forms.map { (case, text) -> case.id to text }, generatedPlRuPersonalPronounRawForms.getValue(id).toList(), id)
        }
    }

    @Test fun aDeclinedPossessiveHasTheFiveGendersAndTheTwoPluralGroupsUnderTheirModelKeys() {
        val declined = possessive(PossessiveFormsRowKind.DECLINED, sg = genders, pl = PossessivePluralRow(cases("pm"), cases("po"))).toForms()
        declined as CoursePossessiveForms.Declined
        assertEquals(listOf(Gender.M_PERSONAL, Gender.M_ANIMATE, Gender.M_INANIMATE, Gender.F, Gender.N), declined.singular.keys.toList())
        assertEquals(listOf("mp", "ma", "mi", "f", "n"), declined.singular.values.map { it.getValue(GramCase.GEN).substringBefore('-') })
        assertEquals(listOf("m-personal", "other"), declined.plural.keys.toList())
        assertEquals(listOf("pm", "po"), declined.plural.values.map { it.getValue(GramCase.NOM).substringBefore('-') })
    }

    @Test fun anInvariablePossessiveIsItsValue() {
        assertEquals(CoursePossessiveForms.Invariant("jego"), possessive(PossessiveFormsRowKind.INVARIANT, value = "jego").toForms())
    }

    @Test fun aPossessiveThatIsBothOrNeitherIsRefusedNamingTheOne() {
        val plural = PossessivePluralRow(cases("pm"), cases("po"))
        val broken = listOf(
            possessive(PossessiveFormsRowKind.INVARIANT, value = "x", sg = genders, pl = plural),
            possessive(PossessiveFormsRowKind.INVARIANT),
            possessive(PossessiveFormsRowKind.DECLINED, value = "x", sg = genders, pl = plural),
            possessive(PossessiveFormsRowKind.DECLINED, sg = genders),
            possessive(PossessiveFormsRowKind.DECLINED, pl = plural),
        )
        for (row in broken) assertEquals("my", assertFailsWith<IllegalArgumentException> { row.toForms() }.message?.substringBefore(':'))
    }
}
