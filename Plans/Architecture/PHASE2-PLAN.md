# Phase 2+ план: синтез 5 архитектурных ревью

Дата: 2026-11-08. Все сборки GREEN после Phase 0 + Phase 1.

## Порядок (выгода/цена, P8)

### Волна 1: мелкие рефакторы (параллельно, ~20-50 строк каждый)

| # | Что | Источник | Мест правки |
|---|---|---|---|
| W1.1 | SkillQueue: удалить training/SkillQueue.kt, collapse rate() | Arch #5 | 3→1 |
| W1.2 | SwipeDecision.locksHorizontal() + AXIS_RATIO | Arch #4, Plan 1.1 | 6→1 |
| W1.3 | UiCopy +2 строки + 4 Swift bridge entries | Arch #2 | 5→1 |
| W1.4 | CardFlipTiming.HALF_MS = 250 в shared | Arch #4 | 3→1 |
| W1.5 | nj03 merge в dsl/biome.project | Arch #3 | 2→1 проекта |

### Волна 2: модель экрана (последовательно, ~150-200 строк)

| # | Что | Источник |
|---|---|---|
| W2.1 | TrainingCardModel в shared/presentation | Arch #2, Plan 2.3 |
| W2.2 | Desktop → модель | Plan 2.4 step 1 |
| W2.3 | Android → модель | Plan 2.4 step 2 |
| W2.4 | Web → модель | Plan 2.4 step 3 |
| W2.5 | Swift snapshot → модель | Plan 2.4 step 4 |

### Волна 3: тесты и data pipeline

| # | Что | Источник |
|---|---|---|
| W3.1 | waitForTimeout → state (7 из 12) | Arch #3 |
| W3.2 | iOS snapshot dedup (~195 строк) | Arch #5 |
| W3.3 | TrainingStore accepts() native refactor | Arch #5, Plan 2.6 |

### Волна 4: модули (Phase 4, после стабилизации)

| # | Что | Источник |
|---|---|---|
| W4.1 | :apple-bridge (iosMain/macosMain из shared) | Plan 4.1 |
| W4.2 | build-logic расширение на shared | Arch #1 |
| W4.3 | androidApp без :shared | Plan 4.2 |
| W4.4 | :srs-progress разрез | Plan 4.4 |

## Решения архитекторов

- **DSL исчерпан** — всё оставшееся native refactoring (Arch #1, #5)
- **TrainingCardModel строить** — 5→2 мест правки, hotkeys внутрь (Arch #2)
- **SkillQueue тройной** — rate() копирует, training/SkillQueue.kt дубль (Arch #5)
- **Swipe native helper** — не DSL, `locksHorizontal()` + const (Arch #4)
- **CI biome --check** — главный пробел в data pipeline (Arch #3)
- **iOS/macOS snapshot** — ~195 общих строк, IosSnapshot копирует shared functions (Arch #5)
