package polski.srs

import kotlin.math.floor

/** Seeded Alea/Mash first draw from ts-fsrs 5.4.2; original MIT notice in THIRD_PARTY_NOTICES.md. */
internal object Alea {
    private const val unit = 2.3283064365386963e-10 // 2^-32
    private fun uint32(x: Double): Long = x.toLong() and 0xffffffffL

    fun first(seed: String): Double {
        var n = 0xefc8249dL.toDouble()
        fun mash(value: String): Double {
            for (ch in value) { // Kotlin Char iteration is UTF-16, as JS charCodeAt.
                n += ch.code
                var h = 0.02519603282416938 * n
                n = uint32(h).toDouble()
                h -= n
                h *= n
                n = uint32(h).toDouble()
                h -= n
                n += h * 4294967296.0
            }
            return uint32(n) * unit
        }
        var s0 = mash(" ")
        val s1 = mash(" ")
        val s2 = mash(" ")
        s0 -= mash(seed)
        if (s0 < 0) s0 += 1
        var a1 = s1 - mash(seed)
        if (a1 < 0) a1 += 1
        var a2 = s2 - mash(seed)
        if (a2 < 0) a2 += 1
        val t = 2091639 * s0 + unit
        val c = t.toInt() // JS bitwise | 0, positive for this first draw.
        return t - c
    }
}

/** JS Number string for the finite seed products used by this scheduler. */
internal fun jsNumberString(value: Double): String {
    if (value == 0.0) return "0"
    val raw = value.toString().replace('E', 'e')
    val i = raw.indexOf('e')
    if (i < 0) return if (raw.endsWith(".0")) raw.dropLast(2) else raw
    val exponent = raw.substring(i + 1).toInt()
    val mantissa = raw.substring(0, i).removeSuffix(".0")
    if (exponent in -6..20) {
        val negative = mantissa.startsWith('-')
        val digits = mantissa.removePrefix("-").replace(".", "")
        val point = mantissa.removePrefix("-").substringBefore('.').length + exponent
        val result = when {
            point <= 0 -> "0." + "0".repeat(-point) + digits
            point >= digits.length -> digits + "0".repeat(point - digits.length)
            else -> digits.substring(0, point) + "." + digits.substring(point)
        }
        return if (negative) "-$result" else result
    }
    return mantissa + "e" + (if (exponent >= 0) "+" else "") + exponent
}
