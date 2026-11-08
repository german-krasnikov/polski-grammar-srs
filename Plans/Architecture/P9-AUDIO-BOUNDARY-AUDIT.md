# P9 audio boundary audit

2026-11-08. In-progress source audit for the eleven new sessions; source hashes and full MP3 durations are in [provenance](P9-SOURCE-PROVENANCE.md). This is ASR plus signal inspection, not human listening certification or browser acceptance. No new session is made catalog-ready by this document.

## C1 April 2024: source-backed candidate windows

Full original MP3 was transcribed with cached faster-whisper `base`, CPU int8, Polish, beam 3, VAD enabled, no previous-text conditioning. Six separate clips (start, four joins, final closure) were then transcribed with beam 5/VAD disabled and checked with `ffmpeg silencedetect=n=-35dB:d=0.25`. The clipped ASR hallucinated/shifted phrases into leading silence at joins I/II and II/III; its raw timestamps are **not** used as boundaries. Actual speech/silence intervals below resolve those joins. The full official transcript is in key pages 1–5 and remains the content source, not the ASR spelling.

| Task | Candidate start–end (seconds) | Observed next-task announcement / closure |
| --- | --- | --- |
| I | 110–468 | First-task announcement begins 110.275; closure 465.013–466.309. |
| II | 468–978 | Announcement 468.964–469.805; closure 975.435–976.774. |
| III | 978–1436 | Announcement 978.942–979.900; closure 1433.833–1435.104. |
| IV | 1436–1952 | Announcement 1437.337–1438.276; closure 1949.289–1950.522. |
| V | 1952–2322 | Announcement 1952.679–1953.768; closure 2320.439–2321.861. |

Each boundary lies in measured silence and preserves the complete closure. The final instruction to check the whole test begins after 2323.955 and is outside the standalone task V window. Full module duration remains the separately sourced official 40-minute limit; these segments are not a substitute for the module configuration.

Arkusz page 2 and the audio instruction explicitly state task I is recorded **once**. Tasks II–V state twice. The full ASR contains two distinct occurrences of matching official-transcript material inside each window: II “Mimoza nieśmiała” near 579 and 827 seconds; III the MySpace interview and Lidia Rudzińska introduction in two blocks around 1024 and 1248; IV “Zespół eksplodującej głowy” around 1483 and 1738; V the holiday-table/banana passage around 2035 and 2214. These anchor times locate repeated content approximately, not exact phrase edges. The player must play each whole recorded segment once, with no extra automatic repeat.

Temporary reproducibility artifacts: `/tmp/polski-p9-audio/C1-2024-04/faster-base.json`, `clips/clipped-base.json`, `clips/*-silence.log`; source runner `/tmp/polski-p9-transcribe.py`, clip runner `/tmp/polski-p9-audit-clips.py`. Candidate windows require independent source review and real-media Playwright acceptance after DSL authoring. **NOT RUN:** human acoustic confirmation, new-session browser tests, all other P9 final boundaries.

A subsequent clipped `base` pass with VAD and word alignment removed the leading-silence hallucinations at C1 I/II and II/III. It places the closure/announcement words near the independently measured signal spans above; `clips/aligned-base.json` records the output. Silence intervals, rather than rounded ASR edges, still define the candidate cut points.

## B2 February 2024: source-backed candidate windows

Full MP3 locator pass: Whisper `tiny`, Polish, CPU, two threads. Independent new clips: faster-whisper `base`, int8, beam 5, VAD and word alignment, plus 16-kHz mono signal inspection with the same silence threshold. Raw tiny text is imperfect and is not the task transcript source. Arkusz/key supply the authoritative content.

| Task | Candidate start–end (seconds) | Observed announcement / closure |
| --- | --- | --- |
| I | 109–500 | Announcement 109.338–110.368; closure 497.144–498.848. |
| II | 500–1028 | Announcement 501.087–502.035; closure 1025.187–1026.753. |
| III | 1028–1591 | Announcement 1028.787–1029.829; closure 1588.440–1589.982. |
| IV | 1591–1978 | Announcement 1592.173–1593.282; closure 1974.738–1976.254. |
| V | 1978–2309 | Announcement 1978.532–1979.534; closure 2307.357–2308.917. |

All five task instructions state that the recording is played twice, confirmed by clipped base recognition. Full-recording anchors show two content occurrences: I the knee-injury conversation around 182/384 seconds, II the Świtalski biography in two blocks around 541/809, III Grand Prismatic Spring around 1138/1424, IV the Politechnika Wrocławska announcement around 1628/1822, V the residents complaining about beavers around 2061/2233. The whole segment already includes the repeat; no second automatic loop is appropriate. Final whole-test checking begins around 2310.975, outside task V. Temporary artifacts: `/tmp/polski-p9-audio/B2-2024-02/audio.json`, `clips/aligned-base.json`, `clips/clip-manifest.json`, and `clips/*-silence.log`. These remain candidate windows pending independent review and real-media browser acceptance; no P9 catalog activation is claimed.
