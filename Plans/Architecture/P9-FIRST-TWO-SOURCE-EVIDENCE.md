# P9 first two papers: data draft and source audit

2026-11-08. The rows in `exams/pl/certyfikat-polski/B2/2024-02/` and `C1/2024-04/` are a **data-only draft**. Neither paper is in the catalog or generated Kotlin project yet. The existing B2 exam ID remains `certyfikat-polski-B2-2024-02`; C1 uses the date-qualified `certyfikat-polski-C1-2024-04`. Both are adult B2/C1 sessions in the [official inventory](EXAM-2022-2024-SOURCE-INVENTORY.md): B2 was 4–5 February 2024; C1 was 20–21 April 2024.

## Sources

The [source archive provenance](P9-SOURCE-PROVENANCE.md) contains the official URLs, byte lengths, and full local acquisition record. These are the exact local PDFs and MP3s used here; the binaries stay in ignored `ExamBin` and are **not** part of this draft.

| Paper | Arkusz SHA-256 | Key/transcript SHA-256 | MP3 SHA-256 |
| --- | --- | --- | --- |
| B2/2024-02 | `088d21252d63363af411f1f914547a98afe947b1103f0f6a57e723357dc9407b` | `331a274f18b85721ea88950586848d248b458752e4260bc92ba39cbd5da34f6f` | `25553efda608359ba51883e8afdda4fa8b5ef0dc81ad4f37f835cae2b858ae70` |
| C1/2024-04 | `6fa2fc89b38203ad00f89846c75f88c07504221c1c94e36ff71de8c07a4af6eb` | `ca94d3bb71765a5008fc0285f39d75ef21c7379aa8451efd9dc446ce75ac5c20` | `4741dbbdb11ee04d0ad778d6c73d6314c345e97a737337bbd5b353c504288cbf` |

PDFKit text extraction with physical page markers was used for passages and transcripts. I visually inspected B2 key pages 5–8 and C1 key pages 6–9 as PDF page images. I also inspected B2 arkusz pages 18 and 25 and C1 arkusz pages 15, 22, and 27, which resolve table continuation, crossed bank examples, and writing set structure. This is an authoring audit, not independent reviewer certification of all printed pages.

After the independent [first-two audit](P9-FIRST-TWO-INDEPENDENT-AUDIT.md) identified duplicate scored questions inside `READING_PASSAGE.text`, the reading boundaries were revised: prose rows now contain articles/example context, fragment and two-column choice prose is in `READING_QUESTION.options`, and inline choices in cloze articles are replaced by numbered gap markers. A parsed-row scan found no scored `questionText` duplicated in its passage. The auditor's original finding remains the record of the earlier draft; an independent recheck of this correction is still pending.

## Authored module inventory

Every module has an explicit `EXAM_MODULE` row from its own arkusz cover: source order listening, reading, grammar, writing; duration 40, 60, 60, 90 minutes; maximum 40 points (160 quarter-points) for each. Listening's 40 minutes is printed as **“do 40 minut”**. Writing remains manual assessment; no automatic writing score or pass threshold is authored.

| Paper/module | Arkusz physical pages | Key physical pages | Source task maxima in points | Draft scored row groups | Quarter sum |
| --- | --- | --- | --- | ---: | ---: |
| B2 listening | 2–8 | 1–5 | I 6, II 10, III 8, IV 8, V 8 | 38 | 160 |
| B2 reading | 9–15 | 6 | I 5, II 10, III 10, IV 5, V 10 | 41 | 160 |
| B2 grammar | 16–23 | 7–8 | I–VIII 5 each | 75 | 160 |
| B2 writing | 24–25 | Arkusz 24–25 | Three sets, each two prompts; module 40 | Six prompts | Manual |
| C1 listening | 1–6 | 1–6 | I 7, II 9, III 9, IV 7, V 8 | 38 | 160 |
| C1 reading | 7–16 | 7 | I 8, II 9, III 10, IV 5, V 8 | 53 | 160 |
| C1 grammar | 17–25 | 8–9 | I–VIII 5 each | 75 | 160 |
| C1 writing | 26–27 | Arkusz 26–27 | Sets I/II two prompts, III one; module 40 | Five prompts | Manual |

Source questions can contain more than one scored slot: B2 listening III has six grouped questions and eight slots; B2 reading V has six grouped questions and ten slots (1/3/1/2/1/2). C1 listening II has seven grouped descriptions and nine slots. Their row maxima and `unordered_set` credit match printed slot counts. B2 reading V's visual key confirms D; C,D,E; C; A,C; B; A,E, with arbitrary order for groups 2, 4, 6. C1 reading II's visual three-column key confirms `TAK, BI, NIE, BI, NIE, TAK, TAK, TAK, NIE`.

## Answer and bank details

- B2 listening V has eight source short answers. The key explicitly accepts other transcript-consistent wording, so all eight use `open_manual`; listed complete alternatives are source-backed variants. B2 grammar VIII has five one-point transformations; the key accepts other correct solutions and prints fragment-level annotations, so only complete canonical answers and complete key variants are authored, with other nonblank answers pending manual review.
- B2 grammar II's printed bank contains 13 **occurrences**: `dla na o od nad pod podczas pomimo w w wśród z ze`. Its two `w` occurrences have separate token IDs. `z` is crossed as the example; `dla` and `od` are excess. The bank row keeps every printed occurrence, including the example.
- C1 grammar V's printed bank contains 13 occurrences: `lecz także`, `chociażby`, `nie tylko`, `dopiero`, `a`, `gdzie`, `jeszcze`, `którego`, `mimo wszystko`, `bo przecież`, `właśnie`, `wprawdzie`, `zresztą`. `którego` is crossed as the example; `dopiero` and `wprawdzie` are excess.
- C1 grammar I/8–9 has precisely three positive pair rows from key page 8: `kilku przyjaciółmi`, `kilkoma przyjaciółmi`, `kilkorgiem przyjaciół`, two quarter-points per item. No Cartesian alternatives or inferred partial pair credits are authored. C1 grammar II has five full canonical transformations with `open_manual`, four quarter-points each; no invented substring-based partial answer is authored.
- B2 writing sets I/II/III require respectively 120+180, 150+150, 30+270 words. C1 sets I/II/III require 100+300, 180+220, and **one 400-word essay**. C1's `speech` and `characterization` genre values follow the printed prompts. No III/b row exists.

The listening rows transcribe content from the official key PDF, not ASR. Segment boundaries are the candidate full-recording windows in [the audio boundary audit](P9-AUDIO-BOUNDARY-AUDIT.md): B2 `[109,500]`, `[500,1028]`, `[1028,1591]`, `[1591,1978]`, `[1978,2309]` seconds; C1 `[110,468]`, `[468,978]`, `[978,1436]`, `[1436,1952]`, `[1952,2322]`. Each source MP3 already includes the announced repeat; C1 task I alone is announced once. These candidate boundaries still need independent acoustic review and real-media browser acceptance. Neither paper's printed question pages use a task illustration requiring a separate image asset.

## Checks and remaining gates

- **PASS, syntax/format:** `biome fmt <all 24 owned .core-data files>` then `biome read --format json` for each; all 24 parsed. `biome fmt --check` is the static formatting gate.
- **PASS, draft score arithmetic:** parsed rows sum to 160 quarter-points for listening, reading, and grammar in each paper. Writing source maximum is 40 points, manually assessed.
- **PASS, draft consistency:** a local `biome read --format json` audit found unique row IDs, valid test/task/passage/question references, and option-answer alignment for selectable single-answer rows in both paper directories.
- **NOT RUN:** `biome generate --check --project dsl/biome.project`, because the draft files are deliberately not yet in that project and the P11/P10 contracts/consumers are being integrated separately. No Kotlin, browser, device, clean-checkout media delivery, independent PDF review, or catalog activation is claimed.

Before activation: reconcile new `ExamModuleRow`, reading `answerMode`/`creditPerCorrectQuarter`, C1 grammar pair rows, and `speech`/`characterization` genres with the shared contract; add these exact file paths to the generate project; generate typed Kotlin; independently audit every printed passage, answer, and candidate audio boundary; confirm reproduced MP3 delivery in a clean checkout; run scorer oracles and real-media browser acceptance. The old B2 placeholder task numbers/IDs did not correspond to this official paper, so source-matching task/item IDs replace those stubs while the published exam ID stays stable.
