package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLAudioElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import polski.exam.QuestionType

internal data class ListeningTask(val id: String, val instruction: String, val questions: List<ListeningQ>, val startMs: Long = 0, val endMs: Long = 0)
internal data class ListeningQ(val id: String, val type: QuestionType, val questionText: String, val options: List<String>, val correctAnswer: String)
internal data class ListeningExam(val tasks: List<ListeningTask>, val audioUrl: String)

internal class ExamListeningController(examId: String, private val onBack: () -> Unit) {
    private val exam = examListeningData[examId] ?: ListeningExam(emptyList(), "")
    private var currentTaskIdx = 0
    private var answers = mutableMapOf<String, String>()
    private var submitted = false
    private var root: HTMLElement? = null
    private var audioElement: HTMLAudioElement? = null
    private var questionsContainer: HTMLElement? = null

    fun render(container: HTMLElement) { root = container; setupAudio(); redraw() }

    private var currentSegmentStart = 0.0
    private var currentSegmentEnd = 0.0

    private fun setupAudio() {
        if (audioElement != null || exam.audioUrl.isBlank()) return
        audioElement = (document.createElement("audio") as HTMLAudioElement).apply {
            src = exam.audioUrl; className = "exam-audio-hidden"
            setAttribute("preload", "auto")
        }
    }

    private fun seekToTask(task: ListeningTask) {
        val audio = audioElement ?: return
        currentSegmentStart = task.startMs / 1000.0
        currentSegmentEnd = if (task.endMs > 0) task.endMs / 1000.0 else audio.duration
        fun doSeek() { audio.currentTime = currentSegmentStart; audio.pause() }
        if (audio.readyState >= 1) doSeek()
        else audio.addEventListener("loadedmetadata", { doSeek() })
        audio.ontimeupdate = {
            if (currentSegmentEnd > 0 && audio.currentTime >= currentSegmentEnd) {
                audio.pause(); audio.currentTime = currentSegmentEnd
            }
        }
    }

    private fun buildCustomPlayer(task: ListeningTask): HTMLElement {
        val audio = audioElement ?: return node("div")
        val segDuration = currentSegmentEnd - currentSegmentStart
        val container = node("div", "exam-custom-player")
        val playBtn = (document.createElement("button") as HTMLButtonElement).apply {
            type = "button"; className = "player-play-btn"; textContent = "▶"
        }
        val timeLabel = node("span", "player-time", "0:00 / ${formatTime(segDuration)}")
        val progressWrap = node("div", "player-progress-wrap")
        val progressBar = node("div", "player-progress-bar")
        val progressFill = node("div", "player-progress-fill")
        progressBar.appendChild(progressFill)
        progressWrap.appendChild(progressBar)

        playBtn.addEventListener("click", {
            if (audio.paused) { audio.play(); playBtn.textContent = "⏸" }
            else { audio.pause(); playBtn.textContent = "▶" }
        })
        progressWrap.addEventListener("click", { e ->
            val rect = progressBar.getBoundingClientRect()
            val pct = ((e as org.w3c.dom.events.MouseEvent).clientX - rect.left) / rect.width
            audio.currentTime = currentSegmentStart + pct * segDuration
        })
        audio.addEventListener("timeupdate", {
            val elapsed = (audio.currentTime - currentSegmentStart).coerceIn(0.0, segDuration)
            val pct = if (segDuration > 0) (elapsed / segDuration * 100) else 0.0
            progressFill.style.setProperty("width", "${pct.toInt()}%")
            timeLabel.textContent = "${formatTime(elapsed)} / ${formatTime(segDuration)}"
            if (audio.paused) playBtn.textContent = "▶"
        })
        audio.addEventListener("ended", { playBtn.textContent = "▶" })

        container.appendChild(playBtn)
        container.appendChild(progressWrap)
        container.appendChild(timeLabel)
        return container
    }

    private fun formatTime(sec: Double): String {
        val m = (sec / 60).toInt(); val s = (sec % 60).toInt()
        return "$m:${s.toString().padStart(2, '0')}"
    }

    private fun redraw() {
        val c = root ?: return; c.innerHTML = ""
        if (submitted) { audioElement?.pause(); renderResult(c) }
        else if (exam.tasks.isEmpty()) renderEmpty(c)
        else renderTask(c)
    }

    private fun renderTask(c: HTMLElement) {
        val task = exam.tasks[currentTaskIdx]
        c.appendChild(node("div", "exam-progress").apply {
            appendChild(node("span", "exam-progress-text", "Задание ${currentTaskIdx + 1} / ${exam.tasks.size}"))
            appendChild(node("div", "exam-progress-bar").apply {
                appendChild(node("div", "exam-progress-fill").apply {
                    style.setProperty("width", "${((currentTaskIdx + 1).toDouble() / exam.tasks.size * 100).toInt()}%")
                })
            })
        })
        c.appendChild(node("h3", "exam-title", task.instruction))
        // Custom audio player — shows only the segment, not full track
        if (audioElement != null) {
            seekToTask(task)
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
            if (currentTaskIdx < exam.tasks.lastIndex) appendChild(navBtn("Далее →") { currentTaskIdx++; redraw() })
            if (currentTaskIdx == exam.tasks.lastIndex) appendChild(navBtn("Завершить", true) { submitted = true; redraw() })
        })
    }

    private fun trueFalseOptions(q: ListeningQ) = node("div", "exam-options").apply {
        listOf("PRAWDA", "FAŁSZ").forEach { opt ->
            appendChild((document.createElement("button") as HTMLButtonElement).apply {
                type = "button"
                className = if (answers[q.id] == opt) "exam-option selected" else "exam-option"
                textContent = opt
                addEventListener("click", { answers[q.id] = opt; redraw() })
            })
        }
    }

    private fun mcOptions(q: ListeningQ) = node("div", "exam-options").apply {
        q.options.forEach { opt ->
            appendChild((document.createElement("button") as HTMLButtonElement).apply {
                type = "button"
                className = if (answers[q.id] == opt) "exam-option selected" else "exam-option"
                textContent = opt
                addEventListener("click", { answers[q.id] = opt; redraw() })
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
        var correct = 0; var total = 0
        exam.tasks.flatMap { it.questions }.forEach { q ->
            total++
            val ans = answers[q.id]?.trim()?.lowercase().orEmpty()
            val exp = q.correctAnswer.trim().lowercase()
            if (ans == exp || (q.type == QuestionType.TrueFalse && matchTrueFalse(ans, exp))) correct++
        }
        val pct = if (total > 0) (correct.toDouble() / total * 100).toInt() else 0
        c.appendChild(node("h2", "exam-title", "Результаты — Аудирование"))
        c.appendChild(node("div", "exam-score").apply {
            appendChild(node("div", "exam-score-big", "$pct%"))
            appendChild(node("div", "exam-score-detail", "$correct / $total"))
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
            appendChild(navBtn("Пройти ещё раз", true) {
                answers.clear(); currentTaskIdx = 0; submitted = false; audioElement = null; redraw()
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
