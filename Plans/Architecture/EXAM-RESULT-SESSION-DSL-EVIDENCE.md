# P11 result and session policy implementation evidence

Date: 2026-11-12. Accepted contract: [EXAM-RESULT-SESSION-DSL-PLAN.md](EXAM-RESULT-SESSION-DSL-PLAN.md). Baseline for native LOC: `0b95ba2` (Set and published catalog checkpoint). This note records the developer slice; independent acceptance and review remain separate gates.

## Ownership and rule changes

The three authored decisions are `dsl/exam/result-policy.logic`, `part-action-policy.logic`, and `session-policy.logic`, each with a contract, independent `.examples`, and typed outcome rows. Kotlin extracts facts and applies the selected effect. Result finality and percentage visibility have one rule source. The four part reducers share one permission decision, including the explicit correction that an unknown Grammar item ID is ignored. The timed reducer uses the session decision for Start, dispatch, and one-part freeze priority. The existing quarter sums, answer/draft copies, deadlines, current module order, host clocks, media, and controller lifetime remain native mechanics. Swift UI receives shared Kotlin session snapshots through the bridge; it does not compute a verdict.

Authored decision/contract/example/outcome sources total 146 lines. The three typed data contracts add 18 lines, and the oracle derivation script only translates authored examples to Kotlin test rows. Generated commonMain policy and outcome files total 225 lines. New native adapters total 83 lines; seven existing score/reducer/card files grow from 247 to 341 lines, before the adapters. This migration is a guarantee and ownership change, not a LOC reduction. A rule change now touches one Logic decision and its authored example/outcome data, rather than four independent reducer permission ladders or score/card finality branches. The three policies have a web consumer through shared JS/Wasm; iOS bridge compilation and tests exercise the same shared source, while full native part/timed UI remains a separate gate.

## Observed verification

| Gate | Status | Command and environment |
|---|---|---|
| DSL generation | PASS | `node <biome-bin> generate --project dsl/biome.project` in repository root; generated Kotlin symbols matched expected `Policy.DispositionInput` and typed row enums. |
| DSL reproducibility | PASS | `node <biome-bin> generate --check --project dsl/biome.project` in repository root. The tool reported a stale toolchain pin warning; generated files compared byte for byte. |
| JS shared exam tests | PASS | `./gradlew :shared:jsBrowserTest --tests 'polski.exam.*'` in `kotlin/`, ChromeHeadless 157, rerun after restoring the policy and adding focused reducer cases. |
| Wasm and iOS bridge tests | PASS | `./gradlew :shared:wasmJsBrowserTest --tests 'polski.exam.*' :apple-bridge:iosSimulatorArm64Test` in `kotlin/`. Native linker emitted cache warnings, but the test task and build passed. |
| Web compilation | PASS | `./gradlew :composeApp:compileKotlinJs :composeApp:compileKotlinWasmJs` in `kotlin/`. Existing Wasm interop opt-in and source map warnings did not fail compilation. |
| Negative control | PASS | Generated temporary result policy with inverted pending condition and session policy without stale-module guard, then ran the two generated-case test classes in JS browser: 4 tests failed as expected. Restored the authored policies by regeneration; targeted `logic check` passed. |
| Browser app acceptance | NOT RUN | Shared tests do not prove the timed DOM timer or teardown. |
| Installed iOS Simulator UI | NOT RUN | Bridge tests do not prove SwiftUI rendering or input. |
| Physical iPad | NOT RUN | Deferred by owner. |

The Grammar unknown-ID regression and the native reducer cases were written after the policy integration, so they are regression evidence rather than test-first RED. The negative control demonstrates that the authored policy expectations detect two priority mistakes.
