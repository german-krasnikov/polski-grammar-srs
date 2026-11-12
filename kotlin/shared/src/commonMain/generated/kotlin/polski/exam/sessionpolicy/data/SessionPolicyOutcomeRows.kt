// Generated from dsl/session-policy.data-contract, dsl/exam/session-policy-outcomes.core-data
// source-sha256=14d12cd38e1ad58265d0e26cbc53a57710874c52751d1e1a7f5a0da731eb9f11
// Edit the source, then run biome generate.

package polski.exam.sessionpolicy.data

enum class SessionPolicyOutcomeRowEffect(val tag: String) {
    IGNORE("ignore"),
    SET_DEADLINE("set_deadline"),
    FREEZE("freeze"),
    DISPATCH_PART_ACTION("dispatch_part_action"),
}

data class SessionPolicyOutcomeRow(
    val disposition: String,
    val effect: SessionPolicyOutcomeRowEffect,
)

val SESSION_POLICY_OUTCOME_ROW_ROWS: List<SessionPolicyOutcomeRow> = listOf(
    SessionPolicyOutcomeRow(disposition = "ignore", effect = SessionPolicyOutcomeRowEffect.IGNORE),
    SessionPolicyOutcomeRow(disposition = "set_deadline", effect = SessionPolicyOutcomeRowEffect.SET_DEADLINE),
    SessionPolicyOutcomeRow(disposition = "freeze", effect = SessionPolicyOutcomeRowEffect.FREEZE),
    SessionPolicyOutcomeRow(
        disposition = "dispatch_part_action",
        effect = SessionPolicyOutcomeRowEffect.DISPATCH_PART_ACTION,
    ),
)
