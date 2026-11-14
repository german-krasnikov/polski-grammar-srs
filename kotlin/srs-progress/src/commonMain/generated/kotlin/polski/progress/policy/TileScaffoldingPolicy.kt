// Generated from dsl/learning/tile-scaffolding.logic
// source-sha256=e0bd83b43025f5a5d1ac8c96d78c8c4ccb7a5cbfddebb63d6f8872d56da55a1f
// Edit the source, then run biome generate.

package polski.progress.policy

object Policy {
    interface IDispositionInputs { fun correct(): Boolean; fun nextStreak(): Double; fun rung(): String; fun toNone(): Double; fun toPartial(): Double }
    object DispositionValues { const val AlreadyHardest = "already_hardest"; const val PromoteFullToPartial = "promote_full_to_partial"; const val PromotePartialToNone = "promote_partial_to_none"; const val StayFull = "stay_full"; const val StayPartial = "stay_partial"; const val WrongAtFull = "wrong_at_full"; const val WrongAtNone = "wrong_at_none"; const val WrongAtPartial = "wrong_at_partial" }
    data class DispositionInput(val correct: Boolean, val nextStreak: Double, val rung: String, val toNone: Double, val toPartial: Double)
    fun disposition(input: IDispositionInputs): String {
        val value0 = input.correct()
        val value2 = input.nextStreak()
        if (value2.isNaN() || value2.isInfinite() || value2 < 0.0 || value2 > 2147483647.0 || kotlin.math.truncate(value2) != value2 || kotlin.math.abs(value2) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: tile.nextStreak")
        val value1 = input.rung()
        if (value1 != "full" && value1 != "partial" && value1 != "none") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: tile.rung")
        val value4 = input.toNone()
        if (value4.isNaN() || value4.isInfinite() || value4 < 1.0 || value4 > 2147483647.0 || kotlin.math.truncate(value4) != value4 || kotlin.math.abs(value4) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: tile.toNone")
        val value3 = input.toPartial()
        if (value3.isNaN() || value3.isInfinite() || value3 < 1.0 || value3 > 2147483647.0 || kotlin.math.truncate(value3) != value3 || kotlin.math.abs(value3) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: tile.toPartial")
        return Decision_disposition.evaluate(value0, value2, value1, value4, value3)
    }
    fun disposition(input: DispositionInput): String {
        val value0 = input.correct
        val value2 = input.nextStreak
        if (value2.isNaN() || value2.isInfinite() || value2 < 0.0 || value2 > 2147483647.0 || kotlin.math.truncate(value2) != value2 || kotlin.math.abs(value2) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: tile.nextStreak")
        val value1 = input.rung
        if (value1 != "full" && value1 != "partial" && value1 != "none") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: tile.rung")
        val value4 = input.toNone
        if (value4.isNaN() || value4.isInfinite() || value4 < 1.0 || value4 > 2147483647.0 || kotlin.math.truncate(value4) != value4 || kotlin.math.abs(value4) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: tile.toNone")
        val value3 = input.toPartial
        if (value3.isNaN() || value3.isInfinite() || value3 < 1.0 || value3 > 2147483647.0 || kotlin.math.truncate(value3) != value3 || kotlin.math.abs(value3) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: tile.toPartial")
        return Decision_disposition.evaluate(value0, value2, value1, value4, value3)
    }
    interface IInputs : IDispositionInputs
    private object Decision_disposition {
        fun evaluate(value0: Boolean, value2: Double, value1: String, value4: Double, value3: Double): String {
            // rule: wrongAtNone (priority 10)
            if ((!(value0 == true)) && (value1 == "none")) return "wrong_at_none"
            // rule: wrongAtFull (priority 20)
            if ((!(value0 == true)) && (value1 == "full")) return "wrong_at_full"
            // rule: wrongAtPartial (priority 30)
            if ((!(value0 == true)) && (value1 == "partial")) return "wrong_at_partial"
            // rule: promoteFullToPartial (priority 40)
            if ((value1 == "full") && (value2 >= value3)) return "promote_full_to_partial"
            // rule: stayFull (priority 50)
            if (value1 == "full") return "stay_full"
            // rule: promotePartialToNone (priority 60)
            if ((value1 == "partial") && (value2 >= value4)) return "promote_partial_to_none"
            // rule: stayPartial (priority 70)
            if (value1 == "partial") return "stay_partial"
            return "already_hardest"
        }
    }
}
