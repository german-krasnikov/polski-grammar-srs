package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.data.rows.plru.*
import polski.model.Gender
import polski.model.NumberGram
import polski.model.Person
import polski.model.Tense

/** R1.10a: the hand-written part of the case and verb teaching path, on hand-built rows with a different text in every slot. */
class PlRuTeachingRowsTest {
    private fun label(tag: String) = FullCompactRow("$tag-full", "$tag-compact")
    private fun subject(mode: VerbSubjectRowGenderMode, fixed: String?, person: Int = 3, number: String = "sg") =
        VerbSubjectRow("s", person, number, mode, fixed, label("s"))

    @Test fun aSubjectKeepsItsPersonNumberLabelAndTheGenderItIsFixedTo() {
        assertEquals(
            VerbSubject("s", Person.THIRD, NumberGram.SG, false, Gender.M_PERSONAL, VerbLabel("s-full", "s-compact")),
            subject(VerbSubjectRowGenderMode.FIXED, "m-personal").toSubject(),
        )
        val selected = subject(VerbSubjectRowGenderMode.SELECTED, null, person = 1, number = "pl").toSubject()
        assertEquals(listOf(Person.FIRST, NumberGram.PL, true, null), listOf(selected.person, selected.number, selected.selectedGender, selected.fixedGender))
    }

    @Test fun aSubjectSelectedWithAFixedGenderOrFixedWithoutOneIsRefusedNamingIt() {
        for (row in listOf(subject(VerbSubjectRowGenderMode.SELECTED, "f"), subject(VerbSubjectRowGenderMode.FIXED, null))) {
            assertEquals("s", assertFailsWith<IllegalArgumentException> { row.toSubject() }.message?.substringBefore(':'))
        }
    }

    @Test fun aPersonNumberOrGenderThatIsNoIdOfTheModelIsRefused() {
        for (row in listOf(subject(VerbSubjectRowGenderMode.FIXED, "f", person = 4), subject(VerbSubjectRowGenderMode.FIXED, "f", number = "dual"), subject(VerbSubjectRowGenderMode.FIXED, "m_personal"))) {
            assertFailsWith<IllegalStateException> { row.toSubject() }
        }
    }

    @Test fun theVerbTeachingKeepsLabelsOptionsTensesAndTheFutureExplanationInPlace() {
        val teaching = VerbTeachingRow(
            listOf(subject(VerbSubjectRowGenderMode.SELECTED, null)), label("control"), listOf(VerbGenderOptionRow("m", label("m")), VerbGenderOptionRow("f", label("f"))),
            TenseLabelsRow(label("pres"), label("past"), label("fut")), ReactCompactRow("react", "compact"),
        ).toTeaching()
        assertEquals(VerbLabel("control-full", "control-compact"), teaching.genderControlLabel)
        assertEquals(listOf("m" to "m-full", "f" to "f-full"), teaching.genderOptions.map { it.id to it.label.full })
        assertEquals(listOf(Tense.PRESENT to "pres-full", Tense.PAST to "past-full", Tense.FUTURE to "fut-full"), teaching.tenseLabels.map { (tense, text) -> tense to text.full })
        assertEquals("react" to "compact", teaching.reactFutureExplanation to teaching.compactFutureExplanation)
    }

    @Test fun theFutureAuxiliaryIsBeAndItsSixForms() {
        assertEquals("be", generatedPlRuFutureAuxiliary.verbId)
        assertEquals(
            mapOf(
                NumberGram.SG to mapOf(Person.FIRST to "będę", Person.SECOND to "będziesz", Person.THIRD to "będzie"),
                NumberGram.PL to mapOf(Person.FIRST to "będziemy", Person.SECOND to "będziecie", Person.THIRD to "będą"),
            ),
            generatedPlRuFutureAuxiliary.forms,
        )
    }
}
