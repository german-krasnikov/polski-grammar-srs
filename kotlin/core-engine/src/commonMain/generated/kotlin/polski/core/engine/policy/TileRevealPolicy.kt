// Generated from dsl/learning/tile-reveal.logic
// source-sha256=8843a84a447132bc9dce6f0eb072a3a49ec7c2a7726098f3a782af29038f2e4c
// Edit the source, then run biome generate.

package polski.core.engine.policy

object Policy {
    interface IDispositionInputs { fun scaffolding(): String }
    object DispositionValues { const val Limited = "limited"; const val Open = "open" }
    data class DispositionInput(val scaffolding: String)
    fun disposition(input: IDispositionInputs): String {
        val value0 = input.scaffolding()
        if (value0 != "full" && value0 != "partial" && value0 != "none") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: reveal.scaffolding")
        return Decision_disposition.evaluate(value0)
    }
    fun disposition(input: DispositionInput): String {
        val value0 = input.scaffolding
        if (value0 != "full" && value0 != "partial" && value0 != "none") throw IllegalArgumentException("LOGIC_INPUT_CONTRACT: reveal.scaffolding")
        return Decision_disposition.evaluate(value0)
    }
    interface IInputs : IDispositionInputs
    private object Decision_disposition {
        fun evaluate(value0: String): String {
            // rule: limited (priority 10)
            if (value0 == "full") return "limited"
            return "open"
        }
    }
}
