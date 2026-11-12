package polski.exam.grammar

/** Combines source tasks without losing bank identity, item rubrics or official answer pairs. */
fun mergeGrammarTests(testId: String, tests: List<GrammarTest>): GrammarTest = GrammarTest(
    testId = testId,
    instruction = tests.firstOrNull()?.instruction.orEmpty(),
    items = tests.flatMap { it.items },
    bankTokens = tests.flatMap { it.bankTokens }.mapIndexed { index, token ->
        token.copy(position = index + 1)
    },
    answerPairs = tests.flatMap { it.answerPairs },
)
