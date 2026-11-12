# P9 independent audio boundary review — 2026-11-12

Scope: 50 candidate Listening task windows in the ten intact, inactive P9 recordings. The eleventh, B2 June 2022, has no authored windows and a damaged received MP3. This review uses original ignored `ExamBin` audio and official key PDFs, current `listening-tasks.core-data` rows in p11, and fresh external artifacts in `/Users/german/Work/JS/polski-grammar-srs-worktrees/media-audit-20261112/`. It does **not** claim human listening or browser playback. Source hashes and candidate history are recorded in [P9 source provenance](P9-SOURCE-PROVENANCE.md), [the prior boundary audit](P9-AUDIO-BOUNDARY-AUDIT.md), and [the fresh decode receipt](P9-MEDIA-DECODE-EVIDENCE.md).

## Independent checks and method

I rehashed each of the ten original MP3s and compared it with both the clip manifest and the pinned source-provenance table; all ten match. I parsed the current DSL rows with `biome read --format json`: each paper has five ordered tasks, and its `segmentStartMs`/`segmentEndMs` values exactly equal the six manifest edges, with no gap or overlap. All 50 source transcripts were compared as complete word sequences with the corresponding official key PDF transcript spans, using the previously extracted, hash-checked PDF text from the independent P9 source audit. The only differences were key-side page headers, page numbers, and source URLs; there was no word deletion or substitution in the authored transcript. This is **text/source fidelity**, not proof that the audio says every transcript word.

For each of the 60 distinct candidate edges (six per recording), I inspected the retained centered 40-second faster-whisper `base`/Polish word-aligned transcript and the `ffmpeg silencedetect=-35dB:0.25` intervals. All 60 cut times lie inside a measured silence interval. The ASR context has the expected order at every edge: ten `Zaczynamy test` → task I announcements, forty prior-task closure → task II–V announcements, and ten task V closure → `Proszę teraz sprawdzić cały test`. The closure is before the cut and the next introduction is after it. The final check instruction is excluded from task V. I treated ASR timestamps as approximate: a few words align early into measured silence, and signal intervals, not those word timestamps, establish the no-speech interval. This independently checks boundary semantics rather than repeating only the silence predicate.

| Recording | DSL task windows, seconds I–V | Closest cut-to-signal margin | Automated source/boundary result |
| --- | --- | ---: | --- |
| B2/2024-02 | 109–500–1028–1591–1978–2309 | 0.083 s, V end | PASS; V end needs human acoustic attention. |
| C1/2024-04 | 110–468–978–1436–1952–2322 | 0.139 s, V end | PASS; V end needs human acoustic attention. |
| B2/2024-06 | 109–572–835–1376–1740–2093 | 0.365 s, I start | PASS. |
| B2/2023-02 | 109–496–892–1189–1635–1954 | 0.284 s, IV/V | PASS. |
| C1/2023-04 | 110–467–841–1372–1934–2365 | 0.238 s, V end | PASS. |
| B2/2023-06 | 109–728–1085–1429–1869–2249 | 0.338 s, I start | PASS. |
| C1/2023-11 | 110–478–809–1401–1760–2152 | 0.273 s, I start | PASS. |
| B2/2022-02 | 107–448–769–1241–1609–1923 | 0.615 s, V end | PASS. |
| C1/2022-03 | 110–547–902–1344–2018–2436 | 0.275 s, I start | PASS. |
| C1/2022-11 | 110–444–853–1257–1733–2174 | 0.168 s, IV/V | PASS with ASR wording uncertainty at IV/V; human check needed. |

The margin is the smaller of cut minus silence start and silence end minus cut. It is a signal observation, not a guarantee against quiet speech or compression tails. The three tightest edges are B2 February 2024 V end (83 ms into silence), C1 April 2024 V end (139 ms into silence), and C1 November 2022 IV/V (168 ms before detected next speech). At the latter join, ASR records `piąte` and the next task instruction but omits the word `zadanie`; the closure, silence, task number and instruction align with task V, but exact audio wording is unconfirmed. I found no observed cut through detected speech, missing numbered introduction, or adjacent task announcement included in the wrong window.

Every edge's smaller silence margin, in seconds, is retained here for individual review. Columns 1–6 are task I start, I/II, II/III, III/IV, IV/V and task V end respectively. ASR context at each numbered column follows the six expected transitions described above; the C1 November 2022 IV/V wording caveat is the sole incomplete announcement phrase.

| Recording | 1 | 2 | 3 | 4 | 5 | 6 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| B2/2024-02 | 0.338 | 1.087 | 0.787 | 1.018 | 0.532 | 0.083 |
| C1/2024-04 | 0.273 | 0.964 | 0.942 | 0.896 | 0.679 | 0.139 |
| B2/2024-06 | 0.365 | 1.038 | 0.722 | 0.603 | 1.061 | 0.754 |
| B2/2023-02 | 0.339 | 0.619 | 0.880 | 0.673 | 0.284 | 0.704 |
| C1/2023-04 | 0.299 | 0.989 | 0.952 | 0.612 | 0.308 | 0.238 |
| B2/2023-06 | 0.338 | 1.069 | 0.990 | 0.376 | 0.708 | 0.708 |
| C1/2023-11 | 0.273 | 1.146 | 0.601 | 0.534 | 0.939 | 0.755 |
| B2/2022-02 | 0.797 | 0.892 | 0.888 | 0.638 | 0.909 | 0.615 |
| C1/2022-03 | 0.275 | 0.886 | 0.608 | 0.533 | 0.483 | 0.539 |
| C1/2022-11 | 0.275 | 0.780 | 0.353 | 0.649 | 0.168 | 1.017 |

The printed source instructions specify one recording for C1 task I in each of the five C1 papers, twice for their II–V, and twice for all five B2 tasks. The current DSL instructions preserve this. The earlier full-recording locator audit reports repeated content inside the complete twice-recorded task windows; these 40-second boundary clips cannot independently establish every repeat or detect an omitted middle section. A player should play each complete source segment once without adding another software loop, pending real-media host acceptance.

## Blocked recording and remaining gates

B2/2022-06 has all five `segmentStartMs`/`segmentEndMs` fields absent. The pinned local MP3 hash matches provenance, but the fresh full-stream decoder exited 183 with `Invalid data found when processing input` (`media-audit-20261112/B2-2022-06.decode.log`). The nominal duration does not justify inventing task V or earlier cut points. **FAIL: received-file full decode; NOT RUN: candidate boundary review and playback.** This recording cannot be activated from these bytes.

For the ten intact papers: **PASS** source-file identity, authored window sequence, key-transcript text fidelity, and automated signal/ASR boundary context for all 60 edges; **NOT RUN** human acoustic listening, complete spoken-word-to-transcript verification, independent full-window replay count, new-paper real-media Playwright, native device playback, clean-checkout audio delivery, and catalog activation. The ten windows are defensible candidates for those host gates, not release-certified media. No audio, DSL, generated, production, test or index file was changed by this review.

Read-only reproduction artifacts: external `decode-summary.json`, `clip-manifest.json`, `boundary-observations.json`, per-edge `.wav`/`.json`/`.signal.log`, and `check-boundaries.py`; temporary `/tmp/p9-audio-review.txt`, `/tmp/p11-audio-window-check.txt`, `/tmp/p11-audio-transcript-diff.txt`. Tool versions recorded in observations: faster-whisper 1.2.1, `Systran/faster-whisper-base`, Polish, CPU int8, beam 5, VAD and word timestamps; signal threshold `-35dB` for `0.25s`. The PDF source remains authoritative over ASR spelling.
