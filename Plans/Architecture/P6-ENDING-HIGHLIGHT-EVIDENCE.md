# P6: ending highlight literal oracle

Plan: [NEXT-SESSION-P5-P8-PLAN.md](NEXT-SESSION-P5-P8-PLAN.md), P6. Implementation date: 2026-11-08. This is a source and test handoff; independent acceptance and review were completed by the parent workflow after the correction below.

## Source and coverage

- `dsl/ending-highlight.data-contract` defines typed case and ordered part records. Biome reads 34 cases from `dsl/ending-highlight/cases.core-data` and 93 literal expected parts from `dsl/ending-highlight/parts.core-data` (127 rows). Separate source files are required because `biome data check` applies each `--rows` record binding to every row in its source file.
- The cases retain the prior test's ending, replacement, sentence, style, before/after, punctuation, substring boundary, declared and absent stem alternation, generic alternation, insertion/deletion, future/aspect auxiliary, zero-ending, and unchanged/free-prose expectations. The old 23 test methods and 65 assertion call sites are represented by the literal rows plus five retained integration/property methods. The aspect auxiliary before-side unchanged result has its own case.
- Independent review found a distinction in the old generic alternation negative case: it called `changeHighlightParts` with the default alternations. `generic-no-alternation` now omits `noAlternations` and uses that default; only `missing-stem-declaration` supplies an explicit empty alternation list. The focused JVM test and full generator check were rerun after this correction.
- The Kotlin dispatcher compares each full ordered `EndingPart` list, consecutive ordinals, concatenated source text, and side roles. The active course pack default alternation stays an integration assertion in Kotlin. `ContrastPair.parts` invalid explicit parts, mixed punctuation insertion, full future comparison, and unchanged pair also remain direct assertions.
- Generated `EndingHighlightRows.kt` is emitted by Biome into `commonTest/generated`; no generated Kotlin was edited by hand. No production implementation changed.
- The handwritten Kotlin test shrank from 244 to 93 lines, but the new contract, DSL sources, and test total 1,140 author lines (23 + 245 + 779 + 93). This is a typed-oracle migration with a larger source footprint, not an overall line-count reduction.

## Executed checks

All commands run from repository root unless noted.

| Check | Command / observation | Result |
|---|---|---|
| Format | `node /Users/german/Work/Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs fmt dsl/ending-highlight/cases.core-data dsl/ending-highlight/parts.core-data` | PASS: 2 files, 0 problems |
| Focused generation | `node /Users/german/Work/Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs data generate --contract dsl/ending-highlight.data-contract --rows HighlightCaseRow dsl/ending-highlight/cases.core-data --rows HighlightPartRow dsl/ending-highlight/parts.core-data --target kt --kt-package polski.presentation.data.oracle --out kotlin/shared/src/commonTest/generated/kotlin/polski/presentation/data/oracle/EndingHighlightRows.kt` | PASS: 127 rows, 2 records, 0 problems; wrote Kotlin rows |
| Full project drift | `node /Users/german/Work/Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs generate --check --project dsl/biome.project` | PASS on final checkpoint (exit 0). Tool printed `TOOLCHAIN_PIN_STALE`; it still compared generated files byte for byte. |
| Focused JVM | `./gradlew :shared:desktopTest --tests polski.presentation.EndingHighlightTest --console=plain --max-workers=2` from `kotlin/` | PASS after independent review correction: 6 tests, 0 failures, 6-second build. |
| Canonical DSL format | `node /Users/german/Work/Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs fmt --check dsl/ending-highlight/cases.core-data dsl/ending-highlight/parts.core-data` | PASS: 2 files, 0 not canonical, 0 problems |
| Behavioral negative control | Temporarily changed first expected part `moj` to `moja`, regenerated, then ran the same focused JVM command | RED: 24 tests, 1 failure at `literalOraclePreservesOrderedFragmentsAndRoles`; restored the literal and regenerated, then focused JVM GREEN |
| Shared browser tests | `./gradlew :shared:jsBrowserTest :shared:wasmJsBrowserTest --console=plain --max-workers=2` from `kotlin/` | NOT RUN to completion: Karma stalled during `jsBrowserTest`; interrupted to free Gradle for the exam build. No browser PASS claimed. |
| Whitespace | `git diff --check` | PASS |

The negative control was intentionally executed after implementation to prove test sensitivity; it is not evidence of a test-first RED. Browser verification remains an independent target check.

Independent acceptance after correction: old assertion mapping H1–H3 **PASS**; fresh `:shared:desktopTest --tests polski.presentation.EndingHighlightTest --rerun-tasks --console=plain --max-workers=2` **6/6 PASS**, 26 tasks executed; fresh full Biome generation check **PASS**. Independent reviewer **APPROVED** this P6-only slice. No browser PASS is claimed.
