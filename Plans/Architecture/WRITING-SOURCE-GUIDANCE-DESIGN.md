# P13: официальные критерии письма для 16 экзаменов

2026-11-13. Коррекция source fidelity для всех опубликованных сдач. Архитектор прочёл DSL, mapper, потребителей и официальные PDF через PDFKit; тесты не запускал. Письмо остаётся ручной оценкой: этот план добавляет видимый текст критериев, а не автоматический scorer, verdict или раннее раскрытие ответа.

## Факт и область

В `dsl/exam-published.core-data` опубликованы 16 сдач и 91 WritingTask. Официальный arkusz каждой сдачи печатает критерии письма. В исходных `writing.core-data` критерии отсутствуют у всех 11 B1/B2 и трёх C1 сдач (14/16). У C1/2023-04 и C1/2023-11 они встроены в `instruction` каждой из пяти задач, то есть факт повторён десять раз. В PDF B1/2017 рубрика озаглавлена `ZASADY OCENIANIA ZADAŃ`, поэтому поиск только `KRYTERIA OCENY` дал бы ложноотрицательный результат. Позднейшая рубрика устной части в тех же PDF не относится к письму.

| Сдача | PDF arkusz, страница | Критерии письма по официальному PDF |
|---|---:|---|
| B1/2017 | 21 | Wykonanie zadania 0–10; Bogactwo językowe 0–10; Poprawność językowa 0–10; всего 30 |
| B1/2019, B1/2020 | 21 | Wykonanie zadania 0–10; Środki językowe 0–10; Poprawność językowa 0–10; всего 30 |
| B2/2017 | 21 | Wykonanie zadania (treść, długość, forma, kompozycja) 0–10; Bogactwo językowe 0–16; Poprawność językowa 0–14; всего 40 |
| B2/2019, B2/2020 | 25 | Wykonanie zadania 0–10; Środki językowe 0–16; Poprawność językowa 0–14; всего 40 |
| B2/2022-02 | 24 | Wykonanie zadania (treść, forma, objętość) 0–10; Środki językowe 0–16; Poprawność językowa 0–14; всего 40 |
| B2/2023-02, B2/2023-06 | 25 | Wykonanie zadania 0–10; Środki językowe 0–16; Poprawność językowa 0–14; всего 40; сверить формулировку каждой сдачи |
| B2/2024-02 | 24 | Те же диапазоны 10/16/14 и итог 40; сохранить текст своей сдачи |
| B2/2024-06 | 22 | Те же диапазоны 10/16/14 и итог 40; сохранить текст своей сдачи |
| C1/2022-03, C1/2022-11, C1/2023-04, C1/2023-11 | 27 | Wykonanie zadania (treść, forma, objętość) 0–10; Środki językowe 0–18; Poprawność językowa 0–12; всего 40 |
| C1/2024-04 | 26 | Те же диапазоны 10/18/12 и итог 40 |

Это критерии всего выбранного набора заданий, не отдельный максимум за каждый prompt. Сверка PDF проведена read-only; перед переносом текста в DSL автор данных фиксирует официальный PDF и страницу каждой сдачи в source receipt. Для B1/B2 не использовать C1 веса или универсальный шаблон: даже одинаковые числовые диапазоны встречаются с разной официальной формулировкой.

## Публичный контракт и зависимость

В `dsl/exam.data-contract:ExamModuleRow` добавить **optional** `writingGuidance: string`, допустимое только при `module: writing`. Одна строка `module: writing` в каждом из 16 `exams/pl/certyfikat-polski/{level}/{session}/exam-modules.core-data` владеет текстом критериев **своей** сдачи. Остальные module rows не получают поле. Не менять `maximumQuarter` (B1 120, B2/C1 160), IDs, sets, limits, genre, images, prompts, маршруты и source provenance. Рубрика остаётся display copy; числа в ней не парсятся в оценщик и не становятся новым scoring policy.

`dsl/exam-published/pipeline.mjs:validateSources` проверяет авторский инвариант: `writingGuidance` непуст после trim, если задан; встречается только на writing cover; у этой сдачи нет дублирующего inline блока критериев в task instructions. Ошибка указывает source file/row через существующий `sourceFieldError`; для отсутствующего поля — строку writing cover. Проверка наличия критериев во **всех нынешних 16** относится к отдельному source/data тесту по явной публикационной инвентаризации, а не к хардкоду списка сдач внутри production pipeline: будущий официальный PDF может иметь другой формат. `aggregate` уже копирует module rows и provenance; новых join keys или cross-paper links не нужно.

`kotlin/shared/src/commonMain/kotlin/polski/exam/ExamWritingSourceData.kt:officialWritingData` уже соединяет `WRITING_TASK_ROW_ROWS` с writing cover и создаёт `WritingTask.instruction`. Здесь механически построить effective instruction из исходной инструкции и `cover.writingGuidance`, отделив критерии заголовком и переносом строк. Если поле отсутствует, результат **byte-identical** прежней инструкции. Если есть, один rubric block виден в каждой относящейся к сдаче задаче; данные всё равно авторизованы только однажды на cover. У C1/2023-04 и C1/2023-11 удалить **только** встроенный фрагмент `KRYTERIA OCENY … PISANIE` из десяти `WritingTaskRow.instruction`; общую инструкцию выбора набора и все prompt поля сохранить дословно. `PISANIE` как заголовок следующего раздела PDF не включать в guidance.

Web `ExamWritingWeb.kt:renderTask` и SwiftUI `NativeExamView.swift` уже отображают `WritingTask.instruction`; `IosExamSession.kt:writingJson` передаёт его. Следовательно, новый host store, UI-блок или platform scoring branch не нужны. Текущие потребители получают эффективный текст и в отдельной, и в timed Writing части. Android/замороженный JVM Desktop не получают новых экранов или controls; если их существующие Writing consumers используют общий task, они видят тот же исправленный source text. Generated `kotlin/shared/src/commonMain/generated/` обновляется только `biome generate`, руками не правится. Optional field сохраняет старые rows/import и прогресс.

## Приёмка и handoff

1. **Source RED → GREEN:** независимый source тест сначала воспроизводит пропуск у 14 сдач и повторение у двух. После исправления все 16 covers имеют ровно один сверенный guidance fact; 91 effective task instructions отображают один rubric block с категорией, диапазонами и итогом **своего** PDF. Отдельно проверить B1/2017 `Bogactwo` и 30, B2/2017 `Bogactwo` и 40, новые B2 `Środki` 10/16/14, C1 `Środki` 10/18/12. Не выводить expected строки из generated rows.
2. **Сохранение контракта:** targeted commonTest и `ExamPublishedModulesTest` сравнивают до/после `promptText`, `wordLimit`, `setId`, genre, порядок 91 задач, 120/160 quarter maxima и manual pending. У optional-missing field instruction остаётся byte-identical. Отрицательные data cases: guidance на listening, пустая строка, inline дубль task+cover дают source diagnostic. Исходные формулировки выбора наборов I/II/III сохранены.
3. **UI:** реальный browser PlayTest открывает B1/2017, B2/2017, B2/2024-06, C1/2022-11 и C1/2024-04 в выбранных наборах I/III и проверяет видимый критерий, word limit, prompt и manual pending после submit; остальные сдачи покрываются model/source contract тестом. Проверить действительный web Wasm/JS artifact, не подменять его jsdom. Apple bridge snapshot/Simulator проверяет хотя бы B1, B2 и C1 в отдельном и timed writing; Kotlin compilation не выдавать за запуск устройства. Android/Desktop — лишь затронутый существующий consumer, без новой функции.
4. **Генерация и review:** `biome fmt` изменённых `.core-data`, `biome generate --project dsl/biome.project`, `biome generate --check --project dsl/biome.project`; затем relevant Kotlin compilation/tests, browser и native acceptance с отдельными PASS/FAIL/NOT RUN. Developer владеет contract/source/mapper и TDD, Tester независимо сверяет страницы PDF и реальные UI, Reviewer читает diff и evidence; основной агент обновляет P13 status. Исторический аудит не переписывать задним числом. Архитектор ничего из перечисленных проверок не запускал.

## Принятое дополнение: читаемость на web и нативном хосте

После source/model переноса разработчик открыл настоящий Wasm web на 1440×900 и 375×667. У C1/2022-11 вся инструкция с рубрикой в `h3` занимала 357 px на узком экране; prompt начинался на y=647, поле ввода — на y=910. В светлой теме prompt, Grammar context и Reading passage имели тёмный фон `#1a1f2a` из fallback несуществующего `--surface-container` и тёмный текст; измеренный контраст Writing prompt и Grammar context был около **1,12:1**. Скриншоты и RED Playwright лежат в `../../../polski-grammar-srs-worktrees/recovery-logs/20261112/exam-learning-quality/p13/writing-guidance/`.

Владелец согласовал узкую правку представления без изменения `WritingTask.instruction`, scoring или состояния. Web показывает короткий заголовок задания и нативный HTML `details` / `summary` с локализованной через authored pack `UiCopy` подписью «Инструкция и критерии» / “Instructions and criteria”. Внутри раскрытия тот же текст делится на обычные абзацы по уже добавленному mapper-разделителю `\n\n`, без разбора польских заголовков и чисел; в review весь текст виден обычным шрифтом. Раскрытие закрыто по умолчанию, поэтому prompt и ввод доступны сразу; проверка содержимого в браузере открывает его через клик или клавиатуру, а не считает скрытый DOM видимым. SwiftUI показывает длинную инструкцию письма шрифтом `.body`. CSS использует реальные `--surface-raised` и `--text` для Writing prompt, Grammar context, Reading passage и audio player в обеих темах. Нативный продуктовый запуск и независимая приёмка остаются отдельными gates.
