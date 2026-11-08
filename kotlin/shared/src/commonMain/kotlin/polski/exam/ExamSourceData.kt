package polski.exam

import polski.exam.grammar.GrammarScoredVariant
import polski.exam.grammar.GrammarTest

/** Grammar tests from generated biome rows. */
val officialGrammarData: Map<String, List<GrammarTest>> by lazy {
    val b1 = mapB1Grammar()
    mapOf(
        "b1-2019" to b1,
        "b2-2019" to mapB2Grammar(),
        "b1-2017" to mapB1y2017Grammar(),
        "b1-2020" to mapB1y2020Grammar(),
        "b2-2017" to mapB2y2017Grammar(),
        "b2-2020" to mapB2y2020Grammar(),
    )
}

private fun mapB1Grammar(): List<GrammarTest> = ExamDataMapper.grammarTests(
    tests = polski.exam.data.b1y2019.GRAMMAR_TEST_ROW_ROWS,
    items = polski.exam.data.b1y2019.GRAMMAR_ITEM_ROW_ROWS,
    testId = { it.testId }, testInstruction = { it.instruction },
    itemTestRef = { it.testRef }, itemId = { it.itemId },
    itemType = { it.questionType.tag }, itemContext = { it.context },
    itemGap = { it.gap }, itemOptions = { it.options },
    itemAnswer = { it.correctAnswer }, itemPoints = { it.points }, itemQuarter = { it.pointsQuarter },
    itemGradingMode = { it.gradingMode?.tag },
    bankTokens = polski.exam.data.b1y2019.GRAMMAR_BANK_TOKEN_ROW_ROWS.groupBy(
        keySelector = { it.testRef },
        valueTransform = { polski.exam.grammar.GrammarBankToken(it.tokenId, it.position, it.text) },
    ),
    itemQuestionTarget = { it.questionTarget },
    variants = polski.exam.data.b1y2019.GRAMMAR_ANSWER_VARIANT_ROW_ROWS.groupBy(
        keySelector = { it.itemRef },
        valueTransform = { GrammarScoredVariant(it.answer, it.earnedQuarter) },
    ),
)

private fun mapB2Grammar(): List<GrammarTest> = ExamDataMapper.grammarTests(
    tests = polski.exam.data.b2y2019.GRAMMAR_TEST_ROW_ROWS,
    items = polski.exam.data.b2y2019.GRAMMAR_ITEM_ROW_ROWS,
    testId = { it.testId }, testInstruction = { it.instruction },
    itemTestRef = { it.testRef }, itemId = { it.itemId },
    itemType = { it.questionType.tag }, itemContext = { it.context },
    itemGap = { it.gap }, itemOptions = { it.options },
    itemAnswer = { it.correctAnswer }, itemPoints = { it.points }, itemQuarter = { it.pointsQuarter },
    itemGradingMode = { it.gradingMode?.tag },
    bankTokens = polski.exam.data.b2y2019.GRAMMAR_BANK_TOKEN_ROW_ROWS.groupBy(
        keySelector = { it.testRef },
        valueTransform = { polski.exam.grammar.GrammarBankToken(it.tokenId, it.position, it.text) },
    ),
    itemQuestionTarget = { it.questionTarget },
    variants = polski.exam.data.b2y2019.GRAMMAR_ANSWER_VARIANT_ROW_ROWS.groupBy(
        keySelector = { it.itemRef },
        valueTransform = { GrammarScoredVariant(it.answer, it.earnedQuarter) },
    ),
)

private fun mapB1y2017Grammar(): List<GrammarTest> {
    val variants = polski.exam.data.b1y2017.GRAMMAR_ANSWER_VARIANT_ROW_ROWS.groupBy(
        keySelector = { it.itemRef },
        valueTransform = { GrammarScoredVariant(it.answer, it.earnedQuarter) },
    )
    return ExamDataMapper.grammarTests(
        tests = polski.exam.data.b1y2017.GRAMMAR_TEST_ROW_ROWS,
        items = polski.exam.data.b1y2017.GRAMMAR_ITEM_ROW_ROWS,
        testId = { it.testId }, testInstruction = { it.instruction },
        itemTestRef = { it.testRef }, itemId = { it.itemId },
        itemType = { it.questionType.tag }, itemContext = { it.context },
        itemGap = { it.gap }, itemOptions = { it.options },
        itemAnswer = { it.correctAnswer }, itemPoints = { it.points }, itemQuarter = { it.pointsQuarter },
        bankTokens = polski.exam.data.b1y2017.GRAMMAR_BANK_TOKEN_ROW_ROWS.groupBy(
        keySelector = { it.testRef },
        valueTransform = { polski.exam.grammar.GrammarBankToken(it.tokenId, it.position, it.text) },
    ),
    itemQuestionTarget = { it.questionTarget },
    variants = variants,
    )
}

private fun mapB1y2020Grammar(): List<GrammarTest> = ExamDataMapper.grammarTests(
    tests = polski.exam.data.b1y2020.GRAMMAR_TEST_ROW_ROWS,
    items = polski.exam.data.b1y2020.GRAMMAR_ITEM_ROW_ROWS,
    testId = { it.testId }, testInstruction = { it.instruction },
    itemTestRef = { it.testRef }, itemId = { it.itemId },
    itemType = { it.questionType.tag }, itemContext = { it.context },
    itemGap = { it.gap }, itemOptions = { it.options },
    itemAnswer = { it.correctAnswer }, itemPoints = { it.points }, itemQuarter = { it.pointsQuarter },
    itemGradingMode = { it.gradingMode?.tag },
    bankTokens = polski.exam.data.b1y2020.GRAMMAR_BANK_TOKEN_ROW_ROWS.groupBy(
        keySelector = { it.testRef },
        valueTransform = { polski.exam.grammar.GrammarBankToken(it.tokenId, it.position, it.text) },
    ),
    itemQuestionTarget = { it.questionTarget },
    variants = polski.exam.data.b1y2020.GRAMMAR_ANSWER_VARIANT_ROW_ROWS.groupBy(
        keySelector = { it.itemRef },
        valueTransform = { GrammarScoredVariant(it.answer, it.earnedQuarter) },
    ),
)

private fun mapB2y2017Grammar(): List<GrammarTest> = ExamDataMapper.grammarTests(
    tests = polski.exam.data.b2y2017.GRAMMAR_TEST_ROW_ROWS,
    items = polski.exam.data.b2y2017.GRAMMAR_ITEM_ROW_ROWS,
    testId = { it.testId }, testInstruction = { it.instruction },
    itemTestRef = { it.testRef }, itemId = { it.itemId },
    itemType = { it.questionType.tag }, itemContext = { it.context },
    itemGap = { it.gap }, itemOptions = { it.options },
    itemAnswer = { it.correctAnswer }, itemPoints = { it.points }, itemQuarter = { it.pointsQuarter },
    itemGradingMode = { it.gradingMode?.tag },
    bankTokens = polski.exam.data.b2y2017.GRAMMAR_BANK_TOKEN_ROW_ROWS.groupBy(
        keySelector = { it.testRef },
        valueTransform = { polski.exam.grammar.GrammarBankToken(it.tokenId, it.position, it.text) },
    ),
    itemQuestionTarget = { it.questionTarget },
    variants = polski.exam.data.b2y2017.GRAMMAR_ANSWER_VARIANT_ROW_ROWS.groupBy(
        keySelector = { it.itemRef },
        valueTransform = { GrammarScoredVariant(it.answer, it.earnedQuarter) },
    ),
)

private fun mapB2y2020Grammar(): List<GrammarTest> = ExamDataMapper.grammarTests(
    tests = polski.exam.data.b2y2020.GRAMMAR_TEST_ROW_ROWS,
    items = polski.exam.data.b2y2020.GRAMMAR_ITEM_ROW_ROWS,
    testId = { it.testId }, testInstruction = { it.instruction },
    itemTestRef = { it.testRef }, itemId = { it.itemId },
    itemType = { it.questionType.tag }, itemContext = { it.context },
    itemGap = { it.gap }, itemOptions = { it.options },
    itemAnswer = { it.correctAnswer }, itemPoints = { it.points }, itemQuarter = { it.pointsQuarter },
    itemGradingMode = { it.gradingMode?.tag },
    bankTokens = polski.exam.data.b2y2020.GRAMMAR_BANK_TOKEN_ROW_ROWS.groupBy(
        keySelector = { it.testRef },
        valueTransform = { polski.exam.grammar.GrammarBankToken(it.tokenId, it.position, it.text) },
    ),
    itemQuestionTarget = { it.questionTarget },
    variants = polski.exam.data.b2y2020.GRAMMAR_ANSWER_VARIANT_ROW_ROWS.groupBy(
        keySelector = { it.itemRef },
        valueTransform = { GrammarScoredVariant(it.answer, it.earnedQuarter, it.onlyIfNotUsedBy) },
    ),
)
