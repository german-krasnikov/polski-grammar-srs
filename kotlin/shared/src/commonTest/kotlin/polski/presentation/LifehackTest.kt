package polski.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import polski.data.selectCoursePack
import polski.data.skills

/**
 * EN-21 (`Plans/Kotlin/EnRuPackPlan.md` §4.2/§6): [LifehackProvider] is the reserved port
 * (`Plans/Kotlin/UniversalCorePlan.md` §5.4's `ExplanationProvider`/`AudioProvider` pattern),
 * [StaticPackLifehackProvider] its only implementation today — reads the active pack's
 * `courses-biome/<pairId>/lifehacks.core-data` (ADR-15: lifehacks are pair-scoped, outside core). The rows and the mapper are LifehackDataTest's and LifehackRowsTest's.
 */
class LifehackTest {
    // Real content, both packs: pl-ru's first authored 5 (EN-20) prove the port isn't en-ru-only.
    @Test fun staticProviderReturnsPlRusRealAuthoredLifehackForCaseGenNeg() {
        val hacks = StaticPackLifehackProvider.forSkill("case.gen.neg")
        assertTrue(hacks.isNotEmpty(), "case.gen.neg should have pl-ru's EN-20 authored lifehack")
        assertTrue(hacks.all { it.skillId == "case.gen.neg" })
        assertEquals(LifehackStatus.Editorial, hacks.first().status)
    }

    // Empty, not a crash and not a placeholder entry — §4.3's "пусто -> блок не рисуется вообще".
    @Test fun staticProviderReturnsEmptyForASkillWithNoAuthoredLifehack() {
        assertEquals(emptyList(), StaticPackLifehackProvider.forSkill("no.such.skill.exists"))
    }

    // §4.2: a cheap existence check for a front-side badge — must agree with forSkill's own
    // emptiness, on both a covered and an uncovered skill, without pretending it's a mock.
    @Test fun hasLifehacksAgreesWithForSkillsEmptiness() {
        assertTrue(StaticPackLifehackProvider.hasLifehacks("case.gen.neg"))
        assertEquals(false, StaticPackLifehackProvider.hasLifehacks("no.such.skill.exists"))
    }

    // §4.3's pack-wide "Лайфхаки" listing: pl-ru's groups come back in curriculum
    // order (the order of the skill rows), not the order of the lifehack rows —
    // and each group is titled by that skill's own real display title, never a made-up label.
    @Test fun listAllOrdersPlRuGroupsByCurriculumAndNamesEachByItsRealSkillTitle() {
        val groups = StaticPackLifehackProvider.listAll()
        assertEquals(skills.map { it.id }, groups.map { it.skillId }, "pl-ru covers every skill, so order is exactly curriculum order")
        groups.forEach { group ->
            assertEquals(skills.first { it.id == group.skillId }.title, group.title)
            assertTrue(group.lifehacks.isNotEmpty())
            assertTrue(group.lifehacks.all { it.skillId == group.skillId })
        }
    }

    // Switching the active pack (EN-22) must switch what both hasLifehacks and listAll answer —
    // the same live-`packRegistry.active` rule forSkill already follows — and pl-ru is restored
    // afterwards, pass or fail, same as every other pack-switching test in this suite.
    @Test fun switchingTheActivePackSwitchesHasLifehacksAndListAll() {
        try {
            selectCoursePack("en-ru")
            assertTrue(StaticPackLifehackProvider.hasLifehacks("en:role.object"))
            assertEquals(false, StaticPackLifehackProvider.hasLifehacks("case.gen.neg"))
            val groups = StaticPackLifehackProvider.listAll()
            assertEquals(16, groups.size, "en-ru's 32 authored lifehacks cover all 16 skills")
            assertEquals(skills.map { it.id }, groups.map { it.skillId }, "en-ru covers every skill, so order is exactly curriculum order")
            groups.forEach { group -> assertEquals(skills.first { it.id == group.skillId }.title, group.title) }
        } finally {
            selectCoursePack("pl-ru")
        }
        // Restored: pl-ru's own answers are unchanged after the round trip.
        assertTrue(StaticPackLifehackProvider.hasLifehacks("case.gen.neg"))
        assertEquals(17, StaticPackLifehackProvider.listAll().size)
    }
}
