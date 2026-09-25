# Stage 4 independent tester evidence — P01

Tested working tree: base `df59774e153a5bdf590fe27bd8780c7bd5457a26` plus uncommitted Stage 4 `kotlin/shared/src/commonMain/kotlin/polski/{model,data,grammar}` and `commonTest/kotlin/polski/grammar` files. These paths are untracked in the shared working tree, so the report identifies the file scope rather than a commit. Static commands below ran from `/Users/german/Work/JS/polski-grammar-srs` on macOS 26.6 aarch64 with Node 24.1.0 and Python 3.14.2; Gradle commands ran from its `kotlin` subdirectory with Gradle 8.14.2, Kotlin plugin 2.4.20, Java 23 launcher and configured JDK 21 toolchain.

| P01 check | Result | Evidence and limit |
| --- | --- | --- |
| Generated Kotlin dictionaries/assertions match portable fixture | PASS | `python3 kotlin/shared/src/commonTest/kotlin/polski/grammar/generate_parity.py --check` exits 0. This checks generated files against the JSON, not independently against React. |
| Current React source reproduces grammar fixture | PASS | In-memory `capture().grammar` through `./node_modules/.bin/tsx -e ...` is deeply equal to `grammar.json`, 1979 cases on each side; no files written. All 13 `manifest.json` source hashes, grammar file hash and ordered case IDs match. React baseline source is unchanged from the recorded Stage 1 revision. |
| Fixture scope | PASS | 1979 unique IDs: 196 noun, 490 adjective, 386 verb, 9 personal pronoun, 490 possessive, 196 phrase, 196 sentence, 16 skill. Nouns cover 14 IDs × 2 numbers × 7 cases; adjective/possessive cases cover all 5 genders × 2 numbers × 7 cases. Checked `kolega → kolegę`, `drogi → drodzy`, and `być → będę` examples against current React capture. |
| Perfective present | PASS | JSON and generated assertions explicitly require the `Perfective verbs have no present tense` error for `buyDone` and `doDone`; Kotlin `verbForm` rejects `PRESENT` for `Aspect.PERFECTIVE`. The assertions ran in both JS and Wasm browser targets. |
| NFC and Polish diacritics | PASS | `AnswerNormalizerTest` covers decomposed `żółć`, distinct accentless input, whitespace and terminal punctuation; both JS and Wasm browser targets passed both methods. |
| Reference rows | PASS (static) | Seven ordered `CaseRow` values and five gender names in `GrammarReference.kt` match the current React `src/ui/GrammarTables.tsx` constants by inspection. The current Kotlin test samples row order, one trigger and one gender name. |
| JS browser common tests | PASS | `./gradlew :shared:jsBrowserTest :shared:wasmJsBrowserTest --rerun-tasks` ran 26 grammar methods in headless Chrome: 20 corpus parts spanning 1979 assertions, four focused engine tests, two normalizer tests; 0 failures/errors/skips. XML: `kotlin/shared/build/test-results/jsBrowserTest/TEST-polski.grammar.*.xml`. |
| Wasm JS browser common tests | PASS | Same command ran the same 26 grammar methods in the Wasm target in headless Chrome; 0 failures/errors/skips. XML: `kotlin/shared/build/test-results/wasmJsBrowserTest/TEST-polski.grammar.*.xml`. |
| Browser UI and native devices | NOT RUN | Stage 4 is domain grammar; common target tests cannot establish browser interaction or native device behavior. |

Commands used for the independent source and target checks:

```sh
# cwd: repository root
python3 kotlin/shared/src/commonTest/kotlin/polski/grammar/generate_parity.py --check
./node_modules/.bin/tsx -e 'import {capture} from "./tests/fixtures/kotlin-parity/capture.ts"; import {readFileSync} from "node:fs"; import {isDeepStrictEqual} from "node:util"; const current=capture().grammar; const expected=JSON.parse(readFileSync("tests/fixtures/kotlin-parity/grammar.json","utf8")); console.log(JSON.stringify({equal:isDeepStrictEqual(current,expected),currentCases:current.cases.length,fixtureCases:expected.cases.length})); if(!isDeepStrictEqual(current,expected)) process.exit(1);'

# cwd: kotlin
./gradlew :shared:tasks --all
./gradlew :shared:jsBrowserTest :shared:wasmJsBrowserTest --rerun-tasks
```

The generated Kotlin data and generated Kotlin assertions use the same `grammar.json`. Source replay and manifest hashes above provide an independent check of that JSON against the current React behavior. `src/ui/GrammarTables.tsx` is outside `manifest.sourceSha256`; reference row parity therefore relies on this direct source comparison and can drift without the fixture hash changing. Verb past stems are handwritten in `generate_parity.py`, while generated present/future metadata comes from the fixture. The target corpus exercises past forms, so a stem typo would fail the browser test even though generator `--check` alone would not detect it.

No test files or production files were changed by the tester. The independent tester diff is this report only. Existing Stage 1 React browser evidence applies to the unchanged React baseline; it does not prove Kotlin browser UI or devices. The browser target checks above prove domain execution in headless Chrome, not app interaction, Safari, physical mobile browsers or native apps.
