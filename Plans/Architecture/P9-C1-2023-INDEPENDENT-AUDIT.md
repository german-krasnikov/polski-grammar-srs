# P9 C1 2023 papers: independent data audit

2026-11-08. Scope: the inactive data drafts `exams/pl/certyfikat-polski/C1/2023-04/` and `C1/2023-11/` in `/tmp/polski-p9-data-worktree`, base revision `5cf6515`. I own only this report. No source, schema, generator, catalog, or media file was changed.

## Official sources and method

Independent SHA-256 checks of the six ignored local binaries match `P9-C1-2023-SOURCE-EVIDENCE.md` and `P9-SOURCE-PROVENANCE.md`: April arkusz `331e7ea0…`, key `0c90381c…`, audio `7836d6d0…`; November arkusz `e68f923e…`, key `77217b89…`, audio `920624ab…`. I used PDFKit text with physical-page markers, visually read the April key physical p. 7 and November key pp. 5, 7 where PDF extraction loses table columns, and parsed each `.core-data` file using `biome read --format json`. The published key, not generated Kotlin, supplied expected answers.

The authored module order and 40/60/60/90-minute source limits match both paper covers; listening says **up to** 40 minutes. April has 37 listening, 50 reading, 75 grammar scored rows; November has 35, 43, 75. Each of those six scored modules totals 160 quarters. Writing remains manual. The numbered answers and source-backed variants agree with the key pages: April listening key pp. 5–6, reading p. 7, grammar pp. 8–9; November listening pp. 5–6, reading p. 7, grammar pp. 8–9. The April listening IV E/F groups each carry two labels at one point per label, and listening V/4 has two one-point facts while V/2, V/5 and V/7 have two half-point facts. November listening IV carries key labels `2|5|6|8|9|12` at one point each; November reading IV groups 2 and 6 each have two fragment labels. These are source-data comparisons, not scorer execution.

No scored `READING_QUESTION.questionText` is duplicated in its passage among April's 50 and November's 43 questions. Long article bodies, unscored examples, and full matching fragments/headings are present in passage or control fields. Every unique substantive April option value (39 listening, 93 reading) occurs in the arkusz PDF text after removing editorial letter prefixes. November has 24 substantive listening and 74 reading values with the same content match. All authored keyed option answers belong to their respective authored option sets. There is no writing photograph in either arkusz writing task.

Both grammar V banks match the official arkusz physical p. 23 as contiguous ordered sequences of 13 printed occurrences. April includes crossed `który`, with `wprawdzie` and `chyba że` excess; November includes crossed `co najmniej`, with `zamiast` and `których` excess. Each occurrence has its own row. No task-wide word-reuse penalty follows from these source checks. All five April writing prompts match contiguous arkusz physical p. 28 text and the printed 80+320, 200+200, one 400-word set structure. All five November prompts retain the printed substance and identical word limits; three match PDFKit text literally, while I/b and II/a differ only in spacing around the quoted final punctuation. No III/b row exists.

## Source-fidelity finding

The November listening I arkusz physical pp. 3–4 prints answer labels with periods (`a.`, `b.`, `c.`), but all eight authored `certyfikat-polski-C1-2023-11-l1-q1` through `-q8` options use parentheses (`a)`, `b)`, `c)`). All 24 substantive option texts match the paper after removing that prefix, and keyed choices point to the matching authored option strings. This is a **minor literal transcription mismatch**, with no observed key/score change. It should be reconciled if the displayed control is intended to preserve the paper's printed labels exactly. No other specific source mismatch was found in the checked rows.

## Forward contract and acceptance limits

November listening IV's `requiredSelections: 6` and V/1, V/2, V/7's `requiredSelections: 2` are proposed fields. The key requires six of twelve statements and two distinct facts from larger accepted pools; slash-separated wording in a fact also represents aliases. I confirmed those instructions and key pools on arkusz physical pp. 6–7/key pp. 5–6. Whether the shared scorer rejects duplicate facts, groups aliases correctly, awards 0/1/2 or 0…6 units, and stops at the required count is **NOT RUN**. April's mixed one-point/half-point fact groups likewise require scorer and browser evidence. Neither source paper is generated or catalog-active.

The candidate audio windows in `P9-AUDIO-BOUNDARY-AUDIT.md` and the source MP3 hashes do not prove audible cut points, announced replay, or browser delivery. Independent acoustic review, DSL generation/check, Kotlin contract compilation, clean-checkout media delivery, browser/device acceptance and catalog activation are **NOT RUN**.

| Required check | Status |
| --- | --- |
| Official PDF/key/MP3 identity, four-module inventory and source maxima | PASS for these two drafts |
| Indexed answers, options, bank occurrences, writing prompts and limits | PASS within the cited source pages; minor November listening label mismatch above |
| Reading passage/control separation | PASS in parsed data; browser presentation NOT RUN |
| November required-selection/alias semantics and April mixed-credit groups | Source rule confirmed; evaluator/control behavior NOT RUN |
| Acoustic windows, generation, Kotlin/browser/device integration, activation | NOT RUN |

Recovery note — 2026-11-12: restored verbatim from the original independent tester tool history after temporary worktree cleanup. Results above are historical; author corrections require a fresh recheck.
