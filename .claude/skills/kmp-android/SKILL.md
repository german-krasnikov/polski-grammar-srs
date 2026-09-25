---
name: kmp-android
description: "Implement or review the Android host of a Kotlin Multiplatform/Compose app: lifecycle, adaptive navigation, themes/settings, notifications, accessibility, gestures and verification. Use for Android-specific work, not a browser merely running on Android."
---

# Kotlin Multiplatform on Android

Use [compose-multiplatform-ui](../compose-multiplatform-ui/SKILL.md) for Compose state/layout principles and [module-architecture](../module-architecture/SKILL.md) for module boundaries. This project renders separate Jetpack Compose Material 3 Android screens over the shared domain/session contract; do not assume the Mac or browser screen layout is the phone layout.

## Coding and lifecycle

- Keep the Android application entrypoint thin: initialize platform services and host Android-specific Compose UI. Android `Context`, activity/result APIs and native storage stay behind Android adapters rather than leaking into portable models.
- Pin a compatible Kotlin/Compose/Gradle/AGP/JDK combination. Inspect the current Android KMP library plugin and separate application-module guidance when upgrading; do not copy a legacy `androidTarget` setup without checking compatibility. [Android KMP plugin](https://developer.android.com/kotlin/multiplatform/plugin)
- Give coroutine jobs and collectors an explicit owner. Use lifecycle-aware collection for Android screens; keep durable review updates in the appropriate state/repository owner so a recomposition or configuration change cannot repeat or lose a rating. [Lifecycle and state collection](https://developer.android.com/develop/ui/compose/state)
- Separate rotation retention, saved instance restoration and durable persistence. A ViewModel survives configuration changes, not arbitrary process death. Save small restoration information, such as a selected ID, separately from the progress document. [Saving state](https://developer.android.com/develop/ui/compose/state-saving)
- Serialize repository read/modify/write updates when concurrency can lose data. Use the existing suitable store; a small app does not need a database purely for architectural symmetry. Keep disk work off the UI thread and surface failures without falsely reporting a saved review.
- Use platform contracts for file export/sharing and permissions when required. Do not introduce broad storage permissions for app-private progress or copy browser download code into `androidMain`.

## Adaptive layout and input

- Base layout on current window constraints, including split-screen and foldable posture when relevant. Choose panes/navigation suited to the available space rather than stretching a phone layout. Preserve selected content as panes appear/disappear. [Adaptive apps](https://developer.android.com/develop/ui/compose/layouts/adaptive/get-started-with-adaptive-apps)
- Handle edge-to-edge/system-bar and IME insets at a known boundary. `Scaffold` padding and inset modifiers can double-apply space; consume it deliberately and check gesture navigation and keyboard overlap. [Insets](https://developer.android.com/develop/ui/compose/system/insets-ui)
- Use `sp` for scalable text and logical layout dimensions. Check enlarged font settings, landscape, display scaling and long Russian/Polish text. Avoid fixed-height answer fields and clipped explanations.
- Use Material controls or equivalent semantic controls with adequate touch targets; standard Android guidance uses at least 48 dp for interactive targets. An invisible enlarged target must not overlap neighboring controls. Check TalkBack, keyboard focus, Switch Access where relevant and non-color correctness cues. [Default accessibility behavior](https://developer.android.com/develop/ui/compose/accessibility/api-defaults)
- Integrate back/predictive-back with the actual navigation stack and modal state. Do not replace every system back action with a generic exit handler or lose a typed answer unexpectedly. [Predictive back](https://developer.android.com/develop/ui/compose/system/predictive-back)

## Animation on Android

- Follow the current [visual decision](../../../Plans/Kotlin/PreGlassRollback.md): restore the pre-glass Material 3 instructional cards, color hierarchy and navigation while preserving source/target ending emphasis. Use `Scaffold`, `TopAppBar`, `NavigationBar`/`NavigationRail`, `Card`, `Surface` and button variants with their semantics and touch targets; verify contrast in both schemes. Dynamic color on supported versions should resolve through a light/dark `ColorScheme`; keep a tested fallback palette. The former [Backdrop pilot](../../../Plans/Kotlin/GlassImplementationSelection.md) is archival. [Material 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3), [adaptive navigation](https://developer.android.com/develop/adaptive-apps/guides/adaptive-dos-and-donts), [Compose phases](https://developer.android.com/develop/ui/compose/performance/phases)
- Use the shared state-driven APIs, with Android navigation transitions only where they belong. Do not drive review persistence from a transition-end callback.
- Verify the selected runtime respects the system animator duration scale, including zero; bridge missing behavior for custom animations. Content and actions must remain usable with animations disabled.
- Make gesture-driven transitions interruptible and reversible; integrate with back progress rather than layering a second independent screen animation on top.
- Profile representative release interactions on an emulator and a real device when performance is a release criterion. A debug recomposition count alone does not establish a user-visible performance problem. Use measurements to decide whether baseline profiles or macrobenchmarks are warranted. [Compose performance](https://developer.android.com/develop/ui/compose/performance)

## Android navigation, Settings and reminders

- Drive top-level navigation from the **current window size class**, not a device label: compact windows get a bottom bar, wider windows a rail or pane layout, and short landscape height may need its own adjustment. `NavigationSuiteScaffold` is a candidate only after checking its pinned compatible artifact; preserve the active card/draft while resizing. [Adaptive navigation](https://developer.android.com/develop/adaptive-apps/guides/build-adaptive-navigation), [window size classes](https://developer.android.com/develop/adaptive-apps/guides/use-window-size-classes)
- Keep Settings as a normal destination with Material 3 switches, choices and grouped data actions. Use system dynamic color where available, then a deliberate fallback palette; respect font scale, dark mode, high contrast, gesture navigation and edge-to-edge insets. Preserve predictable system/predictive Back, including from Settings and modal import confirmation.
- Provide system/light/dark choices: observe the system appearance when selected, while an explicit override chooses the corresponding Material 3 dynamic or fallback `ColorScheme`. Android 12+ dynamic colors are a palette input, not permission to lose contrast between source and target marks; test light/dark and wallpaper variants. [Material 3 color](https://developer.android.com/develop/ui/compose/designsystems/material3)
- For review rating, show two visible actions and optionally allow horizontal swipe after reveal. Use a direction cue and threshold, cancel on vertical scroll, and do not steal table drags or TalkBack gestures. Commit the FSRS action once on release; animate the acknowledged state, never the opposite direction's decision.
- If reminders are implemented, default them off; request Android 13+ `POST_NOTIFICATIONS` only after the user enables them, create a user-controllable notification channel, and expose denied/channel-disabled state. Use inexact `WorkManager` scheduling for ordinary study reminders; periodic work and Doze do not guarantee an exact minute. Cancel/reschedule when preferences, due count or timezone changes, and open the due queue on tap. [Permission](https://developer.android.com/develop/ui/compose/notifications/notification-permission), [channels](https://developer.android.com/develop/ui/compose/notifications/channels), [work requests](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work)
- After the final review, `dueCount` may be zero while `nextDue` is tomorrow. Persist/schedule that future reminder while the app is closed; include selected vocabulary in both recall directions and route the tap to the correct queue. An updated FSRS due time cancels the superseded work.

## Verification

- Run shared domain tests on an enabled host plus relevant Android adapter/instrumented tests. A JVM shared test is not an Android UI check. Discover actual Gradle tasks before writing commands into evidence.
- Verify a complete training sequence, typed entry with IME, background/foreground, rotation, process recreation, export/reload, back handling and an interrupted/reduced-motion transition as applicable.
- Check compact and expanded layouts, TalkBack traversal and large text. Record device/API, build variant and executed tasks; report unavailable physical-device checks honestly.
- Keep signed release, Play Store publication, analytics and notification permissions out of an ordinary implementation task unless requested.

Sources checked 2026-09-23. These are Android-specific applications of the shared skill, not requirements to duplicate every screen or domain service.

NavigationBar, insets and overdraw guidance rechecked against Android Developers 2026-09-25.
