package polski.exam.grammar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.exam.ExamDataMapper
import polski.exam.data.b2y2020.GRAMMAR_ANSWER_VARIANT_ROW_ROWS
import polski.exam.data.b2y2020.GRAMMAR_ITEM_ROW_ROWS
import polski.exam.data.b2y2020.GRAMMAR_TEST_ROW_ROWS
import polski.exam.data.oracle.GRAMMAR_CONSTRAINT_CASE_ROW_ROWS

class GrammarWordBankConstraintTest {
    private val actualTest by lazy {
        ExamDataMapper.grammarTests(
            tests = GRAMMAR_TEST_ROW_ROWS,
            items = GRAMMAR_ITEM_ROW_ROWS,
            testId = { it.testId }, testInstruction = { it.instruction },
            itemTestRef = { it.testRef }, itemId = { it.itemId },
            itemType = { it.questionType.tag }, itemContext = { it.context },
            itemGap = { it.gap }, itemOptions = { it.options },
            itemAnswer = { it.correctAnswer }, itemPoints = { it.points }, itemQuarter = { it.pointsQuarter },
            itemGradingMode = { it.gradingMode?.tag },
            variants = GRAMMAR_ANSWER_VARIANT_ROW_ROWS.groupBy(
                keySelector = { it.itemRef },
                valueTransform = { GrammarScoredVariant(it.answer, it.earnedQuarter, it.onlyIfNotUsedBy) },
            ),
        ).single { it.testId == "certyfikat-polski-B2-2020-grammar-2" }
    }

    @Test fun officialConditionalAnswerUsesWholeAttempt() {
        val item6 = actualTest.items.single { it.itemId.endsWith("-2-6") }
        val item8 = actualTest.items.single { it.itemId.endsWith("-2-8") }
        for (row in GRAMMAR_CONSTRAINT_CASE_ROW_ROWS) {
            val answers = mapOf(item6.itemId to row.answer6, item8.itemId to row.answer8)
            val result = GrammarExamScorer.scoreAttempt(actualTest, answers)
            assertEquals(row.expected6Quarter, GrammarExamScorer.gradeInAttempt(actualTest, item6, answers).confirmedQuarter, row.id)
            assertEquals(row.expected8Quarter, GrammarExamScorer.gradeInAttempt(actualTest, item8, answers).confirmedQuarter, row.id)
            assertEquals(row.expected6Quarter + row.expected8Quarter, result.earnedQuarterPoints, row.id)
        }
        assertEquals(0, GrammarExamScorer.grade(item8, "w").confirmedQuarter, "Context-free grade must not confirm a conditional key")
    }

    @Test fun conditionalReferenceMustBeAnotherItemOfSameTest() {
        val item6 = actualTest.items.single { it.itemId.endsWith("-2-6") }
        val item8 = actualTest.items.single { it.itemId.endsWith("-2-8") }
        for (badReference in listOf(item8.itemId, "outside-test")) {
            assertFailsWith<IllegalArgumentException>(badReference) {
                GrammarTest("invalid", "", listOf(item6, item8.copy(
                    scoredVariants = listOf(GrammarScoredVariant("w", 2, badReference)),
                )))
            }
        }
    }
}
