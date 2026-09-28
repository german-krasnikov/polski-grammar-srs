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
