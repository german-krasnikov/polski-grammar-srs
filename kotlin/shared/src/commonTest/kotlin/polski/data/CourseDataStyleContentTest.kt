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

    // Every skill in the real course still has no styleContent field today; the reader must
    // default gracefully rather than requiring CONTENT's field to exist first.
    @Test fun realCourseSkillsAllDefaultUntilContentAuthorsStyleContent() {
        for (skill in skills) {
            assertEquals(SkillStyleContent(), styleContentBySkillId(skill.id))
        }
    }
}
