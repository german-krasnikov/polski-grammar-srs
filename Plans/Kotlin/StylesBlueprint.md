# Стили подачи как данные (UC-10)

Дата: 2026-09-27. Реализует ADR-11/ADR-12 и строку UC-10 [UniversalCorePlan.md](UniversalCorePlan.md#12-задачи-миграции-малые-с-приёмкой-uc-xx) **сейчас**, в текущем `kotlin/shared` (без `:core-*`, без schema v2/`courses/core`) — переезд в `:core-presentation` и `courses/core/styles/` откладывается до UC-12. Заменяет `ExplanationMethod {Logic, Situations}` (`presentation/AppUiState.kt:12`), `PreferredMethod` (`preferences/UserPreferences.kt:3`) и жёсткий `methods.{logic,situations}` (`courses/pl-ru/course.json`, `data/CourseData.kt:15-27`) на четыре стиля-рецепта, читаемых как данные.

Факт-база: `SkillPresentation.logic/situations: MethodPresentation{introduction, promptLead, introduce, retrieve, feedback, review}` уже реализована ([MethodologyCycleBlueprint.md](MethodologyCycleBlueprint.md)); `EndingHighlight.kt` (`EndingPart`, `ContrastPair`, `changeHighlightParts`) уже даёт подсвеченные диапазоны и переезжает без изменений ([ContrastHighlightPlan.md](ContrastHighlightPlan.md)); `reference.russianSupport` — отдельный срез для 4 строк экрана карты ([CourseRussianSupportBlueprint.md](CourseRussianSupportBlueprint.md)), не источник данных стиля. `TrainingWebApp.kt:713-910` показывает сегодняшнюю раскладку front/back, из которой берутся имена и источники блоков ниже.

---

## 1. Модель блоков

`sealed interface Block` — типизированный, презентационный, без HTML/Compose-типов, живёт в `kotlin/shared/src/commonMain/kotlin/polski/presentation/Block.kt` (NEW):

```kotlin
sealed interface Block {
    data class Formula(val text: String) : Block                              // "Mam X → Nie mam + GEN"
    data class Rule(val text: String) : Block                                  // theory-прозаическое правило
    data class Table(val caption: String, val rows: List<TableRow>) : Block    // схема окончаний
    data class Scene(val text: String) : Block                                 // короткая сцена
    data class NativeParallel(val pairs: List<NativeParallelPair>) : Block     // L1 ↔ target
    data class Examples(val items: List<String>) : Block                       // доп. примеры без разбора
    data class WhyOnDemand(val text: String, val collapsedLabel: String = "Почему так?") : Block
    data class Changes(val items: List<ChangeItem>) : Block                    // exercise.changes, диф
    data class Contrast(val before: List<EndingPart>, val after: List<EndingPart>) : Block // focus было→стало
}
data class TableRow(val label: String, val before: List<EndingPart>, val after: List<EndingPart>)
data class ChangeItem(val before: List<EndingPart>, val after: List<EndingPart>, val reason: String)
data class NativeParallelPair(val native: String, val target: String, val note: String, val matches: Boolean)
```

`TableRow`/`ChangeItem`/`Contrast` используют существующий `EndingPart`/`changeHighlightParts` — не новый механизм подсветки. `Formula`/`Changes`/`Contrast` — уже существующие в UI фрагменты (`rule.appendChild(...formula...)`, `change-list`, `rule-contrast`), здесь только типизированы как блоки, чтобы порядок стал данными, а не веткой `if (state.explanationMethod == Logic)`.

### Фазы

Ровно две — `enum class StylePhase { Front, Back }`, совпадают с `CardPhase.Question`/`Revealed`. Внутри `Front` сегодняшнее различие «первая встреча (`introPending`) vs повтор» и текст `method.retrieve/promptLead` остаются как есть — это **не блок**, а одна короткая строка-лид, которую хост подставляет рядом с первым Front-блоком (источник — `MethodPresentation`, см. §4); она не варьируется по 4 стилям сильнее, чем сегодня по 2, и не входит в контракт данных этой задачи. `review`-строка перед кнопками оценки — аналогично, всегда рендерится, не блок.

| Block | Front | Back |
|---|---|---|
| Formula | rule-first | rule-first (повтор) |
| Table | rule-first | — |
| Rule | — | rule-first |
| Scene | situation-first | — |
| NativeParallel | native-contrast | native-contrast (повтор) |
| Examples | minimal-theory | — |
| WhyOnDemand | — | minimal-theory |
| Changes | — | все 4 (инвариант упражнения, не стиля) |
| Contrast | — | rule-first, native-contrast |

---

## 2. Стили как данные — `styles/*.json`

Расположение: `courses/styles/*.json` (NEW каталог). Не `courses/core/styles/` — этот путь принадлежит schema v2 (§3.1 UniversalCorePlan), которой пока нет; переезд туда — механическое перемещение файлов на UC-12, не архитектурная правка. Формат языко- и парно-независим: никакого `pl`/`ru` внутри.

Файлы: `rule-first.json`, `situation-first.json`, `native-contrast.json`, `minimal-theory.json`. Схема одного рецепта:

```json
{
  "id": "native-contrast",
  "label": { "ru": "Через сравнение с родным", "en": "Native contrast" },
  "description": { "ru": "По-родному так → по-изучаемому так, где сходится и где отличается.",
                    "en": "L1 parallel: where it matches and where it differs." },
  "blocks": { "front": ["nativeParallel"], "back": ["changes", "nativeParallel", "contrast"] },
  "requires": ["nativeParallel"],
  "fallback": "rule-first"
}
```

`requires` — список `BlockKind`, для которых стиль обязан иметь непустой авторский контент на конкретном навыке; `fallback` — `StyleId` рецепта, чьи `blocks` подставляются **для этого навыка** целиком (front и back), если `requires` не выполнен. Инвариант валидатора (§5): у рецепта, названного как `fallback`, свой `requires` должен быть пуст — не более одного уровня отката, никогда не крашится, никогда не зацикливается. `rule-first`, `situation-first`, `minimal-theory` не декларируют `requires` — их блоки (`Formula`/`Table`/`Rule`/`Scene`/`Examples`/`WhyOnDemand`/`Changes`/`Contrast`) всегда выводимы (§3), поэтому они годятся как чужой fallback.

---

## 3. Контент по навыку — `styleContent` (schema v1, аддитивно)

`skills[].styleContent` — НОВЫЙ необязательный объект в `courses/pl-ru/course.json`, ничего существующего не переименовывает. Каждое поле — необязательно; отсутствие означает production-выведенное значение (derive), не ошибку.

| Поле | Выводится (derive) из | Когда нужен авторский текст |
|---|---|---|
| `rule` | `skill.theory` | Никогда для MVP; переопределение — редакторская правка тона |
| `table` | `EndingHighlight.ContrastPair.generated(focus.before, focus.after)` как одна строка `"Было → Стало"` | Настоящая многострочная таблица склонения — позже, вместе с `table-specs.json` (UC-09), не в этой задаче |
| `scene` | `methods.situations.introduce` (уже авторский текст, тот же цикл) | Никогда для MVP |
| `nativeParallel` | — (нет универсального источника; `reference.russianSupport` покрывает только 4 из 16 навыков и другой формат экрана) | **Да** — обязателен для содержательного `native-contrast`; без него — задекларированный fallback на `rule-first` (§2), не крах |
| `examples` | `[]` (сама карточка = единственный пример) | Опционально — 1-2 доп. примера обогащают `minimal-theory`, не обязательны |
| `why` | `skill.theory` (тот же текст, что и `rule`, просто свёрнут) | Опционально — короче/проще сформулированная версия для «Почему так?» |

Только `nativeParallel` реально требует новой редакторской работы по всем 16 навыкам; `rule-first`/`situation-first`/`minimal-theory` работают на существующих данных с первого дня — это и есть «толерантная миграция без тяжёлой работы».

### Точный пример — `case.gen.neg`

Добавляется к существующему объекту навыка (все текущие поля `id/title/.../methods` не меняются):

```json
{
  "id": "case.gen.neg",
  "...": "…существующие focus/methods без изменений…",
  "styleContent": {
    "nativeParallel": [
      {
        "native": "Не вижу жену.",
        "target": "Nie widzę żony.",
        "note": "В русском при отрицании падеж не меняется (живы оба варианта — «жену»/«жены»); в польском после nie обязателен Dopełniacz.",
        "matches": false
      },
      {
        "native": "Не вижу книгу.",
        "target": "Nie widzę książki.",
        "note": "Тот же сдвиг у неодушевлённого объекта — родительный обязателен без вариантов.",
        "matches": false
      }
    ],
    "examples": ["Nie mam czasu.", "Nie widzę żadnego problemu."]
  }
}
```

`rule`/`table`/`scene`/`why` не указаны — берутся выводимые значения. Схема (`courses/schema/course-pack-v1.schema.json`, объект `skill`, сейчас `additionalProperties: false`) получает новое необязательное свойство `styleContent` с `additionalProperties: false` внутри и всеми полями необязательными; `nativeParallel[]`/`examples[]` — списки объектов, `matches: boolean` обязателен внутри пары, если пара присутствует.

---

## 4. Kotlin API (`kotlin/shared`, будущий переезд в `:core-presentation`)

```kotlin
// presentation/StyleRecipe.kt (NEW)
enum class StyleId { RuleFirst, SituationFirst, NativeContrast, MinimalTheory }
enum class StylePhase { Front, Back }
enum class BlockKind { Formula, Table, Rule, Scene, NativeParallel, Examples, WhyOnDemand, Changes, Contrast }

data class StyleRecipe(
    val id: StyleId,
    val label: Map<String, String>,
    val description: Map<String, String>,
    val blocks: Map<StylePhase, List<BlockKind>>,
    val requires: Set<BlockKind> = emptySet(),
    val fallback: StyleId? = null,
)

// data/CourseData.kt (extend, NOT rename existing SkillPresentation/MethodPresentation)
data class SkillStyleContent(
    val rule: String? = null,
    val table: List<TableRow>? = null,
    val scene: String? = null,
    val nativeParallel: List<NativeParallelPair> = emptyList(),
    val examples: List<String> = emptyList(),
    val why: String? = null,
)
// PolishCourseData.styleContent: Map<String, SkillStyleContent> by lazy — по умолчанию SkillStyleContent() для навыка без поля.

// presentation/StyleComposer.kt (NEW)
object StyleComposer {
    /** Style whose `requires` isn't met by [content] resolves to its declared `fallback`; a style
     *  without `requires` (or already satisfied) resolves to itself. Never throws. */
    fun resolveEffectiveStyle(style: StyleRecipe, content: SkillStyleContent, registry: Map<StyleId, StyleRecipe>): StyleId

    /** Pure: same (style, phase, exercise, skill, content) -> same blocks. Does not read FSRS,
     *  progress or preferences; does not create a review; [style] should already be the resolved
     *  (post-fallback) recipe. */
    fun compose(style: StyleRecipe, phase: StylePhase, exercise: Exercise, skill: Skill, focus: SkillPresentation, content: SkillStyleContent): List<Block>
}
```

### Точки замены

- `presentation/AppUiState.kt:12,41` — удалить `enum class ExplanationMethod`, поле `explanationMethod: ExplanationMethod` → `styleId: StyleId = StyleId.RuleFirst`.
- `presentation/AppAction.kt:14` — `SetExplanationMethod(method)` → `SetStyle(styleId: StyleId)`.
- `presentation/TrainingStore.kt:50,65,167` — переименовать параметр/ветку 1:1, поведение не меняется (смена стиля так же не создаёт review, не трогает draft/frozenAnswer — уже гарантировано текущей веткой `mutate { it.copy(...) }`).
- `preferences/UserPreferences.kt:3` — `enum class PreferredMethod { Logic, Situations }` → `enum class PreferredStyle { RuleFirst, SituationFirst, NativeContrast, MinimalTheory }`; `UserPreferencesV2.explanationMethod` → **v3** `UserPreferencesV3.styleId: PreferredStyle`, `schemaVersion = 3`.
- `preferences/UserPreferencesCodec.kt` — толерантный decode по образцу текущего `animationsEnabled`/v1→v2: `version in 1..2` читает старое поле `explanationMethod` (`"Logic"/"Situations"`) и маппит `Logic→RuleFirst`, `Situations→SituationFirst`; `version == 3` читает новое поле `styleId` напрямую по `enumValues<PreferredStyle>()`. `fieldsV3 = fieldsV2 - "explanationMethod" + "styleId"`. Прогресс/FSRS не участвуют — только эта одна JSON-функция и её тест меняются.
- `data/CourseData.kt:23-28` — `SkillPresentation` не переименовывается (`logic`/`situations` остаются буквальными полями `MethodPresentation`, это источник данных `Scene`/`Rule`/`Formula`, а не сам стиль); добавляется `styleContent: Map<String, SkillStyleContent>` рядом.
- Реестр рецептов: `StyleRegistry` (NEW, тонкий) грузит 4 встроенных JSON (тот же build-time приём инлайна, что `generateCoursePackSource`, отдельная маленькая генерируемая константа `generatedStylesJson`) и парсит в `Map<StyleId, StyleRecipe>` — тоже данные, не Kotlin-код рецептов.

### iOS/macOS snapshot

`kotlin/shared/src/iosMain/kotlin/polski/ios/IosSnapshot.kt` не может экспортировать `sealed interface Block` с ассоциированными данными удобно через Objective-C header — по образцу `matrixSnapshot(state): JsonElement` (строка 174) добавляется `styleSnapshot(state): JsonElement`, сериализующий `List<Block>` в тот же плоский `JsonObject`-по-типу (`{"kind": "table", "rows": [...]}` и т.п.), который `PolskiGrammarApp.swift` уже умеет разбирать как JSON (текущий паттерн `matrix`/`supportLines`). Swift-код читает `kind` и рисует нужный view — без нового native Kotlin-типа на границе.

### Web/Android потребление

`TrainingWebApp.kt:713-910` и `AndroidTrainingScreen.kt:147-266` заменяют `if (state.explanationMethod == Logic) ... else ...` на `StyleComposer.compose(...).forEach { block -> when (block) { is Block.Formula -> …; is Block.Table -> …; … } }` — тело каждой ветки `when` — перенос существующего кода рендера (`rule.appendChild(...formula...)`, `change-list`, `rule-contrast`) без изменения текста/DOM для `rule-first`/`situation-first` (это и есть проверка «оба стиля рендерят тот же контент, что сегодня» из UC-10).

---

## 5. Разделение CORE / CONTENT

Строгая файловая граница, две независимые ветки одного PR-набора.

**CORE** (владеет): `kotlin/shared/src/commonMain/kotlin/polski/presentation/{Block,StyleRecipe,StyleComposer}.kt` (NEW), `AppUiState.kt`, `AppAction.kt`, `TrainingStore.kt`, `preferences/UserPreferences.kt`, `preferences/UserPreferencesCodec.kt`, `data/CourseData.kt` (только добавление типа `SkillStyleContent` и чтения необязательного поля — не трогает JSON), `kotlin/composeApp/src/{commonMain,webMain}/**` (рендер блоков в web). Механический **compile-preserving** rename `ExplanationMethod`→`StyleId`/`explanationMethod`→`styleId` разрешён в любом Kotlin-файле, который иначе не собирается (`androidMain/AndroidTrainingScreen.kt`, `desktopMain/*`, Swift-мосты `iosMain/IosSnapshot.kt`/`macosMain`) — но **без** новой визуальной раскладки там: `native-contrast`/`minimal-theory` на этих хостах временно рисуются как `rule-first`/`situation-first` до задач §6. CORE не редактирует `courses/pl-ru/course.json`, `courses/schema/*`, `scripts/*`; тест `StyleComposer`/`Block` строит `Exercise`/`Skill`/`SkillPresentation`/`SkillStyleContent` как литералы Kotlin в `commonTest` (NEW `StyleComposerTest.kt`) — без JSON-фикстуры и без правки `generateCoursePackSource`. Поскольку `styleContent` у CORE всегда может быть пустым (default `SkillStyleContent()`), CORE зелёный и до, и после CONTENT.

**CONTENT** (владеет): `courses/schema/course-pack-v1.schema.json` (добавить `styleContent` в объект `skill`), `courses/pl-ru/course.json` (заполнить `styleContent.nativeParallel`/`examples` для 16 навыков), `courses/styles/*.json` (NEW, 4 рецепта), `scripts/validate-course.mjs` (проверка `styleContent`, `styles/*.json`: уникальные `id`, `fallback`-рецепт без своего `requires`, `blocks` ссылаются только на известные `BlockKind`-строки), `src/data/course.ts` (тип `SkillPresentation`/`Skill` — добавить необязательное поле `styleContent?`, чтобы React-эталон не падал на новом JSON; React не обязан рендерить стили — ADR-5 снял parity-gate). CONTENT не редактирует `kotlin/`.

**Точный JSON-контракт между сторонами** — единственное, что должно совпасть побайтово: имя поля `styleContent`, имена вложенных полей `rule/table/scene/nativeParallel/examples/why`, форма `nativeParallel[]` (`native/target/note/matches`), форма `table` (`caption?`, `rows[].{label,before,after}` — если и когда авторится вручную), и имена 4 файлов/`id` в `courses/styles/*.json` (`rule-first`/`situation-first`/`native-contrast`/`minimal-theory`) с полями `label/description/blocks/requires/fallback`, где ключи `blocks` — точно `"front"`/`"back"`, а значения — массивы строк из фиксированного набора `formula|table|rule|scene|nativeParallel|examples|whyOnDemand|changes|contrast` (camelCase, совпадает с `BlockKind.name.lowercase-first`). Обе стороны кодируют/декодируют этот набор строк независимо — CORE как `enum class BlockKind`, CONTENT как enum в JSON Schema; несовпадение — ловится валидатором CONTENT и compile-time `enumValues<BlockKind>()` CORE, не рантаймом.

---

## 6. UI хостов — задачи на потом (не в этой задаче)

- **Настройки, все хосты**: пикер «Стиль объяснений» — 4 варианта вместо 2, `label.ru`/`description.ru` из рецепта (сегодняшний паттерн `settingsSelect(...)`/`AndroidChoiceMenu(...)` с двумя записями — механически расширяется до четырёх, источник списка — `StyleRegistry`, не литерал в UI-коде хоста).
- **Карточка, все хосты**: рендер `List<Block>` по `BlockKind` вместо `if (Logic) ... else ...`; web/Android получают это в CORE (§5), Desktop/iOS/macOS — отдельные PR по образцу web-реализации.
- **Быстрое переключение стиля на карточке**: кнопка/меню рядом с карточкой, диспатчит `SetStyle` без ре-генерации упражнения — то же самое поведение, которым уже обладает переключение Logic/Situations сегодня (draft/frozenAnswer/exercise ID не трогаются, §4 `TrainingStore`).

---

## 7. Критерии приёмки

| ID | Наблюдаемое условие |
|---|---|
| ST-01 | Для каждого из 16 навыков и каждого из 4 стилей `StyleComposer.compose` возвращает непустой `List<Block>` для `Front` и `Back`; ни один навык/стиль не бросает исключение при пустом `styleContent`. |
| ST-02 | `rule-first`/`situation-first` при пустом `styleContent` рендерят тот же текст, что сегодняшние `Logic`/`Situations` (Formula/Table производятся из `formula`/`focus`, Scene — из `methods.situations.introduce`, Rule — из `theory`) — побайтовое сравнение на фикстуре `case.gen.neg` и ещё 2 навыках. |
| ST-03 | `native-contrast` без `styleContent.nativeParallel` на конкретном навыке резолвится в `rule-first` через `resolveEffectiveStyle` (не крашится, не показывает пустой блок); с `nativeParallel` — показывает `NativeParallel` на Front. |
| ST-04 | Смена `styleId` на текущей карточке (до и после reveal) не создаёт review, не меняет `exercise.id`/`draft`/`frozenAnswer`/`phase`; тот же инвариант, что сегодня для `SetExplanationMethod` (`TrainingStoreTest`, продолжение существующих кейсов). |
| ST-05 | Один и тот же `exercise`/FSRS-фикстура/skill ID при всех 4 стилях; `Changes`-блок идентичен между стилями (данные упражнения, не стиля). |
| ST-06 | `styles/*.json` и `Block`/`StyleRecipe`/`BlockKind`/`StyleComposer` не содержат строк `pl`/`ru`/польских слов — grep-проверка в `validate-course.mjs`/CORE-тесте; весь язык-специфичный текст — в `courses/pl-ru/course.json`. |
| ST-07 | `UserPreferencesCodec`: `schemaVersion 1/2` c `explanationMethod: "Logic"`/`"Situations"` декодируется в `styleId: RuleFirst`/`SituationFirst`; `schemaVersion 3` читает `styleId` напрямую; неизвестное значение/поле → `RecoveryRequired`, не крах, не тихий дефолт. |
| ST-08 | Валидатор `scripts/validate-course.mjs` отвергает: рецепт с `fallback`, у которого целевой рецепт сам имеет `requires`; рецепт с `blocks`-значением вне `BlockKind`; `styleContent.nativeParallel[].matches` не boolean; дубли `id` в `courses/styles/*`. |
| ST-09 | CORE-ветка (kotlin/shared+composeApp common/web) собирается и её тесты зелёные без единой правки в `courses/`, `courses/schema/`, `scripts/`; CONTENT-ветка проходит `npm run course:validate` без единой правки в `kotlin/`. |
| ST-10 | Android/Desktop/iOS/macOS хосты после механического rename (§5) компилируются и показывают прежнее поведение 2 стилей неизменным (скриншот/сборка не отличаются от текущего Logic/Situations) — новые 2 стиля на этих хостах видимы как алиас `rule-first`/`situation-first` до задач §6, не крашат экран. |
| ST-11 | `case.gen.neg` с примером из §3 проходит schema-валидацию и даёт ожидаемый `NativeParallel`/`Examples` блок на `native-contrast`/`minimal-theory` без побочных изменений `methods.logic/situations` того же навыка. |

---

## 8. Открытые вопросы (не блокируют старт)

1. Реальная многострочная `Table` (не производный 1-строчный «Было→Стало») ждёт `table-specs.json`/`MatrixTableEngine` (UC-09) — сейчас достаточно производной строки, схема `TableRow` уже её переживёт без изменения формы.
2. `reference.russianSupport` как черновая подсказка для авторинга `nativeParallel` (4 из 16 навыков совпадают по смыслу) — ручная сверка редактора, не автоматический перенос: разный экран, разная гранулярность (§1 CourseRussianSupportBlueprint.md).
3. Переезд `styles/*.json` из `courses/styles/` в `courses/core/styles/` — механический шаг UC-12, откладывается до schema v2; в этой задаче путь фиксируется намеренно на верхнем уровне, чтобы не подразумевать несуществующий `courses/core/`.
