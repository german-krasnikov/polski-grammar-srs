# P19 integrated web and shared acceptance — 2026-11-13

Scope: Main source commit `c2902b712c01dbcdc7f869c85673b0a2bf457dde` (P13/P17/P18 plus accepted P14/P15 web and P16 Rive). Main HEAD later advanced to `7db5e5d3620cb5ccadeed6de99ac7a5988057552` through documentation-only commits; no production source changed during these runs. The test-only working diff is in `tests/browser/kotlin-en-course-switch.spec.ts`, `kotlin-grouped-practice-feedback.spec.ts`, `kotlin-exam-feedback.spec.ts`, and the owned-Chrome hidden-tab harness `p14-real-hidden-practice.cjs`. Native P14/P15 is still isolated and **not** part of this artifact. Contract: [EXAM-LEARNING-QUALITY-PLAN.md](EXAM-LEARNING-QUALITY-PLAN.md), A14–A19.

## Built artifacts and runner

Working directory for npm, Biome, and Playwright: `/Users/german/Work/JS/polski-grammar-srs`; Gradle: its `kotlin/` child. Playwright 1.55.1, one worker, zero retries, actual Chromium/Firefox/WebKit projects; Vite preview serves the fresh compatibility distribution on owned strict ports 4224–4233. The compatibility loader and actual requested runtime scripts were checked in the DSL and P17 cases.

| Fresh production artifact | SHA-256 |
|---|---|
| `kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable/originJsComposeApp.js` | `2d6f6c0f89269d691a3e7ae7a8b5cd2ac63a85dc58ab45df6a9069f92c37789b` |
| `kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable/originWasmComposeApp.js` | `c6e34570890c96279ec7409364e3aae30cfa6926789c5a34dd73604a6409cc63` |
| Compatibility `composeApp.js` loader | `cb180f5c21a6756fde901f3bd13a2be73cb5402528213e08b2e23e7b078e2ea8` |

Direct JS and Wasm production `composeApp.js` hashes equal the corresponding compatibility `origin*` hashes above. The 16 pinned exam MP3 files passed each distribution's Gradle finalizer. `:composeApp:verifyNoWebSourceMaps` found no source-map files.

## Executed checks

Logs are retained under `../polski-grammar-srs-worktrees/recovery-logs/20261113/p19-integrated-web/` relative to Main.

| Gate | Result | Command / evidence |
|---|---|---|
| Biome generated source and format | **PASS** | `node /Users/german/Work/Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs generate --check --project dsl/biome.project`; `biome-check-final.log`, exit 0 |
| TypeScript | **PASS** | `npm run typecheck` after final test edits; `typecheck-final.log`, exit 0 |
| Source/content Vitest | **PASS 322/322**, 24 files | `npx vitest run tests/exam-*.test.ts tests/course-*.test.ts tests/ui-copy-swift-source.test.ts`; `vitest-focused.log` |
| Shared JVM/desktop focused classes | **PASS 41/41** | `./gradlew :shared:desktopTest --offline --max-workers=2 --console=plain --tests 'polski.exam.ExamPracticeFeedbackTest' --tests 'polski.exam.ExamGroupedPracticeFeedbackTest' --tests 'polski.exam.ExamReviewRowTest' --tests 'polski.preferences.UserPreferencesCodecTest' --tests 'polski.data.PlRuExpansionCoverageTest' --tests 'polski.data.UiCopyRowsTest' --tests 'polski.exam.ExamContentIntegrityAuditTest'`; `shared-desktop.log`, seven result XMLs |
| Shared JS and Wasm browser-backed Kotlin suites | **PASS 479/479 each**, zero failures/skips | `./gradlew :shared:jsTest` and `:shared:wasmJsTest`, each `--offline --max-workers=2 --console=plain`; `shared-js.log`, `shared-wasm.log`, 111 XML suites per target |
| Fresh JS/Wasm/compatibility distributions, audio, source maps | **PASS** | `./gradlew :composeApp:verifyNoWebSourceMaps --offline --max-workers=2 --console=plain`; `web-build.log`, exit 0, 16 audio assets checked in each distribution |
| Wasm Chromium integrated Playwright selection | **PASS 24/24** | `browser-wasm-chromium.log` |
| JS Chromium same selection | **PASS 22/24 initial; PASS 2/2 failed-case rerun** after test-only corrections | `browser-js-chromium.log`, `browser-js-chromium-failed-rerun.log`; original two failures retained |
| Wasm Firefox + WebKit selected integration | **PASS 28/28** (14 per engine) | `browser-wasm-firefox-webkit.log` |
| JS Firefox + WebKit selected integration | **PASS 28/28** (14 per engine) | `browser-js-firefox-webkit.log` |
| Imported OFF/reload, legacy default ON, timed no early key | **PASS 2/2** Wasm Chromium and **PASS 2/2** JS Chromium after correcting a test count assumption | `browser-wasm-new-gaps.log` original, `browser-wasm-new-gaps-rerun.log`, `browser-js-new-gaps-final.log` |
| Timed first single choice can be changed before submit | **PASS 1/1** Wasm Chromium and **PASS 1/1** JS Chromium after strengthening the new assertion | `browser-wasm-timed-edit-final.log`, `browser-js-timed-edit-final.log` |
| Accepted source variant versus unknown pending wording | **PASS 2/2** Wasm Chromium and **PASS 2/2** direct JS Chromium | `browser-wasm-variant-pending.log`, `browser-js-variant-pending.log` |
| File-inventory-based P17 compatibility runtime assertion | **PASS 1/1** Wasm Chromium and **PASS 1/1** JS Chromium | `p17-inventory-wasm.log`, `p17-inventory-js.log`; actual requested scripts `originWasmComposeApp.js` and `originJsComposeApp.js` |
| P17 direct JS path still recognized | **PASS 1/1** Chromium | `p17-inventory-direct-js.log`; actual `composeApp.js`, no `origin*` runtime |
| Future settings recovery and file import with backup | **PASS 2/2** Wasm Chromium, **PASS 2/2** direct JS Chromium | `preferences-wasm-recovery.log`, `preferences-js-recovery.log` |
| Genuine background tab during grammar feedback | **PASS** owned Chrome 140.0.7339.186 on fresh Wasm compatibility and direct JS; hidden event at +1828 ms / +732 ms, feedback cleared, question 1/50 and neutral locked answer retained after >2400 ms hidden | `hidden-owned-{wasm,js}-final.log` and owned-harness screenshots/JSON |
| Whitespace | **PASS** | `git diff --check`, exit 0 |

Playwright invocation for the integrated selections:

```sh
BIOME_DSL_TS_ROOT=/Users/german/Work/Python/biome-dsl/implementations/typescript \
KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable \
KOTLIN_SPIKE_BRANCH=<wasm|js> KOTLIN_SPIKE_PORT=<owned strict port> \
npx playwright test -c playwright.kotlin.config.ts --project=<chromium|firefox|webkit> \
  --workers=1 --retries=0 \
  tests/browser/kotlin-exam-feedback.spec.ts \
  tests/browser/kotlin-grouped-practice-feedback.spec.ts \
  tests/browser/kotlin-exam.spec.ts \
  tests/browser/kotlin-en-course-switch.spec.ts \
  tests/browser/kotlin-course-expansion-nine.spec.ts \
  tests/browser/kotlin-rive-lifecycle.spec.ts --grep '<selected case titles>'
```

The Chromium 24-case `--grep` value was `practice choice reveals|manual next cancels|route teardown|grammar text commits|grammar bank commits|open manual grammar|grammar submitted review|default Reading practice|default Listening practice|Reading final group|Reading focus wraps|Listening unordered|Listening text keeps|submitted Reading and Listening|preference OFF keeps Reading|Listening keeps the same playing audio beyond 41|new Reading answer|DSL practice PlayTest|matrix shows only|switching course keeps rated Polish|rating hit target recovers|P16 rating overlay is cleared|P16 failed effect metadata`. The Firefox/WebKit 14-case selection used `practice choice reveals|route teardown|grammar submitted review|DSL practice PlayTest|default Reading practice|default Listening practice|Listening unordered|submitted Reading and Listening|new Reading answer|matrix shows only|rating hit target recovers|P16 rating overlay is cleared|P16 failed effect metadata`. Chromium covered first wrong/repeated grammar choice, 1999/2000 ms and no duplicate advance, text Check, bank identity, pending manual, wrong/blank report, grouped reading/listening focus/wrap/final stay, partial set, listening text draft, wrong/source frozen reports, OFF behavior, same real player beyond 41 seconds, route/reduced-motion cleanup, both authored DSL PlayTests, P17 pack switch and retained Polish progress, P18 rating after motion/route return, and two Rive failure/route cleanup cases. The additional import/timed and source-variant cases close the remaining selected web semantics.

## Initial failures and classification

- JS Chromium first selection, 2/24 failures: **test artifact assumptions**, no product behavior failure. P17 test expected direct JS with no `origin*` script while the command served the compatibility distribution and actually loaded `originJsComposeApp.js`. The grouped preference-OFF case registered the JS fallback hook after first loading Settings, then asserted an injected script tag that was absent on hash navigation. Test-only fixes classify compatibility by actual files and register fallback before the first navigation. Both failed cases passed 2/2 on focused rerun; the original `browser-js-chromium.log` and traces remain.
- First new timed test expected exactly two controls in the first B1/2017 listening question, but the actual question has three. That unrelated count assertion was replaced by visibility of two distinct options; natural taps, editable selection, and absent early verdict/key remain asserted. The original `browser-wasm-new-gaps.log` and trace remain; corrected Wasm 2/2 and JS 2/2 pass.
- A Playwright headless Chromium hidden-tab attempt never reached its `document.hidden=true` precondition after `bringToFront`; it timed out without exercising the product callback (`hidden-wasm-chromium.log` and trace). The existing owned-Chrome CDP fixture drove a real tab activation instead and passed on both final bundles. Its previous runtime guard was Wasm-only; the test-only fixture now accepts `P14_EXPECT_RUNTIME=composeApp.js` for direct JS while requiring an exact script inventory. The first direct-JS fixture run stopped at the old Wasm-only guard (`hidden-owned-js.log`); final JS and Wasm runs passed. Both owned Vite servers and Chrome profiles were stopped/removed.
- The P17 test now recognizes a compatibility bundle from its `originJsComposeApp.js` and `originWasmComposeApp.js` files, not from a directory name. It still asserts the one expected actual runtime script. This preserves direct-JS acceptance on a differently named distribution and avoids a path-dependent green result.

No production correction was made in this lane. Initial failures are not silently counted as first-run PASS.

## Criteria and remaining gates

| Criterion | Integrated web/shared status |
|---|---|
| A14/A15 | **PASS** for selected web/default/legacy/OFF/import/timed, wrong/source, blank/partial/variant/pending cases in shared tests and actual JS/Wasm browsers. Native preference, feedback, and report remain separate. |
| A16 | **PASS** on selected exact 2-second, group wrap/final stay, manual/route/overlay cleanup, reduced motion, disabled option, and 41-second audio paths; genuine hidden-tab Grammar passed afresh on Wasm and JS. Accepted P14 keyboard and grouped Reading/Listening hidden-tab evidence is reused because the merged P16 production changes affect Training/Rive, not the exam visibility callback. |
| A17 | **PASS** selected final JS/Wasm pack switch plus accepted P17 24/24 web, 6/6 progress, bridge and installed iPad evidence where native consumer unchanged. |
| A18 | **PASS** focused final bundle rating/persistence route and 26-topic source-catalog Kotlin checks; prior independent audit of all 54 new sentence chains, ten topics/four styles, WebKit six-cycle, and native nine topics is reused where course data and native paths are unchanged. |
| A19 web/shared | **PASS** for checks above on the fresh merged web artifact. Final independent reviewer, native P14/P15 Simulator acceptance and physical-device status are **NOT RUN in this lane**. P19 as a whole remains open until those results are integrated. |

P13's 16-paper official PDF/source audit, P17 native matrix, P18 semantic audit/native cards, and P16 34-case independent Rive lifecycle acceptance are reused from their accepted evidence documents; repeating those full unchanged suites would not add a discriminating integration check. This run did not install a native app, use Simulator, touch live progress, publish, or change Git index/release state.
