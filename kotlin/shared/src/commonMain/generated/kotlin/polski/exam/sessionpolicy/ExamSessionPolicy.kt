// Generated from dsl/exam/session-policy.logic
// source-sha256=343512b88fa9cfd0bd04d29eea1c6268a07607d3a6b697ad48d6ab78029b8ca0
// Edit the source, then run biome generate.

package polski.exam.sessionpolicy

object Policy {
    interface IDispositionInputs { fun completed(): Boolean; fun deadlineReached(): Boolean; fun event(): String; fun moduleMatchesCurrent(): Boolean; fun started(): Boolean }
    object DispositionValues { const val DispatchPartAction = "dispatch_part_action"; const val Freeze = "freeze"; const val Ignore = "ignore"; const val SetDeadline = "set_deadline" }
    data class DispositionInput(val completed: Boolean, val deadlineReached: Boolean, val event: String, val moduleMatchesCurrent: Boolean, val started: Boolean)
    fun disposition(input: IDispositionInputs): String {
        val value1 = input.completed()
        val value4 = input.deadlineReached()
        val value0 = input.event()
        if (value0 != "start" && value0 != "part_action" && value0 != "finish_part" && value0 != "tick") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: session.event")
        val value3 = input.moduleMatchesCurrent()
        val value2 = input.started()
        return Decision_disposition.evaluate(value1, value4, value0, value3, value2)
    }
    fun disposition(input: DispositionInput): String {
        val value1 = input.completed
        val value4 = input.deadlineReached
        val value0 = input.event
        if (value0 != "start" && value0 != "part_action" && value0 != "finish_part" && value0 != "tick") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: session.event")
        val value3 = input.moduleMatchesCurrent
        val value2 = input.started
        return Decision_disposition.evaluate(value1, value4, value0, value3, value2)
    }
    interface IInputs : IDispositionInputs
    private object Decision_disposition {
        fun evaluate(value1: Boolean, value4: Boolean, value0: String, value3: Boolean, value2: Boolean): String {
            // rule: start (priority 10)
            if ((!(value1 == true)) && (value0 == "start") && (!(value2 == true))) return "set_deadline"
            // rule: deadline (priority 20)
            if ((!(value1 == true)) && (value2 == true) && (value3 == true) && (value4 == true) && (value0 == "part_action")) return "freeze"
            // rule: finishDeadline (priority 30)
            if ((!(value1 == true)) && (value2 == true) && (value3 == true) && (value4 == true) && (value0 == "finish_part")) return "freeze"
            // rule: tickDeadline (priority 40)
            if ((!(value1 == true)) && (value2 == true) && (value4 == true) && (value0 == "tick")) return "freeze"
            // rule: earlyFinish (priority 50)
            if ((!(value1 == true)) && (value2 == true) && (value3 == true) && (value0 == "finish_part")) return "freeze"
            // rule: dispatch (priority 60)
            if ((!(value1 == true)) && (value2 == true) && (value3 == true) && (value0 == "part_action")) return "dispatch_part_action"
            return "ignore"
        }
    }
}
