import SwiftUI
import RiveRuntime

/// Decorative Rive rating-effect overlay for the macOS flash card (`Plans/Kotlin/FlipCardRivePlan.md`
/// FC-15/17/20). Never interactive: `.allowsHitTesting(false)` so it can never intercept the flip or
/// swipe gestures underneath, and `.accessibilityHidden(true)` so VoiceOver/assistive tech never
/// lands on it.
///
/// The `RiveViewModel`s (one per distinct vendored `.riv` asset the table in `RiveEffects` names —
/// today `confetti.riv`/`again.riv`, see `THIRD_PARTY/credits.md`) are created lazily, on the first
/// accepted rating, not eagerly at this view's first appearance — mirroring
/// `iosApp/PolskiGrammar/RiveEffectOverlay.swift`, where an XCUITest run caught a real regression
/// from eager creation (a Metal-backed render surface loading on every Question-phase re-render
/// slowed typing enough to drop characters). Deferring creation until the moment it is actually
/// needed keeps the Question/typing path exactly as cheap as it was before this feature.
///
/// Which asset/state-machine/trigger plays for which rating (`RiveEffects`, generated from
/// `dsl/effects/card-effects.core-data`, DSL-MIGRATION-PLAN.md Block A) is no longer written here:
/// this view only walks [RiveEffects.plays(for:)], the same shared table the iOS host reads.
struct RiveEffectOverlay: View {
    let effect: CardEffectEvent?
    let reduceMotion: Bool

    @State private var viewModels: [String: RiveViewModel] = [:]

    var body: some View {
        ZStack {
            ForEach(Array(viewModels.keys), id: \.self) { asset in viewModels[asset]?.view() }
        }
        .allowsHitTesting(false)
        .accessibilityHidden(true)
        // D5: turning off "Анимации" (or system Reduce Motion / Motion.Reduced, which already fold
        // into the same `reduceMotion` the caller passes in) must dispose any Rive view already
        // created, not just skip creating new ones below.
        .onChange(of: reduceMotion) { _, isReduced in
            guard isReduced else { return }
            viewModels = [:]
        }
        .onChange(of: effect) { _, event in
            guard let event, !reduceMotion else { return }
            for play in RiveEffects.plays(for: event.name) {
                let model = viewModels[play.asset] ?? RiveViewModel(fileName: play.asset, stateMachineName: play.stateMachine)
                viewModels[play.asset] = model
                model.triggerInput(play.trigger)
            }
        }
    }
}
