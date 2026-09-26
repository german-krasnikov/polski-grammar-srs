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

## Status

Done. Committed in this worktree; ready for the cross-lane merge and later end-to-end test pass.
