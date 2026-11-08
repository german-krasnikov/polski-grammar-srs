# 02. Консолидация UI по платформам: анализ и план

Дата: 2026-11-08. Оценка по `biome-dsl/AI/PRINCIPLES.md` (P1, P5, P7, P9, P10, P25), `PATTERN_DICTIONARY.md` §1/§4,
`ProjectMigratioPrincipals.md` (место правки и гарантии важнее строк). Пути от `kotlin/composeApp/src/` если не указано иное.

Метод: прочитаны целиком `desktopMain/.../TrainingScreen.kt`, `androidMain/.../AndroidTrainingScreen.kt`,
`TrainingWebApp.kt` (строки 320-420, 600-760 + список функций), список функций `MatrixWeb.kt`/`VocabularyWeb.kt`,
первая часть `MatrixScreen.kt` (desktop), `VocabularyScreen.kt` (common, до строки 120), `Block.kt`, `MatrixTableViewModel.kt`,
`IosSnapshot.kt` (75-130), `NativeHostDecision.md`, `WebImplementation.md` (решение DOM).
Не читались целиком: `VocabularyWeb.kt`, `MatrixWeb.kt`, `AndroidMatrixScreen.kt` (структура видна из списка функций),
Swift-файлы (только счёт строк и строковых литералов). Всё остальное помечено `[не проверено]`.

## 1. Главный вывод

Логика уже не дублируется: `TrainingStore`, `AppUiState`, `AppAction`, `StyleComposer`, `Block`, `MatrixTableViewModel`
живут в `shared` (`shared/src/commonMain/kotlin/polski/presentation/`, 1425 строк). Дублируется **третий слой между
состоянием и виджетом**: какие секции показать в какой фазе, какие подписи, какой ключ что делает, что маскировать,
как считать интервал. Этот слой сейчас пишется 5 раз (web DOM, Android, Desktop, iOS Swift, macOS Swift) - не 3.

Просьба «перенести в commonMain» как есть **не подходит**:

1. `Plans/Kotlin/NativeHostDecision.md` (решение владельца 2026-09-25): web = семантический DOM, Android = свой Compose,
   iOS/macOS = SwiftUI. Прямо: «Общий Compose UI модуль для мобильных экранов не вводить».
2. `Plans/Kotlin/WebImplementation.md` (замер): Compose canvas на web даёт textbox без имени и таблицу без `row`/`columnheader`.
   Web не может использовать Compose-экран; commonMain Compose покрыл бы максимум 2 из 5 хостов (Android, Desktop).
3. Desktop Compose - «превью и источник данных» до приёмки нативного macOS (`NativeHostDecision.md`, `kotlin/README.md:48`).

Поэтому консолидируем **модель экрана** (данные + решения), а не Compose-код. Рендереры остаются нативными (P10, P25).

## 2. Замеры

### 2.1 Размеры (факт, `wc -l`)

| Хост | Файлы | Строк |
|---|---|---|
| `commonMain` (composeApp) | 3 (1 экран) | 456 |
| `androidMain` | 15 | 2494 |
| `desktopMain` (UI-экраны 4 + Main + platform) | 13 | 2006 (экраны: 905) |
| `webMain` | 20 | 3997 |
| Swift (iOS+macOS) | 8+ файлов | 4665 (`PolskiGrammarApp.swift` 1422, `PolskiGrammarMacApp.swift` 1220) |
| Legacy TS `src/` | React-эталон | 881 |

### 2.2 Состав кода (эвристика по regex, **не точная**)

Классификатор строк: platform API (`window.`, `pointerInput`, `onKeyEvent`, `Rive`, `focusRequester`, `setAttribute` ...),
logic/state-read (`dispatch(`, `state.phase`, `StyleComposer`, `Rating.`), layout/widgets (`Column`, `Text(`, `node(`),
other (остаток, в основном DOM-цепочки `.apply {` и разметка). Доли от непустых некомментированных строк.

| Файл | Строк кода | platform API | logic/state | layout | other |
|---|---|---|---|---|---|
| `TrainingWebApp.kt` | 753 | 10% | 16% | 37% | 37% |
| `VocabularyWeb.kt` | 432 | 14% | 2% | 37% | 48% |
| `MatrixWeb.kt` | 438 | 10% | 8% | 25% | 57% |
| desktop `TrainingScreen.kt` | 238 | 4% | 28% | 53% | 15% |
| `AndroidTrainingScreen.kt` | 234 | 7% | 24% | 48% | 20% |
| `AndroidMatrixScreen.kt` | 308 | 1% | 9% | 50% | 40% |
| common `VocabularyScreen.kt` | 225 | 3% | 6% | 60% | 31% |

Что это значит: **platform API - 1-14%**. Genuinely platform-specific код мал. 86-99% строк - это раскладка того же
содержимого плюс ветвление по тому же `AppUiState`. В `TrainingWebApp.kt` `TrainingDomRenderer` (строки 320-975) ~650 строк
в одном классе: рядом лежат решение (`justRevealed`, `enteringChainComplete`, `justShowedReference`) и DOM.

### 2.3 Дублирование литералов UI (факт, regex по русским строкам >=6 символов в training-экранах)

Web 72, Desktop 37, Android 38, iOS 182, macOS 103 уникальных строк; всего 301 уникальная.
В >=2 хостах: 81; в >=3: 30; в >=4: 18; в 5 хостах: 2.
Примеры в >=4 хостах: «Перейти к заданию», «Следующий набор слов», «Таблица под рукой», «Повторения на сейчас завершены»,
«Совпадает с правильным вариантом», «Сравни свой ответ с эталоном», «ЗАПОМНИ», «Ответ не введён».
Правка одной формулировки = до 5 мест.

### 2.4 Одинаковые решения, написанные заново

| Решение | Где копии |
|---|---|
| `intervalLabel(due, now)` (мин/ч/дн) | `TrainingWebApp.kt:1005`, desktop `TrainingScreen.kt:310`; Android/Swift - `[не проверено]` |
| Порядок секций карточки: source -> prompt -> retrieve -> ответ -> «Что изменилось» -> «ЗАПОМНИ» -> lifehack -> review | web `renderCard`, desktop `TrainingScreen`, Android `AndroidTrainingScreen`, `FlashCardView.swift`, `MacFlashCardView.swift` |
| `introducing = phase==Question && introPending` и скрытие справки | web, desktop, Android (3 копии выражения) |
| Hotkeys Space/1/2 (что в какой фазе) | desktop `onKeyEvent` (строки ~70-95), web keydown-handler `[не проверено]` |
| Маскирование целевой строки справки до reveal | `AndroidCaseReference`, `matrixContrastMasked` (web), `IosSnapshot.referenceMaskPlaceholder` |
| Подпись режимов/ответов (`Цепочка предложений`, `Вслух / про себя`) | везде, разные формулировки (desktop «Ответ вслух / про себя», Android «Вслух / про себя») |
| Строка «Next: Таблица под рукой / Скрыть таблицу» | 3 Kotlin-хоста + Swift |

Уже решено правильно (образец): `StyleComposer.compose(...)` -> `List<Block>`; все хосты рисуют блоки
(`CardBlocksWeb.kt`, `AndroidStyleBlocks.kt`, `MacStyleBlockView.swift`), не ветвятся по `styleId`. Но Desktop
`TrainingScreen.kt` этого **не** использует: он ещё ветвит `if (state.styleId == StyleId.SituationFirst)` (строки ~125, ~200)
и вручную собирает «ЗАПОМНИ». Android с комментарием `S2: never a literal if/else on styleId here` уже мигрировал.
Т.е. Desktop - отстающая копия (нарушает P7: логика выбора контента повторена вручную).

### 2.5 Vocabulary - особый случай

`commonMain/.../VocabularyScreen.kt` (293) вызывается **только** из `desktop/Main.kt:399` и desktop-тестов (grep).
Android имеет свой `AndroidVocabularyScreen` (197) и не вызывает common-версию; web - `VocabularyWeb.kt` (616).
То есть «common» экран фактически desktop-экран; его параметр `enableSwipeRating` и двойные ветки
`if (enableSwipeRating) Column else Row` - следы того, что он проектировался и для Android, но Android отделился [значение флага у вызывающих не проверено].
Имя `commonMain` вводит в заблуждение (P7: расположение должно отражать владельца).

### 2.6 Matrix - миграция начата, не закончена

`MatrixTableViewModel`/`toViewModel()` уже существует и используется по 1 файлу в web/Android/Desktop, но все три хоста
всё ещё вызывают `nounPhrase(...)` сами (web 4, Android 4, desktop 2). Desktop `CaseReferenceScreen` строит таблицу
вручную: `MatrixTable(listOf("Падеж", ...), caseRows.map { ... nounPhrase(...) })`. Т.е. таблица "справка по падежам"
собирается 3 раза, хотя модель есть. Нарушает P7 и собственное правило `MatrixTableViewModel` (комментарий: «single source every host renderer reads»).

## 3. Что общее, что платформенное

| Категория | Решение | Основание |
|---|---|---|
| Состояние, действия, reducer | Уже `shared` (`TrainingStore`) | P12, сделано |
| Состав и порядок секций, подписи, видимость, hotkeys-карта, маски, интервалы | **В `shared` как pure-функция/модель** (новое) | P7, P10 «решает, а не исполняет» |
| Рендер виджетов (DOM / Compose / SwiftUI) | Остаётся в хосте | NativeHostDecision, a11y-blocker, P25 S5 (физика) |
| Gesture, Rive, focus/IME, `matchMedia(pointer: coarse)`, анимации раскрытия, `HtmlElementView` | Платформенное | P25 S5 |
| Persistence/prefs/system (`Desktop*Repository`, `WebPreferencesRepository`) | Платформенное, уже порты | P9 |
| Legacy TS `src/` (881) | Заморозить как oracle, не трогать | Plans: «React остаётся эталоном» |

## 4. План консолидации

Порядок по частоте правок и риску (P8). Каждый шаг самостоятелен, зелёный gate до следующего.
Шаги разбиты на ~100-150 строк правки на файл.

### Шаг 1. `UiCopy` - единый каталог подписей (P7)

NEW `shared/src/commonMain/kotlin/polski/presentation/UiCopy.kt`: `object UiCopy` с константами для строк из 2.3
(начать с 18 строк, встречающихся >=4 хостах; потом 30 строк >=3). Хосты ссылаются на константы.
Для Swift - строки уходят в snapshot (`IosSnapshot.kt`/`MacSnapshot.kt` уже передают `chainCompletionTitle`,
`chainStepSummary`; это прецедент). Не ломать accessibility-идентификаторы в UITests: `PolskiGrammarUITests.swift`,
`FlipCorrectnessUITests.swift`, `DesktopScreenTest.kt`, `AndroidCaseReferenceLeakTest.kt` ищут текст - прогнать, текст не менять.

Тест: NEW `commonTest` проверка «ни один из хостов не содержит литерал из `UiCopy`» (grep-тест по исходникам, как
`check:boundaries` в biome-dsl) - иначе копия вернётся.

### Шаг 2. `TrainingCardModel` - модель экрана (P7, P10)

NEW `shared/.../presentation/TrainingCardModel.kt`:

```kotlin
fun trainingCardModel(state: AppUiState, now: Instant?): TrainingCardModel
```

Содержит (данные, не виджеты): `header` (режимы + selected + `dueCount`), `chainProgress`, `referenceToggle(label, enabled)`,
`front: List<CardSection>`, `back: List<CardSection>` (поверх существующих `Block`), `ratingButtons: List<RatingButton(rating, label, intervalLabel)>`,
`introducing`, `hotkeys: Map<Key, AppAction>` (абстрактный enum `HotKey`, не Compose `Key`), `lifehackBadge`.
Сюда переезжает `intervalLabel` (удалить 2 копии), `introducing`, выбор `method` (logic/situations), правило маски справки.

Не входит: стили Compose, CSS-классы, анимации, `FocusRequester`.
Зависимости: только `polski.presentation`/`polski.data` (направление `composeApp -> shared`, без нового ребра; P9).

Тесты (`shared/commonTest`, TDD): таблица «фаза x answerMode x introPending x styleId -> секции/кнопки/hotkeys» -
независимые ожидания (P15), не из вывода кода. Граничные: `introPending` + `showReference`, `NoDue`, `ChainComplete`,
Typed vs Oral, стиль с fallback, нет lifehack.

### Шаг 3. Перевести хосты на модель по одному

Порядок: Desktop -> Android -> Web -> iOS/macOS snapshot.

1. **Desktop `TrainingScreen.kt`** (317 -> ожид. ~180): убрать ручную ветку по `styleId`, подключить `StyleComposer`/модель.
   Сначала самый отстающий хост, он же самый дешёвый.
2. **Android `AndroidTrainingScreen.kt`** (348): убрать подписи, `introducing`, ручное составление.
3. **Web `TrainingWebApp.kt`** (1054): выделить из `TrainingDomRenderer` чистый «что рисовать» (читает модель) и
   «как DOM» (платформа). Попутно разбить файл (CLAUDE.md проекта: файлы <200 строк): `TrainingDomRenderer` (650),
   `executeEffect`, `render` с diff-логикой (`previous`, `composing`) - три файла.
4. **Swift**: `IosSnapshot.kt:75-208` и `MacSnapshot.kt` сериализуют `TrainingCardModel` вместо ручной сборки полей.
   Snapshot станет проекцией модели (одна сериализация, не два набора `put(...)`). Swift-код при этом упрощается,
   версию `schemaVersion` поднять и проверить совместимость (риск, см. §6).

Измерение выигрыша (P5, P25): после шага 3 пересчитать §2.3/§2.4 тем же скриптом; успех = число хостов с копией
правила 5 -> 1 (модель) + рендереры, а не уменьшение строк.

### Шаг 4. Matrix: довести `MatrixTableViewModel`

Убрать три ручных построения таблицы справки: `CaseReferenceScreen` (desktop), `AndroidCaseReference`, `renderCaseReferenceWeb`.
NEW `caseReferenceViewModel(exercise, revealed)` в `shared/.../presentation/MatrixTables.kt` (файл уже есть) возвращает
таблицу с уже применённой маской (`isTarget && !revealed`). Затем `MatrixWeb.kt` (587) и `AndroidMatrixScreen.kt` (437)
читают только модели секций (`Map/Cases/Verbs/Pronouns/Lifehacks`).

### Шаг 5. Vocabulary: решить владельца

Два варианта, выбрать один (см. §6, вопрос 1):

- A. `VocabularyScreen.kt` переименовать/переместить в `desktopMain` (честное расположение, 0 изменений поведения). Дёшево.
- B. Если Android должен делить его: удалить `AndroidVocabularyScreen` (197) и `enableSwipeRating`-ветки. Противоречит
  NativeHostDecision; не делать без решения владельца.

Одновременно вынести `VocabularyCardModel` (по образцу шага 2): `VocabularyWeb.kt` (616) содержит `VocabularyWebController` с
логикой выбора направления/интервалов, `activePackLanguages()`, `nativeToTarget()`. [не проверено, что это уже есть в `VocabularySession`]

### Шаг 6. Desktop: заморозка или выход

Desktop Compose - превью (NativeHostDecision) при том, что нативный macOS на SwiftUI существует (`macosApp/`, 4665 Swift строк
вместе с iOS). Каждая правка карточки сегодня идёт в 5 мест, одно из них - заведомо переходное.
Рекомендация: после шагов 1-2 **заморозить** desktop-экраны (только багфиксы), учесть в `AGENTS.md`; удаление - отдельное решение
после приёмки macOS (см. §6, вопрос 2). Не оценивать экономию «строк desktop» до этого решения.

## 5. Роль DSL (P1, P10, P25)

Проверка по AGENTS.md: «перед новым синтаксисом сравни с нативным помощником с теми же гарантиями».

| Что | Нативный помощник | DSL TEMPLATE | Вердикт |
|---|---|---|---|
| `UiCopy` | Kotlin `object` + snapshot в Swift | `.copy` строки -> Kotlin + Swift + web | Начать нативно. DSL имеет смысл только если нужна **генерация Swift** (V3 вывод) и гарантия «ни одного литерала» (V4). Замер после шага 1 |
| `TrainingCardModel` | pure Kotlin функция + commonTest | `DECISION` (фаза x режим -> секции) | Кандидат на DECISION: правило «решает, а не исполняет» (P25.1), таблица истинности, >=3 потребителя. Но stop-signal S3/S1 по размеру: модель ~150 строк. Сравнить длину DECISION vs Kotlin до внедрения |
| Hotkeys-карта | enum + map | декларация `KEYS` | Нативно; 2 потребителя (web, desktop) |
| Рендереры (DOM/Compose/SwiftUI из одного описания) | - | TEMPLATE «screen» | **Не делать**: шаблон экрана по 3 UI-стекам = дорогая определяемая часть, 1 продукт, нет C1+ потребителя (P13, S4); сжатие с одной целью <1 (PATTERN_DICTIONARY §2) |

Повторяющийся скелет «карточка -> секции -> кнопки» подходит под `npx biome patterns` для поиска кандидатов [не запускалось].
Вывод: консолидация делается нативным Kotlin; DSL рассматривается после замера как следующий шаг, не как условие.

## 6. Риски и открытые вопросы

Решения владельца (без них шаги 5-6 не начинать):

1. Vocabulary: оставлять ли Android-экран отдельным (вариант A) или объединять (B, против NativeHostDecision)?
2. Desktop Compose: заморозить сейчас или удалить после приёмки macOS SwiftUI?
3. Нужен ли `UiCopy`-каталог как единственный источник для будущей локализации (учебный язык/родной язык из `CoursePack`) -
   тогда формат подписей должен быть pack-aware, иначе введём второй каталог (риск P7).

Риски:

- **Snapshot-совместимость Swift** (шаг 3.4): `IosSnapshot`/`MacSnapshot` - публичный контракт для 4665 строк Swift. Поднять `schemaVersion`,
  держать старые поля до миграции Swift; UITests `PolskiGrammarUITests`, `FlipCorrectnessUITests` обязательны. NOT RUN до фактического запуска (P17).
- **Web diff-рендер** (`render()`: `old.copy(draft = state.draft) == state`, `composing`): поведение IME/фокуса тонкое; модель
  не должна менять момент перестроения DOM. Покрыть `browserAcceptance`/webTest до рефактора [не проверено, какие тесты есть].
- **Регресс подписей**: UITests ищут текст. Менять формулировки отдельным коммитом (P15).
- Эвристика §2.2 грубая: «other» у web = в основном DOM-цепочки; платформенной доли может быть больше. Не использовать как KPI.

## 7. Критерии готовности

- Шаг 1: `grep` по хостам на литералы `UiCopy` = 0; `intervalLabel` один.
- Шаг 2: таблица тестов модели зелёная в `shared:commonTest` без Compose/DOM.
- Шаг 3: каждый хост проходит свои тесты (`desktopTest`, `androidApp` unit, `webTest`, Swift UITests - NOT RUN считать явно).
- Шаг 4: `nounPhrase(` в `composeApp` host-экранах = 0.
- Повторный замер §2.3-2.4 и запись «до/после» по числу мест правки (P5).

## 8. Нарушения, найденные в анализе

| # | Находка | Принцип | Серьёзность |
|---|---|---|---|
| 1 | Порядок/состав секций карточки написан 5 раз | P7, P10 | H |
| 2 | 81 строка UI в >=2 хостах, 18 в >=4 | P7 | H |
| 3 | Desktop `TrainingScreen` ветвит по `styleId`, обходя `StyleComposer` | P7, P10 | M |
| 4 | Таблица справки по падежам строится 3 раза при существующем `MatrixTableViewModel` | P7 | M |
| 5 | `intervalLabel` скопирован минимум 2 раза | P7 | L |
| 6 | `commonMain/VocabularyScreen` используется только desktop | P7, P9 (расположение) | L |
| 7 | `TrainingDomRenderer` ~650 строк, решения и DOM смешаны | P10, правило <200 строк | M |
| 8 | Предложение «общий Compose в commonMain» противоречит `NativeHostDecision.md` и a11y-замеру web | P25 (граница по замеру) | - |
