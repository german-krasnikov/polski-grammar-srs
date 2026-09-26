# Lane iOS — evidence log

Worktree: `/Users/german/Work/JS/polski-lanes/ios` (branch `lane-ios`, from `main@e52320c` + WIP
`683da62`). Do not edit `Plans/Kotlin/FlipCardRivePlan.md` from this lane; this file is the lane's
own evidence log.

## I1 — D1 expand reveal (task/grammar cards, `FlashCardView`)

**Change:** `FlashCardView.swift` no longer does a native 3D flip for task/grammar cards. The
question stays on screen; the answer/explanation/"Что изменилось"/"ЗАПОМНИ"/rating panel unfolds
below it with a spring insertion transition (`.move(edge: .top)` + opacity), staggered per section
via `AnyTransition.animation(_:)` (`staggeredReveal(index:)`, ~50ms/step). Reveal is one-directional
and exactly-once: a tap on the question (`questionHeader`, gated to `phase == "Question" &&
!introPending`) sends the same `"reveal"` action the button already did; once `phase == "Revealed"`
the gesture is gone, and tapping the now-visible answer panel is a no-op (no flip-back). `reduceMotion`
(system Reduce Motion or the app's own `Motion.Reduced`) snaps the reveal, matching the old flip's
gate.

`questionHeader` (sentence + prompt) and `answerControls` (retrieve hint, "Ответ" Picker,
typed-answer `TextField`, reveal button) are now **siblings**, not ancestor/descendant — the tap
gesture lives only on `questionHeader`, mirroring the web host's own `front`/`answer-area` DOM
split (`TrainingWebApp.kt`). This deliberately avoids re-introducing the historical regression FC2
found for the old flip's tap gesture (an `.onTapGesture` ancestor of the Picker broke its
menu-style "Напечатать" option) — verified with a dedicated regression test, see below.

**Removed as dead weight of the flip going away:** `flipped`/`rotation`/`showBack` state,
`setFlipped`, the two-phase `withAnimation` swap, `rotation3DEffect`, the `POLSKI_FLIP_DEBUG_SCALE`
mid-flip sampling seam (no longer meaningful — there is no swap point to sample), and the
`onRingsExpandedChange` callback (`FlashCardView`'s public surface no longer has a "flip in
progress" moment to report).

**`PolskiGrammarApp.swift` (`TrainingView`) call site:** dropped `ringsExpanded` state, the
`RiveFlipRingsOverlay(expanded:)` composition and the `onRingsExpandedChange` argument — nothing
drives that overlay any more. `RiveFlipRingsOverlay` itself (`RiveEffectOverlay.swift`) is left in
place but now unused/dead, with its doc comment updated to say so; removing it and `rings.riv`
entirely is D3's job (ring effect removal), out of this task's scope. `RiveEffectOverlay`
(confetti/again on rating) and `RiveChainCompleteOverlay` are untouched — unrelated to the flip.

**UI tests updated** (all pre-existing tests that encoded the removed flip-back behavior):
- `PolskiGrammarUITests.swift`: `testTappingRevealedCardFlipsTwiceWithoutExtraReviewThenRatingCountsOnce`
  → renamed `testTappingRevealedCardDoesNothingThenRatingCountsOnce`; now asserts the question stays
  visible after reveal, a tap on the revealed answer is a no-op (doesn't hide it, doesn't re-offer
  Reveal), and exactly one rating is still recorded.
- `FlipCorrectnessUITests.swift`: FC2-05's mid-flip-swap-timing proof is moot (no swap point any
  more) — replaced with two D1-focused tests: `testTappingTheQuestionRevealsTheAnswerOnce` (tap the
  question sentence itself reveals, once, without hiding the question) and
  `testQuestionTapGestureDoesNotBreakTheAnswerModePicker` (the sibling-not-ancestor regression
  check above — switches to "Напечатать" and types into the field while the tap-to-reveal gesture
  is attached).
- `FlipRivePerfUITests.swift`: removed `flipRoundTrip` and the 5x flip-cost loop in `runVariant`
  (it repeatedly tapped back→front→back on the same revealed card, which no longer exists without a
  flip-back); the rate-cycle loop, which already re-reveals a fresh card each iteration, is
  untouched and still exercises reveal cost. Variants A/B/C and the launch metric test are otherwise
  unchanged.

## Verification

Working directory for all commands: `/Users/german/Work/JS/polski-lanes/ios/kotlin`.
`JAVA_HOME=$(/usr/libexec/java_home -v 21 -a arm64)`, simulator
`4384946F-9E6B-43D0-ADA3-CA219A3456B8`, `-derivedDataPath /private/tmp/lane-ios-dd`.

| Check | Command | Result |
|---|---|---|
| Build | `xcodebuild ... build` | PASS |
| RED→GREEN: D1 tap-reveal | `-only-testing:PolskiGrammarUITests/FlipCorrectnessUITests` | PASS (2 tests) |
| RED→GREEN: no flip-back, rating counts once | `-only-testing:.../testTappingRevealedCardDoesNothingThenRatingCountsOnce` | PASS |
| Regression: typed-answer input, Picker+TextField unaffected | `-only-testing:.../testTypedPolishAnswerUsesNativeInput` | PASS |
| Regression: method switch keeps typed draft through reveal+review | `-only-testing:.../testMethodSwitchKeepsTypedDraftThroughRevealAndOneReview` | PASS |
| Regression: intro-pending card hides reference until continue | `-only-testing:.../testFirstMethodIntroductionKeepsReferenceAnswerHiddenUntilContinue` | PASS |
| Regression: swipe rating (Again/Good) still advances once per gesture | `-only-testing:.../testBinaryRatingSwipesAdvanceOnceInEachDirection` | PASS |

Not run (out of scope/lean mode): `FlipRivePerfUITests` variants A/B/C (long-running perf
measurement, mechanically edited only — build success covers compile correctness); Android/macOS/web
hosts (other lanes' worktrees).

Not regenerated: `kotlin/iosApp/generate_project.rb` — no files added/removed, only existing file
contents changed, so the `.xcodeproj`'s file list didn't need touching.

## Open follow-ups (not this task)

- D3: remove `RiveFlipRingsOverlay`, `rings.riv` and the "again.riv" ring wiring entirely.
- D3/D5: swipe-rating visual polish (tint+label growing with drag distance, snap-back, fly-out) —
  the existing `DragGesture` threshold-rate behavior in `answerFace` is preserved as-is, not
  upgraded.
