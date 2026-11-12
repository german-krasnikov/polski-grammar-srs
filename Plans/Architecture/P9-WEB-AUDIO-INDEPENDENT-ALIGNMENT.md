# P9 web audio: source-to-derivative alignment

2026-11-12. Root independently measured the eight final web derivatives in the `p9-publication` worktree. This complements the developer's encoder verification and the earlier independent official-transcript audit; it does not establish browser playback or human acoustic review. Accepted contract: [web audio delivery](WEB-EXAM-AUDIO-DELIVERY-PLAN.md).

## Observed RED and correction

The initial candidate used `-write_xing 0`. Four real 40-second B2/2024-02 boundary clips measured a consistent **25.0625 ms** derivative delay against the original. The failed candidate was held from publication. The developer regenerated all eight recordings with Xing/gapless metadata and pinned the new actual hashes. Original MP3s, transcript text and authored task timestamps were unchanged.

## Final measured results

Environment: ffmpeg 8.0.1, Python 3.12.3, NumPy 2.0.1 on the local macOS host. Full-stream `ffmpeg -v error -xerror` decoding to mono float PCM at 16 kHz completed for all eight source/derivative pairs. For each pair, the total decoded sample count is identical. FFT cross-correlation compares the actual 40-second source and derivative clips around each of the six previously audited boundary coordinates per paper, searching offsets within ±100 ms. All **48/48** clips measure **0 samples / 0 ms** offset; the lowest normalized correlation is **0.9996317987009246**. Every source and derivative SHA-256 and derivative path matches the authored manifest read through public `biome read`.

| Paper | Source decoded samples at 16 kHz | Web decoded samples at 16 kHz | Difference |
| --- | ---: | ---: | ---: |
| B2/2024-02 | 38052693 | 38052693 | 0 |
| C1/2024-04 | 38305558 | 38305558 | 0 |
| B2/2024-06 | 34569822 | 34569822 | 0 |
| B2/2023-02 | 32346296 | 32346296 | 0 |
| B2/2023-06 | 37115211 | 37115211 | 0 |
| C1/2023-11 | 35566254 | 35566254 | 0 |
| C1/2022-03 | 40102783 | 40102783 | 0 |
| C1/2022-11 | 35923609 | 35923609 | 0 |

MP3 re-encoding is lossy: high waveform correlation and unchanged timing do not mean byte or sample-value identity. The eight originals remain authoritative native assets. The six existing public recordings and two new 192 kb/s recordings use original bytes; no derivative alignment claim is needed for those unchanged files. The earlier source audit covers 60 new-paper boundary coordinates and 50 task windows; this check covers the 48 coordinates affected by encoding.

## Reproduction and remaining gates

Durable evidence is under sibling `../media-audit-20261112/web-derivative-independent/`: `check-full-alignment.py`, `full-alignment.json`, `full-alignment.log`, and public-reader `manifest.json`; the initial failed measurements remain in `alignment-red.log`. From the publication worktree root:

```sh
OPENBLAS_NUM_THREADS=1 uv run --with numpy==2.0.1 python ../media-audit-20261112/web-derivative-independent/check-full-alignment.py
```

**PASS:** full decoding, exact decoded length, measured boundary alignment, original/derivative DSL hash chain. **NOT RUN here:** human listening, new derivative browser playback, HTTP range/seek behavior, native sixteen-paper install, release delivery and public Pages verification. Those are separate acceptance gates and must use the final artifact.
