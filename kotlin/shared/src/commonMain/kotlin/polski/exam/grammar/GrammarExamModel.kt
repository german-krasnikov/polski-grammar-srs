package polski.exam.grammar

import polski.exam.QuestionType
import polski.exam.ExamGradingMode
import polski.grammar.normalize

data class GrammarTest(
    val testId: String,
    val instruction: String,
    val items: List<GrammarItem>,
    val bankTokens: List<GrammarBankToken> = emptyList(),
) {
    init {
        require(bankTokens.map { it.tokenId }.distinct().size == bankTokens.size) { "Duplicate bank token ID in $testId" }
        require(bankTokens.map { it.position }.distinct().size == bankTokens.size) { "Duplicate bank position in $testId" }
        require(bankTokens.all { it.position > 0 && it.text.isNotBlank() }) { "Invalid bank token in $testId" }
        require(items.none { it.questionType == QuestionType.WordBank } || bankTokens.isNotEmpty()) { "Missing word bank in $testId" }
        val itemIds = items.map { it.itemId }.toSet()
        require(itemIds.size == items.size) { "Duplicate item in $testId" }
        items.forEach { item ->
            require(item.questionType != QuestionType.ChooseForm ||
                (item.options.isNotEmpty() && item.options.all { it.isNotBlank() })) {
                "Missing choice options for $testId/${item.itemId}"
            }
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

data class GrammarBankToken(val tokenId: String, val position: Int, val text: String)

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
    val questionTarget: String? = null,
) {
    val maximumQuarter: Int get() = pointsQuarter ?: points * 4

    init {
        require(maximumQuarter > 0) { "Item $itemId must have a positive maximum" }
        require(normalize(correctAnswer).isNotBlank()) { "Missing answer for $itemId" }
        require(questionType != QuestionType.QuestionFormation || !questionTarget.isNullOrBlank()) { "Missing question target for $itemId" }
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
