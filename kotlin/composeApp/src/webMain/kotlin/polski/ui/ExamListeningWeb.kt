package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLAudioElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import polski.exam.formatQuarterPoints
import polski.exam.QuestionType
import polski.presentation.UiCopy

internal data class ListeningTask(
    val id: String,
    val instruction: String,
    val questions: List<ListeningQ>,
    val startMs: Long = 0,
    val endMs: Long = 0,
    val promptImageUrl: String? = null,
    val promptImageAlt: String? = null,
)
internal data class ListeningQ(val id: String, val type: QuestionType, val questionText: String, val options: List<String>, val correctAnswer: String, val pointsQuarter: Int = 4)
internal data class ListeningExam(val tasks: List<ListeningTask>, val audioUrl: String)

internal class ExamListeningController(examId: String, private val onBack: () -> Unit) {
    private val exam = examListeningData[examId] ?: ListeningExam(emptyList(), "")
    private var currentTaskIdx = 0
    private var answers = mutableMapOf<String, String>()
    private var submitted = false
    private var root: HTMLElement? = null
    private var audioElement: HTMLAudioElement? = null
    private var playButton: HTMLButtonElement? = null
    private var timeLabel: HTMLElement? = null
    private var progressFill: HTMLElement? = null
    private var mediaError: HTMLElement? = null
    private var renderedTaskIdx = -1

    fun render(container: HTMLElement) { root = container; setupAudio(); redraw() }

    /** Releases the single media element when the exam route or module is left. */
    fun close() {
        disposeAudio()
        root = null
    }

    private fun disposeAudio() {
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

    private fun setupAudio() {
        if (audioElement != null || exam.audioUrl.isBlank()) return
        audioElement = (document.createElement("audio") as HTMLAudioElement).apply {
            src = exam.audioUrl; className = "exam-audio-hidden"
            setAttribute("preload", "auto")
            hidden = true
            addEventListener("loadedmetadata", { currentTime = currentSegmentStart; pause() })
            addEventListener("timeupdate", {
                if (currentSegmentEnd > 0 && currentTime >= currentSegmentEnd) {
                    pause(); currentTime = currentSegmentEnd
                }
                updatePlayer()
            })
            addEventListener("ended", { updatePlayer() })
            addEventListener("error", { updatePlayer() })
        }
    }

    private fun seekToTask(task: ListeningTask) {
        val audio = audioElement ?: return
        currentSegmentStart = task.startMs / 1000.0
        currentSegmentEnd = if (task.endMs > 0) task.endMs / 1000.0 else audio.duration
        if (audio.readyState >= 1) { audio.currentTime = currentSegmentStart; audio.pause() }
    }

    private fun updatePlayer() {
        val audio = audioElement ?: return
        val duration = (currentSegmentEnd - currentSegmentStart).coerceAtLeast(0.0)
        val elapsed = (audio.currentTime - currentSegmentStart).coerceIn(0.0, duration)
        progressFill?.style?.setProperty("width", "${if (duration > 0) (elapsed / duration * 100).toInt() else 0}%")
        timeLabel?.textContent = "${formatTime(elapsed)} / ${formatTime(duration)}"
        playButton?.textContent = if (audio.paused) "▶" else "⏸"
        val unavailable = audio.error != null
        playButton?.disabled = unavailable
        mediaError?.textContent = if (unavailable) "Аудиозапись недоступна" else ""
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
            if (audio.paused) {
                if (currentSegmentEnd > 0 && audio.currentTime >= currentSegmentEnd) audio.currentTime = currentSegmentStart
                audio.play()
            } else audio.pause()
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
        if (submitted) { audioElement?.pause(); renderResult(c) }
        else if (exam.tasks.isEmpty()) renderEmpty(c)
        else renderTask(c)
    }

    private fun renderTask(c: HTMLElement) {
        val task = exam.tasks[currentTaskIdx]
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
                        QuestionType.FillGap -> appendChild(fillGapInput(q))
                        else -> appendChild(mcOptions(q))
                    }
                })
            }
        })
        c.appendChild(node("div", "exam-nav").apply {
            if (currentTaskIdx > 0) appendChild(navBtn("← Назад") { currentTaskIdx--; redraw() })
            appendChild(node("span", "exam-nav-spacer"))
            if (currentTaskIdx < exam.tasks.lastIndex) appendChild(navBtn(UiCopy.Next) { currentTaskIdx++; redraw() })
            if (currentTaskIdx == exam.tasks.lastIndex) appendChild(navBtn(UiCopy.Finish, true) { submitted = true; redraw() })
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
        answers[questionId] = answer
        val question = root?.querySelectorAll(".exam-reading-q") ?: return
        for (i in 0 until question.length) {
            val row = question.item(i) as? HTMLElement ?: continue
            val buttons = row.querySelectorAll(".exam-option")
            if (buttons.length == 0) continue
            val matches = exam.tasks[currentTaskIdx].questions.getOrNull(i)?.id == questionId
            if (!matches) continue
            for (j in 0 until buttons.length) {
                val button = buttons.item(j) as? HTMLButtonElement ?: continue
                button.className = if (button.textContent == answer) "exam-option selected" else "exam-option"
            }
            return
        }
    }

    private fun mcOptions(q: ListeningQ) = node("div", "exam-options").apply {
        q.options.forEach { opt ->
            appendChild((document.createElement("button") as HTMLButtonElement).apply {
                type = "button"
                className = if (answers[q.id] == opt) "exam-option selected" else "exam-option"
                textContent = opt
                addEventListener("click", { selectAnswer(q.id, opt) })
            })
        }
    }

    private fun fillGapInput(q: ListeningQ) = node("div", "exam-input-wrap").apply {
        appendChild((document.createElement("input") as HTMLInputElement).apply {
            type = "text"; className = "exam-input"; placeholder = "Wpisz odpowiedź..."
            value = answers[q.id].orEmpty()
            addEventListener("change", { answers[q.id] = this.value })
        })
    }

    private fun renderResult(c: HTMLElement) {
        var earnedQuarter = 0; var totalQuarter = 0
        exam.tasks.flatMap { it.questions }.forEach { q ->
            totalQuarter += q.pointsQuarter
            val ans = answers[q.id]?.trim()?.lowercase().orEmpty()
            val exp = q.correctAnswer.trim().lowercase()
            if (ans == exp || (q.type == QuestionType.TrueFalse && matchTrueFalse(ans, exp))) earnedQuarter += q.pointsQuarter
        }
        val pct = if (totalQuarter > 0) (earnedQuarter.toDouble() / totalQuarter * 100).toInt() else 0
        c.appendChild(node("h2", "exam-title", "${UiCopy.Results} — ${UiCopy.Listening}"))
        c.appendChild(node("div", "exam-score").apply {
            appendChild(node("div", "exam-score-big", "$pct%"))
            appendChild(node("div", "exam-score-detail", "${formatQuarterPoints(earnedQuarter)} / ${formatQuarterPoints(totalQuarter)} баллов"))
        })
        c.appendChild(node("div", "exam-review").apply {
            exam.tasks.flatMap { it.questions }.forEachIndexed { i, q ->
                val ans = answers[q.id].orEmpty()
                val ok = ans.trim().lowercase() == q.correctAnswer.trim().lowercase() ||
                    (q.type == QuestionType.TrueFalse && matchTrueFalse(ans.trim().lowercase(), q.correctAnswer.trim().lowercase()))
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
            appendChild(navBtn(UiCopy.TryAgain, true) {
                disposeAudio(); answers.clear(); currentTaskIdx = 0; submitted = false; setupAudio(); redraw()
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

private fun matchTrueFalse(ans: String, exp: String): Boolean {
    val t = setOf("prawda", "tak", "true", "p", "t")
    val f = setOf("fałsz", "nie", "false", "f", "n")
    return (ans in t && exp in t) || (ans in f && exp in f)
}
