package polski.interaction

import kotlin.math.abs
import polski.srs.Rating

/** Pure release decision. The host measures distance and owns gesture capture and dispatch.
 *  Business logic (threshold, axis, direction → rating) lives in swipe.logic DSL;
 *  this adapter computes the mechanical facts and maps the DSL result to Rating. */
object SwipeDecision {
    /** Shared "horizontal dominates vertical" ratio — hosts should call [locksHorizontal]
     *  instead of inlining this constant. */
    const val AXIS_RATIO = 1.25

    /** True when a horizontal drag of [dx]/[dy] is unambiguously horizontal (not diagonal/vertical). */
    fun locksHorizontal(dx: Double, dy: Double): Boolean = abs(dx) > abs(dy) * AXIS_RATIO

    fun rating(dx: Double, dy: Double, threshold: Double, axisRatio: Double,
               checkYOnRelease: Boolean): Rating? {
        require(dx.isFinite() && dy.isFinite() && threshold.isFinite() && threshold > 0.0)
        require(axisRatio.isFinite() && axisRatio > 0.0)
        // Mechanical facts — computed here, not in DSL (P4: pure arithmetic).
        val belowThreshold = abs(dx) < threshold
        val yDominates = checkYOnRelease && !locksHorizontal(dx, dy)
        val dxPositive = dx > 0.0
        // DSL decision — generated from swipe.logic.
        val result = Policy.rating(Policy.RatingInput(
            swipeBelowThreshold = belowThreshold,
            swipeDxPositive = dxPositive,
            swipeYDominates = yDominates,
        ))
        return when (result) {
            Policy.RatingValues.Good -> Rating.Good
            Policy.RatingValues.Again -> Rating.Again
            else -> null
        }
    }
}
