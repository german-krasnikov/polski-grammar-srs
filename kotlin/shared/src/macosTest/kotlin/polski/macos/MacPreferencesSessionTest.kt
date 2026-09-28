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

    /**
     * EN-22: the target/native pickers next to the style picker. Only pl-ru is a real, registered
     * pack today (see `polski.data.packRegistry`'s own KDoc for why en-ru isn't wired in yet), so
     * this pins the honest current shape — one pack option, pl-ru selected and unrejectable — not a
     * second pack this bridge can't safely offer.
     */
    @Test fun defaultSnapshotExposesPlRuAsTheOnlyPackOption() = withSession { session ->
        val snapshot = snapshotOf(session)
        assertEquals("pl", snapshot.getValue("target").jsonPrimitive.content)
        assertEquals("ru", snapshot.getValue("native").jsonPrimitive.content)
        val packs = snapshot.getValue("packs").let { it as kotlinx.serialization.json.JsonArray }
        assertEquals(1, packs.size)
        val onlyPack = packs.single() as JsonObject
        assertEquals("pl-ru", onlyPack.getValue("pairId").jsonPrimitive.content)
        assertEquals("Польский", onlyPack.getValue("targetLabel").jsonPrimitive.content)
        assertEquals("Русский", onlyPack.getValue("nativeLabel").jsonPrimitive.content)
    }

    @Test fun settingTargetToAnUnregisteredLanguageIsRejected() = withSession { session ->
        val error = session.set("target", "en")
        assertEquals(true, error != null)
        assertEquals("pl", snapshotOf(session).getValue("target").jsonPrimitive.content)
    }

    @Test fun settingTargetToItsOwnCurrentValueSucceeds() = withSession { session ->
        val error = session.set("target", "pl")
        assertNull(error)
        assertEquals("pl", snapshotOf(session).getValue("target").jsonPrimitive.content)
    }
}
