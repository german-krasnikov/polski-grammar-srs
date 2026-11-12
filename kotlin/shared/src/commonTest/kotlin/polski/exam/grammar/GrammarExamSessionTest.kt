package polski.exam.grammar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import polski.exam.QuestionType

class GrammarExamSessionTest {
    private val items = listOf(
        GrammarItem("q1", QuestionType.ChooseForm, "ctx1 ___", "___", listOf("a", "b"), "a", 1),
        GrammarItem("q2", QuestionType.FillGap, "ctx2 ___", "___", emptyList(), "word", 1),
    )
    private val test = GrammarTest("t1", "Fill the gaps", items)
    private val initial = GrammarExamState(test)

    @Test fun initialState() {
        assertEquals(0, initial.currentIndex)
        assertTrue(initial.answers.isEmpty())
        assertFalse(initial.submitted)
        assertNull(initial.result)
    }

    @Test fun answerStoresValue() {
        val s = reduceGrammarExam(initial, GrammarExamAction.Answer("q1", "a"))
        assertEquals("a", s.answers["q1"])
    }

    @Test fun unknownItemIdDoesNotEnterAnswerSnapshot() {
        val s = reduceGrammarExam(initial, GrammarExamAction.Answer("missing", "a"))
        assertEquals(initial, s)
    }

    @Test fun nextAndPrevious() {
        val s1 = reduceGrammarExam(initial, GrammarExamAction.Next)
        assertEquals(1, s1.currentIndex)
        val s2 = reduceGrammarExam(s1, GrammarExamAction.Next)
        assertEquals(1, s2.currentIndex)
        val s3 = reduceGrammarExam(s2, GrammarExamAction.Previous)
        assertEquals(0, s3.currentIndex)
    }

    @Test fun submitScores() {
        var s = reduceGrammarExam(initial, GrammarExamAction.Answer("q1", "a"))
        s = reduceGrammarExam(s, GrammarExamAction.Answer("q2", "word"))
        s = reduceGrammarExam(s, GrammarExamAction.Submit)
        assertTrue(s.submitted)
        assertNotNull(s.result)
        assertEquals(2, s.result!!.correct)
    }

    @Test fun submittedStateIgnoresActions() {
        var s = reduceGrammarExam(initial, GrammarExamAction.Submit)
        val frozen = s
        s = reduceGrammarExam(s, GrammarExamAction.Answer("q1", "x"))
        assertEquals(frozen, s)
    }
}
