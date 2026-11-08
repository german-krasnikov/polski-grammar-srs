package polski.exam.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.exam.ExamAnswerStatus
import polski.exam.ExamGradingMode
import polski.exam.evaluateExamAnswer
import polski.exam.policy.data.ANSWER_POLICY_OUTCOME_ROW_ROWS

class ExamAnswerPolicyTest {
    @Test fun fullIntQuarterDomainRemainsValid() {
        val result = evaluateExamAnswer(
            mode = ExamGradingMode.Exact,
            fullAnswer = "tak",
            variants = emptyList(),
            answer = "tak",
            maximumQuarter = Int.MAX_VALUE,
        )
        assertEquals(ExamAnswerStatus.ConfirmedFull, result.status)
        assertEquals(Int.MAX_VALUE, result.confirmedQuarter)
    }

    @Test fun authoredLogicCasesMatchGeneratedPolicy() {
        assertEquals(
            ANSWER_POLICY_CASES.map { it.expectedDisposition }.toSet(),
            ANSWER_POLICY_OUTCOME_ROW_ROWS.map { it.disposition }.toSet(),
        )
        for (case in ANSWER_POLICY_CASES) {
            assertEquals(
                case.expectedDisposition,
                Policy.disposition(
                    Policy.DispositionInput(
                        answerNonblank = case.answerNonblank,
                        matchesCanonical = case.matchesCanonical,
                        maximumQuarter = case.maximumQuarter.toDouble(),
                        openManual = case.openManual,
                        variantQuarter = case.variantQuarter.toDouble(),
                    ),
                ),
                case.id,
            )
        }
    }
}
