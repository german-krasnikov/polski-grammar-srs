package polski.exam

import polski.exam.grammar.GrammarItem
import polski.exam.grammar.GrammarTest
import polski.exam.grammar.GrammarScoredVariant

/** Maps generated biome rows to domain model. Each pack provides its own generated rows;
 *  this mapper works generically over the row shapes (all packs share the same contract). */
object ExamDataMapper {

    /** Grammar tests from generated rows — groups items by testRef. */
    fun <T, I> grammarTests(
        tests: List<T>, items: List<I>,
        testId: (T) -> String, testInstruction: (T) -> String,
        itemTestRef: (I) -> String, itemId: (I) -> String,
        itemType: (I) -> String, itemContext: (I) -> String,
        itemGap: (I) -> String?, itemOptions: (I) -> String?,
        itemAnswer: (I) -> String, itemPoints: (I) -> Int?,
        itemQuarter: (I) -> Int? = { null },
        itemGradingMode: (I) -> String? = { null },
        variants: Map<String, List<GrammarScoredVariant>> = emptyMap(),
    ): List<GrammarTest> = tests.map { t ->
        val grouped = items.filter { itemTestRef(it) == testId(t) }
        GrammarTest(
            testId = testId(t),
            instruction = testInstruction(t),
            items = grouped.map { i ->
                val points = itemPoints(i)
                val quarter = itemQuarter(i)
                require(points != null || quarter != null) { "Missing score for ${itemId(i)}" }
                GrammarItem(
                    itemId = itemId(i),
                    questionType = QuestionType.fromTag(itemType(i)),
                    context = itemContext(i),
                    gap = itemGap(i),
                    options = itemOptions(i)?.split("|") ?: emptyList(),
                    correctAnswer = itemAnswer(i),
                    points = points ?: 0,
                    pointsQuarter = quarter,
                    scoredVariants = variants[itemId(i)].orEmpty(),
                    gradingMode = itemGradingMode(i)?.let(ExamGradingMode::fromTag) ?: ExamGradingMode.Exact,
                )
            },
        )
    }
}
