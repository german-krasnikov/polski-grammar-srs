package polski.exam.writing

import polski.exam.ExamPartScore

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
    if (state.submitted) return state
    val active = if (state.exam.sets.isEmpty()) state.exam.tasks else state.exam.tasksForSet(state.selectedSetId)
    return when (action) {
        is WritingExamAction.SelectSet -> if (action.setId != null && action.setId !in state.exam.sets) state else state.copy(selectedSetId = action.setId, currentIndex = 0)
        is WritingExamAction.Answer -> if (active.none { it.id == action.taskId }) state else state.copy(drafts = state.drafts.toMap() + (action.taskId to action.text))
        WritingExamAction.Next -> state.copy(currentIndex = (state.currentIndex + 1).coerceAtMost((active.size - 1).coerceAtLeast(0)))
        WritingExamAction.Previous -> state.copy(currentIndex = (state.currentIndex - 1).coerceAtLeast(0))
        WritingExamAction.Submit -> state.copy(drafts = state.drafts.toMap(), submitted = true, submittedTaskIds = active.map { it.id }, result = state.exam.maximumQuarter?.let { ExamPartScore(0, it, it) })
    }
}
