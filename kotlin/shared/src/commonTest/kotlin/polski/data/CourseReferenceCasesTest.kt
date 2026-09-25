package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CourseReferenceCasesTest {
    @Test
    fun authoredNotesAndComparisonNounsKeepTheExistingOrder() {
        assertEquals(
            "Wołacz показан как форма обращения; с неодушевлёнными словами обычно используется только стилистически. «Zachwycam się…» = «Восхищаюсь…» (Narzędnik), «Przyglądam się…» — Celownik.",
            referenceCaseTeaching.reactNote,
        )
        assertEquals(
            "Wołacz — форма обращения. «Zachwycam się…» требует Narzędnik; «Przyglądam się…» — Celownik.",
            referenceCaseTeaching.compactNote,
        )
        assertEquals(
            "Читай по строке, чтобы сравнить типы. По столбцу — чтобы увидеть все формы одного слова.",
            referenceCaseTeaching.comparisonReadingHint,
        )
        assertEquals(listOf("husband", "friendM", "dog", "house", "wife", "book", "child"), comparisonNounIds)
        assertTrue(comparisonNounIds.all { id -> nounById(id).forms.isNotEmpty() })
    }
}
