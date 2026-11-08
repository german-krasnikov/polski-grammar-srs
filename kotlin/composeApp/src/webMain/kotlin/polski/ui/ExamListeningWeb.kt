package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLAudioElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import polski.exam.listening.*
import polski.exam.formatQuarterPoints
import polski.exam.QuestionType
import polski.exam.ExamAnswerMode
import polski.exam.ExamAnswerStatus
import polski.presentation.UiCopy

internal class ExamListeningController(
    examId: String, private val onBack: () -> Unit,
    private val externalState: (() -> ListeningExamState)? = null,
    private val onAction: ((ListeningExamAction) -> Unit)? = null,
    private val onTimedFinish: (() -> Unit)? = null,
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
    private var audioElement: HTMLAudioElement? = null
    private var playButton: HTMLButtonElement? = null
    private var timeLabel: HTMLElement? = null
    private var progressFill: HTMLElement? = null
    private var mediaError: HTMLElement? = null
    private var renderedTaskIdx = -1
    private var playRequested = false
    private var playRequestId = 0
    private var playInFlight = false
    private var replaySeekPending = false
    private var playbackError: String? = null

    fun render(container: HTMLElement) { root = container; setupAudio(); redraw() }

    /** Releases the single media element when the exam route or module is left. */
    fun close() {
        disposeAudio()
        root = null
    }

    private fun disposeAudio() {
        cancelPlayback()
        audioElement?.let { audio ->
            audio.pause()
            audio.removeAttribute("src")
            audio.load()
            audio.remove()
        }
        audioElement = null
        playButton = null
        timeLabel = null
        progressFill = null
        mediaError = null
        renderedTaskIdx = -1
    }

    private var currentSegmentStart = 0.0
    private var currentSegmentEnd = 0.0

    private fun cancelPlayback() {
        playRequested = false
        replaySeekPending = false
        playInFlight = false
        playRequestId++
    }

    private fun startPlayback(audio: HTMLAudioElement) {
        if (!playRequested || playInFlight || audio.seeking || audioElement !== audio) return
        val requestId = playRequestId
        playInFlight = true
        audio.play().then<JsAny?>({
            if (requestId == playRequestId) {
                playInFlight = false
                updatePlayer()
            }
            null
        }, {
            if (requestId == playRequestId && playRequested) {
                playInFlight = false
                playRequested = false
                playbackError = "Не удалось воспроизвести аудиозапись"
                updatePlayer()
            }
            null
        })
    }

    private fun setupAudio() {
        if (audioElement != null || exam.audioUrl.isBlank()) return
        audioElement = (document.createElement("audio") as HTMLAudioElement).apply {
            src = exam.audioUrl; className = "exam-audio-hidden"
            setAttribute("preload", "auto")
            hidden = true
            addEventListener("loadedmetadata", {
                if (!playRequested) currentTime = currentSegmentStart
                updatePlayer()
            })
            addEventListener("seeked", {
                if (replaySeekPending) replaySeekPending = false
                if (playRequested) startPlayback(this)
                updatePlayer()
            })
            addEventListener("seeking", { updatePlayer() })
            addEventListener("play", { updatePlayer() })
            addEventListener("pause", { updatePlayer() })
            addEventListener("timeupdate", {
                if (!replaySeekPending && currentSegmentEnd > 0 && currentTime >= currentSegmentEnd) {
                    cancelPlayback()
                    pause()
                    currentTime = currentSegmentEnd
                }
                updatePlayer()
            })
            addEventListener("ended", { updatePlayer() })
            addEventListener("error", { updatePlayer() })
        }
    }

    private fun seekToTask(task: ListeningTask) {
        val audio = audioElement ?: return
        cancelPlayback()
        audio.pause()
        playbackError = null
        currentSegmentStart = task.startMs / 1000.0
        currentSegmentEnd = if (task.endMs > 0) task.endMs / 1000.0 else audio.duration
        if (audio.readyState >= 1) audio.currentTime = currentSegmentStart
    }

    private fun updatePlayer() {
        val audio = audioElement ?: return
        val duration = (currentSegmentEnd - currentSegmentStart).coerceAtLeast(0.0)
        val elapsed = (audio.currentTime - currentSegmentStart).coerceIn(0.0, duration)
        progressFill?.style?.setProperty("width", "${if (duration > 0) (elapsed / duration * 100).toInt() else 0}%")
        timeLabel?.textContent = "${formatTime(elapsed)} / ${formatTime(duration)}"
        playButton?.textContent = if (audio.paused) "▶" else "⏸"
        val unavailable = audio.error != null
        // A click during a seek records play intent; seeked starts playback. Some engines
        // report seeking during the last UI event at the segment end, so disabling here can
        // strand an otherwise ready replay button after the seek has completed.
        playButton?.disabled = unavailable || audio.readyState < 1
        mediaError?.textContent = if (unavailable) "Аудиозапись недоступна" else playbackError.orEmpty()
    }

    private fun buildCustomPlayer(task: ListeningTask): HTMLElement {
        val audio = audioElement ?: return node("div")
        val segDuration = currentSegmentEnd - currentSegmentStart
        val container = node("div", "exam-custom-player")
        val playBtn = (document.createElement("button") as HTMLButtonElement).apply {
            type = "button"; className = "player-play-btn"; textContent = if (audio.paused) "▶" else "⏸"
        }
        val playerTime = node("span", "player-time", "0:00 / ${formatTime(segDuration)}")
        val error = node("span", "player-error").apply { setAttribute("role", "status") }
        val progressWrap = node("div", "player-progress-wrap")
        val progressBar = node("div", "player-progress-bar")
        val fill = node("div", "player-progress-fill")
        progressBar.appendChild(fill)
        progressWrap.appendChild(progressBar)

        playBtn.addEventListener("click", {
            if (playRequested || !audio.paused) {
                cancelPlayback()
                audio.pause()
            } else {
                playRequested = true
                playRequestId++
                playbackError = null
                if (currentSegmentEnd > 0 && audio.currentTime >= currentSegmentEnd) {
                    replaySeekPending = true
                    audio.currentTime = currentSegmentStart
                } else startPlayback(audio)
            }
            updatePlayer()
        })
        progressWrap.addEventListener("click", { e ->
            val rect = progressBar.getBoundingClientRect()
            val pct = ((e as org.w3c.dom.events.MouseEvent).clientX - rect.left) / rect.width
            audio.currentTime = currentSegmentStart + pct * segDuration
        })
        container.appendChild(playBtn)
        container.appendChild(progressWrap)
        container.appendChild(playerTime)
        container.appendChild(error)
        playButton = playBtn
        timeLabel = playerTime
        progressFill = fill
        mediaError = error
        updatePlayer()
        return container
    }

    private fun formatTime(sec: Double): String {
        val m = (sec / 60).toInt(); val s = (sec % 60).toInt()
        return "$m:${s.toString().padStart(2, '0')}"
    }

    private fun redraw() {
        val c = root ?: return; c.innerHTML = ""
        audioElement?.let { c.appendChild(it) }
        if (submitted) { cancelPlayback(); audioElement?.pause(); renderResult(c) }
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
        if (audioElement != null) {
            if (renderedTaskIdx != currentTaskIdx) {
                seekToTask(task)
                renderedTaskIdx = currentTaskIdx
            }
            c.appendChild(buildCustomPlayer(task))
        }
        // Questions
        c.appendChild(node("div", "exam-listening-questions").apply {
            task.questions.forEachIndexed { i, q ->
                appendChild(node("div", "exam-reading-q").apply {
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
        c.appendChild(node("div", "exam-nav").apply {
            if (currentTaskIdx > 0) appendChild(navBtn("← Назад") { dispatch(ListeningExamAction.Previous); redraw() })
            appendChild(node("span", "exam-nav-spacer"))
            if (currentTaskIdx < exam.tasks.lastIndex) appendChild(navBtn(UiCopy.Next) { dispatch(ListeningExamAction.Next); redraw() })
            if (currentTaskIdx == exam.tasks.lastIndex) appendChild(navBtn(UiCopy.Finish, true) { if (onTimedFinish != null) onTimedFinish.invoke() else { dispatch(ListeningExamAction.Submit); redraw() } })
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
        dispatch(ListeningExamAction.Answer(questionId, selected.joinToString("|")))
        val question = root?.querySelectorAll(".exam-reading-q") ?: return
        for (i in 0 until question.length) {
            val row = question.item(i) as? HTMLElement ?: continue
            val buttons = row.querySelectorAll(".exam-option")
            if (buttons.length == 0) continue
            val matches = exam.tasks[currentTaskIdx].questions.getOrNull(i)?.id == questionId
            if (!matches) continue
            for (j in 0 until buttons.length) {
                val button = buttons.item(j) as? HTMLButtonElement ?: continue
                button.className = if (button.textContent in selected) "exam-option selected" else "exam-option"
            }
            return
        }
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
            addEventListener("input", { dispatch(ListeningExamAction.Answer(q.id, this.value)) })
        })
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
                    values[index] = this.value
                    dispatch(ListeningExamAction.Answer(q.id, values.filter { it.isNotBlank() }.joinToString("|")))
                })
            })
        }
    }

    private fun renderResult(c: HTMLElement) {
        val questions = exam.tasks.flatMap { it.questions }
        val result = requireNotNull(state.result)
        val evaluations = questions.map { result.evaluations.getValue(it.id) }
        val earnedQuarter = result.confirmedQuarter
        val pendingQuarter = result.manualPendingQuarter
        val totalQuarter = result.possibleQuarter
        c.appendChild(node("h2", "exam-title", "${UiCopy.Results} — ${UiCopy.Listening}"))
        c.appendChild(node("div", "exam-score").apply {
            if (pendingQuarter == 0) {
                appendChild(node("div", "exam-score-big", "${requireNotNull(result.finalPercent)}%"))
                appendChild(node("div", "exam-score-detail", "${formatQuarterPoints(earnedQuarter)} / ${formatQuarterPoints(totalQuarter)} баллов"))
            } else {
                appendChild(node("div", "exam-score-big", "${UiCopy.ExamConfirmedPoints}: ${formatQuarterPoints(earnedQuarter)} / ${formatQuarterPoints(totalQuarter)}"))
                appendChild(node("div", "exam-score-detail", "${UiCopy.ExamPendingPoints}: ${formatQuarterPoints(pendingQuarter)}"))
            }
        })
        c.appendChild(node("div", "exam-review").apply {
            questions.forEachIndexed { i, q ->
                val ans = answers[q.id].orEmpty()
                val evaluation = evaluations[i]
                val status = when (evaluation.status) {
                    ExamAnswerStatus.ConfirmedFull -> "correct"
                    ExamAnswerStatus.ConfirmedPartial -> "partial"
                    ExamAnswerStatus.Wrong -> "wrong"
                    ExamAnswerStatus.PendingManual -> "pending"
                }
                appendChild(node("div", "exam-review-item $status").apply {
                    appendChild(node("span", "exam-review-num", "${i + 1}."))
                    appendChild(node("span", "exam-review-context", "${q.questionText}: ${ans.ifEmpty { "[—]" }}"))
                    if (evaluation.status == ExamAnswerStatus.ConfirmedPartial) appendChild(node("span", "exam-review-correct", "+${formatQuarterPoints(evaluation.confirmedQuarter)} / ${formatQuarterPoints(q.pointsQuarter)}"))
                    if (evaluation.status == ExamAnswerStatus.PendingManual) appendChild(node("span", "exam-review-manual", UiCopy.ExamManualReview))
                    if (evaluation.status != ExamAnswerStatus.ConfirmedFull) {
                        val examples = (listOf(q.correctAnswer) + q.scoredVariants.map { it.answer }).joinToString(" / ")
                        appendChild(node("span", "exam-review-correct", "${UiCopy.ExamOfficialExamples}: $examples"))
                    }
                })
            }
        })
        c.appendChild(node("div", "exam-nav").apply {
            appendChild(navBtn("← К модулям") { onBack() })
            appendChild(node("span", "exam-nav-spacer"))
            appendChild(navBtn(UiCopy.TryAgain, true) {
                disposeAudio(); ownState = ListeningExamState(exam); setInputValues.clear(); setupAudio(); redraw()
            })
        })
    }

    private fun renderEmpty(c: HTMLElement) {
        c.appendChild(node("h2", "exam-title", "Данные аудирования ещё не загружены"))
        c.appendChild(navBtn("← Назад") { onBack() })
    }

    private fun navBtn(label: String, primary: Boolean = false, onClick: () -> Unit) =
        (document.createElement("button") as HTMLButtonElement).apply {
            type = "button"; className = if (primary) "exam-nav-btn primary" else "exam-nav-btn"
            textContent = label; addEventListener("click", { onClick() })
        }
}
