package polski.exam

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import polski.exam.grammar.GrammarExamScorer
import polski.exam.grammar.GrammarItem
import polski.exam.grammar.GrammarScoredVariant
import polski.exam.grammar.GrammarTest
import polski.exam.grammar.GrammarBankToken
import polski.exam.grammar.GrammarAnswerPair
import polski.exam.practicefeedbackpolicy.Policy

class ExamPracticeFeedbackTest {
    private val item = GrammarItem("q1", QuestionType.ChooseForm, "___", "___", listOf("bad", "good"), "good", 1)
    private val next = item.copy(itemId = "q2")
    private val test = GrammarTest("t", "instruction", listOf(item, next))

    @Test fun policyNeverAdvancesWithoutAnAvailableGrade() {
        val unavailable = Policy.disposition(Policy.DispositionInput(
            alreadyCommitted = false, answerComplete = true, dependenciesReady = true,
            event = "choice", gradeStatus = "none", hasNextQuestion = true,
            mode = "practice", preferenceEnabled = true, validTarget = true, explicitCheckRequired = false,
        ))
        assertEquals(Policy.DispositionValues.CommitDeferred, unavailable)
    }

    @Test fun firstChoiceIsFrozenAndUsesExistingScorer() {
        val first = acceptGrammarPracticeInput(PracticeFeedbackState(), test, "q1", "bad", enabled = true)
        assertEquals("bad", first.committed.getValue("q1").rawAnswer)
        assertEquals(ExamAnswerStatus.Wrong, first.visible?.evaluation?.status)
        assertEquals(GrammarExamScorer.gradeInAttempt(test, item, mapOf("q1" to "bad")), first.visible?.evaluation)
        assertEquals("good", first.visible?.officialExamples?.single())
        assertTrue(first.visible?.mayAutoAdvance == true)
        assertEquals(first, acceptGrammarPracticeInput(first, test, "q1", "good", enabled = true))
        assertEquals("q2", advancePracticeFocus(first, test.items.map { it.itemId }).focusedQuestionId)
    }

    @Test fun navigationClearsVisibleAndInvalidatesTimerWithoutDiscardingCommit() {
        val first = acceptGrammarPracticeInput(PracticeFeedbackState(), test, "q1", "bad", enabled = true)
        val moved = clearPracticeFeedback(first)
        assertNull(moved.visible)
        assertEquals(first.committed, moved.committed)
        assertTrue(moved.generation > first.generation)
        assertFalse(moved.generation == first.visible?.generation)
        assertTrue(acceptGrammarPracticeInput(PracticeFeedbackState(), test, "foreign", "bad", enabled = true).committed.isEmpty())
        assertTrue(acceptGrammarPracticeInput(PracticeFeedbackState(), test, "q1", "bad", enabled = false).committed.isEmpty())
        assertTrue(acceptGrammarPracticeInput(PracticeFeedbackState(), test, "q1", "bad", enabled = true, timed = true).committed.isEmpty())
    }

    @Test fun finalChoiceShowsVerdictWithoutAdvanceAndExistingAnswersRemainGradingContext() {
        val final = acceptGrammarPracticeInput(
            PracticeFeedbackState(), test, "q2", "bad", enabled = true,
            existingAnswers = mapOf("q1" to "good"),
        )
        assertEquals(ExamAnswerStatus.Wrong, final.visible?.evaluation?.status)
        assertFalse(final.visible?.mayAutoAdvance ?: true)
        assertEquals("q2", advancePracticeFocus(final, test.items.map { it.itemId }).focusedQuestionId)
    }

    @Test fun uncommittedPartnerCannotSettleConditionalAnswer() {
        val constrained = item.copy(scoredVariants = listOf(GrammarScoredVariant("bad", 2, onlyIfNotUsedByItemId = "q2")))
        val constrainedTest = test.copy(items = listOf(constrained, next))
        val feedback = acceptGrammarPracticeInput(
            PracticeFeedbackState(), constrainedTest, "q1", "bad", enabled = true,
            existingAnswers = mapOf("q2" to "bad"),
        )
        assertNull(feedback.visible)
        assertFalse("q1" in feedback.evaluations)
    }

    @Test fun dependencyDefersVerdictAndManualVerdictDoesNotAdvance() {
        val dependent = item.copy(scoredVariants = listOf(GrammarScoredVariant("bad", 2, onlyIfNotUsedByItemId = "q2")))
        val deferred = acceptGrammarPracticeInput(
            PracticeFeedbackState(), test.copy(items = listOf(dependent, next)), "q1", "bad", enabled = true,
        )
        assertEquals("bad", deferred.committed.getValue("q1").rawAnswer)
        assertNull(deferred.visible)

        val manual = item.copy(gradingMode = ExamGradingMode.OpenManual)
        val pending = acceptGrammarPracticeInput(
            PracticeFeedbackState(), test.copy(items = listOf(manual, next)), "q1", "bad", enabled = true,
        )
        assertEquals(ExamAnswerStatus.PendingManual, pending.visible?.evaluation?.status)
        assertFalse(pending.visible?.mayAutoAdvance ?: true)
    }

    @Test fun textWaitsForExplicitCheckAndBlankCheckDoesNotCommit() {
        val textItem = item.copy(questionType = QuestionType.FillGap, options = emptyList())
        val textTest = test.copy(items = listOf(textItem, next))
        val start = PracticeFeedbackState()
        assertEquals(start, acceptGrammarPracticeInput(start, textTest, "q1", "good", enabled = true))
        assertEquals(start, acceptGrammarPracticeInput(start, textTest, "q1", "  ", enabled = true, event = PracticeInputEvent.Check))
        val checked = acceptGrammarPracticeInput(start, textTest, "q1", "good", enabled = true, event = PracticeInputEvent.Check)
        assertEquals("good", checked.committed.getValue("q1").rawAnswer)
        assertEquals(ExamAnswerStatus.ConfirmedFull, checked.visible?.evaluation?.status)
        assertEquals(checked, acceptGrammarPracticeInput(checked, textTest, "q1", "bad", enabled = true, event = PracticeInputEvent.Check))
    }

    @Test fun duplicateBankTextRetainsSelectedTokenIdentity() {
        val bankItem = item.copy(questionType = QuestionType.WordBank, options = emptyList())
        val bank = listOf(GrammarBankToken("one", 1, "good"), GrammarBankToken("two", 2, "good"))
        val bankTest = test.copy(items = listOf(bankItem, next), bankTokens = bank)
        val committed = acceptGrammarPracticeInput(PracticeFeedbackState(), bankTest, "q1", "good", enabled = true,
            selectedTokenId = "two")
        assertEquals("good", committed.committed.getValue("q1").rawAnswer)
        assertEquals("two", committed.committed.getValue("q1").selectedTokenId)
        assertEquals(committed, acceptGrammarPracticeInput(committed, bankTest, "q1", "good", enabled = true,
            selectedTokenId = "one"))
        assertTrue(acceptGrammarPracticeInput(PracticeFeedbackState(), bankTest, "q1", "good", enabled = true,
            selectedTokenId = "foreign").committed.isEmpty())
    }

    @Test fun secondPartnerCommitSettlesBothWithoutChangingFirstRawAnswer() {
        val first = item.copy(questionType = QuestionType.FillGap, options = emptyList(), pairedWithItemId = "q2")
        val second = next.copy(questionType = QuestionType.FillGap, options = emptyList(), pairedWithItemId = "q1")
        val pair = GrammarAnswerPair("pair", "q1", "bad", "q2", "good", 2, 2)
        val pairedTest = GrammarTest("paired", "instruction", listOf(first, second), answerPairs = listOf(pair))
        val deferred = acceptGrammarPracticeInput(PracticeFeedbackState(), pairedTest, "q1", "bad", enabled = true,
            event = PracticeInputEvent.Check)
        assertNull(deferred.visible)
        val settled = acceptGrammarPracticeInput(deferred, pairedTest, "q2", "good", enabled = true,
            event = PracticeInputEvent.Check, existingAnswers = mapOf("q1" to "bad"))
        assertEquals("bad", settled.committed.getValue("q1").rawAnswer)
        assertEquals(ExamAnswerStatus.ConfirmedPartial, settled.evaluations.getValue("q1").status)
        assertEquals(ExamAnswerStatus.ConfirmedPartial, settled.evaluations.getValue("q2").status)
        assertFalse(settled.visible?.mayAutoAdvance ?: true)
    }

    @Test fun conditionalAnswerWaitsForCommittedDependencyEvenWhenDraftExists() {
        val conditional = item.copy(scoredVariants = listOf(GrammarScoredVariant("bad", 2, onlyIfNotUsedByItemId = "q2")))
        val conditionalTest = test.copy(items = listOf(conditional, next))
        val deferred = acceptGrammarPracticeInput(PracticeFeedbackState(), conditionalTest, "q1", "bad", enabled = true,
            existingAnswers = mapOf("q2" to "bad"))
        assertNull(deferred.visible)
        assertFalse("q1" in deferred.evaluations)
        val settled = acceptGrammarPracticeInput(deferred, conditionalTest, "q2", "bad", enabled = true,
            existingAnswers = mapOf("q1" to "bad"))
        assertEquals(ExamAnswerStatus.Wrong, settled.evaluations.getValue("q1").status)
    }
}
