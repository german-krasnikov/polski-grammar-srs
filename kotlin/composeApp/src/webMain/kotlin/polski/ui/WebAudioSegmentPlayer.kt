package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLAudioElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent
import polski.audio.AudioSegment

/**
 * Reusable audio segment player widget for web. Owns one [HTMLAudioElement] (kept in
 * `document.body`, hidden) and the visible controls returned by [buildWidget].
 *
 * Typical lifecycle:
 *  1. [setup] — creates or reuses the audio element, seeks to the segment start.
 *  2. [buildWidget] — returns the player DOM (call after [setup], once per render pass).
 *  3. [dispose] — call when the host route is left.
 */
internal class WebAudioSegmentPlayer {
    private var audioElement: HTMLAudioElement? = null
    private var currentUrl: String? = null
    private var playButton: HTMLButtonElement? = null
    private var timeLabel: HTMLElement? = null
    private var progressFill: HTMLElement? = null
    private var mediaError: HTMLElement? = null
    private var currentSegmentStart = 0.0
    private var currentSegmentEnd = 0.0
    private var playRequested = false
    private var playRequestId = 0
    private var playInFlight = false
    private var replaySeekPending = false
    private var playbackError: String? = null
    var onSegmentEnded: (() -> Unit)? = null

    /** Creates (or reuses) the audio element for [segment]. Disposes any element with a
     *  different URL. Seeks to the segment start. */
    fun setup(segment: AudioSegment) {
        if (currentUrl != segment.audioUrl || audioElement == null) {
            dispose()
            currentUrl = segment.audioUrl
            val audio = (document.createElement("audio") as HTMLAudioElement).apply {
                src = segment.audioUrl
                className = "exam-audio-hidden"
                setAttribute("preload", "auto")
                hidden = true
                addEventListener("loadedmetadata", {
                    // Always seek to segment start on load — handles the case where seekTo()
                    // was called before metadata was available (readyState < 1).
                    // The seeked event fires next and starts playback if playRequested.
                    currentTime = currentSegmentStart
                    updateWidget()
                })
                addEventListener("seeked", {
                    if (replaySeekPending) replaySeekPending = false
                    if (playRequested) startPlayback(this@apply)
                    updateWidget()
                })
                addEventListener("seeking", { updateWidget() })
                addEventListener("play", { updateWidget() })
                addEventListener("pause", { updateWidget() })
                addEventListener("timeupdate", {
                    if (!replaySeekPending && currentSegmentEnd > 0 && currentTime >= currentSegmentEnd) {
                        cancelPlayback()
                        pause()
                        currentTime = currentSegmentEnd
                        updateWidget()
                        onSegmentEnded?.invoke()
                        return@addEventListener
                    }
                    updateWidget()
                })
                addEventListener("ended", { updateWidget() })
                addEventListener("error", { updateWidget() })
            }
            document.body?.appendChild(audio)
            audioElement = audio
        }
        seekTo(segment)
    }

    /** Updates segment bounds and seeks to [segment] start; cancels any in-progress playback. */
    fun seekTo(segment: AudioSegment) {
        val audio = audioElement ?: return
        cancelPlayback()
        audio.pause()
        playbackError = null
        currentSegmentStart = segment.startMs / 1000.0
        currentSegmentEnd = if (segment.endMs > 0) segment.endMs / 1000.0 else audio.duration
        if (audio.readyState >= 1) audio.currentTime = currentSegmentStart
    }

    /** Builds and returns the visible player controls. Must call [setup] first. */
    fun buildWidget(): HTMLElement {
        val audio = audioElement ?: return node("div")
        val segDuration = currentSegmentEnd - currentSegmentStart
        val container = node("div", "exam-custom-player")
        val playBtn = (document.createElement("button") as HTMLButtonElement).apply {
            type = "button"; className = "player-play-btn"
            textContent = if (audio.paused) "▶" else "⏸"
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
            updateWidget()
        })
        progressWrap.addEventListener("click", { e ->
            val rect = progressBar.getBoundingClientRect()
            val pct = ((e as MouseEvent).clientX - rect.left) / rect.width
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
        updateWidget()
        return container
    }

    /** Starts playback programmatically (e.g. auto-play on card load). */
    fun startPlaybackIfRequested() {
        val audio = audioElement ?: return
        playRequested = true
        playRequestId++
        playbackError = null
        playInFlight = false
        if (audio.readyState >= 1) {
            if (currentSegmentEnd > 0 && audio.currentTime >= currentSegmentEnd) {
                replaySeekPending = true
                audio.currentTime = currentSegmentStart
            } else startPlayback(audio)
        }
        // If readyState < 1: loadedmetadata fires → seeks to start → seeked → startPlayback
        updateWidget()
    }

    /** Cancels any pending play intent without disposing the element. */
    fun cancelPlayback() {
        playRequested = false
        replaySeekPending = false
        playInFlight = false
        playRequestId++
    }

    /** Disposes the audio element and resets all state. */
    fun dispose() {
        cancelPlayback()
        audioElement?.let { audio ->
            audio.pause()
            audio.removeAttribute("src")
            audio.load()
            audio.remove()
        }
        audioElement = null
        currentUrl = null
        playButton = null
        timeLabel = null
        progressFill = null
        mediaError = null
    }

    private fun startPlayback(audio: HTMLAudioElement) {
        if (!playRequested || playInFlight || audio.seeking || audioElement !== audio) return
        val requestId = playRequestId
        playInFlight = true
        audio.play().then<JsAny?>({
            if (requestId == playRequestId) {
                playInFlight = false
                updateWidget()
            }
            null
        }, {
            if (requestId == playRequestId && playRequested) {
                playInFlight = false
                playRequested = false
                playbackError = "Не удалось воспроизвести аудиозапись"
                updateWidget()
            }
            null
        })
    }

    private fun updateWidget() {
        val audio = audioElement ?: return
        val duration = (currentSegmentEnd - currentSegmentStart).coerceAtLeast(0.0)
        val elapsed = (audio.currentTime - currentSegmentStart).coerceIn(0.0, duration)
        progressFill?.style?.setProperty("width", "${if (duration > 0) (elapsed / duration * 100).toInt() else 0}%")
        timeLabel?.textContent = "${formatTime(elapsed)} / ${formatTime(duration)}"
        playButton?.textContent = if (audio.paused) "▶" else "⏸"
        val unavailable = audio.error != null
        playButton?.disabled = unavailable || audio.readyState < 1
        mediaError?.textContent = if (unavailable) "Аудиозапись недоступна" else playbackError.orEmpty()
    }

    private fun formatTime(sec: Double): String {
        val m = (sec / 60).toInt(); val s = (sec % 60).toInt()
        return "$m:${s.toString().padStart(2, '0')}"
    }
}
