package dev.polski.grammarmatrix

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import polski.preferences.PreferencesDecode
import polski.preferences.PreferencesLoad
import polski.preferences.PreferencesSave
import polski.preferences.PreferredMethod
import polski.preferences.UserPreferencesCodec
import polski.preferences.UserPreferencesV2

/** Android storage for the portable preferences document; old method-only data migrates on first load. */
internal class AndroidUserPreferencesStore(context: Context) {
    private val storage = context.applicationContext.getSharedPreferences("polski-preferences", Context.MODE_PRIVATE)
    private val lock = Mutex()
    private var invalidRaw: String? = null

    suspend fun load(): PreferencesLoad = withContext(Dispatchers.IO) {
        lock.withLock { loadUnlocked() }
    }

    suspend fun save(value: UserPreferencesV2): PreferencesSave = withContext(Dispatchers.IO) {
        lock.withLock { saveUnlocked(value) }
    }

    private fun loadUnlocked(): PreferencesLoad {
        val raw = storage.getString("document", null)
        if (raw != null) return when (val decoded = UserPreferencesCodec.decode(raw)) {
            is PreferencesDecode.Loaded -> PreferencesLoad.Loaded(decoded.value)
            is PreferencesDecode.RecoveryRequired -> {
                invalidRaw = raw
                PreferencesLoad.RecoveryRequired(raw, decoded.reason)
            }
        }
        val legacy = storage.getString("explanationMethod", null)
        val initial = UserPreferencesV2(
            explanationMethod = if (legacy == "Situations") PreferredMethod.Situations else PreferredMethod.Logic,
        )
        return when (val saved = saveUnlocked(initial)) {
            PreferencesSave.Saved -> PreferencesLoad.Loaded(initial)
            is PreferencesSave.WriteFailed -> PreferencesLoad.Unavailable(saved.reason)
        }
    }

    private fun saveUnlocked(value: UserPreferencesV2): PreferencesSave {
        if (invalidRaw != null) return PreferencesSave.WriteFailed("Исходный JSON настроек требует восстановления")
        val raw = try { UserPreferencesCodec.encode(value) }
        catch (error: IllegalArgumentException) { return PreferencesSave.WriteFailed(error.message ?: "Недопустимая настройка") }
        return if (storage.edit().putString("document", raw).commit() && storage.getString("document", null) == raw) {
            PreferencesSave.Saved
        } else PreferencesSave.WriteFailed("Не удалось сохранить настройки")
    }

    fun recoveryRaw(): String? = invalidRaw
}
