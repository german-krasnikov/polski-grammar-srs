package polski.exam

import polski.grammar.normalize

enum class ExamGradingMode(val tag: String) {
    Exact("exact"),
    OpenManual("open_manual");

    companion object {
        fun fromTag(tag: String): ExamGradingMode = entries.first { it.tag == tag }
    }
}

enum class ExamAnswerMode(val tag: String) {
    Single("single"),
    UnorderedSet("unordered_set");

    companion object {
        fun fromTag(tag: String): ExamAnswerMode = entries.first { it.tag == tag }
    }
}

enum class ExamAnswerStatus(val tag: String) {
    ConfirmedFull("confirmed_full"),
    ConfirmedPartial("confirmed_partial"),
    Wrong("wrong"),
    PendingManual("pending_manual"),
}

data class ExamAnswerVariant(val answer: String, val earnedQuarter: Int)

/** A confirmed score and the separate maximum awaiting human review. */
data class ExamAnswerEvaluation(
    val status: ExamAnswerStatus,
    val confirmedQuarter: Int,
    val pendingQuarter: Int,
)

/** Evaluates a printed answer and documented alternatives in exact quarter units. */
fun evaluateExamAnswer(
    mode: ExamGradingMode,
    fullAnswer: String,
    variants: List<ExamAnswerVariant>,
    answer: String,
    maximumQuarter: Int,
): ExamAnswerEvaluation {
    require(maximumQuarter > 0) { "Answer maximum must be positive" }
    require(normalize(fullAnswer).isNotBlank()) { "Full answer must not be blank" }
    require(variants.all { it.earnedQuarter in 1..maximumQuarter && normalize(it.answer).isNotBlank() }) {
        "Answer variants must have nonblank text and a score within the maximum"
    }
    val normalized = normalize(answer)
    if (normalized == normalize(fullAnswer)) return ExamAnswerEvaluation(ExamAnswerStatus.ConfirmedFull, maximumQuarter, 0)
    val variant = variants.firstOrNull { normalize(it.answer) == normalized }
    if (variant != null) {
        val status = if (variant.earnedQuarter == maximumQuarter) ExamAnswerStatus.ConfirmedFull else ExamAnswerStatus.ConfirmedPartial
        return ExamAnswerEvaluation(status, variant.earnedQuarter, 0)
    }
    if (normalized.isNotBlank() && mode == ExamGradingMode.OpenManual) {
        return ExamAnswerEvaluation(ExamAnswerStatus.PendingManual, 0, maximumQuarter)
    }
    return ExamAnswerEvaluation(ExamAnswerStatus.Wrong, 0, 0)
}

/** Evaluates a set answer; member order has no meaning. */
fun evaluateUnorderedSetAnswer(
    correctAnswer: String,
    options: List<String>,
    answer: String,
    maximumQuarter: Int,
    creditPerCorrectQuarter: Int? = null,
    mode: ExamGradingMode = ExamGradingMode.Exact,
): ExamAnswerEvaluation {
    require(maximumQuarter > 0) { "Set answer maximum must be positive" }
    val key = correctAnswer.split("|").map(::normalize)
    val choices = options.map(::normalize)
    require(key.isNotEmpty() && key.all { it.isNotBlank() } && key.distinct().size == key.size) { "Invalid set key" }
    require(choices.all { it.isNotBlank() } && choices.distinct().size == choices.size && (choices.isEmpty() || choices.containsAll(key))) {
        "Set choices must be distinct and include the key"
    }
    if (creditPerCorrectQuarter != null) {
        require(creditPerCorrectQuarter > 0 && creditPerCorrectQuarter * key.size == maximumQuarter) {
            "Per-choice credit must add up to the maximum"
        }
    }
    val selected = answer.split("|").map(::normalize)
    if (selected.any { it.isBlank() } || selected.distinct().size != selected.size || selected.size > key.size) {
        return ExamAnswerEvaluation(ExamAnswerStatus.Wrong, 0, 0)
    }
    if (choices.isNotEmpty() && !choices.containsAll(selected)) {
        return ExamAnswerEvaluation(ExamAnswerStatus.Wrong, 0, 0)
    }
    if (selected.size == key.size && selected.containsAll(key)) {
        return ExamAnswerEvaluation(ExamAnswerStatus.ConfirmedFull, maximumQuarter, 0)
    }
    val confirmedTokens = selected.count { it in key }
    val unknownTokens = selected.size - confirmedTokens
    if (creditPerCorrectQuarter != null) {
        val confirmedQuarter = confirmedTokens * creditPerCorrectQuarter
        val pendingQuarter = if (choices.isEmpty() && mode == ExamGradingMode.OpenManual) unknownTokens * creditPerCorrectQuarter else 0
        val status = when {
            pendingQuarter > 0 -> ExamAnswerStatus.PendingManual
            confirmedQuarter > 0 -> ExamAnswerStatus.ConfirmedPartial
            else -> ExamAnswerStatus.Wrong
        }
        return ExamAnswerEvaluation(status, confirmedQuarter, pendingQuarter)
    }
    if (choices.isEmpty() && unknownTokens > 0 && mode == ExamGradingMode.OpenManual) {
        return ExamAnswerEvaluation(ExamAnswerStatus.PendingManual, 0, maximumQuarter)
    }
    return ExamAnswerEvaluation(ExamAnswerStatus.Wrong, 0, 0)
}
