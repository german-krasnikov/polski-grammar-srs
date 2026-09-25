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

## Batch: macos-host (C1 macOS part, M12, M4 macOS part, M13)

Native macOS host: `kotlin/macosApp/PolskiGrammarMac/PolskiGrammarMacApp.swift`, `kotlin/shared/src/macosMain/kotlin/polski/macos/*`, macosTest.

### C1 (macOS) — save-failure error not shown anywhere, export not reachable
- **File:** `kotlin/macosApp/PolskiGrammarMac/PolskiGrammarMacApp.swift`
- **Before:** `TrainingSnapshot.error` was decoded but never read by `MacModel` — only the generic `model.error` (read/import failures) drove the `.alert`. A progress save failure (`TrainingSnapshot.error` set, `loadStatus` still `Ready`) was invisible on every tab.
- **Fix:** added `ErrorBanner` (mirrors `PolskiGrammarApp.swift`'s), rendered above `NavigationSplitView` in `MacRootView` for every tab whenever `model.training?.error` is set, with an "Экспортировать JSON" button dispatching `model.send("export")` (`AppAction.RequestExport` → `UiEffect.DownloadJson`, now handled — see M12).
- **RED/GREEN:** no new Kotlin behavior — `TrainingSnapshot.error`'s "exposed regardless of loadStatus" contract is already locked by shared/commonTest; this is a pure Swift wiring fix. Verified by inspection and by the xcodebuild build below (compiles, banner/button wired to real state); no macOS XCTest target exists to assert UI text (per the original review's Checks table), so this is BUILD-verified rather than XCTest-verified, consistent with the existing gap noted for macOS.

### M12 — macOS never read the effects channel: reset unreachable, `pendingEffects` grows forever
- **Files:** `kotlin/macosApp/PolskiGrammarMac/PolskiGrammarMacApp.swift`; new test `kotlin/shared/src/macosTest/kotlin/polski/macos/MacSessionTest.kt`
- **Before:** `TrainingSnapshot` didn't decode `effects` at all. Every reveal queues a `FocusReveal` (`TrainingStore.addFocus`) that macOS never acknowledged, so `pendingEffects` grew by one per reveal for the rest of the process's life and was re-sent in every snapshot. There was no reset button and no handling for `ConfirmReset`.
- **Fix:**
  - `TrainingSnapshot.Effect` decodes `id`/`kind`/`exerciseId`/`filename`/`json`/`prompt` from the `effects` array `MacSnapshot.kt` already emitted.
  - `MacModel.handleEffects` claims each effect id once (`claimedEffects: Set<Int64>`), routes `"reset"` to `resetEffectId` (root `.alert` + `decideReset` → `resetDecision` command) and `"export"` to `exportEffectId`/`exportDocument` (root `.fileExporter`, success/failure both acknowledge), and acknowledges every other kind (currently only `"focus"`) as Skipped — macOS does not move VoiceOver focus for a reveal, matching `UiEffect`'s documented fallback for a host that does not act.
  - Added "Сбросить прогресс" button to `ProgressViewNative`, dispatching `AppAction.RequestReset` via `model.send("reset")`.
  - A successful progress import replaces the store (and its effect-id counter restarts at 1), so `MacModel.importJSON` now clears `claimedEffects`/`resetEffectId`/`exportEffectId`/`exportDocument` on success — otherwise a reused id would look already-claimed and its effect would never surface again.
- **RED/GREEN:** `MacSessionTest.focusEffectIsAcknowledgeableAndThenLeavesPendingEffects` failed before any fix was possible to observe without the test itself (it exercises `MacSession.acknowledgeEffect`, which already existed) — the real regression this batch fixes is Swift-side (the effect was never claimed at all); the Kotlin-side acknowledge path was already correct and is now exercised end-to-end by this test. See M13 below for the RED story on this file specifically.
- **Verification:** `xcodebuild` build below (compiles, effect routing/alerts/exporter wired); `:shared:macosArm64Test` (unaffected production Kotlin, no changes needed there — `MacSnapshot.kt` already emitted `effects`).

### M4 (macOS) — vocabulary due status never refreshed
- **File:** `kotlin/macosApp/PolskiGrammarMac/PolskiGrammarMacApp.swift`
- **Before:** no timer and no activation hook for vocabulary on macOS at all — a word rated "Again" stayed hidden ("На сейчас всё повторено") until some unrelated state change happened to trigger a snapshot.
- **Fix:** `MacModel` now runs a 30s `Timer` calling `vocab("refresh")` (mirrors the iOS host's `vocabularyRefreshTimer`) and observes `NSApplication.didBecomeActiveNotification` to refresh on app activation. Both callbacks hop back onto `@MainActor` via `Task { @MainActor in ... }` (avoids the `-Werror`-adjacent actor-isolation warning `xcodebuild` flagged on the first pass). Both are invalidated/removed in `close()` and `deinit`.
- **RED/GREEN:** no shared-Kotlin behavior changed (`MacVocabularySession.dispatch("refresh")` already existed and already worked); this is host wiring. Verified by `xcodebuild` build succeeding with zero warnings from this file, and by code inspection that the timer/observer actually call `vocab("refresh")` and are cleaned up.

### M13 — macOS had one target-test file; MacSession/MacSnapshot effect lifecycle and stale-guard were untested
- **File (new):** `kotlin/shared/src/macosTest/kotlin/polski/macos/MacSessionTest.kt`
- **Fix:** three tests against a real `MacSession` (temp-dir `MacProgressRepository`, fresh install), mirroring `IosVocabularyCollisionSnapshotTest`'s `CFRunLoopRunInMode` polling pattern (needed because `MacProgressRepository.load()`/`save()` hop onto `Dispatchers.Default`, so a snapshot taken immediately after construction — before spinning the run loop — still reflects the pre-`install()` default state, not the loaded one):
  - `focusEffectIsAcknowledgeableAndThenLeavesPendingEffects` — `continueIntroduction` queues exactly one `"focus"` effect; `acknowledgeEffect(id, "skipped")` removes it from the next snapshot (locks the M12 "must not grow" contract at the Kotlin/bridge boundary).
  - `staleRevealForAnExerciseNoLongerOnScreenIsIgnored` — `dispatch("reveal", <wrong id>)` leaves `phase`/`exercise` unchanged.
  - `oneReviewPerRateIgnoresARepeatedRateForTheSameStaleExerciseId` — rating an exercise once advances to the next card; replaying `"rate"` with the same, now-stale id does not score a second review.
- **RED:** first draft of these tests (without the `awaitReady`/`CFRunLoopRunInMode` polling helper) failed — `focusEffectIsAcknowledgeableAndThenLeavesPendingEffects` with `continueIntroduction should queue exactly one FocusReveal. Expected <1>, actual <0>` and `oneReviewPerRateIgnoresARepeatedRateForTheSameStaleExerciseId` with `Expected <Revealed>, actual <Question>` — both because the assertions ran before `store.start()`'s background load had actually landed on the Main-dispatched state (confirming these tests exercise real, not accidentally-vacuous, behavior).
- **GREEN:** after adding the polling helper, all three pass; full suite 202/202 (was 199/199 before this batch's 3 new tests).
- **No production Kotlin change needed:** the guards and acknowledge path this locks were already correct; the actual macOS defect (effects never claimed) was entirely in Swift (M12).

### Checks (working directory `/Users/german/Work/JS/polski-grammar-srs/kotlin` unless noted)

| Check | Result | Counts |
|---|---|---|
| `export JAVA_HOME=$(/usr/libexec/java_home -v 21 -a arm64); ./gradlew :shared:macosArm64Test --rerun` | PASS | 202/202 (was 199/199; +3 new `MacSessionTest` cases) |
| `xcodebuild -project kotlin/macosApp/PolskiGrammarMac.xcodeproj -scheme PolskiGrammarMac -configuration Debug -destination 'platform=macOS' -derivedDataPath /private/tmp/polski-mac-dd CODE_SIGNING_ALLOWED=NO build` | PASS | BUILD SUCCEEDED, zero warnings from this batch's diff (one pre-existing, unrelated AppIntents metadata warning) |

No macOS XCTest target exists in this project (confirmed in the original review's Checks table: "тестового target нет, значит macOS XCTest NOT_RUN"), so C1/M12/M4's Swift-side behavior is verified by build success plus code inspection, not by an automated UI test — same limitation the original review already recorded, not introduced by this batch.

### Changed paths
- `kotlin/macosApp/PolskiGrammarMac/PolskiGrammarMacApp.swift`
- `kotlin/shared/src/macosTest/kotlin/polski/macos/MacSessionTest.kt` (new)

### Skipped
- **Automated Swift UI assertion for C1/M12's banner, alert and export flow:** no macOS XCTest target exists in this project (pre-existing gap, not introduced here); skipped for the same reason `xcodebuild test` is NOT_RUN for macOS throughout this review. Not a defect skip — the production fix is verified by build success and code inspection.
- **Grammar (non-vocabulary) `RefreshTime` ticker on macOS:** out of this batch's stated M4 scope (macOS part is vocabulary-only per the batch instructions); the original review's macOS M4 note only covers vocabulary due-status ("в macOS обновления нет совсем" refers to vocabulary). Not fixed here; would be a separate finding if wanted.

## Batch: android-desktop (M4 Android+desktop part, M8, M9, m4, m6)

Native Android host and the retained JVM Compose Desktop preview: `kotlin/androidApp/**`, `kotlin/composeApp/src/androidMain/**`, `kotlin/composeApp/src/desktopMain/kotlin/polski/desktop/Main.kt`.

### M4 (Android + desktop preview) — vocabulary due status never refreshed by the periodic ticker/resume
- **Files:** `kotlin/androidApp/src/main/java/dev/polski/grammarmatrix/MainActivity.kt`, `kotlin/composeApp/src/desktopMain/kotlin/polski/desktop/Main.kt`
- **Before:** the 30s ticker (`repeatOnLifecycle(STARTED)` on Android, the `LaunchedEffect(store)` loop on desktop) only dispatched `AppAction.RefreshTime` to the grammar `TrainingStore`. `VocabularySession.refresh()` was never called, so a word rated "Again" stayed hidden behind "На сейчас всё повторено" until an unrelated state change.
- **Fix:** added `session.vocabulary.refresh()` next to the existing `dispatch(AppAction.RefreshTime)` call inside the same loop on both hosts. On Android the loop lives inside `repeatOnLifecycle(STARTED)`, so this also covers app resume (the block restarts and fires immediately on every return to `STARTED`), matching the review's "ticker/resume" wording without a second hook.
- **RED/GREEN:** `VocabularySession.refresh()` itself is pre-existing, already-tested shared behavior (no Kotlin production change needed); this is host wiring, matching the precedent set by the iOS/macOS batches for the same finding. Verified by code inspection (the call sites) and by the full checks below (`:androidApp:testDebugUnitTest`, `:androidApp:assembleDebug`, `:composeApp:desktopTest`, all green) plus the emulator smoke run.

### M8 — `AndroidGlassPreferences.save`/`load` ran synchronous SharedPreferences `commit()`/read on the main thread
- **Files:** `kotlin/androidApp/src/main/java/dev/polski/grammarmatrix/AndroidUserPreferencesStore.kt` (renamed from `AndroidGlassPreferences.kt`, see m6), `AndroidSessionViewModel.kt`
- **Before:** `AndroidGlassPreferences(context: Context)` exposed plain `fun load(): PreferencesLoad` / `fun save(...): PreferencesSave`, called directly from the ViewModel's constructor and from `setAppearance`/`persistExplanationMethod` on the main thread — blocking disk I/O and a `commit()` read-back, unlike `AndroidProgressRepository`/`AndroidVocabularyRepository`, which already use `withContext(Dispatchers.IO)`.
- **Fix:** `load()`/`save()` are now `suspend fun`, wrapped in `withContext(Dispatchers.IO)` plus a `Mutex` (same shape as the existing progress/vocabulary repositories). `AndroidSessionViewModel` now loads preferences in `init { viewModelScope.launch { ... } }` instead of the constructor; the initial `preferences` value is the default `UserPreferencesV2()` until that load resolves, at which point, if the loaded `explanationMethod` differs from the store's initial default, `AppAction.SetExplanationMethod` is dispatched to reconcile the already-started `TrainingStore`. All four setters (`setAppearance`, `persistExplanationMethod`, `setMotion`, `setSwipeRatingEnabled`) now go through a shared `updatePreferences(next)`: `preferences` is updated synchronously (immediate, responsive UI, no main-thread disk I/O in the call path) and the write is launched on `viewModelScope.launch { preferencesStore.save(next) }`; a `PreferencesSave.WriteFailed` rolls `preferences` back to the previous value and surfaces `preferencesError`, preserving the old failure-visible behavior.
- **RED/GREEN:** no round-trip *behavior* changed (encode/decode/migration semantics are the same `UserPreferencesCodec`), so the direct regression evidence is the renamed `AndroidUserPreferencesStoreTest` (5 cases, adapted from the pre-existing `AndroidGlassPreferencesTest` to call the now-`suspend` `load()`/`save()` via `runBlocking`) staying green — a wrong signature or a dropped `withContext` would fail these to compile or run. The new async-wiring behavior in the ViewModel (state updates before the disk write lands) is covered by the M9 tests below, which explicitly assert the value is already in `preferences` synchronously after the setter returns, before the background save is awaited.

### M9 — Android had no Motion, swipe-rating, or reminders settings; swipe-to-rate was hardcoded on
- **Files:** `kotlin/androidApp/src/main/java/dev/polski/grammarmatrix/MainActivity.kt`, `AndroidSessionViewModel.kt`; `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidContent.kt`, `AndroidTrainingScreen.kt`
- **Before:** `AndroidSettingsScreen` only had an Appearance section. `VocabularyScreen(..., enableSwipeRating = true)` was hardcoded, and `AndroidRatingActions` (the grammar training screen's own swipe-to-rate surface, separate from `VocabularyScreen`'s) always attached `detectHorizontalDragGestures` unconditionally, so `UserPreferencesV2.swipeRatingEnabled = false` had no effect and a random swipe could silently misrate a card. There was no reminders section and no Motion section.
- **Fix:**
  - `AndroidSessionViewModel` gained `setMotion(Motion)` and `setSwipeRatingEnabled(Boolean)`, both routed through the M8 `updatePreferences` path.
  - `AndroidSettingsScreen` gained a "Движение" segmented control (Системное/Уменьшенное, mirrors desktop's wording) and a "Свайп-оценка карточек" `Switch`, both bound to `session.preferences`; a "Напоминания" section states "Недоступно на Android в этой сборке" (matches the desktop host's own honest-unavailable text) rather than a silent gap.
  - `MainActivity`'s `VocabularyScreen` call now passes `enableSwipeRating = session.preferences.swipeRatingEnabled` instead of the hardcoded `true` (`VocabularyScreen` already gated its own internal swipe gesture and compact/wide layout on this single flag — no change needed inside the shared composable).
  - `AndroidContent`/`AndroidTrainingScreen`/`AndroidRatingActions` gained a threaded `swipeRatingEnabled: Boolean` parameter (default `true` to keep other unrelated call sites compiling); `AndroidRatingActions` now only attaches the drag-gesture modifier, and only shows the "Свайп влево/вправо" hint text, when it is `true`. The two ratings buttons (Again/Good) remain available either way.
  - Preferences JSON import/export was intentionally **skipped** (see below) — not trivial for this batch.
- **RED:** wrote `AndroidSessionViewModelPreferencesTest` (2 cases) against the intended `setMotion`/`setSwipeRatingEnabled` API before it existed on `AndroidSessionViewModel`; confirmed RED by temporarily removing the two methods (restoring afterwards) — `:androidApp:testDebugUnitTest` then failed at `:androidApp:compileDebugKotlin` with `Unresolved reference 'setMotion'`/`'setSwipeRatingEnabled'` from `MainActivity.kt`'s new settings UI, i.e. a real missing-API failure, not a tooling issue.
- **GREEN:** with the methods restored, both tests pass. Each test asserts the state is reflected in `session.preferences` synchronously right after the call (no dispatcher trickery needed, since `updatePreferences` applies the in-memory update before launching the background save), then polls `AndroidUserPreferencesStore(context).load()` (bounded `withTimeout(2_000)`, 20 ms interval — the same "poll for the real async write to land" pattern the macOS batch used for `MacSessionTest`, needed because the save genuinely runs on `Dispatchers.IO`, a real thread the test's virtual/main state can't simply "advance") to confirm the value survived the actual disk round-trip.
- **Emulator smoke (real device evidence, not just unit tests):** built and installed the debug APK on `emulator-5554`, launched the app, opened Settings via `adb shell input tap`, confirmed via `uiautomator dump` that "Движение" (Системное/Уменьшенное), "Свайп-оценка карточек" (a checked `Switch`) and "Напоминания: Недоступно на Android в этой сборке" are all rendered. Tapped "Уменьшенное" and the swipe-rating switch; re-dumped and confirmed both flipped (`Motion.Reduced` segment `checked="true"`, switch `checked="false"`), Appearance untouched. Force-stopped and relaunched the app, reopened Settings, re-dumped: both choices survived the restart (persisted to `SharedPreferences`). No `AndroidRuntime:E` crashes in `logcat` across the run.

### m4 — import dialog dropped on rotation
- **File:** `kotlin/androidApp/src/main/java/dev/polski/grammarmatrix/MainActivity.kt`
- **Fix:** `confirmImport` changed from `remember { mutableStateOf(false) }` to `rememberSaveable { mutableStateOf(false) }`, matching the existing `showSettings` pattern.
- **Verification:** covered by `:androidApp:assembleDebug` (compiles) and code inspection; a config-change/rotation instrumentation test was judged not worth adding for a one-line `rememberSaveable` swap during active development (LEAN MODE) — the same fix shape already proven correct for `showSettings` in this file.

### m6 — `AndroidGlassPreferences` name is stale ("glass" UI was rolled back)
- **Files:** `kotlin/androidApp/src/main/java/dev/polski/grammarmatrix/AndroidGlassPreferences.kt` → `AndroidUserPreferencesStore.kt` (renamed); `AndroidSessionViewModel.kt` (field `glassPreferences` → `preferencesStore`); test file `AndroidGlassPreferencesTest.kt` → `AndroidUserPreferencesStoreTest.kt`.
- **Kept compatible on purpose:** the `SharedPreferences` file name (`"polski-preferences"`) and both stored keys (`"document"`, legacy `"explanationMethod"`) are unchanged, so existing installs migrate exactly as before — only the Kotlin class/file/field names changed, per the review's "поле схемы оставить" instruction (the schema's own `glassTintPercent` field is untouched; that's a separate, already-rejected finding, see the original review's §7 item 2/5/8).
- **Verification:** `AndroidUserPreferencesStoreTest` (5 cases, same assertions as the old `AndroidGlassPreferencesTest`, including the legacy-key migration and v1→v2 document migration cases) green under the new name.

### Checks (working directory `/Users/german/Work/JS/polski-grammar-srs/kotlin` unless noted)

| Check | Result | Counts |
|---|---|---|
| `export JAVA_HOME=$(/usr/libexec/java_home -v 21 -a arm64); export ANDROID_HOME=$HOME/Library/Android/sdk; ./gradlew :androidApp:testDebugUnitTest :androidApp:assembleDebug :composeApp:desktopTest --rerun` | PASS | Android unit 18/18 (was 16/16 + this batch's 7 new cases across `AndroidUserPreferencesStoreTest` and `AndroidSessionViewModelPreferencesTest`, replacing the 5 old `AndroidGlassPreferencesTest` cases 1:1 by rename); debug APK assembled; desktop 54/54 |
| Emulator smoke: install debug APK on `emulator-5554`, launch, open Settings, toggle Motion + swipe-rating via `adb shell input tap`, `uiautomator dump` before/after/after-relaunch | PASS | New controls present, both toggles flip and persist across a force-stop/relaunch; no `AndroidRuntime:E` in `logcat` |

### Changed paths
- `kotlin/androidApp/src/main/java/dev/polski/grammarmatrix/AndroidUserPreferencesStore.kt` (new, replaces `AndroidGlassPreferences.kt`)
- `kotlin/androidApp/src/main/java/dev/polski/grammarmatrix/AndroidSessionViewModel.kt`
- `kotlin/androidApp/src/main/java/dev/polski/grammarmatrix/MainActivity.kt`
- `kotlin/androidApp/src/test/java/dev/polski/grammarmatrix/AndroidUserPreferencesStoreTest.kt` (new, replaces `AndroidGlassPreferencesTest.kt`)
- `kotlin/androidApp/src/test/java/dev/polski/grammarmatrix/AndroidSessionViewModelPreferencesTest.kt` (new)
- `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidContent.kt`
- `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidTrainingScreen.kt`
- `kotlin/composeApp/src/desktopMain/kotlin/polski/desktop/Main.kt`

### Skipped
- **Preferences JSON import/export on Android (part of M9's original file list):** the review's fix direction bundles it into M9, but it is not trivial (needs a new `ActivityResultContracts.GetContent`/`CreateDocument` picker pair, wiring through `AndroidSessionViewModel`, and decode/recovery-required handling matching the desktop host's `DesktopPreferencesController.importJson`/`exportRaw`) — explicitly out of LEAN MODE's "skip if not tiny" allowance for this batch. Progress and vocabulary import/export already exist on Android; only the small preferences document is missing. Left for a follow-up if wanted.
- **Instrumented/Compose-UI test for the swipe-gesture gating itself (vs. the emulator smoke):** `androidApp` has no `androidx.compose.ui.test`/instrumented-test dependency configured, and adding one is more than this batch's scope; the gating logic (`if (swipeRatingEnabled) Modifier.pointerInput(...) else Modifier`) is simple enough that the emulator smoke (toggle off, settings persist) plus code inspection was judged sufficient evidence, consistent with LEAN MODE.
- **`Plans/Kotlin/NativeUIRollback.md` and `Plans/Kotlin/Review-2026-09-25.md` still say `AndroidGlassPreferences`:** left unchanged — the first is a historical record of the glass-UI rollback (renaming it would misrepresent what the code was called at that point in time) and the second is the review report itself, not something this batch should edit.

## Batch: web-host (M7, M6, m3, m15)

### M7 — periodic `RefreshTime` reset caret, selection and scroll in the answer textarea
- **Files:** `kotlin/composeApp/src/webMain/kotlin/polski/ui/TrainingWebApp.kt` (`TrainingDomRenderer.render`); `kotlin/composeApp/src/webMain/kotlin/polski/ui/VocabularyWeb.kt` (vocabulary answer textarea).
- **Root cause confirmed:** `TrainingStore.project()` always stamps `now = captured.at`, so the renderer's "nothing changed but `draft`" fast path (`old.copy(draft = state.draft) == state`) never matches on a `RefreshTime` dispatch (fired by the 30s timer, `visibilitychange`, and window `focus`) — `now` (and often `dueCount`/`nextDue`/`intervals`) always differs. The renderer falls through to the full rebuild (`content.textContent = ""` + re-append), which recreates the `<textarea>`; only `.focus()` was restored, so the caret always landed at the end and any selection was lost. The `Vocabulary` answer textarea had no `id` at all, so even focus was lost there.
- **Fix (the review's listed alternative, chosen for a minimal diff over restructuring the renderer into a time-only-partial-update path):** before the rebuild, capture `selectionStart`, `selectionEnd` and `scrollTop` from `document.activeElement` if it is an `HTMLTextAreaElement`; after focus is restored post-rebuild, reapply them (indices clamped to the new value's length) when the restored element is again a textarea. Gave the Vocabulary answer textarea a stable `id="vocabulary-answer"` (the Training answer textarea already had `id="training-answer"`), so the existing `focusId`/`getElementById` restore path — and now the new selection restore — reaches it too.
- **RED:** added `a periodic time refresh does not disturb caret or selection while typing` to `tests/browser/kotlin-training.spec.ts` (fills the answer, sets `setSelectionRange(6, 10)`, dispatches a synthetic `window` `focus` event — the same `RefreshTime` path the real timer/visibility/focus listeners use — then asserts focus and `[selectionStart, selectionEnd]`). Reverted only the renderer fix, rebuilt the JS distribution, ran the test on chromium: failed with `[23, 23]` (caret pushed to the end) instead of `[6, 10]`.
- **GREEN:** restored the fix, rebuilt, reran: passes on both the `js` and `wasm` distributions on chromium, alongside the full existing `kotlin-training.spec.ts` suite (37/37 both branches, see Checks).

### M6 — web vocabulary writes bypassed the safe-write pattern the rest of the app uses
- **File:** `kotlin/composeApp/src/webMain/kotlin/polski/ui/VocabularyWeb.kt`.
- **Kept small on purpose:** did not migrate the web Vocabulary UI onto the shared `VocabularySession`/`VocabularyRepository` port (the review's primary suggestion) — that is a real port implementation plus wiring change, out of scope for a lean fix batch. Implemented its explicitly offered fallback instead: read-back after write, a backup key before import, and a `storage` listener.
- **Fix:**
  - Added `writeDocument(next, backupCurrent)`, mirroring the existing `WebPreferencesRepository.save`/`import` pattern already in this codebase (`kotlin/composeApp/src/webMain/kotlin/polski/platform/WebPreferencesRepository.kt`): reads back the previous value, optionally backs it up to a new `polski-vocabulary-pl-ru-v1-backup` key (verified by reading it back), writes the new value, reads it back to confirm it landed, and restores the previous value on any failure (write exception or read-back mismatch).
  - `commit()` (used by rating, catalog selection and the own-word editor) now delegates to `writeDocument(next, backupCurrent = false)`, keeping its existing "blocked while recovery is pending" gate in front.
  - The import button ("Добавить данные из JSON") now calls `writeDocument(merged, backupCurrent = true)` directly (bypassing the recovery gate on purpose, since import is the only way to escape `recoveryRaw != null`), and only clears `recoveryRaw`/`importText` on success.
  - Added a `window` `"storage"` listener (registered once in `init`, removed in the new `close()`) that reloads the document from `localStorage` and re-invokes the last `render()` call's `refresh` callback whenever another tab writes the vocabulary key — `storage` events never fire in the tab that made the write, so this only fires for genuinely cross-tab changes. `TrainingDomRenderer.close()` now also calls `vocabulary.close()`.
- **RED:** added two cases to `tests/browser/kotlin-vocabulary.spec.ts`: (1) import keeps a read-back-verified backup of the previous document under the new backup key; (2) a synthetic cross-tab `storage` event (same-tab writes can't produce a real one, so the test writes `localStorage` directly and dispatches `StorageEvent` itself) reloads the catalog count. Reverted only the `VocabularyWeb.kt` fix (and the now-dangling `vocabulary.close()` call in `TrainingWebApp.kt`, temporarily), rebuilt, ran on chromium/js: both failed — no backup key written (`null` vs expected previous document), and the catalog count stayed at `1` instead of dropping to `0` after the synthetic `storage` event.
- **GREEN:** restored both fixes, rebuilt, reran: both new cases plus the full existing `kotlin-vocabulary.spec.ts` suite pass on `js` and `wasm` chromium (39/39 both branches).

### m3 — `attemptedEffects` grew without bound
- **File:** `kotlin/composeApp/src/webMain/kotlin/polski/ui/TrainingWebApp.kt:67`, `:151-156`.
- **Fix:** after the per-render effect-execution loop, `attemptedEffects.retainAll(state.pendingEffects.mapTo(mutableSetOf()) { it.id })` — bounds the set to ids still present in `pendingEffects`; acknowledged/removed effects (and any that were skipped/completed and thus already gone from `pendingEffects`) stop being remembered.
- **Verification:** covered indirectly by every Playwright spec that drives an effect to completion (export, reset, focus-reveal) across the full `kotlin-training.spec.ts`/`kotlin-preferences-settings.spec.ts`/`kotlin-vocabulary.spec.ts` runs (all green, see Checks) — a one-line bounding fix with no behavior change on the happy path, so a dedicated unit test asserting set size after many effect cycles was judged not worth it in LEAN MODE; the existing effect-completion specs already exercise `add`/`remove` on this set every run.

### m15 — Playwright screenshots overwrote tracked PNGs on every run
- **File:** `tests/browser/kotlin-preferences-settings.spec.ts:464` (now `:466`).
- **Fix:** `directory = testInfo.outputPath('ux2-test')` instead of the tracked `'Plans/Kotlin/artifacts/ux2-test'` literal. `testInfo.outputPath(...)` resolves under this config's existing gitignored `outputDir` (`./test-results/kotlin-${branch}/...`), which is Playwright's own idiomatic per-test artifact location — no new ignore rule needed. Left the currently tracked PNGs under `Plans/Kotlin/artifacts/ux2-test/` alone (not asked to delete them, and deleting tracked review material is out of scope for a fix batch).
- **Verification:** ran the spec itself (part of the Checks below); screenshots now land under `test-results/kotlin-<branch>/<test-title>/ux2-test/...` and `git status` after the run shows no changes under `Plans/Kotlin/artifacts/`.

### Checks (working directory `/Users/german/Work/JS/polski-grammar-srs/kotlin` unless noted; `export JAVA_HOME=$(/usr/libexec/java_home -v 21 -a arm64); export ANDROID_HOME=$HOME/Library/Android/sdk` for the Gradle runs)

| Check | Result | Counts |
|---|---|---|
| `./gradlew :composeApp:composeCompatibilityBrowserDistribution` (fresh distribution) | PASS | Compiles and bundles `js` + `wasmJs` + compatibility dist |
| `./gradlew :composeApp:jsBrowserTest` | PASS | 22/22 |
| `./gradlew :composeApp:wasmJsBrowserTest` | PASS | 22/22 |
| `KOTLIN_SPIKE_DIST=.../dist/js/productionExecutable KOTLIN_SPIKE_BRANCH=js npx playwright test --config=playwright.kotlin.config.ts tests/browser/kotlin-training.spec.ts tests/browser/kotlin-vocabulary.spec.ts tests/browser/kotlin-preferences-settings.spec.ts --project=chromium` | PASS | 39/39 |
| `KOTLIN_SPIKE_DIST=.../dist/wasmJs/productionExecutable KOTLIN_SPIKE_BRANCH=wasm npx playwright test --config=playwright.kotlin.config.ts tests/browser/kotlin-training.spec.ts tests/browser/kotlin-vocabulary.spec.ts tests/browser/kotlin-preferences-settings.spec.ts --project=chromium` | PASS | 39/39 |

### Changed paths
- `kotlin/composeApp/src/webMain/kotlin/polski/ui/TrainingWebApp.kt`
- `kotlin/composeApp/src/webMain/kotlin/polski/ui/VocabularyWeb.kt`
- `tests/browser/kotlin-training.spec.ts`
- `tests/browser/kotlin-vocabulary.spec.ts`
- `tests/browser/kotlin-preferences-settings.spec.ts`

### Skipped
- **Full migration of web Vocabulary onto `VocabularySession`/`VocabularyRepository` (M6's primary fix direction):** real architecture work (new web `VocabularyRepository` adapter, wiring `VocabularyWebController` through the shared session instead of talking to `localStorage` directly) rather than a lean fix; the review itself offers the backup+read-back+listener fallback ("или хотя бы...") that this batch implemented instead. Left for a follow-up if the two-tabs-editing-simultaneously race (last-write-wins on the exact same key) needs to be fully eliminated rather than mitigated.
- **Restructuring the renderer to update only time-derived DOM parts (M7's primary fix direction) instead of the full rebuild:** would need `renderHeader`'s stats, the "По расписанию · N" button label and the rating-button interval labels to be patchable independently while a Question-phase textarea keeps its identity — a larger renderer refactor. Took the review's explicitly listed alternative (capture/restore `selectionStart`/`selectionEnd`/`scrollTop`) instead, which fixes the actual user-visible symptom with a much smaller diff.
- **Firefox/WebKit projects for the touched specs:** not run (matches the original review's own NOT_RUN scope for web; only `chromium` is configured to run by default in this environment per the task's instructions).

### Correction round — reviewer blocker: `pendingRefresh` replayed a stale route after M6's `storage` listener
- **Reviewer finding (major):** `VocabularyWebController.pendingRefresh` is set only inside `render()`, to a closure captured by `TrainingDomRenderer` (`kotlin/composeApp/src/webMain/kotlin/polski/ui/TrainingWebApp.kt:267-270`) that closes over the `route`/`state` from that specific render call. `VocabularyWebController` is a long-lived field of `TrainingDomRenderer`, not recreated per route, so once the user opens Vocabulary and then navigates elsewhere, `pendingRefresh` keeps holding that stale `route = Vocabulary` closure. A later cross-tab `storage` write (M6's new listener) calls `reload()` → `pendingRefresh?.invoke()`, which re-runs `render(root, state, route = Vocabulary(stale), ...)` regardless of the app's real current route — tearing down whatever page (e.g. Training, mid-answer) the user was actually looking at and replacing it with Vocabulary, with the nav bar and URL now disagreeing with the visible page.
- **File:** `kotlin/composeApp/src/webMain/kotlin/polski/ui/VocabularyWeb.kt`, `kotlin/composeApp/src/webMain/kotlin/polski/ui/TrainingWebApp.kt`.
- **Fix:** added `VocabularyWebController.deactivate()` (`pendingRefresh = null`). `TrainingDomRenderer.render()` calls `vocabulary.deactivate()` whenever the route being rendered is not `Vocabulary` (right before the route `when`), so leaving the Vocabulary tab always drops the stale closure — a later `storage` event's `reload()` still refreshes the in-memory `document` (so the catalog is fresh whenever the user comes back) but no longer calls a callback that would repaint the wrong route. Re-entering Vocabulary sets a fresh `pendingRefresh` on the next `render()` call as before, so the original M6 cross-tab-reload behavior on that tab is unaffected.
- **RED:** added `'a storage write from another tab does not resurrect a stale vocabulary route'` to `tests/browser/kotlin-vocabulary.spec.ts` — opens Vocabulary, selects a card (so the storage key exists), navigates to Training (`Карточки`), then dispatches a synthetic cross-tab `storage` event on the vocabulary key, and asserts Training's nav button keeps `aria-current="page"` and `.vocabulary-page` is absent. With the fix reverted: FAILED — `Карточки` lost `aria-current` (empty string) because the stale `render(..., route = Vocabulary)` replay repainted the Vocabulary page and reset the nav to Vocabulary's `active` state. With the fix restored: PASSED on `js` and `wasm` chromium.
- **GREEN / checks re-run:**
  - `./gradlew :composeApp:composeCompatibilityBrowserDistribution` (cwd `kotlin/`) — PASS, fresh `js`+`wasmJs`+compat dist rebuilt with the fix.
  - `./gradlew :composeApp:jsBrowserTest` — PASS.
  - `./gradlew :composeApp:wasmJsBrowserTest` — PASS.
  - `KOTLIN_SPIKE_DIST=.../dist/js/productionExecutable KOTLIN_SPIKE_BRANCH=js npx playwright test --config=playwright.kotlin.config.ts tests/browser/kotlin-training.spec.ts tests/browser/kotlin-vocabulary.spec.ts tests/browser/kotlin-preferences-settings.spec.ts --project=chromium` — PASS, 40/40 (the new case included).
  - `KOTLIN_SPIKE_DIST=.../dist/wasmJs/productionExecutable KOTLIN_SPIKE_BRANCH=wasm npx playwright test --config=playwright.kotlin.config.ts tests/browser/kotlin-training.spec.ts tests/browser/kotlin-vocabulary.spec.ts tests/browser/kotlin-preferences-settings.spec.ts --project=chromium` — PASS, 40/40.
  - One transient failure was seen mid-round on both branches in `kotlin-preferences-settings.spec.ts` ("narrow layout keeps the next training action visible and the document within the viewport", overflow `70` vs expected `<= 1`) when run as part of the full 3-file/40-test batch; re-running that single test alone (`--repeat-each 2/3`) passed every time on both `js` and `wasm`, and the immediately following full-batch re-run also passed 40/40 on both branches, confirming it is a pre-existing timing flake under parallel/first-run load, not caused by this fix. Not itself touched.
- **Changed paths (correction round):** `kotlin/composeApp/src/webMain/kotlin/polski/ui/VocabularyWeb.kt`, `kotlin/composeApp/src/webMain/kotlin/polski/ui/TrainingWebApp.kt`, `tests/browser/kotlin-vocabulary.spec.ts`, `Plans/Kotlin/Review-2026-09-25-Fixes.md`. No other batch's files touched.

## Batch: build-docs-cleanup (M14, M18, m5, m10, m11, m12, m14, JDK-doc)

### M14 — CI never ran Android/desktop JVM tests
- **File:** `.github/workflows/kotlin-check.yml`.
- **Fix:** added a second `jvm` job (`runs-on: ubuntu-latest`, JDK 21 via `actions/setup-java@v4`, Android SDK via `android-actions/setup-android@v3` + `sdkmanager "platforms;android-37" "build-tools;36.0.0"`) running `:shared:desktopTest`, `:composeApp:desktopTest` and `:androidApp:testDebugUnitTest`, independent of the existing `web` job.
- **Checks:** `python3 -c "import yaml; yaml.safe_load(open('.github/workflows/kotlin-check.yml'))"` — PASS (parses). The three new tasks were run locally (see below) instead of by re-triggering CI.

### M18 — stale React↔Kotlin parity-gate wording
- **Files:** `.claude/skills/workflow/SKILL.md` (the `keep the existing web implementation as a behavior oracle until its parity gate is complete` line), `AGENTS.md` (`не означает закрытия web parity … gates`), `Plans/Kotlin/PreGlassRollback.md` (`React остаётся эталоном до отдельного parity gate`).
- **Fix:** reworded all three to record the 2026-09-25 decision (stage 11 dropped; per-host native UI; React is a behavior reference, no longer a blocking gate).
- **Checked, no change needed:** `kmp-web`, `testing-tdd` and `playwright-testing` skills — their React-parity sections describe comparison technique and fixture hygiene, not a blocking closure gate; nothing there requires the dropped stage-11 gate, so left as-is.

### m5 — dead stage-2 Spike
- **Grep proof of no production references:** `grep -rln "Spike" kotlin --include="*.kt"` only matched the spike files themselves; no navigation/host wiring calls `SpikeScreen(`. `tests/browser/kotlin-spike.spec.ts` is absent from `playwright.kotlin.config.ts`'s `testMatch`.
- **Deleted:** `kotlin/composeApp/src/commonMain/kotlin/polski/ui/SpikeScreen.kt`, `.../androidMain/.../SpikeControlsAndroid.kt`, `.../desktopMain/.../SpikeControlsDesktop.kt`, `.../webMain/.../SpikeHtmlControls.kt`, `kotlin/shared/src/commonMain/kotlin/polski/spike/SpikeSession.kt`, `kotlin/shared/src/commonTest/kotlin/polski/spike/SpikeSessionTest.kt`, `tests/browser/kotlin-spike.spec.ts`, plus the now-empty `polski/spike` directories.
- **Doc fix:** `kotlin/README.md` no longer claims "the old stage-2 spike remains in source".
- **Checks:** `:shared:compileKotlinMetadata`/`:shared:desktopTest` and `:composeApp:compileKotlinJs :composeApp:compileKotlinWasmJs`/`:composeApp:desktopTest` below all compiled and passed with the spike sources gone (no leftover reference anywhere else in the module graph).

### m10 — `rootProject.name` still "spike"
- **File:** `kotlin/settings.gradle.kts` — `polski-web-spike` → `polski-grammar-kotlin`.
- **Verified safe before renaming:** `grep -rln "polski-web-spike" .` outside `build/` only matched `settings.gradle.kts` itself and the review doc; `kotlin/kotlin-js-store/yarn.lock` and `kotlin/kotlin-js-store/wasm/yarn.lock` do not reference the project name (Kotlin/JS npm module names come from the per-module `js`/`wasmJs` package names, e.g. `polski-web-spike-shared`, which are unaffected by `rootProject.name` alone but are otherwise not depended on by name anywhere); the real distribution JS filenames served/tested (`composeApp.js`, `originJsComposeApp.js`, `originWasmComposeApp.js`) come from the Compose webpack task, not the project name. No `.github` workflow references the string.
- **Checks:** `:composeApp:compileKotlinJs :composeApp:compileKotlinWasmJs`, `:androidApp:assembleDebug` below both passed after the rename.

### m11 — `generateCoursePackSource` not wired to metadata compiles
- **File:** `kotlin/shared/build.gradle.kts`.
- **Fix:** removed the fragile `tasks.matching { it.name.startsWith("compileKotlin") || it.name == "compileAndroidMain" }.configureEach { dependsOn(...) }` and instead derived the `commonMain` `srcDir` from the task's own outputs: `kotlin.srcDir(generateCoursePackSource.map { it.outputs.files.singleFile })`. Gradle then infers the task dependency automatically for every consumer of `commonMain` (JS, Wasm, desktop, Android, iOS, macOS and metadata compiles alike), not just tasks whose name happened to match the old prefix check.
- **RED:** reverted to the old file (`git show HEAD:kotlin/shared/build.gradle.kts`), deleted the generated directory, ran `./gradlew :shared:compileCommonMainKotlinMetadata --rerun-tasks` — task graph was `transformCommonMainDependenciesMetadata → compileCommonMainKotlinMetadata` with **no `generateCoursePackSource` anywhere in it** (confirmed the missing dependency: `compileCommonMainKotlinMetadata` does not start with `compileKotlin`, so the old matcher never caught it).
- **GREEN:** restored the fix, deleted the generated directory again, re-ran the same command — task graph now correctly shows `generateCoursePackSource → transformCommonMainDependenciesMetadata → compileCommonMainKotlinMetadata`. Also re-verified `:shared:compileKotlinMetadata` (the aggregate task) and `:composeApp:compileKotlinJs :composeApp:compileKotlinWasmJs`, `:composeApp:desktopTest`, `:androidApp:assembleDebug` (which exercises `shared:compileAndroidMain`) all still generate-then-compile correctly.

### m12 — surrogate pair could be split by `chunked(8000)`
- **File:** `kotlin/shared/build.gradle.kts`, `literalChunks`.
- **Fix:** replaced `source.chunked(8000)` with a manual loop that computes each chunk boundary and backs it off by one UTF-16 unit whenever it would land between a high and a low surrogate (`Character.isHighSurrogate`/`isLowSurrogate`), before doing the existing string-literal escaping. Not currently reachable by the actual course/frequency JSON content (max code point observed is U+2194 per the review), so no dedicated RED fixture was practical without fabricating course data outside this batch's scope; verified instead via the same real-content compile/test runs below (no change in generated output for the current course pack — chunk boundaries are unaffected when no chunk edge falls inside a surrogate pair).

### m14 — stale glass benchmark scripts
- **Files moved:** `scripts/benchmark-android-glass.mjs`, `scripts/benchmark-android-glass-auto.mjs`, `scripts/benchmark-kotlin-glass.mjs`, `scripts/benchmark-glass-rollout.mjs` → `Plans/Kotlin/archive/scripts/` (archived rather than deleted, since three docs cite their exact historical runs as evidence).
- **Doc fixes:** `Plans/Kotlin/GlassAndroidPerformance.md`, `Plans/Kotlin/GlassWebPerformance.md`, `Plans/Kotlin/GlassOpticsPilot.md` — updated both the markdown links and the inline `node scripts/...` command examples to the new `Plans/Kotlin/archive/scripts/...` path.
- **Confirmed pre-existing, out of scope:** `Plans/Kotlin/GlassAndroidPerformance.md` also links to `kotlin/androidApp/src/{debug,benchmark}/java/dev/polski/grammarmatrix/GlassBenchmarkActivity.kt`, which no longer exists in source (only stale `.class` files remain under `build/`). That link was already broken at HEAD (`git show HEAD:...` has the identical dead link) and is not one of the paths this finding named — left unchanged rather than expanding scope.
- **Checks:** manual link-resolution script over the edited docs confirmed every new/changed link resolves to an existing file; the two pre-existing `GlassBenchmarkActivity.kt` links remain the only unresolved links, unchanged from HEAD.

### JDK-doc — Android build instructions still pointed at x86_64 openlogic JDK
- **File:** `kotlin/README.md`, Android section.
- **Fix:** `export JAVA_HOME=/Library/Java/JavaVirtualMachines/openlogic-openjdk-21.jdk/Contents/Home` → `export JAVA_HOME=$(/usr/libexec/java_home -v 21 -a arm64)`, with a one-line note that an x86_64 JDK runs under Rosetta and misidentifies the host as `macos_x64`, breaking native Gradle tasks (matches the actual failure mode recorded in the original review for `iosSimulatorArm64Test`/`macosArm64Test`).

### Checks (this batch)
| Command | cwd | Result | Detail |
|---|---|---|---|
| `python3 -c "import yaml; yaml.safe_load(open('.github/workflows/kotlin-check.yml'))"` | repo root | PASS | parses |
| `./gradlew :shared:compileCommonMainKotlinMetadata --rerun-tasks` (RED: old file, deleted generated dir) | `kotlin/` | FAIL (missing dependency, as expected) | `generateCoursePackSource` absent from the task graph |
| `./gradlew :shared:compileCommonMainKotlinMetadata --rerun-tasks` (GREEN: fixed file, deleted generated dir) | `kotlin/` | PASS | `generateCoursePackSource` runs first |
| `./gradlew :shared:compileKotlinMetadata` | `kotlin/` | PASS | — |
| `./gradlew :shared:desktopTest` | `kotlin/` | PASS | 195/195, 0 failures/errors |
| `./gradlew :composeApp:compileKotlinJs :composeApp:compileKotlinWasmJs` | `kotlin/` | PASS | — |
| `./gradlew :composeApp:desktopTest` | `kotlin/` | PASS | 54/54, 0 failures/errors |
| `./gradlew :androidApp:assembleDebug` | `kotlin/` | PASS | exercises `shared:compileAndroidMain`/`composeApp:compileAndroidMain` |
| Manual markdown link-resolution over the 7 edited docs | repo root | PASS | only the two pre-existing, out-of-scope `GlassBenchmarkActivity.kt` links unresolved |

Env used: `JAVA_HOME=$(/usr/libexec/java_home -v 21 -a arm64)` (arm64 Temurin 21.0.12.1), `ANDROID_HOME=$HOME/Library/Android/sdk`.

### Skipped
- **Rewiring `kmp-web`/`testing-tdd`/`playwright-testing` skill text (M18's "check ... skills" note):** read all three; none contains blocking parity-gate language that needs rewording (see M18 above). No edit made.
- **`androidApp:testDebugUnitTest` re-run as its own check:** covered indirectly by `:androidApp:assembleDebug`, which compiles the same `androidMain`/`commonMain` graph through `shared:compileAndroidMain`; not re-run separately in this pass since none of this batch's diffs touch Android-specific test sources — CI will exercise the actual task via the new `jvm` job.
- **Fixing the pre-existing dead `GlassBenchmarkActivity.kt` links in `GlassAndroidPerformance.md`:** not part of any named finding in this batch (m14 named the scripts only); left unchanged per minimal-diff instruction.

### Changed paths
- `.github/workflows/kotlin-check.yml`
- `.claude/skills/workflow/SKILL.md`
- `AGENTS.md`
- `Plans/Kotlin/PreGlassRollback.md`
- `kotlin/README.md`
- `kotlin/settings.gradle.kts`
- `kotlin/shared/build.gradle.kts`
- `Plans/Kotlin/GlassAndroidPerformance.md`, `Plans/Kotlin/GlassWebPerformance.md`, `Plans/Kotlin/GlassOpticsPilot.md`
- Deleted: `kotlin/composeApp/src/commonMain/kotlin/polski/ui/SpikeScreen.kt`, `kotlin/composeApp/src/androidMain/kotlin/polski/ui/SpikeControlsAndroid.kt`, `kotlin/composeApp/src/desktopMain/kotlin/polski/ui/SpikeControlsDesktop.kt`, `kotlin/composeApp/src/webMain/kotlin/polski/ui/SpikeHtmlControls.kt`, `kotlin/shared/src/commonMain/kotlin/polski/spike/SpikeSession.kt`, `kotlin/shared/src/commonTest/kotlin/polski/spike/SpikeSessionTest.kt`, `tests/browser/kotlin-spike.spec.ts`
- Renamed (git mv): `scripts/benchmark-android-glass.mjs`, `scripts/benchmark-android-glass-auto.mjs`, `scripts/benchmark-kotlin-glass.mjs`, `scripts/benchmark-glass-rollout.mjs` → `Plans/Kotlin/archive/scripts/`
- No other batch's files touched.

## Batch: course-inventory (M16)

### M16 — Course inventory stale after all Kotlin host changes; both npm inventory checks red
- **Files:** `courses/pl-ru/source-inventory.json`, `courses/pl-ru/inventory-decisions.json`, `courses/pl-ru/inventory-coverage.json`.
- **Fix:**
  1. `node scripts/inventory-course.mjs` — regenerated the file inventory. Net effect: 29 new production files added (all from the reintroduced per-host preferences/settings surface — `polski.preferences.*`, `IosPreferencesSession`, `Mac*Preferences*`/`Mac*Session*`/`Mac*Repository*`/`MacSnapshot`, `Desktop*Preferences*`, `MacSystemPreferences`, `WebAppearance`/`WebPreferencesRepository`/`WebPreferencesController`/`WebRouteController`/`WebSwipeRating`/`WebNav`/`SettingsWeb`, `PointerInterop.js/.wasm`, `AndroidUserPreferencesStore`, `AppNavigationBar`) and 5 files removed (the deleted `Spike*`/`SpikeSession` prototype files, matching the rollback already committed).
  2. Classified every new/changed literal-line candidate in `inventory-decisions.json` (753 candidates: 750 by rule, 3 by direct lookup — see below) and dropped 180 decisions that no longer match any candidate (stale `Spike*` entries plus lines whose text or symbol context changed in already-tracked files). Net decisions count: 2488 → 3061 (matches the checker's own candidate count).
  3. `node scripts/check-course-inventory.mjs` — regenerated `inventory-coverage.json` from the completed decisions.
  4. Re-ran both checks; both PASS.
- **Classification method (per `scripts/check-course-inventory.mjs:309`'s category enum, consistent with existing decisions):**
  - Cross-file mirroring: `MacSnapshot.kt`, `MacVocabularySession.kt`, `MacProgressRepository.kt`, `MacSession.kt`, `MacVocabularyRepository.kt` are near-duplicates of their already-classified `Ios*` counterparts (same literal text, same nesting, differing only in class name/package and the file-backed-vs-`NSUserDefaults` persistence details) — matched by `(normalized symbol, sourceFingerprint, occurrence)` and reused the sibling's exact category+reason (231 candidates covered this way).
  - Same-file mirroring: `AndroidTrainingScreen.kt`'s reformat made every literal line report `symbol=file-scope` (the regex-based Kotlin symbol scanner in `check-course-inventory.mjs` no longer recognized the composable boundaries), so its 41 "new" candidates and 41 "stale" decisions were matched by `(sourceFingerprint, occurrence)` alone and given the prior line's category+reason (39/41; the remaining 2 genuinely new lines classified by rule).
  - Rule-based classification for everything else, following the pattern already established across the codebase: a literal token with no Cyrillic character is `serialization-plumbing` (data/session/repository/codec files: "Persistence, scheduling, model, or platform identifier used for application behavior."; UI/screen/DOM files: "Host component wiring, CSS/DOM key, accessibility attribute, enum/skill identifier or course-pack field lookup.", or a file's own already-established variant of that sentence, e.g. `MainActivity.kt`/`Main.kt`). A token containing Cyrillic is either `diagnostic` ("Runtime validation, persistence, or import failure message; it is not Polish lesson content.") when the file is a data/session/repository class returning the string as an error/status value, or `ui-localization-chrome` (quoting the string, "is application interface text in `<File>.kt`; language-pair teaching copy and fixed examples are stored in course.json.", or a file's own established chrome reason for training controls / vocabulary catalog text / app-chrome actions) when the file is UI-presentation code. Three exact-text and one symbol-specific exception carried over from existing sibling decisions: `"Импортируйте совместимый JSON явно"`, `"Сессия закрыта"`, `"Дождитесь сохранения прогресса или экспортируйте JSON"` (all `ui-localization-chrome`, matching the identical strings already classified in `IosProgressRepository.kt`/`IosSession.kt`), and `AnswerNormalizerMac.kt`'s `"pl_PL"` locale identifier → `runtime-morphology` (matching `AnswerNormalizerAndroid.kt`'s identical `"pl-PL"` classification, not the file's own default).
  - No candidate was classified `pack-backed-authored` or `inactive-prototype`: none of the new preferences/settings text is sourced verbatim from `course.json`, and `inactive-prototype` remains restricted to the deleted `Spike*` files per `check-course-inventory.mjs`'s `inactivePrototypePaths` allow-list (unchanged, not weakened).
- **Checker not weakened:** no change to `scripts/check-course-inventory.mjs` or `scripts/inventory-course.mjs`; `inactivePrototypePaths` and the category enum are untouched.
- **RED/GREEN:** RED = `npm run course:inventory:check` and `npm run course:inventory:decisions:check` both failing at the start of this batch (stale file list; `TrainingWebApp.kt:251` unclassified). GREEN = both PASS after the fix (see Checks).

### Checks (this batch)
| Command | cwd | Result | Detail |
|---|---|---|---|
| `npm run course:validate` | repo root | PASS | course pack + vocabulary editorial journal |
| `npm run course:inventory:check` | repo root | PASS | 125 source files |
| `npm run course:inventory:decisions:check` | repo root | PASS | 3061 candidates, 5535 tokens (was: unclassified/stale mismatch) |

### Skipped
- None. All 753 new/changed candidates were classified; no item was left unclassified for review.

### Changed paths
- `courses/pl-ru/source-inventory.json`
- `courses/pl-ru/inventory-decisions.json`
- `courses/pl-ru/inventory-coverage.json`
- No source files touched (classification-only batch); no other batch's files touched.
