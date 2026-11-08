# B2/2020 source-data checkpoint

Current integration update, 2026-11-08: the source checkpoint and pending/hidden statements below are historical. The schema, six-exam catalog, delivered media and browser behavior have since passed the bounded [six-exam acceptance](SIX-EXAM-BROWSER-ACCEPTANCE.md). That receipt records exact artifact hashes, the earlier intermittent seekability failure and remaining limits; it does not mark P10/P11 or the entire exam plan complete.
Date: 2026-11-08. Source is the locally supplied official sample paper
`ExamBin/pl/B2/2020/arkusz.pdf` (SHA-256 `ff786439979e4bb935b3b228c27788e0ca43537f1bf12a5d3eec7715493d409c`)
and key `ExamBin/pl/B2/2020/klucz.pdf` (SHA-256 `1d794631516c9a7d71a4a3dc66d697e648e6282e4b78ca8c56e82e3606e4e19f`).
The MP3 SHA-256 is `e931d9c4466ee5186a6b47fb3a01201a93882791c8bbc74a2ea8275ba9f3d72e`;
this checkpoint does not certify segment boundaries or acoustic correspondence.

## Paper and key provenance

| Module | Paper | Key | Authored rows | Quarter points |
| --- | --- | --- | --- | ---: |
| Listening | pp. 3–8 | pp. 4–5 | 5 tasks, 38 questions, 5 answer variants | 160 |
| Reading | pp. 10–16 | p. 6 | 5 full passages, 45 questions | 160 |
| Grammar | pp. 18–24 | pp. 7–8 | 8 full test contexts, 75 items, 8 unconditional variants | 160 |
| Writing | pp. 25–26 | prompts in paper | 3 sets × 2 prompts | 40-point human rubric |

Listening tasks I–V contain respectively 8, 9, 8, 7, 6 question rows, worth
8, 9, 10, 7, 6 points. Task III/1 and III/6 each contain two unordered letters;
the key explicitly permits either order. Task V/6 asks for two Turkish-origin
words, each worth 0.5 point; one unordered `czajnik|filiżanka` row preserves
both answer slots and partial credit. Task V/1 has two official 0.5-point
alternatives, `z Afryki` and `z regionu Kaffa`; the complete answer is
`z dzisiejszej Etiopii`. Other transcript-consistent free responses require
manual review. Paper p. 6 and key p. 4 were visually inspected for the grouped
answers; key p. 5 was visually inspected for partial credit and open responses.

Paper p. 6 states Listening III is `10 p. (10 × 1 p.)`: items 1 and 6 each
have two printed blanks; the other six have one each. Key p. 4 gives
1=`A,D`, 6=`A,B` and permits either order. Each correct letter earns one
point (`creditPerCorrectQuarter: 4`), including a partial set. Paper p. 8
and key p. 5 assign 0.5 point to each word in Listening V/6
(`creditPerCorrectQuarter: 2`).

Reading tasks I–V have 5, 10, 10, 10, 10 questions worth 5, 10, 10, 10, 5
points. Task V's ten answers are each worth 0.5 point. The two-column matching
table on paper p. 13 and the three writing-set table on p. 26 were visually
inspected after PDF extraction. Grammar tasks I–VII have ten half-point items
each; VIII has five one-point transformations. The full paper text is included
with numbered gaps in `grammar-tests.core-data`, while the item rows retain
focused response context and the printed alternatives. Key p. 8 permits other
correct transformations, so VIII is `open_manual`.

Grammar II/8 accepts `w` only if II/6 did not use `w` (key p. 7). This
conditional answer is **not** an unconditional variant. The accepted
`EXAM-WORD-BANK-CONSTRAINT-ADDENDUM.md` specifies its linked row and scorer;
the conditional row remains to be added after the schema field exists.

## Checks and release boundary

- **PASS** — `biome fmt exams/pl/certyfikat-polski/B2/2020/*.core-data`:
  10 files, 0 problems.
- **PASS** — focused `biome data check --contract dsl/exam.data-contract`
  with all ten B2/2020 row bindings: 196 rows, 0 problems after grouping V/6
  and adding the explicit unconditional alternatives. Rerun after the
  conditional variant is added.
- **NOT RUN** — browser playback of the updated audio windows or independent
  acceptance in this source-data checkpoint. The catalog should remain hidden
  until audio, scoring, and independent acceptance finish.

The authored data are in `exams/pl/certyfikat-polski/B2/2020/`. No generated
Kotlin rows were edited by hand.

## Audio boundary update, 2026-11-08

The local MP3 SHA-256 is stated above; `ffprobe` duration is 1744.718350 s.
The full-file Polish Whisper **tiny** pass produced 473 segments in
`/tmp/polski-audio-audit/B2-2020/audio.json`. Independent Whisper **base**
clips around six transitions are in
`/tmp/polski-audio-audit/B2-2020/base-boundaries.json`. `ffmpeg
silencedetect=n=-35dB:d=0.25` checked signal at the joins. The first base/tiny
clips placed words into the long initial silence; the actual first speech
begins after 97.34 s, so that waveform governs the first start.

| Task | Source window (s) | Boundary evidence |
| --- | ---: | --- |
| I | 97–417 | closure 415.31–416.54; next speech 418.63 |
| II | 418–702 | closure 699.59–700.71; next speech 702.97 |
| III | 703–1082 | closure 1079.99–1081.18; next speech 1083.21 |
| IV | 1083–1357 | closure 1354.00–1355.14; next speech 1357.56 |
| V | 1357–1672 | closure 1669.98–1671.38; post-test instruction starts 1673.48 |

The waveform shows roughly 45-second pauses between repeated content in
each task; one playback of the full source window includes the recorded
repetition. A 60-second silent tail and later section-ending phrase are
excluded from Task V. These are ASR plus waveform checks, **not human acoustic
listening** or verbatim transcription certification. Generated-row and
browser artifact playback checks are pending after the next generation.
