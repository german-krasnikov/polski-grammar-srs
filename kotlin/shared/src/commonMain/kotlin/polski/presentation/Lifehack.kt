package polski.presentation

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import polski.data.generatedLifehacksJsonByPairId
import polski.data.packRegistry
import polski.data.skills

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
 * One skill's (or one cross-skill topic's) full set of lifehacks, for a pack-wide "Лайфхаки"
 * listing on top of [LifehackProvider.forSkill]'s per-skill card block (EnRuPackPlan.md §4.3).
 * [skillId] is `null` for a cross-skill entry — [title] is then [topic] itself, since no
 * [polski.model.Skill] names it. [title] is a real skill's own display title (curriculum order)
 * when [skillId] is set, never invented — the same title the skill picker/matrix already show.
 */
data class LifehackGroup(val skillId: String?, val topic: String?, val title: String, val lifehacks: List<Lifehack>)

/**
 * EnRuPackPlan.md §4.2, the same reserved-port pattern UniversalCorePlan.md §5.4 uses for
 * `ExplanationProvider`/`AudioProvider`. A plain interface, not `fun interface` — [listAll] has
 * no sensible default in terms of [forSkill] alone (it needs curriculum order too), so there is
 * more than one abstract member; nothing in this codebase relied on SAM-converting a lambda into
 * one. A future server-backed provider (real `helpful`/`notHelpful` votes, `community` status) is
 * still a second implementation, not a breaking change to any caller.
 */
interface LifehackProvider {
    fun forSkill(skillId: String): List<Lifehack>

    /** Cheap existence check for a front-side "has a lifehack" badge — a caller that only needs
     *  to know whether to draw the badge should call this, not `forSkill(id).isNotEmpty()`, so a
     *  provider that can answer without building the full [Lifehack] list (like
     *  [StaticPackLifehackProvider]'s own override) gets the chance to. */
    fun hasLifehacks(skillId: String): Boolean = forSkill(skillId).isNotEmpty()

    /** Every lifehack the active pack has, grouped for a "Лайфхаки" section: one [LifehackGroup]
     *  per skill that actually has a tip, in curriculum order, each named by that skill's own
     *  display title — followed by any cross-skill (`skillId == null`) topics, alphabetically
     *  (curriculum has no ordering opinion about those). A skill/topic with no authored tip is
     *  skipped entirely, never an empty group (§4.3's "пусто -> блок не рисуется"). */
    fun listAll(): List<LifehackGroup>
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

    private val activeHacks: List<Lifehack> get() = byPairId[packRegistry.active.pairId] ?: emptyList()

    override fun forSkill(skillId: String): List<Lifehack> = activeHacks.filter { it.skillId == skillId }

    override fun hasLifehacks(skillId: String): Boolean = activeHacks.any { it.skillId == skillId }

    override fun listAll(): List<LifehackGroup> {
        val bySkillId = activeHacks.filter { it.skillId != null }.groupBy { it.skillId }
        val skillGroups = skills.mapNotNull { skill ->
            bySkillId[skill.id]?.let { LifehackGroup(skill.id, null, skill.title, it) }
        }
        val topicGroups = activeHacks.filter { it.skillId == null }
            .groupBy { it.topic }
            .entries.sortedBy { it.key.orEmpty() }
            .map { (topic, hacks) -> LifehackGroup(null, topic, topic.orEmpty(), hacks) }
        return skillGroups + topicGroups
    }
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
