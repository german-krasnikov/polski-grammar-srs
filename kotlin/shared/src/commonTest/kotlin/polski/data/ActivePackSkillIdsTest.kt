package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.model.Skill

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
    /** A fixture pack: pl-ru's typed data with its own identity and one skill. */
    private fun pack(packId: String, target: String, skillId: String): CoursePackFields = object : CoursePackFields by PlRuTypedPack {
        override val id = packId
        override val pairId = packId
        override val targetLanguage = target
        override val skills = listOf(Skill(skillId, "t", "g", "A1", "f", "th", "h", emptyList()))
    }

    private val plRu = pack("pl-ru", "pl", "case.acc.f")
    private val enRu = pack("en-ru", "en", "en:role.object")
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
