package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * EN-06 (Plans/Kotlin/EnRuPackPlan.md §6): [PackRegistry] must offer a real active-pack switch
 * keyed by [CoursePackFields.pairId], while pl-ru stays the default active pack when nothing selects
 * otherwise. EN-22 is the first task to actually call [PackRegistry.select] on the real
 * production registry ([packRegistry]) — see [productionRegistryCanSelectTheRealEnRuPackAndSwitchBack].
 */
class PackRegistryTest {
    /** A fixture pack: everything is pl-ru's typed data except the identity the registry keys on. */
    private fun pack(packId: String, target: String): CoursePackFields = object : CoursePackFields by PlRuTypedPack {
        override val id = packId
        override val pairId = packId
        override val targetLanguage = target
    }

    private val plRu = pack("pl-ru", "pl")
    private val enRu = pack("en-ru", "en")
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
     * [packRegistry] too — a typed pack like pl-ru — not only the fixture registry above. Restores pl-ru afterwards (`finally`) so this real singleton's
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
