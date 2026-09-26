package polski.macos

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID

/**
 * D5 (`Plans/Kotlin/FlipCardRivePlan.md`): the native Settings "Анимации" toggle round-trips
 * through the shared [UserPreferencesV2.animationsEnabled] field via [MacPreferencesSession],
 * exactly like every other preference field it already exposes (`method`, `appearance`, ...).
 */
@OptIn(ExperimentalForeignApi::class)
class MacPreferencesSessionTest {
    private fun withSession(block: (MacPreferencesSession) -> Unit) {
        val directory = NSTemporaryDirectory() + "polski-mac-prefs-session-${NSUUID().UUIDString}"
        val session = MacPreferencesSession(directory)
        try {
            block(session)
        } finally {
            NSFileManager.defaultManager.removeItemAtPath(directory, null)
        }
    }

    private fun snapshotOf(session: MacPreferencesSession): JsonObject =
        Json.parseToJsonElement(session.currentSnapshot()) as JsonObject

    @Test fun defaultSnapshotExposesAnimationsEnabledTrue() = withSession { session ->
        assertEquals(true, snapshotOf(session).getValue("animationsEnabled").jsonPrimitive.boolean)
    }

    @Test fun settingAnimationsEnabledFalsePersistsAndReflectsInSnapshot() = withSession { session ->
        val error = session.set("animationsEnabled", "false")
        assertNull(error)
        assertEquals(false, snapshotOf(session).getValue("animationsEnabled").jsonPrimitive.boolean)
    }

    @Test fun settingAnimationsEnabledInvalidValueIsRejected() = withSession { session ->
        val error = session.set("animationsEnabled", "nope")
        assertEquals(true, error != null)
        assertEquals(true, snapshotOf(session).getValue("animationsEnabled").jsonPrimitive.boolean)
    }
}
