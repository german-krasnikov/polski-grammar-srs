import SwiftUI

/// Vocabulary flash card for the "Слова" screen (`Plans/Kotlin/FlipCardRivePlan.md` D2). Mirrors
/// the web reference host natively (`VocabularyWeb.kt`'s `FlipCard` / `installTapGesture`, read but
/// never edited here):
///
/// - The whole rounded panel — background, border, corner radius, shadow — flips as **one**
///   object: they are attached to this view's own outer container, never to the face content
///   that swaps inside it, so the panel itself is what rotates.
/// - Unrevealed: there is no separate "Показать ответ" button any more. [questionFace]'s prompt
///   block is the accessible reveal control (`accessibilityLabel("Показать ответ")`, `.isButton`
///   trait, identifier `vocabularyReveal` — the same identifier the old button used, so existing
///   callers keep working). A tap on it reveals the domain answer once (`sendVocabulary("reveal")`)
///   *and* flips to face it, driven downstream of `revealed` turning true — the same
///   "visual state stays downstream of domain state" contract `FlashCardView`'s D1 reveal uses.
///   The gesture lives on the prompt block only, a sibling of the mode `Toggle` / typed `TextField`
///   / "Проверить" button — never their ancestor — the exact split D1's own doc comment calls out
///   to avoid the FC2 class of bug where an ancestor tap gesture breaks a sibling control.
/// - Revealed: tapping the card again flips it back and forth **purely visually** (`flipped`) —
///   it never re-reveals (that guard lives in `onReveal`'s caller) and never itself rates; a
///   horizontal drag on the same revealed card rates instead (D3), never the tap.
/// - The face swap happens at exactly 90° of the 180° rotation, both directions: `showBack` flips
///   at the animation's halfway point, the same discrete swap a CSS 3D flip does.
///
/// D3 (`Plans/Kotlin/FlipCardRivePlan.md`): no rating buttons on touch — [SwipeToRate] is attached
/// to this view's own **outer** container (below), the same one the flip's own `rotation3DEffect`
/// and background/border/shadow already move as one object, so a rating drag carries the WHOLE
/// panel, not just its face content (`active: showBack` keeps a drag on the still-showing question
/// face inert — it must never rate).
struct VocabularyCardView: View {
    @ObservedObject var model: AppModel
    let state: Record
    let card: Record
    /// System Reduce Motion OR the app's `Motion.Reduced` — snaps the flip instead of animating it,
    /// mirroring `TrainingView.cardMotionReduced`.
    let reduceMotion: Bool

    @State private var flipped = false
    @State private var showBack = false

    private var revealed: Bool { state.bool("revealed") }
    private var polishAnswer: Bool { state.string("direction") == "ru-pl" }
    private let flipDuration = 0.5

    var body: some View {
        Group {
            if showBack { answerFace } else { questionFace }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 20, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 20, style: .continuous).strokeBorder(.separator))
        .shadow(color: .black.opacity(0.08), radius: 6, y: 2)
        .rotation3DEffect(.degrees(flipped ? 180 : 0), axis: (x: 0, y: 1, z: 0))
        .swipeToRate(active: showBack, reduceMotion: reduceMotion, cornerRadius: 20) { remembered in
            guard !state.bool("busy") else { return }
            model.sendVocabulary(remembered ? "good" : "again")
        }
        // A new due item always starts question-side-up, never inheriting the previous item's
        // face — never animated, so the next card never visibly "un-reveals" (mirrors D1's own
        // `onChange(of: card.string("id"))`).
        .onChange(of: state.string("currentId")) { _, _ in setFlipped(false, animated: false) }
        .onChange(of: revealed) { _, isRevealed in if isRevealed { setFlipped(true) } }
        .onAppear { setFlipped(revealed, animated: false) }
    }

    @ViewBuilder private var questionFace: some View {
        VStack(alignment: .leading, spacing: 12) {
            VStack(alignment: .leading, spacing: 4) {
                Text(polishAnswer ? "Вспомни по-польски" : "Вспомни по-русски")
                    .font(.caption).foregroundStyle(.secondary)
                Text(polishAnswer ? card.string("translation") : card.string("lemma"))
                    .font(.title2.weight(.semibold))
            }
            .contentShape(Rectangle())
            .accessibilityElement(children: .combine)
            .accessibilityAddTraits(.isButton)
            .accessibilityLabel("Показать ответ")
            .accessibilityIdentifier("vocabularyReveal")
            .onTapGesture { model.sendVocabulary("reveal") }

            Toggle("Напечатать ответ", isOn: Binding(
                get: { state.bool("typed") },
                set: { model.sendVocabulary("typed", $0 ? "true" : "false") }
            ))
            if state.bool("typed") {
                TextField("Твой ответ", text: Binding(
                    get: { state.string("draft") },
                    set: { model.sendVocabulary("draft", $0) }
                ))
                .textInputAutocapitalization(.never)
                .accessibilityIdentifier("vocabularyDraft")
                Button("Проверить") { model.sendVocabulary("reveal") }
                    .buttonStyle(.borderedProminent)
                    .accessibilityIdentifier("vocabularyCheck")
            }
        }
    }

    @ViewBuilder private var answerFace: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(polishAnswer ? card.string("lemma") : card.string("translation"))
                .font(.title2.weight(.bold))
                // A stable, non-button tap target for the visual-flip-back UI test — deliberately
                // not a button and not wired to any action itself; the whole answer face's own
                // `onTapGesture` (below) is what handles the tap.
                .accessibilityIdentifier("vocabularyAnswerFace")
            LabeledContent("Перевод", value: card.string("translation"))
            LabeledContent("Форма", value: card.string("form"))
            Text(card.string("example")).font(.callout)
            if state.bool("typed") {
                Text("Твой ответ: \(state.string("draft")). Сравни сам и выбери оценку.")
                    .font(.footnote)
            }
            // D3: no rating buttons on touch — the whole panel is the swipe-to-rate gesture
            // surface, attached at this view's outer container (see [SwipeToRate]'s own call
            // site). The identifier lives on this long-text leaf, not the shared container — see
            // `SwipeToRate`'s own doc comment for why.
            Text("Свайп влево — повторить · вправо — вспомнил")
                .font(.footnote).foregroundStyle(.secondary)
                .accessibilityIdentifier("ratingSwipeArea")
        }
        // Undoes the outer panel's own 180° rotation so the back face's own content reads
        // normally rather than mirrored — the standard SwiftUI 3D-flip counter-rotation.
        .rotation3DEffect(.degrees(180), axis: (x: 0, y: 1, z: 0))
        .contentShape(Rectangle())
        .onTapGesture { toggleFlip() }
    }

    private func toggleFlip() { setFlipped(!flipped) }

    private func setFlipped(_ newValue: Bool, animated: Bool = true) {
        guard flipped != newValue else { return }
        guard animated && !reduceMotion else {
            flipped = newValue
            showBack = newValue
            return
        }
        withAnimation(.linear(duration: flipDuration)) { flipped = newValue }
        // The face swap at exactly 90° of the 180° rotation — half of `flipDuration`.
        DispatchQueue.main.asyncAfter(deadline: .now() + flipDuration / 2) { showBack = newValue }
    }
}
