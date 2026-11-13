# P19 source-authored practice PlayTests — pre-merge verification

Date: 2026-11-13. Worktree HEAD `a705970449e00365c59928ec1eb5473877a39938` plus test-only changes. This is a focused P14/P15 browser gap check, not final merged P19 acceptance.

## Source receipts and contract

- Official local B1/2017 [`arkusz.pdf`](../../ExamBin/pl/B1/2017/arkusz.pdf), printed page 16: Grammar I/1 offers `dobry teatr`, `dobrego teatru`, `dobrym teatrem` in the sentence `dumni są z ...`. [`klucz.pdf`](../../ExamBin/pl/B1/2017/klucz.pdf), printed page 5: I/1 key is `dobrego teatru`.
- The same paper, printed page 9, Reading I/1 prints three options including `kioski internetowe są popularne w całym kraju` and `kioski internetowe oferują dostęp do Internetu`. The key, printed page 4, gives I/1 `c`.
- [Accepted P14–P16 contract](EXAM-PRACTICE-FEEDBACK-DESIGN.md): first wrong practice choice commits immutably, shows chosen and source values, advances after two seconds inside a grouped task; submitted report preserves the chosen wrong value beside the key.

The new `.playtest` files supply independent expected words and actions to the existing Biome `compilePlan`/`runPlaymodePlan` host. The TypeScript host adds only DOM read fields, natural pointer input for a disabled option, and Playwright virtual-clock controls. The browser spec installs the clock, selects actual JS/Wasm runtime, and records requested bundle scripts.

## Executed checks

Working directory: `/Users/german/Work/JS/polski-grammar-srs-worktrees/p14-practice-feedback`. Browser artifact: immutable `../snapshots/p14-grouped-readable-20261113`; `originWasmComposeApp.js` SHA-256 `ca8ca53877f6255fc41398fb02b00065be34de2f93ab1213025269a631516708`; `originJsComposeApp.js` SHA-256 `c8d2dbbaa01685bde80c7acef9f57bff5f98dba112423f163017c0867be0b48e`. The test observes the requested matching runtime script. No build was run against native development files.

Both browser commands use:

```sh
BIOME_DSL_TS_ROOT=/Users/german/Work/Python/biome-dsl/implementations/typescript \
KOTLIN_SPIKE_DIST=../snapshots/p14-grouped-readable-20261113 \
KOTLIN_SPIKE_BRANCH=<wasm|js> KOTLIN_SPIKE_PORT=<4222|4223> \
npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-exam.spec.ts \
  --project=chromium --grep 'DSL practice PlayTest'
```

| Check | Result | Evidence |
|---|---|---|
| Wasm Chromium: wrong grammar choice, ignored second pointer, 1999/2000 ms transition, no duplicate transition, frozen wrong/source report; grouped Reading answer and same-passage focus | **PASS 2/2** | `../recovery-logs/20261113/p19-dsl-feedback/p19-playtest-wasm-final.log` |
| JS fallback Chromium: same two DSL cases and actual JS request | **PASS 2/2** | `../recovery-logs/20261113/p19-dsl-feedback/p19-playtest-js-final.log` |
| `npm run typecheck` | **PASS** | Executed after additions; TypeScript project build exited 0 |
| `git diff --check` for owned test files | **PASS** | Exit 0 |
| Firefox, WebKit, final merged artifact, native Simulator, physical iPad | **NOT RUN** | Final P19 and native lanes remain separate |

Initial runs were harness failures, not product RED: the default relative `BIOME_DSL_TS_ROOT` resolves incorrectly from this worktree; setting the absolute existing reader path fixed import. The first authored labels for the grammar options were reversed, and the Reading case had entered task 2 using an unnecessary `next`; both were corrected from the printed source and actual UI. A first timing run let wall time elapse between actions; `pauseClock` now freezes the clock before the answer, matching the accepted virtual-clock pattern. A first runtime provenance assertion used Performance entries, which are empty here; the final test records actual browser requests. All final focused runs pass with zero retries.

No production files, old fixed expectations, native files, or Simulator state were changed. These tests supplement the developer's accepted web checks and do not establish an earlier RED→GREEN implementation sequence.
