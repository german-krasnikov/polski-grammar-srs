# P9 C1 2023 papers: data draft and source audit

2026-11-08. `exams/pl/certyfikat-polski/C1/2023-04/` and `C1/2023-11/` are **data-only drafts**. The stable date-qualified IDs are `certyfikat-polski-C1-2023-04` and `certyfikat-polski-C1-2023-11`. These were the official adult C1 sessions of 15–16 April and 18–19 November 2023 in the [source inventory](EXAM-2022-2024-SOURCE-INVENTORY.md). Neither paper is in the active Biome project, generated Kotlin, or catalog.

## Source provenance

The [provenance inventory](P9-SOURCE-PROVENANCE.md) records the official download URLs and byte sizes. I used the exact ignored `ExamBin` PDFs and MP3s; the binaries are outside the draft diff.

| Paper | Arkusz SHA-256 | Key/transcript SHA-256 | MP3 SHA-256 |
| --- | --- | --- | --- |
| C1/2023-04 | `331e7ea00c95710027181454f9aefbf1441f2c786564bb6c27736dd8a8855a12` | `0c90381c01b658141be4009dc7b81e4eb8806e93d2acebe79fe049d8f15592a7` | `7836d6d038029c2eebfa4491b9bcd3b1dc7fea83784189dc88c33196072afd28` |
| C1/2023-11 | `e68f923ea594067c2d5b41bd9a0e7e51748b95aaf8dd5e71ec07495b3bdc332b` | `77217b89ec083d8ecef025561f3fdffd9921a926b5452475323ff37ee0b18c11` | `920624ab06c0585ec5a584523e0ec5ae7eba524ded1561583f2363c2c3a566ef` |

PDFKit page text supplied printed wording and transcripts; rendered page images resolved the columns, underlining, and grouped credits. I visually inspected both key PDFs' physical pages 5–9, both arkusz grammar-bank pages 23, and both writing pages 28. This is an authoring audit, not independent certification of all pages. Transcript rows are from official key PDFs, not ASR; the full April and November task V transcripts continue on key physical page 5 and are included. Page headers, footers, citations, and key tables are excluded from transcript content.

## Module and item inventory

Each paper has four explicit source-order `EXAM_MODULE` rows: listening, reading, grammar, writing; durations 40, 60, 60, 90 minutes and maxima 160 quarter-points each. Listening's cover says **“do 40 minut”**. Writing is manual assessment, with sets I/II requiring both prompts and set III one prompt.

| Paper/module | Arkusz physical pages | Key physical pages | Printed task maxima, points | Scored row groups | Quarter sum |
| --- | --- | --- | --- | ---: | ---: |
| April listening | 3–7 | 1–6 | I 6, II 8, III 7, IV 9, V 10 | 37 | 160 |
| April reading | 9–17 | 7 | I 10, II 5, III 10, IV 10, V 5 | 50 | 160 |
| April grammar | 19–26 | 8–9 | I–VIII 5 each | 75 | 160 |
| April writing | 28 | Arkusz 28 | I 80+320, II 200+200, III one 400-word essay; module 40 | Five prompts | Manual |
| November listening | 3–7 | 1–6 | I 8, II 8, III 10, IV 6, V 8 | 35 | 160 |
| November reading | 9–17 | 7 | I 7, II 8, III 10, IV 10, V 5 | 43 | 160 |
| November grammar | 19–26 | 8–9 | I–VIII 5 each | 75 | 160 |
| November writing | 28 | Arkusz 28 | I 80+320, II 200+200, III one 400-word argument; module 40 | Five prompts | Manual |

April listening IV has seven labeled descriptions and nine scored slots: E and F each take two person numbers, in source order; no unsupported cross-product of choices is authored. April listening V has nine question groups but ten points: item 4 has two **one-point** answers, while items 2, 5, and 7 have two **half-point** answers. The key says optional numeric precision and accepts other transcript-consistent answers. Complete canonical responses and selected full printed alternatives are preserved with `open_manual`; no substring-based partial credit is invented. November listening IV asks for exactly six true statements out of twelve (paper p6, key p5: `2, 5, 6, 8, 9, 12`), in arbitrary order, one point each. Its grouped row carries the full printed statement list in the task instruction, six correct IDs, and six quarter-credit units of four.

November listening V/1 and V/2 each request **two** facts from a larger printed key pool; V/7 requests **two** audience examples from three key facts (paper p7, key p6). Those rows preserve every printed accepted fact and carry `requiredSelections: 2`. This is a **proposed forward field**, not an accepted contract field; contract validation and activation are **blocked** until the scorer distinguishes accepted pool size from required selections and handles synonyms within a fact. November listening IV also has a proposed `requiredSelections: 6`. Other transcript-consistent short answers remain manual. November reading IV (paper pp15–16, key p7) has eight topic groups worth ten points: items 2 and 6 require two fragment letters each; the source explicitly permits a topic in several fragments. Their rows carry unordered two-letter answers and four quarter-points per letter.

Both C1 grammar II tests contain five one-point sentence transformations. The visually checked keys distribute four quarter-points across transformations and accept other valid solutions with proportional partial credit; the draft stores complete canonical sentences and complete printed variants under `open_manual`, without claiming that the current scorer implements the key's free-form partial-credit rubric. April grammar V's 13 printed bank occurrences are `gdyby | chyba że | który | przeciwnie | wprawdzie | chociaż | gdy | jeszcze | aż | lecz wkrótce | zamiast | wręcz | by`; `który` is the crossed example, `wprawdzie` and `chyba że` are excess. November grammar V's 13 occurrences are `co najmniej | by | których | słowem | niż | bowiem | jakoby | co | tymczasem | dopiero | w tym także | zamiast | to jednak`; `co najmniej` is crossed as example, `zamiast` and `których` are excess. Each printed occurrence has its own bank-token row; no global word-depletion penalty is invented.

The reading passages keep the article body and unscored example. Scored prompts are only in `READING_QUESTION`; full printed A–K/A–L/A–M fragment prose and April II column prose reside in control options. April II's example heading is `Ekscentryczne lokum → D`; November IV's example is retained beside the five source fragments. A parsed scan found no scored `questionText` duplicated in its passage. Neither writing page contains an image prompt requiring an asset.

## Audio and verification boundary

The [audio boundary audit](P9-AUDIO-BOUNDARY-AUDIT.md) records candidate full-recording windows in seconds: April `[110,467]`, `[467,841]`, `[841,1372]`, `[1372,1934]`, `[1934,2365]`; November `[110,478]`, `[478,809]`, `[809,1401]`, `[1401,1760]`, `[1760,2152]`. Each source MP3 includes its announced repeats; task I is announced once. The windows still require independent human acoustic and real-media browser review.

- **PASS, authoring format/parse:** `biome fmt` and `biome read --format json` on the 24 owned `.core-data` files. A final `biome fmt --check` is recorded separately in the handoff.
- **PASS, local structural audit:** distinct row IDs except shared writing set IDs, references, source task counts, and 160 quarter-points in each scored module. All substantive selectable option values were compared to normalized official PDF text; no nonliteral option was found.
- **NOT RUN:** `biome generate --check --project dsl/biome.project`, Kotlin compile, clean-checkout media delivery, browser and device acceptance, independent PDF and acoustic review, and catalog activation. The drafts are outside the active project. Proposed `requiredSelections` rows cannot be called contract-valid until the schema, scorer, and tests are accepted.

Recovery note — 2026-11-12: restored the source-authored data and this historical evidence from the original agent history. Current `biome fmt --check` and `biome read --format json` passed for all 48 recovered `.core-data` files; contract and runtime gates remain NOT RUN.
