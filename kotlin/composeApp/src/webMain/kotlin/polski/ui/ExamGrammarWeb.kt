package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import polski.presentation.UiCopy
import org.w3c.dom.HTMLInputElement
import polski.exam.QuestionType
import polski.exam.grammar.*

internal class ExamGrammarController(examId: String, private val onBack: () -> Unit) {
    private var state = run {
        val tests = examGrammarData.getValue(examId)
        val merged = GrammarTest(examId, tests.firstOrNull()?.instruction ?: "", tests.flatMap { it.items })
        GrammarExamState(test = merged)
    }
    private var root: HTMLElement? = null
    private var inputRef: HTMLInputElement? = null

    fun render(container: HTMLElement) { root = container; redraw() }

    private fun dispatch(action: GrammarExamAction) {
        val oldIndex = state.currentIndex
        state = reduceGrammarExam(state, action)
        if (state.submitted && state.result != null) { root?.let { it.innerHTML = ""; renderResult(it) }; return }
        if (state.currentIndex != oldIndex) { redraw(); return }
        // Same question — update without full redraw to keep focus
        updateInPlace()
    }

    private fun updateInPlace() {
        val c = root ?: return
        val card = grammarExamCardModel(state)
        // Update progress
        c.querySelector(".exam-progress-text")?.textContent = card.progress
        (c.querySelector(".exam-progress-fill") as? HTMLElement)?.style?.setProperty(
            "width", "${((state.currentIndex + 1).toDouble() / state.test.items.size * 100).toInt()}%"
        )
        // Update option buttons selection
        c.querySelectorAll(".exam-option").let { nodes ->
            for (i in 0 until nodes.length) {
                val btn = nodes.item(i) as? HTMLButtonElement ?: continue
                val isSelected = card.item.options.getOrNull(i)?.let { it == card.currentAnswer } == true
                btn.className = if (isSelected) "exam-option selected" else "exam-option"
            }
        }
    }

    private fun redraw() {
        val c = root ?: return; c.innerHTML = ""; inputRef = null
        if (state.submitted && state.result != null) renderResult(c) else renderQuestion(c)
    }

    private fun renderQuestion(c: HTMLElement) {
        val card = grammarExamCardModel(state)
        c.appendChild(node("h2", "exam-title", card.instruction))
        c.appendChild(node("div", "exam-progress").apply {
            appendChild(node("span", "exam-progress-text", card.progress))
            appendChild(node("div", "exam-progress-bar").apply {
                appendChild(node("div", "exam-progress-fill").apply {
                    style.setProperty("width", "${((state.currentIndex + 1).toDouble() / state.test.items.size * 100).toInt()}%")
                })
            })
        })
        c.appendChild(node("div", "exam-context").apply {
            setAttribute("lang", "pl")
            val parts = card.item.context.split("___")
            if (parts.size == 2) {
                appendChild(node("span", text = parts[0]))
                appendChild(node("span", "exam-gap", "___"))
                appendChild(node("span", text = parts[1]))
            } else textContent = card.item.context
        })
        if (card.item.options.isNotEmpty()) {
            c.appendChild(node("div", "exam-options").apply {
                card.item.options.forEachIndexed { i, opt ->
                    appendChild((document.createElement("button") as HTMLButtonElement).apply {
                        type = "button"
                        className = if (card.currentAnswer == opt) "exam-option selected" else "exam-option"
                        textContent = "${'A' + i}. $opt"
                        addEventListener("click", { dispatch(GrammarExamAction.Answer(card.item.itemId, opt)) })
                    })
                }
            })
        } else {
            val input = (document.createElement("input") as HTMLInputElement).apply {
                type = "text"; className = "exam-input"; placeholder = "Wpisz odpowiedź..."
                value = card.currentAnswer
                addEventListener("input", {
                    dispatch(GrammarExamAction.Answer(card.item.itemId, this.value))
                })
            }
            inputRef = input
            c.appendChild(node("div", "exam-input-wrap").apply { appendChild(input) })
        }
        c.appendChild(node("div", "exam-nav").apply {
            if (!card.isFirst) appendChild(navBtn("← Назад") { dispatch(GrammarExamAction.Previous) })
            appendChild(node("span", "exam-nav-spacer"))
            if (!card.isLast) appendChild(navBtn(UiCopy.Next) { dispatch(GrammarExamAction.Next) })
            if (card.isLast) appendChild(navBtn(UiCopy.Finish, primary = true) { dispatch(GrammarExamAction.Submit) })
        })
    }

    private fun renderResult(c: HTMLElement) {
        val result = state.result ?: return
        c.appendChild(node("h2", "exam-title", "${UiCopy.Results} — ${UiCopy.Grammar}"))
        c.appendChild(node("div", "exam-score").apply {
            appendChild(node("div", "exam-score-big", "${result.percentage.toInt()}%"))
            appendChild(node("div", "exam-score-detail", "${result.correct} / ${result.total} баллов"))
        })
        c.appendChild(node("div", "exam-review").apply {
            state.test.items.forEachIndexed { i, item ->
                val answer = state.answers[item.itemId].orEmpty()
                val ok = GrammarExamScorer.score(item, answer)
                appendChild(node("div", if (ok) "exam-review-item correct" else "exam-review-item wrong").apply {
                    appendChild(node("span", "exam-review-num", "${i + 1}."))
                    appendChild(node("span", "exam-review-context", item.context.replace("___", if (answer.isNotEmpty()) "[$answer]" else "[—]")))
                    if (!ok) appendChild(node("span", "exam-review-correct", "→ ${item.correctAnswer}"))
                })
            }
        })
        c.appendChild(node("div", "exam-nav").apply {
            appendChild(navBtn("← К модулям") { onBack() })
            appendChild(node("span", "exam-nav-spacer"))
            appendChild(navBtn(UiCopy.TryAgain, primary = true) {
                state = GrammarExamState(test = state.test); inputRef = null; redraw()
            })
        })
    }
}

private fun navBtn(label: String, primary: Boolean = false, onClick: () -> Unit) =
    (document.createElement("button") as HTMLButtonElement).apply {
        type = "button"; className = if (primary) "exam-nav-btn primary" else "exam-nav-btn"
        textContent = label; addEventListener("click", { onClick() })
    }
