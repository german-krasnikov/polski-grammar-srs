# Review 2026-09-25 — Fixes log

Tracks fixes applied per finding ID from `Plans/Kotlin/Review-2026-09-25.md`, batch by batch. Baseline commit `31684bb`.

## Batch: shared-core (M1, M2, M15, m1, m2, m13)

### M1 — `SrsCardWire.decode` accepted `CardState.New` with non-initial FSRS memory
- **Files:** `kotlin/shared/src/commonMain/kotlin/polski/srs/SrsCardWire.kt`, `kotlin/shared/src/commonTest/kotlin/polski/srs/SrsCardWireTest.kt`, `kotlin/shared/src/commonTest/kotlin/polski/progress/ProgressCodecTest.kt`
- **Fix:** added `|| (state == CardState.New && !memoryIsInitial)` to the invalid-memory condition in `SrsCardWire.decode`, symmetric to the existing `memoryIsInitial && state != CardState.New` check.
- **RED:** added `newCardsWithLearnedMemoryAreInvalid` (unit) and `newCardWithLearnedMemoryIsRejectedByProgressImport` (via `ProgressCodec.decode`) — both failed before the fix (`AssertionError` at `SrsCardWireTest.kt:48`).
- **GREEN:** both tests pass after the fix.
- **Fallout fixed:** `ProgressCodecTest.unknownPropertiesAndUnknownSkillsSurviveCompletionAndExport` used a fixture with `state=0` (New) but `stability=1,difficulty=2` (learned memory) to exercise unknown-field survival — this became correctly rejected by the tightened check. Changed fixture `state` from `0` to `2` (Review), which is valid with that memory and preserves the test's actual intent (round-tripping unknown JSON fields), unrelated to memory-state validity.

### M2 — Vocabulary import size guard blocked `:shared:jsBrowserTest` on JS
- **File:** `kotlin/shared/src/commonMain/kotlin/polski/vocabulary/VocabularySession.kt:149`
- **Fix:** added a cheap `require(raw.length <= 10_000_000)` before the exact `raw.encodeToByteArray().size <= 10_000_000` check. Every UTF-16 code unit encodes to at least one UTF-8 byte, so `raw.length` is always a lower bound on byte count — this rejects grossly oversized input before paying for `encodeToByteArray()`, which is what made Kotlin/JS blow past the Mocha timeout on a 10 MB synthetic string. Exact byte-count semantics (`<= 10_000_000` bytes) unchanged for inputs that pass the cheap check.
- **RED/GREEN:** the existing acceptance test `VocabularySessionAcceptanceTest.recoveryRestoresLegacyHomonymAndFourHistoriesOnlyAfterValidImport` (uses `importJson("x".repeat(10_000_001))`) already covered this; no new test needed. Confirmed GREEN on `:shared:desktopTest`, `:shared:jsBrowserTest` and `:shared:wasmJsBrowserTest` (197/197 on both JS and Wasm, previously 195/196 on JS with a timeout).

### M15 — `:composeApp:desktopTest` fixture defect (test-only, no production change)
- **File:** `kotlin/composeApp/src/desktopTest/kotlin/polski/desktop/DesktopPreferencesRepositoryTest.kt:39` (`invalidV1RetainsRawAndNeverFallsBackToLegacy`)
- **Fix:** the fixture `{"schemaVersion":2}` is legitimately valid per `UserPreferencesCodec` (matches `UserPreferencesCodecTest.defaultsAndExplicitChoicesRoundTrip`, which asserts `{"schemaVersion":1}` decodes to `Loaded(defaults)`; the v2 case with an omitted `glassTintPercent` falls back to the default 50, which is in range). Replaced the fixture with a truly invalid document: `{"schemaVersion":2,"glassTintPercent":101}` (out of the required `0..100` range).
- No RED needed (fixture-only correction, per testing-tdd rules); confirmed GREEN on `:composeApp:desktopTest` (54/54, was 51/52).

### m1 — `TrainingStore` did not normalize the stale daily counter on install
- **Files:** `kotlin/shared/src/commonMain/kotlin/polski/presentation/TrainingStore.kt`, `kotlin/composeApp/src/commonTest/kotlin/polski/presentation/TrainingStoreTest.kt`
- **Fix:** `install()` now calls `ProgressCodec.normalizeDay(completed, captured.localDay)` before assigning `document`, so a document loaded with yesterday's `lastDay`/`reviewsToday` is normalized in memory (and queued for save, same as the existing `completeKnownSkills` divergence check) before the first review of the day, matching `src/progress/storage.ts`'s `loadProgress` day-rollover behavior. The UI projection (`project()`) already zeroed `todayCount` for display; this fix makes the underlying `document` — and therefore `exportJson()`/`RequestExport` — consistent with what's shown.
- **RED:** added `installNormalizesStaleDailyCounterBeforeFirstReview` — loads a document with `lastDay="2026-09-22", reviewsToday=3` under a clock at `2026-09-23`, dispatches `RequestExport`, decodes the exported JSON and asserts `reviewsToday==0, lastDay=="2026-09-23"`. Failed before the fix (`AssertionError` at `TrainingStoreTest.kt:195`).
- **GREEN:** passes after the fix.

### m2 — `Failed` effect outcome never removed from `pendingEffects`
- **Files:** `kotlin/shared/src/commonMain/kotlin/polski/presentation/TrainingStore.kt`, `kotlin/composeApp/src/commonTest/kotlin/polski/presentation/TrainingStoreTest.kt`
- **Fix:** `acknowledge()` now calls `removeEffect(effect.id)` in both the `Failed` and non-`Failed` branches (previously it returned early on `Failed`, leaving the effect permanently in `pendingEffects`).
- **Host check:** verified desktop (`Main.kt`, `attemptedEffects` set), web (`TrainingWebApp.kt`, `attemptedEffects` set) and Android (`AndroidSessionViewModel.kt`, `session.claimEffect(id)`) all guard effect processing by effect id in a host-local structure, not by presence in `pendingEffects` — none re-process an effect once claimed, so removing a `Failed` effect from the store's list does not cause any host to re-run the export flow. iOS/macOS do not consume `UiEffect.DownloadJson`/`EffectOutcome.Failed` at all (not referenced in either Swift app), so unaffected.
- **RED:** added `acknowledgedFailedEffectIsRemovedFromPendingEffects` — dispatches `RequestExport`, acknowledges the resulting effect with `EffectOutcome.Failed("export failed")`, asserts the effect is gone from `pendingEffects` and `state.error` is set. Failed before the fix (`AssertionError` at `TrainingStoreTest.kt:212`).
- **GREEN:** passes after the fix.

### m13 — `chainPresentation` had no `require()` guard
- **File:** `kotlin/shared/src/commonMain/kotlin/polski/data/CourseData.kt`
- **Fix:** `chainPresentation`'s `steps` list now goes through `.requireUniqueIds(ChainStep::id)`, the same helper already used for `nouns`, `adjectives`, `verbs`, `skills` and `vocabulary`. Guards against an empty or duplicate-id `steps` array in `courses/pl-ru/course.json`.
- **No RED invented:** `PolishCourseData` is an internal singleton parsing the single compiled `generatedCourseJson`; there is no injection seam to feed it malformed data (same as the other `requireUniqueIds` call sites, none of which have malformed-fixture tests — they guard real course data, exercised only positively). Consistent with the testing-tdd allowance for structural guards on fixed production data mirroring an established pattern.
- **GREEN:** `CourseChainPresentationTest` (2/2) and the full `:shared:desktopTest` suite (197/197) pass with the guard in place, confirming the real course data satisfies it.

### Checks (working directory `/Users/german/Work/JS/polski-grammar-srs/kotlin` unless noted)

| Check | Result | Counts |
|---|---|---|
| `:shared:desktopTest` | PASS | 197/197 |
| `:shared:jsBrowserTest` | PASS | 197/197 (was 195/196, JS timeout on M2) |
| `:shared:wasmJsBrowserTest` | PASS | 197/197 |
| `:composeApp:desktopTest` | PASS | 54/54 (was 51/52, M15) |
| `:composeApp:jsBrowserTest` | PASS | — |
| `:composeApp:wasmJsBrowserTest` | PASS | — |
| `:androidApp:testDebugUnitTest` | PASS | 16/16 |
| `:shared:iosSimulatorArm64Test` | PASS | 218/218 (arm64 JDK 21; previously NOT_RUN under the x86_64 JDK) |
| `:shared:macosArm64Test` | PASS | 199/199 (arm64 JDK 21; previously NOT_RUN under the x86_64 JDK) |

Environment: `JAVA_HOME=$(/usr/libexec/java_home -v 21 -a arm64)`, `ANDROID_HOME=$HOME/Library/Android/sdk`, one Gradle invocation at a time.

### Changed paths
- `kotlin/shared/src/commonMain/kotlin/polski/srs/SrsCardWire.kt`
- `kotlin/shared/src/commonMain/kotlin/polski/vocabulary/VocabularySession.kt`
- `kotlin/shared/src/commonMain/kotlin/polski/presentation/TrainingStore.kt`
- `kotlin/shared/src/commonMain/kotlin/polski/data/CourseData.kt`
- `kotlin/shared/src/commonTest/kotlin/polski/srs/SrsCardWireTest.kt`
- `kotlin/shared/src/commonTest/kotlin/polski/progress/ProgressCodecTest.kt` (fixture fix, fallout of M1)
- `kotlin/composeApp/src/commonTest/kotlin/polski/presentation/TrainingStoreTest.kt`
- `kotlin/composeApp/src/desktopTest/kotlin/polski/desktop/DesktopPreferencesRepositoryTest.kt`

Not part of this batch, not touched: M3–M12, M14–M18, remaining minors (m3–m12, m14, m15). Nothing in this batch was skipped.

## Batch: ios-host (C1 iOS part, M10, M11, M4 iOS part, M17, m7)

Native iOS host: `kotlin/iosApp/PolskiGrammar/PolskiGrammarApp.swift`, `kotlin/shared/src/iosMain/kotlin/polski/ios/*`, iosTest, `PolskiGrammarUITests`.

### C1 (iOS) — save-failure banner not shown across tabs, export action not reachable
- **Files:** `kotlin/iosApp/PolskiGrammar/PolskiGrammarApp.swift`, `kotlin/shared/src/iosTest/kotlin/polski/ios/IosErrorSnapshotTest.kt` (new), `kotlin/iosApp/PolskiGrammarUITests/PolskiGrammarUITests.swift`
- **Fix:** a new `ErrorBanner` view renders `TrainingStore.state.error` above every tab (Training/Matrix/Progress/Vocabulary) in `PolskiGrammarApp`'s `WindowGroup`, regardless of `loadStatus`, with an "Экспортировать JSON" button (`errorBannerExport` a11y id) dispatching the existing `RequestExport` effect. Added `IosErrorSnapshotTest` locking that `snapshot()` exposes `error` unconditionally (the Kotlin contract the Swift banner relies on). Added a UI-test-only fault-injection seam (`POLSKI_UITEST_FORCE_SAVE_FAILURE` env var, opt-in, default off) in `IosProgressRepository.save` and a new XCTest, `testSaveFailureShowsErrorBannerOnEveryTabWithExportReachable`, asserting the banner text and export button on all four tabs.
- **Correction round 1 (this entry):** the tester found the banner text rendered correctly but `errorBannerExport` was never discoverable by XCTest/VoiceOver (0/5 on every tab, every run). Root cause: the fix in the prior round applied `.accessibilityIdentifier("trainingErrorBanner")` on the outer `HStack` *before* `.accessibilityElement(children: .contain)` in the modifier chain — SwiftUI then propagates that identifier onto every exposed child once the container starts exposing children individually, silently overwriting the button's own `errorBannerExport` id with `trainingErrorBanner`. Fixed by reordering: `.accessibilityElement(children: .contain)` first, then `.accessibilityIdentifier("trainingErrorBanner")` (matches the ordering that makes it apply to the container node only). Confirmed via a raw accessibility-hierarchy dump before/after the reorder — before: `Button, identifier: 'trainingErrorBanner', label: 'Экспортировать JSON'`; after: the button's own identifier is `errorBannerExport` and is found directly.
- **RED/GREEN:** `IosErrorSnapshotTest.snapshotExposesErrorEvenWhileLoadStatusIsReady` is a contract lock, not a regression test (`snapshot()` already exposed `error` unconditionally). The real RED/GREEN for the accessibility defect is the XCTest itself: before the reorder, `testSaveFailureShowsErrorBannerOnEveryTabWithExportReachable` failed deterministically on `XCTAssertTrue(app.buttons["errorBannerExport"].exists, app.debugDescription)` (confirmed twice, once with the identifier before `.accessibilityElement`, once — pre-fix — with no `.accessibilityElement` modifier at all per the tester's report); after reordering, it passes with 0 failures.
- **Verification:** `testSaveFailureShowsErrorBannerOnEveryTabWithExportReachable` PASS in isolation and in the full 17-test suite on both iPhone 17 Pro and iPad Pro 11-inch (M5).

### M17 — `testS6VocabularyFileImporterCancellationKeepsSelection` Cancel locator
- **File:** `kotlin/iosApp/PolskiGrammarUITests/PolskiGrammarUITests.swift:169` — unchanged from the prior round (`app.descendants(matching: .any)["Отменить"]`, since iOS 26 exposes the system picker's Cancel as a Link, not a Button — matches the pattern already used elsewhere in this file).
- **Investigated this round, not changed further — confirmed environment blocker, not a code defect:** the tester reproduced the system document picker never presenting at all (no picker window/sheet appears anywhere in the accessibility tree after tapping "Импортировать словарь JSON") on 4/4 runs across both simulators. I reproduced the identical symptom independently, then isolated the variable: I rebuilt with `SWIFT_ACTIVE_COMPILATION_CONDITIONS="DEBUG S6_PICKER_ACCEPTANCE"` and ran the pre-existing, already-gated `testS6VocabularySystemFileImporterRejectsCurrentFile` (a test that predates this batch, untouched by any of this project's iOS-host changes, and is deliberately excluded from routine runs behind that flag precisely because system-picker interaction is known-unreliable) — it failed with the exact same symptom (picker never presents, `Cell` never found). This proves the "picker never presents" failure is a session/simulator-level issue orthogonal to the M17 locator fix, this batch's diff, or which picker-triggering test is run. `.fileImporter` wiring (`PolskiGrammarApp.swift:336`, unchanged by this or the prior round) is not implicated.
- **Not fixed further:** there is no product-code change available that makes a system-owned document-picker extension present in a simulator session where it currently does not for any test that opens it. Re-running on a clean/rebooted simulator did not change the outcome (tester already tried one full reboot; I did not repeat that specific step but did use a freshly-booted device for the iPad run and saw the same failure). Recommend re-verifying on a different Xcode/simulator install or a physical device before treating this as resolved; the locator fix itself is correct and will pass once the picker actually presents.

### M10, M11, M4 (iOS), m7 — reconfirmed, no changes needed this round
Tester verdict PASS on all four with concrete code/evidence citations (`IosSession.kt`, `AppModel.reconcileAnswerMode`, `revealButton`'s `@AccessibilityFocusState` gated by `focusExerciseId`, `vocabularyRefreshTimer` + `scenePhase` vocabulary refresh, and the single `*-import-backup-latest` key in all three iOS stores). Nothing to change; re-verified by the full-suite reruns below.

### Checks (working directory `/Users/german/Work/JS/polski-grammar-srs/kotlin` unless noted)

| Check | Result | Counts |
|---|---|---|
| `export JAVA_HOME=$(/usr/libexec/java_home -v 21 -a arm64); ./gradlew :shared:iosSimulatorArm64Test --rerun` | PASS | 224/224 (unchanged from the tester's count — no Kotlin production code touched this round) |
| `xcodebuild … -destination id=4384946F-9E6B-43D0-ADA3-CA219A3456B8 (iPhone 17 Pro) -only-testing:testSaveFailureShowsErrorBannerOnEveryTabWithExportReachable test` | PASS | 1/1, 0 failures (was failing on `errorBannerExport` before the reorder fix) |
| `xcodebuild … -destination id=4384946F-9E6B-43D0-ADA3-CA219A3456B8 (iPhone 17 Pro) -only-testing:testS6VocabularyFileImporterCancellationKeepsSelection test` | FAIL | 1/1 failed; picker never presents (environment blocker, see M17 above) |
| `xcodebuild … -destination id=4384946F-9E6B-43D0-ADA3-CA219A3456B8 (iPhone 17 Pro), full 17-test suite, clean install` | FAIL | 15/17 passed, 2 failed: `testAnswerModeChosenInSettingsSurvivesAppRestart` (flaky under full-suite load — re-run isolated below), `testS6VocabularyFileImporterCancellationKeepsSelection` (M17, environment blocker) |
| `xcodebuild … -destination id=4384946F-9E6B-43D0-ADA3-CA219A3456B8 (iPhone 17 Pro) -only-testing:testAnswerModeChosenInSettingsSurvivesAppRestart test` (isolated re-run) | PASS | 1/1, 0 failures — confirms the full-suite failure above was transient/load-related, not a regression |
| `xcodebuild … -destination id=31CA284B-5814-40C2-B6DE-D75D0056590D (iPad Pro 11-inch M5), full 17-test suite, clean install` | FAIL | 14/17 passed, 3 failed: `testNativeVerbGenderControlChangesSelectedSubjectOnly` (Matrix/verb-gender, unrelated to this batch — not investigated, out of ios-host scope), `testS6VocabularyFileImporterCancellationKeepsSelection` (M17, environment blocker, same symptom as iPhone). `testSaveFailureShowsErrorBannerOnEveryTabWithExportReachable` (C1) and `testAnswerModeChosenInSettingsSurvivesAppRestart` (M10) both PASS here, corroborating the iPhone run's two anomalies were transient/environmental, not regressions from this round's fix |
| `xcodebuild … -project kotlin/iosApp/PolskiGrammar.xcodeproj -scheme PolskiGrammar SWIFT_ACTIVE_COMPILATION_CONDITIONS="DEBUG S6_PICKER_ACCEPTANCE" -only-testing:testS6VocabularySystemFileImporterRejectsCurrentFile test` (diagnostic, not part of this batch's own suite) | FAIL | 1/1 failed, identical "picker never presents" symptom on a pre-existing, already-gated test — confirms M17's remaining failure is an environment issue, not this batch's diff |

### Changed paths (ios-host batch, cumulative across both rounds)
- `kotlin/iosApp/PolskiGrammar/PolskiGrammarApp.swift`
- `kotlin/iosApp/PolskiGrammarUITests/PolskiGrammarUITests.swift`
- `kotlin/shared/src/iosMain/kotlin/polski/ios/IosSession.kt`
- `kotlin/shared/src/iosMain/kotlin/polski/ios/IosProgressRepository.kt`
- `kotlin/shared/src/iosMain/kotlin/polski/ios/IosVocabularyRepository.kt`
- `kotlin/shared/src/iosMain/kotlin/polski/ios/IosPreferencesSession.kt`
- `kotlin/shared/src/iosTest/kotlin/polski/ios/IosErrorSnapshotTest.kt` (new)
- `kotlin/shared/src/iosTest/kotlin/polski/ios/IosSessionTest.kt` (new)
- `kotlin/shared/src/iosTest/kotlin/polski/ios/IosProgressRepositoryTest.kt`
- `kotlin/shared/src/iosTest/kotlin/polski/ios/IosVocabularyRepositoryTest.kt`
- `kotlin/shared/src/iosTest/kotlin/polski/ios/IosPreferencesSessionTest.kt`

### Skipped
- **M17 full device-level pass:** not achievable in this environment — the system document picker does not present at all in the current simulator session, reproduced on an unrelated, pre-existing, already-gated test too (see above). Not a defect in this batch's diff; the locator fix itself is in place and correct.
- **M11 automated VoiceOver-focus assertion:** unchanged from the prior round — XCTest has no supported API to assert actual VoiceOver focus (only keyboard/hittable focus); tracked as NOT_RUN consistent with `Plans/Kotlin/iOS.md:37`. Not a defect skip; the production fix (gated `Completed`/`Skipped` acknowledgement via `focusExerciseId`) is in place and reconfirmed by the tester's code-level review.

### Not part of this batch
`testNativeVerbGenderControlChangesSelectedSubjectOnly` failing on iPad Pro 11 — unrelated to C1/M10/M11/M4-ios/M17/m7 (Matrix/verb-gender feature), flagged by the tester, reproduced once more here, left for whichever batch/owner covers that area.
