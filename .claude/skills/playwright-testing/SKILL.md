---
name: playwright-testing
description: "Create, debug and review Playwright browser acceptance for the React baseline and Kotlin/Wasm/JS web app: input, storage, semantics, layout and parity. Keep pure domain tests in Vitest or Kotlin commonTest."
---

# Playwright browser testing

## Project scope

- Read `package.json`, the lockfile and any browser configuration first. Playwright is not assumed installed. Adding it is an implementation step when browser automation is in scope, not a prerequisite for writing a plan.
- Use [the Kotlin migration plan](../../../Plans/Kotlin/Plan.md) for parity cases and target browser scope. The source project's Chromium-only policy is not inherited here: Chromium can establish an initial smoke lane, but cannot prove Safari/iOS compatibility.
- Label React source integration, Kotlin source integration and tested production artifacts separately. Installation/discovery alone proves none of them.
- Use [testing-tdd](../testing-tdd/SKILL.md) for general evidence and [kmp-web](../kmp-web/SKILL.md) for web/platform constraints.

## Installation and configuration

1. Restore locked dependencies with the actual package manager. If setup is authorized, add a selected compatible Playwright version and matching browsers; preserve unrelated manifest edits.
2. Use Playwright-managed isolated browser processes. Personal browser profiles or remote debugging are unnecessary for automated acceptance.
3. Reuse configuration. For a new lane, keep browser files separate from Vitest discovery and include TypeScript browser test/config files in a real typecheck; Playwright transforms TS without typechecking it.
4. Set finite server/test/assertion timeouts, `forbidOnly` in CI and failure artifacts. Project fallback: acceptance evidence uses zero retries with `trace: 'retain-on-failure'` and screenshots on failure. If a lane uses retries, expose the initial failure and classify flakiness.
5. Configure `webServer` from an existing command and readiness URL; bind to loopback locally. Avoid stale servers in CI. Identify the exact React/Kotlin build and browser project used.
6. Keep browser binaries and reports out of version control; retain test sources, configuration, lockfiles and reviewed visual baselines.

Consult [configuration](https://playwright.dev/docs/test-configuration), [web server](https://playwright.dev/docs/test-webserver) and [browser installation](https://playwright.dev/docs/browsers) for current APIs. Do not create empty jobs or placeholder assertions to mark a planned lane green.

## Input, waiting and assertions

- Prefer roles, labels, visible text and meaningful containers; use stable test IDs where semantics cannot express the target. Await actions and auto-retrying assertions. [Best practices](https://playwright.dev/docs/best-practices)
- Register event/response waits before their triggering action. Wait for the expected UI/state condition; fixed sleeps and `networkidle` are not readiness contracts.
- Do not bypass natural input with `force`, `dispatchEvent` or direct state mutation. A fixture may set initial state, but must not set the expected outcome of the action under test.
- A Compose canvas need not expose each composable as a DOM element. Inspect the actual accessibility/semantics bridge before selecting locators. Where real coordinates are required, derive them from bounds, scale and a documented input contract; pair input with semantic/domain output evidence. Do not invent hidden writable testing globals.
- Exercise keyboard focus, typing, shortcuts, pointer/touch, scrolling and selection in representative user flows. Typing Polish characters and IME composition must not trigger grading/navigation unexpectedly.
- Mock only real external boundaries. Never replace the grading handler, queue reducer, renderer or storage adapter with a success-producing fake in its own acceptance case.

## Isolation, lifecycle and storage

- Use a fresh page/context per independent case. Do not share mutable pages or browser storage between tests.
- Test persistence separately by reloading the same context/origin after grading, and test cleanup/re-entry on the same live app where needed. A fresh page can conceal leaked listeners or jobs.
- Compare React and Kotlin against equivalent copied fixtures at isolated origins/contexts. Do not let one implementation overwrite the other's baseline data. Validate legacy key/JSON migration and failed import/write behavior.
- Collect `pageerror` and relevant console errors before navigation; assert unexpected errors explicitly. Match expected failures narrowly and keep collection active through teardown.
- Close owned contexts/resources in fixture teardown or `finally`. Browser loss, missing readiness, stale replies and cleanup failures are non-PASS.

## Parity and visual evidence

- For each scenario, verify input, visible result and the relevant learning/progress state. A click, a resolved action or a screenshot alone does not prove grading or persistence.
- Cover answer reveal, oral/typed mode, correct/incorrect grading, queue advancement, shortcuts/double-submit prevention, table filters, navigation and progress reload according to the baseline contract.
- Fix viewport, browser/OS, font loading and scale for screenshot comparisons. React DOM and Compose rendering may need separate reviewed goldens; compare meaningful layout and behavior, not cross-renderer pixel identity. Never update goldens automatically after failure. [Visual comparisons](https://playwright.dev/docs/test-snapshots)
- Test animation endpoints and interrupted/repeated interactions independently of visual playback. Control or disable nonessential motion for stable snapshots; separately verify natural motion and reduced-motion preferences.
- Mobile emulation is useful but does not prove native app or real Safari behavior. Record engine/version/device evidence separately; WebKit automation is not identical to shipping Safari.

## Running and diagnosing

Use actual package scripts and configured projects. Once installed, `npx playwright test --list` checks discovery and `npx playwright test <existing-file> --project=<configured-project>` runs a focused case. Substitute real paths/names; discovery is not a test pass.

Inspect the first failed assertion and retained trace before increasing timeouts or changing selectors. Fix the cause before rerunning. CI must select nonempty required scenarios, serve the intended artifact, and retain failure artifacts on failure. Limit workers according to actual isolation/resources.

Report commands, selected cases, browser/tool versions where relevant, tested build, outcome and artifact paths. Keep installation smoke, behavior parity, visual review and mobile-browser acceptance distinct. Traces and storage snapshots can contain user progress; use neutral fixtures and share only within authorized scope.

Official sources checked 2026-09-23. Zero-retry acceptance is a project convention, not a Playwright default.
