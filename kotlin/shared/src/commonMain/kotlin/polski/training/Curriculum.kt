package polski.training

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import polski.core.model.FeatureFocus
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.core.model.LexicalFilter
import polski.core.model.SkillSpec
import polski.data.generatedCurriculumJsonByLang

/**
 * Parses one `lang/<code>/curriculum.json` document (UniversalCorePlan.md §3.1/§3.2, §12 UC-06)
 * into its [SkillSpec]s. Pure parsing — no defaults beyond the JSON's own optionality; the
 * shipped file always writes `focus`/`lexicalFilter` explicitly, `null` included.
 */
fun parseCurriculum(json: String): List<SkillSpec> = Json.parseToJsonElement(json).jsonArray.map { it.jsonObject.toSkillSpec() }

/** pl's curriculum (UC-06) — the data ExerciseFactory.kt's `when(skillId)` branches are checked against. */
val plCurriculum: List<SkillSpec> by lazy { parseCurriculum(generatedCurriculumJsonByLang.getValue("pl")) }

private fun JsonElement?.orNullObject(): JsonObject? = this?.takeIf { it !is JsonNull }?.jsonObject

private fun JsonObject.toSkillSpec(): SkillSpec = SkillSpec(
    id = getValue("id").jsonPrimitive.content,
    construction = getValue("construction").jsonPrimitive.content,
    focus = this["focus"].orNullObject()?.toFeatureFocus(),
    fixed = this["fixed"].orNullObject()?.toFeatureBundle() ?: emptyMap(),
    lexicalFilter = this["lexicalFilter"].orNullObject()?.toLexicalFilter(),
    level = this["level"]?.jsonPrimitive?.contentOrNull,
    prerequisites = this["prerequisites"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
)

private fun JsonObject.toFeatureFocus(): FeatureFocus = FeatureFocus(
    feature = FeatureKey(getValue("feature").jsonPrimitive.content),
    from = FeatureValue(getValue("from").jsonPrimitive.content),
    to = FeatureValue(getValue("to").jsonPrimitive.content),
)

private fun JsonObject.toFeatureBundle(): Map<FeatureKey, FeatureValue> =
    entries.associate { (key, value) -> FeatureKey(key) to FeatureValue(value.jsonPrimitive.content) }

private fun JsonObject.toLexicalFilter(): LexicalFilter = LexicalFilter(
    slot = getValue("slot").jsonPrimitive.content,
    where = getValue("where").jsonObject.entries.associate { (key, value) ->
        key to value.jsonArray.map { it.jsonPrimitive.content }
    },
)
