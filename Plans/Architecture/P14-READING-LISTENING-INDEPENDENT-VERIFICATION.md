# P14 Reading/Listening and P15 grouped review — independent verification

Date: 2026-11-13. Accepted contract: [Reading/Listening practice addendum](READING-LISTENING-PRACTICE-ADDENDUM.md). This is Web and shared grouped behavior only. Native bridge/SwiftUI/Simulator and P16 effects remain separate.

## Target and evidence

- Isolated `p14-practice-feedback` worktree, base HEAD `d42b47d37885ebf1db3314c798149f82153ba9b1` plus uncommitted developer implementation. Tester changed only `tests/browser/kotlin-grouped-practice-feedback.spec.ts` and this evidence file. No production edit, index, commit, or push.
- Frozen compatibility distribution: `../snapshots/p14-grouped-practice-20261113` relative to worktree. SHA-256: `originWasmComposeApp.js` `ca8ca53877f6255fc41398fb02b00065be34de2f93ab1213025269a631516708`; `originJsComposeApp.js` `c8d2dbbaa01685bde80c7acef9f57bff5f98dba112423f163017c0867be0b48e`; `exam.css` `5cd1cf31105d4ce59ccebdbaeb04b62f325370794f0937afcd1156e3802c5704`.
- Browser Playwright 1.55.1, Chromium/Firefox/WebKit projects with one worker and zero retries. Genuine hidden tests used owned Chrome 140.0.7339.186. Logs, JSON, and screenshots: `../recovery-logs/20261112/exam-learning-quality/p14/independent/grouped/` relative to the worktree, outside `/tmp`.
- Developer's [evidence](P14-READING-LISTENING-DEVELOPER-EVIDENCE.md) retains genuine pre-production RED 2/2 on the old grammar artifact, focused shared/scorer/JS/Wasm build and Biome gates, 17 legacy Reading/Listening PlayTests, and timed full-part regression. Those passes are reused for unchanged scoring and source data; this tester did not rerun the broad legacy set or an expensive Gradle rebuild.

## Functional results

| Check | Result | Evidence |
|---|---|---|
| Source-backed Reading wrong click shows chosen red, official answer green and text; focus changes at 1999/2000 ms within same passage, never auto changes group or submits | **PASS** three browser engines | `wasm-three-engines.log` |
| Source-backed Listening wrong click preserves same playing audio node/time, focuses within task; explicit Next seeks/pauses the new segment | **PASS** three engines | `wasm-three-engines.log` |
| Reading true/false and matching; last group answer stays until explicit Next; return is neutrally locked; manual navigation, new answer and route cleanup cancel stale overlay/callback | **PASS** three engines | `wasm-three-engines.log` |
| Earlier unanswered Reading row is chosen after the final answered row, with 1999/2000 ms boundary and unchanged passage | **PASS** three engines, added tester case | `wrap-oversel-three-engines.log` |
| Listening unordered set remains draft below slot count; commits 1/3 as partial +1/2; all options lock at source slot count and real pointer click cannot add a third option | **PASS** three engines, strengthened tester assertions | `wasm-three-engines.log`, `wrap-oversel-three-engines.log` |
| Listening text remains editable until explicit Check; frozen report shows chosen wrong/source, blank, partial and pending without draft mutation; preference OFF remains editable with no early key | **PASS** three engines | `wasm-three-engines.log` |
| Actual JS fallback across the eleven short grouped cases; loader confirmed as `originJsComposeApp.js` | **PASS** Chromium 11/11, loader focused rerun 1/1 | `js-chromium.log`, `js-bundle-confirm.log` |
| Real B1 2019 distributed audio remains the same connected playing node and advances past 41 seconds after grading/focus | **PASS** Chromium, 46.3 s case | `long-audio-chromium.log` |
| Real browser `document.hidden` lifecycle cancels Reading and Listening feedback and stale advance | **PASS** owned Chrome: Reading event +267 ms; Listening +191 ms; Q/task unchanged after ≥2400 ms hidden, selected answer/neutral lock retained, status absent | `hidden-reading/real-hidden.json`, `hidden-listening/real-hidden.json`, screenshots and logs |
| Browser source typecheck and whitespace | **PASS** | `typecheck.log`, `git diff --check` |
| Native Apple/Android app UI, P16 Rive | **NOT RUN** | Separate lanes |

The shared grouped tests cover valid current group, first commit, wrap and final stay, slot count and open Check, timed/OFF, frozen variant/partial/pending review. The developer executed them on this source; the independent Web cases above exercise the published B1/B2 controls through real clicks and typing. The known Writing/Reading/player background-token issue fixed on Main at `05ca753` is an integration dependency for merging this branch, not a new defect report here.

## Light-theme readability defect — FAIL

A new computed-style acceptance test measures rendered text contrast after compositing translucent feedback/review backgrounds and disabled option opacity. It uses the existing 4.5:1 normal-text criterion from `kotlin-emphasis-underline.spec.ts`. Real Chromium on the frozen Wasm artifact:

| Rendered text on source exercise | Dark | Light |
|---|---:|---:|
| Reading chosen wrong option | 4.62:1 PASS | **3.01:1 FAIL** |
| Reading official correct option | 4.54:1 PASS | **2.96:1 FAIL** |
| Frozen wrong review official source text | 8.42:1 PASS | **1.72:1 FAIL** |
| Frozen partial review credit text | 7.33:1 PASS | **1.85:1 FAIL** |
| Status, pending and chosen review text (remaining seven samples) | PASS, minimum 12.80:1 | PASS, minimum 12.11:1 |

Reproduction: select Light in Settings; open B1 2017 Reading, click wrong `kioski internetowe są popularne w całym kraju`, inspect the disabled wrong and correct options; finish and inspect `Образцы из ключа`; open B2 2017 Listening task IV, choose `1` and `3` for partial credit, then finish and inspect `+1 / 2`. Expected each normal text sample ≥4.5:1. Actual values above are computed from browser styles; full RGB/background values, trace, and screenshots are in `theme-contrast-chromium-full.log` and Playwright retained artifacts. The immediate option loss follows `.exam-option.locked { opacity: 0.72 }`; report source/partial text uses pale `--primary` via `.exam-review-correct`. This affects the newly used feedback/report UI and is distinct from the older Main background-token correction. Production fix belongs to the developer; this test remains RED.

The initial visual run also had a **test harness** timeout after the Reading report because navigating to the same `/#/exam` URL did not recreate the catalog. The test now reloads before opening the second exam; its full dark run passed and full light run reached all eleven computed samples before failing the contrast assertion. The first log is retained as `theme-contrast-chromium.log`.

## Commands

All commands from this worktree unless stated otherwise:

```sh
KOTLIN_SPIKE_DIST=/Users/german/Work/JS/polski-grammar-srs-worktrees/snapshots/p14-grouped-practice-20261113 KOTLIN_SPIKE_BRANCH=wasm KOTLIN_SPIKE_PORT=4224 npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-grouped-practice-feedback.spec.ts --grep-invert 'beyond 41 seconds' --reporter=list

KOTLIN_SPIKE_DIST=/Users/german/Work/JS/polski-grammar-srs-worktrees/snapshots/p14-grouped-practice-20261113 KOTLIN_SPIKE_BRANCH=wasm KOTLIN_SPIKE_PORT=4224 npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-grouped-practice-feedback.spec.ts --grep 'focus wraps to|unordered choices commit' --reporter=list

KOTLIN_SPIKE_DIST=/Users/german/Work/JS/polski-grammar-srs-worktrees/snapshots/p14-grouped-practice-20261113 KOTLIN_SPIKE_BRANCH=js KOTLIN_SPIKE_PORT=4224 npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-grouped-practice-feedback.spec.ts --grep-invert 'beyond 41 seconds' --project=chromium --reporter=list

KOTLIN_SPIKE_DIST=/Users/german/Work/JS/polski-grammar-srs-worktrees/snapshots/p14-grouped-practice-20261113 KOTLIN_SPIKE_BRANCH=wasm KOTLIN_SPIKE_PORT=4224 npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-grouped-practice-feedback.spec.ts --grep 'beyond 41 seconds' --project=chromium --reporter=list

KOTLIN_SPIKE_DIST=/Users/german/Work/JS/polski-grammar-srs-worktrees/snapshots/p14-grouped-practice-20261113 KOTLIN_SPIKE_BRANCH=wasm KOTLIN_SPIKE_PORT=4224 npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-grouped-practice-feedback.spec.ts --grep 'remain readable' --project=chromium --reporter=list

npx vite preview --host 127.0.0.1 --port 4224 --strictPort --outDir /Users/german/Work/JS/polski-grammar-srs-worktrees/snapshots/p14-grouped-practice-20261113
node tests/browser/p14-real-hidden-reading.cjs http://127.0.0.1:4224/ ../recovery-logs/20261112/exam-learning-quality/p14/independent/grouped/hidden-reading
node tests/browser/p14-real-hidden-listening.cjs http://127.0.0.1:4224/ ../recovery-logs/20261112/exam-learning-quality/p14/independent/grouped/hidden-listening

npm run typecheck
git diff --check
```

Only the owned preview port 4224 and fixture Chrome profiles were used and cleaned. The real-hidden fixtures do not override visibility or synthesize grading events. Passing functional tests do not constitute code-review approval.

## Readability correction — independent recheck

The Light-theme FAIL above remains the historical RED against `../snapshots/p14-grouped-practice-20261113`. The developer changed only `exam.css` production styling for feedback, reports and disabled controls. The corrected immutable target is `../snapshots/p14-grouped-readable-20261113`: `exam.css` SHA-256 `eba8438c1349eab5e8e8a4ebd132ee02ef5f7df53f6a05351b1135d3427f1585`; Wasm and JS entrypoints retain the hashes listed above (`ca8ca538…`, `c8d2dbba…`). No acceptance threshold or contrast calculation was weakened.

**PASS:** Full Light/Dark computed-style matrix for Reading/Listening feedback/report and Grammar return lock/report: 12/12 Chromium, Firefox and WebKit on Wasm; 4/4 Chromium on actual JS fallback. The four formerly failing Light samples now measure chosen wrong 5.54–6.90:1, source correct 5.47–6.43:1, frozen source text 5.78:1 and partial credit 13.00:1. The dark samples remain above 4.5:1. The strengthened test also verifies wrong text uses a red channel, correct text a green channel, partial chosen chips use the neutral text token without a wrong class, manual pending has no red/green verdict, and committed options retain native `disabled`, `aria-disabled=true` and `cursor: not-allowed`. Grammar selected choice and neutral note remain readable and locked after return. `npm run typecheck` and `git diff --check`: **PASS**.

Evidence: `../recovery-logs/20261112/exam-learning-quality/p14/independent/grouped/readable/{wasm-three-engines.log,wasm-semantic-three-engines.log,js-chromium.log,js-semantic-chromium.log,typecheck.log}` relative to the worktree. The post-correction semantic runs are the final acceptance results; earlier contrast logs are retained. The previous 30/30 functional, 6/6 focused wrap/oversel, JS 11/11, 46-second audio and genuine hidden-tab passes remain applicable because the JS/Wasm entrypoints and media artifacts did not change. This recheck did not repeat the long audio or native device work.

```sh
KOTLIN_SPIKE_DIST=/Users/german/Work/JS/polski-grammar-srs-worktrees/snapshots/p14-grouped-readable-20261113 KOTLIN_SPIKE_BRANCH=wasm KOTLIN_SPIKE_PORT=4224 npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-grouped-practice-feedback.spec.ts --grep 'remain readable' --reporter=list

KOTLIN_SPIKE_DIST=/Users/german/Work/JS/polski-grammar-srs-worktrees/snapshots/p14-grouped-readable-20261113 KOTLIN_SPIKE_BRANCH=js KOTLIN_SPIKE_PORT=4224 npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-grouped-practice-feedback.spec.ts --grep 'remain readable' --project=chromium --reporter=list
```

The only tester change after the developer's style correction strengthened `tests/browser/kotlin-grouped-practice-feedback.spec.ts` semantic assertions. Production defect status for this scoped visual check: **resolved and independently verified**. Native remains **NOT RUN**. The known Main `05ca753` background-token integration dependency is unchanged.
