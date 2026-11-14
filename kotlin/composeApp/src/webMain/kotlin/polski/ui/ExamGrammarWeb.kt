package polski.ui

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.Event
import polski.exam.PracticeFeedbackState
import polski.exam.acceptGrammarPracticeInput
import polski.exam.PracticeInputEvent
import polski.exam.grammarReviewRows
import polski.exam.clearPracticeFeedback
import polski.exam.advancePracticeFocus
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import polski.presentation.UiCopy
import org.w3c.dom.HTMLInputElement
import polski.exam.QuestionType
import polski.exam.ExamAnswerStatus
import polski.exam.formatQuarterPoints
import polski.exam.grammar.*
import polski.grammar.normalize

internal class ExamGrammarController(
    examId: String, private val onBack: () -> Unit,
    private val externalState: (() -> GrammarExamState)? = null,
    private val onAction: ((GrammarExamAction) -> Unit)? = null,
    private val onTimedFinish: (() -> Unit)? = null,
    private val immediateFeedbackEnabled: () -> Boolean = { true },
    /** M10 blueprint: opens straight onto this exam item (the post-correction verification
     *  question), instead of the first — `null`/unknown falls back to the first item exactly as
     *  before. */
    startAtItemId: String? = null,
    /** M10 blueprint AC2: a review row whose item is wrong (status != ConfirmedFull) and has a
     *  linked skill (`polski.exam.grammar.skillIdFor`) gets a "Потренировать навык" affordance —
     *  invoked with (examId, itemId, skillId); `null` (every pre-M10 call site) hides it entirely. */
    private val onTrainSkill: ((itemId: String, skillId: String) -> Unit)? = null,
) {
    private val tests = examGrammarData.getValue(examId)
    private val instructionsByItemId = tests.flatMap { test ->
        test.items.map { item -> item.itemId to test.instruction }
    }.toMap()
    private val bankByItemId = tests.flatMap { test ->
        test.items.map { item -> item.itemId to test.bankTokens }
    }.toMap()
    private val selectedBankTokenByItemId = mutableMapOf<String, String>()
    private var ownState = run {
        val merged = mergeGrammarTests(examId, tests)
        val startIndex = startAtItemId?.let { id -> merged.items.indexOfFirst { it.itemId == id } }?.takeIf { it >= 0 } ?: 0
        GrammarExamState(test = merged, currentIndex = startIndex)
    }
    private val state get() = externalState?.invoke() ?: ownState
    private var root: HTMLElement? = null
    private var inputRef: HTMLInputElement? = null
    private var feedback = PracticeFeedbackState()
    private var autoAdvanceTimer: Int? = null
    private val onVisibilityChange: (Event) -> Unit = {
        cancelFeedbackTimer()
        updateInPlace()
    }

    private fun practiceFeedbackEnabled(): Boolean = onTimedFinish == null && externalState == null && immediateFeedbackEnabled()

    private fun cancelFeedbackTimer() {
        autoAdvanceTimer?.let(window::clearTimeout)
        autoAdvanceTimer = null
        feedback = clearPracticeFeedback(feedback)
    }

    private fun answerPractice(itemId: String, answer: String, event: PracticeInputEvent = PracticeInputEvent.Choice,
                               tokenId: String? = null) {
        if (!practiceFeedbackEnabled()) {
            if (tokenId != null) selectedBankTokenByItemId[itemId] = tokenId
            dispatch(GrammarExamAction.Answer(itemId, answer))
            return
        }
        val next = acceptGrammarPracticeInput(feedback, state.test, itemId, answer, enabled = true,
            existingAnswers = state.answers, event = event, selectedTokenId = tokenId)
        if (next === feedback) return
        feedback = next
        if (tokenId != null) selectedBankTokenByItemId[itemId] = tokenId
        dispatch(GrammarExamAction.Answer(itemId, answer))
        updateInPlace()
        val view = feedback.visible ?: return
        if (!view.mayAutoAdvance || state.currentIndex >= state.test.items.lastIndex) return
        val generation = view.generation
        autoAdvanceTimer = window.setTimeout({
            autoAdvanceTimer = null
            if (root != null && feedback.generation == generation &&
                state.test.items.getOrNull(state.currentIndex)?.itemId == itemId) {
                feedback = advancePracticeFocus(feedback, state.test.items.map { it.itemId })
                val targetId = feedback.focusedQuestionId
                while (state.currentIndex < state.test.items.lastIndex &&
                    state.test.items[state.currentIndex].itemId != targetId) {
                    dispatch(GrammarExamAction.Next)
                }
            }
            null
        }, 2_000)
    }

    fun render(container: HTMLElement) {
        root = container
        document.addEventListener("visibilitychange", onVisibilityChange)
        redraw()
    }
    fun close() {
        cancelFeedbackTimer()
        document.removeEventListener("visibilitychange", onVisibilityChange)
        root = null
    }

    private fun dispatch(action: GrammarExamAction) {
        if (action == GrammarExamAction.Next || action == GrammarExamAction.Previous || action == GrammarExamAction.Submit) cancelFeedbackTimer()
        val oldIndex = state.currentIndex
        if (onAction != null) onAction.invoke(action) else ownState = reduceGrammarExam(ownState, action)
        if (state.submitted && state.result != null) { root?.let { it.innerHTML = ""; renderResult(it) }; return }
        if (state.currentIndex != oldIndex) { redraw(); return }
        // Same question — update without full redraw to keep focus
        updateInPlace()
    }

    private fun updateInPlace() {
        val c = root ?: return
        val card = grammarExamCardModel(state)
        c.querySelector(".exam-feedback")?.remove()
        c.querySelector(".exam-choice-locked")?.remove()
        val committedChoice = if (practiceFeedbackEnabled()) feedback.committed[card.item.itemId] else null
        if (committedChoice != null && feedback.visible?.itemId != card.item.itemId) {
            val note = node("p", "exam-choice-locked", "${UiCopy.ExamAnswerLocked} · ${UiCopy.ExamYourAnswer}: ${committedChoice.rawAnswer}")
            note.setAttribute("role", "note")
            c.querySelector(".exam-nav")?.let { c.insertBefore(note, it) }
        }
        feedback.visible?.takeIf { it.itemId == card.item.itemId && practiceFeedbackEnabled() }?.let { view ->
            val label = when (view.evaluation.status) {
                ExamAnswerStatus.Wrong -> UiCopy.ExamFeedbackWrong
                ExamAnswerStatus.PendingManual -> UiCopy.ExamManualReview
                ExamAnswerStatus.ConfirmedPartial -> UiCopy.ExamFeedbackPartial
                else -> UiCopy.ExamFeedbackCorrect
            }
            val notice = node("div", "exam-feedback", "$label · ${UiCopy.ExamYourAnswer}: ${view.chosen} · ${UiCopy.ExamOfficialExamples}: ${view.officialExamples.joinToString(" / ")}")
            notice.setAttribute("role", "status")
            notice.setAttribute("aria-live", "polite")
            c.querySelector(".exam-nav")?.let { c.insertBefore(notice, it) }
        }
        // Update progress
        c.querySelector(".exam-progress-text")?.textContent = card.progress
        (c.querySelector(".exam-progress-fill") as? HTMLElement)?.style?.setProperty(
            "width", "${((state.currentIndex + 1).toDouble() / state.test.items.size * 100).toInt()}%"
        )
        // Update option buttons selection
        c.querySelectorAll(".exam-option").let { nodes ->
            for (i in 0 until nodes.length) {
                val btn = nodes.item(i) as? HTMLButtonElement ?: continue
                val bankId = btn.getAttribute("data-bank-token-id")
                val isSelected = if (bankId != null) committedChoice?.selectedTokenId == bankId || selectedBankTokenByItemId[card.item.itemId] == bankId
                    else card.item.options.getOrNull(i)?.let { it == card.currentAnswer } == true
                val view = feedback.visible?.takeIf { it.itemId == card.item.itemId && practiceFeedbackEnabled() }
                val option = card.item.options.getOrNull(i)
                val optionText = if (bankId != null) btn.textContent else option
                val wrong = view?.evaluation?.status == ExamAnswerStatus.Wrong &&
                    (if (bankId != null) bankId == committedChoice?.selectedTokenId else optionText == view.chosen)
                val correct = view != null && view.evaluation.status != ExamAnswerStatus.PendingManual &&
                    optionText?.let { normalize(it) == normalize(card.item.correctAnswer) } == true && !wrong
                btn.className = "exam-option" + (if (isSelected) " selected" else "") +
                    (if (wrong) " feedback-wrong" else if (correct) " feedback-correct" else "") +
                    (if (committedChoice != null && view == null) " locked" else "")
                btn.disabled = committedChoice != null
                if (committedChoice != null) btn.setAttribute("aria-disabled", "true") else btn.removeAttribute("aria-disabled")
            }
        }
        (c.querySelector("#exam-grammar-input") as? HTMLInputElement)?.let { input ->
            input.disabled = committedChoice != null
            if (committedChoice != null) input.setAttribute("aria-disabled", "true") else input.removeAttribute("aria-disabled")
            input.className = "exam-input" + when (feedback.visible?.takeIf { it.itemId == card.item.itemId }?.evaluation?.status) {
                ExamAnswerStatus.Wrong -> " feedback-wrong"
                ExamAnswerStatus.ConfirmedFull, ExamAnswerStatus.ConfirmedPartial -> " feedback-correct"
                else -> ""
            }
        }
        (c.querySelector(".exam-check-btn") as? HTMLButtonElement)?.let { it.disabled = committedChoice != null }
    }

    private fun redraw() {
        val c = root ?: return; c.innerHTML = ""; inputRef = null
        if (state.submitted && state.result != null) renderResult(c) else renderQuestion(c)
    }

    private fun renderQuestion(c: HTMLElement) {
        val card = grammarExamCardModel(state)
        c.appendChild(node("h2", "exam-title", instructionsByItemId[card.item.itemId] ?: card.instruction))
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
        card.item.questionTarget?.let { target ->
            c.appendChild(node("mark", "exam-question-target", target).apply { setAttribute("lang", "pl") })
        }
        when (card.item.questionType) {
        QuestionType.WordBank -> {
            val bank = bankByItemId.getValue(card.item.itemId)
            require(bank.isNotEmpty()) { "Missing bank for ${card.item.itemId}" }
            c.appendChild(node("div", "exam-options exam-word-bank").apply {
                bank.forEach { token ->
                    val occurrence = bank.count { it.position <= token.position && it.text == token.text }
                    appendChild((document.createElement("button") as HTMLButtonElement).apply {
                        type = "button"
                        className = if (selectedBankTokenByItemId[card.item.itemId] == token.tokenId) "exam-option selected" else "exam-option"
                        textContent = token.text
                        setAttribute("data-bank-token-id", token.tokenId)
                        setAttribute("aria-label", "${token.text}, $occurrence")
                        val committed = if (practiceFeedbackEnabled()) feedback.committed[card.item.itemId] else null
                        disabled = committed != null
                        if (committed != null) setAttribute("aria-disabled", "true")
                        addEventListener("click", { answerPractice(card.item.itemId, token.text, tokenId = token.tokenId) })
                    })
                }
            })
        }
        QuestionType.ChooseForm -> {
            require(card.item.options.isNotEmpty()) { "Missing options for ${card.item.itemId}" }
            val committedChoice = if (practiceFeedbackEnabled()) feedback.committed[card.item.itemId] else null
            c.appendChild(node("div", "exam-options").apply {
                card.item.options.forEachIndexed { i, opt ->
                    appendChild((document.createElement("button") as HTMLButtonElement).apply {
                        type = "button"
                        className = "exam-option" + (if (card.currentAnswer == opt) " selected" else "") +
                            (if (committedChoice != null) " locked" else "")
                        textContent = "${'A' + i}. $opt"
                        disabled = committedChoice != null
                        if (committedChoice != null) setAttribute("aria-disabled", "true")
                        addEventListener("click", { answerPractice(card.item.itemId, opt) })
                    })
                }
            })
        }
        QuestionType.FillGap, QuestionType.Transform, QuestionType.WordFormation, QuestionType.QuestionFormation -> {
            val committed = if (practiceFeedbackEnabled()) feedback.committed[card.item.itemId] else null
            val input = (document.createElement("input") as HTMLInputElement).apply {
                type = "text"; id = "exam-grammar-input"; className = "exam-input"; placeholder = "Wpisz odpowiedź..."
                value = card.currentAnswer
                disabled = committed != null
                if (committed != null) setAttribute("aria-disabled", "true")
                addEventListener("input", { dispatch(GrammarExamAction.Answer(card.item.itemId, this.value)) })
            }
            inputRef = input
            c.appendChild(node("div", "exam-input-wrap").apply {
                appendChild(input)
                if (practiceFeedbackEnabled()) appendChild(navBtn(UiCopy.ExamCheck) {
                    answerPractice(card.item.itemId, input.value, PracticeInputEvent.Check)
                }.apply {
                    className += " exam-check-btn"
                    disabled = committed != null
                })
            })
        }
        else -> error("Unsupported grammar control ${card.item.questionType} for ${card.item.itemId}")
        }
        if (practiceFeedbackEnabled() && feedback.visible?.itemId != card.item.itemId) {
            feedback.committed[card.item.itemId]?.let { committed ->
                c.appendChild(node("p", "exam-choice-locked", "${UiCopy.ExamAnswerLocked} · ${UiCopy.ExamYourAnswer}: ${committed.rawAnswer}").apply {
                    setAttribute("role", "note")
                })
            }
        }
        feedback.visible?.takeIf { it.itemId == card.item.itemId && practiceFeedbackEnabled() }?.let { view ->
            val label = when (view.evaluation.status) {
                ExamAnswerStatus.Wrong -> UiCopy.ExamFeedbackWrong
                ExamAnswerStatus.PendingManual -> UiCopy.ExamManualReview
                ExamAnswerStatus.ConfirmedPartial -> UiCopy.ExamFeedbackPartial
                else -> UiCopy.ExamFeedbackCorrect
            }
            c.appendChild(node("div", "exam-feedback", "$label · ${UiCopy.ExamYourAnswer}: ${view.chosen} · ${UiCopy.ExamOfficialExamples}: ${view.officialExamples.joinToString(" / ")}").apply {
                setAttribute("role", "status")
                setAttribute("aria-live", "polite")
            })
        }
        c.appendChild(node("div", "exam-nav").apply {
            if (!card.isFirst) appendChild(navBtn("← Назад") { dispatch(GrammarExamAction.Previous) })
            appendChild(node("span", "exam-nav-spacer"))
            if (!card.isLast) appendChild(navBtn(UiCopy.Next) { dispatch(GrammarExamAction.Next) })
            if (card.isLast) appendChild(navBtn(UiCopy.Finish, primary = true) { if (onTimedFinish != null) onTimedFinish.invoke() else dispatch(GrammarExamAction.Submit) })
        })
    }

    private fun renderResult(c: HTMLElement) {
        val result = state.result ?: return
        c.appendChild(node("h2", "exam-title", "${UiCopy.Results} — ${UiCopy.Grammar}"))
        c.appendChild(node("div", "exam-score").apply {
            if (result.isFinal) {
                appendChild(node("div", "exam-score-big", "${result.percentage.toInt()}%"))
                appendChild(node("div", "exam-score-detail", "${formatQuarterPoints(result.earnedQuarterPoints)} / ${formatQuarterPoints(result.possibleQuarterPoints)} баллов"))
            } else {
                appendChild(node("div", "exam-score-big", "${UiCopy.ExamConfirmedPoints}: ${formatQuarterPoints(result.earnedQuarterPoints)} / ${formatQuarterPoints(result.possibleQuarterPoints)}"))
                appendChild(node("div", "exam-score-detail", "${UiCopy.ExamPendingPoints}: ${formatQuarterPoints(result.manualPendingQuarterPoints)}"))
            }
        })
        val reviews = grammarReviewRows(state.test, state.answers)
        c.appendChild(node("div", "exam-review").apply {
            state.test.items.forEachIndexed { i, item ->
                val review = reviews[i]
                val answer = review.chosenAnswer.orEmpty()
                val evaluation = review.evaluation
                val status = when (evaluation.status) {
                    ExamAnswerStatus.ConfirmedFull -> "correct"
                    ExamAnswerStatus.ConfirmedPartial -> "partial"
                    ExamAnswerStatus.Wrong -> "wrong"
                    ExamAnswerStatus.PendingManual -> "pending"
                }
                appendChild(node("div", "exam-review-item $status").apply {
                    appendChild(node("span", "exam-review-num", "${i + 1}."))
                    appendChild(node("span", "exam-review-context", item.context.replace("___", if (answer.isNotEmpty()) "[$answer]" else "[—]")))
                    appendChild(node("span", "exam-review-chosen", "${UiCopy.ExamYourAnswer}: ${answer.ifBlank { UiCopy.NoAnswerEntered }}"))
                    if (evaluation.status == ExamAnswerStatus.ConfirmedPartial) appendChild(node("span", "exam-review-correct", "+${formatQuarterPoints(evaluation.confirmedQuarter)} / ${formatQuarterPoints(item.maximumQuarter)}"))
                    if (evaluation.status == ExamAnswerStatus.PendingManual) appendChild(node("span", "exam-review-manual", UiCopy.ExamManualReview))
                    if (evaluation.status != ExamAnswerStatus.ConfirmedFull) {
                        val examples = review.officialExamples.joinToString(" / ")
                        appendChild(node("span", "exam-review-correct", "${UiCopy.ExamOfficialExamples}: $examples"))
                    }
                    if (item.rubricComponents.isNotEmpty()) {
                        appendChild(node("ol", "exam-review-rubric").apply {
                            item.rubricComponents.sortedBy { it.order }.forEach { component ->
                                appendChild(node("li", "exam-review-rubric-component",
                                    "${component.criterionText} · ${formatQuarterPoints(component.creditQuarter)}").apply {
                                    setAttribute("lang", "pl")
                                })
                            }
                        })
                    }
                    // M10 blueprint AC2: only for a wrong+linked item, never for a correct or
                    // unlinked one.
                    val linkedSkillId = skillIdFor(item.itemId)
                    if (onTrainSkill != null && evaluation.status != ExamAnswerStatus.ConfirmedFull && linkedSkillId != null) {
                        appendChild((document.createElement("button") as HTMLButtonElement).apply {
                            type = "button"; className = "exam-nav-btn exam-train-skill-btn"
                            textContent = "Потренировать навык"
                            addEventListener("click", { onTrainSkill(item.itemId, linkedSkillId) })
                        })
                    }
                })
            }
        })
        c.appendChild(node("div", "exam-nav").apply {
            appendChild(navBtn("← К модулям") { onBack() })
            appendChild(node("span", "exam-nav-spacer"))
            appendChild(navBtn(UiCopy.TryAgain, primary = true) {
                cancelFeedbackTimer(); feedback = PracticeFeedbackState(); ownState = GrammarExamState(test = state.test); selectedBankTokenByItemId.clear(); inputRef = null; redraw()
            })
        })
    }
}
