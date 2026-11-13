package polski.exam.grammar

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.exam.ExamAnswerStatus
import polski.exam.officialGrammarData

/** Official C1 paper/key pages identify ten numeric operands and three coupled answer pairs. */
class C1NumericPairSourceTest {
    @Test
    fun november2022PairCreditsOnlyThreePrintedCombinations() {
        val test = officialGrammarData.getValue("c1-2022-11").single { it.testId.endsWith("grammar-1") }
        val first = test.items.single { it.itemId.endsWith("g1-q9") }
        val second = test.items.single { it.itemId.endsWith("g1-q10") }
        val crossed = mapOf(first.itemId to "trzydzieściorgiem", second.itemId to "rozbitkami")
        assertEquals(ExamAnswerStatus.PendingManual,
            GrammarExamScorer.gradeInAttempt(test, first, crossed).status,
            "crossed number/noun forms cannot earn automatic credit")
        assertEquals(second.itemId, first.pairedWithItemId)
        assertEquals(first.itemId, second.pairedWithItemId)
        assertEquals(3, test.answerPairs.size)
        val allowed = listOf(
            "trzydziestu" to "rozbitkami",
            "trzydziestoma" to "rozbitkami",
            "trzydzieściorgiem" to "rozbitków",
        )
        for ((number, noun) in allowed) {
            val answers = mapOf(first.itemId to number, second.itemId to noun)
            for (item in listOf(first, second)) {
                val result = GrammarExamScorer.gradeInAttempt(test, item, answers)
                assertEquals(ExamAnswerStatus.ConfirmedFull, result.status, "$number $noun/${item.itemId}")
                assertEquals(2, result.confirmedQuarter, "$number $noun/${item.itemId}")
                assertEquals(0, result.pendingQuarter, "$number $noun/${item.itemId}")
            }
        }
        for ((number, noun) in listOf(
            "trzydzieściorgiem" to "rozbitkami",
            "trzydziestu" to "rozbitków",
        )) {
            val answers = mapOf(first.itemId to number, second.itemId to noun)
            for (item in listOf(first, second)) {
                val result = GrammarExamScorer.gradeInAttempt(test, item, answers)
                assertEquals(ExamAnswerStatus.PendingManual, result.status, "$number $noun/${item.itemId}")
                assertEquals(0, result.confirmedQuarter, "$number $noun/${item.itemId}")
                assertEquals(2, result.pendingQuarter, "$number $noun/${item.itemId}")
            }
        }
        assertEquals(ExamAnswerStatus.PendingManual,
            GrammarExamScorer.grade(first, first.correctAnswer).status, "a pair member alone is not auto-graded")
    }

    @Test
    fun printedNumericOperandsSurviveTheSharedQuestionProjection() {
        val operands = mapOf(
            "c1-2022-03" to mapOf(1 to "(3)", 7 to "(40)"),
            "c1-2022-11" to mapOf(9 to "(30)"),
            "c1-2023-04" to mapOf(3 to "(2)", 7 to "(300)", 9 to "(3)"),
            "c1-2023-11" to mapOf(1 to "(300)", 4 to "(4)", 6 to "(50)", 8 to "(10)"),
        )
        for ((route, cases) in operands) {
            val items = officialGrammarData.getValue(route).single { it.testId.endsWith("grammar-1") }.items
            for ((position, printed) in cases) {
                val item = items.single { it.itemId.endsWith("g1-q$position") }
                assertEquals(printed, item.context, item.itemId)
            }
        }
    }

    @Test
    fun authoredOfficialGrammarContextsContainMeaningfulContent() {
        for ((route, tests) in officialGrammarData) {
            for (item in tests.flatMap { it.items }) {
                if (item.context.isBlank()) continue
                // The source also contains a printed vulgar fraction, (¾).
                assertEquals(true, item.context.any { it.isLetterOrDigit() || it == '¾' },
                    "$route/${item.itemId} has punctuation-only context")
            }
        }
    }
}
