# P14 slice 2 developer handoff — web grammar and P15 review

Worktree: `/Users/german/Work/JS/polski-grammar-srs-worktrees/p14-practice-feedback`. Baseline: root checkpoint `e7b32b5`. No commit or push by developer.

## Behavior

- Shared grammar practice accepts first source-backed ChooseForm/WordBank choice or explicit Check of nonblank text. WordBank stores token ID and raw source text separately. Draft typing stays editable until Check; timed and preference OFF continue through the old reducer.
- The Biome practice policy receives choice/check and explicit-check facts. The existing `GrammarExamScorer` alone supplies grade status and quarter points. Dependent answer evaluation waits for both committed answers; a second commit recomputes both from the full attempt snapshot while retaining the first raw answer. PendingManual and final items have no auto timer.
- Web grammar locks every committed control with native disabled semantics, neutral authored note on return, status/live text and source answer. The cancellable 2000 ms timer follows the shared disposition. Review uses source-backed `grammarReviewRows` built from the reducer's submitted frozen answer map.
- The legacy B2 2020 PlayTest intentionally reselects bank answers; its fixture now switches feedback OFF in Settings. No scoring oracle changed.

## TDD chronology

- Focused common tests were authored before the new shared APIs, but their first compilation stopped at missing API symbols. This is a setup failure, not a behavioral RED.
- After initial shared/Web code draft and before building the new distribution, the natural browser text Check test ran against the pre-change distribution. It failed waiting for the missing authored “Проверить” button (45 s timeout). This is a real old-artifact behavior RED, but it was observed after the first production draft and is not claimed as the start of the TDD cycle.
- First new Chromium run: text and paired cases passed; bank and manual assertions failed on test expectations (`jeszcze` source casing, authored “Требуется ручная проверка”). Assertions were corrected; bank correct cue was also changed to compare normalized Polish text.
- Submitted review first run failed on an incorrect blank-label expectation (“Не отвечено”); authored “Ответ не введён” was used, then it passed.
- Legacy B2 conditional word bank failed with native disabled after first commit. Setting preference OFF in that legacy reselecting fixture restored its intended old mode; it passed.

## Checks

- PASS `./gradlew :shared:desktopTest --tests polski.exam.grammar.GrammarExamScorerTest --tests polski.exam.ExamPracticeFeedbackTest --tests polski.exam.ExamReviewRowTest --max-workers=2` from `kotlin/`; log `slice2-shared-tests.log`.
- PASS `./gradlew :composeApp:compileKotlinWasmJs :composeApp:compileKotlinJs --max-workers=2` from `kotlin/`.
- PASS `./gradlew :composeApp:composeCompatibilityBrowserDistribution --max-workers=2` from `kotlin/`; final rebuild log `slice2-final-build.log` (record final completion below).
- PASS `npm run typecheck` from worktree root.
- PASS `biome generate --check --project dsl/biome.project`; log `slice2-biome-check.log`. Toolchain pin stale warning is pre-existing; generated files compared byte for byte.
- PASS new 4 live grammar cases in Chromium, Firefox and WebKit on Wasm: 12/12. PASS new submitted review in all 3 engines: 3/3. PASS same 5 cases in Chromium forced JS fallback: 5/5.
- PASS feedback suite Chromium: 9 passed, 1 managed-tab hidden case skipped by explicit precondition. PASS owned Chrome 140 real-hidden fixture against this build: genuine `document.hidden=true` at 263 ms, status cleared, no stale advance after 2.4 s, neutral lock and selected answer retained. Evidence JSON/screenshots: `slice2-real-hidden/`.
- PASS 21/21 existing grammar DSL PlayTests Chromium after the targeted legacy OFF fixture change; log `slice2-legacy-grammar-playtests.log`.
- PASS `git diff --check`.
- NOT RUN native Apple/Android/desktop app UI, Reading/Listening practice, P16 Rive. These are outside this bounded handoff.

## Known boundary

The new shared helper is grammar-specific. Reading/Listening grouped controls and native consumers remain later slices. Managed Playwright tabs do not provide genuine hidden visibility; the owned Chrome fixture supplies that lifecycle evidence. No new scoring ladder was introduced.

Canonical handoff copy in Plans/Architecture. Original dated receipt and named logs: `../../../polski-grammar-srs-worktrees/recovery-logs/20261112/exam-learning-quality/p14/`. This copied receipt preserves the original chronology. Root froze the final browser artifact at `../snapshots/p14-grammar-controls-20261113` relative to this worktree root; sibling `.sha256.json` records actual entrypoint hashes.
