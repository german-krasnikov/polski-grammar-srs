package polski.audio

import polski.exam.listening.ListeningTask

data class AudioSegment(
    val audioUrl: String,
    val startMs: Long,
    val endMs: Long,
)

fun ListeningTask.toAudioSegment(audioUrl: String): AudioSegment =
    AudioSegment(audioUrl, startMs, endMs)
