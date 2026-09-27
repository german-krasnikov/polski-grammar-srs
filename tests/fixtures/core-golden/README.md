# Core-golden fixtures (UniversalCorePlan.md §6/§12 UC-04)

Pinned at commit: `a4b4aec` (UC-04, "pack.pairId instead of \"pl-ru\" literals; SkillQueue fixes foreign-skillId crash")

## What this is

`UniversalCorePlan.md` §6 calls out a risk: pl-ru content is still changing, so the
byte-parity gate the generic engine (UC-05..08) needs has to be pinned to one commit
*before* that migration starts, or "byte-identical" becomes a moving target.

These two files are that pin. They are copies of the already-verified
`tests/fixtures/kotlin-parity/{grammar,exercises}.json` — not freshly re-derived data,
because that pair is already proven to equal the current Kotlin engine's output byte for
byte: `GrammarParityTest`/`TrainingParityTest` (`kotlin/shared/src/commonTest/kotlin/polski/{grammar,training}/`)
assert every case in them against `polski.data`/`polski.grammar`/`polski.training` on every
KMP target (JVM, JS, Wasm, iOS simulator, macOS), and those suites are green as of this
commit. Copying them into a directory of their own, separate from `kotlin-parity` (whose
job is the React-oracle comparison, and whose content moves with the React baseline),
freezes this exact snapshot for the *next* gate instead.

## Coverage

- `grammar.json` (1979 cases): every noun/adjective/verb/pronoun/possessive form
  (`G-NOUN-*`, `G-ADJ-*`, `G-VERB-*`, `G-PRON-*`, `G-POSS-*` — 490 possessive-form cases,
  the full `PossessiveId(7) × Gender(5) × NumberGram(2) × GramCase(7)` cartesian), plus
  full-sentence/phrase composition (`G-PHRASE-*`, `G-SENTENCE-*` — the "matrix" shape: a
  noun+adjective+owner+number realized across all 7 cases) and skill metadata (`S-*`, all
  16 pl-ru skills).
- `exercises.json` (112 cases): one generated `Exercise` per skill × representative seed
  (`E-*`) and the 5-step sentence chain (`C-*`), covering `ExerciseFactory.generateForSkill`/
  `generateChain` — the "exercise" shape UC-07/08's `ExerciseGenerator` must reproduce.

Both carry `sourceRevision: df59774e153a5bdf590fe27bd8780c7bd5457a26` internally (the
commit their content was generated from); that predates this pin and is unrelated to the
`<commit>` above, which is when they were frozen into *this* directory.

## How UC-05..08 uses this

The generic engine (`ConstructionRealizer`/`TemplateInterpreter`/`ExerciseGenerator` in
`:core-engine`) runs side by side with today's `GrammarEngine`/`ExerciseFactory` on the same
inputs recorded here and diffs its output against the `expected` field of every case —
byte-for-byte, per `UniversalCorePlan.md` §5.3's parity gate. A case that no longer matches
is a regression in the new engine, not a fixture update; these files do not change again
until the generic engine has fully replaced the old one (UC-08) and a new pin is deliberately
taken.
