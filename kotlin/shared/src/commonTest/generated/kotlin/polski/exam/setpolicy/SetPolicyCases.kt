// Generated from dsl/exam/set-policy.examples by biome generate. Do not edit.
package polski.exam.setpolicy

data class SetPolicyCase(
    val id: String,
    val selectionCount: Int,
    val requiredSelections: Int,
    val distinctKnownFactCount: Int,
    val knownCreditQuarter: Int,
    val maximumQuarter: Int,
    val unknownNonblankCount: Int,
    val hasMalformedSelection: Boolean,
    val answerNonblank: Boolean,
    val openManual: Boolean,
    val usesSourceConservativeOverselection: Boolean,
    val hasPerFactCredit: Boolean,
    val expectedDisposition: String,
)

val SET_POLICY_CASES = listOf(
    SetPolicyCase("legacy-full-weighted", 2, 2, 2, 8, 8, 0, false, true, false, false, true, "full"),
    SetPolicyCase("legacy-partial-wrong-choice", 2, 2, 1, 4, 8, 1, false, true, false, false, true, "partial"),
    SetPolicyCase("legacy-over-limit", 3, 2, 2, 8, 8, 1, false, true, false, false, true, "wrong"),
    SetPolicyCase("legacy-duplicate", 2, 2, 1, 4, 8, 0, true, true, false, false, true, "wrong"),
    SetPolicyCase("legacy-blank", 0, 2, 0, 0, 8, 0, false, false, false, false, true, "wrong"),
    SetPolicyCase("legacy-no-credit-one", 1, 2, 1, 0, 8, 0, false, true, false, false, false, "wrong"),
    SetPolicyCase("legacy-no-credit-full", 2, 2, 2, 0, 8, 0, false, true, false, false, false, "full"),
    SetPolicyCase("legacy-open-mixed", 2, 2, 1, 2, 4, 1, false, true, true, false, true, "pending"),
    SetPolicyCase("b2-any-two-of-three", 2, 2, 2, 4, 4, 0, false, true, true, true, true, "full"),
    SetPolicyCase("b2-one-of-three", 1, 2, 1, 2, 4, 0, false, true, true, true, true, "partial"),
    SetPolicyCase("b2-three-over-limit", 3, 2, 3, 6, 4, 0, false, true, true, true, true, "manual_review"),
    SetPolicyCase("alias-same-fact-twice", 2, 2, 1, 2, 4, 0, false, true, true, true, true, "partial"),
    SetPolicyCase("new-blank", 0, 2, 0, 0, 4, 0, false, false, true, true, true, "wrong"),
    SetPolicyCase("new-open-mixed", 2, 2, 1, 2, 4, 1, false, true, true, true, true, "pending"),
    SetPolicyCase("new-unparseable", 1, 2, 0, 0, 4, 0, true, true, false, true, true, "manual_review"),
    SetPolicyCase("new-unused-empty-slot", 1, 2, 1, 2, 4, 0, false, true, true, true, true, "partial"),
    SetPolicyCase("c1-six-of-twelve", 6, 6, 6, 24, 24, 0, false, true, false, true, true, "full"),
    SetPolicyCase("c1-april-two-by-four", 2, 2, 2, 8, 8, 0, false, true, true, true, true, "full"),
    SetPolicyCase("c1-april-two-by-two", 2, 2, 2, 4, 4, 0, false, true, true, true, true, "full"),
)
