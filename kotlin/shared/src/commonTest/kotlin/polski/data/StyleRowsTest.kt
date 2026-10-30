package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.data.rows.styles.BlockKindRow
import polski.data.rows.styles.LocalizedTextRow
import polski.data.rows.styles.StyleRecipeRow
import polski.presentation.BlockKind
import polski.presentation.StyleId
import polski.presentation.StylePhase

/**
 * R1.18: the style mapper on hand-built rows with a different text in every slot. The real rows are StyleRegistryTest's (the order of the blocks of each built-in style) and the vitest test
 * course-styles (the four rows against the sealed courses/styles JSON files, sha256 pinned); a kind or a reference that the contract can state is refused by `biome data check` before this runs.
 */
class StyleRowsTest {
    private fun row(id: String, front: List<BlockKindRow>, back: List<BlockKindRow>, requires: List<BlockKindRow>? = null, fallback: String? = null) =
        StyleRecipeRow(id, LocalizedTextRow("ru-$id", "en-$id"), LocalizedTextRow("dru-$id", "den-$id"), front, back, requires, fallback)

    @Test fun theMapperIsGenericOverAnyRowCountAndLoadsAnIdThatIsNoBuiltInOne() {
        val recipes = listOf(
            row("native-contrast", listOf(BlockKindRow.EXAMPLES), listOf(BlockKindRow.WHYONDEMAND), requires = listOf(BlockKindRow.EXAMPLES), fallback = "minimal-theory"),
            row("future-style", listOf(BlockKindRow.SCENE), listOf(BlockKindRow.RULE)),
        ).toStyleRecipes()
        assertEquals(listOf(StyleId.NativeContrast, StyleId("FutureStyle")), recipes.map { it.id })
        val known = recipes[0]
        assertEquals(mapOf("ru" to "ru-native-contrast", "en" to "en-native-contrast"), known.label)
        assertEquals(mapOf("ru" to "dru-native-contrast", "en" to "den-native-contrast"), known.description)
        assertEquals(mapOf(StylePhase.Front to listOf(BlockKind.Examples), StylePhase.Back to listOf(BlockKind.WhyOnDemand)), known.blocks)
        assertEquals(setOf(BlockKind.Examples), known.requires)
        assertEquals(StyleId.MinimalTheory, known.fallback)
        val future = recipes[1]
        assertEquals(mapOf(StylePhase.Front to listOf(BlockKind.Scene), StylePhase.Back to listOf(BlockKind.Rule)), future.blocks)
        assertEquals(emptySet(), future.requires)
        assertEquals(null, future.fallback)
    }

    @Test fun noRowsMakeNoRecipesAndEveryKindOfTheContractIsAKindOfTheModelInTheOrderOfBoth() {
        assertEquals(emptyList(), emptyList<StyleRecipeRow>().toStyleRecipes())
        val all = listOf(row("all", BlockKindRow.entries.take(4), BlockKindRow.entries.drop(4))).toStyleRecipes().single()
        assertEquals(BlockKind.entries.toList(), all.blocks.values.flatten())
        assertEquals(BlockKind.entries.map { it.name.replaceFirstChar(Char::lowercaseChar) }, BlockKindRow.entries.map { it.tag })
    }

    @Test fun theOrderOfTheBlocksInAPhaseIsTheOrderOfTheRow() {
        val recipe = listOf(row("a", listOf(BlockKindRow.TABLE, BlockKindRow.FORMULA), listOf(BlockKindRow.CONTRAST, BlockKindRow.RULE, BlockKindRow.CHANGES))).toStyleRecipes().single()
        assertEquals(listOf(BlockKind.Table, BlockKind.Formula), recipe.blocks.getValue(StylePhase.Front))
        assertEquals(listOf(BlockKind.Contrast, BlockKind.Rule, BlockKind.Changes), recipe.blocks.getValue(StylePhase.Back))
    }

    // C3 (EmphasisUXAudit E10): the card appends the front blocks after the reveal too, so a kind in both phases, or twice in one, would be drawn twice.
    @Test fun aKindShownTwiceWithinAPhaseOrOnBothIsRefusedAndDisjointPhasesAreAccepted() {
        val twiceInFront = row("front", listOf(BlockKindRow.FORMULA, BlockKindRow.FORMULA), listOf(BlockKindRow.RULE))
        val twiceInBack = row("back", listOf(BlockKindRow.TABLE), listOf(BlockKindRow.RULE, BlockKindRow.CONTRAST, BlockKindRow.RULE))
        val onBoth = row("both", listOf(BlockKindRow.FORMULA), listOf(BlockKindRow.FORMULA, BlockKindRow.RULE))
        for (bad in listOf(twiceInFront, twiceInBack, onBoth)) {
            val failure = assertFailsWith<IllegalArgumentException>(bad.id) { listOf(bad).toStyleRecipes() }
            assertEquals(true, failure.message!!.startsWith("${bad.id}: ["), failure.message)
        }
        listOf(row("ok", listOf(BlockKindRow.TABLE, BlockKindRow.FORMULA), listOf(BlockKindRow.RULE, BlockKindRow.CHANGES, BlockKindRow.CONTRAST))).toStyleRecipes()
    }

    @Test fun aKindListedTwiceInRequiresIsRefused() {
        val twice = row("r", listOf(BlockKindRow.EXAMPLES), listOf(BlockKindRow.RULE), requires = listOf(BlockKindRow.EXAMPLES, BlockKindRow.EXAMPLES))
        assertEquals("r: requires lists a kind twice", assertFailsWith<IllegalArgumentException> { listOf(twice).toStyleRecipes() }.message)
    }
}
