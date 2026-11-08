# P8 JS browser test timeout diagnosis

Date: 2026-11-08. Scope: `kotlin/composeApp/karma.config.d/mocha-timeout.js` only. This follows [P8-WEB-IMPLEMENTATION-EVIDENCE.md](P8-WEB-IMPLEMENTATION-EVIDENCE.md). No production source, DSL data, or permanent test assertion changed.

## Failure and measurement

The existing 10,000 ms Mocha timeout failed `TrainingStoreTest.tabNavigationResetsMatrixSelectionOnlyOnEntry` in the P8 implementation checkout and in the unmodified `1e28907` baseline control (`/tmp/polski-p8-baseline-trainingstore.log`). A forced focused run at 10,000 ms still failed with ASR CPU work paused; Gradle's earlier UP-TO-DATE result was not counted as a rerun. XML reported `Timeout of 10000ms exceeded`.

Temporary, subsequently removed prints measured the focused JS test body at **16.1014 s** under the original timeout (the body continued after Mocha marked the test failed). With a temporary 60,000 ms timeout, the same test passed; milestones were: `ProgressCodec.fresh` about 1.1–1.4 s, cold `PlExerciseEngine` construction about 7.1 s, `TrainingStore` construction and initial chain about 12.3 s, and store start/navigation under 1 s afterward. Total body was 15–17 s. This identifies a cold synchronous fixture cost, not an indefinitely unresolved Promise. The measurement is browser/host specific; it does not assert that all machines complete inside 30 s.

The final config uses **30,000 ms**, roughly twice the observed cold body duration. It keeps Mocha's timeout active for actual hangs and leaves the shared module's Karma configuration unchanged. Temporary `TrainingStoreTest.kt` probes were removed; `git diff` for that file is empty.

## Final checks

Commands run from `kotlin/` with `--console=plain --max-workers=2` on the final 30,000 ms config:

| Gate | Command | Result |
|---|---|---|
| Full affected JS class | `./gradlew :composeApp:jsBrowserTest --tests 'polski.presentation.TrainingStoreTest'` | PASS: 23/23, 0 failures, 1m47s Gradle run. |
| Full affected Wasm class | `./gradlew :composeApp:wasmJsBrowserTest --tests 'polski.presentation.TrainingStoreTest'` | PASS: 23/23, 0 failures, 43s. |
| Complete composeApp JS browser suite | `./gradlew :composeApp:jsBrowserTest` | PASS: 32 tests across five suites, 0 failures, 2m03s. Includes four current renderer lifecycle tests. |
| Complete composeApp Wasm browser suite | `./gradlew :composeApp:wasmJsBrowserTest` | PASS: 32 tests across five suites, 0 failures, 51s. |
| Static | `git diff --check`; `git diff -- kotlin/composeApp/src/commonTest/kotlin/polski/presentation/TrainingStoreTest.kt` | PASS: no whitespace errors and no retained test probe. |

Gradle/Karma test XML under `kotlin/composeApp/build/test-results/{jsBrowserTest,wasmJsBrowserTest}/` records the final 32/32 results. Logs: `/tmp/polski-p8-trainingstore-class-js30.log`, `/tmp/polski-p8-trainingstore-class-wasm30.log`, `/tmp/polski-p8-full-js30.log`, `/tmp/polski-p8-full-wasm30.log`. The 12 user-owned UX4 PNG modifications in the shared checkout were untouched. ASR CPU work paused by the root during timing measurement was explicitly released for resumption before the full-class gates.

Independent code review: APPROVED. Reviewer read final JS and Wasm XML: each 32 tests, zero failures/errors/skips across five suites, including TrainingStore 23 and lifecycle 4. Only test timeout configuration changed; no assertions or production source changed.
