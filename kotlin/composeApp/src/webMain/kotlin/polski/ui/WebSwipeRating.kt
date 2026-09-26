package polski.ui

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent

private const val SWIPE_THRESHOLD_PX = 75.0
private const val SWIPE_COMMIT_TRANSITION_MS = 220
private const val SWIPE_EASING = "cubic-bezier(.16,1,.3,1)"

/**
 * UX4-08/09: the whole revealed card face IS the swipe-to-rate gesture surface (mouse and touch
 * alike — no pointer-type gating any more, since a mouse drag must work exactly like a touch
 * swipe on both hosts) — [faceForTransform] is the element that actually tilts/translates with
 * the finger and receives the live `--swipe-progress` custom property (`-1`..`1`, read by
 * `training.css`'s tint/label rules, and inherited down to it from [zone] if they differ).
 *
 * [zone] — the element the pointer listeners are attached to — is **not** always
 * [faceForTransform] itself: a face that visually moves during the gesture briefly leaves its own
 * resting hitbox while it animates back (snap-back/fly-out), so a *second* gesture starting near
 * that edge within the ~220ms return window could otherwise miss it entirely (its layout box is
 * unaffected, but a CSS `transform` moves what's actually under the pointer for hit-testing too).
 * Callers therefore pass a **stable, never-transformed ancestor** that has exactly the same
 * resting footprint as the face (training's `.card-answer-wrap`, vocabulary's `.card-flip`) as
 * [zone], so the gesture surface itself never moves even while [faceForTransform] does.
 *
 * [baseTransform] (e.g. `"rotateY(180deg)"` for the vocabulary card's back face) is preserved
 * underneath every drag/settle transform this function sets, so a caller that already needs its
 * own static `transform` for an unrelated reason (there, undoing the flip's mirroring) never has
 * it clobbered by this one.
 *
 * The gesture is ignored when it starts on an interactive descendant (a rating button, the typed-
 * answer textarea) or while a text field has focus, so it never fights typed input or steals a
 * button's click (see `editableTarget`, and the `closest()`-based fix recorded in
 * FlipCardRivePlan.md §8's deviations for why `event.target`, not just `activeElement`, must be
 * checked). A drag that turns out short or more vertical than horizontal snaps back with the same
 * transition as a committed swipe's fly-out, never rating.
 */
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
internal fun installSwipeCard(zone: HTMLElement, faceForTransform: HTMLElement, baseTransform: String = "", onRating: (remembered: Boolean) -> Unit) {
    data class Start(val x: Int, val y: Int, val pointerId: Int)
    var start: Start? = null
    var horizontal = false
    fun withBase(transform: String) = if (baseTransform.isEmpty()) transform else "$baseTransform $transform"

    fun setProgress(value: Double) { faceForTransform.style.setProperty("--swipe-progress", value.toString()) }
    // P1-6: a horizontal drag otherwise selects the card's own text like any other mouse drag —
    // suppressed on `zone` (not `faceForTransform`; the selection a fast drag already started can
    // span outside the moving face) for exactly the gesture's duration, and any range already
    // selected before the suppression took effect is dropped too.
    fun clearVisual() {
        faceForTransform.style.removeProperty("transition")
        faceForTransform.style.removeProperty("transform")
        faceForTransform.style.removeProperty("opacity")
        faceForTransform.style.removeProperty("--swipe-progress")
        zone.style.removeProperty("user-select")
    }
    fun settle(committed: Boolean, dx: Int) {
        val instant = motionInstantActive()
        if (!committed) {
            if (instant) { clearVisual(); return }
            faceForTransform.style.setProperty("transition", "transform ${SWIPE_COMMIT_TRANSITION_MS}ms $SWIPE_EASING")
            faceForTransform.style.setProperty("transform", withBase("translateX(0) rotate(0deg)"))
            setProgress(0.0)
            window.setTimeout({ clearVisual(); null }, SWIPE_COMMIT_TRANSITION_MS)
            return
        }
        val remembered = dx > 0
        if (instant) { clearVisual(); onRating(remembered); return }
        val sign = if (remembered) 1 else -1
        faceForTransform.style.setProperty("transition", "transform ${SWIPE_COMMIT_TRANSITION_MS}ms $SWIPE_EASING, opacity ${SWIPE_COMMIT_TRANSITION_MS}ms $SWIPE_EASING")
        faceForTransform.style.setProperty("transform", withBase("translateX(${sign * 140}%) rotate(${sign * 14}deg)"))
        faceForTransform.style.setProperty("opacity", "0")
        window.setTimeout({ clearVisual(); onRating(remembered); null }, SWIPE_COMMIT_TRANSITION_MS)
    }

    zone.addEventListener("pointerdown", { raw ->
        // Capturing the pointer here would redirect its later click/pointerup to `zone` too, which
        // must never happen when the pointerdown itself landed on a button (or another interactive
        // descendant) — e.g. the rating buttons now live inside this same back-face zone — or on a
        // focused text field (typed-answer mode).
        val onInteractive = editableTarget(raw.target as? Element) || editableTarget(document.activeElement)
        if (!isPrimaryPointer(raw) || onInteractive) {
            start = null
            return@addEventListener
        }
        val event = raw as MouseEvent
        val pointerId = pointerIdentifier(raw)
        start = Start(event.clientX, event.clientY, pointerId)
        horizontal = false
        runCatching { zone.setPointerCapture(pointerId) }
    })
    zone.addEventListener("pointermove", { raw ->
        val origin = start ?: return@addEventListener
        if (!isPrimaryPointer(raw) || pointerIdentifier(raw) != origin.pointerId) return@addEventListener
        val event = raw as MouseEvent
        val dx = event.clientX - origin.x
        val dy = event.clientY - origin.y
        // Decided as soon as the movement is unambiguous, not only at release: a horizontal-
        // dominant drag "sticks" to this gesture (tinting/translating the face) even if it briefly
        // wobbles vertically afterwards; a vertical/diagonal one is left alone entirely (no visual
        // follow at all), so it never fights page scroll.
        if (!horizontal && kotlin.math.abs(dx) <= kotlin.math.abs(dy) * 1.25) return@addEventListener
        if (!horizontal) {
            zone.style.setProperty("user-select", "none")
            clearTextSelection()
        }
        horizontal = true
        val rotate = (dx / 22.0).coerceIn(-8.0, 8.0)
        faceForTransform.style.setProperty("transition", "none")
        faceForTransform.style.setProperty("transform", withBase("translateX(${dx}px) rotate(${rotate}deg)"))
        setProgress((dx / SWIPE_THRESHOLD_PX).coerceIn(-1.0, 1.0))
    })
    zone.addEventListener("pointerup", { raw ->
        val origin = start
        start = null
        if (origin == null || !isPrimaryPointer(raw) || pointerIdentifier(raw) != origin.pointerId) return@addEventListener
        val event = raw as MouseEvent
        val dx = event.clientX - origin.x
        val dy = event.clientY - origin.y
        val wasHorizontal = horizontal
        horizontal = false
        val horizontalDominant = kotlin.math.abs(dx) > kotlin.math.abs(dy) * 1.25
        // `wasHorizontal` covers a real drag (locked in by a pointermove partway through, see
        // above); `horizontalDominant` alone covers a same-tick down→up pair with no intermediate
        // move at all — a synthetic/scripted gesture, but also the only way a fast enough real
        // one could ever arrive. Neither means "never moved" (a tap) or "stayed vertical/diagonal".
        if (!wasHorizontal && !horizontalDominant) return@addEventListener
        settle(kotlin.math.abs(dx) >= SWIPE_THRESHOLD_PX && horizontalDominant, dx)
    })
    zone.addEventListener("pointercancel", { start = null; if (horizontal) settle(false, 0); horizontal = false })
    zone.addEventListener("lostpointercapture", { start = null; if (horizontal) settle(false, 0); horizontal = false })
}

/**
 * UX4-10: the two direction labels ("Повторить"/"Вспомнил") that grow with drag distance,
 * decorative and driven purely by CSS reading the `--swipe-progress` custom property
 * [installSwipeCard] sets on the same element — shared by both card hosts so there is exactly one
 * copy of this markup.
 */
internal fun appendSwipeLabels(face: HTMLElement) {
    listOf("again" to "Повторить", "good" to "Вспомнил").forEach { (kind, label) ->
        val span = document.createElement("span") as HTMLElement
        span.className = "swipe-label swipe-label-$kind"
        span.textContent = label
        span.setAttribute("aria-hidden", "true")
        face.appendChild(span)
    }
}

/**
 * A pointer gesture with near-zero movement counts as a tap; anything larger is left to whatever
 * swipe/drag handler shares the same element (this one does nothing for it). Shared by the
 * training card's click-to-reveal question and the vocabulary card's click-to-flip (`FlipCard`)
 * — one tap-vs-drag implementation for both, not two copies of the same 10px threshold.
 */
internal fun installTapGesture(target: HTMLElement, onTap: () -> Unit) {
    data class Start(val x: Int, val y: Int, val pointerId: Int)
    var start: Start? = null
    target.addEventListener("pointerdown", { raw ->
        start = if (isPrimaryPointer(raw) && !editableTarget(raw.target as? Element)) {
            val event = raw as MouseEvent
            Start(event.clientX, event.clientY, pointerIdentifier(raw))
        } else null
    })
    // P1-5: a drag that wanders far enough and then returns to its starting point must not still
    // read as a tap on release (dx/dy back near zero) — cancel the pending tap as soon as the
    // gesture is unambiguously a drag, using the same 10px threshold `pointerup` checks below.
    target.addEventListener("pointermove", { raw ->
        val origin = start ?: return@addEventListener
        if (!isPrimaryPointer(raw) || pointerIdentifier(raw) != origin.pointerId) return@addEventListener
        val event = raw as MouseEvent
        if (kotlin.math.abs(event.clientX - origin.x) >= 10 || kotlin.math.abs(event.clientY - origin.y) >= 10) start = null
    })
    target.addEventListener("pointerup", { raw ->
        val origin = start
        start = null
        if (origin == null || !isPrimaryPointer(raw) || pointerIdentifier(raw) != origin.pointerId || editableTarget(raw.target as? Element)) return@addEventListener
        val event = raw as MouseEvent
        val dx = event.clientX - origin.x
        val dy = event.clientY - origin.y
        if (kotlin.math.abs(dx) < 10 && kotlin.math.abs(dy) < 10) onTap()
    })
    target.addEventListener("pointercancel", { start = null })
}
