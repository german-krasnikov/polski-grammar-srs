package polski.ui

import kotlinx.browser.window
import org.w3c.dom.HTMLElement

private const val SLIDE_DURATION_MS = 320
internal const val SLIDE_EASING = "cubic-bezier(.2,0,0,1)"

/**
 * UX5: phone-style tab paging, replacing the View Transitions crossfade (dropped after measuring
 * it — see FlipCardRivePlan.md §19/tabs.json: a 4x-CPU-throttled control run with
 * `document.startViewTransition` deleted before load was just as janky as the one using it, so the
 * dominant cost was never the API itself). Both layers are plain siblings under [viewport]
 * (`.route-viewport`, always mounted, `display:grid` in the stylesheet with every `.route-content`
 * pinned to the same `1/1` cell — the "CSS grid stack" trick): neither layer is ever given
 * `position`, so the shared grid row keeps auto-sizing to whichever of the two is currently
 * taller for the whole transition, never clipping a taller incoming route or leaving a gap under
 * a shorter one (correction round — a `position:absolute` incoming layer sized only by outgoing
 * did both). Only `transform` (and, once, the `overflow` needed to clip the horizontal slide)
 * changes for the transition's whole duration, so both layers stay on their own compositor layer,
 * still cheaply, without any JS height measurement. Committing the "from" frame before the
 * transition can animate to the "to" one uses two `requestAnimationFrame`s rather than the usual
 * forced-read trick ([FlipCard]/`applyExpand`'s `getBoundingClientRect()`) — see [start]'s own
 * comment for why: a forced read fuses building `incoming` with that frame's layout/paint into
 * one long task, which roughly doubled the worst-case frame on the two content-heaviest routes
 * (measured — FlipCardRivePlan.md §19/tabs.json).
 *
 * A route change that arrives before the previous one finished [settle]s it synchronously first
 * (removing its outgoing layer and resetting the still-incoming one back to a plain resting
 * `.route-content`) rather than letting two transitions overlap — a fast double/triple tab click
 * is therefore always exactly as clean as a single one, never leaving a stray leftover layer or
 * timer behind. A same-route content change that arrives WHILE a slide is still pending/animating
 * (see [isPending]) calls [refresh] instead of [start] — see its own doc comment.
 */
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
internal class RouteSlider {
    private var buildTimer: Int? = null
    private var finishTimer: Int? = null
    private var raf1: Int? = null
    private var raf2: Int? = null
    private var finishNow: (() -> Unit)? = null
    private var pendingBuild: (() -> HTMLElement)? = null
    private var liveIncoming: HTMLElement? = null

    /**
     * True from the moment [start] commits to a real slide until it settles — including the
     * whole deferred-build window before `incoming` even exists yet. The caller's own same-route
     * rebuild path checks this and calls [refresh] instead of rebuilding in place when true.
     */
    fun isPending(): Boolean = buildTimer != null || raf1 != null || raf2 != null || finishTimer != null

    /**
     * Applies a same-route content change (a rating, a Settings toggle, a Matrix sub-section…)
     * that arrived while a slide to THIS route is still [isPending] — never dropped as if it were
     * just Compose's own routine post-navigation echo re-render (that distinction is the caller's
     * job; this class only ever sees "same route, slide pending"). RED: an earlier version simply
     * ignored every such call outright (reasoning that the staleness window was too short to
     * matter) — a real, later interaction in that same window (e.g. clicking a Matrix sub-section
     * right after the Training→Matrix slide) was then silently lost, because the ALREADY-scheduled
     * build closure was itself stale and nothing ever told it to use fresher one. If [buildIncoming]
     * hasn't run yet, this simply replaces what it will build; if `incoming` already exists (mid-
     * animation or freshly appended), its children are replaced in place — its own transform/
     * transition/position stay exactly as they are, so a refresh never restarts or visually
     * glitches the slide already under way.
     */
    fun refresh(buildIncoming: () -> HTMLElement) {
        val incoming = liveIncoming
        if (incoming != null) {
            val fresh = buildIncoming()
            incoming.textContent = ""
            while (fresh.firstChild != null) incoming.appendChild(fresh.firstChild!!)
            return
        }
        if (buildTimer != null) pendingBuild = buildIncoming // else: not actually pending — nothing to attach to
    }

    /** Finishes any in-flight transition immediately; a no-op when nothing is running. */
    fun settle() {
        buildTimer?.let { window.clearTimeout(it) }
        buildTimer = null
        pendingBuild = null
        finishTimer?.let { window.clearTimeout(it) }
        finishTimer = null
        raf1?.let { window.cancelAnimationFrame(it) }
        raf1 = null
        raf2?.let { window.cancelAnimationFrame(it) }
        raf2 = null
        liveIncoming = null
        val finish = finishNow
        finishNow = null
        finish?.invoke()
    }

    /**
     * [buildIncoming] must return a brand-new, not-yet-mounted `.route-content`. [onMounted] runs
     * once it is attached to the DOM and the transition has actually started (or immediately, for
     * the instant/first-mount cases that never animate at all) — the caller uses it to move focus
     * onto the new content. [onSettled] runs once the incoming layer is the sole, plain, resting
     * `.route-content` left in [viewport].
     */
    fun start(
        viewport: HTMLElement,
        buildIncoming: () -> HTMLElement,
        direction: Int,
        instant: Boolean,
        onMounted: (HTMLElement) -> Unit,
        onSettled: (HTMLElement) -> Unit,
    ) {
        settle() // never let a second transition of the same viewport overlap the first
        val outgoing = viewport.querySelector(":scope > .route-content") as? HTMLElement
        if (outgoing == null || instant) {
            val incoming = buildIncoming()
            outgoing?.remove()
            viewport.appendChild(incoming)
            onMounted(incoming)
            onSettled(incoming)
            return
        }
        // Building `incoming` (its whole subtree — for the vocabulary catalog or the grammar
        // table, genuinely large) is deferred one macrotask out from the click handler that
        // called this, rather than run inline right here — the same task-chunking the View
        // Transitions API did structurally for free (measured: one ~70ms task fusing the click
        // dispatch with the build AND that frame's layout/paint, versus several separate ~20-40ms
        // tasks — FlipCardRivePlan.md §19/tabs.json). A route change is not itself an input
        // gesture that must react within the SAME task, so this costs nothing perceptible.
        buildTimer = window.setTimeout({
            buildTimer = null
            // `pendingBuild` (see `refresh`) wins if a same-route change arrived before this ever
            // ran — it reflects strictly newer state than the closure `start` was originally
            // called with.
            val incoming = (pendingBuild ?: buildIncoming)()
            pendingBuild = null
            liveIncoming = incoming
            // Correction round: `incoming` no longer needs `position:absolute`/`top`/`left`/
            // `width` — `.route-viewport>.route-content{grid-area:1/1}` (training.css) already
            // stacks it exactly on top of `outgoing` and auto-sizes the shared row to whichever
            // of the two is taller, which a lone `position:absolute` layer sized only by
            // `outgoing` (still in normal flow) could not: it either clipped a taller incoming
            // route to outgoing's height or left a gap under a shorter one, for the whole slide.
            incoming.style.setProperty("will-change", "transform")
            incoming.style.setProperty("transform", "translateX(${direction * 100}%)")
            outgoing.style.setProperty("will-change", "transform")
            viewport.style.setProperty("overflow", "hidden")
            // The outgoing screen is about to visually leave — take it out of the accessibility
            // tree and tab order right away (not only once it is finally removed ~320ms later),
            // so a keyboard/screen-reader user (or a same-named locator, RED: a role query for
            // "Прогресс" ambiguously matched the still-live outgoing page's own "Сбросить
            // прогресс" button while a Progress→Training slide was still in flight) can never
            // reach a page that is already on its way out.
            outgoing.setAttribute("inert", "")
            outgoing.setAttribute("aria-hidden", "true")
            viewport.appendChild(incoming)
            // `finishNow` must exist from the moment `incoming` is actually IN the DOM, not only
            // once raf2 below gets around to starting the visual animation — otherwise a second
            // route change (or a same-route rebuild) landing in that gap finds `settle()` a no-op
            // (nothing to invoke yet) while `incoming` is already a second, orphaned
            // `.route-content` sibling that nothing ever removes again (RED: a same-route update
            // — e.g. a Settings toggle re-render — arriving between this append and raf2 left two
            // `#settings-return` buttons in the DOM permanently). Settling this early, before any
            // frame of the slide has painted, simply snaps `incoming` straight to rest — a
            // reasonable outcome for an interrupt this early, and always safe.
            val finish = {
                outgoing.remove()
                viewport.style.removeProperty("overflow")
                listOf("will-change", "transform", "transition").forEach {
                    incoming.style.removeProperty(it)
                }
                liveIncoming = null
                onSettled(incoming)
            }
            finishNow = finish
            // Two `requestAnimationFrame`s, not a forced `offsetWidth`/`getBoundingClientRect`
            // read, commit the "from" frame before the transition can animate to "to": a forced
            // read would re-fuse this task with that frame's layout/paint right back together.
            // rAF callbacks run BEFORE their frame's style/layout/paint, so `raf1` (still frame N,
            // `incoming` at its "from" position) does nothing but schedule `raf2`; frame N then
            // paints "from" on its own, unforced. `raf2` (frame N+1, "from" already painted) is
            // the one that sets the "to" transform — the same "commit a frame, then change it"
            // contract the forced-read trick gives, just paid for by the browser's own scheduled
            // work rather than a synchronous script-side flush.
            raf1 = window.requestAnimationFrame {
                raf1 = null
                raf2 = window.requestAnimationFrame {
                    raf2 = null
                    val transition = "transform ${SLIDE_DURATION_MS}ms $SLIDE_EASING"
                    outgoing.style.setProperty("transition", transition)
                    incoming.style.setProperty("transition", transition)
                    outgoing.style.setProperty("transform", "translateX(${-direction * 100}%)")
                    incoming.style.setProperty("transform", "translateX(0)")
                    onMounted(incoming)
                    finishTimer = window.setTimeout({ finishTimer = null; finishNow = null; finish(); null }, SLIDE_DURATION_MS)
                }
            }
            null
        }, 0)
    }
}
