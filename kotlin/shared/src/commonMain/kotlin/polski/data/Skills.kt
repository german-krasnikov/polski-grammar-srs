package polski.data

import polski.model.Skill

/**
 * EN-22: was `by lazy` (frozen at first access); now read fresh on every call so
 * [PackRegistry.select] — wired to a real host picker as of this task — takes effect immediately,
 * matching [activePackSkillIds] below which already reads fresh for the same reason.
 */
// NJ03 B5: pl-ru reads from typed Biome literal source; other packs use JSON path.
val skills: List<Skill> get() = if (packRegistry.active.id == "pl-ru") generatedPlRuSkills
    else packRegistry.active.skills

fun skillById(id: String): Skill = skills.firstOrNull { it.id == id } ?: error("Unknown skill $id")

/**
 * EN-10 (Plans/Kotlin/EnRuPackPlan.md §6): the real predicate [polski.progress.SkillQueue]'s
 * `activePackFilter` was declared for -- read fresh from [registry] on every call, unlike
 * [skills]'s frozen `by lazy` snapshot, so a later [PackRegistry.select] (still unwired to any
 * host, EN-22) takes effect without a new [polski.presentation.TrainingStore]. pl-ru's own skill
 * ids stay bare; any later pack's own curriculum brings its `${target}:${skillId}` namespace
 * already baked into its ids, so this needs no per-language branch.
 */
internal fun activePackSkillIds(registry: PackRegistry): Set<String> = registry.active.skills.map { it.id }.toSet()

// NJ03: pl-ru reads from typed Biome literal source.
fun presentationBySkillId(id: String): SkillPresentation =
    if (packRegistry.active.id == "pl-ru") generatedPlRuPresentations[id] ?: error("Unknown skill presentation $id")
    else packRegistry.active.presentations[id] ?: error("Unknown skill presentation $id")

fun styleContentBySkillId(id: String): SkillStyleContent =
    if (packRegistry.active.id == "pl-ru") generatedPlRuStyleContent[id] ?: SkillStyleContent()
    else packRegistry.active.styleContent[id] ?: SkillStyleContent()
