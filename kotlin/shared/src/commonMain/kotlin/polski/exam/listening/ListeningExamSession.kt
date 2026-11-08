package polski.exam.listening

import polski.exam.ExamPartScore

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
    if (state.submitted) return state
    return when (action) {
        is ListeningExamAction.Answer -> if (state.exam.tasks.none { task -> task.questions.any { it.id == action.questionId } }) state else state.copy(answers = state.answers.toMap() + (action.questionId to action.answer))
        ListeningExamAction.Next -> state.copy(currentIndex = (state.currentIndex + 1).coerceAtMost(state.exam.tasks.lastIndex))
        ListeningExamAction.Previous -> state.copy(currentIndex = (state.currentIndex - 1).coerceAtLeast(0))
        ListeningExamAction.Submit -> state.copy(answers = state.answers.toMap(), submitted = true, result = ListeningExamScorer.scoreAttempt(state.exam, state.answers))
    }
}
