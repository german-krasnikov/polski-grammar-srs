import SwiftUI

/// Whole-panel flip for the macOS vocabulary card (D2, `Plans/Kotlin/FlipCardRivePlan.md` and the
/// web reference at `webMain/kotlin/polski/ui/VocabularyWeb.kt`'s `.card-flip`/`.card-flip-inner`).
/// Unlike the training card's D1 expand-down reveal, the WHOLE rounded panel — background, border,
/// radius — rotates as one object: chrome lives on the rotating container itself (mirroring
/// `contentCard` in `PolskiGrammarMacApp.swift`), not on a fixed wrapper around swapped content.
///
/// There is no "Показать ответ" button: the panel itself is the reveal control, exposed to
/// accessibility as a single button with that label before reveal. A tap both reveals (dispatches
/// `MacSession`'s `reveal`, once — idempotent after) and flips to the back; once revealed, a tap
/// only ever toggles which face faces the viewer, purely visual, never touching FSRS. Only one
/// face is ever mounted at a time, swapped at the rotation's halfway point (90°) — the same
/// split `MacFlashCardView`/`FlashCardView` use — with the back face's content counter-rotated
/// `-180°` so its text isn't mirrored once it swaps in past 90°.
struct MacVocabularyCardView: View {
    @ObservedObject var model: MacModel
    let state: VocabularySnapshot
    let item: VocabularySnapshot.Item
    /// Same reduced-motion gate the training flip uses (`TrainingView.cardMotionReduced`):
    /// system Reduce Motion or the app's own `Motion.Reduced` setting (D5).
    let reduceMotion: Bool

    @State private var showBack = false
    @State private var rotation: Double = 0

    var body: some View {
        Group {
            if showBack {
                backFace.rotation3DEffect(.degrees(-180), axis: (x: 0, y: 1, z: 0))
                    .rotation3DEffect(.degrees(rotation), axis: (x: 0, y: 1, z: 0), perspective: 0.35)
            } else if rotation == 0 {
                frontFace
            } else {
                frontFace.rotation3DEffect(.degrees(rotation), axis: (x: 0, y: 1, z: 0), perspective: 0.35)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(24)
        .background(Color(nsColor: .controlBackgroundColor), in: RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 18, style: .continuous)
            .strokeBorder(Color.primary.opacity(0.09), lineWidth: 1))
        // D3: attached here, after `.background`/`.overlay`, so the whole panel (chrome included)
        // follows the finger, same as the flip above already moves the whole panel as one object.
        .swipeToRate(enabled: state.revealed, reduceMotion: reduceMotion) { remembered in
            model.vocab(remembered ? "good" : "again")
        }
        .onChange(of: state.revealed) { _, revealed in if revealed { setFlipped(true) } }
        // A new due word always starts face-up on its question, regardless of how the previous
        // card was left — never animated, so the next word never visibly "un-flips".
        .onChange(of: item.id) { _, _ in setFlipped(false, animated: false) }
        .onAppear { setFlipped(state.revealed, animated: false) }
    }

    private func setFlipped(_ newValue: Bool, animated: Bool = true) {
        if animated && !reduceMotion {
            withAnimation(.easeInOut(duration: 0.5)) { rotation = newValue ? 180 : 0 }
            // Swap the mounted face at the halfway point, once it is edge-on and invisible.
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) { showBack = newValue }
        } else {
            rotation = newValue ? 180 : 0
            showBack = newValue
        }
    }

    private func handleTap() {
        if state.revealed { setFlipped(!showBack) } else { model.vocab("reveal") }
    }

    @ViewBuilder private var frontFace: some View {
        let content = VStack(alignment: .leading, spacing: 8) {
            Text("Слово").font(.caption.weight(.semibold)).tracking(1.4).foregroundStyle(.secondary)
            Text(item.lemma).font(.title2.bold())
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .contentShape(Rectangle())
        .onTapGesture(perform: handleTap)
        if state.revealed {
            content
        } else {
            // Not yet revealed: the panel is the reveal control — a single accessible button
            // named "Показать ответ", matching the web reference's `promptBlock` (`role="button"`,
            // `aria-label="Показать ответ"`) rather than combining the lemma text into the label.
            content
                .accessibilityElement(children: .ignore)
                .accessibilityLabel("Показать ответ")
                .accessibilityAddTraits(.isButton)
        }
    }

    @ViewBuilder private var backFace: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(item.translation)
            Text(item.form)
            Text(item.example)
            HStack {
                RatingButton(label: "Повторить", prominent: false, dueMs: state.intervals?.again, nowMs: state.now) {
                    model.vocab("again")
                }
                .keyboardShortcut("1", modifiers: [.command])
                RatingButton(label: "Вспомнил", prominent: true, dueMs: state.intervals?.good, nowMs: state.now) {
                    model.vocab("good")
                }
                .keyboardShortcut("2", modifiers: [.command])
                // D3: ArrowLeft/ArrowRight rate too (+ ⌘1/⌘2 above), mirroring the training card —
                // only mounted once flipped to this back face, i.e. only once actually revealed;
                // this card has no typed-answer field yet to conflict with the plain arrow keys.
                Button("") { model.vocab("again") }
                    .keyboardShortcut(.leftArrow, modifiers: [])
                    .frame(width: 0, height: 0).hidden().accessibilityHidden(true)
                Button("") { model.vocab("good") }
                    .keyboardShortcut(.rightArrow, modifiers: [])
                    .frame(width: 0, height: 0).hidden().accessibilityHidden(true)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .contentShape(Rectangle())
        .onTapGesture(perform: handleTap)
    }
}
