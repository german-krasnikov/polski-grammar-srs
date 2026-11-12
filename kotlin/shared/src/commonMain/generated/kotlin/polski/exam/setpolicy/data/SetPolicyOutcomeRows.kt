// Generated from dsl/set-policy.data-contract, dsl/exam/set-policy-outcomes.core-data
// source-sha256=949bf37f72196eb6afe01edc298b11c86c44944cb2ae3fe0610194da33bff542
// Edit the source, then run biome generate.

package polski.exam.setpolicy.data

enum class SetPolicyOutcomeRowStatus(val tag: String) {
    CONFIRMED_FULL("confirmed_full"),
    CONFIRMED_PARTIAL("confirmed_partial"),
    PENDING_MANUAL("pending_manual"),
    WRONG("wrong"),
}

enum class SetPolicyOutcomeRowConfirmedSource(val tag: String) {
    MAXIMUM("maximum"),
    KNOWN("known"),
    ZERO("zero"),
}

enum class SetPolicyOutcomeRowPendingSource(val tag: String) {
    MAXIMUM("maximum"),
    REMAINING("remaining"),
    ZERO("zero"),
}

data class SetPolicyOutcomeRow(
    val disposition: String,
    val status: SetPolicyOutcomeRowStatus,
    val confirmedSource: SetPolicyOutcomeRowConfirmedSource,
    val pendingSource: SetPolicyOutcomeRowPendingSource,
)

val SET_POLICY_OUTCOME_ROW_ROWS: List<SetPolicyOutcomeRow> = listOf(
    SetPolicyOutcomeRow(
        disposition = "full",
        status = SetPolicyOutcomeRowStatus.CONFIRMED_FULL,
        confirmedSource = SetPolicyOutcomeRowConfirmedSource.MAXIMUM,
        pendingSource = SetPolicyOutcomeRowPendingSource.ZERO,
    ),
    SetPolicyOutcomeRow(
        disposition = "partial",
        status = SetPolicyOutcomeRowStatus.CONFIRMED_PARTIAL,
        confirmedSource = SetPolicyOutcomeRowConfirmedSource.KNOWN,
        pendingSource = SetPolicyOutcomeRowPendingSource.ZERO,
    ),
    SetPolicyOutcomeRow(
        disposition = "pending",
        status = SetPolicyOutcomeRowStatus.PENDING_MANUAL,
        confirmedSource = SetPolicyOutcomeRowConfirmedSource.KNOWN,
        pendingSource = SetPolicyOutcomeRowPendingSource.REMAINING,
    ),
    SetPolicyOutcomeRow(
        disposition = "wrong",
        status = SetPolicyOutcomeRowStatus.WRONG,
        confirmedSource = SetPolicyOutcomeRowConfirmedSource.ZERO,
        pendingSource = SetPolicyOutcomeRowPendingSource.ZERO,
    ),
    SetPolicyOutcomeRow(
        disposition = "manual_review",
        status = SetPolicyOutcomeRowStatus.PENDING_MANUAL,
        confirmedSource = SetPolicyOutcomeRowConfirmedSource.ZERO,
        pendingSource = SetPolicyOutcomeRowPendingSource.MAXIMUM,
    ),
)
