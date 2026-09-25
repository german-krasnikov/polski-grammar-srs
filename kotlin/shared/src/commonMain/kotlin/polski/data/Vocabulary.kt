package polski.data

val vocabularyItems: List<VocabularyItem> by lazy { PolishCourseData.vocabulary }
val frequencyItems: List<FrequencyItem> by lazy { PolishCourseData.frequency }
fun vocabularyById(id: String): VocabularyItem? = vocabularyItems.firstOrNull { it.id == id }
