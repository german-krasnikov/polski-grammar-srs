import SwiftUI
import RiveRuntime

/// Decorative Rive rating-effect overlay for the macOS flash card (`Plans/Kotlin/FlipCardRivePlan.md`
/// FC-15/17/20). Never interactive: `.allowsHitTesting(false)` so it can never intercept the flip or
/// swipe gestures underneath, and `.accessibilityHidden(true)` so VoiceOver/assistive tech never
/// lands on it.
///
/// The two `RiveViewModel`s (one per vendored `.riv` file, `confetti.riv`/`again.riv`, see
/// `THIRD_PARTY/credits.md`) are created lazily, on the first accepted rating, not eagerly at this
/// view's first appearance — mirroring `iosApp/PolskiGrammar/RiveEffectOverlay.swift`, where an
/// XCUITest run caught a real regression from eager creation (a Metal-backed render surface loading
/// on every Question-phase re-render slowed typing enough to drop characters). Deferring creation
/// until the moment it is actually needed keeps the Question/typing path exactly as cheap as it was
/// before this feature.
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
        // D5: turning off "Анимации" (or system Reduce Motion / Motion.Reduced, which already fold
        // into the same `reduceMotion` the caller passes in) must dispose any Rive view already
        // created, not just skip creating new ones below.
        .onChange(of: reduceMotion) { _, isReduced in
            guard isReduced else { return }
            rememberedViewModel = nil
            againViewModel = nil
        }
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
