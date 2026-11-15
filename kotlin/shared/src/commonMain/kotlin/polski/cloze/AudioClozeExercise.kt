package polski.cloze

data class AudioClozeExercise(
    val id: String,
    val displaySentence: String,
    val correctForm: String,
    val options: List<String>,
    val audioUrl: String,
    val startMs: Long,
    val endMs: Long,
    val source: AudioClozeSource,
    val gapLexemeKey: String,
) {
    init {
        require(correctForm in options)
        require(options.size == 3)
        require(options.distinct().size == 3)
    }
}

sealed interface AudioClozeSource {
    data class ListeningQuestion(val examRouteId: String, val taskId: String, val questionId: String) : AudioClozeSource
    data class ListeningTranscript(val examRouteId: String, val taskId: String) : AudioClozeSource
    data class Custom(val sourceId: String, val label: String) : AudioClozeSource
}
