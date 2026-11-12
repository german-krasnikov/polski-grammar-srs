# P9 next two B2 papers: data draft and source audit

2026-11-08. The rows in `exams/pl/certyfikat-polski/B2/2024-06/` and `B2/2023-02/` are **data-only drafts**. They are not in `dsl/biome.project`, the generated Kotlin data, or the exam catalog. IDs are date-qualified: `certyfikat-polski-B2-2024-06` and `certyfikat-polski-B2-2023-02`. The official sessions were 22–23 June 2024 and 5–6 February 2023, respectively, per the [source inventory](EXAM-2022-2024-SOURCE-INVENTORY.md).

## Source provenance and method

The [provenance inventory](P9-SOURCE-PROVENANCE.md) records the official URLs, byte sizes, and acquisition history. These ignored local binaries in `ExamBin` supplied every row; they are not committed.

| Paper | Arkusz SHA-256 | Key/transcript SHA-256 | MP3 SHA-256 |
| --- | --- | --- | --- |
| B2/2024-06 | `6c22f42ad9216c6990c1a95f071d3bfd162fc674a3f7fa07d05cd44b2c697866` | `2b244b375866a6e23744509b6f988f1ff2312e877e870528382d6e03c5de4574` | `c09eb89ec3a8db76d0c4e96418d164c74aa376aa7415a7749e534023bb546a32` |
| B2/2023-02 | `0f307da5ab5060f72103918eaa3a4fb4398fb23556bede0bda0db33348f24f54` | `e0287697990323a64e5ad80189c56287268cd10d9893c09df48036e9a6042b75` | `ff699635fba80bcaeb1b2febb6d98a70e245d17d8ab0cb4fbe2ad25e86fe6049` |

PDFKit page text supplied the wording and transcript; rendered source pages resolved tables, underlining, bank duplicates, and photos. I visually inspected the June key physical pages 5–8 and February key pages 4–8, June arkusz pages 17 and 23, and February arkusz pages 16, 19, and 26. These are authoring checks, not independent certification of every page. Transcripts are copied from the official key PDF, not ASR. The reading passages now contain articles and examples, while scored prompts and full printed A–K/column choice prose reside in `READING_QUESTION` controls. A parsed-row scan finds no scored `questionText` inside its passage in either draft.

## Module inventory

Each paper has explicit source-order `EXAM_MODULE` rows: listening, reading, grammar, writing; 40, 60, 60, 90 minutes and 40 points (160 quarter-points) per module. Listening is printed as **“do 40 minut”**. Writing is manually assessed; six prompts are three two-prompt sets, not six simultaneous tasks.

| Paper/module | Arkusz physical pages | Key physical pages | Printed task maxima, points | Scored row groups | Quarter sum |
| --- | --- | --- | --- | ---: | ---: |
| June listening | 3–7 | 1–5 | I 6, II 8, III 10, IV 8, V 8 | 37 | 160 |
| June reading | 9–14 | 6 | I 5, II 10, III 10, IV 10, V 5 | 45 | 160 |
| June grammar | 16–21 | 7–8 | I–VIII 5 each | 75 | 160 |
| June writing | 23 | Arkusz 23 | Three sets of two prompts, module 40 | Six prompts | Manual |
| February listening | 3–8 | 1–5 | I 6, II 7, III 9, IV 10, V 8 | 36 | 160 |
| February reading | 10–16 | 6 | I 5, II 10, III 10, IV 10, V 5 | 45 | 160 |
| February grammar | 18–24 | 7–8 | I–VIII 5 each | 75 | 160 |
| February writing | 26 | Arkusz 26 | Three sets of two prompts, module 40 | Six prompts | Manual |

The June listening III source has eight grouped descriptions and ten scored answers: groups 2 and 8 each take two answers, with arbitrary order, at one point per selected answer. The February listening IV source has six grouped descriptions and ten scored answers: C takes three, D and E take two, with arbitrary order. Both keys were checked as page images. Other quarter-point totals reflect half-point slots where printed. The key's `TAK/NIE/BI` columns were read visually, not inferred from PDF text column order.

## Answer forms, banks, and image prompts

- June listening V/1 accepts two distinct facility answers, four quarter-points each. June V/3 asks **two of three** facts: large/spacious rooms, bright rooms, new equipment. The key page 5 states “Wystarczy wymienić dwie z wymienionych odpowiedzi.” The draft preserves all three accepted facts, two required selections, and half a point per fact. `requiredSelections: 2` is a **proposed forward field**, not an accepted contract field yet. Contract validation and activation remain **blocked** until the schema and scorer distinguish accepted answer pool size from required selections; treating the pool of three as a three-answer requirement or truncating it to two would change the official rule. Other transcript-consistent short answers remain `open_manual` without invented paraphrase auto-credit.
- February listening V/3 and V/8 each ask two answers worth half a point each. The key page 5 explicitly accepts other transcript-consistent answers. Complete printed alternatives are in variant rows; other wording remains manual review. June and February grammar VIII transformations likewise use complete canonical answers and source-backed full variants with `open_manual`, not substring or partial-answer grading.
- June grammar III's printed bank has **13 occurrences**: `bez do do dzięki o od po pod pomiędzy przeciwko w w za`. The first `do` is the crossed example; both `do` and both `w` occurrences have distinct token IDs. The instruction says two words are excess. February grammar II's bank has **11 occurrences**: `a chociaż czyli dlatego gdyby jeżeli kiedy lub ponieważ że żeby`; `kiedy` is crossed as example. February reading V's 14 printed tokens are `ani lecz lęk obrazowo osobno pomaga przeciwdziała przekaz razem sprzyja świadomie uwzględnia współpraca zawodowo`; `świadomie` is crossed as example, and `ani`, `osobno`, `przeciwdziała` are excess. Its full bank is preserved in control options, but the current reading contract has no typed word-bank control or once-only usage policy, so that interaction needs a contract decision before activation.
- June writing sets require 60+240, 150+150, 30+270 words; February sets require 30+270, 100+200, 50+250 words. The two source photographs were cropped from the respective official arkusz writing pages and saved as `kotlin/composeApp/src/webMain/resources/exam-images/B2-2024-06-writing-I-b.png` (two planes; SHA-256 `5716c5b0ff9da2b28be0351a28e6bb0856fc29fb8aad61ff100308203fc5012c`) and `B2-2023-02-writing-I-b.png` (musicians by water; SHA-256 `cd502aa99c6852ed36305755dfa791cdb8a0055e78bc3356bff60b7f98aead46`). Both crops were visually inspected. No other task image assets were needed.

## Audio and verification boundary

The [audio boundary audit](P9-AUDIO-BOUNDARY-AUDIT.md) records candidate full-recording windows in seconds: June `[109,572]`, `[572,835]`, `[835,1376]`, `[1376,1740]`, `[1740,2093]`; February `[109,496]`, `[496,892]`, `[892,1189]`, `[1189,1635]`, `[1635,1954]`. The source audio contains its announced repeats. These are candidate cuts only; independent human acoustic and real-media browser checks are **NOT RUN**.

- **PASS, format and parse:** `biome fmt` on the owned `.core-data` files; `biome read --format json` on each. A final `biome fmt --check` is the formatting gate.
- **PASS, draft structure:** parsed IDs/references, choice/answer alignment, per-module row counts, and 160-quarter sums were checked locally. Normalized selectable option values were compared with official PDF text; no nonliteral substantive option was found. Reading passage/control duplication scan passed after boundary repair.
- **NOT RUN:** `biome generate --check --project dsl/biome.project`, Kotlin compilation, browser and device acceptance, independent PDF and acoustic review, clean-checkout MP3 delivery, and catalog activation. These drafts are intentionally outside the active project pending the P10/P11 contract and scorer gates, including the June any-two-of-three answer rule and February reading word-bank interaction.
