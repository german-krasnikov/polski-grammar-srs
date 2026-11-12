package polski.exam.partactionpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.exam.partactionpolicy.data.PART_ACTION_POLICY_OUTCOME_ROW_ROWS

class ExamPartActionPolicyTest {
    @Test fun authoredLogicCasesMatchGeneratedPolicy() {
        assertEquals(
            PART_ACTION_POLICY_CASES.map { it.expectedDisposition }.toSet(),
            PART_ACTION_POLICY_OUTCOME_ROW_ROWS.map { it.disposition }.toSet(),
        )
        for (case in PART_ACTION_POLICY_CASES) {
            assertEquals(
                case.expectedDisposition,
                Policy.disposition(
                    Policy.DispositionInput(
                        kind = case.kind,
                        action = case.action,
                        submitted = case.submitted,
                        validTarget = case.validTarget,
                        validSelectedSet = case.validSelectedSet,
                        targetInSelectedSet = case.targetInSelectedSet,
                        atFirst = case.atFirst,
                        atLast = case.atLast,
                    ),
                ),
                case.id,
            )
        }
    }
}
