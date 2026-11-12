package polski.ui

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import polski.exam.ExamPartScore
import polski.exam.QuestionType
import polski.exam.listening.ListeningExam
import polski.exam.listening.ListeningExamState
import polski.exam.listening.ListeningQ
import polski.exam.reading.ReadingQuestion

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

    @Test fun zeroDenominatorListeningResultRendersWithoutPercentage() {
        val exam = ListeningExam(emptyList(), "")
        val state = ListeningExamState(exam, submitted = true, result = ExamPartScore(0, 0, 0))
        val container = document.createElement("div") as HTMLElement
        val controller = ExamListeningController("missing-exam", {}, externalState = { state })

        controller.render(container)

        assertFalse(container.textContent.orEmpty().contains("%"))
        controller.close()
    }
}
