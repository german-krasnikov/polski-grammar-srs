# P14 grammar controls and P15 frozen grammar review — independent verification

Date: 2026-11-13. Accepted scope: [P14–P16 design, slice 2 addendum](EXAM-PRACTICE-FEEDBACK-DESIGN.md). This verifies common grammar practice, all Web grammar control families, and grammar-only submitted review. Reading/Listening practice, native hosts, and P16 animation are later work.

## Target

- Worktree `p14-practice-feedback`, base HEAD `e7b32b519e7bd292be7073010d998dd8711d876b` plus the developer's uncommitted slice 2 changes. No production files were changed by the tester.
- Frozen actual browser target: `../snapshots/p14-grammar-controls-20261113` relative to this worktree. SHA-256: `originWasmComposeApp.js` `d6b86e6532685d4d991eefca53504b450333622da3e38e5abd83c3a1f6c9bae4`; `originJsComposeApp.js` `9e211325d1902c8386055239ba1b8efe800872bd0facad6a1604f2b9fb4beab1`; `exam.css` `2bf03eb1cd4d6271062cb0f6ea5052d6dcc181c89cdd4d6bd04fcf0c8488a9df`. Browser runner Playwright 1.55.1; actual hidden-tab Chrome 140.0.7339.186.
- Logs/JSON/screenshot: `../recovery-logs/20261112/exam-learning-quality/p14/independent/` relative to this worktree, outside `/tmp`. The developer's original dated receipt is `../recovery-logs/20261112/exam-learning-quality/p14/P14-SLICE2-DEVELOPER-EVIDENCE.md`; the [copy in Plans](P14-GRAMMAR-CONTROLS-DEVELOPER-EVIDENCE.md) records its own checks and honest RED chronology.

## Acceptance

| Contract | Independent result | Evidence |
|---|---|---|
| Focused shared grammar scorer, practice policy/state, and frozen review row tests | **PASS**, re-executed `:shared:desktopTest` with `--rerun-tasks`, 26 tasks executed | `slice2-shared-rerun.log` |
| Text draft remains editable, blank Check does not commit, later explicit Check freezes raw answer, shows source answer, advances at 2000 ms, and locks on return | **PASS** Chromium/Firefox/WebKit, source C1 April 2024 | `slice2-five-3-engine.log`, strengthened `slice2-text-blank-draft-3-engine.log` |
| Word bank commits raw source text and token identity; locks on return | **PASS** three engines, source C1 April 2024 | `slice2-five-3-engine.log` |
| Duplicate printed `w` tokens remain distinct: selecting occurrence 2 retains token ID `certyfikat-polski-B1-2020-grammar-8-bank-7`, never marks occurrence 1 selected after navigation, and locks all options | **PASS** three engines, authored B1 2020 grammar VIII source | New `practice bank keeps the second printed w token distinct after navigation`; `slice2-duplicate-bank-3-engine.log` |
| Paired text first commit defers status/advance; second committed partner settles source tuple; manual open answer reports pending and does not auto-advance | **PASS** three engines; shared tests also cover uncommitted partner draft, conditional variant, both settled evaluations, and final item stay | `slice2-five-3-engine.log`, `slice2-shared-rerun.log` |
| Submitted Web grammar review freezes wrong chosen answer, blank, and pending manual with source examples; shared review rows preserve partial quarter credit, canonical/accepted variant, and blank choice | **PASS** three engines for source-backed Web submit and focused shared tests for partial/variant | `slice2-five-3-engine.log`, `slice2-shared-rerun.log` |
| Actual JS fallback path for all six new browser cases, including duplicate bank token | **PASS** Chromium 6/6; `originJsComposeApp.js` loading separately asserted | `slice2-six-js-chromium.log`, `slice2-js-bundle-confirm.log` |
| Genuine hidden-tab cancellation on the new Wasm artifact | **PASS** owned Chrome: real `document.hidden` event at +557 ms, after ≥2400 ms hidden and return Q1 remains, selected answer and neutral lock remain, status absent, options disabled | `slice2-hidden/real-hidden.json`, screenshot, `run.log` |
| TypeScript typecheck and whitespace | **PASS** | `slice2-typecheck.log`, `git diff --check` |
| Native Apple/Android app UI, Reading/Listening practice, P16 effects | **NOT RUN** | Outside this handoff |

The 15 initial new browser checks passed across Chromium, Firefox, and WebKit. The additional duplicate-source case passed 3/3. The text case was then strengthened with blank Check and a changed draft and passed 3/3. Developer evidence covers the 21 legacy grammar PlayTests after the fixture explicitly turned preference OFF for its old reselecting bank scenario; this independent pass did not rerun the entire legacy set. Earlier unchanged score/timed cases remain recorded in [first-slice independent evidence](P14-INDEPENDENT-VERIFICATION.md). No production defect was observed in this slice.

Commands (run from this worktree unless `cd kotlin` is shown):

```sh
cd kotlin && ./gradlew :shared:desktopTest --tests polski.exam.grammar.GrammarExamScorerTest --tests polski.exam.ExamPracticeFeedbackTest --tests polski.exam.ExamReviewRowTest --rerun-tasks --max-workers=2

KOTLIN_SPIKE_DIST=/Users/german/Work/JS/polski-grammar-srs-worktrees/snapshots/p14-grammar-controls-20261113 KOTLIN_SPIKE_BRANCH=wasm KOTLIN_SPIKE_PORT=4224 npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-exam-feedback.spec.ts --grep 'grammar text commits|grammar bank commits|paired grammar waits|open manual grammar|grammar submitted review' --reporter=list

KOTLIN_SPIKE_DIST=/Users/german/Work/JS/polski-grammar-srs-worktrees/snapshots/p14-grammar-controls-20261113 KOTLIN_SPIKE_BRANCH=wasm KOTLIN_SPIKE_PORT=4224 npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-exam-feedback.spec.ts --grep 'second printed w' --reporter=list

KOTLIN_SPIKE_DIST=/Users/german/Work/JS/polski-grammar-srs-worktrees/snapshots/p14-grammar-controls-20261113 KOTLIN_SPIKE_BRANCH=js KOTLIN_SPIKE_PORT=4224 npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-exam-feedback.spec.ts --grep 'grammar text commits|grammar bank commits|second printed w|paired grammar waits|open manual grammar|grammar submitted review' --project=chromium --reporter=list

npx vite preview --host 127.0.0.1 --port 4224 --strictPort --outDir /Users/german/Work/JS/polski-grammar-srs-worktrees/snapshots/p14-grammar-controls-20261113
node tests/browser/p14-real-hidden-practice.cjs http://127.0.0.1:4224/ ../recovery-logs/20261112/exam-learning-quality/p14/independent/slice2-hidden

npm run typecheck
git diff --check
```

Test-only changes: `tests/browser/kotlin-exam-feedback.spec.ts` gained the B1 2020 duplicate-token acceptance case, blank/draft assertions in the text case, and an explicit JS bundle assertion. The owned port 4224 preview and Chrome/profile were stopped and removed; no shared server or simulator was touched. Passing tests do not replace independent code review.
