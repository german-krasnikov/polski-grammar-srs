# Polski → Biome DSL: план исполнения миграции

Статус: **план, NOT RUN**. Основание: `AI/ProjectMigratioPrincipals.md`, `NJ03PragmaticCourseCutoverDecision.md`, `KotlinOnlyCutoverAmendment.md`, `NoJsonFullPortPlan.md`. Ветка: `experiment/nj03-exercise-oracle` @ `d96bf34`.

## Текущий статус

| Срез | Статус | SHA |
|---|---|---|
| S1 whitespace/BOM, vocabulary trim | **DONE** main | `3ae9df8` |
| S2 shared swipe decision | **DONE** main | `db0728e` |
| COMMAND/WIRE | **STOP** main | `ae614a0` |
| Exercise oracle: 112 cases `.core-data` | Транскрибировано, генератор 65 строк, diff=0 | `d96bf34` |
| CourseValue pilot: RED→GREEN `case.acc.f` | Pilot passed | `9bc0356` |
| biome-dsl gate:all на `7ba4a05a` | Запущен, сборка/typecheck прошли | open |

## Три блока миграции

### Блок A: Exercise oracle (текущий)

**Цель**: 112 exercise-ожиданий управляются из `.core-data`, генерируются в Kotlin, проходят `desktopTest`.

| Шаг | Действие | Вход | Выход | Проверка |
|---|---|---|---|---|
| A1 | Подключить генератор к файлу | `requirements.core-data` (112 CASE) | Тело `allExerciseCases()` в stdout | `diff` с существующим = 0 ✅ |
| A2 | Скрипт сборки: `python3 generate-exercise-oracle-test.py > body`, вставка в `TrainingParityTest.kt` между маркерами | Маркеры `// --- GENERATED START ---` / `// --- GENERATED END ---` в `.kt` | Полный `.kt` с ручными тестами + сгенерированным телом | `diff` полного файла |
| A3 | Gradle `desktopTest --tests "*.TrainingParityTest"` | Сгенерированный `.kt` | 5 @Test, 0 failures | PASS/FAIL |
| A4 | Мутация: изменить одно ожидание в `.core-data`, регенерировать, запустить | Мутированный oracle | RED: один assertion fail | Видимый RED |
| A5 | Восстановить oracle, регенерировать, запустить | Оригинал | GREEN | PASS |
| A6 | Коммит + SHA oracle hash в `.kt` header | — | Один коммит | — |

**NOT RUN после A**: JS/Wasm, iOS, macOS, Android, browser, English exercises.

### Блок B: Course catalog (один bundle)

**Цель**: курсовые JSON (course.json 145K + pairs + lexicon + curriculum + forms + recipes + styles + lifehacks) → один типизированный Biome source → один Kotlin printer → один Gradle task. По `NJ03PragmaticCourseCutoverDecision.md`.

**Зависимость**: biome-dsl gate:all PASS на `7ba4a05a`.

| Шаг | Действие | Файлы | Проверка |
|---|---|---|---|
| B1 | Заморозить хеши всех 11 курсовых JSON | `courses/**/*.json` | SHA-256 записаны |
| B2 | Одноразовый конвертер: JSON → Biome record/list syntax по файлам/пакам | 11 JSON → `.core-data` файлы | Побайтовое сравнение parsed values |
| B3 | `CourseValue` Kotlin IR: `Record/List/Text/Number/Boolean/Null` | NEW `CourseValue.kt` ~50 строк | Компиляция |
| B4 | Kotlin printer: Biome IR → `CourseValue` конструкторы | NEW `CourseValuePrinter.py` в biome-dsl или проектный скрипт | Генерирует `.kt`, `diff` parsed values = 0 |
| B5 | Заменить `CourseData.kt` JsonObject helpers → `CourseValue` access | 944 строк → ~same с `CourseValue` | `desktopTest` GREEN |
| B6 | Заменить `CoursePackLoader.fromV2Layers` → generated bundle read | 28 строк loader | `desktopTest` GREEN |
| B7 | Заменить `generateCoursePackSource` + `generateFormsFixtureSource` в Gradle → один typed task | `build.gradle.kts` lines 48–228 | `./gradlew :shared:desktopTest` GREEN |
| B8 | Удалить 11 активных JSON, старые `build-pack*.mjs`, `migrate-v1-to-v2.mjs` | Удаление | no-JSON scan clean |
| B9 | RecipeLoader (166), EnLexicon (65), Curriculum (54), Lifehack (130), StyleRecipe (123) → generated bundle | 538 строк суммарно | `desktopTest` GREEN, consumers compile |
| B10 | English exercise oracle: 16 skills + 5-step chain с полными ожиданиями | NEW `.core-data` | `desktopTest` GREEN |
| B11 | Pair-lifetime: старый generator после `pl-ru→en-ru` + свежий en generator | NEW test | Frozen outputs match |

**Порядок**: B1→B2→B3→B4→B5–B6 (вместе)→B7→B8→B9. B10–B11 параллельно с B5+.

**STOP**: второй активный authority, JSON в строке/ячейке, ручная правка генерата, P23 нелинейность.

### Блок C: User data (progress, vocabulary, preferences)

**Цель**: ProgressCodec (185), VocabularyDocument (263), UserPreferencesCodec (162) — переход с JSON wire на текстовый формат. Legacy JSON → read-only migration path.

**Зависимость**: Блок B завершён (course path чист от JSON), wire protocol выбран (`TextWireDesign.md`).

| Шаг | Действие | Файлы | Проверка |
|---|---|---|---|
| C1 | Wire protocol selection: POLSKI-TEXT vs alternatives | `TextWireDesign.md` corpus | Measured A/B/C |
| C2 | Text codec: encode/decode для Progress, Vocabulary, Preferences | 3 Kotlin codecs | Round-trip + edge cases |
| C3 | Legacy JSON reader (read-only) | Existing codecs, restricted | Never writes JSON |
| C4 | Migration path: old JSON → text + receipt + cleanup | Transaction test | Power-fail, retry, old-tab safe |
| C5 | Host tests: browser, Android, Desktop, iOS, macOS | Per-host | Storage + recovery per host |
| C6 | Delete old JSON writers, backup paths | — | no-JSON scan final |

## Чего нет в плане (NOT IN SCOPE)

- UI/UX перенос (уже Kotlin Compose + SwiftUI)
- FSRS алгоритм (нативный, P4 — механический)
- EndingHighlight (display-only, ADR осознанное расхождение)
- Unity/CosMiner (OWNER-DEFERRED)
- Публикация, push, CI (отдельное полномочие владельца)

## Инвентарь затронутых файлов

| Группа | Файлы | Строк | Блок |
|---|---|---|---|
| Course JSON (авторские) | 11 файлов | ~1.1M bytes | B |
| Course Kotlin consumers | CourseData, CoursePackLoader, RecipeLoader, EnLexicon, Curriculum, Lifehack, StyleRecipe | 1510 | B |
| Course Gradle embedders | build.gradle.kts lines 23–228 | ~200 | B |
| Course Node scripts | 11 `.mjs` файлов | ~2500 | B (retire) |
| Exercise oracle | TrainingParityTest.kt | 634 | A |
| User data codecs | ProgressCodec, VocabularyDocument, UserPreferencesCodec | 610 | C |
| TS tests (historical) | 20 files | — | Retire after Kotlin proof |

## Правила исполнения (из ProjectMigratioPrincipals.md)

1. **Обнаружение прогресса** — первый шаг, не перечитывать всё
2. **Генератор = команда** — вызывать, не копировать вывод
3. **diff, не перечитывать** — сравнение через `diff`/hash
4. **Отчёт = таблица** — что сделано, что NOT RUN, числа
5. **P7** — один факт = один источник, не дублировать hardcoded
6. **P11** — никакого JSON в авторских/генерированных/runtime форматах
7. **P15** — ожидания независимы от backend
8. **P16** — генерат не правится руками
9. **P17** — PASS = фактическое исполнение, не компиляция
10. **RED→GREEN** — сначала поведенческий RED, затем GREEN

## Ближайшие действия

1. Завершить **A2–A6** (exercise oracle подключен к Gradle)
2. Дождаться **biome-dsl gate:all** PASS на `7ba4a05a`
3. Начать **B1–B2** (freeze + transcribe course JSON)
