package polski.ui

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import polski.cloze.ErrorHuntAction
import polski.cloze.ErrorHuntExercise
import polski.cloze.ErrorHuntSessionState
import polski.presentation.AppAction
import polski.presentation.AppUiState
import polski.presentation.UiCopy

private const val ERROR_HUNT_FEEDBACK_DELAY_MS = 1_500

/** DOM controller for the Error Hunt sub-mode, shaped like [ClozeWebController]. */
internal class ErrorHuntWebController {
    private var root: HTMLElement? = null
    private var feedbackTimer: Int? = null

    fun render(container: HTMLElement, state: ErrorHuntSessionState, dispatch: (AppAction) -> Unit) {
        root = container
        redraw(state, dispatch)
    }

    fun close() {
        cancelTimer()
        root = null
    }

    private fun cancelTimer() {
        feedbackTimer?.let(window::clearTimeout)
        feedbackTimer = null
    }

    private fun redraw(state: ErrorHuntSessionState, dispatch: (AppAction) -> Unit) {
        val c = root ?: return
        c.innerHTML = ""
        if (state.exercises.isEmpty()) {
            c.appendChild(node("p", "notice", UiCopy.ErrorHuntNoTasks))
            return
        }
        val ex = state.exercises.getOrNull(state.currentIndex) ?: return
        if (state.currentIndex >= state.exercises.lastIndex && state.answers.containsKey(ex.id)) {
            renderSummary(c, state, dispatch)
            return
        }
        renderExercise(c, ex, state, dispatch)
    }

    private fun renderExercise(
        c: HTMLElement,
        ex: ErrorHuntExercise,
        state: ErrorHuntSessionState,
        dispatch: (AppAction) -> Unit,
    ) {
        val answered = state.answers[ex.id]
        val found = ex.id in state.found
        val revealed = state.shouldReveal(ex.id)
        val missCount = state.missCounts[ex.id] ?: 0

        // Progress
        c.appendChild(node("div", "exam-progress").apply {
            appendChild(node("span", "exam-progress-text", "${state.currentIndex + 1} / ${state.exercises.size}"))
            appendChild(node("div", "exam-progress-bar").apply {
                appendChild(node("div", "exam-progress-fill").apply {
                    style.setProperty("width",
                        "${((state.currentIndex + 1).toDouble() / state.exercises.size * 100).toInt()}%")
                })
            })
        })

        // Prompt
        val promptText = when {
            found || revealed -> UiCopy.ErrorHuntFound
            else -> UiCopy.ErrorHuntPrompt
        }
        c.appendChild(node("p", "eh-prompt", promptText))

        // Sentence tokens
        c.appendChild(node("div", "eh-sentence").apply {
            setAttribute("lang", "pl")
            setAttribute("aria-live", "polite")
            ex.tokens.forEachIndexed { index, token ->
                val isError = index == ex.errorTokenIndex
                val tokenClass = when {
                    isError && (found || revealed) && answered == null -> "eh-token eh-token-error"
                    isError && (found || revealed) && answered != null -> "eh-token eh-token-error locked"
                    answered != null -> "eh-token locked"
                    else -> "eh-token"
                }
                appendChild((document.createElement("button") as HTMLButtonElement).apply {
                    type = "button"
                    className = tokenClass
                    textContent = token
                    setAttribute("data-index", index.toString())
                    if (answered != null) {
                        disabled = true
                        setAttribute("aria-disabled", "true")
                    }
                    addEventListener("click", {
                        if (answered == null) dispatch(AppAction.ErrorHuntAct(ErrorHuntAction.TapToken(ex.id, index)))
                    })
                })
            }
        })

        // "This word is correct" note (non-blocking, shown after a miss before reveal/found)
        if (missCount > 0 && !found && !revealed) {
            c.appendChild(node("div", "exam-feedback feedback-wrong", UiCopy.ErrorHuntWordCorrect).apply {
                setAttribute("role", "status")
                setAttribute("aria-live", "polite")
            })
        }

        // Options (after found or revealed, while not yet answered)
        if ((found || revealed) && answered == null) {
            c.appendChild(node("div", "exam-options").apply {
                ex.options.forEachIndexed { i, opt ->
                    appendChild((document.createElement("button") as HTMLButtonElement).apply {
                        type = "button"
                        className = "exam-option"
                        textContent = "${'A' + i}. $opt"
                        addEventListener("click", { onChoose(ex, opt, state, dispatch) })
                    })
                }
            })
        }

        // Post-answer feedback + locked options + nav
        if (answered != null) {
            val correct = answered == ex.correctForm
            val feedbackClass = if (correct) "exam-feedback feedback-correct" else "exam-feedback feedback-wrong"
            val feedbackText = if (correct) "${UiCopy.ExamFeedbackCorrect}! ${ex.correctForm}"
                else "${UiCopy.ExamFeedbackWrong} — ${UiCopy.ErrorHuntCorrectAnswer}: ${ex.correctForm}"
            c.appendChild(node("div", feedbackClass, feedbackText).apply {
                setAttribute("role", "status")
                setAttribute("aria-live", "polite")
            })
            ex.ruleText?.let { rule ->
                c.appendChild(node("div", "eh-rule", rule))
            }
            c.appendChild(node("div", "exam-options").apply {
                ex.options.forEachIndexed { i, opt ->
                    val isChosen = opt == answered
                    val isCorrect = opt == ex.correctForm
                    val cls = "exam-option locked" +
                        (if (isChosen && !isCorrect) " feedback-wrong" else "") +
                        (if (isCorrect) " feedback-correct" else "")
                    appendChild((document.createElement("button") as HTMLButtonElement).apply {
                        type = "button"
                        className = cls
                        textContent = "${'A' + i}. $opt"
                        disabled = true
                        setAttribute("aria-disabled", "true")
                    })
                }
            })
            c.appendChild(node("div", "exam-nav").apply {
                appendChild(node("span", "exam-nav-spacer"))
                appendChild(navBtn(UiCopy.Next) {
                    cancelTimer()
                    dispatch(AppAction.ErrorHuntAct(ErrorHuntAction.Next))
                })
            })
        }
    }

    private fun onChoose(
        ex: ErrorHuntExercise,
        chosen: String,
        state: ErrorHuntSessionState,
        dispatch: (AppAction) -> Unit,
    ) {
        dispatch(AppAction.ErrorHuntAct(ErrorHuntAction.Choose(ex.id, chosen)))
        // Auto-advance after correct answer, only on non-last exercises
        if (chosen == ex.correctForm && state.currentIndex < state.exercises.lastIndex) {
            cancelTimer()
            feedbackTimer = window.setTimeout({
                feedbackTimer = null
                dispatch(AppAction.ErrorHuntAct(ErrorHuntAction.Next))
                null
            }, ERROR_HUNT_FEEDBACK_DELAY_MS)
        }
    }

    private fun renderSummary(c: HTMLElement, state: ErrorHuntSessionState, dispatch: (AppAction) -> Unit) {
        val summary = state.summary()
        c.appendChild(node("h2", "exam-title", UiCopy.ErrorHuntSummary))
        c.appendChild(node("div", "eh-summary").apply {
            val labels = listOf(
                "perfect" to UiCopy.ErrorHuntScorePerfect,
                "found_late" to UiCopy.ErrorHuntScoreFoundLate,
                "revealed_correct" to UiCopy.ErrorHuntScoreRevealedCorrect,
                "wrong_form" to UiCopy.ErrorHuntScoreWrongForm,
            )
            for ((key, label) in labels) {
                val count = summary[key] ?: 0
                appendChild(node("div", "eh-summary-row", "$label: $count"))
            }
        })
        c.appendChild(navBtn(UiCopy.ErrorHuntRestart, primary = true) {
            dispatch(AppAction.ErrorHuntAct(ErrorHuntAction.Restart))
        })
    }
}

/** Renders the Error Hunt view. Auto-dispatches [AppAction.StartErrorHunt] when session is null. */
internal fun renderErrorHuntWeb(
    container: HTMLElement,
    state: AppUiState,
    errorHunt: ErrorHuntWebController,
    dispatch: (AppAction) -> Unit,
) {
    val session = state.errorHunt
    if (session == null) {
        dispatch(AppAction.StartErrorHunt)
        container.appendChild(node("p", text = UiCopy.Loading))
        return
    }
    errorHunt.render(container, session, dispatch)
}
