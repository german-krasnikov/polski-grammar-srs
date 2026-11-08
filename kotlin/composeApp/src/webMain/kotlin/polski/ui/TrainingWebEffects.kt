package polski.ui

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLElement
import polski.presentation.*

// Uses closest(), not just the exact target's own tag: a click on a <button> often lands on one
// of its child nodes (e.g. the rating buttons' <small>/<span> hint text), and callers here rely on
// interactive ANCESTORS being excluded too (e.g. the card-flip tap handler must never toggle while
// a rating button is mid-click).
internal fun editableTarget(target: Element?): Boolean =
    target?.closest("input, textarea, select, button, a, [contenteditable]") != null

/**
 * UX5: moves focus to the freshly built route's own heading (its first `h1`/`h2`, wherever it is
 * nested — every route has exactly one) so a screen reader announces the new screen right when it
 * appears, not the tab bar it was clicked from. A route with no heading of its own (Training, whose
 * content starts directly with its mode toolbar) falls back to the route container itself — still
 * a real, announced focus move, just without a heading to name it. At the moment this runs the
 * target may still be mid-slide (translated off-screen, see [RouteSlider]) — `transform` never
 * moves an element's own layout box, only its paint position, so this never needs to scroll the
 * PAGE to bring the target's box into view. It can still scroll the target's own box INTO the
 * page's current viewport, though (reviewer note, §19.7): the grid-stack the two sliding layers
 * share (training.css) auto-sizes to whichever layer is taller, and a still-mid-slide `incoming`
 * heading can therefore land below the fold for one frame — `focus()`'s default scroll-into-view
 * would jump the page under the still-animating slide to chase it. `preventScroll: true` heads
 * that off; it isn't in either binding this project has for `focus()` (kotlinx-browser and
 * kotlin-dom-api-compat both omit `FocusOptions`), so [focusPreventScroll] reaches it through the
 * one `js()` snippet both the `js()` and `wasmJs()` targets compile identically.
 */
internal fun focusRouteHeading(content: HTMLElement) {
    val target = (content.querySelector("h1, h2") as? HTMLElement) ?: content
    if (!target.hasAttribute("tabindex")) target.setAttribute("tabindex", "-1")
    focusPreventScroll(target)
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun focusPreventScroll(element: HTMLElement): Unit = js("element.focus({ preventScroll: true })")

internal fun executeEffect(root: HTMLElement, state: AppUiState, effect: UiEffect, store: TrainingStore,
                          navigate: (WebRoute) -> Unit): Boolean {
    when (effect) {
        is UiEffect.FocusReveal -> {
            if (state.exerciseId == effect.exerciseId && state.phase == CardPhase.Question && state.tab == AppTab.Training) {
                if (state.introPending) return false
                val reveal = root.querySelector("#training-reveal") as? HTMLElement ?: return false
                reveal.focus()
                store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Completed))
            } else store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Skipped))
        }
        is UiEffect.ConfirmReset -> {
            val previousRevision = store.state.value.revision
            val confirmed = window.confirm(effect.prompt)
            store.dispatch(AppAction.ResetDecision(effect.id, confirmed))
            if (confirmed && store.state.value.revision > previousRevision && store.state.value.tab == AppTab.Training) {
                navigate(WebRoute.Training)
            }
        }
        is UiEffect.DownloadJson -> {
            try {
                val anchor = document.createElement("a") as HTMLAnchorElement
                anchor.href = "data:application/json;charset=utf-8,${percentEncode(effect.json)}"
                anchor.download = effect.filename
                document.body?.appendChild(anchor)
                anchor.click()
                anchor.remove()
                store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Completed))
            } catch (error: Throwable) {
                store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Failed(error.message ?: "Экспорт не удался")))
            }
        }
        is UiEffect.DownloadText -> {
            try {
                val anchor = document.createElement("a") as HTMLAnchorElement
                anchor.href = "data:text/plain;charset=utf-8,${percentEncode(effect.text)}"
                anchor.download = effect.filename
                document.body?.appendChild(anchor)
                anchor.click()
                anchor.remove()
                store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Completed))
            } catch (error: Throwable) {
                store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Failed(error.message ?: "Экспорт не удался")))
            }
        }
    }
    return true
}

private fun percentEncode(value: String): String {
    val digits = "0123456789ABCDEF"
    return buildString {
        for (byte in value.encodeToByteArray()) {
            val unsigned = byte.toInt() and 255
            append('%')
            append(digits[unsigned shr 4])
            append(digits[unsigned and 15])
        }
    }
}

