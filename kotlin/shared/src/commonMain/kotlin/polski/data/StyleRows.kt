package polski.data

import polski.data.rows.styles.BlockKindRow
import polski.data.rows.styles.STYLE_RECIPE_ROW_ROWS
import polski.data.rows.styles.StyleRecipeRow
import polski.presentation.BlockKind
import polski.presentation.StyleId
import polski.presentation.StylePhase
import polski.presentation.StyleRecipe

/**
 * R1.18: the built-in presentation styles, from the rows of courses-biome/styles/styles.core-data in the order of the file. The id is open (UC-01): a new row is a new style, with no Kotlin
 * change. A row is refused, not repaired, when a kind is shown twice in one phase or in both: the card still shows the front blocks after the reveal, so it would be drawn twice (C3).
 */
internal val generatedStyleRecipes: List<StyleRecipe> = STYLE_RECIPE_ROW_ROWS.toStyleRecipes()

internal fun List<StyleRecipeRow>.toStyleRecipes(): List<StyleRecipe> = map { row ->
    val front = row.front.map { it.toBlockKind() }
    val back = row.back.map { it.toBlockKind() }
    val requires = row.requires.orEmpty().map { it.toBlockKind() }
    val repeated = (front + back).groupingBy { it }.eachCount().filterValues { it > 1 }.keys
    require(repeated.isEmpty()) { "${row.id}: $repeated shown twice (within front, within back or on both)" }
    require(requires.distinct().size == requires.size) { "${row.id}: requires lists a kind twice" }
    StyleRecipe(
        id = row.id.toStyleId(),
        label = mapOf("ru" to row.label.ru, "en" to row.label.en),
        description = mapOf("ru" to row.description.ru, "en" to row.description.en),
        blocks = mapOf(StylePhase.Front to front, StylePhase.Back to back),
        requires = requires.toSet(),
        fallback = row.fallback?.toStyleId(),
    )
}

/** The wire name of a kind is its name with the first letter in lower case (`NativeParallel` is `nativeParallel`): the tag of the row. */
private fun BlockKindRow.toBlockKind(): BlockKind = BlockKind.entries.single { it.name.replaceFirstChar(Char::lowercaseChar) == tag }

/** `native-contrast`, the id of a row, is `NativeContrast` on the wire of every host bridge (the Swift `styleId`, the web and Android pickers). */
private fun String.toStyleId() = StyleId(split('-').joinToString("") { it.replaceFirstChar(Char::uppercaseChar) })
