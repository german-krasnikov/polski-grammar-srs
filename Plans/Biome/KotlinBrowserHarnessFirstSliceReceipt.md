# Kotlin/JVM browser harness: implementation receipt

Status: **test-only harness implemented; accepted browser navigation remains NOT RUN**. This is the first executable slice of [KotlinBrowserHarnessFirstSlice.md](KotlinBrowserHarnessFirstSlice.md), not a Kotlin/DSL parity claim. Product source baseline is `56eb148d722968ad6df373ded04144216751a2b9` on `biome/full-port-2026`; the isolated implementation commit is `70a3a599bcb5f78e6392637bc8c0bd3ebb203252`, followed by the independently authored fail-closed controls cherry-picked as `ef9eb5482eb2864f60412ea459e4ea3714e92bfb`. The compiler checkout observed during verification was `eda6ea20583837f1591f75926aca6e69b988b74f`, but no compiler API was called by this harness. Host: macOS `aarch64`, JDK `21.0.12.1`.

## Implemented boundary

`kotlin/browserAcceptance` is a Kotlin/JVM JUnit module. Its two explicit branch tasks depend on the matching Gradle browser distribution, select exactly `SettingsRouteSmokeTest` when the preflight is green, and use separate XML/report directories. The ordinary `test` task selects only the server/artifact helper cases; `driverProbeTest` and `distributionIdentityTest` are explicit diagnostics. Browser tasks disable up-to-date reuse and use one fork, no retry, and a fresh context/server/browser per engine. The independent assertion clicks the visible “Настройки” button on the Training route and expects the visible “Настройки обучения” heading. It does not derive that string from product source or generated output.

The distribution helper rejects a different root, symlink escape, traversal, missing output, branch mismatch, and a distribution `index.html`/`composeApp.js` that differs from this worktree's processed resource/Webpack output. Wasm identification compares the selected distribution payload with the optimized Kotlin application Wasm bytes, verifies the bundle references that file, and later requires the actual HTTP response bytes to match. It does not infer Wasm execution from a shared Skiko request. The server uses an ephemeral `127.0.0.1` port, serves only selected files, checks index readiness, and closes its executor. The browser scenario records unexpected page/console errors and suspicious local-storage writes without moving application documents into a machine reporter. These runtime checks remain unexecuted until the no-JSON preflight is green.

## Commands and observed evidence

All Gradle commands below ran from `/Users/german/Work/JS/polski-grammar-srs-kotlin-browser-harness-dev/kotlin` on the same source baseline, with only harness/test/docs edits in the worktree. No root npm or TypeScript browser runner was used.

| Command / control | Selected → executed / skipped / failed | Result and scope |
| --- | --- | --- |
| `./gradlew :browserAcceptance:playwright --args=install --console=plain --max-workers=2` | CLI task 1 → 1 / 0 / 0 | PASS. Firefox `155.0` revision `1543` and WebKit `26.6` revision `2359` downloaded; Chromium revision `1243` already present. Installation alone is not UI evidence. |
| `./gradlew :browserAcceptance:driverProbeTest --console=plain --max-workers=2` | 3 → 3 / 0 / 0 | PASS after restore. Chromium `153.0.8010.12`, Firefox `155.0`, WebKit `26.6` launched and closed. Playwright Java JAR SHA-256 `f7bf1cb0fcc15d3de5e2d4f490cf9a8187cb03f14a43de625d1a03e03334e45a`. |
| `./gradlew :composeApp:jsBrowserDistribution :composeApp:wasmJsBrowserDistribution --console=plain --max-workers=2` | 2 distributions → 2 / 0 / 0 | PASS, 105 Gradle tasks: 96 executed, 9 up-to-date, 1m39s. Kotlin/JS and Wasm bundle outputs were refreshed in this worktree. Kotlin's own build-only Node/Yarn steps ran; this does not require the historical root npm app. |
| `./gradlew :browserAcceptance:test --console=plain --max-workers=2` | helper 3 → 3 / 0 / 0 | PASS on implementation commit `70a3a59`, after HttpClient cleanup. HTTP content/containment, missing/outside output and corrupted bundle are discriminating local tests. The later independent tester ran 6/6, including three added preflight negatives, on `7c1529f`. |
| Same command after cherry-picking independent controls into `ef9eb548` | helper 6 → 6 / 0 / 0 | PASS on the current isolated source in 5 s; current XML has `DistributionServerTest` 3/3 and `NoJsonReadinessIndependentTest` 3/3, both with zero skips/failures/errors. This tests the helper and rejection controls, not browser UI. |
| `./gradlew :browserAcceptance:distributionIdentityTest --console=plain --max-workers=2` | identity 1 → 1 / 0 / 0 | PASS on final helper source and fresh distributions. The JS/Wasm bundle hashes differ and exactly one distributed Wasm matches the optimized Kotlin application payload. |
| `./gradlew :browserAcceptance:driverProbeTest -Ppolski.browser.denyEngine=WEBKIT --console=plain --max-workers=2` | 3 → 3 / 0 / 1 | Expected negative FAIL: Chromium and Firefox passed; the missing WebKit executable caused a Playwright launch failure, not a skip. Restored task then passed 3/3. |
| `./gradlew :browserAcceptance:browserJsTest --console=plain --max-workers=2` | initialization check 1 → 1 / 0 / 1; UI 0/3 | Expected fail-closed result before any browser launch: `Old progress writer still emits JSON on fresh browser startup`. This is neither a browser assertion failure nor a six-case PASS. Wasm browser task remains NOT RUN. |

Selected distribution identities from `DistributionIdentityTest` JUnit XML:

| Artifact | SHA-256 |
| --- | --- |
| JS `index.html` | `9076135ac21f8c16177edc4461e767b84d6837bae8c59c7f6d44edf9650b9c36` |
| JS `composeApp.js` | `0441216bacbac5387ca2546b065b5bb49ada4f088a0f2a546524cba1116c1eab` |
| Wasm `index.html` | `9076135ac21f8c16177edc4461e767b84d6837bae8c59c7f6d44edf9650b9c36` |
| Wasm `composeApp.js` | `88228f5939b8251c43930305f6e4163c61275af65733eacb531009e7e6443300` |
| Kotlin application `026671ec17d3896a1f6d.wasm` | `684d7f1d025f83fe92c1cabbe4f4d6c2f31249ed8dc5e316e9e21a516ca87917` |

Installed executable paths were observed by `DriverProbeTest` and appear in its plain JUnit XML stdout: Chromium `/Users/german/Library/Caches/ms-playwright/chromium-1243/chrome-mac-arm64/Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing`; Firefox `/Users/german/Library/Caches/ms-playwright/firefox-1543/firefox/Nightly.app/Contents/MacOS/firefox`; WebKit `/Users/german/Library/Caches/ms-playwright/webkit-2359/pw_run.sh`. These are local tool-cache paths, not product artifacts.

## Open product gate and next execution

The full tracked no-app-JSON audit `python3 scripts/check-no-app-json-tracked.py --summary` is still **RED** on this baseline: 111 tracked `.json` paths, 10 exact tool paths, 0 accepted legacy inputs in this baseline, 101 violations. `RawProgressRepository.save()` still calls `ProgressCodec.encodeLegacyV1(document)` on fresh profile startup, so the browser test rejects execution before it can count navigation. The JS bundle still has `application/json` paths. The local generated distribution directories also contain optional machine-format source maps. This probe does not prove whether a deployment currently ships or serves them:

| Local generated file | Bytes | SHA-256 | State |
| --- | ---: | --- | --- |
| `kotlin/composeApp/build/dist/js/productionExecutable/composeApp.js.map` | 4,900,747 | `5e99bc0dd081e26b2896c0d08b982e5c7143af8807db1d9a9dbfd8bc45e717db` | OPEN: starts with `{`, JSON-format source map. |
| `kotlin/composeApp/build/dist/wasmJs/productionExecutable/composeApp.js.map` | 1,498,535 | `64cd84b19fe24a17ae60ed0d68a67c4c3d54ee00571d1096649132eac5987a3f` | OPEN: starts with `{`, JSON-format source map. |

The harness now rejects `.map` files in its selected distribution. Product bundling was not changed in this test-only slice; final NJ08 must exclude these optional JSON maps from the shipped distribution or replace their format. The exact no-JSON policy, startup/save replacement, generated/data migration and storage proof belong to their product lanes. No accepted browser test, normal-storage check, page/console error check or six-engine cleanup receipt has run yet. The historical heading-mutation negative control also waits for the same text-wire gate; a fake assertion failure on this JSON-writing baseline would not be accepted proof.

The preflight's exact source-substring and bundle-media-type checks, plus its browser hook for local-storage values starting with `{` or `[`, are interim fail-closed controls. They cannot prove the absence of JSON scalars, other stores, downloads or equivalent writer code. The independent tester added three negative preflight controls and observed 6/6 helper PASS in its isolated worktree; these controls test the guard's stated rejection paths, not complete NJ08 coverage.

After the text-wire/no-JSON product gate is green, run both branch tasks from the same clean product SHA and require `3 selected / 3 executed / 0 skipped / 0 failed` each in Chromium, Firefox and WebKit. Re-run the wrong-heading, corrupt-selected-bundle and denied-executable negatives, restore, then re-run all six. Review the raw XML and text receipts before marking only this settings-navigation assertion as mapped. The other 171 Kotlin-web historical scenarios, three browser engines per branch, and 23 React controls remain NOT MAPPED/NOT RUN.

The first native harness costs 594 authored lines across its Gradle file and six Kotlin test/helper files (`wc -l` after the independent controls), including 148 lines for reusable HTTP/artifact mechanics, 84 lines of HTTP/artifact helper negatives and 63 lines of no-JSON preflight negatives. A heading change has one test expectation edit site, while artifact names and paths are mechanical wiring. Startup time, one accepted case runtime and marginal cost per additional scenario are **NOT MEASURED** because no accepted UI case may run yet. No project DSL browser executor exists here; a DSL adapter has no proved extra guarantee or measured cost in this slice, so the native host remains the bounded choice under P4/P8/P17. Revisit the comparison after the text-wire gate and a second real browser scenario.
