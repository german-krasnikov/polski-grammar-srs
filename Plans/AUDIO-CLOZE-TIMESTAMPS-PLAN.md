# Module design: Audio Cloze — per-sentence timestamp alignment

## Scope and consumers

**Capability**: replace proportional character-count estimation of per-sentence audio timestamps with
pre-computed, Whisper-aligned values stored in DSL data files. Accurate sentence-level audio segments
(target: ≤ 1 s error) for all `AudioClozeExercise` items generated from `ListeningTask` transcripts.

**Consumers**:
- `AudioClozeGenerator` (commonMain) — reads sentence timestamps at exercise build time
- `ExamListeningSourceData` (commonMain) — assembles `ListeningTask` from DSL rows
- Offline Python alignment script — produces new `.core-data` entries

**Target runtimes**: Web (JS/Wasm), Android, iOS — all via shared `commonMain`. No runtime Whisper
dependency in the app; alignment is offline.

**Compatibility constraints**:
- DSL-first principle: all data in `.core-data` files, versioned in git
- biome generate pipeline must accept the new record without touching existing generated rows
- Existing exams without segment rows continue to work (graceful proportional fallback)

---

## Root cause, restated

`AudioClozeGenerator.fromTranscript` distributes `task.endMs - task.startMs` by character count.
The first sentence aligns incidentally (charOffset = 0 → task.startMs). Error accumulates across
sentences because:
- Task audio begins with exam instructions not present in the transcript
- Speaking rate is non-uniform; pauses aren't reflected in character counts

---

## Public contract

### New DSL record (in `dsl/exam.data-contract`) — NEW rows at end of file

```
# Per-sentence audio alignment produced by offline Whisper alignment.
# COUNT 0.. — optional; tasks without rows fall back to proportional estimation.
RECORD ListeningTranscriptSegmentRow KEY segmentId COUNT 0..
  segmentId: string
  taskRef: ref ListeningTaskRow
  sentenceIndex: int
  sentenceText: string
  startMs: int
  endMs: int
END
```

`segmentId` convention: `{taskId}:s{sentenceIndex}` — e.g. `certyfikat-polski-B2-2022-02-listen-1:s0`

`sentenceText` stores the exact sentence string (after `AudioClozeGenerator.splitSentences`), enabling
staleness detection if the transcript is later edited.

### New Kotlin value type (in `ListeningExamModel.kt`) — NEW

```kotlin
data class ListeningTaskSegment(val text: String, val startMs: Long, val endMs: Long)
```

### Modified `ListeningTask` (in `ListeningExamModel.kt`)

Add one field with a default:

```kotlin
val segments: List<ListeningTaskSegment> = emptyList(),
```

Position: after `transcript: String? = null`. All existing call sites that omit `segments` remain valid.

### Modified `AudioClozeGenerator.fromTranscript`

Replace the inline timestamp calculation with a lookup + fallback:

```kotlin
fun fromTranscript(...): List<AudioClozeExercise> {
    ...
    val totalChars = sentences.sumOf { it.length }
    val taskDuration = task.endMs - task.startMs
    var charOffset = 0
    return sentences.mapIndexedNotNull { sentIdx, sentence ->
        val seg = task.segments.getOrNull(sentIdx)
        val sentStartMs = seg?.startMs ?: (task.startMs + charOffset.toLong() * taskDuration / totalChars)
        charOffset += sentence.length
        val sentEndMs = seg?.endMs ?: (task.startMs + charOffset.toLong() * taskDuration / totalChars)
        buildExercise(examRouteId, task, audioUrl, sentence, sentIdx, allWords, sentStartMs, sentEndMs)
    }
}
```

---

## Components and dependencies

### Existing files modified

| File | Change |
|---|---|
| `dsl/exam.data-contract` | Append `ListeningTranscriptSegmentRow` record |
| `kotlin/shared/src/commonMain/kotlin/polski/exam/listening/ListeningExamModel.kt` | Add `ListeningTaskSegment` data class + `segments` field on `ListeningTask` |
| `kotlin/shared/src/commonMain/kotlin/polski/exam/ExamListeningSourceData.kt` | Group segment rows by `taskRef`; inject into `ListeningTask` construction |
| `kotlin/shared/src/commonMain/kotlin/polski/cloze/AudioClozeGenerator.kt` | Prefer segment timestamps; fall back to proportional |

### Generated file (auto-updated by biome generate)

`kotlin/shared/src/commonMain/generated/kotlin/polski/exam/data/published/ExamRows.kt` — biome adds
`ListeningTranscriptSegmentRow` data class and `LISTENING_TRANSCRIPT_SEGMENT_ROW_ROWS` constant.

### New offline script

`scripts/align-transcript-timestamps.py` (NEW)

**Inputs**: `--exam-data PATH` (a `.core-data` file or directory), `--audio-dir PATH` (directory of MP3 files)
**Output**: writes `LISTENING_TRANSCRIPT_SEGMENT { ... }` blocks to a specified `.core-data` file (or stdout)
**Requires**: `faster-whisper`, `ffmpeg` on PATH

**Algorithm**:
1. Parse `LISTENING_TASK` blocks from the input `.core-data` file (regex or simple line parser; no need for a full DSL parser)
2. For each task with a `transcript` field:
   a. Extract audio slice: `ffmpeg -ss {startMs/1000} -to {endMs/1000} -i {audioFile} /tmp/slice_{taskId}.mp3`
   b. Run faster-whisper with `word_timestamps=True, language="pl"` on the slice (seconds relative to slice start)
   c. Adjust word timestamps: `word.start + startMs/1000`
   d. Apply `splitSentences` logic (Python re-implementation of the Kotlin: split on `[.!?]`, filter len ≥ 20 and ≥ 4 tokens)
   e. Align: for each transcript sentence, find the matching span of Whisper words using normalized DP token alignment (lowercase, strip punctuation, skip OOV words)
   f. `sentStartMs = first_aligned_word.start * 1000`, `sentEndMs = last_aligned_word.end * 1000`
3. Output `.core-data` blocks; operator reviews and appends to the target `.core-data` file

**Alignment note**: faster-whisper can re-transcribe the slice at the original language; the word-level output `{text, start, end}` aligns well to Polish speech. The DP step handles minor lexical differences (numbers written out, contractions). A match confidence below threshold → emit a warning and skip that sentence (it will fall back to proportional).

### New `.core-data` data for existing exams

`exams/pl/certyfikat-polski/B2/2022-02/listening-tasks.core-data` — append segment rows after running the script.
Same pattern for B1 and other exams that have transcripts.

---

## Assembly change (`ExamListeningSourceData.kt`)

```kotlin
val segments = LISTENING_TRANSCRIPT_SEGMENT_ROW_ROWS.groupBy { it.taskRef }

// inside ListeningTask { ... }:
segments = segments[task.taskId].orEmpty()
    .sortedBy { it.sentenceIndex }
    .map { ListeningTaskSegment(it.sentenceText, it.startMs.toLong(), it.endMs.toLong()) },
```

`LISTENING_TRANSCRIPT_SEGMENT_ROW_ROWS` is generated by biome once the contract and `.core-data` entries exist.

---

## State and resources

- `ListeningTaskSegment` is immutable value data; no ownership concerns.
- Offline script: ffmpeg slice files are written to `/tmp`; remove after each task.
- Whisper model loaded once per script run; memory is the operator's concern.

---

## Decisions and tradeoffs

| Decision | Chosen | Rejected / why |
|---|---|---|
| Timestamp storage location | New `ListeningTranscriptSegmentRow` record in DSL | Encoded string field on `ListeningTaskRow` (fragile, couples format to parsing; opaque to biome validator) |
| `sentenceText` field | Stored alongside timestamps | Index-only (index silently drifts when transcript is edited) |
| Sentence splitting in script | Python re-implementation of Kotlin `splitSentences` | Using Kotlin for the script (adds build dependency for an offline tool) |
| Runtime Whisper | Not used; offline only | In-app: adds multi-MB models to every target, incompatible with Wasm/iOS |
| Fallback behavior | Proportional estimation when segments absent | Hard failure (would break all tasks without precomputed data) |
| `COUNT 0..` | Optional per design | Required (would need all tasks timestamped before generating) |

---

## Verification

### Unit: `AudioClozeGeneratorTest` (commonTest)

1. Task with `segments` populated → `startMs`/`endMs` on each exercise match the segment row exactly (no proportional math)
2. Task with empty `segments` → behavior unchanged from current (proportional, matches existing snapshot)
3. Task with partial `segments` (only sentences 0, 2) → sentences 0 and 2 use stored values; sentence 1 uses proportional fallback
4. Segment `sentenceText` mismatch (transcript edited, segment stale) — no contract to enforce at runtime but add a debug-only log assertion

### Integration: `ExamListeningSourceDataTest` (commonTest)

5. After biome generate with the new record present in `.core-data`, `officialListeningData["certyfikat-polski-B2-2022-02"]` tasks have non-empty `segments` with `sentenceIndex` in order and `startMs < endMs`
6. Tasks without segment rows have `segments = emptyList()` (not null, not an error)

### DSL contract check

7. `node <biome> generate --check --project dsl/biome.project` passes with the new record (no generation drift)

### Offline script

8. Script run on B2 2022-02 slice produces `startMs/endMs` within ±1 s of manually sampled audio positions for 3 reference sentences (manual spot-check; script outputs a summary table)

---

## Documentation and handoff

### Implementation steps (ordered)

1. **DSL contract**: append `ListeningTranscriptSegmentRow` to `dsl/exam.data-contract`
2. **biome generate**: run `node <biome> generate --project dsl/biome.project` — adds the row class and constant to `ExamRows.kt`
3. **Kotlin model**: add `ListeningTaskSegment` + `segments` field to `ListeningExamModel.kt`
4. **Assembly**: update `ExamListeningSourceData.kt` to group and inject segment rows
5. **Generator**: update `AudioClozeGenerator.fromTranscript` with lookup + fallback
6. **Tests**: write unit tests (steps 1–4 above) in `commonTest`; run `./gradlew :shared:jsTest` (or the project's standard Kotlin test command) — confirm GREEN
7. **Alignment script**: write `scripts/align-transcript-timestamps.py`; test it on B2 2022-02 against the existing MP3
8. **Data**: run script for each exam with a transcript; review output; append to `.core-data` files; re-run biome generate; confirm tests still pass
9. **Spot-check**: manually play the first sentence of tasks 2–5 in the app to confirm timing

### Open questions

- **Audio file location**: the script needs the actual MP3 path. The `audioUrl` in `.core-data` is a relative web asset path (`exam-audio/B2-2022-02.mp3`); the script needs a `--audio-dir` mapping from that relative path to the local file. Confirm the MP3 files are present on the dev machine (check `scripts/prepare-exam-audio.ts` for the asset pipeline).
- **B1 exams**: do they have MP3 files locally, or only B2 2022-02? The script design is the same; confirm MP3 availability before running.
- **Whisper language**: `pl` for Polish. Confirm that faster-whisper installed in the video-editor venv is accessible from outside that project (or document that the script should be run from the video-editor venv).
- **Sentence count drift**: if the Kotlin `splitSentences` regex is later modified, stored `sentenceIndex` values may no longer correspond. Add a note to `CLAUDE.md` or `AI/decisions.md` that `sentenceText` must be re-verified after any `splitSentences` change.
