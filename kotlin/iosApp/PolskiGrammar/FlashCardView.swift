import SwiftUI
#if DEBUG
import os
#endif

#if DEBUG
/// `os_signpost` interval names in this file share this subsystem/category with
/// `RiveEffectOverlay.swift`'s own log (FC2-17, R4) so `xctrace`/`XCTOSSignpostMetric` can
/// correlate flip cost against Rive playback cost from the same trace.
private let flipSignpostLog = OSLog(subsystem: "dev.polski.grammarmatrix", category: "flip")

/// Test-only duration multiplier (FC2-05's proof): `POLSKI_FLIP_DEBUG_SCALE`, read once, defaults
/// to 1 (production speed) whenever it is absent, non-numeric or non-positive. Only ever set by
/// `XCTest`'s `launchEnvironment` (see `FlipCorrectnessUITests.swift`) — never by a person — and
/// only compiled into Debug builds at all, so it can never affect a Release flip's timing.
private let flipDebugScale: Double = {
    guard let raw = ProcessInfo.processInfo.environment["POLSKI_FLIP_DEBUG_SCALE"],
          let value = Double(raw), value > 0 else { return 1 }
    return value
}()
#endif

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
///
/// **FC2-05 (R1):** the face swap happens exactly at the 90° midpoint, not on a fixed timer that
/// only approximates it. `setFlipped` runs two sequential `withAnimation`s — 0°→90° ease-in, then
/// (only once that one's completion handler actually fires) 90°→final ease-out — and swaps
/// `showBack` in the gap between them, at the real edge-on point, in both directions.
struct FlashCardView<RevealButton: View>: View {
    @ObservedObject var model: AppModel
    let state: Record
    let card: Record
    @Binding var localDraft: String
    var answerFocused: FocusState<Bool>.Binding
    /// FC-09/12/14/20's shared gate (system Reduce Motion OR the app's `Motion.Reduced`): snaps the
    /// flip instead of animating it. Computed once by the caller (`TrainingView.cardMotionReduced`).
    let reduceMotion: Bool
    /// FC2-06/07 (R2): called `true` right as an animated flip's first half starts and `false` once
    /// its second half settles — the caller (`TrainingView`) feeds this into `RiveFlipRingsOverlay`.
    /// Never called for a `reduceMotion` (snap) flip, matching the ring cue's own reduced-motion gate.
    var onRingsExpandedChange: (Bool) -> Void = { _ in }
    let revealButton: (_ title: String, _ expands: Bool) -> RevealButton

    @State private var flipped = false
    @State private var showBack = false
    @State private var rotation: Double = 0

    /// Each half's duration: 0.25s in production, scaled only under `#if DEBUG` by
    /// `POLSKI_FLIP_DEBUG_SCALE` (FC2-05's correctness proof) — see the file-level doc comment.
    private var halfDuration: Double {
        #if DEBUG
        return 0.25 * flipDebugScale
        #else
        return 0.25
        #endif
    }

    var body: some View {
        Group {
            // At rest during `Question` (rotation is always 0 there — the flip gesture is only ever
            // attached once `Revealed`, see `frontFace` below), skip the `rotation3DEffect` modifier
            // entirely rather than applying it with `.degrees(0)`: even a nominally-identity 3D
            // transform ancestor confused a Menu-style Picker's popup anchoring in an XCUITest run
            // (the "Ответ" answer-mode picker's "Напечатать" option became untappable) — that Picker
            // only ever exists during `Question`, so this is the only branch that needs the skip.
            //
            // FC2-05 (R1): gating this skip on `rotation == 0` instead of on `phase == "Question"`
            // was a real, reproduced bug — every front→back flip starts from `rotation == 0`, and
            // adding a brand-new `rotation3DEffect` modifier for the first time is a *structural*
            // view change (a different branch of this `if`/`else`, hence a different view identity),
            // not a continuous property mutation on an already-mounted one. `withAnimation` cannot
            // meaningfully interpolate a transform that did not exist a moment ago, so its completion
            // fired almost immediately instead of after the requested duration — confirmed with
            // `os_log` timestamps: the modifier-insertion flip settled in ~0.68s against a requested
            // 5s `POLSKI_FLIP_DEBUG_SCALE` duration, while a flip that only *mutates* an
            // already-attached modifier (back→front, starting at `rotation == 180`) took the full,
            // correct ~5s. Every reachable front→back flip happens only once `Revealed` (never during
            // `Question`, see above), so gating on `phase` instead keeps the Picker fix exactly as
            // narrow as it always needed to be, while keeping the modifier continuously mounted
            // (even at a nominally-identity `.degrees(0)`) for every animation that touches it.
            if showBack {
                backFace.rotation3DEffect(.degrees(-180), axis: (x: 0, y: 1, z: 0))
                    .rotation3DEffect(.degrees(rotation), axis: (x: 0, y: 1, z: 0), perspective: 0.35)
            } else if rotation == 0 && state.string("phase") == "Question" {
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
        guard animated && !reduceMotion else {
            rotation = newValue ? 180 : 0
            showBack = newValue
            return
        }
        // This flip's very first animated call is reached from `.onChange(of: state.string("phase"))`
        // — synchronously, within the *same* SwiftUI update transaction that just changed `phase`
        // itself (from "Question" to "Revealed"). Right up above, that phase change is *also* what
        // flips `frontFace`'s `rotation3DEffect` modifier from absent to present (see the file-level
        // gate a few lines up) — so, without this dispatch, the modifier's first-ever appearance and
        // its very first animated mutation would land in that same transaction, with no earlier
        // commit showing it already mounted at `rotation == 0` for `withAnimation` to interpolate
        // *from*. Confirmed with `os_signpost`/log timestamps, not assumed: that produced exactly the
        // same class of bug FC2-01/02 found and fixed on the web host ("no previous frame to
        // transition from") — the completion handler fired after ~1s instead of the requested
        // (scaled) duration, because SwiftUI had no prior frame to animate the insert from. Deferring
        // by one run-loop turn lets the phase-change transaction commit and render on its own first,
        // so the modifier truly already exists, at its resting value, by the time this animates it.
        DispatchQueue.main.async { [self] in
            #if DEBUG
            let signpostID = OSSignpostID(log: flipSignpostLog)
            os_signpost(.begin, log: flipSignpostLog, name: "FlipHalf1", signpostID: signpostID)
            #endif
            onRingsExpandedChange(true)
            // Mid is always 90° regardless of direction — the halfway point between 0 and 180 either
            // way — so this single ease-in phase covers both a forward and a backward flip.
            withAnimation(.easeIn(duration: halfDuration)) {
                rotation = 90
            } completion: {
                #if DEBUG
                os_signpost(.end, log: flipSignpostLog, name: "FlipHalf1", signpostID: signpostID)
                let signpostID2 = OSSignpostID(log: flipSignpostLog)
                os_signpost(.begin, log: flipSignpostLog, name: "FlipHalf2", signpostID: signpostID2)
                #endif
                // The swap happens right here, in the gap between the two animations — genuinely
                // edge-on (90°, invisible either way), not on a timer that merely assumes the real
                // ease curve is symmetric.
                showBack = newValue
                withAnimation(.easeOut(duration: halfDuration)) {
                    rotation = newValue ? 180 : 0
                } completion: {
                    #if DEBUG
                    os_signpost(.end, log: flipSignpostLog, name: "FlipHalf2", signpostID: signpostID2)
                    #endif
                    onRingsExpandedChange(false)
                }
            }
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
