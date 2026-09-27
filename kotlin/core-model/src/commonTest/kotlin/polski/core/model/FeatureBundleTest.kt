package polski.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * UniversalCorePlan.md §1/§3.2: [FeatureBundle] is the open, language-independent "конструкция ×
 * набор признаков" cell — a plain [Map] keyed by [FeatureKey], not a closed enum per axis.
 */
class FeatureBundleTest {
    @Test fun featureBundleIsAPlainMapKeyedByFeatureKey() {
        val bundle: FeatureBundle = mapOf(FeatureKey("Case") to FeatureValue("Gen"), FeatureKey("Number") to FeatureValue("Sing"))
        assertEquals(FeatureValue("Gen"), bundle[FeatureKey("Case")])
        assertEquals(FeatureValue("Sing"), bundle[FeatureKey("Number")])
        assertNull(bundle[FeatureKey("Tense")])
    }

    @Test fun featureKeyAndValueEqualityIsStructuralNotIdentity() {
        assertEquals(FeatureKey("Polarity"), FeatureKey("Polarity"))
        assertEquals(FeatureValue("Neg"), FeatureValue("Neg"))
    }

    @Test fun constructionDefaultsToNoAxesAndNoComposition() {
        val construction = Construction(id = "core.verb.tense")
        assertEquals(emptySet(), construction.axes)
        assertEquals(emptyList(), construction.composesOf)
    }

    @Test fun compositeConstructionNamesTheStepsItSequentiallyApplies() {
        // core.composite (UniversalCorePlan.md §2): a chain applies steps in order to one bundle.
        val mixed = Construction(id = "core.composite.mixed", composesOf = listOf("core.verb.tense", "core.sentence.polarity"))
        assertEquals(listOf("core.verb.tense", "core.sentence.polarity"), mixed.composesOf)
    }

    @Test fun skillSpecFocusNamesTheAxisAndTheFromToValues() {
        // lang/pl/curriculum.json's case.gen.neg (UniversalCorePlan.md §3.2).
        val spec = SkillSpec(
            id = "case.gen.neg",
            construction = "core.sentence.polarity",
            focus = FeatureFocus(FeatureKey("Polarity"), FeatureValue("Pos"), FeatureValue("Neg")),
            fixed = mapOf(FeatureKey("Tense") to FeatureValue("Pres")),
            level = "A1",
            prerequisites = listOf("case.acc.f"),
        )
        assertEquals(FeatureKey("Polarity"), spec.focus?.feature)
        assertEquals(FeatureValue("Neg"), spec.focus?.to)
        assertEquals(FeatureValue("Pres"), spec.fixed[FeatureKey("Tense")])
    }

    @Test fun skillSpecFocusAndFixedAreOptionalWithSafeDefaults() {
        val spec = SkillSpec(id = "case.acc.f", construction = "core.argument.case-role")
        assertNull(spec.focus)
        assertEquals(emptyMap(), spec.fixed)
        assertEquals(emptyList(), spec.prerequisites)
    }
}
