package polski.exam.grammar

import polski.exam.ExamAnswerEvaluation
import polski.exam.ExamAnswerVariant
import polski.exam.evaluateExamAnswer
import polski.grammar.normalize

data class GrammarExamResult(
    /** Legacy whole-point view. Use quarter fields for exams with fractional scores. */
    val correct: Int,
    val total: Int,
    val percentage: Double,
    val earnedQuarterPoints: Int = correct * 4,
    val possibleQuarterPoints: Int = total * 4,
    val manualPendingQuarterPoints: Int = 0,
) {
    val isFinal: Boolean get() = manualPendingQuarterPoints == 0
    val upperBoundQuarterPoints: Int get() = earnedQuarterPoints + manualPendingQuarterPoints
}

object GrammarExamScorer {
    fun score(item: GrammarItem, answer: String): Boolean =
        normalize(answer) == normalize(item.correctAnswer)

    fun grade(item: GrammarItem, answer: String): ExamAnswerEvaluation = evaluateWithVariants(
        item, answer, item.scoredVariants.filter { it.onlyIfNotUsedByItemId == null },
    )

    /** Grades conditional key variants against the complete, current answer snapshot. */
    fun gradeInAttempt(test: GrammarTest, item: GrammarItem, answers: Map<String, String>): ExamAnswerEvaluation {
        require(item in test.items) { "Item ${item.itemId} is not in ${test.testId}" }
        val eligible = item.scoredVariants.filter { variant ->
            variant.onlyIfNotUsedByItemId?.let { otherId ->
                normalize(answers[otherId].orEmpty()) != normalize(variant.answer)
            } ?: true
        }
        return evaluateWithVariants(item, answers[item.itemId].orEmpty(), eligible)
    }

    private fun evaluateWithVariants(
        item: GrammarItem, answer: String, variants: List<GrammarScoredVariant>,
    ): ExamAnswerEvaluation = evaluateExamAnswer(
        mode = item.gradingMode,
        fullAnswer = item.correctAnswer,
        variants = variants.map { ExamAnswerVariant(it.answer, it.earnedQuarter) },
        answer = answer,
        maximumQuarter = item.maximumQuarter,
    )

    /** Earned score in exact quarter-point units. Full answer takes precedence over variants. */
    fun earnedQuarter(item: GrammarItem, answer: String): Int = grade(item, answer).confirmedQuarter

    fun scoreAttempt(test: GrammarTest, answers: Map<String, String>): GrammarExamResult {
        val possibleQuarter = test.items.sumOf { it.maximumQuarter }
        val evaluations = test.items.map { item -> gradeInAttempt(test, item, answers) }
        val earnedQuarter = evaluations.sumOf { it.confirmedQuarter }
        val pendingQuarter = evaluations.sumOf { it.pendingQuarter }
        return GrammarExamResult(
            correct = earnedQuarter / 4,
            total = possibleQuarter / 4,
            percentage = if (possibleQuarter > 0) earnedQuarter.toDouble() / possibleQuarter * 100.0 else 0.0,
            earnedQuarterPoints = earnedQuarter,
            possibleQuarterPoints = possibleQuarter,
            manualPendingQuarterPoints = pendingQuarter,
        )
    }
}
