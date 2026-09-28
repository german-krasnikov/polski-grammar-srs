package polski.ios

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import platform.Foundation.NSUserDefaults
import polski.data.packRegistry
import polski.preferences.Appearance
import polski.preferences.Motion
import polski.preferences.PreferencesDecode
import polski.preferences.PreferredAnswerMode
import polski.preferences.PreferredStyle
import polski.preferences.UserPreferencesCodec
import polski.preferences.UserPreferencesV2
import polski.presentation.StyleId
import polski.presentation.StyleRegistry
import polski.presentation.builtInStyleIds
import polski.presentation.legacyStyleWireValue
import polski.presentation.toLegacyWireValue

/**
 * Native Settings bridge. The Apple material setting is owned by the OS; portable tint is
 * retained in JSON. Reads/switches the real production [polski.data.packRegistry] directly
 * (EN-22) — [UserPreferencesCodec] itself is already wired to that same singleton (`decode`/
 * `encode` both call [polski.data.PackRegistry.contains]), so there is no separate registry a
 * session could sensibly be given instead.
 */
class IosPreferencesSession(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) {
    private val key = "polski-preferences-v2"
    private val backupKey = "polski-preferences-import-backup-latest"
    private var loaded: PreferencesDecode = defaults.stringForKey(key)?.let(UserPreferencesCodec::decode)
        ?: PreferencesDecode.Loaded(UserPreferencesV2(
            styleId = if (defaults.stringForKey("explanationMethod") == "Situations")
                PreferredStyle.SituationFirst else PreferredStyle.RuleFirst,
        ))

    /**
     * EN-22: re-applies whatever target/native was saved from a previous run — called once, by
     * the host, strictly *after* its own training session has already produced its first
     * snapshot. That ordering matters: this app's every `packRegistry.active`-derived value
     * (nouns, sentence seeds, exercise copy, reference rows, …) is a top-level `by lazy` that
     * caches whatever pack was active the first time anything reads it, and a still-content-thin
     * pack (en-ru's `reference` is deliberately empty — no en-ru reference screens are authored
     * yet) can throw the moment one of those is forced against it. Calling this from the class's
     * own constructor — before the host's training session exists at all — would let a
     * *previously* saved non-pl-ru selection make that very first, unrelated snapshot crash on
     * every cold start. Called this late instead, pl-ru's own values are already permanently
     * cached by the time this runs, so it can never retroactively break them; a saved pairId
     * that is no longer registered is left alone rather than crashing launch (`select` throws on
     * it, guarded by `contains`).
     */
    fun reapplySavedCoursePack() {
        (loaded as? PreferencesDecode.Loaded)?.value?.let { prefs ->
            val pairId = "${prefs.target}-${prefs.native}"
            if (packRegistry.contains(pairId)) packRegistry.select(pairId)
        }
    }

    var onState: ((String) -> Unit)? = null
        set(value) { field = value; value?.invoke(currentSnapshot()) }

    fun currentSnapshot(): String = buildJsonObject {
        put("schemaVersion", 2)
        // StylesBlueprint.md §2/S1: recipe label/description are per-language data (empty in CORE
        // until pl-ru content merges) — Swift falls back to its own copy when a value is "".
        put("styles", JsonArray(builtInStyleIds.map { id ->
            val recipe = StyleRegistry.recipes[id]
            buildJsonObject {
                put("id", id.value)
                put("label", recipe?.label?.get("ru") ?: "")
                put("description", recipe?.description?.get("ru") ?: "")
            }
        }))
        // EN-22: every registered pack's own (target, native) — the real option list for the
        // Settings target/native pickers, never a hardcoded pl/en literal pair.
        put("coursePacks", JsonArray(packRegistry.options.map { (target, native) ->
            buildJsonObject { put("target", target); put("native", native) }
        }))
        when (val result = loaded) {
            is PreferencesDecode.Loaded -> {
                put("status", "Ready")
                // PreferredStyle's `.name` (preferences enum) and StyleId's `.value` share the
                // same PascalCase wire vocabulary for the 4 built-in styles by construction.
                put("method", StyleId(result.value.styleId.name).toLegacyWireValue())
                put("styleId", result.value.styleId.name)
                put("target", result.value.target)
                put("native", result.value.native)
                put("answerMode", result.value.answerMode.name)
                put("appearance", result.value.appearance.name)
                put("motion", result.value.motion.name)
                put("animationsEnabled", result.value.animationsEnabled)
            }
            is PreferencesDecode.RecoveryRequired -> {
                put("status", "RecoveryRequired")
                put("error", result.reason)
            }
        }
    }.toString()

    fun exportJson(): String? = defaults.stringForKey(key)
        ?: (loaded as? PreferencesDecode.Loaded)?.value?.let(UserPreferencesCodec::encode)

    fun set(field: String, value: String): String? {
        val current = (loaded as? PreferencesDecode.Loaded)?.value ?: return "Настройки требуют восстановления"
        val next = when (field) {
            "method" -> current.copy(styleId = legacyStyleWireValue(value)?.let { PreferredStyle.valueOf(it.value) } ?: return "Неизвестный метод")
            "styleId" -> current.copy(styleId = PreferredStyle.entries.firstOrNull { it.name == value } ?: return "Неизвестный стиль")
            // EN-22: target/native together must name a registered pack (`packRegistry.contains`) before
            // anything is written — an unpaired combination (e.g. this target with the old native)
            // is rejected with the pairId that was tried, the same explicit-error shape every other
            // field here already uses, rather than silently keeping the old pack active.
            "target" -> current.copy(target = value).also {
                if (!packRegistry.contains("${it.target}-${it.native}")) return "Неизвестная пара языков: ${it.target}-${it.native}"
            }
            "native" -> current.copy(native = value).also {
                if (!packRegistry.contains("${it.target}-${it.native}")) return "Неизвестная пара языков: ${it.target}-${it.native}"
            }
            "answerMode" -> current.copy(answerMode = PreferredAnswerMode.entries.firstOrNull { it.name == value } ?: return "Неизвестный способ ответа")
            "appearance" -> current.copy(appearance = Appearance.entries.firstOrNull { it.name == value } ?: return "Неизвестная тема")
            "motion" -> current.copy(motion = Motion.entries.firstOrNull { it.name == value } ?: return "Неизвестное движение")
            "animationsEnabled" -> current.copy(animationsEnabled = value.toBooleanStrictOrNull() ?: return "Неверное значение")
            else -> return "Неизвестная настройка"
        }
        if (field == "target" || field == "native") packRegistry.select("${next.target}-${next.native}")
        return write(UserPreferencesCodec.encode(next), next)
    }

    fun importJson(raw: String): String? {
        if (raw.encodeToByteArray().size > 1_000_000) return "Файл настроек слишком большой"
        val next = when (val decoded = UserPreferencesCodec.decode(raw)) {
            is PreferencesDecode.Loaded -> decoded.value
            is PreferencesDecode.RecoveryRequired -> return decoded.reason
        }
        val old = defaults.stringForKey(key)
        if (old != null) {
            // Only the latest pre-import document is kept as a backup.
            defaults.setObject(old, forKey = backupKey)
            if (!defaults.synchronize() || defaults.stringForKey(backupKey) != old) return "Не удалось сохранить резервную копию"
        }
        // EN-22: an imported document's own target/native becomes the live active pack too — decode
        // already proved it's registered (`packRegistry.contains`), the same guard `set` uses above.
        packRegistry.select("${next.target}-${next.native}")
        return write(raw, next)
    }

    private fun write(raw: String, next: UserPreferencesV2): String? = try {
        defaults.setObject(raw, forKey = key)
        check(defaults.synchronize() && defaults.stringForKey(key) == raw) { "Не удалось сохранить настройки" }
        loaded = PreferencesDecode.Loaded(next)
        onState?.invoke(currentSnapshot())
        null
    } catch (error: Throwable) { error.message ?: "Не удалось сохранить настройки" }
}
