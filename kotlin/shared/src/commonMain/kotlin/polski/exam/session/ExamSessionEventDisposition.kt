package polski.exam.session

import polski.exam.sessionpolicy.Policy
import polski.exam.sessionpolicy.data.SESSION_POLICY_OUTCOME_ROW_ROWS
import polski.exam.sessionpolicy.data.SessionPolicyOutcomeRowEffect

/** Returns the authored effect for one event; time and module identity are supplied by the reducer. */
internal fun examSessionEventDisposition(
    event: String,
    completed: Boolean,
    started: Boolean,
    moduleMatchesCurrent: Boolean,
    deadlineReached: Boolean,
): SessionPolicyOutcomeRowEffect {
    val disposition = Policy.disposition(
        Policy.DispositionInput(
            event = event,
            completed = completed,
            started = started,
            moduleMatchesCurrent = moduleMatchesCurrent,
            deadlineReached = deadlineReached,
        ),
    )
    return SESSION_POLICY_OUTCOME_ROW_ROWS.single { it.disposition == disposition }.effect
}
