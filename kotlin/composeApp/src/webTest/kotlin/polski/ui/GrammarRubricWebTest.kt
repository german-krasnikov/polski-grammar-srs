package polski.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import polski.exam.ExamGradingMode
import polski.exam.QuestionType
import polski.exam.grammar.GrammarExamState
import polski.exam.grammar.GrammarItem
import polski.exam.grammar.GrammarRubricComponent
import polski.exam.grammar.GrammarTest
import polski.exam.grammar.reduceGrammarExam

class GrammarRubricWebTest {
    @Test
    fun reviewShowsOrderedQuarterCriteriaOnlyAfterSubmission() {
        val components = listOf(
            GrammarRubricComponent("c3", 3, 1, "zainteresowania"),
            GrammarRubricComponent("c1", 1, 1, "odnotowują"),
            GrammarRubricComponent("c4", 4, 1, "klientów"),
            GrammarRubricComponent("c2", 2, 1, "spadek"),
        )
        val item = GrammarItem("c1-2022-11-grammar-2-1", QuestionType.Transform,
            "Firma ___ wynik.", null, emptyList(), "odnotowują spadek zainteresowania klientów",
            points = 0, pointsQuarter = 4, gradingMode = ExamGradingMode.OpenManual,
            rubricComponents = components)
        var state = GrammarExamState(GrammarTest("c1-2022-11-grammar-2", "Uzupełnij tekst.", listOf(item)))
        val container = document.createElement("div") as HTMLElement
        val controller = ExamGrammarController("b1-2017", {}, externalState = { state },
            onAction = { action -> state = reduceGrammarExam(state, action) })

        controller.render(container)
        assertEquals(0, container.querySelectorAll(".exam-review-rubric-component").length)
        for (component in components) assertFalse(container.textContent.orEmpty().contains(component.criterionText))

        val input = container.querySelector("#exam-grammar-input") as HTMLInputElement
        input.value = "inna odpowiedź"
        input.dispatchEvent(Event("input"))
        (container.querySelector(".exam-nav-btn.primary") as HTMLButtonElement).click()

        val rows = container.querySelectorAll(".exam-review-rubric-component")
        assertEquals(4, rows.length)
        assertEquals(listOf("odnotowują · 0,25", "spadek · 0,25", "zainteresowania · 0,25", "klientów · 0,25"),
            (0 until rows.length).map { rows.item(it)?.textContent })
        controller.close()
    }
}
