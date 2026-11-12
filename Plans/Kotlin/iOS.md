# iOS native host — implementation lane

Date: 2026-09-23. User requested an iPhone app with a native interface. This lane advances before the still-open web, Android and iOS acceptance gates; no publication cutover is implied.

## Grounded contract

- `kotlin/shared` owns grammar, exercise generation, FSRS, `ProgressCodec` and now the UI-independent `polski.presentation` session. `kotlin/composeApp` remains the Web/Android/Desktop renderer. Move only presentation state, actions, effects and `TrainingStore` to `shared`; preserve package names and behavior for existing callers.
- Add `iosArm64` and `iosSimulatorArm64` framework targets to `shared`. `kotlin/iosApp` is a separate SwiftUI host; it consumes `PolskiShared` through a narrow bridge, not Compose UI.
- An iOS session owns one `TrainingStore`, `CoroutineScope`, state observer and app-private progress repository. The Swift model owns bridge lifetime, delivers actions, handles file import/export and alert effects. File IO must not overwrite unreadable progress. Import validates v1 JSON, backs up existing bytes, then reloads a fresh store; pending review writes must finish first.
- The Kotlin/Swift boundary sends immutable JSON snapshots and semantic commands to avoid exporting internal mutable Kotlin graphs or depending on generated sealed-class names. The JSON is a host presentation protocol, not the persisted progress schema. `ProgressCodec` remains the only durable schema authority.
- SwiftUI implements native training, matrix and progress navigation, safe-area/keyboard behavior, Dynamic Type and VoiceOver labels. Keep the unrevealed answer absent from accessibility output. Every card transition and review remains a shared `TrainingStore` action.

## Ordered work and acceptance

1. Move session to `shared`, add Native targets and NFC/Polish case adapter; compile and run existing web/desktop/Android consumers.
2. Add iOS repository and bridge with tests for load/save, malformed recovery, import backup, state/actions and effect acknowledgment. Compare shared fixtures on iOS simulator.
3. Add Xcode SwiftUI project and actual app UI including chain/schedule/focused modes, typed/oral answer, four ratings, matrix sections, stats, JSON transfer and reset.
4. Build/link/launch simulator, then launch on physical iPhone when connected and authorized by existing task. Check Polish keyboard, background/restart persistence, safe areas, landscape, large text, VoiceOver and Reduce Motion. Record each result independently.

The installed Xcode is 27.0; Kotlin 2.4.20's published compatibility table currently names Xcode 26.4. The local Xcode license was accepted. Debug Kotlin frameworks and the SwiftUI app now build with Xcode 27 on this Mac despite the compatibility table naming Xcode 26.4; this is observed local evidence, not an upstream support claim. Physical-device signing needs a working Apple Development account and provisioning profile.

## Implementation and verification on 2026-09-23

The native SwiftUI host is `kotlin/iosApp/PolskiGrammar/PolskiGrammarApp.swift`, generated Xcode project in `kotlin/iosApp/PolskiGrammar.xcodeproj`. `polski.presentation` moved from `composeApp/commonMain` to `shared/commonMain` without changing its package or public actions. `IosSession` owns one `TrainingStore`, the native clock and a main-dispatcher state observer; `IosSnapshot` exports a read-only JSON presentation snapshot. `IosProgressRepository` stores the React-compatible v1 document in app-private UserDefaults, validates before replacement, reads back writes, and keeps a named backup before explicit JSON import. SwiftUI owns file pickers, confirmation, tab navigation and scene refresh. Browser and Android data do not migrate automatically.

| Check | Result |
| --- | --- |
| `:shared:iosSimulatorArm64Test` | **PASS**: 145/145 Kotlin/Native tests, including NFC/Polish forms, grammar/FSRS/progress parity and the new native save/invalid-import/backup test. |
| `xcodebuild ... CODE_SIGNING_ALLOWED=NO test` on iPhone 17 Pro Simulator, iOS 26.0 | **PASS**: 2 SwiftUI UI tests. One opened the native card, confirmed the unrevealed answer was absent, revealed and rated it, opened matrix/progress, relaunched, and saw the saved review. The other entered a Polish answer in the native text field and checked the matching result. |
| iPhone 17 Pro Simulator app install/launch and visual screenshot | **PASS**: full-height safe-area layout after adding `UILaunchScreen`; [capture](artifacts/ios/iphone-17-pro.png). |
| iPad (A16) Simulator app install/launch and visual screenshot | **PASS**: native top tab layout and full-width training form; [capture](artifacts/ios/ipad-a16.png). |
| `xcodebuild ... -destination 'platform=iOS,id=00008101-00154D5218E1401E' CODE_SIGNING_ALLOWED=NO build` | **PASS**: ARM64 device framework and SwiftUI application compile/link. This artifact cannot be installed without signing. |
| Signed build, install and launch on connected iPad Air (4th generation), iPadOS 26.6.2 | **PASS**: Xcode automatic signing with the active Gmail Personal Team created a development identity and provisioning profile. `xcodebuild -allowProvisioningUpdates DEVELOPMENT_TEAM=57E7J6MPML build`, `devicectl device install app`, and `devicectl device process launch` succeeded. The final build with the app icon was installed and launched after unlocking the iPad. |
| `xcodebuild ... test` on the physical iPad | **PASS**: 2/2 UI tests on the final compact-hero/icon build, including card/review persistence and Polish text entry. The result bundle `Test-PolskiGrammar-2026.09.23_16-50-22-+0200.xcresult` reports 2 passed, 0 failed on iPad Air (4th generation), iPadOS 26.6.2. The app was relaunched after the tests. |
| `:shared:desktopTest`, `:composeApp:desktopTest` | **PASS** after presentation moved to `shared`. |
| JS/Wasm shared and composeApp browser tests plus `:androidApp:assembleDebug :androidApp:testDebugUnitTest` | **PASS** after the same move. |
| Physical iPhone, physical iPad VoiceOver, Polish soft keyboard, large text/landscape, Reduce Motion, complete P01–P11 target parity | **NOT RUN**. The focused iPad UI smoke does not close Stage 15/16 gates. |
| Independent Tester/Reviewer | **NOT RUN**: the agent runtime returned `agent thread limit reached`; main-agent checks and source review are not independent review. |

The two iPhone UI tests are focused smoke scenarios, not the full acceptance matrix. The web parity gate and Android Stage 13/14 acceptance remain open independently of this iOS implementation. See [kotlin/README.md](../../kotlin/README.md) for commands.

## UC-09 (part 2/2): Matrix screens read `MatrixTableEngine`/`MatrixTableViewModel` (2026-09-28)

`IosSnapshot.kt`'s `matrixSnapshot` now builds the cases, case-comparison, verb conjugation, tense, aspect, pronoun and possessive tables through `:core-engine`'s `MatrixTableEngine` and `:shared`'s `MatrixTableViewModel` (UniversalCorePlan.md §5.3.3/§12 UC-09, ADR-22) instead of re-deriving each cell by hand; the wire JSON `MatrixView` (SwiftUI) reads is unchanged, so no Swift file needed editing. `russianSupport`'s embedded comparisons, `systemCards` and `maleAccRows` stay outside `MatrixTableEngine`'s scope, per ADR-21.

| Check | Result |
| --- | --- |
| `:shared:iosSimulatorArm64Test` (full suite, incl. `IosMatrixSnapshotTest` 12/12) | **PASS** |
| `:shared:compileKotlinIosSimulatorArm64` / `compileKotlinIosArm64` | **PASS** |
| RED→GREEN: breaking one column's `contrastFrom` (NOM→GEN) failed `IosMatrixSnapshotTest`; revert restored green | **PASS** (confirmed live) |
| `xcodebuild test` on iPhone 17 Pro Simulator (iOS 26.0), 8 Matrix/system-map UI tests | **PASS**: `** TEST SUCCEEDED **`, 8/8, 0 failures |
| Simulator screenshots (added `XCTAttachment` capture to 3 tests) | **PASS**, see [artifacts/ios/uc09-matrix-cases-comparison.png](artifacts/ios/uc09-matrix-cases-comparison.png), [uc09-matrix-verbs-tense-aspect.png](artifacts/ios/uc09-matrix-verbs-tense-aspect.png), [uc09-matrix-pronouns-possessives.png](artifacts/ios/uc09-matrix-pronouns-possessives.png) |
| Android/Web/Desktop/macOS, physical device, VoiceOver/Dynamic Type on Matrix | **NOT RUN** — iOS-only host wiring, out of scope |

See [decisions.md ADR-22](../../AI/decisions.md) for the full contract.

## Native visual system and multilingual icon

SwiftUI keeps the platform tab bar, Form controls, safe areas, Dynamic Type text styles and system grouped background. The training screen adds a compact study summary and a clearer sentence/action hierarchy; the iPad keeps its platform tab placement. The icon from [branding](../../assets/branding/README.md) has no language-specific letter or flag. After this visual change, both iPhone 17 Pro Simulator and physical iPad UI tests passed again, and the iOS asset catalog compiled with `AppIcon`; the iPhone and iPad simulator captures above show the compact layout. The native text field now owns a local draft during editing and sends each change to the shared session: the UI test observed a dropped Polish letter before this fix and passed afterward.

## EN-24 (iOS): English matrix table on `MatrixView`, `enVerbForm` shared with web (2026-09-28)

`IosSnapshot.kt`'s `matrixSnapshot` adds `englishExampleLemma`/`englishVerbsRows`/`englishDoSupportRows` next to the existing pl `verbsRows`/`tenseRows` — the one live English table (`EnRuPackPlan.md` §5 gap H / §6, the same minimum slice ADR-28 already shipped on web), built through the same `MatrixTableEngine`/`MatrixTableViewModel` every pl table above uses, fixed to one example verb ("see") and read from `lang/en/forms.generated.json`. `enVerbForm` (pronoun → person/number → `enMorphology.form`) moved from a webMain-only private helper into `:shared` commonMain (`EnLexicon.kt`) so both hosts call the one implementation instead of keeping a second hand-copied map. `MatrixView.swift` renders a new `english` section with its own Russian tense labels, deliberately not `matrix.record("verbTenseLabels")` (that map holds pl glosses like "Teraz", which would mislabel an English column — the same class of bug ADR-28 fixed for `lang="pl"` on web).

| Check | Result |
| --- | --- |
| `:shared:iosSimulatorArm64Test` (incl. new `IosMatrixSnapshotTest` test, RED→GREEN confirmed live) | **PASS** |
| `:shared:desktopTest` / `:shared:macosArm64Test` | **PASS** (pl regression check) |
| `:composeApp:compileKotlinJs` / `compileKotlinWasmJs` / `:composeApp:desktopTest` (after moving `enVerbForm` out of `MatrixWeb.kt`) | **PASS** |
| `xcodebuild test` on iPhone 17 Pro Simulator, `testEnglishMatrixTableShowsRealFormsAndDoSupportSplit` | **PASS**: `** TEST SUCCEEDED **`, real `he→sees/saw/will see`, `do/does` split, invariant `did`, no future do-support |
| Same simulator, 3 adjacent existing Matrix UI tests (verb gender, cases, pronouns) | **PASS**: `** TEST SUCCEEDED **`, 0 failures — no regression on the pl tables sharing this screen |
| Screenshots | [en24-matrix-english-verbs.png](artifacts/ios/en24-matrix-english-verbs.png), [en24-matrix-do-support.png](artifacts/ios/en24-matrix-do-support.png) |
| Android / web / desktop / macOS hosts | **NOT RUN** — iOS-only lane per the plan's task table; still open per-host work, not silent |

See [decisions.md ADR-34](../../AI/decisions.md) for the full contract.

## EnRuAcceptance-2026-09-28 §7 item 2 (iOS lane): target/native pickers genuinely offer en-ru; the shared self-correction is now visible

ADR-36 already made en-ru parse completely (`usableCourseSelections`), and ADR-37/38 already made
`IosSession`/`IosPreferencesSession` degrade softly and self-correct when a switch to it can't
actually build a working exercise engine (`courses/lang/en/forms.generated.json` has no
noun/adjective/possessive forms yet — a separate, out-of-lane content gap, unchanged here). What
was still missing on this host: `AppModel.receivePreferences` silently dropped the one-shot
`packSwitchWarning` field the shared bridge already produced, so the self-correction happened but
nothing on screen ever told the user. `PolskiGrammarApp.swift`'s `receivePreferences` now surfaces
it through the same `notice` alert every other Settings error already uses. The stale
`testCoursePickersListOnlyUsablePacks` (asserted "Английский" must NOT be offered, written before
ADR-36) is replaced by `testSelectingEnglishTargetIsOfferedAndSelfCorrectsWithAVisibleNoticeOnRelaunch`,
which proves the whole host-visible contract live: the picker offers English, picking it is
accepted, and on relaunch the app is never silently stuck — Polish keeps training and an alert
names exactly why the switch didn't stick. No host-side hardcoded `"pl"`/`PackEngine("pl")` was
found in `kotlin/iosApp` or `iosMain` (the wiring to `packRegistry.active` was already done pre-existing).

| Check | Result |
| --- | --- |
| RED: unmodified `testCoursePickersListOnlyUsablePacks` on iPhone 17 Pro Simulator | **FAIL** (expected, proves the stale assertion): `XCTAssertFalse failed - en-ru must not be offered while it is unusable` — "Английский" now exists |
| GREEN: `testSelectingEnglishTargetIsOfferedAndSelfCorrectsWithAVisibleNoticeOnRelaunch` | **PASS**: `** TEST SUCCEEDED **`, 29.4s |
| Regression: `testNativeAppearanceSettingsKeepsTrainingCard`, `testAnimationsToggleDefaultsOnAndPersistsOffAcrossRelaunch`, `testAnswerModeChosenInSettingsSurvivesAppRestart` | **PASS**: `** TEST SUCCEEDED **`, 0 failures |
| Screenshots | [en22-target-picker-offers-english.png](artifacts/ios/en22-target-picker-offers-english.png), [en22-pack-switch-warning-self-correction.png](artifacts/ios/en22-pack-switch-warning-self-correction.png) |
| `:shared` Kotlin sources | unchanged — this is a Swift-host-only fix (`PolskiGrammarApp.swift` + the UI test) |
| Android / web / desktop / macOS hosts | **NOT RUN** — iOS-only lane; each host still needs its own `packSwitchWarning` wiring |

**Not fixed here, by design (out of this lane's scope):** `courses/lang/en/forms.generated.json` has
no noun/adjective/possessive forms (only verbs, from `scripts/build-pack-en.mjs`), so selecting
en-ru is accepted by every host's picker but never actually builds a working training session yet —
this is the still-open content gap ADR-37 documented. Until it closes, "select English and train
all 16 skills" stays unreachable on every host, iOS included; what this change closes is only the
picker's own honesty and the self-correction's visibility.

**Superseded by the correction below**: ADR-39 (2026-09-28, core lane) closed the content gap this
section calls "still-open" — `forms.generated.json` now materializes every category, and en-ru
really trains. `testSelectingEnglishTargetIsOfferedAndSelfCorrectsWithAVisibleNoticeOnRelaunch`
(GREEN above) was renamed/rewritten in the correction below because it turned out to still pass
after ADR-39 for a wrong reason — see ADR-43.

## EnRuAcceptance-2026-09-28 §7 item 2 (iOS lane, correction after ADR-39): false-green fix — en-ru now really stays active across a relaunch

ADR-39 (core lane) closed the content gap the section above called out (`forms.generated.json` now
has every category `ConstructionRealizer` needs), so `PlExerciseEngine` genuinely builds an en-ru
session — the same class that already works live on Android. But
`testSelectingEnglishTargetIsOfferedAndSelfCorrectsWithAVisibleNoticeOnRelaunch` kept passing
unchanged after that fix landed, still asserting the *old* rollback contract (self-correction alert,
reverted picker). Live diagnosis (temporary `NSLog` in `IosSession.rebuildIfCourseSwitched`,
`xcrun simctl spawn … log stream` on `4384946F-9E6B-43D0-ADA3-CA219A3456B8`) found the true cause: it
was never that rebuild — `activeCoursePackId` stayed `pl-ru` the entire relaunch, so no en-ru rebuild
was ever attempted. The real bug was `PolskiGrammarApp.swift`'s `AppModel.init()` assigning
`preferencesSession.onState` (whose setter eagerly reconciles the persisted target/native against
the *still-default* active pack) before calling `preferencesSession.reapplySavedCoursePack()` — the
only call that actually switches the pack. Every cold start with a persisted `target=en` therefore
self-corrected it back to `pl-ru` before ever trying to apply it. Full root cause and fix: ADR-43.

| Check | Result |
| --- | --- |
| RED (diagnostic): temporary `NSLog` in `rebuildIfCourseSwitched`, old init order | confirms `activeCoursePackId` logs `pl-ru`/`pl-ru` for every check across the relaunch — en-ru rebuild never attempted |
| GREEN: `testSelectingEnglishTargetSticksAcrossRelaunchAndServesRealEnglishContent` (renamed/rewritten, positive contract) on iPhone 17 Pro Simulator | **PASS**: `** TEST SUCCEEDED **`, 35.5s — no `Сообщение` alert, old Polish sentence absent, real Latin-only card text found, `settingsTargetPicker` shows "Английский" after relaunch |
| `:shared:iosSimulatorArm64Test` (full suite) | **PASS**: 400/400, unchanged (`:shared` production code not touched — see ADR-43) |
| Regression (`xcodebuild test`, same simulator): `testAnimationsToggleDefaultsOnAndPersistsOffAcrossRelaunch`, `testAnswerModeChosenInSettingsSurvivesAppRestart`, `testNativeAppearanceSettingsKeepsTrainingCard`, `testSaveFailureShowsErrorBannerOnEveryTabWithExportReachable`, `testNativeChainCompletionShowsFiveAnswersAndKeepsFiveRatings`, `testNativeTrainingMatrixAndProgress` | see `Plans/Kotlin/Lane-ios.md` for the exact per-test result of this run |
| `:shared` Kotlin sources | KDoc-only change (`IosPreferencesSession.reapplySavedCoursePack`) |
| Android / web / desktop / macOS hosts | **NOT RUN** — iOS-only lane; `MacPreferencesSession` never had this hazard (applies the saved pack synchronously in its own `init`, not a separately-ordered host call) |

See [decisions.md ADR-43](../../AI/decisions.md) for the full contract.

### Physical iPad launch — 2026-11-08

Revision `7135717`: signed Debug build **PASS** with existing team `57E7J6MPML`, installed and launched on connected physical iPad Air 4 (`00008101-00154D5218E1401E`); `devicectl` confirmed the installed application process. Existing progress was not cleared. This is a build/install/launch check, not a UI acceptance run. At this revision native iOS had no exam screen; owner highlighted that gap. Safari was launched with the deployed web URL, but physical-device Safari rendering/media was **NOT VERIFIED**. Shared DSL exam logic and native exam consumption require the corrected architecture plan; launch does not close those gates.

## 2026-11-12 — P11 published native exams on iPad Simulator

The SwiftUI **Экзамен** tab now offers all four parts for the six published papers. `IosExamSession` projects source instructions, questions, passages, writing sets, scores, and timed state from the shared Kotlin catalog/reducers. Native `AVAudioPlayer` owns offline listening segment playback; `scripts/prepare-ios-exam-assets.ts` stages only published media through Biome's public `read` command, checks pinned MP3 SHA-256 values, and verifies the exact bundle file set. Native audio and image control phrases are read from the selected course pack's `UiCopy` rows. The November 2022 C1 source remains unpublished and is absent from this catalog and bundle.

| Check | Result |
| --- | --- |
| `npx tsx scripts/prepare-ios-exam-assets.ts check-app kotlin/iosApp/build/Build/Products/Debug-iphonesimulator/PolskiGrammar.app` (repository root) | **PASS** — six pinned MP3s and four published images are byte exact in the built `.app` after Xcode's resource phase |
| `./gradlew :apple-bridge:iosSimulatorArm64Test :apple-bridge:linkDebugFrameworkIosSimulatorArm64 --console=plain` (`kotlin/`) | **PASS** — including 10/10 `IosExamSessionTest`; framework link succeeded |
| `xcodebuild ... -destination 'platform=iOS Simulator,id=AD9779D0-EAAC-40F0-8D29-C293BD5D5912' ... build` (repository root) | **PASS** — iPad Simulator app build |
| `xcodebuild ... -only-testing:PolskiGrammarUITests/NativeExamUITests -only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testOfficialB12017GrammarUsesSharedNativeExam -only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testNativeExamTypedPolishDraftSurvivesTabReturn ... test` (same iPad Simulator) | **PASS** — 7/7: offline MP3 first segment advanced beyond 40 seconds and replay control was exercised; published image loaded; free set's second Polish slot survived tab return and yielded pending manual review; reading/writing accepted native input; timed flow advanced from listening to reading; both retained Grammar scenarios passed |
| Physical iPad and signed distribution | **NOT RUN** — owner-deferred device gate; simulator result does not prove physical device behavior |

The previous Grammar-only availability assertion was updated to require all four module buttons. The first native focused run observed one failure at that obsolete assertion; the rerun above passed. Two added Simulator checks exposed long-task navigation taps being lost when controls were at the bottom of the scroll view; placing them in a safe-area bar made both pass. The new bridge tests were compiled and run after implementation, so they provide regression evidence, not a claimed test-first RED for the new parts. Build/test logs and the final `.xcresult` are preserved under `/Users/german/Work/JS/polski-grammar-srs-worktrees/recovery-logs/20261112/`.

### 2026-11-12 follow-up: seek and source rubric review

The listening player now shows a single accessible segment seek slider with elapsed and duration text. Seeking uses the source task's absolute segment start/end, clamps to that segment, and updates playback position without altering shared answer or score state. Replay returns to the segment start. When the scene becomes active, the host immediately forwards current wall time to the shared timed session; inactive scenes pause audio at its current position. `UiCopy` supplies the new Russian/English media labels.

The submitted Grammar review JSON now projects source `GrammarRubricComponent` rows (criterion, order, and quarter-point weight) without making an automatic award. SwiftUI renders those rows under the existing manual-review copy. The six published papers have no such rows; nonempty November 2022 C1 rubric rendering in an installed app is **NOT RUN** until that paper is published.

| Check | Result |
| --- | --- |
| Independent seek XCTest RED on the prior installed app | **FAIL as expected** — no accessible Slider; `IndependentSeekRedExecuted.xcresult` |
| Independent seek XCTest GREEN on iPad Simulator | **PASS** — B1-2017 first segment shows 4:14, seek to 80% shows 3:xx, Replay returns 0:xx |
| Independent >40-second Slider playback and free-set tab-return/manual-review checks | **PASS** — the corrected free-set test waited for native tab selection; the first attempt tapped Exam before Matrix navigation completed |
| New focused `IosExamSessionTest.submittedReviewProjectsFourSourceRubricCriteriaInOrderWithoutAwardingThem` | **RED then GREEN** — before: `rubricComponents` absent; after: four source-style criteria ordered 1–4, each formatted 0,25, no award field |
| `./gradlew :apple-bridge:iosSimulatorArm64Test :apple-bridge:linkDebugFrameworkIosSimulatorArm64` (`kotlin/`) | **PASS** — 11/11 exam bridge tests, framework linked |
| Xcode iPad Simulator build and `prepare-ios-exam-assets.ts check-app` | **PASS** — app compiled; six MP3s and four images remained byte exact in bundle |

### 2026-11-12 independent reviewer corrections

Swift passes the rendered part's origin to the bridge for answers, navigation, and submission. The shared timed reducer receives that origin, so a queued event from a previous part cannot act on the new part. The bridge reports unknown source item and invalid writing set IDs while keeping the attempt intact. Writing's placeholder maps to a null set; selecting the original set restores its draft. Swift caps option selections at the source slot count, clears free-set slot drafts when a submitted attempt is left or retried, and pauses listening on tab/background changes while retaining position. Xcode verifies staged audio hashes and the published asset set before Copy Bundle Resources, then restores exact bytes after Xcode's resource processing.

| Check | Result |
| --- | --- |
| Independent reviewer UI tests against prior build | **RED** — 4/4 behavioral failures: third choice selected, old retry slot shown, audio position lost, writing placeholder retained editor |
| Independent staged MP3 tamper build against prior project | **RED** — Xcode incorrectly succeeded; `check-app` then detected corrupted bundled audio |
| `./gradlew :apple-bridge:iosSimulatorArm64Test --tests 'polski.ios.IosExamSessionTest'` (`kotlin/`) | **PASS** — 15/15 including stale timed origin and independent/timed nullable writing set boundaries |
| Xcode iPad Simulator build with manifest check; `prepare-ios-exam-assets.ts check-app` | **PASS** — six pinned MP3s and four published images in app |
| Independent four UI corrections on installed iPad Simulator | **PASS** — each case 1/1; `IndependentChoiceGreen`, `IndependentRetryGreen`, `IndependentAudioLifecycleGreen`, `IndependentWritingSetGreen` xcresults |
| Final corrected app native UI regression | **PASS** — entire `NativeExamUITests` class 10/10; retained official B1-2017 Grammar and typed Polish draft cases 1/1 each, with result bundles under `recovery-logs/20261112/p11-independent-native/` |
| Independent staged MP3 tamper against corrected project | **PASS** — Xcode exits 65 at staged audio mismatch before resource copy; stage restored and `check-app` passes |
| Physical iPad | **NOT RUN** — owner-deferred gate |

### 2026-11-12: published sixteen-paper cohort

The installed iPad Simulator app now includes 16 source-exact original MP3s and seven published source images. Independent `IosExamSessionTest` is **PASS 17/17**; published C1 April 2024 paired controls and November 2022 quarter-point rubric render correctly, and both retained B1-2017 Grammar/Polish draft cases pass. Rubric accessibility changed with an observed installed RED → GREEN; independent source review APPROVED. [Native publication evidence](../Architecture/P9-PUBLICATION-NATIVE-INDEPENDENT-EVIDENCE.md).

Full four-part timed completion and extra-large text/landscape cases pass. A natural 40-minute Listening deadline was observed advancing to Reading; its interrupted long XCTest is NOT COMPLETED. Genuine window resizing remains NOT VERIFIED: system window gestures did not narrow the measured frame. The acceptance test remains preserved on `work/exam-dsl-native-pilot-20261108` at `a332681`. [Deadline and resize evidence](../Architecture/IOS-FULL-EXAM-INDEPENDENT-EVIDENCE.md). P11 remains open; physical iPad remains owner-deferred.

### 2026-11-12: P11 Simulator acceptance closed

The published sixteen-paper app passed the remaining five focused installed cases: genuine iPadOS Left/Zoom resize 834→412→834 pt while retaining the Polish answer and Submit, source matching, Reading fragments, duplicate word-bank IDs and exact image alt. Independent Reviewer APPROVED. [Full evidence](../Architecture/P11-WINDOW-AND-CONTROLS-ACCEPTANCE-20261112.md). Earlier failed harness attempts and incomplete natural-deadline XCTest remain recorded separately; physical iPad is still owner-deferred. P13–P19 feedback/content improvements are new scope rather than evidence of this older contract.
