# `.feature` GROUP+RESET design for TrainingStore — and why it cannot compile to Kotlin here

Commissioned question: design a `dsl/feature/training.feature` (biome-dsl, `USES elm`, `GROUP`/`RESET`) that replaces
the seven `~14`-line `.copy()` blocks in `TrainingStore.kt` (`startChain`, `startSchedule`, `chooseSkill`,
`startDaily`, `continueDaily`, `trainExamMistake`, `resetDecision`) with generated Kotlin.

## Headline finding (read this before the rest)

**The Kotlin FEATURE emitter (`mvp`/`elm`) cannot be used from `TrainingStore.kt` as it stands.** `TrainingStore` lives
in `kotlin/shared/src/commonMain/kotlin/polski/presentation/TrainingStore.kt`, and `kotlin/shared/build.gradle.kts`
targets `jvm("desktop")`, `android`, `iosArm64`/`iosSimulatorArm64`/`macosArm64`, **and `js { browser() }` /
`wasmJs { browser() }`** (lines 172–187). Every file the Kotlin `elm` pattern prints imports `java.io.Closeable`,
which exists only on the JVM target:

- `packages/languages/kt/src/kt-parts.ts::ktView` — the `${n}View` interface (`onIntent(...): Closeable`) — prints
  `import java.io.Closeable` (last arg of its `kt(...)` call). This same file is where the **enum classes and the
  `${n}Intent` sealed interface live** (`ktView`'s `enums`/`intents` locals), not in `State.kt`/`Update.kt`.
- `packages/languages/kt/src/patterns/elm.ts::host` — `${n}Host`, `Closeable` subscription bookkeeping, same import.
- `State.kt`/`Msg.kt`/`Update.kt` (`ktElmFiles`) have no `Closeable` import *themselves*, but every type they
  reference by name (`TrainingPhase`, `TrainingMode`, `TrainingIntent`, …) is **defined inside `View.kt`** — same
  Kotlin package, no explicit import, so excluding `View.kt` from a sourceSet breaks `State.kt`/`Update.kt` too.
  There is no way to keep only the Closeable-free files without hand-splitting generated output (forbidden, P16).

This project already hit exactly this wall and already made the call: `Plans/DSL-CONSTRUCT-PLAN.md` (the
`COMPOSE_HOST`/`AppearanceSettings` pilot, the only existing Kotlin FEATURE consumer in this repo) states explicitly
*"Android and Desktop (JVM `commonMain`/`androidMain`) are direct consumers; iOS stays [native]"* because of the
`Closeable` contract, and wires its generated files into `kotlin/composeApp/src/jvmGenerated/kotlin` — **not**
`commonMain`, specifically because that output "imports `java.io.Closeable`, unavailable on composeApp's js/wasmJs
targets" (`dsl/biome.project:142-147`, `dsl/feature/appearance-settings.feature:1-6`). `TrainingStore` has no
JVM-only escape hatch available: it is the one class every target (web, Android, Desktop, iOS) drives through.

So this plan gives the `.feature` file the task asked for (Option A, §2–4) as a **reviewable spec**, not a build
step, and gives the actually-buildable replacement (Option B, §5) that gets the same declarative GROUP+RESET shape
without the toolchain blocker. §6 is the tradeoff table; recommendation is Option B now, Option A only if biome-dsl's
`lang-kt` elm/mvp backend is changed to stop coupling pure State/Msg/Update types to the Closeable-based View/Host
(a biome-dsl toolchain change, out of this repo's scope — flag back to the owner, do not build around it silently).

## 1. Scope and consumers

- **Capability**: declarative "reset to a fresh card/session" for seven `TrainingStore` actions, read from
  `kotlin/shared/src/commonMain/kotlin/polski/presentation/TrainingStore.kt` (872 lines) against
  `AppUiState`/`AppAction`/enums in `AppUiState.kt` (126 lines) and `AppAction.kt` (41 lines).
- **Consumer**: `TrainingStore` only (single consumer — relevant to the P25/P13 comparison in §6).
- **Targets**: every KMP target `kotlin/shared` builds (`desktop` JVM, `android`, `iosArm64`, `iosSimulatorArm64`,
  `macosArm64`, `js`, `wasmJs`). No target may be dropped for this change.
- **Out of scope**: FSRS/scheduler calls, `exerciseEngine`/`document` IO, `AttemptLogRepository`/`ProgressRepository`
  writes, the `Write` channel, effects (`addFocus`, `refreshTime`), `tileBuilder`/`introPending`/`predictionPending`
  derivation (all native helpers already in `TrainingStore`) — none of that is touched by either option.

## 2. Real fields vs. the owner's illustrative draft

The owner's syntax draft used placeholder enum tags for readability. Grounded against the actual types, several do
not exist and must be corrected:

| Owner's draft | Real type (`AppUiState.kt`) | Correction |
|---|---|---|
| `tab : enum home\|training\|settings` | `AppTab { Training, Vocabulary, Matrix, Progress }` (line 11) | no `home`/`settings` tag; use the 4 real tags |
| `mode : enum idle\|chain\|schedule\|focused\|daily\|exam` | `TrainingMode { Chain, Schedule, Focused, Daily }` (line 10) | **no `idle`, no `exam` tag.** `resetDecision` sets `mode = TrainingMode.Chain` (line 794), not an idle placeholder. `trainExamMistake` sets `mode = TrainingMode.Focused` (line 557) — the "exam" distinction is carried by `sessionSource`/`examRetryOrigin`, never by `mode` |
| `phase : enum question\|answer\|result` | `CardPhase { Question, Revealed, ChainComplete, NoDue, DailyComplete }` (line 18) | real tags, and `phase` is often **conditional on native nullability** (`if (exercise == null) CardPhase.NoDue else CardPhase.Question`, lines 428, 449, 478) — not a fixed default in 3 of 7 actions |
| `frozenAnswer/evaluation/error : optional<text>`/`optional<Evaluation>` | `val frozenAnswer: String? = null`, `val evaluation: Evaluation? = null`, `val error: String? = null` | **`optional<T>` is not a STATE type.** `GRAMMAR_V1.md`'s `feature` surface STATE grammar (`type := int[..] \| bool \| enum .. \| text \| ref \| list<..>`) has no `optional<>` — that form exists only for PROGRAM/`.core-data` (`optional<>` registry entry, `GRAMMAR_V1.md:1227`), and the Kotlin printer's `ktType` (`kt-parts.ts:10`) maps unknown/`ref` types to `Any`, never `T?`. These three fields cannot be STATE at all |
| `sessionSource : enum none\|chain\|schedule\|focused\|daily\|exam = none` | `SessionSource { ManualPick, DailyButton, ScheduleDue, Chain, Reset, ExamMistake }`, nullable, default `null` | no `none` tag; each action sets a **distinct, literal** tag — never "reset to a shared default" |
| `sessionId : text = ""` | `val sessionId: String? = null`, minted via `sessionIds.newId()` (a stateful counter, `TrainingStore.kt:67-70`) | not expressible: `USE PURE` requires a deterministic, side-effect-free function (`ProjectMigratioPrincipals.md` P4/P12); a counter that mutates on every call is not that. Minting must stay native regardless of which option is picked |

Also: **generated enum types are never the real ones.** `enumName(f, x) = S(f) + PascalCase(fieldName)`
(`packages/universal/feature/src/plan.ts:31`) — a `FEATURE training` with `STATE mode : enum ..` generates a *new*
Kotlin enum class literally named `TrainingMode`, in a generated package, distinct from
`polski.presentation.TrainingMode`. Every DSL-typed field needs an explicit bridge function both ways; there is no
"bind to an existing enum" construct in FEATURE STATE.

## 3. Option A — the `.feature` file as a reviewable spec (not compiled)

Scoped to only the STATE-expressible, unconditional subset (§2's corrections applied); `frozenAnswer`, `evaluation`,
`sessionId` stay out of STATE entirely (native-only, §5); `error` is deliberately **not** in `cardReset` (see the
parity note below `continueDaily`).

`dsl/feature/training.feature` (**NEW**, spec-only while the Closeable blocker stands):

```
# Spec for TrainingStore's "start a fresh card/session" resets (startChain, startSchedule, chooseSkill,
# startDaily, continueDaily, trainExamMistake, resetDecision). NOT wired into dsl/biome.project: the Kotlin
# elm backend's generated types live in a Closeable-tied package (ktView) that cannot enter kotlin/shared's
# commonMain (js/wasmJs targets). See Plans/DSL-FEATURE-TRAINING-PLAN.md §headline finding before touching
# the GENERATE line below. chain/exercise/tileBuilder/introPending/predictionPending/daily/examRetryOrigin/
# sessionId/frozenAnswer/evaluation stay fully native (TrainingStore.kt) — not representable here (optional<T>
# is not a STATE type; sessionId's mint is impure).
FEATURE training USES elm
  STATE tab : enum training|vocabulary|matrix|progress = training
  STATE mode : enum chain|schedule|focused|daily = chain
  STATE phase : enum question|revealed|chainComplete|noDue|dailyComplete = question
  STATE draft : text = ""
  STATE sessionSource : enum manualPick|dailyButton|scheduleDue|chain|reset|examMistake = chain

  GROUP cardReset = phase, draft

  ACTION startChain -> RESET cardReset; tab = training; mode = chain; sessionSource = chain
  ACTION startSchedule -> RESET cardReset; tab = training; mode = schedule; sessionSource = scheduleDue
  ACTION chooseSkill -> RESET cardReset; tab = training; mode = focused; sessionSource = manualPick
  ACTION startDaily -> RESET cardReset; tab = training; mode = daily; sessionSource = dailyButton
  ACTION continueDaily -> RESET cardReset
  ACTION trainExamMistake -> RESET cardReset; tab = training; mode = focused; sessionSource = examMistake
  ACTION resetDecision -> RESET cardReset; tab = training; mode = chain; sessionSource = reset
  SHOW tab, mode, phase, draft, sessionSource
  EXAMPLES training.examples
END
```

Deviations from the owner's literal draft, each forced by a grounded fact above, not a stylistic choice:

- No action parameters: none of the five STATE fields depend on a caller argument (`chain: list<Exercise>`,
  `index: int` affect `AppUiState.chain`/`chainIndex`, not anything in this slice) — grammar makes the
  `ACTION id (…)` param optional, so it is simply omitted.
- `GROUP sessionReset` is dropped. The owner's draft paired `sessionId, sessionSource` under one group reset to a
  shared default, but real `sessionSource` is a **distinct literal per action**, never a shared default — a
  `RESET` whose default is always immediately overridden by the next clause is a no-op dressed as a group
  (`feature-group.sources` row 22 confirms the grammar allows "RESET group + explicit assign" to coexist, but
  nothing requires it). Each action assigns `sessionSource` directly instead.
- `error` is **not** in `cardReset`, though the owner's draft put it there. `continueDaily` is the one action that
  uses `RESET cardReset` without the other five's full assignment list, and real `continueDaily` (`TrainingStore.kt`
  line 477-484) never touches `error` — folding it into the shared group would silently start clearing a pending
  error message on "ещё одно задание", a real behavior change. The six "start a new session" actions still clear
  `error` — natively, right next to the other native-only fields (§5) — so behavior is unchanged either way this
  gets built.
- `frozenAnswer`/`evaluation` are dropped from `cardReset` (optional<T> unsupported, §2); they get the exact same
  always-null native treatment as `error`.

`dsl/feature/training.examples` (**NEW**, same spec-only status) — one example per action, mirroring
`dsl/feature/appearance-settings.examples`'s shape:

```
EXAMPLE start_chain_enters_training_tab_in_chain_mode
  DO startChain
  EXPECT view tab=training; mode=chain; phase=question; draft=""; sessionSource=chain

EXAMPLE continue_daily_only_resets_the_card
  DO startDaily
  DO continueDaily
  EXPECT view tab=training; mode=daily; phase=question; draft=""; sessionSource=dailyButton
```

### What generation would produce, if the blocker is lifted

`ktElmFiles` (`packages/languages/kt/src/patterns/elm.ts:80-83`) would print `Training.State.kt`, `Training.Msg.kt`,
`Training.Update.kt`, `Training.Host.kt`; the enums/`TrainingIntent` live in the companion `View.kt`
(`ktView`, shared by the pattern). The one call site matters: `fun trainingUpdate(s: TrainingState, msg: TrainingMsg,
d: TrainingDecisions): TrainingUpdate` returning `TrainingUpdate(state, effects)` — a pure function, no PORT declared
here so `effects` is always empty and `TrainingDecisions` is an empty interface (no DECISION in this FEATURE).
`Training.Host.kt` would own its *own* `AppUiState`-shaped state (`private var state = TrainingStateInitial.value`)
and must never be instantiated — `TrainingStore` already owns the one `MutableStateFlow<AppUiState>`; a second state
owner is not a dead-code risk, it is a correctness bug (two sources of truth). The CLI line, for the record (not run
until the blocker is resolved):

```
GENERATE training-feature feature generate --feature {dir}/feature/training.feature --lang kt --kt-package polski.generated.training --out {dir}/../kotlin/shared/src/commonMain/generated/kotlin --tests-out {dir}/../kotlin/shared/src/commonTest/generated/kotlin --flat
```

(This `--out` is the one from `dsl/biome.project:146`'s `appearance-settings-feature` line, pointed at `shared`
instead of `composeApp`'s `jvmGenerated` — and it is exactly this redirection into a real `commonMain` that the
Closeable import breaks for `js`/`wasmJs`.)

## 4. Mapping table (generated ↔ real), for whenever Option A becomes buildable

| Generated (package `polski.generated.training`) | Real (`polski.presentation`) | Bridge needed |
|---|---|---|
| `TrainingTab` (enum, 4 tags) | `AppTab` (enum, 4 tags, same order) | `fun TrainingTab.toAppTab(): AppTab`, reverse `fun AppTab.toTrainingTab()` |
| `TrainingMode` (enum, 4 tags) — **name collision** with `polski.presentation.TrainingMode` | `TrainingMode` | import-alias one side (`import polski.generated.training.TrainingMode as GenTrainingMode`) + `toTrainingMode()`/`toGenTrainingMode()` |
| `TrainingPhase` (enum, 5 tags) | `CardPhase` | `toCardPhase()`/`toTrainingPhase()` |
| `TrainingSessionSource` (enum, 6 tags) | `SessionSource` (nullable in real state) | `toSessionSource(): SessionSource` (total); real→generated direction needs a placeholder for `null` (never actually reached, since by the time any of these 7 actions run, `sessionSource` has already been minted once by `install()`) |
| `TrainingState.draft: String` | `AppUiState.draft: String` | identity |

Each bridge is ~4-6 lines (`when` over 4-6 tags); ~20-30 lines of hand-written glue total, committed next to
`TrainingStore.kt`, never generated (P7: the mapping is a fact of this integration, one place).

## 5. Option B — native helper (buildable today, same declarative shape)

Two small private extension functions on `AppUiState` in `TrainingStore.kt`, replacing the repeated tails of all
seven `.copy()` blocks. `sessionId`/`frozenAnswer`/`evaluation`/`error`/`exercise`/`tileBuilder`/`chain`/
`chainIndex`/`seedIndex`/`focusedSkillId`/`showSkillPicker`/`introPending`/`predictionPending`/`daily`/
`examRetryOrigin` stay exactly as native as they are today (§7) — only the fields in §3's `cardReset` plus the
per-action literals (`tab`, `mode`, `sessionSource`) move into the helper:

```kotlin
/** Declarative "enter a fresh session" reset — the cardReset/tab/mode/sessionSource slice of
 *  dsl/feature/training.feature (Plans/DSL-FEATURE-TRAINING-PLAN.md). Kept as hand-written Kotlin: the
 *  generated Kotlin FEATURE/elm backend needs java.io.Closeable, unavailable on this module's js/wasmJs
 *  targets (kotlin/shared/build.gradle.kts). Every other field the 6 call sites still set natively. */
private fun AppUiState.enterSession(mode: TrainingMode, sessionSource: SessionSource): AppUiState = copy(
    tab = AppTab.Training, mode = mode, phase = CardPhase.Question, draft = "",
    sessionId = sessionIds.newId(), sessionSource = sessionSource,
)

/** continueDaily's narrower slice: same cardReset fields, no tab/mode/session change (already correct). */
private fun AppUiState.resetCard(): AppUiState = copy(phase = CardPhase.Question, draft = "")
```

Call-site shape (`startChain`, today lines 408-416):

```kotlin
mutableState.value = state.value.enterSession(TrainingMode.Chain, SessionSource.Chain).copy(
    seedIndex = index, chain = chain, chainIndex = 0, focusedSkillId = null, showSkillPicker = false,
    exercise = chain.first(), frozenAnswer = null, evaluation = null,
    tileBuilder = deriveTileBuilder(chain.first()),
    introPending = needsIntroduction(chain.first(), document),
    predictionPending = predictionPendingFor(chain.first(), document), error = null, daily = null,
    examRetryOrigin = null,
)
```

`continueDaily` (today lines 477-484):

```kotlin
mutableState.value = project(current.resetCard().copy(
    exercise = exercise, tileBuilder = tileBuilder,
    answerMode = dailyAnswerMode(step, tileBuilder, current.answerMode),
    introPending = needsIntroduction(exercise, document),
    predictionPending = predictionPendingFor(exercise, document),
    daily = daily.copy(extended = true, currentStep = step),
), document ?: return, captured)
```

(`startSchedule`/`startDaily` still override `phase = CardPhase.NoDue` after `enterSession`/`resetCard` when the
native `exercise` comes back `null` — that branch is native-conditional either way, §2.)

This removes the duplicated `tab = AppTab.Training, mode = …, phase = CardPhase.Question, draft = "",
sessionId = sessionIds.newId(), sessionSource = …` line-group from all six call sites (`resetDecision` similarly),
and the `phase = …, draft = ""` pair from `continueDaily` — no `.feature` file required, zero generation step, no
bridge code, works identically on every target today.

## 6. Decisions and tradeoffs

Per this project's own `biome-dsl` principle P25 (`PRINCIPLES.md` §D: "V1–V5 benefit vs. S1–S6 stop-signal", and
`ARCHITECTURE.md` §4's own measured STATE probe — 133+29 DSL lines vs. 32 native, "no volume win"):

| Criterion | Option A (DSL/elm) | Option B (native helper) |
|---|---|---|
| Builds on every `kotlin/shared` target today | **No** — `Closeable` blocks `js`/`wasmJs` (headline finding) | Yes |
| Consumers | 1 (`TrainingStore`) — stop-signal S4 | 1 |
| Extra surface | generated enums + bridge functions (~20-30 lines) + a GENERATE line + an `.examples` file | 2 private functions (~10 lines) |
| Multi-language payoff | none — Kotlin is the only target reading `AppUiState` (same point `DSL-FEATURE-MIGRATION.md` already made for 4 other UI candidates) | n/a |
| Declarative (no if/else) | yes, in spec form | yes — still one `.copy()` call per concern, no branching added |
| Parity risk | bridge code is new, untested surface for a 1-consumer win | zero — same `.copy()` fields, same order of assignment |

**Recommendation: build Option B now.** Keep `dsl/feature/training.feature`/`training.examples` from §3 as the
committed, reviewable spec of the reset shape (not run through `biome generate`, not wired into `dsl/biome.project`)
— useful as the single place reviewers check the two helpers against when either changes. Revisit Option A only if
biome-dsl's `lang-kt` elm/mvp backend stops coupling `State.kt`/`Update.kt`'s types to the `Closeable`-based
`View.kt`/`Host.kt` (a `packages/languages/kt` change, not something to route around in this repo) — flag that back
to the owner as a biome-dsl toolchain question, separate from this task.

## 7. What stays native either way

- `exercise`, `chain`, `chainIndex`, `seedIndex`, `focusedSkillId`, `showSkillPicker`, `tileBuilder`, `daily`,
  `examRetryOrigin`, `introPending`, `predictionPending`, `answerMode` — all derived from `exerciseEngine`,
  `document`, `skillQueue`, `DailySessionPlanner`, or caller arguments; none are literal per-action constants.
- `sessionId` — impure mint (`sessionIds.newId()`), can never be a STATE default (§2).
- `frozenAnswer`, `evaluation`, `error` — nullable; no STATE type for them (§2); always-`null` on these 7 actions,
  asserted natively right next to the helper call.
- `addFocus(...)`, `refreshTime()`, `queueSave(...)`/`Write` channel sends — side effects, already outside any
  `.copy()` block.

## 8. Acceptance cases

1. **Spec file reviewed, not generated.** `dsl/feature/training.feature`/`training.examples` exist, are not listed
   in `dsl/biome.project`'s `GENERATE` lines, and their header states why (the Closeable blocker) — a reviewer
   reading them must not assume they compile.
2. **`enterSession`/`resetCard` extracted.** `commonTest` (or a focused `TrainingStoreTest` if one exists) asserts,
   per action: `tab`, `mode`, `phase`, `draft`, `sessionSource`, and that `sessionId` changed from the prior value —
   one case per of the 7 actions, mirroring `training.examples`' `EXPECT view ...` rows by hand.
3. **No behavior change.** Existing `TrainingStore`/`TrainingStoreTest` suites (whatever currently covers
   `startChain`/`startSchedule`/`chooseSkill`/`startDaily`/`continueDaily`/`trainExamMistake`/`resetDecision`) pass
   unmodified — this is a refactor, not a behavior change; `continueDaily` in particular must still leave `error`
   untouched (§3's deviation).
4. **Still builds everywhere.** `kotlin/shared` compiles for `desktop`, `android`, `iosArm64`/`iosSimulatorArm64`,
   `js`, `wasmJs` (the exact set this plan exists to protect).
5. **If Option A is ever revisited**: confirm the `lang-kt` change first (own biome-dsl PR/plan), then re-derive
   §3's `.feature` against whatever new, Closeable-free file split it produces — do not assume this plan's generated
   file list still matches.

## Documentation and handoff

- Implementation (Option B) is a small, independent Kotlin change: `kotlin/shared/.../TrainingStore.kt` only, no
  new module, no Gradle change, no `dsl/biome.project` edit. A developer agent can take §5 directly.
- The `.feature`/`.examples` pair from §3 needs no further tooling to be useful as committed documentation; do not
  add a `GENERATE` line for it without first resolving the headline finding.
- Open question for the owner, not resolved by this plan: whether to raise the `Closeable`-coupling in biome-dsl's
  `lang-kt` (worth doing once a second Kotlin FEATURE consumer needs commonMain, not just this one) — flagged, not
  decided here.
