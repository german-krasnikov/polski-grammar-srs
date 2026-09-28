package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.pack.EmbeddedCoursePackSource

/**
 * EN-06 (Plans/Kotlin/EnRuPackPlan.md §6): [PackRegistry] must offer a real active-pack switch
 * keyed by [CoursePack.pairId], while pl-ru stays the default active pack when nothing selects
 * otherwise — the real production registry ([packRegistry]) never calls [PackRegistry.select]
 * yet, so its pl-ru behavior is unaffected by this task.
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
}
