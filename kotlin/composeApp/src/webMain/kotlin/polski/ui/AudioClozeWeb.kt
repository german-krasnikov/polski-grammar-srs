package polski.ui

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import polski.audio.AudioSegment
import polski.cloze.AudioClozeExercise
import polski.cloze.AudioClozeSessionAction
import polski.cloze.AudioClozeSessionState
import polski.exam.officialListeningData
import polski.presentation.AppAction
import polski.presentation.AppUiState
import polski.presentation.LoadStatus

private const val AUDIO_CLOZE_FEEDBACK_DELAY_MS = 1_500

/** DOM controller for the Audio Cloze tab, shaped like [ClozeWebController]. */
internal class AudioClozeWebController {
    private var root: HTMLElement? = null
    private val player = WebAudioSegmentPlayer()
    private var feedbackTimer: Int? = null
    private var lastExerciseId: String? = null
    private val onVisibilityChange: (Event) -> Unit = {
        if (browserDocumentHidden()) { cancelTimer(); player.cancelPlayback() }
    }

    fun render(container: HTMLElement, state: AudioClozeSessionState, dispatch: (AppAction) -> Unit) {
        root = container
        document.removeEventListener("visibilitychange", onVisibilityChange)
        document.addEventListener("visibilitychange", onVisibilityChange)
        redraw(state, dispatch)
    }

    fun close() {
        cancelTimer()
        document.removeEventListener("visibilitychange", onVisibilityChange)
        player.dispose()
        root = null
        lastExerciseId = null
    }

    private fun cancelTimer() {
        feedbackTimer?.let(window::clearTimeout)
        feedbackTimer = null
    }

    private fun redraw(state: AudioClozeSessionState, dispatch: (AppAction) -> Unit) {
        val c = root ?: return
        c.innerHTML = ""
        if (state.exercises.isEmpty()) {
            c.appendChild(node("p", "notice", "Нет доступных заданий."))
            return
        }
        renderExercise(c, state, dispatch)
    }

    private fun renderExercise(c: HTMLElement, state: AudioClozeSessionState, dispatch: (AppAction) -> Unit) {
        val exercise = state.exercises.getOrNull(state.currentIndex) ?: return
        val answered = state.answers[exercise.id]
        val segment = AudioSegment(exercise.audioUrl, exercise.startMs, exercise.endMs)

        val isNewExercise = exercise.id != lastExerciseId
        if (isNewExercise) {
            lastExerciseId = exercise.id
            player.setup(segment)
        }

        // Progress
        c.appendChild(node("div", "exam-progress").apply {
            appendChild(node("span", "exam-progress-text", "${state.currentIndex + 1} / ${state.exercises.size}"))
            appendChild(node("div", "exam-progress-bar").apply {
                appendChild(node("div", "exam-progress-fill").apply {
                    style.setProperty("width", "${((state.currentIndex + 1).toDouble() / state.exercises.size * 100).toInt()}%")
                })
            })
        })

        // Audio player
        c.appendChild(player.buildWidget())

        // Replay after wrong answer
        if (answered != null && answered != exercise.correctForm) {
            c.appendChild(navBtn("Переслушать") {
                dispatch(AppAction.AudioClozeAct(AudioClozeSessionAction.RecordReplay(exercise.id)))
                player.seekTo(segment)
                player.startPlaybackIfRequested()
            })
        }

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

        // Options (A/B/C)
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

        // Feedback row
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
                appendChild(navBtn("← Назад") { dispatch(AppAction.AudioClozeAct(AudioClozeSessionAction.Previous)) })
            }
            appendChild(node("span", "exam-nav-spacer"))
            when {
                state.currentIndex < state.exercises.lastIndex ->
                    appendChild(navBtn("Далее →") { dispatch(AppAction.AudioClozeAct(AudioClozeSessionAction.Next)) })
                else ->
                    appendChild(navBtn("Заново", primary = true) { dispatch(AppAction.AudioClozeAct(AudioClozeSessionAction.Restart)) })
            }
        })

        // Auto-play on new card
        if (isNewExercise) player.startPlaybackIfRequested()
    }

    private fun onChoice(
        exercise: AudioClozeExercise,
        chosen: String,
        state: AudioClozeSessionState,
        dispatch: (AppAction) -> Unit,
    ) {
        dispatch(AppAction.AudioClozeAct(AudioClozeSessionAction.Choose(exercise.id, chosen)))
        // Auto-advance only after correct answer, and only when not on the last item
        if (chosen == exercise.correctForm && state.currentIndex < state.exercises.lastIndex) {
            cancelTimer()
            feedbackTimer = window.setTimeout({
                feedbackTimer = null
                dispatch(AppAction.AudioClozeAct(AudioClozeSessionAction.Next))
                null
            }, AUDIO_CLOZE_FEEDBACK_DELAY_MS)
        }
    }

    private fun buildOptionClass(opt: String, answered: String?, exercise: AudioClozeExercise): String {
        if (answered == null) return "exam-option"
        val isChosen = opt == answered
        val isCorrect = opt == exercise.correctForm
        return "exam-option" +
            (if (isChosen && !isCorrect) " feedback-wrong" else "") +
            (if (isCorrect) " feedback-correct" else "") +
            " locked"
    }
}

/** Renders the audio cloze route content — shows an exam picker when session is null. */
internal fun renderAudioClozeWeb(
    container: HTMLElement,
    state: AppUiState,
    audioCloze: AudioClozeWebController,
    dispatch: (AppAction) -> Unit,
) {
    val session = state.audioCloze
    if (session == null) {
        if (state.loadStatus != LoadStatus.Ready) {
            container.appendChild(node("p", text = "Загрузка…"))
        } else {
            renderExamPicker(container, dispatch)
        }
        return
    }
    audioCloze.render(container, session, dispatch)
}

private fun renderExamPicker(container: HTMLElement, dispatch: (AppAction) -> Unit) {
    container.appendChild(node("h2", "exam-title", "Аудирование: заполни пропуск"))
    container.appendChild(node("p", "notice", "Слушайте аудио и заполняйте пропуски в тексте"))

    container.appendChild(navBtn("Все экзамены", primary = true) {
        dispatch(AppAction.StartAudioCloze())
    })

    val examsWithTranscripts = officialListeningData
        .filter { (_, exam) -> exam.tasks.any { it.transcript != null } }
        .keys.sorted()
    if (examsWithTranscripts.isEmpty()) return

    val grouped = examsWithTranscripts.groupBy { routeId ->
        when {
            routeId.startsWith("b1") -> "B1"
            routeId.startsWith("b2") -> "B2"
            routeId.startsWith("c1") -> "C1"
            else -> "Другие"
        }
    }
    for ((level, routes) in grouped) {
        container.appendChild(node("h3", "exam-title", level))
        container.appendChild(node("div", "exam-options").apply {
            routes.forEach { routeId ->
                appendChild(navBtn(routeId) {
                    dispatch(AppAction.StartAudioCloze(examRouteId = routeId))
                })
            }
        })
    }
}
