package polski.presentation

/** A presentation preference, not a diagnosis; every style shares the same exercises, FSRS ratings and skill IDs. */
enum class StyleId { RuleFirst, SituationFirst, NativeContrast, MinimalTheory }

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
 * The 4 built-in recipes (UC-10). Data, not language-specific logic: no target/native language
 * is named here. Held as Kotlin literals for now — moving them to JSON files under
 * `courses/styles` is a mechanical, CONTENT-owned follow-up (UC-12), not an architecture change.
 */
object StyleRegistry {
    val recipes: Map<StyleId, StyleRecipe> by lazy {
        listOf(
            StyleRecipe(
                id = StyleId.RuleFirst,
                blocks = mapOf(
                    StylePhase.Front to listOf(BlockKind.Formula, BlockKind.Table),
                    StylePhase.Back to listOf(BlockKind.Changes, BlockKind.Formula, BlockKind.Rule, BlockKind.Contrast),
                ),
            ),
            StyleRecipe(
                id = StyleId.SituationFirst,
                blocks = mapOf(
                    StylePhase.Front to listOf(BlockKind.Scene),
                    StylePhase.Back to listOf(BlockKind.Changes),
                ),
            ),
            StyleRecipe(
                id = StyleId.NativeContrast,
                blocks = mapOf(
                    StylePhase.Front to listOf(BlockKind.NativeParallel),
                    StylePhase.Back to listOf(BlockKind.Changes, BlockKind.NativeParallel, BlockKind.Contrast),
                ),
                requires = setOf(BlockKind.NativeParallel),
                fallback = StyleId.RuleFirst,
            ),
            StyleRecipe(
                id = StyleId.MinimalTheory,
                blocks = mapOf(
                    StylePhase.Front to listOf(BlockKind.Examples),
                    StylePhase.Back to listOf(BlockKind.Changes, BlockKind.WhyOnDemand),
                ),
            ),
        ).associateBy { it.id }.also { registry ->
            registry.values.forEach { recipe ->
                val fallback = recipe.fallback?.let(registry::getValue)
                require(fallback == null || fallback.requires.isEmpty()) {
                    "Fallback ${fallback?.id} for ${recipe.id} must not declare its own requires (no chains)"
                }
            }
        }
    }
}
