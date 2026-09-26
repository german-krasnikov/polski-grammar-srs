import SwiftUI

/// Native 3D flip card for the training screen (`Plans/Kotlin/FlipCardRivePlan.md` FC-11/13/14).
///
/// The flip is purely a host-local visual state (`flipped`): it never dispatches an `AppAction` and
/// never reads or mutates `CardPhase`/FSRS (contract in the plan's §0). A tap only turns the card
/// while `phase == "Revealed"` — there is no answer face to show before that, so a tap during
/// `Question` is a no-op by construction, which is also why a typed answer's `TextField` never needs
/// its own tap-exclusion: the whole front face ignores taps until revealed. `Reveal` auto-flips the
/// card to its back once (`onChange(of: state.phase)`); flipping back afterwards is purely visual.
///
/// Only one face is ever mounted at a time — split at the rotation's halfway point, mirroring the
/// Android `AndroidFlipCard`'s equivalent split — rather than mounting both permanently: this file's
/// `TrainingView` embeds the card as a single `Form`/`List` row (see the `.listRowInsets` call site
/// in `PolskiGrammarApp.swift`, the same pattern already used for `StudyHero`), and a real 3D
/// transform fights row-splitting if the builder emits more than one top-level child. The back
/// face's own content carries a `-180°` counter-rotation so its text isn't mirrored once it swaps in
/// past the 90° mark (the same anti-mirror technique `AndroidFlipCard` uses for `rotationY`).
struct FlashCardView<RevealButton: View>: View {
    @ObservedObject var model: AppModel
    let state: Record
    let card: Record
    @Binding var localDraft: String
    var answerFocused: FocusState<Bool>.Binding
    /// FC-09/12/14/20's shared gate (system Reduce Motion OR the app's `Motion.Reduced`): snaps the
    /// flip instead of animating it. Computed once by the caller (`TrainingView.cardMotionReduced`).
    let reduceMotion: Bool
    let revealButton: (_ title: String, _ expands: Bool) -> RevealButton

    @State private var flipped = false
    @State private var showBack = false
    @State private var rotation: Double = 0

    var body: some View {
        Group {
            // `rotation == 0` at rest (showing the front, not mid-flip) skips the `rotation3DEffect`
            // modifier entirely rather than applying it with `.degrees(0)`: even a nominally-identity
            // 3D transform ancestor confused a Menu-style Picker's popup anchoring in an XCUITest run
            // (the "Ответ" answer-mode picker's "Напечатать" option became untappable) — the modifier
            // is only ever load-bearing while genuinely mid-animation or showing the back.
            if showBack {
                backFace.rotation3DEffect(.degrees(-180), axis: (x: 0, y: 1, z: 0))
                    .rotation3DEffect(.degrees(rotation), axis: (x: 0, y: 1, z: 0), perspective: 0.35)
            } else if rotation == 0 {
                frontFace
            } else {
                frontFace.rotation3DEffect(.degrees(rotation), axis: (x: 0, y: 1, z: 0), perspective: 0.35)
            }
        }
        .onChange(of: state.string("phase")) { _, phase in
            if phase == "Revealed" { setFlipped(true) }
        }
        // A new exercise is always shown face-up on its question, regardless of how the previous
        // card was left — never animated, so the next question never visibly "un-flips".
        .onChange(of: card.string("id")) { _, _ in setFlipped(false, animated: false) }
        .onAppear { setFlipped(state.string("phase") == "Revealed", animated: false) }
    }

    private func setFlipped(_ newValue: Bool, animated: Bool = true) {
        flipped = newValue
        if animated && !reduceMotion {
            withAnimation(.easeInOut(duration: 0.5)) { rotation = newValue ? 180 : 0 }
            // Swap the mounted face at the halfway point, once it is edge-on and invisible, matching
            // AndroidFlipCard's `angle >= 90f` split.
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) { showBack = newValue }
        } else {
            rotation = newValue ? 180 : 0
            showBack = newValue
        }
    }

    @ViewBuilder private var frontFace: some View {
        let content = VStack(alignment: .leading, spacing: 12) {
            Text("Исходное предложение").font(.caption).foregroundStyle(.secondary)
            highlightedSentence(card.rows("sourceParts"), before: true)
                .font(.system(.title2, design: .rounded, weight: .semibold))
                .accessibilityLabel(card.string("source"))
            if state.bool("introPending") && state.string("phase") == "Question" {
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
                if state.string("phase") == "Question" {
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
        // The tap-to-flip recognizer is only ever attached while `phase == "Revealed"` — exactly
        // when the Question-only Picker/TextField/reveal button above are absent from this view —
        // rather than always-attached-but-guarded in its closure. A UI test caught a real
        // regression from the always-attached form: merely having a `.onTapGesture` ancestor (even
        // one whose closure is a no-op during Question) broke the `Picker`'s own "Ответ" menu-style
        // popup, making its "Напечатать" option untappable in XCUITest. Scoping the modifier itself
        // to Revealed — when there is no competing control underneath — avoids that conflict instead
        // of merely working around its symptom.
        if state.string("phase") == "Revealed" {
            content.contentShape(Rectangle()).onTapGesture { setFlipped(true) }
        } else {
            content
        }
    }

    @ViewBuilder private var backFace: some View {
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
            Text("Что изменилось").font(.headline)
            ForEach(Array(card.rows("changes").enumerated()), id: \.offset) { _, change in
                VStack(alignment: .leading, spacing: 3) {
                    (Text("Было: ") + Text(change.string("from")).foregroundColor(Color(uiColor: .systemRed)).underline()
                     + Text(" → Стало: ") + highlightedNewForm(change))
                    Text(change.string("reason")).foregroundStyle(.secondary)
                }
            }
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
            VStack(alignment: .leading, spacing: 9) {
                Text("Когда повторить?").font(.headline)
                Text(card.string("methodReview"))
                Text("Свайп влево — повторить · вправо — вспомнил")
                    .font(.footnote).foregroundStyle(.secondary)
                    .accessibilityIdentifier("ratingSwipeArea")
                HStack(spacing: 8) {
                    ForEach([("Again", "Повторить"), ("Good", "Вспомнил")], id: \.0) { rating, label in
                        Button {
                            model.send("rate", rating)
                        } label: {
                            VStack(alignment: .leading, spacing: 3) {
                                Text(label).fontWeight(.semibold)
                                Text(formattedDate(card.record("intervals").int64(rating)))
                                    .font(.caption).foregroundStyle(.secondary)
                            }
                            .frame(maxWidth: .infinity, minHeight: 48, alignment: .leading)
                        }
                        .buttonStyle(.bordered)
                        .accessibilityIdentifier("rate\(rating)")
                    }
                }
            }
            .padding(.vertical, 4)
        }
        // FC-04/05/07: one gesture on the whole revealed back face (relocated from the narrow
        // "Когда повторить?" strip) — a tap flips back to the front, a horizontal drag past the
        // threshold rates. `.simultaneousGesture` (not `.gesture`) keeps this from blocking the
        // enclosing `Form`'s vertical scroll, exactly as this drag already worked pre-relocation.
        .contentShape(Rectangle())
        .onTapGesture { setFlipped(false) }
        .simultaneousGesture(DragGesture(minimumDistance: 18).onEnded { gesture in
            guard state.string("phase") == "Revealed" else { return }
            let x = gesture.translation.width
            let y = gesture.translation.height
            guard abs(x) >= 80, abs(x) > abs(y) * 1.5 else { return }
            model.send("rate", x < 0 ? "Again" : "Good")
        })
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
