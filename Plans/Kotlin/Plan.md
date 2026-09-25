# План перехода Polski Grammar SRS на Kotlin Multiplatform

**Решение от 2026-09-25:** переход на Kotlin выполнен; этап 11 (React ↔ Kotlin parity, WEB PARITY GATE) снят. Упоминания web parity gate ниже — история и больше не блокируют этапы.

**Актуальное решение по UI от 2026-09-25:** пользователь отменил кастомное стекло и поручил вернуться к оформлению до утренних экспериментов, восстановив учебное форматирование на всех платформах. Исполнение и приёмка — [PreGlassRollback.md](PreGlassRollback.md). [Промежуточный возврат к плоскому системному UI](NativeUIRollback.md) и стеклянные планы ниже — история, они больше не задают направление реализации. Светлая/тёмная/системная схемы, учебные функции и совместимость JSON сохраняются.

Восстановление UI проверено отдельно и не закрывает этапы миграции автоматически. Дальнейшая работа возвращается к первому незавершённому gate этого плана — этапу 2 с его браузерными, IME и assistive-technology условиями. Результаты отката и открытые проверки перечислены в [PreGlassRollback.md](PreGlassRollback.md#проверка-восстановления-25-сентября-2026).

**Уточнение интерфейсов от 2026-09-25:** по решению пользователя каждый host использует собственный идиоматичный UI: семантический DOM/CSS в web, Jetpack Compose Material 3 на Android, SwiftUI на iPhone/iPad **и macOS**. Общее Kotlin-ядро сохраняет грамматику, FSRS, данные и семантические действия. Детали перехода, включая статус текущего Compose Desktop preview, находятся в [NativeHostDecision.md](NativeHostDecision.md); при расхождении о конечном Mac UI этот документ имеет приоритет над прежними формулировками ниже.

Текущая UX-задача с двумя оценками, мобильными свайпами и выделением правил описана пошагово в [BinaryRatingUXPlan.md](BinaryRatingUXPlan.md). Она меняет видимый выбор в существующих приложениях, сохраняя четыре исторических значения FSRS в данных.

Продолжение работы над наглядным сравнением старой и новой формы в карточках и схемах описано в [ContrastHighlightPlan.md](ContrastHighlightPlan.md).

Расширение на языковые пакеты, разные методики и карточки слов разбито на этапы в [CoursePacksPlan.md](CoursePacksPlan.md), где указаны текущая реализация и открытые проверки. Первый пакет польский для русскоязычных; остальные языки добавляются после подготовки содержания.

Профессиональное обновление UI/UX, адаптацию навигации, системную/светлую/тёмную темы, Settings и напоминания выполнить по [PlatformUXPlan.md](PlatformUXPlan.md). Сначала сохранить текущую виртуальную приёмку CoursePacks 7–9, затем UX0–UX6 в порядке web → Mac → Android → iPhone/iPad. Рекомендации и источники внесены в общие платформенные навыки; новый план не объявляет функции реализованными.

Актуальный срез 24 сентября 2026 года: в [плане курсов](CoursePacksPlan.md) шаги 1–5 приняты, шаг 6 реализован и прошёл повторные проверки после исправления legacy-импорта; шаги 7–9 имеют адресную виртуальную матрицу Tester. Независимый Reviewer не нашёл Critical/Major в коде Step6/Steps7–9, но **не принял полный виртуальный gate** из-за непроверенного взаимодействия с упакованным Mac-приложением. React unit **201/201**, React browser **21/21**, Kotlin JS и Wasm browser по **174/174** в Chromium/Firefox/WebKit, общий Gradle test/assemble, финальные iPhone и iPad XCTest по **14/14**, отдельный iPhone Reduce Motion **1/1** и Mac Compose Desktop **38/38** прошли на исправленном срезе. Свежий [source/build manifest](artifacts/final-virtual/manifest.json) хранит хеши текущих исходников и артефактов; [матрица Tester](FinalVirtualAcceptance-Tester.md) — точные PASS/FAIL/NOT RUN. Взаимодействие с узким окном готового Mac `.app`, физические устройства, реальные IME и screen readers остаются **NOT RUN**; исходные gates этапов 2/11/14/16 и веб-публикация не закрыты.

По запросу пользователя открыта нативная iPhone/iPad lane со SwiftUI и общим Kotlin-сеансом. Реализация и текущие проверки — в [iOS.md](iOS.md); формальные зависимости и gate этапов 15–16 остаются открытыми.

Дата: 2026-09-23. Статус: этап 1 завершён; этап 2 имеет работающий Kotlin web spike, но обязательная проверка физических устройств и assistive technology остаётся открытой. Последовательность теперь включает React-эталон, локальное Kotlin web-превью, запрошенный macOS Desktop и затем Android/iOS; web parity gate остаётся обязательным перед переключением публикации. Создание skills и этого документа само по себе не означает завершения миграции приложения. По отдельному поручению пользователя после локального браузерного превью добавлена macOS desktop lane; она описана в [MacDesktop.md](MacDesktop.md). Это изменение приоритета не закрывает web parity gate. Локальная реализация desktop-хоста и проверки находятся в [MacDesktop.md](MacDesktop.md): JVM tests, запуск `.app`, визуальная проверка и сборка `.dmg` прошли; VoiceOver и независимое ревью остаются открытыми. По следующему поручению пользователя начата Android lane, статус и критерии в [Android.md](Android.md); этапы 13–14 пока не закрыты.

Прогресс на 2026-09-23: этап 1 завершён по Architect → Developer → Tester → Reviewer, итог ревью — APPROVED. Зафиксированы 2141 эталонный случай, браузерные сценарии и известные дефекты React; результаты и ограничения находятся в [Evidence.md](Evidence.md), [Parity.md](Parity.md) и [отчёте тестировщика](Stage1-Tester.md). На этапе 2 создан временный Kotlin web spike: независимый desktop suite прошёл 42/42 для Wasm и 42/42 для JS в Chromium, Firefox и Playwright WebKit. [WebCompatibility.md](WebCompatibility.md) и [Stage2-Tester.md](Stage2-Tester.md) фиксируют результаты. Ревью не нашло Critical/Major дефекта кода, но не приняло закрытие этапа из-за NOT RUN на физических Android/iPhone, с реальной IME и screen reader. Дальнейшая реализация идёт с provisional HTML-host решением; веб-паритет и публикация не достигнуты.

Этап 3 имеет Gradle wrapper, pinned версии, Yarn lock, отдельный CI check и [инструкцию запуска](../../kotlin/README.md). Тестировщик подтвердил wrapper/tasks/lock и 84/84 браузерных сценария на пересобранном артефакте ([Stage3-Tester.md](Stage3-Tester.md)); ревью не нашло дефекта кода. Формальное закрытие этапа отложено: clean committed checkout и живой GitHub Actions run не выполнены, поскольку файлы миграции пока не отслеживаются в Git. Реализация независимого Kotlin-ядра продолжается без изменения React-публикации.

Реализация и отдельная приёмка кода этапа 4 завершены: [Stage4-Tester.md](Stage4-Tester.md) подтверждает 1979 случаев и 26 тестовых методов на каждом JS/Wasm target; независимое ревью — APPROVED_WITH_MINOR. Справочные строки таблиц пока сверены статически и получат отдельную проверку на этапе UI. Зависимости этапов 2–3 формально остаются открытыми, поэтому их незакрытые gate не скрываются отметкой этапа 4.

Код этапа 5 также принят с minor замечанием: [Stage5-Tester.md](Stage5-Tester.md) подтверждает 112 exercise и 9 evaluation cases, 12 связанных цепочек и по 5 целевых тестов на JS/Wasm; независимое ревью — APPROVED_WITH_MINOR. Отдельный тест не доказывает независимость ID и content RNG, хотя порты разделены в реализации; UI/session сценарии остаются будущими этапами. Для этапа 6 по [решению о переносе FSRS](FsrsDecision.md) реализован общий Kotlin scheduler. После исправления граничных переходов и валидации карточек независимый тестировщик подтвердил 70 oracle cases и 99/99 SRS tests на каждом JS/Wasm target ([Stage6-Tester.md](Stage6-Tester.md)); ревью — APPROVED_WITH_MINOR без Critical/Major. Основной агент дополнительно прогнал весь shared suite: 142/142 на JS и 142/142 на Wasm. Общий scheduler принят как кодовый gate; физические браузеры и полный UI остаются этапами 2/11.

Этап 7 имеет общий codec/reducer/repository и браузерный LocalStorage-адаптер. При наличии React-прогресса или частичной миграции `load()` предупреждает до первой записи Kotlin-preview, а перенос остаётся явным действием. Основной агент повторил полный shared suite: 142/142 на JS и Wasm, и браузерный composeApp suite: 15/15 на JS и Wasm. Независимый [Stage7-Tester.md](Stage7-Tester.md) первоначально выставил **FAIL P11**: после временного отказа записи marker интерфейс не предлагал повторить перенос. После исправления разработчика независимый [ретест этапов 7–8](Stage7-8-Retest.md) подтвердил recovery → retry → Ready после двух перезагрузок на JS/Wasm в Chromium, Firefox и WebKit, а также 20-case progress ledger по задокументированному безопасному контракту. `TrainingStoreTest` прошёл 17/17 на каждой JS/Wasm цели, включая reset после удержанной записи и отказ устаревшей оценке. Независимый Reviewer оценил ретест как APPROVED_WITH_MINOR без Critical/Major; остались общие web/device gate и формальная сверка зависимостей этапов 7–8, поэтому чекбоксы ниже не закрыты автоматически.

После нативных host-работ текущий Kotlin web distribution снова прошёл 42/42 Playwright checks на Wasm и 42/42 на JS в Chromium, Firefox и WebKit. Дополнительно пять local-day fixtures и два midnight/DST перехода прошли 21/21 на каждой ветке, прямое сравнение React ↔ Kotlin для среза P08 прошло 3/3, а точные serialized outcomes `P-oral-good`/`P-typed-wrong` — 6/6 на каждой ветке. Обновлённые экраны тренировки, матрицы и прогресса видны в Safari на iPhone Simulator и Android Emulator WebView; Android WebView также прошёл экранный ввод, reveal/rate и reload с сохранением review. [WebCompatibility.md](WebCompatibility.md) фиксирует точные пределы этой проверки. Реализация экранов этапов 9–10 существует, но их полная независимая приёмка и этап 11 React ↔ Kotlin parity matrix не завершены. Виртуальные устройства не закрывают обязательные проверки реальных IME, мобильных браузеров и assistive technology.

Дополнительный browser fixture replay сравнил точные React JSON для `P-fresh`, `P-missing-skills`, `P-save-load`, `P-export-import` (15/15 на каждой JS/Wasm ветке вместе с `P-queue`). Отдельно unsafe React recovery cases проверены по безопасному Kotlin-контракту: malformed/unsupported preview остаётся доступным для raw export, ошибка storage read не создаёт пустой прогресс (9/9 на каждой ветке). Generated common test повторил второй same-skill review через полночь и три import raws из React-fixtures: 5/5 на каждом JS/Wasm target. [ProgressParity.md](ProgressParity.md) сопоставляет все 20 случаев с Kotlin-проверками и отличиями безопасности от React. Адресный независимый Tester/Reviewer ретест P11 от 24 сентября прошёл; полный web parity и device gate остаются открытыми.

Этапы 9–10 уже имеют видимый Kotlin browser UI: карточки, справку, четыре раздела матрицы и прогресс, связанные с общим состоянием. Production compatibility distribution собран для JS/Wasm. После последнего исправления основной агент повторил единый browser suite: 42/42 на каждой JS/Wasm ветке в Chromium, Firefox и Playwright WebKit, включая тренировку, матрицу, прогресс, перенос и 320px/крупный текст. Эти результаты не заменяют независимую приёмку всей P02–P12 матрицы, реальные мобильные браузеры, IME и screen reader; формальные этапы 7–11 остаются открытыми.

Обновление 24 сентября: при ретесте контраста выяснилось, что прежние 156/156 JS и Wasm после партии 6 обслуживали старый `build/dist`, хотя новые webpack-файлы были собраны. Это исправлено в [Evidence.md](Evidence.md). На действительно свежем `jsBrowserDistribution`/`wasmJsBrowserDistribution` после CSS-исправления переполнения при 320 px основной агент выполнил полный Kotlin Web Playwright suite: JS **156/156** и Wasm **156/156** в Chromium, Firefox и WebKit. Следующий свежий полный прогон после методик прошёл **159/159** на каждой ветке. Независимый Tester прошёл адресные сравнения 21/21 на каждой ветке и виртуальные Android/iPhone сценарии; финальный независимый Reviewer одобрил CSS-правку и методики без Critical/Major. Реальные IME, screen reader, физические Safari/Android и формальное закрытие web/device gate остаются **NOT RUN** по выбранной пользователем проверке на виртуальных устройствах. Следующие критерии [CoursePacksPlan.md](CoursePacksPlan.md) выполняются отдельно.

После расширения fixture и прямых parity-сценариев полный browser suite прошёл **120/120 на Wasm и 120/120 на принудительной JS-ветке** в Chromium, Firefox и Playwright WebKit. Он включает сравнение всех 12 пятишаговых цепочек, выбора 16 навыков, четырёх видимых интервалов FSRS и сохранённого результата всех четырёх оценок с React, а также защиту от повторной оценки скрытой карточки. Параллельный запуск более раннего suite сначала вызвал конфликт каталогов Playwright artifacts; конфигурация теперь разделяет результаты по веткам, оба полных последующих прогона прошли. Подробности и оставшиеся gate — в [Evidence.md](Evidence.md) и [ProgressParity.md](ProgressParity.md).

## 1. Цель, границы и порядок работы

Цель — общее Kotlin-ядро грамматики, упражнений, прогресса и контракта сессии, с интерфейсом, подходящим каждой платформе: Kotlin DOM для браузера, Jetpack Compose Material 3 для Android и SwiftUI для iPhone/iPad и Mac. Существующий Compose Desktop остаётся переходным preview до нативного Mac host. Текущее решение по визуальному слою — [возврат к оформлению до экспериментов со стеклом](PreGlassRollback.md): восстановить учебную иерархию UX2 и нативных host при сохранении светлой/тёмной/системной схемы и без собственного optical renderer. [План Liquid Glass](LiquidGlassRollout.md), [выбор реализаций](GlassImplementationSelection.md), [оптический пилот](GlassOpticsPilot.md) и [промежуточный возврат к плоскому системному UI](NativeUIRollback.md) сохранены как архив решений. React-версия больше не служит обязательным эталоном: отдельное сравнение React ↔ Kotlin снято (этап 11).

В текущую работу входят подготовка инструкций для агентов и последовательного плана. Переписывание приложения, изменение production deployment, удаление React, публикация мобильных приложений и расширение учебного курса — последующие задачи. Импорт через интерфейс, синхронизация, аккаунты, PWA, уведомления и новый контент не являются условиями соответствия текущей версии.

Работа идёт по [workflow](../../.claude/skills/workflow/SKILL.md): Architect → Developer → Tester → Reviewer. Каждый этап ниже начинается после приёмки предыдущего; независимая работа допустима только после явного назначения непересекающихся файлов. Согласование blueprint происходит внутри этой цепочки и не требует повторного разрешения пользователя на уже порученную работу. Разработчик сохраняет TDD и исправление production-кода; тестировщик независимо проверяет критерии приёмки и дополняет тесты. При замечаниях Tester/Reviewer разработчик исправляет конкретные блокеры; Tester перепроверяет затронутые сценарии, Architect подключается повторно, когда меняется контракт.

| Задача агента | Какие инструкции читать |
| --- | --- |
| Границы модулей, состояние, зависимости, миграция данных | [module-architecture](../../.claude/skills/module-architecture/SKILL.md), [kotlin](../../.claude/skills/kotlin/SKILL.md) |
| Kotlin API, имена, nullability, coroutines, форматирование | [kotlin](../../.claude/skills/kotlin/SKILL.md), [code-style](../../.claude/skills/code-style/SKILL.md) |
| Общий Compose UI, доступность, адаптивная верстка, анимации | [compose-multiplatform-ui](../../.claude/skills/compose-multiplatform-ui/SKILL.md) |
| Браузер, Wasm/JS, DOM-интеграция, клавиатура, web storage | [kmp-web](../../.claude/skills/kmp-web/SKILL.md) |
| Android lifecycle, системные отступы, ввод, доступность | [kmp-android](../../.claude/skills/kmp-android/SKILL.md) |
| iOS shell, Swift interop, safe areas, ввод, доступность | [kmp-ios](../../.claude/skills/kmp-ios/SKILL.md) |
| Поведенческие проверки и браузерные сценарии | [testing-tdd](../../.claude/skills/testing-tdd/SKILL.md), [playwright-testing](../../.claude/skills/playwright-testing/SKILL.md), skill соответствующей платформы |

Текущий проект уже имеет [продуктовый контракт](../../docs/product-contract.md), [README](../../README.md) и [исторический отчёт проверки](../../docs/verification.md). Последний описывает неполную браузерную проверку; его нельзя использовать как доказательство текущего полного соответствия.

## 2. Подтверждённая исходная реализация

| Существующий путь и символы | Поведение, которое переносим | Предлагаемый Kotlin-путь: NEW |
| --- | --- | --- |
| `src/types.ts`: `Noun`, `Adjective`, `Verb`, `Skill`, `Exercise`, `Progress`, `StoredCard`, `SerializedCard` | Идентификаторы и значения грамматических категорий, формы, состояние повторений | `kotlin/shared/src/commonMain/kotlin/polski/model/GrammarModels.kt`, `Exercise.kt`, `Progress.kt`; wire DTO отдельно в `progress/LegacyProgressDto.kt` |
| `src/data/nouns.ts`, `adjectives.ts`, `verbs.ts`, `pronouns.ts`: словари, `conjugate`, `possessiveForm` | Все существующие формы и исключения; исходные ID не переименовывать | `kotlin/shared/src/commonMain/kotlin/polski/data/{Nouns,Adjectives,Verbs,Pronouns}.kt` |
| `src/grammar/engine.ts`: `nounForm`, `adjectiveForm`, `nounPhrase`, `verbForm`, `normalize`, `capitalize` | Склонение, согласование, время и нормализация польского текста | `kotlin/shared/src/commonMain/kotlin/polski/grammar/GrammarEngine.kt`, `AnswerNormalizer.kt` |
| `src/training/skills.ts`: `skills`, `skillById` | 16 навыков, ID, русский текст, prerequisites и порядок | `kotlin/shared/src/commonMain/kotlin/polski/training/Skills.kt` |
| `src/training/generator.ts`: `sentenceSeeds`, `caseSentence`, `generateForSkill`, `generateChain` | 12 наборов слов; пять связанных преобразований; ограничения выбора слов для навыков | `kotlin/shared/src/commonMain/kotlin/polski/training/ExerciseFactory.kt` |
| `src/training/evaluator.ts`: `evaluate`, `Evaluation` | Сравнение с эталоном и accepted, нормализованный ответ и расстояние | `kotlin/shared/src/commonMain/kotlin/polski/training/AnswerEvaluator.kt` |
| `src/training/queue.ts`: `nextSkillId` | Выбранный навык сохраняет focused drill; автоматическая очередь по due | `kotlin/shared/src/commonMain/kotlin/polski/training/SkillQueue.kt` |
| `src/srs/scheduler.ts`: `newSkillCard`, `serialize`, `deserialize`, `preview`, `review`, `isDue` | FSRS, четыре оценки, сериализованные поля и даты | `kotlin/shared/src/commonMain/kotlin/polski/srs/Scheduler.kt`, `FsrsScheduler.kt` |
| `src/progress/review.ts`: `recordReview` | Одна оценка обновляет карточку, stats, streak, дневной и общий счётчики | `kotlin/shared/src/commonMain/kotlin/polski/progress/ReviewReducer.kt` |
| `src/progress/storage.ts`: `localDay`, `freshProgress`, `loadProgress`, `saveProgress`, `resetProgress`, `exportProgress`, `importProgress` | Локальное сохранение, восстановление, добавление новых навыков, JSON | `kotlin/shared/src/commonMain/kotlin/polski/progress/ProgressCodec.kt`, `ProgressRepository.kt`; web adapter в `kotlin/composeApp/src/webMain/kotlin/polski/platform/WebProgressRepository.kt` |
| `src/ui/App.tsx`: `App`, `startChain`, `automatic`, `choose`, `rate`, `persist`, `download`, `reset` | Три вкладки, три режима, ввод и раскрытие ответа, оценки, прогресс | `kotlin/composeApp/src/commonMain/kotlin/polski/presentation/TrainingStore.kt`, `AppUiState.kt`, `AppAction.kt`; `ui/App.kt`, `ui/training/TrainingScreen.kt`, `ui/progress/ProgressScreen.kt` |
| `src/ui/GrammarTables.tsx`: `GrammarTables`, `CaseReference`, `caseRows`, `genderNames` | Карта системы, падежи, глаголы, местоимения, контекстная справка и переходы в тренировку | `kotlin/composeApp/src/commonMain/kotlin/polski/ui/grammar/GrammarScreen.kt`, `CaseReference.kt`; общие данные справки в `shared/.../grammar/GrammarReference.kt` |
| `src/ui/style.css` | Тема, адаптация узкого экрана, горизонтальная прокрутка таблиц, focus, reveal и reduced motion | `kotlin/composeApp/src/commonMain/kotlin/polski/ui/theme/AppTheme.kt`, `ui/components/`, платформенная motion policy |
| `src/main.tsx`, `vite.config.ts`, `.github/workflows/deploy.yml` | Запуск, относительные asset paths, GitHub Pages | Новые web entrypoints в `kotlin/composeApp/src/{wasmJsMain,jsMain}/kotlin/polski/main.kt`; новая сборка первоначально без production deploy |
| `tests/grammar.test.ts`, `regressions.test.ts`, `sentences.test.ts`, `app.test.tsx` | Имеющийся набор из 38 проверок в четырёх файлах | Сохранить эталон; добавить Kotlin common/platform tests и общие JSON fixtures по этапам |

`webMain` здесь — планируемый общий source set для `jsMain` и `wasmJsMain`, а не предположение о существующей настройке Gradle. Его иерархию и доступность библиотек подтверждает этап 2. Перечисленные пути Kotlin не существуют до начала реализации.

Фактический стек эталона: React + TypeScript + Vite + Vitest; `package-lock.json` фиксирует `ts-fsrs` **5.4.2**. Есть `npm test`, `npm run typecheck`, `npm run build`; существующий deploy после push в `main` запускает тесты и публикует `dist`. Сохранять этот workflow до отдельного этапа переключения.

## 3. Архитектурный контракт

### Минимальные модули и направление зависимостей

Начальная структура — `kotlin/shared` и `kotlin/composeApp`. `shared` содержит Kotlin-модели, данные, чистые вычисления, FSRS и узкие контракты внешних возможностей; не зависит от Compose, DOM, Android или Foundation. `composeApp` зависит от `shared`, владеет общим UI и подключает платформенные реализации. Отдельные `kotlin/androidApp` и `kotlin/iosApp` добавляются в соответствующих этапах. Не создавать модуль на каждую папку, feature framework, event bus или DI framework без выявленной необходимости.

Начальные targets определяет web spike: `wasmJs` и, если нужен compatibility mode, `js`. JVM можно добавить как вспомогательный быстрый тестовый target только с ясной целью; JVM-прохождение не заменяет браузерный запуск. Native targets добавляются позднее; ранняя формальная декларация target не считается поддержкой платформы.

### Публичные контракты: проектируемые API

Ниже контракт, а не готовый код. Конкретные импорты Instant/Clock и сигнатуры фиксируются в blueprint после выбора совместимой toolchain.

| Контракт NEW | Входы, выходы и ошибки |
| --- | --- |
| `GrammarEngine` | Чистые `nounForm`, `adjectiveForm`, `nounPhrase`, `verbForm`, `caseSentence`; допустимые ID/enum дают те же формы, неизвестные ID и невозможные формы дают явную доменную ошибку |
| `ExerciseFactory` | `generateForSkill(skillId, preferredSeed?)`, `generateChain(seed)`; получает `RandomSource` и независимый `ExerciseIdFactory`; не читает системные часы и не обращается к хранилищу |
| `AnswerNormalizer`, `AnswerEvaluator` | NFC, польское приведение регистра, trim, свёртка пробелов, удаление завершающей `.?!`; диакритика сохраняется; `Evaluation(correct, normalized, expected, distance)` |
| `Scheduler` | `newCard(skillId, at)`, `preview(card, at)`, `review(card, rating, at)`, `isDue(card, at)`; preview не меняет состояние; даты и FSRS-поля сохраняют wire-совместимость |
| `LocalDayProvider` | Локальная дата для переданного `Instant`; runtime time zone передаётся через адаптер, тесты задают её явно |
| `ReviewReducer` | `recordReview(progress, skillId, rating, typedCorrect?, at, localDay)` возвращает новое состояние; устная точность определяется оценкой, печатная — результатом проверки |
| `ProgressCodec` | Декодирование и кодирование legacy v1, явные результаты valid/unsupported/invalid; ISO даты, числовые FSRS enum и отсутствие `last_review` обрабатываются без потери смысла |
| `ProgressRepository` | `suspend load()` и `suspend save(snapshot)`; явные missing/loaded/invalid/unavailable/write-failed; ошибки не превращаются молча в успешное сохранение |
| `TrainingStore` | Наблюдаемый immutable `StateFlow<AppUiState>`, `dispatch(AppAction)`; единственный владелец сессии, выбранных режимов и in-memory прогресса; платформенные эффекты имеют явное завершение |

NFC и локаль не заменять на удаление диакритики или default lowercase: перенос `src/grammar/engine.ts` проверяется на decomposed Unicode. Если подходящей common-библиотеки нет, `AnswerNormalizer` остаётся узким портом с web/native адаптерами и единым набором fixtures. Выбор библиотеки требует подтверждения её поддержки каждого target.

### Состояние, эффекты и lifecycle

- Долгоживущий `Progress` принадлежит repository + `TrainingStore`; сценарий тренировки хранится в store. Режимы chain/schedule/focused, текущая цепочка и шаг, exercise, answer mode, текст и reveal/complete не восстанавливаются из legacy прогресса автоматически: React также не сохраняет текущую сессию между загрузками.
- Поля таблиц живут на уровне экрана/его state holder. Переход в таблицы и назад сохраняет текущую карточку и ответ согласно эталону; контракт lifetime для настроек таблиц фиксируется по baseline, а не угадывается.
- Composable получает состояние и callbacks; не выбирает следующую карточку и не пишет в хранилище во время recomposition. Переключение режима/набора — action, а не побочный эффект появления элемента.
- `Rate` допустим только после reveal; store сразу отмечает конкретный exercise оценённым. Повторный click/key event и completion устаревшей операции не создают второй review. Мутации обрабатываются последовательно; сохранения сериализованы и привязаны к ревизии snapshot.
- Один `at` используется для расчёта одного review и его local day. Preview использует согласованный момент времени; периодический tick и возврат из background обновляют due/today. Работа таймера прекращается с владельцем, без `GlobalScope`.
- Ошибка сохранения оставляет доступный in-memory snapshot и заметное сообщение с экспортом; ошибку нельзя замаскировать успешным UI. Старое завершившееся сохранение не убирает более новое сообщение об ошибке и не перезаписывает новую ревизию.
- Focus, download/share, запрос подтверждения сброса и platform motion policy принадлежат shell/адаптерам. Однократные эффекты имеют ID и подтверждение обработки либо выражаются устойчивым UI state; не терять обязательный эффект из-за отсутствующего collector и не повторять экспорт после recomposition.
- Coroutine scope принадлежит явному lifecycle owner; закрытие/смена владельца отменяет работу. Решение о common ViewModel принимается после проверки поддержки выбранных targets; для трёх вкладок отдельный navigation framework пока не обязателен.

### FSRS и данные пользователя

Текущие настройки обязательны к переносу: `request_retention = 0.9`, `maximum_interval = 3650`, `enable_fuzz = true`, `enable_short_term = true`, `learning_steps = ['1m', '10m']`, `relearning_steps = ['10m']`. Значение по умолчанию из другого FSRS-пакета не считается эквивалентным. Зафиксировать реальные веса/defaults и сериализованные поля из установленного `ts-fsrs` 5.4.2.

Сначала проверить существующие Kotlin FSRS реализации по версии алгоритма, лицензии, коротким шагам, targets и fixtures. Если подходящей нет — ограниченный порт нужного алгоритма с сохранением атрибуции. JS bridge к существующему `ts-fsrs` допустим как явно временный инструмент web spike, но не закрывает этап общего scheduler и готовность Android/iOS.

Для числовой модели задать допустимую погрешность после измерения эталона; при выключенном fuzz сопоставлять точные состояния и даты с заданными округлениями. Production fuzz остаётся включённым. Для fuzz отдельно проверить диапазоны, монотонность/ограничения, воспроизводимость при управляемом источнике случайности и отсутствие изменения preview; не требовать совпадения внутренней последовательности случайных чисел JS и Kotlin. Если нужна точная совместимость fuzz, выделить и зафиксировать алгоритм/seed в адаптере до реализации, а не ослаблять критерии после расхождения.

Legacy ключ: **`polski-grammar-srs-v1`**. LocalStorage разделяется по origin, а не по URL path: React и `/kotlin-preview/` на том же origin могут видеть один ключ. На localhost и production origin данные различаются. По умолчанию Kotlin preview использует отдельный ключ `polski-grammar-srs-kmp-preview-v1` и копию fixture/экспорта. React и Kotlin не должны одновременно писать в legacy ключ во время сравнений.

Контракт миграции:

1. Читать legacy только после явного запуска migration/recovery flow; сохранить исходный raw JSON без изменений как backup и предоставить экспорт. Если backup записать не удалось, не выполнять разрушительное продолжение.
2. Проверить version, структуру, даты, числовые значения, skill ID и дубликаты; неизвестные/неподдерживаемые записи не терять молча. Невалидный JSON сохраняется для восстановления, пользователь видит ошибку; silent reset не переносим как обязательную особенность эталона.
3. Добавить отсутствующие известные навыки, сохранить существующие карточки и stats. Daily counter нормализовать по текущему локальному дню; total/streak не сбрасывать. Зафиксировать политику будущих/неизвестных полей и skill ID в codec tests.
4. Записать новое состояние под отдельным KMP ключом, прочитать обратно и сравнить. Маркер успешной миграции записать последним; повторный запуск идемпотентен. Legacy оригинал до cutover остаётся нетронутым.
5. До переключения origin/version проверить export → legacy decoder → React. Для rollback сохранить совместимый экспорт **текущего** KMP состояния: восстановление только старого backup потеряло бы обзоры после перехода. Если обратное преобразование невозможно, это блокер переключения, а не повод молча откатить данные.
6. В cutover/recovery процедуре остановить старый клиент и исключить двух writers. Не перезаписывать legacy из stale preview. Отдельно проверить reload, interrupted migration, quota/private-mode failures и повторную миграцию.

`importProgress` уже существует как функция, но импорт-кнопки в текущем UI нет. Migration/recovery harness и codec нужны для безопасности перехода; полноценный новый экран импорта — отдельная продуктовая задача.

## 4. Матрица функционального соответствия

Эта матрица становится NEW `Plans/Kotlin/Parity.md` на этапе 1: каждая строка получает сценарии, fixture IDs, результаты React/Kotlin, браузер/устройство и ссылки на evidence. Совпадение screenshots по пикселям не является целью; обязательны учебный смысл, содержимое, ввод, доступность и результаты действий.

| ID | Что должно сохраниться | Обязательные примеры |
| --- | --- | --- |
| P01 | Данные и грамматика | 16 навыков, 12 seeds, 14 существительных, все прилагательные/глаголы/местоимения; 7 падежей, sg/pl, 5 родовых категорий; `kolega`, `drogi`, `być`, perfective без настоящего времени |
| P02 | Цепочки | Все 12 × 5 шагов; `previous.expected == next.source`; завершение, итог пяти предложений, следующий seed по кругу; «их» неизменно |
| P03 | Все навыки | Все 16, допустимые seed restrictions, preferred seed, accepted варианты, explanation/changes/tags; source — полное предложение |
| P04 | Печатный ответ | Эталон и accepted, регистр/пробелы/конечная пунктуация, NFC, польская диакритика, пустой/неверный ответ; после проверки ввод зафиксирован; Enter проверяет, Shift+Enter вводит перенос; IME composition не проверяется преждевременно |
| P05 | Устная оценка | Ответ скрыт до reveal; снова/трудно/хорошо/легко; точность отдельно от interval rating в typed mode; stats/streak изменяются один раз |
| P06 | Режимы и очередь | По умолчанию chain; focused сохраняет навык; schedule выбирает минимум due и показывает завершение, когда due нет; режим меняется без переноса stale answer/review |
| P07 | Клавиатура и focus | Space reveal, 1–4 rating, фильтрация повторов/модификаторов; стандартная активация buttons/links; hotkeys не перехватывают textarea/select/IME; следующий reveal получает focus; клавиатура работает без мыши |
| P08 | Таблицы | Карта, дерево Biernik, падежи и целые предложения, 7 типов склонения; 9 подлежащих × 3 времени, выбор рода и глагола; личные и 7 притяжательных; все контролы и drill-переходы |
| P09 | Контекстная справка | Текущий noun/adjective/owner/number, подсветка active case; открытие/закрытие и переход в матрицу не теряют текущую карточку |
| P10 | FSRS | Новая/learning/review/relearning карточка, каждая оценка, повтор в один день, просрочка, preview, round trip всех полей, короткие шаги, fuzz и maximum interval |
| P11 | Прогресс | due/today/total, точность без отзывов «—», локальная полночь и смена timezone/DST, missing skills; перезагрузка, JSON export, подтверждение/отмена reset, ошибка сохранения |
| P12 | Браузер и адаптация | Узкий viewport от 320 CSS px, desktop, landscape, zoom/крупный текст, прокрутка таблиц; focus видим, контраст/смысл доступны без цвета; reduced motion; Safari/iOS keyboard и реальный Android browser |

Если baseline выявляет ошибку React (например, edge case IME), зарегистрировать отличие и ожидаемое исправленное поведение. Не закреплять дефект в Kotlin ради формального совпадения и не скрывать исправление внутри «полной идентичности».

## 5. Последовательные этапы

Все пункты изначально `[ ]`. После этапа записывать отдельно PASS / FAIL / NOT RUN, команду, cwd, commit/diff, версию runtime, fixtures и приложенные артефакты в NEW `Plans/Kotlin/Evidence.md`. Переход допускается после требуемых критериев и отсутствия нерешённых Critical/Major замечаний. Для документации достаточно статической проверки; не выдумывать RED/GREEN для markdown.

### Этап 1. Зафиксировать эталон React и детерминированные сценарии

- [x] **Зависимости:** подготовленный plan и skills; чтение actual source/lockfile и изменений пользователя.
- **Действия:** создать `Parity.md`; зафиксировать baseline revision, `npm test`, `npm run typecheck`, `npm run build`. Выполнить сценарии P01–P12 там, где они уже доступны; тесты jsdom описать отдельно от реального браузера. Снять состояния основных экранов на desktop и narrow viewport. Выполнить экспорт seed progress без пользовательских данных.
- **Выход:** NEW `tests/fixtures/kotlin-parity/` с JSON grammar/exercise/progress/scheduler cases, manifest параметров и происхождения; NEW `Plans/Kotlin/{Parity,Evidence}.md`. Часы и выбор случайных вариантов контролировать harness, не меняя продуктовый результат. Exercise ID нормализовать отдельно; не сравнивать строки, полученные неконтролируемым RNG.
- **Приёмка:** fixture описывает вход и ожидаемый наблюдаемый результат; охвачены все 16 навыков, 12 цепочек, 14 существительных. Реальные DOM/keyboard assertions baseline сохранены. Исторические 38 тестов сами по себе не закрывают браузерную часть.
- **Передача:** Architect утверждает границы parity, Developer создаёт fixtures/harness, Tester проверяет сценарии и воспроизводимость результатов, Reviewer проверяет итоговые изменения, источник и достаточность доказательств.

### Этап 2. Web spike: доказать пригодность Compose до полного переноса

- [ ] **Зависимости:** этап 1.
- **Действия:** создать минимальный временный web consumer в `kotlin/composeApp` и `shared`; один экран с польским текстом, typed input, reveal/ratings, таблицей и локальным round trip. Проверить Compose/Wasm и unified JS fallback, если он требуется целевым браузерам. Зафиксировать browser support matrix с конкретными версиями на момент проверки.
- **Критические проверки:** Safari на iOS, Chrome на Android, desktop Chromium/Firefox/Safari; IME и Polish keyboard, soft keyboard resize, focus/Tab/Space/Enter, screen reader semantics, zoom/scroll, reduced motion. Canvas/semantics Compose не приравнивать к React DOM: проверить, что доступно настоящим browser locators; выбрать поддерживаемые Compose/host tests и ручные assistive-technology сценарии для остального. Скриншот без интеракции не доказывает доступность.
- **Выход:** NEW `Plans/Kotlin/WebCompatibility.md` с toolchain candidate, результатами, bundle/startup measurements и выбранной тестовой стратегией.
- **Приёмка:** все обязательные сценарии исполнимы на целевых браузерах; fallback проверен как загруженная JS-ветка, а не только как собранный файл. Обнаруженный blocker input/semantics/Safari означает остановку полного UI переноса. Architect предлагает конкретный web adapter/DOM-hosted control либо пересматривает web UI стратегию с сохранением общего ядра; существенное изменение продуктового scope выносится координатору.

### Этап 3. Зафиксировать воспроизводимую Kotlin-сборку

- [ ] **Зависимости:** принято решение этапа 2.
- **Действия:** оформить `kotlin/settings.gradle.kts`, root/module `build.gradle.kts`, Gradle Wrapper, `gradle/libs.versions.toml`, Java toolchain; зафиксировать совместимые Kotlin/Compose/Gradle/JDK и нужные библиотеки. Версии выбирать по актуальной compatibility table и доказанной сборке, без динамических `+`. Зафиксировать package-manager lock для web dependencies, если их создаёт выбранный плагин; определить воспроизводимость зависимостей и проверку checksum wrapper.
- **Выход:** NEW `kotlin/README.md` с реальными командами; NEW CI check workflow, который собирает Kotlin и сохраняет artifact, **не публикует** Pages. Имеющийся React deploy остаётся прежним.
- **Приёмка:** clean checkout собирает и запускает web preview; перечислены реально существующие Gradle tasks после `./gradlew tasks --all`. Команды Kotlin tests/build записываются по полученному проекту — до этого в плане не выдаются предполагаемые task names за проверенные.

### Этап 4. Перенести модели, словари и грамматический движок

- [ ] **Зависимости:** этап 3.
- **Действия:** создать `model`, `data`, `grammar` в `shared`; сохранить IDs и тексты. Перенести `conjugate`/possessives и `nounPhrase`, реализовать normalizer port там, где необходима платформа.
- **Выход:** чистые Kotlin API и common tests на fixtures P01; platform tests нормализации для web targets.
- **Приёмка:** все табличные формы совпадают, отличия явно разобраны; нет потери NFC/диакритики и выдачи perfective present. Целевые тесты проходят в actual web target, не только JVM.
- **TDD:** тест на отсутствующее поведение наблюдаемо RED, минимальная реализация GREEN, затем refactor и affected checks; missing import не считается требуемым RED.

### Этап 5. Перенести упражнения, проверку ответа и очередь

- [ ] **Зависимости:** этап 4.
- **Действия:** `Skills`, `ExerciseFactory`, `AnswerEvaluator`, `SkillQueue`; внедрить `RandomSource` и `ExerciseIdFactory`, использовать управляемый выбор ветвей. Сохранить поведение explicit selection и приоритет due, определить стабильный порядок для equal due.
- **Выход:** P02–P04/P06 common tests, generator fixtures для всех навыков и допустимых seeds.
- **Приёмка:** 12 связанных цепочек по пять шагов; accepted варианты и критерии typed correct совпадают; отсеивание seed по навыку сохранено. Проверяется результат заданного выбора, а не число внутренних вызовов RNG. Unknown skill и пустая очередь дают документированный результат/ошибку.

### Этап 6. Выбрать и доказать общий FSRS scheduler

- [ ] **Зависимости:** этап 5 и scheduler baseline fixtures.
- **Действия:** сравнить поддерживаемые Kotlin реализации с установленным `ts-fsrs` 5.4.2; зафиксировать выбор в NEW `Plans/Kotlin/FsrsDecision.md`. Сопоставить все state/rating wire значения, даты, weights/defaults, learning/relearning, fuzz и ограничения. Реализовать `Scheduler` с переданным временем и без globals.
- **Выход:** общий scheduler, serialisation mapping и deterministic differential tests P10. Тестовый режим fuzz-off не меняет production настройки.
- **Приёмка:** состояния/даты/поля проходят fixture comparison по заранее записанным допускам; все четыре preview согласованы с review при одинаковом `at`; preview не мутирует карточку. Fuzz проверен отдельно. Нельзя закрыть этап только npm bridge либо заменой FSRS другим алгоритмом.

### Этап 7. Перенести прогресс и безопасную миграцию legacy

- [ ] **Зависимости:** этап 6.
- **Действия:** `ReviewReducer`, `ProgressCodec`, `ProgressRepository`, web storage adapter; реализовать протокол backup → validation → new key → read-back → marker. Одним временем вычислять review/day. В preview ключ изолирован от React.
- **Выход:** tests P05/P10/P11, migration/recovery harness, процедура экспортирования совместимого legacy состояния для rollback.
- **Приёмка:** real browser localStorage round trip; old JSON с missing skills сохраняет историю; invalid/quota/unavailable/interrupted migration не уничтожают оригинал. Ночью и после timezone change correct daily count; oral/typed точность независима от rating. Повторная миграция идемпотентна, reset требует подтверждения, export остаётся доступен после failed save. До React rollback проверен текущий, а не только исходный snapshot.

### Этап 8. Реализовать UDF state holder и жизненный цикл сессии

- [ ] **Зависимости:** этап 7.
- **Действия:** `AppUiState`, `AppAction`, `TrainingStore`; связать shared domain/repository/clock и UI effects. Перенести `startChain`, `automatic`, `choose`, `rate` из React в проверяемые переходы состояния. Описать ownership scope и acknowledged effects.
- **Выход:** state-transition tests на chain/focused/schedule, переключение вкладок, reveal, typed freeze, повторную оценку, async save failure, stale completion и закрытие owner.
- **Приёмка:** одна оценка даёт ровно один review; старое сохранение не перезаписывает новое; navigation/reference не генерируют другую карточку. Пересоздание UI не повторяет download/reset/focus intent и не заводит второй timer. Durable progress восстанавливается после reload.

### Этап 9. Перенести экран тренировки и общий дизайн

- [ ] **Зависимости:** этап 8.
- **Действия:** тема, компоненты, header/nav, режимы, front/back, изменения слов, accepted, formula/details, оценки/interval preview, цепочка и завершение, context reference. Stateless UI поверх store. Анимации короткие, отменяемые, не задерживают ввод; reduced motion отключает необязательное движение. Не раскрывать эталон во время flip/transition раньше действия reveal.
- **Выход:** рабочий полный training flow и host/Compose проверки P02–P07/P09.
- **Приёмка:** keyboard/focus/IME, 320 px и desktop, семантика карточки и announcements проверены; длинные русские/польские строки не обрезаются. Анимация не запускает review и не блокирует кнопку после interruption/recomposition. В snapshot сравнениях анимации и время контролируются.

### Этап 10. Перенести все таблицы, карту и экран прогресса

- [ ] **Зависимости:** этап 9.
- **Действия:** `GrammarScreen`, `CaseReference`, `ProgressScreen`; перенести все разделы и селекторы из `GrammarTables.tsx`, drill callbacks, stats, export/reset UI и errors.
- **Выход:** доступные таблицы/эквивалентная семантическая структура и полная навигация, P08/P09/P11.
- **Приёмка:** проверить каждую строку матрицы P08, включая comparison table, девять субъектов и aspect section; число слов/форм не уменьшено. Горизонтальная прокрутка не мешает вертикальному чтению; screen reader понимает заголовки и значения. Переходы из таблиц сохраняют предусмотренный seed и training mode. Импорт UI не объявляется «утраченным функционалом».

### Этап 11. Сравнение React ↔ Kotlin web — снят

Отменён решением пользователя 2026-09-25: переход на Kotlin уже выполнен, отдельная React ↔ Kotlin parity-приёмка и WEB PARITY GATE не требуются. Номер этапа сохранён для ссылок.

### Этап 12. Подготовить переключение веб-публикации

- [ ] **Зависимости:** этап 10; пользователь поручил соответствующий scope публикации.
- **Действия:** собрать конкретный проверенный web artifact, проверить GitHub Pages base path/assets/MIME/cache и direct reload, URL возврата на старую сборку и migration entrypoint. Подготовить изменение `.github/workflows/deploy.yml`, release notes, backup/rollback runbook; сохранять React source и воспроизводимый старый artifact.
- **Выход:** reviewable cutover diff и rehearsal на preview/staging. Сам deploy/merge не следует автоматически из наличия этого плана.
- **Приёмка:** отсутствие 404/loading loop, один progress writer, backup перед миграцией, совместимый export текущего состояния, проверенный возврат на React. Статус публикации остаётся отдельным от native фаз.

### Этап 13. Подключить Android shell и адаптеры

- [ ] **Зависимости:** этап 10; решение этапа 12 зафиксировано как executed/deferred. Общий API соответствует web fixtures.
- **Действия:** добавить Android targets и `kotlin/androidApp`, применить совместимые актуальные Android/KMP Gradle plugins; определить min/target SDK по toolchain и текущим требованиям. Подключить общий domain/session contract к отдельному нативному Jetpack Compose Material 3 UI, lifecycle owner, storage/clock/normalizer, native export/share и confirmation. Browser LocalStorage автоматически на Android не переносится: перенос через совместимый файл — отдельный явный пользовательский flow.
- **Выход:** debug build и запуск на emulator/device, Android adapter integration tests, команды SDK/JDK в `kotlin/README.md`.
- **Приёмка:** P01–P11 на Android adapters, сохранение после process death, lifecycle cancellation, configuration change и фон/возврат; нет потери typed answer в пределах обещанного session lifecycle. Без реального запуска писать «Android build подготовлен», не «Android поддерживается».

### Этап 14. Завершить Android layout, ввод, доступность и анимации

- [ ] **Зависимости:** этап 13.
- **Действия:** адаптация compact/expanded windows, edge-to-edge/insets, IME, системный Back, поворот/multi-window; TalkBack, font scale, touch targets, reduced/disabled animations; measurement slow frames на representative device. Общие motion primitives сохраняются, platform policy подключается через адаптер.
- **Выход:** Android UI/device evidence и платформенная parity matrix.
- **Приёмка — ANDROID GATE:** карточки/таблицы доступны при клавиатуре и крупном шрифте; Back не теряет подтверждённый прогресс; rotate/resume не повторяют rate/effects; TalkBack позволяет завершить полный цикл; motion settings соблюдены. Store publication/signing — отдельный scope.

### Этап 15. Подключить iOS shell и адаптеры

- [ ] **Зависимости:** этап 14, общий core прошёл web/Android gates.
- **Действия:** добавить `iosArm64`, подходящий simulator target и `kotlin/iosApp`; Xcode shell размещает нативный SwiftUI interface поверх общего Kotlin domain/session contract, владеет жизненным циклом и platform effects. Спроектировать узкий Swift-facing bridge для immutable state и действий; проверить совместимость Kotlin/Native/Xcode и интеграцию выбранным поддерживаемым способом без одновременного внедрения нескольких package managers. Реализовать native persistence, локальное время/NFC, document export/share; lifecycle и cancellation на Kotlin/Swift границе описать явно.
- **Выход:** simulator build/run, iOS adapter tests и документированные реальные build steps; Swift interop API узкий, без случайного экспорта внутреннего mutable state.
- **Приёмка:** legacy-compatible fixtures и P01–P11 проходят с iOS adapters, reload/background/foreground не теряют сохранённые reviews, Unicode и даты совпадают. Данные Safari и native app не объявлять общим хранилищем. Simulator PASS не равен device PASS.

### Этап 16. Завершить iOS layout, ввод, доступность и анимации

- [ ] **Зависимости:** этап 15.
- **Действия:** safe areas, keyboard avoidance, compact/large screens, landscape, Dynamic Type, VoiceOver, аппаратная клавиатура, selection/copy; Reduce Motion и interruption жестом/сменой экрана, плавность на устройстве. Учитывать iOS navigation/lifecycle; не накладывать Android-only interaction conventions без проверки.
- **Выход:** iPhone/iPad matrix согласно заявленному support scope, physical-device evidence, список проверенных OS/targets.
- **Приёмка — IOS GATE:** полный цикл тренировки/таблиц/export доступен с VoiceOver и крупным текстом; keyboard не закрывает обязательные controls; animation не теряет focus и не запускает действие дважды. При отсутствии физического устройства обозначить незакрытые device критерии; App Store/signing/distribution не объявлять выполненными.

### Этап 17. Свести сопровождение трёх платформ

- [ ] **Зависимости:** web, Android и iOS gates; статус web cutover указан отдельно.
- **Действия:** обновить README/support matrix/CI по реально поддерживаемым targets, команды и способы восстановления данных. Сохранить одни contract fixtures в общей части, отдельно platform tests. Зафиксировать policy обновления toolchain, schema version и FSRS, включая повторную проверку differential fixtures.
- **Выход:** итоговый review и evidence по каждой платформе, документация запуска и известных ограничений.
- **Приёмка:** каждый заявленный target воспроизводимо собирается и проверен на заявленных hosts; нет скрытых NOT RUN required criteria. Удаление React и старых fixtures рассматривать только отдельной задачей после успешной эксплуатации/rollback window; этот план не поручает удаление.

## 6. Проверка и handoff

Для каждого изменения поведения Developer предъявляет наблюдаемый RED → GREEN и результат необходимых affected checks; расширять набор только при новых изменениях или рисках. Общие фикстуры доказывают доменные контракты; Compose tests — UI semantics там, где они поддержаны; реальные браузеры/устройства — ввод, storage, accessibility и rendering. Один вид доказательств не подменяет остальные.

Tester независимо сопоставляет критерии этапа с актуальными доказательствами, добавляет недостающие регрессионные/приёмочные тесты и запускает отсутствующие или затронутые проверки. Актуальные результаты для неизменённого поведения можно использовать повторно с указанием источника. Tester не исправляет production-код и не подменяет последующее ревью; его тесты после реализации не выдаются за исходный RED разработчика.

Минимальная запись evidence:

```text
Этап / parity IDs:
Revision / changed paths:
Команда + cwd + runtime/toolchain:
RED: observed failure + причина, либо N/A для документации:
GREEN / unit / integration / build / browser / device: PASS | FAIL | NOT RUN:
Fixture inputs / clock / timezone / RNG choices:
Screenshots / traces / exported JSON:
Открытые различия и ограничения:
Tester: acceptance IDs + результаты + воспроизведение дефектов + test diff:
Reviewer: scope + verdict + unresolved blockers:
```

Architect передаёт Developer этот plan, выбранный этап, exact paths/contracts, навыки, исходные сценарии и ограничения. Developer возвращает реализацию этапа, актуальное evidence и необходимые public notes. Координатор передаёт их Tester вместе с критериями, target environments и областью владения тестами. Tester возвращает отчёт и test diff; дефекты возвращаются разработчику, отсутствующее evidence остаётся незакрытым. Reviewer получает итоговый actual diff относительно зафиксированного baseline, критерии этапа и отчёты Developer/Tester; проверяет договорённый scope, сохранность данных и полноту доказательств. Координатор обновляет статус только после устранения блокеров.

## 7. Проверенные официальные источники и точки пересмотра

Источники проверены 2026-09-23. При начале toolchain/native этапов перепроверить текущую совместимость; примеры версий из документации не копировать как автоматически подходящие зависимости.

- [Статус KMP и Compose по платформам](https://kotlinlang.org/docs/multiplatform/supported-platforms.html): Android/iOS стабильны; Kotlin/Wasm и Compose web — Beta, Kotlin/JS — Stable. Это причина раннего web spike, а не доказательство конкретной доступности продукта.
- [Kotlin for web](https://kotlinlang.org/docs/web-overview.html): Compose/Wasm для общего UI, compatibility mode с `js` и `wasmJs`; DOM UI — отдельный вариант архитектуры.
- [Kotlin/Wasm overview](https://kotlinlang.org/docs/wasm-overview.html): требования runtime браузера; поддержку проверяем на конкретной distribution.
- [Compose compatibility and versions](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html): согласование Kotlin/Compose/Gradle и платформенных инструментов.
- [Multiplatform ViewModel](https://kotlinlang.org/docs/multiplatform/compose-viewmodel.html): общий state holder допускается, lifecycle ownership и зависимости нужно выбрать для фактического host.
- [Multiplatform Gradle DSL](https://kotlinlang.org/docs/multiplatform/multiplatform-dsl-reference.html): targets/source sets и Gradle-конфигурация; реальные tasks подтверждаются созданным проектом.

Нерешённые до spike вопросы: точная toolchain и browser minimums; доступность выбранного Compose UI тестового API на web; приемлемость Safari/IME/semantics; FSRS implementation с подтверждённой parity; общий normalizer или платформенные реализации. Это последовательные инженерные решения этапов 2–6, а не повод заранее считать миграцию невозможной или безусловно безопасной.
