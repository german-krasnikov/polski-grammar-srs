# Polski S1: независимые ожидания владельца для 20 отложенных примеров

Подготовлено до S1 policy transfer и до запуска native `COMMAND`/`WIRE` baseline. Источник решения — [ADR-49](../../AI/decisions.md), который закрепляет JS-семантику Unicode-пробелов/BOM, обрезку четырёх полей словаря до проверки 240 UTF-16 code units и записи, а также прежние приоритет `typedCorrect` и проверку дубликатов. [План Biome, Polski S0](../../../../Python/biome-dsl/AI/DSL/AFTER_READINESS_PLAN.md) требует выбрать ожидания независимо от вывода backend. Это **ожидания**, не результат исполнения.

В worktree `biome/after-readiness-B` каталог `parity/examples` отсутствует. Ниже идентификаторы и исходные входы взяты из чистого соседнего продуктового worktree `../polski-grammar-srs-biome` на `22f72de0326114ccf32b258aec2f8a9dbf96a616`: [answer-grading.json](../../../polski-grammar-srs-biome/parity/examples/answer-grading.json), [vocabulary.json](../../../polski-grammar-srs-biome/parity/examples/vocabulary.json), [review-transition.json](../../../polski-grammar-srs-biome/parity/examples/review-transition.json). Их `options` служат указателем на альтернативы, а основание выбора — ADR-49, продуктовый контракт и сохранение ранее принятого поведения. При переносе в новый формат входы/ожидания должны совпасть с перечисленными ниже; формат и раннер здесь не создаются.

Обозначения в таблицах: `␠` = U+0020; `NBSP` = U+00A0; `THIN` = U+2009; `NNBSP` = U+202F; `IDEOGRAPHIC` = U+3000; `BOM` = U+FEFF. Обозначения описывают входы, а не буквальные символы, которые надо вводить в файл примеров. `normalized` — точная строка после нормализации; `distance` — расстояние Левенштейна в UTF-16 code units.

## 12 answer-grading

Все строки ниже имеют `correct: true`, `distance: 0` и соответствуют варианту `A-unicode-whitespace`. Ожидаемая фраза для `AG-U*` — `widzę żonę`; для `AG-W*` — `Widzę moją piękną żonę.`. `AG-W*` помечены `ui: true` в исходном корпусе; это не означает, что UI уже проверен.

| ID | Вход `answer` | Точный `normalized` |
|---|---|---|
| AG-U01 | `widzę` + NBSP + `żonę` | `widzę żonę` |
| AG-U02 | NBSP + `widzę żonę` + NBSP | `widzę żonę` |
| AG-U03 | `widzę` + THIN + `żonę` | `widzę żonę` |
| AG-U04 | `widzę` + NNBSP + `żonę` | `widzę żonę` |
| AG-U05 | `widzę` + IDEOGRAPHIC + `żonę` | `widzę żonę` |
| AG-U06 | IDEOGRAPHIC + `widzę żonę` + IDEOGRAPHIC | `widzę żonę` |
| AG-U07 | BOM + `widzę żonę` | `widzę żonę` |
| AG-U08 | `widzę żonę` + BOM | `widzę żonę` |
| AG-U09 | `widzę żonę.` + NBSP | `widzę żonę` |
| AG-W05 | `Widzę` + NBSP + `moją piękną żonę.` | `widzę moją piękną żonę` |
| AG-W06 | NBSP + `Widzę moją piękną żonę.` + NBSP | `widzę moją piękną żonę` |
| AG-W07 | BOM + `Widzę moją piękną żonę.` | `widzę moją piękną żonę` |

`AG-U09` особо проверяет порядок: крайний NBSP удаляется, затем конечная точка удаляется. Поля `expected` упражнения остаются исходными; регистр и польские диакритические знаки в `normalized` указаны точно.

## 7 vocabulary

Для `VI-P01`–`VI-P05` вход — документ v1 `pl-ru`, пустые `selectedIds`/`cards`, одна пользовательская запись с ID `user.00000000-0000-4000-8000-000000000001`, уровнем `A1`, `frequencyRank: null`, `custom: true`. Если ниже не указано иное, её сохранённые поля равны `lemma: szkoła`, `translation: школа`, `form: szkoła · род. szkoły`, `example: To jest moja szkoła.`. Во всех пяти случаях точный результат: `accepted: true`, один `custom` с указанными четырьмя обрезанными значениями, тем же ID и `level: A1`, `selectedIds: []`, `cardKeys: []`. Это выбор `A-trim-then-validate`, включая проверку длины **после** обрезки.

| ID | Отличающийся сырой вход | Точное сохранённое значение |
|---|---|---|
| VI-P01 | `lemma: "  szkoła "` | `lemma: "szkoła"` |
| VI-P02 | `lemma: TAB + "szkoła"`; `translation: "школа" + NBSP`; `form: " szkoła · род. szkoły "`; `example: "To jest moja szkoła. "` | `lemma: "szkoła"`; `translation: "школа"`; `form: "szkoła · род. szkoły"`; `example: "To jest moja szkoła."` |
| VI-P03 | `lemma: NBSP + BOM + "szkoła"` | `lemma: "szkoła"` |
| VI-P04 | `lemma: "a" × 240 + ␠` (241 сырая UTF-16 единица) | `lemma: "a" × 240` (240 единиц) |
| VI-P05 | `lemma: ␠ × 10 + "a" × 235 + ␠ × 10` (255 сырых UTF-16 единиц) | `lemma: "a" × 235` (235 единиц) |

`VM-P01` и `VM-P02` используют заполненный локальный профиль с выбранной своей записью ID `...0001`, `lemma: szkoła`, и импорт с новой ID `...0002`; оба профиля имеют пустые `cards`. Различается только импортированная лемма: `SZKOŁA` для `VM-P01`, `"  szkoła "` для `VM-P02`. Точный исход обоих — `accepted: false` (вариант `A-ignore-case-and-padding`); локальные `custom`, `selectedIds` и история остаются прежними, запись/merge не происходит. Для `VM-P02` импортированное поле обрезается до сравнения; для `VM-P01` сохраняется уже действующее сравнение лемм без различия регистра. Дубликат с **тем же ID** и допуск старого custom homonym в **пустом** профиле не меняются.

## 1 review-transition

`RT-05`: `now: 2026-02-03T12:00:00.000Z`, `localDay: 2026-02-03`, карточки `a` (`reps: 0`) и `b` (`reps: 3`), `review: { skillId: a, rating: again, typedCorrect: true }`. До перехода: `stats.a = { reviews: 4, correct: 3, mistakes: 1, streak: 2 }`, `stats.b = { reviews: 1, correct: 1, mistakes: 0, streak: 1 }`, `reviewsToday: 5`, `totalReviews: 20`, `lastDay: 2026-02-03`.

Точный результат `A-typed-wins`: `rejected: false`; `stats.a = { reviews: 5, correct: 4, mistakes: 1, streak: 3 }`; `stats.b = { reviews: 1, correct: 1, mistakes: 0, streak: 1 }`; `reviewsToday: 6`; `lastDay: 2026-02-03`; `totalReviews: 21`; `reps: { a: 1, b: 3 }`. `typedCorrect: true` определяет статистику, несмотря на `rating: again`; планирование карточки по рейтингу и настройки FSRS не переопределяются этим ожидаемым срезом.

## Граница с уже существующим покрытием

Это **20 новых разрешённых ожиданий**: 12 answer + 7 vocabulary + 1 review. Они не заменяют ранее имевшиеся 54 принятых TS/Kotlin примера и UI-примеры. В частности, `AG-W01`–`AG-W04` уже фиксируют обычный ответ, регистр/точку и диакритику; `VI-12`/`VI-13` отклоняют 241-символьные **после обрезки** значения; `VM-01`–`VM-08` покрывают восстановление, конфликт ID/лемм и FSRS merge; `RT-01`–`RT-04`/`RT-06`–`RT-09` покрывают обычные рейтинги, счётчики, локальный день и неизвестный skill. Их ожидания не следует подгонять под новый backend. Числа 54 и 20 — инвентаризация исторического корпуса, а не текущая зелёная квитанция.

Проверки в этом FS-only handoff: native `COMMAND`/`WIRE` baseline **NOT RUN**; S1/S2 transfer, DSL compilation, `biome check changed`, TS/Kotlin tests, Gradle, browser/device и полный LSP gate **NOT RUN**. Для будущей приёмки нужно убедиться, что все 20 реально исполнились на каждом заявленном target, отдельно от компиляции и существующих примеров; пропуск или только связанный тест не считается доказательством.
