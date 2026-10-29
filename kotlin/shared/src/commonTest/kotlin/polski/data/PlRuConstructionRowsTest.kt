package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.core.engine.ConstructionTemplate
import polski.core.engine.SlotTemplate
import polski.data.rows.plru.*

/**
 * R1.11b: the constructions of the rows (slots, government, ordered alternatives) on hand-built rows with expected values written out, and the real pl-ru
 * templates against values read off the sealed source (lang/pl/realization.json sha256 2dd50f80...) before it was transcribed.
 */
class PlRuConstructionRowsTest {
    private fun slot(name: String, vararg features: String, optional: Boolean? = null, whens: List<RequiredWhenRow>? = null, agreement: String? = null) =
        SlotRow(name, "cat-$name", optional, features.toList(), whens, agreement)
    private fun condition(feature: String, value: String) = ConditionRow(feature, value)

    @Test fun aSlotKeepsItsFeaturesItsConditionsInOrderAndItsAgreement() {
        val row = slot("verb", "Tense", "Number", optional = true, agreement = "noun", whens = listOf(
            RequiredWhenRow(condition("Tense", "past"), listOf("Tense", "Gender")), RequiredWhenRow(condition("Aspect", "x=y"), listOf("Aspect")),
        ))
        val expected = SlotTemplate("verb", "cat-verb", true, listOf("Tense", "Number"), mapOf("Tense=past" to listOf("Tense", "Gender"), "Aspect=x=y" to listOf("Aspect")), "noun")
        assertEquals(expected, row.toSlot())
        assertEquals(SlotTemplate("n", "cat-n", false, emptyList(), emptyMap(), null), slot("n").toSlot())
        assertEquals(listOf("Tense=past", "Aspect=x=y"), row.toSlot().requiredFeaturesWhen.keys.toList())
    }

    @Test fun aConstructionKeepsItsSlotsItsGovernmentAndItsOrderedAlternativesInOrder() {
        val row = ConstructionRow(
            "c", listOf(slot("a"), slot("b")), "Case", "acc",
            listOf(GovWhenRow(condition("Polarity", "neg"), "gen"), GovWhenRow(condition("Mood", "q"), "voc")),
            listOf(OrderWhenRow(condition("Mood", "q"), listOf(slot("b"), slot("a"))), OrderWhenRow(condition("Mood", "r"), listOf(slot("a")))),
        )
        val template = ConstructionTemplate(
            slots = listOf(slot("a").toSlot(), slot("b").toSlot()), govFeature = "Case", govDefault = "acc", govWhen = mapOf("Polarity=neg" to "gen", "Mood=q" to "voc"),
            orderWhen = mapOf("Mood=q" to listOf(slot("b").toSlot(), slot("a").toSlot()), "Mood=r" to listOf(slot("a").toSlot())),
        )
        assertEquals(template, row.toTemplate())
        assertEquals(listOf("Mood=q", "Mood=r"), row.toTemplate().orderWhen.keys.toList())
        assertEquals(ConstructionTemplate(listOf(slot("a").toSlot())), ConstructionRow("c", listOf(slot("a"))).toTemplate())
    }

    @Test fun aConstructionRefusesAConditionTwiceAHalfGovernmentAndAFeatureWithAnEqualsSign() {
        val bare = ConstructionRow("c", listOf(slot("a")))
        val twice = listOf(GovWhenRow(condition("P", "n"), "a"), GovWhenRow(condition("P", "n"), "b"))
        val order = OrderWhenRow(condition("Mood", "q"), listOf(slot("a")))
        val required = RequiredWhenRow(condition("Tense", "past"), listOf("Tense"))
        val bad = listOf(
            bare.copy(govFeature = "Case", govWhen = twice), bare.copy(govDefault = "acc"), bare.copy(govWhen = twice.take(1)), bare.copy(orderWhen = listOf(order, order)),
            bare.copy(orderWhen = listOf(OrderWhenRow(condition("a=b", "c"), emptyList()))), bare.copy(slots = listOf(slot("a", whens = listOf(required, required)))),
        )
        for (row in bad) assertFailsWith<IllegalArgumentException>("$row") { row.toTemplate() }
    }

    @Test fun theRealConstructionsAreTheFiveOfRealizationWithThePolarityGovernmentAndThePastFeatures() {
        val templates = generatedPlRuConstructionTemplates
        assertEquals(listOf("core.argument.case-role", "core.agreement.possessive", "core.number", "core.sentence.polarity", "core.verb.tense"), templates.keys.toList())
        val polarity = templates.getValue("core.sentence.polarity")
        assertEquals(Triple("Case", "acc", mapOf("Polarity=neg" to "gen")), Triple(polarity.govFeature, polarity.govDefault, polarity.govWhen))
        assertEquals(listOf("owner", "adjective", "noun"), polarity.slots.map { it.name })
        assertEquals(mapOf("Tense=past" to listOf("Tense", "Person", "Number", "Gender")), templates.getValue("core.verb.tense").slots.single().requiredFeaturesWhen)
    }
}
