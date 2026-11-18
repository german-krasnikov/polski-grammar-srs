// Generated from dsl/learning/error-hunt.logic
// source-sha256=cfc50adff6c6cdae3bb653ac6f35ddb021ea43a7bfd744cad311f0a284cbc272
// Edit the source, then run biome generate.

package polski.cloze.policy

object Policy {
    interface ITapOutcomeInputs { fun tapIsError(): Boolean }
    object TapOutcomeValues { const val Hit = "hit"; const val Miss = "miss" }
    data class TapOutcomeInput(val tapIsError: Boolean)
    fun tapOutcome(input: ITapOutcomeInputs): String {
        val value0 = input.tapIsError()
        return Decision_tapOutcome.evaluate(value0)
    }
    fun tapOutcome(input: TapOutcomeInput): String {
        val value0 = input.tapIsError
        return Decision_tapOutcome.evaluate(value0)
    }
    interface IShouldRevealInputs { fun found(): Boolean; fun missCount(): Double }
    object ShouldRevealValues { const val Hidden = "hidden"; const val Revealed = "revealed" }
    data class ShouldRevealInput(val found: Boolean, val missCount: Double)
    fun shouldReveal(input: IShouldRevealInputs): String {
        val value2 = input.found()
        val value1 = input.missCount()
        if (value1.isNaN() || value1.isInfinite() || value1 < 0.0 || value1 > 2147483647.0 || kotlin.math.truncate(value1) != value1 || kotlin.math.abs(value1) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: hunt.missCount")
        return Decision_shouldReveal.evaluate(value2, value1)
    }
    fun shouldReveal(input: ShouldRevealInput): String {
        val value2 = input.found
        val value1 = input.missCount
        if (value1.isNaN() || value1.isInfinite() || value1 < 0.0 || value1 > 2147483647.0 || kotlin.math.truncate(value1) != value1 || kotlin.math.abs(value1) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: hunt.missCount")
        return Decision_shouldReveal.evaluate(value2, value1)
    }
    interface IScoreClassInputs { fun answerCorrect(): Boolean; fun found(): Boolean; fun missCount(): Double }
    object ScoreClassValues { const val FoundLate = "found_late"; const val Perfect = "perfect"; const val RevealedCorrect = "revealed_correct"; const val WrongForm = "wrong_form" }
    data class ScoreClassInput(val answerCorrect: Boolean, val found: Boolean, val missCount: Double)
    fun scoreClass(input: IScoreClassInputs): String {
        val value3 = input.answerCorrect()
        val value2 = input.found()
        val value1 = input.missCount()
        if (value1.isNaN() || value1.isInfinite() || value1 < 0.0 || value1 > 2147483647.0 || kotlin.math.truncate(value1) != value1 || kotlin.math.abs(value1) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: hunt.missCount")
        return Decision_scoreClass.evaluate(value3, value2, value1)
    }
    fun scoreClass(input: ScoreClassInput): String {
        val value3 = input.answerCorrect
        val value2 = input.found
        val value1 = input.missCount
        if (value1.isNaN() || value1.isInfinite() || value1 < 0.0 || value1 > 2147483647.0 || kotlin.math.truncate(value1) != value1 || kotlin.math.abs(value1) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: hunt.missCount")
        return Decision_scoreClass.evaluate(value3, value2, value1)
    }
    interface IDistractorValidInputs { fun distractorEqualsCorrect(): Boolean; fun distractorInAccepted(): Boolean }
    object DistractorValidValues { const val Invalid = "invalid"; const val Valid = "valid" }
    data class DistractorValidInput(val distractorEqualsCorrect: Boolean, val distractorInAccepted: Boolean)
    fun distractorValid(input: IDistractorValidInputs): String {
        val value4 = input.distractorEqualsCorrect()
        val value5 = input.distractorInAccepted()
        return Decision_distractorValid.evaluate(value4, value5)
    }
    fun distractorValid(input: DistractorValidInput): String {
        val value4 = input.distractorEqualsCorrect
        val value5 = input.distractorInAccepted
        return Decision_distractorValid.evaluate(value4, value5)
    }
    interface IInputs : ITapOutcomeInputs, IShouldRevealInputs, IScoreClassInputs, IDistractorValidInputs
    private object Decision_tapOutcome {
        fun evaluate(value0: Boolean): String {
            // rule: hit (priority 10)
            if (value0 == true) return "hit"
            return "miss"
        }
    }
    private object Decision_shouldReveal {
        fun evaluate(value2: Boolean, value1: Double): String {
            // rule: revealedAfterMisses (priority 10)
            if ((value1 >= 2.0) && (!(value2 == true))) return "revealed"
            // rule: revealedFoundByUser (priority 20)
            if (value2 == true) return "revealed"
            return "hidden"
        }
    }
    private object Decision_scoreClass {
        fun evaluate(value3: Boolean, value2: Boolean, value1: Double): String {
            // rule: perfect (priority 10)
            if ((value2 == true) && (value1 == 0.0) && (value3 == true)) return "perfect"
            // rule: foundLate (priority 20)
            if ((value2 == true) && (value1 > 0.0) && (value3 == true)) return "found_late"
            // rule: revealedCorrect (priority 30)
            if ((!(value2 == true)) && (value3 == true)) return "revealed_correct"
            return "wrong_form"
        }
    }
    private object Decision_distractorValid {
        fun evaluate(value4: Boolean, value5: Boolean): String {
            // rule: valid (priority 10)
            if ((!(value4 == true)) && (!(value5 == true))) return "valid"
            return "invalid"
        }
    }
}
