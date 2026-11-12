package polski.exam.reading

import polski.exam.ExamPartScore
import polski.exam.examPartActionPermission
import polski.exam.partactionpolicy.data.PartActionPolicyOutcomeRowMutation

data class ReadingExamState(
    val exam: ReadingExam, val currentIndex: Int = 0, val answers: Map<String, String> = emptyMap(),
    val submitted: Boolean = false, val result: ExamPartScore? = null,
)
sealed interface ReadingExamAction {
    data class Answer(val questionId: String, val answer: String) : ReadingExamAction
    data object Next : ReadingExamAction
    data object Previous : ReadingExamAction
    data object Submit : ReadingExamAction
}
fun reduceReadingExam(state: ReadingExamState, action: ReadingExamAction): ReadingExamState {
    val actionKind = when (action) {
        is ReadingExamAction.Answer -> "answer"
        ReadingExamAction.Next -> "next"
        ReadingExamAction.Previous -> "previous"
        ReadingExamAction.Submit -> "submit"
    }
    val permission = examPartActionPermission(
        kind = "reading", action = actionKind, submitted = state.submitted,
        validTarget = action is ReadingExamAction.Answer && state.exam.questions.any { it.id == action.questionId },
        atFirst = state.currentIndex == 0, atLast = state.currentIndex >= state.exam.passages.lastIndex,
    )
    return when (permission) {
        PartActionPolicyOutcomeRowMutation.IGNORE -> state
        PartActionPolicyOutcomeRowMutation.APPLY_ANSWER -> {
            action as ReadingExamAction.Answer
            state.copy(answers = state.answers.toMap() + (action.questionId to action.answer))
        }
        PartActionPolicyOutcomeRowMutation.MOVE -> when (action) {
            ReadingExamAction.Next -> state.copy(currentIndex = state.currentIndex + 1)
            ReadingExamAction.Previous -> state.copy(currentIndex = state.currentIndex - 1)
            else -> error("Unexpected Reading move action: $action")
        }
        PartActionPolicyOutcomeRowMutation.SUBMIT -> state.copy(answers = state.answers.toMap(), submitted = true, result = ReadingExamScorer.scoreAttempt(state.exam, state.answers))
        else -> error("Unexpected Reading action permission: $permission")
    }
}
