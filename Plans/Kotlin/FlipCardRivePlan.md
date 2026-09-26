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

