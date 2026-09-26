# Third-party credits: Rive rating effects

Vendored per FlipCardRivePlan.md FC-15, for the card-flip/rating-effect feature. Files are copied
per host (see each host's own `rive/` resource directory); this file records provenance once for
the whole repository, as the plan asks. Everything below is MIT-licensed by Rive, Inc.

## Runtime (web host only)

| File | Source | Version / commit | License | SHA-256 |
|---|---|---|---|---|
| `rive.js` | [`@rive-app/canvas-lite`](https://www.npmjs.com/package/@rive-app/canvas-lite) npm package, `rive.js` | 2.43.1 | MIT (Rive, Inc.) | `9fe995d892988459b814ee74bb5079be74ba68c17c200d795d9a8190470516d7` |
| `rive.wasm` | Same package, `rive.wasm` | 2.43.1 | MIT (Rive, Inc.) | `f4a9be2fbf64feb90900616f8bde5dd26e552b5589866f6a873dff49bc120215` |

Android uses `app.rive:rive-android` and iOS/macOS use the `rive-ios` SPM package (`RiveRuntime`)
instead of these two files; their own build files declare those dependency versions directly.

## `.riv` animation assets (all hosts)

| File | Source | License | SHA-256 |
|---|---|---|---|
| `confetti.riv` — "Remembered" burst | [rive-ios Demo-App](https://github.com/rive-app/rive-ios/blob/main/Demo-App/RiveExampleSPM/Rive%20Files/confetti.riv) | MIT (repository), state machine "State Machine 1", trigger "Trigger explosion" | `efdbdcb87ef4a9e937e4dd7e1c7f112de651b10fea3b5667921cfeb8f5eae49d` |
| `again.riv` — "Again" cue (renamed from `ui_swipe_left_to_delete.riv`) | rive-android / rive-ios sample assets | MIT (repository), state machine "Swipe to delete", trigger "Trigger Delete" | `e2283a70642ce425242e61c8f8f8e97409470fdb6c01b104fe0d9527183e5c51` |

`again.riv` was authored for a swipe-to-delete gesture, not for a spaced-repetition rating; it is
reused here only as a visually "sharp, distinct from confetti" cue for `Rating.Again`, with no
implied semantic match. Replace it with a purpose-built asset once Rive editor access is available
(see `Plans/Kotlin/RiveResearch.md` §6, "Recommended order" step 6, and `FlipCardRivePlan.md` §6).

Both `.riv` files were downloaded verbatim (no edits) into each host's resource directory; the
SHA-256 above is the file as vendored and should match across every host copy.
