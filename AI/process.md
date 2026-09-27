# Процесс работы

## Роли

Architect → Developer → Tester → Reviewer ([workflow](../.claude/skills/workflow/SKILL.md)). Агенты Claude — `.claude/agents/`, Codex — `.codex/agents/`; навыки общие.

## Правила

- **Маленькие задачи.** Одна функция × одна платформа на разработчика; короткое ревью; пользователь видит результат сразу.
- **Бережливая проверка.** Только затронутые проверки. Не перепроверять то, что работало. Tester и полная регрессия — для рискованных изменений и в конце.
- **Без предрелизного укрепления** (миграции данных, durability), пока приложение в разработке.
- **Параллельно — только в отдельных worktree** (`../polski-lanes/<host>`, ветка `lane-<host>`): у каждого свой build и DerivedData; в одном дереве Gradle не запускать одновременно. Каждый поток пишет журнал в свой файл, после — слияние и полное тестирование. `git stash` не использовать: stash общий для всех worktree — для RED-проверки временно откатывай правку вручную или делай WIP-коммит.
- Коммит, push, публикация — отдельное решение, не следуют автоматически из ревью.

## Окружение

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21 -a arm64)   # x86-JDK ломает Kotlin/Native
export ANDROID_HOME=$HOME/Library/Android/sdk
```

| Цель | Команда |
| --- | --- |
| Shared-тесты | `./gradlew :shared:jsBrowserTest :shared:wasmJsBrowserTest :shared:desktopTest :shared:iosSimulatorArm64Test :shared:macosArm64Test` (из `kotlin/`) |
| Web | `./gradlew :composeApp:composeCompatibilityBrowserDistribution`, затем `KOTLIN_SPIKE_BRANCH=wasm\|js npx playwright test --config=playwright.kotlin.config.ts` |
| Android | `./gradlew :androidApp:testDebugUnitTest :androidApp:assembleDebug`; эмулятор `Polski_ARM35` (`-gpu swiftshader_indirect`), проверка через `adb` + `uiautomator dump` |
| iOS | `arch -arm64 ruby kotlin/iosApp/generate_project.rb`; `xcodebuild … -scheme PolskiGrammar -destination 'platform=iOS Simulator,id=<id>' test` |
| macOS | `xcodebuild -project kotlin/macosApp/PolskiGrammarMac.xcodeproj -scheme PolskiGrammarMac -destination 'platform=macOS' build` |
| Контент курса | `npm run course:validate`, `course:inventory:check`, `course:inventory:decisions:check` |

Подробно: [kotlin/README.md](../kotlin/README.md).
