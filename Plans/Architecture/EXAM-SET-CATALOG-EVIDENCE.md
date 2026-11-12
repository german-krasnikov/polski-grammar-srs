# Set policy and published catalog: integration evidence

2026-11-12. Worktree: `polski-grammar-srs-worktrees/p11`, based on `c74f0d4`. Contract: [Set and catalog plan](EXAM-DSL-SET-AND-CATALOG-PLAN.md). This is a work-in-progress record, not closure of P11 or activation of P9.

## Current implementation

Set grading priority is authored in `dsl/exam/set-policy.logic`; typed outcome rows select public status and confirmed/pending credit sources. Source fact identity, aliases, required selection count and explicit conservative-overselection metadata are separate from printed wording. Shared Kotlin resolves normalized facts and quarter arithmetic. [Independent acceptance evidence](SET-POLICY-INDEPENDENT-EVIDENCE.md) records the observed metadata-default and malformed-slot regressions and subsequent checks.

`dsl/exam-published.core-data` declares the six existing routes, titles and canonical source directories. Original source checks precede deterministic aggregation with line provenance, then Biome prints one published typed row package. The pipeline uses the documented Biome data reader. It accepts both existing `modules.core-data` and draft `exam-modules.core-data` filenames by record discovery; inactive directories are not published automatically. No P9 paper is activated.

Developer measurement before review corrections: 1,143 published rows of 18 record types; generated Kotlin output 504,790 bytes; aggregates plus provenance approximately 1.7 MB in 36 files; full generation approximately 75 seconds in this worktree. These measurements are preliminary. A seventh sitting needs canonical source rows, verified media metadata and one publication row, rather than per-date Kotlin imports. Original six per-paper generated packages are retained for compatibility checks.

## Observed checks

| Check | Result and limit |
| --- | --- |
| Developer source/generation checks | PASS: generation, focused catalog Vitest 4/4, TypeScript typecheck, desktop tests and JS compilation before final review corrections. |
| Independent Set runtime | JVM, JS and Wasm focused runtime PASS after the absent-metadata correction. An additional malformed-slot case reproduced a second behavioral RED; the final independent rerun passed on all three targets: 4 methods each, 22 API cases, 17 frozen legacy cases and 19 Logic examples. |
| Root generation check | PASS, `biome generate --check --project dsl/biome.project`, 2026-11-12. The developer subsequently reran full generation and `--check` successfully after the 22-case fixture and catalog review corrections. Log: sibling `recovery-logs/p11-biome-check-20261112.log`. |
| Apple bridge and Swift compilation | Developer PASS: actual Xcode Simulator app build after the source display-title change. Root current `:apple-bridge:iosSimulatorArm64Test` PASS after the shared adapter move: 51 tests, 0 failures/skips, including 6 exam bridge tests. |
| Installed iPad Simulator UI | Root PASS: 2 selected XCTest cases, 0 failures, 68.422 seconds. Official B1/2017 grammar reaches the result; Polish `żółć`, source context and instruction survive a tab round trip. This exercises the current grammar pilot, not native Reading/Listening/Writing. |
| Independent catalog acceptance | Initial run without `BIOME_BIN` skipped four tests and is not evidence. Correctly configured run: 3 PASS, source-value parity case timed out at 45 seconds during concurrent full generation. A subsequent developer run recorded 4 PASS / 1 TIMEOUT during the distribution build. The test now uses the documented in-process Biome reader instead of 17 extra CLI reads, with the original 45-second timeout and assertions retained; a build-idle rerun remains required. |
| Current combined browser acceptance | NOT RUN after the latest Set/catalog changes. Earlier browser counts are historical. |
| Physical iPad | Deferred by owner; not retried. |

Simulator command from the worktree root:

```sh
xcodebuild -project kotlin/iosApp/PolskiGrammar.xcodeproj \
  -scheme PolskiGrammar -configuration Debug \
  -destination 'platform=iOS Simulator,id=DCBFB4BA-9CC0-444E-88BA-9217EFE4DA8B' \
  -derivedDataPath kotlin/build/ios-catalog-derived \
  -only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testOfficialB12017GrammarUsesSharedNativeExam \
  -only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testNativeExamTypedPolishDraftSurvivesTabReturn \
  CODE_SIGNING_ALLOWED=NO test
```

Result bundle: `kotlin/build/ios-catalog-derived/Logs/Test/Test-PolskiGrammar-2026.11.12_18-35-43-+0100.xcresult`. Build and UI logs are retained outside the checkout under sibling `recovery-logs/p11-native-exam-ui-20261112.log`.

## Independent review and remaining work

Initial review: **REJECTED**, three Major findings. (1) An extra empty raw slot in `A||B` could collapse into a full answer for two required selections; preserve valid partial `A|`. (2) Reading/Listening/Writing typed-row projections still lived in webMain instead of shared adapters. (3) Publication checked module tags but did not reject invalid order, duration or maxima before host catalog construction. All three corrections were independently rereviewed: **APPROVED**, no remaining Critical/Major finding in this scope. Reading/Listening/Writing projections are now shared; module cover failures report original field spans. Final browser and catalog test integration gates remain open.

Result visibility, part-action permissions and timed-session decisions still require their own DSL policies. Native Reading/Listening/Writing screens and media delivery remain P11 work. Documentation coverage: `kotlin/README.md` updated for catalog, Set policy and pinned audio archive delivery; accepted architecture plans retained; original source metadata formatting preserves parsed values. Current compatibility distribution and Apple bridge tests PASS (4m 54s, 147 actionable Gradle tasks); JS, Wasm and combined audio verification each checked all six pinned assets. Browser acceptance and the build-idle catalog Vitest rerun remain required.
