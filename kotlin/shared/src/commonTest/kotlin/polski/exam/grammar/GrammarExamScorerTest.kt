package polski.exam.grammar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.exam.QuestionType
import polski.exam.data.oracle.SCORING_CASE_ROW_ROWS
import polski.exam.data.oracle.QUARTER_SCORING_CASE_ROW_ROWS

/**
 * Loops [SCORING_CASE_ROW_ROWS] (generated from dsl/exam-scoring/scoring.core-data by biome generate).
 * Adding a scoring test case = one line in the .core-data file (P4: test expectations in DSL).
 */
class GrammarExamScorerTest {
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
}
