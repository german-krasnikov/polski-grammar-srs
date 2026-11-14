# DSL migration: decision logic in TrainingStore.rate(), DailySessionPlanner, ReviewReducer, TileGenerator

Scope: move the classification/threshold/verdict fragments of these four files onto `biome-dsl` Logic
`DECISION`s, per `/Users/german/Work/Python/biome-dsl/AI/PRINCIPLES.md` P4/P10/P12/P25 and
`ProjectMigratioPrincipals.md`. Everything that *executes* (mutation, collection traversal, FSRS
scheduling, time stamping, model ownership) stays native. This plan follows the exact pattern already
in `dsl/exam/*` (`DECISION … DEFAULT … AT n WHEN … -> value END` + `.contract` + optional disposition-keyed
outcome `.core-data` table + `GENERATE logic generate`/`GENERATE data` lines in `dsl/biome.project`),
not a new convention.

Toolchain: `node <biome-dsl checkout>/implementations/typescript/packages/biome/src/bin.mjs generate
--project dsl/biome.project` (and `--check`), run from `/Users/german/Work/JS/polski-grammar-srs`.

## Candidates found vs. rejected

| Fragment | File:lines | Verdict |
|---|---|---|
| `phase` ladder | `TrainingStore.kt:703-710` | **Migrate** — classification ladder, 4 facts → `CardPhase` |
| retry-ticket enqueue (`correctness==false && mode!=Chain`) | `TrainingStore.kt:663-665` | **Keep native** — 2-fact AND inline in the same expression that builds the mutated list; extracting it removes no copy and adds no guarantee (no S1–S6 win, P8) |
| `isRetry`, `clearedExamRetryOrigin` | `TrainingStore.kt:643,688-689` | **Keep native** — single equality checks, below P22/P8 threshold |
| `reviewCandidate`/`newCandidate`/`practiceCandidate` queries | `DailySession.kt:63-80` | **Keep native** — domain *selection* over a collection (`minByOrNull`, `all{}`), not a verdict; this project's Logic dialect has no accepted `EACH .. WHERE`/aggregate consumer yet (ARCHITECTURE.md Δ5/K-1: experimental, 0 consumers) — S6 |
| kind-priority/backfill ladder | `DailySession.kt:82-93` | **Migrate** — exactly the "лестница вердиктов → DECISION" case BestPractice.md §7 calls out |
| `nextTileState` promotion/demotion ladder | `ReviewReducer.kt:73-82` | **Migrate** — threshold ladder over a pilot (SB-8) placeholder the KDoc already says is meant to be tuned |
| `nextIndependentSince` | `ReviewReducer.kt:88-92` | **Migrate, folded into the same outcome row** — its 3-way branch is a static function of the ladder's own disposition (see below), not a second DECISION |
| scaffolding reveal ratio | `TileGenerator.kt:53-56` | **Migrate** — same reasoning as `nextTileState`: a "~30%" comment is a tunable product ratio, not a formula that should require a Kotlin redeploy to change |
| `distractorsFor` sampling, `buildSegments` slicing | `TileGenerator.kt:62-116` | **Keep native** — mechanical traversal/RNG sampling of real morphology forms, not a decision (P4: utility, not verdict) |

Each "migrate" row gets its own `.logic`/`.contract` below. All four land under `dsl/learning/` (domain
folder, mirrors `dsl/exam/`); each targets the Gradle module that owns its native consumer.

---

## 1. `cardPhase` — replaces `TrainingStore.kt:703-710`

### Contract

Facts (all booleans/enum already computed natively before the decision — no new native computation
needed beyond what `rate()` already has in scope):

```
dsl/learning/card-phase.contract
CONTRACT learning.cardPhase
COVERAGE declared-scope
MODULE ../../kotlin/shared/src/commonMain/kotlin/polski/presentation/TrainingStore.kt
FACT phase.mode enum=chain,focused,schedule,daily
FACT phase.nextExerciseIsNull bool
FACT phase.dailyStepIsNull bool
FACT phase.dailyHistoryAtPlanned bool
FACT phase.dailyExtended bool
INPUT phase phase.mode phase.nextExerciseIsNull phase.dailyStepIsNull phase.dailyHistoryAtPlanned phase.dailyExtended
```

### `.logic` (same shape as `dsl/exam/practice-feedback-policy.logic` — disposition consumed directly, no outcome table)

```
dsl/learning/card-phase.logic
# One source for the phase ladder in TrainingStore.rate(): first match wins, same order as the
# existing `when` (chain-complete, schedule-no-due, daily-complete, daily-no-due, else question).
DECISION phase DEFAULT question
  chainComplete AT 10 WHEN phase.mode == chain AND phase.nextExerciseIsNull -> chain_complete
  scheduleNoDue AT 20 WHEN phase.mode == schedule AND phase.nextExerciseIsNull -> no_due
  dailyComplete AT 30 WHEN phase.mode == daily AND phase.nextExerciseIsNull AND phase.dailyStepIsNull AND phase.dailyHistoryAtPlanned AND NOT phase.dailyExtended -> daily_complete
  dailyNoDue AT 40 WHEN phase.mode == daily AND phase.nextExerciseIsNull -> no_due
END
```

### Generated target

```
GENERATE learning-card-phase logic generate --source {dir}/learning/card-phase.logic --contract {dir}/learning/card-phase.contract --target "kt {dir}/../kotlin/shared/src/commonMain/generated/kotlin/polski/presentation/policy/CardPhasePolicy.kt AS polski.presentation.policy"
GENERATE learning-card-phase-examples logic examples --source {dir}/learning/card-phase.examples --logic {dir}/learning/card-phase.logic --contract {dir}/learning/card-phase.contract --out {dir}/learning/generated/card-phase-examples.json
```

Produces `object Policy { fun phase(input: IInputs): String; ... }` in package `polski.presentation.policy`
(same shape as `polski.exam.policy.Policy.disposition`). `TrainingStore.kt:703` becomes:

```kotlin
val phase = when (Policy.phase(CardPhaseInput(
    mode = current.mode.name.lowercase(), nextExerciseIsNull = nextExercise == null,
    dailyStepIsNull = nextDaily?.currentStep == null,
    dailyHistoryAtPlanned = (nextDaily?.history?.size ?: 0) >= (nextDaily?.planned ?: 0),
    dailyExtended = nextDaily?.extended == true,
))) {
    "chain_complete" -> CardPhase.ChainComplete
    "no_due" -> CardPhase.NoDue
    "daily_complete" -> CardPhase.DailyComplete
    else -> CardPhase.Question
}
```

### AC (`dsl/learning/card-phase.examples`, one row per original branch + 2 negative controls)

1. `mode=chain, nextExerciseIsNull=true` → `chain_complete`.
2. `mode=chain, nextExerciseIsNull=false` → `question` (negative control: chain never reports `no_due`).
3. `mode=schedule, nextExerciseIsNull=true` → `no_due`.
4. `mode=daily, nextExerciseIsNull=true, dailyStepIsNull=true, dailyHistoryAtPlanned=true, dailyExtended=false` → `daily_complete`.
5. Same as 4 but `dailyExtended=true` → `no_due` (extended session never reports complete).
6. Same as 4 but `dailyHistoryAtPlanned=false` → `no_due`.
7. `mode=focused, nextExerciseIsNull=true` → `question` (Focused regenerates synchronously; `nextExercise` is never actually null for it today — negative control that the ladder doesn't touch Focused).

---

## 2. `dailyStepKind` — replaces the backfill loop at `DailySession.kt:82-93`

The query functions (`reviewCandidate`/`newCandidate`/`practiceCandidate`) stay native and get called
*unconditionally* (they're pure, cheap, in-memory lookups over the skills list — no behavior change,
small eagerness change only). The DECISION only picks *which kind wins*, given per-kind cap/candidate facts.

### Contract

```
dsl/learning/daily-step.contract
CONTRACT learning.dailyStepKind
COVERAGE declared-scope
MODULE ../../kotlin/shared/src/commonMain/kotlin/polski/presentation/DailySession.kt
FACT step.reviewCapped bool
FACT step.reviewHasCandidate bool
FACT step.newCapped bool
FACT step.newHasCandidate bool
FACT step.practiceCapped bool
FACT step.practiceHasCandidate bool
INPUT kind step.reviewCapped step.reviewHasCandidate step.newCapped step.newHasCandidate step.practiceCapped step.practiceHasCandidate
```

### `.logic`

```
dsl/learning/daily-step.logic
# DailySessionPlanner.nextStep's priority/backfill ladder: Review > New > Practice, each skipped when
# capped (done >= mix cap) OR it has no real candidate this step — in which case the NEXT kind gets a
# chance (`Plans/Learning/Plan.md` §M3 backfill). `none` is the fail-soft "nothing to do" case.
DECISION kind DEFAULT none
  review AT 10 WHEN NOT step.reviewCapped AND step.reviewHasCandidate -> review
  new AT 20 WHEN NOT step.newCapped AND step.newHasCandidate -> new
  practice AT 30 WHEN NOT step.practiceCapped AND step.practiceHasCandidate -> practice
END
```

### Generated target

```
GENERATE learning-daily-step logic generate --source {dir}/learning/daily-step.logic --contract {dir}/learning/daily-step.contract --target "kt {dir}/../kotlin/shared/src/commonMain/generated/kotlin/polski/presentation/policy/DailyStepPolicy.kt AS polski.presentation.policy"
GENERATE learning-daily-step-examples logic examples --source {dir}/learning/daily-step.examples --logic {dir}/learning/daily-step.logic --contract {dir}/learning/daily-step.contract --out {dir}/learning/generated/daily-step-examples.json
```

`DailySessionPlanner.nextStep` becomes:

```kotlin
val review = reviewCandidate(); val new = newCandidate(); val practice = practiceCandidate()
val caps = mapOf(...) // unchanged
return when (Policy.kind(DailyStepKindInput(
    reviewCapped = (done[Review] ?: 0) >= caps[Review]!!, reviewHasCandidate = review != null,
    newCapped = (done[New] ?: 0) >= caps[New]!!, newHasCandidate = new != null,
    practiceCapped = (done[Practice] ?: 0) >= caps[Practice]!!, practiceHasCandidate = practice != null,
))) {
    "review" -> review; "new" -> new; "practice" -> practice; else -> null
}
```

### AC (`dsl/learning/daily-step.examples`)

1. All three not capped, all three have candidates → `review` (priority order wins).
2. Review capped, New/Practice open+have candidates → `new`.
3. Review open but `reviewHasCandidate=false` (no due reviews), New capped, Practice open+has candidate → `practice` (confirms backfill skips a capped-AND-empty kind the same as an open-but-empty one — this is the exact case the current loop's `continue`-on-cap vs. fall-through-on-null distinction covers).
4. Review open+has candidate, New open+has candidate → `review` still wins (priority, not "most available").
5. Everything capped or empty → `none`.

---

## 3. `tileRungOutcome` — replaces `ReviewReducer.kt:73-92` (`nextTileState` + `nextIndependentSince`)

`nextStreak = if (tileCorrect) streak + 1 else 0` stays native (plain arithmetic, not a decision — P10
"вычисление без слоя выше"). Everything downstream of `rung`/`nextStreak`/thresholds is the ladder.
`nextIndependentSince`'s 3-way branch (`null` / keep / stamp `localDay`) is **not** a second DECISION:
checking all 8 leaf branches of the original ladder shows `independentSinceAction` is a pure function of
which disposition fired (clear whenever the new rung isn't `none`; `stampFresh` only on the
`partial→none` promotion; `keep` only on the "already hardest" default) — so it is one more column on
the same outcome row, the same shape as `dsl/exam/answer-policy.logic` + `answer-policy-outcomes.core-data`.

### Contract

```
dsl/learning/tile-scaffolding.contract
CONTRACT learning.tileScaffolding
COVERAGE declared-scope
MODULE ../../kotlin/srs-progress/src/commonMain/kotlin/polski/progress/ReviewReducer.kt
FACT tile.correct bool
FACT tile.rung enum=full,partial,none
FACT tile.nextStreak number min=0 max=2147483647 integer
FACT tile.toPartial number min=1 max=2147483647 integer
FACT tile.toNone number min=1 max=2147483647 integer
INPUT disposition tile.correct tile.rung tile.nextStreak tile.toPartial tile.toNone
```

### `.logic`

```
dsl/learning/tile-scaffolding.logic
# SB-8 pilot ladder (ReviewReducer.nextTileState): wrong answers demote only from `none`; correct
# streaks promote toward `none` once they reach the tunable thresholds. `nextStreak` is already
# `streak+1` on correct / `0` on wrong — computed natively before this decision.
DECISION disposition DEFAULT alreadyHardest
  wrongAtNone AT 10 WHEN NOT tile.correct AND tile.rung == none -> wrong_at_none
  wrongAtFull AT 20 WHEN NOT tile.correct AND tile.rung == full -> wrong_at_full
  wrongAtPartial AT 30 WHEN NOT tile.correct AND tile.rung == partial -> wrong_at_partial
  promoteFullToPartial AT 40 WHEN tile.rung == full AND tile.nextStreak >= tile.toPartial -> promote_full_to_partial
  stayFull AT 50 WHEN tile.rung == full -> stay_full
  promotePartialToNone AT 60 WHEN tile.rung == partial AND tile.nextStreak >= tile.toNone -> promote_partial_to_none
  stayPartial AT 70 WHEN tile.rung == partial -> stay_partial
END
```

(`alreadyHardest` = `rung==none AND correct` — DEFAULT, matching the original `else` arm exactly.)

### Outcome table

```
dsl/learning/tile-scaffolding.data-contract
DSL 1
RECORD TileScaffoldingOutcomeRow KEY disposition COUNT 8
  disposition: string
  newRung: enum full|partial|none
  finalStreak: enum zero|next
  independentSinceAction: enum clear|keep|stamp_fresh
END
```

```
dsl/learning/tile-scaffolding-outcomes.core-data
DSL 1
TILE_SCAFFOLDING_OUTCOME { disposition: wrong_at_none newRung: partial finalStreak: zero independentSinceAction: clear }
TILE_SCAFFOLDING_OUTCOME { disposition: wrong_at_full newRung: full finalStreak: zero independentSinceAction: clear }
TILE_SCAFFOLDING_OUTCOME { disposition: wrong_at_partial newRung: partial finalStreak: zero independentSinceAction: clear }
TILE_SCAFFOLDING_OUTCOME { disposition: promote_full_to_partial newRung: partial finalStreak: zero independentSinceAction: clear }
TILE_SCAFFOLDING_OUTCOME { disposition: stay_full newRung: full finalStreak: next independentSinceAction: clear }
TILE_SCAFFOLDING_OUTCOME { disposition: promote_partial_to_none newRung: none finalStreak: zero independentSinceAction: stamp_fresh }
TILE_SCAFFOLDING_OUTCOME { disposition: stay_partial newRung: partial finalStreak: next independentSinceAction: clear }
TILE_SCAFFOLDING_OUTCOME { disposition: already_hardest newRung: none finalStreak: next independentSinceAction: keep }
```

### Generated target

```
CONTRACT tile-scaffolding-outcomes FROM tile-scaffolding.data-contract
GENERATE learning-tile-scaffolding logic generate --source {dir}/learning/tile-scaffolding.logic --contract {dir}/learning/tile-scaffolding.contract --target "kt {dir}/../kotlin/srs-progress/src/commonMain/generated/kotlin/polski/progress/policy/TileScaffoldingPolicy.kt AS polski.progress.policy"
GENERATE learning-tile-scaffolding-outcomes data --contract tile-scaffolding-outcomes --rows TileScaffoldingOutcomeRow {dir}/learning/tile-scaffolding-outcomes.core-data --target kt --kt-package polski.progress.policy.data --out {dir}/../kotlin/srs-progress/src/commonMain/generated/kotlin/polski/progress/policy/data/TileScaffoldingOutcomeRows.kt
GENERATE learning-tile-scaffolding-examples logic examples --source {dir}/learning/tile-scaffolding.examples --logic {dir}/learning/tile-scaffolding.logic --contract {dir}/learning/tile-scaffolding.contract --out {dir}/learning/generated/tile-scaffolding-examples.json
```

**Build prerequisite:** `kotlin/srs-progress/build.gradle.kts` only wires
`kotlin.srcDir("src/commonTest/generated/kotlin")` today (no `commonMain/generated`) — add
`kotlin.srcDir("src/commonMain/generated/kotlin")` next to it, same as `kotlin/shared/build.gradle.kts:196`.

`ReviewReducer.nextTileState`/`nextIndependentSince` collapse into one native function:

```kotlin
private fun nextRungState(rung: Int, streak: Int, tileCorrect: Boolean?, oldIndependentSince: String?, localDay: String): TileRungState {
    if (tileCorrect == null) return TileRungState(rung, streak, oldIndependentSince)
    val nextStreak = if (tileCorrect) streak + 1 else 0
    val disposition = Policy.disposition(TileScaffoldingInput(
        correct = tileCorrect, rung = rungName(rung), nextStreak = nextStreak,
        toPartial = tileThresholds.toPartial, toNone = tileThresholds.toNone,
    ))
    val outcome = TILE_SCAFFOLDING_OUTCOME_ROW_ROWS.single { it.disposition == disposition }
    val newRung = rungOf(outcome.newRung)
    val finalStreak = if (outcome.finalStreak == "zero") 0 else nextStreak
    val independentSince = when (outcome.independentSinceAction) {
        "clear" -> null; "keep" -> oldIndependentSince; else -> localDay
    }
    return TileRungState(newRung, finalStreak, independentSince)
}
```

`rungName`/`rungOf` are the only new native code: a 3-way `Int ↔ "full"/"partial"/"none"` mapping at the
DSL boundary (the `RUNG_FULL`/`PARTIAL`/`NONE` Int constants stay — unifying them with
`polski.core.model.ScaffoldingLevel` from candidate 4 is a separate, cross-module refactor, not part of
this migration; flagged for the developer, not forced here).

### AC (`dsl/learning/tile-scaffolding.examples`, mirrors the 8 outcome rows 1:1)

One case per disposition row above (8 cases: `correct/rung/nextStreak/toPartial/toNone →
expectedDisposition`), plus 2 boundary cases: `nextStreak == toPartial` exactly (promotes, not
`stayFull`) and `nextStreak == toPartial - 1` (stays) — the `>=` boundary the original `if (nextStreak >=
tileThresholds.toPartial)` encodes. Existing `ReviewReducerTest`/`TileScaffoldingTest` (if present) keep
their current input→output expectations; only the implementation under test changes.

---

## 4. `tileRevealRatio` — replaces `TileGenerator.kt:53-56`

The `(tileTargetSlots.size + 1) / 3` formula is **not** a generic `ceil(size/3)`; verifying
`(N+1)/3` against `ceil(N/3) = (N+2)/3` (integer division) diverges at `N=1` (`0` vs `1`). To preserve
exact behavior bit-for-bit, the outcome row carries the formula's own `(numerator, addend, divisor)`
rather than re-deriving a "generic" ratio: `count = (size*numerator + addend) / divisor`. For
`Full`: `(1,1,3)` reproduces `(size+1)/3` exactly. For `Partial`/`None`: `(1,0,1)` gives `size/1 = size`,
i.e. "take all" (`.toSet()` already was the full slot list for those two levels).

### Contract

```
dsl/learning/tile-reveal.contract
CONTRACT learning.tileReveal
COVERAGE declared-scope
MODULE ../../kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/TileGenerator.kt
FACT reveal.scaffolding enum=full,partial,none
INPUT disposition reveal.scaffolding
```

### `.logic`

```
dsl/learning/tile-reveal.logic
# TileGenerator.fromAnswerPhrase's scaffolding → reveal-ratio switch. Full gets the "~30% Choice, rest
# Locked" ramp; Partial/None reveal every DSL-eligible slot as Choice.
DECISION disposition DEFAULT open
  limited AT 10 WHEN reveal.scaffolding == full -> limited
END
```

### Outcome table

```
dsl/learning/tile-reveal.data-contract
DSL 1
RECORD TileRevealOutcomeRow KEY disposition COUNT 2
  disposition: string
  numerator: number integer min=0 max=1000
  addend: number integer min=0 max=1000
  divisor: number integer min=1 max=1000
END
```

```
dsl/learning/tile-reveal-outcomes.core-data
DSL 1
TILE_REVEAL_OUTCOME { disposition: limited numerator: 1 addend: 1 divisor: 3 }
TILE_REVEAL_OUTCOME { disposition: open numerator: 1 addend: 0 divisor: 1 }
```

### Generated target

```
CONTRACT tile-reveal-outcomes FROM tile-reveal.data-contract
GENERATE learning-tile-reveal logic generate --source {dir}/learning/tile-reveal.logic --contract {dir}/learning/tile-reveal.contract --target "kt {dir}/../kotlin/core-engine/src/commonMain/generated/kotlin/polski/core/engine/policy/TileRevealPolicy.kt AS polski.core.engine.policy"
GENERATE learning-tile-reveal-outcomes data --contract tile-reveal-outcomes --rows TileRevealOutcomeRow {dir}/learning/tile-reveal-outcomes.core-data --target kt --kt-package polski.core.engine.policy.data --out {dir}/../kotlin/core-engine/src/commonMain/generated/kotlin/polski/core/engine/policy/data/TileRevealOutcomeRows.kt
GENERATE learning-tile-reveal-examples logic examples --source {dir}/learning/tile-reveal.examples --logic {dir}/learning/tile-reveal.logic --contract {dir}/learning/tile-reveal.contract --out {dir}/learning/generated/tile-reveal-examples.json
```

**Build prerequisite:** `kotlin/core-engine/build.gradle.kts` has no `generated` source dir wired at all —
add `kotlin.srcDir("src/commonMain/generated/kotlin")` to its `commonMain` source set.

`TileGenerator.fromAnswerPhrase` becomes:

```kotlin
val disposition = Policy.disposition(TileRevealInput(scaffolding = scaffolding.name.lowercase()))
val ratio = TILE_REVEAL_OUTCOME_ROW_ROWS.single { it.disposition == disposition }
val activeChoices = tileTargetSlots.take((tileTargetSlots.size * ratio.numerator + ratio.addend) / ratio.divisor).toSet()
```

(`ScaffoldingLevel.Full/Partial/None` are the only 3 enum members today — `scaffolding.name.lowercase()`
is a safe, exhaustive mapping; a future 4th level fails loudly at the `.single{}` lookup, not silently.)

### AC (`dsl/learning/tile-reveal.examples`)

1. `scaffolding=full` → `limited`.
2. `scaffolding=partial` → `open`.
3. `scaffolding=none` → `open`.
4. Native-side regression table (unchanged test file, new implementation): `tileTargetSlots.size` ∈
   `{0,1,2,3,4,5,10}` × `Full` must reproduce `(size+1)/3` exactly (0,0,1,1,1,2,3) — add these as explicit
   cases in the existing `TileGeneratorTest` (or wherever scaffolding ratio is currently asserted) so the
   bit-for-bit claim above is actually checked, not just argued.

---

## Order

1. **Candidate 1 (`cardPhase`)** first — self-contained, no outcome table, smallest `.logic`, proves the
   `dsl/learning/` folder + `GENERATE` wiring end-to-end before the others reuse it.
2. **Candidate 4 (`tileRevealRatio`)** — second smallest, but touches `:core-engine`, which has *no*
   generated source dir yet; doing it second (not first) means the `kotlin.srcDir` build-config pattern
   is already proven on `:shared` from step 1 before extending it to a module that has none.
3. **Candidate 3 (`tileRungOutcome`)** — first outcome-row table in `dsl/learning/`; depends on step 2
   having already exercised "module has no `generated` dir" once (`:srs-progress` needs the same fix).
4. **Candidate 2 (`dailyStepKind`)** last — the only one that changes *eagerness* (candidates now always
   computed), so it benefits from the other three's generation/outcome-row plumbing being already
   familiar when reviewing its one real behavioral nuance (step 3 above, AC #3).

Each step: edit `.logic`/`.contract`/`.core-data` → `node <biome> generate --project dsl/biome.project`
→ `node <biome> generate --check --project dsl/biome.project` → Gradle module test (`:shared`,
`:core-engine`, `:srs-progress` respectively) → `gate changed` (biome-dsl side, if `.logic`/`.contract`
touched) before moving to the next candidate. `gate:all` once, after all four, per
`BestPractice.md` §8 "Порядок работы агентов".

## Documentation and handoff

- `AI/decisions.md` (this project): record the `dsl/learning/` domain folder and the
  disposition-keyed-outcome-row pattern reused from `dsl/exam/*`, so the next decision-logic candidate
  (e.g. `exerciseEngine.generateForSkill`'s weak-seed bias, M5) is evaluated against the same table above
  instead of re-deriving P10/P25 from scratch.
- Flagged, not actioned here: `polski.progress` rung `Int` constants vs. `polski.core.model.ScaffoldingLevel`
  enum are the same three-level domain concept in two representations (P7) — a native cross-module
  unification, independent of this DSL migration.
- Generated Kotlin is committed per P16; `biome generate --check` is the freshness gate, not a second
  hand-written mirror.
