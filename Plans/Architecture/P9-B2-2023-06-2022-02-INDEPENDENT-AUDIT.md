# P9 independent source audit: B2 June 2023 and February 2022

2026-11-08. Read-only data acceptance of the frozen drafts in `exams/pl/certyfikat-polski/B2/2023-06/` and `B2/2022-02/`, based on `5cf6515477fd39cccc1eae8d603591b034295553` plus uncommitted draft rows. This receipt does not validate the proposed forward schema fields or activate either paper.

## Source and method

I compared the draft rows with the ignored official `ExamBin` arkusz and key/transcript PDFs identified in [the author evidence](P9-B2-2023-06-2022-02-SOURCE-EVIDENCE.md). The six local PDF/MP3 SHA-256 values match that evidence. I extracted full PDFKit text, read the key pages, inspected representative rendered pages including the February writing photograph, and parsed the `.core-data` rows through `biome read --format json`. This is a data/source audit. MP3 contents and playback were not independently checked.

## Findings requiring correction

| Paper / row | Official source | Expected | Actual draft | Result |
| --- | --- | --- | --- | --- |
| B2/2023-06 `certyfikat-polski-B2-2023-06-listen-1.transcript` | Key/transcript PDF, physical pp. 1–2, task I at item 4 | Spoken transcript proceeds from `Raz-dwa załatwimy sprawę i w drogę.` to `4. -Światowa Organizacja Zdrowia...` | PDF running header `ROZUMIENIE ZE SŁUCHU Transkrypcje TRANSKRYPCJE I KLUCZ DO ZADAŃ` is inserted between those sentences. | **FAIL: source contamination** |
| B2/2022-02 `certyfikat-polski-B2-2022-02-listen-2.transcript` | Key/transcript PDF, physical pp. 1–2, task II | Spoken sentence reads `Alpy Kamnickie myślę, że można śmiało przyrównać...` | The same running header is inserted between `można` and `śmiało`. | **FAIL: source contamination** |

The header is page furniture, not spoken audio. Remove only that inserted header in each transcript; retain the adjacent spoken words and the rest of each transcript. A scan of the other eight listening transcripts found no matching header. This defect affects transcript display and any source-based audio/text comparison, not the scored key values.

## Verified source content

- **PASS — scope and weights:** June listening/reading/grammar have 39/45/75 scored rows; February 38/40/75. All six scored modules total 160 quarter-points. Writing is manual with three alternative two-prompt sets per paper, each requiring both `a` and `b`. Source task maxima and module durations agree with the author evidence table.
- **PASS — answer keys and options:** The scored answer rows checked against the official key pages retain the printed labels, answer variants and grouped weights. Every authored selectable answer is present among its options, and the substantive option text matches the arkusz. June reading I–III and February reading I–III label sequences match the official keys; February reading IV retains its 2/3/1/1/3 grouped answer counts. This check does not assert that arbitrary free-text paraphrases can be scored automatically.
- **PASS — passages and worked examples:** The listening task instructions retain their worked examples and answers; all ten transcripts begin with their recorded worked examples where printed. Reading passages preserve source prose and unscored examples without leaking separately rendered scored option text. In particular, June and February reading I contain `Z tekstu dowiadujemy się, że` as the **unscored worked-example question**. I initially flagged that repeated question phrase as a scored-prompt leak, then checked the physical page and option boundaries; that finding was a false positive. Preserve both examples.
- **PASS — printed token banks:** June grammar II has all 11 occurrences in source order, including crossed example `by`. February grammar IV has all 13 occurrences in source order, including two distinct `na` occurrences, the second being the crossed example; `pośród` and `za` are excess. Source instructions support occurrence presentation, but this audit does not infer an extra global depletion penalty.
- **PASS — writing:** Six prompt texts per paper match contiguous arkusz text and the printed word limits (June 40+260 / 150+150 / 40+260; February 60+240 / 140+160 / 40+260). February writing I/b image crop visually matches the motorboat photograph on arkusz physical p. 25; June writing p. 26 has no image prompt.
- **PASS — advanced source semantics:** June listening IV/6 accepts C and D in either order. June listening V/7 requires any **one** of three distinct invention facts; February listening V/3 requires any **two** of six source facts. February listening III/1 and III/4 have two-person answers C,E and A,E. February reading IV uses source groups of 2/3/1/1/3; this is not evidence of a global bank depletion grading rule.

## Gates and limits

- **FAIL — data source cleanliness:** The two transcript rows above contain nonspoken PDF headers. Recheck these exact spans after author correction, including the full spoken sentence around the February page break.
- **NOT RUN — forward contract:** `requiredSelections` is proposed and not yet accepted by the active schema or evaluator. Neither June V/7 nor February V/3 can be claimed automatically graded from this data receipt. February reading IV's grouped selection/control behavior also needs its contract and UI checks.
- **NOT RUN — media and runtime:** Independent acoustic verification of MP3 segment boundaries, generated Kotlin, clean-checkout asset delivery, browser/device input, scoring, and catalog activation. The author evidence's timestamp windows are candidates, not independently confirmed audio results.

No production source, generated code, catalog, or index was changed in this audit.

Recovery note — 2026-11-12: restored verbatim from the original independent tester tool history after temporary worktree cleanup. Results above are historical; author corrections require a fresh recheck.
