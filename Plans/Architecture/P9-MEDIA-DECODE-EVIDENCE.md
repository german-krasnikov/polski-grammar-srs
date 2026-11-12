# P9 fresh full-stream media check

2026-11-12. All eleven received MP3 files were hashed and fully decoded from the original ignored main-checkout `ExamBin` vault. No original bytes were changed. Pins are in [source provenance](P9-SOURCE-PROVENANCE.md); candidate task windows remain in the [boundary audit](P9-AUDIO-BOUNDARY-AUDIT.md).

| Recording | Pinned SHA-256 | Full decode | ffprobe nominal seconds |
| --- | --- | --- | ---: |
| B2/2024-02 | MATCH | PASS | 2378.305306 |
| C1/2024-04 | MATCH | PASS | 2394.109388 |
| B2/2024-06 | MATCH | PASS | 2158.760300 |
| B2/2023-02 | MATCH | PASS | 2021.655510 |
| C1/2023-04 | MATCH | PASS | 2437.551020 |
| B2/2023-06 | MATCH | PASS | 2319.712653 |
| C1/2023-11 | MATCH | PASS | 2222.902857 |
| B2/2022-02 | MATCH | PASS | 1990.752653 |
| C1/2022-03 | MATCH | PASS | 2506.435918 |
| B2/2022-06 | MATCH | FAIL, exit 183 | 2180.427755 |
| C1/2022-11 | MATCH | PASS | 2245.237551 |

**Observed result:** all 11 hashes match their recorded pins; 10 recordings fully decode with zero exit status. B2/2022-06 fails with `Invalid data found when processing input` / `Error submitting packet to decoder`, exit 183. A matching hash proves byte identity, not a healthy audio stream. Nominal duration does not certify the damaged tail.

Command per file:

```sh
ffmpeg -nostdin -hide_banner -v warning -threads 1 -err_detect explode \
  -i <original.mp3> -map 0:a:0 -xerror -f null -
```

Two independent files were checked concurrently. Machine-readable results and per-file logs are retained outside the checkout under sibling `media-audit-20261112/{decode-summary.json,<level-session>.decode.log}`. This is fresh decode evidence; it does not establish acoustic task boundaries, transcript completeness at each cut, delivery, browser/native playback, or publication readiness.

The official [archive](https://certyfikatpolski.pl/o-egzaminie/testy-egzaminacyjne-z-poprzednich-lat/) still links B2 June 2022 to the pinned canonical MP3 URL. A fresh local ordinary range request returned HTTP 403. The web reader recognized that URL as audio/mpeg but does not expose downloadable binary content; computer/browser control was unavailable in this environment. No replacement bytes were obtained, and no inference is made that the origin itself shares the local corruption. B2/2022-06 remains inactive.
