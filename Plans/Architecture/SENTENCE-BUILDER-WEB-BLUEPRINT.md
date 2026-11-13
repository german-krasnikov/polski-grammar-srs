# Sentence Builder: Web UI blueprint (SB-6)

Grounds `SENTENCE-BUILDER-DESIGN.md` §8/§11 (SB-6: "Web UI: tap-to-place + form picker + error
highlight", depends on SB-4 only — **not** SB-7/SB-8). Read alongside `SENTENCE-BUILDER-BLUEPRINT.md`
(SB-1..5, core, already shipped per `e75b1f1`).

## 0. Gap this closes

`AnswerMode.TileBuilder`, `TileBuilderState`/`TileAction` and `tileBuilderStateFor()` exist in
`:shared` (SB-4), and `evaluateTileAnswer()` exists in `:core-engine` (SB-3). Nothing wires them in:
`AppUiState` has no `tileBuilder` field, `AppAction`/`TrainingStore.dispatch` has no case for
`TileAction`, and no web renderer exists. This blueprint closes that gap for the web host only —
Android/iOS (SB-9/10) reuse the shared-layer wiring below unchanged.

## 1. Shared-layer wiring (prerequisite, not web-specific)

New files: none. Modified: `AppUiState.kt`, `TrainingStore.kt` (both
`kotlin/shared/src/commonMain/kotlin/polski/presentation/`).

### 1.1 `AppUiState`

```kotlin
data class AppUiState(
    // ... existing fields unchanged ...
    val tileBuilder: TileBuilderState? = null,
)
```

`tileBuilder` is **derived from `exercise`**, not a separate selection: every place `TrainingStore`
assigns a new `exercise` (`chooseSkill`, `startChain`/`startSchedule` via `project`/initial state,
`rate()`'s `nextExercise`), it also assigns
`tileBuilder = newExercise?.let { tileBuilderStateFor(it, tileRandom) }`
(`ScaffoldingLevel.Partial` default — SB-8 will make this progress-driven; out of SB-6 scope).
This is unconditional — computed regardless of `answerMode` — because it is cheap, pure, and
`null` exactly when the exercise doesn't support tiles (SB-4's own documented fallback). The UI
uses `state.tileBuilder != null` as the single "is TileBuilder available for this card" check; it
never re-derives eligibility itself (AC4.3 — UI makes no learning decisions).

### 1.2 `TrainingStore`

- New constructor param `tileRandom: RandomSource = RandomSource { kotlin.random.Random.nextDouble() }`
  (default-valued — every existing call site in `TrainingWebApp.kt`/`Main.kt`/
  `AndroidSessionViewModel.kt`/`MacSession.kt`/`IosSession.kt` keeps compiling unchanged). Mirrors
  the `RandomSource { Random.nextDouble() }` pattern each host already builds for `PlExerciseEngine`.
- New `AppAction.Tile(val exerciseId: String, val action: TileAction)` case.
- `dispatch()`: `is AppAction.Tile -> tile(action)`.
- New private `tile(action: AppAction.Tile)`:
  ```kotlin
  private fun tile(action: AppAction.Tile) {
      val current = state.value
      val builder = current.tileBuilder ?: return
      when (val a = action.action) {
          is TileAction.Place -> mutate { it.copy(tileBuilder = builder.copy(
              placed = builder.placed.filterNot { p -> p.position == a.position } + TileSelection(a.position, a.form),
          )) }
          is TileAction.Remove -> mutate { it.copy(tileBuilder = builder.copy(
              placed = builder.placed.filterNot { it.position == a.position },
          )) }
          TileAction.Check -> reveal(action.exerciseId) // delegates — see 1.3, no duplicated freeze/evaluate logic
          is TileAction.TapError -> mutate { it.copy(tileBuilder = builder.copy(
              errorExplanation = errorExplanationFor(current, a.position),
          )) }
      }
  }

  /** Interim bridge until SB-7 wires `StyleComposer.blocksForError` per-position: a generic
   *  skill-level Rule block (theory + this exercise's own `explanation`), not yet the specific
   *  per-error reason (`TileError.reason` is always null out of `evaluateTileAnswer` today —
   *  SB-3's own documented scope). Swapping this one-line body for `blocksForError` is SB-7's
   *  entire remaining task; the UI contract (`errorExplanation: List<Block>?`) does not change. */
  private fun errorExplanationFor(state: AppUiState, position: Int): List<Block> {
      val exercise = state.exercise ?: return emptyList()
      val skill = skillById(exercise.primarySkill)
      return listOf(Block.Rule(skill.theory, exercise.explanation, emptyList()))
  }
  ```
- `accepts()` gains:
  ```kotlin
  is AppAction.Tile -> s.loadStatus == LoadStatus.Ready && s.exercise?.id == action.exerciseId && when (val a = action.action) {
      is TileAction.Place, is TileAction.Remove -> s.phase == CardPhase.Question && s.tileBuilder != null
      TileAction.Check -> s.phase == CardPhase.Question && s.tileBuilder?.let { b ->
          b.exercise.segments.filterIsInstance<TileSegment.Choice>().all { c -> b.placed.any { it.position == c.position } }
      } == true
      is TileAction.TapError -> s.phase == CardPhase.Revealed && s.tileBuilder?.result?.errors?.any { it.position == a.position } == true
  }
  ```
  (Check is gated on completeness here, not just in the UI, so a stray dispatch can never check a
  half-filled answer — same defensive posture `Reveal`/`Rate` already have.)
- `reveal()` extended (parallel to its existing `Typed` branch):
  ```kotlin
  private fun reveal(exerciseId: String) {
      val current = state.value
      val exercise = current.exercise ?: return
      val evaluation = if (current.answerMode == AnswerMode.Typed) evaluate(current.draft, exercise) else null
      val tileResult = current.tileBuilder?.takeIf { current.answerMode == AnswerMode.TileBuilder }
          ?.let { evaluateTileAnswer(it.exercise, it.placed) }
      mutate { it.copy(
          phase = CardPhase.Revealed, frozenAnswer = current.draft, evaluation = evaluation,
          tileBuilder = current.tileBuilder?.copy(result = tileResult),
      ) }
  }
  ```
  `TileAction.Check` reuses this unchanged — no second "freeze the card" code path.
- `rate()`'s correctness argument to `reviewReducer.recordReview` gains a third branch, parallel to
  the existing Typed-only one: `AnswerMode.TileBuilder -> current.tileBuilder?.result?.correct`,
  else (Oral) `null`, exactly like Typed already works. FSRS/progress journal entries are otherwise
  identical — SB-AC8.
- Every place `exercise = nextExercise`/the initial exercise is assigned also assigns the derived
  `tileBuilder` (§1.1). `tileBuilder` is cleared to the newly-derived value, never carried over
  from the previous card.

### 1.3 Why `Check` delegates to `reveal()` instead of its own phase machine

The design doc's `TileAction.Check` and the existing `AppAction.Reveal` both mean "the learner is
done answering, freeze and show the result, run the SRS rating UI next." Giving `TileBuilder` its
own `Revealed`-adjacent phase would duplicate `reveal()`'s freezing logic and `rate()`'s next-card
logic for no behavioral difference. Keeping one `CardPhase` state machine for all three
`AnswerMode`s means `renderAnswerBack`'s existing ratings row, Rive trigger, FSRS write and
chain/schedule advancement are reused completely unchanged for TileBuilder — the only new code is
what differs: the tile row itself and its correctness source.

## 2. Web rendering — new file `TileBuilderWeb.kt`

`kotlin/composeApp/src/webMain/kotlin/polski/ui/TileBuilderWeb.kt` (NEW), same role as
`CardBlocksWeb.kt`/`CardFlip.kt`: a focused helper `TrainingCardDom.kt` calls into, not a
standalone screen.

```kotlin
internal fun renderTileBuilderSection(
    area: HTMLElement,
    exerciseId: String,
    builder: TileBuilderState,
    openPicker: Int?,
    openError: Int?,
    langCode: String,
    onTogglePicker: (Int?) -> Unit,
    onToggleError: (Int?) -> Unit,
    dispatch: (AppAction) -> Unit,
): HTMLElement // returns the "Check" button, so renderAnswerArea can wire its own disabled state
```

### 2.1 Structure (Question phase, `builder.result == null`)

```
<div class="tile-row" aria-label="Собери предложение">
  <!-- one child per TileSegment, in `position` order -->
  <span class="tile tile-locked">Widzę</span>
  <button class="tile tile-choice" aria-expanded="false">moją</button>   <!-- placed -->
  <button class="tile tile-choice tile-empty" aria-expanded="true">___</button> <!-- unplaced, open -->
</div>
<div class="tile-picker collapsible" aria-live="polite">   <!-- only the OPEN Choice's candidates -->
  <button class="tile tile-option">nową</button>
  <button class="tile tile-option">nowej</button>
  <button class="tile tile-option">nowe</button>
</div>
```

- Each `TileSegment.Locked` renders as a plain `<span class="tile tile-locked">`, matching the
  design doc: locked segments are never interactive.
- Each `TileSegment.Choice` renders as a `<button class="tile tile-choice">`: label is the
  currently `placed` form for that position, or `"___"` (+ `.tile-empty`) when unplaced.
  `aria-expanded` reflects `openPicker == segment.position`. Click toggles `onTogglePicker` (close
  if already open, else open — and close any other open picker, since only one inline panel exists
  at a time, keeping exactly one DOM subtree instead of N parallel ones).
- The picker panel (`(correctForm + alternatives).shuffled(tileRandomForUiShuffleOnly)` — **order
  only**, already shuffled once by `TileGenerator`/`fromAnswerPhrase` at generation time per SB-2's
  own algorithm step 5, so the web renderer does **not** reshuffle on every render; it just renders
  `listOf(choice.correctForm) + choice.alternatives` in the exact order the shared state already
  carries, so repeated re-renders of the same open picker never visibly reorder candidates under
  the user's finger) sits directly under the tile row, using the exact `.collapsible` grid-row
  trick `applyCollapsible` already implements (`TrainingCardDom.kt`) — 0fr→1fr, `inert`/
  `aria-hidden` synced, same `transition`/`will-change` convention. Tapping a candidate dispatches
  `AppAction.Tile(exerciseId, TileAction.Place(position, form))` and `onTogglePicker(null)` (closes).
- No drag, no reordering: `position` is fixed by `TileExercise.segments` (SB-6 is Polish-only
  today; design doc §9 reserves non-fixed order for a future language, out of this scope).

### 2.2 Structure (Revealed phase, `builder.result != null`)

Same `.tile-row`, now read-only:
- `Locked` unchanged.
- `Choice` tiles are no longer buttons-that-open-a-picker; each becomes `.tile-correct` (green) or
  `.tile-incorrect` (red) by checking `builder.result.errors.any { it.position == segment.position }`.
  An incorrect tile stays a `<button>` (now meaning "show the rule", not "open picker") —
  `aria-expanded` reflects `openError == segment.position`; a correct tile becomes a plain `<span>`
  (nothing to tap). Tapping an incorrect tile dispatches
  `AppAction.Tile(exerciseId, TileAction.TapError(position))` and `onToggleError(position)`.
- Below the row, the same `.collapsible` panel (reused, different content) renders
  `builder.errorExplanation` via the **existing** `renderCardBlocks(panel, blocks, "card-blocks-tile-error", langCode)`
  from `CardBlocksWeb.kt` — zero new block-rendering code; `Block.Rule` already renders into
  `.rule-focus` exactly like the Typed/Oral method-feedback box does.
- An overall `.typed-result correct|incorrect` banner (same classes `renderAnswerBack` already uses
  for Typed) summarizes `builder.result.correct`, so TileBuilder's Revealed face reads consistently
  with Typed's, not as a third unrelated visual language.

### 2.3 `TrainingCardDom.kt` integration points

`renderAnswerArea` (Question phase, ~line 314): the existing `modes` row of two buttons
(Oral/Typed) gains a third:
```kotlin
modes.appendChild(button("Собрать из слов", active = state.answerMode == AnswerMode.TileBuilder, pressed = state.answerMode == AnswerMode.TileBuilder) {
    dispatch(AppAction.SetAnswerMode(AnswerMode.TileBuilder))
}.apply { disabled = state.tileBuilder == null })
```
Then the existing `if (state.answerMode == AnswerMode.Typed) {...} else {...}` becomes a
three-way branch:
```kotlin
when {
    state.answerMode == AnswerMode.Typed -> { /* unchanged textarea */ }
    state.answerMode == AnswerMode.TileBuilder && state.tileBuilder != null -> {
        val checkButton = renderTileBuilderSection(area, exercise.id, state.tileBuilder, openPicker, openError, lang, ::setOpenPicker, ::setOpenError, dispatch)
        area.appendChild(checkButton) // "Проверить" — see 2.4, replaces the shared reveal-button append below
        return // tile builder owns its own trailing button; skip the generic one below
    }
    else -> area.appendChild(node("p", "muted", "Произнеси целое предложение, затем переверни карточку.")) // Oral, and TileBuilder-unavailable fallback (SB-4's documented fallback)
}
area.appendChild(button(if (state.answerMode == AnswerMode.Typed) UiCopy.CheckAndShowAnswer else UiCopy.ShowAnswer, primary = true) {
    state.exerciseId?.let { dispatch(AppAction.Reveal(it)) }
}.apply { id = "training-reveal"; className += " reveal-button" })
```

### 2.4 The "Check" button

Distinct from the shared `#training-reveal` button used by Oral/Typed: labeled "Проверить",
`primary = true`, same `.reveal-button` class (identical sizing/placement), `disabled` while any
`Choice` position is unfilled (mirrors the `accepts()` guard in §1.2 — belt-and-suspenders, same
pattern `model.referenceToggle.enabled` already uses for a disabled `<button>`). Dispatches
`AppAction.Tile(exercise.id, TileAction.Check)`, not `AppAction.Reveal` directly — honors the
design doc's own UI action contract (§8) even though both end up calling the same `reveal()`
internally (§1.3).

### 2.5 Picker/error "which one is open" state — where it lives

Not in `TileBuilderState` (SB-4's shared contract has no such field, correctly — it's a pure
display concern, not cross-host learning state). Two small instance vars on `TrainingCardDom`
(which already outlives individual renders, held by `TrainingDomRenderer`):
```kotlin
private var openPicker: Int? = null
private var openError: Int? = null
```
Reset to `null` whenever `exercise.id` changes (checked at the top of `renderCard`, same place
`justRevealed`/`enteringChainComplete` are already computed by comparing against the previous
`AppUiState`) — a stale picker/error must never survive into the next card.

## 3. Styling — `training.css` additions

New rules only; nothing existing changes shape (only the `modes`/`answer-mode` row gains a third
button, already handled by the existing `.answer-mode button` rule).

```css
.tile-row{display:flex;flex-wrap:wrap;gap:8px;align-items:center;margin:4px 0 10px}
.tile{min-height:48px;min-width:48px;padding:10px 14px;border-radius:10px;display:inline-flex;align-items:center;justify-content:center;font-size:1.05rem}
.tile-locked{background:transparent;border:1px solid transparent;color:var(--text);padding-left:4px;padding-right:4px}
.tile-choice{background:var(--button);border:2px solid var(--line);color:var(--button-text)}
.tile-choice.tile-empty{border-style:dashed;color:var(--muted)}
.tile-choice[aria-expanded=true]{border-color:var(--focus)}
.tile-picker{display:flex;flex-wrap:wrap;gap:8px;padding:2px 0 4px}
.tile-option{background:var(--surface-raised);border:1px solid var(--line)}
.tile-correct{background:#183c2d;border-color:#2f6b4c;color:#a7e4bd}
.tile-incorrect{background:#382228;border-color:#7a3c45;color:#efbbc3}
:root[data-theme=light] .tile-correct{background:#d9f2e4;border-color:#5fa97c;color:#15533d}
:root[data-theme=light] .tile-incorrect{background:#fce5e8;border-color:#c66b79;color:#812438}
.tile-row+.collapsible{margin-bottom:10px}
```

`.collapsible`/`.collapsible.expanded`/`transition`/`will-change`/reduced-motion disabling already
exist globally (`training.css` lines 242–249, 101, 206) — the picker and error panels inherit
`200ms` motion for free by reusing that class, satisfying SB-AC10/the design doc's "200ms ease-out"
and "`animationsEnabled: false` → instant" requirements with zero new animation code. The global
`@media(prefers-reduced-motion:reduce){*{animation:none!important;transition:none!important}}`
rule and `[data-animations=off] *` rule (already present) cover tile movement the same as every
other transition in this file — nothing tile-specific to add there.

Tile size (`min-height/min-width:48px`) and gap (`8px`) satisfy SB-AC9 directly in the base rule,
not only in a mobile media query — same tap target on desktop and phone. `.tile-row`/`.tile-picker`
use `flex-wrap:wrap`, never `overflow-x:auto`, so no horizontal scroll at any width (kmp-web's own
320px-reflow rule) — long sentences wrap to a second line instead of scrolling.

## 4. Accessibility

- `.tile-row` has `aria-label="Собери предложение"`; each `Choice` button's accessible name is its
  own text content ("moją" / "___") — no separate visually-hidden label needed, consistent with
  how rating buttons already work.
- `aria-expanded` on the active Choice/error tile mirrors visual open state (same pattern
  `renderTraining`'s `Focused` mode button and `ToggleReference` already use).
- `inert`/`aria-hidden` on the collapsed picker/error panel (via `applyCollapsible`, unchanged)
  keeps hidden candidates out of the tab order and AT tree — same guarantee the reference panel
  already has.
- Correct/incorrect is never color-only: `.tile-correct`/`.tile-incorrect` tiles additionally gain
  `aria-label="${form} — верно"` / `"${form} — неверно, нажми для правила"` (text, not only color),
  satisfying compose-multiplatform-ui's "correctness understandable without color alone."
- Keyboard: tiles are real `<button>`s, so Tab/Shift+Tab/Enter/Space work with zero extra wiring
  (same reason every other control in this file is a native `<button>`, never a `div role=button`).
  No new global keydown handler is added — `TrainingWebApp.kt`'s existing keyboard handler is
  untouched; Space/Enter/Arrow rating shortcuts already guard on `editableTarget`, and tiles are
  plain buttons, not editable fields, so no conflict arises.

## 5. Acceptance criteria (SB-6)

- **SB6-AC1:** An exercise with `tileTargetSlots` set (SB-5) shows an enabled "Собрать из слов"
  mode button; selecting it renders the full sentence as a tile row with every `Choice` position
  initially empty (`___`) and every `Locked` position as plain text, in `position` order.
- **SB6-AC2:** An exercise without `tileTargetSlots` (today: every recipe, until SB-5 content is
  authored) shows the mode button disabled; selecting `TileBuilder` on such a card (e.g. via a
  stale session value) falls back to the Oral prompt, never a broken/empty tile row.
- **SB6-AC3:** Tapping an empty/filled `Choice` tile opens its form picker showing exactly
  `correctForm` + its `alternatives`, in the shared state's existing order; tapping a candidate
  places it (`AppAction.Tile(.., Place)`), closes the picker, and the tile now shows that form.
  Only one picker is ever open at a time.
- **SB6-AC4:** The "Проверить" button is disabled until every `Choice` position has a placed
  form; enabling it never requires `Locked` positions to be "filled" (they never are).
- **SB6-AC5:** Pressing "Проверить" with at least one wrong form shows each wrong tile in red and
  each right tile in green; locked tiles are unstyled. An overall `.typed-result incorrect` banner
  appears, mirroring Typed mode's existing visual language.
- **SB6-AC6:** Tapping a red tile shows a mini-rule panel (`Block.Rule`, via the existing
  `renderCardBlocks`) below the row; tapping it again (or a different red tile) toggles/swaps it.
  Tapping a green tile does nothing (not a button).
- **SB6-AC7:** Rating (Again/Good) after a TileBuilder check writes to the same FSRS/progress
  journal as Oral/Typed, with `tileBuilder.result.correct` as the recorded correctness — verified
  against the same `ReviewReducer`/`ProgressDocument` round-trip Typed already has a test for.
- **SB6-AC8 (mobile/responsive):** At 320–375 CSS px, tiles remain ≥48×48, gap stays 8px, and the
  row wraps rather than scrolling horizontally; the picker panel never pushes the "Проверить"
  button off-screen in a way that requires horizontal scroll to reach.
- **SB6-AC9 (motion):** With `animationsEnabled: false` (or `prefers-reduced-motion: reduce`),
  opening/closing the picker and error panel is instant (inherits the existing global rule) with
  no separate code path to verify.
- **SB6-AC10 (keyboard/AT):** Every tile, including the picker/error toggle, is reachable by
  Tab and activatable by Enter/Space; a screen reader announces correctness from text, not color
  alone (§4); hidden picker/error candidates are absent from a Tab sweep while collapsed.
- **SB6-AC11 (no regression):** Oral and Typed modes render and behave byte-identical to before
  this change — verified by running the existing Playwright Training specs unmodified.

## 6. Verification plan

- **Shared (`:shared` commonTest):** extend `TrainingStoreTest`-style coverage (or a new focused
  test file) for: `AppAction.Tile(Place/Remove)` mutating only `tileBuilder.placed`; `Check`
  (via `accepts()` + `reveal()`) computing `result` and leaving `phase == Revealed`; `rate()`
  recording `tileBuilder.result.correct` into the review the same way Typed's `evaluation.correct`
  already is (mirror the existing Typed round-trip test); `accepts()` rejecting `Check` while
  incomplete and `TapError` for a non-error position; a fresh `exercise` always clearing/re-deriving
  `tileBuilder`, including the `tileBuilder == null` case when the new exercise opts out.
- **Web (Playwright, `playwright-testing` skill):** a new spec exercising: mode switch to
  TileBuilder, filling every `Choice` via its picker, pressing "Проверить", seeing color feedback,
  tapping a red tile to reveal the rule panel, rating, and confirming the next card starts clean
  (no leftover open picker). Reuse role/name locators (`getByRole("button", { name: ... })`) per
  the project's existing Training specs rather than CSS selectors. Needs a course pack/seed whose
  recipe actually sets `tileTargetSlots` (SB-5) — if none is authored yet, the spec must author one
  fixture-side rather than asserting against production content that doesn't opt in yet.
- **Regression:** run the existing Training Playwright suite unchanged (SB6-AC11) and the
  `:shared` test suite, to confirm the new `AppUiState.tileBuilder` field and `reveal()`/`rate()`
  changes don't alter Oral/Typed behavior (every new branch is additive — `tileBuilder == null`
  for Oral/Typed's own derivation path only if the active exercise lacks `tileTargetSlots`; when it
  doesn't lack them, `tileBuilder` is still derived and populated even under Oral/Typed, but never
  read by their rendering branch — confirm via a snapshot-style test that Oral/Typed `AppUiState`
  equality checks in `TrainingDomRenderer.render`'s diffing (`old.copy(draft = state.draft) == state`)
  still short-circuit correctly with the new field present).

## 7. Documentation and handoff

- Update `Plans/Architecture/SENTENCE-BUILDER-BLUEPRINT.md`'s own "SB-6..10 — хосты параллельно"
  line with a pointer to this file once implemented, same convention the SB-1..5 blueprint uses
  for its own acceptance table.
- Flag explicitly to whoever implements SB-7: `errorExplanationFor` in `TrainingStore.kt` (§1.2) is
  a one-line placeholder; replacing it with `StyleComposer.blocksForError(exercise, error)` is SB-7's
  entire remaining scope — no UI change needed on either web or a future Android/iOS host, since
  the contract (`List<Block>?`) doesn't move.
- Flag to whoever implements SB-8: `ScaffoldingLevel.Partial` is hardcoded at every `tileBuilder`
  derivation site (§1.1); SB-8 replaces that literal with a progress-driven lookup — again no UI
  change, since the UI never reads `scaffolding` directly today (it affects which segments are
  `Locked` vs `Choice`, already baked into `TileExercise.segments` by the time the UI sees it).
- Open question for the content/DSL owner (SB-5, already shipped): no pack today actually sets
  `tileTargetSlots`, so SB6-AC1/AC3/AC5/AC6 have no real production exercise to exercise end-to-end
  until at least one recipe opts in. Not a blocker for implementing SB-6 (the Playwright spec can
  author a fixture recipe), but worth flagging so "I tried it in the real app and saw nothing" isn't
  mistaken for a web bug.
