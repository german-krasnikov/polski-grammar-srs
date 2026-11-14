package polski.cloze

data class ClozeExercise(
    val id: String,
    val displaySentence: String,  // full sentence with "___" for the gap
    val correctForm: String,
    val options: List<String>,    // length == 3, shuffled; includes correctForm
    val source: ClozeSource,
    val gapLexemeKey: String,     // e.g. "noun:wife" — for analytics/debug
) {
    init {
        require(correctForm in options) { "correctForm must be in options for $id" }
        require(options.size == 3) { "options must have exactly 3 elements for $id" }
        require(options.distinct().size == 3) { "all options must be distinct for $id" }
    }
}

sealed interface ClozeSource {
    data class ExamItem(val itemId: String) : ClozeSource
    data class CourseExercise(val exerciseId: String, val skillId: String) : ClozeSource
}
