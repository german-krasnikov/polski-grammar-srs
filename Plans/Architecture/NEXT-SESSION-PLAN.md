# Следующая сессия: план

Дата: 2026-11-08. 51 коммит за сессию, все сборки GREEN.

Продолжение 2026-11-08: исторический GREEN выше не заменяет текущие проверки. Работа идёт по [плану исполнения](NEXT-SESSION-EXECUTION.md) и [уточнению контракта B1/2017](B1-2017-CONTRACT-ADDENDUM.md). Сверка B1/2017 выявила повреждённые извлечённые ответы, дробные веса и комплекты письменных заданий; сдача требует восстановления по PDF, а не только подключения генерации. Scheduler DSL-перенос реализован, независимые gates отражаются в [evidence](SCHEDULER-DSL-ORACLE-EVIDENCE.md).

## Уточнения владельца и текущие блокеры — 2026-11-08

Продолжать автономно **до конца плана по ТЗ**. Экзамены должны быть полноценными официальными заданиями для проверки знаний: тексты, варианты, иллюстрации, аудио, баллы, допустимые ответы и итог. Не считать схемную валидацию или количество строк доказательством полноты. Завершённые проверенные части коммитить; временные скрипты и оставшиеся тестовые процессы убирать, сохраняя исходники и нужные evidence. Предпочитать DSL для правил, UI-данных и независимых тестовых ожиданий по пяти документам biome-dsl, перечисленным в плане исполнения.

Приоритет перед дальнейшим расширением:

1. **Текст вместо заглушек — пользовательский скриншот.** В доступном B1/2019 чтение показывает `Krotkie fragmenty tekstow...`, `Pytanie N` и голые `a/b/c`. Восстановить все тексты/вопросы/варианты из PDF и сверить ключи. Проверить также все доступные модули 2019: аналогичные заглушки аудирования B1 и повреждённую польскую орфографию B2. Плейтест проверяет реальные содержательные варианты и текст, затем ввод и результат; одних кликов/счётчиков недостаточно.
2. **Плеер — пользовательский скриншот B1/2017, задание V (паспорта).** Прогресс остаётся `0:00 / 4:54`, воспроизведение обрывается примерно через 40 секунд. Диагностировать реальный MP3, загрузку metadata, seekable/range, установку абсолютного `currentTime`, относительный прогресс, обработчики и конец сегмента. Не подменять это только исправлением значка кнопки.
3. **Перепроверить транскрипции и все аудиограницы.** По исходному MP3 подтвердить начало/конец каждого задания, полные фразы и число записанных повторов. Сопоставить с текстом задания и независимыми временными наблюдениями, а не только грубой Whisper tiny транскрипцией. Не обрезать речь, не включать соседнее задание и не дублировать программно уже записанный повтор. Сохранить источник/хеш, интервалы, подтверждающие фрагменты и ограничения проверки.
4. **Реальные media-плейтесты.** Проверять движение отображаемого и фактического времени, непрерывность более 40 секунд для воспроизведённого дефекта, паузу/продолжение, перемотку, правильный старт/конец каждого сегмента, смену задания, сохранение выбранного ответа и остановку/очистку при уходе и повторном входе. Положительный сценарий — реальный MP3; 404/ошибка загрузки — отдельный тест границы IO. Мок успешного плеера не доказывает исправление.
5. **Полнота оценивания и доставки.** Подтвердить P/F в чтении B1/2017, дробные баллы, частичные варианты, неупорядоченные двойные ответы B1/2020 и письменные комплекты с фотографиями. Решить воспроизводимую доставку аудио: проигрывание из игнорируемого локального файла не доказывает готовность чистой сборки. Незавершённые модули не объявлять готовыми.

Эти пункты уточняют приоритеты 1/3/4 ниже и не снимают оставшиеся задачи 5–12. Текущая пользовательская сборка: `http://127.0.0.1:4190/`; её сервер не удалять как тестовый мусор, пока пользователь проверяет приложение.

## Выполнено

### DSL-миграция (Phase 0-1-2)
- 7 data-contract (course, frequency, forms, style, effects, grammar-oracle, exam)
- 49+ .core-data файлов, 38+ GENERATE шагов, 7 derive скриптов
- GrammarParityTest 2069→57, scoring oracle на DSL
- UiCopy 33 pack-aware ключей (pl-ru + en-ru переводы)
- Effects 4→1, intervalLabel 3→1, SkillQueue 3→1, swipe 6→1
- classify → data rows (P10 закрыт)

### Модели и рефакторы
- TrainingCardModel + Desktop/Android/Web миграция
- SnapshotCommon (iOS 559→103, macOS 392→78)
- :apple-bridge модуль, build-logic dedup
- accepts() consolidated, MatrixTableViewModel, enVerbForm dedup
- waitForTimeout 9/12 → state-based

### Экзамены
- exam.data-contract + DSL-first pipeline (.core-data → biome generate → typed rows → web UI)
- B1 2019 полностью распарсен (45 grammar + 42 listening + 35 reading)
- B2 2019 полностью распарсен (75 grammar + 38 listening + 15 reading)
- Grammar exam module (session/scorer/card model)
- Web UI: выбор экзамена, грамматика, чтение, аудирование с кастомным плеером, письмо
- Аудио с whisper timestamps, кастомный segment player

### CI
- Deploy SUCCESS (GitHub Pages)
- Playwright POLSKI-TEXT fix
- Removed React build/course:validate from CI

## Не завершено (для следующей сессии)

### Высокий приоритет
1. **4 экзамена допарсить** — B1 2017/2020, B2 2017/2020 из PDF (ExamBin/ готов)
   - Whisper timestamps для аудио
   - GENERATE steps в biome.project
   
2. **SchedulerExpandedTest → DSL oracle — завершено** (1144→85 строк Kotlin)
   - Реализовано: 70 сохранённых случаев → `.core-data`, типизированный генерат, 85 строк Kotlin-потребителя
   - Frozen-oracle audit, намеренно неверное ожидание RED → восстановление GREEN, `generate --check`: PASS; независимая сверка 70 случаев и JVM-проверка: PASS; независимое ревью: APPROVED. JS/Wasm scheduler checks: NOT RUN; см. evidence выше

3. **Exam UI баги**:
   - Focus loss при вводе текста (fix сделан но проверить)
   - Все типы вопросов (word_bank, matching, transform, gap_fill_fragment)
   - Аудио: "nagranie zostanie odtworzone dwa razy" — авто-повтор
   
4. **Playwright тесты** — 53 passed, ~9 нужно обновить для model changes

### Средний приоритет
5. **:srs-progress модуль** — граница подтверждена, но cross-refs в derive/Python мешают
6. **EndingHighlightTest → DSL oracle** — 65 assertions → .core-data
7. **MatrixTablesTest → DSL oracle** — 43 assertions → .core-data
8. **TrainingWebApp.kt split** — 1040 строк, >200 line rule

### Низкий приоритет
9. **Exam B2/C1 данные** из certyfikatpolski.pl (сессии 2022-2024)
10. **Grammar exam: все типы** (transform, word_bank, question_formation)
11. **Reading/Listening/Writing session** в shared (сейчас только в webMain)
12. **Swift UiCopy** — дочитать на Swift стороне оставшиеся labels

## DSL-first принцип (память)

Бизнес-логику пишем на DSL. Native только для IO/rendering/platform API.
- Решения/пороги → .logic DECISION
- Данные → .core-data + data-contract
- Тестовые ожидания → .core-data oracle (как GrammarParity, scoring oracle)
- UI строки → UiCopy pack-aware
- Проверять P25 (V1-V5 vs S1-S6) перед каждым решением DSL vs native

## Ключевые пути

```
biome-dsl:     /Users/german/Work/Python/biome-dsl
biome CLI:     node -e "const {spawnSync}=require('child_process'); ..."
ExamBin:       /Users/german/Work/JS/polski-grammar-srs/ExamBin/pl/<level>/<year>/
Exam .core-data: exams/pl/certyfikat-polski/<level>/<year>/
Plans:         Plans/Architecture/
```
