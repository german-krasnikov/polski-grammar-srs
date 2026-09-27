# Журнал решений (ADR)

Новые сверху. Формат: решение → почему → где подробно.

## ADR-16 · 2026-09-27 · UC-03: `CoursePack`/`PackRegistry` вместо `PolishCourseData`
`internal object PolishCourseData` (`data/CourseData.kt`) стал `internal class CoursePack(source: CoursePackSource)` — тот же набор `by lazy`-полей (nouns/adjectives/verbs/skills/…), плюс `id`. Единственная точка входа теперь `internal val packRegistry: PackRegistry by lazy { PackRegistry(embeddedCoursePackSources.map(::CoursePack)) }`, где `PackRegistry.active` — единственный (pl-ru) пакет; все прежние обращения `PolishCourseData.X` (в `Adjectives/Nouns/Pronouns/Skills/Verbs/Vocabulary.kt`, `grammar/GrammarEngine.kt`, `grammar/GrammarReference.kt`) заменены на `packRegistry.active.X`. Схема v1, авторский `course.json` и все проверки не изменились — файл ещё pl-ru-специфичен, generic-схема (`CoursePack(core, lang, pair)` из §4.1) остаётся задачей UC-05+/UC-12.
Почему: приёмка `UniversalCorePlan.md` §12 UC-03 требует, чтобы потребители брали активный пакет из реестра, а не из захардкоженного глобального объекта — это готовит почву для UC-04 (`pack.id`/`pack.pairId` вместо литералов `"pl-ru"`) без смены схемы или поведения хостов.
Подробно: `Plans/Kotlin/UniversalCorePlan.md` §4.1, §12 UC-03; `kotlin/shared/src/commonMain/kotlin/polski/data/CourseData.kt`.

## ADR-15 · 2026-09-27 · UC-02: `:pack-format` и сканирование каталога пакетов
`generateCoursePackSource` (`kotlin/shared/build.gradle.kts`) больше не читает один захардкоженный путь `courses/pl-ru/course.json` — сканирует `courses/*` на подкаталог со своим `course.json` (кроме `schema`/`styles`) и оборачивает содержимое каждого найденного пакета в `EmbeddedCoursePackSource` (новый модуль `:pack-format`, `polski.pack.CoursePackSource`/`PackManifest`). `PolishCourseData` теперь читает pl-ru через `embeddedCoursePackSources.first { it.id == "pl-ru" }`, а не через прямое имя сгенерированного свойства — сама схема v1 не изменилась. Байт-идентичность runtime-результата подтверждена fixture-тестом (`CoursePackFixtureTest`, сериализация `PolishCourseData.skills` пиновалась до рефакторинга и совпала после).
Почему: приёмка `UniversalCorePlan.md` §12 UC-02 требует, чтобы второй пакет добавлялся каталогом без правки Gradle/Kotlin; закрытый путь к одному файлу был единственным местом, которое такую правку требовало.
Подробно: `Plans/Kotlin/UniversalCorePlan.md` §4.1, §4.2, §12 UC-02; `kotlin/pack-format/`, `kotlin/shared/build.gradle.kts`, `kotlin/shared/src/commonTest/kotlin/polski/data/CoursePackFixtureTest.kt`.

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
