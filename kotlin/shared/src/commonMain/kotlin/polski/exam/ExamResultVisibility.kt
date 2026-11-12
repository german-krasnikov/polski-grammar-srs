package polski.exam

import polski.exam.resultpolicy.Policy
import polski.exam.resultpolicy.data.RESULT_POLICY_OUTCOME_ROW_ROWS

/** Policy outcome for an available part or the aggregate timed sitting. */
internal data class ExamResultVisibility(val isFinal: Boolean, val showFinalPercent: Boolean)

internal fun examResultVisibility(
    resultReady: Boolean,
    sittingCompleted: Boolean,
    manualPendingQuarter: Int,
    possibleQuarter: Int,
    scope: String = "part",
): ExamResultVisibility {
    val disposition = Policy.disposition(
        Policy.DispositionInput(
            resultReady = resultReady,
            sittingCompleted = sittingCompleted,
            manualPendingQuarter = manualPendingQuarter.toDouble(),
            possibleQuarter = possibleQuarter.toDouble(),
            scope = scope,
        ),
    )
    val row = RESULT_POLICY_OUTCOME_ROW_ROWS.single { it.disposition == disposition }
    return ExamResultVisibility(row.isFinal, row.showFinalPercent)
}
