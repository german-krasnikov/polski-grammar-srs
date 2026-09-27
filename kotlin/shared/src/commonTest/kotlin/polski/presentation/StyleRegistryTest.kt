package polski.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * D3/D1 (StylesIntegrationTest-2026-09-27.md): the `courses/styles` recipe JSON files must be the
 * single source of truth for [StyleRegistry], not a separate, hand-written Kotlin literal that can drift from it.
 */
class StyleRegistryTest {
    // D1: situation-first's Back must be exactly [changes, rule] per situation-first.json
    // — the pre-fix hand-written registry had only [Changes], dropping Rule after reveal.
    @Test fun situationFirstBackMatchesJsonOrderWithRule() {
        val recipe = StyleRegistry.recipes.getValue(StyleId.SituationFirst)
        assertEquals(listOf(BlockKind.Changes, BlockKind.Rule), recipe.blocks.getValue(StylePhase.Back))
    }

    // D3: rule-first's Back order must match rule-first.json byte-for-byte (rule, changes,
    // contrast) — the pre-fix literal used a different order. C3 (EmphasisUXAudit E10): Back no
    // longer repeats Formula, already shown on Front and still visible after reveal.
    @Test fun ruleFirstBackMatchesJsonOrder() {
        val recipe = StyleRegistry.recipes.getValue(StyleId.RuleFirst)
        assertEquals(listOf(BlockKind.Rule, BlockKind.Changes, BlockKind.Contrast), recipe.blocks.getValue(StylePhase.Back))
    }

    // D3: label/description must come from the JSON files, not stay blank (the pre-fix literal
    // never carried them, forcing every host to keep its own fallback copy permanently).
    @Test fun allRecipesCarryNonBlankLabelAndDescriptionFromJson() {
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

    // UC-01: StyleId is open now — "adding a 5th style must need only a new JSON file", so a
    // recipe whose id isn't one of the 4 built-in ones is loaded and selectable, not skipped. This
    // uses a local JSON fixture, not the real generatedStylesJson.
    @Test fun parserIsGenericOverAnArbitraryRecipeCountAndLoadsAnUnknownId() {
        val fixtureJson = """
            [
              {
                "id": "native-contrast",
                "label": { "ru": "Тестовая метка", "en": "Test label" },
                "description": { "ru": "Тестовое описание", "en": "Test description" },
                "blocks": { "front": ["examples"], "back": ["whyOnDemand"] },
                "requires": ["examples"],
                "fallback": "minimal-theory"
              },
              {
                "id": "future-style",
                "label": { "ru": "Будущий стиль", "en": "Future style" },
                "description": { "ru": "Ещё не существует", "en": "Does not exist yet" },
                "blocks": { "front": ["scene"], "back": ["rule"] }
              }
            ]
        """.trimIndent()
        val parsed = parseStyleRecipesJson(fixtureJson)
        // Both recipes are loaded now — an unrecognized id is a brand-new StyleId, not a skip.
        assertEquals(2, parsed.size)
        val known = parsed[0]
        // Every field comes straight from this fixture, proving no leftover hardcoded literal
        // shadows JSON content for a known id.
        assertEquals(StyleId.NativeContrast, known.id)
        assertEquals("Тестовая метка", known.label["ru"])
        assertEquals(mapOf(StylePhase.Front to listOf(BlockKind.Examples), StylePhase.Back to listOf(BlockKind.WhyOnDemand)), known.blocks)
        assertEquals(setOf(BlockKind.Examples), known.requires)
        assertEquals(StyleId.MinimalTheory, known.fallback)
        // The unknown id is loaded and selectable — dispatchable as any other StyleId, not a crash.
        val future = parsed[1]
        assertEquals(StyleId("FutureStyle"), future.id)
        assertEquals("Будущий стиль", future.label["ru"])
        assertEquals(mapOf(StylePhase.Front to listOf(BlockKind.Scene), StylePhase.Back to listOf(BlockKind.Rule)), future.blocks)
        assertEquals(emptySet(), future.requires)
        assertEquals(null, future.fallback)
    }

    // "Selectable" end-to-end: an unknown-id recipe reaches AppUiState.styleId through the same
    // SetStyle/TrainingStore path any of the 4 built-in styles use — no enum boundary rejects it.
    @Test fun anUnknownStyleIdIsSelectableThroughSetStyle() {
        val exotic = StyleId("FutureStyle")
        val action = AppAction.SetStyle(exotic)
        val next = AppUiState(styleId = StyleId.RuleFirst).copy(styleId = action.styleId)
        assertEquals(exotic, next.styleId)
    }

    @Test fun parserNeverThrowsOnAnEmptyRecipeList() {
        assertEquals(emptyList(), parseStyleRecipesJson("[]"))
    }

    // C3 (EmphasisUXAudit-2026-09-27.md E10): Front stays visible after reveal (both hosts append
    // Front and Back), so a kind repeated within one phase or shared across Front/Back would
    // render twice. `scripts/validate-course.mjs`'s `assertNoDuplicateBlockKinds` rejects this at
    // authoring time; this locks the same invariant on the parsed, built-in recipes.
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
