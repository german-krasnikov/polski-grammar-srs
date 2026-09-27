import SwiftUI

/// Downward expand-reveal for the macOS training card (D1, `Plans/Kotlin/FlipCardRivePlan.md`
/// §12-§18 and the web reference at `webMain/kotlin/polski/ui/TrainingWebApp.kt`'s
/// `applyExpand`/`.card-answer-wrap`). Replaces the earlier 3D flip: the question stays visible
/// and the answer/explanation/rating panel unfolds below it with a spring-like ease-out and a
/// short stagger across its three groups, mirroring the web's `grid-template-rows` + per-group
/// `animation-delay` trick. Reveal is host-local visual state gated by `state.phase` — it never
/// dispatches a `MacSession` command itself and never reads/mutates FSRS (contract in the plan's
/// §0); tapping the question card or the button both just send the same `reveal` command the
/// button always did, and the panel expands once the model confirms `phase == "Revealed"`. There
/// is no un-reveal for this card (unlike the vocabulary flip) — D1 only ever expands.
struct MacFlashCardView: View {
    @ObservedObject var model: MacModel
    let state: TrainingSnapshot
    let exercise: TrainingSnapshot.Exercise
    /// FC-12/14/20's shared gate (system Reduce Motion OR the app's `Motion.Reduced`): snaps the
    /// reveal instead of animating it. Computed once by the caller (`TrainingView.cardMotionReduced`).
    let reduceMotion: Bool

    @State private var revealed = false

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            frontFace
            if revealed {
                backFace.padding(.top, 16)
            }
        }
        .onChange(of: state.phase) { _, phase in
            if phase == "Revealed" { setRevealed(true) }
        }
        // A new exercise always starts collapsed on its question, regardless of how the previous
        // card was left — never animated, so the next question never visibly "un-expands".
        .onChange(of: exercise.id) { _, _ in setRevealed(false, animated: false) }
        .onAppear { setRevealed(state.phase == "Revealed", animated: false) }
    }

    private func setRevealed(_ newValue: Bool, animated: Bool = true) {
        if animated && !reduceMotion {
            withAnimation(.spring(response: 0.5, dampingFraction: 0.82)) { revealed = newValue }
        } else {
            revealed = newValue
        }
    }

    @ViewBuilder private var frontFace: some View {
        VStack(alignment: .leading, spacing: 8) {
            styleQuickSwitch
            tapToRevealContent
            if !state.introPending {
                frontStyleBlocks
            }
            if !state.introPending && state.phase == "Question" {
                answerControls
            }
        }
    }

    /// UC-10 S2: the current style's `Front`-phase blocks (Formula/Table for rule-first, Scene for
    /// situation-first, NativeParallel for native-contrast, Examples for minimal-theory), rendered
    /// generically by `BlockKind` — never a per-style `if` here (`Plans/Kotlin/StylesBlueprint.md`
    /// §6). Kept present after reveal too: `frontFace` stays mounted alongside `backFace` under D1,
    /// and `styleBlocksSnapshot` gives Front its own blocks independent of `state.phase` for exactly
    /// this reason.
    @ViewBuilder private var frontStyleBlocks: some View {
        if let blocks = state.styleBlocks?.frontBlocks, !blocks.isEmpty {
            VStack(alignment: .leading, spacing: 12) {
                ForEach(Array(blocks.enumerated()), id: \.offset) { _, block in
                    MacStyleBlockView(block: block, reduceMotion: reduceMotion)
                }
            }
        }
    }

    /// UC-10 S1: a compact quick switch for the 4 presentation styles, mirroring the `Способ
    /// ответа` picker's own persist-through-preferences path (`model.preference`, not a direct
    /// session command) so a style chosen here also becomes the new default. Shown in both
    /// `Question` and `Revealed` phases — switching only ever `mutate`s `styleId` in
    /// `TrainingStore` (never creates a review, never touches `draft`/`frozenAnswer`).
    private var styleQuickSwitch: some View {
        Picker("Стиль объяснений", selection: Binding(
            get: { state.styleId },
            set: { model.preference("styleId", $0) }
        )) {
            ForEach(model.preferences?.styles ?? []) { style in
                Text(style.label).tag(style.id)
            }
        }
        .pickerStyle(.menu)
        .labelsHidden()
        .controlSize(.small)
        .fixedSize()
        .accessibilityIdentifier("styleQuickSwitch")
    }

    // D1: tapping the sentence/task-prompt region reveals the card too, alongside the
    // button/⌘Return — scoped to just this non-interactive block, never to the Picker/TextField/
    // reveal-Button below (a sibling, not a descendant, of this gesture: see `answerControls` in
    // `frontFace`) and never while `introPending` (its own "Перейти к заданию" button lives inside
    // this block and must keep sole gesture priority there). This mirrors the web reference's
    // `front`-only tap target (`TrainingWebApp.kt`'s `installTapGesture(front) { ... }`, attached
    // only to the sentence/operation element and excluded via `!state.introPending`, never to its
    // sibling `renderAnswerArea`) and the iOS sibling's phase-exclusive scoping in
    // `FlashCardView.swift` — both keep the gesture off any view that hosts its own controls,
    // rather than relying on an ancestor gesture yielding priority to descendants.
    @ViewBuilder private var tapToRevealContent: some View {
        let block = VStack(alignment: .leading, spacing: 8) {
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
            }
        }
        if !state.introPending && state.phase == "Question" {
            block.contentShape(Rectangle()).onTapGesture { model.send("reveal", exercise.id) }
        } else {
            block
        }
    }

    @ViewBuilder private var answerControls: some View {
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

    @ViewBuilder private var backFace: some View {
        VStack(alignment: .leading, spacing: 16) {
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
            }
            .transition(reduceMotion ? .identity : .revealGroup(delay: 0.05))

            // UC-10 S2: the current style's `Back`-phase blocks, rendered generically by
            // `BlockKind` (`Plans/Kotlin/StylesBlueprint.md` §6) — replaces the previous
            // hardcoded Changes+Formula pair. `Changes` is exercise data, not style data (ST-05),
            // so every style's recipe still lists it and it shows the same "Что изменилось" list
            // as before, just built from the block's own highlighted spans; the feedback/
            // explanation strings the old Formula box appended are the method lead/review lines
            // shown elsewhere (`methodFeedback`), not part of any block.
            VStack(alignment: .leading, spacing: 16) {
                ForEach(Array((state.styleBlocks?.backBlocks ?? []).enumerated()), id: \.offset) { _, block in
                    MacStyleBlockView(block: block, reduceMotion: reduceMotion)
                }
                if let feedback = exercise.methodFeedback, !feedback.isEmpty { Text(feedback) }
                if let explanation = exercise.explanation, !explanation.isEmpty { Text(explanation) }
            }
            .transition(reduceMotion ? .identity : .revealGroup(delay: 0.14))

            VStack(alignment: .leading, spacing: 8) {
                Text("Свайп влево — повторить · вправо — вспомнил")
                    .font(.footnote).foregroundStyle(.secondary)
                    .accessibilityIdentifier("ratingSwipeArea")
                HStack(spacing: 10) {
                    Spacer()
                    RatingButton(label: "Повторить", prominent: false,
                                 dueMs: exercise.intervals?.again, nowMs: state.now) {
                        model.send("rate", "\(exercise.id)|Again")
                    }
                    .keyboardShortcut("1", modifiers: [.command])
                    .accessibilityIdentifier("rateAgain")
                    RatingButton(label: "Вспомнил", prominent: true,
                                 dueMs: exercise.intervals?.good, nowMs: state.now) {
                        model.send("rate", "\(exercise.id)|Good")
                    }
                    .keyboardShortcut("2", modifiers: [.command])
                    .accessibilityIdentifier("rateGood")
                    // D3: ArrowLeft/ArrowRight rate too, alongside ⌘1/⌘2 — hidden buttons rather
                    // than a second `.keyboardShortcut` on the visible ones (SwiftUI only keeps the
                    // last shortcut set on a control). Only mounted while this back face is, i.e.
                    // only in the `Revealed` phase — the typed-answer `TextField` (where arrow keys
                    // must move the caret, not rate) only ever exists in the `Question` phase's
                    // `answerControls`, so the two never coexist and no extra focus-gating is needed.
                    Button("") { model.send("rate", "\(exercise.id)|Again") }
                        .keyboardShortcut(.leftArrow, modifiers: [])
                        .frame(width: 0, height: 0).hidden().accessibilityHidden(true)
                    Button("") { model.send("rate", "\(exercise.id)|Good") }
                        .keyboardShortcut(.rightArrow, modifiers: [])
                        .frame(width: 0, height: 0).hidden().accessibilityHidden(true)
                }
            }
            .transition(reduceMotion ? .identity : .revealGroup(delay: 0.22))
        }
    }
}

/// Short per-group stagger for the expand-reveal (D1), mirroring the web's `.revealing` fade-in
/// delays on `.card-answer-wrap`'s three part groups (`.4s ease-out` at increasing delays). Each
/// group only ever *inserts* once — `backFace` is only mounted while `revealed`, and `revealed`
/// only flips true→false unanimated on the next exercise — so this plays once on the real reveal
/// and never replays on an unrelated re-render (e.g. the 30s refresh timer) of an already-answered
/// card, the same guarantee the web's own `.revealing`-once-per-render class gives.
private extension AnyTransition {
    static func revealGroup(delay: Double) -> AnyTransition {
        .modifier(
            active: RevealGroupFade(offset: 10, opacity: 0),
            identity: RevealGroupFade(offset: 0, opacity: 1)
        ).animation(.easeOut(duration: 0.4).delay(delay))
    }
}

private struct RevealGroupFade: ViewModifier {
    let offset: CGFloat
    let opacity: Double
    func body(content: Content) -> some View {
        content.offset(y: offset).opacity(opacity)
    }
}
