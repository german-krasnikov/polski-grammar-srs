package polski.ui

import kotlin.test.Test
import kotlin.test.assertFailsWith
import polski.exam.QuestionType

class ExamControlValidationWebTest {
    @Test fun printedReadingFragmentRequiresOptions() {
        assertFailsWith<IllegalArgumentException> {
            ReadingQuestion("gap-1", "passage-1", QuestionType.GapFillFragment, "Luka 1", emptyList(), "E")
        }
    }

    @Test fun printedListeningMatchingRequiresOptions() {
        assertFailsWith<IllegalArgumentException> {
            ListeningQ("match-1", QuestionType.Matching, "Mówca B", emptyList(), "2")
        }
    }
}
