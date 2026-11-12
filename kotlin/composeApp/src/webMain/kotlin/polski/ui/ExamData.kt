package polski.ui

import polski.exam.officialGrammarData
import polski.exam.grammar.GrammarTest

/** The web host uses the shared published DSL catalog without additional sample aliases. */
internal val examGrammarData: Map<String, List<GrammarTest>> get() = officialGrammarData
