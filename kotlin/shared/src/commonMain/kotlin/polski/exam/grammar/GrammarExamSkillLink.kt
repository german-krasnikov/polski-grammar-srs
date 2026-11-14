package polski.exam.grammar

import polski.exam.grammar.data.EXAM_SKILL_LINK_ROW_ROWS

/** M10 blueprint: author-curated `itemId -> skillId` links (`dsl/exam-skill-links.data-contract`,
 *  `courses-biome/pl-ru/exam-skill-links.core-data`) — a real exam grammar item worth redirecting
 *  a learner to course practice for, after they miss it. */
private val linkedSkillByItemId: Map<String, String> by lazy {
    EXAM_SKILL_LINK_ROW_ROWS.associate { it.itemId to it.skillId }
}

/** The other side of the same lookup, grouped so [verificationItemFor] can find a sibling item. */
private val linkedItemIdsBySkillId: Map<String, List<String>> by lazy {
    EXAM_SKILL_LINK_ROW_ROWS.groupBy(keySelector = { it.skillId }, valueTransform = { it.itemId })
}

/** The linked skill for [itemId], or `null` when this exam item has none authored yet
 *  (fail-soft: an unlinked item simply shows no "practice this skill" affordance). */
fun skillIdFor(itemId: String): String? = linkedSkillByItemId[itemId]

/**
 * A different item sharing [skillId] (not [excludeItemId]) to use as the post-training
 * verification question — `null` when [skillId] has no second linked item yet, in which case the
 * host must show an explicit "not enough linked questions yet" state (Learning Plan KPI note:
 * repeating the disclosed question doesn't count as transfer), never silently reuse
 * [excludeItemId].
 */
fun verificationItemFor(skillId: String, excludeItemId: String): String? =
    linkedItemIdsBySkillId[skillId].orEmpty().firstOrNull { it != excludeItemId }
