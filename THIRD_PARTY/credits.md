# Third-party credits: Rive card-flip and rating effects

Vendored per FlipCardRivePlan.md FC-15 (v1) and FC2-06/09/10/12 (v2), for the card-flip/rating/
celebration effects. Files are copied per host (see each host's own `rive/` resource directory);
this file records provenance once for the whole repository, as the plan asks.

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
| `confetti.riv` — "Remembered" burst | [rive-ios Demo-App](https://github.com/rive-app/rive-ios/blob/main/Demo-App/RiveExampleSPM/Rive%20Files/confetti.riv) (MIT, repository copy); the same artwork is also published on the Rive Marketplace as ["Confetti Explosion" #1456](https://rive.app/marketplace/1456-2840-confetti-explosion/) by **danny.jamesbuckley** (2021-11-12, not a remix) | MIT (repository) **and** CC BY 4.0 (https://creativecommons.org/licenses/by/4.0/, Marketplace original) — both apply; state machine "State Machine 1", trigger "Trigger explosion" | `efdbdcb87ef4a9e937e4dd7e1c7f112de651b10fea3b5667921cfeb8f5eae49d` |
| `again.riv` — "Again"/"Remembered" rating cue (v2, FC2-09: **replaces** the v1 `ui_swipe_left_to_delete.riv`, which drew an opaque teal phone mock-up over the card — see `Plans/Kotlin/RiveCatalog.md` §0.1). **Web, Android and iOS in this pass** — macOS still ships the v1 file at this path; its replacement is tracked separately, not done here. | ["Check/Error" #2276](https://rive.app/marketplace/2276-4497-checkerror/) by **gytly** (2022-03-14, not a remix) | CC BY 4.0 (https://creativecommons.org/licenses/by/4.0/), unmodified | `f9c21d280f85a985d127ed1d9c6ec9dbd9574cb66289c9ad208cba08597d028b` |
| `rings.riv` — flip-in-progress cue (v2, FC2-06/R2), renamed from `interactive_rings.riv` | [rive-ios Demo-App](https://github.com/rive-app/rive-ios/blob/main/Demo-App/RiveExampleSPM/Rive%20Files/interactive_rings.riv) | MIT (repository), state machine "State Machine 1", boolean input "IsExpanded" | `433dfddefc53917bb19e477d2b91188b56f75d9d68aa2a617fe3bd3bb8d408a0` |
| `chain-complete.riv` / Android `chain_complete.riv` (same bytes, underscore for a valid Android resource name) — session-completion celebration (v2, FC2-10/R3, "Tada" artboard only), renamed from `1714-4322-rives-animated-emojis.riv` | ["Rive's animated emojis" #1714](https://rive.app/marketplace/1714-4322-rives-animated-emojis/) by **JcToon** (2021-12-20, not a remix; author states "you can use in your projects") | CC BY 4.0 (https://creativecommons.org/licenses/by/4.0/), unmodified — only the `Tada` artboard's `Reveal` animation is used; the file also contains `Onfire`/`joy`/`love`/`Mindblown`/`Bullseye` artboards, kept as backlog (`RiveCatalog.md` §5) | `57741d5f290b3e34f92f69936a16759ecec8d01b40cb065839e784c832cd24ea` |

`again.riv`'s v1 asset (`ui_swipe_left_to_delete.riv`) was authored for a swipe-to-delete gesture,
not for a spaced-repetition rating, and — worse — painted an opaque scene over the card instead of
a transparent cue (confirmed defect, `RiveCatalog.md` §0.1). It has been replaced with the
purpose-built "Check/Error" asset above, whose `Error` trigger plays for `Rating.Again` and whose
`Check` trigger plays alongside `confetti.riv`'s `Trigger explosion` for `Rating.Good`/Remembered
(FC2-09; see `FlipCardRivePlan.md` §12.7 for the "together, not instead" rationale). Android plays
both together the same way (two stacked `RiveAnimationView`s in `AndroidRiveOverlay.kt`), not
confetti alone, for host parity.

All `.riv` files were downloaded verbatim (no edits). `confetti.riv` is vendored identically on
every host (SHA-256 above matches all four copies). `rings.riv`, `chain-complete.riv` and the new
`again.riv` (Check/Error) are vendored for **web, Android and iOS** as of this pass (byte-identical
to the web copies at every host — verified by SHA-256 before/after copying, see
`FlipCardRivePlan.md` §15's evidence log for the iOS copy); porting them to macOS is tracked
separately, not done here.
