package polski.ui

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import polski.cloze.ClozeExercise
import polski.cloze.ClozeSessionAction
import polski.cloze.ClozeSessionState
import polski.presentation.AppAction

private const val CLOZE_FEEDBACK_DELAY_MS = 1_500

/** DOM controller for the Подстановка (cloze) tab, shaped like [ExamGrammarController]. */
internal class ClozeWebController {
    private var root: HTMLElement? = null
    private var feedbackTimer: Int? = null

    fun render(container: HTMLElement, state: ClozeSessionState, dispatch: (AppAction) -> Unit) {
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

    private fun redraw(state: ClozeSessionState, dispatch: (AppAction) -> Unit) {
        val c = root ?: return
        c.innerHTML = ""
        if (state.exercises.isEmpty()) {
            c.appendChild(node("p", "notice", "Нет доступных заданий."))
            return
        }
        renderQuestion(c, state, dispatch)
    }

    private fun renderQuestion(c: HTMLElement, state: ClozeSessionState, dispatch: (AppAction) -> Unit) {
        val exercise = state.exercises.getOrNull(state.currentIndex) ?: return
        val answered = state.answers[exercise.id]

        c.appendChild(node("h2", "exam-title", "Подстановка"))
        c.appendChild(node("div", "exam-progress").apply {
            appendChild(node("span", "exam-progress-text", "${state.currentIndex + 1} / ${state.exercises.size}"))
            appendChild(node("div", "exam-progress-bar").apply {
                appendChild(node("div", "exam-progress-fill").apply {
                    style.setProperty("width", "${((state.currentIndex + 1).toDouble() / state.exercises.size * 100).toInt()}%")
                })
            })
        })

        // Sentence with gap
        c.appendChild(node("div", "exam-context").apply {
            setAttribute("lang", "pl")
            val parts = exercise.displaySentence.split("___")
            if (parts.size == 2) {
                appendChild(node("span", text = parts[0]))
                appendChild(node("span", "exam-gap", "___"))
                appendChild(node("span", text = parts[1]))
            } else textContent = exercise.displaySentence
        })

        // Options
        c.appendChild(node("div", "exam-options").apply {
            exercise.options.forEachIndexed { i, opt ->
                appendChild((document.createElement("button") as HTMLButtonElement).apply {
                    type = "button"
                    className = buildOptionClass(opt, answered, exercise)
                    textContent = "${'A' + i}. $opt"
                    disabled = answered != null
                    if (answered != null) setAttribute("aria-disabled", "true")
                    addEventListener("click", {
                        if (answered == null) onChoice(exercise, opt, state, dispatch)
                    })
                })
            }
        })

        // Feedback row (after answer)
        if (answered != null) {
            val correct = answered == exercise.correctForm
            val label = if (correct) "Верно!" else "Неверно — правильный ответ: ${exercise.correctForm}"
            c.appendChild(node("div", if (correct) "exam-feedback feedback-correct" else "exam-feedback feedback-wrong", label).apply {
                setAttribute("role", "status")
                setAttribute("aria-live", "polite")
            })
        }

        // Navigation
        c.appendChild(node("div", "exam-nav").apply {
            if (state.currentIndex > 0) {
                appendChild(navBtn("← Назад") { dispatch(AppAction.ClozeAct(ClozeSessionAction.Previous)) })
            }
            appendChild(node("span", "exam-nav-spacer"))
            if (state.currentIndex < state.exercises.lastIndex) {
                appendChild(navBtn("Далее →") { dispatch(AppAction.ClozeAct(ClozeSessionAction.Next)) })
            } else {
                appendChild(navBtn("Заново", primary = true) { dispatch(AppAction.ClozeAct(ClozeSessionAction.Restart)) })
            }
        })
    }

    private fun onChoice(
        exercise: ClozeExercise,
        chosen: String,
        state: ClozeSessionState,
        dispatch: (AppAction) -> Unit,
    ) {
        dispatch(AppAction.ClozeAct(ClozeSessionAction.Choose(exercise.id, chosen)))
        // Auto-advance after feedback unless on last item
        if (state.currentIndex < state.exercises.lastIndex) {
            cancelTimer()
            feedbackTimer = window.setTimeout({
                feedbackTimer = null
                dispatch(AppAction.ClozeAct(ClozeSessionAction.Next))
            }, CLOZE_FEEDBACK_DELAY_MS)
        }
    }

    private fun buildOptionClass(opt: String, answered: String?, exercise: ClozeExercise): String {
        if (answered == null) return "exam-option"
        val isChosen = opt == answered
        val isCorrect = opt == exercise.correctForm
        return "exam-option" +
            (if (isChosen && !isCorrect) " feedback-wrong" else "") +
            (if (isCorrect) " feedback-correct" else "") +
            " locked"
    }
}

/** Renders the cloze route content — auto-dispatches StartCloze when cloze is null. */
internal fun renderClozeWeb(
    container: HTMLElement,
    state: polski.presentation.AppUiState,
    cloze: ClozeWebController,
    dispatch: (AppAction) -> Unit,
) {
    val session = state.cloze
    if (session == null) {
        dispatch(AppAction.StartCloze)
        container.appendChild(node("p", text = "Загрузка…"))
        return
    }
    cloze.render(container, session, dispatch)
}
