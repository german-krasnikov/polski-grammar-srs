package polski.exam.listening

import polski.exam.ExamPartScore
import polski.exam.examPartActionPermission
import polski.exam.partactionpolicy.data.PartActionPolicyOutcomeRowMutation

data class ListeningExamState(
    val exam: ListeningExam, val currentIndex: Int = 0, val answers: Map<String, String> = emptyMap(),
    val submitted: Boolean = false, val result: ExamPartScore? = null,
)
sealed interface ListeningExamAction {
    data class Answer(val questionId: String, val answer: String) : ListeningExamAction
    data object Next : ListeningExamAction
    data object Previous : ListeningExamAction
    data object Submit : ListeningExamAction
}
fun reduceListeningExam(state: ListeningExamState, action: ListeningExamAction): ListeningExamState {
    val actionKind = when (action) {
        is ListeningExamAction.Answer -> "answer"
        ListeningExamAction.Next -> "next"
        ListeningExamAction.Previous -> "previous"
        ListeningExamAction.Submit -> "submit"
    }
    val permission = examPartActionPermission(
        kind = "listening", action = actionKind, submitted = state.submitted,
        validTarget = action is ListeningExamAction.Answer && state.exam.tasks.any { task -> task.questions.any { it.id == action.questionId } },
        atFirst = state.currentIndex == 0, atLast = state.currentIndex == state.exam.tasks.lastIndex,
    )
    return when (permission) {
        PartActionPolicyOutcomeRowMutation.IGNORE -> state
        PartActionPolicyOutcomeRowMutation.APPLY_ANSWER -> {
            action as ListeningExamAction.Answer
            state.copy(answers = state.answers.toMap() + (action.questionId to action.answer))
        }
        PartActionPolicyOutcomeRowMutation.MOVE -> when (action) {
            ListeningExamAction.Next -> state.copy(currentIndex = state.currentIndex + 1)
            ListeningExamAction.Previous -> state.copy(currentIndex = state.currentIndex - 1)
            else -> error("Unexpected Listening move action: $action")
        }
        PartActionPolicyOutcomeRowMutation.SUBMIT -> state.copy(answers = state.answers.toMap(), submitted = true, result = ListeningExamScorer.scoreAttempt(state.exam, state.answers))
        else -> error("Unexpected Listening action permission: $permission")
    }
}
