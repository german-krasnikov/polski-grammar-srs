package polski.mission

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/** M9 blueprint: a mission's last recorded outcome and when it happened — [lastOutcome] is
 *  `"goal_reached"` or `"dead_end"` (the same tags `MissionNodeRowOutcome` uses on the wire). */
data class MissionProgressEntry(val lastOutcome: String, val completedAt: String)

/** M9 blueprint: deliberately minimal — `missionId -> last outcome/date` only. No FSRS card, no
 *  attempt-journal entry in the MVP (named follow-up, see the blueprint's own Decisions
 *  section). */
data class MissionDocument(val schemaVersion: Int = 1, val progress: Map<String, MissionProgressEntry> = emptyMap())

/** Hand-rolled JSON, same style as `polski.vocabulary`/`polski.progress`'s own document codecs
 *  (no `@Serializable` plugin applied in `:shared` — a reflection-based `Json.encodeToString<T>`
 *  would fail at runtime without it). */
object MissionCodec {
    fun decode(raw: String): MissionDocument {
        val root = Json.parseToJsonElement(raw).jsonObject
        val schemaVersion = root["schemaVersion"]?.jsonPrimitive?.int ?: 1
        val progress = root["progress"]?.jsonObject?.mapValues { (_, value) ->
            val entry = value.jsonObject
            MissionProgressEntry(
                lastOutcome = entry.getValue("lastOutcome").jsonPrimitive.content,
                completedAt = entry.getValue("completedAt").jsonPrimitive.content,
            )
        } ?: emptyMap()
        return MissionDocument(schemaVersion, progress)
    }

    fun encode(document: MissionDocument): String = buildJsonObject {
        put("schemaVersion", document.schemaVersion)
        putJsonObject("progress") {
            document.progress.forEach { (missionId, entry) ->
                putJsonObject(missionId) {
                    put("lastOutcome", entry.lastOutcome)
                    put("completedAt", entry.completedAt)
                }
            }
        }
    }.toString()
}
