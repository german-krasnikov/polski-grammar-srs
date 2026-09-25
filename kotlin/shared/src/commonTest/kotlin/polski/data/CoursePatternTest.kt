package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CoursePatternTest {
    @Test
    fun fillsPolishFormWithoutChangingPunctuation() {
        assertEquals("Widzę moją żonę.", renderCoursePattern("seenAcc", mapOf("acc" to "moją żonę")))
    }

    @Test
    fun rejectsMissingForm() {
        assertFailsWith<IllegalStateException> { renderCoursePattern("seenAcc", emptyMap()) }
    }
}
