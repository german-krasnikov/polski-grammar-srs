package polski.presentation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import polski.core.engine.RandomSource
import polski.core.engine.evaluateTileAnswer
import polski.core.model.ScaffoldingLevel
import polski.core.model.TileSegment
import polski.core.model.TileSelection
import polski.data.activePackSkillIds
import polski.data.packRegistry
import polski.data.presentationBySkillId
import polski.data.skillById
import polski.data.skills
import polski.data.styleContentBySkillId
import polski.data.nouns
import polski.data.adjectives
import polski.data.verbs
import polski.model.Exercise
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.progress.LoadResult
import polski.progress.MigrationResult
import polski.progress.ProgressCodec
import polski.progress.ProgressDocument
import polski.progress.ProgressRepository
import polski.progress.ReviewReducer
import polski.progress.ReviewResult
import polski.progress.SaveResult
import polski.progress.SkillQueue
import polski.srs.Rating
import polski.srs.Scheduler
import polski.training.PlExerciseEngine
import polski.training.evaluate
import polski.training.sentenceSeeds

/**
 * Session owner. Dispatch and state collection belong to one UI thread. The supplied scope
 * owns this store; [close] cancels its child writer. A new page load creates a new store and reloads
 * durable progress, while draft, reveal and chain position remain session-only values.
 *
 * Writes are serialized by one channel. Every accepted review/reset advances [AppUiState.revision]
 * before enqueueing a snapshot. A failed write leaves the current in-memory document exportable.
 */
class TrainingStore(
    private val repository: ProgressRepository,
    private val scheduler: Scheduler,
    private val exerciseEngine: PlExerciseEngine,
    private val time: TimeSource,
    ownerScope: CoroutineScope,
    initialStyleId: StyleId = StyleId.RuleFirst,
    initialAnswerMode: AnswerMode = AnswerMode.Oral,
    /** SB-6 blueprint §1.2: defaulted so every existing call site keeps compiling unchanged;
     *  mirrors the `RandomSource { Random.nextDouble() }` pattern each host already builds for
     *  [polski.training.PlExerciseEngine]. */
    private val tileRandom: RandomSource = RandomSource { kotlin.random.Random.nextDouble() },
) {
    private val scope = CoroutineScope(ownerScope.coroutineContext + SupervisorJob(ownerScope.coroutineContext[Job]))
    private val writes = Channel<Write>(Channel.UNLIMITED)
    private val knownSkillIds = skills.map { it.id }
    private val knownSkillIdSet = knownSkillIds.toSet()
    private val reviewReducer = ReviewReducer(scheduler, knownSkillIdSet)
    /** UniversalCorePlan.md §6/EN-10: the active pack's skills only, re-read from [packRegistry]
     * on every call (see [activePackSkillIds]) rather than this store's own frozen [knownSkillIdSet]. */
    private val skillQueue = SkillQueue { it in activePackSkillIds(packRegistry) }
    private var document: ProgressDocument? = null
    private var recoveryRaw: String? = null
    private var nextEffectId = 0L
    private var started = false
    private var closed = false

    private val initialChain = exerciseEngine.generateChain()
    private val mutableState = MutableStateFlow(
        AppUiState(
            chain = initialChain.toList(), exercise = initialChain.first(), styleId = initialStyleId,
            answerMode = initialAnswerMode, tileBuilder = deriveTileBuilder(initialChain.first()),
        ),
    )
    val state: StateFlow<AppUiState> = mutableState

    /** SB-6 blueprint §1.1: the single derivation site every `exercise = ...` assignment below
     *  reuses — unconditional (computed regardless of `answerMode`), `null` exactly when
     *  [exercise] opts out of tiles ([tileBuilderStateFor]'s own documented fallback).
     *  SB-8: [source] defaults to the `document` field so every existing single-arg call site
     *  keeps compiling unchanged, picking up the right document because in every one of those
     *  call sites `document` is already reassigned to the document this exercise's stats should
     *  come from before this runs. The scaffolding rung comes from that document's own
     *  [polski.progress.SkillStats.tileScaffoldingRung] for this exercise's skill, defaulting to 0
     *  (Full) when there is no document yet or no stats recorded for this skill. */
    private fun deriveTileBuilder(exercise: Exercise?, source: ProgressDocument? = document): TileBuilderState? =
        exercise?.let {
            val rung = source?.progress?.stats?.get(it.primarySkill)?.tileScaffoldingRung ?: 0
            tileBuilderStateFor(it, tileRandom, scaffoldingLevelFor(rung))
        }

    private fun scaffoldingLevelFor(rung: Int): ScaffoldingLevel = when (rung) {
        0 -> ScaffoldingLevel.Full
        1 -> ScaffoldingLevel.Partial
        else -> ScaffoldingLevel.None
    }

    init {
        scope.launch {
            for (write in writes) {
                val result = try {
                    when (write) {
                        is Write.Save -> repository.save(write.document)
                        is Write.Reset -> repository.resetConfirmed(write.at, write.localDay, knownSkillIds)
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Throwable) {
                    SaveResult.WriteFailed(error)
                }
                if (!closed) completeWrite(write.revision, result)
            }
        }
    }

    /** Load preview or migrate legacy; never replace recovery-needed bytes with a fresh save. */
    suspend fun start() {
        if (started || closed) return
        started = true
        withContext(scope.coroutineContext) {
            val captured = time.capture()
            val result = try { repository.load() } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                LoadResult.Unavailable(error)
            }
            if (closed) return@withContext
            when (result) {
                is LoadResult.Loaded -> install(result.document, captured)
                LoadResult.Missing -> {
                    val fresh = ProgressCodec.fresh(knownSkillIds, captured.at, captured.localDay, scheduler)
                    install(fresh, captured)
                    queueSave(fresh)
                }
                is LoadResult.LegacyAvailable -> {
                    recoveryRaw = result.raw
                    mutableState.value = state.value.copy(
                        loadStatus = LoadStatus.MigrationAvailable,
                        error = "Найден прежний прогресс. Сначала экспортируй JSON или перенеси его.",
                    )
                }
                is LoadResult.RecoveryRequired -> recovery(result.raw, result.reason)
                is LoadResult.Invalid -> recovery(result.raw, result.reason)
                is LoadResult.Unsupported -> recovery(result.raw, "Неподдерживаемая версия ${result.version}")
                is LoadResult.Unavailable -> unavailable(result.cause)
            }
        }
    }

    /** Close once when the host unmounts; later actions and delayed completions are ignored. */
    fun close() {
        if (closed) return
        closed = true
        writes.close()
        scope.cancel()
    }

    fun dispatch(action: AppAction) {
        if (closed) return
        val s = state.value
        if (!accepts(action, s)) return
        when (action) {
            is AppAction.EffectAcknowledged -> acknowledge(action)
            is AppAction.ResetDecision -> resetDecision(action)
            AppAction.RequestExport -> export()
            AppAction.RequestMigration -> requestMigration()
            AppAction.RequestReset -> addEffect { UiEffect.ConfirmReset(it, "Сбросить весь прогресс?") }
            AppAction.RefreshTime -> refreshTime()
            is AppAction.SelectTab -> {
                mutableState.value = s.copy(
                    tab = action.tab,
                    matrixSelection = if (action.tab == AppTab.Matrix && s.tab != AppTab.Matrix) MatrixSelection() else s.matrixSelection,
                )
            }
            is AppAction.SelectMatrixSection -> mutate { it.copy(matrixSelection = it.matrixSelection.copy(section = action.section)) }
            is AppAction.SetMatrixSelection -> {
                val selection = action.selection
                if (nouns.none { it.id == selection.nounId } ||
                    adjectives.none { it.id == selection.adjectiveId } ||
                    verbs.none { it.id == selection.verbId } ||
                    PossessiveId.entries.none { it.id == selection.ownerId } ||
                    NumberGram.entries.none { it.id == selection.numberId }
                ) mutate { it.copy(error = "Неизвестное значение в таблице") }
                else mutate { it.copy(matrixSelection = selection, error = null) }
            }
            AppAction.ToggleReference -> if (s.caseReferenceAvailable) mutate { it.copy(showReference = !it.showReference) }
            AppAction.OpenSkillPicker -> mutate { it.copy(showSkillPicker = !it.showSkillPicker) }
            is AppAction.StartChain -> startChain(action.seedIndex ?: s.seedIndex)
            is AppAction.SelectChainSeed -> startChain(action.index)
            AppAction.StartSchedule -> startSchedule()
            AppAction.StartDaily -> startDaily()
            AppAction.ContinueDaily -> continueDaily()
            is AppAction.ChooseSkill -> chooseSkill(action)
            is AppAction.SetAnswerMode -> mutate { it.copy(answerMode = action.mode) }
            is AppAction.SetStyle -> mutate { it.copy(styleId = action.styleId) }
            AppAction.ContinueIntroduction -> {
                mutate { it.copy(introPending = false) }
                s.exercise?.let(::addFocus)
            }
            is AppAction.EditAnswer -> mutate { it.copy(draft = action.text) }
            is AppAction.Reveal -> reveal(action.exerciseId)
            is AppAction.Rate -> rate(action)
            is AppAction.Tile -> tile(action)
        }
    }

    private fun accepts(action: AppAction, s: AppUiState): Boolean = when (action) {
        is AppAction.StartChain, is AppAction.SelectChainSeed,
        AppAction.StartSchedule, AppAction.StartDaily, is AppAction.ChooseSkill -> s.loadStatus == LoadStatus.Ready
        AppAction.ContinueDaily -> s.loadStatus == LoadStatus.Ready && s.mode == TrainingMode.Daily && s.phase == CardPhase.DailyComplete
        AppAction.RequestReset -> s.loadStatus == LoadStatus.Ready && s.pendingEffects.none { it is UiEffect.ConfirmReset }
        is AppAction.SetAnswerMode, is AppAction.EditAnswer -> s.phase == CardPhase.Question
        AppAction.ContinueIntroduction -> s.phase == CardPhase.Question && s.introPending
        is AppAction.Reveal -> s.loadStatus == LoadStatus.Ready && s.phase == CardPhase.Question &&
            !s.introPending && s.exercise?.id == action.exerciseId &&
            (s.answerMode != AnswerMode.TileBuilder || s.tileBuilder?.let { b ->
                b.exercise.segments.filterIsInstance<TileSegment.Choice>().all { c -> b.placed.any { it.position == c.position } }
            } == true)
        is AppAction.Rate -> s.loadStatus == LoadStatus.Ready && s.phase == CardPhase.Revealed &&
            s.exercise?.id == action.exerciseId && document != null
        is AppAction.Tile -> s.loadStatus == LoadStatus.Ready && s.exercise?.id == action.exerciseId && when (val a = action.action) {
            is TileAction.Place, is TileAction.Remove -> s.phase == CardPhase.Question && s.tileBuilder != null
            TileAction.Check -> s.phase == CardPhase.Question && s.tileBuilder?.let { b ->
                b.exercise.segments.filterIsInstance<TileSegment.Choice>().all { c -> b.placed.any { it.position == c.position } }
            } == true
            is TileAction.TapError -> s.phase == CardPhase.Revealed && s.tileBuilder?.result?.errors?.any { it.position == a.position } == true
        }
        else -> true
    }

    private fun install(source: ProgressDocument, captured: TimeCapture) {
        val completed = ProgressCodec.completeKnownSkills(source, knownSkillIds, captured.at, scheduler)
        val complete = ProgressCodec.normalizeDay(completed, captured.localDay)
        document = complete
        recoveryRaw = null
        mutableState.value = project(state.value.copy(
            loadStatus = LoadStatus.Ready, error = null,
            introPending = needsIntroduction(state.value.exercise, complete),
            // SB-8: the first exercise's tileBuilder was derived before any document existed
            // (the `mutableState` property initializer runs with `document == null`); recompute
            // now that the real document — and its scaffolding rung — is known.
            tileBuilder = deriveTileBuilder(state.value.exercise, complete),
        ), complete, captured)
        if (complete != source) queueSave(complete)
    }

    private fun recovery(raw: String, reason: String) {
        recoveryRaw = raw
        mutableState.value = state.value.copy(loadStatus = LoadStatus.RecoveryRequired, error = reason)
    }

    private fun unavailable(error: Throwable) {
        mutableState.value = state.value.copy(loadStatus = LoadStatus.Unavailable, error = error.message ?: "Хранилище недоступно")
    }

    private fun requestMigration() {
        if (state.value.loadStatus !in setOf(LoadStatus.MigrationAvailable, LoadStatus.RecoveryRequired)) return
        mutableState.value = state.value.copy(loadStatus = LoadStatus.Loading, error = null)
        scope.launch {
            val captured = time.capture()
            val result = try {
                repository.migrateLegacy(captured.at, captured.localDay, knownSkillIds)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                MigrationResult.Unavailable(error)
            }
            if (closed) return@launch
            when (result) {
                is MigrationResult.Migrated -> install(result.document, captured)
                is MigrationResult.AlreadyMigrated -> install(result.document, captured)
                MigrationResult.Missing -> recovery(recoveryRaw.orEmpty(), "Прежний прогресс исчез до переноса")
                is MigrationResult.RecoveryRequired -> recovery(result.raw, result.reason)
                is MigrationResult.Unavailable -> recovery(recoveryRaw.orEmpty(), result.cause.message ?: "Не удалось перенести прогресс")
            }
        }
    }

    private fun needsIntroduction(exercise: Exercise?, source: ProgressDocument?): Boolean =
        exercise != null && source?.progress?.stats?.get(exercise.primarySkill)?.reviews == 0

    private fun startChain(index: Int) {
        if (index !in sentenceSeeds.indices) {
            mutate { it.copy(error = "Неизвестный набор слов") }
            return
        }
        val chain = exerciseEngine.generateChain(sentenceSeeds[index]).toList()
        mutableState.value = state.value.copy(
            tab = AppTab.Training, mode = TrainingMode.Chain, seedIndex = index,
            chain = chain, chainIndex = 0, focusedSkillId = null, showSkillPicker = false,
            exercise = chain.first(), phase = CardPhase.Question, draft = "", frozenAnswer = null,
            evaluation = null, tileBuilder = deriveTileBuilder(chain.first()),
            introPending = needsIntroduction(chain.first(), document), error = null, daily = null,
        )
        addFocus(chain.first())
        refreshTime()
    }

    private fun startSchedule() {
        val captured = time.capture()
        val progress = document?.progress ?: return
        val exercise = skillQueue.next(progress.cards, scheduler, captured.at)?.let { exerciseEngine.generateForSkill(it.skillId) }
        mutableState.value = project(state.value.copy(
            tab = AppTab.Training, mode = TrainingMode.Schedule, focusedSkillId = null,
            showSkillPicker = false, exercise = exercise,
            phase = if (exercise == null) CardPhase.NoDue else CardPhase.Question,
            draft = "", frozenAnswer = null, evaluation = null, tileBuilder = deriveTileBuilder(exercise),
            introPending = needsIntroduction(exercise, document), error = null, daily = null,
        ), document ?: return, captured)
        if (exercise != null) addFocus(exercise)
    }

    /** M3 blueprint: picks the first [DailySessionPlanner] step for a fresh session and generates
     *  its exercise exactly like [startSchedule] does for its own card — same null-handling 1:1
     *  (no due/eligible step at all → [CardPhase.NoDue], the fail-soft "nothing to do" case). */
    private fun startDaily() {
        val captured = time.capture()
        val progress = document?.progress ?: return
        val step = DailySessionPlanner.nextStep(progress, scheduler, skillQueue, skills, captured.at, completed = emptyList())
        val exercise = step?.let { exerciseEngine.generateForSkill(it.skillId) }
        val tileBuilder = deriveTileBuilder(exercise)
        mutableState.value = project(state.value.copy(
            tab = AppTab.Training, mode = TrainingMode.Daily, focusedSkillId = null,
            showSkillPicker = false, exercise = exercise,
            phase = if (exercise == null) CardPhase.NoDue else CardPhase.Question,
            draft = "", frozenAnswer = null, evaluation = null, tileBuilder = tileBuilder,
            answerMode = dailyAnswerMode(step, tileBuilder, state.value.answerMode),
            introPending = needsIntroduction(exercise, document), error = null,
            daily = DailySessionState(currentStep = step),
        ), document ?: return, captured)
        if (exercise != null) addFocus(exercise)
    }

    /** M3 blueprint: only reachable from [CardPhase.DailyComplete] ([accepts] guards it) — the
     *  learner's voluntary "ещё одно задание"; marks [DailySessionState.extended] so a later
     *  [advanceDaily] never re-triggers [CardPhase.DailyComplete] a second time for this session. */
    private fun continueDaily() {
        val current = state.value
        val daily = current.daily ?: return
        val captured = time.capture()
        val progress = document?.progress ?: return
        // Deviates from the blueprint's literal "from daily.history.map { it.kind }": once
        // CardPhase.DailyComplete is reached, every DailyMix cap is necessarily already exhausted
        // (planned == sum of caps, each kind bounded by its own cap — reaching the total forces
        // every kind to its cap exactly), so replaying the full history here would make
        // ContinueDaily always return null. A fresh per-kind budget for the bonus round keeps the
        // same Review > New > Practice priority without that dead end (AI/decisions.md).
        val step = DailySessionPlanner.nextStep(progress, scheduler, skillQueue, skills, captured.at, completed = emptyList())
        val exercise = step?.let { exerciseEngine.generateForSkill(it.skillId) }
        val tileBuilder = deriveTileBuilder(exercise)
        mutableState.value = project(current.copy(
            exercise = exercise, phase = if (exercise == null) CardPhase.NoDue else CardPhase.Question,
            draft = "", frozenAnswer = null, evaluation = null, tileBuilder = tileBuilder,
            answerMode = dailyAnswerMode(step, tileBuilder, current.answerMode),
            introPending = needsIntroduction(exercise, document),
            daily = daily.copy(extended = true, currentStep = step),
        ), document ?: return, captured)
        if (exercise != null) addFocus(exercise)
    }

    /** M3 blueprint: forces [AnswerMode.TileBuilder] for the one Practice step when the chosen
     *  skill's exercise actually supports tiles; every other step keeps the learner's own
     *  [fallback] untouched — a narrow, documented scaffolding exception, not a precedent for
     *  overriding user choice elsewhere. */
    private fun dailyAnswerMode(step: DailyStep?, tileBuilder: TileBuilderState?, fallback: AnswerMode): AnswerMode =
        if (step?.kind == DailyStepKind.Practice && tileBuilder != null) AnswerMode.TileBuilder else fallback

    /** M3 blueprint: called from [rate]'s own `when (current.mode)` branch, after
     *  [ReviewReducer.recordReview] already ran. Appends this step's [classifyDailyOutcome] to
     *  [DailySessionState.history]; once the planned volume is reached (and this session was never
     *  extended), returns `null` with [DailySessionState.currentStep] cleared so [rate] can route
     *  to [CardPhase.DailyComplete] — otherwise asks the planner for the next step. */
    private fun advanceDaily(current: AppUiState, rating: Rating, newDocument: ProgressDocument, at: kotlin.time.Instant): Pair<Exercise?, DailySessionState> {
        val daily = current.daily ?: return null to DailySessionState()
        val step = daily.currentStep
        val history = step?.let { daily.history + DailyStepOutcome(it.kind, it.skillId, classifyDailyOutcome(current, rating)) } ?: daily.history
        if (history.size >= daily.planned && !daily.extended) {
            return null to daily.copy(history = history, currentStep = null)
        }
        // Same fresh-budget reasoning as continueDaily's own deviation, for a session already past
        // its planned volume (extended == true): the full history would already have every cap
        // saturated, so a bonus round keeps flowing only with a fresh per-kind budget each step.
        val completedForPlanner = if (daily.extended) emptyList() else history.map { it.kind }
        val nextStep = DailySessionPlanner.nextStep(newDocument.progress, scheduler, skillQueue, skills, at, completed = completedForPlanner)
        val nextExercise = nextStep?.let { exerciseEngine.generateForSkill(it.skillId) }
        return nextExercise to daily.copy(history = history, currentStep = nextStep)
    }

    private fun chooseSkill(action: AppAction.ChooseSkill) {
        if (action.skillId == "chain") {
            val index = sentenceSeeds.indexOfFirst { it.nounId == action.preferredSeed?.nounId }.coerceAtLeast(0)
            startChain(index)
            return
        }
        if (action.skillId !in knownSkillIds) {
            mutate { it.copy(error = "Неизвестный навык: ${action.skillId}") }
            return
        }
        if (action.preferredSeed != null &&
            (nouns.none { it.id == action.preferredSeed.nounId } ||
                adjectives.none { it.id == action.preferredSeed.adjectiveId })
        ) {
            mutate { it.copy(error = "Неизвестные слова для навыка") }
            return
        }
        val exercise = exerciseEngine.generateForSkill(action.skillId, action.preferredSeed)
        mutableState.value = state.value.copy(
            tab = AppTab.Training, mode = TrainingMode.Focused, focusedSkillId = action.skillId,
            showSkillPicker = false, exercise = exercise, phase = CardPhase.Question,
            draft = "", frozenAnswer = null, evaluation = null, tileBuilder = deriveTileBuilder(exercise),
            introPending = needsIntroduction(exercise, document), error = null, daily = null,
        )
        addFocus(exercise)
        refreshTime()
    }

    private fun reveal(exerciseId: String) {
        val current = state.value
        val exercise = current.exercise ?: return
        val evaluation = if (current.answerMode == AnswerMode.Typed) evaluate(current.draft, exercise) else null
        val tileResult = current.tileBuilder?.takeIf { current.answerMode == AnswerMode.TileBuilder }
            ?.let { evaluateTileAnswer(it.exercise, it.placed) }
        mutate {
            it.copy(
                phase = CardPhase.Revealed, frozenAnswer = current.draft, evaluation = evaluation,
                tileBuilder = current.tileBuilder?.copy(result = tileResult),
            )
        }
    }

    /** SB-6 blueprint §1.2: dispatched by [TileAction.Place]/[Remove]/[TapError] directly, and by
     *  [TileAction.Check] which delegates to [reveal] — one `CardPhase` state machine for every
     *  [AnswerMode] (blueprint §1.3): no duplicated freeze/evaluate logic. */
    private fun tile(action: AppAction.Tile) {
        val current = state.value
        val builder = current.tileBuilder ?: return
        when (val a = action.action) {
            is TileAction.Place -> mutate {
                it.copy(tileBuilder = builder.copy(placed = builder.placed.filterNot { p -> p.position == a.position } + TileSelection(a.position, a.form)))
            }
            is TileAction.Remove -> mutate {
                it.copy(tileBuilder = builder.copy(placed = builder.placed.filterNot { p -> p.position == a.position }))
            }
            TileAction.Check -> reveal(action.exerciseId)
            is TileAction.TapError -> mutate {
                it.copy(tileBuilder = builder.copy(errorExplanation = errorExplanationFor(current, a.position)))
            }
        }
    }

    /** SB-7: the specific per-tile explanation for the tapped error, via
     *  [StyleComposer.blocksForError]. `state.tileBuilder?.result?.errors` is already guaranteed
     *  non-null-for-this-position by the [accepts] guard for [TileAction.TapError]; the
     *  `firstOrNull` here is a defensive fallback, not the primary guard. */
    private fun errorExplanationFor(state: AppUiState, position: Int): List<Block> {
        val exercise = state.exercise ?: return emptyList()
        val error = state.tileBuilder?.result?.errors?.firstOrNull { it.position == position } ?: return emptyList()
        val skill = skillById(exercise.primarySkill)
        val focus = presentationBySkillId(exercise.primarySkill)
        val content = styleContentBySkillId(exercise.primarySkill)
        return StyleComposer.blocksForError(exercise, skill, focus, content, error)
    }

    private fun rate(action: AppAction.Rate) {
        val current = state.value
        val exercise = current.exercise ?: return
        val before = document ?: return
        val captured = time.capture()
        val correctness = when (current.answerMode) {
            AnswerMode.Typed -> current.evaluation?.correct
            AnswerMode.TileBuilder -> current.tileBuilder?.result?.correct
            AnswerMode.Oral -> null
        }
        val tileCorrect = correctness.takeIf { current.answerMode == AnswerMode.TileBuilder }
        val result = reviewReducer.recordReview(
            before, exercise.primarySkill, action.rating, correctness,
            captured.at, captured.localDay, tileCorrect,
        )
        if (result !is ReviewResult.Reviewed) {
            mutate { it.copy(error = "Неизвестный навык: ${exercise.primarySkill}") }
            return
        }
        document = result.document
        // Replace the card synchronously. The old ID can never be rated twice, even while saving.
        var nextDaily: DailySessionState? = null
        val nextExercise = when (current.mode) {
            TrainingMode.Chain -> current.chain.getOrNull(current.chainIndex + 1)
            TrainingMode.Focused -> exerciseEngine.generateForSkill(current.focusedSkillId ?: exercise.primarySkill)
            TrainingMode.Schedule ->
                skillQueue.next(result.document.progress.cards, scheduler, captured.at)?.let { exerciseEngine.generateForSkill(it.skillId) }
            TrainingMode.Daily -> {
                val (ex, daily) = advanceDaily(current, action.rating, result.document, captured.at)
                nextDaily = daily
                ex
            }
        }
        val phase = when {
            current.mode == TrainingMode.Chain && nextExercise == null -> CardPhase.ChainComplete
            current.mode == TrainingMode.Schedule && nextExercise == null -> CardPhase.NoDue
            current.mode == TrainingMode.Daily && nextExercise == null && nextDaily?.currentStep == null &&
                (nextDaily?.history?.size ?: 0) >= (nextDaily?.planned ?: 0) && nextDaily?.extended == false -> CardPhase.DailyComplete
            current.mode == TrainingMode.Daily && nextExercise == null -> CardPhase.NoDue
            else -> CardPhase.Question
        }
        val tileBuilder = deriveTileBuilder(nextExercise)
        val updated = current.copy(
            chainIndex = if (current.mode == TrainingMode.Chain && nextExercise != null) current.chainIndex + 1 else current.chainIndex,
            exercise = nextExercise, phase = phase, draft = "", frozenAnswer = null, evaluation = null,
            tileBuilder = tileBuilder,
            answerMode = if (current.mode == TrainingMode.Daily) dailyAnswerMode(nextDaily?.currentStep, tileBuilder, current.answerMode) else current.answerMode,
            introPending = needsIntroduction(nextExercise, result.document),
            daily = if (current.mode == TrainingMode.Daily) nextDaily else current.daily,
        )
        mutableState.value = project(updated, result.document, captured)
        queueSave(result.document)
        if (nextExercise != null) addFocus(nextExercise)
    }

    private fun refreshTime() {
        val captured = time.capture()
        val current = state.value
        val doc = document ?: return
        val projected = project(current, doc, captured)
        if (current.mode == TrainingMode.Schedule && current.phase == CardPhase.NoDue && projected.dueCount > 0) {
            val exercise = skillQueue.next(doc.progress.cards, scheduler, captured.at)?.let { exerciseEngine.generateForSkill(it.skillId) }
            mutableState.value = project(projected.copy(
                exercise = exercise, phase = CardPhase.Question, tileBuilder = deriveTileBuilder(exercise),
                introPending = needsIntroduction(exercise, doc),
            ), doc, captured)
            if (exercise != null) addFocus(exercise)
        } else mutableState.value = projected
    }

    private fun project(base: AppUiState, doc: ProgressDocument, captured: TimeCapture): AppUiState {
        val cards = doc.progress.cards
        val due = skillQueue.due(cards, scheduler, captured.at).size
        val next = skillQueue.nextDueAt(cards, captured.at)
        val interval = base.exercise?.let { exercise ->
            cards.firstOrNull { it.skillId == exercise.primarySkill }?.let { scheduler.preview(it, captured.at) }
        }
        return base.copy(
            progress = doc.progress.copy(cards = cards.toList(), stats = doc.progress.stats.toMap()),
            dueCount = due,
            todayCount = if (doc.progress.lastDay == captured.localDay) doc.progress.reviewsToday else 0,
            nextDue = next, intervals = interval, now = captured.at, localDay = captured.localDay,
        )
    }

    private fun queueSave(snapshot: ProgressDocument) {
        val revision = state.value.revision + 1
        mutate { it.copy(revision = revision) }
        writes.trySend(Write.Save(revision, snapshot))
    }

    private fun resetDecision(action: AppAction.ResetDecision) {
        val effect = state.value.pendingEffects.firstOrNull { it.id == action.effectId } as? UiEffect.ConfirmReset ?: return
        removeEffect(effect.id)
        if (!action.confirmed) return
        if (state.value.loadStatus != LoadStatus.Ready) return
        val captured = time.capture()
        val stamp = document?.source?.get("__kmpMigration")
        val fresh = ProgressCodec.fresh(knownSkillIds, captured.at, captured.localDay, scheduler).let {
            if (stamp == null) it else it.copy(source = JsonObject(mapOf("__kmpMigration" to stamp)))
        }
        document = fresh
        recoveryRaw = null
        val chain = exerciseEngine.generateChain().toList()
        val next = state.value.copy(
            loadStatus = LoadStatus.Ready, tab = AppTab.Training, mode = TrainingMode.Chain,
            seedIndex = 0, chain = chain, chainIndex = 0, focusedSkillId = null,
            exercise = chain.first(), phase = CardPhase.Question, draft = "", frozenAnswer = null,
            evaluation = null, tileBuilder = deriveTileBuilder(chain.first()),
            introPending = needsIntroduction(chain.first(), fresh),
            showSkillPicker = false, error = null, daily = null,
        )
        mutableState.value = project(next, fresh, captured)
        val revision = state.value.revision + 1
        mutate { it.copy(revision = revision) }
        writes.trySend(Write.Reset(revision, captured.at, captured.localDay))
        addFocus(chain.first())
    }

    private fun export() {
        // NJ03 C6: export as POLSKI-TEXT; DownloadJson retained for recovery path only.
        val text = document?.let { ProgressCodec.encodeText(it).decodeToString() }
            ?: recoveryRaw ?: return
        val isText = text.startsWith("POLSKI-TEXT")
        if (isText) addEffect { UiEffect.DownloadText(it, "polski-srs-progress.txt", text) }
        else addEffect { UiEffect.DownloadJson(it, "polski-srs-progress.json", text) }
    }

    private fun acknowledge(action: AppAction.EffectAcknowledged) {
        val effect = state.value.pendingEffects.firstOrNull { it.id == action.effectId } ?: return
        if (effect is UiEffect.ConfirmReset) return
        if (action.outcome is EffectOutcome.Failed) {
            mutate { it.copy(error = action.outcome.reason) }
        }
        removeEffect(effect.id)
    }

    private fun addFocus(exercise: Exercise) {
        if (!state.value.introPending) addEffect { UiEffect.FocusReveal(it, exercise.id) }
    }

    private fun addEffect(factory: (Long) -> UiEffect) {
        val id = ++nextEffectId
        mutate { it.copy(pendingEffects = it.pendingEffects + factory(id)) }
    }

    private fun removeEffect(id: Long) = mutate { it.copy(pendingEffects = it.pendingEffects.filterNot { effect -> effect.id == id }) }

    private fun completeWrite(revision: Long, result: SaveResult) {
        when (result) {
            SaveResult.Saved -> mutate {
                it.copy(
                    savedRevision = maxOf(it.savedRevision, revision),
                    error = if (revision == it.revision && it.error?.contains("Экспортируй JSON") == true) null else it.error,
                )
            }
            is SaveResult.WriteFailed -> mutate {
                it.copy(error = "Не удалось сохранить прогресс. Экспортируй JSON перед закрытием.")
            }
        }
    }

    private inline fun mutate(change: (AppUiState) -> AppUiState) {
        mutableState.value = change(mutableState.value)
    }

    private sealed interface Write {
        val revision: Long
        data class Save(override val revision: Long, val document: ProgressDocument) : Write
        data class Reset(override val revision: Long, val at: kotlin.time.Instant, val localDay: String) : Write
    }
}
