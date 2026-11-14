// Generated from dsl/learning/daily-step.logic
// source-sha256=0e8ce38a35766533a55059061a1aad5dff680b6999fed37d80df69eacf0b6924
// Edit the source, then run biome generate.

package polski.presentation.dailysteppolicy

object Policy {
    interface IKindInputs { fun newCapped(): Boolean; fun newHasCandidate(): Boolean; fun practiceCapped(): Boolean; fun practiceHasCandidate(): Boolean; fun reviewCapped(): Boolean; fun reviewHasCandidate(): Boolean }
    object KindValues { const val New = "new"; const val None = "none"; const val Practice = "practice"; const val Review = "review" }
    data class KindInput(val newCapped: Boolean, val newHasCandidate: Boolean, val practiceCapped: Boolean, val practiceHasCandidate: Boolean, val reviewCapped: Boolean, val reviewHasCandidate: Boolean)
    fun kind(input: IKindInputs): String {
        val value2 = input.newCapped()
        val value3 = input.newHasCandidate()
        val value4 = input.practiceCapped()
        val value5 = input.practiceHasCandidate()
        val value0 = input.reviewCapped()
        val value1 = input.reviewHasCandidate()
        return Decision_kind.evaluate(value2, value3, value4, value5, value0, value1)
    }
    fun kind(input: KindInput): String {
        val value2 = input.newCapped
        val value3 = input.newHasCandidate
        val value4 = input.practiceCapped
        val value5 = input.practiceHasCandidate
        val value0 = input.reviewCapped
        val value1 = input.reviewHasCandidate
        return Decision_kind.evaluate(value2, value3, value4, value5, value0, value1)
    }
    interface IInputs : IKindInputs
    private object Decision_kind {
        fun evaluate(value2: Boolean, value3: Boolean, value4: Boolean, value5: Boolean, value0: Boolean, value1: Boolean): String {
            // rule: review (priority 10)
            if ((!(value0 == true)) && (value1 == true)) return "review"
            // rule: new (priority 20)
            if ((!(value2 == true)) && (value3 == true)) return "new"
            // rule: practice (priority 30)
            if ((!(value4 == true)) && (value5 == true)) return "practice"
            return "none"
        }
    }
}
