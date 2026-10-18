# NJ05 E3 independent tester receipt

Status: **PASS for bounded test-only A/C retained-number probe on Desktop JVM, macOS Arm64 Native and iOS Simulator Arm64 Native**. This tests candidate `experiment/nj05-e3-retained-number-probe@0e7cb8c50c86dbc6b9c2b952ac2523e8e5afc254` in a separate detached worktree, not a production codec, chosen wire arm, completed migration or zero-JSON application. The independent R01/R02 oracle and exact-path seals predate the candidate and were not edited. The tester added only `kotlin/shared/src/commonTest/kotlin/polski/textwire/RetainedNumberIndependentMutationTest.kt` (SHA-256 `fd06d7b452d4ad517b6b57de40dc4b5296220bf2897748e850c065157a3da33b`) and this receipt.

From `/Users/german/Work/JS/polski-grammar-srs-nj05-e3-independent-tester` on 2026-10-18:

| Check | Observed result |
| --- | --- |
| `python3 tests/fixtures/textwire/check_retained_number.py` | PASS 3/3 exact-path seals, 5/5 ordered non-string old numeric nodes, R01 A 316/C 618 literal bytes and full digests, 14/14 recomputed negative mutation recipes plus bad-digest control per arm. |
| `python3 tests/fixtures/textwire/check_representability.py` | PASS E2 2/2 seals and 18/18 typed-tree/UTF-16 expectations. |
| `shasum -a 256 -c tests/fixtures/textwire/retained-number.sha256` and `shasum -a 256 -c tests/fixtures/textwire/legacy-representability.sha256` | PASS 3/3 and 2/2 exact-path entries before the target runs; first seal PASS 3/3 again afterward. The 683-byte input remained SHA-256 `a01e0693ced1d3ef3021b3bb86aa68204f3a0405fd536b986ffc337f5954219e`. |

From the same worktree's `kotlin/` directory, each target ran sequentially with only the exact named classes `polski.textwire.RetainedNumberPrototypeTest` and `polski.textwire.RetainedNumberIndependentMutationTest` selected:

| Target and command prefix | Gradle result | XML selected / executed / skipped / failed / errors |
| --- | --- | --- |
| `./gradlew :shared:desktopTest --tests polski.textwire.RetainedNumberPrototypeTest --tests polski.textwire.RetainedNumberIndependentMutationTest --console=plain --max-workers=2 --no-daemon` | PASS, 27 actionable tasks executed, 30 s | 5 / 5 / 0 / 0 / 0 in exactly two named class XML files. |
| Same flags with `:shared:macosArm64Test` | PASS, 27 executed / 13 up-to-date, 44 s | 5 / 5 / 0 / 0 / 0 in exactly two named class XML files. |
| Same flags with `:shared:iosSimulatorArm64Test` | PASS, 17 executed / 23 up-to-date, 44 s | 5 / 5 / 0 / 0 / 0 in exactly two named class XML files. |

The independent test starts with a tiny typed tree in memory and runs four controls per arm. It changes `1e0` to invalid `+1` **with a recomputed digest** and observes `RetainedNumberSyntax`; changes it to valid `2e0` **without updating the digest** and observes `DigestMismatch`; changes a map-child key to `-` with a recomputed digest and observes `MapKey`; and gives a list item a named key with a recomputed digest and observes `ListItemKey`. This tests rejection boundaries independently of the candidate's pre-existing negative test method. R01's exact bytes/hash and R02's ordered projection are covered by the separately frozen oracle in the other four executed methods. The tiny mutation baseline is constructed by the test-only encoder; it is not an independent exact-byte oracle, and no such claim is made.

**Limits and handoff.** N01–N13 and N15 generic rejection and R01/R02 typed distinctions passed on the three named targets. N14 `KnownTypedFieldR` has no complete accepted progress document/schema-admission test and is **NOT RUN**. N16 remains **PENDING** a reviewed N26 numeric bound; the recursive test-only parser is unbounded. N17's sealed duplicate-key old input is rejected by the read-only Python preflight, but Kotlin old-reader preflight and required-host recovery are **NOT RUN**. JS/Wasm and Android device are **NOT RUN**; the available JS `--tests` path previously selected prohibited old writer/import tests, and no isolated read-only path was demonstrated. The production commonMain codec, document admission, exact R02 A/C bytes, storage transaction, physical quotas, P23 N=10/100/1000 performance, full UTF-16 ordering matrix and zero-JSON application acceptance are **NOT RUN**. Existing `generateCoursePackSource` and `generateFormsFixtureSource` tasks ran in the build graph, so this result does not remove the transitional JSON-fed product graph. No old writer/import/save/export/backup was called by the selected tests or static checker. This is tester evidence for independent read-only review, not reviewer approval.
