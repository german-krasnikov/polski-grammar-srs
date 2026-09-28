package polski.ios

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import platform.CoreFoundation.CFRunLoopRunInMode
import platform.CoreFoundation.kCFRunLoopDefaultMode
import platform.Foundation.NSUserDefaults
import polski.data.packRegistry
import polski.data.selectActiveCoursePack

/**
 * M10: a store built for a fresh scene must start from the answerMode (and explanationMethod)
 * already persisted in defaults, the same way both preferences are re-applied after import.
 */
class IosSessionTest {
    @Test
    fun newSessionRestoresPersistedAnswerModeAndExplanationMethod() {
        val suite = "polski-ios-session-test-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            defaults.setObject("Typed", forKey = "answerMode")
            defaults.setObject("Situations", forKey = "explanationMethod")

            val session = IosSession(defaults)
            val snapshot = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
            assertEquals("Typed", snapshot.getValue("answerMode").jsonPrimitive.content)
            assertEquals("Situations", snapshot.getValue("explanationMethod").jsonPrimitive.content)
            session.close()
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }

    /**
     * EN-22 diagnostic: reproduces the exact live crash found on iPhone 17 Pro Simulator — a
     * [session]'s `currentSnapshot()` called *after* `packRegistry` is switched to en-ru (as
     * `AppModel.init()` now does, `reapplySavedCoursePack()` before `session.currentSnapshot()`)
     * must not throw, even though the first call already happened with pl-ru active.
     */
    @Test
    fun currentSnapshotAfterSwitchingActivePackToEnRuDoesNotThrow() {
        val suite = "polski-ios-session-enru-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val session = IosSession(defaults)
            session.currentSnapshot() // mirrors the first, pl-active call `onState`'s setter makes
            selectActiveCoursePack("en-ru") // the host path; a no-op while en-ru is unusable (ADR-35)
            session.currentSnapshot() // mirrors AppModel.init()'s explicit call after reapply
            session.close()
        } finally {
            packRegistry.select("pl-ru")
            defaults.removePersistentDomainForName(suite)
        }
    }

    @Test
    fun newSessionDefaultsToOralAndLogicWithoutPersistedPreferences() {
        val suite = "polski-ios-session-test-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val session = IosSession(defaults)
            val snapshot = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
            assertEquals("Oral", snapshot.getValue("answerMode").jsonPrimitive.content)
            assertEquals("Logic", snapshot.getValue("explanationMethod").jsonPrimitive.content)
            session.close()
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }

    /**
     * FlipCardRivePlan.md FC-01/FC-17/FC-20: the iOS host's decorative Rive overlay listens to
     * [IosSession.onEffect], which must fire exactly once per accepted rating — with the effect
     * `cardEffectFor` maps that [polski.srs.Rating] to — and never for a rate the domain itself
     * drops (here: a second "rate" landing on the already-advanced, still-unrevealed next card).
     */
    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    @Test
    fun rateFiresOnEffectOnceWithTheDomainsMappedEffectAndSkipsADroppedSecondRate() {
        val suite = "polski-ios-session-test-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val session = IosSession(defaults)
            val effects = mutableListOf<String>()
            session.onEffect = { effects.add(it) }
            var loaded = false
            repeat(200) {
                val snapshot = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
                if (snapshot.getValue("loadStatus").jsonPrimitive.content == "Ready") loaded = true
                if (!loaded) CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
            }
            assertTrue(loaded, "iOS session did not finish loading")

            session.dispatch("continueIntroduction")
            session.dispatch("reveal")
            val ratedId = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
                .getValue("exercise").jsonObject.getValue("id").jsonPrimitive.content

            session.dispatch("rate", "Good")
            assertEquals(listOf("Remembered"), effects)
            val nextId = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
                .getValue("exercise").jsonObject.getValue("id").jsonPrimitive.content
            assertTrue(ratedId != nextId, "a real rate must advance past the rated card")

            // Same command dispatched again now targets the new, still-Question card: TrainingStore.rate's
            // own phase guard drops it, and onEffect must stay silent — no spurious second burst.
            session.dispatch("rate", "Good")
            assertEquals(listOf("Remembered"), effects)

            session.close()
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    @Test
    fun rateAgainFiresTheAgainEffect() {
        val suite = "polski-ios-session-test-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val session = IosSession(defaults)
            val effects = mutableListOf<String>()
            session.onEffect = { effects.add(it) }
            var loaded = false
            repeat(200) {
                val snapshot = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
                if (snapshot.getValue("loadStatus").jsonPrimitive.content == "Ready") loaded = true
                if (!loaded) CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
            }
            assertTrue(loaded, "iOS session did not finish loading")

            session.dispatch("continueIntroduction")
            session.dispatch("reveal")
            session.dispatch("rate", "Again")
            assertEquals(listOf("Again"), effects)

            session.close()
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }
}
