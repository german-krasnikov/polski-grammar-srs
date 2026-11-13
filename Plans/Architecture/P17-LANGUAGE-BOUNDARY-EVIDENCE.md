# P17 — граница языка матрицы, evidence 2026-11-13

Основание: [EXAM-LEARNING-QUALITY-PLAN.md](EXAM-LEARNING-QUALITY-PLAN.md), P17/A17. Реализация в отдельном worktree `p17-language-boundary`, база `6e070111d117e203e5d153da43d625b33f7e2fdc`; изменения не закоммичены. Английские таблицы теперь выводятся только при `activeCoursePackOption.target == "en"`. SwiftUI читает `targetLanguage` из того же текущего Kotlin pack snapshot; для `pl` bridge не создаёт английские строки. После принятой смены `target`/`native` Swift host отправляет существующий `refresh`, чтобы активный сеанс немедленно перестроился. Ключи прогресса, FSRS и course data не менялись.

## Наблюдённый RED и исправление

- **Browser RED:** новый Playwright-сценарий `pl-ru → en-ru → pl-ru` запущен на прежнем JS distribution из основного checkout; на первой польской матрице получил две английские секции вместо нуля. Исходный artifact `composeApp.js` SHA-256 `d7f293b1abaa98ced391fe42fe69204309191976b370b085a5e4e2c38e40179f`.
- **Native snapshot RED:** новый `IosMatrixSnapshotTest.englishMatrixRowsFollowTheActiveTargetAcrossCourseSwitches` на прежнем snapshot не нашёл `targetLanguage` (`NoSuchElementException`). После изменения тест и существующие проверки английских форм прошли.
- **Native UI RED:** после выбора английского в Settings без перезапуска SwiftUI продолжал показывать польское `Лицо × число × время`, а английские строки не появлялись. Причина — `AppModel.setPreference` не вызывал `IosSession.rebuildIfCourseSwitched` до следующего dispatch/currentSnapshot. В текущем коде accepted target/native вызывает `send("refresh")`; XCTest затем подтверждает оба направления переключения.
- Первые два прогона XCTest падали на локализованной кнопке закрытия Settings (`Done`/`Готово`); это ошибки тестового селектора, не поведенческий RED. После правки обнаружился описанный выше дефект. При последующих прогонах установленный XCTest runner выполнял предыдущую версию тестового тела; удалён только test-runner app симулятора, пользовательское приложение и прогресс не сбрасывались. Итоговый тест исполнился **1/1 PASS**.

## Актуальные проверки

| Область | Статус | Команда и наблюдение |
|---|---|---|
| Kotlin/Native snapshot | **PASS** | `kotlin/`: `./gradlew :apple-bridge:iosSimulatorArm64Test --tests 'polski.ios.IosMatrixSnapshotTest.*English*' --offline --max-workers=1`; English rows отсутствуют для `pl`, присутствуют с реальными `see/saw`, `do/does/did` для `en`, исчезают после возврата на `pl`. |
| Web compilation, media, distribution | **PASS** | `kotlin/`: `./gradlew :composeApp:verifyNoWebSourceMaps --offline --max-workers=1`; JS, Wasm и compatibility distributions собраны, pinned 16 audio проверены в каждом distribution, source maps отсутствуют. Финальный JS `composeApp.js` SHA-256 `3e2059f2bde8d1a7699c87cdf8fb695fe3ed62f7a05236506f6afe1e4fea31a9`. |
| Browser acceptance | **PASS** | Корень репозитория: `KOTLIN_SPIKE_DIST=<этот worktree>/kotlin/composeApp/build/dist/js/productionExecutable KOTLIN_SPIKE_BRANCH=js KOTLIN_SPIKE_PORT=4187 npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-en-course-switch.spec.ts tests/browser/kotlin-en-matrix.spec.ts tests/browser/kotlin-en-acceptance-fixes.spec.ts --grep 'matrix shows only the active|English person × tense|do-support table|Matrix "Времена'`; 12/12 в Chromium, Firefox, WebKit. Проверены DOM-отсутствие английских секций на `pl`, реальные английские формы на `en` и повторный `pl` без restart. |
| iOS Simulator accessibility/UI | **PASS** | iPad Pro 11 M4 Simulator iOS 26.1: `xcodebuild -project kotlin/iosApp/PolskiGrammar.xcodeproj -scheme PolskiGrammar -configuration Debug -destination 'platform=iOS Simulator,id=AD9779D0-EAAC-40F0-8D29-C293BD5D5912' -derivedDataPath kotlin/iosApp/build CODE_SIGNING_ALLOWED=NO -only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testEnglishMatrixTableShowsRealFormsAndDoSupportSplit test`; 1/1, польская секция отсутствует после перехода на `en`, английские a11y labels `Before/After` и do-support доступны, возврат на `pl` убирает английский материал. |
| iOS final build | **PASS** | Та же команда `xcodebuild`, с `build` вместо `test`, после последних правок Swift copy/indent; `BUILD SUCCEEDED`. |
| TypeScript | **PASS** | Корень: `npm run typecheck`. |
| Diff hygiene | **PASS** | `git diff --check`. |
| Полные suites и устройства | **NOT RUN** | Полный `npm test`, полный Playwright и физические iOS/Android устройства: scope P17 требует focused host checks; независимые tester/reviewer gates ещё открыты. |

Первый обычный web build упал на `prepareExamAudio` с HTTP 403, потому что в этом worktree ещё не были доступны локальные исходные MP3. После восстановления только локальных ссылок на pinned source и проверки SHA полный build прошёл без исключения audio task. Промежуточный build с `-x :composeApp:prepareExamAudio` использовался только для раннего focused browser GREEN и не считается полным distribution gate. Логи сохранены вне worktree: `../recovery-logs/20261112/exam-learning-quality/p17/`.

Изменения ограничены web matrix, iOS bridge matrix snapshot, SwiftUI host и соответствующими regression tests. Android и замороженный Desktop не затронуты. Независимые tester/reviewer проверки P19 ещё требуются до закрытия задачи.
