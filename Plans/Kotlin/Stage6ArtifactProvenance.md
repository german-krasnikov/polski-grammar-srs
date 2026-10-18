# Stage 6 historical JSON provenance

This is a provenance index for the 28 Stage 6 user-document captures removed from the accepted tree during NJ01 cleanup. The historical [Stage 6 tester report](Stage-VocabularyModes-Tester.md) retains its original observations and links to the exact entries below. The removed bytes remain in Git history at immutable source commit `c43e54100c70609ec7a3650ea6870c3ea0038f94`; no current test or application source reads these paths. This index does not convert an old JSON document into a new-format oracle or prove the current hosts.

For any entry, recover its original bytes locally with `git show c43e54100c70609ec7a3650ea6870c3ea0038f94:<original path>` and compare SHA-256 with the recorded digest. The SHA values are over the original file bytes, with no parsing, normalization, or rewrite. The accompanying non-JSON logs/XML and the tester report retain the scenario, host, observed result and limits. The old documents are historical evidence only, not new application inputs or outputs.

This bounded cleanup changes the tracked JSON count from 154 to 126 and the historical `H` count from 82 to 54; it removes 32,666 bytes of tracked Stage 6 JSON. The other 54 historical files need their own dependency review. In particular, `tests/perf/flip-rive-measurements.spec.ts` and archived glass benchmark scripts still write JSON in other historical artifact directories, so this commit does not claim that all `H` files can be removed unchanged. No active source path reference to these 28 Stage 6 files was found; that source scan is not a runtime or host proof.

Verification in the isolated worktree based on `c43e54100c70609ec7a3650ea6870c3ea0038f94`:

- Git recovery against the source commit: 28/28 original payloads matched recorded byte size, SHA-256 and Git blob; 28/28 paths absent from the current tree.
- Historical report links: 26/26 retargeted links resolved to exact entries; active application/test source references to these 28 paths: 0 observed; `git diff --check` and `git diff --cached --check` passed.
- Root `npm test`: 41/41 files, 268/268 tests passed, 0 failures or skips reported. Root `npm run build`: four existing course validation commands, `tsc -b` and Vite's 51-module build passed. These still execute the JSON-dependent baseline.
- Gradle, Playwright, native hosts, no-JSON output scans, storage and migration: **NOT RUN** for this documentation/archive-only slice. No NJ01 or FP acceptance gate is claimed.

## Archived payloads

### S6-01

Original path: `Plans/Kotlin/artifacts/stage6/android-corrupt-raw.json`
Size: 24 bytes
SHA-256: `5cbb5b64af2def0b9dcb62cf44a09ac917c2f7b95c8ec95517bb6856b9b930da`
Git blob: `94a403da988b9e027ef8771946ea43365aece3fd`

### S6-02

Original path: `Plans/Kotlin/artifacts/stage6/android-export-vocabulary-v1.json`
Size: 1363 bytes
SHA-256: `6e9d312ce2a53a704fec4b2e79424effc2e6a570ce4e814ebd88d3c1dfae7687`
Git blob: `6f280e2e6974130ba5d804c534647debadceec34`

### S6-03

Original path: `Plans/Kotlin/artifacts/stage6/android-invalid-json.json`
Size: 7 bytes
SHA-256: `be302461cda285db3ed2d45fb767b23e08ca8808b35a638009937df22b332d73`
Git blob: `d7a98f7cc5635a6039ac63eb9fc2aae38642128a`

### S6-04

Original path: `Plans/Kotlin/artifacts/stage6/android-invalid-pair.json`
Size: 1363 bytes
SHA-256: `f8686e4bb336a4cb7f14633804d29c6e322ad280448c82964756fb03522e9bd2`
Git blob: `8e058fafd0e74d5a7c729c9fdf6601aee9302a90`

### S6-05

Original path: `Plans/Kotlin/artifacts/stage6/android-merge-additive.json`
Size: 850 bytes
SHA-256: `1d381d08d24c084ff51c97bb4a44739ebccd9ce539296d097b2895d6379c3fcb`
Git blob: `7bb59b8ae3cae3061091f591b38da3931b538f69`

### S6-06

Original path: `Plans/Kotlin/artifacts/stage6/android-picker-recovery-raw.json`
Size: 24 bytes
SHA-256: `5cbb5b64af2def0b9dcb62cf44a09ac917c2f7b95c8ec95517bb6856b9b930da`
Git blob: `94a403da988b9e027ef8771946ea43365aece3fd`

### S6-07

Original path: `Plans/Kotlin/artifacts/stage6/android-private-after-cancel.json`
Size: 1363 bytes
SHA-256: `6e9d312ce2a53a704fec4b2e79424effc2e6a570ce4e814ebd88d3c1dfae7687`
Git blob: `6f280e2e6974130ba5d804c534647debadceec34`

### S6-08

Original path: `Plans/Kotlin/artifacts/stage6/android-private-after-invalid-json.json`
Size: 1363 bytes
SHA-256: `6e9d312ce2a53a704fec4b2e79424effc2e6a570ce4e814ebd88d3c1dfae7687`
Git blob: `6f280e2e6974130ba5d804c534647debadceec34`

### S6-09

Original path: `Plans/Kotlin/artifacts/stage6/android-private-after-invalid-pair.json`
Size: 1363 bytes
SHA-256: `6e9d312ce2a53a704fec4b2e79424effc2e6a570ce4e814ebd88d3c1dfae7687`
Git blob: `6f280e2e6974130ba5d804c534647debadceec34`

### S6-10

Original path: `Plans/Kotlin/artifacts/stage6/android-private-after-merge.json`
Size: 1363 bytes
SHA-256: `6e9d312ce2a53a704fec4b2e79424effc2e6a570ce4e814ebd88d3c1dfae7687`
Git blob: `6f280e2e6974130ba5d804c534647debadceec34`

### S6-11

Original path: `Plans/Kotlin/artifacts/stage6/android-private-after-oversize.json`
Size: 1363 bytes
SHA-256: `6e9d312ce2a53a704fec4b2e79424effc2e6a570ce4e814ebd88d3c1dfae7687`
Git blob: `6f280e2e6974130ba5d804c534647debadceec34`

### S6-12

Original path: `Plans/Kotlin/artifacts/stage6/android-private-after-recovery.json`
Size: 1363 bytes
SHA-256: `6e9d312ce2a53a704fec4b2e79424effc2e6a570ce4e814ebd88d3c1dfae7687`
Git blob: `6f280e2e6974130ba5d804c534647debadceec34`

### S6-13

Original path: `Plans/Kotlin/artifacts/stage6/android-private-after-restart.json`
Size: 1909 bytes
SHA-256: `6695adc6a66fcbb6104aeabfabc4156c42e0f6b31f7651470b7acac4c00d3aab`
Git blob: `cf9774ce6eb3ae8670403af18f9c81ccf8f6c0cf`

### S6-14

Original path: `Plans/Kotlin/artifacts/stage6/android-private-before-negative.json`
Size: 1363 bytes
SHA-256: `6e9d312ce2a53a704fec4b2e79424effc2e6a570ce4e814ebd88d3c1dfae7687`
Git blob: `6f280e2e6974130ba5d804c534647debadceec34`

### S6-15

Original path: `Plans/Kotlin/artifacts/stage6/desktop-picker-export.json`
Size: 1363 bytes
SHA-256: `6e9d312ce2a53a704fec4b2e79424effc2e6a570ce4e814ebd88d3c1dfae7687`
Git blob: `6f280e2e6974130ba5d804c534647debadceec34`

### S6-16

Original path: `Plans/Kotlin/artifacts/stage6/desktop-picker-recovery-raw.json`
Size: 23 bytes
SHA-256: `4fd182cf68502e32b4dacfa98f7d056b3937cbdaad1f033322bbed398ee79506`
Git blob: `41d19b66d2e9b1ed790c77c6b65373d584fe9cb2`

### S6-17

Original path: `Plans/Kotlin/artifacts/stage6/desktop-private-after-merge.json`
Size: 1909 bytes
SHA-256: `6695adc6a66fcbb6104aeabfabc4156c42e0f6b31f7651470b7acac4c00d3aab`
Git blob: `cf9774ce6eb3ae8670403af18f9c81ccf8f6c0cf`

### S6-18

Original path: `Plans/Kotlin/artifacts/stage6/desktop-private-after-repair.json`
Size: 1363 bytes
SHA-256: `6e9d312ce2a53a704fec4b2e79424effc2e6a570ce4e814ebd88d3c1dfae7687`
Git blob: `6f280e2e6974130ba5d804c534647debadceec34`

### S6-19

Original path: `Plans/Kotlin/artifacts/stage6/desktop-private-before-corrupt.json`
Size: 1909 bytes
SHA-256: `6695adc6a66fcbb6104aeabfabc4156c42e0f6b31f7651470b7acac4c00d3aab`
Git blob: `cf9774ce6eb3ae8670403af18f9c81ccf8f6c0cf`

### S6-20

Original path: `Plans/Kotlin/artifacts/stage6/ios-corrupt-raw.json`
Size: 20 bytes
SHA-256: `2fe277f34ab5f0a670a22b1eecc240f9787a41f484cc6edb1dfb8a5ec25d1086`
Git blob: `6e4eb721371c3b833cee10b947c0187c078f524d`

### S6-21

Original path: `Plans/Kotlin/artifacts/stage6/ios-files-export.json`
Size: 1363 bytes
SHA-256: `6e9d312ce2a53a704fec4b2e79424effc2e6a570ce4e814ebd88d3c1dfae7687`
Git blob: `6f280e2e6974130ba5d804c534647debadceec34`

### S6-22

Original path: `Plans/Kotlin/artifacts/stage6/ios-picker-invalid-pair.json`
Size: 1363 bytes
SHA-256: `f8686e4bb336a4cb7f14633804d29c6e322ad280448c82964756fb03522e9bd2`
Git blob: `8e058fafd0e74d5a7c729c9fdf6601aee9302a90`

### S6-23

Original path: `Plans/Kotlin/artifacts/stage6/ios-picker-recovery-raw.json`
Size: 20 bytes
SHA-256: `2fe277f34ab5f0a670a22b1eecc240f9787a41f484cc6edb1dfb8a5ec25d1086`
Git blob: `6e4eb721371c3b833cee10b947c0187c078f524d`

### S6-24

Original path: `Plans/Kotlin/artifacts/stage6/ios-private-after-import-restart.json`
Size: 1363 bytes
SHA-256: `6e9d312ce2a53a704fec4b2e79424effc2e6a570ce4e814ebd88d3c1dfae7687`
Git blob: `6f280e2e6974130ba5d804c534647debadceec34`

### S6-25

Original path: `Plans/Kotlin/artifacts/stage6/ios-private-after-invalid-json.json`
Size: 1842 bytes
SHA-256: `006bc915cd67fd6a4a8cf6dfa62d773f5bfd2338e564a7658153eab282f86ac9`
Git blob: `4b7a72c2a6f2d046d0bae8286b82ab5810eee60d`

### S6-26

Original path: `Plans/Kotlin/artifacts/stage6/ios-private-after-invalid-pair.json`
Size: 1842 bytes
SHA-256: `006bc915cd67fd6a4a8cf6dfa62d773f5bfd2338e564a7658153eab282f86ac9`
Git blob: `4b7a72c2a6f2d046d0bae8286b82ab5810eee60d`

### S6-27

Original path: `Plans/Kotlin/artifacts/stage6/ios-private-after-oversize.json`
Size: 1842 bytes
SHA-256: `006bc915cd67fd6a4a8cf6dfa62d773f5bfd2338e564a7658153eab282f86ac9`
Git blob: `4b7a72c2a6f2d046d0bae8286b82ab5810eee60d`

### S6-28

Original path: `Plans/Kotlin/artifacts/stage6/ios-private-after-recovery.json`
Size: 1363 bytes
SHA-256: `6e9d312ce2a53a704fec4b2e79424effc2e6a570ce4e814ebd88d3c1dfae7687`
Git blob: `6f280e2e6974130ba5d804c534647debadceec34`
