package polski.exam.grammar

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.exam.ExamAnswerStatus
import polski.exam.ExamGradingMode
import polski.exam.officialGrammarData

/** The B1/2017 key, PDF page 6, also accepts other correct same-meaning sentences in task VII. */
class B12017OpenManualSourceTest {
    @Test
    fun allFiveTransformationsKeepKeyCreditAndReferUnlistedTextToManualReview() {
        val test = officialGrammarData.getValue("b1-2017").single { it.testId.endsWith("grammar-7") }
        assertEquals((1..5).map { "B1-2017-g7-$it" }, test.items.map { it.itemId })
        for (item in test.items) {
            assertEquals(ExamGradingMode.OpenManual, item.gradingMode, item.itemId)
            val keyed = GrammarExamScorer.gradeInAttempt(test, item, mapOf(item.itemId to item.correctAnswer))
            assertEquals(ExamAnswerStatus.ConfirmedFull, keyed.status, item.itemId)
            assertEquals(4, keyed.confirmedQuarter, item.itemId)
            assertEquals(0, keyed.pendingQuarter, item.itemId)

            // The policy cannot decide whether an unlisted sentence preserves meaning.
            val unlisted = GrammarExamScorer.gradeInAttempt(test, item, mapOf(item.itemId to "Inna wersja do oceny."))
            assertEquals(ExamAnswerStatus.PendingManual, unlisted.status, item.itemId)
            assertEquals(0, unlisted.confirmedQuarter, item.itemId)
            assertEquals(4, unlisted.pendingQuarter, item.itemId)

            val blank = GrammarExamScorer.gradeInAttempt(test, item, emptyMap())
            assertEquals(ExamAnswerStatus.Wrong, blank.status, item.itemId)
            assertEquals(0, blank.confirmedQuarter, item.itemId)
            assertEquals(0, blank.pendingQuarter, item.itemId)
        }
    }
}
