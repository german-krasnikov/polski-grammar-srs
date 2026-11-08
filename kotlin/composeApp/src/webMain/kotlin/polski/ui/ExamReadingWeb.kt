package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import polski.presentation.UiCopy
import org.w3c.dom.HTMLInputElement
import polski.exam.QuestionType
import polski.exam.exactAnswerQuarter
import polski.exam.formatQuarterPoints

internal data class ReadingPassage(val id: String, val instruction: String, val text: String)
internal data class ReadingQuestion(val id: String, val passageId: String, val type: QuestionType, val questionText: String, val options: List<String>, val correctAnswer: String, val pointsQuarter: Int = 4) {
    init {
        require(type !in setOf(QuestionType.MultipleChoice, QuestionType.MatchHeading, QuestionType.Matching, QuestionType.GapFillFragment) || options.isNotEmpty()) {
            "Missing options for $passageId/$id ($type)"
        }
    }
}
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
        else if (exam.passages.isEmpty()) { c.appendChild(node("h2", "exam-title", "Нет данных")); c.appendChild(navBtn("← Назад") { onBack() }) }
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
                    when (q.type) {
                        QuestionType.TrueFalse, QuestionType.TrueFalseNotGiven -> {
                            val opts = q.options.ifEmpty { if (q.type == QuestionType.TrueFalseNotGiven) listOf("TAK", "NIE", "BRAK INFORMACJI") else listOf("TAK", "NIE") }
                            appendChild(node("div", "exam-options").apply {
                                opts.forEach { opt ->
                                    appendChild((document.createElement("button") as HTMLButtonElement).apply {
                                        type = "button"
                                        className = if (answers[q.id] == opt) "exam-option selected" else "exam-option"
                                        textContent = opt
                                        addEventListener("click", { answers[q.id] = opt; redraw() })
                                    })
                                }
                            })
                        }
                        QuestionType.FillGap, QuestionType.ShortAnswer -> {
                            if (q.options.isNotEmpty()) {
                                appendChild(node("div", "exam-options").apply {
                                    q.options.forEach { opt ->
                                        appendChild((document.createElement("button") as HTMLButtonElement).apply {
                                            type = "button"
                                            className = if (answers[q.id] == opt) "exam-option selected" else "exam-option"
                                            textContent = opt
                                            addEventListener("click", { answers[q.id] = opt; redraw() })
                                        })
                                    }
                                })
                            } else {
                                appendChild(node("div", "exam-input-wrap").apply {
                                    appendChild((document.createElement("input") as HTMLInputElement).apply {
                                        type = "text"; className = "exam-input"; placeholder = "Wpisz..."
                                        value = answers[q.id].orEmpty()
                                        addEventListener("input", { answers[q.id] = this.value })
                                    })
                                })
                            }
                        }
                        QuestionType.MultipleChoice, QuestionType.MatchHeading, QuestionType.Matching, QuestionType.GapFillFragment -> {
                            appendChild(node("div", "exam-options").apply {
                                q.options.forEach { opt ->
                                    appendChild((document.createElement("button") as HTMLButtonElement).apply {
                                        type = "button"
                                        className = if (answers[q.id] == opt) "exam-option selected" else "exam-option"
                                        textContent = opt
                                        addEventListener("click", { answers[q.id] = opt; redraw() })
                                    })
                                }
                            })
                        }
                        else -> error("Unsupported reading control ${q.type} for ${passage.id}/${q.id}")
                    }
                })
            }
        })
        c.appendChild(node("div", "exam-nav").apply {
            if (currentPassageIdx > 0) appendChild(navBtn("← Назад") { currentPassageIdx--; redraw() })
            appendChild(node("span", "exam-nav-spacer"))
            if (currentPassageIdx < exam.passages.lastIndex) appendChild(navBtn(UiCopy.Next) { currentPassageIdx++; redraw() })
            if (currentPassageIdx == exam.passages.lastIndex) appendChild(navBtn(UiCopy.Finish, true) { submitted = true; redraw() })
        })
    }

    private fun renderResult(c: HTMLElement) {
        val total = exam.questions.sumOf { it.pointsQuarter }
        val correct = exam.questions.sumOf { q -> exactAnswerQuarter(answers[q.id].orEmpty(), q.correctAnswer, q.pointsQuarter) }
        val pct = if (total > 0) (correct.toDouble() / total * 100).toInt() else 0
        c.appendChild(node("h2", "exam-title", "${UiCopy.Results} — ${UiCopy.Reading}"))
        c.appendChild(node("div", "exam-score").apply {
            appendChild(node("div", "exam-score-big", "$pct%"))
            appendChild(node("div", "exam-score-detail", "${formatQuarterPoints(correct)} / ${formatQuarterPoints(total)}"))
        })
        c.appendChild(node("div", "exam-review").apply {
            exam.questions.forEachIndexed { i, q ->
                val ans = answers[q.id].orEmpty()
                val ok = exactAnswerQuarter(ans, q.correctAnswer, q.pointsQuarter) == q.pointsQuarter
                appendChild(node("div", if (ok) "exam-review-item correct" else "exam-review-item wrong").apply {
                    appendChild(node("span", "exam-review-num", "${i + 1}."))
                    appendChild(node("span", "exam-review-context", "${q.questionText}: ${ans.ifEmpty { "[—]" }}"))
                    if (!ok) appendChild(node("span", "exam-review-correct", "→ ${q.correctAnswer}"))
                })
            }
        })
        c.appendChild(node("div", "exam-nav").apply {
            appendChild(navBtn("← К модулям") { onBack() })
            appendChild(node("span", "exam-nav-spacer"))
            appendChild(navBtn(UiCopy.TryAgain, true) { answers.clear(); currentPassageIdx = 0; submitted = false; redraw() })
        })
    }

    private fun navBtn(label: String, primary: Boolean = false, onClick: () -> Unit) =
        (document.createElement("button") as HTMLButtonElement).apply {
            type = "button"; className = if (primary) "exam-nav-btn primary" else "exam-nav-btn"
            textContent = label; addEventListener("click", { onClick() })
        }
}
