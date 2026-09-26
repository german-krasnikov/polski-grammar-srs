package polski.ui.screens

import android.content.Context
import android.os.Trace
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.core.Rive
import dev.polski.grammarmatrix.compose.R
import java.util.concurrent.atomic.AtomicBoolean
import polski.presentation.CardEffect

/** Loads the native Rive runtime once per process; [RiveAnimationView] requires this before construction. */
@Volatile private var riveRuntimeInitialized = false
private fun ensureRiveInitialized(context: Context) {
    if (riveRuntimeInitialized) return
    Rive.init(context.applicationContext)
    riveRuntimeInitialized = true
}

/**
 * `android.os.Trace` bracketing for the app's first-ever Rive rating effect (FC2-16, R4). The
 * legacy [RiveAnimationView] has no `onLoad` callback (unlike the web/iOS runtimes), so this can
 * only measure "constructor return → `fireState()` return", not "until the first painted frame" —
 * that gap is a deliberate, documented approximation, not a stand-in for real load time. A single
 * process-wide span, not one per overlay instance: only the very first rating in a session is the
 * "cold" one measurement cares about.
 */
private object RiveFirstEffectTrace {
    private val began = AtomicBoolean(false)
    private val ended = AtomicBoolean(false)
    fun beginOnce() { if (began.compareAndSet(false, true)) Trace.beginSection("RiveFirstEffect") }
    fun endOnce() { if (began.get() && ended.compareAndSet(false, true)) Trace.endSection() }
}

private class RiveEffectSpec(val resId: Int, val stateMachine: String, val trigger: String)

/**
 * Which `.riv`/state-machine/trigger to fire for [effect] (FC2-09: Pick A replaces the old, opaque
 * `again.riv` with a transparent "Check/Error" asset). `Remembered` plays it together with
 * `confetti.riv`, not instead of it — the same "both, not either/or" pairing the web bridge uses
 * (`rive-bridge.js`'s `RATING_EFFECTS`, `FlipCardRivePlan.md` §12.7).
 */
private fun effectSpecs(effect: CardEffect): List<RiveEffectSpec> = when (effect) {
    CardEffect.Remembered -> listOf(
        RiveEffectSpec(R.raw.confetti, "State Machine 1", "Trigger explosion"),
        RiveEffectSpec(R.raw.again, "State Machine 1", "Check"),
    )
    CardEffect.Again -> listOf(RiveEffectSpec(R.raw.again, "State Machine 1", "Error"))
    CardEffect.None -> emptyList()
}

/**
 * Non-interactive Rive rating-effect overlay for the Android flash card (FC-15/16/20, FC2-09).
 * Decorative only: hidden from TalkBack via [hideFromAccessibility] and never receives touch
 * (`touchPassThrough = true`), so it can never intercept the flip/rating gestures it sits above.
 * Reduced motion and the measurement-variant debug flag are decided one level up (see
 * [cardEffectToPlay]); this composable simply plays whatever non-null [effect] it is given, once,
 * then calls [onConsumed] so the same rating never re-fires on recomposition. Up to two views are
 * kept warm (the largest [effectSpecs] list needs two, for `Remembered`); only as many as the
 * current effect needs are actually attached to the tree.
 */
@Composable
fun AndroidRiveOverlay(effect: CardEffect?, onConsumed: () -> Unit) {
    val context = LocalContext.current
    val views = remember(context) {
        ensureRiveInitialized(context)
        List(2) { RiveAnimationView(context).apply { touchPassThrough = true } }
    }
    var activeCount by remember { mutableIntStateOf(0) }
    DisposableEffect(effect) {
        if (effect != null && effect != CardEffect.None) {
            RiveFirstEffectTrace.beginOnce()
            val specs = effectSpecs(effect)
            specs.forEachIndexed { index, spec ->
                views[index].setRiveResource(spec.resId, stateMachineName = spec.stateMachine)
                views[index].fireState(spec.stateMachine, spec.trigger)
            }
            RiveFirstEffectTrace.endOnce()
            activeCount = specs.size
            onConsumed()
        }
        onDispose {}
    }
    Box(Modifier.fillMaxSize().semantics { hideFromAccessibility() }) {
        views.take(activeCount).forEach { view -> AndroidView(factory = { view }, modifier = Modifier.fillMaxSize()) }
    }
}

/**
 * Flip-in-progress ring cue (FC2-06/07/08, R2): a boolean `IsExpanded` input on `rings.riv`, driven
 * by [expanded] exactly as [AndroidFlipCard]'s `onRingsExpandedChange` reports it — expanding right
 * as an animated flip starts, contracting once it settles. Placed *behind* the card in z-order (the
 * caller composes this before the flip card in the same `Box`), never on top of it, since the cue is
 * decorative and must never cover the question/answer text.
 */
@Composable
fun AndroidFlipRingsOverlay(expanded: Boolean) {
    val context = LocalContext.current
    val view = remember(context) {
        ensureRiveInitialized(context)
        RiveAnimationView(context).apply {
            touchPassThrough = true
            setRiveResource(R.raw.rings, stateMachineName = "State Machine 1")
        }
    }
    LaunchedEffect(expanded) { view.setBooleanState("State Machine 1", "IsExpanded", expanded) }
    AndroidView(factory = { view }, modifier = Modifier.fillMaxSize().semantics { hideFromAccessibility() })
}

/**
 * One-shot chain-completion celebration (FC2-10, R3 Pick C): plays only the `chain-complete.riv`
 * file's `Tada` artboard, `Reveal` animation — a plain animation name, not a state-machine trigger,
 * autoplaying once on load. Mounted only while [CardPhase.ChainComplete] is showing (the caller
 * gates that, and reduced motion/the measurement flag, the same way the rating overlay is gated),
 * never on the card itself.
 */
@Composable
fun AndroidChainCompleteOverlay() {
    val context = LocalContext.current
    val view = remember(context) {
        ensureRiveInitialized(context)
        RiveAnimationView(context).apply {
            touchPassThrough = true
            setRiveResource(R.raw.chain_complete, artboardName = "Tada", animationName = "Reveal", autoplay = true)
        }
    }
    AndroidView(factory = { view }, modifier = Modifier.fillMaxSize().semantics { hideFromAccessibility() })
}
