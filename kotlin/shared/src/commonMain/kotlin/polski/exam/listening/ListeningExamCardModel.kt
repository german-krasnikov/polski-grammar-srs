package polski.exam.listening

import polski.exam.ExamPartScore

data class ListeningExamCardModel(val task: ListeningTask, val answers: Map<String, String>, val position: Int, val count: Int, val canPrevious: Boolean, val canNext: Boolean, val submitted: Boolean, val result: ExamPartScore?)
fun listeningExamCardModel(state: ListeningExamState): ListeningExamCardModel = ListeningExamCardModel(state.exam.tasks[state.currentIndex], state.answers.toMap(), state.currentIndex + 1, state.exam.tasks.size, state.currentIndex > 0, state.currentIndex < state.exam.tasks.lastIndex, state.submitted, state.result)
