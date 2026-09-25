# Android host — implementation lane

Date: 2026-09-23. User requested Android development immediately after the local macOS build. This lane advances in parallel with the still-open web parity gate; it does not claim web cutover or Android acceptance.

## Grounded contract

- Existing `kotlin/shared` owns grammar, exercise generation, FSRS, and the versioned `ProgressCodec`; `kotlin/composeApp` owns `TrainingStore` and Compose presentation. Browser storage is `WebProgressRepository`; macOS storage is `DesktopProgressRepository`.
- Add Android targets to the two KMP libraries with `com.android.kotlin.multiplatform.library`, and a separate `kotlin/androidApp` application. Keep `Context`, activity-result contracts, `AtomicFile`, and Android lifecycle in Android source sets/application. No Android framework type enters `shared/commonMain`.
- Android progress remains an app-private v1 JSON document. Validate before replacing, serialize mutations, persist off the main thread, back up before explicit import, and use Storage Access Framework for user-chosen import/export. Browser `localStorage` is never read automatically.
- A single activity owns the Compose composition and a retained session owner; `TrainingStore` owns review transitions and one writer. Configuration change must not rate twice or repeat file effects. Process restart reloads durable progress; draft answer is session state and needs explicit save/restore handling.
- Keep the established training/matrix/progress state and actions, but render Android-specific Material 3 screens. Android shell handles bottom navigation, insets, compact layout, system back, IME, confirmation, file pickers, and lifecycle-aware collection.

## Ordered implementation and evidence

1. Configure pinned AGP/Kotlin/Compose/Gradle/SDK versions; compile Android libraries and debug APK. Verify web and desktop targets after source-set changes.
2. Add Android UTF-8/Unicode normalizer actual and app-private repository with tests for valid, invalid, unsupported, failed write, import backup, and React v1 fixture.
3. Host `TrainingStore` in the Android activity/session owner, connect native JSON import/export/reset effects, and render training, matrix, progress with the shared domain.
4. Run target tests and launch on emulator/device when available; check exercise chain, typed input, schedule, reload, rotation, background/foreground, back, compact/expanded, large text, TalkBack, and motion settings. Record PASS/FAIL/NOT RUN separately. Stage 13/14 remain open until their acceptance criteria are evidenced.

The local SDK initially had API 34 and no configured AVD. API 37 and an API 35 ARM64 image were installed for this development lane; the `Polski_ARM35` emulator now runs on this Mac.

Toolchain selection: Kotlin 2.4.20, Compose 1.12.1, Gradle 9.3.1, AGP 9.1.1, compile SDK 37, target SDK 35, min SDK 24. The first AGP 8.13/API 34 attempt failed AAR metadata because current Compose and lifecycle artifacts require AGP 9.1/API 37. Build results, not this version statement, establish actual compatibility. References: [Android KMP plugin](https://developer.android.com/kotlin/multiplatform/plugin), [AGP 9.1 notes](https://developer.android.com/build/releases/agp-9-1-0-release-notes), [AGP 9 Kotlin migration](https://developer.android.com/build/migrate-to-built-in-kotlin).

## Implementation and verification on 2026-09-23

`shared` and `composeApp` now compile Android library targets, and `androidApp` packages the native activity. The activity owns the lifecycle and Android file pickers; a retained ViewModel owns `TrainingStore`. The Android repository stores an atomic app-private JSON document and backs up the previous bytes on explicit import. Android has its own Material 3 screens; macOS has separate Compose Desktop controls, while both consume the same state/actions. The browser retains its separate DOM host and storage keys.

| Check | Result |
| --- | --- |
| `:androidApp:assembleDebug` | **PASS**: debug APK rebuilt after the native mobile UI and IME inset correction. |
| `:androidApp:testDebugUnitTest` | **PASS**: 6/6 tests, zero failures/skips, covering save/reload, malformed-file protection, React v1 import with backup, unsupported-version refusal, bounded file reads and Android Polish Unicode normalization. |
| `:androidApp:lintDebug` | **PASS**: Android lint report produced with no reported issues. |
| `:shared:desktopTest`, `:composeApp:desktopTest` | **PASS**: shared 144/144 from the preceding Android build and composeApp 22/22 after the desktop-only screen move. |
| `:shared:jsBrowserTest`, `:shared:wasmJsBrowserTest`, `:composeApp:jsBrowserTest`, `:composeApp:wasmJsBrowserTest` | **PASS**: shared 144/144 on each target from the preceding build and composeApp 17/17 on each target after separating mobile screens. |
| `:composeApp:composeCompatibilityBrowserDistribution` and `:composeApp:packageDmg` | **PASS**; `hdiutil verify` confirmed the rebuilt Mac image. Webpack still reports a 2.89 MiB JS bundle, so startup performance requires target-device measurement. |
| API 35 ARM64 emulator | **PASS** for APK installation and launch, a single rating persisted after relaunch, typed draft surviving rotation, training/matrix/progress navigation, SAF JSON export, and React v1 JSON import with a backup of the prior Android document. After backgrounding and `am kill`, a new process reopened the durable v1 document with all 16 cards. With the new Material 3 UI, section selection and progress navigation worked; typed entry with the soft keyboard could scroll to «Проверить ответ», and one rating persisted exactly once. |
| Pixel 4 XL, Android 13, USB | **PASS** for install and visual foreground launch of the final APK after the IME fix. The unlocked phone shows the native Material 3 training card, compact answer-mode selection and bottom navigation without the earlier clipped label. This is a visual launch check, not full physical-device parity or accessibility acceptance. |
| Polish IME, TalkBack, large-font/landscape layout, reduced motion, full exercise-chain and P01–P11 parity on physical device, live CI | **NOT RUN**. Physical launch and emulator smoke checks do not close Stage 13 or 14 acceptance. |
| Independent Tester/Reviewer | **NOT RUN**: the agent runtime returned `agent thread limit reached`; only sequential main-agent verification and source review were possible. |

## Native visual system and multilingual icon

The Android host now uses Material 3 dynamic color on Android 12+ and a defined light/dark palette on older devices. Its training hero, exercise prompt, card surfaces, progress indicator and navigation icons use platform theme colors; the system status/navigation icon appearance follows the selected mode. The launcher uses the language-neutral icon from [branding](../../assets/branding/README.md), with adaptive and monochrome variants. No exercise or scheduling action changed.

| Check | Result |
| --- | --- |
| `:androidApp:assembleDebug :androidApp:testDebugUnitTest :androidApp:lintDebug` after visual/icon changes | **PASS**. |
| API 35 ARM64 emulator, light/dark app launch and visual inspection | **PASS**: [light](artifacts/android/native-training.png) and [dark](artifacts/android/native-training-dark.png) captures; the app-drawer icon and both navigation-bar contrast modes were visually inspected. |
| Updated themed APK on physical Pixel 4 XL | **PASS**: installed with `adb -s 9B051FFBA008DQ install -r`, launched with `am start`, and confirmed as the foreground `MainActivity` with a running process. The dark Material 3 training screen was visually inspected in the [physical-device capture](artifacts/android/pixel-4-xl-final.png). The USB connection briefly dropped during the first transfer; installation succeeded after reconnecting. |
| TalkBack, large text, Polish hardware/soft keyboard and full Stage 13/14 acceptance on the themed APK | **NOT RUN**. |

## Native mobile UI correction

The first physical Pixel 4 XL launch exposed a desktop-shaped Android layout: wide horizontal button rows and a clipped answer-mode label. The user clarified that mobile applications need native platform interfaces. `shared` grammar/FSRS/progress and the `TrainingStore` behavior contract remain shared, while Android now has Jetpack Compose Material 3 screens in `composeApp/src/androidMain`; existing Compose Desktop controls moved to `desktopMain`. The browser remains its DOM host. `MainActivity` owns Android navigation, insets, file contracts and lifecycle; Android screen composables receive immutable `AppUiState` and `AppAction` dispatch only.

Implementation order: (1) mobile shell with top app bar and bottom navigation, (2) phone training flow with wrapping/vertical controls and IME-safe answer entry, (3) matrix and progress as readable cards instead of fixed-width tables, (4) real Pixel verification at normal/enlarged text plus Android/unit and web/desktop regression. Preserve every matrix section, selection and drill callback, both answer modes, four ratings and import/export/reset semantics. A future iOS host will use a separate SwiftUI interface over the same domain/session contract, with a narrow Kotlin/Swift bridge designed at Stage 15; no shared desktop screen is assumed to be the iOS UI.

The first IME smoke check exposed double keyboard insets in the Android shell: the field remained visible but the check button could not be reached. Removing the extra `imePadding` left a single insets owner (`Scaffold`). A second emulator screenshot and accessibility-tree dump confirmed that the typed field and «Проверить ответ» are both reachable by scrolling while the soft keyboard is open. No Polish keyboard or TalkBack claim follows from this emulator check.
