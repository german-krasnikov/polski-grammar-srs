# Stage 2 independent acceptance evidence

Date: 2026-09-23. Contract: the temporary browser slice in [Plan.md](Plan.md), stage 2, and [WebImplementation.md](WebImplementation.md), not full React/Kotlin parity. Working directory for every command below: `/Users/german/Work/JS/polski-grammar-srs`.

## Tested artifact and runner

- Source: `df59774` plus uncommitted Stage 2 work. Production distribution: `kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable/`.
- Final artifact SHA-256: `index.html` `ab90fa6f9db56123f3ac4d6feaacafd6f83d8991ce0ca79f6054ebbe6b29db58`; loader `composeApp.js` `cb180f5c21a6756fde901f3bd13a2be73cb5402528213e08b2e23e7b078e2ea8`; app branches `originWasmComposeApp.js` `d398f3b160527ce4efadb6280934acb2cef3f67982b7b055382130768414d2cb` and `originJsComposeApp.js` `b3c1e400a5f6195670816308f588333cd07c12c63bbf5c0cae70464df8bca4df`.
- Tester-owned files: `tests/browser/kotlin-spike.spec.ts`, `playwright.kotlin.config.ts`, this report. Node 24.1.0, Playwright 1.55.1, Python 3.14.2; Chromium 140.0.7339.186, Firefox 141.0, Playwright WebKit 26.0.
- TypeScript **PASS**: `npx tsc --noEmit --skipLibCheck --moduleResolution bundler --module esnext --target es2022 --types node,playwright playwright.kotlin.config.ts tests/browser/kotlin-spike.spec.ts`.
- Wasm full suite **PASS 36/36**: `KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable KOTLIN_SPIKE_BRANCH=wasm npx playwright test --config=playwright.kotlin.config.ts`.
- JS full suite **PASS 36/36**: the same command with `KOTLIN_SPIKE_BRANCH=js`.
- Added natural Tab and 320 px/landscape/reduced-motion cases **PASS 6/6 per branch**: the same respective commands with `--grep 'Tab reaches|keeps controls usable'`. The suite now selects 42 cases per branch; the original 36 were unaffected by the later test-only additions.

The JS run simulates lack of WasmGC support by changing only the loader's small feature probe before navigation. A request for `originJsComposeApp.js`, rather than merely the presence of that file on disk, establishes that the fallback loaded. Default runs requested `originWasmComposeApp.js`. The JS renderer also downloads Skiko Wasm; that does not mean the app branch was Wasm. Every case uses a fresh Playwright context and the spike-only localStorage key.

## Acceptance results

| Contract | Check and result |
| --- | --- |
| Stage 2 / P04 | **PASS** on both branches and all three engines: named browser textbox, Polish diacritics and Russian prompt, hidden answer, Space/digit retained while editing, Enter reveal, Shift+Enter newline. **NOT RUN:** actual hardware IME composition and soft keyboard. |
| Stage 2 / P05 | **PASS** on both branches and all three engines: answer hidden until reveal; each of Again/Hard/Good/Easy is a reachable browser button and records its own visible rating. |
| Stage 2 / P07 | **PASS** on both branches and all three engines: natural Tab reaches textbox, reveal and rating; Space/Enter activate buttons; typing in textarea does not reveal or rate. Compose viewport adds a focus stop. In Playwright WebKit, focus returns to BODY after reveal, but Again is reachable by further Tab presses. This is an observation, not a failed stage-2 scenario. |
| Stage 2 / P08 | **PASS** on both branches and all three engines: browser table role, two column headers, seven row headers and values; horizontal wheel scroll at 320 CSS px. **NOT RUN:** human screen-reader reading of header/value context. |
| Stage 2 / P11 | **PASS** on both branches and all three engines: typed answer and chosen rating survive reload in isolated localStorage. This is temporary spike state, not legacy progress migration. |
| Stage 2 / P12 | **PASS** on desktop automation: Chromium, Firefox, WebKit, Wasm and forced JS; 320 CSS px no document-level horizontal overflow with 20 px root font, landscape reveal/rating and reduced-motion endpoint. **NOT RUN:** physical Safari on iPhone, Chrome on Android, real keyboard resize/touch, actual screen reader, offline/slow-network and performance thresholds. Playwright WebKit is not physical Safari. |

## Correction history and open gate

The initial production artifact exposed an unnamed textbox, canvas-intercepted role clicks, and generic table text without row/column roles. Those were reproducible **product FAILs** on the earlier branch bundles (`originWasmComposeApp.js` SHA-256 `921e82ea5dc4dc8d76ef7bee1460b2615f37a2d0d4a7515876e819950fcb3233`; `originJsComposeApp.js` `7402d34e1c324f46972f1a5306c415a6bd2fd0268af1019de2a5f8bf9119d3d1`). Traces remain under `test-results/kotlin-spike-*` when retained locally. The final artifact above passed the previously failed named-textbox, ordinary role-click and table-role cases in the full desktop suites. Tests added after implementation are regression/acceptance evidence, not retrospective proof of developer RED → GREEN.

The first natural-Tab test incorrectly required the first Tab to reach the textbox; the Compose viewport takes an initial stop. After measuring the actual focus order, the test was corrected to assert natural reachability within a bounded number of Tab presses. The corrected case passes in all three engines and both branches. No production behavior was changed for that test correction.

**Stage 2 remains open for required physical-device IME/soft-keyboard and human assistive-technology evidence.** This tester had no physical Android or iPhone host and did not substitute desktop emulation for them. Passing browser automation does not constitute code-review approval or validate later grammar, FSRS or legacy-progress parity.
