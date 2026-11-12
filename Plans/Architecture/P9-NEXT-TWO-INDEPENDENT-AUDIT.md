# P9 next two B2 papers: independent data audit

2026-11-08. Scope: data-only drafts `B2/2024-06` and `B2/2023-02` in `/tmp/polski-p9-data-worktree`, base `3b218267`. Neither paper is generated or catalog-active. I changed only this audit document; the source author retains the `.core-data` and image assets.

## Source identity and checks

I independently hashed the six local ignored official assets. Their full SHA-256 values match `P9-NEXT-TWO-SOURCE-EVIDENCE.md` and `P9-SOURCE-PROVENANCE.md`: June arkusz `6c22f42a…`, key `2b244b37…`, MP3 `c09eb89e…`; February arkusz `0f307da5…`, key `e0287697…`, MP3 `ff699635…`. I extracted PDF text with PDFKit physical-page markers, inspected rendered June key p. 6 and February key p. 6 for TAK/NIE/BI columns, and visually compared both writing image crops with arkusz June p. 23 and February p. 26. I parsed rows using `biome read --format json`; no production mapper or generated Kotlin was used to derive expected answers.

Both papers author listening, reading, grammar, writing in printed order, at 40/60/60/90 minutes and 160 quarters per module. The cover says listening **up to** 40 minutes. I compared the indexed row answers with the official listening key pp. 4–5, reading key p. 6, and grammar key pp. 7–8 of each paper. In both drafts, listening, reading and grammar each sum to exactly 160 quarters, with answer values aligned to authored options. June has 37/45/75 scored rows and February has 36/45/75; grouped unordered-set answers explain row counts below printed slot counts. All B2 June listening/grammar substantive options (42/86) and B2 February listening/grammar options (39/87) occur literally in the corresponding arkusz text. The 77 unique June and 49 unique February substantive reading option texts also occur in arkusz after removing their added `A.`/`B.` label prefixes; PDF labels and layout account for the prefix mismatch. No scored question text is embedded in a reading passage row.

The printed June grammar III bank matches the 13 authored occurrences in order, including two `do` and two `w`; the first `do` is the worked example (arkusz p. 17, key p. 7). February grammar II matches all 11 printed occurrences, including worked `kiedy` (arkusz p. 19, key p. 7). February reading V has all 14 printed tokens in the ten question option lists, including worked `świadomie` and the three key-listed excess words `ani`, `osobno`, `przeciwdziała` (arkusz p. 16, key p. 6). Its printed instruction says some words are unnecessary; it does **not** establish a separate global depletion penalty. A task-level selectable bank is a presentation/contract issue; no further grading restriction should be inferred without source evidence.

June listening V/3 is printed as two required answers chosen from three acceptable facts (key p. 5): spacious rooms, bright rooms, new equipment. The draft preserves all three, `requiredSelections: 2`, and two quarters per correct fact. February listening IV key p. 5 gives A/B/F one label, C three, D/E two, in any order; February listening V/3 and V/8 each require two facts at half a point apiece. June listening III has two two-label groups. The authored source answers and quarter sums reflect those printed groups. B2 June and February grammar VIII and open listening responses preserve complete key alternatives and pending manual grading for unlisted wording; this audit did not execute a scorer.

Writing matches the source set structure: June 60+240, 150+150, 30+270 words; February 30+270, 100+200, 50+250 (both arkusz writing pages cited above). The two source photographs match the retained PNG crops: June two planes `5716c5b0…`, February musicians beside water `cd502aa9…`. Nine of twelve prompt strings match contiguous PDFKit text exactly. The three exceptions retain the beginning and end of the printed prompt: June II/a adds a period after the portal name relative to PDFKit text, June III/b omits the intervening printed citation URL from the prompt, and February III/b differs only in PDF-extracted punctuation spacing. The task requests and word limits remain present; these are minor transcription/attribution details to reconcile before publication, not changed answer keys.

## Contract and acceptance boundary

`requiredSelections: 2` for June listening V/3 is a proposed field. Until the shared schema, evaluator and browser control distinguish the three accepted facts from two required submissions, the correct 0/1/2-fact quarter outcomes are **NOT RUN** and catalog activation remains open. February reading V currently repeats its 14-word bank as options on each `fill_gap` row. The source tokens and keys are present, but a task-level word-bank control, selected occurrence handling and display of worked/excess words are not verified by this data audit. The printed instruction alone does not justify a task-wide reuse prohibition.

The ten audio segment boundaries in `P9-AUDIO-BOUNDARY-AUDIT.md` are candidates. I verified the source MP3 hashes, not their cut points, announced repeats or playback in a browser. The two inactive papers have no generated Kotlin, clean-checkout audio delivery, scorer oracle, browser run or device evidence here.

| Required check | Status |
| --- | --- |
| Official PDF/key/MP3 identity; four-module source inventory | PASS for these two drafts |
| Indexed answers, substantive options, banks, grouped cardinality as authored, quarter arithmetic | PASS against cited source/key pages; runtime scoring NOT RUN |
| Writing prompts, limits and image source identity | PASS for content/images; three minor punctuation/attribution variances noted |
| Reading passage/control separation | PASS in source rows; browser rendering NOT RUN |
| June any-two-of-three evaluator and February task-level reading bank | NOT RUN, forward contract/consumer work |
| Acoustic cuts, generation, Kotlin/browser/device integration, catalog activation | NOT RUN |

No production source, schema, image, generator, catalog, or release state was modified by this audit.
