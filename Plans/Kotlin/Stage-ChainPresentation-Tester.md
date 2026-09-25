# Party 4 — independent chain presentation acceptance

Scope: [CourseChainPresentationBlueprint.md](CourseChainPresentationBlueprint.md), five authored training labels and host-specific completion copy. Tested against the local Party 4 working tree on 2026-09-24. The developer's source and focused tests were not edited by this tester.

## Independent test additions

- `tests/course-chain-presentation-acceptance.test.ts`: omitted middle step and blank visible label are rejected by the real pack validator.
- `tests/browser/kotlin-parity-chain.spec.ts`: all 12 seeds × 5 ratings compare React with Kotlin JS/Wasm; checks previous expected sentence becomes next source, all ordered visible labels and current/done/aria-current states, host-specific completion text, one persisted review per rating, final 60 reviews after reload.
- `kotlin/composeApp/src/desktopTest/kotlin/polski/desktop/DesktopScreenTest.kt`: native summary, completion title, five sentences, and absence of React/Web-only copy. The completion fixture now uses the actual retained zero-based `chainIndex=4` and requires 5/5 plus a full progress-bar semantic value.
- `kotlin/iosApp/PolskiGrammarUITests/PolskiGrammarUITests.swift`: real five-rating SwiftUI flow, native summary/completion/sentences, final `5 / 5`, and persisted `totalReviews + 5` after relaunch.

## Results

| Gate | Result and evidence |
| --- | --- |
| Validator negatives | **PASS** 2/2; `npm test -- tests/course-chain-presentation-acceptance.test.ts` from repo root. |
| React course focused | **PASS** 4/4; `npm test -- tests/course-chain-presentation.test.ts tests/course-reference-chain.test.ts` from repo root. |
| TypeScript | **PASS**; `npm run typecheck` from repo root after test additions. |
| Production web distributions | **PASS**; `./gradlew :composeApp:jsBrowserProductionWebpack :composeApp:wasmJsBrowserProductionWebpack --console=plain` from `kotlin/`. |
| Browser P02/P05 | **PASS** JS Chromium/Firefox/WebKit 3/3 and Wasm Chromium/Firefox/WebKit 3/3, no retries. `npx playwright test tests/browser/kotlin-parity-chain.spec.ts --config=playwright.kotlin.config.ts --grep 'P02 React and Kotlin show the same 12 five-step chains'` with `KOTLIN_SPIKE_BRANCH=js` or `wasm`, respective `KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/{js,wasmJs}/productionExecutable` and isolated ports 4290–4292. Each run included a separately started React Vite origin. Exact completion body differs by host as specified; 60 single ratings persist on both origins after reload. |
| Desktop Compose UI | **PASS** after correction 1/1; `./gradlew :composeApp:desktopTest --tests 'polski.desktop.DesktopScreenTest.chainHeaderAndCompletionUseNativeCopyWithFiveAnswers' --console=plain` from `kotlin/`. The revised test uses real completion index 4 and checks 5/5 plus progress semantics 1.0. The first modified run failed at test compilation due to an invalid `onAllNodes` import; removing that import made the focused test pass. That setup failure was not a behavioral RED. |
| iOS Simulator UI | **PASS** after correction targeted test 1/1, 54.9 s; `xcodebuild -project kotlin/iosApp/PolskiGrammar.xcodeproj -scheme PolskiGrammar -configuration Debug -destination 'platform=iOS Simulator,id=502F57FE-944F-4C27-B74F-5EC34D1B995D' -derivedDataPath kotlin/iosApp/build -resultBundlePath kotlin/iosApp/build/Test-ChainPresentation-AfterFix-2026-09-24.xcresult -parallel-testing-enabled NO -only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testNativeChainCompletionShowsFiveAnswersAndKeepsFiveRatings test` from repo root. The test checked stage summaries 0–4/5, final 5/5, completion content, and `totalReviews + 5` after relaunch. |
| Android Emulator UI | **PASS** after correction: developer-built debug APK installed with `/Users/german/Library/Android/sdk/platform-tools/adb install -r kotlin/androidApp/build/outputs/apk/debug/androidApp-debug.apk` from repo root; five real `Показать ответ` → `Вспомнил` ratings on `emulator-5554` lead to `Цепочка завершена`, exact five sentences, `5 / 5` and visibly full bar. UIAutomator confirmed the exact completion header. Persisted `files/progress-v1.json` `totalReviews` was 11 before, 16 after five ratings, and 16 after force-stop/relaunch. [After-fix screenshot](artifacts/android/course-chain-presentation-after-fix.png). The initial run **FAIL** was `4 / 5` and ~80% despite all five answers; [initial screenshot](artifacts/android/course-chain-presentation-native.png). |
| Formatting | **PASS** `git diff --check` from repo root. |

Android reproduction of the corrected defect: launch the debug app on `emulator-5554`, enter the chain session, and for each of five successive cards reveal and tap `Вспомнил`. The fifth rating enters completion; inspect the top summary and bar. The initial 4/5 failure was reported to the main agent and developer. The developer corrected only the native display projection, and independent Desktop, iOS and Android reruns passed. Browser parity tests above ran before this native-only correction; the developer ran focused shared JS/Wasm checks afterward. No physical device was used.
