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
