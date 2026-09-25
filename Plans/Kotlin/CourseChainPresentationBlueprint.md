# Срез course pack: отображение пяти шагов тренировки

## Scope и baseline

Партия 4 [CoursePackRemainingRoadmap.md](CoursePackRemainingRoadmap.md) этапов 1–2 [CoursePacksPlan.md](CoursePacksPlan.md), после принятия партии 3. Перенести только фиксированные учебные подписи пяти шагов и авторский текст завершения цепочки в courses/pl-ru/course.json. Существующие src/training/generator.ts (generateChain) и kotlin/shared/src/commonMain/kotlin/polski/training/ExerciseFactory.kt (generateChain) вычисляют упражнения и остаются без изменений. reference.chainRows — пять справочных before/after примеров карты, у них другие labels («База», «Их вместо моей», «Говорю о…»); не подставлять их labels в тренировку и не переписывать chainRows.

Фактические потребители: src/ui/App.tsx (упорядоченный ol, completion); kotlin/composeApp/src/webMain/kotlin/polski/ui/TrainingWebApp.kt (renderChainHeader/renderChainComplete); kotlin/composeApp/src/desktopMain/kotlin/polski/ui/screens/TrainingScreen.kt; kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidTrainingScreen.kt; kotlin/shared/src/iosMain/kotlin/polski/ios/IosSnapshot.kt (верхнеуровневый snapshot); kotlin/iosApp/PolskiGrammar/PolskiGrammarApp.swift. React/Kotlin Web показывают пять нумерованных шагов с current/done и aria-current=step; Desktop/Android/iOS показывают «текущий / 5 · Вижу → Прошлое → Отрицание → Владелец → Говорю о» и индикатор прогресса на Desktop/Android. Все хосты показывают завершение и пять expected предложений; React/Kotlin Web дополняют их skill title, native — только номер и expected. Эти различия сохранить.

| Видимый текст | Точный baseline |
| --- | --- |
| Шаги, в порядке 0–4 | «Вижу», «Прошлое», «Отрицание», «Владелец», «Говорю о» |
| React completion | eyebrow «5 преобразований»; h2 «Цепочка завершена»; body «Ты изменил время, отрицание, владельца и падеж, сохранив одну мысль. Оценки сохранены в расписании повторений.» |
| Kotlin Web completion | h2 «Цепочка завершена»; body «Пять преобразований завершены. Оценки сохранены в расписании повторений.»; eyebrow отсутствует |
| Desktop/Android/iOS completion | «Цепочка завершена» без eyebrow/body |

Кнопки «Следующий набор слов», «К повторениям по расписанию» (React/Web) и «К повторениям» (native), выбор seed, aria-label «Шаги цепочки», progress/count format, card-meta «Цепочка · …», rating и schedule messages — UI chrome/поведение, не менять в этом срезе. Отсутствующую body в native не добавлять ради выравнивания. Текущее динамическое число steps равно пяти; текст «5 преобразований» — отдельная точная React-строка, а не новый источник числа в state.

## Контракт данных и связи

Добавить обязательный training.chainPresentation в pack (если training namespace отсутствует — NEW строгий объект верхнего уровня) со steps: упорядоченный массив ровно пяти `{id,label}`: `acc/Вижу`, `past/Прошлое`, `neg/Отрицание`, `owner/Владелец`, `loc/Говорю о`. Добавить completion `{title, reactEyebrow, reactBody, webBody}` с exact строками выше; native использует title и не получает body. Все значения read-only, nonblank/noHtml. Названия ID — ключи отображения, не skill IDs и не сериализуемые значения progress. Выбор действия по позиции остаётся генератором: 0 = один из case.acc.f/n/m по gender seed, 1 = verb.past, 2 = case.gen.neg, 3 = agreement.my, 4 = case.loc. Не закреплять один acc skill в данных. reference.chainRows и presentation.steps связаны только длиной и последовательностью преобразований, не равенством label.

courses/schema/course-pack-v1.schema.json: required training.chainPresentation, strict properties и exact five steps. scripts/validate-course.mjs: unique и fixed order IDs, непустые labels/completion, длина reference.chainRows = 5 и проверка существования фиксированных skill IDs для шагов 1–4 и допустимых acc variants для шага 0; путь ошибки /training/chainPresentation/steps/N. Не валидировать тексты против нынешней редакции в semantic validator; exact baseline держат pinned tests. Не вводить поля с готовыми expected предложениями или сроком повторения.

src/data/course.ts экспортирует узкий courseChainPresentation; Kotlin CourseData.kt — typed ChainPresentation/ChainStep с immutable list. React/Kotlin Web и native читают один ordered list; native summary строится `steps.map(label).joinToString(" → ")`, сохраняя текущие число/пробелы/разделители. iOS snapshot добавляет только step labels/summary и completion title к текущим scalar/chainAnswers; SwiftUI читает их, не загружает JSON отдельно. Никакого нового mutable state/async ресурса. Существующие AppAction.StartChain/SelectChainSeed/Rate и TS handlers не получают новых параметров.

## P02/P05 и handoff

Developer: exact fixture пяти ID/labels и трёх completion variants, negative validation на missing/duplicate/reordered step и пустую строку; Kotlin commonTest на typed order и summary; iOS snapshot test на те же значения. P02: для 12 seeds остаётся ровно 5 упражнений, previous.expected == next.source, неизменность «их», completion после пятой оценки, пять ожидаемых предложений, следующий seed по кругу; React/Web current/done/aria-current и native count/progress не меняются. Browser parity для React/Kotlin Web сверяет разные completion body при одинаковом состоянии и шаги 1–5; Desktop/Android/iOS сверяют summary и title без добавленной body. P05: rating каждого шага по-прежнему ровно один раз обновляет review/stats, mode и persisted progress остаются прежними; копия о сохранённых оценках не считается доказательством сохранения.

Перегенерировать courses/pl-ru/source-inventory.json существующим node scripts/inventory-course.mjs. Запускать реальные course:validate/course:inventory:check, адресные npm/Gradle/browser/native checks, с PASS/FAIL/NOT RUN в Plans/Kotlin/Evidence.md; отсутствие устройства не закрывает native gate. Developer владеет source и focused tests, Tester — независимым P02/P05, Reviewer — чтением diff/evidence. Этот файл — только архитектурный handoff; код и тесты не выполнялись на этапе проектирования.
