package polski.exam.setpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.exam.ExamAcceptedFact
import polski.exam.ExamGradingMode
import polski.exam.ExamSetAnswerSpec
import polski.exam.evaluateFactSetAnswer
import polski.exam.data.oracle.NOVEMBER_LISTENING_CASE_ROW_ROWS
import polski.exam.data.p9listening.LISTENING_QUESTION_ROW_ROWS
import polski.exam.data.p9listening.LISTENING_ACCEPTED_FACT_ROW_ROWS
import polski.exam.data.p9listening.LISTENING_ACCEPTED_FACT_ALIAS_ROW_ROWS

class P9ListeningSourceTest {
    @Test fun novemberLegendFactsRetainIdentityAcrossAliasesAndOrder() {
        val question = LISTENING_QUESTION_ROW_ROWS.single { it.questionId.endsWith("l5-q3") }
        val facts = LISTENING_ACCEPTED_FACT_ROW_ROWS.filter { it.questionRef == question.questionId }.map { fact ->
            ExamAcceptedFact(
                fact.factId, fact.displayText, fact.creditQuarter,
                LISTENING_ACCEPTED_FACT_ALIAS_ROW_ROWS.filter { it.factRef == fact.factId }.map { it.answerText },
            )
        }
        val spec = ExamSetAnswerSpec(
            requiredSelections = requireNotNull(question.requiredSelections),
            maximumQuarter = requireNotNull(question.pointsQuarter),
            facts = facts,
            gradingMode = ExamGradingMode.OpenManual,
        )
        for (row in NOVEMBER_LISTENING_CASE_ROW_ROWS) {
            val actual = evaluateFactSetAnswer(spec, row.answer)
            assertEquals(row.expectedStatus.tag, actual.status.tag, row.id)
            assertEquals(row.confirmedQuarter, actual.confirmedQuarter, row.id)
            assertEquals(row.pendingQuarter, actual.pendingQuarter, row.id)
        }
    }
}
