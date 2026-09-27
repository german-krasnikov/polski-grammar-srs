# Интеграционный тест: четыре стиля подачи (UC-10)

Коммит: `main 73380c7`. Blueprint: [StylesBlueprint.md](StylesBlueprint.md) (ST-01..ST-11). Логи: `/private/tmp/claude-501/styles-test/<platform>/`.

Вердикт: **FAIL** — 2 продуктовых дефекта (situation-first без правила после reveal; `Rule.detail` не рендерится на iOS/macOS/Android) + архитектурный разрыв (`courses/styles/*.json` не загружается).

## Сводка по платформам

| Платформа | Проверки | Итог |
|---|---|---|
| shared (desktop/js/wasm) | `:shared:desktopTest` 213/213, `jsBrowserTest` 212/212, `wasmJsBrowserTest` 212/212 | PASS после правки устаревшего теста |
| shared macOS K/N | `:shared:macosArm64Test` 228 | PASS после правки 2 устаревших тестов |
| shared iOS K/N | `:shared:iosSimulatorArm64Test` 244/244 | PASS после правки устаревшего теста |
| Web Playwright (chromium, wasm+js) | 6 спецификаций: 68 PASS, 1 FAIL (`kotlin-ux4.spec.ts:256`, не относится к стилям) | FAIL (не стили) |
| Web Playwright (webkit) | `kotlin-style-blocks.spec.ts` wasm 1/1, js 1/1 | PASS |
| npm | `typecheck` PASS; `course:validate` PASS; `npm test` 204/204 | PASS |
| Android | `:androidApp:testDebugUnitTest` 57/57 (вкл. `AndroidStyleBlocksComposeTest` 2/2), `assembleDebug`, ручной прогон на эмуляторе | PASS (но дефект D2 не проверялся визуально) |
| iOS XCUITest | 6/6 существующих PASS; новый `testFourStylesShowIntendedBlocksWithRealContent` 0/1 | FAIL (дефект D1) |
| macOS SwiftUI | `xcodebuild build` PASS; запуск + persist через файл preferences PASS | PARTIAL (reveal не прогнан) |

## Проверка критериев

| Критерий | Результат |
|---|---|
| rule-first: formula/table на лицевой, rule на обороте | PASS (web, Android, iOS, macOS front) |
| situation-first: сцена на лицевой, правило после reveal | **FAIL** — на обороте только `Changes` (web, iOS; общий код → все хосты) |
| native-contrast: RU↔PL + бейдж совпадает/отличается | PASS (web, Android: оба состояния бейджа на реальном контенте; iOS по a11y id) |
| minimal-theory: примеры + свёрнутое «Почему так?» | PASS (web, Android, iOS) |
| смена стиля не создаёт review и не очищает ввод | PASS (iOS XCUITest `testMethodSwitchKeepsTypedDraftThroughRevealAndOneReview`, unit-тесты) |
| сохранение после перезапуска | PASS (Android, iOS, macOS) |
| Logic/Situations → rule-first/situation-first | PASS (`UserPreferencesCodecTest`; macOS вживую: v1 `Situations` → «Через ситуацию») |
| ответ не виден до reveal | PASS по стилям; см. D4 (утечка через focus в rule-first, существовала до UC-10) |
| light/dark, узкая ширина | PASS web (390/1280, 32 скриншота), Android (~411dp), macOS light/dark; iOS dark/narrow — NOT_RUN |

## Продуктовые дефекты

| # | Дефект | Класс | Файл |
|---|---|---|---|
| D1 | `StyleRegistry`: `SituationFirst` Back = `[Changes]`, нет `Rule`. Blueprint §1 и `courses/styles/situation-first.json` требуют `[changes, rule]`. Воспроизведено на web (скриншот + `innerText`) и iOS XCUITest (`PolskiGrammarUITests.swift:1150`). | production, ядро, все хосты | `kotlin/shared/src/commonMain/kotlin/polski/presentation/StyleRecipe.kt:48` |
| D2 | `Block.Rule.detail` (объяснение упражнения, fix f8742fc) рендерится только на web. iOS `StyleRuleBlock`, macOS `MacStyleBlockView` case `"rule"`, Android `AndroidRuleBlock` показывают только `text`. Регрессия rule-first/ST-02 на 3 хостах: ветки lane слиты без rebase на f8742fc. | production, хосты | `kotlin/iosApp/PolskiGrammar/FlashCardView.swift:257`, `kotlin/macosApp/PolskiGrammarMac/MacStyleBlockView.swift:21`, `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidStyleBlocks.kt:81` |
| D3 | `courses/styles/*.json` нигде не загружается (Blueprint §4/§5). `StyleRegistry` — ручные литералы с пустыми label/description; хосты берут свои fallback-строки, и они расходятся: macOS «Схемы и правила» / «Через сравнение с русским» против JSON «Через правило» / «Через сравнение с родным». Порядок блоков тоже расходится (rule-first front JSON `[table, formula]` против Kotlin `[Formula, Table]`). Это первопричина D1. | архитектурное расхождение с blueprint | `StyleRecipe.kt:33-75`, `MacPreferencesSession.kt`, `AndroidStylePresentation.kt` |
| D4 | rule-first, `case.acc.f`: блок Table/«ЗАПОМНИ» на лицевой показывает `moja piękna żona → moją piękną żonę`, то есть готовый ответ. Существовало до UC-10 (ST-02 требует байт-в-байт как Logic). | контент/дизайн, существовало раньше | `courses/pl-ru/course.json` (`case.acc.f.focus`), `StyleComposer.kt` (Table из focus) |
| D5 | `kotlin-ux4.spec.ts:256`: strict-mode, 2 × `.route-content` после перехода на вкладку «Прогресс»; 3/3 на wasm и js. Файлы из этого diff не менялись. | неясно (нужен прогон на базе до UC-10) | `tests/browser/kotlin-ux4.spec.ts`, RouteSlider |

Мелочь (не нарушение spec): native-contrast на обороте — оранжевый контрастный блок без подписи «ЗАПОМНИ»/правила.

## Исправленные тесты (устаревшие допущения, не продукт)

- `kotlin/shared/src/commonTest/kotlin/polski/data/CourseDataStyleContentTest.kt` — `realCourseSkillsAllDefaultUntilContentAuthorsStyleContent` → `realCourseSkillsAllHaveAuthoredNativeParallel` (CONTENT уже заполнил styleContent для 16 навыков).
- `kotlin/shared/src/macosTest/kotlin/polski/macos/MacSnapshotStyleBlocksTest.kt` — fallback native-contrast → rule-first больше не срабатывает на реальном контенте.
- `tests/browser/kotlin-preferences-settings.spec.ts`, `tests/browser/kotlin-style-blocks.spec.ts` — ожидания 2 стилей → 4.
- В lane iOS (`polski-lanes/ios`) добавлен `testFourStylesShowIntendedBlocksWithRealContent` (`PolskiGrammarUITests.swift`) — сейчас красный из-за D1.

## Визуальная проверка

- Web: 4 стиля × front/back × 390/1280 × light/dark просмотрены вручную. Дефектов вёрстки нет; D1 виден визуально. Скриншоты: `/private/tmp/claude-501/styles-test/web-shared/screenshots/`.
- Android: все 4 стиля, настройки, dark/light на эмуляторе. Дефектов вёрстки нет, падений в logcat нет. D2 не отмечен: проверялось наличие блоков, а не текст `detail`.
- iOS: 4 скриншота только верха страницы (ошибка в хелпере захвата). Подписи стилей в порядке; блоки визуально не проверены.
- macOS: front rule-first, light/dark, persist. Оборот не открыт (нет доступа к Accessibility). D4 подтверждён кропом.

## NOT_RUN

- macOS: reveal, смена стиля мышью, ресайз окна. `osascript`/`cliclick` не имеют доступа к Accessibility (-1719).
- iOS: dark mode, iPhone SE (узкая ширина), скриншоты блоков в середине скролла, запуск со старыми preferences через NSUserDefaults.
- Android: ширина <360dp и планшет, TalkBack, поворот, миграция старых preferences на устройстве, instrumented-тесты.
- Web: Firefox; базовый прогон `kotlin-ux4.spec.ts:256` на коммите до UC-10.
- JVM Compose Desktop preview: только `:composeApp:desktopTest` (в нём по-прежнему 2 стиля, ST-10 это допускает).
