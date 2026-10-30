package polski.core

import polski.core.engine.ChainStepRecipe
import polski.core.engine.ConstantSlot
import polski.core.engine.SkillRecipe

/**
 * UniversalCorePlan.md §3.1/§5.1/§12 UC-07/UC-08: the recipes of one pack, as `:core-engine` takes them — the [SkillRecipe] of each skill, the chain's [ChainStepRecipe]s and the
 * pack's own defaults. A pack's rows make it (PlRuRecipeRows.kt, and its copy EnRuRecipeRows.kt); every Polish, English or Russian string in it is an opaque copy key, never interpreted here.
 */
data class RecipeSet(
    val skills: Map<String, SkillRecipe>,
    val chain: List<ChainStepRecipe>,
    val firstSkillByGender: Map<String, String>,
    /** The pack's own defaults (UniversalCorePlan.md §1.3 principle #3) — never assumed by `:core-engine` itself. */
    val defaultOwnerLexeme: String,
    val verbLexeme: String,
    /** EnRuPackPlan.md §5 gap A/EN-14: further pack-constant lexical slots beyond `owner`/`verb` (`prep`/`aux`/`neg`/...), each present unconditionally or conditionally per [ConstantSlot]. Empty for pl-ru. */
    val constantSlots: List<ConstantSlot> = emptyList(),
)
