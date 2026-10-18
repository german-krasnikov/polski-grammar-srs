package polski.data

/** EN-22: read fresh on every call (not frozen at first access), same fix as [skills] — see its KDoc. */
// NJ03: pl-ru vocabulary reads from typed Biome literal source.
val vocabularyItems: List<VocabularyItem> get() =
    if (packRegistry.active.id == "pl-ru") generatedPlRuVocabularyItems
    else packRegistry.active.vocabulary
val frequencyItems: List<FrequencyItem> get() = packRegistry.active.frequency
fun vocabularyById(id: String): VocabularyItem? = vocabularyItems.firstOrNull { it.id == id }
