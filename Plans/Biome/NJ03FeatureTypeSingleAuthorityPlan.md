# NJ03: one authority for curriculum `Fixed` feature keys

Status: architecture handoff, **not implemented or validated**. The target is
the isolated `dev/nj03-typed-curriculum` candidate at `db2cff9`; this plan is
recorded separately from canonical product `206ed2c`. The candidate remains
transitional and cannot be promoted as a no-JSON product.

## Finding and invariant

`courses/core/registry.core-data` authors seven ordered `FEATURE` keys: `Case`,
`Number`, `Person`, `Tense`, `Polarity`, `Mood`, `Aspect`. Candidate
`courses/lang/en/curriculum-types.program:2` manually repeats exactly these
seven names in `TYPE Fixed = record{...}`. This is an accidental P7 copy, not
an independently specified narrower `fixed` whitelist: the existing English
curriculum uses `Aspect` in `fixed`, Polish has no `Aspect` in its language
`usesFeatures`, and the project validator already applies that language
restriction separately. The independently frozen 32 skill values and old
feature relation require every authored fixed key to exist in the registry,
be enabled by the language, and carry one of the registry values. `Gender` is
a lexicon filter property, not an eighth Core feature. Do not infer a
`fixed`-eligible subset from the present 32 rows.

The registry owns feature **identity and values**. A typed language profile
owns `usesFeatures` and any language-specific value subset such as `Case`;
the curriculum owns references and their source spans. `Fixed`'s structural
shape is the derived set of all registry feature keys, each
`optional<text>`. The per-language and value checks remain project relation
checks. A registry addition changes the derived type automatically but does
not silently enable that feature in either language. If the domain later
needs a narrower `fixed` policy, specify its meaning and independent cases
first, then author that policy once alongside the `FEATURE` fact (for
example an explicit registry property); do not reintroduce a list in a
curriculum type or JS helper.

## Existing capability and chosen composition

Public Biome Core already provides `parseDefinitions`, `parseSurface`,
`readProgramTypes(source, sourceId, parent?)`, and
`readLocatedExprValueResult` in `implementations/typescript/packages/core/src`.
`readProgramTypes` resolves a named `record` through a parent type registry;
its canonical type parser rejects duplicate/invalid members. The NJ02
project validator at `scripts/nj02/registry/validate.mjs` already validates
the registry's `FEATURE` records with the located Core reader and rejects
duplicate feature keys. The NJ03 `readCoreCatalog` adapter preserves source
order in its `Map`. No Core API, new DSL syntax, generic feature map, or
second tokenizer is needed.

Change the candidate's authored
`courses/lang/en/curriculum-types.program` by removing only `TYPE Fixed`;
keep `Focus`, `FilterWhere`, `FilterSlot`, `LexicalFilter`, `Level` and `Skill`
there, including `fixed:Fixed`. A small build-only project function,
**NEW** `scripts/nj03/curriculum/feature-types.mjs`, accepts the already
validated ordered `coreCatalog.features` and the authored type source. It
constructs the deterministic one-line DSL declaration
`TYPE Fixed = record{Case:optional<text>,Number:optional<text>,...}` in registry
order, calls `core.readProgramTypes` on that generated line, then calls
`core.readProgramTypes` on the authored file with the first result's registry
as `parent`. It returns the resolved `Skill` type, derived declaration text,
and canonical structural read-plan signature. Preserve the authored file's
own line coordinates by parsing it separately with `parent`; a parse error in
the derived declaration names the registry-derived artifact. Reject an empty
feature registry, duplicate/reserved identifier, invalid type, or unresolved
`Skill`; never fall back to a handwritten list or `data` map.

`scripts/nj03/curriculum/build.mjs` and
`scripts/nj03/curriculum/validate.mjs` must use that *same resolved type* for
both language sources. Refactor `verify.mjs` and the focused diagnostic
checker to call the same composition; their expected values remain in
independent `.cases`/`.examples`, not in this helper. Shared Logic/Test
`LET` variables do not add value for this static feature catalog; use them
when a real repeated executable test input appears. Emit the derived line
as **NEW generated, ignored**
`kotlin/shared/build/generated/biome/curriculum-fixed.program` for inspection.
The existing `--check` must compare its exact bytes as well as the Kotlin
output and manifest, without writing. Add its digest and the fully resolved
`Skill` read-plan digest to `curricula.manifest`, with the registry, authored
types and helper among its hashed inputs. `generateBiomeCurricula` declares
the new artifact as an output; `checkBiomeCurricula` remains read-only.
`freshness.mjs` copies and tampers all three generated artifacts and checks
restoration in its disposable tree. A changed registry that leaves Kotlin
skills byte-identical must still fail `--check` until regeneration because
the derived type/read plan and input hash changed. All generated files are
reproduced from authored DSL and checked; none is edited by hand.
Update `kotlin/shared/build.gradle.kts` inputs for the new helper and include
`scripts/nj03/curriculum/examples.mjs`, which the current manifest hashes but
the current task input list omits.

The generated `.program` is a build artifact, never an application format.
No JSON application output is introduced. For the *final cutover*, replace
`historicalLanguageCatalog(JSON.parse(lang.json), JSON.parse(lexicon.json))`
in `build.mjs`, `verify.mjs` and focused checks with validated typed language
profile/lexicon facts; remove the old JSON inputs from Gradle task inputs and
manifest. `PackEngine`'s default JSON curriculum path and the JSON-fed forms,
recipes and realization still block no-JSON/production acceptance. This plan
does not claim to implement those migrations. The typed language profile
must expose `usesFeatures` as a set of references to registry keys and its
language-specific case subset; it must reject unknown/duplicate references
with authored locations. It is a downstream interface, not a second list of
global features or a broad language-profile implementation here.

## Independent acceptance before implementation

Freeze two new mutation cases in a separate `.cases` ledger before candidate
diagnostics are consulted. Preserve all existing NJ03 32-row and mutation
ledgers unchanged. Coordinates below use the current ASCII one-line source;
an independent tester must seal the exact mutated bytes and UTF-16 spans.

1. **Registry gains a feature, language does not.** Append
   `FEATURE {key: Evidentiality values: [Direct]}` to a disposable registry
   source; in English skill `en:role.object` append
   `Evidentiality: Direct` inside `fixed` after `Number: Sing` (current source
   line 2). The derived structural type accepts the key. The English
   `usesFeatures` still excludes it, so the first project error is
   `unknownFeature`, public code `CURRICULUM_UNKNOWN_FEATURE`, path
   `fixed.Evidentiality`, at the **key** line 2 column 156, length 13. Also
   assert that adding the key only, without changing a skill, makes
   `build.mjs --check` stale; after generation the 32 old values remain
   unchanged. A separate positive control enables the feature through the
   validator's language-catalog input and accepts the same `fixed` value;
   after the language-profile cutover, run that control through the typed
   profile. This demonstrates that the derived type has no hidden static
   whitelist.
2. **Registry loses a referenced feature.** Delete only the `Aspect`
   `FEATURE` fact in a disposable registry source, leaving English skill
   `en:verb.presentSimple` unchanged. The type no longer has `Aspect` and
   Core rejects its `fixed` member before the relation pass: reason `extra`,
   public code `CURRICULUM_EXTRA`, path `fixed.Aspect`, at the **key** line 3
   column 145, length 6. Assert no Kotlin artifact is generated from this
   invalid source. A disposable tamper of `curriculum-fixed.program` alone
   must be rejected by `--check` without rewriting it.

The acceptance runner must also execute the unchanged 32/32 semantic oracle,
all previously frozen negative rows, both construct examples through their
actual evaluator, source/type/binding/output freshness, and the focused
Kotlin Desktop consumer and generated-value tests on an immutable candidate
revision. Record selected/executed/skipped/failure counts and exact compiler
pin. JS/Wasm and other hosts remain NOT RUN until actually executed. Run
`npx biome check changed` from the compiler directory after DSL edits; any
`not checked` product file needs its own public Core surface/type/example
check and an explicit report, not a green CLI claim. The product's full
no-JSON inventory and P23 real-pipeline benchmark remain separate gates.

## Cost and handoff

| Choice | P7 guarantee | P8 cost and decision |
| --- | --- | --- |
| Native `FIXED_KEYS` constant/helper with seven literal names | Still a second authorial list beside seven registry facts; a drift test detects but does not remove it. | Smallest immediate edit, but every feature change has two semantic sites and a missed update can reject valid skill data. Reject. |
| New Core `recordFromFeatureCatalog` syntax or generic declarative cross-surface type import | Could centralize the keys. | New grammar, portability and P14/P23 obligations for one product use; no measured advantage over the existing public parent-registry API. Defer. |
| One project derivation from validated `FEATURE` facts using Core `readProgramTypes` parent | One authored feature identity, structural refusal with source span, unchanged semantic relation checks. | One small build-only helper and generated-line freshness; uses the existing generator/task. Choose and measure against the static helper at N=10/100/1000 with real emitters before a P23 claim. |

Developer handoff: implement only the feature-type derivation and its
freshness/negative controls in the isolated candidate; do not rewrite the
whole compiler or switch production curriculum in this step. Independent
tester and read-only reviewer must check P7, P8, P14, P15, P16, P17 and
P23 evidence. Current status is a design, with **zero new tests or Gradle
tasks executed** and no production PASS claim.
