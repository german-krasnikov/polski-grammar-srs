package polski.data

import polski.core.model.FeatureFocus
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.core.model.LexicalFilter
import polski.core.model.SkillSpec
import polski.data.rows.plru.SKILL_ROW_ROWS
import polski.data.rows.plru.SkillRow

/**
 * R1.4b: the pl-ru [SkillSpec]s, read from the same rows as the skills — level and prerequisites are written once.
 * The first access fails if the prerequisites form a cycle, which the data contract cannot see.
 */
internal val generatedPlRuSkillSpecs: List<SkillSpec> = SKILL_ROW_ROWS.map { it.toSkillSpec() }
    .also { specs -> requireAcyclic(specs.associate { it.id to it.prerequisites }) }

internal fun SkillRow.toSkillSpec(): SkillSpec {
    val fixedFeatures = fixed.associate { FeatureKey(it.feature) to FeatureValue(it.value) }
    require(fixedFeatures.size == fixed.size) { "$id: a fixed feature is written twice" }
    return SkillSpec(
        id = id,
        construction = construction,
        focus = featureFocus?.let { FeatureFocus(FeatureKey(it.feature), FeatureValue(it.from), FeatureValue(it.to)) },
        fixed = fixedFeatures,
        lexicalFilter = lexicalFilter?.let { filter ->
            LexicalFilter(filter.slot, buildMap {
                filter.where.nounId?.let { put("nounId", it) }
                filter.where.gender?.let { put("gender", it) }
            })
        },
        level = level.tag,
        prerequisites = prerequisites,
    )
}
