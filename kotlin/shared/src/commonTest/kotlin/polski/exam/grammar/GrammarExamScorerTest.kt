package polski.exam.grammar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.exam.QuestionType
import polski.exam.ExamGradingMode
import polski.exam.data.oracle.SCORING_CASE_ROW_ROWS
import polski.exam.data.oracle.QUARTER_SCORING_CASE_ROW_ROWS
import polski.exam.data.oracle.ANSWER_ATTEMPT_CASE_ROW_ROWS
import polski.exam.data.oracle.GRAMMAR_PAIR_CASE_ROW_ROWS
import polski.exam.data.p9oracle.GRAMMAR_TEST_ROW_ROWS
import polski.exam.data.p9oracle.GRAMMAR_ITEM_ROW_ROWS
import polski.exam.data.p9oracle.GRAMMAR_ANSWER_PAIR_ROW_ROWS
import polski.exam.ExamDataMapper

/**
 * Loops [SCORING_CASE_ROW_ROWS] (generated from dsl/exam-scoring/scoring.core-data by biome generate).
 * Adding a scoring test case = one line in the .core-data file (P4: test expectations in DSL).
 */
class GrammarExamScorerTest {
    @Test fun officialPairOracleUsesCompleteAnswerSnapshot() {
        val testId = "certyfikat-polski-C1-2024-04-grammar-1"
        val pairs = GRAMMAR_ANSWER_PAIR_ROW_ROWS.map { row -> GrammarAnswerPair(
            row.pairId, row.firstItemRef, row.firstAnswer, row.secondItemRef, row.secondAnswer,
            row.firstEarnedQuarter, row.secondEarnedQuarter,
        ) }
        val test = ExamDataMapper.grammarTests(
            tests = GRAMMAR_TEST_ROW_ROWS.filter { it.testId == testId },
            items = GRAMMAR_ITEM_ROW_ROWS,
            testId = { it.testId }, testInstruction = { it.instruction },
            itemTestRef = { it.testRef }, itemId = { it.itemId },
            itemType = { it.questionType.tag }, itemContext = { it.context },
            itemGap = { it.gap }, itemOptions = { it.options },
            itemAnswer = { it.correctAnswer }, itemPoints = { it.points }, itemQuarter = { it.pointsQuarter },
            itemGradingMode = { it.gradingMode?.tag },
            answerPairs = mapOf(testId to pairs),
        ).single()
        val first = test.items.single { it.itemId.endsWith("g1-q8") }
        val second = test.items.single { it.itemId.endsWith("g1-q9") }
        assertEquals(false, GrammarExamScorer.score(first, first.correctAnswer))
        assertEquals("pending_manual", GrammarExamScorer.grade(first, first.correctAnswer).status.tag)
        for (row in GRAMMAR_PAIR_CASE_ROW_ROWS) {
            val answers = mapOf(first.itemId to row.answer8, second.itemId to row.answer9)
            val firstGrade = GrammarExamScorer.gradeInAttempt(test, first, answers)
            val secondGrade = GrammarExamScorer.gradeInAttempt(test, second, answers)
            assertEquals(row.status8.tag, firstGrade.status.tag, row.id)
            assertEquals(row.status9.tag, secondGrade.status.tag, row.id)
            assertEquals(row.confirmed8, firstGrade.confirmedQuarter, row.id)
            assertEquals(row.confirmed9, secondGrade.confirmedQuarter, row.id)
            assertEquals(row.pending8, firstGrade.pendingQuarter, row.id)
            assertEquals(row.pending9, secondGrade.pendingQuarter, row.id)
            val result = GrammarExamScorer.scoreAttempt(test, answers)
            assertEquals(row.confirmed8 + row.confirmed9, result.earnedQuarterPoints, row.id)
            assertEquals(row.pending8 + row.pending9, result.manualPendingQuarterPoints, row.id)
        }
        assertFailsWith<IllegalArgumentException> {
            test.copy(items = test.items.map { if (it.itemId == first.itemId) it.copy(pairedWithItemId = null) else it })
        }
    }

    private val template = GrammarItem(
        itemId = "q1", questionType = QuestionType.ChooseForm,
        context = "test ___", gap = "___", options = listOf("a", "b", "c"),
        correctAnswer = "placeholder", points = 1,
    )

    @Test fun scoringOracle() {
        for (row in SCORING_CASE_ROW_ROWS) {
            val item = template.copy(correctAnswer = row.expected)
            assertEquals(row.correct, GrammarExamScorer.score(item, row.answer), row.id)
        }
    }

    @Test fun fullAttempt() {
        val items = listOf(
            template.copy(itemId = "q1", correctAnswer = "nas", points = 1),
            template.copy(itemId = "q2", correctAnswer = "do", points = 2),
        )
        val test = GrammarTest("t1", "instruction", items)
        val result = GrammarExamScorer.scoreAttempt(test, mapOf("q1" to "nas", "q2" to "wrong"))
        assertEquals(1, result.correct)
        assertEquals(3, result.total)
        assertEquals(100.0 / 3, result.percentage, 0.1)
    }

    @Test fun unansweredItemScoresZero() {
        val test = GrammarTest("t1", "instr", listOf(template.copy(correctAnswer = "nas")))
        val result = GrammarExamScorer.scoreAttempt(test, emptyMap())
        assertEquals(0, result.correct)
    }

    @Test fun officialQuarterPointAnswers() {
        for (row in QUARTER_SCORING_CASE_ROW_ROWS) {
            val item = template.copy(
                correctAnswer = row.fullAnswer,
                points = 0,
                pointsQuarter = row.maximumQuarter,
                scoredVariants = listOf(GrammarScoredVariant(row.partialAnswer, row.partialQuarter)),
            )
            val result = GrammarExamScorer.scoreAttempt(
                GrammarTest("official", "", listOf(item)),
                mapOf(item.itemId to row.answer),
            )
            assertEquals(row.expectedQuarter.toDouble() / row.maximumQuarter * 100.0, result.percentage, 0.001, row.id)
        }
    }

    @Test fun invalidScoredVariantsAreRejected() {
        for (variant in listOf(
            GrammarScoredVariant("", 1),
            GrammarScoredVariant("z kim", 0),
            GrammarScoredVariant("z kim", 3),
            GrammarScoredVariant("z którym", 1),
        )) {
            assertFailsWith<IllegalArgumentException> {
                template.copy(correctAnswer = "z którym", pointsQuarter = 2, scoredVariants = listOf(variant))
            }
        }
    }

    @Test fun provisionalAttemptOracle() {
        for (row in ANSWER_ATTEMPT_CASE_ROW_ROWS) {
            val exact = template.copy(itemId = "exact", correctAnswer = row.exactAnswer, pointsQuarter = row.exactMaximumQuarter)
            val open = template.copy(itemId = "open", correctAnswer = row.openAnswer, pointsQuarter = row.openMaximumQuarter, gradingMode = ExamGradingMode.OpenManual)
            val initial = GrammarExamState(GrammarTest(row.id, "", listOf(exact, open)))
            val answered = reduceGrammarExam(
                reduceGrammarExam(initial, GrammarExamAction.Answer(exact.itemId, row.exactResponse)),
                GrammarExamAction.Answer(open.itemId, row.openResponse),
            )
            val submitted = reduceGrammarExam(answered, GrammarExamAction.Submit)
            val result = requireNotNull(submitted.result)
            assertEquals(row.expectedConfirmedQuarter, result.earnedQuarterPoints, row.id)
            assertEquals(row.expectedPendingQuarter, result.manualPendingQuarterPoints, row.id)
            assertEquals(row.expectedPossibleQuarter, result.possibleQuarterPoints, row.id)
            assertEquals(row.expectedFinal, result.isFinal, row.id)
            assertEquals(row.expectedConfirmedQuarter + row.expectedPendingQuarter, result.upperBoundQuarterPoints, row.id)
            assertEquals(submitted, reduceGrammarExam(submitted, GrammarExamAction.Submit), row.id)
            assertEquals(submitted, reduceGrammarExam(submitted, GrammarExamAction.Answer(open.itemId, "changed")), row.id)
        }
    }
}
