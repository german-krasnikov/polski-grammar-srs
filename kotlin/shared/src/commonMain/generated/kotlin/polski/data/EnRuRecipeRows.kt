// Generated from kotlin/shared/src/commonMain/kotlin/polski/data/PlRuRecipeRows.kt by dsl/derive-enru-mappers.mjs: the en-ru copy of that file. Edit the pl-ru file, then run biome generate.
package polski.data

import polski.core.RecipeSet
import polski.core.engine.ChainStepRecipe
import polski.core.engine.ChangeSpec
import polski.core.engine.ConstantSlot
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
import polski.data.rows.enru.*

/**
 * R1.11: the recipes and the constructions of the en-ru rows as the types of :core-engine (R1Design.md D5). A variant is one record with a kind and
 * OPTIONAL fields, so each mapper first checks that the fields written are exactly the ones its kind uses: the engine would meet a missing one at
 * the first exercise of the skill, and would never see one of another kind. Nothing here fills in what the data does not say, except what the
 * engine types default (an absent `accepted` is none, an absent `optional` is false).
 */
private fun requireFields(what: String, kind: String, written: Map<String, Any?>, vararg needed: String) {
    val present = written.filterValues { it != null }.keys
    require(present == needed.toSet()) { "$what of kind $kind writes $present, the kind needs ${needed.toSet()}" }
}

internal fun PhraseSpecRow.toPhraseSpec() = PhraseSpec(construction, bundle.map { it.feature to it.value }.toDistinctMap("bundle of $construction"), ownerLexeme)

internal fun TextValueRow.toTextValue(): TextValue {
    val written = mapOf("key" to key, "value" to value, "spec" to spec, "genderToKey" to genderToKey, "caseId" to caseId)
    val text: TextValue = when (kind) {
        TextValueRowKind.COPY -> { requireFields("text value", kind.tag, written, "key"); TextValue.Copy(key!!) }
        TextValueRowKind.LITERAL -> { requireFields("text value", kind.tag, written, "value"); TextValue.Literal(value!!) }
        TextValueRowKind.PHRASE -> { requireFields("text value", kind.tag, written, "spec"); TextValue.Phrase(spec!!.toPhraseSpec()) }
        TextValueRowKind.PRONOUN -> {
            requireFields("text value", kind.tag, written, "genderToKey", "caseId")
            val keys = genderToKey!!.map { it.gender to it.pronounId }.toDistinctMap("pronoun for case $caseId")
            require("default" in keys) { "pronoun for case $caseId has no \"default\" gender" }
            TextValue.Pronoun(keys, caseId!!)
        }
    }
    return if (capitalized == true) TextValue.Capitalized(text) else text
}

internal fun TextSpecRow.toTextSpec(): TextSpec {
    val written = mapOf("value" to value, "key" to key, "values" to values, "parts" to parts, "spec" to spec, "prefixCase" to prefixCase, "punct" to punct)
    return when (kind) {
        TextSpecRowKind.DIRECT -> { requireFields("text spec", kind.tag, written, "value"); TextSpec.Direct(value!!.toTextValue()) }
        TextSpecRowKind.PATTERN -> {
            requireFields("text spec", kind.tag, written, "key", "values")
            TextSpec.Pattern(key!!, values!!.map { it.name to it.value.toTextValue() }.toDistinctMap("pattern $key"))
        }
        TextSpecRowKind.CONCAT -> { requireFields("text spec", kind.tag, written, "parts"); TextSpec.Concat(parts!!.map { it.toTextValue() }) }
        TextSpecRowKind.PREFIXED -> {
            requireFields("text spec", kind.tag, written, "spec", "prefixCase", "punct")
            TextSpec.Prefixed(spec!!.toPhraseSpec(), prefixCase!!, punct!!)
        }
    }
}

internal fun ReasonSpecRow.toReasonSpec(): ReasonSpec {
    val written = mapOf("key" to key, "rules" to rules)
    return when (kind) {
        ReasonSpecRowKind.FIXED -> { requireFields("reason", kind.tag, written, "key"); ReasonSpec.Fixed(key!!) }
        ReasonSpecRowKind.CLASSIFIED -> {
            requireFields("reason", kind.tag, written, "rules")
            require(rules!!.any { it.whenUnchanged != true && it.whenGender == null && it.whenNomSingularSuffix == null }) {
                "a classified reason has no unconditional fallback rule"
            }
            ReasonSpec.Classified(rules.map { ReasonRule(it.whenUnchanged, it.whenGender, it.whenNomSingularSuffix, it.key) })
        }
    }
}

internal fun ChangeSpecRow.toChangeSpec() = ChangeSpec(from.toTextValue(), to.toTextValue(), reason.toReasonSpec())

internal fun ConditionRow.toCondition(): String {
    require("=" !in feature) { "a condition's feature has no \"=\": $feature" }
    return "$feature=$value"
}

internal fun SlotRow.toSlot() = SlotTemplate(
    name, category, optional == true, requiredFeatures,
    requiredFeaturesWhen.orEmpty().map { it.condition.toCondition() to it.features }.toDistinctMap("slot $name"), agreementSource,
)

internal fun ConstructionRow.toTemplate(): ConstructionTemplate {
    require(govFeature != null || (govDefault == null && govWhen.isNullOrEmpty())) { "$id: govDefault and govWhen need a govFeature" }
    return ConstructionTemplate(
        slots = slots.map { it.toSlot() }, govFeature = govFeature, govDefault = govDefault,
        govWhen = govWhen.orEmpty().map { it.condition.toCondition() to it.result }.toDistinctMap("$id govWhen"),
        orderWhen = orderWhen.orEmpty().map { it.condition.toCondition() to it.slots.map(SlotRow::toSlot) }.toDistinctMap("$id orderWhen"),
    )
}

internal fun OwnerDrawRow.toOwnerDraw(): OwnerDraw {
    require(owners.isNotEmpty()) { "an owner draw has no owner" }
    return OwnerDraw(owners.map { it.lexeme }, owners.map { it.lexeme to it.labelKey }.toDistinctMap("owner draw"), promptPrefixKey, promptSuffixKey)
}

internal fun StaticExerciseRow.toStaticExercise() =
    StaticExercise(sourceKey, promptKey, expectedKey, explanationKey, acceptedKeys, changeFromKey, changeToKey, changeReasonKey, nounId, adjectiveId, tags)

/** A static recipe writes nothing but its exercise; a constructed one writes its texts, its changes and its tags, and a prompt (a key, or an owner draw). */
internal fun SkillRecipeRow.toSkillRecipe(): SkillRecipe {
    val others = mapOf(
        "source" to source, "expected" to expected, "promptKey" to promptKey, "explanationKey" to explanationKey, "changes" to changes, "accepted" to accepted,
        "tags" to tags, "includeNounGenderTag" to includeNounGenderTag, "ownerDraw" to ownerDraw, "numberOverride" to numberOverride,
    )
    if (staticOverride != null) {
        require(staticPool == null) { "$skill: staticOverride and staticPool are exclusive" }
        require(others.values.all { it == null }) { "$skill: a static recipe writes staticOverride only, not ${others.filterValues { it != null }.keys}" }
        return SkillRecipe(skill, staticOverride = staticOverride.toStaticExercise())
    }
    if (staticPool != null) {
        require(staticPool.isNotEmpty()) { "$skill: staticPool must not be empty" }
        require(others.values.all { it == null }) { "$skill: a pooled recipe writes staticPool only, not ${others.filterValues { it != null }.keys}" }
        return SkillRecipe(skill, staticPool = staticPool.map { it.toStaticExercise() })
    }
    val lacking = listOf("source", "expected", "explanationKey", "changes", "tags").filter { others.getValue(it) == null }
    require(lacking.isEmpty()) { "$skill: a constructed recipe lacks $lacking" }
    require(promptKey != null || ownerDraw != null) { "$skill: a recipe has a promptKey or an ownerDraw" }
    return SkillRecipe(
        skillId = skill, source = source!!.toTextSpec(), expected = expected!!.toTextSpec(), promptKey = promptKey.orEmpty(), explanationKey = explanationKey!!,
        changes = changes!!.map { it.toChangeSpec() }, accepted = accepted.orEmpty().map { it.toTextSpec() }, tags = tags!!,
        includeNounGenderTag = includeNounGenderTag == true, ownerDraw = ownerDraw?.toOwnerDraw(), numberOverride = numberOverride,
    )
}

/** Only the first step has no skill: it is the first skill of the noun's gender, resolved by the engine. */
internal fun ChainStepRecipeRow.toChainStep() =
    ChainStepRecipe(skill.orEmpty(), source.toTextSpec(), expected.toTextSpec(), promptKey, explanationKey, changes.map { it.toChangeSpec() }, tags, ownerOut)

internal fun recipeSetOf(recipes: List<SkillRecipeRow>, chain: List<ChainStepRecipeRow>, defaults: RecipeDefaultsRow): RecipeSet {
    val firstSkills = defaults.firstSkillByGender.map { it.gender to it.skill }.toDistinctMap("firstSkillByGender")
    require("default" in firstSkills) { "firstSkillByGender has no \"default\" gender" }
    require(chain.drop(1).all { it.skill != null }) { "a chain step after the first has no skill" }
    return RecipeSet(
        skills = recipes.map { it.skill to it.toSkillRecipe() }.toDistinctMap("recipes"), chain = chain.map { it.toChainStep() },
        firstSkillByGender = firstSkills, defaultOwnerLexeme = defaults.defaultOwnerLexeme, verbLexeme = defaults.verbLexeme,
        constantSlots = defaults.constantSlots.orEmpty().map { ConstantSlot(it.slot, it.lexeme, it.whenFeature, it.whenValues.orEmpty()) },
    )
}

internal val generatedEnRuConstructionTemplates: Map<String, ConstructionTemplate> = CONSTRUCTION_ROW_ROWS.associate { it.id to it.toTemplate() }
internal val generatedEnRuRecipes: RecipeSet = recipeSetOf(SKILL_RECIPE_ROW_ROWS, CHAIN_STEP_RECIPE_ROW_ROWS, RECIPE_DEFAULTS_ROW_ROWS.single())
