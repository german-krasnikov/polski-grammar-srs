package polski.exam

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import polski.exam.data.b1y2017.GRAMMAR_ANSWER_VARIANT_ROW_ROWS
import polski.exam.data.b1y2017.GRAMMAR_ITEM_ROW_ROWS
import polski.exam.data.b1y2017.GRAMMAR_TEST_ROW_ROWS
import polski.exam.data.b1y2017.LISTENING_QUESTION_ROW_ROWS
import polski.exam.data.b1y2017.LISTENING_TASK_ROW_ROWS
import polski.exam.data.b1y2017.READING_PASSAGE_ROW_ROWS
import polski.exam.data.b1y2017.READING_QUESTION_ROW_ROWS
import polski.exam.data.b1y2017.WRITING_TASK_ROW_ROWS
import polski.exam.data.oracle.EXAM_INVENTORY_CASE_ROW_ROWS
import polski.exam.data.oracle.ExamInventoryCaseRowSection
import polski.exam.data.oracle.WRITING_PROMPT_CASE_ROW_ROWS

/** Checks generated exam rows against independent paper inventory cases in the DSL. */
class B1y2017DataTest {
    @Test fun taskCountsAndOfficialPointTotals() {
        for (case in EXAM_INVENTORY_CASE_ROW_ROWS) {
            when (case.section) {
                ExamInventoryCaseRowSection.LISTENING -> {
                    val task = LISTENING_TASK_ROW_ROWS.single { it.taskId == case.taskRef }
                    val items = LISTENING_QUESTION_ROW_ROWS.filter { it.taskRef == case.taskRef }
                    assertEquals(case.itemCount, items.size, case.id)
                    assertEquals(case.pointsQuarter, items.sumOf { it.pointsQuarter ?: requireNotNull(it.points) * 4 }, case.id)
                    assertEquals(case.segmentStartMs, task.segmentStartMs, case.id)
                    assertEquals(case.segmentEndMs, task.segmentEndMs, case.id)
                    assertTrue(task.segmentEndMs > task.segmentStartMs, case.id)
                }
                ExamInventoryCaseRowSection.READING -> {
                    READING_PASSAGE_ROW_ROWS.single { it.passageId == case.taskRef }
                    val items = READING_QUESTION_ROW_ROWS.filter { it.passageRef == case.taskRef }
                    assertEquals(case.itemCount, items.size, case.id)
                    assertEquals(case.pointsQuarter, items.sumOf { it.pointsQuarter ?: requireNotNull(it.points) * 4 }, case.id)
                }
                ExamInventoryCaseRowSection.GRAMMAR -> {
                    GRAMMAR_TEST_ROW_ROWS.single { it.testId == case.taskRef }
                    val items = GRAMMAR_ITEM_ROW_ROWS.filter { it.testRef == case.taskRef }
                    assertEquals(case.itemCount, items.size, case.id)
                    assertEquals(case.pointsQuarter, items.sumOf { it.pointsQuarter ?: requireNotNull(it.points) * 4 }, case.id)
                    assertEquals(case.variantCount ?: 0, GRAMMAR_ANSWER_VARIANT_ROW_ROWS.count { variant -> items.any { it.itemId == variant.itemRef } }, case.id)
                }
            }
        }
        assertEquals(EXAM_INVENTORY_CASE_ROW_ROWS.count { it.section == ExamInventoryCaseRowSection.LISTENING }, LISTENING_TASK_ROW_ROWS.size)
        assertEquals(EXAM_INVENTORY_CASE_ROW_ROWS.count { it.section == ExamInventoryCaseRowSection.READING }, READING_PASSAGE_ROW_ROWS.size)
        assertEquals(EXAM_INVENTORY_CASE_ROW_ROWS.count { it.section == ExamInventoryCaseRowSection.GRAMMAR }, GRAMMAR_TEST_ROW_ROWS.size)
    }

    @Test fun writingSetsAndRequiredPhoto() {
        assertEquals(WRITING_PROMPT_CASE_ROW_ROWS.size, WRITING_TASK_ROW_ROWS.size)
        for (case in WRITING_PROMPT_CASE_ROW_ROWS) {
            val task = WRITING_TASK_ROW_ROWS.single { it.setId == case.setId && it.taskNumber == case.taskNumber }
            assertEquals(case.wordLimit, task.wordLimit, case.id)
            assertEquals(case.promptImageUrl, task.promptImageUrl, case.id)
            if (case.promptImageUrl != null) assertTrue(!task.promptImageAlt.isNullOrBlank(), case.id)
        }
    }
}
