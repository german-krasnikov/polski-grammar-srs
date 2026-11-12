// Generated from dsl/exam/result-policy.examples by biome generate. Do not edit.
package polski.exam.resultpolicy

data class ResultPolicyCase(
    val id: String,
    val resultReady: Boolean,
    val scope: String,
    val sittingCompleted: Boolean,
    val manualPendingQuarter: Int,
    val possibleQuarter: Int,
    val expectedDisposition: String,
)

val RESULT_POLICY_CASES = listOf(
    ResultPolicyCase("absent-part", false, "part", false, 0, 4, "hidden"),
    ResultPolicyCase("automatic-part", true, "part", false, 0, 4, "final"),
    ResultPolicyCase("partial-pending", true, "part", false, 2, 4, "provisional"),
    ResultPolicyCase("manual-only", true, "part", false, 4, 4, "provisional"),
    ResultPolicyCase("zero-denominator-part", true, "part", false, 0, 0, "final_no_percent"),
    ResultPolicyCase("incomplete-sitting", true, "sitting", false, 0, 16, "provisional"),
    ResultPolicyCase("completed-sitting", true, "sitting", true, 0, 16, "final"),
    ResultPolicyCase("completed-pending", true, "sitting", true, 2, 16, "provisional"),
    ResultPolicyCase("zero-denominator-sitting", true, "sitting", true, 0, 0, "final_no_percent"),
)
