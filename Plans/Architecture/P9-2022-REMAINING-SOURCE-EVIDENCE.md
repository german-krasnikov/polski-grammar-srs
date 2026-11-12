# P9 final three 2022 papers: inactive source drafts

2026-11-12. The adult C1 March 2022, C1 November 2022, and B2 June 2022 written papers are drafted as source rows under `exams/pl/certyfikat-polski/`. These are **inactive drafts**. They are outside the active Biome project, generated Kotlin, catalog, and browser exam flow. The official PDF and MP3 files remain unchanged in the ignored main-checkout `ExamBin` vault; [source provenance](P9-SOURCE-PROVENANCE.md) lists official URLs, byte sizes, and all nine matching SHA-256 values.

## Source comparison

I extracted PDFKit text from all arkusz/key pages, inspected rendered key pages for table columns and grouped answers, and read the bank pages and writing covers. The C1 March 2022 key PDF has a **“marzec 2021” running header**, while its paired arkusz and archive filename identify March 2022. That header is excluded from transcripts and data; this source discrepancy needs independent provenance review. No ASR wording supplied answer or transcript text.

| Paper | Listening / reading / grammar scored rows | Each scored module | Writing sets and word limits | Local candidate audio windows (seconds) |
| --- | ---: | ---: | --- | --- |
| C1/2022-03 | 40 / 41 / 75 | 160 quarter-points | I: 180+220; II: 200+200; III: 400 | 110–547, 547–902, 902–1344, 1344–2018, 2018–2436 |
| C1/2022-11 | 36 / 50 / 75 | 160 quarter-points | I: 100+300; II: 80+320; III: 400 | 110–444, 444–853, 853–1257, 1257–1733, 1733–2174 |
| B2/2022-06 | 39 / 45 / 75 | 160 quarter-points | I: 30+270; II: 140+160; III: 50+250 | **Unverified; fields omitted** |

The C1 windows are the *candidate* boundaries from the newer [P9 audio boundary audit](P9-AUDIO-BOUNDARY-AUDIT.md) in the main checkout, not a fresh acoustic check. The B2 June MP3 has a damaged zero tail. The five B2 `LISTENING_TASK` rows retain their official audio URL and source transcript, but deliberately omit the required `segmentStartMs`/`segmentEndMs` fields. Do not invent a task V endpoint, truncate it, activate the paper, or call contract validation passed until a sound source is obtained and reviewed.

The C1 March reading V key groups six questions into **2, 1, 2, 1, 3, 1** labels, with arbitrary order inside each group (key physical p. 7). November listening III requires any five true statements, keyed **2, 3, 6, 8, 10** (key p. 5). June listening V/3 requires both the central location and elevated standard for two points; V/8 requires both appliance names for one point (arkusz pp. 8–9/key p. 5). These rows carry forward `requiredSelections` and set credit where needed; scorer and UI acceptance are still required. November C1 grammar V/4 permits `mianowicie` only if unused in V/9, encoded as `onlyIfNotUsedBy` (key p. 8). All transformations remain open manual, and writing remains manually assessed.

All printed grammar bank occurrences are ordered and preserved: 13 each in C1 March and November task V (arkusz p. 23), and 11 in B2 June task II (arkusz p. 19), including crossed worked examples and excess tokens. The B2 June reading V bank has 14 printed words, including the example and three excess words. Worked examples stay in the passage or task instruction. Source PDF running headers and date footers were removed from prose, options, and transcripts. No source attribution was put inside a scored answer choice.

## Current checks and unresolved gates

- **PASS, source syntax:** `biome fmt --check` on the 36 owned `.core-data` files; `biome read --format json` parsed each one. These commands ran in the P9 worktree on 2026-11-12.
- **PASS, row invariants:** distinct IDs, valid local references, five listening tasks and reading passages, eight grammar tests, 160 quarter-points for each of nine scored modules, keyed selectable answers within their option sets, and source-literal substantive option/question text after editorial label removal. All 16 writing prompt texts occur in the arkusz PDFs with the printed word limits. `git diff --check` passed.
- **NOT RUN, acceptance:** independent source audit of these newly authored rows, active `biome generate --check`, DSL contract/evaluator compilation, acoustic verification of the C1 candidates, any B2 audio boundary, clean-checkout media delivery, browser/device acceptance, and catalog activation. C1 writing `speech`, B2 reading `word_bank`, grouped reading selections, and answer-policy fields need their accepted forward contracts before activation.

## Fresh media recovery attempt

The coordinator retried the official B2 June MP3 URL on 2026-11-12 with an ordinary ranged request for bytes 75497472–75498500, starting at the damaged tail boundary. The server returned **HTTP 403** (`curl` exit 56); no replacement audio bytes were obtained and the original file was not changed. Headers are retained outside the checkout in sibling `recovery-logs/b2-2022-06-tail-range-20261112.headers`. This does not establish whether the official origin file itself is damaged; the received local file remains the only observed damaged artifact.
