# NJ06 browser writer exclusion and migration transaction

Status: **architecture plan only** at product `cc3cb98`. No browser migration,
cross-tab exclusion, deployment policy, or host acceptance is implemented or
proved here. Use after the NJ05 wire and document contracts are independently
selected and frozen. The existing independent state requirements are
[`transaction.cases`](../../tests/fixtures/textwire/transaction.cases) T01–T19;
this plan adds browser-specific execution requirements without changing their
expected results. The sole old JSON allowance is input-only, read by the
one-time legacy decoder. New saves, backups, receipts, downloads and tests'
expected outputs use the chosen text wire.

## Observed owners and collision

| Surface at `cc3cb98` | Existing behavior and necessary change |
| --- | --- |
| React `src/progress/storage.ts`, `src/ui/App.tsx` | `/react/` reads and writes `polski-grammar-srs-v1`; ratings and reset write/remove it, and exports emit JSON. Its already loaded script has no new-version exclusion protocol. Replace active React writes/exports with the selected text document or retire that published writer; keep the old key as read-only migration input. |
| React `src/vocabulary/storage.ts`, `src/ui/VocabularyView.tsx` | `polski-vocabulary-pl-ru-v1` is JSON storage and export; existing state is held in a tab-local React component and saves have no cross-tab compare. This is also the KMP pl-ru vocabulary key. Preserve old bytes and both card directions/custom items. |
| React `src/ui/App.tsx` | `polski-explanation-method-v1` is a plain-string legacy preference key, not a JSON document; its effect writes after mount. Include it in the old-key inventory and retire its writer after text preferences commit. |
| KMP `kotlin/shared/src/commonMain/kotlin/polski/progress/ProgressRepository.kt`, `kotlin/composeApp/src/webMain/kotlin/polski/platform/WebProgressRepository.kt` | `RawProgressRepository` uses JSON at `polski-grammar-srs-kmp-preview-v1` plus JSON `polski-grammar-srs-kmp-legacy-backup-v1`, a string marker `polski-grammar-srs-kmp-migrated-v1`, and the React key. `BrowserKeyValueStore` only offers `get`/`put`; the new host store needs deletion, exact-byte readback, per-key inventory, receipt and lock boundary. The old preview is also a legacy input, never a new text destination. |
| KMP `kotlin/composeApp/src/webMain/kotlin/polski/ui/VocabularyWeb.kt`, common `VocabularyDocument.kt` | `VocabularyCodec.key` is `polski-vocabulary-${pairId}-v1`; browser backup is `polski-vocabulary-${pairId}-v1-backup`. The `storage` listener only reloads other tabs; `writeDocument` reads/writes without exclusion and can restore a stale previous value after another tab's success. Replace both storage and export path; enumerate actual pair IDs before cleanup. |
| KMP `kotlin/composeApp/src/webMain/kotlin/polski/platform/WebPreferencesRepository.kt`, `ui/WebPreferencesController.kt` | `polski-preferences-v1` and `polski-preferences-v1-backup` hold JSON, and load of the plain method key can call `save` during construction. New preferences must be admitted before this side effect and before cold-start course selection. |
| Host startup `kotlin/composeApp/src/{jsMain,wasmJsMain}/kotlin/polski/main.kt`, `webMain/.../TrainingWebApp.kt` | Both JS and Wasm start `TrainingWebApp`; it constructs preferences, `TrainingDomRenderer` constructs `VocabularyWebController` and reloads, and `LaunchedEffect(store)` starts progress. Put the migration/read-only gate before these repository/UI constructors or make each constructor side-effect-free until the gate resolves. Do not let a default preference/progress document write over unreadable old bytes. |
| Site `.github/workflows/deploy.yml` | GitHub Pages publishes KMP JS+Wasm at the site root and a built React rollback copy at `/react/`. They share the same origin and `localStorage` despite distinct paths. A repository search found no service-worker registration or worker asset in these first-party entrypoints; no worker-controlled upgrade can be assumed. Removing `/react/` in a later upload does not stop a tab that already loaded its JavaScript. |

Freeze a **per-document exact legacy-key manifest** before implementation, including
the React key, KMP preview/backup/marker, each supported vocabulary pair and
backup, the preferences primary/backup and method key. Any present unknown
legacy-looking key cannot be deleted by a wildcard sweep: classify it or enter
recovery. Never interpret a successful new document as permission to ignore a
leftover old key. Keep text-key names and receipt schema in the NJ05/NJ06 single
source; this table records existing facts, not another mutable schema.

The KMP progress `PREVIEW_KEY` can contain reviews newer than the React
`LEGACY_KEY`; its marker/backup are provenance, not proof that the two live
documents are semantically equal. Freeze accepted old `RawProgressRepository`
states and the exact expected winner/recovery outcome **independently** before
adapting them. A matching old marker/backup may establish the valid preview as
the current progress source; a broken chain, divergent React rewrite or two
plausible current documents requires recovery, never a silent choice of the
React key or an empty document. Likewise, a vocabulary import backup can hold
an older valid document: differing bytes do not justify silent deletion.
The candidate receipt in `TextWireDesign.md` names one legacy path/hash, which
does not by itself authorize deletion of several distinct old KMP keys. Freeze
an explicit ordered key→hash manifest in the receipt contract (or a separate
verified receipt per old key) and add independent multi-key conflict cases
before implementing cleanup. A key not covered by that proof stays untouched.
For browser `localStorage`, hash the exact retrieved legacy value under a
specified code-unit/byte mapping; malformed Unicode must remain recoverable
without lossy replacement during hashing or export.

## Enforceable new-version protocol

The web adapter owns physical exclusion; common KMP document logic owns typed
validation, semantic equality and the decision states in `transaction.cases`.
React and KMP JS/Wasm, while both remain published, must invoke the **same**
per-document cross-tab protocol for every migration, normal save, import and
reset. Do not add Web Locks, DOM, origin policy or Pages knowledge to Biome Core,
the text codec, FSRS or the game model (P9/P10). The new app must fail closed if
the chosen exclusion capability is absent or storage throws; it must not fall
back to a `localStorage` lease, timer, `BroadcastChannel`, or a `storage` event.

1. Select and prove one browser exclusion mechanism in the required Chromium,
   Firefox and WebKit production paths, including KMP JS and Wasm interop. A
   named exclusive [Web Lock](https://www.w3.org/TR/web-locks/) per document is a
   candidate **only for cooperating new code**. Hold it through source/new/receipt
   reads, encode, new-key write/readback, receipt write/readback, matching-source
   deletion and absence verification. Use a stable name shared by React/KMP;
   acquire document locks in a fixed order when an action spans documents.
   Cancellation or tab close releases the lock but does not roll back committed
   keys, so the receipt protocol remains necessary. If the target matrix or
   JS/Wasm bridge fails, evaluate an IndexedDB authority with read/write
   transactions as a separate bounded design; an IndexedDB transaction cannot
   make legacy `localStorage` changes atomic or constrain old tabs. Do not call
   a cross-tab lock selected merely because an API exists in one browser.
2. After acquiring the lock, **re-read** old primary/backups/marker and new
   document/receipt from storage. Validate exact hashes and versions; compare
   the previous revision/hash carried by the pending UI action with the current
   new document. If another new tab advanced it, reject the stale commit and
   reload/replay the semantic action once against the fresh document only where
   the action ID, exercise ID and merge rules make that replay safe; otherwise
   present recovery. In particular, do not replay a rating or custom-word add
   blindly and do not restore a stale backup over a peer's committed text. The
   current in-memory pending work remains available for text export on failure.
3. **Only after the host policy below positively excludes old writers**, for
   a sole old input with no valid new document, strictly decode the old
   bytes read under the lock and compare every known and representable unknown
   field against the new model. Use a distinct new text key. Write, read back,
   verify digest and semantic equivalence, then write/read back the **text**
   receipt. Before deleting any old primary or backup, re-read each exact byte
   sequence and require the receipt's old hashes. Delete only matching named
   keys, verify absence, then expose normal `Ready`/save. Without that exclusion,
   the new app may inspect old data and offer recovery, but does not delete it
   or claim a committed automatic migration. A changed old key,
   missing/malformed/mismatched receipt, stale new hash or any fault yields
   `RecoveryRequired` and preserves all surviving old and new bytes. A valid
   new text document with old bytes but no matching receipt is T02/T03/T04:
   **no write, delete, ordinary save or implicit winner**. An exact receipt
   after a crash permits only idempotent cleanup of matching old bytes (T05).
4. On every new-version load, resume, cross-tab notification and **before each
   ordinary commit**, check for reappeared legacy keys under the exclusion
   mechanism. A reappeared value is T15 recovery; retain both its bytes and the
   valid new text bytes. Keep `storage`/`BroadcastChannel` only as prompt UI
   invalidation; the writer's own tab gets no `storage` event, and events may be
   delayed while another document is inactive. Re-read under the lock rather
   than trusting a notification. Every normal text save must use revision/hash
   compare to avoid two new tabs losing a review or custom word (T16).

No sequence of `localStorage` read/check/delete operations is atomic against an
**uncooperative old tab**. The [HTML storage standard](https://html.spec.whatwg.org/multipage/webstorage.html)
explicitly cautions authors to assume no cross-window locking; its storage
event is sent to other storage holders, not the writer. The
[Web Locks](https://www.w3.org/TR/web-locks/) manager only orders callers of
that API; the current React and KMP bundles do not call it. A legacy writer can
change old bytes between the final hash check and deletion, or write again
after cleanup. The new app can detect an **observed** rewrite and preserve a
conflict; it cannot make deletion lossless while that writer may still run.

## Release/host policy: required writer exclusion

**Block destructive migration and a literal zero-application-JSON release until
the old writer is excluded.** A new bundle, a user prompt to close tabs, a
`BroadcastChannel` heartbeat, a `storage` listener, a Web Lock held only by new
tabs, a wait period, a Pages asset update or an unregistered service worker is
not evidence that every already-open old tab has stopped. An existing tab can
remain loaded without requesting new assets. The present Pages artifact also
actively publishes `/react/` as a JSON writer, so merely switching the root
does not meet the requirement.

The production decision must explicitly choose and prove one of these scopes:

* **Managed same-origin cohort:** before cleanup, positively close/reload every
  old writer in that controlled browser profile, remove the `/react/` JSON
  writer and all old JSON-emitting assets from the served artifact, then execute
  the old/new-tab adversarial acceptance below. Record how the cohort was
  enumerated and excluded. This is not presently enforceable for arbitrary
  public GitHub Pages visitors and must not be generalized from a test profile.
* **Origin isolation with explicit legacy transfer:** host the new text-only app
  at a different origin, make old data import an explicit read-only file/input
  action, and retire the old Pages writer separately. Browser storage is
  origin-scoped, so the new origin cannot silently auto-read the prior origin's
  `localStorage`. This changes the automatic browser migration experience and
  needs a separately accepted requirement and user journey. It still cannot
  erase code already executing in an old origin tab.

For the current unmanaged same-origin Pages deployment, a universal claim that
**no previously loaded old tab can emit JSON** is unprovable by new app code.
If the owner requires that literal guarantee for every visitor, NJ06/NJ08
remain a release blocker until an enforceable host/cohort policy exists. The
weaker attainable claim is that new-version sessions are serializable with one
another and fail closed on *observed* old-key conflict; label that partial,
never a completed zero-JSON migration. No telemetry, hidden-tab count or
service-worker mechanism is present in this repository to strengthen it.

## Freeze and execute acceptance before integration

Independent Tester freezes exact-path byte-hashed old-user input fixtures,
expected typed values and old/new byte hashes before the first host change;
they are read-only migration-test inputs excluded from shipped bundles (P15/P16).
Use the selected NJ05 text decoder/domain documents; codec conformance alone
does not prove browser transactions. Run a production-built served site in
Chromium, Firefox and WebKit, selecting **both** KMP Wasm and JS fallback; where
React remains, exercise its built path too. Record source and served-asset
hashes, browser/version, selected/executed/skipped tests, old/new/receipt bytes
before and after, and every injected operation (P17). Playwright WebKit is not
an iOS Safari/device substitute. Existing `webTest`, `jsBrowserTest` or
`wasmJsBrowserTest` alone does not close this UI/storage gate.

| Browser case, frozen before code | Required observation |
| --- | --- |
| T01/T14 and old progress/vocabulary/preferences variants | No legacy gives fresh text only; unsupported/corrupt/truncated/unconvertible old bytes block before a default constructor/effect saves. Preserve exact old bytes, all known/unknown fields, local-day and early preferences pack choice. Freeze the old KMP progress preview/marker/backup state with newer preview reviews separately from raw React progress, plus divergent vocabulary import backups. |
| T02–T04/T07/T19 | Valid new plus old without correct receipt, divergent backup or wrong legacy-path hash: both byte sequences and receipt stay untouched; all UI writers/ordinary saves disabled; explicit recovery visible. |
| T05/T06/T12/T13 | Pause after each new write/readback, receipt write/readback and each old-key deletion; reload in either JS/Wasm. Matching receipt resumes matching cleanup once; pre-receipt coexistence recovers; delete failure preserves new document and reports remaining old key. |
| T08–T11/T17/T18 | Inject quota/security errors, disabled storage, thrown `getItem`/`setItem`/`removeItem`, returned stale/truncated values, malformed checksum, cancellation and tab kill. Assert no old-byte loss, no JSON output, no ordinary save, retry from current storage state. |
| T16 two cooperating tabs | Pause one tab after it reads old A and let the other commit B; losing tab cannot overwrite B/delete changed legacy, and once reloaded a review/custom word is applied at most once. Repeat with normal text saves, different document locks, tab close, frozen/background tab and JS↔Wasm tabs. |
| T15 uncooperative old tab | Hold the **actual pre-migration production React bundle** at `/react/` and the old KMP JS/Wasm root bundle in separate tests. In controlled fault injection, rate/add a word/reset/change settings in the old tab at read, text-write, receipt and immediately before delete, stopping cleanup before destructive deletion. In a separate controlled post-cleanup simulation, make the old tab write C after valid B commits. On observed rewrite, preserve C and B, show recovery and block zero-JSON claim. The before-delete interleave must demonstrate why no new lock prevents C from being deleted; it is an expected blocker, not a test to weaken. |
| Release artifact and origin | Fetch the built root and `/react/` routes on the same test origin and classify all currently served JSON emitters/old bundles, worker registrations, cache behavior and redirects. Under the chosen policy, prove old writer exclusion with a concrete controlled cohort or record unmanaged Pages as NOT PROVEN; never infer this from app unit tests. |

Fault injection belongs at the **web storage boundary** with explicit get/put/
delete/readback hooks and at tab scheduling points, not as a fake success return
from the domain codec. The independent expected outcome for each injected
boundary follows T01–T19. Reviewer checks P7 single source, P9/P10 ownership,
P15 independent expectations, P16 fixture/artifact freshness, P17 actual hosts
and P18 unchanged gates. Current status of every row above: **NOT RUN**.
