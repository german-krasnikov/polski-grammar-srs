# P11 certified written module covers

Checked 2026-11-08 against local official `arkusz.pdf` files under ignored `ExamBin/pl/`. The four cover pages in each row were rendered through PDFKit and visually inspected. PDFKit text extraction was used to read the printed times and maxima on those pages and the immediately following module pages. The values below were authored as `ExamModuleRow` in the corresponding `exams/pl/certyfikat-polski/<level>/<year>/modules.core-data`; there is no level-wide timing default. Listening, reading and grammar maximums also equal their authored item-quarter sums; writing maximum comes from the cover, not prompt count. Speaking is excluded.

| Sitting | PDF SHA-256 | Cover physical pages (L/R/G/W) | Time minutes (L/R/G/W) | Maximum points each |
| --- | --- | --- | --- | ---: |
| B1/2017 | `cd176f129cf8feaa13ee25844d5adac9bb2e59df3a26e9f937f795c699897851` | 2/8/15/21 | 25/45/45/75 | 30 |
| B1/2019 | `784a569a318778449fe5aac1bcd6f9934017550ad9c18c50c1b386855713056e` | 2/8/15/21 | 25/45/45/75 | 30 |
| B1/2020 | `42f1801bf9661842ef0211ae4d796c36c65c1bed082ef1b23527a69e02f72521` | 2/8/15/21 | 30/40/45/75 | 30 |
| B2/2017 | `2eff6451832e1ef3e1f1e2cc3c177693a9a4fb49d7020b7f23bbfa37d20796b6` | 2/7/14/21 | 30/60/60/90 | 40 |
| B2/2019 | `4c210043516fafc255872c570fdd600b43fd507fb021ad7f232b4a374ab3acb2` | 2/9/17/25 | 30/60/60/90 | 40 |
| B2/2020 | `ff786439979e4bb935b3b228c27788e0ca43537f1bf12a5d3eec7715493d409c` | 2/9/17/25 | 40/60/60/90 | 40 |

The independent `dsl/exam-scoring/module-covers.core-data` oracle records all 24 values. `ExamSourceModuleTest` compares generated module rows against it and checks four distinct ordered parts per sitting. `dsl/exam-scoring/reading-sets.core-data` records the separate B2/2024-02 Reading V grouped key from [source form findings](P9-SOURCE-FORM-FINDINGS.md); this oracle does not activate that paper in the browser catalog. P11 timed entry is gated by a complete generated configuration, mapped parts and valid audio segment bounds. Device and independent tester/reviewer gates remain separate from these source checks.
