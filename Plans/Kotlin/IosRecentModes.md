# iOS: последние режимы упражнений

Дата: 2026-11-14. Основание: `ad70574` (общий Cloze и web «Подстановка»), `3e4529c` (нативный TileBuilder), запрос о недостающих режимах на физическом iPad. Исходный [Cloze-план](../CLOZE-EXERCISE-PLAN.md) явно откладывал iOS, а [SB blueprint](../Architecture/SENTENCE-BUILDER-BLUEPRINT.md) определил общую основу TileBuilder. Область: SwiftUI iPhone/iPad и `apple-bridge`; web, Android и замороженный macOS Desktop не меняются. Этапы 15–16 в [Plan.md](Plan.md) остаются открытыми до полной приёмки устройств.

## Область и потребитель

`kotlin/shared/src/commonMain/kotlin/polski/presentation/TrainingStore.kt` уже владеет `AppTab.Cloze`, `AppAction.StartCloze` и `ClozeAct`; `AppUiState.cloze` хранит временную сессию из десяти заданий. `ClozeSession.kt` определяет выбор, переходы и перезапуск. `IosSession` и `IosSnapshot` пока не передают Cloze нативному хосту, а `PolskiGrammarApp.swift` предлагает только Training, Matrix, Progress, Vocabulary и Exam. `TileBuilderView.swift`, `FlashCardView.swift`, `IosSession` и `IosSnapshot` уже содержат TileBuilder; нужно подтвердить его доступность на целевом iPad, а не реализовать повторно.

## Контракт Swift ↔ Kotlin

- `IosSession.dispatch("tab", "Cloze")` использует существующий `SelectTab`: при первом входе готовый `TrainingStore` сам запускает Cloze. Добавить команды `"clozeRestart"`, `"clozePrevious"`, `"clozeNext"`, `"clozeChoose"`. Для выбора передавать идентификатор текущего задания и форму без разделителя в пользовательском тексте: предпочтительно отдельный узкий метод `chooseCloze(exerciseId: String, form: String)`; он проверяет текущий ID и принадлежность формы текущим `options`, затем отправляет `ClozeAct(Choose)`. Повторный/устаревший выбор не меняет ответ. Навигационные команды направляют существующие `ClozeSessionAction`; неверные команды и вызов до загрузки являются no-op.
- `IosSnapshot.snapshot` добавляет поле `cloze`: `null` до старта; иначе объект `{ currentIndex, total, current }`, где `current` равен `null` для пустого пула или содержит `{ id, displaySentence, options, selectedForm, correctForm }`. Список вариантов сохраняет порядок общего ядра; `selectedForm` равен `null` до ответа, а `correctForm` отсутствует до ответа. Снимок не создаёт задания и не хранит отдельный индекс.
- `PolskiGrammarApp.swift` добавляет пункт «Подстановка» в `tabOrder`, `tabBarItems` и `tabContent`, а `AppModel.chooseCloze(id:form:)` лишь пересылает вызов в bridge; `ClozeView.swift` (**NEW**) читает только снимок и вызывает эти методы модели. Нужны предложение с польской языковой семантикой, три кнопки формы, номер задания, текстовый результат, «Назад», «Далее», «Заново», а также пустое состояние. Выбор блокируется после ответа; верный вариант и неверный выбор видны текстом и VoiceOver, а не только цветом. Высокий Dynamic Type и изменяемый размер окна iPad не должны обрезать длинное предложение или формы.
- Поведение web после выбора: результат виден 1,5 с, затем переход к следующему заданию, кроме последнего. Таймер принадлежит `ClozeView`, отменяется при уходе с вкладки, ручном переходе, перезапуске, смене задания и освобождении view. Перед автоматическим переходом сверять ID и индекс текущего снимка, чтобы запоздалый таймер не перескочил другую карточку. Показ результата и ручная навигация доступны без ожидания таймера.

## Компоненты и зависимости

| Файл | Работа |
| --- | --- |
| `kotlin/apple-bridge/src/iosMain/kotlin/polski/ios/IosSession.kt` | Узкие команды Cloze; валидация текущего выбора на границе Swift/Kotlin. Не добавлять хранилище или новый `TrainingStore`. |
| `kotlin/apple-bridge/src/iosMain/kotlin/polski/ios/IosSnapshot.kt` | Сериализация `AppUiState.cloze` в JSON без пересчёта заданий и прогресса. |
| `kotlin/iosApp/PolskiGrammar/PolskiGrammarApp.swift` | Пункт навигации и вызов нового экрана; общий `AppModel` и его жизненный цикл сохраняются. |
| `kotlin/iosApp/PolskiGrammar/ClozeView.swift` (**NEW**) | SwiftUI отображение, VoiceOver и локальный отменяемый таймер. |
| `kotlin/iosApp/generate_project.rb`, `kotlin/iosApp/PolskiGrammar.xcodeproj/project.pbxproj` | Зарегистрировать новый Swift-файл в генераторе и текущем Xcode-проекте. Сохранить уже внесённую регистрацию `TileBuilderView.swift` и изменения схемы. |
| `kotlin/apple-bridge/src/iosTest/kotlin/polski/ios/IosSessionTest.kt`, `kotlin/iosApp/PolskiGrammarUITests/PolskiGrammarUITests.swift` | Проверки bridge и видимого сценария; отдельные тесты только для различимого поведения. |

Направление зависимости: SwiftUI → `IosSession`/JSON snapshot → общий `TrainingStore`/`ClozeSession`. Cloze не пишет FSRS/`ProgressDocument`; оценки карточек по прежнему проходят только через `Rate`. `IosSession` остаётся владельцем `TrainingStore`, scope и observer, `AppModel` закрывает bridge при освобождении. Временная Cloze-сессия может начаться заново после холодного запуска; сохранённый прогресс от этого не меняется.

## Решения и приёмка

1. Сначала зафиксировать bridge-тест: вкладка Cloze запускает не более одной сессии при повторном входе; snapshot содержит текущее задание и три варианта; выбор даёт результат, повторный/устаревший выбор игнорируется; границы Next/Previous и Restart соблюдаются; пустой пул безопасен. Использовать изолированный `NSUserDefaults` и реально доступный iOS test target. Проверить, что `revision`/`savedRevision` и экспорт прогресса не меняются от Cloze-действий.
2. SwiftUI UI-сценарий: открыть «Подстановка», выбрать ошибочную форму, услышать/увидеть правильную, вернуться и убедиться, что ответ заблокирован; перейти/перезапустить; уйти со вкладки во время задержки и вернуться без неожиданного пропуска. Проверить VoiceOver label/status, Dynamic Type и iPad resize на доступном окружении. Если физический iPad недоступен, явно отметить `NOT RUN`.
3. Проверить TileBuilder на iPad через существующий экран Training: режим «Собрать из слов» виден, формы выбираются, результат и объяснение ошибки открываются, а одна оценка меняет review ровно один раз. При необходимости исправлять конкретный дефект в уже существующем пути. Настройки сейчас предлагают только Oral/Typed: добавить TileBuilder в `settingsAnswerModePicker`, чтобы режим можно было выбрать без текущей карточки; сохранить прежний wire value `TileBuilder` и проверить восстановление после запуска.
4. Сборка iOS simulator и signed device build, затем установка и запуск на физическом iPad с сохранённым прогрессом. Отдельно проверить видимый экран и действия на устройстве: одна лишь компоновка/запуск не доказывает, что режим доступен. После исправлений запускать затронутые bridge/UI тесты и соответствующие сборки; отчёт PASS/FAIL/NOT RUN вести отдельно для тестов, simulator, device build, device UI и доступности.

Разработчик обновляет [iOS.md](iOS.md) фактическими проверками и ограничениями. Генерируемые данные курса и Kotlin-строки не редактировать. Новое архитектурное решение для этого адаптера не требуется: он применяет уже принятый контракт нативного SwiftUI поверх общего состояния.
