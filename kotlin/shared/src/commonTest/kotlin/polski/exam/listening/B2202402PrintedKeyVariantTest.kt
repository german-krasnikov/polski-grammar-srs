package polski.exam.listening

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.exam.ExamAnswerStatus
import polski.exam.ExamGradingMode
import polski.exam.officialListeningData

/** The B2/2024-02 key, PDF page 5, uses parentheses and slashes to list literal full answers. */
class B2202402PrintedKeyVariantTest {
    @Test
    fun firstTwoListeningTaskFiveQuestionsConfirmEveryPrintedForm() {
        val questions = officialListeningData.getValue("b2-2024-02").tasks.single { it.id.endsWith("listen-5") }.questions
        val printed = mapOf(
            "certyfikat-polski-B2-2024-02-l5-q1" to listOf(
                "Zapadł się asfalt.", "Na drodze zapadł się asfalt.", "Droga się zapadła.",
            ),
            "certyfikat-polski-B2-2024-02-l5-q2" to listOf(
                "Sieć korytarzy.", "Sieć tuneli.",
                "Sieć korytarzy zbudowanych przez bobry.", "Sieć tuneli zbudowanych przez bobry.",
                "labirynt", "podziemne korytarze", "podziemne tunele",
            ),
        )
        assertEquals(printed.keys, questions.take(2).map { it.id }.toSet())
        for (question in questions.take(2)) {
            assertEquals(ExamGradingMode.OpenManual, question.gradingMode, question.id)
            assertEquals(4, question.pointsQuarter, question.id)
            for (answer in printed.getValue(question.id)) {
                val result = ListeningExamScorer.grade(question, answer)
                assertEquals(ExamAnswerStatus.ConfirmedFull, result.status, "${question.id}: $answer")
                assertEquals(4, result.confirmedQuarter, "${question.id}: $answer")
                assertEquals(0, result.pendingQuarter, "${question.id}: $answer")
            }
            val unknown = ListeningExamScorer.grade(question, "Inna odpowiedź do oceny")
            assertEquals(ExamAnswerStatus.PendingManual, unknown.status, question.id)
            assertEquals(0, unknown.confirmedQuarter, question.id)
            assertEquals(4, unknown.pendingQuarter, question.id)
            val blank = ListeningExamScorer.grade(question, "")
            assertEquals(ExamAnswerStatus.Wrong, blank.status, question.id)
            assertEquals(0, blank.confirmedQuarter, question.id)
            assertEquals(0, blank.pendingQuarter, question.id)
        }
    }
}
