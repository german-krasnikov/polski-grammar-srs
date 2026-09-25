# macOS desktop: approved local implementation blueprint

Date: 2026-09-23. User explicitly requested macOS desktop as the next target while the browser parity gate remains open. This is an additional desktop lane, not evidence that web, Android or iOS gates have closed. Current browser UI is `composeApp/src/webMain` DOM code; only the domain and `TrainingStore` are portable today.

## Target and boundaries

- Add JVM targets to `shared` and `composeApp`. Keep the existing JS/Wasm entry points and tests. Keep the historical `SpikeScreen` `expect` contract in `commonMain` and supply JVM `actual` controls; the JS/Wasm `actual` controls remain unchanged.
- Add `composeApp/src/desktopMain/kotlin/polski/desktop/` as the macOS host. `main()` owns one Compose `Window`, one `TrainingStore`, its coroutine scope, clock/local-day provider and cleanup. `DesktopScreen` renders training, the four grammar matrix sections and progress from `AppUiState`; actions go through `AppAction`. Desktop UI is native Compose components, while browser UI remains DOM.
- `DesktopProgressRepository` stores a single v1 JSON document under the user's Application Support directory (`~/Library/Application Support/Polski Grammar Matrix/progress-v1.json`). Save by writing a temporary file and replacing the target atomically where the file system supports it. Load validates with `ProgressCodec`; invalid or unsupported bytes enter recovery and are never silently replaced. The UI exports the current document and explicitly imports a React/Kotlin v1 JSON chosen through the system file picker. Import validates before replacing a save, creates a raw backup, and recreates the session so the imported data is immediately shown. Browser LocalStorage is never read directly by the desktop app.
- Keep `RawProgressRepository` and its browser keys untouched. The desktop adapter implements `ProgressRepository`; `migrateLegacy` is unused in the desktop host because file import is an explicit separate command. `TrainingStore` remains the sole owner of learning state and ordered writes.
- Use the current pinned Kotlin/Compose versions. Configure Compose Desktop `run`, `createDistributable` and macOS `.dmg` packaging without signing or publishing. The local macOS package metadata uses `1.0.0` because `jpackage` rejects a leading zero; this is not a released product version. Local run is the first acceptance target. Signing/notarization is a separate distribution decision.

## Acceptance

1. `:shared:jvmTest` and `:composeApp:jvmTest` exercise valid/invalid/unsupported save, atomic replacement failure, import backup and store transitions. Existing JS/Wasm tests still compile and pass.
2. Desktop app launches on this Mac, shows a native window with chain training, typed/oral answer, reveal and four ratings; one rating produces one review and survives restart.
3. Matrix displays map, seven cases with selections and comparison, nine verb subjects, pronouns, and drill callbacks; contextual reference does not change the current exercise.
4. Progress shows totals and per-skill rows; export, confirmed reset and explicit JSON import preserve recoverability. Importing a current browser export is verified; malformed import cannot overwrite a valid desktop save.
5. Keyboard, focus, scrolling, large text/window resize and accessible labels are checked in the actual desktop window where automation permits. Tests, compilation, packaging, launch, visual/accessibility checks are reported separately as PASS/FAIL/NOT RUN.
6. Browser production build and affected browser scenarios remain green. No React publication or old progress is deleted.

## Handoffs

Architect: this blueprint. Developer: source and focused tests. Tester: independent macOS run, file corruption/import, screenshots and web regression. Reviewer: final diff and evidence. If independent agent slots are unavailable, the main agent executes roles sequentially and reports that independence limit.

Official references consulted on 2026-09-23: [Compose native distributions](https://kotlinlang.org/docs/multiplatform/compose-native-distribution.html), [desktop windows](https://kotlinlang.org/docs/multiplatform/compose-desktop-top-level-windows-management.html), [recommended KMP structure](https://kotlinlang.org/docs/multiplatform/multiplatform-project-recommended-structure.html).

## Local implementation and verification (2026-09-23)

The JVM target, native Compose screens and file repository are implemented in the working tree. `Main.kt` opens one desktop window and one `TrainingStore` session; the training, matrix and progress controls live in `composeApp/src/desktopMain/kotlin/polski/ui/screens/`, with separate Android mobile screens consuming the same state/actions. `DesktopProgressRepository.kt` uses atomic same-directory replacement, validates v1 JSON, and retains the previous raw file in `backups/` on explicit import. The desktop app never reads browser LocalStorage. A copied `P-fresh` React fixture is exercised through the desktop import API. The Kotlin web target still uses its existing DOM host.

| Check | Result |
| --- | --- |
| `./gradlew :shared:desktopTest :composeApp:desktopTest` in `kotlin/` | **PASS**: shared 144/144 and composeApp 22/22, zero failures/skips (including three Compose UI tests and three desktop storage/import tests). |
| `./gradlew :shared:jsBrowserTest :shared:wasmJsBrowserTest :composeApp:jsBrowserTest :composeApp:wasmJsBrowserTest` in `kotlin/` | **PASS** after adding JVM target: shared 144/144 on each browser target; composeApp 17/17 on each. |
| `./gradlew :composeApp:composeCompatibilityBrowserDistribution` in `kotlin/` | **PASS** after desktop target changes. Existing Playwright 42/42 per branch is historical, not a post-desktop rerun. |
| `./gradlew :composeApp:run` in `kotlin/` | **PASS for process launch**: native window process stayed alive and created `~/Library/Application Support/Polski Grammar Matrix/progress-v1.json`. |
| `./gradlew :composeApp:packageDmg` in `kotlin/` | **PASS** for the local unsigned `.app` and `.dmg` after changing invalid jpackage version `0.1.0` to `1.0.0`. Rebuilt after separating Android and desktop UI; `hdiutil verify` **PASS**. The packaged `.app` was opened in the earlier desktop check and its process remained alive. |
| Native window visual inspection | **PASS**: the packaged `.app` was opened and its window captured directly with `screencapture -l`; [updated training screen](artifacts/stage10/native-desktop-training.png). |
| Real file-picker clicks, hardware keyboard/IME, VoiceOver, window scaling, Intel Mac package, signed/notarized distribution, live CI | **NOT RUN**. A window screenshot does not prove these interactions or accessibility. |
| Independent Tester/Reviewer | **NOT RUN**: the agent runtime rejected new role threads with `agent thread limit reached`; the main agent performed sequential verification and source review only. |

The local `.dmg` is a development artifact. The existing web parity gate is still open; this additional desktop lane does not close it.

## Visual refresh and icon

The Compose Desktop window now has a bounded study surface, a compact summary, clear navigation, a stronger exercise card and a coordinated dark palette. The packaged app uses the language-neutral [macOS icon](../../assets/branding/README.md) with transparent rounded corners. After the refresh, `:composeApp:desktopTest :composeApp:packageDmg` and `hdiutil verify` passed; the packaged `.app` launched and its `Info.plist` points to the bundled `.icns`. This visual change leaves `TrainingStore`, filesystem persistence and keyboard shortcuts intact.
