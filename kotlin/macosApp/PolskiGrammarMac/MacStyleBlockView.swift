import SwiftUI

/// UC-10 S2: renders one `BlockJSON` from `styleBlocks.frontBlocks`/`backBlocks`
/// (`Plans/Kotlin/StylesBlueprint.md` §6) — `MacFlashCardView` just maps a `[BlockJSON]` over this,
/// never branching on `styleId` itself. Each case gets its own clear visual per the task; `formula`
/// and `rule` keep today's single "ЗАПОМНИ"-style accent box, `changes` keeps today's per-change
/// Было→Стало layout (now built from the block's own highlighted spans instead of `exercise.changes`
/// directly, but the same look). Accessible: every block groups as one element with a label a
/// screen reader announces as a heading, and `whyOnDemand`'s disclosure exposes its own
/// expanded/collapsed state.
struct MacStyleBlockView: View {
    let block: BlockJSON
    /// Same gate every other card animation in this file uses (system Reduce Motion OR the app's
    /// own `Motion.Reduced`) — passed down rather than re-read, so a block never disagrees with the
    /// reveal it lives inside.
    let reduceMotion: Bool

    var body: some View {
        switch block.kind {
        case "formula": accentBox(caption: "ЗАПОМНИ", parts: block.parts ?? [])
        case "rule": accentBox(caption: "ПРАВИЛО", parts: block.parts ?? [], detail: block.detail)
        case "table": TableBlockBody(caption: block.caption ?? "", rows: block.rows ?? [])
        case "scene": SceneBlockBody(parts: block.parts ?? [])
        case "nativeParallel": NativeParallelBlockBody(pairs: block.pairs ?? [])
        case "examples": ExamplesBlockBody(items: block.exampleItems ?? [], itemParts: block.itemParts ?? [])
        case "whyOnDemand": WhyOnDemandBlockBody(parts: block.parts ?? [], collapsedLabel: block.collapsedLabel?.isEmpty == false ? block.collapsedLabel! : "Почему так?", reduceMotion: reduceMotion)
        case "changes": ChangesBlockBody(items: block.changeItems ?? [])
        case "contrast": ContrastBlockBody(before: block.before ?? [], after: block.after ?? [])
        default: EmptyView()
        }
    }

    /// [parts] renders through [highlightedText] with `before: false` as its *fallback* role only
    /// — a part with its own `side` (a mixed Before+After list, W3 correction) wins over that
    /// default, same as the web reference's `appendContrastParts(..., "change-after")` calls in
    /// `CardBlocksWeb.kt` for these same block kinds (S4/M1, EmphasisUXAudit E7).
    @ViewBuilder private func accentBox(caption: String, parts: [TrainingSnapshot.HighlightPart], detail: String? = nil) -> some View {
        VStack(alignment: .leading, spacing: 7) {
            Text(caption).font(.caption.weight(.semibold)).tracking(1.1).foregroundStyle(.secondary)
            highlightedText(parts, before: false).font(.headline)
            if let detail, !detail.isEmpty {
                Text(detail).font(.subheadline).foregroundStyle(.secondary)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(Color.accentColor.opacity(0.12), in: RoundedRectangle(cornerRadius: 12))
        .accessibilityElement(children: .combine)
    }
}

private struct TableBlockBody: View {
    let caption: String
    let rows: [BlockJSON.TableRow]
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            if !caption.isEmpty {
                Text(caption).font(.caption.weight(.semibold)).foregroundStyle(.secondary).accessibilityAddTraits(.isHeader)
            }
            ForEach(Array(rows.enumerated()), id: \.offset) { _, row in
                HStack(alignment: .firstTextBaseline, spacing: 10) {
                    if !row.label.isEmpty {
                        Text(row.label).font(.subheadline).foregroundStyle(.secondary).frame(minWidth: 70, alignment: .leading)
                    }
                    highlightedText(row.before, before: true)
                    Text("→").foregroundStyle(.tertiary)
                    highlightedText(row.after, before: false)
                }
                .accessibilityElement(children: .combine)
            }
        }
    }
}

private struct SceneBlockBody: View {
    let parts: [TrainingSnapshot.HighlightPart]
    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            RoundedRectangle(cornerRadius: 1.5).fill(Color.accentColor.opacity(0.5)).frame(width: 3)
            highlightedText(parts, before: false).italic().foregroundStyle(.secondary).fixedSize(horizontal: false, vertical: true)
        }
        .accessibilityElement(children: .combine)
    }
}

private struct NativeParallelBlockBody: View {
    let pairs: [BlockJSON.NativeParallelPair]
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            ForEach(Array(pairs.enumerated()), id: \.offset) { _, pair in
                VStack(alignment: .leading, spacing: 4) {
                    HStack(alignment: .top, spacing: 10) {
                        Text(pair.native).frame(maxWidth: .infinity, alignment: .leading)
                        highlightedText(pair.targetParts, before: false).fontWeight(.medium).frame(maxWidth: .infinity, alignment: .leading)
                        matchBadge(pair.matches)
                    }
                    if !pair.note.isEmpty { Text(pair.note).font(.footnote).foregroundStyle(.secondary) }
                }
                .accessibilityElement(children: .combine)
                .accessibilityLabel("\(pair.native). По-польски: \(pair.target). \(pair.matches ? "Совпадает" : "Отличается"). \(pair.note)")
            }
        }
    }

    private func matchBadge(_ matches: Bool) -> some View {
        Text(matches ? "Совпадает" : "Отличается")
            .font(.caption.weight(.semibold))
            .padding(.horizontal, 8).padding(.vertical, 3)
            .background((matches ? Color.green : Color.orange).opacity(0.18), in: Capsule())
            .foregroundStyle(matches ? .green : .orange)
            .accessibilityHidden(true)
    }
}

private struct ExamplesBlockBody: View {
    let items: [String]
    /// Parallel to [items] by index (S4/M1, EmphasisUXAudit E7); an out-of-range index (should
    /// not happen — `Block.Examples.itemParts` is always the same length as `items`) falls back
    /// to the plain item text rather than crashing.
    let itemParts: [[TrainingSnapshot.HighlightPart]]
    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            ForEach(Array(items.enumerated()), id: \.offset) { index, item in
                HStack(alignment: .top, spacing: 6) {
                    Text("•").foregroundStyle(.secondary)
                    if index < itemParts.count { highlightedText(itemParts[index], before: false) } else { Text(item) }
                }
                .accessibilityElement(children: .combine)
            }
        }
    }
}

private struct WhyOnDemandBlockBody: View {
    let parts: [TrainingSnapshot.HighlightPart]
    let collapsedLabel: String
    let reduceMotion: Bool
    @State private var expanded = false

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Button(action: toggle) {
                HStack(spacing: 6) {
                    Image(systemName: expanded ? "chevron.down" : "chevron.right")
                    Text(collapsedLabel).font(.subheadline.weight(.medium))
                }
            }
            .buttonStyle(.plain)
            .accessibilityLabel(collapsedLabel)
            .accessibilityValue(expanded ? "Развёрнуто" : "Свёрнуто")
            .accessibilityAddTraits(.isButton)
            if expanded {
                highlightedText(parts, before: false).foregroundStyle(.secondary).fixedSize(horizontal: false, vertical: true)
                    .transition(reduceMotion ? .identity : .opacity.combined(with: .move(edge: .top)))
            }
        }
    }

    private func toggle() {
        if reduceMotion { expanded.toggle() } else { withAnimation(.easeInOut(duration: 0.22)) { expanded.toggle() } }
    }
}

private struct ChangesBlockBody: View {
    let items: [BlockJSON.ChangeItem]
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Что изменилось").font(.headline).accessibilityAddTraits(.isHeader)
            ForEach(Array(items.enumerated()), id: \.offset) { _, item in
                VStack(alignment: .leading, spacing: 3) {
                    (Text("Было: ") + highlightedText(item.before, before: true) + Text(" → Стало: ") + highlightedText(item.after, before: false))
                        .textSelection(.enabled)
                    Text(item.reason).foregroundStyle(.secondary)
                }
                .accessibilityElement(children: .combine)
            }
        }
    }
}

private struct ContrastBlockBody: View {
    let before: [TrainingSnapshot.HighlightPart]
    let after: [TrainingSnapshot.HighlightPart]
    var body: some View {
        (Text("Было: ") + highlightedText(before, before: true) + Text(" → Стало: ") + highlightedText(after, before: false))
            .accessibilityElement(children: .combine)
    }
}
