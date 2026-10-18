# Polski → Biome DSL: план исполнения миграции

Статус: **A завершён; B1–B2 пилот завершён; B продолжается**. Основание: `AI/ProjectMigratioPrincipals.md`, `NJ03PragmaticCourseCutoverDecision.md`, `KotlinOnlyCutoverAmendment.md`, `NoJsonFullPortPlan.md`. Ветка: `experiment/nj03-exercise-oracle`.

## Текущий статус

| Срез | Статус | SHA | Доказательство |
|---|---|---|---|
| S1 whitespace/BOM, vocabulary trim | **DONE** main | `3ae9df8` | desktopTest 29/29 |
| S2 shared swipe decision | **DONE** main | `db0728e` | desktopTest + framework build |
| COMMAND/WIRE | **STOP** main | `ae614a0` | measured NO-GO |
| A1–A6: exercise oracle 112 cases | **DONE** | `6e3c6ab` | desktopTest 5/5, RED/GREEN |
| B1: freeze 7 JSON SHA-256 | **DONE** | `f80c5cc` | hashes verified |
| B2 пилот: 16 pl skills round-trip | **DONE** | `f80c5cc` | 16/16 identical |
| biome.project + derive --check (P16) | **DONE** | uncommitted | `biome generate --check` PASS |
| biome-dsl gate:all `7ba4a05a` | open | — | build/typecheck PASS; full open |

## Слой каждого правила (ARCHITECTURE §3)

| Намерение | Слой | Инструмент | Статус |
|---|---|---|---|
| Exercise ожидания (112 cases) | `.sources` / oracle | `biome.project` derive → Python → Kotlin `@Test` | **DONE** |
| SwipeDecision (порог, ratio, направление) | Logic DECISION | `.logic` → `biome logic generate --target kt` | NOT RUN; сейчас нативный Kotlin (STOP P8) |
| Course skills/curriculum (16+16) | Данные, Core reader | Biome record/list literals + typed reader | B2 пилот: pl-ru 16 DONE |
| Course forms, recipes, pairs | Данные | Одноразовый конвертер → Biome literals | NOT RUN |
| User data codec | Нативный | Kotlin text codec за контрактом | NOT RUN (Блок C) |
| Freshness check генератов | `biome.project` derive | `GENERATE derive --check` | **DONE** |

## Блок A: Exercise oracle ✅

Все шаги A1–A6 завершены. Коммит `6e3c6ab`.

```
requirements.core-data (112 CASE, 116 строк)
    ↓  biome.project → derive-oracle.mjs → generate-exercise-oracle-test.py (65 строк)
TrainingParityTest.kt::allExerciseCases() (560 строк, между маркерами)
    ↓  ./gradlew :shared:desktopTest
    5/5 PASS
```

**NOT RUN**: JS/Wasm, iOS, macOS, Android, English exercises oracle.

## Блок B: Course catalog

**Принципы (из аудита Biome capabilities):**
- Данные курса → Biome record/list literals, читает Core typed reader — не кастомный парсер
- Каждый генерат → `biome.project` с `GENERATE derive --check` (P16)
- Классификационные правила → `.logic` DECISION (будущие навыки)
- `.tables` — hard NO-GO (всегда эмитирует JSON)

| Шаг | Действие | Инструмент | Проверка | Статус |
|---|---|---|---|---|
| B1 | Freeze SHA-256 для 7 JSON | Python script | SHA verified | **DONE** |
| B2 пилот | pl-ru 16 skills → Biome literals | `convert-course-json.py` | 16/16 round-trip | **DONE** |
| B2 full | Все секции course.json → Biome literals | Расширить конвертер | Round-trip каждой секции | TODO |
| B3 | `CourseValue` Kotlin IR | NEW `CourseValue.kt` | Компиляция | TODO |
| B4 | Kotlin printer: Biome IR → `CourseValue` конструкторы | `biome.project` derive → JS/Python printer | `biome generate --check` | TODO |
| B5 | Заменить `CourseData.kt` JsonObject → `CourseValue` | Kotlin edit | `desktopTest` GREEN | TODO |
| B6 | Заменить `CoursePackLoader.fromV2Layers` | Kotlin edit | `desktopTest` GREEN | TODO |
| B7 | Один Gradle task вместо двух JSON embedders | `build.gradle.kts` | `desktopTest` GREEN | TODO |
| B8 | Удалить 11 JSON, старые Node скрипты | Удаление | no-JSON scan clean | TODO |
| B9 | RecipeLoader, EnLexicon, Curriculum, Lifehack, StyleRecipe | Kotlin edit | `desktopTest` GREEN | TODO |
| B10 | English exercise oracle: 16 skills + chain | NEW `.core-data` | `desktopTest` GREEN | TODO |
| B11 | Pair-lifetime scenario | NEW test | Frozen outputs match | TODO |

**Следующий: B2 full** — конвертация оставшихся секций `course.json`.

## Блок C: User data

**Зависимость**: Блок B завершён, wire protocol выбран.

| Шаг | Действие | Статус |
|---|---|---|
| C1 | Wire protocol selection (TextWireDesign.md A/B/C) | NOT RUN |
| C2 | Text codec: Progress, Vocabulary, Preferences | NOT RUN |
| C3 | Legacy JSON reader (read-only) | NOT RUN |
| C4 | Migration path + receipt + cleanup | NOT RUN |
| C5 | Host tests: browser, Android, Desktop, iOS, macOS | NOT RUN |
| C6 | Delete old JSON writers | NOT RUN |

## Использование Biome DSL (аудит по ARCHITECTURE §3)

| Возможность | Используем | Почему не иначе |
|---|---|---|
| `biome logic generate --target kt` | НЕТ (SwipeDecision нативный) | STOP по P8; при следующем похожем правиле — `.logic` |
| `biome test generate --lang kt` | НЕТ | Нет шва `Host.open()` к `PlExerciseEngine` — честный NOT RUN |
| `biome.project` + `GENERATE derive --check` | **ДА** (exercise oracle) | P16 freshness — единственный правильный путь |
| Core record/list literals + typed reader | **ДА** (course data) | Biome native syntax, не кастомный парсер |
| `.contract` с FACT | НЕТ | Нет DECISION/FEATURE consumer для exercise типов |
| `.examples` / `.sources` формат | NOT RUN | Следующий срез при English oracle |

## Правила исполнения

1. Намерение → таблица слоёв (ARCHITECTURE §3) → ближайший corpus пример
2. Любой генерат → `biome.project` с `GENERATE derive --check` (P16)
3. Генератор печатает только переменную часть (P1/P6)
4. diff, не перечитывать (экономия контекста)
5. Отчёт = таблица с числами и NOT RUN
