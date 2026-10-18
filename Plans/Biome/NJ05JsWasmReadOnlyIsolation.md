# NJ05 JS/Wasm read-only E2/E3 test isolation

Status: **isolated test infrastructure candidate; Gradle and browser execution NOT RUN**. Based on canonical `biome/full-port-2026@f3892033e3e25d8895eaf92624d60bdc714c7c37`. It does not select A/C, authorize a codec, make a JSON-producing application path acceptable, or close NJ05/NJ06.

The existing `:shared:jsBrowserTest --tests ...` command previously selected 64 classes/393 tests, including old JSON writer/import paths, so a class filter is not an admissible E2/E3 read-only runner. The new `:nj05ReadOnlyProbe` Kotlin Multiplatform **test module** depends on `:shared` main as a consumer but does not depend on or alter `:shared` commonTest. Its `read-only-sources.list` names exactly three pre-existing E2/E3 test classes (one + four + one `@Test` methods) and two test-only helpers. A `Sync` task copies only those five named files into this module's test output, alongside the two test-only fixture sources generated from existing sealed E2/admission inputs by `:shared:generateRepresentabilityTestSource` and `:shared:generateLegacyAdmissionTestSource`. No sealed `.cases`, SHA ledger, old input, production source or production source set changed.

The Gradle `prepareNj05ReadOnlyTestSources` task checks the exact class/method count and rejects old writer/import/save calls before copying; `verifyNj05ReadOnlySelection` checks the seven-file output set. Both browser tasks depend on that verification. The independent static `python3 kotlin/nj05ReadOnlyProbe/check_selection.py` at the product root passed 5/5 source paths, 3/3 test classes, 6/6 methods, two helper files and two declared generated fixture edges. Its in-memory negative controls rejected an extra old test class and an `encodeLegacyV1` call. `git diff --check` passed. These are source/configuration checks; a compiled task graph, test XML, browser execution and actual exclusion from a linked JS/Wasm test bundle are **NOT RUN**.

After the compiler gate releases Gradle, run from `kotlin/`, sequentially:

```text
./gradlew :nj05ReadOnlyProbe:jsBrowserTest --console=plain --max-workers=2 --no-daemon
./gradlew :nj05ReadOnlyProbe:wasmJsBrowserTest --console=plain --max-workers=2 --no-daemon
```

Before accepting either run, inspect its task graph and XML. Require exactly the three named classes, six executed tests, zero skipped/failed/errors and no `LegacyBoundaryAdmissionTest`, `VocabularySessionTest` or other old writer/import/save class. Record product and compiler SHA, browser, task command, selected/executed count and sealed E2 input SHA before/after. If Gradle configuration fails, the linked bundle includes any other test, the runner executes zero tests, or old bytes differ, report **FAIL** and do not infer JS/Wasm E2/E3 success. JS/Wasm host storage, Android device, N14 domain admission, N16/N26 bounds, N17 Kotlin preflight, E4 resource measurements, production codec and migration remain **NOT RUN**.
