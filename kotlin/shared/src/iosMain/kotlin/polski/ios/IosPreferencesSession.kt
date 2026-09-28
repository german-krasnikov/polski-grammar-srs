package polski.ios

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import platform.Foundation.NSUserDefaults
import polski.data.activeCoursePackId
import polski.data.availableCoursePacks
import polski.data.selectActiveCoursePack
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
import polski.presentation.builtInStyleIds
import polski.presentation.legacyStyleWireValue
import polski.presentation.toLegacyWireValue

/**
 * Native Settings bridge. The Apple material setting is owned by the OS; portable tint is
 * retained in JSON. EN-22: target/native pickers offer and switch only
 * [availableCoursePacks] (packs whose content fully parses), the same gate Android/macOS use.
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
    private var lastPackSwitchWarning: String? = null

    /**
     * EN-22: re-applies whatever target/native was saved from a previous run — must be called by
     * the host **before** this instance's [onState] is assigned (that setter eagerly calls
     * [currentSnapshot], which runs [reconcileWithActivePack]; calling it first would see the
     * still-default active pack, conclude the persisted choice failed, and silently revert it
     * before this method ever ran — the exact false-green bug EnRuAcceptance-2026-09-28.md §7
     * item 2's iOS fix traces to `PolskiGrammarApp.swift`'s init ordering). A saved pair that is
     * not (or no longer) usable is left alone rather than crashing launch ([selectActiveCoursePack]
     * is a no-op then).
     */
    fun reapplySavedCoursePack() {
        (loaded as? PreferencesDecode.Loaded)?.value?.let { prefs ->
            val pairId = "${prefs.target}-${prefs.native}"
            selectActiveCoursePack(pairId)
        }
    }

    var onState: ((String) -> Unit)? = null
        set(value) { field = value; value?.invoke(currentSnapshot()) }

    fun currentSnapshot(): String {
        reconcileWithActivePack()
        val json = buildJsonObject {
            put("schemaVersion", 2)
            // StylesBlueprint.md §2/S1: recipe label/description are per-language data (empty in
            // CORE until pl-ru content merges) — Swift falls back to its own copy when a value is "".
            put("styles", JsonArray(builtInStyleIds.map { id ->
                val recipe = StyleRegistry.recipes[id]
                buildJsonObject {
                    put("id", id.value)
                    put("label", recipe?.label?.get("ru") ?: "")
                    put("description", recipe?.description?.get("ru") ?: "")
                }
            }))
            // EN-22: every usable pack's own (target, native) — the real option list for the
            // Settings target/native pickers, never a hardcoded pl/en literal pair.
            put("coursePacks", JsonArray(availableCoursePacks.map { pack ->
                buildJsonObject { put("target", pack.target); put("native", pack.native) }
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
                    lastPackSwitchWarning?.let { put("packSwitchWarning", it) }
                }
                is PreferencesDecode.RecoveryRequired -> {
                    put("status", "RecoveryRequired")
                    put("error", result.reason)
                }
            }
        }.toString()
        lastPackSwitchWarning = null
        return json
    }

    /**
     * ADR-37 correction (EnRuAcceptance §7 item 2, blocker 2): mirrors
     * [polski.macos.MacPreferencesSession.reconcileWithActivePack] — see its KDoc. A pack switch
     * [set] accepted (past its own [availableCoursePacks] parses-only check) can still be rolled
     * back later by [IosSession.rebuildIfCourseSwitched] on a different bridge instance, straight
     * against the shared [polski.data.packRegistry]; this bridge's own persisted [loaded] never
     * hears about it otherwise. Writes directly to [defaults] (not through [write], which would
     * recurse back into [currentSnapshot]).
     */
    private fun reconcileWithActivePack() {
        val prefs = (loaded as? PreferencesDecode.Loaded)?.value ?: return
        val requestedPairId = "${prefs.target}-${prefs.native}"
        if (requestedPairId == activeCoursePackId) return
        val actual = availableCoursePacks.firstOrNull { it.pairId == activeCoursePackId } ?: return
        val corrected = prefs.copy(target = actual.target, native = actual.native)
        val raw = UserPreferencesCodec.encode(corrected)
        try {
            defaults.setObject(raw, forKey = key)
            check(defaults.synchronize() && defaults.stringForKey(key) == raw)
            loaded = PreferencesDecode.Loaded(corrected)
            lastPackSwitchWarning = "Пакет «$requestedPairId» пока не может обучать — вернулись к «${actual.pairId}»"
        } catch (_: Throwable) { /* leave loaded/defaults as they were; next snapshot retries */ }
    }

    fun exportJson(): String? = defaults.stringForKey(key)
        ?: (loaded as? PreferencesDecode.Loaded)?.value?.let(UserPreferencesCodec::encode)

    fun set(field: String, value: String): String? {
        val current = (loaded as? PreferencesDecode.Loaded)?.value ?: return "Настройки требуют восстановления"
        val next = when (field) {
            "method" -> current.copy(styleId = legacyStyleWireValue(value)?.let { PreferredStyle.valueOf(it.value) } ?: return "Неизвестный метод")
            "styleId" -> current.copy(styleId = PreferredStyle.entries.firstOrNull { it.name == value } ?: return "Неизвестный стиль")
            // EN-22: target/native together must name a usable pack ([availableCoursePacks]) before
            // anything is written — an unpaired combination (e.g. this target with the old native)
            // is rejected with the pairId that was tried, the same explicit-error shape every other
            // field here already uses, rather than silently keeping the old pack active.
            "target" -> current.copy(target = value).also {
                if (availableCoursePacks.none { pack -> pack.pairId == "${it.target}-${it.native}" }) return "Неизвестная пара языков: ${it.target}-${it.native}"
            }
            "native" -> current.copy(native = value).also {
                if (availableCoursePacks.none { pack -> pack.pairId == "${it.target}-${it.native}" }) return "Неизвестная пара языков: ${it.target}-${it.native}"
            }
            "answerMode" -> current.copy(answerMode = PreferredAnswerMode.entries.firstOrNull { it.name == value } ?: return "Неизвестный способ ответа")
            "appearance" -> current.copy(appearance = Appearance.entries.firstOrNull { it.name == value } ?: return "Неизвестная тема")
            "motion" -> current.copy(motion = Motion.entries.firstOrNull { it.name == value } ?: return "Неизвестное движение")
            "animationsEnabled" -> current.copy(animationsEnabled = value.toBooleanStrictOrNull() ?: return "Неверное значение")
            else -> return "Неизвестная настройка"
        }
        if (field == "target" || field == "native") selectCoursePack("${next.target}-${next.native}")
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
        // EN-22: an imported document's own target/native becomes the live active pack too, when usable.
        selectActiveCoursePack("${next.target}-${next.native}")
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
