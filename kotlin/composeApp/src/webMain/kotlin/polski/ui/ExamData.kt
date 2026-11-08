package polski.ui

import polski.exam.QuestionType
import polski.exam.grammar.GrammarItem
import polski.exam.grammar.GrammarTest

/** Grammar tests from generated biome rows — single source of truth (.core-data → biome generate → typed rows). */
internal val examGrammarData: Map<String, List<GrammarTest>> by lazy {
    val b1 = mapB1Grammar()
    mapOf(
        "b1-2019" to b1,
        "b2-2019" to mapB2Grammar(),
        "b1-sample" to b1.take(1).map { it.copy(testId = "b1-sample", items = it.items.take(20)) },
    )
}

private fun mapB1Grammar(): List<GrammarTest> {
    val tests = polski.exam.data.b1y2019.GRAMMAR_TEST_ROW_ROWS
    val items = polski.exam.data.b1y2019.GRAMMAR_ITEM_ROW_ROWS
    return tests.map { t ->
        GrammarTest(t.testId, t.instruction, items.filter { it.testRef == t.testId }.map { i ->
            GrammarItem(i.itemId, QuestionType.fromTag(i.questionType.tag), i.context, i.gap, i.options?.split("|") ?: emptyList(), i.correctAnswer, i.points)
        })
    }
}

private fun mapB2Grammar(): List<GrammarTest> {
    val tests = polski.exam.data.b2y2019.GRAMMAR_TEST_ROW_ROWS
    val items = polski.exam.data.b2y2019.GRAMMAR_ITEM_ROW_ROWS
    return tests.map { t ->
        GrammarTest(t.testId, t.instruction, items.filter { it.testRef == t.testId }.map { i ->
            GrammarItem(i.itemId, QuestionType.fromTag(i.questionType.tag), i.context, i.gap, i.options?.split("|") ?: emptyList(), i.correctAnswer, i.points)
        })
    }
}
