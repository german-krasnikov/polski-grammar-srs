package polski.exam

import polski.exam.data.published.EXAM_PUBLICATION_ROW_ROWS
import polski.exam.data.published.GRAMMAR_ANSWER_VARIANT_ROW_ROWS
import polski.exam.data.published.GRAMMAR_ANSWER_PAIR_ROW_ROWS
import polski.exam.data.published.GRAMMAR_RUBRIC_COMPONENT_ROW_ROWS
import polski.exam.data.published.GRAMMAR_BANK_TOKEN_ROW_ROWS
import polski.exam.data.published.GRAMMAR_ITEM_ROW_ROWS
import polski.exam.data.published.GRAMMAR_TEST_ROW_ROWS
import polski.exam.grammar.GrammarBankToken
import polski.exam.grammar.GrammarScoredVariant
import polski.exam.grammar.GrammarAnswerPair
import polski.exam.grammar.GrammarRubricComponent
import polski.exam.grammar.GrammarTest

/** Grammar rows are shared by every host; route IDs come only from publication metadata. */
val officialGrammarData: Map<String, List<GrammarTest>> by lazy {
    val variants = GRAMMAR_ANSWER_VARIANT_ROW_ROWS.groupBy(
        keySelector = { it.itemRef },
        valueTransform = { GrammarScoredVariant(it.answer, it.earnedQuarter, it.onlyIfNotUsedBy) },
    )
    val bankTokens = GRAMMAR_BANK_TOKEN_ROW_ROWS.groupBy(
        keySelector = { it.testRef },
        valueTransform = { GrammarBankToken(it.tokenId, it.position, it.text) },
    )
    val testByItem = GRAMMAR_ITEM_ROW_ROWS.associate { it.itemId to it.testRef }
    val answerPairs = GRAMMAR_ANSWER_PAIR_ROW_ROWS.groupBy(
        keySelector = { pair -> testByItem.getValue(pair.firstItemRef) },
        valueTransform = { pair -> GrammarAnswerPair(
            pair.pairId, pair.firstItemRef, pair.firstAnswer, pair.secondItemRef,
            pair.secondAnswer, pair.firstEarnedQuarter, pair.secondEarnedQuarter,
        ) },
    )
    val rubricComponents = GRAMMAR_RUBRIC_COMPONENT_ROW_ROWS.groupBy(
        keySelector = { it.itemRef },
        valueTransform = { GrammarRubricComponent(it.componentId, it.order, it.creditQuarter, it.criterionText) },
    )
    EXAM_PUBLICATION_ROW_ROWS.associate { publication ->
        publication.routeId to ExamDataMapper.grammarTests(
            tests = GRAMMAR_TEST_ROW_ROWS.filter { it.examRef == publication.examRef },
            items = GRAMMAR_ITEM_ROW_ROWS,
            testId = { it.testId }, testInstruction = { it.instruction },
            itemTestRef = { it.testRef }, itemId = { it.itemId },
            itemType = { it.questionType.tag }, itemContext = { it.context },
            itemGap = { it.gap }, itemOptions = { it.options },
            itemAnswer = { it.correctAnswer }, itemPoints = { it.points }, itemQuarter = { it.pointsQuarter },
            itemGradingMode = { it.gradingMode?.tag },
            bankTokens = bankTokens, itemQuestionTarget = { it.questionTarget }, variants = variants,
            answerPairs = answerPairs, rubricComponents = rubricComponents,
        )
    }
}
