# NJ02 independent core-registry ledger

Status: **old-source facts frozen; candidate text surface, source spans, and new-arm execution NOT FROZEN / NOT RUN**. This ledger is independent of the proposed DSL, native builder, TOML arm, generated output, and [NJ02IndependentCases.md](NJ02IndependentCases.md). Its companion [NJ02RegistryIndependent.cases](NJ02RegistryIndependent.cases) defines the cases. Product authority inspected: `biome/full-port-2026@16060a72554d82849128a56c0960f847476f6b79`. No application JSON was written. The existing JSON files are a read-only oracle during this experiment, not a second future authority.

## Source identity

SHA-256 covers complete on-disk files, including whitespace and final newline.

| Source | SHA-256 | Role |
| --- | --- | --- |
| `courses/core/constructions.json` | `0537aa8ca3e661b093ff132df37dd6943bf9149639f181bbf0ff91b6c83ceb96` | Ordered construction facts. |
| `courses/core/features.json` | `e51e4cac5b823bb8736c7d0b9cc583062e88b77425c66fdbab96dadcd8405358` | Ordered feature and value facts. |
| `courses/schema/core-constructions-v1.schema.json` | `415a1e85d5c3b8d7b382d160d62094dad42291713d284161d594b34064518c0d` | Existing construction shape. |
| `courses/schema/core-features-v1.schema.json` | `7735abaccaf77b301a4afa0766ba7730aa031813d85c6ecbd3a40e0d54b7286a` | Existing feature shape. |
| `scripts/validate-pack-v2.mjs` | `e954048a5b344f46219cbf7e1d08e9676101043911313df61bfe17bee0151080` | Existing schema and curriculum relation checks. |
| `tests/pack-v2.test.ts` | `f9eaa4df5d4bc0ac3a76a1cd55e3e8897b6c78b3b004e95f23dfc7fab8489c60` | Existing pack test scope. |
| `courses/lang/en/curriculum.json` | `4c8f1c21595479a1b49ebb38a6fa4df9fff15f02bbe6d6cdd2b8ed4ecaf0afe6` | English relation inputs. |
| `courses/lang/pl/curriculum.json` | `4f98cb4e0a5bc5b5f21890120fe25e077cedc2ba5e65852bff4b1e252b4234b7` | Polish relation inputs. |
| `courses/lang/en/lang.json` | `4e97a8fe6e4b0d7cb7ce900d60791019d9384bdbffb5edaacf2cd1ba9bc50a31` | English `usesFeatures`. |
| `courses/lang/pl/lang.json` | `374938851126238479d9e0030bf583003df1780738c8eca1b2182bac4f0b7cff` | Polish `usesFeatures`. |

## Ordered positive facts

Both files have `schemaVersion: 1` and a human `$comment`. The nested catalogs below preserve their authored insertion order. Every construction has a `description` record with required nonempty `ru` and optional nonempty `en`; every one here has both. `axis` is explicitly **null** for `core.agreement.possessive` and `core.composite`, rather than omitted. Only `core.composite` authors `composesOf`, as a **present empty list**. All other `composesOf` members are **absent**.

| Position | Construction ID | axis | `description.ru` | `description.en` |
| --- | --- | --- | --- | --- |
| 1 | `core.argument.case-role` | `Case` | Падеж аргумента предложения (или семантическая роль в языках без падежей). | An argument's case (or semantic role in a caseless language). |
| 2 | `core.verb.tense` | `Tense` | Время глагола. | Verb tense. |
| 3 | `core.sentence.polarity` | `Polarity` | Утверждение/отрицание предложения (в pl — родительный падеж дополнения при отрицании). | Sentence polarity (pl: negation flips the object's case to genitive). |
| 4 | `core.sentence.mood` | `Mood` | Тип предложения: утверждение или общий вопрос. | Sentence mood: declarative or yes/no question. |
| 5 | `core.agreement.possessive` | Null | Владелец и согласование притяжательного/прилагательного с существительным. | The owner, and agreement of the possessive/adjective with the noun. |
| 6 | `core.number` | `Number` | Число (единственное/множественное). | Grammatical number (singular/plural). |
| 7 | `core.composite` | Null | Составной тип: ссылается на ≥2 структур (цепочка/`mixed`); pl-ru реализует состав через `chain`/лексический фокус в lang/pl/exercise-recipes.json, а не статический список здесь (UniversalCorePlan.md §10, риск 1, не решён). | Composite type referencing >=2 structures (a chain, or 'mixed'); pl-ru resolves the actual composition through lang/pl/exercise-recipes.json's `chain`, not a static list here (open risk, §10.1). |

Feature `values` are ordered, nonempty lists of distinct, nonempty **text** values. `Person` values `1`, `2`, `3` are text, not numbers. Every current feature authors a `gloss` with both languages, although the schema permits the entire `gloss` to be absent and permits either language member to be absent when `gloss` is present.

| Position | Feature | Values in exact order | `gloss.ru` | `gloss.en` |
| --- | --- | --- | --- | --- |
| 1 | `Case` | `Nom`, `Gen`, `Dat`, `Acc`, `Inst`, `Loc`, `Voc`, `Subj`, `Obj`, `In`, `With`, `To`, `About`, `Of` | падеж | case |
| 2 | `Number` | `Sing`, `Plur` | число | number |
| 3 | `Person` | `1`, `2`, `3` | лицо | person |
| 4 | `Tense` | `Pres`, `Past`, `Fut` | время | tense |
| 5 | `Polarity` | `Pos`, `Neg` | полярность (утверждение/отрицание) | polarity (affirmative/negative) |
| 6 | `Mood` | `Decl`, `YesNoQ` | наклонение/тип предложения | mood / sentence type |
| 7 | `Aspect` | `Simple`, `Continuous`, `Perfect` | аспект (простой/длительный/совершённый) | aspect (simple/continuous/perfect) |

Schema distinctions to retain: `description` is required; `description.ru` must be nonempty; `description.en` may be absent but, if present, nonempty. `axis` may be absent, text, or null. `composesOf` may be absent or an empty/nonempty list of text. `gloss` may be absent or an empty record; present `ru`/`en` must be nonempty text. `values` must have at least one distinct nonempty text item. A missing feature or construction catalog is invalid. These are schema observations, not a proposed DSL record layout.

## Existing behavior and evidence boundary

`validatePackV2()` reads both registry JSON files, schema-validates them with Ajv, then iterates languages with a lexicon and curriculum. `checkConstructionsResolve` rejects a curriculum construction outside `constructions`; `checkFeaturesResolve` rejects a `focus` or `fixed` key outside `features`, a key outside that language's `usesFeatures`, or a value outside the feature's `values`. The current error names a curriculum path and offending key/value but has **no authored registry token span**. The schema's `uniqueItems` rejects a duplicate value inside one `values` list. `tests/pack-v2.test.ts` exercises a missing construction mutation; its positive tests cover checked-in pack validity, membership of curriculum feature keys, and additions to `Case`/`Aspect`. They do not exercise every R2b/R5–R8 branch or source spans. The new arm must execute those cases before any PASS claim.

`JSON.parse` collapses duplicate object keys before schema and relation checks. Therefore duplicate construction IDs and duplicate feature keys are **NEW authoring-profile integrity requirements**, not existing validator rejections. The current construction schema accepts arbitrary text `axis` and `composesOf` items; the validator does not resolve either field or detect composition cycles. Unknown axis, unknown/wrong-kind composition target, and cycles are **outside accepted old parity**. Empty `core.composite.composesOf` must remain valid.

The companion cases separate existing parity R2b/R5–R8 from new R1/R2a/R9. Candidate-specific source text, exact codes, and offending token coordinates must be frozen in a later append-only receipt **after the surface is pinned and before its implementation runs**. Do not derive those locations from compiler diagnostics or generated artifacts. The same independent expected behavior applies to A/B/C; each arm needs its own source coordinates. A linked test, printed target value, or passing baseline pack is not evidence that a new-arm case ran (P15–P17).

Read-only inspection plus baseline execution receipt: cwd `/Users/german/Work/JS/polski-grammar-srs-biome-nj02-registry-tester`; product SHA above; `sha256sum` on the ten listed sources; `node scripts/validate-pack-v2.mjs` **PASS pack v2**; `npx vitest run tests/pack-v2.test.ts` **22 selected, 22 passed, 0 skipped, 0 failed**. `node_modules` is a local ignored symlink to the canonical checkout's installed dependencies. New registry cases selected/executed/skipped/failed: **0/0/0/0; NOT RUN**. Compiler, TS/Kotlin generated values, Kotlin product execution, browser, and devices: **NOT RUN**. This document and its case text claim no generation or freshness result.
