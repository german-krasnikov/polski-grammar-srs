# Frozen UTF-16 fixture construction — 2026-11-08

Kotlin/JS constant folding converted the test's lone-surrogate `Char.toString()` inputs into literal question marks in emitted JS. Full shared JS browser run failed `FrozenAcCorpusProbeTest.p07ToP12MatchFrozenAAndCBytesAndOrderedTrees` before the fix. Constructing each unit via runtime `charArrayOf(...).concatToString()` preserves the intended UTF-16 input. Frozen expected bytes, trees and hashes are unchanged; production code is unchanged.

Focused JS browser verification: **8/8 PASS**, `/tmp/polski-frozen-utf16-focused.log`. Full shared JS browser rerun: **FAIL**, Karma disconnected after ping timeout; no assertion failure reported (`/tmp/polski-p10-main-browser-unit-fixed.log`). Subsequent targets in the combined invocation were **NOT RUN**. Independent read-only review **APPROVED**. This is not a claim that all browser unit suites pass.
