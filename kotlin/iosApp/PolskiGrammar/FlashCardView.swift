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
            emphasizedText(card.rows("sourceParts"), role: .before, accessibilityLabel: card.string("source"))
                .font(.system(.title2, design: .rounded, weight: .semibold))
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
                // StylesBlueprint.md §1/S2: the current style's own front-of-card support — e.g.
                // rule-first's formula+table, situation-first's scene — shown *before* reveal.
                ForEach(Array(state.record("styleBlocks").rows("front").enumerated()), id: \.offset) { _, block in
                    styleBlockView(block)
                }
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
        // StylesBlueprint.md §1/S2: back blocks replace the old fixed "Что изменилось"/"ЗАПОМНИ"
        // sections — which style/blocks show here now comes entirely from the composer (a style
        // without Formula, e.g. situation-first, simply has no amber box any more).
        let blocks = state.record("styleBlocks").rows("back")
        VStack(alignment: .leading, spacing: 12) {
            VStack(alignment: .leading, spacing: 12) {
                Text("Эталон").font(.caption).foregroundStyle(.secondary)
                emphasizedText(card.rows("expectedParts"), role: .after, accessibilityLabel: card.string("expected"))
                    .font(.system(.title2, design: .rounded, weight: .semibold))
                if !card.strings("accepted").isEmpty { Text("Также: \(card.strings("accepted").joined(separator: " / "))") }
                if state.string("answerMode") == "Typed" {
                    Label(card.bool("correct") ? "Совпадает с правильным вариантом" : "Сравни свой ответ с эталоном",
                          systemImage: card.bool("correct") ? "checkmark.circle" : "info.circle")
                    Text(card.string("frozenAnswer").isEmpty ? "Ответ не введён" : card.string("frozenAnswer"))
                }
                // The retrieve/feedback/review cycle text is not a style block (StylesBlueprint.md
                // §1) — it stays independent of which blocks the current style composes, always
                // shown here, the same one canonical spot for every style.
                Text(card.string("methodFeedback"))
                Text(card.string("explanation"))
            }
            .transition(staggeredReveal(0))

            ForEach(Array(blocks.enumerated()), id: \.offset) { index, block in
                styleBlockView(block)
                    .transition(staggeredReveal(index + 1))
            }

            VStack(alignment: .leading, spacing: 9) {
                Text("Когда повторить?").font(.headline)
                Text(card.string("methodReview"))
                Text("Свайп влево — повторить · вправо — вспомнил")
                    .font(.footnote).foregroundStyle(.secondary)
                    // See `SwipeToRate`'s own doc comment for why this identifier lives on this
                    // long-text leaf specifically, and not on the shared gesture container.
                    .accessibilityIdentifier("ratingSwipeArea")
                    // VoiceOver intercepts raw finger drags for its own navigation, so the swipe
                    // gesture above is unreachable with VoiceOver on. These two rotor actions,
                    // reached via VoiceOver's Actions rotor once focus lands on this hint, are the
                    // only rating path for a VoiceOver user — no visible button is added.
                    .accessibilityActions {
                        Button("Повторить") { rate(remembered: false) }
                        Button("Вспомнил") { rate(remembered: true) }
                    }
            }
            .padding(.vertical, 4)
            .transition(staggeredReveal(blocks.count + 1))
        }
        // D3: no rating buttons on touch — the whole panel above is the swipe-to-rate gesture
        // surface. D1 already removed the flip-back tap this container used to share with the old
        // drag, so there is no reverse action on this panel any more, only the rating drag.
        .swipeToRate(active: state.string("phase") == "Revealed", reduceMotion: reduceMotion) { remembered in
            rate(remembered: remembered)
        }
    }

    private func rate(remembered: Bool) {
        guard state.string("phase") == "Revealed" else { return }
        model.send("rate", remembered ? "Good" : "Again")
    }
}

/// `ContrastHighlightPlan.md` §"Контракт выделения" — the two emphasis roles every before/after
/// pair on this card uses: `before` (warm red, dashed) is the old, still-correct form; `after`
/// (cool accent, solid) is the new one. Both tokens live in one place — `Assets.xcassets`'
/// `EmphasisBefore`/`EmphasisAfter` colorsets (light/dark variants, each ≥4.5:1 against the card's
/// background in both) — so every call site below shares them instead of each picking its own color.
private enum EmphasisRole {
    case before, after

    var color: Color {
        switch self {
        case .before: return Color("EmphasisBefore")
        case .after: return Color("EmphasisAfter")
        }
    }
}

/// One word-internal run: [text] with [changed] marking whether [EmphasisRole] styling applies.
private struct EmphasisSegment { let text: String; let changed: Bool }

/// One whitespace-delimited word (its own trailing space, if any, kept as the word's own last
/// segment — see [emphasisWords]) built from one or more `EndingPart` runs, e.g. an unchanged stem
/// segment followed by a changed ending segment. The atomic unit [WrapLayout] wraps at, so a
/// changed segment's underline (drawn by [EmphasisWordView]) always travels with its own word.
private struct EmphasisWord: Identifiable {
    let id = UUID()
    let segments: [EmphasisSegment]
}

/// Splits `EndingPart` runs (`{text, changed}`, `card.rows("sourceParts"|"expectedParts")` or a
/// style block's `before`/`after` rows) into [EmphasisWord]s on whitespace, keeping each word's own
/// trailing space glued to it (so [WrapLayout] can space words using the font's own real space
/// glyph instead of a guessed constant) and preserving which characters are `changed`.
private func emphasisWords(_ parts: [Record]) -> [EmphasisWord] {
    var words: [EmphasisWord] = []
    var current: [EmphasisSegment] = []
    var hasContent = false
    for part in parts {
        let changed = part.bool("changed")
        var buffer = ""
        for ch in part.string("text") {
            buffer.append(ch)
            if ch.isWhitespace {
                current.append(EmphasisSegment(text: buffer, changed: changed))
                buffer = ""
                if hasContent {
                    words.append(EmphasisWord(segments: current))
                    current = []
                    hasContent = false
                }
            } else {
                hasContent = true
            }
        }
        if !buffer.isEmpty { current.append(EmphasisSegment(text: buffer, changed: changed)) }
    }
    if !current.isEmpty { words.append(EmphasisWord(segments: current)) }
    return words
}

/// A left-to-right flow layout, wrapping to a new line whenever the next child would overflow —
/// lays out [EmphasisWord]s the way text itself wraps. `spacing` is 0: inter-word space comes from
/// each word's own trailing-space segment (see [emphasisWords]), not a layout gap.
private struct WrapLayout: Layout {
    var spacing: CGFloat = 0

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let width = proposal.width ?? .infinity
        var x: CGFloat = 0, y: CGFloat = 0, lineHeight: CGFloat = 0
        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x > 0, x + size.width > width { x = 0; y += lineHeight; lineHeight = 0 }
            x += size.width + spacing
            lineHeight = max(lineHeight, size.height)
        }
        return CGSize(width: width.isFinite ? width : x, height: y + lineHeight)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x = bounds.minX, y = bounds.minY, lineHeight: CGFloat = 0
        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x > bounds.minX, x + size.width > bounds.minX + bounds.width { x = bounds.minX; y += lineHeight; lineHeight = 0 }
            subview.place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(size))
            x += size.width + spacing
            lineHeight = max(lineHeight, size.height)
        }
    }
}

/// A straight horizontal line filling whatever size its view is given — the geometry
/// [EmphasisWordView] strokes solid (`after`) or dashed (`before`).
private struct UnderlineShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.minX, y: rect.midY))
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.midY))
        return path
    }
}

/// One [EmphasisWord]; each [EmphasisSegment.changed] run is bold, [role]-colored, and underlined
/// with a hand-drawn [UnderlineShape] — dashed for `before`, solid for `after`. Not
/// `Text.underline(pattern: .dash)`: confirmed on the iOS 17 simulator
/// (`EmphasisUXAudit-2026-09-27.md` E9, re-checked against this fix's own first attempt, an
/// `AttributedString`-backed `Text` with a `.dash` `underlineStyle`) that SwiftUI silently renders
/// that pattern solid regardless of how the `Text` is built — this shape is the contract's own
/// required fallback ("Если платформа не рисует пунктир… хост рисует его собственной фигурой").
private struct EmphasisWordView: View {
    let word: EmphasisWord
    let role: EmphasisRole

    var body: some View {
        HStack(spacing: 0) {
            ForEach(Array(word.segments.enumerated()), id: \.offset) { _, segment in
                if segment.changed {
                    Text(segment.text)
                        .fontWeight(.bold)
                        .foregroundColor(role.color)
                        .overlay(alignment: .bottom) {
                            UnderlineShape()
                                .stroke(role.color, style: StrokeStyle(
                                    lineWidth: 2, dash: role == .before ? [4, 3] : []))
                                .frame(height: 2)
                                .offset(y: 4)
                        }
                } else {
                    Text(segment.text)
                }
            }
        }
    }
}

/// Renders `EndingPart` runs (`{text, changed}`) as a wrapping sentence/phrase, changed spans
/// marked per [role] (`ContrastHighlightPlan.md` §"Контракт выделения"). `accessibilityElement`
/// collapses the many per-word `Text`s this builds back into one spoken label — the same single
/// element shape the old one-`Text` rendering gave VoiceOver, and every existing
/// `app.staticTexts[...]` UI test still looks up by that exact label. `.isStaticText` is added
/// explicitly: unlike a plain `Text` (which carries that trait natively), a generic
/// `accessibilityElement(children: .ignore)` container defaults to no traits, and XCUITest's
/// `staticTexts` query — every one of those call sites — matches only elements that carry it.
private func emphasizedText(_ parts: [Record], role: EmphasisRole, accessibilityLabel: String) -> some View {
    WrapLayout {
        ForEach(emphasisWords(parts)) { word in
            EmphasisWordView(word: word, role: role)
        }
    }
    .accessibilityElement(children: .ignore)
    .accessibilityAddTraits(.isStaticText)
    .accessibilityLabel(accessibilityLabel)
}

/// Shared by every block view below that shows a before/after pair (`table`, `changes`, `contrast`)
/// — the accessible label is simply every part's own text joined back together, the same content
/// VoiceOver read off the old concatenated `Text` (no separate string carries it at these call sites).
private func styledParts(_ parts: [Record], role: EmphasisRole) -> some View {
    emphasizedText(parts, role: role, accessibilityLabel: parts.map { $0.string("text") }.joined())
}

// MARK: - StylesBlueprint.md §1/§4/S2 — one view per `Block` kind (`BlockKind.name.lowercase-first`
// wire tag, see `blocksToJson`), rendered from whatever `StyleComposer.compose` returned for the
// current style — never a style/kind literal branch in the host beyond this single `switch`.
extension FlashCardView {
    @ViewBuilder
    fileprivate func styleBlockView(_ block: Record) -> some View {
        switch block.string("kind") {
        case "formula": StyleFormulaBlock(text: block.string("text"))
        case "rule": StyleRuleBlock(text: block.string("text"), detail: block.string("detail"))
        case "table": StyleTableBlock(caption: block.string("caption"), rows: block.rows("rows"))
        case "scene": StyleSceneBlock(text: block.string("text"))
        case "nativeParallel": StyleNativeParallelBlock(pairs: block.rows("pairs"))
        case "examples": StyleExamplesBlock(items: block.strings("items"))
        case "whyOnDemand": StyleWhyOnDemandBlock(
            text: block.string("text"), label: block.string("collapsedLabel"), reduceMotion: reduceMotion)
        case "changes": StyleChangesBlock(items: block.rows("items"))
        case "contrast": StyleContrastBlock(before: block.rows("before"), after: block.rows("after"))
        default: EmptyView()
        }
    }
}

/// `Block.Formula` — unchanged from the pre-S2 "ЗАПОМНИ" box, now shown whenever the current
/// style's own recipe lists `formula` for that phase (rule-first front *and* back — TABLE 1) rather
/// than unconditionally.
private struct StyleFormulaBlock: View {
    let text: String
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("ЗАПОМНИ").font(.caption.weight(.semibold))
            Text(text).font(.headline).bold()
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(Color.accentColor.opacity(0.12), in: RoundedRectangle(cornerRadius: 16))
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier("styleBlock-formula")
    }
}

/// `Block.Rule` — theory prose (unchanged visual weight from the pre-S2 explanation text), plus
/// [detail] (`Exercise.explanation`, fix f8742fc) shown underneath as its own sub-element — parity
/// with web's `CardBlocksWeb.kt` (`Block.Rule` → two `<p>`s) and macOS/Android's own D2 fix, and the
/// same "container id + `-text`-suffixed sub id" shape [StyleWhyOnDemandBlock] below already uses.
private struct StyleRuleBlock: View {
    let text: String
    let detail: String
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(text)
                .accessibilityIdentifier("styleBlock-rule")
            if !detail.isEmpty {
                Text(detail)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .accessibilityIdentifier("styleBlock-rule-detail")
            }
        }
    }
}

/// `Block.Table` — a compact endings table: one row per `TableRow`, before/after highlighted like
/// every other before/after pair in this app (`NativeContrastPairView`, `styledParts`).
private struct StyleTableBlock: View {
    let caption: String
    let rows: [Record]
    var body: some View {
        Group {
            if !rows.isEmpty {
                VStack(alignment: .leading, spacing: 6) {
                    if !caption.isEmpty { Text(caption).font(.caption.weight(.semibold)).foregroundStyle(.secondary) }
                    ForEach(Array(rows.enumerated()), id: \.offset) { _, row in
                        HStack(alignment: .firstTextBaseline, spacing: 6) {
                            if !row.string("label").isEmpty {
                                Text(row.string("label")).font(.footnote).foregroundStyle(.secondary)
                            }
                            styledParts(row.rows("before"), role: .before)
                            Text("→").foregroundStyle(.secondary)
                            styledParts(row.rows("after"), role: .after)
                        }
                    }
                }
                .accessibilityElement(children: .combine)
                .accessibilityIdentifier("styleBlock-table")
            }
        }
    }
}

/// `Block.Scene` — a short situation, shown quote-like (situation-first's front block).
private struct StyleSceneBlock: View {
    let text: String
    var body: some View {
        HStack(alignment: .top, spacing: 8) {
            Image(systemName: "quote.opening").foregroundStyle(.secondary).accessibilityHidden(true)
            Text(text).italic()
        }
        .padding(12)
        .background(Color(uiColor: .secondarySystemBackground), in: RoundedRectangle(cornerRadius: 12))
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(text)
        .accessibilityIdentifier("styleBlock-scene")
    }
}

/// `Block.NativeParallel` — a two-column row per pair (native ↔ target) with a trailing
/// match/differs badge (icon-only; the accessible label carries the state in words instead, so
/// VoiceOver doesn't just hear "image").
private struct StyleNativeParallelBlock: View {
    let pairs: [Record]
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            ForEach(Array(pairs.enumerated()), id: \.offset) { _, pair in
                let matches = pair.bool("matches")
                VStack(alignment: .leading, spacing: 3) {
                    HStack(alignment: .firstTextBaseline, spacing: 8) {
                        Text(pair.string("native")).foregroundStyle(.secondary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                        Image(systemName: "arrow.right").font(.caption).foregroundStyle(.tertiary)
                        Text(pair.string("target")).fontWeight(.semibold)
                            .frame(maxWidth: .infinity, alignment: .leading)
                        Image(systemName: matches ? "checkmark.circle.fill" : "exclamationmark.triangle.fill")
                            .foregroundStyle(matches ? Color(uiColor: .systemGreen) : Color(uiColor: .systemOrange))
                            .accessibilityHidden(true)
                    }
                    if !pair.string("note").isEmpty {
                        Text(pair.string("note")).font(.footnote).foregroundStyle(.secondary)
                    }
                }
                .accessibilityElement(children: .ignore)
                .accessibilityLabel(
                    "\(pair.string("native")); \(pair.string("target")); "
                    + (matches ? "совпадает" : "отличается")
                    + (pair.string("note").isEmpty ? "" : "; \(pair.string("note"))"))
            }
        }
        .accessibilityIdentifier("styleBlock-nativeParallel")
    }
}

/// `Block.Examples` — a plain list; an empty list (no authored examples yet for this skill) renders
/// nothing rather than an empty heading.
private struct StyleExamplesBlock: View {
    let items: [String]
    var body: some View {
        Group {
            if !items.isEmpty {
                VStack(alignment: .leading, spacing: 4) {
                    ForEach(Array(items.enumerated()), id: \.offset) { _, item in
                        Text("• \(item)")
                    }
                }
                .accessibilityIdentifier("styleBlock-examples")
            }
        }
    }
}

/// `Block.WhyOnDemand` — collapsed by default, an animated disclosure that respects [reduceMotion]
/// and stays reachable as a single VoiceOver-focusable toggle. `label` is CORE's `collapsedLabel`,
/// which names no language (StylesBlueprint.md §1) — empty falls back to this file's own copy, the
/// same pattern `PolskiGrammarApp.swift`'s `styleFallbackLabel` already uses for style names.
private struct StyleWhyOnDemandBlock: View {
    let text: String
    let label: String
    let reduceMotion: Bool
    @State private var expanded = false
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Button {
                if reduceMotion { expanded.toggle() } else {
                    withAnimation(.easeOut(duration: 0.25)) { expanded.toggle() }
                }
            } label: {
                HStack(spacing: 4) {
                    Text(label.isEmpty ? "Почему так?" : label)
                    Image(systemName: expanded ? "chevron.up" : "chevron.down")
                }
            }
            .accessibilityIdentifier("styleBlock-whyOnDemand")
            .accessibilityValue(expanded ? "развёрнуто" : "свёрнуто")
            if expanded {
                Text(text)
                    .transition(reduceMotion ? .identity : .opacity.combined(with: .move(edge: .top)))
                    .accessibilityIdentifier("styleBlock-whyOnDemand-text")
            }
        }
    }
}

/// `Block.Changes` — the exercise's own diff (identical across styles, ST-05), now rendered from
/// the composed block instead of `card.rows("changes")` directly — same data, one less special case.
private struct StyleChangesBlock: View {
    let items: [Record]
    var body: some View {
        Group {
            if !items.isEmpty {
                VStack(alignment: .leading, spacing: 6) {
                    Text("Что изменилось").font(.headline)
                    ForEach(Array(items.enumerated()), id: \.offset) { _, item in
                        VStack(alignment: .leading, spacing: 3) {
                            HStack(spacing: 4) {
                                styledParts(item.rows("before"), role: .before)
                                Text("→").foregroundStyle(.secondary)
                                styledParts(item.rows("after"), role: .after)
                            }
                            Text(item.string("reason")).foregroundStyle(.secondary)
                        }
                    }
                }
                .accessibilityIdentifier("styleBlock-changes")
            }
        }
    }
}

/// `Block.Contrast` — the focus form's before→after, same big-font treatment the old fixed
/// "rule-contrast" box used, now shown only for styles whose recipe actually lists `contrast`.
private struct StyleContrastBlock: View {
    let before: [Record]
    let after: [Record]
    var body: some View {
        HStack(spacing: 6) {
            styledParts(before, role: .before)
            Text("→").foregroundStyle(.secondary)
            styledParts(after, role: .after)
        }
        .font(.system(.title3, design: .rounded, weight: .semibold))
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier("styleBlock-contrast")
    }
}
