# Kotlin Multiplatform preview

This directory contains the Kotlin Multiplatform implementation for browser, Mac, Android and iOS. The browser preview renders grammar training, the matrix, progress and the first Polish/Russian vocabulary mode on both Wasm and JS; native hosts expose those flows through their platform UI. The current [pre-glass visual rollback](../Plans/Kotlin/PreGlassRollback.md) restores the earlier instructional hierarchy in semantic browser controls, Android Material 3 and SwiftUI on iPhone, iPad and Mac, while keeping system/light/dark appearance and no app-owned glass optics. All hosts read the shared content pack in `../courses/pl-ru/`. The React app at the repository root remains the product and progress baseline until independent parity and device checks pass. The disposable stage-2 renderer spike (`SpikeScreen`, `SpikeSession` and platform `SpikeControls*`) has been removed now that per-host native UI has landed; it is not part of any shipped screen.

## Toolchain

- JDK 21 to run Gradle and for the Kotlin Java toolchain; Kotlin 2.4.20; Compose Multiplatform 1.12.1; Gradle 9.3.1. Android builds use AGP 9.1.1 and compile SDK 37.
- Run commands from this `kotlin/` directory. `./gradlew` downloads the pinned Gradle distribution and verifies its SHA-256 against `gradle/wrapper/gradle-wrapper.properties`. The checked-in wrapper JAR SHA-256 is `7d3a4ac4de1c32b59bc6a4eb8ecb8e612ccd0cf1ae1e99f66902da64df296172`.
- Gradle plugin and library versions are in `gradle/libs.versions.toml`. The Kotlin/JS dependency tree is locked in `kotlin-js-store/yarn.lock`; a missing or changed lock fails the build. Update it deliberately with `./gradlew kotlinUpgradeYarnLock` after changing dependencies and review the diff.

## Build and preview

```sh
cd kotlin
./gradlew tasks --all
./gradlew :srs-progress:jsBrowserTest :srs-progress:wasmJsBrowserTest :shared:jsBrowserTest :shared:wasmJsBrowserTest
./gradlew :composeApp:compileKotlinJs :composeApp:compileKotlinWasmJs
./gradlew :composeApp:composeCompatibilityBrowserDistribution
node ../node_modules/vite/bin/vite.js preview --host 127.0.0.1 --port 8765 --strictPort --outDir composeApp/build/dist/composeWebCompatibility/productionExecutable
```

Open `http://127.0.0.1:8765/`. The distribution contains a Wasm branch and a JS fallback. The browser tests use headless Chrome through Karma; Chrome must be installed and discoverable. The separate Kotlin CI check uploads its production distribution as an artifact. On pushes to `main`, `.github/workflows/deploy.yml` builds the same compatibility distribution and publishes it to GitHub Pages.

Use a server supporting HTTP byte ranges for exam audio. Vite preview serves MP3 range requests as `206 Partial Content`; the plain Python server used earlier did not provide seekable media ranges and caused incorrect segment playback.

For a clean checkout, install JDK 21 and root Node dependencies (`npm ci`) before the Gradle web build. Use `JAVA_HOME` pointing to JDK 21 if another JDK is selected by default. The Gradle and Yarn caches may be empty; the first run downloads dependencies.

The exam audio manifest is `../dsl/exam-assets/exam-audio.core-data`. Browser resource tasks verify the SHA-256 of six local `../ExamBin/pl/{B1,B2}/{2017,2019,2020}/audio.mp3` files before copying them into ignored web resources. CI restores the owner-approved `exam-audio-v1` release archive through `../.github/actions/exam-audio/action.yml`, verifies the archive and each MP3 against pinned hashes, then builds the distribution. For local builds without that archive or the audio vault, the preparer tries official URLs; those endpoints returned HTTP 403 in the 2026-11-08 local check, so the local build still needs verified audio files. Direct JS, Wasm and compatibility distribution tasks verify their output MP3s as a final gate. The MP3 files are not tracked in Git.

From the repository root, `npm run exam:audio:check-paths` checks that the six exams' DSL task URLs match the manifest. After a production distribution, run `npm run exam:audio:check-dist -- kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable`; with a local server running, `npm run exam:audio:check-url -- http://127.0.0.1:8765/` checks the served response bytes. Both checks compare all six assets to the manifest SHA-256.

The browser exam catalog offers independent parts and a timed four-part session for the six published B1/B2 sittings from 2017, 2019 and 2020. `../dsl/exam-published.core-data` declares stable route IDs, display titles and canonical source directories. The `exam-published` steps in `../dsl/biome.project` check each original `.core-data` file with Biome before aggregating its rows and line provenance into `../dsl/exam-published/generated/`; the typed Kotlin rows in `shared/src/commonMain/generated/kotlin/polski/exam/data/published/ExamRows.kt` supply all hosts. Adding a source directory alone does not publish a sitting. Module times and official maxima come from each paper's source module rows, whether its file is named `modules.core-data` or `exam-modules.core-data`. Publication checks declared audio paths against the pinned `../dsl/exam-assets/exam-audio.core-data` manifest and image paths against browser resources; real audio bytes are checked separately for delivery. The timed session keeps one wall-clock deadline per active part; pausing audio or leaving the tab does not pause it. A part freezes when its time expires or the user finishes it, and the next part starts at that observed time. Timed sessions are held in memory and do not resume after leaving or reloading the page. Results show confirmed points and the separate amount awaiting human review; writing remains a manual assessment, so the app does not show a final percentage or pass verdict while review is pending. The official maximum is the sum of four certified module maxima, independent of the confirmed score range.

Single-answer verdict priority lives in `../dsl/exam/answer-policy.logic`; `../dsl/exam/answer-policy-outcomes.core-data` maps its disposition to public status and confirmed/pending quarter sources. Set-answer priority lives in `../dsl/exam/set-policy.logic` with its typed outcome map in `../dsl/exam/set-policy-outcomes.core-data`. `biome generate --project dsl/biome.project` prints both common Kotlin policies and the independent DSL example fixtures; `biome generate --check --project dsl/biome.project` checks their sources and generated files. Set answers may carry explicit `requiredSelections`, source fact identities, aliases and per-fact quarter credit. Legacy questions without those fields retain their existing key-size and scoring behavior. Unknown nonblank wording in an open-manual fact set retains confirmed credit for known facts and leaves the remaining maximum pending. Malformed or excessive input receives zero confirmed credit and the full maximum pending only when source rows explicitly select `manual_review`; absent metadata preserves the legacy wrong verdict. Unicode normalization, fact lookup and quarter arithmetic remain in shared Kotlin; exam-session transitions are a separate pending DSL decision.

## Progress migration contract (work in progress)

Pure scheduling, progress documents, repository ports, and the POLSKI-TEXT codec live in `:srs-progress` (`srs-progress/src/commonMain/kotlin/polski/{srs,progress,textwire}`). `:shared` exports that module with an `api` dependency so existing host imports and storage adapters retain their package names. Platform repositories and lifecycle ownership remain in their host modules. The pinned progress and scheduler parity tests and generated scheduler rows live in `srs-progress/src/commonTest/`; the cross-document textwire admission tests remain in `:shared` and continue to exercise its compatibility bridge.

The browser adapter is `WebProgressRepository(scheduler)` and `BrowserLocalDayProvider` in `composeApp/src/webMain/kotlin/polski/platform/`. It keeps the React key `polski-grammar-srs-v1` read-only and writes only the separate `polski-grammar-srs-kmp-preview-v1` key. The raw legacy backup is `polski-grammar-srs-kmp-legacy-backup-v1`; successful migration is recorded in `polski-grammar-srs-kmp-migrated-v1`.

Call `load()` before creating or writing progress. `Missing` permits a fresh preview; `LegacyAvailable(raw)` requires an explicit user migration action. `RecoveryRequired`, `Invalid`, `Unsupported`, and `Unavailable` require recovery instead of a fresh write. `migrateLegacy(at, localDay, skillIds)` saves and verifies the exact raw backup, validates the v1 document, completes missing known skills, writes and reads back the preview, then writes the marker. An interrupted migration can resume without changing the React key. A failed `save` leaves the current in-memory document available for `ProgressCodec.encodeLegacyV1(document)` export. The browser UI offers an explicit retry after a partial migration and keeps raw JSON export available during recovery. A current Kotlin export has been loaded in the React baseline in desktop browser tests; device and independent parity gates remain open, so this preview is not a production cutover.

The [20-case progress ledger](../Plans/Kotlin/ProgressParity.md) lists fixture replay evidence and intentional safety differences from React. After changing `tests/fixtures/kotlin-parity/progress.json`, regenerate its Kotlin midnight/import assertions from the repository root with `python3 kotlin/srs-progress/src/commonTest/kotlin/polski/progress/generate_parity.py`, then run the shared JS/Wasm browser tests. The generated source is `srs-progress/src/commonTest/kotlin/polski/progress/ProgressFixtureParityTest.kt`.

The vocabulary mode uses a separate browser key, `polski-vocabulary-pl-ru-v1`. It keeps selected words, custom entries and FSRS cards for `ru-pl` and `pl-ru` independently, without altering the grammar-progress migration keys. The React and Kotlin web previews can read the same vocabulary JSON layout; localStorage still belongs to each browser origin. The first course includes 32 prepared cards and a 1,000-lemma discovery list, so most frequency entries are not yet selectable. Local Mac, Android and iOS hosts now expose the same vocabulary document, two review directions, editor and JSON import/export; mobile simulator and assistive-technology gates are tracked in [CoursePacksPlan.md](../Plans/Kotlin/CoursePacksPlan.md).

## Training session state (work in progress)

`TrainingStore(repository, scheduler, exerciseFactory, time, ownerScope)` in `shared/src/commonMain/kotlin/polski/presentation/` owns one UI-independent session per host. The host calls `start()` once, renders `state: StateFlow<AppUiState>`, sends semantic `AppAction` through `dispatch`, and calls `close()` on unmount. `TimeSource.capture()` supplies one absolute instant and browser-local day per transition. Tabs and the contextual reference preserve the current exercise; chain, focused and schedule actions replace it deliberately. `Rate(exerciseId, rating)` ignores stale/repeated cards, updates in-memory progress once, and queues ordered writes. On save failure, `RequestExport` downloads the current memory snapshot rather than the last saved snapshot.

When `LoadStatus.MigrationAvailable` is shown, the browser host offers `RequestExport` of the raw React progress and an explicit `RequestMigration`; it does not offer rating or reset. `RecoveryRequired` likewise keeps raw export and a migration retry available. `RequestReset` is valid only in `Ready` and produces a `UiEffect.ConfirmReset`; the host returns `ResetDecision(effectId, confirmed)`. `UiEffect.FocusReveal` and `UiEffect.DownloadJson` are one-shot intents that the host acknowledges with `EffectAcknowledged` after execution or a deliberate skip. The browser host owns the 30-second refresh timer, visibility/focus refresh, global keyboard guards, real HTML textarea/buttons/tables, effect execution and session teardown. Real IME, assistive-technology reading order and physical browser checks remain open.

## Verification status

The [stage-2 browser evidence](../Plans/Kotlin/WebCompatibility.md) covers the earlier production artifact in Playwright Chromium, Firefox, and WebKit, including both JS and Wasm branches. This toolchain change is verified separately with the commands reported in the stage-3 handoff. Native installed desktop browsers, physical Android Chrome and iPhone Safari, real IME, and assistive-technology reading order remain unverified. Passing Gradle browser tests or building a distribution does not close those checks.

## macOS native host and Compose Desktop preview

The native Mac application is `macosApp/PolskiGrammarMac.xcodeproj`: a standard SwiftUI `NavigationSplitView`, sidebar, Settings scene and system file dialogs over the shared Kotlin framework. The JVM Compose Desktop host in `composeApp/src/desktopMain/` remains a separate preview; it is not the native Mac interface. Build the native host from the repository root with `xcodebuild -project kotlin/macosApp/PolskiGrammarMac.xcodeproj -scheme PolskiGrammarMac -configuration Debug -destination 'platform=macOS' CODE_SIGNING_ALLOWED=NO build`. To run the retained Compose preview, use these commands from `kotlin/`:

```sh
./gradlew :composeApp:run
./gradlew :srs-progress:desktopTest :shared:desktopTest :composeApp:desktopTest
./gradlew :composeApp:packageDmg
```

The local `.app` and `.dmg` appear under `composeApp/build/compose/binaries/main/`. The package is unsigned and not notarized; it is a local preview, not a distribution release. `jpackage` requires package version `1.0.0` (a leading zero is invalid); this is packaging metadata, not a declaration of production readiness.

The desktop stores `progress-v1.json` in `~/Library/Application Support/Polski Grammar Matrix/`. It never reads Safari/Chrome LocalStorage. Use **Экспорт JSON** in the browser, then **Импорт JSON** in the Mac app to transfer current progress. The app validates the file, backs up the previous desktop document in `backups/`, and only then replaces it. The current in-memory state can also be exported from the desktop app; reset requires confirmation. If the stored document is malformed or uses an unsupported version, the app shows recovery instead of writing a fresh document over it.

The **Слова** tab stores `vocabulary-v1.json` beside that grammar file. Its **Импорт словаря JSON** and **Экспорт словаря JSON** actions use separate system dialogs; importing a valid vocabulary file backs up the previous vocabulary bytes. Grammar progress is unchanged.

The Compose Mac preview has a local **Настройки** screen, opened from the sidebar or the application menu with **⌘,**. It keeps `preferences-v1.json` alongside the two data files. On first launch without that file, it migrates the old `java.util.prefs` explanation method; a valid v1 file takes precedence. An invalid preferences file is retained for recovery and can be exported. The native SwiftUI Settings scene has System/Light/Dark appearance and Reduced Motion choices, and JSON import/export for progress, vocabulary and preferences. Notification delivery is explicitly unavailable. Dynamic system appearance, large-text layout and assistive-technology behavior still need live host checks.

Current desktop checks and limits are recorded in [MacDesktop.md](../Plans/Kotlin/MacDesktop.md). A successful local launch and JVM tests do not establish VoiceOver, all keyboard/IME behavior, visual layout at every window size, signing or notarization.


## Android development preview

The Android application lives in `androidApp/`. It uses the shared grammar, FSRS and `TrainingStore`, with Android-specific Jetpack Compose Material 3 training, matrix and progress screens and bottom navigation. Its v1 JSON is private to the Android app; transfer existing browser progress through **Экспорт JSON** in the browser and **Импорт** on Android. Import validates the file and backs up the previous Android document. It does not access browser LocalStorage. The current debug APK has launched on a Pixel 4 XL (Android 13) and an API 35 emulator; accessibility and full parity checks remain open.

Build with JDK 21 and an Android SDK containing API 37 and Build Tools 36 (Gradle can download missing SDK packages after licenses are accepted). On Apple silicon, use an arm64 JDK 21 — an x86_64 JDK runs under Rosetta and misidentifies the host as `macos_x64`, which breaks native (iOS/macOS) Gradle tasks:

```sh
cd kotlin
export JAVA_HOME=$(/usr/libexec/java_home -v 21 -a arm64)
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew :androidApp:assembleDebug :androidApp:testDebugUnitTest
```

The debug APK is generated under `androidApp/build/outputs/apk/debug/`. Install it with `$ANDROID_HOME/platform-tools/adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk` when a device or emulator is connected. Build and local tests are separate from device checks; the current acceptance status is in [Android.md](../Plans/Kotlin/Android.md).

The **Слова** tab keeps `vocabulary-v1.json` in app-private storage, separate from grammar progress. Its import/export buttons use Android's document picker. After revealing a word, swipe left for **Повторить** or right for **Вспомнил**; the same actions remain available as buttons.

## iPhone and iPad development preview

`iosApp/PolskiGrammar.xcodeproj` is a native SwiftUI application. It links `PolskiShared` from `:apple-bridge`; its Xcode build phase runs the matching iOS device or Apple Silicon simulator Gradle framework task. The shared session supplies immutable screen snapshots and semantic actions. iOS keeps its own app-private v1 progress; use **Экспортировать JSON** and **Импортировать JSON** to transfer a browser or Android save. Import validates before replacement and keeps a backup of the previous native document.

The SwiftUI **Слова** tab keeps an independent vocabulary document with two directions and editable personal words. It uses its own JSON file importer/exporter; after reveal, swipe left/right or use the two rating buttons. The Kotlin session alone calculates the two FSRS schedules.

With Xcode 27 and its license accepted, run the Kotlin Native tests and build the Swift app on an Apple Silicon Mac from the repository root:

```sh
cd kotlin
./gradlew :srs-progress:compileKotlinIosSimulatorArm64 :apple-bridge:iosSimulatorArm64Test
cd ..
xcodebuild -project kotlin/iosApp/PolskiGrammar.xcodeproj -scheme PolskiGrammar -configuration Debug -destination 'generic/platform=iOS Simulator' -derivedDataPath kotlin/iosApp/build CODE_SIGNING_ALLOWED=NO build
```

To run Swift UI tests, select an installed simulator from `xcodebuild -showdestinations -project kotlin/iosApp/PolskiGrammar.xcodeproj -scheme PolskiGrammar` and use its destination with `xcodebuild test`; a generic build destination cannot run tests.

Open `kotlin/iosApp/PolskiGrammar.xcodeproj` in Xcode to run on a simulator or a signed physical device. For a connected device, select the active Apple Development team in Xcode, or build from the repository root with `xcodebuild -project kotlin/iosApp/PolskiGrammar.xcodeproj -scheme PolskiGrammar -configuration Debug -destination 'platform=iOS,id=<device-udid>' -derivedDataPath kotlin/iosApp/build -allowProvisioningUpdates DEVELOPMENT_TEAM=<team-id> build`. Install the resulting `kotlin/iosApp/build/Build/Products/Debug-iphoneos/PolskiGrammar.app` with `xcrun devicectl device install app --device <device-udid> <app-path>`, then launch `dev.polski.grammarmatrix.ios` with `xcrun devicectl device process launch --device <device-udid> dev.polski.grammarmatrix.ios`. The source project can be regenerated with `arch -arm64 ruby kotlin/iosApp/generate_project.rb` if the local `xcodeproj` Ruby gem is installed. Current simulator, device and parity evidence is tracked in [iOS.md](../Plans/Kotlin/iOS.md).
