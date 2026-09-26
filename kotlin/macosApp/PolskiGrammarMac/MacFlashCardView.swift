import SwiftUI

/// Native 3D flip card for the macOS training screen (`Plans/Kotlin/FlipCardRivePlan.md` FC-06/12/13/14).
///
/// Mirrors `iosApp/PolskiGrammar/FlashCardView.swift`'s contract and its single-mounted-face /
/// anti-mirror technique, adapted to this host's own typed `TrainingSnapshot`/`MacModel` bridge
/// (macOS decodes JSON into `Decodable` structs; there is no dynamic `Record` accessor here). The
/// flip is purely host-local visual state: it never dispatches a `MacSession` command and never
/// reads or mutates `phase`/FSRS (contract in the plan's §0). A tap only turns the card while
/// `state.phase == "Revealed"` — the back face has no answer data before that, so a tap during
/// `Question` is a no-op by construction. `Reveal` auto-flips the card to its back once
/// (`onChange(of: state.phase)`); flipping back afterwards is purely visual.
///
/// Only one face is ever mounted at a time — swapped at the rotation's halfway point, the same
/// split `FlashCardView`/`AndroidFlipCard` use — rather than mounting both permanently in a
/// `ZStack`. The back face's content carries a `-180°` counter-rotation so its text isn't mirrored
/// once it swaps in past the 90° mark.
struct MacFlashCardView: View {
    @ObservedObject var model: MacModel
    let state: TrainingSnapshot
    let exercise: TrainingSnapshot.Exercise
    /// FC-12/14/20's shared gate (system Reduce Motion OR the app's `Motion.Reduced`): snaps the
    /// flip instead of animating it. Computed once by the caller (`TrainingView.cardMotionReduced`).
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
        .onChange(of: state.phase) { _, phase in
            if phase == "Revealed" { setFlipped(true) }
        }
        // A new exercise is always shown face-up on its question, regardless of how the previous
        // card was left — never animated, so the next question never visibly "un-flips".
        .onChange(of: exercise.id) { _, _ in setFlipped(false, animated: false) }
        .onAppear { setFlipped(state.phase == "Revealed", animated: false) }
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

    @ViewBuilder private var frontFace: some View {
        let content = VStack(alignment: .leading, spacing: 8) {
            Text("ПРЕДЛОЖЕНИЕ").font(.caption.weight(.semibold))
                .tracking(1.4).foregroundStyle(.secondary)
            highlightedText(exercise.sourceParts, before: true)
                .font(.system(size: 28, weight: .medium, design: .rounded))
                .accessibilityLabel(exercise.source)
                .textSelection(.enabled)
                .fixedSize(horizontal: false, vertical: true)
            Divider().padding(.vertical, 4)
            if state.introPending {
                VStack(alignment: .leading, spacing: 18) {
                    Text("Знакомство с навыком").font(.title3.weight(.semibold))
                    Text("Посмотрите на предложение и переходите к заданию, когда будете готовы.")
                        .foregroundStyle(.secondary)
                    Button("Перейти к заданию") { model.send("continueIntroduction") }
                        .buttonStyle(.borderedProminent)
                        .controlSize(.large)
                }
            } else {
                VStack(alignment: .leading, spacing: 12) {
                    Text("ЗАДАНИЕ").font(.caption.weight(.semibold))
                        .tracking(1.4).foregroundStyle(.secondary)
                    Text(exercise.prompt ?? "").font(.title3.weight(.medium))
                        .fixedSize(horizontal: false, vertical: true)
                }
                if state.phase == "Question" {
                    VStack(alignment: .leading, spacing: 18) {
                        Picker("Способ ответа", selection: Binding(
                            get: { state.answerMode },
                            set: { model.preference("answerMode", $0) }
                        )) {
                            Text("Вслух").tag("Oral")
                            Text("Напечатать").tag("Typed")
                        }
                        .pickerStyle(.segmented)
                        .padding(3)
                        .frame(maxWidth: 360)
                        if state.answerMode == "Typed" {
                            TextField("Ответ по-польски", text: Binding(
                                get: { model.training?.draft ?? "" },
                                set: { model.send("draft", $0) }
                            ), axis: .vertical)
                            .textFieldStyle(.roundedBorder)
                            .lineLimit(2...4)
                            .font(.body)
                        }
                        HStack {
                            Spacer()
                            Button("Показать ответ") { model.send("reveal", exercise.id) }
                                .keyboardShortcut(.return, modifiers: [.command])
                                .buttonStyle(.borderedProminent)
                                .controlSize(.large)
                        }
                    }
                }
            }
        }
        // The tap-to-flip recognizer is only ever attached while `phase == "Revealed"` — exactly
        // when the Question-only Picker/TextField/reveal button above are absent from this view —
        // mirroring `FlashCardView`'s own scoping, which a real XCUITest regression showed was
        // necessary there (an always-attached ancestor gesture, even a no-op one, can still confuse
        // a sibling control's own gesture recognition).
        if state.phase == "Revealed" {
            content.contentShape(Rectangle()).onTapGesture { setFlipped(true) }
        } else {
            content
        }
    }

    @ViewBuilder private var backFace: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("ОТВЕТ").font(.caption.weight(.semibold))
                .tracking(1.4).foregroundStyle(.secondary)
            highlightedText(exercise.expectedParts ?? [], before: false)
                .font(.title2.weight(.semibold))
                .accessibilityLabel(exercise.expected ?? "")
                .textSelection(.enabled)
            if let answer = exercise.frozenAnswer {
                Text("Ваш ответ: \(answer)").foregroundStyle(.secondary)
            }
            if let changes = exercise.changes, !changes.isEmpty {
                Text("Что изменилось").font(.headline)
                ForEach(Array(changes.enumerated()), id: \.offset) { _, change in
                    VStack(alignment: .leading, spacing: 3) {
                        (Text("Было: ")
                         + Text(change.from).foregroundColor(Color(nsColor: .systemRed)).underline()
                         + Text(" → Стало: ")
                         + highlightedText(change.toParts, before: false))
                            .textSelection(.enabled)
                        Text(change.reason).foregroundStyle(.secondary)
                    }
                }
            }
            if let formula = exercise.formula, !formula.isEmpty {
                VStack(alignment: .leading, spacing: 7) {
                    Text("ЗАПОМНИ").font(.caption.weight(.semibold))
                    Text(formula).font(.headline)
                    if let feedback = exercise.methodFeedback, !feedback.isEmpty { Text(feedback) }
                    if let explanation = exercise.explanation, !explanation.isEmpty { Text(explanation) }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(14)
                .background(Color.accentColor.opacity(0.12), in: RoundedRectangle(cornerRadius: 12))
            }
            VStack(alignment: .leading, spacing: 8) {
                Text("Свайп влево — повторить · вправо — вспомнил")
                    .font(.footnote).foregroundStyle(.secondary)
                    .accessibilityIdentifier("ratingSwipeArea")
                HStack(spacing: 10) {
                    Spacer()
                    Button("Повторить") { model.send("rate", "\(exercise.id)|Again") }
                        .keyboardShortcut("1", modifiers: [.command])
                        .accessibilityIdentifier("rateAgain")
                    Button("Вспомнил") { model.send("rate", "\(exercise.id)|Good") }
                        .keyboardShortcut("2", modifiers: [.command])
                        .buttonStyle(.borderedProminent)
                        .accessibilityIdentifier("rateGood")
                }
                .controlSize(.large)
            }
        }
        // FC-06/12: one gesture on the whole revealed back face (macOS previously had no swipe at
        // all — only the buttons/⌘1/⌘2 already above). A tap flips back to the front; a horizontal
        // drag past the threshold rates. `.simultaneousGesture` (not `.gesture`) keeps this from
        // blocking the enclosing `ScrollView`'s vertical scroll.
        .contentShape(Rectangle())
        .onTapGesture { setFlipped(false) }
        .simultaneousGesture(DragGesture(minimumDistance: 18).onEnded { gesture in
            guard state.phase == "Revealed" else { return }
            let x = gesture.translation.width
            let y = gesture.translation.height
            guard abs(x) >= 80, abs(x) > abs(y) * 1.5 else { return }
            model.send("rate", "\(exercise.id)|\(x < 0 ? "Again" : "Good")")
        })
    }
}
