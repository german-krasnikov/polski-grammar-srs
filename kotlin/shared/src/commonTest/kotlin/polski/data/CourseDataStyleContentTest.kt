package polski.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import polski.presentation.NativeParallelPair

/** Exercises the `skills[].styleContent` JSON contract (UC-10 §3) with a literal fixture — never touching `courses/pl-ru/course.json`. */
class CourseDataStyleContentTest {
    @Test fun missingStyleContentMeansAllDerivedDefaults() {
        assertEquals(SkillStyleContent(), parseSkillStyleContent(null))
    }

    // The exact case.gen.neg example from Plans/Kotlin/StylesBlueprint.md §3.
    @Test fun parsesTheDocumentedContract() {
        val json = Json.parseToJsonElement(
            """
            {
              "nativeParallel": [
                {"native": "Не вижу жену.", "target": "Nie widzę żony.", "note": "падеж", "matches": false},
                {"native": "Не вижу книгу.", "target": "Nie widzę książki.", "note": "падеж 2", "matches": false}
              ],
              "examples": ["Nie mam czasu.", "Nie widzę żadnego problemu."]
            }
            """.trimIndent(),
        ).jsonObject
        val content = parseSkillStyleContent(json)
        assertEquals(
            listOf(
                NativeParallelPair("Не вижу жену.", "Nie widzę żony.", "падеж", matches = false),
                NativeParallelPair("Не вижу книгу.", "Nie widzę książki.", "падеж 2", matches = false),
            ),
            content.nativeParallel,
        )
        assertEquals(listOf("Nie mam czasu.", "Nie widzę żadnego problemu."), content.examples)
        assertEquals(null, content.rule)
        assertEquals(null, content.table)
        assertEquals(null, content.scene)
        assertEquals(null, content.why)
    }

    @Test fun parsesOptionalRuleTableSceneWhy() {
        val json = Json.parseToJsonElement(
            """
            {
              "rule": "override rule",
              "scene": "override scene",
              "why": "override why",
              "table": {"rows": [
                {"label": "L", "before": [{"text": "psa", "isEnding": false, "isChanged": false}],
                 "after": [{"text": "psa", "isEnding": false, "isChanged": false}]}
              ]}
            }
            """.trimIndent(),
        ).jsonObject
        val content = parseSkillStyleContent(json)
        assertEquals("override rule", content.rule)
        assertEquals("override scene", content.scene)
        assertEquals("override why", content.why)
        assertEquals(1, content.table?.size)
        assertEquals("L", content.table?.single()?.label)
    }

    // CONTENT has since authored styleContent for all 16 real skills (UC-10 §3/ST-11), so this no
    // longer asserts every skill defaults; it now guards the other half of the same reader
    // contract — an id absent from the course (never authored) must still default gracefully
    // instead of throwing, and every real skill's authored/derived content must parse without
    // throwing (see CourseDataStyleContentTest.parsesTheDocumentedContract for the exact shape).
    @Test fun unknownSkillIdDefaultsAndRealSkillsParseWithoutThrowing() {
        assertEquals(SkillStyleContent(), styleContentBySkillId("no-such-skill"))
        for (skill in skills) {
            styleContentBySkillId(skill.id)
        }
    }
}
