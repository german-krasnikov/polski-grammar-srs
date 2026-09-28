# Архитектура

## Сейчас

Kotlin Multiplatform: общее ядро + нативный UI на каждой платформе. React-версия (`src/`) — исторический эталон, переход на Kotlin завершён.

```text
courses/core/*.json, courses/lang/pl/{lang,lexicon}.json, courses/pairs/pl-ru/pair.json   schema v2 (UC-12) — механическое
  разбиение courses/pl-ru/course.json (scripts/migrate-v1-to-v2.mjs) + формализация уже существующих Construction/
  template-op/exercise-kind фактов; validate-pack-v2.mjs — cross-checks §8.2. course.json НЕ переехал/не удалён (план §6) —
  остаётся живым входом для Gradle/React ниже; Kotlin-загрузчик v2-слоёв («CoursePackLoader») не построен — план §6/
  kotlin/pack-format/.../CoursePackSource.kt
courses/lang/en/{lang,lexicon,prepositions}.json (ADR-26, EnRuPackPlan.md §1.3/§6 EN-11) — первый второй язык; чистый
  v2, без course.json/v1-моста (тот мост pl-ru-специфичен, см. ниже). validate-pack-v2.mjs сканирует courses/lang/*
  и схема-проверяет lang.json/lexicon.json (courses/schema/lexicon-v1.schema.json, новая)/prepositions.json
  (courses/schema/prepositions-v1.schema.json, новая категория для case-role→предлог) для КАЖДОГО найденного языка;
  §8.2 cross-checks (construction/focus/fixed/lexicalFilter) запускаются только если у языка уже есть curriculum.json
  (пока только pl — EN-12 добавит его для en). reconstructCoursePack→course-pack-v1.schema.json→validateCoursePack
  остаётся ЖЁСТКО pl-ru-специфичным (v1-схема буквально кодирует pl'ские падежные метки как ключи объекта) — не
  генерализован, en идёт мимо него до EN-04/EN-05 (:core-engine v2-загрузчик, гэп G).
courses/*/course.json ──(build: generateCoursePackSource scans courses/*, UC-02)──► kotlin/shared (через :pack-format CoursePackSource)
courses/lang/<code>/curriculum.json ──(тот же generateCoursePackSource, сканирует courses/lang/*, UC-06)──► polski.training.plCurriculum ──► polski.core.plExerciseGenerator (UC-08, живой путь)
kotlin/build-logic         convention-плагин polski.kmp-common — 7 KMP-таргетов для новых :core-* модулей
kotlin/core-model          FeatureKey/FeatureValue/FeatureBundle, Construction, SkillSpec (UC-01, первый :core-* модуль)
kotlin/pack-format         CoursePackSource/EmbeddedCoursePackSource/PackManifest (UC-02) — раздача сырого JSON пакета по id
kotlin/core-engine         Morphology/TableMorphology (UC-05) — табличный lookup формы по (lexeme, FeatureBundle);
                           forms.generated.json (scripts/build-pack.mjs, из courses/pl-ru/course.json) — единственный
                           источник форм; ConstructionRealizer/TemplateInterpreter (order/gov/agree, ≤10 операторов)/
                           ExerciseGenerator (UC-07) читают realization.json/exercise-recipes.json. UC-08: :shared
                           (commonMain) зависит на :core-engine (api); GrammarEngine.kt/ExerciseFactory.kt удалены —
                           polski.grammar.PackMorphology.kt (nounForm/adjectiveForm/possessiveForm/verbForm/nounPhrase/
                           caseSentence) и polski.training.PlExerciseEngine оборачивают plMorphology/plExerciseGenerator
                           (polski.core.PlEngine.kt) с прежней публичной сигнатурой — ни один хост/консьюмер не менял
                           контракт. Постоянные golden-guards: TrainingParityTest/GrammarParityTest (буквальные
                           ожидания из tests/fixtures/kotlin-parity), TableMorphologyParityTest (исчерпывающий паритет
                           таблицы форм)
kotlin/shared (commonMain)
  model, data, grammar      польские данные и морфология (polski.grammar.PackMorphology поверх :core-engine); model/Grammar.kt — тонкий адаптер над :core-model
  srs                       FSRS (порт ts-fsrs 5.4.2)
  progress, vocabulary      документы прогресса/словаря, кодеки, репозитории
  preferences               UserPreferences (тема, движение, animationsEnabled, методика…)
  presentation              TrainingStore (единственный владелец сессии), AppAction/AppUiState, CardEffect;
                           Lifehack/LifehackProvider/StaticPackLifehackProvider (ADR-27, EN-21) — читает
                           `pairs/<pairId>/lifehacks.json` через generatedLifehacksJsonByPairId
  iosMain / macosMain       Swift-мосты (IosSession, MacSession, snapshots)
kotlin/composeApp
  webMain                   Kotlin/JS + Wasm, семантический DOM/CSS (не Compose UI)
  androidMain               экраны Jetpack Compose Material 3
  desktopMain               JVM Compose Desktop — сохранённое превью
kotlin/androidApp           Android-приложение
kotlin/iosApp               SwiftUI iPhone/iPad (проект генерируется generate_project.rb)
kotlin/macosApp             SwiftUI macOS
```

Правила:

- `TrainingStore` — единственный владелец сессии: одна оценка = один review, сохранения упорядочены по ревизии, ошибка записи видна пользователю.
- Хост не принимает учебных решений — только отображает состояние и отправляет действия.
- Rive — только декоративный слой поверх карточки: не перехватывает ввод, скрыт от screen reader, не грузится при выключенных анимациях или reduced motion. Рантаймы: `@rive-app/canvas-lite` 2.43.1 (web, self-hosted, ленивый), `rive-android` 11.12.1, `RiveRuntime` 6.27.0 (iOS/macOS). Ассеты и лицензии — `THIRD_PARTY/credits.md`.

Версии: Kotlin 2.4.20, Compose Multiplatform 1.12.1, Gradle 9.3.1, AGP 9.1.1, JDK 21 arm64.

## Карточки и движение (все хосты)

- **Задания («Карточки»)** — без переворота; ответ раскрывается расширением карточки вниз.
- **Слова** — переворачивается вся панель целиком, смена стороны ровно на 90°; клик = раскрыть и перевернуть.
- **Оценка** — свайп всей карточки: влево «Повторить», вправо «Вспомнил». На телефоне без кнопок; на ПК кнопки + ←/→.
- **Вкладки** — пейджер (экраны едут лентой), панели сворачиваются анимированно.
- Настройка «Анимации» выключает Rive полностью и делает движение мгновенным.

## Цель: универсальное ядро

Подход — гибридный ([UniversalCorePlan.md](../Plans/Kotlin/UniversalCorePlan.md)). Gradle-модуль заводится только на границе кода/API; языки и пары — данные.

```text
:core-model         FeatureBundle (UD/UniMorph), Construction, SkillSpec (+ LexicalFilter, UC-06), порты будущего
:core-engine        Morphology/TableMorphology (UC-05); ConstructionRealizer/TemplateInterpreter (≤10 операторов)/ExerciseGenerator (UC-07) — единственный движок с UC-08 (:shared зависит на :core-engine как api, хосты переключены, GrammarEngine/ExerciseFactory удалены); MatrixTableEngine (UC-09, ADR-21) — generic row-axis×columns builder; polski.presentation.MatrixTableViewModel (:shared) навешивает ContrastPair-подсветку сверху. UC-09 часть 2/2: хосты рендерят матрицы через MatrixTableEngine.build(...).toViewModel() (web, Android, iOS, macOS — см. ADR-22 и следующие); layout каждого хоста не менялся. EN-24 (ADR-28, гэп H минимум): PackEngine.morphology читает generatedFormsGeneratedJsonByLang[langId] с фолбэком на старый pl-фикстур (byte-identical для pl); courses/lang/en/forms.generated.json — scripts/build-pack-en.mjs из lexicon.json+prepositions.json (ADR-39: материализует ВСЕ категории ConstructionRealizer может запросить — noun/adjective/possessive/pronoun/prep/neg/verb/aux — не только verb-flat-поля; en-ru реально генерирует упражнения на всех 5 хостах, не только матрицу); только web (MatrixWeb.kt) читает enMorphology для EN-24's английской матрицы — 4 других хоста остаются на pl-литералах там, задокументированный техдолг, не тихий пропуск
:core-srs           FSRS
:core-progress      прогресс по pack.id
:core-presentation  сессии, StyleComposer (стили → блоки)
:pack-format        схема v2, загрузчик, валидатор, PackRegistry — EN-22 (ADR-30/31 android, ADR-33 iOS): production `packRegistry` теперь реально несёт 2 пакета (pl-ru v1 `course.json` + en-ru реконструирован из v2-слоёв через уже готовый `CoursePackLoader`, EN-04), с `contains`/`options` для host-пикеров; `select` — первый production-вызов (host preferences-сессии), а не только тест. `usableCourseSelections` — подмножество, чей `CoursePack` парсится целиком (EN-22 §7 item 1, ADR-36: `CoursePack`'s pl-специфичные grammar-поля — `Noun.gender`, case-keyed `possessiveForms`/`futureAuxiliary`/`reference`-строки — стали опциональными/языко-нейтральными, поэтому теперь оба зарегистрированных пакета, pl-ru и en-ru, парсятся целиком и попадают сюда); все хосты (Android `selectActiveCoursePack`, macOS/desktop/iOS `availableCoursePacks`/`selectCoursePack`) делают активным только его; глобалы пакета читают `packRegistry.active` живьём (ADR-35) с пустым/blank фолбэком вместо `!!` там, где активный пакет не декларирует pl-специфичную reference-секцию (ADR-36); Training-движок (`plExerciseGenerator`/`plMorphology`/`plChainSteps`) уже привязан к активному пакету (`PackEngine(packRegistry.active.targetLanguage)`, `PlEngine.kt`); en-ru реально генерирует все 16 skills + 5-шаговую цепочку на любом хосте (ADR-39 закрыл последний блокер — `forms.generated.json`'s content gap плюс `caseSentencePrefix`/`personalPronounForm`'s pl-only `GramCase`-форсинг)
:morph-api          интерфейс MorphologyPlugin (для языков, где таблиц мало)
:shared :composeApp :androidApp   хосты — имена не меняются
courses/core|lang|pairs/*          языки, L1 и пары как JSON/Markdown, подхватываются сканированием каталога
styles/*.json                      стили подачи как данные
```

Зависимости: ядро не знает ни одного языка; хосты зависят от ядра и выбранных пакетов. Новый язык — ноль правок в `:core-*`, `:pack-format`, `:morph-api`.

Зарезервированные интерфейсы (без реализации): `AudioProvider`, `AnswerInput.Spoken` + `SpeechRecognizer`, `ExplanationProvider`, `HintProvider`. `LifehackProvider` (тот же паттерн) реализован — см. ADR-27.

Совместимость `pl-ru`: ID навыков не меняются, ключи прогресса v1 сохраняются, настройки v2→v3 читаются без потерь. Переход на новый движок проходит только через эталонные фикстуры, зафиксированные на коммите.
