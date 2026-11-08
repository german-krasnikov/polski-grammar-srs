import SwiftUI
import RiveRuntime
#if DEBUG
import os

/// Shares `flipSignpostLog`'s subsystem/category (`FlashCardView.swift`) under a name distinct
/// from that file's own private `let` (Swift file-scoping means a same-named `private let` in each
/// file is fine, but a shared name reads confusingly across files) — FC2-17, R4: lets
/// `xctrace`/`XCTOSSignpostMetric` isolate Rive playback cost from flip cost in the same trace.
private let riveSignpostLog = OSLog(subsystem: "dev.polski.grammarmatrix", category: "flip")
#endif

/// Decorative Rive rating-effect overlay for the iOS flash card (`Plans/Kotlin/FlipCardRivePlan.md`
/// FC-15/17/20, FC2-09/17). Never interactive: `.allowsHitTesting(false)` so it can never intercept
/// the flip or swipe gestures underneath, and `.accessibilityHidden(true)` so VoiceOver never lands
/// on it.
///
/// The `RiveViewModel`s (one per distinct vendored `.riv` asset the table in `RiveEffects` names —
/// today `confetti.riv`/`again.riv`, see `THIRD_PARTY/credits.md`) are created lazily, on the first
/// accepted rating, not eagerly at this view's first appearance. Loading a `.riv` file and mounting
/// its Metal-backed render surface has real per-frame cost; creating it while the user is still just
/// reading or typing the Question side of a card (i.e. always, since this overlay sits over the card
/// from the start) measurably slowed down every keystroke's re-render and caused an XCUITest run to
/// drop characters mid-type — a real, reproduced regression, not a hypothetical one. Deferring
/// creation until the moment it is actually needed (mirroring the web host's own lazy-load of the
/// Rive runtime, per the plan's §8 evidence log) keeps the Question/typing path exactly as cheap as
/// it was before this feature.
///
/// Which asset/state-machine/trigger plays for which rating (`RiveEffects`, generated from
/// `dsl/effects/card-effects.core-data`, DSL-MIGRATION-PLAN.md Block A) is no longer written here:
/// this view only walks [RiveEffects.plays(for:)] and fires whatever it lists, keyed by asset name
/// so `Remembered`'s `confetti`+`again` pairing (`FlipCardRivePlan.md` §12.7, FC2-09 Pick A) and
/// `Again`'s lone trigger both fall out of the same loop.
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
        .onChange(of: effect) { _, event in
            guard let event, !reduceMotion else { return }
            #if DEBUG
            let signpostID = OSSignpostID(log: riveSignpostLog)
            os_signpost(.begin, log: riveSignpostLog, name: "RiveRatingEffect", signpostID: signpostID)
            #endif
            for play in RiveEffects.plays(for: event.name) {
                let model = viewModels[play.asset] ?? RiveViewModel(fileName: play.asset, stateMachineName: play.stateMachine)
                viewModels[play.asset] = model
                model.triggerInput(play.trigger)
            }
            #if DEBUG
            os_signpost(.end, log: riveSignpostLog, name: "RiveRatingEffect", signpostID: signpostID)
            #endif
        }
    }
}

/// One-shot chain-completion celebration (`Plans/Kotlin/FlipCardRivePlan.md` FC2-10, R3 Pick C):
/// plays only `chain-complete.riv`'s "Tada" artboard, "Reveal" animation — a plain named animation,
/// not a state-machine trigger — autoplaying once as soon as this view exists. The caller mounts it
/// only while `CardPhase.ChainComplete` is showing, on that completion screen and never on the card
/// itself, and only when reduced motion/the measurement variant don't suppress it (same gate as
/// [RiveEffectOverlay]), so this view's own body has nothing left to gate.
struct RiveChainCompleteOverlay: View {
    private let viewModel = RiveViewModel(
        fileName: RiveEffects.chainComplete.asset,
        animationName: RiveEffects.chainComplete.animation,
        artboardName: RiveEffects.chainComplete.artboard
    )

    var body: some View {
        viewModel.view().allowsHitTesting(false).accessibilityHidden(true)
    }
}
