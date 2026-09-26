package polski.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import polski.presentation.CardEffect
import polski.presentation.cardEffectFor
import polski.srs.Rating

/**
 * Pure host-local rating contract shared by the Android training card and (below,
 * [isFlipTap]/[AndroidFlipCard]) the vocabulary card's D2 whole-panel flip
 * (`Plans/Kotlin/FlipCardRivePlan.md` §0/FC-07/FC-20/§16.0-B). D1 replaced the training card's 3D
 * flip with a downward expand-reveal (see [AndroidAnswerReveal]/[AndroidStaggeredReveal] below);
 * D2 brings the same 90°-rotationY flip back for the vocabulary card only. These stay plain
 * functions/classes (no Compose dependency) where possible so they are unit-testable from
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
        enter = expandVertically(
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
            expandFrom = Alignment.Top,
        ) + fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
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

/**
 * Whether a gesture that moved ([dx], [dy]) px in total, without reaching a rating threshold, is a
 * tap (flips the vocabulary card back) rather than an aborted/vertical swipe (does nothing) — the
 * same "short and mostly-still" contract the pre-D1 training flip used for its own tap-vs-swipe
 * split (FC-07).
 */
fun isFlipTap(dx: Float, dy: Float, tapSlopPx: Float): Boolean =
    kotlin.math.abs(dx) < tapSlopPx && kotlin.math.abs(dy) < tapSlopPx

/**
 * One recognizer for the vocabulary card's revealed back face (D2/FC-04/07/14): a tap flips the
 * card back and forth, a horizontal drag past [thresholdPx] rates it, and anything in between (a
 * short or vertical swipe) does neither. A single detector, not a `clickable` layered over a
 * separate drag [pointerInput] — stacking those independently let a `clickable` ancestor win over
 * a drag descendant in practice on this exact card (`FlipCardRivePlan.md` §14's evidence log), so
 * this reuses the merged approach that fixed it there.
 *
 * Consumption is gated on confirmed horizontal intent, mirroring
 * [androidx.compose.foundation.gestures.detectHorizontalDragGestures]'s own touch-slop
 * cancellation: while the drag is still undecided (under [tapSlopPx]) nothing is consumed, and the
 * moment it turns out vertical-dominant this bails without consuming — so a vertical scroll still
 * reaches the ancestor `verticalScroll` untouched.
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
 * D2 whole-panel flip for the vocabulary card (`FlipCardRivePlan.md` §16.0-B): [front]/[back] are
 * each expected to draw their own full panel chrome (background/border/radius/shadow) — the
 * rounded panel itself is what turns, not a static frame around rotating content (the same lesson
 * the web host's v4 pass already applied, §17.1). Reuses the exact `rotationY`/`cameraDistance`/
 * [Animatable] pattern the training card's own pre-D1 flip used (git `af126a6`/`b22e758`), keyed by
 * [itemId] instead of an exercise id. [revealed] auto-flips to the back the first time it becomes
 * true; once revealed, a further tap only turns the panel back and forth — it never re-reveals and
 * never re-rates ([onRate] is still gated by the caller's own [SingleRatingGate], so at most one
 * rating reaches it either way). Only one face is composed at a time (split at the 90° midpoint),
 * so the hidden face is never in the accessibility tree and never receives touch.
 */
@Composable
fun AndroidFlipCard(
    itemId: String,
    revealed: Boolean,
    reduceMotion: Boolean,
    enableSwipeRating: Boolean,
    onRate: (Rating) -> Unit,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
) {
    var flipped by remember(itemId) { mutableStateOf(false) }
    val angleAnim = remember(itemId) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    suspend fun setFlipped(target: Boolean) {
        if (flipped == target) return
        flipped = target
        val to = if (target) 180f else 0f
        if (reduceMotion) angleAnim.snapTo(to) else angleAnim.animateTo(to, tween(500))
    }
    LaunchedEffect(revealed, itemId) { if (revealed) setFlipped(true) }
    val angle = angleAnim.value
    val density = LocalDensity.current.density
    val showingBack = angle >= 90f
    val thresholdPx = with(LocalDensity.current) { 72.dp.toPx() }
    val tapSlopPx = with(LocalDensity.current) { 12.dp.toPx() }
    fun toggleFlip() { scope.launch { setFlipped(!flipped) } }
    // Gesture detection lives on this OUTER, untransformed Box, never on a rotationY-carrying
    // descendant: a rotationY(180°) child mirrors its local X axis, which would silently flip the
    // sign of every measured drag (see the plan's evidence log for the training card's own version
    // of this bug). The rotation itself lives purely on the inner Box below, for drawing. Before
    // `revealed`, there is nothing to flip back to and no rating to give, so no gesture at all —
    // the front face's own tap-to-reveal control lives inside [front] itself.
    val gesture = when {
        !revealed -> Modifier
        enableSwipeRating -> Modifier.pointerInput(itemId, thresholdPx, tapSlopPx) {
            detectFlipOrSwipe(thresholdPx, tapSlopPx, onTap = ::toggleFlip, onRate = onRate)
        }
        else -> Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { toggleFlip() }
    }
    Box(Modifier.fillMaxWidth().then(gesture)) {
        Box(Modifier.graphicsLayer { rotationY = angle; cameraDistance = 12f * density }) {
            if (!showingBack) front() else Box(Modifier.graphicsLayer { rotationY = 180f }) { back() }
        }
    }
}
