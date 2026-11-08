# 03. Покрытие DSL: что в polski заменимо biome-dsl, а что нет

Дата: 2026-11-08. Оценка по `biome-dsl/AI/PRINCIPLES.md` (P1-P25), `ARCHITECTURE.md` §3 (таблица «намерение -> слой»),
`PATTERN_DICTIONARY.md` §4, `ProjectMigratioPrincipals.md`, `docs/product/scope.md`. Пути от корня polski, Kotlin - от `kotlin/`.
Всё, что не проверено, помечено `[не проверено]`. Строки - справочно (P5); решает число мест правки и гарантия.

## 0. Вывод

1. Основной выигрыш DSL в polski уже получен: данные курса (`courses-biome/**/*.core-data`, 10 707 строк) + 4 контракта
   (`dsl/*.data-contract`, 1 014 строк) -> 26.7k строк сгенерированного Kotlin с `generate --check`. Это V3 (вывод) + V4 (гарантия).
2. Среди рукописных решений (18 968 строк main-кода) **нет ни одного правила, которое чисто выигрывает у native helper по P25**.
   Лучший кандидат (action-gating в `TrainingStore`) - условный; остальное - S1/S3/S5.
3. Единственная Logic-точка (`swipe.logic`) - самый слабый пример: S1 + S4 (DSL длиннее натива, один потребитель),
   плюс дефект проводки (§2.1). Её не расширять; починить проводку или вернуть в натив.
4. Реальные находки лежат не в «перенести правило в DSL», а в: (a) генераты вне стандартного layout и вне `--check`,
   (b) 4 Python-генератора тестов поверх JSON-фикстур (P11/P7), (c) польские литералы внутри «языково-нейтрального» core-engine (P10),
   (d) 7 регэксп-копий мапперов en-ru (P7, закрыто `--check`, но хрупко).
5. 51% authored Kotlin - тесты (19 467 из 38 435 строк). Самая большая поверхность для DSL - **тестовые ожидания как данные**, не логика.
6. Нового DSL не требуется ни для одного пункта плана. Два пункта (§3.5, §3.6) потребовали бы новой функции biome-dsl;
   по P13 они не открываются без второго потребителя.

## 1. Что прочитано

- biome-dsl: PRINCIPLES, ARCHITECTURE §1-§4, PATTERN_DICTIONARY, ProjectMigratioPrincipals (частично), scope.md, extending.md (таблица GENERATE, строки 1-60 и 257), SELFHOST_BOUNDARY (§0-§1).
- polski: `dsl/biome.project`, `dsl/swipe/*`, `dsl/derive-*.mjs`, `CLAUDE.md`, `AGENTS.md`, `AI/decisions.md` (ADR про свайп и COMMAND/WIRE),
  `Plans/Biome/CommandWireExperiment.md`, `Plans/Architecture/01-MODULE-STRUCTURE.md`.
- Kotlin целиком: `srs/FsrsScheduler.kt`, `srs/FsrsMath.kt`, `training/*`, `grammar/AnswerNormalizer.kt`, `progress/ReviewReducer.kt`, `progress/SkillQueue.kt`,
  `interaction/*`, `core-engine/.../ExerciseGenerator.kt`, `Recipe.kt`, `ConstructionRealizer.kt`; `presentation/TrainingStore.kt` (наполовину), `StyleComposer.kt`.
- Не читались целиком: `EndingHighlight.kt`, `MatrixTables.kt`, `VocabularySession.kt`, `*Codec.kt`, UI-слой, Swift. Для них вердикт по заголовкам и размерам.
- Ничего не запускалось: `biome generate --check`, `npx biome check changed`, Gradle-тесты - NOT RUN.

## 2. Что уже на DSL (сегодня) и что в нём не так

| Поверхность | Файлы | Место правки при изменении | Дрейф ловит |
|---|---|---|---|
| data-contract `course` | `dsl/course.data-contract` (785) + 36 `.core-data` pl-ru/en-ru | 1 строка данных | `generate --check`, `fmt --check`, `data check` |
| data-contract `forms`/`frequency`/`style` | 3 контракта, 53 строки | то же | то же |
| `derive` forms | `dsl/derive-forms.mjs` (157) -> `FormRows.kt` 12 825 + 2 133 строк | правила в одном JS | `--check` |
| `derive` copy en-ru | `dsl/derive-enru-mappers.mjs` (25), 7 шагов | pl-ru маппер; en-ru - копия | `--check` |
| Logic DECISION | `dsl/swipe/swipe.logic` (7) + `.contract` + `.examples` (9) | см. §2.1 | только вне `dsl/biome.project` |

### 2.1. Дефекты проводки (исправляются сегодняшними средствами)

- **W1. Swipe не в `dsl/biome.project`.** `grep -n swipe dsl/biome.project` пусто. Шаг generate лежит в
  `tests/fixtures/nj03-exercise-oracle/biome.project` (`derive-swipe-policy.mjs`), а `tests/swipe-generation.test.ts` пропускается без
  checkout biome-dsl (NOT RUN). Итог: `biome generate --check --project dsl/biome.project` **не видит** дрейфа `SwipePolicy.kt`. Нарушение P7/P16 (копия без `--check` в основном проекте).
- **W2. Генерат в авторском корне.** `shared/src/commonMain/kotlin/polski/interaction/SwipePolicy.kt` с шапкой «Generated from swipe.logic»,
  но не с kit-banner и не в `commonMain/generated/` (где лежит всё остальное). `docs/product/layout.md` требует стандартные корни; canary-тесты не покрывают этот путь.
- **W3. `swipe-native.js` - заглушка** («Kotlin is the real consumer»): контракт требует `MODULE`, которого для kt-потребителя нет. Не ошибка, но показывает: `.contract` здесь нужен лишь для чтения фактов.
- **W4. `PROVENANCE "Hand-written from SwipeDecisionTest.kt expectations"`** в `swipe.examples` - ожидания взяты из Kotlin-теста, который проверяет тот же Kotlin: независимость (P15) формально есть, фактически нет второго источника.

Решение по W1/W2: либо перенести шаг `GENERATE swipe logic ... --target kt` в `dsl/biome.project` с `--out` в `commonMain/generated/kotlin/polski/interaction/`
(и с `AS`/namespace по `layout.md`), либо вернуть решение в натив (§3.2). Выбор - владельцу; рекомендация - §3.2.

## 3. Карта: рукописное правило -> поверхность -> вердикт

Условные обозначения: **DSL-сегодня** = существующие поверхности без изменений biome-dsl; **DSL-новое** = нужна работа в biome-dsl;
**native** = остаётся; S1-S6 - стоп-сигналы P25 (S1 длиннее натива, S2 лишний бюджет, S3 линейные данные, S4 один потребитель,
S5 физика, S6 словарь без потребителя); V1-V5 - выгоды (места правки / копии / вывод / гарантия / повтор).

| # | Правило | Файл (строки) | Слой по ARCH §3 | Вердикт |
|---|---|---|---|---|
| 3.1 | Данные курса, формы, стили | `courses-biome/**`, `dsl/*` | data-contract | **уже DSL**, держать |
| 3.2 | Swipe release -> rating | `interaction/SwipeDecision.kt` (30) + `SwipePolicy.kt` | Logic DECISION | S1+S4: **native проще**; DSL оправдан только с починкой W1/W2 |
| 3.3 | Классификатор причины изменения `classify()` | `ExerciseGenerator.kt:161-165` | данные пака (или DECISION) | **P10-находка**; DSL-сегодня: data rows |
| 3.4 | Допуск действий `TrainingStore.dispatch` (action x loadStatus x phase) | `TrainingStore.kt:140-460` | Logic DECISION (enum-факты) | **условный кандидат**, нужен замер |
| 3.5 | Тестовые оракулы: 4 Python-генератора + JSON | `*/generate_parity.py` (291) -> ~3 200 строк Kotlin | `.examples`/data-contract | **кандидат номер 1 по месту правки**; DSL-сегодня |
| 3.6 | Копии мапперов en-ru через regex | `derive-enru-mappers.mjs` | data printer | работает; DSL-новое только при 3-й паре |
| 3.7 | FSRS-арифметика и лестница шагов | `srs/Fsrs*.kt` (180) | PROGRAM / native | S3+S5: **native** |
| 3.8 | `ReviewReducer` (статистика, смена дня) | `progress/ReviewReducer.kt` (45) | модель (P12) | S1: **native** |
| 3.9 | Нормализация ответа и расстояние | `AnswerNormalizer.kt`, `AnswerEvaluator.kt` (75) | PROGRAM | S1+S5 (`expect fun` NFC): **native** |
| 3.10 | Интерпретатор рецептов (`Recipe.kt` + `ExerciseGenerator.kt` + `ConstructionRealizer.kt`, 527) | core-engine | CORE/MECHANISM | **native**; данные уже в `.core-data` |
| 3.11 | `StyleComposer`, `EndingHighlight`, `MatrixTables` | presentation (~470) | TEMPLATE? | **native**: алгоритм выравнивания строк (S1/S3) |
| 3.12 | Wire/Codec: `IosSnapshot`, `MacSnapshot`, `PolskiTextCodec`, `ProgressCodec` | ~1 800 | COMMAND/WIRE | **закрыто NO-GO** (`Plans/Biome/CommandWireExperiment.md`); не открывать |
| 3.13 | UI Android/Desktop/Web/Swift | - | вне DSL (scope.md: «UI layout and styles») | **вне границы** |

### 3.1. Данные курса - что DSL не покрывает

Замер: `.core-data` = 10 707 строк; 2 x 16 `RECIPE` (pl-ru 1 058 строк, en-ru 1 141). Один факт - одна строка; Kotlin-строки генерируются.
Остаток вне покрытия:
- `derive-forms.mjs` (157 строк JS) - правила склонения. Это P10 «вычисление без слоя выше». По P25 S1: DSL-версия не короче 157 строк при тех же гарантиях,
  один потребитель (build-time, JS-only). **native (JS)**, класс MEASURED-NATIVE; триггер пересмотра - второй язык курса с другой морфологией (не en-ru).
- `PlRu*Rows.kt` (рукописные мапперы, 7 файлов) - см. §3.6.

### 3.2. Swipe: измерение

| Мера | DSL-версия | Native-версия |
|---|---|---|
| Файлы | `swipe.logic` 7, `.contract` 12, `.examples` 9, `swipe-native.js` 3, `derive-swipe-policy.mjs`, `SwipePolicy.kt` ~40 сгенерированных, `SwipeDecision.kt` 30 | `SwipeDecision.kt` ~20 |
| Правил | 4 (`belowThreshold`, `verticalDominates`, `swipeRight`, `swipeLeft`) | 3 строки `when` |
| Места правки на «изменить приоритет» | 1 (`.logic`) + регенерация + `.examples` | 1 (`.kt`) + `SwipeDecisionTest` |
| Потребители правила | 1 (Kotlin `SwipeDecision.rating`); хосты Android/web/iOS/macOS зовут его через Kotlin | то же |
| Гарантия, которой нет в нативе | `.examples` проверяет полноту таблицы фактов (5 строк) | `SwipeDecisionTest` проверяет те же случаи руками |

Факты, которые на самом деле копируются, - не правило, а **параметры хоста**, и они разведены намеренно (`AI/decisions.md`, ADR S2:
Android 72 dp, ratio 1,0, `checkYOnRelease=false`; web 75 px; iOS 80 pt; macOS 70 pt; ratio 1,25). Общий порог **не вводится** по решению владельца.
Реально повторяется (проверено grep):
- ratio `1.25` - 6 вхождений в 3 хостах (`WebSwipeRating.kt:107,127,133`, `SwipeToRate.swift:80,87`, `SwipeRating.swift:36,44`): два раза в каждом хосте,
  потому что guard «горизонталь доминирует» во время жеста (`abs(dx) > abs(dy)*1.25`) продублирован рядом с вызовом `SwipeDecision.rating`, который вычисляет `yDominates` при отпускании.
- Это **одно решение (захват жеста), записанное дважды в каждом хосте** и не покрытое `swipe.logic`.

Вердикт P25: S1 (DSL длиннее) + S4 (один Kotlin-потребитель) -> **native**. Лучший ход - не DSL, а native helper в `SwipeDecision`:
`fun locksHorizontal(dx, dy, axisRatio): Boolean` + константа `AXIS_RATIO`, вызываемые из Swift/web. Это убирает 6 -> 1 место правки без нового механизма (P6).
Если владелец хочет сохранить DECISION как пилот (C0, P13: «KPI-justified experimental stays»), то только при условии W1/W2 и добавления второго `DECISION lock`
в тот же `.logic`, чтобы оба решения жили в одном файле и под одним `--check`. Без этого - вернуть в натив (P25: «PROGRAM/решение, проигравшее native, возвращается»).

### 3.3. `classify()` в core-engine - находка P10

```kotlin
// ExerciseGenerator.kt:161
genderOf(nounId) == "m-personal" && nomSingularOf(nounId).endsWith("a") -> "personalA"
```

Это польское правило («муж. личные на -a, напр. kolega -> kolegę») внутри модуля, который объявляет себя нейтральным
(`ExerciseGenerator` KDoc: «no language-specific rule is ever hardcoded here»). Рядом: ключ `"Gender"`, слоты `"noun"/"adjective"/"owner"`, `"voc"`, `"sg"`.
Для en-ru этот классификатор тоже исполняется (с `genderOf == null`, ветка просто не срабатывает).

| Вариант | Места правки при новом правиле пака | Гарантия |
|---|---|---|
| Сейчас | 1 (Kotlin core-engine) на **каждый** язык; язык меняет ядро | нет |
| DECISION (Logic, enum-факты `gender`, `endsWithA`, `unchanged`) | 1 `.logic` на пак + регенерация; печатается в kt | таблица `.examples`; полнота фактов |
| **Data rows** (`ReasonClassRow` в `recipes.core-data` / `recipeDefaults`: упорядоченный список `whenFeature`, `whenSuffix`, `class`) | 1 строка данных; ядро без правил | `data check` по контракту, порядок «первое совпадение» как в `ReasonSpec.Classified` |

Рекомендация: **data rows** (DSL-сегодня, расширение существующего `course.data-contract`). Причина: правило разное по паку и должно ехать с данными пака,
а не компилироваться в ядро; `ReasonSpec.Classified` уже есть как упорядоченный список. DECISION здесь проигрывает: компилируемый ladder на пак = ещё одна копия кода (V2 отрицательная).
Объём: добавить поле условия в `ReasonRule` (сейчас `whenClass: String`), убрать `classify()`; ~15 строк Kotlin, ~10 строк контракта. Тест: pl-ru `kolega` остаётся `personalA` (`TrainingParityTest`, закреплённые литералы).

### 3.4. Допуск действий в `TrainingStore` - единственный условный кандидат

Факты (проверено): 5 значений `LoadStatus`, 4 `CardPhase`, 15 видов `AppAction`. Допуск записан разрозненно: `ifReady {}` (4 вызова), явные
`phase == Question` (SetAnswerMode, EditAnswer, ContinueIntroduction), `phase != Revealed || exercise.id != action.exerciseId` (rate), `loadStatus == Ready && ...` (RequestReset). Всего 10 совпадений условий в 461 строках.

| Мера | Сейчас | DECISION `accepts` |
|---|---|---|
| Места правки при новой фазе | N условий по файлу | 1 таблица |
| Полнота «action x phase x loadStatus» | неявная; 543 строки `TrainingStoreTest` частично | `.examples` с явной сеткой |
| Размер | ~0 (inline) | `.logic` ~25 + `.contract` ~10 + 40 gen |

Проверка, которой нет: **не проверено**, поддерживает ли kt-принтер Logic enum-факты и многозначный результат в нужной форме (в `decision-condition.ts` enum есть; kt-вывод не смотрел).
Честная оценка: 15 действий x 4 фазы - сетка небольшая; native `when (action)` с одним `guard(phase, status)` помощником даёт почти ту же компактность.
**Не переносить сразу.** Порядок: (1) native-рефакторинг в одну функцию `accepts(action, state): Boolean` (место правки 1) - бесплатно;
(2) если после него таблица >20 правил или появляется 2-й потребитель (iOS/macOS хотят знать «кнопка доступна?» из того же правила) - DECISION (V1+V2).
Сейчас хосты не дублируют этот допуск (проверено только по вызовам `rate`, `reveal`) - S4 действует.

### 3.5. Тестовые оракулы - главная возможность по местам правки

Факты:
- Тесты = 19 467 строк (51%). `GrammarParityTest` 2 069, `SchedulerExpandedTest` 1 144, `TrainingParityTest` 636, `SchedulerOracleTest` 403, `TrainingParityEnTest` 496.
- Четыре Python-генератора (`grammar` 107, `progress` 69, `training` 84, `srs` 31 = 291 строка) печатают Kotlin-ассерты из JSON-фикстур `tests/fixtures/kotlin-parity/*.json`.
- Эти генераторы **вне** `generate --check`: дрейф теста от фикстуры ловится только повторным запуском руками. JSON-фикстуры - нарушение духа P11 (но они машинные, выведены из React-эталона).
- Шапки: «Generated by generate_parity.py from the pinned 21-case ts-fsrs 5.4.2 oracle» - ожидания независимы от Kotlin (P15 выполнен).

Что даёт DSL-сегодня (без новой функции):
1. Фикстуры перевести в `.core-data` + `*.data-contract` (механика уже в проекте: `GENERATE ... data --target kt`).
2. Типизированные строки `OracleCaseRow` печатаются в `commonTest/generated/`; тело теста (30-60 строк на область) остаётся рукописным и итерирует строки.
3. `generate --check` + `fmt --check` ловят дрейф; Python-генераторы удаляются (-291 строка, -1 toolchain).

Чего это **не** даёт: число строк Kotlin почти не падает (данные остаются данными). Выигрыш - V2 (копия фикстура/тест под `--check`) и V4 (контракт типов: пропущенное поле - ошибка до компиляции Kotlin).
Конкурент - уже работающий Python (291 строка). По P25 это S1-нейтрально, но выигрывает V2. Риски:
- P15: ожидания не должны регенерироваться из Kotlin; конвертер JSON->`.core-data` - одноразовый, фикстуры пинуются по SHA.
- Эпизод `TrainingParityTest` содержит `Draws(random)`-последовательности и порядок вызовов - **это логика теста**, она остаётся рукописной.
- Масштаб: 1 область за раз (~100-150 LOC правок на агента, memory `feedback_smaller_agent_tasks`); начать с `srs` (31 строка генератора, 21 кейс) как самого простого замера.

Метрика приёмки пилота: число мест правки при «добавить кейс оракула» (сейчас: JSON + `python generate_parity.py` + коммит двух файлов; после: 1 строка `.core-data`);
`generate --check` падает на устаревшем тесте; `SchedulerOracleTest` проходит с теми же ожидаемыми значениями (Gradle - NOT RUN до запуска).

### 3.6. Копии мапперов en-ru

Факт: 7 шагов `GENERATE enru-* derive ... copy`; каждый делает `replaceAll('PlRu','EnRu')` + замену пакета. Вывод - генерат с `--check` (P7 выполнен).
Хрупкость: имена (`PlRu`, `pl-ru`) в тексте pl-ru файла, любая новая строка с этой подстрокой молча меняется. Гарантий типов нет: ошибка видна только kotlinc.
Native-контроль: общий интерфейс строк (`interface SkillRowLike`) и один маппер на оба пакета; генератор строк должен был бы печатать `implements`.
Это **DSL-новое** (опция data-printer). P13: один продукт, одна пара пакетов - не открывать. Триггер: третья пара курса (напр. de-ru) или появление ошибки из-за regex.
Сейчас: **оставить** (закрыто `--check`).

### 3.7-3.9. Вердикты native с цифрами

- **FSRS** (`FsrsScheduler` 121, `FsrsMath` 59, `Alea` 60): порт ts-fsrs 5.4.2 по оракулу в 21 случай (`SchedulerOracleTest`) + расширенные 1 144 строк.
  Лестница шагов (`step()`: Again/Hard/Good/Easy x learningSteps -> минуты) - таблица, но выходы числовые и зависят от `steps.size`;
  DECISION выдаёт enum-вердикт, не число (S1+S3). Любое отклонение от ts-fsrs - баг по определению, нужна побитовая сверка (S5). **native**, класс MEASURED-NATIVE.
- **ReviewReducer**: два правила (`correct = typedCorrect ?: rating != Again`, сброс `reviewsToday` по `lastDay`). Места правки 1. Мутация (P12) принадлежит модели. **native**.
- **Нормализация**: порядок шагов важен (NFC -> lowercase -> trim -> strip `[.!?]+$` -> collapse); часть `expect fun` по платформам (S5). Расстояние Левенштейна - алгоритм. **native**.
  React-копия этих же правил (`src/grammar`) удаляется вместе с React (AGENTS.md); после удаления копия исчезает без DSL.

### 3.10-3.13. Остаток

- Интерпретатор рецептов: sealed-иерархия `TextValue/TextSpec` - это уже интерпретатор данных. Альтернатива «TEMPLATE разворачивает рецепты в Kotlin-функции»
  дала бы 2 x 16 функций (одна на скилл) вместо одного интерпретатора на 243 строки и усилила бы дублирование (P1: TEMPLATE «не маскирует нативный алгоритм»). Данные уже под контрактом. **native.**
- `StyleComposer` (`when(kind)` по 9 блокам) и `EndingHighlight` (206, выравнивание форм): механизм/алгоритм; стили уже данные (`style.data-contract`). **native.**
- Codecs/Snapshots: COMMAND/WIRE - стоп-правило выполнено и оформлено NO-GO. Повторно замер не делать до нового потребителя.
- UI: scope.md прямо исключает «UI layout and styles». ARCHITECTURE §6.1.

## 4. Что нового требуется от biome-dsl

Ни один шаг §5 не требует новой функции biome-dsl. Список ниже - только условные триггеры (P13: названный потребитель):

| Возможность | Потребитель сегодня | Триггер |
|---|---|---|
| Опция data-printer: общий интерфейс для двух пакетов строк (§3.6) | 1 продукт, 2 пака | 3-й пак курса |
| kt-принтер Logic с enum-фактами/многозначным выходом (§3.4) | не проверено | решение по §3.4 после native-рефакторинга |
| `FLOW` на Kotlin | нет (kt FLOW = `-`, scope.md) | не нужен: Tutorial в polski нет |

Запрещено открывать (P2/P25): R9 (py/kt reader printers), TD-84/118 (рукописный Kt-рантайм), COMMAND/WIRE, паттерн dots.

## 5. План действий (по убыванию отношения выгода/цена, P8)

| Шаг | Что | Выгода | Цена | Проверка |
|---|---|---|---|---|
| S-1 | Починить W1/W2: шаг swipe в `dsl/biome.project` + вывод в `generated/` ИЛИ вернуть в натив (§3.2) | закрывает дыру `--check` | ~15 строк проекта | `biome generate --check --project dsl/biome.project`, `npm run test -- swipe` |
| S-2 | Native helper `locksHorizontal` + `AXIS_RATIO` в `SwipeDecision`; хосты зовут его (6 мест -> 1) | убирает 5 копий правила | ~20 строк Kotlin + 3 хоста | `SwipeDecisionTest`, UI-тесты хостов; параметры по ADR S2 не менять |
| S-3 | `classify()` -> data rows в `course.data-contract` (§3.3) | ядро без польских литералов (P10) | ~25 строк | `TrainingParityTest`, `EnExerciseGeneratorTest`, `data check` |
| S-4 | Пилот оракулов `srs` -> `.core-data` + typed rows (§3.5) | `--check` на тестовые ожидания; -1 toolchain | 1 область | `generate --check`; `SchedulerOracleTest` (те же значения) |
| S-5 | Native `accepts(action, state)` в `TrainingStore` (§3.4), замер до DECISION | 10 условий -> 1 функция | ~40 строк | `TrainingStoreTest` без правок ожиданий |
| S-6 | Если S-4 дал выигрыш - `grammar`, `training`, `progress` | остальные ~3 000 строк тестов | по областям | то же |

Порядок S-1 и S-2 обязателен вместе: иначе остаётся DSL-решение без `--check` и 6 копий соседнего правила.

## 6. Что не делать

- Не переносить `FsrsMath`, нормализацию, `ReviewReducer`, `EndingHighlight` в PROGRAM/DECISION: P10 («транслитерация JS - находка»), S1/S3/S5.
- Не вводить TEMPLATE под рецепты и стили: данные уже под контрактом, интерпретатор короче развёртки.
- Не считать число DSL-файлов или FUNCTION прогрессом (P25). Мера - места правки и гарантии из §3.
- Не менять `swipe.examples` под вывод Kotlin и не менять пороги хостов (ADR S2, P15).
- Не добавлять пакеты/конструкции в biome-dsl «на будущее» (P13).

## 7. Не проверено / NOT RUN

- `biome generate --check --project dsl/biome.project`, `npx biome check changed`, Gradle, Playwright - не запускались.
- Каталог `tests/fixtures/kotlin-parity/` существует; содержимое JSON и актуальность генераторов не открывались, запуск `generate_parity.py` не выполнялся.
- Число хостов, дублирующих допуск действий (§3.4), - только по `rate`/`reveal`; Swift-хосты и Android ViewModel не читались.
- Поддержка kt-печати Logic с enum-фактами - только по наличию enum в `decision-condition.ts` biome-dsl.
- Доля тестов 51% - по имени файла (`Test` в пути); включает генерат-тесты и ручные.
- Строки сгенерированного Kotlin (26.7k) - сумма пяти файлов `generated/`, без остальных.
