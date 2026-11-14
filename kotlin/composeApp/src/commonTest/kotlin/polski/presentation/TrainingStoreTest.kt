package polski.presentation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.TestScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import polski.data.skills
import polski.progress.LoadResult
import polski.progress.MigrationResult
import polski.progress.ProgressCodec
import polski.progress.ProgressDocument
import polski.progress.ProgressRepository
import polski.progress.SaveResult
import polski.srs.Rating
import polski.srs.SchedulePreview
import polski.srs.Scheduler
import polski.srs.SrsCard
import polski.srs.StoredCard
import polski.training.PlExerciseEngine
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.core.model.ScaffoldingLevel
import polski.core.model.TileSegment
import polski.training.sentenceSeeds

@OptIn(ExperimentalCoroutinesApi::class)
class TrainingStoreTest {
    private val at = Instant.parse("2026-09-23T12:00:00Z")
    // The real FSRS parity suite lives in shared; transition tests control due times directly.
    private val scheduler: Scheduler = FastScheduler()

    @Test
    fun tabNavigationResetsMatrixSelectionOnlyOnEntry() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        assertEquals(AppTab.Training, store.state.value.tab)

        for (tab in listOf(AppTab.Vocabulary, AppTab.Progress, AppTab.Training, AppTab.Matrix)) {
            store.dispatch(AppAction.SelectTab(tab))
            assertEquals(tab, store.state.value.tab)
        }
        val selected = MatrixSelection(section = MatrixSection.Cases)
        store.dispatch(AppAction.SetMatrixSelection(selected))
        assertEquals(selected, store.state.value.matrixSelection)
        store.dispatch(AppAction.SelectTab(AppTab.Matrix))
        assertEquals(selected, store.state.value.matrixSelection)
        store.dispatch(AppAction.SelectTab(AppTab.Vocabulary))
        store.dispatch(AppAction.SelectTab(AppTab.Matrix))
        assertEquals(MatrixSelection(), store.state.value.matrixSelection)
        store.close()
    }

    @Test
    fun introductionAndMethodSwitchPreserveDraftPhaseAndReviewCount() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        val id = assertNotNull(store.state.value.exerciseId)
        // M7: case.acc.f (the first chain skill) is a prediction pilot — dismiss it first so the
        // rest of this test exercises introPending exactly as before M7 existed.
        if (store.state.value.predictionPending) store.dispatch(AppAction.ContinuePrediction)
        assertTrue(store.state.value.introPending)
        assertEquals(0, store.state.value.progress?.totalReviews)
        store.dispatch(AppAction.Reveal(id))
        assertEquals(CardPhase.Question, store.state.value.phase)
        assertEquals(null, store.state.value.evaluation)
        assertEquals(0, store.state.value.progress?.totalReviews)
        store.dispatch(AppAction.SetStyle(StyleId.SituationFirst))
        assertTrue(store.state.value.introPending)
        assertEquals(id, store.state.value.exerciseId)
        store.dispatch(AppAction.ContinueIntroduction)
        assertFalse(store.state.value.introPending)
        store.dispatch(AppAction.SetAnswerMode(AnswerMode.Typed))
        store.dispatch(AppAction.EditAnswer("Moja próba"))
        store.dispatch(AppAction.SetStyle(StyleId.RuleFirst))
        assertEquals("Moja próba", store.state.value.draft)
        assertEquals(CardPhase.Question, store.state.value.phase)
        store.dispatch(AppAction.Reveal(id))
        store.dispatch(AppAction.SetStyle(StyleId.SituationFirst))
        assertEquals("Moja próba", store.state.value.frozenAnswer)
        assertEquals(CardPhase.Revealed, store.state.value.phase)
        assertEquals(0, store.state.value.progress?.totalReviews)
        store.dispatch(AppAction.Rate(id, Rating.Good))
        assertEquals(1, store.state.value.progress?.totalReviews)
        assertTrue(store.state.value.introPending)
        store.close()
    }

    @Test
    fun nextFirstEncounterQueuesFocusOnlyAfterIntroductionContinues() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        val firstId = assertNotNull(store.state.value.exerciseId)
        if (store.state.value.predictionPending) store.dispatch(AppAction.ContinuePrediction)
        store.dispatch(AppAction.ContinueIntroduction)
        val firstFocus = store.state.value.pendingEffects.filterIsInstance<UiEffect.FocusReveal>().single()
        assertEquals(firstId, firstFocus.exerciseId)
        store.dispatch(AppAction.EffectAcknowledged(firstFocus.id, EffectOutcome.Completed))
        store.dispatch(AppAction.Reveal(firstId))
        store.dispatch(AppAction.Rate(firstId, Rating.Good))

        val nextId = assertNotNull(store.state.value.exerciseId)
        assertTrue(store.state.value.introPending)
        assertTrue(store.state.value.pendingEffects.none { it is UiEffect.FocusReveal })
        store.dispatch(AppAction.RefreshTime)
        assertTrue(store.state.value.pendingEffects.none { it is UiEffect.FocusReveal })
        store.dispatch(AppAction.ContinueIntroduction)
        assertFalse(store.state.value.introPending)
        val nextFocus = store.state.value.pendingEffects.filterIsInstance<UiEffect.FocusReveal>().single()
        assertEquals(nextId, nextFocus.exerciseId)
        store.dispatch(AppAction.ContinueIntroduction)
        assertEquals(listOf(nextFocus), store.state.value.pendingEffects.filterIsInstance<UiEffect.FocusReveal>())
        store.close()
    }

    private fun revealAfterIntroduction(store: TrainingStore, exerciseId: String) {
        // M7: a pilot skill's prediction step always precedes its introduction — dismiss it first.
        if (store.state.value.predictionPending) store.dispatch(AppAction.ContinuePrediction)
        if (store.state.value.introPending) store.dispatch(AppAction.ContinueIntroduction)
        store.dispatch(AppAction.Reveal(exerciseId))
    }

    @Test fun seedsZeroToTwoLinkAndAdvance() = runTest { checkSeeds(0..2) }
    @Test fun seedsThreeToFiveLinkAndAdvance() = runTest { checkSeeds(3..5) }
    @Test fun seedsSixToEightLinkAndAdvance() = runTest { checkSeeds(6..8) }
    @Test fun seedsNineToElevenLinkAndAdvance() = runTest { checkSeeds(9..11) }

    private suspend fun TestScope.checkSeeds(indices: IntRange) {
        for (seed in indices) {
            val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
            val store = newStore(repo, backgroundScope)
            store.start()
            store.dispatch(AppAction.StartChain(seed))
            val chain = store.state.value.chain
            assertEquals(5, chain.size)
            for (index in 1..4) assertEquals(chain[index - 1].expected, chain[index].source)
            for (index in 0..4) {
                val id = assertNotNull(store.state.value.exerciseId)
                revealAfterIntroduction(store, id)
                store.dispatch(AppAction.Rate(id, Rating.Good))
                assertEquals(index + 1, store.state.value.progress?.totalReviews)
                assertEquals(if (index == 4) CardPhase.ChainComplete else CardPhase.Question, store.state.value.phase)
            }
            store.dispatch(AppAction.StartChain((seed + 1) % sentenceSeeds.size))
            assertEquals((seed + 1) % sentenceSeeds.size, store.state.value.seedIndex)
            store.close()
        }
    }

    @Test
    fun fiveRatingsCompleteChainExactlyOnce() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        for (index in 0..4) {
            val id = assertNotNull(store.state.value.exerciseId)
            revealAfterIntroduction(store, id)
            store.dispatch(AppAction.Rate(id, Rating.Good))
            store.dispatch(AppAction.Rate(id, Rating.Good))
            assertEquals(index + 1, store.state.value.progress?.totalReviews)
        }
        assertEquals(CardPhase.ChainComplete, store.state.value.phase)
        store.close()
    }

    @Test
    fun revealFreezesTypedAnswerAndNavigationPreservesCard() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        val id = assertNotNull(store.state.value.exerciseId)
        val expected = assertNotNull(store.state.value.exercise).expected
        store.dispatch(AppAction.SetAnswerMode(AnswerMode.Typed))
        store.dispatch(AppAction.EditAnswer(expected))
        revealAfterIntroduction(store, id)
        store.dispatch(AppAction.EditAnswer("wrong"))
        store.dispatch(AppAction.SelectTab(AppTab.Matrix))
        store.dispatch(AppAction.ToggleReference)
        store.dispatch(AppAction.SelectTab(AppTab.Training))
        assertEquals(id, store.state.value.exerciseId)
        assertEquals(expected, store.state.value.frozenAnswer)
        assertEquals(true, store.state.value.evaluation?.correct)
        store.dispatch(AppAction.Rate(id, Rating.Again))
        assertEquals(1, store.state.value.progress?.stats?.get(store.state.value.chain[0].primarySkill)?.correct)
        store.close()
    }

    @Test
    fun failedSaveExportsCurrentRevisionAndLaterSuccessClearsWarning() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        repo.nextSave = SaveResult.WriteFailed(IllegalStateException("quota"))
        val firstId = assertNotNull(store.state.value.exerciseId)
        revealAfterIntroduction(store, firstId)
        store.dispatch(AppAction.Rate(firstId, Rating.Good))
        runCurrent()
        assertTrue(store.state.value.error.orEmpty().contains("Экспортируй JSON"))
        store.dispatch(AppAction.RequestExport)
        val effect = store.state.value.pendingEffects.filterIsInstance<UiEffect.DownloadText>().single()
        val decoded = ProgressCodec.decodeText(effect.text.encodeToByteArray())
        assertEquals(1, (decoded as polski.progress.DecodeResult.Valid).document.progress.totalReviews)
        store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Completed))
        assertFalse(store.state.value.pendingEffects.any { it.id == effect.id })
        val secondId = assertNotNull(store.state.value.exerciseId)
        revealAfterIntroduction(store, secondId)
        store.dispatch(AppAction.Rate(secondId, Rating.Good))
        runCurrent()
        assertEquals(2, store.state.value.savedRevision)
        assertEquals(null, store.state.value.error)
        store.close()
    }

    @Test
    fun installNormalizesStaleDailyCounterBeforeFirstReview() = runTest {
        val stale = ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-22", scheduler).let {
            it.copy(progress = it.progress.copy(reviewsToday = 3, lastDay = "2026-09-22"))
        }
        val repo = FakeRepository(stale)
        val clock = MutableTime(TimeCapture(at, "2026-09-23"))
        val store = newStore(repo, backgroundScope, clock)
        store.start()
        store.dispatch(AppAction.RequestExport)
        val effect = store.state.value.pendingEffects.filterIsInstance<UiEffect.DownloadText>().single()
        val decoded = assertIs<polski.progress.DecodeResult.Valid>(ProgressCodec.decodeText(effect.text.encodeToByteArray()))
        assertEquals(0, decoded.document.progress.reviewsToday)
        assertEquals("2026-09-23", decoded.document.progress.lastDay)
        store.close()
    }

    @Test
    fun acknowledgedFailedEffectIsRemovedFromPendingEffects() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.RequestExport)
        val effect = store.state.value.pendingEffects.filterIsInstance<UiEffect.DownloadText>().single()
        store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Failed("export failed")))
        assertFalse(store.state.value.pendingEffects.any { it.id == effect.id })
        assertEquals("export failed", store.state.value.error)
        store.close()
    }

    @Test
    fun resetRequiresEffectDecisionAndRefreshUsesCurrentLocalDay() = runTest {
        val clock = MutableTime(TimeCapture(at, "2026-09-23"))
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope, clock)
        store.start()
        store.dispatch(AppAction.RequestReset)
        val confirmation = store.state.value.pendingEffects.filterIsInstance<UiEffect.ConfirmReset>().single()
        store.dispatch(AppAction.ResetDecision(confirmation.id, false))
        assertEquals(0, store.state.value.revision)
        val id = assertNotNull(store.state.value.exerciseId)
        revealAfterIntroduction(store, id)
        store.dispatch(AppAction.Rate(id, Rating.Good))
        assertEquals(1, store.state.value.todayCount)
        clock.value = TimeCapture(Instant.parse("2026-09-23T22:30:00Z"), "2026-09-24")
        store.dispatch(AppAction.RefreshTime)
        assertEquals(0, store.state.value.todayCount)
        store.dispatch(AppAction.RequestReset)
        val confirmed = store.state.value.pendingEffects.filterIsInstance<UiEffect.ConfirmReset>().single()
        store.dispatch(AppAction.ResetDecision(confirmed.id, true))
        assertEquals(0, store.state.value.progress?.totalReviews)
        assertEquals("2026-09-24", store.state.value.localDay)
        store.close()
    }

    @Test
    fun writerKeepsRevisionsOrderedWhileOlderSaveIsPending() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val held = CompletableDeferred<SaveResult>()
        repo.nextGate = held
        val store = newStore(repo, backgroundScope)
        store.start()
        val first = assertNotNull(store.state.value.exerciseId)
        revealAfterIntroduction(store, first)
        store.dispatch(AppAction.Rate(first, Rating.Good))
        runCurrent()
        val second = assertNotNull(store.state.value.exerciseId)
        revealAfterIntroduction(store, second)
        store.dispatch(AppAction.Rate(second, Rating.Good))
        runCurrent()
        assertEquals(listOf(1), repo.writes.map { it.progress.totalReviews })
        assertEquals(2, store.state.value.progress?.totalReviews)
        held.complete(SaveResult.WriteFailed(IllegalStateException("quota")))
        runCurrent()
        assertEquals(listOf(1, 2), repo.writes.map { it.progress.totalReviews })
        assertEquals(2, store.state.value.savedRevision)
        assertEquals(null, store.state.value.error)
        store.close()
    }

    @Test
    fun resetWaitsForOlderSaveAndRejectsTheOldCardAfterCompletion() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val held = CompletableDeferred<SaveResult>()
        repo.nextGate = held
        val store = newStore(repo, backgroundScope)
        store.start()
        val oldId = assertNotNull(store.state.value.exerciseId)
        revealAfterIntroduction(store, oldId)
        store.dispatch(AppAction.Rate(oldId, Rating.Good))
        runCurrent()
        assertEquals(listOf(1), repo.writes.map { it.progress.totalReviews })

        store.dispatch(AppAction.RequestReset)
        val confirmation = store.state.value.pendingEffects.filterIsInstance<UiEffect.ConfirmReset>().single()
        store.dispatch(AppAction.ResetDecision(confirmation.id, true))
        store.dispatch(AppAction.Rate(oldId, Rating.Good))
        assertEquals(0, store.state.value.progress?.totalReviews)
        assertEquals(0, repo.resets)

        held.complete(SaveResult.Saved)
        runCurrent()
        assertEquals(1, repo.resets)
        assertEquals(2, store.state.value.savedRevision)
        assertEquals(0, store.state.value.progress?.totalReviews)
        assertEquals(CardPhase.Question, store.state.value.phase)
        store.close()
    }

    @Test
    fun scheduleShowsNoDueThenRefreshActivatesCardWithoutReplacingExistingCard() = runTest {
        val later = Instant.parse("2026-09-24T12:00:00Z")
        val base = ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler)
        val future = base.copy(progress = base.progress.copy(cards = base.progress.cards.map {
            it.copy(card = it.card.copy(due = later))
        }))
        val clock = MutableTime(TimeCapture(at, "2026-09-23"))
        val store = newStore(FakeRepository(future), backgroundScope, clock)
        store.start()
        store.dispatch(AppAction.StartSchedule)
        assertEquals(CardPhase.NoDue, store.state.value.phase)
        assertEquals(later, store.state.value.nextDue)
        clock.value = TimeCapture(later, "2026-09-24")
        store.dispatch(AppAction.RefreshTime)
        assertEquals(CardPhase.Question, store.state.value.phase)
        val id = assertNotNull(store.state.value.exerciseId)
        store.dispatch(AppAction.RefreshTime)
        assertEquals(id, store.state.value.exerciseId)
        store.close()
    }

    /** UniversalCorePlan.md §6: a card for a skillId outside the active pack (e.g. left over from
     *  a future multi-pack switch) must never reach [polski.training.PlExerciseEngine] — it used to
     *  crash the whole store instead of just being skipped as not-yet-actionable. */
    @Test
    fun scheduleSkipsForeignSkillIdInsteadOfCrashing() = runTest {
        val base = ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler)
        val foreignCard = StoredCard("zh:verb.tense", SrsCard(Instant.fromEpochMilliseconds(at.toEpochMilliseconds() - 3_600_000L)))
        val withForeign = base.copy(progress = base.progress.copy(cards = base.progress.cards + foreignCard))
        val store = newStore(FakeRepository(withForeign), backgroundScope)
        store.start()
        assertEquals(skills.size, store.state.value.dueCount)
        store.dispatch(AppAction.StartSchedule)
        assertEquals(CardPhase.Question, store.state.value.phase)
        assertTrue(store.state.value.exercise!!.primarySkill in skills.map { it.id })
        store.close()
    }

    @Test
    fun invalidLoadPreservesRawForExportAndBlocksReview() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        repo.loadResult = LoadResult.Invalid("broken json", "Invalid cards")
        val store = newStore(repo, backgroundScope)
        store.start()
        assertEquals(LoadStatus.RecoveryRequired, store.state.value.loadStatus)
        val id = assertNotNull(store.state.value.exerciseId)
        revealAfterIntroduction(store, id)
        store.dispatch(AppAction.Rate(id, Rating.Good))
        assertEquals(0, store.state.value.revision)
        store.dispatch(AppAction.RequestExport)
        assertEquals("broken json", store.state.value.pendingEffects.filterIsInstance<UiEffect.DownloadJson>().single().json)
        store.dispatch(AppAction.RequestReset)
        assertTrue(store.state.value.pendingEffects.none { it is UiEffect.ConfirmReset })
        assertEquals(LoadStatus.RecoveryRequired, store.state.value.loadStatus)
        store.dispatch(AppAction.RequestExport)
        assertTrue(store.state.value.pendingEffects.filterIsInstance<UiEffect.DownloadJson>().all { it.json == "broken json" })
        assertTrue(repo.writes.isEmpty())
        store.close()
    }

    @Test
    fun legacyProgressWaitsForExplicitMigrationBeforeAnyWrite() = runTest {
        val original = ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler)
        val repo = FakeRepository(original)
        repo.loadResult = LoadResult.LegacyAvailable("legacy bytes")
        repo.migrationResult = MigrationResult.Migrated(original)
        val store = newStore(repo, backgroundScope)
        store.start()
        assertEquals(LoadStatus.MigrationAvailable, store.state.value.loadStatus)
        assertTrue(repo.writes.isEmpty())
        store.dispatch(AppAction.RequestReset)
        assertTrue(store.state.value.pendingEffects.none { it is UiEffect.ConfirmReset })
        store.dispatch(AppAction.RequestExport)
        assertEquals("legacy bytes", store.state.value.pendingEffects.filterIsInstance<UiEffect.DownloadJson>().single().json)
        store.dispatch(AppAction.RequestMigration)
        runCurrent()
        assertEquals(LoadStatus.Ready, store.state.value.loadStatus)
        assertEquals(1, repo.migrations)
        store.close()
    }

    @Test
    fun recoveryRetriesMigrationWithoutDroppingExportableRaw() = runTest {
        val original = ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler)
        val repo = FakeRepository(original)
        repo.loadResult = LoadResult.LegacyAvailable("legacy bytes")
        repo.migrationResult = MigrationResult.RecoveryRequired("preview bytes", "Injected quota failure")
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.RequestMigration)
        runCurrent()
        assertEquals(LoadStatus.RecoveryRequired, store.state.value.loadStatus)
        store.dispatch(AppAction.RequestExport)
        assertEquals("preview bytes", store.state.value.pendingEffects.filterIsInstance<UiEffect.DownloadJson>().single().json)
        assertTrue(repo.writes.isEmpty())
        repo.migrationResult = MigrationResult.Migrated(original)
        store.dispatch(AppAction.RequestMigration)
        runCurrent()
        assertEquals(LoadStatus.Ready, store.state.value.loadStatus)
        assertEquals(2, repo.migrations)
        assertTrue(repo.writes.isEmpty())
        store.close()
    }

    @Test
    fun retryOfMalformedRecoveryRemainsNonWritableAndExportable() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        repo.loadResult = LoadResult.RecoveryRequired("corrupt preview", "Invalid JSON")
        repo.migrationResult = MigrationResult.RecoveryRequired("corrupt preview", "Invalid JSON")
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.RequestMigration)
        runCurrent()
        assertEquals(LoadStatus.RecoveryRequired, store.state.value.loadStatus)
        store.dispatch(AppAction.RequestExport)
        assertEquals("corrupt preview", store.state.value.pendingEffects.filterIsInstance<UiEffect.DownloadJson>().single().json)
        assertTrue(repo.writes.isEmpty())
        store.close()
    }

    @Test
    fun focusedSelectionKeepsSkillAndInvalidChoicePreservesCurrentCard() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.OpenSkillPicker)
        store.dispatch(AppAction.ChooseSkill("case.acc.f", sentenceSeeds.first()))
        assertEquals(TrainingMode.Focused, store.state.value.mode)
        assertEquals("wife", store.state.value.exercise?.nounId)
        assertFalse(store.state.value.showSkillPicker)
        val id = assertNotNull(store.state.value.exerciseId)
        store.dispatch(AppAction.ChooseSkill("missing"))
        assertEquals(id, store.state.value.exerciseId)
        assertTrue(store.state.value.error.orEmpty().contains("Неизвестный"))
        revealAfterIntroduction(store, id)
        store.dispatch(AppAction.Rate(id, Rating.Good))
        assertEquals("case.acc.f", store.state.value.exercise?.primarySkill)
        assertTrue(store.state.value.exerciseId != id)
        store.close()
    }

    // M7 blueprint: a pilot skill's first exposure gates on predictionPending before introPending;
    // ContinuePrediction clears only predictionPending and leaves introPending (and the exercise)
    // untouched, and is rejected once already dismissed.
    @Test
    fun predictionPendingPrecedesIntroductionForAPilotSkillAndIsRejectedOnceDismissed() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.ChooseSkill("case.acc.m", sentenceSeeds.first()))
        assertEquals("case.acc.m", store.state.value.exercise?.primarySkill)
        assertTrue(store.state.value.predictionPending)
        assertTrue(store.state.value.introPending)
        val id = assertNotNull(store.state.value.exerciseId)

        // Reveal is rejected while predictionPending, even once introPending alone would allow it.
        store.dispatch(AppAction.Reveal(id))
        assertEquals(CardPhase.Question, store.state.value.phase)

        store.dispatch(AppAction.ContinuePrediction)
        assertFalse(store.state.value.predictionPending)
        assertTrue(store.state.value.introPending)
        assertEquals(id, store.state.value.exerciseId)

        // Idempotent: a second ContinuePrediction is rejected (accepts() guard), same style as
        // ContinueIntroduction's own guard.
        store.dispatch(AppAction.ContinuePrediction)
        assertFalse(store.state.value.predictionPending)

        store.dispatch(AppAction.ContinueIntroduction)
        assertFalse(store.state.value.introPending)
        revealAfterIntroduction(store, id)
        assertEquals(CardPhase.Revealed, store.state.value.phase)
        store.close()
    }

    // M7 blueprint regression guard: a skill without an authored predictionQuestion never gates on
    // predictionPending — behaves exactly as before M7.
    @Test
    fun aSkillWithoutAPredictionQuestionNeverSetsPredictionPending() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.ChooseSkill("agreement.my", sentenceSeeds.first()))
        assertFalse(store.state.value.predictionPending)
        assertTrue(store.state.value.introPending)
        store.close()
    }

    // M2 blueprint: a reliably diagnosed (Typed) error enqueues a retry ticket; RETRY_DELAY
    // further ratings on an unrelated skill mature it, substituting the mode's own next pick with
    // a fresh exercise for the errored skill (generated through the same generateForSkill path
    // M4's anti-repeat already covers — no separate seed-difference assertion needed here per the
    // blueprint's own note), flagged via activeRetrySkillId; the rating after that clears the flag.
    @Test
    fun typedErrorEnqueuesARetryTicketThatMaturesAfterRetryDelayRatings() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()

        store.dispatch(AppAction.ChooseSkill("case.acc.f", sentenceSeeds.first())) // wife/beautiful
        if (store.state.value.predictionPending) store.dispatch(AppAction.ContinuePrediction)
        if (store.state.value.introPending) store.dispatch(AppAction.ContinueIntroduction)
        store.dispatch(AppAction.SetAnswerMode(AnswerMode.Typed))
        val errorId = assertNotNull(store.state.value.exerciseId)
        store.dispatch(AppAction.EditAnswer("wrong"))
        store.dispatch(AppAction.Reveal(errorId))
        store.dispatch(AppAction.Rate(errorId, Rating.Again))
        assertEquals(listOf(RetryTicket("case.acc.f", RETRY_DELAY, errorId)), store.state.value.retryQueue)
        assertNull(store.state.value.activeRetrySkillId)

        store.dispatch(AppAction.ChooseSkill("case.acc.m"))
        store.dispatch(AppAction.SetAnswerMode(AnswerMode.Oral))
        repeat(RETRY_DELAY - 1) {
            val id = assertNotNull(store.state.value.exerciseId)
            revealAfterIntroduction(store, id)
            store.dispatch(AppAction.Rate(id, Rating.Good))
            // Not yet matured: still showing case.acc.m, not substituted.
            assertEquals("case.acc.m", store.state.value.exercise?.primarySkill)
            assertNull(store.state.value.activeRetrySkillId)
        }
        val lastId = assertNotNull(store.state.value.exerciseId)
        revealAfterIntroduction(store, lastId)
        store.dispatch(AppAction.Rate(lastId, Rating.Good)) // the RETRY_DELAY-th rating after the error

        assertEquals("case.acc.f", store.state.value.exercise?.primarySkill)
        assertEquals("case.acc.f", store.state.value.activeRetrySkillId)
        assertEquals(emptyList(), store.state.value.retryQueue)
        assertEquals("Ещё раз: Biernik · żeński", trainingCardModel(store.state.value).retryBadge)

        val retryId = assertNotNull(store.state.value.exerciseId)
        revealAfterIntroduction(store, retryId)
        store.dispatch(AppAction.Rate(retryId, Rating.Good))
        assertNull(store.state.value.activeRetrySkillId)
        store.close()
    }

    // M2 blueprint: Oral mode has no trustworthy correctness signal — an Oral "error" (rating
    // Again) must never enqueue a retry ticket.
    @Test
    fun oralRatingNeverEnqueuesARetryTicketEvenWhenRatedAgain() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.ChooseSkill("case.acc.f", sentenceSeeds.first()))
        val id = assertNotNull(store.state.value.exerciseId)
        revealAfterIntroduction(store, id)
        store.dispatch(AppAction.Rate(id, Rating.Again))
        assertEquals(emptyList(), store.state.value.retryQueue)
        store.close()
    }

    // M2 blueprint: Chain mode is scripted content — an error there never enqueues a ticket, and
    // even a ticket already pending from elsewhere is never a candidate while mode == Chain.
    @Test
    fun chainModeIsNeverInterruptedByAndNeverEnqueuesARetryTicket() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.SetAnswerMode(AnswerMode.Typed))
        for (index in 0..3) {
            val id = assertNotNull(store.state.value.exerciseId)
            if (store.state.value.predictionPending) store.dispatch(AppAction.ContinuePrediction)
            if (store.state.value.introPending) store.dispatch(AppAction.ContinueIntroduction)
            store.dispatch(AppAction.EditAnswer("wrong"))
            store.dispatch(AppAction.Reveal(id))
            store.dispatch(AppAction.Rate(id, Rating.Again))
            assertEquals(emptyList(), store.state.value.retryQueue)
            assertNull(store.state.value.activeRetrySkillId)
            assertEquals(TrainingMode.Chain, store.state.value.mode)
        }
        store.close()
    }

    @Test
    fun focusEffectIsStableUntilAcknowledgedAndCloseIgnoresLateWrite() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val held = CompletableDeferred<SaveResult>()
        repo.nextGate = held
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.StartChain(1))
        store.dispatch(AppAction.ContinueIntroduction)
        val effect = store.state.value.pendingEffects.filterIsInstance<UiEffect.FocusReveal>().single()
        store.dispatch(AppAction.SelectTab(AppTab.Progress))
        assertEquals(effect, store.state.value.pendingEffects.filterIsInstance<UiEffect.FocusReveal>().single())
        store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Skipped))
        assertFalse(store.state.value.pendingEffects.any { it.id == effect.id })
        val id = assertNotNull(store.state.value.exerciseId)
        revealAfterIntroduction(store, id)
        store.dispatch(AppAction.Rate(id, Rating.Good))
        runCurrent()
        val saved = store.state.value.savedRevision
        store.close()
        held.complete(SaveResult.Saved)
        runCurrent()
        assertEquals(saved, store.state.value.savedRevision)
    }

    // SB-6 blueprint §1/§6: shared-layer TileBuilder wiring. `case.acc.f` is one of the three
    // pilot recipes opted into tiles via tileTargetSlots (construction-based path).
    // Gap B Phase 1: `aspect` exercises can also produce tiles via StaticTileBuilder fallback
    // when their expected string contains recognized noun/adjective forms.

    @Test
    fun chooseSkillDerivesTileBuilderForOptedInAndStaticFallback() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.ChooseSkill("case.acc.f", sentenceSeeds.first()))
        val builder = assertNotNull(store.state.value.tileBuilder)
        assertTrue(builder.exercise.segments.any { it is TileSegment.Choice })
        assertEquals(emptyList(), builder.placed)
        assertNull(builder.result)

        // Gap B Phase 1: aspect exercises also get tiles via StaticTileBuilder when
        // the expected string contains recognizable noun/adjective forms.
        store.dispatch(AppAction.ChooseSkill("aspect", sentenceSeeds.first()))
        assertNotNull(store.state.value.tileBuilder)
        store.close()
    }

    @Test
    fun tilePlaceAndRemoveMutateOnlyPlaced() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.ChooseSkill("case.acc.f", sentenceSeeds.first()))
        val id = assertNotNull(store.state.value.exerciseId)
        val choice = assertNotNull(store.state.value.tileBuilder).exercise.segments.filterIsInstance<TileSegment.Choice>().first()

        store.dispatch(AppAction.Tile(id, TileAction.Place(choice.position, choice.correctForm)))
        assertEquals(listOf(choice.position), assertNotNull(store.state.value.tileBuilder).placed.map { it.position })

        store.dispatch(AppAction.Tile(id, TileAction.Place(choice.position, choice.alternatives.first())))
        assertEquals(choice.alternatives.first(), assertNotNull(store.state.value.tileBuilder).placed.single().selectedForm)

        store.dispatch(AppAction.Tile(id, TileAction.Remove(choice.position)))
        assertEquals(emptyList(), assertNotNull(store.state.value.tileBuilder).placed)
        store.close()
    }

    @Test
    fun acceptsRejectsCheckUntilEveryChoiceIsFilled() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.ChooseSkill("case.acc.f", sentenceSeeds.first()))
        val id = assertNotNull(store.state.value.exerciseId)
        store.dispatch(AppAction.Tile(id, TileAction.Check))
        assertEquals(CardPhase.Question, store.state.value.phase) // incomplete — rejected

        val choices = assertNotNull(store.state.value.tileBuilder).exercise.segments.filterIsInstance<TileSegment.Choice>()
        choices.forEach { store.dispatch(AppAction.Tile(id, TileAction.Place(it.position, it.correctForm))) }
        store.dispatch(AppAction.Tile(id, TileAction.Check))
        assertEquals(CardPhase.Revealed, store.state.value.phase)
        store.close()
    }

    @Test
    fun checkDelegatesToRevealAndComputesCorrectTileResult() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.ChooseSkill("case.acc.f", sentenceSeeds.first()))
        store.dispatch(AppAction.SetAnswerMode(AnswerMode.TileBuilder))
        val id = assertNotNull(store.state.value.exerciseId)
        val choices = assertNotNull(store.state.value.tileBuilder).exercise.segments.filterIsInstance<TileSegment.Choice>()
        choices.forEach { store.dispatch(AppAction.Tile(id, TileAction.Place(it.position, it.correctForm))) }
        store.dispatch(AppAction.Tile(id, TileAction.Check))
        assertEquals(CardPhase.Revealed, store.state.value.phase)
        val result = assertNotNull(assertNotNull(store.state.value.tileBuilder).result)
        assertTrue(result.correct)
        assertEquals(emptyList(), result.errors)
        store.close()
    }

    @Test
    fun wrongTileFormIsIncorrectAndTapErrorFillsMiniRulePanel() = runTest {
        // SB-8: this test exercises every tile slot's own wrong-answer/error-tap behavior, which
        // needs every tileTargetSlot to be a Choice — that's ScaffoldingLevel.Partial/None, not
        // the fresh-document default Full (which locks all but ~30% of slots). Seed the rung so
        // scaffolding-level progression (covered by its own test) doesn't change this test's setup.
        val fresh = ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler)
        val seeded = fresh.copy(progress = fresh.progress.copy(
            stats = fresh.progress.stats + ("case.acc.f" to fresh.progress.stats.getValue("case.acc.f").copy(tileScaffoldingRung = 1)),
        ))
        val repo = FakeRepository(seeded)
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.ChooseSkill("case.acc.f", sentenceSeeds.first()))
        store.dispatch(AppAction.SetAnswerMode(AnswerMode.TileBuilder))
        val id = assertNotNull(store.state.value.exerciseId)
        val choices = assertNotNull(store.state.value.tileBuilder).exercise.segments.filterIsInstance<TileSegment.Choice>()
        val wrongChoice = choices.first { it.alternatives.isNotEmpty() }
        choices.forEach {
            val form = if (it.position == wrongChoice.position) it.alternatives.first() else it.correctForm
            store.dispatch(AppAction.Tile(id, TileAction.Place(it.position, form)))
        }
        store.dispatch(AppAction.Tile(id, TileAction.Check))
        val result = assertNotNull(assertNotNull(store.state.value.tileBuilder).result)
        assertFalse(result.correct)
        assertEquals(listOf(wrongChoice.position), result.errors.map { it.position })

        // TapError on a non-error position is rejected by accepts().
        val correctPosition = choices.first { it.position != wrongChoice.position }.position
        store.dispatch(AppAction.Tile(id, TileAction.TapError(correctPosition)))
        assertNull(store.state.value.tileBuilder?.errorExplanation)

        store.dispatch(AppAction.Tile(id, TileAction.TapError(wrongChoice.position)))
        val explanation = assertNotNull(store.state.value.tileBuilder?.errorExplanation)
        assertTrue(explanation.isNotEmpty())
        // SB-7 regression: the explanation is now the specific per-tile diff, not just any
        // non-empty placeholder — its Changes item's "after" parts join back to the tapped
        // tile's own correctForm.
        val changes = explanation.filterIsInstance<Block.Changes>().single().items.single()
        assertEquals(wrongChoice.correctForm, changes.after.joinToString("") { it.text })
        store.close()
    }

    @Test
    fun rateRecordsTileBuilderCorrectnessIntoTheSameReviewAsTyped() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.ChooseSkill("case.acc.f", sentenceSeeds.first()))
        store.dispatch(AppAction.SetAnswerMode(AnswerMode.TileBuilder))
        val id = assertNotNull(store.state.value.exerciseId)
        val choices = assertNotNull(store.state.value.tileBuilder).exercise.segments.filterIsInstance<TileSegment.Choice>()
        choices.forEach { store.dispatch(AppAction.Tile(id, TileAction.Place(it.position, it.correctForm))) }
        store.dispatch(AppAction.Tile(id, TileAction.Check))
        store.dispatch(AppAction.Rate(id, Rating.Good))
        assertEquals(1, store.state.value.progress?.stats?.get("case.acc.f")?.correct)
        // A fresh exercise always re-derives tileBuilder rather than carrying the old one over.
        assertNotEquals(id, store.state.value.exerciseId)
        store.close()
    }

    // SB-8: all-correct TileBuilder cycles at case.acc.f promote the next exercise's scaffolding
    // Full -> Partial (at the 3rd correct) -> None (at the 5th correct after that); one wrong
    // cycle at None then demotes back to Partial.
    @Test
    fun scaffoldingLevelAdvancesAndDemotesWithTileBuilderProgress() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.ChooseSkill("case.acc.f", sentenceSeeds.first()))
        store.dispatch(AppAction.SetAnswerMode(AnswerMode.TileBuilder))

        fun playCycle(allCorrect: Boolean) {
            val id = assertNotNull(store.state.value.exerciseId)
            val choices = assertNotNull(store.state.value.tileBuilder).exercise.segments.filterIsInstance<TileSegment.Choice>()
            if (allCorrect) {
                choices.forEach { store.dispatch(AppAction.Tile(id, TileAction.Place(it.position, it.correctForm))) }
            } else {
                val wrong = choices.first { it.alternatives.isNotEmpty() }
                choices.forEach {
                    val form = if (it.position == wrong.position) it.alternatives.first() else it.correctForm
                    store.dispatch(AppAction.Tile(id, TileAction.Place(it.position, form)))
                }
            }
            store.dispatch(AppAction.Tile(id, TileAction.Check))
            store.dispatch(AppAction.Rate(id, Rating.Good))
        }

        assertEquals(ScaffoldingLevel.Full, assertNotNull(store.state.value.tileBuilder).exercise.scaffolding)
        repeat(2) { playCycle(allCorrect = true) }
        assertEquals(ScaffoldingLevel.Full, assertNotNull(store.state.value.tileBuilder).exercise.scaffolding)
        playCycle(allCorrect = true) // 3rd correct -> Partial
        assertEquals(ScaffoldingLevel.Partial, assertNotNull(store.state.value.tileBuilder).exercise.scaffolding)

        repeat(4) { playCycle(allCorrect = true) }
        assertEquals(ScaffoldingLevel.Partial, assertNotNull(store.state.value.tileBuilder).exercise.scaffolding)
        playCycle(allCorrect = true) // 5th correct at Partial -> None
        assertEquals(ScaffoldingLevel.None, assertNotNull(store.state.value.tileBuilder).exercise.scaffolding)

        playCycle(allCorrect = false) // wrong at None -> back to Partial
        assertEquals(ScaffoldingLevel.Partial, assertNotNull(store.state.value.tileBuilder).exercise.scaffolding)
        store.close()
    }

    // M3 blueprint (DAILY-SESSION-BLUEPRINT.md §Verification): TrainingMode.Daily transitions.
    // `reviews=1` for 4 already-known, already-due skills (none sharing `case.acc.f`'s New slot or
    // `verb.present`'s) fills the default mix's Review cap (4) without the single-skill "due
    // chases its own review" deadlock a totally fresh document would hit after only 2 steps (its
    // own review always pushes a card's due forward past "now", so one just-reviewed skill can
    // never immediately re-qualify for the very next step).
    private val dailyReviewSeedIds = listOf("case.acc.n", "case.inst", "case.dat", "sentence.question")

    /** M3 blueprint: `dailyAnswerMode` forces [AnswerMode.TileBuilder] for a Practice step whenever
     *  the chosen skill's exercise supports tiles — now most constructed skills (commit
     *  a67225e), so a Daily test can't just [revealAfterIntroduction]; it must fill every
     *  [TileSegment.Choice] and [TileAction.Check] instead, same as the TileBuilder tests above. */
    private fun answerDailyStep(store: TrainingStore, rating: Rating) {
        val id = assertNotNull(store.state.value.exerciseId)
        if (store.state.value.predictionPending) store.dispatch(AppAction.ContinuePrediction)
        if (store.state.value.introPending) store.dispatch(AppAction.ContinueIntroduction)
        val builder = store.state.value.tileBuilder
        if (store.state.value.answerMode == AnswerMode.TileBuilder && builder != null) {
            val choices = builder.exercise.segments.filterIsInstance<TileSegment.Choice>()
            choices.forEach { store.dispatch(AppAction.Tile(id, TileAction.Place(it.position, it.correctForm))) }
            store.dispatch(AppAction.Tile(id, TileAction.Check))
        } else {
            store.dispatch(AppAction.Reveal(id))
        }
        store.dispatch(AppAction.Rate(id, rating))
    }

    private fun dailyReadyDocument(): ProgressDocument {
        val base = ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler)
        return base.copy(progress = base.progress.copy(
            stats = base.progress.stats + dailyReviewSeedIds.associateWith { base.progress.stats.getValue(it).copy(reviews = 1, correct = 1) },
        ))
    }

    @Test
    fun startDailyRatingPlannedVolumeReachesDailyComplete() = runTest {
        val repo = FakeRepository(dailyReadyDocument())
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.StartDaily)
        assertEquals(TrainingMode.Daily, store.state.value.mode)
        assertNotNull(store.state.value.daily)

        repeat(6) { answerDailyStep(store, Rating.Good) }
        assertEquals(CardPhase.DailyComplete, store.state.value.phase)
        assertEquals(6, assertNotNull(store.state.value.daily).history.size)
        store.close()
    }

    @Test
    fun continueDailyFromCompleteGoesBackToQuestionExtendedAndKeepsHistory() = runTest {
        val repo = FakeRepository(dailyReadyDocument())
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.StartDaily)
        repeat(6) { answerDailyStep(store, Rating.Good) }
        assertEquals(CardPhase.DailyComplete, store.state.value.phase)
        val historyBefore = assertNotNull(store.state.value.daily).history

        store.dispatch(AppAction.ContinueDaily)
        assertEquals(CardPhase.Question, store.state.value.phase)
        val daily = assertNotNull(store.state.value.daily)
        assertTrue(daily.extended)
        assertEquals(historyBefore, daily.history)
        store.close()
    }

    @Test
    fun eachDailyRateProducesExactlyOneReviewReducerWrite() = runTest {
        // A fresh (unseeded) document is enough here — AC4 only needs two consecutive Daily
        // ratings (New, then Practice falling back to the same just-introduced skill) to show no
        // double-count; it does not need the full 6-step plan.
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.StartDaily)
        for (index in 0..1) {
            answerDailyStep(store, Rating.Good)
            assertEquals(index + 1, store.state.value.progress?.totalReviews)
        }
        store.close()
    }

    @Test
    fun switchingAwayFromAnInProgressDailySessionClearsDaily() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.StartDaily)
        assertNotNull(store.state.value.daily)

        store.dispatch(AppAction.StartChain())
        assertNull(store.state.value.daily)

        store.dispatch(AppAction.StartDaily)
        assertNotNull(store.state.value.daily)
        store.dispatch(AppAction.StartSchedule)
        assertNull(store.state.value.daily)

        store.dispatch(AppAction.StartDaily)
        assertNotNull(store.state.value.daily)
        store.dispatch(AppAction.ChooseSkill("case.acc.f", sentenceSeeds.first()))
        assertNull(store.state.value.daily)
        store.close()
    }

    // SB-8 install() fix: `tileBuilder` for the in-progress exercise is recomputed against the
    // just-loaded document (same pattern as `introPending`). Not independently testable as its
    // own regression today: `ExerciseGenerator.generateChain` never sets `tileTargetSlots`/
    // `tilePhrase` (see `CoreExercise.tileTargetSlots` KDoc — "Empty = this skill has no
    // sentence-builder support"), so the Chain mode's own initial exercise is never tile-eligible
    // and its `tileBuilder` is null before and after this fix either way. The fix still matters
    // the moment `generateChain` or an initial non-Chain mode ever becomes tile-eligible, and is
    // otherwise exercised the same way as every other `deriveTileBuilder(..., document)` call —
    // see `scaffoldingLevelAdvancesAndDemotesWithTileBuilderProgress` and
    // `wrongTileFormIsIncorrectAndTapErrorFillsMiniRulePanel` above, both of which seed progress
    // before `start()` and read it back correctly on the very first `ChooseSkill`.

    private fun newStore(repo: FakeRepository, scope: kotlinx.coroutines.CoroutineScope, clock: MutableTime = MutableTime(TimeCapture(at, "2026-09-23"))): TrainingStore {
        var id = 0
        return TrainingStore(
            repo, scheduler,
            PlExerciseEngine(RandomSource { 0.0 }, ExerciseIdFactory { "exercise-${++id}" }),
            clock, scope,
        )
    }

    private class MutableTime(var value: TimeCapture) : TimeSource {
        override fun capture(): TimeCapture = value
    }

    private class FakeRepository(var loaded: ProgressDocument) : ProgressRepository {
        var loadResult: LoadResult? = null
        var migrationResult: MigrationResult = MigrationResult.Missing
        var migrations = 0
        var resets = 0
        var nextSave: SaveResult? = null
        var nextGate: CompletableDeferred<SaveResult>? = null
        val writes = mutableListOf<ProgressDocument>()
        override suspend fun load(): LoadResult = loadResult ?: LoadResult.Loaded(loaded)
        override suspend fun migrateLegacy(at: Instant, localDay: String, skillIds: List<String>): MigrationResult {
            migrations++
            return migrationResult
        }
        override suspend fun save(document: ProgressDocument): SaveResult {
            writes += document
            nextGate?.let { gate ->
                nextGate = null
                return gate.await()
            }
            return nextSave?.also { nextSave = null } ?: SaveResult.Saved
        }
        override suspend fun resetConfirmed(at: Instant, localDay: String, skillIds: List<String>): SaveResult {
            resets++
            return SaveResult.Saved
        }
    }

    private class FastScheduler : Scheduler {
        override fun newCard(skillId: String, at: Instant): StoredCard = StoredCard(skillId, SrsCard(at))
        override fun preview(card: StoredCard, at: Instant): SchedulePreview = SchedulePreview(
            at.plusMinutes(1), at.plusMinutes(10), at.plusMinutes(60), at.plusMinutes(240),
        )
        override fun review(card: StoredCard, rating: Rating, at: Instant): StoredCard = card.copy(
            card = card.card.copy(due = at.plusMinutes(60), reps = card.card.reps + 1),
        )
        override fun isDue(card: StoredCard, at: Instant): Boolean = card.card.due <= at
        private fun Instant.plusMinutes(minutes: Int): Instant = Instant.fromEpochMilliseconds(toEpochMilliseconds() + minutes * 60_000L)
    }
}
