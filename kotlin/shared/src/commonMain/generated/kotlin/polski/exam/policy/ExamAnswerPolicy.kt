// Generated from dsl/exam/answer-policy.logic
// source-sha256=c7a254c6c194ae598aa4f0da1f99321dfca614ee1f36c5496aef6e089108c541
// Edit the source, then run biome generate.

package polski.exam.policy

object Policy {
    interface IDispositionInputs { fun answerNonblank(): Boolean; fun matchesCanonical(): Boolean; fun maximumQuarter(): Double; fun openManual(): Boolean; fun variantQuarter(): Double }
    object DispositionValues { const val Canonical = "canonical"; const val FullVariant = "full_variant"; const val OpenUnknown = "open_unknown"; const val PartialVariant = "partial_variant"; const val Wrong = "wrong" }
    data class DispositionInput(val answerNonblank: Boolean, val matchesCanonical: Boolean, val maximumQuarter: Double, val openManual: Boolean, val variantQuarter: Double)
    fun disposition(input: IDispositionInputs): String {
        val value3 = input.answerNonblank()
        val value0 = input.matchesCanonical()
        val value2 = input.maximumQuarter()
        if (value2.isNaN() || value2.isInfinite() || value2 < 1.0 || value2 > 2147483647.0 || kotlin.math.truncate(value2) != value2 || kotlin.math.abs(value2) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: answer.maximumQuarter")
        val value4 = input.openManual()
        val value1 = input.variantQuarter()
        if (value1.isNaN() || value1.isInfinite() || value1 < 0.0 || value1 > 2147483647.0 || kotlin.math.truncate(value1) != value1 || kotlin.math.abs(value1) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: answer.variantQuarter")
        return Decision_disposition.evaluate(value3, value0, value2, value4, value1)
    }
    fun disposition(input: DispositionInput): String {
        val value3 = input.answerNonblank
        val value0 = input.matchesCanonical
        val value2 = input.maximumQuarter
        if (value2.isNaN() || value2.isInfinite() || value2 < 1.0 || value2 > 2147483647.0 || kotlin.math.truncate(value2) != value2 || kotlin.math.abs(value2) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: answer.maximumQuarter")
        val value4 = input.openManual
        val value1 = input.variantQuarter
        if (value1.isNaN() || value1.isInfinite() || value1 < 0.0 || value1 > 2147483647.0 || kotlin.math.truncate(value1) != value1 || kotlin.math.abs(value1) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: answer.variantQuarter")
        return Decision_disposition.evaluate(value3, value0, value2, value4, value1)
    }
    interface IInputs : IDispositionInputs
    private object Decision_disposition {
        fun evaluate(value3: Boolean, value0: Boolean, value2: Double, value4: Boolean, value1: Double): String {
            // rule: canonical (priority 10)
            if (value0 == true) return "canonical"
            // rule: fullVariant (priority 20)
            if (value1 == value2) return "full_variant"
            // rule: partialVariant (priority 30)
            if (value1 > 0.0) return "partial_variant"
            // rule: openUnknown (priority 40)
            if ((value4 == true) && (value3 == true)) return "open_unknown"
            return "wrong"
        }
    }
}
