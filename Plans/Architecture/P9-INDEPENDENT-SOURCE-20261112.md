# P9 independent source audit: eleven inactive papers

Date: 2026-11-12. Tested tree: persistent `p9` worktree at `14b25a9`, clean before this report. Scope: inactive C1 March 2022, C1 November 2022, B2 June 2022 drafts. Source: ignored official `ExamBin/pl/<level>/<session>/{arkusz,klucz}.pdf` in the main checkout. This is a source-data audit, not a runtime or publication approval. [Author evidence](P9-2022-REMAINING-SOURCE-EVIDENCE.md) and [provenance](P9-SOURCE-PROVENANCE.md) were read; the newer main-checkout [audio audit](https://github.com/german-krasnikov/polski-grammar-srs/blob/main/Plans/Architecture/P9-AUDIO-BOUNDARY-AUDIT.md) supersedes earlier candidate notes.

## Method and source integrity

I independently SHA-256 hashed the six PDFs; all match the pinned values in `P9-SOURCE-PROVENANCE.md`. PDFKit extracted text from every physical page of each arkusz and key. I visually inspected the key's column and grouped-answer pages: C1 March pp. 6–7, C1 November pp. 5–6, B2 June pp. 4–6. I also rendered each paper's grammar word-bank page (C1 p. 23, B2 p. 19) and writing page (C1 p. 28, B2 p. 26). Draft rows were read through `biome read --format json`, independently of Kotlin mappers and generated output.

The C1 March key has `marzec 2021` running headers on physical pp. 1–9, although its cover says March 2022 and the paired arkusz is dated March 2022. This is a source-document inconsistency. The paper and key pair by substantive content: the key's listening I fire/fountain sequence, March reading V grouped answers, and grammar V bank match the March 2022 arkusz prompts and authored rows. The erroneous running header is absent from draft transcripts. The PDF identity/provenance concern should remain visible; no date was silently inferred from that header.

## Key and quarter comparison

| Paper | Scored row counts L/R/G | Quarter totals | Key pages inspected | Result |
| --- | --- | --- | --- | --- |
| C1/2022-03 | 40 / 41 / 75 | 160 / 160 / 160 | L pp. 5–6; R p. 7; G pp. 8–9 | PASS for canonical answer sequences, option labels and group credit. |
| C1/2022-11 | 36 / 50 / 75 | 160 / 160 / 160 | L pp. 4–5; R p. 6; G pp. 7–8 | PASS for canonical answer sequences, option labels and group credit. |
| B2/2022-06 | 39 / 45 / 75 | 160 / 160 / 160 | L pp. 4–5; R p. 6; G pp. 7–8 | PASS for canonical answer sequences, option labels and group credit. |

The authored primary keys match each numbered source table. I resolved TAK/NIE/BI using the rendered table columns, since extracted PDF text loses column position. Special groups were checked explicitly:

- C1 March Reading V, key p. 7: `A,D | A | B,C | E | A,B,D | B`, order free within each point. Draft has 2/1/2/1/3/1 selections and 8/4/8/4/12/4 quarter maxima. Reuse of `A` and `B` across different questions is present in the key and rows.
- C1 November Listening III, key p. 5: exactly five true statements `2,3,6,8,10`, any order, 20 quarters. Draft has one unordered-set question, five required selections and four quarters per choice.
- B2 June Listening V/3 and V/8, key p. 5: central location plus elevated standard carries 8 quarters (4+4); dishwasher plus washing machine carries 4 quarters (2+2). Draft reflects both identities/counts. Listening V/5's bare `cena` has only 2 quarters in the key and in its authored scored variant; qualified price wording carries 4.
- C1 November Grammar V/4, key p. 8: `mianowicie` is accepted only if not used in V/9. The draft variant carries `onlyIfNotUsedBy` pointing to V/9. A runtime consumer for that forward field was not verified.

Every authored answer for a choice question is present in that row's option list. A distinct five-word anchor scan of question and option text found 117/117 C1 March, 112/112 C1 November and 101/101 B2 June substantive anchors in the respective arkusz. The excluded misses were editorial `Luka N w tekście ...` labels and one editorial November listening instruction. This is an anchor check, not an exact full-passage proof. All 15 listening transcript starts and ends matched their key text; word-level comparison of each complete transcript span found only PDF page headers and source attribution URLs inserted on the key side, with no substantive wording edit in the draft.

## Examples, banks, writing and forward fields

The printed grammar banks match the ordered draft occurrences, including crossed worked examples: 13 tokens in each C1 Grammar V (arkusz p. 23) and 11 in B2 Grammar II (p. 19). The B2 Reading V 14-word bank includes the crossed `dziedziczone` example and the three unused words printed in key p. 6. Worked examples remain in source instruction or passage text and do not create scored rows. The primary and enumerated alternate grammar/listening answers inspected on key pp. 5–9 match authored variants; open transformations remain `open_manual`.

Writing matches the rendered source pages: C1 March sets I 180+220, II 200+200, III 400; C1 November I 100+300, II 80+320, III 400; B2 June I 30+270, II 140+160, III 50+250. Genres and all 16 substantive prompts match. None of these three writing pages contains a photograph or a photo-dependent prompt, so no crop is required for this slice. C1 writing `speech` and B2 reading `word_bank` remain forward contract values; no active mapper acceptance is claimed. The C1 candidate audio windows in draft rows match the newer main-checkout audit; I did not independently listen. B2 June's damaged-audio boundary fields remain omitted on all five tasks, as required by the source-integrity block.

## Findings and activation gates

1. **Forward contract blocker, C1 November Listening V/3.** Key physical p. 5 accepts `Inków` plus either `skarbu` or the castle wording `zamku niedzickiego / zamku w Niedzicy`. Draft `listening-questions.core-data` q3 has `answerMode: unordered_set`, canonical `Inków|skarbu`; two full-pipe rows in `listening-answer-variants.core-data` preserve the castle alternatives. Current shared `ListeningExamScorer` sends unordered sets to the set scorer without `scoredVariants`, so those alternatives would not auto-confirm if published. Represent the second fact's accepted wordings in the accepted fact/alias source contract or establish an equivalent DSL-tested consumer before activation. This is not a discrepancy in the authored wording.
2. **Forward contract blocker, C1 November Grammar II.** Key physical p. 7 assigns four separate quarter-points to each of five transformations, including accepted alternate phrasings. Draft `grammar-items.core-data` g2-q1…q5 records the 4-quarter maximum and `open_manual` but no component-credit facts. This preserves total maximum and defers unknown wording; it does not yet encode the published partial-quarter rubric. A source-backed component-credit contract and acceptance cases are required before automatic grading is claimed.
3. **Minor transcription, C1 March Writing II/a.** Arkusz physical p. 28 prints `„Na ratunek ginącym zwierzętom”.`; `writing.core-data` has a space before the period: `„Na ratunek ginącym zwierzętom” .`. Task substance and 200-word limit match. Source author should remove the stray space.
   Resolution: the coordinator removed the stray space in `362224e`; the one-file Biome format check passed. The original observation above is retained as audit history.
4. **Existing source-integrity block, B2 June audio.** The pinned MP3 has a damaged zero tail per the newer provenance audit. I did not set or infer any task V endpoint. Listening activation remains blocked independently of the paper/key transcription results.

## Check status

- **PASS, source inspection:** six PDF hashes, complete PDFKit extraction, rendered key/bank/writing pages, typed read of three draft paper sets, all numbered primary key groups and quarter sums, all 15 transcript word spans modulo key headers/URLs.
- **PASS, source boundary:** choice-key membership, printed bank order and example occurrence, writing set structure/limits, absence of B2 audio windows. The tester did not edit draft source files.
- **NOT RUN:** active `biome generate --check`, Kotlin compilation/evaluation of these inactive rows, human acoustic confirmation, browser/device acceptance, media delivery and catalog publication. A passing source audit does not clear those gates.

Reproduction commands from `p9`: `shasum -a 256 <six PDFs>`; `swift /tmp/p9_pdf_extract.swift <pdf>`; `swift /tmp/p9_pdf_render.swift <pdf> <physical page> <png>`; `node /Users/german/Work/Python/biome-dsl-core/implementations/typescript/packages/biome/src/bin.mjs read --format json <source.core-data>`. Temporary read-only comparison scripts and extracted text live under `/tmp/p9-*`; they are not a production fixture or artifact.

## Fresh recovered-eight recheck

The next eight inactive drafts were restored by `1864bb7` after their earlier source audits. I rechecked the current rows at `3f8fc9c` against the ignored original PDFs, rather than treating the restored historical receipts as current test results. The eight are B2 February/June 2024, C1 April 2024, B2 February/June 2023, C1 April/November 2023 and B2 February 2022. All 24 arkusz/key/MP3 SHA-256 values match `P9-SOURCE-PROVENANCE.md`. PDFKit extracted the 16 PDFs; `biome read --format json` parsed each restored source row. No source file was edited by this audit.

| Draft | L/R/G scored rows | L/R/G quarters | Fresh source result |
| --- | --- | --- | --- |
| B2/2024-02 | 38 / 41 / 75 | 160 / 160 / 160 | PASS for counts, grouped rows and targeted key p. 6. |
| C1/2024-04 | 38 / 53 / 75 | 160 / 160 / 160 | PASS for counts and restored transcript. |
| B2/2024-06 | 37 / 45 / 75 | 160 / 160 / 160 | PASS for counts and targeted key p. 5. |
| B2/2023-02 | 36 / 45 / 75 | 160 / 160 / 160 | PASS for counts and restored transcript. |
| C1/2023-04 | 37 / 50 / 75 | 160 / 160 / 160 | PASS for counts and restored transcript. |
| B2/2023-06 | 39 / 45 / 75 | 160 / 160 / 160 | PASS for counts, corrected transcript and targeted key p. 5. |
| C1/2023-11 | 35 / 43 / 75 | 160 / 160 / 160 | PASS for counts, corrected option labels and targeted key pp. 5–6. |
| B2/2022-02 | 38 / 40 / 75 | 160 / 160 / 160 | PASS for counts, corrected transcript and targeted key p. 5. |

No authored choice key falls outside its own option list. All 40 complete listening transcript spans match the key's word sequence after removal of key-side PDF running headers, page numbers and source URLs. In particular, B2 June 2023 task I now joins `Raz-dwa załatwimy sprawę i w drogę.` directly to item 4 without a page header; B2 February 2022 task II now joins `można śmiało` directly. This resolves the two source-contamination failures in the [historical independent receipt](P9-B2-2023-06-2022-02-INDEPENDENT-AUDIT.md). C1 November 2023 listening I options now use the paper's `a.`, `b.`, `c.` labels on arkusz pp. 3–4, resolving the historical label finding in [its receipt](P9-C1-2023-INDEPENDENT-AUDIT.md).

I visually rechecked the highest-risk grouped keys on rendered pages: B2 February 2024 Reading V key p. 6 has group sizes 1/3/1/2/1/2, including order-free `C,D,E`, `A,C`, `A,E`; B2 June 2024 Listening V/3 key p. 5 permits any two of three facts; B2 June 2023 Listening V/7 key p. 5 permits any one of three facts; B2 February 2022 Listening V/3 key p. 5 permits any two listed answers; C1 November 2023 Listening IV key p. 5 lists six true statements `2,5,6,8,9,12`, and V key p. 6 has two half-point response slots in items 1, 2, 7 and 8. The current `answerMode`, `requiredSelections` where the source pool exceeds slot count, `creditPerCorrectQuarter`, and authored pools agree with these pages. The B2 February 2024 Reading V result also agrees with the earlier [form finding](P9-SOURCE-FORM-FINDINGS.md).

The earlier independent receipts for these eight contain broader page-by-page key, bank, example, writing and photo comparisons. I reused those only for fields unchanged by restoration; this fresh pass directly rechecked the corrected transcript/label spans, all current module quarter sums and option membership, all 40 current transcripts, and the listed grouped keys. The source-question five-word anchor scan has editorial and PDF-layout misses, so I do **not** claim exact full-question or passage equality from it. A fresh exhaustive visual re-audit of every key cell, word bank and writing prompt in these eight remains **NOT RUN**. Set resolver/browser behavior, forward field validation, acoustic cut points, clean-checkout media delivery and activation also remain **NOT RUN**. The restored papers stay inactive.

Read-only reproduction artifacts: `/tmp/p9-recovered-invariants.txt`, `/tmp/p9-recovered-transcript-diff.txt`, `/tmp/p9-recovered-text-snippet.txt` and rendered `/tmp/p9-recovered-*-key*.png`. The scripts `/tmp/p9_recovered_invariants.py` and `/tmp/p9_recovered_transcript_diff.py` invoke the pinned local Biome CLI. These are temporary evidence, not committed fixtures.
