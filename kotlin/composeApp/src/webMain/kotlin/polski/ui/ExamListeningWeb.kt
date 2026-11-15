package polski.ui

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import polski.audio.AudioSegment
import polski.exam.listening.*
import polski.exam.formatQuarterPoints
import polski.exam.QuestionType
import polski.exam.ExamAnswerMode
import polski.exam.ExamAnswerStatus
import polski.presentation.UiCopy
import polski.exam.PracticeFeedbackState
import polski.exam.PracticeInputEvent
import polski.exam.acceptListeningPracticeInput
import polski.exam.advanceGroupedPracticeFocus
import polski.exam.clearPracticeFeedback
import polski.exam.listeningReviewRows
import org.w3c.dom.events.Event

internal class ExamListeningController(
    examId: String, private val onBack: () -> Unit,
    private val externalState: (() -> ListeningExamState)? = null,
    private val onAction: ((ListeningExamAction) -> Unit)? = null,
    private val onTimedFinish: (() -> Unit)? = null,
    private val immediateFeedbackEnabled: () -> Boolean = { true },
) {
    private val exam = examListeningData[examId] ?: ListeningExam(emptyList(), "")
    private var ownState = ListeningExamState(exam)
    private val state get() = externalState?.invoke() ?: ownState
    private val currentTaskIdx get() = state.currentIndex
    private val answers get() = state.answers
    private fun dispatch(action: ListeningExamAction) { if (onAction != null) onAction.invoke(action) else ownState = reduceListeningExam(ownState, action) }
    private val setInputValues = mutableMapOf<String, MutableList<String>>()
    private val submitted get() = state.submitted
    private var root: HTMLElement? = null
    private val player = WebAudioSegmentPlayer()
    private var renderedTaskIdx = -1
    private var feedback = PracticeFeedbackState()
    private var autoAdvanceTimer: Int? = null
    private val onVisibilityChange: (Event) -> Unit = {
        if (browserDocumentHidden()) { player.cancelPlayback(); cancelFeedbackTimer(); updateCurrentRows() }
    }
    private fun practiceEnabled() = externalState == null && onTimedFinish == null && immediateFeedbackEnabled()
    private fun cancelFeedbackTimer() {
        autoAdvanceTimer?.let(window::clearTimeout)
        autoAdvanceTimer = null
        feedback = clearPracticeFeedback(feedback)
    }

    fun render(container: HTMLElement) { root = container; document.removeEventListener("visibilitychange", onVisibilityChange); document.addEventListener("visibilitychange", onVisibilityChange); redraw() }

    private fun onDraftInput(questionId: String) {
        if (practiceEnabled() && feedback.visible?.itemId != questionId && autoAdvanceTimer != null) {
            cancelFeedbackTimer()
            updateCurrentRows()
        }
    }

    /** Releases the single media element when the exam route or module is left. */
    fun close() {
        cancelFeedbackTimer()
        document.removeEventListener("visibilitychange", onVisibilityChange)
        player.dispose()
        renderedTaskIdx = -1
        root = null
    }

    private fun answerPractice(questionId: String, answer: String, event: PracticeInputEvent = PracticeInputEvent.Choice) {
        if (!practiceEnabled()) { dispatch(ListeningExamAction.Answer(questionId, answer)); updateQuestionRow(questionId); return }
        if (feedback.visible?.itemId != questionId && autoAdvanceTimer != null) {
            cancelFeedbackTimer()
            updateCurrentRows()
        }
        val task = exam.tasks[currentTaskIdx]
        val next = acceptListeningPracticeInput(feedback, exam, task.id, questionId, answer,
            enabled = true, existingAnswers = answers, event = event)
        if (next === feedback) {
            if (questionId in feedback.committed || event == PracticeInputEvent.Check) return
            dispatch(ListeningExamAction.Answer(questionId, answer))
            updateQuestionRow(questionId)
            return
        }
        feedback = next
        dispatch(ListeningExamAction.Answer(questionId, answer))
        updateQuestionRow(questionId)
        val view = feedback.visible ?: return
        if (!view.mayAutoAdvance) return
        val taskIndex = currentTaskIdx
        val taskId = task.id
        val generation = view.generation
        autoAdvanceTimer = window.setTimeout({
            autoAdvanceTimer = null
            if (root != null && !submitted && !browserDocumentHidden() && practiceEnabled() &&
                currentTaskIdx == taskIndex && exam.tasks[currentTaskIdx].id == taskId &&
                feedback.generation == generation) {
                feedback = advanceGroupedPracticeFocus(feedback, task.questions.map { it.id })
                feedback.focusedQuestionId?.let { focusGroupedQuestion(root, it) }
                updateCurrentRows()
            }
            null
        }, 2_000)
    }

    private fun updateQuestionRow(questionId: String) {
        val question = exam.tasks.getOrNull(currentTaskIdx)?.questions?.firstOrNull { it.id == questionId } ?: return
        val row = root?.querySelector(".exam-reading-q[data-question-id='$questionId']") as? HTMLElement ?: return
        updateGroupedFeedbackRow(row, questionId, answers[questionId].orEmpty(), question.correctAnswer,
            question.pointsQuarter, feedback, practiceEnabled())
    }

    private fun updateCurrentRows() { exam.tasks.getOrNull(currentTaskIdx)?.questions?.forEach { updateQuestionRow(it.id) } }

    private fun redraw() {
        val c = root ?: return; c.innerHTML = ""
        if (submitted) { player.cancelPlayback(); renderResult(c) }
        else if (exam.tasks.isEmpty()) renderEmpty(c)
        else renderTask(c)
    }

    private fun renderTask(c: HTMLElement) {
        val card = listeningExamCardModel(state)
        val task = card.task
        c.appendChild(node("div", "exam-progress").apply {
            appendChild(node("span", "exam-progress-text", "${UiCopy.Task} ${currentTaskIdx + 1} / ${exam.tasks.size}"))
            appendChild(node("div", "exam-progress-bar").apply {
                appendChild(node("div", "exam-progress-fill").apply {
                    style.setProperty("width", "${((currentTaskIdx + 1).toDouble() / exam.tasks.size * 100).toInt()}%")
                })
            })
        })
        c.appendChild(node("h3", "exam-title", task.instruction))
        task.promptImageUrl?.let { imageUrl ->
            c.appendChild(node("img", "exam-writing-image").apply {
                setAttribute("src", imageUrl)
                setAttribute("alt", requireNotNull(task.promptImageAlt) { "Missing image description for ${task.id}" })
            })
        }
        // Custom audio player — shows only the segment, not full track
        if (exam.audioUrl.isNotBlank()) {
            if (renderedTaskIdx != currentTaskIdx) {
                player.setup(AudioSegment(exam.audioUrl, task.startMs, task.endMs))
                renderedTaskIdx = currentTaskIdx
            }
            c.appendChild(player.buildWidget())
        }
        // Questions
        c.appendChild(node("div", "exam-listening-questions").apply {
            task.questions.forEachIndexed { i, q ->
                appendChild(node("div", "exam-reading-q").apply {
                    setAttribute("data-question-id", q.id)
                    setAttribute("tabindex", "-1")
                    appendChild(node("div", "exam-reading-q-text", "${i + 1}. ${q.questionText}"))
                    when (q.type) {
                        QuestionType.TrueFalse -> appendChild(trueFalseOptions(q))
                        QuestionType.MultipleChoice -> appendChild(mcOptions(q))
                        QuestionType.FillGap -> appendChild(if (q.answerMode == ExamAnswerMode.UnorderedSet) unorderedFillInputs(q) else fillGapInput(q))
                        QuestionType.Match, QuestionType.Matching -> appendChild(mcOptions(q))
                        else -> error("Unsupported listening control ${q.type} for ${task.id}/${q.id}")
                    }
                })
            }
        })
        updateCurrentRows()
        c.appendChild(node("div", "exam-nav").apply {
            if (currentTaskIdx > 0) appendChild(navBtn("← Назад") { cancelFeedbackTimer(); dispatch(ListeningExamAction.Previous); redraw() })
            appendChild(node("span", "exam-nav-spacer"))
            if (currentTaskIdx < exam.tasks.lastIndex) appendChild(navBtn(UiCopy.Next) { cancelFeedbackTimer(); dispatch(ListeningExamAction.Next); redraw() })
            if (currentTaskIdx == exam.tasks.lastIndex) appendChild(navBtn(UiCopy.Finish, true) { cancelFeedbackTimer(); if (onTimedFinish != null) onTimedFinish.invoke() else { dispatch(ListeningExamAction.Submit); redraw() } })
        })
    }

    private fun trueFalseOptions(q: ListeningQ) = node("div", "exam-options").apply {
        q.options.ifEmpty { listOf("PRAWDA", "FAŁSZ") }.forEach { opt ->
            appendChild((document.createElement("button") as HTMLButtonElement).apply {
                type = "button"
                className = if (answers[q.id] == opt) "exam-option selected" else "exam-option"
                textContent = opt
                addEventListener("click", { selectAnswer(q.id, opt) })
            })
        }
    }

    private fun selectAnswer(questionId: String, answer: String) {
        val current = exam.tasks[currentTaskIdx].questions.firstOrNull { it.id == questionId } ?: return
        val selected = if (current.answerMode == ExamAnswerMode.UnorderedSet) {
            val choices = answers[questionId]?.split("|")?.filter { it.isNotBlank() }?.toMutableList() ?: mutableListOf()
            if (answer in choices) choices.remove(answer)
            else if (choices.size < current.answerSlotCount) choices.add(answer)
            choices
        } else mutableListOf(answer)
        answerPractice(questionId, selected.joinToString("|"))
    }

    private fun mcOptions(q: ListeningQ) = node("div", "exam-options").apply {
        q.options.forEach { opt ->
            appendChild((document.createElement("button") as HTMLButtonElement).apply {
                type = "button"
                className = if (answers[q.id]?.split("|")?.contains(opt) == true) "exam-option selected" else "exam-option"
                textContent = opt
                addEventListener("click", { selectAnswer(q.id, opt) })
            })
        }
    }

    private fun fillGapInput(q: ListeningQ) = node("div", "exam-input-wrap").apply {
        appendChild((document.createElement("input") as HTMLInputElement).apply {
            type = "text"; className = "exam-input"; placeholder = "Wpisz odpowiedź..."
            value = answers[q.id].orEmpty()
            addEventListener("input", { onDraftInput(q.id); if (q.id !in feedback.committed || !practiceEnabled()) dispatch(ListeningExamAction.Answer(q.id, this.value)) })
        })
        if (practiceEnabled()) appendChild(navBtn(UiCopy.ExamCheck) {
            val input = querySelector(".exam-input") as? HTMLInputElement
            answerPractice(q.id, input?.value.orEmpty(), PracticeInputEvent.Check)
        }.apply { className += " exam-check-btn" })
    }

    private fun unorderedFillInputs(q: ListeningQ) = node("div", "exam-input-wrap exam-set-inputs").apply {
        val values = setInputValues.getOrPut(q.id) { MutableList(q.answerSlotCount) { "" } }
        repeat(q.answerSlotCount) { index ->
            appendChild((document.createElement("input") as HTMLInputElement).apply {
                type = "text"
                className = "exam-input"
                setAttribute("aria-label", "${q.questionText} ${index + 1}")
                value = values[index]
                addEventListener("input", {
                    onDraftInput(q.id)
                    values[index] = this.value
                    if (q.id !in feedback.committed || !practiceEnabled())
                        dispatch(ListeningExamAction.Answer(q.id, values.filter { it.isNotBlank() }.joinToString("|")))
                })
            })
        }
        if (practiceEnabled()) appendChild(navBtn(UiCopy.ExamCheck) {
            answerPractice(q.id, values.joinToString("|"), PracticeInputEvent.Check)
        }.apply { className += " exam-check-btn" })
    }

    private fun renderResult(c: HTMLElement) {
        val questions = exam.tasks.flatMap { it.questions }
        val reviews = listeningReviewRows(exam, answers)
        val result = requireNotNull(state.result)
        val earnedQuarter = result.confirmedQuarter
        val pendingQuarter = result.manualPendingQuarter
        val totalQuarter = result.possibleQuarter
        c.appendChild(node("h2", "exam-title", "${UiCopy.Results} — ${UiCopy.Listening}"))
        c.appendChild(node("div", "exam-score").apply {
            val percent = result.finalPercent
            if (percent != null) {
                appendChild(node("div", "exam-score-big", "$percent%"))
                appendChild(node("div", "exam-score-detail", "${formatQuarterPoints(earnedQuarter)} / ${formatQuarterPoints(totalQuarter)} баллов"))
            } else {
                appendChild(node("div", "exam-score-big", "${UiCopy.ExamConfirmedPoints}: ${formatQuarterPoints(earnedQuarter)} / ${formatQuarterPoints(totalQuarter)}"))
                if (!result.isFinal) appendChild(node("div", "exam-score-detail", "${UiCopy.ExamPendingPoints}: ${formatQuarterPoints(pendingQuarter)}"))
            }
        })
        c.appendChild(node("div", "exam-review").apply {
            questions.forEachIndexed { i, q ->
                val review = reviews[i]
                val ans = review.chosenAnswer.orEmpty()
                val evaluation = review.evaluation
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
                    if (evaluation.status == ExamAnswerStatus.ConfirmedPartial) appendChild(node("span", "exam-review-correct", "+${formatQuarterPoints(evaluation.confirmedQuarter)} / ${formatQuarterPoints(q.pointsQuarter)}"))
                    if (evaluation.status == ExamAnswerStatus.PendingManual) appendChild(node("span", "exam-review-manual", UiCopy.ExamManualReview))
                    if (evaluation.status != ExamAnswerStatus.ConfirmedFull) {
                        appendChild(node("span", "exam-review-correct", "${UiCopy.ExamOfficialExamples}: ${review.officialExamples.joinToString(" / ")}"))
                    }
                })
            }
        })
        c.appendChild(node("div", "exam-nav").apply {
            appendChild(navBtn("← К модулям") { onBack() })
            appendChild(node("span", "exam-nav-spacer"))
            appendChild(navBtn(UiCopy.TryAgain, true) {
                cancelFeedbackTimer(); feedback = PracticeFeedbackState(); player.dispose(); renderedTaskIdx = -1; ownState = ListeningExamState(exam); setInputValues.clear(); redraw()
            })
        })
    }

    private fun renderEmpty(c: HTMLElement) {
        c.appendChild(node("h2", "exam-title", "Данные аудирования ещё не загружены"))
        c.appendChild(navBtn("← Назад") { onBack() })
    }
}
