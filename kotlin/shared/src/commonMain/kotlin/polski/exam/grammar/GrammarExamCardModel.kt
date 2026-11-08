package polski.exam.grammar

data class GrammarExamCard(
    val instruction: String,
    val progress: String,
    val item: GrammarItem,
    val currentAnswer: String,
    val isFirst: Boolean,
    val isLast: Boolean,
    val submitted: Boolean,
    val isCorrect: Boolean?,
    val result: GrammarExamResult?,
)

fun grammarExamCardModel(state: GrammarExamState): GrammarExamCard {
    val item = state.test.items[state.currentIndex]
    val answer = state.answers[item.itemId].orEmpty()
    return GrammarExamCard(
        instruction = state.test.instruction,
        progress = "${state.currentIndex + 1} / ${state.test.items.size}",
        item = item,
        currentAnswer = answer,
        isFirst = state.currentIndex == 0,
        isLast = state.currentIndex == state.test.items.lastIndex,
        submitted = state.submitted,
        isCorrect = if (state.submitted) GrammarExamScorer.score(item, answer) else null,
        result = state.result,
    )
}
