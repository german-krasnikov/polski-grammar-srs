# P8 web renderer extraction and lifetime handoff

Date: 2026-11-08. Accepted contracts: [NEXT-SESSION-P5-P8-PLAN.md](NEXT-SESSION-P5-P8-PLAN.md) §P8 and [P8-LIFECYCLE-TEST-ADDENDUM.md](P8-LIFECYCLE-TEST-ADDENDUM.md). Browser control baseline: [P8-WEB-BASELINE-EVIDENCE.md](P8-WEB-BASELINE-EVIDENCE.md). The independent tester replay and final reviewer acceptance are recorded below; broader JS runner timeouts remain open.

## Ownership and behavior

`TrainingWebApp.kt` retains the one Compose mount, `remember(activeCoursePackId)` store, unkeyed `remember` renderer, routes, keyboard/composition/visibility listeners, and effect acknowledgement. `TrainingWebEffects.kt` owns effect execution, URL encoding and focus helpers. `TrainingDomRenderer.kt` owns route diff, navigation, vocabulary, exam, Rive overlay, RouteSlider, the Matrix idle prewarm timer and `close()`. `TrainingCardDom.kt` renders training/card DOM using only the renderer's Rive overlay and composing-state callback; it has no independent session/store or route lifetime. Shared `node`, `button`, contrast and collapsible helpers moved with the card DOM. The original file was 1,071 lines; the four files total 1,128 lines (199 + 103 + 344 + 482), including the timer port and cleanup. No public UI route or production debug hook was added.

`TrainingDomRenderer.close()` is idempotent. It cancels the owned prewarm timeout, closes RouteSlider/WebNav/controllers, and blocks render/schedule after close. A callback already dequeued checks `closed` before `renderMatrixWeb` or DOM mutation. A new renderer may independently schedule one 1,500 ms prewarm. `RouteSlider.close()` cancels timeout/rAF IDs and clears pending closures without invoking completion callbacks; async callbacks guard against close. `WebNav.close()` cancels both initial rAF stages and its resize listener. The actual offscreen Matrix layout path and CSS remain unchanged.

## Executed checks

All Gradle commands ran from `kotlin/`, with `--console=plain --max-workers=2`.

| Gate | Command / observation | Result |
|---|---|---|
| Source compilation | `./gradlew :composeApp:compileKotlinJs :composeApp:compileKotlinWasmJs` | PASS after extraction; no production compile errors. |
| Web test compilation | `./gradlew :composeApp:compileTestKotlinJs :composeApp:compileTestKotlinWasmJs` | PASS with actual DOM MutationObserver test on both targets. |
| Focused W3 JS | `./gradlew :composeApp:jsBrowserTest --tests 'polski.ui.TrainingDomRendererLifecycleTest'` | PASS: 2 tests, 0 failures after final guard. |
| Focused W3 Wasm | Same task using `wasmJsBrowserTest` | PASS: 2 tests, 0 failures on final source (last task result reused after restoring identical guard bytes). |
| Behavioral negative control | Temporarily removed only the stale-callback `closed` guard, ran focused JS browser test | RED: 2 tests, 1 failure; MutationObserver saw one `main.matrix-page` insertion after close, expected zero. Restored guard and reran focused JS GREEN. This was a post-implementation sensitivity check; the initial pre-fix runner stalled, so test-first RED was not observed. |
| Broad JS web suite | `./gradlew :composeApp:jsBrowserTest` | FAIL: 11 tests completed, 2 failures reported for `polski.presentation.TrainingStoreTest.tabNavigationResetsMatrixSelectionOnlyOnEntry` with Chrome/Karma `transport close` and `ping timeout` disconnect messages. Log: `/tmp/polski-p8-green-js.log`. |
| Focused broad-suite failure | `./gradlew :composeApp:jsBrowserTest --tests 'polski.presentation.TrainingStoreTest.tabNavigationResetsMatrixSelectionOnlyOnEntry'` | FAIL: 1 test, 1 failure; XML reports async test timeout of 10,000 ms. `TrainingStore` and this test were not edited in P8, but this rerun establishes that the broad failure is reproducible on the current checkout and remains unresolved. Log: `/tmp/polski-p8-trainingstore-focused.log`. |
| Production package | `./gradlew :composeApp:composeCompatibilityBrowserDistribution` | PASS in 1m51s. `prepareExamAudio`, JS/Wasm distribution audio verification and compatibility audio verification all PASS for six assets. |
| Static | `git diff --check` | PASS. |
| W1/W2 browser acceptance | New immutable distribution described below | NOT RUN by developer; handed to independent tester for the selected 20 + 2 baseline scenarios. |

The final distribution was copied from `kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable` to `/tmp/polski-p8-20261108-cb180f5-dist`. `composeApp.js` SHA-256 in both locations: `cb180f5c21a6756fde901f3bd13a2be73cb5402528213e08b2e23e7b078e2ea8`. No new Kotlin source edit followed this build; two unused-import deletions were reverted so the snapshot matches the current source. The 12 tracked UX4 PNG changes visible in the shared checkout predate this work and are owned by the root/user, not this implementation.

## Independent tester addendum: mounted-slide disposal

The tester extended only `kotlin/composeApp/src/webTest/kotlin/polski/ui/TrainingDomRendererLifecycleTest.kt`. Test-only browser scheduling capture retains cancelled callbacks and restores the native `setTimeout`, `clearTimeout`, `requestAnimationFrame` and `cancelAnimationFrame` methods in `finally`, including assertion failures. A separate listener probe likewise restores native `addEventListener`/`removeEventListener` in `finally`. No production scheduler or route behavior changed after the distribution above.

The added mounted-slide case observes the real incoming `.route-content` and inert outgoing layer, closes the slider, mounts a fresh root, and forcibly delivers the cancelled build timer and first rAF. It also closes separate slides with the second rAF pending and with the finish timer pending, then delivers those stale callbacks. It asserts no late mount/settle callbacks, one retained route layer, cleared outgoing accessibility attributes and an untouched fresh root. The added `WebNav` case counts one resize listener removal per closed instance, verifies the old two-stage init rAF cannot continue after close, and observes one nav shell on remount.

From `kotlin/`, `./gradlew :composeApp:jsBrowserTest --tests 'polski.ui.TrainingDomRendererLifecycleTest.closeDuringMountedSlideIgnoresDequeuedFramesAndTimerAndLeavesNewRootIntact' --tests 'polski.ui.TrainingDomRendererLifecycleTest.webNavCloseRemovesResizeListenerAndIgnoresQueuedInitialFrames' :composeApp:wasmJsBrowserTest --tests 'polski.ui.TrainingDomRendererLifecycleTest.closeDuringMountedSlideIgnoresDequeuedFramesAndTimerAndLeavesNewRootIntact' --tests 'polski.ui.TrainingDomRendererLifecycleTest.webNavCloseRemovesResizeListenerAndIgnoresQueuedInitialFrames' --console=plain --max-workers=2` passed **2/2 JS and 2/2 Wasm** on ChromeHeadless 156. Log: `/tmp/polski-p8-tester-w3-new-cases.log`; XML: `kotlin/composeApp/build/test-results/{jsBrowserTest,wasmJsBrowserTest}/`. `git diff --check` passed. An earlier full four-test JS-class run reported **3 pass, 1 fail**: the original prewarm test hit the Karma/Mocha 10,000 ms async timeout; the two added cases passed in that run. Log: `/tmp/polski-p8-tester-w3-extended.log`. The selected new-case result is the clean W3 extension evidence; the full-class timeout remains an open runner/test result, not a production behavior RED.

## Independent tester acceptance

On the exact immutable `cb180f5…` artifact, Playwright 1.55.1 / Node 24.1.0: Chromium/Wasm W1/W2 selection **17/17 PASS**, including the two one-layer pack-switch assertions and real exam typing/caret/focus/scoring; UX5 rapid/interrupted navigation **3/3 PASS**; Firefox/Wasm typing and same-route checkbox focus **2/2 PASS**. Artifacts: `/tmp/polski-p8-tester-w1w2-results`, `/tmp/polski-p8-tester-ux5-results`, `/tmp/polski-p8-tester-firefox-results`. No test/source changes were made by the tester. W3 JS/Wasm and sensitivity results above were reviewed and reused, not independently rerun.

The tester reproduced the same focused `TrainingStoreTest.tabNavigationResetsMatrixSelectionOnlyOnEntry` **10,000 ms async timeout** in an untouched detached worktree at pre-P8 `1e28907`. Log: `/tmp/polski-p8-baseline-trainingstore.log`; XML: `/tmp/polski-p8-baseline-test/kotlin/composeApp/build/test-results/jsBrowserTest/TEST-jsBrowserTest.polski.presentation.TrainingStoreTest.xml`. The focused current Desktop test passes. This establishes that this timeout predates P8; the broader JS gate remains FAIL and is not relabeled PASS. Independent review is pending.

## Final independent review

APPROVED after test-only correction of the W3 gap. Reviewer confirmed mounted incoming-layer disposal, remount, forced stale build/rAF1/rAF2/finish callbacks, no old completion, intact new root, cleared inert/aria-hidden state and WebNav resize-listener cleanup. No remaining production blocker was found. Selected new cases are JS 2/2 and Wasm 2/2 PASS. This approval does not change either recorded full-class/broad-JS timeout into PASS.

## Later runner correction

The recorded 10-second failures above are historical observations, superseded for current composeApp verification by [measured runner correction](P8-JS-TEST-HARNESS-EVIDENCE.md): cold synchronous test fixture setup took 15–17 seconds. With a measured 30-second Mocha limit and unchanged assertions, complete JS **32/32** and Wasm **32/32** pass, including all four lifecycle cases. Independent review APPROVED; production source/artifact is unchanged. This does not relabel earlier runs or the separate shared-module P10 JS run.
