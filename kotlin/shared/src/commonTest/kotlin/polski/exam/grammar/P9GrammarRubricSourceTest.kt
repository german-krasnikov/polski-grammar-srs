package polski.exam.grammar

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.exam.ExamDataMapper
import polski.exam.data.oracle.GRAMMAR_RUBRIC_CASE_ROW_ROWS
import polski.exam.data.p9rubric.GRAMMAR_TEST_ROW_ROWS
import polski.exam.data.p9rubric.GRAMMAR_ITEM_ROW_ROWS
import polski.exam.data.p9rubric.GRAMMAR_RUBRIC_COMPONENT_ROW_ROWS

class P9GrammarRubricSourceTest {
    @Test fun novemberComponentsRemainReviewCriteriaWithoutAutomaticPartialMatching() {
        val testId = "certyfikat-polski-C1-2022-11-grammar-2"
        val components = GRAMMAR_RUBRIC_COMPONENT_ROW_ROWS.groupBy(
            keySelector = { it.itemRef },
            valueTransform = { GrammarRubricComponent(it.componentId, it.order, it.creditQuarter, it.criterionText) },
        )
        val test = ExamDataMapper.grammarTests(
            tests = GRAMMAR_TEST_ROW_ROWS.filter { it.testId == testId },
            items = GRAMMAR_ITEM_ROW_ROWS,
            testId = { it.testId }, testInstruction = { it.instruction },
            itemTestRef = { it.testRef }, itemId = { it.itemId },
            itemType = { it.questionType.tag }, itemContext = { it.context },
            itemGap = { it.gap }, itemOptions = { it.options },
            itemAnswer = { it.correctAnswer }, itemPoints = { it.points }, itemQuarter = { it.pointsQuarter },
            itemGradingMode = { it.gradingMode?.tag },
            rubricComponents = components,
        ).single()
        for (case in GRAMMAR_RUBRIC_CASE_ROW_ROWS) {
            val item = test.items.single { it.itemId == case.itemRef }
            assertEquals(case.componentCount, item.rubricComponents.size, case.id)
            assertEquals(case.maximumQuarter, item.maximumQuarter, case.id)
            assertEquals(case.maximumQuarter, item.rubricComponents.sumOf { it.creditQuarter }, case.id)
            assertEquals("confirmed_full", GrammarExamScorer.grade(item, item.correctAnswer).status.tag, case.id)
            assertEquals("wrong", GrammarExamScorer.grade(item, "").status.tag, case.id)
            val unknown = GrammarExamScorer.grade(item, case.unknownAnswer)
            assertEquals("pending_manual", unknown.status.tag, case.id)
            assertEquals(0, unknown.confirmedQuarter, case.id)
            assertEquals(case.unknownPendingQuarter, unknown.pendingQuarter, case.id)
        }
    }
}
