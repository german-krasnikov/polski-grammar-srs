# Module design: Cloze (Fill-the-Gap) Exercise Mode

## Scope and consumers

A practice mode presenting a Polish sentence with one word hidden as `___` and three form choices (one correct, two distractors). Two sentence sources:

- Exam grammar items (`QuestionType.ChooseForm`) — distractors already authored
- Course training exercises (`CoreExercise` with `tileTargetSlots.isNotEmpty()`) — distractors generated from `MorphologyParadigm`

Target hosts: Web (Kotlin/JS), Android, Desktop/macOS. All three consume the same session state from `:shared`. iOS is out of scope here; the model is compatible with future addition.

Source sets: `shared/commonMain` for session logic and generator; `composeApp` per-platform source sets for host UI.

---

## Public contract

### ClozeExercise — NEW

`kotlin/shared/src/commonMain/kotlin/polski/cloze/ClozeExercise.kt`

```kotlin
data class ClozeExercise(
    val id: String,
    val displaySentence: String,  // full sentence with "___" for the gap
    val correctForm: String,
    val options: List<String>,    // length == 3, shuffled; includes correctForm
    val source: ClozeSource,
    val gapLexemeKey: String,     // e.g. "noun:wife" — for analytics/debug
)

sealed interface ClozeSource {
    data class ExamItem(val itemId: String) : ClozeSource
    data class CourseExercise(val exerciseId: String, val skillId: String) : ClozeSource
}
```

Invariant: `correctForm in options`, `options.size == 3`, all options distinct (enforced in `init`).

---

### ClozeSession — NEW

`kotlin/shared/src/commonMain/kotlin/polski/cloze/ClozeSession.kt`

```kotlin
data class ClozeSessionState(
    val exercises: List<ClozeExercise>,
    val currentIndex: Int = 0,
    val answers: Map<String, String> = emptyMap(),  // exerciseId -> chosen form
)

sealed interface ClozeSessionAction {
    data class Choose(val id: String, val form: String) : ClozeSessionAction
    data object Next : ClozeSessionAction
    data object Previous : ClozeSessionAction
    data object Restart : ClozeSessionAction
}

fun reduceClozeSession(state: ClozeSessionState, action: ClozeSessionAction): ClozeSessionState
```

`Choose` before the gap is answered records the answer; subsequent Choose on the same id is ignored (like exam commit semantics). `Next`/`Previous` guard against out-of-bounds. `Restart` zeroes `currentIndex` and `answers`.

---

### DistractorPicker — NEW

`kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/DistractorPicker.kt`

```kotlin
/**
 * Pure: pick [count] distinct forms from [paradigm] for [lexemeKey] that differ from [correctForm].
 * Returns fewer than [count] when the paradigm has fewer candidates (syncretism / small paradigm).
 * Never returns [correctForm].
 */
fun pickDistractors(
    paradigm: MorphologyParadigm,
    lexemeKey: String,
    correctForm: String,
    count: Int,
    random: RandomSource,
): List<String>
```

`TileGenerator.distractorsFor()` (existing, `core-engine`) delegates to this function — same behavior, no API change. `ClozeGenerator` also calls it. This is the single source of truth for the syncretism-safety filter (`it != correctForm` after dedup).

---

### ClozeGenerator — NEW

`kotlin/shared/src/commonMain/kotlin/polski/cloze/ClozeGenerator.kt`

```kotlin
class ClozeGenerator(
    private val morphology: MorphologyParadigm,
    private val random: RandomSource,
) {
    /**
     * Returns null when item.questionType != ChooseForm or item.options.size < 3.
     * Uses authored options directly — no morphology lookup needed.
     */
    fun fromExamItem(item: GrammarItem): ClozeExercise?

    /**
     * Returns null when exercise.tileTargetSlots is empty or no eligible slot yields 2+
     * distractors from the paradigm.
     * Builds displaySentence by replacing the slot's surface span in exercise.expected with "___".
     */
    fun fromCoreExercise(exercise: CoreExercise): ClozeExercise?
}
```

`fromCoreExercise` selects the first `tileTargetSlots` entry that has a corresponding key in `exercise.slots`, derives `lexemeKey = "${category}:${lexeme}"` matching the format `TileGenerator` uses, calls `pickDistractors()`, shuffles the three options, and replaces the span in `exercise.expected` with `___` using the span range stored in `exercise.tilePhrase.slotSpans`.

Returns null instead of throwing — same fail-soft contract as `TileGenerator`.

---

### ClozePool — NEW

`kotlin/shared/src/commonMain/kotlin/polski/cloze/ClozePool.kt`

```kotlin
fun interface ClozePool {
    fun draw(limit: Int, random: RandomSource): List<ClozeExercise>
}
```

Default production implementation (`DefaultClozePool`) is constructed in `TrainingStore` (or its init block) and draws from:

1. All `QuestionType.ChooseForm` items across `examGrammarData` (the same map `ExamGrammarController` reads) — via `ClozeGenerator.fromExamItem()`
2. All `CoreExercise`s with `tileTargetSlots.isNotEmpty()` from the existing exercise bank — via `ClozeGenerator.fromCoreExercise()`

Candidates from both sources are computed once at construction; `draw()` shuffles and limits to `limit` (default 10). No network or storage I/O.

---

### AppUiState / AppAction integration — EXISTING files modified

`AppUiState.kt`:
- `enum class AppTab { Training, Vocabulary, Matrix, Progress, Cloze }` — `Cloze` appended
- `val cloze: ClozeSessionState? = null` — new field; null outside the Cloze tab

`AppAction.kt`:
```kotlin
data object StartCloze : AppAction
data class ClozeAct(val action: ClozeSessionAction) : AppAction
```

`TrainingStore.kt`:
- `StartCloze` → calls `clozePool.draw(10, random)`, builds `ClozeSessionState`, sets `tab = AppTab.Cloze`
- `ClozeAct` → `reduceClozeSession(state.cloze!!, action)`, updates `AppUiState.cloze`

---

### Web UI — NEW

`kotlin/composeApp/src/webMain/kotlin/polski/ui/ClozeWeb.kt`

DOM controller identical in shape to `ExamGrammarController`:

- `render(container: HTMLElement)` / `close()` lifecycle
- `renderQuestion()`: renders sentence with `<span class="exam-gap">___</span>` + three `<button class="exam-option">` elements
- `updateInPlace()`: applies `feedback-correct` / `feedback-wrong` CSS classes after a choice
- Immediate feedback mode: highlight for 1 500 ms, then auto-advance

Wired in the existing web router wherever `AppTab.Cloze` is active.

---

### Android UI — NEW

`kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidClozeScreen.kt`

Compose screen. `AndroidContent.kt` gains:
```kotlin
state.tab == AppTab.Cloze -> AndroidClozeScreen(state, dispatch)
```

`tabNavOrder` in `AndroidContent.kt` updated to include `AppTab.Cloze` (rightmost position, or between Progress and Vocabulary — TBD by designer).

---

### Desktop UI — NEW

`kotlin/composeApp/src/desktopMain/kotlin/polski/ui/screens/ClozeScreen.kt`

Compose screen wired in `Main.kt`'s content switch on `AppTab.Cloze`.

---

## State and resources

- `ClozeSessionState` is immutable; `TrainingStore` owns it on `AppUiState.cloze`
- Session is not persisted — same ephemeral lifetime as `DailySessionState` and `RetryTicket`
- `DefaultClozePool` candidate list is computed once at app init; `draw()` is stateless beyond that
- `RandomSource` is injected; deterministic in tests

---

## Decisions and tradeoffs

**Gap selection from CoreExercise — first eligible tileTargetSlot only.**
Matches existing `TileGenerator` behavior and needs no extra authored metadata. Multi-gap cloze is explicitly out of scope here.

**Authored distractors for exam items.**
`GrammarItem.options` are human-reviewed and contextually appropriate. Morphology-derived distractors are fallback for course exercises only. If `item.options.size < 3` the item is skipped (edge case: some exam items have only 2 options).

**DistractorPicker extracted to `core-engine`.**
Prevents duplication between `TileGenerator` and `ClozeGenerator`. `TileGenerator.distractorsFor()` becomes a thin delegate — existing `TileGeneratorTest` fixtures confirm behavior is unchanged.

**No new Gradle module.**
`polski/cloze/` lives in `:shared/commonMain`. Adding a module for one domain package introduces build overhead with no dependency isolation benefit — `cloze` already depends on `core-engine` transitively through `:shared`.

**AppTab enum extension is backward-safe.**
No exhaustive `when` over `AppTab` exists in the codebase (confirmed by grep); all tab dispatch is `state.tab == AppTab.X` guards. Only `tabNavOrder` list additions are required.

**No SRS rating in this mode.**
Cloze is a fluency drill, not a scheduled review. No attempt events are written. Confirm with owner before adding scheduling.

---

## Verification

| # | Behavior | Test location |
|---|---|---|
| AC1 | `fromExamItem()` returns null for non-`ChooseForm` item | `shared/commonTest` |
| AC2 | `fromExamItem()` with 3 options: `options` matches `item.options` shuffled, `correctForm == item.correctAnswer` | `shared/commonTest` |
| AC3 | `fromCoreExercise()` returns null when `tileTargetSlots` is empty | `shared/commonTest` |
| AC4 | `fromCoreExercise()` with noun slot: `options.size == 3`, `correctForm in options`, all options distinct | `shared/commonTest` |
| AC5 | `fromCoreExercise()` returns null when paradigm yields < 2 distractors for the chosen slot | `shared/commonTest` |
| AC6 | `pickDistractors()` never includes `correctForm`; handles syncretism (multiple forms with identical surface text) | `core-engine/commonTest` |
| AC7 | `pickDistractors()` returns `candidates.size` forms when candidates < count (no crash, no padding) | `core-engine/commonTest` |
| AC8 | `reduceClozeSession(Choose)` before answer: records it; second Choose on same id is ignored | `shared/commonTest` |
| AC9 | `reduceClozeSession(Next)` at last index is a no-op; Previous at index 0 is a no-op | `shared/commonTest` |
| AC10 | `TileGeneratorTest` passes unchanged after delegation to `pickDistractors` | `core-engine/commonTest` (existing) |
| AC11 | Web: choosing correct option adds `feedback-correct`; wrong option adds `feedback-wrong` | Playwright / manual walk |
| AC12 | Android: `AppTab.Cloze` renders `AndroidClozeScreen`; tabNavOrder slide direction is correct | Android compose test / screenshot |

---

## Documentation and handoff

**Implementation order:**

1. `DistractorPicker.kt` in `core-engine` → update `TileGenerator.distractorsFor()` to delegate → verify AC6/AC7/AC10.
2. `ClozeExercise.kt`, `ClozeSession.kt`, `ClozeGenerator.kt`, `ClozePool.kt` in `shared/commonMain/kotlin/polski/cloze/` → verify AC1–AC9.
3. Extend `AppUiState`, `AppAction`; wire `StartCloze`/`ClozeAct` in `TrainingStore`.
4. Web: `ClozeWeb.kt` + router wiring → verify AC11.
5. Android: `AndroidClozeScreen.kt` + `AndroidContent` branch + `tabNavOrder` → verify AC12.
6. Desktop: `ClozeScreen.kt` + `Main.kt` branch.

**Open questions:**

- Pool size (10 items): hardcoded for now. Author a `.core-data` config field if the number needs to vary by level.
- `tabNavOrder` position for `AppTab.Cloze` on Android: leftmost, rightmost, or between existing tabs? Needs UX decision.
- iOS: session model compatible; screen implementation deferred.
- Progress persistence: no SRS rating written. Confirm before adding attempt events.
