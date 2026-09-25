package polski.training

/** Due time is an absolute epoch millisecond value. Input order breaks equal-time ties. */
data class DueSkillCard(val skillId: String, val dueEpochMillis: Long)

/** Focused selection wins when present, otherwise returns the earliest card regardless of now. */
fun nextSkillId(cards: List<DueSkillCard>, selected: String? = null): String {
    if (selected != null && cards.any { it.skillId == selected }) return selected
    return cards.withIndex().minWithOrNull(compareBy<IndexedValue<DueSkillCard>> { it.value.dueEpochMillis }.thenBy { it.index })
        ?.value?.skillId ?: error("No skills available")
}
