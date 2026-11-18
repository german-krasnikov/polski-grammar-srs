package polski.cloze

/**
 * One "find the error" exercise. [tokens] is the error sentence split on whitespace (punctuation
 * stays attached to its token). [errorTokenIndex] points to the token containing [errorForm].
 * [correctToken] is the original token at that index (same position, correct form + punctuation).
 * Multi-word slots are not supported in MVP; [ErrorHuntGenerator] returns null for them.
 */
data class ErrorHuntExercise(
    val id: String,
    val tokens: List<String>,
    val errorTokenIndex: Int,
    val correctToken: String,
    val errorForm: String,
    val correctForm: String,
    /** 3 distinct shuffled options, contains [correctForm]. */
    val options: List<String>,
    val gapLexemeKey: String,
    val skillId: String,
    val ruleText: String?,
    val sourceExerciseId: String,
) {
    init {
        require(options.size == 3) { "options must have 3 elements for $id" }
        require(options.distinct().size == 3) { "all options must be distinct for $id" }
        require(correctForm in options) { "correctForm must be in options for $id" }
        require(errorForm != correctForm) { "errorForm must differ from correctForm for $id" }
        require(errorTokenIndex in tokens.indices) { "errorTokenIndex out of range for $id" }
    }
}
