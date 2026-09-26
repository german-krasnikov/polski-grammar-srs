# macOS flip+Rive perf pass — tester evidence (2026-09-26)

Repo `/Users/german/Work/JS/polski-grammar-srs`, branch `main`, working tree at (uncommitted, on top of)
HEAD `a7b063c`. This directory holds this tester pass's raw data only. It supplements, and does not
replace, the developer's own §11 evidence log in `Plans/Kotlin/FlipCardRivePlan.md`.

## What was actually measured (real, reproducible)

1. **Build.** `xcodebuild -project kotlin/macosApp/PolskiGrammarMac.xcodeproj -scheme PolskiGrammarMac
   -destination 'platform=macOS' -derivedDataPath /private/tmp/polski-mac-dd-perf
   CODE_SIGNING_ALLOWED=NO build` → `** BUILD SUCCEEDED **` (Debug, arm64), 43.3s wall.
2. **Bundle size delta vs baseline `a7b063c`.** Baseline built fresh in a disposable git worktree
   (`git worktree add --detach /private/tmp/polski-baseline-wt a7b063c`), same xcodebuild invocation,
   separate clean `-derivedDataPath /private/tmp/polski-mac-dd-baseline`. See `bundle-size-delta.csv`.
   Result: 28128 KB → 37972 KB, **Δ +9844 KB (~9.6 MB)**, almost entirely
   `Contents/Frameworks/RiveRuntime.framework` (9696 KB, absent from baseline). This independently
   corroborates the developer's own §11 number (28148→37972 KB, +9824 KB) to within ~20 KB (both are
   `du -sk` snapshots of an unsigned Debug build; the small difference is noise from directory
   metadata/timestamps, not a real discrepancy).
3. **Idle process footprint (no interaction).** Launched the built app (`open ... -n`, fresh
   `POLSKI_MAC_DATA_DIR`), sampled `ps -o pid,rss,%cpu,etime` 4× over ~8s while idle (no window focus,
   no input — see limitation below), then sent `kill` (clean exit, no crash). See
   `idle-memory-cpu-samples.csv`. RSS ~110–113 MB idle; CPU 2.7–18.7% (noisy, consistent with SwiftUI/
   AppKit's own idle redraw/animation-timer churn on a freshly-launched, unfocused window, not
   attributable to app logic since zero user interaction occurred).
4. **No UI test target exists for this host.** `grep -c "isa = PBXNativeTarget"
   PolskiGrammarMac.xcodeproj/project.pbxproj` → only 1 target (`PolskiGrammarMac`, the app itself, no
   `*UITests` target), confirming the developer's §11/§6 statement that `generate_project.rb` never
   created one for macOS.

## What could NOT be measured, and why (environment limitation, verified independently)

The task asked for scripted flip/swipe-with-effect cycles (≥5 each per variant) plus live frame-time
p50/p95/p99, dropped/janky-frame %, long-task/hitch counts, CPU during interaction, memory after 50
cycles (leak check), and Rive first-effect latency, via `xcrun xctrace record --template
'Animation Hitches' --launch`. None of this was obtainable in this session's environment:

- **No GUI automation path exists.** `osascript -e 'tell application "System Events" to get UI
  elements of window 1 of (first process whose frontmost is true)'` → `execution error: System Events
  got an error: osascript is not allowed assistive access. (-1719)`. This is the same class of failure
  the developer's own §11 log already hit (`-1728`, same root cause: no Accessibility/TCC grant for
  this non-interactive session) — independently reproduced here, not just trusted from the plan.
  Without Accessibility, there is no way to send synthetic clicks/keystrokes to the app from this
  session (CGEvent posting requires the same TCC grant), and there is no UI test target to drive it
  another way (see point 4 above). So the ≥5-flip/≥5-swipe scripted interaction this protocol calls for
  could not be produced at all — not even manually via a script, let alone measured.
- **`xctrace record` itself does not complete.** Two independent attempts (`--time-limit 5s`, both with
  and without a preceding literal-path/`--output` ordering mistake ruled out on the second try):
  attempt 1 PID 12047, attempt 2 PID 14967 — both sat near 100% CPU and produced no `.trace` output and
  no stdout at all, well past their own `--time-limit`, and had to be `kill -9`'d manually after 40–60s
  each. No `sudo` is available either (`sudo -n true` → "a password is required", non-interactive
  session can't supply one), ruling out an elevated-privilege retry. A real console GUI session for
  user `german` does exist on this Mac (`ioreg`'s `IOConsoleUsers` shows `kCGSSessionOnConsoleKey=Yes`),
  so this is specifically a missing-grant problem for *this automated session*, not "no display at
  all" — but granting it requires a human clicking an Allow dialog, which this pass has no way to
  request or wait for.
- Consequently: frame time percentiles, dropped/janky frame %, long-task/hitch counts, live CPU-during-
  interaction, the 50-cycle leak check, and Rive first-effect latency are **NOT RUN** for macOS in this
  pass, exactly as the developer's own §11 already flagged as an open gap. This tester pass does not
  manufacture numbers for them.

## Explicit caveat (per task instructions)

Every number above is from an unsigned Debug build on this one arm64 Mac; it is indicative only, not
device/Release-representative, and (per the two points above) no live-interaction runtime metric exists
for macOS at all in this environment — the gap is total, not merely "simulator vs device" noise.

## Cleanup

Disposable artifacts removed after this pass: `/private/tmp/polski-baseline-wt` (git worktree),
`/private/tmp/polski-mac-dd-baseline`, `/private/tmp/polski-mac-dd-perf`, `/private/tmp/polski-perf-macos`,
`/private/tmp/polski-mac-perf-data-idle`. Nothing was committed; the working tree's tracked/untracked
files are otherwise unchanged by this pass except the new files under this `artifacts/flip-rive/macos/`
directory.
