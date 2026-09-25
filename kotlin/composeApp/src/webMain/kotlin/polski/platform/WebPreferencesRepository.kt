package polski.platform

import kotlinx.browser.window
import polski.preferences.PreferencesDecode
import polski.preferences.PreferencesLoad
import polski.preferences.PreferencesSave
import polski.preferences.PreferredMethod
import polski.preferences.UserPreferencesCodec
import polski.preferences.UserPreferencesRepository
import polski.preferences.UserPreferencesV2

internal const val WEB_PREFERENCES_KEY = "polski-preferences-v1"
private const val LEGACY_METHOD_KEY = "polski-explanation-method-v1"
private const val BACKUP_KEY = "polski-preferences-v1-backup"

/** The legacy method key is read only; v1 always wins, including when it needs recovery. */
internal class WebPreferencesRepository : UserPreferencesRepository {
    private var rawForRecovery: String? = null

    override fun load(): PreferencesLoad {
        val raw = try { window.localStorage.getItem(WEB_PREFERENCES_KEY) }
        catch (error: Throwable) { return PreferencesLoad.Unavailable(error.message ?: "Хранилище настроек недоступно") }
        if (raw != null) return when (val decoded = UserPreferencesCodec.decode(raw)) {
            is PreferencesDecode.Loaded -> PreferencesLoad.Loaded(decoded.value)
            is PreferencesDecode.RecoveryRequired -> {
                rawForRecovery = raw
                PreferencesLoad.RecoveryRequired(raw, decoded.reason)
            }
        }
        val legacy = try { window.localStorage.getItem(LEGACY_METHOD_KEY) }
        catch (error: Throwable) { return PreferencesLoad.Unavailable(error.message ?: "Хранилище настроек недоступно") }
        if (legacy == null) return PreferencesLoad.Missing
        val migrated = UserPreferencesV2(explanationMethod = if (legacy == "situations") PreferredMethod.Situations else PreferredMethod.Logic)
        return when (save(migrated)) {
            PreferencesSave.Saved -> PreferencesLoad.Loaded(migrated)
            is PreferencesSave.WriteFailed -> PreferencesLoad.Unavailable("Не удалось перенести настройки; прежний ключ сохранён")
        }
    }

    override fun save(value: UserPreferencesV2): PreferencesSave {
        if (rawForRecovery != null) return PreferencesSave.WriteFailed("Сначала сохраните и восстановите исходный JSON настроек")
        val raw = UserPreferencesCodec.encode(value)
        var previous: String? = null
        var activeWriteAttempted = false
        return try {
            previous = window.localStorage.getItem(WEB_PREFERENCES_KEY)
            if (previous != null && window.localStorage.getItem(BACKUP_KEY) == null) {
                window.localStorage.setItem(BACKUP_KEY, previous)
                if (window.localStorage.getItem(BACKUP_KEY) != previous) return PreferencesSave.WriteFailed("Резервная копия настроек не подтверждена")
            }
            activeWriteAttempted = true
            window.localStorage.setItem(WEB_PREFERENCES_KEY, raw)
            if (window.localStorage.getItem(WEB_PREFERENCES_KEY) != raw) {
                restore(previous)
                PreferencesSave.WriteFailed("Настройки не прошли проверку записи")
            }
            else PreferencesSave.Saved
        } catch (error: Throwable) {
            if (activeWriteAttempted) runCatching { restore(previous) }
            PreferencesSave.WriteFailed(error.message ?: "Не удалось сохранить настройки")
        }
    }

    override fun recoveryRaw(): String? = rawForRecovery

    fun import(raw: String): PreferencesSave {
        val decoded = UserPreferencesCodec.decode(raw)
        if (decoded !is PreferencesDecode.Loaded) return PreferencesSave.WriteFailed((decoded as PreferencesDecode.RecoveryRequired).reason)
        var old: String? = null
        var activeWriteAttempted = false
        return try {
            old = window.localStorage.getItem(WEB_PREFERENCES_KEY)
            if (old != null) {
                window.localStorage.setItem(BACKUP_KEY, old)
                if (window.localStorage.getItem(BACKUP_KEY) != old) return PreferencesSave.WriteFailed("Резервная копия не подтверждена")
            }
            activeWriteAttempted = true
            window.localStorage.setItem(WEB_PREFERENCES_KEY, raw)
            if (window.localStorage.getItem(WEB_PREFERENCES_KEY) != raw) {
                restore(old)
                return PreferencesSave.WriteFailed("Импорт не прошёл проверку записи")
            }
            rawForRecovery = null
            PreferencesSave.Saved
        } catch (error: Throwable) {
            if (activeWriteAttempted) runCatching { restore(old) }
            PreferencesSave.WriteFailed(error.message ?: "Импорт не удался")
        }
    }

    private fun restore(raw: String?) {
        if (raw == null) window.localStorage.removeItem(WEB_PREFERENCES_KEY)
        else window.localStorage.setItem(WEB_PREFERENCES_KEY, raw)
    }
}
