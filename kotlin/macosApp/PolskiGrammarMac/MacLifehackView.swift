import SwiftUI

/// EN-21 (`Plans/Kotlin/EnRuPackPlan.md` §4.2/§4.3): a lifehack is deliberately **not** a
/// [BlockJSON]/`BlockKind` — it shows the same way after every style's own `Back` blocks
/// (`MacFlashCardView`'s `backFace`), never inside `MacStyleBlockView`'s per-kind `switch`. Absent
/// entirely when the active skill has none (no empty frame) — `MacFlashCardView` only mounts this
/// view when `lifehacks` is non-empty. One collapsible entry per [LifehackJSON] (a skill can have
/// more than one), collapsed by default — same disclosure mechanic as `MacStyleBlockView`'s
/// `WhyOnDemandBlockBody`. The toggle's own label is the always-visible source attribution
/// ("Лайфхак · источник: editorial/community"), so it's exactly what VoiceOver announces as the
/// control's name whether collapsed or expanded; the citation (and URL, if any) only appear once
/// expanded.
struct MacLifehackListView: View {
    let lifehacks: [LifehackJSON]
    let reduceMotion: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            ForEach(Array(lifehacks.enumerated()), id: \.offset) { _, hack in
                MacLifehackView(hack: hack, reduceMotion: reduceMotion)
            }
        }
    }
}

private struct MacLifehackView: View {
    let hack: LifehackJSON
    let reduceMotion: Bool
    @State private var expanded = false

    private var caption: String { "Лайфхак · источник: \(hack.status == "community" ? "community" : "editorial")" }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Button(action: toggle) {
                HStack(spacing: 6) {
                    Image(systemName: expanded ? "chevron.down" : "chevron.right")
                    Text(caption).font(.subheadline.weight(.medium))
                }
            }
            .buttonStyle(.plain)
            .accessibilityLabel(caption)
            .accessibilityValue(expanded ? "Развёрнуто" : "Свёрнуто")
            .accessibilityAddTraits(.isButton)
            if expanded {
                VStack(alignment: .leading, spacing: 4) {
                    Text(hack.text).foregroundStyle(.secondary).fixedSize(horizontal: false, vertical: true)
                    if let raw = hack.url, let url = URL(string: raw) {
                        Link("источник: \(hack.citation)", destination: url).font(.footnote)
                    } else {
                        Text(hack.citation).font(.footnote).foregroundStyle(.secondary)
                    }
                }
                .transition(reduceMotion ? .identity : .opacity.combined(with: .move(edge: .top)))
            }
        }
    }

    private func toggle() {
        if reduceMotion { expanded.toggle() } else { withAnimation(.easeInOut(duration: 0.22)) { expanded.toggle() } }
    }
}
