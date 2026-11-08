# B1/2017 exam data evidence

Date: 2026-11-08. Scope: local exam preview from `ExamBin/pl/B1/2017/`. This is the provider's *test przykładowy* archived in the 2017 directory. The paper, key, and audio were supplied locally; no provider download or publication was performed.

## Source provenance

| Input | SHA-256 | Relevant pages |
|---|---|---|
| `ExamBin/pl/B1/2017/arkusz.pdf` | `cd176f129cf8feaa13ee25844d5adac9bb2e59df3a26e9f937f795c699897851` | Listening 3–7; reading 9–14; grammar 16–20; writing 22 |
| `ExamBin/pl/B1/2017/klucz.pdf` | `e5eacbd6c37e222640c19e00c15a71139f20feff2747a0b854042702ac2ddd70` | Listening 3; reading 4; grammar 5–6 |
| `ExamBin/pl/B1/2017/audio.mp3` | `e7d78c0f1bf64ef6ebf83e7d8b9d38f30539a2d33f205d40d230d56d03fb2747` | 24m34s local recording |

The paper and key were text extracted with `pdfplumber`; the writing page was rendered and visually inspected. The Set II/2 image was extracted from the embedded JPEG on paper page 22. `kotlin/composeApp/src/webMain/resources/exam-images/B1-2017-writing-II-2.jpeg` is 494×646 pixels, SHA-256 `ed4520cd51239c1087137b0341cf39f7e5548017e06cbb5534653e4ca4b919cd`. Its alternative text is in `writing.core-data`.

## Authored rows versus printed counts

| Section | Paper | Authored rows | Maximum |
|---|---|---:|---:|
| Listening I–V | 14×0.5, 7×1, 5×1, 6×1, 5×1 | 5 tasks, 37 questions | 120 quarters = 30 points |
| Reading I–V | 5×1, 7×1, 7×1, 7×1, 8×0.5 | 5 passages, 34 questions | 120 quarters = 30 points |
| Grammar I–VII | 10×0.5, 5×0.5, 10×0.5, 5×0.5, 10×0.75, 5×0.5, 5×1 | 7 tests, 50 items | 120 quarters = 30 points |
| Writing | Choose one of three sets, complete both tasks in it | 3 sets, 6 prompts | Human graded |

Grammar IV/2 accepts `z którym` for 0.5 and `z kim` for 0.25; IV/3 accepts `po co` for 0.5 and `dlaczego` for 0.25, as the official key states. The primary answer and scored variants are separate DSL rows. Writing limits in paper order are 20, 180, 25, 175, 60, 140 words. Set II/2 requires the paper image and now references its extracted local resource.

## Audio timing and delivery

A local `whisper` tiny Polish transcription of the MP3 produced 177 timestamped segments. A separate `whisper --model base --language Polish` pass on Task V and four join clips, plus `ffmpeg silencedetect=n=-35dB:d=0.15`, found the exact task transitions. The current task windows are I 115–369 s, II 369–599 s, III 599–877 s, IV 877–1116 s, V 1116–1395 s. The earlier provisional 597-second II/III join clipped about 0.3 seconds of the spoken Task II closure; 1100-second IV/V join played the end of Task IV and a long silence at the start of Task V. At 1080–1130 seconds, the waveform has silence from 1095.62–1111.84, the Task IV closure at about 1111.84–1113.10, silence until 1116.17, then the Task V announcement. At the end, the second Task V content ends around 1378.12, followed by silence until 1392.90; the spoken closure ends around 1394.10, and the next unrelated instruction begins after 1396.63. The 1395-second endpoint sits in that measured silence. These audio-derived expectations are in `dsl/exam-scoring/b1-2017-inventory.core-data`.

Content blocks II (399–463/505–580 s), III (627–717/768–856 s), IV (913–977/1036–1090 s), and V (1129–1236/1268–1378 s) appear twice in the actual recording. Task I appears once. The player must not add a second automatic playback. Whisper still contains noisy individual words, especially near Task V; this evidence validates task identity and boundaries, not a verbatim transcript.

For local browser acceptance only, the 22 MB MP3 was copied to ignored `kotlin/composeApp/src/webMain/resources/exam-audio/B1-2017.mp3`; the built distribution contains it. This binary is not tracked, so durable delivery from a fresh checkout or published site remains **NOT RUN**. The web player must show an unavailable state if the file is missing.

## Executed checks

- **PASS** — `node <BIOME_BIN> data check --contract dsl/exam.data-contract` with all nine B1/2017 row bindings: 147 rows, 0 problems; cwd repository root.
- **PASS** — `node <BIOME_BIN> generate --project dsl/biome.project` and `generate --check`: generated B1/2017 typed rows and no stale outputs; cwd repository root. The neighboring toolchain reports the pre-existing stale pin warning (`b3fc1ec9` pinned, `80779aa9` current); output is still byte checked.
- **PASS** — `./gradlew :shared:desktopTest --tests polski.exam.B1y2017DataTest --tests polski.exam.ExamPointsTest --tests polski.exam.grammar.GrammarExamScorerTest --console=plain`; cwd `kotlin/`.
- **PASS** — `./gradlew :composeApp:wasmJsBrowserDistribution --console=plain`; cwd `kotlin/`, final local artifact includes MP3 and JPEG.
- **PASS** — independent audio boundary check with `whisper` base on the official MP3 Task V/join clips and `ffmpeg silencedetect`; corrected II/III and IV/V endpoints above. A new browser playback check against the corrected artifact is pending.
- **NOT RUN** — JS fallback, Firefox/WebKit, native devices, and fresh-checkout/published durable audio delivery at the time of this record. Browser results are handed off separately.
