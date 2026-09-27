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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
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
 * D2 brings the same 90°-rotationY flip back for the vocabulary card only. D3
 * ([AndroidRatingDragSurface]) replaces the commit-or-nothing swipe both used with a live,
 * whole-card drag affordance and drops the visible rating buttons entirely on Android. These stay
 * plain functions/classes (no Compose dependency) where possible so they are unit-testable from
 * `androidApp/src/test` without a Compose test rule; see `AndroidFlipCardTest.kt`.
 */

/** Which rating (if any) a completed horizontal drag of [dragX] px selects, given [thresholdPx]. */
fun ratingForDrag(dragX: Float, thresholdPx: Float): Rating? = when {
    dragX <= -thresholdPx -> Rating.Again
    dragX >= thresholdPx -> Rating.Good
    else -> null
}

/** How far towards a committed rating a live drag of [dx] px is, in `[-1, 1]` (D3: drives the tint/label growth). */
fun dragProgress(dx: Float, thresholdPx: Float): Float =
    if (thresholdPx <= 0f) 0f else (dx / thresholdPx).coerceIn(-1f, 1f)

/** The card's slight tilt while dragged [dx] px, capped at [maxDegrees] (D3: "translate + slight tilt"). */
fun dragRotationDegrees(dx: Float, pxPerDegree: Float, maxDegrees: Float = 8f): Float =
    if (pxPerDegree <= 0f) 0f else (dx / pxPerDegree).coerceIn(-maxDegrees, maxDegrees)

/** The Rive effect to play for [rating], or `null` when reduced motion or a measurement variant disables it (FC-16/20). */
fun cardEffectToPlay(rating: Rating, reduceMotion: Boolean, riveDisabledForMeasurement: Boolean): CardEffect? =
    if (reduceMotion || riveDisabledForMeasurement) null else cardEffectFor(rating)

/**
 * Ensures exactly one rating reaches [dispatch] per revealed card (FC-07/20), no matter how many
 * gestures or accessibility actions race for it — the drag gesture and the TalkBack custom actions
 * on [AndroidRatingDragSurface] both go through the same gate instance. Plain class, not Compose
 * state, so `remember { SingleRatingGate() }` keeps one per card without pulling in a test rule.
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
 * Whether a gesture that moved ([dx], [dy]) px in total, without reaching a rating threshold, is a
 * tap (flips the vocabulary card back) rather than an aborted/vertical swipe (does nothing) — the
 * same "short and mostly-still" contract the pre-D1 training flip used for its own tap-vs-swipe
 * split (FC-07).
 */
fun isFlipTap(dx: Float, dy: Float, tapSlopPx: Float): Boolean =
    kotlin.math.abs(dx) < tapSlopPx && kotlin.math.abs(dy) < tapSlopPx

/**
 * Continuous drag recognizer behind [AndroidRatingDragSurface] (D3): reports every
 * horizontal-confirmed move via [onDrag] (drives the live translate/tilt/tint) and, once released,
 * lets the caller decide whether the final `(dx, dy)` commits a rating, flips a tap, or snaps back
 * — this stays a pure recognizer. Consumption is gated on confirmed horizontal intent, mirroring
 * [androidx.compose.foundation.gestures.detectHorizontalDragGestures]'s own touch-slop
 * cancellation: while the drag is still undecided (under [tapSlopPx]) nothing is consumed, and the
 * moment it turns out vertical-dominant this bails without consuming or calling back at all — so a
 * vertical scroll still reaches the ancestor `verticalScroll` untouched (superseded the old,
 * separate `detectSwipeRating`/`detectFlipOrSwipe`, which only reported the final release).
 */
private suspend fun PointerInputScope.detectDragGesture(
    tapSlopPx: Float,
    onDrag: (Float) -> Unit,
    onRelease: (dx: Float, dy: Float) -> Unit,
) {
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
            onDrag(dx)
        }
        onRelease(dx, dy)
    }
}

/**
 * D3 whole-card drag-to-rate affordance, shared by the training answer panel and the vocabulary
 * back face: the card translates and tilts with the finger ([dragRotationDegrees]), a tint and a
 * growing label preview which rating a release would commit ([AndroidDragRatingOverlay]), a
 * released drag under [thresholdPx] snaps back, and a committed drag flies off before [onRate]
 * fires — mirroring the web host's own drag language (`FlipCardRivePlan.md` §17.3/UX4-08..10).
 *
 * No visible rating buttons on Android (D3: "phones/tablets: no rating buttons") — the two ratings
 * are exposed as TalkBack [CustomAccessibilityAction]s on this surface instead, so screen-reader
 * users keep an always-available, equivalent way to rate without a drag (`compose-multiplatform-ui`
 * skill: "one accessible action per rating"). [onTap] (vocabulary only) fires for a short,
 * mostly-still release that isn't a rating commit; training has no tap action ([onTap] stays
 * `null`, a swipe short of the threshold there just snaps back).
 */
@Composable
fun AndroidRatingDragSurface(
    itemKey: Any,
    reduceMotion: Boolean,
    onRate: (Rating) -> Unit,
    onTap: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { 72.dp.toPx() }
    val tapSlopPx = with(density) { 12.dp.toPx() }
    val rotationPxPerDegree = with(density) { 22.dp.toPx() }
    val offsetX = remember(itemKey) { Animatable(0f) }
    val scope = rememberCoroutineScope()

    fun settle(target: Float) {
        scope.launch { if (reduceMotion) offsetX.snapTo(target) else offsetX.animateTo(target, tween(220)) }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .pointerInput(itemKey, thresholdPx, tapSlopPx) {
                detectDragGesture(
                    tapSlopPx = tapSlopPx,
                    onDrag = { dx -> scope.launch { offsetX.snapTo(dx) } },
                    onRelease = { dx, dy ->
                        val rating = ratingForDrag(dx, thresholdPx)
                        when {
                            rating != null -> {
                                val sign = if (rating == Rating.Good) 1f else -1f
                                scope.launch {
                                    if (reduceMotion) offsetX.snapTo(sign * thresholdPx * 4f)
                                    else offsetX.animateTo(sign * thresholdPx * 4f, tween(220))
                                    onRate(rating)
                                }
                            }
                            onTap != null && isFlipTap(dx, dy, tapSlopPx) -> onTap()
                            else -> settle(0f)
                        }
                    },
                )
            }
            .semantics {
                contentDescription = "Оценка карточки"
                customActions = listOf(
                    CustomAccessibilityAction("Повторить") { onRate(Rating.Again); true },
                    CustomAccessibilityAction("Вспомнил") { onRate(Rating.Good); true },
                )
            },
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .offset { IntOffset(kotlin.math.round(offsetX.value).toInt(), 0) }
                .graphicsLayer { rotationZ = dragRotationDegrees(offsetX.value, rotationPxPerDegree) },
        ) { content() }
        val progress = dragProgress(offsetX.value, thresholdPx)
        if (progress != 0f) AndroidDragRatingOverlay(progress)
    }
}

/**
 * Decorative tint + growing label for [AndroidRatingDragSurface] — never receives touch (a plain
 * `Box`, no gesture of its own) and is hidden from TalkBack ([hideFromAccessibility]), since the
 * surface's own [CustomAccessibilityAction]s already cover the same two ratings. Reuses the
 * existing error/primary tokens the rest of the app already uses for "before"/"after" contrast,
 * not a new palette (`compose-multiplatform-ui` skill: "a restrained semantic palette").
 */
@Composable
private fun BoxScope.AndroidDragRatingOverlay(progress: Float) {
    val again = (-progress).coerceIn(0f, 1f)
    val good = progress.coerceIn(0f, 1f)
    Box(Modifier.matchParentSize().semantics { hideFromAccessibility() }, contentAlignment = Alignment.Center) {
        if (again > 0f) Surface(
            color = MaterialTheme.colorScheme.error.copy(alpha = again * 0.30f),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.matchParentSize(),
        ) {}
        if (good > 0f) Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = good * 0.30f),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.matchParentSize(),
        ) {}
        val (label, weight) = if (again >= good) "Повторить" to again else "Вспомнил" to good
        if (weight > 0.05f) Text(
            label,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (again >= good) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.graphicsLayer {
                alpha = weight
                val scale = 0.85f + 0.15f * weight
                scaleX = scale
                scaleY = scale
            },
        )
    }
}

/**
 * Lets [AndroidSwipeHint] play its wiggle at most once per instance — one gate `remember`ed at the
 * screen level (not per-card) means the very first revealed card in a screen visit nudges and every
 * later card that screen shows does not (A4, `EmphasisUXAudit-2026-09-27.md` U2).
 */
class SwipeNudgeGate {
    private var shown = false

    /** `true` the first time this is called; `false` on every call after. */
    fun consumeFirstTime(): Boolean {
        if (shown) return false
        shown = true
        return true
    }
}

/**
 * Persistent, arrow-marked swipe-rating hint for the revealed card (A4, `EmphasisUXAudit-2026-09-27.md`
 * U2): ADR-8 keeps phones swipe-only with no rating buttons, so this closes the discoverability gap
 * for sighted users without adding any — [AndroidRatingDragSurface]'s TalkBack
 * [CustomAccessibilityAction]s already cover screen-reader users and are unaffected by this. On the
 * first instance of a fresh [nudgeGate] (and only while [reduceMotion] is `false` — the "Анимации"
 * toggle), the hint plays one brief left-right wiggle to draw the eye to the swipe direction.
 */
@Composable
fun AndroidSwipeHint(nudgeGate: SwipeNudgeGate, reduceMotion: Boolean) {
    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (!reduceMotion && nudgeGate.consumeFirstTime()) {
            offsetX.animateTo(-10f, tween(140))
            offsetX.animateTo(10f, tween(220))
            offsetX.animateTo(0f, tween(140))
        }
    }
    Row(
        Modifier.offset { IntOffset(kotlin.math.round(offsetX.value).toInt(), 0) },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("←", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
        Text("Повторить · Вспомнил", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("→", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }
}

/**
 * D1: the training card's answer panel — expands downward below the always-visible question the
 * moment it enters composition (the caller only composes this once [polski.presentation.CardPhase]
 * becomes `Revealed`, so its first composition IS the reveal moment). [MutableTransitionState]
 * starts at `false` while immediately targeting `true`: the standard idiom for playing an enter
 * transition on a composable's very first appearance — passing a plain `visible = true` from frame
 * one would have nothing to transition from and would just snap. `reduceMotion` skips the
 * transition but still wraps [content] in [AndroidRatingDragSurface] — rating must stay reachable
 * (D3 removed the button fallback) with instant, not skipped, drag settling. [onRate] shares the
 * caller's [SingleRatingGate] with [AndroidRatingDragSurface] (FC-07/20).
 */
@Composable
fun AndroidAnswerReveal(
    itemKey: Any,
    reduceMotion: Boolean,
    onRate: (Rating) -> Unit,
    content: @Composable () -> Unit,
) {
    if (reduceMotion) {
        AndroidRatingDragSurface(itemKey, reduceMotion = true, onRate = onRate) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) { content() }
        }
        return
    }
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = expandVertically(
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
            expandFrom = Alignment.Top,
        ) + fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
    ) {
        AndroidRatingDragSurface(itemKey, reduceMotion = false, onRate = onRate) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) { content() }
        }
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
 * D2 whole-panel flip for the vocabulary card (`FlipCardRivePlan.md` §16.0-B): [front]/[back] are
 * each expected to draw their own full panel chrome (background/border/radius/shadow) — the
 * rounded panel itself is what turns, not a static frame around rotating content (the same lesson
 * the web host's v4 pass already applied, §17.1). Reuses the exact `rotationY`/`cameraDistance`/
 * [Animatable] pattern the training card's own pre-D1 flip used (git `af126a6`/`b22e758`), keyed by
 * [itemId] instead of an exercise id. [revealed] auto-flips to the back the first time it becomes
 * true; once revealed, a further tap only turns the panel back and forth (visual only, never
 * re-reveals, never re-rates — [onRate] is still gated by the caller's own [SingleRatingGate]).
 * Only one face is composed at a time (split at the 90° midpoint), so the hidden face is never in
 * the accessibility tree and never receives touch.
 *
 * [front] receives this card's own local flip toggle as its lambda parameter, for a tap that lands
 * while [revealed] is already `true`: the caller's reveal control still calls its own "reveal"
 * action on every tap regardless of [revealed] (that call is what makes the *first* tap turn it
 * true), but session-style state that is already `true` re-writes the same value, a `StateFlow`
 * never re-emits an equal value, and [LaunchedEffect] never re-fires for a later front visit
 * (`main @ 25c378c` regression 2, `PostMergeTest-2026-09-27.md`). The caller invokes the parameter
 * only in that already-revealed case, so the very first reveal still goes through its own reveal
 * action undisturbed.
 *
 * Gesture/drag ([AndroidRatingDragSurface]) attaches only once `showingBack` (angle past 90°), not
 * the instant [revealed] turns true: `revealed` flips true the moment the flip *starts*, but
 * `front()` (with its own always-on tap-to-reveal control) keeps rendering and receiving touch for
 * the ~250ms `tween(500)` takes to cross 90°. Attaching the drag surface any earlier let a swipe
 * fired right after the reveal tap dispatch a rating before the answer face was ever shown, and let
 * a double-tap in that window race `front`'s reveal against this gesture's `toggleFlip`, reversing
 * the in-flight animation (`Plans/Kotlin/Lane-android.md`'s "gate on `showingBack`" correction).
 */
@Composable
fun AndroidFlipCard(
    itemId: String,
    revealed: Boolean,
    reduceMotion: Boolean,
    onRate: (Rating) -> Unit,
    front: @Composable (flipVisually: () -> Unit) -> Unit,
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
    fun toggleFlip() { scope.launch { setFlipped(!flipped) } }
    val cardContent: @Composable () -> Unit = {
        Box(Modifier.graphicsLayer { rotationY = angle; cameraDistance = 12f * density }) {
            if (!showingBack) front(::toggleFlip) else Box(Modifier.graphicsLayer { rotationY = 180f }) { back() }
        }
    }
    if (showingBack) {
        AndroidRatingDragSurface(itemId, reduceMotion, onRate = onRate, onTap = ::toggleFlip) { cardContent() }
    } else {
        Box(Modifier.fillMaxWidth()) { cardContent() }
    }
}
