# Polski Grammar Matrix

Прочитай [AGENTS.md](AGENTS.md): там описаны текущий стек, выбор локальных навыков, роли и правила проверки.

План перехода на Kotlin Multiplatform: [Plans/Kotlin/Plan.md](Plans/Kotlin/Plan.md). Сначала сохраняем функциональность веб-версии; Нативные Android и iOS preview разрабатываются параллельно, но web parity gate остаётся открытым; статус iOS — в [Plans/Kotlin/iOS.md](Plans/Kotlin/iOS.md).

Используй настроенные роли из `.claude/agents/` и workflow из `.claude/skills/workflow/SKILL.md`. Загружай только нужные навыки из таблицы в `AGENTS.md`; не подменяй их JavaScript/TypeScript-инструкциями из исходного проекта.

Модель агентов Claude остаётся `claude-sonnet-5`. Отдельные конфигурации `.codex/agents/` и выбор GPT-6 Sol относятся только к Codex; навыки общие для обоих клиентов.
