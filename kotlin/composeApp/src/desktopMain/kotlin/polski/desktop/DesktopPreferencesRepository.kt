package polski.desktop

import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.prefs.Preferences
import polski.preferences.PreferencesDecode
import polski.preferences.PreferencesLoad
import polski.preferences.PreferencesSave
import polski.preferences.PreferredMethod
import polski.preferences.UserPreferencesCodec
import polski.preferences.UserPreferencesRepository
import polski.preferences.UserPreferencesV1

/** A separate, versioned macOS document. Existing unreadable bytes are never silently replaced. */
internal class DesktopPreferencesRepository(
    private val directory: Path,
    private val legacyMethod: () -> String? = {
        Preferences.userRoot().node("dev/polski/grammarmatrix").get("explanationMethod", null)
    },
    private val atomicWrite: (Path, String) -> Unit = ::writeDesktopJsonAtomically,
) : UserPreferencesRepository {
    private val file = directory.resolve("preferences-v1.json")
    private var recoverableRaw: String? = null

    override fun load(): PreferencesLoad = try {
        if (Files.exists(file)) {
            val raw = readLimited(file)
            when (val decoded = UserPreferencesCodec.decode(raw)) {
                is PreferencesDecode.Loaded -> {
                    recoverableRaw = null
                    PreferencesLoad.Loaded(decoded.value)
                }
                is PreferencesDecode.RecoveryRequired -> {
                    recoverableRaw = raw
                    PreferencesLoad.RecoveryRequired(raw, decoded.reason)
                }
            }
        } else {
            val oldMethod = legacyMethod() ?: return PreferencesLoad.Missing
            val migrated = UserPreferencesV1(explanationMethod =
                if (oldMethod == "Situations") PreferredMethod.Situations else PreferredMethod.Logic)
            when (val saved = save(migrated)) {
                PreferencesSave.Saved -> PreferencesLoad.Loaded(migrated)
                is PreferencesSave.WriteFailed -> PreferencesLoad.Unavailable(saved.reason)
            }
        }
    } catch (error: Exception) {
        PreferencesLoad.Unavailable(error.message ?: "Unable to read preferences")
    }

    override fun save(value: UserPreferencesV1): PreferencesSave = try {
        val current = if (Files.exists(file)) readLimited(file) else null
        if (current != null && UserPreferencesCodec.decode(current) !is PreferencesDecode.Loaded) {
            return PreferencesSave.WriteFailed("Existing preferences need recovery")
        }
        val raw = UserPreferencesCodec.encode(value)
        replaceAndVerify(raw, current)
        recoverableRaw = null
        PreferencesSave.Saved
    } catch (error: Exception) {
        PreferencesSave.WriteFailed(error.message ?: "Unable to save preferences")
    }

    /** Explicit import validates before replacing, backs up the old raw, and verifies the new bytes. */
    fun importJson(raw: String): PreferencesSave {
        if (raw.encodeToByteArray().size > MAX_BYTES) return PreferencesSave.WriteFailed("Preferences file is too large")
        val decoded = UserPreferencesCodec.decode(raw)
        if (decoded is PreferencesDecode.RecoveryRequired) return PreferencesSave.WriteFailed(decoded.reason)
        return try {
            val current = if (Files.exists(file)) readLimited(file) else null
            if (current != null) {
                val backup = directory.resolve("backups").resolve("preferences-${UUID.randomUUID()}.json")
                atomicWrite(backup, current)
                check(Files.readString(backup) == current) { "Preferences backup read-back mismatch" }
            }
            replaceAndVerify(raw, current)
            recoverableRaw = null
            PreferencesSave.Saved
        } catch (error: Exception) {
            PreferencesSave.WriteFailed(error.message ?: "Unable to import preferences")
        }
    }

    override fun recoveryRaw(): String? = recoverableRaw

    private fun readLimited(path: Path): String {
        require(Files.size(path) <= MAX_BYTES) { "Preferences file is too large" }
        return Files.readString(path)
    }

    private fun replaceAndVerify(raw: String, previous: String?) {
        try {
            atomicWrite(file, raw)
            check(Files.readString(file) == raw) { "Preferences read-back mismatch" }
        } catch (error: Exception) {
            // Atomic replacement protects normal write failures; restore if read-back found a mismatch.
            if (previous != null && Files.exists(file) && runCatching { Files.readString(file) }.getOrNull() != previous) {
                writeDesktopJsonAtomically(file, previous)
                check(Files.readString(file) == previous) { "Preferences rollback failed" }
            } else if (previous == null && Files.exists(file)) {
                Files.delete(file)
            }
            throw error
        }
    }

    private companion object { const val MAX_BYTES = 10_000_000L }
}
