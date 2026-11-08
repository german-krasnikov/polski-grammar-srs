# B2/2019 source-data checkpoint

Current integration update, 2026-11-08: the source checkpoint and pending/hidden statements below are historical. The schema, six-exam catalog, delivered media and browser behavior have since passed the bounded [six-exam acceptance](SIX-EXAM-BROWSER-ACCEPTANCE.md). That receipt records exact artifact hashes, the earlier intermittent seekability failure and remaining limits; it does not mark P10/P11 or the entire exam plan complete.
Date: 2026-11-08. The sample paper is the locally supplied
`ExamBin/pl/B2/2019/arkusz.pdf` (SHA-256
`4c210043516fafc255872c570fdd600b43fd507fb021ad7f232b4a374ab3acb2`);
the key is `klucz.pdf` (SHA-256
`61bb998f0693284184109386561bbf37fa740a0c4f1c6e88843c218433404c93`).
The local MP3 SHA-256 is
`4aa580740d38b5f5a3cbcd10c0b6b4e9ed700d6c1407dce60c2733445d9b9650`.
These are local bytes, not a claim of a fresh official download or publication.

## Restored paper content

| Module | Paper | Key | Authored rows | Quarter points |
| --- | --- | --- | --- | ---: |
| Listening | pp. 3–8 | p. 5 | 5 tasks, 36 questions, 3 printed single-answer variants | 160 |
| Reading | pp. 10–16 | p. 6 | 5 full passages, 45 questions | 160 |
| Grammar | pp. 18–24 | pp. 7–8 | 8 full task texts, 75 items, 7 official variants | 160 |
| Writing | pp. 25–26 | prompts in paper | 3 sets × 2 prompts | 40-point human rubric |

Listening tasks I–V have respectively 8, 6, 8, 8, 6 question rows and
8, 6, 8, 10, 8 points. Paper p. 7 gives two printed one-point slots in
IV/4 and IV/7. Key p. 5 gives B/D and A/E, permits either order, and assigns
one point per correct letter (`answerMode: unordered_set`,
`creditPerCorrectQuarter: 4`). Paper p. 8/key p. 5 give two one-point slots
in V/1 and V/3 and two half-point slots in V/4. These are also one grouped
row per question, with per-correct quarter values 4, 4, and 2. The key says
other transcript-consistent responses are acceptable, so all six V questions
are `open_manual`; the three explicit single-question variant rows are only
known examples. Key p. 5 and the plant-photo table on paper p. 7 were visually
inspected.

The five reading tasks have 5, 10, 10, 10, 10 questions, worth
5, 10, 10, 10, 5 points. Full source passages, gap fragments, matching
dialogue, and word bank are present. The two-column matching table on paper
p. 13 and the answer table on key p. 6 were visually inspected. Reading IV
answers are `NIE, TAK, BI, NIE, BI, NIE, NIE, TAK, NIE, TAK`; task V has ten
half-point word-bank answers.

Grammar tasks I–VII have ten half-point items each; VIII has five one-point
transformations. The complete paper text, numbered gaps, examples and printed
choices are in each `GrammarTestRow.instruction`; item rows provide focused
response context. Key p. 8 gives explicit alternatives for VI/9, VII/6,
VII/10 and several VIII answers; all VIII items are `open_manual` because the
key accepts other correct transformations. No conditional word-bank variant
is present in this paper.

Writing p. 26 was visually inspected. Set I includes the complete printed
job advertisement for the motivational letter (100 words) and a review
(200); set II has an invitation (30) and argument (270); set III has a formal
`podanie` to a housing manager (100) and a story (200). The latter requires
new `WritingTaskRow.genre: application`; classifying it as an ordinary letter
would lose the paper's genre. The advertisement is text, not an omitted photo.

The five plant photos and printed A–E labels on Listening IV paper p. 7 were
rendered into
`kotlin/composeApp/src/webMain/resources/exam-images/B2-2019-listening-IV.png`
(SHA-256 `1b7a90cae4637109d3a47869794dd933adcc422978c9b4977b1e25095462af12`),
visually checked, and linked from `ListeningTaskRow.promptImageUrl` with alt
text. This is a local source image; artifact delivery has not yet been tested.

## Verification and blockers

- **PASS** — `biome fmt exams/pl/certyfikat-polski/B2/2019/*.core-data`:
  ten files, 0 problems.
- **PASS** — focused `biome data check --contract dsl/exam.data-contract`:
  Listening 45 rows/4 bindings, Reading 51 rows/3, Grammar 91 rows/4;
  each has 0 problems. Each scored module sums to 160 quarter units.
- **FAIL (known contract gap)** — focused Writing check: six rows, one
  `DATA_ENUM` at `writing.core-data:51` because `application` is not yet in
  the shared genre enum. The main developer owns this additive contract.
- **NOT RUN** — combined ten-binding check, full project generation of this
  restoration, Gradle, browser input/playback, native devices, and image
  delivery in the web distribution.

Listening windows were independently corrected in
`EXAM-AUDIO-BOUNDARY-AUDIT.md`; the exam remains hidden until the schema,
generation, scoring, audio, and independent acceptance gates pass. Generated
Kotlin rows were not edited by hand.
