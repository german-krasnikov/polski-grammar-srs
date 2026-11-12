package polski.exam.grammar

import polski.exam.examPartActionPermission
import polski.exam.partactionpolicy.data.PartActionPolicyOutcomeRowMutation

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
    val actionKind = when (action) {
        is GrammarExamAction.Answer -> "answer"
        GrammarExamAction.Next -> "next"
        GrammarExamAction.Previous -> "previous"
        GrammarExamAction.Submit -> "submit"
    }
    val permission = examPartActionPermission(
        kind = "grammar", action = actionKind, submitted = state.submitted,
        validTarget = action is GrammarExamAction.Answer && state.test.items.any { it.itemId == action.itemId },
        atFirst = state.currentIndex == 0, atLast = state.currentIndex == state.test.items.lastIndex,
    )
    return when (permission) {
        PartActionPolicyOutcomeRowMutation.IGNORE -> state
        PartActionPolicyOutcomeRowMutation.APPLY_ANSWER -> {
            action as GrammarExamAction.Answer
            state.copy(answers = state.answers.toMap() + (action.itemId to action.answer))
        }
        PartActionPolicyOutcomeRowMutation.MOVE -> when (action) {
            GrammarExamAction.Next -> state.copy(currentIndex = state.currentIndex + 1)
            GrammarExamAction.Previous -> state.copy(currentIndex = state.currentIndex - 1)
            else -> error("Unexpected Grammar move action: $action")
        }
        PartActionPolicyOutcomeRowMutation.SUBMIT -> state.copy(answers = state.answers.toMap(), submitted = true, result = GrammarExamScorer.scoreAttempt(state.test, state.answers))
        else -> error("Unexpected Grammar action permission: $permission")
    }
}
