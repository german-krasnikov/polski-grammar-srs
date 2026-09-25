package polski.ui

import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent

/** Adds a touch-only horizontal gesture to a dedicated affordance; buttons remain the primary alternative. */
internal fun installTouchSwipeRating(zone: HTMLElement, onRating: (remembered: Boolean) -> Unit) {
    data class Start(val x: Int, val y: Int, val pointerId: Int)
    var start: Start? = null
    zone.addEventListener("pointerdown", { raw ->
        if (!window.matchMedia("(pointer: coarse)").matches || !isTouchPointer(raw) || !isPrimaryPointer(raw)) {
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
        if (origin == null || !isTouchPointer(raw) || !isPrimaryPointer(raw) || pointerIdentifier(raw) != origin.pointerId) return@addEventListener
        val event = raw as MouseEvent
        val dx = event.clientX - origin.x
        val dy = event.clientY - origin.y
        if (kotlin.math.abs(dx) >= 75 && kotlin.math.abs(dx) > kotlin.math.abs(dy) * 1.25) onRating(dx > 0)
    })
    zone.addEventListener("pointercancel", { start = null })
    zone.addEventListener("lostpointercapture", { start = null })
}
