package polski.macos

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import platform.CoreFoundation.CFRunLoopRunInMode
import platform.CoreFoundation.kCFRunLoopDefaultMode
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID

/**
 * M13: MacSession/MacSnapshot had almost no target tests. Locks two things the SwiftUI host
 * relies on and that are easy to break silently:
 * 1. Every [polski.presentation.UiEffect] surfaced in a snapshot must be acknowledgeable and then
 *    leave `pendingEffects` — otherwise it re-appears in every future snapshot forever, which is
 *    exactly the growth M12 found for `FocusReveal` on every reveal.
 * 2. `MacSession.dispatch`'s own reveal/rate guards reject a command whose ID no longer matches
 *    the exercise currently on screen — the "one review per rate" contract that stops a stale or
 *    duplicate tap (from a background window or a race with the next card) from scoring twice.
 */
@OptIn(ExperimentalForeignApi::class)
class MacSessionTest {
    private fun withSession(block: (MacSession) -> Unit) {
        val directory = NSTemporaryDirectory() + "polski-mac-session-${NSUUID().UUIDString}"
        val session = MacSession(directory)
        try {
            // MacProgressRepository.load() runs on Dispatchers.Default; the fresh-progress
            // install() it triggers only lands on the Main-queued state after that finishes, so
            // the run loop must be spun until loadStatus flips to Ready (same pattern as
            // IosVocabularyCollisionSnapshotTest for the iOS bridge).
            awaitReady(session)
            block(session)
        } finally {
            session.close()
            NSFileManager.defaultManager.removeItemAtPath(directory, null)
        }
    }

    private fun awaitReady(session: MacSession) {
        repeat(200) {
            if (state(session).getValue("loadStatus").jsonPrimitive.content == "Ready") return
            CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
        }
        error("MacSession did not reach Ready in time")
    }

    private fun state(session: MacSession) = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
    private fun exerciseId(session: MacSession) =
        state(session).getValue("exercise").jsonObject.getValue("id").jsonPrimitive.content
    private fun phase(session: MacSession) = state(session).getValue("phase").jsonPrimitive.content
    private fun totalReviews(session: MacSession) = state(session).getValue("totalReviews").jsonPrimitive.int

    @Test
    fun focusEffectIsAcknowledgeableAndThenLeavesPendingEffects() = withSession { session ->
        session.dispatch("continueIntroduction")
        val effects = state(session).getValue("effects").jsonArray
        assertEquals(1, effects.size, "continueIntroduction should queue exactly one FocusReveal")
        val effect = effects.single().jsonObject
        assertEquals("focus", effect.getValue("kind").jsonPrimitive.content)
        val id = effect.getValue("id").jsonPrimitive.long

        session.acknowledgeEffect(id, "skipped")

        assertTrue(state(session).getValue("effects").jsonArray.isEmpty(),
            "an acknowledged effect must not keep re-appearing in later snapshots")
    }

    @Test
    fun staleRevealForAnExerciseNoLongerOnScreenIsIgnored() = withSession { session ->
        session.dispatch("continueIntroduction")
        val currentId = exerciseId(session)

        session.dispatch("reveal", "not-the-current-exercise")

        assertEquals("Question", phase(session))
        assertEquals(currentId, exerciseId(session))
    }

    @Test
    fun oneReviewPerRateIgnoresARepeatedRateForTheSameStaleExerciseId() = withSession { session ->
        session.dispatch("continueIntroduction")
        val firstId = exerciseId(session)
        session.dispatch("reveal", firstId)
        assertEquals("Revealed", phase(session))

        session.dispatch("rate", "$firstId|Good")
        assertEquals(1, totalReviews(session))
        val nextId = exerciseId(session)
        assertTrue(nextId != firstId, "rate should have advanced to the next exercise")

        // A duplicate or late-arriving tap replaying the same, now-stale exercise id must not
        // score a second review for it.
        session.dispatch("rate", "$firstId|Good")
        assertEquals(1, totalReviews(session))
        assertEquals(nextId, exerciseId(session))
    }

    /**
     * FlipCardRivePlan.md FC-01/FC-17/FC-20: the macOS host's decorative Rive overlay listens to
     * [MacSession.onEffect], which must fire exactly once per accepted rating — with the effect
     * `cardEffectFor` maps that [polski.srs.Rating] to — and never for a rate the domain itself
     * drops (mirrors `IosSessionTest.rateFiresOnEffectOnceWithTheDomainsMappedEffectAndSkipsADroppedSecondRate`).
     */
    @Test
    fun rateFiresOnEffectOnceWithTheDomainsMappedEffectAndSkipsADroppedSecondRate() = withSession { session ->
        val effects = mutableListOf<String>()
        session.onEffect = { effects.add(it) }

        session.dispatch("continueIntroduction")
        val firstId = exerciseId(session)
        session.dispatch("reveal", firstId)

        session.dispatch("rate", "$firstId|Good")
        assertEquals(listOf("Remembered"), effects)
        val nextId = exerciseId(session)
        assertTrue(nextId != firstId, "a real rate must advance past the rated card")

        // Same command dispatched again now targets the new, still-Question card: TrainingStore's
        // own phase guard drops it, and onEffect must stay silent — no spurious second burst.
        session.dispatch("rate", "$firstId|Good")
        assertEquals(listOf("Remembered"), effects)
    }

    @Test
    fun rateAgainFiresTheAgainEffect() = withSession { session ->
        val effects = mutableListOf<String>()
        session.onEffect = { effects.add(it) }

        session.dispatch("continueIntroduction")
        val firstId = exerciseId(session)
        session.dispatch("reveal", firstId)
        session.dispatch("rate", "$firstId|Again")

        assertEquals(listOf("Again"), effects)
    }

    /**
     * EnRuAcceptance-2026-09-28.md §7 item 2: `MacPreferencesSession.set("target"/"native", ...)`
     * flips `polski.data.packRegistry`'s process-wide active pack synchronously and independently
     * of this session — its own `TrainingStore` (built once, at construction) must notice on its
     * very next [MacSession.dispatch]/[MacSession.currentSnapshot] and rebuild for the newly
     * active pack, the same way [MacSession.importJson] already rebuilds after a progress import.
     *
     * EnRuAcceptance-2026-09-28.md §7 item 1 (ADR-37 blocker 1, closed by this task): this test
     * used to name its own premise — en-ru's engine "can't actually build yet" — because
     * `lang/en/forms.generated.json` only had verb forms; any real generation threw and this
     * bridge's `rebuildIfCourseSwitched` correctly rolled the switch back rather than crash.
     * `scripts/build-pack-en.mjs` now materializes every category `ConstructionRealizer` can
     * query (noun/adjective/possessive/pronoun/prep/neg/verb/aux), and `caseSentencePrefix`/
     * `personalPronounForm` (`CourseData.kt`/`Pronouns.kt`) no longer force a pack's own case ids
     * through pl's closed [polski.model.GramCase] — en-ru genuinely builds now, so the switch must
     * succeed, not roll back.
     */
    @Test
    fun switchingToEnRuNowSucceedsAndServesARealEnglishExercise() = withSession { session ->
        session.dispatch("continueIntroduction")
        val beforeId = exerciseId(session)
        try {
            polski.data.selectCoursePack("en-ru")

            session.dispatch("refresh")

            assertEquals("en-ru", polski.data.activeCoursePackId, "en-ru now builds a real session and must stay active")
            assertTrue(exerciseId(session) != beforeId, "switching pack must serve a new (en-ru) exercise, not the stale pl-ru one")
            assertEquals("Question", phase(session))
        } finally {
            polski.data.selectCoursePack("pl-ru")
        }
    }
}
