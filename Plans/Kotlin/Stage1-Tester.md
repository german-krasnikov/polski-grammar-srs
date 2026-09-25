# Независимая приёмка этапа 1

**Объект:** React baseline от `df59774e153a5bdf590fe27bd8780c7bd5457a26`, текущий незакоммиченный diff этапа 1 и собранный React `dist` из developer evidence. Production `src/**` и deploy не менялись. Рабочий каталог команд: корень репозитория `/Users/german/Work/JS/polski-grammar-srs`.

**Добавленные тесты:** [`tests/parity-acceptance.test.ts`](../../tests/parity-acceptance.test.ts) проверяет уникальность semantic IDs и схему всех пяти fixture-файлов, ошибку настоящего времени у обоих совершенных глаголов и фактические `recordReview` счётчики на варшавской полуночи зимой и в день перехода на летнее время. [`tests/browser/react-acceptance.spec.ts`](../../tests/browser/react-acceptance.spec.ts) проверяет Enter/Space на кнопках, экспорт состояния после отказа `LocalStorage.setItem` и горизонтальную прокрутку таблицы реальным wheel input. Это тесты после реализации, они не доказывают исходный RED → GREEN разработчика.

| ID | Независимый результат React baseline | Оставшаяся граница |
| --- | --- | --- |
| P01 | PASS: повторно проверены full inventory replay, IDs и отказ совершенного вида в настоящем времени, включая `G-VERB-buyDone-present-rejected` и `G-VERB-doDone-present-rejected` в portable JSON. | Kotlin NOT RUN. |
| P02 | PASS: reused 60 linked fixture cases и Chromium five-card completion; исходный тест и JSON не менялись. | Kotlin NOT RUN. |
| P03 | PASS: reused 16 skills и controlled generator fixture replay; исходный тест и JSON не менялись. | Kotlin NOT RUN. |
| P04 | PASS: reused normalize/evaluate fixture и typed browser correct/incorrect/accepted; реальный IME NOT RUN. | Возможный composing Enter требует реального IME. |
| P05 | PASS: reused rating/counter browser flow и verified Enter/Space keyboard activation. | Kotlin NOT RUN. |
| P06 | PASS: reused queue/focused/chain browser evidence; исходный scenario не менялся. | Kotlin NOT RUN. |
| P07 | PASS: standard keyboard activation, modifiers/input guards reused. Focus после rating: confirmed baseline defect. | Реальный IME NOT RUN. |
| P08 | PASS: reused все 4 sections/selectors/drill; реальный horizontal wheel input на 320 CSS px проверен отдельно. | Screen reader NOT RUN. |
| P09 | PASS: reused context reference и сохранение карточки; исходный scenario не менялся. | Kotlin NOT RUN. |
| P10 | PASS: reused 4 × 4 states/ratings, preview, fuzz/steps/cap fixture replay; исходный fixture не менялся. | Kotlin NOT RUN. |
| P11 | PASS: actual `recordReview` counters проверены по обе стороны winter/DST midnight и экспорт из текущего session state после failed save; `P-review-midnight-winter/dst` теперь содержат full before/after state в portable JSON. Reused reload/reset/legacy/browser storage evidence. | Версия 99 принимается `importProgress`: baseline defect. Kotlin NOT RUN. |
| P12 | PASS: reused Chromium screenshots, 320 layout и reduced-motion computed style; их PNG SHA-256 не изменились. Root font 20 px при 320 px: confirmed navigation overflow. | Реальный browser zoom, IME, screen reader, физические Android browser и iOS Safari NOT RUN. |

Текущие независимые команды: `TZ=UTC npx vitest run tests/parity-fixtures.test.ts tests/parity-acceptance.test.ts` — PASS 12/12 после fixture correction; `npm test` — PASS 50/50 (исходные 38 + developer fixture 7 + tester 5); `npm run typecheck` — PASS; `npx playwright test tests/browser/react-acceptance.spec.ts` — PASS 3/3, Chromium 140.0.7339.186, Playwright 1.55.1, 0 retries. `git diff --check` — PASS для tracked diff. Проверка wheel сначала дала FAIL из-за положения pointer ниже viewport; test harness исправлен через `scrollIntoViewIfNeeded`, после чего focused case и полный tester browser spec прошли. Это был дефект теста, не продукта.

Повторно использованы developer результаты [`Evidence.md`](Evidence.md): `npm run fixtures:generate` дважды с одинаковыми байтами после correction, `npm run build`, Chromium baseline 10/10 и 21 PNG. Пять JSON теперь содержат 2141 cases (grammar 1979, exercises 112, evaluation 9, scheduler 21, progress 20) и source hashes остаются привязанными к исходной ревизии; новых production изменений нет. PNG не перезаписывались независимым browser spec. Device и Kotlin результаты не выводятся из Chromium/jsdom. Tester не выдаёт code-review approval; результат последующего независимого ревью зафиксирован в Evidence.md.

**Дефекты React baseline:** (1) после оценки новой карточки `.reveal-button` теряет keyboard focus (`tests/browser/react-baseline.spec.ts`, focus characterization); (2) при width 320 CSS px и root font 20 px верхняя навигация увеличивает `document.documentElement.scrollWidth` сверх viewport (`large-text-front.png` и browser assertion); (3) `importProgress('{"version":99,"cards":[],"stats":{}}')` возвращает объект вместо отказа (`P-import-unsupported-version`). Эти результаты не являются требованиями повторить дефекты в Kotlin. Отдельный продуктовый fix остаётся вне этапа 1.

**Fixture correction:** Developer добавил perfective-present errors и `recordReview` across local midnight/DST в versioned JSON с input, expected, time zone и fixed instant. Tester перепроверил replay в UTC и независимые assertions. Эти tests после реализации не утверждают, что был исходный test-first RED.
