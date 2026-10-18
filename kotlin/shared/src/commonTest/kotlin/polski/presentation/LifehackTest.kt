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
 * `pairs/<pairId>/lifehacks.json` (ADR-15: lifehacks are pair-scoped, outside core).
 */
class LifehackTest {
    // NJ03: typed lifehacks from GeneratedLifehacks.kt — verify structure.
    @Test fun typedLifehacksHaveExpectedStructure() {
        val enRu = polski.data.generatedLifehacksByPairId["en-ru"] ?: error("en-ru lifehacks missing")
        assertTrue(enRu.isNotEmpty(), "en-ru should have lifehacks")
        val first = enRu.first()
        assertTrue(first.id.isNotEmpty(), "id should not be empty")
        assertTrue(first.text.isNotEmpty(), "text should not be empty")
        assertTrue(first.source.citation.isNotEmpty(), "citation should not be empty")
        val plRu = polski.data.generatedLifehacksByPairId["pl-ru"] ?: error("pl-ru lifehacks missing")
        assertTrue(plRu.isNotEmpty(), "pl-ru should have lifehacks")
    }
    @Test fun parserNeverThrowsOnAnEmptyLifehackList() {
        val json = """{"schemaVersion":1,"pairId":"en-ru","lifehacks":[]}"""
    }

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

    // A cross-skill lifehack (skillId == null) must never surface through forSkill(realId) —
    // §4.1 reserves null+topic for cross-skill advice the per-skill card doesn't render (EN-21 web
    // scope is the per-skill collapsible block only).
    @Test fun aCrossSkillLifehackNeverMatchesAnyRealSkillId() {
        val json = """
            {"schemaVersion":1,"pairId":"en-ru","lifehacks":[
              {"id":"x","skillId":null,"topic":"t","text":"t","source":{"kind":"project-authored","citation":"c"},"status":"editorial","votes":{"helpful":0,"notHelpful":0}}
            ]}
        """.trimIndent()
    }

    // §4.2: a cheap existence check for a front-side badge — must agree with forSkill's own
    // emptiness, on both a covered and an uncovered skill, without pretending it's a mock.
    @Test fun hasLifehacksAgreesWithForSkillsEmptiness() {
        assertTrue(StaticPackLifehackProvider.hasLifehacks("case.gen.neg"))
        assertEquals(false, StaticPackLifehackProvider.hasLifehacks("no.such.skill.exists"))
    }

    // §4.3's pack-wide "Лайфхаки" listing: pl-ru's groups come back in curriculum
    // order (Curriculum.kt/pair.json's own skills[] order), not lifehacks.json's authoring order —
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
        assertEquals(16, StaticPackLifehackProvider.listAll().size)
    }
}
