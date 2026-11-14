# iOS recent modes: independent test handoff

Date: 2026-11-14. Baseline: `81f8ce7` plus the current uncommitted implementation. Contract: [IosRecentModes.md](IosRecentModes.md), acceptance items 1–4. Tested on Xcode 27.2, iPad Simulator iOS 26.1 (`AD9779D0-EAAC-40F0-8D29-C293BD5D5912`). Physical-device evidence is owned by the main agent after this handoff.

## Added verification

- `kotlin/iosApp/PolskiGrammarUITests/PolskiGrammarUITests.swift`: `testClozeAnsweredQuestionAutomaticallyAdvancesOnce`, `testTileBuilderWrongChoiceShowsResultAndErrorRule`, and `testRecentModesReadOnlyDeviceSmoke`. The last case reads screens only: it does not submit answers, rate, reset, import, or change preferences and is suitable for selection on the physical iPad.
- `kotlin/apple-bridge/src/iosTest/kotlin/polski/ios/IosSessionTest.kt`: `tileBuilderCheckExposesWrongChoiceAndErrorExplanation`, added as a focused isolated-defaults test and completed/run by the developer. It exercises the public bridge rather than checking implementation spelling.

## Results

| Contract / gate | Result | Evidence |
| --- | --- | --- |
| AC1 Cloze bridge actions, stale/repeated choices, boundaries, no progress writes | **PASS**, reused developer's four focused Kotlin cases; bridge code for these behaviors was unchanged after that run | `/tmp/ios-cloze-focused.log` |
| AC2 Cloze native wrong answer, manual navigation, tab timer cancellation | **PASS**, reused developer's selected UI case; no change to that path afterward except loading retry | `/tmp/ios-cloze-ui-rerun.log` |
| AC2 Cloze automatic advance exactly once | **PASS**, tester case 1/1 on final Swift implementation | `/tmp/ios-recent-tester-ui-fixed.log` |
| AC2 early tab entry while loading | **PARTIAL**: read-only UI smoke opens Cloze immediately and sees a sentence/three options, but runner timing did not guarantee the tab was selected before `Ready` | `/tmp/ios-recent-tester-ui-fixed.log`; deterministic loading race not isolated |
| AC3 TileBuilder Settings availability and relaunch restore | **PASS**, reused developer's selected UI case, with unchanged preference path | `/tmp/ios-cloze-ui.log` |
| AC3 TileBuilder place/check/wrong result/error explanation | **PASS** after production fixes: bridge case 1/1 and failed-only iPad Simulator UI case 1/1, including screenshot attachment `tile-builder-error-and-rule-ipad` in Xcode result bundle | `/tmp/ios-tile-bridge-green.log`; `/tmp/ios-recent-tester-tile-final.log` |
| AC3 one rating increments reviews once | **PASS** at the iOS bridge: after TileBuilder Check, one `Good` raises `totalReviews` and `revision` by one; immediate duplicate leaves both unchanged. The native swipe gesture itself was not repeated in this selected run | `/tmp/ios-tile-rate-once-test.log`, 1/1 |
| AC4 iPad Simulator build and selected native UI tests | **PASS**; final failed-only UI runner rebuilt app/framework and exited 0 | `/tmp/ios-recent-tester-tile-final.log` |
| AC4 signed physical build, install, launch, safe UI smoke | **NOT RUN by tester**; main agent is running this gate after Xcode handoff | Pending main-agent evidence |
| VoiceOver spoken order, large Dynamic Type, live iPad resize | **NOT RUN**; simulator XCTest checked accessibility labels/identifiers and readable result text, but did not automate system accessibility modes or Stage Manager resize | Open acceptance evidence |

The first TileBuilder UI run reproduced no visible result after an enabled Check. A focused bridge test then showed `tileBuilder.result` serialized as JSON null even though the card phase was `Revealed`. `IosSnapshot.tileBuilderSnapshot` had a `put(...) ?: put(null)` expression: the second write overwrote the first. The developer replaced it with explicit branches. The focused bridge and failed-only UI rerun passed. The first UI rerun after changing the Check command still failed because the snapshot defect remained; the final UI run passed after both fixes. The failure was a product defect, not an assertion or UI-query mismatch.

The older `IosSessionTest` rating-effect class failures reported in [IosRecentModes-Developer.md](IosRecentModes-Developer.md) remain outside these modes; the full class was **FAIL** in the developer's isolated run. The selected TileBuilder rating test passed afterward and does not establish that the older effect cases pass. Repository-wide native tests, accessibility mode testing, and device-specific behavior are not implied by the selected passes. Tests added after implementation provide regression evidence, not an observed RED→GREEN sequence for the whole feature.

Working directory for `xcodebuild`: repository root. Runner command pattern: `xcodebuild -project kotlin/iosApp/PolskiGrammar.xcodeproj -scheme PolskiGrammar -configuration Debug -destination 'platform=iOS Simulator,id=AD9779D0-EAAC-40F0-8D29-C293BD5D5912' -derivedDataPath kotlin/iosApp/build -only-testing:PolskiGrammarUITests/PolskiGrammarUITests/<case> CODE_SIGNING_ALLOWED=NO test`. Final selected case: `testTileBuilderWrongChoiceShowsResultAndErrorRule`. No commit, release, or live progress reset was performed by the tester.

Working directory for focused bridge rating test: `kotlin/`. Command: `./gradlew :apple-bridge:iosSimulatorArm64Test --tests 'polski.ios.IosSessionTest.tileBuilderCheckExposesWrongChoiceAndErrorExplanation' --console=plain`. The isolated defaults suite was removed in `finally`; no physical-device progress was used.
