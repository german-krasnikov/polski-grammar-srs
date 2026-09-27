# Журнал решений (ADR)

Новые сверху. Формат: решение → почему → где подробно.

## ADR-16 · 2026-09-27 · UC-05: `:core-engine` (Morphology/TableMorphology) + `forms.generated.json`
Новый модуль `:core-engine` (через `polski.kmp-common`, зависит только от `:core-model`) вводит `Morphology`
(`fun form(lexeme, bundle): String`) и `TableMorphology` — чистый lookup, без правил в рантайме. `scripts/build-pack.mjs`
(NEW, Node, по образцу `validate-course.mjs`) материализует noun/adjective/verb/possessive формы из
`courses/pl-ru/course.json` в `courses/pl-ru/forms.generated.json` (лексема с префиксом категории, например
`noun:wife`/`verb:have`/`possessive:my`, → список `{bundle, form}`); `--check` пересчитывает и сверяет побайтно с
`tests/fixtures/core-golden/grammar.json`. `forms.generated.json` встроен в `:shared`'s `commonTest` (новая задача
`generateFormsFixtureSource`, тот же приём встраивания JSON-в-Kotlin-строку, что `generateCoursePackSource`) —
`TableMorphologyParityTest` строит `TableMorphology` из этого встроенного JSON и проверяет побайтное совпадение с
`GrammarEngine` на каждой лексеме × наборе признаков (носители — существующие `nouns`/`adjectives`/`verbs`/`possessives`).
`GrammarEngine`/`ExerciseFactory` остаются живым путём — переключение (UC-07/08) не входит в эту задачу;
`ExerciseFactory.kt` не тронут (параллельная задача UC-06).
Почему: приёмка `UniversalCorePlan.md` §12 UC-05 требует именно эту пару (таблица + конвертер) до
`ConstructionRealizer`/`ExerciseGenerator` (UC-07), и явно фиксирует риск §6 — golden-фикстуры зафиксированы на
коммите `a4b4aec`, поэтому «побайтное совпадение» проверяемо, а не плывущая цель.
Подробно: `Plans/Kotlin/UniversalCorePlan.md` §5.1, §5.3.1, §8, §12 UC-05; `scripts/build-pack.mjs`;
`kotlin/core-engine/src/commonMain/kotlin/polski/core/engine/Morphology.kt`;
`kotlin/shared/src/commonTest/kotlin/polski/grammar/TableMorphologyParityTest.kt`.

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
