# UX2 Web — independent virtual acceptance

Date: 2026-09-25. Scope: UX1 preferences contract as consumed by the browser, UX2 Settings/navigation/theme/motion/swipe/responsive behavior, reset parity, and the affected React ↔ Kotlin matrix scenarios. Independent tester used production JS and Wasm distributions, not source-mode dev servers.

## Final artifacts

The last distribution was produced after the reset-route fix and the final Settings copy correction:

```text
JS composeApp.js
e555b741b21d8632ebce7b16bc2c7015932333ca7b7c183c93183ee7ec3ae023

Wasm composeApp.js loader
1efee9de67d6fd6ef33e6d87d0d2171577ee6442dab265bd736e806e47599dca

Wasm module 6c9ceb97c73d4eeeef04.wasm
7999704e42a0ae3b1a000891b4941518e8f39cb4f5a974db215a70600469fad5
```

Build command in `kotlin/`:

```sh
./gradlew :composeApp:compileKotlinJs :composeApp:compileKotlinWasmJs :composeApp:composeCompatibilityBrowserDistribution
```

Result: **PASS** (`BUILD SUCCESSFUL`). Webpack emitted bundle-size recommendations for Compose/Skiko assets; no compilation or packaging error occurred.

## Results

| Acceptance | Result | Evidence |
| --- | --- | --- |
| Shared settings, storage migration/recovery, Settings navigation/history/focus, themes, reduced motion, JSON actions and gestures | **PASS** | `kotlin-preferences-settings.spec.ts`: Wasm 59 PASS / 0 FAIL / 4 SKIP; JS 58 PASS / 1 intermittent FAIL / 4 SKIP across Chromium, Firefox and WebKit. See responsive caveat below. |
| Two-rating colors and matrix/progress suite after reset fix | **PASS** | `kotlin-binary-rating.spec.ts` + `kotlin-matrix-progress.spec.ts`: combined retest 27/27 PASS per target across Chromium/Firefox/WebKit. |
| P08 matrix-to-training parity after reset fix | **PASS** | `kotlin-parity-matrix.spec.ts` P08: 3/3 PASS per target across Chromium/Firefox/WebKit. |
| Training reset semantics | **PASS** | Included in matrix/progress retest: cancel stays on Progress; confirmed reset returns to Training only when the store accepts the reset, matching React. |
| Settings swipe toggle controls grammar and vocabulary zones | **PASS** | Final-copy build direct Chromium UI smoke on JS and Wasm: OFF gives 0/0 zones; ON gives 1/1 zones; accessible name is `Оценивать карточки свайпом` and helper text covers grammar and words. |
| 390×844 training action, 320px document width and narrow Settings | **PASS with one unrepeatable outlier** | The final Settings suite had one JS/Chromium `training`-route measurement at 320px showing 70px overflow. The exact focused test passed immediately afterward and in five further repeats, then passed 10/10 additional repetitions. Thus the later focused evidence is 16/16 PASS; the initial outlier's cause is unknown and remains noted rather than erased. |
| Kotlin web compilation and production build | **PASS** | Exact Gradle command above compiled Kotlin/JS and Kotlin/Wasm and emitted both compatibility artifacts. |
| TypeScript/browser checks | **PASS** | `npm run typecheck`; `npx tsc -p tsconfig.browser.json --noEmit`. |
| Diff whitespace | **PASS** | `git diff --check`. |

The final 10-repeat responsive command, in repository root, was:

```sh
KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/js/productionExecutable \
KOTLIN_SPIKE_BRANCH=js KOTLIN_SPIKE_PORT=4181 \
npx playwright test tests/browser/kotlin-preferences-settings.spec.ts \
  -c playwright.kotlin.config.ts --project=chromium \
  -g 'narrow layout keeps the next training action visible' \
  --repeat-each=10 --reporter=line
```

Result: **10/10 PASS**. The preceding six identical focused runs also passed. Screenshot artifacts from the test matrix are in [`artifacts/ux2-test`](artifacts/ux2-test/).

## Skips and remaining limits

The four Playwright skips per target are browser-specific CDP touch simulations unavailable in Firefox and WebKit. Chromium touch-pointer tests passed; browser automation does not substitute for physical iPhone/Android browser checks.

Direct browser UI zoom to 400%, physical Safari/iPhone/iPad/Android browser, actual software keyboard/IME, hardware keyboard, VoiceOver/TalkBack/screen reader, formal WCAG audit, and real closed-browser reminder delivery were **NOT RUN**. The 320 CSS-pixel reflow target was tested, but it is not reported as a direct 400% zoom check. Browser Settings accurately state that reliable notifications after closing the page need a separate Push/PWA capability gate. These limitations remain open in the platform/migration plan.

## Reviewer

Independent read-only review: **APPROVED** for UX2 web source and evidence. No blocking findings remained. The reviewer specifically checked storage single-writer/migration, route/media listener cleanup, reset route guard, swipe scope, theme coverage and final swipe copy.
