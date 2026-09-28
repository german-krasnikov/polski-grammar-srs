# Приёмка пакета en-ru — 2026-09-28 (финальная, report-only)

Ревизия: `main` @ `40ccbc2` (рабочее дерево чистое на момент проверки, 212 коммитов впереди `origin/main`). Роль: senior-tester + native-level линговист en/ru. Режим: report-only — правки производственного кода не вносились; ниже только реально наблюдавшиеся результаты и найденные дефекты.

## 0. Итог одной строкой

Тест-матрица (Gradle/npm/Playwright/xcodebuild) — **зелёная по всем прогнанным пунктам**. Но заявленный в задаче пользовательский сценарий — «переключить Settings на target English / native Russian и пройти все 16 en-skills» — **невозможно выполнить ни на одном из 5 хостов через UI сегодня**: пакет en-ru зарегистрирован, но не проходит внутренний gate `usableCourseSelections` (см. §2), а на web пикера target/native вообще не существует в коде. Это не гипотеза — подтверждено живьём на собранном web-дистрибутиве, на реальном Android-эмуляторе (`emulator-5554`) и текстом действующего iOS UI-теста. Контент пакета (16 skills, лексикон, emphasis-контракт) при этом лингвистически корректен и был проверен напрямую через движок (см. §4), а не через недостижимый Training UI. pl-ru не задет: весь основной regression-набор зелёный.

## 1. Тест-матрица

| Проверка | Команда | Результат |
|---|---|---|
| `:shared:desktopTest` | `./gradlew :shared:desktopTest` | PASS (UP-TO-DATE, дерево чистое → доказательство валидно) |
| `:core-engine` тесты | `./gradlew :core-engine:desktopTest` | PASS |
| `:pack-format` тесты | `./gradlew :pack-format:desktopTest` | PASS |
| `:composeApp:desktopTest` | — | PASS |
| `:shared:macosArm64Test` | — | PASS |
| `:shared:jsBrowserTest` | — | PASS |
| `:shared:wasmJsBrowserTest` | — | PASS |
| `:composeApp:compileKotlinJs` / `compileKotlinWasmJs` | — | PASS (BUILD SUCCESSFUL) |
| `:shared:iosSimulatorArm64Test` | — | PASS |
| `:androidApp:testDebugUnitTest` | — | PASS |
| `:androidApp:assembleDebug` | — | PASS |
| `npm test` | `npm test` | PASS — 268/268, 41 файлов |
| `npm run course:validate` | — | PASS — 4 валидатора (course pack, vocabulary editorial, curriculum, pack v2) зелёные |
| `composeCompatibilityBrowserDistribution` | `./gradlew :composeApp:composeCompatibilityBrowserDistribution` | PASS — дистрибутив собран, wasm+JS-фолбэк |
| macOS native build | `xcodebuild … PolskiGrammarMac … build` | PASS — `** BUILD SUCCEEDED **` |
| iOS simulator build | `xcodebuild … PolskiGrammar … -destination 'iPhone 17 Pro Simulator' build` | PASS — `** BUILD SUCCEEDED **`, приложение установлено и запущено на симуляторе `4384946F-9E6B-43D0-ADA3-CA219A3456B8` |
| Playwright, wasm, chromium (training/styles/matrix/vocabulary/settings specs) | `KOTLIN_SPIKE_BRANCH=wasm npx playwright test …` | 64 passed / **2 pre-existing failed** (не регрессия, см. ниже) |
| Playwright, js, chromium (те же specs) | `KOTLIN_SPIKE_BRANCH=js npx playwright test …` | 64 passed / **2 pre-existing failed**, идентично wasm |
| Android build+install+smoke | `adb install` + запуск на `emulator-5554` | PASS — приложение живо, экраны Тренировка/Матрица/Настройки/Слова открываются |
| iOS build+install+smoke | `xcrun simctl install/launch` на iPhone 17 Pro | PASS — приложение живо, Тренировка/Настройки открываются |

**2 Playwright-падения** (`kotlin-parity-matrix.spec.ts`: «tense comparison» и «aspect form», отсутствуют `.change-before`/`.change-after` в 2 конкретных pl-таблицах) — не регрессия этой приёмки: тот же разрыв документирован в ADR-22 («предсуществующий, не внесённый и не исправленный … разрыв»), сегодня повторно подтверждён — одинаково падает на wasm и js веткам, не связан с en-ru.

## 2. Главная находка — en-ru физически нельзя сделать активным пакетом ни на одном хосте

`kotlin/shared/src/commonMain/kotlin/polski/data/CourseData.kt:591-611` (`usableCourseSelections`) — собственный комментарий разработчика:

> «en-ru fails this probe: `lang/en/lexicon.json` correctly has no grammatical gender or case … but `CoursePack`'s schema was written for pl-ru alone and requires both throughout (`Noun.gender`, case-keyed `possessiveForms`/`futureAuxiliary`/reference rows, `PossessiveId`'s closed set not even knowing `its`…)».

То есть: пакет **зарегистрирован** (`packRegistry.contains("en-ru") == true`, `availableCourseSelections` его перечисляет), но **не проходит** `parsesCompletely()` и поэтому исключён из `usableCourseSelections` — единственного списка, из которого любой хост может реально выбрать активный пакет (`selectActiveCoursePack`/`availableCoursePacks` читают только его). Это не отдельный баг одного хоста — гейт живёт в `:shared` commonMain и одинаково действует на все 5 хостов. Подтверждено живьём на трёх независимых путях:

1. **Web.** В `composeApp/src/webMain/...` нет вообще ни одного упоминания `packRegistry`/`CourseSelection`/`selectCoursePack` (`grep -rn` — пусто). Собранный дистрибутив, открытый headless Chromium: вкладка «Настройки» не содержит раздела «Курс»/«Целевой язык» вообще — только стиль объяснений, способ ответа и оформление. EN-22 для web не выполнена **ни разу** — в отличие от android/ios/macos у него просто нет ни одного коммита `feat(web): EN-22 …`.
2. **Android**, актуальная сборка, живой прогон на `emulator-5554`: «Настройки» → «Целевой язык» показывает единственный radio-пункт «Польский» — «Английский» отсутствует физически (скриншот `android-02-settings.png`).
3. **iOS**, актуальный исходник: `kotlin/iosApp/PolskiGrammarUITests/PolskiGrammarUITests.swift:29` — тест называется `testCoursePickersListOnlyUsablePacks` и **утверждает** `XCTAssertFalse(app.buttons["Английский"].exists, "en-ru must not be offered while it is unusable")` — то есть текущее ограничение зафиксировано как ожидаемое поведение, не пропущено.

Даже если бы гейт не стоял, переключение `packRegistry.active` **всё равно не меняет**, что генерирует Training-карточка: `plExerciseGenerator`/`plMorphology`/`plChainSteps` жёстко привязаны к `PackEngine("pl")` на всех 5 хостах (см. коммиты EN-07/EN-22, `ADR-33`: «Тренировочная карточка … остаётся польской независимо от выбора в пикере»). То есть даже гипотетическое закрытие гейта не открывает «пройти все 16 en skills через реальные карточки» — нужен ещё один, отдельный шаг wiring'а движка Training к активному пакету.

**Практический вывод: требуемый в задаче сценарий — «switch Settings to target English/native Russian and walk: all 16 en skills front/back in 4 styles, chain mode…, vocabulary cards» — физически недостижим ни на одном хосте сегодняшней сборки.** Отчёт по этой части — **NOT_RUN**, с указанной выше причиной, не «пропущено по недосмотру».

### Что из этого всё же реально работает (независимо от гейта)

- **Матрица (EN-24).** У web (`MatrixWeb.kt`) и iOS (`MatrixView.swift`) английская таблица глаголов/do-support построена через **отдельный**, не зависящий от `packRegistry.active` движок (`val enMorphology = PackEngine("en")`, `EnLexicon.kt`) — поэтому реально видна и корректна без всякого пикера. Подтверждено:
  - iOS — реальные скриншоты в репозитории `Plans/Kotlin/artifacts/ios/en24-matrix-english-verbs.png` (he → sees/saw/will see) и `en24-matrix-do-support.png`.
  - Android — подтверждено живьём в этой приёмке: «Матрица» → «Времена и лица» реально показывает `see → sees / saw / will see` по местоимениям (скриншот `android-07-verbs-scrolled.png`, `adb`-путь: Матрица → выбор раздела «Времена и лица»).
  - Web — `tests/browser/kotlin-en-matrix.spec.ts` прошёл в составе Playwright-прогона (wasm и js).
- **Лайфхаки (EN-19/21).** Контент (32 записи en-ru) существует и порт `LifehackProvider` реализован на всех 5 хостах, но блок рисуется по `skill.id` **текущего** упражнения — а упражнение остаётся польским (см. выше), поэтому английские лайфхаки сегодня не всплывают ни на одном реальном экране, несмотря на готовый код и данные.
- **Словарь (EN-16, 30 карточек).** Контент существует и прошёл собственную редакторскую проверку (commit `3c11095`), но UI словаря на web жёстко хардкожен на `StudyDirection.RussianToPolish/PolishToRussian` (`VocabularyWeb.kt:34,118-123` — только «Русский → польский» / «Польский → русский» в селекторе), несмотря на то что EN-09 уже сделал `StudyDirection` открытым типом. En-ru карточки недостижимы через UI словаря ни на одном проверенном хосте.

## 3. Лингвистическая проверка контента (напрямую через движок, не через недостижимый UI)

Поскольку Training UI недостижим, проверка сделана на уровне пакетных данных и реального движка — `EnExerciseGeneratorTest`/`EnEmphasisContractTest` (`:shared` commonTest, зелёные) дают реальный, не синтетический вывод `ExerciseGenerator` по всем 16 `lang/en` skill id; тексты сверены вручную с `courses/pairs/en-ru/pair.json`'s `exerciseCopy`/`exercisePatterns` (реальные шаблоны подстановки, не тестовые заглушки).

**Грамматика 16 skills — корректна.** Формулы/теория/примеры (`role.object`, `verb.presentSimple/pastSimple/futureSimple/presentContinuous`, `possessive.my`, `mood.question`, `role.location/instrument/recipient`, `polarity.present/past`, `number.plural`, `pronouns`, `tense.contrast`, `mixed`) — грамматически верный английский и точный, идиоматичный русский текст объяснений. Do-support (`do/does/did` + `not`, инверсия в вопросе), неправильные глаголы (`go→went`, `see→saw`), `-s/-es`, `-ed`, `-ing`, `will`, множественное число (`child→children`) — реализованы правильно, ошибок не найдено.

**Найденный контентный дефект (низкий/средний, легко чинится):** `en:role.recipient` (`courses/lang/en/curriculum.json`) имеет `"lexicalFilter": null` — то есть случайный noun-seed может быть любым из 12 (`wife, husband, friend, son, dog, cat, book, car, house, phone, child, window`), включая неодушевлённые. Это порождает семантически невозможные предложения вида **«I give a present to my new house/car/window/phone.»** — получателем подарка не может быть дом/машина/окно. Для сравнения, соседний `en:pronouns` **уже** ограничивает pool через `lexicalFilter: {slot: noun, where: {nounId: […]}}` (неодушевлённые — осознанно, для «it»). Исправление: аналогичный `lexicalFilter`, ограничивающий `role.recipient` одушевлёнными носителями (`wife/husband/friend/son/dog/cat/child`) — чисто контентная правка одного поля, без изменения кода.

**Мелкая шероховатость (низкий приоритет):** `en:tense.contrast`'s три параллельных примера меняют объект между Simple («I read **new books**», мн.ч., без артикля) и Continuous/Perfect («I am reading / I have read **a new book**», ед.ч. с артиклем) — контраст должен показываться на одном и том же предложении, различается только форма глагола; смена числа/артикля затемняет именно то сравнение, которое skill призван продемонстрировать.

**Не дефект, а унаследованная от pl-ru конвенция:** предложения вида «My beautiful house walks/walked/will walk» (неодушевлённое подлежащее + глагол движения) семантически странны, но это **тот же паттерн**, что уже использует pl-ru (`verb.present`-рецепт берёт `nom`-роль seed'а как подлежащее + `verbLexeme` — в pl это `go`/`iść`, буквально «mój dom idzie»). Не регрессия en-ru, отмечено для полноты, не как приоритетный фикс.

## 4. Emphasis-контракт (EN-23)

Проверен на реальном выводе движка через `EnEmphasisContractTest` (`:shared` commonTest, зелёный, часть общего PASS в §1): do-support вставка (`do/does/did`, `not`) рендерится как `insertion`, никогда как `ending`; неправильный глагол (`see→saw`) — как `whole`; стяжения (`don't`/`didn't`) объявлены как typed-answer `accepted` и структурно никогда не участвуют в diff-рендере (`sentenceHighlightParts`/`changeHighlightParts` читают только `exercise.changes`, `AnswerEvaluator.evaluate` — только `exercise.accepted`/`expected`, два независимых пути, проверено на реальных en-ru данных). Дефектов не найдено.

## 5. pl-ru — не задет

Все golden-guard тесты (`TrainingParityTest`, `GrammarParityTest`, `NoLanguageLiteralsTest`) зелёные без изменения ожиданий во всём core-task матче (§1). `npm test` 268/268 и `course:validate` не тронуты содержательно en-ru работой (только аддитивные añadido в схемах — см. ADR-26 про `vocabulary-editorial-v1.schema.json`, проверено регрессией). pl-ru прогресс/направление словаря/матрица — байт-в-байт то же поведение, что до начала en-ru работ.

## 6. NOT_RUN (с причиной)

| Пункт задачи | Причина |
|---|---|
| Переключение Settings на target=English/native=Russian и обход всех 16 en skills front/back в 4 стилях, живьём через Training UI | Физически невозможно на всех 5 хостах — en-ru не входит в `usableCourseSelections` (§2); на web пикера вообще нет в коде |
| Chain-режим на английском через UI | Тот же блокер — активный пакет остаётся pl-ru |
| Карточки словаря en-ru через UI | Тот же блокер + web жёстко хардкожен на pl-ru/ru-pl (`VocabularyWeb.kt`) |
| Лайфхак-блоки en-ru через UI | Контент/код готовы, но недостижимы — блок читает skill текущего (польского) упражнения |
| Firefox/WebKit ветки Playwright | Не запрашивались явно в задаче как обязательные (только «wasm+js chromium»); не прогонялись в этой приёмке |
| Полное переключение всех 5 хостов на `MatrixTableViewModel` (UC-09 часть 2/2) | Явный, задокументированный техдолг плана (§7 «что не входит»), не в объёме EN-релиза |

## 7. Приоритизированный список задач-фиксов (лейны)

1. **[core, самое важное, блокирует всё остальное] Обобщить схему `CoursePack`, чтобы en-ru проходил `parsesCompletely()`.** Убрать жёсткую зависимость `Noun.gender`/case-keyed `possessiveForms`/`futureAuxiliary`/reference-строк и закрытый `PossessiveId` (не знает `its`) от pl-грамматики — сделать эти поля опциональными/языко-нейтральными там, где язык их физически не имеет. Без этого шага все нижеследующие пункты бессмысленны — пикер может показывать «Английский», но выбор будет no-op.
2. **[core] Wiring Training-движка к активному пакету.** `plExerciseGenerator`/`plMorphology`/`plChainSteps` (все 5 хостов) должны читать `PackEngine(packRegistry.active.id)`, а не жёсткий `"pl"` — иначе даже после фикса №1 карточки Training останутся польскими.
3. **[web] Реализовать EN-22 для web** — пикеры «Целевой язык»/«Родной язык» в `SettingsWeb.kt`, единственный хост без них вовсе.
4. **[web/android/ios/macos] Открыть словарь для en-ru** — `VocabularyWeb.kt` (и, вероятно, аналоги на других хостах) жёстко на `StudyDirection.RussianToPolish/PolishToRussian`; после фиксов 1-2 подключить открытый `StudyDirection` к реальному выбору направления en-ru/ru-en.
5. **[content, малое] `en:role.recipient` lexicalFilter** — ограничить noun-seed одушевлёнными (`wife/husband/friend/son/dog/cat/child`), как уже сделано для `en:pronouns`. Один JSON-файл, без кода.
6. **[content, малое, низкий приоритет] `en:tense.contrast`** — унифицировать объект (число/артикль) across трёх примеров Simple/Continuous/Perfect, чтобы контраст был только в форме глагола.
7. **[web, не связано с en-ru, не регрессия] Раздокументированный разрыв `.change-before`/`.change-after`** на 2 pl-таблицах (`kotlin-parity-matrix.spec.ts`) — существует с ADR-22, подтверждён повторно; можно закрыть отдельным тикетом вне en-ru лейна.
8. **[документация] После фикса №1 обновить `AI/architecture.md`/новую ADR** — зафиксировать генерализацию `CoursePack` как структурное решение (следующий свободный номер после ADR-35).

Все пункты — небольшие, укладываются в отдельные лейны (core/web/content), ни один не требует правки pl-ru golden-фикстур.
