package polski.data

import polski.core.engine.ChangeSpec
import polski.core.engine.ConstructionTemplate
import polski.core.engine.PhraseSpec
import polski.core.engine.ReasonRule
import polski.core.engine.ReasonSpec
import polski.core.engine.SlotTemplate
import polski.core.engine.TextSpec
import polski.core.engine.TextValue
import polski.data.rows.plru.*

/**
 * R1.11: the recipe expressions and the constructions of the pl-ru rows as the types of :core-engine (R1Design.md D5). A variant is one record with a kind and
 * OPTIONAL fields, so each mapper first checks that the fields written are exactly the ones its kind uses: the engine would meet a missing one at
 * the first exercise of the skill, and would never see one of another kind. Nothing here fills in what the data does not say, except what the
 * engine types default (an absent `accepted` is none, an absent `optional` is false).
 */
private fun requireFields(what: String, kind: String, written: Map<String, Any?>, vararg needed: String) {
    val present = written.filterValues { it != null }.keys
    require(present == needed.toSet()) { "$what of kind $kind writes $present, the kind needs ${needed.toSet()}" }
}

/** A map from pairs; a name written twice is refused, not overwritten. */
private fun <V> List<Pair<String, V>>.toDistinctMap(what: String): Map<String, V> = toMap().also {
    require(it.size == size) { "$what: ${groupingBy { pair -> pair.first }.eachCount().filterValues { n -> n > 1 }.keys} written twice" }
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
            require(rules!!.any { it.whenClass == "default" }) { "a classified reason has no \"default\" rule" }
            ReasonSpec.Classified(rules.map { ReasonRule(it.whenClass, it.key) })
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

internal val generatedPlRuConstructionTemplates: Map<String, ConstructionTemplate> = CONSTRUCTION_ROW_ROWS.associate { it.id to it.toTemplate() }
