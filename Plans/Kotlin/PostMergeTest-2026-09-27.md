# Тестирование после слияния: main 25c378c (2026-09-27)

Проверяемая ревизия: `main @ 25c378c`. В неё вошли web UX v5 и прогрев таблиц, а также слияние веток Android, iOS и macOS: раскрытие карточки-задания вниз, переворот всей панели словарной карточки, оценка свайпом, анимированное переключение вкладок и сворачиваемые блоки, переключатель «Анимации». Требования взяты из ADR-8 и ADR-9 в `AI/decisions.md`.
Роль: senior-tester, только отчёт. Рабочий код не менялся, тесты не правились, ничего не закоммичено.
Логи лежат в `/private/tmp/claude-501/fulltest/<platform>/`.

## Итог

| Платформа | Итог | Регрессии в продукте |
|---|---|---|
| Web (KMP JS/Wasm + React baseline) | FAIL | 2 (+1 процессная) |
| Android | FAIL | 1 |
| iOS | FAIL | 1 |
| macOS | PASS (частично) | 0, интерактивная проверка не выполнялась |

## Web

| Проверка | Результат | Цифры |
|---|---|---|
| Gradle: `:shared` и `:composeApp` для js/wasmJs/desktop + `composeCompatibilityBrowserDistribution` | PASS | shared 199/199 ×3; composeApp 22/22 js, 22/22 wasm, 54/54 desktop |
| `npm test` (vitest) | PASS | 35 файлов, 201/201 |
| `npm run typecheck` | PASS | без ошибок |
| `npm run course:validate` | PASS | — |
| `npm run course:inventory:check` | FAIL | 1/1 |
| `npm run course:inventory:decisions:check` | FAIL | та же причина |
| Playwright wasm/chromium | FAIL | 138/140 |
| Playwright js/chromium | PASS | 140/140 |
| Playwright wasm/firefox | FAIL | 132 pass / 5 fail / 3 skip |
| Playwright wasm/webkit | FAIL | 133 pass / 4 fail / 3 skip |
| Повтор P05 ×3 (wasm/chromium) | PASS | 3/3 |
| Повтор «narrow layout» ×3 (wasm/chromium) | FAIL | 2/3, overflow=70px при каждом падении |
| Повтор «Скрыть каталог» ×3 (wasm/webkit) | FAIL | 0/3 |
| Визуальная проверка: 390×844 touch и 1280×900 desktop, светлая и тёмная темы | PASS | 20 PNG, в итоговых состояниях дефектов нет |

## Android (emulator-5554, API 35 arm64)

| Проверка | Результат | Цифры |
|---|---|---|
| `:androidApp:testDebugUnitTest :androidApp:assembleDebug` | PASS | 54/54, 13 классов |
| Установка APK | PASS | — |
| Карточка-задание: раскрытие вниз по нажатию | PASS | — |
| Свайп вправо («Вспомнил») и влево («Повторить») | PASS | — |
| Словарь: переворот лицо→оборот, потом оборот→лицо | PASS | — |
| Словарь: третье нажатие снова переворачивает на оборот | **FAIL** | 3/3 воспроизведения |
| Словарь: переворот, раскрытие и оценка свайпом | PASS | — |
| Нижние вкладки | PASS | 4/4 |
| «Анимации» выкл.: Rive не загружается | PASS | в logcat 0 упоминаний `librive-android.so` |
| Поворот экрана при раскрытом ответе | PASS | — |
| force-stop и перезапуск: прогресс сохраняется | PASS | счётчики и «Мой словарь · 2» на месте |

## iOS (iPhone 17 Pro и iPad Pro 11 M5, iOS 26 sim)

| Проверка | Результат | Цифры |
|---|---|---|
| `:shared:iosSimulatorArm64Test` | PASS | 231/231 |
| Проверка, что проект Xcode не устарел (сравнение с `generate_project.rb`) | PASS | отличаются только UUID; сгенерированные файлы откачены |
| XCUITest, iPhone | FAIL | 27 тестов, 23 pass, 4 метода падают |
| XCUITest, iPad | FAIL | 27 тестов, 22 pass, 5 методов падают |
| Повторы на чистой установке (`simctl uninstall`) | MIXED | см. ниже |
| FlipRivePerfUITests | PASS | iPhone 849 с, iPad 650 с, укладываются в 20 мин |
| Проверка Swift-кода на соответствие D1–D5 | PASS | `rings.riv` полностью удалён |

## macOS (worktree lane-macos @ 25c378c)

| Проверка | Результат | Цифры |
|---|---|---|
| `:shared:macosArm64Test --rerun-tasks` | PASS | 211/211 |
| `xcodebuild build` | PASS | — |
| `xcodebuild test` | NOT_RUN | в схеме нет test target (`<Testables/>` пуст) |
| Запуск приложения больше чем на 20 с, чистое завершение | PASS | — |
| Crash-логи и DiagnosticReports | PASS | 0 |
| Скриншот экрана | PASS | окно отрисовано верно, ответ скрыт |
| Управление UI через osascript | NOT_RUN | «osascript is not allowed assistive access» |

## Регрессии в продукте

1. **iOS: «Анимации» не выключаются и выключение не сохраняется.** Тест `testAnimationsToggleDefaultsOnAndPersistsOffAcrossRelaunch`: после tap значение `"1"`, после перезапуска тоже `"1"`. Воспроизводится на iPhone 2/2 и на iPad. Kotlin-тест `IosPreferencesSessionTest` проходит.
   Где искать: `kotlin/iosApp/PolskiGrammar/PolskiGrammarApp.swift`. Это цепочка `setPreference` → `IosPreferencesSession` → `receivePreferences`, примерно строки 163–168, и get/set у Toggle, примерно строка 531.
   Что делать: пройти путь записи отладчиком и найти место, где пропадает значение: не вызывается `set`, `receivePreferences` не присваивает `model.preferences`, или get у Toggle читает другой ключ или копию.
2. **Android: словарная карточка перестаёт переворачиваться после одного полного цикла.** Причина: `VocabularySession.reveal()` при повторном вызове записывает то же `revealed = true`. `StateFlow` одинаковое значение заново не отдаёт, поэтому `LaunchedEffect(revealed, itemId)` не срабатывает, а нажатие на лицевую сторону ничего не делает.
   Где искать: `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidFlipCard.kt:343-376`; `kotlin/shared/src/commonMain/kotlin/polski/vocabulary/VocabularySession.kt:93-98`.
   Что делать: сделать так, чтобы нажатие на лицевую сторону при уже раскрытом ответе само ставило локальный `flipped = true`, например обработчиком на лицевой стороне в `AndroidFlipCard`. `reveal()` для этого не нужен.
3. **Web: горизонтальный overflow 70px на 320px в режиме «Цепочка».** Тест «narrow layout keeps the next training action visible» проходит 2/3: падает, когда стартовый экран — цепочка. У `ol` стоит `overflow-x:auto`, но у flex-предков нет `min-width:0`.
   Где искать: `kotlin/composeApp/src/webMain/resources/training.css`, `.chain-header ol` в `@media(max-width:600px)`, примерно строка 209. Появилось в 156cae8.
   Что делать: добавить `min-width:0` flex-родителю `.chain-header` и самому `ol`. Тест нестабилен из-за того, что не фиксирует режим; зафиксировать seed или режим, чтобы он проверял цепочку всегда.
4. **Web/WebKit, вероятная регрессия: после «Скрыть каталог» фокус не возвращается на кнопку, если до этого фокус был в `<select>`.** 0/3 на webkit, на chromium и firefox проходит. Safari — целевой браузер.
   Где искать: `kotlin/composeApp/src/webMain/kotlin/polski/ui/VocabularyWeb.kt`, обработчик в `renderCatalog`, примерно строки 283–292.
   Что делать: перед `inert` вызвать `activeElement.blur()`, затем `toggle.focus()`, а `inert` ставить после focus, например в следующем кадре. Проверить в настоящем Safari.
5. **Процесс: не обновлён `courses/pl-ru/source-inventory.json`.** Он устарел после добавления обработки ←/→ в `TrainingWebApp.kt`.
   Что делать: запустить `node scripts/inventory-course.mjs` и закоммитить результат.
6. **Документация (низкий приоритет):** `THIRD_PARTY/credits.md:23` всё ещё пишет, что `rings.riv` используется в Android и iOS. Файла нет ни на одной платформе. Строку нужно убрать или исправить.

## Дефекты тестов

- `tests/browser/kotlin-ux4.spec.ts:175` (UX4-11/12) на firefox: `isMobile is not supported in Firefox`. Нужен `test.skip(browserName === 'firefox')`, как у соседнего P0-1.
- `tests/browser/flip-rive-perf.spec.ts:74`: `newCDPSession` работает только в Chromium, поэтому 6 падений на firefox и webkit. Нужен `test.skip(browserName !== 'chromium')`. Проблема старая, с af126a6.
- `tests/browser/kotlin-ux4.spec.ts:255` (evidence-скриншоты v4): тест не ждёт окончания route-slide длиной 320 мс, поэтому часть PNG в `Plans/Kotlin/artifacts/ux4/web` снята посреди перехода. Утверждений в тесте нет, поэтому он проходит.
- iOS `testAnswerModeChosenInSettingsSurvivesAppRestart`: условие `label BEGINSWITH "Ответ,"` находит Picker тренировки под модальным окном Settings, 3/3 на iPhone и 2/2 на iPad. Нужен уникальный accessibilityIdentifier для Picker в Settings или скрыть TrainingView от accessibility, пока открыт sheet.
- iOS, только iPad: `testFirstMethodIntroductionKeepsReferenceAnswerHiddenUntilContinue` (не найден `ratingSwipeArea`) и `testNativeVerbGenderControlChangesSelectedSubjectOnly` (строка 716), оба 2/2. Вероятно, число свайпов в помощниках прокрутки подобрано под iPhone. Реальная проблема вёрстки на iPad не исключена, нужна проверка разработчиком.

## Среда и нестабильные тесты

- Web P05 «Вспомнил» (`kotlin-parity-ratings.spec.ts:23`): упал только в полном прогоне, отдельно проходит 3/3. Похоже на конкуренцию за ресурсы.
- Web UX5 scroll-to-top (`kotlin-ux4.spec.ts:353`), только firefox: `mouse.wheel` в headless Firefox ненадёжен. Прогон был один, повтор не делался.
- iOS `testNativeVocabularyRevealAndBinaryRating`: на iPhone упал из-за состояния, оставшегося на симуляторе. После `simctl uninstall` проходит, на iPad проходит.
- iOS `testS6VocabularyFileImporterCancellationKeepsSelection`: известная старая нестабильность системного UIDocumentPicker, описана в Lane-ios.md.

## Визуальные дефекты по скриншотам

- Web: в итоговых состояниях дефектов нет (20 PNG). Кажущаяся обрезка Settings оказалась артефактом `scrollIntoViewIfNeeded` в скрипте тестировщика. Evidence-PNG ux4 подписаны как итоговые, но сняты посреди перехода (см. дефекты тестов).
- Web 320px, цепочка: список шагов обрезается справа — это регрессия 3.
- Android: дефектов нет. Отдельное наблюдение: после force-stop индикатор цепочки сбросился с 2/5 на 0/5, а счётчики сохранились. Возможно, это задумано (показывается текущий шаг к повторению); решение за продуктом.
- iOS: 4 скриншота из XCTAttachment без дефектов. Toggle «Анимации» по умолчанию отрисован во включённом состоянии.
- macOS: 1 полноэкранный скриншот без дефектов.

## NOT_RUN и пробелы

- macOS: интерактивная проверка ADR-8/ADR-9 не выполнялась (раскрытие, переворот, свайп и ←/→, вкладки, «Анимации»): у osascript нет Accessibility-разрешения. XCTest target в `PolskiGrammarMac.xcodeproj` отсутствует.
- iOS: тесты под флагами `S6_PICKER_ACCEPTANCE`, `DIRECT_PICKER_ACCEPTANCE`, `IPAD_LANDSCAPE_ACCEPTANCE`, `REDUCE_MOTION_ACCEPTANCE` не компилируются, так задумано в проекте. Ручного управления через Simulator.app GUI в этой среде нет.
- Web: firefox wheel и CDP-падения не перепроверялись отдельным `--repeat-each`.
- Мелкое расхождение: в отчётах web и macOS указаны разные версии Kotlin (2.4.20 и 2.2.21) при одном и том же коммите. Одна из записей неверна; стоит проверить `libs.versions.toml`.
