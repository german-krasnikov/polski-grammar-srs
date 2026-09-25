---
name: kmp-desktop
description: "Build or review this project's native SwiftUI macOS host or retained JVM Compose Desktop preview: windows, menus/settings, themes, reminders, keyboard, persistence, accessibility, motion and packaging. Use for Mac apps, not a browser tab."
---

# Kotlin Multiplatform native macOS host and desktop preview

Use [module-architecture](../module-architecture/SKILL.md) for target/source-set boundaries, [compose-multiplatform-ui](../compose-multiplatform-ui/SKILL.md) for state and shared UI behavior, [kotlin](../kotlin/SKILL.md) for semantic portability, and [testing-tdd](../testing-tdd/SKILL.md) for evidence. The project-specific contract is [MacDesktop.md](../../../Plans/Kotlin/MacDesktop.md).

## Host and persistence

- The shipped Mac interface is SwiftUI over the Kotlin framework; Compose Desktop runs on JVM as a retained preview. Keep `java.*` and Swing/AWT dialogs in `desktopMain`, native SwiftUI/AppKit details in the Mac host, and `shared/commonMain` portable. A web DOM renderer cannot be reused as a desktop composable. Preserve common grammar, scheduler, serialization and `TrainingStore` behavior.
- The window owns the session coroutine scope. Start one store per window/session, collect its read-only state, acknowledge effects by ID, and close the store when the window is disposed. Avoid repeating export/reset or launching timers during recomposition.
- Use the app-specific Application Support directory. A Mac process cannot read Safari/Chrome LocalStorage as its own state. Transfer progress through explicit v1 JSON import/export; validate first, back up the existing raw document before replacement, and do not replace unreadable data with an empty save. Treat failed or pending writes as recoverable; keep current in-memory progress exportable.
- File writes should use a temporary file in the destination directory, flush it, then replace the destination atomically when supported. Handle unavailable directories, permissions, truncated files and unsupported versions as user-visible recovery states. File pickers and confirmation dialogs are desktop host effects, not domain code.

## Window, input and motion

- Follow the current [visual decision](../../../Plans/Kotlin/PreGlassRollback.md): the native Mac host restores its pre-glass instructional cards and source/answer/rule emphasis within SwiftUI `NavigationSplitView`, sidebar `List`, toolbar, `GroupBox`, `Form`/Settings controls and system colors/materials. On supported macOS versions these controls adopt the OS visual language automatically. Avoid app-owned glass backgrounds, control styles and intensity sliders; leave the OS appearance setting to macOS. The former [glass rollout](../../../Plans/Kotlin/LiquidGlassRollout.md) is archival. Keep the Compose Desktop preview a separate host with its own acceptance. Check light/dark/system, Increase Contrast, Reduce Transparency and fallback on macOS 14–25. [Apple adoption guidance](https://developer.apple.com/documentation/TechnologyOverviews/adopting-liquid-glass), [Apple HIG materials](https://developer.apple.com/design/human-interface-guidelines/materials)
- Let long Polish/Russian learning text wrap at large font sizes. Keep the main vertical scroll owner clear; scroll wide matrices horizontally with labelled headings and selectable row content. Check small windows and resizing, not only the initial window size.
- Standard Compose buttons/text fields supply keyboard and accessibility semantics. Give custom actions visible focus and meaningful labels. Scope Space/1–4 shortcuts to the training card so text input and navigation controls retain standard behavior; never intercept IME composition or modifier shortcuts. Check real hardware keyboard, Polish diacritics, selection and VoiceOver separately from Compose semantics tests.
- State commits reveal/rating/saving once; animation never triggers them or keeps an old rating actionable. Keep optional motion short and interruptible, and provide a non-moving path when macOS Reduce Motion is enabled. Verify the platform preference before promising it is respected.

## macOS navigation and preferences

- Use a resizable desktop layout with a stable sidebar or toolbar for top-level destinations and a bounded reading column for the current card. Let the reference pane appear beside the card only when width permits; preserve focus, scroll and draft on resize. A phone-sized bottom bar stretched across a Mac window is not a desktop layout. [Apple sidebars](https://developer.apple.com/design/human-interface-guidelines/sidebars)
- Expose Settings through the macOS application menu and `⌘,`, with keyboard-operable groups for learning, appearance, reminders and data. Keep normal text-editing shortcuts, `⌘` navigation and VoiceOver order intact; use the existing native file dialogs for JSON. [Apple keyboard guidance](https://developer.apple.com/design/human-interface-guidelines/keyboards)
- Offer system/light/dark appearance with actual macOS appearance observation. Map semantic tokens to distinct readable palettes; system colors adapt automatically, hard-coded Compose colors do not. Recheck source/target contrast and selection/focus in both appearances and at Increase Contrast. [Apple Dark Mode](https://developer.apple.com/design/human-interface-guidelines/dark-mode)
- Treat notification delivery as a platform integration gate for this JVM host: verify a supported macOS notification bridge, app identity, packaged `.app`, user authorization, delivery and click routing before showing an enabled reminder toggle. A local popup or timer while the app is open does not prove system notifications. When unavailable, show a clear unsupported state and preserve the preference without claiming delivery. [Apple notification authorization](https://developer.apple.com/documentation/usernotifications/asking-permission-to-use-notifications)

## Build and evidence

- Use the pinned Kotlin/Compose versions and the real `desktop` Gradle target. Run JVM/domain and desktop UI tests, launch the app, and build the macOS app image/DMG as separate checks. A built installer is not proof of launch, input, VoiceOver or signing.
- The Compose Desktop plugin packages with `jpackage`. macOS DMG creation requires a macOS host; a local unsigned build can be tested locally but public distribution needs a separate signing/notarization decision. Verify the generated DMG and test the packaged `.app`, not only Gradle `run`.
- Report PASS/FAIL/NOT RUN for tests, compilation, package, live app and accessibility/device checks. Preserve the browser JS/Wasm suite when changing common source sets.

Official sources checked 2026-09-23: [native distributions](https://kotlinlang.org/docs/multiplatform/compose-native-distribution.html), [desktop windows](https://kotlinlang.org/docs/multiplatform/compose-desktop-top-level-windows-management.html), [Compose UI testing](https://kotlinlang.org/docs/multiplatform/compose-test.html), [desktop accessibility](https://kotlinlang.org/docs/multiplatform/compose-desktop-accessibility.html). The app-specific storage and shortcut decisions above are project contracts, not general Kotlin requirements.

Native macOS appearance and standard-control guidance checked against Apple documentation 2026-09-25.
