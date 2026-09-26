import SwiftUI
import RiveRuntime

/// Decorative Rive rating-effect overlay for the iOS flash card (`Plans/Kotlin/FlipCardRivePlan.md`
/// FC-15/17/20). Never interactive: `.allowsHitTesting(false)` so it can never intercept the flip or
/// swipe gestures underneath, and `.accessibilityHidden(true)` so VoiceOver never lands on it.
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
            switch event.name {
            case "Remembered":
                if rememberedViewModel == nil {
                    rememberedViewModel = RiveViewModel(fileName: "confetti", stateMachineName: "State Machine 1")
                }
                rememberedViewModel?.triggerInput("Trigger explosion")
            case "Again":
                if againViewModel == nil {
                    againViewModel = RiveViewModel(fileName: "again", stateMachineName: "Swipe to delete")
                }
                againViewModel?.triggerInput("Trigger Delete")
            default: break
            }
        }
    }
}
