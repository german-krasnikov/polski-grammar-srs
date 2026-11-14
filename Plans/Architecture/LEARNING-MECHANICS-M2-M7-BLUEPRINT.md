# Learning mechanics M2, M4–M7 — architecture blueprint

Base analysis: `Plans/Learning/Plan.md` §3 (mechanics table), §4 (M1–M3 MVP, already landed via SB-1..SB-8
and the M3 "Сегодня" session), §7 (file bindings), §8 (AC1–AC10). M8–M10 stay out of scope. This
blueprint covers M2 (retry), M4 (variability), M5 (readiness-based selection), M6 (skill progress),
M7 (prediction-before-explanation) as one package, ordered for minimal, incremental landing.

Status: **design only — no production code, DSL data or tests changed.** Pedagogical KPIs
(T7/E/R7/ER7) stay unmeasured until the P1 attempt journal exists; this package only builds the
product mechanics, same scoping the Plan itself applies to M4's "сontrol vs training set" KPI need
(flagged as an open dependency below, not fabricated here).

## Grounding already confirmed in code

- `kotlin/shared/src/commonMain/kotlin/polski/presentation/DailySession.kt` `DailySessionPlanner.nextStep`'s
  `newCandidate()` **already** gates a brand-new skill on `skill.prerequisites.all { reviewsOf(it) > 0 }`.
  M5's first bullet ("DailySessionPlanner использует prerequisites") is therefore already implemented for
  `TrainingMode.Daily`. This blueprint's M5 section covers only the real gap: within-skill weak-form
  prioritization.
- `kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/ExerciseGenerator.kt` already has one
  anti-repeat mechanism, but only for `recipe.staticPool` (`remainingStaticIndexes`/`lastStaticIndex`,
  lines 93–94, 234–245). The dynamic seed path (`pick(candidates)` at line 230) has no anti-repeat and
  no weighting — that is M4's and M5's real gap.
- `polski.progress.SkillStats` (`kotlin/srs-progress/src/commonMain/kotlin/polski/progress/Progress.kt`)
  already carries `tileScaffoldingRung`/`tileScaffoldingStreak` (SB-8). M6 reuses these; it needs exactly
  one new field (`independentSince`), not a parallel tracking system.
- `polski.presentation.AppUiState.introPending` + `TrainingCardModel`'s `introducing` branch
  (`CardSection.MethodIntro` → `ContinueIntroduction`) is the existing "first exposure" gate M7 extends.

## Shared building block: seed key (lands with M4, reused by M5 and M2)

One helper, defined once, used by three mechanics — avoids three ad hoc string formats.

- `kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/ExerciseGenerator.kt` — `NEW` top-level
  `fun seedKey(candidate: Map<String, String>): String = "${candidate.getValue("noun")}:${candidate.getValue("adjective")}"`.
  Used internally for anti-repeat (M4) and for the weak-form bias lookup (M5).
- `kotlin/shared/src/commonMain/kotlin/polski/model/Exercise.kt` — `NEW` top-level
  `fun seedKey(exercise: Exercise): String = "${exercise.nounId}:${exercise.adjectiveId}"`. Same format,
  separate function because `Exercise` (presentation-facing) and the generator's `Map<String,String>`
  candidate are different shapes — not a shared dependency, just a shared string convention documented in
  both KDocs so they never drift silently.

## M4: variability (land first — purely additive, no schema change)

**Goal:** don't hand back the same noun+adjective pair twice in a row for a skill; leave a documented
hook for a future training/control split.

### Files and changes

- `ExerciseGenerator.kt`:
  - `NEW` private `val lastSeedKey: MutableMap<String, String> = mutableMapOf()` (same shape as the
    existing `lastStaticIndex`, line 94 — one more per-skill "last thing shown" map, not a new pattern).
  - In `generateForSkill`, right after `val candidates = candidatesFor(...)` and before picking the seed:
    when `preferredSeed == null` and `candidates.size > 1`, filter out the candidate whose `seedKey(...)`
    equals `lastSeedKey[skillId]` before calling `pick(...)`. Falls back to the unfiltered list when that
    would leave zero candidates (a skill with exactly one lexical candidate must still generate).
  - After computing `seed`, set `lastSeedKey[skillId] = seedKey(mapOf("noun" to seed.getValue("noun"), "adjective" to seed.getValue("adjective")))`
    (or simpler: build the key straight from `seed`, since `seed` already has those two keys).
  - `preferredSeed` calls (`ChooseSkill` with an explicit seed) bypass the filter unchanged — a learner's
    explicit pick is never second-guessed.

### Public contract

No signature change on `generateForSkill` — the anti-repeat is an internal behavior change, observable
only as "two consecutive calls for the same `skillId` never share a seed when ≥2 candidates exist."
`ExerciseGenerator` is already a per-session object owned by one `TrainingStore`/host adapter
(`PlExerciseEngine`), so this mutable map has the same lifetime as `remainingStaticIndexes` — no new
ownership question.

### Acceptance

- `kotlin/core-engine/src/commonTest/kotlin/polski/core/engine/ExerciseGeneratorTest.kt`: for a skill with
  ≥2 lexical candidates, two consecutive `generateForSkill` calls with a `RandomSource` that would
  otherwise pick the same index twice (`FakeRandomSource`-style fixture already used in this file) must
  return different `(nounId, adjectiveId)`. A skill with exactly one candidate (e.g. `case.gen.neg`'s
  `lexicalFilter: null` against a filtered seed pool of size 1, if any) must not throw or stall.
- A `preferredSeed` call must still return that seed even if it equals `lastSeedKey[skillId]`.

### Open dependency (not solved by this blueprint)

The Plan's second M4 bullet — "контрольные примеры отдельно от тренировочных (для KPI T7)" — needs the
P1 attempt-journal/probe-set infrastructure (`Plans/Learning/Plan.md` §5, P1, "не начат"). Building a real
train/control split here without that journal would be a fabricated KPI seam. **Flagged as blocked on
P1, not implemented in this package.**

## M6: skill progress ("Мои достижения")

**Goal:** show "раньше с таблицей, теперь сам" with a date, from data that already exists almost
entirely — only the date is missing.

### Files and changes

- `kotlin/srs-progress/src/commonMain/kotlin/polski/progress/Progress.kt` — `SkillStats` gets one `NEW`
  field: `val independentSince: String? = null` (a `localDay` string, same format as `Progress.lastDay`).
  Default `null` keeps every pre-M6 stored document compatible (same reasoning already documented for
  `tileScaffoldingRung`).
- `kotlin/srs-progress/src/commonMain/kotlin/polski/progress/ReviewReducer.kt` — `recordReview`, right
  after computing `(nextRung, nextTileStreak)` (line 41): set
  `independentSince = when { nextRung != RUNG_NONE -> null; oldStats.tileScaffoldingRung == RUNG_NONE -> oldStats.independentSince; else -> localDay }`.
  Sticky only while *currently* at `RUNG_NONE` — a later demotion (the documented rollback-to-Partial path
  in `nextTileState`) clears it, so the UI never claims "independent since <date>" for a skill that is
  back to needing a partial scaffold. Re-reaching `RUNG_NONE` later stamps a fresh date, which is correct:
  it is a new confirmation, not a continuation of the old one.
- `kotlin/composeApp/src/webMain/kotlin/polski/ui/ProgressWeb.kt` — `NEW` section
  `renderAchievementsSection` (own `<section class="card matrix-section">`, inserted between the summary
  and the skills table), iterating `skills.filter { progress.stats[it.id]?.tileScaffoldingRung == 2 }`:
  for each, render title + "с {independentSince}" + "подтверждено {tileScaffoldingStreak} раз без
  таблицы". Skills not yet at rung 2 are simply absent from this section (no placeholder row) — matches
  the existing table's `"—"` convention is not needed here because the section itself is empty-tolerant.

### Public contract

`SkillStats` stays a plain data class; no new store method. `ReviewReducer.recordReview`'s signature is
unchanged (the new field is derived inside the existing `nextStats.copy(...)`, no new parameter needed
since `localDay` is already a parameter).

### Acceptance

- `kotlin/srs-progress/src/commonTest/kotlin/polski/progress/ReviewReducerTest.kt`: a sequence of correct
  `TileBuilder` reviews that promotes a skill to `RUNG_NONE` sets `independentSince` to that review's
  `localDay`; a further correct review on a later day leaves `independentSince` at the *original* date
  (sticky while independent); a wrong review that rolls `RUNG_NONE` back to `RUNG_PARTIAL` clears
  `independentSince` to `null`.
- Decoding a pre-M6 stored document (missing `independentSince`) must not fail — covered by the existing
  `ProgressCodec`/compat fixture pattern; add one skill-stats fixture without the field if the current
  suite does not already generate JSON missing newer optional fields.
- Playwright (web): with a seeded progress document where one skill is at rung 2, the achievements
  section shows that skill's title and a date string; a skill at rung 0/1 does not appear there.

## M7: prediction before explanation

**Goal:** an authored short question shown before a brand-new skill's own introduction text, only once
per skill (same moment `introPending` already gates).

### DSL and generated data

- `dsl/course.data-contract` `RECORD SkillRow`: add `predictionQuestion: string OPTIONAL` (next to the
  existing `caseReferenceAvailable: bool OPTIONAL` line). DSL-first per `AI/decisions.md` — the question
  is authored content, never a Kotlin literal.
- `courses-biome/pl-ru/skills.core-data`: add `predictionQuestion: "Почему Widzę dom, но Widzę psa?"`
  (one line per `SKILL` block) for the three pilot skills named in the Plan's §1 scope
  (`case.acc.f`, `case.acc.m`, `case.gen.neg`); leave it absent for every other skill (OPTIONAL → no
  question → M7 is simply inactive for that skill, never a placeholder question).
  - **Content-authoring note:** the PRM example question in this task's brief is for `case.acc.m`
    (animate/inanimate contrast). Author matching questions for `case.acc.f` and `case.gen.neg`
    separately — do not reuse one question for three different contrasts.
- Regenerate `kotlin/shared/src/commonMain/generated/kotlin/polski/data/rows/plru/CourseRows.kt`'s
  `SkillRow`/`SKILL_ROW_ROWS` via the project's existing `biome data`/generation step (see `AGENTS.md`
  for the exact invocation — this blueprint does not introduce a new generator, only a new optional
  field in an existing one).
- `kotlin/shared/src/commonMain/kotlin/polski/model/Grammar.kt` — `Skill` data class gets one `NEW`
  trailing field: `val predictionQuestion: String? = null` (trailing + defaulted, so every existing
  positional/named construction of `Skill` keeps compiling).
- `kotlin/shared/src/commonMain/kotlin/polski/data/PlRuSkillRows.kt` — `SkillRow.toSkill()` passes
  `row.predictionQuestion` through.

### Presentation state and flow

- `kotlin/shared/src/commonMain/kotlin/polski/presentation/AppUiState.kt` — `NEW` field
  `val predictionPending: Boolean = false`.
- `kotlin/shared/src/commonMain/kotlin/polski/presentation/TrainingStore.kt` — every site that currently
  computes `introPending = needsIntroduction(exercise, document)` (6 call sites: `install`, `startChain`,
  `startSchedule`, `startDaily`, `chooseSkill`, `continueDaily`/`rate`'s `nextExercise` assignment) gets a
  parallel `predictionPending = needsIntroduction(exercise, document) && exercise?.let { skillById(it.primarySkill).predictionQuestion != null } == true`.
  Pull this into one `NEW` private helper `predictionPendingFor(exercise, source)` mirroring
  `needsIntroduction`'s own shape, to avoid repeating the skill lookup six times.
- `kotlin/shared/src/commonMain/kotlin/polski/presentation/AppAction.kt` — `NEW`
  `data object ContinuePrediction : AppAction`.
- `TrainingStore.accepts()` — `NEW` branch: `AppAction.ContinuePrediction -> s.phase == CardPhase.Question && s.predictionPending`.
- `TrainingStore.dispatch()` — `NEW` branch: `AppAction.ContinuePrediction -> mutate { it.copy(predictionPending = false) }`
  (leaves `introPending` untouched — the existing `MethodIntro`/`ContinueIntroduction` phase runs next,
  unchanged).
- `kotlin/shared/src/commonMain/kotlin/polski/presentation/CardSection.kt` — `NEW` variant
  `data class PredictionQuestion(val text: String) : CardSection`.
- `kotlin/shared/src/commonMain/kotlin/polski/presentation/TrainingCardModel.kt` — `trainingCardModel`:
  when `state.predictionPending`, `front` is exactly
  `[CardSection.SourceSentence(sourceParts), CardSection.PredictionQuestion(skill.predictionQuestion!!), CardSection.Action("Что скажешь? Дальше", AppAction.ContinuePrediction)]`
  — no `MethodIntro`/retrieve/answer input yet. The existing `introducing` branch
  (`state.phase == Question && state.introPending`) only fires once `predictionPending` is false, i.e.
  prediction always precedes the existing introduction unchanged.

### Acceptance

- Pure `trainingCardModel` test (new case in the existing TrainingCardModel/TrainingStore test suite):
  a fresh skill with a `predictionQuestion` shows the prediction section first; `ContinuePrediction`
  moves to the existing `MethodIntro` phase; a skill without `predictionQuestion` skips straight to
  `MethodIntro` exactly as today (regression guard — M7 must not change behavior for the other 24+
  skills).
- `accepts()` rejects `ContinuePrediction` once `predictionPending` is already false (idempotent guard,
  same style as `ContinueIntroduction`'s existing guard).
- Playwright (web): opening `case.acc.m` for the first time on a fresh profile shows the authored
  question and a "Дальше" control before the existing introduction text; answering/continuing reaches
  the existing first-exposure flow unchanged.
- Measurement (E +0.5, "T7 защитный критерий") is explicitly **not** part of this AC — it needs the P1
  survey/journal infra and is a product-decision gate, not a code gate.

## M5: readiness-based selection — within-skill weak-form priority

(Cross-skill prerequisite gating is already implemented — see "Grounding" above. This section is the
real remaining gap.)

**Goal:** when generating a practice exercise for a skill the learner has already started, prefer a
lexical candidate (seed) the learner has gotten wrong more often, instead of uniform random.

### Files and changes

- `Progress.kt` — `SkillStats` gets one more `NEW` field: `val seedErrors: Map<String, Int> = emptyMap()`
  (key = the `seedKey` format from M4's shared helper, e.g. `"wife:beautiful"`; value = wrong-answer
  count for that seed on this skill). Default empty map — same compatibility shape as every other SB-8
  field.
- `ReviewReducer.recordReview` — `NEW` trailing optional parameter `seedKey: String? = null` (same
  "appended last, defaulted" pattern already used for `tileCorrect` — every existing positional call site
  keeps compiling). When `correct == false` and `seedKey != null`, bump
  `oldStats.seedErrors[seedKey]` by one in the copied map; otherwise pass the map through unchanged. This
  is a **count**, never reset to zero on a later correct answer — the point is "which forms have ever
  been hard," not a live streak.
- `TrainingStore.rate()` — pass `seedKey(exercise)` (the M4 presentation-side helper) as the new trailing
  argument to `reviewReducer.recordReview(...)`.
- `ExerciseGenerator.generateForSkill` — `NEW` trailing optional parameter
  `weakSeedCount: ((candidateSeedKey: String) -> Int)? = null`. When non-null, after building `candidates`
  (and after M4's last-seed exclusion): draw one extra `random.nextDouble()`; with that draw `< 0.5`,
  restrict the pick to `candidates.filter { weakSeedCount(seedKey(it)) > 0 }.let { if it.isEmpty() candidates else it }`
  weighted toward the maximum count (simplest correct version: pick the single highest-count candidate
  deterministically on that branch, breaking ties via the normal `pick(...)` draw over the tied subset);
  otherwise (`>= 0.5`, or no weak candidates at all) fall through to the existing uniform `pick(candidates)`.
  This keeps `RandomSource` to its existing single-draw-per-decision contract and stays testable with the
  same `FakeRandomSource` fixtures already in `ExerciseGeneratorTest.kt` — no new randomness abstraction.
- `TrainingStore` — `NEW` private helper
  `private fun weakSeedCountFor(skillId: String): (String) -> Int = { key -> document?.progress?.stats?.get(skillId)?.seedErrors?.get(key) ?: 0 }`,
  passed at every existing `exerciseEngine.generateForSkill(skillId, ...)` call site (there are six:
  `startSchedule`, `startDaily`, `continueDaily`, `chooseSkill`, `rate`'s `Focused`/`Schedule`/`Daily`
  branches, `refreshTime`'s backfill branch). One helper, reused everywhere — not six bespoke closures.

### Public contract

Both new parameters are trailing and defaulted, so this is additive: no existing caller (including other
hosts' adapters, if any construct `ExerciseGenerator`/`ReviewReducer` calls directly) needs to change to
keep compiling. Hosts that want weak-form prioritization opt in by passing the closure; hosts that don't
wire it simply get today's uniform behavior.

### Acceptance

- `ExerciseGeneratorTest.kt`: given a `weakSeedCount` function that marks one candidate's seed as the
  only one with errors, a `FakeRandomSource` drawing `< 0.5` must return that candidate; drawing `>= 0.5`
  must fall back to uniform selection (verifiable via the existing fixture pattern). No `weakSeedCount`
  (default `null`) must reproduce exactly today's behavior (regression guard, same assertions as the
  pre-M5 tests in this file).
- `ReviewReducerTest.kt`: a wrong review with a given `seedKey` increments exactly that key in
  `seedErrors`; a correct review never decrements or clears it; an absent `seedKey` (old call-site shape)
  leaves `seedErrors` untouched.
- `DailySessionPlannerTest.kt` is unaffected — `DailySessionPlanner` itself doesn't call
  `generateForSkill`; the bias is wired one layer up in `TrainingStore`, so the planner's own pure
  selection logic needs no change for M5.

## M2: retry after error

**Goal:** after an error on a skill, surface a new exercise on the same skill (different seed, via M4's
anti-repeat) after 2–3 other tasks — a genuine new FSRS exposure, not a duplicate of the original review,
and not routed through the diagnostic-free-form path SB-7 already owns for the *immediate* re-try inside
one exercise.

Lands last because it reuses M4's seed exclusion and M5's per-skill generation helper, and because it is
the one mechanic that changes the shared "what's next" decision inside `rate()` across every mode.

### Files and changes

- `kotlin/shared/src/commonMain/kotlin/polski/presentation/AppUiState.kt`:
  - `NEW` data class `RetryTicket(val skillId: String, val remaining: Int)` (session-only, same lifetime
    class as `DailySessionState` — never persisted, reset per app load).
  - `AppUiState` `NEW` field `val retryQueue: List<RetryTicket> = emptyList()`.
  - `NEW` field `val activeRetrySkillId: String? = null` — set exactly when the *current* `exercise` was
    served because a ticket matured, cleared on the next `rate()`. Drives the "Ещё раз: {skill}" badge.
- `kotlin/shared/src/commonMain/kotlin/polski/presentation/TrainingStore.kt` — `rate()`:
  - After `reviewReducer.recordReview(...)` succeeds and *before* computing `nextExercise`: if
    `correctness == false` (the same `correctness` value already computed for the `Typed`/`TileBuilder`
    branches — `Oral` has no correctness signal and is excluded from M2, matching the Plan's "надёжно
    распознанная ошибка" framing) **and** `current.mode != TrainingMode.Chain` (chain content is scripted,
    not a candidate for substitution), push `RetryTicket(exercise.primarySkill, remaining = RETRY_DELAY)`
    onto `retryQueue` (constant `RETRY_DELAY = 2`, a `NEW` top-level `const val` in `TrainingStore.kt`,
    matching the Plan's "2-3" with the lower, more conservative bound — tune during the pilot exactly like
    `TilePromotionThresholds` already is).
  - Decrement every other ticket's `remaining` by one on every `rate()` call (any mode, any outcome) —
    "через N заданий" counts elapsed tasks, not elapsed errors.
  - After the existing per-mode `nextExercise` computation (the `when (current.mode) { ... }` block),
    `NEW` override: if `current.mode != TrainingMode.Chain` and any ticket in the (decremented) queue has
    `remaining <= 0`, take the **oldest** such ticket (`minByOrNull` over queue order — first error served
    first), replace `nextExercise` with
    `exerciseEngine.generateForSkill(ticket.skillId, weakSeedCount = weakSeedCountFor(ticket.skillId))`
    (M4's anti-repeat and M5's weak-form bias both apply for free, since this is the same
    `generateForSkill` entry point), remove that ticket from the queue, and set
    `activeRetrySkillId = ticket.skillId`. Otherwise `activeRetrySkillId = null`.
  - This override intentionally **replaces** whatever the mode's own logic chose (a due Review, a new
    Practice step, etc.) for that one step — the retry is a scheduling interrupt, not an extra step
    inserted mid-queue. The displaced candidate simply gets picked again on the following step by the
    mode's own unchanged logic (nothing is lost, only delayed by one step).
  - The substitute review still goes through `reviewReducer.recordReview` exactly like any other step the
    *next* time the learner rates it — this satisfies "не дублировать FSRS review": nothing here creates
    a second review of the *original* card/instant; it is one ordinary new review of a new exercise
    instance, same as every other step. What M2 must avoid (and already does, by construction) is calling
    `recordReview` twice for one `AppAction.Rate` dispatch.
  - `TrainingMode.Daily` special case: `advanceDaily`'s own `DailyStepOutcome` bookkeeping
    (`DailyStepKind`) has no "Retry" kind. Tag the substituted step as `DailyStepKind.Practice` in the
    history (closest existing semantic — a retry *is* practice of an already-introduced skill) rather than
    adding a fourth enum value; `classifyDailyOutcome` needs no change since it only reads `AnswerMode`/
    `rating`, not step kind.
- `kotlin/shared/src/commonMain/kotlin/polski/presentation/TrainingCardModel.kt` — `TrainingCardModel`
  `NEW` field `val retryBadge: String?`, computed as
  `state.activeRetrySkillId?.let { "Ещё раз: ${skillById(it).title}" }`.
- Web UI (`kotlin/composeApp/src/webMain/kotlin/polski/ui/` training screen — the file rendering
  `TrainingCardModel`, not read in this pass but implied by the existing `lifehackBadge`/`dailyProgress`
  rendering pattern): render `retryBadge` the same way `lifehackBadge` is rendered today — a small label
  above the prompt, visible only while non-null.

### Public contract

`retryQueue`/`activeRetrySkillId` are pure presentation state, never serialized (`ProgressDocument`
untouched) — closing the tab loses pending retries, which is correct: a retry is a within-session nudge,
not a scheduling guarantee.

### Acceptance

- `TrainingStore`-level test (new cases alongside existing rate()/mode tests): rating a `Typed`/
  `TileBuilder` exercise incorrect enqueues a ticket; the next `RETRY_DELAY - 1` ratings do not surface
  that skill out of turn; the `RETRY_DELAY`-th rating after the error substitutes that skill's exercise
  for whatever the mode would otherwise have shown, and `activeRetrySkillId`/`retryBadge` reflect it; the
  rating after that clears `activeRetrySkillId` back to `null`.
- The substituted exercise's seed must differ from the original error's seed when the skill has ≥2
  lexical candidates (reuses M4's AC via the shared `generateForSkill` path — no separate anti-repeat
  logic to test here).
- `TrainingMode.Chain` is never interrupted by a ticket (regression guard — chain content stays exactly
  as scripted).
- `Oral` mode errors never enqueue a ticket (no correctness signal to trust — matches AC3's existing
  "не выдумывать диагноз" principle).
- Playwright (web): force a `Typed` error on a pilot skill, complete `RETRY_DELAY` more unrelated steps,
  confirm the "Ещё раз: …" badge appears on the substituted card and the seed differs from the original.

## Implementation order

| Order | Mechanic | Why this position |
| --- | --- | --- |
| 1 | M4 | Purely additive to `ExerciseGenerator`; no schema change; produces the `seedKey` convention M5/M2 reuse. |
| 2 | M6 | Fully isolated (one field + one `ReviewReducer` branch + one Web section); zero interaction with the others. |
| 3 | M7 | Isolated in a different direction (DSL + new card phase); needs authored content, so can proceed in parallel with M6 once the DSL field lands. |
| 4 | M5 | Needs M4's `seedKey` and extends the same `generateForSkill` call; adds the `seedErrors` schema field `ReviewReducer` also touches for M2's substitution path. |
| 5 | M2 | Needs M4 (anti-repeat) and M5's `weakSeedCountFor` helper already wired at every call site; touches the shared `rate()` advance logic last, once the lower-risk pieces are proven. |

Each still fits "one feature × one host, sequential, early feedback": land shared-module changes with
`commonTest` coverage first, then the one web-visible piece (ProgressWeb section / card badge / prediction
card) with Playwright, before moving to the next mechanic. Android/iOS pick up the same shared-module
behavior automatically; their own host screens (Progress list, card badges) are separate follow-up tasks,
not blocking this package per "web-first."

## Documentation and handoff

- `AI/decisions.md`: record the `seedKey` format convention and the M2 "replace, don't insert" scheduling
  rule once implemented — both are the kind of cross-cutting decision future mechanics (M8–M10) will need
  to know about.
- `Plans/Learning/Plan.md` §7 (file bindings table) and §10 (document status) should gain a row/line per
  mechanic once landed, same as the table already tracks M1–M3.
- Remaining real open questions, not resolved by this blueprint:
  1. M4's control/probe-set split depends on the P1 attempt journal (not started) — flagged, not solved.
  2. M7's authored questions for `case.acc.f`/`case.gen.neg` need actual content, not just the `case.acc.m`
     example already given — a content-authoring task, not a code task.
  3. M2's `RETRY_DELAY = 2` and M5's `< 0.5` explore/exploit split are both placeholder constants in the
     same spirit as the existing `TilePromotionThresholds` — tune during the pilot, do not treat as final.
