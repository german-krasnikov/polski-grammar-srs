# Browser verification: continuation 2026-11-08

Scope: [execution plan](NEXT-SESSION-EXECUTION.md), P4. These are local Kotlin/Wasm Chromium results, not React, other engines, or device acceptance.

## Artifact and interrupted baseline

The independent tester built `cd kotlin && ./gradlew :composeApp:wasmJsBrowserDistribution`: PASS at HEAD `1de974327b8f12a69856afcc72af58371e51897a` plus the initial dirty 2019 generated rows. Distribution: `kotlin/composeApp/build/dist/wasmJs/productionExecutable`; `composeApp.js` SHA-256 `ef8129453310d1d1ae6f0ebc416799765dd5a3b367a62a812ba4420bed730d00`.

Command from repository root:

```sh
PATH=/usr/bin:$PATH KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/wasmJs/productionExecutable KOTLIN_SPIKE_BRANCH=wasm KOTLIN_SPIKE_PORT=4186 npx playwright test --config=playwright.kotlin.config.ts --project=chromium --grep-invert='kotlin-ux4\.spec\.ts'
```

The initial HTTP server attempt with Homebrew Python 3.14 did not become ready; `/usr/bin/python3` via PATH worked. The run selected 137 cases, with zero retries, but was interrupted after 34 cases when the tester session ended. **There is no full-suite PASS.** Log: `/tmp/polski-kotlin-wasm-chromium-baseline-20261108.log`. UX4 was excluded to preserve pre-existing changed tracked screenshots.

## Storage helper correction

The test-only progress reader omitted the `K:` prefix used for field names in POLSKI-TEXT. Rating failures showed an advanced card and updated visible review count while the helper returned zero. The tester corrected `tests/browser/kotlin-storage.ts` and added `kotlin-storage.spec.ts` to the Kotlin configuration.

On the same production artifact, independent tester results:

- Four previously failing rating/helper-consumer scenarios: PASS 4/4. Log: `/tmp/polski-kotlin-storage-targeted-20261108.log`.
- `kotlin-training.spec.ts`: PASS 7/7.
- New full-object POLSKI-TEXT browser fixture: PASS 1/1.
- `npx tsc --noEmit -p tsconfig.node.json` and `git diff --check`: PASS.

## Product defect found

`kotlin-method-cycle.spec.ts`: FAIL 1/1 on the original artifact. At a 320px viewport, SituationFirst → training → reference table exposed the target Biernik phrase as “Было” before answer reveal. The failure is separate from the storage helper. Trace: `test-results/kotlin-wasm/kotlin-method-cycle-first--da14e-d-stores-exactly-one-rating-chromium/trace.zip`; snapshot: adjacent `error-context.md`.

The developer changed the reference model to retain the nominative source phrase and the masked web rendering to use it. Rebuild and browser verification of that source correction are pending in this evidence snapshot. Exam UI scenarios, Firefox, WebKit, and physical devices: NOT RUN in these baseline runs.
