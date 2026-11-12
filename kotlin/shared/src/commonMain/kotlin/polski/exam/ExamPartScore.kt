package polski.exam

/** [possibleQuarter] is the full official denominator, independent of the score range. */
data class ExamPartScore(
    val confirmedQuarter: Int, val possibleQuarter: Int, val manualPendingQuarter: Int,
    val evaluations: Map<String, ExamAnswerEvaluation> = emptyMap(),
) {
    val isFinal: Boolean get() = examResultVisibility(true, false, manualPendingQuarter, possibleQuarter).isFinal
    val finalPercent: Int? get() = if (examResultVisibility(true, false, manualPendingQuarter, possibleQuarter).showFinalPercent) (confirmedQuarter.toDouble() / possibleQuarter * 100).toInt() else null
}
