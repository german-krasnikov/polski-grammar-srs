package polski.data

import polski.model.Skill

val skills: List<Skill> by lazy { packRegistry.active.skills }

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

// EN-22: cached once at first access, the same `by lazy`-on-`packRegistry.active` pattern [skills]
// just above already uses — not re-read on every call, so a live [PackRegistry.select] away from
// pl-ru can't turn the very next card render into an "Unknown skill presentation" crash for a pl
// skill id another pack's own data doesn't have (training content isn't pack-aware yet).
private val presentations: Map<String, SkillPresentation> by lazy { packRegistry.active.presentations }
private val styleContentMap: Map<String, SkillStyleContent> by lazy { packRegistry.active.styleContent }

fun presentationBySkillId(id: String): SkillPresentation =
    presentations[id] ?: error("Unknown skill presentation $id")

/** Absent styleContent for [id] (true for every skill today) means [SkillStyleContent]'s all-derived defaults. */
fun styleContentBySkillId(id: String): SkillStyleContent =
    styleContentMap[id] ?: SkillStyleContent()
