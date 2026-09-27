package polski.model

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue

/**
 * UniversalCorePlan.md §4.1 UC-01: the closed Polish enums stay exactly as they were (same
 * entries, same [GramCase.id]/`fromId`, unchanged by every other consumer/test) — this only adds
 * a thin adapter mapping each enum onto the universal core's open [FeatureKey]/[FeatureValue]
 * catalog, proving pl declares a subset of it rather than :core-model growing pl-specific cases.
 */
class GrammarFeatureAdapterTest {
    @Test fun caseEnumMapsOntoTheOpenCaseFeatureByItsExistingWireId() {
        assertEquals(FeatureValue("gen"), GramCase.GEN.toFeatureValue())
        assertEquals(FeatureValue("acc"), GramCase.ACC.toFeatureValue())
    }

    @Test fun numberGenderTenseMapOntoTheirOwnFeatureValues() {
        assertEquals(FeatureValue("pl"), NumberGram.PL.toFeatureValue())
        assertEquals(FeatureValue("f"), Gender.F.toFeatureValue())
        assertEquals(FeatureValue("future"), Tense.FUTURE.toFeatureValue())
    }

    @Test fun personMapsItsIntIdAsAFeatureValue() {
        assertEquals(FeatureValue("1"), Person.FIRST.toFeatureValue())
        assertEquals(FeatureValue("3"), Person.THIRD.toFeatureValue())
    }

    @Test fun featureKeysAreStableUdStyleNames() {
        assertEquals(FeatureKey("Case"), CaseFeature)
        assertEquals(FeatureKey("Number"), NumberFeature)
        assertEquals(FeatureKey("Gender"), GenderFeature)
        assertEquals(FeatureKey("Person"), PersonFeature)
        assertEquals(FeatureKey("Tense"), TenseFeature)
    }
}
