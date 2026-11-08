package polski.exam

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.exam.data.b1y2020.GRAMMAR_ANSWER_VARIANT_ROW_ROWS as B1_GRAMMAR_VARIANTS
import polski.exam.data.b1y2020.GRAMMAR_BANK_TOKEN_ROW_ROWS as B1_GRAMMAR_BANK
import polski.exam.data.b1y2020.GRAMMAR_ITEM_ROW_ROWS as B1_GRAMMAR_ITEMS
import polski.exam.data.b1y2020.GRAMMAR_TEST_ROW_ROWS as B1_GRAMMAR_TESTS
import polski.exam.data.b2y2017.LISTENING_ANSWER_VARIANT_ROW_ROWS as B2_LISTENING_VARIANTS
import polski.exam.data.b2y2017.LISTENING_QUESTION_ROW_ROWS as B2_LISTENING_QUESTIONS
import polski.exam.data.oracle.OPEN_ITEM_GROUP_CASE_ROW_ROWS
import polski.exam.data.oracle.OpenItemGroupCaseRowSection
import polski.exam.grammar.GrammarExamScorer
import polski.exam.grammar.GrammarScoredVariant
import polski.exam.grammar.GrammarBankToken

/** Exercises official open-answer rows through the same mapper and evaluator used by the app. */
class OpenExamSourceRowsTest {
    @Test fun sourceGroupsKeepUnknownAnswersPendingAndBlankAnswersWrong() {
        val grammarVariants = B1_GRAMMAR_VARIANTS.groupBy(
            keySelector = { it.itemRef },
            valueTransform = { GrammarScoredVariant(it.answer, it.earnedQuarter) },
        )
        val grammar = ExamDataMapper.grammarTests(
            tests = B1_GRAMMAR_TESTS,
            items = B1_GRAMMAR_ITEMS,
            testId = { it.testId }, testInstruction = { it.instruction },
            itemTestRef = { it.testRef }, itemId = { it.itemId },
            itemType = { it.questionType.tag }, itemContext = { it.context },
            itemGap = { it.gap }, itemOptions = { it.options },
            itemAnswer = { it.correctAnswer }, itemPoints = { it.points }, itemQuarter = { it.pointsQuarter },
            itemGradingMode = { it.gradingMode?.tag }, variants = grammarVariants,
            bankTokens = B1_GRAMMAR_BANK.groupBy({ it.testRef }, { GrammarBankToken(it.tokenId, it.position, it.text) }),
            itemQuestionTarget = { it.questionTarget },
        )
        val listeningVariants = B2_LISTENING_VARIANTS.groupBy(
            keySelector = { it.questionRef },
            valueTransform = { ExamAnswerVariant(it.answer, it.earnedQuarter) },
        )

        for (case in OPEN_ITEM_GROUP_CASE_ROW_ROWS) {
            when (case.section) {
                OpenItemGroupCaseRowSection.GRAMMAR -> {
                    val items = grammar.single { it.testId == case.groupRef }.items
                    assertEquals(case.expectedItemCount, items.size, case.id)
                    for (item in items) {
                        val unknown = GrammarExamScorer.grade(item, case.unknownAnswer)
                        val blank = GrammarExamScorer.grade(item, " ")
                        assertEquals(case.expectedUnknownStatus.tag, unknown.status.tag, item.itemId)
                        assertEquals(item.maximumQuarter, unknown.pendingQuarter, item.itemId)
                        assertEquals(0, unknown.confirmedQuarter, item.itemId)
                        assertEquals(case.expectedBlankStatus.tag, blank.status.tag, item.itemId)
                        assertEquals(0, blank.pendingQuarter, item.itemId)
                    }
                }
                OpenItemGroupCaseRowSection.LISTENING -> {
                    val questions = B2_LISTENING_QUESTIONS.filter { it.taskRef == case.groupRef }
                    assertEquals(case.expectedItemCount, questions.size, case.id)
                    for (question in questions) {
                        val variants = listeningVariants[question.questionId].orEmpty()
                        val mode = question.gradingMode?.tag?.let(ExamGradingMode::fromTag) ?: ExamGradingMode.Exact
                        val maximum = question.pointsQuarter ?: requireNotNull(question.points) * 4
                        val unknown = evaluateExamAnswer(mode, question.correctAnswer, variants, case.unknownAnswer, maximum)
                        val blank = evaluateExamAnswer(mode, question.correctAnswer, variants, " ", maximum)
                        assertEquals(case.expectedUnknownStatus.tag, unknown.status.tag, question.questionId)
                        assertEquals(maximum, unknown.pendingQuarter, question.questionId)
                        assertEquals(0, unknown.confirmedQuarter, question.questionId)
                        assertEquals(case.expectedBlankStatus.tag, blank.status.tag, question.questionId)
                        assertEquals(0, blank.pendingQuarter, question.questionId)
                        for (variant in variants) {
                            val known = evaluateExamAnswer(mode, question.correctAnswer, variants, variant.answer, maximum)
                            assertEquals(variant.earnedQuarter, known.confirmedQuarter, variant.answer)
                            assertEquals(0, known.pendingQuarter, variant.answer)
                        }
                    }
                }
            }
        }
    }
}
