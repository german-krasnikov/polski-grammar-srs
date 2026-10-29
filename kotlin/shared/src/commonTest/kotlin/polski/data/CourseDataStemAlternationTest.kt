package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.presentation.StemAlternation

/** The `stemAlternations` of the active pack (UC S2). The JSON parser tests went with the JSON pack reader (R1.3); the typed mapper test lands with R1.8b. */
class CourseDataStemAlternationTest {
    // The real pl-ru pack declares its Polish ó~o alternation as data, never as shared code.
    @Test fun activePackDeclaresItsAlternations() {
        assertEquals(listOf(StemAlternation('ó', 'o')), courseStemAlternations)
    }
}
