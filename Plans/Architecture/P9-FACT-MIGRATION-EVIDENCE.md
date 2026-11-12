# P9 source fact migration

2026-11-12. The nineteen `open_manual` unordered listening questions in the eight affected inactive P9 papers now have explicit `ListeningAcceptedFactRow` identities. Key alternatives that name the same fact use `ListeningAcceptedFactAliasRow`; aliases carry no additional credit. The authored `correctAnswer` strings and question IDs remain unchanged. Each source directory was checked against the official key PDF in the main checkout under `ExamBin/pl/` and the pinned PDF inventory in [P9 source provenance](P9-SOURCE-PROVENANCE.md). These are source and contract checks, not publication or audio acceptance.

| Paper | Questions | Key PDF page | Selection and credit read from key |
| --- | --- | --- | --- |
| B2/2022-02 | V/3 | 5 | Any two of six; 2 quarters per fact. The key's longer descriptions and private photograph wording are aliases. |
| B2/2022-06 | V/3, V/8 | 5 | Two location/standard facts at 4 quarters each; two appliance facts at 2 quarters each. Printed standard adjectives and accusative appliance forms are aliases. |
| B2/2023-02 | V/3, V/8 | 5 | Two facts per answer, 2 quarters per fact. |
| B2/2023-06 | V/3, V/6, V/7 | 5 | Two geography and two medium facts at 2 quarters each; any one of three inventions at 4 quarters. Printed prepositional and expanded invention forms are aliases. |
| B2/2024-06 | V/1, V/3 | 5 | Two facilities at 4 quarters each; any two of three room features at 2 quarters each. Printed shorter and alternate wording is aliased. |
| C1/2022-11 | V/3 | 5 | Two facts at 2 quarters each; `skarbu`, `zamku niedzickiego`, and `zamku w Niedzicy` share one fact ID. The redundant whole-pipe variants were removed. |
| C1/2023-04 | V/2, V/4, V/5, V/7 | 6 | Two facts per answer; V/4 is 4 quarters per fact and the others 2. The key says numeric values in parentheses are optional; the unnumbered debris wording is an alias for V/4's second fact. |
| C1/2023-11 | V/1, V/2, V/7, V/8 | 6 | V/1 and V/2 require two of four facts, V/7 two of three, V/8 both facts; 2 quarters per fact. Slash alternatives in the key are aliases inside one fact, not additional selectable facts. |

Where the original draft omitted `requiredSelections`, the key's a/b structure or explicit any-one/any-two instruction now supplies it. Source validation checks quarter arithmetic, alias collisions, and presence of fact rows for source sets. The unchanged published six retain their legacy no-metadata route.

B2/2022-06 still has no defensible audio task endpoints because the received MP3 is damaged; `segmentStartMs`/`segmentEndMs` remain absent. Its Reading V `word_bank` source tag is now declared in the contract, but full-paper data validation cannot pass until audio is recovered and the remaining source/media gates are met. No task window was invented.
