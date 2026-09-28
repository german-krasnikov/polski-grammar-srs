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

/// EnRuPackPlan.md §4.3 (host-side follow-up): the Matrix screen's pack-wide "Лайфхаки"
/// sub-section — every lifehack the active pack has, grouped by skill/topic in curriculum order
/// with real skill titles (`MacSnapshot.kt`'s `lifehackGroups`, already ordered/grouped). Each
/// group is independently collapsible via the native `DisclosureGroup` (system-styled, respects
/// light/dark automatically, VoiceOver announces expanded/collapsed on its own); each lifehack
/// inside a group keeps [MacLifehackListView]'s own per-entry disclosure, so a group's source
/// attribution is visible as soon as the group opens and the text/citation only once expanded.
/// Absent entirely (not an empty section) when the active pack has no lifehacks at all — mirrors
/// every other lifehack render path's "пусто -> ничего не рисуется" rule.
struct MacLifehacksMatrixSection: View {
    let groups: [LifehackGroupJSON]
    let reduceMotion: Bool

    var body: some View {
        if groups.isEmpty {
            Text("Для активного набора лайфхаков пока нет.").foregroundStyle(.secondary)
        } else {
            VStack(alignment: .leading, spacing: 10) {
                ForEach(Array(groups.enumerated()), id: \.offset) { _, group in
                    DisclosureGroup(group.title) {
                        MacLifehackListView(lifehacks: group.lifehacks, reduceMotion: reduceMotion)
                            .padding(.top, 6)
                    }
                    .accessibilityIdentifier("lifehackGroup")
                }
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
