package polski.exam.writing

import polski.exam.ExamPartScore
import polski.exam.examPartActionPermission
import polski.exam.partactionpolicy.data.PartActionPolicyOutcomeRowMutation

data class WritingExamState(
    val exam: WritingExam, val selectedSetId: String? = null, val currentIndex: Int = 0,
    val drafts: Map<String, String> = emptyMap(), val submitted: Boolean = false,
    val result: ExamPartScore? = null, val submittedTaskIds: List<String> = emptyList(),
)
sealed interface WritingExamAction {
    data class SelectSet(val setId: String?) : WritingExamAction
    data class Answer(val taskId: String, val text: String) : WritingExamAction
    data object Next : WritingExamAction
    data object Previous : WritingExamAction
    data object Submit : WritingExamAction
}
fun reduceWritingExam(state: WritingExamState, action: WritingExamAction): WritingExamState {
    val active = if (state.exam.sets.isEmpty()) state.exam.tasks else state.exam.tasksForSet(state.selectedSetId)
    val actionKind = when (action) {
        is WritingExamAction.SelectSet -> "select_set"
        is WritingExamAction.Answer -> "answer"
        WritingExamAction.Next -> "next"
        WritingExamAction.Previous -> "previous"
        WritingExamAction.Submit -> "submit"
    }
    val permission = examPartActionPermission(
        kind = "writing", action = actionKind, submitted = state.submitted,
        validTarget = action is WritingExamAction.Answer && state.exam.tasks.any { it.id == action.taskId },
        validSelectedSet = action is WritingExamAction.SelectSet && (action.setId == null || action.setId in state.exam.sets),
        targetInSelectedSet = action is WritingExamAction.Answer && active.any { it.id == action.taskId },
        atFirst = state.currentIndex == 0, atLast = state.currentIndex >= (active.size - 1).coerceAtLeast(0),
    )
    return when (permission) {
        PartActionPolicyOutcomeRowMutation.IGNORE -> state
        PartActionPolicyOutcomeRowMutation.SELECT_SET -> {
            action as WritingExamAction.SelectSet
            state.copy(selectedSetId = action.setId, currentIndex = 0)
        }
        PartActionPolicyOutcomeRowMutation.APPLY_ANSWER -> {
            action as WritingExamAction.Answer
            state.copy(drafts = state.drafts.toMap() + (action.taskId to action.text))
        }
        PartActionPolicyOutcomeRowMutation.MOVE -> when (action) {
            WritingExamAction.Next -> state.copy(currentIndex = state.currentIndex + 1)
            WritingExamAction.Previous -> state.copy(currentIndex = state.currentIndex - 1)
            else -> error("Unexpected Writing move action: $action")
        }
        PartActionPolicyOutcomeRowMutation.SUBMIT -> state.copy(drafts = state.drafts.toMap(), submitted = true, submittedTaskIds = active.map { it.id }, result = state.exam.maximumQuarter?.let { ExamPartScore(0, it, it) })
    }
}
