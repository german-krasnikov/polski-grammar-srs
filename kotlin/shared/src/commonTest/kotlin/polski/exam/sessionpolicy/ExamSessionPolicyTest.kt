package polski.exam.sessionpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.exam.sessionpolicy.data.SESSION_POLICY_OUTCOME_ROW_ROWS

class ExamSessionPolicyTest {
    @Test fun authoredLogicCasesMatchGeneratedPolicy() {
        assertEquals(
            SESSION_POLICY_CASES.map { it.expectedDisposition }.toSet(),
            SESSION_POLICY_OUTCOME_ROW_ROWS.map { it.disposition }.toSet(),
        )
        for (case in SESSION_POLICY_CASES) {
            assertEquals(
                case.expectedDisposition,
                Policy.disposition(
                    Policy.DispositionInput(
                        event = case.event,
                        completed = case.completed,
                        started = case.started,
                        moduleMatchesCurrent = case.moduleMatchesCurrent,
                        deadlineReached = case.deadlineReached,
                    ),
                ),
                case.id,
            )
        }
    }
}
