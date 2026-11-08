# P10 implementation evidence — six published exam controls

Date: 2026-11-08. Isolated draft worktree `/tmp/polski-exam-controls-worktree`, based on `1e28907`. Integration and code review remain root-agent gates. This file records developer checks and the subsequent independent tester findings for this draft.

## Source and behavior

The eight grammar word banks are transcribed as one `GrammarBankTokenRow` per printed occurrence: B1/2017 VI; B1/2019 II and VIII; B1/2020 II and VIII; B2/2017, 2019 and 2020 II. They total 73 tokens. B2/2019 and B2/2020 II each print `na` twice; B1/2020 VIII prints `w` twice. Source page checks used the local `ExamBin/pl/{B1,B2}/{2017,2019,2020}/arkusz.pdf` files; these assets are ignored and are not included in the diff. PDFKit text extraction checked all eight bank lines; visual PDF thumbnails checked the underlined question targets on B1/2017 pp. 17–18 and B1/2019–2020 p. 18. The B1/2017 IV/2 target is exactly `z tym nowym`, excluding `studentem`.

`word_bank` selects a specific printed token but stores its former raw text answer. Duplicate printed occurrences have distinct IDs and accessible labels. Navigation retains the selected occurrence. The scorer still receives the full answer snapshot and the existing narrow B2/2020 II/6→II/8 `onlyIfNotUsedBy` condition; no task-wide depletion rule was added. Per-item bank options were removed after the bank control was verified. Question formation and word formation remain typed Unicode input, fragment gaps select printed labels, and listening matching preserves unordered-set cardinality and quarter credit. Reading/listening option-bearing controls reject missing options with source IDs; unknown tags still throw. The B1/2020 binary TAK/NIE source fix from the earlier restoration (`341238b`) remains present and browser-verified.

The independent DSL oracle is `dsl/exam-scoring/bank-tokens.core-data` plus `question-controls.core-data`. It freezes all eight printed lists, duplicate counts, underlined targets, representative raw payloads and full/partial/wrong/pending quarter outcomes. Existing `word-bank-constraint.core-data` freezes B2/2020 II/6+II/8 normalized pair outcomes. Kotlin common tests and browser PlayTests consume these rows or separate source-grounded `.playtest` scenarios. Test rows were added after implementation in this draft; the initial failing old mapper tests were an API-update regression, not a behavioral RED claim.

The independent tester added `missingChoiceOptionsFailBeforeExamOpens` and a B2/2017 word-formation browser scenario. The new constructor test produced a behavioral RED: a `ChooseForm` item with no options was accepted, so the web layer would fail later with only the item ID. `GrammarTest` now checks nonempty, nonblank choice options and reports both test and item IDs before opening the task. The developer reran that same case GREEN before affected checks. The tester also reported source PASS for all eight banks/73 occurrences and 15 underlined targets, three focused Chromium browser cases PASS, and eight focused Firefox/WebKit control cases PASS. These independent results were handed to the root agent; their exact command lines are not recorded in this isolated developer log.

## Checks

| Area | Command and working directory | Result |
|---|---|---|
| DSL generation | `node /Users/german/Work/Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs generate --project dsl/biome.project` from worktree root | PASS |
| DSL generated check | Same tool with `generate --check --project dsl/biome.project` from worktree root | PASS; toolchain pin warning is pre-existing; byte comparison passed |
| Shared desktop tests | `./gradlew :shared:desktopTest` as part of combined run from `kotlin/` | PASS |
| Shared Wasm browser tests | `./gradlew :shared:wasmJsBrowserTest` as part of combined run from `kotlin/` | PASS |
| Shared JS browser tests | `./gradlew :shared:jsBrowserTest` as part of combined run from `kotlin/` | FAIL: 1 timeout in unrelated `EnEmphasisContractTest` among 400 tests; focused rerun `--tests 'polski.core.EnEmphasisContractTest'` PASS 5/5 |
| Web model browser tests | `./gradlew :composeApp:wasmJsBrowserTest` from `kotlin/` | PASS 30/30, including missing-option validation |
| Web model JS browser tests | `./gradlew :composeApp:jsBrowserTest` from `kotlin/` | FAIL: 3 timeouts in existing `TrainingStoreTest` among 30 tests; focused class retry failed when Karma disconnected before its 2-second reconnect deadline. JS compile passed; this lane is unresolved. |
| Final production Wasm distribution | `./gradlew :composeApp:wasmJsBrowserDistribution` from `kotlin/` | PASS; `verifyWasmJsExamAudio` also PASS 6 assets |
| TypeScript typecheck | `npm run typecheck` from worktree root | PASS |
| Final Kotlin JS compilation | `./gradlew :composeApp:compileKotlinJs --no-daemon` from `kotlin/` after the target-mark edit | PASS |
| Audio asset preparation | `npm run exam:audio:prepare -- --offline` from worktree root | PASS, 6 hash-checked assets |
| Browser acceptance, first immutable Wasm artifact | `npx playwright test --config=playwright.kotlin.config.ts tests/browser/kotlin-exam.spec.ts --project=chromium` with `KOTLIN_SPIKE_DIST=/tmp/p10-immutable-wasm`, `KOTLIN_SPIKE_BRANCH=wasm`, `BIOME_DSL_TS_ROOT=/Users/german/Work/Python/biome-dsl/implementations/typescript` from worktree root | PASS 46/46; artifact predates final bank-option removal and target correction |
| Browser acceptance, corrected source artifact | Same command with `KOTLIN_SPIKE_DIST=/tmp/p10-final-wasm-1e28907` | PASS 48/48; artifact predates only the final `<mark>` and CSS presentation edit |
| Browser acceptance, final marked artifact | Same command with `KOTLIN_SPIKE_DIST=/tmp/p10-final-marked-wasm-1e28907` and `--grep 'question target'` | PASS 1/1; asserts semantic `<mark>`, exact `widelcem`, typed Unicode answer, navigation return and quarter score |
| Tester blocker RED | `./gradlew :shared:desktopTest --tests 'polski.exam.QuestionControlSourceTest.missingChoiceOptionsFailBeforeExamOpens' --no-daemon` from `kotlin/`, before constructor fix | FAIL 1/1: expected `IllegalArgumentException`, none thrown; `/tmp/p10-choice-red.log` |
| Tester blocker GREEN | Same focused command after constructor fix | PASS 1/1; `/tmp/p10-choice-green.log` |
| Post-fix affected checks | `./gradlew :shared:desktopTest :shared:compileKotlinJs :shared:compileKotlinWasmJs :composeApp:compileKotlinJs :composeApp:compileKotlinWasmJs --no-daemon` from `kotlin/` | PASS; 406/406 desktop tests and all four compilations; `/tmp/p10-choice-affected.log` |
| Post-fix Wasm distribution | `./gradlew :composeApp:wasmJsBrowserDistribution --no-daemon` from `kotlin/` | PASS; audio distribution check PASS 6 assets; `/tmp/p10-choice-wasm-dist.log` |
| Post-fix browser controls | Same Playwright command with `KOTLIN_SPIKE_DIST=/tmp/p10-choice-final-wasm-1e28907` and `--grep 'conditional word bank|question target|derived word'` | PASS 3/3 on the final artifact; `/tmp/p10-choice-playwright.log` |
| Desktop/iOS/Android devices | Not in P10 scope | NOT RUN |

Final immutable artifact after the tester blocker fix: `/tmp/p10-choice-final-wasm-1e28907` (copied and made read only after the successful distribution). `composeApp.js` SHA-256 `71941bb399caa96498ceaa420bc14058e13a9ae6a0bfd5700591ea2a47535694`; generated application Wasm `3b6a66971ed9ff492c33.wasm` SHA-256 `b0c3fa66945b1e1cad7cb45375278933c50bd5a8d739a9c8cd4de30cd04b0507`. `npm run exam:audio:check-dist -- /tmp/p10-choice-final-wasm-1e28907` passed. The complete 48-case suite was not rerun after the localized constructor validation; three affected grammar browser cases passed on the exact final artifact. The prior full 48/48 result remains attached to the earlier immutable artifact.

## Independent review and main integration

Independent reviewer: APPROVED after the tester-found missing-options constructor defect was corrected. Tester independently reran the exact prior RED case (1/1 PASS) and Wasm web validation (2/2 PASS), and verified the final isolated artifact hash. No Critical/Major finding remains.

After applying the reviewed slice to main (including P8 renderer extraction and measured runner limit), `biome generate --check`, `npm run typecheck`, all shared Desktop tests **406/406**, JS/Wasm web compilation and compatibility distribution PASS. Fresh integrated artifact `/tmp/polski-p10-integrated-20261108-dist`: loader `composeApp.js` SHA `cb180f5c21a6756fde901f3bd13a2be73cb5402528213e08b2e23e7b078e2ea8`; application `c9fcb2abf1b82c73af43.wasm` SHA `681c6f6e6b1df6faa42cd22a0ffe3bf3191f7fdb4da026336d7391e88acde7b3`; `exam.css` SHA `83fd445be2fd383660a401685b10f530d4fedff9f464cb9ef6439cfa48b734a1`. A loader hash alone does not identify changed Wasm application code.

Full integrated Chromium/Wasm exam Playwright suite: **49/49 PASS**, no retries, 3.1 minutes. Command: `KOTLIN_SPIKE_DIST=/tmp/polski-p10-integrated-20261108-dist KOTLIN_SPIKE_BRANCH=wasm BIOME_DSL_TS_ROOT=/Users/german/Work/Python/biome-dsl/implementations/typescript npx playwright test --config=playwright.kotlin.config.ts tests/browser/kotlin-exam.spec.ts --project=chromium --output=/tmp/polski-p10-main-browser-results --reporter=line`. Logs: `/tmp/polski-p10-main-{generate-check,typecheck,integration,browser}.log`. Additional combined shared/composeApp JS/Wasm unit-browser rerun is in progress and is not included in this PASS claim.
