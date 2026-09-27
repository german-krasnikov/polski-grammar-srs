import SwiftUI

/// D4: one tab of the custom bottom bar `PagingTabBar` drives (see `PolskiGrammarApp.body`'s
/// paging `ZStack`, which needs a hook to animate the *content* transition between tabs — plain
/// SwiftUI `TabView` offers none, it swaps tab content instantly with no transition slot).
struct TabBarItem {
    let tag: String
    let title: String
    let icon: String
}

/// Replaces `TabView`'s own tab bar chrome one-for-one (same four labels/icons, same tap
/// contract: `onSelect(tag)`), styled to read as native as practical — icon-over-title buttons,
/// tinted when selected. VoiceOver/XCUITest identify each button by `title` alone (matching what
/// `Label(title, systemImage:)`'s `.tabItem` already produced), never a combined
/// icon+title description, so existing lookups like `app.buttons["Тренировка"]` keep working.
struct PagingTabBar: View {
    let selected: String
    let items: [TabBarItem]
    let onSelect: (String) -> Void

    var body: some View {
        HStack(spacing: 0) {
            ForEach(items, id: \.tag) { item in
                let isSelected = item.tag == selected
                Button { onSelect(item.tag) } label: {
                    VStack(spacing: 3) {
                        Image(systemName: item.icon).font(.system(size: 21))
                        Text(item.title).font(.caption2)
                    }
                    .frame(maxWidth: .infinity, minHeight: 44)
                    .padding(.vertical, 6)
                }
                .foregroundStyle(isSelected ? Color.accentColor : Color.secondary)
                .accessibilityLabel(item.title)
                .accessibilityAddTraits(isSelected ? [.isSelected] : [])
            }
        }
        .padding(.top, 6)
        .background(.bar)
    }
}
