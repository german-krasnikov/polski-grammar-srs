package polski.ui

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent

/**
 * Adds a horizontal swipe-to-rate gesture to [zone].
 * With [acceptAnyPointerType] false (the historical default, still used by the vocabulary card),
 * only a touch/pen primary pointer on a coarse-pointer device is accepted. With it true (the
 * training card's whole back-face, mouse included), any primary pointer qualifies, but the
 * gesture is ignored while a text field/textarea has focus so it never fights typed-answer input.
 */
internal fun installTouchSwipeRating(zone: HTMLElement, acceptAnyPointerType: Boolean = false, onRating: (remembered: Boolean) -> Unit) {
    data class Start(val x: Int, val y: Int, val pointerId: Int)
    var start: Start? = null
    zone.addEventListener("pointerdown", { raw ->
        val accepted = isPrimaryPointer(raw) &&
            (acceptAnyPointerType || (window.matchMedia("(pointer: coarse)").matches && isTouchPointer(raw)))
        // Capturing the pointer here would redirect its later click/pointerup to `zone` too, which
        // must never happen when the pointerdown itself landed on a button (or another interactive
        // descendant) — e.g. the rating buttons now live inside this same back-face zone — or on a
        // focused text field (typed-answer mode).
        val onInteractive = acceptAnyPointerType && (editableTarget(raw.target as? Element) || editableTarget(document.activeElement as? Element))
        if (!accepted || onInteractive) {
            start = null
            return@addEventListener
        }
        val event = raw as MouseEvent
        val pointerId = pointerIdentifier(raw)
        start = Start(event.clientX, event.clientY, pointerId)
        runCatching { zone.setPointerCapture(pointerId) }
    })
    zone.addEventListener("pointerup", { raw ->
        val origin = start
        start = null
        if (origin == null || !isPrimaryPointer(raw) || pointerIdentifier(raw) != origin.pointerId) return@addEventListener
        val event = raw as MouseEvent
        val dx = event.clientX - origin.x
        val dy = event.clientY - origin.y
        if (kotlin.math.abs(dx) >= 75 && kotlin.math.abs(dx) > kotlin.math.abs(dy) * 1.25) onRating(dx > 0)
    })
    zone.addEventListener("pointercancel", { start = null })
    zone.addEventListener("lostpointercapture", { start = null })
}
