package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CourseMethodPresentationTest {
    @Test
    fun allSkillsExposeBothFourStageMethods() {
        assertEquals(16, skills.size)
        skills.forEach { skill ->
            val presentation = presentationBySkillId(skill.id)
            listOf(presentation.logic, presentation.situations).forEach { method ->
                assertTrue(method.introduce.isNotBlank(), "${skill.id} introduce")
                assertTrue(method.retrieve.isNotBlank(), "${skill.id} retrieve")
                assertTrue(method.feedback.isNotBlank(), "${skill.id} feedback")
                assertTrue(method.review.isNotBlank(), "${skill.id} review")
            }
        }
    }
}
