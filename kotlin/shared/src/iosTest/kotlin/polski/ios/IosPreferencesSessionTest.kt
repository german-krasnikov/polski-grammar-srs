package polski.ios

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import platform.Foundation.NSUserDefaults
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import polski.preferences.PreferencesDecode
import polski.preferences.UserPreferencesCodec
import polski.preferences.UserPreferencesV2

private fun IosPreferencesSession.snapshotBool(key: String) =
    Json.parseToJsonElement(currentSnapshot()).jsonObject.getValue(key).jsonPrimitive.boolean

class IosPreferencesSessionTest {
    @Test
    fun firstLaunchMigratesLegacySituationsThroughExportRestartAndAppearanceEdit() {
        val suite = "polski-ios-preferences-legacy-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            defaults.setObject("Situations", forKey = "explanationMethod")
            val first = IosPreferencesSession(defaults)
            assertEquals("Situations", Json.parseToJsonElement(first.currentSnapshot()).jsonObject.getValue("method").jsonPrimitive.content)
            val exported = assertNotNull(first.exportJson())
            assertEquals("Situations", assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(exported)).value.explanationMethod.name)

            val restarted = IosPreferencesSession(defaults)
            assertEquals("Situations", Json.parseToJsonElement(restarted.currentSnapshot()).jsonObject.getValue("method").jsonPrimitive.content)
            assertNull(restarted.set(field = "appearance", value = "Dark"))
            val saved = assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(assertNotNull(restarted.exportJson()))).value
            assertEquals("Situations", saved.explanationMethod.name)
            assertEquals("Dark", saved.appearance.name)
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }

    @Test
    fun unsupportedStoredDocumentStaysInRecoveryDespiteLegacyMethod() {
        val suite = "polski-ios-preferences-unsupported-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val raw = """{"schemaVersion":3,"explanationMethod":"Logic"}"""
            defaults.setObject("Situations", forKey = "explanationMethod")
            defaults.setObject(raw, forKey = "polski-preferences-v2")
            val session = IosPreferencesSession(defaults)
            assertEquals("RecoveryRequired", Json.parseToJsonElement(session.currentSnapshot()).jsonObject.getValue("status").jsonPrimitive.content)
            assertEquals(raw, session.exportJson())
            assertEquals("Настройки требуют восстановления", session.set(field = "appearance", value = "Dark"))
            assertEquals(raw, defaults.stringForKey("polski-preferences-v2"))
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }

    @Test
    fun appearanceEditPreservesImportedTintAndInvalidImportPreservesDocument() {
        val suite = "polski-ios-preferences-test-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val session = IosPreferencesSession(defaults)
            val imported = UserPreferencesCodec.encode(UserPreferencesV2(glassTintPercent = 87))
            assertNull(session.importJson(imported))
            assertEquals("Invalid glassTintPercent", session.importJson(imported.replace("87", "101")))
            assertNull(session.set(field = "appearance", value = "Dark"))
            val saved = assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(assertNotNull(session.exportJson()))).value
            assertEquals(87, saved.glassTintPercent)
            assertEquals("Dark", saved.appearance.name)
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }

    @Test
    fun animationsEnabledDefaultsToTrueAndPersistsAcrossRestart() {
        val suite = "polski-ios-preferences-animations-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val session = IosPreferencesSession(defaults)
            assertEquals(true, session.snapshotBool("animationsEnabled"))
            assertEquals("Неверное значение", session.set(field = "animationsEnabled", value = "maybe"))
            assertNull(session.set(field = "animationsEnabled", value = "false"))
            assertEquals(false, session.snapshotBool("animationsEnabled"))

            val restarted = IosPreferencesSession(defaults)
            assertEquals(false, restarted.snapshotBool("animationsEnabled"))
            val saved = assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(assertNotNull(restarted.exportJson()))).value
            assertEquals(false, saved.animationsEnabled)
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }

    @Test
    fun repeatedImportsKeepOnlyTheLatestBackup() {
        val suite = "polski-ios-preferences-backup-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val session = IosPreferencesSession(defaults)
            val first = UserPreferencesCodec.encode(UserPreferencesV2(glassTintPercent = 10))
            val second = UserPreferencesCodec.encode(UserPreferencesV2(glassTintPercent = 20))
            assertNull(session.importJson(first))
            assertNull(session.importJson(second))

            val backups = defaults.dictionaryRepresentation().keys.filter {
                it.toString().startsWith("polski-preferences-import-backup-")
            }
            assertEquals(1, backups.size, "Only the most recent pre-import backup should be kept, not one per import")
            assertEquals(first, defaults.stringForKey(backups.single().toString()))
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }
}
