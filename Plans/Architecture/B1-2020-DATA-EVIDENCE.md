# B1/2020 exam data checkpoint

Date: 2026-11-08. This is a source-data checkpoint under [NEXT-SESSION-EXECUTION.md](NEXT-SESSION-EXECUTION.md) P1. The exam is **not ready for the catalog**.

## Sources and authored rows

Compared `exams/pl/certyfikat-polski/B1/2020/*.core-data` with local `ExamBin/pl/B1/2020/arkusz.pdf` and `klucz.pdf`. Text was extracted with `pdfplumber`; the answer tables were also checked by word coordinates to resolve the TAK/NIE columns. Local paper pages 3–7 cover listening, 9–14 reading, 16–20 grammar, and 22 writing. Local key pages 3, 4, and 5–6 provide their respective answers. PDF visual-render verification was not run.

| Module | Paper count and points | Current DSL |
|---|---:|---|
| Listening I–V | 12+7+5+7 answer slots+5; 6+7+5+7+5 = 30 points | 5 tasks, 34 question rows; task IV has five opinions, with B and C requiring two answers each. All maxima sum to 120 quarter units. |
| Reading I–V | 5+7+7+7+8 = 34 questions; 5+7+7+7+4 = 30 points | 5 complete passage texts, 34 questions, 120 quarter units. |
| Grammar I–VIII | 10+5+5+10+5+5+10+5 = 55 items; 5+2.5+2.5+5+2.5+5+5+2.5 = 30 points | 8 instructions, 55 keyed items, 7 official full-credit alternative rows, 120 quarter units. |
| Writing | Three sets, two tasks per set | I: SMS 25 + report 175 words; II: opinion 40 + essay 160; III: wishes 25 + story 175, from paper p. 22. |

The former rows included watermark text, key headers, aggregate answers and an invented writing prompt. These were replaced with individual paper questions and official keyed answers. The optional `pointsQuarter` fields express half points precisely. The `sms` and `opinion` genre and Reading quarter-weight schema additions were coordinated with the main developer.

## Checks

- **PASS** — `node /Users/german/Work/Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs fmt exams/pl/certyfikat-polski/B1/2020/*.core-data` from repo root: nine canonical files.
- **PASS** — `biome data check --contract dsl/exam.data-contract` with all nine B1/2020 files bound to `ExamRow`, `ListeningTaskRow`, `ListeningQuestionRow`, `ReadingPassageRow`, `ReadingQuestionRow`, `GrammarTestRow`, `GrammarItemRow`, `GrammarAnswerVariantRow`, `WritingTaskRow`: 155 rows, 0 problems.
- **PASS** — independent count/quarter audit over authored rows: Listening 34 rows/120 quarters, Reading 34/120, Grammar 55/120, Writing 6.
- **NOT RUN** — full project generation/check, Gradle tests, browser execution, PDF render, audio listening/timestamp review, and delivery of `audio.mp3` in a web artifact.

## Acceptance blockers

1. Listening windows are now populated from the audio audit below. A browser playback check against a generated artifact remains pending.
2. Listening task IV key explicitly says answer order is arbitrary. B and C are recorded as grouped sets `1|4` and `2|4`, worth two points each. Current single-select listening UI cannot answer or score these sets. A set-aware answer mode must accept both members in either order before this module is available.
3. Grammar VI permits other correct transformations beyond the printed examples. The seven variant rows contain only alternatives printed in the key; exhaustive automatic grading is not established. Reading/listening end-to-end scoring and all browser input types also remain unverified.

## Audio boundary update, 2026-11-08

The local MP3 SHA-256 is `b88f61dea88036425bfd6990c07f308264a42b06d0aa9ce75a64a82bf3e840c8` and its `ffprobe` duration is 1491.173875 seconds. A full-file Whisper **tiny** Polish pass (`/tmp/polski-audio-audit/B1-2020/audio.json`, 298 segments) identified five task announcements and their content. A separate Whisper **base** pass over six transition clips (`/tmp/polski-audio-audit/B1-2020/base-boundaries.json`) checked the spoken closures and next announcements. `ffmpeg silencedetect=n=-35dB:d=0.25` checked the actual signal at each cut. The first base clip hallucinated/shifted words into a 90–102.19 s silence, so the first start uses the waveform and full-file timeline, not that clip's timestamp.

| Task | Source window (s) | Boundary evidence |
| --- | ---: | --- |
| I | 102–271 | first announcement after 102.19 s silence; closure around 268.10–269.58; next speech starts 272.06 |
| II | 272–553 | start 272.06; closure 550.85–552.09; next speech 554.32 |
| III | 554–798 | start 554.32; closure 795.66–797.05; next speech 799.41 |
| IV | 799–1154 | start 799.41; closure 1152.02–1153.28; next speech 1155.70 |
| V | 1155–1418 | start 1155.70; closure 1415.13–1416.33; post-task instruction starts 1418.86 |

Task I is announced as single-play. Tasks II–V explicitly announce two plays and the full-file ASR timeline contains repeated task content, so the player must use one playback of each entire window. This is ASR plus waveform verification, **not human acoustic listening** or a verbatim transcription audit. The new source windows need generated-row and browser artifact checks before catalog exposure.

Paper p. 6 states Listening IV is `7 p. (7 × 1 p.)`; B and C each have two
printed blanks, while A, D and E have one. Key p. 3 gives B=`1,4`, C=`2,4`
and permits either order. Each correct member of B/C earns one point
(`creditPerCorrectQuarter: 4`), including when the other is missing or wrong.

Grammar VI's five transformations are now `open_manual`: key p. 6 accepts
other correct solutions beyond the printed examples. Focused data check with
the nine B1/2020 source bindings passes with 155 rows and zero problems.
