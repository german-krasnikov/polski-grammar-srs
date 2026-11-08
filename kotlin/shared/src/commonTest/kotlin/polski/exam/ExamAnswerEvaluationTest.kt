package polski.exam

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.exam.data.oracle.ANSWER_EVALUATION_CASE_ROW_ROWS
import polski.exam.data.oracle.UNORDERED_SET_CASE_ROW_ROWS

class ExamAnswerEvaluationTest {
    @Test fun generatedOracleCases() {
        for (row in ANSWER_EVALUATION_CASE_ROW_ROWS) {
            val variants = if (row.variantAnswer == null) emptyList() else listOf(
                ExamAnswerVariant(row.variantAnswer, requireNotNull(row.variantQuarter)),
            )
            val result = evaluateExamAnswer(
                mode = ExamGradingMode.fromTag(row.mode.tag),
                fullAnswer = row.fullAnswer,
                variants = variants,
                answer = row.answer,
                maximumQuarter = row.maximumQuarter,
            )
            assertEquals(row.expectedStatus.tag, result.status.tag, row.id)
            assertEquals(row.expectedConfirmedQuarter, result.confirmedQuarter, row.id)
            assertEquals(row.expectedPendingQuarter, result.pendingQuarter, row.id)
        }
    }

    @Test fun unorderedSetOracleCases() {
        for (row in UNORDERED_SET_CASE_ROW_ROWS) {
            val result = evaluateUnorderedSetAnswer(
                correctAnswer = row.correctAnswer,
                options = row.options.takeIf { it.isNotBlank() }?.split("|") ?: emptyList(),
                answer = row.answer,
                maximumQuarter = row.maximumQuarter,
                creditPerCorrectQuarter = row.creditPerCorrectQuarter,
                mode = row.mode?.tag?.let(ExamGradingMode::fromTag) ?: ExamGradingMode.Exact,
            )
            assertEquals(row.expectedStatus.tag, result.status.tag, row.id)
            assertEquals(row.expectedConfirmedQuarter, result.confirmedQuarter, row.id)
            assertEquals(row.expectedPendingQuarter ?: 0, result.pendingQuarter, row.id)
        }
    }
}
