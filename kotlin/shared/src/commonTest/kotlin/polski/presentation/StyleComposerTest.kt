package polski.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import polski.data.MethodPresentation
import polski.data.SkillPresentation
import polski.data.SkillStyleContent
import polski.data.presentationBySkillId
import polski.data.skillById
import polski.model.FormChange
import polski.model.Exercise
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.model.Skill

class StyleComposerTest {
    private val registry = StyleRegistry.recipes

    private fun exercise(changes: List<FormChange> = listOf(FormChange("psa", "psa", "падеж без изменений"))) = Exercise(
        id = "fixture-1", primarySkill = "case.gen.neg", source = "Mam psa.", prompt = "Nie...",
        expected = "Nie mam psa.", explanation = "explanation", tags = emptyList(),
        nounId = "dog", adjectiveId = "beautiful", possessive = PossessiveId.MY, number = NumberGram.SG, changes = changes,
    )

    // ST-01: every style/phase composes without throwing, and returns a non-empty list, even
    // with the all-derived default SkillStyleContent — using several real skills, no JSON.
    @Test fun everyStyleAndPhaseComposesNonEmptyForRealSkills() {
        for (id in listOf("case.gen.neg", "verb.present", "sentence.question")) {
            val skill = skillById(id)
            val focus = presentationBySkillId(id)
            val ex = exercise().copy(primarySkill = id)
            for (recipe in registry.values) {
                val effective = registry.getValue(StyleComposer.resolveEffectiveStyle(recipe, SkillStyleContent(), registry))
                for (phase in StylePhase.entries) {
                    val blocks = StyleComposer.compose(effective, phase, ex, skill, focus, SkillStyleContent())
                    assertTrue(blocks.isNotEmpty(), "${recipe.id}/$phase for $id must not be empty")
                }
            }
        }
    }

    // ST-02: rule-first/situation-first, with no styleContent, render the same source text as
    // today's Logic/Situations — Formula/Rule from skill.formula/theory, Scene from
    // methods.situations.introduce — checked byte-for-byte against real course data.
    @Test fun ruleFirstAndSituationFirstDeriveTodaysText() {
        for (id in listOf("case.gen.neg", "verb.present")) {
            val skill = skillById(id)
            val focus = presentationBySkillId(id)
            val ex = exercise().copy(primarySkill = id)
            val ruleFirst = registry.getValue(StyleId.RuleFirst)
            val front = StyleComposer.compose(ruleFirst, StylePhase.Front, ex, skill, focus, SkillStyleContent())
            assertEquals(Block.Formula(skill.formula), front.filterIsInstance<Block.Formula>().single())
            val back = StyleComposer.compose(ruleFirst, StylePhase.Back, ex, skill, focus, SkillStyleContent())
            assertEquals(skill.theory, back.filterIsInstance<Block.Rule>().single().text)
            assertEquals(skill.formula, back.filterIsInstance<Block.Formula>().single().text)

            val situationFirst = registry.getValue(StyleId.SituationFirst)
            val situationFront = StyleComposer.compose(situationFirst, StylePhase.Front, ex, skill, focus, SkillStyleContent())
            assertEquals(focus.situations.introduce, situationFront.filterIsInstance<Block.Scene>().single().text)
        }
    }

    // ST-03: native-contrast without nativeParallel content resolves to its declared fallback
    // (rule-first) — never an empty/crashing NativeParallel block; with content, it resolves to
    // itself and Front carries the parallel.
    @Test fun nativeContrastFallsBackWithoutContentAndComposesWithIt() {
        val nativeContrast = registry.getValue(StyleId.NativeContrast)
        val empty = SkillStyleContent()
        assertEquals(StyleId.RuleFirst, StyleComposer.resolveEffectiveStyle(nativeContrast, empty, registry))

        val withPair = SkillStyleContent(nativeParallel = listOf(NativeParallelPair("native", "target", "note", matches = false)))
        assertEquals(StyleId.NativeContrast, StyleComposer.resolveEffectiveStyle(nativeContrast, withPair, registry))
        val skill = skillById("case.gen.neg")
        val focus = presentationBySkillId("case.gen.neg")
        val front = StyleComposer.compose(nativeContrast, StylePhase.Front, exercise(), skill, focus, withPair)
        assertEquals(listOf(NativeParallelPair("native", "target", "note", false)), front.filterIsInstance<Block.NativeParallel>().single().pairs)
    }

    // ST-04 (composer half; TrainingStore's own invariant is covered in TrainingStoreTest):
    // resolving a style never mutates exercise/skill/content — pure function, same inputs, same output.
    @Test fun resolveAndComposeArePure() {
        val nativeContrast = registry.getValue(StyleId.NativeContrast)
        val content = SkillStyleContent()
        val a = StyleComposer.resolveEffectiveStyle(nativeContrast, content, registry)
        val b = StyleComposer.resolveEffectiveStyle(nativeContrast, content, registry)
        assertEquals(a, b)
        val skill = skillById("case.gen.neg")
        val focus = presentationBySkillId("case.gen.neg")
        val ex = exercise()
        assertEquals(
            StyleComposer.compose(registry.getValue(StyleId.RuleFirst), StylePhase.Back, ex, skill, focus, content),
            StyleComposer.compose(registry.getValue(StyleId.RuleFirst), StylePhase.Back, ex, skill, focus, content),
        )
    }

    // ST-05: the Changes block is an exercise invariant, identical across all 4 styles.
    @Test fun changesBlockIsIdenticalAcrossAllStyles() {
        val skill = skillById("case.gen.neg")
        val focus = presentationBySkillId("case.gen.neg")
        val ex = exercise(changes = listOf(FormChange("psa", "psa", "reason A"), FormChange("mam", "nie mam", "reason B")))
        val changesPerStyle = registry.values.map { recipe ->
            StyleComposer.compose(recipe, StylePhase.Back, ex, skill, focus, SkillStyleContent())
                .filterIsInstance<Block.Changes>().singleOrNull()
        }
        assertTrue(changesPerStyle.all { it == changesPerStyle.first() })
        assertEquals(2, changesPerStyle.first()?.items?.size)
    }

    // ST-06 (behavioral genericity): a fully invented, non-Polish skill/exercise/presentation
    // composes exactly like a real one — nothing in Block/StyleRecipe/StyleComposer is coupled
    // to a specific language pair.
    @Test fun composerIsGenericAcrossLanguagePairs() {
        val fakeSkill = Skill("core.made-up.axis", "Made-up axis", "group", "A1", "X -> Y", "Some prose theory.", "hint", emptyList())
        val fakePresentation = SkillPresentation(
            "before-form", "after-form",
            MethodPresentation("intro", "lead", "logic introduce", "retrieve", "feedback", "review"),
            MethodPresentation("intro", "lead", "a short invented scene", "retrieve", "feedback", "review"),
        )
        val fakeExercise = Exercise(
            "fake-1", "core.made-up.axis", "source sentence", "prompt", "expected sentence",
            explanation = "explanation", tags = emptyList(), nounId = "n", adjectiveId = "a",
            possessive = PossessiveId.MY, number = NumberGram.SG, changes = listOf(FormChange("x", "y", "because")),
        )
        for (recipe in registry.values) {
            val effective = registry.getValue(StyleComposer.resolveEffectiveStyle(recipe, SkillStyleContent(), registry))
            for (phase in StylePhase.entries) {
                assertTrue(StyleComposer.compose(effective, phase, fakeExercise, fakeSkill, fakePresentation, SkillStyleContent()).isNotEmpty())
            }
        }
        val front = StyleComposer.compose(registry.getValue(StyleId.SituationFirst), StylePhase.Front, fakeExercise, fakeSkill, fakePresentation, SkillStyleContent())
        assertEquals("a short invented scene", front.filterIsInstance<Block.Scene>().single().text)
    }

    // §2 validator invariant: whatever a recipe names as its fallback must itself have empty requires.
    @Test fun fallbackTargetsNeverDeclareTheirOwnRequires() {
        for (recipe in registry.values) {
            val fallback = recipe.fallback?.let(registry::getValue) ?: continue
            assertTrue(fallback.requires.isEmpty())
        }
    }

    @Test fun unknownStyleOrFallbackNeverThrows() {
        val orphan = StyleRecipe(
            StyleId.NativeContrast,
            blocks = mapOf(StylePhase.Front to listOf(BlockKind.NativeParallel)),
            requires = setOf(BlockKind.NativeParallel),
            fallback = StyleId.MinimalTheory,
        )
        // The fallback (MinimalTheory) isn't in this registry — must resolve to itself, not throw.
        assertEquals(StyleId.NativeContrast, StyleComposer.resolveEffectiveStyle(orphan, SkillStyleContent(), emptyMap()))
    }
}
