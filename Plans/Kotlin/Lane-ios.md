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

## I2 — D2 whole-card flip (vocabulary cards, `VocabularyCardView`)

**Change:** new `VocabularyCardView.swift`, mirroring the web reference host's own flip contract
(`kotlin/composeApp/src/webMain/kotlin/polski/ui/VocabularyWeb.kt`'s `FlipCard`/
`installTapGesture` — read, not edited) natively via `rotation3DEffect`:
- The whole rounded panel — background, border, corner radius, shadow — is attached to the
  view's own **outer** container and rotates as one object; only the face *content* underneath
  swaps.
- Unrevealed: no separate "Показать ответ" button any more. The prompt block (eyebrow + word) is
  the accessible reveal control — `accessibilityLabel("Показать ответ")`, `.isButton` trait,
  identifier `vocabularyReveal` (the same identifier the old `Button` used, so callers/tests keep
  working unchanged). A tap on it dispatches `sendVocabulary("reveal")`; the visual flip itself
  stays downstream of the domain `revealed` flag turning true (`onChange(of: revealed)`), the same
  "visual state never drives itself" contract I1/D1's reveal uses. The gesture lives on the prompt
  block only — a **sibling** of the mode `Toggle` / typed `TextField` / "Проверить" button, never
  their ancestor — the same split D1 already established to avoid the FC2 class of bug (an
  ancestor tap gesture breaking a sibling Picker/TextField).
- Typed mode keeps an explicit "Проверить" button (same `"reveal"` action) alongside the
  tap-to-reveal prompt; taps inside the `TextField` never flip (it's a sibling, never wrapped).
- Revealed: tapping the card again flips it back and forth **purely visually** (`flipped`/
  `showBack`) — it never re-reveals and never itself rates; the "Повторить"/"Вспомнил" rating
  buttons on the answer face are unaffected by this gesture.
- Face swap at exactly 90° of the 180° rotation, both directions: `showBack` flips at the
  animation's halfway point (`flipDuration / 2`), the discrete swap a CSS 3D flip does. `reduceMotion`
  (system Reduce Motion or `Motion.Reduced`, `VocabularyView.cardMotionReduced`, mirroring
  `TrainingView`'s own) snaps the flip instead of animating it — D5's motion gate.
- A new due item (`state.currentId` changing) always resets to question-side-up, unanimated —
  mirrors D1's own `onChange(of: card.string("id"))`.

**`PolskiGrammarApp.swift` (`VocabularyView`) call site:** the "Карточка" section's inline
oral/typed/revealed branching (Toggle, TextField, "Показать ответ" button, answer fields, rating
`HStack`, `.swipeActions`) is replaced by one `VocabularyCardView(...)` call. `.swipeActions` was
dropped — it's a List-row-only modifier that doesn't apply to a custom flip container, and no test
exercised it (D3's drag-swipe rating rework is out of this task's scope; the two rating buttons are
otherwise preserved as-is). `reduceMotion`/`cardMotionReduced` added to `VocabularyView`, matching
`TrainingView`.

**Out of scope (D3/D4, not touched):** swipe-to-rate on the vocabulary card, touch-vs-pointer
rating-button visibility, Rive rating effects on vocabulary (none exist yet on any host for
vocabulary — training-only today), tab-switch paging.

**New UI tests** (`VocabularyFlipUITests.swift`, fixture word `noun.book` — deliberately **not**
`noun.wife`, which `PolskiGrammarUITests.testNativeVocabularyRevealAndBinaryRating` already owns;
sharing one FSRS-scheduled word between test files starved the later one of a due card when both
ran in the same `xcodebuild test` invocation, confirmed by reproducing the collision and fixing it
by switching fixture words — not a product bug):
- `testTapOnUnrevealedCardRevealsAndFlipsOnce`: unrevealed, `vocabularyReveal` exists and
  `vocabularyAgain`/`vocabularyGood` don't; one tap reveals+flips, both rating buttons become
  hittable, and `vocabularyReveal` itself is gone (reveal is exactly once).
- `testTappingRevealedCardFlipsVisuallyWithoutRating`: after reveal, a tap on the answer face's own
  content (a dedicated non-button accessibility id, `vocabularyAnswerFace`) does not change
  "Мой словарь · N" and leaves both rating buttons intact — proving the flip-back is visual-only,
  never a second reveal or a rating. Ends by rating "Again" (not "Good") on purpose, so the shared
  fixture word stays due again soon for repeat runs, the same choice the pre-existing
  `testNativeVocabularyRevealAndBinaryRating` already makes for the same reason.

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
| I2 build | `xcodebuild ... build` (after adding `VocabularyCardView.swift`) | PASS |
| I2 RED→GREEN: tap-to-reveal-and-flip, exactly once | `-only-testing:PolskiGrammarUITests/VocabularyFlipUITests/testTapOnUnrevealedCardRevealsAndFlipsOnce` | RED (missing `vocabularyReveal`/scroll target before the fix — see below) → PASS |
| I2 RED→GREEN: flip-back is visual-only, no re-rating | `-only-testing:.../testTappingRevealedCardFlipsVisuallyWithoutRating` | RED (tap target not hittable/found before adding `vocabularyAnswerFace`) → PASS |
| I2 regression: pre-existing vocabulary reveal+rating flow (`noun.wife`, old button identifier reused) | `-only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testNativeVocabularyRevealAndBinaryRating` | PASS (run together with `VocabularyFlipUITests` in one invocation, confirming both fixture words coexist without collision) |
| I2 regression: D1 training-card flip untouched | `-only-testing:PolskiGrammarUITests/FlipCorrectnessUITests` | PASS (2 tests) |

RED evidence for I2: the first `VocabularyFlipUITests` run (before the `vocabularyAnswerFace`
accessibility id and the `noun.book`/`noun.wife` fixture split existed) failed —
`testTappingRevealedCardFlipsVisuallyWithoutRating`: "Failed to not hittable: StaticText ...
label: 'Форма'", then (after the fixture-id fix but before the word split) the pre-existing
`testNativeVocabularyRevealAndBinaryRating` failed with "Failed to tap 'vocabularyReveal' Button:
No matches found" — both fixed as described above, then all three tests verified passing together.

Not run (out of scope/lean mode): `FlipRivePerfUITests` variants A/B/C (long-running perf
measurement, untouched by I2); Android/macOS/web hosts (other lanes' worktrees); D3/D4/D5 behavior
this task doesn't touch (swipe rating, tab paging, Rive effects on vocabulary).

Not regenerated for I1: `kotlin/iosApp/generate_project.rb` — no files added/removed there, only
existing file contents changed. For I2, `generate_project.rb` **was** updated (added
`VocabularyCardView.swift` to the app target's source list and `VocabularyFlipUITests.swift` to the
UI test target's) and re-run to add both new files to the `.xcodeproj`.

## Open follow-ups (not this task)

- D3: remove `RiveFlipRingsOverlay`, `rings.riv` and the "again.riv" ring wiring entirely.
- D3/D5: swipe-rating visual polish (tint+label growing with drag distance, snap-back, fly-out) —
  the existing `DragGesture` threshold-rate behavior in `answerFace` is preserved as-is, not
  upgraded.
