package polski.exam.grammar

import polski.exam.QuestionType
import polski.exam.ExamGradingMode
import polski.grammar.normalize

data class GrammarTest(
    val testId: String,
    val instruction: String,
    val items: List<GrammarItem>,
) {
    init {
        val itemIds = items.map { it.itemId }.toSet()
        require(itemIds.size == items.size) { "Duplicate item in $testId" }
        items.forEach { item ->
            item.scoredVariants.forEach { variant ->
                variant.onlyIfNotUsedByItemId?.let { otherId ->
                    require(otherId != item.itemId && otherId in itemIds) {
                        "Conditional answer for ${item.itemId} must reference another item in $testId"
                    }
                }
            }
        }
    }
}

data class GrammarItem(
    val itemId: String,
    val questionType: QuestionType,
    val context: String,
    val gap: String?,
    val options: List<String>,
    val correctAnswer: String,
    val points: Int,
    /** Authoritative maximum in quarter points; legacy [points] remains for integer exams. */
    val pointsQuarter: Int? = null,
    val scoredVariants: List<GrammarScoredVariant> = emptyList(),
    val gradingMode: ExamGradingMode = ExamGradingMode.Exact,
) {
    val maximumQuarter: Int get() = pointsQuarter ?: points * 4

    init {
        require(maximumQuarter > 0) { "Item $itemId must have a positive maximum" }
        require(normalize(correctAnswer).isNotBlank()) { "Missing answer for $itemId" }
        require(scoredVariants.all { it.earnedQuarter in 1..maximumQuarter }) { "Invalid variant score for $itemId" }
        require(scoredVariants.all { normalize(it.answer).isNotBlank() }) { "Blank variant answer for $itemId" }
        val accepted = listOf(correctAnswer) + scoredVariants.map { it.answer }
        require(accepted.map(::normalize).toSet().size == accepted.size) { "Duplicate answer for $itemId" }
    }
}

data class GrammarScoredVariant(
    val answer: String,
    val earnedQuarter: Int,
    /** Item in this test whose same normalized answer blocks this variant. */
    val onlyIfNotUsedByItemId: String? = null,
)

data class GrammarAttempt(val itemId: String, val answer: String)
