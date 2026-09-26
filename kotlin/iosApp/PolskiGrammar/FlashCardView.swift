import SwiftUI

/// Task/grammar card for the training screen (`Plans/Kotlin/FlipCardRivePlan.md` D1, replacing the
/// earlier native 3D flip, FC-11/13/14/FC2-05).
///
/// D1: no 3D flip. The question stays on screen and the answer/explanation/rating panel unfolds
/// below it — a plain SwiftUI insertion transition (`.move(edge: .top)` + opacity), spring-eased,
/// with a short per-section stagger via [staggeredReveal]. This is purely a host-local visual
/// state (`revealed`): it never dispatches an `AppAction` itself — only the existing "reveal"
/// action (from the reveal button, or now also a tap on the question) does, and `revealed` is
/// always driven downstream of `state.phase` becoming `"Revealed"` (`onChange` below), mirroring
/// how the old flip's own visual state used to stay downstream of the same phase change.
///
/// Reveal happens **exactly once**: the tap gesture that triggers it is only ever attached while
/// `phase == "Question"` (and not `introPending`, which has no answer yet), so once the phase
/// flips to `"Revealed"` the gesture is gone and tapping the now-visible answer panel is a no-op —
/// there is no flip-back, D1 is one-directional. The gesture is attached to [questionHeader] only,
/// never to an ancestor of the Question-only `Picker`/`TextField` in [answerControls]: those are a
/// sibling subtree, exactly the split the web host's own tap-vs-controls DOM structure uses
/// (`TrainingWebApp.kt`'s `front` vs `answer-area`), so the tap gesture can never intercept a tap
/// meant for the Picker's popup or the TextField's focus — the same class of bug FC2 fixed for the
/// old flip's tap gesture.
struct FlashCardView<RevealButton: View>: View {
    @ObservedObject var model: AppModel
    let state: Record
    let card: Record
    @Binding var localDraft: String
    var answerFocused: FocusState<Bool>.Binding
    /// Snaps the reveal instead of animating it: system Reduce Motion OR the app's `Motion.Reduced`
    /// (computed once by the caller, `TrainingView.cardMotionReduced`).
    let reduceMotion: Bool
    let revealButton: (_ title: String, _ expands: Bool) -> RevealButton

    @State private var revealed = false

    private var introPending: Bool { state.bool("introPending") && state.string("phase") == "Question" }
    private var tapToRevealEnabled: Bool { state.string("phase") == "Question" && !introPending }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            questionHeader
                .contentShape(Rectangle())
                .onTapGesture { if tapToRevealEnabled { model.send("reveal") } }
            if state.string("phase") == "Question" {
                answerControls
            }
            if revealed {
                answerFace
            }
        }
        .onChange(of: state.string("phase")) { _, phase in
            if phase == "Revealed" { setRevealed(true) }
        }
        // A new exercise is always shown unrevealed, regardless of how the previous card was left
        // — never animated, so the next question never visibly "un-reveals".
        .onChange(of: card.string("id")) { _, _ in setRevealed(false, animated: false) }
        .onAppear { setRevealed(state.string("phase") == "Revealed", animated: false) }
    }

    private func setRevealed(_ newValue: Bool, animated: Bool = true) {
        guard revealed != newValue else { return }
        guard animated && !reduceMotion else {
            revealed = newValue
            return
        }
        withAnimation(.spring(response: 0.42, dampingFraction: 0.86)) {
            revealed = newValue
        }
    }

    /// D1's stagger: each answer section gets its own `AnyTransition.animation(...)`, which
    /// overrides the animation SwiftUI uses for *that view's own insertion* independent of the
    /// outer `withAnimation` above — the supported way to stagger transitions in SwiftUI. `index`
    /// zero is the first section to appear; each later one is delayed a little more.
    private func staggeredReveal(_ index: Int) -> AnyTransition {
        let animation: Animation? = reduceMotion ? nil
            : .spring(response: 0.42, dampingFraction: 0.86).delay(Double(index) * 0.05)
        return .move(edge: .top).combined(with: .opacity).animation(animation)
    }

    @ViewBuilder private var questionHeader: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Исходное предложение").font(.caption).foregroundStyle(.secondary)
            highlightedSentence(card.rows("sourceParts"), before: true)
                .font(.system(.title2, design: .rounded, weight: .semibold))
                .accessibilityLabel(card.string("source"))
            if introPending {
                Text("Знакомство с навыком").font(.headline)
                Text(card.string("methodIntroduce"))
                Button("Перейти к заданию") { model.send("continueIntroduction") }
                    .buttonStyle(.borderedProminent)
                    .accessibilityIdentifier("continueIntroduction")
            } else {
                Label(card.string("prompt"), systemImage: "arrow.turn.down.right")
                    .font(.headline)
                    .foregroundStyle(.primary)
                Text(card.string("methodLead")).font(.footnote).foregroundStyle(.secondary)
            }
        }
    }

    @ViewBuilder private var answerControls: some View {
        if !introPending {
            VStack(alignment: .leading, spacing: 12) {
                Text(card.string("methodRetrieve"))
                Picker("Ответ", selection: Binding(get: { state.string("answerMode") }, set: { model.send("answerMode", $0) })) {
                    Text("Вслух / про себя").tag("Oral")
                    Text("Напечатать").tag("Typed")
                }
                if state.string("answerMode") == "Typed" {
                    TextField("Ответ по-польски", text: Binding(get: { localDraft }, set: {
                        localDraft = $0
                        model.send("draft", $0)
                    }), axis: .vertical)
                        .lineLimit(2...5).textInputAutocapitalization(.sentences)
                        .focused(answerFocused)
                        .accessibilityLabel("Ответ по-польски")
                        .accessibilityIdentifier("typedAnswer")
                } else {
                    Text("Произнеси целое предложение, затем покажи ответ.").foregroundStyle(.secondary)
                }
                if state.string("answerMode") != "Typed" || !answerFocused.wrappedValue {
                    revealButton(state.string("answerMode") == "Typed" ? "Проверить ответ" : "Показать ответ", true)
                }
            }
        }
    }

    @ViewBuilder private var answerFace: some View {
        VStack(alignment: .leading, spacing: 12) {
            VStack(alignment: .leading, spacing: 12) {
                Text("Эталон").font(.caption).foregroundStyle(.secondary)
                highlightedSentence(card.rows("expectedParts"), before: false)
                    .font(.system(.title2, design: .rounded, weight: .semibold))
                    .accessibilityLabel(card.string("expected"))
                if !card.strings("accepted").isEmpty { Text("Также: \(card.strings("accepted").joined(separator: " / "))") }
                if state.string("answerMode") == "Typed" {
                    Label(card.bool("correct") ? "Совпадает с правильным вариантом" : "Сравни свой ответ с эталоном",
                          systemImage: card.bool("correct") ? "checkmark.circle" : "info.circle")
                    Text(card.string("frozenAnswer").isEmpty ? "Ответ не введён" : card.string("frozenAnswer"))
                }
                if state.string("explanationMethod") == "Situations" {
                    Text(card.string("methodFeedback"))
                    Text(card.string("explanation"))
                }
            }
            .transition(staggeredReveal(0))

            VStack(alignment: .leading, spacing: 6) {
                Text("Что изменилось").font(.headline)
                ForEach(Array(card.rows("changes").enumerated()), id: \.offset) { _, change in
                    VStack(alignment: .leading, spacing: 3) {
                        (Text("Было: ") + Text(change.string("from")).foregroundColor(Color(uiColor: .systemRed)).underline()
                         + Text(" → Стало: ") + highlightedNewForm(change))
                        Text(change.string("reason")).foregroundStyle(.secondary)
                    }
                }
            }
            .transition(staggeredReveal(1))

            VStack(alignment: .leading, spacing: 7) {
                Text("ЗАПОМНИ").font(.caption.weight(.semibold))
                Text(card.string("formula")).font(.headline).bold()
                if state.string("explanationMethod") == "Logic" {
                    Text(card.string("methodFeedback"))
                    Text(card.string("explanation"))
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(14)
            .background(Color.accentColor.opacity(0.12), in: RoundedRectangle(cornerRadius: 16))
            .transition(staggeredReveal(2))

            VStack(alignment: .leading, spacing: 9) {
                Text("Когда повторить?").font(.headline)
                Text(card.string("methodReview"))
                Text("Свайп влево — повторить · вправо — вспомнил")
                    .font(.footnote).foregroundStyle(.secondary)
                    // See `SwipeToRate`'s own doc comment for why this identifier lives on this
                    // long-text leaf specifically, and not on the shared gesture container.
                    .accessibilityIdentifier("ratingSwipeArea")
            }
            .padding(.vertical, 4)
            .transition(staggeredReveal(3))
        }
        // D3: no rating buttons on touch — the whole panel above is the swipe-to-rate gesture
        // surface. D1 already removed the flip-back tap this container used to share with the old
        // drag, so there is no reverse action on this panel any more, only the rating drag.
        .swipeToRate(active: state.string("phase") == "Revealed", reduceMotion: reduceMotion) { remembered in
            guard state.string("phase") == "Revealed" else { return }
            model.send("rate", remembered ? "Good" : "Again")
        }
    }
}

private func highlightedNewForm(_ change: Record) -> Text {
    change.rows("toParts").reduce(Text("")) { result, part in
        let fragment = Text(part.string("text")).bold()
        return result + (part.bool("changed")
            ? fragment.foregroundColor(Color(uiColor: .systemOrange)).underline()
            : fragment)
    }
}

private func highlightedSentence(_ parts: [Record], before: Bool) -> Text {
    parts.reduce(Text("")) { result, part in
        let fragment = Text(part.string("text"))
        return result + (part.bool("changed")
            ? fragment.bold().foregroundColor(Color(uiColor: before ? .systemRed : .systemOrange)).underline()
            : fragment)
    }
}
