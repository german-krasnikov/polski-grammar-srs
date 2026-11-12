// Generated from dsl/exam/part-action-policy.examples by biome generate. Do not edit.
package polski.exam.partactionpolicy

data class PartActionPolicyCase(
    val id: String,
    val kind: String,
    val action: String,
    val submitted: Boolean,
    val validTarget: Boolean,
    val validSelectedSet: Boolean,
    val targetInSelectedSet: Boolean,
    val atFirst: Boolean,
    val atLast: Boolean,
    val expectedDisposition: String,
)

val PART_ACTION_POLICY_CASES = listOf(
    PartActionPolicyCase("grammar-known", "grammar", "answer", false, true, false, false, true, false, "apply_answer"),
    PartActionPolicyCase("grammar-unknown", "grammar", "answer", false, false, false, false, true, false, "ignore"),
    PartActionPolicyCase("reading-unknown", "reading", "answer", false, false, false, false, true, false, "ignore"),
    PartActionPolicyCase("listening-known", "listening", "answer", false, true, false, false, true, false, "apply_answer"),
    PartActionPolicyCase("writing-active-task", "writing", "answer", false, true, false, true, true, false, "apply_answer"),
    PartActionPolicyCase("writing-inactive-task", "writing", "answer", false, true, false, false, true, false, "ignore"),
    PartActionPolicyCase("writing-unknown-task", "writing", "answer", false, false, false, true, true, false, "ignore"),
    PartActionPolicyCase("writing-valid-set", "writing", "select_set", false, false, true, false, true, false, "select_set"),
    PartActionPolicyCase("writing-null-set", "writing", "select_set", false, false, true, false, true, false, "select_set"),
    PartActionPolicyCase("writing-invalid-set", "writing", "select_set", false, false, false, false, true, false, "ignore"),
    PartActionPolicyCase("nonwriting-set", "grammar", "select_set", false, false, true, false, true, false, "ignore"),
    PartActionPolicyCase("first-previous", "grammar", "previous", false, false, false, false, true, false, "ignore"),
    PartActionPolicyCase("later-previous", "grammar", "previous", false, false, false, false, false, true, "move"),
    PartActionPolicyCase("last-next", "reading", "next", false, false, false, false, false, true, "ignore"),
    PartActionPolicyCase("earlier-next", "reading", "next", false, false, false, false, true, false, "move"),
    PartActionPolicyCase("first-submit", "writing", "submit", false, false, false, false, true, false, "submit"),
    PartActionPolicyCase("repeated-submit", "writing", "submit", true, false, false, false, true, false, "ignore"),
    PartActionPolicyCase("answer-after-submit", "grammar", "answer", true, true, false, false, true, false, "ignore"),
)
