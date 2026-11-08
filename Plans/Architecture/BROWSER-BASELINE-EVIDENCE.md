# Kotlin browser baseline, 2026-11-08

## Artifact and scope

At HEAD `1de974327b8f12a69856afcc72af58371e51897a`, with the pre-existing dirty generated 2019 exam rows, `cd kotlin && ./gradlew :composeApp:wasmJsBrowserDistribution` passed. The tested production directory was `kotlin/composeApp/build/dist/wasmJs/productionExecutable`; `composeApp.js` SHA-256 was `ef8129453310d1d1ae6f0ebc416799765dd5a3b367a62a812ba4420bed730d00`. Playwright 1.55.1 used Chromium, one worker, zero retries, port 4186. The UX4 spec was excluded to preserve existing tracked PNG artifacts.

The first Playwright launch stopped before tests because the default Homebrew Python 3.14 HTTP server did not become ready within 30 seconds. Prefixing `PATH=/usr/bin:$PATH` selected system Python 3.9.6 and made the server ready. This was runner setup, not a behavioral failure.

`PATH=/usr/bin:$PATH KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/wasmJs/productionExecutable KOTLIN_SPIKE_BRANCH=wasm KOTLIN_SPIKE_PORT=4186 npx playwright test --config=playwright.kotlin.config.ts --project=chromium --grep-invert='kotlin-ux4\\.spec\\.ts'` discovered 137 cases. The agent turn ended and terminated Playwright after case 34, before a suite summary. **Full 137-case baseline: NOT RUN to completion.** Partial log: `/tmp/polski-kotlin-wasm-chromium-baseline-20261108.log`. Do not cite this run as a full pass or infer all nine historical failures from it.

## Focused checks and defect

The partial run observed four failures in `kotlin-binary-rating.spec.ts` and `kotlin-flip-card.spec.ts` when the test helper read POLSKI-TEXT fields without the required `K:` key prefix. The test-only fix in `tests/browser/kotlin-storage.ts` made those same four cases pass on the unchanged artifact. Log: `/tmp/polski-kotlin-storage-targeted-20261108.log`. A new independent POLSKI-TEXT full-object fixture passed 1/1 (`/tmp/polski-kotlin-storage-fixture-20261108.log`). The `kotlin-training.spec.ts` affected suite passed 7/7; `kotlin-method-cycle.spec.ts` failed 1/1 for a separate product defect. Log: `/tmp/polski-kotlin-storage-affected-20261108.log`. Targeted TypeScript typecheck and `git diff --check` passed. These tests were added after the helper existed; they are regression evidence, not retrospective TDD evidence.

The method-cycle failure reproduces at a 320 px viewport: choose SituationFirst, enter the training card, and open “Таблица под рукой” before reveal. The target Biernik row shows `Было: moją piękną żonę` while its `Стало` answer is masked. The accepted pre-reveal contract withholds the exercise's own answer from visible and accessible reference text. Actual snapshot: `test-results/kotlin-wasm/kotlin-method-cycle-first--da14e-d-stores-exactly-one-rating-chromium/error-context.md`; trace beside it. `MatrixWeb.kt` passed the target phrase to `matrixContrastMasked`. Developer correction is pending a fresh artifact and independent retest; this document does not mark it closed.

No exam UI browser case was included in the baseline config at that revision. Exam UI, JS branch, Firefox, WebKit and devices: **NOT RUN** in this baseline. No tracked UX4 PNG was edited by the tester.
