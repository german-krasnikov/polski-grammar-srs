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

/** M3 blueprint: [done]/[planned] from [AppUiState.daily]; null outside [TrainingMode.Daily].
 *  [extended] mirrors [DailySessionState.extended] — true once the learner has voluntarily
 *  continued past the originally planned volume. */
data class DailyProgress(val done: Int, val planned: Int, val extended: Boolean)

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
    val dailyProgress: DailyProgress?,
    /** M2 blueprint: non-null exactly when [AppUiState.activeRetrySkillId] is set — "Ещё раз:
     *  {skill}", same rendering pattern as [lifehackBadge]. */
    val retryBadge: String?,
)

private val modeLabel = mapOf(
    TrainingMode.Chain to "Цепочка предложений",
    TrainingMode.Schedule to "По расписанию",
    TrainingMode.Focused to "Отдельный навык",
    TrainingMode.Daily to "Сегодня · ~5 мин",
)

/** M3 blueprint: pure, composes the completion summary from per-[DailyStepKind] counts. Total
 *  (never throws): an empty [history] — only reachable if [AppAction.StartDaily] itself hit
 *  [CardPhase.NoDue], not [CardPhase.DailyComplete] — still returns a neutral fallback string. */
fun dailySummaryText(history: List<DailyStepOutcome>): String {
    val reviews = history.count { it.kind == DailyStepKind.Review }
    val news = history.count { it.kind == DailyStepKind.New }
    val practice = history.count { it.kind == DailyStepKind.Practice }
    if (reviews == 0 && news == 0 && practice == 0) return "Сегодня пока нечего показать — начни сессию ещё раз."
    val parts = buildList {
        if (reviews > 0) add("повторил $reviews ${skillWord(reviews)}")
        if (news > 0) add("освоил $news ${skillWord(news)}")
        if (practice > 0) add("потренировал $practice ${skillWord(practice)}")
    }
    return "Готово! ${parts.joinToString(", ")}."
}

private fun skillWord(count: Int): String = when {
    count % 10 == 1 && count % 100 != 11 -> "навык"
    count % 10 in 2..4 && count % 100 !in 12..14 -> "навыка"
    else -> "навыков"
}

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
    // M7 blueprint: prediction always precedes the existing introduction — introducing only fires
    // once predicting is no longer current, for the exact same Question-phase moment.
    val predicting = state.phase == CardPhase.Question && state.predictionPending
    val introducing = state.phase == CardPhase.Question && state.introPending && !predicting
    val clockNow = now ?: state.now
    val retryBadge = state.activeRetrySkillId?.let { "Ещё раз: ${skillById(it).title}" }

    val header = TrainingHeader(
        modes = listOf(
            ModeOption(TrainingMode.Chain, modeLabel.getValue(TrainingMode.Chain), AppAction.StartChain()),
            ModeOption(TrainingMode.Schedule, modeLabel.getValue(TrainingMode.Schedule), AppAction.StartSchedule),
            ModeOption(TrainingMode.Focused, modeLabel.getValue(TrainingMode.Focused), AppAction.OpenSkillPicker),
            ModeOption(TrainingMode.Daily, modeLabel.getValue(TrainingMode.Daily), AppAction.StartDaily),
        ),
        selected = state.mode,
        dueCount = state.dueCount,
        todayCount = state.todayCount,
    )
    val dailyProgress = if (state.mode == TrainingMode.Daily) {
        state.daily?.let { DailyProgress(it.history.size, it.planned, it.extended) }
    } else null
    val referenceToggle = ReferenceToggle(
        label = if (!introducing && !predicting && state.showReference) "Скрыть таблицу" else "Таблица под рукой",
        enabled = !introducing && !predicting && state.caseReferenceAvailable,
        expanded = state.showReference && !introducing && !predicting && state.caseReferenceAvailable,
    )
    val chainProgress = if (state.mode == TrainingMode.Chain) {
        ChainProgress(state.chainDisplayCount, state.chain.size, courseChainPresentation.summary)
    } else null

    val exercise = state.exercise ?: return TrainingCardModel(
        header = header, chainProgress = chainProgress, referenceToggle = referenceToggle,
        front = emptyList(), back = emptyList(), ratingButtons = emptyList(),
        introducing = introducing, hotkeys = emptyMap(), lifehackBadge = null,
        dailyProgress = dailyProgress, retryBadge = retryBadge,
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

    if (predicting) {
        // M7 blueprint: exactly these three sections — no MethodIntro/retrieve/answer input yet.
        front += CardSection.SourceSentence(sourceParts)
        front += CardSection.PredictionQuestion(skill.predictionQuestion.orEmpty())
        front += CardSection.Action("Что скажешь? Дальше", AppAction.ContinuePrediction)
    } else if (introducing) {
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
            predicting -> put(HotKey.Space, AppAction.ContinuePrediction)
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
    // introPending/predictionPending (no teaser mid-introduction/prediction) — an explicit choice,
    // not derived ad hoc.
    val lifehackBadge = if (!introducing && !predicting && state.phase == CardPhase.Question && StaticPackLifehackProvider.hasLifehacks(exercise.primarySkill)) {
        LifehackBadge()
    } else null

    return TrainingCardModel(header, chainProgress, referenceToggle, front, back, ratingButtons, introducing, hotkeys, lifehackBadge, dailyProgress, retryBadge)
}

