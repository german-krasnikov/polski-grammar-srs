# P15: узкий отчёт экзамена на Web

2026-11-13. База Main `b81fcab` (после этого только документация `ff6cd13`). Изменены только `kotlin/composeApp/src/webMain/resources/exam.css`, новый `tests/browser/kotlin-exam-report-layout.spec.ts` и его discovery в `playwright.kotlin.config.ts`. Источники экзамена, DOM, оценивание, таймеры и аудио не менялись.

## Исполняемый RED

На старой compatibility distribution с исходным `display:flex` новый Playwright-тест открыл реальные source-backed отчёты через интерфейс. Chromium/Wasm выполнил 3 сценария и все 3 упали при 375 CSS px: контекст Grammar сжался до **45,9 px**, Reading до **81,7 px**, выбранный ответ Listening до **43,4 px**. В отчёте были реальные неверный ответ/ключ, частичный балл и ручная проверка. [Лог RED](../../../polski-grammar-srs-worktrees/recovery-logs/20261113/p19-integrated-web/live/mobile-report-layout-red.log), [скриншоты и трассы](../../../polski-grammar-srs-worktrees/recovery-logs/20261113/p19-integrated-web/live/mobile-report-red-artifacts/). Это поведенческий layout RED до правки CSS; не ошибка компиляции.

## Исправление и GREEN

Строка отчёта теперь имеет колонку номера и `minmax(0,1fr)` для остальных полей. Контекст, выбранный ответ, ключ, очки, ручная проверка и список критериев идут отдельными строками; текст переносится без обрезания на 375/320 px и остаётся читаемым на desktop 1200 px. В CSS удалён `white-space:nowrap` для ключа. Все поля и тексты сохранены.

Проверки на свежей compatibility distribution:

| Gate | Результат |
|---|---|
| `kotlin/`: `./gradlew :composeApp:composeCompatibilityBrowserDistribution --max-workers=2 --console=plain` | **PASS**, 116 задач, 16 audio assets; [лог](../../../polski-grammar-srs-worktrees/recovery-logs/20261113/p19-integrated-web/live/mobile-report-layout-build.log) |
| Chromium/Wasm: Grammar wrong/blank, Reading wrong/source, Listening partial/pending; 375/320/1200 px | **PASS 3/3**, [лог](../../../polski-grammar-srs-worktrees/recovery-logs/20261113/p19-integrated-web/live/mobile-report-layout-wasm-green.log) |
| Chromium/JS fallback: те же 3 сценария | **PASS 3/3**, [лог](../../../polski-grammar-srs-worktrees/recovery-logs/20261113/p19-integrated-web/live/mobile-report-layout-js-green.log) |
| Firefox/WebKit, Wasm: те же 3 сценария | **PASS 6/6**, [лог](../../../polski-grammar-srs-worktrees/recovery-logs/20261113/p19-integrated-web/live/mobile-report-layout-other-engines.log) |
| C1/2022-11 source rubric, 4 пункта, 375/320/1200 px, Chromium/Wasm | **PASS 1/1**, [лог](../../../polski-grammar-srs-worktrees/recovery-logs/20261113/p19-integrated-web/live/mobile-report-layout-rubric-green.log) |
| `npm run typecheck` и `git diff --check` | **PASS** после добавления rubric case, [typecheck log](../../../polski-grammar-srs-worktrees/recovery-logs/20261113/p19-integrated-web/live/mobile-report-layout-typecheck-final.log) |

Команда browser gate: `KOTLIN_SPIKE_DIST="$PWD/kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable" KOTLIN_SPIKE_BRANCH=wasm|js KOTLIN_SPIKE_PORT=4208 npx playwright test tests/browser/kotlin-exam-report-layout.spec.ts --config playwright.kotlin.config.ts --project=chromium|firefox|webkit --workers=1`. CSS SHA-256 в рукописном источнике и в собранной compatibility distribution совпадает: `d24d7af8b492b2e7c64704a58d678aa746aaf5560fb666c2fa61aa1ba0cddd7c`.

Независимый визуальный review ещё **NOT RUN**. Browser automation проверила реальные строки, их границы, внутреннее переполнение, ширину поля и ширину документа; native Simulator/device к этой CSS правке не относится.
