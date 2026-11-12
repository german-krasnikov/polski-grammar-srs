# P9 first two papers: independent data audit

2026-11-08. Scope: only `B2/2024-02` and `C1/2024-04` under `exams/pl/certyfikat-polski/` in the isolated `/tmp/polski-p9-data-worktree` draft (base `3b218267`). This is a source-data check, not activation or browser acceptance. The adjacent B2/2024-06 and B2/2023-02 drafts were outside this audit.

## Sources and method

I independently hashed the six ignored local binaries. Their SHA-256 values match `P9-FIRST-TWO-SOURCE-EVIDENCE.md` and `P9-SOURCE-PROVENANCE.md`: B2 arkusz `088d2125…`, key `331a274f…`, audio `25553efd…`; C1 arkusz `6fa2fc89…`, key `ca94d3bb…`, audio `4741dbbd…`. I used PDFKit text extraction with physical-page markers, visually inspected B2 key p. 5 and C1 key pp. 6–7, and parsed each draft `.core-data` file with `biome read --format json`. No generated Kotlin or runtime was used as an oracle.

I compared each numbered reading, grammar and listening answer group with the published key tables on B2 physical pp. 5–8 and C1 physical pp. 6–9. For option controls, all authored single answers occur among their authored choices; set answers use only authored labels. All 314 substantive option strings (B2 reading 52, listening 42, grammar 90; C1 reading 88, listening 42; labels shorter than four characters excluded) occur literally in the respective arkusz PDFKit text. The PDFKit string comparison found the full ordered 13-token grammar banks in B2 arkusz p. 18 (`w` twice) and C1 arkusz p. 22, including crossed examples. All eleven `promptText` values match contiguous arkusz text; B2 writing physical pp. 24–25 has three two-prompt sets with 120+180, 150+150, 30+270 words; C1 pp. 26–27 has 100+300, 180+220, then one 400-word essay. No C1 III/b row exists. C1 grammar I/8–9's three authored pairs match key p. 8 exactly: `kilku przyjaciółmi`, `kilkoma przyjaciółmi`, `kilkorgiem przyjaciół`.

For large prose fields I compared consecutive 100-character normalized excerpts with the official PDF text. The initial whole-task draft matched B2 reading 151/153 excerpts and C1 reading 199/205; most exceptions crossed PDF page/column boundaries. After the passage-boundary correction, the articles, examples, and gap text remain in passage rows, while complete printed matching fragments/headings appear in question text or options. For example, B2 reading III and C1 reading IV now keep a short passage title and put their full printed fragment lists in the controls. No scored `questionText` remains inside its passage across 41 B2 and 53 C1 reading rows. This supports preservation of the source prose and removal of duplicate controls; it does not certify layout, images, or every glyph. The authored module order, 40/60/60/90-minute limits and 160-quarter maxima agree with the printed covers and source task maxima. Each scored listening, reading and grammar module sums to 160 quarters; writing remains manual.

## Passage-boundary correction recheck

The previous finding concerned `READING_PASSAGE.text` containing scored questions/options alongside separately authored `READING_QUESTION` rows. Rechecking the revised rows against B2 arkusz physical pp. 9–15 and C1 pp. 7–16 found that the duplicate scored questions are gone. B2 `...read-1` retains its five source news items and C1 `...read-1` retains the housing article and unscored worked example; their five/eight scored questions remain in `...r1-q*` rows. Full matching content remains available through the question controls for B2 task III and C1 task IV. The affected passage-boundary data check is now **PASS**. Rendering these inactive drafts in a browser is still **NOT RUN**.

## Gate status

| Check | Result |
| --- | --- |
| Official-file identity and textual source coverage for these two drafts | PASS, within the PDFKit/visual limits above |
| Numbered answer keys, options, B2 unordered groups, C1 pair constraint, quarter maxima | PASS against the cited physical key pages |
| Three B2 and three C1 writing set prompts/limits | PASS against arkusz pp. 24–25 and 26–27 |
| Reading passage/body boundary for activation | PASS on revised source rows; 94/94 scored question texts absent from passage text, source examples and fragments retained |
| Independent acoustic confirmation of ten candidate audio windows and replay behavior | NOT RUN; `P9-AUDIO-BOUNDARY-AUDIT.md` remains candidate evidence |
| DSL generation, shared scorer/consumer compilation, clean-checkout audio delivery, real browser and device acceptance | NOT RUN; draft is not in `dsl/biome.project` or catalog, and P11 forward fields are not integrated here |

No production data, schema, generator, activation state, or other exam directories were changed by this audit.
