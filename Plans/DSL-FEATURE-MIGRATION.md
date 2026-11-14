# DSL `.feature` migration review — tile builder, daily session, expandable section, achievements

Commissioned question: can `.feature` DSL + constructs (biome-dsl) remove the duplication `biome patterns`
found (expandable section ×6, `Row`+`horizontalScroll` ×16, `navBtn` ×4) and simplify SB-9/SB-10
(Android/iOS tile builder)? Read before this plan: `/Users/german/Work/Python/biome-dsl/AI/ARCHITECTURE.md`,
`PATTERN_DICTIONARY.md`, `BestPractice.md`, `ProjectMigratioPrincipals.md`, and the four polski-grammar-srs
UI files named in the task.

## Verdict up front

**Do not migrate these four UI candidates to `.feature`/FLOW DSL.** Two independent, grounded reasons:

1. **biome-dsl cannot generate any of this in Kotlin today.** FEATURE's own emitter comment is explicit:
   `kt and py name ts and print no dialect FEATURE` (`packages/universal/feature/src/compile-feature.ts:45`
   in `biome-dsl`) — the FEATURE pattern printer (mvp/elm/atomic/ecs) has no Kotlin backend at all. `lang-kt`
   only has `program-kt.ts`, `data-kt.ts`, `logic-kotlin.ts`, `flow-target.ts` — typed PROGRAM, DATA CONTRACT,
   Logic DECISION, and FLOW word-binding, not FEATURE/UI patterns. The Compose FLOW packs that *would* cover
   part of this (`packages/libs/kt/packs/compose-tween.pack.md`, `compose-module.pack.md`, `compose-events.pack.md`,
   `compose-navigation.pack.md`, `compose-wait.pack.md`, `rive-compose.pack.md`) exist only as unwired pack
   files — zero `.stack`/project reference anywhere in the biome-dsl repo (verified by grep). This matches
   `ARCHITECTURE.md` §6.1's own `†` marker: *"ни стека, ни проекта; новых `compose-*` нет, пока продукт не
   назовёт Compose-FLOW"*. Adopting them now means being the **first-ever consumer** of untested pack
   templates, and even then they only cover lifecycle primitives (`Animatable.animateTo`, a `CoroutineScope`
   wrapper, event wiring) — not arbitrary Compose layout (`Row`, `horizontalScroll`, card shells). There is no
   construct for "generate this Compose markup" anywhere in biome-dsl; Pattern Dictionary §4 puts UI-framework
   syntax under "принтер в `languages/*`" (backend changes syntax, not semantics) — layout composition is
   Layer 4 app code by biome-dsl's own architecture table, not a DSL target.

2. **Even for the decision logic behind these screens, this project's own measured economics say no.**
   biome-dsl's effectiveness boundary (`DSL_EFFECTIVENESS_BOUNDARY.md`, cited in `ARCHITECTURE.md` §7) found
   single-target DSL costs *more* lines than hand-written native code (STATE probe: 133+29 DSL lines vs 32
   native) and that Logic-DSL breaks even only at **≥2 target languages**. polski-grammar-srs has exactly one
   active generation language — Kotlin, shared across web/Android/Desktop via `commonMain`, with iOS SwiftUI
   consuming the same Kotlin through the KMP bridge (no `lang-swift` exists in biome-dsl — confirmed absent).
   The project *does* already use Logic DECISION for UI-adjacent rules (`AI/decisions.md`: exam commit/feedback/
   advance — *"Решения о commit/feedback/advance принадлежат DSL"*), but that's a high-branching rule reused
   across four exam types × multiple settings, where a `.examples`-validated contract earns its keep (P8
   Pareto). The four candidates here (tile builder Check-enabled, daily-session bounds, single-open-panel
   invariant, achievement threshold) are simple, low-branching, Kotlin-only booleans — below that bar.

The actual, cheaper fix for "eliminate duplication, simplify SB-9/10" is an ordinary Kotlin Multiplatform
dedup pass: hoist the repeated Compose/DOM shape into shared code once, the same way `AndroidCollapsible`
already does for Android. That is the plan below.

## What `biome patterns` actually found, grounded

| Finding | Where (confirmed by reading) | Nature |
|---|---|---|
| Expandable section ×6 | `AndroidCollapsible`/`AndroidInfoCard` (`kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidWidgets.kt:55-90`, already shared *within* Android); Desktop has **no equivalent** — `DesktopCardSections.kt`, `DesktopStyleBlocks.kt`, `DesktopLifehackBlock.kt` define no collapsible helper; web's `applyCollapsible` (`TrainingCardDom.kt:528`, already a single shared `internal fun` reused by `TileBuilderWeb.kt`) | Three hosts, three independent implementations of "one panel open, animate height" — expected (Compose vs DOM can't share render code), but Desktop is missing the hoist Android already did |
| `Row`+`horizontalScroll` ×16 | `MatrixScreen.kt`, `TrainingScreen.kt`, `ProgressScreen.kt`, `DesktopSettingsScreen.kt`, `DesktopCardSections.kt` (desktop); `VocabularyScreen.kt` (commonMain) | Mostly desktop-side repeats of "scrollable row of chips/cells"; no shared `commonMain` component wraps the `Modifier.horizontalScroll(rememberScrollState())` + `Row` pairing |
| `navBtn` ×4 | `private fun navBtn` redefined in `ExamReadingWeb.kt:285`, `ExamListeningWeb.kt:460`, `ExamWritingWeb.kt`, `ExamGrammarWeb.kt` — identical DOM button factory (`type="button"; className = "exam-nav-btn" (+ " primary")`) | Literal copy-paste of a private helper across four exam files, same host (web/DOM), zero variation |

None of these are domain-rule duplication; all are UI-shape duplication. Per Pattern Dictionary §4 that
belongs to "existing DSL or native utility" (small case) — here, a native Kotlin utility/composable, since
there is no existing DSL construct for it and the case is small.

## Decision logic actually worth extracting (plain Kotlin, not DSL)

While reading the four files, three rules turned out to be **inline in a single host file today**, which is
exactly the SB-9/10 risk: Android's tile builder will have to re-derive them independently unless they move
to `commonMain` first.

| Rule | Currently inline at | Risk if left alone |
|---|---|---|
| Tile builder "Check" enabled only when every choice slot has a placed form | `TileBuilderWeb.kt:149` — `checkButton.disabled = choices.any { c -> builder.placed.none { it.position == c.position } }` | SB-9 (Android) and SB-10 (iOS, via the Kotlin bridge) will re-derive this by eye; one of three can drift |
| Picker/error panel "only one open at a time" + close-on-select | `TileBuilderWeb.kt:72-95` (`togglePicker`/`toggleError`), expressed as local `var` state, not a pure function | Same risk — no shared function to call from a Compose renderer |
| Achievement row shown only at `tileScaffoldingRung == 2`, with optional `independentSince` date | `ProgressWeb.kt:80-98` (`renderAchievementsSection`) | **Not yet duplicated** — `AndroidProgressScreen.kt`/`ProgressScreen.kt` (desktop) have no achievements section at all. This is the cheapest moment to extract it, before a second host re-derives it |

Daily-session bounds (`todayCount`/`dueCount`, M3) already have Android/Desktop references
(`AndroidTrainingScreen.kt`, `Main.kt`) — worth a quick grep-level check by the developer that they call one
shared function rather than three copies, but it's out of this plan's critical path since no duplication was
found by `biome patterns` there.

## Design: plain Kotlin dedup (recommended, in place of DSL)

### Public contract

New pure functions in `commonMain/polski/presentation/` (file: `TileBuilderRules.kt`, `NEW`):

```kotlin
/** True exactly when every TileSegment.Choice in `exercise` has a placed form. SB6-AC: gates "Проверить". */
fun tileBuilderCanCheck(segments: List<TileSegment>, placed: List<PlacedTile>): Boolean

/** Next open-panel id after tapping `tapped`, given `current` — toggle-closes, opening closes any other. */
fun <Id> togglePanel(current: Id?, tapped: Id): Id?
```

New pure function in `commonMain/polski/presentation/` (file: `AchievementRules.kt`, `NEW`):

```kotlin
data class SkillAchievement(val skillId: String, val streak: Int, val independentSince: String?)

/** Skills currently at tileScaffoldingRung == 2, in `skills` order. Empty list ⇒ caller shows no section. */
fun achievedSkills(skills: List<Skill>, stats: Map<String, SkillStats>): List<SkillAchievement>
```

New shared Compose components in `commonMain/polski/ui/` (file: `Collapsible.kt`, `NEW` — ports the existing
`AndroidCollapsible` body, drops the `Android` prefix):

```kotlin
@Composable
fun Collapsible(visible: Boolean, reduceMotion: Boolean, content: @Composable () -> Unit)

@Composable
fun ScrollableChoiceRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit)
```

New shared web helper in `webMain/polski/ui/` (file: `ExamNavBtn.kt`, `NEW`):

```kotlin
internal fun navBtn(label: String, primary: Boolean = false, onClick: () -> Unit): HTMLButtonElement
```

### Components and dependencies

- `TileBuilderWeb.kt` (web) calls `tileBuilderCanCheck`/`togglePanel` instead of its inline `any{}`/local-`var`
  logic — behavior-preserving, same `HTMLElement` DOM structure.
- `ProgressWeb.kt` (web) calls `achievedSkills` instead of the inline `filter` in `renderAchievementsSection`.
- `AndroidWidgets.kt` keeps `AndroidInfoCard`; `AndroidCollapsible` is deleted in favor of the new shared
  `Collapsible` (commonMain) — Android call sites switch import, no behavior change (identical
  `AnimatedVisibility` body).
- Desktop screens (`DesktopCardSections.kt`, `DesktopStyleBlocks.kt`, `DesktopLifehackBlock.kt`,
  `DesktopSettingsScreen.kt`, `MatrixScreen.kt`) adopt `Collapsible`/`ScrollableChoiceRow` where they currently
  hand-roll the same shape — this is the actual duplication removal for the ×6/×16 findings.
- `ExamReadingWeb.kt`, `ExamListeningWeb.kt`, `ExamWritingWeb.kt`, `ExamGrammarWeb.kt` delete their private
  `navBtn` and import the shared one.
- SB-9 (Android tile builder, `NEW` — not yet built): its renderer calls `tileBuilderCanCheck`/`togglePanel`
  and `Collapsible` directly — no re-derivation, which is the concrete "simplifies SB-9/10" outcome.
- SB-10 (iOS, `NEW`): consumes the same `commonMain` functions through the existing Kotlin↔Swift bridge; the
  SwiftUI view stays hand-written Swift (no DSL/Swift printer exists in biome-dsl — confirmed, `lang-swift`
  absent), but the *rule* is shared, not reimplemented.

### State and resources

No new state ownership: `tileBuilderCanCheck`/`togglePanel`/`achievedSkills` are pure, no side effects, no
coroutines. `Collapsible` owns no resources beyond the `AnimatedVisibility` it already wraps (unchanged from
`AndroidCollapsible` today). `navBtn` owns no listener lifetime beyond the one `addEventListener` per call,
same as today's four copies.

### Decisions and tradeoffs

- Rejected: `.feature`/FLOW DSL migration (see Verdict). Rejected: adopting the unwired `compose-tween`/
  `compose-module` packs now — they are untested, zero-consumer, and only cover animation/lifecycle wiring,
  not the layout duplication actually reported.
- Accepted: plain Kotlin hoist. Matches `module-architecture` skill guidance (`commonMain` for shared pure
  logic and Compose components; platform source sets keep only genuinely platform-specific code) and costs
  four small, independently reviewable files.
- Deferred, explicit trigger: if tile-builder validity or achievement rules grow materially more branchy
  (e.g., multiple scaffolding tiers with distinct unlock conditions, the way exam commit/feedback/advance
  did), revisit Logic DECISION then — same pattern as the exam feedback ADR, not a speculative pack today
  (biome-dsl P13: "Пак «на будущее» не добавляется").

### Verification

- Unit tests (`commonTest`): `tileBuilderCanCheck`, `togglePanel`, `achievedSkills` — table-driven, covering
  the existing inline branches (`error == null && placedForm != null`, all-placed, none-placed, rung!=2,
  rung==2 with/without `independentSince`).
- Compose UI test: `Collapsible`/`ScrollableChoiceRow` reduced-motion vs animated paths (per
  `compose-multiplatform-ui` skill: verify old controls aren't actionable mid-exit).
- Playwright (web): existing `TileBuilderWeb`/`ProgressWeb` specs must stay green unchanged — this is a pure
  refactor, no behavior change; re-run `biome patterns` after to confirm the four counts drop.
- Desktop/Android: existing screenshot or semantics tests for the touched screens, per `kmp-android`/
  `kmp-desktop` skills.

## Order (small tasks, one file-unit each, sequential)

1. `TileBuilderRules.kt` + `AchievementRules.kt` (commonMain, `NEW`) with unit tests — extract from
   `TileBuilderWeb.kt`/`ProgressWeb.kt`, no behavior change.
2. `TileBuilderWeb.kt`/`ProgressWeb.kt` switch to call the extracted functions — Playwright regression check.
3. `Collapsible.kt`/`ScrollableChoiceRow` (commonMain, `NEW`) — port `AndroidCollapsible` body; `AndroidWidgets.kt`
   drops `AndroidCollapsible`, imports the shared one.
4. Desktop screens adopt `Collapsible`/`ScrollableChoiceRow` where they duplicate the shape (one screen per
   task: `DesktopCardSections.kt`, `DesktopStyleBlocks.kt`, `DesktopLifehackBlock.kt`, then
   `DesktopSettingsScreen.kt`/`MatrixScreen.kt` for the scroll rows).
5. `ExamNavBtn.kt` (webMain, `NEW`) — four exam files switch in one task each or one combined small task
   (same host, mechanical).
6. SB-9 (Android tile builder) and SB-10 (iOS tile builder) proceed per the existing Sentence Builder design
   (`Plans/Architecture/SENTENCE-BUILDER-DESIGN.md` SB-9/SB-10), now calling the step-1/3 shared functions
   instead of re-deriving them — this is where the "simplifies SB-9/10" payoff actually lands.

## Documentation and handoff

- Record the "no `.feature` DSL for UI components" verdict and its two reasons in `AI/decisions.md` if the
  team wants it discoverable later (avoids re-asking the same question before a future SB-9/10 pass).
- `Plans/Architecture/SENTENCE-BUILDER-DESIGN.md` (SB-9/SB-10 rows) should note the new shared-function
  dependency once step 1 lands, so the Android/iOS developer knows not to re-derive the rule.
- Open question for the product owner, not resolved here: whether to ever name a Compose-FLOW stack in
  biome-dsl (which would unblock `compose-tween`/`compose-module`) — out of scope for this migration; no
  current consumer need justifies it (biome-dsl P13).
