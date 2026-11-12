# Independent acceptance: adult B2 June 2022 withdrawal

2026-11-12. Contract: [withdrawal plan](P9-B2-2022-06-WITHDRAWAL-PLAN.md), A1–A5. Tested tree: `HEAD 5b8a0013f3fbf7eb664b8c4d8719afcae650e216` plus the uncommitted withdrawal diff in `/Users/german/Work/JS/polski-grammar-srs`. This is an independent source and test audit; developer-run results are identified separately.

| Criterion | Result | Evidence |
|---|---|---|
| A1, scoped removal | PASS | `git diff --name-only -- exams/pl/certyfikat-polski` lists only the 14 files under `B2/2022-06/`. All 14 are absent in the working tree. Every tracked file in `B1/2022-06` (8), `B2/2022-02` (14), `C1/2022-03` (12) and `C1/2022-11` (15) is byte-identical to `HEAD`. |
| A2, active publication | PASS for source/generator boundary | `dsl/biome.project`, `dsl/exam-published.core-data`, `dsl/exam-assets/exam-audio.core-data`, and generated `ExamRows.kt` are byte-identical to `HEAD`. Publication and audio manifests each have 16 records; no withdrawn ID occurs there or in `dsl/exam-published/generated/` or generated published Kotlin rows. Existing negative tests in Vitest, `ExamCatalogTest.kt`, and Playwright remain in place. Actual browser/catalog runtime is a separate check. |
| A3, forward facts and word-bank fixture | PASS | Independent focused Vitest passed 21/21, including the unchanged fact-presence loop over seven retained P9 papers and its new count of 17. The only word-bank web test change replaces two synthetic IDs; its content and assertions are unchanged. Developer's completed JS and Wasm browser test XML each report 1 test, 0 failures. |
| A4, generation and targets | PASS for affected checks | Independent focused Vitest: PASS. `git diff --check`: PASS. Developer's `biome generate --check --project dsl/biome.project` log reports 16 publication and 16 audio rows with byte comparison; JS and Wasm focused Gradle browser tasks completed successfully; `npm run typecheck` log shows `tsc -b` with no diagnostics. Playwright full UI and physical devices: NOT RUN for this withdrawal. |
| A5, current status/provenance | PASS | ADR-57 and current P9 status state ten published P9 papers and one owner-withdrawn draft, with historical source-recovery links. No claim that the withdrawn paper's listening is complete. |

## Reproduction and archive integrity

All commands below ran in `/Users/german/Work/JS/polski-grammar-srs`.

```sh
BIOME_BIN=/Users/german/Work/Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs \
BIOME_DSL_TS_ROOT=/Users/german/Work/Python/biome-dsl/implementations/typescript \
npx vitest run tests/exam-published-generation.test.ts tests/exam-audio-assets.test.ts
```

Result: Vitest 4.1.11, 2 files PASS, 21 tests PASS, 33.08 s. This independently reran the developer's earlier 21/21 result; it does not establish the developer's RED phase. The test uses the public Biome source reader in `discoverSources`, checks accepted-fact identity for each qualifying question, and asserts the remaining total of 17. The exclusion cases remain in `tests/exam-published-generation.test.ts`, `tests/exam-audio-assets.test.ts`, `kotlin/shared/src/commonTest/kotlin/polski/exam/ExamCatalogTest.kt`, and `tests/browser/kotlin-exam.spec.ts`.

A Python read-only comparison loaded `../polski-grammar-srs-worktrees/recovery-logs/20261112/p9-withdrawal/withdrawn-source-draft-manifest.json`, enumerated the 14 paths from `git ls-tree -r HEAD`, and compared each archived file with `git show HEAD:<path>`, its manifest byte count and SHA-256. All 14 matched. It also compared every retained 2022 source file and the four active-boundary files above with `HEAD`. No remaining 2022 source differed. The archive is outside active inputs.

The ignored `ExamBin/pl/B2/2022-06/{arkusz.pdf,audio.mp3,klucz.pdf}` files remain present. Their size and SHA-256 match the three pins in [P9 source provenance](P9-SOURCE-PROVENANCE.md): `ec4c01f8befcb656bbd0ef38a235290fc8aca4bd53d4ec90fbce3901123c4a4c`, `171c36bfa5304217ea9c0e4e40d99ba5b4905da4ad41dba2a9adaa0f049ff57b`, `ea3f271e9cc016546b3cd16085946e952b0acb57d379df7567f5ea5b2da6cfb0`. The MP3 pin identifies the retained damaged received bytes, not complete media.

Developer-run logs in `../polski-grammar-srs-worktrees/recovery-logs/20261112/p9-withdrawal/`: `biome-generate-check.log`, `npm-typecheck.log`, `reading-word-bank-js-browser.log`, `reading-word-bank-wasm-browser.log`. The Biome check emits `TOOLCHAIN_PIN_STALE` for the neighboring checkout but continues byte-for-byte comparison and reports no generation problem. Gradle 9.3.1 logs show `:composeApp:jsBrowserTest` BUILD SUCCESSFUL in 1m 4s and `:composeApp:wasmJsBrowserTest` BUILD SUCCESSFUL in 1m 5s. I inspected `kotlin/composeApp/build/test-results/{jsBrowserTest,wasmJsBrowserTest}/TEST-*.ReadingWordBankWebTest.xml`: each has `tests="1" skipped="0" failures="0" errors="0"` on ChromeHeadless 157.0.0.0. These are attributed to the developer, not rerun by this tester. The task outputs prove the focused test in a managed browser; they do not prove full app Playwright acceptance or a physical device.

No regression test or production file was changed by the independent tester. The only tester-authored file is this evidence report.
