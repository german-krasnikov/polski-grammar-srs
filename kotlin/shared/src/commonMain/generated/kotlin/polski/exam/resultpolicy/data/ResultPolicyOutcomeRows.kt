// Generated from dsl/result-policy.data-contract, dsl/exam/result-policy-outcomes.core-data
// source-sha256=58edcb2fe930b75a75a8e4767c6d50b6ef3c6622a766d9e3c03f58ebbdfc8c5b
// Edit the source, then run biome generate.

package polski.exam.resultpolicy.data

data class ResultPolicyOutcomeRow(
    val disposition: String,
    val isFinal: Boolean,
    val showFinalPercent: Boolean,
)

val RESULT_POLICY_OUTCOME_ROW_ROWS: List<ResultPolicyOutcomeRow> = listOf(
    ResultPolicyOutcomeRow(disposition = "hidden", isFinal = false, showFinalPercent = false),
    ResultPolicyOutcomeRow(disposition = "provisional", isFinal = false, showFinalPercent = false),
    ResultPolicyOutcomeRow(disposition = "final", isFinal = true, showFinalPercent = true),
    ResultPolicyOutcomeRow(disposition = "final_no_percent", isFinal = true, showFinalPercent = false),
)
