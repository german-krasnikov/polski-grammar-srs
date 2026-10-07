package polski.interaction

import kotlin.math.abs
import polski.srs.Rating

/** Pure release decision. The host measures distance and owns gesture capture and dispatch. */
object SwipeDecision {
    fun rating(dx: Double, dy: Double, threshold: Double, axisRatio: Double,
               checkYOnRelease: Boolean): Rating? {
        require(dx.isFinite() && dy.isFinite() && threshold.isFinite() && threshold > 0.0)
        require(axisRatio.isFinite() && axisRatio > 0.0)
        if (abs(dx) < threshold || (checkYOnRelease && abs(dx) <= abs(dy) * axisRatio)) return null
        return if (dx > 0.0) Rating.Good else Rating.Again
    }
}
