# 01. Структура модулей: анализ и план

Дата: 2026-11-08. Оценка по `AI/PRINCIPLES.md` (P1-P25) из biome-dsl. Пути от `kotlin/`.
Метод: прочитаны `settings.gradle.kts`, build-файлы 5 модулей, `AI/architecture.md`, `TrainingStore.kt`,
`CourseData.kt`, заголовки `TrainingWebApp.kt`, `Main.kt`, `MainActivity.kt`. Файлы целиком не читались,
кроме указанных кусков. Всё, что не проверено, помечено `[не проверено]`.

## 1. Текущее состояние

### Модули и зависимости (факт из build-файлов)

```text
core-model  <-- core-engine  <-- shared  <-- composeApp <-- androidApp
                  (api)         (api x2)     (impl)         (impl composeApp + shared)
nj05ReadOnlyProbe --> shared (probe, 4 ссылки на build-директорию shared)
browserAcceptance (зависимости на project(...) не найдены)
build-logic: convention plugin polski.kmp-common (только для core-*)
```

- Направление зависимостей однонаправленное, циклов нет. Это хорошо.
- `core-*` не импортируют язык/пару/хост (заявлено в комментариях build-файлов; импорты в core-engine
  проверены выборочно: только `polski.core.model`).
- `shared` (76 файлов commonMain) - монолит: данные курса, FSRS, progress, vocabulary, preferences,
  presentation (`TrainingStore`), textwire, плюс `iosMain`/`macosMain` Swift-мосты (`IosSnapshot.kt` 559,
  `MacSnapshot.kt` 392).
- `composeApp`: commonMain только 3 файла (`ContrastText`, `ReferenceChainComparison`, `VocabularyScreen`).
  Остальное: `webMain` 20 (DOM/CSS, не Compose UI), `androidMain` 15, `desktopMain` 13.
- `androidApp` содержит свой слой: `AndroidSessionViewModel` (337), `AndroidProgressRepository`,
  `AndroidUserPreferencesStore`, `AndroidVocabularyRepository`, `MainActivity` (447).
- `shared/build.gradle.kts` содержит ~200+ строк рукописных Gradle-задач (SHA-256 seal, генерация
  тест-исходников, `literalChunks`) прямо в build-файле.

### Реальное владение сессией

`TrainingStore` (461) - единственный владелец сессии, это правильно (AGENTS: "Хост не принимает учебных
решений"). Создаётся в 5 местах: `TrainingWebApp.kt:61`, `desktop/Main.kt:166`, `IosSession.kt:184`,
`MacSession.kt:206`, Android через ViewModel.

### UI по платформам

| Платформа | Экранов | Технология |
|---|---|---|
| Android | 15 файлов `Android*` | Compose M3 |
| Desktop | 5 файлов (`MatrixScreen`, `TrainingScreen`, ...) + `Main.kt` 457 | Compose Desktop |
| Web (js+wasm) | 16 файлов `*Web` | DOM-билдеры (`node()`, `button()`) |
| iOS/macOS | SwiftUI (вне Gradle) | Swift поверх `IosSession`/`MacSession` |
| Common | 1 экран (`VocabularyScreen`) | Compose |

## 2. Проблемы

### Нумерация: тяжесть H / M / L

**1. [H] Глобальное изменяемое состояние пака вместо внедрённой зависимости. Нарушает P12, P9.**
`data/CourseData.kt:34` `internal val packRegistry by lazy`, далее ~40 top-level `val ... get() =
packRegistry.active.xxx` (строки 70-178). `TrainingStore.kt:14-18,55,59` импортирует `skills`, `nouns`,
`verbs`, `packRegistry`, `activePackSkillIds` напрямую, хотя принимает `repository/scheduler/exerciseEngine`
через конструктор. Итог: store тестируется только при одном глобальном активном паке; смена пака =
мутация глобала (`selectCoursePack`, 7 импортов в UI). Комментарий `EN-22: every val below was by lazy
(frozen)` показывает, что уже один раз сломались на этом. Хосты тоже читают глобалы: `polski.data.*`
импортируют 7 файлов webMain, 6 androidMain, 7 desktopMain (топ: `activeCoursePackId` 9,
`skills` 8, `nounById` 7). Логика (решения) и модель (мутация) смешаны через глобал - P12:
"Logic -> решения; модель -> мутации".

**2. [H] Три независимых UI-реализации одной и той же презентации. Нарушает P7, P1.**
Матрица, тренинг, прогресс, словарь, настройки, lifehack реализованы 3 раза: `webMain/*Web.kt` (DOM),
`androidMain/Android*Screen.kt`, `desktopMain/*Screen.kt`. Размеры: `MatrixWeb` 587 vs
`AndroidMatrixScreen` 437 vs desktop `MatrixScreen` 352. Общий слой есть (`MatrixTableViewModel`,
`MatrixTableSnapshot`, `Block`, `StyleComposer`), но разметка блоков, фокус, a11y и жесты написаны
повторно. Это повтор, который по P1 должен быть назван паттерном ("snapshot -> view tree"), а не
копироваться. Web не на Compose, поэтому Compose-common не решает проблему целиком.

**3. [H] `shared` - монолит с тремя причинами изменения. Нарушает P9, P22.**
В одном модуле: (a) данные курса + мапперы (`polski/data`, 28 файлов, 27k generated строк), (b) чистое ядро
(srs, progress, textwire), (c) presentation + платформенные мосты (`iosMain`, `macosMain`). Любая правка
курса пересобирает FSRS и iOS-мост; `nj05ReadOnlyProbe` тянет артефакты `shared` через пути в `build/`
(`nj05ReadOnlyProbe/build.gradle.kts:87-93`) - хрупкая связь, не граф зависимостей.

**4. [M] Дублирование имени/смысла `SkillQueue`. Нарушает P7, P22.**
`progress/SkillQueue.kt` (класс с `activePackFilter`, due/next по `StoredCard`) и `training/SkillQueue.kt`
(функция `nextSkillId` + `DueSkillCard`, тот же выбор "earliest due, tie by index"). Два способа выбрать
следующую карточку; `TrainingStore` импортирует оба (`progress.SkillQueue`, `training.nextSkillId`).
Нужно проверить, что правило выбора одно `[не проверено: сравнение тай-брейков]`.

**5. [M] Хостовая склейка дублируется: preferences, repository, session-bootstrap. Нарушает P7.**
`WebPreferencesController` (98) и `AndroidUserPreferencesStore` (64) реализуют одно: load/save/encode
`UserPreferences` поверх `UserPreferencesCodec`. `AndroidProgressRepository`/`AndroidVocabularyRepository`,
`IosSession`/`MacSession` (`newStore()` у обоих) повторяют сборку `TrainingStore`. Все 5 мест создания
store передают `(repository, scheduler, engine, time, scope, style, answerMode)` вручную; откат пака
`buildTrainingStoreOrRollback` существует только в desktop (`Main.kt:159-179`) `[не проверено: есть ли
аналог в web/android]`.

**6. [M] Крупные composable-/DOM-файлы смешивают роли. Нарушает P22 (<200 строк), P12.**
`TrainingWebApp.kt` 1054: `@Composable TrainingWebApp()` (стр.47) + рендерер `TrainingDomRenderer` +
`executeEffect` (242) + `percentEncode` (291, утилита) + `intervalLabel` (1005) + `node/button`
хелперы (1035+). Это навигация, эффекты, рендер, утилиты в одном файле. `Main.kt` 457: окно,
файловые диалоги (import/export x4, стр.252-335), навигация, `desktopDate` (456). `MainActivity` 447:
Activity + `AndroidScreen` + `AndroidSettingsScreen` + 2 хелпера темы.

**7. [M] Рукописная логика сборки в `shared/build.gradle.kts`. Нарушает P22, P16, P10.**
SHA-seal, regex по ledger, генерация Kotlin-исходников строкой (`"package polski.textwire\n\n" + ...`) -
это генератор, написанный как Gradle-код. Рядом уже есть biome-dsl как генератор. Это "рецепт вывода" по
PATTERN_DICTIONARY §4, а не build-логика. Тесты (92 файла commonTest, `GrammarParityTest` 2069 строк) тоже
зависят от этого.

**8. [L] Convention plugin только для `core-*`.** `shared` и `composeApp` повторяют 7-таргетную конфигурацию
(`jvmToolchain(21)`, `android { compileSdk = 37; minSdk = 24 }`, js/wasm) вручную; `compileSdk=37` и
`minSdk=24` продублированы в `composeApp` и `androidApp`. P7: константы платформы в одном месте
(`libs.versions.toml` / `build-logic`).

**9. [L] `androidApp` зависит от `shared` напрямую в обход `composeApp`** (`androidApp/build.gradle.kts:30-31`),
т.е. app-слой видит data-слой. Желательно app -> presentation API, не app -> data.

**10. [L] Legacy TS (`src/`, 881 строка), `courses/*.json` и `AI/architecture.md` описывают "исторический
слой JSON v2".** Документ сам говорит, что часть описания устарела (kotlin/pack-format удалён, но упомянут
в схеме). P7: два описания реальности. Только уборка документа/кода, не архитектура.

## 3. Что уже хорошо (не трогать)

- Цепочка `core-model -> core-engine -> shared` без циклов; `core-*` свободны от языка/пары.
- Данные: `.core-data` + `.data-contract` -> generated Kotlin rows (P11, P16 соблюдены).
- `TrainingStore` как единственный владелец сессии, упорядоченная очередь записи через `Channel`.
- Parity-тесты с независимыми ожиданиями из `tests/fixtures` (P15).

## 4. Предлагаемые изменения

Принцип отбора: места правки и гарантии (P5, P8), не LOC. Каждый пункт: усилие (S <1 дня, M 1-3 дня,
L >3 дней), принцип, DSL-механизм.

### A. Внедрить пак вместо глобала (Проблема 1) - усилие M-L

- Ввести `interface CoursePackContext { val active: CoursePack; val skillIds: Set<String>; ... }`
  в `shared/.../data/`. `TrainingStore` принимает его в конструкторе вместо `packRegistry`/`skills`/
  `nouns`. Глобалы `val x get() = packRegistry.active...` остаются как deprecated-фасад, пока хосты
  мигрируют по одному.
- Поддерживает: **P12** (модель владеет мутацией пака, store читает), **P9**, **P7**.
- DSL-механизм: часть уже есть - `data-contract` печатает типизированные строки. Выбор "что активно"
  - это FEATURE (`STATE activePack`, `ACTION SelectPack`) в духе Elm/MVP-паттерна biome-dsl; кандидат на
  FEATURE только при ≥2 потребителях (P13: polski = 1 продукт, поэтому DSL - `[гипотеза]`, начать с
  нативного интерфейса, P25 S4).
- Риск: изменение конструктора `TrainingStore` ломает 5 мест создания и тесты (`TrainingStoreTest` 543).
  Сделать значение по умолчанию = глобальный реестр, чтобы существующие вызовы не менялись.
- Проверка: тест store с двумя разными паками в одном процессе (сейчас невозможен).

### B. Единый `SessionFactory` для хостов (Проблема 5) - усилие M

- `shared/.../presentation/TrainingSessionFactory.kt` (NEW): `create(ports, packContext, preferences, scope)
  -> TrainingStore` + `buildTrainingStoreOrRollback` из `desktop/Main.kt:159-179` переезжает сюда. Все 5
  хостов вызывают одну функцию.
- Поддерживает: **P7**, **P12** ("Tutorial/хост владеет lifetime"). Место правки: 5 -> 1.
- `UserPreferencesRepository` в common уже есть (`preferences/UserPreferencesRepository.kt`) -
  web/android реализуют только порт хранилища (key-value), без своей кодировки.

### C. Один snapshot -> view tree, три рендерера (Проблема 2) - усилие L, сначала замер

- Шаг 1 (S, замер): `npx biome patterns kotlin/composeApp/src` из biome-dsl `implementations/typescript`
  по webMain/androidMain/desktopMain - получить кандидатов повтора. Не утверждать выигрыш до замера
  (PATTERN_DICTIONARY §7: совпадение != эквивалентность).
- Шаг 2 (M): вынести в common то, что решает, а не рисует: порядок блоков, подписи, выбор CSS-класса/стиля
  из `Block`/`StyleSnapshot`. Правило: если решение есть в двух рендерерах, оно в `presentation`.
  Кандидат: `MatrixTableSnapshot` уже такой; повторить для Training/Vocabulary/Progress.
- Шаг 3 (L, условный): если измерение шага 1 покажет >=10 повторов формы (порог P13/ProjectMigration:
  "пак ~10 использований"), описать экран как **TEMPLATE** (`screen: snapshot -> {dom, compose}`).
  biome-dsl есть Kotlin-принтер (`lang-kt`), но "для Kotlin плеча нет" (PATTERN_DICTIONARY §7) и
  Py/Kt хосты без названного потребителя не добавляются (P2, TD-118). Поэтому шаг 3 - **не делать**
  до отдельного GO по замеру. Альтернатива: native Compose common-слой для Android+Desktop (там
  обе стороны Compose), Web остаётся на DOM.
- Поддерживает: **P1** (повтор -> паттерн), **P7**, **P5** (места правки: 3 -> 1 на решение).

### D. Разрезать `shared` (Проблема 3) - усилие L

Целевой граф (по слоям P9: ядро -> данные -> презентация -> хосты):

```text
core-model -> core-engine -> srs-progress (srs, progress, textwire, vocabulary doc)
                                 -> course-data (data/*, generated rows, Pl/En engines)
                                       -> presentation (TrainingStore, AppUiState, Matrix*)
                                             -> composeApp / androidApp / ios+mac bridges (iosMain)
```

- Порядок (Парето P8): сначала вынести `iosMain`/`macosMain` в `:apple-bridge` (они ни от кого не зависят
  обратно, ~950 строк в 4 файлах; риск минимален, ускоряет Android/Desktop сборку), потом `srs-progress`
  (чистое ядро, нет зависимости на `polski.data`) `[не проверено: srs/progress/textwire не импортируют
  polski.data - проверить grep перед разрезом]`.
- `course-data` (генерируется) пересобирается отдельно от FSRS и UI.
- Негативные тесты границ: `srs-progress` не видит `polski.data` (аналог `check:boundaries`, P9/P18).
  В Gradle: отсутствие `project(":course-data")` в зависимостях - структурная гарантия.
- Поддерживает: **P9**, **P22**, **P18**. Усилие L из-за перемещения пакетов и тестов (92 теста).
- Отложить, если P19 (стабильность) приоритетнее: разрез в чистом виде не меняет поведение.

### E. Убрать дубли и разбить крупные файлы (Проблемы 4, 6) - усилие S-M каждое

- `SkillQueue` x2: оставить `progress.SkillQueue`, `training.nextSkillId` реализовать через него либо
  удалить после проверки тай-брейка (тест: "equal due -> input order"). Усилие S.
- `TrainingWebApp.kt` 1054 -> `TrainingWebApp.kt` (composable, ~150), `TrainingDomRenderer.kt`,
  `WebEffects.kt` (`executeEffect`), `DomBuilders.kt` (`node`, `button`, `percentEncode`,
  `intervalLabel`). Чисто механический перенос, строки не меняются (P22). Усилие M, под защитой
  `webTest` + `browserAcceptance`.
- `Main.kt` -> `DesktopFileDialogs.kt` (import/export x4), `DesktopNavigation.kt`, `Main.kt` (окно+session).
- `MainActivity.kt` -> `AndroidSettingsScreen.kt`, `AndroidTheme.kt`.
- Кандидаты в порядке размера: TrainingWebApp 1054, VocabularyWeb 616, MatrixWeb 587, TrainingStore 461
  (TrainingStore трогать последним: ядро, 543 теста; разбивать на `ReviewHandler`/`WriteQueue` только после A).

### F. Gradle: build-logic и константы (Проблемы 7, 8) - усилие M

- Перенести seal/ledger-генераторы из `shared/build.gradle.kts` в `build-logic` как typed tasks
  (`GenerateLegacyAdmissionLedger`), в файле остаётся одна строка `polski.legacy-ledger`.
- `polski.android-library` / `polski.kmp-app` convention-плагины: `compileSdk`, `minSdk`, `jvmToolchain`
  в одном месте. Константы - в `gradle/libs.versions.toml`.
- Более радикальный вариант (оценить отдельно, `[гипотеза]`): печатать тест-фикстуры тем же
  `biome generate`, что и rows - по P16/P11 и "рецепт вывода -> TEMPLATE". Делать, только если генератор
  фикстур нужен второму продукту (P13), иначе типизированный Gradle task достаточен (P25 S4).

### G. Мелочи (Проблемы 9, 10) - усилие S

- `androidApp` зависит только от `composeApp` (api-реэкспорт нужных типов presentation); убрать
  `project(":shared")` когда A/D выполнены.
- `AI/architecture.md`: убрать упоминание `kotlin/pack-format` и историю JSON v2 в архив
  (`AI/decisions.md` хранит историю). P7.
- Решить судьбу `nj05ReadOnlyProbe`: если это закрытый эксперимент - удалить из `settings.gradle.kts`
  `[не проверено: статус пробы]`.

## 5. Соответствие biome-dsl принципам (сводка)

| Принцип | Состояние polski | Действие |
|---|---|---|
| P1 Абстракция | UI x3 повторяет форму | C: замер `biome patterns`, затем решение |
| P7 Один факт | глобал-реестр + фасад; SkillQueue x2; prefs x2; compileSdk x2 | A, B, E, F |
| P9 Слои | `shared` смешивает 3 слоя; `androidApp -> shared` | D, G |
| P10 Правило в слое | рецепт генерации в Gradle-коде | F |
| P11 Нет рукописного JSON | соблюдён (`.core-data`) | - |
| P12 Владение | глобал нарушает "модель мутирует"; store - ок | A |
| P13 Словарь из продуктов | polski = 1 потребитель; новый паттерн/пак не заводить | C шаг 3 условно |
| P16 Генерат не руками | соблюдён для rows; фикстуры в Gradle | F |
| P22 Файл <200 | 1054/616/587/461/457/447 | E |
| P25 Граница | DSL для данных - да; UI рендер - native, пока нет замера | C |

Что DSL здесь НЕ решает: проблемы 1, 3, 4, 5, 6, 8, 9 - обычный рефакторинг. DSL-кандидаты только 2
(экранный TEMPLATE, после замера) и 7 (генератор фикстур, при втором потребителе). Это честная граница
по P25: нет стоп-сигналов S4 (1 потребитель) для остального.

## 6. Приоритет

| # | Задача | Усилие | Риск | Эффект (места правки / гарантия) |
|---|---|---|---|---|
| 1 | E: дубль `SkillQueue` + разбить TrainingWebApp/Main/MainActivity | S+M | низкий (механика) | файлы <200, ревью проще |
| 2 | B: `TrainingSessionFactory` | M | низкий | создание store: 5 -> 1 |
| 3 | A: `CoursePackContext` вместо глобала (с дефолтом) | M-L | средний | store тестируем с 2 паками |
| 4 | G: `androidApp` без `:shared`, чистка документа/пробы | S | низкий | граница видна |
| 5 | F: build-logic для Gradle-задач и констант | M | низкий | `shared/build.gradle.kts` -200 строк |
| 6 | C шаг 1-2: замер повторов, вынос решений в `presentation` | S+M | средний | решения рендера: 3 -> 1 |
| 7 | D: разрез `shared` (сначала `:apple-bridge`) | L | средний | инкрементальная сборка, границы |
| 8 | C шаг 3: экранный TEMPLATE | L | высокий | только после GO по замеру |

Обоснование порядка: 1-2 механические и дают быстрый эффект; 3 разблокирует тесты и разрез D; 7 дорог
и не меняет поведение, поэтому после стабилизации (P19); 8 может не понадобиться.

## 7. Проверка каждого шага

- До: `./gradlew :shared:allTests :composeApp:desktopTest :androidApp:testDebugUnitTest` - зафиксировать
  зелёный baseline и дату (P17: старый лог не покрывает новые правки).
- После каждого шага: те же задачи + `webTest`; для web-файлов `browserAcceptance` / `playwright.kotlin.config.ts`.
- Parity-тесты (`GrammarParityTest`, `TrainingParityTest`) не менять (P15); если красные - ошибка в рефакторинге.
- Для iOS/macOS: `iosTest`/`macosTest` и сборка Swift-проекта `[NOT RUN в этом анализе]`.
- DSL-правки (если дойдём до F/C шага 3): `npx biome check changed` и `generate --check` в biome-dsl.

## 8. Не проверено в этом анализе

- Не запускались сборка, тесты и `biome patterns`; цифры строк взяты из постановки задачи и `wc -l`.
- Не читались целиком: `VocabularyWeb`, `MatrixWeb`, `IosSnapshot`, `AndroidSessionViewModel`.
- Не проверено, что `srs`/`progress`/`textwire` не импортируют `polski.data` (условие для разреза D).
- Не проверен `browserAcceptance` (зависимости на модули не найдены grep-ом по `project(`).
- Не проверено, существует ли откат пака (`buildTrainingStoreOrRollback`) на web/android/iOS.
