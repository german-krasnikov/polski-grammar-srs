package polski.ui

import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import polski.exam.ExamAnswerStatus
import polski.exam.PracticeFeedbackState
import polski.exam.formatQuarterPoints
import polski.presentation.UiCopy

/** Read actual browser visibility on both JS and Wasm; event listeners invalidate pending generations. */
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
internal fun browserDocumentHidden(): Boolean = js("document.hidden")

/** Update one grouped question without replacing its siblings or the Listening audio node. */
internal fun updateGroupedFeedbackRow(
    row: HTMLElement, questionId: String, rawAnswer: String, correctAnswer: String,
    maximumQuarter: Int, feedback: PracticeFeedbackState, enabled: Boolean,
) {
    row.querySelector(".exam-feedback")?.remove()
    row.querySelector(".exam-choice-locked")?.remove()
    val committed = if (enabled) feedback.committed[questionId] else null
    val view = if (enabled) feedback.visible?.takeIf { it.itemId == questionId } else null
    val chosenParts = rawAnswer.split("|").filter { it.isNotBlank() }.toSet()
    val correctParts = correctAnswer.split("|").toSet()
    val buttons = row.querySelectorAll(".exam-option")
    for (index in 0 until buttons.length) {
        val button = buttons.item(index) as? HTMLButtonElement ?: continue
        val option = button.textContent.orEmpty()
        val selected = option in chosenParts
        val settled = view != null && view.evaluation.status != ExamAnswerStatus.PendingManual
        val wrong = view?.evaluation?.status == ExamAnswerStatus.Wrong && selected && option !in correctParts
        val correct = settled && option in correctParts
        val partial = view?.evaluation?.status == ExamAnswerStatus.ConfirmedPartial && selected && option !in correctParts
        button.className = "exam-option" + (if (selected) " selected" else "") +
            (if (wrong) " feedback-wrong" else if (correct) " feedback-correct" else if (partial) " feedback-partial" else "") +
            (if (committed != null && view == null) " locked" else "")
        button.disabled = committed != null
        if (committed != null) button.setAttribute("aria-disabled", "true") else button.removeAttribute("aria-disabled")
    }
    val inputs = row.querySelectorAll(".exam-input")
    for (index in 0 until inputs.length) {
        val input = inputs.item(index) as? HTMLInputElement ?: continue
        input.disabled = committed != null
        if (committed != null) input.setAttribute("aria-disabled", "true") else input.removeAttribute("aria-disabled")
        input.className = "exam-input" + when (view?.evaluation?.status) {
            ExamAnswerStatus.Wrong -> " feedback-wrong"
            ExamAnswerStatus.ConfirmedFull, ExamAnswerStatus.ConfirmedPartial -> " feedback-correct"
            else -> ""
        }
    }
    (row.querySelector(".exam-check-btn") as? HTMLButtonElement)?.disabled = committed != null
    if (committed != null && view == null) {
        row.appendChild(node("p", "exam-choice-locked", "${UiCopy.ExamAnswerLocked} · ${UiCopy.ExamYourAnswer}: ${committed.rawAnswer}").apply {
            setAttribute("role", "note")
        })
    }
    if (view != null) {
        val label = when (view.evaluation.status) {
            ExamAnswerStatus.Wrong -> UiCopy.ExamFeedbackWrong
            ExamAnswerStatus.PendingManual -> UiCopy.ExamManualReview
            ExamAnswerStatus.ConfirmedPartial -> UiCopy.ExamFeedbackPartial
            ExamAnswerStatus.ConfirmedFull -> UiCopy.ExamFeedbackCorrect
        }
        val credit = if (view.evaluation.status == ExamAnswerStatus.ConfirmedPartial)
            " · +${formatQuarterPoints(view.evaluation.confirmedQuarter)} / ${formatQuarterPoints(maximumQuarter)}" else ""
        row.appendChild(node("div", "exam-feedback", "$label$credit · ${UiCopy.ExamYourAnswer}: ${view.chosen} · ${UiCopy.ExamOfficialExamples}: ${view.officialExamples.joinToString(" / ")}").apply {
            setAttribute("role", "status")
            setAttribute("aria-live", "polite")
        })
    }
}

internal fun focusGroupedQuestion(root: HTMLElement?, questionId: String) {
    val rows = root?.querySelectorAll(".exam-reading-q") ?: return
    for (index in 0 until rows.length) {
        val row = rows.item(index) as? HTMLElement ?: continue
        val focused = row.getAttribute("data-question-id") == questionId
        row.classList.toggle("practice-focused", focused)
        if (focused) {
            row.focus()
            row.scrollIntoView()
        }
    }
}
