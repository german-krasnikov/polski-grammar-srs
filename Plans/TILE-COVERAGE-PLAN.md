# Module design: Tile Builder — full coverage

## Diagnosis

### Current state

26 skills total. Tile builder is currently available on **15 constructed skills** that have
`tileTargetSlots` in `recipes.core-data`. It is blocked on:

| Group | Count | Root cause |
|---|---|---|
| Static exercises (`staticPool`/`staticOverride`) | 11 | `staticExercise()` never sets `tileTargetSlots` or `tilePhrase`; `StaticExercise` has no phrase spec |
| Chain exercises | 5 steps × all seeds | `generateChain()` never sets `tileTargetSlots` or `tilePhrase`; `ChainStepRecipe` has no `tileTargetSlots` |

### Gap A — Chain exercises (ExerciseGenerator.kt line 369–384)

`generateChain` creates `CoreExercise` without `tileTargetSlots` or `tilePhrase` — both default
to empty/null.  `ChainStepRecipe` (Recipe.kt line 111–121) has no `tileTargetSlots` field at all.

Every chain step's `expected` is `TextSpec.Pattern` with exactly **one** embedded `TextValue.Phrase`:

| Step | expected key | Phrase slot |
|---|---|---|
| 1 — nom→acc | `seenAcc` | `acc` → `core.argument.case-role` / Case=acc |
| 2 — verb.past | `chainPast` | `acc` → same |
| 3 — case.gen.neg | `chainNeg` | `gen` → `core.sentence.polarity` / Polarity=neg |
| 4 — agreement.my | `chainOwner` | `theirGen` → `core.agreement.possessive` / Case=gen |
| 5 — case.loc | `chainLoc` | `theirLoc` → `core.argument.case-role` / Case=loc |

`singlePhraseSpec(step.expected)` (ExerciseGenerator.kt line 174–179) already handles
`TextSpec.Pattern` → returns the single `PhraseSpec` for all five steps.  The realization
infrastructure (`realizePhrase`, `TilePhrase`) already works for these constructions.

**Fix is mechanical**: add `tileTargetSlots` to `ChainStepRecipe` + call `tilePhraseForStep` in
`generateChain`.

### Gap B — Static exercises (ExerciseGenerator.kt line 242–247)

`staticExercise()` builds a `CoreExercise` from pre-authored copy keys — no construction
realization at all.  `StaticExercise` (Recipe.kt line 70–82) has no `TextSpec`-typed `expected`
and no `tileTargetSlots`.

The 11 affected skills and their data files in `courses-biome/pl-ru/recipes.core-data`:

- `aspect` (line 820) — `staticOverride`
- `degree.comparison` (line 1089) — `staticPool`, 8 items
- `verb.participles` (line 1173) — `staticPool`, 8 items
- `voice.impersonal` (line 1257) — `staticPool`, 8 items
- `wordform.nouns` (line 1341) — `staticPool`, 8 items
- `numeral.agreement` (line 1425) — `staticPool`, 8 items
- `wordform.adjectives` (line 1509) — `staticPool`, 8 items
- `sentence.connectives` (line 1593) — `staticPool`, 8 items
- `sentence.rephrase` (line 1677) — `staticPool`, 8 items
- `case.valency` (line 1761) — `staticPool`, 8 items
- `verb.mood` (line 1845) — `staticPool`, 8 items

Because these exercises carry pre-authored text, `TileGenerator.fromAnswerPhrase` needs a
`TilePhrase` with `slotSpans` (character-range offsets for each tile slot inside the expected
string).  Without `ConstructionRealizer` being called, those ranges don't exist automatically.

---

## Fix 1 — Chain tilePhrase (low risk, immediate)

### Changes

**`kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/Recipe.kt`**
- Add `val tileTargetSlots: List<String> = emptyList()` to `ChainStepRecipe` after `ownerOut`.

**`kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/ExerciseGenerator.kt`**
- Extract private helper (mirrors `tilePhraseFor`):

```kotlin
private fun tilePhraseForStep(step: ChainStepRecipe, lexicalSlots: Map<String, String>): TilePhrase? {
    if (step.tileTargetSlots.isEmpty()) return null
    val phraseSpec = singlePhraseSpec(step.expected) ?: return null
    val realized = realizePhrase(phraseSpec, lexicalSlots)
    return TilePhrase(phraseSpec.construction, phraseSpec.bundle, realized.text, realized.slotSpans)
}
```

- In `generateChain` (line 372–383), add to each `CoreExercise(...)` call:

```kotlin
tileTargetSlots = step.tileTargetSlots,
tilePhrase = tilePhraseForStep(step, lexicalSlots),
```

**`courses-biome/pl-ru/chainSteps.core-data`**
- Add `tileTargetSlots: [adjective noun]` to each of the 5 `CHAIN` blocks (after `ownerOut`).
- Verify slot names match what `ConstructionRealizer.slotFacts` returns for each construction.

**Acceptance cases (Fix 1)**

- AC1.1 — `generateChain` with `tileTargetSlots: [adjective noun]` in all five steps produces `CoreExercise` where `tileTargetSlots == ["adjective","noun"]` and `tilePhrase != null` for a standard seed.
- AC1.2 — `TileGenerator.fromAnswerPhrase(chainExercise, ScaffoldingLevel.Partial)` returns a non-null `TileExercise` for each of the five step positions.
- AC1.3 — `tilePhrase.text` is a substring of the step's `expected` string (offset ≥ 0 guard passes).
- AC1.4 — A step whose `tileTargetSlots` is left empty (default) still produces `tilePhrase = null` — backward-compatible no-op.
- AC1.5 — Existing `TrainingParityTest` byte-parity fixture passes unchanged (chain exercises have no parity fixture today, so no regression risk).

---

## Fix 2 — Static exercises: author-annotated tilePhrase path (safe, requires DSL authoring)

### Design

`StaticExercise` stores pre-authored text via copy keys.  To reuse the existing `TileGenerator`
path (which requires `TilePhrase.slotSpans` from a real `ConstructionRealizer` call), each static
exercise must carry the `PhraseSpec` whose realization matches its copy-key expected text.

**Step 2a — Extend `StaticExercise`** (Recipe.kt)

```kotlin
data class StaticExercise(
    // ... existing fields ...
    val tileTargetSlots: List<String> = emptyList(),
    /** When non-null, the phrase whose realization exactly matches the tile-target substring
     *  of copy.text(expectedKey) — staticExercise() uses it to compute tilePhrase. */
    val tilePhraseSpec: PhraseSpec? = null,
)
```

**Step 2b — Update `staticExercise()`** (ExerciseGenerator.kt)

```kotlin
private fun staticExercise(skillId: String, s: StaticExercise): CoreExercise {
    val expected = copy.text(s.expectedKey)
    val lexicalSlots = lexicalSlotsFor(s.nounId, s.adjectiveId)
    val tilePhrase = s.tilePhraseSpec?.let { spec ->
        val realized = realizePhrase(spec, lexicalSlots)
        TilePhrase(spec.construction, spec.bundle, realized.text, realized.slotSpans)
            .takeIf { expected.contains(it.text) }  // fail-soft: authoring mismatch → no tiles
    }
    return CoreExercise(
        id = newId(), primarySkill = skillId,
        source = copy.text(s.sourceKey), prompt = copy.text(s.promptKey),
        expected = expected, accepted = s.acceptedKeys.map(copy::text),
        explanation = copy.text(s.explanationKey),
        tags = s.tags, changes = listOf(CoreFormChange(copy.text(s.changeFromKey),
            copy.text(s.changeToKey), copy.text(s.changeReasonKey))),
        slots = mapOf("noun" to s.nounId, "adjective" to s.adjectiveId,
            "owner" to defaultOwnerLexeme, "number" to "sg"),
        tileTargetSlots = s.tileTargetSlots,
        tilePhrase = tilePhrase,
    )
}
```

**Step 2c — Annotate static exercises in recipes.core-data**

Each `StaticExercise` block gains two optional fields:

```
tileTargetSlots: [adjective noun]
tilePhraseSpec: {
  construction: core.argument.case-role
  bundle: [{feature: Number value: sg} {feature: Case value: acc}]
}
```

The `tilePhraseSpec.construction`+`bundle` must realize to a text that is a verbatim substring of
the exercise's `expectedKey` copy value.  This is a **content authoring task** — verify with
`biome generate` output or a unit test that checks `expected.contains(realized.text)`.

**Acceptance cases (Fix 2a)**

- AC2.1 — A `StaticExercise` with `tileTargetSlots` and a matching `tilePhraseSpec` produces `tilePhrase != null` after `staticExercise()`.
- AC2.2 — A `StaticExercise` with a `tilePhraseSpec` whose realized text is NOT a substring of `expectedKey`'s copy value produces `tilePhrase = null` (fail-soft).
- AC2.3 — A `StaticExercise` with empty `tileTargetSlots` and no `tilePhraseSpec` produces the same `CoreExercise` as today — no regression.
- AC2.4 — `TileGenerator.fromAnswerPhrase` returns non-null for a fully annotated static exercise.

---

## Fix 3 — Static exercises: word-split fallback `StaticTileBuilder` (optional, broader coverage)

For static exercises where `tilePhraseSpec` annotation is impractical (e.g., sentence.rephrase
involves complex paraphrase, not a simple case form), a word-split path can cover them without
morphological construction lookup.

### Design (NEW module or class inside `core-engine`)

```
NEW kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/StaticTileBuilder.kt
```

**Public contract**

```kotlin
/**
 * Builds a TileExercise from a pre-authored expected string using whitespace-split word positions.
 * Distractors come from MorphologyParadigm.formsOf on the nounId/adjectiveId provided by StaticExercise.
 * Returns null when the expected string has fewer than 2 tokens (nothing to pick from).
 */
object StaticTileBuilder {
    fun fromStaticExercise(
        expected: String,
        nounId: String,
        adjectiveId: String,
        morphology: MorphologyParadigm,
        scaffolding: ScaffoldingLevel,
        random: RandomSource,
    ): TileExercise?
}
```

**Behavior**

1. Split `expected` by whitespace → token list with character offset per token.
2. Build a word→forms lookup for `nounId` and `adjectiveId` using `MorphologyParadigm.formsOf`.
3. For each token: if the token text appears as a form of noun/adjective, it becomes a
   `TileSegment.Choice` whose `alternatives` are other forms of the same lexeme.  Otherwise
   `TileSegment.Locked`.
4. Apply scaffolding ratio (same `Policy.disposition` / `TILE_REVEAL_OUTCOME_ROW_ROWS` lookup).
5. Return `TileExercise(segments, scaffolding)`.

**Dependency**: `MorphologyParadigm` (already in `core-engine`), `RandomSource`, `ScaffoldingLevel`,
`Policy` / `TILE_REVEAL_OUTCOME_ROW_ROWS` (already imported by `TileGenerator`).

**Limitations**
- Reverse lookup (form → is this noun/adjective?) depends on the morphology paradigm covering
  every surface form.  Ambiguous forms (a word that is both nom-sg-noun and something else) may
  produce a false match — flag for content review.
- Distractors are morphological alternants of noun/adjective only — no paradigm-external noise.
- Does NOT reuse `TileGenerator` internals; produces the same `TileExercise` output type.

**Acceptance cases (Fix 3)**

- AC3.1 — `fromStaticExercise` with a known expected string containing a recognizable noun form
  returns `TileExercise` with at least one `Choice` segment.
- AC3.2 — Every `Choice` segment's `alternatives` list contains only real morphological forms,
  none identical to the correct form.
- AC3.3 — `fromStaticExercise` returns `null` when `expected` has fewer than 2 tokens.
- AC3.4 — Scaffolding ratio is applied: `ScaffoldingLevel.Full` → all identified slots as Choice;
  `ScaffoldingLevel.Partial` → subset per existing ratio table.

---

## Implementation order

| Step | File(s) | Risk | Unlocks |
|---|---|---|---|
| 1 | `Recipe.kt` + `ExerciseGenerator.kt` + `chainSteps.core-data` | Low | 5 chain steps × all seeds |
| 2a | `Recipe.kt` + `ExerciseGenerator.kt` | Low | Static exercises that get annotated |
| 2b | `recipes.core-data` (content authoring) | Medium (mismatch fails soft) | Each annotated static exercise |
| 3 | `StaticTileBuilder.kt` (NEW) + wire-up in host | Medium (reverse morph lookup) | All remaining static exercises |

Steps 1 and 2a are independent and can proceed in parallel.  Step 3 is optional and extends
coverage to exercises where step 2b annotation is impractical.

---

## Documentation and handoff

- **Recipe.kt changes** (steps 1 + 2a): add KDoc to `tileTargetSlots` on `ChainStepRecipe` and to
  `tileTargetSlots`/`tilePhraseSpec` on `StaticExercise`.
- **chainSteps.core-data**: comment each `tileTargetSlots` addition with the construction name so
  content authors understand which slot names are valid.
- **StaticTileBuilder** (step 3): KDoc must state the reverse-lookup limitation and the fail-soft
  behavior (no match → Locked).
- **Open question**: Does `ConstructionRealizer.slotFacts` return slot names `"adjective"` and
  `"noun"` for all five chain constructions?  Verify before shipping step 1 to `chainSteps.core-data`.
