// Generated from dsl/exam/session-policy.examples by biome generate. Do not edit.
package polski.exam.sessionpolicy

data class SessionPolicyCase(
    val id: String,
    val event: String,
    val completed: Boolean,
    val started: Boolean,
    val moduleMatchesCurrent: Boolean,
    val deadlineReached: Boolean,
    val expectedDisposition: String,
)

val SESSION_POLICY_CASES = listOf(
    SessionPolicyCase("first-start", "start", false, false, true, false, "set_deadline"),
    SessionPolicyCase("repeated-start", "start", false, true, true, true, "ignore"),
    SessionPolicyCase("completed-start", "start", true, false, true, false, "ignore"),
    SessionPolicyCase("completed-action", "part_action", true, true, true, true, "ignore"),
    SessionPolicyCase("prestart-action", "part_action", false, false, true, false, "ignore"),
    SessionPolicyCase("prestart-tick", "tick", false, false, true, true, "ignore"),
    SessionPolicyCase("stale-action-before-deadline", "part_action", false, true, false, false, "ignore"),
    SessionPolicyCase("stale-action-at-deadline", "part_action", false, true, false, true, "ignore"),
    SessionPolicyCase("stale-finish-at-deadline", "finish_part", false, true, false, true, "ignore"),
    SessionPolicyCase("matching-action-before-deadline", "part_action", false, true, true, false, "dispatch_part_action"),
    SessionPolicyCase("matching-action-at-deadline", "part_action", false, true, true, true, "freeze"),
    SessionPolicyCase("matching-finish-before-deadline", "finish_part", false, true, true, false, "freeze"),
    SessionPolicyCase("matching-finish-at-deadline", "finish_part", false, true, true, true, "freeze"),
    SessionPolicyCase("tick-before-deadline", "tick", false, true, true, false, "ignore"),
    SessionPolicyCase("tick-at-deadline", "tick", false, true, true, true, "freeze"),
    SessionPolicyCase("tick-after-backward-clock", "tick", false, true, true, false, "ignore"),
    SessionPolicyCase("completed-tick", "tick", true, true, true, true, "ignore"),
)
