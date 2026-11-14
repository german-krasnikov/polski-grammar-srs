# M8–M10 blueprint: words → phrases, speech missions, exam → training

Status: plan only, nothing implemented. Written against `Plans/Learning/Plan.md` §3 (M8/M9/M10 rows) and §5 (P6). Base: current `main` after the P1 attempt-journal commit (`324372a`).

Scope note: all three mechanics are **web-first** (`kmp-web`). Shared Kotlin stays host-agnostic (no DOM/Compose types), so Android/iOS keep compiling, but their own screens are not required to adopt the new UI in this pass — same split the Learning Plan already uses for M1–M7.

## Shared grounding (read before either mechanic)

- **Vocabulary ↔ grammar lexeme bridge already exists as data, not code.** `courses-biome/pl-ru/vocabulary.core-data` items use ids `noun.<x>`/`adjective.<x>`/`verb.<x>` (e.g. `noun.book`, lemma `"książka"`). `courses-biome/pl-ru/nouns.core-data` has the matching lexeme row `NOUN { id: book, lemma: "książka", gender: f, forms: {...} }`. `courses-biome/pl-ru/sentenceSeeds.core-data` pairs that same `book` id with an adjective: `SEED {nounId: book adjectiveId: new}`. So `noun.<x>` strips to exactly the `nounId` the generator/seed pool already uses — no new ID scheme needed.
- **Forcing a specific word into a grammar exercise already has a public contract.** `ExerciseGenerator.generateForSkill(skillId, preferredSeed, ...)` (`kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/ExerciseGenerator.kt:274`) takes an optional `preferredSeed: Map<String,String>` (`"noun"`/`"adjective"` keys) and uses it verbatim as the seed **iff** some candidate for that skill's `lexicalFilter` has the same noun id (`ExerciseGenerator.kt:290`: `candidates.any { it["noun"] == preferred["noun"] }`); otherwise it silently falls back to a random seed. `TrainingStore.chooseSkill` (`kotlin/shared/src/commonMain/kotlin/polski/presentation/TrainingStore.kt:509-537`) already wires `AppAction.ChooseSkill(skillId, preferredSeed: SentenceSeed?)` end to end: validates the seed's `nounId`/`adjectiveId` exist (`polski.data.nouns`/`polski.data.adjectives`), calls `generateForSkill`, switches `tab = AppTab.Training`, `mode = Focused`, `sessionSource = SessionSource.ManualPick`.
- **Risk this creates for M8/M10:** nothing today checks *before* dispatch whether the chosen `skillId`'s `lexicalFilter` actually admits the forced noun. If it doesn't, `ChooseSkill` silently practices a different, randomly-picked word while the UI still says "practice `książka`" — unacceptable for a feature whose whole point is "use *this* word." M8 must close that gap (see its AC2).
- **Exam items are flat `biome` rows with globally unique keys**, not nested literals: `dsl/exam.data-contract:150` `RECORD GrammarItemRow KEY itemId COUNT 1..`. Exam content is pinned to a certified source (SHA-256 provenance per `AGENTS.md`), so M10's skill tagging must live in a **separate, author-only side table** that references `itemId` by `ref`, never edits `GrammarItemRow` itself.
- **Per-item exam correctness already exists**: `grammarReviewRows(test, frozenAnswers)` (`kotlin/shared/src/commonMain/kotlin/polski/exam/ExamReviewRow.kt:20`) returns one `ExamReviewRow(itemId, evaluation: ExamAnswerEvaluation, ...)` per item; `ExamGrammarWeb.kt:300-330` (`renderResult`) already iterates these after submit. That's the exact hook for M10's "practice this skill" prompt.
- **The attempt journal (P1) is additive and versioned** (`AttemptEvent` in `kotlin/srs-progress/src/commonMain/kotlin/polski/progress/AttemptLog.kt`, `schemaVersion`/unknown-field passthrough already designed for extension). M8 and M10 reuse `AttemptKind.Practice`/`Retry` and `SessionSource` as-is; M10 needs one new `SessionSource` entry (below). M9 is the only one proposing a genuinely new concept (meaning vs. form scored separately) and is scoped as a follow-up to the journal, not a blocker for its MVP.

---

## M8 — vocabulary word → phrase (M size)

### Scope and consumers

Web `VocabularyWeb.kt` typed mode + shared `polski.vocabulary`/`polski.presentation` code. Covers **noun** vocabulary items only (ids `noun.*`); adjective/verb items are out of scope for this pass (see Decisions).

### Public contract

New pure functions, no behavior change to existing ones:

```kotlin
// kotlin/shared/src/commonMain/kotlin/polski/vocabulary/VocabularyUsage.kt (NEW)
fun VocabularyItem.lexemeId(): String? =            // "noun.book" -> "book"; null for non-noun ids
    id.takeIf { it.startsWith("noun.") }?.removePrefix("noun.")

/** Normalized-text check against the one correct side for [direction] — lemma when recalling the
 *  target, translation when recalling native. No accepted-variant list exists yet for vocabulary
 *  (unlike grammar `Exercise.accepted`); this is a single-string compare via the same
 *  `polski.grammar.normalize` grammar typed-answer checking already uses. */
fun checkTyped(item: VocabularyItem, direction: StudyDirection, target: String, native: String, typed: String): Boolean
```

```kotlin
// kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/ExerciseGenerator.kt (extend)
/** Which of [skills]'s own skill ids a word with this noun id could actually be forced into via
 *  generateForSkill's preferredSeed — reuses the exact matchesFilter rule generateForSkill already
 *  applies internally, so a caller never has to guess compatibility and risk a silent substitution. */
fun eligibleSkillsFor(nounId: String): List<String>
```

```kotlin
// kotlin/shared/src/commonMain/kotlin/polski/presentation/TrainingStore.kt (extend)
fun eligibleSkillsFor(nounId: String): List<String> = exerciseEngine.eligibleSkillsFor(nounId)
```

No change to `AppAction`/`AppUiState`: M8 reuses `AppAction.ChooseSkill(skillId, preferredSeed)` exactly as M3/UC-07 already built it.

### Components and dependencies

| File | Change |
|---|---|
| `kotlin/shared/src/commonMain/kotlin/polski/vocabulary/VocabularyUsage.kt` | NEW — `lexemeId()`, `checkTyped()`, pure, no I/O |
| `kotlin/core-engine/.../ExerciseGenerator.kt` | add `eligibleSkillsFor(nounId)`, built from existing private `candidatesFor`/`matchesFilter`/`skills` — no new state |
| `kotlin/shared/.../presentation/TrainingStore.kt` | one-line delegating accessor |
| `kotlin/composeApp/src/webMain/kotlin/polski/ui/VocabularyWeb.kt` | `renderRevealedAnswer` (around line 302): replace the self-grading sentence with an automatic `checkTyped` verdict in typed mode; after reveal, for a noun item with `lexemeId() != null`, render a "Использовать в фразе" (use in a phrase) list built from `trainingStore.eligibleSkillsFor(lexemeId)`, each button dispatching `AppAction.ChooseSkill(skillId, SentenceSeed(nounId, <that seed's adjectiveId>))` and navigating `WebRouteController.navigate(WebRoute.Training)` |
| `kotlin/shared/src/commonMain/kotlin/polski/training/PlExerciseEngine.kt` | none — `sentenceSeeds` already public |

Phrase *progression* (recognition → modified phrase → full sentence → negation) is **not** a new authored list: it falls out of ordering `eligibleSkillsFor(nounId)` by each skill's existing `level`/`group`/`construction` fields (`polski.data.skills`), e.g. a `case.acc.<gender>` skill before `case.gen.neg`. No new DSL table is needed for the ordering itself — it's a sort over data that already exists.

### State and resources

No new persisted state. `checkTyped` is pure and stateless. The "use in a phrase" action reuses `TrainingStore`'s existing session/document ownership; crossing from the Vocabulary screen to Training is a navigation action already modeled by `WebRoute`/`AppTab`.

### Decisions and tradeoffs

- **Noun-only scope.** `generateForSkill`'s `preferredSeed` match compares only `it["noun"]`; there is no equivalent "force this adjective" or "force this verb" contract today. Extending that is a core-engine change with its own recipe implications and is deferred — flagged here so it isn't silently assumed solved.
- **No new accepted-answer list for vocabulary.** Reusing a single lemma/translation compare (via `normalize`) instead of inventing a variant list keeps this M-size; a word with multiple acceptable Russian translations is a known limitation, not a regression (today there is *no* check at all, so this is strictly additive).
- **`eligibleSkillsFor` lives on `ExerciseGenerator`, not duplicated in `presentation`,** so the single `matchesFilter` rule (gender filters, explicit `nounId` allow-lists like `agreement.my`'s `{nounId: [wife husband friendM son child]}`) stays the one source of truth `generateForSkill` itself uses — this is what prevents the silent-substitution risk flagged in the shared grounding section.

### Acceptance cases

- **AC1:** typed vocabulary answer now gets an automatic correct/incorrect verdict (normalized compare against lemma or translation depending on direction); the previous "сравни сам" self-grading sentence remains available to the host as a fallback label but is no longer the only signal.
- **AC2:** every skill id offered under "Использовать в фразе" for a given word is one `eligibleSkillsFor` actually returned for that word's `nounId` — dispatching `ChooseSkill` for it never falls back to a different noun (regression test: for every `vocabularyItems` noun, every id in `eligibleSkillsFor` round-trips through `generateForSkill(skillId, preferredSeed)` with the exact same `slots["noun"]` back out).
- **AC3:** a noun whose gender/id is excluded by every skill's `lexicalFilter` shows no "use in a phrase" affordance at all (empty list is a valid, handled state, not an error).
- **AC4:** existing vocabulary flip/typed/oral flows, FSRS scheduling and persisted `VocabularyDocument` are unchanged for any interaction that doesn't touch the new affordance.

### Verification

- `commonTest`: `VocabularyUsageTest` (lexemeId/checkTyped table), `ExerciseGeneratorTest` addition for `eligibleSkillsFor`.
- Playwright: extend `vocabulary` specs — type a correct/incorrect answer and assert the verdict; click "Использовать в фразе" for a known word (e.g. `noun.book`) and assert the Training tab opens with that noun's lemma visible in the exercise.

### Implementation order

1. `VocabularyUsage.kt` + unit tests (pure, no UI).
2. `ExerciseGenerator.eligibleSkillsFor` + unit test (AC2's round-trip check).
3. `TrainingStore` accessor.
4. `VocabularyWeb.kt` UI + Playwright.

---

## M10 — exam mistake → targeted training → verification question (M–L size)

### Scope and consumers

Web `ExamGrammarWeb.kt` review screen + shared `polski.exam`/`polski.presentation`. Scoped to the **grammar** exam module only (it is the module with a generative, skill-shaped course counterpart); reading/listening/writing items have no equivalent course skill to redirect to and are out of scope.

### Public contract

New DSL-backed lookup (shared, pure):

```kotlin
// generated by biome from dsl/exam-skill-links.data-contract (NEW)
data class ExamSkillLinkRow(val itemId: String, val skillId: String, val note: String?)
val examSkillLinks: Map<String, List<ExamSkillLinkRow>>  // itemId -> link rows (grouped for lookup)
```

```kotlin
// kotlin/shared/src/commonMain/kotlin/polski/exam/grammar/GrammarExamSkillLink.kt (NEW)
/** The linked skill, or null when this exam item has none authored yet. */
fun skillIdFor(itemId: String): String?

/** A different item sharing [skillId] (not [excludeItemId]) to use as the post-training check —
 *  null when only one item is linked to this skill, in which case the host must fall back to a
 *  freshly generated training-skill exercise (see Decisions). */
fun verificationItemFor(skillId: String, excludeItemId: String): String?
```

New presentation-level state, mirroring M2's `RetryTicket` shape:

```kotlin
// kotlin/shared/src/commonMain/kotlin/polski/presentation/AppUiState.kt (extend)
enum class SessionSource { ManualPick, DailyButton, ScheduleDue, Chain, Reset, ExamMistake }  // +1 entry

data class ExamRetryOrigin(val examRouteId: String, val itemId: String, val skillId: String, val verificationItemId: String?)
// carried on AppUiState as `examRetryOrigin: ExamRetryOrigin? = null`, cleared once the targeted
// skill is rated Good once — same lifetime shape as `RetryTicket`.
```

### Components and dependencies

| File | Change |
|---|---|
| `dsl/exam-skill-links.data-contract` | NEW — `RECORD ExamSkillLinkRow KEY linkId COUNT 0.. { linkId: string, itemRef: ref GrammarItemRow, skillId: string, note: string OPTIONAL }` |
| `courses-biome/pl-ru/exam-skill-links.core-data` (and per-exam files as authored) | NEW — author-curated rows, one per exam item worth linking; at least 2 items per targeted `skillId` so a verification question differs from the mistake trigger |
| `kotlin/shared/src/commonMain/generated/.../ExamSkillLinkRows.kt` | generated by `biome generate` |
| `kotlin/shared/src/commonMain/kotlin/polski/exam/grammar/GrammarExamSkillLink.kt` | NEW — `skillIdFor`, `verificationItemFor`, both pure lookups over the generated rows |
| `kotlin/shared/src/commonMain/kotlin/polski/presentation/AppUiState.kt` | `SessionSource.ExamMistake`, `ExamRetryOrigin` |
| `kotlin/shared/src/commonMain/kotlin/polski/presentation/TrainingStore.kt` | `chooseSkill`/`rate` set/clear `examRetryOrigin`; on a Good rating with a non-null `examRetryOrigin`, emit a `UiEffect`/event the host can react to ("exam verification question is ready") instead of an in-store exam navigation (TrainingStore must not depend on `polski.exam`) |
| `kotlin/composeApp/src/webMain/kotlin/polski/ui/ExamGrammarWeb.kt` | `renderResult` (line ~300-330): for a review row whose `evaluation.status != ConfirmedFull` and `skillIdFor(itemId) != null`, render a "Потренировать навык" button; clicking it navigates to Training via `WebRouteController.navigate(WebRoute.Training)` and dispatches `ChooseSkill(skillId)` with `examRetryOrigin` set |
| `kotlin/composeApp/src/webMain/kotlin/polski/ui/WebRouteController.kt` | none — `WebRoute.Exam`/`WebRoute.Training` already both exist |
| `kotlin/srs-progress/.../AttemptLog.kt` | none required for MVP: `AttemptEvent.sessionSource`/`sessionKind` already carry enough to reconstruct "this practice followed an exam mistake" once `SessionSource.ExamMistake` exists; `sessionId` can encode `examRouteId:itemId` if a stronger join key is needed later |

### State and resources

`examRetryOrigin` is session-only (not persisted), same lifetime class as the existing `daily`/`focusedSkillId` transient fields on `AppUiState` — cleared on a qualifying Good rating or on leaving Training via any other `ChooseSkill`/`StartDaily`/etc. action. The verification exam item is **not** auto-submitted; the learner is shown it as a normal exam item they answer themselves, so no exam-session state is force-mutated by the training side.

### Decisions and tradeoffs

- **Separate side table, not a `GrammarItemRow` field.** Keeps certified exam content byte-identical to its provenance-checked source; the link table is pure course metadata and can be revised without touching exam data integrity checks (`ExamContentIntegrityAuditTest` et al. stay untouched).
- **"New exam question" means a *different authored item for the same skill*, not a freshly generated training exercise**, per the Learning Plan's explicit KPI note ("повтор разобранного вопроса не считается переносом" — repeating the disclosed question doesn't count as transfer). This requires **at least two linked items per targeted skill** to be authored; `verificationItemFor` returning `null` is a real, expected state for an under-authored skill and must degrade visibly (e.g., "скоро добавим новый вопрос"), not silently reuse the same item.
- **`TrainingStore` must not import `polski.exam`.** The cross-tab flow (exam → train → back to exam) is coordinated by the web host (`ExamGrammarWeb`/whatever composes it with `TrainingWebApp`), not by `TrainingStore` reaching into exam types — keeps the module dependency direction one-way (`exam` → nothing in `presentation`; `presentation` stays exam-agnostic).

### Acceptance cases

- **AC1:** a `GrammarExamSkillLink` row always references a real `itemId` (validated by `biome generate --check`, same mechanism other `ref` fields already use).
- **AC2:** after submitting a grammar exam with ≥1 wrong linked item, the review screen shows a "Потренировать навык" affordance only for wrong+linked items, never for correct or unlinked ones.
- **AC3:** clicking it opens Training already on that skill (`focusedSkillId == skillId`), and `examRetryOrigin` is set; rating that exercise Good clears `examRetryOrigin` and signals the host to present `verificationItemFor(skillId, originalItemId)` — never the original item id.
- **AC4:** a skill with only one linked item degrades to an explicit "not enough linked questions yet" state, not a silent repeat of the same item.
- **AC5:** existing exam scoring/review behavior (`GrammarExamScorer`, `ExamReviewRow`) is byte-for-byte unchanged for any item without a link row.

### Verification

- `commonTest`: `GrammarExamSkillLinkTest` (lookup + verification-item selection, including the "only one linked item" degrade case).
- `biome generate --check --project dsl/biome.project` for the new contract/rows.
- Playwright: submit a grammar exam with a known wrong+linked answer, click the practice affordance, rate Good, assert navigation back to a *different* exam item id than the one that triggered it.

### Implementation order

1. `dsl/exam-skill-links.data-contract` + a handful of authored links for one exam + `biome generate`.
2. `GrammarExamSkillLink.kt` + unit tests.
3. `AppUiState`/`TrainingStore` additions (`SessionSource.ExamMistake`, `ExamRetryOrigin`, effect on qualifying rating).
4. `ExamGrammarWeb.kt` wiring + Playwright.
5. Author remaining exam→skill links incrementally (content work, not blocked on code).

---

## M9 — speech mini-missions (L size, MVP-reduced)

### Scope and consumers

Web-only MVP: one new screen, one new shared session type. Does **not** reuse `ExerciseGenerator`/constructed recipes — missions are hand-authored branching dialogue, not generated constructions, so they need their own small DSL and a much simpler session (no FSRS scheduling in the MVP; "has the learner completed mission X" is the only persisted fact).

### Minimal DSL

```
# dsl/missions.data-contract (NEW)
DSL 1
# Authored branching speech scenario: a goal, a sequence of situations, and per-situation reply
# choices with both a meaning consequence (does the situation move toward the goal) and a
# grammar-form check (is the typed/picked reply an accepted form). Content only — no generative
# construction/recipe involvement, unlike constructed skills.

RECORD MissionRow KEY missionId COUNT 0..
  missionId: string
  title: string
  goal: string
  level: string
  skillTag: string OPTIONAL      # loosely associates the mission with one course skill for reporting; not validated against skills.core-data (different DSL style)
END

RECORD MissionNodeRow KEY nodeId COUNT 0..
  nodeId: string
  missionRef: ref MissionRow
  situation: string              # native-language scene description shown before the choices
  prompt: string                 # what the learner is asked to express
  isTerminal: bool
  outcome: enum goal_reached|dead_end|none OPTIONAL   # meaningful only when isTerminal
END

RECORD MissionChoiceRow KEY choiceId COUNT 0..
  choiceId: string
  nodeRef: ref MissionNodeRow
  label: string                  # native-language gloss shown on the choice button
  expectedForm: string           # the one accepted target-language reply text
  acceptedVariant: string OPTIONAL
  meaningAdvancesGoal: bool       # scored independently of expectedForm's grammar check
  nextNodeRef: ref MissionNodeRow OPTIONAL   # absent on a terminal choice
END
```

### Public contract (shared Kotlin)

```kotlin
// kotlin/shared/src/commonMain/kotlin/polski/mission/MissionSession.kt (NEW)
data class MissionOutcome(val meaningCorrect: Boolean, val formCorrect: Boolean, val reachedGoal: Boolean)

interface MissionRepository {               // same shape as VocabularyRepository
    suspend fun loadRaw(): String?
    suspend fun saveRaw(value: String)
}

class MissionSession(private val repository: MissionRepository, private val now: () -> kotlin.time.Instant) {
    val state: kotlinx.coroutines.flow.StateFlow<MissionUiState>
    suspend fun start()
    fun choose(choiceId: String, typedForm: String): MissionOutcome   // grades form via polski.grammar.normalize, same rule AnswerEvaluator/GrammarExamScorer already use
    suspend fun complete()                                            // persists "missionId -> last outcome/date"
}
```

Grammar grading reuses the exact normalize-and-compare rule already used twice elsewhere (`polski.training.evaluate`, `GrammarExamScorer.score`) — a third copy of the same one-liner, not new text-matching logic.

### Components and dependencies

| File | Role |
|---|---|
| `dsl/missions.data-contract` | NEW schema |
| `courses-biome/pl-ru/missions.core-data` | NEW — 2-3 authored missions for the MVP (per Plan.md's example: finding out if a book is available → choosing another → reporting what was bought) |
| `kotlin/shared/src/commonMain/kotlin/polski/mission/MissionSession.kt`, `MissionDocument.kt` | NEW — session/document pair, same shape as `polski.vocabulary.VocabularySession`/`VocabularyDocument` |
| `kotlin/composeApp/src/webMain/kotlin/polski/ui/MissionWeb.kt` | NEW screen |
| `kotlin/composeApp/src/webMain/kotlin/polski/ui/WebRouteController.kt` | add `WebRoute.Mission("mission", null)` |

### State and resources

`MissionDocument` persists only `Map<missionId, MissionProgress(lastOutcome, completedAt)>` — no FSRS card, no attempt-journal entry in the MVP. This is a deliberate scope cut flagged below, not an oversight.

### Decisions and tradeoffs

- **No attempt-journal integration in the MVP.** M9's KPI ("T7 on a new comparable mission +10pp and E +0.5; meaning and form scored separately") needs a journal shape the current `AttemptEvent` doesn't have (a single `correct: Boolean?`, not two independent booleans). Rather than bolt on a half-fitting field under schedule pressure, the MVP keeps mission outcomes host-local (`MissionDocument`) and the journal extension (`meaningCorrect`/`formCorrect` as two new optional fields, additive per the existing versioning discipline) is a named follow-up before any mission-based KPI measurement is attempted. Flagging this now so it isn't assumed solved by "the session compiles."
- **No FSRS/spaced repetition for missions in the MVP.** A mission is closer to a short story than a flashcard; re-running the same branching script on a schedule has unclear pedagogical value until there's more than 2-3 missions. "Replay" stays a manual action, not a scheduled one.
- **`nextNodeRef` branching is a simple forward DAG, not a full state machine.** No shared variables across nodes (e.g. "remember which book was mentioned") in the MVP — each node's `situation`/`prompt` text is authored to read naturally regardless of path, same constraint small narrative games accept at this scale. A mission needing real state carries its own risk and should be prototyped as content before the engine is asked to support it.

### Acceptance cases

- **AC1:** every `MissionChoiceRow.nextNodeRef` (when present) resolves to a real node in the same mission (`biome generate --check`).
- **AC2:** every mission has at least one path from its start node to a terminal node with `outcome = goal_reached`.
- **AC3:** `MissionSession.choose` reports `formCorrect` and `meaningAdvancesGoal`'s value independently — a grammatically correct but goal-irrelevant reply is distinguishable from a goal-correct but misspelled one in the returned `MissionOutcome`, and both are shown to the learner as separate signals (not merged into one pass/fail).
- **AC4:** completing a mission persists `MissionDocument` via the same load/save-and-verify discipline `VocabularySession`/`TrainingStore` already use (no silent data loss on a failed write).

### Verification

- `commonTest`: `MissionDocumentTest` (DAG reachability + persistence round-trip), `MissionSessionTest` (choice grading, meaning/form independence).
- Playwright: play one authored mission end to end on web, including one "wrong form, right meaning" choice and asserting both signals render distinctly.

### Implementation order

1. `dsl/missions.data-contract` + 1 authored mission (just enough to unblock engine work) + `biome generate`.
2. `MissionDocument`/`MissionSession` + unit tests.
3. `MissionWeb.kt` + route wiring + Playwright for the one mission.
4. Author the remaining 1-2 MVP missions (content work).
5. Journal extension (`meaningCorrect`/`formCorrect`) as a separate follow-up task, once M9's MVP is observed to be worth measuring against K1/K3.

---

## Package-level ordering and open questions

Recommended order across the three: **M8 → M10 → M9**. M8 and M10 are almost entirely wiring of mechanisms that already exist (`preferredSeed`, `ChooseSkill`, `grammarReviewRows`) plus one small additive DSL table each — fast to build, fast to get wrong in an obvious and cheap-to-fix way. M9 is genuinely new (DSL, engine, content, screen) and benefits from M8/M10's "link course skill to something external" pattern already being proven once (the `eligibleSkillsFor`/`skillIdFor` shape) before inventing a third variant for missions.

Open questions for the product/content owner, not decided by this blueprint:
1. How many exam items per skill are realistically authorable for M10's "different verification question" requirement — if most skills only ever get one linked item, the KPI as written in Plan.md §2 K1 can't be measured for them.
2. Whether M9's missions should eventually share the attempt journal (making them comparable to M1–M7's KPIs) or stay a separate, qualitatively-evaluated feature — this blueprint defers the decision but names the exact schema gap that forces it.
3. M8's adjective/verb vocabulary items currently have no phrase-linking path at all; whether that's acceptable for the first release or needs its own follow-up.
