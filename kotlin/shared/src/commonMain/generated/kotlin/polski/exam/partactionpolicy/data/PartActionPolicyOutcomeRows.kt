// Generated from dsl/part-action-policy.data-contract, dsl/exam/part-action-policy-outcomes.core-data
// source-sha256=487fadab6dd4c913195d371d290d7682ba7edd383fe38eaf583d213abb314cf4
// Edit the source, then run biome generate.

package polski.exam.partactionpolicy.data

enum class PartActionPolicyOutcomeRowMutation(val tag: String) {
    IGNORE("ignore"),
    APPLY_ANSWER("apply_answer"),
    SELECT_SET("select_set"),
    MOVE("move"),
    SUBMIT("submit"),
}

data class PartActionPolicyOutcomeRow(
    val disposition: String,
    val mutation: PartActionPolicyOutcomeRowMutation,
)

val PART_ACTION_POLICY_OUTCOME_ROW_ROWS: List<PartActionPolicyOutcomeRow> = listOf(
    PartActionPolicyOutcomeRow(disposition = "ignore", mutation = PartActionPolicyOutcomeRowMutation.IGNORE),
    PartActionPolicyOutcomeRow(
        disposition = "apply_answer",
        mutation = PartActionPolicyOutcomeRowMutation.APPLY_ANSWER,
    ),
    PartActionPolicyOutcomeRow(disposition = "select_set", mutation = PartActionPolicyOutcomeRowMutation.SELECT_SET),
    PartActionPolicyOutcomeRow(disposition = "move", mutation = PartActionPolicyOutcomeRowMutation.MOVE),
    PartActionPolicyOutcomeRow(disposition = "submit", mutation = PartActionPolicyOutcomeRowMutation.SUBMIT),
)
