package polski.presentation

import polski.model.Skill
import polski.progress.SkillStats

/** M6 blueprint: one entry per skill currently at `tileScaffoldingRung == 2` ("раньше с таблицей,
 *  теперь сам" — least tile-builder scaffolding help). [independentSince] mirrors
 *  [SkillStats.independentSince] and can still be `null` for a skill that reached the rung before
 *  M6 shipped and hasn't been promoted again since. */
data class SkillAchievement(val skillId: String, val streak: Int, val independentSince: String?)

/**
 * Skills from [skills] currently at `tileScaffoldingRung == 2`, in [skills] order. Extracted from
 * the web-only `ProgressWeb.renderAchievementsSection` filter, ahead of a second (Android/iOS)
 * caller, so the threshold is defined once. Empty list ⇒ caller shows no achievements section.
 */
fun achievedSkills(skills: List<Skill>, stats: Map<String, SkillStats>): List<SkillAchievement> =
    skills.filter { stats[it.id]?.tileScaffoldingRung == 2 }
        .map { skill -> stats.getValue(skill.id).let { SkillAchievement(skill.id, it.tileScaffoldingStreak, it.independentSince) } }
