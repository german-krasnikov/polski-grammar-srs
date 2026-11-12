// Generated from dsl/exam/result-policy.logic
// source-sha256=2eaa9901c0c9e9f861654cf067525de94ba6fc42fb8c3fd9a67f8f83a282cc39
// Edit the source, then run biome generate.

package polski.exam.resultpolicy

object Policy {
    interface IDispositionInputs { fun manualPendingQuarter(): Double; fun possibleQuarter(): Double; fun resultReady(): Boolean; fun scope(): String; fun sittingCompleted(): Boolean }
    object DispositionValues { const val Final = "final"; const val FinalNoPercent = "final_no_percent"; const val Hidden = "hidden"; const val Provisional = "provisional" }
    data class DispositionInput(val manualPendingQuarter: Double, val possibleQuarter: Double, val resultReady: Boolean, val scope: String, val sittingCompleted: Boolean)
    fun disposition(input: IDispositionInputs): String {
        val value3 = input.manualPendingQuarter()
        if (value3.isNaN() || value3.isInfinite() || value3 < 0.0 || value3 > 2147483647.0 || kotlin.math.truncate(value3) != value3 || kotlin.math.abs(value3) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: result.manualPendingQuarter")
        val value4 = input.possibleQuarter()
        if (value4.isNaN() || value4.isInfinite() || value4 < 0.0 || value4 > 2147483647.0 || kotlin.math.truncate(value4) != value4 || kotlin.math.abs(value4) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: result.possibleQuarter")
        val value0 = input.resultReady()
        val value1 = input.scope()
        if (value1 != "part" && value1 != "sitting") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: result.scope")
        val value2 = input.sittingCompleted()
        return Decision_disposition.evaluate(value3, value4, value0, value1, value2)
    }
    fun disposition(input: DispositionInput): String {
        val value3 = input.manualPendingQuarter
        if (value3.isNaN() || value3.isInfinite() || value3 < 0.0 || value3 > 2147483647.0 || kotlin.math.truncate(value3) != value3 || kotlin.math.abs(value3) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: result.manualPendingQuarter")
        val value4 = input.possibleQuarter
        if (value4.isNaN() || value4.isInfinite() || value4 < 0.0 || value4 > 2147483647.0 || kotlin.math.truncate(value4) != value4 || kotlin.math.abs(value4) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: result.possibleQuarter")
        val value0 = input.resultReady
        val value1 = input.scope
        if (value1 != "part" && value1 != "sitting") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: result.scope")
        val value2 = input.sittingCompleted
        return Decision_disposition.evaluate(value3, value4, value0, value1, value2)
    }
    interface IInputs : IDispositionInputs
    private object Decision_disposition {
        fun evaluate(value3: Double, value4: Double, value0: Boolean, value1: String, value2: Boolean): String {
            // rule: sittingFinalNoPercent (priority 10)
            if ((value0 == true) && (value1 == "sitting") && (value2 == true) && (value3 == 0.0) && (value4 == 0.0)) return "final_no_percent"
            // rule: sittingFinal (priority 20)
            if ((value0 == true) && (value1 == "sitting") && (value2 == true) && (value3 == 0.0)) return "final"
            // rule: partFinalNoPercent (priority 30)
            if ((value0 == true) && (value1 == "part") && (value3 == 0.0) && (value4 == 0.0)) return "final_no_percent"
            // rule: partFinal (priority 40)
            if ((value0 == true) && (value1 == "part") && (value3 == 0.0)) return "final"
            // rule: available (priority 50)
            if (value0 == true) return "provisional"
            return "hidden"
        }
    }
}
