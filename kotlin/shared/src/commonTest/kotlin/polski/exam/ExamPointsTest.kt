package polski.exam

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.exam.data.oracle.WEIGHTED_SCORING_CASE_ROW_ROWS
import polski.exam.data.oracle.QUARTER_FORMAT_CASE_ROW_ROWS

class ExamPointsTest {
    @Test fun readingWeightedOracle() {
        for (row in WEIGHTED_SCORING_CASE_ROW_ROWS) {
            assertEquals(row.earnedQuarter, exactAnswerQuarter(row.answer, row.expected, row.maximumQuarter), row.id)
        }
    }

    @Test fun quarterFormatting() {
        for (row in QUARTER_FORMAT_CASE_ROW_ROWS) {
            assertEquals(row.expectedText, formatQuarterPoints(row.pointsQuarter), row.id)
        }
    }
}
