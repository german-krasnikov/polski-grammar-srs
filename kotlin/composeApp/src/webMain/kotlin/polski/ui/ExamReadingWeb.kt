package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import polski.exam.QuestionType

internal data class ReadingPassage(val id: String, val instruction: String, val text: String)
internal data class ReadingQuestion(val id: String, val passageId: String, val type: QuestionType, val questionText: String, val options: List<String>, val correctAnswer: String)
internal data class ReadingExam(val passages: List<ReadingPassage>, val questions: List<ReadingQuestion>)

internal class ExamReadingController(examId: String, private val onBack: () -> Unit) {
    private val exam = examReadingData[examId] ?: ReadingExam(emptyList(), emptyList())
    private var currentPassageIdx = 0
    private var answers = mutableMapOf<String, String>()
    private var submitted = false
    private var root: HTMLElement? = null

    fun render(container: HTMLElement) { root = container; redraw() }

    private fun redraw() {
        val c = root ?: return; c.innerHTML = ""
        if (submitted) renderResult(c)
        else if (exam.passages.isEmpty()) renderEmpty(c)
        else renderPassage(c)
    }

    private fun renderPassage(c: HTMLElement) {
        val passage = exam.passages[currentPassageIdx]
        val questions = exam.questions.filter { it.passageId == passage.id }
        c.appendChild(node("div", "exam-progress").apply {
            appendChild(node("span", "exam-progress-text", "Задание ${currentPassageIdx + 1} / ${exam.passages.size}"))
            appendChild(node("div", "exam-progress-bar").apply {
                appendChild(node("div", "exam-progress-fill").apply {
                    style.setProperty("width", "${((currentPassageIdx + 1).toDouble() / exam.passages.size * 100).toInt()}%")
                })
            })
        })
        c.appendChild(node("h3", "exam-title", passage.instruction))
        if (passage.text.isNotBlank()) {
            c.appendChild(node("div", "exam-reading-text").apply {
                setAttribute("lang", "pl")
                passage.text.split("\n").forEach { para ->
                    if (para.isNotBlank()) appendChild(node("p", text = para.trim()))
                }
            })
        }
        c.appendChild(node("div", "exam-reading-questions").apply {
            questions.forEachIndexed { i, q ->
                appendChild(node("div", "exam-reading-q").apply {
                    appendChild(node("div", "exam-reading-q-text", "${i + 1}. ${q.questionText}"))
                    appendChild(node("div", "exam-options").apply {
                        q.options.forEach { opt ->
                            val selected = answers[q.id] == opt
                            appendChild((document.createElement("button") as HTMLButtonElement).apply {
                                type = "button"
                                className = if (selected) "exam-option selected" else "exam-option"
                                textContent = opt
                                addEventListener("click", { answers[q.id] = opt; redraw() })
                            })
                        }
                    })
                })
            }
        })
        c.appendChild(node("div", "exam-nav").apply {
            if (currentPassageIdx > 0) appendChild(navBtn("← Назад") { currentPassageIdx--; redraw() })
            appendChild(node("span", "exam-nav-spacer"))
            if (currentPassageIdx < exam.passages.lastIndex) appendChild(navBtn("Далее →") { currentPassageIdx++; redraw() })
            if (currentPassageIdx == exam.passages.lastIndex) appendChild(navBtn("Завершить", true) { submitted = true; redraw() })
        })
    }

    private fun renderResult(c: HTMLElement) {
        var correct = 0; var total = 0
        exam.questions.forEach { q ->
            total++
            if (answers[q.id]?.trim()?.lowercase() == q.correctAnswer.trim().lowercase()) correct++
        }
        val pct = if (total > 0) (correct.toDouble() / total * 100).toInt() else 0
        c.appendChild(node("h2", "exam-title", "Результаты — Чтение"))
        c.appendChild(node("div", "exam-score").apply {
            appendChild(node("div", "exam-score-big", "$pct%"))
            appendChild(node("div", "exam-score-detail", "$correct / $total"))
        })
        c.appendChild(node("div", "exam-nav").apply {
            appendChild(navBtn("← К модулям") { onBack() })
            appendChild(node("span", "exam-nav-spacer"))
            appendChild(navBtn("Пройти ещё раз", true) {
                answers.clear(); currentPassageIdx = 0; submitted = false; redraw()
            })
        })
    }

    private fun renderEmpty(c: HTMLElement) {
        c.appendChild(node("h2", "exam-title", "Данные чтения ещё не загружены"))
        c.appendChild(navBtn("← Назад") { onBack() })
    }

    private fun navBtn(label: String, primary: Boolean = false, onClick: () -> Unit) =
        (document.createElement("button") as HTMLButtonElement).apply {
            type = "button"; className = if (primary) "exam-nav-btn primary" else "exam-nav-btn"
            textContent = label; addEventListener("click", { onClick() })
        }
}
