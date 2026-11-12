# P9 web audio derivative provenance

2026-11-12. This records the local browser delivery candidates for the eight new papers whose original source MP3s are 320 kb/s. The authoritative originals, their official URLs and SHA-256 pins remain in [source provenance](P9-SOURCE-PROVENANCE.md) and `dsl/exam-assets/exam-audio.core-data`. No original MP3, task URL, source text or listening window was changed. MP3 re-encoding is lossy; byte and signal checks do not substitute for human acoustic review.

## Encoder and correction

Encoder: Homebrew FFmpeg **8.0.1**, `libmp3lame`, on macOS. Each original was SHA-256 checked against its DSL pin before encoding. The final command for each source and output was:

```sh
ffmpeg -nostdin -hide_banner -v error -threads 1 -i <original.mp3> \
  -map 0:a:0 -vn -c:a libmp3lame -b:a 160k -ac 2 \
  -id3v2_version 0 -y <web.mp3>
```

No resampling, trimming, normalization or timestamp change was requested. Source and output are 44.1 kHz stereo. The command retains FFmpeg's default Xing/LAME metadata for gapless decoder delay. An initial candidate used `-write_xing 0`; independent signal review found a **25.0625 ms delay** at four sampled source boundaries despite high correlation. That candidate was rejected before the web archive was built. The final Xing candidate has no measured lag in the independent 48-boundary review. The checked maintainer command is `npx tsx scripts/encode-exam-web-audio.ts verify-derivatives`; its `encode` mode reproduces the exact command and refuses to replace a derivative if its output hash differs from the authored DSL pin.

| Paper | Web bytes | Web SHA-256 |
| --- | ---: | --- |
| B2/2023-02 | 40,434,416 | `717aa32c86340c7b5b193aa6a4befe6e790392f23ffefe61a16d9474e8db2a8c` |
| B2/2023-06 | 46,395,559 | `b3bd3e0882a2027c43ce6fee964a473982ebf2b05860ca5da3c1c84c3b796d68` |
| B2/2024-02 | 47,567,412 | `3453c7158733f5b3ac3680b7c443f1cf4c9cb2036f634c61b65b0de16160758f` |
| B2/2024-06 | 43,213,322 | `f8b17f45b64ec6c7fb5e70657b5bf4166d21505479c405d149ec2d858e14e6bc` |
| C1/2022-03 | 50,130,024 | `06566b785cf8712fd3195099aa1eb4c6c9ea140e4773dbe3c893722118cce095` |
| C1/2022-11 | 44,906,057 | `b1fb9447413b42049b837d5de22990555264c2a02b538791c3b5682350645d26` |
| C1/2023-11 | 44,459,363 | `0df5c919fc1bf9aad892b127166edf32fff63af5953070214b730815e8ebea1e` |
| C1/2024-04 | 47,883,493 | `2f03d9275e821afd44be024b4fb60bb5e90d76f56c41ea28ac684b716f23e52f` |

The other eight browser files are the exact original bytes: six legacy recordings plus B2/2022-02 and C1/2023-04. Their `webSha256`/`webLocalSourcePath` fields are absent, so the manifest's original source hash applies to browser delivery too.

## Decode and timeline evidence

Developer `verify-derivatives` rechecked each of eight original and derivative hashes and fully decoded both streams to stereo PCM with `ffmpeg -err_detect explode -xerror`. Each pair has exactly the same decoded sample count at 44.1 kHz; output channels and sample rate match source. See local log `/tmp/p9-web-derivative-xing-verify-20261112.log`.

An independent full-record audit decoded both files once at 16 kHz mono for each of eight pairs. All eight decoded lengths matched exactly. Across **48 actual task-boundary clips**, every best correlation lag was **0.0 ms** and the minimum normalized correlation was **0.9996318**. The audit and rejected-candidate RED evidence are in sibling `media-audit-20261112/web-derivative-independent/`, summarized in [independent alignment evidence](P9-WEB-AUDIO-INDEPENDENT-ALIGNMENT.md). These measurements demonstrate timing preservation at reviewed edges; they do not certify every spoken word or final browser seeking behavior.
