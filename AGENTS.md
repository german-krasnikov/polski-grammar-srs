# Работа агентов в Polski Grammar Matrix

## Текущая задача и границы

Проект пока работает на React/TypeScript/Vite. Целевое направление — Kotlin Multiplatform с общей логикой и интерфейсом каждой платформы: браузерная версия уже имеет локальное превью, macOS Desktop собран локально, Android использует отдельные Jetpack Compose экраны; iPhone/iPad имеют нативное SwiftUI-превью поверх общего Kotlin-сеанса. Последовательность и критерии сохранения функциональности находятся в [Plans/Kotlin/Plan.md](Plans/Kotlin/Plan.md); desktop lane — в [Plans/Kotlin/MacDesktop.md](Plans/Kotlin/MacDesktop.md), Android lane — в [Plans/Kotlin/Android.md](Plans/Kotlin/Android.md), iOS lane — в [Plans/Kotlin/iOS.md](Plans/Kotlin/iOS.md). Наличие кода и локального превью не означает закрытия web parity, device и независимых review gates.

Перед изменениями прочитай относящийся к задаче шаг плана, реальные файлы и актуальную проверку. Продолжай с первого незавершённого шага. Сохраняй польские формы, поведение упражнений, расписание FSRS и старый прогресс; конкретные условия сравнения перечислены в плане. Новые возможности не добавляй под видом обязательного переноса.

## Workflow и роли

Для существенной работы используй [workflow](.claude/skills/workflow/SKILL.md): основной агент координирует Architect → Developer → Tester → Reviewer, исправления возвращаются разработчику. Внутреннее согласование blueprint не требует повторного разрешения пользователя на уже порученную работу. Для небольшого изменения или отдельного ревью используй соответствующий этап.

| Роль | Claude | Codex | Ответственность |
|---|---|---|---|
| Архитектор | [senior-architect](.claude/agents/senior-architect.md) | [senior-architect](.codex/agents/senior-architect.toml) | План, контракты, зависимости и критерии приёмки |
| Разработчик | [senior-developer](.claude/agents/senior-developer.md) | [senior-developer](.codex/agents/senior-developer.toml) | Реализация, поведенческие тесты и проверки |
| Тестировщик | [senior-tester](.claude/agents/senior-tester.md) | [senior-tester](.codex/agents/senior-tester.toml) | Независимые проверки приёмки, регрессионные тесты и воспроизводимые отчёты |
| Ревьюер | [code-reviewer](.claude/agents/code-reviewer.md) | [code-reviewer](.codex/agents/code-reviewer.toml) | Независимое чтение изменений и доказательств; без правок |

Состав выбран пользователем: архитектор, разработчик, тестировщик, ревьюер. Промпты архитектора, разработчика и ревьюера сохранены с точечными изменениями стека и handoff; промпт тестировщика добавлен отдельно. Разработчик сохраняет ответственность за TDD и исправления кода, тестировщик проверяет приёмку и дополняет тесты, ревьюер оценивает итоговый diff и доказательства. Документацию ведёт разработчик или основной агент через `documentation-maintenance`.

Для четырёх агентов **Codex** пользователь выбрал **GPT-6 Sol** (`gpt-6-sol`). Модель указана в каждой конфигурации `.codex/agents/*.toml` и как значение по умолчанию для subagents в [.codex/config.toml](.codex/config.toml). При запуске через инструмент Codex явно указывай эту модель; если полное наследование истории не допускает смену модели, передай выбранный контекст и контракт роли отдельному новому агенту. Не продолжай старую сессию другой модели вместо запуска выбранной. Агенты **Claude** сохраняют прежнюю модель `claude-sonnet-5` в `.claude/agents/*.md`.

Используй конфигурацию роли своего клиента: отдельные TOML-файлы для Codex, Markdown-промпты для Claude. Контракты ролей согласованы; при изменении обязанностей обновляй обе версии, сохраняя отдельные настройки моделей и корректные пути к общим навыкам. Если нужная модель недоступна, сообщи об этом без молчаливой замены. Если независимые агенты недоступны, выполни роли последовательно и явно укажи отсутствие независимого ревью.

## Выбор навыков

Канонические файлы лежат в `.claude/skills/`; `.agents/skills/` содержит относительные ссылки на те же каталоги для Codex. Правь оригинал, не создавай расходящиеся копии. Читай только навыки, относящиеся к текущей задаче.

| Задача | Навык |
|---|---|
| Координация этапов и передача результатов | [workflow](.claude/skills/workflow/SKILL.md) |
| Архитектура KMP, source sets, границы модулей и состояние | [module-architecture](.claude/skills/module-architecture/SKILL.md) |
| Kotlin, типы, Unicode, корутины и переносимость | [kotlin](.claude/skills/kotlin/SKILL.md) |
| Именование, форматирование, KDoc и lint | [code-style](.claude/skills/code-style/SKILL.md) |
| Общий Compose: состояние, вёрстка, доступность, анимации | [compose-multiplatform-ui](.claude/skills/compose-multiplatform-ui/SKILL.md) |
| Браузер: Wasm/JS, хранение, ввод, вёрстка, анимации | [kmp-web](.claude/skills/kmp-web/SKILL.md) |
| macOS Desktop: JVM, окно, файлы, ввод, доступность, упаковка | [kmp-desktop](.claude/skills/kmp-desktop/SKILL.md) |
| Нативный Android: код, lifecycle, вёрстка, анимации | [kmp-android](.claude/skills/kmp-android/SKILL.md) |
| Нативный iOS: код, interop, вёрстка, анимации | [kmp-ios](.claude/skills/kmp-ios/SKILL.md) |
| Поведенческие тесты, TDD и доказательства | [testing-tdd](.claude/skills/testing-tdd/SKILL.md) |
| Браузерные сценарии и сравнение React/Kotlin | [playwright-testing](.claude/skills/playwright-testing/SKILL.md) |
| Изменение документации и примеров | [documentation-maintenance](.claude/skills/documentation-maintenance/SKILL.md) |

При работе с общим UI добавляй только затронутые платформенные навыки. Safari на iPhone относится к `kmp-web`; нативное приложение Mac — к `kmp-desktop`; приложение iOS — к `kmp-ios`. Общие принципы не требуют писать платформенные адаптеры раньше соответствующего шага плана.

## Проверки и результат

- Документы и промпты проверяй на корректность frontmatter, ссылок, маршрутизации и соответствие задаче; не создавай искусственные тесты на их формулировки.
- Для поведения следуй выбранному TDD workflow и фиксируй только реально наблюдавшиеся RED/GREEN. Проверки запускай по изменённому поведению и зависимостям.
- Доступны `npm test`, `npm run typecheck`, `npm run build`, а также реальные Kotlin/Gradle-команды из `kotlin/README.md`. Проверяй наличие задач, не выдавай запланированную команду за выполненную.
- Отдельно сообщай PASS / FAIL / NOT RUN для тестов, компиляции, сборки, браузеров и устройств. jsdom не доказывает работу браузера, а собранный framework не доказывает запуск iOS-приложения.
- Коммит, публикация, переключение production и удаление React-эталона не следуют автоматически из подготовки плана или прохождения ревью.
