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

### Текущий срез — 2026-11-12

Исходный план закрыт **12/12** с учётом явно согласованного владельцем исключения повреждённого B2/2022-06. По новому поручению добавлены семь обязательных пунктов P13–P19: общий текущий статус **16/19** (P13, P16, P17 и P18 закрыты). Исторические результаты ниже сохраняют свои даты и артефакты.

- **P3/P4 завершены:** финальный Chromium exam **92/92 PASS**; полный non-exam прогон дал 166 PASS / 2 SKIP / 1 FAIL, после исправления установки storage-reader в мобильном test fixture затронутый случай **1/1 PASS**. Каталог и 80 аудиоокон проверены в Chromium и Firefox. Ограничение локального WebKit и исходные неуспешные прогоны сохранены. Опубликованный сайт отдельно прошёл Chromium/Firefox/WebKit: 16 официальных вариантов, действительный Wasm, непрерывное аудио >41 секунды, движение времени/прогресса и пауза. Детали — [итоговая публикация](P9-PUBLIC-DEPLOYMENT-EVIDENCE.md).
- **P9 завершён в изменённом владельцем составе:** 10 новых экзаменов опубликованы, всего 16 официальных. После безрезультатного поиска исправного MP3 владелец исключил взрослый B2/2022-06. Удалены только 14 неактивных DSL-файлов этого черновика; исходники, повреждённая запись и хеши сохранены в архиве. Остальные экзамены 2022 года и опубликованные/generated данные не изменились. Независимые Tester/Reviewer PASS/APPROVED, Vitest 21/21, Biome check, typecheck, JS/Wasm word-bank tests PASS; коммит `6e07011`. Это исключение из объёма, а не реализация повреждённого экзамена. [План](P9-B2-2022-06-WITHDRAWAL-PLAN.md), [evidence](P9-B2-2022-06-WITHDRAWAL-EVIDENCE.md).
- **P11 завершён:** общий сеанс и предметные решения используют DSL, четыре раздела подключены к SwiftUI; source review APPROVED, bridge 17/17, 16 MP3 и семь source images проверены в bundle. Независимые installed UI проверки на 16-paper приложении: настоящий resize 834→412→834 pt с польским ответом и доступным Submit, matching, reading fragments, повторяющиеся токены word bank и source alt — **5/5 PASS**. Финальное независимое ревью APPROVED; прежние full four-part timed, крупный шрифт, media/lifecycle и legacy UI evidence сохраняют силу. Естественный 40-минутный deadline наблюдался; прерванный XCTest не считается PASS. Физический iPad отложен владельцем. [Приёмка](P11-WINDOW-AND-CONTROLS-ACCEPTANCE-20261112.md), [предыдущие результаты](IOS-FULL-EXAM-INDEPENDENT-EVIDENCE.md).

Проверенный runtime объединён и отправлен в `main` (`a753640`); Kotlin check и Pages deployment SUCCESS. [Открыть опубликованную версию](https://german-krasnikov.github.io/polski-grammar-srs/). Постоянные рабочие деревья и evidence сохранены в `../polski-grammar-srs-worktrees/`.

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
- B1 2019 подключён (исходный инвентарь: 45 grammar + 42 listening + 35 reading). Прежняя отметка «полностью распарсен» снята при сверке 2026-11-08: чтение/аудирование содержали заглушки, грамматика — повреждённую диакритику, письмо — неполный набор. Восстановление и независимая приёмка продолжаются.
- B2 2019 подключён (исходный инвентарь: 75 grammar + 38 listening + 15 reading). Прежняя отметка «полностью распарсен» снята: найдены повреждённые тексты, требуется повторная сверка всех модулей с PDF/ключом.
- Grammar exam module (session/scorer/card model)
- Web UI: выбор экзамена, грамматика, чтение, аудирование с кастомным плеером, письмо
- Аудио с whisper timestamps, кастомный segment player

### CI
- Deploy SUCCESS (GitHub Pages)
- Playwright POLSKI-TEXT fix
- Removed React build/course:validate from CI

## Не завершено (для следующей сессии)

### Высокий приоритет
1. **4 экзамена допарсить — завершено для шести исходных вариантов**, включая восстановление B1/B2 2019; подключены все четыре раздела. Проверенный срез `341238b`, [приёмка](SIX-EXAM-BROWSER-ACCEPTANCE.md): Chromium exam 38/38 PASS, целевые Firefox/WebKit PASS, источник аудио/иллюстраций и ограничения зафиксированы. Полный timed flow и семантические controls остаются P10/P11.
   
2. **SchedulerExpandedTest → DSL oracle — завершено** (1144→85 строк Kotlin)
   - Реализовано: 70 сохранённых случаев → `.core-data`, типизированный генерат, 85 строк Kotlin-потребителя
   - Frozen-oracle audit, намеренно неверное ожидание RED → восстановление GREEN, `generate --check`: PASS; независимая сверка 70 случаев и JVM-проверка: PASS; независимое ревью: APPROVED. JS/Wasm scheduler checks: NOT RUN; см. evidence выше

3. **Exam UI баги — завершено:** реальные тексты/варианты из источников, Polish input/focus, все используемые controls, источник рубрик, прогресс/seek/replay/очистка аудио и записанные повторы проверены. Финальная приёмка — [публикация](P9-PUBLIC-DEPLOYMENT-EVIDENCE.md); ограничения сверки транскрипций сохраняются в source/media audit.
4. **Playwright тесты — завершено для опубликованного среза:** Chromium exam 92/92, затронутая mobile storage-регрессия 1/1 после исправления fixture; опубликованный сайт Chromium/Firefox/WebKit 3/3 PASS с настоящим MP3 >41 секунды. Предыдущие полные прогоны, пропуски и локальная WebKit intermittency не скрыты; [evidence](P9-PUBLIC-DEPLOYMENT-EVIDENCE.md).

### Средний приоритет

Границы и критерии пунктов 5–8: [план исполнения](NEXT-SESSION-P5-P8-PLAN.md).
5. **:srs-progress модуль — завершено**, коммит `c19e599`: перенос без изменения форматов прогресса/публичных пакетов, обновлены derive/Python и Apple bridge. Независимые Tester/Reviewer: PASS/APPROVED; после объединения генерация, 56 module + 423 shared Desktop tests и JS/Wasm compilation PASS. Ограничения браузерного повторного прогона и native UI — в [evidence](P5-SRS-PROGRESS-EVIDENCE.md).
6. **EndingHighlightTest → DSL oracle — завершено**, `bdbf83f`: прежние 65 assert sites сохранены в 34 DSL cases/93 expected parts и retained property/integration tests. Независимая сверка покрытия и свежий JVM 6/6 PASS, генерация PASS, ревью APPROVED. Kotlin 244→93 строки; полный авторский DSL+test footprint вырос до 1140 строк, см. [evidence](P6-ENDING-HIGHLIGHT-EVIDENCE.md).
7. **MatrixTablesTest → DSL oracle — завершено**: фактические 45 прежних assert sites сохранены через DSL 7 tables/28 headers/47 literal cells и property checks. Независимые T1–T3, JVM 4/4, генерация и ревью PASS/APPROVED; [evidence](P7-MATRIX-TABLES-EVIDENCE.md).
8. **TrainingWebApp.kt split — завершено**: mount/effects/renderer/card разделены по владельцам; отменяются prewarm, slide и navigation callbacks. Независимые Playwright Chromium 20/20 и Firefox 2/2 PASS; расширенные lifecycle cases JS/Wasm PASS, review APPROVED. После измерения холодного fixture 15–17 секунд лимит Mocha исправлен: полный composeApp JS 32/32 и Wasm 32/32 PASS, отдельное ревью APPROVED; [evidence](P8-JS-TEST-HARNESS-EVIDENCE.md).

### Низкий приоритет

Контракты и порядок пунктов 9–12: [план исполнения](NEXT-SESSION-P9-P12-PLAN.md).
9. **Exam B2/C1 данные** из certyfikatpolski.pl (сессии 2022-2024) — [инвентарь официальных источников](EXAM-2022-2024-SOURCE-INVENTORY.md): 11 взрослых вариантов; все 33 PDF/MP3 получены и SHA-256 проверены ([provenance](P9-SOURCE-PROVENANCE.md)); нужны ID с датой сессии, данные и дополнительные правила зависимых ответов C1
10. **Grammar exam: все типы — завершено для шести исходных экзаменов**: восемь банков/73 печатных токена, question/word formation, выделение вопросного фрагмента, matching/fragment controls и ранняя валидация. Независимая source/browser приёмка и ревью APPROVED; основной объединённый Chromium exam 49/49 PASS, [evidence](P10-EVIDENCE.md).
11. **Reading/Listening/Writing session и правила экзамена через DSL — завершено.** Сгенерированная `.logic` policy владеет предметными решениями; общий контракт подключён к web и SwiftUI iPad. Исправления source review APPROVED; оставшиеся обязательные installed UI случаи и настоящее уменьшение окна закрыты **5/5 PASS**, финальное review APPROVED. [План](EXAM-DSL-LOGIC-CORRECTION-PLAN.md), [финальная приёмка](P11-WINDOW-AND-CONTROLS-ACCEPTANCE-20261112.md). Физический iPad отложен владельцем.
12. **Swift UiCopy — завершено**: 202 DSL-ключа на pack, 94 oracle cases, Apple snapshot и SwiftUI consumers; formatter сохраняет скобки в пользовательском вводе. Независимые tester/reviewer PASS/APPROVED; main generation, desktop/native snapshot и signed device build PASS; [evidence](SWIFT-UICOPY-DSL-EVIDENCE.md). Физическая VoiceOver-проверка NOT RUN.

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

## Проверенный checkpoint — 2026-11-08

Коммит `4a07fb1`: восстановлены все четыре раздела B1/2017, чтение и аудирование B1/2019, source-only B1/2020; добавлены точные дробные веса, письменные комплекты/иллюстрации и 10 DSL PlayTests. Независимый reviewer одобрил этот ограниченный срез. Новые счётчики каталога прошли Wasm compilation, их отдельная браузерная проверка после пересборки ещё NOT RUN. Подробности — [exam acceptance](EXAM-ACCEPTANCE-EVIDENCE.md) и [audio delivery](EXAM-AUDIO-ASSET-EVIDENCE.md).

B1/2019 грамматика/письмо и B2/2017–2020 остаются в работе. Открытые ответы реализуются по [контракту provisional grading](EXAM-OPEN-ANSWER-ADDENDUM.md), чтобы неизвестный допустимый вариант не считался автоматически ошибкой. Проверка оставшихся аудиограниц, все остальные разделы 2019 и задачи 5–12 не закрыты этим checkpoint.

### Следующий проверенный срез — `25be893`

Восстановлены грамматика и все письменные комплекты B1/2019; открытые ответы получают подтверждённые и ожидающие ручной проверки баллы без ложного окончательного результата. Исправлены потеря польского ввода в Firefox, восстановление фокуса checkbox и повторное воспроизведение аудио после конца сегмента. Независимые Tester и Reviewer завершили проверку этого ограниченного среза; ревью APPROVED. Аудиограницы сверены с транскрипциями и независимыми наблюдениями; ограничения метода указаны в [аудите](EXAM-AUDIO-BOUNDARY-AUDIT.md).

B1/2020 и B2/2017–2020 пока скрыты в каталоге. Для их подключения остаются интеграция восстановленных данных B2/2019, условный ответ word bank B2/2020, письменный жанр заявления и независимые проверки всех разделов/аудио. P5 реализуется в изолированном worktree; P6–P12 остаются открыты. Этот коммит не означает завершения всего плана или доставки аудио в чистую сборку без исходного архива.

P5 объединён в основной checkout в `c19e599`; выполняются P6/P7 (DSL oracle). Проверочная сборка шести экзаменов проходит независимую приёмку отдельно; P10/P11 и новые сдачи P9 остаются открыты.

### Шесть подключённых исходных экзаменов — `341238b`

B1/B2 2017, 2019 и 2020 доступны в каталоге после приёмки всех четырёх разделов. B1/2020 TAK/NIE исправлены по двум воспроизведённым браузерным ошибкам; B2/2020 условный вариант учитывает полный снимок ответов. Исправленная сборка `78e105e7…`: 38/38 Chromium exam PASS; исходный таймаут seekability одного MP3 в предыдущем полном прогоне остаётся явно записанным наблюдением, причина не доказана. Нельзя переносить этот результат на ещё не выполненные P10/P11/P9.

### Уточнение владельца — убрать тестовый экзамен

Удалены вручную добавленная web-карточка `B1 Przykładowy` и её Grammar alias. Каталог web теперь берётся только из общего DSL-backed `ExamCatalog`: 16 официальных вариантов. Наблюдавшийся RED 17 вместо 16 → GREEN: каталог/вход в timed и явный JS fallback во всех трёх браузерах, 7 PASS / 2 platform skips; JS/Wasm build и typecheck PASS. [Evidence](P9-PUBLIC-DEPLOYMENT-EVIDENCE.md#owner-follow-up-remove-the-demonstration-card).

## Новый обязательный срез P13–P19 — качество экзамена и обучения (2026-11-12)

По поручению владельца открыт [план качества экзаменов и учебного маршрута](EXAM-LEARNING-QUALITY-PLAN.md). Порядок: **P13** — source→model→экран аудит всех 16 опубликованных экзаменов и пустых ответов; **P14** — включённая по умолчанию мгновенная обратная связь при самостоятельном прохождении с фиксацией первого ответа и переходом через 2 с (полный timed exam показывает оценку после submit); **P15** — в отчёте явно видеть свой неверный выбор рядом с правильным; **P16** — устранить застревающие feedback/animation состояния и старые callbacks; **P17** — скрыть английские таблицы в польской матрице, сохранив их для `en-ru`; **P18** — по аудированным пробелам расширить правила, упражнения, лайфхаки и сводки с source/citation и четырьмя способами подачи; **P19** — независимые browser PlayTests, затронутые native UI проверки и review. Все семь пунктов открыты; предыдущие PASS не закрывают их. Работа идёт через Architect → Developer → Tester → Reviewer, с DSL для авторских данных и предметных решений, реальными web UI сценариями и отдельным статусом устройств.

Детальный контракт P14–P16 принят: [фиксация первого ответа, совместимость настройки и отменяемый переход](EXAM-PRACTICE-FEEDBACK-DESIGN.md). Первый вертикальный срез — общий DSL/core/preferences + web grammar `ChooseForm`; до проверки остальных controls и native этот срез не закрывает весь P14. Работа ведётся отдельно от P13/P17, чтобы не смешивать аудируемый артефакт и новую UX-семантику.

### Принятый P17 и очистка — 2026-11-13

**P17 завершён:** польская матрица не показывает английские блоки; native host сразу обновляет общий сеанс при принятой смене языка. Независимые Wasm/JS browser 24/24, сохранение польского прогресса 6/6, native bridge 2/2 и iPad Simulator 1/1 PASS, Reviewer APPROVED. Объединено в `66e65e9`; [evidence](P17-INDEPENDENT-VERIFICATION.md). Новая настройка обратной связи пока разрабатывается отдельно, этот commit её не публикует.

[Завершённые ветки/ворктри очищены](WORKTREE-CLEANUP-20261113.md) с сохранением полной истории и уникальных изменений. Состав P18 принят в [дизайне расширения](COURSE-EXPANSION-DESIGN.md); следующий срез — общий механизм и первая из десяти тем. P13 остаётся открытым до завершения повторной сверки официальных ключей и обнаруженных исправлений.

Уточнение 2026-11-13: механизм `staticPool` и первая тема P18 приняты в `23bc731` после независимых Kotlin/browser проверок, шести карточек/четырёх стилей в iPad Simulator и Reviewer APPROVED. Остальные девять семейств реализуются отдельно; весь P18 не закрыт. Для P16 сохранены два воспроизводимых RED-теста в активной ветке `work/p16-rive-lifecycle-20261113` (`dbe1080`, handoff `8039beb`); source Rive ещё не исправлен. Эти активные рабочие деревья нужны для текущих задач и не относятся к удалённым завершённым веткам.

### Проверенные исправления источников — 2026-11-13

Повторная сверка всех 16 официальных сдач охватила 2420 оцениваемых строк и 91 письменное задание; [source ledger](P13-OFFICIAL-SOURCE-REAUDIT.md) сохраняет PDF/ключи и историю находок. Исправления B1/2017 open-manual, вариантов B2/2024-02 и десяти числовых подсказок/связанных ответов C1 приняты независимо; последний source commit `3762024`, shared 3/3 и Chromium Wasm 7/7 PASS, Reviewer APPROVED. Пустые числовые подсказки восстановлены из PDF, а несовместимые формы больше не получают автоматический полный балл.

Pages deploy `43337751689` для `3762024` PASS. После публикации координатор проверил live-сайт Chromium 140 через настоящие нажатия: 16 карточек каталога и C1/2022-03 Grammar I/1 `(3)`, I/7 `(40)` PASS; фактически загружен `originWasmComposeApp.js`. Квитанция `../polski-grammar-srs-worktrees/recovery-logs/20261112/exam-learning-quality/p13/live-c1-postfix.{json,png}`. Это выборочная live-проверка исправления, не повторная проверка всех экзаменов или native app.

Промежуточный статус до финальной приёмки P13: [критерии письма для всех 16 сдач](WRITING-SOURCE-GUIDANCE-DESIGN.md) ещё реализуются. Первый ChooseForm-срез P14 сохранён отдельно в `04d1a1c`: независимые browser 12/12, weighted/timed 6/6 и настоящее скрытие вкладки PASS. Ревью выявило Major в блокировке при возврате; исправление `e7b32b5` принято: независимые 15/15 browser, настоящее скрытие вкладки и Reviewer APPROVED. Начат следующий ограниченный срез — все грамматические controls и общий frozen review. Другие controls и native feedback ещё не готовы. P16 получил [два настоящих RED-воспроизведения](P16-ANIMATION-REPRODUCTION.md), исправление пока впереди. Тогда общий статус составлял **13/19**.


### P13 закрыт — 2026-11-13

Commit `05ca753` завершает исправления источников и читаемости: критерии всех 16 экзаменов хранятся один раз в DSL, 91 задание сохранило темы/лимиты/наборы, длинные инструкции доступны через disclosure, текст и время аудио читаемы в обеих темах. Независимые PDF/source проверки, Firefox/WebKit по 19/19, финальный контраст 9/9 и нативные standalone/timed сценарии B1/B2/C1 на iPad Simulator PASS; Reviewer APPROVED. [Полные receipts и ограничения](P13-OFFICIAL-SOURCE-REAUDIT.md). Письмо сохраняет ручную оценку; физический iPad отложен владельцем, NOT RUN.

Остаются P14, P15, P16, P18, P19. Все web grammar controls и grammar review приняты отдельно в `893a5e9`; чтение/аудирование и native feedback ещё впереди. P18 содержит подготовленные 10 новых тем/60 упражнений и проходит независимую проверку содержания; это пока не опубликованный итог.

Публикация P13 `ea81239`: Kotlin check и Pages PASS. После деплоя координатор проверил live Wasm на 375×667: 16 экзаменов, C1/2022-11 writing disclosure и критерии 18/12, читаемый таймер B1/2017 (`rgb(79,98,119)` на `rgb(240,245,251)`) PASS. Receipt `../polski-grammar-srs-worktrees/recovery-logs/20261112/exam-learning-quality/p13/writing-guidance/live-writing-ea81239-final.{json,png}`. Первый live-пробник остановился при повторном переходе на тот же hash-route без сброса вложенного экрана; повтор с отдельной свежей страницей прошёл. Рабочий grammar checkpoint `d42b47d` также прошёл Kotlin CI; grouped Web и native остаются отдельными gates.


### P18 закрыт — 2026-11-13

В `f8bb4fb` объединены принятые `23bc731`/`d88cc86`: десять новых тем, 60 полноценных упражнений, четыре стиля, проверенные tips и Matrix summaries; всего 26 польских навыков и 42 tips. Первоначальные смысловые/библиографические находки исправлены и независимо перепроверены по Swan/WSJP, Reviewer APPROVED. Старые 16 IDs и прогресс сохранены; generic host reveal gate устраняет потерю мышиного нажатия без изменения FSRS. [История FAIL/исправления/приёмки](P18-SLICES-BD-INDEPENDENT-VERIFICATION.md).

Main integration: Biome generate/check, typecheck, focused shared tests и JS/Wasm compatibility build с 16 audio PASS; Chromium course/reference 5/5 и WebKit motion 1/1 PASS. Интеграционный strict-locator FAIL из-за двух временных DOM слоёв исправлен выбором единственной доступной учебной region; 600ms и persistence assertions сохранены. Нативные девять тем и исправленный Piotr проверены на iPad Simulator, сохранён Main app/progress; физические устройства NOT RUN. Pages deployment `43359159946` для `d57dd97` PASS. После публикации Playwright Chromium 140 на 375×667 подтвердил 26 навыков, source-backed преобразование Piotr/Jan, четыре стиля до раскрытия, видимый эталон после окончания анимации, сводку и 16 официальных экзаменов; фактически загружен Wasm, page errors отсутствуют. Receipt `../polski-grammar-srs-worktrees/recovery-logs/20261113/course-expansion/live/live-course-d57dd97.{json,png}`. Kotlin CI `43359160604` также PASS.

Остаются P14/P15 (native), P16 (финальная независимая приёмка Rive) и P19 (объединённая регрессия/публикация).


### P16 закрыт — 2026-11-13

Web Rive lifecycle `56652d0`/`b748832` принят независимыми Tester/Reviewer и объединён в `d155627`: route/unmount/background/Reduced Motion/OFF отменяют принадлежащие хосту эффекты; поколения и таймеры не дают старым callbacks воскресить слой, сбой/зависание bridge ограничено 3,2 с и допускает повторную попытку. FSRS и grading не менялись. Независимые JS/Wasm по 34/34 в Chromium/Firefox/WebKit (по два документированных timer skips), настоящий hidden в обеих ветках и Animations OFF PASS. [Квитанции, hashes и исходные FAIL](P16-RIVE-LIFECYCLE-INDEPENDENT-VERIFICATION.md).

После merge свежие Main JS/Wasm build с 16 audio, Biome check и typecheck PASS; Chromium по 6/6 и WebKit по 3/3 на каждом target подтвердили Rive и сохранение оценок P18. Завершённая ветка/worktree удалены с сохранением проверенных артефактов. Общий статус **16/19**. Остаются P14/P15 native и P19 объединённая приёмка/публикация. Нативный feedback lifecycle входит в эти оставшиеся gates; web PASS не означает запуск физического iPad.


### P19: web/shared принят, native ещё открыт

Объединённый Web P14/P15 в `c2902b7`, итоговые tests/evidence `2dacf34` приняты независимым Reviewer. Biome check, typecheck, Vitest 322/322; shared Desktop 41/41, JS/Wasm по 479/479; свежие JS/Wasm/compat build и 16 audio pins PASS. Selected browser: Wasm Chromium 24/24; JS Chromium initial 22/24 + исправленные два artifact-fixture случая 2/2 PASS; Firefox/WebKit combined по 28/28 на каждом target. Дополнительные import/OFF/legacy/timed/variant/recovery и настоящий hidden на обоих targets PASS. Первоначальные ошибки стенда сохранены в [P19 evidence](P19-INTEGRATED-WEB-VERIFICATION.md). Публикация этого web-среза запускается отдельно от native.

Native checkpoint `da80678`: developer bridge 34/34 и целевые iPad Simulator UI PASS; independent gate открыт. Независимый тест подтвердил ghost advance под Settings sheet (открытие через 0,52с после ответа, до двухсекундного timer); исправление возвращено разработчику. Общий статус остаётся **16/19**, P14/P15/P19 не закрыты.


### P19 live visual follow-up

Pages/Kotlin CI для `b81fcab` PASS. Live Chromium 140 на 375×667 проверил реальный Wasm и реальный JS: default ON, неверный первый ответ/блокировку/переход, frozen wrong/source report и OFF после reload — functional PASS, без page errors. Receipts `p19-integrated-web/live/live-feedback-b81fcab-{wasm,js}.json`. Отдельный просмотр PNG выявил **VISUAL FAIL отчёта на телефоне**: `flex`/`nowrap` сжимают context/выбранный ответ и обрезают эталон справа. Это остаётся P15/P19, исправление CSS и новый no-overflow PlayTest возвращены Developer; функциональный PASS не считается визуальным PASS. Исходные PNG и `live-report-visual-baseline.json` сохранены рядом с receipts.

Native independent RED Settings-sheet и бледных disabled options исправлены в `694a695`/`edaa6b4`, повторные четыре UI cases PASS. Измеренный контраст исправленных вариантов 5,83/6,29 (light), 7,45/10,75 (dark); соседние numeric labels нейтральны. Остальная native приёмка продолжается, общий статус **16/19** не меняется.
