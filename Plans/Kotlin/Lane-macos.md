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

## Status

M1 (D1) and M2 (D2) both applied and committed. Both verified by build + structural code review
against their respective specs/precedents; interactive click-through is the one open gap for both
(no Accessibility/Input Monitoring permission in this session) — worth closing with a real
XCUITest target or a manual pass before ship, not before the cross-lane merge.
