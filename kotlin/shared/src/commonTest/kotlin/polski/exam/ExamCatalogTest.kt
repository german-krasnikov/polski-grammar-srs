package polski.exam

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExamCatalogTest {
    @Test
    fun officialCatalogUsesSourceIdsModulesAndGrammarWithoutWebSample() {
        val papers = listOf(
            polski.exam.data.b1y2017.EXAM_ROW_ROWS.single().examId,
            polski.exam.data.b1y2019.EXAM_ROW_ROWS.single().examId,
            polski.exam.data.b1y2020.EXAM_ROW_ROWS.single().examId,
            polski.exam.data.b2y2017.EXAM_ROW_ROWS.single().examId,
            polski.exam.data.b2y2019.EXAM_ROW_ROWS.single().examId,
            polski.exam.data.b2y2020.EXAM_ROW_ROWS.single().examId,
        )
        assertEquals(papers.toSet(), ExamCatalog.entries.map { it.sourceExamId }.toSet())
        assertEquals(6, ExamCatalog.entries.size)
        assertNull(ExamCatalog.find("b1-sample"))
        for (exam in ExamCatalog.entries) {
            assertEquals(exam, ExamCatalog.find(exam.routeId))
            assertEquals(listOf(ExamModule.Listening, ExamModule.Reading, ExamModule.Grammar, ExamModule.Writing),
                exam.modules.map { it.module })
            assertEquals(officialGrammarData.getValue(exam.routeId).sumOf { it.items.size }, exam.grammarItemCount)
            assertTrue(exam.modules.all { it.durationSeconds > 0 && it.maximumQuarter > 0 })
        }
    }

    @Test
    fun sharedGrammarRetainsSourceInstructionsBankIdentityAndConditionalKey() {
        val grammar = officialGrammarData.getValue("b2-2020")
        val sourceTests = polski.exam.data.b2y2020.GRAMMAR_TEST_ROW_ROWS
        val sourceItems = polski.exam.data.b2y2020.GRAMMAR_ITEM_ROW_ROWS
        assertEquals(sourceTests.map { it.testId to it.instruction }, grammar.map { it.testId to it.instruction })
        assertEquals(sourceItems.map { it.itemId to it.context }, grammar.flatMap { it.items }.map { it.itemId to it.context })
        assertEquals(sourceItems.map { it.itemId to it.questionTarget }, grammar.flatMap { it.items }.map { it.itemId to it.questionTarget })
        assertEquals(polski.exam.data.b2y2020.GRAMMAR_BANK_TOKEN_ROW_ROWS.map { it.tokenId }.toSet(),
            grammar.flatMap { it.bankTokens }.map { it.tokenId }.toSet())
        val conditional = polski.exam.data.b2y2020.GRAMMAR_ANSWER_VARIANT_ROW_ROWS.single { it.onlyIfNotUsedBy != null }
        val mapped = assertNotNull(grammar.flatMap { it.items }.single { it.itemId == conditional.itemRef }
            .scoredVariants.singleOrNull { it.answer == conditional.answer })
        assertEquals(conditional.onlyIfNotUsedBy, mapped.onlyIfNotUsedByItemId)
        assertEquals(conditional.earnedQuarter, mapped.earnedQuarter)
    }
}
