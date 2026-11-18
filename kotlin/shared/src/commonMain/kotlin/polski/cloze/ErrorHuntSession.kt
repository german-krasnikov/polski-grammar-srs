package polski.cloze

import polski.cloze.policy.Policy

data class ErrorHuntSessionState(
    val exercises: List<ErrorHuntExercise>,
    val currentIndex: Int = 0,
    /** exerciseId → wrong-word tap count. */
    val missCounts: Map<String, Int> = emptyMap(),
    /** exerciseIds where the error token was tapped. */
    val found: Set<String> = emptySet(),
    /** exerciseId → chosen form. */
    val answers: Map<String, String> = emptyMap(),
) {
    fun shouldReveal(id: String): Boolean =
        Policy.shouldReveal(Policy.ShouldRevealInput(
            found = id in found,
            missCount = (missCounts[id] ?: 0).toDouble(),
        )) == Policy.ShouldRevealValues.Revealed

    fun scoreClass(id: String): String? {
        val answer = answers[id] ?: return null
        val ex = exercises.firstOrNull { it.id == id } ?: return null
        return Policy.scoreClass(Policy.ScoreClassInput(
            answerCorrect = answer == ex.correctForm,
            found = id in found,
            missCount = (missCounts[id] ?: 0).toDouble(),
        ))
    }

    fun summary(): Map<String, Int> =
        exercises.mapNotNull { scoreClass(it.id) }.groupingBy { it }.eachCount()
}

sealed interface ErrorHuntAction {
    /** Tap a token by its index in [ErrorHuntExercise.tokens]. */
    data class TapToken(val id: String, val index: Int) : ErrorHuntAction
    /** Choose a form from options. Ignored unless error is found or revealed. */
    data class Choose(val id: String, val form: String) : ErrorHuntAction
    data object Next : ErrorHuntAction
    data object Previous : ErrorHuntAction
    data object Restart : ErrorHuntAction
}

/** Pure reducer. Native fact [ErrorHuntAction.TapToken.index] == [ErrorHuntExercise.errorTokenIndex]
 *  is computed here; policy yields the verdict; reducer applies mutation (P12). */
fun reduceErrorHuntSession(
    state: ErrorHuntSessionState,
    action: ErrorHuntAction,
): ErrorHuntSessionState = when (action) {
    is ErrorHuntAction.TapToken -> {
        val ex = state.exercises.firstOrNull { it.id == action.id } ?: return state
        if (state.answers.containsKey(action.id)) return state
        val tapIsError = action.index == ex.errorTokenIndex
        when (Policy.tapOutcome(Policy.TapOutcomeInput(tapIsError = tapIsError))) {
            Policy.TapOutcomeValues.Hit ->
                if (action.id in state.found) state
                else state.copy(found = state.found + action.id)
            else ->
                if (action.id in state.found) state
                else state.copy(missCounts = state.missCounts +
                    (action.id to ((state.missCounts[action.id] ?: 0) + 1)))
        }
    }
    is ErrorHuntAction.Choose -> {
        if (state.answers.containsKey(action.id)) return state
        if (action.id !in state.found && !state.shouldReveal(action.id)) return state
        state.copy(answers = state.answers + (action.id to action.form))
    }
    ErrorHuntAction.Next ->
        if (state.currentIndex >= state.exercises.lastIndex) state
        else state.copy(currentIndex = state.currentIndex + 1)
    ErrorHuntAction.Previous ->
        if (state.currentIndex <= 0) state
        else state.copy(currentIndex = state.currentIndex - 1)
    ErrorHuntAction.Restart ->
        state.copy(currentIndex = 0, missCounts = emptyMap(),
                   found = emptySet(), answers = emptyMap())
}
