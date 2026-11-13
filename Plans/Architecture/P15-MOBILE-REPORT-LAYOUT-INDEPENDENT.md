# P15 mobile report layout — independent web acceptance

2026-11-13. Source revision `be422a4cd2e8f151bb393e39efb1612cff010476`, previous production base `b81fcab`. The only production delta is `kotlin/composeApp/src/webMain/resources/exam.css`: review rows use a numbered grid and wrapping text. The source CSS and fresh compatibility distribution CSS have identical SHA-256 `d24d7af8b492b2e7c64704a58d678aa746aaf5560fb666c2fa61aa1ba0cddd7c`. Actual loaded JS and Wasm runtime scripts remain `2d6f6c0f89269d691a3e7ae7a8b5cd2ac63a85dc58ab45df6a9069f92c37789b` and `c6e34570890c96279ec7409364e3aae30cfa6926789c5a34dd73604a6409cc63`; this is a CSS-only correction. [Developer RED/GREEN evidence](P15-MOBILE-REPORT-LAYOUT-EVIDENCE.md) includes the old 375 px screenshots with clipped, squeezed columns. Its fresh build and 16-audio result are reused because the artifact hashes match.

## Independent checks

Working directory: `/Users/german/Work/JS/polski-grammar-srs`. Playwright 1.55.1, zero retries, one worker; actual compatibility distribution served on owned ports 4240–4244. The strengthened test observes the matching `originJsComposeApp.js` or `originWasmComposeApp.js` **network request**, not just a script element. Each case uses real answer and Submit input, checks visible chosen/source/blank/partial/pending/rubric content, waits for the document to settle within the new viewport, then checks every relevant field's bounding rectangle, nonempty text, internal scroll width, and page scroll width at 375, 320, and 1200 CSS px.

| Check | Result | Retained evidence |
|---|---|---|
| Grammar wrong choice/source/blank, Reading wrong/source, Listening partial/pending, C1/2022-11 four-item source rubric on Wasm Chromium | **PASS 4/4** | `../polski-grammar-srs-worktrees/recovery-logs/20261113/p19-integrated-web/live/mobile-report-independent-wasm-provenance.log` |
| Same four cases on actual JS fallback Chromium | **PASS 4/4** | `.../live/mobile-report-independent-js-provenance.log` |
| All four cases on Wasm Firefox and WebKit | **PASS 8/8** | `.../live/mobile-report-independent-other-provenance.log` |
| Existing computed contrast assertions for wrong/correct/partial/pending/report text in Light and Dark themes, fresh Wasm and JS Chromium | **PASS 2/2 each**; minimum observed ratio **5.47:1 Wasm**, **5.78:1 JS** | `.../live/mobile-report-independent-contrast-{wasm,js}.log` |
| Visual review of captured 375, 320, and 1200 px PNGs | **PASS**: all four report types retain readable chosen and source text inside the row; blank and pending labels, partial credit, and the C1 rubric wrap without clipping. Desktop rows remain legible. | `.../live/mobile-report-independent-screenshots/{wasm,js}/` (12 PNGs per runtime) |
| Native Simulator / physical device | **NOT RUN**; this change touches browser CSS only | — |

The final layout command was:

```sh
P15_LAYOUT_CAPTURE=1 \
KOTLIN_SPIKE_DIST=kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable \
KOTLIN_SPIKE_BRANCH=<wasm|js> KOTLIN_SPIKE_PORT=<4240|4241> \
npx playwright test -c playwright.kotlin.config.ts tests/browser/kotlin-exam-report-layout.spec.ts \
  --project=chromium --workers=1 --retries=0 \
  --output=../polski-grammar-srs-worktrees/recovery-logs/20261113/p19-integrated-web/live/mobile-report-independent-screenshots/<wasm|js>
```

The Firefox/WebKit command used the same distribution and `KOTLIN_SPIKE_BRANCH=wasm`, port 4244, `--project=firefox --project=webkit`, with capture disabled. Contrast commands selected `kotlin-grouped-practice-feedback.spec.ts --grep 'grouped feedback and frozen report text remain readable'` on fresh Wasm/JS Chromium, ports 4242/4243. The 24 retained PNGs were visually inspected; representative 320 px Grammar/Reading/Listening and 375/1200 px C1 images show complete text. The old `live-review-b81fcab-{wasm,js}.png` images show the earlier compressed, horizontally clipped fields.

## Preserved first failure

The initial JS Chromium run passed 3/4; after a 375 px screenshot the Reading case observed `document.documentElement.scrollWidth=375` immediately following the resize to 320 px, although its failed screenshot showed the report fields contained within a 320 px image. A focused rerun on the **unchanged production artifact** passed 1/1. The test now polls the observable document width for at most two seconds after viewport change, then still requires every report field and the full document to fit. Final JS and Wasm runs passed 4/4; the original `mobile-report-independent-js-chromium.log` is retained. The first failure screenshot and trace were in Playwright's default output directory and were cleared by a later command; the screenshot was visually inspected before cleanup. This is a transient resize observation, not evidence of a persistent production overflow; no production code was altered during independent verification.

The broad P19 exam/source/FSRS, language, Rive, and native evidence remains applicable because this patch changes CSS layout only. `npm run typecheck` and `git diff --check` are run on the final test-only delta. No source/data/scorer changes, Gradle rebuild, native install, Git index change, or publication occurred in this independent lane.
