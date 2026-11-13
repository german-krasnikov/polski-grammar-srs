# M3 "Сегодня" — architecture blueprint

Дата: 2026-11-13. Источник требований: `Plans/Learning/Plan.md` §M3, §7, §8 (AC8).
Статус: **план предложен; код не написан, не проверен**.

## Scope and consumers

Четвёртый режим входа в тренировку, рядом с "Цепочка предложений" (`TrainingMode.Chain`),
"По расписанию" (`TrainingMode.Schedule`) и "Отдельный навык" (`TrainingMode.Focused`). Логика
живёт в `:shared` commonMain (`polski.presentation`) — тот же модуль, что уже хранит
`TrainingStore`/`AppUiState`/`AppAction`/`TrainingCardModel`, так что Android/iOS хосты получают
"Сегодня" бесплатно через тот же `TrainingStore`, когда у них появится свой рендер; эта задача
реализует только web (`composeApp/webMain`). Не затрагивает `:core-engine`, `:core-model`,
DSL-данные курса, `ReviewReducer`, `Scheduler`/FSRS или формат `ProgressDocument` — M3 только
выбирает, какой уже существующий `Exercise` показать следующим, и упаковывает это в сессию с
ясным объёмом и итогом.

Не ломает Chain/Schedule/Focused: их код в `TrainingStore`/`TrainingCardDom` не удаляется, только
получает по одной новой ветке `TrainingMode.Daily` там, где уже есть `when (current.mode)`.

## Public contract

### Shared (`:shared` commonMain, host-agnostic)

**NEW** `kotlin/shared/src/commonMain/kotlin/polski/presentation/DailySession.kt`:

```kotlin
enum class DailyStepKind { Review, New, Practice }
data class DailyStep(val kind: DailyStepKind, val skillId: String)

enum class DailyOutcome { Independent, Assisted, Errored }
data class DailyStepOutcome(val kind: DailyStepKind, val skillId: String, val outcome: DailyOutcome)

/** Soft per-kind budget for one planned session; `planned` is the total shown before the
 *  voluntary-continue prompt. Caps are advisory — [DailySessionPlanner] backfills from another
 *  kind when one runs out, so a learner with no due reviews still gets a full session. */
data class DailyMix(val reviews: Int = 4, val new: Int = 1, val practice: Int = 1) {
    val planned: Int get() = reviews + new + practice
}

/** Session-only — never part of [polski.progress.ProgressDocument]; reset on every
 *  [AppAction.StartDaily], same lifetime class as [AppUiState.chain]/[AppUiState.chainIndex]. */
data class DailySessionState(
    val mix: DailyMix = DailyMix(),
    val currentStep: DailyStep? = null,
    val history: List<DailyStepOutcome> = emptyList(),
    val extended: Boolean = false,
) {
    val planned: Int get() = mix.planned
}

/**
 * Pure selection, called by [TrainingStore] only. Priority 1 a due review of an *already
 * reviewed* skill (`reviews > 0`&nbsp;— a brand-new skill's freshly scheduled card is due too,
 * but that is [DailyStepKind.New]'s job, not a review); priority 2 a not-yet-reviewed skill whose
 * [polski.model.Skill.prerequisites] are all already reviewed; priority 3 the lowest-accuracy
 * already-reviewed skill, meant for [AnswerMode.TileBuilder] practice. Each capped by [mix] for
 * one planned session; once a cap is hit OR a kind has nothing left, the remaining slot backfills
 * from the next kind in the same order. Returns null only when every kind is empty — the fail-soft
 * "nothing to do" case ([Plans/Learning/Plan.md] §M3 "если нет ничего — сообщить").
 */
object DailySessionPlanner {
    fun nextStep(
        progress: polski.progress.Progress,
        scheduler: polski.srs.Scheduler,
        skillQueue: polski.progress.SkillQueue,
        skills: List<polski.model.Skill>,
        at: kotlin.time.Instant,
        completed: List<DailyStepKind>,
        mix: DailyMix = DailyMix(),
    ): DailyStep?
}

/** Pure, used by [TrainingStore.rate] right before advancing — never persisted, never changes
 *  FSRS: every Daily rating still calls [polski.progress.ReviewReducer.recordReview] exactly like
 *  Schedule/Focused today, so a Daily review is a real exposure, not a second bookkeeping path
 *  ([Plans/Learning/Plan.md] §M3 "дополнительная практика не маскируется под тест памяти"). */
fun classifyDailyOutcome(current: AppUiState, rating: polski.srs.Rating): DailyOutcome
```

`AppUiState.kt` (**existing file, edited**):
- `enum class TrainingMode { Chain, Schedule, Focused, Daily }` — add `Daily`.
- `enum class CardPhase { Question, Revealed, ChainComplete, NoDue, DailyComplete }` — add
  `DailyComplete`, the "planned volume reached, summary + voluntary continue" phase. The
  fail-soft "nothing at all available" case reuses the existing `CardPhase.NoDue` (same shape as
  Schedule's own empty state today); `TrainingCardDom` only swaps its copy for `mode == Daily`.
- `AppUiState` gains `val daily: DailySessionState? = null` — `null` outside `TrainingMode.Daily`,
  same documented-fallback pattern as `tileBuilder` (SB-6 §1.1).

`AppAction.kt` (**existing file, edited**):
```kotlin
data object StartDaily : AppAction
data object ContinueDaily : AppAction
```

`TrainingStore.kt` (**existing file, edited**) — new private functions, same shape as the existing
`startSchedule()`/`chooseSkill()`:
- `startDaily()`: captures time, asks `DailySessionPlanner.nextStep(document.progress, scheduler, skillQueue, skills, at, completed = emptyList())`, generates the `Exercise` for that step via the existing `exerciseEngine.generateForSkill(skillId)`, sets `mode = Daily`, `phase = if (exercise == null) NoDue else Question`, `daily = DailySessionState(currentStep = step)`. Mirrors `startSchedule()`'s null-handling 1:1.
- `continueDaily()`: only reachable from `CardPhase.DailyComplete` (guarded in `accepts`); asks the planner for one more step from `daily.history.map { it.kind }`, sets `daily = daily.copy(extended = true, currentStep = step)`, phase back to `Question` (or `NoDue` if the planner is now genuinely empty).
- `advanceDaily(current, rating, newDocument, at): Pair<Exercise?, DailySessionState>` — called from inside `rate()`'s existing `when (current.mode)` block (new `TrainingMode.Daily` branch), after `reviewReducer.recordReview(...)` has already run for every mode exactly as today. Appends one `DailyStepOutcome` (built from `classifyDailyOutcome(current, action.rating)` and `current.daily.currentStep`) to `history`; if `history.size >= daily.planned && !daily.extended`, returns `null to daily.copy(currentStep = null)` (→ `CardPhase.DailyComplete`); otherwise asks the planner for the next step and returns its generated `Exercise` alongside the updated `DailySessionState`.
- `dailyAnswerMode(step, tileBuilder, fallback)`: if `step?.kind == Practice && tileBuilder != null` returns `AnswerMode.TileBuilder`, else `fallback` (the learner's own last-chosen mode, untouched for Review/New steps — M1's "осознанный пользовательский выбор" is about *rating*, not this scaffolding choice, so overriding the answer mode only for the one practice step is a deliberate, narrow exception, not a precedent for overriding user choice elsewhere).
- `startChain()`, `startSchedule()`, `chooseSkill()`, `resetDecision()` each gain `daily = null` in their `.copy(...)` — hygiene, not required for correctness (every read of `state.daily` is already guarded by `state.mode == Daily`), but keeps a stale session from surviving a mode switch by accident.
- `accepts()`: add `AppAction.StartDaily` to the existing `s.loadStatus == LoadStatus.Ready` line (same bucket as `StartSchedule`); add `AppAction.ContinueDaily -> s.loadStatus == Ready && s.mode == TrainingMode.Daily && s.phase == CardPhase.DailyComplete`.

`TrainingCardModel.kt` (**existing file, edited**) — pure, host-agnostic view model, used by both the web renderer and (later) Android/iOS:
```kotlin
data class DailyProgress(val done: Int, val planned: Int, val extended: Boolean)
```
- `modeLabel` gains `TrainingMode.Daily to "Сегодня · ~5 мин"`.
- `header.modes` gains a 4th `ModeOption(TrainingMode.Daily, modeLabel.getValue(TrainingMode.Daily), AppAction.StartDaily)`.
- `TrainingCardModel` gains `val dailyProgress: DailyProgress?`, computed as
  `if (state.mode == TrainingMode.Daily) state.daily?.let { DailyProgress(it.history.size, it.planned, it.extended) } else null`.
- `fun dailySummaryText(history: List<DailyStepOutcome>): String` — pure, composes the
  "Готово! Повторил 3 навыка, освоил 1 новый" copy from counts per `DailyStepKind`; empty history
  → a neutral fallback string (only reachable if `StartDaily` itself hit `NoDue`, not
  `DailyComplete`, so practically unreachable, but keeps the function total).

### Web (`composeApp/webMain`, this task's scope)

`TrainingCardDom.kt` (**existing file, edited**):
- `renderTraining()`'s `when (state.phase)` gains `CardPhase.DailyComplete -> renderDailyComplete(card, state, dispatch)`.
- **NEW** `renderDailyComplete(card, state, dispatch)`: heading, `dailySummaryText(state.daily.history)`,
  two buttons — "Ещё одно задание" (`dispatch(AppAction.ContinueDaily)`) and "К повторениям по
  расписанию" (`dispatch(AppAction.StartSchedule)`, same escape hatch `renderChainComplete` already
  offers).
- **NEW** `renderDailyProgress(main, state)`, called from `renderTraining()` right where
  `renderChainHeader` is called today (`if (state.mode == TrainingMode.Daily) renderDailyProgress(...)`):
  a `role="progressbar"` element with `aria-valuenow/min/max` and a visible "`done` / `planned`"
  label, "· продолжение" appended when `extended`.
- `renderNoDue()`: branch its copy on `state.mode == TrainingMode.Daily` ("Сегодня нечего
  тренировать" instead of the Schedule-specific "Повторения на сейчас завершены"); same button.
- `renderCard()`'s existing `card-meta` `when (state.mode)` gains a `Daily` branch. Per
  [Plan.md §M3] ("дополнительная практика не маскируется под тест памяти"), label the current
  step honestly by its real kind rather than a generic "Сегодня":
  `Review`/`New` → `"Сегодня · шаг ${done+1} / ${planned}"`; `Practice` → `"Короткая проверка
  применения"`.

`TrainingWebApp.kt`: **no change required.** `dispatch` already forwards every action it does not
special-case (`SelectTab`/`SetStyle`/`SetAnswerMode`) straight to `store.dispatch`, so
`StartDaily`/`ContinueDaily` reach `TrainingStore` with zero new wiring; the keyboard handler
already gates on `CardPhase.Question`/`Revealed`, so `DailyComplete` safely falls through to no-op
exactly like `ChainComplete`/`NoDue` do today.

CSS (`training.css` or wherever `.study-toolbar`/`.chain-header` live today): a `.daily-progress`
rule for the new progress element — implementation detail, not specified here.

## Components and dependencies

```
polski.data.skills (existing)  polski.progress.Progress/SkillQueue (existing)  polski.srs.Scheduler (existing)
                    \                      |                                    /
                     v                     v                                   v
                      DailySessionPlanner.nextStep()   (NEW, polski.presentation, pure)
                                      |
                                      v
                          TrainingStore.startDaily / advanceDaily / continueDaily  (edited)
                                      |
                                      v
                      AppUiState.daily : DailySessionState?   (edited)
                                      |
                 -----------------------------------------------
                 |                                             |
                 v                                             v
      TrainingCardModel.dailyProgress/dailySummaryText   TrainingCardDom.renderDailyProgress/
      (edited, pure, host-agnostic)                       renderDailyComplete/renderCard (edited, web-only)
```

Dependency direction stays the same as the existing three modes: `DailySessionPlanner` depends
only on already-public read-only contracts (`Progress`, `Scheduler`, `SkillQueue`, `Skill`), never
on Compose or DOM; `TrainingStore` is the only caller; the web renderer only ever reads
`AppUiState`/`TrainingCardModel`, never the planner directly — identical layering to how
`renderCard`/`trainingCardModel` already consume `TrainingStore`'s Schedule/Focused output.

## State and resources

- `DailySessionState` is session-only, owned by the single `TrainingStore` instance exactly like
  `chain`/`chainIndex`/`draft` already are (see the class doc comment at the top of
  `TrainingStore.kt`): a page reload starts a fresh Daily session, nothing new to persist, no
  `ProgressDocument`/schema change, no migration risk.
- FSRS state (`SrsCard`/`SkillStats`) is mutated through the exact same `ReviewReducer.recordReview`
  call `rate()` already makes for every mode — Daily adds no second write path and no double-count
  risk (AC4 in `Plan.md` §8 already guards this at the `rate()` level, unchanged).
- `DailySessionPlanner.nextStep` is a pure function with no owned resources; every input is
  borrowed (read-only `List`/`Map` already immutable per `Progress`'s own contract).

## Decisions and tradeoffs

- **Where does the mix/priority logic live?** In `:shared` presentation, next to `TrainingStore`,
  not in `:core-engine`. It decides *which skillId* to practice next from `Progress`/`Scheduler`
  state, not *how* to generate an exercise for a skill (that stays `PlExerciseEngine`'s job,
  called identically for every mode) — a session-composition concern, not course-DSL content, so
  it does not belong in the DSL-first course data either.
- **"Due review" vs "new skill" for a never-touched skill whose freshly scheduled card is already
  due.** `ProgressCodec.fresh`/`completeKnownSkills` give every known skill a `StoredCard` with
  `due` at creation time, so on a brand-new document *every* skill is technically "due" per
  `SkillQueue.due()`. Treating that literally would make Priority 1 swallow Priority 2 entirely
  for a first-time user. `DailySessionPlanner` therefore defines "due review" as "due **and**
  `SkillStats.reviews > 0`" — a `reviews == 0` card, however due, is only ever offered through the
  New path (prerequisite-gated). This is the one place this blueprint diverges from a literal
  reading of "due reviews (из FSRS schedule)" in `Plan.md` §M3, and is called out here because it
  is a real design choice, not an oversight: without it, a fresh user's "Сегодня" could present a
  never-seen skill as a routine review with no introduction screen.
- **Soft caps + backfill vs hard per-kind slots.** `Plan.md` §M3 says "точное распределение —
  параметр пилота" — the mix is explicitly not contractual. Hard slots would mean a learner with
  zero due reviews gets a 2-item session instead of 6; backfilling in priority order (Review → New
  → Practice) keeps the planned volume honest ("ограниченный объём") without inventing empty
  categories. The tradeoff is that a genuinely fresh user's first session is New-heavy (see
  Verification below) — acceptable for MVP, revisit once `Plan.md` P3 has real usage data.
  `DailySessionPlanner.nextStep` takes `mix: DailyMix = DailyMix()` as a parameter specifically so
  this can be tuned (or made a Settings-level pilot parameter later) without touching call sites.
- **Forcing `AnswerMode.TileBuilder` for the Practice step only.** Reuses the already-existing,
  already-tested `tileBuilderStateFor`/`deriveTileBuilder` fail-soft (`null` when an exercise opts
  out — SB-6 §1.1): if the chosen weak skill's exercise has no tile content, `tileBuilder` stays
  `null` and the card quietly falls back to the learner's own mode, never a broken tile UI. No new
  fail-soft path to write.
- **Not introducing a `DailySessionPlanner`-level dependency on `AppUiState`.** The planner only
  ever sees `Progress`/`Scheduler`/`SkillQueue`/`Skill`/`Instant` — the same pattern
  `SkillQueueTest.kt` already tests against — so it is independently testable in `commonTest`
  without constructing a `TrainingStore` or Compose runtime, and reusable verbatim by a future
  Android/iOS renderer.

## Verification

- **NEW** `kotlin/shared/src/commonTest/kotlin/polski/presentation/DailySessionPlannerTest.kt` —
  pure unit tests against fake `Progress`/`Scheduler`/`SkillQueue` fixtures (same style as
  `kotlin/srs-progress/src/commonTest/kotlin/polski/progress/SkillQueueTest.kt`):
  - no reviews at all → first call returns a `New` step for a skill with empty `prerequisites`.
  - a skill with `reviews > 0` and a due card → `Review`, never conflated with `New`.
  - `reviews == 0` but `prerequisites` not yet all reviewed → neither `Review` nor `New`; falls
    through to `Practice` or `null` depending on what else exists.
  - `reviews.size` at each cap boundary (reviews=4, new=1, practice=1) — backfill kicks in exactly
    when a kind both hits its cap and the next kind has nothing, never earlier.
  - every input empty (no cards, no skills reviewed, no skills with satisfied prerequisites) →
    `null` (the fail-soft "сообщить" case), not a thrown error.
- Extend `kotlin/composeApp/src/commonTest/kotlin/polski/presentation/TrainingStoreTest.kt`
  (`FakeRepository`/`FastScheduler`/`newStore` already defined there) with `TrainingMode.Daily`
  cases:
  - `StartDaily` → `Rate` six times (planned default) → `CardPhase.DailyComplete`, `daily.history.size == 6`.
  - `ContinueDaily` from `DailyComplete` → back to `Question`, `daily.extended == true`,
    `daily.history` unchanged until the next `Rate`.
  - Each `Rate` call still produces exactly one `ReviewReducer` write (reuse the existing
    `totalReviews`-counting assertion pattern already used for Chain in this file) — guards AC4
    (no double-count) for the new mode specifically.
  - Switching `StartChain`/`StartSchedule`/`ChooseSkill` away from an in-progress Daily session
    clears `state.daily` to `null`.
- Playwright, **NEW** `tests/browser/kotlin-training-daily.spec.ts` (same fixtures/helpers as
  `tests/browser/kotlin-training.spec.ts`, including `installProgressReader`):
  1. On a fresh profile, the "Сегодня · ~5 мин" button is visible next to the three existing mode
     buttons; clicking it switches to Daily, shows the progress element at "0 / 6".
  2. Revealing and rating six cards (mixing `Rating.Again`/`Good` the way the existing chain spec
     already does) reaches a heading + summary text + both "Ещё одно задание" and "К повторениям
     по расписанию" buttons; no `pageerror` (reuse this file's existing `errors`/`afterEach` guard).
  3. Clicking "Ещё одно задание" returns to a question card with the progress label past
     `planned` (e.g. "7 / 6"), confirming voluntary continuation without resetting the summary data.
  4. On a fresh profile specifically (no due reviews possible yet per the Priority-1 redefinition
     above), the first Daily card shows the introduction screen (`"Перейти к заданию"`), confirming
     the fail-soft "no due reviews → new skill" path is reachable, not just theoretically defined.
  5. Regression: existing `kotlin-training.spec.ts`/`kotlin-training-tile.spec.ts` keep passing
     unchanged — the mode-button ordering change (Daily added to `header.modes`) must not break
     any locator there; grep both specs for assumptions about button position/count before landing
     (they currently locate by accessible name, which should be unaffected, but this is an actual
     thing to check, not assumed).
- Does **not** re-verify K1–K4 (T7/E/R7/ER7) — those are pedagogical KPIs per `Plan.md` §1/§6,
  "P3/P4 — наблюдение за обучением", out of scope for this architecture and for AC1–AC10.

## Documentation and handoff

- `Plans/Learning/Plan.md` §7 ("Привязка к текущему коду") already lists `TrainingStore.kt`/
  `AppUiState.kt` as the files M3 will touch — this blueprint is the concrete version of that row;
  no change to `Plan.md` itself is needed unless the developer deviates from the Priority-1
  redefinition or the soft-cap mix above, in which case update `AI/decisions.md` per `AGENTS.md`.
  Attempt-event logging (`Plan.md` §7's "Минимальные поля будущего события") is **not** built by
  this blueprint — `DailyStepOutcome` is an in-memory session summary only, not the P1 attempt
  journal; that remains a separate, not-yet-started piece of work.
  - **Open question for the developer, not resolved here:** exact default `DailyMix` values
    (4/1/1 is this blueprint's starting proposal, matching "~5-8 для 5 минут" loosely) and the
    exact Russian copy for `dailySummaryText`/button labels are implementation choices within this
    contract, not load-bearing for the architecture — pick something reasonable, consistent with
    existing `UiCopy.kt` phrasing, and record the final choice in the PR/commit, not here.
- Owner of implementation: a developer agent following this blueprint; owner of the Playwright AC:
  the same developer agent or a tester agent, per `AGENTS.md`'s role table — this document does
  not assign that split, it only names the real file paths both need.
