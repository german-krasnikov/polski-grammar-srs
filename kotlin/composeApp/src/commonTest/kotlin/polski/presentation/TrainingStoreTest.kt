package polski.presentation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.TestScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
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
import polski.training.ExerciseFactory
import polski.training.ExerciseIdFactory
import polski.training.RandomSource
import polski.training.sentenceSeeds

@OptIn(ExperimentalCoroutinesApi::class)
class TrainingStoreTest {
    private val at = Instant.parse("2026-09-23T12:00:00Z")
    // The real FSRS parity suite lives in shared; transition tests control due times directly.
    private val scheduler: Scheduler = FastScheduler()

    @Test
    fun introductionAndMethodSwitchPreserveDraftPhaseAndReviewCount() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        val id = assertNotNull(store.state.value.exerciseId)
        assertTrue(store.state.value.introPending)
        assertEquals(0, store.state.value.progress?.totalReviews)
        store.dispatch(AppAction.Reveal(id))
        assertEquals(CardPhase.Question, store.state.value.phase)
        assertEquals(null, store.state.value.evaluation)
        assertEquals(0, store.state.value.progress?.totalReviews)
        store.dispatch(AppAction.SetExplanationMethod(ExplanationMethod.Situations))
        assertTrue(store.state.value.introPending)
        assertEquals(id, store.state.value.exerciseId)
        store.dispatch(AppAction.ContinueIntroduction)
        assertFalse(store.state.value.introPending)
        store.dispatch(AppAction.SetAnswerMode(AnswerMode.Typed))
        store.dispatch(AppAction.EditAnswer("Moja próba"))
        store.dispatch(AppAction.SetExplanationMethod(ExplanationMethod.Logic))
        assertEquals("Moja próba", store.state.value.draft)
        assertEquals(CardPhase.Question, store.state.value.phase)
        store.dispatch(AppAction.Reveal(id))
        store.dispatch(AppAction.SetExplanationMethod(ExplanationMethod.Situations))
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
        val effect = store.state.value.pendingEffects.filterIsInstance<UiEffect.DownloadJson>().single()
        val decoded = ProgressCodec.decode(effect.json)
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

    private fun newStore(repo: FakeRepository, scope: kotlinx.coroutines.CoroutineScope, clock: MutableTime = MutableTime(TimeCapture(at, "2026-09-23"))): TrainingStore {
        var id = 0
        return TrainingStore(
            repo, scheduler,
            ExerciseFactory(RandomSource { 0.0 }, ExerciseIdFactory { "exercise-${++id}" }),
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
