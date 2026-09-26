package polski.ui

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLScriptElement
import polski.presentation.CardEffect

/** True when the app's Motion setting (or the OS reduce-motion signal) disables animation. */
internal fun reducedMotionActive(): Boolean =
    (document.documentElement)?.getAttribute("data-motion") == "reduced"

/**
 * Each half of the card flip's duration in ms (R1's two-half swap-at-90°, [TrainingDomRenderer.
 * applyFlip]). Normally a fixed 250ms; `?flipDebugScale=<n>` multiplies it for tests that need a
 * slowed-down, non-flaky window to assert mid-flip DOM/aria state in — the same test-only-timing
 * idea as the iOS/macOS `POLSKI_FLIP_DEBUG_SCALE` env var (FlipCardRivePlan.md §12.1/FC2-05),
 * ported to a URL flag since that is this host's existing debug-flag convention (`?riveDisabled=1`).
 */
internal fun flipHalfDurationMs(): Int {
    val search = window.location.search
    val marker = "flipDebugScale="
    val start = search.indexOf(marker)
    if (start < 0) return 250
    val rest = search.substring(start + marker.length)
    val end = rest.indexOf('&').let { if (it < 0) rest.length else it }
    val scale = rest.substring(0, end).toDoubleOrNull() ?: return 250
    return (250 * scale).toInt().coerceIn(1, 20_000)
}

/**
 * Non-interactive Rive effect overlays for the web training card: the rating cue (FC-15/18/20,
 * v2 FC2-09), the flip-in-progress ring cue (FC2-06/07/08, R2) and the chain-completion
 * celebration (FC2-10, R3).
 *
 * The rating and chain layers live outside `.route-content` (appended directly to the Compose
 * host root), so they survive the full DOM teardown that [TrainingDomRenderer] performs on most
 * state changes — the same architectural hazard the flip state has to work around. Each is a
 * single `<canvas>`-holding div created once per app session and simply repositioned/retriggered
 * over the current target's bounding box. The ring cue is different: it must paint *behind* the
 * card's own (semi-transparent) faces, so it lives *inside* the per-render `.card-flip` element
 * instead — see [mountRings] and `training.css`'s `.card-flip-rings`.
 *
 * All actual Rive API calls happen in the hand-written `rive/rive-bridge.js`, signalled purely
 * through DOM attributes (`data-rive-effect`, `data-rive-chain`, `data-rive-rings`), so no
 * js()/dynamic Kotlin<->JS bindings are needed and the same code runs unmodified on the JS and
 * Wasm targets (see FC-18's deviation note in FlipCardRivePlan.md §8).
 */
internal class RiveEffectOverlay {
    private var ratingLayer: HTMLElement? = null
    private var chainLayer: HTMLElement? = null
    private var scriptRequested = false
    private var ratingSequence = 0
    private var chainSequence = 0

    /** Debug-only escape hatch for the A/B/C measurement variants (§5): `?riveDisabled=1`. */
    private fun disabledForMeasurement(): Boolean =
        window.location.search.contains("riveDisabled=1")

    private fun gated(): Boolean = reducedMotionActive() || disabledForMeasurement()

    private fun ensureBridgeLoaded() {
        if (scriptRequested) return
        scriptRequested = true
        val script = document.createElement("script") as HTMLScriptElement
        script.src = "rive/rive-bridge.js"
        document.head?.appendChild(script)
    }

    private fun ensureFixedLayer(root: HTMLElement, id: String, canvasCount: Int): HTMLElement {
        (document.getElementById(id) as? HTMLElement)?.let { return it }
        val div = (document.createElement("div") as HTMLElement).apply {
            this.id = id
            className = "card-effect-overlay"
            setAttribute("aria-hidden", "true")
            style.setProperty("display", "none")
            style.setProperty("position", "fixed")
        }
        repeat(canvasCount) { div.appendChild(document.createElement("canvas") as HTMLCanvasElement) }
        root.appendChild(div)
        return div
    }

    private fun positionOverBounds(div: HTMLElement, bounds: HTMLElement) {
        val rect = bounds.getBoundingClientRect()
        div.style.setProperty("display", "block")
        div.style.setProperty("left", "${rect.left}px")
        div.style.setProperty("top", "${rect.top}px")
        div.style.setProperty("width", "${rect.width}px")
        div.style.setProperty("height", "${rect.height}px")
    }

    /**
     * Plays the rating effect over [cardBounds], unless motion is reduced or measurement disables
     * Rive. `Remembered` plays confetti together with the "Check" cue; `Again` plays only "Error"
     * (v2 FC2-09 — the old `again.riv` painted an opaque scene, see RiveCatalog.md §0.1).
     */
    fun trigger(root: HTMLElement, cardBounds: HTMLElement, effect: CardEffect) {
        if (effect == CardEffect.None || gated()) return
        val div = ratingLayer ?: ensureFixedLayer(root, "polski-rive-overlay", canvasCount = 2).also { ratingLayer = it }
        positionOverBounds(div, cardBounds)
        ensureBridgeLoaded()
        ratingSequence += 1
        val kind = if (effect == CardEffect.Remembered) "remembered" else "again"
        div.setAttribute("data-rive-effect", "$kind:$ratingSequence")
    }

    /** Plays the "Tada" celebration once, over [anchor], when a training chain finishes (FC2-10, R3). */
    fun triggerChainComplete(root: HTMLElement, anchor: HTMLElement) {
        if (gated()) return
        val div = chainLayer ?: ensureFixedLayer(root, "polski-rive-chain", canvasCount = 1).also { chainLayer = it }
        positionOverBounds(div, anchor)
        ensureBridgeLoaded()
        chainSequence += 1
        div.setAttribute("data-rive-chain", chainSequence.toString())
    }

    /**
     * Creates the decorative flip-in-progress ring cue as a sibling of `.card-flip-inner`, inside
     * [flip] (`.card-flip`) — see `training.css`'s `.card-flip-rings` for why this placement makes
     * it paint behind the card's faces rather than over their text (FC2-06).
     */
    fun mountRings(flip: HTMLElement): HTMLElement {
        val div = (document.createElement("div") as HTMLElement).apply {
            className = "card-flip-rings"
            setAttribute("aria-hidden", "true")
        }
        div.appendChild(document.createElement("canvas") as HTMLCanvasElement)
        flip.insertBefore(div, flip.firstChild)
        return div
    }

    /** Expands/contracts the ring cue at a flip's 0°/180° boundaries; a no-op when gated (FC2-08). */
    fun setRingsExpanded(ringsLayer: HTMLElement, expanded: Boolean) {
        if (gated()) return
        ensureBridgeLoaded()
        ringsLayer.setAttribute("data-rive-rings", if (expanded) "1" else "0")
    }
}
