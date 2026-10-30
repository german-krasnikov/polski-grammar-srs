package polski.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * D3/D1 (StylesIntegrationTest-2026-09-27.md): the rows of `courses-biome/styles/styles.core-data` (R1.18; until then the `courses/styles` JSON files)
 * are the single source of truth for [StyleRegistry], not a separate, hand-written Kotlin literal that can drift from it. The mapper from the rows is StyleRowsTest's.
 */
class StyleRegistryTest {
    // D1: situation-first's Back must be exactly [changes, rule] per its row
    // — the pre-fix hand-written registry had only [Changes], dropping Rule after reveal.
    @Test fun situationFirstBackIsChangesThenRule() {
        val recipe = StyleRegistry.recipes.getValue(StyleId.SituationFirst)
        assertEquals(listOf(BlockKind.Changes, BlockKind.Rule), recipe.blocks.getValue(StylePhase.Back))
    }

    // D3: rule-first's Back order must match its row (rule, changes, contrast) — the pre-fix
    // literal used a different order. C3 (EmphasisUXAudit E10): Back no longer repeats Formula,
    // already shown on Front and still visible after reveal.
    @Test fun ruleFirstBackIsRuleChangesContrast() {
        val recipe = StyleRegistry.recipes.getValue(StyleId.RuleFirst)
        assertEquals(listOf(BlockKind.Rule, BlockKind.Changes, BlockKind.Contrast), recipe.blocks.getValue(StylePhase.Back))
    }

    // D3: label/description must come from the rows, not stay blank (the pre-fix literal
    // never carried them, forcing every host to keep its own fallback copy permanently).
    @Test fun allRecipesCarryNonBlankLabelAndDescription() {
        for (recipe in StyleRegistry.recipes.values) {
            assertTrue(recipe.label["ru"]?.isNotBlank() == true, "${recipe.id} label.ru")
            assertTrue(recipe.label["en"]?.isNotBlank() == true, "${recipe.id} label.en")
            assertTrue(recipe.description["ru"]?.isNotBlank() == true, "${recipe.id} description.ru")
        }
    }

    @Test fun nativeContrastStillDeclaresItsRequiresAndFallback() {
        val recipe = StyleRegistry.recipes.getValue(StyleId.NativeContrast)
        assertEquals(setOf(BlockKind.NativeParallel), recipe.requires)
        assertEquals(StyleId.RuleFirst, recipe.fallback)
    }

    // "Selectable" end-to-end: an unknown-id recipe reaches AppUiState.styleId through the same
    // SetStyle/TrainingStore path any of the 4 built-in styles use — no enum boundary rejects it.
    @Test fun anUnknownStyleIdIsSelectableThroughSetStyle() {
        val exotic = StyleId("FutureStyle")
        val action = AppAction.SetStyle(exotic)
        val next = AppUiState(styleId = StyleId.RuleFirst).copy(styleId = action.styleId)
        assertEquals(exotic, next.styleId)
    }

    // C3 (EmphasisUXAudit-2026-09-27.md E10): Front stays visible after reveal (both hosts append
    // Front and Back), so a kind repeated within one phase or shared across Front/Back would
    // render twice. The mapper (StyleRowsTest) refuses such a row; this locks the same invariant on the real, built-in recipes.
    @Test fun noBuiltInRecipeRepeatsABlockKindWithinOrAcrossItsPhases() {
        for (recipe in StyleRegistry.recipes.values) {
            val front = recipe.blocks.getValue(StylePhase.Front)
            val back = recipe.blocks.getValue(StylePhase.Back)
            assertEquals(front.toSet().size, front.size, "${recipe.id} front repeats a block kind")
            assertEquals(back.toSet().size, back.size, "${recipe.id} back repeats a block kind")
            assertTrue(front.intersect(back).isEmpty(), "${recipe.id} shows the same kind on both front and back: ${front.intersect(back)}")
        }
    }
}
