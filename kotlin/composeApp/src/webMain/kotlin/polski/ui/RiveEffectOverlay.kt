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

/** v3/C: true when the explicit Settings → Animations toggle is off. */
internal fun animationsDisabledActive(): Boolean =
    (document.documentElement)?.getAttribute("data-animations") == "off"

/** v3/C: true when nothing should move at all — reduced motion OR the Animations toggle is off.
 *  Drives the training expand-reveal and the vocabulary flip's own CSS transition, independently
 *  of whether Rive itself is also gated (see [riveGated], which additionally checks the
 *  measurement-only `?riveDisabled=1` escape hatch). */
internal fun motionInstantActive(): Boolean = reducedMotionActive() || animationsDisabledActive()

/** Debug-only escape hatch for the A/B/C measurement variants (§5): `?riveDisabled=1`. */
private fun disabledForMeasurement(): Boolean = window.location.search.contains("riveDisabled=1")

/** Whether ANY Rive call (bridge load, trigger, ring pulse, prewarm) is allowed right now. */
internal fun riveGated(): Boolean = motionInstantActive() || disabledForMeasurement()

/**
 * Each half of the card flip's duration in ms (R1's two-half swap-at-90°, [FlipCard.apply]).
 * Normally a fixed 250ms; `?flipDebugScale=<n>` multiplies it for tests that need a slowed-down,
 * non-flaky window to assert mid-flip DOM/aria state in — the same test-only-timing idea as the
 * iOS/macOS `POLSKI_FLIP_DEBUG_SCALE` env var (FlipCardRivePlan.md §12.1/FC2-05), ported to a URL
 * flag since that is this host's existing debug-flag convention (`?riveDisabled=1`).
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

private var riveBridgeRequested = false
private var disposeSequence = 0

/** Loads `rive-bridge.js` at most once per page, shared by every card host (training,
 *  vocabulary) and by [prewarmRiveIfEnabled] — independent loaders would double the network
 *  request for the same script for no benefit. */
private fun ensureRiveBridgeLoaded() {
    if (riveBridgeRequested) return
    riveBridgeRequested = true
    val script = document.createElement("script") as HTMLScriptElement
    script.src = "rive/rive-bridge.js"
    document.head?.appendChild(script)
}

/**
 * v3/D: warms the Rive runtime (script + wasm + a throwaway instance — see `rive-bridge.js`'s
 * `schedulePrewarm`, which defers the actual work to `requestIdleCallback`) shortly after the
 * app's first render, so the very first real effect (reveal ring, flip ring or rating) never pays
 * the first-fetch/first-compile cost on the same frames as its own CSS motion — the diagnosed
 * cause of the first-reveal/first-flip jank. Never called when gated: a reduced-motion or
 * Animations-off session must not fetch rive.js, rive.wasm or any .riv file at all, prewarm or not (contract
 * C) — the caller re-checks on every preferences change, so turning Animations on later still
 * prewarms lazily at that point.
 */
internal fun prewarmRiveIfEnabled(animationsEnabled: Boolean) {
    if (!animationsEnabled || reducedMotionActive()) return
    ensureRiveBridgeLoaded()
    document.body?.setAttribute("data-rive-prewarm", "1")
}

/** v3/C: disposes every live Rive instance (rating, chain, ring cues) the moment Animations is
 *  turned off at runtime. A no-op call (already off, or turning on) does nothing; only the
 *  enabled→disabled edge bumps the sequence, so toggling the same state twice never re-disposes
 *  an already-empty runtime. */
internal fun disposeRiveOnDisable(wasEnabled: Boolean, isEnabled: Boolean) {
    if (!wasEnabled || isEnabled) return
    disposeSequence += 1
    document.body?.setAttribute("data-rive-dispose-all", disposeSequence.toString())
}

/**
 * Creates the decorative flip/reveal-in-progress ring cue as a sibling of the rotating/expanding
 * element, inside [container] — see `training.css`'s `.card-flip-rings` for why this placement
 * makes it paint behind the card's faces rather than over their text (FC2-06). Shared by the
 * vocabulary flip and the training expand-reveal (v3).
 */
internal fun mountRings(container: HTMLElement): HTMLElement {
    val div = (document.createElement("div") as HTMLElement).apply {
        className = "card-flip-rings"
        setAttribute("aria-hidden", "true")
    }
    div.appendChild(document.createElement("canvas") as HTMLCanvasElement)
    container.insertBefore(div, container.firstChild)
    return div
}

/** Expands/contracts a ring cue mounted by [mountRings] at a flip/reveal's boundaries; a no-op
 *  when gated (FC2-08/v3-C). */
internal fun setRingsExpanded(ringsLayer: HTMLElement, expanded: Boolean) {
    if (riveGated()) return
    ensureRiveBridgeLoaded()
    ringsLayer.setAttribute("data-rive-rings", if (expanded) "1" else "0")
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
 * Non-interactive Rive effect overlays for the web card hosts: the rating cue (FC-15/18/20, v2
 * FC2-09), the chain-completion celebration (FC2-10, R3) and the training card's one-shot reveal
 * ring pulse (v3/A, reusing [mountRings]/[setRingsExpanded]). One instance is created per host
 * controller (training, vocabulary); they share the single `rive-bridge.js` load (module-level
 * state above) and its module-level Rive instances (see `rive-bridge.js`'s own
 * `currentRating`/`currentChain`/`canvas.__polskiRive`).
 *
 * The rating and chain/reveal layers live outside `.route-content` (appended directly to the
 * Compose host root), so they survive the full DOM teardown that the per-route renderers perform
 * on most state changes. Each is a single `<canvas>`-holding div created once per app session and
 * simply repositioned/retriggered over the current target's bounding box.
 *
 * All actual Rive API calls happen in the hand-written `rive/rive-bridge.js`, signalled purely
 * through DOM attributes (`data-rive-effect`, `data-rive-chain`, `data-rive-rings`), so no
 * js()/dynamic Kotlin<->JS bindings are needed and the same code runs unmodified on the JS and
 * Wasm targets (see FC-18's deviation note in FlipCardRivePlan.md §8).
 */
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
internal class RiveEffectOverlay {
    private var ratingLayer: HTMLElement? = null
    private var chainLayer: HTMLElement? = null
    private var revealLayer: HTMLElement? = null
    private var ratingSequence = 0
    private var chainSequence = 0

    /**
     * Plays the rating effect over [cardBounds], unless motion is reduced, Animations is off, or
     * measurement disables Rive. `Remembered` plays confetti together with the "Check" cue;
     * `Again` plays only "Error" (v2 FC2-09 — the old `again.riv` painted an opaque scene, see
     * RiveCatalog.md §0.1).
     */
    fun trigger(root: HTMLElement, cardBounds: HTMLElement, effect: CardEffect) {
        if (effect == CardEffect.None || riveGated()) return
        val div = ratingLayer ?: ensureFixedLayer(root, "polski-rive-overlay", canvasCount = 2).also { ratingLayer = it }
        positionOverBounds(div, cardBounds)
        ensureRiveBridgeLoaded()
        ratingSequence += 1
        val kind = if (effect == CardEffect.Remembered) "remembered" else "again"
        div.setAttribute("data-rive-effect", "$kind:$ratingSequence")
    }

    /** Plays the "Tada" celebration once, over [anchor], when a training chain finishes (FC2-10, R3). */
    fun triggerChainComplete(root: HTMLElement, anchor: HTMLElement) {
        if (riveGated()) return
        val div = chainLayer ?: ensureFixedLayer(root, "polski-rive-chain", canvasCount = 1).also { chainLayer = it }
        positionOverBounds(div, anchor)
        ensureRiveBridgeLoaded()
        chainSequence += 1
        div.setAttribute("data-rive-chain", chainSequence.toString())
    }

    /**
     * v3/A: a one-shot ring pulse over [bounds] exactly when the training card's answer expands
     * downward — the same `rings.riv` accent the vocabulary flip uses, reused rather than a new
     * asset. Never blocks the CSS expand transition itself, which runs regardless of Rive's own
     * load state (v3/D's "never block motion on Rive" contract) — this only sets attributes the
     * bridge reacts to whenever it happens to be ready.
     */
    fun triggerReveal(root: HTMLElement, bounds: HTMLElement) {
        if (riveGated()) return
        val div = revealLayer ?: ensureFixedLayer(root, "polski-rive-reveal", canvasCount = 1).also {
            it.classList.add("card-flip-rings")
            revealLayer = it
        }
        positionOverBounds(div, bounds)
        setRingsExpanded(div, true)
        window.setTimeout({ setRingsExpanded(div, false); null }, 900)
    }
}
