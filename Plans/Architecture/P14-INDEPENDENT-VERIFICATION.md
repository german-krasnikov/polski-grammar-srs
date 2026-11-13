# P14 first ChooseForm slice — independent verification (2026-11-13)

Scope: accepted [P14–P16 design](EXAM-PRACTICE-FEEDBACK-DESIGN.md), first P14 Web grammar `ChooseForm` slice only. This is tester evidence for the developer's uncommitted worktree; it does not close the remaining P14 controls, P15 review projection, native host, or P16 effects.

## Target and method

- Worktree: `p14-practice-feedback`, base HEAD `4fd603611ac941842889a3e89c435aef290653f3` plus the developer's uncommitted P14 diff. No production code was changed by this verification.
- Browser target: frozen compatibility distribution `../snapshots/p14-first-feedback-20261113`; `composeApp.js` SHA-256 `cb180f5c21a6756fde901f3bd13a2be73cb5402528213e08b2e23e7b078e2ea8`, `originWasmComposeApp.js` `7c8012693e952283517a9133214a69f6f255b66c631d2fe3329ac87f62d11420`, `originJsComposeApp.js` `323d08f394879503f5ab68815dbfd988635c5616d68e4497c340adcacc7fc10b`. `KOTLIN_SPIKE_BRANCH=wasm` uses the compatibility loader; the dedicated fallback case forces its JS branch.
- Browser runner: Playwright 1.55.1, Chromium/Firefox/WebKit projects. The owned real-hidden browser reports Chrome 140.0.7339.186.
- Existing developer [evidence](P14-PRACTICE-FEEDBACK-EVIDENCE.md) supplies the focused shared, generation, build, and typecheck gates for this unchanged artifact. The independent runs below exercise browser behavior directly. Logs and screenshot are in `../recovery-logs/20261112/exam-learning-quality/p14/independent/` outside `/tmp`.

## Results

| Check | Result | Evidence |
|---|---|---|
| Wrong first choice shows the chosen answer and source answer, locks repeated pointer input, advances once at 2000 ms, preserves 0% review and wrong chosen answer | **PASS** Chromium, Firefox, WebKit | `feedback-3-engine-clean.log`, 12/12 dedicated cases total |
| Manual Next cancels delayed advance; preference OFF survives reload and permits changing a choice | **PASS** three engines | Same 12/12 run |
| Route teardown clears stale feedback; Reduced motion and animations OFF retain semantic feedback | **PASS** three engines | Same 12/12 run |
| Compatibility JS fallback displays feedback and advances | **PASS** three engines | Same 12/12 run |
| Actual hidden tab clears feedback and prevents stale advance after a real click | **PASS** owned Chrome 140.0.7339.186, Wasm branch | `real-hidden.json`, `real-hidden-after.png`, `real-hidden-run-5.log` |
| Existing B1 2017 weighted half-point and four-part timed PlayTests | **PASS** 6/6 across Chromium, Firefox, WebKit | `weighted-timed-3-engine-corrected.log` |
| Browser source typecheck after test-only change | **PASS** `npm run typecheck` | `typecheck.log` |
| Physical device and native feedback | **NOT RUN** | No native consumer in this first slice |

The real-hidden fixture is `tests/browser/p14-real-hidden-practice.cjs`. It starts and owns a separate Chrome process/profile, performs the choice through Playwright, disconnects that Playwright process, then activates a real second tab through CDP. A passive `visibilitychange` listener records the browser's actual `document.hidden` transition; it neither overrides visibility nor dispatches synthetic events. On this run, the hidden event occurred **1250 ms** after the wrong-answer click, before the 2000 ms advance deadline. After at least 2400 ms hidden, the page still showed `1 / 50`, no feedback status, and the selected `A. dobry teatr` on return. The fixture cleaned its owned profile and browser; the separately owned server on port 4214 was left running.

Commands from this worktree:

```sh
node tests/browser/p14-real-hidden-practice.cjs http://127.0.0.1:4214/ ../recovery-logs/20261112/exam-learning-quality/p14/independent

KOTLIN_SPIKE_DIST=/Users/german/Work/JS/polski-grammar-srs-worktrees/snapshots/p14-first-feedback-20261113 KOTLIN_SPIKE_BRANCH=wasm KOTLIN_SPIKE_PORT=4215 npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-exam-feedback.spec.ts --grep-invert 'real hidden tab' --reporter=list

KOTLIN_SPIKE_DIST=/Users/german/Work/JS/polski-grammar-srs-worktrees/snapshots/p14-first-feedback-20261113 KOTLIN_SPIKE_BRANCH=wasm KOTLIN_SPIKE_PORT=4215 BIOME_DSL_TS_ROOT=/Users/german/Work/Python/biome-dsl/implementations/typescript npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-exam.spec.ts --grep 'B1 2017 half point score|B1 2017 four certified parts' --reporter=list
```

## Harness failures retained and correction

- The first three owned Chrome fixture attempts could activate a real hidden tab, but `Runtime.evaluate` on the background target answered 9–17 seconds late. A fourth attempt lacked an event listener in the navigated document. These were observation timing defects, not a product failure. The final fixture records the browser's own `visibilitychange` timestamp before the background read and passed. Failed logs remain in this evidence directory.
- First three-engine feedback run: 11/12 passed; Chromium's first case failed at `clock.pauseAt(new Date())` with `Cannot fast-forward to the past`, before the choice. The test now pauses at the page clock's observed time plus 10 seconds. It preserves every answer, timer, score, and review assertion. The focused rerun passed 1/1 and the clean three-engine rerun passed 12/12.
- Initial weighted/timed run: 0/6 executed because `BIOME_DSL_TS_ROOT` was omitted, so the PlayTest dialect module could not load. The corrected path above passed 6/6. This was an environment setup error, not behavioral RED.

Source inspection found the first choice is committed through `acceptGrammarPracticeInput` and graded by the existing `GrammarExamScorer.gradeInAttempt` over the full answer snapshot. Web cancellation increments a generation token and clears the timer on navigation, close, and `visibilitychange`; the browser cases above tested the observable cancellation paths. Shared developer tests and browser acceptance passing do not constitute code-review approval.
