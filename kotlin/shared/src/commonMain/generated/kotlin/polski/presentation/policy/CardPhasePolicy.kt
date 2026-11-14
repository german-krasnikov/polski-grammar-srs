// Generated from dsl/learning/card-phase.logic
// source-sha256=785c549ea97290a147967240b0b1fbacd47810bf6f6d6fd3da0b4b1a82c05e17
// Edit the source, then run biome generate.

package polski.presentation.policy

object Policy {
    interface IPhaseInputs { fun dailyExtended(): Boolean; fun dailyHistoryAtPlanned(): Boolean; fun dailyStepIsNull(): Boolean; fun mode(): String; fun nextExerciseIsNull(): Boolean }
    object PhaseValues { const val ChainComplete = "chain_complete"; const val DailyComplete = "daily_complete"; const val NoDue = "no_due"; const val Question = "question" }
    data class PhaseInput(val dailyExtended: Boolean, val dailyHistoryAtPlanned: Boolean, val dailyStepIsNull: Boolean, val mode: String, val nextExerciseIsNull: Boolean)
    fun phase(input: IPhaseInputs): String {
        val value4 = input.dailyExtended()
        val value3 = input.dailyHistoryAtPlanned()
        val value2 = input.dailyStepIsNull()
        val value0 = input.mode()
        if (value0 != "chain" && value0 != "focused" && value0 != "schedule" && value0 != "daily") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: phase.mode")
        val value1 = input.nextExerciseIsNull()
        return Decision_phase.evaluate(value4, value3, value2, value0, value1)
    }
    fun phase(input: PhaseInput): String {
        val value4 = input.dailyExtended
        val value3 = input.dailyHistoryAtPlanned
        val value2 = input.dailyStepIsNull
        val value0 = input.mode
        if (value0 != "chain" && value0 != "focused" && value0 != "schedule" && value0 != "daily") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: phase.mode")
        val value1 = input.nextExerciseIsNull
        return Decision_phase.evaluate(value4, value3, value2, value0, value1)
    }
    interface IInputs : IPhaseInputs
    private object Decision_phase {
        fun evaluate(value4: Boolean, value3: Boolean, value2: Boolean, value0: String, value1: Boolean): String {
            // rule: chainComplete (priority 10)
            if ((value0 == "chain") && (value1 == true)) return "chain_complete"
            // rule: scheduleNoDue (priority 20)
            if ((value0 == "schedule") && (value1 == true)) return "no_due"
            // rule: dailyComplete (priority 30)
            if ((value0 == "daily") && (value1 == true) && (value2 == true) && (value3 == true) && (!(value4 == true))) return "daily_complete"
            // rule: dailyNoDue (priority 40)
            if ((value0 == "daily") && (value1 == true)) return "no_due"
            return "question"
        }
    }
}
