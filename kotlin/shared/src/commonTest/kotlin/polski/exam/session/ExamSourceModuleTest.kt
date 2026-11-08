package polski.exam.session

import kotlin.test.*
import polski.exam.*
import polski.exam.data.oracle.EXAM_MODULE_CASE_ROW_ROWS
import polski.exam.data.oracle.READING_SET_CASE_ROW_ROWS

class ExamSourceModuleTest {
    private data class Cover(val id: String, val examRef: String, val module: String, val order: Int, val seconds: Int, val quarter: Int)

    @Test fun certifiedRowsMatchIndependentCoverOracle() {
        val actual = buildList {
            polski.exam.data.b1y2017.EXAM_MODULE_ROW_ROWS.forEach { add(Cover(it.moduleId, it.examRef, it.module.tag, it.order, it.durationSeconds, it.maximumQuarter)) }
            polski.exam.data.b1y2019.EXAM_MODULE_ROW_ROWS.forEach { add(Cover(it.moduleId, it.examRef, it.module.tag, it.order, it.durationSeconds, it.maximumQuarter)) }
            polski.exam.data.b1y2020.EXAM_MODULE_ROW_ROWS.forEach { add(Cover(it.moduleId, it.examRef, it.module.tag, it.order, it.durationSeconds, it.maximumQuarter)) }
            polski.exam.data.b2y2017.EXAM_MODULE_ROW_ROWS.forEach { add(Cover(it.moduleId, it.examRef, it.module.tag, it.order, it.durationSeconds, it.maximumQuarter)) }
            polski.exam.data.b2y2019.EXAM_MODULE_ROW_ROWS.forEach { add(Cover(it.moduleId, it.examRef, it.module.tag, it.order, it.durationSeconds, it.maximumQuarter)) }
            polski.exam.data.b2y2020.EXAM_MODULE_ROW_ROWS.forEach { add(Cover(it.moduleId, it.examRef, it.module.tag, it.order, it.durationSeconds, it.maximumQuarter)) }
        }
        val expected = EXAM_MODULE_CASE_ROW_ROWS.map { Cover(it.id, it.examRef, it.module.tag, it.order, it.durationSeconds, it.maximumQuarter) }
        assertEquals(expected.sortedBy { it.id }, actual.sortedBy { it.id })
        assertEquals(6, actual.groupBy { it.examRef }.size)
        actual.groupBy { it.examRef }.values.forEach { rows ->
            assertEquals(listOf(1, 2, 3, 4), rows.map { it.order }.sorted())
            assertEquals(setOf("listening", "reading", "grammar", "writing"), rows.map { it.module }.toSet())
        }
    }

    @Test fun b2February2024PrintedReadingGroupsAcceptAnyKeyOrder() {
        assertEquals(listOf(4, 12, 4, 8, 4, 8), READING_SET_CASE_ROW_ROWS.map { it.maximumQuarter })
        READING_SET_CASE_ROW_ROWS.forEach { row ->
            val evaluation = evaluateUnorderedSetAnswer(row.correctAnswer, row.options.split("|"), row.answer,
                row.maximumQuarter, row.creditPerCorrectQuarter)
            assertEquals(row.expectedConfirmedQuarter, evaluation.confirmedQuarter, row.id)
            assertEquals(ExamAnswerStatus.ConfirmedFull, evaluation.status, row.id)
        }
    }
}
