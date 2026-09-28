package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.pack.EmbeddedCoursePackSource

/**
 * EN-06 (Plans/Kotlin/EnRuPackPlan.md §6): [PackRegistry] must offer a real active-pack switch
 * keyed by [CoursePack.pairId], while pl-ru stays the default active pack when nothing selects
 * otherwise. EN-22 is the first task to actually call [PackRegistry.select] on the real
 * production registry ([packRegistry]) — see [productionRegistryCanSelectTheRealEnRuPackAndSwitchBack].
 */
class PackRegistryTest {
    private fun packJson(pairId: String, target: String): String =
        """{"schemaVersion":1,"id":"$pairId","targetLanguage":"$target","nativeLanguage":"ru"}"""

    private val plRu = CoursePack(EmbeddedCoursePackSource("pl-ru", packJson("pl-ru", "pl")))
    private val enRu = CoursePack(EmbeddedCoursePackSource("en-ru", packJson("en-ru", "en")))
    private val registry = PackRegistry(listOf(plRu, enRu))

    @Test fun defaultActivePackIsTheFirstRegisteredPack() {
        assertEquals("pl-ru", registry.active.pairId)
    }

    @Test fun selectSwitchesActivePackByPairId() {
        registry.select("en-ru")
        assertEquals("en-ru", registry.active.pairId)
    }

    @Test fun productionRegistryDefaultsToPlRuWithoutAnySelection() {
        assertEquals("pl-ru", packRegistry.active.pairId)
    }

    @Test fun registryExposesWhetherAPairIdIsRegistered() {
        assertEquals(true, registry.contains("pl-ru"))
        assertEquals(true, registry.contains("en-ru"))
        assertEquals(false, registry.contains("de-ru"))
    }

    /**
     * EN-22 (Plans/Kotlin/EnRuPackPlan.md §6): en-ru is embedded into the real, production
     * [packRegistry] too — reconstructed from its v2 layers (EN-04's [CoursePackLoader]), the same
     * way pl-ru's own fixture comparison already proves in [CoursePackLoaderTest] — not only the
     * fixture registry above. Restores pl-ru afterwards (`finally`) so this real singleton's
     * default stays unaffected for every other test sharing this binary, pass or fail.
     */
    @Test fun productionRegistryCanSelectTheRealEnRuPackAndSwitchBack() {
        assertEquals(true, packRegistry.contains("en-ru"))
        try {
            packRegistry.select("en-ru")
            assertEquals("en-ru", packRegistry.active.pairId)
            assertEquals("en", packRegistry.active.targetLanguage)
            assertEquals("ru", packRegistry.active.nativeLanguage)
        } finally {
            packRegistry.select("pl-ru")
        }
        assertEquals("pl-ru", packRegistry.active.pairId)
    }
}
