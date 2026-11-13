package polski.presentation

import polski.core.model.TileError
import polski.data.SkillPresentation
import polski.data.SkillStyleContent
import polski.model.Exercise
import polski.model.FormChange
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

    /**
     * SB-7: the per-tile "what went wrong" panel filled by `TrainingStore.errorExplanationFor` on
     * [polski.presentation.TileAction.TapError]. Same purity contract as [compose]: pure, trusts
     * [error] belongs to [exercise]'s own `TileExercise` (structurally guaranteed by the caller),
     * never throws. Always exactly 2 blocks: the skill-level "why" (reusing [ruleBlock] so it never
     * drifts from [compose]'s own [BlockKind.Rule] branch), then the specific diff for this tile —
     * [error.selected]/[error.expected] word-aligned via [changeHighlightParts], with [ChangeItem.reason]
     * taken from the [exercise.changes] entry whose [FormChange.to] contains [error.expected]
     * (substring match, not identity — tolerant of future per-tile-slot `changes` splits), falling
     * back to the first (or only) entry, and to [exercise.explanation] if [exercise.changes] is empty.
     */
    fun blocksForError(
        exercise: Exercise,
        skill: Skill,
        focus: SkillPresentation,
        content: SkillStyleContent,
        error: TileError,
    ): List<Block> {
        val reason = matchingChange(exercise, error)?.reason ?: exercise.explanation
        return listOf(
            ruleBlock(exercise, skill, focus, content),
            Block.Changes(listOf(ChangeItem(
                changeHighlightParts(error.selected, error.expected, ChangeSide.Before),
                changeHighlightParts(error.selected, error.expected, ChangeSide.After),
                reason,
            ))),
        )
    }

    private fun matchingChange(exercise: Exercise, error: TileError): FormChange? =
        exercise.changes.firstOrNull { it.to.contains(error.expected) } ?: exercise.changes.firstOrNull()

    private fun ruleBlock(exercise: Exercise, skill: Skill, focus: SkillPresentation, content: SkillStyleContent): Block.Rule =
        (content.rule ?: skill.theory).let { text -> Block.Rule(text, exercise.explanation, styleParts(text, focus)) }

    private fun block(kind: BlockKind, exercise: Exercise, skill: Skill, focus: SkillPresentation, content: SkillStyleContent): Block? =
        when (kind) {
            BlockKind.Formula -> Block.Formula(skill.formula, styleParts(skill.formula, focus))
            BlockKind.Rule -> ruleBlock(exercise, skill, focus, content)
            BlockKind.Table -> Block.Table("", content.table ?: listOf(derivedTableRow(focus)))
            BlockKind.Scene -> (content.scene ?: focus.situations.introduce).let { text -> Block.Scene(text, styleParts(text, focus)) }
            BlockKind.NativeParallel -> content.nativeParallel.takeIf { it.isNotEmpty() }
                ?.map { pair -> pair.copy(targetParts = styleParts(pair.target, focus)) }
                ?.let(Block::NativeParallel)
            BlockKind.Examples -> Block.Examples(content.examples, content.examples.map { text -> styleParts(text, focus) })
            BlockKind.WhyOnDemand -> (content.why ?: skill.theory).let { text -> Block.WhyOnDemand(text, styleParts(text, focus)) }
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

    /** [styleTextHighlightParts] against the skill's own `focus.before`/`focus.after` — the same
     *  explicit, non-exercise-specific pair [Block.Table]/[Block.Contrast] already draw from, so a
     *  style block's prose never carries the current exercise's own answer (EmphasisUXAudit E7/S4,
     *  Emphasis contract §5: no leak, front or back). */
    private fun styleParts(text: String, focus: SkillPresentation): List<EndingPart> =
        styleTextHighlightParts(text, focus.focusBefore, focus.focusAfter)

    /** Label is "" (no per-skill [SkillStyleContent.table] to draw it from) — see UC-09 for a real
     *  multi-row table; CORE names no language here, so it never guesses a caption like "before → after". */
    private fun derivedTableRow(focus: SkillPresentation): TableRow {
        val pair = ContrastPair.generated(focus.focusBefore, focus.focusAfter)
        return TableRow("", pair.parts(ChangeSide.Before), pair.parts(ChangeSide.After))
    }

    /** Every [BlockKind] but [BlockKind.NativeParallel] is always derivable (see [SkillStyleContent]'s doc). */
    private fun BlockKind.isSatisfiedBy(content: SkillStyleContent): Boolean =
        this != BlockKind.NativeParallel || content.nativeParallel.isNotEmpty()
}
