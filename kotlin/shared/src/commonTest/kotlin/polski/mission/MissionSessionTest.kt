package polski.mission

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import polski.mission.data.MissionNodeRowOutcome

/** M9 blueprint: `MissionSession`'s state machine against the real authored `book-shop` mission
 *  (asking about a book, a goal-irrelevant exit, and a goal-reached purchase). */
class MissionSessionTest {
    private val at = Instant.parse("2026-11-14T12:00:00Z")

    private fun session(repository: MissionRepository = MemoryMissionRepository()) =
        MissionSession(repository, { at })

    @Test fun startOpensTheMissionOnItsFirstAuthoredNode() = runTest {
        val session = session()
        session.start("book-shop")
        val state = session.state.value
        assertEquals("book-shop", state.missionId)
        assertEquals("book-shop-ask", state.nodeId)
        assertFalse(state.completed)
        assertNull(state.lastResult)
        assertEquals(2, state.choices.size)
    }

    @Test fun startWithAnUnknownMissionIdFailsSoftWithAnError() = runTest {
        val session = session()
        session.start("no-such-mission")
        assertTrue(session.state.value.error != null)
    }

    // AC3: a grammatically correct but goal-irrelevant reply is distinguishable from a
    // goal-correct but misspelled one — meaningCorrect and formCorrect never merge into one flag.
    @Test fun chooseReportsMeaningAndFormIndependently_correctFormWrongMeaning() = runTest {
        val session = session()
        session.start("book-shop")
        val outcome = session.choose("book-shop-ask-no", "Nie, dziękuję.")
        assertFalse(outcome.meaningCorrect)
        assertTrue(outcome.formCorrect)
        assertFalse(outcome.reachedGoal)
        assertEquals("book-shop-leave", session.state.value.nodeId)
        assertTrue(session.state.value.completed)
        assertEquals(MissionNodeRowOutcome.DEAD_END, session.state.value.outcome)
    }

    @Test fun chooseReportsMeaningAndFormIndependently_wrongFormCorrectMeaning() = runTest {
        val session = session()
        session.start("book-shop")
        val outcome = session.choose("book-shop-ask-yes", "jakis losowy tekst")
        assertTrue(outcome.meaningCorrect)
        assertFalse(outcome.formCorrect)
        assertFalse(outcome.reachedGoal)
        assertEquals("book-shop-alt", session.state.value.nodeId)
    }

    @Test fun chooseAcceptsTheAuthoredVariantAsFormCorrectToo() = runTest {
        val session = session()
        session.start("book-shop")
        val outcome = session.choose("book-shop-ask-yes", "Czy macie książkę „Pan Tadeusz”?")
        assertTrue(outcome.formCorrect)
    }

    @Test fun formComparisonIsNormalizedLikeEveryOtherTypedAnswerCheck() = runTest {
        val session = session()
        session.start("book-shop")
        val outcome = session.choose("book-shop-ask-yes", "  CZY masz książkę „Pan Tadeusz”  ")
        assertTrue(outcome.formCorrect)
    }

    @Test fun walkingTheGoalReachedPathSetsReachedGoalOnTheFinalChoiceOnly() = runTest {
        val session = session()
        session.start("book-shop")
        val first = session.choose("book-shop-ask-yes", "Czy masz książkę „Pan Tadeusz”?")
        assertFalse(first.reachedGoal)
        assertFalse(session.state.value.completed)
        val second = session.choose("book-shop-alt-yes", "Czy możecie polecić inną książkę?")
        assertTrue(second.reachedGoal)
        assertTrue(session.state.value.completed)
        assertEquals(MissionNodeRowOutcome.GOAL_REACHED, session.state.value.outcome)
    }

    @Test fun completePersistsTheOutcomeAndCompleteBeforeReachingATerminalNodeIsANoop() = runTest {
        val repository = MemoryMissionRepository()
        val session = MissionSession(repository, { at })
        session.start("book-shop")
        assertFalse(session.complete())
        session.choose("book-shop-ask-yes", "Czy masz książkę „Pan Tadeusz”?")
        session.choose("book-shop-alt-yes", "Czy możecie polecić inną książkę?")
        assertTrue(session.complete())
        val saved = MissionCodec.decode(repository.raw!!)
        assertEquals(MissionProgressEntry("goal_reached", at.toString()), saved.progress["book-shop"])
    }

    @Test fun completeSurfacesAWriteFailureWithoutLosingTheCompletedState() = runTest {
        val repository = MemoryMissionRepository()
        repository.failSaves = true
        val session = MissionSession(repository, { at })
        session.start("book-shop")
        session.choose("book-shop-ask-no", "Nie, dziękuję.")
        assertFalse(session.complete())
        assertTrue(session.state.value.error != null)
        assertTrue(session.state.value.completed)
    }

    private class MemoryMissionRepository : MissionRepository {
        var raw: String? = null
        var failSaves = false
        override suspend fun loadRaw(): String? = raw
        override suspend fun saveRaw(value: String) {
            if (failSaves) throw IllegalStateException("Нет места")
            raw = value
        }
    }
}
