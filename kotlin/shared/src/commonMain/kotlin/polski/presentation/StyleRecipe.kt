package polski.presentation

import polski.data.generatedStyleRecipes

/**
 * A presentation preference, not a diagnosis; every style shares the same exercises, FSRS ratings
 * and skill IDs. UC-01 opened this from a closed enum to a string-backed id (UniversalCorePlan.md
 * §1.1, "an open catalog, not a closed enum") so a 5th style needs only a new row in
 * `courses-biome/styles/styles.core-data` — no Kotlin change reaches [StyleRegistry]. [value] is the
 * PascalCase wire vocabulary every host bridge already speaks (Swift `styleId`/`"styles"` snapshot
 * rows, web/Android pickers) — the 4 built-in constants below keep the exact strings the old enum's
 * `.name` produced, so no existing host wire format changes; polski.data.StyleRows.kt derives the same
 * shape from the kebab-case id of any new row, so nothing but that file needs to know the transform.
 */
data class StyleId(val value: String) {
    companion object {
        val RuleFirst = StyleId("RuleFirst")
        val SituationFirst = StyleId("SituationFirst")
        val NativeContrast = StyleId("NativeContrast")
        val MinimalTheory = StyleId("MinimalTheory")
    }
}

/**
 * The 4 built-in styles in the old closed enum's declaration order — hosts that show a fixed,
 * ordered picker (Settings, quick-switch) keep exactly today's option order by iterating this
 * instead of [StyleRegistry.recipes]' keys, whose order follows the rows of `styles.core-data`
 * (alphabetical by id today) and grows with any new row. Iterating [StyleRegistry.recipes] directly is
 * right where "any currently loaded style" is the point (dispatch validation, exhaustive tests).
 */
val builtInStyleIds: List<StyleId> = listOf(StyleId.RuleFirst, StyleId.SituationFirst, StyleId.NativeContrast, StyleId.MinimalTheory)

/** [CardPhase.Question]/[CardPhase.Revealed] under the names blocks are grouped by. */
enum class StylePhase { Front, Back }

/** Fixed lowercase wire names (`formula|table|rule|scene|nativeParallel|examples|whyOnDemand|changes|contrast`)
 *  are `name.replaceFirstChar { it.lowercase() }` — see [presentation.blocksToJson]. */
enum class BlockKind { Formula, Table, Rule, Scene, NativeParallel, Examples, WhyOnDemand, Changes, Contrast }

/**
 * A presentation recipe: which [BlockKind]s appear per [StylePhase]. [requires] names the kinds
 * that need real per-skill authored content; a skill missing it resolves (see [StyleComposer])
 * to [fallback] instead of showing an empty block or crashing. A recipe named as someone else's
 * [fallback] must declare an empty [requires] itself — one level, never a chain.
 */
data class StyleRecipe(
    val id: StyleId,
    val label: Map<String, String> = emptyMap(),
    val description: Map<String, String> = emptyMap(),
    val blocks: Map<StylePhase, List<BlockKind>>,
    val requires: Set<BlockKind> = emptySet(),
    val fallback: StyleId? = null,
)

/**
 * The built-in recipes (UC-10), the rows of `courses-biome/styles/styles.core-data` (R1.18; polski.data.StyleRows.kt makes them). That file is the single source of truth — adding a row
 * there needs no Kotlin change to reach [recipes] (StylesBlueprint.md §2/§4).
 */
object StyleRegistry {
    val recipes: Map<StyleId, StyleRecipe> by lazy {
        generatedStyleRecipes.associateBy { it.id }.also(::requireNoFallbackChains)
    }
}

private fun requireNoFallbackChains(registry: Map<StyleId, StyleRecipe>) {
    registry.values.forEach { recipe ->
        val fallback = recipe.fallback?.let(registry::get)
        require(fallback == null || fallback.requires.isEmpty()) {
            "Fallback ${fallback?.id} for ${recipe.id} must not declare its own requires (no chains)"
        }
    }
}
