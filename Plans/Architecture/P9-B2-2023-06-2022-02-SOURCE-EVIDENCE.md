# P9 B2 June 2023 and February 2022: source data draft

2026-11-08. The official adult B2 sessions dated 24–25 June 2023 and 6–7 February 2022 are drafted under `exams/pl/certyfikat-polski/B2/2023-06/` and `B2/2022-02/`, with stable IDs `certyfikat-polski-B2-2023-06` and `certyfikat-polski-B2-2022-02`. These rows are data only. Neither paper is in the active Biome project, generated Kotlin, or exam catalog.

## Provenance and method

The ignored local `ExamBin` copies are byte-identical to the [source provenance inventory](P9-SOURCE-PROVENANCE.md), which gives each official download URL and byte size. The source PDFs and MP3s are not part of this diff.

| Paper | Arkusz SHA-256 | Key/transcript SHA-256 | MP3 SHA-256 |
| --- | --- | --- | --- |
| B2/2023-06 | `afce8d9bec871c4a6d8cd21a732ee442823a258e1835a713dcd85ed708f6f91a` | `99dc89bae16305e4d35c2541749b628088a70d82451ea1f85ead44f60eff7a2b` | `bd5dfe1c3ebfe65a77974df59ac019f14919aad201f7daa3d1a364d51198791a` |
| B2/2022-02 | `d0edb5738913bc437fa472ac07a4050c1ef63c42bc257c92f954e8ab658ad375` | `8e7b94e446f245ba721d5d4686a4884bbe33322df7a50ce0e25a6a69e280d272` | `f7d5ccc4f4bc023b07125d515b2d0a2ebe6bf502b573c1c91994b556b042e8ad` |

I read PDFKit text for all source task pages and official transcripts, then rendered key pages for grouped answers and grammar alternatives. In particular, I visually checked June key physical pp. 5–8, February key pp. 4–8, June arkusz p. 19 and February arkusz p. 19 for the printed word banks, and February arkusz p. 25 for the photograph. This is an authoring check; independent PDF/media review remains outstanding. The transcript fields contain the official key text, including the example and the complete task recording, with PDF source attributions removed. ASR supplied no transcript wording.

After the [independent source audit](P9-B2-2023-06-2022-02-INDEPENDENT-AUDIT.md), I removed one nonspoken PDF running header from June listening I and one from February listening II (key/transcript physical pp. 1–2 in each PDF). June now runs directly from `Raz-dwa załatwimy sprawę i w drogę.` to item `4. -Światowa Organizacja Zdrowia...`; February now reads `Alpy Kamnickie myślę, że można śmiało przyrównać...`. Only those two transcript fields changed in this correction. The official neighboring speech, task instructions, answers, and credits remain intact; `biome fmt --check` and `biome read --format json` pass on both touched task files.

## Module and question inventory

Each paper has four source-order `EXAM_MODULE` rows, with cover durations listening **up to** 40 minutes, reading 60 minutes, grammar 60 minutes, writing 90 minutes. Each module maximum is 40 points, represented as 160 quarter-points. Writing has manual assessment and three alternative two-prompt sets; the chosen set requires both `a` and `b`.

| Paper/module | Arkusz physical pages | Key physical pages | Printed task maxima, points | Scored rows | Quarter sum |
| --- | --- | --- | --- | ---: | ---: |
| June listening | 3–8 | 1–5 | I 8, II 6, III 9, IV 9, V 8 | 39 | 160 |
| June reading | 10–16 | 6 | I 5, II 10, III 10, IV 10, V 5 | 45 | 160 |
| June grammar | 18–24 | 7–8 | I–VIII 5 each | 75 | 160 |
| June writing | 26 | Arkusz 26 | I 40+260, II 150+150, III 40+260 words | 6 prompts | Manual |
| February listening | 3–7 | 1–5 | I 6, II 8, III 10, IV 8, V 8 | 38 | 160 |
| February reading | 9–15 | 6 | I 5, II 10, III 10, IV 10, V 5 | 40 | 160 |
| February grammar | 17–23 | 7–8 | I–VIII 5 each | 75 | 160 |
| February writing | 25 | Arkusz 25 | I 60+240, II 140+160, III 40+260 words | 6 prompts | Manual |

June listening IV/6 (arkusz p. 7, key p. 5) has two correct venue labels, `C` and `D`, either order, one point each. February listening III/1 and III/4 (arkusz p. 5, key p. 5) each have two person labels, `C,E` and `A,E`, either order. February reading IV (arkusz pp. 13–14, key p. 6) has five cartoon-description groups with answer counts **2/3/1/1/3** and ten one-point matches total; its first, second, and fifth groups retain all labels as unordered sets. June and February reading passages exclude the separately rendered scored prompts/options, while keeping article prose, unscored examples, and necessary bank/fragment content in passage or control fields.

June grammar II (arkusz p. 19) has eleven printed bank occurrences in order: `a | ale | by | chociaż | czy | gdyż | iż | jak i | jeśli | które | zarówno`. `by` is the crossed example; no excess word is printed. February grammar IV (arkusz p. 19) has thirteen: `dzięki | na | na | nad | o | pośród | po | podczas | przez | u | w | z | za`. The second `na` is the crossed example, and `pośród` and `za` are excess according to key p. 7. Repeated occurrences have distinct token IDs; the source's one-use instruction does not justify a new automatic global penalty.

June grammar VIII's key (physical p. 8) gives complete alternative transformations and explicit quarter/half-point components, including item 5's two half-point variants. The draft records only complete printed answers and variants under `open_manual`; it does not infer substring or free-paraphrase credit. February grammar VIII key p. 8 likewise accepts other correct full transformations, so the five canonical sentences remain manual. The keyed alternatives in other grammar tasks are stored as full answer variants.

## Source-specific open-answer constraints

- June listening V/7, arkusz p. 8/key p. 5: **one** invention suffices. The official key accepts three distinct invention types: photographic paper, devices measuring color intensity, or a bulletproof vest. Its printed aliases include `papier fotograficzny czuły pod względem barw` and `urządzenia do pomiaru intensywności barw/kolorów`. The row retains the three canonical facts with proposed `requiredSelections: 1`, `answerMode: unordered_set`, and `open_manual`.
- February listening V/3, arkusz p. 7/key p. 5: **any two** from descriptions, photographs, biographical films, documentary films, reports, and playlists. The key's longer forms include `bogate opisy` and `fotografie z prywatnych zbiorów rodziny kompozytora`. The row retains six canonical facts with proposed `requiredSelections: 2`, `answerMode: unordered_set`, and `open_manual`.
- June listening V/3 and V/6 require two half-point facts; February listening V/3 requires two half-point facts. Short answers otherwise follow the full official key and selected complete printed variants. The key permits other transcript-consistent responses, so these responses remain manual.

`requiredSelections` is a **proposed forward field**, not an accepted contract field. Contract validation and activation of these rows are **blocked** until the schema and scorer distinguish accepted pool size from required answer count, group synonyms into facts, and award source credit without counting repeated aliases twice. `open_manual` does not establish automatic evaluation of unseen paraphrases.

February writing I/b uses the exact photograph cropped from arkusz physical p. 25 at `kotlin/composeApp/src/webMain/resources/exam-images/B2-2022-02-writing-I-b.png` (SHA-256 `7a626738f71817d0fca1beb35b191009244700f187b5cfae180ceacbad7cf7b0`). The crop was visually inspected: two people in a motorboat on a lake. June writing p. 26 contains no image prompt. All six prompts in each paper preserve the source genres and printed word counts.

## Audio and verification boundary

The [audio boundary audit](P9-AUDIO-BOUNDARY-AUDIT.md) records candidate full-recording windows in seconds: June `[109,728]`, `[728,1085]`, `[1085,1429]`, `[1429,1869]`, `[1869,2249]`; February `[107,448]`, `[448,769]`, `[769,1241]`, `[1241,1609]`, `[1609,1923]`. The MP3s include announced repeats; task I is announced once. These windows still need independent acoustic and real-media browser review.

- **PASS, authoring syntax:** `biome fmt` and `biome read --format json` on the 24 owned `.core-data` files.
- **PASS, local structural check:** unique row IDs, 39/45/75 and 38/40/75 listening/reading/grammar row counts, and 160 quarter-points in each scored module. Keyed selectable answers belong to their authored option sets. All substantive selectable option values occur in normalized official arkusz text; this is a source-text check, not an independent page audit.
- **NOT RUN:** active `biome generate --check --project dsl/biome.project`, Kotlin compilation, clean-checkout media delivery, browser/device acceptance, independent source and acoustic review, and catalog activation. The drafts are outside the active project, and proposed selection-count rows cannot be called contract-valid.

Recovery note — 2026-11-12: restored this historical authoring record and the corresponding source edits from the original agent history. Current format and parse checks passed; historical independent findings require fresh recheck after corrections.
