package polski.exam.reading

import polski.exam.ExamPartScore

data class ReadingExamCardModel(
    val passage: ReadingPassage, val questions: List<ReadingQuestion>, val answers: Map<String, String>,
    val position: Int, val count: Int, val canPrevious: Boolean, val canNext: Boolean, val submitted: Boolean,
    val result: ExamPartScore?,
)
fun readingExamCardModel(state: ReadingExamState): ReadingExamCardModel {
    val passage = state.exam.passages[state.currentIndex]
    val questions = state.exam.questions.filter { it.passageId == passage.id }
    return ReadingExamCardModel(passage, questions, state.answers.toMap(), state.currentIndex + 1, state.exam.passages.size, state.currentIndex > 0, state.currentIndex < state.exam.passages.lastIndex, state.submitted, state.result)
}
