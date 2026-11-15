package polski.cloze

data class AudioClozeSessionState(
    val exercises: List<AudioClozeExercise>,
    val currentIndex: Int = 0,
    val answers: Map<String, String> = emptyMap(),
    val replayCount: Map<String, Int> = emptyMap(),
)

sealed interface AudioClozeSessionAction {
    data class Choose(val id: String, val form: String) : AudioClozeSessionAction
    data class RecordReplay(val id: String) : AudioClozeSessionAction
    data object Next : AudioClozeSessionAction
    data object Previous : AudioClozeSessionAction
    data object Restart : AudioClozeSessionAction
}

fun reduceAudioClozeSession(
    state: AudioClozeSessionState,
    action: AudioClozeSessionAction,
): AudioClozeSessionState = when (action) {
    is AudioClozeSessionAction.Choose ->
        if (state.answers.containsKey(action.id)) state
        else state.copy(answers = state.answers + (action.id to action.form))
    is AudioClozeSessionAction.RecordReplay ->
        state.copy(replayCount = state.replayCount + (action.id to (state.replayCount[action.id] ?: 0) + 1))
    AudioClozeSessionAction.Next ->
        if (state.currentIndex >= state.exercises.lastIndex) state
        else state.copy(currentIndex = state.currentIndex + 1)
    AudioClozeSessionAction.Previous ->
        if (state.currentIndex <= 0) state
        else state.copy(currentIndex = state.currentIndex - 1)
    AudioClozeSessionAction.Restart -> state.copy(currentIndex = 0, answers = emptyMap(), replayCount = emptyMap())
}
