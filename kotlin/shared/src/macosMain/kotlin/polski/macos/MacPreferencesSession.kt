package polski.macos

import kotlinx.serialization.json.JsonArray
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
import polski.presentation.StyleRegistry

/** Small synchronous bridge for native Settings and preferences Files import/export. */
class MacPreferencesSession(directory: String) {
    private val repository = MacPreferencesRepository(directory)
    private var loaded = repository.load()
    var onState: ((String) -> Unit)? = null
        set(value) { field = value; value?.invoke(currentSnapshot()) }

    fun currentSnapshot(): String = buildJsonObject {
        put("schemaVersion", 2)
        put("styles", styleCatalogJson())
        when (val result = loaded) {
            is PreferencesDecode.Loaded -> {
                put("status", "Ready")
                put("styleId", result.value.styleId.name)
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
            "styleId" -> current.copy(styleId = PreferredStyle.entries.firstOrNull { it.name == value } ?: return "Неизвестный стиль")
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

/**
 * macOS-host Russian copy for the 4 styles, used only where [StyleRegistry]'s own [label]/
 * [description] are blank — CONTENT authoring these per UC-10/StylesBlueprint.md §2 (a parallel
 * worktree) replaces this fallback automatically once merged, with no bridge change needed.
 */
private val styleFallbackCopy: Map<StyleId, Pair<String, String>> = mapOf(
    StyleId.RuleFirst to ("Схемы и правила" to "Формула и таблица окончаний, затем правило и разбор."),
    StyleId.SituationFirst to ("Через ситуацию" to "Короткая сцена вместо схемы — разбор такой же, как обычно."),
    StyleId.NativeContrast to ("Через сравнение с русским" to "По-русски так → по-польски так — где совпадает и где расходится."),
    StyleId.MinimalTheory to ("Минимум теории" to "Только примеры; объяснение — по запросу."),
)

private fun styleCatalogJson(): JsonArray = JsonArray(StyleId.entries.map { id ->
    val recipe = StyleRegistry.recipes[id]
    val fallback = styleFallbackCopy.getValue(id)
    buildJsonObject {
        put("id", id.name)
        put("label", recipe?.label?.get("ru")?.takeIf { it.isNotBlank() } ?: fallback.first)
        put("description", recipe?.description?.get("ru")?.takeIf { it.isNotBlank() } ?: fallback.second)
    }
})
