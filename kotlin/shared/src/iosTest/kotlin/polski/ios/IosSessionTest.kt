package polski.ios

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import platform.Foundation.NSUserDefaults

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
}
