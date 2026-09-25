---
name: kmp-ios
description: "Implement or review this project's native SwiftUI iOS host over Kotlin Multiplatform logic: Swift bridge, adaptive navigation, themes/settings, local notifications, VoiceOver, gestures and device verification. Use for native iOS work, not Safari web-target behavior."
---

# Kotlin Multiplatform on iOS

Read [module-architecture](../module-architecture/SKILL.md) for boundaries and [kotlin](../kotlin/SKILL.md) for portable behavior. The accepted mobile direction uses a separate SwiftUI interface on iOS over the shared Kotlin domain/session contract; load [compose-multiplatform-ui](../compose-multiplatform-ui/SKILL.md) only if a particular embedded Compose component is actually in scope.

## Coding and native integration

- Keep the SwiftUI app host small and expose a narrow Kotlin framework API for immutable state and actions. Do not introduce `ComposeUIViewController` as the default screen host. Own platform services in `iosMain` or the Swift host; Foundation/UIKit types should not become shared domain contracts. [iOS framework integration](https://kotlinlang.org/docs/multiplatform-ios-integration-overview.html)
- Select device/simulator targets and a Kotlin/Xcode/deployment-target combination using the current compatibility table. A simulator build does not prove device linking or signing. [Compatibility](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html)
- Treat the Swift/Objective-C boundary as a public API: map expected failures deliberately, define callback/coroutine cancellation and avoid exposing unnecessary implementation types. Inspect actual export behavior before relying on `suspend`, exceptions, generics or generated names in Swift. [Kotlin/Swift interoperability](https://kotlinlang.org/docs/native-objc-interop.html)
- Keep UI work on the appropriate main context; do not block it with file operations or domain-heavy work. Own native observers, delegates and callbacks and remove them on disposal. Check cross-language retention if a closure retains a controller/state owner.
- Separate UI restoration from durable progress. Save important changes when made; background/termination callbacks are not a reliable final save point. Keep exported JSON compatible with the shared schema and use native file/share facilities for transfer.
- Inspect how SwiftUI view/scene and application events map to the shared session lifecycle. Avoid duplicating collectors when a view reappears or losing a review because the app became inactive. [SwiftUI host with shared logic](https://kotlinlang.org/docs/multiplatform-mobile-upgrade-app.html)

## Layout, typography and interaction

- Respect safe areas and the software keyboard without applying the same inset twice. Check small iPhones, landscape and iPad window resizing with a typed answer active. [Layout](https://developer.apple.com/design/human-interface-guidelines/layout)
- Preserve iOS-appropriate navigation gestures, text editing, selection, scrolling and focus through SwiftUI controls. If UIKit interop is actually needed, define one owner for touch and scrolling.
- Support Dynamic Type and larger accessibility sizes through SwiftUI layout that stacks controls when needed and retains complete learning text. [Typography](https://developer.apple.com/design/human-interface-guidelines/typography)
- Prefer legible system typography or licensed, properly packaged fonts with Polish/Russian glyph coverage. Native system font availability does not authorize copying font binaries into all distributions.
- Use comfortable separated touch targets; adopt 44 pt as a default design target for principal iOS actions unless an existing component/requirement calls for another validated layout. Verify actual tap areas on devices.

## Accessibility and motion

- Follow the current [visual decision](../../../Plans/Kotlin/PreGlassRollback.md): keep the pre-glass accent `StudyHero`, instructional source/answer/rule emphasis and standard SwiftUI `TabView`, `NavigationStack`/split navigation, `List`, `Form`, `Button`, `Picker` and system materials/colors. These controls adopt the OS appearance, including Liquid Glass where supported, without app-owned optical wrappers. Avoid custom glass backgrounds, `.glassEffect` on content and controls, or an in-app glass intensity slider. The earlier [glass rollout](../../../Plans/Kotlin/LiquidGlassRollout.md) is archival. Verify iOS 17–25 fallback, system/light/dark appearance, Increase Contrast, Reduce Transparency and Reduce Motion. [Apple adoption guidance](https://developer.apple.com/documentation/TechnologyOverviews/adopting-liquid-glass), [HIG materials](https://developer.apple.com/design/human-interface-guidelines/materials)
- Give SwiftUI controls meaningful labels, values, roles and state. Validate VoiceOver reading order, focus after navigation/reveal, keyboard access and that unrevealed answers are absent. XCTest identifiers are not spoken labels. [Apple accessibility](https://developer.apple.com/design/human-interface-guidelines/accessibility)
- Test larger text, light/dark appearance and Increase Contrast. Use indicators other than color for mistakes and correctness.
- Honor SwiftUI's Reduce Motion environment preference, including changes while the app is active. Prefer subtle or instantaneous alternatives to large spatial motion; keep a fully usable no-motion path. [Reduce Motion environment](https://developer.apple.com/documentation/SwiftUI/EnvironmentValues/accessibilityReduceMotion)
- Use a shared motion policy with an iOS preference adapter where necessary. Card progress/persistence must not depend on animation completion. Test rapid reversal, swipe-back interruption, backgrounding and repeated taps.
- Measure release frame pacing on a representative device; do not assume a fixed 60 Hz refresh rate or treat simulator smoothness as device evidence. Use Instruments when investigation is justified.

## iPhone/iPad navigation, Settings and reminders

- Keep a small, stable set of top-level destinations in a native `TabView` on iPhone. On iPad, use an adaptive sidebar/split presentation when available width and multitasking justify it; preserve selected card, draft and scroll across size changes. Put secondary configuration in a Settings screen, not as a tab action. [Apple tab bars](https://developer.apple.com/design/human-interface-guidelines/tab-bars), [sidebars](https://developer.apple.com/design/human-interface-guidelines/sidebars)
- Use SwiftUI `Form`, `Picker`, `Toggle` and native file importer/exporter for Settings/data. Show system/light/dark choice only when implemented, and follow Dynamic Type, VoiceOver, Increase Contrast and Reduce Motion for both current and changed preferences. Keep an accessible visible alternative to swipe grading. The swipe must not conflict with navigation-back, vertical scroll, text selection or VoiceOver gestures; commit one review only after a completed post-reveal gesture.
- Use semantic SwiftUI/system colors or explicit light/dark/high-contrast variants for the source and target cues. “System” follows appearance changes; manual light/dark overrides do not alter the card, draft or FSRS. Verify contrast in sheets and elevated surfaces too. [Apple Dark Mode](https://developer.apple.com/design/human-interface-guidelines/dark-mode)
- When the typed keyboard is up, place the essential reveal action in a real keyboard toolbar or a layout that is measured clear of the keyboard; test iPhone and iPad with method picker and long text. A safe-area inset can still cover a scrolled picker. [Keyboard toolbar](https://developer.apple.com/documentation/swiftui/toolbaritemplacement/keyboard)
- For local reminders, use `UNUserNotificationCenter` only after the user opts in, read authorization state, schedule/cancel on setting and due-state changes, and route a tap to the due queue. Respect quiet hours and avoid revealing learning text on the lock screen by default. Denied permission is a Settings state, not repeated prompts. [Permission](https://developer.apple.com/documentation/usernotifications/asking-permission-to-use-notifications), [local scheduling](https://developer.apple.com/documentation/usernotifications/scheduling-a-notification-locally-from-your-app)
- A zero current due count does not mean there is no future review. Include grammar and selected vocabulary/direction next-due times in local scheduling, keep the request through app termination, and replace it when FSRS or the selection changes. Test delivery and tap routing after the app has been closed.

## Verification

- Run applicable common tests on Kotlin/Native and exercise native adapters; verify the actual configured target tasks. Check framework integration and a simulator launch separately.
- Test training, typed Polish input, keyboard avoidance, safe-area layout, file export/reload, background/foreground and accessibility with an explicit OS/device matrix. XCTest and Accessibility Inspector complement Compose tests.
- Use XCTest and Accessibility Inspector for native SwiftUI acceptance; verify Kotlin framework integration separately from the visible application.
- Report simulator, physical-device, accessibility and release-build evidence independently. Signed distribution/TestFlight/App Store work is separate scope; do not call an unlaunched framework an iOS application.

Sources checked 2026-09-23. Kotlin's [recommended project structure](https://kotlinlang.org/docs/multiplatform/multiplatform-project-recommended-structure.html) explicitly supports a native Swift UI with shared Kotlin logic. iOS target and bridge details still require Stage 15 build evidence.

Native SwiftUI appearance and standard-control guidance checked against Apple documentation 2026-09-25.
