# Журнал решений (ADR)

Новые сверху. Формат: решение → почему → где подробно.

## ADR-16 · 2026-09-27 · UC-06: `lang/pl/curriculum.json` (16 SkillSpec) как проверяемая копия `ExerciseFactory`, не замена
`SkillSpec` (`:core-model`, UC-01) получил `lexicalFilter: LexicalFilter?` (`slot` + `where: Map<String, List<String>>`) — свойство кандидата и список допустимых значений, например `case.acc.f` → `{"gender": ["f"]}`, `case.inst` → `{"nounId": [...]}`. Новый `courses/lang/pl/curriculum.json` несёт все 16 skill ID с `focus`/`fixed`/`lexicalFilter`, вручную сверенными с ветками `ExerciseFactory.kt:49-136`; `level`/`prerequisites` не задублированы — файл читается и сравнивается с `skillById(...)` из уже загруженного `course.json`. Файл встроен в Kotlin тем же приёмом, что `course.json`/`styles/*.json` (`generateCoursePackSource` в `kotlin/shared/build.gradle.kts` сканирует `courses/lang/*/curriculum.json`), читается `polski.training.plCurriculum`/`parseCurriculum` и покрыт `CurriculumReaderTest` (11 тестов) на JVM/JS/Wasm/iOS-sim/macOS. Схема — `courses/schema/curriculum-v1.schema.json`, валидатор — `scripts/validate-curriculum.mjs`, встроен в `npm run course:validate`.
`ExerciseFactory` остаётся единственным живым путём генерации упражнений — `plCurriculum` не читается runtime-кодом хостов, это данные для будущего `ExerciseGenerator` (UC-07). Для `agreement.my` (случайный владелец из 6), `pronouns` (замена именной группы местоимением), `aspect` и `mixed` (составные изменения ≥2 осей одновременно) `focus = null` — код не делает единственный flip одного признака, поэтому значение не выдумывается.
Почему: план требует данные, покрывающие все 16 ID «с тем же `focus`/`fixed`/`lexicalFilter`, что текущие ветки», без замены `ExerciseFactory` до параллельного parity-gate (UC-07). `GrammarEngine`/морфология не тронуты — параллельная работа по UC-05 на `main`.
Подробно: `Plans/Kotlin/UniversalCorePlan.md` §3.1-§3.2, §5.3, §12 UC-06; `kotlin/core-model/src/commonMain/kotlin/polski/core/model/FeatureBundle.kt`; `kotlin/shared/src/commonMain/kotlin/polski/training/Curriculum.kt`.

## ADR-15 · 2026-09-27 · Второй пакет — английский для русскоязычных (en-ru) + «Лайфхаки» вне ядра
Проверка ядра — пакет `en-ru` (вместо рекомендованного в плане zh), использующий всё: 4 стиля, правила, карты слов, таблицы, подсветку, L1-сравнение с русским. Ядро должно поддерживать любую пару «родной → изучаемый» (ru→pl, ru→en, pl→en, en→ru …); найденные при этом упущения ядра дорабатываются в ядре, а не обходятся в пакете.
**Лайфхаки — вне ядра.** Это приёмы, которые облегчают понимание темы носителю конкретного родного языка при изучении конкретного целевого (у ru→en одни, у ru→pl другие, у pl→en третьи). Живут в слое пары (`pairs/<target>-<native>/lifehacks`), привязаны к навыку/теме, имеют источник и статус проверки (`editorial` — отобраны редакцией по исследованиям; `community` — проверены пользователями: «помогло / не помогло»). Сейчас — только статичные отобранные лайфхаки; приём и проверка лайфхаков от пользователей требуют сервера и идут отдельной задачей через зарезервированный порт (`LifehackProvider`), ядро от неё не зависит.
Почему: пользователь хочет проверить универсальность ядра на реальном курсе; en-ru типологически далёк от pl-ru (без падежей, аналитические времена, артикли, do-support) и сразу полезен; ценность лайфхаков — в опыте носителей, а не в грамматике.
Порядок: UC-05/06 → UC-07/08 (универсальный движок, побайтный паритет pl-ru) → UC-09 (таблицы как данные) + UC-12 (схема v2) → пакет en-ru с лайфхаками для ru→en (и добор для ru→pl). Критерий: ноль правок кода ядра ради en-ru.

## ADR-14 · 2026-09-27 · UC-01-correction: Android-пикер стилей остаётся закрытым, `persistStyle` — терпимым
`AndroidStylePicker` перечисляет `builtInStyleIds` (как и Web/Desktop-пикеры), а не все ключи `StyleRegistry` — `AndroidSessionViewModel.persistStyle` до сих пор пишет через закрытый 4-значный enum `PreferredStyle`, и `PreferredStyle.valueOf(styleId.value)` без обработки падал на любом id вне этих 4. `persistStyle` дополнительно сделан терпимым (`PreferredStyle.entries.firstOrNull { it.name == styleId.value } ?: return` — no-op вместо падения) на случай, если `AppUiState.styleId` получит внешний id не через этот пикер.
Почему: ADR-13 открыл `StyleId`/`StyleRegistry`, но `AndroidStylePicker` был расширен до `registry.keys.forEach` в том же коммите без сопоставления с `PreferredStyle` — 5-й `courses/styles/*.json` рецепт ронял приложение на первом же выборе стиля в Android-настройках. Автообнаружение новых стилей во всех хостах — отдельная задача.
Подробно: `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidStylePicker.kt`, `kotlin/androidApp/src/main/java/dev/polski/grammarmatrix/AndroidSessionViewModel.kt`.

## ADR-13 · 2026-09-27 · UC-01: первый `:core-*` модуль и открытый `StyleId`
`:core-model` (через новый convention-плагин `polski.kmp-common`, те же 7 KMP-таргетов, что `:shared`) вводит `FeatureKey`/`FeatureValue`/`FeatureBundle`/`Construction`/`SkillSpec`; польские enum (`model/Grammar.kt`) не переименованы — сверху добавлен тонкий адаптер (`toFeatureValue()`, `CaseFeature`/`NumberFeature`/…), декларирующий подмножество открытого каталога. `StyleId` (UC-10) заменён с закрытого enum на `data class StyleId(val value: String)` с 4 именованными константами того же wire-значения — `StyleRegistry` больше не отбрасывает нераспознанный `id` рецепта, любой JSON-файл в `courses/styles` становится стилем без правки Kotlin.
Почему: приёмка `UniversalCorePlan.md` §12 UC-01 требует пилотный `:core-*` модуль и демонстрацию открытого каталога признаков; закрытый `StyleId` был единственным местом, где «5-й стиль» требовал Kotlin-правки, противореча ADR-12.
Подробно: `Plans/Kotlin/UniversalCorePlan.md` §1, §4.1, §4.3; `Plans/Kotlin/StylesBlueprint.md` §2, §4.

## ADR-12 · 2026-09-26 · Универсальное ядро, гибридная архитектура
Общий костяк «конструкция × признаки» для всех языков; языки, L1-слои, пары и стили — данные; Gradle-модули только на границах кода (`:core-*`, `:pack-format`, `:morph-api`); опциональные плагины морфологии. Главный критерий — гибкая расширяемость.
Почему: цель — много языков и родных языков с минимумом кода. Гибрид набрал 44/45 против 34/45 у «только данные».
Подробно: `Plans/Kotlin/UniversalCorePlan.md`.

## ADR-11 · 2026-09-26 · Четыре стиля подачи вместо «гуманитарий/технарь»
Стили: через правило, через ситуацию, через сравнение с родным языком, минимум теории. Стиль — предпочтение, а не диагноз; задания и FSRS общие.
Почему: старые «Логика/Ситуации» отличались одной строкой подсказки; «типы мозга» — нейромиф.

## ADR-10 · 2026-09-26 · Сейчас только статичные данные и карточки слов
Озвучка, голосовой ввод и ИИ — позже, через зарезервированные интерфейсы.

## ADR-9 · 2026-09-26 · Настройка «Анимации»
Выключено → Rive не загружается вообще, движение мгновенное. Хранится в общей `UserPreferences.animationsEnabled`.

## ADR-8 · 2026-09-26 · Поведение карточек
Задания — раскрытие вниз без переворота. Слова — переворот всей панели, смена стороны на 90°, клик переворачивает сразу. Оценка свайпом (влево — повторить, вправо — вспомнил); телефон без кнопок; ПК с кнопками и ←/→. Вкладки — пейджер. Круговой эффект Rive убран.
Заменяет: переворот карточек заданий (коммиты af126a6, b22e758).

## ADR-7 · 2026-09-26 · Rive через официальные рантаймы
Нативный переворот/свайп, Rive — эффекты поверх. Rive-CMP не берём (подходит только Compose-хостам); форк Rive-CMP — запасной вариант при проблемах порта.
Подробно: `Plans/Kotlin/RiveResearch.md`, `FlipCardRivePlan.md`, `RiveCatalog.md`.

## ADR-6 · 2026-09-26 · Хранение iOS на UserDefaults до релиза
Перенос в файлы с миграцией (M5) отложен: приложение в разработке, пользователей нет.
Подробно: `Plans/Kotlin/IosFileStorageBlueprint.md`.

## ADR-5 · 2026-09-25 · Этап 11 (React ↔ Kotlin parity gate) снят
Переход на Kotlin выполнен; React — только исторический эталон.

## ADR-4 · 2026-09-25 · Нативный UI на каждом хосте
Web — семантический DOM/CSS, Android — Compose Material 3, iOS/macOS — SwiftUI; общее — только Kotlin-ядро. Кастомное «стекло» откатано.
Подробно: `Plans/Kotlin/NativeHostDecision.md`, `PreGlassRollback.md`.
