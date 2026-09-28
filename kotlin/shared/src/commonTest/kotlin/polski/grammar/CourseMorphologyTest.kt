package polski.grammar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.data.CoursePossessiveForms
import polski.data.packRegistry
import polski.model.*

class CourseMorphologyTest {
    @Test
    fun authoredTablesAreCompleteAndUsedByPublicGrammar() {
        val owners = packRegistry.active.possessiveForms
        // PossessiveId.ITS is en-ru-only (EnRuAcceptance §7 item 1) — pl-ru's own table never
        // declares it, so it is excluded from pl's completeness check here.
        assertEquals(PossessiveId.entries.toSet() - PossessiveId.ITS, owners.keys)
        for ((id, forms) in owners) when (forms) {
            is CoursePossessiveForms.Invariant -> {
                assertEquals(forms.value, possessiveForm(id, Gender.F, NumberGram.SG, GramCase.ACC))
                assertEquals(forms.value, possessiveForm(id, Gender.M_PERSONAL, NumberGram.PL, GramCase.VOC))
            }
            is CoursePossessiveForms.Declined -> {
                assertEquals(Gender.entries.toSet(), forms.singular.keys)
                assertEquals(setOf("m-personal", "other"), forms.plural.keys)
                for (gender in Gender.entries) for (gramCase in GramCase.entries) {
                    assertEquals(forms.singular.getValue(gender).getValue(gramCase),
                        possessiveForm(id, gender, NumberGram.SG, gramCase))
                    val group = if (gender == Gender.M_PERSONAL) "m-personal" else "other"
                    assertEquals(forms.plural.getValue(group).getValue(gramCase),
                        possessiveForm(id, gender, NumberGram.PL, gramCase))
                }
            }
        }
        // pl-ru always declares a compound future auxiliary (EnRuAcceptance §7 item 1: it is
        // absent, and null, only for a pack like en-ru with no such tense) — `!!` is safe here.
        val futureAuxiliary = packRegistry.active.futureAuxiliary!!
        for (number in NumberGram.entries) for (person in Person.entries) {
            assertEquals(futureAuxiliary.forms.getValue(number).getValue(person),
                verbForm(futureAuxiliary.verbId, Tense.FUTURE, person, number))
        }
        assertFailsWith<IllegalStateException> { verbForm("missing", Tense.FUTURE, Person.FIRST, NumberGram.SG) }
        assertFailsWith<IllegalStateException> { verbForm("buyDone", Tense.PRESENT, Person.FIRST, NumberGram.SG) }
    }
}
