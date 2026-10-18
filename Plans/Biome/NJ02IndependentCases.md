# NJ02 independent preimplementation cases

Status: **frozen old-source facts, new compiler/consumer checks NOT RUN**. This is an independent expectation record for [NoJsonFullPortPlan.md](NoJsonFullPortPlan.md) NJ02 and [NoJsonAcceptance.md](NoJsonAcceptance.md) FP01/FP10. It is independent of any proposed compiler syntax or implementation arm. The old authored course files were read only; no generated output supplied an expected value, and no JSON file was written. The product source is `biome/full-port-2026@044bcf08cf97ca756546e8d7f392fca79ed643e9`. The locally observed biome checkout from the earlier Stage 0 inventory was `2ab6275d59496e113cd2a17caf0f75d1c63174d2`; it was **not** executed for this corpus, pinned as a product dependency, or proved fetchable here.

## Source identity and scope

SHA-256 is over each complete on-disk UTF-8 file, including formatting and final newline. These files are existing app-owned JSON and may be consulted as read-only preport controls; the accepted product must replace their app-data role. The first two English curriculum records below are the **curriculum slice**, not a claim that the linked recipe, pair copy or lifehack records have been fully frozen.

| Old source (read only) | SHA-256 | Fact used here |
| --- | --- | --- |
| `courses/lang/en/curriculum.json` | `4c8f1c21595479a1b49ebb38a6fa4df9fff15f02bbe6d6cdd2b8ed4ecaf0afe6` | Full 16-ID order and complete two selected curriculum records. |
| `courses/lang/en/lexicon.json` | `f96da7fc14e569d2e061a9f0b39a6d97ead26eb7f21d6521d9ed457a8d0abd80` | Seven recipient filter noun IDs exist. |
| `courses/core/constructions.json` | `0537aa8ca3e661b093ff132df37dd6943bf9149639f181bbf0ff91b6c83ceb96` | Selected construction IDs exist. |
| `courses/core/features.json` | `e51e4cac5b823bb8736c7d0b9cc583062e88b77425c66fdbab96dadcd8405358` | `Case`, `Tense`, `Person`, `Number` and selected values are declared. |
| `courses/lang/en/exercise-recipes.json` | `05757efb0e6966dfd25144f62ab34b49fd66453dd9a4dc077bbdeaed28a6c326` | Both IDs have separate recipe records under `skills`; this document freezes only selected reference keys/order below. |
| `courses/pairs/en-ru/pair.json` | `55f79438cf1511b1061d389f4d74117112c910092e7515c73c57aa0d3af61862` | Both IDs have pair-facing copy; selected title/focus/prerequisite cross-check below. |
| `courses/pairs/en-ru/lifehacks.json` | `4cc8eb175cef2189b30493308e03d6f75cbbc6f19b2e899309394143bf6369c0` | Selected lifehack-to-skill reference IDs below. |

The English curriculum contains **16** entries in this order: `en:role.object`, `en:verb.presentSimple`, `en:possessive.my`, `en:mood.question`, `en:role.location`, `en:role.instrument`, `en:polarity.present`, `en:verb.pastSimple`, `en:role.recipient`, `en:number.plural`, `en:polarity.past`, `en:verb.futureSimple`, `en:pronouns`, `en:verb.presentContinuous`, `en:tense.contrast`, `en:mixed`. A typed replacement must preserve this order, not sort by ID. The selected entries are positions **9** and **16**, counting from one. In each selected record the existing field order is `id`, `construction`, `focus`, `fixed`, `lexicalFilter`, `level`, `prerequisites`. An emitted typed value may have a target-language field declaration order, but its ordered source data and lists must retain the semantic order below.

### Arm-neutral expected shape and bounded reference catalog

For this ledger, `Text` means an exact UTF-16 code-unit sequence, `Null` an explicit null, `Record` an ordered sequence of named typed members (including zero members), `List` an ordered sequence of typed items (including zero items), and `Absent` means no member was authored. These words describe the **expected semantic tree**, not proposed DSL tokens, generated class names or a JSON interchange. E1/E2/E3 are seven-member `Record` values in the source order above. Their `id`, `construction`, `level` and all named leaves are `Text`; `focus` and `lexicalFilter` are `Record` or `Null`; `fixed` is `Record`; `prerequisites` is `List<Text>`. E1's `lexicalFilter.where.nounId` is `List<Text>`. The candidate may use target-specific typed DTOs, but its observable semantic values must equal this tree. `Absent`, `Null`, `Record([])` and `List([])` have four different observations; a target representation that collapses them fails the relevant case.

Reference checks for the bounded E1/E2/E3 slice resolve against **this independent, text-only catalog plus those three records**, never by importing the old JSON into the new compiler or copying a generated catalog. The declared external skill IDs are `en:role.instrument`, `en:possessive.my`, `en:verb.pastSimple`, `en:verb.presentSimple`. The existing edges are `en:role.instrument` → `en:role.object`, `en:possessive.my` → `en:role.object`, and `en:verb.pastSimple` → `en:verb.presentSimple`; `en:verb.presentSimple` has an empty prerequisite list. These IDs/edges come from the pinned English curriculum. E1 and E2 supply their own edges below; E3 is `en:role.object` and has no outgoing edge. The closed catalog contains **seven unique skills**: E1, E2, E3 and four external IDs. Every referenced skill is in that set. A full 16-skill port must separately check every reference against the complete independent ID list above.

The bounded construction catalog is `core.argument.case-role` with axis `Case`, and `core.composite` with no axis, from pinned `courses/core/constructions.json`. The feature/value catalog needed by these records is `Case: Subj, Obj, To`; `Tense: Pres`; `Person: "1"` (text); `Number: Sing`, from pinned `courses/core/features.json`. This is a selected subset, not a replacement for full closed value sets. The noun-ID catalog is `wife`, `husband`, `friend`, `son`, `dog`, `cat`, `child`, from pinned `courses/lang/en/lexicon.json`. A real consumer must resolve E1's construction/focus/fixed/filter and E2's construction/prerequisites against these declarations, and reject unknown IDs/values. Merely printing the listed strings or compiling a DTO does not prove resolution.

## Frozen positive case E1: `en:role.recipient`

The expected typed curriculum record has **all seven named fields present**. Exact old-source values, including types and sequence:

| Field | Literal expected value |
| --- | --- |
| `id` | string `en:role.recipient` |
| `construction` | string `core.argument.case-role` |
| `focus` | present record with `feature` = string `Case`, `from` = string `Obj`, `to` = string `To`, in that order |
| `fixed` | ordered record: `Tense` = string `Pres`; `Person` = string `1`; `Number` = string `Sing`. `1` is text, never integer one. |
| `lexicalFilter` | present record: `slot` = string `noun`; `where` = record with `nounId` = ordered list of strings `wife`, `husband`, `friend`, `son`, `dog`, `cat`, `child`. No additional filter members. |
| `level` | string `A2` |
| `prerequisites` | one-element ordered string list: `en:role.instrument` |

`core.argument.case-role` exists in the pinned core constructions; `Case` values include `Obj` and `To`; `Tense` includes `Pres`; `Number` includes `Sing`. The seven `nounId` strings each exist as IDs in the pinned English lexicon. `en:role.instrument` is curriculum position 6. These are observed reference facts, not proof that a future compiler resolves them correctly.

The pair-facing entry independently names `id` = `en:role.recipient`, `title` = `To · получатель`, `focus.before` = `my good friend`, `focus.after` = `to my good friend`, and `prerequisites` = [`en:role.instrument`]. The recipe `skills` entry has `skillId` = `en:role.recipient`, `source` pattern key `seenObj`, `expected` pattern key `roleDrill`, `promptKey` = `roleRecipientPrompt`, `explanationKey` = `roleDrillExplanation`, and tags in order [`role`, `to`]. Lifehack records `en-ru.role.recipient.to` and `en-ru.role.recipient.word-order` each reference `en:role.recipient`. These cross-references must not become a second handwritten curriculum fact when the source is ported.

## Frozen positive case E2: `en:mixed`

The expected typed curriculum record again has **all seven fields present**. A null is an explicit value and must not be confused with an omitted field; an empty record is neither null nor an absent record.

| Field | Literal expected value |
| --- | --- |
| `id` | string `en:mixed` |
| `construction` | string `core.composite` |
| `focus` | **explicit null** |
| `fixed` | **present empty record**, zero entries |
| `lexicalFilter` | **explicit null** |
| `level` | string `B1` |
| `prerequisites` | two-element ordered string list: `en:possessive.my`, then `en:verb.pastSimple` |

`core.composite` exists in the pinned core constructions; the two prerequisite IDs occur at curriculum positions 3 and 8 respectively. The pair-facing entry names `id` = `en:mixed`, `title` = `Смешанные трансформации`, `focus.before` = `I see my beautiful house.`, `focus.after` = `I saw his beautiful house.`, and the same ordered prerequisites. The recipe `skills` entry has `skillId` = `en:mixed`, `source` pattern key `seenObj`, `expected` pattern key `chainPast`, `promptKey` = `mixedPrompt`, `explanationKey` = `mixedExplanation`, tags in order [`mixed`, `past`, `agreement`], and ordered owner pool [`your`, `his`, `her`, `its`, `our`, `their`]. Its two `changes` entries are ordered verb change first, owner change second. Lifehack IDs `en-ru.mixed.interleaving` and `en-ru.mixed.cognitive-load` each reference `en:mixed`. Exact pair prose, recipe trees and citations need their own full-content freeze before NJ03, rather than an inference from these selected keys.

## Frozen positive case E3: empty prerequisites

The first existing English curriculum record, `en:role.object`, has the same seven fields in the same order. Its typed values are `id` = Text `en:role.object`; `construction` = Text `core.argument.case-role`; `focus` = Record(`feature`: Text `Case`, `from`: Text `Subj`, `to`: Text `Obj`); `fixed` = Record(`Tense`: Text `Pres`, `Person`: Text `1`, `Number`: Text `Sing`); `lexicalFilter` = **Null**; `level` = Text `A1`; `prerequisites` = **List([])**, a present list with zero items. This old-source fact is a positive empty-list control. It must remain distinct from `Absent`, `Null` and an empty record. It also completes the bounded reference catalog's root skill. No new compiler test was run.

## Synthetic printer probe T1: text rendering beyond the curriculum schema

T1 is a **new printer-only input**, not old course content or a new `SkillSpec` field. A minimal independent `TextProbe` record has one member named `text` of semantic type `Text`; it is a disposable generic emitter conformance probe, never an authored course schema, product data file or P13 consumer. Its `text` value has **exactly 21 UTF-16 code units**, in order:

```text
005A 0061 017C 00F3 0142 0107 002F 0024 005C 0022 000A
0420 0443 0441 2028 0058 2029 0059 D83D DE00 0021
```

The units spell `Zażółć/`, `$`, one backslash, one double quote, LF, `Рус`, U+2028, `X`, U+2029, `Y`, U+1F600 (the **paired** D83D DE00 units), `!`. The display description and hex sequence together define the input and expected value; the hex sequence is authoritative where Markdown rendering hides controls. A test input must encode these units unambiguously; generated TS and Kotlin values must expose the same units after **actual execution**. The source spelling and escaping syntax remain candidate-dependent and unaccepted. Raw invalid UTF-8 or escaped **unpaired** D800/DC00 in curriculum/copy authoring is a separate rejection case N8. A read-only scan of all 48 `courses/**/*.json` files at the pinned product SHA found zero unpaired surrogates after strict UTF-8 decode and old JSON parsing, and `rg` found zero `\\uD8xx`–`\\uDFxx` source escape matches. This course-authoring rejection policy differs from the text-wire user's legacy-data preservation contract, which requires lossless UTF-16 handling.

## Future discriminating cases and diagnostic status

The following inputs are **new proposed mutations/probes**, not records in the old source and not evidence of an accepted compiler grammar. Their required semantic result is independent of syntax arm; **no exact diagnostic code, wording or source span is yet accepted**. Each case is **NOT RUN**. Write an independently authored test source only after choosing a candidate surface, then freeze the expected position in that source before executing it. A mere parser error is insufficient if it does not identify the bad declaration/relation.

| Case | Input change or probe | Expected semantic result; present status |
| --- | --- | --- |
| N1 duplicate identity | Add a second curriculum skill with `id` `en:role.recipient` while retaining the original. | Reject duplicate skill ID with both occurrences locatable; **NOT RUN**, exact diagnostic pending. |
| N2 duplicate member | In E1, repeat `Person` in `fixed` with a second value. | Reject duplicate record key, not last-wins; **NOT RUN**, exact diagnostic pending. |
| N3 missing/cyclic relation | (a) Change E1's only prerequisite to `en:role.absent`; (b) in a separate input, retain E1 → `en:role.instrument` and change the catalog's `en:role.instrument` edge from `en:role.object` to `en:role.recipient`. | (a) Reject unresolved skill ID, without inventing it as an empty declaration; (b) reject the explicit two-skill cycle E1 → instrument → E1, without silently reordering or dropping an edge. Both **NOT RUN**; diagnostic codes/spans pending. |
| N4 missing filter noun | Change E1's `child` filter ID to `absent-noun`. | Reject unresolved English lexicon noun ID through the actual consumer/validator; **NOT RUN**, exact diagnostic pending. |
| N5 invalid construction/feature | Change E1's construction to `core.absent` or its `focus.to` to `Impossible`. | Reject undeclared construction or `Case` value, respectively; **NOT RUN**, exact diagnostics pending. |
| N6 null/empty/absent distinction | Compare valid E2 (`focus` = Null, `fixed` = Record([]), `lexicalFilter` = Null, prerequisites = two-item List) and valid E3 (prerequisites = List([])) with four separate variants: E2 `focus` Absent, E2 `fixed` Null, E2 `fixed` Absent, E3 `prerequisites` Absent. | **Reject all four malformed authoring records at schema validation**; no default, omission collapse or typed emission. E2/E3 remain accepted positive controls. The lowered `SkillSpec` need not represent invalid omission because validation happens before lowering. **NOT RUN**; diagnostic codes/spans pending. |
| N7 order/type drift | Reverse E1's seven `nounId` values; reverse E2 prerequisites; replace E1 `Person` text `1` with numeric one. | Lists retain authored sequence and numeric-looking text remains string; changed input must be detected by independent expectation/freshness check; **NOT RUN**. |
| N8 text escapes and malformed Unicode | Run T1's exact 21-unit positive printer value. In separate invalid curriculum/copy-authoring inputs, provide raw invalid UTF-8, escaped unpaired `D800`, and escaped unpaired `DC00`. | T1's TS/Kotlin emitted values compile and execute with exact code-unit equality and no JSON-shaped cell/intermediate; **reject** each malformed authoring input before emission, naming its span. A valid astral pair must remain valid. All **NOT RUN**; diagnostic codes/spans pending. This does not weaken text-wire legacy-value preservation. |
| N9 malformed source | Truncate a nested declaration or its text delimiter; point a reference at an invalid source token. | Reject at a useful source span; no partial accepted artifact; **NOT RUN**, syntax and exact diagnostic pending. |
| N10 stale binding/output | Mutate source, fact binding and generated typed output one at a time after a clean generation. | Each changed state fails the appropriate freshness/contract check; **NOT RUN**, commands/artifact hashes pending. |

The later NJ02 execution report must pin the candidate source and artifact hashes; compare these literal typed fields and all order/null/empty distinctions on actual TS and Kotlin consumers; count selected, executed, skipped and failed cases; record command/cwd/compiler SHA; and distinguish generated text, successful compile, and executed behavior. This file freezes only the positive old-source subset and the intended negative categories. It does **not** claim a new grammar, backend, read plan, Kotlin escaping, generated artifact, or host result has passed.

## Independent consumer audit for P13

Read-only Biome inspection found: `implementations/typescript/tests/tooling/project-cli.test.ts` exercises project `HEAD`/`ITEM` declarations and a generated C# `SHELL` class in temporary projects; `packages/universal/feature/tests/feature-collections.sources` and `packages/emitters/tests/feature-collections.test.ts` exercise `list<record{...}>` parsing/lowering; `packages/languages/program-bindings.construct` and `tests/tooling/kotlin-program/fixtures/definitions/values.construct` exercise reusable `LIST`, `OPTIONAL`, `RECORD` and `EMPTY RECORD` type-binding constructs, including Kotlin spelling. These are useful **capability probes**, but their test fixtures are not an independent product consumer of a new ordered course-data declaration. Generating TS and Kotlin from the same Polski input is also one consumer in two languages, not two independent consumers. The Biome selfhost program-bindings generator is a **candidate second use** for a general record/list capability because it already owns real typed API bindings; prove a concrete, independently required authoring task and actual execution there before counting it under P13. The existing `corpus/labs/authoring-data/authoring-data.feature` declares a list of diagnostic records for a lab; its presence alone is likewise not second-consumer proof. At this revision the P13 second-consumer gate is **NOT RUN / not established**; no new syntax is justified by these reads alone.

Read-only receipt at this revision, cwd `/Users/german/Work/JS/polski-grammar-srs-biome-full`: `git status --short --branch`, `git rev-parse HEAD`, `sed -n` on selected course ranges, `rg -n` on the selected IDs and declared values, `shasum -a 256` on the seven listed sources, and a Python `json.loads` inspection of the old curriculum/recipe files for current fields, positions, types and order. A further Python read-only scan strictly decoded and parsed all 48 `courses/**/*.json` files and found zero unpaired surrogates; a source-escape `rg` scan found zero surrogate escapes. `json.loads` inspected historical source only and wrote no JSON. In `/Users/german/Work/Python/biome-dsl`, `rg -n` and `sed -n` read the HEAD/ITEM project test, FEATURE collection tests, program-bindings constructs, authoring-data lab and P13 definition; none was executed. **Application tests, biome compiler, TypeScript/Kotlin compile, generated-consumer execution, browser and devices: NOT RUN.**
