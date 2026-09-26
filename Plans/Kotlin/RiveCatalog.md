# Rive animation catalog for Polski Grammar Matrix (v2 research, 2026-09-26)

Scope: R2 (a Rive effect for the flip), R3 (more Rive animations), license check for every candidate.
Read-only research. Nothing in the repo was changed. Working files are in `/private/tmp/claude-501/riv2/`:

| Path | Contents |
|---|---|
| `official.tsv` | all 453 `.riv` paths found in the rive-app GitHub repos (repo, branch, size, path) |
| `dl/` | 218 unique official `.riv` files under 700 KB (7 of them are Git-LFS pointers or old v6 files that fail to load) |
| `mp/` | 96 Rive Marketplace runtime files, downloaded without login |
| `mp_all.json`, `meta.json`, `search/` | raw Marketplace API search results and post metadata |
| `inspect-all.txt`, `inspect-final.txt`, `inspect-mp.txt` | artboards, animations with durations, state machines, inputs, view models and embedded assets |
| `inspect4.mjs` | the improved inspector: lists assets (font, image, audio) and does not hang on images. Run: `node --import ./stub.mjs inspect4.mjs f.riv` |
| `render/` | Playwright + `@rive-app/canvas` 2.43.1 render harness |
| `render/out/*.png` | frames captured after firing each input, over a checkerboard so any opaque background shows |
| `render/frames{0,1,2}.png`, `render/orb.png` | contact sheets of those frames |
| `picks/` | the shortlisted files |

## 0. Findings that affect v1 (fix these first)

1. **`again.riv` (the v1 "Again" cue) draws an opaque scene.** The file is `ui_swipe_left_to_delete.riv`. Its only artboard, "New Artboard" (500x500), paints a teal background with a phone mock-up and list rows. Firing `Trigger Delete` only moves a small teal "x" blob inside that phone. The web bridge (`rive-bridge.js`) and the Android, iOS and macOS overlays show this full artboard over the card for about 2.6 s. That is a teal phone picture on top of the card, not a "sharp cue".
   - Evidence: `render/out/swipeDel_*.png` and `swipeBlend_*.png` (on sheet `frames2.png`).
   - Replace it (see §3, pick A).
2. **`confetti.riv` is a Marketplace file, so it needs CC BY attribution.** v1 credits it only as "MIT (rive-ios repository)". It matches Marketplace post #1456, "Confetti Explosion" by **danny.jamesbuckley** (2021-11-12, not a remix):
   - https://rive.app/marketplace/1456-2840-confetti-explosion/
   - runtime file: https://public.rive.app/community/runtime-files/1456-2840-confetti-explosion.riv

   Evidence of the match:
   - Same artboard `Main` 500x500, same animations `Initial`/`Explosion`, same `State Machine 1` with the trigger `Trigger explosion`.
   - Same payload size: 4590 B after the header.
   - The first 1885 payload bytes are identical. After that, the two animations are stored in a different order.
   - The Marketplace copy has an extra 2-byte file-id varint in its header, which is why its total size is 4599 B (ours is 4597 B).

   Action: add the CC BY 4.0 credit line to `THIRD_PARTY/credits.md` and keep the MIT line. Also note that the effect is small and faint at card size (see `render/out/confetti_*.png`).
3. **The rive-android runtime marks state machine inputs as deprecated.** The legacy `RiveFileController` in 11.12.1 contains the string "State machine inputs are deprecated. Use data binding properties instead." It already supports `autoBind` and `getViewModelInstance`, so data-binding files work through the same legacy `RiveAnimationView` path.

## 1. Licensing (verified)

### Rive Marketplace (Community) files: CC BY 4.0

- **Rive Terms of Service §6c, verbatim** (https://rive.app/docs/legal/terms-of-service):
  > "Community Content. User Content that is marked as Community is accessible to all Users, and may be remixed, modified and used by all Users, including saving as their own User Content within their separate Rive account, under the creative commons license described at https://creativecommons.org/licenses/by/4.0/ ."
- **Rive docs, verbatim** (https://rive.app/docs/community/marketplace-overview):
  > "Marketplace files are all shared under a CC BY license."
- **What CC BY 4.0 requires:**
  - credit the creator
  - link to the source
  - link to the license
  - say whether you changed the file

  Commercial use is allowed. The credit can go in `THIRD_PARTY/credits.md` plus an in-app licenses screen.
- **Downloading without login works.** Every post has a public runtime `.riv` at `https://public.rive.app/community/runtime-files/<post>-<revision>-<slug>.riv`. It returns HTTP 200 with no authentication; I downloaded 96 files this way.
- **Unofficial but working discovery API** (found in the site's JavaScript bundle):
  - `https://api.rive.app/api/community-posts/search?query=<q>&page=<n>`
  - `/api/community-posts?tag=<t>&limit=20&before=<revId>`
  - `/api/community-posts/<id>`
  - `/api/community-posts/featured?page=&limit=`

  Each post's JSON includes `file_url`, `video_url`, `thumbnail_url`, the owner's `username` and `is_remix`.
- **Remixes.** When `is_remix == true`, the credit chain goes back to the original author, which is not always visible. I avoided remixes in the picks.
- **Trademarks.** Many popular community files copy Duolingo characters ("Duo", "Falstaff", "Lily" and similar). CC BY does not license someone else's trademark or character, so these are excluded.

### Official rive-app GitHub example files: MIT (repository license)

MIT repos: rive-android, rive-ios, rive-wasm, rive-react, rive-flutter, rive-react-native, rive-unity, rive-bevy and rive-runtime (checked with `gh api .../license`).

- **Caveat 1:** some of these files started life as Marketplace posts (confetti, shown above; `avatars.riv` = drawsgood's "Avatar Pack" #2195 or #1331). If a Marketplace original exists, credit it as CC BY as well.
- **Caveat 2: `rive-app/rive-use-cases` has no LICENSE file** (the API returns `license: null`), which means all rights are reserved by default. Its files are excluded:
  - `pull_to_refresh_use_case.riv` (also copied into the Rive-CMP sample)
  - `avatar_demo.riv`, `hero_use_case.riv`, and the Google-ads demos

### Editing needs a paid plan

- Rive pricing (https://rive.app/pricing) says the free plan does not support exports; exporting starts at the Cadet tier. We can download runtime files for free, but recoloring, cropping an artboard, removing a background or adding inputs requires re-exporting from the editor, and that needs a paid seat.
- Workaround with no editor: choose files whose artboard is already transparent and needs no edits (every pick below).

### Runtime limits that affect which files we can use

- `@rive-app/canvas-lite` does not render Rive Text or play Rive Audio (its README says so). Files that embed fonts are also heavy: 0.3–10 MB, mostly font bytes. Skip files with text.
- Files that use **Rive Scripting** failed to load their ScriptAssets in the canvas-advanced 2.43.1 inspector ("ScriptAsset doesn't have a generator function ..."). Affected: #27331 Scripted Confetti and #27354 Treasure Chest. Skip them for now.
- Large embedded bitmaps (1–18 MB) are not acceptable for a small overlay.

## 2. Rive capabilities worth using (and which need the editor)

| Capability | What it gives us | Needs editor? |
|---|---|---|
| Legacy SM inputs (trigger/bool/number) | Fire from host code: `fire()`, `.value=`, `fireState`, `triggerInput`, `setNumberState`. All v1 files use these | No |
| Play animation by name, no SM | Works on every runtime: web `animations:[..]`, Android `play("Reveal")`, iOS `RiveViewModel(fileName:animationName:)`. Needed for 1714 "Reveal" and for files without a SM | No |
| **Number input bound to a gesture or to progress** | e.g. `FillPercent` = drag distance / threshold, or flip progress 0..100. The Rive state machine blends or scrubs the timeline, so the host sends one number per frame | No, if the file already has the input |
| **Data binding (ViewModel)** | Typed properties (number, bool, trigger, string, color, enum, list, image) instead of inputs. This is the direction Rive recommends: inputs are deprecated in rive-android 11.x. Web: `autoBind:true` then `rive.viewModelInstance.trigger('correct').trigger()` (verified on #28088). Android legacy: `autoBind` / `controller.getViewModelInstance`. Android Compose (new API) and Apple's new API support *only* data binding | No, to drive existing properties |
| Rive Events (SM → host) | e.g. an "effect finished" event, which would let us drop the host's fixed 2600 ms hide timer. None of the picks emit events; `rating_animation_with_events.riv` and `leg_day_events_example.riv` do | Yes, to add events |
| Listeners (pointer inside Rive) | Must stay **off**: web `shouldDisableRiveListeners:true`, CSS `pointer-events:none`, `.allowsHitTesting(false)` | n/a |
| Blend states (1D, driven by a number) | Smooth scrubbing by a number, e.g. `Swipe to delete BLEND[Swipe Threshold]` or `NumFireworks` | Yes, to create new ones |
| Layouts / Fit.Layout | Responsive artboards | Yes |
| Recolor, crop artboard, remove background | Brand colors, overlay-safe background | **Yes (paid export)** |

## 3. Shortlist (recommended), all licenses confirmed

All picks have transparent backgrounds (verified in the render frames), contain no Rive Text or Audio, have no Scripting, and are at most 60 KB.

| # | Use | File | Source (license) | Size | Artboard / SM / inputs | Look (rendered) |
|---|---|---|---|---|---|---|
| **F** | **Flip effect (R2)** | `interactive_rings.riv` | [rive-ios Demo-App](https://github.com/rive-app/rive-ios/blob/main/Demo-App/RiveExampleSPM/Rive%20Files/interactive_rings.riv), MIT (repo) | 1.5 KB | "New Artboard" 500x500, `State Machine 1[IsExpanded:bool]`; `ExpandEllipses` 0.33 s, then pulsing loops | Two soft lavender rings pulse. With `IsExpanded=true` they expand into a large translucent disk in about 0.33 s; with `false` they contract. Transparent. `render/out/rings_*.png` |
| **A** | Rating feedback: replaces again.riv, and complements or replaces confetti | `2276-4497-checkerror.riv` "Check/Error" by **gytly** | [Marketplace #2276](https://rive.app/marketplace/2276-4497-checkerror/) (CC BY 4.0), original (not a remix), 2022-03-14 | 2.2 KB | "check_artboard" 500x500, `State Machine 1[Check:trigger, Error:trigger, Reset:trigger]`; anims `Filling_circle` 0.5 s, `Check`/`Err`/`End` 1 s, `Loading` loop | A green arc sweeps around, then a green circle with a check (Check, fully drawn at about 1.3 s), or a red disk with a white cross (Error, at about 0.8 s). Transparent. Sheet `frames0.png` rows 2276check and 2276err |
| **B** | Mascot that reacts to answers (correct/wrong), typing and chain done | `28088-53050-ai-orb-mascot.riv` "AI Orb Mascot" by **aln.omrv** | [Marketplace #28088](https://rive.app/marketplace/28088-53050-ai-orb-mascot/) (CC BY 4.0), original, 2026-06-26 | 15 KB (4.1 KB gzip) | Artboard 500x500, `State Machine 1` (no inputs); **ViewModel1(correct:trigger, wrong:trigger, jump:trigger, loadingBoolean:boolean, typingBoolean:boolean)**; anims Idle/Typing/Correct/Wrong/Jump/Reveal | A dark orb with a gradient ring and blinking eyes. `correct` shows a check face, `wrong` a cross face, `jump` a bounce. Transparent. `render/orb.png` (web data binding verified) |
| **C** | Chain/session completion ("Tada") and streak milestone ("Onfire") | `1714-4322-rives-animated-emojis.riv` "Rive's animated emojis" by **JcToon** | [Marketplace #1714](https://rive.app/marketplace/1714-4322-rives-animated-emojis/) (CC BY 4.0), original, 2021-12-20; the author says "you can use in your projects" | 59 KB (19 KB gzip) | Artboards `Tada`, `Onfire`, `joy`, `love`, `Mindblown`, `Bullseye` (800x900 each); `controller[isHover:bool]`; per artboard `Reveal` (3–6 s), `idle` loop, `Hover` | Tada: a party popper shooting a ring and confetti. Onfire: a pink-orange flame that grows. Transparent. Play `Reveal` by name, or set `isHover=true`. Sheet `frames0.png` rows 1714tada and 1714fire |
| D | Swipe-progress meter (R3, number bound to drag) | `riveslider.riv` | [rive-ios Example-iOS/Assets](https://github.com/rive-app/rive-ios/tree/main/Example-iOS/Assets), MIT (repo) | 510 B | "New Artboard" 500x60, `Slide[FillPercent:number]` 0..100 | A grey track fills blue with a white knob. Transparent, plain. `render/out/slider_*.png` |
| E | Empty state "всё повторено" | `robo_dude.riv` | [rive-unity examples/basic](https://github.com/rive-app/rive-unity/tree/main/examples/basic/Assets), MIT (repo) | 7.9 KB | "Robot" 300x484, `StateMachine[idle:bool, lookCenter/lookLeft/lookRight/bounce:trigger, flying/bladeSpinFast/Jetpack/bladeSpinOff:bool]` | A teal propeller robot that bounces, looks around and flies. Transparent. `render/out/robo_*.png` |

### Why these, and how to sync them

**F. Flip.** No free file is designed as a card-flip VFX. The Marketplace "card flip" files (#9841, #10278, #18386, #9386, #3548) are whole Rive-drawn cards with an opaque background; they are not overlays. We keep the native flip so the card text stays real and accessible.

- Sync plan:
  - Set `IsExpanded=true` at the start of the first half (0→90°, about 250 ms). The 0.33 s expansion peaks around the 90° swap.
  - Set `IsExpanded=false` when the second half ends. The rings contract as the card lands.
  - With reduced motion, create nothing.
- It is a subtle ripple or glow behind the card and costs 1.5 KB.
- **Better result (needs the paid editor):** a custom `flip_fx.riv` with a number `progress` 0..100 (or a data-binding number) that the host feeds with the real rotation angle. At 0→50 a glare sweeps across the card; at 50 a sparkle burst on the edge-on frame; at 50→100 the glare fades.
  - Donors to remix with credit: #1920 "Start" by JcToon (the `Demo_effect` star burst, 4.7 KB, but its background is opaque) and #1477 "Check with sparkling" (particles).

**A. Rating feedback.** It means exactly Remembered (✓) and Again (✗), is 2.2 KB, and is transparent.

- It fixes the v1 problem where again.riv draws the teal phone.
- Keep confetti for Remembered if you want a double reward: fire `Check` together with confetti. For Again, fire `Error` only.
- Fire it at `AppAction.Rate`. It starts drawing within about 150 ms (see the frames).

**B. Mascot.** This is the one pick that uses data binding. It proves the path Rive recommends (inputs are deprecated), and it maps directly to app state:

| App state | Mascot property |
|---|---|
| typed-answer field focused | `typingBoolean` |
| Remembered | `correct` |
| Again | `wrong` |
| chain completed | `jump` |
| loading | `loadingBoolean` |

- Place it in the session header or on the progress card, not over the card text.
- **[verify on hosts]:**
  - Android: legacy `RiveAnimationView` with autoBind, then `controller.activeArtboard` → `ViewModelInstance.getTriggerProperty("correct").trigger()`.
  - iOS/macOS: RiveRuntime 6.27, `RiveViewModel.riveModel?.enableAutoBind` / `viewModelInstance?.triggerProperty(fromPath:)`. The names come from rive-ios 6.x docs and were not compiled here, so this is **[speculation]** until built.

**C. Celebrations.** One 19 KB-gzip file covers both "chain complete" (Tada) and "streak milestone" (Onfire), plus spare emojis (joy, love, Mindblown) for the backlog. Play `Reveal` once and hide when it ends. No trigger input exists, so use play-by-name or `isHover=true`.

**D and E** are functional but plain. Build them after F, A, B and C.

## 4. Full candidate catalog

Abbreviations: T = transparent background, O = opaque background, Txt = contains Rive Text/fonts.

### 4.1 Flip / card

| File | Source, license | Size | Inputs | Notes |
|---|---|---|---|---|
| 9841 Card flip demo (drawsgood) | [MP](https://rive.app/marketplace/9841-18767-card-flip-demo/) CC BY; the same "Interaction" artboard is also in rive-unity `nestedinputtest.riv` (MIT) | 9.9 KB | `isFlipped:bool`; flip / flip back 0.25 s | O (black); a whole pixel-art playing card. Good reference for a Rive-drawn flip, not an overlay |
| 10278 Card Flip (ribat.algozi) | MP CC BY | 95 KB | `Flip Card:trigger` | O; tarot-style card art |
| 18386 Card Flip (tonazar) | MP CC BY | 139 KB, 125 KB image | `Trigger 1` | O, bitmap card |
| 9386 8Bit Deck (drawsgood) | MP CC BY | 18 KB | `next:trigger, flipped:bool, holding:bool` | O; card deck |
| 3548 Flip Button (JcToon) | MP CC BY | 12.6 KB | `Pressed:trigger` | O; a button flipping Back/Continue with bones |
| 3515 Holo card (drawsgood) | MP CC BY | 13.6 KB | `follow strength:bool` | O; holographic reflection that follows the pointer. Idea for a card glare |
| 7905 Card Shuffle (cwthrs) | MP CC BY | 15 KB | hover triggers | O; fan of cards |
| 24847 3D Book page flip (Tom_acco) | MP CC BY | 8.9 MB (images, audio) | VM `Book(Next/Back/PageFipped)` | Too heavy |
| 22198 Fake 3D cards (isaganttus) | MP CC BY | 520 KB, images | VM | Too heavy |

### 4.2 Celebration / burst

| File | Source, license | Size | Inputs | Notes |
|---|---|---|---|---|
| confetti.riv (v1) = 1456 Confetti Explosion (danny.jamesbuckley) | rive-ios MIT + MP CC BY | 4.6 KB | `Trigger explosion` | T; small and faint at card size |
| 8907 Colorful confetti (okazu, remix) | MP CC BY | 6.7 KB | same names as v1: drop-in | T; still faint; a remix |
| 13279 Confetti Explosion (zenzuke) | MP CC BY | 7.5 KB | `ConfettiExplosion[Explode]`, 3 s | **O (#333 rect)**, vivid. Needs the editor to remove the background |
| 12079 / 13341 / 13588 confetti | MP CC BY | 232 KB | no inputs, loop | Lottie-converted, heavy |
| 12317 Fireworks (JcToon) | MP CC BY | 8.4 KB | `NumFireworks:number` 0..100 (blend) | O (black). Nice number-driven "cards reviewed" celebration if the background were removed |
| 17836 Party popper (mograbear) | MP CC BY | 28 KB | `Click:trigger` | O, drawn "CONGRATULATIONS" |
| 1118 Party Popper (JcToon) | MP CC BY | 4.7 KB | anim only | O (purple) |
| 1920 Start (JcToon) | MP CC BY | 4.8 KB | `Demo_effect[Recreate]` | O (dark); pink star burst with a ring. Good flip-burst donor |
| birb.riv `starSingle` | rive-wasm MIT | 412 KB (whole file) | anim `starBurst` | T; a nice yellow star pop, but it lives in a 412 KB file full of bitmaps |
| 16104 Diamond, 22340 Diamond reward | MP CC BY | 207 / 90 KB | trigger | O |
| 1714 Tada / Onfire | **pick C** | | | |

### 4.3 Rating / answer feedback

| File | Source, license | Size | Inputs | Notes |
|---|---|---|---|---|
| **2276 Check/Error** | **pick A** | | | |
| 14742 Loading to success/failure | MP CC BY | 2.9 KB | `payment failed/successful:bool` | T (not rendered here; the thumbnail shows a red ✕ on white). A fallback for A |
| 368 Checkmark / 369 Error icon (jpereira) | MP CC BY | 2.7 / 0.6 KB | anim `show` only | Thumbnail shows a light background |
| 1029 Success Check (guidorosso) | MP CC BY | 2.5 KB | anim only | Particle trim paths |
| 1477 Check with sparkling | MP CC BY | 2 KB | anim only | O (dark) |
| 405 / rating.riv / rating_animation*.riv (JcToon) | MP CC BY; rive-android MIT | 15.6 KB | `rating:number` 0..5 | O (dark); 5-star bar |
| 3145 Star Rating (guidorosso) | MP CC BY | 31 KB | `Rating:number` | O |
| 2195 / avatars.riv Avatar Pack (drawsgood) | MP CC BY; rive-react MIT | 14.8 KB | `isHappy/isSad:bool` | T (the circle is part of the art). Happy/sad face reaction: an alternative mascot |
| 28088 AI Orb | **pick B** | | | |
| 12335 Robocat (setyosn) | MP CC BY | 382 KB (Rubik font) | `No Internet/Error/Chat:bool, Download:number` | O, Txt |
| 18720 Robot expressions | MP CC BY | 443 KB, font | `Expressions:number` | Txt, heavy |
| 25712 Owl mascot pack, 26964 Cloud mascot (AnggaMotion) | MP CC BY | 219 / 162 KB | none (per-artboard loops) | T; loops only, no reactions |

### 4.4 Streak / fire / progress

| File | Source, license | Size | Inputs | Notes |
|---|---|---|---|---|
| 1714 Onfire | **pick C** | | | |
| 15369 streak-normal (team-CQBTP) | MP CC BY, **remix** | 33.6 KB | anims `Normal`/`Fire` | T; a flame with a heart. The remix chain is unclear, so skip |
| 17443 Flame icon (alexwright) | MP CC BY | 21 KB | `surprise me, flamePressed:bool` | A dark app-icon square; a flame face |
| 3703 Fire Button (JcToon) | MP CC BY | 4.6 KB | `ON, Hover:bool` | O (dark) |
| 27337 Dynamic streak fire | MP CC BY | 286 KB | VM `streak:number` | Txt (Nunito, 277 KB): the digits are Rive Text, which canvas-lite does not draw |
| 24807 Daily streak / 17936 Streak Days | MP CC BY | 0.9 / 0.75 MB | VM / `%:number` | Heavy, Txt |
| riveslider.riv | **pick D** | | | |
| energy_bar_example.riv = 586 Energy Bar (JcToon) | rive-ios MIT + MP CC BY | 8.2 KB | `Energy:number` 0..100 | O (magenta) |
| life_bar.riv | rive-ios MIT | 4.7 KB | 5 bools | Heart bar |
| liquid_download.riv / 1030 Loader icon (guidorosso) | rive-flutter MIT + MP CC BY | 7 KB | `Progress:number`, `Download` trigger | O (blue) |
| 451 Flame loading bar (Bobbeh) | MP CC BY | 7.4 KB | `Load%:number` | O; a flame running along a bar. Great look, needs background removal |
| 715 Water bar | MP CC BY | 69 KB | `Level:number` | O |

### 4.5 Swipe progress (number bound to drag)

| File | Source, license | Size | Inputs | Notes |
|---|---|---|---|---|
| ui_swipe_left_to_delete.riv (v1 again) | rive-android/ios MIT | 5.4 KB | `Swipe Threshold:number` (+ a BLEND SM) | **O: teal phone mock-up.** Unusable as an overlay |
| alligator_swipe.riv / 4463 (pedroalpera) | Rive-CMP sample (Apache), MP CC BY | 9.6 / 130 KB | `scroll` / `SliderNumber:number` | O; phone mock-up |
| 4462 Swipe example | MP CC BY | 1.1 MB images | `Swipe Direction:number` | Heavy |
| 5277 Swipe Animation | MP CC BY | 42 KB | VM `mouseXPosition:number` | Scene with a phone |
| pull_to_refresh_use_case.riv | rive-use-cases, **no license** | 26.6 KB | `pull:number` | Excluded |
| 3146 Pull to Refresh (JcToon) | MP CC BY | 15 KB | `numDrag, numLoad:number` | Scene, O |
| riveslider.riv | **pick D** | | | |

### 4.6 Empty state / mascot / loading

| File | Source, license | Size | Inputs | Notes |
|---|---|---|---|---|
| robo_dude.riv | **pick E** | | | |
| cute_robot.riv = 3364 Cute Robot (JcToon) | rive-unity MIT + MP CC BY | 20 KB | `Skin:number, Fly:trigger` | Flying robot |
| skull_404.riv | rive-flutter MIT | 10.7 KB | loop | T; flaming skull "404". Wrong mood |
| Zombie_Character.riv | rive-flutter MIT | 16.5 KB | `Hit, In` | Wrong mood |
| 2492 Impatient placeholder | MP CC BY | 9.4 KB | loop | O (light grey) |
| 5102 fold_er | MP CC BY | 93 KB | `isOpen:bool` | O (grey) |
| mascot.riv | rive-android/ios MIT | 28 KB | none (one 16.7 s loop) | Ambient only |
| 1714 joy / love | pick C file | | | Reuse for empty state |

### 4.7 Vocabulary / icons

| File | Source, license | Size | Inputs | Notes |
|---|---|---|---|---|
| clean_icon_set.riv | rive-ios MIT | 10 KB | per-icon `active:bool` (STAR, BELL, TIMER...) | T, 64x64. Tab icons, "saved" star |
| explore.riv AddToWatchlist | rive-ios MIT | 4.4 KB | VM `isSelected:boolean` | Bookmark toggle for "save word" (data binding) |
| 336 Bookmark interaction (Bobbeh) / 18234 Bookmark | MP CC BY | 5.9 / 11 KB | `Marked` / `ON/OFF:bool` | O (pink / grey) |
| 340 Like button (guidorosso), 6845 Like explosion, 375 Light like (JcToon) | MP CC BY | 2–21 KB | bools | Favourites |
| web_icons_pack.riv / travel_icons_pack.riv | rive-flutter MIT | 7 / 42 KB | `active:bool` | Icons |

## 5. Ranked backlog (not in the first batch)

1. A custom `flip_fx.riv` with data binding (a `progress` number plus a `burst` trigger), a brand palette and a transparent background. Needs a Cadet export seat. Replaces F.
2. Remove the backgrounds of 451 Flame loading bar and 12317 Fireworks (editor). Then use them for session progress (`Load%` = done/total) and for the end-of-session celebration (`NumFireworks` = cards reviewed).
3. Use riveslider (D) as a swipe-release meter under the card: `FillPercent = min(100, |dx| / 75px × 100)`.
4. Use robo_dude (E) or 1714 `joy` on the "всё повторено" empty state.
5. explore.riv (data binding) as a "save word" toggle in the vocabulary screen.
6. Add Rive Events ("done") to our own files, so hosts hide the overlay when the effect ends instead of after a fixed 2600 ms.

## 6. Integration notes (per host, for the Developer)

- **One overlay, many files.** Keep one canvas/view per host and swap the `src`/artboard per effect (as `rive-bridge.js` does now). Use `Fit.Contain`. Put the overlay above the card with `pointer-events:none`, `aria-hidden`, `invisibleToUser`, `.accessibilityHidden(true)`, `.allowsHitTesting(false)`. With reduced motion, instantiate nothing.
- **Pick A:** SM `State Machine 1`, fire `Check` or `Error`; hide after about 1500 ms.
- **Pick F:** SM `State Machine 1`, bool `IsExpanded`, set at the flip-half boundaries.
- **Pick C:** `artboard: 'Tada' | 'Onfire'`, `animations: ['Reveal']` (web). On Android use `setRiveResource(R.raw.emojis, artboardName = "Tada", animationName = "Reveal")`. On Apple use `RiveViewModel(fileName: "emojis", animationName: "Reveal", artboardName: "Tada")`.
- **Pick B (web, verified):** `new rive.Rive({ src, stateMachines: 'State Machine 1', autoBind: true })`, then `r.viewModelInstance.trigger('correct').trigger()` / `.boolean('typingBoolean').value = true`.
- **Credits.** Add to `THIRD_PARTY/credits.md`: title, author, Marketplace URL, "CC BY 4.0 (https://creativecommons.org/licenses/by/4.0/), unmodified" and the SHA-256:
  - 2276: `f9c21d280f85a985d127ed1d9c6ec9dbd9574cb66289c9ad208cba08597d028b`
  - 28088: `eccafb28f0f7f1f5a1d332e5336f71ed4d8069df55049e64cf675247e7fadc5b`
  - 1714: `57741d5f290b3e34f92f69936a16759ecec8d01b40cb065839e784c832cd24ea`
  - interactive_rings: `433dfddefc53917bb19e477d2b91188b56f75d9d68aa2a617fe3bd3bb8d408a0`
  - riveslider: `57f2756f89c6e9e770dca46693549c3e0fef94bf29785e5424d1b3b96b8c2675`
  - robo_dude: `9652d212adcd5883be055b6d96498ba908ada27a7ae4d0ee2fc5888d1c782835`
  - plus the corrected confetti line (1456, danny.jamesbuckley)
