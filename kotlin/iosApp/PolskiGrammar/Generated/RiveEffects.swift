// Generated from dsl/effects/card-effects.core-data by derive-rive-effects.mjs — do not edit.

/// One Rive trigger to fire on a given state-machine asset.
struct RiveEffectPlay { let asset: String; let stateMachine: String; let trigger: String }

/// The chain-complete celebration clip: an artboard and a plain animation name, not a state-machine trigger.
struct RiveChainComplete { let asset: String; let artboard: String; let animation: String }

/// The effect -> Rive trigger table, the one source for every host (DSL-MIGRATION-PLAN.md Block A).
enum RiveEffects {
    static let remembered: [RiveEffectPlay] = [
        RiveEffectPlay(asset: "confetti", stateMachine: "State Machine 1", trigger: "Trigger explosion"),
        RiveEffectPlay(asset: "again", stateMachine: "State Machine 1", trigger: "Check"),
    ]
    static let again: [RiveEffectPlay] = [
        RiveEffectPlay(asset: "again", stateMachine: "State Machine 1", trigger: "Error"),
    ]
    static let chainComplete = RiveChainComplete(asset: "chain-complete", artboard: "Tada", animation: "Reveal")

    /// The plays for the Kotlin `CardEffect` wire name ("Remembered"/"Again"); anything else (including "None") has none.
    static func plays(for effectName: String) -> [RiveEffectPlay] {
        switch effectName {
        case "Remembered": return remembered
        case "Again": return again
        default: return []
        }
    }
}
