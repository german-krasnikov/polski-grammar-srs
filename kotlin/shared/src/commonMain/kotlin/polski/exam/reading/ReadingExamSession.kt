package polski.exam.reading

import polski.exam.ExamPartScore

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
    if (state.submitted) return state
    return when (action) {
        is ReadingExamAction.Answer -> if (state.exam.questions.none { it.id == action.questionId }) state else state.copy(answers = state.answers.toMap() + (action.questionId to action.answer))
        ReadingExamAction.Next -> state.copy(currentIndex = (state.currentIndex + 1).coerceAtMost(state.exam.passages.lastIndex))
        ReadingExamAction.Previous -> state.copy(currentIndex = (state.currentIndex - 1).coerceAtLeast(0))
        ReadingExamAction.Submit -> state.copy(answers = state.answers.toMap(), submitted = true, result = ReadingExamScorer.scoreAttempt(state.exam, state.answers))
    }
}
