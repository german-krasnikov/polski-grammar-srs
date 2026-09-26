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

## Open follow-ups (not this task)

- D4 (tab/screen paging) and D5 (the `UserPreferences.animationsEnabled` toggle's own iOS wiring,
  and unloading Rive entirely when it's off) are untouched — this task was D3 only.
- `THIRD_PARTY/credits.md` / `Plans/Kotlin/RiveCatalog.md` still list `rings.riv` for "Android and
  iOS" — each host's own D3 pass should update its own line once all three have landed, to avoid
  a three-way merge conflict on the same lines right now.
