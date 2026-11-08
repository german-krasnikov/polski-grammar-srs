package polski.presentation

import kotlin.time.Instant
import polski.data.courseChainPresentation
import polski.data.presentationBySkillId
import polski.data.skillById
import polski.data.styleContentBySkillId
import polski.srs.Rating

/** A key a host's keyboard handler maps to the [AppAction] [TrainingCardModel.hotkeys] names for it. */
enum class HotKey { Space, Digit1, Digit2, ArrowLeft, ArrowRight }

/** The front-side "has a lifehack" teaser; never carries the lifehack's own text (that stays on
 *  [CardSection.LifehackSection], gated behind reveal). */
data class LifehackBadge(val text: String = "💡 Есть лайфхак", val contentDescription: String = "Есть лайфхак")

data class ModeOption(val mode: TrainingMode, val label: String, val action: AppAction)

data class TrainingHeader(val modes: List<ModeOption>, val selected: TrainingMode, val dueCount: Int, val todayCount: Int)

/** [displayCount]/[total] from [AppUiState.chainDisplayCount]; null outside [TrainingMode.Chain]. */
data class ChainProgress(val displayCount: Int, val total: Int, val summary: String)

data class ReferenceToggle(val label: String, val enabled: Boolean, val expanded: Boolean)

/** [label] stays plain ("Повторить"/"Вспомнил") — a host that wants a keyboard hint prepends its
 *  own hotkey glyph next to the button; that's a platform affordance, not card content. */
data class RatingButton(val rating: Rating, val label: String, val intervalLabel: String)

data class TrainingCardModel(
    val header: TrainingHeader,
    val chainProgress: ChainProgress?,
    val referenceToggle: ReferenceToggle,
    /** Visible before reveal: source sentence, prompt, the style's own Front blocks, and — only
     *  while [CardPhase.Question] — the retrieve hint, answer input and action button. */
    val front: List<CardSection>,
    /** Added once revealed; empty until [AppUiState.phase] is [CardPhase.Revealed]. */
    val back: List<CardSection>,
    val ratingButtons: List<RatingButton>,
    val introducing: Boolean,
    val hotkeys: Map<HotKey, AppAction>,
    val lifehackBadge: LifehackBadge?,
)

private val modeLabel = mapOf(
    TrainingMode.Chain to "Цепочка предложений",
    TrainingMode.Schedule to "По расписанию",
    TrainingMode.Focused to "Отдельный навык",
)

/**
 * Builds the one card-level view model every host's training screen renders from. Pure: the same
 * [state]/[now] always yields the same [TrainingCardModel] — no coroutine, no `Instant.now()` call,
 * no FSRS/progress mutation, no per-[StyleId] branch outside [StyleComposer] itself.
 *
 * [now] overrides [AppUiState.now] as the clock for [RatingButton.intervalLabel] — a host passes
 * its own live clock tick without funneling every tick through `dispatch(RefreshTime)`. Falls back
 * to [AppUiState.now] when null, then to the due instant itself (today's existing host fallback).
 */
fun trainingCardModel(state: AppUiState, now: Instant? = state.now): TrainingCardModel {
    val introducing = state.phase == CardPhase.Question && state.introPending
    val clockNow = now ?: state.now

    val header = TrainingHeader(
        modes = listOf(
            ModeOption(TrainingMode.Chain, modeLabel.getValue(TrainingMode.Chain), AppAction.StartChain()),
            ModeOption(TrainingMode.Schedule, modeLabel.getValue(TrainingMode.Schedule), AppAction.StartSchedule),
            ModeOption(TrainingMode.Focused, modeLabel.getValue(TrainingMode.Focused), AppAction.OpenSkillPicker),
        ),
        selected = state.mode,
        dueCount = state.dueCount,
        todayCount = state.todayCount,
    )
    val referenceToggle = ReferenceToggle(
        label = if (!introducing && state.showReference) "Скрыть таблицу" else "Таблица под рукой",
        enabled = !introducing,
        expanded = state.showReference && !introducing,
    )
    val chainProgress = if (state.mode == TrainingMode.Chain) {
        ChainProgress(state.chainDisplayCount, state.chain.size, courseChainPresentation.summary)
    } else null

    val exercise = state.exercise ?: return TrainingCardModel(
        header = header, chainProgress = chainProgress, referenceToggle = referenceToggle,
        front = emptyList(), back = emptyList(), ratingButtons = emptyList(),
        introducing = introducing, hotkeys = emptyMap(), lifehackBadge = null,
    )

    val skill = skillById(exercise.primarySkill)
    val presentation = presentationBySkillId(exercise.primarySkill)
    // Method text (not StyleComposer blocks) always keys off the requested style, same as every
    // host today — only SituationFirst needs the "situations" wording, the other 3 share "logic".
    val method = if (state.styleId == StyleId.SituationFirst) presentation.situations else presentation.logic
    val content = styleContentBySkillId(exercise.primarySkill)
    val requestedRecipe = StyleRegistry.recipes.getValue(state.styleId)
    val effective = StyleRegistry.recipes.getValue(StyleComposer.resolveEffectiveStyle(requestedRecipe, content, StyleRegistry.recipes))

    val front = mutableListOf<CardSection>()
    val back = mutableListOf<CardSection>()
    val sourceParts = sentenceHighlightParts(exercise.source, exercise.changes, ChangeSide.Before)

    if (introducing) {
        front += CardSection.SourceSentence(sourceParts)
        front += CardSection.MethodIntro(method.introduce)
        front += CardSection.Action("Перейти к заданию", AppAction.ContinueIntroduction)
    } else {
        front += CardSection.SourceSentence(sourceParts)
        front += CardSection.PromptBox(exercise.prompt, method.promptLead)
        front += StyleComposer.compose(effective, StylePhase.Front, exercise, skill, presentation, content).map(CardSection::Styled)
        if (state.phase == CardPhase.Question) {
            front += CardSection.RetrieveHint(method.retrieve)
            if (state.answerMode == AnswerMode.Typed) front += CardSection.AnswerInput(AnswerMode.Typed, state.draft)
            front += CardSection.Action(
                if (state.answerMode == AnswerMode.Typed) UiCopy.CheckAndShowAnswer else UiCopy.ShowAnswer,
                AppAction.Reveal(exercise.id),
            )
        }
        if (state.phase == CardPhase.Revealed) {
            back += StyleComposer.compose(effective, StylePhase.Back, exercise, skill, presentation, content).map(CardSection::Styled)
            back += CardSection.Expected(sentenceHighlightParts(exercise.expected, exercise.changes, ChangeSide.After), exercise.accepted)
            if (state.answerMode == AnswerMode.Typed) {
                back += CardSection.EvaluationFeedback(state.evaluation?.correct == true, state.frozenAnswer.orEmpty())
            }
            val methodHeading = if (state.styleId == StyleId.SituationFirst) "Сравни смысл и форму" else "Разбор изменений"
            back += CardSection.MethodFeedback(methodHeading, method.feedback, exercise.explanation)
            val lifehacks = StaticPackLifehackProvider.forSkill(exercise.primarySkill)
            if (lifehacks.isNotEmpty()) back += CardSection.LifehackSection(lifehacks)
            back += CardSection.ReviewHint(method.review)
        }
    }

    val ratingButtons = if (state.phase == CardPhase.Revealed) {
        listOf(Rating.Again to UiCopy.Again, Rating.Good to UiCopy.Good).map { (rating, label) ->
            val due = state.intervals?.get(rating)
            RatingButton(rating, label, due?.let { intervalLabel(it.toEpochMilliseconds(), (clockNow ?: it).toEpochMilliseconds()) }.orEmpty())
        }
    } else emptyList()

    val hotkeys = buildMap {
        when {
            introducing -> put(HotKey.Space, AppAction.ContinueIntroduction)
            // The one a *pure* function can express: a DOM-focused host instead relies on the
            // focused textarea eating the keydown first (no mode guard) — see the design doc's
            // "latent web gap" note. Typed + Question deliberately gets no Space entry here.
            state.phase == CardPhase.Question && state.answerMode == AnswerMode.Oral ->
                put(HotKey.Space, AppAction.Reveal(exercise.id))
            state.phase == CardPhase.Revealed -> {
                put(HotKey.Digit1, AppAction.Rate(exercise.id, Rating.Again))
                put(HotKey.Digit2, AppAction.Rate(exercise.id, Rating.Good))
                put(HotKey.ArrowLeft, AppAction.Rate(exercise.id, Rating.Again))
                put(HotKey.ArrowRight, AppAction.Rate(exercise.id, Rating.Good))
            }
        }
    }

    // Hidden once revealed (the back already shows the full LifehackSection) and during
    // introPending (no teaser mid-introduction) — an explicit choice, not derived ad hoc.
    val lifehackBadge = if (!introducing && state.phase == CardPhase.Question && StaticPackLifehackProvider.hasLifehacks(exercise.primarySkill)) {
        LifehackBadge()
    } else null

    return TrainingCardModel(header, chainProgress, referenceToggle, front, back, ratingButtons, introducing, hotkeys, lifehackBadge)
}

