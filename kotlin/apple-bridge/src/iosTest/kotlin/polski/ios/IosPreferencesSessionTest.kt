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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import polski.data.packRegistry
import polski.preferences.PreferencesDecode
import polski.preferences.UserPreferencesCodec
import polski.preferences.UserPreferencesV2

private fun IosPreferencesSession.snapshotBool(key: String) =
    Json.parseToJsonElement(currentSnapshot()).jsonObject.getValue(key).jsonPrimitive.boolean
private fun IosPreferencesSession.snapshotString(key: String) =
    Json.parseToJsonElement(currentSnapshot()).jsonObject.getValue(key).jsonPrimitive.content

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
            assertEquals("SituationFirst", assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(exported)).value.styleId.name)

            val restarted = IosPreferencesSession(defaults)
            assertEquals("Situations", Json.parseToJsonElement(restarted.currentSnapshot()).jsonObject.getValue("method").jsonPrimitive.content)
            assertNull(restarted.set(field = "appearance", value = "Dark"))
            val saved = assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(assertNotNull(restarted.exportJson()))).value
            assertEquals("SituationFirst", saved.styleId.name)
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

    /**
     * EnRuAcceptance §7 item 1 generalized [polski.data.CoursePack] enough that en-ru now passes
     * [polski.data.usableCourseSelections] too, so the pickers list both real packs and switching
     * to en-ru succeeds instead of being rejected. Restores pl-ru in `finally` regardless of
     * outcome, since [packRegistry] is a process-wide singleton shared with every other test.
     */
    @Test
    fun targetAndNativeListBothUsablePacksAndRejectTrulyUnknownOnes() {
        val suite = "polski-ios-preferences-course-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val session = IosPreferencesSession(defaults)
            val options = Json.parseToJsonElement(session.currentSnapshot()).jsonObject.getValue("coursePacks").jsonArray
            assertEquals(listOf("pl" to "ru", "en" to "ru"), options.map {
                it.jsonObject.getValue("target").jsonPrimitive.content to it.jsonObject.getValue("native").jsonPrimitive.content
            })
            assertEquals("Неизвестная пара языков: de-ru", session.set(field = "target", value = "de"))
            assertEquals("pl", session.snapshotString("target"))
            assertEquals("pl-ru", packRegistry.active.pairId, "a rejected target must not move the active pack")
            assertNull(session.set(field = "target", value = "en"))
            assertEquals("en", session.snapshotString("target"))
            assertEquals("en-ru", packRegistry.active.pairId)
            assertNull(session.set(field = "native", value = "ru"))
            assertEquals("en-ru", packRegistry.active.pairId)
        } finally {
            packRegistry.select("pl-ru")
            defaults.removePersistentDomainForName(suite)
        }
    }

    /**
     * EnRuAcceptance §7 item 2 (ADR-37 correction blocker 2): [IosSession.rebuildIfCourseSwitched]
     * rolls the process-wide active pack back to pl-ru when en-ru's engine can't build a session
     * yet — that rollback happens on a *different* bridge instance and only ever touches
     * [packRegistry], never this session's own persisted `NSUserDefaults` document. Simulating that
     * exact rollback ([packRegistry.select] straight to pl-ru) must make a later [currentSnapshot]
     * notice the mismatch, report pl-ru (not the stale "en" it kept claiming before this fix) with
     * a one-shot warning, and persist the correction so a fresh session over the same defaults
     * suite (a simulated app restart) reports it too.
     */
    @Test
    fun currentSnapshotSelfCorrectsAfterAnExternalRollbackOfTheActivePack() {
        val suite = "polski-ios-preferences-rollback-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val session = IosPreferencesSession(defaults)
            assertNull(session.set(field = "target", value = "en"))
            assertEquals("en-ru", packRegistry.active.pairId)

            packRegistry.select("pl-ru")

            val snapshot = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
            assertEquals("pl", snapshot.getValue("target").jsonPrimitive.content)
            assertEquals("ru", snapshot.getValue("native").jsonPrimitive.content)
            assertEquals(true, "packSwitchWarning" in snapshot)

            val again = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
            assertEquals(false, "packSwitchWarning" in again)

            val restarted = IosPreferencesSession(defaults)
            val restartedSnapshot = Json.parseToJsonElement(restarted.currentSnapshot()).jsonObject
            assertEquals("pl", restartedSnapshot.getValue("target").jsonPrimitive.content)
            assertEquals("ru", restartedSnapshot.getValue("native").jsonPrimitive.content)
        } finally {
            packRegistry.select("pl-ru")
            defaults.removePersistentDomainForName(suite)
        }
    }

    /** A saved, now-usable pair (en-ru) is applied on cold start, same as pl-ru always was. */
    @Test
    fun reapplySavedCoursePackAppliesAUsableSavedPack() {
        val suite = "polski-ios-preferences-course-coldstart-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            defaults.setObject(UserPreferencesCodec.encode(UserPreferencesV2(target = "en", native = "ru")), forKey = "polski-preferences-v2")
            val session = IosPreferencesSession(defaults)
            session.reapplySavedCoursePack()
            assertEquals("en-ru", packRegistry.active.pairId)
            session.currentSnapshot()
        } finally {
            packRegistry.select("pl-ru")
            defaults.removePersistentDomainForName(suite)
        }
    }
}
