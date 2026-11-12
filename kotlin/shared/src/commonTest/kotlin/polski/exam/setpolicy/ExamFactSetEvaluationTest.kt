package polski.exam.setpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.exam.*
import polski.exam.setpolicy.oracle.*
import polski.exam.listening.ListeningExamScorer
import polski.exam.listening.ListeningQuestion
import polski.exam.reading.ReadingExamScorer
import polski.exam.reading.ReadingQuestion

class ExamFactSetEvaluationTest {
    @Test
    fun sourceAuthoredFactSetCases() {
        for (case in SET_FACT_CASE_ROW_ROWS) {
            val facts = SET_FIXTURE_FACT_ROW_ROWS.filter { it.caseRef == case.id }.map { row ->
                ExamAcceptedFact(
                    id = row.factId,
                    displayText = row.displayText,
                    creditQuarter = row.creditQuarter,
                    aliases = SET_FIXTURE_ALIAS_ROW_ROWS.filter { it.factRef == row.factId }.map { it.answerText },
                )
            }
            val spec = ExamSetAnswerSpec(
                requiredSelections = case.requiredSelections,
                maximumQuarter = case.maximumQuarter,
                facts = facts,
                options = case.options?.split("|") ?: emptyList(),
                gradingMode = ExamGradingMode.fromTag(case.mode.tag),
                overselectionMode = ExamSetOverselectionMode.ManualReview,
            )
            if (case.verifyModelDefault == true) {
                val listeningDefault = ListeningQuestion(
                    "l", QuestionType.Matching, "Choose two", case.options?.split("|").orEmpty(),
                    "A|B|C", case.maximumQuarter, answerMode = ExamAnswerMode.UnorderedSet,
                    requiredSelections = case.requiredSelections, acceptedFacts = facts,
                )
                val readingDefault = ReadingQuestion(
                    "r", "p", QuestionType.Matching, "Choose two", case.options?.split("|").orEmpty(),
                    "A|B|C", case.maximumQuarter, answerMode = ExamAnswerMode.UnorderedSet,
                    requiredSelections = case.requiredSelections, acceptedFacts = facts,
                )
                val override = case.setOverselectionMode?.tag?.let { tag ->
                    if (tag == "manual_review") ExamSetOverselectionMode.ManualReview else ExamSetOverselectionMode.LegacyWrong
                }
                val listening = override?.let { listeningDefault.copy(setOverselectionMode = it) } ?: listeningDefault
                val reading = override?.let { readingDefault.copy(setOverselectionMode = it) } ?: readingDefault
                val expected = ExamAnswerEvaluation(
                    ExamAnswerStatus.entries.single { it.tag == case.expectedStatus?.tag },
                    requireNotNull(case.expectedConfirmedQuarter), requireNotNull(case.expectedPendingQuarter),
                )
                assertEquals(expected, ListeningExamScorer.grade(listening, case.answer), "listening/${case.id}")
                assertEquals(expected, ReadingExamScorer.grade(reading, case.answer), "reading/${case.id}")
            } else if (case.expectIntegrityFailure == true) {
                assertFailsWith<IllegalArgumentException>(case.id) { evaluateFactSetAnswer(spec, case.answer) }
            } else {
                val result = evaluateFactSetAnswer(spec, case.answer)
                assertEquals(case.expectedStatus?.tag, result.status.tag, case.id)
                assertEquals(case.expectedConfirmedQuarter, result.confirmedQuarter, case.id)
                assertEquals(case.expectedPendingQuarter, result.pendingQuarter, case.id)
                if (case.id == "b2-any-two-forward") {
                    val listening = ListeningQuestion("l", QuestionType.ShortAnswer, "Why?", emptyList(),
                        "są duże|są jasne|jest w nich nowy sprzęt", case.maximumQuarter,
                        gradingMode = spec.gradingMode, answerMode = ExamAnswerMode.UnorderedSet,
                        requiredSelections = case.requiredSelections, acceptedFacts = facts)
                    val reading = ReadingQuestion("r", "p", QuestionType.ShortAnswer, "Why?", emptyList(),
                        "są duże|są jasne|jest w nich nowy sprzęt", case.maximumQuarter,
                        gradingMode = spec.gradingMode, answerMode = ExamAnswerMode.UnorderedSet,
                        requiredSelections = case.requiredSelections, acceptedFacts = facts)
                    assertEquals(case.requiredSelections, listening.answerSlotCount)
                    assertEquals(case.requiredSelections, reading.answerSlotCount)
                    assertEquals(result, ListeningExamScorer.grade(listening, case.answer))
                    assertEquals(result, ReadingExamScorer.grade(reading, case.answer))
                    val exactChoices = ReadingQuestion("exact", "p", QuestionType.Matching, "Choose", facts.map { it.displayText },
                        "są duże|są jasne|jest w nich nowy sprzęt", case.maximumQuarter,
                        answerMode = ExamAnswerMode.UnorderedSet, creditPerCorrectQuarter = 2,
                        requiredSelections = case.requiredSelections)
                    assertEquals(case.requiredSelections, exactChoices.answerSlotCount)
                    assertEquals(result, ReadingExamScorer.grade(exactChoices, case.answer))
                }
            }
        }
    }
}
