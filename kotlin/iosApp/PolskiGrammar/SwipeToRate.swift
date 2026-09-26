import SwiftUI

/// D3's whole-card swipe-to-rate affordance (`Plans/Kotlin/FlipCardRivePlan.md`), shared by
/// `FlashCardView`'s answer panel and `VocabularyCardView`'s answer face: the WHOLE card (whatever
/// [Content] is — training's answer panel has no background of its own; vocabulary's is the
/// entire rounded, shadowed outer container) follows the finger — translate + a slight tilt — a
/// tint + rating label grows with drag distance, it snaps back under [threshold] and flies off-
/// screen on commit, mirroring the web host's own `installSwipeCard`/`appendSwipeLabels`
/// (`WebSwipeRating.kt`, read, not edited): same ~80pt threshold, same "horizontal-dominant sticks,
/// vertical/diagonal never moves the card" gesture split (so it never fights the enclosing Form's
/// scroll — `.simultaneousGesture`, not `.gesture`), and the fly-out finishes **before** [onRate]
/// actually dispatches the rating, exactly like the web host's own commit-then-settle order.
///
/// Touch has no rating buttons any more (D3: "Phones/tablets (touch): no rating buttons") — this
/// gesture is the only way to rate on iPhone/iPad. Each call site puts the `ratingSwipeArea`
/// accessibility identifier explicitly on its own long hint text (`FlashCardView`'s "Свайп влево…"
/// footnote, `VocabularyCardView`'s own copy), never here on the shared, multi-child [content]:
/// setting it here would let SwiftUI inherit it down onto several descendant leaves at once (every
/// one of them a plain, unidentified `Text`), and an XCUITest query's `firstMatch` could then
/// resolve to whichever small caption happens to come first in traversal order — its own tiny
/// frame, not the whole card's, is what a synthesized `swipeLeft()`/`swipeRight()` would then swipe
/// across, well under [threshold]. A real finger gesture is unaffected either way (translation
/// comes from the touch itself, not from the element it started on) — this only matters for the
/// scripted, frame-relative gesture XCUITest synthesizes.
/// Exactly one rating per gesture: [onRate] fires
/// once, from `DragGesture`'s own `onEnded`, guarded by `flying` against a second gesture
/// overlapping the fly-out's brief window; the real reset happens for free because both call sites
/// only ever mount this face while its card is actually up (`FlashCardView`'s `if revealed`,
/// `VocabularyCardView`'s `if showBack`), so a stale in-flight drag can never survive onto the next
/// card — a fresh `SwipeToRate` instance, with fresh `@State`, mounts for it.
struct SwipeToRate: ViewModifier {
    /// False makes every gesture on this face snap back, never commit — `VocabularyCardView` passes
    /// `showBack` here (this modifier sits on the shared container both faces render through, so a
    /// drag started on the still-showing question face must never rate); `FlashCardView` always
    /// passes `true` since its own call site only exists while the card is already revealed.
    let active: Bool
    let reduceMotion: Bool
    /// The tint/label's corner radius — vocabulary's whole rounded panel (`cornerRadius: 20`,
    /// matching its own background shape) vs. training's plain, background-less answer panel
    /// (`cornerRadius: 16`, matching its own "ЗАПОМНИ" callout's radius for visual consistency).
    var cornerRadius: CGFloat = 16
    let onRate: (_ remembered: Bool) -> Void

    @State private var translation: CGFloat = 0
    @State private var opacity: Double = 1
    @State private var flying = false

    private let threshold: CGFloat = 80
    private let flyDistance: CGFloat = 600
    private let settleDuration = 0.22

    private var progress: Double { Double(min(abs(translation) / threshold, 1)) }
    private var tilt: Double { max(-8, min(8, Double(translation) / 22)) }

    func body(content: Content) -> some View {
        content
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .fill((translation < 0 ? Color(uiColor: .systemRed) : Color(uiColor: .systemGreen)).opacity(0.22 * progress))
            )
            .overlay(swipeLabel("Повторить", active: translation < 0))
            .overlay(swipeLabel("Вспомнил", active: translation > 0))
            .offset(x: translation)
            .rotationEffect(.degrees(reduceMotion ? 0 : tilt), anchor: .bottom)
            .opacity(opacity)
            .contentShape(Rectangle())
            .simultaneousGesture(
                // 18, matching the pre-D3 rating drag this replaces: `.simultaneousGesture`
                // already keeps this from blocking the enclosing Form's own scroll gesture, but a
                // lower minimumDistance made this recognizer engage earlier/more eagerly, which
                // measurably ate into that same scroll's effective distance per swipe in a
                // reference-table-heavy screen (reproduced: an XCUITest scroll loop that reliably
                // reached a target at 10 fixed swipes before D3 needed 10+ and still fell short
                // after switching to 10 here; back to 18, the same loop reaches it again).
                DragGesture(minimumDistance: 18)
                    .onChanged { value in
                        guard active, !flying else { return }
                        let x = value.translation.width, y = value.translation.height
                        guard abs(x) > abs(y) * 1.25 else { return }
                        translation = x
                    }
                    .onEnded { value in
                        guard active, !flying else { return }
                        let x = value.translation.width, y = value.translation.height
                        let horizontalDominant = abs(x) > abs(y) * 1.25
                        settle(committed: horizontalDominant && abs(x) >= threshold, remembered: x > 0)
                    }
            )
    }

    private func settle(committed: Bool, remembered: Bool) {
        guard translation != 0 || committed else { return }
        guard committed else {
            if reduceMotion { translation = 0; return }
            withAnimation(.spring(response: 0.35, dampingFraction: 0.82)) { translation = 0 }
            return
        }
        flying = true
        let sign: CGFloat = remembered ? 1 : -1
        guard !reduceMotion else {
            onRate(remembered)
            translation = 0; opacity = 1; flying = false
            return
        }
        withAnimation(.easeOut(duration: settleDuration)) {
            translation = sign * flyDistance
            opacity = 0
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + settleDuration) {
            onRate(remembered)
            translation = 0; opacity = 1; flying = false
        }
    }

    @ViewBuilder private func swipeLabel(_ text: String, active: Bool) -> some View {
        Text(text)
            .font(.headline.weight(.bold))
            .foregroundStyle(.white)
            .opacity(active ? progress : 0)
            .accessibilityHidden(true)
    }
}

extension View {
    /// See [SwipeToRate]'s own doc comment for the full contract.
    func swipeToRate(active: Bool, reduceMotion: Bool, cornerRadius: CGFloat = 16, onRate: @escaping (_ remembered: Bool) -> Void) -> some View {
        modifier(SwipeToRate(active: active, reduceMotion: reduceMotion, cornerRadius: cornerRadius, onRate: onRate))
    }
}
