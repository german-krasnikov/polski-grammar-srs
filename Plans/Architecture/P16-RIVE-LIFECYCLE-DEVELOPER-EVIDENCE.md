# P16: жизненный цикл Rive в web-хосте — передача разработчика

2026-11-13. Изолированная ветка `work/p16-rive-lifecycle-20261113`, исходный HEAD `8039beb`. Принятый контракт: [EXAM-PRACTICE-FEEDBACK-DESIGN.md](EXAM-PRACTICE-FEEDBACK-DESIGN.md), исходное воспроизведение: [P16-ANIMATION-REPRODUCTION.md](P16-ANIMATION-REPRODUCTION.md). Это web-исправление; grading, FSRS, DSL-таблица эффектов и нативные хосты не изменены.

## Изменённое поведение

- `RiveEffectOverlay` помечает слой владельцем, отменяет только cue этого владельца при уходе с маршрута/размонтировании и оставляет DOM-слой для другого живого хоста. Отмена маршрута обратима: при возвращении карточки запускают новый эффект. Отказ или зависание загрузки bridge очищает слой и разрешает следующую попытку.
- `rive-bridge.js` проверяет поколение cue и текущие настройки после каждого ленивого callback. Таймеры скрытия запускаются до загрузки метаданных/runtime, хранятся отдельно для rating и chain, отменяются при замене cue; ошибка script, уход страницы в фон, Reduced Motion и Animations OFF очищают текущие instances и слои. Внутреннее состояние оценки не зависит от завершения Rive.
- Обычная оценка по-прежнему запрашивает `again.riv`, завершение цепочки — `chain-complete.riv`; исходные имена/маппинг берутся из неизменённой сгенерированной DSL-таблицы.

## Наблюдавшиеся RED

Все RED исполнены на неизменяемом `../snapshots/p14-first-feedback-20261113` (Wasm). Логи ниже находятся в `../recovery-logs/20261112/exam-learning-quality/p16/` относительно этого дерева.

| Сценарий | Результат до исправления | Квитанция |
|---|---|---|
| Rating → немедленный Matrix | Слой остался видимым | `isolated-red.log`, root baseline 2/2 RED |
| Ошибка `rive-effects.generated.js` | Слой видим после 4 с | `isolated-red.log` |
| Ошибка `rive-bridge.js` | Слой видим после 4 с | `developer-red-bridge/trace.zip`, запуск `failed bridge` |
| Зависший запрос `rive-bridge.js` | Слой видим после 4 с | `developer-stalled-bridge-baseline-red.log` |
| Настоящий background tab | Chrome перевёл документ в `hidden` через 416 мс, слой остался `display:block` | `developer-real-hidden-baseline-red.log` |
| Пятая оценка цепочки → Matrix | `#polski-rive-chain` остался видимым на Matrix | `developer-chain-route-baseline.log` |

Обычный Playwright `newPage`/`bringToFront` оставлял `document.visibilityState=visible` в headless Chrome; этот пробный тест удалён как недостоверная проверка поведения. `p16-real-hidden-rive.cjs` запускает собственный Chrome-профиль, завершает Playwright-клиент перед активацией второй вкладки через CDP и записывает реальный `visibilitychange`. Профиль и процесс удаляются в `finally`.

## Проверки и границы

`kotlin/` → `./gradlew :composeApp:composeCompatibilityBrowserDistribution --max-workers=2 --console=plain`: **PASS**, `developer-build-final4.log`, JS/Wasm и 16 проверенных audio assets. Финальный `RiveEffectOverlay.kt` имеет SHA-256 `b379f1c12730805d89b94d79e660d723ae1b7e0c0c327a99265c7db244d7b7c6`; `rive-bridge.js` — `a809155ef90f1377c96aa312616672b301f673aef9d0f5c155b11b1e2af007b2` в рукописном источнике, JS, Wasm и compatibility distribution. JS `composeApp.js`: `7bd082f6e229ca822b2ab4f3b36c03a7a682652ff7e70763a458fa175078f836`; Wasm launcher: `17035f02e00bf143c181517a911dd95376a1b0b6ebaba754c047301ca17f0d50`; Wasm app: `2f1738616f56093429951f8f97179408dab486c30c5111c3c7b843310436c59b`.

`npm run typecheck`: **PASS**. `node --check` для bridge и real-hidden fixture: **PASS**. `biome generate --check --project dsl/biome.project`: **PASS**, `developer-biome-check.log`; исходные DSL/генерация не изменены. `git diff --check`: **PASS**.

| Реальный браузер / набор | Результат | Квитанция |
|---|---|---|
| JS Chromium + Firefox, lifecycle без отдельного one-review сценария | **PASS 18/18** | `developer-final-js-core-all-engines.log` |
| JS WebKit, тот же набор | **PASS 6/9; FAIL 3/9** — три сценария требуют rating click, оставшаяся раскрытая карточка подтверждает общий дефект ввода P18 | `developer-final-js-core-all-engines.log`, `final-js-webkit-p18-failures/` |
| Wasm Chromium, lifecycle без отдельного one-review сценария | **PASS 10/10** | `developer-final-wasm-core-all-engines.log` |
| Wasm Firefox + WebKit, тот же набор | **PASS 17/20; FAIL 3/20** — рейтинг-клик потерян до создания cue/перехода к следующей карточке | `developer-final-wasm-core-all-engines.log`, `final-wasm-p18-failures/` |
| Настоящий hidden, Chrome 140.0.7339.186, финальные JS/Wasm артефакты | **PASS**: слой скрыт при реальном `document.hidden`, соответственно за 434/679 мс после rating; после возврата не появляется | `real-hidden-final-js/real-hidden.json`, `real-hidden-final-wasm/real-hidden.json` |
| Следующая оценка после временной ошибки bridge | **PASS 3/3 JS и 3/3 Wasm** в Chromium/Firefox/WebKit | `developer-final-js-bridge-retry.log`, `developer-final-wasm-core-all-engines.log` |
| Два быстрых rating cue на одной цепочке | **PASS JS Chromium и Wasm Chromium**: второй `data-rive-effect` реально меняется, и слой остаётся видим после срока первого таймера | `developer-final-js-rapid-timer2.log`, `developer-final-wasm-rapid-timer.log` |
| Существующие flip/Rive сценарии до последнего добавления host timeout | **PASS 15/15 JS и 15/15 Wasm** в Chromium/Firefox/WebKit, включая Animations OFF без Rive-запросов | `developer-js-existing-flip-rive.log`, `developer-wasm-existing-flip-rive.log` |
| Animations изначально OFF на точном финальном артефакте | **PASS JS Chromium и Wasm Chromium**: ни одного Rive-запроса при reveal, flip и rating | `developer-final-js-animations-off.log`, `developer-final-wasm-animations-off.log` |

До объединения с исправлением P18 отдельный тест мгновенного клика оценки в JS/WebKit дважды дал `totalReviews=0` при оставшейся раскрытой карточке. В финальном прогоне все шесть FAIL также остановились на раскрытой карточке с видимой кнопкой оценки, то есть до проверки целевого Rive-состояния. Независимая диагностика P18 зафиксировала перемещение кнопки между `mousedown` и `mouseup` во время существующего перехода раскрытия `0fr→1fr`; предположение о перестройке route из-за счётчиков сохранения было опровергнуто и отменено. P18 исправляет защиту pointer-ввода в период этого перехода. Тест оставлен без искусственного ожидания, чтобы объединённая сборка могла проверить один сохранённый отзыв на каждый клик. P16 не меняет обработчик rating/FSRS.

Независимое тестирование и review ещё не выполнялись для этого diff. Нативный Simulator, Android и физические устройства **NOT RUN** для P16 web-среза.

## Совмещённая проверка после P18 gate и motion override

Текущий HEAD `cefc53c` включает `0752eed` и `cefc53c`; исходники P16 выше остались теми же. Относительно этого HEAD сделана **новая** production compatibility сборка, затем полный `tests/browser/kotlin-rive-lifecycle.spec.ts` без ожидания между «Показать ответ» и click по оценке. Предыдущие шесть потерь click в Firefox/WebKit больше не воспроизводятся. Тест не менялся ради этого прогона.

| Проверка | Итог | Квитанция в `../recovery-logs/20261112/exam-learning-quality/p16/` |
|---|---|---|
| `kotlin/`: `./gradlew :composeApp:composeCompatibilityBrowserDistribution --max-workers=2 --console=plain` | **PASS**, 116 tasks; JS, Wasm, compatibility audio по 16 assets | `developer-combined-build.log` |
| Корень: `npm run typecheck`; `node --check` bridge и real-hidden fixture; `git diff --check` | **PASS** | `developer-combined-typecheck.log`; console |
| Корень: `node /Users/german/Work/Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs generate --check --project dsl/biome.project` | **PASS** | `developer-combined-biome.log` |
| Compatibility distribution, `KOTLIN_SPIKE_BRANCH=js`, Playwright Chromium/Firefox/WebKit | **PASS 34/34**, 2 intentional skips (real-time rapid timer only Chromium), zero retries | `developer-combined-js-lifecycle.log` |
| Та же distribution, `KOTLIN_SPIKE_BRANCH=wasm`, Playwright Chromium/Firefox/WebKit | **PASS 34/34**, 2 intentional skips, zero retries | `developer-combined-wasm-lifecycle.log` |
| Реальный `visibilitychange`, owned Chrome 140.0.7339.186, отдельные JS/Wasm production artifacts | **PASS** в обеих ветках: слой скрыт после background, после возврата не воскрес | `developer-combined-real-hidden-{js,wasm}.log`, `real-hidden-combined-{js,wasm}/` |

Browser command: `KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable KOTLIN_SPIKE_BRANCH=<js|wasm> KOTLIN_SPIKE_PORT=<4241|4242> npx playwright test --config=playwright.kotlin.config.ts tests/browser/kotlin-rive-lifecycle.spec.ts --output=../recovery-logs/20261112/exam-learning-quality/p16/combined-<branch>-artifacts` из корня. Real-hidden fixture запускался через `node tests/browser/p16-real-hidden-rive.cjs <served-url> <evidence-dir>` с `vite preview` на портах 4243/4244. Это настоящий browser background transition, не подмена `document.hidden`.

Хэши **новой** сборки: `RiveEffectOverlay.kt` `b379f1c12730805d89b94d79e660d723ae1b7e0c0c327a99265c7db244d7b7c6`; bridge source/JS/Wasm/compatibility `a809155ef90f1377c96aa312616672b301f673aef9d0f5c155b11b1e2af007b2`; JS `composeApp.js` `6cf7eaa2d07e1bdfa42920e4952444640d5039185a8acc2e3cd06d822c6c62b4`; Wasm launcher `composeApp.js` `f1a4a652a01c1c45962d0bf812e93055241b56ca4edfc6ba8192376cd1b60d0d`. Эти хэши связывают browser результаты с `cefc53c` + текущим P16 diff; старые хэши выше относятся к предыдущему артефакту до P18.

Developer gate готов к независимому Tester → Reviewer. iOS Simulator, Android и физические браузеры/устройства для P16 web-среза **NOT RUN**; ни одна из web-проверок их не закрывает.
