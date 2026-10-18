# NJ01 JSON inventory — product `81443efa6b823bbb220c7f24ae70157ccc1518c1`

Status: **source and tracked-path inventory, not NJ01 acceptance**. This is an independent read-only inventory of the canonical product tree, prepared under [NoJsonFullPortPlan.md](NoJsonFullPortPlan.md) and [NoJsonAcceptance.md](NoJsonAcceptance.md). Application code, the plan, and the acceptance oracle were not edited. The product's zero-JSON contract overrides the compiler's general allowance for generated JSON. Classification below is by **exact tracked path**; a directory or `*.json` pattern is never an exception. The observed 154 paths divide into 10 tool-required paths, 141 app-owned course/schema/fixture/history paths, and 3 Kotlin test resources. Two of those three are *candidate* immutable legacy-user inputs; no input exception is approved yet.

## Tracked path ledger

`git ls-files '*.json'` returned **154** paths at the stated SHA. Legend: **T** = exact-path tool-required exception, conditional on tool-only content; **A** = app-owned active source/generated/validator/test artifact that must leave the accepted tree; **H** = app-owned historical report/evidence, development read-only only, to leave the accepted tree; **L?** = candidate input-only legacy-user migration fixture, requiring byte-hash freeze and packaging exclusion before approval. The `Role / consumer` column is a source-level attribution, not proof of execution. All paths in this ledger are relative to repository root.

| Exact tracked path | Class | Role / consumer |
| --- | --- | --- |
| `Plans/Kotlin/artifacts/final-virtual/android-legacy-after.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/final-virtual/android-legacy-homonym.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/final-virtual/ipad-files-export-v1.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/final-virtual/ipad-full-tests.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/final-virtual/ipad-private-after-files-import.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/final-virtual/ipad-typed-postfix-attachments/manifest.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/final-virtual/iphone-failures/manifest.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/final-virtual/iphone-focus-postfix-failure/manifest.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/final-virtual/iphone-pronoun/manifest.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/final-virtual/iphone-verb/manifest.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/final-virtual/manifest.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/flip-rive/android/leak-check-15-cycles-flip_rive.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/flip-rive/android/variant-A-no_animation.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/flip-rive/android/variant-C-flip_rive.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/flip-rive/ios/perf-report.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/flip-rive/v1-measurements.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/flip-rive/v3/web/first-motion-after.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/flip-rive/v3/web/first-motion-before.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/flip-rive/v3/web/first-motion.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/flip-rive/web/variant-A-js.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/flip-rive/web/variant-A-wasm.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/flip-rive/web/variant-B-js.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/flip-rive/web/variant-B-wasm.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/flip-rive/web/variant-C-js.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/flip-rive/web/variant-C-wasm.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/glass-android/auto-scroll/raw-results.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/glass-android/host-gpu-single-process/raw-results.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/glass-android/host-gpu/raw-results.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/glass-android/pixel4xl-auto-scroll/raw-results.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/glass-android/raw-results.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/glass-optics/web/performance-full-scene/web-matrix-scroll.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/glass-optics/web/performance-slice/projection-matrix.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/glass-optics/web/performance-slice/web-matrix-scroll.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/glass-optics/web/performance/web-matrix-scroll.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/glass-rollout/android-optics-nav-benchmark.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/glass-rollout/control-current/web-matrix-scroll.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/glass-rollout/control/web-matrix-scroll.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/glass-rollout/web-matrix-scroll-final.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/glass-rollout/web-matrix-scroll.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/glass-web/js-mobile/raw-results.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/glass-web/raw-results.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/glass-web/webkit-repeat/raw-results.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/liquid-preview/interaction-control/interaction-results.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/liquid-preview/interaction-repeat/interaction-results.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/liquid-preview/interaction-results.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/liquid-preview/raw-results.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/liquid-preview/rejected-amoeba/raw-results.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/liquid-preview/rejected-intermediate/android-emulator-optimized/raw-results.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage5/android-imported-custom-w.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage5/ios-exact-v5-postfix-activities.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage5/ios-exact-v5-postfix-results.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage5/ios-exact-v5-postfix-scroll-results.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/android-corrupt-raw.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/android-export-vocabulary-v1.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/android-invalid-json.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/android-invalid-pair.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/android-merge-additive.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/android-picker-recovery-raw.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/android-private-after-cancel.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/android-private-after-invalid-json.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/android-private-after-invalid-pair.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/android-private-after-merge.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/android-private-after-oversize.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/android-private-after-recovery.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/android-private-after-restart.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/android-private-before-negative.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/desktop-picker-export.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/desktop-picker-recovery-raw.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/desktop-private-after-merge.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/desktop-private-after-repair.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/desktop-private-before-corrupt.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/ios-corrupt-raw.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/ios-files-export.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/ios-picker-invalid-pair.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/ios-picker-recovery-raw.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/ios-private-after-import-restart.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/ios-private-after-invalid-json.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/ios-private-after-invalid-pair.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/ios-private-after-oversize.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/stage6/ios-private-after-recovery.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/ux5/web/tables-hitch.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `Plans/Kotlin/artifacts/ux5/web/tabs.json` | H | Historical Kotlin host/performance evidence; no live consumer established |
| `courses/core/constructions.json` | A | Authored core course declarations; v2 validator/core engine |
| `courses/core/exercise-kinds.json` | A | Authored core course declarations; v2 validator/core engine |
| `courses/core/features.json` | A | Authored core course declarations; v2 validator/core engine |
| `courses/core/template-ops.json` | A | Authored core course declarations; v2 validator/core engine |
| `courses/en-ru/frequency-top1000.json` | A | Imported frequency course data; React/Kotlin vocabulary and validators |
| `courses/lang/en/curriculum.json` | A | Authored language content; shared Kotlin embed/reader and validator |
| `courses/lang/en/exercise-recipes.json` | A | Authored language content; shared Kotlin embed/reader and validator |
| `courses/lang/en/forms.generated.json` | A | Generated forms; build-pack-en/build-pack writer, shared Kotlin reader |
| `courses/lang/en/lang.json` | A | Authored language content; shared Kotlin embed/reader and validator |
| `courses/lang/en/lexicon.json` | A | Authored language content; shared Kotlin embed/reader and validator |
| `courses/lang/en/prepositions.json` | A | Authored language content; shared Kotlin embed/reader and validator |
| `courses/lang/en/realization.json` | A | Authored language content; shared Kotlin embed/reader and validator |
| `courses/lang/pl/curriculum.json` | A | Authored language content; shared Kotlin embed/reader and validator |
| `courses/lang/pl/exercise-recipes.json` | A | Authored language content; shared Kotlin embed/reader and validator |
| `courses/lang/pl/lang.json` | A | Authored language content; shared Kotlin embed/reader and validator |
| `courses/lang/pl/lexicon.json` | A | Derived v2 product of migrate-v1-to-v2; Kotlin pack and validators |
| `courses/lang/pl/realization.json` | A | Authored language content; shared Kotlin embed/reader and validator |
| `courses/pairs/en-ru/lifehacks.json` | A | Authored pair lifehacks; validator and Kotlin presentation |
| `courses/pairs/en-ru/pair.json` | A | Authored pair content; shared Kotlin embed/reader and validator |
| `courses/pairs/en-ru/vocabulary-editorial.json` | A | Editorial vocabulary; validator and course content |
| `courses/pairs/pl-ru/lifehacks.json` | A | Authored pair lifehacks; validator and Kotlin presentation |
| `courses/pairs/pl-ru/pair.json` | A | Derived v2 product of migrate-v1-to-v2; Kotlin pack and validators |
| `courses/pl-ru/course.json` | A | Live v1 authoring authority; React, Kotlin embed, validators |
| `courses/pl-ru/forms.generated.json` | A | Generated forms; build-pack-en/build-pack writer, shared Kotlin reader |
| `courses/pl-ru/frequency-top1000.json` | A | Imported frequency course data; React/Kotlin vocabulary and validators |
| `courses/pl-ru/inventory-coverage.json` | A | Generated coverage report; check-course-inventory writer |
| `courses/pl-ru/inventory-decisions.json` | A | Human-classified inventory decisions; check-course-inventory reader |
| `courses/pl-ru/source-inventory.json` | A | Generated source inventory; inventory-course writer, check-course-inventory reader |
| `courses/pl-ru/vocabulary-editorial.json` | A | Editorial vocabulary; validator and course content |
| `courses/schema/core-constructions-v1.schema.json` | A | App validator schema; scripts/validate-* or check-course-inventory |
| `courses/schema/core-exercise-kinds-v1.schema.json` | A | App validator schema; scripts/validate-* or check-course-inventory |
| `courses/schema/core-features-v1.schema.json` | A | App validator schema; scripts/validate-* or check-course-inventory |
| `courses/schema/core-template-ops-v1.schema.json` | A | App validator schema; scripts/validate-* or check-course-inventory |
| `courses/schema/course-pack-v1.schema.json` | A | App validator schema; scripts/validate-* or check-course-inventory |
| `courses/schema/curriculum-v1.schema.json` | A | App validator schema; scripts/validate-* or check-course-inventory |
| `courses/schema/frequency-top1000-v1.schema.json` | A | App validator schema; scripts/validate-* or check-course-inventory |
| `courses/schema/inventory-decisions-v1.schema.json` | A | App validator schema; scripts/validate-* or check-course-inventory |
| `courses/schema/lang-pack-v2.schema.json` | A | App validator schema; scripts/validate-* or check-course-inventory |
| `courses/schema/lexicon-v1.schema.json` | A | App validator schema; scripts/validate-* or check-course-inventory |
| `courses/schema/lifehacks-v1.schema.json` | A | App validator schema; scripts/validate-* or check-course-inventory |
| `courses/schema/pair-pack-v1.schema.json` | A | App validator schema; scripts/validate-* or check-course-inventory |
| `courses/schema/prepositions-v1.schema.json` | A | App validator schema; scripts/validate-* or check-course-inventory |
| `courses/schema/style-recipe-v1.schema.json` | A | App validator schema; scripts/validate-* or check-course-inventory |
| `courses/schema/vocabulary-editorial-v1.schema.json` | A | App validator schema; scripts/validate-* or check-course-inventory |
| `courses/styles/minimal-theory.json` | A | Editorial style recipe; validator and Kotlin style embed |
| `courses/styles/native-contrast.json` | A | Editorial style recipe; validator and Kotlin style embed |
| `courses/styles/rule-first.json` | A | Editorial style recipe; validator and Kotlin style embed |
| `courses/styles/situation-first.json` | A | Editorial style recipe; validator and Kotlin style embed |
| `kotlin/androidApp/src/test/resources/react-progress-v1.json` | L? | Old React progress test input; decoder-only candidacy, hash/packaging pending |
| `kotlin/composeApp/src/desktopTest/resources/react-progress-v1.json` | L? | Old React progress test input; decoder-only candidacy, hash/packaging pending |
| `kotlin/iosApp/PolskiGrammar/Assets.xcassets/AccentColor.colorset/Contents.json` | T | Xcode asset-catalog metadata |
| `kotlin/iosApp/PolskiGrammar/Assets.xcassets/AppIcon.appiconset/Contents.json` | T | Xcode asset-catalog metadata |
| `kotlin/iosApp/PolskiGrammar/Assets.xcassets/EmphasisAfter.colorset/Contents.json` | T | Xcode asset-catalog metadata |
| `kotlin/iosApp/PolskiGrammar/Assets.xcassets/EmphasisBefore.colorset/Contents.json` | T | Xcode asset-catalog metadata |
| `kotlin/shared/src/commonTest/kotlin/polski/srs/expanded_oracle.json` | A | Independent scheduler oracle; replace with text |
| `package-lock.json` | T | npm package manifest / lockfile |
| `package.json` | T | npm package manifest / lockfile |
| `tests/fixtures/core-golden/exercises.json` | A | Active app behavior/acceptance fixture; migrate independent expectations |
| `tests/fixtures/core-golden/grammar.json` | A | Active app behavior/acceptance fixture; migrate independent expectations |
| `tests/fixtures/kotlin-parity/evaluation.json` | A | Active app behavior/acceptance fixture; migrate independent expectations |
| `tests/fixtures/kotlin-parity/exercises.json` | A | Active app behavior/acceptance fixture; migrate independent expectations |
| `tests/fixtures/kotlin-parity/grammar.json` | A | Active app behavior/acceptance fixture; migrate independent expectations |
| `tests/fixtures/kotlin-parity/manifest.json` | A | Active app behavior/acceptance fixture; migrate independent expectations |
| `tests/fixtures/kotlin-parity/progress.json` | A | Active app behavior/acceptance fixture; migrate independent expectations |
| `tests/fixtures/kotlin-parity/scheduler.json` | A | Active app behavior/acceptance fixture; migrate independent expectations |
| `tests/fixtures/react-vocabulary-sample.json` | A | Active app behavior/acceptance fixture; migrate independent expectations |
| `tests/fixtures/vocabulary-baseline-32.json` | A | Active app behavior/acceptance fixture; migrate independent expectations |
| `tests/fixtures/vocabulary-four-cards.json` | A | Active app behavior/acceptance fixture; migrate independent expectations |
| `tsconfig.app.json` | T | TypeScript compiler project configuration |
| `tsconfig.browser.json` | T | TypeScript compiler project configuration |
| `tsconfig.json` | T | TypeScript compiler project configuration |
| `tsconfig.node.json` | T | TypeScript compiler project configuration |

Ledger check: **T 10 + A 60 + H 82 + L? 2 = 154**.

## Source call sites and consumers

These are source matches inspected at this SHA. A line identifies a seam; it does not establish that the seam ran on any host.

| Seam and source lines | Current consumer / implication |
| --- | --- |
| `src/data/course.ts:1`; `src/vocabulary/catalog.ts:2` | React imports the v1 course and frequency JSON at build time. `src/progress/storage.ts:2,4-7` uses the legacy localStorage key, JSON parse/stringify, and progress import/export; `src/ui/App.tsx:79` downloads `application/json`. |
| `src/vocabulary/storage.ts:5,42-52`; `src/ui/VocabularyView.tsx:73,84,103` | React vocabulary localStorage and JSON import, recovery download, and export. The current code writes new JSON on save. |
| `kotlin/shared/build.gradle.kts:50-132,138-188` | `generateCoursePackSource` and forms/recipe tasks read course/style/curriculum/lexicon/pair/lifehack/frequency/forms/recipe JSON and emit Kotlin `String` constants containing JSON. `GeneratedCourseJson.kt`, `GeneratedFormsFixtureJson.kt`, `GeneratedRecipesFixtureJson.kt` are app-owned build outputs even though their extension is `.kt`. |
| `kotlin/shared/src/commonMain/kotlin/polski/data/CourseData.kt:177-179,533`; `CoursePackLoader.kt:20-21`; `EnLexicon.kt:28`; `training/Curriculum.kt:23`; `presentation/StyleRecipe.kt:93`; `presentation/Lifehack.kt:111`; `core/RecipeLoader.kt:33,66`; `core/PlEngine.kt:44` | Shared Kotlin runtime parses embedded JSON into course, frequency, pair, English lexicon, curriculum, styles, lifehacks, recipes and forms. `core-engine` consumes resulting typed data; replacement must preserve those API consumers. |
| `kotlin/shared/src/commonMain/kotlin/polski/progress/ProgressCodec.kt:31,110`; `ProgressRepository.kt:58-61,66-82,101,112-172,227`; `kotlin/shared/src/commonMain/kotlin/polski/presentation/TrainingStore.kt:390,411`; `kotlin/shared/src/commonMain/kotlin/polski/presentation/PresentationPorts.kt:27` | Shared progress decode/encode, preview/legacy/backup/marker key migration, JSON-equivalence parse, JSON stamp field, and `DownloadJson` effect. The existing migration makes a JSON backup, incompatible with final zero-JSON cleanup. |
| `kotlin/shared/src/commonMain/kotlin/polski/vocabulary/VocabularyDocument.kt:217,239-258`; `preferences/UserPreferencesCodec.kt:37,51,95-119` | Shared vocabulary and preferences JSON decoders/encoders; platform repositories and export UI consume these strings. |
| `kotlin/composeApp/src/webMain/kotlin/polski/platform/WebProgressRepository.kt:21-22`; `WebPreferencesRepository.kt:12-14,21-79`; `ui/TrainingWebApp.kt:261-264`; `ui/VocabularyWeb.kt:32,143,183,400,520-530`; `ui/SettingsWeb.kt:98,166` | Browser localStorage, JSON backups, data-URI download, vocabulary recovery/export and import/save, and JSON preferences import/export. `VocabularyWeb` directly consumes `VocabularyCodec` and owns the vocabulary localStorage writes. |
| `kotlin/androidApp/src/main/java/dev/polski/grammarmatrix/AndroidProgressRepository.kt:36,71`; `AndroidVocabularyRepository.kt:24,39`; `AndroidSessionViewModel.kt:215-229`; `MainActivity.kt:175,187,217` | Android app-private JSON files/backups and document-picker export of `DownloadJson`. Android preferences storage/import paths require a separate host audit. |
| `kotlin/composeApp/src/desktopMain/kotlin/polski/desktop/DesktopProgressRepository.kt:39,73`; `DesktopVocabularyRepository.kt:16,29`; `DesktopPreferencesRepository.kt:23,73`; `Main.kt:230-239,304,337` | Compose Desktop JSON files/backups and chooser-based export. |
| `kotlin/shared/src/iosMain/kotlin/polski/ios/IosProgressRepository.kt:26-27,47-75`; `IosVocabularyRepository.kt:20-45`; `kotlin/shared/src/macosMain/kotlin/polski/macos/MacProgressRepository.kt:33,68`; `MacVocabularyRepository.kt:28,41`; `MacPreferencesRepository.kt:18,35` | Apple platform preferences and app-file stores currently retain or write JSON document and backup bytes. iOS/macOS preference repositories and bridge entrypoints need exact-key audit before NJ01 closure. |
| `kotlin/shared/src/iosMain/kotlin/polski/ios/IosSnapshot.kt:75,201,205`; `IosVocabularySession.kt:80,106,112-155`; `IosPreferencesSession.kt:63-100,129,157`; `IosSession.kt:49,64,133,162`; `kotlin/iosApp/PolskiGrammar/PolskiGrammarApp.swift:270,278,292,312,352,370,401,406,476-484` | iOS KMP emits JSON state/effect snapshots and exposes JSON import/export methods; Swift parses snapshots with `JSONSerialization` and uses JSON file import/export types. |
| `kotlin/shared/src/macosMain/kotlin/polski/macos/MacSnapshot.kt:74,190,194`; `MacVocabularySession.kt:79,107,123-179`; `MacPreferencesSession.kt:32-59,120`; `MacSession.kt:56,71-72,133,164`; `kotlin/macosApp/PolskiGrammarMac/PolskiGrammarMacApp.swift:321,425,445,615,1202,1215` | macOS KMP emits JSON state/effect snapshots and exposes JSON import/export methods; Swift decodes and opens JSON file dialogs. |
| `scripts/build-pack.mjs:18,189-210`; `build-pack-en.mjs:34,133-149`; `inventory-course.mjs:8,106-113`; `check-course-inventory.mjs:9-10,310-324`; `migrate-v1-to-v2.mjs:29,54-73` | Build/check scripts parse sources and write four tracked generated JSON files or derived v2 JSON copies. Their freshness gates must be replaced with typed/text checks, not hand-repaired outputs. |
| `scripts/validate-course.mjs:6,9-11,446-448`; `validate-pack-v2.mjs:32,151-227`; `validate-curriculum.mjs:6,8,60-61`; `validate-lifehacks.mjs:17-18,61-62`; `validate-vocabulary-editorial.mjs:7-9,109-112`; `extract-course-morphology.mjs:3-6,48-53` | Course/schema validators and morphology extraction consume JSON, including fixture controls. `JSON.stringify` in `validate-pack-v2.mjs:106` and `check-course-inventory.mjs:23,200,236` also acts as internal comparison/token encoding; audit against the no-JSON rule rather than granting a blanket exception. |

## Generated, ignored, bundle and live-store limits

The tracked-path ledger is **not** a release artifact scan. `.gitignore:1-17` excludes `node_modules`, `dist`, `.vite`, test reports, Kotlin Gradle state and every Kotlin `build/`; a read-only filesystem count excluding only `node_modules` and `.git` found 948 `*.json` names, but did not classify their origin or content. The Gradle-generated Kotlin constants above carry JSON without a `.json` filename. Rive's vendored `kotlin/composeApp/src/webMain/resources/rive/rive.js:458` contains a dependency-private package metadata `JSON.parse`; inspect bundled role and any first-party use instead of classifying it as application interchange from this match alone.

Supplement at canonical product `74acc6e` on 2026-10-18: `npm run build` **PASS** (course validation, TypeScript build and Vite 51 modules). `rg --files --no-ignore dist` found only `index.html`, one CSS, one JS and two images, so this particular React output has no `*.json` path. The 442.74 kB JS bundle `dist/assets/index-D2zslB3j.js` has SHA-256 `1c1b411028a24c9700c8f1c5278acb74a2bd316901e0223ee7740c3d1dbda83e`; a direct string check found the app's `Экспорт JSON` label, `polski-srs-progress` key and `JSON.parse`/`JSON.stringify` calls. The absence of a `.json` filename therefore does not satisfy the no-JSON rule. This is a built-file scan, not a browser execution or proof that every parse call belongs to the app rather than bundled dependencies.

The existing `kotlin/shared/build/generated/course/kotlin/polski/data/GeneratedCourseJson.kt` is 610,806 bytes (SHA-256 `82e4e30dc5cc39315479090e836e9ca84eaf5a654c850b80b8030d27bacb1c61`) and declares `generatedCurriculumJsonByLang` and other embedded course JSON strings. Two other generated Kotlin fixture files total 408,465 bytes and declare forms/recipe JSON strings. These were inspected as existing outputs, **not freshly rebuilt** in this supplement. An ignored-file scan with `rg --files --no-ignore kotlin -g '*.json'`, excluding `node_modules` and `.gradle`, found 57 paths: 7 source/test paths already in the tracked ledger and 50 under `kotlin/**/build/`. The 50 build paths require role-by-role classification before allowlisting; observed names include generated Gradle/npm `package.json` and Kotlin compilation metadata. Two ignored `test-results/kotlin-{js,wasm}/.last-run.json` files contain only runner keys `status` and `failedTests`, so they are tool-status candidates, not application document exceptions. Neither observation authorizes a broad build-directory exception.

The React `dist` scan is partial. No APK/framework/app bundle resource scan, compiler-output scan, browser `localStorage`/downloads inspection, Android/Apple app-private file inspection, old-browser-tab concurrency exercise, or live import/export run was performed. Thus no zero-JSON claim is supported. At release, inventory ignored and generated files by **path and content role**, inspect archives/bundles and actual writable stores, and distinguish tool metadata and third-party private data from application data. Temporary tool manifests require their own observed exact path and requiring tool; none is preapproved here.

## Ordered removal and checks

1. Freeze current independent course, exercise, FSRS and user-document expectations with source hashes; freeze each legacy-user JSON fixture at an exact path and byte hash before NJ06. The two `react-progress-v1.json` resources below are candidates only; vocabulary/preferences/invalid and crash-retry inputs are still unenumerated.
2. Replace course/core/style/pair authoring and validator schemas with typed Biome sources/contracts and independent text examples. Generate typed TS/Kotlin modules directly; verify source mutation, generated-output mutation, missing reference and freshness failures. Remove the four JSON generators and v1/v2 duplicate authority only after consumers pass.
3. Replace shared progress/vocabulary/preferences codecs and every platform repository, backup, import and export with the selected text wire. The one-time decoder may read immutable old JSON bytes; validated text readback and durable receipt precede deletion of old JSON and backups. Reject conflicting old/new state before any write.
4. Replace iOS/macOS JSON snapshots with typed bridges or a separately tested text protocol, then update Swift consumers and file types. Check each actual host; compilation alone does not establish execution.
5. Convert independent root/oracle fixtures to text while preserving expected behavior; remove historical JSON from the accepted tree with Git provenance retained. Run existing product gates and new no-JSON path/content scans on a clean checkout, generated tree, release bundles, live stores and exports. Record selected/executed/skipped counts and host scope.

The plan's 154/10/144 denominator is consistent with this tracked list. Its Stage 0 inventory, by its own wording, did **not** complete runtime, output or host scans; this document remains a source inventory. The proposed input-only exception is also still unfrozen and cannot yet reduce the 144 removal targets. This follows P7 (one authority), P11 (product no-JSON profile), P15–P17 (independent expectations, generated freshness, actual execution) and P18 (do not weaken gates).
