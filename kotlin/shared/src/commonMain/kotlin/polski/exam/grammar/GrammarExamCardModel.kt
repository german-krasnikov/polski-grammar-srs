package polski.exam.grammar

import polski.exam.ExamAnswerStatus

data class GrammarExamCard(
    val instruction: String,
    val progress: String,
    val item: GrammarItem,
    val currentAnswer: String,
    val isFirst: Boolean,
    val isLast: Boolean,
    val submitted: Boolean,
    val isCorrect: Boolean?,
    val gradeStatus: ExamAnswerStatus?,
    val result: GrammarExamResult?,
)

fun grammarExamCardModel(state: GrammarExamState): GrammarExamCard {
    val item = state.test.items[state.currentIndex]
    val answer = state.answers[item.itemId].orEmpty()
    val evaluation = if (state.submitted) GrammarExamScorer.gradeInAttempt(state.test, item, state.answers) else null
    return GrammarExamCard(
        instruction = state.test.instruction,
        progress = "${state.currentIndex + 1} / ${state.test.items.size}",
        item = item,
        currentAnswer = answer,
        isFirst = state.currentIndex == 0,
        isLast = state.currentIndex == state.test.items.lastIndex,
        submitted = state.submitted,
        isCorrect = evaluation?.let { it.status == ExamAnswerStatus.ConfirmedFull },
        gradeStatus = evaluation?.status,
        result = state.result,
    )
}
