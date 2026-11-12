package polski.exam

import polski.exam.partactionpolicy.Policy
import polski.exam.partactionpolicy.data.PART_ACTION_POLICY_OUTCOME_ROW_ROWS
import polski.exam.partactionpolicy.data.PartActionPolicyOutcomeRowMutation

/** Caller derives identity and boundary facts; Logic owns the permission decision. */
internal fun examPartActionPermission(
    kind: String,
    action: String,
    submitted: Boolean,
    validTarget: Boolean = false,
    validSelectedSet: Boolean = false,
    targetInSelectedSet: Boolean = false,
    atFirst: Boolean = false,
    atLast: Boolean = false,
): PartActionPolicyOutcomeRowMutation {
    val disposition = Policy.disposition(
        Policy.DispositionInput(
            kind = kind,
            action = action,
            submitted = submitted,
            validTarget = validTarget,
            validSelectedSet = validSelectedSet,
            targetInSelectedSet = targetInSelectedSet,
            atFirst = atFirst,
            atLast = atLast,
        ),
    )
    return PART_ACTION_POLICY_OUTCOME_ROW_ROWS.single { it.disposition == disposition }.mutation
}
