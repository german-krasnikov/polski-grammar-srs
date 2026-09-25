# Stage 7–8 independent retest — P05/P10/P11

Date: 2026-09-24. **Focused retest: PASS.** This supersedes the transient-marker retry failure in [Stage7-Tester.md](Stage7-Tester.md) for the current, uncommitted workspace. It does not close the overall Kotlin migration, device, or reviewer gates. HEAD is `df59774e153a5bdf590fe27bd8780c7bd5457a26` plus workspace changes.

## Independent acceptance

| Contract | Result | Evidence and limit |
| --- | --- | --- |
| P05/P10 one review and durable reload | **PASS** | The pinned `P-oral-good` and `P-typed-wrong` JSON documents match every expected field after one UI rating, then still match after page reload. Chromium, Firefox and Playwright WebKit passed on forced JS and Wasm. The second-rating shortcut is rejected and saved progress survives reload in the focused training checks (2/2 per branch on Chromium). |
| Stage 8 async save and stale completion | **PASS** | New `TrainingStoreTest` holds a review save, requests reset, tries a stale old-card rating, then completes the save. Reset waits for the older write and returns to an empty Question state with one reset and no extra review. Focused TrainingStore tests: 17/17 on JS and 17/17 on Wasm, zero failures/skips. Existing tests also exercise ordered revisions, close/late write, and a five-rating chain. |
| P11 partial marker write and retry | **PASS** | Real `localStorage` throws once on the migration marker. Backup remains the exact legacy raw and the unmarked preview remains readable. Reload shows recovery rather than Ready; in-app retry binds the existing preview to the marker without changing backup, legacy raw, or review count. A further reload enters Ready. The extended case passed on all three browser engines for both JS and Wasm. This is the previously failing Stage 7 acceptance. |
| P11 browser migration and React compatibility | **PASS** | Full focused migration spec passed 9/9 per branch over Chromium, Firefox and Playwright WebKit. It includes Kotlin current export after review loaded in React, invalid legacy recovery, and transient marker retry. |
| P11 all 20 pinned progress fixtures | **PASS under the documented Kotlin safety contract** | Independent browser replay of the 17 UI cases passed 17/17 on each branch in Chromium. Generated shared common replay of the remaining three raw import cases and both same-skill midnight transitions passed 5/5 on JS and 5/5 on Wasm. Case mapping and intentional differences are in [ProgressParity.md](ProgressParity.md). Malformed/unsupported progress remains recoverable instead of React's fresh fallback; version 99 is rejected instead of accepted. |
| Native devices and real Safari/iPhone | **NOT RUN** | Playwright WebKit is browser-engine evidence, not physical-device certification. |

## Test-only changes

- `tests/browser/kotlin-progress-migration.spec.ts`: make the injected marker failure one-shot across reload, check partial-state recovery before retry, and verify Ready plus unchanged raw data after another reload.
- `tests/browser/kotlin-parity-progress-review.spec.ts`: seed preview only when absent and compare exact reviewed JSON again after reload for the oral and typed cases.
- `kotlin/composeApp/src/commonTest/kotlin/polski/presentation/TrainingStoreTest.kt`: add reset/older-save/stale-rating regression with a controlled pending write and count reset calls.

No production source was changed by this Tester retest.

## Commands and artifacts

Working directory `kotlin/`:

- `./gradlew :composeApp:jsBrowserTest :composeApp:wasmJsBrowserTest --tests 'polski.presentation.TrainingStoreTest' --console=plain` — **PASS**, 17/17 per target, zero failures/skips.
- `./gradlew :shared:jsBrowserTest :shared:wasmJsBrowserTest --tests 'polski.progress.ProgressFixtureParityTest' --console=plain` — **PASS**, 5/5 per target, zero failures/skips. The ChromeHeadless process needed SIGKILL during post-test teardown; Gradle tasks completed successfully.

Working directory repository root, using `npx playwright test --config=playwright.kotlin.config.ts` with `KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/{js|wasmJs}/productionExecutable`, matching `KOTLIN_SPIKE_BRANCH=js|wasm`, and a unique port per run:

- `tests/browser/kotlin-progress-migration.spec.ts` — **PASS**, 9/9 per branch across Chromium, Firefox and WebKit.
- `tests/browser/kotlin-parity-progress-load.spec.ts tests/browser/kotlin-parity-progress-review.spec.ts tests/browser/kotlin-progress-local-day.spec.ts tests/browser/kotlin-parity-progress-recovery.spec.ts --project=chromium` — **PASS**, 17/17 per branch.
- `tests/browser/kotlin-parity-ratings.spec.ts tests/browser/kotlin-training.spec.ts --project=chromium --grep 'second rating shortcut|reload restores saved progress'` — **PASS**, 2/2 per branch.
- `npm run typecheck` — **PASS** on final rerun. An earlier attempt failed on concurrent `src/data/course.ts` optional-field work; that source was corrected outside this Tester scope before the successful rerun.
- `git diff --check` — **PASS**.

The tested JS bundle SHA-256 is `5c16dbf373a3f4d3911e8c7576b08232b88241d6b7c4e07d3de03d491bc38638`; Wasm bundle `561708e4212c4b1709f1753fcde58a25983b9c7300b2071f6740356dbbf8d755`; pinned `progress.json` fixture `12547f0cf5c27bb5340f92be7940007776f05044dc0673d8a821d232fa04ac56`. Browser tests used local loopback origins and storage, not live user data. Full suite, physical-device, and independent code-review checks were **NOT RUN** in this focused retest.
