package polski.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.core.Rive
import dev.polski.grammarmatrix.compose.R
import polski.presentation.CardEffect

/** Loads the native Rive runtime once per process; [RiveAnimationView] requires this before construction. */
@Volatile private var riveRuntimeInitialized = false
private fun ensureRiveInitialized(context: Context) {
    if (riveRuntimeInitialized) return
    Rive.init(context.applicationContext)
    riveRuntimeInitialized = true
}

/**
 * Non-interactive Rive rating-effect overlay for the Android flash card (FC-15/16/20). Decorative
 * only: hidden from TalkBack via [hideFromAccessibility] and never receives touch
 * (`touchPassThrough = true`), so it can never intercept the flip/rating gestures it sits above.
 * Reduced motion and the measurement-variant debug flag are decided one level up (see
 * [cardEffectToPlay]); this composable simply plays whatever non-null [effect] it is given, once,
 * then calls [onConsumed] so the same rating never re-fires on recomposition.
 */
@Composable
fun AndroidRiveOverlay(effect: CardEffect?, onConsumed: () -> Unit) {
    val context = LocalContext.current
    val view = remember(context) {
        ensureRiveInitialized(context)
        RiveAnimationView(context).apply { touchPassThrough = true }
    }
    DisposableEffect(effect) {
        if (effect != null && effect != CardEffect.None) {
            val (resId, stateMachine, trigger) = when (effect) {
                CardEffect.Remembered -> Triple(R.raw.confetti, "State Machine 1", "Trigger explosion")
                CardEffect.Again -> Triple(R.raw.again, "Swipe to delete", "Trigger Delete")
                CardEffect.None -> Triple(null, "", "")
            }
            if (resId != null) {
                view.setRiveResource(resId, stateMachineName = stateMachine)
                view.fireState(stateMachine, trigger)
            }
            onConsumed()
        }
        onDispose {}
    }
    AndroidView(factory = { view }, modifier = Modifier.fillMaxSize().semantics { hideFromAccessibility() })
}
