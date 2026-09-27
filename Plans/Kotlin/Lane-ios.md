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

## Open follow-ups (not I1/I2 — resolved by I3 below)

- D3: remove `RiveFlipRingsOverlay`, `rings.riv` and the "again.riv" ring wiring entirely.
- D3/D5: swipe-rating visual polish (tint+label growing with drag distance, snap-back, fly-out) —
  the existing `DragGesture` threshold-rate behavior in `answerFace` is preserved as-is, not
  upgraded.

## I3 — D3 whole-card swipe rating (touch), Rive rating effects on vocabulary, ring removal

**Change:** new `SwipeToRate.swift` — a `ViewModifier` shared by `FlashCardView`'s answer panel and
`VocabularyCardView`'s answer face (mirroring the web host's own `installSwipeCard`/
`appendSwipeLabels`, `WebSwipeRating.kt`, read, not edited): the whole revealed face follows the
finger (translate + a small tilt, `.offset`/`.rotationEffect`), a red/green tint + "Повторить"/
"Вспомнил" label grows with drag distance, it snaps back under an 80pt threshold and flies
off-screen on commit — the domain rating dispatches only *after* the fly-out finishes (~220ms),
matching the web host's own commit-then-settle order. Both call sites pass `reduceMotion` (D5's
existing gate) to make the whole thing instant when motion is reduced/off.

- `FlashCardView.answerFace`: the two "Повторить"/"Вспомнил" `Button`s (with their FSRS interval
  previews) are gone; `.swipeToRate(active: phase == "Revealed", ...)` replaces the old plain
  `DragGesture` that used to live there. The existing "Свайп влево — повторить · вправо —
  вспомнил" hint keeps the `ratingSwipeArea` identifier (now the *only* rating UI on this panel).
- `VocabularyCardView`: the "Повторить"/"Вспомнил" `Button`s in `answerFace` are replaced by the
  same kind of hint text (`ratingSwipeArea`). `.swipeToRate` is attached at the view's **outer**
  container (after the flip's own `rotation3DEffect`, background/border/shadow), not inside
  `answerFace`, so a rating drag carries the whole panel D2 already made "one object" for the flip
  — `active: showBack` keeps a drag on the still-showing question face inert.
- The `ratingSwipeArea` identifier itself lives on each call site's own hint `Text`, never inside
  `SwipeToRate` on the shared, multi-child content — see the file's own doc comment: setting it on
  the container let SwiftUI inherit it onto several plain, unidentified descendant `Text`s at
  once, and a `firstMatch` query resolved to whichever tiny caption came first in traversal order
  (reproduced: `"ЗАПОМНИ"`, a 43×14pt caption on `FlashCardView`) — a synthesized
  `swipeLeft()`/`swipeRight()` then travels only that caption's own width, well under the 80pt
  threshold. A real finger gesture is unaffected either way; this only matters for XCUITest's own
  frame-relative gesture synthesis.
- `DragGesture(minimumDistance: 18)`, not a lower value: `.simultaneousGesture` already keeps this
  from blocking the enclosing `Form`'s own scroll, but a lower `minimumDistance` measurably ate
  into that scroll's own effective distance per gesture on a reference-table-heavy screen
  (reproduced with an XCUITest scroll loop that reliably reached a target at a fixed swipe count
  before this, needed more after lowering it, and reached it again at 18).

**Rive rating effect on vocabulary (parity with the web host, which already fires
`cardEffectFor(rating)` for vocabulary too — `VocabularyWeb.kt`):**
`IosVocabularySession` (`shared/iosMain`) gets an `onEffect: ((String) -> Unit)?` mirroring
`IosSession.onEffect` exactly — fires once per *accepted* rating (`VocabularySession.rate`'s own
returned `Boolean`) with `cardEffectFor`'s mapped name, using the same shared
`polski.presentation.cardEffectFor` as the training card so there is one mapping, not two. Its
constructor now takes an injectable `defaults: NSUserDefaults = .standard` (mirroring `IosSession`),
additive/backward-compatible — needed so a test can point it at an isolated suite instead of the
real device's `standardUserDefaults`; `AppModel`'s own call site is updated to
`IosVocabularySession(defaults: .standard)` (Kotlin's default-parameter value isn't visible from
Swift, so every call site there — like `IosSession`'s own — always passes it explicitly).
`AppModel` gets a second published `vocabularyCardEffect` (kept separate from the training card's
own `cardEffect` so a rating on one card never re-triggers the other's overlay purely because
`CardEffectEvent` is `Equatable`), and `VocabularyView`'s "Карточка" section wraps
`VocabularyCardView` in a `ZStack` with a second `RiveEffectOverlay`, gated by the same
`cardMotionReduced` `VocabularyView` already computes for the flip (no debug-only variant-B
override — that's training-only measurement instrumentation, out of scope here).

**Ring effect removed entirely (D3):** `RiveFlipRingsOverlay` (already dead code since D1 — no
call site) deleted from `RiveEffectOverlay.swift`; `Rive/rings.riv` deleted; `generate_project.rb`
no longer vendors it into the bundle (list and comment updated, project regenerated). Not touched:
`THIRD_PARTY/credits.md` / `Plans/Kotlin/RiveCatalog.md` — both are shared reference docs the
Android/macOS lanes' own D3 passes will each also want to update for their own `rings.riv` removal;
editing them here risked a three-way merge conflict on the same lines for no functional gain (the
build itself no longer references the file regardless of what the docs say).

**Kotlin (`shared/iosMain`/`iosTest`), TDD:**
- RED→GREEN: `IosVocabularySessionTest.goodRatingFiresTheRememberedEffectOnceAndSkipsAnUnrevealedRate`
  and `.againRatingFiresTheAgainEffect` (new file, mirrors `IosSessionTest`'s own two `onEffect`
  tests) — reverted the `onEffect?.invoke(...)` wiring in `IosVocabularySession.dispatch` back to
  the plain `session.rate(...)` call to confirm both fail first (`kotlin.AssertionError`, empty/
  wrong `effects` list), then restored it: GREEN, 2/2.
- Regression: `IosSessionTest` (3/3) and `IosVocabularyRepositoryTest` (2/2) unaffected by the
  `IosVocabularySession(defaults:)` constructor change.

**iOS UI tests, TDD and fixes found along the way (all against a freshly-`simctl uninstall`ed app —
this simulator's own device state, not just `resetTrainingProgress`, mattered for a couple of
these):**
- Updated for "no rating buttons": `testBinaryRatingSwipesAdvanceOnceInEachDirection` (now asserts
  `rateAgain`/`rateGood` don't exist, was checking dead identifiers `rateHard`/`rateEasy` that
  never matched anything — a pre-existing vacuous assertion, not something I3 broke), a new shared
  `rateViaSwipe(app:good:)` helper replacing every other test's `rateGood.tap()`-based "advance one
  review" mechanism, `testNativeVocabularyRevealAndBinaryRating` (asserts `vocabularyAgain`/
  `vocabularyGood` absent, rates via a `ratingSwipeArea` drag instead of a button tap), and
  `VocabularyFlipUITests`'s two tests (`vocabularyAnswerFace`/`ratingSwipeArea` in place of the
  removed buttons).
- `rateViaSwipe` uses a coordinate-based drag (`XCUIElement.coordinate(withNormalizedOffset:)` +
  `press(forDuration:thenDragTo:)`), not `.swipeLeft()/.swipeRight()`: reproduced the convenience
  method failing ("visible frame is empty") when the swipe zone sits right at the Form's own
  scrolled edge (`testFirstMethodIntroductionKeepsReferenceAnswerHiddenUntilContinue`, which also
  shows the reference table) — a coordinate drag between two explicit offsets inside the element's
  frame doesn't hit that path. Also bumped its own scroll-hunt loop 7→10 swipes (dropping the two
  rating buttons shifted how far this test's own *preceding* scroll steps leave the now-shorter
  panel) and `continueIntroductionIfPresent`'s post-swipe wait 0→1.5s (a caller right after a swipe
  must give the fly-out's own ~220ms commit delay room to actually land the next card, or a
  following card that needs its own introduction is never seen to tap — reproduced both a miss at
  0.5s under load and clean passes at 1.5s across three fresh-install runs).
- `VocabularyCardView`'s own reveal→scroll-hunt has the same class of race: reproduced a swipe
  landing before the flip's own ~0.25s face-swap settles overscrolling straight past this
  single-screen vocabulary card into the catalog list below it (the query then finds nothing at
  all, not just "off-screen" — likely cell reuse once scrolled that far). Fixed with a bounded
  `waitForExistence(timeout: 1)` before any scrolling in `testNativeVocabularyRevealAndBinaryRating`
  and both `VocabularyFlipUITests` tests.
- `VocabularyFlipUITests.testTappingRevealedCardFlipsVisuallyWithoutRating` rewritten: reproduced
  (twice, not a fluke) that a tap on the answer face reliably flips it *all the way* back to the
  question face by the time the very next assertion runs (this host's own tap-driven flip-back has
  no way back to the answer face afterward via tap — pre-existing `VocabularyCardView` behavior
  from I2, untouched here) rather than leaving the answer face showing, which the old assertion (and
  its D2-era predecessor checking the rating buttons) assumed. The test now asserts what actually,
  reproducibly happens — flips away, no rating — and drops the follow-on rating dance instead of
  papering over the mismatch; `testNativeVocabularyRevealAndBinaryRating` (a different fixture word)
  is what verifies the swipe-to-rate drag itself.

## Verification

Working directory for all commands: `/Users/german/Work/JS/polski-lanes/ios/kotlin`.
`JAVA_HOME=$(/usr/libexec/java_home -v 21 -a arm64)`, simulator
`4384946F-9E6B-43D0-ADA3-CA219A3456B8`, `-derivedDataPath /private/tmp/lane-ios-dd`. Several iOS UI
runs below used a freshly `xcrun simctl uninstall`ed app first — noted where it mattered.

| Check | Command | Result |
|---|---|---|
| Kotlin RED→GREEN: vocabulary `onEffect` (Good) | `:shared:iosSimulatorArm64Test --tests polski.ios.IosVocabularySessionTest` | RED (reverted wiring, both tests failed) → GREEN (2/2) |
| Kotlin regression | `--tests polski.ios.IosSessionTest,polski.ios.IosVocabularyRepositoryTest` | PASS (5/5) |
| iOS build | `xcodebuild ... build` (after adding `SwipeToRate.swift`, removing `rings.riv`) | PASS |
| iOS regression: D1 training-card flip | `-only-testing:PolskiGrammarUITests/FlipCorrectnessUITests` | PASS (2/2) |
| iOS D3 direction contract | `-only-testing:.../testBinaryRatingSwipesAdvanceOnceInEachDirection` | RED→GREEN, then 3 more fresh-install repeats, all PASS (see fixes above) |
| iOS regression: rating still advances chain/typed/reference flows | `testTappingRevealedCardDoesNothingThenRatingCountsOnce`, `testTypedPolishAnswerUsesNativeInput`, `testNativeTrainingMatrixAndProgress`, `testSaveFailureShowsErrorBannerOnEveryTabWithExportReachable`, `testFirstMethodIntroductionKeepsReferenceAnswerHiddenUntilContinue` | PASS (5/5) |
| iOS D3 vocabulary swipe rating | `testNativeVocabularyRevealAndBinaryRating` (fresh install) | RED→GREEN (see fixes above) |
| iOS regression: D2 vocabulary flip | `-only-testing:PolskiGrammarUITests/VocabularyFlipUITests` (fresh install) | RED→GREEN (see fixes above), then PASS again combined with the group above |
| Not run (lean mode) | `FlipRivePerfUITests` (long perf measurement, untouched); Android/macOS/web hosts | — |
| Known pre-existing, unrelated flake | `testS6VocabularyFileImporterCancellationKeepsSelection` — reproduced failing identically at `HEAD` (before any I3 change, fresh install): a system `UIDocumentPickerViewController`'s own "Отменить" never becomes queryable, unrelated to D3 | not this task's regression |

RED evidence: `IosVocabularySessionTest`'s two new tests, reverting only the `onEffect?.invoke(...)`
lines (keeping the new constructor param so the test still compiles) — both failed with
`kotlin.AssertionError`. `testBinaryRatingSwipesAdvanceOnceInEachDirection` against the pre-I3 `HEAD`
commit (`7adcdcc`, via `git stash`) passes; against my first `SwipeToRate` cut (identifier on the
shared container, `minimumDistance: 10`, no `continueIntroductionIfPresent` wait) it failed two
different ways before each fix above, described inline.

## I3 correction — VoiceOver rating path (reviewer blocker on commit `177166b`)

**Blocker:** I3's `SwipeToRate` is a bare `DragGesture` with no accessibility counterpart.
VoiceOver intercepts raw finger drags for its own navigation (it never forwards them to the app),
so a VoiceOver user had no way at all to rate a card once the two rating buttons were removed —
`SwipeToRate.swift`, `FlashCardView.answerFace`'s and `VocabularyCardView`'s own call sites.

**Fix:** both call sites' `ratingSwipeArea` hint `Text` (the one long-text leaf, not the shared
gesture container — see that leaf's own doc comment for why it, specifically, carries per-element
accessibility state) now also carries two custom accessibility actions, "Повторить" (`remembered:
false`) and "Вспомнил" (`remembered: true`), reachable via VoiceOver's Actions rotor once VoiceOver
focus lands on that hint — no visible button is added, keeping D3's "no rating buttons on touch"
intact. Each call site's own rating logic (previously inlined in `swipeToRate`'s trailing closure)
is factored into a private `rate(remembered:)` on the view itself, reused by both the accessibility
actions and the drag's own `onRate` closure, so there is exactly one rating code path per card, not
two: `FlashCardView.rate` keeps the existing `phase == "Revealed"` guard, `VocabularyCardView.rate`
keeps the existing `!busy` guard.

**API choice, reproduced:** `.accessibilityAction(named:) { }` (attached directly, two calls) is
what a first cut used and is what the correction's own suggested direction names — but it
reproducibly broke `testBinaryRatingSwipesAdvanceOnceInEachDirection` 4/4 times (fresh install
each time): after the first `rating.swipeLeft()`, `continueIntroductionIfPresent`'s tap on
"Перейти к заданию" (the next card's intro-continue button, wholly unrelated to the rating panel)
silently had no effect, leaving that button on screen and failing the next `revealAnswer` lookup.
Every one of those failures logged the same runtime note right at the failure point:
`Automation type mismatch: computed Button from legacy attributes vs PopUpButton from modern
attribute` — an XCTest accessibility-snapshot artifact, reproduced correlating with *every* run in
this session that had any `.accessibilityAction`/`.accessibilityActions` anywhere on screen and
*no* run without one (checked across this session's own run logs). Switching to the ViewBuilder
form, `.accessibilityActions { Button(...) { } }`, cut the failure rate sharply (2/2 solo passes)
but not to zero — it still failed once more in a 7-test batch run, same symptom, same line. Since
this is an XCTest automation-snapshot instability, not a VoiceOver or product behavior difference
(a real VoiceOver user's rotor action is unaffected either way), the fix that actually eliminated
it was hardening the test helper itself: `continueIntroductionIfPresent`'s final `next.tap()` is
now a bounded tap-and-verify retry (up to 3 attempts, each re-checking `next.exists` /
`waitForExistence` after tapping) instead of one unconditional tap — 3/3 solo passes and 1/1 clean
7-test batch pass afterwards, described inline in that helper's own comment.

## Verification (I3 correction)

Working directory: `/Users/german/Work/JS/polski-lanes/ios/kotlin`. Same
`JAVA_HOME`/simulator/`-derivedDataPath` as above. Every UI run below used a freshly `xcrun simctl
uninstall`ed app first.

| Check | Command | Result |
|---|---|---|
| iOS build | `xcodebuild ... build` (after each edit below) | PASS at each step |
| Bisect: `.accessibilityAction(named:)` (first cut) | `-only-testing:.../testBinaryRatingSwipesAdvanceOnceInEachDirection` ×4 | FAIL ×4, same symptom (`continueIntroductionIfPresent`'s tap on the next card's intro button silently no-ops) |
| Bisect: extraction only, no accessibility action at all | same test ×2 | PASS ×2 (confirms the `rate(remembered:)` extraction itself is not the cause) |
| Bisect: `.accessibilityActions { Button }` (before hardening `continueIntroductionIfPresent`) | same test ×2 solo, then ×1 inside the 7-test batch below | PASS ×2 solo, FAIL ×1 in the batch (same symptom) |
| Baseline (pre-I3-correction `HEAD`, `177166b`, `git stash`) | same test ×3 | PASS ×3 — confirms the instability is not pre-existing flakiness at this rate |
| Final: `.accessibilityActions { Button }` + hardened `continueIntroductionIfPresent` retry | same test, solo | PASS ×3 |
| Final, combined regression | `testBinaryRatingSwipesAdvanceOnceInEachDirection`, `testTappingRevealedCardDoesNothingThenRatingCountsOnce`, `testTypedPolishAnswerUsesNativeInput`, `testNativeTrainingMatrixAndProgress`, `testSaveFailureShowsErrorBannerOnEveryTabWithExportReachable`, `testFirstMethodIntroductionKeepsReferenceAnswerHiddenUntilContinue`, `testNativeVocabularyRevealAndBinaryRating` (one `xcodebuild test` invocation) | PASS 7/7 |
| iOS regression: D1/D2 flip suites | `-only-testing:PolskiGrammarUITests/FlipCorrectnessUITests`, `-only-testing:PolskiGrammarUITests/VocabularyFlipUITests` | PASS 4/4 |
| Not run (lean mode) | `FlipRivePerfUITests` (unrelated); Android/macOS/web hosts (this lane's own scope is iOS only); Kotlin `shared` tests (untouched by this correction — no Kotlin file changed) | — |

**Verification gap, disclosed rather than papered over:** confirming the fix from an actual
VoiceOver user's perspective (Accessibility Inspector or VoiceOver-on pass, as the correction
asked) needs interactive Simulator/device access this environment does not have — there is no
public XCTest API to invoke a named `UIAccessibilityCustomAction` or to drive the real VoiceOver
engine headlessly (checked: `XCUIElementAttributes` exposes no actions property, and
`XCUIAccessibilityAuditType.action` — the one audit type that flags "gesture with no accessible
action alternative" — is compiled out entirely on iOS/watchOS/tvOS/Simulator, macOS/Mac Catalyst
only, confirmed against `XCUIAccessibilityAuditTypes.h`). What is verified, automatically and
reproducibly, is: (1) code review — the two actions on each `ratingSwipeArea` element call the same
guarded `rate(remembered:)` the drag itself calls, so a rotor invocation rates exactly like a
successful swipe would; (2) the fix does not destabilize the touch rating path it sits next to
(table above). An interactive Accessibility Inspector pass on a real Mac/simulator session remains
the one remaining check for a human (or a session with Simulator UI access) to do.

## Open follow-ups (not this task)

- D4 (tab/screen paging) and D5 (the `UserPreferences.animationsEnabled` toggle's own iOS wiring,
  and unloading Rive entirely when it's off) are untouched — this task was D3 only.
- `THIRD_PARTY/credits.md` / `Plans/Kotlin/RiveCatalog.md` still list `rings.riv` for "Android and
  iOS" — each host's own D3 pass should update its own line once all three have landed, to avoid
  a three-way merge conflict on the same lines right now.

## I4 — D4 tab paging + animated reference-panel collapsible

**Change (tab paging, `PolskiGrammarApp.swift` + new `PagingTabBar.swift`):** replaced the plain
SwiftUI `TabView` with a custom paging container — `TabView` swaps tab content instantly with no
transition slot at all, so there is no way to make old/new content visibly slide together from it
(confirmed by inspection, not assumed: a `TabView` tab switch is a UIKit root-view swap, not
something `.transition`/`.animation` on a child can hook). The new container is a `ZStack` over
`tabOrder` (`["Training", "Matrix", "Progress", "Vocabulary"]`) that mounts **only** the active
tab's subtree (`if tab == model.state.string("tab")`) — matching `TabView`'s own accessibility
contract, where an inactive tab's elements are not independently discoverable (this is what keeps
the very large number of existing `app.buttons["Тренировка"].firstMatch.tap()`-style lookups
throughout the UI test suite from becoming ambiguous or from finding leftover elements of a tab
that isn't showing — a permanently-mounted side-by-side pager, the other standard approach, was
rejected specifically for this reason: it would keep every tab's Form/NavigationStack attached to
the accessibility tree simultaneously). `.transition(.asymmetric(insertion: .move(edge:
pagingEdge), removal: .move(edge: pagingEdge's opposite)))` drives the actual slide; `pagingEdge`
is computed in `.onChange(of: model.state.string("tab"))` from `tabOrder.firstIndex` of the old vs.
new tab (later index ⇒ slide in from `.trailing`, matching "old and new slide together in the
direction of the chosen tab"). `.animation(motionActive ? .easeOut(duration: 0.3) : nil, value:
model.state.string("tab"))` — not `withAnimation` around the `model.send("tab", …)` call — because
the model's own state update arrives through `IosSession.onState`'s `DispatchQueue.main.async`,
outside any `withAnimation` transaction that wrapped the call that triggered it; `.animation(_,
value:)` ties the animation to the value's diff itself, regardless of when the write actually
lands, matching the pattern `TrainingView.cardMotionReduced`/`.animation(_, value:
state.string("phase"))` already uses one call up. `motionActive` mirrors that same gate (system
Reduce Motion or `Motion.Reduced`), added at the `PolskiGrammarApp` scene level the same way
`scenePhase` already is (`@Environment` works on an `App`, not only a `View`).

**New `PagingTabBar.swift`:** replaces `TabView`'s own tab-bar chrome one-for-one — same four
labels/icons/tags, same tap contract. Each button's `.accessibilityLabel(item.title)` is set
explicitly (overriding whatever combined icon+text label SwiftUI would otherwise compute for a
`Button` containing both an `Image` and a `Text`), so `app.buttons["Тренировка"]` etc. keep
resolving to exactly one element with exactly that name — unchanged from what `Label(title,
systemImage:)`'s `.tabItem` produced. `generate_project.rb` hardcodes its source file list (not a
directory glob), so it needed one line added for the new file — found by the project failing to
build with "cannot find 'TabBarItem' in scope" until that line was added, not by inspection first.

**Trade-off, disclosed:** mounting only the active tab's subtree (needed for the accessibility
reason above) means each tab's own local `@State` that used to survive a round trip through
another tab (because `TabView` keeps every tab's view alive) now resets when you leave and come
back — concretely, `MatrixView.comparisonCase` (the "Сравнение типов склонения" filter, not
model-persisted) goes back to `"NOM"`. `TrainingView.localDraft`/`answerFocused` and
`VocabularyView.editing`/`deletingId` are unaffected (already resynced from model state on
`onAppear`, or tied to a sheet/alert that would close on leaving the tab anyway). No test in the
suite exercises `comparisonCase` surviving a tab round trip; flagged here rather than silently
accepted.

**Change (animated collapsible, `TrainingView`'s reference panel):** the existing "Таблица под
рукой"/"Скрыть таблицу" toggle's content (`referenceRows` + "Все таблицы и схема") is unchanged in
its own logic — same `model.send("reference")`, same `showReference`/`introPending` gating — only
*how it appears* changes. `Group { … }.transition(.opacity.combined(with: .move(edge: .top)))` on
the conditional content, `.animation(cardMotionReduced ? nil : .easeOut(duration: 0.25), value:
state.bool("showReference"))` on the enclosing `Section` (a bare `if` isn't an expression you can
chain `.transition` onto directly — that's a real compiler error, "cannot infer contextual base in
reference to member 'transition'", not a style choice — `Group` is what makes it one). No `inert`/
`aria-hidden`-equivalent step was needed here, unlike the web host's own version of this same panel
(`FlipCardRivePlan.md` §17.5, UX4-21): collapsed content is fully removed from the SwiftUI tree
(`Form` row deletion), not shrunk to zero height while still mounted, so there is nothing left for
VoiceOver or Tab focus to reach either way.

**Not touched (kept in scope):** `showSkillPicker`'s `ChoiceMenu` and the `mode == "Chain"` block in
the same top `Section` — the task named the reference panel's own web-plan precedent
(`reference-panel`, §17.5) as the collapsible to match; these two are a different, unnamed pattern
and adding animation to them wasn't asked for.

## Verification (I4)

Working directory: `/Users/german/Work/JS/polski-lanes/ios/kotlin`. Same `JAVA_HOME`, simulator
`4384946F-9E6B-43D0-ADA3-CA219A3456B8`, `-derivedDataPath /private/tmp/lane-ios-dd`.

| Check | Command | Result |
|---|---|---|
| Ruby project regen | `arch -arm64 ruby kotlin/iosApp/generate_project.rb` (after adding `PagingTabBar.swift` to its hardcoded file list) | PASS |
| iOS build | `xcodebuild ... build` | FAIL → FAIL (different error) → PASS, see compiler-error notes above |
| iOS: reference-panel toggle, fresh install | `-only-testing:.../testFirstMethodIntroductionKeepsReferenceAnswerHiddenUntilContinue` | PASS (49.9s) |
| iOS: tab-paging regression batch 1 (no fresh install between) | `FlipCorrectnessUITests`, `VocabularyFlipUITests`, `testBinaryRatingSwipesAdvanceOnceInEachDirection`, `testNativeTrainingMatrixAndProgress`, `testNativeVocabularyRevealAndBinaryRating`, `testSaveFailureShowsErrorBannerOnEveryTabWithExportReachable` | 7/8 PASS, 1 FAIL (`testNativeVocabularyRevealAndBinaryRating`) |
| Bisect: same vocabulary test, fresh install, solo | same test | PASS (26.0s) — confirms the batch-1 failure was leftover app state from the *preceding* tests in that same batch (this test's own I3 evidence already documents it needs a fresh install; not a D4 regression) |
| iOS: tab-paging regression batch 2, fresh install | `FlipCorrectnessUITests` (2), `VocabularyFlipUITests` (2), `testBinaryRatingSwipesAdvanceOnceInEachDirection`, `testNativeTrainingMatrixAndProgress`, `testSaveFailureShowsErrorBannerOnEveryTabWithExportReachable` | PASS 7/7 |
| Not run (lean mode) | `FlipRivePerfUITests` (long perf measurement, unrelated); `testNativeCasesShowCompactNoteAndOrderedComparisonNouns` and the rest of `PolskiGrammarUITests` not touched by tab paging (checked by reading: none of them tab away from Matrix and back, so `comparisonCase`'s reset doesn't affect them); Android/macOS/web hosts (this lane's own scope is iOS only) | — |

RED evidence: none in the TDD sense — there is no existing runner/assertion for "content slides
when switching tabs" or "reference panel animates" to turn red first (SwiftUI's own transition/
animation timing isn't something XCUITest asserts on in this suite, and adding a new one for it
was out of this task's small scope). What is verified, reproducibly, is the negative space: the
build errors above (`cannot find 'TabBarItem' in scope`, then `cannot infer contextual base in
reference to member 'transition'`) are real compiler-rejected first cuts, not invented after the
fact, and the batch-1/batch-2/bisect sequence above is genuine regression evidence, not assumed.

**Verification gap, disclosed:** confirming the slide direction/duration and the panel's
height/opacity curve *look* right (the ~300ms/easeOut timing, "no jank, no flashes") needs an
actual Simulator visual check or a screen recording — this environment has no interactive
Simulator UI access, only `xcodebuild test`'s pass/fail and the one screenshot attachment
`testNativeVocabularyRevealAndBinaryRating` already takes (unrelated to this feature). What is
verified automatically: the transition/animation modifiers compile and attach to the right nodes,
the four tabs remain reachable and correctly labeled under animation, and no existing flow (rating,
reveal, import/export, settings) regressed across two fresh-install runs.

## I5 — D5 animations toggle (`IosPreferencesSession` bridge, Settings "Анимации")

**Change (`shared/iosMain` + `iosTest`), TDD, RED→GREEN:** `IosPreferencesSession.currentSnapshot()`
now puts `animationsEnabled` (already a `UserPreferencesV2` field with a `true` decode default —
that part landed on `main` before this lane started, untouched here); `set(field:value:)` gains an
`"animationsEnabled" -> value.toBooleanStrictOrNull() ?: return "Неверное значение"` case,
mirroring the existing enum-field cases' shape. New test
`animationsEnabledDefaultsToTrueAndPersistsAcrossRestart` (`IosPreferencesSessionTest.kt`): default
snapshot value is `true`, an invalid value (`"maybe"`) is rejected with `"Неверное значение"` and
does not persist, `"false"` is accepted, and a freshly-constructed session against the same
`NSUserDefaults` suite (simulating relaunch) still reads `false` from both the live snapshot and
the exported JSON document.

**Change (`PolskiGrammarApp.swift`):**
- New `Record.bool(_:default:)` (alongside the existing `bool(_:)`, which defaults missing-key to
  `false`) — needed because `animationsEnabled`'s own decode default is `true`; using the plain
  `bool(_:)` would have made the empty `preferences` snapshot that exists for one frame during
  `AppModel.init` (before `receivePreferences` runs) read as animations-off.
- `motionActive` (tab paging, `PolskiGrammarApp` scene level) and both `cardMotionReduced`
  properties (`TrainingView`, `VocabularyView`) now also require
  `model.preferences.bool("animationsEnabled", default: true)` — the same three gates D4/earlier
  D5 doc comments already anticipated ("D4/D5" was already in `motionActive`'s comment before this
  task). Since `RiveEffectOverlay`/`RiveChainCompleteOverlay` were already lazily created only when
  `!reduceMotion` (I1/I3), and `riveEffectsSuppressed` already derives from `cardMotionReduced`,
  folding the new gate into these three properties is sufficient: turning the toggle off makes the
  card flip/reveal, the tab-paging slide, and the reference-panel collapse all instant, and no new
  `RiveViewModel` is ever constructed for a rating/chain-complete effect that fires while it's off.
- `IosSettingsView`'s "Внешний вид" section gets a `Toggle("Анимации", isOn: …)` bound through
  `model.setPreference("animationsEnabled", $0 ? "true" : "false")` — the same
  `AppModel.setPreference(field:value:)` bridge every other Settings control already uses (no new
  method needed there: it already just forwards to `preferencesSession.set` for fields with no
  extra domain side effect, exactly `animationsEnabled`'s case).

**Not done (disclosed, out of this task's stated scope — "off ⇒ RiveViewModel never created and
motion instant"):** an already-created `RiveViewModel` (from a rating that fired before the toggle
was switched off mid-session) is not actively disposed; it simply never receives another trigger
once the gate is on, the same way the pre-existing `Motion.Reduced`/system-Reduce-Motion gate has
always behaved. Rive state-machine playback is one-shot, not looping, so nothing keeps animating on
screen either way. Actively tearing down a live `RiveViewModel`/its Metal view on toggle-off (the
literal reading of the web host's `disposeRiveOnDisable`) would be a second, separable change; the
task's own scope line names only "never created" for the off state, which this satisfies without
it.

**iOS UI test, written after the Swift wiring (not strict RED→GREEN at this layer — there is no
pre-existing Swift/XCUITest unit-test target for `AppModel`, only the shared Kotlin bridge and full
`PolskiGrammarUITests` app-level suite; `testNativeAppearanceSettingsKeepsTrainingCard` already
covers the sibling pickers in the same "Внешний вид" section without needing an update for this
change):** `testAnimationsToggleDefaultsOnAndPersistsOffAcrossRelaunch` — opens Settings, asserts
the "Анимации" switch is on by default, turns it off, dismisses, force-terminates and relaunches
the app, reopens Settings, and asserts the switch is still off (proving the
`IosPreferencesSession`-backed persistence the Kotlin test already covers in isolation also holds
through this Settings screen's own bridge), then restores it to on for later tests in the same run.

## Verification (I5)

Working directory: `/Users/german/Work/JS/polski-lanes/ios/kotlin`. Same `JAVA_HOME`, simulator
`4384946F-9E6B-43D0-ADA3-CA219A3456B8`, `-derivedDataPath /private/tmp/lane-ios-dd`.

| Check | Command | Result |
|---|---|---|
| Kotlin RED→GREEN: `animationsEnabled` snapshot/set/persist | `:shared:iosSimulatorArm64Test --tests polski.ios.IosPreferencesSessionTest` | RED (`kotlin.NoSuchElementException`, key absent) → GREEN (5/5) |
| Kotlin regression | same invocation, full `IosPreferencesSessionTest` + `--tests polski.preferences.*` | PASS |
| iOS UI: toggle default/persist-across-relaunch | `-only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testAnimationsToggleDefaultsOnAndPersistsOffAcrossRelaunch` | started, did not finish inside this session's time budget — see gap below |
| Not run (lean mode) | `FlipRivePerfUITests`; the rest of `PolskiGrammarUITests`/`FlipCorrectnessUITests`/`VocabularyFlipUITests` beyond the new test (this change's own gates are compile-time additions to already-tested properties, not new branches those suites exercise); Android/macOS/web hosts | — |

**Verification gap, disclosed rather than papered over:** the one new XCUITest above was still
running (build + two full app launches with a terminate/relaunch in between) when this session's
time budget ran out — neither confirmed passing nor failing. What *is* verified: the Kotlin bridge
change with real RED→GREEN evidence (table above); by inspection, every call site the toggle needs
to reach (`motionActive`, both `cardMotionReduced`s, the `RiveEffectOverlay`/
`RiveChainCompleteOverlay` construction sites gated by them) is updated consistently with the
existing `Motion.Reduced` gate's own shape, so the same code path that's long since proven to make
motion instant and skip Rive creation for `Motion.Reduced` now also does so for
`animationsEnabled == false`; and the app target compiled successfully against these changes
(`xcodebuild … build` implicitly, as the first phase of the still-running `test` invocation, got
past the build phase — the process was observed in its test-execution phase, not stuck compiling).
The next session/lane step should re-run the command above (or the full
`PolskiGrammarUITests`/`FlipCorrectnessUITests`/`VocabularyFlipUITests` regression battery, per this
lane's own established pattern above) to turn this from "verified by inspection + Kotlin test" into
full on-device confirmation before this lane's own work is considered done.

## I8 — iPad-only failures in `testFirstMethodIntroductionKeepsReferenceAnswerHiddenUntilContinue` and `testNativeVerbGenderControlChangesSelectedSubjectOnly`

**Diagnosis, not a product bug:** neither test's screen has any iPad-specific code path — no
`NavigationSplitView`, size-class or idiom branch exists anywhere in `PolskiGrammarApp.swift`,
`FlashCardView.swift` or `SwipeToRate.swift`; both screens are the same `Form` on every device.
Confirmed with real RED evidence (both methods, iPad Pro 11, `-collect-test-diagnostics never` to
keep each iteration under a minute instead of the default ~10-minute sysdiagnose collection):

- `testFirstMethodIntroductionKeepsReferenceAnswerHiddenUntilContinue`: `rateViaSwipe`'s existing
  10-swipe hunt for `ratingSwipeArea` failed with XCTest's own `"Failed to get matching snapshot:
  No matches found"` — not merely off-screen, genuinely unmounted from the lazy `Form`. Raising the
  count to 16 made it *worse*: the debug snapshot at failure showed the viewport sitting at the
  screen's absolute last row (the reference table's "Все таблицы и schema" button, then the tab
  bar) with the zone never having existed — the fixed-size `app.swipeUp()` convenience covers
  proportionally more content on iPad Pro 11's taller frame than on iPhone, so the same count that
  reliably lands inside the zone's narrow mounted window on iPhone jumps clean over it on iPad.
- `testNativeVerbGenderControlChangesSelectedSubjectOnly`: the original file's line 716 (shifted by
  this lane's earlier I6/I7 edits) was the *final* assertion — `"Было: robić; Стало: robił"` waited
  for plainly, with no scroll at all, right after a swipe loop that found the *adjacent* `"on — он"`
  label. That adjacent pair straddled the lazy Form's mount boundary on iPad only.

**Fix (test-only, `PolskiGrammarUITests.swift`):** for the second test, added the same
scroll-until-exists loop already used for every other lookup in the file before the final
assertion — a plain omission, not a device-specific branch.

For `rateViaSwipe`, the real story took several iterations to land on, each with its own RED
evidence, because the first few plausible-looking fixes reproduced *worse* than the original:

1. A fixed swipe count in one direction (10, then 16) sometimes scrolled clean *past* the zone's
   narrow mounted window in a lazy `Form` before ever reading it — a debug snapshot at failure
   showed the viewport at the screen's absolute last row (the reference table's own last button,
   then the tab bar), zone never having existed. Raising the count only got there faster.
2. A "self-correcting" hunt (reverse direction once the zone is seen to exist, then vanish) doesn't
   help if the zone is *never* seen existing in the first place — which turned out to be the
   dominant failure mode: a debug snapshot taken right after `reveal.tap()` (before any scrolling)
   showed the zone already mounted with a small **negative**-Y frame, i.e. just above the top edge.
   A full-screen `app.swipeUp()` moves far more than that frame's own height, so the very first
   guess (forward, matching every simpler screen) throws it straight out of the lazy `Form`'s mount
   buffer before the loop ever gets a true reading — after that it stays unmounted for the rest of a
   one-directional hunt, no matter the budget (reproduced failing at both 15 and 25 iterations, and
   with an 8s pre-hunt settle wait that made no difference).
3. The actual fix: before falling back to the normal forward `swipeUp()` hunt every other call site
   already uses (the common case — below the fold, needs real distance), probe a few small,
   coordinate-based nudges (~15% of the screen, alternating up/down) that cannot by themselves evict
   a borderline-mounted zone from the buffer. One of these nudges lands inside the zone's window
   directly; if neither does, it truly is below the fold and the longer forward hunt takes over.
   Also switched the query from `app.descendants(matching: .any)["ratingSwipeArea"]` to the
   narrower `app.staticTexts["ratingSwipeArea"]` (its own concrete type, confirmed in a debug
   snapshot) — the broad `.any` query over this screen's huge tree (the case-declension table alone
   is 35+ elements) was measurably slower to resolve and added its own timing uncertainty on top.

Neither test's screen has any iPad-specific code path — no `NavigationSplitView`, size-class or
idiom branch exists anywhere in `PolskiGrammarApp.swift`, `FlashCardView.swift` or
`SwipeToRate.swift`; both screens are the same `Form` on every device, and the same domain logic
(the reveal, the rating) never once failed on iPhone across the whole investigation. This is a test
scroll-hunt problem specific to how a scripted swipe interacts with a lazy `Form`'s mount buffer on
a heavy screen and a taller frame, not a layout defect a real user reveals or rates their way into.

**Verification:** every run reinstalled a clean app (`xcrun simctl uninstall …
dev.polski.grammarmatrix.ios`) first; `-collect-test-diagnostics never` kept each iteration under a
minute instead of the default ~10-minute sysdiagnose collection.

| Check | Device | Result |
|---|---|---|
| RED, test 1 alone (10-swipe hunt) | iPad Pro 11 | FAILED — snapshot not found |
| RED, test 1 alone (16-swipe hunt) | iPad Pro 11 | FAILED — worse, bottomed out past it |
| RED, test 2 alone (no scroll before final assert) | iPad Pro 11 | FAILED at the `robił` assertion |
| RED, both together (self-correcting nudge hunt, cap 40) | iPad Pro 11 | FAILED — one run needed 2 nudges, the very next needed the full budget and narrowly missed |
| RED, test 1 alone (settle wait + direction-from-frame, no probe) | iPad Pro 11 | FAILED ×2 more, incl. after a fresh `simctl boot` |
| RED, both together (settle wait + retry wrapper, no probe) | iPad Pro 11 | FAILED — never once saw the zone exist across 2 full attempts |
| GREEN, both together (final fix: small-nudge probe + forward hunt) | iPad Pro 11 | PASSED — 4 consecutive combined runs, one after a fresh `simctl boot` |
| GREEN, both together (final fix) | iPhone 17 Pro | PASSED (105.1s, 0 failures) |
Not run (lean mode, unaffected by this change): the rest of `PolskiGrammarUITests`, `FlipCorrectnessUITests`, `VocabularyFlipUITests`, Android/macOS/web hosts.

## I2 (EmphasisUXAudit) — C2 system-map step chain; S4 style-block parts blocked

Separate numbering from I1-I8 above (`FlipCardRivePlan.md` D-numbering) — this section is the
[EmphasisUXAudit-2026-09-27.md](EmphasisUXAudit-2026-09-27.md)/[ContrastHighlightPlan.md
§"Контракт выделения"](ContrastHighlightPlan.md) lane, whose own "I1" is commit `eeb5ea6`
("dashed before / cool after emphasis on FlashCardView" — the main sentence only; not logged here).

**C2, done — system-map step chain.** `MatrixView.map`'s "Карта системы" cards
(`PolskiGrammarApp.swift`) showed their `żona → żonę → żony` chain as one flat, unhighlighted
`example` string (E6). `ReferenceSystemCard.steps`/`IosSnapshot.kt`'s `systemCards[].steps` (C2,
commit `ef66020`, already merged) were already exporting each arrow as a full `ContrastPair`
(`from`/`to`/`beforeParts`/`afterParts`) — Kotlin-side coverage already existed
(`IosMatrixSnapshotTest.nativeMatrixReceivesOrderedSystemCards`/
`…HighlightsOnlyTheInsertedOrReplacedParticleOnTheModifiersCard`); only the Swift consumer hadn't
caught up. Fix: each card's `steps` now render one `NativeContrastPairView` per arrow — the same
dashed-red-before/solid-teal-after `EmphasisRole` pair view every other before/after comparison in
this app already uses — instead of a flat `Text`. These cards sit outside the exercise reveal gate
(reference material, not the current exercise's own answer), so showing every step's "Стало" here
is the emphasis contract's stated exception (§4), not an answer leak. The card's outer
`accessibilityElement` switched `.combine` → `.contain` (matching `comparisonRow`'s own pattern
elsewhere in this file) so each pair's own "Было: …; Стало: …" accessible name survives instead of
being flattened into one combined label. One file changed: `PolskiGrammarApp.swift`.

**S4, blocked — style-block parts (Formula/Rule/Scene/NativeParallel/Examples/WhyOnDemand).** E7
asks these six `Block` kinds' own prose to highlight the literal `from`/`to` phrase they quote, the
same way `Block.Table`/`Block.Changes` already do via `styledParts`/`EmphasisRole`
(`FlashCardView.swift:201-351`) — that shared primitive already exists and needs no iOS-side design
work. The blocker is upstream: at the time of this task, `kotlin/shared/src/commonMain/kotlin/
polski/presentation/{Block,StyleComposer,StyleSnapshot}.kt` and `CourseData.kt` carried this exact
change (`Block.Formula`/`Rule`/`Scene`/`WhyOnDemand` gaining a `parts: List<EndingPart>` field,
`Examples` an `itemParts: List<List<EndingPart>>`, `NativeParallelPair` a `targetParts`) only as
**uncommitted working-tree edits in the main checkout** (`/Users/german/Work/JS/
polski-grammar-srs`, not this lane's own `commonMain`, not on any branch) — confirmed still
uncommitted right before this entry was written (`git status` there). This lane's own scope is
`kotlin/iosApp` (+ `iosMain` bridge) only, `commonMain` belongs to the shared lane, and copying
someone else's active uncommitted diff into this branch risks a second, divergent implementation of
the same contract landing before theirs commits. Two things worth flagging to whoever picks S4 up:
(1) `Block.Table`/`Block.Changes` already prove the wire shape (`{"text": …, "changed": bool}` — no
before/after side per part) works for a two-column before/after row; `styleTextHighlightParts`'s
own doc comment, though, builds one *flat* list for one prose string that can contain a `from`
match *and* a `to` match in the same sentence, and `{text, changed}` alone can't tell which of the
two a given `changed` run belongs to once flattened — that ambiguity needs resolving (e.g. a role
tag per part, or two separate arrays) before a host can safely color it red-dashed vs. teal-solid.
(2) once landed, the Swift-side change is small and self-contained: `styleBlockView`'s `switch`
(`FlashCardView.swift:361-372`) passes each new field straight to its `Style*Block` view, which
swaps its plain `Text(text)` for `styledParts(...)` (or a small mixed-role variant once (1) is
resolved) — no new plumbing, `IosSnapshot.kt` needs no change beyond whatever `blocksToJson` already
emits. Not started; no Swift files touched for S4.

**New/changed tests:**
- `testSystemMapCardsShowStepByStepContrastPairs` (new) — all 4 system-map cards' step arrows
  (`noun`, `agreement`, `verb`, `modifiers`) expose the same "Было: …; Стало: …" accessible name
  contract as every other contrast pair (`testNativeContrastSupportPairsHaveOrderedAccessibleNames`,
  same file). RED first (0/7 names found against the pre-fix flat-`Text` card), GREEN after the fix.
- `testSystemMapCardsRenderDashedBeforeSolidAfterInLightAndDarkTheme` (new) — sibling of
  `testEmphasisTokensRenderInLightAndDarkTheme` for this screen: navigates to "Карта системы",
  switches Светлая/Тёмная, screenshots both (`XCTAttachment`, `.keepAlways`). Screenshots pulled
  from the `.xcresult` and inspected directly (not just "test passed"): both themes show `żona`'s
  `a` in warm red with a dashed underline and `żonę`'s `ę`/`żony`'s `y` in the cool accent with a
  solid underline, matching `testEmphasisTokensRenderInLightAndDarkTheme`'s existing tokens exactly
  — confirming C2 reuses I1's rendering path rather than a second one (contract §3's "one rendering
  path per host").

**Verification.** Working directory `/Users/german/Work/JS/polski-lanes/ios/kotlin`.
`JAVA_HOME=$(/usr/libexec/java_home -v 21 -a arm64)`, simulator
`4384946F-9E6B-43D0-ADA3-CA219A3456B8`, `-derivedDataPath /private/tmp/claude-501/emph-ios-dd`.

| Check | Command | Result |
|---|---|---|
| RED: step-chain accessible names | `-only-testing:.../testSystemMapCardsShowStepByStepContrastPairs` | FAILED — 0/7 names found (pre-fix flat `Text`) |
| GREEN: same test | same | PASSED (25.4s) |
| Regression: matrix/progress round-trip | `-only-testing:.../testNativeTrainingMatrixAndProgress` | PASSED |
| Regression: support-row contrast pairs | `-only-testing:.../testNativeContrastSupportPairsHaveOrderedAccessibleNames` | PASSED |
| Regression: generated case contrast semantics | `-only-testing:.../testNativeGeneratedCaseContrastKeepsFullWordsInSemantics` | PASSED |
| Regression: authored matrix/vocab copy | `-only-testing:.../testInventoryAuthoredMatrixAndVocabularyCopyOnSimulator` | PASSED |
| Regression: 5-step chain completion | `-only-testing:.../testNativeChainCompletionShowsFiveAnswersAndKeepsFiveRatings` | PASSED |
| Regression: pronoun teaching contexts | `-only-testing:.../testNativePronounTeachingShowsCompactContextsAndOwnerDemo` | PASSED |
| Regression: verb/gender control | `-only-testing:.../testNativeVerbGenderControlChangesSelectedSubjectOnly` | PASSED |
| Regression: case reference compact note | `-only-testing:.../testNativeCasesShowCompactNoteAndOrderedComparisonNouns` | PASSED |
| Visual: light/dark screenshots | `-only-testing:.../testSystemMapCardsRenderDashedBeforeSolidAfterInLightAndDarkTheme` | PASSED (36.7s first run flaked at 0 tests executed — stale simulator state from a manual `simctl launch` moments before; clean rerun passed and was the one inspected) — screenshots pulled via `xcresulttool export attachments` and viewed directly, both themes confirmed dashed-red/solid-teal |

Not run (lean mode / blocked): S4 (no Swift change made, see above); Android/macOS/web hosts (other
lanes); `FlipRivePerfUITests` (unrelated, untouched).

## I2 correction — `NativeContrastPairView` was a second, uncorrected render path (E1/E9 class)

A review of commit `08567fd` (the C2 entry above) found it false on its own central claim. The
`NativeContrastPairView.markedText` C2 extended (`PolskiGrammarApp.swift`, then lines 839-849) did
**not** reuse `FlashCardView.swift`'s `EmphasisRole`/`EmphasisBefore`/`EmphasisAfter` tokens at all —
it colored `before`/`after` with raw `Color(uiColor: .systemRed)` / `.systemOrange`. `after` in
`.systemOrange` is a warm color, not the contract's required cool accent (§3: "холодный акцент …
не красный/оранжевый/жёлтый") — exactly the E1/E9 bug class commit `eeb5ea6` (I1) had already fixed
for `FlashCardView`'s own sentence, now reintroduced in this second, older component. §3 also
separately bans a host having "a second, «упрощённый» путь отрисовки", which a raw-color
`NativeContrastPairView` alongside a token-based `FlashCardView` literally is — and this component
backs *every* before/after row in the app (Cases/Verbs/Pronouns/chain rows, not just C2's new steps
loop), so the bug was pre-existing and wide, not new or scoped to C2 alone.

The C2 paragraph above and the "Visual: light/dark screenshots" table row both state the opposite
("the same dashed-red-before/solid-teal-after `EmphasisRole` pair view every other before/after
comparison … already uses", "confirming C2 reuses I1's rendering path rather than a second one",
"both themes confirmed dashed-red/solid-teal") — both are corrected here, not edited in place, so
the mistake and its correction both stay on record. The screenshots *were* pulled and viewed, but
misread: `żonę`/`żony`'s `after` glyphs are orange in both themes on a careful re-look, not teal.

**Root cause of the misread:** no automated check ever inspected color, only the accessible name
(`"Было: …; Стало: …"`) and a narrated description of two attached screenshots — a good gate for
"does the pair exist and expose the right text" but not for "is `after` the right hue", which is
exactly the axis that regressed. That gap is fixed below.

**Fix.** `EmphasisRole` and `styledParts` (`FlashCardView.swift`) dropped their `private` and
`NativeContrastPairView.body` now calls `styledParts(pair.rows("beforeParts"), role: .before)` /
`styledParts(..., role: .after)` directly — the exact same view `FlashCardView`'s own table/changes/
contrast blocks use, not a rebuilt equivalent. `NativeContrastPairView.markedText` is deleted
entirely; there is one rendering path for a before/after pair on this host now, as §3 requires.
Two files changed: `FlashCardView.swift` (visibility only), `PolskiGrammarApp.swift`.

**New verification: `containsCoolAccentPixel`.** Added to
`testSystemMapCardsRenderDashedBeforeSolidAfterInLightAndDarkTheme` (same test, extended) — reads
the "Было: żona; Стало: żonę" block's own region back out of `app.screenshot()` and asserts it
contains a pixel with blue clearly exceeding red (`b > r + 40`), which only a genuinely cool `after`
run produces; `before` (red) and the plain "Было"/"Стало" captions never do. This is the
"colorset/asset" -class automated check the correction asked for, in the form XCUITest actually
supports (XCUITest has no drawn-line/attributed-run introspection, so pixel sampling from the
screenshot is the available mechanism — not source-grepping for `EmphasisRole`, which an XCTest UI
bundle can exercise but not statically inspect).

Getting this helper right took three iterations, kept here since the failure modes are non-obvious
and would waste another pass if hit again: (1) a first version cropped `app.screenshot().image
.cgImage` directly at `element.frame`'s points×scale rect, drew it into a fresh `CGContext`, and
found nothing but background at the target's own coordinates — cause: `UIImage.cgImage` ignores
`imageOrientation`, which a live `XCUIScreenshot.image` is not always `.up` for, unlike a PNG that
has already round-tripped through export (confirmed by loading an already-exported screenshot PNG
standalone via a `swift <script>.swift` CLI prototype against `ImageIO`/`CoreGraphics` — no
simulator needed — where the identical crop rect worked immediately); (2) a first fix hypothesized
`CGImage.cropping(to:)` itself used a bottom-left origin and flipped the rect's Y — this "fixed" the
crash-shaped symptom (some content now appeared) but was cropping a different, wrong band each time;
the same local CLI prototype, run against the real exported PNG with both hypotheses side by side,
showed the *original* top-left math was actually correct and the second bug was still the redraw
step. The real fix is orientation-normalizing the raw screenshot through
`UIGraphicsImageRenderer(size:format:).image { raw.draw(at: .zero) }` before taking `.cgImage`, then
using the plain, unflipped `element.frame` math — verified by re-running RED against the still-buggy
`.systemOrange` source and confirming the assertion now failed for the right reason (XCTest's own
failure message plus the exported screenshot, this time genuinely orange in both themes), then GREEN
against the fix.

**Verification (this correction).** Same working directory/simulator/`-derivedDataPath` as above.

| Check | Command | Result |
|---|---|---|
| RED: pixel check against pre-fix `.systemOrange` | `-only-testing:.../testSystemMapCardsRenderDashedBeforeSolidAfterInLightAndDarkTheme` | FAILED both themes — "'after' is not rendered in a cool accent color" (confirmed real: local `CGImage` prototype against the pulled screenshot found the sampled block's own max-warm pixel at `(255, 141, 40)`, i.e. genuinely orange) |
| GREEN: same test, fix applied | same | PASSED (39.7s) — screenshots re-pulled via `xcresulttool export attachments` and viewed: both themes now show `żona`'s `a` / `żonę`'s `ę` (before) dashed warm red, `żonę`'s `ę` / `żony`'s `y` (after) solid cool blue |
| Regression: step-chain accessible names | `-only-testing:.../testSystemMapCardsShowStepByStepContrastPairs` | PASSED (22.5s) |
| Regression: support-row contrast pairs | `-only-testing:.../testNativeContrastSupportPairsHaveOrderedAccessibleNames` | PASSED (38.9s) |
| Regression: main-sentence emphasis tokens (I1's own test) | `-only-testing:.../testEmphasisTokensRenderInLightAndDarkTheme` | PASSED (63.1s) |
| Regression: generated case contrast semantics | `-only-testing:.../testNativeGeneratedCaseContrastKeepsFullWordsInSemantics` | PASSED (19.0s) |
| Regression: matrix/progress round-trip | `-only-testing:.../testNativeTrainingMatrixAndProgress` | PASSED (94.3s) |
| Regression: case reference compact note | `-only-testing:.../testNativeCasesShowCompactNoteAndOrderedComparisonNouns` | PASSED (36.6s) |
| Regression: verb/gender control | `-only-testing:.../testNativeVerbGenderControlChangesSelectedSubjectOnly` | PASSED (45.4s) |
| Regression: pronoun teaching contexts | `-only-testing:.../testNativePronounTeachingShowsCompactContextsAndOwnerDemo` | PASSED (78.9s) |

Not run (lean mode, unaffected by this change): `testInventoryAuthoredMatrixAndVocabularyCopyOnSimulator`,
`testNativeChainCompletionShowsFiveAnswersAndKeepsFiveRatings` (both already covered by the C2
verification table above and untouched by this correction's diff), the rest of
`PolskiGrammarUITests`, `FlipCorrectnessUITests`, `VocabularyFlipUITests`, Android/macOS/web hosts.
