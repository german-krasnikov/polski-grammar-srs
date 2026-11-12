package polski.exam.resultpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import polski.exam.ExamPartScore
import polski.exam.grammar.GrammarExamResult
import polski.exam.resultpolicy.data.RESULT_POLICY_OUTCOME_ROW_ROWS

class ExamResultPolicyTest {
    @Test fun authoredLogicCasesMatchGeneratedPolicy() {
        assertEquals(
            RESULT_POLICY_CASES.map { it.expectedDisposition }.toSet(),
            RESULT_POLICY_OUTCOME_ROW_ROWS.map { it.disposition }.toSet(),
        )
        for (case in RESULT_POLICY_CASES) {
            assertEquals(
                case.expectedDisposition,
                Policy.disposition(
                    Policy.DispositionInput(
                        resultReady = case.resultReady,
                        scope = case.scope,
                        sittingCompleted = case.sittingCompleted,
                        manualPendingQuarter = case.manualPendingQuarter.toDouble(),
                        possibleQuarter = case.possibleQuarter.toDouble(),
                    ),
                ),
                case.id,
            )
        }
    }

    @Test fun zeroDenominatorRemainsFinalWithoutPercentage() {
        val part = ExamPartScore(0, 0, 0)
        assertTrue(part.isFinal)
        assertNull(part.finalPercent)
        val grammar = GrammarExamResult(0, 0, 0.0)
        assertTrue(grammar.isFinal)
        assertEquals(0.0, grammar.percentage)
    }

    @Test fun confirmedCreditWithPendingReviewHasNoFinalPercentage() {
        val part = ExamPartScore(2, 4, 2)
        assertFalse(part.isFinal)
        assertNull(part.finalPercent)
    }
}
