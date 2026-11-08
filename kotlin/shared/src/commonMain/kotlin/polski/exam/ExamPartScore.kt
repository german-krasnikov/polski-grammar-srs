package polski.exam

/** [possibleQuarter] is the full official denominator, independent of the score range. */
data class ExamPartScore(
    val confirmedQuarter: Int, val possibleQuarter: Int, val manualPendingQuarter: Int,
    val evaluations: Map<String, ExamAnswerEvaluation> = emptyMap(),
) {
    val isFinal: Boolean get() = manualPendingQuarter == 0
    val finalPercent: Int? get() = if (isFinal && possibleQuarter > 0) (confirmedQuarter.toDouble() / possibleQuarter * 100).toInt() else null
}
