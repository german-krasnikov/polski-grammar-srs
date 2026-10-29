package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.training.plCurriculum

/**
 * R1.4b: the SkillSpecs of the rows against the `lang/pl/curriculum.json` the engine still parses.
 * R1.13 makes the engine read the rows and removes the JSON path; the parity test goes with it.
 */
class PlRuSkillSpecsTest {
    @Test fun theSpecsOfTheRowsEqualTheShippedCurriculumInOrder() {
        assertEquals(16, generatedPlRuSkillSpecs.size)
        assertEquals(plCurriculum, generatedPlRuSkillSpecs)
    }

    @Test fun aSkillAndItsSpecAreOneRowSoTheyAgreeOnLevelAndPrerequisites() {
        assertEquals(
            generatedPlRuSkills.map { Triple(it.id, it.level, it.prerequisites) },
            generatedPlRuSkillSpecs.map { Triple(it.id, it.level, it.prerequisites) },
        )
    }
}
