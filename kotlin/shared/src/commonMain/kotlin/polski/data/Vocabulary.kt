package polski.data

/** EN-22: read fresh. pl-ru returns PlRuTypedPack fields (NJ03 P7). */
val vocabularyItems: List<VocabularyItem> get() = packRegistry.active.vocabulary
val frequencyItems: List<FrequencyItem> get() = packRegistry.active.frequency
fun vocabularyById(id: String): VocabularyItem? = vocabularyItems.firstOrNull { it.id == id }
