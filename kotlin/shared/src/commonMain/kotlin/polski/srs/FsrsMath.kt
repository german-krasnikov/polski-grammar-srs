package polski.srs

import kotlin.math.*

/** Bounded port of ts-fsrs 5.4.2 FSRS-6 arithmetic. See THIRD_PARTY_NOTICES.md. */
internal object FsrsMath {
    val w = doubleArrayOf(0.212, 1.2931, 2.3065, 8.2956, 6.4133, 0.8334, 3.0194, 0.001, 1.8722, 0.1666, 0.796, 1.4835, 0.0614, 0.2629, 1.6483, 0.6014, 1.8729, 0.5425, 0.0912, 0.0658, 0.1542)
    private const val minStability = 0.001
    private const val maxStability = 36500.0
    private val decay = -w[20]
    private val factor = round8(exp(ln(0.9) / decay) - 1)
    private val intervalModifier = round8((0.9.pow(1 / decay) - 1) / factor)

    fun round8(x: Double): Double = floor(x * 100000000.0 + 0.5) / 100000000.0
    private fun jsRound(x: Double): Int = floor(x + 0.5).toInt()
    private fun clamp(x: Double, low: Double, high: Double) = min(max(x, low), high)

    fun nextState(d: Double, s: Double, t: Int, grade: Rating): Pair<Double, Double> {
        require(t >= 0) { "Invalid delta_t" }
        val g = grade.wire
        if (d == 0.0 && s == 0.0) {
            return clamp(round8(w[4] - exp((g - 1) * w[5]) + 1), 1.0, 10.0) to max(w[g - 1], 0.1)
        }
        require(d >= 1 && s >= minStability) { "Invalid memory state" }
        val r = forgettingCurve(t, s)
        val nextS = if (t == 0) {
            val sinc = s.pow(-w[19]) * exp(w[17] * (g - 3 + w[18]))
            round8(clamp(s * (if (g >= 2) max(sinc, 1.0) else sinc), minStability, maxStability))
        } else if (g == 1) {
            val fail = round8(clamp(w[11] * d.pow(-w[12]) * ((s + 1).pow(w[13]) - 1) * exp((1 - r) * w[14]), minStability, maxStability))
            clamp(round8(s / exp(w[17] * w[18])), minStability, fail)
        } else {
            val hard = if (g == 2) w[15] else 1.0
            val easy = if (g == 4) w[16] else 1.0
            round8(clamp(s * (1 + exp(w[8]) * (11 - d) * s.pow(-w[9]) * (exp((1 - r) * w[10]) - 1) * hard * easy), minStability, maxStability))
        }
        val delta = -w[6] * (g - 3)
        val damped = round8(delta * (10 - d) / 9)
        val initEasy = round8(w[4] - exp(3 * w[5]) + 1)
        val nextD = clamp(round8(w[7] * initEasy + (1 - w[7]) * (d + damped)), 1.0, 10.0)
        return nextD to nextS
    }

    private fun forgettingCurve(t: Int, s: Double): Double = round8((1 + factor * t / s).pow(decay))

    fun interval(stability: Double, elapsedDays: Int, seed: String, fuzz: Boolean): Int {
        val ivl = min(max(1, jsRound(stability * intervalModifier)), 3650)
        if (!fuzz || ivl < 2.5) return ivl
        var delta = 1.0
        delta += 0.15 * max(min(ivl.toDouble(), 7.0) - 2.5, 0.0)
        delta += 0.1 * max(min(ivl.toDouble(), 20.0) - 7.0, 0.0)
        delta += 0.05 * max(ivl - 20.0, 0.0)
        var low = max(2, jsRound(ivl - delta))
        val high = min(jsRound(ivl + delta), 3650)
        if (ivl > elapsedDays) low = max(low, elapsedDays + 1)
        low = min(low, high)
        return floor(Alea.first(seed) * (high - low + 1) + low).toInt()
    }
}
