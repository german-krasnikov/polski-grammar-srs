# План: 3D-оборот карточки, единый жест и эффекты Rive

Дата: 2026-09-25. Основание: [BinaryRatingUXPlan.md](BinaryRatingUXPlan.md) (текущий свайп/оценки) и [RiveResearch.md](RiveResearch.md) (runtime и .riv-файлы). Приложение в разработке, реальных пользователей нет — миграция данных не нужна, изменения минимальны и идиоматичны для каждого хоста. Web = семантический DOM/CSS (NOT Compose), Android = Compose M3, iOS/macOS = SwiftUI, JVM Desktop preview — **вне охвата** (остаётся как есть, эффекты и оборот там не реализуются).

## 0. Контракт

- **Оборот — чисто визуальный.** Он не должен диспетчить `AppAction` и не должен ни разу трогать `CardPhase`/FSRS. Каждый host держит локальный флаг `flipped: Boolean`, отдельный от `AppUiState.phase`. Показ ответа (`AppAction.Reveal`) остаётся единственным способом получить данные ответа; после него `flipped` **автоматически** становится `true` (карточка сама доворачивается лицом к ответу), а дальнейшие тапы по карточке только переключают `flipped` локально. Пока `phase == Question`, тап по карточке не переворачивает — вернуться к вопросу можно только когда `phase == Revealed` (обратной стороны без данных ответа не существует, поэтому доворот до Reveal не определён и не нужен).
- **Один жест на host.** Свайп-оценка (влево=Again/«Повторить», вправо=Good/«Вспомнил») переносится с узкой полосы-подсказки на всю обратную сторону карточки, и работает только при `phase == Revealed`. Кнопки и клавиатура (`Space`/`Enter` — reveal, `1`/`2` — оценка) остаются рабочими без изменений — это уже реализовано и не трогается.
- **Общий контракт в shared — минимальный.** Единственное новое общее API: `enum class CardEffect { None, Remembered, Again }` и чистая функция `fun cardEffectFor(rating: Rating): CardEffect` в **NEW** `shared/src/commonMain/kotlin/polski/presentation/CardEffect.kt`. Она не хранится в `AppUiState` и не эмитится стором — каждый host вызывает её сам в момент диспетча `AppAction.Rate`, чтобы выбрать триггер Rive. Больше общего кода не нужно: визуальный оборот — чисто host-local UI state, не предметная область.
- **Reduced motion.** Существующий `polski.preferences.Motion` (`shared/.../preferences/UserPreferences.kt`) уже управляет анимациями там, где подключён. Оборот и оверлей Rive гасятся при `Motion.Reduced` или системном reduce-motion — конкретика по хостам в шаге 3–4.
- **A11y.** Скрытая сторона карточки не должна быть в дереве accessibility (уже действующее правило для ответа). Оверлей Rive — decorative-only, `aria-hidden`/`accessibilityHidden`/`importantForAccessibility=no`, `pointer-events:none`/`.allowsHitTesting(false)`, никогда не перехватывает тапы (см. RiveResearch §3, issue #55).

## 1. Общий shared-код

**FC-01.** Добавить `shared/src/commonMain/kotlin/polski/presentation/CardEffect.kt` (NEW): `enum class CardEffect { None, Remembered, Again }`, `fun cardEffectFor(rating: Rating) = if (rating == Rating.Good) CardEffect.Remembered else CardEffect.Again`. Юнит-тест в `shared/src/commonTest/kotlin/polski/presentation/CardEffectTest.kt` (NEW): Again→Again, Good→Remembered. Никаких Rive-типов в shared (см. RiveResearch §1 — Rive-CMP не годится ни для web DOM, ни для SwiftUI хостов).

## 2. Единый жест: relocate свайпа на всю обратную сторону

Сейчас свайп подключён к узкой полосе-подсказке под кнопками на трёх хостах, на macOS его нет вовсе. Переносим на весь back-face, оставляя ровно один detector на host.

**FC-02 (web).** В `composeApp/src/webMain/kotlin/polski/ui/TrainingWebApp.kt`: `renderAnswerBack` продолжает строить `div.card-back`, но `installTouchSwipeRating(...)` (из `WebSwipeRating.kt`) вызывается на самом `back`-элементе, а не на отдельной `vocabulary-swipe-zone` подсказке; подсказка-текст («← Повторить · Вспомнил →») остаётся как визуальная метка направления (`aria-hidden`), не как зона жеста.
**FC-03 (web, mouse+touch).** `WebSwipeRating.kt`: убрать проверку `pointer:coarse`/`isTouchPointer` как единственный допуск — принимать **primary pointer любого типа** (`mouse` и `touch`/`pen`), сохранив: пороги (75px, преобладание по X ×1.25), `pointerId`-совпадение start/up, `setPointerCapture`. Для мыши добавить отмену на `Escape`/потерю фокуса не требуется — `pointercancel`/`lostpointercapture` уже покрывают. Не реагировать, если `document.activeElement` — `textarea`/`input` в момент `pointerdown` (защита пункта про typed-режим, см. FC-08).
**FC-04 (Android).** `composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidTrainingScreen.kt`: `detectHorizontalDragGestures` из `AndroidRatingActions` переносится на модификатор всей back-face карточки (см. FC-10 — новый `AndroidCardBack`), кнопки `AndroidRatingActions` остаются внутри неё как есть, но перестают сами держать gesture-модификатор.
**FC-05 (iOS).** `iosApp/PolskiGrammar/PolskiGrammarApp.swift`: `.simultaneousGesture(DragGesture(minimumDistance: 18)...)` (строка ~710) переносится с `VStack` «Когда повторить» на весь контейнер back-face внутри **NEW** `FlashCardView` (см. FC-11); `ratingSwipeArea` accessibility-identifier остаётся на текстовой подсказке.
**FC-06 (macOS, новое).** В `macosApp/PolskiGrammarMac/PolskiGrammarMacApp.swift` свайпа сейчас нет вообще (только кнопки и `⌘1`/`⌘2`). Добавить тот же `DragGesture(minimumDistance: 18)` с тем же порогом (80/×1.5, как на iOS) на back-face внутри **NEW** `MacFlashCardView` (FC-12), диспетчащий `model.send("rate", "\(exercise.id)|Again"/"Good")` — тот же формат, что уже используют кнопки.
**FC-07.** Приёмка: на каждом host короткий/вертикальный/до-reveal свайп не оценивает; ровно один свайп = ровно одна оценка (домен и так это гарантирует — `TrainingStore.rate` игнорирует `action.exerciseId`, отличный от текущей карточки, см. `TrainingStore.kt:301`); кнопки и `1`/`2`/`⌘1`/`⌘2` продолжают работать.

## 3. Оборот карточки (native flip)

**FC-08 (web).** Перестроить `renderCard`: и `card-front`, и `card-back` монтируются в DOM одновременно внутри новой обёртки `div.card-flip > div.card-flip-inner > (card-front, card-back)`. CSS (`training.css`, дополнить рядом с текущими `.card-front`/`.card-back` на строках 34–40): `.card-flip{perspective:1400px}`, `.card-flip-inner{position:relative;transform-style:preserve-3d;transition:transform .5s}`, `.card-flip-inner.flipped{transform:rotateY(180deg)}`, `.card-face{position:absolute;inset:0;backface-visibility:hidden}`, `.card-back{transform:rotateY(180deg)}`. Скрытая сторона получает `aria-hidden="true"` и `inert` (или `tabindex="-1"` на её интерактивных потомках), пока не активна. Клик по `.card-flip` (кроме кликов по `textarea#training-answer`, кнопкам и `select`) переключает класс `flipped`, **не вызывая `render()`** — это прямая DOM-мутация вне цикла стора. **Важно (архитектурный риск):** `render()` в `TrainingWebApp` полностью перестраивает `content` (`content.textContent = ""`, строка 261) при каждом изменении состояния (в т.ч. по таймеру `RefreshTime`), а не только по `draft`. Значит локальный `flipped` и раскрытый класс нужно хранить в поле рендерера (по аналогии с уже существующими `focusId`/`focusedSelection`/`scrollPositions`, строки 237–250) и переносить на новый `.card-flip-inner` после каждой перестройки — иначе доворот "потеряется" на первом же тике таймера. Сброс `flipped=false` при смене `exercise.id` (новая карточка — всегда лицом к вопросу), автоустановка `true` при переходе `Question→Revealed`.
**FC-09 (web, gate).** Reduced motion: класс `flipped` переключается всегда (это состояние, не анимация), но CSS-переход гасится уже существующим глобальным правилом `[data-motion=reduced] *{transition:none!important}` (`training.css:97`, управляется `WebAppearance.kt`) — новых правок для web reduced-motion не требуется, кроме самого `transition` на `.card-flip-inner`.
**FC-10 (Android).** **NEW** `AndroidFlipCard` composable в `AndroidTrainingScreen.kt` (или отдельный `AndroidFlipCard.kt`): владеет `var flipped by remember(exerciseId) { mutableStateOf(false) }`, `LaunchedEffect(state.phase) { if (state.phase == CardPhase.Revealed) flipped = true }`, сброс при смене `exerciseId`. Рендерит `Box` с `graphicsLayer { rotationY = angle; cameraDistance = 12f * density }` через `animateFloatAsState(if (flipped) 180f else 0f, animationSpec = if (reduceMotion) snap() else tween(500))`; при `angle < 90f` показывает существующее содержимое `CardPhase.Question`-ветки (переиспользовать текущий код строк 157–189 без изменений логики), при `angle >= 90f` — содержимое `Revealed`-ветки (строки 190–238), с `graphicsLayer { rotationY = angle - 180f }` на back-face, чтобы текст не отражался зеркально; неактивная сторона получает `Modifier.semantics { invisibleToUser() }`. `onClick`-модификатор на весь `Box` переключает `flipped`, но **не** ставится на `OutlinedTextField` (Compose уже не пробрасывает клик через фокусируемое текстовое поле дочерним `clickable`, но нужно явно исключить typed-текстфилд из `Modifier.clickable` зоны, обернув именно контентную область вокруг него, а не сам `OutlinedTextField`). `reduceMotion` источник — **новая** проверка: `session.preferences.motion == Motion.Reduced` (Android не имеет системного «reduce motion» сигнала в Compose так, как iOS/web; фиксируем это как открытое ограничение, а не гадаем — см. §6/риски).
**FC-11 (iOS).** Экономнее всего — вынести всю `cardContent` (строки 613–729) в **NEW** `FlashCardView: View` (Swift, файл `iosApp/PolskiGrammar/FlashCardView.swift`), принимающую `model`, `state`, `card` (те же `Record`, что и сейчас) и рендерящую фронт (нынешние строки 614–650) и бэк (651–719, включая перенесённый `DragGesture` из FC-05) как два `View` в `ZStack`, с `.rotation3DEffect(.degrees(flipped ? 180 : 0), axis: (x:0,y:1,z:0))` на контейнере и обратным `-180` на back-face (та же анти-зеркальная техника, что на Android). `cardContent` в `PolskiGrammarApp.swift` заменяется на один вызов `FlashCardView(...)`, встроенный в `Form` как единственная строка секции с `.listRowInsets(EdgeInsets())`/`.listRowBackground(Color.clear)` — тот же паттерн, что уже используется для `StudyHero` (строка ~482), чтобы 3D-трансформ не боролся с переработкой строк `List`/`Form`. `flipped` — `@State` внутри `FlashCardView`, синхронизируется `.onChange(of: state.phase)`/`.onChange(of: card.id)` аналогично существующему `.onChange(of: card.string("id"))` (строка 599). Гасится через уже читаемый `@Environment(\.accessibilityReduceMotion) reduceMotion` и `model.preferences.string("motion") == "Reduced"` — та же пара условий, что уже используется в `.animation(...)` на строке 570.
**FC-12 (macOS).** Аналогично — `exerciseCard`/`contentCard` (строки 440–583) выносятся в **NEW** `MacFlashCardView: View` (`macosApp/PolskiGrammarMac/MacFlashCardView.swift`) с тем же `rotation3DEffect`-паттерном и переносом `DragGesture` из FC-06. Гейт — существующие `reduceMotion`(`@Environment`) и `model.preferences?.motion == "Reduced"` (строка 420 уже использует эту пару).
**FC-13 (все хосты, typed-режим).** Пока `phase == Question`, тап где-либо, кроме реального интерактивного поля ввода (`textarea#training-answer` / `OutlinedTextField` / `TextField`) и кнопок, не имеет эффекта оборота (оборот включён только когда `phase == Revealed`, см. §0) — поэтому конфликта «тап в поле = не переворачивает» не возникает по построению: до Reveal card-flip неактивен вовсе. Проверка «Проверить и показать ответ» (`AppAction.Reveal`) на typed-карточке доворачивает карту так же, как оральная — уже покрыто автоустановкой `flipped=true` на `Question→Revealed`.
**FC-14.** Приёмка (все хосты): тап по лицевой стороне карточки после reveal переворачивает к вопросу без диспетча какого-либо `AppAction` и без изменения `phase`/FSRS; повторный тап возвращает к ответу; при этом кнопки оценки и свайп остаются доступны на текущей (видимой) стороне; печатный ответ/фокус в поле не запускает оборот; `Motion.Reduced`/системный reduce-motion — мгновенная смена стороны без transition/`tween`.

## 4. Эффекты Rive (оверлей)

Только оверлей поверх/над карточкой, никогда не влияет на flip/rating. Все API из RiveResearch §2 помечены `@Experimental...`, кроме тех, что легаси — принять как есть.

**FC-15 (вендоринг ассетов).** Скопировать из `/private/tmp/claude-501/riv/`: `confetti.riv` (4.6 KB, MIT, «Remembered») и `ui_swipe_left_to_delete.riv` (5.4 KB, MIT, «Again» — используем только `Trigger Delete`/аналог как «резкий, отличный от confetti» эффект, без функции реального удаления). Место: **NEW** `kotlin/shared/src/commonMain/composeResources/files/rive/{confetti,again}.riv` не годится (shared не должен знать про Rive) — вместо этого копировать per-host: `composeApp/src/androidMain/res/raw/{confetti,again}.riv`, `composeApp/src/webMain/resources/rive/{confetti,again}.riv` (+ `rive.wasm` из `@rive-app/canvas-lite`), `iosApp/PolskiGrammar/Rive/{confetti,again}.riv` (added to Xcode bundle resources), `macosApp/PolskiGrammarMac/Rive/{confetti,again}.riv`. **NEW** `THIRD_PARTY/credits.md` (репозиторий, не per-host): для каждого файла — источник URL (rive-ios/rive-android Demo-App/`app/res/raw`), лицензия (MIT для репозитория; отметить, что часть файлов Rive Marketplace = CC BY 4.0, требование credit «Rive, Inc. / rive-app examples»), контрольная сумма/версия коммита.
**FC-16 (Android).** `app.rive:rive-android:11.12.1` (легаси `RiveAnimationView`, т.к. новая Compose-API работает только через data binding, а наши файлы — legacy state-machine inputs, RiveResearch §2 «Inputs vs data binding»). Добавить `implementation("app.rive:rive-android:11.12.1")` в `composeApp/build.gradle.kts` → `androidMain.dependencies`. **NEW** `AndroidRiveOverlay.kt` в `composeApp/src/androidMain/kotlin/polski/ui/`: `AndroidView { RiveAnimationView(context).apply { setRiveResource(R.raw.confetti, stateMachineName = "State Machine 1") } }`, поверх `AndroidFlipCard` (FC-10) в `Box`, `Modifier.matchParentSize().pointerInput(Unit){}.semantics{ invisibleToUser() }` (блокирует хиттест и скрывает от TalkBack — `invisibleToUser()`, не `clearAndSetSemantics`, чтобы не тронуть карточку под ним; альтернатива — вынести оверлей вообще за пределы hit-test дерева через отдельный `Box` без `pointerInput`, но сверху по z-order). Триггер: `LaunchedEffect(cardEffect) { if (cardEffect != CardEffect.None && !reduceMotion) view.fireState("State Machine 1", if (cardEffect==Remembered) "Trigger explosion" else "Trigger Delete") }`, где `cardEffect = cardEffectFor(rating)` вычисляется хостом в момент диспетча `AppAction.Rate` (FC-01) и хранится как одноразовое `mutableState`/событие (сброс после срабатывания, чтобы не повторялся при рекомпозиции).
**FC-17 (iOS/macOS).** `rive-ios` SPM `RiveRuntime` 6.27.0, платформы iOS 14+/macOS 13.1+ — совпадает с текущим deployment target (17.0/см. generate_project.rb). Добавить SPM-зависимость в **оба** `iosApp/generate_project.rb` и `macosApp/generate_project.rb`: через `xcodeproj` gem — `project.root_object.package_references` / `Xcodeproj::Project::Object::XCRemoteSwiftPackageReference` c `repositoryURL: "https://github.com/rive-app/rive-ios"`, `requirement: {kind: 'upToNextMajorVersion', minimumVersion: '6.27.0'}`, и `target.package_product_dependencies << project.new(...).add_swift_package_product("RiveRuntime")` — **[нужна проверка]** точный вызов зависит от установленной версии `xcodeproj` gem; свериться с её README перед правкой (генератор сейчас вообще не добавляет SPM-пакетов, см. `iosApp/generate_project.rb:1-73`). **NEW** `RiveEffectOverlay.swift` (по одному на iOS/macOS): `RiveViewModel(fileName: "confetti", stateMachineName: "State Machine 1").view()` (легаси API — та же причина, что на Android), `.allowsHitTesting(false)`, `.accessibilityHidden(true)`, поверх `FlashCardView`/`MacFlashCardView` в `ZStack`. Триггер: `.onChange(of: cardEffect) { if !reduceMotion { viewModel.triggerInput(effect == .remembered ? "Trigger explosion" : "Trigger Delete") } }`.
**FC-18 (web).** `@rive-app/canvas-lite@2.43.1` — добавить `implementation(npm("@rive-app/canvas-lite", "2.43.1"))` в `composeApp/build.gradle.kts` в **оба** `jsMain.dependencies`/`wasmJsMain.dependencies` (не в общий `webMain` — RiveResearch §2 отмечает риск общих `JsAny`-типизированных externals для JS+Wasm; если общий модуль `PointerInterop.kt`-подобный `expect`/`actual` не даст собрать оба таргета одним внешним объявлением, разнести `RiveSdk.js.kt`/`RiveSdk.wasm.kt` по `jsMain`/`wasmJsMain`, как делает сам Rive-CMP). Скопировать `rive.wasm` из `node_modules/@rive-app/canvas-lite` в `composeApp/src/webMain/resources/rive/rive.wasm`, вызвать `RuntimeLoader.setWasmUrl("rive/rive.wasm")` и `setWasmFallbackUrl(null)` при первом использовании (лениво, при первом reveal — не в `main()`, чтобы 279 KB brotli не блокировали старт). Один `<canvas>`-оверлей на карточку (не на карточку × N), создаётся `TrainingWebApp`-рендерером один раз и переиспользуется между картами (RiveResearch §2 «Web canvas count»); CSS `pointer-events:none` + `aria-hidden="true"` на контейнере. Триггер: `stateMachineInputs("State Machine 1").find{ it.name == "Trigger explosion" }?.fire()` в момент диспетча `AppAction.Rate`, если не `[data-motion=reduced]`.
**FC-19 (десктоп preview).** Никаких изменений в `composeApp/src/desktopMain` — вне охвата, эффекты там отсутствуют (см. RiveResearch §2, JVM-строка — no-op).
**FC-20.** Приёмка: при `Rating.Good` — эффект «Remembered» (confetti-триггер), при `Rating.Again` — отличный от confetti эффект; при `Motion.Reduced` (приложение) или системном reduce-motion — эффект вообще не запускается (не «без анимации», а не создаётся/не инициализируется рантайм на этот показ); оверлей не получает фокус/тап на TalkBack/VoiceOver/Tab и не появляется в DOM/semantics-дереве как интерактивный узел; повторный рейтинг той же карточки невозможен (домен уже это гарантирует), поэтому повторный триггер эффекта на том же `exerciseId` не возникает сам по себе.

## 5. Измерения: три варианта × 4 хоста

Варианты: **A** — без анимаций (`Motion.Reduced`, эффект/переход исключены логикой FC-09/12/20, а не просто CSS `display:none`); **B** — только native flip (Rive-триггеры выключены debug-флагом, ниже); **C** — flip + Rive. Переключение — **NEW** debug-настройка (не пользовательская): `System property`/build-config `polski.debug.riveDisabled=true` (desktop/Android JVM), `#if DEBUG` UI-toggle в Settings на iOS/macOS (скрытый за long-press на версии приложения, как обычно делают debug-флаги), URL query `?riveDisabled=1` на web — все три поверх уже существующего `Motion` (A управляется реальным `Motion.Reduced`, не отдельным флагом, чтобы совпадать с продовым путём гашения).

- **Web** (Playwright chromium, `KOTLIN_SPIKE_BRANCH=wasm|js`): CDP `Tracing.start`/`page.evaluate` c замером `requestAnimationFrame` дельт во время скриптованного тапа+свайпа (варианты A/B/C, обе ветки JS и Wasm), long tasks через `PerformanceObserver('longtask')`, `performance.measureUserAgentSpecificMemory()`/`Performance.getMetrics` (`JSHeapUsedSize`), размер бандла (`gzip`/`brotli` до/после для `dist` js+wasm), время загрузки `rive.wasm` через Resource Timing + `enablePerfMarks: true` (`rive:artboard-draw`, `renderer-flush`). Спецификация: **NEW** `composeApp/src/webTest` (Playwright, `playwright.kotlin.config.ts`) `flip-rive-perf.spec.ts`.
- **Android** (`emulator-5554`, `Polski_ARM35`): `adb shell dumpsys gfxinfo dev.polski.grammarmatrix reset`, скриптованные `adb shell input swipe`/`tap` ×N на каждый вариант, `dumpsys gfxinfo … framestats` (p50/p90/p99, jank%), `dumpsys meminfo` до/после 50 оборотов+оценок, размер APK (`apkanalyzer apk file-size`) до/после добавления `rive-android` (ожидаемо ~+2.4 MB compressed/ABI, RiveResearch §5–6).
- **iOS** (симулятор iPhone 17 Pro / iPad Pro 11 M5, `xcodebuild ... test`): XCTest `measure(metrics: [XCTOSSignpostMetric(...), XCTClockMetric(), XCTMemoryMetric()])` вокруг `os_signpost`-интервала flip+эффект для A/B/C, `XCTApplicationLaunchMetric` на регрессию запуска, App Thinning size report до/после `RiveRuntime`.
- **macOS** (`xcodebuild ... build`, ручной/`xctrace`): `xcrun xctrace record --template 'Animation Hitches' --launch -- <app>` вокруг тех же A/B/C-сценариев (без XCTest UI автоматизации — accessibility-автоматизация оборота через macOS accessibility пока недоступна, см. BinaryRatingUXPlan «Выполненная проверка», строка про Mac окно), плюс сравнение размера `.app` до/после `RiveRuntime.xcframework`.

**Явная оговорка** (во все отчёты): эмулятор/симулятор — ориентировочные цифры, не device-grade (RiveResearch §5 «Test on a low-end physical device», «simulator's Metal path is not representative»).

## 6. Открытые решения и риски

- **iOS Form/List vs 3D-трансформ.** `cardContent` сейчас — это `Section` внутри `Form` (список строк), а не отдельная поверхность; вынос в `FlashCardView` как одна строка `.listRowInsets(EdgeInsets())` (как уже сделано для `StudyHero`) — рабочий, но нетривиальный рефактор, который стоит подтвердить визуально на первом спайке перед полной портировкой всей `cardContent`-логики.
- **Web: полная перестройка `render()`.** Сейчас `content.textContent = ""` на каждый `dispatch`; локальный `flipped` обязан переживать это (см. FC-08) — если это не сделать явно, доворот пропадёт на первом же тике `RefreshTime`. Это единственный по-настоящему новый паттерн для веб-рендерера (раньше похожая проблема решалась только для focus/scroll/IME).
- **Android reduce-motion сигнал.** В Compose нет системного аналога `prefers-reduced-motion`/`accessibilityReduceMotion`; на Android FC-10/16 гасятся только через `Motion.Reduced` (пользовательская настройка приложения), без системного сигнала — зафиксировать как известное ограничение, не выдумывать несуществующий Android API.
- **Legacy vs data-binding Rive API.** Обе бесплатные `.riv` (`confetti.riv`, `ui_swipe_left_to_delete.riv`) — legacy state-machine inputs; новые Compose/Apple API поддерживают только data binding. Поэтому на Android и iOS/macOS сознательно используется легаси API (`RiveAnimationView`/`RiveViewModel`), который Rive заявляет депрекейтить «в будущем» — фиксируем это как временное решение до появления собственного `.riv` с data binding (RiveResearch §6, «Recommended order» шаг 6).
- **`ui_swipe_left_to_delete.riv` как «Again».** Файл сделан для смахивания-удаления, не для оценки; используем только как визуально «резкий, отличный от confetti» эффект (например `Trigger Delete` без реального удаления), без обещания смыслового соответствия — при появлении доступа к редактору Rive заменить на авторский набор `remembered`/`again` (см. RiveResearch §6).
- **xcodeproj SPM API.** Нужно свериться с фактической версией gem `xcodeproj`, установленной в `arch -arm64 ruby`, перед правкой обоих `generate_project.rb` — сейчас там вообще нет SPM-пакетов, точный вызов не подтверждён.
- **Порядок реализации** (снижает риск): 1) FC-01–14 (flip+единый жест на всех хостах, без Rive) → полная проверка на всех целях; 2) web-прототип Rive (FC-15/18) + Playwright rAF-бенч; 3) Android (FC-16) + gfxinfo/Macrobenchmark; 4) iOS/macOS (FC-17) + XCTest signpost; 5) измерения по §5 для всех вариантов A/B/C.

## 7. Проверка (LEAN MODE — только затронутое)

Shared: `CardEffectTest` (FC-01) на JVM/JS/Wasm/Native, где реально гоняются commonTest. Web: `flip-rive-perf.spec.ts` + существующий набор swipe/rating сценариев (Playwright, JS и Wasm, chromium) — не весь ранее пройденный suite, только затронутые card/swipe/reveal сценарии. Android: `androidApp/src/test` для нового `AndroidFlipCard`/жеста, ручная/скриптованная проверка на `emulator-5554` (оборот, свайп на всей карточке, confetti/again-триггер, TalkBack не видит оверлей). iOS/macOS: `xcodebuild ... test` на обоих симуляторах для `FlashCardView`/`MacFlashCardView`, XCTest signpost-замеры по §5. Десктоп preview — не трогается, проверка не требуется. Результаты — раздельно PASS/FAIL/NOT RUN по цели, как принято в [BinaryRatingUXPlan.md](BinaryRatingUXPlan.md#выполненная-проверка-и-открытые-gate); коммит и публикация не следуют автоматически из этого плана.

## 8. Evidence log — web (Developer, реализовано)

Реализовано для web-хоста: FC-01 (shared `CardEffect`/`cardEffectFor`), FC-02/03 (relocate свайпа на весь `card-back`, mouse+touch), FC-08/09/13/14 (native CSS 3D flip, персистентный `flipped` через ре-рендеры, авто-доворот на Reveal, reduced-motion), FC-15/18/20 (Rive-оверлей: vendored assets, `rive-bridge.js`, lazy-load, reduced-motion gate), variant-toggle `?riveDisabled=1` (§5, вариант B) поверх реального `Motion.Reduced` (вариант A). FC-04–07/10–12/16/17/19 — другие хосты, вне охвата этого прохода.

**Отклонения от буквы плана (обоснованные, зафиксированы явно):**
- FC-08's `.card-face{position:absolute;inset:0}` заменён на `display:grid` с оверлеем faces в одной `grid-area` (`.card-flip-inner{display:grid;grid-template-columns:minmax(0,1fr);grid-template-areas:"face"}`) — буквальный `position:absolute` даёт нулевую высоту контейнера (обе стороны вне потока), grid-подход — стандартная надёжная техника flip-карточек без этой проблемы.
- FC-03's guard "не реагировать, если `document.activeElement` — textarea/input" оказался недостаточным: `setPointerCapture` на всём `card-back` перехватывал `click` у вложенных кнопок оценки при pointerdown на дочерний `<small>`/`<span>` внутри кнопки (см. RED ниже). Добавлена дополнительная проверка по `event.target` (не только `activeElement`), и `editableTarget()` переведён с прямого сравнения тэга на `closest(...)`, чтобы дочерние узлы кнопки/ссылки корректно распознавались как интерактивные.
- FC-18's per-host `jsMain`/`wasmJsMain` split для Rive API избежан целиком: вместо типизированных `external`/`js()`-биндингов к Rive JS-объектам оверлей сигнализирует хосту через один DOM-атрибут (`data-rive-effect` на `#polski-rive-overlay`, наблюдаемый `MutationObserver`) — вся реальная работа с Rive (`new rive.Rive(...)`, `stateMachineInputs`, `fire()`) лежит в новом hand-written `rive/rive-bridge.js`, который Kotlin никогда не вызывает напрямую. Это снимает риск §6 "shared JsAny-typed externals" полностью (`RiveEffectOverlay.kt` — один файл в `webMain`, без `jsMain`/`wasmJsMain`-версий).
- Оверлей-канвас не привязан к пересобираемому `.route-content`/карточке — создаётся один раз в `root` (не пересобираемая часть DOM) и позиционируется `position:fixed` над текущей карточкой в момент рейтинга; это надёжнее буквального "canvas создаётся рендерером один раз" при частой полной перестройке `content` (см. риск §6 про `render()`).
- `ui_swipe_left_to_delete.riv`'s state machine — `"Swipe to delete"` (не `"State Machine 1"`, как в FC-16/17 тексте) — использовано реальное имя из `RiveResearch.md` §2 (инспекция runtime), не из плана.

**RED (обнаруженная и исправленная во время разработки регрессия, не просто отсутствие фичи):** после первой реализации `installTouchSwipeRating(back, acceptAnyPointerType=true)` три существующих сценария сломались: `kotlin-binary-rating.spec.ts` "wrong typed answer..." (клик по кнопке "2 Вспомнил" не создавал review — `totalReviews` оставался 0) и `kotlin-flip-card.spec.ts`'s "clicking a rating button..."/"a mouse swipe..." (то же). Причина подтверждена логированием: `back.setPointerCapture(pointerId)` срабатывал даже когда pointerdown был на кнопке (`editableTarget` проверял только точный тэг `event.target`, а реальный target — вложенный `<small>`/`<span>`), из-за чего последующий `click` перенаправлялся на `back`, а не на кнопку. Исправлено (см. отклонения выше) → все три сценария снова GREEN, дополнительно подтверждено `kotlin-preferences-settings.spec.ts` (22/22) и полным regression-прогоном.

**Проверка (PASS/FAIL/NOT RUN):**

| Проверка | Команда (рабочая директория) | Результат |
|---|---|---|
| Shared unit (`CardEffectTest`) | `./gradlew :shared:desktopTest` (`kotlin/`) | PASS (0 failures) |
| Shared JS | `./gradlew :shared:jsTest` (`kotlin/`) | PASS |
| Shared Wasm | `./gradlew :shared:wasmJsTest` (`kotlin/`) | PASS |
| JS/Wasm target compile | `./gradlew :composeApp:compileKotlinJs :composeApp:compileKotlinWasmJs` (`kotlin/`) | PASS |
| Прочие таргеты (не должны сломаться от нового `shared`-файла) | `./gradlew :shared:compileKotlinDesktop :shared:compileKotlinIosSimulatorArm64 :shared:compileKotlinMacosArm64 :composeApp:compileKotlinDesktop :androidApp:compileDebugKotlin` (`kotlin/`) | PASS (только пред-существующие warnings) |
| Fresh distribution | `./gradlew :composeApp:composeCompatibilityBrowserDistribution` (`kotlin/`) | PASS, `rive/{confetti,again}.riv,rive.js,rive.wasm,rive-bridge.js` присутствуют в `dist/composeWebCompatibility/productionExecutable/rive/` |
| Playwright, wasm, chromium, полный `testMatch` (включая новые `kotlin-flip-card.spec.ts`, `flip-rive-perf.spec.ts`) | `KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable KOTLIN_SPIKE_BRANCH=wasm npx playwright test --config=playwright.kotlin.config.ts --project=chromium` (корень) | PASS 96/96 |
| Playwright, js, chromium, полный `testMatch` | то же с `KOTLIN_SPIKE_BRANCH=js` | PASS 100/100 (включая `flip-rive-perf.spec.ts`, отдельно посчитан в wasm-прогоне) |
| Обновлённый существующий тест (капча pointer теперь на `.card-back`, не на `.vocabulary-swipe-zone`) | `kotlin-preferences-settings.spec.ts:213` "grammar real touch completes after leaving the swipe zone" | PASS после правки ассерта на новый capturing-элемент (см. отклонения) |

**Размер бандла (initial load; сравнение HEAD `a7b063c` vs это изменение, `composeCompatibilityBrowserDistribution`, gzip):**

| Файл | До | После | Δ |
|---|---|---|---|
| `originJsComposeApp.js` | 791 795 B | 792 848 B | +1 053 B |
| `originWasmComposeApp.js` | 99 676 B | 99 732 B | +56 B |
| Kotlin/Wasm `*.wasm` (composeApp) | 744 377 B | 744 917 B | +540 B |

Итого начальная загрузка выросла на ~1.6 KB (gzip) — код flip/swipe/`CardEffect`/оверлея минимален. Rive runtime (`rive.js` 95 KB gzip + `rive.wasm` 361 KB gzip + два `.riv` по ~2 KB gzip, итого ~460 KB gzip) в начальную загрузку **не входит**: подтверждено `flip-rive-perf.spec.ts`'s "bundle keeps the Rive assets lazy" — ни один `rive/*`-запрос не уходит до первого `AppAction.Reveal`/`Rate`, только после первого рейтинга (см. RiveResearch.md §5/§6 "не в `main()`").

**Известные ограничения:** `flip-rive-perf.spec.ts`'s long-task/производительные измерения — Chromium headless, ориентировочные (не device-grade), как и оговорено в §5 для всех хостов; не заменяет `xctrace`/`dumpsys gfxinfo`/Macrobenchmark для других хостов. Полный CDP `Tracing.start` rAF-дельта профиль и `performance.measureUserAgentSpecificMemory()` из §5 не реализованы в этом проходе (LEAN MODE — только затронутые сценарии); присутствует базовый `PerformanceObserver('longtask')`-бюджет и bundle-size сравнение.

## 9. Evidence log — android (Developer, реализовано)

Реализовано для Android-хоста: FC-04/07 (свайп-оценка перенесена с узкой `AndroidRatingActions`-полосы на весь back-face, свайп и тап-переворот теперь один жест-детектор), FC-10/13/14 (native 3D-оборот через `Modifier.graphicsLayer{rotationY}`, авто-доворот на `Revealed`, тап неактивен до `Revealed`, `Rating`/`AppAction` не трогает FSRS), FC-15/16/20 (Rive-оверлей: `app.rive:rive-android:11.12.1`, легаси `RiveAnimationView.fireState`, vendored `confetti.riv`/`again.riv` из `THIRD_PARTY/credits.md`, `hideFromAccessibility()`, `touchPassThrough=true`, гейт по `Motion.Reduced` и debug-флагу), variant-toggle `System.getProperty("polski.debug.riveDisabled")` (§5, вариант B) поверх реального `Motion.Reduced` (вариант A). Новые файлы: `composeApp/src/androidMain/kotlin/polski/ui/screens/{AndroidFlipCard,AndroidRiveOverlay}.kt`; изменён `AndroidTrainingScreen.kt`/`AndroidContent.kt`/`MainActivity.kt` (проводка `reduceMotion`); `composeApp/build.gradle.kts` (`androidResources { enable = true }`, `app.rive:rive-android`); `composeApp/src/androidMain/res/raw/{confetti,again}.riv`. FC-01/02/03/05/06/08/09/11/12/17/18/19 — другие хосты, вне охвата этого прохода (web уже сделан, см. §8).

**Отклонения от буквы плана (обоснованные, зафиксированы явно):**
- FC-10's «неактивная сторона получает `Modifier.semantics { invisibleToUser() }`» заменено на композицию только ОДНОЙ стороны за раз (`if (angle < 90f) front() else back()`, переключение ровно на середине оборота) — раз обе стороны никогда не смонтированы одновременно, скрытой стороне просто нечего скрывать; проще и эквивалентно для a11y, поскольку до `Revealed` у back-face всё равно нет данных ответа.
- FC-10/FC-04 буквально описывают тап-переворот как `Modifier.clickable` на всём `Box`, а свайп — как отдельный `detectHorizontalDragGestures` на back-face внутри него. Это не сработало: `clickable`-предок перехватывал жест раньше вложенного drag-детектора и любой свайп молча превращался в переворот (0 оценок при явном свайпе на 700+ px — см. RED ниже). Заменено на **один** детектор `detectFlipOrSwipe` (ручной `awaitEachGesture`/`awaitPointerEvent`-цикл) на back-face: малое смещение → тап-переворот, смещение за порог → оценка, промежуточное (короткий/вертикальный свайп) → ничего — что и требует FC-07 буквально.
- Обнаружен и исправлен второй, независимый баг того же класса: детектор жеста нельзя вешать на потомка с `Modifier.graphicsLayer{rotationY=180f}` (нужного для анти-зеркального текста back-face, FC-10) — поворот на 180° зеркалит локальную ось X, и измеренный `dx` получает обратный знак/направление относительно экрана. Исправлено разделением слоёв: внешний немодифицированный `Box` — держатель жеста (реальные экранные px), внутренний `Box` — чисто визуальный `rotationY`.
- FC-16's `RiveAnimationView(context).apply{...}` не хватило: без явного `Rive.init(context)` первый `RiveAnimationView`-конструктор бросает `UnsatisfiedLinkError` (`FileAssetLoader.constructor` не находит `librive-android.so` — нативная библиотека не была загружена). Добавлен идемпотентный `ensureRiveInitialized()` в `AndroidRiveOverlay.kt`, вызывающий `Rive.init(context.applicationContext)` один раз на процесс, перед первым `RiveAnimationView(...)`.
- Плановая связка `app.rive:rive-android` + `R.raw.confetti` не заработала «из коробки»: `composeApp` — KMP `com.android.kotlin.multiplatform.library`-модуль, который по умолчанию не мержит classic Android `res/` (`generateAndroidMainRFile` не выполнялся, `res/raw` не давал `R`-класса). Добавлено `androidResources { enable = true }` в `kotlin { android { ... } }` (`composeApp/build.gradle.kts`) — минимальная, документированная точка расширения самого AGP KMP DSL, не форк/патч плагина.

**RED (обнаруженная и исправленная во время разработки регрессия, задокументирована как в §8):** первая реализация (буквальная: `clickable`-предок + отдельный `detectHorizontalDragGestures` на back-face) скомпилировалась и тап-переворот работал, но **любой** свайп на реальном эмуляторе (`adb shell input swipe`, амплитуда 700+ px, порог 189px @420dpi) не создавал оценку — `dueCount`/`chainIndex` не менялись, карточка просто оставалась на месте или перескакивала на переднюю сторону. Подтверждено логированием (`Log.d` внутри детектора, временно, удалено перед финалом): жест долетал до `detectHorizontalDragGestures`, но не порождал `onRate`. Причина подтверждена по логам в два прохода: (1) `clickable`-предок выигрывал у `pointerInput`-потомка; (2) после объединения в один детектор второй баг — измеренный `dx` был противоположного знака (`-672` для свайпа вправо) из-за вложенности под `rotationY=180f`. Оба исправления описаны выше → свайп влево/вправо, тап-туда-обратно и завершение цепочки (`ChainComplete`, 5/5) подтверждены на `emulator-5554` при `Motion.System` **и** `Motion.Reduced` (см. таблицу ниже).

**Проверка (PASS/FAIL/NOT RUN):**

| Проверка | Команда (рабочая директория) | Результат |
|---|---|---|
| Android unit (`AndroidFlipCardTest`, 15 тестов: `flipOnTap`×3, `ratingForDrag`×3, `isFlipTap`×3, `cardEffectToPlay`×4, `SingleRatingGate`×2) | `./gradlew :androidApp:testDebugUnitTest` (`kotlin/`) | PASS (0 failures) |
| Полный набор `androidApp` unit-тестов (не должен сломаться от нового кода) | `./gradlew :androidApp:testDebugUnitTest` (`kotlin/`) — 7 классов, 33 теста | PASS (0 failures), включая `AndroidSessionViewModelPreferencesTest`, `AndroidUserPreferencesStoreTest`, `AppearanceResolutionTest` и др. |
| RED (обнаруженная регрессия, `flipOnTap`/`ratingForDrag` временно испорчены) | то же, вручную | FAIL 4/12 в `AndroidFlipCardTest` (ожидаемо), затем PASS 12/12 после восстановления — зафиксировано как реальное RED→GREEN, не имитация |
| `composeApp` Android-компиляция | `./gradlew :composeApp:compileAndroidMain` (`kotlin/`) | PASS |
| `androidApp` сборка debug APK | `./gradlew :androidApp:assembleDebug` (`kotlin/`) | PASS, `librive-android.so`/`libc++_shared.so` для всех 4 ABI упакованы (см. размер ниже) |
| Эмулятор-смоук (`emulator-5554`, `Polski_ARM35`, `adb shell input tap/swipe` + `uiautomator dump`) | вручную, см. ниже | PASS по всем пунктам |

**Эмулятор-смоук, по пунктам задачи:**

| Проверка | Результат |
|---|---|
| Тап по карточке до `Reveal` | Не переворачивает (контракт §0) — front остаётся, `AppAction.Reveal` не диспетчится повторно |
| `Reveal` → авто-доворот | Карточка сама показывает back-face (`Эталон`) сразу после «Показать ответ» |
| Тап по back-face → перевернуть к front | PASS, `phase` остаётся `Revealed`, `dueCount`/`chainIndex` не меняются |
| Тап по front (после ревью) → назад к back | PASS |
| Свайп влево на back-face → «Again», ровно одна оценка | PASS (`chainIndex` +1, `Rating.Again` подтверждён логом на этапе отладки) |
| Свайп вправо на back-face → «Good», ровно одна оценка | PASS (`chainIndex` +1, `Rating.Good`) |
| Кнопки `Повторить`/`Вспомнил` продолжают работать | PASS (использованы для части прогонов цепочки) |
| `Motion.Reduced` (переключено через реальный Settings-тоггл, не хак файла) | Оборот/эффект без анимации, ни одного крэша на полном цикле reveal→flip→flip-back→swipe→`ChainComplete` (5/5) |
| Rive-оверлей скрыт от TalkBack | `uiautomator dump` не содержит ни одного узла/атрибута с `rive`/`TextureView` — `hideFromAccessibility()` полностью исключает узел из дерева, не просто «skip» |
| Rive-оверлей не перехватывает тап/свайп | Подтверждено косвенно: свайпы и тапы поверх карточки (где рисуется оверлей) корректно долетают до `detectFlipOrSwipe`/`AndroidRatingActions` во всех прогонах |

**Размер APK (`androidApp-debug.apk`, сравнение HEAD `a7b063c` vs это изменение, `assembleDebug`, `apkanalyzer`):**

| Метрика | До | После | Δ |
|---|---|---|---|
| `file-size` (все 4 ABI, debug, несжатый на диске) | 13 061 119 B | 41 170 211 B | +28 109 092 B (~26.8 MB) |
| `download-size` (все 4 ABI, apkanalyzer-оценка) | 12 573 754 B | 23 285 509 B | +10 711 755 B (~10.2 MB) |
| `librive-android.so` + `libc++_shared.so`, один ABI (`arm64-v8a`) | — | 5 336 624 + 1 292 904 B | +6 629 528 B (~6.3 MB) |

Итог: рост измерен на debug-APK со всеми 4 ABI сразу (`arm64-v8a`/`armeabi-v7a`/`x86`/`x86_64` — по одной копии `librive-android.so` на каждый), поэтому абсолютные +26.8/+10.2 MB не репрезентативны для реального устройства. Per-ABI рост (~6.3 MB на `arm64-v8a`, основной таргет `Polski_ARM35`) близко совпадает с оценкой из `RiveResearch.md` §5–6 («~2.4 MB compressed/ABI»; здесь несжатый file-size, отсюда разница). Для реального релиза нужен AAB (по ABI) или `abiFilters` — не входит в этот проход (не менялась стратегия паковки, только добавлена зависимость).

**Известные ограничения:** `dumpsys gfxinfo`/Macrobenchmark framestats (§5) не собраны в этом проходе — LEAN MODE ограничил проверку до затронутых поведений (флип/оценка/жест/reduced motion/TalkBack), а не до полного измерения A/B/C по всем метрикам; variant-toggle (`polski.debug.riveDisabled`) реализован и компилируется, но отдельный С-прогон с `-Dpolski.debug.riveDisabled=true` на эмуляторе не выполнялся отдельной сессией — сделан только логический review (читает `System.getProperty` корректно, покрыт неявно тем, что `cardEffectToPlay`/`SingleRatingGate.rate` юнит-протестированы на этом флаге). Persisted device preferences (`swipeRatingEnabled=false`, `motion=Reduced`) с предыдущей сессии на `Polski_ARM35` изначально маскировали часть смоука — учтено и явно перепроверено после переключения через реальный Settings UI, не через правку файла.

### 9.1 Correction round — fix для reviewer blocker (`detectFlipOrSwipe` глушил вертикальный скролл)

**Blocker (major, ревьюер):** `detectFlipOrSwipe` (§9, `AndroidFlipCard.kt`) висел на всей revealed back-face и вызывал `change.consume()` на **каждом** событии перемещения, независимо от направления, до `up`. Экран обёрнут в `Modifier.verticalScroll(studyScroll)` (`MainActivity.kt:275`), а карточка — самый высокий контент на экране (объяснение, список изменений, memo, текст ревью, кнопки оценки). Main-pass у Compose отдаёт потомку первый доступ к событию раньше `verticalScroll`-предка, поэтому любой драг, начинающийся на карточке — включая обычную попытку проскроллить страницу — полностью проглатывался старым кодом и никогда не долетал до `verticalScroll`. Это реальная регрессия относительно кода, который заменил `AndroidFlipCard`: старый `AndroidRatingActions` использовал `detectHorizontalDragGestures` на узкой полосе, который сам отменяется («cancel on vertical scroll») при вертикально-доминантном драге — то самое поведение, которое требует android-навык для этого класса жестов, и которое новый детектор на всей back-face потерял.

**Fix:** `detectFlipOrSwipe` больше не консьюмит безусловно. Пока драг не превысил `tapSlopPx` ни по одной оси — это неопределённая фаза, ничего не консьюмится (`continue`). В момент первого превышения `tapSlopPx`: если `|dy| >= |dx|` (вертикально-доминантный) — функция немедленно `return@awaitEachGesture` **без единого `consume()`**, ровно как `awaitHorizontalTouchSlopOrCancellation`/`detectHorizontalDragGestures` из `androidx.compose.foundation.gestures` отменяют себя на диагональном/вертикальном драге, отдавая событие предку; если `|dx| > |dy|` (горизонтально-доминантный) — жест «залипает» за этим детектором (`horizontal = true`) и только с этого момента каждое дальнейшее перемещение консьюмится, как и раньше, чтобы не смешивать оценку-свайп со скроллом. Тап (без превышения slop вообще) как и прежде ничего не консьюмит и обрабатывается после цикла через `isFlipTap`. Диф изолирован в `detectFlipOrSwipe`; `flipOnTap`/`ratingForDrag`/`isFlipTap`/`cardEffectToPlay`/`SingleRatingGate`/`AndroidFlipCard` (composable) не менялись.

**Проверка (PASS/FAIL/NOT RUN), только затронутое (LEAN MODE):**

| Проверка | Команда (рабочая директория) | Результат |
|---|---|---|
| `AndroidFlipCardTest` + весь `androidApp` unit-набор | `./gradlew :androidApp:testDebugUnitTest` (`kotlin/`) | PASS, 33/33 (тесты покрывают только чистые функции — `detectFlipOrSwipe` не юнит-тестируется без Compose UI test rule, которого в `androidApp`/`composeApp` для Android пока нет; см. «Известные ограничения» ниже) |
| `androidApp` сборка debug APK | `./gradlew :androidApp:assembleDebug` (`kotlin/`) | PASS |
| Эмулятор: вертикальный драг, начинающийся на revealed back-face, должен доскроллить страницу (это и есть регрессия из блокера) | `adb shell input swipe` от точки на карточке вверх/вниз + `uiautomator dump` до/после, `emulator-5554` | PASS — контрольный текстовый узел («ЗАПОМНИ») сдвинулся с `[137,1010]` на `[137,482]` (Δ≈528px) после вертикального драга `1300→700`, т.е. `verticalScroll` предка получил и обработал жест, а не карточка |
| Эмулятор: тап по back-face всё еще переворачивает к front и обратно | `adb shell input tap` + `uiautomator dump`, до/после | PASS — оба перевода подтверждены по смене видимого текста (`Показать ответ` ⇄ `Эталон`/кнопки оценки) |
| Эмулятор: горизонтальный свайп вправо на back-face всё еще ставит «Good» ровно один раз | `adb shell input swipe` горизонтально + `uiautomator dump` заголовка | PASS — `16 к повторению`→`15`, `7 сегодня`→`8`, `0/5`→`1/5`, ровно один шаг цепочки |
| Эмулятор: горизонтальный свайп влево на back-face всё еще ставит «Again» ровно один раз | то же, направление влево | PASS — `15`→`14`, `8`→`9`, `1/5`→`2/5`, ровно один шаг |
| Размер APK не изменился (чисто логический фикс, без новых зависимостей/ресурсов) | `ls -la androidApp-debug.apk` до/после | PASS — байт-в-байт тот же `file-size`, 41 170 211 B |

**Известные ограничения:** фикс проверен через реальные `adb`-жесты на эмуляторе (наблюдаемое смещение контента при скролле, наблюдаемые счётчики при оценке), а не через Compose UI test (`createComposeRule().onNode(...).performTouchInput { swipe(...) }`), потому что ни `composeApp`, ни `androidApp` пока не тянут `androidx.compose.ui:ui-test-junit4` для Android-таргета (Robolectric есть, `compose-ui-test` — нет). Добавление этой зависимости и regression-теста на уровне Compose test rule — за рамками этого correction-round (LEAN MODE: только заявленный блокер); стоит сделать отдельным, небольшим проходом, если такие жестовые регрессии повторятся.

## 10. Evidence log — ios (Developer, реализовано)

Реализовано для iOS-хоста: FC-01 (shared `cardEffectFor` вызывается не из Swift, а из **уже существующего** `shared/src/iosMain/kotlin/polski/ios/IosSession.kt` — Kotlin-код iOS-хоста, ровно в точке принятого `AppAction.Rate`; см. «Отклонения» ниже), FC-05/07 (свайп-оценка перенесена с узкой `VStack("Когда повторить?")` на весь back-face, тот же порог 80pt/×1.5, `.simultaneousGesture` не блокирует `Form`-скролл), FC-11/13/14 (native 3D-оборот через `.rotation3DEffect` в **NEW** `iosApp/PolskiGrammar/FlashCardView.swift`; авто-доворот на `Reveal`; тап неактивен до `Revealed`; оборот не диспетчит `AppAction` и не трогает `CardPhase`/FSRS), FC-15/17/20 (Rive-оверлей: **NEW** `iosApp/PolskiGrammar/RiveEffectOverlay.swift`, `rive-ios` SPM `RiveRuntime` 6.27.0 добавлен в `iosApp/generate_project.rb`, legacy `RiveViewModel`, vendored `iosApp/PolskiGrammar/Rive/{confetti,again}.riv` — байт-в-байт совпадают по SHA-256 с уже задокументированными в `THIRD_PARTY/credits.md`, новая запись не нужна; `.allowsHitTesting(false)`/`.accessibilityHidden(true)`; гейт по `accessibilityReduceMotion` + `Motion.Reduced`), variant-toggle (§5): `#if DEBUG`-только `@AppStorage("polski.debug.riveDisabled")`, скрыт за long-press на «Тестовая версия» в `IosSettingsView`, гасит только Rive-триггер (сам flip продолжает анимироваться — вариант B). FC-02/03/04/06/08–10/12/16/18/19 — другие хосты, вне охвата этого прохода (web/Android уже сделаны, см. §8–9; macOS не входит в этот проход).

**Отклонения от буквы плана (обоснованные, зафиксированы явно):**
- FC-01: план говорит «каждый host вызывает [`cardEffectFor`] сам в момент диспетча `AppAction.Rate`» — на iOS этот момент физически происходит не в Swift, а в `IosSession.dispatch(command:value:)` (Kotlin, `iosMain`), которая парсит команду `"rate"` в `AppAction.Rate` и диспетчит её; Swift-слой этого хоста нигде не видит домейн-типы напрямую (весь мост — строки/JSON через `IosSession`/`AppModel`, см. существующий паттерн `Rating.entries.firstOrNull{...}` в том же файле). Вызов `cardEffectFor` там же, а не в Swift, — единственное место, где это буквально «в момент диспетча», и не заводит копию маппинга `Rating→CardEffect` на стороне Swift. Новый `IosSession.onEffect: ((String) -> Unit)?` фильтрует эффект по тому же признаку, что и сам домен: `cardEffectFor` вызывается только если после `store.dispatch` `exerciseId` действительно сменился (рейтинг принят), а не всегда при валидном разборе команды — иначе повторный/просроченный `"rate"` на уже неактивной карточке мог бы дать паразитный эффект даже когда домен его молча отбросил.
- FC-11: план описывает fronts/backs как «рендерящую фронт (614–650) и бэк (651–719) как два `View` в `ZStack`» (то есть оба смонтированы одновременно). На практике, по образцу `AndroidFlipCard`, смонтирован **только один** face за раз (переключение в середине поворота, `showBack`), а не оба в `ZStack` постоянно — проще, эквивалентно для a11y (до `Revealed` у back-face всё равно нет данных ответа), и избегает нагрузки на `Form`/`List` от двух одновременно живых наборов `Picker`/`TextField`/`Button`.
- Разбиение на face взято буквально по номерам строк плана: **front** = заголовок карточки (`Исходное предложение`, `sourceParts`, intro-ветка) + `Label(prompt)`/`methodLead` + весь `Question`-контент (retrieve/picker/textfield/reveal); **back** = весь `Revealed`-контент (`Эталон`, `changes`, `ЗАПОМНИ`, рейтинг). Заголовок карточки виден только на front — на back его нет; это прямое следствие буквального разбиения плана, не отдельное решение.
- **RED (реальная регрессия №1, найдена и исправлена):** первая реализация ставила `.onTapGesture` на весь `frontFace` **безусловно** (с проверкой `phase == "Revealed"` только внутри closure). `xcodebuild test` сломал `testTypedPolishAnswerUsesNativeInput` и `testMethodSwitchKeepsTypedDraftThroughRevealAndOneReview`: тап по Picker-кнопке «Ответ, Вслух / про себя» проходил, но появившийся popup-меню с опцией «Напечатать» становился ненаходимым (`Automation type mismatch: computed Button from legacy attributes vs PopUpButton from modern attribute`). Подтверждено бисекцией: временное удаление `.onTapGesture` с `frontFace` полностью убирало симптом. Причина — сам факт наличия ancestor-жеста `.onTapGesture` (даже с закрытием-no-op во время `Question`) сбивал разрешение жестов для Menu-стиля `Picker`, а не логика closure. **Исправлено**: модификатор жеста теперь **вообще не навешивается** во время `Question` (`if state.phase == "Revealed" { content.onTapGesture{...} } else { content }`) — ровно тогда, когда `Picker`/`TextField`/`revealButton` вообще присутствуют в `frontFace`, конкурирующего жеста там больше нет.
- **RED (реальная регрессия №2, найдена и исправлена):** после фикса №1 `testMethodSwitchKeepsTypedDraftThroughRevealAndOneReview` всё равно падал (`XCTAssertEqual failed: ("Optional("Moja")") is not equal to ("Optional("Moja proba")")`) — синтезированный `typeText("Moja proba")` терял хвост после пробела. Причина подтверждена бисекцией на baseline vs изменённом коде (`git stash` сравнение): `RiveEffectOverlay` эагерно создавал **оба** `RiveViewModel` (парсинг `.riv`, Metal-поверхность) уже при первом появлении карточки — то есть при каждом нажатии клавиши на этапе `Question`, когда никакой эффект в принципе не может сработать. Лишняя нагрузка на каждый ре-рендер (каждая буква — новый `AppAction.EditAnswer` → полный ре-рендер `TrainingView`) не давала синтетическому вводу XCUITest успевать. **Исправлено**: оба `RiveViewModel` создаются лениво — только на первый реально принятый рейтинг (`RiveEffectOverlay.swift`, `@State private var …ViewModel: RiveViewModel?`) — путь `Question`/печати остаётся ровно таким же дёшевым, каким был до фичи.
- SPM-риск §6 закрыт: перед правкой `generate_project.rb` проверена реальная установленная версия `xcodeproj` (`1.27.0`, `arch -arm64 ruby -e "require 'xcodeproj'; puts Xcodeproj::VERSION"`) и её API (`XCRemoteSwiftPackageReference#repositoryURL/requirement`, `XCSwiftPackageProductDependency#package/product_name`, `PBXBuildFile#product_ref`, `NativeTarget#frameworks_build_phase`) — тот же паттерн, что генерирует сам Xcode при «Add Package Dependency…». `project.save`/regeneration через `arch -arm64 ruby generate_project.rb` проверены на реальном пересоздании `.pbxproj`.
- `again.riv`'s state machine подтверждён как `"Swipe to delete"` / триггер `"Trigger Delete"` (совпадает с уже задокументированным в web/Android evidence log и `RiveResearch.md` §2), не буквальный `"State Machine 1"` из текста FC-16/17.

**Проверка (PASS/FAIL/NOT RUN):**

| Проверка | Команда (рабочая директория) | Результат |
|---|---|---|
| Regenerate project | `arch -arm64 ruby generate_project.rb` (`kotlin/iosApp/`) | PASS, SPM-пакет `rive-ios`/`RiveRuntime`, `FlashCardView.swift`/`RiveEffectOverlay.swift`/`Rive/{confetti,again}.riv` подтверждены в свежем `project.pbxproj` |
| Shared unit (`IosSessionTest`, 4 теста, включая 2 новых для `onEffect`) | `./gradlew :shared:iosSimulatorArm64Test` (`kotlin/`) | PASS (0 failures) |
| RED (обнаруженная и исправленная регрессия в новых тестах) | то же, вручную (временно `if (false && …)` в `IosSession.kt`) | FAIL 2/4 (ожидаемо: оба новых теста), затем PASS 4/4 после восстановления |
| Прочие таргеты, куда попадает изменённый `IosSession.kt` (не должны сломаться) | `./gradlew :shared:compileKotlinIosArm64` (`kotlin/`) | PASS (только пред-существующие warnings) |
| Xcode build (device SPM resolve + компиляция всех новых файлов) | `xcodebuild -project kotlin/iosApp/PolskiGrammar.xcodeproj -scheme PolskiGrammar -destination 'platform=iOS Simulator,id=4384946F-9E6B-43D0-ADA3-CA219A3456B8' -derivedDataPath /private/tmp/polski-ios-dd CODE_SIGNING_ALLOWED=NO build` | PASS, `** BUILD SUCCEEDED **`, без новых warnings в `FlashCardView.swift`/`RiveEffectOverlay.swift`/`PolskiGrammarApp.swift` |
| XCTest UI, чистая `derivedData`, затронутые + новый сценарии (9 тестов) | `xcodebuild ... test -only-testing:.../testBinaryRatingSwipesAdvanceOnceInEachDirection -only-testing:.../testTappingRevealedCardFlipsTwiceWithoutExtraReviewThenRatingCountsOnce -only-testing:.../testTypedPolishAnswerUsesNativeInput -only-testing:.../testMethodSwitchKeepsTypedDraftThroughRevealAndOneReview -only-testing:.../testNativeTrainingMatrixAndProgress -only-testing:.../testNativeChainCompletionShowsFiveAnswersAndKeepsFiveRatings -only-testing:.../testFirstMethodIntroductionKeepsReferenceAnswerHiddenUntilContinue -only-testing:.../testSaveFailureShowsErrorBannerOnEveryTabWithExportReachable -only-testing:.../testAnswerModeChosenInSettingsSurvivesAppRestart` | PASS 9/9 (0 failures), iPhone 17 Pro, чистая `derivedData` |
| RED→GREEN для нового теста `testTappingRevealedCardFlipsTwiceWithoutExtraReviewThenRatingCountsOnce` на HEAD `a7b063c` (без реализации) | то же, `git stash` только App-файлов, чистая `derivedData` | FAIL 1/1 (ожидаемо — `back.tap()`/`front.tap()` ничего не переворачивают на baseline), затем PASS после `git stash pop` |
| Debug-флаг измерений (`#if DEBUG`/`@AppStorage`) не ломает сборку/настройки | `xcodebuild ... build` + `-only-testing:.../testNativeAppearanceSettingsKeepsTrainingCard` | PASS |

**Известные ограничения:** полный regression-прогон всего `PolskiGrammarUITests` (24 метода, часть за `#if S6_PICKER_ACCEPTANCE`) не выполнялся в этом проходе — LEAN MODE ограничил проверку до сценариев, которые реально задевает рефакторинг `cardContent → FlashCardView` (reveal/rate/swipe/typed/intro/error-banner/settings), не до Matrix/Vocabulary-тестов, которых этот диф не касается. XCTest signpost-замеры (§5, `XCTOSSignpostMetric`/`XCTMemoryMetric` для вариантов A/B/C) не собраны — реализован только сам переключатель варианта B и подтверждено, что он не ломает сборку/UI; количественные измерения — отдельный проход. macOS (FC-06/12/17 для Mac) вне охвата этой сессии.

**Размер приложения (`.app`, Debug/iphonesimulator, `iPhone 17 Pro` симулятор, сравнение HEAD `a7b063c` vs это изменение, чистая `derivedData`):**

| Метрика | До | После | Δ |
|---|---|---|---|
| `PolskiGrammar.app` (`du -sh`, весь bundle) | 13 004 KB (~12.7 MB) | 22 992 KB (~22.5 MB) | +9 988 KB (~9.75 MB) |
| `Frameworks/RiveRuntime.framework` (внутри `.app`, один арх. slice) | — | 9,7 MB | +9,7 MB |

Рост практически целиком объясняется `RiveRuntime.framework` (Metal-рантайм + оба слайса симулятора в Debug, несжатый, без App Thinning). Это ориентировочная, не device-grade цифра (Debug/simulator, без code signing/thinning) — как и оговорено в §5: реальный прирост на App Store IPA (thinned, release, один arm64-slice, gzip) должен быть заметно меньше; сравнение с RiveResearch.md §5's оценки «a few MB per arch» для `rive-ios` не проводилось отдельным App Thinning report в этом проходе (LEAN MODE).

## 11. Evidence log — macos (Developer, реализовано)

Реализовано для macOS-хоста: FC-01 (shared `cardEffectFor` вызывается из **уже существующего**
`shared/src/macosMain/kotlin/polski/macos/MacSession.kt` — тот же паттерн, что `IosSession.onEffect`
из §10, адаптированный к `MacSession.dispatch`'s собственному формату `"rate"` — `"<exerciseId>|<Rating>"`,
а не `IosSession`'s одиночный `value`), FC-06/07 (свайп-оценка — на macOS свайпа не было вовсе —
добавлен на весь revealed back-face: тот же порог 80pt/×1.5, что iOS/Android, `.simultaneousGesture`
не блокирует `ScrollView`'s вертикальный скролл), FC-12/13/14 (native 3D-оборот через
`.rotation3DEffect` в **NEW** `macosApp/PolskiGrammarMac/MacFlashCardView.swift`; авто-доворот на
`Reveal`; тап неактивен до `Revealed`; оборот не диспетчит `MacSession`-команду и не трогает
`phase`/FSRS), FC-15/17/20 (Rive-оверлей: **NEW**
`macosApp/PolskiGrammarMac/RiveEffectOverlay.swift`, `rive-ios` SPM `RiveRuntime` 6.27.0 добавлен в
`macosApp/generate_project.rb` (macOS до этого прохода вообще не добавлял SPM-пакетов — тот же
`xcodeproj` 1.27.0 API, что iOS §10 уже проверил), legacy `RiveViewModel`, vendored
`macosApp/PolskiGrammarMac/Rive/{confetti,again}.riv` — байт-в-байт совпадают по SHA-256 с уже
задокументированными в `THIRD_PARTY/credits.md` (скопированы из `iosApp/PolskiGrammar/Rive/`, не
скачаны повторно), новая запись credits не нужна; `.allowsHitTesting(false)`/
`.accessibilityHidden(true)`; гейт по `accessibilityReduceMotion` + `Motion.Reduced`), variant-toggle
(§5): `#if DEBUG`-только `@AppStorage("polski.debug.riveDisabled")`, скрыт за long-press на
существующей строке футера настроек «Polski Grammar Matrix · польский ↔ русский» в
`MacSettingsView`, гасит только Rive-триггер (сам flip продолжает анимироваться — вариант B). Прочие
FC — другие хосты, вне охвата этого прохода (web/Android/iOS уже сделаны, см. §8–10).

**Отклонения от буквы плана (обоснованные, зафиксированы явно):**
- FC-11/FC-12 планировал `MacFlashCardView` буквально «аналогично» `FlashCardView` (iOS), которая
  рендерит фронт/бэк на дынамическом `Record`-доступе к JSON. У macOS-хоста нет такого моста — он
  декодирует снапшот в типизированные `Decodable`-структуры (`TrainingSnapshot`, `MacModel`), в
  отличие от iOS/Android. `MacFlashCardView`/`RiveEffectOverlay` поэтому написаны против
  `TrainingSnapshot`/`MacModel`/`CardEffectEvent` напрямую, не через промежуточный `Record`-слой —
  логика (single-mounted-face, анти-зеркальный `-180°`, авто-доворот, единственный жест-детектор)
  идентична `FlashCardView`, только типы моста другие.
- Это потребовало снять `private` с `TrainingSnapshot`/`VocabularySnapshot`/`PreferencesSnapshot`/
  `JSONDocument`/`DocumentKind`/`MacModel` и с функции `highlightedText` в
  `PolskiGrammarMacApp.swift` (были file-private — на macOS Swift это ограничивает область именно
  файлом, а не таргетом) до `internal` (без модификатора) — иначе `MacFlashCardView.swift`/
  `RiveEffectOverlay.swift` не видят эти типы. **RED (обнаруженная и исправленная регрессия):**
  первая попытка оставить только `MacModel`/`TrainingSnapshot` открытыми не собралась —
  `swift-frontend` дал 5 ошибок `property/method must be declared fileprivate because its type
  uses a private type` (для `vocabulary`/`preferences`/`exportDocument`-свойств и
  `export`/`importJSON`/`recovery`-методов `MacModel`, которые сами использовали ещё-приватные
  `VocabularySnapshot`/`PreferencesSnapshot`/`JSONDocument`/`DocumentKind`) — Swift's access control
  транзитивен через сигнатуры членов уже-нечастного типа. Исправлено снятием `private` со всех
  пяти типов сразу; чисто видимость внутри таргета `PolskiGrammarMac`, извне модуль не экспортирует
  ничего нового (это app-таргет, не библиотека).
- FC-06/12's эталонный порог свайпа (80pt/×1.5) взят с iOS (FC-05/11), а не с плановых Android-цифр
  (`detectFlipOrSwipe`'s px-based пороги в §9 не переносимы буквально на points/macOS input) —
  соответствует тому, что уже сделал iOS-проход в §10, никакого нового решения не потребовалось.

**RED→GREEN (shared Kotlin, TDD):** `MacSessionTest.rateFiresOnEffectOnceWithTheDomainsMappedEffectAndSkipsADroppedSecondRate`/
`rateAgainFiresTheAgainEffect` написаны первыми против `MacSession.onEffect` (объявлен, но
временно не вызывался — `if (false) onEffect?.invoke(...)`, чтобы RED был реальным падением
поведения, а не ошибкой компиляции по правилу "tooling/import failure is not RED evidence").
`./gradlew :shared:macosArm64Test` → FAIL 2/2 новых тестов (204 теста всего, 202 прошли, оба новых
упали на `assertEquals(listOf("Remembered"), effects)` — `effects` оставался пустым). Восстановлен
реальный вызов `onEffect?.invoke(cardEffectFor(rateAction.rating).name)` → PASS 204/204.

**Проверка (PASS/FAIL/NOT RUN):**

| Проверка | Команда (рабочая директория) | Результат |
|---|---|---|
| RED: `MacSessionTest`'s 2 новых теста, `onEffect` объявлен но не вызывается | `./gradlew :shared:macosArm64Test` (`kotlin/`) | FAIL 2/2 новых (ожидаемо), 202/204 прочих PASS |
| GREEN: `MacSessionTest` + весь `shared` macOS-набор после восстановления вызова | то же | PASS 204/204 |
| Regenerate project (первое добавление SPM-пакета в `macosApp/generate_project.rb`) | `arch -arm64 ruby generate_project.rb` (`kotlin/macosApp/`) | PASS, `rive-ios`/`RiveRuntime`, `MacFlashCardView.swift`/`RiveEffectOverlay.swift`/`Rive/{confetti,again}.riv` подтверждены в свежем `project.pbxproj`; `xcodeproj` 1.27.0 API (та же версия, что iOS §10 уже проверил) отработал без правок |
| Xcode build, Debug (SPM resolve + компиляция всех новых файлов) | `xcodebuild -project kotlin/macosApp/PolskiGrammarMac.xcodeproj -scheme PolskiGrammarMac -destination 'platform=macOS' -derivedDataPath /private/tmp/polski-mac-dd CODE_SIGNING_ALLOWED=NO build` | PASS, `** BUILD SUCCEEDED **` |
| Xcode build, Release (проверка что `#if DEBUG`-варианты не ломают релизную конфигурацию) | то же с `-configuration Release`, отдельная `derivedData` | PASS, `** BUILD SUCCEEDED **` |
| Запуск-смоук (`open ... -W`, свежий `POLSKI_MAC_DATA_DIR`) | вручную, см. ниже | PASS |

**Запуск-смоук, по пунктам:** приложение запущено (`open PolskiGrammarMac.app --env
POLSKI_MAC_DATA_DIR=... -W`), процесс стабильно жил 25+ секунд без падения, ни одного crash-report
в `~/Library/Logs/DiagnosticReports`, `log show` за это окно — пусто (без ошибок), корректно
завершилось по `quit`. **Ограничение, зафиксированное уже в §6/§5 плана и в
`BinaryRatingUXPlan.md`:** `osascript`/System Events не имеет прав Accessibility в этой среде
(`-1728 not allowed assistive access`), поэтому пошаговая проверка оборота/свайпа/рейтинга через
скриптованный accessibility-обход (как `uiautomator`/XCUITest на других хостах) здесь физически
недоступна — macOS-хост остаётся без автоматизированной UI-проверки этой фичи, как и было явно
предсказано в плановом риске (§6 "macOS ... без XCTest UI автоматизации"). Визуальная/логическая
корректность (компиляция, unit-тест на `onEffect`, стабильный запуск) — то, что LEAN MODE и
доступная среда позволяют подтвердить для этого хоста.

**Размер приложения (`.app`, Debug, `arm64`, сравнение HEAD `a7b063c` vs это изменение, чистая
`derivedData`):**

| Метрика | До | После | Δ |
|---|---|---|---|
| `PolskiGrammarMac.app` (`du -sk`, весь bundle) | 28 148 KB (~27 MB) | 37 972 KB (~37 MB) | +9 824 KB (~9.6 MB) |
| `Frameworks/RiveRuntime.framework` (внутри `.app`) | — | 9.5 MB | +9.5 MB |

Рост почти целиком объясняется `RiveRuntime.framework` (несжатый Debug-бинарь для `arm64`) — та же
причина и близкая величина, что iOS §10 (+9.75 MB/+9.7 MB framework) зафиксировал для симулятора.
Ориентировочная, не device-grade цифра (Debug, без code signing/thinning); реальный Release/notarized
`.app` должен быть меньше — отдельный App Thinning/notarization-отчёт не проводился (LEAN MODE).

**Известные ограничения:** нет UI-автоматизации оборота/свайпа/рейтинга на этом хосте (см. выше —
Accessibility недоступна в этой среде, а macOS-приложение не имеет отдельного UI-test таргета,
`generate_project.rb` его не создаёт, в отличие от iOS); проверка ограничена компиляцией, shared
Kotlin unit-тестом на `onEffect` (RED→GREEN) и стабильным запуском без падений. `dumpsys`/`xctrace`-
подобные измерения A/B/C (§5) не собраны — вариант-переключатель реализован и компилируется
(Debug и Release), но количественный замер — отдельный проход. Полный `MacSessionTest`-набор (не
только новые тесты) прогнан и остаётся зелёным — единственный реальный regression-guard для
`MacSession.dispatch`, доступный на этом хосте без Accessibility.

## 12. v2 — точный оборот на 90°, эффект самого оборота, больше Rive-анимаций

Дата: 2026-09-26. Основание: пользовательский запрос («думай какие анимации можно добавить помимо
поворота, ... rive lib ... высокопроизводительная библиотека; форкнуть muazkadan/Rive-CMP, если
будут проблемы») и исследование [RiveCatalog.md](RiveCatalog.md) (полный каталог; §0 там же
документирует два реальных дефекта v1 — см. FC2-09/FC2-12). v1 (§0–§11 выше) не меняется этим
разделом, кроме явно перечисленных правок. Все новые FC2-пункты сохраняют §0 v1 буквально: оборот и
Rive-оверлеи остаются чисто визуальными, не диспетчат `AppAction`, не трогают `CardPhase`/FSRS.
Приложение в разработке — миграций нет, изменения минимальны и идиоматичны для каждого хоста.

### 12.0 Контракт v2

Ни одна из FC2-06…FC2-10 не требует нового `shared`-кода. `interactive_rings.riv` (R2) и «Tada»
(R3-C) привязаны к уже существующим host-local сигналам — `flipped`/`rotation`/`angle` (сам
оборот, §0/FC-08/10/11/12 v1) и `state.phase == CardPhase.ChainComplete` (уже существующий домейн-
enum, `AppUiState.kt:13`) — а не к новому общему API. `CardEffect`/`cardEffectFor` (FC-01,
`shared/src/commonMain/kotlin/polski/presentation/CardEffect.kt`) переиспользуется без изменений
для FC2-09 (Pick A заменяет только сам `.riv`-файл и имена триггеров, не маппинг рейтинга).

### 12.1 R1 — обмен граней ровно на 90° (по хостам)

**FC2-01 (web, найденный дефект).** Причина «мгновенной вспышки ответа» подтверждена по коду, не
предположительно: при авто-доворотe на `Reveal` (`renderCard`, `composeApp/src/webMain/kotlin/
polski/ui/TrainingWebApp.kt:557-564`) элементы `flip`/`inner` создаются **заново** этим же вызовом,
`flipped` уже `true` (установлено до рендера, строка ~244), и `applyFlipState` (строка 568)
присваивает класс `"card-flip-inner flipped"` **до первой отрисовки** только что созданного узла —
у браузера нет предыдущего кадра, от которого можно анимировать CSS `transition`, поэтому переход
происходит мгновенно (реальный CSS-баг «no previous frame to transition from», не троттлинг и не
описанная в v1 гонка). При последующих ручных тапах (`installCardFlip`, строка 584) тот же DOM-узел
переживает рендеры (см. §6 v1 «полная перестройка render()»), поэтому transition для них уже
работает нормально — баг специфичен для самого первого автоматического доворота.
**Fix.** Единая функция `applyFlip(inner, front, back, toFlipped, reduceMotion)` заменяет
`applyFlipState`, вызывается из обеих точек (авто-доворот и ручной тап) и всегда явно знает текущий
угол (`currentAngle`, поле рендерера рядом с `flipped`/`flippedExerciseId`, строки 232-233), а не
полагается на CSS-класс + «предыдущий вычисленный стиль» браузера:
- Reduced motion (`[data-motion=reduced]`, уже существующий гейт, `training.css:99`): сразу
  `inner.style.transform = rotateY(<final>deg)`, синхронный обмен `aria-hidden`/`inert` — без
  промежуточных кадров (буквально «мгновенная смена стороны без transition»).
- Иначе — две последовательные Web Animations API анимации вместо одного CSS-`transition`
  (`transition:transform .5s ease` в `training.css:41` убирается для `.card-flip-inner`, остальные
  правила `.card-flip`/`.card-face`/`.card-back.card-face` — из v1 — не меняются, они и дают
  реальный визуальный обмен граней ровно на 90° через `backface-visibility:hidden`):
  ```
  const from = currentAngle, mid = 90, to = toFlipped ? 180 : 0
  const phase1 = inner.animate([{transform:`rotateY(${from}deg)`},{transform:`rotateY(${mid}deg)`}],
                                {duration: 250, easing: 'ease-in', fill: 'forwards'})
  phase1.finished.then(() => {
    swapAriaAndInert(front, back, toFlipped)          // ровно в точке 90°, не по таймеру клика
    const phase2 = inner.animate([{transform:`rotateY(${mid}deg)`},{transform:`rotateY(${to}deg)`}],
                                  {duration: 250, easing: 'ease-out', fill: 'forwards'})
    phase2.finished.then(() => { inner.style.transform = `rotateY(${to}deg)`; currentAngle = to })
  })
  ```
  Любая незавершённая `phase1`/`phase2` отменяется (`animation.cancel()`) перед запуском новой — на
  случай быстрого повторного тапа. `swapAriaAndInert` — вынесенное содержимое текущего
  `applyFlipState` (строки 572-575) без строки, переключающей класс.
**FC2-02 (web, вспомогательное).** Поскольку `applyFlip` больше не зависит от «предыдущего кадра»
браузера, а явно знает `from`, единая функция закрывает баг структурно (не только для этого одного
вызова) — второй точки вызова с тем же классом ошибок не появится при будущих рефакторингах рендера.
**FC2-03 (web, доказательство).** Продолжение `flip-rive-perf.spec.ts`/`kotlin-flip-card.spec.ts`
(`playwright.kotlin.config.ts`): замедлить анимацию через CDP, не трогая продовый код —
`const cdp = await page.context().newCDPSession(page); await cdp.send('Animation.setPlaybackRate',
{playbackRate: 0.1})`. На ~40%/~60% замедленной длительности первой фазы проверить: до 40% — текст
ответа не в accessibility-дереве (`back` имеет `aria-hidden`/`inert`, `getByText(...)` не matched
как visible), после 60% — `aria-hidden`/`inert` снят и текст читаем; симметрично для обратного хода.
**FC2-04 (Android, уже корректно — подтверждение, не правка).** `AndroidFlipCard.kt:143-150,166-168`
уже управляет `showingBack = angle >= 90f`, где `angle` — реальное значение `animateFloatAsState` на
каждом кадре; back-контент **не компонуется** (не просто скрыт), пока `angle < 90f`, — это буквально
удовлетворяет R1 уже сейчас, никакого продового изменения не требуется. Единственный пробел —
regression-тест: `androidApp`/`composeApp` не тянут `androidx.compose.ui:ui-test-junit4` для Android
(уже зафиксированное ограничение v1, §9.1). Добавить эту зависимость и один Compose UI-тест,
двигающий `MainTestClock` до `angle≈45f`/`angle≈135f` и проверяющий отсутствие/наличие back-контента
в семантическом дереве — тот самый «regression test where the runner allows» из требования R1.
**FC2-05 (iOS/macOS, уточнение таймера до реального угла).** `setFlipped` в
`iosApp/PolskiGrammar/FlashCardView.swift:59-70` и его зеркало в
`macosApp/PolskiGrammarMac/MacFlashCardView.swift` сегодня планируют обмен граней через
`DispatchQueue.main.asyncAfter(deadline: .now() + 0.25)` — это приближение (верно только если
реальный ease-curve симметричен и не подвержен системным задержкам кадра), не показание угла.
Заменить на две последовательные `withAnimation` с колбэком завершения (API доступен с iOS 17/
macOS 14 — deployment target проекта, см. `RiveResearch.md` §2/generate_project.rb):
```swift
withAnimation(.easeIn(duration: 0.25)) { rotation = mid } completion: {
    showBack = newValue   // ровно на границе 90°, не по таймеру
    withAnimation(.easeOut(duration: 0.25)) { rotation = final }
}
```
`mid = 90` независимо от направления (середина пути 0↔180 — всегда 90°). Reduced-motion путь не
меняется (прямое присваивание, без анимации). **Доказательство:** новый XCTest (по образцу
`FlipRivePerfUITests`, но для корректности, не производительности) с тест-only множителем
длительности через `ProcessInfo.processInfo.environment["POLSKI_FLIP_DEBUG_SCALE"]` (по умолчанию
1, только под `#if DEBUG`) — скриншоты на ~40%/~60% первой фазы, ассерт на отсутствие/наличие
accessibility-текста обратной стороны. Не меняет доверенный `.rotation3DEffect`/anti-mirror `-180°`
механизм v1 (FC-11/12) — только источник границы обмена граней.

### 12.2 R2 — эффект самого оборота (`interactive_rings.riv`)

**FC2-06 (вендоринг).** `interactive_rings.riv` (1.5 KB, MIT, rive-ios Demo-App,
`/private/tmp/claude-501/riv2/picks/interactive_rings.riv`, SHA-256 `433dfddefc53917bb19e477d2b91188b56f75d9d68aa2a617fe3bd3bb8d408a0`)
копируется по тому же соглашению путей, что `confetti.riv`/`again.riv` (FC-15):
`composeApp/src/androidMain/res/raw/rings.riv`, `composeApp/src/webMain/resources/rive/rings.riv`,
`iosApp/PolskiGrammar/Rive/rings.riv`, `macosApp/PolskiGrammarMac/Rive/rings.riv`. Вход:
`State Machine 1[IsExpanded:bool]`.
**FC2-07 (запуск, по хостам, чисто host-local).** `IsExpanded=true` в момент старта первой фазы
оборота (0→90°, любой оборот — не только по рейтингу), `IsExpanded=false` по завершении второй фазы
(в т.ч. при обороте назад) — привязка ровно к тем же точкам, что уже открывает/закрывает
`applyFlip`/`AndroidFlipCard`'s `angle`-переход/`setFlipped` (FC2-01/04/05), без нового общего
сигнала. Оверлей — decorative, позади карточки по z-order (не над ней, в отличие от confetti/again —
кольца не должны перекрывать текст): web — 3-й `kind` в `rive-bridge.js`'s `EFFECTS`, но с булевым
инпутом, не триггером (нужен `setBooleanInput`-аналог `fire()`, срабатывающий на изменении
`data-rive-effect="rings:<0|1>"`); Android — переиспользовать `AndroidRiveOverlay`'s
`RiveAnimationView`, добавить `setBooleanState("State Machine 1", "IsExpanded", bool)`; iOS/macOS —
переиспользовать `RiveViewModel`, добавить `setInput("IsExpanded", bool)`.
**FC2-08 (гейты).** Reduced motion / measurement-вариант B — те же предикаты, что уже проверяют
FC-09/12/14/20 (`Motion.Reduced`/`accessibilityReduceMotion`/`?riveDisabled=1`/`@AppStorage`) — не
завести вторую копию условия.

### 12.3 R3 — два новых эффекта (кроме оборота)

**FC2-09 (Pick A, исправляет реальный дефект v1).** `RiveCatalog.md` §0.1 подтверждает: `again.riv`
(на самом деле `ui_swipe_left_to_delete.riv`) рисует непрозрачную сцену — тёмно-бирюзовый телефон на
все ~2.6 c поверх карточки на всех 4 хостах, а не «резкий, отличный от confetti» курс. Заменить на
"Check/Error" (Marketplace #2276, gytly, CC BY 4.0, не ремикс, 2.2 KB, SHA-256
`f9c21d280f85a985d127ed1d9c6ec9dbd9574cb66289c9ad208cba08597d028b`), прозрачный фон, входы
`State Machine 1[Check, Error, Reset: trigger]`. Файл остаётся по пути `again.riv` (стабильность
путей во всех 4 host-каталогах — `AndroidRiveOverlay.kt`, `rive-bridge.js`'s `EFFECTS.again`,
оба `RiveEffectOverlay.swift`), меняется только содержимое файла и имя триггера: `Error` для
`CardEffect.Again`, `Check` для `CardEffect.Remembered` (см. открытый вопрос §12.7 — вместе с
confetti или вместо него). `cardEffectFor`/`CardEffect`-маппинг (FC-01) не меняется.
**FC2-10 (Pick C, только «Tada» — завершение цепочки).** Вендорить "Rive's animated emojis"
(Marketplace #1714, JcToon, CC BY 4.0, не ремикс, 59 KB/19 KB gzip, SHA-256
`57741d5f290b3e34f92f69936a16759ecec8d01b40cb065839e784c832cd24ea`), артборд `Tada`, анимация
`Reveal` (по имени, без state machine — `play("Reveal")`/`animations:['Reveal']`, не триггер).
Точка запуска — переход `state.phase` **в** `CardPhase.ChainComplete` (уже существующий домейн-
enum, `AppUiState.kt:13`, уже отрисовывается: web `renderChainComplete`
(`TrainingWebApp.kt:474/395`), Android `AndroidInfoCard(courseChainPresentation.completion.title)`
(`AndroidTrainingScreen.kt:125`), iOS `case "ChainComplete":` (`PolskiGrammarApp.swift:566`), macOS
`state.phase == "ChainComplete"` (`PolskiGrammarMacApp.swift:506`)) — чисто host-local наблюдение
за уже читаемым полем, без нового `CardEffect`/`onEffect`. Гейты — те же reduced-motion/вариант-B
предикаты. Монтировать на экране завершения цепочки, не на самой карточке.
**FC2-11 (осознанно в backlog, не в этом проходе).** Pick B («AI Orb Mascot», data binding) требует
нового per-host пути (`ViewModelInstance`/data-binding API), расходящегося с уже устоявшимся legacy-
триггерным паттерном всех overlay (FC-16/17/18) — сам `RiveCatalog.md` §3 отмечает Android/iOS
вызовы как **[verify on hosts]**/непроверенные; заслуживает отдельного прохода, не смешивания с
этим. Pick D (`riveslider.riv`, метр свайпа) требует непрерывной подачи прогресса драга в число во
время самого жеста — все три жестовых детектора (`detectFlipOrSwipe`, `DragGesture` iOS/macOS,
`WebSwipeRating`) сегодня отдают колбэк только по завершении жеста, а не на каждое перемещение —
более крупная переработка жеста, чем что-либо ещё в этом плане. Pick E (`robo_dude.riv`, «всё
повторено») не имеет естественного якоря: `CardPhase.NoDue` рендерится инлайн-текстом
(`renderChainComplete`/`AndroidInfoCard`/строковые ветки iOS/macOS), не отдельным экраном с местом
под маскота. Ни один из трёх не получает FC2-ID в этом проходе.

### 12.4 Вендоринг и `THIRD_PARTY/credits.md`

**FC2-12.** Скопировать из `/private/tmp/claude-501/riv2/picks/`: `interactive_rings.riv` (FC2-06),
`2276-4497-checkerror.riv`→`again.riv` (FC2-09, заменяет текущий файл), `1714-4322-rives-animated-
emojis.riv`→новое имя, например `chain-complete.riv` (FC2-10), в каждый из 4 host-каталогов (тот же
список путей, что FC-15). Обновить `THIRD_PARTY/credits.md`:
- Добавить строку CC BY 4.0 для `confetti.riv` (Marketplace #1456 «Confetti Explosion»,
  danny.jamesbuckley, https://rive.app/marketplace/1456-2840-confetti-explosion/), не убирая
  существующую строку MIT (repository) — обе верны: MIT покрывает копию из репозитория rive-ios,
  которую мы фактически вендорим, CC BY — лицензию и требование credit оригинала на Marketplace.
- Заменить строку `again.riv` на новый credit (CC BY 4.0, gytly, #2276, SHA-256 выше), одной строкой
  отметив замену `ui_swipe_left_to_delete.riv` (причина — RiveCatalog.md §0.1, не эстетика).
- Добавить строки для `interactive_rings.riv` (MIT, rive-ios Demo-App, SHA-256 выше) и
  `chain-complete.riv` (CC BY 4.0, JcToon, #1714, SHA-256 выше). Хэши вендоренных байт проверяются
  разработчиком при копировании (значения выше — из инспекции `RiveCatalog.md` §6, не с потолка).
**FC2-13 (исследовательский артефакт).** [`Plans/Kotlin/RiveCatalog.md`](RiveCatalog.md) — уже
скопирован этим архитектурным проходом из `/private/tmp/claude-501/rive-catalog.md` (полный каталог
из 453+96 инспектированных `.riv`, лицензии, ранжированный шорт-лист, backlog §5) — тот же принцип,
что `RiveResearch.md` (закоммиченное исследование, не черновик).

### 12.5 R4 — измерительные пробелы

**FC2-14 (web).** Расширить `flip-rive-perf.spec.ts`/измерительный spec CDP-троттлингом CPU:
`const cdp = await page.context().newCDPSession(page); await cdp.send('Emulation.setCPUThrottlingRate',
{rate})` для `rate ∈ {1,4,6}`, повторяя существующий захват frame-time/long-task на каждой ставке —
v1's tester evidence (`v1-measurements.json`) явно объясняет плоские 16.7 мс/0% janky именно
отсутствием троттлинга (headless Chromium без нагрузки никогда не превышает кадровый бюджет).
**FC2-15 (Android, вариант B становится достижимым).** Заменить
`System.getProperty("polski.debug.riveDisabled")` (недостижим снаружи процесса — реальный пробел,
зафиксированный tester'ом в `v1-measurements.json`'s android-секции) на тот же паттерн, что уже
работает на iOS/macOS: `#if DEBUG`-аналог — `BuildConfig.DEBUG`-гейтед пункт в существующем экране
настроек (тот же файл-семейство, что `AndroidUserPreferencesStoreTest`), скрытый за long-press,
зеркалируя `IosSettingsView`'s скрытый `@AppStorage`-тоггл — достижим реальным
`adb shell input tap`, а не недостижимым JVM-свойством.
**FC2-16 (Android, латентность первого эффекта).** Обернуть конструирование `RiveAnimationView` и
первый вызов `fireState` в `AndroidRiveOverlay.kt` в `android.os.Trace.beginSection("RiveFirstEffect")`
/`endSection()` (видно в Perfetto/`dumpsys gfxinfo`, тот же приём, что `RiveResearch.md` §5
предлагает через `trace("RiveFileLoad"){}`). У легаси `RiveAnimationView` нет `onLoad`-колбэка (в
отличие от web/iOS) — это измеряет «конструктор → возврат из `fireState()`», не «до первого
отрисованного кадра»; зафиксировать разницу явно, не выдавать одно за другое.
**FC2-17 (iOS/macOS).** Добавить `os_signpost`-интервалы в `FlashCardView.setFlipped` (обе фазы,
FC2-05) и в оба `RiveEffectOverlay.swift` (создание `RiveViewModel` + `triggerInput`), через
`OSLog(subsystem: "dev.polski.grammarmatrix", category: "flip")`, `#if DEBUG`/perf-сборка (риск уже
отмечен в `RiveResearch.md` §5) — закрывает именно тот пробел, который обе tester-проходы v1 явно
пометили как «NOT RUN, no signposts exist» (`v1-measurements.json`'s ios/macos-секции).

### 12.6 v2 протокол измерений (уточнение §5 v1)

**FC2-18.** Варианты переименованы буквально по формулировке задачи: **no-animation** (= v1 A),
**native-flip** (= v1 B, теперь достижим на Android — FC2-15), **flip+rive-all** (= v1 C, включая
rings/Check-Error/Tada). Web: 1×/4×/6× CPU throttle × 3 варианта × {js, wasm} (FC2-14). Android:
`dumpsys gfxinfo` + `Trace`-секции (FC2-16) + размер APK на релиз-подобной сборке с
`abiFilters "arm64-v8a"` (не debug/все-4-ABI, как в v1 — та цифра переоценивает реальный прирост на
устройстве в 3-4 раза, см. v1's android tester-заметка). iOS/macOS: `XCTOSSignpostMetric` вокруг
новых signposts (FC2-17); для macOS сохраняется задокументированный в v1 §11 пробел с Accessibility
— если `osascript`/XCUITest всё ещё не может управлять приложением (та же `-1719`/`-1728` ошибка,
которую tester получил дважды независимо), запасной вариант — ровно тот, что просит сама задача:
(a) новый macOS UI-test таргет (по образцу iOS `PolskiGrammarUITests`, `generate_project.rb` уже
умеет добавлять SPM-пакеты тем же `xcodeproj` API, FC-17 v1) или (b) `#if DEBUG` авто-плей луп внутри
приложения (кнопка, которая сама выполняет N оборотов/эффектов по таймеру и логирует signposts) —
какой из двух реально снимает блокировку, решает человек с доступом к Accessibility.

### 12.7 Открытые решения

- **Pick A:** `Check` вместе с confetti или вместо него для `Remembered`? Рекомендация — вместе
  (`Check` подтверждает правильность быстро, confetti — праздничный акцент), но это продуктовое
  решение, не архитектурное — подтвердить с пользователем при реализации.
- **Кольца по z-order позади карточки** — если на тёмной теме мягкий лавандовый цвет визуально не
  читается, пользователь заранее одобрил запасной вариант: форкнуть `muazkadan/Rive-CMP`'s
  `wasmJsMain`-interop (`RiveResearch.md` §1) — но только если ручной `rive-bridge.js`/нативные
  адаптеры реально не тянут булевый инпут (строгое подмножество уже работающих триггеров), что не
  ожидается.
- **Streak/«Onfire»** (вторая половина Pick C) не имеет домейн-концепции в `shared` вообще (нет ни
  одного streak-счётчика в `shared/src/commonMain`) — осознанно не включён в FC2-10, а не придуман
  ради использования артборда, который просто есть в том же файле. Что считается «streak» (подряд
  идущие Good, в рамках сессии или между сессиями) — отдельное продуктовое решение.
- **FC2-16** — задокументированное приближение (нет `onLoad`-аналога в легаси Android view); точный
  замер требует перехода `AndroidRiveOverlay` на новую Compose `Rive(...)`-композабл с data binding
  — более крупная миграция, чем v2 (см. `RiveResearch.md` §2), не делается в этом проходе.

## 13. Evidence log — web v2 (Developer, реализовано)

Реализовано для web-хоста: FC2-01/02/03 (точный обмен граней на 90° вместо мгновенного — реальный
найденный баг v1, не гипотеза), FC2-06/07/08 (кольца `rings.riv`, синхронизированные с фазами
оборота, R2), FC2-09 (замена `again.riv` на "Check/Error" #2276, `Check` вместе с confetti для
`Remembered`, `Error` для `Again` — принята рекомендация "вместе" из §12.7), FC2-10 (одноразовая
"Tada"-анимация на экране завершения цепочки, R3), FC2-12 (вендоринг трёх новых файлов +
обновление `THIRD_PARTY/credits.md`), FC2-14 (CDP `Emulation.setCPUThrottlingRate` 1×/4×/6× в
`flip-rive-perf.spec.ts`). FC2-04 (Android), FC2-05 (iOS/macOS), FC2-15/16/17/18 (другие хосты) —
вне охвата этого прохода (задача — web).

**Изменённые/новые файлы:** `composeApp/src/webMain/kotlin/polski/ui/TrainingWebApp.kt` (обмен
граней переписан на `applyFlip`, кольца, "Tada"-триггер), `composeApp/src/webMain/kotlin/polski/ui/
RiveEffectOverlay.kt` (кольца/"Tada"-оверлеи, `flipHalfDurationMs()`/`reducedMotionActive()`
вынесены в top-level), `composeApp/src/webMain/resources/rive/rive-bridge.js` (мульти-canvas
эффект "Remembered", кольца как persistent boolean, единый subtree-observer), `training.css`
(`.card-flip-inner`/`.card-flip-rings` стекинг), **NEW** `rings.riv`, `chain-complete.riv`,
заменённый `again.riv`; `THIRD_PARTY/credits.md`; `tests/browser/kotlin-flip-card.spec.ts` (+4
новых теста), `tests/browser/flip-rive-perf.spec.ts` (+3 CPU-throttle теста).

**Отклонения от буквы плана (обоснованные, зафиксированы явно):**
- FC2-01's снипет использует Web Animations API (`inner.animate([...])`). Реализовано вместо этого
  через **две последовательные CSS-transition** (`transition`+`transform` inline-стили) с
  принудительным reflow (`inner.getBoundingClientRect()`) между установкой стартового угла и
  запуском перехода — та же причина, что FC-18 уже зафиксировал для Rive-моста: `Element.animate`
  не входит в типизированные DOM-биндинги, которые Kotlin/JS и Kotlin/Wasm гарантированно
  используют ОДИНАКОВО без `js()`/`dynamic` (которого в Kotlin/Wasm нет вовсе). CSS-transition
  подход достигает того же наблюдаемого поведения (двухфазный оборот, обмен граней ровно на 90°,
  симметричный ease-in/ease-out, мгновенный обмен при reduced motion) через уже используемые в
  файле типизированные API (`style.setProperty`, `getBoundingClientRect`, `addEventListener` с
  `(Event) -> Unit)`), что явно разрешено формулировкой требования R1 ("or equivalent angle-driven
  swap").
- Планового `transitionend`-слушателя оказалось недостаточно: см. RED ниже — переход на
  `window.setTimeout`/`clearTimeout` с той же длительностью (250 мс на фазу) вместо
  `addEventListener("transitionend", …)`.
- FC2-07's требование «кольца — decorative, позади карточки по z-order» реализовано буквально
  (`.card-flip-rings{z-index:-1}` внутри `.card-flip{z-index:0}`, подтверждено кор­ректным в
  изолированных repro с идентичными правилами — см. RED №3) — но в headless Chromium/SwiftShader
  (том же движке, которым управляет Playwright) сам Rive-canvas визуально красится ПОВЕРХ текста,
  несмотря на корректный вычисленный `z-index`. Не сумев подтвердить причину за разумное время
  (не CSS-ошибка — исключено тремя независимыми изолированными репро: 2D canvas, WebGL canvas,
  активный 3D `rotateY`-transform — все три ведут себя корректно вне приложения), принято прагматичное
  смягчение: `opacity:.32` на `.card-flip-rings` — кольца остаются лёгким, полностью читаемым
  сквозь них glow-эффектом независимо от того, красит ли конкретный движок его формально "за" или
  "перед" текстом. Задокументировано как известное ограничение измерительной среды (то же семейство
  оговорок, что и headless/SwiftShader во всех остальных §5-разделах плана), а не как решённая
  архитектурная задача.

**RED (обнаруженные и исправленные во время разработки регрессии, не гипотезы):**
1. **Зависание оборота при повторном тапе ровно на 90°.** Первая реализация (FC2-01) слушала
   `transitionend` для перехода между фазами. `kotlin-flip-card.spec.ts`'s «tapping the card flips
   it back…» тест (существовавший, из v1) стал детерминированно падать (3/3 повторов) на
   `expect(card.locator('.card-back')).not.toHaveAttribute('aria-hidden','true')` с таймаутом
   10 с. Причина подтверждена трассировкой Playwright (`console`-лог `DEBUG applyFlip
   toFlipped=true isFlipEvent=true currentAngle=90.0`, временно добавленный и удалённый после
   диагностики): второй тап прилетал через ~432 мс после первого — ровно в СЕРЕДИНЕ второй фазы
   первого тапа (уже прошедшей 90°-точку), поэтому `from` (=90, последняя зафиксированная
   контрольная точка) совпадал с `mid` (=90 всегда) нового перехода — CSS-переход к ТОЙ ЖЕ
   величине не создаёт видимого изменения стиля, и `transitionend` для него никогда не срабатывает
   в Chromium. Исправлено переходом на `setTimeout(callback, 250)`/`clearTimeout` вместо
   `transitionend`-слушателя — таймер срабатывает безусловно, независимо от того, изменилось ли
   визуальное значение перехода. Задокументировано в самом коде (`applyFlip`'s KDoc), не только
   здесь.
2. **Отсутствие обмена гранями до 90° на самом первом автообороте.** До правки FC2-01 (`aria-hidden`
   выставлялся сразу при создании) ответ формально был в DOM с `aria-hidden="true"`, но реальная
   проверка (`?flipDebugScale=20` + семплы на 40%/60% суммарной длительности) потребовала явного
   `swapAriaAndInert(front, back, !toFlipped)` СРАЗУ при старте анимации (а не полагаться на
   пред­ыдущее состояние атрибутов) — иначе на свежесозданном узле пара front/back вообще не имела
   `aria-hidden` ни на одной из сторон в первые 250 мс. Исправлено: `applyFlip` теперь синхронно
   переустанавливает pre-flip-пару перед стартом первой фазы.
3. **Кольца никогда не срабатывали для рейтинг-оверлея (реальный, не гипотетический баг).**
   Первая реализация `rive-bridge.js` использовала per-element `MutationObserver`, устанавливаемый
   один раз в `attach()` при первом запуске скрипта, с условием "элемент уже существует". Поскольку
   кольца (FC2-06) вызывают `ensureBridgeLoaded()` уже на САМОМ ПЕРВОМ обороте карточки — то есть
   до первой оценки, — скрипт загружался и `attach()` отрабатывал ДО того, как `#polski-rive-overlay`
   (создаётся только в момент первой оценки) вообще существовал в DOM; повторная попытка
   (`setTimeout(attach, 0)`) тоже срабатывала слишком рано. Итог: подтверждено вручную (canvas
   оставался `300×150` — HTML-дефолт, `console.log`-трассировка показала, что `playRating` вообще
   не вызывается) — confetti/Check/Error никогда не проигрывались, хотя атрибут `data-rive-effect`
   корректно выставлялся и Playwright-тест на сам атрибут (не на пиксели) ложно проходил. Это
   не было заметно в v1, потому что там `ensureBridgeLoaded()` вызывался только из `trigger()`
   (после оценки), когда оверлей уже гарантированно существовал. Исправлено: один
   `MutationObserver` на `document.body` с `subtree:true`, слушающий все три атрибута
   (`data-rive-effect`/`data-rive-chain`/`data-rive-rings`) сразу — не требует, чтобы целевой
   элемент уже существовал на момент подписки. Подтверждено визуально (скриншоты confetti+Check,
   Error-крест, "Tada"-конфетти — см. ниже) и через `canvas.width/height` (878×857, не дефолтные
   300×150) до и после исправления.

**Проверка (PASS/FAIL/NOT RUN):**

| Проверка | Команда (рабочая директория) | Результат |
|---|---|---|
| JS/Wasm target compile | `./gradlew :composeApp:compileKotlinJs :composeApp:compileKotlinWasmJs` (`kotlin/`) | PASS (0 ошибок, 1 пред-существующий warning) |
| Прочие таргеты (не должны сломаться — изменения только в `webMain`) | `./gradlew :shared:compileKotlinDesktop :shared:compileKotlinIosSimulatorArm64 :shared:compileKotlinMacosArm64 :composeApp:compileKotlinDesktop :androidApp:compileDebugKotlin :shared:jsTest :shared:wasmJsTest` (`kotlin/`) | PASS |
| Fresh distribution | `./gradlew :composeApp:composeCompatibilityBrowserDistribution` (`kotlin/`) | PASS; `rive/{confetti,again,rings,chain-complete}.riv`, `rive.js`, `rive.wasm`, `rive-bridge.js` присутствуют |
| Playwright, wasm, chromium, полный `testMatch` (107 тестов, включая 7 новых) | `KOTLIN_SPIKE_DIST=… KOTLIN_SPIKE_BRANCH=wasm npx playwright test --config=playwright.kotlin.config.ts --project=chromium` (корень) | PASS 107/107 |
| Playwright, js, chromium, полный `testMatch` | то же с `KOTLIN_SPIKE_BRANCH=js` | PASS (см. §13 продолжение ниже / отдельный прогон) |
| RED→GREEN: `kotlin-flip-card.spec.ts`'s «tapping the card flips it back…» (существовавший v1-тест) | `--repeat-each=3`, тот же конфиг | FAIL 3/3 (обнаруженная регрессия #1), затем PASS после `setTimeout`-фикса |
| Новый: обмен граней ровно на 90° (авто-доворот, `?flipDebugScale=20`, семплы 40%/60%) | `kotlin-flip-card.spec.ts:119` | PASS |
| Новый: обмен граней ровно на 90° (ручной тап назад) | `kotlin-flip-card.spec.ts:138` | PASS |
| Новый: кольца — lazy, `aria-hidden`, `pointer-events:none` | `kotlin-flip-card.spec.ts:152` | PASS |
| Новый: reduced motion гасит кольца отдельно от rating-оверлея | `kotlin-flip-card.spec.ts:165` | PASS |
| Новый: CPU throttle 1×/4×/6× (CDP), flip+rate остаётся отзывчивым | `flip-rive-perf.spec.ts` (3 новых теста) | PASS |
| Визуальное подтверждение (headless Chromium, ручные скриншоты) | Check (зелёная галка), Error (красный крест), confetti, кольца-glow, "Tada"-конфетти — все проигрываются и корректно исчезают | PASS (см. swap_evidence) |

**Известные ограничения:** визуальный z-order колец в headless Chromium/SwiftShader — см. отклонение
выше (смягчено `opacity`, не логическая ошибка). Полный количественный протокол §5/FC2-18 (frame
p50/p95/p99 по трём вариантам × трём CPU-ставкам, память, APK/App-size по другим хостам) не
собирался в этом проходе — LEAN MODE ограничил объём до R1–R4's заявленных для web пунктов
(корректность обмена, кольца, два новых эффекта, CPU-throttling scaffold); абсолютные
frame-timing числа при 1×/4×/6× не сведены в таблицу (тест проверяет функциональную устойчивость и
верхнюю границу long-task, не публикует сравнительные проценты — это отдельный, более длинный
измерительный проход по образцу `tests/perf/flip-rive-measurements.spec.ts`, а не часть этого).

**Размер бандла (initial load; сравнение HEAD `af126a6` (v1) vs это изменение,
`composeCompatibilityBrowserDistribution`, gzip, независимая пересборка baseline в disposable git
worktree):**

| Файл | До (v1) | После (v2) | Δ |
|---|---|---|---|
| `originJsComposeApp.js` | 792 838 B | 793 668 B | +830 B |
| `originWasmComposeApp.js` (JS-glue для wasm-ветки) | 99 724 B | 99 747 B | +23 B |
| Kotlin/Wasm `*.wasm` (composeApp, app-специфичный файл) | 744 919 B | 745 685 B | +766 B |
| `training.css` (eager, `<link>` в `index.html`) | 5 009 B | 5 491 B | +482 B |

Итого initial load вырос на ~2.1 KB (gzip) — весь новый код (обмен на 90°, кольца, "Tada"-триггер,
мульти-canvas rating-эффект) остаётся в уже существующих, eagerly загружаемых файлах, но прирост
пропорционально мал. Rive runtime + `.riv`-ассеты остаются **lazy** (не в initial load, подтверждено
`flip-rive-perf.spec.ts`'s "bundle keeps the Rive assets lazy" и новым тестом на кольца — ни один
`rive/*`-запрос не уходит до первого оборота):

| Файл (`rive/`, lazy) | До (v1) | После (v2) | Δ |
|---|---|---|---|
| `confetti.riv` | 2 208 B | 2 208 B | 0 (без изменений) |
| `again.riv` | 2 740 B | 1 081 B | −1 659 B (новый файл меньше старого) |
| `rings.riv` | — | 648 B | +648 B (новый) |
| `chain-complete.riv` | — | 19 021 B | +19 021 B (новый; используется только артборд "Tada", остальные — задокументированный backlog §5 `RiveCatalog.md`) |
| `rive-bridge.js` | 1 515 B | 2 939 B | +1 424 B (мульти-canvas эффекты, кольца, единый observer) |
| `rive.js` / `rive.wasm` | 95 049 B / 360 770 B | без изменений | 0 |

Итого lazy Rive-бандл вырос с ~452 KB до ~470 KB gzip — рост почти целиком объясняется
`chain-complete.riv` (одноразовая, редко срабатывающая анимация конца цепочки), при этом
`again.riv` стал МЕНЬШЕ (новый asset компактнее старого), а `confetti.riv`/рантайм не изменились.

### 13.1 Correction round — fix для reviewer blocker (кольца никогда не освобождались, утечка Rive-инстанса)

**Blocker (critical, ревьюер):** `RiveEffectOverlay.mountRings()` создаёт новый `<div class="card-flip-rings"><canvas>…` внутри `.card-flip` на **каждый** вызов `renderCard()`, а `TrainingDomRenderer.render()` полностью выбрасывает это поддерево через `content.textContent = ""` (`TrainingWebApp.kt:292`) на любое реальное изменение состояния — включая обычную смену `exerciseId` на следующую карточку, что происходит практически при каждой оценке (авто-доворот на reveal всегда создаёт кольцевой инстанс для этой карточки). Кэш инстанса на узле (`canvas.__polskiRive`, `rive-bridge.js`) не имел никакого явного `cleanup()`-вызова при отбрасывании узла — только `stopRating()` и `currentChain.cleanup()` вызывали `.cleanup()`, что подтверждено `grep`-ом файла до фикса. Прочтение вендоренного `rive.js` подтвердило, что именно `Rive.prototype.cleanup` — единственный путь к `stopRendering()`, который останавливает собственный рекурсивный `requestAnimationFrame`-цикл инстанса; без него цикл рисования продолжается вечно против отсоединённого от DOM canvas.

**Fix:** `rive-bridge.js`'s единый `MutationObserver` (уже существующий, `attach()`) расширен с `attributes`-only до `attributes + childList`, оставаясь `subtree: true`. Для каждой `childList`-мутации новая функция `disposeDetachedRings(removedNode)` сканирует удалённый узел и его потомков (`querySelectorAll('canvas')`, плюс сам узел, если это `<canvas>`) на признак `canvas.__polskiRive`; если найден — вызывает `.cleanup()` (в `try/catch`, как и остальные вызовы `cleanup()` в файле) и обнуляет ссылку. Признак `__polskiRive` ставится только на кольцевые canvas-узлы (rating/chain инстансы хранятся в модульных переменных `currentRating`/`currentChain`, не тегируются на canvas), поэтому безусловное сканирование безопасно и не задевает другие эффекты. Диф изолирован в `rive-bridge.js`: `playRings`/`react`/`scan` не менялись; добавлена только `disposeDetachedRings` и правка регистрации observer'а. Тестовый счётчик `window.__polskiRiveRingDisposals` (инкрементируется при каждой реальной утилизации) добавлен по той же конвенции, что и `?flipDebugScale=`/`?riveDisabled=1` — узкий, безвредный test-only хук, позволяющий Playwright-тесту убедиться в реальной утилизации без обращения к внутренностям `rive.js` (нет прямого способа снаружи проверить, что `requestAnimationFrame`-цикл остановлен).

**RED→GREEN:** новый тест `kotlin-flip-card.spec.ts`'s «ring cue Rive instance is disposed, not leaked, when the card is replaced» (после первого reveal и загрузки `rings.riv`, оценка «Вспомнил» → следующая карточка → `window.__polskiRiveRingDisposals > 0`). Проверено RED: с временно откаченной `disposeDetachedRings`/observer-регистрацией (childList-ветка удалена) тест падает по таймауту (`Timeout 10000ms exceeded`, счётчик остаётся `0`) — воспроизведено детерминированно. После восстановления фикса — PASS.

**Проверка (PASS/FAIL/NOT RUN), только затронутое (LEAN MODE):**

| Проверка | Команда (рабочая директория) | Результат |
|---|---|---|
| Fresh distribution (пересборка с фиксом) | `./gradlew :composeApp:composeCompatibilityBrowserDistribution` (`kotlin/`) | PASS |
| RED: новый тест с откаченным фиксом | `KOTLIN_SPIKE_DIST=… KOTLIN_SPIKE_BRANCH=wasm npx playwright test --config=playwright.kotlin.config.ts tests/browser/kotlin-flip-card.spec.ts -g "ring cue Rive instance is disposed" --project=chromium` (корень) | FAIL (обнаруженная утечка воспроизведена; `Timeout … Received: 0`) |
| GREEN: тот же тест с фиксом | то же | PASS (1.2s) |
| `kotlin-flip-card.spec.ts`, wasm, полный файл (12 тестов) | то же без `-g` | PASS 12/12 |
| Playwright, wasm, chromium, полный `testMatch` | `KOTLIN_SPIKE_BRANCH=wasm npx playwright test --config=playwright.kotlin.config.ts --project=chromium` (корень) | PASS 108/108 (107 + 1 новый регрессионный тест) |
| Playwright, js, chromium, полный `testMatch` | то же с `KOTLIN_SPIKE_BRANCH=js` | PASS 108/108 |

**Размер (lazy `rive-bridge.js`, gzip -9, изолированная дельта фикса):** 3 039 B → 3 601 B (+562 B, +1 596 B до сжатия) — новая функция `disposeDetachedRings`, обновлённая регистрация observer'а, test-only счётчик и уточнённые doc-комментарии. Файл остаётся lazy (не в initial load), initial-load дельта из §13 не меняется.

**Известные ограничения:** тест проверяет утилизацию через test-only счётчик (`window.__polskiRiveRingDisposals`), а не напрямую через остановку `requestAnimationFrame` (нет доступного снаружи API для этого без патчинга `rive.js`); это тот же класс компромисса, что и остальные test-only хуки в этом файле (`?flipDebugScale`). Другие хосты (Android/iOS/macOS) не затронуты этим раундом — кольца/`rive-bridge.js` существуют только на web.

## 14. Evidence log — android v2 (Developer, реализовано)

Реализовано для Android-хоста: FC2-04 (подтверждение, не правка — `angle >= 90f` уже управляет
реальной композицией граней; закрыт единственный реальный пробел, отсутствие regression-теста, а
не гипотетическая правка), FC2-06/07/08 (кольца `rings.riv`, боковой vs основной z-order — decorative,
позади карточки, через `AndroidFlipRingsOverlay`, синхронизированные с реальными точками начала/конца
анимации оборота, R2), FC2-09 (замена `again.riv` на "Check/Error" #2276 — тот же файл, что уже
вендорен web-хостом; `Check` вместе с `confetti.riv`'s `Trigger explosion` для `CardEffect.Remembered`,
`Error` для `CardEffect.Again` — та же пара "вместе, не вместо", что реализована на web, для host-
паритета), FC2-10 (одноразовая "Tada"-анимация через `AndroidChainCompleteOverlay`, на экране
завершения цепочки — `CardPhase.ChainComplete`, не на самой карточке, R3), FC2-12 (копирование трёх
файлов из уже провендоренных web-копий, с проверкой SHA-256 до/после — байт-идентичны; переименование
`chain-complete.riv`→`chain_complete.riv` для Android — дефис недопустим в имени Android-ресурса;
обновление `THIRD_PARTY/credits.md`), FC2-15 (реальный, реально доступный из `adb`/UI дебаг-тоггл
варианта B — `RiveMeasurementVariant` вместо недостижимого снаружи процесса `System.getProperty`,
плюс скрытый long-press в `AndroidSettingsScreen`, тот же паттерн, что уже работал на iOS/macOS),
FC2-16 (`android.os.Trace` вокруг конструирования `RiveAnimationView` и первого вызова `fireState`).
FC2-05 (iOS/macOS), FC2-17/18 (другие хосты) — вне охвата этого прохода (задача — android).

**Изменённые/новые файлы:**
`composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidFlipCard.kt` (`Animatable` вместо
`animateFloatAsState` — тот же наблюдаемый оборот, но с явной точкой «анимация началась»/«анимация
завершилась» для колец; `onRingsExpandedChange` параметр), `AndroidRiveOverlay.kt` (мульти-view
рейтинг-эффект — до двух `RiveAnimationView`, как у web-бриджа; **NEW**
`AndroidFlipRingsOverlay`/`AndroidChainCompleteOverlay`; `RiveFirstEffectTrace`), **NEW**
`RiveMeasurementVariant.kt` (host-local `mutableStateOf`-синглтон), `AndroidTrainingScreen.kt`
(проводка колец/`riveEnabled`/чейн-оверлея, замена `riveDisabledForMeasurement()` на
`RiveMeasurementVariant.riveDisabled`), `androidApp/.../MainActivity.kt` (скрытый long-press-тоггл
в `AndroidSettingsScreen`, за `BuildConfig.DEBUG`), `androidApp/build.gradle.kts`
(`buildConfig = true`; `ui-test-junit4`/`ui-test-manifest` test-зависимости;
`testOptions.unitTests.isIncludeAndroidResources = true`), **NEW**
`androidApp/src/debug/AndroidManifest.xml` (регистрирует `androidx.activity.ComponentActivity` —
см. RED ниже), **NEW** `androidApp/src/test/.../AndroidFlipCardComposeTest.kt`; вендоринг:
`composeApp/src/androidMain/res/raw/{rings.riv, chain_complete.riv}` (**NEW**), `again.riv`
(заменён); `THIRD_PARTY/credits.md`.

**Отклонения от буквы плана (обоснованные, зафиксированы явно):**
- FC2-09's формулировка допускала «Remembered играет либо confetti одна, либо confetti+Check» как
  открытый вопрос; реализовано **confetti+Check вместе** (не confetti одна), ради паритета с уже
  принятым web-поведением, а не потому что план обязывал это на Android буквально — расширение
  `AndroidRiveOverlay` с одного `RiveAnimationView` до списка specs (максимум 2) было небольшим
  дифом (`effectSpecs()` — чистая функция, `views.take(activeCount)`), не архитектурным изменением.
- FC2-16's дословная формулировка — один `Trace`-спан «конструктор → возврат `fireState()`». Как и
  сам план честно отмечает как приближение, этот спан пересекает произвольный человеческий
  reaction-time зазор между монтированием оверлея (композиция) и первой реальной оценкой (могут
  быть секунды или минуты) — это НЕ то же самое, что «время загрузки Rive-рантайма», но именно то,
  что буква FC2-16 просит измерить; зафиксировано явно тем же комментарием в коде
  (`RiveFirstEffectTrace`'s KDoc), не выдано за нагрузочную метрику.
- FC2-06/07's «кольца — decorative, позади карточки по z-order» реализовано через порядок
  добавления в `Box` (Compose рисует по порядку объявления потомков — кольца добавлены первым
  потомком, `AndroidFlipCard` вторым), не через explicit z-index API (Compose UI не имеет такого,
  в отличие от CSS) — эквивалентное поведение для этой платформы.

**RED→GREEN (FC2-04, регрессионный Compose UI-тест):**
Роболектрик genuinely не резолвил `ActivityScenario.launch`/`createComposeRule()` из коробки в этом
проекте — не гипотеза, воспроизведено изолированным пробным тестом
(`ScenarioProbeTest`, временный, удалён после диагностики): `RuntimeException: Unable to resolve
activity for Intent {... cmp=dev.polski.grammarmatrix/androidx.activity.ComponentActivity}`.
Корень (подтверждён инспекцией `PackageManager.getPackageInfo(..., GET_ACTIVITIES)` изнутри теста,
затем `aapt2 dump xmltree` двух разных упакованных манифестов): `packageDebugUnitTestForUnitTest`
пакует манифест **основного debug-варианта**, а не `debugUnitTest`-специфичный смёрженный манифест
(тот, что `test_config.properties`'s `android_merged_manifest` указывает как текстовый файл) — тестовая
зависимость `ui-test-manifest` регистрирует `androidx.activity.ComponentActivity` только в
debugUnitTest-манифесте, который Robolectric для целей `PackageManager`/`ActivityScenario` не
использует; используется именно упакованный ресурс-APK debug-варианта. Фикс — зарегистрировать ту
же активность (тот же `exported` атрибут, чтобы не столкнуться при мёрдже) в **NEW**
`androidApp/src/debug/AndroidManifest.xml`, которая попадает в основной debug-манифест и,
следовательно, в упакованный ресурс-APK. После фикса тот же `ScenarioProbeTest` резолвит активность
(`activities=[androidx.activity.ComponentActivity, dev.polski.grammarmatrix.MainActivity]`).

Отдельно — генуинный RED для самого регрессионного теста (не инфраструктурный): временно ослаблен
порог в `AndroidFlipCard.kt` (`angle >= 90f` → `angle >= 200f`, недостижимо) — `backFaceIsNotComposedBefore90DegreesAndIsAfter`
падает (`AssertionError`, back-face node не найден на 80% пути), подтверждая, что тест
действительно управляется реальной композицией, а не проходит тривиально; откачено обратно на
`angle >= 90f`, GREEN восстановлен.

**Проверка (PASS/FAIL/NOT RUN), только затронутое (LEAN MODE):**

| Проверка | Команда (рабочая директория `kotlin/`) | Результат |
|---|---|---|
| `composeApp` компилируется (Android target) | `./gradlew :composeApp:compileAndroidMain` | PASS |
| `androidApp` компилируется | `./gradlew :androidApp:compileDebugKotlin` | PASS |
| RED: искусственно сломанный порог обмена граней | `./gradlew :androidApp:testDebugUnitTest --tests "…AndroidFlipCardComposeTest"` (с `angle >= 200f`) | FAIL (`AssertionError`, как ожидалось) |
| GREEN: тот же тест, порог восстановлен | то же (с `angle >= 90f`) | PASS |
| Полный набор Android unit-тестов | `./gradlew :androidApp:testDebugUnitTest` | PASS 34/34 (33 существующих + 1 новый) |
| `androidApp:assembleDebug` | `./gradlew :androidApp:assembleDebug` | PASS |
| Эмулятор smoke (Polski_ARM35, `emulator-5554`), реальное устройство: оборот, кольца, рейтинг-эффект, вариант B | `adb shell input tap/swipe` + `screencap` при `animator_duration_scale=10` (см. ограничение ниже) | PASS (визуально: кольца заметно увеличиваются в момент старта оборота и остаются увеличенными несколько кадров; лицевая/обратная стороны видимо меняются местами при повороте; `Тестовая версия` long-press переключает «Замер: Rive-эффекты отключены (вариант B)» и обратно) |

**Известные ограничения:**
- `Settings.Global.ANIMATOR_DURATION_SCALE` (`animator_duration_scale`) замедляет только
  View-based `ValueAnimator`/`ObjectAnimator`, а не Jetpack Compose's собственный кадровый клок —
  подтверждено эмпирически (обмен граней завершался за реальные ~500 мс независимо от scale=10, не
  за расчётные 5000 мс). Для точного замедленного mid-flip захвата на Android нужен либо
  Compose-специфичный test-only множитель длительности (по образцу web's `?flipDebugScale=`/iOS's
  `POLSKI_FLIP_DEBUG_SCALE`, не заведён в этом проходе — не входил в заявленный R1-объём для
  Android, поскольку FC2-04 уже было **подтверждением**, а не новой реализацией), либо
  Compose UI-тест с `MainTestClock` (то, что реализовано и PASS'ит выше) — эмулятор smoke остаётся
  визуальным подтверждением "оно вообще двигается и меняется", не точным протоколом углов.
- Визуально кольца не сжимаются обратно так же быстро, как расширяются (остаются увеличенными
  несколько секунд после завершения оборота на смоук-тесте) — код отправляет `IsExpanded=false`
  сразу по завершении `angleAnim.animateTo()` (подтверждено чтением кода, не гипотеза), но
  собственная кривая перехода `rings.riv`'s state machine для этого файла не измерялась отдельно;
  зафиксировано как наблюдение, а не как диагностированный баг — файл идентичен уже работающей
  web-копии (тот же `.riv`, тот же вход), так что поведение файла, а не проводки, наиболее вероятная
  причина.
- FC2-16's `Trace`-секция не верифицирована через `dumpsys gfxinfo`/Perfetto trace capture в этом
  проходе (LEAN MODE) — подтверждена только по коду (`beginSection`/`endSection` парность,
  `AtomicBoolean`-гейты); реальный захват трейса на эмуляторе не выполнялся.
- Рейтинг-эффект (confetti+Check/Error) не подтверждён скриншотом на реальном устройстве в этом
  проходе — попытка захвата совпала с автопереходом на следующую карточку (домен продвигается
  сразу по диспетчу `Rate`), окно оверлея оказалось короче цикла adb screencap; логика подтверждена
  юнит-тестами/чтением кода (`effectSpecs`), не отдельным визуальным доказательством.

## 15. Evidence log — ios v2 (Developer, реализовано)

Реализовано для iOS-хоста: FC2-05 (R1, реальный найденный баг, не гипотеза — см. RED ниже),
FC2-06/07/08 (кольца `rings.riv` через **NEW** `RiveFlipRingsOverlay`, синхронизированные с
реальными точками начала/конца анимации оборота через новый параметр `onRingsExpandedChange`, R2),
FC2-09 (замена `again.riv` на "Check/Error" #2276 — тот же файл, что уже вендорен web/Android;
`Check` вместе с `confetti.riv`'s `Trigger explosion` для `CardEffect.Remembered`, `Error` для
`CardEffect.Again` — та же пара "вместе, не вместо"), FC2-10 (одноразовая "Tada"-анимация через
**NEW** `RiveChainCompleteOverlay`, на экране завершения цепочки, не на самой карточке, R3),
FC2-12 (вендоринг `rings.riv`/`chain-complete.riv` в `iosApp/PolskiGrammar/Rive/`, копии
байт-в-байт с уже провендоренными web-копиями — подтверждено SHA-256 до/после; замена `again.riv`
на Check/Error, тоже байт-в-байт с web/Android; обновление `THIRD_PARTY/credits.md`), FC2-17
(`os_signpost`-интервалы вокруг обеих фаз оборота в `FlashCardView.setFlipped` и вокруг создания
`RiveViewModel`/`triggerInput` в `RiveEffectOverlay.swift`, `#if DEBUG`-only). FC2-04
(Android)/FC2-14 (web)/FC2-15/16/18 (Android/измерительный протокол) — другие хосты, вне охвата
этого прохода. macOS (FC2-05/06/07/08/09/10/17 для Mac) — вне охвата, не входил в задачу.

**Изменённые/новые файлы:** `iosApp/PolskiGrammar/FlashCardView.swift` (`setFlipped` переписан на
две последовательные `withAnimation(...) { } completion: { }` с реальным колбэком завершения вместо
`DispatchQueue.main.asyncAfter(0.25)`; `onRingsExpandedChange` параметр; `POLSKI_FLIP_DEBUG_SCALE`;
`os_signpost`), `iosApp/PolskiGrammar/RiveEffectOverlay.swift` (`again.riv`'s триггеры Check/Error;
**NEW** `RiveFlipRingsOverlay`/`RiveChainCompleteOverlay`; `os_signpost`), `PolskiGrammarApp.swift`
(проводка колец/Tada-оверлея, `ringsExpanded` состояние, сброс при смене карточки), вендоринг:
`iosApp/PolskiGrammar/Rive/{rings.riv, chain-complete.riv}` (**NEW**), `again.riv` (заменён);
`iosApp/generate_project.rb` (три новых `.riv`-ресурса; **NEW** тестовый файл
`FlipCorrectnessUITests.swift` зарегистрирован в UI-test таргете); **NEW**
`iosApp/PolskiGrammarUITests/FlipCorrectnessUITests.swift`; `THIRD_PARTY/credits.md`.

**RED (реальная регрессия, найдена и исправлена — не гипотеза, подтверждена `os_log`-таймстампами):**
Первая реализация FC2-05 (буквально по снипету плана — просто заменить таймер на два
`withAnimation(...) completion:`) провалила собственный новый тест: обмен граней настоящей
"первой" анимации (авто-доворот на `Reveal`, front→back) завершался за ~0.68–0.97 с при заданной
(через `POLSKI_FLIP_DEBUG_SCALE`) длительности фазы 5 с — обратный ход (back→front) при этом
корректно занимал полные ~5 с. Причина подтверждена добавлением временного `os_log`-трейсинга
(`Logger.fault`, снят после диагностики) и чтением `body`: существовавшая уже в v1 оптимизация
`else if rotation == 0 { frontFace }` (без `rotation3DEffect`-модификатора, введена ради другого
бага — Menu-style Picker's popup anchoring) означает, что **первый** оборот от `rotation == 0`
добавляет сам модификатор в дерево впервые — структурное изменение (другая ветка `if`/`else`,
другая identity), а не непрерывную мутацию свойства на уже смонтированном модификаторе; у
`withAnimation` нет "предыдущего кадра", от которого интерполировать — тот же класс бага, что
FC2-01/02 уже нашли и исправили на web ("no previous frame to transition from"), только в SwiftUI,
не в CSS. Обратный ход не задет, потому что после первого оборота `rotation` остаётся на 180, и
модификатор уже смонтирован. **Исправлено** двумя правками: (1) гейт "скип модификатора" сужен с
`rotation == 0` на `rotation == 0 && phase == "Question"` — ровно там, где реально существует
Picker, и ровно то условие, при котором оборот в принципе никогда не запускается; (2) сама первая
анимация в `setFlipped` завёрнута в `DispatchQueue.main.async`, чтобы транзакция, сменившая `phase`
(и тем самым впервые примонтировавшая модификатор), успела закоммититься собственным кадром
**до** того, как эта же функция анимированно меняет `rotation` — тот же архитектурный приём, что
FC2-02 формализовал на web (явно знать текущий угол и не полагаться на "предыдущий кадр браузера"),
адаптированный к SwiftUI-транзакциям. После обеих правок оба направления показывают полные ~5 с
(±0.1 с диспетчерской задержки) — подтверждено тем же `os_log`-трейсингом перед его снятием.

**Отклонения от буквы плана (обоснованные, зафиксированы явно):**
- FC2-05's снипет предполагал `mid = 90` и два `withAnimation` без доп. диспетчинга — реализовано
  с добавленным `DispatchQueue.main.async` перед первой фазой, по причине, найденной и описанной
  выше (RED); без него баг воспроизводится детерминированно на **каждом** первом обороте карточки.
- FC2-03's формулировка (CDP `Animation.setPlaybackRate`) специфична для web; на iOS использован
  уже принятый в v1 §10/§12.1 паттерн — `#if DEBUG`-only `POLSKI_FLIP_DEBUG_SCALE` через
  `ProcessInfo.environment`, читаемый только из `XCTest`'s `launchEnvironment`.
- Доказательство FC2-05 потребовало доп. приёма, не описанного буквально в плане: `XCUIElement.tap()`
  сам блокируется на несколько секунд ("Wait for ... to idle" в `xcodebuild test`'s таймлайне) при
  тапе, запускающем длинную (сотни мс — десятки с) анимацию — подтверждено по `t = ...s` временной
  шкале самого `xcodebuild test`, не предположение. Наивная выборка "sleep(X) после `tap()`" на этом
  фоне ненадёжна: `tap()` сам съедает переменную, заранее непредсказуемую часть бюджета. Исправлено
  измерением от `Date()`, взятого **до** `tap()`, и досыпанием только оставшегося времени
  (`sleepUntilElapsed`), а не слепым `Thread.sleep` после возврата из `tap()`; длительность фазы
  увеличена до 15 с (вместо буквальных ~5 с) для устойчивого запаса поверх этого оверхеда.
- FC2-06/07's "кольца — decorative, позади карточки по z-order" реализовано через порядок объявления
  в `ZStack` (SwiftUI рисует по порядку потомков — кольца добавлены первым потомком, `FlashCardView`
  вторым), тем же приёмом, что уже применил Android (`AndroidFlipRingsOverlay`, §14) для той же цели.

**Проверка (PASS/FAIL/NOT RUN), только затронутое (LEAN MODE):**

| Проверка | Команда (рабочая директория `kotlin/`) | Результат |
|---|---|---|
| Regenerate project (3 новых `.riv`-ресурса + новый UI-test файл) | `arch -arm64 ruby generate_project.rb` (`iosApp/`) | PASS, `rings.riv`/`chain-complete.riv`/заменённый `again.riv`/`FlipCorrectnessUITests.swift` подтверждены в свежем `project.pbxproj` |
| Xcode build (Debug/simulator, все изменённые файлы + вендоринг) | `xcodebuild -project iosApp/PolskiGrammar.xcodeproj -scheme PolskiGrammar -destination 'platform=iOS Simulator,id=4384946F-9E6B-43D0-ADA3-CA219A3456B8' -derivedDataPath /private/tmp/polski-ios-dd CODE_SIGNING_ALLOWED=NO build` | PASS, `** BUILD SUCCEEDED **`; все 4 `.riv` подтверждены в собранном `.app` (`confetti.riv, again.riv, rings.riv, chain-complete.riv`) |
| RED: FC2-05's новый тест на первой реализации (только `withAnimation`, без `DispatchQueue.main.async`, гейт ещё `rotation==0`) | `xcodebuild ... test -only-testing:PolskiGrammarUITests/FlipCorrectnessUITests` | FAIL 2/2 (front→back: `pre-swap face must still be mounted`/`post-swap face must not exist` — оба ассерта на чекпоинте 40%), подтверждено `os_log`-таймстампами (~0.68–0.97 с вместо 5 с) |
| GREEN: тот же тест после обеих правок (гейт по `phase`, `DispatchQueue.main.async`) + исправленная тестовая выборка (`sleepUntilElapsed` от `Date()` до `tap()`, 15 с/фаза) | то же | PASS 1/1 (0 failures), 88.3 с |
| Полный затронутый regression-набор: новый корректностный тест + 3 существующих flip/swipe/chain-теста | `xcodebuild ... test -only-testing:PolskiGrammarUITests/FlipCorrectnessUITests -only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testTappingRevealedCardFlipsTwiceWithoutExtraReviewThenRatingCountsOnce -only-testing:.../testBinaryRatingSwipesAdvanceOnceInEachDirection -only-testing:.../testNativeChainCompletionShowsFiveAnswersAndKeepsFiveRatings` | PASS 4/4 (0 failures), 394.3 с суммарно, чистая `derivedData` |

**Известные ограничения:**
- FC2-17's `os_signpost`-интервалы подтверждены только по коду (парность `.begin`/`.end`,
  `#if DEBUG`-гейт) — реальный `xctrace`/`XCTOSSignpostMetric`-захват и корреляция с Rive-стоимостью
  не выполнялись в этом проходе (LEAN MODE); это тот же документированный пробел, что v1 §10/§11
  зафиксировали для iOS/macOS signposts, теперь с самими интервалами на месте, но без замера.
- Кольца/"Tada"-оверлей не подтверждены визуальным скриншотом на реальном/симуляторном устройстве в
  этом проходе — только компиляцией, `generate_project.rb`'s подтверждением ресурсов в `.app`, и
  логической проводкой (`onRingsExpandedChange`/`riveEffectsSuppressed`); то же ограничение, что
  Android §14 зафиксировал для своего рейтинг-эффекта (окно эффекта короче цикла ручного захвата).
- `RiveViewModel.setInput`/двух-конструкторный `RiveViewModel(fileName:animationName:artboardName:)`
  API подтверждены по исходнику SPM-пакета (`rive-ios` 6.27.0, `Source/RiveViewModel.swift`,
  закешированная копия в `~/Library/Caches/org.swift.swiftpm/repositories/`), не по документации —
  оба реально скомпилировались и слинковались в `** BUILD SUCCEEDED **` выше.
- Полный `PolskiGrammarUITests`-набор (24+ метода) не прогонялся целиком в этом проходе — LEAN MODE
  ограничил проверку до нового теста и трёх существующих сценариев, которые реально касается это
  изменение (flip/swipe/chain-complete), тем же принципом, что v1 §10 уже применил.
- Rive-CMP-форк (`muazkadan/Rive-CMP`) не понадобился — легаси `RiveViewModel`'s `setInput`/
  `triggerInput` (то же API, что уже использовал v1) полностью покрыли булевый инпут колец и оба
  триггера Check/Error без затруднений, соответствуя ожиданию `RiveResearch.md` §1.

**Размер приложения (`.app`, Debug/iphonesimulator, `iPhone 17 Pro` симулятор, сравнение v1 §10
baseline vs это изменение, чистая `derivedData`):**

| Метрика | v1 §10 baseline | После этого прохода | Δ |
|---|---|---|---|
| `PolskiGrammar.app` (`du -sk`, весь bundle) | 22 992 KB | 23 168 KB | +176 KB |
| Новые/заменённые `.riv` (`rings.riv` +`chain-complete.riv`, `again.riv` заменён 5.4 КБ→2.2 КБ) | — | rings 1.5 КБ + chain-complete 59 КБ − (5.4-2.2) КБ ≈ +57 КБ по содержимому | — |

Рост объясняется почти целиком новым `chain-complete.riv` (59 КБ) и `rings.riv` (1.5 КБ), за вычетом
уменьшения `again.riv` (заменён на файл втрое меньше — 2.2 КБ против 5.4 КБ); разница между этим и
измеренным `+176 KB` — округление файловой системы на блоках (`du` считает блоками, не байтами) и
незначительный рост скомпилированного бинаря от новых `RiveViewModel`/`os_signpost`-путей.
`RiveRuntime.framework` не изменился (уже был в v1, эта правка не трогает SPM-зависимость).


## 16. v3 — training expand-reveal, vocabulary flip, explicit Animations toggle, first-motion fix

Дата: 2026-09-26. Основание: финальный пользовательский запрос (A/B/C/D ниже), заменяющий
предыдущую договорённость «обе карточки переворачиваются» — теперь переворот остаётся только у
карточки слов, а учебная карточка получает другую анимацию раскрытия. v1/v2 (§0–§15) не меняются
этим разделом, кроме явно перечисленных правок. Приложение в разработке — миграций нет.

### 16.0 Контракт v3

- **A. Учебная карточка («Карточки») теряет 3D-оборот.** Ответ теперь раскрывается вниз под уже
  видимым вопросом, а не переворотом. Триггеры раскрытия: кнопка «Показать ответ» (было), Space
  (было) и **новый** — тап/клик по самой карточке-вопросу (см. FC3-02). Ровно один раз на карточку;
  ответ не существует в DOM (и тем более в accessibility-дереве) до раскрытия. Свайп влево/вправо
  по раскрытой карточке — «Повторить»/«Вспомнил», как раньше (FC-02, не меняется — зона свайпа
  осталась той же, только внутри нового контейнера). Confetti+Check/Error и разовая Tada на
  завершении цепочки остаются без изменений (FC2-09/10).
- **B. Карточка слов («Слова») получает настоящий оборот.** Весь объект (включая свою панель с
  кнопками оценки) поворачивается как одно целое — переиспользуется **буквально тот же** код, что
  раньше крутил учебную карточку (не копия, см. FC3-01). Клик на вопросе — раскрытие один раз +
  доворот; повторный клик — доворот назад чисто визуально (без ревью); свайп на раскрытой карточке
  оценивает один раз (без изменений — тот же узкий свайп-хинт, что был у слов раньше). Кольца
  `rings.riv` при обороте и те же эффекты оценки (confetti/Check/Error), которых у карточки слов
  раньше не было вовсе (см. FC3-04) — теперь есть.
- **C. Явный тоггл «Анимации» в настройках.** Включён по умолчанию; хранится в общих
  `UserPreferencesV2` (см. FC3-05/06). Выключен ⇒ Rive никогда не грузится (ни прогрев, ни
  rive.js/rive.wasm/*.riv, ни инициализация моста) и всё оставшееся движение (раскрытие, оборот)
  мгновенно. `Motion.Reduced`/системный reduced-motion — тоже мгновенно и без Rive, как раньше, но
  это **отдельный** от «Анимаций» гейт (можно выключить один без другого). `?riveDisabled=1` и
  `?flipDebugScale=<n>` продолжают работать как раньше.
- **D. Первое раскрытие/первый оборот не должны заикаться.** Диагноз и правка — см. §16.3.

### 16.1 A — почему раскрытие вниз, а не `clip-path`/`max-height`

Три реальных варианта анимать высоту от «есть контент» до «есть контент, но 0» и обратно без
`height:auto`-транзишена (CSS не умеет анимировать до/от `auto` напрямую):

1. **`grid-template-rows: 0fr → 1fr`** (выбрано, FC3-02). Контейнер — `display:grid` с одной строкой
   (`grid-template-rows`), единственный grid-item — сам контент; `0fr`/`1fr` — доли свободного
   места, а не абсолютные величины, поэтому браузер сам считает промежуточные высоты как доли от
   «естественной» высоты контента на каждом кадре транзишена — тот самый трюк, который анимирует
   fr-значения (браузеры, включая все три в `playwright.kotlin.config.ts`, поддерживают это уже
   несколько лет). Не требует JS-измерения высоты контента (в отличие от `max-height`, где нужно
   либо угадать достаточно большое значение, либо измерить `scrollHeight` через JS и анимировать до
   точного пикселя — второе работает, но требует синхронного layout-чтения на каждое раскрытие).
2. **`max-height` с JS-измерением.** Работает, но требует `element.scrollHeight` (forced layout)
   именно в момент раскрытия — тот же класс проблемы, что FC2-01 (нужен «предыдущий кадр» для
   транзишена), и более хрупко: контент с изменяемой высотой (например, разная длина текста ответа)
   даёт неточный `max-height`, если не пересчитывать при каждом ре-рендере.
3. **`clip-path: inset(100% 0 0 0) → inset(0 0 0 0)`.** Не требует измерения высоты, но не меняет
   **layout**-высоту контейнера — контент остаётся вырезанным места под собой не резервирует, то
   есть либо card растягивается на полную высоту рансвернутого контента с первого кадра (и просто
   визуально «показывается» clip'ом — не «раскрытие вниз», а иллюзия), либо нужен тот же trick с
   `grid-template-rows`/`max-height` под ним для реального изменения высоты — то есть `clip-path`
   решает не ту задачу, которую просит пользователь («карточка растёт вниз»).

`grid-template-rows` — единственный вариант, где растёт **реальная layout-высота** контейнера без
JS-измерения, поэтому выбран. Easing — `cubic-bezier(.16,1,.3,1)` (стандартный "ease-out-expo"-
подобный спринг-профиль, тот же тип кривой, что современные iOS/Material раскрытия используют для
"вырастающего" ощущения без библиотек физики) вместо линейного/обычного ease — соответствует
пользовательскому «spring-like ease-out».

**FC3-01 (переиспользование, не копия).** `FlipCard` (`CardFlip.kt`, новый файл) — весь код
`applyFlip`/`swapAriaAndInert`/`installCardFlip`, ранее приватный внутри `TrainingDomRenderer`
(v1/v2 §8/§12.1), вынесен как есть (без изменения самой логики 90°-обмена/reduced motion/Rive-колец)
в отдельный класс с состоянием на один экземпляр карточки (`currentAngle`/`pendingTimer`).
`VocabularyWebController` — единственный вызывающий теперь (учебная карточка больше не флипает).
`mountRings`/`setRingsExpanded` стали top-level функциями в `RiveEffectOverlay.kt` по той же причине
(нужны и `FlipCard`, и `RiveEffectOverlay`, без завязки на конкретный экземпляр оверлея).

**FC3-02 (раскрытие, web).** `TrainingDomRenderer.renderCard` (`TrainingWebApp.kt`): при
`CardPhase.Revealed` вместо `.card-flip`-обёртки строится `.card-answer-wrap > .card-back`; класс
`expanded` держит финальное состояние, класс `revealing` — только на самом кадре раскрытия (гейтит
CSS-стаггер, см. FC3-03), выставляется/убирается по той же схеме, что FC2-01: inline `0fr` →
`getBoundingClientRect()` (форсирует layout, коммитит "from"-кадр) → снятие inline-стилей, чтобы
включился stylesheet-транзишен к `.expanded{grid-template-rows:1fr}`. Тап по карточке-вопросу
(новый триггер, часть A) — `installTapGesture` (общий helper, вынесенный в `WebSwipeRating.kt` из
бывшего `installCardFlip`, см. FC3-01) на `.card-front`, отключён на экране `method-introduce`
(там уже есть отдельная кнопка «Перейти к заданию», тап по карточке до неё ничего не значит).

**FC3-03 (стаггер, web).** Три именованные группы прямых потомков `.card-back`
(эталон+источник/`eyebrow`+`answer-sentence`; разбор изменений/`change-list`+`method-feedback`;
панель оценки/`rule-focus`+`rating-label`+`ratings`+свайп-хинт) получают `opacity:0;
transform:translateY(10px); animation: answer-part-in .4s ease-out <delay> forwards` с
возрастающей задержкой (80/180/280 мс), но **только** под классом `.revealing` — обычный ре-рендер
уже раскрытой карточки (например, 30-секундный `RefreshTime`) класс не получает и стаггер не
реиграет. `[data-motion=reduced]`/`[data-animations=off]` глушат это как любую другую CSS-анимацию
(существующее блэнкет-правило `*{animation:none!important}`, расширенное на `data-animations=off`).

**FC3-04 (оборот слов, web).** `VocabularyWebController.renderCard` (`VocabularyWeb.kt`): при
`revealed` строится `.card-flip`/`.card-flip-inner`/`.card-face`-обёртка (идентичная разметка
учебной карточки v1/v2), `FlipCard`-инстанс на контроллер, `flippedItemId` сбрасывает состояние при
смене показанного слова (аналог `flippedExerciseId` v1). Оценка (`ratings`-кнопки и свайп-зона)
теперь также вызывает `RiveEffectOverlay.trigger`/`cardEffectFor` — у карточки слов раньше **не
было вообще никакого** Rive-эффекта на оценку (только сохранение в FSRS), это пробел v1/v2, который
v3 закрывает попутно как часть «те же эффекты оценки» из пользовательского запроса. Свайп-зона и её
приёмник событий (узкая `<p class="vocabulary-swipe-zone">`, не вся раскрытая площадь) — не
изменены, только теперь живут внутри `.card-back.card-face` вместо плоского `.vocabulary-answer`.

### 16.2 C — тоггл «Анимации»

**FC3-05 (shared).** `UserPreferencesV2.animationsEnabled: Boolean = true` — новое поле,
добавленное **в конец** списка полей (сохраняет позиционный вызов конструктора в
`UserPreferencesCodec.decode` рабочим без изменения порядка существующих). `fieldsV2` включает
`"animationsEnabled"`; `decode` читает его через уже существующий `optionalBoolean(key, default)` —
отсутствие поля (v1 **и** старый v2-документ без него) даёт `true` без отдельной ветки миграции,
ровно как просило требование «missing field = enabled». Тест
`animationsEnabledMissingMeansEnabled` (`UserPreferencesCodecTest.kt`) — decode `{"schemaVersion":2,
"coursePair":"pl-ru"}` без поля, decode v1-документа, round-trip с `false`, невалидное значение.
**FC3-06 (web wiring).** `WebPreferencesController.setAnimationsEnabled`; `SettingsWeb.kt` — новый
toggle `settings-animations` (существующий select `settings-motion` **переименован** в
интерфейсе с «Анимации» на «Движение» — оба контрола раньше делили русское слово «Анимации», что
стало неоднозначно с новым явным тоглом; `kotlin-preferences-settings.spec.ts`'s тест обновлён на
новую подпись). `WebAppearance.apply()` пишет `data-animations="on"/"off"` на `<html>` (тот же
паттерн, что уже существующий `data-motion`) и, на грани "было включено → стало выключено"
(`lastAppliedAnimationsEnabled`, не на каждый `apply()`), вызывает `disposeRiveOnDisable` —
top-level функция в `RiveEffectOverlay.kt`, которая инкрементит счётчик и пишет
`data-rive-dispose-all` на `<body>`; `rive-bridge.js`'s наблюдатель атрибутов (тот же
`MutationObserver`, что уже слушает `data-rive-effect`/`data-rive-chain`/`data-rive-rings`, теперь
плюс `data-rive-prewarm`/`data-rive-dispose-all`) вызывает `disposeAllRive()` — останавливает
`currentRating`/`currentChain`, чистит все canvas с `__polskiRive`, скрывает оверлеи. Гейт для
самого Rive — `motionInstantActive()` (`reducedMotionActive() || animationsDisabledActive()`),
используется везде, где раньше был только `reducedMotionActive()` (`riveGated`, `FlipCard.apply`,
`applyExpand`), плюс отдельно `[data-animations=off] *` добавлен в то же блэнкет CSS-правило, что
уже глушит анимации под `[data-motion=reduced]`.

### 16.3 D — первый reveal/flip не должен заикаться

**Диагноз (не гипотеза — см. §16.3 evidence, `first-motion.json`).** v1/v2 уже сделали Rive
полностью ленивым — ни `rive.js`, ни `rive.wasm`, ни любой `.riv` не запрашивались, пока
пользователь не совершит первое реальное действие (первый флип/реveal). Это означает, что **самое
первое** взаимодействие пользователя с картой в целой сессии — единственный момент, когда браузер
одновременно (а) впервые парсит/выполняет `rive-bridge.js`, (б) впервые фетчит и компилирует
`rive.wasm`, и (в) проигрывает саму CSS-анимацию раскрытия/оборота — то есть именно первый reveal
получает лишнюю работу на тех же кадрах, которых у второго и всех последующих reveal просто нет
(Rive уже загружен). `first-motion.json` (§16.4) подтверждает это числами: до правки reveal#1 при
4×-throttle даёт p95 33.3 мс/max 50 мс (2-3 пропущенных кадра), а reveal#2/flip#1/flip#2 в той же
сессии — стабильные ~16.7-16.8 мс (без пропусков); никакого «первый флип принципиально хуже»
эффекта после первого reveal в сессии не наблюдается.

**Правки:**
- **Прогрев вместо полной лени (FC3-07).** `prewarmRiveIfEnabled(animationsEnabled)`
  (`RiveEffectOverlay.kt`, top-level) вызывается из `LaunchedEffect(preferences.value.
  animationsEnabled)` в `TrainingWebApp()` — то есть сразу после первого коммита композиции, не
  привязано к первому клику пользователя. Сам прогрев **не блокирует** ничего синхронно: он лишь
  ставит `<body data-rive-prewarm="1">`; `rive-bridge.js`'s `schedulePrewarm()` откладывает
  реальную работу (`withRive`+`configureWasm`+одноразовый offscreen-инстанс `rings.riv`, тут же
  освобождаемый) на `requestIdleCallback` (с `setTimeout(200)`-фоллбеком там, где API нет —
  например часть версий WebKit) — то есть либо ждёт простоя движка, либо не позже 200 мс, но
  никогда не занимает кадры первого рендера. Это меняет ранее задокументированный (v2 §12.5/тест
  «no Rive asset should load before the first flip») контракт «ничего не грузится до клика» — v3
  сознательно заменяет его на «ничего не грузится, если Animations выключены; иначе грузится скоро
  после первого кадра, не после клика» (новый тест в `kotlin-flip-card.spec.ts` заменяет старый).
- **Никогда не блокировать CSS-движение на Rive (FC3-08).** Уже было так структурно (Rive-триггеры
  всегда fire-and-forget через DOM-атрибуты, никогда не await'ятся перед стартом транзишена) — v3
  не меняет этот путь, только явно документирует его как часть контракта D в комментариях
  `RiveEffectOverlay.triggerReveal`/`FlipCard.apply`.
- **Промоутить слои заранее (FC3-09).** `will-change:grid-template-rows` на `.card-answer-wrap` и
  `will-change:transform` на `.card-flip-inner` — теперь **безусловные** правила стилшита (не
  добавляются/убираются в момент анимации), поэтому композитор может создать слой заранее, а не
  впервые в момент первого реального транзишена. `first-motion.json`'s `before`-замер (§16.4) —
  без этого + без прогрева; именно там видно 50 мс max на reveal#1.
- **Не перестраивать DOM карточки в момент reveal/flip (FC3-10, уточнение объёма).** Полная
  перестройка `.route-content` при каждом изменении состояния — существующая, задокументированная в
  v1/v2 (§6 v1) архитектурная особенность всего рендерера, не только карточки; переписывать её
  целиком (сохранять DOM-узлы между рендерами) — несоразмерно этому проходу и не запрошено отдельно
  от самого reveal/flip. То, что реально устраняет джанк именно "на грани reveal/flip" — тот же
  forced-layout приём, что уже закрыл FC2-01 (явный "from"-кадр перед транзишеном), применённый
  теперь и к `grid-template-rows` (`applyExpand`), и это и есть содержание FC3-02/FC3-04 выше — без
  него самый первый reveal/flip **любой** карточки, а не только первый в сессии, схлопывался бы
  мгновенно вместо анимации (тот самый баг, который FC2-01 уже нашёл и исправил для флипа; v3
  переносит идентичный приём на новую aniмацию).

### 16.4 Измерения — первое движение (web, до/после)

`Plans/Kotlin/artifacts/flip-rive/v3/web/first-motion.json` (и разбитые `first-motion-before.json`/
`first-motion-after.json`). Chromium headless, CDP `Emulation.setCPUThrottlingRate` rate=4,
`requestAnimationFrame`-дельты + `PerformanceObserver('longtask')` за 900 мс окно от клика reveal/
flip. «before» — временно отключены прогрев (FC3-07) и `will-change` (FC3-09) на той же v3-кодовой
базе (не откат к v1/v2 — сравнение изолирует именно вклад D, не всю v3 сразу); «after» — текущий
код. Результат подтверждает диагноз §16.3: только reveal#1 регрессирует без правки (p95 16.7→33.3
мс, max 16.8→50 мс), reveal#2/flip#1/flip#2 в обоих замерах статистически неотличимы (уже были в
рамках кадрового бюджета). Indicative-only (headless + software throttling, не device-grade — тот
же caveat, что весь §5/v1).

### 16.5 Проверка (LEAN MODE — затронутое)

- `:shared:desktopTest --tests polski.preferences.UserPreferencesCodecTest` — PASS (5 тестов,
  включая новый `animationsEnabledMissingMeansEnabled`).
- `:composeApp:compileKotlinJs`/`:composeApp:compileKotlinWasmJs` — PASS (только 1 pre-existing
  redundant-cast warning, не из этого прохода).
- `:composeApp:composeCompatibilityBrowserDistribution` — PASS.
- Playwright (оба `KOTLIN_SPIKE_BRANCH=wasm` и `=js`, `--project=chromium`), по файлам (изолированно,
  чтобы не попадать на описанный ниже pre-existing flake):
  `kotlin-flip-card.spec.ts` (переписан: без флипа на учебной карточке, раскрытие один раз, тап-
  реveal, оборот и стаггер-Rive-эффекты у слов, тоггл Animations = 0 запросов) — 15/15 PASS на wasm
  и на js; `kotlin-vocabulary.spec.ts` (обновлён: served-`.wasm` список теперь явно исключает
  `rive.wasm`, ожидаемо прогретый прежде, чем тест успевает его проверить) — 11/11 PASS;
  `kotlin-preferences-settings.spec.ts` (обновлён: подпись select — «Движение», грамматический
  real-touch тест ждёт заселения transition/стаггера перед измерением координат) — 21-22/22 PASS
  (см. ниже); `flip-rive-perf.spec.ts` (переименовано на "expand-reveal", убран устаревший «lazy
  before first flip» тест, замещённый прогревом) — 6/6 PASS; плюс регрессия по
  `kotlin-training.spec.ts`/`kotlin-method-cycle.spec.ts`/`kotlin-binary-rating.spec.ts`/
  `kotlin-parity-ratings.spec.ts`/`kotlin-parity-chain.spec.ts` — 16/16 PASS.
- **Найденный pre-existing flake (не регрессия этого прохода).** `kotlin-preferences-settings.
  spec.ts`'s «narrow layout keeps the next training action visible…» иногда падает (~70px overflow)
  при запуске **вместе** с другими spec-файлами в одном воркере, но надёжно проходит в изоляции;
  воспроизведено бинарным поиском **на неизменённом `main`** (тот же провал тем же числом `70`) —
  то есть это существующая нестабильность тестовой инфраструктуры (по всей вероятности гонка layout-
  измерения с чем-то в самой SPA-навигации при повторных `page.goto` на тот же путь с другим hash),
  не что-то, что внесла v3. Не исправлено в этом проходе (вне заявленного объёма, LEAN MODE).

## 17. v4 — цельный оборот слов, свайп-первый рейтинг, анимированные табы/панели, полировка UI/UX

Дата: 2026-09-26. Основание — скриншот и текстовый фидбек пользователя на текущую v3-сборку (§16), а
не гипотеза: пользователь описал ровно то, что реально в коде (проверено чтением файлов ниже, не
только §16 текста плана — сам текст §16.0/16.1 утверждает то, что должно быть, но CSS этому не
соответствует, см. 17.1). Приложение в разработке — миграций нет. v0–v16 не переоткрываются, кроме
перечисленных правок.

### 17.0 Контракт v4 (пункты фидбека → решения)

| # фидбека | Решение |
|---|---|
| 1. Оборот — не «контент во статичной рамке» | 17.1: панель (фон/рамка/радиус/тень) переносится на сами лица `.card-face`, а не на статичный `.card`/`.vocabulary-card` |
| 2. Убрать кольца в центре при обороте и на reveal | 17.2: `mountRings`/`setRingsExpanded`/`rings.riv` полностью удаляются из обоих путей; confetti/Check/Error/Tada не трогаются |
| 3. Свайп влево/вправо на телефоне, без кнопок; на PC — свайп мышью + кнопки + стрелки | 17.3: единый «карточка тянется за пальцем/мышью» жест на обоих хостах, кнопки визуально свёрнуты (не убраны из a11y-дерева — см. 17.3.6) под `(pointer: coarse)`/узким вьюпортом, `ArrowLeft`/`ArrowRight` добавлены в клавиатуру обоих хостов |
| 4. Анимированное переключение табов | 17.4: View Transitions API (уже полностью в стабильных Chromium/Firefox 144+/Safari 18+ same-document, см. ниже) + CSS-кроссфейд/slide-фоллбек + скользящий underline-индикатор в `nav.primary-nav` |
| 5. Анимация скрытия/раскрытия панелей | 17.5: тот же `grid-template-rows: 0fr↔1fr`-приём, что уже есть у reveal (§16.1), применяется к каталогу слов и к `reference-panel` |
| 6. Общая полировка | 17.6: чек-лист по существующим токенам `training.css`, без новой палитры |

### 17.1 Цельный оборот панели слов (UX4-01..04)

**Диагноз (не гипотеза — построчно из `training.css`).** `.card{background:linear-gradient(...);
border:1px solid var(--line);border-radius:14px;box-shadow:0 12px 36px #0002}` (строка 31) стоит на
**статичном** `<section class="card vocabulary-card">` (`VocabularyWeb.kt:121`), который не входит во
вращающийся `.card-flip-inner` — вращаются только `.card-front`/`.card-back` (`VocabularyWeb.kt:180-
186`), а у них в CSS **нет** ни `background`, ни `border`, ни `border-radius` — только `padding`
(строки 35/40). Визуально это ровно то, что видно на скриншоте: закруглённая панель стоит на месте,
контент внутри неё меняется. `§16.0/16.4` текста плана называет это «весь объект... поворачивается
как одно целое», но CSS этого не делает — расхождение плана и кода, не новая идея пользователя.

**UX4-01 (CSS).** В `training.css`: убрать `background/border/border-radius/box-shadow` из общего
правила `.card` **не трогая** его использование как простого не-вращающегося layout-контейнера
(`section.flashcard card`, `aside.card reference-panel`, `.session-complete`, `method-introduce`,
`.vocabulary-catalog` и т.п. — они не флипают и обойдутся тем же визуалом, поэтому чтобы не задеть
их, правильный ход — **не трогать** `.card`, а перекрыть его именно там, где он служит статичной
рамкой вокруг флипа): добавить `.vocabulary-card{background:none;border:0;box-shadow:none;padding:0}`
(конкретно для `section.card.vocabulary-card` — единственный сегодня host, где `.card` оборачивает
`.card-flip`) и перенести весь набор (`background`, `border`, `border-radius:14px`,
`box-shadow:0 12px 36px #0002`) на `.card-face` (общий класс обеих граней, `VocabularyWeb.kt:176/177`
уже проставляет `card-face` на оба). Это даёт: каждая грань — самодостаточная закруглённая панель со
своим фоном/рамкой/тенью, видна ровно одна за раз (`backface-visibility:hidden`), контейнер вокруг
неё не рисует вообще ничего — именно «весь объект переворачивается».
**UX4-02.** `.card-back` сегодня несёт `border-top:1px solid #365342` (визуальный разделитель
вопрос/ответ, нужный **только** там, где обе грани видны одновременно стопкой — это training-раскрытие
§16, не флип). У флипа (только словарная карточка теперь, §16.0/B) верхняя граница создаёт лишнюю
линию поперёк уже закруглённой сверху панели. Сузить это правило до `.card-answer-wrap .card-back`
(раскрытие) и не переносить его на `.card-face` вовсе — грань словарной карточки получает **только**
общую панельную рамку из UX4-01, без внутреннего разделителя.
**UX4-03.** `padding` у `.card-front`/`.card-back` (32px/28px по бокам) остаётся как есть — теперь
это внутренний отступ самой панели-грани, а не второй слой отступа внутри чужой рамки; визуально
размер карточки не должен измениться (padding суммарно тот же, просто раньше распределялся между
несуществующей внутренней и существующей внешней рамкой, а теперь — на одной).
**UX4-04.** Приёмка: DevTools/Playwright `getComputedStyle` на смонтированной словарной карточке —
у `.card-flip`/`.card-flip-inner`/родительского `.vocabulary-card` `background-color` прозрачный и
`border-style:none`; у активной (не-`aria-hidden`) `.card-face` — непрозрачный фон, `border-radius`
и `box-shadow`, идентичные на обеих гранях (одна и та же панель, независимо от того, какая грань
показана); визуальный скриншот-диф — рамка/скругление/тень движутся вместе с текстом при 90°-обмене,
а не остаются на месте. Существующая логика поворота (`FlipCard.apply`, 90°-обмен aria/inert, reduced
motion) не меняется — это чисто CSS-правка, ни одна строка Kotlin-логики оборота не тронута.

### 17.2 Убрать кольцевой Rive-эффект (UX4-05..07)

Пользователь просит убрать именно `rings.riv`(«interactive_rings», Pick F из `RiveCatalog.md` §3) —
и на самом обороте (`FlipCard.apply`, `CardFlip.kt:74/93`), и на training-reveal
(`RiveEffectOverlay.triggerReveal`). `RiveCatalog.md` §3 уже отмечает: «No free file is designed as a
card-flip VFX» — колечки были компромиссом v2/v3, не тем, что просил именно пользователь; теперь он
явно говорит убрать. Confetti/Check-Error (rating) и Tada (chain-complete) — другой, не затронутый
путь (`RiveEffectOverlay.trigger`/`triggerChainComplete`) и **не убираются**.

**UX4-05 (Kotlin).** `CardFlip.kt`: `apply()` перестаёт принимать `ringsLayer`/вызывать
`setRingsExpanded` (сигнатура сужается до `apply(inner, front, back, toFlipped, isFlipEvent)`);
`VocabularyWeb.kt:181` убирает `val ringsLayer = mountRings(flip)` и оба места, что его передают
(`renderCard`'s два вызова `flipCard.apply(...)`, строки 189/192). `RiveEffectOverlay.kt`:
`triggerReveal()` удаляется целиком (единственный вызывающий — `TrainingWebApp.kt:566`
`if (justRevealed) riveOverlay.triggerReveal(root, wrap)` — эта строка тоже удаляется); `mountRings`/
`setRingsExpanded` удаляются как мёртвый код (после UX4-05 у них не остаётся вызывающих).
**UX4-06 (ассеты/бридж).** `composeApp/src/webMain/resources/rive/rings.riv` — удалить файл;
`rive-bridge.js` — удалить ветку, слушающую `data-rive-rings` (`MutationObserver`, см. §16.2-описание
атрибутов) и функцию, которая проигрывает `interactive_rings`/`IsExpanded`. `training.css`:
`.card-flip-rings` правило (строки 58-66) — удалить; `.card-flip`/`.card-flip-inner`/`.card-face`
правила (56) остаются (сам оборот не убирается, только кольца). `THIRD_PARTY/credits.md`: убрать
строку с `interactive_rings`/rive-ios Demo-App (если это был единственный потребитель — проверить
перед удалением, что ни Android/iOS/macOS не используют тот же файл отдельно от web; если они его
не трогают этим проходом — оставить кредит с пометкой "web: снят v4", не удалять сам файл истории
кредитов задним числом).
**UX4-07.** Приёмка: Playwright — сетевой лог по флипу и по training-reveal не содержит запроса к
`rings.riv` (даже при прогретом Rive — прогрев (§16.3) тоже не должен greifen ring-логику, так как
вызывающих у неё больше нет); DOM после флипа/reveal не содержит `.card-flip-rings`/`canvas` с
`data-rive-rings`; rating-эффекты (confetti/Check/Error) и chain-complete Tada — без изменений
(регрессия по существующим `flip-rive-perf.spec.ts`/rating spec).

### 17.3 Свайп-первый рейтинг (UX4-08..20)

**Существующее сегодня (проверено, не предположение).**
`installTouchSwipeRating` (`WebSwipeRating.kt:16`) с `acceptAnyPointerType=true` уже висит на **всей**
задней грани training-карточки (`TrainingWebApp.kt:718`, мышь тоже работает); у словарной карточки —
тот же helper, но `acceptAnyPointerType=false` (только `coarse`-указатель) и только на узкой
`<p class="vocabulary-swipe-zone">`-подсказке (`VocabularyWeb.kt:206-213`), не на всей грани — то
самое «пунктирная плашка», которую видно на скриншоте. Клавиатурный `keydown`-обработчик
(`TrainingWebApp.kt:94-114`) существует **только** для `route == Training`; у словарной карточки
клавиатурной оценки нет вовсе. Кнопки оценки словарной карточки (`VocabularyWeb.kt:216-217`) — только
текст, без `<small>`-подсказки и без интервала (в отличие от training, `TrainingWebApp.kt:696-706`,
где есть оба) — отсюда «высокие пустые кнопки» на скриншоте: `.ratings button` — flex-column с
`gap:8px` под текст трёх строк, а тут всего одна строка.

**UX4-08 (единый жест-хелпер, замена/расширение `installTouchSwipeRating`).** Новый
`installSwipeCard(zone: HTMLElement, faceForTransform: HTMLElement, onRating: (Boolean) -> Unit)` в
`WebSwipeRating.kt` — заменяет `installTouchSwipeRating` на обоих хостах (helper переименован/
расширен, не дублирован; старое имя может остаться internal alias, если так проще миграции тестов).
Принимает **любой** primary pointer (свойство `acceptAnyPointerType=true` становится единственным
режимом — differentiation по типу указателя для *приёма жеста* больше не нужна: и телефон, и мышь
должны тянуть карточку). Логика:
- `pointerdown` (не на интерактивном потомке/сфокусированном поле — та же проверка, что уже есть,
  `editableTarget`) → запомнить `start`, `setPointerCapture`.
- `pointermove` → `dx = clientX - start.x`; если `|dx| > |dy|*1.25` (тот же анти-vertical-scroll
  критерий, что раньше проверялся только на `pointerup` — теперь непрерывно, чтобы решение
  «это горизонтальный жест» принималось раньше и не боролось с `touch-action:pan-y`, который остаётся
  на зоне как раньше и физически не даёт браузеру начать вертикальный скролл под пальцем, пока JS не
  решил иначе): установить `faceForTransform.style.transform =
  "translateX(${dx}px) rotate(${(dx/22.0).coerceIn(-8.0,8.0)}deg)"` и CSS-переменную
  `--swipe-progress` = `(dx / 75.0).coerceIn(-1.0, 1.0)` на `zone` (та же пороговая дистанция 75px,
  что уже используется — не новое число). `--swipe-progress` управляет через чистый CSS двумя
  псевдо-слоями подсказки (см. UX4-10), без лишних inline-стилей на них.
- `pointerup` → если `|dx| >= 75 && |dx| > |dy|*1.25`: commit — анимировать
  `faceForTransform` до `translateX(${sign*140%}) rotate(${sign*14}deg)`, `opacity:0` за ~220ms
  `cubic-bezier(.16,1,.3,1)` (тот же easing-токен, что уже выбран в §16.1 для reveal — не новый
  профиль), затем вызвать `onRating`; иначе — snap-back: убрать inline `transform`/`--swipe-progress`
  с тем же transition (220ms), карточка возвращается в 0. Под `motionInstantActive()` — обе ветки без
  transition (мгновенно), как и весь остальной motion в проекте.
- `pointercancel`/`lostpointercapture` → snap-back как при неуспешном releases.
**UX4-09 (перенос на обе карточки).** `TrainingWebApp.kt:718` и `VocabularyWeb.kt:206-213` вызывают
`installSwipeCard(zone = back /* вся грань */, faceForTransform = back, onRating = ...)` вместо
старого узкого `installTouchSwipeRating` на подсказке; сама грань (`.card-back`/`.card-face`) —
одновременно и зона жеста, и элемент, который визуально тянется — что и просил пользователь
(«карточка тянется/наклоняется за пальцем»). Текстовая подсказка `<p class="vocabulary-swipe-zone">`
превращается в чисто декоративный `aria-hidden` слой (уже так и есть), но его CSS (пунктирная
рамка, UX4-10) заменяется на едвовидимый статичный текст без своей рамки — сама рамка-подсказка была
частью жалобы («dashed hint box»), не нужна, когда вся карточка сама двигается.
**UX4-10 (тинт/подсказка при драге, CSS).** Заменить `.vocabulary-swipe-zone{...dashed...}`
(`training.css:126`) на два псевдо-слоя на самой грани, управляемых `--swipe-progress`
(custom property, выставляется в JS из UX4-08, читается только в CSS — ни одного лишнего inline-стиля
на самих слоях): `.card-face{position:relative}`, `.card-face::before,.card-face::after{content:"";
position:absolute;inset:0;border-radius:inherit;pointer-events:none;opacity:calc(var(--swipe-progress,0) * -1);
background:linear-gradient(to right, color-mix(in srgb, var(--before) 35%, transparent), transparent 40%)}`
(левый, «Again», активен при `--swipe-progress<0`) и симметричный `::after` с `var(--green)` слева
направо для «Good» (активен при `>0`) — `opacity` через `calc`/`clamp` так, чтобы отрицательный
прогресс не давал отрицательную opacity (`clamp(0,calc(var(--swipe-progress,0)*-1),1)` и
`clamp(0,var(--swipe-progress,0),1)` соответственно). Подпись («Повторить»/«Вспомнил») — два
`<span class="swipe-label swipe-label-again|good">` внутри грани (не псевдоэлементы — тексту нужен
контент), `opacity` и `transform:scale()` тоже через тот же `--swipe-progress` (`opacity:clamp(...)`,
`transform:scale(calc(.85 + .15*abs(var(--swipe-progress,0))))` — «растёт с дистанцией», как просил
пользователь). Оба цвета — существующие токены `--before`/`--green` (уже используются для
акцентов «было/стало» и ответов), не новая палитра.
**UX4-11 (кнопки — не убраны из a11y-дерева, визуально свёрнуты под `pointer:coarse`).** Ключевое
архитектурное решение, отличающееся от буквального «NO buttons» в фидбеке — обосновано ниже
(17.8.1): кнопки оценки остаются в DOM и в accessibility-дереве **всегда** (иначе TalkBack/VoiceOver
на телефоне, где наш кастомный pointer-свайп физически недоступен экранному диктору — единый
touch-жест на телефоне у VoiceOver/TalkBack зарезервирован под их собственную навигацию, — теряют
единственный способ оценить карточку; это прямое нарушение уже существующего правила kmp-web:
«Keep named buttons and keyboard actions equivalent»). Визуально под `@media (pointer: coarse),
(max-width: 480px)` кнопки переводятся в стандартный «visually-hidden»/`sr-only`-паттерн (не
`display:none`/`visibility:hidden`/`aria-hidden` — эти три реально убирают элемент из
accessibility-дерева, что и была бы регрессия): `.ratings{position:absolute;width:1px;height:1px;
overflow:hidden;clip-path:inset(50%);white-space:nowrap;margin:-1px}` — элемент нулевого визуального
следа, но фокусируемый и озвучиваемый. На fine-pointer/широком экране `.ratings` — обычная видимая
раскладка (без изменений расположения), но сама вёрстка кнопки получает содержимое, которого раньше
не было у словарной карточки (UX4-13) — «пустые высокие кнопки» с одной строкой текста больше не
воспроизводятся ни на одном хосте.
**UX4-12.** Приёмка a11y: с `pointer:coarse` — `.ratings` кнопки не видны на экране (0×0 клипнуты),
но `getByRole('button', {name: 'Повторить'})`/TAB-навигация до них всё ещё находит и активирует их
(Playwright `page.emulateMedia({ ... })` не эмулирует `pointer`, поэтому это ассерция через
`page.setViewportSize` + CSS `@media(max-width:480px)` ветку, а для собственно `pointer:coarse` —
через `page.evaluate` внедрение `matchMedia`-мок или через фактический mobile emulation профиль
Playwright, который выставляет `hasTouch:true` → браузер сам матчит `pointer:coarse`; отметить как
**[нужна проверка]**, какой из двух путей Chromium реально даёт в headless).
**UX4-13 (интервал в кнопках словарной карточки — устраняет «пустые» кнопки на fine-pointer тоже).**
Новый **NEW** `VocabularyCodec.preview(document, id, direction, scheduler, at): SchedulePreview` —
чистая функция (без сайд-эффектов, зеркалит `dueIds`'s способ получить `StoredCard` по
`cardKey(id, direction)`, но зовёт `scheduler.preview` вместо `scheduler.review`; тот же контракт,
что `Scheduler.preview` уже даёт `TrainingStore`, `TrainingStore.kt:367`). Юнит-тест — рядом с
существующими `VocabularySessionTest`/`VocabularyDocumentTest`: preview для нового id даёт те же
значения, что `dueIds`+`review` дали бы при реальной оценке (не дублирует FSRS-логику, только
маршрутизирует её). `VocabularyWebController.renderRevealedAnswer` зовёт его один раз на рендер,
передаёт `SchedulePreview` в кнопки — те получают ту же структуру `<small>hint</small><span>
interval</span>`, что уже есть у training (`intervalLabel`, переносимая как есть, она уже
top-level-совместима — просто общая, не приватная, функция).
**UX4-14 (клавиатура — теперь на обоих хостах, `ArrowLeft`/`ArrowRight` + существующие `1`/`2`).**
`TrainingWebApp.kt`'s единственный `keydown`-обработчик (строки 94-114) — единственное место, где
уже решены все конфликты (composing/IME/repeat/alt/ctrl/meta/`editableTarget`, см. существующий
guard) — расширяется, а не дублируется: убрать `route != WebRoute.Training` из условия выхода,
заменить веткой `when (route) { Training -> ...; Vocabulary -> ...; else -> return@keyboard }`, где
`Vocabulary`-ветка требует нового публичного метода на `VocabularyWebController`,
**NEW** `fun rateCurrentIfRevealed(rating: Rating, refresh: () -> Unit)` (тонкая обёртка над уже
существующим приватным `rate(id, rating, refresh)`, читающая текущий due `id` тем же способом, что
`renderCard` — не публикует внутреннее состояние, только принимает команду, тот же narrow-contract
принцип, что уже используется в этом файле для `close()`/`deactivate()`). `when (event.key)` в обеих
ветках получает `"ArrowLeft" -> Rating.Again`, `"ArrowRight" -> Rating.Good` рядом с существующими
`"1"`/`"2"` (не заменяет их — оба набора клавиш работают одновременно, как и просил пользователь).
`ArrowLeft`/`ArrowRight` НЕ должны срабатывать во время печатного ответа — уже покрыто тем же
`editableTarget(event.target as? Element)`-guard'ом, стоящим до всей ветки `if/else`, никакой новой
проверки не требуется (курсор в `<textarea>` продолжает штатно двигаться стрелками).
**UX4-15.** Приёмка (оба хоста, LEAN): короткий/вертикальный/до-reveal свайп не оценивает (как в
v1/FC-07, не регрессирует); ровно один полноценный свайп = одна оценка; `ArrowLeft`/`ArrowRight`
работают на обеих карточках вне печатного поля, не работают внутри него; кнопки оценки видимы и
рабочие на fine-pointer с интервалом, визуально свёрнуты, но доступны через Tab/screen-reader на
coarse-pointer/узком вьюпорте; тинт/подпись растут с `|dx|`, fly-out/snap-back анимируются под
нормальным motion и мгновенны под `Motion.Reduced`/Animations-off; ни один из существующих rating-
Rive-триггеров (`riveOverlay.trigger`) не меняет момент вызова — он остаётся на `onRating`, то есть
после commit жеста/клика по кнопке, как и раньше.

### 17.4 Анимированное переключение табов (UX4-16..19)

**Исследование (проверено, не по памяти).** Same-document View Transitions API — стабильно во всех
трёх движках уже на сегодня (2026-09-26): Chromium/Edge 111+, **Safari 18+** (macOS/iPadOS/iOS),
**Firefox 144+** (`caniuse.com/view-transitions`, MDN `View_Transition_API`, проверено WebSearch этим
проходом). Значит `document.startViewTransition` можно использовать как основной путь с CSS-
фоллбеком только для более старых версий тех же трёх браузеров — не как экзотическую прогрессивную
надстройку. Rive Marketplace: единственный найденный релевантный кандидат «tab bar» —
пост **#283 «Bottom Navigation Bar»** (автор Nader, не remix, 2021, `public.rive.app/community/
runtime-files/283-5137-bottom-navigation-bar.riv`, CC BY 4.0 по правилам маркетплейса из
`RiveCatalog.md` §1) — **не скачан и не инспектирован** этим проходом (прозрачность фона, набор
инпутов/артбордов, вес файла — неизвестны, см. 17.8.2); ни один из уже проинспектированных в
`RiveCatalog.md` §4.7 файлов (`clean_icon_set.riv` — иконки STAR/BELL/TIMER, не совпадают по смыслу
с «Карточки/Слова/Таблицы/Прогресс/Настройки») не годится напрямую. **Решение: CSS/View Transitions
для v4, Rive — в backlog** (соответствует «only if licensed, transparent and cheap; otherwise CSS»
из самого запроса — «cheap» здесь буквально не проверено, значит не берём в этот проход).

**UX4-16 (skeleton, `WebNav.kt`).** Добавить `<span class="nav-indicator" aria-hidden="true">`
как первый child `shell` (`nav.primary-nav`), позиционируемый `position:absolute` относительно
`nav{position:relative}`. `update(route)` (уже существует, строки 34-41) дополняется: после
проставления `active`/`aria-pressed`/`aria-current` — взять `getBoundingClientRect()` активной
кнопки относительно `shell`, выставить на индикаторе `transform:translateX(${left}px)` и
`width:${width}px` через inline-style (тот же forced-layout+inline-style паттерн, что уже
используется во флипе/reveal — тут он не нужен для «from»-кадра, потому что индикатор не пересоздаётся
между рендерами, только двигается — обычный CSS `transition:transform .25s cubic-bezier(.16,1,.3,1),
width .25s cubic-bezier(.16,1,.3,1)` в stylesheet достаточен). Под `motionInstantActive()` —
`transition:none` inline, как везде.
**UX4-17 (переход контента, `TrainingWebApp.kt`).** `render()` сегодня всегда просто
`content.textContent = ""` + перестройка (`TrainingWebApp.kt:273`) — этот путь остаётся для
любого обновления **в пределах** одного route (таймер, ре-рендер после оценки и т.п. не должны
триггерить переходную анимацию каждые 30 секунд). Новое: у вызывающего `render()` кода (там, где
известно, что *route изменился* — `WebRouteController`'s callback, уже приходящий в `App`-composable
как `navigate`-обработчик) оборачивать именно эту перестройку в `document.startViewTransition { ... }`,
если функция существует (`js("typeof document.startViewTransition === 'function'")`/аналог в Wasm —
**[нужна проверка]** точный синтаксис feature-detection для обоих таргетов, тот же класс вопроса, что
уже отмечен как открытый в RiveResearch/kmp-web для `dynamic`-интеропа) **и** не `motionInstantActive()`.
CSS: `::view-transition-old(root),::view-transition-new(root){animation-duration:.22s;
animation-timing-function:cubic-bezier(.16,1,.3,1)}` — стандартный crossfade по умолчанию достаточен
(усложнять до directional slide через `view-transition-name` на `.route-content` — не обязательно
первым проходом; slide — очевидное последующее усиление: `view-transition-name:route-content` на
контейнере плюс `::view-transition-old/new(route-content){animation:slide-out/.in .22s}` с направлением
из знака перехода между индексами `destinations`). **Фоллбек** (браузер без API, или motion instant):
текущее мгновенное `textContent=""`-поведение — уже корректный, просто немотанный, фоллбек; никакого
отдельного кода для него не нужно, кроме собственно `if (supported && !instant) transition else plain`.
**UX4-18 (клавиатурная/URL-навигация — тоже переходит).** `startViewTransition` оборачивает
перестройку независимо от того, что вызвало `navigate()` (клик по `nav`, `popstate`/`hashchange` из
`WebRouteController`, программный `dispatch(AppAction.SelectTab)` после `Settings`→`returnTo`) —
единая точка (внутри `render()`'а самого перехода между route, не внутри `WebNav`) гарантирует, что
Back/Forward тоже анимируются, а не только клик по кнопке. Это не создаёt новых `history`-записей —
`WebRouteController` их и так создаёт по клику (уже существующий контракт, не меняется).
**UX4-19.** Приёмка: клик по табу — контент кроссфейдит/слайдит ~220мс, `nav-indicator` синхронно
скользит под новую активную кнопку; `Motion.Reduced`/Animations-off/`prefers-reduced-motion` — тот же
переход без transition (мгновенно, `startViewTransition` либо не вызывается вовсе, либо вызывается
с `animation:none` через CSS — оба варианта корректны, тест проверяет итоговый DOM, не факт вызова
API); 30-секундный таймер/оценка карточки **не** триггерят переход (только реальная смена route);
браузер без `startViewTransition` (WebKit/Firefox старых версий, Playwright WebKit проверить отдельно
— **[нужна проверка]**, какую версию несёт текущий Playwright) — навигация работает идентично
сегодняшней, без ошибки в консоли.

### 17.5 Анимация скрытия/раскрытия панелей (UX4-20..22)

Переиспользуется **тот же** `grid-template-rows: 0fr↔1fr` + forced-layout приём, что §16.1 уже
установил для reveal (`applyExpand`) — не второй способ анимировать высоту. Кандидаты, найденные
чтением кода (не гипотетические): каталог слов (`catalogVisible`, `VocabularyWeb.kt:223-226`, кнопка
«Скрыть/Открыть каталог», уже с `aria-expanded`, но без анимации — `refresh()` просто убирает контент
инстантно) и `reference-panel` («Таблица под рукой»/«Скрыть таблицу», `TrainingWebApp.kt:373-378`,
тоже уже `aria-expanded`, тоже без анимации — целый `aside.card` то есть, то нет).
**UX4-20 (обёртка).** Обе кнопки продолжают вызывать существующий `refresh()`/`dispatch(...)` (логика
видимости не меняется — только то, как перестроенный DOM визуально появляется/исчезает). Новый общий
хелпер (top-level, рядом с `applyExpand`, либо буквально переиспользуемый как `applyExpand(wrap,
isRevealEvent = justToggled)` — сигнатура уже достаточно общая) применяется на: (а) `.catalog-list`+
всё, что рендерится после чекбокса «каталог» (сама секция, обёрнутая в `.card-answer-wrap`-подобный
grid-контейнер — переименовать по смыслу или буквально реюзать класс, раз механика идентична); (б)
`.reference-panel`, тоже оборачиваемая в тот же grid-wrap. Оба — и раскрытие, и скрытие — анимируются
(в отличие от reveal, который только раскрывает; здесь классу `expanded` соответствует и снятие: при
скрытии из `expanded` в неё же не добавленный класс — переход `1fr→0fr` идёт по тому же
`transition`, направление берётся из того, какое значение сейчас у `grid-template-rows`, CSS это уже
умеет без дополнительной ветки кода).
**UX4-21 (фокус, a11y).** При скрытии — если фокус был внутри скрываемого контента (например,
чекбокс каталога), переносить его на саму toggle-кнопку **до** начала transition (не после — иначе на
момент запуска анимации фокус на секунду «висит» на узле, который через мгновение получит `inert`/
уйдёт из потока); `aria-expanded` на кнопке — уже правильно обновляется существующим кодом, не
трогается. Скрытый контент во время `0fr`-состояния — не убирается из DOM (как и раскрытие сегодня),
поэтому а11y-дерево может недолго содержать «сжатый до нуля, но не `inert`» узел — та же оговорка,
что уже принята для reveal (§16.1 не ставит `inert` на пока-не-раскрытый `.card-back`, потому что он
там ещё не смонтирован вовсе; здесь контент **уже был** смонтирован и просто сжимается — нужно
явно поставить `inert`/`aria-hidden` на контент в момент, когда `grid-template-rows` уходит в `0fr`,
и снять при `1fr` — **новый** шаг, которого не было у reveal, потому что там скрытая грань не
существовала в DOM до раскрытия, а здесь существует и до, и после).
**UX4-22.** Приёмка: клик «Скрыть каталог» — список сжимается по высоте с плавным transition (не
мгновенно исчезает), фокус не «падает» в никуда; клик «Открыть каталог» — обратная анимация; то же
для «Таблица под рукой»/«Скрыть таблицу»; `aria-expanded` синхронен с видимым состоянием на каждом
кадре перехода, не только в начале/конце; `Motion.Reduced`/Animations-off — мгновенно; в сжатом
состоянии `inert`-контент не фокусируется через Tab и не озвучивается screen-reader’ом.

### 17.6 Полировка (UX4-23)

Большая часть списка из фидбека (spacing scale, `:focus-visible`, hover/active на кнопках, transition
150мс, `--focus`-outline, 44px `min-height` у `.primary-nav button`, светлая/тёмная тема через
`data-theme`, реакция на 320px/zoom через `overflow-wrap:anywhere`+`clamp()`-типографику) **уже
реализована** в `training.css` (проверено чтением, не предположение — конкретные строки процитированы
в §17.1-17.5 выше). Новое, что реально нужно добавить этим проходом — то, что уже перечислено в
17.1-17.5 (панель-на-грани, тинт/подпись свайпа, nav-indicator, collapsible-transition); отдельного
«стилевого прохода» сверх них не требуется — дублирующий чек-лист без конкретной правки был бы
именно тем «пустым архитектурным слоем», который принцип KISS (`module-architecture` skill) просит
не создавать. Единственный реально новый пункт — **UX4-23**: touch-target аудит после 17.3 — кнопки
`.ratings` на coarse-pointer визуально свёрнуты (UX4-11), но их «настоящий», видимый на fine-pointer
вариант должен остаться ≥44px по меньшей стороне (уже так, `.ratings button{min-height:102px}` на
mobile-медиа, десктопный вариант — проверить фактическую высоту после добавления `<small>`/`<span>`
из UX4-13, не только у training).

### 17.7 Затронутые файлы

`CardFlip.kt` (UX4-05, сузить сигнатуру `apply`), `VocabularyWeb.kt` (UX4-01/05/09/13, `preview`-вызов,
убрать `mountRings`), `TrainingWebApp.kt` (UX4-05/09/14/16-19, keydown-ветка, `startViewTransition`),
`WebSwipeRating.kt` (UX4-08, новый/расширенный `installSwipeCard`), `RiveEffectOverlay.kt` (UX4-05,
убрать `triggerReveal`/`mountRings`/`setRingsExpanded`), `WebNav.kt` (UX4-16, `nav-indicator`),
`training.css` (UX4-01/02/10/16/20, убрать `.card-flip-rings`/`.vocabulary-swipe-zone`-рамку), `rive/
rive-bridge.js` (UX4-06, убрать ring-ветку), `shared/.../vocabulary/VocabularyDocument.kt` (UX4-13,
**NEW** `VocabularyCodec.preview`), `THIRD_PARTY/credits.md` (UX4-06). Ничего в `androidMain`/
`iosApp`/`macosApp` не трогается (весь §17 — web-only, кольца у других хостов — вне охвата этого
прохода, если явно не попросят отдельно).

### 17.8 Открытые решения и риски

**17.8.1 (кнопки на touch — визуально скрыты, не убраны из a11y-дерева).** Буквальный фидбек
(«NO rating buttons» на телефоне) реализован **визуально** (UX4-11), но не как полное удаление из
DOM/aria — полное удаление нарушило бы уже принятое в этом проекте правило kmp-web («Keep named
buttons and keyboard actions equivalent») и WCAG-путь для VoiceOver/TalkBack, у которых собственный
жестовый слой конфликтует с произвольным pointer-свайпом веб-страницы. Если пользователь после
ревью настоит на буквальном удалении из DOM — это осознанный, явно принимаемый шаг назад по
доступности, не молчаливое расхождение; фиксируется здесь для явного решения, не решается этим
планом самостоятельно.
**17.8.2 (Rive-иконка табов — не проверена, backlog).** Пост #283 «Bottom Navigation Bar»
(17.4) — единственный найденный релевантный кандидат, но не скачан/не инспектирован (прозрачность,
инпуты, вес — неизвестны); v4 не берёт Rive для табов и полностью полагается на CSS/View Transitions
(что и удовлетворяет запрос буквально: «otherwise CSS/View Transitions»). Если понадобится вернуться
к этому — следующий шаг тот же пайплайн, что `RiveCatalog.md` уже применял (скачать в `riv3/`,
`inspect4.mjs`, рендер-харнесс с checkerboard-фоном для проверки прозрачности) до вставки в v4/v5.
**17.8.3 (feature-detection `startViewTransition` на JS/Wasm).** Точный синтаксис проверки наличия
API одинаково на обоих web-таргетах — **[нужна проверка]** этим проходом не выполнена (тот же класс
вопроса, что уже отмечен как открытый в `kmp-web`/RiveResearch для JS↔Wasm-интеропа); этот план решает
только *что* проверяется и *где* (17.4), не конкретный Kotlin/JS-синтаксис вызова.
**17.8.4 (Playwright/`pointer:coarse` эмуляция).** UX4-12 отмечает неопределённость в том, как
надёжнее всего заставить headless Chromium матчить `(pointer: coarse)` в тесте — решается
разработчиком/тестировщиком экспериментально, не архитектурным решением.

## 18. Evidence log — web v4 (Developer, реализовано)

Реализовано для web-хоста: UX4-01..04 (панель на грани — `.card-face` несёт
background/border/radius/shadow, `.vocabulary-card`/`.card`-обёртка ничего не рисует, весь
объект видимо поворачивается как одно целое), UX4-05..07 (кольцевой Rive-эффект полностью убран —
`mountRings`/`setRingsExpanded`/`triggerReveal`/`rings.riv`/`.card-flip-rings`/`data-rive-rings`
удалены; confetti/Check-Error/Tada не тронуты), UX4-08..15 (единый `installSwipeCard` — любой
primary pointer, живой drag-transform + `--swipe-progress`, растущие подписи, fly-out/snap-back;
кнопки визуально свёрнуты только под `(pointer:coarse)`, остаются в a11y-дереве;
`ArrowLeft`/`ArrowRight` рядом с `1`/`2` на обеих карточках через один keydown-обработчик;
`VocabularyCodec.preview`+интервалы в кнопках словарной карточки), UX4-16..19 (скользящий
`nav-indicator`, View Transitions на реальной смене route через `withViewTransition`, мгновенный
fallback без API/при instant-motion, 30-секундный таймер не анимируется), UX4-20..22 (общий
`.collapsible`-приём для каталога слов, двунаправленный, с `inert`/`aria-hidden` и переносом
фокуса на toggle до коллапса; reference-panel — однонаправленная fade+rise-анимация на появление,
без изменения её существующего условного монтирования).

**Отклонения от буквы плана (обоснованные, зафиксированы явно):**
- **UX4-11's `,(max-width:480px)` OR-условие убрано целиком** — оставлен только `(pointer:
  coarse)`. RED: `kotlin-method-cycle.spec.ts`'s клик по «2 Вспомнил» на 320px-вьюпорте (обычный
  Chromium, мышь, НЕ touch-эмуляция) стал недостижим — `.ratings` схлопывался до 1×1px по одной
  ширине вьюпорта, `elementFromPoint` резолвился в `.card-back` вместо кнопки (подтверждено
  побитовым сравнением: та же CSS-правка на ЧИСТОМ HEAD-коде, без единой Kotlin-правки, уже
  воспроизводит регрессию — см. проверку ниже). `max-width` путает «маленький телефон» с «узкое
  окно десктопного браузера» — второе держит `pointer:fine` и должно оставлять кнопки кликабельными
  мышью. `(pointer:coarse)` один точно матчит реальные touch/stylus-устройства независимо от
  ширины вьюпорта, что и просил пользователь буквально («на телефоне»).
- **`installSwipeCard`'s `zone` (слушатели жеста) — не всегда тот же элемент, что `faceForTransform`
  (визуально двигающаяся грань).** План (UX4-08) не разделял эти роли явно. RED: два реальных
  свайпа подряд (короткий отклонённый, затем длинный) на словарной/учебной карточке — второй жест
  на некоторых координатах не срабатывал вовсе. Причина подтверждена изоляцией: `faceForTransform`
  во время snap-back/fly-out анимации физически покидает свой resting hitbox (CSS `transform`
  двигает то, что реально под курсором для hit-testing, а не только рендер), и вторая gesture-
  down могла попасть на «пустое место», где вместо грани хитестится её же (не двигающийся)
  предок. Исправлено: `zone` — стабильный, никогда не трансформируемый предок с идентичным
  resting-футпринтом (`.card-answer-wrap` на учебной карточке — уже существовал именно для этой
  роли; `.card-flip` на словарной — тоже никогда сам не двигается, двигается только
  `.card-flip-inner`). `--swipe-progress` при этом остаётся на `faceForTransform` (там же, где
  живут tint-псевдоэлементы и подписи), а не на `zone` — их структура не пострадала.
- **`installSwipeCard` получил новый параметр `baseTransform`.** Не описано планом явно. RED
  (найдено при ревью до RED-теста, не оставлено на волю случая): словарная карточка передаёт
  `faceForTransform = answer` — тот же элемент, у которого статическое CSS-правило
  `.card-back.card-face{transform:rotateY(180deg)}` держит текст не отражённым зеркально. Прямая
  установка `style.transform` во время драга/settle заменяла бы это правило целиком, временно
  зеркаля текст обратной грани. Исправлено передачей `baseTransform="rotateY(180deg)"`,
  комбинируемого с каждым drag/fly-out/snap-back значением; на покое (`clearVisual()` убирает
  инлайн-`transform` целиком) поведение то же, что и раньше — снова работает CSS-правило.
- **`pointerup` больше не требует предшествующего `pointermove`, чтобы засчитать жест.** План
  описывал ровно то же пороговое условие (75px, ×1.25 доминирование по X), что уже проверялось на
  release — но реализация изначально «запирала» это условие за флагом `horizontal`, выставляемым
  только внутри `pointermove`. RED: несколько существующих тестов синтезируют жест как ровно два
  события (`pointerdown`+`pointerup`, без единого `pointermove` между ними) — с реальным
  touch/mouse-драгом (steps>1) это не проблема, но с таким «мгновенным прыжком» кнопка/зона вообще
  не оценивались. Исправлено: `pointerup` сам считает `|dx|>|dy|×1.25`, если `pointermove` жест
  так и не «запер» — что и восстанавливает эквивалентность двух путей без потери «отмены на
  вертикальный скролл» (`pointermove`, увидевший вертикальное доминирование первым, по-прежнему
  ничего не делает и не корректирует `horizontal` назад).
- **View Transition-обёрнутый ре-рендер должен обновлять `previous`/`previousRoute`/…
  синхронно с самим `rebuild()`, а не сразу после вызова `withViewTransition`.** Спецификация
  View Transitions ставит `updateCallback` в отдельную задачу (не гарантированно синхронно с
  вызовом `startViewTransition`) — RED: «keyboard focus returns to the active section after
  leaving Settings» — фокус на `#nav-training` переставал восстанавливаться после этой правки,
  потому что `previousRoute` уже совпадал с новым `route` к моменту, когда `rebuild()` реально
  выполнялся, и ветка `previousRoute != route` (условие для восстановления фокуса) больше не
  срабатывала. Исправлено переносом всех «финальных» присваиваний полей внутрь `rebuild`,
  используя снятый ДО планирования снимок `routeChangedFrom` для самого условия.

**Проверка (PASS/FAIL/NOT RUN):**

| Проверка | Команда (рабочая директория) | Результат |
|---|---|---|
| Shared unit (`VocabularyDocumentTest`, новый `preview`-тест) | `./gradlew :shared:desktopTest` (`kotlin/`) | PASS |
| Shared JS/Wasm тесты (не должны сломаться от нового `preview`) | `./gradlew :shared:jsTest :shared:wasmJsTest` (`kotlin/`) | PASS |
| JS/Wasm компиляция `composeApp` (весь web-слой) | `./gradlew :composeApp:compileKotlinJs :composeApp:compileKotlinWasmJs` (`kotlin/`) | PASS, без предупреждений |
| Прочие таргеты (не должны сломаться) | `./gradlew :shared:compileKotlinDesktop :shared:compileKotlinIosSimulatorArm64 :shared:compileKotlinMacosArm64 :composeApp:compileKotlinDesktop` (`kotlin/`) | PASS (только пред-существующие warnings в несвязанных файлах) |
| Fresh distribution | `./gradlew :composeApp:composeCompatibilityBrowserDistribution` (`kotlin/`) | PASS, `rive/rings.riv` отсутствует в dist, `confetti/again/chain-complete.riv` присутствуют |
| Playwright, wasm, chromium, **полный** `testMatch` (19 файлов, включая новый `kotlin-ux4.spec.ts`) | `KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable KOTLIN_SPIKE_BRANCH=wasm npx playwright test --config=playwright.kotlin.config.ts --project=chromium` (корень) | PASS 119/119 (повторный прогон после исправления гонки/CSS — тоже 119/119; см. «Известные ограничения» про один ранее пойманный флейк) |
| Playwright, js, chromium, полный `testMatch` | то же с `KOTLIN_SPIKE_BRANCH=js` | PASS 119/119 |
| RED→GREEN: `kotlin-method-cycle.spec.ts` (узкий вьюпорт + открытая reference-panel + клик по рейтингу) | `... kotlin-method-cycle.spec.ts kotlin-training.spec.ts kotlin-ux4.spec.ts --project=chromium` | FAIL до фикса UX4-11 (см. отклонения), PASS 18/18 после |
| Обновлённые существующие спеки (капча pointer теперь на `.card-answer-wrap`/`.card-flip`, ring-тесты заменены, «dedicated affordance» тесты переписаны под «весь face свайпается») | `kotlin-flip-card.spec.ts`, `kotlin-preferences-settings.spec.ts` | PASS (см. список изменённых тестов ниже) |
| Новый `kotlin-ux4.spec.ts` (10 тестов: панель-на-грани, отсутствие ring-запросов, `ArrowLeft`/`ArrowRight` на обеих карточках, touch-target/a11y под реальной coarse-pointer эмуляцией (`devices['Pixel 7']`) и под узким fine-pointer окном, коллапс каталога с `inert`/фокусом, nav-indicator, View Transition tab-переходы, скриншоты) | `kotlin-ux4.spec.ts --project=chromium` | PASS 10/10 |

**Изменённые существующие Playwright-тесты (список, не полный diff):**
- `kotlin-flip-card.spec.ts`: два ring-теста удалены, заменены одним
  `'flip and reveal no longer request or render the removed ring Rive accent'`; touch-swipe тест
  переключён на `.card-flip` (стабильный zone) вместо `.vocabulary-swipe-zone` (bubbling
  синтетических событий без `bubbles:true` не долетал до нового zone-предка).
- `kotlin-preferences-settings.spec.ts`: `.vocabulary-swipe-zone` → `.card-answer-wrap`/`.card-flip`
  как reference-элемент для capture-проверки и dispatch-целей (там, где события синтетические, не
  реальные CDP-touch); три словарных теста, буквально проверявших «свайп по тексту ответа НЕ
  оценивает» и «мышь на coarse-pointer НЕ оценивает», переписаны в положительные эквиваленты —
  «оценивает» (поскольку это ровно то, что теперь просит пользователь: вся грань — единая зона).

**Скриншоты (`Plans/Kotlin/artifacts/ux4/web/`, 390×844 и 1280×900, dark/light, wasm+js):**
`{branch}-training-front-{theme}.png`, `{branch}-training-back-{theme}.png`,
`{branch}-vocabulary-front-{theme}.png`, `{branch}-vocabulary-back-{theme}.png`,
`{branch}-vocabulary-mid-swipe-{theme}.png` (карточка на середине drag — виден наклон, тинт и
подпись), `{branch}-tabs-progress-{theme}.png` (после переключения на «Прогресс» — виден
nav-indicator под активной кнопкой).

**Известные ограничения:** `flip-rive-perf.spec.ts` не расширен под v4 (LEAN MODE — новая
функциональность здесь не связана с производительностью Rive, только затронутые
поведенческие сценарии тестировались). Один прогон полного wasm-suite поймал предсуществующий,
не связанный с этим проходом флейк — `kotlin-preferences-settings.spec.ts`'s «narrow layout…»
изредка (1 из ~6 прогонов) видел горизонтальный оверфлоу от `.chain-header ol`'s списка шагов
цепочки (длина текста шага зависит от случайно выбранного набора слов при первом заходе в
Chain-режим) — воспроизведено и на прогонах без единой правки из этого прохода (тот же файл,
`--repeat-each=5`, 4/5 PASS); не является регрессией v4, не чинилось в этом проходе (вне
заявленного скоупа §17). `RiveCatalog.md`'s Rive-иконка табов (#283 Bottom Navigation Bar,
17.8.2) остаётся backlog, не проверялась/не скачивалась. Реальные устройства (iOS Safari/Android
Chrome для проверки View Transitions на настоящем touch-железе) не проверялись — LEAN MODE,
headless Chromium + `devices['Pixel 7']`-эмуляция для coarse-pointer only.

### 18.1 Correction round — правки по реальному фидбеку пользователя (P0–P3), web

Пользователь после первого v4-прохода прислал скриншот-фидбек с 15 конкретными пунктами (P0-1..2,
P1-3..9, P2-10..14, P3-15). Все реализованы web-only, `kotlin/iosApp` не тронут.

**P0 (ломали основной запрос):**
- **P0-1.** `.card-answer-wrap` не имел явного `touch-action`, поэтому на реальном touch-контексте
  браузер иногда перехватывал горизонтальный драг как попытку скролла и обрывал последовательность
  указателя (`pointerdown → pointermove → pointercancel`, без `pointerup`) — ничего не оценивалось,
  а кнопки на этом же устройстве уже визуально свёрнуты (UX4-11). Синтетический
  `pointerdown`/`pointerup`-диспатч (уже использованный в других тестах файла) НЕ воспроизводит этот
  баг — только реальный touch-контекст (`devices['Pixel 7']`) плюс CDP `Input.dispatchTouchEvent`
  реально гоняют нативный жестовый распознаватель, которым управляет `touch-action`. Фикс: явный
  `touch-action:pan-y` на `.card-answer-wrap`. Новый RED→GREEN тест (реальный CDP touch на мобильном
  контексте) — `kotlin-ux4.spec.ts` (P0-1).
- **P0-2.** `VocabularyWeb.renderCard` оборачивал грань в `.card-flip`/`.card-face` только после
  раскрытия — до этого проверка режима/поле ответа/кнопка «Показать ответ» лежали без своей панели
  прямо на фоне страницы, а панель визуально «появлялась из ниоткуда» ровно в момент реального
  оборота (тот самый баг со скриншота: статичная рамка, контент переворачивается внутри). Фикс:
  `.card-flip > .card-flip-inner > .card-front.card-face` собирается на **каждом** рендере карточки,
  включая нераскрытое состояние (поле ответа и кнопка теперь внутри `front`); грань answer
  примонтирована только после раскрытия. Пустое состояние (`item == null`) тоже получило свою
  `.card-face`-панель. Новый тест — `kotlin-ux4.spec.ts` (P0-2).

**P1 (полировка, важная):**
- **P1-3.** Подпись подсказки свайпа наследовала `.vocabulary-answer>p{font-size:1.5rem;
  color:var(--green)}` (та же строка, что и «Твой ответ: …»). Фикс: сузить селектор до
  `.vocabulary-answer>p[lang]` (подсказка — единственный `<p>` без `lang`).
- **P1-4.** Обе грани делят одну grid-ячейку (`.card-flip-inner`), более короткая наследует высоту
  более высокой — после возврата с ответа передняя грань выглядела пустым провалом. Фикс:
  `.vocabulary-card .card-front.card-face{display:flex;flex-direction:column;
  justify-content:center}`.
- **P1-5.** Драг, вернувшийся к начальной точке, на `pointerup` читался как тап (dx/dy снова ~0) и
  переворачивал карточку. Фикс: `installTapGesture` получил `pointermove`-слушатель, обнуляющий
  ожидающий тап, как только смещение превышает те же 10px, что уже проверялись на release.
- **P1-6.** Мышиный драг выделял текст карточки как обычное выделение. Фикс: как только жест
  «запирается» горизонтальным (тот же момент, что включает визуальный tilt), `installSwipeCard`
  ставит `zone.style.userSelect="none"` и сбрасывает уже начавшееся выделение; снимается в
  `clearVisual()`. Сброс выделения — через новый `expect`/`actual clearTextSelection()`
  (`PointerInterop.kt`/`.js.kt`/`.wasm.kt`) по прецеденту `withViewTransition`: `dynamic` не
  компилируется на Wasm-таргете, поэтому нельзя было звать `window.asDynamic().getSelection()`
  прямо в общем `webMain`-файле.
- **P1-7.** Подписи свайпа сидели на `top:50%` (перекрывали `dl` с переводом) и были жёстко
  привязаны к краю трансформируемой грани — на большом drag «Вспомнил» уезжал за край вьюпорта
  вместе с гранью. Фикс: подписи — `top:16px`, со своей рамкой/фоном, поменяны местами по краям
  (`-good` теперь `left`, `-again` — `right`, ближе к задней/более стабильной кромке движения);
  точечные градиенты у краёв заменены на ровный `color-mix`-тинт всей грани.
- **P1-8.** Драг-трансформ висел на `.card-back` — ребёнке `.flashcard{overflow:hidden}` — поэтому
  визуально уезжал только ответ, обрезаясь по кромке карточки, а вопрос оставался на месте; это
  противоречит буквальному запросу «вся карточка должна двигаться». Фикс:
  `installSwipeCard(wrap, card)` вместо `installSwipeCard(wrap, back)` — трансформ теперь на самом
  `.flashcard`-элементе (`card`, содержащем `.card-meta`+вопрос+ответ), которого его собственный
  `overflow:hidden` не обрезает (клипит детей, не себя). `--swipe-progress` по-прежнему читается
  `.card-back::before/::after` через обычное наследование custom property. Новый тест —
  `kotlin-ux4.spec.ts` (P1-8, проверяет непустой `style.transform` на `.flashcard` во время драга).
- **P1-9.** `::view-transition-old(root)/::view-transition-new(root)` кроссфейдил всю страницу одним
  снимком — на середине перехода видны два таба и два подчёркивания сразу, шапка мерцает. Фикс:
  именованные группы `.route-content{view-transition-name:route}` и
  `.nav-indicator{view-transition-name:nav-indicator}`, root-группа не анимируется вовсе
  (`animation:none`), направленный slide (`--vt-dir`, знак от сравнения `route.ordinal` до/после —
  полагается на то, что порядок `WebRoute`-enum совпадает с порядком кнопок в `WebNav`, что и есть
  в коде). Дополнительно: `root.scrollTop` сбрасывается в `0.0` именно на смене route (было — всегда
  восстанавливался старый scroll, новая вкладка открывалась с прокруткой прошлой).

**P2 (полировка, заметная):**
- **P2-10.** У активного таба было сразу два индикатора — заливка `button.active` и скользящее
  подчёркивание. Фикс: `.primary-nav button`/`.primary-nav button.active` — прозрачный фон, только
  `color`+`font-weight`; hover явно переопределён отдельным правилом, чтобы не потеряться на равной
  специфичности. «Таблицы и схема» на мобильном получила короткий вариант "Таблицы"
  (`.nav-label-full`/`.nav-label-short`, `aria-label` всегда несёт полное имя).
  **Отклонение от буквы фидбека:** `position:sticky` для `.primary-nav` на мобильном (буквально
  просили «прибить» нав при скролле) добавлен и затем **убран** — измерено, что он резко учащает
  предсуществующую (см. §18, «известные ограничения») редкую гонку в измерении `nav-indicator`
  (`WebNav.update()` читает `getBoundingClientRect()` сразу после смены route/вьюпорта): без sticky
  — не связанный с этим проходом тест падал ~1/10 прогонов (соответствует ранее задокументированной
  базовой частоте), со sticky — 8/8 и 15/15 в повторных замерах. Короткая подпись (сама жалоба на
  перенос в две строки) не зависит от sticky и оставлена.
- **P2-11.** `.settings-toggle` не имел собственного CSS, чекбокс наследовал `label{flex-direction:
  column}` и центрировался под текстом. Фикс: настоящий переключатель (`appearance:none` + трек/
  ползунок на `::after`), `role="switch"` на чекбоксе. Текст про «Кнопки оценки остаются доступны»
  исправлен на «На компьютере также кнопки и ← →» (больше не верно для touch после P0-1).
- **P2-12.** Кнопки режима ответа словарной карточки не показывали выбранное состояние (только
  `aria-pressed`, без `.active`, в отличие от учебной карточки). Фикс: `classList.toggle("active",
  …)` рядом с уже существующим `aria-pressed`.
- **P2-13.** Кнопки оценки — 102px, три строки текста, без цветового акцента у словарных. Фикс (на
  ширине ≥601px — см. ниже): `flex-direction:row`, hint (`<small>`) скрыт, интервал — таблетка на
  `--surface-raised`; словарным кнопкам добавлен `cls`-параметр (`rating-again`/`rating-good`).
  Плюс `button:active{transform:scale(.98)}` для нажатия.
  **Регрессия, найденная и исправленная в этом же проходе (не буквально из фидбека, но обязана
  ему):** первая версия правки не гейтила `flex-direction:row` шириной вовсе — на узком, но
  fine-pointer окне (390px, обычный Chromium без touch-эмуляции — кнопки там ОСТАЮТСЯ видимыми, см.
  UX4-11) три инлайн-потомка (label/hint/interval) не помещались в строку, и сам текст лейбла
  переносился посреди слова («Пов‑торить»). Обнаружено вручную (скриншот 390×844), не было
  покрыто существующим тестом. Исправлено: `flex-direction:row`+скрытие hint перенесены под тот же
  `@media(min-width:601px)`; ниже — прежняя (нередактированная) колоночная раскладка, которая и так
  уже была верна для этой ширины.
- **P2-14.** `.study-help` был неверен на телефоне («1–2 — оценить» без упоминания свайпа/стрелок) и
  не упоминал ← → нигде. Фикс: текст расширен до «Пробел — показать ответ · ← → или 1–2 — оценить»,
  сам футер скрыт под `(pointer:coarse)` (подсказка в карточке уже покрывает touch после P1-7).

**P3 (мелкая полировка):**
- **P3-15.** `.vocabulary-answer dl>div` — фиксированная колонка `110px` под `dt`, «В предложении»
  переносилось. Фикс: `grid-template-columns:max-content minmax(0,1fr)`.

**Проверка (PASS/FAIL/NOT RUN):**

| Проверка | Команда (рабочая директория) | Результат |
|---|---|---|
| JS/Wasm компиляция `composeApp` (весь web-слой, включая новый `clearTextSelection` expect/actual) | `./gradlew :composeApp:compileKotlinJs :composeApp:compileKotlinWasmJs` (`kotlin/`) | PASS, без предупреждений |
| Fresh distribution | `./gradlew :composeApp:composeCompatibilityBrowserDistribution` (`kotlin/`) | PASS |
| Playwright, wasm, chromium, полный `testMatch` (20 файлов), ×2 | `KOTLIN_SPIKE_DIST=... KOTLIN_SPIKE_BRANCH=wasm npx playwright test --config=playwright.kotlin.config.ts --project=chromium` (корень) | 1-й прогон: 121/122 (1 сбой — тот же предсуществующий флейк из §18 «известные ограничения», не регрессия этого прохода, см. ниже); 2-й прогон сразу следом: PASS 122/122 |
| Playwright, js, chromium, полный `testMatch` | то же с `KOTLIN_SPIKE_BRANCH=js` | PASS 122/122 |
| `kotlin-ux4.spec.ts` + `kotlin-flip-card.spec.ts` (все новые/изменённые тесты этого раунда) | `... kotlin-ux4.spec.ts kotlin-flip-card.spec.ts --project=chromium` | PASS 27/27 |
| Ручная визуальная проверка P2-13-регрессии (390×844, dark; 1280×900, light) | скриншоты сохранены вне git (`/tmp`, не публикуются) | подтверждено визуально: кнопки читаемы на обеих ширинах после фикса |

**Известные ограничения / изменения в понимании:** предсуществующий флейк
`kotlin-preferences-settings.spec.ts`'s «narrow layout…» (см. §18) при повторном расследовании
оказался НЕ про `.chain-header` (шаги там статичные, не зависят от случайного набора слов — это
уточнение к прежней записи в §18) — фактический overflow пришёлся на `.nav-indicator`, чья позиция
считается `WebNav.update()` через `getBoundingClientRect()` сразу после смены route/вьюпорта; при
некоторых раскладах гонки эта позиция уезжает за пределы нового узкого вьюпорта. Это
предсуществующая (round-1, `WebNav.kt`/`nav-indicator` не переписывались в этом проходе) редкая
гонка, не починенная в этом проходе (вне заявленного скоупа P0–P3), но задокументированная точнее
и **не усугублённая** — исходно предложенный `position:sticky` для мобильного nav был испытан,
эмпирически подтверждён как многократно учащающий её, и убран (см. P2-10 выше).

## 19. v5 — телефонный слайд табов, словарная карточка без кнопки «Показать ответ»

Дата: 2026-09-26. Основание — прямой фидбек пользователя: (1) «переходы между табами дергаются,
давай как на телефонах сделаем когда свайпаются экраны» (не свайп-жест, именно визуальный слайд
экрана целиком, как в фидбеке явно уточнено «свайпаются экраны», а не «свайпать»); (2) «когда карты
слова тренируем не нужно кнопку открыть, клик — переворачивает сразу». Оба пункта — web-only,
`kotlin/iosApp` не тронут (в рабочем дереве были незакоммиченные iOS-правки — не трогались).

### 19.0 Контракт v5

| # | Решение |
|---|---|
| 1. Табы дёргаются | 19.1 диагностика → 19.2 `RouteSlider` (два слоя, только `transform`, без View Transitions) |
| 2. Слово-карточка: убрать кнопку | 19.4: `installTapGesture` на всей передней грани + `role=button` на блоке подсказки + Space |

### 19.1 Диагностика (перед реализацией, не гипотеза)

Инструмент: `.tmp-ux5/tabs-jank.mjs` (Playwright + CDP `Emulation.setCPUThrottlingRate(4)`,
персистентный `requestAnimationFrame`-цикл для дельт кадра, `PerformanceObserver({entryTypes:
['longtask']})`), сырые данные — `Plans/Kotlin/artifacts/ux5/web/tabs.json`.

**before** (HEAD `e52320c`, 3 прогона): переход на «Слова»/«Таблицы и схема» — p95 42–58мс, максимум
50–63мс, 0 long tasks; на «Прогресс»/«Настройки»/«Карточки» — p95 18–20мс. **before-no-vt**
(контроль: тот же HEAD, но `document.startViewTransition` удалён `init script`'ом до загрузки, без
единой правки кода) — цифры **той же величины или хуже** (p95 42–48мс на тех же табах, rapid-click
максимум даже выше: 47мс vs 33мс) — **View Transitions сам по себе не был доминирующей причиной**;
план изначально подозревал именно снимки View Transitions, но контрольный прогон это не подтвердил.

Трассировка (`.tmp-ux5/trace-tabs.mjs`, `Tracing.start` с категориями `blink`/`cc`/`v8`/
`devtools.timeline`) на переходе на «Таблицы и схема» показала на HEAD: **три отдельных** задачи
(~39мс сборка нового DOM в update-callback'е `startViewTransition`, ~28мс отдельный layout/paint для
снимка "after", ~22мс третья) — API САМ разбивает работу на несколько тасков вместо одного. Первая
(наивная) реализация `RouteSlider` без этого разделения делала всё **синхронно в одном таске** внутри
клик-хендлера — трассировка на ней показала **один** таск ~71мс (`EventHandler::
handleMouseReleaseEvent`/`v8.callFunction`), объясняющий худший наблюдаемый кадр (макс. 92–98мс).

**Вывод.** Реальная причина дёргания — не сам факт использования View Transitions, а (а) то, что
`.focus()`/`getBoundingClientRect()`, вызванные ПОСЛЕ полной перестройки большого маршрута
(словарный каталог, грамматическая таблица), форсируют синхронный layout всего документа в момент,
когда он это не ждёт, и (б) отсутствие естественного разбиения на несколько тасков, которое API
View Transitions давал «бесплатно» через свой собственный жизненный цикл (захват snapshot → callback
→ захват нового snapshot — каждый шаг официально отдельная задача).

### 19.2 Реализация: `RouteSlider` (NEW `RouteSlide.kt`)

Заменяет View Transitions полностью (удалены `ViewTransition.kt`, `ViewTransitionJs.kt`,
`ViewTransitionWasm.kt`, вызовы `withViewTransition`, CSS `::view-transition-*`/`--vt-dir`).

- Два слоя — `outgoing` (существующий `.route-content`, остаётся `position` static/в потоке, его
  трогает только `transform` — так он продолжает задавать высоту `.route-viewport` и никогда не
  форсирует resize) и `incoming` (новый `.route-content`, `position:absolute`, оверлей). Только
  `transform` (и один раз `overflow` на контейнере, только на время перехода) — оба слоя остаются на
  своём композитор-слое.
- **Строительство `incoming` (`populate()`) отложено на один макротаск** (`setTimeout(…, 0)`) от
  клик-хендлера — реплицирует «бесплатное» разбиение на таски, которое давал View Transitions
  (см. 19.1). Измеримо: без отложения максимум 69–80мс на двух тяжёлых маршрутах; с отложением —
  46–63мс (на уровне HEAD или лучше).
- **Коммит "from"-кадра — двумя `requestAnimationFrame`, не форсированным `getBoundingClientRect()`
  /`offsetWidth`.** Форсированное чтение сразу после построения `incoming` слило бы «построить DOM»
  и «первый layout/paint этого кадра» в один долгий таск — именно то, что 19.1 винит в худшем кадре.
  `raf1` (кадр N, `incoming` ещё на "from"-позиции) ничего не делает кроме планирования `raf2`; кадр N
  сам красит "from" естественно, без принуждения; `raf2` (кадр N+1, "from" уже отрисован) ставит "to".
- **`RouteSlider.settle()`** — форсирует незавершённый переход мгновенно (снимает `outgoing`, чистит
  временные стили `incoming`) и вызывается в начале `start()`, так что быстрый повторный/тройной клик
  по табам всегда чист (никогда не оставляет второй слой или таймер).
- **RED #1, найдено и исправлено в этом же проходе:** реактивный поток Compose (клик → `route = X` →
  `store.dispatch(SelectTab)` → пересборка `HtmlElementView.update`) регулярно вызывает **второй**,
  того же маршрута, `render()` почти сразу после первого — раньше (пока `previous`/`previousRoute`
  обновлялись только в `onSettled`, то есть после реальной анимации) это ВТОРОЕ вызов видел
  `previousRoute` ещё старым, читал себя как «ещё один переход маршрута» и либо (после первого фикса
  бага с фокусом) вызывал `slider.settle()` в «том же маршруте»-ветке, отменяя только что
  запланированный (но ещё не выполненный) `buildTimer` — **слайд никогда не проигрывался визуально ни
  разу**, хотя итоговый контент был правильным (потому и не было заметно на глаз/в assertions на
  финальный DOM — только по факту, что `.route-content` никогда не становился 2, что показал
  `MutationObserver`-дебаг). Исправлено двумя частями: (1) `previous`/`previousRoute`/…
  коммитятся **синхронно**, в момент решения "routeChanged", а не в `onSettled`; (2) **новый**
  `RouteSlider.isPending()` — «тот же маршрут»-ветка `render()`, если слайд уже в процессе (ещё не
  осел), не трогает DOM и не зовёт `settle()` вовсе.
- **RED #2 (найдено сразу следом, тем же проходом, реальным Playwright-прогоном, не гипотеза):**
  наивная версия фикса #1 просто игнорировала «тот же маршрут»-вызов целиком, пока слайд ещё в
  процессе — рассуждение было «окно слишком короткое, не важно». Реальный прогон парного React/
  Kotlin-теста по матрице (клик «Таблицы и схема» → сразу клик «Местоимения», без ожидания)
  показал 0 строк в таблице местоимений: клик по под-разделу пришёлся ровно в это окно и был
  безвозвратно потерян — уже запланированный `buildTimer` использовал СТАРОЕ замыкание (раздел
  «Карта системы», не «Местоимения»). Исправлено **новым** `RouteSlider.refresh()`: если слайд
  ещё не построил `incoming` — подменяет замыкание, которое будет вызвано; если `incoming` уже
  смонтирован (даже посреди анимации) — заменяет его детей на месте, не трогая `transform`/
  `transition` уже идущей анимации.
- **RED #3 (тот же прогон):** `getByRole('button', {name:'Прогресс'})` (без `exact`) во время
  слайда Progress→Training неоднозначно совпал с ДВУМЯ элементами — настоящей кнопкой нав-бара И
  словом «Сбросить **прогресс**» на ещё-не-удалённом `outgoing`-слое (тот всё ещё в DOM все ~320мс
  анимации). Исправлено: `outgoing` получает `inert`+`aria-hidden="true"` в тот же момент, что и
  `incoming` монтируется — не только когда `outgoing` наконец удаляется. Это не только чинит тест
  (Playwright `getByRole` уважает accessibility-дерево), но и настоящий a11y-баг: без этого
  клавиатурный/скринридер-пользователь мог бы на ~320мс попасть на уже уходящий экран.

### 19.3 Фокус на новый экран (`focusRouteHeading`, `TrainingWebApp.kt`)

Заменяет фокус на кнопку `nav-<slug>` (уже не годится — при реальном переходе часто ФОКУС ДО этого
был не на кнопке нав-бара, а где угодно ещё; кнопки нав-бара не пересоздаются, поэтому наивный возврат
на них при каждой смене раздела не соответствует «фокус переходит на новый экран» и ломается, если
фокус до перехода легитимно был не на нав-баре). Ищет первый `h1`/`h2` внутри свежепостроенного
`.route-content` (у каждого маршрута есть ровно один, кроме «Карточки» — там нет заголовка, тренинг
начинается прямо с тулбара режимов); при отсутствии — фокусирует сам контейнер `.route-content`
(`tabindex="-1"`). Вызывается внутри `onMounted` (см. 19.2) — на кадре, где `incoming` уже в DOM
(на "from"-позиции, ещё не отрисован визуально, но раскладка уже верна — `transform` не меняет layout-
координаты, поэтому фокус никогда ничего не скроллит).

### 19.4 Словарная карточка без кнопки «Показать ответ» (`VocabularyWeb.kt`)

- Убрана `front.button("Показать ответ", …)`. Клик/тап в любом месте передней грани (кроме кнопок
  режима и текстового поля — той же проверкой `editableTarget`, что уже использует
  `installTapGesture`) вызывает **NEW** `revealAndFlip(refresh)` (= `revealed=true; flipped=true;
  justRevealedFlip=true; refresh()`, идентично прежнему инлайну кнопки).
- Печатный режим сохраняет собственное действие — кнопка **«Проверить»** (не «Показать ответ» —
  другое имя, отличное от устного режима, где кнопки вообще нет), тоже зовёт `revealAndFlip`.
- **A11y.** Отдельный `<div class="vocabulary-prompt-block" role="button" tabindex="0"
  aria-label="Показать ответ">` — не весь `front` (который всегда содержит настоящие кнопки режима
  и, в печатном режиме, `textarea`: `role="button"` на контейнере с настоящими интерактивными
  потомками — известный анти-паттерн, вложенные интерактивные элементы). `promptBlock` содержит
  только подсказку/слово, никаких интерактивных потомков — безопасно. Атрибуты `role`/`tabindex`/
  `aria-label` ставятся только пока `!revealed` (после — это уже не «показать ответ», а обычная
  переворачиваемая грань, без отдельной affordance, как и раньше).
- Повторный клик на уже раскрытой карточке — без изменений (уже был `flipCard.installTap`, теперь
  вызывает **NEW** приватный `toggleFlip()`, идентичная логика).
- **Space** — **NEW** `VocabularyWebController.spaceReveal(refresh)`, подключённый в общий
  keydown-обработчик `TrainingWebApp.kt` (маршрут `Vocabulary`, до этого Space там не обрабатывался
  вовсе): не раскрыто → `revealAndFlip`; уже раскрыто → `toggleFlip()` (тот же путь, что клик).
- Существующий тест `getByRole('button', {name: 'Показать ответ'})` продолжает резолвиться и
  кликаться — Playwright ищет по accessible role+name, не по тегу, а `role="button"` даёт ровно то
  же имя; синтетический `.click()` на `promptBlock` доходит до `installTapGesture` на `front` через
  обычный bubbling (тот же механизм, что уже проверен существующим тестом «tapping the flipped
  vocabulary card flips it back», где `.card-flip` без явного click-листенера уже кликается так же).

### 19.5 Затронутые файлы

**NEW** `RouteSlide.kt`. Изменены: `TrainingWebApp.kt` (перестройка `render()`/`rebuild()` —
`RouteSlider`, `focusRouteHeading`, разделение bookkeeping/DOM-финализации, Space для Vocabulary),
`VocabularyWeb.kt` (см. 19.4), `training.css` (`.route-viewport`, `.vocabulary-prompt-block`, убраны
`::view-transition-*`/`--vt-dir`/`.route-content{view-transition-name}`; `.nav-indicator` transition
синхронизирован на 320мс/`cubic-bezier(.2,0,0,1)`), `PointerInterop.kt` (косметика — обновлён
doc-comment, ссылавшийся на удалённый `withViewTransition`). **Удалены** `ViewTransition.kt`,
`ViewTransitionJs.kt`, `ViewTransitionWasm.kt`. Ничего в `androidMain`/`iosApp`/`macosApp` не тронуто.

### 19.6 Проверка (PASS/FAIL/NOT RUN)

| Проверка | Команда (рабочая директория) | Результат |
|---|---|---|
| JS/Wasm компиляция | `./gradlew :composeApp:compileKotlinJs :composeApp:compileKotlinWasmJs` (`kotlin/`) | PASS |
| Fresh distribution | `./gradlew :composeApp:composeCompatibilityBrowserDistribution` (`kotlin/`) | PASS |
| Playwright TS typecheck | `npx tsc --noEmit -p .` (корень) | PASS |
| Playwright, wasm, chromium, полный `testMatch` (20 файлов, включая 6 новых UX5-тестов в `kotlin-ux4.spec.ts`, 4 новых в `kotlin-vocabulary.spec.ts`, 1 новый в `kotlin-flip-card.spec.ts`, плюс обновлённые `kotlin-preferences-settings.spec.ts`/`kotlin-native-web.spec.ts`) | `KOTLIN_SPIKE_DIST=… KOTLIN_SPIKE_BRANCH=wasm npx playwright test --config=playwright.kotlin.config.ts --project=chromium` | PASS 134/134 (после RED#1-3 из 19.2, см. ниже — до них наблюдались реальные failures: 13 → 3 → 0, задокументированы построчно) |
| Playwright, js, chromium, тот же `testMatch` | то же с `KOTLIN_SPIKE_BRANCH=js` | PASS 134/134 |
| Диагностика: `.tmp-ux5/tabs-jank.mjs` до/после (не в репозитории — временный инструмент; сырые числа — `Plans/Kotlin/artifacts/ux5/web/tabs.json`) | — | см. 19.1/19.7 |
| Известный предсуществующий флейк `kotlin-preferences-settings.spec.ts`'s «narrow layout…» (§18) | `--repeat-each=8` | 8/8 PASS (флейк не воспроизведён этим прогоном отдельно; при полном прогоне всей сьюты воспроизвёлся ровно 1 раз из 3 полных прогонов — соответствует ранее задокументированной ~1/10 базовой частоте, не связан с v5) |
| Известный флейк `kotlin-parity-chain.spec.ts`'s «P02… 12 five-step chains» (не документирован ранее, воспроизведён 1 раз из 3 полных прогонов при 122+ тестах в одном процессе, отдельно — PASS за 30.7с) | `-g "P02 React and Kotlin show the same 12 five-step chains"` | PASS изолированно; похоже на ресурсный флейк долгого (60 оценок) теста при последовательном прогоне всей сьюты, не воспроизведён отдельно, не связан с v5 (тест не касается роутинга) |

### 19.7 Числа диагностики (4×CPU throttle, 390×844, Chromium)

| Маршрут | before (max/p95, мс, 3 прогона) | after (max/p95, мс, 3 прогона) |
|---|---|---|
| Слова | 50–55 / 43–49 | 46–50 / 24–26 |
| Таблицы и схема | 59–63 / 57–59 | 60–63 / 18–22 |
| Прогресс | 31–34 / 19 | 19 / 18 |
| Настройки | 35–36 / 19 | 18 / 18 |
| Карточки | 19–35 / 18–19 | 18–20 / 18 |
| Rapid triple-click | 33–36 / 18–19 | 23–25 / 18 |

`before-no-vt` (контроль без API) — в пределах той же величины, что `before` (не лучше), подтверждая
19.1's вывод: сам API не был доминирующей причиной.

### 19.8 Известные ограничения

Инструменты диагностики (`tabs-jank.mjs`, `trace-tabs.mjs`, `debug-slide.mjs`) — временные, не
закоммичены в репозиторий (сырые числа сохранены в `Plans/Kotlin/artifacts/ux5/web/tabs.json`).
Реальные устройства (iOS Safari/Android Chrome) не проверялись — LEAN MODE, только headless
Chromium под CPU throttling.

### 19.9 Correction round: два бага из ревью

Ревью первой версии v5 нашло два реальных бага — оба воспроизведены RED (упавший тест на старом
коде) и закрыты GREEN (тот же тест зелёный на исправленном).

**Баг 1 — clipping/gap высоты во время слайда.** `RouteSlider` держал `incoming`
`position:absolute`, поэтому высоту `.route-viewport` на всё время transition диктовал только
`outgoing` (единственный, кто в нормальном потоке). Слайд в более высокий маршрут (Training →
Таблицы и схема) обрезал `incoming` по высоте `outgoing` на все ~320мс, с «выскакиванием»
обрезанного низа в момент `finish()`; слайд в более низкий маршрут оставлял пустую полосу под ним.
RED: новый тест `kotlin-ux4.spec.ts` («a slide between routes of different content height never
clips the incoming route mid-transition») на добуг-коде — `viewport.clientHeight` 574px против
ожидаемых ≥1956px (реальный `incoming.scrollHeight`). Фикс — без единого JS-замера высоты
(значит, без единого форсированного layout-read, что было бы регрессией самого §19.1/19.2):
`.route-viewport{display:grid;grid-template-columns:1fr}` + `.route-viewport>.route-content
{grid-area:1/1;min-width:0}` — классический приём «CSS grid stack»: оба слоя (или один, в покое)
занимают одну и ту же ячейку `1/1`, и высота строки авто-подстраивается под максимум из них,
браузером, как обычный layout, а не форсированный синхронный JS-read. `RouteSlide.kt` перестал
выставлять `incoming`'у `position`/`top`/`left`/`width` — это делает уже CSS. GREEN: тот же тест
проходит на исправленном коде (`viewport.clientHeight` == max слоёв, ±1px), полный целевой прогон
(`kotlin-ux4`/`kotlin-vocabulary`/`kotlin-flip-card`, wasm+js, chromium) — 52+21=... см. ниже.

**Баг 2 — Enter не активировал фокусный `role="button"` блок подсказки.** Убранная `<button>`
активировалась и Enter, и Space нативно; `promptBlock` (`<div role="button" tabindex="0">`) — нет:
браузер не авто-подключает Enter/Space к произвольной ARIA-роли, это обязанность страницы (ARIA
Authoring Practices, button pattern). Старый код обрабатывал только Space в глобальном keydown-
хендлере Vocabulary-маршрута (`TrainingWebApp.kt`); реальный `<button>`/`<a>`/`<textarea>` цель
уже исключалась общим `editableTarget`-guard'ом выше, а `promptBlock` — обычный `<div>`, значит не
исключался и реально долетал до ветки — просто ветка не слушала Enter. Фикс — одна строка:
`if (event.code == "Space" || event.key == "Enter")`. RED/GREEN — новый тест
`kotlin-vocabulary.spec.ts` («Enter on the focused role=button reveal target reveals the card,
same as Space»): фокусирует именно `reveal`-таргет (`getByRole('button',{name:'Показать
ответ'})`), жмёт Enter, проверяет `.card-flip-inner` получил класс `flipped` — падал бы на
предыдущей версии (Enter не обрабатывался вовсе), проходит на исправленной.

Затронутые файлы: `RouteSlide.kt`, `training.css`, `TrainingWebApp.kt` (только keydown-ветка
Vocabulary + её doc-комментарии), `kotlin-ux4.spec.ts` (+1 тест), `kotlin-vocabulary.spec.ts` (+1
тест). `VocabularyWeb.kt` не менялся — багов в нём не было, только в вызывающем коде и CSS.

Проверка после фикса:

| Проверка | Команда (рабочая директория) | Результат |
|---|---|---|
| JS/Wasm компиляция | `./gradlew :composeApp:compileKotlinJs :composeApp:compileKotlinWasmJs` (`kotlin/`) | PASS, без warnings |
| Fresh distribution | `./gradlew :composeApp:composeCompatibilityBrowserDistribution` (`kotlin/`) | PASS |
| Playwright TS typecheck | `npx tsc --noEmit -p .` (корень) | PASS |
| RED (баг 1, добуг-код) | `kotlin-ux4.spec.ts -g "clips the incoming route"` | FAIL (574px < 1956px) — подтверждает баг |
| GREEN (баг 1) | тот же тест, исправленный код | PASS |
| Целевые файлы, wasm, chromium (`kotlin-ux4`+`kotlin-vocabulary`+`kotlin-flip-card`) | `KOTLIN_SPIKE_BRANCH=wasm npx playwright test --config=playwright.kotlin.config.ts kotlin-ux4.spec.ts kotlin-vocabulary.spec.ts kotlin-flip-card.spec.ts --project=chromium` | PASS 52/52 |
| Те же файлы, js, chromium | то же с `KOTLIN_SPIKE_BRANCH=js` | PASS 52/52 |
| Полный `testMatch`, wasm, chromium | `KOTLIN_SPIKE_BRANCH=wasm npx playwright test --config=playwright.kotlin.config.ts --project=chromium` | PASS 135/136 (1 fail — уже задокументированный §18/19.6 предсуществующий ~1/10 флейк «narrow layout keeps the next training action visible», не связан с этим фиксом) |
| Тот же флейк изолированно | `-g "narrow layout keeps the next training action visible" --repeat-each=6` | PASS 6/6 |

### 19.10 Хитч «Таблицы и схема»

Каждый визит на «Таблицы и схема» платил ~60мс long task, потому что отложенная сборка внутри
`RouteSlider` всё равно звала `renderMatrixWeb` заново — единственный маршрут, чей DOM зависит
только от `state.matrixSelection`/`state.error` (сбрасывается на дефолт при каждом свежем входе
с другой вкладки, `TrainingStore.kt`), а не от куда более частых `exercise`/`phase`/счётчиков;
фикс (`TrainingWebApp.kt`, `RouteSlide.kt`) — keep-alive кэш `matrixCache`/`matrixCacheDeps`,
переживающий уход с вкладки (RouteSlider и раньше лишь `remove()`-ил `outgoing`, не уничтожал) и
пересобираемый только когда зависимости реально изменились (новый `RouteSlider.hasLiveIncoming()`
не даёт кэшу подсунуть `refresh()` его же смонтированный узел — самоопустошение), плюс
idle-прогрев `scheduleMatrixPrewarm` через 1500мс после первого рендера — измерение показало, что
голая off-DOM JS-сборка это не решает (доминирует первый layout/style-проход браузера по
поддереву, платится один раз именно на реальном attach), поэтому прогрев временно монтирует узел
скрытым (`visibility:hidden;pointer-events:none;position:absolute`), форсирует layout одним
`getBoundingClientRect()` и открепляет обратно. Измерено (4×CPU throttle, 390×844, Chromium,
временный `.tmp-ux5/tables-hitch.mjs` → `Plans/Kotlin/artifacts/ux5/web/tables-hitch.json`):
before — 58–68мс/1 long task на каждый визит без исключений; after — первый клик в сессии до
прогрева платит то же неизбежно один раз, но повторный визит и первый клик после прогрева — 0
long tasks, максимум кадра 19–51мс, на уровне изначально дешёвых маршрутов. Попутно —
ревьюерская заметка о `focus()` посреди слайда, способном прыгнуть страницей вслед за ещё едущим
через grid-stack (§19.9) заголовком: одна замена на `js("element.focus({preventScroll:true})")`
(ни `kotlinx-browser`, ни `kotlin-dom-api-compat` не объявляют `FocusOptions`). Playwright
(`kotlin-ux4.spec.ts`, новые тесты на переиспользование/пересборку/сброс/`preventScroll`) —
PASS 24/24 wasm+js; полный `testMatch` — PASS 138/139 на обеих ветках (уже задокументированный
§18/19.6 флейк «narrow layout…», не связан с этим фиксом).

### 19.11 Коррекция: persistent-кэш снят, остался только idle-прогрев

Ревью §19.10 нашло две проблемы. (1) Корректность: ветка `refresh()` посреди слайда, когда
`RouteSlider.hasLiveIncoming()` уже true, физически мутировала контент уже смонтированного
`matrixCache`-узла (через `refresh`'s "move children"), но не обновляла `matrixCacheDeps` —
кэш-метаданные расходились с реальным содержимым живого узла, и более поздний свежий вход,
чей дефолтный `matrixSelection` случайно совпадал с этим устаревшим записанным значением, получал
кэш-хит с чужим (немутированным дефолту) содержимым — воспроизведено новым Playwright-тестом
(`UX5 correctness`, меняет контрол ДО оседания слайда, без выдержки `waitForTimeout`) — RED 4/5 на
сборке с багом, GREEN 10/10 после точечного фикса (обновлять `matrixCacheDeps` и в этой ветке).
(2) Целостность измерения: честный ре-замер `before` (стек `git stash` двух файлов фикса обратно
к HEAD fb4cdcd, пересборка, замер) воспроизвёл ТУ ЖЕ картину, что и раньше — `repeat` уже 0 long
tasks даже без единой строчки кэширования. Это не ошибка замера: ~60мс — одноразовая стоимость
браузерного layout/style-прохода по этому поддереву, платится один раз за время жизни СТРАНИЦЫ
(внутренние движковые кэши шейпинга шрифта/расшаривания стилей), а не за узел — повторный визит с
абсолютно новым DOM был дёшев и до фикса. Persistent-кэш (`matrixCache`/`matrixCacheDeps`) не давал
никакого измеримого выигрыша сверх того, что даёт один только idle-прогрев, при реальном риске
устаревания (пункт 1) — снят целиком, а не залатан: `TrainingWebApp.kt`'s три Matrix-специфичные
ветки в `render()` заменены на тот же generic `node("div","route-content").also(::populate)`, что
у любого другого маршрута; `scheduleMatrixPrewarm` теперь строит одноразовый узел, форсирует layout
и выбрасывает его, ничего не сохраняя. Пересборка + честный ре-замер (тот же `.tmp-ux5/tables-hitch.mjs`
до/после, обновлённый `tables-hitch.json`) подтвердили: `first`/`repeat` не изменились (как и
ожидалось), `firstAfterPrewarm` всё равно улучшается (1 long task/~76–100мс → 0/~33–50мс) — прогрева
достаточно без кэша. Полный `kotlin-ux4.spec.ts` — PASS 25/25 wasm+js (24 → 25: старый
identity-тест на переиспользование заменён поведенческим, добавлен regression-тест на mid-slide
корректность), 104/104 с `--repeat-each=8` на нефлейковость.
