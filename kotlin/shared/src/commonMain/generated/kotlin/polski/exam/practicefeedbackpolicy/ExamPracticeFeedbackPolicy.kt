// Generated from dsl/exam/practice-feedback-policy.logic
// source-sha256=5dba575e31752dfb9aace5d391c8755fe817fc8dcb14391e9a242a85a2753c93
// Edit the source, then run biome generate.

package polski.exam.practicefeedbackpolicy

object Policy {
    interface IDispositionInputs { fun alreadyCommitted(): Boolean; fun answerComplete(): Boolean; fun dependenciesReady(): Boolean; fun event(): String; fun gradeStatus(): String; fun hasNextQuestion(): Boolean; fun mode(): String; fun preferenceEnabled(): Boolean; fun validTarget(): Boolean }
    object DispositionValues { const val CommitDeferred = "commit_deferred"; const val CommitShowAdvance = "commit_show_advance"; const val CommitShowStay = "commit_show_stay"; const val Editable = "editable"; const val Ignore = "ignore" }
    data class DispositionInput(val alreadyCommitted: Boolean, val answerComplete: Boolean, val dependenciesReady: Boolean, val event: String, val gradeStatus: String, val hasNextQuestion: Boolean, val mode: String, val preferenceEnabled: Boolean, val validTarget: Boolean)
    fun disposition(input: IDispositionInputs): String {
        val value5 = input.alreadyCommitted()
        val value4 = input.answerComplete()
        val value6 = input.dependenciesReady()
        val value2 = input.event()
        if (value2 != "choice") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: practice.event")
        val value7 = input.gradeStatus()
        if (value7 != "none" && value7 != "confirmed_full" && value7 != "confirmed_partial" && value7 != "wrong" && value7 != "pending_manual") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: practice.gradeStatus")
        val value8 = input.hasNextQuestion()
        val value0 = input.mode()
        if (value0 != "practice" && value0 != "timed") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: practice.mode")
        val value1 = input.preferenceEnabled()
        val value3 = input.validTarget()
        return Decision_disposition.evaluate(value5, value4, value6, value2, value7, value8, value0, value1, value3)
    }
    fun disposition(input: DispositionInput): String {
        val value5 = input.alreadyCommitted
        val value4 = input.answerComplete
        val value6 = input.dependenciesReady
        val value2 = input.event
        if (value2 != "choice") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: practice.event")
        val value7 = input.gradeStatus
        if (value7 != "none" && value7 != "confirmed_full" && value7 != "confirmed_partial" && value7 != "wrong" && value7 != "pending_manual") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: practice.gradeStatus")
        val value8 = input.hasNextQuestion
        val value0 = input.mode
        if (value0 != "practice" && value0 != "timed") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: practice.mode")
        val value1 = input.preferenceEnabled
        val value3 = input.validTarget
        return Decision_disposition.evaluate(value5, value4, value6, value2, value7, value8, value0, value1, value3)
    }
    interface IInputs : IDispositionInputs
    private object Decision_disposition {
        fun evaluate(value5: Boolean, value4: Boolean, value6: Boolean, value2: String, value7: String, value8: Boolean, value0: String, value1: Boolean, value3: Boolean): String {
            // rule: repeated (priority 10)
            if ((value0 == "practice") && (value1 == true) && (value2 == "choice") && (value5 == true)) return "ignore"
            // rule: deferred (priority 20)
            if ((value0 == "practice") && (value1 == true) && (value2 == "choice") && (value3 == true) && (value4 == true) && (!(value5 == true)) && ((!(value6 == true)) || (value7 == "none"))) return "commit_deferred"
            // rule: advance (priority 30)
            if ((value0 == "practice") && (value1 == true) && (value2 == "choice") && (value3 == true) && (value4 == true) && (!(value5 == true)) && (value6 == true) && (value7 != "pending_manual") && (value7 != "none") && (value8 == true)) return "commit_show_advance"
            // rule: stay (priority 40)
            if ((value0 == "practice") && (value1 == true) && (value2 == "choice") && (value3 == true) && (value4 == true) && (!(value5 == true)) && (value6 == true)) return "commit_show_stay"
            return "editable"
        }
    }
}
