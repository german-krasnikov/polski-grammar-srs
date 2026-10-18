# NJ03 independent curriculum facts

Status: **the old-source facts for 16 English and 16 Polish skills are frozen before candidate DSL transcription**. The compact, ordered expectations are [NJ03CurriculumIndependent.cases](NJ03CurriculumIndependent.cases). This is a preparatory old-source corpus, not a candidate parser, generator, consumer, or NJ03 acceptance result. Product baseline inspected: `biome/full-port-2026@2e705f280aebcf3a67204fc6916b922fb3a6b41e`. Neither output from the NJ02 experiment nor a generated `SkillSpec` was used as an expectation. The existing course files were read only; no application JSON was authored or emitted.

## Locked source identity and notation

The SHA-256 values cover **complete on-disk bytes**, including indentation, line endings, and final newline. The `.cases` `SOURCE` lines bind each language to its exact relative path and full-file hash.

| Role | Exact path | SHA-256 |
| --- | --- | --- |
| English old facts | `courses/lang/en/curriculum.json` | `4c8f1c21595479a1b49ebb38a6fa4df9fff15f02bbe6d6cdd2b8ed4ecaf0afe6` |
| Polish old facts | `courses/lang/pl/curriculum.json` | `4f98cb4e0a5bc5b5f21890120fe25e077cedc2ba5e65852bff4b1e252b4234b7` |
| Existing curriculum schema | `courses/schema/curriculum-v1.schema.json` | `f9a5cc1a25c24bb7a7d9f10bfc8dc33a0ba842b8255cfc81bdfd82690d15461f` |
| Existing local validator | `scripts/validate-curriculum.mjs` | `47c36a9b1340f6946967614fe4a66be5f466c504edda21db53df29d16aaf7e8e` |
| Existing pack cross-validator | `scripts/validate-pack-v2.mjs` | `e954048a5b344f46219cbf7e1d08e9676101043911313df61bfe17bee0151080` |

The `ROW` columns are language, 1-based list position, `id`, `construction`, `focus`, `fixed`, `lexicalFilter`, `level`, and `prerequisites`. They preserve all current values and ordering without copying the two source documents wholesale. The seven top-level keys in **every** current record are present in this exact order: `id`, `construction`, `focus`, `fixed`, `lexicalFilter`, `level`, `prerequisites`. `NULL` means a **present null**, `{}` a **present empty record**, and `[]` a **present empty list**; omission is a separate state and occurs for none of these seven fields. `feature:from>to` denotes the ordered `focus` keys `feature`, `from`, `to`; `key=value` pairs retain `fixed` insertion order. `slot/whereKey=values` denotes `lexicalFilter` with present `slot` and `where`, one present where-key, and an ordered nonempty value list. Lists of prerequisites retain their listed order. `Person=1`, `2`, or `3` uses **text values**, not numbers. The two `mixed` skills have `fixed: {}`; all other current `fixed` records are nonempty. English `focus` is null at positions 3, 13, 15, 16; Polish at 8, 12, 13, 14. The two old files contain no non-ASCII bytes; this corpus therefore does not prove Unicode rendering or escaping in a future Biome source, TypeScript/Kotlin output, or host. That needs an independently specified candidate probe.

The old-source consistency script [check-nj03-old-curriculum-facts.mjs](../../scripts/check-nj03-old-curriculum-facts.mjs) reads only the two locked files and this text fixture. It compares hashes, exact record counts/order, top-level presence and order, nested key order, and every semantic value including null/empty distinctions. It never writes a source file and never consumes a candidate output. It is a static check of this freeze, **not execution of NJ03 DSL or product behavior**. A future source change requires an explicit requirement/content review before revising these independent expectations (P15), rather than changing them to match a generated result.

## Existing acceptance, with the actual gate boundary

`curriculum-v1.schema.json` requires all seven top-level properties and rejects extras. It requires a nonempty `id`, a `construction` beginning with `core.`, an explicit `focus` object or null, a present `fixed` record (possibly empty) whose values are nonempty text, an explicit `lexicalFilter` object or null, one of the enumerated CEFR `level` values, and a present `prerequisites` list (possibly empty) with distinct nonempty text. If present, `focus` has exactly `feature`, `from`, `to`; `lexicalFilter` has exactly `slot`, `where`, with nonempty, unique string lists in a nonempty where-record. This schema does **not** resolve references.

When **explicitly invoked for a file**, `validateCurriculum` applies that schema and rejects duplicate skill IDs, a no-op `focus.from === focus.to`, missing prerequisites, and prerequisite cycles within the supplied list. The normal `npm run course:validate` script invokes its CLI without a file argument, so it checks **Polish** by default; it does not automatically invoke this local schema/DAG validator on English. This freeze explicitly ran it on both files. Do not infer English local validation from `validatePackV2()`.

`validatePackV2()` reads both curriculum files as existing inputs and checks construction IDs against the core construction catalog; `focus`/`fixed` feature keys against the core feature catalog and the language's `usesFeatures`; their values against catalogued values; and lexical-filter `gender`/`nounId` values against the language lexicon. It rejects an unrecognised `where` key. For English, it also checks the 16 pair skill IDs against curriculum IDs in **the same order**, recipe copy/pattern references, and lifehack skill references; Polish lifehack references and v1 reconstruction are separately checked. It does **not** call `validateCurriculum` for either language, validate the `lexicalFilter.slot` value, detect duplicate JSON object keys collapsed by `JSON.parse`, or report candidate authoring token spans. These are distinct checks, not one comprehensive legacy gate.

## Required mutation matrix for the next NJ03 surface

The following are independently specified categories, **not executed candidate cases**. After candidate syntax is pinned, a tester must append concrete source mutations and offending UTF-16 line/column expectations **before** implementation is judged against them. Existing-equivalence rows retain their old meaning; source-integrity rows are explicitly new requirements. The candidate must not weaken existing checks to reach GREEN.

| ID | Mutation against a copy of the current fact set | Required distinction / current authority |
| --- | --- | --- |
| C01 | Omit `focus` from English position 1; separately substitute null for `fixed` and omit `prerequisites` | Existing schema rejects omission/null for required fields; keep absent, null and empty distinct. |
| C02 | Replace English position 1 `focus.to` with `Subj` | Existing `validateCurriculum` rejects no-op focus. |
| C03 | Repeat English position 1 `id` at position 2 | Existing `validateCurriculum` rejects duplicate IDs; order remains part of the positive facts. |
| C04 | Point English position 3 prerequisite to `en:missing` | Existing `validateCurriculum` rejects a dangling same-list prerequisite. |
| C05 | Make English position 1 depend on `en:possessive.my`, which already depends on position 1 | Existing `validateCurriculum` rejects the cycle. |
| C06 | Set English position 1 `construction` to `core.unknown` | Existing `validatePackV2` rejects an unregistered construction. |
| C07 | Add `Bogus=Value`, or set a known fixed feature to a noncatalogued value, at English position 1 | Existing `validatePackV2` rejects unknown feature keys and unsupported values; distinguish the two diagnostics. |
| C08 | Change English position 9 `lexicalFilter.where.nounId[0]` to `missingNoun`; separately use a `where` key other than `nounId`/`gender` | Existing `validatePackV2` rejects both failed lexicon resolution and unrecognised where-key. |
| C09 | Duplicate a prerequisite or lexical `where` value; make a lexical value empty | Existing schema rejects list duplicate/empty items when `validateCurriculum` is invoked. |
| N01 | Repeat an authored field name at one source position | New source-integrity rule: reject before a duplicate can be overwritten; old `JSON.parse` collapses duplicate object keys. Duplicate skill IDs are already covered by C03. Freeze token coordinates after syntax choice. |
| N02 | Change `lexicalFilter.slot` to an unknown nonempty slot while keeping a valid `nounId` | New rule to decide and state explicitly: the current schema/pack checks only nonempty slot text, not slot resolution. Do not call this old parity. |
| N03 | Author an impossible fixed/focus combination | Domain-specific new rule needs an explicit contract and frozen accepted/rejected example; the old validators do not establish every such case. |
| N04 | Malform the new text source, invalidate a feature/ref binding, leave generated modules stale, or remove one generated output | New authoring diagnostic and per-output freshness requirements. Freeze source location and expected outcome independently; never edit a generated file by hand. |
| N05 | Add a valid non-ASCII identifier/value in a complete registry/curriculum fixture and an escaping-sensitive string in a separate accepted probe; add an unpaired surrogate in a rejected probe | New TS/Kotlin/UTF-16 portability proof, not inferable from these ASCII old sources. Specify input bytes, expected code units or rejection and target execution before the candidate run. |

The old source's `focus` null, `lexicalFilter` null, empty `fixed`, and empty prerequisites remain positive cases. A proposed rule that rejects any of them changes the requirement and needs separate owner-level justification. Same-list prerequisites here are direct skill-ID references; their order and edges are all frozen in the `.cases` rows.

## Execution receipt and limits

Working directory: `/Users/german/Work/JS/polski-grammar-srs-nj03-curriculum-facts`, baseline product SHA `2e705f280aebcf3a67204fc6916b922fb3a6b41e`, Node `v24.1.0` on 2026-10-18. The temporary, ignored `node_modules` symlink points to the canonical checkout's installed dependencies. Commands and observed results:

| Command | Actual result |
| --- | --- |
| `node scripts/check-nj03-old-curriculum-facts.mjs` | PASS: 2 exact-path SHA locks, 32/32 expected rows compared, 0 skipped static rows. |
| `node scripts/validate-curriculum.mjs courses/lang/en/curriculum.json` | PASS: English source accepted by the existing local validator. |
| `node scripts/validate-curriculum.mjs courses/lang/pl/curriculum.json` | PASS: Polish source accepted by the existing local validator. |
| `node scripts/validate-pack-v2.mjs` | PASS: existing pack accepted. |

The static checker was also run against two temporary **text** fixture mutations with `--cases`: one changed English position 1 `Case:Subj>Obj` to `Case:Subj>Nom`, and one replaced its source SHA with 64 zeroes. It rejected both (2 selected, 2 executed, 0 skipped). An inline, read-only `node --input-type=module -` probe called the existing `validateCurriculum` on seven separate in-memory copies of English: missing `focus`, null `fixed`, no-op focus, duplicate ID, dangling prerequisite, cycle, and duplicate prerequisite. All seven were rejected as expected (7/7, 0 skipped); this checks only exported old local-validator behavior and does not exercise the pack cross-validator or a candidate.

Candidate DSL mutation cases selected/executed/skipped: **0/0/0; NOT RUN**. Candidate compiler, generated TypeScript/Kotlin values, freshness checks, live `Curriculum`/`PlEngine`/React consumers, browser, Desktop, Android, iOS, and `gate:all`: **NOT RUN**. This corpus does not make NJ03 PASS. Independent reviewer approval remains pending.
