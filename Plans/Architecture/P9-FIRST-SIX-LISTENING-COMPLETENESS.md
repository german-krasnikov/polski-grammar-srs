# P9 first six papers: listening instruction completeness

2026-11-08. This targeted data correction covers only `listening-tasks.core-data` for the first six inactive source drafts. The latest B2/2023-06 and B2/2022-02 drafts were not changed during their independent audit. No scored `LISTENING_QUESTION` row, answer, audio interval, transcript word, or module maximum was changed in this pass.

The task instructions now retain the official direction and **complete unscored worked example** before the first scored item. Multiple-choice example answers are annotated from the underlined source option; true/false examples show the column containing the printed `X`; matching examples keep the source's filled-in answer box. Scored item prose and its options are displayed only by the corresponding question rows. Source attributions following scored items remain at task level. A shared bank remains in the task instruction when the question options contain only its labels.

| Paper | Arkusz physical pages, tasks I–V | Corrections and retained shared content |
| --- | --- | --- |
| B2/2024-02 | 3, 5, 6, 7, 8 | Task I now includes both complete Amy Bauernschmidt example multiple-choice groups and printed answers **b, a** (p. 3); previously it stopped at the general header. Tasks II–V now include their full worked examples; task III keeps the A–D place bank. Removed repeated scored descriptions/questions from task III. |
| B2/2024-06 | 3, 4, 5, 6, 7 | Tasks I–V keep worked examples; task III keeps the A–D beach bank. Removed scored question text/options from task instructions. Source-marked examples: I **c, a**, II **TAK**, III **A**, IV **c**; V's written answer remains verbatim. |
| B2/2023-02 | 3, 5, 6, 7, 8 | Tasks I–V keep worked examples; task IV keeps the **1–4** animal bank. Removed scored question text/options from task instructions. Source-marked examples: I **c, a**, II **a**, III **NIE**, IV **1**; V's written answer remains verbatim. |
| C1/2024-04 | 2, 3, 4, 5, 6 | Tasks I–V keep worked examples; task II keeps the **1–4** plant bank, and task V keeps the full **A–I column II** proposition bank after separating the interleaved printed table. The task V example maps “Jedzenie spożywane w złym towarzystwie” to **E** as marked below the table on p. 6. Other source-marked examples: I **a**, II **1**, III **TAK**, IV **b**. |
| C1/2023-04 | 3, 4, 5, 6, 7 | Tasks I–V keep worked examples; task IV keeps the six-person matching context. Removed scored question text/options from task instructions. Source-marked examples: I **a**, II **TAK**, III **b**, IV **1**; V's written answer remains verbatim. |
| C1/2023-11 | 3, 4, 5, 6, 7 | Tasks I–III and V keep worked examples; task III keeps the **1–5** treasure bank. Task IV keeps its complete **0–12 statement bank** because the one scored control contains only statement numbers; the printed example is statement **0**. Source-marked examples: I **b**, II **NIE**, III **1**; V's written answer remains verbatim. The post-item-8 Polskie Radio attribution remains outside the scored option. |

All example marks above were read on rendered arkusz pages, including the underlines and true/false table positions that PDF text extraction does not preserve. The source PDF text supplied the example prompts and all option/bank wording. Task-level attributions follow the printed source text, including URLs that PDF extraction splits across a line; the drafts do not treat those attributions as answer choices.

## Checks and boundary

- **PASS:** `biome fmt --check` and `biome read --format json` on all six touched listening-task files.
- **PASS:** parsed listening question row counts remain B2/2024-02 **38**, B2/2024-06 **37**, B2/2023-02 **36**, C1/2024-04 **38**, C1/2023-04 **37**, C1/2023-11 **35**; each still sums to **160 quarter-points**. All choice/matching keyed answers remain members of their option sets. No unique scored question text or scored option body was found in the shortened instructions; generic stems legitimately repeated in worked examples were not treated as leaks.
- **PASS, content scan:** no `ROZUMIENIE ZE SŁUCHU`, `Transkrypcje`, `KLUCZ DO ZADAŃ`, source URL, or printed page-date footer appears in these six official transcript fields. No scored question option contains an arkusz source attribution.
- **NOT RUN:** generation, Kotlin compilation, browser/audio-media acceptance, independent re-review of this instruction correction, and catalog activation. These papers remain inactive drafts; proposed answer-policy fields elsewhere are unaffected.

Recovery note — 2026-11-12: restored this historical authoring record and the corresponding source edits from the original agent history. Current format and parse checks passed; historical independent findings require fresh recheck after corrections.
