package polski.preferences

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull

sealed interface PreferencesDecode {
    data class Loaded(val value: UserPreferencesV2) : PreferencesDecode
    data class RecoveryRequired(val raw: String, val reason: String) : PreferencesDecode
}

/** Strict v1/v2 wire codec. Unknown data is retained by the caller, never rewritten as defaults. */
object UserPreferencesCodec {
    private val json = Json { isLenient = false }
    private val fieldsV1 = setOf("schemaVersion", "coursePair", "explanationMethod", "answerMode", "appearance", "motion", "swipeRatingEnabled", "reminder")
    private val fieldsV2 = fieldsV1 + "glassTintPercent"
    private val reminderFields = setOf("enabled", "localTime", "days", "quietStart", "quietEnd")

    fun decode(raw: String): PreferencesDecode {
        val root = try { json.parseToJsonElement(raw) as? JsonObject }
        catch (_: Exception) { null } ?: return invalid(raw, "Expected preferences object")
        val version = root.number("schemaVersion") ?: return invalid(raw, "Invalid schemaVersion")
        if (version !in 1..2) return invalid(raw, "Unsupported schemaVersion: $version")
        if (root.keys.any { it !in (if (version == 1) fieldsV1 else fieldsV2) }) return invalid(raw, "Unknown preference field")
        val defaults = UserPreferencesV2()
        val pair = root.optionalString("coursePair", defaults.coursePair) ?: return invalid(raw, "Invalid coursePair")
        if (pair != "pl-ru") return invalid(raw, "Unsupported coursePair")
        val method = root.optionalEnum("explanationMethod", defaults.explanationMethod) ?: return invalid(raw, "Invalid explanationMethod")
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
        return PreferencesDecode.Loaded(UserPreferencesV2(2, pair, method, answer, appearance, motion, swipe, reminder, tint))
    }

    fun encode(value: UserPreferencesV2): String {
        require(value.schemaVersion == 2 && value.coursePair == "pl-ru" && validReminder(value.reminder) && value.glassTintPercent in 0..100)
        val reminder = value.reminder
        val root = JsonObject(mapOf(
            "schemaVersion" to JsonPrimitive(2),
            "coursePair" to JsonPrimitive(value.coursePair),
            "explanationMethod" to JsonPrimitive(value.explanationMethod.name),
            "answerMode" to JsonPrimitive(value.answerMode.name),
            "appearance" to JsonPrimitive(value.appearance.name),
            "motion" to JsonPrimitive(value.motion.name),
            "swipeRatingEnabled" to JsonPrimitive(value.swipeRatingEnabled),
            "glassTintPercent" to JsonPrimitive(value.glassTintPercent),
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
    private fun invalid(raw: String, reason: String) = PreferencesDecode.RecoveryRequired(raw, reason)
}
