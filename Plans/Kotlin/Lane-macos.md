# Lane macOS — evidence log

Worktree: `/Users/german/Work/JS/polski-lanes/macos` (branch `lane-macos`, from `main` @ `e52320c`).
Scope: macOS host only (`kotlin/macosApp/**`). No `commonMain`, Android, iOS or web files touched.

## M1 — D1 expand reveal on the training card (`MacFlashCardView.swift`)

**Change**: replaced the 3D flip (`rotation3DEffect`, `showBack`/`rotation` state, face-swap at the
halfway point) with a downward expand-reveal, matching D1 and the web reference implementation
(`webMain/kotlin/polski/ui/TrainingWebApp.kt`'s `applyExpand` / `.card-answer-wrap` grid-rows
trick):

- The question (`frontFace`) stays mounted and visible; the answer/explanation/rating panel
  (`backFace`) is inserted below it only once `state.phase == "Revealed"`, via a `revealed`
  `@State` flag driven by `.onChange(of: state.phase)` (never dispatches a command itself).
- The insertion animates with `withAnimation(.spring(response: 0.5, dampingFraction: 0.82))`
  (spring-like ease-out), snapped instead when `reduceMotion` (system Reduce Motion or the app's
  `Motion.Reduced`) is set — unchanged gate, still computed once by `TrainingView.cardMotionReduced`.
- Three sub-groups inside `backFace` (answer/frozen-answer; changes+formula; rating panel) each
  carry their own `AnyTransition` (`.revealGroup(delay:)`, a `.opacity`+10pt-`offset` modifier
  transition) with increasing delays (0.05s / 0.14s / 0.22s) for a short stagger, mirroring the
  web's per-group `animation-delay` (0.08s/0.18s/0.28s). Skipped under `reduceMotion` (`.identity`).
- New: tapping anywhere on the still-visible question card while `state.phase == "Question"` now
  also sends `reveal` (same command the button already sent) — exactly once, since the gesture is
  only attached during `Question`. Nested Picker/TextField/Button keep gesture priority over the
  ancestor tap (same precedent as the old back-face gesture never stealing taps from its sibling
  rating buttons).
- Removed: the tap-to-flip-back gesture on the back face — D1 task/grammar cards only ever expand,
  they don't un-reveal. The swipe-to-rate `DragGesture` on the back face (unrelated to the flip)
  is unchanged, as are the existing rating buttons and their ⌘1/⌘2 shortcuts.
- Out of scope for this task (left untouched): D2 (vocabulary whole-panel flip), D3 (rating swipe
  visuals: tilt/tint-grows/fly-out — the existing plain drag-to-rate gesture already there is kept
  as-is), D4 (tab paging), D5 (animations toggle wiring beyond the existing `reduceMotion` gate).

**Files changed**: `kotlin/macosApp/PolskiGrammarMac/MacFlashCardView.swift` only. No
`commonMain`/shared Kotlin changes.

## Verification

- Build: `xcodebuild -project kotlin/macosApp/PolskiGrammarMac.xcodeproj -scheme PolskiGrammarMac
  -destination "platform=macOS" -derivedDataPath /private/tmp/lane-macos-dd
  CODE_SIGNING_ALLOWED=NO build` — **BUILD SUCCEEDED** (arm64, JDK 21 arm64 via
  `/usr/libexec/java_home -v 21 -a arm64`), against the repo's existing, unregenerated
  `PolskiGrammarMac.xcodeproj` (no Swift files were added/removed, so `generate_project.rb` did
  not need to run; it was tried once, produced only churn — new random object IDs, no structural
  change — and was reverted).
- `:shared:macosArm64Test`: not run — no Kotlin (`commonMain`/`macosMain`) files were touched.
- No automated UI/unit test target exists for this SwiftUI view (no XCUITest target in
  `PolskiGrammarMac.xcodeproj`); verification is the build plus manual code reading against the D1
  contract. Flagging this gap rather than adding a new test target, which is out of scope for a
  small, focused task.

## M1 correction — scope the reveal tap gesture away from the interactive controls

**Reviewer blocker** (on `f572529`): the `.onTapGesture` that reveals the card on tap was attached
to the *entire* Question-phase `frontFace` content, including the answer-mode `Picker`, the Typed
`TextField` and the reveal `Button` — always co-present with them, unlike either cited precedent
(iOS's `FlashCardView.swift`, which only attaches its tap gesture while `phase == "Revealed"`, i.e.
never alongside its own Question-phase Picker/TextField/Button; and the web reference's
`installTapGesture(front) { ... }`, attached only to the sentence/operation element, a sibling of
`renderAnswerArea`, never an ancestor of it).

**Fix**: split `frontFace` into two siblings instead of one gesture-wrapped container:
- `tapToRevealContent` — the ПРЕДЛОЖЕНИЕ label, sentence, divider and (when not `introPending`) the
  ЗАДАНИЕ label/prompt text. The `.onTapGesture { model.send("reveal", exercise.id) }` is attached
  only here, and only when `!state.introPending && state.phase == "Question"` — so it's absent
  entirely during `introPending` (whose own "Перейти к заданию" button lives inside this same block
  and needs sole gesture priority there) and absent once `Revealed`.
- `answerControls` — the Picker/TextField/reveal-`Button`, rendered by `frontFace` as a plain
  sibling of `tapToRevealContent`, never nested under its gesture. This is a structural exclusion, not gesture-priority
  reliance: there is no ancestor tap gesture over these controls at all now, matching the web
  reference's sibling layout (`front` / `renderAnswerArea`) exactly.
- No behavior change to `answerControls` itself (Picker, TextField binding, reveal button) or to
  `backFace`/rating — only `frontFace`'s internal split changed.

**Verification**:
- Build: `xcodebuild -project kotlin/macosApp/PolskiGrammarMac.xcodeproj -scheme
  PolskiGrammarMac -destination "platform=macOS" -derivedDataPath /private/tmp/lane-macos-dd
  CODE_SIGNING_ALLOWED=NO build` — **BUILD SUCCEEDED** (rerun after the fix).
- Manual interactive click-through (the reviewer's explicit ask) was attempted on the built app
  (`open`ed the binary directly, real windowed macOS session, not a simulator) but could not be
  completed: driving it via `osascript`/System Events failed with "osascript is not allowed
  assistive access" (-1728) — this sandboxed session has no Accessibility permission for
  `osascript`/Terminal, and granting one requires an interactive System Settings approval this
  session cannot perform, and wasn't asked for. The app was launched, screenshotted (confirmed a
  real window server session exists) and then killed cleanly (`pkill`) rather than left running.
  Flagging this as a real, unresolved verification gap: (a)-(d) from the reviewer's list are backed
  here by the structural exclusion (Picker/TextField/Button are siblings with zero ancestor tap
  gesture — there is no code path left by which tapping them can reach `model.send("reveal", ...)`)
  and by matching both cited precedents' exact scoping, but not by an actual click-through.
- `:shared:macosArm64Test`: not run — still no Kotlin (`commonMain`/`macosMain`) files touched by
  this fix.

## M2 — D2 whole-panel flip on the vocabulary card (new `MacVocabularyCardView.swift`)

**Change**: the plain `GroupBox("Карточка слова")` + `Button("Показать ответ")` in
`VocabularyView` (`PolskiGrammarMacApp.swift`) is replaced by a new `MacVocabularyCardView`, a
native 3D flip of the whole rounded panel — matching D2 and the web reference
(`webMain/kotlin/polski/ui/VocabularyWeb.kt`'s `.card-flip`/`.card-flip-inner`, `installTapGesture`
on `front`, `flipCard.installTap` on the revealed face):

- **Whole panel flips as one object**: the background/border/radius chrome (matching the app's
  own `TrainingView.contentCard` styling — `controlBackgroundColor` fill, 18pt continuous corner
  radius, 0.09-opacity 1pt stroke) is applied to the *rotating* container itself, not to a fixed
  wrapper around swapped content — unlike `GroupBox`, which would only rotate its content and
  leave the frame static.
- **No "Показать ответ" button**: the panel itself is the reveal control. Before reveal, the
  front face is exposed to accessibility as a single button (`.accessibilityElement(children:
  .ignore)` + `.accessibilityLabel("Показать ответ")` + `.accessibilityAddTraits(.isButton)`) —
  matching the web's `promptBlock` (`role="button"`, `aria-label="Показать ответ"`), not
  combining the visible lemma text into the label. A tap anywhere on the panel dispatches
  `model.vocab("reveal")` once (idempotent after — `VocabularySession.reveal()` no-ops when
  already `revealed`) and the flip follows automatically via `.onChange(of: state.revealed)`.
- **90° face swap**: reuses the exact split-mount technique from `MacFlashCardView`'s pre-D1 flip
  (`git show af126a6:.../MacFlashCardView.swift`) — `rotation` drives a `rotation3DEffect` on
  whichever face is mounted, and the *other* face is swapped in only at the halfway point
  (`DispatchQueue.main.asyncAfter(deadline: .now() + 0.25)`, half of the 0.5s
  `.easeInOut` animation), with the back face counter-rotated `-180°` so its text isn't mirrored.
  Snaps instantly (`reduceMotion`) under the same gate as the training flip: system Reduce Motion
  OR the app's own `Motion.Reduced` (new `VocabularyView.cardMotionReduced`, identical formula to
  `TrainingView.cardMotionReduced`).
- **Tap again flips back, visually only**: once `state.revealed`, a tap toggles `showBack` without
  re-dispatching `reveal` or touching FSRS — `handleTap()` branches on `state.revealed` alone. The
  rating buttons (`Повторить`/`Вспомнил`, unchanged, still calling `model.vocab("again"/"good")`)
  live inside the back face as real `Button`s and keep gesture priority over the ancestor
  `.onTapGesture`, the same precedent `MacFlashCardView`'s back face already relies on.
- A new due word (or the direction/filter changing the current item) always starts face-up on its
  question, unanimated (`.onChange(of: item.id)`), never inheriting the previous word's face.
- Out of scope for this task (left untouched): D3 (swipe-to-rate gesture and tilt/tint visuals —
  macOS vocabulary card has none yet, same as before this change; only the buttons exist), typed
  mode / direction toggle / "Проверить" (not present in the macOS vocabulary screen at all yet —
  adding them was not part of this task and would have been scope creep beyond "whole-card flip on
  click, no reveal button, 90° swap, accessibility label").

**Files changed**:
- `kotlin/macosApp/PolskiGrammarMac/MacVocabularyCardView.swift` (new).
- `kotlin/macosApp/PolskiGrammarMac/PolskiGrammarMacApp.swift`: `VocabularyView` now renders
  `MacVocabularyCardView` instead of the inline `GroupBox`, plus the same `reduceMotion`/
  `cardMotionReduced` pair `TrainingView` already has.
- `kotlin/macosApp/generate_project.rb`: added `MacVocabularyCardView.swift` to the hardcoded
  source-file list the generator writes into the `.xcodeproj` (it does not glob the directory).
- No `commonMain`/shared Kotlin changes — `VocabularySession`/`MacVocabularySession` already
  exposed everything this needed (`reveal`, `again`, `good`, `revealed`), untouched.

**Verification**:
- Regenerated the project (`arch -arm64 ruby kotlin/macosApp/generate_project.rb`) — required this
  time, unlike M1, because a new source file was added.
- Build: `xcodebuild -project kotlin/macosApp/PolskiGrammarMac.xcodeproj -scheme
  PolskiGrammarMac -destination "platform=macOS" -derivedDataPath /private/tmp/lane-macos-dd
  CODE_SIGNING_ALLOWED=NO build` — **BUILD SUCCEEDED** (arm64, JDK 21 arm64).
- `:shared:macosArm64Test`: not run — no Kotlin files touched by this change.
- Manual interactive click-through was attempted again (launched the built `.app`, real windowed
  session) and again could not be completed: `osascript`/System Events UI scripting is blocked
  ("assistive access" -1719, same as M1's logged gap), and a from-scratch attempt at a
  permission-free alternative — a small Swift helper posting synthetic `CGEvent` mouse clicks via
  `CGEventPost(.cghidEventTap, ...)` at the sidebar's "Словарь" tab's screen coordinates — was
  tried and produced no effect on the app (repeated screenshots after single- and double-click
  sequences show the same "Карточки" tab still selected), consistent with synthetic input also
  being silently dropped without a granted input/accessibility permission in this sandboxed
  session. Verification here is the build plus structural code review against D2 and the exact
  precedent (`af126a6`'s pre-D1 `MacFlashCardView`); the interactive click-through remains the same
  kind of open gap M1 already flagged, not newly introduced by this task.

## M2 correction — stretch the tap target/accessibility element to the full panel

**Reviewer blocker** (on `6c64b2a`): `frontFace`/`backFace` attached `.contentShape(Rectangle())`,
`.onTapGesture(handleTap)` and (pre-reveal) the `.accessibilityElement`/`.accessibilityLabel`/
`.accessibilityAddTraits` modifiers to their own inner `content`/`VStack`, which had no expanding
child (no `Spacer`, no own `.frame(maxWidth: .infinity)`). The outer `Group` in `body` only gets
`.frame(maxWidth: .infinity, alignment: .leading)` *afterwards*, stretching the visible
background/border but not the tap target or the accessibility element sized from `content`'s
intrinsic size — a SwiftUI VStack with no expanding child reports only its intrinsic (text) size
regardless of an ancestor frame added later. Result: clicking the panel's padding or empty space
below a short lemma did nothing, and the single VoiceOver "Показать ответ" button's frame didn't
match the visible card — a real a11y regression, and it also meant tap-to-flip-back after reveal
had the same undersized target.

**Fix** (the reviewer's second suggested direction, the smaller of the two): added
`.frame(maxWidth: .infinity, alignment: .leading)` to `content` in `frontFace`, and to the `VStack`
in `backFace`, *before* `.contentShape(Rectangle())` — so the shape/gesture/accessibility modifiers
now compute over the already-stretched view instead of the intrinsic one. Two one-line insertions,
no structural change: per-state accessibility branching (`state.revealed`) in `frontFace` is
untouched, `handleTap`/rotation/reveal logic untouched.

**Verification**:
- Build: `xcodebuild -project kotlin/macosApp/PolskiGrammarMac.xcodeproj -scheme
  PolskiGrammarMac -destination "platform=macOS" -derivedDataPath /private/tmp/lane-macos-dd
  CODE_SIGNING_ALLOWED=NO build` — **BUILD SUCCEEDED** (rerun after the fix; project not
  regenerated, no files added/removed).
- Launched the built `.app` (`open`) — runs, no crash, quit cleanly afterward.
- Real interactive click-through / VoiceOver hit-test (the reviewer's explicit ask) again could not
  be performed: `osascript`/System Events UI scripting still fails with "osascript is not allowed
  assistive access" (-25211) in this sandboxed session — same unresolved environment gap already
  logged for M1 and M2's first pass, not something this fix could close. The fix itself is a direct,
  minimal structural correction (stretch-before-shape, exactly the reviewer's cited pattern), not a
  workaround, so the code-level defect is resolved even though the interactive confirmation remains
  open pending real Accessibility permission.
- `:shared:macosArm64Test`: not run — no Kotlin files touched.

## M3 — D3 swipe/keys/interval on both cards, Rive rating effect for vocabulary too

**Change**: added the full D3 drag-to-rate affordance (whole card follows the finger, tint+label
grows with distance, snap-back/fly-out), compact interval-showing rating buttons, and
ArrowLeft/ArrowRight keyboard rating to **both** the training card (which previously had only a
crude threshold-only `DragGesture` with no visual feedback and no keyboard arrows) and the
vocabulary card (which had no swipe, no keyboard shortcuts and no Rive effect at all yet).

- New shared `kotlin/macosApp/PolskiGrammarMac/SwipeRating.swift`:
  - `SwipeToRate` (`ViewModifier`, via the `.swipeToRate(enabled:reduceMotion:onRating:)` extension):
    a `DragGesture` (`.simultaneousGesture`, so it never blocks the rating buttons or the existing
    tap-to-flip/tap-to-reveal gestures underneath) that live-translates+tilts the view it's attached
    to, snaps back under a 70pt threshold (`.spring`), and flies it off-screen
    (`.easeIn(0.22s)`, then off `flyingOut`) past it before calling `onRating`. Reduced-motion
    (system or `Motion.Reduced`) skips both animations and calls back immediately. Mirrors the web
    reference's `installSwipeCard` (`webMain/kotlin/polski/ui/WebSwipeRating.kt`).
  - `SwipeRatingTint`: the decorative red/green tint + growing "Повторить"/"Вспомнил" label driven
    by drag progress (`-1...1`), `allowsHitTesting(false)` so it never intercepts anything.
  - `RatingButton`: compact (`.controlSize(.regular)`, `.bordered`/`.borderedProminent`) button
    showing the FSRS interval preview under its label when given `dueMs`/`nowMs` — D3's "compact
    buttons with interval stay" (vs. no rating buttons at all on touch hosts).
  - `intervalLabel(dueMs:nowMs:)`: native Swift port of the web reference's `intervalLabel`
    (`webMain/kotlin/polski/ui/TrainingWebApp.kt`) — pure display formatting, kept host-local
    rather than exported from `commonMain` since this lane must not touch shared Kotlin source sets
    for something with no domain logic.
- **Training card** (`MacFlashCardView.swift`): removed the old bare `DragGesture(minimumDistance:
  18).onEnded` on the back face. `.swipeToRate` is now attached instead at the `TrainingView`
  call site around `exerciseCard(state, exercise)` (`PolskiGrammarMacApp.swift`) — deliberately
  *outside* `contentCard`'s background/border, not inside `MacFlashCardView` itself, so the
  transform covers the whole visible panel (chrome included), matching the vocabulary card's
  already-whole-panel flip, not just its text content. `enabled: state.phase == "Revealed"`, same
  guard the old gesture had. The two rating buttons became compact `RatingButton`s wired to
  `exercise.intervals?.again`/`.good` + the newly-decoded top-level `now` (both already emitted by
  `MacSnapshot.kt`'s existing `intervals`/`now` JSON fields — Kotlin needed no changes here, only
  the Swift decode). Two invisible (`0×0`, `.hidden()`, `.accessibilityHidden(true)`) buttons add
  bare `.leftArrow`/`.rightArrow` shortcuts alongside the existing ⌘1/⌘2 (a `Button` only keeps its
  *last* `.keyboardShortcut`, so a second shortcut needs a second button) — both only mounted
  inside the back face, i.e. only in the `Revealed` phase, which never coexists with the typed
  answer `TextField` (`Question`-phase-only, in `answerControls`), so arrow keys there always mean
  "move the caret", never "rate", with no extra focus-tracking needed.
- **Vocabulary card** (`MacVocabularyCardView.swift`): `.swipeToRate(enabled: state.revealed, ...)`
  attached after `.background`/`.overlay` in `body`, so it moves the whole flipped panel — calls
  `model.vocab("good"/"again")`. Back-face buttons became compact `RatingButton`s (previously plain
  `Button`s with no interval, no shortcuts) wired to the new `state.intervals?.again`/`.good` +
  `state.now`, plus the same ⌘1/⌘2 + hidden-button ArrowLeft/ArrowRight pattern as training (no
  typed-answer field exists yet on this card to conflict with).
- **Rive rating effect for vocabulary** (previously: only the training card had one):
  - `kotlin/shared/src/macosMain/kotlin/polski/macos/MacVocabularySession.kt`: added `onEffect`
    (mirrors `MacSession.onEffect` exactly), routing `dispatch("again"/"good")` through a new
    private `rate(rating)` that calls `cardEffectFor(rating).name` through `onEffect` **only when**
    `session.rate(rating)` returns `true` — so a rate the domain itself rejects (not yet revealed)
    never fires a spurious cue, same contract as the training bridge. Also extracted the session's
    `FsrsScheduler()` into a `private val scheduler` (was inline-only) so it can be reused for the
    new interval preview.
  - Same file's `snapshot()`: added `now`/`intervals` (Again/Good due-epoch-millis), computed via
    the existing shared `VocabularyCodec.preview(document, currentId, direction, scheduler, at)` —
    only when `state.revealed`, mirroring training's own "only in the Revealed phase" scoping.
    `commonMain` itself was **not** touched — `VocabularyCodec.preview`/`SchedulePreview` already
    existed and are exactly what the web reference already calls for the same purpose.
  - `PolskiGrammarMacApp.swift`'s `MacModel`: wired `vocabularySession.onEffect` to the *same*
    `cardEffect`/`effectCounter` pair `trainingSession.onEffect` already feeds (not a second
    published property) — the training and vocabulary cards are never both on screen at once
    (tab-switched), so one decorative-overlay slot correctly covers both. `VocabularyView` now
    wraps `MacVocabularyCardView` in a `ZStack` with a `RiveEffectOverlay(effect: model.cardEffect,
    reduceMotion: cardMotionReduced)`, mirroring `TrainingView.exerciseCard`'s existing pattern.
  - `TrainingSnapshot`/`VocabularySnapshot` (`PolskiGrammarMacApp.swift`): added the new shared
    `RatingIntervals` decodable (`again`/`good`, keyed `"Again"`/`"Good"`) and decoded `now`/
    `intervals` on both — `TrainingSnapshot.Exercise.intervals`/`TrainingSnapshot.now` were already
    being *emitted* by Kotlin before this task but never decoded on the Swift side at all.
- **Rings**: `Plans/Kotlin/FlipCardRivePlan.md`'s "remove rings.riv + ring overlay code" — verified
  there was nothing to remove on macOS: no `rings.riv` file and no ring-related code ever existed
  under `kotlin/macosApp/**` (`RiveEffectOverlay.swift` only ever had the `confetti`/`again`
  view-models). No-op here; this instruction only applies to the Android/iOS/web lanes.
- Regenerated the project (`SwipeRating.swift` added to `generate_project.rb`'s hardcoded file list,
  same as M2 needed for its new file).

**TDD**: `MacVocabularySessionTest.kt` (new) — RED first (scaffolded a no-op `onEffect` property so
the test compiled, confirmed both cases failed with the effects list empty), then GREEN after
wiring `rate()`. Covers: (1) a rate dispatched before `reveal` fires no effect (domain-rejected);
(2) `good` after `reveal` fires exactly `"Remembered"`; (3) `again` fires exactly `"Again"`. No
equivalent Kotlin-side test was needed for the training card's swipe/keys/buttons themselves — they
dispatch the pre-existing `"rate"`/`"reveal"` commands `MacSessionTest` already covers; only the new
vocabulary `onEffect`/`intervals` plumbing was new Kotlin behavior.

**Files changed**:
- `kotlin/macosApp/PolskiGrammarMac/SwipeRating.swift` (new).
- `kotlin/macosApp/PolskiGrammarMac/MacFlashCardView.swift`, `MacVocabularyCardView.swift`,
  `PolskiGrammarMacApp.swift`.
- `kotlin/macosApp/generate_project.rb` (added `SwipeRating.swift` to the source list).
- `kotlin/shared/src/macosMain/kotlin/polski/macos/MacVocabularySession.kt`.
- `kotlin/shared/src/macosTest/kotlin/polski/macos/MacVocabularySessionTest.kt` (new).
- No `commonMain`, Android, iOS or web files touched.

**Verification**:
- `./gradlew :shared:macosArm64Test` (cwd `kotlin/`, `JAVA_HOME` = JDK 21 arm64) — **PASSED**,
  including the 2 new `MacVocabularySessionTest` cases and all pre-existing macOS shared tests
  (`MacSessionTest`, `MacProgressRepositoryTest`, etc. — none regressed).
- `arch -arm64 ruby kotlin/macosApp/generate_project.rb` then `xcodebuild -project
  kotlin/macosApp/PolskiGrammarMac.xcodeproj -scheme PolskiGrammarMac -destination
  "platform=macOS" -derivedDataPath /private/tmp/lane-macos-dd CODE_SIGNING_ALLOWED=NO build` —
  **BUILD SUCCEEDED** (arm64).
- Launched the built `.app` (`open`), confirmed it stayed running (`pgrep`) with no crash/error in
  `log show --predicate 'process == "PolskiGrammarMac"'` over the run, then quit cleanly (`pkill`).
  Full interactive click-through of the new drag/tilt/tint/keyboard behavior itself was not done —
  same sandboxed-session Accessibility/Input-Monitoring gap M1/M2 already logged (no permission to
  drive real mouse drags or key events via `osascript`/`CGEventPost` here) — verification for the
  gesture/keyboard mechanics themselves is the build plus structural code review against the D3
  spec and the web reference's `installSwipeCard`/`intervalLabel`; the Rive-effect and
  interval-preview *data path* is additionally locked by the new `MacVocabularySessionTest`.

## Status

M1 (D1), M2 (D2) and M3 (D3) all applied and committed, plus one reviewer-requested correction each
to M1 and M2 (M1: scope the reveal tap gesture off interactive controls; M2: stretch the tap
target/accessibility element to the full panel). All verified by build + structural code review
against their respective specs/precedents, plus (M3) a focused Kotlin RED→GREEN test for the new
vocabulary Rive-effect/interval-preview plumbing. Interactive click-through of the actual
gestures/keyboard remains the one open gap across all three (no Accessibility/Input Monitoring
permission in this session) — worth closing with a real XCUITest target or a manual pass before
ship, not before the cross-lane merge.
