package polski.ui

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLScriptElement
import polski.presentation.CardEffect

/**
 * Non-interactive Rive rating-effect overlay for the web training card (FC-15/18/20).
 *
 * The overlay lives outside `.route-content` (appended directly to the Compose host root), so it
 * survives the full DOM teardown that [TrainingDomRenderer] performs on most state changes — the
 * same architectural hazard the flip state has to work around. A single `<canvas>` is created
 * once per app session and simply repositioned/retriggered over the current card's bounding box;
 * it is never mounted per-card and never re-created mid-effect.
 *
 * All actual Rive API calls happen in the hand-written `rive/rive-bridge.js`, signalled purely
 * through a DOM attribute (`data-rive-effect`), so no js()/dynamic Kotlin<->JS bindings are
 * needed and the same code runs unmodified on the JS and Wasm targets.
 */
internal class RiveEffectOverlay {
    private var layer: HTMLElement? = null
    private var scriptRequested = false
    private var sequence = 0

    /** True when the app's Motion setting (or the OS reduce-motion signal) disables animation. */
    private fun reducedMotion(): Boolean =
        (document.documentElement)?.getAttribute("data-motion") == "reduced"

    /** Debug-only escape hatch for the A/B/C measurement variants (§5): `?riveDisabled=1`. */
    private fun disabledForMeasurement(): Boolean =
        window.location.search.contains("riveDisabled=1")

    private fun ensureLayer(root: HTMLElement): HTMLElement {
        layer?.let { return it }
        val div = (document.createElement("div") as HTMLElement).apply {
            id = "polski-rive-overlay"
            className = "card-effect-overlay"
            setAttribute("aria-hidden", "true")
            style.setProperty("display", "none")
            style.setProperty("position", "fixed")
        }
        div.appendChild(document.createElement("canvas") as HTMLCanvasElement)
        root.appendChild(div)
        layer = div
        return div
    }

    private fun ensureBridgeLoaded() {
        if (scriptRequested) return
        scriptRequested = true
        val script = document.createElement("script") as HTMLScriptElement
        script.src = "rive/rive-bridge.js"
        document.head?.appendChild(script)
    }

    /** Plays the rating effect over [cardBounds], unless motion is reduced or measurement disables Rive. */
    fun trigger(root: HTMLElement, cardBounds: HTMLElement, effect: CardEffect) {
        if (effect == CardEffect.None || reducedMotion() || disabledForMeasurement()) return
        val div = ensureLayer(root)
        val rect = cardBounds.getBoundingClientRect()
        div.style.setProperty("display", "block")
        div.style.setProperty("left", "${rect.left}px")
        div.style.setProperty("top", "${rect.top}px")
        div.style.setProperty("width", "${rect.width}px")
        div.style.setProperty("height", "${rect.height}px")
        ensureBridgeLoaded()
        sequence += 1
        val kind = if (effect == CardEffect.Remembered) "remembered" else "again"
        div.setAttribute("data-rive-effect", "$kind:$sequence")
    }
}
