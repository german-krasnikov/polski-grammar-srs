# Step 5 · независимая приёмка каталога слов V1–V5

Дата: 2026-09-24. Роль: Codex senior-tester (GPT-6 Sol). Контракт: [VocabularyCatalogBlueprint.md](VocabularyCatalogBlueprint.md). Проверен текущий, незафиксированный коммитом рабочий срез; результаты относятся к точным файлам и собранным артефактам ниже. Это тестовая приёмка поведения, не code review и не сертификация CEFR.

| Артефакт | SHA-256 |
|---|---|
| `courses/pl-ru/course.json` | `4daefc9fd70112e7e38013a62510d2882601c10d5630424dadb6c09283579b6b` |
| canonical `JSON.stringify(vocabulary.items)` | `03a7757d1e34eb5901c108eb692721cd6f6f483423ab900a2350623c966888ad` |
| `courses/pl-ru/vocabulary-editorial.json` | `0fc37f1a2288e2cd12e4121dba94b394df1e461517558bd6d0cd1037c223a510` |
| `courses/pl-ru/frequency-top1000.json` | `4549d27b90ec4bb3e1ca9db55fcdd2256b91e28a25f0474cbd55f6a94e2a9a70` |
| React served production JS after collision fix | `9fa9e05ec763dab73161c6403c67e1e2cf17f25d2605c906fa51dd856736ad7f` |
| Kotlin JS served `composeApp.js` after fix | `7a378f7bcb6491d9a1bf644c17998ac1b684a7c9c31626968f0d42267400c58d` |
| Kotlin Wasm served `composeApp.js` after fix | `805bbcc6171e72a6ba5c4e2790016108c35eed772b988a5c923dab36b6c8bd74` |
| Kotlin compatibility production `composeApp.js` after fix | `cb180f5c21a6756fde901f3bd13a2be73cb5402528213e08b2e23e7b078e2ea8` |
| Android installed APK after fix | `795b6bbb12233463de8666f58872c7e75ea9d40da316636692da61eb73b45395` |

Kotlin browser response bytes were compared to `KOTLIN_SPIKE_DIST/composeApp.js` by SHA-256 after the correction. The JS run fetched only the Skiko Wasm binary; the Wasm run additionally fetched the app Wasm binary. Thus the two test labels corresponded to different served distributions, not just different environment names. The aggregate compatibility distribution was built, but no separate browser run used it.

## Acceptance matrix

| ID | Result | Independent evidence and limit |
|---|---|---|
| V1 | **PASS** | `npm run course:validate` and 192 Vitest tests pass. Frequency ranks are exactly 1…1000, with unique lemma; 100/500/1000 are nested prefixes. Source commit, CSV hash, CC BY 4.0, selection rule and change notice are in the journal/attribution. React/Kotlin browser tests observe the source link. This result assumes the pinned upstream comparison recorded in the accepted blueprint; upstream was not fetched again in this runtime pass. |
| V2 | **PASS** | The [line-by-line editorial report](Stage-Vocabulary-Editorial-Tester.md) reviewed all 32 item fields and both prompt directions against WSJP on 2026-09-24: 32 PASS, zero unresolved. The journal has 32 matching `approved` records with reviewer/date; 970 other ranked entries remain discovery (`needs-review` 93, `deferred` 877), with no selectable placeholder. Strict authoring validation rejects pending/unknown records, duplicate selectable lemma, duplicate/reused rank, mismatched provenance and unlicensed external text. Authorship is stated as `project-authored`; this verifies recorded provenance and semantic review, not independent forensic proof of original composition. |
| V3 | **PASS for shipped data and filters** | Rank and local level are independent. The 32 ready items have 25 A1 and 7 A2, B1 is empty, and the interface explicitly says the labels are local and not official CEFR certification. Schema/loaders permit `—`; exact level filtering keeps it out of A1/A2/B1. No shipped item currently has `—`, so its display is supported by code/validation rather than a current-content browser case. |
| V4 | **PASS** | Pinned fixture checks all 32 old IDs and 64 two-direction FSRS keys. Legacy `selectedIds`, review history and exact JSON export survive load; separate schedule mutation and merge are tested. Kotlin Web imports the React sample without losing the two histories; Desktop and Android native storage paths were tested on isolated data. No new ready IDs were minted in this batch, so new-ID policy is validated by authoring rules, not an actual promotion. |
| V5 | **PASS on tested browsers and virtual/native targets** | Initial no-custom browser tests passed, then imported custom `w` exposed a defect: Top 100 reported 93 unavailable but only 92 rows were disabled. After the Developer corrected all four lookup paths, React Chromium regression passed 1/1; Kotlin JS/Wasm regression and served-artifact checks passed 6/6 each across Chromium, Firefox and WebKit. Desktop Compose and Android Emulator with imported selected custom `w` show rank 1 disabled in Top 100 while Mine retains the custom item and history. New iOS simulator bridge snapshot verifies rank 1 unavailable, Mine selected and export history preserved; a fresh DerivedData iPhone XCTest passed exact 7/100 and disabled row 1/1. Physical-device limits below remain open. |

Stage 6 picker round-trip is a separate acceptance step: **NOT RUN** here. Earlier Step 4 full JS/Wasm browser suites (159/159 each, recorded in [Plan.md](Plan.md)) cover unchanged grammar paths; this run used focused vocabulary cases instead of repeating those suites.

## Executed checks

Unless stated otherwise, working directory was repository root. Commands below are representative exact invocations; environment substitutions distinguish browser branches.

| Target | Command / observation | Result |
|---|---|---|
| Pack/editorial | `npm run course:validate` | **PASS**; strict pack and editorial checks. |
| React unit/types/build | `npm test -- --run && npm run typecheck && npm run build` | **PASS** after correction; 35 files/192 tests, TypeScript build, fresh Vite production bundle. |
| React Chromium | Initial no-custom command: `npx playwright test tests/browser/react-baseline.spec.ts -g 'word cards\|frequency catalog' --workers=1` — **PASS** 2/2 before correction. New `-g 'imported custom word cannot'` reproduced **RED** (disabled 92 versus 93) and passed **GREEN** 1/1 on fresh React dist. Firefox/WebKit React baseline: **NOT RUN** in this focused pass. |
| Kotlin shared/web/Desktop | From `kotlin/`: `./gradlew :shared:desktopTest :composeApp:desktopTest :shared:jsBrowserTest :shared:wasmJsBrowserTest :composeApp:jsBrowserDistribution :composeApp:wasmJsBrowserDistribution --console=plain` | **PASS** after correction of a stale test-only Desktop UI-copy expectation. JS/Wasm tasks and distributions reported up-to-date; actual served bytes were then verified separately. |
| Kotlin JS browser | `KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/js/productionExecutable KOTLIN_SPIKE_BRANCH=js KOTLIN_SPIKE_PORT=4174 npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-vocabulary.spec.ts -g 'imported custom word cannot\|serves the selected' --workers=1` | Pre-fix **RED** in Chromium: disabled 92/93. Fresh dist **PASS** 6/6, Chromium/Firefox/WebKit. Earlier full vocabulary file was 12/12 before correction. |
| Kotlin Wasm browser | Same focused command with `wasmJs/productionExecutable`, `KOTLIN_SPIKE_BRANCH=wasm`, port `4175` | Fresh dist **PASS** 6/6, Chromium/Firefox/WebKit. Earlier full vocabulary file was 12/12 before correction. |
| Desktop Compose | `./gradlew :composeApp:desktopTest --tests polski.desktop.DesktopVocabularyAcceptanceTest --console=plain` from `kotlin/` | **PASS** after correction; imported custom `w` remains enabled in Mine while rank 1 is disabled in Top 100 at exact 7/100. Reveal/schedule/storage cases also pass. Standalone packaged Mac app manual UI: **NOT RUN**. |
| Android unit/build | `ANDROID_HOME=/Users/german/Library/Android/sdk ./gradlew :androidApp:testDebugUnitTest :androidApp:assembleDebug --console=plain` from `kotlin/` | **PASS**. Initial call without explicit SDK path failed setup (`SDK location not found`); rerun passed. |
| Android Emulator | Fresh APK installed via `adb install -r` on `emulator-5554` (Android 15/API 35, 1080×2400). Injected an isolated document with selected custom `w` and review history using `run-as`; navigated Слова → Топ 100 → Мои слова. Removed the injected document afterward. | **PASS**. [Top 100 screenshot](artifacts/stage5/android-collision-top100.png)/[XML](artifacts/stage5/android-collision-top100.xml): `7/100`, rank 1 `w` disabled. [Mine screenshot](artifacts/stage5/android-collision-mine.png)/[XML](artifacts/stage5/android-collision-mine.xml): custom `w` enabled and checked. Saved JSON retained its selected ID and exact review card. |
| iOS Kotlin bridge | `./gradlew :shared:iosSimulatorArm64Test --console=plain` from `kotlin/` with `IosVocabularyCollisionSnapshotTest` | **PASS** 203/203 after correction. Isolated `NSUserDefaults` seeded/restored; Top 100 snapshot rank 1 `w` has empty ID and `available=false`, coverage 7/100; Mine has selected, available custom `w`; export retained exact FSRS card. |
| iPhone 17 Pro Simulator | Xcode 27.0, iOS 26.1, UDID `502F57FE-944F-4C27-B74F-5EC34D1B995D`. `xcodebuild -project kotlin/iosApp/PolskiGrammar.xcodeproj -scheme PolskiGrammar -configuration Debug -destination 'platform=iOS Simulator,id=502F57FE-944F-4C27-B74F-5EC34D1B995D' -derivedDataPath kotlin/iosApp/build CODE_SIGNING_ALLOWED=NO -parallel-testing-enabled NO -only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testInventoryAuthoredMatrixAndVocabularyCopyOnSimulator -resultBundlePath test-results/vocabulary/ios-exact-v5.xcresult test -quiet` | **PASS** 1/1, 0 failed/skipped, observed via `xcresulttool` before subsequent Playwright runs cleared `test-results`. XCTest navigated Слова → Топ 100 and asserted exact `7/100` and disabled unavailable selection. Earlier two focused native tests passed 2/2 in retained `kotlin/iosApp/build/Logs/Test/Test-PolskiGrammar-2026.09.24_17-32-27-+0200.xcresult`. Both iPhone runs preceded the collision fix. |
| iPhone 17 Pro Simulator, post-fix | Same `xcodebuild` with fresh `-derivedDataPath kotlin/iosApp/build/CatalogV5Clean` and `-resultBundlePath Plans/Kotlin/artifacts/stage5/ios-exact-v5-clean.xcresult` | **PASS** 1/1, 0 failed/skipped, fresh UI test bundle timestamp 18:08:34. Existing XCTest scrolls to visible Top 100 count and disabled unavailable selector. Local [result bundle](artifacts/stage5/ios-exact-v5-clean.xcresult) and [log](artifacts/stage5/ios-exact-v5-clean.log) are retained; the 608 KB result is not staged for commit. Imported custom collision itself is proven by the iOS bridge snapshot above, not by this empty-custom UI test. |

One added separate iOS XCTest invocation hung during simulator diagnostics/finalization without a valid result bundle. It was stopped; the simulator was rebooted and the exact V5 assertions were added to an existing inventory test. A first serial UDID-pinned run passed before the collision fix. Two post-fix runs using old DerivedData failed to locate the count/selector; xcresult still pointed to old source lines and recorded no new scroll actions after the test-only scroll loop was added. A clean DerivedData rebuild then passed 1/1 with the updated runner. Retained [first failure summary](artifacts/stage5/ios-exact-v5-postfix-results.json), [second failure summary](artifacts/stage5/ios-exact-v5-postfix-scroll-results.json) and [clean pass result](artifacts/stage5/ios-exact-v5-clean.xcresult) distinguish stale test-runner evidence from the final result. The original by-name Xcode destination had also been ambiguous because multiple iPhone 17 Pro simulators were installed; the UDID resolved that setup issue. These harness events are not product defects.

The imported-custom regression independently produced **RED** on pre-fix React and Kotlin JS bundles: Top 100 displayed `Готово 7/100 · недоступно 93`, but only 92 checkboxes were disabled because imported custom `w` occupied deferred rank 1. Repro: import [the isolated document](artifacts/stage5/android-imported-custom-w.json), then open Слова → Топ 100. Exact failed commands were the React and Kotlin JS commands in the table with `-g 'imported custom word cannot'`; Playwright reported `Expected: 93, Received: 92` at React spec line 133 and Kotlin spec line 68. Its default output directory was cleared by later reruns, so the pre-fix screenshots/traces are not retained; the observed CLI failures and regression source are the reproducible evidence. Corrected browser tests also assert Mine still exposes the custom item and its selected ID/review card survive. One immediate React post-fix run failed because my new test used an obsolete placeholder phrase; I corrected the test to the accepted pack copy, after which it passed. The first expanded Desktop test likewise used the wrong Mine content description; the corrected test passed. These were test assertion mistakes, not product regressions.

## Remaining target evidence

| Target/check | Status and reason |
|---|---|
| Physical Android and iOS devices | **NOT RUN**; virtual devices were the available chosen scope. |
| Physical iPad | **NOT RUN**; no physical iPad. |
| Installed Safari browser | **NOT RUN**; Playwright WebKit passed, which is engine automation rather than Safari app evidence. |
| Assistive technology, large text, 320 px, real IME | **NOT RUN** in this focused Step 5 pass. UIAutomation/semantics covered enabled state and visible count only. |
| Long-lived offline/restart on each native host | **NOT RUN** as a full end-to-end matrix; pinned storage fixtures and Android installed-app launch cover narrower paths. |
| iPhone UI with imported custom collision | **NOT RUN**; the colliding document was exercised in the native iOS bridge snapshot, while the fresh iPhone XCTest covered Top 100 with no custom collision. |

The collision defect was corrected and the affected React/Kotlin browser, Desktop, Android and native iOS bridge paths are green. The fresh iPhone UI count/disabled test is green; a full iPhone UI walk with the imported colliding document remains a narrower evidence gap.

## Test-only changes in this acceptance

- `tests/vocabulary-editorial.test.ts`: rejects duplicate selectable lemma and reuse of an approved rank by a candidate.
- `tests/browser/kotlin-vocabulary.spec.ts`: verifies the served JS/Wasm production distribution by response hash and Wasm resource set.
- `tests/browser/react-baseline.spec.ts` and `tests/browser/kotlin-vocabulary.spec.ts`: import selected custom `w` with a saved review card, assert rank 1 stays disabled in Top 100 and Mine/history remain intact.
- `kotlin/composeApp/src/desktopTest/kotlin/polski/desktop/DesktopVocabularyAcceptanceTest.kt`: asserts exact Top 100 count and disabled `w` with an imported colliding custom `w`, then enabled Mine entry.
- `kotlin/composeApp/src/desktopTest/kotlin/polski/desktop/DesktopInventoryContentAcceptanceTest.kt`: updates an old exact-copy assertion to the accepted editorial notice; no product change.
- `kotlin/iosApp/PolskiGrammarUITests/PolskiGrammarUITests.swift`: updates the accepted notice assertion and checks Top 100 count/disabled row in the existing simulator test.
- `kotlin/shared/src/iosTest/kotlin/polski/ios/IosVocabularyCollisionSnapshotTest.kt`: seeds and restores isolated iOS preferences, verifies Top 100/Mine snapshot flags and exact export history for imported custom `w`.

The added tests are regression/acceptance evidence after implementation; they do not establish the Developer's earlier RED → GREEN order.
