# Срез course pack: падежная подсказка и сравнение существительных

## Границы и текущий baseline

Это партия 1 из [CoursePackRemainingRoadmap.md](CoursePackRemainingRoadmap.md) для этапов 1–2 [CoursePacksPlan.md](CoursePacksPlan.md). Переносим фиксированный учебный текст о Wołacz и упорядоченный выбор семи существительных для сравнения из хостов в courses/pl-ru/course.json. Формы, предложения и подсветка по-прежнему вычисляются существующими грамматическими функциями. Срез не меняет упражнения, FSRS, сохранённый прогресс, выбор noun/adjective/owner/number, layout или редакторскую формулировку. Работа над reference.russianSupport — отдельный срез; если она ещё идёт, разработчик начинает правку общих adapter/host файлов после его слияния.

Текущие источники: src/ui/GrammarTables.tsx (GrammarTables, секции падежей и сравнения); kotlin/composeApp/src/webMain/kotlin/polski/ui/MatrixWeb.kt (renderCases); kotlin/composeApp/src/desktopMain/kotlin/polski/ui/screens/MatrixScreen.kt (CasesDesktop); kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidMatrixScreen.kt (AndroidCasesSection); kotlin/shared/src/iosMain/kotlin/polski/ios/IosSnapshot.kt (matrixSnapshot); kotlin/iosApp/PolskiGrammar/PolskiGrammarApp.swift (секция cases). Во всех пяти вариантах список ID сейчас повторён литералом; iOS получает его через snapshot. Сами 14 nouns и семь reference.caseRows уже находятся в pack.

| Содержимое | Наблюдаемый host baseline |
| --- | --- |
| Развёрнутая заметка React | «Wołacz показан как форма обращения; с неодушевлёнными словами обычно используется только стилистически. «Zachwycam się…» = «Восхищаюсь…» (Narzędnik), «Przyglądam się…» — Celownik.» |
| Компактная заметка Kotlin Web/Desktop/Android/iOS | «Wołacz — форма обращения. «Zachwycam się…» требует Narzędnik; «Przyglądam się…» — Celownik.» |
| Подсказка над сравнением React/Kotlin Web | «Читай по строке, чтобы сравнить типы. По столбцу — чтобы увидеть все формы одного слова.» |
| Порядок сравнения всех хостов | husband, friendM, dog, house, wife, book, child |

React и Kotlin Web показывают таблицу семь падежей × семь существительных отдельно для sg/pl, с контрастом относительно Nominative и горизонтальной прокруткой. Desktop показывает таблицу 7×7 без подсказки чтения. Android/iOS показывают семь выбранных слов только для выбранного падежа; не заменять это таблицей. Заголовки «Сравнение типов склонения» (с динамическим номером только в браузерах), «Падеж для сравнения» и «Выбери группу слов» остаются в UI как текущие подписи управления/контейнера. React case table, Kotlin Web/Desktop case table и mobile cards также сохраняют нынешние разные колонки и компоновку. Падежная заметка отличается осознанно; общий текст вместо двух вариантов изменил бы baseline.

## Контракт данных и зависимости

Добавить обязательный reference.caseTeaching в courses/pl-ru/course.json: caseNote.react и caseNote.compact с точными строками выше, comparisonReadingHint с точной строкой выше. Добавить обязательный reference.comparisonNounIds как упорядоченный массив ровно семи ссылок на существующие nouns, в порядке выше. Это immutable данные курса без HTML, шаблонов, нового локального состояния или async API. Не добавлять таблицу падежных форм: nounById(...).forms, nounPhrase и caseSentence остаются единственным источником вычисленных форм. Русский текст сравнительного заголовка и подписи элементов управления можно рассмотреть в общем UI-localization проходе; этот срез не должен менять их.

| Файл / символ | Изменение для разработчика |
| --- | --- |
| courses/schema/course-pack-v1.schema.json; scripts/validate-course.mjs | Расширить строгий reference: nonblank/noHtml текстовые поля, массив из ровно семи непустых уникальных ID, каждый ID существует в nouns. Для pl-ru проверять утверждённый порядок в pinned test; semantic validator проверяет ссылки/структуру, не запрещая будущую осознанную замену лексемы. Диагностика указывает /reference/caseTeaching или /reference/comparisonNounIds/N. |
| src/data/course.ts; kotlin/shared/src/commonMain/kotlin/polski/data/CourseData.kt | Узкие read-only exports для caseTeaching и comparisonNounIds. В Kotlin допустимы typed value classes/data class; идентификаторы как строки проверяются при загрузке pack и разрешаются через существующий nounById. Не объявлять фиктивный compile-time union из JSON. |
| GrammarTables.tsx, MatrixWeb.kt, MatrixScreen.kt, AndroidMatrixScreen.kt | Читать нужный вариант заметки и ordered IDs из adapter. В React убрать оба одинаковых литеральных массива (header и cells), чтобы порядок не расходился. Сохранить текущие FormsContrast, caseRows, number, прокрутку и seed действия case.gen.neg. |
| IosSnapshot.kt → PolskiGrammarApp.swift | Snapshot строит comparison из pack IDs в прежней структуре rows/case values и добавляет только caseNote.compact как scalar для SwiftUI. SwiftUI берёт эту строку, не загружает JSON отдельно. |
| courses/pl-ru/source-inventory.json | Перегенерировать существующим node scripts/inventory-course.mjs после удаления литералов; generated файл не редактировать вручную. |

Зависимость: pack → TypeScript/Kotlin shared adapters → React/Kotlin hosts → iOS snapshot/SwiftUI. Существующий selected case/number остаётся состоянием host; текст и порядок ID — значениями pack. Не расширять публичные actions или формат progress.

## Приёмка и проверка

Разработчик добавляет точные fixtures для двух заметок, reading hint и семи ID в существующую course validation suite или отдельный tests/course-reference-cases.test.ts; отрицательные validator cases: отсутствующий/пустой текст, неверный тип, duplicate/unknown/пропущенный noun ID. Kotlin commonTest проверяет typed чтение, порядок и разрешение каждого ID в noun; iOS snapshot test проверяет порядок семи rows и доступность семи форм по GramCase, а также compact note. P08 browser parity: React и Kotlin Web показывают прежние строки и 7×7 формы в sg и pl; horizontal overflow и Nominative contrast сохраняются, case.gen.neg получает прежний выбранный noun/adjective. Для Desktop проверить 7×7 и compact note; для Android/iOS — семь ordered noun rows при выборе каждого падежа и compact note. Отсутствующий запуск устройства записывать NOT RUN, не объявлять PASS по common/browser tests.

Запускать реальные course:validate, course:inventory:check, адресные npm/Gradle checks и доступные browser/host acceptance из проекта; evidence с PASS/FAIL/NOT RUN фиксируется в Plans/Kotlin/Evidence.md. Срез готов, когда текст, порядок и вычисленные формы совпадают с зафиксированным baseline каждого хоста, а другие reference groups, упражнения и progress fixtures не изменились. Независимые Tester и Reviewer проверяют итоговый diff и фактические evidence после разработки.
