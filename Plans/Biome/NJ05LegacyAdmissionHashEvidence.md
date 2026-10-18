# NJ05 legacy input hash freeze — bounded execution receipt

Status at isolated product `experiment/nj05-hash-freeze@2c926b657c3d20bd49b3512dc6834148d0ede1f9`: **test-only exact-input prerequisite, not N26 or a text codec**. This slice completes the commonTest freeze for the 16 old-user inputs in `legacy-boundary.cases` and `numeric-admission.cases`; the macOS host part of step 1 in [NJ05N26NumericFreezeGate.md](NJ05N26NumericFreezeGate.md) remains open. It creates no application JSON file and does not write a new user document. The old decoders still receive in-memory legacy JSON **only as migration-input controls**.

The two human-readable `.cases` ledgers have whole-file SHA-256 seals in `tests/fixtures/textwire/legacy-admission.sha256`. Gradle generates a commonTest digest table from those ledgers; the test hashes each constructed `raw.encodeToByteArray()` and checks length **before** calling the old decoder. The generated table is test-only and is derived from those ledgers rather than separately authored Kotlin constants. The test-only pure Kotlin SHA-256 helper was checked against the empty and `abc` public vectors; a changed input byte and a changed expected digest fail. The seal checker mutates one recipe in each ledger and one seal digest independently (three deliberate mutations). The two large 10 MB progress controls have separate test methods so one browser test does not aggregate both under Karma's unchanged timeout.

## Current target execution

Commands ran from `kotlin/` in this isolated worktree, each with `--console=plain --max-workers=2` and both `--tests 'polski.textwire.LegacyBoundaryAdmissionTest' --tests 'polski.textwire.LegacyAdmissionDigestTest'`. Gradle exit was 0 for each row; the **current target XML** was inspected rather than inferring execution from compilation. The first class has eight test methods covering all 16 frozen input names; the digest class has two vector/mutation methods. Zero skips/failures/errors were recorded in the selected classes.

| Target | Gradle task | Target XML result |
| --- | --- | --- |
| Desktop JVM | `:shared:desktopTest` | 8 + 2 = **10/10**, skip 0, fail/error 0 |
| JS ChromeHeadless | `:shared:jsBrowserTest` | 8 + 2 = **10/10**, skip 0, fail/error 0 |
| Wasm ChromeHeadless | `:shared:wasmJsBrowserTest` | 8 + 2 = **10/10**, skip 0, fail/error 0 |
| macOS arm64 Native | `:shared:macosArm64Test` | 8 + 2 = **10/10**, skip 0, fail/error 0 |
| iOS Simulator Native | `:shared:iosSimulatorArm64Test` | 8 + 2 = **10/10**, skip 0, fail/error 0 |
| Android actual device | no selected commonTest task established | **NOT RUN** |

The iOS shared-module task exited 0 (`BUILD SUCCESSFUL in 32s`); its two selected class XML files show 8 and 2 executed cases with zero skips, failures or errors. This is not an iOS app import/readback test.

The `legacy-admission.sha256` entries pin the two source ledgers to `e3550c49fc7ab34ef4ae08d751a51c9705a47839d801f84a804cb4387914d5d9` and `546ed10dcd69c4e2b4fb95a4194c383304aa0c8151415d1274204e803e692e11`. At the same isolated revision, from the repository root, `python3 tests/fixtures/textwire/check_legacy_boundary.py` passed **10/10** recipes, `check_numeric_admission.py` passed **6/6**, `check_legacy_admission_seal.py` passed both ledger hashes and **3/3 deliberate mutations**, and `check_numeric_sizes.py` passed **4/4** independent A/C byte counts without an encoder. `shasum -a 256 -c tests/fixtures/textwire/corpus.sha256` passed all four previously frozen generic corpus files. These static checks are separate from the target XML. No old-host import/readback, Android/iOS app path, new wire, N26 exact/+1 limit, or P23 headroom is proved by the 10/10 target tests. The preserved legacy inputs are lower bounds on admitted old data, not a selected output ceiling.
