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

1. Every listening segment is still `0..0` (unmarked). `ffprobe` reports the local MP3 duration as 1491.173875 seconds. Segment boundaries and repeat behavior require listening verification; the local `ExamBin` URL is not yet a delivered web asset.
2. Listening task IV key explicitly says answer order is arbitrary. B and C are recorded as grouped sets `1|4` and `2|4`, worth two points each. Current single-select listening UI cannot answer or score these sets. A set-aware answer mode must accept both members in either order before this module is available.
3. Grammar VI permits other correct transformations beyond the printed examples. The seven variant rows contain only alternatives printed in the key; exhaustive automatic grading is not established. Reading/listening end-to-end scoring and all browser input types also remain unverified.
