# P19 — итоговое интеграционное принятие

2026-11-13. Производственный Main `4f515112012ab150c26e0aef7ec422d06f6154e1`; последующие правки этого протокола — документация. Исходные P1–P12 и обязательный P13–P19 рассматриваются в согласованном объёме:16 активных официальных экзаменов, B2/2022-06 исключён владельцем, native primary — iPad Simulator; физический iPad отложен. Ниже фактические результаты для финальной сверки Reviewer.

## Проверки и происхождение

| Среда / критерий | Результат / граница |
|---|---|
| Main generation / TypeScript | Biome generate --check и npm typecheck PASS на объединённом Main; `restore-incident-0626/final-main-{generation,typecheck}.log` |
| Shared / baseline | Vitest322/322, shared Desktop41/41, JS479/479, Wasm479/479 PASS на интеграционном исходнике `c2902b7`; common production после него не менялся, reuse обоснован [Web evidence](P19-INTEGRATED-WEB-VERIFICATION.md) |
| Main iOS bridge | Свежие25 exam+9 preference = **34/34 PASS**, failures/errors/skips0, `final-main-ios-bridge.log` и XML `apple-bridge/build/test-results/iosSimulatorArm64Test` |
| Swift и очищенные XCTest sources | Serial arm64 build-for-testing PASS после Main→native merge; native production `3ea8d0e` побайтово совпадает с Main. [Native receipt](P14-NATIVE-INDEPENDENT-VERIFICATION.md) |
| Production JS/Wasm | Свежий compatibility build,16 audio pins и отсутствие source maps PASS. Поздние native изменения затронули только ios source sets/Swift, web binaries не менялись |
| Playwright функциональность | Wasm Chromium24/24; JS initial22/24 + два исправленных artifact-fixture cases2/2 PASS; Firefox/WebKit combined28/28 на каждом target. Import/OFF/legacy/timed/variant/recovery и реальный hidden PASS; все исходные FAIL и границы сохранены в Web receipt |
| Мобильный отчёт | Real source Grammar/Reading/Listening + C1 rubric,320/375/1200px: Wasm Chromium4/4, actualJS Chromium4/4, Wasm Firefox/WebKit8/8;24PNG просмотрены. Light/dark contrast PASS, минимум5,47. [Приёмка](P15-MOBILE-REPORT-LAYOUT-INDEPENDENT.md) |
| Native actual UI | iPad iOS26.1: first choice/2s/lock, bank identity, grouped final/manual Next, same-task MP3, timed no early key, frozen wrong/blank/partial/pending, sheet/tab/background cancellation PASS. Final actual Files import/export2/2 PASS, OFF сохраняет raw/index, unlock и restart. Три найденных product defects исправлены; baseline timing/menu/0-selected harness результаты не подменены PASS |
| Native доступность | AX labels/focus, Dynamic Type portrait, реальный narrow window и Increase Contrast PASS. Actual VoiceOver speech/gestures, отдельный iPhone, actual landscape **NOT RUN**; physical iPad **DEFERRED / NOT RUN** |
| Ревью | Web/shared P19 APPROVED `2dacf34`; mobile CSS APPROVED `ae0fad5`; native APPROVED_WITH_MINOR, без Critical/Major. Незначительный предел unordered-set tint указан ниже |

Старые P13 PDF/key2420 scored rows +91 writing prompts, P17 pair isolation и P18 десять тем/60 упражнений/четыре стиля приняты отдельно; неизменённые источники не пересверялись искусственно. Все26 польских навыков/42tips сохранены; Writing и unknown manual ответы не получают выдуманной автоматической оценки. Ссылки: [P13](P13-OFFICIAL-SOURCE-REAUDIT.md), [P17](P17-INDEPENDENT-VERIFICATION.md), [P18](P18-SLICES-BD-INDEPENDENT-VERIFICATION.md).

## Публикация и артефакты

Runtime `4f51511`: [Kotlin CI43377406998](https://github.com/german-krasnikov/polski-grammar-srs/actions/runs/43377406998) и [Pages43377407266](https://github.com/german-krasnikov/polski-grammar-srs/actions/runs/43377407266) **SUCCESS**. Live Chromium140 на375×667 загрузил по отдельности реальные `originWasmComposeApp.js`/`originJsComposeApp.js` (HTTP200). DefaultON, неверный первый выбор/lock/auto-next, frozen выбранный+source, OFF/reload/editability PASS, page errors0. CSS HTTP SHA `d24d7af8b492b2e7c64704a58d678aa746aaf5560fb666c2fa61aa1ba0cddd7c`; все три текстовых поля263px без intrinsic overflow внутри319px report row. Live receipts/PNG: `../polski-grammar-srs-worktrees/recovery-logs/20261113/p19-integrated-web/live/live-feedback-4f51511-{wasm,js}.json`, `live-review-4f51511-{wasm,js}.png`.

Принятые копии вне удалённых worktrees:

- `snapshots/p19-final-20261113`:46files,830494736bytes, treeSHA `ca02b7d24054eb95f3f6bfbcdad16b1fda9191a2a0ecb6cb0c4348d16e1815b6`.
- `recovery-logs/20261113/worktree-cleanup/native-artifacts/p14-final-accepted.app`:38files,1212298946bytes, treeSHA `662d22fe481873830c4c1eed89e4b2c07bcc89d37581a7ee9ba902d4b34273ce`, dylibSHA `8aa79159ad2c859abb759c1a301ca8f641e1b6cc74f866b78c543a6904015452`.
- Полный manifest `worktree-cleanup/p19-final-artifacts.json`; source/test receipts и original owner backups сохранены.

Локально и на GitHub остался только `main`; git worktree list содержит только основной checkout. Все удалённые task commits вошли в опубликованный Main, remote deletion использовал expected-SHA lease. [Манифесты очистки и старый uniqueWIP bundle](WORKTREE-CLEANUP-20261113.md).

## Данные и ограничения

Опасная ошибочная restore-команда была прервана; последующий ограниченный аудит не обнаружил непреднамеренной потери данных в проверенных областях, полной форензики хоста нет. Root восстановил original app data проверенным helper без rsync/delete, сохранил post-test данные для rollback и проверил19 original hashes до boot. Fresh owner-specific UI1/1 PASS: defaultON и каталог; все прежние progress values/другие preferences совпали после запуска.33 cards/stats стали42 только от инициализации девяти новых тем. [Полный отчёт об инциденте и границах проверки](SIMULATOR-RESTORE-INCIDENT-20261113.md).

Неблокирующий UI предел: правильный unordered set показан целиком зелёным текстом в feedback/отчёте, но отдельные кнопки его элементов пока не получают зелёный tint. Scoring, первый raw/token, полный эталон и quarter points доступны; это не выдано за per-member visual coverage. Native actual VoiceOver/landscape/iPhone и physical device ограничения перечислены явно выше. Нативный app установлен и работает в CoreSimulator; отдельная macOS Simulator.app GUI отсутствует в установленном Xcode tree (parent Developer mtime2026-10-30), поэтому не заявляется открытое macOS окно. Финальный CoreSimulator frame сохранён как `restore-incident-0626/final-owner-app.png`.

**Итоговая сверка: APPROVED.** Независимый Reviewer сверил runtime `4f51511`, документацию `de61ff4`, публикацию, сохранённые артефакты, owner-data evidence и фактическую очистку веток/worktree. Все обязательные code/target/browser/Simulator gates согласованного среза выполнены; P19 закрыт, общий план **19/19**. Указанные minor и NOT RUN сохраняются; физический iPad отложен владельцем, полный forensic-аудит host не заявляется.
