package polski.exam.grammar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import polski.exam.QuestionType

class GrammarExamScorerTest {
    private val item = GrammarItem(
        itemId = "q1", questionType = QuestionType.ChooseForm,
        context = "test ___", gap = "___", options = listOf("a", "b", "c"),
        correctAnswer = "nas", points = 1,
    )

    @Test fun exactMatch() = assertTrue(GrammarExamScorer.score(item, "nas"))
    @Test fun caseInsensitive() = assertTrue(GrammarExamScorer.score(item, "Nas"))
    @Test fun trimmed() = assertTrue(GrammarExamScorer.score(item, "  nas  "))
    @Test fun wrong() = assertFalse(GrammarExamScorer.score(item, "nami"))
    @Test fun empty() = assertFalse(GrammarExamScorer.score(item, ""))

    @Test fun fullAttempt() {
        val test = GrammarTest("t1", "instruction", listOf(item, item.copy(itemId = "q2", correctAnswer = "do", points = 2)))
        val result = GrammarExamScorer.scoreAttempt(test, mapOf("q1" to "nas", "q2" to "wrong"))
        assertEquals(1, result.correct)
        assertEquals(3, result.total)
        assertEquals(100.0 / 3, result.percentage, 0.1)
    }

    @Test fun unansweredItemScoresZero() {
        val test = GrammarTest("t1", "instr", listOf(item))
        val result = GrammarExamScorer.scoreAttempt(test, emptyMap())
        assertEquals(0, result.correct)
    }
}
