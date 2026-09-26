# Android flip/Rive adb perf harness (manual, not a Gradle task)

Test-only tooling used to produce `Plans/Kotlin/artifacts/flip-rive/android/`.
Not wired into any Gradle task or CI: it drives a **running emulator/device**
over `adb` + `uiautomator dump`, so it belongs alongside the app, not inside
it, and it is not something `./gradlew test` can invoke.

Prereqs: an installed debug build of `dev.polski.grammarmatrix` running on a
connected device/emulator (`adb devices` shows it), Python 3, `ADB` env var
pointing at the `adb` binary (defaults to `adb` on PATH).

```
export ADB=$ANDROID_HOME/platform-tools/adb
python3 flip_rive_perf_driver.py flip_rive 5 5   # variant name, n_flips, n_rates
python3 flip_rive_perf_driver.py no_animation 5 5
python3 leak_check.py 15                          # rating cycles, meminfo every 5
```

Toggle the measured variant by hand first, in Settings -> Движение:
"Системное" = flip+Rive, "Уменьшенное" = no-animation (Rive gated off by
`reduceMotion`). There is no reachable toggle for the third "native-flip
only, Rive off" variant on Android -- see the artifacts README for why
(`riveDisabledForMeasurement()` reads a JVM `System.getProperty` that nothing
on a running Android process can set from outside it).

Known limitation: the driver's own `uiautomator dump` + scroll-to-find-button
steps are real UI work on the (software-rendered) emulator and dominate the
frame counts reported by `dumpsys gfxinfo`, so treat the gfxinfo numbers as
a controlled-but-noisy comparison, not an isolated flip/Rive cost. A real
Macrobenchmark (`androidApp/src/benchmark`, currently an empty, unwired
source set) would be needed for a trustworthy per-animation frame-time
metric.
