# Sentence Builder: дизайн

Дата: 2026-11-13. Статус: **предложен**.

## 1. Мотивация

Sentence builder (сборка предложения из тайлов с выбором грамматической формы) — Tier 0 механика вовлечённости. На мобильных удобнее тапов, чем клавиатуры; тренирует чутьё на формы через дистракторы; при ошибке переходит в правило.

Аркадное задание: быстро, тактильно, с мгновенным feedback. Три уровня сложности (scaffolding) ложатся на M1 из Learning Plan.

## 2. Принципы

- **DSL-first.** Авторские параметры — в `.core-data`. Вычисляемое (тайлы, дистракторы) — в движке.
- **Язык-агностично.** Ядро (`core-model`, `core-engine`) не знает ни одного языка. Дистракторы генерирует `Morphology` по `FeatureBundle` — работает для любого языка с таблицей форм.
- **L1-агностично.** Задание на целевом языке. L1 появляется только в prompt/source (как сейчас) и в объяснении ошибки (StyleComposer).
- **Ортогонально стилям.** Sentence builder — это `InputMode`, не стиль подачи. Комбинируется: rule-first + tileBuilder, situation-first + tileBuilder.
- **Обратно совместимо.** `tiles: null` = обычное упражнение. Старые рецепты, прогресс, FSRS — без изменений.

## 3. Модель данных

### core-model

```kotlin
data class TileExercise(
    val segments: List<TileSegment>,
    val distractors: List<TileDistractor>,
    val scaffolding: ScaffoldingLevel
)

sealed class TileSegment {
    abstract val position: Int

    data class Locked(
        override val position: Int,
        val text: String
    ) : TileSegment()

    data class Choice(
        override val position: Int,
        val correctForm: String,
        val lemma: String,
        val targetFeature: FeatureKey,
        val alternatives: List<String>
    ) : TileSegment()
}

data class TileDistractor(
    val text: String,
    val sourceFeature: FeatureKey
)

enum class ScaffoldingLevel {
    Full,       // 60-70% locked, 0 дистракторов — знакомство
    Partial,    // 0-30% locked, 2-3 формы — практика
    None        // 0% locked, 4-5 форм + лишние слова — проба
}
```

### Расширение CoreExercise

```kotlin
data class CoreExercise(
    // ... существующие поля без изменений ...
    val tiles: TileExercise? = null  // null = обычное упражнение
)
```

### Расширение AnswerMode

```kotlin
enum class AnswerMode { Oral, Typed, TileBuilder }
```

### Результат проверки

```kotlin
data class TileAnswer(
    val selections: List<TileSelection>,
    val correct: Boolean,
    val errors: List<TileError>
)

data class TileSelection(
    val position: Int,
    val selectedForm: String
)

data class TileError(
    val position: Int,
    val selected: String,
    val expected: String,
    val feature: FeatureKey,
    val reason: String?  // заполняет AnswerEvaluator, показывает UI
)
```

## 4. Генерация тайлов

Тайлы **вычисляются** из существующего `CoreExercise`, не хранятся в DSL.

```
ExerciseGenerator.generateForSkill(recipe, seed)
  → CoreExercise(expected = "Widzę moją nową książkę", slots = {noun, adj, ...})

TileGenerator.fromExercise(exercise, morphology, scaffolding)
  → TileExercise(
      segments = [
        Locked(0, "Widzę"),
        Choice(1, "moją", "mój", Case, ["moja","mojej","moich"]),
        Choice(2, "nową", "nowy", Case, ["nowa","nowej","nowe"]),
        Choice(3, "książkę", "książka", Case, ["książka","książki","książce"])
      ],
      distractors = [TileDistractor("mojego", Case), ...],
      scaffolding = Partial
    )
```

### Алгоритм

1. Разбить `expected` на токены.
2. Для каждого токена: есть в `slots` → `Choice`, нет → `Locked`.
3. Для `Choice`: через `Morphology.formsOf(lemma, featureKey)` получить альтернативные формы. Исключить правильную. Взять N ближайших по частотности или грамматической близости.
4. По `scaffolding`:
   - `Full`: часть `Choice` превращаются в `Locked`, alternatives пустые.
   - `Partial`: все `Choice` активны, 2–3 альтернативы.
   - `None`: все `Choice` активны, 4–5 альтернатив + отдельные дистракторы.
5. Перемешать порядок alternatives внутри каждого Choice.

### Точка расширения: `TileGenerator`

Новый класс в `core-engine`, рядом с `ExerciseGenerator`. Зависит от `Morphology`. Не знает языка — получает формы через существующий табличный lookup.

```kotlin
class TileGenerator(private val morphology: Morphology) {
    fun fromExercise(
        exercise: CoreExercise,
        scaffolding: ScaffoldingLevel,
        distractorCount: Int = 3
    ): TileExercise
}
```

## 5. DSL

### Минимальное расширение recipeDefaults.core-data

```
RECIPE_DEFAULTS {
  tileEnabled: true          // можно ли генерировать тайлы для этого рецепта
  tileTargetSlots: [noun, adjective]  // какие slots становятся Choice
}
```

`tileEnabled: true` по умолчанию для рецептов с `slots`. Автор может отключить для рецептов, где тайлы не имеют смысла (например, чисто лексические).

`tileTargetSlots` определяет, какие части предложения становятся выбором. Для падежей: noun + adjective. Для глаголов: verb. Для согласования: noun + adjective + possessive.

### Уровень scaffolding — НЕ в DSL

Scaffolding определяется прогрессом пользователя, не автором контента. Логика в `TrainingStore`:

```
Первый контакт с конструкцией → Full
N успехов с Full → Partial  
M успехов с Partial → None
Ошибка на None → откат к Partial + M2 (разбор)
```

Конкретные пороги N, M — параметры пилота, не захардкожены в DSL.

## 6. Проверка ответа

Новый метод в `AnswerEvaluator`:

```kotlin
fun evaluateTileAnswer(
    exercise: TileExercise,
    selections: List<TileSelection>
): TileAnswer
```

Проверяет:
1. Все позиции заполнены.
2. Для каждой `Choice`: `selectedForm == correctForm`.
3. Порядок слов (если нефиксированный — для будущих языков).
4. При ошибке: заполняет `TileError.reason` из `ConstructionRealizer.changes` (from→to→reason уже есть в CoreExercise).

Строковый Levenshtein не используется — это точное сравнение форм.

## 7. Ошибка → правило (M2)

```
TileError на позиции 1: selected "moja", expected "moją"
  ↓
AnswerEvaluator → TileError(feature = Case, reason = "acc.f.sg")
  ↓
UI показывает: подсветка ошибочного тайла красным, правильного зелёным
  ↓
Тап на ошибку → StyleComposer.blocksForError(exercise, error)
  → [Block.Changes(from="moja", to="moją", reason="..."), Block.Rule("Винительный...")]
  ↓
TrainingStore планирует retry через 2-3 задания на ту же конструкцию (другой seed)
```

`StyleComposer` уже умеет генерировать блоки Rule/Changes/Table — нужен только метод `blocksForError`, адресующий конкретную ошибку, а не всё объяснение.

## 8. UI контракт (все хосты)

UI не принимает учебных решений. Контракт:

```kotlin
// Состояние от TrainingStore
data class TileBuilderState(
    val exercise: TileExercise,
    val placed: List<TileSelection>,     // что уже поставлено
    val result: TileAnswer?,             // null до проверки
    val errorExplanation: List<Block>?   // блоки после тапа на ошибку
)

// Действия от UI
sealed class TileAction {
    data class Place(val position: Int, val form: String) : TileAction()
    data class Remove(val position: Int) : TileAction()
    object Check : TileAction()
    data class TapError(val position: Int) : TileAction()
}
```

### Мобильный UX

- Tap-to-place (не drag). Тайлы минимум 48dp, gap 8px.
- Зона ответа сверху, тайлы внизу.
- Choice-тайл при тапе раскрывает формы (bottom sheet или inline picker).
- Мгновенный цветовой feedback: зелёный/красный.
- Тап на красный тайл → мини-правило.

### Анимация

- Тайл плавно перемещается в позицию (200ms ease-out).
- Rive-декорация не перехватывает ввод.
- `animationsEnabled: false` → мгновенное перемещение.

## 9. Универсальность

### Новый язык

Sentence builder работает для любого языка, у которого:
1. Есть `Morphology` (таблица форм) — уже есть для pl и en.
2. Есть `ConstructionRealizer` (генерация фразы) — есть.
3. Рецепты содержат `slots` — есть.

Для языков без морфологии (китайский, вьетнамский) sentence builder работает как reorder-задание (только порядок слов, без выбора формы).

### Новый L1

L1 влияет только на:
- `source` / prompt (задание на родном языке) — уже есть.
- `StyleComposer` → Block.NativeParallel (L1-сравнение) — уже есть.
- `TileError.reason` — локализуется через существующие объяснения.

Никаких L1-специфичных правил в движке тайлов.

## 10. Интеграция с Learning Plan

| Механика | Как sentence builder помогает |
|----------|------------------------------|
| M1 (scaffolding) | Три уровня: Full → Partial → None |
| M2 (ошибка → правило) | TileError + StyleComposer.blocksForError + retry |
| M3 (ежедневная сессия) | Sentence builder как первое задание «Сегодня» — быстрый старт |
| M6 (прогресс) | "Раньше Full, сегодня None" — видимый рост самостоятельности |
| M7 (любопытство) | Вопрос-затравка → sentence builder → объяснение |
| M9 (мини-миссии) | Цепочка sentence builder'ов с сюжетной связкой |

## 11. Порядок реализации

| Шаг | Что | Зависимости |
|-----|-----|-------------|
| SB-1 | `TileExercise`, `TileSegment`, `ScaffoldingLevel` в core-model | нет |
| SB-2 | `TileGenerator` в core-engine (fromExercise + Morphology) | SB-1 |
| SB-3 | `TileAnswer`, `TileError`, evaluator в core-engine | SB-1 |
| SB-4 | `AnswerMode.TileBuilder` + `TileBuilderState`/`TileAction` в shared | SB-1..3 |
| SB-5 | DSL: `tileEnabled`/`tileTargetSlots` в recipeDefaults | SB-2 |
| SB-6 | Web UI: tap-to-place + form picker + error highlight | SB-4 |
| SB-7 | Ошибка → blocksForError → мини-правило | SB-3 |
| SB-8 | Scaffolding логика в TrainingStore (Full→Partial→None) | SB-4 |
| SB-9 | Android UI | SB-4 |
| SB-10 | iOS UI | SB-4 |

SB-1..5 — общее ядро, SB-6..10 — хосты параллельно.

## 12. Acceptance Criteria

- **SB-AC1:** тайлы генерируются из любого рецепта с slots без ручной разметки.
- **SB-AC2:** дистракторы — реальные формы того же слова из Morphology, не выдуманные.
- **SB-AC3:** три уровня scaffolding работают и переключаются по прогрессу.
- **SB-AC4:** ошибка в форме показывает конкретную причину и переводит в правило.
- **SB-AC5:** retry после ошибки — другой seed, та же конструкция.
- **SB-AC6:** `tiles: null` не ломает существующие упражнения.
- **SB-AC7:** en-ru генерирует тайлы через тот же TileGenerator без правок.
- **SB-AC8:** FSRS и прогресс совместимы; TileBuilder пишет в тот же журнал.
- **SB-AC9:** мобильные тайлы ≥48dp, tap-to-place, мгновенный feedback.
- **SB-AC10:** при `animationsEnabled: false` — без анимаций.

## 13. Что НЕ входит

- Голосовой ввод (`AnswerInput.Spoken` — зарезервирован, не реализуется).
- Генерация дистракторов через LLM (только Morphology).
- Свободный ввод внутри тайла (тайл = выбор из форм, не печать).
- Мини-миссии (M9) — отдельный дизайн поверх sentence builder.
