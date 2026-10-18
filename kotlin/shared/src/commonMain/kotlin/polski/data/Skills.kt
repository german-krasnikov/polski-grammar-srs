package polski.data

import polski.model.Skill

/** EN-22: read fresh. pl-ru returns PlRuTypedPack fields — one override point (NJ03 P7). */
val skills: List<Skill> get() = packRegistry.active.skills

fun skillById(id: String): Skill = skills.firstOrNull { it.id == id } ?: error("Unknown skill $id")

internal fun activePackSkillIds(registry: PackRegistry): Set<String> =
    registry.active.skills.map { it.id }.toSet()

fun presentationBySkillId(id: String): SkillPresentation =
    packRegistry.active.presentations[id] ?: error("Unknown skill presentation $id")

fun styleContentBySkillId(id: String): SkillStyleContent =
    packRegistry.active.styleContent[id] ?: SkillStyleContent()
