# Independent acceptance: native iPad grammar exam

Date: 2026-11-08. Accepted contract: `EXAM-DSL-LOGIC-CORRECTION-PLAN.md` (in the main checkout) and [IPAD-EXAM-GRAMMAR-EVIDENCE.md](IPAD-EXAM-GRAMMAR-EVIDENCE.md). Tested uncommitted P11 source in `/tmp/polski-p11-shared-worktree` at HEAD `08fc78a`. This is the first **grammar-only** native slice; it does not validate the other three parts or a full timed exam.

## Independent checks

| Criterion | Result | Evidence |
|---|---|---|
| iPad Simulator full B1/2017 grammar route and result | PASS | `xcodebuild ... test-without-building` (command below), `testOfficialB12017GrammarUsesSharedNativeExam`: 50-item route, first answer retained across tab navigation, submission and `Подтверждено, 0,5 / 30`. `/tmp/polski-p11-ipad-independent-ui.log`. |
| iPad Simulator typed Polish draft lifecycle | PASS | Same command, `testNativeExamTypedPolishDraftSurvivesTabReturn`: exact `żółć` remains after Matrix content mounts and Exam returns. Two selected cases passed, zero failures, `** TEST EXECUTE SUCCEEDED **`. |
| Bridge source/pending/result behavior | PASS | `kotlin/`: `./gradlew :apple-bridge:iosSimulatorArm64Test --tests polski.ios.IosExamSessionTest --console=plain --max-workers=2`; `/tmp/polski-p11-ipad-independent-bridge.log`, 6 cases, zero failures. The tester added one case for the B1/2017 `z gazety` question target, hiding the key before submit, then exposing `skąd` only in review. |
| Source and bridge contract audit | PASS | `IosExamSession` uses `GrammarExamState`/`reduceGrammarExam` and `GrammarExamScorer`; Swift has no answer table or scorer. Catalog reads six generated official packages and four source-backed module covers; only Grammar is enabled on iPad. Bank buttons carry token IDs, while display text and source position remain separate. `questionTarget` renders from the bridge snapshot; `review`/official examples are added only after result creation; pending manual quarters suppress final percentage. |
| Project generation route | PASS | `ruby -c kotlin/iosApp/generate_project.rb` reports `Syntax OK`; `NativeExamView.swift` is in the generator's Swift list and in the current Xcode project's file/source entries. No project regeneration was needed for this static routing check. |
| Physical iPad build/install | PASS (reused) | Existing developer logs: `/tmp/polski-ipad-exam-physical-build.log` and successful `devicectl install` recorded in the implementation evidence. |
| Physical iPad launch / exam interaction | DEFERRED BY OWNER | No launch or progress reset performed. The prior locked-device `RequestDenied` is recorded in the implementation evidence; owner explicitly deferred this check. |
| Physical VoiceOver / accessibility speech | NOT RUN | Simulator UI checks identifiers and selected controls; they do not establish spoken order or device VoiceOver behavior. |

Exact UI command from repository root:

```sh
xcodebuild -project kotlin/iosApp/PolskiGrammar.xcodeproj -scheme PolskiGrammar -configuration Debug -destination 'platform=iOS Simulator,id=AD9779D0-EAAC-40F0-8D29-C293BD5D5912' -derivedDataPath /tmp/polski-ipad-exam-device-derived CODE_SIGNING_ALLOWED=NO '-only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testOfficialB12017GrammarUsesSharedNativeExam' '-only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testNativeExamTypedPolishDraftSurvivesTabReturn' test-without-building
```

This reran the developer's exact built iPad Simulator artifact without invoking Gradle while the web build was active. Xcode 27.2; simulator `Polski-Card-Acceptance-iPad` (`AD9779D0-EAAC-40F0-8D29-C293BD5D5912`). The test bundle was built at 22:15:57 after the UI test source changed at 22:15:40, and the app binary at 22:16:51 after native Swift/bridge source changes. The developer's final two-case build/test log is `/tmp/polski-ipad-exam-ipad-two-case-final.log`. The independent rerun passed both cases. `git diff --check` passed. No independent product defect was found in this grammar slice.

The added test is in `kotlin/apple-bridge/src/iosTest/kotlin/polski/ios/IosExamSessionTest.kt`. Its literal expected target/key come from the authored B1/2017 source, not the snapshot being tested. Existing bridge grading comparisons use `GrammarExamScorer` and therefore establish native/shared parity; the separate DSL answer-policy oracle remains the independent authority for grading expectations.
