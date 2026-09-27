package polski.desktop

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import polski.preferences.Appearance
import polski.preferences.PreferencesLoad
import polski.preferences.PreferencesSave
import polski.preferences.PreferredStyle
import polski.preferences.UserPreferencesCodec
import polski.preferences.UserPreferencesV1

class DesktopPreferencesRepositoryTest {
    @Test
    fun legacyMethodMigratesOnceAndV1WinsOnReopen() {
        val directory = Files.createTempDirectory("polski-prefs-")
        try {
            val repository = DesktopPreferencesRepository(directory, { "Situations" })
            val migrated = assertIs<PreferencesLoad.Loaded>(repository.load()).value
            assertEquals(PreferredStyle.SituationFirst, migrated.styleId)
            val file = directory.resolve("preferences-v1.json")
            val firstRaw = Files.readString(file)
            assertEquals(migrated, assertIs<PreferencesLoad.Loaded>(repository.load()).value)
            assertEquals(firstRaw, Files.readString(file))
            val reopened = DesktopPreferencesRepository(directory, { "Logic" })
            assertEquals(PreferredStyle.SituationFirst, assertIs<PreferencesLoad.Loaded>(reopened.load()).value.styleId)
        } finally { directory.toFile().deleteRecursively() }
    }

    @Test
    fun invalidV1RetainsRawAndNeverFallsBackToLegacy() {
        val directory = Files.createTempDirectory("polski-prefs-")
        try {
            val raw = "{\"schemaVersion\":2,\"glassTintPercent\":101}"
            Files.writeString(directory.resolve("preferences-v1.json"), raw)
            val repository = DesktopPreferencesRepository(directory, { "Situations" })
            assertIs<PreferencesLoad.RecoveryRequired>(repository.load())
            assertEquals(raw, repository.recoveryRaw())
            assertIs<PreferencesSave.WriteFailed>(repository.save(UserPreferencesV1()))
            assertEquals(raw, Files.readString(directory.resolve("preferences-v1.json")))
        } finally { directory.toFile().deleteRecursively() }
    }

    @Test
    fun writeAndReadbackFailuresDoNotClaimSaved() {
        val directory = Files.createTempDirectory("polski-prefs-")
        try {
            val repository = DesktopPreferencesRepository(directory, { null }) { _, _ -> }
            assertIs<PreferencesSave.WriteFailed>(repository.save(UserPreferencesV1(appearance = Appearance.Light)))
            assertTrue(Files.notExists(directory.resolve("preferences-v1.json")))
            val migration = DesktopPreferencesRepository(directory, { "Situations" }) { _, _ -> }
            assertIs<PreferencesLoad.Unavailable>(migration.load())
        } finally { directory.toFile().deleteRecursively() }
    }

    @Test
    fun explicitImportValidatesAndBacksUpExistingPreferences() {
        val directory = Files.createTempDirectory("polski-prefs-")
        try {
            val repository = DesktopPreferencesRepository(directory, { null })
            val before = UserPreferencesV1(appearance = Appearance.Dark)
            assertIs<PreferencesSave.Saved>(repository.save(before))
            val oldRaw = UserPreferencesCodec.encode(before)
            assertIs<PreferencesSave.WriteFailed>(repository.importJson("{"))
            assertEquals(oldRaw, Files.readString(directory.resolve("preferences-v1.json")))
            val imported = UserPreferencesV1(styleId = PreferredStyle.SituationFirst)
            assertIs<PreferencesSave.Saved>(repository.importJson(UserPreferencesCodec.encode(imported)))
            assertEquals(imported, assertIs<PreferencesLoad.Loaded>(repository.load()).value)
            val backups = Files.list(directory.resolve("backups")).use { it.toList() }
            assertEquals(1, backups.size)
            assertEquals(oldRaw, Files.readString(backups.single()))
        } finally { directory.toFile().deleteRecursively() }
    }

    @Test
    fun failedImportReadbackRestoresActivePreferencesAndKeepsBackup() {
        val directory = Files.createTempDirectory("polski-prefs-")
        try {
            val current = UserPreferencesV1(appearance = Appearance.Dark)
            val currentRaw = UserPreferencesCodec.encode(current)
            val file = directory.resolve("preferences-v1.json")
            Files.writeString(file, currentRaw)
            val repository = DesktopPreferencesRepository(directory, { null }) { path, raw ->
                Files.createDirectories(path.parent)
                Files.writeString(path, if (path == file) "$raw " else raw)
            }
            val incoming = UserPreferencesCodec.encode(UserPreferencesV1(appearance = Appearance.Light))

            assertIs<PreferencesSave.WriteFailed>(repository.importJson(incoming))
            assertEquals(currentRaw, Files.readString(file))
            assertEquals(current, assertIs<PreferencesLoad.Loaded>(repository.load()).value)
            val backups = Files.list(directory.resolve("backups")).use { it.toList() }
            assertEquals(listOf(currentRaw), backups.map(Files::readString))
        } finally { directory.toFile().deleteRecursively() }
    }

    @Test
    fun oversizedPreferencesImportLeavesActiveFileUntouched() {
        val directory = Files.createTempDirectory("polski-prefs-")
        try {
            val file = directory.resolve("preferences-v1.json")
            val currentRaw = UserPreferencesCodec.encode(UserPreferencesV1(appearance = Appearance.Dark))
            Files.writeString(file, currentRaw)
            val repository = DesktopPreferencesRepository(directory, { null })

            assertIs<PreferencesSave.WriteFailed>(repository.importJson("x".repeat(10_000_001)))
            assertEquals(currentRaw, Files.readString(file))
            assertTrue(Files.notExists(directory.resolve("backups")))
        } finally { directory.toFile().deleteRecursively() }
    }
}
