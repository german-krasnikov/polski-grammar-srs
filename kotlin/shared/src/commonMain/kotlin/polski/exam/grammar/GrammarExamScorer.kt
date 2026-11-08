package polski.exam.grammar

import polski.grammar.normalize

data class GrammarExamResult(
    /** Legacy whole-point view. Use quarter fields for exams with fractional scores. */
    val correct: Int,
    val total: Int,
    val percentage: Double,
    val earnedQuarterPoints: Int = correct * 4,
    val possibleQuarterPoints: Int = total * 4,
)

object GrammarExamScorer {
    fun score(item: GrammarItem, answer: String): Boolean =
        normalize(answer) == normalize(item.correctAnswer)

    /** Earned score in exact quarter-point units. Full answer takes precedence over variants. */
    fun earnedQuarter(item: GrammarItem, answer: String): Int {
        if (score(item, answer)) return item.maximumQuarter
        val normalized = normalize(answer)
        return item.scoredVariants.firstOrNull { normalize(it.answer) == normalized }?.earnedQuarter ?: 0
    }

    fun scoreAttempt(test: GrammarTest, answers: Map<String, String>): GrammarExamResult {
        val possibleQuarter = test.items.sumOf { it.maximumQuarter }
        val earnedQuarter = test.items.sumOf { item ->
            answers[item.itemId]?.let { earnedQuarter(item, it) } ?: 0
        }
        return GrammarExamResult(
            correct = earnedQuarter / 4,
            total = possibleQuarter / 4,
            percentage = if (possibleQuarter > 0) earnedQuarter.toDouble() / possibleQuarter * 100.0 else 0.0,
            earnedQuarterPoints = earnedQuarter,
            possibleQuarterPoints = possibleQuarter,
        )
    }
}
