# B2/2017 source-data checkpoint

Date: 2026-11-08. Scope: [NEXT-SESSION-EXECUTION.md](NEXT-SESSION-EXECUTION.md) P1 and [EXAM-COMPLETION-ADDENDUM.md](EXAM-COMPLETION-ADDENDUM.md). The exam remains hidden until audio, scoring, generation and browser gates pass.

## Paper and key

Read local `ExamBin/pl/B2/2017/arkusz.pdf` (27 pages) and `klucz.pdf` (8 pages) with `pdfplumber`; rendered the key's image-only listening-V table (key p. 5) and writing-prompt photograph (paper p. 22) with PDFium. The embedded writing photograph was cropped to `kotlin/composeApp/src/webMain/resources/exam-images/B2-2017-writing-III-b.jpeg` and visually inspected; SHA-256 `b9a94313954946c7946e27e647398f654ccce768d1511437ca42bf050275c1f2`. Local PDF pages: listening 3–6/key 4–5, reading 8–13/key 6, grammar 15–20/key 7–8, writing 22. The former rows included OCR watermark fragments, aggregate answers and a fake writing prompt.

| Section | Authored rows | Paper maximum |
|---|---:|---:|
| Listening I–V | 5 tasks, 43 question rows and 6 official form variants; per-task rows 12+8+8+6+9 | 6+8+8+10+8 = 40 points (160 quarter units) |
| Reading I–V | 5 passages, 45 questions; 5+10+10+10+10 | 5+10+10+10+5 = 40 points (160 quarter units) |
| Grammar I–VIII | 8 instructions, 65 items; 10+10+10+10+10+5+5+5; 2 official full-credit alternative rows | Eight tasks × 5 = 40 points (160 quarter units) |
| Writing | Three sets, two prompts each; III-b uses the extracted photograph | I wishes 20 + essay 280 words; II email 120 + review 180; III advertisement 60 + story 240 |

Listening IV A={1,2} is a prefilled example. The scored sets are B={1,2}, C={2}, D={3,4}, E={1,2,4}, F={3}, G={1}. These are grouped rows with maxima 2,1,2,3,1,1 points; set cardinality reaches three. Listening V first item has two distinct half-point slots (`młodą` and `miłą`), followed by seven one-point open answers. The key explicitly permits other transcript-consistent answers; printed phrases do not establish exhaustive automatic grading.

Paper p. 6 states Listening IV is `10 p. (10 × 1 p.)`. Example A has two
prefilled blanks and is excluded. B/D/E have respectively two, two and three
scored blanks, while C/F/G have one each, totaling ten. Key p. 4 lists
B=`1,2`, D=`3,4`, E=`1,2,4`. Each correct member earns one point
(`creditPerCorrectQuarter: 4`), including a partial set.

## Checks and blockers

- **PASS** — `biome fmt exams/pl/certyfikat-polski/B2/2017/*.core-data` from repo root; ten files canonical after the added Listening V variant source.
- **PASS** — initial focused `biome data check --contract dsl/exam.data-contract` with nine B2/2017 row bindings: 180 rows, 0 problems; updated ten-binding result is below.
- **PASS** — independent row/quarter audit: Listening 43/160, Reading 45/160, Grammar 65/160, Writing 6.
- **PASS** — after adding the Listening V form variants and `open_manual` on all nine slots, focused data check with ten row bindings: 186 rows, 0 problems. B1/2020 Grammar VI five transformations likewise have `open_manual`; its focused check remains 155 rows, 0 problems.
- **NOT RUN** — full project generation/check, Gradle, browser, media playback, audio boundary transcription, and delivery/browser availability of the writing image.

The five listening windows are now populated from the audio audit below. Task
IV needs unordered-set UI/scoring with cardinality derived from the answer
set; task V needs an adjudicated path for valid open responses. The exam is not
catalog-ready. Generated Kotlin remains owned by `biome generate`.

## Audio boundary update, 2026-11-08

The local MP3 SHA-256 is
`d1d865a43dd4a6e91ef2508e1476cdffca2413e0abcd25a9371b568b702cf84e`;
`ffprobe` reports 1800.045714 s. A full-file Polish Whisper **tiny** pass
(`/tmp/polski-audio-audit/B2-2017/audio.json`, 190 segments) located the five
tasks. Independent Whisper **base** clips over six joins
(`/tmp/polski-audio-audit/B2-2017/base-boundaries.json`) identified the spoken
closures/announcements. `ffmpeg silencedetect=n=-35dB:d=0.25` located actual
signal boundaries. Base hallucinated words through long silences in the first
and II/III clips; the cuts below follow the waveform and full-file timeline.

| Task | Source window (s) | Boundary evidence |
| --- | ---: | --- |
| I | 162–445 | first speech 162.15; closure 441.96–444.12; next speech 446.20 |
| II | 446–772 | closure 769.04–771.10; next speech 773.25 |
| III | 773–1055 | closure 1052.20–1054.33; next speech 1056.40 |
| IV | 1056–1400 | closure 1396.78–1398.98; next speech 1401.14 |
| V | 1401–1692 | closure 1689.44–1691.64; post-task instruction follows 1692.60 |

The recording itself contains the announced repeats. These are ASR plus
waveform-derived windows, **not human acoustic listening** or verbatim
transcription certification. Generated-row and browser playback checks remain
pending after regeneration.

Key p. 5 visibly prints Listening V/1 as `młoda (osoba)` and `miła (osoba)`
in nominative, while its transcript quotes `miłą, młodą osobę` in accusative.
Both slots are worth 0.5 point. The existing accusative adjective answers
remain, and `listening-answer-variants.core-data` adds the printed nominative
forms with/without `osoba` and the transcript accusative phrase with `osobę`.
All nine V answer slots are `open_manual` because the same key explicitly
allows other transcript-consistent responses; the listed forms are examples,
not an exhaustive automatic key.
