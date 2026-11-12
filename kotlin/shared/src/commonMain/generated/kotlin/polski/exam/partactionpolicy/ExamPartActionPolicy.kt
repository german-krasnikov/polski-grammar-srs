// Generated from dsl/exam/part-action-policy.logic
// source-sha256=de1cb9fc6688f22f1c84d4ab1b34b108ca5bdd44b024bf765b49e1174d1f4487
// Edit the source, then run biome generate.

package polski.exam.partactionpolicy

object Policy {
    interface IDispositionInputs { fun action(): String; fun atFirst(): Boolean; fun atLast(): Boolean; fun kind(): String; fun submitted(): Boolean; fun targetInSelectedSet(): Boolean; fun validSelectedSet(): Boolean; fun validTarget(): Boolean }
    object DispositionValues { const val ApplyAnswer = "apply_answer"; const val Ignore = "ignore"; const val Move = "move"; const val SelectSet = "select_set"; const val Submit = "submit" }
    data class DispositionInput(val action: String, val atFirst: Boolean, val atLast: Boolean, val kind: String, val submitted: Boolean, val targetInSelectedSet: Boolean, val validSelectedSet: Boolean, val validTarget: Boolean)
    fun disposition(input: IDispositionInputs): String {
        val value1 = input.action()
        if (value1 != "answer" && value1 != "select_set" && value1 != "next" && value1 != "previous" && value1 != "submit") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: part.action")
        val value6 = input.atFirst()
        val value7 = input.atLast()
        val value0 = input.kind()
        if (value0 != "grammar" && value0 != "reading" && value0 != "listening" && value0 != "writing") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: part.kind")
        val value2 = input.submitted()
        val value5 = input.targetInSelectedSet()
        val value4 = input.validSelectedSet()
        val value3 = input.validTarget()
        return Decision_disposition.evaluate(value1, value6, value7, value0, value2, value5, value4, value3)
    }
    fun disposition(input: DispositionInput): String {
        val value1 = input.action
        if (value1 != "answer" && value1 != "select_set" && value1 != "next" && value1 != "previous" && value1 != "submit") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: part.action")
        val value6 = input.atFirst
        val value7 = input.atLast
        val value0 = input.kind
        if (value0 != "grammar" && value0 != "reading" && value0 != "listening" && value0 != "writing") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: part.kind")
        val value2 = input.submitted
        val value5 = input.targetInSelectedSet
        val value4 = input.validSelectedSet
        val value3 = input.validTarget
        return Decision_disposition.evaluate(value1, value6, value7, value0, value2, value5, value4, value3)
    }
    interface IInputs : IDispositionInputs
    private object Decision_disposition {
        fun evaluate(value1: String, value6: Boolean, value7: Boolean, value0: String, value2: Boolean, value5: Boolean, value4: Boolean, value3: Boolean): String {
            // rule: answerOther (priority 10)
            if ((!(value2 == true)) && (value1 == "answer") && (value0 != "writing") && (value3 == true)) return "apply_answer"
            // rule: answerWriting (priority 20)
            if ((!(value2 == true)) && (value1 == "answer") && (value0 == "writing") && (value3 == true) && (value5 == true)) return "apply_answer"
            // rule: selectWritingSet (priority 30)
            if ((!(value2 == true)) && (value0 == "writing") && (value1 == "select_set") && (value4 == true)) return "select_set"
            // rule: moveNext (priority 40)
            if ((!(value2 == true)) && (value1 == "next") && (!(value7 == true))) return "move"
            // rule: movePrevious (priority 50)
            if ((!(value2 == true)) && (value1 == "previous") && (!(value6 == true))) return "move"
            // rule: submit (priority 60)
            if ((!(value2 == true)) && (value1 == "submit")) return "submit"
            return "ignore"
        }
    }
}
