# Module design: Sentence Builder SB-7 (per-error explanation) + SB-8 (progress-driven scaffolding)

Date: 2026-11-13. Status: **proposed**. Builds on SB-1..SB-6 (already implemented; three recipes
opted in: `case.acc.f`, `case.acc.m`, `case.gen.neg`).

## Scope and consumers

Two independent fixes to known placeholders in `kotlin/shared/src/commonMain/kotlin/polski/presentation/TrainingStore.kt`:

- **SB-7**: `errorExplanationFor` (lines 364-368) always returns a generic `Block.Rule(skill.theory, exercise.explanation, emptyList())`, ignoring which tile was tapped. Replace with a real `StyleComposer.blocksForError` that is specific to the tapped `TileError`.
- **SB-8**: `ScaffoldingLevel.Partial` is hardcoded in `deriveTileBuilder` (line 89, via `tileBuilderStateFor`'s default param). Replace with a level derived from per-skill attempt history.

Both are `commonMain` changes inside `:shared` (consumer: `TrainingStore`, already the one caller of both
`StyleComposer` and `tileBuilderStateFor`) plus one `:srs-progress` change (SB-8's new persisted fields).
No platform (`webMain`/`androidMain`/`iosMain`) code changes — `TileBuilderWeb.kt`'s `renderCardBlocks` already
renders whatever `Block` list it is given (verified at `kotlin/composeApp/src/webMain/kotlin/polski/ui/TileBuilderWeb.kt:134-136`),
and nothing platform-side reads `scaffolding` directly (confirmed by `SENTENCE-BUILDER-WEB-BLUEPRINT.md`'s own
SB-8 flag). Consumers of `:srs-progress` across hosts (Android/iOS/desktop all go through the same `ProgressRepository`)
get the new fields for free once `ProgressCodec` is updated.

## SB-7: `StyleComposer.blocksForError`

### Public contract

```kotlin
// kotlin/shared/src/commonMain/kotlin/polski/presentation/StyleComposer.kt
object StyleComposer {
    // existing compose()/resolveEffectiveStyle() unchanged
    fun blocksForError(
        exercise: Exercise,
        skill: Skill,
        focus: SkillPresentation,
        content: SkillStyleContent,
        error: TileError,
    ): List<Block>
}
```

Pure function, same purity contract as `compose()` (same inputs -> same output; never reads FSRS/progress/
preferences; never throws — `error` is trusted to belong to `exercise`'s own `TileExercise`, which
`TrainingStore` already guarantees structurally since `error` always comes from
`evaluateTileAnswer(tileBuilder.exercise, ...)` for that same `exercise`, same as `compose()` trusts its
own `exercise`/`skill`/`focus` triple today).

Returns exactly 2 blocks, in order:
1. `Block.Rule(ruleText, exercise.explanation, styleParts(ruleText, focus))` — the skill-level "why" (e.g.
   `accExplanation` = *"Widzę требует Biernik. Согласуй притяжательное местоимение, прилагательное и
   существительное."*), with `ruleText = content.rule ?: skill.theory` — **identical derivation** to
   `compose()`'s own `BlockKind.Rule` branch (factor out a shared private `ruleBlock(...)` helper so the two
   never drift).
2. `Block.Changes(listOf(ChangeItem(beforeParts, afterParts, reason)))` — the specific diff for *this* tile:
   `beforeParts`/`afterParts` come from `changeHighlightParts(error.selected, error.expected, side)` (the same
   word-aligned diff utility `compose()`'s `BlockKind.Changes`/`Contrast` branches already use, just applied
   to the tile's own selected/expected word pair instead of the whole-phrase `exercise.changes` pair), and
   `reason` comes from the matching `exercise.changes` entry (next section).

### Matching a `TileError` to an `exercise.changes` entry

`CoreExercise.changes` / `Exercise.changes` (`polski.model.FormChange(from, to, reason)`) is **phrase-granularity
today**, not per-tile: verified against `courses-biome/pl-ru/recipes.core-data` — `case.acc.f`/`case.acc.m` each
declare exactly **one** `changes` entry (whole nominative-phrase → accusative-phrase, e.g. "moja córka" →
"moją córkę") even though `tileTargetSlots: [adjective noun]` makes **two** tiles; `case.gen.neg` is the same
shape (one change, two tile slots). The matching rule:

```kotlin
private fun matchingChange(exercise: Exercise, error: TileError): FormChange? =
    exercise.changes.firstOrNull { it.to.contains(error.expected) } ?: exercise.changes.firstOrNull()
```

This is exact for all three pilot recipes: each `changes[0].to` is resolved from the *same* construction+bundle
as the exercise's embedded `tilePhrase` (verified in `ExerciseGenerator.kt` — `tilePhraseFor`'s `singlePhraseSpec`
and the recipe's own `changes[0].to` spec both realize `core.argument.case-role` with the identical `Case`/`Number`
bundle for `case.acc.f`/`case.acc.m`, and `core.sentence.polarity` with the identical `Polarity`/`Number` bundle
for `case.gen.neg`), so `error.expected` (a substring of `tilePhrase.text`, per `TileGenerator.buildSegments`) is
always a substring of `changes[0].to`. The `firstOrNull()` fallback makes the rule degrade safely (never throws,
never returns null reason — the caller still falls back to `exercise.explanation`) if a future recipe ever
declares zero `changes` or several non-matching ones.

**This is intentionally substring-based, not identity-based**, so it keeps working unchanged if a future
recipe splits `changes` into one entry per tile slot (each entry's own `to` would then just be the slot's own
form, still found by `contains`) — no `core-model`/DSL schema change is required to adopt this blueprint.

### Known content gap (not a code task)

The example in the task brief — *"Прилагательное ж.р. в Bierniku: -a → -ą"* — is **more specific prose** than
what `case.acc.f`'s actual authored reason currently says. Read from `courses-biome/pl-ru/exerciseCopy.core-data`:
`accReasonChanged` = *"Изменяется вся группа слов."* (generic). The DSL **already supports** gender-specific
reasons — `ReasonSpec.Classified` (used by `accReasonPersonalA`, `whenGender: m-personal`) is exactly the
mechanism to add a `whenGender: f key: accReasonFeminine` rule with the specific `-a → -ą` prose. `blocksForError`
will surface whatever reason text the DSL resolves — getting the *exact* wording from the example is **a
content-authoring follow-up** (new `COPY`/rule entries in `recipes.core-data`/`exerciseCopy.core-data`), not a
`StyleComposer` change, and consistent with this project's DSL-first principle (business-specific prose lives
in `.core-data`, never synthesized as a Kotlin string template). Flagging for whoever picks up the content task;
not blocking SB-7's code landing with today's (generic) reason text.

### `TrainingStore` wiring

```kotlin
// replaces lines 364-368
private fun errorExplanationFor(state: AppUiState, position: Int): List<Block> {
    val exercise = state.exercise ?: return emptyList()
    val error = state.tileBuilder?.result?.errors?.firstOrNull { it.position == position } ?: return emptyList()
    val skill = skillById(exercise.primarySkill)
    val focus = presentationBySkillId(exercise.primarySkill)
    val content = styleContentBySkillId(exercise.primarySkill)
    return StyleComposer.blocksForError(exercise, skill, focus, content, error)
}
```

New imports needed in `TrainingStore.kt`: `polski.data.presentationBySkillId`, `polski.data.styleContentBySkillId`
(both already exist, used today by `TrainingCardModel.kt:89-94` for the exact same per-skill lookups — no new
lookup functions needed). `state.tileBuilder?.result?.errors` is already guaranteed non-null-for-this-position by
the existing `accepts()` guard for `TileAction.TapError` (`TrainingStore.kt:216`), so the `firstOrNull` is a
defensive fallback, not the primary guard.

### Verification

- `kotlin/shared/src/commonTest/kotlin/polski/presentation/StyleComposerTest.kt` (existing file, if present —
  else new): `blocksForError` for a `case.acc.f` fixture exercise + a synthetic `TileError(position, selected =
  wrong adjective form, expected = correct form, feature = Case)` returns 2 blocks; the `Block.Changes` item's
  `before`/`after` parts join back to `selected`/`expected` exactly (same invariant `Block.Formula`/`Block.Rule`
  KDoc already documents for `parts`); the `Block.Rule` text equals `skill.theory` (no `content.rule` authored
  for pl today) and highights the same focus pair every other `Rule` block highlights.
- `kotlin/composeApp/src/commonTest/kotlin/polski/presentation/TrainingStoreTest.kt:570-597` — the existing
  `wrongTileFormIsIncorrectAndTapErrorFillsMiniRulePanel` test already asserts `explanation.isNotEmpty()`; extend
  it (or add a sibling test) to assert the explanation now contains a `Block.Changes` item whose `after` parts
  join back to the tapped tile's own `correctForm`, not just "any non-empty list" — this is the regression guard
  that actually proves SB-7 landed (today's placeholder would also pass the weaker `isNotEmpty()` check).

## SB-8: progress-driven scaffolding

### Why not reuse `SkillStats.streak`

`SkillStats.streak` (consecutive-correct) already exists in `kotlin/srs-progress/.../Progress.kt`, incremented
by `ReviewReducer.recordReview` on **every** correct review regardless of `AnswerMode` (Oral/Typed/TileBuilder
all feed the same counter — `ReviewReducer.kt:27-33`). Reusing it directly for scaffolding would mean a learner
who has drilled a skill via Typed/Oral for a while and switches to TileBuilder for the first time lands straight
on `None` (hardest, no help) despite zero contact with the tile mechanic — contradicts the design doc's "первый
контакт **с конструкцией**" framing (`SENTENCE-BUILDER-DESIGN.md:158-167`). Scaffolding needs its own counter,
updated only by TileBuilder-mode reviews, tracked at the same per-skill granularity `SkillStats` already uses
(no broader "construction" grouping exists anywhere in the codebase — skill-level is the only granularity every
other progress signal uses, so this reuses that, not a new concept).

### Where it lives: `Progress.kt` (`:srs-progress`), not `core-model`

`:srs-progress` has **zero** dependency on `:core-model` (verified: `grep project(":" kotlin/srs-progress/build.gradle.kts`
returns nothing; only `:core-engine` and `:shared` declare `api(project(":core-model"))`). `ScaffoldingLevel` is a
`core-model` enum, so `SkillStats` cannot reference it directly without inverting that dependency. `SkillStats` is
already a fully-primitive data class (no enums at all, not even `Rating` — that enum lives in the same
`polski.srs` package as a plain `Int`-backed enum, `Scheduler.kt:5`), so staying primitive here matches existing
project style, not just avoiding the dependency:

```kotlin
// kotlin/srs-progress/src/commonMain/kotlin/polski/progress/Progress.kt
data class SkillStats(
    val reviews: Int = 0,
    val correct: Int = 0,
    val streak: Int = 0,
    val mistakes: Int = 0,
    /** SB-8: sentence-builder scaffolding rung for this skill — 0=Full (most help), 1=Partial,
     *  2=None (least help). A plain Int, not core-model's `ScaffoldingLevel`: srs-progress has no
     *  dependency on core-model (see module-architecture note). Independent of [streak], which
     *  mixes every AnswerMode — this rung advances only from TileBuilder-mode reviews
     *  (ReviewReducer's new `tileCorrect` param). Default 0: a never-drilled-via-tiles skill, and
     *  every pre-SB-8 stored document (decoded with this field absent), starts at Full. */
    val tileScaffoldingRung: Int = 0,
    /** Consecutive correct TileBuilder-mode reviews at the CURRENT [tileScaffoldingRung] only —
     *  reset to 0 on a wrong Tile review or whenever the rung itself changes (promote/demote).
     *  Default 0, same compatibility reasoning as [tileScaffoldingRung]. */
    val tileScaffoldingStreak: Int = 0,
)
```

New fields appended **last** with defaults: every existing positional-argument test construction
(`SkillStats(3, 1, 0, 2)` in `ReviewReducerTest.kt:37`, etc.) keeps compiling unchanged.

### `ProgressCodec` compatibility (both wire formats)

`ProgressCodec.decode`/`ProgressCodec.encodeLegacyV1` (JSON v1, React-compatible, used only by
`migrateLegacy` — `ProgressRepository.kt:138-187`) and `ProgressCodec.decodeText`/`encodeText` (`POLSKI-TEXT`,
the live `save()`/`load()` format — `ProgressRepository.kt:83-106`) currently read `reviews`/`correct`/
`streak`/`mistakes` as **required** (`?: return DecodeResult.Invalid(...)` on missing — `ProgressCodec.kt:59-62`,
`199-202`). The two new fields must **not** follow that pattern, or every progress document saved before SB-8
would fail to load:

```kotlin
// decode() / decodeText(): default, never invalidate
tileScaffoldingRung = value.nonnegativeInt("tileScaffoldingRung") ?: 0,
tileScaffoldingStreak = value.nonnegativeInt("tileScaffoldingStreak") ?: 0,
```

(mirror for the `WireNode.WireI64` reads in `decodeText`). `encodeText`/`encodeLegacyV1` always write both
fields going forward, so a round-tripped document is fully self-describing; a pre-SB-8 document decodes with
both at `0` (Full, no streak) and gets written back with explicit `0`s the next save — no migration step needed.

`encodeLegacyV1`'s own round-trip inside `migrateLegacy` (`ProgressRepository.kt:172-181`) only ever compares
its own output against its own `decode()` (self-consistency, not byte-parity against the original legacy raw
string — confirmed by reading that function), so adding two always-present keys there is safe. **Verification
task, not re-derived here**: re-run `ProgressCodecTest`, `ProgressRepositoryTest`, `MigrationFailureTest`
(`kotlin/srs-progress/src/commonTest/kotlin/polski/progress/`) after this change — none of them should assert
an exact/closed field set for `stats` JSON, but confirm rather than assume.

### Promotion/demotion: `ReviewReducer`

```kotlin
// kotlin/srs-progress/src/commonMain/kotlin/polski/progress/ReviewReducer.kt
/** SB-8 pilot thresholds — not derived from any spec, a placeholder the same way
 *  ScaffoldingLevel.Partial was a placeholder before this blueprint; tune during the pilot. */
data class TilePromotionThresholds(val toPartial: Int = 3, val toNone: Int = 5)

class ReviewReducer(
    private val scheduler: Scheduler,
    private val knownSkillIds: Set<String>,
    private val tileThresholds: TilePromotionThresholds = TilePromotionThresholds(),
) {
    fun recordReview(
        document: ProgressDocument,
        skillId: String,
        rating: Rating,
        typedCorrect: Boolean?,
        tileCorrect: Boolean? = null, // SB-8: non-null only when this review's AnswerMode was TileBuilder
        at: Instant,
        localDay: String,
    ): ReviewResult {
        // ... unchanged lookup/scheduler.review/correct computation ...
        val (nextRung, nextTileStreak) = nextTileState(oldStats.tileScaffoldingRung, oldStats.tileScaffoldingStreak, tileCorrect)
        val nextStats = oldStats.copy(
            reviews = oldStats.reviews + 1,
            correct = oldStats.correct + if (correct) 1 else 0,
            mistakes = oldStats.mistakes + if (correct) 0 else 1,
            streak = if (correct) oldStats.streak + 1 else 0,
            tileScaffoldingRung = nextRung,
            tileScaffoldingStreak = nextTileStreak,
        )
        // ... unchanged document.copy(...) ...
    }

    /** null tileCorrect = this review wasn't TileBuilder-mode: rung/streak pass through untouched.
     *  Wrong at rung None rolls back to Partial (design doc §5: "Ошибка на None → откат к
     *  Partial" — the "+ M2 (разбор)" half of that line is SB-7's blocksForError, already shown
     *  on every tap-to-reveal regardless of rung, so no extra plumbing here). Wrong at Full/Partial
     *  only resets the streak (stay, try again) — the design doc is silent on this case; silence
     *  read as "no demotion below where you already were" is the conservative choice. */
    private fun nextTileState(rung: Int, streak: Int, tileCorrect: Boolean?): Pair<Int, Int> {
        if (tileCorrect == null) return rung to streak
        if (!tileCorrect) return (if (rung == RUNG_NONE) RUNG_PARTIAL else rung) to 0
        val nextStreak = streak + 1
        return when (rung) {
            RUNG_FULL -> if (nextStreak >= tileThresholds.toPartial) RUNG_PARTIAL to 0 else RUNG_FULL to nextStreak
            RUNG_PARTIAL -> if (nextStreak >= tileThresholds.toNone) RUNG_NONE to 0 else RUNG_PARTIAL to nextStreak
            else -> RUNG_NONE to nextStreak // already hardest; keep counting, nothing to promote to
        }
    }

    private companion object { const val RUNG_FULL = 0; const val RUNG_PARTIAL = 1; const val RUNG_NONE = 2 }
}
```

### `TrainingStore` wiring

```kotlin
// TrainingStore.kt — new import: polski.core.model.ScaffoldingLevel

private fun scaffoldingLevelFor(rung: Int): ScaffoldingLevel = when (rung) {
    0 -> ScaffoldingLevel.Full
    1 -> ScaffoldingLevel.Partial
    else -> ScaffoldingLevel.None
}

// replaces line 89; `source` defaults to the `document` field so every existing single-arg
// call site (startChain/startSchedule/chooseSkill/rate/resetDecision — TrainingStore.kt:89,
// 278, 293, 320, 404, 465) keeps compiling unchanged AND picks up the right document, because
// in every one of those call sites `document` (the field) is already reassigned to the
// document this exercise's stats should come from BEFORE deriveTileBuilder runs (verified:
// `rate()` sets `document = result.document` before computing `nextExercise`'s tile builder;
// `resetDecision` sets `document = fresh` before `deriveTileBuilder(chain.first())`; `install()`
// sets `document = complete` before the one new call site below).
private fun deriveTileBuilder(exercise: Exercise?, source: ProgressDocument? = document): TileBuilderState? =
    exercise?.let {
        val rung = source?.progress?.stats?.get(it.primarySkill)?.tileScaffoldingRung ?: 0
        tileBuilderStateFor(it, tileRandom, scaffoldingLevelFor(rung))
    }
```

**One real behavior fix bundled in, not optional**: `install()` (`TrainingStore.kt:221-231`) currently never
recomputes `tileBuilder`, so the *first* exercise shown (built in the `mutableState` property initializer,
`TrainingStore.kt:77-83`, before `document` is loaded) keeps whatever scaffolding was derived with `document ==
null` for its entire lifetime on screen, even after real progress loads and could call for a higher rung. Add
`tileBuilder = deriveTileBuilder(state.value.exercise, complete)` to the `state.value.copy(...)` inside
`install()`, right alongside the existing `introPending = needsIntroduction(...)` line (same pattern — recompute
a per-exercise derived field now that the real document is known). Every other call site already recomputes
`tileBuilder` for a *freshly chosen* exercise, so this is the one gap.

`rate()` (`TrainingStore.kt:370-410`) gains one line: the existing `correctness` local is already exactly
`current.tileBuilder?.result?.correct` for `AnswerMode.TileBuilder` and `null` otherwise, so:

```kotlin
val tileCorrect = correctness.takeIf { current.answerMode == AnswerMode.TileBuilder }
val result = reviewReducer.recordReview(before, exercise.primarySkill, action.rating, correctness, tileCorrect, captured.at, captured.localDay)
```

### Decisions and tradeoffs

| Decision | Why | Alternative considered |
|---|---|---|
| Rung as plain `Int` in `SkillStats`, mapped to `ScaffoldingLevel` only in `:shared` | Preserves `srs-progress`'s zero-dependency-on-core-model boundary; matches existing all-primitive `SkillStats` style | A `ScaffoldingRung` enum local to `srs-progress` (mirroring `Rating`'s `wire: Int` pattern) — viable, slightly more type-safe, rejected only for less churn; flag as acceptable alternative if a reviewer prefers it |
| Separate `tileScaffoldingRung`/`tileScaffoldingStreak`, not reusing `streak` | `streak` mixes every `AnswerMode`; scaffolding must track contact with the tile mechanic specifically (design doc's "первый контакт с конструкцией") | Reuse `streak` directly — rejected: wrong semantics, verified against `ReviewReducer.kt:27-33` |
| Thresholds as constructor-injected `TilePromotionThresholds`, not hardcoded constants | Testable without reflection/subclassing (testing-tdd: tunable parameters); explicitly flagged in the design doc as "параметры пилота" | Hardcoded private constants — rejected: harder to drive a short-promotion-path test, and the design doc explicitly asks for these not to be baked in |
| Wrong answer at Full/Partial resets streak but does not demote | Design doc only specifies demotion from `None`; silence elsewhere read conservatively | Demote on any wrong answer at any rung — plausible, but not what the doc says; flag as an open product question, not resolved here |
| New fields appended last, defaulted to 0 on missing in both codecs | Required-field decoding (current pattern) would invalidate every pre-SB-8 saved document | Bump `Progress.version`/wire schema version — rejected: unnecessary, these are purely additive and backward-readable without a version bump |

### Verification

- `ReviewReducerTest.kt`: new tests driving `recordReview(..., tileCorrect = true/false, ...)` through a
  sequence of reviews — promotion at exactly `toPartial`/`toNone` thresholds, no promotion on `tileCorrect =
  null` (Oral/Typed reviews don't move the rung), demotion from `None` on one wrong `tileCorrect = false`,
  no demotion from `Full`/`Partial` on wrong (streak resets to 0 instead).
- `ProgressCodecTest.kt`: round-trip a document through `encodeText`/`decodeText` with non-zero
  `tileScaffoldingRung`/`tileScaffoldingStreak`; decode a **pre-SB-8 fixture string** (missing both fields) and
  assert it decodes `Valid` with both at `0`, not `Invalid`.
- `TrainingStoreTest.kt`: extend or add near the existing TileBuilder tests (`:565-614`) — after enough
  `TileAction.Check` + `AppAction.Rate` cycles at `case.acc.f` with all-correct tile answers, the next
  `tileBuilder.exercise.scaffolding` is `Partial` then `None`; one wrong cycle at `None` brings it back to
  `Partial`. Also a dedicated regression test for the `install()` fix: seed a `FakeRepository` with a document
  whose `case.acc.f` stats already have `tileScaffoldingRung = 2`, start the store, switch to
  `AnswerMode.TileBuilder` on the very first exercise, and assert `tileBuilder?.exercise?.scaffolding ==
  ScaffoldingLevel.None` (today's code would show `Full`/whatever stale default the pre-load initializer picked).

## Documentation and handoff

- No user-facing docs change — both are internal mechanics, not new UI surface (SB-6 already shipped the UI).
- Content-authoring follow-up (SB-7's "known content gap" above — per-gender `ReasonSpec.Classified` rules for
  `case.acc.f`/`case.acc.m`) is a separate task for whoever owns `courses-biome/pl-ru/*.core-data`; not blocking
  this blueprint's code landing.
- Open product question (flagged, not resolved): should a wrong TileBuilder answer at `Full`/`Partial` demote,
  or only reset the streak as designed here? Needs a product decision, not an architecture one, before pilot
  thresholds are tuned.
- Implementation order suggestion: SB-7 first (pure, no persisted-schema risk, smaller diff), SB-8 second (touches
  persisted `SkillStats` shape — wants its own focused review before the two land together).
