# NJ02 Kotlin live registry: independent relation and freshness freeze

Status: **frozen before the live Kotlin registry implementation; new-path execution NOT RUN**. This append-only receipt supplements [NJ02RegistryIndependent.cases](NJ02RegistryIndependent.cases), [NJ02RegistryIndependentCases.md](NJ02RegistryIndependentCases.md), and `Plans/Biome/NJ02RegistryMutationFreeze.md` on the isolated registry candidate. It neither edits those independent expectations nor promotes old JSON into the accepted product. The old files below were read only, solely as historical input controls. No application JSON file was written.

Product baseline: `biome/full-port-2026@b3c810a94f9cad7b3db76d0720544f7668aee672`. Architecture handoff: `plan/nj02-kotlin-live-registry@55b2943831cd97f726f04e574689f6bd3e110d83`, `Plans/Biome/NJ02KotlinLiveRegistryPlan.md`. Registry syntax/adapter candidate: `experiment/nj02-registry-slice@7560d18cc7983fc362f966b24aae6c31dce7a636`. Candidate compiler: `experiment/nj02-located-syntax-rescue@175ded7f` (short pin from handoff; resolve to a full, clean revision before build). Neither candidate source nor compiler code is copied into this tester branch.

## Historical input identity and positive controls

SHA-256 covers complete original file bytes. The 14 registry values and schema distinctions remain fixed by the older independent ledger; this receipt adds only live relation and freshness cases.

| Read-only old file | SHA-256 |
| --- | --- |
| `courses/lang/pl/curriculum.json` | `4f98cb4e0a5bc5b5f21890120fe25e077cedc2ba5e65852bff4b1e252b4234b7` |
| `courses/lang/en/curriculum.json` | `4c8f1c21595479a1b49ebb38a6fa4df9fff15f02bbe6d6cdd2b8ed4ecaf0afe6` |
| `courses/lang/pl/lang.json` | `374938851126238479d9e0030bf583003df1780738c8eca1b2182bac4f0b7cff` |
| `courses/lang/en/lang.json` | `4e97a8fe6e4b0d7cb7ce900d60791019d9384bdbffb5edaacf2cd1ba9bc50a31` |
| `courses/core/constructions.json` | `0537aa8ca3e661b093ff132df37dd6943bf9149639f181bbf0ff91b6c83ceb96` |
| `courses/core/features.json` | `e51e4cac5b823bb8736c7d0b9cc583062e88b77425c66fdbab96dadcd8405358` |
| `scripts/validate-pack-v2.mjs` | `e954048a5b344f46219cbf7e1d08e9676101043911313df61bfe17bee0151080` |

Original zero-based curriculum index 0 is `case.acc.n` for `pl` and `en:role.object` for `en`. Both use `core.argument.case-role`, a `Case` focus, and `Tense: Pres`, `Person: "1"`, `Number: Sing` fixed entries. Polish focus is `Nom → Acc`; English focus is `Subj → Obj`. Both languages declare `Case` and `Tense` in `usesFeatures`; the core registry declares those keys and values. The baseline must accept both complete 16-skill curricula through the **actual Kotlin catalog/PackEngine path**, with selected/executed/skipped counts reported per host. Old `validatePackV2()` success is only a historical control.

## R5–R7: frozen independent semantic failures

Each row is a **separate in-memory mutation** of the SHA-pinned old curriculum or language declaration. Preserve all other bytes and records; do not write a mutated JSON document. Before the relevant NJ03 Biome curriculum/language syntax is pinned, the required location is only the historical path and zero-based array index shown below. An implementation may format paths differently, but its diagnostic must identify the offending old input file, index 0, and named field/key/value. No Biome token line, column, code or wording is invented here. Reject before an invalid `SkillSpec` reaches `ExerciseGenerator`; a generated value that merely compiles is insufficient.

| Case | Independent mutation | Required failure attribution and meaning |
| --- | --- | --- |
| R5-pl | In `pl` curriculum index 0, set `construction` from `core.argument.case-role` to `core.does-not-exist`. | `/lang/pl/curriculum.json/0/construction`; reject undeclared construction `core.does-not-exist`. |
| R5-en | In `en` curriculum index 0, make the same replacement. | `/lang/en/curriculum.json/0/construction`; reject the same undeclared construction. |
| R6a-focus | In `pl` curriculum index 0, set `focus.feature` from `Case` to `AbsentFeature`; leave `from` and `to` intact. | `/lang/pl/curriculum.json/0/focus`; reject undeclared feature key `AbsentFeature`, before considering its values. |
| R6a-fixed | In `en` curriculum index 0, append `AbsentFeature: Pres` to `fixed`; retain the original entries. | `/lang/en/curriculum.json/0/fixed/AbsentFeature`; reject undeclared fixed feature key. |
| R6b-from | In `pl` curriculum index 0, set `focus.from` from `Nom` to `Impossible`. | `/lang/pl/curriculum.json/0/focus`; reject `Impossible` as absent from declared `Case` values. |
| R6b-to | In `en` curriculum index 0, set `focus.to` from `Obj` to `Impossible`. | `/lang/en/curriculum.json/0/focus`; reject `Impossible` as absent from declared `Case` values. |
| R6b-fixed | In `pl` curriculum index 0, set `fixed.Tense` from `Pres` to `Impossible`. | `/lang/pl/curriculum.json/0/fixed/Tense`; reject `Impossible` as absent from declared `Tense` values. |
| R7-pl | Remove only `Case` from `pl` `usesFeatures`, retaining the core feature and the curriculum unchanged. | Conflict between `courses/lang/pl/lang.json/usesFeatures` and `/lang/pl/curriculum.json/0/focus`; reject used `Case` as disabled for `pl`. |
| R7-en | Remove only `Case` from `en` `usesFeatures`, retaining the core feature and the curriculum unchanged. | Conflict between `courses/lang/en/lang.json/usesFeatures` and `/lang/en/curriculum.json/0/focus`; reject used `Case` as disabled for `en`. |

These nine cases preserve old `checkConstructionsResolve`/`checkFeaturesResolve` semantics. The old validator attaches `/lang/<code>/curriculum.json/<index>` paths and has **no authored registry token coordinate**. The new Kotlin check may attach richer origin data later, but cannot claim a future DSL line/column from these old inputs. R3 unknown construction `axis` and R4 composition target/cycle are excluded, as the old schema and validator accepted or ignored them. These cases are not a second authored registry table: expected IDs/values are independent regression examples only.

## R9: pinned artifact, command and mutation identities

The candidate source files below are currently on `experiment/nj02-registry-slice@7560d18`; they are **not** present on this clean tester branch. These input hashes and exact replacements were computed from that candidate's physical text before generation. The accepted live build must consume them from one authored registry authority. The output names and Gradle task names are pinned to the architecture handoff; they are NEW implementation targets, not claims that a command currently exists.

| Stage | Path / command identity |
| --- | --- |
| Fact source | `courses/core/registry.core-data`, SHA-256 `8e4d578675378195ca07a04a6615a9f6c3136d965d7505acf556a181d426d83d` |
| Declaration/type input | `courses/core/registry.construct`, SHA-256 `67e552fc42a98bf5bda01ed0a76f3efaa9885a81ad78e2295a4063ceec5c1ee7`; `courses/core/registry-types.program`, SHA-256 `faf98ffd041cdfe7ec43205f074a51024ec430e9e3b7011fc21efb88bd9eeba7` |
| Independent examples | `courses/core/registry.examples`, SHA-256 `4a06ce547fe77291e682467ccbd41bd35ddf61131a6fb9124fbd4b4d127a6739` |
| Generated Kotlin value | `kotlin/shared/build/generated/biome/kotlin/polski/data/GeneratedCoreRegistry.kt` |
| Readable freshness manifest | `kotlin/shared/build/generated/biome/registry.manifest` |
| Explicit generation | From product `kotlin/`: `./gradlew :shared:generateBiomeCoreRegistry` |
| Read-only freshness check | From product `kotlin/`: `./gradlew :shared:checkBiomeCoreRegistry` |

The check task must compare the current fact, construct, type/binding, compiler revision, Kotlin output and manifest identities without regenerating or repairing them. The acceptance run must observe this read-only check on a stale state before any build task can regenerate it; the exact build dependency is implementation evidence to inspect. If the task names or output locations need an architecture change, freeze a **new** receipt before running the new implementation; do not rewrite these expectations after seeing a result.

| Case | One change after a clean generation | Expected observable result |
| --- | --- | --- |
| R9a source-only | In `registry.core-data`, replace the sole `values: [Sing Plur]` with `values: [Sing Plur Dual]` and leave binding, generated Kotlin and manifest untouched. The entire mutated source SHA-256 must be `44203334546e4348e8d2eb1ba377b7a611ae6f3ddd0361b3a589eef86c85dec9`. | Valid authored data has changed; read-only check fails source/output freshness before accepted build. A parse failure does not satisfy this case. |
| R9b binding-only | In `registry-types.program` line 1, replace only the first `en:optional<text>` with `en:text` (the `Description` type), leaving the later `Gloss` optional member unchanged; source, generated Kotlin and manifest untouched. The entire mutated type file SHA-256 must be `19ded4c06ab6e824a1abcdf13fa6ef44b0c04e3a098edd80a54a247f455c43af`. | Valid type declaration and existing full source still parse; read-only check fails changed contract/read-plan identity even if emitted Kotlin value bytes would be identical. A type or syntax failure does not satisfy this case. |
| R9c output-only | Append exactly `\n// NJ02 R9c output tamper\n` (UTF-8) to `GeneratedCoreRegistry.kt`, leaving every source and manifest untouched. The clean generated output hash is recorded **after** generation, before tampering; no output hash can be frozen before that file exists. | Read-only check fails its output checksum. Successful Kotlin compilation of this harmless comment does not satisfy freshness. |

For each R9 case use a disposable copy/worktree, verify the one edited file and its hash, run the same read-only check command, record nonzero exit and the failing identity, restore, then rerun a clean green check. `registry.manifest` must contain source paths/hashes, compiler revision, declaration/type/read-plan identity, output path/hash as readable lines. Do not use a JSON manifest or generated JSON intermediary. Report `--check` or Gradle selected/executed/skipped/failed counts, exit codes, cwd, SHA, and whether generated Kotlin was actually compiled and consumed; a stale check result or a Gradle UP-TO-DATE shortcut does not close R9.

## Evidence at freeze time

Read-only inspection in this worktree checked the seven old file hashes above and index-0 values by `python3` strict text read plus `json.loads` in memory; it wrote no JSON. Read-only inspection in the isolated registry candidate checked four candidate input hashes and computed R9a/R9b replacement hashes in memory. `scripts/validate-pack-v2.mjs` was inspected for the current error paths and check order. **New live Kotlin implementation, all nine R5–R7 mutations, all three R9 mutations, Kotlin tests, generated output, browser and native hosts: NOT RUN (0 selected / 0 executed / 0 skipped / 0 failed).** This receipt is a preimplementation oracle, not a PASS for NJ02.
