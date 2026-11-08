# Единый план архитектуры polski-grammar-srs

Дата: 2026-11-08. Синтез пяти анализов: модули (01), UI (02), DSL (03), анимации (04), data/тесты (05).
Оценка по `biome-dsl/AI/PRINCIPLES.md` P1-P25.

## Масштаб проекта

| Мера | Значение |
|---|---|
| Authored Kotlin | 38 435 строк (51% — тесты) |
| Generated Kotlin | 27 242 строк (data-contract → rows) |
| Платформы | Android, Desktop, Web/DOM, iOS, macOS |
| Gradle-модули | 9 (androidApp, composeApp, shared, core-model, core-engine, browserAcceptance, build-logic, nj05ReadOnlyProbe) |
| DSL сегодня | 4 data-contract, 15 GENERATE-шагов, 1 Logic (swipe) |
| Дублирование UI | 16 Android / 15 Web / 4 Desktop / 1 Common экран |

## Топ-проблемы (по серьёзности)

| # | Проблема | Принцип | Источник |
|---|---|---|---|
| 1 | CI не исполняет DSL-слой; 19 vitest скипаются тихо | P17, P20 | 05 |
| 2 | 2 мёртвых Python-генератора могут перезаписать рукописный код | P16 | 05 |
| 3 | Effect-таблица Rive написана 4 раза; macOS дрейфнул | P7 | 04 |
| 4 | Экраны UI написаны 3–5 раз; 81 строка в ≥2 хостах | P7, P1 | 02 |
| 5 | Глобальный `packRegistry` вместо DI; store не тестируется с 2 паками | P12, P9 | 01 |
| 6 | `shared` — монолит: данные + SRS + progress + presentation + iOS-мосты | P9, P22 | 01 |
| 7 | Swipe.logic не под `--check`; дрейф SwipePolicy.kt не ловится | P16, P20 | 03 |
| 8 | Польское правило `m-personal` в нейтральном core-engine | P10 | 03 |
| 9 | GrammarParityTest: 1979 assertEquals — данные, притворяющиеся тестом | P5, P6, P22 | 05 |
| 10 | Два описания «корректный курс»: data-contract + JSON-схемы | P7, P11 | 05 |

## Фазы

### Фаза 0: Защита (1–2 дня)

Цель: CI ловит дрейф данных, мёртвый код удалён.

| Шаг | Что | Принцип | Усилие |
|---|---|---|---|
| 0.1 | `paths:` в `kotlin-check.yml`: добавить `courses-biome/**`, `dsl/**`, `tests/**` | P20 | S |
| 0.2 | Job `data` в CI: checkout biome-dsl на TOOLCHAIN SHA, `biome generate --check` оба проекта | P16, P17 | M |
| 0.3 | `describe.skipIf(!hasCli)` → throw при `CI=true` | P17 | S |
| 0.4 | Удалить `grammar/generate_parity.py` и `training/generate_parity.py` (мёртвые, опасные) | P16 | S |
| 0.5 | macOS: заменить `again.riv` на 2249 B версию, триггеры → `Check`/`Error` | P7 | S |
| 0.6 | Android Rive: `onDispose { views.forEach { it.stop() } }` | P22 | S |

Приёмка: PR с правкой `.core-data` без `generate` → CI RED. `npm test` при `CI=true` без CLI → FAIL.

### Фаза 1: Единый источник правды (3–5 дней)

Цель: каждый факт живёт в одном месте.

| Шаг | Что | Проблема | Принцип | Усилие |
|---|---|---|---|---|
| 1.1 | Swipe: native helper `locksHorizontal` + `AXIS_RATIO` (6 мест → 1). Шаг в `dsl/biome.project` ИЛИ вернуть в натив | #7 | P7, P25 | S |
| 1.2 | `classify()` → data rows в `course.data-contract` (ядро без польских литералов) | #8 | P10 | S |
| 1.3 | Дубль `SkillQueue` → один (проверить тай-брейк) | 01§4 | P7 | S |
| 1.4 | `UiCopy` — каталог подписей в shared (18 строк ≥4 хостов, затем 30 ≥3) | #4 | P7 | M |
| 1.5 | Rive effects → единый line-DSL файл + derive-скрипт → Kotlin/Swift/JS | #3 | P7, P16 | M |
| 1.6 | `RiveGate.decide` — единая гейтинг-функция в shared | 04§D2 | P7 | S |
| 1.7 | Единый `.riv` источник + `sync-rive-assets --check` | 04§D1 | P7, P16 | S |

Приёмка: grep подписей `UiCopy` в хостах = 0. Effect-таблица из 1 файла на 4 хоста. `--check` падает при ручной правке.

### Фаза 2: Консолидация модели (5–7 дней)

Цель: решения не дублируются в рендерерах.

| Шаг | Что | Проблема | Принцип | Усилие |
|---|---|---|---|---|
| 2.1 | `TrainingSessionFactory` — создание store из 5 мест в 1 | 01§5 | P7, P12 | M |
| 2.2 | `CoursePackContext` вместо глобала (дефолт = глобальный реестр) | #5 | P12, P9 | M-L |
| 2.3 | `TrainingCardModel` — pure function `AppUiState → секции/кнопки/hotkeys` | #4 | P7, P10 | M |
| 2.4 | Перевод хостов на модель: Desktop → Android → Web → Swift snapshot | #4 | P7 | L |
| 2.5 | `MatrixTableViewModel`: довести — убрать 3 ручных построения таблицы справки | 02§2.6 | P7 | M |
| 2.6 | Native `accepts(action, state)` в TrainingStore (10 условий → 1 функция) | 03§3.4 | P10 | S |

Приёмка: `intervalLabel` — 1 копия. Desktop без `if (styleId ==`. `TrainingStoreTest` зелёный с 2 паками.

### Фаза 3: Данные и тесты (3–5 дней)

Цель: оракулы под `--check`, данные — данные.

| Шаг | Что | Проблема | Принцип | Усилие |
|---|---|---|---|---|
| 3.1 | `progress`/`srs` генераторы → derive-шаги с `--check` | 05§7 | P16 | M |
| 3.2 | GrammarParityTest: 1979 assertEquals → таблица `.core-data` + цикл (~40 строк) | #9 | P5, P22 | M |
| 3.3 | Второй проект nj03 → в `npm run oracle:check` и `AGENTS.md` | 05§4 | P7, P20 | S |
| 3.4 | Пометить `TrainingParityEnTest` как characterization (не parity) | 05§4 | P15 | S |
| 3.5 | Убрать 12 `waitForTimeout` в Playwright → ожидание состояния | 05§6 | P17 | M |
| 3.6 | Трейлер `Expectation-Change: <причина>` для коммитов в `tests/fixtures/` | 05§4 | P15, P21 | S |

Приёмка: `generate --check` покрывает оба проекта. `GrammarParityTest.kt` < 200 строк. `grep waitForTimeout` = 0.

### Фаза 4: Структура (5–10 дней, после стабилизации P19)

Цель: модули отражают слои.

| Шаг | Что | Проблема | Принцип | Усилие |
|---|---|---|---|---|
| 4.1 | `:apple-bridge` — вынести `iosMain`/`macosMain` из shared (~950 строк) | #6 | P9 | M |
| 4.2 | `android.app` без `:shared`, только через `:composeApp` | 01§9 | P9 | S |
| 4.3 | Gradle: build-logic для задач и констант (`compileSdk`, `minSdk`) | 01§7,8 | P7, P22 | M |
| 4.4 | Разрез shared: `:srs-progress` (чистое ядро) + `:course-data` (генерат) | #6 | P9 | L |
| 4.5 | Vocabulary: решить владельца (common = desktop-only → переместить) | 02§5 | P7, P9 | S |
| 4.6 | Desktop Compose: заморозить (только багфиксы) до решения по macOS | 02§6 | P19 | S |
| 4.7 | Убрать двойной слой валидации: `courses/schema` JSON → `.core-data` + контракты | #10 | P7, P11 | L |

Приёмка: `:srs-progress` не импортирует `polski.data`. `check:boundaries`-аналог (Gradle dependency graph). `courses/schema` удалён.

## Что DSL решает, а что нет

| Область | DSL | Native | Основание |
|---|---|---|---|
| Данные курса | **уже DSL** (data-contract) | — | V3+V4, работает |
| Тестовые оракулы | **кандидат** (`.core-data` + derive) | Python-генераторы | V2 (`--check`), V4 |
| Swipe-решение | **проигрывает** (S1+S4) | native helper `locksHorizontal` | 03§3.2 |
| `classify()` | **data rows** (расширение контракта) | — | P10 |
| Action gating | **условно** (после native рефакторинга) | `accepts()` сначала | 03§3.4 |
| FSRS, нормализация, reducer | — | **native** (S1/S3/S5) | 03§3.7-3.9 |
| UI экраны | — | **native** + модель в shared | P13 (1 продукт), scope.md |
| Rive | derive-скрипт (effect table) | физика анимаций | P25 S5 |
| Кодеки/снапшоты | — | **native** (COMMAND/WIRE = NO-GO) | 03§3.12 |

## Общий порядок

```
Фаза 0 (защита)  →  Фаза 1 (единый источник)  →  Фаза 2 (модель)
                                                    ↕
                                                 Фаза 3 (тесты)  →  Фаза 4 (структура)
```

Фазы 2 и 3 можно вести параллельно (разные файлы). Фаза 4 — после стабилизации (P19).

## Решения владельца (2026-11-08)

1. **CI**: DSL генерируется локально, CI-шаги генерации не нужны. Шаги 0.1–0.3 убраны.
2. **Vocabulary**: делать по BestPractice на все платформы включая Web.
3. **Desktop**: заморозить сейчас (только багфиксы).
4. **macOS Rive**: забытый — заменить.
5. **React `src/`**: удалить, делать по BestPractice Kotlin Multiplatform.

## Метрики успеха (не строки — P5)

| Метрика | Сейчас | Цель |
|---|---|---|
| Мест правки на «добавить строку UI» | до 5 | 1 (UiCopy) + рендереры |
| Мест правки на «изменить effect» | 4 | 1 |
| Мест создания `TrainingStore` | 5 | 1 (фабрика) |
| Копий `intervalLabel` | ≥2 | 1 |
| Файлов >200 строк в composeApp | 6 | 0 |
| Тихих NOT RUN в CI | 19 | 0 |
| Python-генераторов вне `--check` | 4 (2 мёртвых) | 0 |

## Прогресс (2026-11-08)

### Phase 0: Защита

| Шаг | Статус | Примечание |
|---|---|---|
| 0.1–0.3 CI | УБРАНЫ | Решение владельца: DSL генерируется локально |
| 0.4 Мёртвые генераторы | DONE | Удалены `grammar/generate_parity.py`, `training/generate_parity.py` |
| 0.5 macOS Rive | DONE | `again.riv` заменён, `chain-complete.riv` добавлен, триггеры → `"State Machine 1"`/`"Check"`/`"Error"`, `RiveChainCompleteOverlay` добавлен |
| 0.6 Android Rive dispose | DONE | `onDispose { views.forEach { it.stop() } }`. ВНИМАНИЕ: `DisposableEffect(effect)` re-key'ится при `onConsumed()` → stop() может обрезать анимацию. Проверить на устройстве; безопаснее `DisposableEffect(Unit)` |
| React `src/` удаление | DONE | Удалены: `src/` (14 файлов), 23 React-зависимых теста, react/react-dom из dependencies, `@testing-library/react`/`@types/react*`/`@vitejs/plugin-react` из devDeps, `vite build` из `build` скрипта, `src` из tsconfig.app.json include, script tag из index.html |
| Desktop freeze | DONE | Записан в AGENTS.md |

Все изменения **не закоммичены** — в working tree polski. 36 TS-тестов остались (не зависят от `src/`). `progress/` и `srs/generate_parity.py` оставлены (работают, нужен `--check` позже).

### Phase 1: DSL-миграция по KPI (2026-11-08)

План: `Plans/Architecture/DSL-MIGRATION-PLAN.md`. Порядок: G → E → B → A → C → D → F.

| Блок | Статус | Примечание |
|---|---|---|
| G: progress/srs → derive | DONE | `--print` в Python, derive-обёртки, GENERATE шаги в biome.project |
| E: classify → data rows | DONE | `classify()` удалён, ReasonRuleRow с whenGender/whenNomSingularSuffix/whenUnchanged, P10 закрыт |
| B: RiveGate.decide | DONE | Единая `RiveGate.skip()` в shared, 2×2×2 тест, Android/Web подключены |
| A: Rive effects → DSL | DONE | effects.data-contract + card-effects.core-data + derive → Kotlin/Swift/JS, 4 хоста на generated |
| C: .riv sync | DONE | `assets/rive/` канон + `sync-rive-assets.mjs --check`, macOS fixed (again 5428→2249, chain-complete added) |
| D: UiCopy каталог | DONE | 12 констант + intervalLabel в shared, 20 Kotlin-файлов обновлены |
| F: GrammarParityTest → .core-data | DONE | 2069→57 строк, 1979 rows в grammar.core-data, grammar-oracle.data-contract |

Все изменения **не закоммичены** — в working tree. Требуется `biome generate` для обновления generated файлов (CourseRows.kt, GrammarCaseRows.kt). Gradle/Xcode NOT RUN.

## Детальные планы

- [01-MODULE-STRUCTURE.md](01-MODULE-STRUCTURE.md) — модули, зависимости, state management
- [02-UI-CONSOLIDATION.md](02-UI-CONSOLIDATION.md) — дублирование экранов по платформам
- [03-DSL-COVERAGE.md](03-DSL-COVERAGE.md) — где DSL помогает, а где нет
- [04-ANIMATIONS-RIVE.md](04-ANIMATIONS-RIVE.md) — Rive интеграция и эффекты
- [05-DATA-TESTING.md](05-DATA-TESTING.md) — data pipeline и тестовая архитектура
