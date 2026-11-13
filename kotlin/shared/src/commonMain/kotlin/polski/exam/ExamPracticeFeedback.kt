package polski.exam

import polski.exam.grammar.GrammarExamScorer
import polski.exam.grammar.GrammarItem
import polski.exam.grammar.GrammarTest
import polski.exam.practicefeedbackpolicy.Policy
import polski.exam.reading.ReadingExam
import polski.exam.reading.ReadingExamScorer
import polski.exam.listening.ListeningExam
import polski.exam.listening.ListeningExamScorer

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

/** Keep grouped practice inside its source passage/task, wrapping to an earlier unanswered question. */
fun advanceGroupedPracticeFocus(state: PracticeFeedbackState, groupQuestionIds: List<String>): PracticeFeedbackState {
    val next = nextGroupedQuestion(state.focusedQuestionId, groupQuestionIds, state.committed.keys)
    return clearPracticeFeedback(state).copy(focusedQuestionId = next ?: state.focusedQuestionId)
}

private fun nextGroupedQuestion(currentId: String?, ids: List<String>, committedIds: Set<String>): String? {
    val index = ids.indexOf(currentId)
    if (index < 0) return null
    return (ids.drop(index + 1) + ids.take(index)).firstOrNull { it !in committedIds }
}

fun acceptReadingPracticeInput(
    state: PracticeFeedbackState, exam: ReadingExam, currentPassageId: String, questionId: String,
    rawAnswer: String, enabled: Boolean, existingAnswers: Map<String, String> = emptyMap(),
    timed: Boolean = false, event: PracticeInputEvent = PracticeInputEvent.Choice,
): PracticeFeedbackState {
    val group = exam.passages.firstOrNull { it.id == currentPassageId } ?: return state
    val questions = exam.questions.filter { it.passageId == group.id }
    val question = questions.firstOrNull { it.id == questionId } ?: return state
    val options = when (question.type) {
        QuestionType.TrueFalse -> question.options.ifEmpty { listOf("TAK", "NIE") }
        QuestionType.TrueFalseNotGiven -> question.options.ifEmpty { listOf("TAK", "NIE", "BRAK INFORMACJI") }
        else -> question.options
    }
    val complete = practiceAnswerComplete(rawAnswer, options, question.answerMode, question.answerSlotCount)
    val checkRequired = options.isEmpty()
    val evaluation = if (complete && enabled && !timed && questionId !in state.committed) {
        ReadingExamScorer.grade(question, rawAnswer)
    } else null
    return acceptGroupedInput(state, questionId, rawAnswer, event, enabled, timed, complete, checkRequired,
        questions.map { it.id }, evaluation, listOf(question.correctAnswer) + question.scoredVariants.map { it.answer })
}

fun acceptListeningPracticeInput(
    state: PracticeFeedbackState, exam: ListeningExam, currentTaskId: String, questionId: String,
    rawAnswer: String, enabled: Boolean, existingAnswers: Map<String, String> = emptyMap(),
    timed: Boolean = false, event: PracticeInputEvent = PracticeInputEvent.Choice,
): PracticeFeedbackState {
    val task = exam.tasks.firstOrNull { it.id == currentTaskId } ?: return state
    val question = task.questions.firstOrNull { it.id == questionId } ?: return state
    val options = if (question.type == QuestionType.TrueFalse) question.options.ifEmpty { listOf("PRAWDA", "FAŁSZ") }
        else question.options
    val complete = practiceAnswerComplete(rawAnswer, options, question.answerMode, question.answerSlotCount)
    val checkRequired = options.isEmpty()
    val evaluation = if (complete && enabled && !timed && questionId !in state.committed) {
        ListeningExamScorer.grade(question, rawAnswer)
    } else null
    return acceptGroupedInput(state, questionId, rawAnswer, event, enabled, timed, complete, checkRequired,
        task.questions.map { it.id }, evaluation, listOf(question.correctAnswer) + question.scoredVariants.map { it.answer })
}

private fun practiceAnswerComplete(raw: String, options: List<String>, mode: ExamAnswerMode, slots: Int): Boolean {
    val parts = if (mode == ExamAnswerMode.UnorderedSet) raw.split("|").map { it.trim() }.filter { it.isNotEmpty() }
        else listOf(raw.trim()).filter { it.isNotEmpty() }
    if (parts.size != slots || parts.distinct().size != parts.size) return false
    return options.isEmpty() || parts.all { it in options }
}

private fun acceptGroupedInput(
    state: PracticeFeedbackState, questionId: String, rawAnswer: String, event: PracticeInputEvent,
    enabled: Boolean, timed: Boolean, complete: Boolean, checkRequired: Boolean,
    groupIds: List<String>, evaluation: ExamAnswerEvaluation?, examples: List<String>,
): PracticeFeedbackState {
    val nextCommitted = state.committed + (questionId to CommittedAnswer(questionId, rawAnswer))
    val disposition = Policy.disposition(Policy.DispositionInput(
        alreadyCommitted = questionId in state.committed,
        answerComplete = complete,
        dependenciesReady = true,
        mode = if (timed) "timed" else "practice",
        preferenceEnabled = enabled,
        event = event.policyTag,
        explicitCheckRequired = checkRequired,
        gradeStatus = evaluation?.status?.tag ?: "none",
        hasNextQuestion = nextGroupedQuestion(questionId, groupIds, nextCommitted.keys) != null,
        validTarget = true,
    ))
    if (disposition != Policy.DispositionValues.CommitShowStay &&
        disposition != Policy.DispositionValues.CommitShowAdvance &&
        disposition != Policy.DispositionValues.CommitDeferred) return state
    val generation = state.generation + 1
    return state.copy(
        committed = nextCommitted,
        focusedQuestionId = questionId,
        generation = generation,
        evaluations = if (evaluation != null) state.evaluations + (questionId to evaluation) else state.evaluations,
        visible = evaluation?.let {
            FeedbackView(questionId, rawAnswer, examples.distinct(), it, generation,
                disposition == Policy.DispositionValues.CommitShowAdvance)
        },
    )
}
