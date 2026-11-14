package polski.ui

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import polski.presentation.UiCopy
import org.w3c.dom.HTMLInputElement
import polski.exam.QuestionType
import polski.exam.reading.*
import polski.exam.ExamAnswerStatus
import polski.exam.ExamAnswerMode
import polski.exam.formatQuarterPoints
import polski.exam.PracticeFeedbackState
import polski.exam.PracticeInputEvent
import polski.exam.acceptReadingPracticeInput
import polski.exam.advanceGroupedPracticeFocus
import polski.exam.clearPracticeFeedback
import polski.exam.readingReviewRows
import org.w3c.dom.events.Event

internal class ExamReadingController(
    examId: String, private val onBack: () -> Unit,
    private val externalState: (() -> ReadingExamState)? = null,
    private val onAction: ((ReadingExamAction) -> Unit)? = null,
    private val onTimedFinish: (() -> Unit)? = null,
    private val immediateFeedbackEnabled: () -> Boolean = { true },
) {
    private val initialExam = examReadingData[examId] ?: ReadingExam(emptyList(), emptyList())
    private var ownState = ReadingExamState(initialExam)
    private val state get() = externalState?.invoke() ?: ownState
    private val exam get() = state.exam
    private val currentPassageIdx get() = state.currentIndex
    private val answers get() = state.answers
    private val submitted get() = state.submitted
    private val setInputValues = mutableMapOf<String, MutableList<String>>()
    private fun dispatch(action: ReadingExamAction) { if (onAction != null) onAction.invoke(action) else ownState = reduceReadingExam(ownState, action) }
    private var feedback = PracticeFeedbackState()
    private var autoAdvanceTimer: Int? = null
    private val onVisibilityChange: (Event) -> Unit = {
        if (browserDocumentHidden()) { cancelFeedbackTimer(); updateCurrentRows() }
    }
    private fun practiceEnabled() = externalState == null && onTimedFinish == null && immediateFeedbackEnabled()
    private fun cancelFeedbackTimer() {
        autoAdvanceTimer?.let(window::clearTimeout)
        autoAdvanceTimer = null
        feedback = clearPracticeFeedback(feedback)
    }
    fun close() { cancelFeedbackTimer(); document.removeEventListener("visibilitychange", onVisibilityChange); root = null }
    private var root: HTMLElement? = null

    fun render(container: HTMLElement) { root = container; document.removeEventListener("visibilitychange", onVisibilityChange); document.addEventListener("visibilitychange", onVisibilityChange); redraw() }

    private fun onDraftInput(questionId: String) {
        if (practiceEnabled() && feedback.visible?.itemId != questionId && autoAdvanceTimer != null) {
            cancelFeedbackTimer()
            updateCurrentRows()
        }
    }

    private fun answerPractice(questionId: String, answer: String, event: PracticeInputEvent = PracticeInputEvent.Choice) {
        if (!practiceEnabled()) { dispatch(ReadingExamAction.Answer(questionId, answer)); updateQuestionRow(questionId); return }
        if (feedback.visible?.itemId != questionId && autoAdvanceTimer != null) {
            cancelFeedbackTimer()
            updateCurrentRows()
        }
        val card = readingExamCardModel(state)
        val next = acceptReadingPracticeInput(feedback, exam, card.passage.id, questionId, answer,
            enabled = true, existingAnswers = answers, event = event)
        if (next === feedback) {
            if (questionId in feedback.committed || event == PracticeInputEvent.Check) return
            dispatch(ReadingExamAction.Answer(questionId, answer))
            updateQuestionRow(questionId)
            return
        }
        feedback = next
        dispatch(ReadingExamAction.Answer(questionId, answer))
        updateQuestionRow(questionId)
        val view = feedback.visible ?: return
        if (!view.mayAutoAdvance) return
        val passageId = card.passage.id
        val passageIndex = currentPassageIdx
        val generation = view.generation
        autoAdvanceTimer = window.setTimeout({
            autoAdvanceTimer = null
            if (root != null && !submitted && !browserDocumentHidden() && practiceEnabled() &&
                currentPassageIdx == passageIndex && readingExamCardModel(state).passage.id == passageId &&
                feedback.generation == generation) {
                feedback = advanceGroupedPracticeFocus(feedback, card.questions.map { it.id })
                feedback.focusedQuestionId?.let { focusGroupedQuestion(root, it) }
                updateCurrentRows()
            }
            null
        }, 2_000)
    }

    private fun updateQuestionRow(questionId: String) {
        val question = readingExamCardModel(state).questions.firstOrNull { it.id == questionId } ?: return
        val row = root?.querySelector(".exam-reading-q[data-question-id='$questionId']") as? HTMLElement ?: return
        updateGroupedFeedbackRow(row, questionId, answers[questionId].orEmpty(), question.correctAnswer,
            question.pointsQuarter, feedback, practiceEnabled())
    }

    private fun updateCurrentRows() { readingExamCardModel(state).questions.forEach { updateQuestionRow(it.id) } }

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
                    setAttribute("data-question-id", q.id)
                    setAttribute("tabindex", "-1")
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
                                            answerPractice(q.id, choices.joinToString("|"))
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
                                            onDraftInput(q.id)
                                            values[index] = this.value
                                            if (q.id !in feedback.committed || !practiceEnabled())
                                                dispatch(ReadingExamAction.Answer(q.id, values.filter { it.isNotBlank() }.joinToString("|")))
                                        })
                                    })
                                }
                                if (practiceEnabled()) appendChild(navBtn(UiCopy.ExamCheck) {
                                    answerPractice(q.id, values.joinToString("|"), PracticeInputEvent.Check)
                                }.apply { className += " exam-check-btn" })
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
                                        addEventListener("click", { answerPractice(q.id, opt) })
                                    })
                                }
                            })
                        }
                        QuestionType.FillGap, QuestionType.ShortAnswer, QuestionType.WordBank -> {
                            if (q.type != QuestionType.WordBank && q.options.isNotEmpty()) {
                                appendChild(node("div", "exam-options").apply {
                                    q.options.forEach { opt ->
                                        appendChild((document.createElement("button") as HTMLButtonElement).apply {
                                            type = "button"
                                            className = if (answers[q.id] == opt) "exam-option selected" else "exam-option"
                                            textContent = opt
                                            addEventListener("click", { answerPractice(q.id, opt) })
                                        })
                                    }
                                })
                            } else {
                                appendChild(node("div", "exam-input-wrap").apply {
                                    appendChild((document.createElement("input") as HTMLInputElement).apply {
                                        type = "text"; className = "exam-input"; placeholder = "Wpisz..."
                                        value = answers[q.id].orEmpty()
                                        addEventListener("input", { onDraftInput(q.id); if (q.id !in feedback.committed || !practiceEnabled()) dispatch(ReadingExamAction.Answer(q.id, this.value)) })
                                    })
                                    if (practiceEnabled()) appendChild(navBtn(UiCopy.ExamCheck) {
                                        val input = querySelector(".exam-input") as? HTMLInputElement
                                        answerPractice(q.id, input?.value.orEmpty(), PracticeInputEvent.Check)
                                    }.apply { className += " exam-check-btn" })
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
                                        addEventListener("click", { answerPractice(q.id, opt) })
                                    })
                                }
                            })
                        }
                        else -> error("Unsupported reading control ${q.type} for ${passage.id}/${q.id}")
                    }
                })
            }
        })
        updateCurrentRows()
        c.appendChild(node("div", "exam-nav").apply {
            if (currentPassageIdx > 0) appendChild(navBtn("← Назад") { cancelFeedbackTimer(); dispatch(ReadingExamAction.Previous); redraw() })
            appendChild(node("span", "exam-nav-spacer"))
            if (currentPassageIdx < exam.passages.lastIndex) appendChild(navBtn(UiCopy.Next) { cancelFeedbackTimer(); dispatch(ReadingExamAction.Next); redraw() })
            if (currentPassageIdx == exam.passages.lastIndex) appendChild(navBtn(UiCopy.Finish, true) { cancelFeedbackTimer(); if (onTimedFinish != null) onTimedFinish.invoke() else { dispatch(ReadingExamAction.Submit); redraw() } })
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
        val reviews = readingReviewRows(exam, answers)
        c.appendChild(node("div", "exam-review").apply {
            reviews.forEachIndexed { i, review ->
                val q = exam.questions.first { it.id == review.itemId }
                val ans = review.chosenAnswer.orEmpty()
                val evaluation = review.evaluation
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
                    appendChild(node("span", "exam-review-chosen", "${UiCopy.ExamYourAnswer}: ${ans.ifBlank { UiCopy.NoAnswerEntered }}"))
                    if (evaluation.status == ExamAnswerStatus.PendingManual) appendChild(node("span", "exam-review-manual", UiCopy.ExamManualReview))
                    if (evaluation.status == ExamAnswerStatus.ConfirmedPartial) appendChild(node("span", "exam-review-correct", "+${formatQuarterPoints(evaluation.confirmedQuarter)} / ${formatQuarterPoints(q.pointsQuarter)}"))
                    if (!ok) appendChild(node("span", "exam-review-correct", "${UiCopy.ExamOfficialExamples}: ${review.officialExamples.joinToString(" / ")}"))
                })
            }
        })
        c.appendChild(node("div", "exam-nav").apply {
            appendChild(navBtn("← К модулям") { onBack() })
            appendChild(node("span", "exam-nav-spacer"))
            appendChild(navBtn(UiCopy.TryAgain, true) { cancelFeedbackTimer(); feedback = PracticeFeedbackState(); ownState = ReadingExamState(exam); setInputValues.clear(); redraw() })
        })
    }
}
