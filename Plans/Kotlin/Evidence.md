# Этап 1: evidence React baseline

**Исходная ревизия приложения:** `df59774e153a5bdf590fe27bd8780c7bd5457a26`. Production `src/**` и deploy не менялись. Текущий рабочий diff этапа 1: `.gitignore`, `package.json`, `package-lock.json`, `tsconfig.node.json`, `vitest.config.ts`, `playwright.config.ts`, `tests/parity-fixtures.test.ts`, `tests/parity-acceptance.test.ts`, `tests/fixtures/kotlin-parity/**`, `tests/browser/react-baseline.spec.ts`, `tests/browser/react-acceptance.spec.ts`, `Plans/Kotlin/Parity.md`, `Plans/Kotlin/Stage1-Tester.md`, этот файл, `Plans/Kotlin/artifacts/stage1/**`. Ранее незакоммиченные README/agent/plan документы принадлежат другой подготовке и сохранены. Коммита нет.

**Окружение:** macOS 26.6 arm64 (25G72), Node 24.1.0, npm 11.3.0, Vitest 4.1.11, Vite 7.3.6, ts-fsrs 5.4.2, Playwright Test 1.55.1 с matching managed Chromium 140.0.7339.186 (build 1193). Browser server: готовый React `dist` через `npm run preview`, `http://127.0.0.1:4173`; новый context каждого test, locale `ru-RU`, timezone `Europe/Warsaw`, fixed clock `2026-02-03T12:00:00.000Z`, DPR 1. Нет retry. Browser screenshots сняты с `document.fonts.ready` и disabled animations; reduced-motion снимок отдельно устанавливает `reduce`. Это desktop Chromium evidence, не iOS Safari или Android browser.

| Первоначальная проверка Developer до fixture correction (cwd: корень репозитория) | Результат |
| --- | --- |
| `npm run fixtures:generate` × 2 и `shasum -a 256 tests/fixtures/kotlin-parity/*.json` | PASS: все шесть JSON, включая manifest, побайтно одинаковы в двух запусках. |
| `npm test` | PASS: 5 файлов, 45 тестов (38 исходных + 7 fixture tests). Browser spec исключён из Vitest. |
| `TZ=UTC npx vitest run tests/parity-fixtures.test.ts` | PASS: 7 тестов; fixture replay не зависит от внешней TZ. |
| `npm run typecheck` | PASS: `tsc -b`, включая Playwright spec/config и generator. |
| `npm run build` | PASS: Vite 7.3.6, `index-aYQhbQxJ.js` 303.99 kB, `index-CcoXCRPe.css` 13.26 kB. Тот же production artifact использован browser lane. |
| `npm run test:browser` | PASS: 10/10 Chromium tests, 0 retries, 13.8 s. Browser context, console/pageerror и storage проверяются в test. |
| `git diff --check` | PASS для tracked diff; untracked файлы просмотрены отдельно. |
| Kotlin/Gradle, Android device, iOS device/Safari, real IME, screen reader, browser zoom | NOT RUN: этапы/оборудование или ручной метод ввода ещё не доступны. Снимок с увеличенным root font есть отдельно. |

**Fixture provenance:** `tests/fixtures/kotlin-parity/manifest.json` хранит SHA-256 13 исходных файлов, lockfile и каждого JSON, case IDs, реальные версии из lockfile, effective параметры FSRS, source revision и правила сравнения. В первоначальном capture было 2137 cases; итоговый состав после correction приведён ниже. `capture.ts` восстанавливает `Date`, `Math.random`, `TZ` и storage в `finally`; для генератора задаёт draws и проверяет их расход. Пять raw exercise ID в каждой цепочке проверены на непустоту и уникальность, wire fixture содержит `<generated-id>`. `ts-fsrs` fuzz использует внутренний Alea от карточки/момента; фиксированные `Date`/card определяют результат, внешний `Math.random` его не заменяет. Часы local-day проверены по обе стороны варшавской полуночи, DST и в UTC. Ошибки import зафиксированы с kind/message. RED для capture как characterization: N/A; observed GREEN — fixture replay и хэши. Исходные независимые assertions остаются в своих 38 тестах. Browser harness сначала дал ошибки discovery/strict locators, исправленные как setup/test selectors; продуктовых RED не заявлено.

**Browser artifacts:** [`artifacts/stage1`](artifacts/stage1) содержит `desktop-front/revealed/typed/reference`, `desktop-matrix-map/cases/verbs/pronouns`, `desktop-progress` при 1280 × 720 CSS px; такие же `320-*` при 320 × 700; `landscape-progress` при 700 × 320; `large-text-front` при 320 × 700 с root font 20 px; `reduced-motion-front` при 700 × 320 с `prefers-reduced-motion: reduce`. Все PNG — full page. Состояния: front = oral, скрытый эталон; revealed = эталон и четыре ratings; typed = textarea, скрытый эталон; reference = typed и открытая контекстная таблица. Матрица снимается по отдельным разделам, progress после возврата через вкладки. Начальный storage пустой, clock/locale/zone как выше. Снимки не являются попиксельным контрактом Compose. Для 320 comparison table browser test действительно прокручивает внутренний контейнер до правого крайнего столбца, и `document.documentElement.scrollWidth <= innerWidth` проверено для 320 progress.

**Визуальный просмотр основным агентом:** после исправления ошибочно одинаковых первых 320 PNG повторно просмотрены текущие файлы с SHA-256 prefixes `320-front` 7f12d229, `320-revealed` 7d325aae, `320-typed` 5575dd55, `320-reference` 0786ca63. Состояния соответствуют именам; на обычных 320 px обязательные тексты, формы, кнопки и ratings видимы без обнаруженного наложения. Также просмотрены desktop front/revealed/reference, desktop matrix map/cases/verbs/pronouns, 320 matrix map/cases и 320 progress: польская диакритика читается, блоки перестраиваются вертикально, широкая case table остаётся в scroll container. `large-text-front` показал выход правой кнопки верхней навигации за 320 CSS px; browser assertion подтверждает `scrollWidth > innerWidth` при root font 20 px. Саму карточку и controls можно прочитать. `reduced-motion-front` читаем, а browser computed style отдельно подтвердил `transitionDuration = 0s` на reveal button при `prefers-reduced-motion: reduce`. Это ограниченный визуальный review PNG, а не accessibility/device proof; каждый текст длинной карты не проверен вручную в полном масштабе.

**Известные расхождения React baseline:** после grading новая reveal button не получает focus; отдельный browser characterization test наблюдает это. При root font 20 px и ширине 320 px верхняя навигация переполняет viewport. `importProgress` принимает JSON версии 99 при наличии `cards`/`stats`; fixture `P-import-unsupported-version` сохраняет текущий результат, а не желаемую валидацию. В `App.tsx` Enter в textarea не проверяет `isComposing`; реальный IME не проверен, поэтому пока это риск, а не подтверждённый дефект. Исправления production вне этапа 1. Печатный ответ проверен в Chromium на exact/accepted/incorrect, но IME и физические устройства остаются открытыми строками [Parity.md](Parity.md).

**Tester / Reviewer:** независимая приёмка и итоговое ревью описаны ниже. Главный план отмечает этап после этих gates.

## Независимая приёмка Tester (2026-09-23)

Отчёт и матрица P01–P12: [Stage1-Tester.md](Stage1-Tester.md). Добавлены `tests/parity-acceptance.test.ts` и `tests/browser/react-acceptance.spec.ts`, без изменений `src/**` и deploy. После замечания Tester Developer добавил четыре portable cases: `G-VERB-buyDone-present-rejected`, `G-VERB-doDone-present-rejected`, `P-review-midnight-winter`, `P-review-midnight-dst`. Итоговый manifest — 2141 cases: grammar 1979, exercises 112, evaluation 9, scheduler 21, progress 20. Developer сообщил о двух побайтно одинаковых запусках generator после correction; Tester самостоятельно проверил fixture replay и manifest hashes через тесты.

| Независимая команда (cwd: корень репозитория) | Результат |
| --- | --- |
| `TZ=UTC npx vitest run tests/parity-fixtures.test.ts tests/parity-acceptance.test.ts` | PASS 12/12, включая corrected portable cases и actual `recordReview` counters через winter/DST midnight. |
| `npm test` | PASS 50/50: исходные 38, developer fixture 7, tester acceptance 5. |
| `npm run typecheck` | PASS, включая оба tester test files. |
| `npx playwright test tests/browser/react-acceptance.spec.ts` | PASS 3/3, Chromium 140.0.7339.186, Playwright 1.55.1, 0 retries, pageerror/console collection. Keyboard Enter/Space, export после failed save и natural horizontal wheel до последней колонки. |
| `npm run build` | Reused Developer PASS: production `src/**` и build inputs не менялись; tester не повторял сборку. |
| `npm run test:browser` (первоначальный запуск до добавления tester spec) | Reused Developer PASS 10/10 для `tests/browser/react-baseline.spec.ts` и 21 PNG: browser source/artifact не менялись. Tester отдельно запустил `tests/browser/react-acceptance.spec.ts` 3/3 без перезаписи PNG; текущие 13 cases не запускались одной командой. |
| Kotlin/Gradle, реальные устройства, Safari, IME, screen reader, browser zoom | NOT RUN, как выше. |

Первый wheel test упал, потому что pointer был ниже видимой области таблицы; после test-only `scrollIntoViewIfNeeded` и проверки достижения `scrollLeft` до `scrollWidth - clientWidth` focused case и весь tester browser spec прошли. Это дефект теста. Три подтверждённых baseline defects (потеря focus после rating, overflow навигации при 320 px/20 px, принятие import version 99) остаются открытыми как характеристики React. Browser/device ограничения и fixture/provenance приведены в [Stage1-Tester.md](Stage1-Tester.md); результат последующего независимого code review — ниже.

## Итог Reviewer и координатора (2026-09-23)

Reviewer (`gpt-6-sol`): **APPROVED**. Scope: фикстуры, browser tests, конфигурация и evidence этапа 1. Ревьюер сопоставил код и отчёты Developer/Tester; сам тесты не запускал и файлы не менял. Нерешённых Critical/Major замечаний по подготовке эталона нет.

Итог: 2141 portable cases; Vitest **50/50 PASS**, TypeScript **PASS**, production build **PASS**; Chromium baseline **10/10 PASS** и independent acceptance **3/3 PASS** в отдельных запусках, без retries. Общий запуск всех 13 сценариев не заявляется. Main дополнительно просмотрел окончательные `desktop-typed.png` и `desktop-reference.png`: поле ввода, скрытый эталон и справочная таблица соответствуют описанным состояниям, наложений не обнаружено. Проверка ссылок/пробелов документации и `git diff --check` — PASS. `src/**`, deploy и исходные 38 тестов не изменены; preview-сервер после проверок остановлен.

Этап 1 в [Plan.md](Plan.md) закрыт как подготовка React baseline. Следующий этап — 2, web spike Compose. Kotlin, реальные мобильные устройства, Safari, IME, screen reader и browser zoom остаются **NOT RUN**; подтверждённые дефекты React описаны в [Parity.md](Parity.md) и не становятся обязательным поведением Kotlin. Коммита и публикации не было.

## Текущая Kotlin browser реализация (2026-09-23, рабочее дерево)

Текущие файлы Kotlin, конфигурация и браузерные тесты находятся в рабочем дереве поверх той же исходной React ревизии; коммита, live CI run и публикации Kotlin нет. Это evidence для локального артефакта, а не утверждение о clean checkout. Отдельные исходные отчёты: [Stage2-Tester.md](Stage2-Tester.md), [Stage3-Tester.md](Stage3-Tester.md), [Stage4-Tester.md](Stage4-Tester.md), [Stage5-Tester.md](Stage5-Tester.md), [Stage6-Tester.md](Stage6-Tester.md). Этапы 2–3 всё ещё имеют указанные в них формальные device/clean-checkout gaps.

| Проверка и cwd | Наблюдавшийся результат |
| --- | --- |
| `node kotlin/shared/src/commonTest/kotlin/polski/srs/generate_expanded.mjs --check`, корень | PASS: 70 oracle cases против установленного `ts-fsrs` 5.4.2. |
| `./gradlew :shared:jsBrowserTest :shared:wasmJsBrowserTest --tests 'polski.srs.*' --rerun-tasks`, `kotlin/`, независимый Stage6 Tester | PASS: 99/99 на JS и 99/99 на Wasm, 0 fail/error/skip. Reviewer: APPROVED_WITH_MINOR без Critical/Major. |
| `./gradlew :shared:jsBrowserTest :shared:wasmJsBrowserTest --rerun-tasks --console=plain`, `kotlin/`, основной агент | PASS: 142/142 на JS и 142/142 на Wasm, 0 fail/error/skip по XML. Использован scoped `shared/karma.config.d/mocha-timeout.js` c Mocha 10s после непостоянного 2s runner timeout; тесты не пропускались. |
| `./gradlew :composeApp:jsBrowserTest :composeApp:wasmJsBrowserTest --rerun-tasks --console=plain`, `kotlin/`, основной агент | PASS: 15/15 на JS и 15/15 на Wasm, включая 14 `TrainingStore` и один реальный LocalStorage web test на target. Browser host UI этими Gradle tests не проверяется. |
| `./gradlew :composeApp:composeCompatibilityBrowserDistribution`, `kotlin/` | PASS для полного Kotlin UI после JS/Wasm компиляции и включения `training.css`/`matrix-progress.css`. Сборка остаётся локальным preview, React deploy не изменён. |
| `npm run typecheck`, корень | PASS после добавления Kotlin browser Playwright specs в отдельный TS project. |
| `KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable KOTLIN_SPIKE_BRANCH=wasm npx playwright test --config=playwright.kotlin.config.ts tests/browser/kotlin-training.spec.ts`, корень, Stage9 Developer | PASS: 18/18, 6 сценариев × Chromium/Firefox/WebKit на production distribution. |
| Та же команда с `KOTLIN_SPIKE_BRANCH=js`, Stage9 Developer | PASS: 18/18, принудительная JS fallback ветка и три desktop engines. |
| Та же команда для `tests/browser/kotlin-matrix-progress.spec.ts`, основной агент | PASS: 15/15 на JS и 15/15 на Wasm, по 5 сценариев × три desktop engines. Первый Wasm прогон имел test-only FAIL: после подтверждённого reset тест ожидал сохранения вкладки Progress, хотя контракт переводит на Training; исправлен test expectation, полный прогон PASS. Firefox focus-visible test сначала использовал программный focus и получил `outline: none`; после реального `Tab` focused retest и полный прогон PASS. Это не наблюдавшиеся production RED. |
| Физический Android Chrome, iPhone Safari, настоящая IME/soft keyboard, screen reader, крупный zoom на устройстве | NOT RUN. Desktop Playwright WebKit и 320px/root-font проверки не доказывают эти пункты. |
| Независимая приёмка и итоговое ревью этапов 7–11, React rollback текущего Kotlin export | NOT RUN / В РАБОТЕ. Их результаты будут добавлены после выполнения, web parity gate пока открыт. |

Визуальный просмотр текущего preview на Chromium: [desktop-training.png](artifacts/stage10/desktop-training.png), [desktop-matrix.png](artifacts/stage10/desktop-matrix.png), [mobile-progress.png](artifacts/stage10/mobile-progress.png). Первый снимок показал белый фон DOM host под светлым текстом вне карточек; в `training.css` задан явный тёмный фон host, после чего все три снимка пересняты и просмотрены. На desktop карточка и таблица читаются, на 320px навигация и сводка прогресса не перекрываются; внутренний scroll host не включается в full-page screenshot, поэтому это просмотр viewport, а не доказательство всей длинной страницы. CSS-правка была скопирована в локально обслуживаемый preview сразу; её inclusion в production distribution требует следующей сборки. Контраст всех пар цветов инструментально не измерялся.

### Повторная локальная проверка после исправления переноса

2026-09-23 основной агент проверил собранный `composeWebCompatibility/productionExecutable` после добавления кнопки **Повторить перенос** и явного тёмного фона DOM host. Исторический независимый отчёт [Stage7-Tester.md](Stage7-Tester.md) сохраняет наблюдавшийся до исправления FAIL; это новое наблюдение не является независимым ревью.

| Проверка и cwd | Результат |
| --- | --- |
| `npm run typecheck`, корень | PASS. |
| `./gradlew :shared:jsBrowserTest :shared:wasmJsBrowserTest :composeApp:jsBrowserTest :composeApp:wasmJsBrowserTest --console=plain`, `kotlin/` | PASS, все четыре задачи завершились; две задачи JS были UP-TO-DATE, Wasm задачи выполнены. Ранее полный shared suite 142/142 и focused store 16/16 на каждой ветке уже проходили после соответствующих правок. |
| `KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable KOTLIN_SPIKE_BRANCH=js KOTLIN_SPIKE_PORT=4174 npx playwright test --config playwright.kotlin.config.ts`, корень | PASS 42/42, 14 сценариев × Chromium/Firefox/WebKit, 0 retries; включает retry после transient marker write, preview/backup/marker/reload и загрузку текущего Kotlin export в React. |
| Та же команда с `KOTLIN_SPIKE_BRANCH=wasm` и `--reporter=line` | PASS 42/42, те же три desktop engines. |
| `git diff --check`, корень | PASS для отслеживаемого diff. |
| Физические Android Chrome/iPhone Safari, реальная IME/soft keyboard, screen reader, device zoom, независимая повторная Stage7/8–11 приёмка и ревью, полное исполнение 20 progress fixtures | NOT RUN. Соответствующие gates остаются открытыми. |

Локальный сервер `http://127.0.0.1:8765/` отдаёт этот артефакт (HTTP 200); превью открыто через браузер системы. Сборка не публиковалась. CSS-исправление вошло в повторную production distribution, а не только в предварительно скопированный файл сервера.

### Расширенное сравнение Kotlin web и виртуальные устройства (2026-09-23)

Полный [журнал 20 progress fixtures](ProgressParity.md) и [матрица P01–P12](Parity.md) отделяют проверенные сценарии от незакрытых критериев. Новые browser specs используют текущий production distribution, pinned React fixtures и управляемое время/часовой пояс. Генерируемый Kotlin common test повторяет same-skill review по обе стороны полуночи/DST и три import raw. Изменений production-кода в этом раунде не было.

| Проверка и cwd | Результат |
| --- | --- |
| `./gradlew :shared:jsBrowserTest :shared:wasmJsBrowserTest --tests 'polski.progress.ProgressFixtureParityTest'`, `kotlin/` | PASS: 5/5 на JS и 5/5 на Wasm, 0 fail/error/skip по JUnit XML. |
| `KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable KOTLIN_SPIKE_BRANCH=wasm npx playwright test --config=playwright.kotlin.config.ts`, корень | PASS: 96/96, 32 сценария × Chromium/Firefox/WebKit, 0 retries. |
| Та же команда с `KOTLIN_SPIKE_BRANCH=js KOTLIN_SPIKE_PORT=4175`, корень | PASS: 96/96, принудительная JS fallback ветка. Первый параллельный прогон дал 2 FAIL инфраструктуры из-за общего Playwright `test-results` и отсутствующих trace files; после раздельного `outputDir` полный JS rerun прошёл. |
| `npm run typecheck`, корень | PASS после расширения browser specs и раздельного `outputDir`. |
| iPhone 17 Pro Simulator Safari | PARTIAL PASS: текущий Kotlin web UI загрузился, [снимок](artifacts/ios/iphone-17-pro-safari-kotlin.png). Safari first-run tip перекрывает низ экрана; IME/interaction не проверялись. |
| Android API 35 Emulator, WebView Browser Tester 124 | PARTIAL PASS: [загрузка](artifacts/android/emulator-webview-kotlin.png), [экранная клавиатура](artifacts/android/emulator-webview-keyboard.png), [ответ](artifacts/android/emulator-webview-answer.png), [reload](artifacts/android/emulator-webview-reload.png). Ввод, проверка, оценка и сохранение после reload наблюдались; это WebView, не Chrome. |
| Физические Android Chrome/iPhone Safari, польская IME-композиция, VoiceOver/TalkBack, device zoom, live CI, независимая Stage 11 приёмка/ревью | NOT RUN. Формальные web/device gates остаются открытыми. |

Код и документация находятся в рабочем дереве; коммита и публикации нет. `git diff --check` прошёл для отслеживаемого diff. Полный suite сам по себе не даёт права отмечать этапы 2–17 завершёнными.

Дополнение: прямое React ↔ Kotlin P03 сравнение всех 16 skill-picker selections прошло 3/3 на каждой ветке в трёх движках. P02 сравнение всех 12 пятишаговых цепочек прошло 3/3 на каждой ветке (60 карточек на запуск). P10 UI preview четырёх оценок для новой карточки прошёл 3/3 на каждой ветке при замороженном времени до загрузки обеих версий. Первый P10 запуск расходился только для fuzzed «Легко», поскольку clocks продолжали идти во время разной загрузки React/Kotlin; после точной фиксации входного времени сравнение прошло без продуктовой правки. После включения всех этих сценариев **полный** Playwright suite прошёл **105/105 на Wasm и 105/105 на JS**. `npm run typecheck` прошёл после этих specs. Первый `npm test` после Kotlin-сборки ошибочно обнаружил 75 тестовых файлов из `kotlin/build/js/node_modules`; 50 собственных тестов при этом прошли. `vitest.config.ts` теперь ограничивает discovery `tests/**/*.test.{ts,tsx}`, и повторный `npm test` завершился **PASS 50/50, 6 файлов**. Это исправление тестового runner, а не продукта.

Ещё один P05 browser test сравнил полный сериализованный React ↔ Kotlin прогресс после каждой из четырёх оценок первой устной карточки при одном фиксированном времени: **12/12 на Wasm и 12/12 на JS** (четыре оценки × три движка). Остальные состояния карточек и независимая приёмка остаются открытыми.

P05 shortcut guard прошёл **3/3 на Wasm и 3/3 на JS**: повторное нажатие клавиши оценки после перехода к скрытой следующей карточке оставило `totalReviews=1`. После добавления этих тестов `npm run typecheck` — PASS и `npm test` — PASS 50/50. Итоговый **полный объединённый browser rerun** со всеми 40 сценариями прошёл **120/120 на Wasm и 120/120 на JS** (Chromium, Firefox, WebKit; 0 retries). Это локальная приёмка основного агента, не независимый Stage 11 sign-off.

### Пакет `pl-ru`, контраст и словарь (2026-09-23)

Текущий рабочий diff добавляет единый курс `courses/pl-ru/course.json`, частотный список с [атрибуцией](../../courses/pl-ru/ATTRIBUTION.md), контраст старой/новой формы, две подачи и отдельный словарь. Публикации и коммита этого diff не было.

Для границ слова получен отдельный RED: `она` внутри `żona` ошибочно выделялась в React (`tests/ending-highlight.test.ts`, 1 FAIL из 5) и Kotlin Desktop (`EndingHighlightTest`, 1 FAIL из 5). После проверки границ Unicode-букв в обеих реализациях — GREEN 5/5 на каждом target. Полный последующий rerun указан ниже отдельно.

| Проверка и cwd | Результат |
| --- | --- |
| `npm test`, корень | PASS: 60/60 после исправления границ подсветки, включая fixture parity, подсветку и отказ записи словаря. |
| `npm run typecheck` и `npm run build`, корень | PASS; production React dist создан. |
| `npm run test:browser -- tests/browser/react-baseline.spec.ts`, корень | PASS: 12/12 Chromium, включая 320 px и увеличенный шрифт. |
| `./gradlew :composeApp:wasmJsBrowserDistribution :composeApp:composeCompatibilityBrowserDistribution`, `kotlin/` | PASS по отдельным запускам; Wasm и JS production dist созданы. Webpack предупреждает о размере бандла. |
| `KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/wasmJs/productionExecutable KOTLIN_SPIKE_BRANCH=wasm npx playwright test --config=playwright.kotlin.config.ts --project=chromium --project=firefox --project=webkit tests/browser/kotlin-vocabulary.spec.ts`, корень | PASS: 9/9, включая React JSON → Kotlin, две очереди и отказ `localStorage`. |
| Та же команда для `dist/js/productionExecutable`, `KOTLIN_SPIKE_BRANCH=js KOTLIN_SPIKE_PORT=4175` | PASS: 9/9. |
| `./gradlew :shared:desktopTest :shared:iosSimulatorArm64Test :shared:compileKotlinJs :shared:compileKotlinWasmJs`, `kotlin/` | PASS после последнего ужесточения ID пользовательского слова. `:composeApp:compileKotlinDesktop` также PASS после UI-правок. |
| `ANDROID_HOME=$HOME/Library/Android/sdk ./gradlew :androidApp:assembleDebug`, `kotlin/`; `adb install -r` и `am start` | PASS: APK собран, установлен и запущен на Android Emulator. [Снимок](artifacts/android/course-method-contrast.png) показывает нативный Material-интерфейс, красные окончания до ответа и выбор подачи. Первый снимок во время долгого cold start был пустым; повторный после `Displayed ... +24s951ms` показывает экран. |
| `xcodebuild ... -destination 'platform=iOS Simulator,id=4384946F-9E6B-43D0-ADA3-CA219A3456B8' ... build`, корень; `xcrun simctl install/launch` | PASS: SwiftUI build, установка и запуск на iPhone 17 Pro Simulator. [Снимок](artifacts/ios/course-method-contrast.png) показывает красные окончания до ответа и нативный выбор подачи. Первый запуск по `name=iPhone 17 Pro` дал destination FAIL из-за `OS:latest`; точный simulator ID решил проблему. |
| Все статические строки таблиц, контент-пакеты других языков, полная методика, нативный словарь Mac/Android/iOS, реальные IME/VoiceOver/TalkBack, независимый Tester/Reviewer | NOT RUN / НЕ ГОТОВО; см. [CoursePacksPlan.md](CoursePacksPlan.md). |

### 23 сентября · повторная веб-приёмка после подсветки и текста ситуаций

Полный Playwright-прогон сначала дал 129/132 на Wasm и 129/132 на JS: один и тот же P08 не совпадал в трёх браузерах. Kotlin не показывал исходное целое предложение в строках падежной таблицы; после исправления P08 дошёл до следующего расхождения — React не показывал пары «инфинитив → форма» в таблице вида. Это реальные RED парности UI. После исправлений P08 прошёл 3/3 на каждом таргете. Тесты, сравнивавшие старую формулировку ситуации дословно, обновлены на смысловую проверку; это устаревшее ожидание теста, а не дефект ответа/FSRS.

| Проверка и cwd | Результат |
| --- | --- |
| `npm test`, `npm run typecheck`, `npm run build`, корень | PASS: 60/60, TypeScript и React production build после правок. |
| `npm run test:browser -- tests/browser/react-baseline.spec.ts`, корень | PASS: 12/12 после обновления проверки сцен. |
| `./gradlew :composeApp:wasmJsBrowserDistribution :composeApp:jsBrowserDistribution`, `kotlin/` | PASS: обе production-сборки после новой таблицы и данных курса; предупреждения Webpack о размере бандла. |
| Полный `npx playwright test --config=playwright.kotlin.config.ts` с `KOTLIN_SPIKE_DIST=.../dist/wasmJs/productionExecutable`, `KOTLIN_SPIKE_BRANCH=wasm`, корень | PASS: 132/132, Chromium/Firefox/WebKit, 0 retries. |
| Та же команда с `.../dist/js/productionExecutable`, `KOTLIN_SPIKE_BRANCH=js`, `KOTLIN_SPIKE_PORT=4175` | PASS: 132/132, Chromium/Firefox/WebKit, 0 retries. |
| `./gradlew :shared:desktopTest :shared:iosSimulatorArm64Test`, `kotlin/` | PASS на текущем пакете курса и исправлении границ подсветки. |
| `ANDROID_HOME=/Users/german/Library/Android/sdk ./gradlew :androidApp:assembleDebug`, `kotlin/`; `adb install -r` / `am start`, корень | PASS: актуальный APK собран, установлен и запущен на Android Emulator `emulator-5554`. [Снимок экрана](artifacts/android/course-method-final.png) осмотрен. |
| `xcodebuild -project kotlin/iosApp/PolskiGrammar.xcodeproj -scheme PolskiGrammar -configuration Debug -destination 'platform=iOS Simulator,id=4384946F-9E6B-43D0-ADA3-CA219A3456B8' -derivedDataPath kotlin/iosApp/build build`, корень; `simctl install/launch` | PASS: актуальный SwiftUI app собран и запущен на iPhone 17 Pro и iPad (A16) Simulator. [iPhone](artifacts/ios/course-method-final-iphone.png) и [iPad](artifacts/ios/course-method-final-ipad.png) снимки осмотрены. |

Запуск независимого `gpt-6-sol` Reviewer вновь отклонён лимитом потоков агента; основной агент выполнил собственный просмотр изменений, который не считается независимым ревью. Реальные устройства, screen readers и нативный словарь остаются NOT RUN.

### 24 сентября · схема пакета и перенос текстов генератора

`courses/schema/course-pack-v1.schema.json` и `frequency-top1000-v1.schema.json` описывают формат данных. `scripts/validate-course.mjs` проверяет schema и связи между навыками, seeds, глаголами, частотным источником и карточками; запускается перед React build и в Kotlin CI. `source-inventory.json` воспроизводимо перечисляет 1561 строку-кандидат из 33 файлов; редакторская классификация и проверка происхождения ещё открыты. 12 sentence seeds, префиксы падежных предложений, фиксированные подсказки/причины, шаблоны предложений, справочные подписи падежей/родов и пятишаговая цепочка перенесены в `course.json`; React и Kotlin читают их оттуда.

Наблюдавшийся RED: 8 из 9 первых тестов валидатора, затем два теста ссылок seed и два теста placeholders; отдельные подстановки шаблонов упали 2/2 в React и 2/2 в Kotlin Desktop на намеренных заглушках. После реализации эти группы прошли. Первый Kotlin JS браузерный прогон показал пустой экран: unescaped `}` в общем Kotlin `Regex` вызывает `SyntaxError` только в JS с Unicode-режимом. После экранирования `}` общие JS/Wasm тесты и три адресных JS браузерных сценария прошли. Старый manifest fixture хранит hash исходного lockfile; после установки Ajv тест проверяет исторические source/fixture hashes и версии `ts-fsrs`/`tsx` в текущем lockfile. CI checkout получает историю Git для чтения исходной ревизии manifest.

| Проверка и cwd | Результат |
| --- | --- |
| `npm run course:validate`, `npm run course:inventory:check`, корень | PASS; пакет и воспроизводимая инвентаризация. |
| `npm test`, корень | PASS 75/75 на переносе генераторов; затем добавлен тест отказа для неподдерживаемого `futureType=irregular`, его focused suite PASS 14/14. |
| `npm run typecheck`, `npm run build`, корень | PASS; build выполнил валидатор и собрал production React. |
| `./gradlew :shared:desktopTest :shared:compileKotlinJs :shared:compileKotlinWasmJs`, `kotlin/` | PASS после переноса шаблонов и текста; отдельные Kotlin pattern tests 2/2. |
| `./gradlew :shared:jsBrowserTest :shared:wasmJsBrowserTest`, `kotlin/` | PASS после JS Regex fix; прежняя проверка ограничивалась компиляцией этих targets. |
| `./gradlew :composeApp:wasmJsBrowserDistribution :composeApp:jsBrowserDistribution`, `kotlin/` | PASS; обе production-сборки, с предупреждениями Webpack о размере. |
| `npm run test:browser -- tests/browser/react-baseline.spec.ts`, корень | PASS 12/12. |
| Полный Kotlin Wasm Playwright suite, Chromium/Firefox/WebKit | PASS 132/132 после переноса генераторов. |
| Полный Kotlin JS Playwright suite, Chromium/Firefox/WebKit | PASS 132/132 после JS Regex fix и переноса генераторов. |
| Отдельные тесты метаданных матрицы | RED 2/2 на дубликате падежа и неизвестном skill, затем GREEN; `npm test` PASS 78/78, `npm run typecheck` и `npm run build` PASS после переноса. |
| `./gradlew :shared:desktopTest :shared:jsBrowserTest :shared:wasmJsBrowserTest :shared:iosSimulatorArm64Test`, `kotlin/` | PASS после переноса справочника. |
| `ANDROID_HOME=/Users/german/Library/Android/sdk ./gradlew :composeApp:wasmJsBrowserDistribution :composeApp:jsBrowserDistribution :composeApp:compileKotlinDesktop :androidApp:assembleDebug`, `kotlin/` | PASS. Первый Android compile выявил отсутствие зависимости `compileAndroidMain → generateCoursePackSource`; сборочная связь исправлена. |
| Kotlin Wasm/JS адресные `kotlin-parity-matrix` и `kotlin-matrix-progress` в трёх браузерах | PASS 18/18 на каждой ветке после переноса справочника. |
| Android Emulator `Polski_ARM35`, актуальный APK | PASS: `adb install -r`, `am start`, `topResumedActivity=...MainActivity`; [экран тренировки](artifacts/android/course-reference-current.png) и [матрица](artifacts/android/course-reference-matrix.png) осмотрены. Первый снимок после касания получился пустым во время перехода; повторный снимок и UI hierarchy подтвердили экран матрицы. |
| iOS Simulator, iPhone 17 Pro и iPad (A16) | PASS: `xcodebuild` Debug, `simctl install/launch`; [iPhone](artifacts/ios/course-reference-current.png) и [iPad](artifacts/ios/course-reference-ipad.png) снимки осмотрены. |
| Реальные устройства, screen readers, независимые Tester/Reviewer | NOT RUN. |

Пятишаговая цепочка теперь хранится в `reference.chainRows`: `label`, `from`, `to`, `change`. Проверка последовательности сначала дала RED 1/1 на разрыве между строками, затем GREEN. React и Kotlin Web используют эти строки в таблице; Desktop/Android показывают обе фразы с красным/синим подчёркнутым изменением и подписями «Было»/«Стало». iOS snapshot передаёт в SwiftUI обе размеченные стороны; отдельный iOS test проверил все пять переходов. Эти изменения сделаны **после** полного браузерного прогона 132/132 выше, поэтому для актуального diff запускались адресные browser gates.

Дополнительно React и Kotlin common tests сравнили каждую пару `from/to` справочной цепочки с реально создаваемым упражнением `generateChain` на эталонной seed. Это защищает авторский пример от расхождения с тренировкой. Новая нативная разметка Android подтверждена [снимком экрана](artifacts/android/course-chain-contrast-native.png); экран матрицы SwiftUI после этой правки визуально не осматривался.

| Проверка после переноса цепочки | Результат |
| --- | --- |
| `npm test`, `npm run build`, `npm run typecheck` | PASS 79/79, production React build и TypeScript. |
| `./gradlew :shared:desktopTest :shared:jsBrowserTest :shared:wasmJsBrowserTest :shared:iosSimulatorArm64Test :composeApp:compileKotlinDesktop :androidApp:assembleDebug :composeApp:wasmJsBrowserDistribution :composeApp:jsBrowserDistribution` с `ANDROID_HOME`, `kotlin/` | PASS, включая iOS snapshot test; Webpack сохранил предупреждения о размере. |
| Новые browser проверки всех пяти `Было → Стало` и P08 матрицы на Wasm и JS | PASS 6/6 для каждой ветки в Chromium/Firefox/WebKit. Первая попытка теста завершилась до запуска из-за JSON import attribute Node; тестовый fixture стал читать JSON через `readFileSync`, после чего сценарии прошли. |
| `xcodebuild -quiet ... build`, `simctl install/launch` iPhone 17 Pro Simulator | PASS после новой SwiftUI цепочки; сам экран матрицы на iOS визуально не осматривался. |
| Android Emulator `Polski_ARM35`, обновлённый APK | PASS: установлен и открыт; [строки цепочки](artifacts/android/course-chain-contrast-native.png) осмотрены с подписью и цветом обеих сторон. |
| `npm run course:validate`, `npm run course:inventory:check`, `actionlint .github/workflows/kotlin-check.yml`, `git diff --check` | PASS. |
| `npx vitest run tests/course-reference-chain.test.ts tests/course-validation.test.ts`, `./gradlew :shared:desktopTest :shared:jsBrowserTest :shared:wasmJsBrowserTest` | PASS: 18/18 React focused checks и shared tests на Desktop/JS/Wasm для связи справочника с фабрикой. |
| Полный browser suite после последнего переноса, реальный iOS/Android, screen readers, независимый Reviewer | NOT RUN. Попытка открыть независимого `gpt-6-sol` Reviewer снова отклонена лимитом потоков. |

Пять строк сравнения времён теперь входят в `reference.tenseRows` общего пакета и проверяются относительно базовой или прошедшей фразы. Тест ошибочной опоры дал RED 1/1, после валидатора GREEN. React, Kotlin Web, Desktop, Android и SwiftUI читают одни и те же строки; нативные экраны показывают «Было» и «Стало». Инвентаризация после переноса: 1545 строк-кандидатов из 33 файлов; остальные строки требуют редакторской классификации.

| Проверка после переноса времён | Результат |
| --- | --- |
| `npm test`, `npm run typecheck`, `npm run build`, `npm run course:validate` | PASS: 81/81, TypeScript, production React, валидатор. |
| `./gradlew :shared:desktopTest :shared:jsBrowserTest :shared:wasmJsBrowserTest :shared:iosSimulatorArm64Test :composeApp:compileKotlinDesktop :androidApp:assembleDebug :composeApp:wasmJsBrowserDistribution :composeApp:jsBrowserDistribution`, `kotlin/` | PASS; повторный прогон завершился exit 0. |
| Kotlin JS и Wasm, Playwright `kotlin-parity-matrix` с тестом пяти временных переходов и P08, Chromium/Firefox/WebKit | PASS: 6/6 для каждой ветки. |
| `xcodebuild -quiet ... build`, iPhone 17 Pro Simulator | PASS: сборка exit 0; SwiftUI app установлен и запущен. |
| Android Emulator `emulator-5554` | PASS: APK установлен и запущен. |
| Визуальный осмотр новых экранов, реальные устройства, screen readers, независимый Reviewer | NOT RUN. Попытка открыть `gpt-6-sol` Architect для следующего нативного этапа отклонена лимитом потоков. |

Таблица глагольного вида также читает `reference.aspectRows` из пакета. Для каждого ряда сохранена опора-инфинитив и три формы; отсутствие настоящего времени у совершенного вида задано `null`. Тест неизвестного глагола дал RED 1/1, затем GREEN. Kotlin compiler первоначально отверг smart cast nullable `present` между модулями; локальное значение исправило компиляцию всех Compose targets.

| Проверка после переноса вида | Результат |
| --- | --- |
| `npm test`, `npm run typecheck`, `npm run build`, `npm run course:inventory:check`, `git diff --check` | PASS: 82/82 React unit, TypeScript, production build, inventory 33 файлов, diff. |
| `npm run test:browser -- tests/browser/react-baseline.spec.ts` | PASS 12/12. |
| Kotlin shared Desktop/JS/Wasm/iOS Simulator tests, Compose Desktop, Android APK, Kotlin JS/Wasm production distribution | PASS после smart-cast fix. Webpack сохранил предупреждения о размере. |
| Kotlin JS и Wasm Playwright: новые authored aspect/tense и P08, Chromium/Firefox/WebKit | PASS 9/9 для каждой ветки. |
| Xcode Debug iPhone 17 Pro Simulator, Android Emulator | PASS: нативные сборки установлены и запущены. [Android матрица](artifacts/android/course-aspect-native.png) осмотрена: видны строки времени/вида и подписанные красная старая и синяя новая части. Экран новых строк SwiftUI отдельно визуально не осматривался. |
| Реальные устройства, screen readers, независимый Tester/Reviewer, полная редакторская классификация | NOT RUN / открыто. |

Мужской Biernik вынесен в `reference.maleAccRows`: для человека, животного и предмета пакет хранит исходную/целевую группу, пример предложения и правило. React, Kotlin Web, Desktop, Android и SwiftUI читают один набор. Неверный пример дал RED 1/1 и затем GREEN в семантическом валидаторе. Android [снимок](artifacts/android/course-male-acc-native.png) показывает подписи, старые красные и новые синие формы.

В ходе iOS проверки обнаружено, что вариант `robiłem / robiłam` не получал подсветки: fallback запрещал её при косой черте. Тесты React/common добавлены с наблюдаемым RED; теперь обе стороны отмечаются как целые формы без ложной подсветки окончания. iOS snapshot test сначала выявил это расхождение, затем прошёл после исправления. Первая браузерная проверка 320 px на Kotlin JS/Wasm упала из-за чтения `scrollWidth` сразу после изменения viewport (старый layout: +960 px); отдельный Chromium запуск с исходным viewport 320 подтвердил ширину 320. Проверка заменена на ожидание перерасчёта layout и прошла. Параллельный JS/Wasm прогон JS дал 11/12 из-за прерванной навигации и таймаута Firefox/WebKit; отдельный повтор адресного JS-сценария прошёл 3/3.

| Проверка после переноса мужского Biernika | Результат |
| --- | --- |
| `npm test`, `npm run build`, `npm run course:inventory:check`, `git diff --check` | PASS: 85/85 React unit, production build, инвентаризация 33 файлов, diff. Новый тест сравнил четыре авторские пары с грамматическим движком. |
| `npm run test:browser -- tests/browser/react-baseline.spec.ts` | PASS 12/12. |
| Kotlin shared Desktop/JS/Wasm/iOS Simulator tests, Compose Desktop, Android APK, Kotlin JS/Wasm production distribution | PASS после исправления альтернативных форм; Webpack предупреждает о размере. |
| `xcodebuild -quiet ... build`, iPhone 17 Pro Simulator; Android Emulator | PASS: сборки установлены и запущены. Android новая схема визуально осмотрена; SwiftUI нет. |
| Адресный Kotlin JS/Wasm Playwright на мужской Biernik и ширину 320 px | PASS 3/3 на Wasm и 3/3 на повторном отдельном JS прогоне в Chromium/Firefox/WebKit. Параллельный JS прогон дал 11/12 с запусковыми сбоями; не считать его чистым полным suite. |
| Полный Kotlin Web Playwright после общей правки подсветки, запуск веток по очереди | PASS 144/144 на Wasm и 144/144 на JS в Chromium/Firefox/WebKit. |
| `CourseReferenceMaleAccTest`, Kotlin Desktop/JS/Wasm/iOS Simulator | PASS: авторские пары `nom → acc` совпали с морфологическим движком на четырёх targets. |
| Реальные устройства, assistive technology, независимый Reviewer и остальные этапы плана | NOT RUN / открыто. |

### 24 сентября · нативный словарь и карточки системной схемы

`VocabularySession` держит одно состояние и сериализует изменения отдельного словарного документа. Mac пишет отдельный JSON атомарно, Android использует `AtomicFile`, iOS — отдельный `NSUserDefaults`-адаптер. Нативные хосты показывают оба направления, раскрытие, ручные оценки, выбор слов, редактор и системные импорт/экспорт. Свайпы на Android/iOS доступны после раскрытия, кнопки оценки остаются. Приёмочные common/Desktop тесты независимого Tester добавлены на запись, read-back, отмену coroutine и восстановление при неизвестном результате записи. Reviewer сначала нашёл два Major-дефекта (зависший busy после отмены и риск повторной оценки после ошибки read-back); после исправления подтвердил отсутствие Major при повторном чтении. Первый iOS UI-тест ошибочно ожидал, что оценённая карточка сразу снова будет due; после исправления теста адресный повтор прошёл.

Четыре карточки карты системы (`noun`, `agreement`, `verb`, `modifiers`) теперь находятся в `reference.systemCards` одного пакета. Schema и семантический валидатор проверяют состав и точный порядок ID. React и Kotlin web/Desktop/Android/iOS читают авторские заголовки, пояснения и примеры из пакета; Android показывает компактные строки, SwiftUI теперь все четыре. Независимый Tester добавил проверку видимого React DOM и прогнал актуальные Kotlin targets. Независимый Reviewer одобрил этот срез без Critical/Major.

| Проверка и среда | Результат |
| --- | --- |
| `npm test`, `npm run typecheck`, `npm run build`, `npm run course:validate`, `npm run course:inventory:check`, корень | PASS: React 89/89 после карточек схемы, TypeScript, production build, валидация и воспроизводимая инвентаризация. |
| Независимый Tester: `npm test -- tests/course-system-cards.test.ts tests/course-reference-system-cards.test.ts tests/course-validation.test.ts`; Kotlin focused Desktop, принудительно свежие JS/Wasm shared browser и iOS Simulator snapshot tests | PASS: React 24/24; Desktop card 1/1, JS/Wasm course test по 1/1, iOS matrix snapshot 5/5. |
| `ANDROID_HOME=/Users/german/Library/Android/sdk ./gradlew :shared:desktopTest :shared:jsBrowserTest :shared:wasmJsBrowserTest :shared:iosSimulatorArm64Test :composeApp:desktopTest :androidApp:testDebugUnitTest :androidApp:assembleDebug :composeApp:wasmJsBrowserDistribution :composeApp:jsBrowserDistribution`, `kotlin/` | PASS: 159 Gradle tasks; production JS/Wasm Webpack предупреждает о размере бандлов. |
| `npm run test:browser -- tests/browser/react-baseline.spec.ts`, корень | PASS 12/12. |
| Полный `npx playwright test --config=playwright.kotlin.config.ts` с `KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/wasmJs/productionExecutable`, `KOTLIN_SPIKE_BRANCH=wasm`, корень | PASS 144/144 в Chromium/Firefox/WebKit на итоговом `systemCards` пакете. |
| Та же команда с `.../dist/js/productionExecutable`, `KOTLIN_SPIKE_BRANCH=js`, `KOTLIN_SPIKE_PORT=4175` | PASS 144/144 в Chromium/Firefox/WebKit; JS fallback реально загружен. |
| Android Emulator `emulator-5554`, актуальный APK | PASS: `adb install -r`, `am start`, словарная карточка после reveal осмотрена; [снимок словаря](artifacts/android/native-vocabulary-screen.png). [Схема](artifacts/android/course-system-cards-native.png) осмотрена на экране матрицы. |
| Xcode Debug build и адресный SwiftUI UI-тест, iPhone 17 Pro Simulator | PASS: `xcodebuild -quiet ... build`; после удаления прежнего приложения `xcodebuild ... -parallel-testing-enabled NO -only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testNativeVocabularyRevealAndBinaryRating test` — 1/1. [Снимок раскрытой карточки](artifacts/ios/native-vocabulary-iphone.png) экспортирован из XCTest attachment. Предыдущий запуск того же теста FAIL: на симуляторе оставалась уже оценённая карточка `noun.wife`, поэтому `vocabularyReveal` отсутствовал; XCTest failure trace это подтвердил. |
| Mac: Desktop UI/unit и фактический запуск окна | PASS: проверки и запуск на локальном Mac; клавиатура и узкое окно вручную NOT RUN (нет разрешения Accessibility для UI-автоматизации). |
| Физические устройства, VoiceOver/TalkBack, мобильные браузеры/IME, file picker round-trip, полный редакторский контроль остальных литералов | NOT RUN / открытые gate. |

### 24 сентября · схема трёх шагов из пакета

В `reference.pipeline` перенесены заголовок и три авторских шага `intent → case → agreement`; компактная строка собирается из вопросов, пример задан явно, чтобы сохранить прежние два коротких текста нативных хостов. React сохранил три блока и стрелки, Kotlin web/Desktop/Android — прежние компактные строки, SwiftUI получает их через iOS snapshot. Schema требует непустые значения и точную структуру, семантический валидатор отвергает перестановку ID. Независимый Tester добавил три случая пустых после trim видимых полей; Reviewer одобрил итоговый diff без Critical/Major.

| Проверка и среда | Результат |
| --- | --- |
| `npm test`, `npm run typecheck`, `npm run build`, `npm run course:validate`, `npm run course:inventory:check`, корень | PASS: итоговые React 97/97, TypeScript, production build, пакет и 33-файловая инвентаризация. У разработчика до трёх тестов Tester было 94/94. |
| Независимый Tester: focused React `course-reference-pipeline` + `course-validation`; React browser сценарий; Kotlin common и JS/Wasm browser сценарий | PASS: focused 30/30; React Chromium 1/1; Kotlin JS/Wasm по 3/3 в Chromium/Firefox/WebKit. В его адресном Gradle запуске JS был UP-TO-DATE от предыдущего PASS, Wasm выполнен заново. |
| Разработчик: shared Desktop/JS/Wasm/iOS Simulator tests, Compose JS/Wasm/Desktop/Android compile, JS/Wasm production distributions, `kotlin/` | PASS, Gradle `BUILD SUCCESSFUL` за 2m42s; focused Playwright на JS/Wasm по 3/3 в Chromium/Firefox/WebKit. Первое ожидание browser test ошибочно искало focused mode при цепочке, исправлен только тест; повтор PASS. |
| `xcodebuild ... -only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testNativeTrainingMatrixAndProgress test`, iPhone 17 Pro Simulator | PASS, exit 0; тест проверил компактную строку на экране матрицы, переходы тренировки и прогресс после перезапуска. |
| `ANDROID_HOME=/Users/german/Library/Android/sdk ./gradlew :androidApp:assembleDebug`, `kotlin/`; `adb install -r`, `am start`, Android Emulator | PASS: свежий APK установлен и открыт. [Снимок компактной схемы](artifacts/android/course-pipeline-native.png) осмотрен. |
| Полный Kotlin Web Playwright suite 144/144 после этого последнего изменения; физические устройства и assistive technology | NOT RUN; последний полный browser suite был перед `reference.pipeline`, адресные сценарии после неё PASS. |

### 24 сентября · четыре строки «Опоры на русский»

`reference.russianSupport` хранит четыре авторских сопоставления с точным порядком `accusative → instrumental → locative → possessive`. Реальные отличия формулировок React, Kotlin web и Desktop таблиц сохранены как именованные варианты; Android/iOS используют одни и те же четыре компактные строки. Schema и семантический валидатор отвергают пропуск варианта, пустой текст, неправильный ID/порядок и `mobileLine` без соответствующей русской опоры. Независимый Tester проверил итоговые source/tests и добавил четыре точных SwiftUI UI-утверждения; Reviewer одобрил diff без Critical/Major.

| Проверка и среда | Результат |
| --- | --- |
| `npm test`, `npm run typecheck`, `npm run build`, `npm run course:validate`, `npm run course:inventory:check`, корень | PASS: 101/101 React unit, TypeScript, production build, пакет и 33-файловая инвентаризация. RED: до pack object точный тест падал; GREEN после переноса. |
| Независимый Tester: focused `course-reference-russian-support` + `course-validation`, React/Kotlin browser parity | PASS: 33/33, обе production ветки JS/Wasm по 3/3 в Chromium/Firefox/WebKit; таблицы сверены с host-specific title/headers/четырьмя строками. |
| Разработчик: shared Desktop/JS/Wasm/iOS Simulator tests, Compose JS/Wasm/Desktop/Android compile, JS/Wasm production distribution, `kotlin/` | PASS, Gradle `BUILD SUCCESSFUL` за 2m59s. Адресный React↔Kotlin browser test по 3/3 на ветке; первоначальный browser locator заголовка исправлен в тесте, повтор PASS. |
| iOS Simulator iPhone 17 Pro, `testNativeTrainingMatrixAndProgress` с четырьмя новыми строками | PASS 1/1, все mobile строки найдены в SwiftUI после прокрутки; локальный результат `kotlin/iosApp/build/Test-RussianSupport-2026-09-24-1142.xcresult`. Первая попытка по имени симулятора завершилась exit 70 до запуска теста из-за неоднозначного destination; повтор по UDID `4384946F-9E6B-43D0-ADA3-CA219A3456B8` PASS. |
| `ANDROID_HOME=/Users/german/Library/Android/sdk ./gradlew :androidApp:assembleDebug`, `kotlin/`; Android Emulator `emulator-5554` | PASS: свежий APK установлен и открыт; [четыре строки](artifacts/android/course-russian-support-native.png) осмотрены. |
| Полный Kotlin Web Playwright suite после `russianSupport`; физические устройства, screen readers, file picker round-trip | NOT RUN / открытые gate. |

### 24 сентября · падежная подсказка и семь существительных для сравнения

`reference.caseTeaching` хранит исходную развёрнутую React-заметку о Wołacz и компактный текст для Kotlin-хостов; `reference.comparisonNounIds` задаёт семь упорядоченных ссылок на nouns. Подсказка чтения браузерной таблицы также находится в пакете. Формы sg/pl, предложения, contrast и выбор упражнения продолжает вычислять прежняя грамматика. Schema и валидатор отвергают пустой текст, повторный/неизвестный noun ID и неверную длину. Независимый Tester добавил SwiftUI UI-сценарий; Reviewer одобрил diff без Critical/Major.

| Проверка и среда | Результат |
| --- | --- |
| `npm test`, `npm run typecheck`, `npm run build`, `npm run course:validate`, `npm run course:inventory:check`, корень | PASS: 104/104 React unit, TypeScript, production build, пакет и инвентаризация. RED: до данных пакета точный тест падал; GREEN после переноса. |
| Независимый Tester: focused React cases+validator, Kotlin JS/Wasm common, P08 browser и iOS UI | PASS: React 35/35; Kotlin focused JS/Wasm (JS UP-TO-DATE после предыдущего PASS, Wasm выполнен); P08 production JS/Wasm Chromium по 1/1; iPhone 17 Pro Simulator `testNativeCasesShowCompactNoteAndOrderedComparisonNouns` 1/1, локальный `Test-CaseTeaching-2026-09-24-1155.xcresult`. |
| Разработчик: shared JS/Wasm/Desktop/iOS Simulator tests, Compose JS/Wasm/Desktop/Android compile и обе production distributions, `kotlin/` | PASS, Gradle `BUILD SUCCESSFUL` за 2m37s; P08 React↔Kotlin JS/Wasm по 3/3 в Chromium/Firefox/WebKit на каждом target. Проверены 7×7 sg/pl и ширина 320 px. |
| `ANDROID_HOME=/Users/german/Library/Android/sdk ./gradlew :androidApp:assembleDebug`, `kotlin/`; Android Emulator | PASS: свежий APK установлен и открыт. [Снимок](artifacts/android/course-case-teaching-native.png) показывает компактную подсказку Wołacz и все семь слов в исходном порядке. |
| Полный browser suite после этого среза, реальные устройства, screen readers, Mac узкое окно/клавиатура | NOT RUN / открытые gate. |

### 24 сентября · девять субъектов глагольной таблицы

`reference.verbTeaching` задаёт девять упорядоченных субъектов, варианты полных и компактных подписей, выбор рода, подписи времён и пояснение составного будущего. React, Kotlin Web, Desktop, Android и iOS snapshot/SwiftUI читают один пакет; формы по-прежнему вычисляет `verbForm`, а выбранный род меняет только `ja/ty/my/wy`. Schema и семантический валидатор проверяют порядок, person/number, режим рода и обязательный текст. Независимый Reviewer одобрил итоговый diff без Major/Critical.

| Проверка и среда | Результат |
| --- | --- |
| `npm test`, `npm run typecheck`, `npm run build`, `npm run course:validate`, `npm run course:inventory:check` | PASS: 106/106 React unit, TypeScript, production build, пакет и инвентаризация. До добавления поля точный тест дал RED; после реализации GREEN. Инвентаризация: 1628 строк-кандидатов из 33 файлов. |
| Независимый Tester: focused React/schema/Kotlin, iPhone 17 Pro Simulator `testNativeVerbGenderControlChangesSelectedSubjectOnly` | PASS: 50/50 focused и iOS UI 1/1. Первая попытка iOS UI дала FAIL из-за test locator: `LabeledContent` объединяет ключ и значение в accessibility label. Tester сверил tree, исправил только locator и повторил PASS; локальный результат `kotlin/iosApp/build/Test-VerbTeaching-Final-2026-09-24-1215.xcresult`. |
| Разработчик: shared Desktop/JS/Wasm/iOS Simulator tests, Compose JS/Wasm/Desktop/Android compile, JS/Wasm distributions; React↔Kotlin P08 | PASS: Gradle target/host checks; production JS/Wasm P08 по 3/3 в Chromium/Firefox/WebKit. Проверены 9 imperfective verbs × 2 выбранных рода × 9 субъектов × 3 времени. |
| `ANDROID_HOME=/Users/german/Library/Android/sdk ./gradlew :androidApp:assembleDebug`, Android Emulator `emulator-5554` | PASS: свежий APK установлен и открыт. [Снимок](artifacts/android/course-verb-teaching-native.png) осмотрен: показаны секция, выбор рода, пояснение будущего и первая из девяти карточек. Остальные карточки отдельно на Android не прокручивались. |
| Полный Kotlin Web browser suite после этого среза, физические устройства, screen readers, Mac узкое окно/клавиатура | NOT RUN / открытые gate. |

### 24 сентября · независимый повтор этапов 7–8

Независимый Tester вернулся к прежнему FAIL P11: после однократного отказа записи migration marker перезагрузил Kotlin Web, увидел состояние восстановления, повторил перенос через интерфейс и ещё раз перезагрузил страницу. Backup, legacy raw и число reviews остались неизменны; состояние стало Ready. Полный протокол и hash проверенных JS/Wasm bundles — в [Stage7-8-Retest.md](Stage7-8-Retest.md). Production-код этого повтора не менялся; добавлены только регрессионные тесты.

| Проверка и среда | Результат |
| --- | --- |
| `TrainingStoreTest`, Kotlin JS/Wasm; `ProgressFixtureParityTest`, Kotlin JS/Wasm | PASS: 17/17 и 5/5 на каждой цели, включая reset после удержанной записи и отказ устаревшей оценке. |
| Playwright marker/retry на production JS/Wasm, Chromium/Firefox/WebKit | PASS: migration spec 9/9 на каждой ветке, включая перезагрузку до и после повтора. |
| Progress ledger и P05 браузерные сценарии на production JS/Wasm | PASS: 17/17 ledger на каждой ветке в Chromium; duplicate-rating и reload 2/2 на каждой ветке. Остальные случаи ledger покрыты common JS/Wasm fixture tests. |
| `npm run typecheck`, `git diff --check` | PASS на финальном прогоне. Первый typecheck дал FAIL из-за параллельной незавершённой работы `reference.pronounTeaching` в `course.ts`; после её исправления PASS. |
| Полный browser suite после новых тестов, реальные мобильные браузеры, независимый Reviewer | NOT RUN для этого ретеста; эти gate не закрыты. |

### 24 сентября · местоименные контексты и притяжательная демонстрация

`reference.pronounTeaching` задаёт девять упорядоченных личных местоимений, пять конструкций, варианты пояснений и три падежа демонстрации семи владельцев. React, Kotlin Web/Desktop/Android и iOS snapshot/SwiftUI используют эти данные, оставляя `possessiveForm` и `nounPhrase` источником форм. Отдельно исправлена Android-строка `Mówię o…`, которая прежде повторяла предлог перед locative. Независимый [Tester](Stage-PronounTeaching-Tester.md) проверил видимый вывод всех доступных хостов; Reviewer одобрил итоговый source/test diff без Critical/Major.

| Проверка и среда | Результат |
| --- | --- |
| Developer: точный RED→GREEN, `npm test`, `npm run typecheck`, `npm run build`, `npm run course:validate`, `npm run course:inventory:check` | PASS: RED 2 поведенческих failure при отсутствии `reference.pronounTeaching`; GREEN focused 16/16, полный React unit 122/122, TypeScript, build, validator и свежая инвентаризация 1624 строк-кандидата из 33 файлов. |
| Kotlin shared Desktop/JS/Wasm/iOS Simulator tests, Compose Web/Desktop/Android compile, совместный JS/Wasm browser distribution | PASS у Developer; `xcodebuild` SwiftUI Simulator build PASS. Первый Xcode build по имени destination не нашёл однозначный simulator, повтор с UDID прошёл. |
| Независимый Tester: React/schema focused, Kotlin JS/Wasm common, P08 browser parity, Desktop UI | PASS: 51/51 focused React, 2/2 common на каждой JS/Wasm цели, 3/3 browser на каждой ветке в Chromium/Firefox/WebKit, Desktop UI 1/1. Первое browser expectation ошибочно искало только `after` внутри FormContrast; тест исправлен без production-правки, повтор PASS. |
| Android Emulator 15/API 35, `:androidApp:assembleDebug`, install/launch, UIAutomator | PASS: видимы `Mówię o… nich` с одним `o` и NOM/ACC/GEN владельца; [снимок](artifacts/android/course-pronoun-teaching-native.png) осмотрен. Первая команда без `ANDROID_HOME` завершилась до компиляции; повтор с SDK PASS. |
| iPhone 17 Pro Simulator, targeted XCTest | PASS 1/1, `kotlin/iosApp/build/Test-PronounTeaching-Final-2026-09-24-1244.xcresult`. Первая попытка на другом simulator зависла до старта XCTest и была остановлена; следующая достигла assertions, но test-only locator не прокрутил off-screen кнопку. После исправления прокрутки повтор PASS. |
| Root: полный Kotlin Web Playwright на production Wasm и forced JS | PASS 153/153 на каждой ветке в Chromium/Firefox/WebKit после Tester browser changes. |
| Физические устройства, VoiceOver/TalkBack, реальные мобильные IME, формальные web/native gate исходного плана | NOT RUN / остаются открытыми. По уточнению пользователя дальнейшую проверку устройств выполняем на симуляторах и эмуляторах. |

### 24 сентября · пять шагов учебной цепочки и нативный индикатор завершения

`training.chainPresentation` содержит пять упорядоченных подписей и отдельные варианты текста завершения для React, Kotlin Web и нативных интерфейсов. Упражнения и их связь продолжает строить прежний генератор; `reference.chainRows`, FSRS и progress schema не менялись. Независимый [Tester](Stage-ChainPresentation-Tester.md) прошёл 12 наборов × 5 шагов в браузере и нативные сценарии. На Android он обнаружил, что завершённая цепочка с пятью сохранёнными оценками показывала `4 / 5` и около 80%; [снимок до исправления](artifacts/android/course-chain-presentation-native.png) фиксирует дефект. Отображаемый count теперь выводится из completed state для Android/Desktop/iOS; [снимок после](artifacts/android/course-chain-presentation-after-fix.png) показывает 5/5 и полную полосу. Reviewer одобрил итоговый diff без Critical/Major.

| Проверка и среда | Результат |
| --- | --- |
| Developer RED→GREEN, `npm test`, `npm run typecheck`, `npm run build`, course validate/inventory | PASS: новый behavioral test сначала упал из-за отсутствия `training.chainPresentation`; после подключения focused 39, полный React unit 125/125, TypeScript/build/validator/inventory. Инвентаризация после native fix: 1631 строка-кандидат из 33 файлов. |
| Kotlin shared JS/Wasm/Desktop/iOS Simulator tests, Compose JS/Wasm/Desktop/Android compile, Xcode Simulator build | PASS у Developer. Дополнительный Desktop test воспроизвёл RED `4 / 5` при реальном `ChainComplete/index4`, затем GREEN после общей display projection; Android APK и SwiftUI build после исправления PASS. |
| Независимый Tester: validator negatives, P02/P05 browser parity production JS/Wasm | PASS: 2/2 negatives; 12×5 связанных шагов и сохранение 60 отзывов после reload, 3/3 на каждой ветке в Chromium/Firefox/WebKit. Проверены разные React/Web completion body. |
| Desktop Compose UI, iPhone 17 Pro Simulator XCTest | PASS после исправления по 1/1: Desktop проверил 5/5 и progress semantics 1.0 на реальном index4; iOS — пять оценок, пять ответов, 5/5 и сохранённый счётчик после relaunch. |
| Android Emulator 15/API 35, пять reveal→rating, force-stop/relaunch | Первый UI прогон **FAIL**: 4/5 после пяти оценок при сохранённом `totalReviews` 11. После исправления **PASS**: 5/5, полная полоса, пять ответов и `totalReviews` 11→16→16 после restart. UIAutomator сверил точный заголовок. |
| Физические устройства, VoiceOver/TalkBack, реальные мобильные IME, полный browser suite после этого native-only исправления | NOT RUN / открытые gate. Предыдущий полный Kotlin Web suite после партии 3 был 153/153 на JS и Wasm; затронутые shared JS/Wasm тесты после native fix PASS. |

### 24 сентября · фиксированные притяжательные формы и будущее `być`

Из прежних TS/Kotlin таблиц в пакет вынесены 196 склоняемых форм четырёх владельцев, три неизменяемые формы и шесть форм будущего `być`. Исходный [oracle](../../tests/fixtures/kotlin-parity/grammar.json) с 490 `G-POSS` и 386 `G-VERB` случаями не изменён; его SHA-256 `f17084de1f877860701b43614668104c4f5817923465b2d4d70fec44bdacbfb9`. Общие grammar API сохранили поведение и ошибки; генератор, FSRS и progress schema не менялись. Независимый [Tester](Stage-MorphologyForms-Tester.md) обнаружил вводящий в заблуждение путь schema error из противоположной `oneOf`-ветки; после перехода на `if/then/else` тот же негативный набор прошёл. Reviewer одобрил итоговый diff без Critical/Major.

| Проверка и среда | Результат |
| --- | --- |
| Developer RED→GREEN, `npm test`, `npm run typecheck`, `npm run build`, course validate/inventory/extract | PASS: два focused теста сначала упали из-за отсутствующих полей pack; после реализации 138/138 React unit, TypeScript/build, validator, extractor и свежая инвентаризация 1638 строк-кандидатов из 33 файлов. Root повторил `npm test`: 138/138. |
| Независимый Tester: React pinned grammar/exercise/negative tests | PASS 19/19; все 490 G-POSS и шесть auxiliary из неизменённого fixture, прежние ошибки unknown verb/perfective present и точные пути ошибок валидатора. |
| Kotlin JS/Wasm/Desktop/iOS Simulator shared tests | PASS: на каждом target 20 методов GrammarParity с 490 G-POSS + 386 G-VERB assertions и пять TrainingParity, без failures/skips; JS/Wasm и Desktop были отдельно принудительно выполнены после UP-TO-DATE прогона. |
| Актуальные production JS/Wasm distributions и React↔Kotlin Playwright | PASS: affected consumers 12/12 и полная P08-матрица 3/3 на каждой ветке в Chromium/Firefox/WebKit. Проверены sg/pl падежи, владельцы, девять субъектов и три времени с выбором рода. |
| Android Emulator и iPhone 17 Pro Simulator | PASS: Android APK установлен; пять реальных оценок дают точные предложения, 5/5 и сохранённый `totalReviews` 16→21→21 после restart. iOS targeted XCTest 1/1, пять оценок и сохранение +5 после relaunch. |
| Реальные устройства, VoiceOver/TalkBack, полный browser suite после Party5 | NOT RUN / остаются открытыми. Запрошены виртуальные устройства; этот срез проверен адресными сценариями на всех затронутых host. |

### 24 сентября · завершение инвентаризации учебного пакета pl-ru

Партия 6 вынесла последние проверенные учебные тезисы в `reference.matrixIntroduction`, `reference.contextHelp`, `reference.maleAccIntro`, `reference.aspectNoPresent`, `reference.webCaseCompositionHeader` и варианты `vocabulary.instructions`/`unavailableLabel`. React, Kotlin Web/Desktop/Android, iOS snapshot и SwiftUI читают соответствующие поля; строка Web «Местоимение + прилагательное + существительное» была отдельно найдена Reviewer как ложное UI-исключение и тоже перенесена в пакет. Прежние польские grammar/exercise fixtures, FSRS и формат прогресса не менялись. Точный независимый протокол — [Stage-InventoryCompletion-Tester.md](Stage-InventoryCompletion-Tester.md).

Инвентаризация теперь рекурсивно охватывает **101 production-файл** и **2365 строк-кандидатов**. TypeScript AST и интерполяционный Kotlin/Swift scanner раскрывают **4504 отдельных token/JSX-text частей**; для каждой есть ключ path/symbol/source fingerprint/occurrence, категория и конкретная причина. Узкая категория `inactive-prototype` разрешена только пяти файлам старого Compose spike; checker запрещает монтирование `SpikeScreen` в приложении. Проверка pack pointer требует собственного строкового листа, точного имени поля в token и отсутствия полной второй копии авторского текста в исходниках. Эти автоматические гарантии не заменяют редакторское чтение; Tester отдельно просмотрел спорные русские фразы и все новые группы.

| Проверка и среда | Результат |
| --- | --- |
| Root: `npm test`, `npm run build`, `npm run course:inventory:decisions:check`, `git diff --check` | **PASS**: 154/154 React/validator/inventory tests; TypeScript/Vite production build и course validator; 101 файл, 2365 candidates, 4504 tokens без unknown/stale/mixed; diff без whitespace ошибок. Первый build до обновления `.d.mts` был **FAIL** на типе `Inventory.schemaVersion`; декларация исправлена, тот же build повторён и **PASS**. |
| Независимый Tester: generator/checker negatives и pinned React parity | **PASS** 17/17, включая JSX без кавычек, внутренние строки TS/Kotlin/Swift multiline, вложенную интерполяцию, неверные pack pointers и изменения source fingerprint. |
| Kotlin JS/Wasm browser, React ↔ Kotlin точные host variants | **PASS на фактически обслуженном старом `build/dist` артефакте**: 3/3 на каждой ветке в Chromium/Firefox/WebKit; полный Kotlin Web Playwright suite — JS 156/156 и Wasm 156/156. Исправление доказательства: Party6 выполнила `jsBrowserProductionWebpack`/`wasmJsBrowserProductionWebpack`, но не `jsBrowserDistribution`/`wasmJsBrowserDistribution`; обслуженные `composeApp.js` имели время 12:28 до последнего переноса Web-заголовка. Поэтому эти прогоны не подтверждают финальный Party6 bundle. Последующий Step3 ретест собирает и обслуживает действительно свежий `build/dist`; исходные device/assistive gate открыты. |
| Desktop Compose, Android Emulator, iPhone 17 Pro Simulator | **PASS**: Desktop UI 1/1; Android install/launch/UIAutomator проверил матрицу, native CEFR и статус Top100 ([снимок](artifacts/android/course-inventory-native.png)); iPhone XCTest 1/1 проверил matrix intro и iOS CEFR. iOS UI-сценарий unavailable-label **NOT RUN**. |
| Физические Android/iPad/iPhone, VoiceOver/TalkBack, реальная мобильная IME, публикация | **NOT RUN** по выбранной пользователем проверке на виртуальных устройствах; соответствующие gate исходного [Plan.md](Plan.md) остаются открытыми. |

Независимый Reviewer прочитал итоговый diff и дал **APPROVED** без Critical/Major. Найденные им до исправления ложное UI-исключение Web-заголовка и пробелы исходной инвентаризации, pack pointer и вложенной интерполяции устранены и повторно проверены. Gate полноты **пакета pl-ru** принят; это не закрывает web parity, физические устройства и доступность исходного плана.

### 24 сентября · явный контраст во всех строках матрицы

Следующий срез шага 3 добавил пять авторских сравнений в четыре строки «Опоры на русский» из `courses/pl-ru/course.json`: `moja żona → moją żonę`, `moja żona → moją żoną`, `żona → żonie`, а для владельца отдельно `moją żonę → jego żonę` и `moją żonę → ich żonę`. `beforeParts`/`afterParts` точно восстанавливают формы; validator и Kotlin loader отвергают неправильные сегменты. React, Kotlin Web, Desktop/Android Compose и SwiftUI показывают подписанные «Было → Стало» пары с красной пунктирной и контрастной сплошной линией. Сгенерированные строки падежей, 7×7 сравнения, глаголов, местоимений и притяжательных форм используют существующий грамматический генератор без изменения польских ответов или FSRS. [Независимый протокол Tester](Stage-ContrastCompletion-Tester.md) фиксирует адресную приёмку до последней CSS-правки.

| Проверка и среда | Фактический результат |
| --- | --- |
| Root после реализации: `npm test`, `npm run build`, `npm run course:inventory:decisions:check` | **PASS**: 163/163 теста, TypeScript/Vite build и валидатор; инвентаризация текущего среза — 101 production-файл, 2431 candidates, 4608 tokens, без незакрытых решений. |
| Независимый Tester: свежие `jsBrowserDistribution`/`wasmJsBrowserDistribution`, Playwright Chromium/Firefox/WebKit | **PASS** адресная матрица 21/21 на JS и 21/21 на Wasm; C4 темы/320 px по 3/3 на каждую ветку; no-answer-leak и пять оценок по 1/1 JS/Wasm Chromium. `npm` focused 22/22; Gradle shared JS/Wasm/Desktop/iOS Simulator, Compose Desktop и Android APK **PASS**. |
| Независимый Tester: Android Emulator и iPhone 17 Pro Simulator | **PASS**: Android UIAutomator обнаружил пять авторских пар и пример сгенерированной пары, снимки показывают разные линии; iOS targeted XCTest 2/2 на авторских/сгенерированных парах. Нативная тёмная тема и реальная речь VoiceOver/TalkBack **NOT RUN**. |
| Root: первый полный suite на свежем JS distribution | **FAIL** 320px/20px: `document.scrollWidth=824`, затем после CSS fix первый полный прогон 155/156 с неповторившимся P08 failure. Адресный P08 1/1 **PASS**. Разработчик привязал абсолютный `.contrast-sr-only` к `position:relative` контейнеру пары в React и Kotlin Web; до/после в браузере 824→320. |
| Developer после CSS fix; Root полный свежий suite | **PASS**: Developer original 320px/20px и support-parity по 3/3 на каждой JS/Wasm ветке в трёх движках, React responsive 1/1, JS/Wasm distribution и React build. Root: повторный полный JS **156/156**, полный Wasm **156/156** в Chromium/Firefox/WebKit; оба использовали обновлённый `build/dist`. |
| Review и неисполненные gate | Независимый Reviewer одобрил исходный Step3 diff, затем снял одобрение и указал CSS overflow как **Major**. После исправления и нового Tester evidence тот же независимый Reviewer повторно прочитал итоговый source/test diff и дал **APPROVED** без Critical/Major 24 сентября. Физические устройства, реальная IME и screen readers **NOT RUN** по выбору виртуальных проверок. |

Виртуальная/browser-приёмка Step3 и финальный независимый review завершены; физические/accessibility gate исходного плана остаются открытыми. Методики и словарные функции из [CoursePacksPlan.md](CoursePacksPlan.md) выполняются отдельно.

### 24 сентября · полный цикл двух методик

По [архитектурному handoff](MethodologyCycleBlueprint.md) все 16 навыков получили из `course.json` четыре этапа для обеих подач: вводное наблюдение на первой встрече, самостоятельный ответ, обратную связь и решение о повторении. Переключение «Схемы и логика» / «Живые ситуации» не меняет задание, ввод, FSRS ID или сохранённый прогресс. React/Kotlin Web и нативные host показывают выбранные тексты; Desktop/Android/iOS продолжают использовать системные контролы. Ответ остаётся скрытым до действия reveal, а контекстная таблица во время обязательного вводного шага не показывает целевую форму.

| Проверка и среда | Фактический результат на текущем срезе |
| --- | --- |
| Developer TDD и root unit | **PASS**: наблюдавшиеся RED на отсутствии этапов/раннем раскрытии, A5 неверном времени подсказки, A6 потерянном фокусе после Continue и A2 утечке ответа через контекстную таблицу; после исправлений root `npm test` **172/172** в 34 файлах, `npm run typecheck` **PASS**. `case.gen.neg` теперь нейтрален к настоящему и прошедшему. |
| Общие данные и инвентаризация | **PASS**: `npm run course:inventory:decisions:check` классифицирует **2467 кандидатов / 4682 части** из 101 production-файла; учебные этапы находятся в одном pack. `npm run build` проходил после A5/A6, после A2 React build выполняется независимым Tester перед браузерной проверкой. |
| Независимый Tester: редактура и browser | **PASS**: независимый A5 RED→GREEN 1/1; React 17/17; свежие Kotlin JS и Wasm по 21/21 адресно в Chromium/Firefox/WebKit на каждую ветку для методик/тренировки, включая фокус, скрытую справку, восстановление справки после Continue и 320 px; P02 12×5 цепочек на JS Chromium 1/1. **Полные** свежие Kotlin Web suites: JS **159/159**, Wasm **159/159** в Chromium/Firefox/WebKit, каждый около пяти минут. Hash и время обслуженных distributions фиксирует Tester; старые test locators адаптированы к обязательному вводному шагу, без изменения product source. |
| Нативные host | Developer: Desktop focused tests и Compose Desktop/Android compile, Kotlin iOS snapshot и SwiftUI Simulator build **PASS**. Независимый Tester: Desktop Compose test **PASS**; Android Emulator `emulator-5554` прошёл смену метода, сохранение черновика, reveal и одну оценку ([прогресс](artifacts/stage4/android-method-progress.png)); iPhone 17 Pro Simulator XCTest первого ввода и расширенного цикла **PASS 1/1** каждый, после диагностических test-only locator правок [снимок](artifacts/stage4/ios-method-revealed.png) и дерево доступности показывают frozen draft. После отдельной native UX-правки Android свежий APK и iPhone targeted XCTest **PASS**: вводная справочная кнопка недоступна и нейтральна, после Continue таблица и подпись восстанавливаются ([Android до перехода](artifacts/stage4/android-next-intro-neutral.png), [после](artifacts/stage4/android-reference-restored.png)). Последний Desktop test `BUILD SUCCESSFUL`, задача `UP-TO-DATE`; source assertion проверен Tester. Физические устройства, реальная IME и VoiceOver/TalkBack **NOT RUN** по выбору виртуальных проверок. |
| Независимый Reviewer | Исходное чтение нашло **Major A2**: React/Kotlin Web справка раскрывала форму на вводном шаге. После исправления и финального Tester report повторное чтение Step4 A1–A7, native UX-поправки и Step3 CSS-fix завершилось **APPROVED** без Critical/Major. Reviewer сам тесты не запускал. |

Независимый [Tester report](Stage-MethodologyCycle-Tester.md) принимает виртуальные/browser критерии A1–A7, финальный Reviewer одобрил итоговый diff. Реальные устройства и assistive technology остаются отдельными открытыми gate. План следующего каталога — [VocabularyCatalogBlueprint.md](VocabularyCatalogBlueprint.md); его реализация не включена в эти проверки.

### 24 сентября · словарный каталог и редакторский журнал

Для шага 5 из одного частотного индекса получаются вложенные топ-100/500/1000. После независимой построчной проверки и восьми уточнений прежние 32 ID сохранены; 32 записи журнала имеют `approved`, ещё 970 рангов остаются кандидатами. Реальное покрытие — **7/100, 23/500, 30/1000**; две готовые карточки вне топ-1000. A1/A2 обозначают локальные группы курса, не сертификацию CEFR. [Редакторский отчёт](Stage-Vocabulary-Editorial-Tester.md) даёт первичные ссылки и пределы языковой сверки; [тестовый протокол V1–V5](Stage-VocabularyCatalog-Tester.md) фиксирует команды, артефакты и платформенные статусы.

Первый независимый Reviewer обнаружил **Major V5**: импортированное пользовательское `w` заполняло недоступный частотный ряд и делало его выбираемым при счётчике 7/100. Tester воспроизвёл RED в React и Kotlin JS (93 недоступных по подписи, 92 реально выключенных). Developer изменил четыре lookup-пути: top-N берёт только готовые карточки пакета, а пользовательское слово с прежним ID/историей остаётся в «Мои слова». После этого `npm test` прошёл **192/192**, строгая `course:validate`, typecheck, React build и адресные Kotlin JS/Wasm/desktop/Android/iOS сборки — **PASS**; независимый Tester получил React GREEN 1/1, свежие JS/Wasm по 6/6 каждая в Chromium/Firefox/WebKit с проверкой обслуженных файлов, Desktop Compose и Android Emulator GREEN. На iOS после fix адресный snapshot-тест с импортированным `w` и карточкой FSRS прошёл в общем simulator suite **203/203**: top-100 недоступен, пользовательское слово и история сохранены. Свежий post-fix iPhone Simulator XCTest после чистой пересборки runner прошёл **1/1** для счётчика и отключённой строки; полный SwiftUI UI-проход именно с импортированным `w` **NOT RUN**, его snapshot проверен отдельно. Финальный независимый Reviewer дал **APPROVED_WITH_MINOR** без Critical/Major; замечание о старой формулировке статуса в этом документе исправлено. Реальные устройства, IME, screen readers и системный file picker round-trip **NOT RUN**.

### 24 сентября · режим слов и перенос четырёх историй

Два ID — готовое `noun.wife` и собственное слово — дают четыре отдельные FSRS-записи по направлениям `ru-pl`/`pl-ru`. [Закреплённый fixture](../../tests/fixtures/vocabulary-four-cards.json) сравнивается по полным объектам после импорта, экспорта и restart. Новая custom-карточка с нормализованной леммой уже готового/собственного слова отклоняется до записи; старый custom с совпадением после обновления поставляемого каталога остаётся доступным со своим ID. React и Kotlin Web могут восстановить повреждённый документ прямым импортом исправленного v1. Контракт — [Step6 blueprint](VocabularyModesCompletionBlueprint.md), независимые действия и ограничения — [Tester report](Stage-VocabularyModes-Tester.md).

| Проверка и среда | Фактический результат |
| --- | --- |
| Root: authoring и React | **PASS**: `course:validate`; inventory checker **2488 кандидатов / 4727 частей** после обновления снимка; `npm test` **198/198**, `npm run typecheck`, `npm run build`; React Playwright **20/20**. Изначальный `npm run test:browser` направлял Kotlin specs на React preview — конфигурация ограничена React specs, после чего штатная команда PASS. |
| Root: Kotlin browser | **PASS**: полные JS и Wasm production suites по **171/171** в Chromium/Firefox/WebKit. HTTP-отданные `composeApp.js` побайтно совпали с соответствующими distribution. Compatibility loader в отдельном smoke реально загрузил обе ветки `originWasmComposeApp.js` и `originJsComposeApp.js`. [Hash manifest](artifacts/final-virtual/manifest.json) и [Wasm log](artifacts/final-virtual/kotlin-wasm-playwright.log). |
| Root: Gradle | **PASS** с установленным `ANDROID_HOME` и JDK 23: shared JS/Wasm/Desktop по **189** тестов, iOS Simulator **207**, Compose Desktop **36**, Android unit **9**, debug APK и три web distributions; XML-отчёты без failures/errors/skips. Первый запуск без SDK завершился до выполнения; отдельная попытка с JDK 21 была остановлена на Kotlin/Native setup ради Xcode UI-прогона, не засчитана. [Gradle log](artifacts/final-virtual/gradle-full-jdk23.log). |
| Независимый Tester: native system files | **PASS** на packaged Mac, Android 15 Emulator и iPhone 17 Pro Simulator: через реальные Open/Save, GetContent/CreateDocument и SwiftUI Files импорт/экспорт двух ID, custom, четырёх FSRS объектов и restart. Wrong pair, malformed JSON, 10 000 001 байт и cancel на каждом нативном хосте сохранили исходный raw документ; на iPhone файлы созданы штатным recovery exporter, чтобы Files их индексировал. |
| Независимый Tester: recovery | **PASS**: Mac/Android/iPhone показали повреждённый словарь, отдали исходный UTF-8 raw через системный экспорт без изменения байтов, приняли исправленный файл и после restart восстановили обе выбранные карточки и четыре истории. Android произвольные невалидные UTF-8 байты не проверены; текстовый reader может их заменить. |
| Reviewer и внешние устройства | Финальный независимый Reviewer Step6 ещё выполняется. Физические устройства, реальная польская IME и VoiceOver/TalkBack **NOT RUN** по выбранному виртуальному scope. Шаги 7–9 и исходная общая матрица P01–P12 остаются отдельной приёмкой. |

После этой таблицы Reviewer обнаружил **Major** в переносе старого v1-файла в действительно пустой профиль: пользовательское слово, лемма которого позднее появилась во встроенном каталоге, отклонялось вместе с выбранным ID и историями FSRS. Developer зафиксировал узкое правило восстановления в [Step6 blueprint](VocabularyModesCompletionBlueprint.md) и наблюдал RED → GREEN в React/Kotlin. В заполненном профиле новые конфликты по-прежнему отклоняются до записи. На исправленном срезе `npm test` прошёл **201/201**, React browser **21/21**, Kotlin JS и Wasm browser по **174/174** в Chromium/Firefox/WebKit, shared JS/Wasm/Desktop по **191** тесту, iOS simulator **209**, Android unit **9**. Независимый Tester импортировал legacy-файл через настоящий Android `GetContent`, подтвердил ID, обе истории, restart и недоступность неготовых частотных строк. [Post-fix Gradle log](artifacts/final-virtual/gradle-post-legacy-fix.log), [Wasm browser log](artifacts/final-virtual/kotlin-wasm-post-legacy-fix.log) и [Android artifacts](artifacts/final-virtual/android-legacy-after.json) относятся к этому срезу. После этого новые iPhone/iPad UI-повторы и Mac UI тесты зафиксированы ниже; [manifest](artifacts/final-virtual/manifest.json) пересчитан после них.

### 24 сентября · финальная виртуальная приёмка CoursePacks 7–9

[Независимая матрица Tester](FinalVirtualAcceptance-Tester.md) на текущем SwiftUI-коде `aa71a10b…` фиксирует финальные iPhone **14/14** и iPad **14/14** XCTest без ошибок после исправления перекрытия кнопки клавиатурой. На iPad отдельно прошёл **1/1** typed input/reveal при увеличенном системном шрифте, тёмной теме и альбомной ориентации. Полные XCTest summaries напечатаны, но `xcodebuild` после них завис в `simctl diagnose` и был остановлен; это доказательство завершённых методов, а не утверждение о завершённом `.xcresult`/exit 0. Адресный iPad P12 завершился exit 0. Отдельный iPhone Reduce Motion с системным readback и XCTest прошёл **1/1**, exit 0; тест проверил одно повторение и сохранение после restart, настройка симулятора восстановлена. Независимый Mac `:composeApp:desktopTest` на свежем коде прошёл **38/38**: добавлены проверки сохранения typed draft при смене метода и узкой Compose-области 320dp с крупным текстом; свежий `:composeApp:createDistributable` **PASS**. Упакованный `.app` запущен, но UI-автоматизация окна недоступна из-за macOS assistive access, поэтому packaged narrow-window/large-text/reduced-motion interaction **NOT RUN**. Физические устройства, настоящая польская IME и VoiceOver/TalkBack также **NOT RUN**.

[Итоговый manifest](artifacts/final-virtual/manifest.json) сопоставляет текущие байты **122** production/test файлов и **13** артефактов, включая React JS, Kotlin JS/Wasm и compatibility bundles, Android APK, iPhone/iPad `PolskiGrammar.debug.dylib` и Mac app/shared JAR. Source-tree SHA-256 `f643c916ae460b58baf5404c4df9ebe4345cc30f7be35397ae6090c205851108`; хеши и размеры повторно сверены с файлами. Manifest доказывает идентичность байтов, а запуск/тесты — отдельные логи Tester/Root. Независимый Reviewer Step6/Steps7–9 не нашёл Critical/Major в коде, но **не принял полный виртуальный gate** из-за отсутствия host UI взаимодействия с упакованным Mac-приложением. Историческую [матрицу Parity](Parity.md) не повышать до общего PASS по одному счётчику тестов. Новый [план UI/UX](PlatformUXPlan.md) и [handoff настроек](PreferencesBlueprint.md) относятся к будущей работе и не меняют эти результаты.
