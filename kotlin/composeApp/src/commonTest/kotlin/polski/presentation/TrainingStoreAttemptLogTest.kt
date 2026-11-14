package polski.presentation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.data.skills
import polski.progress.AttemptEvent
import polski.progress.AttemptKind
import polski.progress.AttemptLogDocument
import polski.progress.AttemptLogLoadResult
import polski.progress.AttemptLogRepository
import polski.progress.AttemptLogSaveResult
import polski.progress.CorrectnessBasis
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
import polski.training.RUBRIC_VERSION
import polski.training.sentenceSeeds

/** P1 blueprint §P1.1/§P1.2/§P1.5 wiring: `TrainingStore.rate()` appends exactly one
 *  [AttemptEvent] per successful rating, carries `correctionOf`/`AttemptKind.Retry` for a matured
 *  M2 ticket, stamps `helpShown` from the self-direction signal, and never lets a journal write
 *  failure block or roll back the review that already succeeded. */
@OptIn(ExperimentalCoroutinesApi::class)
class TrainingStoreAttemptLogTest {
    private val at = Instant.parse("2026-09-23T12:00:00Z")
    private val scheduler: Scheduler = FastScheduler()

    private fun revealAfterIntroduction(store: TrainingStore, exerciseId: String) {
        if (store.state.value.predictionPending) store.dispatch(AppAction.ContinuePrediction)
        if (store.state.value.introPending) store.dispatch(AppAction.ContinueIntroduction)
        store.dispatch(AppAction.Reveal(exerciseId))
    }

    private fun newStore(
        repo: FakeRepository,
        attemptLog: FakeAttemptLogRepository,
        scope: kotlinx.coroutines.CoroutineScope,
    ): TrainingStore {
        var id = 0
        return TrainingStore(
            repo, scheduler,
            PlExerciseEngine(RandomSource { 0.0 }, ExerciseIdFactory { "exercise-${++id}" }),
            MutableTime(TimeCapture(at, "2026-09-23")), scope,
            attemptLog = attemptLog,
        )
    }

    @Test
    fun oneRateCallAppendsExactlyOneAttemptEventWithTheExpectedFields() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val attemptLog = FakeAttemptLogRepository()
        val store = newStore(repo, attemptLog, backgroundScope)
        store.start()
        store.dispatch(AppAction.ChooseSkill("case.acc.f", sentenceSeeds.first()))
        store.dispatch(AppAction.SetAnswerMode(AnswerMode.Typed))
        val id = assertNotNull(store.state.value.exerciseId)
        val sessionId = assertNotNull(store.state.value.sessionId)
        val expected = assertNotNull(store.state.value.exercise).expected
        store.dispatch(AppAction.EditAnswer(expected))
        revealAfterIntroduction(store, id)
        store.dispatch(AppAction.Rate(id, Rating.Good))
        runCurrent()

        assertEquals(1, attemptLog.saved.size)
        val event = attemptLog.saved.single()
        assertEquals(id, event.attemptId)
        assertEquals(sessionId, event.sessionId)
        assertEquals("case.acc.f", event.skillId)
        assertEquals(AttemptKind.Practice, event.kind)
        assertEquals(true, event.correct)
        assertEquals(CorrectnessBasis.Evaluated, event.correctnessBasis)
        assertEquals("Good", event.selfRating)
        assertNull(event.correctionOf)
        assertEquals("Focused", event.sessionKind)
        assertEquals(SessionSource.ManualPick.name, event.sessionSource)
        assertEquals(RUBRIC_VERSION, event.rubricVersion)
        assertEquals(1, event.schemaVersion)
        store.close()
    }

    @Test
    fun secondRateCallAppendsASecondDistinctEventNeverEditingTheFirst() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val attemptLog = FakeAttemptLogRepository()
        val store = newStore(repo, attemptLog, backgroundScope)
        store.start()
        for (index in 0..1) {
            val id = assertNotNull(store.state.value.exerciseId)
            revealAfterIntroduction(store, id)
            store.dispatch(AppAction.Rate(id, Rating.Good))
            runCurrent()
        }
        assertEquals(2, attemptLog.saved.size)
        assertNotEquals(attemptLog.saved[0].attemptId, attemptLog.saved[1].attemptId)
        store.close()
    }

    @Test
    fun maturedRetryAttemptCarriesCorrectionOfAndRetryKind() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val attemptLog = FakeAttemptLogRepository()
        val store = newStore(repo, attemptLog, backgroundScope)
        store.start()

        store.dispatch(AppAction.ChooseSkill("case.acc.f", sentenceSeeds.first()))
        store.dispatch(AppAction.SetAnswerMode(AnswerMode.Typed))
        val errorId = assertNotNull(store.state.value.exerciseId)
        if (store.state.value.predictionPending) store.dispatch(AppAction.ContinuePrediction)
        if (store.state.value.introPending) store.dispatch(AppAction.ContinueIntroduction)
        store.dispatch(AppAction.EditAnswer("wrong"))
        store.dispatch(AppAction.Reveal(errorId))
        store.dispatch(AppAction.Rate(errorId, Rating.Again))
        runCurrent()

        store.dispatch(AppAction.ChooseSkill("case.acc.m"))
        store.dispatch(AppAction.SetAnswerMode(AnswerMode.Oral))
        repeat(RETRY_DELAY) {
            val id = assertNotNull(store.state.value.exerciseId)
            revealAfterIntroduction(store, id)
            store.dispatch(AppAction.Rate(id, Rating.Good))
            runCurrent()
        }
        assertEquals("case.acc.f", store.state.value.activeRetrySkillId)

        val retryId = assertNotNull(store.state.value.exerciseId)
        revealAfterIntroduction(store, retryId)
        store.dispatch(AppAction.Rate(retryId, Rating.Good))
        runCurrent()

        val retryEvent = attemptLog.saved.last()
        assertEquals(retryId, retryEvent.attemptId)
        assertEquals(AttemptKind.Retry, retryEvent.kind)
        assertEquals(errorId, retryEvent.correctionOf)
        store.close()
    }

    @Test
    fun helpShownIsTrueOnlyWhenTheReferenceWasOpenedBeforeReveal() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val attemptLog = FakeAttemptLogRepository()
        val store = newStore(repo, attemptLog, backgroundScope)
        store.start()
        store.dispatch(AppAction.ChooseSkill("case.acc.f", sentenceSeeds.first()))
        store.dispatch(AppAction.SetAnswerMode(AnswerMode.Typed))
        val withHelpId = assertNotNull(store.state.value.exerciseId)
        store.dispatch(AppAction.ToggleReference)
        revealAfterIntroduction(store, withHelpId)
        store.dispatch(AppAction.Rate(withHelpId, Rating.Good))
        runCurrent()
        assertEquals(true, attemptLog.saved[0].helpShown)

        // showReference is sticky across cards (toggled, not per-card) until explicitly toggled
        // off again — turn it off so this second card is a genuine unaided control.
        store.dispatch(AppAction.ToggleReference)
        val withoutHelpId = assertNotNull(store.state.value.exerciseId)
        revealAfterIntroduction(store, withoutHelpId)
        store.dispatch(AppAction.Rate(withoutHelpId, Rating.Good))
        runCurrent()
        assertEquals(false, attemptLog.saved[1].helpShown)
        store.close()
    }

    @Test
    fun oralModeHasNoAutomaticGradingSoBasisIsSelfReported() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val attemptLog = FakeAttemptLogRepository()
        val store = newStore(repo, attemptLog, backgroundScope)
        store.start()
        val id = assertNotNull(store.state.value.exerciseId)
        revealAfterIntroduction(store, id)
        store.dispatch(AppAction.Rate(id, Rating.Good))
        runCurrent()
        val event = attemptLog.saved.single()
        assertEquals(CorrectnessBasis.SelfReported, event.correctnessBasis)
        assertNull(event.correct)
        store.close()
    }

    @Test
    fun chooseSkillMintsAFreshSessionIdDifferentFromTheInitialLoad() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val attemptLog = FakeAttemptLogRepository()
        val store = newStore(repo, attemptLog, backgroundScope)
        store.start()
        val initialSessionId = assertNotNull(store.state.value.sessionId)
        assertEquals(SessionSource.Chain, store.state.value.sessionSource)
        store.dispatch(AppAction.ChooseSkill("case.acc.f", sentenceSeeds.first()))
        val nextSessionId = assertNotNull(store.state.value.sessionId)
        assertNotEquals(initialSessionId, nextSessionId)
        assertEquals(SessionSource.ManualPick, store.state.value.sessionSource)
        store.close()
    }

    @Test
    fun attemptLogWriteFailureNeverBlocksOrRollsBackTheReview() = runTest {
        val repo = FakeRepository(ProgressCodec.fresh(skills.map { it.id }, at, "2026-09-23", scheduler))
        val attemptLog = FakeAttemptLogRepository(nextResult = AttemptLogSaveResult.WriteFailed(IllegalStateException("quota")))
        val store = newStore(repo, attemptLog, backgroundScope)
        store.start()
        val id = assertNotNull(store.state.value.exerciseId)
        val skillId = assertNotNull(store.state.value.exercise).primarySkill
        revealAfterIntroduction(store, id)
        store.dispatch(AppAction.Rate(id, Rating.Good))
        runCurrent()
        // The FSRS review and Progress save already succeeded regardless of the journal write.
        assertEquals(1, store.state.value.progress?.stats?.get(skillId)?.reviews)
        assertEquals(1, repo.writes.size)
        assertNotNull(store.state.value.error, "a journal write failure surfaces, but never blocks the review")
        store.close()
    }

    private class MutableTime(var value: TimeCapture) : TimeSource {
        override fun capture(): TimeCapture = value
    }

    private class FakeAttemptLogRepository(private var nextResult: AttemptLogSaveResult? = null) : AttemptLogRepository {
        val saved = mutableListOf<AttemptEvent>()
        override suspend fun load(): AttemptLogLoadResult = AttemptLogLoadResult.Loaded(AttemptLogDocument(polski.progress.AttemptLog(1, saved.toList())))
        override suspend fun append(event: AttemptEvent): AttemptLogSaveResult {
            saved += event
            return nextResult?.also { nextResult = null } ?: AttemptLogSaveResult.Saved
        }
    }

    private class FakeRepository(var loaded: ProgressDocument) : ProgressRepository {
        var nextSave: SaveResult? = null
        val writes = mutableListOf<ProgressDocument>()
        override suspend fun load(): LoadResult = LoadResult.Loaded(loaded)
        override suspend fun migrateLegacy(at: Instant, localDay: String, skillIds: List<String>): MigrationResult = MigrationResult.Missing
        override suspend fun save(document: ProgressDocument): SaveResult {
            writes += document
            return nextSave?.also { nextSave = null } ?: SaveResult.Saved
        }
        override suspend fun resetConfirmed(at: Instant, localDay: String, skillIds: List<String>): SaveResult {
            writes += loaded
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
