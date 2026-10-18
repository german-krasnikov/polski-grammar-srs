# NJ02 Kotlin core registry: selective product integration

Status: **bounded first slice on `experiment/nj02-registry-selective-integration`; NJ02 and the no-JSON cutover remain open**. This branch starts from canonical `biome/full-port-2026@c574f7814c96fa4d7ae3ee667772d492a4862a0d`. The implementation is selected from `experiment/nj02-kotlin-live-registry-dev@9ef04a1`; the independent behavior/freshness test was `test/nj02-kotlin-live-registry-independent@32c7a74`. The preimplementation R5–R9 freeze is [NJ02KotlinRegistryOracleFreeze.md](NJ02KotlinRegistryOracleFreeze.md), and the independent 14-value expectation is [NJ02RegistryIndependent.cases](NJ02RegistryIndependent.cases). This integration does not cherry-pick the broad experiment's 18 commits, English three-skill probe, TypeScript printer, mini Gradle project, generated tracked files or comparator scaffolds.

## Exact authority and generated output

The new diff before this receipt is 16 paths, +750/−20 lines. Four authored Biome files are the only new registry authority; their complete SHA-256 values equal the frozen source:

| Authored input | SHA-256 |
| --- | --- |
| `courses/core/registry.core-data` | `8e4d578675378195ca07a04a6615a9f6c3136d965d7505acf556a181d426d83d` |
| `courses/core/registry.construct` | `67e552fc42a98bf5bda01ed0a76f3efaa9885a81ad78e2295a4063ceec5c1ee7` |
| `courses/core/registry-types.program` | `faf98ffd041cdfe7ec43205f074a51024ec430e9e3b7011fc21efb88bd9eeba7` |
| `courses/core/registry.examples` | `4a06ce547fe77291e682467ccbd41bd35ddf61131a6fb9124fbd4b4d127a6739` |

The build-only loader pins clean compiler revision `175ded7f42286f2bca25a82d8cc476a60b56b71d`, origin `https://github.com/german-krasnikov/biome-dsl.git`, and aggregate SHA-256 `971947b575ee2315175d355e37fa472cb863d002cab0bdb77636605c48516ac1` of all 104 compiled Core/Kit JavaScript artifacts. `scripts/nj02/registry/validate.mjs` uses the public located Core reader and typed program reader. The narrow `scripts/nj02/registry/kt-text.mjs` owns Kotlin quoting; no TypeScript printer is imported. Gradle declares the exact inputs, outputs and task-provider dependency into `commonMain`. Neither generated file is tracked or an application JSON document:

| Generated build output | SHA-256 after generation |
| --- | --- |
| `kotlin/shared/build/generated/biome/kotlin/polski/data/GeneratedCoreRegistry.kt` | `3eb755ac4dab67bfc7f8a9b38a6c8e705d62be0d66c0dc89506b235b7dea1cc5` |
| `kotlin/shared/build/generated/biome/registry.manifest` | `d674af9baf7c9fbb3585e8f890ff0c9bc579bdad89c4c1af9442d081a2e9853a` |

The Kotlin output hash equals the broad candidate's independently checked hash. The manifest hash differs because its input list now names the small Kotlin-only quote helper instead of the removed broad printer. The manifest records source, binding, type, read-plan, compiler artifact and output identities; `--check` compares them without writing.

## Observed checks on this branch

From the product root with `BIOME_DSL_COMPILER=/Users/german/Work/Python/biome-dsl-nj02-kotlin-pin/implementations/typescript/packages`, `node scripts/nj02/registry/build.mjs` generated 14/14 facts and the same command with `--check` checked 14/14 fresh, zero skipped/failed. The generator parsed both positive and the independent rejected `registry.examples` cases and all 14 typed declarations. From `kotlin/`, `./gradlew :shared:generateBiomeCoreRegistry :shared:checkBiomeCoreRegistry :shared:desktopTest` with five `--tests` filters (`CoreCatalogTest`, `CoreRegistryValueTest`, `CoreRegistryOldInputTest`, `PackEngineTest`, `EnPackEngineRealFormsTest`), `--max-workers=1` and the exact `-PbiomeCompilerRoot` passed; XML contains **15 selected/executed, 0 skipped/failed** (6+2+2+2+3). The old input test reads SHA-pinned old course files only in tests and executes all 16+16 curriculum records and nine independent R5–R7 in-memory mutations through the Kotlin catalog. The live `PackEngine` rejection test runs for both languages before `ExerciseGenerator` construction.

R9a/b/c were applied one at a time to the exact frozen inputs/output and restored. The **read-only production `node scripts/nj02/registry/build.mjs --check`** exited 1 with `STALE output` for R9a source SHA `44203334546e4348e8d2eb1ba377b7a611ae6f3ddd0361b3a589eef86c85dec9`, `STALE manifest` for R9b type SHA `19ded4c06ab6e824a1abcdf13fa6ef44b0c04e3a098edd80a54a247f455c43af`, and `STALE output` for R9c generated Kotlin SHA `516ef6f6dafb201ffcac06280f0fd1dbfcb138908c22a7a111b9dd2c7d54289c`. After each restore the same check returned 14/14 green. The first local R9b harness draft incorrectly required a unique `en:optional<text>` token and stopped before mutation; the corrected first-occurrence recipe matches the frozen full-file SHA and produced the result above. A separate ignored-artifact control used clean compiler worktree `biome-dsl-nj02-tester-pin@175ded7f`: changing only ignored `kit/dist/text.js` from SHA `3a097cbc26fac937d7ffe5924940e699589c51c7ba479c16cd388bd6c4c39854` to `8a5f76dc24c16681f59870a60b147e546467c04e055616b2bb7a18906f61855b` left Git status clean but made `--check` exit 1 on the compiler artifact pin. Restoring exact bytes returned 14/14 green.

From the clean pinned compiler `implementations/typescript`, `npx biome check changed` selected **0 files/0 projects** because these product DSL files are outside that checkout. It is not a DSL-file pass; the production registry reader/construct/type/example generator above is their current surface check. `npm run check:boundaries` passed 30 packages/649 files, and `npm run logic:grid:check` passed; the compiler checkout itself was unchanged. `node --check` on all three new Node modules and the loader, and `git diff --check`, passed. The earlier independent candidate test executed source/schema audits 33/33 and 25/25 against the same four frozen SHA values and compiler pin; their comparator scaffolds are intentionally absent from this product integration, so they were **not rerun here**.

## Target scope and open guarantees

| Kotlin target / selected suites | XML selected / executed | Result |
| --- | ---: | --- |
| Desktop: five named suites above | 15 / 15 | 15 pass, 0 skipped/failures |
| JS ChromeHeadless: four common suites | 13 / 13 | 12 pass, **1 fail**, 0 skipped |
| Wasm ChromeHeadless: catalog and registry values | 8 / 8 | 8 pass, 0 skipped/failures |
| macOS Native: four common suites | 13 / 13 | 13 pass, 0 skipped/failures |
| iOS Simulator Native: four common suites | 13 / 13 | 13 pass, 0 skipped/failures |
| Android / device UI / accessibility | 0 / 0 | NOT RUN |

The JS failure was `EnPackEngineRealFormsTest.everySkillGeneratesFromTheRealCheckedInFormsTableWithEnRuActive`, which hit the runner's 10-second timeout. A one-test focused rerun on this integration branch selected/executed 1/1 and failed with the same timeout. On a separate clean detached **baseline `c574f78` worktree**, the identical focused Gradle command also selected/executed 1/1 and failed with the same 10-second timeout; this failure is reproducible before the registry diff, though the broader JS suite remains **FAIL** on this branch and performance equivalence is not established. The timeout and test expectation were not changed. The registry-specific JS cases were green: `CoreCatalogTest` 6/6 and `CoreRegistryValueTest` 2/2; `PackEngineTest` 2/2 was green. Broader Wasm engine cases were not selected. The native results are Kotlin/Native test binaries, not device UI or accessibility execution.

The R5–R7 choice is **defer shared `.given PORTS FROM .contract` for this slice**. The 14 registry declarations are typed Biome data, but the two language curricula and `usesFeatures` still enter through old JSON. A Logic/`.given` bridge here would prepare duplicate old-input facts without a live Kotlin Logic/EditMode consumer; `CoreCatalog` is the smaller pure Kotlin relation helper over the one generated registry and returns decisions without changing state. R7 executes only in the Kotlin old-input/catalog test; live `PackEngine` passes no `usesFeatures`, so runtime R7 is **NOT PROVED**. NJ03 should revisit one typed FACT/QUERY/LET relation with an actual Kotlin EditMode case and independent `.examples/.sources` expectation after curriculum and language facts become DSL. Kotlin PlayTest remains NOT RUN until it has a real runner.

The old embedded curriculum JSON, other application JSON, old validator, React/TypeScript product and tests are retained only as transitional controls. This slice is not a no-JSON acceptance pass, all-32-skill port, P14 full-language differential, P23 end-to-end N=10/100/1000 proof, Android/device execution or final product gate. The compiler revision has not been proved fetchable by clean CI; clean CI is **NOT RUN**.
