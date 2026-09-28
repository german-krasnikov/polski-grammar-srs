package polski.macos

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import polski.data.availableCoursePacks
import polski.data.selectCoursePack
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

    init { syncActivePack() }

    fun currentSnapshot(): String = buildJsonObject {
        put("schemaVersion", 2)
        put("styles", styleCatalogJson())
        put("packs", packOptionsJson())
        when (val result = loaded) {
            is PreferencesDecode.Loaded -> {
                put("status", "Ready")
                put("target", result.value.target)
                put("native", result.value.native)
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
            "target" -> current.copy(target = value.takeIf { candidate -> availableCoursePacks.any { it.target == candidate } } ?: return "Неизвестный изучаемый язык")
            "native" -> current.copy(native = value.takeIf { candidate -> availableCoursePacks.any { it.native == candidate } } ?: return "Неизвестный родной язык")
            "styleId" -> current.copy(styleId = PreferredStyle.entries.firstOrNull { it.name == value } ?: return "Неизвестный стиль")
            "answerMode" -> current.copy(answerMode = PreferredAnswerMode.entries.firstOrNull { it.name == value } ?: return "Неизвестный способ ответа")
            "appearance" -> current.copy(appearance = Appearance.entries.firstOrNull { it.name == value } ?: return "Неизвестная тема")
            "motion" -> current.copy(motion = Motion.entries.firstOrNull { it.name == value } ?: return "Неизвестное движение")
            "glassTintPercent" -> current.copy(glassTintPercent = value.toIntOrNull()?.takeIf { it in 0..100 } ?: return "Недопустимая плотность стекла")
            "animationsEnabled" -> current.copy(animationsEnabled = value.toBooleanStrictOrNull() ?: return "Недопустимое значение анимаций")
            else -> return "Неизвестная настройка"
        }
        val error = repository.save(next)
        if (error == null) { loaded = PreferencesDecode.Loaded(next); syncActivePack(); onState?.invoke(currentSnapshot()) }
        return error
    }

    /**
     * EN-22: makes `target`/`native` a real pack switch, not just a saved preference — a
     * `pairId` [availableCoursePacks] doesn't have (only pl-ru is registered today; see
     * `polski.data.packRegistry`'s own KDoc) is left as the still-active pack rather than thrown,
     * since this runs on every load/save, not only on a picker change.
     */
    private fun syncActivePack() {
        val prefs = (loaded as? PreferencesDecode.Loaded)?.value ?: return
        val pairId = "${prefs.target}-${prefs.native}"
        if (availableCoursePacks.any { it.pairId == pairId }) selectCoursePack(pairId)
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

private fun styleCatalogJson(): JsonArray = JsonArray(styleFallbackCopy.keys.map { id ->
    val recipe = StyleRegistry.recipes[id]
    val fallback = styleFallbackCopy.getValue(id)
    buildJsonObject {
        put("id", id.value)
        put("label", recipe?.label?.get("ru")?.takeIf { it.isNotBlank() } ?: fallback.first)
        put("description", recipe?.description?.get("ru")?.takeIf { it.isNotBlank() } ?: fallback.second)
    }
})

/** EN-22: a language code's Russian display name for the target/native pickers — every code
 *  [polski.data.availableCoursePacks] can report today or once a second pack is wired in. */
private val languageDisplayNames: Map<String, String> = mapOf("pl" to "Польский", "en" to "Английский", "ru" to "Русский")

private fun packOptionsJson(): JsonArray = JsonArray(availableCoursePacks.map { pack ->
    buildJsonObject {
        put("pairId", pack.pairId)
        put("target", pack.target)
        put("native", pack.native)
        put("targetLabel", languageDisplayNames[pack.target] ?: pack.target)
        put("nativeLabel", languageDisplayNames[pack.native] ?: pack.native)
    }
})
