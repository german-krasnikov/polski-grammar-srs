package polski.ui

import polski.exam.officialGrammarData
import polski.exam.grammar.GrammarTest

/** Official source data lives in shared; this preserves the historical web-only sample alias. */
internal val examGrammarData: Map<String, List<GrammarTest>> by lazy {
    val b1 = officialGrammarData.getValue("b1-2019")
    officialGrammarData + ("b1-sample" to b1.take(1).map {
        it.copy(testId = "b1-sample", items = it.items.take(20))
    })
}
