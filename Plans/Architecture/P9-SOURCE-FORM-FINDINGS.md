# P9 source form findings

Read-only source inspection, 2026-11-08. This supplements [P9–P12](NEXT-SESSION-P9-P12-PLAN.md); it does not approve a schema implementation or certify all eleven papers. Original files and hashes are listed in [provenance](P9-SOURCE-PROVENANCE.md). Temporary PDFKit text extraction includes explicit physical-page markers; visual inspection remains required for table slots, underlining and illustrations.

## B2 / February 2024: reading unordered sets

Visually checked arkusz physical page 15 (printed reading page 7) and key physical page 6. Reading task V declares ten answers worth one point each (task instruction begins on physical page 14). The table groups these into six questions: slot counts `1,3,1,2,1,2`. The key explicitly permits arbitrary order for questions 2, 4 and 6:

| Question | Correct labels | Maximum quarter-points |
| --- | --- | ---: |
| V/1 | D | 4 |
| V/2 | C, D, E | 12 |
| V/3 | C | 4 |
| V/4 | A, C | 8 |
| V/5 | B | 4 |
| V/6 | A, E | 8 |

P11/P9 must preserve grouped answer identity and the printed number of selections. The current listening unordered-set evaluator is the relevant existing contract; a single exact pipe-joined reading string would reject valid reordered answers. Reading requires the same explicit answer mode/slot credit support before this paper can be activated. This is a source-backed extension, not a request to change existing single-answer reading items.

## Timing observations to verify during module authoring

All eleven extracted papers contain four written-module cover times in source order: listening 40 minutes (most say “do 40 minut”), reading 60, grammar 60, writing 90. These are observations from text extraction, not authored `ExamModuleRow` defaults: each paper's cover pages and maxima must still be checked when its DSL config is written. C1 March 2022 says “40 minut” without “do”; preserve source wording in provenance. Oral examination content occurs in some source PDFs, but the accepted P11 scope is the four written parts and must not imply certification of speaking.

## Transformation grading

B2 February 2024 key page 8 contains per-transformation quarter-point annotations and explicitly accepts other correct solutions. Preserve canonical complete answers and source-backed variants; unmatched nonblank free text requires manual evaluation. Do not infer automatic partial points from substring overlap with transformed fragments. C1 April 2024 dependent forms already have their dedicated [contract](C1-2024-ANSWER-CONSTRAINT-ADDENDUM.md).

## C1 writing sets contain either one or two prompts

Visually checked C1 April 2024 arkusz physical page 27 (printed writing page 2): set I has a 100-word speech and 300-word argument; set II has a 180-word group characterization and 220-word report; set III has one 400-word essay. All five extracted C1 papers explicitly describe two prompts in I/II and one prompt in III; other C1 pages still need their per-paper visual gate. A writing session must derive the selected set's prompt count from DSL rows, preserve its draft(s), and avoid a global two-prompt requirement or invented III/b. The current writing genre enum lacks `speech`; source-backed speech support is needed before C1 activation, while characterization must be classified explicitly rather than silently losing its instruction. The common writing maximum is confirmed here as 40 points; manual assessment remains required.
