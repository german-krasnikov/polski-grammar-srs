package polski.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import polski.presentation.CardEffect
import polski.presentation.CardPhase
import polski.presentation.cardEffectFor
import polski.srs.Rating

/**
 * Pure host-local flip/rating contract for the Android flash card
 * (`Plans/Kotlin/FlipCardRivePlan.md` §0, FC-10). The flip is purely visual: it never dispatches
 * an [polski.presentation.AppAction] and never reads or mutates [CardPhase]/FSRS state. These are
 * plain functions/classes (no Compose dependency) so they are unit-testable from
 * `androidApp/src/test` without a Compose test rule — see `AndroidFlipCardTest.kt`.
 */

/** A tap only turns the card while [CardPhase.Revealed] — there is no answer face before reveal. */
fun flipOnTap(phase: CardPhase, current: Boolean): Boolean = if (phase == CardPhase.Revealed) !current else current

/** Which rating (if any) a completed horizontal drag of [dragX] px selects, given [thresholdPx]. */
fun ratingForDrag(dragX: Float, thresholdPx: Float): Rating? = when {
    dragX <= -thresholdPx -> Rating.Again
    dragX >= thresholdPx -> Rating.Good
    else -> null
}

/**
 * Whether a gesture that moved ([dx], [dy]) px in total, without reaching a rating threshold, is a
 * tap (flips the card) rather than an aborted/vertical swipe (does nothing, per FC-07: a short or
 * vertical swipe must not rate — nor should it surprise the user by flipping the card either).
 */
fun isFlipTap(dx: Float, dy: Float, tapSlopPx: Float): Boolean = kotlin.math.abs(dx) < tapSlopPx && kotlin.math.abs(dy) < tapSlopPx

/** The Rive effect to play for [rating], or `null` when reduced motion or a measurement variant disables it (FC-16/20). */
fun cardEffectToPlay(rating: Rating, reduceMotion: Boolean, riveDisabledForMeasurement: Boolean): CardEffect? =
    if (reduceMotion || riveDisabledForMeasurement) null else cardEffectFor(rating)

/**
 * Ensures exactly one rating reaches [dispatch] per revealed card (FC-07/20), no matter how many
 * gestures or buttons race for it — the rating buttons and the back-face swipe both go through the
 * same gate instance. Plain class, not Compose state, so `remember { SingleRatingGate() }` keeps
 * one per card without pulling in a test rule to verify it.
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
 * One recognizer for the revealed back-face (FC-04/07/14): a tap flips the card, a horizontal drag
 * past [thresholdPx] rates it, and anything in between (a short or vertical swipe) does neither.
 * Deliberately a single detector rather than a `clickable` layered over a separate drag
 * `pointerInput` — stacking those two independently let the ancestor's tap win over the
 * descendant's drag in practice, silently swallowing every swipe (see the plan's evidence log).
 *
 * Consumption is gated on confirmed horizontal intent, mirroring
 * [androidx.compose.foundation.gestures.detectHorizontalDragGestures]'s own touch-slop
 * cancellation: while the drag is still undecided (under [tapSlopPx]) nothing is consumed, and the
 * moment it turns out vertical-dominant this bails without ever consuming a change. The training
 * screen wraps this card in `Modifier.verticalScroll`, and Compose's Main pass gives this
 * descendant first look at every pointer move — consuming unconditionally (as an earlier version
 * did) silently ate every scroll attempt that happened to start on the card. Only once horizontal
 * dominance is confirmed does this start consuming, so a vertical drag still reaches the ancestor
 * scrollable untouched.
 */
private suspend fun PointerInputScope.detectFlipOrSwipe(thresholdPx: Float, tapSlopPx: Float, onTap: () -> Unit, onRate: (Rating) -> Unit) {
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
        val rating = ratingForDrag(dx, thresholdPx)
        when {
            rating != null -> onRate(rating)
            isFlipTap(dx, dy, tapSlopPx) -> onTap()
        }
    }
}

/**
 * 3D flip container (FC-10): [front] is the question face, [back] the revealed answer face. Only
 * one face is composed at a time (split at the 90° midpoint), so the hidden face is never in the
 * accessibility tree and never receives touch — simpler than mounting both and hiding one, and
 * equivalent for a11y since there is nothing on the back face before [CardPhase.Revealed] anyway.
 * `reduceMotion` (the app's [polski.preferences.Motion.Reduced] setting) snaps instead of
 * animating, per the plan's shared reduced-motion contract (FC-09/12/14/20). [onRate] shares the
 * caller's [SingleRatingGate] with the rating buttons inside [back] (FC-07/20).
 */
@Composable
fun AndroidFlipCard(
    exerciseId: String,
    phase: CardPhase,
    reduceMotion: Boolean,
    onRate: (Rating) -> Unit,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
) {
    var flipped by remember(exerciseId) { mutableStateOf(false) }
    LaunchedEffect(phase) { if (phase == CardPhase.Revealed) flipped = true }
    val angle by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = if (reduceMotion) snap() else tween(500),
        label = "cardFlip",
    )
    val density = LocalDensity.current.density
    fun flip() { flipped = flipOnTap(phase, flipped) }
    val showingBack = angle >= 90f
    val thresholdPx = with(LocalDensity.current) { 72.dp.toPx() }
    val tapSlopPx = with(LocalDensity.current) { 12.dp.toPx() }
    // Gesture detection lives on this OUTER, untransformed Box, never on a rotationY-carrying
    // descendant: a rotationY(180°) child mirrors its local X axis, which silently flipped the
    // sign of every measured drag and made real swipes fail to cross the threshold (see the
    // plan's evidence log). The rotation itself lives purely on the inner Box below, for drawing.
    val gesture = when {
        showingBack -> Modifier.pointerInput(exerciseId, thresholdPx, tapSlopPx) {
            detectFlipOrSwipe(thresholdPx, tapSlopPx, onTap = ::flip, onRate = onRate)
        }
        // Front has no competing drag gesture, so a plain `clickable` is safe here.
        phase == CardPhase.Revealed -> Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { flip() }
        else -> Modifier
    }
    Box(Modifier.fillMaxWidth().then(gesture)) {
        Box(Modifier.graphicsLayer { rotationY = angle; cameraDistance = 12f * density }) {
            if (!showingBack) front() else Box(Modifier.graphicsLayer { rotationY = 180f }) { back() }
        }
    }
}
