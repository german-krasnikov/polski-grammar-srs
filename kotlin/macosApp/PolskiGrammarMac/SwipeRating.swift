import SwiftUI

/// D3 (`Plans/Kotlin/FlipCardRivePlan.md` §12-§18): the whole revealed card follows the finger
/// (translate + slight tilt), a tint + direction label grows with drag distance, a short drag
/// snaps back and a long one flies the card off before the rating is dispatched. Mirrors the web
/// reference's `installSwipeCard` (`webMain/kotlin/polski/ui/WebSwipeRating.kt`) translated to a
/// native gesture — shared by `MacFlashCardView` and `MacVocabularyCardView` so there is exactly
/// one copy of the drag/tint/fly-out mechanics, attached once at each card's own outermost view
/// (background + border included) so the *whole* card panel moves, not just its text content.
private let ratingSwipeThreshold: CGFloat = 70
private let ratingFlyDistance: CGFloat = 420
private let ratingCommitDuration: Double = 0.22

private struct SwipeToRate: ViewModifier {
    let enabled: Bool
    let reduceMotion: Bool
    let onRating: (Bool) -> Void

    @State private var offset: CGFloat = 0
    @State private var flyingOut = false

    func body(content: Content) -> some View {
        let live = flyingOut ? (offset > 0 ? ratingFlyDistance : -ratingFlyDistance) : offset
        content
            .offset(x: live)
            .rotationEffect(.degrees(Double((live / 22).clamped(-8, 8))))
            .opacity(flyingOut ? 0 : 1)
            .overlay(SwipeRatingTint(progress: (offset / ratingSwipeThreshold).clamped(-1, 1)))
            .simultaneousGesture(drag)
    }

    private var drag: some Gesture {
        DragGesture(minimumDistance: 8)
            .onChanged { value in
                guard enabled, !flyingOut, abs(value.translation.width) > abs(value.translation.height) * 1.25 else { return }
                offset = value.translation.width
            }
            .onEnded { value in
                guard enabled, !flyingOut else { return }
                let dx = value.translation.width
                let dy = value.translation.height
                guard abs(dx) > abs(dy) * 1.25, abs(dx) >= ratingSwipeThreshold else { snapBack(); return }
                commit(remembered: dx > 0)
            }
    }

    private func snapBack() {
        if reduceMotion { offset = 0; return }
        withAnimation(.spring(response: 0.35, dampingFraction: 0.75)) { offset = 0 }
    }

    private func commit(remembered: Bool) {
        if reduceMotion { offset = 0; onRating(remembered); return }
        withAnimation(.easeIn(duration: ratingCommitDuration)) { flyingOut = true }
        DispatchQueue.main.asyncAfter(deadline: .now() + ratingCommitDuration) {
            offset = 0
            flyingOut = false
            onRating(remembered)
        }
    }
}

/// The decorative tint + growing "Повторить"/"Вспомнил" label, driven purely by [progress]
/// (`-1`...`1`), mirroring the web reference's `appendSwipeLabels` + `--swipe-progress` CSS.
private struct SwipeRatingTint: View {
    let progress: Double

    var body: some View {
        let magnitude = min(abs(progress), 1)
        ZStack {
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .fill(progress < 0 ? Color.red : Color.green)
                .opacity(magnitude * 0.16)
            Text(progress < 0 ? "Повторить" : "Вспомнил")
                .font(.headline.weight(.bold))
                .foregroundStyle(progress < 0 ? .red : .green)
                .opacity(magnitude)
                .scaleEffect(0.85 + magnitude * 0.15)
        }
        .allowsHitTesting(false)
    }
}

extension View {
    /// - Parameters:
    ///   - enabled: gates whether the drag is live (e.g. only once the card is actually revealed),
    ///     so an un-revealed card never starts tracking a drag it would have to reject anyway.
    ///   - onRating: `true` for "Вспомнил" (remembered/right), `false` for "Повторить" (again/left).
    func swipeToRate(enabled: Bool, reduceMotion: Bool, onRating: @escaping (Bool) -> Void) -> some View {
        modifier(SwipeToRate(enabled: enabled, reduceMotion: reduceMotion, onRating: onRating))
    }
}

private extension Comparable {
    func clamped(_ lower: Self, _ upper: Self) -> Self { min(max(self, lower), upper) }
}

/// D3's "compact buttons with interval stay": the rating buttons show the FSRS interval preview
/// they'd schedule, mirroring the web reference's `<small>hint</small><span>interval</span>`
/// button structure (`webMain/kotlin/polski/ui/VocabularyWeb.kt`'s `button(label, hint, interval)`)
/// — compact (`.controlSize(.regular)`, not `.large`) rather than the touch host's full-width ones,
/// since phones/tablets get no rating buttons at all (D3) and don't need this affordance.
struct RatingButton: View {
    let label: String
    let prominent: Bool
    let dueMs: Int64?
    let nowMs: Int64?
    let action: () -> Void

    @ViewBuilder var body: some View {
        let label = VStack(spacing: 1) {
            Text(self.label).font(.callout.weight(.medium))
            if let dueMs, let nowMs {
                Text(intervalLabel(dueMs: dueMs, nowMs: nowMs))
                    .font(.caption2).foregroundStyle(.secondary)
            }
        }.padding(.horizontal, 2)
        if prominent {
            Button(action: action) { label }.buttonStyle(.borderedProminent).controlSize(.regular)
        } else {
            Button(action: action) { label }.buttonStyle(.bordered).controlSize(.regular)
        }
    }
}

/// Mirrors the web reference's `intervalLabel` (`webMain/kotlin/polski/ui/TrainingWebApp.kt`) —
/// kept as a native, host-local formatter rather than a shared Kotlin export, since it is pure
/// display formatting with no domain logic and this lane must not touch `commonMain`.
func intervalLabel(dueMs: Int64, nowMs: Int64) -> String {
    let minutes = max(1, (dueMs - nowMs + 30_000) / 60_000)
    if minutes < 60 { return "\(minutes) мин" }
    if minutes < 2_880 { return "\((minutes + 30) / 60) ч" }
    return "\((minutes + 720) / 1_440) дн"
}
