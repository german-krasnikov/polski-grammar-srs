package polski.grammar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.data.CoursePossessiveForms
import polski.data.PolishCourseData
import polski.model.*

class CourseMorphologyTest {
    @Test
    fun authoredTablesAreCompleteAndUsedByPublicGrammar() {
        val owners = PolishCourseData.possessiveForms
        assertEquals(PossessiveId.entries.toSet(), owners.keys)
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
        for (number in NumberGram.entries) for (person in Person.entries) {
            assertEquals(PolishCourseData.futureAuxiliary.forms.getValue(number).getValue(person),
                verbForm(PolishCourseData.futureAuxiliary.verbId, Tense.FUTURE, person, number))
        }
        assertFailsWith<IllegalStateException> { verbForm("missing", Tense.FUTURE, Person.FIRST, NumberGram.SG) }
        assertFailsWith<IllegalStateException> { verbForm("buyDone", Tense.PRESENT, Person.FIRST, NumberGram.SG) }
    }
}
