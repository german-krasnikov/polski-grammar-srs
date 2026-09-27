package polski.presentation

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import polski.data.generatedStylesJson

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
 * The `courses/styles` recipe files' kebab-case `id`/`fallback` wire value for each [StyleId].
 * Bounded to today's 4 named pedagogical styles (ADR-11): a recipe object whose `id` isn't one of
 * these is skipped by [parseStyleRecipesJson], never a crash — a genuinely new style still needs
 * its own [StyleId] entry, but a stray or in-progress recipe file next to the real 4 never breaks
 * the build.
 */
private val styleIdByWireId: Map<String, StyleId> = mapOf(
    "rule-first" to StyleId.RuleFirst,
    "situation-first" to StyleId.SituationFirst,
    "native-contrast" to StyleId.NativeContrast,
    "minimal-theory" to StyleId.MinimalTheory,
)

/** [BlockKind]'s own fixed lowercase wire name (see [BlockKind]'s doc), inverted for parsing. */
private val blockKindByWireName: Map<String, BlockKind> =
    BlockKind.entries.associateBy { it.name.replaceFirstChar(Char::lowercaseChar) }

/**
 * The built-in recipes (UC-10), loaded from the `courses/styles` recipe files at build time:
 * `:shared`'s `generateCoursePackSource` task inlines every JSON file in that directory into
 * [generatedStylesJson] the same way `course.json` becomes `generatedCourseJson`
 * (`kotlin/shared/build.gradle.kts`). Those files are the single source of truth — editing or
 * adding a recipe there needs no Kotlin change to reach [recipes] (StylesBlueprint.md §2/§4).
 */
object StyleRegistry {
    val recipes: Map<StyleId, StyleRecipe> by lazy {
        parseStyleRecipesJson(generatedStylesJson).associateBy { it.id }.also(::requireNoFallbackChains)
    }
}

/** Never throws: a recipe object naming an `id` or block kind outside today's fixed wire
 *  vocabularies ([styleIdByWireId]/[blockKindByWireName]) is skipped, not a crash. */
internal fun parseStyleRecipesJson(json: String): List<StyleRecipe> =
    Json.parseToJsonElement(json).jsonArray.mapNotNull { it.jsonObject.toStyleRecipeOrNull() }

private fun JsonObject.toStyleRecipeOrNull(): StyleRecipe? {
    val id = styleIdByWireId[getValue("id").jsonPrimitive.content] ?: return null
    val blocks = getValue("blocks").jsonObject
    return StyleRecipe(
        id = id,
        label = getValue("label").jsonObject.toStringMap(),
        description = getValue("description").jsonObject.toStringMap(),
        blocks = mapOf(
            StylePhase.Front to blocks.blockKindList("front"),
            StylePhase.Back to blocks.blockKindList("back"),
        ),
        requires = get("requires")?.jsonArray?.mapNotNull { blockKindByWireName[it.jsonPrimitive.content] }?.toSet() ?: emptySet(),
        fallback = get("fallback")?.jsonPrimitive?.content?.let(styleIdByWireId::get),
    )
}

private fun JsonObject.blockKindList(key: String): List<BlockKind> =
    getValue(key).jsonArray.mapNotNull { blockKindByWireName[it.jsonPrimitive.content] }

private fun JsonObject.toStringMap(): Map<String, String> = mapValues { (_, value) -> value.jsonPrimitive.content }

private fun requireNoFallbackChains(registry: Map<StyleId, StyleRecipe>) {
    registry.values.forEach { recipe ->
        val fallback = recipe.fallback?.let(registry::get)
        require(fallback == null || fallback.requires.isEmpty()) {
            "Fallback ${fallback?.id} for ${recipe.id} must not declare its own requires (no chains)"
        }
    }
}
