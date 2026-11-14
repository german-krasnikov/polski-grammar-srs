package polski.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
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

/** M10 blueprint: `AppAction.TrainExamMistake`'s own session-source/origin lifecycle — distinct
 *  from every other `ChooseSkill`-style session start, which never touches `examRetryOrigin`. */
@OptIn(ExperimentalCoroutinesApi::class)
class TrainingStoreExamMistakeTest {
    private val at = Instant.parse("2026-09-23T12:00:00Z")
    private val scheduler: Scheduler = FastScheduler()
    private val skillId = "case.acc.f"

    @Test
    fun trainExamMistakeSetsSessionSourceModeAndOrigin() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.TrainExamMistake("b1-2017", "B1-2017-g1-2", skillId, "B1-2017-g1-5"))
        val state = store.state.value
        assertEquals(SessionSource.ExamMistake, state.sessionSource)
        assertEquals(skillId, state.focusedSkillId)
        assertEquals(TrainingMode.Focused, state.mode)
        assertEquals(AppTab.Training, state.tab)
        val origin = state.examRetryOrigin
        assertEquals(ExamRetryOrigin("b1-2017", "B1-2017-g1-2", skillId, "B1-2017-g1-5"), origin)
        store.close()
    }

    @Test
    fun unknownSkillIsRejectedWithoutMutatingOrigin() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.TrainExamMistake("b1-2017", "B1-2017-g1-2", "no.such.skill", null))
        assertNull(store.state.value.examRetryOrigin)
        assertTrue(store.state.value.error != null)
        store.close()
    }

    @Test
    fun goodRatingClearsOriginAndEmitsVerificationReadyEffect() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.TrainExamMistake("b1-2017", "B1-2017-g1-2", skillId, "B1-2017-g1-5"))
        val id = requireNotNull(store.state.value.exerciseId)
        if (store.state.value.predictionPending) store.dispatch(AppAction.ContinuePrediction)
        if (store.state.value.introPending) store.dispatch(AppAction.ContinueIntroduction)
        store.dispatch(AppAction.Reveal(id))
        store.dispatch(AppAction.Rate(id, Rating.Good))
        assertNull(store.state.value.examRetryOrigin)
        val effect = store.state.value.pendingEffects.filterIsInstance<UiEffect.ExamVerificationReady>().singleOrNull()
        assertEquals(UiEffect.ExamVerificationReady(effect?.id ?: -1L, "b1-2017", skillId, "B1-2017-g1-5"), effect)
        store.close()
    }

    @Test
    fun goodRatingWithNoVerificationItemStillClearsOriginAndCarriesNullForward() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.TrainExamMistake("b1-2017", "B1-2017-g1-3", skillId, null))
        val id = requireNotNull(store.state.value.exerciseId)
        if (store.state.value.predictionPending) store.dispatch(AppAction.ContinuePrediction)
        if (store.state.value.introPending) store.dispatch(AppAction.ContinueIntroduction)
        store.dispatch(AppAction.Reveal(id))
        store.dispatch(AppAction.Rate(id, Rating.Good))
        assertNull(store.state.value.examRetryOrigin)
        val effect = store.state.value.pendingEffects.filterIsInstance<UiEffect.ExamVerificationReady>().singleOrNull()
        assertEquals(null, effect?.verificationItemId)
        store.close()
    }

    @Test
    fun againRatingLeavesOriginPending() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.TrainExamMistake("b1-2017", "B1-2017-g1-2", skillId, "B1-2017-g1-5"))
        val id = requireNotNull(store.state.value.exerciseId)
        if (store.state.value.predictionPending) store.dispatch(AppAction.ContinuePrediction)
        if (store.state.value.introPending) store.dispatch(AppAction.ContinueIntroduction)
        store.dispatch(AppAction.Reveal(id))
        store.dispatch(AppAction.Rate(id, Rating.Again))
        assertEquals(skillId, store.state.value.examRetryOrigin?.skillId)
        assertTrue(store.state.value.pendingEffects.filterIsInstance<UiEffect.ExamVerificationReady>().isEmpty())
        store.close()
    }

    @Test
    fun startingADifferentSessionClearsAPendingOrigin() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val store = newStore(repo, backgroundScope)
        store.start()
        store.dispatch(AppAction.TrainExamMistake("b1-2017", "B1-2017-g1-2", skillId, "B1-2017-g1-5"))
        assertEquals(skillId, store.state.value.examRetryOrigin?.skillId)
        store.dispatch(AppAction.StartChain())
        assertNull(store.state.value.examRetryOrigin)
        store.close()
    }

    private fun newStore(repo: FakeRepository, scope: CoroutineScope): TrainingStore {
        var id = 0
        return TrainingStore(repo, scheduler, PlExerciseEngine(RandomSource { 0.0 }, ExerciseIdFactory { "exercise-${++id}" }), MutableTime(TimeCapture(at, "2026-09-23")), scope)
    }

    private class MutableTime(var value: TimeCapture) : TimeSource {
        override fun capture(): TimeCapture = value
    }

    private class FakeRepository(var loaded: ProgressDocument) : ProgressRepository {
        override suspend fun load(): LoadResult = LoadResult.Loaded(loaded)
        override suspend fun migrateLegacy(at: Instant, localDay: String, skillIds: List<String>): MigrationResult = MigrationResult.Missing
        override suspend fun save(document: ProgressDocument): SaveResult = SaveResult.Saved
        override suspend fun resetConfirmed(at: Instant, localDay: String, skillIds: List<String>): SaveResult = SaveResult.Saved
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
