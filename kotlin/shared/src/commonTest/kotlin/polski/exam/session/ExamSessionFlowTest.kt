package polski.exam.session

import kotlin.test.*
import polski.exam.*
import polski.exam.grammar.*
import polski.exam.listening.*
import polski.exam.reading.*
import polski.exam.writing.*
import polski.exam.data.p9oracle.GRAMMAR_TEST_ROW_ROWS
import polski.exam.data.p9oracle.GRAMMAR_ITEM_ROW_ROWS
import polski.exam.data.p9oracle.GRAMMAR_ANSWER_PAIR_ROW_ROWS

class ExamSessionFlowTest {
    @Test fun officialPairedAnswersSurviveSessionGrammarMerge() {
        val testId = "certyfikat-polski-C1-2024-04-grammar-1"
        val pairs = GRAMMAR_ANSWER_PAIR_ROW_ROWS.map { row -> GrammarAnswerPair(
            row.pairId, row.firstItemRef, row.firstAnswer, row.secondItemRef, row.secondAnswer,
            row.firstEarnedQuarter, row.secondEarnedQuarter,
        ) }
        val sourceTest = ExamDataMapper.grammarTests(
            tests = GRAMMAR_TEST_ROW_ROWS.filter { it.testId == testId },
            items = GRAMMAR_ITEM_ROW_ROWS,
            testId = { it.testId }, testInstruction = { it.instruction },
            itemTestRef = { it.testRef }, itemId = { it.itemId },
            itemType = { it.questionType.tag }, itemContext = { it.context },
            itemGap = { it.gap }, itemOptions = { it.options },
            itemAnswer = { it.correctAnswer }, itemPoints = { it.points }, itemQuarter = { it.pointsQuarter },
            itemGradingMode = { it.gradingMode?.tag }, answerPairs = mapOf(testId to pairs),
        ).single()
        val base = config()
        val certified = base.copy(
            grammar = listOf(sourceTest),
            modules = base.modules.map { module ->
                if (module.module == ExamModule.Grammar) module.copy(
                    maximumQuarter = sourceTest.items.sumOf { it.maximumQuarter },
                ) else module
            },
        )
        val state = ExamSessionState(certified)
        assertEquals(pairs.size, state.grammar.test.answerPairs.size)
    }

    @Test fun emptyGrammarDoesNotAdvanceOnNext() {
        val grammar = GrammarExamState(GrammarTest("empty", "", emptyList()))
        assertEquals(grammar, reduceGrammarExam(grammar, GrammarExamAction.Next))
    }

    @Test fun emptyReadingDoesNotAdvanceOnNext() {
        val reading = ReadingExamState(ReadingExam(emptyList(), emptyList()))
        assertEquals(reading, reduceReadingExam(reading, ReadingExamAction.Next))
    }

    @Test fun emptyListeningDoesNotAdvanceOnNext() {
        val listening = ListeningExamState(ListeningExam(emptyList(), ""))
        assertEquals(listening, reduceListeningExam(listening, ListeningExamAction.Next))
    }

    private fun config() = ExamSessionConfig(
        "paper", listOf(
            ExamModuleConfig(ExamModule.Listening, 10, 4), ExamModuleConfig(ExamModule.Reading, 20, 4),
            ExamModuleConfig(ExamModule.Grammar, 30, 4), ExamModuleConfig(ExamModule.Writing, 40, 8),
        ),
        ListeningExam(listOf(ListeningTask("lt", "Listen", listOf(ListeningQ("lq", QuestionType.FillGap, "Word", emptyList(), "kot")), 0, 5000)), "audio.mp3"),
        ReadingExam(listOf(ReadingPassage("rp", "Read", "Text")), listOf(ReadingQuestion("rq", "rp", QuestionType.ShortAnswer, "Word", emptyList(), "kot"))),
        listOf(GrammarTest("gt", "Grammar", listOf(GrammarItem("gq", QuestionType.FillGap, "___", null, emptyList(), "kot", 1)))),
        WritingExam(listOf(WritingTask("wq", "Write", "Prompt", null)), 8),
    )

    @Test fun deadlineAndStaleEventsFreezeOnlyOnePart() {
        val initial = ExamSessionState(config())
        val started = reduceExamSession(initial, ExamSessionAction.Start(1_000))
        assertEquals(started, reduceExamSession(started, ExamSessionAction.Start(2_000)))
        val answered = reduceExamSession(started, ExamSessionAction.PartAction(ExamModule.Listening, ExamPartAction.Listening(ListeningExamAction.Answer("lq", "kot")), 10_999))
        val expired = reduceExamSession(answered, ExamSessionAction.PartAction(ExamModule.Listening, ExamPartAction.Listening(ListeningExamAction.Answer("lq", "pies")), 11_000))
        assertEquals("kot", expired.listening.answers["lq"])
        assertEquals(ExamModule.Reading, examSessionCardModel(expired, 11_000).currentModule)
        assertEquals(31_000, expired.deadlineEpochMs)
        assertEquals(expired, reduceExamSession(expired, ExamSessionAction.FinishPart(ExamModule.Listening, 12_000)))
        assertEquals(expired, reduceExamSession(expired, ExamSessionAction.Tick(10_000)))
        assertEquals(4, expired.frozenResults[ExamModule.Listening]?.confirmedQuarter)
    }

    @Test fun staleModuleAtDeadlineDoesNotFreezeCurrentPart() {
        val started = reduceExamSession(ExamSessionState(config()), ExamSessionAction.Start(0))
        assertEquals(started, reduceExamSession(started, ExamSessionAction.FinishPart(ExamModule.Reading, 10_000)))
        val frozen = reduceExamSession(started, ExamSessionAction.Tick(10_000))
        assertEquals(ExamModule.Reading, examSessionCardModel(frozen, 10_000).currentModule)
        assertEquals(30_000, frozen.deadlineEpochMs)
    }

    @Test fun deadlineOverflowFailsExplicitly() {
        assertFailsWith<IllegalArgumentException> {
            reduceExamSession(ExamSessionState(config()), ExamSessionAction.Start(Long.MAX_VALUE))
        }
    }

    @Test fun backwardClockDoesNotShowAnExpiredDeadline() {
        val started = reduceExamSession(ExamSessionState(config()), ExamSessionAction.Start(0))
        val card = examSessionCardModel(started, Long.MIN_VALUE)
        assertTrue(card.remainingMs > 0)
        assertEquals(started, reduceExamSession(started, ExamSessionAction.Tick(Long.MIN_VALUE)))
    }

    @Test fun manualReviewSeparatesOfficialDenominatorAndScoreRange() {
        var state = reduceExamSession(ExamSessionState(config()), ExamSessionAction.Start(0))
        state = reduceExamSession(state, ExamSessionAction.FinishPart(ExamModule.Listening, 1))
        state = reduceExamSession(state, ExamSessionAction.FinishPart(ExamModule.Reading, 2))
        state = reduceExamSession(state, ExamSessionAction.FinishPart(ExamModule.Grammar, 3))
        state = reduceExamSession(state, ExamSessionAction.PartAction(ExamModule.Writing, ExamPartAction.Writing(WritingExamAction.Answer("wq", "draft")), 4))
        state = reduceExamSession(state, ExamSessionAction.FinishPart(ExamModule.Writing, 5))
        val card = examSessionCardModel(state, 5)
        assertTrue(state.completed)
        assertEquals("draft", state.writing.drafts["wq"])
        assertEquals(20, card.possibleQuarter)
        assertEquals(8, card.manualPendingQuarter)
        assertFalse(card.isFinal)
        assertNull(card.finalPercent)
    }

    @Test fun readingSetAndManualAnswerUseQuarterCredits() {
        val exam = ReadingExam(listOf(ReadingPassage("p", "Read", "Text")), listOf(
            ReadingQuestion("set", "p", QuestionType.Matching, "Choose", listOf("A", "B", "C"), "A|C", 8, answerMode = ExamAnswerMode.UnorderedSet, creditPerCorrectQuarter = 4),
            ReadingQuestion("open", "p", QuestionType.ShortAnswer, "Explain", emptyList(), "official", 4, gradingMode = ExamGradingMode.OpenManual),
        ))
        val first = ReadingExamState(exam)
        val partial = reduceReadingExam(first, ReadingExamAction.Answer("set", "C|B"))
        val pending = reduceReadingExam(partial, ReadingExamAction.Answer("open", "other valid answer"))
        val done = reduceReadingExam(pending, ReadingExamAction.Submit)
        assertEquals(4, done.result?.confirmedQuarter)
        assertEquals(4, done.result?.manualPendingQuarter)
        assertNull(done.result?.finalPercent)
        assertEquals(first, reduceReadingExam(first, ReadingExamAction.Answer("unknown", "x")))
        assertEquals(done, reduceReadingExam(done, ReadingExamAction.Answer("set", "A|C")))
        assertEquals(done, reduceReadingExam(done, ReadingExamAction.Submit))
    }

    @Test fun writingTimeoutBeforeSetChoiceStillCompletesWithManualMaximum() {
        val base = config()
        val withSets = base.copy(writing = WritingExam(listOf(
            WritingTask("i", "Write", "First", null, "I"),
            WritingTask("ii", "Write", "Second", null, "II"),
        ), 8))
        var state = reduceExamSession(ExamSessionState(withSets), ExamSessionAction.Start(0))
        state = reduceExamSession(state, ExamSessionAction.FinishPart(ExamModule.Listening, 1))
        state = reduceExamSession(state, ExamSessionAction.FinishPart(ExamModule.Reading, 2))
        state = reduceExamSession(state, ExamSessionAction.FinishPart(ExamModule.Grammar, 3))
        state = reduceExamSession(state, ExamSessionAction.Tick(40_003))
        assertTrue(state.completed)
        assertEquals(8, state.frozenResults[ExamModule.Writing]?.manualPendingQuarter)
        assertTrue(state.writing.submittedTaskIds.isEmpty())
    }

    @Test fun writingSetChangesKeepDraftsAndFreezeSelectedOneOrTwoPrompts() {
        val exam = WritingExam(listOf(
            WritingTask("ia", "Write", "I/a", null, "I"),
            WritingTask("ib", "Write", "I/b", null, "I"),
            WritingTask("iia", "Write", "II/a", null, "II"),
            WritingTask("iib", "Write", "II/b", null, "II"),
            WritingTask("iiia", "Write", "III/a", null, "III"),
        ), 160)
        var state = WritingExamState(exam)
        state = reduceWritingExam(state, WritingExamAction.SelectSet("I"))
        state = reduceWritingExam(state, WritingExamAction.Answer("ia", "first draft"))
        assertEquals(2, writingExamCardModel(state).count)
        state = reduceWritingExam(state, WritingExamAction.SelectSet("III"))
        assertEquals(1, writingExamCardModel(state).count)
        assertEquals(state, reduceWritingExam(state, WritingExamAction.Answer("ia", "invalid for selected set")))
        state = reduceWritingExam(state, WritingExamAction.Answer("iiia", "one prompt"))
        state = reduceWritingExam(state, WritingExamAction.SelectSet("I"))
        assertEquals("first draft", writingExamCardModel(state).draft)
        state = reduceWritingExam(state, WritingExamAction.Submit)
        assertEquals(listOf("ia", "ib"), state.submittedTaskIds)
        assertEquals("one prompt", state.drafts["iiia"])
        assertEquals(160, state.result?.manualPendingQuarter)
        assertEquals(state, reduceWritingExam(state, WritingExamAction.SelectSet("III")))
    }

    @Test fun nullWritingSetClearsSelectionAndKeepsDraft() {
        val exam = WritingExam(listOf(
            WritingTask("a", "Write", "A", null, "I"),
            WritingTask("b", "Write", "B", null, "I"),
        ), 8)
        var state = reduceWritingExam(WritingExamState(exam), WritingExamAction.SelectSet("I"))
        state = reduceWritingExam(state, WritingExamAction.Answer("a", "draft"))
        state = reduceWritingExam(state, WritingExamAction.Next)
        state = reduceWritingExam(state, WritingExamAction.SelectSet(null))
        assertNull(state.selectedSetId)
        assertEquals(0, state.currentIndex)
        assertEquals("draft", state.drafts["a"])
    }

    @Test fun directWritingSubmitWithoutCertifiedMaximumKeepsResultAbsent() {
        val exam = WritingExam(listOf(WritingTask("a", "Write", "Prompt", null)))
        val submitted = reduceWritingExam(WritingExamState(exam), WritingExamAction.Submit)
        assertTrue(submitted.submitted)
        assertEquals(listOf("a"), submitted.submittedTaskIds)
        assertNull(submitted.result)
    }

    @Test fun incompleteOrMismatchedCertificationIsRejected() {
        val valid = config()
        assertFailsWith<IllegalArgumentException> { valid.copy(modules = valid.modules.dropLast(1)) }
        assertFailsWith<IllegalArgumentException> { valid.copy(modules = valid.modules.map { if (it.module == ExamModule.Listening) it.copy(maximumQuarter = 5) else it }) }
        assertFailsWith<IllegalArgumentException> { valid.copy(modules = valid.modules.map { if (it.module == ExamModule.Writing) it.copy(durationSeconds = 0) else it }) }
    }
}
