package polski.exam

import polski.exam.grammar.GrammarExamScorer
import polski.exam.grammar.GrammarTest
import polski.exam.practicefeedbackpolicy.Policy

/** The first complete practice answer is authoritative until this part is reset. */
data class CommittedAnswer(val itemId: String, val rawAnswer: String)

data class FeedbackView(
    val itemId: String,
    val chosen: String,
    val officialExamples: List<String>,
    val evaluation: ExamAnswerEvaluation,
    val generation: Long,
    val mayAutoAdvance: Boolean,
)

data class PracticeFeedbackState(
    val committed: Map<String, CommittedAnswer> = emptyMap(),
    val focusedQuestionId: String? = null,
    val visible: FeedbackView? = null,
    val generation: Long = 0,
)

/** Commit a source-backed grammar choice. Timed and preference-off callers keep their existing reducer. */
fun acceptGrammarPracticeInput(
    state: PracticeFeedbackState,
    test: GrammarTest,
    itemId: String,
    rawAnswer: String,
    enabled: Boolean,
    existingAnswers: Map<String, String> = emptyMap(),
    timed: Boolean = false,
): PracticeFeedbackState {
    val item = test.items.firstOrNull { it.itemId == itemId }
    val complete = item?.questionType == QuestionType.ChooseForm && rawAnswer in item.options
    val answers = existingAnswers + state.committed.mapValues { it.value.rawAnswer } + (itemId to rawAnswer)
    val dependenciesReady = item != null &&
        item.pairedWithItemId?.let { !answers[it].isNullOrBlank() } != false &&
        item.scoredVariants.none { variant -> variant.onlyIfNotUsedByItemId?.let { answers[it].isNullOrBlank() } == true }
    val evaluation = if (item != null && complete && dependenciesReady && enabled && !timed && itemId !in state.committed) {
        GrammarExamScorer.gradeInAttempt(test, item, answers)
    } else null
    val disposition = Policy.disposition(Policy.DispositionInput(
        alreadyCommitted = itemId in state.committed,
        answerComplete = complete,
        dependenciesReady = dependenciesReady,
        mode = if (timed) "timed" else "practice",
        preferenceEnabled = enabled,
        event = "choice",
        gradeStatus = evaluation?.status?.tag ?: "none",
        hasNextQuestion = item?.let { candidate ->
            test.items.drop(test.items.indexOf(candidate) + 1).any { it.itemId !in state.committed }
        } == true,
        validTarget = item != null,
    ))
    if (disposition != Policy.DispositionValues.CommitDeferred &&
        disposition != Policy.DispositionValues.CommitShowStay &&
        disposition != Policy.DispositionValues.CommitShowAdvance) return state
    val acceptedItem = item ?: return state
    val committed = state.committed + (itemId to CommittedAnswer(itemId, rawAnswer))
    val nextGeneration = state.generation + 1
    return state.copy(
        committed = committed,
        focusedQuestionId = itemId,
        generation = nextGeneration,
        visible = evaluation?.let {
            FeedbackView(
                itemId = itemId,
                chosen = rawAnswer,
                officialExamples = (listOf(acceptedItem.correctAnswer) + acceptedItem.scoredVariants.map { variant -> variant.answer }).distinct(),
                evaluation = it,
                generation = nextGeneration,
                mayAutoAdvance = disposition == Policy.DispositionValues.CommitShowAdvance,
            )
        },
    )
}

/** Hide the overlay and invalidate host callbacks while preserving committed answers. */
fun clearPracticeFeedback(state: PracticeFeedbackState): PracticeFeedbackState =
    state.copy(visible = null, generation = state.generation + 1)

/** The next unanswered source item is preferred; a completed final item stays put. */
fun advancePracticeFocus(state: PracticeFeedbackState, sourceOrderedQuestionIds: List<String>): PracticeFeedbackState {
    val currentIndex = sourceOrderedQuestionIds.indexOf(state.focusedQuestionId)
    val next = sourceOrderedQuestionIds.drop(currentIndex + 1).firstOrNull { it !in state.committed }
    return clearPracticeFeedback(state).copy(focusedQuestionId = next ?: state.focusedQuestionId)
}
