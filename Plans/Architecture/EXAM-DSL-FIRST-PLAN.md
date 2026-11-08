# Экзаменный модуль: DSL-first (A–D)

Дата: 2026-11-08. Отвечает на прямое задание «переделай экзаменный модуль на DSL-first».
Дополняет (не дублирует) `Plans/Architecture/EXAM-MODULES-PLAN.md` (исходный план),
`Plans/Architecture/EXAM-FULL-FLOW-PLAN.md` (flow/timer/§1 EXAM_MODULE, §2 domain promotion —
активная параллельная ветка `arch-exam-flow`) и `Plans/Architecture/EXAM-AUDIO-PLAYBACK-PLAN.md`
(`arch-exam-audio`). Где пересекается — явно указано, не переизобретено.

## Проверенные факты (не предположения)

- Хардкод **не** в `:shared` (вопреки заданию): `ExamData.kt`, `ExamListeningData.kt`,
  `ExamReadingData.kt`, `ExamWritingData.kt` лежат в
  `kotlin/composeApp/src/webMain/kotlin/polski/ui/` — только web-платформа видит их. Grammar
  domain (`GrammarExamModel/Session/Scorer/CardModel.kt`) уже в `:shared commonMain`, корректно.
  `ExamData.kt:109` сам себя помечает: `/** ... Later replaced by biome-generated rows. */`.
- `dsl/exam.data-contract` уже существует и покрывает все 8 нужных `RECORD`. Реальные `.core-data`
  уже лежат в `exams/pl/certyfikat-polski/{B1,B2}/{sample,2017,2019,2020,2022-06,2023,2023-02,
  2023-06,2024-02}/` — не черновики, настоящий текст экзамена (сверено построчно:
  `grammar-items.core-data:g1` = `"a teraz mysla o (czegos, czyms, cos)___ wiekszym"`, то же
  предложение, что в `ExamData.kt:37` `cf("g1", "a teraz myślą o ___ większym", ...)`, только без
  диакритики). `dsl/biome.project` **не** генерирует Kotlin из них — единственная строка
  (`GENERATE exam-b1-sample data --contract exam ...`) закомментирована: `(disabled: sample data
  needs field fixes)`.
- Измерено `biome data check` напрямую (не из `biome.project`, т.к. шаг не подключён):
  - `B1/2019`: **2 проблемы** — `ReadingPassageRow.text is required` (2 строки с пустым `text`,
    пассажи matching/word-bank типов). Контракт верный, пробел в данных.
  - `B2/2019`: **38 проблем** — все `DATA_REF`, `ListeningQuestionRow.taskRef` ссылается на
    `certyfikat-polski-B2-2019-listening-1`, а `ListeningTaskRow.taskId` называется иначе
    (ID-конвенция разъехалась между двумя файлами одной экзаменационной сдачи). Не контракт,
    не структура — опечатка в id.
  - `B1/sample`: **17 проблем**, включая `DATA_SYNTAX: unterminated {` — черновик, объясняет,
    почему шаг был отключён именно на нём.
  Итог: контракт готов; данные почти готовы для B1/2019+B2/2019, остальные сдачи — в работе
  параллельных агентов (`fix-exam-data`, `import-all-exams`, `parse-all-exams`,
  `parse-arkusz-texts`, `parse-b2-2019` видны в списке активных агентов сессии) — не блокирую
  этот план их статусом, подключаю `GENERATE` инкрементально по мере готовности (P17: доказательство
  — исполнение `biome generate --check`, не допущение).
- **Аудио-тайминги — риск потери факта.** `segmentStartMs`/`segmentEndMs` в
  `listening-tasks.core-data` у всех сдач равны `0`/`0` (не размечено). Реальные тайминги
  (`startMs = 85000, endMs = 305000, ...` для B1-2019, из whisper-транскрипции — см. коммит
  `6ad0bf0`) существуют **только** в хардкоде `ExamListeningData.kt`. P7 («один факт — одно
  место»): прежде чем удалять этот файл, тайминги обязаны перейти в `.core-data` как числа —
  иначе это не DSL-first, а потеря данных. Это предусловие раздела A, не частность.
- **Локальный прецедент DECISION→Kotlin уже есть и я его измерил**: `dsl/swipe/swipe.logic`
  (4 правила, 3 факта) → `derive-swipe-policy.mjs` → `kotlin/shared/.../SwipePolicy.kt`
  (генерируемый, **37 строк**) потреблённый `SwipeDecision.kt` (нативная обвязка). У swipe
  **два хоста** одной логики: `swipe-native.js` (JS) и Kotlin — отсюда V1 (места правки) и V4
  (мультиязычность) реально работают на DSL. Это и есть образец для замера раздела B ниже, а не
  абстрактный пример из biome-dsl.
- **`.sources`/`.logic`/`.examples` biome-dsl формат в этом репозитории используется только
  для swipe** (`dsl/swipe/swipe.logic` + `.examples`, один прецедент на весь проект). Остальной
  DSL в репо — это `data`/`derive`/`fmt` GENERATE-шаги по `.core-data`+`.data-contract`
  (`AGENTS.md:9-11`). Тестовые ожидания как данные в этом проекте делаются иначе, чем в
  BestPractice §4 таблице biome-dsl: `dsl/grammar-oracle/grammar.core-data` (1979 строк) →
  `GENERATE grammar-oracle data --target kt --out
  kotlin/shared/src/commonTest/generated/kotlin/polski/data/rows/oracle/GrammarCaseRows.kt` →
  обычный Kotlin `commonTest`, который итерирует сгенерированные строки. Это и есть локальный
  аналог «ожидание как DSL-данные», и раздел D предлагает именно этот путь, а не рукописный
  `.sources`-файл формата biome-dsl (которого больше нигде в этом Kotlin-проекте нет).

## A. Data → Generated Kotlin

**Решение: да, GENERATE-шаг, по образцу `course-plru`/`styles`/`grammar-oracle` (уже в
`dsl/biome.project`).** Контракт не меняется (кроме C ниже — аддитивно). Схема идентична
существующему `course-plru`: один `GENERATE ... data --contract exam --rows <Record> <file>...
--target kt --kt-package polski.data.rows.exam --out
kotlin/shared/src/commonMain/generated/kotlin/polski/data/rows/exam/ExamRows.kt`, рукописный
маппер рядом (как `polski/data/PlRuSkillRows.kt` маппит `SkillRow` → `Skill` через `toSkill()`).

### Компоненты (NEW, если не указано иное)

```
kotlin/shared/src/commonMain/generated/kotlin/polski/data/rows/exam/ExamRows.kt   # печатает biome, не редактируется (P16)
kotlin/shared/src/commonMain/kotlin/polski/exam/grammar/GrammarRowMapper.kt       # GrammarTestRow+GrammarItemRow → GrammarTest/GrammarItem
kotlin/shared/src/commonMain/kotlin/polski/exam/ExamCatalog.kt                    # ExamRow(+ExamModuleRow, см. C) → ExamId/каталог сдач
```

`GrammarRowMapper.kt` — по образцу `PlRuSkillRows.kt`:

```kotlin
internal fun GrammarItemRow.toGrammarItem() = GrammarItem(
    itemId, questionType.toDomain(), context, gap, options?.split("|").orEmpty(), correctAnswer, points,
)
internal fun GrammarItemRowQuestionType.toDomain() = when (this) { CHOOSE_FORM -> ChooseForm; FILL_GAP -> FillGap; ... }
internal fun grammarTestsByExam(testRows: List<GrammarTestRow>, itemRows: List<GrammarItemRow>): Map<String, GrammarTest> = ...
```

Reading/Listening/Writing мапперы **не проектирую здесь**: их domain-модель (`ReadingPassage`,
`ListeningTask`, `WritingTask`) сегодня — приватные `data class` внутри `webMain`
(`Exam{Reading,Listening,Writing}Web.kt`), а не публичный тип `:shared`. Промоция в `:shared` —
предмет §2 `EXAM-FULL-FLOW-PLAN.md` (владеют `arch-exam-listening`/`arch-exam-reading`, уже в
работе по аудиту того плана). Маппер row→domain для этих трёх модулей пишется **после** или
**вместе** с той промоцией, на типы, которые она определит — не раньше, чтобы не задать домену
форму дважды (P7).

### Порядок (с гейтами, не одним шагом)

1. **Блокер (отдельная маленькая задача, не эта)**: перенести реальные `segmentStartMs`/
   `segmentEndMs` из `ExamListeningData.kt` в `listening-tasks.core-data` для B1-2019/B2-2019 (10
   чисел), заполнить 2 пустых `ReadingPassageRow.text` в `B1/2019`, исправить 38 `taskRef` в
   `B2/2019`. Проверка: `biome data check` (команда из «Проверенные факты») — 0 problems для обеих
   сдач. Это задача `fix-exam-data`/data-pipeline агента, не архитектурная.
2. Подключить `GENERATE exam-data data --contract exam --rows ... --target kt --kt-package
   polski.data.rows.exam --out .../ExamRows.kt` в `dsl/biome.project`, **только** с B1/2019 +
   B2/2019 файлами (список `--rows` растёт по мере готовности остальных 7 сдач — каждая
   добавляется отдельным коммитом, после `biome generate --check` зелёный для неё; не «все сразу
   по допущению»).
3. Написать `GrammarRowMapper.kt`, заменить `examGrammarTests` в `ExamData.kt` вызовом маппера;
   удалить `ExamData.kt`'s хардкод (`b1SampleGrammar`/`b1_2019Grammar`/`b2_2019Grammar`) — **не**
   удалять `b1-sample`, т.к. B1/sample данные ещё не чистые (шаг 1 не покрывает `sample`); либо
   сузить scope до B1-2019/B2-2019 и оставить `b1-sample` как явный, помеченный временный
   хардкод до готовности его `.core-data` (не тихо — issue-комментарий в коде, кто доделывает).
4. `ExamListeningData.kt`/`ExamReadingData.kt`/`ExamWritingData.kt` удаляются **в той же волне**,
   что промоция их доменных типов в `:shared` (шаг «Reading/Listening/Writing мапперы» выше) —
   координировать с `arch-exam-listening`/`arch-exam-reading`, не удалять раньше, чем есть
   маппер, иначе web лишится данных без замены.

### Открытый факт для раздела A (не решаю сам)

`ListeningQuestionRow.options`/`correctAnswer` в `.core-data` — короткие метки (`"a|b|c"`,
`correctAnswer: "b"`), а текущий native `ExamListeningData.kt` хранит варианты как целые строки с
префиксом (`"b) w bibliotece"`, `correctAnswer = "b) w bibliotece"`). Это разные конвенции
отображения одного и того же факта — маппер может восстановить любую, но UI-рендер
(`ExamListeningWeb.kt`) сегодня написан под вторую. Выбор — за тем, кто ведёт промоцию
Listening-домена (шаг 4), не архитектурный вопрос этого плана.

## B. Scoring → остаётся native (P25, не DECISION)

**Измерено, не предположено.** `GrammarExamScorer.score` — тело в одну строку:
`normalize(answer) == normalize(item.correctAnswer)`. Локальный прецедент на то же решение,
4-ветвенный `swipe.logic` (`belowThreshold`/`verticalDominates`/`swipeRight`/`swipeLeft`), даёт
**37 строк** сгенерированного `SwipePolicy.kt` плюс `.logic`+`.contract`+`.examples` (19 строк
источника). Эквивалент для одноветвенного равенства был бы короче swipe, но всё равно длиннее
native (минимум: `DECISION`+`FACT normalized_match`+`CONTRACT` — то, что в задании уже
прикидывалось на ~10 строк + контракт) — **P25 S1 (длиннее native) срабатывает** даже без второго
замера.

Второй, более сильный стоп-сигнал — **S4 (один потребитель)**: у swipe два хоста одной логики
(`swipe-native.js` в JS, Kotlin) — это даёт V1/V4 реальную выгоду. У grammar-scorer — только
Kotlin `:shared`, ни один другой язык-цель эту конкретную проверку не реализует (React-миграция
завершена, второй рантайм не планируется — `CLAUDE.md`: «Переход выполнен»). Нет второго хоста →
нет мультиязычной выгоды → S4.

Обобщение из `EXAM-FULL-FLOW-PLAN.md` §2 (pipe-separated alternatives: `correctAnswer` может быть
`"a|b"`) **не меняет вывод**: тело остаётся одной строкой —
`correct.split("|").any { normalize(it) == normalize(answer) }`. Тоже короче любого DECISION
эквивалента, тот же P25 S1+S4.

**Решение: `GrammarExamScorer.kt` остаётся native.** Применить обобщение (pipe-alternatives) как
обычную правку Kotlin-функции, не DSL. Зафиксировать как запись о P25-отказе (owner's call
задокументирован, не DSL-отказ «по умолчанию») — одна строка в `AI/decisions.md`: «Grammar/Reading/
Listening scorer — native per P25 S1+S4 (замер: swipe.logic 4-rule→37 lines vs scorer 1-line body;
0 второго хоста); пересмотреть при появлении второго DSL-таргета для сдачи экзаменов».

## C. Exam config → `.core-data` (RECORD, не native)

Это данные (таймер/бюджет баллов на часть), не решение — согласен с заданием. `ExamSessionState`
(порядок частей, переход между ними) — поведение, остаётся Kotlin-реducer (`:shared`,
уже спроектирован в `EXAM-FULL-FLOW-PLAN.md` §3 как `ExamSessionSession.kt`, не меняю).

**Пересекается с `EXAM-FULL-FLOW-PLAN.md` §1**, которое уже предлагает этот же RECORD под именем
`ExamModuleRow`. Принимаю ту форму (не задания `EXAM_PART`/`partId`/`order`), т.к. она:
переиспользует существующий домен-enum `ExamModule` (Listening/Reading/Grammar/Writing,
`ExamModel.kt:7`) вместо нового `partId: string`; составной `KEY (examRef, module)` вместо
отдельного `order: int` — порядок частей уже фиксирован в `ExamSessionState.partOrder` (константа
Kotlin), дублировать его как данные было бы второй копией одного факта (P7).

```
# dsl/exam.data-contract — добавить (аддитивно, P14, существующие .core-data не трогает)
RECORD ExamModuleRow KEY (examRef, module) COUNT 1..
  examRef: ref ExamRow
  module: enum listening|reading|grammar|writing
  timerMinutes: int
  totalPoints: int
END
```

Данные — новый файл на сдачу (`exam-modules.core-data`, рядом с `exam.core-data`), не правка
существующих файлов:

```
# exams/pl/certyfikat-polski/B1/2019/exam-modules.core-data — NEW
DSL 1
EXAM_MODULE { examRef: "certyfikat-polski-B1-2019" module: listening timerMinutes: 25 totalPoints: 30 }
EXAM_MODULE { examRef: "certyfikat-polski-B1-2019" module: reading   timerMinutes: 45 totalPoints: 30 }
EXAM_MODULE { examRef: "certyfikat-polski-B1-2019" module: grammar   timerMinutes: 45 totalPoints: 30 }
EXAM_MODULE { examRef: "certyfikat-polski-B1-2019" module: writing   timerMinutes: 45 totalPoints: 30 }
```

(25/45/45/45 мин, 30×4 pts — факт, сверенный `EXAM-FULL-FLOW-PLAN.md`'s аудитом реальной
структуры B1/B2, не выдумано здесь.) Генерируется в тот же `ExamRows.kt` (раздел A), маппер
`ExamRow.toExamId()`/`ExamModuleRow.toExamModuleConfig()` в `ExamCatalog.kt`.

**Координация, не самостоятельное решение**: `EXAM-FULL-FLOW-PLAN.md` уже в репозитории
(untracked, тот же сеанс) с идентичным предложением — если `arch-exam-flow` ещё активен, эта часть
плана **не вносит правку в `dsl/exam.data-contract` параллельно** с ним; владение этим конкретным
`RECORD` — за тем агентом, этот план лишь подтверждает его форму согласованной с §A/§D ниже и не
дублирует работу.

## D. Test expectations

**UI (Playwright `.spec.ts`) остаётся native** — согласен с заданием, не DOM-тест не выражается
выше в DSL (P10 «вычисление без слоя выше»).

**Scorer-ожидания НЕ переносятся в biome-dsl `.sources` формат.** Причина — факт из раздела
«Проверенные факты»: biome-dsl `.sources`/`.logic` формат в этом репозитории используется ровно
один раз (`dsl/swipe/`), для DECISION, который реально генерируется в Kotlin. У scorer (раздел B)
решение осталось native — переносить его *тестовые ожидания* в `.sources`, который по BestPractice
§4 существует для тестирования DSL-слоя (`.logic`+`.contract`+`.sources`), означало бы создавать
DSL-артефакт без DSL-правила за ним — находка P10 наоборот (транслитерация теста, не логики).

Вместо этого — **существующий локальный паттерн «ожидание как генерированные данные»**:
`dsl/grammar-oracle/grammar.core-data` (1979 строк) → `GrammarCaseRows.kt` → обычный
`commonTest`. Он оправдан на 1979 кейсах. `GrammarExamScorerTest.kt` сегодня — **7** рукописных
кейсов, проверяющих `normalize()`-поведение (case/trim/empty), не предметные данные экзамена —
ниже порога, где генерация данных окупается (P25 того же рода: 7 строк короче любого `.core-data`
+ `GENERATE`-шага + `--check`). **Решение: `GrammarExamScorerTest.kt` остаётся рукописным Kotlin
`commonTest`**, без изменений по этому плану.

Если раздел A даст реальный ответный ключ (correctAnswer по каждому `GrammarItemRow` уже есть в
`.core-data` для 45+75 заданий на сдачу) — это **уже** покрыто самим `GrammarRowMapper` +
существующими parity/oracle тестами данных (как `TrainingParityTest.kt` для course data), не
нуждается в отдельном scorer-oracle: подставить реальный `answers`-map = `correctAnswer` для
каждого `itemId` и ожидать `scoreAttempt(...).correct == test.items.sumOf { it.points }` — это
**один** сквозной `.examples`-подобный тест на сдачу («все ответы верны → максимальный балл»), не
список по каждому вопросу. Предлагаю (не обязательно, оценочно низкий приоритет): один
`GrammarExamScorerAllCorrectTest.kt` в `commonTest`, итерирующий сгенерированные `GrammarTest` из
раздела A и проверяющий это свойство для каждой подключённой сдачи — ловит реальные опечатки в
`.core-data` (несовпадение `correctAnswer` с самим собой при будущих правках), не повторяет логику
A ни в раздел B.

## Сводка по P25 (чек-лист раздела «Применение к development feedback»)

| Пункт задания | Слой | V1–V5 | S1–S6 | Решение |
|---|---|---|---|---|
| A. Экзаменные данные | GENERATE (data) | V1 (копия устранена, P7), V3 (вывод в 4 платформы) | — | DSL, уже частично готово |
| B. Scoring | native | нет измеримой V | S1 (длиннее native), S4 (1 хост) | native, остаётся |
| C. Таймер/баллы на часть | RECORD (data) | V1 (не вторая копия `partOrder`) | — | DSL, аддитивно |
| D. UI-тесты | native `.spec.ts` | — | S3 (вычисление без слоя выше не в scope) | native, без изменений |
| D. Scorer-тесты | native `commonTest` | — | S1 (7 кейсов короче `.core-data`+GENERATE) | native, без изменений |

## Верификация (на этапе реализации, не выполнена здесь)

- `biome data check --contract dsl/exam.data-contract --rows ... <B1/2019 files>` — 0 problems
  (сегодня 2; команда и вывод — в «Проверенные факты», воспроизводима).
- `biome generate --check --project dsl/biome.project` — зелёный после подключения шага A.
- `GrammarRowMapper`: unit-тест на 1 известный item (`g1`→`czymś`/`czyms` — сверить, что текст
  после генерации сохраняет диакритику; если `.core-data` ASCII-only, это отдельный, более
  приоритетный дефект данных, обнаруженный этим планом — диакритика нужна для реального польского
  текста, импорт должен её восстановить до того, как раздел A генерирует Kotlin).
- `GrammarExamScorerTest.kt`, `GrammarExamSessionTest.kt` — не трогаются (раздел B/D).
- Playwright — не трогается этим планом.

## Документация и handoff

- `AI/decisions.md`: одна запись — P25-отказ для scorer (раздел B), с замером.
- `dsl/exam.data-contract`: правка C — координировать с владельцем `EXAM-FULL-FLOW-PLAN.md` §1
  перед коммитом (не вносить обеими ветками независимо).
- Реализация раздела A шаг 1 (audio timestamps + 2 missing text + 38 taskRef) — отдельная
  маленькая задача data-pipeline агента, блокирует шаг 2 (GENERATE wiring), не блокирует раздел C
  (C генерируется из новых `exam-modules.core-data` файлов, не зависит от shag 1's фиксов).
- Reading/Listening/Writing мапперы (раздел A) — явно отложены до/вместе с `arch-exam-listening`/
  `arch-exam-reading` §2 промоцией; не начинать параллельно без их контракта, иначе домен
  получит форму дважды.
- Это plan-only документ (senior-architect роль): реализация — `senior-developer`, маленькими
  задачами (один файл/шаг за раз, per project memory «small-tasks»), в порядке раздела A «Порядок».
