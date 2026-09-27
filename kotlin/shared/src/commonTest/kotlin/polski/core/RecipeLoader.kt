package polski.core

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import polski.core.engine.ChainStepRecipe
import polski.core.engine.ChangeSpec
import polski.core.engine.ConstructionTemplate
import polski.core.engine.OwnerDraw
import polski.core.engine.PhraseSpec
import polski.core.engine.ReasonRule
import polski.core.engine.ReasonSpec
import polski.core.engine.SkillRecipe
import polski.core.engine.SlotTemplate
import polski.core.engine.StaticExercise
import polski.core.engine.TextSpec
import polski.core.engine.TextValue

/**
 * UniversalCorePlan.md §3.1/§5.1/§12 UC-07: parses `lang/pl/realization.json`/`exercise-
 * recipes.json` (embedded at build time by `shared/build.gradle.kts`'s `generateFormsFixtureSource`,
 * test-only until UC-08) into the plain [ConstructionTemplate]/[SkillRecipe]/[ChainStepRecipe]
 * `:core-engine` types — manual `JsonElement` navigation, matching `polski.training.Curriculum`'s
 * convention (no `@Serializable` compiler plugin applied). Pure parsing; every Polish/Russian
 * string it touches is an opaque copy-key, never interpreted here.
 */
fun parseConstructionTemplates(json: String): Map<String, ConstructionTemplate> =
    Json.parseToJsonElement(json).jsonObject.getValue("constructions").jsonObject.mapValues { (_, v) -> v.jsonObject.toTemplate() }

private fun JsonObject.toTemplate(): ConstructionTemplate = ConstructionTemplate(
    slots = getValue("slots").jsonArray.map { it.jsonObject.toSlot() },
    govFeature = this["govFeature"]?.jsonPrimitive?.contentOrNull,
    govDefault = this["govDefault"]?.jsonPrimitive?.contentOrNull,
    govWhen = this["govWhen"]?.jsonObject?.toStringMap() ?: emptyMap(),
)

private fun JsonObject.toSlot(): SlotTemplate = SlotTemplate(
    name = getValue("name").jsonPrimitive.content,
    category = getValue("category").jsonPrimitive.content,
    optional = this["optional"]?.jsonPrimitive?.boolean ?: false,
    requiredFeatures = this["requiredFeatures"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
    requiredFeaturesWhen = this["requiredFeaturesWhen"]?.jsonObject?.mapValues { (_, v) -> v.jsonArray.map { it.jsonPrimitive.content } } ?: emptyMap(),
    agreementSource = this["agreementSource"]?.jsonPrimitive?.contentOrNull,
)

private fun JsonObject.toStringMap(): Map<String, String> = entries.associate { (k, v) -> k to v.jsonPrimitive.content }

data class RecipeSet(
    val skills: Map<String, SkillRecipe>,
    val chain: List<ChainStepRecipe>,
    val firstSkillByGender: Map<String, String>,
)

fun parseRecipes(json: String): RecipeSet {
    val root = Json.parseToJsonElement(json).jsonObject
    return RecipeSet(
        skills = root.getValue("skills").jsonObject.mapValues { (id, v) -> v.jsonObject.toSkillRecipe(id) },
        chain = root.getValue("chain").jsonArray.map { it.jsonObject.toChainStep() },
        firstSkillByGender = root.getValue("firstSkillByGender").jsonObject.toStringMap(),
    )
}

private fun JsonObject.toSkillRecipe(id: String): SkillRecipe = SkillRecipe(
    skillId = this["skillId"]?.jsonPrimitive?.content ?: id,
    source = this["source"]?.jsonObject?.toTextSpec() ?: TextSpec.Direct(TextValue.Literal("")),
    expected = this["expected"]?.jsonObject?.toTextSpec() ?: TextSpec.Direct(TextValue.Literal("")),
    promptKey = this["promptKey"]?.jsonPrimitive?.content ?: "",
    explanationKey = this["explanationKey"]?.jsonPrimitive?.content ?: "",
    changes = this["changes"]?.jsonArray?.map { it.jsonObject.toChangeSpec() } ?: emptyList(),
    accepted = this["accepted"]?.jsonArray?.map { it.jsonObject.toTextSpec() } ?: emptyList(),
    tags = this["tags"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
    includeNounGenderTag = this["includeNounGenderTag"]?.jsonPrimitive?.boolean ?: false,
    ownerDraw = this["ownerDraw"]?.jsonObject?.toOwnerDraw(),
    numberOverride = this["numberOverride"]?.jsonPrimitive?.contentOrNull,
    staticOverride = this["staticOverride"]?.jsonObject?.toStaticExercise(),
)

private fun JsonObject.toChainStep(): ChainStepRecipe = ChainStepRecipe(
    skillId = this["skillId"]?.jsonPrimitive?.content ?: "",
    source = getValue("source").jsonObject.toTextSpec(),
    expected = getValue("expected").jsonObject.toTextSpec(),
    promptKey = getValue("promptKey").jsonPrimitive.content,
    explanationKey = getValue("explanationKey").jsonPrimitive.content,
    changes = getValue("changes").jsonArray.map { it.jsonObject.toChangeSpec() },
    tags = this["tags"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
    ownerOut = this["ownerOut"]?.jsonPrimitive?.content ?: "my",
)

private fun JsonObject.toOwnerDraw(): OwnerDraw = OwnerDraw(
    pool = getValue("pool").jsonArray.map { it.jsonPrimitive.content },
    labelKeys = getValue("labelKeys").jsonObject.toStringMap(),
    promptPrefixKey = getValue("promptPrefixKey").jsonPrimitive.content,
    promptSuffixKey = getValue("promptSuffixKey").jsonPrimitive.content,
)

private fun JsonObject.toStaticExercise(): StaticExercise = StaticExercise(
    sourceKey = getValue("sourceKey").jsonPrimitive.content,
    promptKey = getValue("promptKey").jsonPrimitive.content,
    expectedKey = getValue("expectedKey").jsonPrimitive.content,
    explanationKey = getValue("explanationKey").jsonPrimitive.content,
    acceptedKeys = getValue("acceptedKeys").jsonArray.map { it.jsonPrimitive.content },
    changeFromKey = getValue("changeFromKey").jsonPrimitive.content,
    changeToKey = getValue("changeToKey").jsonPrimitive.content,
    changeReasonKey = getValue("changeReasonKey").jsonPrimitive.content,
    nounId = getValue("nounId").jsonPrimitive.content,
    adjectiveId = getValue("adjectiveId").jsonPrimitive.content,
    tags = getValue("tags").jsonArray.map { it.jsonPrimitive.content },
)

private fun JsonObject.toChangeSpec(): ChangeSpec = ChangeSpec(
    from = getValue("from").jsonObject.toTextValue(),
    to = getValue("to").jsonObject.toTextValue(),
    reason = getValue("reason").jsonObject.toReasonSpec(),
)

private fun JsonObject.toReasonSpec(): ReasonSpec = when (getValue("type").jsonPrimitive.content) {
    "fixed" -> ReasonSpec.Fixed(getValue("key").jsonPrimitive.content)
    "classified" -> ReasonSpec.Classified(
        getValue("rules").jsonArray.map { it.jsonObject.let { rule -> ReasonRule(rule.getValue("whenClass").jsonPrimitive.content, rule.getValue("key").jsonPrimitive.content) } },
    )
    else -> error("Unknown reason type in ${this}")
}

private fun JsonObject.toTextSpec(): TextSpec = when (getValue("type").jsonPrimitive.content) {
    "direct" -> TextSpec.Direct(getValue("value").jsonObject.toTextValue())
    "pattern" -> TextSpec.Pattern(getValue("key").jsonPrimitive.content, getValue("values").jsonObject.mapValues { (_, v) -> v.jsonObject.toTextValue() })
    "concat" -> TextSpec.Concat(getValue("parts").jsonArray.map { it.jsonObject.toTextValue() })
    "prefixed" -> TextSpec.Prefixed(getValue("spec").jsonObject.toPhraseSpec(), getValue("prefixCase").jsonPrimitive.content)
    else -> error("Unknown TextSpec type in $this")
}

private fun JsonObject.toTextValue(): TextValue = when (getValue("type").jsonPrimitive.content) {
    "copy" -> TextValue.Copy(getValue("key").jsonPrimitive.content)
    "literal" -> TextValue.Literal(getValue("value").jsonPrimitive.content)
    "phrase" -> TextValue.Phrase(getValue("spec").jsonObject.toPhraseSpec())
    "capitalized" -> TextValue.Capitalized(getValue("inner").jsonObject.toTextValue())
    "pronoun" -> TextValue.Pronoun(getValue("genderToKey").jsonObject.toStringMap(), getValue("case").jsonPrimitive.content)
    else -> error("Unknown TextValue type in $this")
}

private fun JsonObject.toPhraseSpec(): PhraseSpec = PhraseSpec(
    construction = getValue("construction").jsonPrimitive.content,
    bundle = getValue("bundle").jsonObject.toStringMap(),
    ownerLexeme = this["ownerLexeme"]?.jsonPrimitive?.contentOrNull,
)
