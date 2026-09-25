# Возврат к оформлению до экспериментов со стеклом

## Решение от 25 сентября 2026

Точка отсечения — состояние перед первым custom glass patch в 11:13 Europe/Warsaw. Вернуть визуальную иерархию уже готовой UX2/нативной версии: браузерные карточки, цветовые и текстовые акценты правил и форм, верхнюю навигацию; Material 3-карточки Android; акцентную учебную карточку и стандартные элементы SwiftUI на iPhone/iPad и Mac. Предыдущий [план возврата только к системным поверхностям](NativeUIRollback.md) и [эксперименты с оптикой](LiquidGlassRollout.md) — история, не действующее требование к внешнему виду.

Откат касается только представления. Сохранить добавленные ранее и позднее данные и поведение: польский курс, FSRS, прогресс и миграцию, словарь и редактор, две оценки и свайпы, переключение методики, сопоставление «было → стало», выделение окончаний, настройки светлой/тёмной темы и движения, импорт/экспорт JSON. Поле `glassTintPercent` остаётся в v2 JSON для совместимости, но не применяется и не показывается. React остаётся эталоном до отдельного parity gate; Kotlin web не публикуется автоматически.

## Контроль восстановления

1. Сравнить изменённые поверхности с архивом до 11:13, не переписывая целые файлы старой версией: нативные настройки и мосты появились позже. Для web доступны `/private/tmp/polski-glass-baseline-web/` и сохранённые UX2-скриншоты в `artifacts/ux2-test/`. Ориентир Android — `artifacts/android/native-training*.png`; iOS — `artifacts/ios/iphone-17-pro.png`.
2. На web вернуть верхнюю навигацию и CSS карточек/правил/ответа/матрицы. На Android вернуть pre-glass Material 3-карточки и корректный контраст в обеих темах. На iOS вернуть акцентную `StudyHero`, оставляя системные `Form`/`TabView`. На Mac вернуть содержательные карточки и различимые исходную, целевую формы и окончания.
3. Проверить видимые красные исходные части, целевые окончания, подписи переходов и жирный акцент правил в тренировке и таблицах после раскрытия ответа. Оценка и сохранение прогресса должны работать как до отката.
4. Собрать web JS/Wasm, Android APK и Apple hosts; запустить соответствующие браузерные/нативные проверки. Сверять скриншоты только через Luna low. Указывать PASS/FAIL/NOT RUN отдельно для сборки, тестов, браузеров и устройств.

Известные архивные UX2-недочёты (например, низкий контраст Android dark и контент у нижней навигации) исправлять адресно, если они мешают чтению; это не основание снова менять весь визуальный язык. Рабочие пользовательские файлы прогресса не стирать. После отката вернуться к первому незавершённому gate в [основном плане](Plan.md).

## Проверка восстановления, 25 сентября 2026

| Поверхность | Наблюдённый результат |
|---|---|
| Web build | **PASS:** свежие JS, Wasm и compatibility production distributions собраны Gradle. `npm run typecheck` — **PASS**; preview открыт локально на `http://127.0.0.1:8765/`. |
| Web browser | **PASS:** JS 15/15 проверок навигации после исправления высоты кнопок и 3/3 сценария 390→320 px; Wasm 45/45 проверок навигации, двух оценок, выделения форм и матрицы в Chromium/Firefox/WebKit. Независимый Tester на финальной Wasm-сборке: 22/22 settings cases в Chromium, включая JSON tint compatibility, темы, возврат фокуса, свайпы, draft и ошибки хранения. Первые падения 6 nav-height и 1 overflow исправлены и перепроверены; не считать их итоговым статусом. Luna low: [dark](artifacts/pre-glass-rollback/web-dark-390.png), [light](artifacts/pre-glass-rollback/web-light-390.png). |
| Android | **PASS:** APK и unit-тесты Gradle, запуск на API 35 emulator, без `AndroidRuntime` ошибок. После адресного исправления Luna low подтвердил [dark contrast](artifacts/pre-glass-rollback/android-dark.png), доступность нижней кнопки и [полную системную строку](artifacts/pre-glass-rollback/android-status-bar.png); `StatusBar`/cutout frame равны 128 px. Реальное устройство и TalkBack — **NOT RUN** в этом откате. |
| iPhone/iPad | **PASS:** адресный Xcode UI test на iPhone 17 Pro и iPad Pro 13-inch (M5) Simulators; Luna low подтвердил восстановленную `StudyHero` и читаемость на [iPhone](artifacts/pre-glass-rollback/iphone.png) и [iPad](artifacts/pre-glass-rollback/ipad.png), iPhone сверил с pre-glass архивом. Физические устройства и VoiceOver — **NOT RUN** в этом откате. |
| Mac | **PASS:** Xcode Debug build, запуск с изолированным `POLSKI_MAC_DATA_DIR`, раскрытие карточки и Luna low review [светлой](artifacts/pre-glass-rollback/mac-light.png)/[тёмной](artifacts/pre-glass-rollback/mac-dark.png) схем. Автоматические Mac UI tests и VoiceOver — **NOT RUN**. |

Эти проверки относятся только к восстановлению UI и не закрывают весь React ↔ Kotlin parity gate или публикацию. Архив до правок сохранён в `/private/tmp/polski-ui-pre-restoration-2026-09-25-1437.tgz` для адресного сравнения; пользовательские progress/vocabulary/preferences файлы не перезаписывались.
