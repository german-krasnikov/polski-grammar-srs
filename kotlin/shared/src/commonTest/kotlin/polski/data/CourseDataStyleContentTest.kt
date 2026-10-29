package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The `styleContent` of the active pack (UC-10 §3). The JSON parser tests went with the JSON pack reader (R1.3); the typed
 * mapper tests (rule, why, examples, nativeParallel with empty targetParts) land with R1.4a.
 */
class CourseDataStyleContentTest {
    // CONTENT has authored styleContent for all 16 real skills (UC-10 §3/ST-11), so this no longer asserts every skill
    // defaults; it guards the other half of the contract: an id absent from the course (never authored) still defaults
    // gracefully instead of throwing, and every real skill's authored content reads without throwing.
    @Test fun unknownSkillIdDefaultsAndRealSkillsParseWithoutThrowing() {
        assertEquals(SkillStyleContent(), styleContentBySkillId("no-such-skill"))
        for (skill in skills) {
            styleContentBySkillId(skill.id)
        }
    }
}
