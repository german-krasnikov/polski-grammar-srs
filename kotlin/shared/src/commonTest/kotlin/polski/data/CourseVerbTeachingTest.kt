package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.model.Gender
import polski.model.NumberGram
import polski.model.Person
import polski.model.Tense

class CourseVerbTeachingTest {
    @Test
    fun subjectsKeepOrderAndResolveOnlySelectedGender() {
        val teaching = referenceVerbTeaching
        assertEquals(listOf("ja", "ty", "on", "ona", "ono", "my", "wy", "oni", "one"), teaching.subjects.map { it.id })
        assertEquals(listOf(Person.FIRST, Person.SECOND, Person.THIRD, Person.THIRD, Person.THIRD,
            Person.FIRST, Person.SECOND, Person.THIRD, Person.THIRD), teaching.subjects.map { it.person })
        assertEquals(listOf(NumberGram.SG, NumberGram.SG, NumberGram.SG, NumberGram.SG, NumberGram.SG,
            NumberGram.PL, NumberGram.PL, NumberGram.PL, NumberGram.PL), teaching.subjects.map { it.number })
        assertEquals(listOf(Gender.M_PERSONAL, Gender.M_PERSONAL, Gender.M_PERSONAL, Gender.F, Gender.N,
            Gender.M_PERSONAL, Gender.M_PERSONAL, Gender.M_PERSONAL, Gender.F), teaching.subjects.map { it.gender(false) })
        assertEquals(listOf(Gender.F, Gender.F, Gender.M_PERSONAL, Gender.F, Gender.N,
            Gender.F, Gender.F, Gender.M_PERSONAL, Gender.F), teaching.subjects.map { it.gender(true) })
        assertEquals("oni — мужская личная группа", teaching.subjects[7].label.full)
        assertEquals("oni — мужская группа", teaching.subjects[7].label.compact)
        assertEquals(listOf("m", "f"), teaching.genderOptions.map { it.id })
        assertEquals(listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE), teaching.tenseLabels.keys.toList())
    }
}
