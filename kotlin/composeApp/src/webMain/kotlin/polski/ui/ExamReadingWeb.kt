package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import polski.presentation.UiCopy
import org.w3c.dom.HTMLInputElement
import polski.exam.QuestionType
import polski.exam.reading.*
import polski.exam.ExamAnswerStatus
import polski.exam.ExamAnswerMode
import polski.exam.formatQuarterPoints

internal class ExamReadingController(
    examId: String, private val onBack: () -> Unit,
    private val externalState: (() -> ReadingExamState)? = null,
    private val onAction: ((ReadingExamAction) -> Unit)? = null,
    private val onTimedFinish: (() -> Unit)? = null,
) {
    private val exam = examReadingData[examId] ?: ReadingExam(emptyList(), emptyList())
    private var ownState = ReadingExamState(exam)
    private val state get() = externalState?.invoke() ?: ownState
    private val currentPassageIdx get() = state.currentIndex
    private val answers get() = state.answers
    private val submitted get() = state.submitted
    private val setInputValues = mutableMapOf<String, MutableList<String>>()
    private fun dispatch(action: ReadingExamAction) { if (onAction != null) onAction.invoke(action) else ownState = reduceReadingExam(ownState, action) }
    fun close() { root = null }
    private var root: HTMLElement? = null

    fun render(container: HTMLElement) { root = container; redraw() }

    private fun redraw() {
        val c = root ?: return; c.innerHTML = ""
        if (submitted) renderResult(c)
        else if (exam.passages.isEmpty()) { c.appendChild(node("h2", "exam-title", "Нет данных")); c.appendChild(navBtn("← Назад") { onBack() }) }
        else renderPassage(c)
    }

    private fun renderPassage(c: HTMLElement) {
        val card = readingExamCardModel(state)
        val passage = card.passage
        val questions = card.questions
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
                    if (q.answerMode == ExamAnswerMode.UnorderedSet) {
                        if (q.options.isNotEmpty()) {
                            appendChild(node("div", "exam-options").apply {
                                q.options.forEach { opt ->
                                    appendChild((document.createElement("button") as HTMLButtonElement).apply {
                                        type = "button"
                                        val selected = answers[q.id].orEmpty().split("|").filter { it.isNotBlank() }
                                        className = if (opt in selected) "exam-option selected" else "exam-option"
                                        textContent = opt
                                        addEventListener("click", {
                                            val choices = answers[q.id].orEmpty().split("|").filter { it.isNotBlank() }.toMutableList()
                                            if (opt in choices) choices.remove(opt) else if (choices.size < q.answerSlotCount) choices.add(opt)
                                            dispatch(ReadingExamAction.Answer(q.id, choices.joinToString("|")))
                                            redraw()
                                        })
                                    })
                                }
                            })
                        } else {
                            val values = setInputValues.getOrPut(q.id) { MutableList(q.answerSlotCount) { index -> answers[q.id].orEmpty().split("|").getOrNull(index).orEmpty() } }
                            appendChild(node("div", "exam-input-wrap").apply {
                                values.indices.forEach { index ->
                                    appendChild((document.createElement("input") as HTMLInputElement).apply {
                                        type = "text"; className = "exam-input"; placeholder = "Wpisz..."
                                        value = values[index]
                                        addEventListener("input", {
                                            values[index] = this.value
                                            dispatch(ReadingExamAction.Answer(q.id, values.filter { it.isNotBlank() }.joinToString("|")))
                                        })
                                    })
                                }
                            })
                        }
                    } else when (q.type) {
                        QuestionType.TrueFalse, QuestionType.TrueFalseNotGiven -> {
                            val opts = q.options.ifEmpty { if (q.type == QuestionType.TrueFalseNotGiven) listOf("TAK", "NIE", "BRAK INFORMACJI") else listOf("TAK", "NIE") }
                            appendChild(node("div", "exam-options").apply {
                                opts.forEach { opt ->
                                    appendChild((document.createElement("button") as HTMLButtonElement).apply {
                                        type = "button"
                                        className = if (answers[q.id] == opt) "exam-option selected" else "exam-option"
                                        textContent = opt
                                        addEventListener("click", { dispatch(ReadingExamAction.Answer(q.id, opt)); redraw() })
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
                                            addEventListener("click", { dispatch(ReadingExamAction.Answer(q.id, opt)); redraw() })
                                        })
                                    }
                                })
                            } else {
                                appendChild(node("div", "exam-input-wrap").apply {
                                    appendChild((document.createElement("input") as HTMLInputElement).apply {
                                        type = "text"; className = "exam-input"; placeholder = "Wpisz..."
                                        value = answers[q.id].orEmpty()
                                        addEventListener("input", { dispatch(ReadingExamAction.Answer(q.id, this.value)) })
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
                                        addEventListener("click", { dispatch(ReadingExamAction.Answer(q.id, opt)); redraw() })
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
            if (currentPassageIdx > 0) appendChild(navBtn("← Назад") { dispatch(ReadingExamAction.Previous); redraw() })
            appendChild(node("span", "exam-nav-spacer"))
            if (currentPassageIdx < exam.passages.lastIndex) appendChild(navBtn(UiCopy.Next) { dispatch(ReadingExamAction.Next); redraw() })
            if (currentPassageIdx == exam.passages.lastIndex) appendChild(navBtn(UiCopy.Finish, true) { if (onTimedFinish != null) onTimedFinish.invoke() else { dispatch(ReadingExamAction.Submit); redraw() } })
        })
    }

    private fun renderResult(c: HTMLElement) {
        val result = requireNotNull(state.result)
        val total = result.possibleQuarter
        val correct = result.confirmedQuarter
        val pct = result.finalPercent
        c.appendChild(node("h2", "exam-title", "${UiCopy.Results} — ${UiCopy.Reading}"))
        c.appendChild(node("div", "exam-score").apply {
            if (pct != null) appendChild(node("div", "exam-score-big", "$pct%"))
            else appendChild(node("div", "exam-score-big", "${UiCopy.ExamConfirmedPoints}: ${formatQuarterPoints(correct)}"))
            if (!result.isFinal) appendChild(node("div", "exam-score-detail", "${UiCopy.ExamPendingPoints}: ${formatQuarterPoints(result.manualPendingQuarter)}"))
            appendChild(node("div", "exam-score-detail", "${formatQuarterPoints(correct)} / ${formatQuarterPoints(total)}"))
        })
        c.appendChild(node("div", "exam-review").apply {
            exam.questions.forEachIndexed { i, q ->
                val ans = answers[q.id].orEmpty()
                val evaluation = requireNotNull(result.evaluations[q.id])
                val ok = evaluation.status == ExamAnswerStatus.ConfirmedFull
                val status = when (evaluation.status) {
                    ExamAnswerStatus.ConfirmedFull -> "correct"
                    ExamAnswerStatus.ConfirmedPartial -> "partial"
                    ExamAnswerStatus.Wrong -> "wrong"
                    ExamAnswerStatus.PendingManual -> "pending"
                }
                appendChild(node("div", "exam-review-item $status").apply {
                    appendChild(node("span", "exam-review-num", "${i + 1}."))
                    appendChild(node("span", "exam-review-context", "${q.questionText}: ${ans.ifEmpty { "[—]" }}"))
                    if (evaluation.status == ExamAnswerStatus.PendingManual) appendChild(node("span", "exam-review-manual", UiCopy.ExamManualReview))
                    if (evaluation.status == ExamAnswerStatus.ConfirmedPartial) appendChild(node("span", "exam-review-correct", "+${formatQuarterPoints(evaluation.confirmedQuarter)} / ${formatQuarterPoints(q.pointsQuarter)}"))
                    if (!ok) appendChild(node("span", "exam-review-correct", "→ ${(listOf(q.correctAnswer) + q.scoredVariants.map { it.answer }).joinToString(" / ")}"))
                })
            }
        })
        c.appendChild(node("div", "exam-nav").apply {
            appendChild(navBtn("← К модулям") { onBack() })
            appendChild(node("span", "exam-nav-spacer"))
            appendChild(navBtn(UiCopy.TryAgain, true) { ownState = ReadingExamState(exam); setInputValues.clear(); redraw() })
        })
    }

    private fun navBtn(label: String, primary: Boolean = false, onClick: () -> Unit) =
        (document.createElement("button") as HTMLButtonElement).apply {
            type = "button"; className = if (primary) "exam-nav-btn primary" else "exam-nav-btn"
            textContent = label; addEventListener("click", { onClick() })
        }
}
