package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * NJ03 B3 pilot: independent oracle for 16 pl-ru skills read from the Biome-literal source.
 * Expectations authored before any CourseValue implementation — from courses-biome/pl-ru/skills.core-data.
 * RED until CourseValue reader exists; GREEN proves typed adapter reads the same values as JSON path.
 * NJ03PragmaticCourseCutoverDecision.md §2.
 */
class NJ03CourseValuePilotTest {

    @Test fun skillCountAndFirstSkill() {
        val skills = courseValueSkills()
        assertEquals(16, skills.size, "16 pl-ru skills")
        val first = skills[0]
        assertEquals("case.acc.n", first.id)
        assertEquals("Biernik · средний род", first.title)
        assertEquals("Przypadki", first.group)
        assertEquals("A1", first.level)
        assertEquals(emptyList(), first.prerequisites)
    }

    @Test fun lastSkillAndB1Level() {
        val skills = courseValueSkills()
        val last = skills.last()
        assertEquals("sentence.plural", last.id)
        assertEquals("A2", last.level)
        val b1 = skills.filter { it.level == "B1" }
        assertEquals(2, b1.size, "2 B1 skills: aspect and mixed")
        assertEquals(setOf("aspect", "mixed"), b1.map { it.id }.toSet())
    }

    @Test fun prerequisitesPreserved() {
        val skills = courseValueSkills()
        val byId = skills.associateBy { it.id }
        // case.gen.neg prerequisite: case.acc.f
        assertEquals(listOf("case.acc.f"), byId.getValue("case.gen.neg").prerequisites)
        // mixed has multiple prerequisites
        val mixed = byId.getValue("mixed")
        assertEquals(listOf("agreement.my", "verb.past"), mixed.prerequisites)
    }
}



