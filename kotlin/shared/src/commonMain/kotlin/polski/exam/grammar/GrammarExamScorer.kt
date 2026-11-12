package polski.exam.grammar

import polski.exam.ExamAnswerEvaluation
import polski.exam.ExamAnswerVariant
import polski.exam.evaluateExamAnswer
import polski.exam.evaluateAnswerPolicyFacts
import polski.exam.examResultVisibility
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
    val isFinal: Boolean get() = examResultVisibility(true, false, manualPendingQuarterPoints, possibleQuarterPoints).isFinal
    val upperBoundQuarterPoints: Int get() = earnedQuarterPoints + manualPendingQuarterPoints
}

object GrammarExamScorer {
    fun score(item: GrammarItem, answer: String): Boolean =
        item.pairedWithItemId == null && normalize(answer) == normalize(item.correctAnswer)

    fun grade(item: GrammarItem, answer: String): ExamAnswerEvaluation = if (item.pairedWithItemId != null) {
        evaluateAnswerPolicyFacts(false, 0, item.maximumQuarter, normalize(answer).isNotBlank(), true)
    } else {
        evaluateWithVariants(item, answer, item.scoredVariants.filter { it.onlyIfNotUsedByItemId == null })
    }

    /** Grades conditional key variants against the complete, current answer snapshot. */
    fun gradeInAttempt(test: GrammarTest, item: GrammarItem, answers: Map<String, String>): ExamAnswerEvaluation {
        require(item in test.items) { "Item ${item.itemId} is not in ${test.testId}" }
        if (item.pairedWithItemId != null) {
            val pair = test.answerPairs.firstOrNull { it.firstItemId == item.itemId || it.secondItemId == item.itemId }
                ?: error("Missing answer pair for ${item.itemId}")
            val firstAnswer = normalize(answers[pair.firstItemId].orEmpty())
            val secondAnswer = normalize(answers[pair.secondItemId].orEmpty())
            val matched = test.answerPairs.firstOrNull { candidate ->
                candidate.firstItemId == pair.firstItemId && candidate.secondItemId == pair.secondItemId &&
                    normalize(candidate.firstAnswer) == firstAnswer && normalize(candidate.secondAnswer) == secondAnswer
            }
            return evaluateAnswerPolicyFacts(
                matchesCanonical = false,
                variantQuarter = when (item.itemId) {
                    pair.firstItemId -> matched?.firstEarnedQuarter ?: 0
                    else -> matched?.secondEarnedQuarter ?: 0
                },
                maximumQuarter = item.maximumQuarter,
                answerNonblank = normalize(answers[item.itemId].orEmpty()).isNotBlank(),
                openManual = true,
            )
        }
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
