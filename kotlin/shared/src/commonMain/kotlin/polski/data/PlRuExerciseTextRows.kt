package polski.data

import polski.data.rows.plru.*
import polski.model.SentenceSeed

/** R1.7: the pl-ru sentence seeds and the dictionaries of the exercise text, from the generated rows (a dictionary keeps the order of its rows). */
internal val generatedPlRuSentenceSeeds: List<SentenceSeed> = SEED_ROW_ROWS.map { SentenceSeed(it.nounId, it.adjectiveId) }
internal val generatedPlRuExerciseCopy: Map<String, String> = COPY_ENTRY_ROW_ROWS.associate { it.key to it.value }
internal val generatedPlRuExercisePatterns: Map<String, String> = PATTERN_ENTRY_ROW_ROWS.associate { it.key to it.value }
internal val generatedPlRuCaseSentencePrefixes: Map<String, String> = PREFIX_ENTRY_ROW_ROWS.associate { it.key to it.value }
