# P1 attempt journal + P2 formal acceptance — architecture blueprint

Base analysis: `Plans/Learning/Plan.md` §P1/§7 (minimal event fields), §8 (AC1–AC10), §4 (M1–M3
MVP). M1–M3 are already landed (SB-1..SB-8, M3 "Сегодня"); M2/M4–M6/M7 landed per
`Plans/Architecture/LEARNING-MECHANICS-M2-M7-BLUEPRINT.md`. This blueprint covers only P1 (the
missing measurement infrastructure) and states exactly which P2 ACs P1 unblocks — P3–P6 are
observation phases, not code, and stay out of scope.

Status: **design only — no production code, DSL data or tests changed.**

## Grounding already confirmed in code

- `Progress.kt`/`ProgressCodec.kt`/`ReviewReducer.kt` (`kotlin/srs-progress/.../polski/progress/`)
  hold **aggregated** `SkillStats` only — no per-attempt history. `ProgressDocument` carries a
  `source: JsonObject` passthrough for forward/legacy compat (`Progress.kt:44-47`); `ProgressCodec`
  has a strict JSON decoder (`decode`) and a text-wire codec (`encodeText`/`decodeText`, POLSKI-TEXT,
  schema `"progress"` v2). `RawProgressRepository` (`ProgressRepository.kt:55`) owns one
  `RawKeyValueStore` key family (`PREVIEW_KEY`/`LEGACY_KEY`/`BACKUP_KEY`/`MARKER_KEY`) and a
  load/save/migrate/reset contract.
- `TrainingStore.rate()` (`kotlin/shared/.../polski/presentation/TrainingStore.kt:503-586`) is the
  **single** call site where `reviewReducer.recordReview(...)` runs, gated by
  `result is ReviewResult.Reviewed` (line 518). This is already the exact 1:1 boundary AC4 needs
  ("one rating → one review"); nothing elsewhere calls `recordReview`.
- `rate()` already computes, as local values, everything the minimal event needs except journal-
  specific signals: `exercise` (has `id`, `primarySkill`, `nounId`/`adjectiveId` →
  `polski.model.seedKey(exercise)`, `Exercise.kt:45`), `current.answerMode`, `correctness`,
  `action.rating`, `captured.at`/`captured.localDay`, `current.mode` (`TrainingMode`).
- Self-direction signal exists piecemeal, not as one contract: `AppUiState.showReference`
  (user-toggled, persists across the card until Reveal — `AppUiState.kt:60-...`, toggled only by
  `ToggleReference`, `TrainingStore.kt:204`); `SkillStats.tileScaffoldingRung` (Full/Partial/None,
  `Progress.kt:21-27`) drives how much the TileBuilder UI pre-fills; `DailySession.kt`'s
  `classifyDailyOutcome` (`DailySession.kt:105-113`) already classifies one review as
  Independent/Assisted/Errored, but only from rating+correctness, **not** from whether a reference
  was visible — and only for `TrainingMode.Daily`. AC2 needs the on-screen-visibility signal
  generalized to every mode.
- `ExerciseGenerator` (`kotlin/core-engine/.../ExerciseGenerator.kt`) already has a stable per-
  candidate key (`seedKey(candidate: Map<String,String>)`, line 70) and uses it for M4 anti-repeat
  (`lastSeedKey`) and M5 weak-form bias (`pickSeed`, lines 254-262). `seeds: List<Map<String,String>>`
  (constructor param, line 90) is the **entire** pool `candidatesFor` draws from — there is no
  partition today between "training" and "held-out" seeds. This is exactly the gap behind AC6.
- `polski.training.evaluate` (`AnswerEvaluator.kt:8-17`) is one deterministic normalize+match
  function with no version marker — correct by construction today, but nothing records *which*
  matching rule judged a given attempt if the rule changes later.
- `AppUiState`/`AppAction`/`DailySession.kt` already have the session-shape primitives P1 needs to
  extend: `TrainingMode` (`Chain/Schedule/Focused/Daily`), `DailySessionState` (session-only, never
  persisted — same lifetime class proposed below for journal session scoping), `RetryTicket`
  (`skillId`, `remaining` — needs one more field for correction linkage, see P1.5).

## Scope decision: where the journal lives

Three options were on the table (see task context). Chosen: **(2) a separate document, new files,
same `:srs-progress` Gradle module.**

- Rejected (1) extend `ProgressDocument`: `Progress` is a small, frequently round-tripped aggregate
  (loaded/saved on every review, migrated, exported). An attempt is a different write pattern
  (append-only, unbounded without pruning, never re-read for scheduling) and a different compat
  surface (new fields here must never risk `ProgressCodec`'s legacy-JSON/text-wire round-trip for
  the FSRS cards). Mixing them raises the 200-line-file risk and makes one document's bug blast
  radius cover both scheduling and measurement.
- Rejected (3) in-memory + export only: fails AC7 directly ("неявка не теряется из отчёта") and
  R7/E/K-series KPIs, which need data to survive across days without the learner manually
  exporting. A reload (new tab, new session) would silently lose every attempt since the last
  export.
- Chosen (2): one more Gradle-module-internal domain object, `AttemptLog`, with its own codec and
  repository, **not** a new Gradle module — it shares `:srs-progress`'s actual reason to exist
  (durable learning state written through `RawKeyValueStore`) and its test infrastructure
  (`ProgressCodecTest.kt`-style fixtures), so a new module would only add build-graph ceremony with
  no isolation benefit (Cohesion / KISS, `module-architecture` skill). It gets its own storage key,
  its own schema version, and is read/written independently of `ProgressDocument` — a corrupt or
  missing attempt log must never block scheduling (`ReviewReducer`/`TrainingStore.rate()` keep
  working even if the journal write fails; see "Failure isolation" below).

## P1.1 — `AttemptLog` domain model and codec (`NEW`, `:srs-progress`)

`NEW` file `kotlin/srs-progress/src/commonMain/kotlin/polski/progress/AttemptLog.kt`:

```kotlin
package polski.progress

/** One graded or self-assessed learner action. [attemptId] reuses the already-unique
 *  Exercise.id (ExerciseIdFactory) — no second ID minting needed. */
data class AttemptEvent(
    val schemaVersion: Int = 1,
    val attemptId: String,              // == Exercise.id
    val sessionId: String,              // minted per session, see P1.5
    val skillId: String,
    val seedKey: String,                // polski.model.seedKey(exercise) / core-engine's twin
    val kind: AttemptKind,              // Practice | Probe | Retry
    val at: String,                     // ISO instant, same format as SrsCardWire's due/last_review
    val localDay: String,               // same validDay() format as Progress.lastDay
    val activeMillis: Long,             // see P1.6; 0 until wired
    val answerMode: String,             // AnswerMode.name — plain string, no cross-module enum dep
    val helpShown: Boolean,             // see P1.2
    val disclosed: Boolean,             // true if expected/accepted text was ever shown before this attempt's own grading (SB-7 "show comparison" path)
    val correct: Boolean?,              // first-attempt correctness; null when answerMode has no signal (Oral)
    val correctnessBasis: CorrectnessBasis,
    val selfRating: String,             // srs.Rating.name
    val correctionOf: String? = null,   // attemptId of the original error this attempt corrects (M2 retry)
    val sessionKind: String,            // TrainingMode.name, or "Probe"
    val sessionSource: String,          // see P1.5
    val experimentVariant: String? = null, // reserved for P3+; no assignment logic lands with P1
    val rubricVersion: Int,             // see P1.4
    val contentVersion: String? = null, // reserved; course/content version is not yet stamped anywhere (open question, see Handoff)
    val scheduledNextDue: String? = null, // SrsCardWire-encoded due after this review, for audit
)

enum class AttemptKind { Practice, Probe, Retry }
enum class CorrectnessBasis { Evaluated, SelfReported, Ambiguous }

data class AttemptLog(val version: Int = 1, val events: List<AttemptEvent>)
data class AttemptLogDocument(val log: AttemptLog, val source: JsonObject = JsonObject(emptyMap()))
```

- Append-only by construction: the only mutation `TrainingStore` performs is `events + newEvent`
  (never edits or removes an existing entry) — this is what makes AC1 ("первая попытка записывается
  один раз; исправление не заменяет ошибку") true by construction rather than by a rule someone has
  to remember. A correction is a **new** event with `correctionOf` set, never an edit.
- `NEW` file `AttemptLogCodec.kt`, same shape as `ProgressCodec`: strict `decode(raw: String):
  DecodeResult` / `encodeText`/`decodeText` using the existing `PolskiTextCodec` wire format with
  kind `"attempts"` schema version 1 (parallel to `"progress"`), so the same text-wire
  infrastructure (`PolskiTextCodec.encode/decode`, `WireNode`) is reused rather than duplicated.
  Unknown fields round-trip via `source` exactly like `ProgressDocument` (AC5: "неизвестные
  сохранённые поля продолжают поддерживаться").

### Retention — bounded growth without losing the measurement window

An unbounded list in `localStorage` (or platform-equivalent) is the real risk of option (2). Policy:
**age-based pruning on every save**, not count-based — drop events older than
`RETENTION_DAYS` (`NEW` placeholder constant, default 37: covers D30±2 plus a safety margin, same
"tune during the pilot" spirit as `RETRY_DELAY`/`TilePromotionThresholds`). Age-based (not count-
based) is required because a count cap could silently drop exactly the D7/D30 probe attempts a
heavy practice day pushes out — the one case AC7 says must never happen silently. Pruning runs
inside `AttemptLogRepository.append`, not in `TrainingStore`, so it is one tested, central place.

### Repository (`NEW`)

```kotlin
interface AttemptLogRepository {
    suspend fun load(): AttemptLogLoadResult
    suspend fun append(event: AttemptEvent): AttemptLogSaveResult // reads, prunes, appends, writes
}
```

`RawAttemptLogRepository(storage: RawKeyValueStore, now: () -> Instant) : AttemptLogRepository`
reuses the same `RawKeyValueStore` port `RawProgressRepository` already takes — one more key,
`const val ATTEMPT_LOG_KEY = "polski-grammar-srs-kmp-attempts-v1"`, no new storage abstraction per
host. Web gets it for free from the existing `BrowserKeyValueStore` (`WebProgressRepository.kt:20`);
Android/iOS wire their own `RawKeyValueStore` adapters exactly as they already must for
`ProgressRepository` (not read in this pass, but same port, same obligation).

### Failure isolation (explicit, because it is easy to get backwards)

`TrainingStore.rate()` must never let a journal write failure block or roll back the review that
already succeeded via `ReviewReducer`. Wire `AttemptLogRepository.append` through the **same**
`writes: Channel<Write>` pattern `TrainingStore` already uses for `Save`/`Reset`
(`TrainingStore.kt:73,118-131`) — add a `Write.LogAttempt(event)` case, fire-and-forget relative to
the UI (no new `AppUiState` field blocks on it), and surface a failure the same lightweight way
`completeWrite` already surfaces a `SaveResult.WriteFailed` (line 691) — a distinct, non-blocking
`error` message, never a thrown exception that could unwind `rate()`.

## P1.2 — Self-direction ("самостоятельность") signal

One `NEW` pure function, `kotlin/shared/.../polski/presentation/TrainingStore.kt` (private helper)
or a `NEW` small file `AttemptSignals.kt` next to it:

```kotlin
private fun helpShownFor(current: AppUiState): Boolean = when (current.answerMode) {
    AnswerMode.Typed, AnswerMode.Oral -> current.showReference && current.caseReferenceAvailable
    AnswerMode.TileBuilder -> scaffoldingLevelFor(tileRungFor(current)) != ScaffoldingLevel.None
}
```

This is the concrete AC2 contract: `showReference` already persists for the whole card once
toggled (`ToggleReference`, `TrainingStore.kt:204`), so "opened at any point before Reveal" is
exactly what this reads — no new click-tracking needed, matching the Plan's "без необходимости
клика" requirement directly. For TileBuilder, Full/Partial rungs *are* the opened reference (pre-
filled/guided tiles); only `None` counts as unaided.

**Probe attempts must force this to `false` structurally, not just report it.** Per AC2 ("отдельная
проба начинается без открытого справочника... не наследует открытый справочник"), a probe exercise
must enter with `showReference` reset and `ToggleReference` disabled, and TileBuilder scaffolding
pinned to `None` regardless of the skill's stored `tileScaffoldingRung` — the *option* to see help
must not exist during a probe, not merely go unused. This is a `TrainingStore` gating change at
whichever action starts a probe (see P1.3) — `accepts()` must reject `ToggleReference` while
`sessionKind == Probe`, mirroring how `accepts()` already gates actions by phase/mode
(`TrainingStore.kt:226-249`).

`disclosed` (AC3's "неоднозначный свободный ответ не получает выдуманный диагноз") is set from
whichever path actually revealed `expected`/`accepted` text to the learner before this attempt was
judged — today that is exactly `CardPhase.Revealed` having been reached (`reveal()`,
`TrainingStore.kt:456-468`) prior to `rate()`. `correctnessBasis` is `Evaluated` when
`current.evaluation != null` or `tileBuilder?.result != null` (a real `AnswerEvaluator`/tile match
ran), `SelfReported` for `AnswerMode.Oral` (no automatic grading exists — `correct` comes only from
the learner's own rating, never invented), and `Ambiguous` is reserved for a future free-text path
(M8) that does not exist yet for the three pilot skills — **not used by P1's own wiring**, just
declared so AC3's contract has a name instead of being silently merged into `Evaluated`.

## P1.3 — Probe/control test sets (AC6)

Two held-out, disjoint content pools per skill, separate from the normal training pool, split by
**stable seed content** (not exercise ID, which is freshly minted every call —
`ExerciseGenerator.newId()`, line 115 — and therefore useless as a partition key; this is exactly
what AC6 rules out: "не только по случайным ID").

`NEW` in `:core-engine` (`kotlin/core-engine/.../ExerciseGenerator.kt` or a sibling
`ProbeAssignment.kt`):

```kotlin
enum class ContentPool { Training, ProbeD7, ProbeD30 }

/** Deterministic, no RandomSource — same (skillId, seed) always lands in the same pool, for every
 *  user and every run, so a probe item served today is still a probe item at D7/D30. */
fun poolOf(skillId: String, seed: Map<String, String>): ContentPool {
    val bucket = stableHash("$skillId:${seedKey(seed)}") % 100
    return when { bucket < PROBE_D7_PERCENT -> ContentPool.ProbeD7
                  bucket < PROBE_D7_PERCENT + PROBE_D30_PERCENT -> ContentPool.ProbeD30
                  else -> ContentPool.Training }
}
```

`PROBE_D7_PERCENT`/`PROBE_D30_PERCENT` (`NEW` placeholder constants, e.g. 15/15) are pilot tuning
knobs, same spirit as every other placeholder in this codebase — **not** derived from any
statistical power calculation (the Plan §2/§6 explicitly defers sample-size math to a later,
separate step). `stableHash` must be a plain deterministic string hash (e.g. FNV-1a, already present
as `RawProgressRepository.fingerprint`, `ProgressRepository.kt:232-235` — reuse that exact function
rather than inventing a second one), **not** `RandomSource` (which is seeded per-session/platform
and must stay that way for M4/M5's own contract).

- `ExerciseGenerator.candidatesFor`/`pickSeed` (lines 135-136, 254-262) must exclude
  `poolOf(skillId, it) != ContentPool.Training` candidates from every **normal** practice/schedule/
  daily pick — a probe seed must never surface as an ordinary exercise. One added filter step before
  the existing M4 anti-repeat filter.
- `NEW` `generateProbeExercise(skillId: String, window: ContentPool)`: same realization pipeline as
  `generateForSkill`, but picks uniformly (no M4/M5 bias — a probe is not supposed to be adapted)
  from `candidatesFor(filter).filter { poolOf(skillId, it) == window }`.
- This blueprint builds the **separation only** (AC6's actual requirement). Deciding *when* a probe
  is surfaced to a learner (a dedicated "проверка" entry point, scheduled at D7/D30 per participant)
  is a P1-adjacent UX/scheduling decision the Plan does not yet specify precisely enough to design
  here — flagged as an open question, not solved by this blueprint (see Handoff). The minimum P1
  needs is: the partition exists, is stable, is excluded from normal generation, and
  `AttemptKind.Probe` is a real, distinct value a probe-flow caller can stamp.

## P1.4 — Rubric version (AC3's "не выдуманный диагноз" traceability)

`NEW` top-level `const val RUBRIC_VERSION = 1` next to `evaluate()` in `AnswerEvaluator.kt`. Bumped
only when the normalize/match rule itself changes in a way that could change a verdict for existing
accepted-variant data (not on every unrelated edit to the file). `TrainingStore.rate()` stamps every
`AttemptEvent.rubricVersion` with this constant. This is deliberately a single global int, not a
per-skill table — the matching *algorithm* is shared across all skills (`evaluate()` takes no
skill-specific rule set today), so one version number correctly describes "which algorithm judged
this," and a future skill-specific rubric would be a separate, explicit field addition, not a
retrofit of this one.

## P1.5 — Session id, kind, source; correction linkage

- `NEW` `fun interface SessionIdFactory { fun newId(): String }`, same shape as the existing
  `ExerciseIdFactory` (`ExerciseGenerator.kt:18-20`) — injected into `TrainingStore` the same way
  `exerciseEngine`/`time` already are, not hand-rolled inline.
- `NEW` `AppUiState.sessionId: String?` (null only before `start()` completes), minted once at each
  of: `start()` (first load), and every "mode start" action — `StartChain`, `StartSchedule`,
  `StartDaily`, `ChooseSkill`, and the post-`ResetDecision` chain restart (`TrainingStore.kt:625-652`)
  — mirroring exactly the places `TrainingStore` already resets `chain`/`chainIndex`/`daily` to a
  fresh run. `sessionKind` is `current.mode.name`, or `"Probe"` once P1.3's probe flow exists.
  `sessionSource` is a `NEW` small enum (`ManualPick`, `DailyButton`, `ScheduleDue`, `Chain`,
  `Reset`) set at the same six call sites — a static classification of *which action* started the
  session, not inferred later.
- **Open question, not resolved here**: whether "session" for K3's survey ("каждая третья начатая
  обычная учебная сессия") should be *this* app-interaction-level boundary or something coarser
  (e.g. one per app open/tab visit). The Plan's own wording ("начатая... сессия... при завершении
  или явном выходе") suggests a boundary with a clear end, which the six mode-start points above
  have (a `CardPhase.ChainComplete`/`NoDue`/`DailyComplete` or tab navigation away) — proposed as
  the working definition, but flagged for product sign-off before the survey trigger is built.
- Correction linkage: `RetryTicket` (`AppUiState.kt`, M2 blueprint) gets one `NEW` field —
  `data class RetryTicket(val skillId: String, val remaining: Int, val originAttemptId: String)` —
  set from the erroring attempt's own `exercise.id` at the point `rate()` enqueues the ticket
  (`TrainingStore.kt:528-531`). When the ticket matures and substitutes `nextExercise`
  (`maturedTicket`, lines 551-562), the **next** `AttemptEvent` for that substituted exercise is
  stamped `correctionOf = maturedTicket.originAttemptId` and `kind = AttemptKind.Retry`. Every other
  attempt (not born from a matured ticket) gets `kind = AttemptKind.Practice` (or `Probe`, P1.3).

## P1.6 — Active time (flagged scope, not fully designed here)

The Plan requires "активное время" excluding hidden-tab/explicit-pause intervals, with "no clicks ≠
idle" (thinking time counts). This is genuinely host-specific (browser `visibilitychange`, Android
`onPause`/`onResume`, iOS `scenePhase`) and belongs with the platform skills
(`kmp-web`/`kmp-android`/`kmp-ios`), not invented uniformly here. Minimal shared contract to land
with P1 so the schema field is real and hosts can wire into it incrementally (one host at a time,
per this project's own "small tasks" convention):

- `NEW` `AppAction.VisibilityChanged(visible: Boolean)`, dispatched by each host's own lifecycle
  listener (not built in this pass).
- `TrainingStore` accumulates `activeMillisSince` per **currently served** exercise using the
  existing `TimeSource`/`TimeCapture` port (`PresentationPorts.kt:6-9`) — sum of `capture().at` deltas
  while `visible == true`, reset whenever `exercise` changes. `AttemptEvent.activeMillis` reads this
  accumulator at `rate()` time.
- Until a host wires `VisibilityChanged`, `activeMillis` is simply wall-clock-since-served (today's
  only available signal) — correct-but-coarse, never fabricated, and the schema field already has
  the right shape so no later migration is needed once a host adds the listener.

## Wiring summary in `TrainingStore.rate()`

One `NEW` block right after `document = result.document` (`TrainingStore.kt:522`), using values
already computed in the surrounding code (`exercise`, `correctness`, `action.rating`, `captured`,
`current.mode`/`current.answerMode`/`current.sessionId`) plus the signals above:

```kotlin
val event = AttemptEvent(
    attemptId = exercise.id, sessionId = current.sessionId ?: "unknown",
    skillId = exercise.primarySkill, seedKey = seedKey(exercise),
    kind = currentAttemptKind, // Retry when this exercise came from a matured ticket, else Practice/Probe
    at = captured.at.toString(), localDay = captured.localDay,
    activeMillis = activeMillisAccumulator, answerMode = current.answerMode.name,
    helpShown = helpShownFor(current), disclosed = current.phase == CardPhase.Revealed,
    correct = correctness, correctnessBasis = correctnessBasisFor(current),
    selfRating = action.rating.name, correctionOf = activeOriginAttemptId,
    sessionKind = current.sessionKind, sessionSource = current.sessionSource.name,
    rubricVersion = RUBRIC_VERSION,
)
writes.trySend(Write.LogAttempt(event))
```

Exactly one append per successful `rate()` — same guard as the single `recordReview` call, so AC1/AC4
hold by construction (one rating → one review → one journal entry; corrections are new entries with
`correctionOf` set, never edits).

## P2 — formal acceptance of the M1–M3 MVP: which ACs are actually closable today

The task framing says P2 is "already implemented, needs only formal acceptance." That is true for
some ACs and **not yet true** for others — those need P1 to exist first, because they ask questions
only the journal can answer. Stating this precisely matters more than restating "done":

| AC | Closable today (M1–M3 alone)? | Why |
| --- | --- | --- |
| AC1 (first attempt recorded once; correction never overwrites) | **No** | There is no attempt history at all yet — `SkillStats` is aggregate-only. True by construction once P1.1 lands (append-only log), but unverifiable before that. |
| AC2 (shown reference marks aid without a click) | Partially | `showReference`/`tileScaffoldingRung` exist and are the right signals (P1.2), but nothing reads them into a recorded fact today — currently only affects live UI rendering. |
| AC3 (accepted variants stay correct; no invented diagnosis for ambiguous free text) | **Yes** | `evaluate()` is deterministic text matching against authored variants; no free-text diagnosis exists yet for the pilot skills (M8 not built). Already satisfied by scope absence, not by a built safeguard — worth saying explicitly rather than claiming a feature that doesn't exist yet. |
| AC4 (one rating → one review; extra exposures visible separately) | Partially | The "one rating → one review" half holds today (`rate()`'s single `recordReview` call). "Extra exposures visible separately" needs the journal (P1.1) to actually show them — not visible anywhere today beyond `totalReviews`. |
| AC5 (old progress + unknown fields still load) | **Yes** | `ProgressCodec`'s `source` passthrough and additive-field defaulting (`tileScaffoldingRung` etc.) already covers this, with existing test coverage (`ProgressCodecTest.kt`, `MigrationFailureTest.kt`). |
| AC6 (training vs. probe content separated by stable content) | **No** | No probe pool exists at all before P1.3. |
| AC7 (expected KPI aggregates from error/help/skip/oral scenarios; non-attendance not silently dropped) | **No** | Needs the journal to compute anything — today only `SkillStats` aggregates exist, with no per-attempt replay. |
| AC8 (session resumes/ends; voluntary continue; no forced full-queue drain) | **Yes** | `DailySessionState`/`continueDaily`/`CardPhase.DailyComplete` already implement exactly this (M3). |
| AC9 (real device/browser walk, not just unit tests) | Needs doing, not blocked | Independent of P1 — a verification task on the already-shipped M1–M3 UI, per this project's own `e2e-acceptance` convention. |
| AC10 (existing fixtures/oracles not rewritten to match new code without a changed requirement) | **Yes**, as a discipline, not a one-time fact | Holds as long as future P1/P2 work keeps following it; nothing in M1–M3 violated it so far. |

**Conclusion for the main agent:** P2's "formal acceptance" cannot be fully closed out before P1
ships, because AC1/AC6/AC7 (and half of AC2/AC4) are specifically about data P1 introduces. The
honest sequencing is: ship P1.1–P1.5 (schema + journal + probe split + helpShown/correctionOf
wiring) → re-run AC1/AC2/AC4/AC6/AC7 against the now-real journal → then P2's acceptance sign-off is
actually meaningful. AC3/AC5/AC8/AC10 can be signed off now; AC9 just needs the existing verification
pass run.

## Verification

- `kotlin/srs-progress/src/commonTest/.../AttemptLogCodecTest.kt` (`NEW`): round-trip encode/decode,
  unknown-field passthrough, pruning-by-age at the repository boundary, same fixture style as
  `ProgressCodecTest.kt`.
- `kotlin/core-engine/src/commonTest/.../ExerciseGeneratorTest.kt`: `poolOf` is deterministic and
  stable across repeated calls for the same `(skillId, seed)`; `Training`-pool exclusion never lets a
  `ProbeD7`/`ProbeD30` seed through `generateForSkill`; `generateProbeExercise` only draws from its
  requested window.
- `kotlin/shared/src/commonTest/.../TrainingStoreTest.kt`: one `rate()` call appends exactly one
  `AttemptEvent`; a matured M2 retry's event carries the correct `correctionOf`; `helpShownFor`
  matches `showReference`/tile-rung expectations across all three `AnswerMode`s; a probe session
  rejects `ToggleReference` via `accepts()`; a journal write failure (`AttemptLogRepository.append`
  throwing) leaves the just-recorded review/FSRS state intact (failure isolation).
- Playwright (web): a forced error followed by its matured retry shows the "Ещё раз" badge
  (existing M2 AC) **and** — once a debug/export surface exists for the journal (not designed here;
  reuse the existing `RequestExport` pattern if a human-readable dump is needed for manual QA) — the
  exported journal shows two linked events. Exact export UI is a small follow-up, not specified in
  this blueprint.

## Documentation and handoff

- `Plans/Learning/Plan.md` §7/§10 should gain a P1 status line once implemented, same convention the
  M2–M7 blueprint used.
- `AI/decisions.md`: record the `AttemptLog` storage-key convention, the age-based (not count-based)
  retention choice, and the `poolOf` hash-based probe split, once implemented — cross-cutting
  decisions later mechanics (M8–M10, which also need probe content) will need to know about.
- Real open questions, not resolved by this blueprint:
  1. Exact session-boundary definition for K3's survey trigger (P1.5) — needs product sign-off.
  2. When/how a probe (D7/D30) is actually surfaced to a learner as a flow — P1.3 only builds the
     content separation, not the scheduling/entry UX.
  3. `contentVersion` stamping — no course/content version identifier exists anywhere in the codebase
     today (checked `polski.data` pack registry); needs its own small decision (e.g. a DSL/biome
     build stamp) before this field can be anything but `null`.
  4. `PROBE_D7_PERCENT`/`PROBE_D30_PERCENT`/`RETENTION_DAYS` are placeholder constants, same
     "tune during the pilot" status as `RETRY_DELAY`/`TilePromotionThresholds` — not derived from any
     sample-size calculation.
  5. Active-time host wiring (P1.6) is explicitly deferred per-host, one small task per platform.
