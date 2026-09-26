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
/// The two `RiveViewModel`s (one per vendored `.riv` file, `confetti.riv`/`again.riv`, see
/// `THIRD_PARTY/credits.md`) are created lazily, on the first accepted rating, not eagerly at this
/// view's first appearance. Loading a `.riv` file and mounting its Metal-backed render surface has
/// real per-frame cost; creating it while the user is still just reading or typing the Question
/// side of a card (i.e. always, since this overlay sits over the card from the start) measurably
/// slowed down every keystroke's re-render and caused an XCUITest run to drop characters mid-type —
/// a real, reproduced regression, not a hypothetical one. Deferring creation until the moment it is
/// actually needed (mirroring the web host's own lazy-load of the Rive runtime, per the plan's §8
/// evidence log) keeps the Question/typing path exactly as cheap as it was before this feature.
///
/// **FC2-09 (R3, Pick A):** `again.riv` was replaced — it used to paint an opaque scene over the
/// card (`RiveCatalog.md` §0.1). The new file's `Check`/`Error` triggers on `State Machine 1` play
/// alongside `confetti.riv` for `Remembered` (both together, not either/or — the same pairing the
/// web/Android hosts already ship, `FlipCardRivePlan.md` §12.7) and alone for `Again`.
struct RiveEffectOverlay: View {
    let effect: CardEffectEvent?
    let reduceMotion: Bool

    @State private var rememberedViewModel: RiveViewModel?
    @State private var againViewModel: RiveViewModel?

    var body: some View {
        ZStack {
            rememberedViewModel?.view()
            againViewModel?.view()
        }
        .allowsHitTesting(false)
        .accessibilityHidden(true)
        .onChange(of: effect) { _, event in
            guard let event, !reduceMotion else { return }
            #if DEBUG
            let signpostID = OSSignpostID(log: riveSignpostLog)
            os_signpost(.begin, log: riveSignpostLog, name: "RiveRatingEffect", signpostID: signpostID)
            #endif
            switch event.name {
            case "Remembered":
                if rememberedViewModel == nil {
                    rememberedViewModel = RiveViewModel(fileName: "confetti", stateMachineName: "State Machine 1")
                }
                if againViewModel == nil {
                    againViewModel = RiveViewModel(fileName: "again", stateMachineName: "State Machine 1")
                }
                rememberedViewModel?.triggerInput("Trigger explosion")
                againViewModel?.triggerInput("Check")
            case "Again":
                if againViewModel == nil {
                    againViewModel = RiveViewModel(fileName: "again", stateMachineName: "State Machine 1")
                }
                againViewModel?.triggerInput("Error")
            default: break
            }
            #if DEBUG
            os_signpost(.end, log: riveSignpostLog, name: "RiveRatingEffect", signpostID: signpostID)
            #endif
        }
    }
}

/// Flip-in-progress ring cue (`Plans/Kotlin/FlipCardRivePlan.md` FC2-06/07/08, R2): `rings.riv`'s
/// boolean `IsExpanded` input on `State Machine 1`, driven by [expanded]. D1 replaced the training
/// card's native flip with an expand-down reveal (`FlashCardView.swift`) that has no "flip in
/// progress" moment to report any more, so `TrainingView` no longer composes this type — it is
/// unused dead code until D3 (drop the ring effect entirely, remove `rings.riv`) removes it.
///
/// Lazily created on the first `expanded == true`, for the same reason as [RiveEffectOverlay]'s own
/// view models above: a `RiveViewModel(...)` default value would otherwise run its expensive
/// initializer on every re-render of this view's identity, not just the first.
struct RiveFlipRingsOverlay: View {
    let expanded: Bool

    @State private var viewModel: RiveViewModel?

    var body: some View {
        ZStack { viewModel?.view() }
            .allowsHitTesting(false)
            .accessibilityHidden(true)
            .onChange(of: expanded) { _, newValue in
                if viewModel == nil {
                    guard newValue else { return }
                    #if DEBUG
                    let signpostID = OSSignpostID(log: riveSignpostLog)
                    os_signpost(.begin, log: riveSignpostLog, name: "RiveFlipRingsFirstLoad", signpostID: signpostID)
                    #endif
                    viewModel = RiveViewModel(fileName: "rings", stateMachineName: "State Machine 1")
                    #if DEBUG
                    os_signpost(.end, log: riveSignpostLog, name: "RiveFlipRingsFirstLoad", signpostID: signpostID)
                    #endif
                }
                viewModel?.setInput("IsExpanded", value: newValue)
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
    private let viewModel = RiveViewModel(fileName: "chain-complete", animationName: "Reveal", artboardName: "Tada")

    var body: some View {
        viewModel.view().allowsHitTesting(false).accessibilityHidden(true)
    }
}
