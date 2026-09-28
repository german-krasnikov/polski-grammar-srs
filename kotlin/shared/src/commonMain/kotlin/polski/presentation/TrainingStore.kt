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
import polski.data.skills
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
import polski.srs.Scheduler
import polski.training.DueSkillCard
import polski.training.PlExerciseEngine
import polski.training.evaluate
import polski.training.nextSkillId
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
) {
    private val scope = CoroutineScope(ownerScope.coroutineContext + SupervisorJob(ownerScope.coroutineContext[Job]))
    private val writes = Channel<Write>(Channel.UNLIMITED)
    private val knownSkillIds = skills.map { it.id }
    private val knownSkillIdSet = knownSkillIds.toSet()
    private val reviewReducer = ReviewReducer(scheduler, knownSkillIdSet)
    /** UniversalCorePlan.md §6: the active pack's skills only — see [SkillQueue]. */
    private val skillQueue = SkillQueue { it in knownSkillIdSet }
    private var document: ProgressDocument? = null
    private var recoveryRaw: String? = null
    private var nextEffectId = 0L
    private var started = false
    private var closed = false

    private val initialChain = exerciseEngine.generateChain()
    private val mutableState = MutableStateFlow(
        AppUiState(chain = initialChain.toList(), exercise = initialChain.first(), styleId = initialStyleId, answerMode = initialAnswerMode),
    )
    val state: StateFlow<AppUiState> = mutableState

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
        when (action) {
            is AppAction.EffectAcknowledged -> acknowledge(action)
            is AppAction.ResetDecision -> resetDecision(action)
            AppAction.RequestExport -> export()
            AppAction.RequestMigration -> requestMigration()
            AppAction.RequestReset -> if (state.value.loadStatus == LoadStatus.Ready &&
                state.value.pendingEffects.none { it is UiEffect.ConfirmReset }
            ) {
                addEffect { UiEffect.ConfirmReset(it, "Сбросить весь прогресс?") }
            }
            AppAction.RefreshTime -> refreshTime()
            is AppAction.SelectTab -> {
                val old = state.value
                mutableState.value = old.copy(
                    tab = action.tab,
                    matrixSelection = if (action.tab == AppTab.Matrix && old.tab != AppTab.Matrix) MatrixSelection() else old.matrixSelection,
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
            AppAction.ToggleReference -> mutate { it.copy(showReference = !it.showReference) }
            AppAction.OpenSkillPicker -> mutate { it.copy(showSkillPicker = !it.showSkillPicker) }
            is AppAction.StartChain -> ifReady { startChain(action.seedIndex ?: state.value.seedIndex) }
            is AppAction.SelectChainSeed -> ifReady { startChain(action.index) }
            AppAction.StartSchedule -> ifReady { startSchedule() }
            is AppAction.ChooseSkill -> ifReady { chooseSkill(action) }
            is AppAction.SetAnswerMode -> if (state.value.phase == CardPhase.Question) mutate { it.copy(answerMode = action.mode) }
            is AppAction.SetStyle -> mutate { it.copy(styleId = action.styleId) }
            AppAction.ContinueIntroduction -> if (state.value.phase == CardPhase.Question && state.value.introPending) {
                mutate { it.copy(introPending = false) }
                state.value.exercise?.let(::addFocus)
            }
            is AppAction.EditAnswer -> if (state.value.phase == CardPhase.Question) mutate { it.copy(draft = action.text) }
            is AppAction.Reveal -> reveal(action.exerciseId)
            is AppAction.Rate -> rate(action)
        }
    }

    private fun install(source: ProgressDocument, captured: TimeCapture) {
        val completed = ProgressCodec.completeKnownSkills(source, knownSkillIds, captured.at, scheduler)
        val complete = ProgressCodec.normalizeDay(completed, captured.localDay)
        document = complete
        recoveryRaw = null
        mutableState.value = project(state.value.copy(
            loadStatus = LoadStatus.Ready, error = null,
            introPending = needsIntroduction(state.value.exercise, complete),
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

    private fun ifReady(block: () -> Unit) {
        if (state.value.loadStatus == LoadStatus.Ready) block()
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
            evaluation = null, introPending = needsIntroduction(chain.first(), document), error = null,
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
            draft = "", frozenAnswer = null, evaluation = null,
            introPending = needsIntroduction(exercise, document), error = null,
        ), document ?: return, captured)
        if (exercise != null) addFocus(exercise)
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
            draft = "", frozenAnswer = null, evaluation = null,
            introPending = needsIntroduction(exercise, document), error = null,
        )
        addFocus(exercise)
        refreshTime()
    }

    private fun reveal(exerciseId: String) {
        val current = state.value
        val exercise = current.exercise ?: return
        if (current.loadStatus != LoadStatus.Ready || current.phase != CardPhase.Question ||
            current.introPending || exercise.id != exerciseId) return
        val evaluation = if (current.answerMode == AnswerMode.Typed) evaluate(current.draft, exercise) else null
        mutate { it.copy(phase = CardPhase.Revealed, frozenAnswer = current.draft, evaluation = evaluation) }
    }

    private fun rate(action: AppAction.Rate) {
        val current = state.value
        val exercise = current.exercise ?: return
        val before = document ?: return
        if (current.loadStatus != LoadStatus.Ready || current.phase != CardPhase.Revealed || exercise.id != action.exerciseId) return
        val captured = time.capture()
        val result = reviewReducer.recordReview(
            before, exercise.primarySkill, action.rating,
            if (current.answerMode == AnswerMode.Typed) current.evaluation?.correct else null,
            captured.at, captured.localDay,
        )
        if (result !is ReviewResult.Reviewed) {
            mutate { it.copy(error = "Неизвестный навык: ${exercise.primarySkill}") }
            return
        }
        document = result.document
        // Replace the card synchronously. The old ID can never be rated twice, even while saving.
        val nextExercise = when (current.mode) {
            TrainingMode.Chain -> current.chain.getOrNull(current.chainIndex + 1)
            TrainingMode.Focused -> exerciseEngine.generateForSkill(current.focusedSkillId ?: exercise.primarySkill)
            TrainingMode.Schedule -> {
                val due = skillQueue.due(result.document.progress.cards, scheduler, captured.at)
                val nextId = due.takeIf { it.isNotEmpty() }?.let { cards ->
                    nextSkillId(cards.map { DueSkillCard(it.skillId, it.card.due.toEpochMilliseconds()) })
                }
                nextId?.let(exerciseEngine::generateForSkill)
            }
        }
        val phase = when {
            current.mode == TrainingMode.Chain && nextExercise == null -> CardPhase.ChainComplete
            current.mode == TrainingMode.Schedule && nextExercise == null -> CardPhase.NoDue
            else -> CardPhase.Question
        }
        val updated = current.copy(
            chainIndex = if (current.mode == TrainingMode.Chain && nextExercise != null) current.chainIndex + 1 else current.chainIndex,
            exercise = nextExercise, phase = phase, draft = "", frozenAnswer = null, evaluation = null,
            introPending = needsIntroduction(nextExercise, result.document),
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
                exercise = exercise, phase = CardPhase.Question,
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
            evaluation = null, introPending = needsIntroduction(chain.first(), fresh),
            showSkillPicker = false, error = null,
        )
        mutableState.value = project(next, fresh, captured)
        val revision = state.value.revision + 1
        mutate { it.copy(revision = revision) }
        writes.trySend(Write.Reset(revision, captured.at, captured.localDay))
        addFocus(chain.first())
    }

    private fun export() {
        val json = document?.let(ProgressCodec::encodeLegacyV1) ?: recoveryRaw ?: return
        addEffect { UiEffect.DownloadJson(it, "polski-srs-progress.json", json) }
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
