# P16: независимая проверка web Rive lifecycle

2026-11-13. Проверен commit `56652d0dfcc75cb4e01c9d86cb03d12dacf77edd` с одним последующим test-only изменением: в `tests/browser/kotlin-rive-lifecycle.spec.ts` для failed/stalled bridge предел ожидания снижен с 4000 до 3200 мс. Production-файлы не менялись. Контракт: [EXAM-PRACTICE-FEEDBACK-DESIGN.md](EXAM-PRACTICE-FEEDBACK-DESIGN.md), P16 срез 3; исходные дефекты: [P16-ANIMATION-REPRODUCTION.md](P16-ANIMATION-REPRODUCTION.md). Доказательства в `../recovery-logs/20261112/exam-learning-quality/p16/independent/` относительно этого worktree.

Артефакты совпадают с объединённой сборкой разработчика: SHA-256 `RiveEffectOverlay.kt` `b379f1c12730805d89b94d79e660d723ae1b7e0c0c327a99265c7db244d7b7c6`, исходный и раздаваемый `rive-bridge.js` `a809155ef90f1377c96aa312616672b301f673aef9d0f5c155b11b1e2af007b2`, JS `composeApp.js` `6cf7eaa2d07e1bdfa42920e4952444640d5039185a8acc2e3cd06d822c6c62b4`, Wasm launcher `composeApp.js` `f1a4a652a01c1c45962d0bf812e93055241b56ca4edfc6ba8192376cd1b60d0d`. Production build, typecheck, Biome check и node syntax checks **reused PASS** из `P16-RIVE-LIFECYCLE-DEVELOPER-EVIDENCE.md`: исходники и хэши сборки не изменились. Они не переименованы в независимые прогоны.

## Проверки

| Критерий | Результат и наблюдение |
|---|---|
| P16 route exit, chain exit, return/new cue | **PASS** JS/Wasm Chromium, Firefox, WebKit: слой отсутствует на Matrix; возвращение разрешает новый cue. Пятая реальная оценка загружает `chain-complete.riv`, затем слой очищается на Matrix. |
| P16 metadata/bridge abort, stalled bridge, retry | **PASS** JS/Wasm во всех трёх движках. Failed/stalled bridge скрыт в пределах Playwright assertion 3200 мс после rating click; вторая оценка после временной ошибки снова показывает cue. |
| P16 delayed callback, first/second cue timers | **PASS**: задержанный metadata load после выхода не показывает слой; в Chromium второй быстрый cue сохраняется за пределом таймера первого. В Firefox/WebKit временной timer-сценарий intentionally skipped; остальные lifecycle сценарии исполнены. |
| P16 motion settings | **PASS**: системный Reduced Motion скрывает активный cue; изначально Animations OFF даёт ноль Rive network requests через reveal, flip и rating на обеих карточках, JS/Wasm во всех трёх движках. |
| P16 reviews and natural input | **PASS**: тест кликает реальные кнопки без ожидания после reveal; при двух rating с уходом и возвратом `totalReviews` становится точно 1, затем 2. Отдельные asset/canvas checks подтверждают обычный `again.riv`. |
| P16 real browser background | **PASS** JS/Wasm, owned Chrome `140.0.7339.186`: `document.hidden=true` получен через CDP tab activation; `display:none` наблюдался через 1718/2142 мс после click, и слой не вернулся при foreground. Это не подмена visibility state. |
| P16 native iOS Simulator, Android, physical Safari/device | **NOT RUN** для этого web diff; Playwright WebKit не закрывает device gate. |

Запуски из корня worktree, Playwright `1.55.1`, zero retries, отдельные strict ports, owned servers завершены:

```text
KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable KOTLIN_SPIKE_BRANCH=js KOTLIN_SPIKE_PORT=4251 npx playwright test --config=playwright.kotlin.config.ts tests/browser/kotlin-rive-lifecycle.spec.ts --output=../recovery-logs/20261112/exam-learning-quality/p16/independent/js-artifacts
KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable KOTLIN_SPIKE_BRANCH=wasm KOTLIN_SPIKE_PORT=4252 npx playwright test --config=playwright.kotlin.config.ts tests/browser/kotlin-rive-lifecycle.spec.ts --output=../recovery-logs/20261112/exam-learning-quality/p16/independent/wasm-artifacts
```

Оба запуска: **34 passed, 2 intentional skipped**. Логи: `js-lifecycle.log`, `wasm-lifecycle.log`. После test-only ужесточения два bridge-сценария повторены с `-g 'failed bridge loading|stalled bridge request'` на портах 4257/4258: **6/6 PASS** в JS и **6/6 PASS** в Wasm, `js-bound.log`, `wasm-bound.log`. `Animations off: zero Rive network requests` из `kotlin-flip-card.spec.ts` отдельно на портах 4255/4256: **3/3 PASS** на каждой ветке, `js-off.log`, `wasm-off.log`.

Настоящий hidden проверен командой `node tests/browser/p16-real-hidden-rive.cjs http://127.0.0.1:<port> <evidence-dir>` с `vite preview --strictPort` для отдельных `dist/js` (4253) и `dist/wasmJs` (4254). Результаты: `js-hidden/real-hidden.json`, `wasm-hidden/real-hidden.json` и PNG после возврата. Профили Chrome и процессы fixture удалены. `git diff --check`: **PASS**. Новых production-дефектов не обнаружено. Эти поздние acceptance прогоны не являются доказательством developer RED → GREEN и не заменяют независимый code review.
