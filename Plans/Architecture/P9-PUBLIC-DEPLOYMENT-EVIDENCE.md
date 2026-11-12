# Published sixteen-paper verification — 2026-11-12

Production runtime revision: `a753640c2643b3ff8e399d3772588fc0536ac4fa`, merged from the independently tested publication/native branches and pushed to `main`. [Published application](https://german-krasnikov.github.io/polski-grammar-srs/).

| Gate | Observed result |
| --- | --- |
| Main Kotlin CI | **PASS** — [run 43311382693](https://github.com/german-krasnikov/polski-grammar-srs/actions/runs/43311382693), web and JVM jobs |
| Main Pages build/deploy | **PASS** — [run 43311382856](https://github.com/german-krasnikov/polski-grammar-srs/actions/runs/43311382856) |
| Public Chromium, Firefox, Playwright WebKit | **PASS 3/3**, actual `originWasmComposeApp.js` and successful Wasm requests, 16 official papers plus one uncertified sample |
| Public B1/2017 passport audio | **PASS in all three engines**, continuous >41 seconds, visible `0:40 / 4:39`, progress 14%, pause works, no page errors |
| Public byte-range delivery | **PASS 16/16** MP3 URLs, HTTP 206 and correct requested bytes |
| Public Writing images | **PASS 7/7** exact source SHA-256 |
| Public full MP3 bytes | **PASS 16/16**, complete served bodies total 798,131,900 bytes; every SHA-256 equals the effective DSL hash |
| Local full Chromium exam regression | **PASS 92/92**, no retries |
| Local full non-exam Chromium regression | Initial **166 PASS / 2 SKIP / 1 FAIL**; fixture lacked progress-reader installation in custom touch context. Corrected affected case **1/1 PASS**. No production or golden change |
| Canonical official answer closure for ten new papers | **PASS 10/10 each** on Desktop JVM, managed Chromium JS and Wasm; all 30 Listening/Reading/Grammar module covers |
| DSL/source checks | **PASS** full Vitest 353/353, typecheck and generation check on publication candidate; merged main generation/check, typecheck and focused 18/18 checks PASS |
| iPad Simulator published catalog | **PASS** 17/17 bridge tests, C1 paired/rubric UI and retained Grammar/Polish draft cases; app contains 16 exact original MP3s and seven source images |
| Genuine iPad window resize | **NOT VERIFIED**, attempted system gestures left frame unchanged; failing acceptance test preserved in branch checkpoint `a332681` |
| Physical iPad / native Safari / human acoustic transcription audit | **NOT RUN**; physical device deferred by owner, managed WebKit is not native Safari |

Public cold startup: Chromium 2,619 ms; WebKit 3,018 ms; Firefox 13,761 ms. No claim that all cold loads finish within ten seconds. The first external probe incorrectly expected the older screenshot's 4:54 duration; the frozen authored segment is 1116–1395 seconds (4:39). Only the external probe expectation was corrected, without changing source data or test oracles.

The first full-body Node verifier used a fixed 60-second transfer limit and timed out on large MP3s. HTTP range checks and playback had already passed. A separate bounded curl verifier allows 300 seconds per file and compares complete response SHA-256 with the DSL's effective web hash. Transfer diagnostics are retained; timeout alone is not classified as corrupt product audio.

The local final WebKit sweep initially passed 15/16 papers and failed B1/2020 pause-after-end; the exact case later passed, while a bounded repeat gave two passes and one startup failure before media. This intermittency remains documented, separately from the successful public WebKit check. System Chrome 157/Karma also reported completion timeouts after canonical scorer completion markers; the managed Chromium 153 JS/Wasm classes passed without changing timeout or expectations. See [independent browser evidence](P9-PUBLICATION-BROWSER-INDEPENDENT-EVIDENCE.md).

Media release: [exam-audio-v2](https://github.com/german-krasnikov/polski-grammar-srs/releases/tag/exam-audio-v2), pinned original and browser archives. Site byte total 830,244,762 (47 files including empty `.nojekyll`), below the accepted 850,000,000 target. Eight derivatives have independently verified full PCM sample counts and 48 zero-lag boundary comparisons; originals remain authoritative. See [alignment](P9-WEB-AUDIO-INDEPENDENT-ALIGNMENT.md) and [native evidence](P9-PUBLICATION-NATIVE-INDEPENDENT-EVIDENCE.md).

Durable logs, screenshots, traces and JSON: sibling `polski-grammar-srs-worktrees/recovery-logs/20261112/public-pages/`, `public-pages-full-audio-curl.log`, and `p9-browser-independent/`. `browser-results.json` records each actual engine/runtime, elapsed audio and page errors; `ranges-and-images.json` records all paths. These files are outside Git to avoid committing binary test output.

Completion remains **10/12** in [the session plan](NEXT-SESSION-PLAN.md). B2 June 2022 is excluded because the original recording is damaged; [other-site research](P9-B2-2022-06-SOURCE-RECOVERY-20261112.md) did not locate a healthy independent copy. P11 remains open for genuine iPad window resize, without treating rotation as equivalent.

## Owner follow-up: remove the demonstration card

The owner requested removal of `B1 Przykładowy`. The web-only prepended card and Grammar data alias were removed; the browser now consumes the same published DSL `ExamCatalog.entries` and `officialGrammarData` as native hosts. Source-only draft fixtures are not exposed by that catalog.

Observed regression RED on the frozen previous artifact: the catalog assertion expected 16 and found 17. After correction, the three-engine catalog/timed-entry and explicit JS fallback checks gave **7 PASS / 2 deliberate Chromium-only skips**, zero retries. Both JS/Wasm/compat distributions and all 16 media hash checks passed `:composeApp:verifyNoWebSourceMaps`; TypeScript typecheck PASS. Logs and original RED trace are retained under sibling `recovery-logs/20261112/remove-sample-*`. The preceding full 92-case result belongs to the prior runtime; only the affected catalog checks were rerun for this small follow-up. Deployment result is recorded separately after publication.
