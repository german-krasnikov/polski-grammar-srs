# NJ05 B: pinned TOML library-printer feasibility

Status: **independently reviewed bounded NO-GO for the pinned B printer pair under the current equal-byte contract**. This is a feasibility experiment, not a completed codec or a change to the independently authored corpus. Product base: `f0e7afc769b7562788bb72e9aaac2b0f79a00a5c`; the executable, dependency-pinned probes and resources are committed only on isolated `experiment/nj05-toml-feasibility@6a89846385bb181cd57ff274641bed95127b0e80`. This archival report in the full-port branch does not add those failed-comparator dependencies to the application; reproduce its commands from that isolated revision. The accepted [freeze amendment](TextWireFreezeAmendment.md) requires the maintained libraries' own printers to emit *identical bytes*, with final LF, for the same ordered `[[record]]` tree; it forbids a product TOML printer or post-formatting. The seven inputs below are **feasibility projections** of P01/P02/P04/P05/P06/P07/P10. They are not B corpus byte oracles, valid digested documents, or a replacement for the full P01–P12 independent freeze. `tests/fixtures/textwire` is unchanged.

## Release and target evidence

| Dependency | Exact release and license | Checked artifact, not bundle delta |
| --- | --- | --- |
| [smol-toml 1.9.1](https://github.com/squirrelchat/smol-toml/releases/tag/v1.9.1) | Published 2026-10-09; [BSD-3-Clause](https://github.com/squirrelchat/smol-toml/blob/v1.9.1/LICENSE). `npm view smol-toml version` returned `1.9.1`; npm tarball SHA-256 `4ac8392f1918c22a4366be19047cf53aa42646077d4c711b2ef73263beefb172`. | Tarball 31,492 bytes, registry unpacked size 140,039 bytes, Node requirement `>=18`; application bundle/startup cost **NOT MEASURED**. |
| [ktoml-core 0.7.1](https://github.com/orchestr7/ktoml/releases/tag/v0.7.1) | Published 2025-08-22; [MIT](https://repo.maven.apache.org/maven2/com/akuleshov7/ktoml-core/0.7.1/ktoml-core-0.7.1.pom). Maven root metadata latest/release `0.7.1`; POM declares Kotlin stdlib 2.2.0, serialization-core 1.9.0, datetime `0.7.1-0.6.x-compat`. Project pins Kotlin 2.4.20. | [Gradle module metadata](https://repo.maven.apache.org/maven2/com/akuleshov7/ktoml-core/0.7.1/ktoml-core-0.7.1.module) and target modules returned HTTP 200. JVM jar 254,821 bytes, SHA-256 `c22cb661eefc75e30007a1b08d37c4e93a157f7f3a950a478251632c0af6050d`; application binary delta **NOT MEASURED**. |

Published KMP target variant artifacts in Maven Central were checked by HTTP 200 against the exact target `.module` URLs: `jvm` 254,821-byte jar; `js` 267,400-byte klib; `wasm-js` 267,438-byte klib; `iosarm64` 266,324-byte klib; `iossimulatorarm64` 266,390-byte klib; `macosarm64` 266,284-byte klib. The root metadata has no dedicated Android variant. Android could resolve a JVM fallback, but that was **NOT RUN**; publication and HTTP status are not host execution or compiler compatibility. There is also a concrete Native publication mismatch: each of the iOS arm64, iOS simulator arm64 and macOS arm64 target modules advertises an API klib with a mixed-case `*Main-0.7.1.klib` filename that returns HTTP **404**. The same SHA-256 bytes exist under the lowercase artifact filename (HTTP 200): iOS `96e49474e00b4011ea99456a8b3839c7c979d16d96d2ac77fd5c2e9716754d5c`, simulator `f4eeede6da50811fb5303f59fc4091e9a8159492296a5a2a2e753ed8f5ab1ebd`, macOS `791a376bac2c57d1c036794c4fd8ff0780242a5e23f895fe9eecbf9e0e095630`. This is a repository metadata/path finding, **not** an executed Native Gradle resolution failure. The repository's `:shared` desktop test compiled and resolved `ktoml-core:0.7.1` with Kotlin plugin 2.4.20 and Gradle 9.3.1. Android, JS browser, Wasm browser, iOS simulator and macOS Native compile/tests, Android device execution, and Swift bridge behavior are **NOT RUN**. This early printer failure makes their B codec execution inapplicable until the contract or library combination changes.

## Exact representative inputs

The experiment constructed the same top-level ordered model for every sample: `wire_version=1`, `kind="progress"`, `schema_version=2`, `record` is the following ordered row list, then `integrity.sha256` is exactly **64 ASCII zeroes**. Zero is only a placeholder to probe library printing, not an accepted digest. Every row lists fields in insertion order. `parent` is the parent ordinal, `index` the child index. `key_units` and `units` are decimal UTF-16 units, not scalar values. All strings below use JavaScript string-literal escape notation solely to specify the experiment's exact code units.

| Projection | Ordered `record` rows, with fields in source order |
| --- | --- |
| P01 | `(-1,0,M,count=0)` |
| P02 | `(-1,0,M,count=2)`; `(0,0,N,key="nothing")`; `(0,1,S,key="empty",text="")` |
| P04 | `(-1,0,M,count=1)`; `(0,0,L,key="z",count=1)`; `(1,0,S,text="value")` |
| P05 | `(-1,0,M,count=2)`; `(0,0,I,key="min",integer="-9223372036854775808")`; `(0,1,I,key="max",integer="9223372036854775807")` |
| P06 | `(-1,0,M,count=2)`; `(0,0,D,key="negativeZero",bits="8000000000000000")`; `(0,1,D,key="largestFinite",bits="7fefffffffffffff")` |
| P07 | `(-1,0,M,count=1)`; `(0,0,S,key="text",text="Zażółć / 😀 / Привет / 漢字\tline\nnext\\end\u0000\u001F\u007F\u0085\u2028\u2029")` |
| P10 | `(-1,0,M,count=2)`; `(0,0,S,key_units=[55296],text="lone key")`; `(0,1,S,key="lone value",units=[56320])` |

`smol-toml.stringify(model)` produced the exact TOML bytes copied in the appendix. `smol-toml.parse` then `stringify` reproduced all seven byte strings (7/7), with the expected 1/3/3/3/3/2/3 row counts. Kotlin `TomlParser(TomlInputConfig.compliant()).parseString(...)` parsed all seven TS byte strings; `TomlWriter(TomlOutputConfig.compliant(indentation = TomlIndentation.NONE)).writeToString(...)` produced the Kotlin byte strings. This parse/write pass does **not** establish full typed-tree, invalid-input, bounded-memory, digest or host compatibility.

## Actual byte comparison

Seven focused desktop tests selected and executed one sample each; all seven exact byte comparisons failed. With `TomlIndentation.FOUR_SPACES` (default), ktoml indented fields inside both `[[record]]` and `[integrity]`, so P01 already differed. The documented `TomlIndentation.NONE` removes that difference. It does **not** add the terminal LF that `smol-toml` prints: for P01/P02/P04/P05/P06/P10, Kotlin bytes equal TypeScript bytes **minus exactly the last `0a` byte**. P07 additionally prints raw TAB `09` where TS prints ASCII backslash+t `5c 74`, and prints literal ASCII `\u0085` bytes `5c 75 30 30 38 35` where TS prints UTF-8 U+0085 bytes `c2 85`; Kotlin again omits terminal `0a`. The exact transformation of P07 TypeScript output by these three edits was checked equal to the actual Kotlin output. Both emit literal U+2028/U+2029 (`e2 80 a8` / `e2 80 a9`). No candidate codec, digest wrapper or independently frozen B expectation was used to make these comparisons pass.

| Projection | TS bytes / SHA-256 | Kotlin bytes / SHA-256 | Exact |
| --- | --- | --- | --- |
| P01 | 197 / `32fd2250d827eba215fbfe03a1e9428dadf2da025a9c24c010935a497b06f157` | 196 / `454c71bba8d8a1af3d4f77f16e1b5a59307bc536113a9204d8ec4c14601fb8c1` | FAIL |
| P02 | 323 / `a4dea66fd1b0770578181c86823187169e60768311dd5d4e33a9d16e246c3b32` | 322 / `9e35bb1ba642f591afe710700b3eb64383fd03075c8622df79ae5577014155ba` | FAIL |
| P04 | 318 / `e89be396add1b58cb14a943a1d0555234634ca349118e4faff4994ad58cdf632` | 317 / `ab847bd251d3b8346eade4877fb9a7ecbefa0be5b7be07470a594cc389bab6dd` | FAIL |
| P05 | 372 / `3fdcf2263395a73b5eb0ad298a1c6d906a9ad89f11843ada0eeba6991d1cadfc` | 371 / `0f560b53f882355f5e49b78eba5fa3ebb8502bb2b88cb846fd0a3c067b381618` | FAIL |
| P06 | 378 / `39de6ba5f93e7c7d01d278c0e35fbddbbcfbb5399beab6489fcdc27a4b2d633d` | 377 / `8f6120024f0c5ac1e331879df5303115b1e2b622ee69db5fd8aa40812cfbafad` | FAIL |
| P07 | 347 / `680df9c93318f23870c9987b1cedd31511cd1e346cf9d7410fa761299549da89` | 349 / `0dffda901eadc8a6ecba256a4ac128338797ccd6e52074990444bc75cbd3ec34` | FAIL |
| P10 | 360 / `9a3573083b350e11ee9e54fe08f0ba4d37e47a10773b0256057c6f7bc5ef332d` | 359 / `349f193bff88db2867ff46f09fec0cfad14a961431ed299625094f17504c8de1` | FAIL |

The TypeScript `stringify` declaration accepts only `maxDepth`, `numbersAsFloat`, and `strictTemporal`; none controls LF or string escape spelling. `TomlOutputConfig` accepts `indentation`, `allowEscapedQuotesInLiteralStrings`, `ignoreNullValues`, `ignoreDefaultValues`, and `explicitTables`; none controls final LF, TAB or U+0085 escaping. These facts were checked in the exact published 1.9.1 tarball and 0.7.1 source jar (`TomlConfig.kt`, `TomlWriter.kt`, `TomlEmitter.kt`). A custom emitter or appending/replacing bytes after printing would be an application-owned post-formatter, forbidden by the accepted B contract. The existing B printer combination is therefore **NO-GO** under P8/P15/P17/P23. A different maintained pinned pair or a separately reviewed contract change would need fresh independent B bytes and equal-guarantee evidence; this report does not authorize one.

## Execution and limits

- Environment: macOS 26.6 arm64, Node `v24.1.0`, npm `11.3.0`, Java 23, Gradle wrapper `9.3.1`, project Kotlin plugin `2.4.20`. From repository root, `npm ci --ignore-scripts` then `node tests/feasibility/toml-printers.mjs` selected/executed **7/7** freshness and TS parse/reprint checks, **7 passed, 0 failed/skipped**. From `kotlin/`, `./gradlew :shared:desktopTest --tests 'polski.textwire.TomlPrinterFeasibilityTest' --rerun-tasks --console=plain` selected/executed **7/7**, **0 skipped, 7 failed** on exact TS/Kotlin byte comparison; Gradle ran 26 tasks in 10 seconds. The desktop XML reported `tests=7 skipped=0 failures=7 errors=0`, and its seven byte counts/hashes match the table above. These expected comparison failures are the observed experimental NO-GO, not a product gate PASS. The preliminary default-indentation P01 probe (before the committed scoped test) ran 1/1 and failed; `NONE` also failed on final LF.
- The executable probe is committed with this report: `tests/feasibility/toml-printers.mjs` holds the exact ordered row models; seven `kotlin/shared/src/desktopTest/resources/toml-feasibility/*.toml` files hold the TS library-printer snapshots; `TomlPrinterFeasibilityTest.kt` uses those exact resource bytes with the Kotlin library printer. `package.json`/lock pin `smol-toml` **1.9.1** as a test-only dev dependency; `kotlin/shared/build.gradle.kts` pins `ktoml-core` **0.7.1** in `desktopTest` only. These are tool/build metadata and text experiment resources, not application-owned JSON or application output. No production source, frozen `.cases`, codec, host or app bundle uses either candidate. The test intentionally records B printer incompatibility and should not be mistaken for a product gate.
- The library's parser accepted these seven positive strings; N27 bare `null`, N28 permissive-null mode, N29 TOML 1.1-only syntax, invalid UTF-8, early byte bounds, memory growth, SHA, terminal digest rewriting, complete 12-case corpus, KMP cross-target execution, app/host saves and P8/P23 cost measurements are **NOT RUN**. `ktoml` default input permits null; this experiment used `TomlInputConfig.compliant()` which disables it, but no negative test ran. No claim of zero-JSON migration, NJ05 completion or choice of A/C follows from this B NO-GO.

## Raw TypeScript printer output

Each following fenced block contains the exact `smol-toml` UTF-8 output bytes, including its final LF immediately before the closing fence. The table above gives exact hashes. For the Kotlin output, remove that final LF from every block; for P07 also replace the `\t` byte pair with a single TAB and replace the raw U+0085 byte sequence with the six ASCII bytes `\u0085`. The Kotlin hashes above verify those reconstructed outputs. This appendix is experimental library output, not an independently authored B oracle.

### P01

```toml
wire_version = 1
kind = "progress"
schema_version = 2

[[record]]
parent = -1
index = 0
tag = "M"
count = 0

[integrity]
sha256 = "0000000000000000000000000000000000000000000000000000000000000000"
```

### P02

```toml
wire_version = 1
kind = "progress"
schema_version = 2

[[record]]
parent = -1
index = 0
tag = "M"
count = 2

[[record]]
parent = 0
index = 0
tag = "N"
key = "nothing"

[[record]]
parent = 0
index = 1
tag = "S"
key = "empty"
text = ""

[integrity]
sha256 = "0000000000000000000000000000000000000000000000000000000000000000"
```

### P04

```toml
wire_version = 1
kind = "progress"
schema_version = 2

[[record]]
parent = -1
index = 0
tag = "M"
count = 1

[[record]]
parent = 0
index = 0
tag = "L"
key = "z"
count = 1

[[record]]
parent = 1
index = 0
tag = "S"
text = "value"

[integrity]
sha256 = "0000000000000000000000000000000000000000000000000000000000000000"
```

### P05

```toml
wire_version = 1
kind = "progress"
schema_version = 2

[[record]]
parent = -1
index = 0
tag = "M"
count = 2

[[record]]
parent = 0
index = 0
tag = "I"
key = "min"
integer = "-9223372036854775808"

[[record]]
parent = 0
index = 1
tag = "I"
key = "max"
integer = "9223372036854775807"

[integrity]
sha256 = "0000000000000000000000000000000000000000000000000000000000000000"
```

### P06

```toml
wire_version = 1
kind = "progress"
schema_version = 2

[[record]]
parent = -1
index = 0
tag = "M"
count = 2

[[record]]
parent = 0
index = 0
tag = "D"
key = "negativeZero"
bits = "8000000000000000"

[[record]]
parent = 0
index = 1
tag = "D"
key = "largestFinite"
bits = "7fefffffffffffff"

[integrity]
sha256 = "0000000000000000000000000000000000000000000000000000000000000000"
```

### P07

```toml
wire_version = 1
kind = "progress"
schema_version = 2

[[record]]
parent = -1
index = 0
tag = "M"
count = 1

[[record]]
parent = 0
index = 0
tag = "S"
key = "text"
text = "Zażółć / 😀 / Привет / 漢字\tline\nnext\\end\u0000\u001f\u007f  "

[integrity]
sha256 = "0000000000000000000000000000000000000000000000000000000000000000"
```

### P10

```toml
wire_version = 1
kind = "progress"
schema_version = 2

[[record]]
parent = -1
index = 0
tag = "M"
count = 2

[[record]]
parent = 0
index = 0
tag = "S"
key_units = [ 55296 ]
text = "lone key"

[[record]]
parent = 0
index = 1
tag = "S"
key = "lone value"
units = [ 56320 ]

[integrity]
sha256 = "0000000000000000000000000000000000000000000000000000000000000000"
```
