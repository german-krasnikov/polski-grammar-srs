package polski.data

/** EN-22: read fresh on every call (not frozen at first access), same fix as [skills] — see its KDoc. */
val vocabularyItems: List<VocabularyItem> get() = packRegistry.active.vocabulary
val frequencyItems: List<FrequencyItem> get() = packRegistry.active.frequency
fun vocabularyById(id: String): VocabularyItem? = vocabularyItems.firstOrNull { it.id == id }
