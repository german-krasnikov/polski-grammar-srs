package polski.exam

import polski.exam.grammar.GrammarExamScorer
import polski.exam.grammar.GrammarItem
import polski.exam.grammar.GrammarTest

/** A read-only review of the submitted, frozen grammar answer map. */
data class ExamReviewRow(
    val itemId: String,
    val chosenAnswer: String?,
    val officialExamples: List<String>,
    val evaluation: ExamAnswerEvaluation,
    val maximumQuarter: Int,
)

fun grammarReviewRows(test: GrammarTest, frozenAnswers: Map<String, String>): List<ExamReviewRow> =
    test.items.map { item ->
        ExamReviewRow(
            itemId = item.itemId,
            chosenAnswer = frozenAnswers[item.itemId]?.takeIf { it.isNotBlank() },
            officialExamples = officialGrammarExamples(item, test),
            evaluation = GrammarExamScorer.gradeInAttempt(test, item, frozenAnswers),
            maximumQuarter = item.maximumQuarter,
        )
    }

fun officialGrammarExamples(item: GrammarItem, test: GrammarTest? = null): List<String> {
    val paired = test?.answerPairs.orEmpty().flatMap { pair ->
        listOfNotNull(
            pair.firstAnswer.takeIf { pair.firstItemId == item.itemId },
            pair.secondAnswer.takeIf { pair.secondItemId == item.itemId },
        )
    }
    return (listOf(item.correctAnswer) + item.scoredVariants.map { it.answer } + paired).distinct()
}
