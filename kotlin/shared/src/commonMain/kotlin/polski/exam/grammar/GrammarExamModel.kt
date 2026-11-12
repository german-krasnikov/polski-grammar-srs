package polski.exam.grammar

import polski.exam.QuestionType
import polski.exam.ExamGradingMode
import polski.grammar.normalize

data class GrammarTest(
    val testId: String,
    val instruction: String,
    val items: List<GrammarItem>,
    val bankTokens: List<GrammarBankToken> = emptyList(),
    /** Official whole-answer combinations; no cross-product is implied. */
    val answerPairs: List<GrammarAnswerPair> = emptyList(),
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
        val partners = mutableMapOf<String, String>()
        val firstItems = mutableSetOf<String>()
        val secondItems = mutableSetOf<String>()
        val tuples = mutableSetOf<List<String>>()
        answerPairs.forEach { pair ->
            require(pair.firstItemId != pair.secondItemId && pair.firstItemId in itemIds && pair.secondItemId in itemIds) {
                "Pair ${pair.pairId} must reference two items in $testId"
            }
            firstItems.add(pair.firstItemId)
            secondItems.add(pair.secondItemId)
            require(pair.firstItemId !in secondItems && pair.secondItemId !in firstItems) {
                "Pair order conflicts in $testId"
            }
            require((partners[pair.firstItemId] == null || partners[pair.firstItemId] == pair.secondItemId) &&
                (partners[pair.secondItemId] == null || partners[pair.secondItemId] == pair.firstItemId)) {
                "Inconsistent pair partners in $testId"
            }
            partners[pair.firstItemId] = pair.secondItemId
            partners[pair.secondItemId] = pair.firstItemId
            val first = items.single { it.itemId == pair.firstItemId }
            val second = items.single { it.itemId == pair.secondItemId }
            require(pair.firstEarnedQuarter in 1..first.maximumQuarter && pair.secondEarnedQuarter in 1..second.maximumQuarter) {
                "Invalid pair credit in ${pair.pairId}"
            }
            val firstAnswer = normalize(pair.firstAnswer)
            val secondAnswer = normalize(pair.secondAnswer)
            val tuple = listOf(pair.firstItemId, pair.secondItemId, firstAnswer, secondAnswer)
            require(firstAnswer.isNotBlank() && secondAnswer.isNotBlank() && tuples.add(tuple)) {
                "Blank or duplicate pair answer in ${pair.pairId}"
            }
        }
        items.forEach { item -> require(item.pairedWithItemId == partners[item.itemId]) {
            "Pair marker mismatch for ${item.itemId}"
        } }
    }
}

data class GrammarBankToken(val tokenId: String, val position: Int, val text: String)

data class GrammarAnswerPair(
    val pairId: String,
    val firstItemId: String,
    val firstAnswer: String,
    val secondItemId: String,
    val secondAnswer: String,
    val firstEarnedQuarter: Int,
    val secondEarnedQuarter: Int,
)

/** Official review criterion, displayed without automatic text matching. */
data class GrammarRubricComponent(
    val componentId: String,
    val order: Int,
    val creditQuarter: Int,
    val criterionText: String,
)

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
    val pairedWithItemId: String? = null,
    val rubricComponents: List<GrammarRubricComponent> = emptyList(),
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
        require(rubricComponents.map { it.componentId }.distinct().size == rubricComponents.size &&
            rubricComponents.map { it.order }.distinct().size == rubricComponents.size &&
            rubricComponents.all { it.order > 0 && it.creditQuarter > 0 && it.criterionText.isNotBlank() } &&
            (rubricComponents.isEmpty() || rubricComponents.sumOf { it.creditQuarter } == maximumQuarter)) {
            "Invalid rubric components for $itemId"
        }
    }
}

data class GrammarScoredVariant(
    val answer: String,
    val earnedQuarter: Int,
    /** Item in this test whose same normalized answer blocks this variant. */
    val onlyIfNotUsedByItemId: String? = null,
)

data class GrammarAttempt(val itemId: String, val answer: String)
