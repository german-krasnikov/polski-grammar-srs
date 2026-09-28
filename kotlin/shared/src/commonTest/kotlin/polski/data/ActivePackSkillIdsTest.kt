package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.pack.EmbeddedCoursePackSource

/**
 * EN-10 (Plans/Kotlin/EnRuPackPlan.md §6): [activePackSkillIds] is the real predicate
 * [polski.progress.SkillQueue]'s `activePackFilter` was declared for -- read fresh from
 * [PackRegistry.active] on every call, unlike [skills]'s frozen `by lazy` snapshot, so a later
 * [PackRegistry.select] (still unwired to any host, EN-22) takes effect without a new
 * [polski.presentation.TrainingStore]. pl-ru's own skill ids stay bare; any later pack's own
 * curriculum brings its `${target}:${skillId}` namespace already baked into its ids
 * (UniversalCorePlan.md §6), so this function needs no per-language branch.
 */
class ActivePackSkillIdsTest {
    private fun packJson(pairId: String, target: String, skillId: String): String =
        """{"schemaVersion":1,"id":"$pairId","targetLanguage":"$target","nativeLanguage":"ru",""" +
            """"skills":[{"id":"$skillId","title":"t","group":"g","level":"A1","formula":"f",""" +
            """"theory":"th","hint":"h","prerequisites":[]}]}"""

    private val plRu = CoursePack(EmbeddedCoursePackSource("pl-ru", packJson("pl-ru", "pl", "case.acc.f")))
    private val enRu = CoursePack(EmbeddedCoursePackSource("en-ru", packJson("en-ru", "en", "en:role.object")))
    private val registry = PackRegistry(listOf(plRu, enRu))

    @Test
    fun readsTheDefaultActivePacksBareIds() {
        assertEquals(setOf("case.acc.f"), activePackSkillIds(registry))
    }

    @Test
    fun followsALaterSelectInsteadOfStayingFrozen() {
        registry.select("en-ru")
        assertEquals(setOf("en:role.object"), activePackSkillIds(registry))
    }

    /** Regression pin: the production singleton, never `.select()`-ed, still yields exactly what
     *  [skills] already gave every existing pl-ru caller -- this task changes no pl-ru behavior. */
    @Test
    fun productionRegistryStillYieldsPlRusBareIdsUnaffected() {
        assertEquals(skills.map { it.id }.toSet(), activePackSkillIds(packRegistry))
    }
}
