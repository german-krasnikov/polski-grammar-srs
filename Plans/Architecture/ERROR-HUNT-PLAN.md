# Module design: "Найди ошибку" (Error Hunt)

Дата: 2026-11-18. Статус: **design only — no production code changed.**

---

## Scope and consumers

**Capability**: a new training sub-mode under `AppTab.Cloze` — show a sentence containing exactly
one substituted wrong form (same lexeme, wrong case/gender/number); user reads the sentence and
taps the error word among all tappable tokens; then chooses the correct form from 3 options; sees
a rule snippet.

**Target source sets**: `commonMain` for domain model + generator + reducer; `webMain` DOM
controller; `androidMain` Compose screen (deferred task); `iosMain` bridge snapshot + dispatch.

**Hosts in scope**: web (Wasm/JS, primary), Android (adds Cloze tab to Android nav), iOS
(apple-bridge snapshot + SwiftUI, deferred).

**Desktop**: frozen by ADR (NativeHostDecision.md) — no new features.

---

## Key decisions

### D1 — Data source: `fromCoreExercise` only

`ClozeGenerator.fromCoreExercise` derives distractors via
`pickDistractors(morphology, lexemeKey, correctForm, 2, random)` — same lexeme, same paradigm,
different case/number/gender. This guarantees the substituted form is a **genuine learner-style
inflection error** (e.g. "nauczycielka" vs "nauczycielkę"), not a lexically different word.

`ClozeGenerator.fromExamItem` uses authored exam options which may be non-paradigm distractors.
**Excluded from Error Hunt.**

Syncretism edge-case: paradigm forms that are valid in the same slot (e.g. neuter nom == acc)
may slip through the paradigm filter. The validity guard in D8 catches accepted-form distractors;
remaining undetected syncretism is low-frequency and noted in `ErrorHuntGenerator` KDoc as a
known open issue.

**en-ru pack**: if course exercises lack `tileTargetSlots`, the pool will be empty; the UI
shows "No exercises available" (same fallback as Cloze mode). No hardcoded Polish anywhere.

### D2 — Navigation: sub-mode of `AppTab.Cloze`

The nav bar already has 8 items. A 9th tab for a mode variant is not warranted.
Error Hunt is promoted to its own tab only if it outgrows the sub-mode.

Mechanism: a new `enum class ClozeSubMode { Fill, ErrorHunt }` on `AppUiState`. Hosts render
a segmented control at the top of the Cloze tab. Switching the control dispatches
`SetClozeSubMode` → TrainingStore starts the new session.

### D3 — Interaction (Phase 1 MVP — all tokens tappable)

**No pre-highlighting.** The error word must not be distinguished at first render; otherwise
there is nothing to find and the mode collapses to Cloze with a wrong word.

Tokenization: split `before` and `after` by whitespace. Punctuation stays attached to its token
(e.g. "domu." is one token); the form used for comparison strips trailing `.,:;!?`.

Interaction flow:
1. All tokens rendered as tappable buttons; no visual distinction between words.
2. **Tap a correct word** → `ErrorHuntPolicy.tapOutcome(tapIsError=false)` → "miss"; "это слово
   верное" feedback (non-blocking). After `ErrorHuntPolicy.shouldReveal()` returns "revealed"
   (2 misses reached), the error token is highlighted, prompting the user to tap it.
3. **Tap the error token** → `tapOutcome(tapIsError=true)` → "hit"; 3 option buttons appear.
4. **Choose a form** → `ErrorHuntPolicy.scoreClass(found, missCount, answerCorrect)` →
   "perfect" / "found_late" / "revealed_correct" / "wrong_form"; feedback (correct/wrong +
   correct form + rule text). Auto-advance after `ERROR_HUNT_FEEDBACK_DELAY_MS = 1_500` ms if
   not last item.

Summary after the last exercise: counts per score class; "Заново" restarts.

Future phase (not in scope here): add "this sentence has no error" variant exercises.

The two-part split `(before, errorForm, after)` is stored in `ErrorHuntExercise` and derived
deterministically from `ClozeExercise.displaySentence.split("___")` + one distractor.

### D4 — DRY: extract shared slot logic

`ClozeGenerator.fromCoreExercise` and the new `ErrorHuntGenerator.fromCoreExercise` share the
same slot-extraction steps (phrase → slot → span → offsets → correctForm + lexemeKey). Extract
to an internal `extractSlot(exercise, morphology): SlotInfo?` in the `polski.cloze` package
(P7 — one fact, one place). Both generators call it.

### D5 — Rule explanation

`SKILL.theory` from `courses-biome/<pair>/skills.core-data` is a per-pair string already
authored for every core skill. Inject into `ErrorHuntGenerator` as `theoryFor: (skillId) -> String?`.
No new DSL field. `ruleText: String?` is null when skill not found or theory is empty.

### D6 — FSRS / attempt journal

No FSRS scheduling. No attempt-journal entries (P1 blueprint is not yet built).
Same decision as AudioCloze. This is a drilling mode, not a formal review.

### D7 — Scoring

In-session only. `ErrorHuntPolicy.scoreClass` classifies each exercise into four buckets:
- **perfect**: error found on first tap (0 misses) AND correct form chosen.
- **found_late**: error found with ≥ 1 misses AND correct form chosen.
- **revealed_correct**: auto-revealed (threshold misses) AND correct form chosen.
- **wrong_form** (default): correct form NOT chosen (regardless of how the error was found).

Summary screen after the last exercise shows bucket counts. No persistence.

### D8 — Validity guard: exclude accepted-answer distractors

A distractor is excluded if, after `normalize()` (NFC — same function as `AnswerEvaluator`),
it equals `normalize(exercise.expected)` or any `normalize(a)` for `a in exercise.accepted`.
This prevents substituting a form that is a valid answer, making the exercise unanswerable.

The generator computes two boolean facts natively:
- `distractorEqualsCorrect = normalize(distractor) == normalize(exercise.expected)`
- `distractorInAccepted = normalize(distractor) in exercise.accepted.map(::normalize)`

Then calls `ErrorHuntPolicy.distractorValid(distractorEqualsCorrect, distractorInAccepted)`.
If "invalid", the distractor is skipped. If no valid distractor remains, `fromCoreExercise`
returns null.

### D9 — DSL-first policy for verdicts; Kotlin for mechanical facts and UI timing

**Belongs in DSL** (`dsl/learning/error-hunt.logic`):
- tap outcome decision (hit/miss)
- reveal threshold (2 misses → revealed; fixed in LOGIC condition)
- score class classification (perfect/found_late/revealed_correct/wrong_form)
- distractor validity verdict

**Stays in Kotlin** (native helpers — mechanical / platform boundary):
- Tokenization of `before`/`after` by whitespace (mechanical text processing)
- `normalize(tappedForm) == normalize(errorForm)` → `tapIsError: Boolean` (mechanical; reuses
  existing `normalize()`)
- `normalize(distractor)` comparisons → `distractorEqualsCorrect: Boolean`,
  `distractorInAccepted: Boolean` (mechanical; not verdicts)
- Reducer mutation logic: "which map/set to update" per policy outcome (P12 — model owns
  mutations, not DSL DECISION)
- `ERROR_HUNT_FEEDBACK_DELAY_MS = 1_500` in `ErrorHuntWeb.kt` — UI timing, host-layer constant
  following `CLOZE_FEEDBACK_DELAY_MS` precedent in `ClozeWeb.kt`
- `ERROR_HUNT_SESSION_SIZE = 10` in `TrainingStore.startErrorHunt()` — session size, following
  `startCloze()` hardcoded `10` precedent

Acknowledged drift between session-size/feedback-delay and BestPractice §1 "thresholds in DSL"
is consistent with the existing pattern across all modes. Revisiting belongs to a future
DSL-coverage pass.

---

## Public contract

### DSL files (NEW — Task 1a)

**`dsl/learning/error-hunt.logic`** (NEW):
```
# ErrorHunt tap-word reducer decisions (TrainingStore/ErrorHuntGenerator consumers).
# tapIsError, distractorEqualsCorrect, distractorInAccepted are native boolean facts.
# missCount is accumulated natively; 2-miss reveal threshold is encoded here.
DECISION tapOutcome DEFAULT miss
  hit AT 10 WHEN hunt.tapIsError -> hit
END

DECISION shouldReveal DEFAULT hidden
  revealedAfterMisses AT 10 WHEN hunt.missCount >= 2 AND NOT hunt.found -> revealed
  revealedFoundByUser AT 20 WHEN hunt.found -> revealed
END

DECISION scoreClass DEFAULT wrong_form
  perfect AT 10 WHEN hunt.found AND hunt.missCount == 0 AND hunt.answerCorrect -> perfect
  foundLate AT 20 WHEN hunt.found AND hunt.missCount > 0 AND hunt.answerCorrect -> found_late
  revealedCorrect AT 30 WHEN NOT hunt.found AND hunt.answerCorrect -> revealed_correct
END

DECISION distractorValid DEFAULT invalid
  valid AT 10 WHEN NOT hunt.distractorEqualsCorrect AND NOT hunt.distractorInAccepted -> valid
END
```

**`dsl/learning/error-hunt.contract`** (NEW):
```
CONTRACT learning.errorHunt
COVERAGE declared-scope
MODULE ../../kotlin/shared/src/commonMain/kotlin/polski/cloze/ErrorHuntSession.kt
FACT hunt.tapIsError bool member=tapIsError
FACT hunt.missCount number min=0 max=2147483647 integer member=missCount
FACT hunt.found bool member=found
FACT hunt.answerCorrect bool member=answerCorrect
FACT hunt.distractorEqualsCorrect bool member=distractorEqualsCorrect
FACT hunt.distractorInAccepted bool member=distractorInAccepted
INPUT tapOutcome hunt.tapIsError
INPUT shouldReveal hunt.missCount hunt.found
INPUT scoreClass hunt.found hunt.missCount hunt.answerCorrect
INPUT distractorValid hunt.distractorEqualsCorrect hunt.distractorInAccepted
```

**`dsl/learning/error-hunt.examples`** (NEW):
```
PROVENANCE "ErrorHunt reducer decisions: tap outcome, reveal threshold (2 misses), score class and distractor validity."
EXAMPLES tapOutcome
  id | tapIsError -> expected
  tap-error-word | true -> hit
  tap-correct-word | false -> miss
END

EXAMPLES shouldReveal
  id | missCount | found -> expected
  zero-misses-not-found | 0 | false -> hidden
  one-miss-not-found | 1 | false -> hidden
  two-misses-not-found | 2 | false -> revealed
  three-misses | 3 | false -> revealed
  found-zero-misses | 0 | true -> revealed
  found-one-miss | 1 | true -> revealed
END

EXAMPLES scoreClass
  id | found | missCount | answerCorrect -> expected
  perfect | true | 0 | true -> perfect
  found-late-one | true | 1 | true -> found_late
  found-late-two | true | 2 | true -> found_late
  revealed-correct | false | 2 | true -> revealed_correct
  wrong-form-found | true | 0 | false -> wrong_form
  wrong-form-revealed | false | 2 | false -> wrong_form
END

EXAMPLES distractorValid
  id | distractorEqualsCorrect | distractorInAccepted -> expected
  valid-different-form | false | false -> valid
  equals-correct | true | false -> invalid
  in-accepted | false | true -> invalid
  both-true | true | true -> invalid
END
```

**`dsl/biome.project` additions** (new GENERATE lines following the tile-reveal pattern):
```
GENERATE learning-error-hunt logic generate --source {dir}/learning/error-hunt.logic --contract {dir}/learning/error-hunt.contract --target "kt {dir}/../kotlin/shared/src/commonMain/generated/kotlin/polski/cloze/policy/ErrorHuntPolicy.kt AS polski.cloze.policy"
GENERATE learning-error-hunt-examples logic examples --source {dir}/learning/error-hunt.examples --logic {dir}/learning/error-hunt.logic --contract {dir}/learning/error-hunt.contract --out {dir}/learning/generated/error-hunt-examples.json
```

**`dsl/learning/error-hunt-outcomes.core-data`** (NEW — optional, only needed if the host needs
outcome metadata rows; not required for Phase 1 since score buckets are enumerated inline):
Phase 1 — omitted. The four score-class string values are sufficient.

---

### `ErrorHuntExercise` (NEW — Task 1b)

```kotlin
// kotlin/shared/src/commonMain/kotlin/polski/cloze/ErrorHuntExercise.kt
package polski.cloze

data class ErrorHuntExercise(
    val id: String,
    val before: String,       // sentence text before the error word
    val errorForm: String,    // wrong form substituted into the sentence
    val after: String,        // sentence text after the error word
    val correctForm: String,
    val options: List<String>, // exactly 3, shuffled; correctForm is in options
    val gapLexemeKey: String,
    val skillId: String,
    val ruleText: String?,    // from SKILL.theory; null = no explanation available
) {
    init {
        require(correctForm in options) { "correctForm must be in options for $id" }
        require(options.size == 3) { "options must have exactly 3 elements for $id" }
        require(options.distinct().size == 3) { "all options must be distinct for $id" }
        require(errorForm != correctForm) { "errorForm must differ from correctForm for $id" }
        require(errorForm !in before && errorForm !in after) {
            "errorForm must not appear in before/after (position ambiguity) for $id"
        }
    }
}
```

### `extractSlot` internal helper (NEW — Task 1b, DRY refactor of ClozeGenerator)

```kotlin
// kotlin/shared/src/commonMain/kotlin/polski/cloze/SlotExtraction.kt
package polski.cloze

internal data class SlotInfo(
    val before: String,
    val correctForm: String,
    val after: String,
    val lexemeKey: String,
    val skillId: String,
)

/** Shared by ClozeGenerator and ErrorHuntGenerator.
 *  Returns null when: tilePhrase absent, tileTargetSlots empty, slot span not found,
 *  phrase text not in exercise.expected, or lexeme unknown. */
internal fun extractSlot(
    exercise: polski.core.model.CoreExercise,
    morphology: polski.core.engine.MorphologyParadigm,
): SlotInfo?
```

`ClozeGenerator.fromCoreExercise` is refactored to call `extractSlot()`.

### `ErrorHuntGenerator` (NEW — Task 1b)

```kotlin
// kotlin/shared/src/commonMain/kotlin/polski/cloze/ErrorHuntGenerator.kt
package polski.cloze

import polski.cloze.policy.ErrorHuntPolicy

class ErrorHuntGenerator(
    private val morphology: MorphologyParadigm,
    private val random: RandomSource,
    private val theoryFor: (skillId: String) -> String?,
) {
    /** Returns null when: extractSlot returns null, or no distractor passes
     *  ErrorHuntPolicy.distractorValid() after normalization checks (D8),
     *  or the valid distractor text appears in before/after (position ambiguity).
     *  Guarantees errorForm != correctForm (enforced by pickDistractors). */
    fun fromCoreExercise(exercise: CoreExercise): ErrorHuntExercise?
}
```

### `ErrorHuntPool` (NEW — Task 1b)

```kotlin
// kotlin/shared/src/commonMain/kotlin/polski/cloze/ErrorHuntPool.kt
package polski.cloze

class ErrorHuntPool(
    courseExercises: List<CoreExercise>,
    generator: ErrorHuntGenerator,
) {
    private val candidates: List<ErrorHuntExercise> by lazy { ... }
    fun draw(limit: Int, random: RandomSource): List<ErrorHuntExercise>
}
```

### `ErrorHuntSession` (NEW — Task 1c)

```kotlin
// kotlin/shared/src/commonMain/kotlin/polski/cloze/ErrorHuntSession.kt
package polski.cloze

import polski.cloze.policy.ErrorHuntPolicy

data class ErrorHuntSessionState(
    val exercises: List<ErrorHuntExercise>,
    val currentIndex: Int = 0,
    val missCounts: Map<String, Int> = emptyMap(),  // exerciseId -> wrong-word tap count
    val found: Set<String> = emptySet(),             // exerciseIds where error token was tapped
    val answers: Map<String, String> = emptyMap(),   // exerciseId -> chosen form
) {
    /** Calls ErrorHuntPolicy.shouldReveal to decide highlight visibility (D9). */
    fun shouldReveal(id: String): Boolean {
        val missCount = missCounts[id] ?: 0
        val found = id in found
        return ErrorHuntPolicy.shouldReveal(
            ErrorHuntPolicy.ShouldRevealInput(missCount = missCount, found = found)
        ) == ErrorHuntPolicy.ShouldRevealValues.Revealed
    }
}

sealed interface ErrorHuntAction {
    /** [tappedForm] is the token text with trailing punctuation stripped.
     *  Reducer computes tapIsError natively, then delegates verdict to ErrorHuntPolicy. */
    data class TapWord(val id: String, val tappedForm: String) : ErrorHuntAction
    data class Choose(val id: String, val form: String) : ErrorHuntAction
    data object Next : ErrorHuntAction
    data object Previous : ErrorHuntAction
    data object Restart : ErrorHuntAction
}

/** Pure reducer — no side effects. Native facts are computed inline; policy yields verdict;
 *  reducer applies mutation (P12). */
fun reduceErrorHuntSession(
    state: ErrorHuntSessionState,
    action: ErrorHuntAction,
): ErrorHuntSessionState = when (action) {
    is ErrorHuntAction.TapWord -> {
        val ex = state.exercises.firstOrNull { it.id == action.id } ?: return state
        if (state.answers.containsKey(action.id)) return state
        // Native fact: normalized token equality (mechanical, not a verdict)
        val tapIsError = normalize(action.tappedForm.trimEnd('.', ',', '!', '?', ';', ':')) ==
                         normalize(ex.errorForm)
        when (ErrorHuntPolicy.tapOutcome(ErrorHuntPolicy.TapOutcomeInput(tapIsError = tapIsError))) {
            ErrorHuntPolicy.TapOutcomeValues.Hit ->
                if (action.id in state.found) state
                else state.copy(found = state.found + action.id)
            else ->  // miss
                if (action.id in state.found) state  // already found, ignore
                else state.copy(missCounts = state.missCounts +
                    (action.id to ((state.missCounts[action.id] ?: 0) + 1)))
        }
    }
    is ErrorHuntAction.Choose ->
        if (state.answers.containsKey(action.id)) state
        else state.copy(answers = state.answers + (action.id to action.form))
    ErrorHuntAction.Next ->
        if (state.currentIndex >= state.exercises.lastIndex) state
        else state.copy(currentIndex = state.currentIndex + 1)
    ErrorHuntAction.Previous ->
        if (state.currentIndex <= 0) state
        else state.copy(currentIndex = state.currentIndex - 1)
    ErrorHuntAction.Restart ->
        state.copy(currentIndex = 0, missCounts = emptyMap(),
                   found = emptySet(), answers = emptyMap())
}
```

Note: `normalize` is imported from `polski.grammar`; if that creates a dependency issue in
`:shared`, the reducer delegates to a native helper `ErrorHuntNormalizer.sameNormalized(a, b)`.

### `AppUiState` additions (Task 1c)

```kotlin
// In AppUiState.kt (commonMain)
enum class ClozeSubMode { Fill, ErrorHunt }

// Add fields to AppUiState:
val clozeSubMode: ClozeSubMode = ClozeSubMode.Fill,
val errorHunt: ErrorHuntSessionState? = null,
```

### `AppAction` additions (Task 1c)

```kotlin
// In AppAction.kt:
data class SetClozeSubMode(val mode: ClozeSubMode) : AppAction
data object StartErrorHunt : AppAction
data class ErrorHuntAct(val action: ErrorHuntAction) : AppAction
```

### `TrainingStore` additions (Task 1c)

```kotlin
private val errorHuntPool: ErrorHuntPool by lazy { ... }

private fun startErrorHunt() {
    val exercises = errorHuntPool.draw(ERROR_HUNT_SESSION_SIZE, tileRandom)
    mutate { it.copy(errorHunt = ErrorHuntSessionState(exercises)) }
}
// SetClozeSubMode: sets clozeSubMode, auto-starts the new sub-mode session if null
// ErrorHuntAct: calls reduceErrorHuntSession, mutates errorHunt
```

---

## Components and dependencies

| File | Status | Responsibility |
|---|---|---|
| `dsl/learning/error-hunt.logic` | NEW | 4 DECISION verdicts: tap outcome, reveal, score class, distractor validity |
| `dsl/learning/error-hunt.contract` | NEW | Fact types + which inputs each decision reads |
| `dsl/learning/error-hunt.examples` | NEW | Independent expectations (P15) for all 4 decisions |
| `dsl/biome.project` | MODIFY | 2 new GENERATE lines (logic generate + logic examples) |
| `kotlin/shared/…/generated/…/policy/ErrorHuntPolicy.kt` | GENERATED | Policy object — never hand-edited |
| `polski/cloze/ErrorHuntExercise.kt` | NEW | Domain model, init invariants |
| `polski/cloze/SlotExtraction.kt` | NEW | `extractSlot()` internal helper (DRY refactor) |
| `polski/cloze/ErrorHuntGenerator.kt` | NEW | Builds `ErrorHuntExercise`; calls `ErrorHuntPolicy.distractorValid` |
| `polski/cloze/ErrorHuntPool.kt` | NEW | Lazy candidate pool + draw |
| `polski/cloze/ErrorHuntSession.kt` | NEW | Reducer + actions; calls `ErrorHuntPolicy.tapOutcome` / `shouldReveal` / `scoreClass` |
| `polski/cloze/ClozeGenerator.kt` | MODIFY | Refactor `fromCoreExercise` to call `extractSlot()` |
| `polski/presentation/AppUiState.kt` | MODIFY | Add `ClozeSubMode`, `clozeSubMode`, `errorHunt` |
| `polski/presentation/AppAction.kt` | MODIFY | Add `SetClozeSubMode`, `StartErrorHunt`, `ErrorHuntAct` |
| `polski/presentation/TrainingStore.kt` | MODIFY | `errorHuntPool`, `startErrorHunt()`, new action handlers |
| `polski/ui/ErrorHuntWeb.kt` | NEW (webMain) | `ErrorHuntWebController` + `renderErrorHuntWeb()` |
| `polski/ui/ClozeWeb.kt` | MODIFY (webMain) | Add sub-mode switcher; delegate to `ErrorHuntWeb` |
| `polski/ui/screens/AndroidClozeScreen.kt` | NEW (androidMain) | Cloze tab with sub-mode switcher (Task 3) |
| `AndroidContent.kt` | MODIFY (androidMain) | Route `AppTab.Cloze` to `AndroidClozeScreen` (Task 3) |
| `AppNavigationBar.kt` | MODIFY (androidMain) | Add Cloze to nav (Task 3) |
| `polski/ios/IosSnapshot.kt` | MODIFY (iosMain) | Add `errorHuntSnapshot()` (Task 4) |
| `polski/ios/IosSession.kt` | MODIFY (iosMain) | Add `startErrorHunt()`, `tapErrorHuntWord(id, form)`, `chooseErrorHunt(id, form)`, `errorHuntNext/Prev/Restart()` (Task 4) |
| `courses-biome/pl-ru/uiCopy.core-data` | MODIFY | Add `errorHunt`, `errorHuntPrompt` |
| `courses-biome/en-ru/uiCopy.core-data` | MODIFY | Same keys, English text |
| `dsl/ui-copy.declare` | MODIFY | Add `KEY errorHunt`, `KEY errorHuntPrompt` |

Dependency direction: `ErrorHuntGenerator` → `SlotExtraction` ← `ClozeGenerator`. Both call
`ErrorHuntPolicy` (generated) for verdicts. `TrainingStore` assembles all above.
Host UI files depend only on `ErrorHuntSession` + `AppAction`.

---

## State and resources

`ErrorHuntSessionState` has the same lifetime class as `ClozeSessionState` — session-only,
never persisted. `errorHuntPool` is `lazy` on `TrainingStore`. `ErrorHuntPool.candidates` is
lazy — computed once at first draw. No coroutines, no async.

Switching sub-mode via `SetClozeSubMode` does not clear the other sub-mode's session; both
`cloze` and `errorHunt` coexist in `AppUiState`. Restart is explicit via `Restart` actions.

---

## DSL contract changes

### New learning policy files

Add `dsl/learning/error-hunt.logic`, `error-hunt.contract`, `error-hunt.examples` (content above).
Add two GENERATE lines in `dsl/biome.project` (following the existing tile-reveal block).

### UI copy keys

```
# courses-biome/pl-ru/uiCopy.core-data
UI_COPY {key: errorHunt text: "Найди ошибку"}
UI_COPY {key: errorHuntPrompt text: "Нажми на ошибочное слово"}
```
```
# courses-biome/en-ru/uiCopy.core-data
UI_COPY {key: errorHunt text: "Find the Error"}
UI_COPY {key: errorHuntPrompt text: "Tap the wrong word"}
```
```
# dsl/ui-copy.declare (before END)
  KEY errorHunt
  KEY errorHuntPrompt
```

After editing: `biome fmt` → `biome generate --project dsl/biome.project` → verify
`ErrorHuntPolicy.kt` and `UiCopy.kt` are regenerated. `biome generate --check` CI gate
catches drift (P16).

---

## Principles compliance

Sources: PRINCIPLES.md, ARCHITECTURE.md §3, BestPractice.md §1/§4/§8,
ProjectMigratioPrincipals.md "Куда помещать намерение".

| Principle | Compliance | Native remainder class |
|---|---|---|
| **P4** (self-hosting is a means) | Business logic in DSL + generated Kotlin policy; rendering in platform hosts. `theoryFor` injection keeps generator pack-agnostic without a hand-written PROGRAM. | — |
| **P7** (one fact — one place) | `extractSlot()` shared between `ClozeGenerator` and `ErrorHuntGenerator`. UI labels in `uiCopy.core-data` only. `normalize()` reused from `AnswerNormalizer.kt`. | — |
| **P10** (rule in its layer) | Verdicts (tap outcome, reveal, score class, distractor validity) in `error-hunt.logic`. UI labels in `uiCopy.core-data`. Tokenization + normalized equality → native helpers (mechanical, not verdicts). **Acknowledged remainder**: `ERROR_HUNT_FEEDBACK_DELAY_MS = 1_500` and `ERROR_HUNT_SESSION_SIZE = 10` follow the existing Kotlin-constant pattern for `CLOZE_FEEDBACK_DELAY_MS` / `startCloze()` — host-timing and session-size values not yet in DSL across any mode. | Timing + session size: existing tech debt pattern, not new divergence |
| **P12** (model owns mutations) | DSL DECISION yields outcome string; Kotlin reducer decides which map/set to update. Policy never mutates `ErrorHuntSessionState`. | — |
| **P15** (independent expectations) | `error-hunt.examples` holds independent expected outputs for all 4 decisions; changed only when the requirement changes, never inferred from generated output. | — |
| **P16** (generated code not hand-edited) | `ErrorHuntPolicy.kt` and `UiCopy.kt` are generated; `biome generate --check` gate enforces freshness. | — |
| **P17** (proof = execution) | Gates state PASS/FAIL/NOT RUN explicitly. `biome generate --check` and Kotlin unit tests provide evidence. | — |
| **P22** (file ≤ 400 non-empty lines) | Each new file is a single-responsibility slice (< 100 lines for DSL files; < 150 lines for generator/session/pool). | — |
| **P25** (self-host boundary by measurement) | Generator, pool, reducer stay native Kotlin: 1 consumer (polski), algorithmic distractor-picking logic, no multi-host benefit from DSL PROGRAM. No V1–V5 benefit for PROGRAM; no S1–S6 stop-signals. | Mechanical computation: measured-better native |

---

## Verification

### Acceptance cases by task

**AC-EH-01** (Task 1a — DSL policy): `biome generate --check` passes after adding logic/contract/
examples. `dsl/learning/generated/error-hunt-examples.json` is consistent with
`error-hunt.logic`. All 4 decision tables executed against their EXAMPLES rows pass.

**AC-EH-02** (Task 1b — generator): `ErrorHuntGeneratorTest` (native Kotlin test; justified
because it exercises randomness + real course data integration that `.examples` cannot express) —
- `fromCoreExercise` returns null when `tileTargetSlots` is empty.
- Returns non-null with `errorForm != correctForm`.
- `options` contains exactly 3 distinct values including `correctForm`.
- Distractor equal to `normalize(exercise.expected)` is excluded (D8 guard).
- Distractor equal to any `normalize(exercise.accepted)` is excluded (D8 guard).
- `fromCoreExercise` returns null when no valid distractor survives the guard.

**AC-EH-03** (Task 1b — DRY refactor): `ClozeGeneratorTest` (existing) must still pass after
`extractSlot()` extraction — no behavioral change to `fromCoreExercise`.

**AC-EH-04** (Task 1c — session reducer): `ErrorHuntSessionTest` (native Kotlin test; justified
because it tests integration with generated `ErrorHuntPolicy` and mutation semantics across
sequential actions) —
- `TapWord` with wrong form increments `missCounts`; same id again increments again.
- `TapWord` with error form adds to `found`; second tap on same id is idempotent.
- `TapWord` on any form after `answered` is set is idempotent.
- After 2 wrong taps, `shouldReveal(id)` returns true (policy threshold reached).
- `Choose` on already-answered id is idempotent.
- `Restart` zeroes index + missCounts + found + answers.

**AC-EH-05** (Task 1c — DSL copy): `biome generate --check` passes after adding the two
`uiCopy.core-data` entries and `ui-copy.declare` KEY lines.

**AC-EH-06** (Task 2 — web): Playwright spec `tests/browser/kotlin-error-hunt.spec.ts`
following the shape of `kotlin-cloze.spec.ts`. Justification: Error Hunt lives in the Cloze
tab, not an exam session — there is no `.playtest` host for tab-level modes (Cloze has none
either). The DOM-based Playwright test is the appropriate acceptance vehicle here, identical
to the established precedent.

Scenario assertions:
1. Cloze tab; mode switcher shows "Подстановка" / "Найди ошибку".
2. Switch to "Найди ошибку"; sentence appears with ALL words as tappable buttons; no word
   is pre-highlighted.
3. Tap a non-error word → "это слово верное" feedback appears.
4. Tap the error word → 3 option buttons appear below sentence.
5. Click the correct option → feedback + rule text; auto-advance fires after 1.5 s.
6. Click a wrong option → "Неверно — правильный ответ: X".
7. After 2 wrong-word taps the error token is visually highlighted (class-based).
8. Last item → summary panel shows bucket counts; "Заново" restarts.

Spec must be registered in `playwright.kotlin.config.ts` `testMatch` before the first run
(ADR-63 lesson: specs without registration are silently skipped).

**AC-EH-07** (Task 2 — web): Playwright — switching to "Подстановка" sub-mode renders a Cloze
exercise (with `___` gap), not an Error Hunt sentence.

**AC-EH-08** (Task 3 — Android): Android nav shows Cloze tab; opening it shows mode switcher;
Error Hunt exercises render; all words tappable; correct/wrong feedback visible (emulator-5554).

**AC-EH-09** (Task 5 — E2E): Full scenario on web (Playwright) — fresh profile, navigate Cloze
tab, switch to Error Hunt, complete 3 exercises (1 with misses → Found Late bucket, 2 Perfect),
verify summary shows the correct bucket counts. One native host (Android or iOS) passes the
same manual walkthrough.

---

## Developer tasks

**Task 1a — DSL policy** (~100 lines of DSL + biome registration)

Files: `dsl/learning/error-hunt.logic`, `error-hunt.contract`, `error-hunt.examples`; 2 new
GENERATE lines in `dsl/biome.project`.
Gate: AC-EH-01. `biome generate --check` passes. Generated `ErrorHuntPolicy.kt` exists.

**Task 1b — Generator + pool** (~150 lines Kotlin)

Files: `SlotExtraction.kt` (NEW, refactor `ClozeGenerator.kt`), `ErrorHuntExercise.kt` (NEW),
`ErrorHuntGenerator.kt` (NEW), `ErrorHuntPool.kt` (NEW).
Tests: `ErrorHuntGeneratorTest.kt`.
Gate: AC-EH-02, AC-EH-03.

**Task 1c — Reducer + wiring + UI copy** (~150 lines Kotlin + DSL copy keys)

Files: `ErrorHuntSession.kt` (NEW), `AppUiState.kt` (MODIFY), `AppAction.kt` (MODIFY),
`TrainingStore.kt` (MODIFY); DSL `uiCopy.core-data` (both packs) + `ui-copy.declare`.
Tests: `ErrorHuntSessionTest.kt`.
Gate: AC-EH-04, AC-EH-05. `./gradlew :shared:desktopTest` green.

**Task 2 — Web UI** (web host, ~200 lines)

Files: `ErrorHuntWeb.kt` (NEW), `ClozeWeb.kt` (MODIFY — sub-mode switcher).
Playwright spec: `tests/browser/kotlin-error-hunt.spec.ts`.
Gate: AC-EH-06, AC-EH-07. `:composeApp:composeCompatibilityBrowserDistribution` builds.

**Task 3 — Android UI** (Android host)

Files: `AndroidClozeScreen.kt` (NEW), modify `AndroidContent.kt` + `AppNavigationBar.kt`.
Gate: AC-EH-08. `:androidApp:testDebugUnitTest` green, live emulator walkthrough.

**Task 4 — iOS bridge + SwiftUI** (iOS host)

Files: `IosSnapshot.kt` (add `errorHuntSnapshot()`), `IosSession.kt` (add dispatch methods),
`iosApp` SwiftUI views.
Gate: `:shared:iosSimulatorArm64Test` green, `xcodebuild test` on simulator passes.

**Task 5 — E2E acceptance** (cross-host gate)

Playwright E2E full scenario (AC-EH-09) on web. Manual walkthrough on one native host.
Gate closes after AC-EH-09.

---

## Open questions

**OQ-1 (product)**: Should switching back to "Подстановка" restart that session, or resume
where the user left off? Default: resume (session stays in `AppUiState.cloze` across
sub-mode switches).

**OQ-2 (product)**: Summary screen after last exercise (like AudioCloze) or auto-loop (like
Cloze)? Default: show summary with bucket counts + "Заново".

**OQ-3 (content)**: For en-ru, if course exercises lack `tileTargetSlots`, the pool is empty.
The mode degrades to "No exercises available" with an explicit message. Accept or add
`tileTargetSlots` to en-ru exercises as a parallel task?

---

## Documentation and handoff

- Developer (Task 1a) adds the GENERATE lines to `biome.project` and verifies `ErrorHuntPolicy.kt`
  is generated correctly before Task 1b depends on it.
- Developer (Task 1b) records the `extractSlot()` extraction in `AI/decisions.md` (new ADR).
- Developer (Task 2) registers `kotlin-error-hunt.spec.ts` in `playwright.kotlin.config.ts`
  `testMatch` before the first Playwright run (ADR-63 lesson).
- Each host task checks `Plans/Kotlin/iOS.md` / `Plans/Kotlin/Android.md` for active lane gates.
