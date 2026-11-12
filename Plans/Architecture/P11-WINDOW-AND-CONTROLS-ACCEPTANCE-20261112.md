# P11 native iPad window and source-control acceptance

Date: 2026-11-12. Contract: [IOS-FULL-EXAM-IMPLEMENTATION-PLAN.md](IOS-FULL-EXAM-IMPLEMENTATION-PLAN.md) §4. This is independent installed Simulator evidence, not physical iPad or source-review approval. It supplements [IOS-FULL-EXAM-INDEPENDENT-EVIDENCE.md](IOS-FULL-EXAM-INDEPENDENT-EVIDENCE.md).

## Target and artifact

- iPad Pro 11-inch (M4) Simulator, iPadOS 26.1, UUID `AD9779D0-EAAC-40F0-8D29-C293BD5D5912`; Xcode 27.2 (27B52).
- Published 16-paper app built and tested from worktree `p9-publication`, source HEAD `7978ec7901fa1324de5822e30925a627de8d3cb7`, with uncommitted test-only additions in `kotlin/iosApp/PolskiGrammarUITests/NativeExamUITests.swift`. App path: `kotlin/iosApp/build/Build/Products/Debug-iphonesimulator/PolskiGrammar.app`; executable SHA-256 `fe1b951d77a5f3eeaecc9887c591892cfc70595da76c2bdaea0c8aa4d1e44411`.
- After the last Xcode run, `npx tsx scripts/prepare-ios-exam-assets.ts check-app kotlin/iosApp/build/Build/Products/Debug-iphonesimulator/PolskiGrammar.app` **PASS**: 16 pinned audio files and seven published images byte-exact. `git diff --check` **PASS**.
- Logs, `.xcresult` bundles and exported screenshot are retained under `/Users/german/Work/JS/polski-grammar-srs-worktrees/recovery-logs/20261112/window-resize-followup/`. The p9 test diff is uncommitted; no production source was changed by the tester.

## Focused installed UI checks

All Xcode commands ran from `/Users/german/Work/JS/polski-grammar-srs-worktrees/p9-publication` with this command shape; each named log contains the fully expanded command on line 2:

```sh
xcodebuild -project kotlin/iosApp/PolskiGrammar.xcodeproj -scheme PolskiGrammar -configuration Debug -destination 'platform=iOS Simulator,id=AD9779D0-EAAC-40F0-8D29-C293BD5D5912' -derivedDataPath kotlin/iosApp/build -resultBundlePath <evidence-directory>/<name>.xcresult '-only-testing:PolskiGrammarUITests/NativeExamUITests/<method>()' CODE_SIGNING_ALLOWED=NO test
```

`BankIdentityRetry` used `test-without-building` against the immediately preceding built runner and app. The other final focused runs used `test`.

| Acceptance check | Result | Evidence |
|---|---|---|
| Genuine iPadOS Windowed Apps menu → Window → Move & Resize → Left while a Polish Listening draft exists; retain draft and hittable Submit; restore full width with Window Controls → Zoom | **PASS 1/1**. Window frame `(0,0,834,1210)` → `(0,0,412,1210)` → `(0,0,834,1210)` | `MenuLeftAcceptance16PaperVisual.log/.xcresult`; exported `MenuLeftAcceptance16PaperVisual-attachments/C9F7F2DB-D735-4BB5-AFF8-025F8A202044.png` |
| B1-2017 Listening task V matching: exact source choices A–F, first question's selected answer retained after back/next | **PASS 1/1** | `NativeControls16Paper.log/.xcresult` |
| B1-2019 Listening source image with exact authored Polish alt label | **PASS 1/1** | `NativeControls16Paper.log/.xcresult` |
| B1-2020 Grammar VIII: both printed `w` tokens have distinct source IDs; selecting token 7 leaves token 2 unselected and survives back/next | **PASS 1/1** | `BankIdentityRetry.log/.xcresult` |
| B2-2020 Reading fragment: source choices `A|B|D|E|F|G|H|I|J|K`, no `C`; first `E` selection survives back/next | **PASS 1/1** | `FragmentChoiceFinal.log/.xcresult` |

The first menu attempt already narrowed the real window to 412 points and passed draft/Submit assertions, but its restoration gesture started outside the left tile. The next attempt inherited that tile. Retained diagnostics identify the system `window-controls:dev.polski.grammarmatrix.ios` and `Zoom-button`; the final scenario restores the original width. An initial Grammar test sent 50 rapid Next taps and stopped at 49/55; the final case waits for the exact token ID. An initial Reading `E` tap overlapped the fixed Previous button at y≈1086–1107, because `E` was at y≈1090; the final case scrolls `E` above the fixed controls before tapping. These were test interaction defects, with no product defect established. A focused two-selector command selected zero tests and is excluded from pass counts. All failed/diagnostic logs remain in the evidence directory.

Physical iPad input, resize and VoiceOver: **NOT RUN**, deferred by the owner. Browser, Android and JVM checks: **NOT RUN** in this native UI follow-up. The screenshot supports the measured 412-point state; XCTest assertions establish the answer and Submit interaction. Independent code review remains a separate gate.

## Final review and integration

Independent Reviewer **APPROVED** the final 137-line test diff and installed evidence. All previously identified §4 gaps are covered; no production code changed. Root ported the exact test diff from the publication worktree onto the byte-identical main test baseline. P11 is closed under its accepted Simulator contract. The physical-device deferral and incomplete long XCTest remain explicit and are not converted to PASS. New P13–P19 product requirements are tracked separately in [the quality plan](EXAM-LEARNING-QUALITY-PLAN.md).
