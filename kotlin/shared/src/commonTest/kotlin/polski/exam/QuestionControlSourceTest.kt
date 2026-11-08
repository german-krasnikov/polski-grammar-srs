package polski.exam

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.exam.data.oracle.BANK_TOKEN_CASE_ROW_ROWS
import polski.exam.data.oracle.QUESTION_CONTROL_CASE_ROW_ROWS
import polski.exam.grammar.GrammarBankToken
import polski.exam.grammar.GrammarItem
import polski.exam.grammar.GrammarTest

/** Fixed paper and key cases are authored in DSL, independently of production mappers. */
class QuestionControlSourceTest {
    private data class Token(val id: String, val testId: String, val position: Int, val text: String)
    private val banks =
        polski.exam.data.b1y2017.GRAMMAR_BANK_TOKEN_ROW_ROWS.map { Token(it.tokenId, it.testRef, it.position, it.text) } +
        polski.exam.data.b1y2019.GRAMMAR_BANK_TOKEN_ROW_ROWS.map { Token(it.tokenId, it.testRef, it.position, it.text) } +
        polski.exam.data.b1y2020.GRAMMAR_BANK_TOKEN_ROW_ROWS.map { Token(it.tokenId, it.testRef, it.position, it.text) } +
        polski.exam.data.b2y2017.GRAMMAR_BANK_TOKEN_ROW_ROWS.map { Token(it.tokenId, it.testRef, it.position, it.text) } +
        polski.exam.data.b2y2019.GRAMMAR_BANK_TOKEN_ROW_ROWS.map { Token(it.tokenId, it.testRef, it.position, it.text) } +
        polski.exam.data.b2y2020.GRAMMAR_BANK_TOKEN_ROW_ROWS.map { Token(it.tokenId, it.testRef, it.position, it.text) }

    @Test fun printedBanksKeepOrderAndDuplicateOccurrences() {
        for (case in BANK_TOKEN_CASE_ROW_ROWS) {
            val actual = banks.filter { it.testId == case.testId }.sortedBy { it.position }
            assertEquals(case.expectedOrderedTokenText.split("|"), actual.map { it.text }, case.id)
            assertEquals(actual.size, actual.map { it.id }.distinct().size, case.id)
            assertEquals(actual.indices.map { it + 1 }, actual.map { it.position }, case.id)
            assertEquals(case.expectedDuplicateCount, actual.size - actual.map { it.text }.distinct().size, case.id)
        }
    }

    @Test fun invalidBankFailsBeforeExamOpens() {
        val item = GrammarItem("source-item", QuestionType.WordBank, "___", null, emptyList(), "na", 0, 2)
        assertFailsWith<IllegalArgumentException> { GrammarTest("missing-bank", "", listOf(item)) }
        val duplicate = listOf(GrammarBankToken("a", 1, "na"), GrammarBankToken("b", 1, "na"))
        assertFailsWith<IllegalArgumentException> { GrammarTest("duplicate-position", "", listOf(item), duplicate) }
    }

    @Test fun missingChoiceOptionsFailBeforeExamOpens() {
        val item = GrammarItem("source-choice", QuestionType.ChooseForm, "___", null, emptyList(), "A", 0, 2)
        val error = assertFailsWith<IllegalArgumentException> {
            GrammarTest("source-test", "", listOf(item))
        }
        kotlin.test.assertContains(error.message.orEmpty(), "source-test")
        kotlin.test.assertContains(error.message.orEmpty(), "source-choice")
    }

    @Test fun sourceControlsRetainPayloadAndQuarterCredit() {
        for (case in QUESTION_CONTROL_CASE_ROW_ROWS) {
            val source = sourceControl(case.sourceItemId)
            assertEquals(case.expectedTag, source.type.tag, case.id)
            assertEquals(case.expectedKind.tag, source.kind, case.id)
            assertEquals(case.expectedOptionCount, source.options.size, case.id)
            case.expectedQuestionTarget?.let { assertEquals(it, source.questionTarget, case.id) }
            val result = if (source.kind == "unordered_set")
                evaluateUnorderedSetAnswer(source.answer, source.options, case.expectedRawAnswer, source.maximum, source.creditPerCorrect, source.mode)
            else evaluateExamAnswer(source.mode, source.answer, emptyList(), case.expectedRawAnswer, source.maximum)
            assertEquals(case.expectedQuarter, result.confirmedQuarter, case.id)
            assertEquals(case.expectedStatus.tag, result.status.tag, case.id)
        }
    }

    private data class Control(
        val type: QuestionType,
        val kind: String,
        val options: List<String>,
        val answer: String,
        val maximum: Int,
        val mode: ExamGradingMode = ExamGradingMode.Exact,
        val creditPerCorrect: Int? = null,
        val questionTarget: String? = null,
    )

    private fun sourceControl(id: String): Control {
        polski.exam.data.b2y2020.GRAMMAR_ITEM_ROW_ROWS.find { it.itemId == id }?.let { row ->
            val type = QuestionType.fromTag(row.questionType.tag)
            val options = banks.filter { it.testId == row.testRef }.map { it.text }
            return Control(type, "bank_option", options, row.correctAnswer, row.pointsQuarter ?: 0)
        }
        polski.exam.data.b1y2020.GRAMMAR_ITEM_ROW_ROWS.find { it.itemId == id }?.let { row ->
            return Control(QuestionType.fromTag(row.questionType.tag), "text", emptyList(), row.correctAnswer, row.pointsQuarter ?: 0, questionTarget = row.questionTarget)
        }
        polski.exam.data.b1y2017.GRAMMAR_ITEM_ROW_ROWS.find { it.itemId == id }?.let { row ->
            return Control(QuestionType.fromTag(row.questionType.tag), "text", emptyList(), row.correctAnswer, row.pointsQuarter ?: 0, questionTarget = row.questionTarget)
        }
        polski.exam.data.b1y2019.GRAMMAR_ITEM_ROW_ROWS.find { it.itemId == id }?.let { row ->
            return Control(QuestionType.fromTag(row.questionType.tag), "text", emptyList(), row.correctAnswer, row.pointsQuarter ?: 0, questionTarget = row.questionTarget)
        }
        polski.exam.data.b2y2017.GRAMMAR_ITEM_ROW_ROWS.find { it.itemId == id }?.let { row ->
            return Control(QuestionType.fromTag(row.questionType.tag), "text", emptyList(), row.correctAnswer, row.pointsQuarter ?: 0)
        }
        polski.exam.data.b2y2020.READING_QUESTION_ROW_ROWS.find { it.questionId == id }?.let { row ->
            return Control(QuestionType.fromTag(row.questionType.tag), "single_option", row.options?.split("|").orEmpty(), row.correctAnswer, row.pointsQuarter ?: 0)
        }
        polski.exam.data.b2y2017.LISTENING_QUESTION_ROW_ROWS.find { it.questionId == id }?.let { row ->
            return Control(QuestionType.fromTag(row.questionType.tag), "unordered_set", row.options?.split("|").orEmpty(), row.correctAnswer,
                row.pointsQuarter ?: 0, creditPerCorrect = row.creditPerCorrectQuarter)
        }
        polski.exam.data.b2y2020.LISTENING_QUESTION_ROW_ROWS.find { it.questionId == id }?.let { row ->
            return Control(QuestionType.fromTag(row.questionType.tag), "text", emptyList(), row.correctAnswer,
                row.pointsQuarter ?: 0, row.gradingMode?.tag?.let(ExamGradingMode::fromTag) ?: ExamGradingMode.Exact)
        }
        error("Source item not found: $id")
    }
}
