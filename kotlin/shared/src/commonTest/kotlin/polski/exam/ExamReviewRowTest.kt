package polski.exam

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import polski.exam.grammar.GrammarItem
import polski.exam.grammar.GrammarScoredVariant
import polski.exam.grammar.GrammarTest

class ExamReviewRowTest {
    @Test fun frozenAnswersShowChosenBlankPartialVariantAndManualTruthfully() {
        val wrong = GrammarItem("wrong", QuestionType.ChooseForm, "___", "___", listOf("bad", "good"), "good", 1)
        val partial = GrammarItem("partial", QuestionType.FillGap, "___", "___", emptyList(), "canonical", 1,
            scoredVariants = listOf(GrammarScoredVariant("variant", 2)))
        val blank = GrammarItem("blank", QuestionType.FillGap, "___", "___", emptyList(), "answer", 1)
        val manual = GrammarItem("manual", QuestionType.Transform, "___", "___", emptyList(), "model", 1,
            gradingMode = ExamGradingMode.OpenManual)
        val test = GrammarTest("review", "instruction", listOf(wrong, partial, blank, manual))
        val frozen = mapOf("wrong" to "bad", "partial" to "variant", "manual" to "open text")
        val rows = grammarReviewRows(test, frozen)
        assertEquals("bad", rows[0].chosenAnswer)
        assertEquals(ExamAnswerStatus.Wrong, rows[0].evaluation.status)
        assertEquals("variant", rows[1].chosenAnswer)
        assertEquals(listOf("canonical", "variant"), rows[1].officialExamples)
        assertEquals(2, rows[1].evaluation.confirmedQuarter)
        assertNull(rows[2].chosenAnswer)
        assertEquals(ExamAnswerStatus.Wrong, rows[2].evaluation.status)
        assertEquals(ExamAnswerStatus.PendingManual, rows[3].evaluation.status)
        assertEquals(4, rows[3].evaluation.pendingQuarter)
        assertEquals("bad", rows[0].chosenAnswer)
    }
}
