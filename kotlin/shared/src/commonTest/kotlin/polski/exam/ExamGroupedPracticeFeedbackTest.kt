package polski.exam

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import polski.exam.reading.ReadingExam
import polski.exam.reading.ReadingPassage
import polski.exam.reading.ReadingQuestion
import polski.exam.reading.ReadingExamScorer
import polski.exam.listening.ListeningExam
import polski.exam.listening.ListeningTask
import polski.exam.listening.ListeningQuestion

class ExamGroupedPracticeFeedbackTest {
    private val passage = ReadingPassage("p", "instruction", "text")
    private val later = ReadingPassage("later", "instruction", "text")
    private val first = ReadingQuestion("q1", "p", QuestionType.MultipleChoice, "one", listOf("bad", "good"), "good")
    private val second = first.copy(id = "q2", questionText = "two")
    private val third = first.copy(id = "q3", questionText = "three")
    private val foreign = first.copy(id = "q4", passageId = "later")
    private val reading = ReadingExam(listOf(passage, later), listOf(first, second, third, foreign))

    @Test fun wrongChoiceLocksFirstAnswerAndFocusesOnlyWithinCurrentGroup() {
        val committed = acceptReadingPracticeInput(PracticeFeedbackState(), reading, "p", "q1", "bad", true)
        assertEquals("bad", committed.committed.getValue("q1").rawAnswer)
        assertEquals(ExamAnswerStatus.Wrong, committed.visible?.evaluation?.status)
        assertEquals(ReadingExamScorer.grade(first, "bad"), committed.visible?.evaluation)
        assertTrue(committed.visible?.mayAutoAdvance == true)
        assertEquals(committed, acceptReadingPracticeInput(committed, reading, "p", "q1", "good", true))
        assertEquals(committed, acceptReadingPracticeInput(committed, reading, "p", "q4", "good", true))
        assertEquals("q2", advanceGroupedPracticeFocus(committed, listOf("q1", "q2", "q3")).focusedQuestionId)
    }

    @Test fun groupedFocusWrapsEarlierGapAndStaysWhenAllAreCommitted() {
        val secondOnly = acceptReadingPracticeInput(PracticeFeedbackState(), reading, "p", "q2", "good", true)
        val thirdThen = acceptReadingPracticeInput(secondOnly, reading, "p", "q3", "good", true)
        assertTrue(thirdThen.visible?.mayAutoAdvance == true)
        assertEquals("q1", advanceGroupedPracticeFocus(thirdThen, listOf("q1", "q2", "q3")).focusedQuestionId)
        val complete = acceptReadingPracticeInput(thirdThen, reading, "p", "q1", "good", true)
        assertFalse(complete.visible?.mayAutoAdvance ?: true)
        assertEquals("q1", advanceGroupedPracticeFocus(complete, listOf("q1", "q2", "q3")).focusedQuestionId)
    }

    @Test fun setDraftCommitsAtSourceSlotCountAndOpenTextNeedsCheck() {
        val set = ReadingQuestion("set", "p", QuestionType.MultipleChoice, "set", listOf("a", "b", "c"), "a|b",
            answerMode = ExamAnswerMode.UnorderedSet)
        val open = ReadingQuestion("open", "p", QuestionType.ShortAnswer, "open", emptyList(), "model",
            gradingMode = ExamGradingMode.OpenManual)
        val exam = ReadingExam(listOf(passage), listOf(set, open))
        val start = PracticeFeedbackState()
        assertEquals(start, acceptReadingPracticeInput(start, exam, "p", "set", "a", true))
        assertEquals(start, acceptReadingPracticeInput(start, exam, "p", "set", "a|b|c", true))
        val settled = acceptReadingPracticeInput(start, exam, "p", "set", "a|c", true)
        assertEquals("a|c", settled.committed.getValue("set").rawAnswer)
        assertEquals(ReadingExamScorer.grade(set, "a|c"), settled.visible?.evaluation)
        assertEquals(start, acceptReadingPracticeInput(start, exam, "p", "open", "draft", true))
        assertEquals(start, acceptReadingPracticeInput(start, exam, "p", "open", "  ", true, event = PracticeInputEvent.Check))
        val checked = acceptReadingPracticeInput(start, exam, "p", "open", "draft", true,
            event = PracticeInputEvent.Check)
        assertEquals(ExamAnswerStatus.PendingManual, checked.visible?.evaluation?.status)
        assertFalse(checked.visible?.mayAutoAdvance ?: true)
    }

    @Test fun listeningCurrentTaskAndTimedOffDoNotCommit() {
        val q1 = ListeningQuestion("l1", QuestionType.MultipleChoice, "one", listOf("bad", "good"), "good")
        val q2 = q1.copy(id = "l2")
        val q3 = q1.copy(id = "l3")
        val exam = ListeningExam(listOf(ListeningTask("t1", "task", listOf(q1, q2)),
            ListeningTask("t2", "task", listOf(q3))), "audio.mp3")
        val start = PracticeFeedbackState()
        assertEquals(start, acceptListeningPracticeInput(start, exam, "t1", "l3", "bad", true))
        assertEquals(start, acceptListeningPracticeInput(start, exam, "t1", "l1", "bad", false))
        assertEquals(start, acceptListeningPracticeInput(start, exam, "t1", "l1", "bad", true, timed = true))
        val committed = acceptListeningPracticeInput(start, exam, "t1", "l1", "bad", true)
        assertEquals(ExamAnswerStatus.Wrong, committed.visible?.evaluation?.status)
        assertEquals("l2", advanceGroupedPracticeFocus(committed, listOf("l1", "l2")).focusedQuestionId)
        assertNull(clearPracticeFeedback(committed).visible)
    }

    @Test fun frozenReviewsPreserveChosenBlankVariantsAndPending() {
        val variant = first.copy(id = "variant", scoredVariants = listOf(ExamAnswerVariant("alternate", 2)))
        val manual = first.copy(id = "manual", type = QuestionType.ShortAnswer, options = emptyList(), gradingMode = ExamGradingMode.OpenManual)
        val read = ReadingExam(listOf(passage), listOf(variant, manual))
        val frozen = mutableMapOf("variant" to "alternate", "manual" to "draft")
        val reviews = readingReviewRows(read, frozen)
        frozen["variant"] = "wrong later"
        assertEquals("alternate", reviews.first().chosenAnswer)
        assertEquals(listOf("good", "alternate"), reviews.first().officialExamples)
        assertEquals(2, reviews.first().evaluation.confirmedQuarter)
        assertEquals(ExamAnswerStatus.PendingManual, reviews.last().evaluation.status)
        val listen = ListeningExam(listOf(ListeningTask("t", "task", listOf(
            ListeningQuestion("a", QuestionType.MultipleChoice, "one", listOf("bad", "good"), "good"),
            ListeningQuestion("b", QuestionType.MultipleChoice, "two", listOf("bad", "good"), "good"),
            ListeningQuestion("c", QuestionType.FillGap, "three", emptyList(), "canonical",
                scoredVariants = listOf(ExamAnswerVariant("variant", 2))),
            ListeningQuestion("d", QuestionType.FillGap, "four", emptyList(), "model",
                gradingMode = ExamGradingMode.OpenManual),
        ))), "audio.mp3")
        val listeningRows = listeningReviewRows(listen, mapOf("a" to "bad", "c" to "variant", "d" to "free text"))
        assertEquals("bad", listeningRows.first().chosenAnswer)
        assertEquals(ExamAnswerStatus.Wrong, listeningRows.first().evaluation.status)
        assertNull(listeningRows[1].chosenAnswer)
        assertEquals(listOf("canonical", "variant"), listeningRows[2].officialExamples)
        assertEquals(2, listeningRows[2].evaluation.confirmedQuarter)
        assertEquals(ExamAnswerStatus.PendingManual, listeningRows[3].evaluation.status)
    }
}
