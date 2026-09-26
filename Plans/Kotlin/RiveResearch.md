# Rive for flip/swipe review cards: research (2026-09-25)

Everything here was checked against source code and published artifacts unless it is marked **[speculation]**.
Local copies used:
- `/private/tmp/claude-501/rive-cmp`: Rive-CMP clone at b42d6ba
- `/private/tmp/claude-501/npmrive`: the npm packages
- `/private/tmp/claude-501/aar`: the rive-android AAR
- `/private/tmp/claude-501/riv`: the .riv files
- `/private/tmp/claude-501/npmrive/inspect.mjs`: a Node tool that lists a .riv file's artboards, state machines and inputs. Run it with `node --import ./stub.mjs inspect.mjs file.riv`.

## TL;DR

1. **Rive-CMP cannot be our single cross-platform layer.** It is a Compose wrapper. Only two of our hosts use Compose: Android and the JVM desktop preview. On iOS it goes through Compose `UIKitView`, and on web through Compose `WebElementView`. Our SwiftUI hosts and our DOM web host cannot use either path.
2. **Use the official runtime on each host.** Put a tiny platform-free "card effect" contract in shared code:
   - Android: `app.rive:rive-android` 11.12.1, called directly.
   - iOS and macOS: `rive-ios` 6.27.0 through SPM (the `RiveRuntime` package supports both).
   - Web: `@rive-app/canvas-lite` 2.43.1 with Kotlin externals, with the `.wasm` file self-hosted.
   - JVM desktop preview: no-op, or Rive-CMP's jvm artifact (macOS arm64 only).
3. **Do the card flip and swipe natively.** Use Compose `graphicsLayer { rotationY; cameraDistance }`, SwiftUI `rotation3DEffect`, and CSS `transform: rotateY` with `backface-visibility`. Use Rive only for a transparent **effects overlay**: confetti or a burst on "remembered", and a small "again" cue. Real text on the card keeps TalkBack, VoiceOver and DOM accessibility, Dynamic Type, and our existing swipe and Reduced Motion settings.
4. **Do not fork Rive-CMP.** The Apache-2.0 license allows it. But the useful part is about 150 lines of thin wrappers, and the rest is JVM JNI code plus a prebuilt 9.1 MB dylib. Our own adapters would be about 50–100 lines per host, with no third-party Compose/AGP lag.

## 1. Rive-CMP (github.com/muazkadan/Rive-CMP)

**Targets and how each one works:**
- `androidLibrary`: wraps legacy `RiveAnimationView` in `AndroidView`.
- `iosArm64` and `iosSimulatorArm64`: rive-ios through spm4kmp, with a Swift `RiveAnimationController` inside `UIKitView`. There is no `iosX64` since 0.4.1.
- `js(browser)` and `wasmJs(browser)`: `@rive-app/canvas` in a `<canvas>` placed with Compose `WebElementView`.
- `jvm()`: its own JNI bridge to the C++ rive-runtime. It renders through the Skia CPU rasterizer into a Compose `Bitmap`, ships a prebuilt `librive-desktoparm64.dylib` of 9.1 MB, and works on **macOS arm64 only**.

**Coordinates:** `dev.muazkadan:rive-cmp:0.5.0` (released 2026-09-16, the latest on Maven Central). The per-target artifacts are `-android`, `-iosarm64`, `-iossimulatorarm64`, `-js`, `-wasm-js`, `-jvm` and `-runtime-macos-arm64`.

**Built with:** Kotlin 2.4.20, CMP 1.12.0, AGP 9.3.2, compileSdk 37, minSdk 24, rive-android 11.12.0, rive-ios 6.27.0, @rive-app/canvas 2.42.2.

**Compatibility with our stack** (Kotlin 2.4.20, CMP 1.12.1, AGP 9.1.1, Gradle 9.3.1, compileSdk 37): OK.
- The AAR metadata is `minAndroidGradlePluginVersion=1.0.0` and `minCompileSdk=37`.
- The README's "AGP 9.3.2+ / Gradle 9.7+" is only needed to build the library itself, not to consume it.
- The README calls Kotlin 2.4 a hard floor. We already meet it.
- Using it on iOS would pull spm4kmp and rive-ios into the Kotlin framework. That is pointless for us, because our iOS UI is SwiftUI.

**Common API** (all marked `@ExperimentalRiveCmpApi`):
- `CustomRiveAnimation(modifier, composition | url | byteArray, alignment, autoPlay, artboardName, fit, stateMachineName, overlay)`
- `rememberRiveComposition { RiveCompositionSpec.url(..) | .byteArray(Res.readBytes(..)) }`
- `RiveComposition.setNumberInput(sm, name, Float)`, `setBooleanInput`, `setTriggerInput`, `pause`, `reset`, `stop`
- `RiveFit` and `RiveAlignment` enums.

**What the API lacks:**
- Rive events or listener callbacks.
- Data binding or ViewModel properties.
- A state-change callback.
- Loading from an Android raw resource.
- Offscreen or shared rendering.

**Implementation problems:**
- On iOS, the input setters ignore `stateMachineName`.
- Changing any parameter recreates the native view.
- On iOS the view is always drawn on top of other content. The transparency issue #106 is still open, and it is blocked by CMP-8588.
- Android still uses the legacy View API. Issue #107 and PR #120, which move it to the new Rive Compose API, are both open.

**Maturity:**

| Metric | Value |
|---|---|
| Stars / forks | 116 / 9 |
| Commits | 296 |
| Open issues + PRs | 12 |
| Created | 2025-06-05 |
| Last push | 2026-09-25 |
| Releases | 15, from 0.1.0 (2025-07) to 0.5.0 |
| Maintainers | one |
| Status in README | "EXPERIMENTAL … might be discontinued" |

Open issues include iOS "not working", which needs rive-ios added to Xcode by hand, and #55, where clicks change the animation state.

**Code size:**
- About 3.8k lines of Kotlin and Swift. About 1.9k of that is the JVM JNI object model.
- The platform wrappers are 150–215 lines each.
- About 2.9k lines of C++ JNI bridge, plus the dylib, plus a git submodule of rive-runtime and Skia that is several GB to build.

**License:** Apache-2.0, so we may vendor or fork it if we keep LICENSE and NOTICE. The dylib also needs the NOTICE text for rive-runtime (MIT), Skia (BSD-3), HarfBuzz and the others.

**Verdict:** Do not add it as a dependency and do not fork it. At most, copy ideas from:
- the `js(...)` interop in `library/src/wasmJsMain/.../RiveSDK.wasmJs.kt` (about 40 lines of `@JsModule("@rive-app/canvas")` externals),
- its `setInput` helpers.

## 2. Best runtime per host

| Host | Runtime | Version | API for us | Renderer | Size impact |
|---|---|---|---|---|---|
| Android (Compose M3) | `app.rive:rive-android` | 11.12.1 (2026-09-16) | New Compose API: `rememberRiveWorker()`, `rememberRiveFile(RiveFileSource.RawRes/Bytes)`, `Rive(file, stateMachine, viewModelInstance, fit, …)`. `ViewModelInstance.setBoolean/setNumber/fireTrigger/getTriggerFlow`. Legacy: `RiveAnimationView.fireState/setBooleanState/setNumberState` | Rive Renderer, `RenderBackend.Vulkan / OpenGL` | arm64: `librive-android.so` 5.34 MB + `libc++_shared.so` 1.29 MB, about 2.4 MB compressed per ABI. The AAR has 4 ABIs, 11.6 MB. Use AAB or `abiFilters` |
| iOS 17 / macOS 14 (SwiftUI) | `rive-ios` SPM `RiveRuntime` | 6.27.0 (2026-09-15) | Platforms: iOS 14, macOS 13.1, Catalyst, tvOS, visionOS. Legacy: `RiveViewModel(fileName:stateMachineName:).view()`, `triggerInput`, `setInput`. New: `Worker`, `File(source:.local(..), worker:)`, `Rive(file:)`, `AsyncRiveUIViewRepresentable`, `dataBind: .auto/.instance`, `.paused()`, `.frameRate(..)` | Rive Renderer on Metal | The xcframework zip is 115.7 MB, but that holds all slices plus symbols. **[speculation]** Real app cost is a few MB per arch. Measure it with an App Thinning report |
| Web (Kotlin/JS + Wasm, DOM) | `@rive-app/canvas-lite` | 2.43.1 (2026-09-23) | `new Rive({canvas, buffer/src, stateMachines, autoplay, layout, onLoad})`, `stateMachineInputs(sm)`, `input.fire()`, `on(EventType.RiveEvent,..)`, `cleanup()`, `RuntimeLoader.setWasmUrl` | Canvas2D, with no WebGL context limit and cheap blend modes on mobile | lite: js 434 KB (94 KB gz) + wasm 882 KB (**279 KB br**). canvas: wasm 1.96 MB (628 KB br). webgl2: wasm 2.22 MB (717 KB br). The lite build drops text, layout, audio and scripting, which effects do not need |
| JVM desktop preview | none, or `rive-cmp-jvm` | 0.5.0 | No-op effect, or the Rive-CMP composable | Skia CPU raster, macOS arm64 only | +9.1 MB dylib |

Notes:
- **Web wasm loading.** By default `RuntimeLoader.wasmURL` is `https://unpkg.com/@rive-app/canvas-lite@<v>/rive.wasm`, with a fallback to jsdelivr. That is a third-party request, it breaks offline use, and it can fail on GitHub Pages or behind a CSP. Copy `rive.wasm` into `webMain/resources` and call `RuntimeLoader.setWasmUrl("rive.wasm")` and `setWasmFallbackUrl(null)` before the first `Rive`. Create the first instance lazily, on the first reveal, so the 279 KB is not on the startup path.
- **Web integration.** Add `implementation(npm("@rive-app/canvas-lite", "2.43.1"))` in `composeApp` `webMain.dependencies`, plus `@JsModule` externals.
  - **[needs verification]** `webMain` is shared between js and wasmJs, so the externals must use `JsAny`-typed declarations as in Rive-CMP's wasm file. If commonized externals fail, split them into `jsMain` and `wasmJsMain`, which is what Rive-CMP does.
  - The `yarn.lock` in `kotlin/kotlin-js-store` will change.
- **Web canvas count.** Use one overlay `<canvas>` above the card stack, not one per card.
- **Inputs vs data binding.** On Android, the new Compose API talks to the state machine only through **data binding** (`ViewModelInstance`). It has no input setters. The new Apple API is the same. Most of the free sample files below use **legacy state machine inputs**, which need the legacy APIs: `RiveAnimationView`, `RiveViewModel`, or `stateMachineInputs` on web. The only data-binding sample is `rewards_demo.riv`. So either accept the legacy API, which Rive says it will eventually deprecate, or re-author the effect file with data binding. Re-authoring needs the editor.
- **Xcode projects.** They are generated by `kotlin/iosApp/generate_project.rb` and `kotlin/macosApp/generate_project.rb`, so the SPM package reference must be added there.

## 3. Driving flip and swipe

Proposed shared contract in `shared/commonMain`, with no Rive types:

```kotlin
enum class CardEffect { Revealed, Remembered, Again }
```

The training store or ViewModel emits one of these after a rating is committed. Each host maps it:

| Host | Mapping |
|---|---|
| Android | `LaunchedEffect(effect) { vmi.fireTrigger("remembered") }`, or legacy `view.fireState("State Machine 1", "Trigger explosion")` |
| iOS / macOS | `.onChange(of: effect) { confettiVM.triggerInput("Trigger explosion") }` |
| Web | `stateMachineInputs("State Machine 1").find{it.name=="Trigger explosion"}.fire()` |

Rules:
- Skip all of it when `UserPreferences.motion == Reduced`, or when the OS reduce-motion setting is on and our preference is System.
- The overlay must not take pointer input. Otherwise Rive listeners steal taps (that is Rive-CMP issue #55):
  - Android new API: `RivePointerInputMode`.
  - Web: `shouldDisableRiveListeners: true` and CSS `pointer-events:none`.
  - SwiftUI: `.allowsHitTesting(false)`.
- Gestures stay native. `WebSwipeRating.kt`, the Android `detectHorizontalDragGestures` in `AndroidRatingActions`, and the iOS `DragGesture` already exist. Rive does not need drag progress. If a drag-linked effect is wanted, feed a 0..100 number input, as `ui_swipe_left_to_delete`'s `Swipe Threshold` or Rive-CMP's `alligator_swipe` `scroll` do.

## 4. Freely usable .riv files (inspected)

| File | Size | Source (license) | Artboard / state machine / inputs | Use |
|---|---|---|---|---|
| confetti.riv | 4.6 KB | [rive-ios Demo-App](https://github.com/rive-app/rive-ios/blob/main/Demo-App/RiveExampleSPM/Rive%20Files/confetti.riv) (repo MIT) | `Main` 500x500, `State Machine 1` [`Trigger explosion`: trigger] | **"Remembered" burst** (best fit) |
| rating.riv | 15.8 KB | [rive-android app/res/raw](https://github.com/rive-app/rive-android/blob/master/app/src/main/res/raw/rating.riv) (MIT); also in rive-react, rive-flutter | `State Machine 1` [`rating`: number] | Star burst for streaks |
| rating_animation_all.riv | 46.7 KB | rive-android (MIT) | ViewModel `Rating Animation(Number_Star:number)`; artboards `With Number`, `Simple`, `Bullseye`[`Rating`: number] | **Data-binding** variant, fits the new Android and Apple APIs |
| ui_swipe_left_to_delete.riv | 5.4 KB | rive-android / rive-ios (MIT) | `Swipe to delete`[`Swipe Threshold`: number, `Trigger Delete`: trigger] | Drag-linked "again" cue (prototype) |
| rewards_demo.riv | 217 KB | rive-android (MIT) | ViewModels `Rewards`, `Coin_Gem_Value(Icon_React:trigger)`, … (data binding) | Coin/gem reward, heavy |
| energy_bar_example.riv / life_bar.riv | 8.2 / 4.7 KB | rive-ios Example-iOS (MIT) | `State Machine `[`Energy`: number] / `Life Machine`[`100`,`75`,`50`,`25`,`0`: bool] | Session progress, streak |
| clean_icon_set.riv | 10 KB | rive-ios (MIT) | `LIKE/STAR` 64x64, `STAR_Interactivity`[`active`: bool] | Small "remembered" icon pop |
| light_switch.riv / rbutton.riv | 0.3 KB | rive-ios (MIT) | `Switch`[`On`: bool] / [`IsPressed`: bool] | Smoke-test fixtures |
| alligator_swipe.riv, mode_switch.riv, pull_to_refresh_use_case.riv | 9.6 / 7 / 26.6 KB | Rive-CMP sample (repo Apache-2.0) | `State Machine 1`[`scroll`: number]; `State Machine 1`[`Hover`: bool, `Click Trigger`: trigger]; `numberSimulation`[`advance`: trigger, `pull`: number] | Examples only |

**Licensing.** The repos are MIT or Apache, but many of these files started as Rive Marketplace files. Marketplace files are **CC BY 4.0** ([Rive docs](https://rive.app/docs/community/marketplace-overview)). Safest path: credit "Rive, Inc. / rive-app examples" in the app's licenses screen, and keep the source URL next to each asset.

**What we cannot get.** There is **no flip-card or shake asset** in any official repo. Marketplace files can be remixed under CC BY, but exporting a `.riv` requires opening the file in the Rive editor with a free account. **[speculation]** Rive's plans may limit exporting on the free tier.

## 5. Performance and measurement

**Android:**
- Frames:
  - `adb shell dumpsys gfxinfo dev.polski.grammarmatrix reset`
  - scripted `adb shell input swipe x1 y x2 y 150` ×N
  - `dumpsys gfxinfo … framestats`: p50, p90 and p99 plus janky %.
- In-app monitoring: JankStats (`androidx.metrics:metrics-performance`).
- CI-grade: a `:macrobenchmark` module (`com.android.test`, app `profileable`) with `FrameTimingMetric` (`frameDurationCpuMs`, `frameOverrunMs`), `MemoryUsageMetric` and `TraceSectionMetric("RiveFileLoad")`. Wrap `rememberRiveFile` in `trace("RiveFileLoad"){}`.
- Traces: Perfetto, with `adb shell perfetto -c - --txt` over gfx, view and sched.
- Memory: `dumpsys meminfo` before and after 50 swipes.
- Size: `apkanalyzer apk file-size` / `bundletool get-size total`.
- Test on a low-end physical device. The emulator GPU is not representative.

**Web:**
- Rive's own markers: `enablePerfMarks: true` in `Rive` options and `RuntimeLoader.enablePerfMarks = true`. The runtime then emits `performance.mark` entries: `rive:artboard-draw`, `rive:sm-advance`, `rive:renderer-flush`, plus wasm-init marks.
- Frame times: Playwright `page.evaluate` that samples `requestAnimationFrame` deltas during scripted swipes, or CDP `Tracing.start` for the devtools timeline.
- Mobile CPU: `Emulation.setCPUThrottlingRate(4)`.
- Memory: CDP `Performance.getMetrics` (`JSHeapUsedSize`) and `performance.measureUserAgentSpecificMemory`.
- Load cost: Resource Timing for `rive.wasm` (fetch + compile) and time to the first `onLoad`.
- Bundle: gzip and brotli sizes of the `dist` js/wasm, compared before and after.
- Run it in both the js and wasmJs builds (see the `playwright-testing` skill).

**iOS / macOS:**
- Test methods:
  - `measure(metrics:[XCTOSSignpostMetric(subsystem:category:name:)], options:)` around an `os_signpost(.animationBegin …)` interval for flip + effect. This reports hitch time ratio, frame count and frame rate.
  - `XCTMemoryMetric`
  - `XCTClockMetric` for File load
  - `XCTApplicationLaunchMetric` to check launch does not regress
- Traces: `xcrun xctrace record --template 'Animation Hitches' --device <UDID> --launch -- <app>`, then `xctrace export --toc`.
- Use a physical device for GPU numbers. The simulator's Metal path is not representative.
- Size: App Thinning size report (`xcodebuild -exportArchive` with `thinning`).

**Known characteristics** (from vendor docs, plus **[speculation]** from the architecture):
- Rive state machines settle and stop drawing when idle. Both new APIs expose settling and pausing, so an idle overlay should cost about nothing.
- The main costs are:
  - first wasm/native init, tens of ms **[speculation]**
  - file decode, trivial for files under 20 KB
  - one render surface per view
- Keep one long-lived overlay instance per screen instead of creating one per card.

## 6. Risks

- The legacy-inputs vs data-binding split between the sample assets and the new runtime APIs (section 2).
- APK growth of about 6.6 MB uncompressed per ABI, about 2.4 MB compressed. Compare with the current APK and consider R8 or ABI splits.
- The web CDN default for the wasm file.
- Two more native SDKs to keep updated: rive-ios and rive-android both release weekly.
- Accessibility: effects must be decorative. Hide them from TalkBack and VoiceOver (`importantForAccessibility=no`, `.accessibilityHidden(true)`, `aria-hidden="true"`), and skip them under Reduced Motion.
- The desktop preview is macOS-arm64 only, and only through Rive-CMP. A no-op there is fine.
- We have no editor, so there is no custom branding of effects. Plan a later pass in the Rive editor with a data-binding view model (`remembered`/`again` triggers) exported once and used on all hosts.

**Recommended order:**
1. Build the native flip on all hosts with the existing swipe code.
2. Add the `CardEffect` contract.
3. Build a web prototype with canvas-lite + confetti.riv, self-hosted wasm, and a Playwright rAF benchmark.
4. Android with the legacy `RiveAnimationView` for confetti.riv, plus gfxinfo and Macrobenchmark.
5. iOS/macOS with `RiveViewModel` + an XCTest signpost metric.
6. Replace the assets with a single data-binding `.riv` when editor access exists.

## Sources

- https://github.com/muazkadan/Rive-CMP (README, `gradle/libs.versions.toml`, `library/src/**`, issues #37 #55 #106 #107 #159 #161, PR #120)
- https://repo1.maven.org/maven2/dev/muazkadan/ and https://repo1.maven.org/maven2/app/rive/rive-android/
- https://github.com/rive-app/rive-ios (Package.swift, 6.27.0 release assets), https://github.com/rive-app/rive-android, https://github.com/rive-app/rive-wasm
- https://rive.app/docs/runtimes/android/android, https://rive.app/docs/runtimes/android/migrating-from-legacy, https://rive.app/docs/runtimes/apple/apple, https://rive.app/docs/runtimes/web/canvas-vs-webgl, https://rive.app/docs/runtimes/data-binding
- https://rive.app/docs/community/marketplace-overview (CC BY), https://creativecommons.org/licenses/by/4.0/
- npm: @rive-app/canvas, canvas-lite, webgl2, canvas-advanced 2.43.1 (sizes measured locally)
