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
     * EnRuAcceptance §7 item 1 generalized [polski.data.CoursePack] enough that en-ru now passes
     * [polski.data.usableCourseSelections] too, so this bridge's target/native pickers list both
     * real, registered packs — not only pl-ru.
     */
    @Test fun defaultSnapshotExposesBothRegisteredPackOptions() = withSession { session ->
        val snapshot = snapshotOf(session)
        assertEquals("pl", snapshot.getValue("target").jsonPrimitive.content)
        assertEquals("ru", snapshot.getValue("native").jsonPrimitive.content)
        val packs = snapshot.getValue("packs").let { it as kotlinx.serialization.json.JsonArray }
        assertEquals(2, packs.size)
        val plRu = packs[0] as JsonObject
        assertEquals("pl-ru", plRu.getValue("pairId").jsonPrimitive.content)
        assertEquals("Польский", plRu.getValue("targetLabel").jsonPrimitive.content)
        assertEquals("Русский", plRu.getValue("nativeLabel").jsonPrimitive.content)
        val enRu = packs[1] as JsonObject
        assertEquals("en-ru", enRu.getValue("pairId").jsonPrimitive.content)
        assertEquals("Английский", enRu.getValue("targetLabel").jsonPrimitive.content)
        assertEquals("Русский", enRu.getValue("nativeLabel").jsonPrimitive.content)
    }

    @Test fun settingTargetToAnUnregisteredLanguageIsRejected() = withSession { session ->
        val error = session.set("target", "de")
        assertEquals(true, error != null)
        assertEquals("pl", snapshotOf(session).getValue("target").jsonPrimitive.content)
    }

    @Test fun settingTargetToItsOwnCurrentValueSucceeds() = withSession { session ->
        val error = session.set("target", "pl")
        assertNull(error)
        assertEquals("pl", snapshotOf(session).getValue("target").jsonPrimitive.content)
    }

    /**
     * EnRuAcceptance §7 item 1: picking English is now a real, accepted switch — this bridge's own
     * [syncActivePack] then flips the process-wide [polski.data.packRegistry] to en-ru, so this
     * test restores pl-ru afterwards (`finally`) the same way [polski.data.EnRuPackSwitchTest]'s
     * own switching tests do, since that registry is shared with every other test in this binary.
     */
    @Test fun settingTargetToEnglishSwitchesToTheEnRuPack() = withSession { session ->
        try {
            val error = session.set("target", "en")
            assertNull(error)
            assertEquals("en", snapshotOf(session).getValue("target").jsonPrimitive.content)
            assertEquals("en-ru", polski.data.activeCoursePackId)
        } finally {
            session.set("target", "pl")
        }
    }
}
