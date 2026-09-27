package polski.presentation

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import polski.data.generatedStylesJson

/**
 * A presentation preference, not a diagnosis; every style shares the same exercises, FSRS ratings
 * and skill IDs. UC-01 opened this from a closed enum to a string-backed id (UniversalCorePlan.md
 * §1.1, "an open catalog, not a closed enum") so a 5th style needs only a new recipe file under
 * `courses/styles` — no Kotlin change reaches [StyleRegistry] (see [parseStyleRecipesJson]). [value] is the
 * PascalCase wire vocabulary every host bridge already speaks (Swift `styleId`/`"styles"` snapshot
 * rows, web/Android pickers) — the 4 built-in constants below keep the exact strings the old enum's
 * `.name` produced, so no existing host wire format changes; [kebabIdToWireValue] derives the same
 * shape for any new recipe id, so nothing but this file needs to know the transform.
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
 * instead of [StyleRegistry.recipes]' keys, whose order follows `courses/styles` filenames
 * (alphabetical) and grows with any new recipe. Iterating [StyleRegistry.recipes] directly is
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
 * `courses/styles` recipe file ids are kebab-case (`native-contrast`); every host bridge speaks the
 * PascalCase form the old closed enum's `.name` produced (`NativeContrast`) — this mechanical
 * transform is the only thing that used to live in a fixed id-lookup table, so a brand-new recipe
 * (UC-01: "a 5th style needs only a new JSON file") gets a brand-new [StyleId] the same way the 4
 * built-in ones do, with no table to extend.
 */
private fun String.kebabIdToWireValue(): String = split('-').joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }

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

/**
 * Never throws: a recipe's `id` is open (UC-01) — every well-formed recipe object is loaded, not
 * just today's known 4. A block kind outside [BlockKind]'s fixed wire vocabulary is skipped within
 * that recipe (`requires`/`blocks`), not a crash — [BlockKind] is a closed set on purpose (§5.2's
 * ~10-operator ceiling has no open-catalog equivalent yet).
 */
internal fun parseStyleRecipesJson(json: String): List<StyleRecipe> =
    Json.parseToJsonElement(json).jsonArray.mapNotNull { it.jsonObject.toStyleRecipeOrNull() }

private fun JsonObject.toStyleRecipeOrNull(): StyleRecipe? {
    val id = StyleId(getValue("id").jsonPrimitive.content.kebabIdToWireValue())
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
        fallback = get("fallback")?.jsonPrimitive?.content?.let { StyleId(it.kebabIdToWireValue()) },
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
