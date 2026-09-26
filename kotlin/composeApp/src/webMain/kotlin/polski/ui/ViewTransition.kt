package polski.ui

/**
 * UX4-16..19: runs [run] (a route's DOM rebuild) inside `document.startViewTransition` when the
 * browser supports the API and motion isn't instant ([motionInstantActive] — the caller checks
 * that; this function's own per-target implementation only checks *browser support*, not the app
 * setting), so browser Back/Forward, keyboard and click navigation all animate the same way.
 * Falls back to calling [run] directly — today's plain, already-correct behaviour — on any
 * browser without the API. One `expect` per FC-18/17.8.3's own precedent: avoid a single shared
 * `dynamic`/`external` declaration whose JS interop shape might not hold on both the JS and Wasm
 * targets; each target implements this the way that is idiomatic for it instead.
 */
internal expect fun withViewTransition(run: () -> Unit)
