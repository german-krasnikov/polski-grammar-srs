package polski.data

import polski.data.rows.plru.SKILL_ROW_ROWS

/** NJ03 B3 pilot reader (R1.4a): the 16 pl-ru skills as the generated rows carry them, the same on every host. It had a private parser of its own. */
internal data class CourseValueSkill(
    val id: String,
    val title: String,
    val group: String,
    val level: String,
    val prerequisites: List<String>,
)

internal fun courseValueSkills(): List<CourseValueSkill> =
    SKILL_ROW_ROWS.map { CourseValueSkill(it.id, it.title, it.group, it.level.tag, it.prerequisites) }
