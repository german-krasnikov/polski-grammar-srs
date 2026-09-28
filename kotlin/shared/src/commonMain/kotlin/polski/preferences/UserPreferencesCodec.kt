package polski.preferences

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import polski.data.packRegistry
import polski.data.usableCourseSelections

sealed interface PreferencesDecode {
    data class Loaded(val value: UserPreferencesV2) : PreferencesDecode
    data class RecoveryRequired(val raw: String, val reason: String) : PreferencesDecode
}

/** Strict v1-v4 wire codec ([encode] still writes v3; v4 is decode-only, EN-08). Unknown data is
 * retained by the caller, never rewritten as defaults. */
object UserPreferencesCodec {
    private val json = Json { isLenient = false }
    private val fieldsV1 = setOf("schemaVersion", "coursePair", "explanationMethod", "answerMode", "appearance", "motion", "swipeRatingEnabled", "reminder")
    private val fieldsV2 = fieldsV1 + "glassTintPercent" + "animationsEnabled"
    private val fieldsV3 = fieldsV2 - "explanationMethod" + "styleId"
    /** EN-08: not yet written by [encode] (no host has two real packs to pick between yet), but
     * decoded tolerantly so a future `target`/`native`-shaped export is never treated as garbage. */
    private val fieldsV4 = fieldsV3 - "coursePair" + "target" + "native"
    private val reminderFields = setOf("enabled", "localTime", "days", "quietStart", "quietEnd")

    /**
     * EN-22: the target/native half of a persisted document, read without checking it against
     * [packRegistry.active] the way [decode] does — a host's cold start needs this *before*
     * calling [polski.data.selectActiveCoursePack] with the result, so [decode]'s own check would
     * otherwise always fail for a persisted pack switch (the active pack is still the old one at
     * that point). Tolerant of both wire shapes (`coursePair` through v3, `target`/`native` from
     * v4); null for any missing/malformed document, same as "leave the default pack active".
     */
    fun peekTargetNative(raw: String): Pair<String, String>? {
        val root = try { json.parseToJsonElement(raw) as? JsonObject } catch (_: Exception) { null } ?: return null
        val version = root.number("schemaVersion") ?: return null
        return if (version <= 3) {
            val joined = (root["coursePair"] as? JsonPrimitive)?.takeIf { it.isString }?.content ?: return null
            val parts = joined.split("-", limit = 2)
            if (parts.size != 2) null else parts[0] to parts[1]
        } else {
            val target = (root["target"] as? JsonPrimitive)?.takeIf { it.isString }?.content ?: return null
            val native = (root["native"] as? JsonPrimitive)?.takeIf { it.isString }?.content ?: return null
            target to native
        }
    }

    fun decode(raw: String): PreferencesDecode {
        val root = try { json.parseToJsonElement(raw) as? JsonObject }
        catch (_: Exception) { null } ?: return invalid(raw, "Expected preferences object")
        val version = root.number("schemaVersion") ?: return invalid(raw, "Invalid schemaVersion")
        if (version !in 1..4) return invalid(raw, "Unsupported schemaVersion: $version")
        val allowedFields = when (version) { 1 -> fieldsV1; 2 -> fieldsV2; 3 -> fieldsV3; else -> fieldsV4 }
        if (root.keys.any { it !in allowedFields }) return invalid(raw, "Unknown preference field")
        val defaults = UserPreferencesV2()
        // v1-v3 wrote the joined `coursePair` (e.g. `"pl-ru"`); v4 writes `target`/`native` directly.
        val (target, native) = if (version <= 3) {
            val joined = root.optionalString("coursePair", "${defaults.target}-${defaults.native}") ?: return invalid(raw, "Invalid coursePair")
            val parts = joined.split("-", limit = 2)
            if (parts.size != 2) return invalid(raw, "Invalid coursePair")
            parts[0] to parts[1]
        } else {
            val t = root.optionalString("target", defaults.target) ?: return invalid(raw, "Invalid target")
            val n = root.optionalString("native", defaults.native) ?: return invalid(raw, "Invalid native")
            t to n
        }
        if (target != packRegistry.active.targetLanguage || native != packRegistry.active.nativeLanguage)
            return invalid(raw, "Unsupported course selection")
        // v1/v2 wrote `explanationMethod: "Logic"/"Situations"`; v3 writes `styleId` directly with all 4 names.
        val style = if (version <= 2) root.legacyStyleId(defaults.styleId) ?: return invalid(raw, "Invalid explanationMethod")
            else root.optionalEnum("styleId", defaults.styleId) ?: return invalid(raw, "Invalid styleId")
        val answer = root.optionalEnum("answerMode", defaults.answerMode) ?: return invalid(raw, "Invalid answerMode")
        val appearance = root.optionalEnum("appearance", defaults.appearance) ?: return invalid(raw, "Invalid appearance")
        val motion = root.optionalEnum("motion", defaults.motion) ?: return invalid(raw, "Invalid motion")
        val swipe = root.optionalBoolean("swipeRatingEnabled", defaults.swipeRatingEnabled) ?: return invalid(raw, "Invalid swipeRatingEnabled")
        val reminder = when (val value = root["reminder"]) {
            null -> defaults.reminder
            is JsonObject -> decodeReminder(value) ?: return invalid(raw, "Invalid reminder")
            else -> return invalid(raw, "Invalid reminder")
        }
        val tint = if (version == 1) defaults.glassTintPercent else
            if ("glassTintPercent" !in root) defaults.glassTintPercent else root.number("glassTintPercent")
        if (tint == null || tint !in 0..100) return invalid(raw, "Invalid glassTintPercent")
        // Missing on decode (v1, or a v2 document saved before this field existed) means enabled.
        val animations = if (version == 1) defaults.animationsEnabled
            else root.optionalBoolean("animationsEnabled", defaults.animationsEnabled) ?: return invalid(raw, "Invalid animationsEnabled")
        return PreferencesDecode.Loaded(UserPreferencesV2(3, target, native, style, answer, appearance, motion, swipe, reminder, tint, animations))
    }

    fun encode(value: UserPreferencesV2): String {
        // EN-22 fix: a real course switch (AndroidSessionViewModel.persistCourseSelectionAndRestart)
        // persists the target/native pair the user is switching *to* — packRegistry.active only
        // catches up on the next cold start (see peekTargetNative's KDoc). Requiring value.target/
        // native to already equal packRegistry.active here made every genuine switch throw and
        // roll back; the invariant this require actually needs to protect is "never persist a pair
        // this build can't safely load", which usableCourseSelections already states precisely.
        // decode() separately checks the persisted pair against packRegistry.active — that's the
        // right place for the "does this match what's active" question, not here.
        require(value.schemaVersion == 3 && (value.target to value.native) in usableCourseSelections && validReminder(value.reminder) && value.glassTintPercent in 0..100)
        val reminder = value.reminder
        val root = JsonObject(mapOf(
            "schemaVersion" to JsonPrimitive(3),
            "coursePair" to JsonPrimitive("${value.target}-${value.native}"),
            "styleId" to JsonPrimitive(value.styleId.name),
            "answerMode" to JsonPrimitive(value.answerMode.name),
            "appearance" to JsonPrimitive(value.appearance.name),
            "motion" to JsonPrimitive(value.motion.name),
            "swipeRatingEnabled" to JsonPrimitive(value.swipeRatingEnabled),
            "glassTintPercent" to JsonPrimitive(value.glassTintPercent),
            "animationsEnabled" to JsonPrimitive(value.animationsEnabled),
            "reminder" to JsonObject(mapOf(
                "enabled" to JsonPrimitive(reminder.enabled),
                "localTime" to JsonPrimitive(reminder.localTime),
                "days" to JsonArray(reminder.days.map(::JsonPrimitive)),
                "quietStart" to JsonPrimitive(reminder.quietStart),
                "quietEnd" to JsonPrimitive(reminder.quietEnd),
            )),
        ))
        return json.encodeToString(JsonElement.serializer(), root)
    }

    private fun decodeReminder(root: JsonObject): ReminderPreferences? {
        if (root.keys.any { it !in reminderFields }) return null
        val defaults = ReminderPreferences()
        val days = when (val value = root["days"]) {
            null -> defaults.days
            is JsonArray -> value.map { (it as? JsonPrimitive)?.takeUnless(JsonPrimitive::isString)?.intOrNull ?: return null }
            else -> return null
        }
        val result = ReminderPreferences(
            enabled = root.optionalBoolean("enabled", defaults.enabled) ?: return null,
            localTime = root.optionalString("localTime", defaults.localTime) ?: return null,
            days = days,
            quietStart = root.optionalString("quietStart", defaults.quietStart) ?: return null,
            quietEnd = root.optionalString("quietEnd", defaults.quietEnd) ?: return null,
        )
        return result.takeIf(::validReminder)
    }

    private fun validReminder(value: ReminderPreferences): Boolean =
        validTime(value.localTime) && validTime(value.quietStart) && validTime(value.quietEnd) &&
            value.days.isNotEmpty() && value.days.distinct().size == value.days.size && value.days.all { it in 1..7 }

    private fun validTime(value: String): Boolean =
        value.length == 5 && value[2] == ':' && value.take(2).toIntOrNull() in 0..23 && value.takeLast(2).toIntOrNull() in 0..59

    private fun JsonObject.number(key: String): Int? = (get(key) as? JsonPrimitive)?.takeUnless(JsonPrimitive::isString)?.intOrNull
    private fun JsonObject.optionalString(key: String, default: String): String? =
        if (key !in this) default else (get(key) as? JsonPrimitive)?.takeIf { it.isString }?.content
    private fun JsonObject.optionalBoolean(key: String, default: Boolean): Boolean? =
        if (key !in this) default else (get(key) as? JsonPrimitive)?.takeUnless(JsonPrimitive::isString)?.booleanOrNull
    private inline fun <reified T : Enum<T>> JsonObject.optionalEnum(key: String, default: T): T? =
        if (key !in this) default else (get(key) as? JsonPrimitive)?.takeIf { it.isString }?.content?.let { wire ->
            enumValues<T>().firstOrNull { it.name == wire }
        }
    /** v1/v2 wrote the 2-value `explanationMethod` enum; UC-10 renamed it to the 4-value [PreferredStyle]. */
    private fun JsonObject.legacyStyleId(default: PreferredStyle): PreferredStyle? =
        if ("explanationMethod" !in this) default else (get("explanationMethod") as? JsonPrimitive)?.takeIf { it.isString }?.content?.let { wire ->
            when (wire) { "Logic" -> PreferredStyle.RuleFirst; "Situations" -> PreferredStyle.SituationFirst; else -> null }
        }
    private fun invalid(raw: String, reason: String) = PreferencesDecode.RecoveryRequired(raw, reason)
}
