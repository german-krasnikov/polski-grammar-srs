package polski.data

val vocabularyItems: List<VocabularyItem> by lazy { packRegistry.active.vocabulary }
val frequencyItems: List<FrequencyItem> by lazy { packRegistry.active.frequency }
fun vocabularyById(id: String): VocabularyItem? = vocabularyItems.firstOrNull { it.id == id }
