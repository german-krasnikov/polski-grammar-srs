import SwiftUI

/// SB-10: Question-phase tile row + inline picker and revealed-phase result row for the
/// sentence-builder mode. Data comes from `state.record("tileBuilder")` (serialised by
/// `IosSnapshot.tileBuilderSnapshot`). Actions are dispatched as `"tilePlace"`,
/// `"tileCheck"` and `"tileTapError"` commands to `IosSession.dispatch`.

// MARK: - Question phase

/// Tile row + inline picker for the question phase. Auto-opens the first unfilled choice
/// (mirrors web SB6-AC2). Tapping a filled tile toggles its picker; picking a candidate
/// closes it and opens the next unfilled slot.
struct TileBuilderQuestionView: View {
    @ObservedObject var model: AppModel
    let tile: Record   // state.record("tileBuilder")
    let exerciseId: String

    // Which choice position is currently expanded (nil = collapsed).
    @State private var openPicker: Int? = nil

    private var segments: [Record] { tile.rows("segments").sorted { $0.int("position") < $1.int("position") } }
    private var choices: [Record] { segments.filter { $0.string("type") == "choice" } }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            tileRow
            if let choice = choices.first(where: { $0.int("position") == openPicker }) {
                pickerRow(for: choice)
                    .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .onAppear { autoOpenFirst() }
        .onChange(of: exerciseId) { _, _ in
            openPicker = nil
            autoOpenFirst()
        }
    }

    private var tileRow: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 6) {
                ForEach(Array(segments.enumerated()), id: \.offset) { _, seg in
                    if seg.string("type") == "locked" {
                        Text(seg.string("text"))
                            .padding(.horizontal, 10).padding(.vertical, 10)
                            .background(Color(uiColor: .secondarySystemFill), in: RoundedRectangle(cornerRadius: 10))
                            .accessibilityHidden(true)
                    } else {
                        let pos = seg.int("position")
                        let placed = seg["placedForm"] as? String
                        let isOpen = openPicker == pos
                        Button {
                            withAnimation(.easeOut(duration: 0.2)) {
                                openPicker = isOpen ? nil : pos
                            }
                        } label: {
                            Text(placed ?? "___")
                                .padding(.horizontal, 10).padding(.vertical, 10)
                        }
                        .buttonStyle(.bordered)
                        .tint(placed != nil ? .accentColor : nil)
                        .accessibilityLabel(placed != nil
                            ? "\(placed!) — изменить"
                            : "Пустое место, позиция \(pos + 1)")
                        .accessibilityAddTraits(isOpen ? [.isSelected] : [])
                    }
                }
            }
            .padding(.vertical, 2)
        }
    }

    private func pickerRow(for choice: Record) -> some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                let forms = ([choice.string("correctForm")] + choice.strings("alternatives"))
                    .filter { !$0.isEmpty }
                ForEach(forms, id: \.self) { form in
                    Button(form) {
                        model.send("tilePlace", "\(choice.int("position")):\(form)")
                        withAnimation(.easeOut(duration: 0.2)) {
                            openPicker = nextUnfilled(after: choice.int("position"))?.int("position")
                        }
                    }
                    .buttonStyle(.borderedProminent)
                    .accessibilityLabel("\(form) — выбрать")
                }
            }
            .padding(10)
        }
        .background(Color(uiColor: .secondarySystemBackground), in: RoundedRectangle(cornerRadius: 14))
    }

    private func nextUnfilled(after position: Int) -> Record? {
        let candidates = choices.filter { $0.int("position") > position }
            .filter { c in (c["placedForm"] as? String) == nil }
        if let first = candidates.first { return first }
        return choices.filter { c in (c["placedForm"] as? String) == nil }.first
    }

    private func autoOpenFirst() {
        let first = choices.first { ($0["placedForm"] as? String) == nil }
        openPicker = first?.int("position")
    }
}

// MARK: - Revealed phase

/// Tile row (green correct / red error), result banner, and inline error explanation
/// when an error tile is tapped.
struct TileBuilderRevealView: View {
    @ObservedObject var model: AppModel
    let tile: Record        // state.record("tileBuilder")
    let exerciseId: String
    let reduceMotion: Bool

    @State private var openError: Int? = nil

    private var segments: [Record] { tile.rows("segments").sorted { $0.int("position") < $1.int("position") } }
    private var result: Record { tile.record("result") }
    private var errors: [Record] { result.rows("errors") }
    private var isCorrect: Bool { result.bool("correct") }
    private var errorBlocks: [Record] { tile.rows("errorExplanation") }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            tileRow
            resultBanner
            if openError != nil && !errorBlocks.isEmpty {
                // Reuse the same style-block renderer FlashCardView already uses.
                ForEach(Array(errorBlocks.enumerated()), id: \.offset) { _, block in
                    tileErrorBlock(block)
                }
                .transition(reduceMotion ? .identity : .opacity.combined(with: .move(edge: .top)))
            }
        }
        .onChange(of: exerciseId) { _, _ in openError = nil }
    }

    private var tileRow: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 6) {
                ForEach(Array(segments.enumerated()), id: \.offset) { _, seg in
                    if seg.string("type") == "locked" {
                        Text(seg.string("text"))
                            .padding(.horizontal, 10).padding(.vertical, 10)
                            .background(Color(uiColor: .secondarySystemFill), in: RoundedRectangle(cornerRadius: 10))
                            .accessibilityHidden(true)
                    } else {
                        let pos = seg.int("position")
                        let placed = seg["placedForm"] as? String ?? ""
                        let hasError = errors.contains { $0.int("position") == pos }
                        if hasError {
                            Button {
                                withAnimation(reduceMotion ? nil : .easeOut(duration: 0.2)) {
                                    openError = openError == pos ? nil : pos
                                    if openError == pos {
                                        model.send("tileTapError", "\(pos)")
                                    }
                                }
                            } label: {
                                Text(placed)
                                    .padding(.horizontal, 10).padding(.vertical, 10)
                            }
                            .buttonStyle(.bordered)
                            .tint(.red)
                            .accessibilityLabel("\(placed) — неверно, нажми для правила")
                        } else {
                            Text(placed.isEmpty ? "___" : placed)
                                .padding(.horizontal, 10).padding(.vertical, 10)
                                .background(placed.isEmpty
                                    ? Color(uiColor: .secondarySystemFill)
                                    : Color.accentColor.opacity(0.15),
                                    in: RoundedRectangle(cornerRadius: 10))
                                .accessibilityLabel(placed.isEmpty ? "Пусто" : "\(placed) — верно")
                        }
                    }
                }
            }
            .padding(.vertical, 2)
        }
    }

    private var resultBanner: some View {
        Label(
            isCorrect ? "Совпадает с правильным вариантом" : "Сравни свой ответ с эталоном",
            systemImage: isCorrect ? "checkmark.circle" : "info.circle"
        )
        .foregroundStyle(isCorrect ? Color(uiColor: .systemGreen) : Color(uiColor: .systemOrange))
        .accessibilityIdentifier("tileBuilderResult")
    }

    @ViewBuilder
    private func tileErrorBlock(_ block: Record) -> some View {
        // Renders simple text blocks from StyleComposer.blocksForError.
        // Full block kinds (formula, table, etc.) are handled by FlashCardView.styleBlockView;
        // error explanations currently use "rule" only (SB-7 contract), so this subset is correct.
        switch block.string("kind") {
        case "rule":
            VStack(alignment: .leading, spacing: 4) {
                Text(block.string("text"))
                    .accessibilityIdentifier("tileBuilderErrorRule")
                if !block.string("detail").isEmpty {
                    Text(block.string("detail"))
                        .font(.footnote).foregroundStyle(.secondary)
                }
            }
        default:
            Text(block.string("text")).font(.footnote).foregroundStyle(.secondary)
        }
    }
}
