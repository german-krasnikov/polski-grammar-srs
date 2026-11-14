# Project CONSTRUCTs: UiCopy accessor, Compose View host

Architect pass, 2026-11-14. Grounded against `/Users/german/Work/Python/biome-dsl` `AI/ARCHITECTURE.md`,
`PRINCIPLES.md`, `PATTERN_DICTIONARY.md`, `BestPractice.md`, `ProjectMigratioPrincipals.md`,
`docs/product/extending.md`, `AI/DSL/GRAMMAR_V1.md` (construct/declare/render sections), and this repo's
`dsl/biome.project`, `dsl/ui-copy.declare`, `dsl/ui-copy-accessors.mjs`, `TileBuilderWeb.kt`,
`TileBuilderState.kt`, `TileBuilderRules.kt`, plus `Plans/DSL-FEATURE-MIGRATION.md` (prior verdict) and
biome-dsl's own `Plans/COMPOSE_PRINTER_PLAN.md` (2026-11-14, same day, unmerged).

## 0. Correction to the brief before designing

Two claims in the brief do not match what is on disk today. Both change what is actually buildable now:

1. **"Compose View host — project CONSTRUCT + TEMPLATE already built in biome-dsl, `COMPOSE_HOST <Name> IN
   <package>`."** Not found. `grep -r COMPOSE_HOST` across biome-dsl returns nothing. What exists is
   `Plans/COMPOSE_PRINTER_PLAN.md`, a same-day architect plan, **not merged**, whose chosen path (D1) is a
   core-layer flag `--kt-ui compose` on the existing `lang-kt` FEATURE emitter (`packages/languages/kt/src`),
   gated on three unanswered owner questions (Q1–Q3) and explicitly "C7 (polski) not started without Q1."
   The plan's own §3 D12(a) names a **project-scope CONSTRUCT + TEMPLATE** as the fallback if the owner
   declines the core change — zero biome-dsl edit, buildable today. This plan below designs that fallback,
   because it is the only path this repo can act on without depending on biome-dsl's unresolved decision.
2. **"Kotlin FEATURE printer exists in `lang-kt`."** True (`ktEmitter`, `mvp`/`elm`) — this part of the brief
   is accurate and corrects an earlier, now-stale claim in this repo's own `Plans/DSL-FEATURE-MIGRATION.md`
   ("kt … print no dialect FEATURE"), which was about the ENTITY dialect, not plain FEATURE.

Consequence for §3 (tile builder `.feature`): see §3's verdict — this repo already has a grounded, dated
decision against modeling tile-builder state as `.feature`, reached independently twice. The design is
written in full below, but it is **not recommended for build** without an explicit owner override.

## 1. UiCopy CONSTRUCT

### Scope and consumers

Replaces `dsl/ui-copy-accessors.mjs` (hand-rolled regex parser + string-concatenation printer) with a project
CONSTRUCT so the source (`dsl/ui-copy.declare`) gets biome's own grammar (positions, `UNIQUE` enforcement,
diagnostics) instead of a bespoke parser, and the output comes from a declarative `TEMPLATE` instead of JS
string building. Single consumer: `UiCopy.kt` (`kotlin/shared/src/commonMain/generated/kotlin/polski/presentation/UiCopy.kt`),
read by every host (web/Android/Desktop/iOS via KMP `commonMain`). This is the "boilerplate shape of your
project" extension point (`extending.md` row 1) — no biome-dsl core change.

### Public contract

New files, `SCOPE project`:

- `dsl/constructs/ui-copy.construct` **NEW**
- `dsl/constructs/ui-copy.examples` **NEW**
- `dsl/ui-copy.declare` — **syntax changes** (see Migration below); same file path, same role as single source.
- `dsl/ui-copy-coverage.mjs` **NEW**, replaces the `checkCoverage` half of `ui-copy-accessors.mjs`.
- `dsl/ui-copy-accessors.mjs` **DELETE** once the two steps below are green.

```text
SURFACE ui-copy CODES UICOPY CONSTRUCTS CATALOG COMMENT line

CONSTRUCT CATALOG VERSION 1 IR 1 SCOPE project
  HEAD CATALOG <object:ident> IN <package:qname>
  ITEM KEY <key:word UNIQUE> [AS <accessor:ident UNIQUE>] [PARTIAL] [DOC <doc:string>]
  ITEM FN <name:ident UNIQUE> <template:string> <param:ident> [DOC <doc:string>]
  EXAMPLES ui-copy.examples
  DECLARE
  TEMPLATE kt HEADER "// Generated from dsl/ui-copy.declare, checked against courses-biome/pl-ru/uiCopy.core-data and courses-biome/en-ru/uiCopy.core-data.\n// Edit dsl/ui-copy.declare, then run biome generate."
    package {package}

    import polski.data.packRegistry

    /**
     * Pack-aware UI string catalog. Each property reads the active pack's `uiCopy` map,
     * so switching packs changes all labels. Fallback = key name (visibly broken if a pack omits it).
     */
    object {object} {
        /** Complete read-only label snapshot for the currently selected course pack. */
        fun all(): Map<String, String> = packRegistry.active.uiCopy.toMap()

    EACH KEY
        {?doc: /** {doc} */}
        {?accessor: val {accessor}: String get() = text("{key}")}
        {!accessor: val {key:PascalCase}: String get() = text("{key}")}
    END
    EACH FN
        {?doc: /** {doc} */}
        fun {name}({param}: String): String = text("{template}")
    END
    }

    private fun text(key: String): String = packRegistry.active.uiCopy[key] ?: key
  END
END
```

This is the `labels` recipe from `extending.md` ("A catalog of accessors printed from a surface") plus two
extras this project's current script already has and the recipe doesn't: `PARTIAL` (OR-coverage across
packs instead of AND) and `FN` (one parameterized accessor per entry, e.g. `languageName(code) = text("lang.$code")`).
Both are expressed with ordinary `ITEM`/slot primitives — no new core grammar.

**Dropped, by design: floating `#`-comment-to-KDoc attachment.** The current script's "a `#` line directly
above an entry becomes its KDoc" is custom parsing; DECLARE's own comment handling (`COMMENT line`) only
*skips* `#` lines, it does not capture them as data. Replaced with an explicit `DOC "<text>"` item clause on
the entry's own line. Today exactly one entry uses the floating form (`Missions`, "M9 blueprint: pl-ru pilot
only …") — a one-line migration, not a feature loss.

### Components and dependencies

- `dsl/constructs/ui-copy.construct` + `.examples` — the CONSTRUCT and its mandatory positive/negative
  examples (a construct is rejected without both, per `extending.md`).
- `dsl/ui-copy.declare` — rewritten to the CATALOG/KEY/FN syntax (see Migration). Single source of truth,
  unchanged in role.
- `dsl/ui-copy-coverage.mjs` **NEW** (~30 lines) — a `script` step. Uses the documented "Read a SURFACE in a
  script" recipe (`extending.md`): `surfaceDefs`/`readSurface` from `${BIOME_CLI}?lib` to get every `KEY` use
  (key, accessor, partial flag) straight from the parsed CONSTRUCT — no regex — then `biome read <pack>.core-data
  --format json` for each pack's `uiCopy` keys, and asserts: non-`PARTIAL` key present in **every** named pack,
  `PARTIAL` key present in **at least one**. Throws with the offending key/pack on the first gap (same failure
  shape as today: `generate` fails before a stale accessor ships).
- `kotlin/shared/.../UiCopy.kt` — **generated**, printed by the `render` step's `TEMPLATE kt`, unchanged output
  shape/package from today's hand-built string.

### `biome.project` integration

First use of `CONSTRUCTS` in this repo's `dsl/biome.project` — add once, near the top:

```text
CONSTRUCTS constructs
```

Replace line 73 (`GENERATE ui-copy-accessors derive {dir}/ui-copy-accessors.mjs generate OUT`) with:

```text
GENERATE ui-copy render --surface ui-copy {dir}/ui-copy.declare kt {dir}/../kotlin/shared/src/commonMain/generated/kotlin/polski/presentation/UiCopy.kt
GENERATE ui-copy-coverage script {dir}/ui-copy-coverage.mjs
```

`render` writes `UiCopy.kt`; `generate --check` compares it byte-for-byte as today. The coverage script runs
after (file order = execution order, per `extending.md`), failing the whole `generate` if a key is missing —
same gate as the current `derive` step's thrown error.

### Migration of `dsl/ui-copy.declare`

Mechanical, one pass: `Name key` → `KEY key AS Name` only where `Name` isn't the default PascalCase of `key`
(most entries keep the implicit form, e.g. `Again again` → `KEY again`, since `{key:PascalCase}` already
yields `Again`); the few deliberate renames (`ExamCheck check`, `Lifehacks lifehacks` is already aligned, etc.)
keep `AS`. Wrap the whole file in `CATALOG UiCopy IN polski.presentation` / `END`. The `FN` lines keep their
shape (`FN languageName lang.$code code` → `FN languageName "lang.$code" code`, string-quoted per the `string`
ptype). The one floating-comment doc becomes `KEY missions DOC "M9 blueprint: pl-ru pilot only — en-ru falls back to this key's own name until missions are authored for that pack too."`.

### Verification (acceptance cases)

- AC1: `.examples` — one positive (`KEY save`, `KEY close AS CloseButton DOC "..."`, `FN languageName "lang.$code" code`)
  renders the three matching Kotlin lines incl. the KDoc comment; one negative, a duplicate `KEY save` twice,
  is `UICOPY_DUPLICATE` at the second use's line/column (the construct's own `UNIQUE` check, no custom code);
  one negative, `CATALOG UiCopy` with no `IN`, is `UICOPY_SYNTAX` at 1:1.
- AC2: `biome generate --project dsl/biome.project` reproduces today's `UiCopy.kt` byte-for-byte after the
  declare-file migration (diff against the pre-migration generated file is empty) — proves the migration
  preserved every accessor and doc comment.
- AC3: `biome generate --check` fails with a named key/pack when a `uiCopy.core-data` row is deleted (parity
  with today's `sed 's/key: cancel /key: abort /'` failure demo in `extending.md`'s own labels walkthrough).
- AC4: a `PARTIAL` key present in only one pack passes; present in neither pack fails with both pack names.
  (No such key exists today — this path is new-but-untested until one is added; note it as the first real
  user before calling `PARTIAL` anything beyond "ported, unused.")
- AC5: `npx biome generate --check` run from `implementations/typescript` is irrelevant here (that's
  biome-dsl's own gate); this repo's check is `node <biome-cli> generate --check --project dsl/biome.project`
  from `polski-grammar-srs` root, per `AGENTS.md`'s existing workflow.

### Risk flagged for the developer

`readSurface`'s exact `uses[].items[]` shape for a **two-ITEM-kind** construct (`KEY` and `FN` both under one
`CATALOG`) is documented loosely ("items, each a list of … maps"; "only the fields named here are promised" —
`extending.md`). Probe this in a scratchpad script against the real toolchain before writing
`ui-copy-coverage.mjs` for real: confirm each item map exposes which `ITEM` kind it came from (needed to skip
`FN` items when walking `KEY`s for coverage). If it doesn't, fall back to printing a `TEMPLATE keys` text
target (proven in the `labels` recipe) and parsing that instead of introspecting `uses` — slightly more code,
zero dependency on an underspecified field.

## 2. Compose View host CONSTRUCT (project-scope, biome-dsl's own D12(a) fallback)

### Scope and consumers

A project `CONSTRUCT COMPOSE_HOST` that prints the Compose-runtime glue class (`<F>ComposeView`) and
`@Composable` entry point (`<F>Screen`) around a Kotlin FEATURE module already generated by
`biome feature generate --lang kt` (mvp or elm pattern — both produce the same `<F>ViewModel`,
`sealed interface <F>Intent`, `interface <F>View`, `<F>Ports`, `<F>Decisions`, `fun mount<F>(view, ports,
decisions): Closeable` contract, per `COMPOSE_PRINTER_PLAN.md` §2). This CONSTRUCT does **not** itself model
STATE/ACTION — it only wires the already-generated View contract into Compose's `mutableStateOf` +
`DisposableEffect`. Android and Desktop (JVM `commonMain`/`androidMain`) are direct consumers; iOS stays
hand-written SwiftUI (no Compose on iOS in this project; no `lang-swift` in biome-dsl).

### Public contract

```text
SURFACE compose-host CODES COMPOSEHOST CONSTRUCTS COMPOSE_HOST

CONSTRUCT COMPOSE_HOST VERSION 1 IR 1 SCOPE project
  HEAD COMPOSE_HOST <feature:ident> IN <package:qname>
  ITEM DECISIONS <expr:text>
  EXAMPLES compose-host.examples
  DECLARE
  TEMPLATE kt {feature}.Compose.kt HEADER "// Generated by the project COMPOSE_HOST construct. Edit the .compose-host source, then run biome generate."
    package {package}

    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.DisposableEffect
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import java.io.Closeable

    /** The View of {feature} as Compose state: `render` keeps the latest view model, `emit` hands an intent to every subscriber (none after `close()`). */
    class {feature}ComposeView : {feature}View {
        var model by mutableStateOf<{feature}ViewModel?>(null)
            private set
        private val handlers = ArrayList<({feature}Intent) -> Unit>()
        val subscribers: Int get() = handlers.size

        override fun render(viewModel: {feature}ViewModel) { model = viewModel }
        override fun onIntent(handler: ({feature}Intent) -> Unit): Closeable {
            handlers += handler
            return Closeable { handlers -= handler }
        }
        fun emit(intent: {feature}Intent) = handlers.toList().forEach { it(intent) }
    }

    /** Mounts {feature} while this call is in the composition (mount on enter, `close()` on leave). `content` draws the latest model and is not called before the first render. */
    @Composable
    fun {feature}Screen(
        ports: {feature}Ports,
        decisions: {feature}Decisions = {?decisions: {decisions}}{!decisions: {feature}LogicDecisions()},
        view: {feature}ComposeView = remember { {feature}ComposeView() },
        content: @Composable ({feature}ViewModel, ({feature}Intent) -> Unit) -> Unit,
    ) {
        DisposableEffect(view) {
            val handle = mount{feature}(view, ports, decisions)
            onDispose { handle.close() }
        }
        view.model?.let { content(it, view::emit) }
    }
  END
END
```

This is a near-verbatim port of `COMPOSE_PRINTER_PLAN.md` §3.1's owner-reviewed sketch (D3/D4: runtime-only
imports, `view` as the `remember`/`DisposableEffect` key so a stable `ports` instance never causes a remount,
`DisposableEffect` not `remember` so the mount survives a cancelled composition). Reusing that sketch instead
of inventing a new one keeps this CONSTRUCT's design risk at "transcription," not "new API."

### Components and dependencies

- `dsl/constructs/compose-host.construct` + `.examples` **NEW**.
- `dsl/feature/<name>.feature` **NEW per consumer** — the FEATURE module (STATE/ACTION/DECISION), compiled by
  the existing `feature generate --lang kt` step (already-shipped `ktEmitter`, no new biome-dsl work).
- `dsl/feature/<name>.compose-host` **NEW per consumer** — one `COMPOSE_HOST <Name> IN <pkg>` block, the
  CONSTRUCT's own source file.
- Hand-written layout composable, **one per consumer**, e.g. `<Name>Layout.kt` — the part `scope.md` keeps
  native (`ARCHITECTURE.md` §6.1: "DSL does not generate layout/styles"). Calls `<Name>Screen(ports, content =
  { vm, emit -> <Name>Layout(vm, emit) })`.
- Hand-written `<Name>Ports` adapter to this project's repositories (native, physical SDK binding — P4).

`biome.project` additions, per consumer:

```text
GENERATE <name>-feature feature generate --feature {dir}/feature/<name>.feature --lang kt --kt-package <pkg> --out {dir}/../kotlin/.../generated/<name>
GENERATE <name>-compose-host render --surface compose-host {dir}/feature/<name>.compose-host kt {dir}/../kotlin/.../generated/<name>/<Name>.Compose.kt
```

### State and resources

Ownership unchanged from `COMPOSE_PRINTER_PLAN.md` §4: the FEATURE-generated `mount<F>` owns mutation (P12);
the Compose host owns subscription lifetime (`onDispose { handle.close() }`) and nothing else; layout is
human-owned. Single-threaded contract: mount, intents and ports run on the Compose main thread; the model is
not thread-safe (same as the plan's own documented limit).

### Decisions and tradeoffs

- **Project-scope, not core-scope.** biome-dsl's own D1 (a `--kt-ui compose` flag on `ktEmitter`) would be
  cheaper per-feature (one flag, no second file) and gets Gradle-proven test coverage for free once merged —
  but it depends on an unmerged, unanswered plan in a repo this project doesn't own. The project CONSTRUCT
  costs one extra `.compose-host` file per feature and is fully this repo's to maintain, but needs nothing
  from biome-dsl and matches `extending.md`'s own stated tradeoff for D12(a). **Revisit once biome-dsl answers
  Q1–Q3**: if `--kt-ui compose` lands, retire this CONSTRUCT in favor of the flag (P7 — don't keep two ways to
  print the same glue) and keep `compose-host.construct` only if it has grown features the flag doesn't cover.
- **Why one `ITEM DECISIONS <expr>` and not more options:** the sketch's only real per-feature variable is how
  `decisions` is constructed (policy default vs. `--kt-decisions` binding vs. a project-specific factory). A
  FEATURE with no `DECISION` needs no `decisions` parameter at all in the real contract — that case is out of
  this CONSTRUCT's v1 scope (document as a known limit, add `[ITEM NO_DECISIONS]` or detect it later if a real
  consumer needs it; don't build it speculatively, P13).
- **Rejected:** teaching COMPOSE_HOST to read an arbitrary hand-written Kotlin interface (skip FEATURE
  entirely) so it could wrap existing native state like `TileBuilderState` directly. That would turn the
  CONSTRUCT into a generic Kotlin-interface-to-Compose-wrapper macro with no FEATURE grounding — a bigger,
  vaguer surface than "glue for a FEATURE View contract," and exactly the shape `PATTERN_DICTIONARY` §5 rule 6
  warns against (a new template form needs ≥2 real consumers, not a speculative generalization for one).

### Verification (acceptance cases)

- AC1: `.examples` — `COMPOSE_HOST Settings IN polski.generated.settings` with no `DECISIONS` line renders
  `class SettingsComposeView : SettingsView {`, `fun SettingsScreen(`, and
  `decisions: SettingsDecisions = SettingsLogicDecisions(),`; a second example with
  `DECISIONS "SettingsDecisionsFromPrefs(prefs)"` renders that expression in the default position instead;
  a negative example, `COMPOSE_HOST Settings` with no `IN`, is `COMPOSEHOST_SYNTAX` at 1:1.
- AC2: generated `<Name>.Compose.kt` imports only `androidx.compose.runtime.*` and `java.io.Closeable` — no
  `androidx.compose.foundation`/`.material`/`.ui`, matching D4 (layout-free).
- AC3: compiles against a real `feature generate --lang kt` output for a small pilot FEATURE in this repo's
  own Gradle build (`:composeApp` Android source set) — the actual proof point, since biome-dsl's own `.verify`
  machinery doesn't run Kotlin/Compose compilation for a project-scope construct.
- AC4: `biome generate --check` is clean after a no-op re-generate (P16); editing the generated `.Compose.kt`
  by hand and re-running `--check` fails, naming the file (same demonstrated behavior as `extending.md`'s
  `Limits.cs` example).

### First consumer recommendation

Per `COMPOSE_PRINTER_PLAN.md` §8 Q1's own recommendation and this repo's `Plans/DSL-FEATURE-MIGRATION.md`
verdict (§3 below): pilot this CONSTRUCT on a **new, self-contained** automaton — the settings panel is the
concrete candidate already named in both documents (it has a Desktop and a web hand-written twin today,
`DesktopSettingsScreen.kt`/`SettingsWeb.kt`, the same automaton biome-dsl's own `corpus/apps/settings-feature`
demo uses) — not the tile builder. See §3.

## 3. Tile Builder `.feature` — designed, but not recommended

### What was asked

A `dsl/training/tile-builder.feature` modeling `TileBuilderState`/`TileAction` (Question/Revealed phases;
Place/Remove/Check/TapError) so SB-9/SB-10 (Android/iOS tile UI) generate from one DSL source via
`COMPOSE_HOST`.

### Why this repo already decided against it, twice

1. **`Plans/DSL-FEATURE-MIGRATION.md` (this repo, earlier)** reviewed exactly this candidate and rejected
   `.feature`/FLOW migration for the tile builder, Android/iOS included, on two grounds: (a) at the time, no
   Kotlin FEATURE backend existed (now stale — it does, `ktEmitter`) and (b) **economics**: single-target DSL
   costs more lines than native (biome-dsl's own measured boundary), and this project's generation target is
   effectively one language (Kotlin, shared via `commonMain`) — the break-even point for Logic/FEATURE DSL is
   ≥2 target languages. Reason (a) is outdated; reason (b) is not.
2. **`COMPOSE_PRINTER_PLAN.md` (biome-dsl, same day)** reaches the same conclusion independently, in its own
   P25 stop-rule table (§6): `TileBuilderRules`/`togglePanel` are listed as **already won by native** (merged
   `fae06a1`), stop-signals S1 (not longer than native) and S4 (single consumer — Kotlin only, Swift sees it
   through the bridge, not a second DSL target). The same document explicitly names the duplication risk by
   name: *"для SB-9 (Android tile builder) [a FEATURE-first module] дублировал бы `TileBuilderState`"* — i.e.
   its own authors flag that building `tile-builder.feature` would create a second, parallel source of the
   same state (`TileBuilderState` in `TrainingStore` vs. FEATURE `STATE`/`ACTION`), a direct P7 violation
   ("one fact, one place") unless `TrainingStore` is rewritten to read from the generated model instead of
   owning its own — a materially bigger migration than "add a `.feature` file."

Both documents independently land on: the **rules** (`tileBuilderCanCheck`, `togglePanel`) are correctly
native already; the **state shape** (`TileBuilderState`, nested in the larger `TrainingStore`/`AppUiState`)
is not a self-contained automaton FEATURE models cleanly — it is one case inside a bigger existing reducer,
and modeling it separately in FEATURE would fork it, not replace it.

### Sketch, for completeness (not vetted against a real `biome feature check`)

If the owner overrides the above and still wants it:

```text
FEATURE TileBuilder USES mvp
  STATE phase : enum question|revealed = question
  STATE openPickerPosition : int[-1..63] = -1
  STATE openErrorPosition : int[-1..63] = -1
  STATE correct : bool = false
  PORT loadExercise(exerciseId: text)
  PORT place(position: int, form: text)
  PORT remove(position: int)
  PORT check()
  PORT tapError(position: int)
  # ... placed tiles and per-choice candidate lists need list<record{...}> STATE or a PORT-backed read;
  # the real TileExercise/TileSegment/TileSelection/TileAnswer domain types do not map onto FEATURE's
  # int/bool/enum/text/ref/list<record> primitives without a dedicated modeling pass — this is the
  # open part, not sketched further here.
  EXAMPLES tile-builder.examples
END
```

This is intentionally incomplete: `TileExercise`'s segments (`Locked`/`Choice`, each with `correctForm` +
`alternatives: List<String>`) and `TileAnswer.errors` are richer than a quick STATE mapping can honestly
claim without a dedicated FEATURE-dialect modeling session — fabricating a complete grammar here would
violate "ground existing components" for a design this plan does not recommend building anyway.

### Recommendation

Do not build `tile-builder.feature`. If SB-9/SB-10 need the tile builder's "one description, two hosts" on
Compose, the already-accepted path is: keep `TileBuilderState`/`TileBuilderRules` native and shared (done,
`fae06a1`), and let the Android Compose renderer be ordinary hand-written Compose calling those shared
functions — the same relationship the web DOM renderer (`TileBuilderWeb.kt`) already has with
`TileBuilderRules.kt`. Revisit only if biome-dsl names a second real target language for this state (the
actual P25 break-even condition), which is not on this project's roadmap today.

## Documentation and handoff

- §1 (UiCopy) is ready to hand to a developer task: new construct + examples, declare-file migration,
  coverage script, `biome.project` edit, delete the old `derive` script once green.
- §2 (Compose host) is ready to hand to a developer task **once a first consumer FEATURE is chosen** — do not
  start with the tile builder (§3); the settings panel is the grounded candidate.
- §3 is a recorded verdict, not a task. If the product owner wants to override it, that override — and the
  `TrainingStore` rework it implies — needs its own architect pass; this plan does not authorize it.
- Record the `COMPOSEHOST`/project-CONSTRUCT-vs-core-flag tradeoff in `AI/decisions.md` if the team wants the
  "revisit when biome-dsl answers Q1–Q3" trigger discoverable later, per this repo's own convention
  (`CLAUDE.md`: "дополняй `AI/decisions.md` при новых решениях").
