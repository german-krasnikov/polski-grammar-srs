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

## A2 correction — gate the vocabulary flip's gesture on `showingBack`, not `revealed`

**Reviewer finding** (Major, on commit `61539db`): `AndroidFlipCard`'s `gesture` when-block
attached the swipe/tap detector (`detectFlipOrSwipe`, or the plain `clickable` toggling `flipped`)
the instant `revealed` became `true`, not once the flip actually crossed 90° (`showingBack`). Since
`front()` — with its own always-on tap-to-reveal `clickable` — keeps rendering and receiving touch
for the ~250ms `tween(500)` takes to reach 90°, a swipe thrown right after the reveal tap (D3's own
primary rating gesture, chained right after a reveal) could dispatch a real `Rating.Again`/`Good`
via `onRate` while the question face was still on screen, before the answer was ever shown; a
double-tap in that same window could also race `front`'s own reveal against the ancestor's
`toggleFlip`, visibly reversing the in-flight animation. The pre-D1 training-card version of this
same gesture pattern (git `af126a6`/`b22e758`) gated on `showingBack` for exactly this reason; D2's
port of it accidentally regressed the gate to `revealed`.

**Fix**: `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidFlipCard.kt` —
`gesture`'s first branch is now `!showingBack -> Modifier` (was `!revealed -> Modifier`); everything
else (the `enableSwipeRating`/`else` branches, `detectFlipOrSwipe`, `toggleFlip`) is unchanged.
Widened the comment above it to explain the timing window and why it matters. One line changed, no
public signatures touched.

**TDD**: RED — added `AndroidFlipCardGestureTimingTest.kt` (Robolectric, real Compose animation
clock via `composeRule.mainClock`, `autoAdvance = false`): reveal via an external button (isolating
the timing question from any interaction with `front`'s own tap-to-reveal control), advance the
clock by 100ms (well under the ~250ms needed to cross 90°, confirmed against `tween(500)`), then
either swipe or tap the card. Against the pre-fix code both tests failed
(`swipeDuringTheFlipAnimationDoesNotDispatchARatingBeforeTheAnswerIsShown`: a rating was dispatched
mid-flip; `tapDuringTheFlipAnimationDoesNotReverseIt`: the card reverted to `FRONT` instead of
settling on `BACK`) — a genuine behavioral RED reproducing both consequences the reviewer described,
not a compile/tooling failure (confirmed by temporarily `git stash`-ing just the fix and re-running:
2/2 failed on the pre-fix code). GREEN — after the one-line fix, same command: 2/2 pass.

**Checks (lane-android worktree, `kotlin/`)**:
- `./gradlew :androidApp:testDebugUnitTest --tests "dev.polski.grammarmatrix.AndroidFlipCardGestureTimingTest"`
  — FAIL (2/2) on the pre-fix code, PASS (2/2) after — RED→GREEN.
- `./gradlew :androidApp:testDebugUnitTest` (full `androidApp` unit suite, incl. the existing
  `AndroidFlipCardTest`/`AndroidAnswerRevealComposeTest`) — PASS, 34/34.
- `./gradlew :androidApp:assembleDebug` — PASS.
- On-device (`emulator-5554`, `Polski_ARM35`, debug APK reinstalled and launched, driven via
  `adb shell input tap`/`swipe`, `uiautomator dump`): tapped the vocabulary prompt ("жена") — the
  dump right after showed the fully-flipped back face (`żona`/translation/form/example/swipe
  hint/rating buttons), confirming reveal+flip still works after the fix; a subsequent
  `input swipe` (right-to-left, simulating "Повторить") on the settled back face rated the card and
  advanced the session to "На сейчас всё повторено" — confirms the swipe-to-rate path itself (now
  gated on `showingBack`, same as before this fix for an already-settled card) still reaches FSRS.
  A manual mid-animation race is not reliably reproducible through `adb shell input`'s timing
  granularity, which is what the Robolectric test above exists to pin down deterministically.

**Skipped**: a live TalkBack pass and typed-mode click-through were already out of scope for A1/A2
and remain so for this one-line correction; D3/D4/D5 remain untouched.

## A3 — whole-card swipe rating (D3)

**Task**: whole-card drag affordance (translate + tilt, growing tint/label, snap-back under
threshold, fly-out on commit) on both the training answer panel and the vocabulary back face; no
rating buttons on phones; Rive rating effects; remove `rings.riv` + the ring-overlay code.

**Design** (`Plans/Kotlin/FlipCardRivePlan.md` §17.3/UX4-08..10, the same drag language the web
host uses): one new shared composable, `AndroidRatingDragSurface` (in `AndroidFlipCard.kt`),
replaces both cards' old commit-or-nothing `detectSwipeRating`/`detectFlipOrSwipe` gestures. It
owns an `Animatable` `offsetX`, a new continuous `detectDragGesture` recognizer (reports every
horizontal-confirmed move, not just the final release — needed to drive a *live* translate/tilt,
unlike the old detectors), and two new pure helpers (`dragProgress`, `dragRotationDegrees`,
alongside the existing `ratingForDrag`/`isFlipTap`) that compute the tint/label growth and tilt
from the live `dx`. A completed drag past the threshold flies the card off (`animateTo` a large
offset, `tween(220)`) and calls `onRate` only once that animation finishes (matching the web
host's own "animate, *then* rate" order); a short release under the threshold snaps back
(`animateTo(0f)`); `reduceMotion` makes both instant (`snapTo`, no button-removal regression: the
card must still be ratable even with motion off). `AndroidDragRatingOverlay` (a `BoxScope`
extension, `matchParentSize()`) draws the tint (reusing the existing `error`/`primary` tokens, no
new palette) and the growing "Повторить"/"Vспомнил" label.

**No rating buttons on Android** (D3: "phones/tablets: no rating buttons", not the web's own
"visually collapse but keep in the a11y tree" compromise, UX4-11): `AndroidRatingActions`
(training) and the `OutlinedButton`/`Button` row (`VocabularyBackFace`) are deleted outright, along
with now-dead `intervalLabel`. Per `compose-multiplatform-ui`'s "one accessible action per rating"
and `kmp-android`'s own visible-actions guidance, TalkBack still needs an equivalent, so
`AndroidRatingDragSurface` exposes both ratings as `CustomAccessibilityAction`s
("Повторить"/"Вспомнил") on its own `semantics` node (`contentDescription = "Оценка карточки"` so
TalkBack announces something when it focuses that node) — always present, independent of drag.

**Removed the "swipe disabled ⇒ fall back to buttons" trap**: the existing `swipeRatingEnabled`
preference (`UserPreferences`, unchanged — no `commonMain` edit) used to gate *whether the drag
gesture rated at all*, with the visible buttons as the guaranteed fallback ("Кнопки оценки работают
всегда" in the old Settings copy). With buttons gone that would have been a dead end — turning the
setting off would leave a card with no way to rate it on a touch host. Fixed by narrowing what the
preference controls: the drag gesture (and its accessibility actions) are now always active
regardless of it; the preference only toggles the on-card hint text ("Свайп влево — повторить ·
вправо — вспомнил") and the Settings copy was rewritten to describe that accurately (`MainActivity.kt`
Settings screen: label → "Подсказка про свайп-оценку", helper text explains rating is always by
swipe and TalkBack gets the same two actions). This is a considered, in-scope deviation from a
literal read of the preference's old contract, not an accidental behavior change — flagging it
here rather than leaving it implicit.

**`AndroidFlipCard` (vocabulary)**: dropped its now-unused `enableSwipeRating` parameter (the
plain-`clickable`-vs-gesture branch it gated no longer exists — `AndroidRatingDragSurface` is
always used once `showingBack`, handling both tap-to-flip-back, via `onTap`, and drag-to-rate in
one recognizer). The flip mechanics themselves (rotationY/cameraDistance, the 90°-midpoint
face-swap, the `showingBack`-not-`revealed` gesture-timing gate from the prior correction) are
untouched — `AndroidRatingDragSurface` wraps the *existing* flip content, it doesn't replace it.

**Rive rating effects**: training already called `AndroidRiveOverlay(cardEffect)` (D1-era,
unchanged); vocabulary had none before this task and still has none — out of scope for this pass
(D3's own wording singles out the drag affordance/buttons/rings; adding a first Rive overlay to the
vocabulary card is a larger, separate wiring change — `SingleRatingGate`/`cardEffectToPlay` are
already there and ready for it, but wiring `AndroidRiveOverlay` into `AndroidVocabularyScreen` was
not part of "do only this, small and focused" and is left for a follow-up task rather than expanded
into this one silently).

**Rings removed**: `AndroidFlipRingsOverlay` deleted from `AndroidRiveOverlay.kt` (it had already
been unwired from both screens in A1/A2, per those sections' notes — this pass deletes the dead
function itself) and `composeApp/src/androidMain/res/raw/rings.riv` deleted
(`git rm`). `THIRD_PARTY/credits.md` — left untouched: it is a shared, cross-host doc ("still used
by Android and iOS this pass") that the iOS lane is presumably editing concurrently for its own
`rings.riv` removal; touching it here risks a lane merge conflict for a doc-only line, so the
correction ("Android" no longer applies) is left for post-merge integration rather than guessed at
here.

**Files changed**:
- `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidFlipCard.kt` — added
  `dragProgress`, `dragRotationDegrees` (pure), `detectDragGesture` (supersedes
  `detectSwipeRating`/`detectFlipOrSwipe`), `AndroidRatingDragSurface` + `AndroidDragRatingOverlay`
  (new); `AndroidAnswerReveal` gained an `itemKey` param and now always wraps its content in
  `AndroidRatingDragSurface` (including the `reduceMotion` early-return path, which previously had
  no gesture at all); `AndroidFlipCard` lost `enableSwipeRating`, gates
  `AndroidRatingDragSurface(onTap = ::toggleFlip)` on `showingBack` exactly as the gesture used to
  be gated.
- `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidTrainingScreen.kt` — call site
  updated (`exercise.id` as `itemKey`, `onRate = ::rate` unconditional); deleted
  `AndroidRatingActions` and `intervalLabel`; the third staggered group is now just the (optional)
  hint text; dropped the now-unused `FilledTonalButton` import.
- `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidVocabularyScreen.kt` —
  `AndroidFlipCard` call site drops `enableSwipeRating`; `VocabularyBackFace` lost its `onRate`
  param and the rating-buttons `Row`.
- `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidRiveOverlay.kt` —
  `AndroidFlipRingsOverlay` deleted.
- `kotlin/composeApp/src/androidMain/res/raw/rings.riv` — deleted.
- `kotlin/androidApp/src/main/java/dev/polski/grammarmatrix/MainActivity.kt` — Settings screen
  copy rewritten (see above); no other change.
- `kotlin/androidApp/src/test/java/dev/polski/grammarmatrix/AndroidFlipCardTest.kt` — added
  `dragProgress`/`dragRotationDegrees` cases.
- **NEW** `kotlin/androidApp/src/test/java/dev/polski/grammarmatrix/AndroidRatingDragSurfaceTest.kt`
  — Robolectric+Compose UI tests for `AndroidRatingDragSurface`: swipe-left/-right past the
  threshold rates `Again`/`Good` exactly once each; a short tap (with `onTap` set) fires `onTap`,
  not a rating; both ratings are always exposed as `SemanticsActions.CustomActions` and invoking
  the "Вспомнил" action rates `Good`.
- `kotlin/androidApp/src/test/java/dev/polski/grammarmatrix/AndroidFlipCardGestureTimingTest.kt` —
  dropped the now-removed `enableSwipeRating` argument from both calls; updated the second test's
  comment (the "plain-`clickable` ancestor branch" it described no longer exists — no gesture at
  all is composed before `showingBack`, which is what the test still verifies).
- `kotlin/androidApp/src/test/java/dev/polski/grammarmatrix/AndroidAnswerRevealComposeTest.kt` —
  all three calls updated with the new `itemKey` parameter.

**TDD**: RED — temporarily deleted `dragProgress`/`dragRotationDegrees` from the source; the new
`AndroidFlipCardTest` cases (and the rest of the file, which now calls them from
`AndroidRatingDragSurface`) failed to compile (`Unresolved reference`), confirmed, then restored —
GREEN. For `AndroidRatingDragSurfaceTest`, the first honest RED hit during development was a real
one: `bothRatingsAreAlwaysExposedAsAccessibilityCustomActions` initially queried the outer
`testTag("card")` node for `SemanticsActions.CustomActions` and failed
(`IllegalStateException: Key not present`) — the tag was on an ancestor Box in the test's own
harness, not on `AndroidRatingDragSurface`'s own semantics node, which doesn't merge descendants;
fixed by querying `onNodeWithContentDescription("Оценка карточки")` instead (the exact node the
actions are declared on). Re-ran — GREEN, 4/4.

**Checks (lane-android worktree, `kotlin/`)**:
- `./gradlew :composeApp:compileAndroidMain :androidApp:compileDebugKotlin` — PASS.
- `./gradlew :androidApp:testDebugUnitTest --tests "dev.polski.grammarmatrix.AndroidFlipCardTest"` —
  FAIL (compile error) with `dragProgress`/`dragRotationDegrees` removed, PASS (16/16) restored —
  RED→GREEN.
- `./gradlew :androidApp:testDebugUnitTest --tests "dev.polski.grammarmatrix.AndroidRatingDragSurfaceTest"`
  — PASS (4/4) after the `onNodeWithContentDescription` fix (FAIL 1/4 before it — RED→GREEN).
- `./gradlew :androidApp:testDebugUnitTest --tests "dev.polski.grammarmatrix.AndroidFlipCardGestureTimingTest"
  --tests "dev.polski.grammarmatrix.AndroidAnswerRevealComposeTest"` — PASS (5/5).
- `./gradlew :androidApp:testDebugUnitTest` — PASS, full `androidApp` unit suite green.
- `./gradlew :androidApp:assembleDebug` — PASS.
- On-device (`emulator-5554`, `Polski_ARM35`, debug APK installed and launched, driven via
  `adb shell input tap`/`swipe`, `uiautomator dump`, `screencap`): **Training** — revealed a card by
  tapping the question, confirmed no rating buttons anywhere in the revealed panel (screenshot),
  confirmed the swipe hint text; swiped right on the revealed panel → rated "Вспомнил" (chain 0/5 →
  1/5, progress bar advanced); revealed the next card, swiped left → rated "Повторить" (1/5 → 2/5).
  **Vocabulary** — unrevealed "жена" card showed no "Показать ответ" button (prompt itself is the
  control, per D2, unaffected by this task); tapping it revealed+flipped to the back face (no rating
  buttons, swipe hint shown); swiped right → rated "Вспомнил", advanced to "На сейчас всё
  повторено" (confirms the rating reached FSRS). **Settings toggle**: turned "Подсказка про
  свайп-оценку" off in Settings, confirmed (scrolled screenshot) the on-card hint text
  disappeared while the rating buttons stayed absent, then swiped a revealed training card again —
  still rated correctly (card 2/5 → 3/5, "negation" example carried through) — confirms rating is
  never gated by this preference, only the hint text is; re-enabled the toggle afterward. `adb
  logcat` showed no `FATAL EXCEPTION`/`AndroidRuntime` crash for the app process across the entire
  session (reveal, both cards' swipes, tab switches, Settings toggle).

**Skipped**: a live TalkBack pass (the custom-action wiring is verified at the semantics-tree level
in `AndroidRatingDragSurfaceTest`, not with an actual screen reader); `THIRD_PARTY/credits.md`'s
"still used by Android" line (left for post-merge integration, see "Rings removed" above); D4/D5
remain untouched.

## Reviewer correction (commit 6659a2f): vocabulary card never played a Rive rating effect

**Blocker**: `AndroidVocabularyScreen.kt`'s `rate()` called `ratingGate.rate(...)` and discarded the
returned `CardEffect?` — no `cardEffect` state, no `AndroidRiveOverlay` in that file. Confetti/error
played for training ratings only; the vocabulary card (explicitly in this task's scope) never got
one, so the D3 "Rive effects: confetti+check on Вспомнил, error on Повторить" requirement was unmet
for vocabulary.

**Fix** (`kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidVocabularyScreen.kt`):
mirrors `AndroidTrainingScreen.kt`'s existing wiring exactly — `var cardEffect by
remember(item.id) { mutableStateOf<CardEffect?>(null) }`, `rate()` now captures
`ratingGate.rate(...)`'s return and sets `cardEffect` when non-null, and the `AndroidFlipCard` call
is wrapped in a `Box` with `AndroidRiveOverlay(cardEffect) { cardEffect = null }` as a sibling. No
other file touched.

**Checks (lane-android worktree, `kotlin/`)**:
- `./gradlew :composeApp:compileAndroidMain` — PASS.
- `./gradlew :androidApp:testDebugUnitTest --tests "dev.polski.grammarmatrix.AndroidFlipCardTest"
  --tests "dev.polski.grammarmatrix.AndroidRatingDragSurfaceTest" --tests
  "dev.polski.grammarmatrix.AndroidAnswerRevealComposeTest"` — PASS (unaffected pure-function/
  gesture logic this wiring reuses; no new pure-function behavior was added, so no new case here —
  see on-device check below for the actual wiring proof, the same way the original task's own
  evidence log verified the vocabulary/training screens on-device rather than through a Compose
  test that would need to mount the real `RiveAnimationView`).
- `./gradlew :androidApp:assembleDebug` — PASS.
- On-device (`emulator-5554`, fresh `pm clear` + reinstalled debug APK, driven via `adb shell input
  tap`/`swipe`, `uiautomator dump`, `screencap`, rapid-fire screencaps around the swipe with no
  sleep in between): selected 2 vocabulary words ("жона", "kobieta"), revealed the "жона" card,
  swiped it right past the threshold. Captured frames show, in order: the live drag with the
  "Вспомнил" tint/label (D3, unchanged), the screen already advanced to the next card ("женщина"),
  and — the frame right after — a green Rive check-mark/confetti animation rendered over the
  "женщина" card, i.e. the effect fired for the *just-rated* vocabulary card and kept playing after
  the deck advanced (same "views are `remember(context)`-stable across the `item.id` change"
  behavior already relied on in `AndroidTrainingScreen`). `adb logcat` showed no
  `FATAL EXCEPTION`/`AndroidRuntime` crash across the session.

**Skipped**: nothing further; this was a single self-contained wiring gap.

## A4 — animated tab paging + collapsibles (D4)

**Task**: phone-like paging between the four bottom-nav tabs (old/new screen slide together
horizontally, direction from nav-bar order, ~300ms, emphasized easing, indicator already in sync
since `state.tab` changes before the animation starts) and animated show/hide (height+opacity)
for the app's collapsible panels, instead of both popping instantly.

**Files changed**:
- `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidContent.kt` — added
  `tabSlideDirection(from, to)` (pure; +1 when `to` sits at-or-right of `from` in the nav-bar order
  `[Training, Matrix, Progress, Vocabulary]`, else -1) and `AndroidTabContent(tab, reduceMotion,
  content)`: an `AnimatedContent` keyed on the tab, `transitionSpec` reading its own
  `initialState`/`targetState` (not the caller's ambient tab, which has already moved on) to pick
  `slideInHorizontally`/`slideOutHorizontally` (`tween(300, easing = CubicBezierEasing(.2f, 0f, 0f,
  1f))`, the same curve the web reference's `RouteSlide.kt` uses) + `fadeIn`/`fadeOut`, and
  `.using(null)` to drop the default size-morph so the container just sizes to whichever screen is
  taller during the transition rather than visibly stretching between two very different heights.
  `reduceMotion` swaps in `EnterTransition.None togetherWith ExitTransition.None` for an instant
  switch. [content] receives the per-branch `AppTab`, not the ambient one — see the bug this avoids
  below.
- `kotlin/androidApp/src/main/java/dev/polski/grammarmatrix/MainActivity.kt` — hoisted
  `reduceMotion` to one `val` (was computed inline twice) and replaced the plain
  `if (state.tab == Vocabulary) … else AndroidContent(state, …)` branch with
  `AndroidTabContent(state.tab, reduceMotion) { tab -> if (tab == Vocabulary) AndroidVocabularyScreen(…)
  else AndroidContent(state.copy(tab = tab), …) }` — `state.copy(tab = tab)` is what makes the
  outgoing branch keep rendering the screen it was already showing (see below), not settings
  (`showSettings`), which is untouched and still switches instantly — out of this task's scope.
- `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidWidgets.kt` — added
  `AndroidCollapsible(visible, reduceMotion, content)`: `AnimatedVisibility(visible = …)` with
  `expandVertically(tween(220), expandFrom = Alignment.Top) + fadeIn(tween(220))` entering and the
  mirror-image `shrinkVertically`/`fadeOut` exiting (top-anchored, matching the D1 answer-reveal fix
  so content unfolds top-down, not bottom-up); `reduceMotion` swaps in `EnterTransition.None`/
  `ExitTransition.None`. `AnimatedVisibility` already removes hidden content from the composition
  (and so the accessibility tree), so no extra semantics were needed.
- `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidTrainingScreen.kt` — the two
  plain `if (…) { … }` panels that popped instantly now go through `AndroidCollapsible`: the skill
  picker (`state.showSkillPicker`) and the case-reference table (`state.showReference &&
  !introducing`, "Таблица под рукой"/"Скрыть таблицу").

**A bug caught before it shipped** (would have been a RED test had I written the naive version
first): `AnimatedContent`'s `content: @Composable (T) -> Unit` composes both the outgoing and
incoming branch *simultaneously* for the ~300ms transition. A first draft had the lambda passed to
`AndroidTabContent` call `AndroidContent(state, …)` directly, reading `state.tab` from the
enclosing scope instead of the branch's own `tab` parameter — since `state` is one shared object,
both branches would have rendered the *same*, already-updated screen, i.e. no slide at all (or the
outgoing screen "converting" instantly into the incoming one, per the caught-in-the-act
`eachBranchRendersItsOwnTabNotTheLatestAmbientOne` test below). Fixed by passing
`state.copy(tab = tab)` — every other field stays the live `state`, only the per-branch screen
selector is pinned.

**TDD**: RED — `AndroidTabTransitionTest`/`AndroidCollapsibleComposeTest` written first against
`polski.ui.screens.{AndroidTabContent,tabSlideDirection,AndroidCollapsible}`, which didn't exist —
compile failure (`Unresolved reference`), confirmed. Implemented `tabSlideDirection`/
`AndroidTabContent` (`AndroidContent.kt`) and `AndroidCollapsible` (`AndroidWidgets.kt`) — compiled,
then two more RED rounds before GREEN:
1. Both Compose tests first mutated a plain `var … by mutableStateOf(...)` captured from outside
   `setContent`, and reading the node right after `mainClock.advanceTimeBy(...)` found nothing —
   the mutation never went through Compose's own snapshot/recomposition path from inside a
   `setContent` frame. Fixed by mutating the state through a real `performClick()` on a `Button`
   inside the composition (the same idiom `AndroidFlipCardGestureTimingTest` already uses), which
   recomposes correctly once the clock advances.
2. `reducedMotionShowsContentImmediatelyWithoutWaitingForAnimation` then failed with
   `advanceTimeBy(16)` — 16ms is *less* than one fake-clock frame period, so zero frames actually
   advanced and the post-click recomposition never ran. Bumped to `advanceTimeBy(32)` (still
   nowhere near the real ~220ms animated path, so the "immediate" assertion stays meaningful) — GREEN.
Final run: 8/8 across both new files.

**Checks (lane-android worktree, `kotlin/`)**:
- `./gradlew :androidApp:testDebugUnitTest --tests "dev.polski.grammarmatrix.AndroidTabTransitionTest"
  --tests "dev.polski.grammarmatrix.AndroidCollapsibleComposeTest"` — FAIL (compile error, symbols
  didn't exist) → FAIL (2 Compose-timing failures, see above) → PASS (8/8) — RED→RED→GREEN.
- `./gradlew :androidApp:testDebugUnitTest` — PASS, full `androidApp` unit suite green (no
  regressions in the D1–D3 suites this touches indirectly via `AndroidTrainingScreen.kt`).
- `./gradlew :androidApp:assembleDebug` — PASS.
- On-device (`emulator-5554`, `Polski_ARM35`, debug APK installed and launched, driven via `adb
  shell input tap`, `uiautomator dump`, `screencap`): tapped Матрица then Прогресс in the bottom
  nav — `uiautomator dump` confirmed `selected="true"` moves to the tapped item each time and the
  screen content matches (Matrix's "Грамматическая матрица"/case tables, then Progress's
  "Обзор"/skills list) — indicator and content stay in sync, no stuck/duplicated screen. On the
  Training tab: dismissed the intro card, scrolled to "Таблица под рукой", tapped it — button label
  flipped to "Скрыть таблицу" and the "Таблица этого предложения" card appeared below (screenshot);
  tapped again — button reverted to "Таблица под рукой" and the table disappeared (screenshot). No
  crash: `adb logcat` showed no `FATAL EXCEPTION`/`AndroidRuntime` for the app process across the
  whole session. (The ~300ms slide/220ms expand themselves are proven by the Robolectric tests
  driving the real animation clock frame-by-frame above — `adb`'s own tap→screencap round trip is
  too slow/racy over ADB to reliably catch a mid-transition frame, confirmed by two attempts that
  both landed on the already-settled frame.)

**Skipped**: settings-screen open/close (`showSettings`) is a separate, pre-existing instant
switch, not a bottom-nav tab — left untouched, out of this task's small/focused scope; a live
TalkBack pass over the new slide (semantics-tree behavior during an `AnimatedContent` transition is
Android's own well-tested machinery, not custom code here).
