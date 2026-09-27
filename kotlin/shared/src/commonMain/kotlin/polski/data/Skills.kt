package polski.data

import polski.model.Skill

val skills: List<Skill> by lazy { packRegistry.active.skills }

fun skillById(id: String): Skill = skills.firstOrNull { it.id == id } ?: error("Unknown skill $id")

fun presentationBySkillId(id: String): SkillPresentation =
    packRegistry.active.presentations[id] ?: error("Unknown skill presentation $id")

/** Absent styleContent for [id] (true for every skill today) means [SkillStyleContent]'s all-derived defaults. */
fun styleContentBySkillId(id: String): SkillStyleContent =
    packRegistry.active.styleContent[id] ?: SkillStyleContent()
