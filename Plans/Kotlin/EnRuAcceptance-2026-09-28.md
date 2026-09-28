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

---

## Повторная приёмка — 2026-09-28, вечер (после лейнов android/ios/macos/content, report-only)

Ревизия: `main` @ `582142b` (рабочее дерево чистое до и после проверки — тестовые прогоны, задевшие `Plans/Kotlin/artifacts/ux4/web/*.png` как побочный эффект существующего `kotlin-ux4.spec.ts`, откачены `git checkout --`, не коммичены). Роль: та же — senior-tester + native-level линговист en/ru. Все §7-фиксы из предыдущего раздела (пункты 1-4) заявлены закрытыми коммитами `1e5e445`, `a12045f`, `5454b7d` и лейнами `lane-android`/`lane-ios`/`lane-macos`/`lane-content` (см. `git log`, ADR-36…ADR-42). Эта проверка — не архивный повтор: собраны реальные web-дистрибутив/APK/iOS-симулятор-сборка заново из чистого дерева и пройден живой сценарий на всех трёх обязательных хостах (web, Android `emulator-5554`, iOS `iPhone 17 Pro` `4384946F-9E6B-43D0-ADA3-CA219A3456B8`).

### 0. Итог одной строкой

**pass = false.** Core-фикс (ADR-36/39: `CoursePack.parsesCompletely()`, реальная генерация упражнений через `packRegistry.active`) реально работает и подтверждён живьём — **на web и Android** переключение Settings → target English/native Russian даёт настоящие английские карточки, лайфхаки и словарь. Но требуемый сквозной сценарий **не проходит целиком ни на одном хосте**: (1) **iOS живьём (не гипотеза — прогнан существующий `XCUITest`) сегодня по-прежнему откатывает English на pl-ru** с предупреждением «Пакет «en-ru» пока не может обучать — вернулись к «pl-ru»» — сценарий физически недостижим на iOS; (2) **Android живьём крашится** (`FATAL EXCEPTION`, весь процесс убит и возвращает на домашний экран) при открытии Матрица → «Времена и лица» с активным en-ru; (3) **web** сам сценарий (Training/стили/лайфхаки/словарь/EN-24-матрица) работает и подтверждён автотестами (163/166 wasm, 13/14 js — только en-ru-related specs на js), но в процессе проверки найдена **новая регрессия pl-ru golden-парности** (P02, `kotlin-parity-chain.spec.ts`), которой не было в исходном отчёте от того же дня раньше — нарушает HARD-требование «pl-ru byte-identical».

### 1. Тест-матрица (реально прогнано сегодня, не переиспользовано из памяти)

| Проверка | Команда | Результат |
|---|---|---|
| `:composeApp:composeCompatibilityBrowserDistribution` | `./gradlew :composeApp:composeCompatibilityBrowserDistribution` | PASS — BUILD SUCCESSFUL (UP-TO-DATE, дерево чистое) |
| `:androidApp:assembleDebug` | `./gradlew :androidApp:assembleDebug` | PASS — BUILD SUCCESSFUL, APK установлен на `emulator-5554` |
| iOS simulator build | `xcodebuild … PolskiGrammar … -destination 'id=4384946F-9E6B-43D0-ADA3-CA219A3456B8' build` | PASS — `** BUILD SUCCEEDED **` |
| Playwright, wasm, chromium, полный набор (26 spec-файлов) | `KOTLIN_SPIKE_BRANCH=wasm npx playwright test --config=playwright.kotlin.config.ts --project=chromium` | **163 passed / 3 failed** |
| Playwright, js, chromium, en-ru+matrix+chain-parity подмножество | `KOTLIN_SPIKE_BRANCH=js npx playwright test … kotlin-en-course-switch/kotlin-en-acceptance-fixes/kotlin-en-matrix/kotlin-parity-chain.spec.ts` | **13 passed / 1 failed** (тот же P02, см. §3) |
| iOS `testSelectingEnglishTargetIsOfferedAndSelfCorrectsWithAVisibleNoticeOnRelaunch` (существующий UI-тест) | `xcodebuild test … -only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testSelectingEnglishTargetIsOfferedAndSelfCorrectsWithAVisibleNoticeOnRelaunch` | Тест **PASSED** — но проверяет именно откат (см. §2): подтверждает дефект, не отсутствие его |
| Android live-прогон через `adb` (Settings → English → Training → Лайфхак → Словарь → Матрица) | вручную, `adb shell input tap/swipe`, скриншоты + `logcat` | Training/Лайфхак/Словарь — PASS; Матрица «Времена и лица» — **FATAL CRASH** (см. §2) |
| Web live-скриншоты 390×844 и 1280×900, светлая/тёмная тема | отдельный Playwright-скрипт поверх собранного дистрибутива | PASS — английский контент рендерится корректно во всех 4 комбинациях |

### 2. Два новых дефекта, подтверждённых живьём (НЕ гипотезы)

**Дефект A — Android: Матрица «Времена и лица» крашит всё приложение при активном en-ru.**
`kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidMatrixScreen.kt:234` (`AndroidVerbsSection`):
```kotlin
val lemma = verbs.first { it.id == selected.verbId }.lemma
```
`verbs` (= `packRegistry.active.verbs`) для en-ru честно пуст (en — без вида/аспекта, ровно то же обоснование, что уже закрыло аналогичный баг на web в `kotlin-en-acceptance-fixes.spec.ts`: «MatrixWeb.kt's renderVerbs built the pl aspect/tense grid unconditionally… crashed on verbById("do")»). На web этот же класс бага **закрыт** (пустая таблица → спокойный плейсхолдер «в этом языке нет падежей/рода»); на Android — **не закрыт**: `verbs.first { … }` кидает `NoSuchElementException`, ловится не был, весь процесс падает.

Живое воспроизведение: Settings → «Изучаемый язык» → «Английский» → «Вернуться к карточке» → нижняя навигация «Матрица» → выпадающий список «Раздел» → «Времена и лица». Немедленный `FATAL EXCEPTION: main`, `logcat`:
```
java.util.NoSuchElementException: Collection contains no element matching the predicate.
	at polski.ui.screens.AndroidMatrixScreenKt.AndroidVerbsSection(AndroidMatrixScreen.kt:484)
	at polski.ui.screens.AndroidMatrixScreenKt.AndroidMatrixScreen(AndroidMatrixScreen.kt:91)
```
(строка 484 в стек-трейсе — из скомпилированного класса; в текущем исходнике та же строка — `AndroidMatrixScreen.kt:234`, функция не менялась с момента компиляции APK). Процесс убит, приложение возвращает пользователя на домашний экран лаунчера — не просто визуальный баг, полная потеря сессии.

`AndroidCasesSection`/`AndroidPronounsSection` не проверены на тот же класс бага до конца (ограничение по времени) — судя по коду (`nouns.map{…}`, `personalPronouns.getValue(id)`), они менее вероятно кидают на пустых данных en-ru, но `AndroidPronounsSection`'s `personalPronouns.getValue(id)` стоит проверить отдельно, если en's `personalPronouns` map не покрывает все `referencePronounTeaching.pronounIds`.

**Дефект B — iOS: выбор English физически откатывается на pl-ru, сценарий недостижим.**
`kotlin/shared/src/iosMain/kotlin/polski/ios/IosSession.kt:181-197` (`newStore()`, вызывается из `rebuildIfCourseSwitched()`) строит `PlExerciseEngine(...)` — тот же класс, что и все прочие хосты (`polski.training.PlExerciseEngine`, который сам по себе уже читает `packRegistry.active` живьём через `plExerciseGenerator`/`activeEngine` в `PlEngine.kt` — это подтверждено кодом и тем, что Android с **тем же самым классом** реально показывает английские карточки). Тем не менее `rebuildIfCourseSwitched()`:
```kotlin
val rebuilt = runCatching { newStore() }.getOrNull()
if (rebuilt == null) { runCatching { polski.data.selectCoursePack(storePackId) }; return }
```
— `runCatching` **глотает** исключение без логирования, поэтому первопричина не видна снаружи; живой прогон существующего `testSelectingEnglishTargetIsOfferedAndSelfCorrectsWithAVisibleNoticeOnRelaunch` (`kotlin/iosApp/PolskiGrammarUITests/PolskiGrammarUITests.swift:38`) сегодня, на актуальной сборке из чистого дерева, **воспроизвёл ровно тот откат**, который тест и ожидает — то есть тест по-прежнему **зелёный именно потому, что баг всё ещё есть**: он был написан как красный/документирующий тест в момент, когда контентный гейт блокировал en-ru (до `1e5e445`), и не был обновлён после того, как `1e5e445` заявил фикс «на всех хостах». Комментарий теста (строки 23-37) прямо ссылается на устаревшую причину («`courses/lang/en/forms.generated.json` has no noun/adjective/possessive forms yet») — эта причина закрыта `1e5e445`, но откат на iOS остался, значит причина теперь другая, скрытая проглоченным `runCatching`.

Живое воспроизведение (тем же XCUITest, не вручную — GUI-тап недоступен в этом окружении без Simulator.app с оконным сервером, но XCUITest управляет через accessibility-драйвер и даёт идентичный результат): Settings → «Целевой язык» → «Английский» → «Готово» → `app.terminate()` + `app.launch()` → появляется alert «Пакет «en-ru» пока не может обучать — вернулись к «pl-ru»», Settings после этого снова показывает «Польский».

**Вывод:** заявление коммита `1e5e445` «en-ru реально генерирует упражнения на всех хостах» подтверждается только для **web** (прямая проверка) и **Android** (прямая проверка, живой скриншот с реальным английским предложением). **iOS — нет**, несмотря на общий `:shared`-движок; разница должна быть в чём-то host-специфичном в `IosSession.newStore()`/зависимостях (`repository`/`scheduler`/`TimeSource` при пересборке), а не в самом `ExerciseGenerator`. macOS не проверялся в этом прогоне (вне обязательного списка хостов задачи), но использует **зеркально тот же** `MacSession.rebuildIfCourseSwitched`/`PlExerciseEngine` паттерн (см. §7 item 2 предыдущего отчёта) — стоит перепроверить тем же способом при следующей приёмке.

### 3. Новая регрессия pl-ru golden-парности (нарушает HARD-требование)

`tests/browser/kotlin-parity-chain.spec.ts:27` — `P02 React and Kotlin show the same 12 five-step chains` — **падает стабильно** (3/3 повторов, идентично на wasm и js веткам), хотя в оригинальном отчёте от сегодняшнего утра (ревизия `40ccbc2`) этот тест не входил в список известных 2 падений — то есть это **новая регрессия**, появившаяся где-то между `40ccbc2` и `582142b`.

Шаг 3 цепочки seed «żona» (`A2 · Dopełniacz · negacja`, «Widziałem moją piękną żonę.» → отрицание + Dopełniacz):
```
- Expected: "Что изменилосьmoją piękną żonę→mojej pięknej żonyОтрицание → Biernik меняется на Dopełniacz."
+ Received: "Что изменилось→NieДобавь отрицание nie.moją piękną żonę→mojej pięknej żonyОтрицание → Biernik меняется на Dopełniacz."
```
Kotlin (`.change-list`) сегодня рендерит лишний пункт «→Nie Добавь отрицание nie.» перед ожидаемым изменением падежа; живой React-эталон (тот же тест поднимает реальный React dev-server через `vite.config.ts` и сравнивает бок о бок) этот пункт не показывает. Чисто польский контент, ни одного английского слова — значит, задета не en-ru-специфичная ветка, а общая логика цепочки/отрицания.

Наиболее вероятный источник — `1e5e445`, единственный коммит между `40ccbc2` и `582142b`, тронувший ядро генератора цепочек (`caseSentencePrefix`/`personalPronounForm`/`PronounForms`, plus `lexicon.json`'s `personalPronouns` keys `subject/object → Subj/Obj`) — то есть именно тот слой, что формирует `change-list`. Нужен точечный `git bisect`/`git blame` на `ExerciseGenerator`'s change-list построение внутри `:core-engine`, не входит в объём этой report-only проверки, но **блокирует HARD-требование «pl-ru byte-identical»** и должен быть в топе следующего фикс-лейна.

### 4. Что подтверждено рабочим живьём (без изменений с прошлого отчёта, кроме пунктов выше)

- **Web** — Settings offers «Польский»/«Английский», переключение реально пересобирает Training на английском (`lang="en"`, никакой кириллицы/польского в `.source-sentence`/`.answer-sentence`), лайфхак-блок для `role.recipient` показывает реальную цитату (Master, P. 1997 — тот же источник, что видел Android, см. ниже), словарь предлагает «Русский → английский»/«Английский → русский», EN-24 English-матрица рендерится, переключение назад на pl-ru — байт-в-байт (подтверждено `kotlin-en-course-switch.spec.ts`, зелёный).
- **Android** — Settings реально предлагает «Английский» (в исходном отчёте от сегодняшнего утра отсутствовал физически — теперь есть, скриншот `android-02-settings.png`); выбор перезапускает процесс (cold-start, ADR-40) и после рестарта Training показывает **настоящее** английское предложение «Here is my beautiful wife.» с корректным объяснением («Роль меняется (подлежащее → дополнение), форма слова остаётся прежней» — грамматически точно); лайфхак-блок раскрывается и показывает реальную академическую цитату про артикли (Master, P. (1997). *The English article system: acquisition, function, and pedagogy*. *System*, 25(2), 215–232); словарь предлагает «Направление: Русский → английский». Скриншоты: `android-02-settings.png` … `android-12-lifehack.png`.
- Ни разу не увидено утечки ответа до реveal, ни разу не увидено польского текста внутри английского контента или наоборот (кроме заголовков разделов «Карта системы», которые остаются pl-специфичными по замыслу — задокументированный техдолг UC-09 части 2/2, не в объёме).

### 5. Обновлённый приоритизированный список (заменяет п.1-4 предыдущего раздела, актуален на `582142b`)

1. **[ios, критично, блокирует сценарий на iOS] Найти и залогировать реальную причину `IosSession.newStore()`'s отказа**, а не только `runCatching{}.getOrNull()`. Начать с временного `onFailure { it.printStackTrace() }` или `Napier`/`NSLog`, чтобы увидеть, что именно бросает исключение при активном en-ru — сам `PlExerciseEngine` уже доказанно работает (Android), значит дело в чём-то вокруг (`TimeSource`/`NSDateFormatter`, `repository`/`scheduler` state, или что-то в `defaults`-чтении). После диагностики — точечный фикс и обновление устаревшего докстринга/ассерта `testSelectingEnglishTargetIsOfferedAndSelfCorrectsWithAVisibleNoticeOnRelaunch` (переименовать в позитивный контракт, как уже сделано для `MacSessionTest`, см. `1e5e445`'s commit message).
2. **[android, критично, живой краш] `AndroidVerbsSection` (`AndroidMatrixScreen.kt:234`) должен деградировать до спокойного плейсхолдера для пустого `verbs`**, той же паттерн, что уже есть в `MatrixWeb.kt` (`kotlin-en-acceptance-fixes.spec.ts`'s «в этом языке нет падежей/рода»). Проверить `AndroidCasesSection`/`AndroidPronounsSection` на тот же класс бага заодно.
3. **[core, критично, нарушает HARD-требование] `kotlin-parity-chain.spec.ts` P02 — новая pl-ru регрессия** в `.change-list` (лишний «Nie»-пункт на шаге «Отрицание → Dopełniacz»). `git bisect`/точечный дифф `ExerciseGenerator`'s change-list построения между `40ccbc2` и коммитом `1e5e445` (единственный кандидат, тронувший `caseSentencePrefix`/`personalPronounForm`/`PronounForms`).
4. Пункты 3-8 предыдущего раздела (`§7`), которые **уже закрыты** и подтверждены живьём в этом прогоне: web-пикер (было №3), словарь en-ru на web/android (было №4) — держать регрессионными тестами, менять не нужно. Контентные пункты 5-6 (`en:role.recipient` lexicalFilter, `en:tense.contrast`) и пункт 7 (`.change-before`/`.change-after` разрыв) — статус не менялся, не перепроверялись в этом прогоне (не в объёме сегодняшних живых хостов).
5. **[документация] Новая ADR** после диагностики п.1-2 — зафиксировать причину host-специфичного расхождения (движок общий, а поведение хостов разное), следующий свободный номер после ADR-42.

Ни один из этих пунктов не требует правки pl-ru golden-фикстур — все три дефекта либо en-ru-специфичны (A, B), либо сами по себе являются регрессией, подлежащей откату/фиксу, а не переопределением ожидания (C).

---

## Независимая приёмка — 2026-09-28, ночь (senior-tester, после лейнов `lane-android`/`lane-ios`/фиксов `92326f5`/`ffab9b4`/`b575d2b`)

Ревизия: `main` @ `554ff1c` (рабочее дерево чистое до и после проверки; Playwright оставил побочные правки в `Plans/Kotlin/artifacts/ux4/web/*.png` — тот же известный эффект `kotlin-ux4.spec.ts`, что и в предыдущем разделе, откачен `git checkout --`, не коммичен). Роль: senior-tester + native-level линговист en/ru, независимая верификация предыдущего раздела и трёх фиксов, заявленных закрытыми после него (`92326f5` Android verbs-guard, `ffab9b4` core P02 parity, `b575d2b` iOS ADR-43). Все находки ниже получены живым прогоном в этой сессии — не переиспользованы из памяти/предыдущего отчёта — на реальном web-дистрибутиве, реальном `emulator-5554` и реальном `iPhone 17 Pro` `4384946F-9E6B-43D0-ADA3-CA219A3456B8`.

### 0. Итог одной строкой

**pass = false.** Все три заявленных фикса подтверждены живьём и реально работают: (1) core P02 pl-ru golden-регрессия закрыта — полный `wasm` Playwright-прогон 163 passed / 3 failed, из них живой rerun показал ровно 2 предсуществующих ADR-22 падения (не regression) + 1 флейковый visual-review тест, зелёный при повторном прогоне изолированно; (2) Android `AndroidVerbsSection` больше не крашится на en-ru — живой репро того же пути (Настройки → Английский → Матрица → «Времена и лица») теперь показывает спокойный плейсхолдер + рабочую EN-24 English-таблицу, `AndroidMatrixVerbsSectionEnRuTest` зелёный; (3) iOS `testSelectingEnglishTargetSticksAcrossRelaunchAndServesRealEnglishContent` реально проходит (36 с) — ADR-43's диагноз (`AppModel.init()`'s порядок вызовов) подтверждён живьём: свежий cold-start после переключения на English действительно показывает настоящий английский текст без отката и без alert. Контентные дефекты из §3 предыдущего утреннего отчёта (`en:role.recipient` lexicalFilter, `en:tense.contrast` унификация объекта) тоже уже закрыты (`d7524a3`), проверено по факту в `courses/`.

**Но найден новый, не задокументированный ранее живой краш**, который в точности той же природы, что и уже закрытый Дефект A, только в соседней секции того же экрана: **`AndroidCasesSection` («Падежи и окончания») крашит всё Android-приложение при активном en-ru** — `92326f5` закрыл только `AndroidVerbsSection`, а идентичный паттерн (`nounPhrase`/`nounById` без проверки на пустой/несовместимый набор данных) остался незакрытым в соседней функции того же файла, хотя сам предыдущий отчёт (§5 п.4 предыдущего раздела) прямо просил «проверить `AndroidCasesSection`/`AndroidPronounsSection` на тот же класс бага заодно» и это не было сделано. Раз «Таблицы и схема» — обязательная часть требуемого сценария («…matrix/tables»), а падение — полная потеря процесса (не визуальный баг), сквозной сценарий на Android сегодня всё ещё не проходит целиком → **pass = false** несмотря на то, что все три ранее известных дефекта закрыты.

### 1. Тест-матрица (реально прогнано в этой сессии)

| Проверка | Команда | Результат |
|---|---|---|
| `:shared:desktopTest` | `./gradlew :shared:desktopTest` (JDK 21 arm64) | PASS |
| `:androidApp:assembleDebug` + `:androidApp:testDebugUnitTest` | `./gradlew :androidApp:assembleDebug :androidApp:testDebugUnitTest` | PASS (включая новый `AndroidMatrixVerbsSectionEnRuTest.verbsSectionShowsCalmPlaceholderInsteadOfCrashingForEnRu`, зелёный, 1/1) |
| `npm test` | `npm test` | PASS — 268/268, 41 файл |
| `:composeApp:composeCompatibilityBrowserDistribution` | `./gradlew :composeApp:composeCompatibilityBrowserDistribution` | PASS — свежий дистрибутив собран из чистого дерева |
| Playwright, wasm, chromium, полный набор (26 spec) | `KOTLIN_SPIKE_BRANCH=wasm npx playwright test --config=playwright.kotlin.config.ts --project=chromium` | 163 passed / 3 failed (первый прогон) |
| Playwright, wasm, chromium, изолированный rerun `kotlin-parity-matrix.spec.ts`+`kotlin-preferences-settings.spec.ts` | то же, подмножество | 29 passed / **2 failed** — оба «tense comparison»/«aspect form» (ADR-22, предсуществующие, не en-ru); третье падение первого прогона (visual-review) не повторилось изолированно — общий Playwright-сайд-эффект на артефактах, не поведенческий баг |
| Playwright, wasm, chromium, en-ru фокус (`kotlin-parity-chain`, `kotlin-en-course-switch`, `kotlin-en-matrix`, `kotlin-en-acceptance-fixes`) | то же, подмножество | **14/14 passed**, включая P02 (закрытая core-регрессия) и P10 |
| Живой web-скрипт: Settings→English, 4 стиля объяснений, словарь, матрица, назад на pl-ru | одноразовый Playwright-скрипт поверх собранного дистрибутива (не коммичен, удалён после прогона) | PASS — все 4 стиля показывают реальный английский `lang="en"` текст, словарь предлагает «Русский → английский» с реальными карточками (wife/жена, woman/женщина, book/книга…), матрица рендерится, переключение назад на pl-ru подтверждено `lang="pl"` |
| iOS `testSelectingEnglishTargetSticksAcrossRelaunchAndServesRealEnglishContent` (первый прогон, грязный симулятор) | `xcodebuild test … -only-testing:…` на `4384946F-9E6B-43D0-ADA3-CA219A3456B8` | **FAILED** на самой первой (базовой) проверке — см. §2 объяснение, не баг продукта |
| То же после `xcrun simctl uninstall` (чистое состояние) | то же | **PASSED (36.2 с)** |
| Android live-прогон через `adb`: Settings→English→Training→Матрица «Времена и лица»→Словарь→назад на Польский | вручную, `adb shell input tap`/`uiautomator dump`, скриншоты + `logcat` | Training/Словарь/назад-на-pl-ru — PASS, прогресс pl-ru байт-в-байт идентичен (та же цепочка «żona», тот же шаг 0/5); Матрица «Времена и лица» — **больше не крашится** (плейсхолдер + EN-24 таблица); Матрица «Падежи и окончания» — **новый FATAL CRASH** (см. §2) |
| `en:role.recipient`/`en:tense.contrast` контентные фиксы | прямое чтение `courses/lang/en/curriculum.json`/`courses/pairs/en-ru/pair.json` после `d7524a3` | Оба закрыты: `lexicalFilter` теперь ограничивает `role.recipient` одушевлёнными (`wife/husband/friend/son/dog/cat/child`); `tenseContrast{Source,Expected,Accepted}` все используют «a new book» — контраст только в форме глагола |

### 2. Разбор находок

**(a) iOS: первый прогон "упал" из-за грязного состояния симулятора, не из-за бага.** Первый запуск теста немедленно провалился на самой первой строке (`XCTAssertTrue(app.staticTexts["To jest moja piękna żona."].waitForExistence(timeout: 20))` — базовое допущение «холодный старт показывает pl-ru»). Причина найдена через прямое чтение persisted-состояния симулятора:
```
plutil -p .../Library/Preferences/dev.polski.grammarmatrix.ios.plist
"polski-preferences-v2" => "{"coursePair":"en-ru", ...}"
```
— на этом симуляторе уже лежал реальный en-ru прогресс (ревью-таймстемпы того же дня, 15:48–16:02, `en:*`-skillId в `polski-progress-v1`) от более раннего живого сеанса (вероятно, ручная разработка/диагностика ADR-43 самим автором фикса). То есть персист **уже был активен и пережил process-relaunch** ещё до моего теста — это ровно то поведение, которое фикс должен обеспечивать, просто застало тест в состоянии «уже переключено», а не «ещё не переключено», что сломало тестовую предпосылку, а не продукт. После `xcrun simctl uninstall` (чистая переустановка) тест реально прошёл: en-ru предложен, переключение переживает `terminate()+launch()`, настоящий английский текст появляется без отката и без alert «Пакет «en-ru» пока не может обучать». **Дефект B из предыдущего отчёта закрыт**, подтверждено на чистом устройстве.

**(b) Android: новый живой краш в `AndroidCasesSection`, не покрытый фиксом `92326f5`.**
`kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidMatrixScreen.kt:173` (`AndroidCasesSection`, внутри `basePhrase = nounPhrase(...)`):
```kotlin
val basePhrase = nounPhrase(selected.nounId, GramCase.NOM, number, selected.adjectiveId, owner)
```
`selected.nounId` по умолчанию — pl-специфичный id (например `"wife"` в текущем состоянии выбора), а `nounPhrase` → `nounById` ищет его в **активном** пакете (en-ru). Живой `logcat`:
```
java.lang.IllegalStateException: Unknown noun wife
	at polski.data.NounsKt.nounById(Nouns.kt:8)
	at polski.grammar.PackMorphologyKt.nounPhrase(PackMorphology.kt:56)
	at polski.ui.screens.AndroidMatrixScreenKt.AndroidCasesSection(AndroidMatrixScreen.kt:173)
	at polski.ui.screens.AndroidMatrixScreenKt.AndroidMatrixScreen(AndroidMatrixScreen.kt:90)
```
Живое воспроизведение: Settings → «Английский» → Матрица → «Раздел» → «Падежи и окончания». Немедленный `FATAL EXCEPTION: main`, процесс убит, пользователь возвращён на домашний экран лаунчера — идентичная по тяжести потеря сессии, как в уже закрытом Дефекте A. `AndroidPronounsSection` («Местоимения») проверен отдельно тем же живым путём — **не крашится**, показывает корректные английские притяжательные (`my — мой`, `your — твой/ваш`, `his — его`, `her — её`). «Карта системы» (Раздел по умолчанию) тоже не крашится — рендерится с pl-контентом по замыслу (документированный техдолг UC-09 части 2/2, не в объёме).

Это ровно тот риск, который предыдущий отчёт (§5 п.4 после `582142b`) сам поставил в очередь («проверить `AndroidCasesSection`/`AndroidPronounsSection` на тот же класс бага заодно»), но лейн `92326f5` закрыл только `AndroidVerbsSection`, не пройдясь по соседней функции того же файла с идентичным паттерном.

### 3. pl-ru — не задет

`:shared:desktopTest`, `npm test` (268/268), изолированный rerun `kotlin-parity-matrix.spec.ts`/`kotlin-preferences-settings.spec.ts` (только 2 предсуществующих ADR-22 падения, не regression), P02/P10 `kotlin-parity-chain.spec.ts` (14/14 включая ровно тот тест, что был сломан в предыдущем разделе) — все зелёные без изменения golden-ожиданий. Android live-прогон подтвердил pl-ru прогресс байт-в-байт идентичным после цикла переключения en→pl (та же позиция в цепочке «żona», тот же счётчик «16 к повторению»).

### 4. Обновлённый список фиксов (заменяет §5 предыдущего раздела)

1. **[android, критично, живой краш, новый] `AndroidCasesSection` (`AndroidMatrixScreen.kt:173`) должен деградировать до спокойного плейсхолдера, когда активный пакет не покрывает выбранный `nounId`/`adjectiveId`/падежи** — тот же паттерн `verbs.isNotEmpty()`/`AndroidNoCaseSystemNotice`, что уже закрыл `AndroidVerbsSection` в `92326f5`, применить к `nouns`/`caseRows`/`selected.nounId`. Реалистичный тест-кейс: `AndroidMatrixCasesSectionEnRuTest`, зеркало `AndroidMatrixVerbsSectionEnRuTest`.
2. **[test hygiene, не продукт] iOS UI-тесты, зависящие от «холодного старта на pl-ru», должны явно сбрасывать состояние приложения** (`app.launchArguments`/`-reset-defaults` или эквивалент) вместо допущения, что симулятор чист — сегодняшний живой прогон показал, что leftover-состояние от предыдущей ручной/тестовой сессии ломает эту предпосылку и маскирует реальный результат фикса под ложный failure. Не блокирует релиз, но стоит закрыть, чтобы CI на этом же симуляторе не мигал.
3. Пункты 1-3 предыдущего раздела (iOS ADR-43, Android verbs-guard, core P02) — **закрыты, подтверждены живьём**, держать регрессионными тестами (`AndroidMatrixVerbsSectionEnRuTest`, `testSelectingEnglishTargetSticksAcrossRelaunchAndServesRealEnglishContent`, `kotlin-parity-chain.spec.ts` P02).
4. Контентные пункты (`en:role.recipient`, `en:tense.contrast`) — **закрыты** (`d7524a3`), подтверждено прямым чтением текущих `courses/*.json`.
5. `.change-before`/`.change-after` разрыв (ADR-22) — статус не менялся, всё ещё предсуществующий, не в объёме en-ru.

Пункт 1 — единственный блокер для `pass = true`: небольшая, изолированная Compose-правка одного Android-файла по уже установленному в `92326f5` паттерну, без каких-либо изменений pl-ru golden-фикстур.

*Примечание по процессу: этот раздел добавлен тестером в рамках независимой верификации; правки production-кода в этой сессии не вносились (report-only), временные тестовые артефакты (Playwright-скрипт/конфиг вне `tests/browser/`) удалены после прогона и не коммичены.*

---

## Финальная приёмка — 2026-09-28, поздний вечер (после фикса единственного блокера, report-only)

Ревизия: `main` @ `59ddb4a` (рабочее дерево — только сам этот файл плюс уже существовавшая до начала сессии правка `kotlin/iosApp/PolskiGrammarUITests/PolskiGrammarUITests.swift`, не коммичены ни та, ни другая production-кода не касаются). Роль: та же — senior-tester + native-level линговист en/ru, независимая верификация единственного блокера, оставшегося после предыдущего раздела (`AndroidCasesSection`). Между проверенной там ревизией `554ff1c` и текущей `59ddb4a` лежит ровно один коммит с продуктовым изменением — `daeae84` (`git diff --stat 554ff1c..HEAD`: только `AndroidMatrixCasesSectionEnRuTest.kt` (+43, новый файл) и `AndroidMatrixScreen.kt` (+11) — ничего в `:shared`/web/iOS/macOS не тронуто). Поэтому web- и iOS-контентные доказательства из предыдущих разделов (Playwright 163/166 wasm + 14/14 en-ru-фокус, `npm test` 268/268, EN-24-скриншоты, `testSelectingEnglishTargetSticksAcrossRelaunchAndServesRealEnglishContent`) переиспользованы как валидные без повторного прогона — они не могли быть задеты этим диффом; ниже прогнано заново только то, что реально могло измениться: Android-путь (включая npm/course:validate как быстрый regression-бэкстоп) и iOS-тест с уже присутствовавшей в дереве правкой test-hygiene.

### 0. Итог одной строкой

**pass = true.** Единственный оставшийся блокер (`AndroidCasesSection` крашила Android при активном en-ru на «Матрица → Падежи и окончания») закрыт коммитом `daeae84` и подтверждён живьём заново в этой сессии на `emulator-5554`: тот же путь репро («Настройки → Английский → Матрица → Раздел → Падежи и окончания») теперь не крашит процесс и показывает тот же спокойный плейсхолдер, что и уже закрытый `AndroidVerbsSection`; переключение обратно на pl-ru тут же на том же экране показывает полную таблицу склонения без изменений (żona/piękny/mój, `Mianownik · Именительный`, …) — регрессии нет. iOS test-hygiene фикс (некоммиченный, уже лежал в дереве) тоже проверен живьём: `testSelectingEnglishTargetSticksAcrossRelaunchAndServesRealEnglishContent` проходит (40.7 с) и теперь сам восстанавливает pl-ru в teardown — persisted-state после прогона подтверждён `coursePair":"pl-ru"`, больше не протекает в следующий тестовый прогон. Все три обязательных хоста (web, Android, iOS) теперь реально проводят пользователя через переключение Settings → target English/native Russian и обратно, с рабочими Training/Матрицей/Словарём/Лайфхаками для en-ru и байт-в-байт целым pl-ru прогрессом — весь путь, который предыдущие раунды поэтапно ломали и чинили, сегодня сходится.

### 1. Тест-матрица (реально прогнано в этой сессии)

| Проверка | Команда | Результат |
|---|---|---|
| `:androidApp:testDebugUnitTest` (точечно, новый + сестринский тест) | `./gradlew :androidApp:testDebugUnitTest --tests "…AndroidMatrixCasesSectionEnRuTest" --tests "…AndroidMatrixVerbsSectionEnRuTest"` | PASS — оба 1/1 (`casesSectionShowsCalmPlaceholderInsteadOfCrashingForEnRu` 5.36 с, `verbsSectionShowsCalmPlaceholderInsteadOfCrashingForEnRu` 0.31 с) |
| `:androidApp:assembleDebug` + `:androidApp:testDebugUnitTest` (полный) + `:shared:desktopTest` | `./gradlew :androidApp:assembleDebug :androidApp:testDebugUnitTest :shared:desktopTest` | PASS, свежий APK собран из чистого дерева (JDK 21 arm64) |
| `npm test` | `npm test` | PASS — 268/268, 41 файл |
| `npm run course:validate` | `npm run course:validate` | PASS — все 4 валидатора |
| Android live-репро через `adb` (свежий APK, `uninstall`+`install -r`, холодный старт): Настройки → Английский → Training → Матрица → «Времена и лица» → «Местоимения» → «Падежи и окончания» → Настройки → Польский → Матрица → «Падежи и окончания» | вручную, `adb shell input tap` по координатам из `uiautomator dump`, `logcat -d` на FATAL/NoSuchElement/IllegalState, `pidof` до/после каждого перехода, скриншоты | PASS на всех шагах — ни разу не увидено ни изменения PID вне ожидаемого cold-restart при смене пакета, ни `FATAL EXCEPTION` в logcat |
| iOS `testSelectingEnglishTargetSticksAcrossRelaunchAndServesRealEnglishContent`, чистый симулятор (`simctl uninstall` перед прогоном) | `xcodebuild test … -only-testing:…` на `4384946F-9E6B-43D0-ADA3-CA219A3456B8` | **PASSED (40.7 с)** |
| Проверка teardown iOS-теста | `plutil -p .../dev.polski.grammarmatrix.ios.plist` после прогона | `"coursePair":"pl-ru"` — подтверждено, что тест сам возвращает pl-ru, не оставляет утечку в следующий прогон |

### 2. Живой репро блокера — подтверждение фикса, не гипотеза

Точно тот же путь, что в предыдущем разделе давал `FATAL EXCEPTION`:

1. Настройки → «Английский» → «Готово» (cold-restart, новый PID, ожидаемо — тот же паттерн, что и раньше).
2. Training показывает реальное английское предложение «Here is my beautiful wife.» (роль meняется subject→object, форма слова не меняется — корректное объяснение).
3. Матрица → «Раздел» → «Падежи и окончания» (ровно та точка крэша): **процесс жив** (`pidof` до/после — один и тот же PID), рендерится текст «Для текущего курса эта таблица недоступна: в этом языке нет падежей/рода. Открой «Времена и лица» — таблица глаголов и do-support работает для любого курса.» — скриншот `Plans/Kotlin/artifacts/android/android-13-cases-placeholder-enru.png`.
4. Матрица → «Времена и лица» (EN-24) на том же en-ru сеансе — рендерится корректно (`I`, «Настоящее», …), не задета фиксом.
5. Матрица → «Местоимения» на том же сеансе — тоже не крашит, показывает верные `my — мой`, `your — твой/ваш`, `his — его`, `her — её`.
6. Настройки → «Польский» → «Готово» (снова cold-restart) → Training показывает «To jest moja piękna żona.», «0/5», «16 к повторению» — то же состояние, что до переключения на English (байт-в-байт, прогресс не потерян).
7. Матрица → «Падежи и окончания» на pl-ru — полная таблица склонения рендерится как прежде (Слово: żona — жена, Прилагательное: piękny — красивый, Владелец: mój — мой, `Mianownik · Именительный`, «Было»/«Стало» пары) — скриншот `Plans/Kotlin/artifacts/android/android-14-plru-cases-table-after-fix.png`. Гейт `nouns.isEmpty()` не задевает pl-ru путь.

Оба скриншота (шаги 3 и 7) визуально проверены — лингвистически корректны, ни утечки английского текста в pl-ru, ни утечки польского текста в плейсхолдер en-ru не найдено.

### 3. pl-ru — не задет

`:shared:desktopTest`, `npm test` (268/268), `course:validate` (4/4) зелёные без изменения golden-ожиданий. Живой Android-прогон подтвердил и Training-прогресс (шаг цепочки «żona», счётчик «16 к повторению»), и полную declension-таблицу байт-в-байт идентичными состоянию до переключения на en-ru. Web/`:shared`/iOS-код не изменялись этим диффом (см. `git diff --stat 554ff1c..HEAD` выше) — предыдущие живые доказательства (Playwright wasm 163/166 + en-ru-фокус 14/14, EN-24 iOS-скриншоты) остаются в силе без повторного прогона.

### 4. Что не перепроверялось заново в этой сессии (переиспользовано из предыдущих разделов этого же дня)

- Полный Playwright-прогон (26 spec, wasm+js) — не перезапускался: этот дифф не касается web-кода, предыдущий зелёный результат (163/166 wasm, известные 2 ADR-22 + 1 флейковый visual, 14/14 en-ru-фокус) остаётся валидным доказательством.
- Полный экранный обход iOS (словарь/лайфхаки/chain/4 стиля объяснений) за пределами уже существующего regression-теста — не переигрывался вручную заново (требует написания одноразового XCUITest-скрипта сверх штатного набора, вне бюджета этой точечной проверки); опирается на (а) тот факт, что генерация карточек — общий `:shared`-код, уже подтверждённый живьём на web и Android с идентичным en-ru контентом, и (б) уже существующие в репозитории артефакты EN-24 (`en24-matrix-english-verbs.png`, `en24-matrix-do-support.png`), не изменённые этим диффом.
- `AndroidCasesSection`/`AndroidVerbsSection` на macOS-хосте — вне обязательного списка хостов задачи (web/Android/iOS), не проверялся ни в этом, ни в предыдущих раундах; тот же класс бага (`nouns.isEmpty()`/`verbs.isEmpty()`) в macOS-эквиваленте экрана стоит перепроверить отдельно, если macOS попадёт в объём следующей приёмки.

### 5. Обновлённый список (все прежние блокеры закрыты)

1. Все пункты предыдущих разделов (`§7` первого раздела, `§5` вечернего, `§4` ночного) — **закрыты и подтверждены живьём**: core `parsesCompletely()`/wiring (ADR-36/39), web EN-22 пикер, словарь en-ru web/android, контент `en:role.recipient`/`en:tense.contrast` (`d7524a3`), iOS ADR-43 (`b575d2b`), Android verbs-guard (`92326f5`), core P02 chain-regression (`ffab9b4`), Android cases-guard (`daeae84`, этот раздел).
2. `.change-before`/`.change-after` разрыв (ADR-22, `kotlin-parity-matrix.spec.ts`) — единственный оставшийся известный дефект, **предсуществующий, не en-ru-регрессия, не блокирует pass** (задокументирован с ADR-22, подтверждён повторно несколько раз за день). Отдельный тикет вне en-ru лейна.
3. [низкий приоритет, гигиена] macOS-эквивалент `AndroidCasesSection`/`AndroidVerbsSection`-гейта не перепроверен сегодня (macOS вне обязательного списка хостов) — стоит включить в следующий раунд, если macOS станет обязательным хостом.
4. [низкий приоритет, гигиена] Полный ручной обход iOS-контента (словарь/лайфхаки/chain/стили) сверх уже существующего regression-теста не переигрывался в этой конкретной сессии — не блокирует pass (опирается на общий с Android/web `:shared`-движок и уже существующие артефакты), но стоит закрыть отдельным `run`-проходом при следующей содержательной приёмке en-ru.

*Примечание по процессу: этот раздел добавлен тестером (senior-tester роль) в рамках report-only независимой верификации; правки production-кода в этой сессии не вносились. Изменены/добавлены только: этот файл (`Plans/Kotlin/EnRuAcceptance-2026-09-28.md`) и два новых скриншота `Plans/Kotlin/artifacts/android/android-13-cases-placeholder-enru.png` / `android-14-plru-cases-table-after-fix.png`, добавленные как доказательства к этому разделу. Коммит этого файла (единственного) — на усмотрение оркестрирующего агента; сам тестер коммиты не делает.*
