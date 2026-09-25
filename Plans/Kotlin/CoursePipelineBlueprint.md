# Срез course pack: схема «Сначала конструкция, затем формы»

## Scope и потребители

Следующий малый срез этапов 1–2 [CoursePacksPlan.md](CoursePacksPlan.md) после `reference.systemCards`: перенести только заголовок и три шага фиксированной схемы в `courses/pl-ru/course.json`. Это авторский учебный материал, а не состояние UI или алгоритм склонения. React показывает три отдельных блока; Kotlin web, Desktop, Android и SwiftUI показывают две компактные строки. Вынесение сохраняет текущий текст, порядок, разметку, доступность и поведение кнопок. Таблица «Опора на русский», остальные заголовки и учебные тексты матрицы остаются следующими самостоятельными срезами.

Текущие потребители: `src/ui/GrammarTables.tsx` (`GrammarTables`, `.rule-pipeline`); `kotlin/composeApp/src/webMain/kotlin/polski/ui/MatrixWeb.kt` (`renderMap`); `kotlin/composeApp/src/desktopMain/kotlin/polski/ui/screens/MatrixScreen.kt` (`MapDesktop`); `kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidMatrixScreen.kt` (`AndroidMapSection`); `kotlin/shared/src/iosMain/kotlin/polski/ios/IosSnapshot.kt` (`matrixSnapshot`) → `kotlin/iosApp/PolskiGrammar/PolskiGrammarApp.swift` (`map`). Уже существующий источник данных и адаптеры: `courses/pl-ru/course.json`, `src/data/course.ts`, `kotlin/shared/src/commonMain/kotlin/polski/data/CourseData.kt`.

## Публичный контракт

Добавить обязательный `reference.pipeline` в существующий schemaVersion 1 pack:

```json
{
  "title": "Сначала конструкция, затем формы",
  "steps": [
    { "id": "intent", "label": "01 · Смысл", "question": "Что хочу сказать?", "example": "Вижу / не вижу / говорю о…" },
    { "id": "case", "label": "02 · Операция", "question": "Какой падеж нужен?", "example": "widzę → Biernik" },
    { "id": "agreement", "label": "03 · Согласование", "question": "Меняю всю группу", "example": "moją + piękną + żonę" }
  ],
  "compactExample": "widzę → Biernik → moją + piękną + żonę"
}
```

`compactSummary` вычисляется из `steps[*].question`, соединённых буквальной строкой ` → `; результат — **точно** `Что хочу сказать? → Какой падеж нужен? → Меняю всю группу`. `compactExample` задан явно, поскольку React показывает иной пример первого шага и его нельзя включать в компактную строку. Это единственная осознанная повторяемость в пакете; она сохраняет авторскую форму без неявного правила «брать шаги 2–3». Числа, порядок и `id` шагов являются контрактом; `label`, `question`, `example`, `title`, `compactExample` — непустые видимые строки. Никакого HTML/исполняемого шаблона. Пакет при чтении не редактируется; ошибки авторского JSON останавливают validation/build с путём поля.

React adapter экспортирует `courseReferencePipeline` из `src/data/course.ts`; UI отрисовывает `title` и `steps` с теми же `small`, `strong`, `span` и стрелками между блоками. Kotlin `CourseData.kt` добавляет узкие `ReferencePipelineStep`/`ReferencePipeline` и `referencePipeline`: immutable прочитанные значения без Compose/DOM. `compactSummary` может быть read-only derived property Kotlin-модели и маленькой функцией TS-адаптера либо локальной презентационной операцией `join`; обе реализации проверяются одной exact fixture. Kotlin web/Desktop/Android оставляют две `p`/`Text` строки с прежним текстом. `IosSnapshot.kt` кладёт `pipelineTitle`, `pipelineSummary`, `pipelineExample` в существующий `matrix` snapshot; SwiftUI продолжает показывать те же три `Text`, читая snapshot вместо исходных литералов. Новый action, storage field, network load или жизненный цикл не нужен.

## Файлы и зависимости

| Путь | Изменение разработчика |
| --- | --- |
| `courses/pl-ru/course.json` | Авторский `reference.pipeline` с точными строками выше. |
| `courses/schema/course-pack-v1.schema.json` | Обязательные поля, `additionalProperties: false`, три шага, стабильные ID и nonblank text. |
| `scripts/validate-course.mjs` | Семантическая проверка порядка `intent → case → agreement`, диагностика `/reference/pipeline/steps/<n>/id`; schema/noHtml уже проверяют структуру и теги. |
| `src/data/course.ts`, `src/ui/GrammarTables.tsx` | Узкий React adapter и прежняя трехблочная DOM-композиция из pack. |
| `kotlin/shared/src/commonMain/kotlin/polski/data/CourseData.kt` | Чтение/валидация и типизированная модель; общая зависимость всех Kotlin-хостов. |
| `kotlin/composeApp/src/{webMain,desktopMain,androidMain}/kotlin/polski/ui/.../Matrix*.kt` | Только подстановка данных в существующую компоновку; фактические пути потребителей перечислены выше. |
| `kotlin/shared/src/iosMain/kotlin/polski/ios/IosSnapshot.kt`, `kotlin/iosApp/PolskiGrammar/PolskiGrammarApp.swift` | Передача компактной проекции и её чтение SwiftUI; нет второй копии текста в Swift. |
| `courses/pl-ru/source-inventory.json` | Перегенерировать существующим `node scripts/inventory-course.mjs` после правок; не редактировать generated строки вручную. |

Направление зависимостей: pack → React/Kotlin data adapters → host rendering; iOS получает строки через существующий snapshot. `reference.systemCards`, упражнения, FSRS, progress и выбор методики не меняются. `compactSummary` — чистая композиция без часов, mutable state или ресурсов.

## Проверка и приёмка

Разработчик добавляет адресные tests: **NEW** `tests/course-reference-pipeline.test.ts` проверяет exact JSON, порядок/компактную строку и отсутствие изменения исходных значений адаптером; `tests/course-validation.test.ts` отклоняет пропущенный/пустой шаг, перестановку/дубли ID и лишнее поле с диагностикой пути; **NEW** `kotlin/shared/src/commonTest/kotlin/polski/data/CourseReferencePipelineTest.kt` сверяет Kotlin-модель/проекцию с теми же строками и порядком; существующий `kotlin/shared/src/iosTest/kotlin/polski/ios/IosMatrixSnapshotTest.kt` проверяет три поля snapshot. В существующих browser tests матрицы достаточно одного осмысленного сценария на React и Kotlin web: React имеет три блока с точными label/question/example; Kotlin web имеет две строки с точным compact summary/example; открытие матрицы и drill из неё остаются рабочими. Тесты не должны просто проверять наличие ключа в JSON.

После реализации выполнить затронутые `npm run course:validate`, `npm run course:inventory:check`, адресный Vitest/TypeScript и реальные Kotlin common/web/Desktop/Android/iOS проверки согласно доступным задачам `kotlin/README.md`; browser и native UI результаты записать отдельно как PASS/FAIL/NOT RUN. Успешный common test не доказывает SwiftUI/Android отображение. Зафиксировать сравнение baseline текста до/после для каждого хоста; обновление golden только после просмотра. Переход допускается, если ни одна видимая строка/порядок не изменилась, React DOM сохраняет три шага, compact hosts показывают прежние две строки, pack отвергает ошибочные IDs и progress fixtures остаются прежними. Tester независимо сверяет эти случаи, Reviewer проверяет итоговый diff и evidence; документацию/evidence ведёт Developer или основной агент.
