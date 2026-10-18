# Browser specifications and root validator guarantees

Inventory baseline: product `f1facd3`; spec bytes and configs unchanged at `a6c795b`. Counts below come from Playwright `--list` on the isolated worktree with the checked-in configs; **no browser test ran** in this inventory. The Kotlin config required a previously built `index.html` path only to load its config. Historical PASS logs are separate and do not establish a Kotlin-authored replacement. Source SHA-256 covers each whole spec. All final Kotlin/JVM Playwright replacements are **NOT MAPPED / NOT RUN (0 selected, 0 executed)**. The three configurations are distinct:

- Root `playwright.config.ts`: 23 selected React scenarios in 4 files, Chromium only (`npm run test:browser`).
- `playwright.kotlin.config.ts`: 513 engine-runs = 171 authored TypeScript scenarios × Chromium/Firefox/WebKit, 27 files, per JS or Wasm branch. These target the Kotlin app but are still TypeScript tests and must be replaced before TS removal.
- `playwright.perf.config.ts`: 3 separate Chromium perf variants in `tests/perf`; not part of the root or Kotlin config selection.

| Current config | Spec source | SHA-256 | Selected by `--list` | Final Kotlin/JVM mapping |
| --- | --- | --- | ---: | --- |
| React/default | `tests/browser/method-cycle-react.spec.ts` | `b453cd6b4eab61ae98aa81b519805be00abca00c9302c551329d20a45b6fae71` | 1 | NOT MAPPED |
| React/default | `tests/browser/react-acceptance.spec.ts` | `dfca2fa326b8045095b72ae17b4506f9acfce9eb340d84c53e7e184eaa8d1494` | 3 | NOT MAPPED |
| React/default | `tests/browser/react-baseline.spec.ts` | `e16f111d5552a6be4f447d422636c6801ae81494a1f14270611b5554a12d4577` | 17 | NOT MAPPED |
| React/default | `tests/browser/react-theme.spec.ts` | `f34edb3a7aaabbe35e6027b7ab5bddf57c36d3c83cad7029d87147ad55759626` | 2 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/flip-rive-perf.spec.ts` | `809b5d6f99bf8cb02306914e84211dde09844e58709469a28f75248c5d621193` | 18 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-binary-rating.spec.ts` | `f579a2eb200090cd022699014ab43e143ea4c0cc590fbbe3da76efc7b025a299` | 9 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-emphasis-underline.spec.ts` | `7dcb1e3d51ef748d73f233c7015246680f465badd3cb5f2f48f17692ea689541` | 9 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-en-acceptance-fixes.spec.ts` | `4d43a29dcf0d8d84d2840252e7ba79b53543ed3768a02f494bb8aef10184d49e` | 18 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-en-course-switch.spec.ts` | `f4e9c3faa7b314ff9f08fe8d52432dce2bcd734a75c220c55dbe92c7d2850ea2` | 12 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-en-matrix.spec.ts` | `083ad846be156bc1cce87a1771a7ac35bc5803d2d956c915c1091bbaee4fefce` | 6 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-flip-card.spec.ts` | `071422e749b2cd4335a42569899f54409a865ec5b7ebd9305f255cbe62548ddd` | 45 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-inventory-content.spec.ts` | `b515b975e76dff82278bd87c7971877663620c23dede2855166223b7bed91def` | 3 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-lifehack-block.spec.ts` | `49e62059906af06cb2d03fe44b331ad8368ffcd3310c5cad23b99e082b395fb2` | 18 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-matrix-progress.spec.ts` | `b8edc3c04edc9994a92f481b316c7463df45456068a6ae58786d240e6ef02fe7` | 21 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-method-cycle.spec.ts` | `9e108795d332181ae50003954f4103d0072b6165a5384693d0bdfa5f313af871` | 3 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-native-web.spec.ts` | `fea98a7343b847372fb6676766c3de9a88f8149cc407046cc8147138f97b83d1` | 15 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-parity-chain.spec.ts` | `8864d7f44fd06bd69dee05364a3fe4e2be76044f830aefc7f80fced9b1df16b0` | 6 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-parity-matrix.spec.ts` | `f645ac917dd15a4c0cfca4d2e578cf92c4c6544f843bc9e0753fb6690d9d2c68` | 21 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-parity-progress-load.spec.ts` | `ed80eb07abb2509cd1aef188cf2a3b2c33368c97c9ff9aa895a358f5805d9a27` | 15 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-parity-progress-recovery.spec.ts` | `8448de26ab2f625aa56b3e46eea022a43571770cad428adc6d89fb387a81333d` | 9 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-parity-progress-review.spec.ts` | `b657b31fe06d0ca775c97543dc20b482700afff269070f3744a119fc0d509c9f` | 6 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-parity-ratings.spec.ts` | `da479d5cce853551554a15505be5d40d395ebd76801759ac752e0587fe3808ed` | 9 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-parity-skill-picker.spec.ts` | `189e8282920bb2dbebf9f0c131392407e5c7661a679b0a93443bcf4e7f4ebbc5` | 3 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-preferences-settings.spec.ts` | `e6e9b2a326e6507f2c04cb7d0f1e3899429d869c5063e385782f7f2811a13fd4` | 72 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-progress-local-day.spec.ts` | `d5dd164f994d4a8595ccffcc080284a7708b6a80d0105077b3acb1de59884dcd` | 21 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-progress-migration.spec.ts` | `a27951bb4045da0ef766b0391ee0591dfaf8ee0d0db8d9fde0030e950dc7e8a9` | 9 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-reference-panel-leak.spec.ts` | `b86a302184d9575f1d35facb7199927d61533edf4cf474e2beb2e3b5d435d84f` | 3 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-style-blocks.spec.ts` | `0627f2a1147c7fedaedc11016d2b4d1c682039d320f7e642d2b7f7639b5930a1` | 9 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-training.spec.ts` | `d282ca7986d6072b4d46bee9da8fec9af99a94da60b5ef181af989629178a262` | 21 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-ux4.spec.ts` | `755035e5f3ba9ed45964be9deaaa0fe4c85289f9af7fb509284fabb26487c70d` | 84 | NOT MAPPED |
| Kotlin-web/TS | `tests/browser/kotlin-vocabulary.spec.ts` | `6567b80998ec85a4b76c1b7b6369a3c7e2a542a5f98651064761f504217b6bdc` | 48 | NOT MAPPED |
| Perf | `tests/perf/flip-rive-measurements.spec.ts` | `33e8cf44afab53d7326ee6b7e49241face328d0184fdd22af23d492fc6eae093` | 3 | NOT MAPPED |

## Validator and generator inventory

A CLI command can be a generator, a validator, or both. Preserve each observable domain rejection and freshness check in Biome/Gradle before deleting its owner; the name alone does not prove coverage. `Known caller / role` records checked-in `package.json` commands, test imports or a named frozen-control workflow. `NONE` means no direct root package command was found; it is **not** evidence that the script is unused by other workflows. Production semantic replacements below are **NOT MAPPED / NOT RUN**. Historical control scripts can be archived after parity; the small Python path guard can remain as native tooling under P4. SHA-256 is over the complete source file.

| Root source | SHA-256 | Known caller / role | Guarantee / disposition |
| --- | --- | --- | --- |
| `scripts/validate-course.mjs` | `da749d20ce7265300077fa49e9368dd030b2e712796caa4d5c7fa2f5b91fc013` | course:validate; imported in course tests | Course schema, references, presentation text and negative cases; NOT MAPPED |
| `scripts/validate-vocabulary-editorial.mjs` | `56c86f6de4ae83b577845556f3b9333919f0f9d207d4777a15a3805fff92781e` | course:validate --strict; vocabulary:editorial:*; imported tests | Vocabulary editorial completeness/collision/strict rejection; NOT MAPPED |
| `scripts/validate-curriculum.mjs` | `47c36a9b1340f6946967614fe4a66be5f466c504edda21db53df29d16aaf7e8e` | course:validate | Curriculum structure, ordering and prerequisite/reference checks; NOT MAPPED |
| `scripts/validate-pack-v2.mjs` | `e954048a5b344f46219cbf7e1d08e9676101043911313df61bfe17bee0151080` | course:validate; pack:validate:v2; pack-v2 tests | V2 core/lang/pair schema and cross-reference rejection; NOT MAPPED |
| `scripts/validate-lifehacks.mjs` | `b9ae264d7956e6deaade178d7a856d843ab24c65cd9c0bebebce51f08a4df563` | lifehacks:check; imported tests | Lifehack content/reference validation for both language pairs; NOT MAPPED |
| `scripts/inventory-course.mjs` | `5150b553db1b37cbae0d62414ba98a4233c7f2f7291c932601c68f913d9d88b5` | course:inventory:check; checker subprocess; imported tests | Source literal discovery and authored inventory freshness; NOT MAPPED |
| `scripts/check-course-inventory.mjs` | `0bdf843b68e3980594260b49a07663ec03a23df379a936a6cc8a128af1d138b6` | course:inventory:decisions:check; imported tests | Inventory decision coverage, missing/duplicate/stale rejection; NOT MAPPED |
| `scripts/migrate-v1-to-v2.mjs` | `51992e41d712f92379e4f8d9b0ab3f40813c88d5dd4f7fd5a213d8d4fc5b2af3` | pack:migrate:v2(:check); imported pack-v2 tests | V1↔V2 reconstruction equality and split freshness; old JSON output retired; NOT MAPPED |
| `scripts/build-pack.mjs` | `8d490d904e82ce49925566e8dadeeb94775ff2e2964047f43da5616fa17b5109` | pack:build(:check) | Polish forms generation and byte-for-byte freshness; NOT MAPPED |
| `scripts/build-pack-en.mjs` | `24af6326fbb03633a98c1f8d67fb804009f92647625c4af2bdeedf0fca887efc` | pack:build:en(:check) | English forms generation and byte-for-byte freshness; NOT MAPPED |
| `scripts/extract-course-morphology.mjs` | `e055a1e9988c3524919f9430cfcb54355d863645e79dae5cfc8b435cc1c59e1a` | NONE (manual --write/--check) | Morphology extraction and freshness if still in build graph; NOT MAPPED |
| `scripts/check-no-app-json-tracked.py` | `53c24ca6b7baf3745e41fb0d84251ec7857bf8cf372e9f9be4324ae452b1e00f` | `tests/check_no_app_json_tracked.py` | Tracked-path JSON exception allowlist and rejection; **RETAIN native P4 candidate**, final bundle scan still open |
| `scripts/check-nj03-old-curriculum-facts.mjs` | `0f5f3c4b75117d05d6412803e1217ba3f97a303f9a225bd9dcd66fa9a47c81fb` | NJ03 frozen-control workflow | Old curriculum fact oracle; **test-only until DSL/Kotlin comparison, then archive** |
| `scripts/check-nj03-old-seeds-chains.mjs` | `806000585d3811c08fb2fe3dc65f356c57a53596280875c3ec0772ba401aeec7` | NJ03 frozen-control workflow | Old seed/chain oracle; **test-only until DSL/Kotlin comparison, then archive** |
| `scripts/check-nj03-old-chain-react.mts` | `6298bd0c10a33fa396c14a71df76cc1887a0fee8ff1757adb2aa240382d57c08` | NJ03 frozen-control workflow | Historical React chain observation; **retire after Kotlin parity** |
| `scripts/nj01/probe-glass-run-rows.mjs` | `7b4600d8062b95af40c57e3b403a67be622ce61840e4833f5f5d8ed56da135d5` | NJ01 isolated measurement | Experimental measurement, no product-validator guarantee; **archive experiment** |
| `tests/fixtures/kotlin-parity/generate.ts` | `1e02998ce56ca9c99421ee30fd13905715341f001a9311aeb66be2dfda0cb7f9` | `fixtures:generate` | Historical parity fixture generation from React baseline; **retire only after independent DSL/Kotlin expectations are frozen** |
| `tests/fixtures/kotlin-parity/capture.ts` | `51312e41f42284b2aa66a027ececaed571aed41895971a453cfeedd5197d99ac` | imported by fixture generator/tests | React capture helper, not independent expectation; **retire with fixture generator** |

Seven `scripts/*.d.mts` files are TypeScript declarations for the active JS validators/generators (`check-course-inventory`, `inventory-course`, `migrate-v1-to-v2`, `validate-course`, `validate-lifehacks`, `validate-pack-v2`, `validate-vocabulary-editorial`). They define types but execute no validation; retire them with their runtime scripts after the Gradle/CI dependency trace.

## Replacement command and evidence contract

Before retiring the 23 React browser cases, implement a Kotlin/JVM browser driver that launches pinned Chromium, Firefox and WebKit against a **fresh** Kotlin JS and Wasm distribution and has an independently reviewed row per accepted assertion. Run a selected one-case smoke per engine first, then the complete mapped suite. Record distribution hash, driver/browser versions, selected/executed/skipped/failed per branch and engine. Old JSON export assertions are requirement-change candidates: preserve user-visible data and recovery behavior under the text-wire contract, but do not reproduce JSON output. A React theme assertion is not automatically a Kotlin UI requirement without independent acceptance. Kotlin Playwright TS specs already target Kotlin UI, yet still require Kotlin-authored replacements and a per-assertion review; their 513 `--list` results are **selection**, not execution.

For production semantic validators, run the future generated-artifact `--check`, negative schema/reference cases, Kotlin target tests, and clean Gradle build on the same immutable source SHA. Their final replacement command and counts are currently **UNDEFINED / 0/0/0/0**. Audit Gradle task dependencies and CI before removing Node/npm tools; the Python path guard may remain if its scoped behavior and negative test stay valid.
