package polski.exam.writing

import polski.exam.ExamPartScore

data class WritingExamCardModel(val sets: List<String>, val task: WritingTask?, val draft: String, val position: Int, val count: Int, val submitted: Boolean, val result: ExamPartScore?)
fun writingExamCardModel(state: WritingExamState): WritingExamCardModel {
    val active = if (state.exam.sets.isEmpty()) state.exam.tasks else state.exam.tasksForSet(state.selectedSetId)
    val task = active.getOrNull(state.currentIndex)
    return WritingExamCardModel(state.exam.sets, task, task?.let { state.drafts[it.id] }.orEmpty(), state.currentIndex + 1, active.size, state.submitted, state.result)
}
