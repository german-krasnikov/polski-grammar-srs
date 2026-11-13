// Generated from kotlin/shared/src/commonMain/kotlin/polski/data/PlRuSkillRows.kt by dsl/derive-enru-mappers.mjs: the en-ru copy of that file. Edit the pl-ru file, then run biome generate.
package polski.data

import polski.data.rows.enru.MethodRow
import polski.data.rows.enru.SKILL_ROW_ROWS
import polski.data.rows.enru.SkillRow
import polski.model.Skill
import polski.presentation.NativeParallelPair

/**
 * R1.4a: the en-ru skill rows, generated from courses-biome/en-ru/skills.core-data by `biome data`, as the domain types
 * EnRuTypedPack serves. Hand-written, and it fills in nothing the data does not say: an absent style field is the
 * domain default, `table` and `scene` have no field in the contract (the old generator printed null for both).
 */
internal val generatedEnRuSkills: List<Skill> = SKILL_ROW_ROWS.map { it.toSkill() }
internal val generatedEnRuPresentations: Map<String, SkillPresentation> = SKILL_ROW_ROWS.associate { it.id to it.toPresentation() }
internal val generatedEnRuStyleContent: Map<String, SkillStyleContent> = SKILL_ROW_ROWS.associate { it.id to it.toStyleContent() }

internal fun SkillRow.toSkill() = Skill(id, title, group, level.tag, formula, theory, hint, prerequisites, caseReferenceAvailable ?: true)

internal fun SkillRow.toPresentation() =
    SkillPresentation(focus.before, focus.after, methods.logic.toPresentation(), methods.situations.toPresentation())

private fun MethodRow.toPresentation() = MethodPresentation(introduction, promptLead, introduce, retrieve, feedback, review)

internal fun SkillRow.toStyleContent() = SkillStyleContent(
    rule = styleContent?.rule,
    nativeParallel = styleContent?.nativeParallel.orEmpty().map { NativeParallelPair(it.native, it.target, it.note, it.matches, emptyList()) },
    examples = styleContent?.examples.orEmpty(),
    why = styleContent?.why,
)
