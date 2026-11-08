# Scheduler expanded oracle: P2 evidence

Date: 2026-11-08. Scope: [NEXT-SESSION-EXECUTION.md](NEXT-SESSION-EXECUTION.md), P2. No scheduler production code or FSRS version changed.

## Source and coverage

- `dsl/scheduler-expanded/scheduler.core-data` is the active, authored oracle: 70 cases with IDs, captured `at`, rating, fuzz setting, all source-card fields, four preview due times, and all reviewed-card fields. The contract adds `fuzzEnabled: bool` because the pinned cases cover both fuzz modes. `dsl/biome.project` prints a commonTest-only typed Kotlin row list; the generated file was not hand-edited.
- Provenance: installed `ts-fsrs` 5.4.2 BasicScheduler; baseline revision `df59774e153a5bdf590fe27bd8780c7bd5457a26`; original pinned `expanded_oracle.json` SHA-256 `1075eeff7a3b0c0a3e8202a16bdda4f2aeb27e5591d737344739921a0bf4fb4e`. Migration copied expectations from that pinned fixture/current 70-case test, never from the current `FsrsScheduler` output. A one-off field-by-field audit compared all 70 DSL records to all 27–28 fields of their pinned fixture cases, including IDs and expected values: PASS. Original Kotlin test had 70 matching IDs and reviewed due values.
- `SchedulerExpandedTest.kt` is 85 lines, down from 1,144. For each record it retains four preview comparisons, ten reviewed-card field comparisons, source immutability, and selected-preview/reviewed-due equality. Stability and difficulty retain absolute tolerance `1e-7`. Invalid rating and card-state indexes are rejected before `entries[index]`.
- `generate_expanded_kotlin.py` was removed because it could overwrite the new DSL-backed test. `expanded_oracle.json` and `generate_expanded.mjs` remain historical provenance, not the active Kotlin oracle.

## Executed checks

| Check | Command and working directory | Result |
|---|---|---|
| DSL formatting | `node /Users/german/Work/Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs fmt dsl/scheduler-expanded/scheduler.core-data` (repo root) | PASS; canonical format |
| DSL generation and stale check | `node /Users/german/Work/Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs generate --project dsl/biome.project`; same command with `--check` (repo root) | PASS; 70 scheduler rows, 0 contract problems, byte-for-byte generated check. Existing stale toolchain pin warning remains; no pin update. |
| Focused Kotlin test | `./gradlew :shared:desktopTest --tests 'polski.srs.SchedulerExpandedTest' --console=plain` (`kotlin/`) | PASS after migration, and PASS after negative-control restoration |
| Negative control | Temporarily changed `on-new-1` DSL `expDue` from `12:01` to `12:02`, generated only the scheduler typed row with `biome data generate`, then ran the same focused Gradle test | RED observed: `on-new-1 due expected:<2026-02-03T12:02:00Z> but was:<2026-02-03T12:01:00Z>`. Restored source and regenerated; focused test PASS. |
| Diff whitespace | `git diff --check` (repo root) | PASS |

JS/Wasm tests, browser scenarios, devices, and full shared test suite: NOT RUN for this P2 slice. The desktop target proves the commonTest consumer on JVM only. Independent tester and reviewer gates remain open.

## Independent verification, 2026-11-08

The GPT-6 Sol tester compared the 70 authored DSL records independently against the frozen `expanded_oracle.json`: all 27 mapped fields per case match, all case IDs occur in the old HEAD test, zero mismatches. The tester reran `./gradlew :shared:desktopTest --tests 'polski.srs.SchedulerExpandedTest' --console=plain` from `kotlin/`: PASS. The XML reports one nonempty `allCases[desktop]` test iterating the 70 generated rows, zero failures. JS/Wasm scheduler tests remain NOT RUN. The preceding developer evidence is preserved as a separate run.

Independent GPT-6 Sol read-only review: **APPROVED**, no blockers in P2 and the browser storage helper slice. Review confirmed fixed oracle provenance, retained comparisons and enum guards; no tests were executed by the reviewer. Exam changes and the pending reference-table correction were outside this verdict.
