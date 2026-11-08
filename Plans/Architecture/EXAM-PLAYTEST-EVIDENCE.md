# Exam web PlayTest pilot — 2026-11-08

## Contract and implementation

The authored browser expectations are in `dsl/exam-web/*.playtest`. The narrow Playwright host is `tests/browser/exam-playtest-host.ts`; it compiles those sources through the public `@biome-dsl/dialect-test` `compilePlan`, `createRuntimeRegistry`, and `runPlaymodePlan` API. `tests/browser/kotlin-exam.spec.ts` provides a fresh Playwright page per case. The host performs natural clicks and keyboard input, reads DOM/media state after actions, and keeps no business answer table. `BIOME_DSL_TS_ROOT` may point at the sibling checkout; the default resolves `../../Python/biome-dsl/implementations/typescript` from this repository.

## Browser evidence

Tested distribution: `kotlin/composeApp/build/dist/wasmJs/productionExecutable`, `composeApp.js` SHA-256 `b1524939fb6659647fc2df796847dd0af5c1779eec1cc0642b9302afa1602e30` before the later listening image/options source changes. Real `B1-2017.mp3` SHA-256 `e7d78c0f1bf64ef6ebf83e7d8b9d38f30539a2d33f205d40d230d56d03fb2747`; real `B1-2019.mp3` SHA-256 `55a563e5a53cb6f576e565fd4656fd705074cf25073a75804afeb19e3159ede1`. Chromium, Wasm branch, Playwright zero retries, loopback Vite preview on port 4187. `curl -I -H 'Range: bytes=1100000-1100099' http://127.0.0.1:4190/exam-audio/B1-2017.mp3` returned HTTP 206, Content-Range 1100000–1100099/23580465 and Accept-Ranges bytes on the same Vite preview serving the user browser.

The original listening defect had a real browser RED: selecting an answer after Play reset the player from ⏸ to ▶ on the old Wasm production distribution, while retaining the selected answer. Source inspection found answer selection redrew the player, sought to the task start, paused audio, and attached another pair of media event listeners. The new controller retains one audio element and listeners, updates selected controls in place, releases media on route exit/retry, and reports missing media. The grammar PlayTest passed on the old distribution as a baseline; B1/2017 quarter scoring and missing-media PlayTests passed on the rebuilt distribution.

The user reported B1/2017 listening Task V stopping after about 40 seconds. Its source segment is 1,100,000–1,394,000 ms (4:54) and the MP3 is 1,473.75 seconds long. The former Python static server and a naïve Playwright WAV `route.fulfill` both exposed `audio.seekable=[0,0]`; playback started at the file beginning, not task start. The positive test now uses the real MP3 with Vite preview's byte-range responses and no interception. `listening-passport.playtest` navigates naturally to task 5/5, waits for a real seek to at least 1100 seconds, asserts the 4:54 duration, then observes the recording still playing after more than 42 seconds, with absolute media time above 1140 and visible elapsed time above 40. Focused run: **PASS**, 43.3 seconds, `KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/wasmJs/productionExecutable KOTLIN_SPIKE_BRANCH=wasm KOTLIN_SPIKE_PORT=4187 npx playwright test --config=playwright.kotlin.config.ts --project=chromium tests/browser/kotlin-exam.spec.ts --grep 'passport task' --output=/tmp/polski-exam-playtest-results`; log `/tmp/polski-exam-passport.log`. This verifies the browser/server path used for the local preview, not every deployment server or real device.

The B1/2019 positive media case uses the same real-distribution Vite server. Focused run: **PASS**, Play → select answer → retained play state and nondecreasing media time → natural progress click to segment end → replay → route exit cleanup → re-entry. Command as above with `--grep 'answer, media boundary'`; log `/tmp/polski-exam-vite2.log`. The 404 test intercepts only the external media request to prove unavailable status.

The five-case checkpoint run on this distribution selected all five DSL cases: **4 PASS / 1 FAIL** (`/tmp/polski-exam-checkpoint.log`). The one failure was a host setup mistake: `open` awaited media metadata even though this case deliberately returned HTTP 404. After removing that wait, the failing subset was **PASS 1/1** (`--grep 'missing recording'`, `/tmp/polski-exam-missing.log`). The full five-case suite has not yet been repeated after that host correction; the independent tester will repeat it. No result here is labeled all-green on the basis of a subset.

## P25 boundary measurement

The pilot currently has five authored PlayTest scenarios in `dsl/exam-web/` (63 lines). The typed action/query registry and runner receipt give a declarative, independently reviewable assertion source (V4), and the same scenarios can run against Wasm and JS distributions without copying expected strings (V1, pending JS execution). The native Playwright host plus spec is 195 lines and still has only one browser host, so this pilot hits S1/S4 on code size/consumer count. Keep it narrow; no new syntax, general adapter framework, or claim of measured code-size savings. Media transport, DOM selection, browser isolation, and cleanup remain native.

## Checks and limits

- PASS: `npx tsc -p tsconfig.browser.json` at repository root after browser host changes.
- PASS: `git diff --check` for the owned source/config changes.
- PASS: `:composeApp:wasmJsBrowserDistribution` as reported by the build owner for the distribution hash above; Kotlin compilation is included in that task.
- NOT RUN here: JS browser branch, Firefox, WebKit, device browsers and native apps.
- The final source changes for B1/2019 reading, B1/2017 truth labels, and listening prompt image require a later rebuild and focused browser rerun; this evidence does not cover them.
