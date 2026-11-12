# Set policy independent acceptance evidence

Date: 2026-11-12. Worktree: `polski-grammar-srs-worktrees/p11`, base `c74f0d4` with uncommitted recovered Set DSL and concurrent catalog edits. Contract: [EXAM-DSL-SET-AND-CATALOG-PLAN.md](EXAM-DSL-SET-AND-CATALOG-PLAN.md), section 1. This note records independent observations; developer RED/GREEN evidence remains separate.

## Contract and fixture inspection

| Check | Result | Evidence |
|---|---|---|
| Grading decisions are authored in Logic | PASS, static inspection | `dsl/exam/set-policy.logic` contains the single ordered `disposition` chain and `dsl/exam/set-policy.examples` has 19 authored cases. `dsl/exam/set-policy-outcomes.core-data` contains five typed disposition-to-status/credit-source rows. Kotlin `evaluateSetPolicy` calls generated `Policy.disposition` and reads generated outcome rows. |
| Fact identity and model examples | PASS, static inspection of implementation and fixture generation | The original `dsl/exam-scoring/set-facts.core-data` had 18 API cases, 50 fact entries and 2 aliases. The tester added four metadata-boundary cases and 12 facts (now 22/62/2). The Kotlin resolver normalizes wordings, rejects ambiguous aliases and invalid weights/slots, deduplicates known fact IDs, and sums quarter credit by ID. The commonTest harness consumes authored expectations. |
| Frozen legacy path | PASS, static inspection; existing developer desktop result | `evaluateUnorderedSetAnswer` retains its signature and routes mechanically derived facts into the DSL policy. `ExamAnswerEvaluationTest.unorderedSetOracleCases` consumes frozen generated rows; developer reported focused desktop PASS. |
| Reading/Listening integration | PASS, static inspection; existing developer desktop result | Both models use explicit `requiredSelections` for slot count and call the same resolver from their scorers. The API fixture harness exercises one case through both paths. |
| P9 official-paper semantics | NOT RUN | The B2/C1 cases are labeled model cases. P9 drafts are unpublished; no official-key confirmation is claimed here. |

## Defect: absent metadata enables conservative review

The accepted plan requires `usesSourceConservativeOverselection` only for new source rows with **explicit** metadata. Initial defaults in `ExamSetAnswerSpec`, `ListeningQuestion`, and `ReadingQuestion` were `ManualReview`; both web row mappers also mapped a missing `setOverselectionMode` to `ManualReview`. The tester added `metadata-absent-over-limit` and `metadata-explicit-manual-over-limit` to `dsl/exam-scoring/set-facts.core-data`, with expectations and quarter values in DSL. For the absent case, a fact-backed row with two required selections, three accepted facts of two quarters each, and raw answer `A|B|C` must be `wrong`, confirmed 0, pending 0. The explicit `manual_review` case expects `pending_manual`, confirmed 0, pending 4. No current published paper has accepted-fact rows, so this is a first-activation defect. The tester made no production change.

**Observed behavioral RED:** `./gradlew :shared:desktopTest --tests 'polski.exam.setpolicy.ExamFactSetEvaluationTest' --console=plain` from `p11/kotlin` compiled and ran one selected test. `listening/metadata-absent-over-limit` expected `Wrong,0,0`; actual `PendingManual,0,4`. This was a behavior failure, not a compiler or runner failure. Subsequent green runs replaced the test XML; the RED output was sent to the main agent at the time.

**Correction verified:** After the developer changed absent mode to nullable through the spec, Reading/Listening models and web mappers, the same focused test passed on Desktop JVM, JS ChromeHeadless and Wasm ChromeHeadless. Each fresh XML recorded one selected test and zero failures. `:composeApp:compileKotlinJs` also passed after the web mapper change.

## Defect: interior empty selection collapses to full credit

The tester added `metadata-absent-empty-segment` and `metadata-explicit-manual-empty-segment` with raw `A||B` and a source pool `A,B,C`, two required selections, two quarters each. Empty slots at the end of a partially filled UI answer retain the authored `b2-unused-empty-slot` expectation. An interior empty segment between two nonblank selections is malformed raw input. The DSL expects absent metadata to yield `wrong`, confirmed 0, pending 0, and explicit manual metadata to yield `pending_manual`, confirmed 0, pending 4. Initial resolver filtering removed the empty middle segment before the policy facts were built.

**Observed behavioral RED:** After targeted `biome data generate` checked and printed 22 case rows, 62 fact rows and two aliases, the focused Desktop test ran one selected test and failed `listening/metadata-absent-empty-segment`: expected `Wrong,0,0`; actual `ConfirmedFull,4,0`. The failure output was sent to the main agent. The developer corrected malformed-segment detection while preserving the trailing empty slot case.

**Final focused regression:** From `p11/kotlin`, `./gradlew :shared:{desktopTest,jsBrowserTest,wasmJsBrowserTest}` was run as three separate commands, each with `--tests` filters for `ExamFactSetEvaluationTest`, `ExamAnswerEvaluationTest` and `ExamSetPolicyTest`. All three targets passed. Fresh XML files under `kotlin/shared/build/test-results/{desktopTest,jsBrowserTest,wasmJsBrowserTest}/` record four selected test methods per target, zero skipped and zero failures. This covers all 22 authored API fact cases, the 17 frozen legacy unordered-set cases, and the 19 Logic examples through the generated policy. The browser tests ran in ChromeHeadless; they are domain runtime evidence, not web UI interaction acceptance.

## Browser runner diagnosis

Generated JS and Wasm Karma configurations both launch `ChromeHeadless`, use webpack preprocessing, and set Mocha timeout to 10 seconds. Their reporters are `karma-kotlin-reporter`, with `failOnEmptyTestSuite: false`; an empty run is not acceptance evidence. Before the run, no process listened on ports 9876 or 9222. The previously observed orphaned Chrome with a Karma temporary profile was owned by this worktree and was removed by the main agent; that observation alone does not establish timeout cause.

**Controlled JS runtime:** `./gradlew :shared:jsBrowserTest --tests 'polski.exam.setpolicy.ExamFactSetEvaluationTest' --info --console=plain` from `p11/kotlin` launched Karma 6.4.4 on port 9876, connected ChromeHeadless 157.0.0.0, ran exactly one selected test, and completed in 29 seconds. It failed on the same expected grading RED, with no runner timeout. XML: `kotlin/shared/build/test-results/jsBrowserTest/TEST-jsBrowserTest.polski.exam.setpolicy.ExamFactSetEvaluationTest.xml`. The prior stall was not reproduced; its cause remains unproven. No harness/deadline change was needed.

## Commands and target status

`node /Users/german/Work/Python/biome-dsl-core/implementations/typescript/packages/biome/src/bin.mjs fmt --check dsl/exam-scoring/set-facts.core-data dsl/exam-scoring/set-fact-entries.core-data` from `p11`: PASS, both files canonical. Full `biome generate --project dsl/biome.project` from `p11`: PASS with stale toolchain pin warning for the earlier 20/56/2 fixture. Targeted `biome data generate` with `dsl/set-facts.data-contract` and all three fixture files: PASS, 22/62/2 and zero data problems. Root also reports a full `generate --check` PASS before the final two fixture rows; final full-project check is outstanding.

Developer-reported checks reused as existing evidence, not an independent rerun: Logic `check` and `examples`, focused desktop session/source tests, and Wasm compilation PASS before the new fixture rows. Independent final focused Desktop, JS and Wasm runtime: PASS after both corrections. JS web adapter compilation: PASS, `./gradlew :composeApp:compileKotlinJs --console=plain` from `p11/kotlin`. Root reports iOS Simulator native UI test run two of two PASS; this tester did not run or inspect that artifact. Physical iPad: deferred by owner. Real web UI Playwright interaction: NOT RUN in this Set assignment.

Independent catalog Vitest: the first invocation without `BIOME_BIN` skipped all four tests and is not a pass. With `BIOME_BIN` set to the Core checkout, three tests passed and the fourth timed out at its 45-second limit (50.7-second elapsed) while another full Biome check was running. The timeout does not establish a publication data mismatch; the developer's earlier four-test PASS is separate evidence. No deadline was changed or check weakened.
