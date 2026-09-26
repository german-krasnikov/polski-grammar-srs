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

/**
 * D3 (`Plans/Kotlin/FlipCardRivePlan.md`): the vocabulary card gets the same decorative Rive
 * rating effect the training card already has, wired the same way — [IosSessionTest]'s own
 * `rateFiresOnEffectOnceWithTheDomainsMappedEffect...`/`rateAgainFiresTheAgainEffect` prove it for
 * [IosSession]; these mirror that for [IosVocabularySession.onEffect], which must fire once per
 * accepted rating with `cardEffectFor`'s mapped name, and never for a rating the domain itself
 * drops (here: rating before the card is revealed).
 */
class IosVocabularySessionTest {
    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    @Test
    fun goodRatingFiresTheRememberedEffectOnceAndSkipsAnUnrevealedRate() {
        val suite = "polski-ios-vocabulary-session-test-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val session = IosVocabularySession(defaults)
            val effects = mutableListOf<String>()
            session.onEffect = { effects.add(it) }
            var ready = false
            repeat(200) {
                val snapshot = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
                if (snapshot.getValue("loadStatus").jsonPrimitive.content == "Ready") ready = true
                if (!ready) CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
            }
            assertTrue(ready, "iOS vocabulary session did not finish loading")

            session.dispatch("select", "noun.wife")
            var selected = false
            repeat(200) {
                if (Json.parseToJsonElement(session.currentSnapshot()).jsonObject
                        .getValue("currentId").jsonPrimitive.content == "noun.wife") selected = true
                if (!selected) CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
            }
            assertTrue(selected, "noun.wife did not become the current card")

            // Rating before reveal is dropped by VocabularySession.rate's own `allowed` guard —
            // onEffect must stay silent for it.
            session.dispatch("good")
            repeat(10) { CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true) }
            assertEquals(emptyList(), effects)

            session.dispatch("reveal")
            session.dispatch("good")
            var fired = false
            repeat(200) {
                if (effects.isNotEmpty()) fired = true
                if (!fired) CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
            }
            assertEquals(listOf("Remembered"), effects)

            session.close()
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    @Test
    fun againRatingFiresTheAgainEffect() {
        val suite = "polski-ios-vocabulary-session-test-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val session = IosVocabularySession(defaults)
            val effects = mutableListOf<String>()
            session.onEffect = { effects.add(it) }
            var ready = false
            repeat(200) {
                if (Json.parseToJsonElement(session.currentSnapshot()).jsonObject
                        .getValue("loadStatus").jsonPrimitive.content == "Ready") ready = true
                if (!ready) CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
            }
            assertTrue(ready, "iOS vocabulary session did not finish loading")

            session.dispatch("select", "noun.wife")
            var selected = false
            repeat(200) {
                if (Json.parseToJsonElement(session.currentSnapshot()).jsonObject
                        .getValue("currentId").jsonPrimitive.content == "noun.wife") selected = true
                if (!selected) CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
            }
            assertTrue(selected, "noun.wife did not become the current card")

            session.dispatch("reveal")
            session.dispatch("again")
            var fired = false
            repeat(200) {
                if (effects.isNotEmpty()) fired = true
                if (!fired) CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
            }
            assertEquals(listOf("Again"), effects)

            session.close()
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }
}
