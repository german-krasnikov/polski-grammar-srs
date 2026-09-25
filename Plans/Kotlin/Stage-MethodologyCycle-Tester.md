# Step 4 methodology cycle: independent tester evidence

Date: 2026-09-24. Scope: `MethodologyCycleBlueprint.md` A1–A7. Production fixes were made by the Developer; this report records independent acceptance and test-only changes. All device checks used simulators/emulators.

## Findings and corrections

- **A5, fixed:** `case.gen.neg` introduction described a past-tense negative while the focused exercise generates present `Nie widzę`. `tests/method-cycle-editorial-acceptance.test.ts` was RED (1 failure) before the content correction and GREEN (1 pass) after it. A manual review of 16 introduction/logic pairs over 12 seeds, owner variants, and chain prompts found no further mismatched book/object/future/yesterday claims. This editorial review is separate from the automatic exact-answer leak assertions.
- **A6, fixed:** Kotlin Web did not focus the reveal control after rating and advancing to the next introduction, then pressing Continue. The independent browser assertion failed before the Developer correction and passed on final fresh JS and Wasm distributions.
- **A2, fixed:** React and Kotlin Web allowed `Таблица под рукой` to expose target reference forms during introduction. Independent browser checks failed before the correction. The final UI disables the control and omits the panel until Continue; it also hides a panel opened on the preceding card during the next introduction, then restores it afterward. Desktop and Android already guarded reference rows; iOS snapshot emits no rows during introduction.
- **Minor native UI finding, subsequently fixed:** Android kept the prior reference-open flag across cards. On the next introduction, rows were correctly hidden but the button said `Скрыть таблицу`. This was misleading text, not an answer leak. Original reproduction: open the table on a card, rate it, inspect the next card before Continue; screenshot `artifacts/stage4/android-next-intro.png`. The Developer corrected the native hosts; independent retest is below.

### Native-only retest after the Minor correction

The Developer changed Desktop, Android, and SwiftUI only. The browser source and served distributions were unchanged, so the web suites above were not rerun.

| Gate | Result | Evidence |
|---|---|---|
| Desktop | **PASS** | From `kotlin`, `./gradlew :composeApp:desktopTest --tests 'polski.desktop.DesktopScreenTest.firstEncounterShowsSourceAndIntroductionBeforeSameExercise' --console=plain`: `BUILD SUCCESSFUL` (test task up-to-date). The existing test asserts neutral disabled `Таблица под рукой` during intro, absent `Скрыть таблицу`, then enabled restored `Скрыть таблицу` after Continue. |
| Android fresh APK and emulator | **PASS** | From `kotlin`, `ANDROID_HOME=/Users/german/Library/Android/sdk ./gradlew :androidApp:assembleDebug --console=plain`: `BUILD SUCCESSFUL` in 6 s. APK mtime 2026-09-24 16:52:46, SHA-256 `2cbb813f604e93234fc91ee478d8b95b9e08059eb4e4724c59f8a54331b53e99`. Installed on `emulator-5554`, cleared app data; opened table on card 1, revealed and rated once. Next intro showed `1 / 5`, neutral `Таблица под рукой`, parent semantics `enabled=false`, no reference rows; tapping it did not open rows. After Continue, `Скрыть таблицу` had parent `enabled=true` and rows appeared. [Intro screenshot](artifacts/stage4/android-next-intro-neutral.png), [intro hierarchy](artifacts/stage4/android-next-intro-neutral.xml), [restored screenshot](artifacts/stage4/android-reference-restored.png), [restored hierarchy](artifacts/stage4/android-reference-restored.xml). |
| iPhone 17 Pro Simulator | **PASS** | Clean app on UDID `502F57FE-944F-4C27-B74F-5EC34D1B995D`; targeted `testFirstMethodIntroductionKeepsReferenceAnswerHiddenUntilContinue` passed 1/1 in 33.4 s, `kotlin/iosApp/build/Test-MethodReference-Final2-2026-09-24.xcresult`. XCTest opened reference on card 1, revealed/rated once, asserted next intro has disabled neutral `Таблица под рукой`, no `Скрыть таблицу` or target row, then Continue restores enabled `Скрыть таблицу` and the target row. Earlier attempts failed on offscreen lazy-Form locators; the final test scrolls the queried controls into the hierarchy. |

The iPhone retest command was `xcodebuild -project kotlin/iosApp/PolskiGrammar.xcodeproj -scheme PolskiGrammar -configuration Debug -destination 'platform=iOS Simulator,id=502F57FE-944F-4C27-B74F-5EC34D1B995D' -derivedDataPath kotlin/iosApp/build -resultBundlePath kotlin/iosApp/build/Test-MethodReference-Final2-2026-09-24.xcresult -parallel-testing-enabled NO -only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testFirstMethodIntroductionKeepsReferenceAnswerHiddenUntilContinue test` after `xcrun simctl uninstall` of the app.

## Browser and content acceptance

| Gate | Result | Evidence |
|---|---|---|
| React final build and methodology/legacy browser | **PASS** | `npm run build`; `npx playwright test tests/browser/method-cycle-react.spec.ts tests/browser/react-baseline.spec.ts tests/browser/react-acceptance.spec.ts --config=playwright.config.ts`: 17/17. |
| Kotlin JS/Wasm production distributions | **PASS** | From `kotlin`: `./gradlew :composeApp:jsBrowserDistribution :composeApp:wasmJsBrowserDistribution --console=plain`. Built after the A2 source correction. JS served `composeApp.js`: local mtime 16:03:02, SHA-256 `58361d4e39a200383e00f8b27d90ff4a050bd55c5a0585cb181a62442643a797`; Wasm served `composeApp.js`: 16:03:23, SHA-256 `6b821b462dbc0eaf18388c9e46bb652a7a81c2689c1d4ded58adb94264367b00`. |
| Kotlin methodology and training targeted, three engines | **PASS** | `KOTLIN_SPIKE_BRANCH=js|wasm KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/<branch>/productionExecutable KOTLIN_SPIKE_PORT=4320|4321 npx playwright test tests/browser/kotlin-method-cycle.spec.ts tests/browser/kotlin-training.spec.ts --config=playwright.kotlin.config.ts`: 21/21 per branch across Chromium, Firefox, WebKit. |
| Kotlin JS complete browser suite | **PASS** | `KOTLIN_SPIKE_BRANCH=js KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/js/productionExecutable KOTLIN_SPIKE_PORT=4329 npx playwright test --config=playwright.kotlin.config.ts --reporter=line`: 159/159 in 5.2 min; `/tmp/stage4-kotlin-js-full-final2.log`. |
| Kotlin Wasm complete browser suite | **PASS** | Same command with branch `wasm`, `wasmJs/productionExecutable`, port 4330: 159/159 in 5.0 min; `/tmp/stage4-kotlin-wasm-full-final.log`. |
| Focused legacy parity and P02 chain | **PASS** | Affected legacy subset 29/29 JS Chromium; P02 12×5 chain isolated JS Chromium 1/1, 30.1 s. The preceding 180-second failures were stale test locators waiting for the pre-Continue card, not product hangs. |
| Type checking of browser test changes | **PASS** | `npm run typecheck`. |

The final browser acceptance checks introduction source without an expected answer, answer visibility after Continue/reveal, method switches preserving the typed draft and phase, one review per rating, pre-opened reference hiding/restoration, and narrow 320 CSS px layout. Older browser specs were updated to cross the explicit introduction step before asserting reveal or chain content. No product assertions were removed.

## Native virtual acceptance

| Gate | Result | Evidence |
|---|---|---|
| Desktop first introduction/reference | **PASS** | From `kotlin`: `./gradlew :composeApp:desktopTest --tests 'polski.desktop.DesktopScreenTest.firstEncounterShowsSourceAndIntroductionBeforeSameExercise' --console=plain`. Pre-opened reference title absent during introduction and present after Continue. |
| Shared iOS state/snapshot | **PASS** | From `kotlin`: `./gradlew :shared:iosSimulatorArm64Test --console=plain`. Independent snapshot asserts frozen typed answer `Moja proba` under both methods in Revealed. |
| iPhone 17 Pro Simulator first introduction | **PASS** | Clean app install then targeted `xcodebuild ... -destination 'platform=iOS Simulator,id=502F57FE-944F-4C27-B74F-5EC34D1B995D' -parallel-testing-enabled NO -only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testFirstMethodIntroductionKeepsReferenceAnswerHiddenUntilContinue test`: 1/1, 18.1 s. The table reveals no target before Continue; the target pair and reveal become available afterward. |
| iPhone method switch, typed draft, one review | **PASS** | Clean iPhone 17 Pro Simulator, targeted `testMethodSwitchKeepsTypedDraftThroughRevealAndOneReview`: 1/1 in 45.8 s, `kotlin/iosApp/build/Test-MethodSwitch-Final-2026-09-24.xcresult`. Method changes preserved typed draft before and after reveal; `totalReviews=1` after one rating. The preceding diagnostic [accessibility tree excerpt](artifacts/stage4/ios-revealed-accessibility.txt) exposed `StaticText` label `Moja proba` at frame `(16,567.7,370,52)`; final [screenshot](artifacts/stage4/ios-method-revealed.png) visually confirms the frozen draft and expected answer. An earlier `isHittable` assertion was brittle while the button itself remained tappable; the test now asserts existence then taps. |
| Android Emulator introduction, switch, draft, review | **PASS** | `ANDROID_HOME=/Users/german/Library/Android/sdk ./gradlew :androidApp:assembleDebug --console=plain`; installed on `emulator-5554`, cleared app data, inspected UIAutomator hierarchy/screenshots. Initial source `To jest moja piękna żona.` appeared without target/reference rows; Logic→Situations switch preserved introduction, Continue enabled reference; typed `moja pobra`, switched methods before and after reveal with draft and expected `Widzę moją piękną żonę.` preserved; one `Вспомнил` produced next intro `1 / 5` and Progress `1 всего карточек · 1 сегодня · 15 к повторению`. See `artifacts/stage4/android-method-progress.png` and `android-next-intro.png`. |
| Physical devices and assistive technology | **NOT RUN** | User requested virtual devices; no physical device or screen-reader session was used. |

The iOS clean-state setup used `xcrun simctl uninstall <UDID> dev.polski.grammarmatrix.ios` before targeted XCTest. The browser runs served the distribution files whose mtimes and hashes are recorded above; earlier pre-correction or stale-artifact runs are not counted as final evidence.

The targeted iPhone switch command was `xcodebuild -project kotlin/iosApp/PolskiGrammar.xcodeproj -scheme PolskiGrammar -configuration Debug -destination 'platform=iOS Simulator,id=502F57FE-944F-4C27-B74F-5EC34D1B995D' -derivedDataPath kotlin/iosApp/build -resultBundlePath kotlin/iosApp/build/Test-MethodSwitch-Final-2026-09-24.xcresult -parallel-testing-enabled NO -only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testMethodSwitchKeepsTypedDraftThroughRevealAndOneReview test`.

## Acceptance mapping

- **A1:** Both authored four-stage methods and pack validation were covered by Developer content/schema tests; independent editorial audit and browser cycle acceptance passed. Independent invalid-pack mutation was **NOT RUN** in this stage.
- **A2:** React/Kotlin Web negative and restoration checks, Desktop test, Android hierarchy, and iPhone first-introduction XCTest passed after the reference correction.
- **A3–A4:** React/Kotlin Web checked draft and phase through method switches, progress before/after a single rating, and preference on reload. Android and iPhone independently checked draft/frozen answer and one rating; the full browser parity suites checked existing exercise/progress behavior. This does not substitute for a physical-device persistence test.
- **A5:** Independent semantic audit and `case.gen.neg` regression passed after correction. No scientific claim about learner brain types was found in the reviewed introductions.
- **A6:** Final fresh JS/Wasm full suites passed across Chromium, Firefox, and WebKit; targeted methodology tests also cover focus, 320 CSS px, early-answer hiding, and reference controls.
- **A7:** Desktop, Android Emulator, and iPhone Simulator checks passed as above. Physical devices and assistive technology remain **NOT RUN**.
