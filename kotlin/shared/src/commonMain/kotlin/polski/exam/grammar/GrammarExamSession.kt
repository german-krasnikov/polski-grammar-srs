package polski.exam.grammar

data class GrammarExamState(
    val test: GrammarTest,
    val currentIndex: Int = 0,
    val answers: Map<String, String> = emptyMap(),
    val submitted: Boolean = false,
    val result: GrammarExamResult? = null,
)

sealed interface GrammarExamAction {
    data class Answer(val itemId: String, val answer: String) : GrammarExamAction
    data object Next : GrammarExamAction
    data object Previous : GrammarExamAction
    data object Submit : GrammarExamAction
}

fun reduceGrammarExam(state: GrammarExamState, action: GrammarExamAction): GrammarExamState {
    if (state.submitted) return state
    return when (action) {
        is GrammarExamAction.Answer -> state.copy(
            answers = state.answers + (action.itemId to action.answer),
        )
        is GrammarExamAction.Next -> state.copy(
            currentIndex = (state.currentIndex + 1).coerceAtMost(state.test.items.lastIndex),
        )
        is GrammarExamAction.Previous -> state.copy(
            currentIndex = (state.currentIndex - 1).coerceAtLeast(0),
        )
        is GrammarExamAction.Submit -> {
            val result = GrammarExamScorer.scoreAttempt(state.test, state.answers)
            state.copy(submitted = true, result = result)
        }
    }
}
