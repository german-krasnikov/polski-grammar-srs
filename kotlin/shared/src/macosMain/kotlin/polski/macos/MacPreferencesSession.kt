package polski.macos

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import polski.preferences.Appearance
import polski.preferences.Motion
import polski.preferences.PreferencesDecode
import polski.preferences.PreferredAnswerMode
import polski.preferences.PreferredStyle
import polski.preferences.UserPreferencesCodec
import polski.preferences.UserPreferencesV2
import polski.presentation.StyleId
import polski.presentation.legacyStyleWireValue
import polski.presentation.toLegacyWireValue

/** Small synchronous bridge for native Settings and preferences Files import/export. */
class MacPreferencesSession(directory: String) {
    private val repository = MacPreferencesRepository(directory)
    private var loaded = repository.load()
    var onState: ((String) -> Unit)? = null
        set(value) { field = value; value?.invoke(currentSnapshot()) }

    fun currentSnapshot(): String = buildJsonObject {
        put("schemaVersion", 2)
        when (val result = loaded) {
            is PreferencesDecode.Loaded -> {
                put("status", "Ready")
                put("method", StyleId.valueOf(result.value.styleId.name).toLegacyWireValue())
                put("answerMode", result.value.answerMode.name)
                put("appearance", result.value.appearance.name)
                put("motion", result.value.motion.name)
                put("glassTintPercent", result.value.glassTintPercent)
                put("animationsEnabled", result.value.animationsEnabled)
            }
            is PreferencesDecode.RecoveryRequired -> {
                put("status", "RecoveryRequired")
                put("error", result.reason)
            }
        }
    }.toString()

    fun exportJson(): String? = repository.raw() ?: (loaded as? PreferencesDecode.Loaded)?.value?.let(UserPreferencesCodec::encode)
    fun recoveryRaw(): String? = repository.raw()

    fun set(field: String, value: String): String? {
        val current = (loaded as? PreferencesDecode.Loaded)?.value ?: return "Настройки требуют восстановления"
        val next: UserPreferencesV2 = when (field) {
            "method" -> current.copy(styleId = legacyStyleWireValue(value)?.let { PreferredStyle.valueOf(it.name) } ?: return "Неизвестный метод")
            "answerMode" -> current.copy(answerMode = PreferredAnswerMode.entries.firstOrNull { it.name == value } ?: return "Неизвестный способ ответа")
            "appearance" -> current.copy(appearance = Appearance.entries.firstOrNull { it.name == value } ?: return "Неизвестная тема")
            "motion" -> current.copy(motion = Motion.entries.firstOrNull { it.name == value } ?: return "Неизвестное движение")
            "glassTintPercent" -> current.copy(glassTintPercent = value.toIntOrNull()?.takeIf { it in 0..100 } ?: return "Недопустимая плотность стекла")
            "animationsEnabled" -> current.copy(animationsEnabled = value.toBooleanStrictOrNull() ?: return "Недопустимое значение анимаций")
            else -> return "Неизвестная настройка"
        }
        val error = repository.save(next)
        if (error == null) { loaded = PreferencesDecode.Loaded(next); onState?.invoke(currentSnapshot()) }
        return error
    }

    fun importJson(raw: String): String? {
        val error = repository.importJson(raw)
        if (error == null) { loaded = repository.load(); onState?.invoke(currentSnapshot()) }
        return error
    }
}
