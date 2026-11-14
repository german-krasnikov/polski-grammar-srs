package polski.cloze

data class ClozeSessionState(
    val exercises: List<ClozeExercise>,
    val currentIndex: Int = 0,
    val answers: Map<String, String> = emptyMap(),  // exerciseId -> chosen form
)

sealed interface ClozeSessionAction {
    data class Choose(val id: String, val form: String) : ClozeSessionAction
    data object Next : ClozeSessionAction
    data object Previous : ClozeSessionAction
    data object Restart : ClozeSessionAction
}

/** Pure reducer — no side effects. Choose before the gap is answered records it; a second Choose
 *  on the same id is ignored. Next/Previous guard against out-of-bounds. Restart zeroes index and answers. */
fun reduceClozeSession(state: ClozeSessionState, action: ClozeSessionAction): ClozeSessionState =
    when (action) {
        is ClozeSessionAction.Choose ->
            if (state.answers.containsKey(action.id)) state
            else state.copy(answers = state.answers + (action.id to action.form))
        ClozeSessionAction.Next ->
            if (state.currentIndex >= state.exercises.lastIndex) state
            else state.copy(currentIndex = state.currentIndex + 1)
        ClozeSessionAction.Previous ->
            if (state.currentIndex <= 0) state
            else state.copy(currentIndex = state.currentIndex - 1)
        ClozeSessionAction.Restart -> state.copy(currentIndex = 0, answers = emptyMap())
    }
