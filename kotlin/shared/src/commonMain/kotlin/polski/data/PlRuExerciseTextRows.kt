package polski.data

import polski.data.rows.plru.*

/** R1.7: the dictionaries of the pl-ru exercise text, from the generated rows (a dictionary keeps the order of its rows). The sentence seeds are in PlRuLexiconRows.kt. */
internal val generatedPlRuExerciseCopy: Map<String, String> = COPY_ENTRY_ROW_ROWS.associate { it.key to it.value }
internal val generatedPlRuExercisePatterns: Map<String, String> = PATTERN_ENTRY_ROW_ROWS.associate { it.key to it.value }
internal val generatedPlRuCaseSentencePrefixes: Map<String, String> = PREFIX_ENTRY_ROW_ROWS.associate { it.key to it.value }
