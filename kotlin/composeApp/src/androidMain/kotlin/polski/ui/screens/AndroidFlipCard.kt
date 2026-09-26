package polski.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import polski.presentation.CardEffect
import polski.presentation.cardEffectFor
import polski.srs.Rating

/**
 * Pure host-local rating contract for the Android training card
 * (`Plans/Kotlin/FlipCardRivePlan.md` §0/FC-07/FC-20). D1 replaced the 3D flip this file used to
 * host with a downward expand-reveal (see [AndroidAnswerReveal]/[AndroidStaggeredReveal] below) —
 * these stay plain functions/classes (no Compose dependency) so they are unit-testable from
 * `androidApp/src/test` without a Compose test rule; see `AndroidFlipCardTest.kt`.
 */

/** Which rating (if any) a completed horizontal drag of [dragX] px selects, given [thresholdPx]. */
fun ratingForDrag(dragX: Float, thresholdPx: Float): Rating? = when {
    dragX <= -thresholdPx -> Rating.Again
    dragX >= thresholdPx -> Rating.Good
    else -> null
}

/** The Rive effect to play for [rating], or `null` when reduced motion or a measurement variant disables it (FC-16/20). */
fun cardEffectToPlay(rating: Rating, reduceMotion: Boolean, riveDisabledForMeasurement: Boolean): CardEffect? =
    if (reduceMotion || riveDisabledForMeasurement) null else cardEffectFor(rating)

/**
 * Ensures exactly one rating reaches [dispatch] per revealed card (FC-07/20), no matter how many
 * gestures or buttons race for it — the rating buttons and the answer panel's swipe both go
 * through the same gate instance. Plain class, not Compose state, so `remember { SingleRatingGate() }`
 * keeps one per card without pulling in a test rule to verify it.
 */
class SingleRatingGate {
    private var rated = false

    /** Rates once; returns the [CardEffect] to play, or `null` when already rated (or motion/measurement suppress it). */
    fun rate(rating: Rating, reduceMotion: Boolean, riveDisabledForMeasurement: Boolean, dispatch: (Rating) -> Unit): CardEffect? {
        if (rated) return null
        rated = true
        dispatch(rating)
        return cardEffectToPlay(rating, reduceMotion, riveDisabledForMeasurement)
    }
}

/**
 * Swipe-to-rate gesture for [AndroidAnswerReveal]'s panel (D1/D3 successor to the old flip's
 * back-face gesture, now without a tap-to-flip branch since there is nothing left to flip back
 * to): a horizontal drag past [thresholdPx] rates the card via [onRate]; a short or
 * vertical-dominant drag does nothing. Consumption is gated on confirmed horizontal intent
 * (mirroring [androidx.compose.foundation.gestures.detectHorizontalDragGestures]'s own touch-slop
 * cancellation) so a vertical drag is never consumed here and still reaches the screen's own
 * `verticalScroll` untouched.
 */
private suspend fun PointerInputScope.detectSwipeRating(thresholdPx: Float, tapSlopPx: Float, onRate: (Rating) -> Unit) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        var dx = 0f
        var dy = 0f
        var horizontal = false
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) break
            dx += change.positionChange().x
            dy += change.positionChange().y
            if (!horizontal) {
                val absDx = kotlin.math.abs(dx)
                val absDy = kotlin.math.abs(dy)
                if (absDx < tapSlopPx && absDy < tapSlopPx) continue // still undecided; don't consume
                if (absDy >= absDx) return@awaitEachGesture // vertical-dominant: let the ancestor scroll
                horizontal = true // horizontal-dominant past slop: this gesture now owns the pointer
            }
            change.consume()
        }
        ratingForDrag(dx, thresholdPx)?.let(onRate)
    }
}

/**
 * D1: the training card's answer panel — expands downward below the always-visible question the
 * moment it enters composition (the caller only composes this once [polski.presentation.CardPhase]
 * becomes `Revealed`, so its first composition IS the reveal moment). [MutableTransitionState]
 * starts at `false` while immediately targeting `true`: the standard idiom for playing an enter
 * transition on a composable's very first appearance — passing a plain `visible = true` from frame
 * one would have nothing to transition from and would just snap. `reduceMotion` skips the
 * transition and renders [content] directly. [onRate] shares the caller's [SingleRatingGate] with
 * the rating buttons inside [content] (FC-07/20) via [detectSwipeRating].
 */
@Composable
fun AndroidAnswerReveal(
    reduceMotion: Boolean,
    onRate: (Rating) -> Unit,
    content: @Composable () -> Unit,
) {
    if (reduceMotion) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) { content() }
        return
    }
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    val thresholdPx = with(LocalDensity.current) { 72.dp.toPx() }
    val tapSlopPx = with(LocalDensity.current) { 12.dp.toPx() }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = expandVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) +
            fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
    ) {
        Column(
            Modifier.fillMaxWidth().pointerInput(Unit) { detectSwipeRating(thresholdPx, tapSlopPx, onRate) },
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) { content() }
    }
}

/**
 * D1: one staggered fade+rise for a group inside [AndroidAnswerReveal] — the spec calls for
 * "answer, explanation, rating panel unfold... with short stagger", so [index] 0/1/2 space those
 * three groups ~70ms apart. `reduceMotion` renders [content] directly, matching
 * [AndroidAnswerReveal]'s own snap.
 */
@Composable
fun AndroidStaggeredReveal(index: Int, reduceMotion: Boolean, content: @Composable () -> Unit) {
    if (reduceMotion) {
        content()
        return
    }
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    val delay = index * 70
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(tween(220, delayMillis = delay)) +
            slideInVertically(animationSpec = tween(220, delayMillis = delay), initialOffsetY = { it / 10 }),
    ) { content() }
}
