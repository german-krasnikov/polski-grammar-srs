// Generated from dsl/effects/card-effects.core-data by derive-rive-effects.mjs — do not edit.
package polski.data.rows.effects

import polski.presentation.CardEffect

/** One Rive trigger to fire on a given state-machine asset. */
data class EffectPlay(val asset: String, val stateMachine: String, val trigger: String)

/** The chain-complete celebration clip: an artboard and a plain animation name, not a state-machine trigger. */
data class ChainCompleteSpec(val asset: String, val artboard: String, val animation: String)

/** The effect -> Rive trigger table, the one source for every host (DSL-MIGRATION-PLAN.md Block A). */
object CardEffectSpecs {
    val remembered: List<EffectPlay> = listOf(
        EffectPlay(asset = "confetti", stateMachine = "State Machine 1", trigger = "Trigger explosion"),
        EffectPlay(asset = "again", stateMachine = "State Machine 1", trigger = "Check"),
    )
    val again: List<EffectPlay> = listOf(
        EffectPlay(asset = "again", stateMachine = "State Machine 1", trigger = "Error"),
    )
    val chainComplete: ChainCompleteSpec = ChainCompleteSpec(asset = "chain-complete", artboard = "Tada", animation = "Reveal")

    /** The plays for [effect], in order; [CardEffect.None] has none. */
    fun plays(effect: CardEffect): List<EffectPlay> = when (effect) {
        CardEffect.Remembered -> remembered
        CardEffect.Again -> again
        CardEffect.None -> emptyList()
    }
}
