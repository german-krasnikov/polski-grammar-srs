package polski.preferences

sealed interface PreferencesLoad {
    data class Loaded(val value: UserPreferencesV2) : PreferencesLoad
    data object Missing : PreferencesLoad
    data class RecoveryRequired(val raw: String, val reason: String) : PreferencesLoad
    data class Unavailable(val reason: String) : PreferencesLoad
}

sealed interface PreferencesSave {
    data object Saved : PreferencesSave
    data class WriteFailed(val reason: String) : PreferencesSave
}

interface UserPreferencesRepository {
    fun load(): PreferencesLoad
    fun save(value: UserPreferencesV2): PreferencesSave
    fun recoveryRaw(): String?
}
