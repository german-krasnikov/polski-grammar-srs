package polski.data

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import polski.presentation.StemAlternation

/** Exercises the `stemAlternations` JSON contract (UC S2) with a literal fixture — never touching `courses/pl-ru/course.json`. */
class CourseDataStemAlternationTest {
    @Test fun missingFieldMeansNoAlternations() {
        assertEquals(emptyList(), parseStemAlternations(null))
    }

    @Test fun parsesDeclaredPairs() {
        val json = Json.parseToJsonElement("""[{"a": "x", "b": "y"}, {"a": "1", "b": "2"}]""")
        assertEquals(listOf(StemAlternation('x', 'y'), StemAlternation('1', '2')), parseStemAlternations(json))
    }

    // The real pl-ru pack declares its Polish ó~o alternation as data, never as shared code.
    @Test fun activePackDeclaresItsAlternations() {
        assertEquals(listOf(StemAlternation('ó', 'o')), courseStemAlternations)
    }
}
