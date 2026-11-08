# B2/2020 source-data checkpoint

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
- **NOT RUN** — full project generation, Gradle, browser, or audio-boundary
  verification in this source-data checkpoint. The catalog should remain
  hidden until audio, scoring, and independent acceptance finish.

The authored data are in `exams/pl/certyfikat-polski/B2/2020/`. No generated
Kotlin rows were edited by hand.
