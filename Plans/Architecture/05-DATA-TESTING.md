# 05. Data pipeline и архитектура тестов

Дата: 2026-11-08. Оценка по `biome-dsl/AI/PRINCIPLES.md` (P7, P15, P16, P17, P20), `BestPractice.md` §8, `ProjectMigratioPrincipals.md`.
Пути от корня polski. Kotlin - от `kotlin/`. Всё запущенное помечено **RAN**, прочитанное - **READ**, остальное - `[не проверено]`.
Дополняет `03-DSL-COVERAGE.md` (там 3.5 назвал Python-генераторы кандидатом №1; здесь факты о их состоянии).

## 0. Вывод

1. **CI не исполняет DSL-слой вообще (P17, P20).** `kotlin-check.yml` не вызывает `biome generate --check`. Тесты `tests/course-*.test.ts`
   стоят под `describe.skipIf(!hasCli)` (19 файлов); в CI `BIOME_BIN` нет, соседнего checkout нет -> они **NOT RUN**, а не PASS.
   Триггеры `paths:` не содержат `courses-biome/**`, `dsl/**`, `tests/**`: правка `.core-data` не запускает ни одного workflow.
2. **Два из четырёх `generate_parity.py` - мёртвые и опасные (P16, P7).** `grammar/generate_parity.py --check` падает (**RAN**:
   `Stale generated dictionary: .../data/Nouns.kt`), `training/generate_parity.py --check` падает (**RAN**: `React oracle drifted: src/training/generator.ts`).
   Запуск без `--check` у grammar перезапишет рукописные `Nouns.kt/Adjectives.kt/Verbs.kt/Pronouns.kt/Skills.kt` статикой из JSON.
3. **`progress` и `srs` генераторы не имеют `--check`** и молча пишут файл при любом запуске (**RAN**: `Wrote .../SchedulerOracleTest.kt`, diff пустой, т.е. свежие).
   Режима «проверить, не писать» нет -> в гейт их не поставить (P16).
4. **Параллельный живой конвейер уже есть**: `tests/fixtures/nj03-exercise-oracle/biome.project` (4 `derive`-шага) + `scripts/nj03/*.py`.
   **RAN**: `biome generate --check` по обоим проектам = OK, `git status` чист. Но второй проект не в `package.json`, не в CI, не в `AGENTS.md`.
5. Значит ответ на вопрос «Python -> biome derive?»: **уже частично сделано** (TrainingParityTest, TrainingParityEnTest, PolskiTextCodecTest).
   Остаток: Grammar/Progress/Scheduler - переводить на тот же путь, старые `generate_parity.py` удалить.

## 1. Карта потока данных

```
АВТОРИНГ            ВАЛИДАЦИЯ                  ГЕНЕРАЦИЯ                         ПОТРЕБЛЕНИЕ            ТЕСТ
courses-biome/     dsl/*.data-contract        dsl/biome.project                 commonMain/generated/  commonTest/**
 {pl-ru,en-ru,      (course 785 строк,         GENERATE data (4 контракта)  ->   rows/{plru,enru}/      *RowsTest, *Pack*Test
  styles}/*.core-    forms 19, frequency 8,     GENERATE derive derive-forms ->   rows/*/forms/FormRows   FormTablesTest
  data (~10.7k       style 20): ключи, COUNT,   GENERATE derive derive-enru-  ->   data/EnRu*.kt (копии   EnRu*Test
  строк, 41 файл)    DATA_REF, типы             mappers (5 копий PlRu*.kt)         PlRu*.kt)
                    fmt-шаги (3) = layout      GENERATE fmt (только проверка)   PlRu*TypedPack -> shared  ParityTests (оракулы)
                                                                                                            browser/Playwright
```

Легаси: `courses/` (29 tracked файлов) - **не источник курса**. `retired-json-path.test.ts` держит это закрытым. Живые остатки:
JSON-схемы + `lang/*.json`, `core/*.json`, `vocabulary-editorial.json`, `source-inventory.json` для `npm run course:validate`
(`validate-course/curriculum/pack-v2/vocabulary-editorial.mjs`, **READ**). Это **вторая, JSON-овая модель валидации**
рядом с `*.data-contract` (P7: два описания «что такое корректный курс»; P11 для авторских JSON).

Второй конвейер (оракулы): `tests/fixtures/kotlin-parity/*.json` (React-capture, `npm run fixtures:generate`) и
`tests/fixtures/nj03-exercise-oracle/{requirements,requirements-en}.core-data` -> `derive-*.mjs` -> `python3 scripts/nj03/generate-*.py` -> регион
`GENERATED START/END` внутри рукописного `.kt`. Python вызывается из `.mjs` через `execFileSync` (скрытая зависимость от `python3`).

Выходы derive-шагов, лежащие вне `generated/` (`data/EnRu*.kt`, регионы в `*Test.kt`): P16 соблюдается за счёт `--check`, но
реестр `tests/generated-registry.test.ts` ищет их по баннеру `// Generated from`; гибриды с регионом баннера не имеют (**READ**).

## 2. Измерение тестов (**RAN** wc по `src/*Test/`)

| Модуль | строк тестов | примечание |
|---|---|---|
| shared | 14 656 | 113 файлов; `commonTest` + `desktop/ios/macosTest`; **GrammarParityTest 2069** (14% модуля) |
| composeApp | 2 043 | 16 файлов, `TrainingStoreTest` 543, `DesktopScreenTest` 507 |
| core-engine | 339 | 5 |
| core-model | 57 | 1 |
| androidApp | 25 файлов | `testDebugUnitTest`, строки `[не проверено]` |
| browserAcceptance | 516 | 6 файлов: `DistributionServer*`, `NoJsonReadiness`, `SettingsRouteSmoke`, `DriverProbe` |
| vitest `tests/*.test.ts(x)` | 3 478 в корне + `fixtures/` | 59 файлов; 19 с `skipIf` |
| Playwright `tests/browser` | 4 813 | 32 spec; `kotlin-*` (27) в `playwright.kotlin.config.ts`, `react-*` (4) в `playwright.config.ts` |
| `tests/perf` | только `android/*.py` | вне любых гейтов |

Тесты данных (`polski/data/*Test.kt`, 41 файл) и `course-*.test.ts` (37) дублируют друг друга по теме (reference, recipes, lifehacks,
pronoun-teaching, chain, forms, styles): TS проверяет `.core-data` через `biome read`, Kotlin - напечатанные строки. Это не лишнее само по себе
(две стороны границы), но одно и то же ожидание живёт в двух языках руками (P7, `ProjectMigratioPrincipals`: места правки).

## 3. Что не ловит ни один тест (gaps)

| # | Правка данных | Кто ловит | Итог | Принцип |
|---|---|---|---|---|
| G1 | Любая правка `courses-biome/**` в PR | Ни один workflow (нет в `paths:`) | **NOT RUN** | P17, P20 |
| G2 | Забыли `biome generate` после правки `.core-data` | Только локальный `generate --check`; в CI нет | устаревший `generated/` мержится | P16, P20 |
| G3 | Невалидная строка по `*.data-contract` (тип, DATA_REF, COUNT) | `generate` -> только локально; CI - `skipIf(!hasCli)` | NOT RUN | P17 |
| G4 | 15-й и далее noun/adj/verb в pl-ru | GrammarParity: 196 noun-кейсов = ровно 14 слов, 490 adj, 386 verb - **замороженный React-срез**; новое слово не покрыто | пропуск | P15 (оракул не растёт с данными) |
| G5 | Ошибка в форме НЕ из замороженных 14 слов, 1000+ `vocabulary`/`frequency` | Контракт ловит форму, не правильность польского | нет внешнего оракула | P15 |
| G6 | en-ru формы | `TrainingParityEnTest`/`EnRuFormTablesTest` = характеризация собственного движка (см. §4) | circular | P15 |
| G7 | Расхождение между `tests/fixtures/kotlin-parity/grammar.json` и `GrammarParityTest.kt` | Нет: генератор сломан (**RAN**), `parity-fixtures.test.ts` сверяет JSON с React `src/`, но не JSON с `.kt` | drift тихий | P7, P16 |
| G8 | Правка `TrainingParityTest.kt` вне региона `GENERATED` | `--check` сравнивает весь файл derive-шага, но шаг лежит во втором проекте, который никто не гоняет | NOT RUN в CI | P16, P17 |
| G9 | `react src/` удалят (сейчас оракул `fixtures:generate`, `parity-fixtures.test.ts`, `training/generate_parity.py` читают `src/`) | Тест, который перегенерирует JSON из React, перестанет собираться; оракул заморозится без владельца | риск при снятии React | P19 |
| G10 | 3 `fmt`-шага + `course-data-layout.test.ts` | Ловят layout; новый файл в `courses-biome/` без `fmt`-имени - тест есть (хорошо) | OK, но тоже `hasCli` | P17 |
| G11 | `deploy.yml` (`npm test` + `npm run build`) | `build` вызывает `course:validate` (JSON-слой), не DSL | валидирует не то, что едет в Kotlin | P7 |

## 4. P15: ожидания, связанные с генерацией

- **Нарушения / риски**
  - `TrainingParityEnTest.kt` (496): шапка прямо говорит, что «expected values record what the shipped engine produced». Ожидание снято с
    самого движка, затем `requirements-en.core-data` стал источником. Это golden из генерата (P15). Пока en-движок не имеет внешнего оракула,
    тест защищает от регрессий, но не доказывает корректность. Метить как **characterization**, не «parity».
  - `EnRuFormTablesTest`, `EnRuRowsTest`: сверка таблиц форм, напечатанных `derive-forms.mjs enru`, с теми же `.core-data`,
    из которых они выведены - тавтология, если ожидание не вынесено в отдельный файл (`[не проверено]` построчно).
  - `StyleSnapshotTest`, `IosMatrixSnapshotTest` (328): снапшоты своего вывода; нужен отдельный коммит с причиной при смене (P15) `[не проверено]`.
- **Соблюдено**
  - pl-ru Grammar/Exercise/Evaluation/Scheduler/Progress: оракул - React-код (`src/`) и `ts-fsrs 5.4.2`, зафиксированы по `sourceRevision`
    `df59774e`, независимы от `derive-forms.mjs` и Kotlin. `course-forms.test.ts` сверяет вывод `derive-forms` с `core-golden/grammar.json`.
  - `requirements.core-data` (112 CASE) подтверждён как внешний вход, derive-шаг только печатает Kotlin.
  - `AGENTS.md` запрещает править `Plans/Biome/*.cases` и `tests/fixtures/*` под выход - политика верная, но принудительной проверки нет:
    нужен `Finding-Regression:`/«changed-expectation» трейлер на коммитах, трогающих `tests/fixtures/**` и `*.core-data` оракула (P21).
- **Острый момент**: `requirements.core-data` и `kotlin-parity/*.json` - два разных формата одного оракула (112 кейсов) в двух местах
  (P7). Третья копия - регион внутри `TrainingParityTest.kt`. Выбрать одно каноническое место (`requirements.core-data`), JSON вывести из него.

## 5. P16: генерат руками

- Рукой правится ли `generated/`? Нет следов (**READ**): баннер есть, `generated-registry.test.ts` требует ровно один шаг на файл,
  `UNREGISTERED` содержит одну запись (`PolskiTextCodecExamples.kt`, «никто не компилирует»).
- **Нарушение**: гибриды (`TrainingParityTest.kt`, `TrainingParityEnTest.kt`, `PolskiTextCodecTest.kt`) - рукописная оболочка + регион. Человек
  легко правит вне региона и в регионе; `--check` сравнивает весь файл, но только локально/во втором проекте (G8).
- **Нарушение**: `GrammarParityTest.kt`, `ProgressFixtureParityTest.kt`, `SchedulerOracleTest.kt` - целиком генерат без баннера и без шага
  `biome.project`; защита - только ручной запуск `generate_parity.py`. Для grammar он ещё и сломан.
- **Хрупкость**: derive-шаг вызывает Python внутри `generate()`; если `python3` другой версии/нет - шаг падает как «derive failed», не как «нет Python».

## 6. Качество тестов

- **Покрытие**: данные курса и движок - густо (tests/course-* + polski/data/*Test). UI-слой - `composeApp` всего 2 043 строки при 16 файлах, ядро
  presentation в `shared`. Пирамида перевёрнута вверх на parity (29% shared), вниз на UI.
- **GrammarParityTest = 1 979 одинаковых строк** `assertEquals("...", run { ... }, "G-NOUN-wife-sg-nom")` (шаблон lambda повторён 1979 раз). Это данные,
  а не тест: заменить таблицей `id|input|expected` в `.examples`/`.core-data` + один цикл в Kotlin (~40 строк). Эффект: файл 2069 -> ~40, места
  правки «добавить кейс» = 1 строка данных (P5/P6, `DSL_EFFECTIVENESS_BOUNDARY`: линейные данные оставлять данными).
- **Независимость**: см. §4. pl-ru хорошо, en-ru слабо.
- **Flakiness-сигналы (**RAN** grep)**:
  - Kotlin-тесты: 0 `Thread.sleep/delay/Random/Clock.System` в `*Test*` - детерминизм хороший (`RandomSource` инъектируется, `Draws`).
  - Playwright: **12 `waitForTimeout`** (ux4 4, flip-card 5, perf 2, preferences 1), в т.ч. 4000 и 2000 мс; `retries: 0`, `workers: 1` (честно,
    но зависит от скорости CI). Заменять на ожидание состояния (`expect.poll`/data-атрибут анимации) или фиксированные часы Compose.
  - `playwright.kotlin.config.ts` требует `KOTLIN_SPIKE_DIST`/`BRANCH`: 27 spec **не запускаются без сборки Kotlin**; `kotlin-check.yml` их не гоняет
    (gradle tests есть, Playwright - нет) `[не проверено: другой workflow]` -> NOT RUN.
  - `browserAcceptance` (516 строк) - это сервер раздачи + smoke; сам acceptance в `tests/browser/*.spec.ts`, разнесён между Gradle и npm.
- **Скипы**: `skipIf(!hasCli)` в 19 файлах -> полный `npm test` в CI и локально без checkout зелёный при нуле проверок данных. Нужен счётчик
  `skipped` в выводе и падение при `CI=true` (P17).

## 7. Python-генераторы: стать ли им derive-шагами?

| Генератор | Вход | Выход | Состояние | Решение |
|---|---|---|---|---|
| `scripts/nj03/generate-exercise-oracle-test.py` | `requirements*.core-data` | регион в `TrainingParity[En]Test.kt` | **живой**, в nj03 project, **RAN OK** | оставить как есть; позже портировать печать в `.mjs` (убрать Python) |
| `scripts/nj03/generate-textwire-tests.py` | `textwire/*.cases` | регион в `PolskiTextCodecTest.kt` | живой | то же |
| `.../grammar/generate_parity.py` | `kotlin-parity/grammar.json` | `GrammarParityTest.kt` + **перезаписывает** `data/Nouns.kt` и др. | **мёртв, --check RED** | удалить; тест заменить таблицей |
| `.../training/generate_parity.py` | `exercises.json`, `evaluation.json`, `git show <rev>:src/...` | `TrainingParityTest.kt` | **мёртв, --check RED**, конкурирует с derive-шагом за один файл | удалить немедленно |
| `.../progress/generate_parity.py` | `progress.json` | `ProgressFixtureParityTest.kt` | живой, нет --check | -> derive-шаг (5 кейсов) |
| `.../srs/generate_parity.py` | `scheduler.json` | `SchedulerOracleTest.kt` | живой, нет --check | -> derive-шаг (21 кейс) |

Критерий (`AGENTS.md`: «DSL только с измеримой выгодой, сравнить с native helper»): derive-шаг оправдан, когда есть `--check` и один источник.
Для `progress`/`srs` выигрыш - только `--check` в общем гейте (P16/P20); сам генератор можно не переписывать, а обернуть тем же
`derive-*.mjs` + `OUT/generate()` шаблоном (~15 строк на шаг). Порт печати в JS - необязателен, пока `python3` прописан как зависимость гейта.
Не нужно: новый синтаксис DSL и новый pack - потребителя `Consumer:` для них нет (P13).

## 8. План улучшений (порядок = ценность/стоимость)

| # | Шаг | Результат | Принцип |
|---|---|---|---|
| 1 | Добавить `courses-biome/**`, `dsl/**`, `tests/**`, `.github/**` в `paths:` `kotlin-check.yml` | Правка данных запускает CI | P20 |
| 2 | Job `data` в CI: checkout `biome-dsl` на закреплённый `TOOLCHAIN` (из первой строки `biome.project`), `BIOME_BIN`, `biome generate --check` для обоих `biome.project`, `biome check changed` | G1-G3, G8 закрыты | P16, P17, P20 |
| 3 | В `describe.skipIf(!hasCli)`: при `CI=true` без CLI - `throw`, не skip | NOT RUN становится FAIL | P17 |
| 4 | Удалить `grammar/` и `training/generate_parity.py` (+ упоминания в `Plans/Kotlin/Stage4/5-Tester.md`, `kotlin/README.md`) | Нет скрипта, способного перезаписать рукописные `Nouns.kt` и т.п. | P16, P7 |
| 5 | Перенести второй проект в `npm run oracle:check`, добавить в `AGENTS.md` и в `gate`/`check`; или влить 4 шага в `dsl/biome.project` (один `--check`) | Один вход проверки | P7, P20 |
| 6 | `progress`/`srs`: `derive-progress-tests.mjs`, `derive-scheduler-tests.mjs` по образцу `derive-oracle.mjs`; баннер `// Generated from` | `--check` есть; `UNREGISTERED`/реестр покрывает | P16 |
| 7 | GrammarParityTest: вынести 1979 кейсов в `tests/fixtures/kotlin-parity/grammar.cases` (или `.core-data`), в Kotlin - цикл по таблице; свежесть - derive-шаг | 2069 -> ~40 строк, места правки 1 | P5, P6, P22 |
| 8 | Пометить `TrainingParityEnTest` как characterization (переименовать, убрать слово parity); завести внешний оракул en (хотя бы 20 слов из словаря) | G6 | P15 |
| 9 | Убрать `waitForTimeout` (12); состояние анимации - в `data-testid`/`aria-busy` | Меньше flaky | P17 |
| 10 | Один слой валидации: перенести `lang/*.json`, `core/*.json`, `vocabulary-editorial.json` в `*.core-data` + контракты; `course:validate` -> `biome generate --check`; удалить `courses/schema` | Конец двойного описания корректности | P7, P11 |
| 11 | `deploy.yml`: добавить `generate --check` перед `npm run build` | Деплой собирает проверенные данные | P20 |
| 12 | Трейлер `Expectation-Change: <причина>` для коммитов, трогающих `tests/fixtures/**`; `scripts/check-expectation-trailer` в CI | Принудительный P15 | P15, P21 |

Шаги 1-4 - малые и независимые. Шаги 5-7 - ~100-150 строк каждый (правило «один агент = один файловый блок»).
Шаг 10 - отдельная ветка, не раньше закрытия 1-3 и 6 (P19).

## 9. Приёмка

- (**CI**) PR с правкой `courses-biome/pl-ru/nouns.core-data` без `biome generate` падает в job `data` (негативный контроль для шага 1-2; P18).
- (**CI**) Без CLI в окружении `CI=true` `npm test` падает с именем пропущенного файла, а не зелёный.
- `git ls-files '*generate_parity.py'` пуст после шагов 4 и 6.
- `generated-registry.test.ts`: `UNREGISTERED` не растёт; `GrammarParityTest/ProgressFixtureParityTest/SchedulerOracleTest` - выходы шагов.
- `grep -rn waitForTimeout tests/browser` = 0.
- `wc -l GrammarParityTest.kt` < 200 (P22), число кейсов в таблице = 1979 (проверка в тесте).
- Показать **RAN**-лог текущего коммита для: `generate --check` (оба проекта), `npm test` (без skipped данных), `./gradlew :shared:desktopTest`.

## 10. Открытые вопросы

1. Есть ли решение держать React `src/` как живой оракул? От этого зависит G9: либо заморозить fixtures как артефакт с `sourceRevision`,
   либо оставить `fixtures:generate` в CI.
2. Где брать biome-dsl в CI: приватный репозиторий (`github biome-dsl`) - нужен токен/submodule/npm-пакет; решение владельца.
3. `courses/pl-ru/source-inventory.json` и `inventory-*.json` ещё нужны `course:inventory:check`; когда inventory считается завершённой?
4. Нужен ли `tests/perf` (только Android Python) в гейте - сейчас без владельца.
