package polski.presentation

import polski.data.SkillPresentation
import polski.data.SkillStyleContent
import polski.model.Exercise
import polski.model.Skill

/**
 * Turns a [StyleRecipe] plus per-skill content into the [Block]s one phase shows. Pure: the same
 * (style, phase, exercise, skill, focus, content) always yields the same blocks. Never reads FSRS,
 * progress or preferences, and never creates a review — switching style is a display choice.
 */
object StyleComposer {
    /**
     * A style whose [StyleRecipe.requires] isn't met by [content] resolves to its declared
     * [StyleRecipe.fallback]; a style without unmet requirements resolves to itself. Never throws:
     * a [style] missing from [registry] (or naming a fallback missing from it) resolves to itself.
     */
    fun resolveEffectiveStyle(style: StyleRecipe, content: SkillStyleContent, registry: Map<StyleId, StyleRecipe>): StyleId {
        if (style.requires.all { it.isSatisfiedBy(content) }) return style.id
        val fallbackId = style.fallback ?: return style.id
        return registry[fallbackId]?.id ?: style.id
    }

    /** [style] should already be the resolved (post-fallback) recipe. */
    fun compose(
        style: StyleRecipe,
        phase: StylePhase,
        exercise: Exercise,
        skill: Skill,
        focus: SkillPresentation,
        content: SkillStyleContent,
    ): List<Block> = style.blocks[phase].orEmpty().mapNotNull { kind -> block(kind, exercise, skill, focus, content) }

    private fun block(kind: BlockKind, exercise: Exercise, skill: Skill, focus: SkillPresentation, content: SkillStyleContent): Block? =
        when (kind) {
            BlockKind.Formula -> Block.Formula(skill.formula)
            BlockKind.Rule -> Block.Rule(content.rule ?: skill.theory)
            BlockKind.Table -> Block.Table("", content.table ?: listOf(derivedTableRow(focus)))
            BlockKind.Scene -> Block.Scene(content.scene ?: focus.situations.introduce)
            BlockKind.NativeParallel -> content.nativeParallel.takeIf { it.isNotEmpty() }?.let(Block::NativeParallel)
            BlockKind.Examples -> Block.Examples(content.examples)
            BlockKind.WhyOnDemand -> Block.WhyOnDemand(content.why ?: skill.theory)
            BlockKind.Changes -> Block.Changes(exercise.changes.map { change ->
                ChangeItem(
                    changeHighlightParts(change.from, change.to, ChangeSide.Before),
                    changeHighlightParts(change.from, change.to, ChangeSide.After),
                    change.reason,
                )
            })
            BlockKind.Contrast -> Block.Contrast(
                changeHighlightParts(focus.focusBefore, focus.focusAfter, ChangeSide.Before),
                changeHighlightParts(focus.focusBefore, focus.focusAfter, ChangeSide.After),
            )
        }

    private fun derivedTableRow(focus: SkillPresentation): TableRow {
        val pair = ContrastPair.generated(focus.focusBefore, focus.focusAfter)
        return TableRow("Было → Стало", pair.parts(ChangeSide.Before), pair.parts(ChangeSide.After))
    }

    /** Every [BlockKind] but [BlockKind.NativeParallel] is always derivable (see [SkillStyleContent]'s doc). */
    private fun BlockKind.isSatisfiedBy(content: SkillStyleContent): Boolean =
        this != BlockKind.NativeParallel || content.nativeParallel.isNotEmpty()
}
