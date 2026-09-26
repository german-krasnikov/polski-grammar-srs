# Android lane — evidence log

Worktree: `/Users/german/Work/JS/polski-lanes/android` (branch `lane-android`, from `main` @ `e52320c`).
Scope: Android host only. `kotlin/iosApp`, `kotlin/macosApp` and web code are owned by other lanes and are not touched here.

## A1 — training card expand reveal (D1)

**Task**: remove the training card's 3D flip; reveal the answer by expanding it downward below the
still-visible question, with a short stagger and a spring-like ease-out; tap on the question
reveals once (alongside the existing button).

**Files changed**:
- `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidFlipCard.kt` — dropped the
  3D-rotation `AndroidFlipCard` composable and `flipOnTap`; added `AndroidAnswerReveal`
  (`AnimatedVisibility` + `expandVertically`/`fadeIn`, `spring(DampingRatioNoBouncy, StiffnessMediumLow)`,
  using the `MutableTransitionState(false).apply { targetState = true }` idiom so the very first
  reveal actually animates instead of snapping) and `AndroidStaggeredReveal` (per-group
  `fadeIn`+`slideInVertically`, `index * 70ms` stagger). Kept `ratingForDrag`, `cardEffectToPlay`,
  `SingleRatingGate` unchanged; replaced the old flip-or-swipe gesture detector with
  `detectSwipeRating` (same horizontal-drag-past-threshold contract, minus the now-meaningless
  tap-to-flip branch).
- `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidTrainingScreen.kt` — the
  question content (source sentence + prompt) is now always composed while
  `CardPhase.Question`/`Revealed`; the answer-mode chooser, typed field and reveal button only
  show pre-reveal; a `Modifier.clickable` on the question column (only while `Question`) dispatches
  `AppAction.Reveal` — nested interactive descendants (mode chooser, text field, the button itself)
  consume their own taps first, so this never double-fires. The revealed answer is composed inside
  `AndroidAnswerReveal`, split into three `AndroidStaggeredReveal` groups (answer, explanation,
  rating panel) per the product spec's "answer, explanation, rating panel unfold... with short
  stagger" wording. Dropped the now-flip-only ring overlay wiring (`ringsExpanded`,
  `AndroidFlipRingsOverlay`) from this screen; `AndroidRiveOverlay` (rating effects) is unchanged.
- `kotlin/androidApp/src/test/java/dev/polski/grammarmatrix/AndroidFlipCardTest.kt` — removed the
  `flipOnTap`/`isFlipTap` cases (no flip left to test); kept the swipe/rating-gate/effect tests
  unchanged.
- `kotlin/androidApp/src/test/java/dev/polski/grammarmatrix/AndroidFlipCardComposeTest.kt` → renamed
  to `AndroidAnswerRevealComposeTest.kt`: replaced the old 90°-midpoint flip test (nothing left to
  flip) with two tests against `AndroidAnswerReveal` — the animated path shows the content once the
  `AnimatedVisibility` clock settles, and `reduceMotion` shows it immediately with no animation.

Left untouched (out of this task's scope, D3): `AndroidFlipRingsOverlay`/`rings.riv` and
`RiveMeasurementVariant` — still defined, just no longer called from the training screen. No
`commonMain`/`shared` changes were needed for this task.

**TDD note**: this was a structural replacement (flip → expand) rather than an isolated new
behavior, so RED was verified by stashing the source changes and confirming the new
`AndroidAnswerRevealComposeTest`/updated `AndroidFlipCardTest` no longer matched the checked-in
API (compile-level, expected for a rename/removal — not claimed as assertion-level RED); GREEN is
the full test run below, plus the on-device verification.

**Checks (lane-android worktree, `kotlin/`)**:
- `./gradlew :composeApp:compileAndroidMain` — PASS.
- `./gradlew :androidApp:testDebugUnitTest` — PASS, full androidApp unit suite green, including
  `AndroidFlipCardTest` (9 tests) and `AndroidAnswerRevealComposeTest` (2 tests, Robolectric +
  Compose UI test, real `AnimatedVisibility` clock driven directly).
- On-device (`emulator-5554`, `Polski_ARM35`, debug APK installed and launched): drove three
  consecutive training cards —
  - tap on the question card revealed the answer (question stayed visible, answer expanded below,
    no flip); repeated tap after reveal was inert (no re-trigger, no crash);
  - swipe right on the revealed answer rated "Вспомнил" (chain 0/5 → 1/5), swipe left on the next
    card rated "Повторить" (1/5 → 2/5) — both single-shot, buttons/mode-chooser correctly hidden
    once revealed and back for the fresh next card;
  - `adb logcat` showed no `FATAL EXCEPTION`/`AndroidRuntime` crash for the app process across the
    whole run.

**Skipped**: Space/keyboard reveal wiring, D3's ring-asset removal, and D2 (vocabulary) — out of
this task's scope.

## Reviewer correction (commit 05e475d): `expandFrom` missing on `expandVertically`

**Finding**: `AndroidAnswerReveal` called `expandVertically(spring(...))` with no `expandFrom`, so
it used the library default `Alignment.Bottom`. Confirmed against the actual pinned
`androidx.compose.animation:animation-android:1.12.1` bytecode (`EnterExitTransitionModifierNode`,
`EnterExitTransitionKt.expandVertically$default`): the default really is `Alignment.Bottom`, and
that alignment is fed straight into the per-frame `Alignment.align(targetSize, currentAnimatedSize)`
placement call that positions the (always fully-measured) content inside the growing/clipped box.

**Empirical proof (not just bytecode reading)**: a Robolectric+Compose-UI-test harness drove the
real `AnimatedVisibility` clock in 8ms steps over two `Text` nodes (`TOP-MARKER` then
`ANSWER-CONTENT`) inside `AndroidAnswerReveal`, reading `fetchSemanticsNode().boundsInRoot` each
step. With the unfixed default: `ANSWER-CONTENT` (the second/tail item) became visible
(`boundsInRoot.height > 0`) at step 2 (~16ms), while `TOP-MARKER` (the heading, meant to unfold
*first* per D1) stayed at height `0` (fully clipped away) until step ~8 (~64ms) — i.e. the tail
renders before the heading, exactly the "opposite of the required... reveal" the reviewer
described. (An earlier, weaker test that only compared `TOP-MARKER`'s *top* position pre/post-settle
passed even on the buggy code — the clip always reports `top` clamped to the visible window's edge
regardless of alignment, so it could not distinguish Top from Bottom; it was replaced.)

**Fix**: pass `expandFrom = Alignment.Top` to `expandVertically(...)` in `AndroidAnswerReveal`
(`kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidFlipCard.kt`), pinning the
content's top and growing/revealing it downward, matching D1's "answer, explanation, rating panel
unfold... " top-down ordering.

**Files changed (this correction)**:
- `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidFlipCard.kt` — added
  `import androidx.compose.ui.Alignment`; `expandVertically(animationSpec = ..., expandFrom =
  Alignment.Top)`.
- `kotlin/androidApp/src/test/java/dev/polski/grammarmatrix/AndroidAnswerRevealComposeTest.kt` —
  replaced the position-only test with `answerContentRevealsTopDownNotBottomUp`: drives the clock
  in 8ms steps and asserts the heading (`TOP-MARKER`) becomes visible (`boundsInRoot.height > 1px`)
  no later than the tail (`ANSWER-CONTENT`).

**TDD**: RED confirmed by `git stash`-ing the fix (reverting to the exact unfixed
`expandVertically(spring(...))` call) and running the new test — it failed with an
`AssertionError` (heading appeared at a later step than the tail), matching the reviewer's report.
GREEN confirmed by restoring the fix and re-running — 3/3 tests in
`AndroidAnswerRevealComposeTest` pass.

**Checks**:
- `./gradlew :androidApp:testDebugUnitTest --tests "dev.polski.grammarmatrix.AndroidAnswerRevealComposeTest"`
  — PASS (3 tests) after the fix; FAIL (1 test, the new one) before it, confirming RED→GREEN.
- `./gradlew :androidApp:testDebugUnitTest --tests "dev.polski.grammarmatrix.AndroidFlipCardTest" --tests "dev.polski.grammarmatrix.AndroidAnswerRevealComposeTest"`
  — PASS.
- `./gradlew :androidApp:assembleDebug` — PASS.
- On-device (`emulator-5554`, debug APK reinstalled): tapped the question card and visually
  confirmed the reveal order top-to-bottom is "Эталон" (answer heading) → "Что изменилось"
  (explanation) → "ЗАПОМНИ" rule box → rating panel, i.e. heading first, matching D1 and the fix
  (screenshot inspected, not attached).

## A2 — vocabulary whole-card flip (D2)

**Task**: give the vocabulary ("Слова") card the whole-panel 3D flip the training card used to
have before D1 replaced it there (`Plans/Kotlin/FlipCardRivePlan.md` §16.0-B): the entire rounded
panel (background/border/radius/shadow) turns, not just its content; no more "Показать ответ"
button — a tap on the card reveals once and flips immediately; a further tap only turns the panel
back and forth (visual only, never re-reveals, never re-rates); face swap exactly at 90°.

**Why a new host-specific screen, not an edit to the shared `VocabularyScreen`**: unlike Training
(already fully replaced per-host by `AndroidTrainingScreen`/`AndroidContent`), the Vocabulary tab
was still calling the shared `commonMain` `VocabularyScreen` composable directly from
`MainActivity.kt` — the only other callers of that file are the (out-of-scope, unmanaged) JVM
desktop preview and its tests. Editing it in place to add a 3D flip would be an Android-only
behavior change smuggled into code the desktop preview also compiles and tests against, and would
have needed either a `reduceMotion`/host-flag threaded through a file two other lanes' tooling
(and the desktop preview's tests) touch, or a flip on desktop too (out of scope, not requested).
Following the already-established Android pattern instead — a full host-specific screen — keeps
the whole change inside `androidMain`/`androidApp`, touching `commonMain` only for one additive,
behavior-preserving visibility widening (below).

**Files changed**:
- `kotlin/composeApp/src/commonMain/kotlin/polski/ui/screens/VocabularyScreen.kt` — **only** change:
  `private fun VocabularyCatalog` → `internal fun VocabularyCatalog`, so the new Android screen can
  reuse the catalog (import/export dialog, filter dropdown, checkboxes, custom-word editor — all
  unaffected by the flip) instead of duplicating ~110 lines of it. No logic changed; the shared
  screen and the desktop preview still compile and behave identically (confirmed below).
- `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidFlipCard.kt` — added
  `isFlipTap` (pure function, same "short and mostly-still" contract the training card's pre-D1
  flip used) and `detectFlipOrSwipe` (single pointer-input recognizer combining tap-to-flip-back
  and swipe-to-rate on the revealed face — a `clickable` layered over a separate drag detector was
  already proven broken for this exact card class, `FlipCardRivePlan.md` §14's evidence log, so
  this reuses the merged-detector fix from there rather than re-discovering it), and the new
  `AndroidFlipCard` composable: the same `rotationY`/`cameraDistance`/`Animatable`-based flip the
  training card used before D1 (git `af126a6`/`b22e758`), generalized from `CardPhase`/exerciseId
  to a plain `revealed: Boolean`/`itemId: String` contract, with the old ring-effect wiring
  (`onRingsExpandedChange`) dropped entirely — D3 already calls for removing `rings.riv` project-
  wide, so this flip is written without it from the start rather than adding it and removing it
  again later.
- **NEW** `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidVocabularyScreen.kt`
  — `AndroidVocabularyScreen` (header, import/export, direction chooser via the existing
  `AndroidChoiceMenu`, the flip card, then the reused `VocabularyCatalog`), `VocabularyFrontFace`
  (no reveal button; the prompt block itself is `Modifier.clickable(onClickLabel = "Показать
  ответ", role = Role.Button)` — narrowly scoped to the prompt, not the whole face, so it doesn't
  nest a button role around the real mode-chooser/`OutlinedTextField`/"Проверить" controls also in
  `front`; those consume their own taps first, same nested-control precedent already verified for
  `AndroidTrainingScreen`'s question-card tap-reveal) and `VocabularyBackFace` (answer, rating
  buttons, swipe hint — each face draws its own full `Card` panel, matching the web host's own v4
  lesson that the flipping object must be the panel itself, `FlipCardRivePlan.md` §17.1).
- `kotlin/androidApp/src/main/java/dev/polski/grammarmatrix/MainActivity.kt` — swapped the shared
  `VocabularyScreen(...)` call for `AndroidVocabularyScreen(...)`, adding
  `reduceMotion = session.preferences.motion == Motion.Reduced` (same source `AndroidContent` already
  uses for the training card, per the plan's FC-10 note that Android has no system reduced-motion
  signal of its own).
- `kotlin/androidApp/src/test/java/dev/polski/grammarmatrix/AndroidFlipCardTest.kt` — added
  `isFlipTap` cases (short tap vs. past-slop drag).

Left untouched (out of this task's scope): swipe-to-rate itself is unchanged (still gated by the
existing `enableSwipeRating`/`session.preferences.swipeRatingEnabled`, using the same
threshold/tap-slop constants as before); rings/Rive effects (D3) — the vocabulary card had none
before this task and still has none; the catalog/editor UI (reused as-is).

**TDD**: RED — added `isFlipTap` calls to `AndroidFlipCardTest.kt` before the function existed;
`:androidApp:testDebugUnitTest --tests "...AndroidFlipCardTest"` failed to compile
(`Unresolved reference 'isFlipTap'`), a genuine (if compile-level) RED for a new pure function, not
a tooling failure. GREEN — added `isFlipTap`/`detectFlipOrSwipe`/`AndroidFlipCard`; same command
passes (10 tests, including the 2 new ones).

**Checks (lane-android worktree, `kotlin/`)**:
- `./gradlew :androidApp:testDebugUnitTest --tests "dev.polski.grammarmatrix.AndroidFlipCardTest"`
  — FAIL (compile error) before the implementation, PASS (10/10) after — RED→GREEN.
- `./gradlew :composeApp:compileAndroidMain :androidApp:compileDebugKotlin` — PASS.
- `./gradlew :androidApp:testDebugUnitTest` — PASS, full `androidApp` unit suite green.
- `./gradlew :androidApp:assembleDebug` — PASS.
- `./gradlew :composeApp:desktopTest --tests "*Vocabulary*"` — PASS (confirms the one `commonMain`
  visibility change didn't affect the JVM desktop preview's own vocabulary tests).
- On-device (`emulator-5554`, `Polski_ARM35`, debug APK installed and launched, driven via
  `adb shell input tap`/`uiautomator dump`/`screencap`, one word ("жена") selected in the A1
  catalog first): unrevealed card showed the prompt with no "Показать ответ" button (`uiautomator
  dump` confirmed a `clickable="true" focusable="true"` node wrapping just the prompt text, no
  button node); tapping the prompt revealed **and** flipped in the same gesture — the dump right
  after showed "Эталон · польский"/"żona"/translation/form/example/swipe hint/"Повторить"/
  "Вспомнил", confirmed visually too (screenshot: whole panel is the answer face, rounded
  corners/shadow intact, no separate static frame); tapping the revealed panel again (not on a
  button) flipped it back to the question face only — the catalog count and card state were
  unchanged, confirming this was visual-only, not a second reveal or a rating; tapping the prompt a
  third time re-revealed+re-flipped, and tapping "Вспомнил" this time actually rated the card — the
  screen advanced to "На сейчас всё повторено" (no due item left), confirming the rating reached
  FSRS; `adb logcat` showed no `FATAL EXCEPTION`/`AndroidRuntime` crash for the app process across
  the whole run.

**Known limitation (honest, not a regression)**: TalkBack's actual spoken announcement for the
prompt's `onClickLabel`/`role = Role.Button` was not captured — `uiautomator dump`'s XML surfaces
`content-desc`/`class`/`clickable`, not a node's accessibility-action label, so the dump confirms
the node is clickable/focusable but not the exact TalkBack phrasing. This is the standard,
documented Compose API for exposing a clickable region as an accessible button
(`Modifier.clickable(onClickLabel, role = Role.Button)`), used the same way elsewhere in this
codebase; a live TalkBack session would be needed to confirm the spoken text and was out of this
LEAN-MODE pass.

**Skipped**: typed-mode on-device click-through (no due word was available to re-select for it
within this pass; the same `front`/`onReveal`/textfield code path is exercised by
`AndroidChoiceMenu`/`OutlinedTextField`, both pre-existing, tested elsewhere); swipe-to-rate
on-device (unchanged from before this task, not re-verified here); D3 (rings/Rive removal),
D4 (tab paging), D5 (Animations toggle) — out of this task's scope entirely.
