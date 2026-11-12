// Generated from dsl/answer-policy.data-contract, dsl/exam/answer-policy-outcomes.core-data
// source-sha256=fc0177e9e5132a87ad4fd5f06964b1d65ba8be280a9107dc80b2af8c30d5ae04
// Edit the source, then run biome generate.

package polski.exam.policy.data

enum class AnswerPolicyOutcomeRowStatus(val tag: String) {
    CONFIRMED_FULL("confirmed_full"),
    CONFIRMED_PARTIAL("confirmed_partial"),
    PENDING_MANUAL("pending_manual"),
    WRONG("wrong"),
}

enum class AnswerPolicyOutcomeRowConfirmedSource(val tag: String) {
    MAXIMUM("maximum"),
    VARIANT("variant"),
    ZERO("zero"),
}

enum class AnswerPolicyOutcomeRowPendingSource(val tag: String) {
    MAXIMUM("maximum"),
    ZERO("zero"),
}

data class AnswerPolicyOutcomeRow(
    val disposition: String,
    val status: AnswerPolicyOutcomeRowStatus,
    val confirmedSource: AnswerPolicyOutcomeRowConfirmedSource,
    val pendingSource: AnswerPolicyOutcomeRowPendingSource,
)

val ANSWER_POLICY_OUTCOME_ROW_ROWS: List<AnswerPolicyOutcomeRow> = listOf(
    AnswerPolicyOutcomeRow(
        disposition = "canonical",
        status = AnswerPolicyOutcomeRowStatus.CONFIRMED_FULL,
        confirmedSource = AnswerPolicyOutcomeRowConfirmedSource.MAXIMUM,
        pendingSource = AnswerPolicyOutcomeRowPendingSource.ZERO,
    ),
    AnswerPolicyOutcomeRow(
        disposition = "full_variant",
        status = AnswerPolicyOutcomeRowStatus.CONFIRMED_FULL,
        confirmedSource = AnswerPolicyOutcomeRowConfirmedSource.VARIANT,
        pendingSource = AnswerPolicyOutcomeRowPendingSource.ZERO,
    ),
    AnswerPolicyOutcomeRow(
        disposition = "partial_variant",
        status = AnswerPolicyOutcomeRowStatus.CONFIRMED_PARTIAL,
        confirmedSource = AnswerPolicyOutcomeRowConfirmedSource.VARIANT,
        pendingSource = AnswerPolicyOutcomeRowPendingSource.ZERO,
    ),
    AnswerPolicyOutcomeRow(
        disposition = "open_unknown",
        status = AnswerPolicyOutcomeRowStatus.PENDING_MANUAL,
        confirmedSource = AnswerPolicyOutcomeRowConfirmedSource.ZERO,
        pendingSource = AnswerPolicyOutcomeRowPendingSource.MAXIMUM,
    ),
    AnswerPolicyOutcomeRow(
        disposition = "wrong",
        status = AnswerPolicyOutcomeRowStatus.WRONG,
        confirmedSource = AnswerPolicyOutcomeRowConfirmedSource.ZERO,
        pendingSource = AnswerPolicyOutcomeRowPendingSource.ZERO,
    ),
)
