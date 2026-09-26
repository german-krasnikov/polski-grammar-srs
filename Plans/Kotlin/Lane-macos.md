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

## Status

Correction applied and committed. Structural fix verified by build + code match against both
cited precedents; interactive click-through evidence is the one open gap (see above) — worth
closing with a real XCUITest target or manual pass before this ships, not before the cross-lane
merge.
