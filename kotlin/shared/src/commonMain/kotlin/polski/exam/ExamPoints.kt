package polski.exam

import polski.grammar.normalize

/** Exact-answer score for reading items, in quarter-point units. */
fun exactAnswerQuarter(answer: String, expected: String, maximumQuarter: Int): Int {
    require(maximumQuarter > 0) { "Question must have a positive maximum" }
    return if (normalize(answer) == normalize(expected)) maximumQuarter else 0
}

/** Formats an exact quarter-point score with a comma decimal separator. */
fun formatQuarterPoints(value: Int): String {
    require(value >= 0) { "Score cannot be negative" }
    val whole = value / 4
    return when (value % 4) {
        0 -> "$whole"
        1 -> "$whole,25"
        2 -> "$whole,5"
        else -> "$whole,75"
    }
}
