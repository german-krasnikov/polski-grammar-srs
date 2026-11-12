package polski.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import polski.exam.QuestionType
import polski.exam.reading.ReadingExam
import polski.exam.reading.ReadingExamState
import polski.exam.reading.ReadingPassage
import polski.exam.reading.ReadingQuestion
import polski.exam.reading.reduceReadingExam

class ReadingWordBankWebTest {
    @Test
    fun printedBankPassageHasEditableAnswerStoredBySharedReducer() {
        val bank = listOf("aktualne", "dziedziczone", "kończą", "nadawane", "nowoczesne", "podkreślają",
            "postępu", "regionalne", "uroku", "wzmocnienia", "zaostrzają", "zabronione", "zdawania", "zjawiska")
        val text = "Po co nam tradycja? Globalizacja ................ 1 konflikt między dawnym i nowym. Bank: ${bank.joinToString(" | ")}"
        val passage = ReadingPassage("word-bank-fixture-passage", "Uzupełnij tekst słowami z ramki.", text)
        val question = ReadingQuestion("word-bank-fixture-question", passage.id, QuestionType.WordBank,
            "Luka 1 w tekście „Po co nam tradycja?”", bank, "zaostrzają", pointsQuarter = 2)
        var state = ReadingExamState(ReadingExam(listOf(passage), listOf(question)))
        val container = document.createElement("div") as HTMLElement
        val controller = ExamReadingController("source-fixture", {}, externalState = { state },
            onAction = { action -> state = reduceReadingExam(state, action) })

        controller.render(container)

        assertEquals(text, container.querySelector(".exam-reading-text")?.textContent)
        assertEquals(0, container.querySelectorAll(".exam-option").length)
        val input = assertNotNull(container.querySelector(".exam-reading-q input") as? HTMLInputElement)
        input.value = "zaostrzają"
        input.dispatchEvent(Event("input"))
        assertEquals("zaostrzają", state.answers[question.id])
        controller.close()
    }
}
