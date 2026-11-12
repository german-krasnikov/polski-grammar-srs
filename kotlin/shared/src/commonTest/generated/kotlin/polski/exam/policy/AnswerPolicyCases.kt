// Generated from dsl/exam/answer-policy.examples by biome generate. Do not edit.
package polski.exam.policy

data class AnswerPolicyCase(
    val id: String,
    val matchesCanonical: Boolean,
    val variantQuarter: Int,
    val maximumQuarter: Int,
    val answerNonblank: Boolean,
    val openManual: Boolean,
    val expectedDisposition: String,
)

val ANSWER_POLICY_CASES = listOf(
    AnswerPolicyCase("canonical", true, 0, 4, true, false, "canonical"),
    AnswerPolicyCase("canonical-over-variant", true, 1, 4, true, true, "canonical"),
    AnswerPolicyCase("full-variant", false, 4, 4, true, false, "full_variant"),
    AnswerPolicyCase("quarter-variant", false, 1, 4, true, true, "partial_variant"),
    AnswerPolicyCase("partial-variant", false, 2, 4, true, false, "partial_variant"),
    AnswerPolicyCase("blank-exact", false, 0, 4, false, false, "wrong"),
    AnswerPolicyCase("blank-open", false, 0, 4, false, true, "wrong"),
    AnswerPolicyCase("unknown-exact", false, 0, 4, true, false, "wrong"),
    AnswerPolicyCase("unknown-open", false, 0, 4, true, true, "open_unknown"),
)
