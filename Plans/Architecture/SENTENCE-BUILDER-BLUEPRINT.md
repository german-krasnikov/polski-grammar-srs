# Sentence Builder: blueprint (SB-1..SB-5, core only)

Дата: 2026-11-13. Статус: **к реализации**. Заземляет `Plans/Architecture/SENTENCE-BUILDER-DESIGN.md` на реальный код. Не меняет и не отменяет дизайн-документ — уточняет механику там, где дизайн предполагал возможности, которых у движка сегодня нет.

## 0. Что показало чтение кода (gap vs. design doc)

1. **`CoreExercise.slots: Map<String, String>`** (`kotlin/core-model/.../CoreExercise.kt:33`) — это ID лексем (`"noun" -> "książka"`), не позиции в `expected`. `expected` — plain `String`, без spans. Наивная токенизация `expected.split(" ")` + сверка со `slots` (как в дизайне, §4 шаг 1-2) не работает: реальная поверхностная форма ("książkę") не равна lexeme id ("książka").
2. **`RealizedSentence.slotSpans`** (`core-model/.../CoreExercise.kt:9`) — уже существует и уже считается в `ConstructionRealizer.realize()` (`core-engine/.../ConstructionRealizer.kt:63-80`), но **выбрасывается**: `ExerciseGenerator.resolvePhrase()` берёт только `.text` (`ExerciseGenerator.kt:133`). Это единственный источник точных границ слов — его нужно не выбрасывать, а протащить до `CoreExercise`.
3. **`Morphology` — это не паради́гма.** Интерфейс (`core-engine/.../Morphology.kt:10-12`) даёт `form(lexeme, bundle): String` — одну форму по известному bundle. Нет метода "дай все формы лексемы" — `TableMorphology.forms` (приватное поле) не выставлено наружу. Дизайн §4 шаг 3 ("через `Morphology.formsOf(lemma, featureKey)`") описывает метод, которого нет. Нужна новая способность enumerate-а.
4. **Per-slot `FeatureBundle` (Case/Gender/Number для конкретного слота) — приватная деталь `ConstructionRealizer.slotBundle()`** (`ConstructionRealizer.kt:89-100`), не возвращается наружу. Без неё `TileGenerator` не может понять, под каким bundle искать формы-дистракторы для слота (`"adjective:nowy"` требует Case+Gender+Number, не просто "любая другая форма").
5. **`expected` чаще всего — `TextSpec.Pattern`**, не прямой `Phrase` (реальный пример: `courses-biome/pl-ru/recipes.core-data:14-29`, `kind: pattern`, один `value` типа `kind: phrase`). Тайлы должны собираться из **одной конкретной фразы внутри `expected`**, а не из всего `expected` целиком.
6. **`AnswerMode` уже существует**: `enum class AnswerMode { Oral, Typed }` в `kotlin/shared/.../presentation/AppUiState.kt:12`, используется в `AppAction.SetAnswerMode`, `CardSection.AnswerInput`, `TrainingCardModel.kt`. SB-4 добавляет один вариант в существующий enum, не создаёт его.
7. **`AppUiState.exercise: Exercise?`** — это `polski.model.Exercise` (`kotlin/shared/.../model/Exercise.kt`), pl-специфичный адаптер над `CoreExercise`, собираемый в `PlExerciseEngine.toExercise()` (`kotlin/shared/.../training/PlExerciseEngine.kt:37-43`). Любое новое поле на `CoreExercise` должно быть явно продублировано на `Exercise` и в этом мапере, иначе `TrainingStore`/UI его не увидят.
8. **DSL-конвейер подтверждён**: `.core-data` → `biome generate` → generated `*Row` Kotlin → ручной маппер `PlRuRecipeRows.kt` → `core-engine`-типы (`SkillRecipe`, `RecipeSet`). `SkillRecipeRow`/`RecipeDefaultsRow` описаны в `dsl/course.data-contract:606-679`. Новое поле — это: контракт + генерируемый Row (автоматически) + ручная правка в `PlRuRecipeRows.kt::toSkillRecipe()`.
9. **Нет равенства `RealizedSentence` в тестах** (`ConstructionRealizerTest.kt` сравнивает только `.text`/`.slotSpans.getValue(...)`, никогда весь объект) — значит новое поле с дефолтом на `RealizedSentence` безопасно, не ломает существующие тесты.
10. **Пакет-агностичность подтверждена архитектурой `PackEngine`** (`kotlin/shared/.../core/PlEngine.kt:36-85`): один класс строит `TableMorphology`+`ConstructionRealizer`+`ExerciseGenerator` для любого пака (pl-ru, en-ru) из его собственных данных. `TileGenerator` добавляется туда же как третий продукт того же `PackEngine` — en-ru получает тайлы бесплатно (SB-AC7) без единой правки.

Эти находки меняют **механику** SB-1..SB-3 относительно дизайн-документа, но не меняют ни один принцип из §2 дизайна (DSL-first, язык-агностично, L1-агностично, обратная совместимость).

## 1. Архитектурное решение: откуда берутся тайлы

Три новых факта должны течь `DSL → SkillRecipe → ExerciseGenerator → CoreExercise`, и из `CoreExercise` — на спрос — в `TileExercise`:

```
SkillRecipeRow.tileTargetSlots (DSL, SB-5)
  → SkillRecipe.tileTargetSlots: List<String>              (core-engine)
  → ExerciseGenerator извлекает ОДНУ Phrase из recipe.expected,
    реализует её (сохраняя RealizedSentence целиком, не только .text)
  → CoreExercise.tileTargetSlots + CoreExercise.tilePhrase   (core-model, SB-1)
  → TileGenerator.fromAnswerPhrase(..., scaffolding)          (core-engine, SB-2, вызывается НА СПРОС)
  → TileExercise                                              (core-model, SB-1)
```

**Ключевое отличие от дизайн-документа:** `CoreExercise` не хранит `tiles: TileExercise?`. `ScaffoldingLevel` — прогресс-зависимая величина (знает `TrainingStore`, не `ExerciseGenerator`), поэтому `TileExercise` не может быть посчитан на момент генерации `CoreExercise`. `CoreExercise` хранит только то, что **не зависит от прогресса**: какие слоты вообще можно сделать `Choice` (`tileTargetSlots`, авторское решение) и факты реализации фразы-ответа (`tilePhrase`, детерминированный побочный продукт генерации). `TileGenerator` вызывается там, где известен `scaffolding` — в `:shared` (SB-4/SB-8), на `Exercise` (куда оба поля продублированы).

## 2. SB-1 — модель в `core-model`

### Файлы
- `kotlin/core-model/src/commonMain/kotlin/polski/core/model/CoreExercise.kt` — **MODIFIED**.
- `kotlin/core-model/src/commonMain/kotlin/polski/core/model/TileExercise.kt` — **NEW**.

### Изменения `CoreExercise.kt`

```kotlin
/** UniversalCorePlan.md-style: the realized "answer phrase" inside one CoreExercise.expected that
 *  TileGenerator decomposes into tiles — the RealizedSentence ExerciseGenerator would otherwise
 *  discard (today it keeps only `.text`). [construction]/[bundle] are the same PhraseSpec fields
 *  recipe.expected's embedded Phrase was realized with — TileGenerator needs them to ask
 *  ConstructionRealizer.slotFacts for each slot's category + exact required FeatureBundle. */
data class TilePhrase(
    val construction: String,
    val bundle: Map<String, String>,
    val text: String,
    val slotSpans: Map<String, IntRange>,
)

data class CoreExercise(
    val id: String,
    val primarySkill: String,
    val source: String,
    val prompt: String,
    val expected: String,
    val accepted: List<String> = emptyList(),
    val explanation: String,
    val tags: List<String>,
    val changes: List<CoreFormChange>,
    val slots: Map<String, String> = emptyMap(),
    /** SB-5: which [slots] keys the recipe allows a sentence-builder UI to turn into a Choice tile.
     *  Empty = this skill has no sentence-builder support (today's default — every existing recipe). */
    val tileTargetSlots: List<String> = emptyList(),
    /** Null when [tileTargetSlots] is empty, OR when ExerciseGenerator could not isolate exactly
     *  one embedded Phrase inside recipe.expected (e.g. a `Prefixed` expected with `prefixCase ==
     *  "voc"`, whose capitalization breaks verbatim substring matching against [expected]) —
     *  TileGenerator must treat null as "no tiles for this instance", never throw. */
    val tilePhrase: TilePhrase? = null,
)
```

Both new fields default so every existing call site (`PlExerciseEngine`, `EnExerciseGeneratorTest`, `TrainingParityTest`, static exercises) compiles and behaves byte-identically. `tiles: TileExercise?` from the design doc is **not** added here — see §1.

### `TileExercise.kt` (NEW)

```kotlin
package polski.core.model

data class TileExercise(
    val segments: List<TileSegment>,
    val scaffolding: ScaffoldingLevel,
)

sealed class TileSegment {
    abstract val position: Int

    data class Locked(override val position: Int, val text: String) : TileSegment()

    /** [lexeme] is the slot's lexeme id (CoreExercise.slots[name]) — reused by the UI to
     *  re-derive the Morphology key together with [TileAnswerEvaluator]'s own lookup; the model
     *  stores no Morphology-internal key format. */
    data class Choice(
        override val position: Int,
        val correctForm: String,
        val lexeme: String,
        val targetFeature: FeatureKey,
        val alternatives: List<String>,
    ) : TileSegment()
}

enum class ScaffoldingLevel { Full, Partial, None }

data class TileSelection(val position: Int, val selectedForm: String)

data class TileError(
    val position: Int,
    val selected: String,
    val expected: String,
    val feature: FeatureKey,
    /** Always null out of core-engine's TileAnswerEvaluator (SB-3) — StyleComposer.blocksForError
     *  (SB-7, out of this blueprint's scope) fills it from CoreExercise.changes/skill metadata. */
    val reason: String? = null,
)

data class TileAnswer(val selections: List<TileSelection>, val correct: Boolean, val errors: List<TileError>)
```

Renamed `lemma` → `lexeme` to match the vocabulary already used everywhere in `core-engine`/`core-model` (`LexemeFeatures`, `lexicalSlots`, `nounId`/`adjectiveId` as lexeme ids) — `core-model` has no concept of "lemma".

### Acceptance (SB-1)
- **AC1.1** `CoreExercise(id=..., ..., slots=...)` without `tileTargetSlots`/`tilePhrase` still compiles and `==`-equals a pre-change literal with the same explicit fields (defaults match) — run existing `EnExerciseGeneratorTest`/`TrainingParityTest`-style fixtures unmodified.
- **AC1.2** `TileExercise`/`TileSegment`/`ScaffoldingLevel`/`TileAnswer`/`TileError`/`TileSelection` compile in `commonMain`, zero platform imports, zero language literals (verified the same way `NoLanguageLiteralsTest` checks `core-engine` today).
- **AC1.3** `core-model` still has zero dependency on `core-engine` or `:shared` (module graph unchanged).

## 3. SB-2 — `TileGenerator` + supporting engine capabilities in `core-engine`

Three additive pieces, in dependency order.

### 3a. `MorphologyParadigm` — NEW capability (Morphology.kt, MODIFIED)

```kotlin
/** Enumerates a lexeme's whole paradigm — every (FeatureBundle, form) TableMorphology knows for it.
 *  A separate interface from [Morphology] (not a default method on it) because a future
 *  rule-based MorphologyPlugin (§4.4) may be unable to enumerate a productive paradigm; only a
 *  table-backed Morphology can promise this for free. */
fun interface MorphologyParadigm {
    fun formsOf(lexeme: String): Map<FeatureBundle, String>
}
```

`TableMorphology` gains the second supertype, trivially:
```kotlin
class TableMorphology(private val forms: Map<String, Map<FeatureBundle, String>>) : Morphology, MorphologyParadigm {
    override fun form(...) = ... // unchanged
    override fun formsOf(lexeme: String): Map<FeatureBundle, String> = forms[lexeme].orEmpty()
}
```
Zero behavior change to existing `form()`; purely additive.

### 3b. `ConstructionRealizer.slotFacts` — NEW public method (ConstructionRealizer.kt, MODIFIED)

Reuses the exact same `applyGovernment`/`slotBundle`/`orderWhen` resolution `realize()` already does privately, so `TileGenerator` never re-implements agreement/government logic:

```kotlin
data class SlotFact(val category: String, val lexeme: String, val requiredBundle: FeatureBundle)

/** The lexeme-category + exact FeatureBundle [realize] would use for each of [construction]'s
 *  slots, given the same [bundle]/[lexicalSlots] — the per-slot facts TileGenerator needs to look
 *  up alternative forms, without this class leaking [templates] or duplicating [slotBundle]. */
fun slotFacts(construction: String, bundle: FeatureBundle, lexicalSlots: Map<String, String>): Map<String, SlotFact> {
    val template = templates[construction] ?: error("ConstructionRealizer: no template for construction=\"$construction\"")
    val resolved = applyGovernment(template, bundle)
    val slots = template.orderWhen.entries.firstOrNull { (c, _) -> matches(c, resolved) }?.value ?: template.slots
    return slots.mapNotNull { slot ->
        val lexeme = lexicalSlots[slot.name] ?: return@mapNotNull null // optional slot absent, same as realize()
        slot.name to SlotFact(slot.category, lexeme, slotBundle(slot, resolved, lexicalSlots))
    }.toMap()
}
```
No existing method signature changes; `realize()` untouched.

### 3c. `TileGenerator` — NEW class

File: `kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/TileGenerator.kt`.

```kotlin
class TileGenerator(
    private val realizer: ConstructionRealizer,
    private val morphology: MorphologyParadigm,
    private val skills: Map<String, SkillSpec>,
    private val random: RandomSource,
) {
    fun fromExercise(exercise: CoreExercise, scaffolding: ScaffoldingLevel, distractorCount: Int = 3): TileExercise? =
        fromAnswerPhrase(exercise.primarySkill, exercise.expected, exercise.slots, exercise.tileTargetSlots, exercise.tilePhrase, scaffolding, distractorCount)

    /** Pure-data overload — callable from :shared on `Exercise` once it carries the same fields
     *  (SB-4), without coupling TileGenerator to CoreExercise's type. */
    fun fromAnswerPhrase(
        primarySkill: String,
        expected: String,
        slots: Map<String, String>,
        tileTargetSlots: List<String>,
        tilePhrase: TilePhrase?,
        scaffolding: ScaffoldingLevel,
        distractorCount: Int = 3,
    ): TileExercise? {
        if (tileTargetSlots.isEmpty() || tilePhrase == null) return null
        val offset = expected.indexOf(tilePhrase.text)
        if (offset < 0) return null // fail-soft: see TilePhrase KDoc

        val bundle = tilePhrase.bundle.entries.associate { FeatureKey(it.key) to FeatureValue(it.value) }
        val facts = realizer.slotFacts(tilePhrase.construction, bundle, slots)
        val focusFeature = skills[primarySkill]?.focus?.feature

        // Which of the DSL-eligible slots actually become Choice THIS instance (scaffolding, §4 of design doc):
        val activeChoices = when (scaffolding) {
            ScaffoldingLevel.Full -> tileTargetSlots.take((tileTargetSlots.size + 1) / 3) // ~30% Choice, rest Locked
            ScaffoldingLevel.Partial, ScaffoldingLevel.None -> tileTargetSlots
        }.toSet()

        val segments = buildSegments(expected, offset, tilePhrase, activeChoices, facts, focusFeature, scaffolding, distractorCount)
        return TileExercise(segments, scaffolding)
    }
    // buildSegments/distractorsFor: private helpers — see §3d below for behavior, not literal code.
}
```

**`buildSegments` behavior** (private, not reproduced as code — these are the acceptance-bearing rules):
1. Locked prefix = `expected.substring(0, offset)` as one `Locked(position=0, ...)` if non-empty.
2. For each slot name in `tilePhrase.slotSpans`, in span order: if the slot is in `activeChoices` → `Choice` (correctForm = substring by span, lexeme = `slots.getValue(name)`, targetFeature = `focusFeature ?: FeatureKey(facts[name].requiredBundle.keys.first().name)`, alternatives = `distractorsFor(facts.getValue(name), correctForm, distractorCount, random)`); else → `Locked` with the same substring (a DSL-eligible slot that scaffolding demoted, or a slot never in `tileTargetSlots` at all).
3. Locked suffix = remainder of `expected` after the phrase, if non-empty.
4. Positions are assigned 0..n in final order.

**`distractorsFor`**: `morphology.formsOf("${fact.category}:${fact.lexeme}")` minus the bundle equal to `fact.requiredBundle`, minus any form text-equal to `correctForm` (syncretism), sampled via `random` down to `distractorCount` (or fewer if the paradigm doesn't have that many distinct forms — never invents text, satisfying SB-AC2).

### Wiring (PackEngine, MODIFIED)
`kotlin/shared/src/commonMain/kotlin/polski/core/PlEngine.kt`: add to `PackEngine`:
```kotlin
fun tileGenerator(random: RandomSource): TileGenerator = TileGenerator(realizer, morphology, skills, random)
```
and a `plTileGenerator(random)` top-level wrapper mirroring `plExerciseGenerator`. Because `PackEngine` is already pack-generic, `en-ru` gets a working `TileGenerator` with this one addition — **no en-ru-specific code**, satisfying SB-AC7 directly.

### Acceptance (SB-2)
- **AC2.1** For a `CoreExercise` with `tileTargetSlots=["adjective","noun"]` and a real `tilePhrase`, `TileGenerator.fromExercise(..., ScaffoldingLevel.None)` returns tiles whose `Locked` + `Choice.correctForm` segments, joined with `" "` in position order, reproduce `expected` exactly on at least one path (fixture test against a real pl-ru skill, e.g. `case.acc.f`).
- **AC2.2** Every `Choice.alternatives` entry is a real value drawn from `MorphologyParadigm.formsOf(...)` for that exact lexeme — never a literal/invented string (fixture asserts membership).
- **AC2.3** `tileTargetSlots=emptyList()` or `tilePhrase=null` → `fromExercise` returns `null`, never throws.
- **AC2.4** `expected.indexOf(tilePhrase.text) < 0` (simulated with a crafted exercise) → returns `null`, never throws.
- **AC2.5** `TableMorphology.formsOf` returns the exact map passed to its constructor for a known lexeme, empty map for an unknown one (no exception — differs intentionally from `form()`'s `error(...)`).
- **AC2.6** Calling the en-ru `PackEngine.tileGenerator(...)` on an en-ru exercise with `tileTargetSlots` set produces tiles via the identical `TileGenerator` class (SB-AC7).

## 4. SB-3 — `TileAnswerEvaluator` in `core-engine`

File: `kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/TileAnswerEvaluator.kt` — **NEW**.

```kotlin
package polski.core.engine

import polski.core.model.TileAnswer
import polski.core.model.TileError
import polski.core.model.TileExercise
import polski.core.model.TileSegment
import polski.core.model.TileSelection

/** Exact-match grading (never Levenshtein — core-model's TileSegment.Choice already enumerates
 *  every legal form, so "close but wrong" is never ambiguous the way free-typed answers are). */
fun evaluateTileAnswer(exercise: TileExercise, selections: List<TileSelection>): TileAnswer {
    val byPosition = selections.associateBy { it.position }
    val errors = exercise.segments.filterIsInstance<TileSegment.Choice>().mapNotNull { choice ->
        val selected = byPosition[choice.position]?.selectedForm
        if (selected == null || selected == choice.correctForm) null
        else TileError(choice.position, selected, choice.correctForm, choice.targetFeature)
    }
    val allFilled = exercise.segments.filterIsInstance<TileSegment.Choice>().all { it.position in byPosition }
    return TileAnswer(selections, correct = allFilled && errors.isEmpty(), errors = errors)
}
```

Deliberately a **top-level function**, not a class — it has no state/dependencies (pure structural comparison over already-resolved `TileExercise`), matching `evaluate()` in `:shared/training/AnswerEvaluator.kt`'s own shape but placed in `core-engine` because `TileExercise`/`TileSelection` are `core-model` types and this logic needs zero pl-specific knowledge (per task's explicit placement). It does **not** import anything from `:shared` — keeps the dependency direction `:shared -> :core-engine`, never reversed.

`TileError.reason` is always `null` here; SB-7 (`StyleComposer.blocksForError`, out of scope) fills it using `CoreExercise.changes`/skill metadata that only `:shared` has in context.

### Acceptance (SB-3)
- **AC3.1** All `Choice` positions filled with `correctForm` → `TileAnswer(correct=true, errors=emptyList())`.
- **AC3.2** One `Choice` filled with a wrong (but listed) alternative → exactly one `TileError` at that position, `feature == choice.targetFeature`, `reason == null`.
- **AC3.3** A `Choice` position missing from `selections` → `correct=false`, no spurious `TileError` for it (an error always has a `selected` value; "missing" is a different UI state the caller already has via `TileAnswer.selections.size < exercise.segments.count { it is Choice }`).
- **AC3.4** `Locked` segments never produce errors regardless of `selections` content (they shouldn't be selectable; evaluator ignores non-`Choice` segments entirely).

## 5. SB-4 — `AnswerMode.TileBuilder` + presentation state in `shared`

### Files
- `kotlin/shared/src/commonMain/kotlin/polski/presentation/AppUiState.kt` — **MODIFIED**: `enum class AnswerMode { Oral, Typed, TileBuilder }`.
- `kotlin/shared/src/commonMain/kotlin/polski/model/Exercise.kt` — **MODIFIED**: add `tileTargetSlots: List<String> = emptyList()`, `tilePhrase: TilePhrase? = null` (same shape as `CoreExercise`, see §1 — `Exercise` needs its own copy since `AppUiState.exercise: Exercise`, not `CoreExercise`).
- `kotlin/shared/src/commonMain/kotlin/polski/training/PlExerciseEngine.kt` — **MODIFIED**: `toExercise()` passes the two new fields through (one-line addition each, same pattern as existing `slots`/`changes` mapping).
- `kotlin/shared/src/commonMain/kotlin/polski/presentation/TileBuilderState.kt` — **NEW**: `TileBuilderState`, `TileAction` (per design doc §8), plus the `TileBuilder` branch wiring into `TrainingStore`'s existing `AppAction`/`CardSection` machinery.

### Contract (unchanged from design doc §8, now grounded)

```kotlin
data class TileBuilderState(
    val exercise: polski.core.model.TileExercise,
    val placed: List<polski.core.model.TileSelection>,
    val result: polski.core.model.TileAnswer?,
    val errorExplanation: List<Block>?,
)

sealed class TileAction {
    data class Place(val position: Int, val form: String) : TileAction()
    data class Remove(val position: Int) : TileAction()
    object Check : TileAction()
    data class TapError(val position: Int) : TileAction()
}
```

`TrainingStore` builds `TileBuilderState` by calling `plTileGenerator(random).fromExercise(...)`-equivalent on the current `Exercise` (via the new `fromAnswerPhrase` overload — §3c) whenever `answerMode == AnswerMode.TileBuilder`, with `scaffolding` resolved from progress (SB-8, out of this blueprint's scope — a placeholder `ScaffoldingLevel.Partial` is an acceptable stub until SB-8 lands, since `TileGenerator`'s contract never requires a "real" scaffolding source).

### Acceptance (SB-4)
- **AC4.1** `AnswerMode.TileBuilder` is a valid enum value; every existing `when (answerMode)` in `TrainingCardModel.kt`/`TrainingStore.kt` either handles it explicitly or the compiler flags an unhandled branch (Kotlin `when` on enum without `else` is exhaustive-checked) — this blueprint requires each such `when` to be grepped and updated, not silently defaulted.
- **AC4.2** `Exercise(..., )` without `tileTargetSlots`/`tilePhrase` still compiles (defaults) — existing `TrainingParityTest` fixtures unaffected.
- **AC4.3** Selecting `AnswerMode.TileBuilder` for a skill whose recipe has no `tileTargetSlots` (every skill today) yields `TileBuilderState` absent/null from the store — UI must have a defined fallback (e.g. stay on Typed) documented for SB-6..10, not fabricated here.

## 6. SB-5 — DSL `tileTargetSlots`

### Files
- `dsl/course.data-contract` — **MODIFIED**: add one field to `RECORD SkillRecipeRow` (line ~606-620):
  ```
  tileTargetSlots: [string] OPTIONAL
  ```
- `courses-biome/pl-ru/recipes.core-data` — **MODIFIED** (opt-in per skill, e.g. `case.acc.f`/`case.acc.n`/`case.acc.m`): add `tileTargetSlots: [adjective noun]` (or whichever slot names that recipe's construction actually names — read from the matching `ConstructionRow.slots[].name`, not assumed).
- `kotlin/shared/src/commonMain/kotlin/polski/data/PlRuRecipeRows.kt` — **MODIFIED**: `toSkillRecipe()` gains `tileTargetSlots = tileTargetSlots.orEmpty()` on the constructed-recipe branch (same pattern as `numberOverride`). **Not** added to the static-recipe `others`-exclusivity map — static/pooled recipes never get tiles in this blueprint (pre-authored text has no construction/bundle to decompose).
- `kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/Recipe.kt` — **MODIFIED**: `SkillRecipe` gains `val tileTargetSlots: List<String> = emptyList()`.
- `kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/ExerciseGenerator.kt` — **MODIFIED**: `generateForSkill`'s constructed-recipe branch, when `recipe.tileTargetSlots.isNotEmpty()`, extracts the single embedded `PhraseSpec` from `recipe.expected` (helper, see §1) via a NEW private `realizePhrase(spec, lexicalSlots): RealizedSentence` (returns the full `RealizedSentence`, unlike the existing `.text`-only `resolvePhrase`), and sets `tileTargetSlots`/`tilePhrase` on the returned `CoreExercise`.

### Deliberate deviation from the original design doc
The design doc proposed a separate `tileEnabled: Boolean` flag defaulting to `true`. This blueprint drops it: **a non-empty `tileTargetSlots` list is itself the opt-in**. One field is simpler, equally safe (every existing recipe has none → tiles stay off everywhere until an author explicitly lists slots), and avoids a `tileEnabled: true, tileTargetSlots: []` contradiction the two-field version would allow.

### Eligibility constraint (must be documented for content authors)
`tileTargetSlots` only works when `recipe.expected` contains **exactly one** `TextValue.Phrase`, reachable as: `Direct(Phrase)`, the sole `Phrase`-typed entry of a `Pattern`'s `values`, the sole `Phrase` among a `Concat`'s `parts`, or `Prefixed` with `prefixCase != "voc"`. `ExerciseGenerator` resolves `tilePhrase = null` (fail-soft, not an error) when this doesn't hold — content authors find out at tile-UI-time (empty tile picker, falls back to Typed/Oral), not at `biome generate` time; this blueprint does not add a `biome generate --check` validation for it (could be a follow-up, out of scope here).

### Acceptance (SB-5)
- **AC5.1** `biome generate --check --project dsl/biome.project` passes after adding the field to the contract and at least one recipe row (round-trips through the generated `SkillRecipeRow`).
- **AC5.2** A pl-ru skill with `tileTargetSlots` set produces a `CoreExercise.tilePhrase != null` and `.tileTargetSlots == [exactly the authored slots]`.
- **AC5.3** Every recipe that does **not** set `tileTargetSlots` produces `CoreExercise.tileTargetSlots == emptyList()` and `.tilePhrase == null` — byte-identical `CoreExercise` otherwise (parity fixtures for untouched skills must not change at all).
- **AC5.4** en-ru's `recipeDefaults.core-data`/`recipes.core-data` need zero changes for SB-1..5 to compile and for existing en-ru exercises to keep working (tiles simply stay off for en-ru skills until an en-ru recipe opts in — SB-AC7 is about the *mechanism* working for en, not about en-ru content being retrofitted in this blueprint).

## 7. Compatibility check (serialization, FSRS, progress)

- `CoreExercise`/`SkillRecipe`/`Exercise` changes are **additive fields with defaults** only — no renamed/removed/reordered field, no changed default for an existing field. Any `kotlinx.serialization` usage of these types (checked: none of `CoreExercise`/`Exercise`/`SkillRecipe` carry `@Serializable` today — they're constructed in-memory per session, not persisted) is unaffected; even if one later gains `@Serializable`, a new field with a default is backward-compatible under the standard decoder.
- **FSRS/progress**: `Progress`/scheduler types are untouched by this blueprint — `TileExercise`/`TileAnswer` never reach `:srs-progress`. SB-4's `TrainingStore` integration must route a `TileAnswer.correct` result through whatever `Rate`/review-logging path already exists for `Evaluation.correct` (exact mechanism is SB-8/TrainingStore's concern, flagged, not designed here) — `TileAnswer` itself adds zero new persisted shape.
- `tiles: null` from the design doc's §2 compatibility principle is honored as `tileTargetSlots.isEmpty() && tilePhrase == null`, the default for every exercise that doesn't opt in — identical guarantee, different field shape.

## 8. Decisions and tradeoffs (summary)

| Decision | Why |
|---|---|
| No `tiles: TileExercise?` field on `CoreExercise` | `ScaffoldingLevel` is a progress fact `ExerciseGenerator` doesn't have; baking it in would force progress-awareness into core-engine generation. |
| `tileTargetSlots` + `tilePhrase` (not full `TileExercise`) on `CoreExercise`/`Exercise` | Both are progress-independent, computed once at generation; `TileExercise` is computed on demand (possibly repeatedly, as scaffolding changes between attempts) by `TileGenerator`. |
| New `ConstructionRealizer.slotFacts` (public), new `MorphologyParadigm` interface | The per-slot bundle and paradigm-enumeration capabilities `TileGenerator` needs don't exist; both are additive, reuse existing private logic, change zero existing behavior. |
| `TileAnswerEvaluator` as a top-level function in `core-engine`, not a class, not in `:shared` | No state/dependencies; keeps `:shared -> :core-engine` direction; matches task's explicit placement. |
| Single DSL field `tileTargetSlots` instead of `tileEnabled` + `tileTargetSlots` | Removes a representable-but-meaningless state (`enabled=true, slots=[]`). |
| Tiles restricted to `generateForSkill`, not `generateChain` | Chain steps' `expected` composition is more varied (owner swaps mid-chain); out of scope, no regression risk since `generateChain` is untouched. |
| Fail-soft (`null`) everywhere a precondition isn't met, never `error()`/throw | Sentence Builder is additive UX; a bug here must never break the existing Typed/Oral path for the same exercise. |

## 9. Verification plan

- **core-model** (`commonTest`, new): construction/equality/default tests for `TileExercise`/`TileSegment`/`ScaffoldingLevel`/`TileAnswer`/`TileError`, plus a "no language literals" sweep (reuse `NoLanguageLiteralsTest`'s pattern) over the new file.
- **core-engine** (`commonTest`, new): `TileGeneratorTest` (AC2.1-2.6) using the exact same `TableMorphology`/`ConstructionRealizer`/`SkillSpec` fixture shapes already in `ConstructionRealizerTest.kt`, plus one real pl-ru fixture routed through `PackEngine`/`plExerciseGenerator`+`plTileGenerator` end to end; `TileAnswerEvaluatorTest` (AC3.1-3.4).
- **shared** (`commonTest`): `Exercise`/`PlExerciseEngine.toExercise()` pass-through test (AC4.2); existing `TrainingParityTest`/`EnExerciseGeneratorTest` re-run unmodified to prove AC5.3 (byte-identical `CoreExercise` for untouched skills).
- **DSL**: `node <biome> generate --check --project dsl/biome.project` (AC5.1) after editing the contract and one recipe row; `biome fmt` the touched `.core-data` files per `dsl/biome.project`'s own `course-plru-fmt` step.
- No host/UI test needed for SB-1..5 (explicitly out of scope) — SB-6/9/10 own that.

## 10. Documentation and handoff

- This blueprint is the implementation reference for SB-1..SB-5; `SENTENCE-BUILDER-DESIGN.md` stays the product/UX reference (§7-9, §11 step list beyond SB-5) and is **not** rewritten — only this file encodes the mechanics above.
- Implementation order must follow §1's data-flow: SB-1 (types) → SB-2 (generator + 3a/3b engine capabilities) → SB-3 (evaluator, independent of SB-2, can run in parallel) → SB-5 (DSL, needs SB-1's `CoreExercise` fields and SB-2's `ExerciseGenerator` change) → SB-4 (needs SB-1..3 types and SB-5's real data to test against meaningfully).
- Open question for the developer, not resolved here: the exact `Full`/`Partial`/`None` → "how many of `tileTargetSlots` become Choice" ratio in `TileGenerator.fromAnswerPhrase` (§3c sketched `(size+1)/3` for `Full` as a placeholder) is a product tuning value, not an architecture constraint — confirm with product/SB-8 owner before hardcoding, or thread it as a parameter.
- Open question: `TileSegment.Choice.targetFeature` falling back to "first key of the slot's required bundle" when the skill has no single `SkillSpec.focus.feature` (e.g. a composite/`null`-focus skill) is a defensible default but untested against a real such skill in this blueprint — flag for SB-2's test author to pick a concrete fixture.
