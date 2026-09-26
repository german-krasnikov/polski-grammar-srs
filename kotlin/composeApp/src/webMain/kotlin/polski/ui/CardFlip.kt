package polski.ui

import kotlinx.browser.window
import org.w3c.dom.HTMLElement

/** Puts [hidden]'s face out of the accessibility tree/hit-testing and [shown]'s face into it. */
private fun swapAriaAndInert(front: HTMLElement, back: HTMLElement, toFlipped: Boolean) {
    val hidden = if (toFlipped) front else back
    val shown = if (toFlipped) back else front
    hidden.setAttribute("aria-hidden", "true")
    hidden.setAttribute("inert", "")
    shown.removeAttribute("aria-hidden")
    shown.removeAttribute("inert")
}

/**
 * Drives a real 3D flip between two faces mounted at once inside a `.card-flip`/`.card-flip-
 * inner`/`.card-face` wrapper (see `training.css`), swapping which face is in the accessibility
 * tree exactly at the 90° edge-on point of the rotation (R1/FC2-01/02) — extracted from the
 * training card's original flip (FlipCardRivePlan.md §12.1) so the vocabulary card (v3/B) reuses
 * the exact same tested behaviour instead of a second copy: exact 90° face swap in both
 * directions, an instant swap under reduced motion or Animations-off, and a ring Rive accent
 * ([mountRings]/[setRingsExpanded]) synced to the two halves.
 *
 * One instance is one card's flip state; the caller [reset]s it whenever a brand-new card mounts
 * and calls [apply] on every render — including the very first, non-animated one — so
 * `currentAngle` always reflects the (possibly freshly rebuilt) DOM node's real angle rather than
 * assuming one.
 */
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
internal class FlipCard {
    private var currentAngle = 0.0
    private var pendingTimer: Int? = null

    /** Call when a brand-new card (new exercise/word) mounts, before its first [apply]. */
    fun reset() {
        pendingTimer?.let { window.clearTimeout(it) }
        pendingTimer = null
        currentAngle = 0.0
    }

    /**
     * Swaps [inner]'s faces to face [toFlipped] ("front" when false, "back" when true). Animates
     * the real 180° rotation (with the Rive ring cue on [ringsLayer] pulsing across it) only when
     * [isFlipEvent] is true and motion isn't instant; otherwise jumps straight to the final angle
     * — a routine DOM rebuild resuming the current face (e.g. the 30s refresh timer) must never
     * replay the flip. Implemented with CSS transitions timed by `setTimeout` (not
     * `transitionend`): a flip interrupted at exactly the 90° checkpoint makes `from == mid`, so
     * the transition has nothing to animate and `transitionend` never fires. A forced layout read
     * between setting the "from" transform and starting the transition (see below) is what
     * actually fixes FC2-01: without it, a freshly created `.card-flip-inner` node has no
     * previously-painted frame for the browser to transition from, so the very first auto-flip
     * on reveal would snap instead of animating.
     */
    fun apply(inner: HTMLElement, front: HTMLElement, back: HTMLElement, ringsLayer: HTMLElement, toFlipped: Boolean, isFlipEvent: Boolean) {
        val to = if (toFlipped) 180.0 else 0.0
        // The actual rotation is always driven by the inline `transform` below (which wins over
        // any stylesheet rule); this class is kept purely as a stable, easily-asserted state
        // marker (existing acceptance tests key off it) and never itself drives the visual angle.
        inner.classList.toggle("flipped", toFlipped)
        pendingTimer?.let { window.clearTimeout(it) }
        pendingTimer = null
        val animateVisually = isFlipEvent && !motionInstantActive()
        if (!animateVisually) {
            inner.style.setProperty("transition", "none")
            inner.style.setProperty("transform", "rotateY(${to}deg)")
            swapAriaAndInert(front, back, toFlipped)
            currentAngle = to
            return
        }
        val from = currentAngle
        val mid = 90.0
        val halfDurationMs = flipHalfDurationMs()
        setRingsExpanded(ringsLayer, true)
        // R1: the target face must stay out of the accessibility tree (and unpainted-as-visible
        // via backface-visibility) until the 90° edge-on point — explicitly (re)assert the
        // pre-flip face/hidden-face pairing now, synchronously, before any frame paints.
        swapAriaAndInert(front, back, toFlipped = !toFlipped)
        inner.style.setProperty("transition", "none")
        inner.style.setProperty("transform", "rotateY(${from}deg)")
        inner.getBoundingClientRect() // force layout: commits the "from" frame before animating
        inner.style.setProperty("transition", "transform ${halfDurationMs}ms ease-in")
        inner.style.setProperty("transform", "rotateY(${mid}deg)")
        pendingTimer = window.setTimeout({
            pendingTimer = null
            currentAngle = mid
            swapAriaAndInert(front, back, toFlipped) // exactly at the 90° edge-on point
            inner.style.setProperty("transition", "transform ${halfDurationMs}ms ease-out")
            inner.style.setProperty("transform", "rotateY(${to}deg)")
            pendingTimer = window.setTimeout({
                pendingTimer = null
                currentAngle = to
                setRingsExpanded(ringsLayer, false)
                null
            }, halfDurationMs)
            null
        }, halfDurationMs)
    }

    /**
     * Tap-to-flip on the whole card: a pointer gesture with near-zero movement toggles the face,
     * skipping interactive descendants (buttons, the typed-answer textarea, `<select>`) — see
     * [installTapGesture]. A larger movement is left entirely to a swipe handler that shares the
     * same element, if any.
     */
    fun installTap(flip: HTMLElement, onToggle: () -> Unit) {
        installTapGesture(flip, onToggle)
    }
}
