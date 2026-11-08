# Exam audio boundary audit

Date: 2026-11-08. This record covers the **local** source MP3 files. It is an
independent ASR and waveform check of task windows, not human acoustic
listening, a verbatim transcript certification, or a browser playback result.

## Method

The previous `ExamBin/pl/{B1,B2}/2019/transcription.json` files were used only
to locate candidate joins. Whisper `base`, Polish, ran on new clips cut directly
from the original MP3 files; the outputs are
`/tmp/polski-audio-audit/old2019-base-boundaries.log` and
`/tmp/polski-audio-audit/B2-2019-base-more.log`. `ffmpeg
silencedetect=n=-35dB:d=0.25` checked the actual signal around each join.
Some base clips hallucinated words across long silence, so the boundary was
chosen from the silence intervals plus the specific spoken closure/next-task
phrases, not from ASR timestamps alone. One example is B2/2019 III/IV: the old
ASR merged `Zadanie 3. Zadanie 4` into a 1144.9–1172.9-second segment; new
base recognizes the closure around 1161 and the fourth-task announcement
around 1164.

## Corrected source windows

| Exam | Task I | II | III | IV | V |
| --- | --- | --- | --- | --- | --- |
| B1/2019 | 103–309 s | 310–546 s | 547–798 s | 799–1068 s | 1069–1475 s |
| B2/2019 | 68–427 s | 428–792 s | 793–1163 s | 1164–1580 s | 1580–2006 s |

B1/2019 waveform evidence: first speech after 103.59 s; task closures at
306.43–307.92, 544.09–545.33, 795.10–796.49, 1065.69–1066.96, and
1472.23–1473.43 s. The old Task III endpoint, 787 s, discarded its spoken
closure and the Task IV start at 790 s played the closure instead of its own
announcement. The fourth-task announcement starts after 798.85 s.

B2/2019 waveform evidence: first task speech after 68.72 s; closures at
425.27–426.50, 790.24–791.36, 1160.75–1161.93, 1577.40–1578.54, and
2003.04–2004.25 s. The old Task III endpoint, 1144.9 s, omitted the long
pause and spoken closure; Task IV began with that preceding task's closure.
The actual fourth-task announcement starts after 1163.97 s. The other old
endpoints also cut some or all of the closure phrases.

The corrected windows are in each exam's `listening-tasks.core-data`. They
include the recorded repeat, so the player should not loop a task again.
**NOT RUN:** generated-row checks, browser playback of these new windows, and
human acoustic confirmation. B2/2019's question and key data remain under
separate source restoration; these timing corrections do not make it catalog
ready.
