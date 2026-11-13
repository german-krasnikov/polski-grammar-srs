# P14 Reading/Listening web developer evidence

2026-11-13. Bounded implementation of [accepted grouped practice addendum](READING-LISTENING-PRACTICE-ADDENDUM.md) in isolated `p14-practice-feedback` worktree, based on `d42b47d`. Developer did not commit, push, or edit Main. Recovery logs and owned-Chrome JSON/screenshots are under `/Users/german/Work/JS/polski-grammar-srs-worktrees/recovery-logs/20261112/exam-learning-quality/p14/`.

## Observed RED before production edits

Two executable natural-input Playwright cases were added to `tests/browser/kotlin-grouped-practice-feedback.spec.ts` and run against frozen `../snapshots/p14-grammar-controls-20261113` before any production file was edited. Command from worktree root:

`KOTLIN_SPIKE_DIST=../snapshots/p14-grammar-controls-20261113 KOTLIN_SPIKE_BRANCH=wasm KOTLIN_SPIKE_PORT=4206 npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-grouped-practice-feedback.spec.ts --project=chromium --workers=1`

**RED 2/2:** source B1 2017 Reading first passage wrong natural click reached the published question, but the row had no `role=status` chosen/correct feedback. Source B1 2019 Listening first task played actual distributed audio, then wrong natural click reached its question but had no row status. The missing behavior was observable in the old browser artifact; no missing-API compile failure was counted as RED. The two cases are GREEN on the final artifact.

## Delivered behavior and owners

- Shared `acceptReadingPracticeInput` and `acceptListeningPracticeInput` validate the current passage/task question, first complete answer, option set slot count, and explicit Check for open text. They use the existing Biome `Policy.disposition` and official `ReadingExamScorer`/`ListeningExamScorer`. No new scoring ladder or source oracle changed. `advanceGroupedPracticeFocus` finds the next uncommitted question in the current group, wrapping to an earlier gap. No callback dispatches passage/task `Next` or `Submit`.
- Shared `readingReviewRows` and `listeningReviewRows` project chosen answer, canonical and accepted variants, evaluation/quarter points, and blank from submitted frozen maps.
- Web Reading/Listening controllers own feedback state, one cancellable 2000 ms timer, current-group focus, native disabled locks, neutral return notes, status/live text and partial-credit styling. They cancel stale callbacks on navigation, a new answer/draft, hidden, reset, route close, or teardown. Preference OFF and timed paths keep the existing editable reducer.
- Listening answer/grade/focus updates only a question row. The same `<audio>` node stays connected and playing without seek, pause, or redraw. Explicit task Next still seeks the new source segment and pauses as before.
- `ExamWebController` passes the live preference lambda to both standalone controllers and closes Reading on back navigation.

## Verification on final source/artifact

Final compatibility distribution: Wasm loader SHA-256 `ca8ca53877f6255fc41398fb02b00065be34de2f93ab1213025269a631516708`; JS loader SHA-256 `c8d2dbbaa01685bde80c7acef9f57bff5f98dba112423f163017c0867be0b48e`.

| Gate | Result |
| --- | --- |
| `./gradlew :shared:desktopTest --tests polski.exam.ExamGroupedPracticeFeedbackTest --tests polski.exam.ExamPracticeFeedbackTest --tests polski.exam.ExamReviewRowTest --tests polski.exam.reading.ReadingExamScorerTest --tests polski.exam.listening.ListeningExamScorerTest --max-workers=2` from `kotlin/` | **PASS**; `grouped-shared-tests.log`. A first test fixture failed because it constructed a MultipleChoice question without options; corrected to source-valid ShortAnswer, then passed. |
| `./gradlew :composeApp:compileKotlinWasmJs :composeApp:compileKotlinJs --max-workers=2` from `kotlin/` | **PASS**. A small web interop helper reads actual `document.hidden`; the visibility event invalidates the generation and each timer also checks the browser fact. No hidden property is overridden. |
| `./gradlew :composeApp:composeCompatibilityBrowserDistribution --max-workers=2` from `kotlin/` | **PASS**; `grouped-final-build.log`, 16 audio assets verified. |
| `biome generate --check --project dsl/biome.project` from worktree root with neighboring `BIOME_DSL_TS_ROOT`/CLI | **PASS**; `grouped-biome-check.log`. No DSL or generated file was edited in this slice. |
| `npm run typecheck`; `git diff --check` from worktree root | **PASS**. |
| New grouped browser cases on final Wasm distribution, Chromium/Firefox/WebKit, one worker, zero retries | **PASS 30/30** excluding 41-second media case; `grouped-final-three-engines.log`. Covers 1999/2000 ms focus, same group/wrap, real source wrong/correct text, true/false, matching, unordered partial, open Check, final stay, manual Next, return locks, submitted wrong/blank reviews, OFF editing, and stale second-answer cancellation. |
| Same ten cases forced to JS fallback, Chromium | **PASS 10/10**; `grouped-final-js-chromium.log`. |
| Real distributed B1 2019 audio stays playing beyond 41 seconds after answer/focus, same node connected, currentTime advanced >41 s, same task | **PASS** on final Wasm Chromium, 45.9 s case; `grouped-final-long-audio.log`. |
| Genuine hidden tab in owned Chrome 140 with Playwright client detached, real CDP tab activation, `document.hidden=true`, then return | **PASS** both Reading and Listening on the final artifact; `grouped-final-real-hidden-reading/real-hidden.json` and `grouped-final-real-hidden-listening/real-hidden.json`. Feedback cleared, no stale advance after 2.4 s hidden, chosen option locked on return. Reading hid 129 ms after click; Listening hid 212 ms after click. Two earlier Reading attempts against the preceding artifact failed the fixture's 1900 ms *timely hidden transition* precondition (observed 35.8 s during parallel Chrome runs, then 4.3 s); neither is counted as a product PASS or as a product behavior failure. |
| Existing relevant source DSL PlayTests Reading/Listening, excluding separate audio-boundary/replay/40-second scripts | **PASS 17/17**; `grouped-legacy-playtests.log`. Existing timed B1 2017 full part PlayTest **PASS 1/1**. Grammar feedback Chromium regression **PASS 11, SKIP 1** managed-tab hidden precondition; real hidden is covered above. |

## Scope left for independent gates

This handoff is Web Reading/Listening and shared projections only. Native bridge/SwiftUI/Simulator, Android exam UI, Desktop feature work, and P16 Rive work are **NOT RUN** here. The managed Playwright tab's hidden precondition remains skipped because it does not expose a real hidden document; owned-Chrome fixtures supply genuine visibility evidence. Tester should independently replay both parts, including the source audio lifetime and grouped focus, before reviewer assessment.

## Visual correction after independent verification

The independent tester's [grouped report](P14-READING-LISTENING-INDEPENDENT-VERIFICATION.md) found Light-theme contrast failures in committed feedback options and frozen review text. The retained `remain readable` browser case was RED on frozen `../snapshots/p14-grouped-practice-20261113`. A new source-backed grammar case was also run on that frozen artifact before CSS edits: Light committed wrong option **2.95:1**, below the existing 4.5:1 normal-text criterion (`grouped-grammar-contrast-red.log`).

Only `kotlin/composeApp/src/webMain/resources/exam.css` changed in production for this correction. `.exam-option:disabled` now preserves full text opacity during visible feedback and neutral return locks, overriding the global `button:disabled` dimming. Wrong text uses existing `--before`, official correct text uses `--green`, and partial credit uses `--text`, so partial is not colored as full credit. Inputs retain readable text when locked; native `disabled`/`aria-disabled`, selected border/background, and noninteractive cursor remain. No grading, timer, audio, preference, or data code changed.

The first post-edit browser run failed because the global `button:disabled` rule still affected options while feedback was visible; the override was moved from `.locked` to `:disabled`. The first JS contrast run then failed its loader precondition: the test installed its JS fallback script after it had already loaded Settings. The test now installs that script before initial navigation. Neither run was counted as a visual PASS.

Final compatibility distribution loader SHA-256 remained Wasm `ca8ca53877f6255fc41398fb02b00065be34de2f93ab1213025269a631516708` and JS `c8d2dbbaa01685bde80c7acef9f57bff5f98dba112423f163017c0867be0b48e`; entry `composeApp.js` is `cb180f5c21a6756fde901f3bd13a2be73cb5402528213e08b2e23e7b078e2ea8`. Corrected `exam.css` SHA-256 is `eba8438c1349eab5e8e8a4ebd132ee02ef5f7df53f6a05351b1135d3427f1585`. This exact compatibility artifact was tested.

| Correction check | Result |
| --- | --- |
| `./gradlew :composeApp:composeCompatibilityBrowserDistribution --max-workers=2` from `kotlin/` | **PASS**; `grouped-visual-final-build.log`. |
| Source-backed Light/Dark grouped and grammar contrast tests on corrected Wasm, Chromium | **PASS 4/4**; `grouped-visual-wasm-chromium.log`. Light wrong/correct options minimum **6.43:1**, review official source **5.78:1**, partial credit **13.00:1**, grammar neutral return lock **12.51:1**. Dark minimum among measured options **7.97:1**. |
| Same four cases on corrected Wasm Firefox/WebKit | **PASS 8/8**; `grouped-visual-wasm-firefox-webkit.log`. |
| Same four cases on corrected JS fallback Chromium | **PASS 4/4**; `grouped-visual-js-chromium.log`. |
| `npm run typecheck`; `git diff --check` | **PASS**; `grouped-visual-typecheck.log`. |

The previously independent **PASS** functional matrix, >41-second same-node audio, and genuine hidden fixtures target the same Kotlin/JS/Wasm loader hashes; this correction changes only CSS and browser contrast test setup. Independent visual recheck and full Web review remain the next gates. All logs are in the recovery directory named above; no files were committed or pushed by the developer.
