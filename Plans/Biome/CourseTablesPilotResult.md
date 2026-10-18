# Stage 2 English curriculum table experiment

Status: **NO-GO for product integration** after independent testing and read-only review. The inspectable candidate is committed only on experiment branch `biome/curriculum-tables-exp` at `ed45e5f`, based on full-port commit `413e125ca4820b1feeb453c228ed0327842c4b34`. The full-port branch retains the original JSON as its authored source. Compiler checkout `/Users/german/Work/Python/biome-dsl` was `2ab6275d59496e113cd2a17caf0f75d1c63174d2`. No compiler or other course file was changed. Clean-CI compiler fetch and publication were NOT RUN.

The independent preconversion English curriculum is frozen in `tests/fixtures/en-curriculum-preconversion.json`, SHA-256 `4c8f1c21595479a1b49ebb38a6fa4df9fff15f02bbe6d6cdd2b8ed4ecaf0afe6`, before writing the `.tables` source. The candidate `courses/lang/en/curriculum.tables` has a `ROWS` header, exactly 16 data rows, seven explicit columns, and all prior ordering, nulls, string types, nested filters and prerequisites. `biome.project` pins the exact local commit and four real compiler source hashes. Its project step invokes the authored generator through a 17-line adapter; runtime consumers still read `curriculum.json`.

## Observed controls

| Control | Current local result |
| --- | --- |
| Independent test before source creation | RED: missing `curriculum.tables` (one selected test failed). |
| Project `--check` against original artifact | RED: stale. Original file used compact nested objects. |
| Changed `.tables` Person value with unchanged generated JSON | RED: stale, exit 1. |
| Invalid prerequisite, construction, noun ID in native JSON | RED: existing validators rejected each, with the offending path. |
| Invalid construction generated from `.tables` | RED: `validatePackV2` rejected `core.missing` at `/lang/en/curriculum.json/0/construction`. |
| Synthetic table row | PASS: Unicode, quoted text, pipe/bracket separators and numeric string preserved; malformed row rejected with `TABLES_ARITY` at line 3, column 1. |
| Frozen semantic comparison | PASS: all 16 complete objects deep-equal, including key order and seven present fields. |

The generated artifact is **not byte-identical**: original 5,010 bytes / 146 lines; generated 6,056 bytes / 318 lines. `git diff --numstat` for that path is 215 additions and 43 deletions. Differences are canonical pretty-printing of nested objects and arrays; parsed values and order are equal. The direct project and authored `--check` commands both report one file checked and PASS.

## Consumer evidence

Unless noted, cwd is `/Users/german/Work/JS/polski-biome-curriculum-exp` on the uncommitted candidate.

| Command | Result and actual scope |
| --- | --- |
| `node --test tests/course-curriculum-authored.test.mjs` | PASS, 1 test. |
| `node <pinned-compiler>/implementations/typescript/packages/biome/src/bin.mjs generate --project biome.project --check en-curriculum` | PASS, 1 generated file checked. |
| `node <pinned-compiler>/implementations/typescript/packages/biome/src/bin.mjs generate --authored --check courses/lang/en` | PASS, 1 authored file checked. |
| `npm run course:validate` | PASS: course pack, vocabulary journal, standalone Polish curriculum, pack v2. Its standalone `validate-curriculum.mjs` invocation defaults to Polish; it does not directly schema/DAG-validate the English curriculum. |
| `node scripts/validate-curriculum.mjs courses/lang/en/curriculum.json` | PASS: explicit English schema/DAG validation, independently rerun by tester. |
| `npm run typecheck` | PASS. |
| `npm test -- --run tests/pack-v2.test.ts tests/lifehacks-en-ru.test.ts` | PASS, 2 files / 34 tests. |
| `npm test` | PASS, 41 files / 268 tests. |
| `npm run build` | PASS: validators, TypeScript, Vite 51 modules. |
| `npx biome check changed`, cwd compiler `implementations/typescript` | Green but **zero changed files and zero projects checked**; this does not cover the external product source. The direct authored check above is its surface check. |
| `./gradlew :shared:desktopTest :shared:jsBrowserTest :shared:wasmJsBrowserTest --console=plain`, cwd `kotlin` | PASS: `BUILD SUCCESSFUL in 3m 5s`, 98 actionable tasks executed; all three named shared test tasks executed. Gradle also reported configuration-error checks SKIPPED and no-source resource tasks; those are not tests. |

Browser app interaction, Android/iOS/macOS application UI, physical devices and clean CI are NOT RUN. Kotlin shared tests, even if green, establish only their respective shared target runners.

The independent tester replayed the pinned project and direct authored freshness checks (one file each), the authored Node test (1/1), `npm run course:validate`, the explicit English curriculum validator above, and the focused pack-v2/lifehacks suite (34/34). The tester also checked frozen structural equality and reproduced stale-artifact and unknown-prerequisite failures. All those checks PASS or rejected the intended negative mutant. The tester did **not** rerun the developer's full 268-test suite, build, Kotlin tasks, Unicode/arity control, or other RED probes; their results above are developer execution evidence only.

## Paired maintenance probe and cost

Both arms started from restored baselines and changed the first skill's `fixed.Person` from string `"1"` to string `"2"`; the independent probe asserted the new literal `"2"`. Both then ran `validateCurriculum` and `validatePackV2`. The DSL arm edited one `.tables` cell, regenerated the JSON with the pinned project step, then validated: **851.9 ms** local elapsed. The native arm edited one JSON value and validated: **127.6 ms**. These are single warm wall-time observations including different generation work, not CPU, cold-start or LLM usage measurements. The invalid construction probe was caught in both arms. The original source required one human-authored file and one semantic edit site. The candidate still requires one semantic edit site, and adds a 19-line / 3,069-byte `.tables` source, a 6-line manifest, a 17-line adapter, the generated artifact and the freshness check. Combined authoring is 42 lines / 4,480 bytes against native JSON's 146 lines / 5,010 bytes, but 15 of the 19 table lines exceed 120 characters (maximum 264). Nested `focus`, `fixed` and filter values remain JSON-like cells. The parser rejects overlapping parent and child columns, while rows need both `focus:null` and nested focus plus `fixed:{}` and nested fixed; a simple pure-column form cannot express this without another mechanism. The canonical output expands to 318 lines; the independent test fixture adds 146 lines. This adds review surfaces across the candidate source, wrapper, manifest, fixture and output; actual reviewer time was not measured.

A second paired probe added `en:verb.presentSimple` to `en:role.location`'s prerequisites, asserted the two-item list, and ran the same two validators. The DSL edit, generation and validation took **820.2 ms**; the native JSON edit and validation took **117.1 ms**. Then each arm changed that new reference to `en:missing`: the DSL arm regenerated and `validateCurriculum` rejected `/4/prerequisites/1` in **646.8 ms**, while the native arm rejected the same path in **0.6 ms**. Both arms were restored to the original baseline in `finally` blocks. These measurements are single warm wall-time observations, not a performance benchmark. Both edits touched one semantic source site in each arm; both invalid references were caught by the existing validator.

This does not demonstrate fewer independent edit sites or stronger reference validation than the existing native source and validators. Under the plan's stop rule and biome principles P6/P8/P13, the measured candidate is **NO-GO for integration**. P7/P11/P15/P16 hold inside the isolated candidate because the JSON is generated, the frozen expectation remains independent, and stale output is rejected. Independent testing confirmed the focused checks and two negative controls; read-only review found no semantic regression and rejected integration on maintenance cost. The candidate commit remains on its experiment branch; no generated curriculum or `.tables` source was merged into the full-port branch.
