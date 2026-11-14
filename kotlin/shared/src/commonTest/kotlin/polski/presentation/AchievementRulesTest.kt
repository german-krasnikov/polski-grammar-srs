package polski.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import polski.model.Skill
import polski.progress.SkillStats

class AchievementRulesTest {
    private fun skill(id: String) = Skill(
        id = id, title = "Title $id", group = "g", level = "A1", formula = "f",
        theory = "t", hint = "h", prerequisites = emptyList(),
    )

    @Test fun achievedSkillsIsEmptyWhenNoSkillReachedRungTwo() {
        val skills = listOf(skill("a"))
        val stats = mapOf("a" to SkillStats(tileScaffoldingRung = 1))
        assertTrue(achievedSkills(skills, stats).isEmpty())
    }

    @Test fun achievedSkillsIsEmptyWhenSkillHasNoStatsAtAll() {
        assertTrue(achievedSkills(listOf(skill("a")), emptyMap()).isEmpty())
    }

    @Test fun achievedSkillsIncludesOnlyRungTwoSkillsInOriginalOrder() {
        val skills = listOf(skill("a"), skill("b"), skill("c"))
        val stats = mapOf(
            "a" to SkillStats(tileScaffoldingRung = 2, tileScaffoldingStreak = 3, independentSince = "2026-01-01"),
            "b" to SkillStats(tileScaffoldingRung = 0),
            "c" to SkillStats(tileScaffoldingRung = 2, tileScaffoldingStreak = 1),
        )
        val achieved = achievedSkills(skills, stats)
        assertEquals(listOf("a", "c"), achieved.map { it.skillId })
        assertEquals(3, achieved[0].streak)
        assertEquals("2026-01-01", achieved[0].independentSince)
        assertEquals(1, achieved[1].streak)
        assertEquals(null, achieved[1].independentSince)
    }
}
