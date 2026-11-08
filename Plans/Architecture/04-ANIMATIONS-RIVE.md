# 04. Анимации и Rive: карта, дублирование, план консолидации

Дата: 2026-11-08. Только чтение кода и артефактов; ничего не запускалось (P17: числа ниже взяты
из `Plans/Kotlin/artifacts/flip-rive/*`, NOT RE-RUN). Принципы: `biome-dsl/AI/PRINCIPLES.md`
(P1, P2, P7, P9, P13, P17, P25), `AI/PATTERN_DICTIONARY.md`.

## 0. Вывод

1. Rive не разбросан хаотично (один overlay-файл на хост), но таблица effect -> .riv -> state machine -> trigger записана 4 раза руками (P7).
2. macOS уже дрейфует от iOS: другой `again.riv` (5428 B vs 2249 B), другие триггеры, нет chain-complete.
3. Desktop (Compose JVM) не имеет ни Rive, ни flip: фича отсутствует, а не продублирована.
4. `rive-compose.pack.md` не покрывает задачу polski и имеет 0 потребителей (P13).
5. В данные стоит выносить таблицу эффектов и политику гейтинга; физика анимаций остаётся нативной (P25 S5).

## 1. Карта анимаций по платформам

Пути относительно `kotlin/`. Размеры файлов - `wc -l`.

| Платформа | Rive | Flip / движение | Reduced motion | Тесты |
|---|---|---|---|---|
| Android (Compose) | `composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidRiveOverlay.kt` (120): `RiveAnimationView` x2, `effectSpecs`, `AndroidChainCompleteOverlay`; `rive-android:11.12.1` (`composeApp/build.gradle.kts:122`) | `AndroidFlipCard.kt` (428): `Animatable` + `graphicsLayer{rotationY}`, `tween(500)`; swipe `tween(220)`; hint 140/220/140; reveal `spring`; stagger `tween(220)` | параметр `reduceMotion` -> `snapTo`; `cardEffectToPlay()` в `AndroidFlipCard.kt:78` | `androidApp/src/test/.../AndroidFlipCard*Test.kt`; `tests/perf/android/*.py` |
| Web (Compose JS/Wasm, DOM) | `webMain/kotlin/polski/ui/RiveEffectOverlay.kt` (164) + рукописный `webMain/resources/rive/rive-bridge.js` (216), канал - DOM-атрибуты `data-rive-*` | `CardFlip.kt` (111): CSS `rotateY`, 2 половины по 250 мс, таймеры `setTimeout`; `RouteSlide.kt` (слайд табов) | `data-motion="reduced"`, `data-animations="off"`, `?riveDisabled=1` (`riveGated()`) | `tests/browser/kotlin-flip-card.spec.ts` (243), `flip-rive-perf.spec.ts` (108), `tests/perf/flip-rive-measurements.spec.ts` (252) |
| iOS (SwiftUI) | `iosApp/PolskiGrammar/RiveEffectOverlay.swift` (88): `RiveViewModel` x2, lazy, `RiveChainCompleteOverlay` | `FlashCardView.swift`: `withAnimation(.spring(0.42, 0.86))`, stagger `.delay(i*0.05)`, disclosure `.easeOut(0.25)` | параметр `reduceMotion` | `PolskiGrammarUITests/FlipRivePerfUITests.swift`, `FlipCorrectnessUITests.swift`, `VocabularyFlipUITests.swift` |
| macOS (SwiftUI) | `macosApp/PolskiGrammarMac/RiveEffectOverlay.swift` (55): без chain-complete, другой набор триггеров | `MacFlashCardView.swift`: `.spring(0.5, 0.82)`, `.revealGroup(delay:)` | `reduceMotion` + `onChange` освобождает VM | только ручной perf (`artifacts/flip-rive/macos/README.md`) |
| Desktop (Compose JVM) | НЕТ | НЕТ (grep `flip|Rive|Motion` в `desktopMain` даёт только prefs/settings) | настройка есть, потребителя в UI нет | нет |

Общий код (единственное, что уже в одном месте): `shared/src/commonMain/kotlin/polski/presentation/CardEffect.kt`
(`enum CardEffect {None, Remembered, Again}`, `cardEffectFor(Rating)`) + `CardEffectTest.kt`.
Хорошо: домен решает ЧТО играть, хост решает КАК (AGENTS.md: "Logic returns decisions").

Ассеты `.riv` (11 копий, 4 хоста): `again.riv` x4 (web/android/ios: 2249 B, macOS: 5428 B),
`confetti.riv` x4 (4597 B), `chain-complete.riv` x3 (59088 B; macOS нет). Источника нет:
копии разложены руками по `res/raw`, `resources/rive`, `Rive/`.

## 2. Дублирование

### D1. Таблица эффектов - 4 копии (P7, P1)

| Копия | Где | Содержимое |
|---|---|---|
| Android | `AndroidRiveOverlay.kt` `effectSpecs()` | Remembered = confetti `Trigger explosion` + again `Check`; Again = again `Error` |
| Web | `rive-bridge.js` `RATING_EFFECTS` | то же |
| iOS | `RiveEffectOverlay.swift` `switch event.name` | то же |
| macOS | `RiveEffectOverlay.swift` | Remembered = только confetti; Again = `Swipe to delete`/`Trigger Delete` (СТАРЫЙ asset) |

Комментарии в коде прямо ссылаются друг на друга ("mirroring iOS", "same pairing the web
bridge uses") - это ручная синхронизация, то есть признак отсутствующего источника правды.
macOS - живое доказательство, что она не держится. Расхождение с `RiveCatalog.md` §0.1:
старый `again.riv` рисует непрозрачную сцену поверх карточки; на macOS этот дефект,
по всей видимости, остался (проверить запуском - NOT RUN).

### D2. Политика гейтинга - 3 реализации (P7)

Правило: "не играть, если reduce motion / Animations off / measurement-variant".
- Android: `cardEffectToPlay(rating, reduceMotion, riveDisabledForMeasurement)` (чистая функция, юнит-тесты есть).
- Web: `riveGated()` читает DOM-атрибуты и `location.search` (не тестируется чистой функцией).
- iOS/macOS: `guard !reduceMotion` внутри `onChange`, measurement-флаг - `#if DEBUG` + `@AppStorage`.
Четвёртое состояние (Animations toggle) на Android/iOS/macOS склеено с `reduceMotion` в вызывающем
коде, на web - отдельный атрибут. Семантика одна, три формы.

### D3. Тайминги flip/reveal - расхождение без фиксации (P7)

web 2x250 мс (+ `flipDebugScale`), Android `tween(500)`, iOS spring 0.42/0.86, macOS spring 0.5/0.82.
Часть различий намеренная (физика платформы), но числа нигде не объявлены как "общий контракт
+ допустимое отклонение"; `FlipCardRivePlan.md` §12.1 описывает их прозой.

### D4. Дебаг-хуки и трассировка - разные механизмы на хост
`?riveDisabled=1`, `RiveMeasurementVariant`, `@AppStorage`, `Trace` vs `os_signpost`: физика хоста, оставить
нативным; но метрики первого эффекта несопоставимы (Android меряет "конструктор -> fireState", не первый кадр; честно оговорено в KDoc).

## 3. Качество интеграции Rive

Плюсы:
- Есть изоляция по хосту: один файл = один адаптер; `CardEffect` в shared; Rive не течёт в
  `core-*`/`shared` (границы слоёв соблюдены, P9).
- Декоративность выдержана везде: `touchPassThrough`/`allowsHitTesting(false)`/`aria-hidden` +
  `hideFromAccessibility`. Жесты не перехватываются.
- Lazy-стратегия обоснована замером: iOS создаёт VM лениво после регрессии XCUITest (потеря
  символов при печати); web грузит `rive.js`/`rive.wasm` лениво + `requestIdleCallback` prewarm;
  при gated-режиме сеть не трогается вообще (контракт C).
- Утечки проверялись: Android 15 циклов PSS ~98-103 МБ без роста; web mem плоская (39.6 МБ js).

Минусы:
- **Нет одного источника** для effect-таблицы и `.riv` (D1). Самое слабое место.
- **macOS drift** (D1) - дефект корректности, не стиля.
- **Android не освобождает views**: `DisposableEffect(effect){ ...; onDispose {} }` пуст,
  `RiveAnimationView` живут в `remember` до ухода композиции. Гипотеза (проверить leak-check на
  повторном mount/unmount экрана, а не на 15 оценках одного экрана): утечка нативного
  ресурса при частой навигации. Контраст: macOS освобождает VM при `reduceMotion`, web делает
  `disposeAllRive`.
- Пакет `rive-android` помечает state-machine inputs как deprecated в пользу data binding
  (`RiveCatalog.md` §0.3): все 4 хоста используют именно inputs -> миграция коснётся 4 мест
  одновременно (усиливает D1).
- web = `rive-bridge.js` + DOM-атрибуты: сознательный выбор (одинаков для JS и Wasm), но это
  нетипизированный канал и единственный рукописный JS на web-стороне Rive.
- Лицензия: `confetti.riv` - CC BY 4.0 (Marketplace #1456), нужен credit; проверить, что
  `THIRD_PARTY/credits.md` обновлён (в `RiveCatalog.md` это было action item).

## 4. Что даёт `rive-compose.pack.md` vs что делает polski

Пак (`implementations/typescript/packages/libs/kt/packs/rive-compose.pack.md`, 80 строк):
`provides: module, on, tween`, `owner: coroutineScope`. Статус в `docs/product/support.md:134`:
**experimental**. KT не имеет FLOW-принтера: пак исключён из golden-тестов
(`tests/flow/stack-golden-platform.test.ts`, коммент: "rive-compose is excluded").
Потребителей в продуктах нет (grep по репо: только сам пак, support.md, тест-коммент).

| Нужно polski | Пак |
|---|---|
| fire trigger (`fireState`, `triggerInput`) | НЕТ: есть `on` (подписка на `deps.riveView.{{event}}.collect`) и `tween` по `getNumberInput` |
| 2 ресурса на один эффект (confetti + again одновременно) | НЕТ: один `deps.riveResource` в `module` |
| artboard + animationName (chain-complete `Tada/Reveal`) | НЕТ |
| lazy-создание view | НЕТ: `setRiveResource` сразу в `mount` |
| освобождение при отключении Animations | частично: `dispose` + `disposers`, но без триггера "gated" |
| `Rive.init(context)` один раз на процесс | НЕТ |
| Compose `AndroidView` вставка | НЕТ: пак про Android View, не про CMP/Apple/web |
| покрытие iOS/macOS/web | НЕТ: `rive-web` (ts) и `rive-unity` (cs) есть, Swift пака нет |

Вывод: пак моделирует "mount + события + твин числового input" (игровой сценарий Unity), а
polski - "one-shot cue по решению домена". Совпадения мало; подгонять пак под polski =
добавлять конструкции без второго потребителя (P13), а Py/Kt/C# хост без названного
потребителя - NOT RUN, не PASS (P2).

## 5. Производительность (по артефактам, NOT RE-RUN)

Источник: `Plans/Kotlin/artifacts/flip-rive/`. Варианты: A (без анимации), B (flip без Rive),
C (flip + Rive).

**Web** (`web/summary.csv`, Chromium, js и wasm):
- flip p50/p95/p99 = 16.7 / ~16.73 / ~16.8 мс, janky 0%, dropped 0% во всех A/B/C. Rive не
  ухудшает кадры в этом протоколе. ОГРАНИЧЕНИЕ: v1 протокол без CPU throttle - плоские 16.7
  честно объяснены в плане §13 (rAF-bound); числа при 4x/6x не сведены в таблицу (план §13, строка 659).
- Первый эффект: 157.2 мс (js) / 160.5 мс (wasm) latency.
- Размер: initial load +1669 B gzip; lazy: `rive.js` 95 КБ + `rive.wasm` 361 КБ gzip + `.riv`
  2.7+2.2 КБ + bridge 1.5 КБ. Платится только при включённых анимациях.
- Longtasks 1-3 шт., max 63-190 мс - в основном старт приложения, от варианта слабо зависят.
- Реальная проблема вне Rive: tab-слайд на web, p95 42-58 мс при 4x CPU (§19.7 плана) -
  это CSS/Compose-рендер, не Rive.

**Android** (`android/variant-*.json`, эмулятор `swiftshader_indirect`):
- A: janky 58.6%, p50 57 / p95 89 мс; C: janky 49.0%, p50 46 / p95 81 мс. Абсолют НЕ
  репрезентативен (software GPU, README это оговаривает), и C "лучше" A - шум.
  Вывод "Rive бесплатен на Android" НЕ доказан; доказано "утечек нет на 15 циклах".
- B: тогда не измерялся (нет способа выставить JVM property). С тех пор добавлен
  `RiveMeasurementVariant` - повторный замер B не найден в артефактах (NOT RUN).
- Размер APK: `rive-android` ожидаемо +~2.4 МБ/ABI (`RiveResearch.md` §5); точной дельты нет.

**iOS / macOS**: `RiveRuntime.framework` = +9.7 МБ (iOS sim Debug) / +9.6 МБ (macOS Debug,
без thinning - верхняя оценка). Время кадров: `perf-report.json` в `ios/`, `xctrace` на macOS
не прогонялся (ручной протокол). iOS: eager-создание VM давало реальную регрессию печати -
лечение лениво (замер есть в `NOTES.txt`).

Итог: единственный надёжный perf-вывод - web (A=B=C по кадрам). Для мобильных нужен повтор
на реальном устройстве; до этого "Rive не вредит" там - гипотеза.

## 6. План консолидации

Правило отбора - P25 (решает ли это; есть ли выгода V1-V5; нет ли S1-S6) и P13
(≥2 потребителей = 4 хоста уже есть; `Consumer: polski-grammar-srs@<sha>` в трейлер).

### Шаг 0. Починить факт, не слой (без DSL)
1. macOS: заменить `again.riv` на 2249-байтовую версию, перейти на `Check`/`Error`, добавить
   `RiveChainCompleteOverlay` или явно записать "macOS: нет chain" в плане. Тест: перенести
   тест D1-соответствия (шаг 2) на macOS.
2. Android: `onDispose { views.forEach { it.stop() } }` (точное API rive-android проверить в
   исходниках 11.12.1, не из памяти) + leak-check на mount/unmount экрана (расширить
   `tests/perf/android/leak_check.py`).
3. Credits: проверить `THIRD_PARTY/credits.md` на CC BY строку по `confetti.riv` (P17).

### Шаг 1. Один источник правды для эффектов (P7, P1, P25 V1+V2)
Читаемый line DSL (не JSON, memory: no authored JSON), например `dsl/effects/card-effects.effects`:

```
EFFECT Remembered
  PLAY confetti  state-machine "State Machine 1" trigger "Trigger explosion"
  PLAY again     state-machine "State Machine 1" trigger "Check"
EFFECT Again
  PLAY again     state-machine "State Machine 1" trigger "Error"
CHAIN-COMPLETE chain-complete artboard "Tada" animation "Reveal"
GATE skip-when reducedMotion | animationsOff | measurementDisabled
```

Генерация (committed, banner, `--check`; P16): `CardEffectSpecs.kt` (commonMain, читается
Android), `RiveEffects.swift` (iOS + macOS общим файлом), `rive-effects.js` (константа для
`rive-bridge.js`). Рукописный остаток на хост - только вызов нативного API
(`fireState` / `triggerInput` / `rive.StateMachine`).
Путь реализации: сначала `derive`-скрипт как у `derive-swipe-policy.mjs` (в polski уже есть
паттерн `dsl/derive-*.mjs`), не новая конструкция DSL (AGENTS.md: "compare against a small
native helper"). Если derive-скрипт короче DSL-версии при тех же гарантиях - остаться на нём
(P25 S1).
Выигрыш: V1 места правки 4 -> 1; V2 копии 4 -> 0; V4 гарантия "macOS не может разойтись".

### Шаг 2. Политика гейтинга как решение (P25 п.1, P20)
Расширить `cardEffectToPlay` до единой чистой функции в `shared/commonMain`:
`RiveGate.decide(reduceMotion, animationsEnabled, measurementDisabled): Boolean`,
с одним набором тестов в `commonTest` (таблица 2x2x2). Android вызывает напрямую;
iOS/macOS - через `shared` (уже линкуются через `IosSession`/`MacSession`); web - через
Compose-код, который выставляет атрибут. Тест паритета на web
(`tests/browser/kotlin-flip-card.spec.ts`) оставить как независимое ожидание (P15).
Это "логика возвращает решение, платформа исполняет".

### Шаг 3. Ассеты из одного места (P7)
`assets/rive/{confetti,again,chain-complete}.riv` + скрипт `sync-rive-assets` с `--check`
(хэши; копии в `res/raw`, `resources/rive`, `Rive/` - генерат по P16). `chain_complete` на
Android (подчёркивание) vs `chain-complete` - имя фиксирует таблица в шаге 1. Размер
`.riv` не в git-диффах гейтов не ослабляет (AGENTS: не добавлять ignore для генерата).

### Шаг 4. Desktop (решение владельца, не техдолг)
Либо явно "Desktop: no Rive, no flip" в `docs/` (P17: отсутствие записано), либо реализовать
через `rive-cmp`/Skia. `RiveResearch.md` §1 (Rive-CMP) - оценить зрелость до решения.
Не начинать без названного продукта-потребителя (P13).

### Шаг 5. Контракт таймингов
`FLIP_HALF_MS = 250` в таблице шага 1 как ориентир с явным допуском; тесты уже есть на всех хостах
(`flipDebugScale`, `AndroidFlipCardGestureTimingTest`, `POLSKI_FLIP_DEBUG_SCALE`). Делает D3 осознанным.

## 7. Где помогает Rive DSL-пак, что остаётся нативным

| Часть | Куда | Основание |
|---|---|---|
| Таблица effect -> (file, SM, trigger), chain cue, gate | DSL/derive | P1, P7, P25 V1/V2/V4; решает, не исполняет |
| Единый `RiveGate.decide` | Kotlin shared (чистая функция) | P25 п.1; уже есть образец `cardEffectToPlay` |
| Синхронизация `.riv` | скрипт `--check` | P7, P16 |
| `rive-compose.pack.md` для polski | НЕ использовать сейчас | P13: 0 потребителей, mismatch с задачей (§4); KT FLOW-принтера нет |
| Compose `Animatable`/`graphicsLayer`, SwiftUI `withAnimation`, CSS `rotateY`, spring-константы | нативный остаток | P25 S5 (физика платформы); значения - в таблице-ориентире, код - нативный |
| `rive-bridge.js` (216 строк), DOM-атрибуты | нативный остаток | уже "минимальный JS-мост"; генерировать только константы |
| Lazy-создание / prewarm / dispose-при-выключении | нативный остаток, паттерн один | P25 S2/S5; общий рецепт можно описать в `PATTERN_DICTIONARY`, не пакетом |
| `RiveMeasurementVariant` и debug-флаги | нативный остаток | измерительный хук хоста, не домен |

Условие для пака: когда второй продукт (Unity-игра с `rive-unity`, не polski) и polski
оба потребуют `fire`-триггера - добавить `fire` в `provides` обоих паков с golden-тестом и
записью `Consumer: <product>@<sha>` (P13). До тех пор честная альтернатива - derive-скрипт из
шага 1, он ничего не добавляет в словарь конструкций.

## 8. Порядок и критерий готовности

Шаг 0 -> 2 -> 1+3 -> (4, 5 по решению владельца). Готово (P17), когда: таблица эффектов читается
из одного файла на 4 хостах; `--check` падает при ручной правке генерата; iOS и macOS играют
одинаковые триггеры (UI-тест на обоих); gate покрыт в `commonTest`; perf на реальном устройстве
записан отдельным артефактом. Проверки: `xcodebuild` macOS, `:androidApp:testDebugUnitTest`,
`leak_check.py`, `biome check changed` - все NOT RUN до выполнения.

## 9. Открытые вопросы

1. macOS: старый `again.riv` намеренный или забытый?
2. Нужен ли Rive на Desktop вообще?
3. Миграция inputs -> data binding: заранее (одна правка после шага 1) или по факту удаления inputs?
4. Perf на реальном Android/iOS-устройстве не собран; "Rive не влияет на кадры" доказано только для web.
