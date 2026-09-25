# Срез course pack: таблица «Опора на русский»

## Scope и потребители

После переноса `reference.systemCards` и `reference.pipeline` следующий ограниченный срез этапов 1–2 [CoursePacksPlan.md](CoursePacksPlan.md) — четыре строки русскоязычной опоры на экране карты. Сейчас они захардкожены в `src/ui/GrammarTables.tsx` (`GrammarTables`), `kotlin/composeApp/src/webMain/kotlin/polski/ui/MatrixWeb.kt` (`renderMap`), `kotlin/composeApp/src/desktopMain/kotlin/polski/ui/screens/MatrixScreen.kt` (`MapDesktop`), `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidMatrixScreen.kt` (`AndroidMapSection`) и `kotlin/iosApp/PolskiGrammar/PolskiGrammarApp.swift` (`map`). iOS получает другие справочные строки через `kotlin/shared/src/iosMain/kotlin/polski/ios/IosSnapshot.kt` (`matrixSnapshot`).

Это один набор учебных сопоставлений с четырьмя вариантами плотности/формулировки, уже существующими в хостах. Срез переносит заголовки, три названия колонок и все четыре строки, не редактируя смысл, пунктуацию, порядок и способ показа: React/Kotlin web/Desktop сохраняют таблицу, Android/iOS — четыре отдельные строки. Не объединять редакторские варианты в одну «улучшенную» версию в этом переносе. Близкие тексты про Wołacz, местоимения и таблицы времён имеют другие потребители и остаются отдельными срезами.

## Контракт авторских данных

Добавить обязательный `reference.russianSupport` в `courses/pl-ru/course.json` с `title.full = "Опора на русский: что переносится, а что проверить"`, `title.compact = "Опора на русский"`, `columns = ["Русская опора", "Польская конструкция", "Проверка"]` и `rows` в точном порядке `accusative`, `instrumental`, `locative`, `possessive`. У каждой строки `id`, `cue`, три явных table-варианта `{construction, check}` (`react`, `web`, `desktop`) и `mobileLine`. Выбор variant является презентационной политикой хоста, а не новым типом упражнения; вариант не зависит от выбранной методики или FSRS. Явные строки вместо fallback/склейки нужны из-за реальных различий в окончаниях предложений, написании польской конструкции и длине пояснения. Формат не содержит HTML или вычисляемых шаблонов.

Точные значения baseline (кавычки отделяют данные, не входят в значение):

| ID / cue | React `construction` / `check` | Kotlin web `construction` / `check` | Desktop `construction` / `check` | Android и iOS `mobileLine` |
| --- | --- | --- | --- | --- |
| `accusative` / «вижу кого? что?» | «Widzę moją żonę.» / «Логика винительного знакома; польские окончания нужно менять во всей группе.» | «Widzę moją żonę.» / «Польские окончания меняются во всей группе.» | «Widzę moją żonę.» / «Окончания меняются во всей группе» | «вижу кого? что? → Widzę moją żonę. Окончания меняются во всей группе.» |
| `instrumental` / «с моей женой» | «z moją żoną» / «Польское женское -ą соответствует здесь творительному; это же окончание есть у прилагательного в Bierniku.» | «z moją żoną» / «Женское -ą здесь соответствует творительному.» | «z moją żoną» / «Женское -ą — творительный» | «с моей женой → z moją żoną. Женское -ą — творительный.» |
| `locative` / «говорю о жене» | «mówię o żonie» / «Местный падеж требует предлога; żona → żonie.» | «mówię o żonie» / «Местный падеж требует предлога; żona → żonie.» | «mówię o żonie» / «Местный требует предлога» | «говорю о жене → mówię o żonie. Местный требует предлога.» |
| `possessive` / «мой / его / их» | «moją żonę / jego żonę / ich żonę» / «jego, jej, ich не склоняются. Формы mojego и mojej зависят от предмета обладания.» | «moją żonę / jego żonę / ich żonę» / «jego, jej, ich не склоняются.» | «moją / jego / ich żonę» / «jego, jej, ich не склоняются» | «мой / его / их → moją / jego / ich żonę. jego, jej, ich не склоняются.» |

Поле `mobileLine` намеренно хранит всю видимую строку: у первого `construction` уже есть точка, у остальных пунктуация добавляется по-разному; простое `cue + construction + check` легко изменит baseline. У Android и iOS мобильные строки побайтно одинаковы, поэтому отдельные copies не нужны. Различия React/Kotlin web/Desktop остаются именованными в пакете, чтобы позднее редакторское выравнивание было явным изменением учебного текста, а не побочным эффектом архитектурной правки.

## Компоненты и зависимости

| Существующий путь | Контракт для разработчика |
| --- | --- |
| `courses/schema/course-pack-v1.schema.json` | Расширить `reference`: обязательный объект с `additionalProperties: false`, точными названиями variant, четырьмя rows и непустыми строками. `id` ограничены четырьмя значениями. |
| `scripts/validate-course.mjs` | Проверить порядок ID, отсутствие дублей и что каждый `mobileLine` начинается с соответствующего `cue + " → "`; выдавать путь `/reference/russianSupport/rows/<n>/...`. Точность самих форм фиксируют pinned tests, валидатор не должен запрещать будущие редакторские исправления только потому, что они отличаются от нынешнего текста. |
| `src/data/course.ts` | Экспорт `courseRussianSupport` без преобразования text fields; `GrammarTables.tsx` выбирает `react`, оставляя текущие `<section>/<table>/<thead>/<tbody>` и cell order. |
| `kotlin/shared/src/commonMain/kotlin/polski/data/CourseData.kt` | Типизированные `RussianSupportRow`/`RussianSupportTable` или эквивалентный узкий read-only contract; `referenceRussianSupport` читает тот же встроенный JSON, валидирует order/required fields. Не вводить Compose/DOM/Swift типы в shared. |
| `kotlin/composeApp/src/webMain/kotlin/polski/ui/MatrixWeb.kt` | Выбор `web`, текущий `matrixSection` и `matrixTable`, прежний порядок колонок/строк. |
| `kotlin/composeApp/src/desktopMain/kotlin/polski/ui/screens/MatrixScreen.kt` | Выбор `desktop`, текущий `MatrixCard`/`MatrixTable`. |
| `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidMatrixScreen.kt` | `title.compact` и `mobileLine` в прежних четырёх `Text`. |
| `kotlin/shared/src/iosMain/kotlin/polski/ios/IosSnapshot.kt` → `kotlin/iosApp/PolskiGrammar/PolskiGrammarApp.swift` | Добавить только `supportTitle` и массив `supportLines` к существующему `matrix` snapshot; SwiftUI `Section` выводит эти четыре строки. Не экспортировать весь course pack через Swift bridge. |
| `courses/pl-ru/source-inventory.json` | Перегенерировать `node scripts/inventory-course.mjs` после удаления literals из hosts; не редактировать generated inventory вручную. |

Зависимость остаётся однонаправленной: immutable pack → React/Kotlin adapters → host renderers, для iOS — через snapshot. Нет новых действий, асинхронной загрузки, mutable state или ресурсов; грамматика, расписание и progress JSON не меняются. Продуктовый title/rows принадлежат пакету, способ layout/semantic table принадлежит хосту.

## Проверка и приёмка

Адресные tests разработчика: **NEW** `tests/course-reference-russian-support.test.ts` на четыре exact rows/варианта и React adapter; отрицательные cases в `tests/course-validation.test.ts` на отсутствие строки/варианта, дубли или перестановку ID, пустой текст, неверный `mobileLine` cue; **NEW** `kotlin/shared/src/commonTest/kotlin/polski/data/CourseRussianSupportTest.kt` на точное чтение всех вариантов и порядок; расширить существующий `kotlin/shared/src/iosTest/kotlin/polski/ios/IosMatrixSnapshotTest.kt` на четыре `supportLines`. В существующем browser lane `tests/browser/kotlin-parity-matrix.spec.ts` сравнить четыре строки таблиц React/Kotlin web с соответствующими variant, включая title/headers, и проверить, что матрица открывается как прежде. Для Desktop/Android/SwiftUI — доступные текущие host checks/снимки с проверкой каждой строки; simulator не приравнивать к device.

Developer сначала фиксирует exact baseline, затем переносит данные и проводит адресные RED/GREEN там, где поведение проверяемо; Tester независимо сверяет текст и доступность таблицы/мобильных строк, Reviewer проверяет конечный diff. Запускать `npm run course:validate`, `npm run course:inventory:check`, затронутый `npm test`/typecheck/build и фактические Kotlin/host tasks, указывая PASS/FAIL/NOT RUN по средам в `Plans/Kotlin/Evidence.md`. Отсутствующий native запуск не заменять common test. Срез принят, когда у каждого хоста совпали текущие title, headers, четыре cue/construction/check или mobile lines, порядок и семантическая таблица; `reference.pipeline`/`systemCards`, упражнения, FSRS и прогресс по pinned fixtures не изменились.
