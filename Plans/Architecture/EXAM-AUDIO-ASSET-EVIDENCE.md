# Exam audio asset pipeline: implementation evidence

Date: 2026-11-08. Scope: [EXAM-COMPLETION-ADDENDUM.md](EXAM-COMPLETION-ADDENDUM.md) §1. This is a **local reproducibility gate**, not permission to publish the official MP3s. All six original files are present in the ignored `ExamBin` vault on this machine. The official URL endpoints returned HTTP 403 in the earlier documented check; a fresh checkout without this vault remains blocked.

## Contract and behavior

- `dsl/exam-assets/exam-audio.core-data` is the six-row source for exam ID, official URL, pinned SHA-256, browser path, and local vault path. `dsl/biome.project` generates `dsl/exam-assets/generated/ExamAudioRows.ts`; the generated file was not hand-edited. The six local source hashes were remeasured with `shasum -a 256` and match the addendum.
- `scripts/prepare-exam-audio.ts` stages all six files, verifies source and staged SHA-256, then atomically renames each into ignored `kotlin/composeApp/src/webMain/resources/exam-audio/`. Missing local sources trigger an official URL download attempt; HTTP failure or wrong bytes stops preparation. `--offline` rejects a missing source immediately. Failed staging removes its temporary file; existing verified resources are not overwritten by corrupt source bytes.
- `check-paths` confirms every listening task's `audioUrl` equals its manifest `webPath`; `prepare` runs that check before copying. Only the 15 stale task paths in B1/2020, B2/2017, B2/2020 were normalized; the other three exams already matched. `check-dist` hashes files in a named distribution. `check-url` performs real HTTP GETs and hashes served response bodies.
- Gradle web resource tasks depend on `prepareExamAudio`. Direct `jsBrowserDistribution`, `wasmJsBrowserDistribution`, and `composeCompatibilityBrowserDistribution` finalize with output-hash checks, including when a distribution task is up-to-date. Native desktop/Android/iOS tasks are not connected to this browser asset gate.

## Executed evidence

| Check | Command (working directory) | Result |
|---|---|---|
| Focused behavior RED | `npx vitest run tests/exam-audio-assets.test.ts` with exported preparation/verification stubs (repo root) | **RED:** three behavioral tests failed: missing source resolved, corrupt source resolved, and expected copied file absent. |
| Focused behavior GREEN | Same command after implementation (repo root) | **PASS:** five tests: missing, mismatched, verified copy/distribution, no partial publish when a later row is missing, and served-body mismatch/success. The small test bytes are an IO sentinel, not playable media. |
| Type check | `npx tsc --noEmit --module ESNext --moduleResolution Bundler --target ES2022 --types node --skipLibCheck --allowImportingTsExtensions scripts/prepare-exam-audio.ts tests/exam-audio-assets.test.ts` (repo root) | **PASS** |
| DSL check | `node /Users/german/Work/Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs generate --check --project dsl/biome.project` (repo root) | **PASS:** 6 asset rows/0 problems; toolchain pin stale warning, generated files still compared byte for byte. |
| Task paths | `npx tsx scripts/prepare-exam-audio.ts check-paths` (repo root) | **PASS:** all six. |
| Local preparation | `npx tsx scripts/prepare-exam-audio.ts prepare --offline` (repo root) | **PASS:** all six verified and copied. |
| Prebuild artifact negative control | `npx tsx scripts/prepare-exam-audio.ts check-dist kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable` before rebuild (repo root) | **FAIL as expected:** B1/2017 MP3 missing in old distribution. |
| Compatibility build and artifact | `./gradlew :composeApp:composeCompatibilityBrowserDistribution --console=plain` (`kotlin/`), then `npm run exam:audio:check-dist -- kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable` (repo root) | **PASS:** Gradle finalizer and explicit check verified all six MP3s. |
| Direct branches | `./gradlew :composeApp:wasmJsBrowserDistribution :composeApp:jsBrowserDistribution --console=plain` (`kotlin/`) | **PASS:** both finalizers verified all six. The Wasm distribution was up-to-date and its finalizer still ran. |
| Served paths | Temporary `python3 -m http.server 4191 --bind 127.0.0.1 --directory kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable`, then `npm run exam:audio:check-url -- http://127.0.0.1:4191/` (repo root) | **PASS:** six HTTP 200 responses with matching SHA-256; temporary server stopped. User's port 4190 was untouched. |
| Whitespace | `git diff --check` (repo root) | **PASS** |

Stable artifact for independent browser tests: `kotlin/composeApp/build/dist/wasmJs/productionExecutable/composeApp.js` SHA-256 `129b9ae331dfdf6b37a4f555d229c84ace6cfe14b74ad932b554f0dc4e4246cb`; compatibility `composeApp.js` SHA-256 `cb180f5c21a6756fde901f3bd13a2be73cb5402528213e08b2e23e7b078e2ea8`; `index.html` SHA-256 `debd8db376f0187124d8df5387df94879193cbbb8a94a057f90d25321053f9a5`.

**NOT RUN:** full official URL GET/hash verification, user port 4190 browser playback, human acoustic review, and publication. A clean CI checkout currently cannot pass the new web distribution gate unless the pinned vault bytes are supplied or the official URL becomes fetchable; this failure is intentional rather than a silent media omission.

## Independent gates

The tester independently ran `npx vitest run tests/exam-audio-assets.test.ts`: PASS 5/5; `npm run exam:audio:check-paths`: PASS six paths; `npm run exam:audio:check-dist -- <distribution>` on both Wasm and compatibility artifacts: PASS six hashes each. The reviewer read the bounded manifest/preparation/Gradle/path/docs slice and found no blockers. Copying is atomic per file, not a six-file filesystem transaction. No remote availability or publication approval is implied.

Preview transport correction: use Vite preview for media playback, not the earlier Python server. A real range request on the user's port 4190 returned `206 Partial Content`, `Accept-Ranges: bytes`, and exactly 100 requested MP3 bytes. Full byte/hash delivery checks above and seek/progress PlayTests are separate checks; see [EXAM-PLAYTEST-EVIDENCE.md](EXAM-PLAYTEST-EVIDENCE.md).

## CI delivery repair — 2026-11-08

Pages run `42519126770` failed at `prepareExamAudio`: official B1/2017 URL returned HTTP 403. After the owner explicitly approved publishing the six original MP3 copies, prepared release `exam-audio-v1` with `polski-exam-audio-v1.tar`. Archive SHA-256: `1ef043108d29dd238e92edc8aa61736f1c343f170f352674324a8d34f0c5e829`; only six canonical `ExamBin` audio paths are included. This supersedes the earlier absence of publication authorization for these six assets.

The shared local GitHub action `.github/actions/exam-audio` restores this pinned archive in both Pages and Kotlin web CI. Archive verification precedes extraction; existing `prepare --offline` then validates every MP3 against the DSL SHA-256 manifest. There is no successful fake audio fallback or skipped media gate. Local source preparation PASS (six hashes); clean archive restoration, remote release and final Pages results are recorded below after execution.

Delivery repair checks: focused Vitest audio + registry **9/9 PASS**; extraction into a fresh temporary directory followed by the existing offline preparer **PASS**, all six source and staged hashes verified. Independent read-only CI review **APPROVED**. Workflow filters now include the shared action path. Remote upload/deployment remains pending at this checkpoint.

Final remote delivery: Release asset `725092433` uploaded (326882304 bytes); GitHub's reported SHA-256 matches the pinned archive hash. Pages run [42520100781](https://github.com/german-krasnikov/polski-grammar-srs/actions/runs/42520100781), revision `7135717`, **SUCCESS**: npm tests, audio restore, JS/Wasm production build and Pages deployment. Published Chromium smoke **PASS**: live application rendered the `Экзамен` navigation control, no page errors; B1/2017 media range request returned HTTP **206**, `audio/mp3`, exactly 100 requested bytes. This smoke does not replace the earlier full 49-case local acceptance or establish physical iPad Safari playback. Temporary smoke initially waited for a nonexistent exact `Матрица` heading and timed out; corrected readiness check waited for rendered controls, without production changes.
