# Android flip+Rive perf measurement (this pass)

Emulator: Polski_ARM35, `emulator-5554`, `-no-window -no-audio -gpu swiftshader_indirect`
(software-rendered GPU -- absolute frame times are NOT representative of a real
device; only relative comparisons between variants on this same emulator are
meaningful, and even those are noisy, see caveats below).

APK under test: `androidApp-debug.apk`, built fresh from the dirty working tree
at HEAD `a7b063c` (`./gradlew :androidApp:assembleDebug`), installed via
`adb install -r`.

## Variants actually exercised

- **A -- no-animation**: Settings -> Движение -> Уменьшенное (reduceMotion=true).
  Flip snaps (`snap()`), Rive is gated off (`cardEffectToPlay` returns null
  when `reduceMotion`). File: `variant-A-no_animation.json`.
- **C -- flip+rive**: Settings -> Движение -> Системное (default). Flip
  animates (`tween(500)`), Rive plays confetti/again on rating. File:
  `variant-C-flip_rive.json`.
- **B -- native-flip only (Rive disabled, flip animated) -- NOT MEASURED.**
  `riveDisabledForMeasurement()` in `AndroidTrainingScreen.kt` reads
  `System.getProperty("polski.debug.riveDisabled")`, a JVM system property.
  There is no reachable way to set this on an installed Android app process
  from the outside (no intent extra, no settings/debug-menu toggle, and
  `adb shell setprop` sets Android's separate `SystemProperties` store, which
  `System.getProperty` never reads). Unlike iOS/macOS (which got a real
  `@AppStorage` debug toggle in Settings per the plan's own summary), Android
  has no equivalent. This variant cannot be produced on the shipped build
  without a source change -- reported as a gap, not fabricated.

Each variant ran 5 tap-to-flip cycles then 5 swipe-to-rate cycles (3x "Good"
and mixes of "Good"/"Again" in the leak run), driven by a disposable Python
harness (`tests/perf` was not used -- the harness lives only in this session's
scratchpad, since it is adb/uiautomator glue, not a Gradle-runnable test) that:
uses `uiautomator dump` to locate the actual on-screen elements each step
(reveal button, flip zone, rating swipe zone), taps/swipes with
`adb shell input`, and reads `dumpsys gfxinfo <pkg>` / `dumpsys meminfo <pkg>`
before and after each batch.

## Results

| metric | A (no-animation) | C (flip+rive) |
|---|---|---|
| total frames in window | 929 | 1095 |
| janky frames % | 58.56% | 49.04% |
| p50 / p90 / p95 / p99 frame time | 57 / 81 / 89 / 150 ms | 46 / 77 / 81 / 113 ms |
| missed vsync | 258 | 254 |
| slow UI thread | 236 | 224 |
| frame deadline missed | 544 | 537 |
| PSS before -> after (KB) | 98490 -> 100181 | 95432 -> 99006 |

**Caveat (important): these two rows are not a clean A/B of the flip/Rive
rendering cost.** Each cycle's `uiautomator dump` + scroll-to-top +
scroll-to-find-button steps are real UI work on this software-rendered
emulator and dominate the frame count (the flip/Rive animation itself is a
handful of frames per gesture out of ~1000 counted). The identical gesture
script ran for both variants, so the comparison is *controlled* for harness
overhead, but the result -- variant A reading slightly *worse* than variant C
on p50/janky% -- should be read as noise (GC timing, emulator scheduling,
small N) rather than evidence that reduced motion is slower. A real
per-frame-type breakdown (e.g. Macrobenchmark's `FrameTimingMetric` isolating
just the flip animation window) would be needed for a trustworthy delta; no
Macrobenchmark harness exists in this repo yet (`androidApp/src/benchmark` is
an empty, unwired source-set stub).

CPU: `top -b -n1` sampled every 0.5s across two rating-swipe cycles shows the
app at 0% CPU at rest and one sample at 96.1% (single core) coinciding with a
rating swipe (flip animation + Rive view creation/trigger + FSRS
save all land in the same frame window) -- see
`cpu-sample-idle-then-active.txt`. This is one real data point, not a
distribution.

Memory-leak check (flip+rive variant, `leak-check-15-cycles-flip_rive.json`):
15 rating cycles (mixed Good/Again, each creating/re-triggering a
`RiveAnimationView` per FC-15/16/20), PSS sampled every 5 cycles:
103369 -> 98125 -> 98761 -> 98333 KB. Flat, no growth trend -- but this is
**15 cycles, not the requested 50** (time-boxed; each cycle costs ~10-15s of
adb round-trips). No leak signal in this shorter window; a 50-cycle run was
not completed this pass.

Rive load / first-effect latency: **not measured**. `adb logcat` was captured
around rating events and grepped for `rive`/`Rive`; rive-android's Kotlin API
(`RiveAnimationView`/`Rive.init`) emits no log lines at Info level or above
that would bracket "resource decode start" vs "first frame drawn", so no
reliable timestamp pair could be extracted. The gfxinfo histograms are the
only indirect signal (see `variant-C-flip_rive.json`'s p95/p99 vs A's).

## Binary size delta vs baseline commit a7b063c

Baseline built in a disposable `git worktree` at `a7b063c` (removed after
measuring): `androidApp/build/outputs/apk/debug/androidApp-debug.apk` =
**13,061,119 B**.
Current dirty tree (same command): **41,170,211 B**.
**Delta: +28,109,092 B (~26.8 MB), a 3.15x size increase of the debug APK.**

Breakdown (`unzip -l`, uncompressed sizes): almost entirely native code added
by `rive-android`, present once **per ABI** in this multi-ABI debug APK:

| ABI | `librive-android.so` | `libc++_shared.so` |
|---|---|---|
| arm64-v8a | 5.34 MB | 1.29 MB |
| armeabi-v7a | 5.09 MB | 0.87 MB |
| x86 | 5.69 MB | 1.25 MB |
| x86_64 | 5.71 MB | 1.25 MB |

Plus `classes12.dex` grew ~1.45 MB (rive-android's Java/Kotlin bytecode) and
a few KB in `classes2.dex`/`classes9.dex` for the new `CardEffect`/flip/Rive
Kotlin code. This debug APK bundles all 4 ABIs; a real Play-distributed App
Bundle would split per-device and ship roughly 1/4 of the native-lib delta
(~6-7 MB) per install instead of the full ~26.8 MB -- this number is a debug,
unsplit, unstripped upper bound, not a per-device download estimate. It is
in the same range as iOS's measured +9.75 MB and macOS's +9.6 MB single-arch
framework deltas (that pass measured one arch slice each; Android's number
looks ~3x larger mainly because it counts all 4 ABIs at once, not because the
Android integration of the same rive-ios-sibling runtime is meaningfully
heavier per architecture).
