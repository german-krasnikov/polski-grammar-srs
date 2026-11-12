// Generated from dsl/exam/set-policy.logic
// source-sha256=b97494da7c2c62a21c3e4ee5a909d91efaaef3a18d93ec4858063955c0cbb91a
// Edit the source, then run biome generate.

package polski.exam.setpolicy

object Policy {
    interface IDispositionInputs { fun answerNonblank(): Boolean; fun distinctKnownFactCount(): Double; fun hasMalformedSelection(): Boolean; fun hasPerFactCredit(): Boolean; fun knownCreditQuarter(): Double; fun maximumQuarter(): Double; fun openManual(): Boolean; fun requiredSelections(): Double; fun selectionCount(): Double; fun unknownNonblankCount(): Double; fun usesSourceConservativeOverselection(): Boolean }
    object DispositionValues { const val Full = "full"; const val ManualReview = "manual_review"; const val Partial = "partial"; const val Pending = "pending"; const val Wrong = "wrong" }
    data class DispositionInput(val answerNonblank: Boolean, val distinctKnownFactCount: Double, val hasMalformedSelection: Boolean, val hasPerFactCredit: Boolean, val knownCreditQuarter: Double, val maximumQuarter: Double, val openManual: Boolean, val requiredSelections: Double, val selectionCount: Double, val unknownNonblankCount: Double, val usesSourceConservativeOverselection: Boolean)
    fun disposition(input: IDispositionInputs): String {
        val value7 = input.answerNonblank()
        val value2 = input.distinctKnownFactCount()
        if (value2.isNaN() || value2.isInfinite() || value2 < 0.0 || value2 > 2147483647.0 || kotlin.math.truncate(value2) != value2 || kotlin.math.abs(value2) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: set.distinctKnownFactCount")
        val value6 = input.hasMalformedSelection()
        val value10 = input.hasPerFactCredit()
        val value3 = input.knownCreditQuarter()
        if (value3.isNaN() || value3.isInfinite() || value3 < 0.0 || value3 > 2147483647.0 || kotlin.math.truncate(value3) != value3 || kotlin.math.abs(value3) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: set.knownCreditQuarter")
        val value4 = input.maximumQuarter()
        if (value4.isNaN() || value4.isInfinite() || value4 < 1.0 || value4 > 2147483647.0 || kotlin.math.truncate(value4) != value4 || kotlin.math.abs(value4) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: set.maximumQuarter")
        val value8 = input.openManual()
        val value1 = input.requiredSelections()
        if (value1.isNaN() || value1.isInfinite() || value1 < 1.0 || value1 > 2147483647.0 || kotlin.math.truncate(value1) != value1 || kotlin.math.abs(value1) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: set.requiredSelections")
        val value0 = input.selectionCount()
        if (value0.isNaN() || value0.isInfinite() || value0 < 0.0 || value0 > 2147483647.0 || kotlin.math.truncate(value0) != value0 || kotlin.math.abs(value0) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: set.selectionCount")
        val value5 = input.unknownNonblankCount()
        if (value5.isNaN() || value5.isInfinite() || value5 < 0.0 || value5 > 2147483647.0 || kotlin.math.truncate(value5) != value5 || kotlin.math.abs(value5) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: set.unknownNonblankCount")
        val value9 = input.usesSourceConservativeOverselection()
        return Decision_disposition.evaluate(value7, value2, value6, value10, value3, value4, value8, value1, value0, value5, value9)
    }
    fun disposition(input: DispositionInput): String {
        val value7 = input.answerNonblank
        val value2 = input.distinctKnownFactCount
        if (value2.isNaN() || value2.isInfinite() || value2 < 0.0 || value2 > 2147483647.0 || kotlin.math.truncate(value2) != value2 || kotlin.math.abs(value2) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: set.distinctKnownFactCount")
        val value6 = input.hasMalformedSelection
        val value10 = input.hasPerFactCredit
        val value3 = input.knownCreditQuarter
        if (value3.isNaN() || value3.isInfinite() || value3 < 0.0 || value3 > 2147483647.0 || kotlin.math.truncate(value3) != value3 || kotlin.math.abs(value3) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: set.knownCreditQuarter")
        val value4 = input.maximumQuarter
        if (value4.isNaN() || value4.isInfinite() || value4 < 1.0 || value4 > 2147483647.0 || kotlin.math.truncate(value4) != value4 || kotlin.math.abs(value4) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: set.maximumQuarter")
        val value8 = input.openManual
        val value1 = input.requiredSelections
        if (value1.isNaN() || value1.isInfinite() || value1 < 1.0 || value1 > 2147483647.0 || kotlin.math.truncate(value1) != value1 || kotlin.math.abs(value1) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: set.requiredSelections")
        val value0 = input.selectionCount
        if (value0.isNaN() || value0.isInfinite() || value0 < 0.0 || value0 > 2147483647.0 || kotlin.math.truncate(value0) != value0 || kotlin.math.abs(value0) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: set.selectionCount")
        val value5 = input.unknownNonblankCount
        if (value5.isNaN() || value5.isInfinite() || value5 < 0.0 || value5 > 2147483647.0 || kotlin.math.truncate(value5) != value5 || kotlin.math.abs(value5) > 9007199254740991.0) throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: set.unknownNonblankCount")
        val value9 = input.usesSourceConservativeOverselection
        return Decision_disposition.evaluate(value7, value2, value6, value10, value3, value4, value8, value1, value0, value5, value9)
    }
    interface IInputs : IDispositionInputs
    private object Decision_disposition {
        fun evaluate(value7: Boolean, value2: Double, value6: Boolean, value10: Boolean, value3: Double, value4: Double, value8: Boolean, value1: Double, value0: Double, value5: Double, value9: Boolean): String {
            // rule: sourceMalformed (priority 10)
            if ((value9 == true) && (value7 == true) && (value6 == true)) return "manual_review"
            // rule: sourceOverLimit (priority 20)
            if ((value9 == true) && (value0 > value1)) return "manual_review"
            // rule: legacyMalformed (priority 30)
            if (value6 == true) return "wrong"
            // rule: legacyOverLimit (priority 40)
            if (value0 > value1) return "wrong"
            // rule: weightedFull (priority 50)
            if ((value10 == true) && (value0 == value1) && (value2 == value1) && (value3 == value4)) return "full"
            // rule: unweightedFull (priority 60)
            if ((!(value10 == true)) && (value0 == value1) && (value2 == value1)) return "full"
            // rule: openUnknown (priority 70)
            if ((value8 == true) && (value5 > 0.0)) return "pending"
            // rule: partialCredit (priority 80)
            if (value3 > 0.0) return "partial"
            return "wrong"
        }
    }
}
