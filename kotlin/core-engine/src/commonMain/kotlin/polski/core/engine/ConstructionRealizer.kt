package polski.core.engine

import polski.core.model.FeatureBundle
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.core.model.RealizedSentence

/**
 * UniversalCorePlan.md §5.1: a lexeme's own grammatical facts that don't come from the sentence's
 * [FeatureBundle] but from the lexeme itself — e.g. a noun's inherent `Gender`, which an adjective
 * or possessive agreeing with it (`agree`, §5.2) must borrow. `:core-engine` never hardcodes which
 * lexemes exist; the caller supplies this lookup.
 */
fun interface LexemeFeatures {
    fun of(lexemeId: String): FeatureBundle
}

/**
 * One lexical position in a [ConstructionTemplate]'s `order` (§5.2) — [category] is the
 * [Morphology] lexeme-id prefix (`"noun"`, `"adjective"`, `"possessive"`, `"verb"`, ...),
 * [requiredFeatures] is the exact [FeatureBundle] shape that lexeme category's table was built
 * with (a noun's forms vary by `Number`+`Case` only; an adjective's also by `Gender`) — overridable
 * per condition via [requiredFeaturesWhen] (`"Tense=past"` needs `Gender` too, present/future don't).
 * [agreementSource] names another slot whose [LexemeFeatures] (typically `Gender`) this slot
 * borrows for the features it requires but the sentence bundle doesn't carry on its own.
 */
data class SlotTemplate(
    val name: String,
    val category: String,
    val optional: Boolean = false,
    val requiredFeatures: List<String> = emptyList(),
    val requiredFeaturesWhen: Map<String, List<String>> = emptyMap(),
    val agreementSource: String? = null,
)

/**
 * UniversalCorePlan.md §3.2/§5.2: one construction's realization recipe — `order` ([slots], in
 * list order, overridable per condition via [orderWhen], e.g. English `mood.question` moving `aux`
 * before the subject), `gov` ([govFeature]/[govDefault]/[govWhen], e.g. pl's negation flipping the
 * object's case from Acc to Gen), joined by [separator]. `when` is [govWhen]'s, [orderWhen]'s and
 * [SlotTemplate.requiredFeaturesWhen]'s single-condition syntax: `"FeatureKey=value"`.
 */
data class ConstructionTemplate(
    val slots: List<SlotTemplate>,
    val separator: String = " ",
    val govFeature: String? = null,
    val govDefault: String? = null,
    val govWhen: Map<String, String> = emptyMap(),
    val orderWhen: Map<String, List<SlotTemplate>> = emptyMap(),
)

/**
 * UniversalCorePlan.md §5.1/§5.2/§12 UC-07: realizes one construction's surface text from a
 * [FeatureBundle] and the lexemes filling its slots — the generic replacement for
 * `GrammarEngine.kt`'s hand-written `nounPhrase`/`verbForm`. Every grammatical fact comes from
 * [templates] (per-language data) or [morphology] (a lookup); this class contains none.
 */
class ConstructionRealizer(
    private val templates: Map<String, ConstructionTemplate>,
    private val morphology: Morphology,
    private val lexemeFeatures: LexemeFeatures,
) {
    fun realize(construction: String, bundle: FeatureBundle, lexicalSlots: Map<String, String>): RealizedSentence {
        val template = templates[construction] ?: error("ConstructionRealizer: no template for construction=\"$construction\"")
        val resolved = applyGovernment(template, bundle)
        val slots = template.orderWhen.entries.firstOrNull { (condition, _) -> matches(condition, resolved) }?.value ?: template.slots
        val parts = mutableListOf<String>()
        val spans = mutableMapOf<String, IntRange>()
        var offset = 0
        for (slot in slots) {
            val lexeme = lexicalSlots[slot.name]
                ?: if (slot.optional) continue else error("ConstructionRealizer: missing lexical slot \"${slot.name}\" for construction=\"$construction\"")
            val text = morphology.form("${slot.category}:$lexeme", slotBundle(slot, resolved, lexicalSlots))
            if (parts.isNotEmpty()) offset += template.separator.length
            spans[slot.name] = offset until (offset + text.length)
            offset += text.length
            parts += text
        }
        return RealizedSentence(parts.joinToString(template.separator), spans)
    }

    private fun applyGovernment(template: ConstructionTemplate, bundle: FeatureBundle): FeatureBundle {
        val feature = template.govFeature ?: return bundle
        val key = FeatureKey(feature)
        val value = template.govWhen.entries.firstOrNull { (condition, _) -> matches(condition, bundle) }?.value ?: template.govDefault
        return if (value == null) bundle else bundle + (key to FeatureValue(value))
    }

    private fun slotBundle(slot: SlotTemplate, bundle: FeatureBundle, lexicalSlots: Map<String, String>): FeatureBundle {
        val features = slot.requiredFeaturesWhen.entries.firstOrNull { (condition, _) -> matches(condition, bundle) }?.value ?: slot.requiredFeatures
        val agreement: FeatureBundle = slot.agreementSource?.let { agreementSlot ->
            val agreementLexeme = lexicalSlots[agreementSlot]
                ?: error("ConstructionRealizer: agreementSource \"$agreementSlot\" has no lexeme for slot \"${slot.name}\"")
            lexemeFeatures.of(agreementLexeme)
        } ?: emptyMap()
        return features.associate { name ->
            val key = FeatureKey(name)
            key to (bundle[key] ?: agreement[key] ?: error("ConstructionRealizer: missing feature \"$name\" for slot \"${slot.name}\""))
        }
    }

    private fun matches(condition: String, bundle: FeatureBundle): Boolean {
        val (key, value) = condition.split("=", limit = 2).let { it[0] to it[1] }
        return bundle[FeatureKey(key)] == FeatureValue(value)
    }
}
