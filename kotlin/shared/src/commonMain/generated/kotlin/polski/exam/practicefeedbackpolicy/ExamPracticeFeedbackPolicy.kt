// Generated from dsl/exam/practice-feedback-policy.logic
// source-sha256=4c7aa4d9f355bcdbf386b999e96b7e069d499d6dfde69aa08f289475b3dd4dc4
// Edit the source, then run biome generate.

package polski.exam.practicefeedbackpolicy

object Policy {
    interface IDispositionInputs { fun alreadyCommitted(): Boolean; fun answerComplete(): Boolean; fun dependenciesReady(): Boolean; fun event(): String; fun explicitCheckRequired(): Boolean; fun gradeStatus(): String; fun hasNextQuestion(): Boolean; fun mode(): String; fun preferenceEnabled(): Boolean; fun validTarget(): Boolean }
    object DispositionValues { const val CommitDeferred = "commit_deferred"; const val CommitShowAdvance = "commit_show_advance"; const val CommitShowStay = "commit_show_stay"; const val Editable = "editable"; const val Ignore = "ignore" }
    data class DispositionInput(val alreadyCommitted: Boolean, val answerComplete: Boolean, val dependenciesReady: Boolean, val event: String, val explicitCheckRequired: Boolean, val gradeStatus: String, val hasNextQuestion: Boolean, val mode: String, val preferenceEnabled: Boolean, val validTarget: Boolean)
    fun disposition(input: IDispositionInputs): String {
        val value6 = input.alreadyCommitted()
        val value5 = input.answerComplete()
        val value7 = input.dependenciesReady()
        val value2 = input.event()
        if (value2 != "choice" && value2 != "check") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: practice.event")
        val value3 = input.explicitCheckRequired()
        val value8 = input.gradeStatus()
        if (value8 != "none" && value8 != "confirmed_full" && value8 != "confirmed_partial" && value8 != "wrong" && value8 != "pending_manual") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: practice.gradeStatus")
        val value9 = input.hasNextQuestion()
        val value0 = input.mode()
        if (value0 != "practice" && value0 != "timed") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: practice.mode")
        val value1 = input.preferenceEnabled()
        val value4 = input.validTarget()
        return Decision_disposition.evaluate(value6, value5, value7, value2, value3, value8, value9, value0, value1, value4)
    }
    fun disposition(input: DispositionInput): String {
        val value6 = input.alreadyCommitted
        val value5 = input.answerComplete
        val value7 = input.dependenciesReady
        val value2 = input.event
        if (value2 != "choice" && value2 != "check") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: practice.event")
        val value3 = input.explicitCheckRequired
        val value8 = input.gradeStatus
        if (value8 != "none" && value8 != "confirmed_full" && value8 != "confirmed_partial" && value8 != "wrong" && value8 != "pending_manual") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: practice.gradeStatus")
        val value9 = input.hasNextQuestion
        val value0 = input.mode
        if (value0 != "practice" && value0 != "timed") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: practice.mode")
        val value1 = input.preferenceEnabled
        val value4 = input.validTarget
        return Decision_disposition.evaluate(value6, value5, value7, value2, value3, value8, value9, value0, value1, value4)
    }
    interface IInputs : IDispositionInputs
    private object Decision_disposition {
        fun evaluate(value6: Boolean, value5: Boolean, value7: Boolean, value2: String, value3: Boolean, value8: String, value9: Boolean, value0: String, value1: Boolean, value4: Boolean): String {
            // rule: repeated (priority 10)
            if ((value0 == "practice") && (value1 == true) && (value6 == true)) return "ignore"
            // rule: deferred (priority 20)
            if ((value0 == "practice") && (value1 == true) && (value4 == true) && (value5 == true) && (!(value6 == true)) && ((value2 == "choice") || (value2 == "check")) && ((value2 == "check") || (!(value3 == true))) && ((!(value7 == true)) || (value8 == "none"))) return "commit_deferred"
            // rule: advance (priority 30)
            if ((value0 == "practice") && (value1 == true) && (value4 == true) && (value5 == true) && (!(value6 == true)) && ((value2 == "choice") || (value2 == "check")) && ((value2 == "check") || (!(value3 == true))) && (value7 == true) && (value8 != "pending_manual") && (value8 != "none") && (value9 == true)) return "commit_show_advance"
            // rule: stay (priority 40)
            if ((value0 == "practice") && (value1 == true) && (value4 == true) && (value5 == true) && (!(value6 == true)) && ((value2 == "choice") || (value2 == "check")) && ((value2 == "check") || (!(value3 == true))) && (value7 == true)) return "commit_show_stay"
            return "editable"
        }
    }
}
