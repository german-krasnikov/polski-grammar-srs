---
name: kmp-web
description: "Build or review the browser target of Kotlin Multiplatform/Compose: JS/Wasm, responsive navigation, themes, settings, browser reminders, accessibility, input and deployment. Use for KMP web work or React-to-Kotlin browser parity; not native Android/iOS implementation."
---

# Kotlin Multiplatform for the browser

Read [compose-multiplatform-ui](../compose-multiplatform-ui/SKILL.md) for shared components and [kotlin](../kotlin/SKILL.md) for language behavior. Preserve an existing product's observable behavior before redesigning it.

## Target and dependency choices

- Establish required browser/OS versions and check the current toolchain matrix. As checked 2026-09-23, Compose Web and Kotlin/Wasm are Beta; Kotlin/JS is Stable. This is a dated observation, not a permanent version rule. [Platform status](https://kotlinlang.org/docs/multiplatform/supported-platforms.html)
- Decide whether to ship Wasm only or the Compose JS/Wasm compatibility distribution. WasmGC support starts at Safari 18.2 in the current documentation; a JS fallback expands compatibility but still requires real input/rendering checks on supported browsers. [Wasm browsers](https://kotlinlang.org/docs/wasm-configuration.html), [compatibility distribution](https://kotlinlang.org/docs/multiplatform/whats-new-compose-190.html#unified-web-distribution)
- Before committing the full UI, prove one vertical slice: load fonts/resources, type Polish text, reveal/rate a card, save/reload and expose usable semantics. Reject an unsuitable rendering choice early rather than replacing missing accessibility with test-only backdoors.
- Put browser capabilities in web/platform source sets. Share code between `js` and `wasmJs` only when APIs and dependencies support both; a JavaScript `dynamic` implementation is not automatically a Wasm implementation.
- Use the dependency catalog and actual generated Gradle tasks. Do not copy an Android-only library or a deprecated entrypoint merely because a sample compiles on JVM.

## Browser coding and storage

- Own and dispose DOM listeners, observers, timers, media-query subscriptions and blob URLs. Avoid a second event loop polling state when the host provides events. Keep costly synchronous work out of input handlers.
- Treat persisted JSON as an external schema: validate, version, preserve recoverable bytes, then migrate. Handle disabled storage/quota failures and give an actionable export/retry path. Do not overwrite unreadable data with an empty progress object.
- `localStorage` belongs to an origin, not a URL path: `/legacy/` and `/kotlin/` can share it. Use isolated contexts/keys for side-by-side evaluation. A new origin cannot directly read the old origin's data. Persistence is neither cross-device sync nor a guarantee that private browsing retains data. [Browser storage](https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage)
- Preserve locale/Unicode semantics explicitly when porting JS string code. Test NFC/NFD, Polish diacritics, punctuation and locale-sensitive case operations. Do not substitute accent removal for normalization.
- Use browser history only for intended navigable states; do not create entries for every card flip. If routes are added, define back/forward, refresh and static-host fallback behavior. Multiplatform navigation supports browser binding, but exact setup depends on JS/Wasm and library versions. [Routing](https://kotlinlang.org/docs/multiplatform/compose-navigation-routing.html)

## Layout and input

- Compose Web renders through a canvas. Configure the host container explicitly with the selected `ComposeViewport` API; ordinary CSS selectors do not style individual composables. Keep host CSS and Compose layout responsibilities separate. [Viewport](https://kotlinlang.org/docs/multiplatform/compose-css-styles.html)
- Test narrow widths, landscape, high-density screens, browser zoom and the mobile visual viewport with the keyboard open. Do not disable pinch zoom or blindly hide all page overflow to conceal clipping.
- Keep table row/column context understandable when horizontally scrolling. Normal card content should reflow at a 320 CSS-pixel viewport or equivalent zoom; a genuinely two-dimensional matrix may have a contained exception. [WCAG reflow](https://www.w3.org/WAI/WCAG22/Understanding/reflow.html)
- Handle Tab/Shift+Tab, Enter, Space and app shortcuts without stealing standard button or text-editing behavior. Respect composition/IME input, key repeat and focus context; rating shortcuts must not fire while a user types.
- Verify copy/paste, Polish keyboard input, focus after next-card transitions, text selection and screen-reader names. Canvas output and `testTag` are not proof that a browser exposes the intended semantic tree.

## Animation in a browser

- The current [visual decision](../../../Plans/Kotlin/PreGlassRollback.md) restores the pre-glass instructional cards and top navigation with semantic HTML controls. Preserve visible source/target endings and rule emphasis. Do not add a custom WebGL lens, optical backdrop capture, or `backdrop-filter` to imitate Apple. Set `color-scheme` for browser controls and match app tokens to `prefers-color-scheme` or the explicit light/dark setting; verify both schemes, contrast, forced colors, focus and production-browser frame pacing. The former [glass experiment](../../../Plans/Kotlin/GlassImplementationSelection.md) is archival. [MDN color-scheme](https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Properties/color-scheme), [MDN prefers-color-scheme](https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/At-rules/%40media/prefers-color-scheme)
- If the reference is a realistic water drop, distinguish contour/highlight imitation from optical refraction. A convincing rain-drop renderer samples the background texture and may model merging/trails; a static SVG or `backdrop-filter` on a control does not reproduce that result. Use a small prototype and measured rendering budget before introducing WebGL/background capture, and keep semantic buttons outside the rendering texture. [Codrops Rain & Water Effect Experiments](https://tympanus.net/codrops/2015/11/04/rain-water-effect-experiments/)
- A changing `border-radius`, SVG path or filter may require repaint; `transform` and `opacity` are often cheaper but cannot by themselves deform only the edge. Limit any morphing to a small surface and verify scroll frame pacing and paint cost on the target browsers before enabling it broadly. Do not assume a visual resemblance proves equal rendering cost. [web.dev animation performance](https://web.dev/articles/animations-and-performance), [MDN frame pipeline](https://developer.mozilla.org/en-US/docs/Web/Performance/Guides/Animation_performance_and_frame_rate)
- Implement the shared motion contract through Compose state. Observe `matchMedia('(prefers-reduced-motion: reduce)')` where necessary and dispose its subscription. CSS reduced-motion rules alone cannot stop internal Compose canvas animation. [Reduced motion](https://developer.mozilla.org/en-US/docs/Web/CSS/@media/prefers-reduced-motion)
- Make correctness independent of frame cadence, background-tab throttling and animation completion. Recompute due times from the clock after resume instead of counting rendered frames or timer ticks.
- Keep hidden/outgoing answers inaccessible and old rating actions disabled. Measure frame pacing during text input, table scrolling and card transitions; include a lower-powered mobile browser and a production build.

## Web navigation, settings and reminders

- Keep top-level destinations visible and keyboard reachable at desktop widths; on narrow viewports use a compact labelled navigation pattern without hiding the current study action behind a menu. Preserve focus and browser Back/Forward on genuine destination changes. Check 320 CSS px, 400% zoom, landscape and virtual-keyboard resize against [WCAG 2.2 reflow/focus](https://www.w3.org/WAI/WCAG22/understanding/).
- Make swipe rating an optional enhancement for touch/pointer users, only after reveal. Keep named buttons and keyboard actions equivalent; cancel a horizontal drag when it conflicts with vertical scrolling, text selection or a reference-table pan. Never rate on `pointerdown` or an animation callback.
- Offer system/light/dark and reduced-motion overrides only when their behavior is implemented on both JS and Wasm. On dynamic changes update Compose motion as well as host CSS. Use the [View Transition API](https://developer.mozilla.org/en-US/docs/Web/API/View_Transition_API) only as a progressive enhancement where supported; basic navigation must work without it.
- Resolve “system” appearance from `prefers-color-scheme` and respond to media-query changes; an explicit saved light/dark choice overrides that value. Semantic contrast colors for source/target changes need separate light/dark values, and changing the choice must preserve input/focus/review state. [Color-scheme media query](https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/At-rules/%40media/prefers-color-scheme)
- Put JSON export/import and permission state in Settings, using browser download/file picker and schema validation already owned by the web adapter. Browser notification permission needs a user gesture and secure context. A static site cannot promise reliable reminders while closed: mobile/background delivery needs a verified service-worker/Push or installed-PWA design and platform testing. Show unsupported/blocked status honestly; do not request permission at launch. [Notifications API](https://developer.mozilla.org/en-US/docs/Web/API/Notifications_API/Using_the_Notifications_API)

## Verification and distribution

- Use [playwright-testing](../playwright-testing/SKILL.md) for actual browser automation. Prefer role/name locators only after confirming the actual semantic bridge; supplement Compose tests and screenshots where needed. Do not claim jsdom or direct state mutation proves user input works.
- Test the chosen Wasm path and JS fallback explicitly. Record Chromium/Firefox/WebKit versions; Playwright WebKit is not a substitute for every Safari/iOS-device check.
- Verify production asset paths under the real hosting subpath, Wasm MIME/loading, lazy resources and cache upgrades. Do not add cross-origin-isolation headers unless the selected features require them and the host supports them.
- Compare features and saved data against the baseline, not React DOM structure or byte-identical screenshots. Track startup/download/input metrics against an agreed baseline; do not invent a universal performance threshold.
- A PWA, service worker, offline install or cloud synchronization is separate scope. Complete the existing web contract first. Release/deployment remains an explicit workflow scope decision.

Sources checked 2026-09-23. Recheck compatibility and API status when changing the toolchain.

`backdrop-filter`, backdrop-root and reduced-transparency behavior rechecked against MDN 2026-09-25.
