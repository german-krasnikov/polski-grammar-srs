# Final virtual and Stage 5 historical artifact provenance

This NJ01 index preserves the exact 15 historical application-owned JSON paths removed from the accepted tree. The original payloads remain in Git history at source commit `3085ccf363bf9e1a0d4dcf2c1ad2a1d35b1bb323`. Restore an entry with `git show 3085ccf363bf9e1a0d4dcf2c1ad2a1d35b1bb323:<original path>` and verify its byte count and SHA-256 below. These old bytes are historical evidence, not a current application input, output, format contract or new acceptance oracle. The linked reports retain their old host observations and limitations; this cleanup does not rerun them.

The final-virtual set has 11 paths; Stage 5 has 4. The main final-virtual manifest enumerated 122 source files and 13 build artifacts at the old test revision. `ipad-full-tests.json` is a zero-byte placeholder, so it never contained a usable test result. The iPad typed postfix attachment manifest is an empty array; neither file is counted as executed evidence. The old tester reports carry the readable scenario/result summaries, while this index preserves exact byte provenance.

No current application, test or benchmark source path reference to these 15 files was found in the tracked tree. The remaining historical directories still include active JSON writers and need separate treatment. This slice reduces tracked JSON from 126 to 111 and historical `H` paths from 54 to 39, removing 133,225 original bytes. NJ01 remains open.

Verification from the isolated worktree based on `3085ccf363bf9e1a0d4dcf2c1ad2a1d35b1bb323`:

- Git retrieval matched byte size, SHA-256 and blob for 15/15 original paths; all 15 are absent from the current tree. All 17 retargeted Markdown links resolve to the corresponding entry. Exact active source references to these paths: 0 observed. `git diff --check` and `git diff --cached --check` passed.
- Root `npm run build`: PASS, including the four existing course validators, `tsc -b` and Vite 51 modules. It still builds the JSON-dependent product.
- Root `npm test`: **FAIL twice** under the default parallel runner, 267/268 tests, one `tests/vocabulary-view.test.tsx:25` test timed out at its unchanged 5000 ms limit. The first run overlapped the build; the second did not. Focused `npx vitest run tests/vocabulary-view.test.tsx`: PASS 7/7. Full `npx vitest run --fileParallelism=false`: PASS 41/41 files, 268/268 tests with the same test assertions and timeout, 0 reported skips. The default command is not claimed green.
- Gradle, Playwright, native hosts, storage, no-JSON release scan and one-time migration: **NOT RUN** for this archive-only slice. No NJ01 or FP acceptance gate is claimed.

## Archived payloads

### FV-01

- Original path: `Plans/Kotlin/artifacts/final-virtual/android-legacy-after.json`
- Size: 840 bytes
- SHA-256: `fb6601d014a4c97082b95ba4144b8e90a4d4625e5d3f493696c4b5d9d54f3952`
- Git blob: `ad0703fbf33cd6431fc0d88382ef463b584260c9`

### FV-02

- Original path: `Plans/Kotlin/artifacts/final-virtual/android-legacy-homonym.json`
- Size: 840 bytes
- SHA-256: `fb6601d014a4c97082b95ba4144b8e90a4d4625e5d3f493696c4b5d9d54f3952`
- Git blob: `ad0703fbf33cd6431fc0d88382ef463b584260c9`

### FV-03

- Original path: `Plans/Kotlin/artifacts/final-virtual/ipad-files-export-v1.json`
- Size: 1363 bytes
- SHA-256: `6e9d312ce2a53a704fec4b2e79424effc2e6a570ce4e814ebd88d3c1dfae7687`
- Git blob: `6f280e2e6974130ba5d804c534647debadceec34`

### FV-04

- Original path: `Plans/Kotlin/artifacts/final-virtual/ipad-full-tests.json`
- Size: 0 bytes
- SHA-256: `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855`
- Git blob: `e69de29bb2d1d6434b8b29ae775ad8c2e48c5391`

### FV-05

- Original path: `Plans/Kotlin/artifacts/final-virtual/ipad-private-after-files-import.json`
- Size: 1363 bytes
- SHA-256: `6e9d312ce2a53a704fec4b2e79424effc2e6a570ce4e814ebd88d3c1dfae7687`
- Git blob: `6f280e2e6974130ba5d804c534647debadceec34`

### FV-06

- Original path: `Plans/Kotlin/artifacts/final-virtual/ipad-typed-postfix-attachments/manifest.json`
- Size: 4 bytes
- SHA-256: `ace810d7e2cbb4f8c40ce09dc8e191ae466adb4e1a7d49c59f2215b411d38b05`
- Git blob: `c44dc44f37c7b9b807ef282b291b36cda0f0bbeb`

### FV-07

- Original path: `Plans/Kotlin/artifacts/final-virtual/iphone-failures/manifest.json`
- Size: 6024 bytes
- SHA-256: `144df96b2fa8ba470873f80972f013bd39ffbab7775df394a5129a2ebed18304`
- Git blob: `b3e1896ef423a86e2ab1debea070401b32d4af46`

### FV-08

- Original path: `Plans/Kotlin/artifacts/final-virtual/iphone-focus-postfix-failure/manifest.json`
- Size: 1477 bytes
- SHA-256: `0f203c2ad6127093a9e6b0d3cc3b28b3e314c59dc6cf26bff1cbf1c9450d0bb6`
- Git blob: `3a776b75f0a46ab37dacf5adb628c171c4abca01`

### FV-09

- Original path: `Plans/Kotlin/artifacts/final-virtual/iphone-pronoun/manifest.json`
- Size: 25095 bytes
- SHA-256: `2de57ed85d9f4d97197d9589ccc8c1ac76ee1eb002621a0cc0ac249595098d68`
- Git blob: `b0dad5648b5106bd463194fe91c3c7080886693b`

### FV-10

- Original path: `Plans/Kotlin/artifacts/final-virtual/iphone-verb/manifest.json`
- Size: 18802 bytes
- SHA-256: `4749631c3787e0602ebbf18adb41a7878682a3a5ef47560969f89572e396efd0`
- Git blob: `d3e8eaf811c4dd5924e122c82d8393504c5bfed9`

### FV-11

- Original path: `Plans/Kotlin/artifacts/final-virtual/manifest.json`
- Size: 19284 bytes
- SHA-256: `7f2488ba7082c553a1f4c1f0e5dde22b5e1f0dda09868e2a7404ff11f07c1757`
- Git blob: `cfa26d87502a424e6ce174fb811082ac1f272dae`

### S5-01

- Original path: `Plans/Kotlin/artifacts/stage5/android-imported-custom-w.json`
- Size: 825 bytes
- SHA-256: `783e16a4121d868ca437d436816a12f57bf7a4c62909ea400a76e2207838b7ac`
- Git blob: `bed2d1e73343ba92ba8859a68e1a10385d2a6696`

### S5-02

- Original path: `Plans/Kotlin/artifacts/stage5/ios-exact-v5-postfix-activities.json`
- Size: 51338 bytes
- SHA-256: `1cdf6cb957c9968dee99ecb05eaaec223e7075156b7330db92978a45ca38c9c6`
- Git blob: `6e90cd0ab58bb06c1441a5d9c3cd542d46a8b70d`

### S5-03

- Original path: `Plans/Kotlin/artifacts/stage5/ios-exact-v5-postfix-results.json`
- Size: 2985 bytes
- SHA-256: `7e4178e3e942a1964f802b2737b9b0c2d1fa58b3c0d38785cfabaad9dce5dfa6`
- Git blob: `a0c8f2a89bd64d6e300e224c0b0c230baeaff5a1`

### S5-04

- Original path: `Plans/Kotlin/artifacts/stage5/ios-exact-v5-postfix-scroll-results.json`
- Size: 2985 bytes
- SHA-256: `317907ed177db35e34dbe8ebd9060b7abba6e2573d47693b2d5805e44aa9001a`
- Git blob: `afc97bcd5a9bc109c8d53947bf81e12f3e734a13`
