package polski.exam.setpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.exam.setpolicy.data.SET_POLICY_OUTCOME_ROW_ROWS

class ExamSetPolicyTest {
    @Test fun authoredLogicCasesMatchGeneratedPolicy() {
        assertEquals(
            SET_POLICY_CASES.map { it.expectedDisposition }.toSet(),
            SET_POLICY_OUTCOME_ROW_ROWS.map { it.disposition }.toSet(),
        )
        for (case in SET_POLICY_CASES) {
            assertEquals(
                case.expectedDisposition,
                Policy.disposition(
                    Policy.DispositionInput(
                        answerNonblank = case.answerNonblank,
                        distinctKnownFactCount = case.distinctKnownFactCount.toDouble(),
                        hasMalformedSelection = case.hasMalformedSelection,
                        hasPerFactCredit = case.hasPerFactCredit,
                        knownCreditQuarter = case.knownCreditQuarter.toDouble(),
                        maximumQuarter = case.maximumQuarter.toDouble(),
                        openManual = case.openManual,
                        requiredSelections = case.requiredSelections.toDouble(),
                        selectionCount = case.selectionCount.toDouble(),
                        unknownNonblankCount = case.unknownNonblankCount.toDouble(),
                        usesSourceConservativeOverselection = case.usesSourceConservativeOverselection,
                    ),
                ),
                case.id,
            )
        }
    }
}
