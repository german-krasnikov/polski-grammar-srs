---
name: testing-tdd
description: "Verify Kotlin Multiplatform behavior with focused Red-Green-Refactor, common tests, real target integration and React-to-Kotlin parity fixtures. Use for test strategy, behavior implementation or evidence review; reuse actual project runners."
---

# Module testing and TDD

## Scope and tooling

Inspect actual Gradle tasks, targets, test dependencies and CI hosts. Reuse the selected Kotlin test stack; keep Vitest/Testing Library for the existing React baseline. Do not install a competing runner simply because an example mentions it.

Use test-first for testable behavior and bug fixes. Documentation/formatting work needs static validation, not artificial tests. TDD is this workflow's convention, not a Kotlin requirement. Never claim RED or a successful run that was not observed.

In the four-role workflow, developer owns test-first implementation and production fixes; tester independently fills acceptance/regression gaps and runs relevant target checks. Reviewer examines both implementation and tests read-only. Route handoffs through the main agent; a tester's later test does not retroactively prove test-first history.

## Red → Green → Refactor

1. Describe an observable contract; write the smallest discriminating test.
2. Run it and observe missing/broken behavior. Import, compiler and runner failures are setup failures, not behavioral RED.
3. Implement the accepted contract and observe the focused test pass.
4. Refactor while preserving behavior; run affected tests and consumers.

For regressions, the test should fail against the broken behavior. Do not duplicate implementation in the test, assert source spelling or merely check that an object exists. If tests follow implementation, report actual regression evidence rather than inventing test-first history.

## Test the contract

- Test public results and relevant effects; mock external boundaries rather than the algorithm under test.
- Cover meaningful empty/default/invalid cases, repeated calls, mutation ownership and error behavior. Several assertions can establish one behavior.
- Use behavior-based names and small readable fixtures. Coverage percentages are supplementary evidence.
- Keep grammar/generator/scheduler tests in `commonTest` using `kotlin.test` where portable. Common tests execute through target runners; there is no single abstract common runtime. [KMP testing](https://kotlinlang.org/docs/multiplatform/multiplatform-run-tests.html)
- Run relevant target tests for differences in numeric/date/regex behavior and platform adapters. A JVM-only result is not Wasm/JS or Native proof.

## Async isolation and cleanup

- Use `runTest` and a shared `TestCoroutineScheduler` for supported coroutine tests. Return the test result where JS requires it; do not launch a test body without waiting for completion. Test dispatchers skip/advance their own scheduled delays, not every platform callback. [Coroutines test API](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)
- Inject the domain clock and random source. Coroutine virtual time does not change wall-clock APIs or Compose's animation clock. Use fixed instants and explicit zones for daily SRS boundaries.
- Restore overridden dispatchers/mocks/global state in teardown, including failures. Close only resources owned by the test and surface cleanup errors.
- Test cancellation, stale completion, concurrent calls and partial failure when part of the contract. A fake store/transport must preserve the relevant failure and cancellation behavior.
- Avoid fixed sleeps. Wait for a specific state or advance the actual controlled scheduler deliberately. Do not concurrently mutate shared state in tests.

## React → Kotlin parity

- Keep the baseline source, lockfile, fixture provenance and current verification status identifiable. Historical documentation is evidence of its recorded run, not a fresh pass.
- Capture versioned input/output fixtures for grammar forms, complete exercise chains, valid answers, grading/queue changes, FSRS scheduling and persisted progress. Include all relevant settings, time, zone and randomness.
- Compare semantic output and agreed numeric tolerances. Do not compare source structure or expect identical RNG sequences from the same JS/Kotlin seed. Prefer explicit draws or preselected inputs.
- Verify both normal and corrupted/legacy progress imports without destroying the original fixture. Repeated imports, missing fields and unknown skills need agreed outcomes.
- For screens, reuse user scenarios and compare resulting learning state. Independent visual baselines may differ by renderer; parity means no lost behavior and acceptable layout/accessibility.

## UI and target integration

- Separate domain tests, storage integration, Compose semantics tests, compilation and real browser/device acceptance.
- Use the Compose test API only on targets supported by the selected release. Consult [Compose testing](https://kotlinlang.org/docs/multiplatform/compose-test.html); do not promise a common UI test runs everywhere simply because it compiles in common code.
- For browser input, reload persistence and visual checks, load [playwright-testing](../playwright-testing/SKILL.md). Canvas pixels are not ordinary DOM nodes; prove what semantics the implementation actually exposes.
- For Android/iOS use [kmp-android](../kmp-android/SKILL.md) or [kmp-ios](../kmp-ios/SKILL.md). Device lifecycle, software keyboard, insets and accessibility need target evidence.
- For animation, assert transition/end-state behavior with the appropriate controlled clock and check reduced motion. A recording or screenshot alone does not prove state correctness; natural timing and frame performance need separate evidence.

## Commands and evidence

Use existing scripts for the baseline (`npm test`, `npm run typecheck`, `npm run build` as configured). For Kotlin, inspect `./gradlew tasks --all` after the wrapper exists, then use the real focused tasks; do not invent a universal `commonTest` command or Android test source-set name.

After a failure, run the failed subset first, then affected checks. Expand only for dependency/risk reasons. Do not delete failures, auto-accept snapshots or repeatedly rerun unchanged failures to produce green evidence.

Report command, working directory, tested revision/artifact, target/tool versions when relevant and PASS / FAIL / NOT RUN separately for tests, lint, compilation, builds and platform integration. Zero selected tests, skipped required scenarios, a timeout or unavailable host is not PASS. Missing required acceptance evidence stays unresolved.

Official sources checked 2026-09-23. Runners and examples are conditional guidance; target selection comes from the project plan/build.
