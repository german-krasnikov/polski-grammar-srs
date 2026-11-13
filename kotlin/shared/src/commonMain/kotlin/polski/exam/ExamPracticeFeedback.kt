package polski.exam

import polski.exam.grammar.GrammarExamScorer
import polski.exam.grammar.GrammarItem
import polski.exam.grammar.GrammarTest
import polski.exam.practicefeedbackpolicy.Policy

/** A source answer is fixed at its first valid practice commit. Bank identity is kept separately from grading text. */
data class CommittedAnswer(val itemId: String, val rawAnswer: String, val selectedTokenId: String? = null)

enum class PracticeInputEvent(val policyTag: String) { Choice("choice"), Check("check") }

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
    /** Settled source scorer results, including an earlier answer resolved by a later dependency. */
    val evaluations: Map<String, ExamAnswerEvaluation> = emptyMap(),
)

/** Commit a grammar input using the source scorer and DSL disposition; drafts remain in the part reducer. */
fun acceptGrammarPracticeInput(
    state: PracticeFeedbackState,
    test: GrammarTest,
    itemId: String,
    rawAnswer: String,
    enabled: Boolean,
    existingAnswers: Map<String, String> = emptyMap(),
    timed: Boolean = false,
    event: PracticeInputEvent = PracticeInputEvent.Choice,
    selectedTokenId: String? = null,
): PracticeFeedbackState {
    val item = test.items.firstOrNull { it.itemId == itemId }
    val complete = when (item?.questionType) {
        QuestionType.ChooseForm -> rawAnswer in item.options
        QuestionType.WordBank -> test.bankTokens.any { it.tokenId == selectedTokenId && it.text == rawAnswer }
        QuestionType.FillGap, QuestionType.Transform, QuestionType.WordFormation, QuestionType.QuestionFormation -> rawAnswer.isNotBlank()
        else -> false
    }
    val explicitCheckRequired = item?.questionType != QuestionType.ChooseForm && item?.questionType != QuestionType.WordBank
    val newCommitted = state.committed + (itemId to CommittedAnswer(itemId, rawAnswer, selectedTokenId))
    val ready = item != null && dependenciesReady(item, newCommitted)
    // The scorer sees the complete attempt. Readiness is based only on fixed answers, never an editable draft partner.
    val answers = existingAnswers + newCommitted.mapValues { it.value.rawAnswer }
    val evaluation = if (item != null && complete && ready && enabled && !timed && itemId !in state.committed) {
        GrammarExamScorer.gradeInAttempt(test, item, answers)
    } else null
    val disposition = Policy.disposition(Policy.DispositionInput(
        alreadyCommitted = itemId in state.committed,
        answerComplete = complete,
        dependenciesReady = ready,
        mode = if (timed) "timed" else "practice",
        preferenceEnabled = enabled,
        event = event.policyTag,
        explicitCheckRequired = explicitCheckRequired,
        gradeStatus = evaluation?.status?.tag ?: "none",
        hasNextQuestion = item?.let { candidate ->
            test.items.drop(test.items.indexOf(candidate) + 1).any { it.itemId !in newCommitted }
        } == true,
        validTarget = item != null,
    ))
    if (disposition != Policy.DispositionValues.CommitDeferred &&
        disposition != Policy.DispositionValues.CommitShowStay &&
        disposition != Policy.DispositionValues.CommitShowAdvance) return state
    val acceptedItem = item ?: return state
    val nextGeneration = state.generation + 1
    val settled = newCommitted.keys.mapNotNull { committedId ->
        val candidate = test.items.firstOrNull { it.itemId == committedId } ?: return@mapNotNull null
        if (!dependenciesReady(candidate, newCommitted)) return@mapNotNull null
        committedId to GrammarExamScorer.gradeInAttempt(test, candidate, answers)
    }.toMap()
    return state.copy(
        committed = newCommitted,
        focusedQuestionId = itemId,
        generation = nextGeneration,
        evaluations = settled,
        visible = settled[itemId]?.let {
            FeedbackView(
                itemId = itemId,
                chosen = rawAnswer,
                officialExamples = officialGrammarExamples(acceptedItem, test),
                evaluation = it,
                generation = nextGeneration,
                mayAutoAdvance = disposition == Policy.DispositionValues.CommitShowAdvance,
            )
        },
    )
}

private fun dependenciesReady(item: GrammarItem, committed: Map<String, CommittedAnswer>): Boolean =
    item.pairedWithItemId?.let { it in committed } != false &&
        item.scoredVariants.none { variant -> variant.onlyIfNotUsedByItemId?.let { it !in committed } == true }

/** Hide the overlay and invalidate host callbacks while preserving committed answers. */
fun clearPracticeFeedback(state: PracticeFeedbackState): PracticeFeedbackState =
    state.copy(visible = null, generation = state.generation + 1)

/** The next unanswered source item is preferred; a completed final item stays put. */
fun advancePracticeFocus(state: PracticeFeedbackState, sourceOrderedQuestionIds: List<String>): PracticeFeedbackState {
    val currentIndex = sourceOrderedQuestionIds.indexOf(state.focusedQuestionId)
    val next = sourceOrderedQuestionIds.drop(currentIndex + 1).firstOrNull { it !in state.committed }
    return clearPracticeFeedback(state).copy(focusedQuestionId = next ?: state.focusedQuestionId)
}
