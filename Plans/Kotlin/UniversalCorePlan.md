# Универсальное ядро «Полиглот»: план модулей, пакетов и движка

Дата: 2026-09-26. Статус: архитектурный план, репозиторий не менялся при подготовке (задача была read-only). Автор: senior-architect. Идентификаторы — на английском, текст — по-русски.

Источники: два независимых дизайна — [`data-tables`](../../../private/tmp/claude-501/universal-core-data-tables.md) и [`hybrid-plugins`](../../../private/tmp/claude-501/universal-core-hybrid-plugins.md) (пути вне репозитория, см. §9 «Исследования»), исследовательский отчёт `universal-core-research.md`, код-мэп `universal-core-codemap.md`; факты сверены напрямую с `kotlin/shared/src/commonMain/kotlin/polski/{model/Grammar.kt, grammar/GrammarEngine.kt, training/ExerciseFactory.kt, data/CourseData.kt, presentation/EndingHighlight.kt, progress/ProgressRepository.kt, preferences/UserPreferences.kt, vocabulary/VocabularyDocument.kt}`, `courses/schema/course-pack-v1.schema.json` (1310 строк), `scripts/validate-course.mjs` (337 строк), `kotlin/settings.gradle.kts` (`:shared :composeApp :androidApp`), `kotlin/shared/build.gradle.kts:6-42` (`generateCoursePackSource`).

Продуктовая рамка: [docs/product-contract.md](../../docs/product-contract.md), [CoursePacksPlan.md](CoursePacksPlan.md) — первая пара `pl-ru`, методики — предпочтение, а не тип мозга, новые языки не показываются доступными без готового пакета.

---

## 0. Цель

Ядро курса — не «польская грамматика», а метод Петрова как данные: **конструкция (смысл) × набор признаков (как это устроено морфологически/синтаксически)**. Добавление изучаемого языка, родного языка (L1) или пары должно требовать **данных (JSON/Markdown), а не правок Kotlin**, кроме одного заранее описанного случая — языка с продуктивной морфологией, которую невыгодно держать таблицей (см. `:morph-api` в §5). Четыре стиля подачи (rule-first / situation-first / L1-contrast / minimal) — рецепты порядка одних и тех же типизированных блоков, не branch в коде. Упражнения и расписание FSRS одинаковы для всех стилей. Пакет `pl-ru` продолжает работать без переименования skill ID, без сброса FSRS/vocabulary прогресса и без нарушения текущей schema v1 до доказанного побайтного паритета движка.

---

## 1. Принципы

1. **Открытый каталог признаков, не закрытые enum.** `FeatureKey`/`FeatureValue`/`FeatureBundle = Map<FeatureKey, FeatureValue>` в `:core-model` заменяют `GramCase`/`Gender`/`Person`/`Tense`/`PossessiveId` (`model/Grammar.kt:3-40`, сейчас `fromId` бросает `error(...)` на всё незнакомое). Значения — из словаря Universal Dependencies `feat` + UniMorph schema (готовое соответствие, не свой список); язык объявляет **подмножество**.
2. **Construction = смысл, Realization = как язык это выражает.** Метод Петрова (Tense × ClauseType × Person/Number) — один `Construction` с осями-признаками, не 16 отдельных генераторов. 16 польских skill схлопываются в 5-6 core-конструкций (§2).
3. **Ядро закрыто, языки открыты (open/closed).** `:core-*` и `:pack-format` никогда не импортируют конкретный язык/пару/стиль. Языковые/парные данные читаются по строковому id через `PackSource`, а не как Kotlin-зависимость.
4. **Gradle-модуль — только там, где есть код или отдельная API-граница** (правило из `module-architecture`: «Source sets share code; Gradle modules provide separate dependency/API boundaries... Do not create one module per screen by habit»). Языковые/парные пакеты — чистые JSON/Markdown, **не Gradle-подпроекты**: не нужно объявлять 7 KMP-таргетов на каждый язык.
5. **Морфология: таблицы во время выполнения, правила — только при сборке.** `TableMorphology` — единственная реализация `Morphology`, нужная сейчас (в т.ч. для pl). `MorphologyPlugin` — маленький Kotlin-интерфейс, зарезервированный, но **не реализуемый**, пока язык с открытым продуктивным словообразованием (агглютинация, корень-паттерн) не докажет, что таблицы не хватает.
6. **Стили — предпочтение, не диагноз.** Один и тот же `Exercise`, тот же FSRS-рейтинг, тот же skill ID — независимо от выбранного стиля (уже принято в `CoursePacksPlan.md`).
7. **ID и прогресс стабильны.** Skill ID не переименовываются. Грамматический прогресс привязан к **целевому языку**, не к паре (смена родного языка не сбрасывает польский прогресс). Словарь остаётся привязан к паре (направление перевода включает L1).
8. **Markdown — только формат авторинга.** Движок Kotlin никогда не парсит Markdown напрямую; длинные текстовые поля (сцены, заметки-контраст) компилируются в JSON тем же build-time приёмом, что сегодня инлайнит `course.json` (`kotlin/shared/build.gradle.kts:6-42`).
9. **Будущие возможности — только форма интерфейса.** Аудио, голосовой ввод, AI-объяснения — порты (`fun interface`, по прецеденту `RandomSource`/`TimeSource`), без реализации сейчас (§7).

---

## 2. Универсальный скелет (core-структуры)

Таблица Петрова (3 времени × утверждение/вопрос/отрицание × лицо) — частный случай общей схемы **Construction × FeatureBundle**. Все 16 нынешних польских skill сводятся к 5 core-структурам + один составной тип:

| CoreStructure ID | Меняемая ось (feature) | pl-ru skill (пример) | en пример | zh пример |
|---|---|---|---|---|
| `core.argument.case-role` | `Case` (или семантическая роль без падежей) | `case.acc.f/m/n`, `case.gen.neg`, `case.inst`, `case.loc`, `case.dat` | предлог + порядок слов вместо падежа | предлог/классификатор, падежа нет |
| `core.verb.tense` | `Tense` | `verb.present/past/future` | `-s / -ed / will` | частицы 了/在/会, без спряжения |
| `core.sentence.polarity` | `Polarity` | `case.gen.neg` (родительный при отрицании) | `not / don't` (do-support) | 不/没 (зависит от аспекта) |
| `core.sentence.mood` | `Mood {Decl, YesNoQ}` | `sentence.question` | инверсия/`do`-вопрос | частица 吗 |
| `core.agreement.possessive` | владелец + `Agree` | `agreement.my` | `my/your/his` (без согласования) | 我的/你的 (инвариант) |
| `core.number` | `Number` | `sentence.plural` | `-s` | без изменений (числ. + классификатор) |
| `core.composite` | ссылается на ≥2 структур (`composesOf`) | `mixed`, 5-шаговая цепочка (`aspect` — лексический фокус `Aspect Imp→Perf` при `Tense=Fut`) | — | — |

Открытый риск (не решён здесь, см. §8): порядок разрешения конфликтов признаков в `core.composite`, когда два шага цепочки трогают одну и ту же ось разными способами. Митигируется тем, что движок применяет шаги последовательно к одному `FeatureBundle` — этого достаточно для нынешней польской цепочки, но не проверено на чужих языках.

Вывод из типологической таблицы (см. полную версию в исследовательском отчёте §3.2): **смысловые оси универсальны, способ выражения — нет**. Отрицание выбирает падеж в pl, частицу по времени/аспекту в zh/ar, суффикс внутри глагола в tr. Поэтому `Construction`+`FeatureBundle` — данные ядра, а *реализация* — данные языка.

---

## 3. Формат пакета (schema v2)

### 3.1 Слои и файлы

```
courses/
  core/                                  # один на продукт, язык-независим
    features.json                        # UD-ключи/значения + UniMorph-алиасы + gloss (ru/en)
    constructions.json                   # CoreStructure/Construction: id, slots, axes, composesOf, level, prerequisites
    template-ops.json                    # ровно ~10 операторов реализации (см. §5.2)
    exercise-kinds.json                  # transform, fill-slot, translate-L1→L2, recognize, chain
    styles/
      rule-first.json situation-first.json l1-contrast.json minimal.json   # рецепты блоков, без языкового текста
  lang/<code>/                           # изучаемый ИЛИ родной язык — один формат для обоих
    lang.json                            # language-profile: какие features есть, орфография, CLDR plural, answer-policy
    morphology/*.json                    # парадигмы+rewrites — вход авторинга, потребляет ТОЛЬКО Node build-скрипт
    forms.generated.json                 # материализованные формы — потребляет Kotlin runtime (TableMorphology lookup)
    lexicon.json                         # леммы + признаки + управление (valency)
    realization.json                     # Construction → как язык это выражает (шаблон-операторы)
    curriculum.json                      # skills[] — ТОЛЬКО для target: id (legacy для pl), construction, focus, fixed, level, prerequisites
    table-specs.json                     # спецификации для экрана «Матрица» (декартово произведение осей)
  pairs/<target>-<native>/
    pair.json                            # constructionNotes (L1-контраст), scenes, glosses, pitfalls
    style-content/                       # тексты стилей для конкретной пары
    vocabulary-editorial.json            # уже пар-скоуп, переносится как есть
    legacy/course.json                   # для pl-ru: нынешний v1 файл — источник правды до переключения (§6)
  schema/*-v2.schema.json                # по одной схеме на слой, рядом с v1
```

`courses/pl-ru/course.json` **не переименовывается и не удаляется** до подтверждённого побайтного паритета движка (§6). Авторинг: JSON — источник истины для рантайма; Markdown допускается только для длинных текстовых полей (`theory`, `scene`, `pitfall`) и компилируется тем же build-скриптом.

### 3.2 Пример pl-ru (сокращённо, реальные значения)

```json
// core/features.json (фрагмент)
{ "Case": { "values": ["Nom","Gen","Dat","Acc","Ins","Loc","Voc"], "gloss": { "ru": "падеж", "en": "case" } },
  "Polarity": { "values": ["Pos","Neg"] } }

// lang/pl/curriculum.json (skill без изменений ID — прямая перепись ExerciseFactory.kt:87-99)
{ "id": "case.gen.neg",
  "construction": "core.sentence.polarity",
  "focus": { "feature": "Polarity", "from": "Pos", "to": "Neg" },
  "fixed": { "Tense": "Pres", "Person": "1", "Number": "Sing" },
  "lexicalFilter": { "slot": "object", "where": { "animate": true } },
  "level": "A1", "prerequisites": ["case.acc.f"] }

// lang/pl/realization.json (фрагмент — перепись GrammarEngine.kt:47-92)
"core.sentence.polarity": {
  "order": ["subject?", "neg?", "verb", "object"],
  "slots": { "neg": { "when": "Polarity=Neg", "prepend": "nie" },
             "object": { "gov": { "default": "Acc", "Polarity=Neg&gov=Acc": "Gen" } } } }

// pairs/pl-ru/pair.json (фрагмент — перепись reference.russianSupport)
{ "constructionNotes": { "core.sentence.polarity/Polarity=Neg": {
    "transfer": "similar-form-diff-use",
    "note": "В русском «не вижу жену/жены» — оба варианта; в польском после nie только Dopełniacz." } } }
```

### 3.3 Гипотетический пример en-ru (иллюстрация формата, не решение о втором языке)

```json
// lang/en/lang.json — English не имеет Case (кроме местоимений) и Gender
{ "usesFeatures": ["Tense","Polarity","Mood","Person","Number"], "case": [] }

// lang/en/curriculum.json
{ "id": "en:verb.past", "construction": "core.verb.tense",
  "focus": { "feature": "Tense", "from": "Pres", "to": "Past" },
  "fixed": { "Polarity": "Pos" }, "level": "A1" }

// lang/en/realization.json — do-support вместо падежной альтернации
"core.sentence.polarity": {
  "slots": { "verb": { "when": "Polarity=Neg", "periphrasis": ["aux:do|Tense,Polarity", "lemma"] } } }

// pairs/en-ru/pair.json
{ "constructionNotes": { "core.sentence.polarity/Polarity=Neg": {
    "transfer": "absent-in-L1",
    "note": "В русском отрицание — частица «не» перед глаголом; в английском нужен вспомогательный do/does/did." } } }
```

Skill ID для нового target-языка — `${targetLanguageId}:${legacySkillShape}` (например `en:verb.past`), namespace — по **целевому** языку, не по паре.

---

## 4. Модульная архитектура Gradle/KMP

### 4.1 Что становится Gradle-модулем (только код/API-граница)

```
kotlin/settings.gradle.kts:
include(
  ":core-model", ":core-engine", ":core-srs", ":core-progress", ":core-presentation",
  ":pack-format", ":morph-api",
  ":shared", ":composeApp", ":androidApp",
)
```

| Модуль | Заменяет / происходит из | Публичный контракт (эскиз) | Зависит от |
|---|---|---|---|
| `:core-model` | `model/Grammar.kt` (40 строк) | `FeatureKey/FeatureValue/FeatureBundle`, `Construction`, `SkillSpec`, `Exercise` (со `slots: Map<String, SlotValue>` вместо жёстких `nounId/adjectiveId`) | — |
| `:core-engine` | `grammar/GrammarEngine.kt` (94), `training/ExerciseFactory.kt` (161) | `Morphology`, `ConstructionRealizer`, `TemplateInterpreter` (~10 операторов), `ExerciseGenerator`, `ChainBuilder`, `MatrixTableEngine` | `:core-model` |
| `:morph-api` | новое, тривиальное | `fun interface MorphologyPlugin { fun tryForm(lexeme: String, bundle: FeatureBundle): String? }` — опциональная обёртка над `TableMorphology` | `:core-model` |
| `:core-srs` | `srs/*` (~340 строк, без изменений — уже язык-нейтрален) | `Scheduler`, `SrsCard`, `Rating` | `:core-model` |
| `:core-progress` | `progress/*` (~490 строк) | `ProgressRepository`, `ProgressCodec`, `ReviewReducer` + `activePackFilter: (skillId) -> Boolean` | `:core-srs`, `:core-model` |
| `:core-presentation` | `presentation/*`, `EndingHighlight.kt` (уже общий, переезжает как есть) | `TrainingStore`, `StyleComposer(styleRecipe, pack) -> List<Block>`, `MatrixTableViewModel`, `AnswerEvaluator` | `:core-engine`, `:core-progress` |
| `:pack-format` | `data/CourseData.kt` (598 строк) | `PackRegistry`, `CoursePack(core, lang, pair)`, `CoursePackSource`/`CoursePackLoader` (v1 и v2 → одинаковый `TargetLanguagePack`) | `:core-model` |
| `:shared` | нынешний `:shared`, но без грамматики/копирайта в коде | тонкий фасад: собирает `CoursePack` из встроенных `PackSource`, экспортирует framework `PolskiShared` (та же конфигурация `iosArm64/iosSimulatorArm64/macosArm64`, `kotlin/shared/build.gradle.kts:63-66` — **не меняется**) | все `:core-*`, `:pack-format` |

**Направление зависимостей (жёстко):** `:core-*` и `:pack-format` никогда не импортируют язык/пару/хост. `:morph-api` не знает конкретных языков. Хосты (`:composeApp`, `:androidApp`, iOS/macOS через `PolskiShared.framework`) зависят на `:core-presentation` + `:shared`, никогда в обратную сторону.

### 4.2 Языковые/парные пакеты — данные, не модули

`courses/core/*`, `courses/lang/*`, `courses/pairs/*` **не становятся Gradle-подпроектами**. Вместо этого — обобщение уже работающего `generateCoursePackSource` (`kotlin/shared/build.gradle.kts:6-42`, сегодня инлайнит один файл) в сканирование каталога:

```kotlin
// :pack-format/build.gradle.kts (эскиз)
val generatePackManifest by tasks.registering {
    inputs.dir(layout.projectDirectory.dir("../../courses"))
    outputs.dir(generatedDir)
    doLast {
        // сканирует courses/core/**, courses/lang/*/*.json, courses/pairs/*/*.json (fileTree)
        // пишет EmbeddedPackSource (id -> JSON-строка, тот же chunked-literal приём) + manifest списка id
    }
}
```

Добавление языка = положить `courses/lang/es/*.json` и пересобрать — **ни одна Kotlin-строка не меняется**, `settings.gradle.kts` не трогается, потому что это запись в манифесте, не модуль. Регистрация происходит в существующей точке инициализации каждого хоста:

```kotlin
// composeApp main / androidApp Application / iOS-macOS bootstrap — единственное затрагиваемое место
val registry = CoursePackRegistry.builder()
    .register(PackSource.embedded("pl")).register(PackSource.embedded("ru")).register(PackSource.embedded("pl-ru"))
    // .register(PackSource.embedded("es"))  // NEW язык — одна строка, если добавлена, а не автосканирование
    .build()
```

Зарезервированный люк на будущее (не реализуется сейчас): `PackSource` — интерфейс; позже добавляется `DownloadedPackSource(httpClient, cacheDir)` рядом с `EmbeddedPackSource` в `CompositePackSource` — без правок `PackRegistry` или существующих пакетов.

### 4.3 Convention-плагин

Чтобы не размножать блок `jvm("desktop"); iosArm64(); …; framework{...}` в каждом новом `:core-*` модуле, таргеты выносятся в `kotlin/build-logic/polski.kmp-common` (NEW, обычный convention plugin). Каждый новый модуль — `plugins { id("polski.kmp-common") }` + зависимости, без копий boilerplate.

### 4.4 Реальный Gradle-модуль на будущее: `:morph-<code>` (пример, не создаётся сейчас)

Если у языка есть продуктивная морфология дороже полной таблицы (гипотетический агглютинативный/root-pattern случай), автор языка добавляет:

```
include(":morph-tr")   // 1 строка — неизбежна, это Kotlin-код
```

`:morph-tr` реализует `MorphologyPlugin` из `:morph-api`, публикует таргеты через `polski.kmp-common`, не трогает `:core-engine`. Единственная осознанная точка касания у хостов — один вызов `MorphologyPluginRegistry.register("tr", TurkishMorphologyPlugin())` в уже существующей точке инициализации. Для табличных языков (в т.ч. pl на старте) эта строка не пишется.

---

## 5. Контракты движка (`:core-engine`)

### 5.1 Публичные типы

```kotlin
interface Morphology { fun form(lexeme: String, bundle: FeatureBundle): String }
class TableMorphology(private val forms: Map<String, Map<FeatureBundle, String>>) : Morphology   // данные, дефолт и единственная реализация сейчас

class ConstructionRealizer(private val realization: RealizationSpec, private val morphology: Morphology) {
    fun realize(construction: String, bundle: FeatureBundle, lexicalSlots: Map<String, String>): RealizedSentence
}
data class RealizedSentence(val text: String, val slotSpans: Map<String, IntRange>)   // основа diff/highlight

class ExerciseGenerator(
    private val realizer: ConstructionRealizer,
    private val skills: Map<String, SkillSpec>,
    private val random: RandomSource,          // существующий порт
    private val ids: ExerciseIdFactory,        // существующий порт
) {
    fun generateForSkill(skillId: String, preferredSeed: SentenceSeed? = null): Exercise
    fun generateChain(skillIds: List<String>): List<Exercise>
}
```

`Exercise.changes` вычисляется как поэлементный diff двух `RealizedSentence` по `slotSpans` — это прямая замена `EndingHighlight.ContrastPair` (уже язык-нейтральный тип, переезжает без изменений) и обобщение сегодняшних `exerciseCopy`/`exercisePatterns`.

### 5.2 Язык шаблонов — жёсткий потолок ~10 операторов

`order`, `when`, `agree`, `gov` (управление из лексикона), `periphrasis` (аналитические формы — pl `będę+инфинитив`, en `do`-support), `prepend/append`, `rewrite` (фонологические правки: pl смягчение согласной, tr гармония гласных), `optional`, `alt`, `punct`. Всё, что не выражается этим набором — авторские `overrides` целой фразой в `curriculum.json`, а не новый оператор. Если 11-й оператор всё же понадобится — это сигнал остановиться и оформить ADR (риск переусложнения до Grammatical Framework, см. §8).

### 5.3 Замена `GrammarEngine`/`ExerciseFactory`/matrix без изменения вывода

1. `GrammarEngine.kt` (94 строки: суффиксы `em/eś/m/ś/śmy/ście`, `będę+infinitive`, VOC-восклицание) становится `lang/pl/realization.json` + `forms.generated.json`, читаемыми одним `TableMorphology`+`ConstructionRealizer`.
2. `ExerciseFactory.kt` (`when(skillId)` на 161 строку, хардкод `wife/husband/friendM/go/book`) становится `lang/pl/curriculum.json` (16 `SkillSpec`) + один `ExerciseGenerator.generateForSkill`. 5-шаговая цепочка (`generateChain`, сейчас :139-160) — данные (`chainSteps`), не код.
3. Matrix-хосты (~1600 строк на 6 файлов: `MatrixWeb.kt:123,143,248` и аналоги в desktop/Android/iOS/macOS) заменяются одним `MatrixTableEngine.build(spec, pack): TableViewModel`, потребляемым тонким рендерером на каждом хосте; `table-specs.json` задаёт оси вместо хардкод-литералов.
4. **Parity gate обязателен и предшествует удалению старого кода:** `ExerciseGenerator` работает параллельно старому `ExerciseFactory`/`GrammarEngine` на одних и тех же `(skillId, seed, seedRandom)`; сравнение — побайтное, по существующим/новым pinned fixtures (490 possessive-форм, все skill×seed, полная цепочка, matrix-демо). Переключение — только после зелёного диффа на JVM/JS/Wasm/Native.

### 5.4 Зарезервированные точки расширения (не реализуются сейчас)

| Возможность | Контракт (форма, без реализации) | Где живёт | Как подключится позже |
|---|---|---|---|
| Аудио (файлы/TTS) | `VocabularyItem.audioRef: AssetRef? = null`, `Exercise.audioRef: AssetRef? = null`; порт `fun interface AudioProvider { fun play(ref: AssetRef) }` | `:core-model`, `:core-presentation` | Пакет ссылается на asset; хост реализует порт (`<audio>`/`AVAudioPlayer`/`MediaPlayer`) или его игнорирует — рендереры не меняются |
| Голосовой ввод | `AnswerInput` sealed (`Typed`, зарезервировать `Spoken(transcript: String)`); порт `fun interface SpeechRecognizer { suspend fun transcribe(): String }` | `:core-presentation` | Распознанный текст входит в тот же `AnswerEvaluator.evaluate(String)`, что и печатный — `:core-engine`/`:core-progress` не меняются |
| AI-объяснения/подсказки | `fun interface ExplanationProvider { suspend fun expand(blockId: String, context: FeatureBundle): String }`, дефолт — `StaticPackExplanationProvider` (текущее статическое поведение) | `:core-presentation` | Отдельный будущий модуль (`:ai-api`+вендор) реализует тот же интерфейс за кнопкой «почему?» стиля 4; отсутствие провайдера = текущий статический текст |

---

## 6. Совместимость с pl-ru v1

- `course-pack-v1.schema.json` не удаляется. `:pack-format` поддерживает оба входа за одним контрактом: `CoursePackSource.load(): RawPackJson` (v1 — сегодняшний единый документ, v2 — 4-слойные файлы) → `CoursePackLoader.parse(raw): TargetLanguagePack` — обе реализации возвращают идентичный тип. Байт-идентичность проверяется на **выходе движка** (golden fixtures), не на форме JSON.
- Skill ID для pl-ru не переименовываются: `case.acc.n`, `stats["case.acc.n"]` и т.п. остаются буквально. Новые пакеты: `${target}:${skillId}`, namespace — по целевому языку (смена родного языка не сбрасывает польский прогресс — уже подтверждено фактом, что FSRS fuzz-seed не зависит от skillId, `FsrsScheduler.kt:31`, и `ProgressCodec.decode` сохраняет неизвестные поля, не обнуляя прогресс).
- `ProgressRepository`/`SkillQueue` получают `activePackFilter: (skillId) -> Boolean` (для pl-ru: `{ it in legacyPolishSkillIds }`) — прямое исправление найденного в код-мэпе краша (`TrainingStore.kt:248-249` бросает на любой чужой skillId).
- Vocabulary остаётся пар-скоуп: `${target}-${native}:vocabulary:{direction}:{id}`; для pl-ru — буквально то же значение `"pl-ru"`, просто не строковый литерал в 4+ местах (`VocabularyDocument.kt:38,96,102,126`), а `pack.pairId`.
- Preferences: `coursePair: String` → `CourseSelection(target, native, style)`; кодек v2→v3 читает старое `coursePair="pl-ru"` → `("pl","ru")`, `explanationMethod=Logic→"rule-first"`, `Situations→"situation-first"` — тот же паттерн, что уже применён для предыдущих tolerant-decode миграций (`UserPreferencesCodec.kt:31,52`).
- `courses/pl-ru/migrate-v1-to-v2.mjs` (NEW) разложит `course.json` на 4-слойные файлы **после**, а не до того, как golden-фикстуры зелёные на generic-движке — расщепление файла и переезд на generic-движок — независимые шаги (задача UC-12).
- **Риск, требующий решения перед стартом UC-05/06/07:** содержимое pl-ru активно меняется (см. git status — UX v4/v5 в работе). Golden-фикстуры нужно зафиксировать на конкретном коммите до начала генерик-миграции, иначе «побайтное совпадение» станет недостижимой целью.

---

## 7. UI по хостам

- **Настройки:** три независимых пикера (target/native/style) вместо одного `coursePair`, источник — `CoursePackRegistry.availableTargets()/availableNatives(target)/availableStyles(target, native)` (учитывает `requires`/`fallback` стиля — недоступный стиль показывается отключённым с объяснением, не скрытым). web/desktop/android/iOS уже имеют раздельные экраны настроек — меняется источник списка, не структура экрана.
- **Карточка по стилю:** `StyleComposer(styleRecipe, pack).compose(phase, exercise) -> List<Block>`; хосты рисуют по типу блока (`rule`/`table`/`nativeParallel`/`scene`/`examples`/`whyOnDemand`) — обобщение нынешних `ExplanationMethod.Logic/Situations` (`presentation/AppUiState.kt:12`, `preferences/UserPreferences.kt:3`, `SkillPresentation.logic/situations` в `CourseData.kt:23-28`), только 4 блока вместо 2.
- **Матрица/таблицы:** один `MatrixTableViewModel` на все хосты; каждый хост сохраняет свой layout (DOM/CSS, Compose M3, SwiftUI), но перестаёт хардкодить skill/seed литералы.
- **Локализация UI-хрома** (~996 строк с кириллицей в коде хостов, см. код-мэп §4.10) — отдельная механическая задача (UC-13), не блокирует остальную миграцию.
- Ничего из этого не меняет текущие UX-планы (`PlatformUXPlan.md`) — они про анимации/навигацию, не про источник данных карточки.

---

## 8. Авторинг и валидация

1. **Морфология** — офлайн Node-скрипт (`scripts/build-pack.mjs`, NEW, по образцу `scripts/validate-course.mjs`) разворачивает `morphology/*.json` (парадигмы+rewrites, либо импорт SGJP/PoliMorf/Wiktionary) в `forms.generated.json`. Работает один раз на сборку пакета, не в рантайме.
2. **Schema v2 + валидатор** — по одной JSON Schema на слой (`core-features-v1`, `lang-pack-v2`, `pair-pack-v1`, `style-recipe-v1`) + перекрёстные проверки: `Skill.construction` существует в `constructions.json`, значение feature входит в `lang.usesFeatures`, ссылки `paradigmRef`/`lexicalFilter` резолвятся. Развитие `scripts/validate-course.mjs` (переиспользуются переносимые функции: unique-id, prerequisite DAG, noHtml, contrast-segments).
3. **Комбинаторная проверка:** сборщик перебирает `(skill × лексемы × оси)`, список предложений на ревью; одобрение хранится хешем предложения (тот же паттерн, что редакторский журнал `vocabulary-editorial.json`).
4. **UD-парсер как автоматический фильтр (гипотеза, не проверена):** после сборки — прогон сгенерированных предложений через открытый UD-парсер целевого языка, сверка признаков; не замена носителю языка.
5. **Редакторская проверка:** сцены и L1-заметки могут черновиться LLM, публикуются только после проверки человеком.
6. **Лицензии** — `attribution.json` на пакет (паттерн — `courses/pl-ru/ATTRIBUTION.md`). Предпочтение источников без ShareAlike для коммерческого продукта: SGJP/PoliMorf (BSD-2) для pl, Wikidata Lexemes (CC0), Unicode CLDR (plural rules); Wiktionary/UniMorph/OpenCorpora/FrequencyWords — CC BY-SA, обязывает публиковать сам пакет данных под той же лицензией — решение по каждому языку отдельно, не архитектурное.
7. **Шаги добавления языка без кода:** (1) `lang.json`; (2) `morphology/*.json` → build-скрипт → `forms.generated.json`; (3) `lexicon.json`; (4) `realization.json` (≤10 операторов); (5) `curriculum.json`; (6) `table-specs.json`; (7) `pairs/<target>-<native>/pair.json`, если нужен L1-контраст; (8) прогон валидатора v2; (9) регистрация одной строкой в bootstrap хоста. Kotlin не трогается, пока язык не приносит механизм словоизменения, не покрытый 10 операторами — тогда это задача `:core-engine`/`:morph-api`, а не пакета.

---

## 9. Оценка двух дизайнов и выбор

| Критерий | `data-tables` (A) | `hybrid-plugins` (B) |
|---|---|---|
| Гибкая расширяемость (top priority) | 4 — открытый `FeatureSet`, реестры, но lang-пакеты описаны как реальные Gradle-модули (`:lang-pl`, `:lang-en`…), что дублирует 7-таргетный boilerplate на каждый язык | **5** — явная таблица «вид расширения → что добавляется/не трогается»; данные не Gradle-модули; `MorphologyPlugin` — честный люк только для сложных случаев |
| Модульность (соответствие `module-architecture`: модуль = код/API-граница, не «по привычке») | 3 — один Gradle-модуль на язык противоречит явной рекомендации навыка | **5** — модуль только там, где есть код (`:morph-<code>`); convention-plugin `polski.kmp-common` устраняет дублирование таргетов |
| Минимум кода на новый язык | 4 — данные, но новый язык = новый Gradle-модуль (Kotlin-правка `settings.gradle.kts` + build.gradle.kts) | **5** — новый язык = новый каталог + ребилд, **ноль** строк Kotlin/Gradle |
| Корректность/продуктивность морфологии | 4 — таблица + операторы шаблона, без честного люка для продуктивных систем | **5** — HYBRID специально: таблица по умолчанию + зарезервированный `MorphologyPlugin` для tr/ar-класса случаев |
| Стоимость авторинга | 4 — тот же пайплайн (Node build + schema + LLM-черновик+ревью) | 4 — идентично |
| Совместимость с pl-ru ID/прогрессом | 5 — идентичное решение (target-namespace, bare pl ID, pair-scoped vocab) | 5 — идентично |
| Тестируемость | 4 — golden fixtures, чёткий порядок задач | **5** — то же + модульная изоляция тестов (`./gradlew :core-engine:test` не требует пересборки `:composeApp`), так как языки не являются Gradle-модулями |
| Соответствие 4 хостам (framework export не ломается) | 3 — не описано, как `:lang-*`-модули не задевают `PolskiShared` framework export при добавлении языка | **5** — явно: `:shared` — тонкий фасад, framework export не меняется, языковые данные вообще не Kotlin-зависимости |
| Время до первого второго языка | 3 — нужно сконфигурировать Gradle-модуль (7 таргетов) до контента | **5** — положить JSON-каталог, пересобрать |
| **Итог** | **34/45** | **44/45** |

**Победитель — `hybrid-plugins` (HYBRID).** Он прямо отвечает на явный приоритет задачи «MODULARITY» (Gradle-модуль только на реальной код/API-границе, конкретный список модулей, направление зависимостей, конвенция для таргетов, как `:shared`/`:composeApp` расщепляются без поломки framework export) и на приоритет «гибкая расширяемость» — таблица §4.2 этого документа воспроизводит его подход буквально.

**Взято у `data-tables` (graft):** явная 5-структурная таблица сведения 16 польских skill к core-структурам (§2 этого плана) — она конкретнее и педагогичнее, чем общий каталог `constructions.json` из B; поле `composesOf` для `core.composite` как явный якорь открытого риска цепочки/`mixed`; терминология `ChainBuilder`/`MatrixTableEngine`, ближе к реальным именам в коде (`ReferenceChainRow`, `chainRows`, `MatrixSection` — код-мэп §4.6). Список из ~10 операторов шаблона взят в редакции B (`order/when/agree/gov/periphrasis/prepend|append/rewrite/optional/alt/punct`) — он ближе к «GF-lite» и включает грамматически осмысленные `agree`/`gov`, а не только механические строковые операции A.

---

## 10. Риски и открытые вопросы (перенесены из обоих дизайнов, не решены здесь)

1. **`core.composite`** (цепочка/`mixed`) — последовательное применение шагов к одному `FeatureBundle` не проверено на реальных многошаговых цепочках других языков.
2. **Падеж vs семантическая роль** для языков без падежей — если деградация `core.argument.case-role` в «предлог+порядок слов» не влезает в один оператор шаблона, потолок ~10 операторов будет нарушен уже на втором языке; нужно проверить на реальном `lang/en` до фиксации списка.
3. **Ползучая сложность шаблонного языка** в сторону Grammatical Framework — 11-й оператор — сигнал остановиться и оформить ADR, а не расширять DSL.
4. **Морфологический build-скрипт** не проверен на нероманских системах (турецкая гармония, японские классы глаголов) — ссылка на Forsberg/Hulden (55 языков Wiktionary) — предположение автора отчёта, не верифицированный факт.
5. **UD-парсер как фильтр сборки** — конкретный парсер/лицензия не выбраны.
6. **ShareAlike-лицензии** производного пакета данных — юридическое решение, не архитектурное.
7. **Golden-фикстуры как тормоз** — контент pl-ru активно меняется (см. git status), фикстуры нужно зафиксировать на коммите перед стартом UC-05.
8. **Второй язык для проверки универсальности** (рекомендация, не решение): типологически далёкий — китайский первым (падежей/согласования нет, проверяет «пустой» `lang.json`), тюркский/турецкий вторым (гармония гласных проверяет build-скрипт на агглютинации). Пример в §3.3 (en-ru) — иллюстрация формата, не рекомендация по выбору.

---

## 11. Исследования (ссылки)

Метод Петрова и родственные: [englishtexts.ru урок 1](https://englishtexts.ru/misc/poliglot-angliyskiy-za-16-chasov) · [Grammatical Framework RGL](https://www.grammaticalframework.org/lib/doc/synopsis.html) · [Language Transfer](https://www.languagetransfer.org/courses).
Схемы признаков: [UniMorph schema](https://unimorph.github.io/schema/) · [UD features](https://universaldependencies.org/u/feat/all.html) · [Marrying UD and UniMorph](https://arxiv.org/pdf/1810.06743) · [UniMorph 4.0](https://arxiv.org/pdf/2205.03608).
CEFR-инвентари: [Cambridge English Grammar Profile](https://www.cambridge.org/elt/blog/2015/11/11/introducing-english-grammar-profile-1-building-profile/) · [CoE Reference Level Descriptions](https://www.coe.int/en/web/common-european-framework-reference-languages/reference-level-descriptions) · [польские стандарты Dz.U. 2016](https://certyfikatpolski.pl/wp-content/uploads/2018/05/rozp_26_2_16.pdf).
Данные и лицензии: [Morfeusz/SGJP license (BSD)](https://morfeusz.sgjp.pl/doc/license/) · [unimorph/pol](https://github.com/unimorph/pol) · [Wikidata Lexemes (CC0)](https://www.wikidata.org/wiki/Wikidata:Lexicographical_data/Documentation) · [Unicode CLDR plurals](https://cldr.unicode.org/translation/getting-started/plurals) · [FrequencyWords](https://github.com/hermitdave/FrequencyWords) · [Leksjo/NKJP (CC BY 4.0)](https://github.com/KubaCiolo/leksjo-dane).
Доказательная база подачи: [Pashler et al. 2008, learning styles](https://journals.sagepub.com/doi/full/10.1111/j.1539-6053.2009.01038.x) · [meta-анализ 2024](https://www.frontiersin.org/journals/psychology/articles/10.3389/fpsyg.2024.1428732/full) · [Norris & Ortega 2000, explicit instruction](https://onlinelibrary.wiley.com/doi/abs/10.1111/0023-8333.00136) · [McManus & Marsden 2017, L1 explicit info](https://eric.ed.gov/?id=EJ1152557) · [Adesope et al. 2017, retrieval practice](https://journals.sagepub.com/doi/abs/10.3102/0034654316689306).
Морфология: [Forsberg/Hulden, paradigm extraction](https://www.researchgate.net/publication/301405072_Generalizing_Inflection_Tables_into_Paradigms_with_Finite_State_Operations).
Полный список источников и разбор по типологической таблице — [UniversalCoreResearch.md](UniversalCoreResearch.md).

---

## 12. Задачи миграции (малые, с приёмкой, UC-xx)

Каждая задача — одна возможность × одно место, отдельный PR. Порядок фиксирован: 1-4 разблокируют параллельную работу без риска для данных pl-ru; 5-11 — самая рискованная часть (движок), поэтому идёт с golden-фикстурами; 9,10,13 механические и параллельны по хостам; 14-15 — второй язык, только после стабилизации контрактов.

| ID | Задача | Место | Приёмка | Параллельно с |
|---|---|---|---|---|
| UC-01 | `:core-model`: `FeatureKey/FeatureValue/FeatureBundle/Construction/SkillSpec`; польские enum — тонкий адаптер сверху | `model/Grammar.kt` (NEW модуль) | Компилируется; текущие enum-тесты зелёные без изменения ожиданий | — (первая) |
| UC-02 | Обобщить `generateCoursePackSource` → сканирование каталога + манифест пакетов в `:pack-format`; pl-ru продолжает читаться как v1 через `CoursePackSource` | `kotlin/shared/build.gradle.kts:6-42` | Тот же `course.json` даёт тот же runtime-результат (JSON fixture-сравнение) | — |
| UC-03 | `CoursePack`/`PackRegistry` вместо глобалов `PolishCourseData.*` | `data/CourseData.kt` | Все `Course*Test.kt` зелёные без изменения ожиданий | UC-11, UC-13, UC-16 |
| UC-04 | Прогресс/vocabulary/preferences читают `pack.id`/`pack.pairId` вместо литералов `"pl-ru"`; `activePackFilter` в `ProgressRepository`/`SkillQueue` | `progress/*`, `vocabulary/VocabularyDocument.kt:38,96,102,126`, `preferences/UserPreferencesCodec.kt:31,52` | Существующий сохранённый прогресс (React-экспорт и KMP-документ) загружается неизменным; golden JSON совпадает | UC-11, UC-13, UC-16 |
| UC-05 | `TableMorphology` + `forms.generated.json`, сгенерированные конвертером из текущего `course.json` | NEW `scripts/build-pack.mjs` (частично) | Verb/noun/adjective/possessive form fixtures (490 possessive и др.) побайтно совпадают со старым `GrammarEngine` | UC-06 |
| UC-06 | `curriculum.json` (16 `SkillSpec` из `when(skillId)`-веток) | `training/ExerciseFactory.kt:49-136` | `SkillSpec` данные покрывают все 16 ID с теми же `focus`/`fixed`/`lexicalFilter`, что текущие ветки | UC-05 |
| UC-07 | `ConstructionRealizer`+`TemplateInterpreter`(~10 операторов)+`ExerciseGenerator` в `:core-engine`; **golden-фикстуры зафиксированы на коммите перед стартом** (см. §6 риск) | `grammar/GrammarEngine.kt`, `training/ExerciseFactory.kt` | Побайтное совпадение с фикстурами (490 possessive, все skill×seed, полная цепочка) на JVM/JS/Wasm/Native | — (после UC-05,06) |
| UC-08 | Переключение: `ExerciseFactory`/`GrammarEngine` удаляются, `ExerciseGenerator` — единственный путь | те же файлы | Все существующие Kotlin/React parity-тесты и Playwright UX-спеки зелёные на всех таргетах | — (gate, после UC-07) |
| UC-09 | `MatrixTableEngine`+`MatrixTableViewModel` + один тонкий renderer на хост (можно по хостам параллельно) | `MatrixWeb.kt`, `MatrixScreen.kt`, `AndroidMatrixScreen.kt`, iOS/macOS Matrix-экраны | Скриншот-паритет с текущими таблицами (те же значения в тех же ячейках) | UC-10, UC-13; сам по себе параллелен по хостам |
| UC-10 | Стили как данные (`styles/*.json`+`StyleComposer`→`List<Block>`), `Logic/Situations`→`rule-first/situation-first` на границе UI | `AppUiState.kt:12`, `UserPreferences.kt:3`, `CourseData.kt:23-28` | Оба стиля рендерят тот же контент, что сегодня, при тех же текстах | UC-09, UC-13 |
| UC-11 | `:morph-api` — интерфейс `MorphologyPlugin` зарезервирован, без реализации для pl | NEW модуль | Компилируется, не используется в runtime pl-ru | UC-03, UC-04, UC-13, UC-16 |
| UC-12 | Schema v2 (core/lang/pair) + `validate-pack-v2.mjs` + `migrate-v1-to-v2.mjs`, конвертирующий `course.json` **после** зелёных фикстур UC-07/08 | `courses/schema/*`, `courses/pl-ru/*` | Валидатор v2 проходит; движок на v2-пакете даёт тот же golden-вывод, что на v1 | — (после UC-08) |
| UC-13 | Таблица UI-строк (`ui-strings/{ru,en}.json`) вместо кириллицы в коде хостов | ~45 файлов, код-мэп §4.10 | pl-ru экран визуально идентичен; строка появляется в таблице | Полностью параллельна всем остальным |
| UC-14 | Второй target-язык (типологически далёкий — рекомендация zh) только данными, без пары | `lang/zh/*` (NEW) | Ноль правок `:core-*`/`:pack-format`/`:morph-api`; если правки понадобились — фиксируется как баг архитектуры | — (после UC-01..UC-11 стабильны) |
| UC-15 | `pairs/zh-ru/pair.json` (или выбранная L1) для второго языка | NEW | Контраст рендерится для перенесённых skill, `fallback` стиля не срабатывает без нужды | — (после UC-14) |
| UC-16 | Зарезервировать порты будущего (`AudioProvider`, `AnswerInput.Spoken`+`SpeechRecognizer`, `ExplanationProvider`/`HintProvider`) — только форма, без реализации | `:core-model`, `:core-presentation` | Компилируется; не подключено ни к одному реальному провайдеру | UC-03, UC-04, UC-11, UC-13 |

Шаги 1-4 (S) не зависят от выбора второго языка и не меняют наблюдаемое поведение pl-ru. Шаг 8 — обязательный gate перед 12. Шаг 14 — критерий приёмки всего плана: если добавление второго языка потребовало правки `:core-*`, ядро не универсально.
