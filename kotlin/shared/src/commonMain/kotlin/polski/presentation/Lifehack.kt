package polski.presentation

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import polski.data.generatedLifehacksJsonByPairId
import polski.data.packRegistry

/** `source.kind` (`lifehacks-v1.schema.json`): why the tip is trusted, never invented (ADR-15). */
enum class LifehackSourceKind { Research, TeachingPractice, ProjectAuthored }

/** `status`: always [Editorial] today — [Community] is reserved for the future server-backed
 *  voting task ADR-15 explicitly defers, not a claim this project has verified anything yet. */
enum class LifehackStatus { Editorial, Community }

data class LifehackSource(val kind: LifehackSourceKind, val citation: String, val url: String? = null)

/** One pair-scoped L1-transfer tip (`courses/pairs/<pairId>/lifehacks.json`). [skillId] is `null`
 *  for a cross-skill tip ([topic] names it instead) — [LifehackProvider.forSkill] never returns
 *  one of those, since no real skill id equals `null`. */
data class Lifehack(
    val id: String,
    val skillId: String?,
    val topic: String?,
    val text: String,
    val source: LifehackSource,
    val status: LifehackStatus,
)

/**
 * EnRuPackPlan.md §4.2, the same reserved-port pattern UniversalCorePlan.md §5.4 uses for
 * `ExplanationProvider`/`AudioProvider`: a fun interface today, so a future server-backed provider
 * (real `helpful`/`notHelpful` votes, `community` status) is a second implementation, not a
 * breaking change to any caller.
 */
fun interface LifehackProvider {
    fun forSkill(skillId: String): List<Lifehack>
}

/**
 * The only [LifehackProvider] today: reads the active pack's own `pairs/<pairId>/lifehacks.json`
 * (embedded at build time into [generatedLifehacksJsonByPairId], `kotlin/shared/build.gradle.kts`,
 * the same mechanism `pair.json`/`course.json` use). An `object`, not a class holding a captured
 * pairId, so it always reflects whichever pack is [polski.data.PackRegistry.active] *now* — once
 * EN-22 wires a real target/native picker, switching packs needs no re-wiring here.
 */
object StaticPackLifehackProvider : LifehackProvider {
    private val byPairId: Map<String, List<Lifehack>> by lazy {
        generatedLifehacksJsonByPairId.mapValues { (_, json) -> parseLifehacksJson(json) }
    }

    override fun forSkill(skillId: String): List<Lifehack> =
        byPairId[packRegistry.active.pairId]?.filter { it.skillId == skillId } ?: emptyList()
}

private val sourceKindByWireName: Map<String, LifehackSourceKind> =
    mapOf("research" to LifehackSourceKind.Research, "teaching-practice" to LifehackSourceKind.TeachingPractice,
        "project-authored" to LifehackSourceKind.ProjectAuthored)

private val statusByWireName: Map<String, LifehackStatus> =
    mapOf("editorial" to LifehackStatus.Editorial, "community" to LifehackStatus.Community)

/** Parses one `lifehacks-v1.schema.json` document. Never throws on an empty `lifehacks` array;
 *  a malformed entry (unknown `source.kind`/`status`) throws via [Map.getValue] rather than
 *  silently dropping it — unlike [BlockKind]'s open-catalog tolerance, this schema's enums are
 *  closed and validated at authoring time by `scripts/validate-pack-v2.mjs`. */
internal fun parseLifehacksJson(json: String): List<Lifehack> =
    Json.parseToJsonElement(json).jsonObject.getValue("lifehacks").jsonArray.map { element ->
        val value = element.jsonObject
        val source = value.getValue("source").jsonObject
        Lifehack(
            id = value.getValue("id").jsonPrimitive.content,
            skillId = value.optionalString("skillId"),
            topic = value.optionalString("topic"),
            text = value.getValue("text").jsonPrimitive.content,
            source = LifehackSource(
                kind = sourceKindByWireName.getValue(source.getValue("kind").jsonPrimitive.content),
                citation = source.getValue("citation").jsonPrimitive.content,
                url = source["url"]?.jsonPrimitive?.content,
            ),
            status = statusByWireName.getValue(value.getValue("status").jsonPrimitive.content),
        )
    }

/** `null` for a JSON `null` or a missing key — [JsonObject.get] already gives `null` for missing;
 *  a schema-valid `"skillId": null` is [JsonNull], not absent, and needs the same result. */
private fun JsonObject.optionalString(key: String): String? = this[key]?.takeUnless { it is JsonNull }?.jsonPrimitive?.content
