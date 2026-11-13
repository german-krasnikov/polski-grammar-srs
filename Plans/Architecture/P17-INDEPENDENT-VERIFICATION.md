# P17 language boundary — independent acceptance, 2026-11-13

Contract: [EXAM-LEARNING-QUALITY-PLAN.md](EXAM-LEARNING-QUALITY-PLAN.md) P17/A17. Tested worktree `/Users/german/Work/JS/polski-grammar-srs-worktrees/p17-language-boundary`, base HEAD `6e070111d117e203e5d153da43d625b33f7e2fdc` plus uncommitted P17 production/test changes. The tester added only browser assertions in `tests/browser/kotlin-en-course-switch.spec.ts`; no production files were edited. Logs and `.xcresult` are under `/Users/german/Work/JS/polski-grammar-srs-worktrees/recovery-logs/20261112/exam-learning-quality/p17/independent/`.

## Tested artifacts and targets

- Web compatibility production distribution: `kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable`, `originWasmComposeApp.js` SHA-256 `89ea6870519a384d049cf43b98de0e8dcbcd4a27d84871181637e2669744b432`. The actual browser resource entries showed `composeApp.js,originWasmComposeApp.js` in Chromium 140.0.7339.186, Firefox 141.0 and WebKit 26.0. No fallback script was loaded in these three runs.
- Direct JS production distribution: `kotlin/composeApp/build/dist/js/productionExecutable`, `composeApp.js` SHA-256 `3e2059f2bde8d1a7699c87cdf8fb695fe3ed62f7a05236506f6afe1e4fea31a9`. The actual browser resource entries showed only `composeApp.js` in all three engines.
- Native: Xcode 27.2 (27B52), iPad Pro 11-inch (M4) Simulator, iOS 26.1, UUID `AD9779D0-EAAC-40F0-8D29-C293BD5D5912`. `kotlin/iosApp/build/Build/Products/Debug-iphonesimulator/PolskiGrammar.app` has `PolskiGrammar.debug.dylib` SHA-256 `4640b28a6f36cee4895c73a040c2201ad47dddba5a2c21f53d0e765f589f308b`. Its 39 KB `PolskiGrammar` executable is a Debug launcher, so the dylib hash identifies the Swift implementation. `check-app` after the installed test **PASS**: 16 pinned audio and seven published images.

## Commands and results

All browser commands ran from the worktree root. The common command was:

```sh
KOTLIN_SPIKE_DIST=<absolute productionExecutable directory> KOTLIN_SPIKE_BRANCH=<wasm|js> KOTLIN_SPIKE_PORT=<4188–4195> npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-en-course-switch.spec.ts tests/browser/kotlin-en-matrix.spec.ts tests/browser/kotlin-en-acceptance-fixes.spec.ts --grep 'matrix shows only the active|English person × tense|do-support table|Matrix "Времена'
```

The distribution paths above and these exact arguments complete each command (the environment value `KOTLIN_SPIKE_DIST` was the absolute path to that row's distribution). `playwright.kotlin.config.ts` serves the directory through Vite preview and uses zero retries:

| Log | Branch | Port | Test arguments after `-c playwright.kotlin.config.ts` |
|---|---|---:|---|
| `wasm-compat-focused.log` | `wasm` | 4188 | three spec files and four-case grep shown in the command above |
| `js-focused.log` | `js` | 4189 | same |
| `wasm-runtime-check.log` | `wasm` | 4190 | `tests/browser/kotlin-en-course-switch.spec.ts --grep 'matrix shows only the active target language'` |
| `wasm-runtime-failed-subset.log` | `wasm` | 4191 | same, plus `--project=firefox --project=webkit` |
| `js-runtime-check.log` | `js` | 4192 | same runtime selector |
| `wasm-progress-preservation.log` / `wasm-progress-preservation-final.log` | `wasm` | 4193 / 4194 | `tests/browser/kotlin-en-course-switch.spec.ts --grep 'switching course keeps rated Polish training progress'` |
| `js-progress-preservation-final.log` | `js` | 4195 | same progress selector |

| Check | Result | Evidence |
|---|---|---|
| P17 source matrix: no English section or do-support under `pl`, real `see/saw`, `do/does/did` under `en`, no Polish conjugation section under `en`, return to `pl` without restart | **PASS 12/12** on compatibility/Wasm and **PASS 12/12** on direct JS, four selected cases × three engines each | `wasm-compat-focused.log`, `js-focused.log` |
| Actual runtime script loaded for that source-switch case | **PASS 3/3** compatibility/Wasm after failed-subset rerun; **PASS 3/3** direct JS | `wasm-runtime-check.log` (first run Chromium pass, Firefox/WebKit startup failure), `wasm-runtime-failed-subset.log` (both pass), `js-runtime-check.log` |
| A real rated Polish training card's 16 original card/FSRS records, per-skill stats and total review count survive `pl → en → pl`; active Training card follows each target | **PASS 3/3** compatibility/Wasm, **PASS 3/3** direct JS | `wasm-progress-preservation-final.log`, `js-progress-preservation-final.log` |
| Native snapshot `englishMatrixRowsFollowTheActiveTargetAcrossCourseSwitches` | **PASS 1/1** executed with `--rerun-tasks`; `targetLanguage` and English rows follow both target changes | `bridge-target-switch-rerun.log`, `bridge-target-switch.xml` |
| Existing native English forms/do-support snapshot regression | **PASS 1/1** executed with `--rerun-tasks` | `bridge-english-rerun.log` |
| Installed SwiftUI matrix `pl → en → pl`, real form a11y labels and do-support | **PASS 1/1** | `native-p17.log/.xcresult`, two exported screenshots in `native-p17-attachments/` |
| TypeScript typecheck, diff whitespace, app media bytes | **PASS** | `typecheck.log`, `git diff --check`, `check-app.log` |
| Physical iPad, Android, Desktop, full npm/Playwright suites | **NOT RUN** in this focused P17 acceptance | — |

Native command from the worktree root:

```sh
xcodebuild -project kotlin/iosApp/PolskiGrammar.xcodeproj -scheme PolskiGrammar -configuration Debug -destination 'platform=iOS Simulator,id=AD9779D0-EAAC-40F0-8D29-C293BD5D5912' -derivedDataPath kotlin/iosApp/build -resultBundlePath /Users/german/Work/JS/polski-grammar-srs-worktrees/recovery-logs/20261112/exam-learning-quality/p17/independent/native-p17.xcresult '-only-testing:PolskiGrammarUITests/PolskiGrammarUITests/testEnglishMatrixTableShowsRealFormsAndDoSupportSplit()' CODE_SIGNING_ALLOWED=NO test
```

Bridge commands from `kotlin/`:

```sh
./gradlew :apple-bridge:iosSimulatorArm64Test --tests 'polski.ios.IosMatrixSnapshotTest.*English*' --offline --max-workers=1 --rerun-tasks --console=plain
./gradlew :apple-bridge:iosSimulatorArm64Test --tests 'polski.ios.IosMatrixSnapshotTest.englishMatrixRowsFollowTheActiveTargetAcrossCourseSwitches' --offline --max-workers=1 --rerun-tasks --console=plain
```

The first selector matched only the older capital-`English` test; the second command selected the new lowercase-`english` case. XML recorded one non-skipped pass for each; only the second XML was copied because the result directory is overwritten by the next run.

## Failure classification and reused evidence

- The first extra compatibility runtime check passed Chromium but Firefox interrupted initial navigation with a duplicate same-URL navigation and WebKit timed out before reaching the matrix control. A focused rerun of just Firefox/WebKit passed, with actual Wasm script recorded for both. The original failures remain in `wasm-runtime-check.log`; they are startup/harness instability, not observed language-boundary failures. The earlier 12/12 compatibility run had already passed all selected browser engines.
- The first Polish progress check compared the whole serialized document byte-for-byte. Entering `en` legitimately adds 16 English cards to the same document, so that assertion failed 3/3. The final test compares every preexisting Polish card and FSRS value, Polish per-skill stats, and total reviews while allowing added English records; it passed 6/6 across the two distributions. This was a test-oracle defect, not a product defect.
- Developer evidence for `:composeApp:verifyNoWebSourceMaps` (JS/Wasm/compatibility build and 16 audio assets) and Xcode build was reused because no production source or distribution content changed during this independent test pass. The tester independently exercised both built web distributions and the native `.app`. A passing test is not code-review approval; final reviewer and P19 gates remain separate.
