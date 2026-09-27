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
            assertEquals(skill.formula, front.filterIsInstance<Block.Formula>().single().text)
            val back = StyleComposer.compose(ruleFirst, StylePhase.Back, ex, skill, focus, SkillStyleContent())
            assertEquals(skill.theory, back.filterIsInstance<Block.Rule>().single().text)
            // Regression (post-df8ade7 correction): the pre-UC-10 rule-focus box always showed a
            // second paragraph with the per-exercise `explanation` alongside the skill's theory —
            // Block.Rule must still carry it so rule-first stays byte-identical to that.
            assertEquals(ex.explanation, back.filterIsInstance<Block.Rule>().single().detail)
            // C3 (EmphasisUXAudit E10): Formula shows once, on Front — Back must not repeat it,
            // since Front stays visible after reveal.
            assertTrue(back.filterIsInstance<Block.Formula>().isEmpty())

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

        val withPair = SkillStyleContent(nativeParallel = listOf(
            NativeParallelPair("native", "target", "note", matches = false, targetParts = emptyList()),
        ))
        assertEquals(StyleId.NativeContrast, StyleComposer.resolveEffectiveStyle(nativeContrast, withPair, registry))
        val skill = skillById("case.gen.neg")
        val focus = presentationBySkillId("case.gen.neg")
        val front = StyleComposer.compose(nativeContrast, StylePhase.Front, exercise(), skill, focus, withPair)
        // "target" contains neither focus.focusBefore nor focus.focusAfter verbatim, so it stays a
        // single unmarked part — the explicit-pair contract never invents a highlight (S4).
        assertEquals(
            listOf(NativeParallelPair("native", "target", "note", false, targetParts = listOf(EndingPart("target", false)))),
            front.filterIsInstance<Block.NativeParallel>().single().pairs,
        )
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

        // Derived defaults must carry no natural-language literal: a WhyOnDemand/Table shown for
        // this made-up, non-Russian pair must not surface Russian UI chrome baked into CORE.
        val minimalTheory = registry.getValue(StyleId.MinimalTheory)
        val why = StyleComposer.compose(minimalTheory, StylePhase.Back, fakeExercise, fakeSkill, fakePresentation, SkillStyleContent())
            .filterIsInstance<Block.WhyOnDemand>().single()
        assertEquals("", why.collapsedLabel)

        val ruleFirst = registry.getValue(StyleId.RuleFirst)
        val table = StyleComposer.compose(ruleFirst, StylePhase.Front, fakeExercise, fakeSkill, fakePresentation, SkillStyleContent())
            .filterIsInstance<Block.Table>().single()
        assertEquals("", table.rows.single().label)
    }

    // S4 (EmphasisUXAudit E7): Formula/Rule/Scene/NativeParallel(target)/Examples/WhyOnDemand each
    // carry highlight parts of their own text, computed from the skill's explicit
    // `focus.before`/`focus.after` pair — never parsed heuristically, never the native side of a
    // NativeParallel pair.
    @Test fun styleBlocksCarryPartsForTheirOwnTextFromTheExplicitFocusPair() {
        val fakeSkill = Skill(
            "core.made-up.axis", "Made-up axis", "group", "A1",
            "See mojej become ich here.", "Rule: mojej changes to ich here.", "hint", emptyList(),
        )
        val fakePresentation = SkillPresentation(
            "mojej", "ich",
            MethodPresentation("intro", "lead", "Logic mojej to ich.", "retrieve", "feedback", "review"),
            MethodPresentation("intro", "lead", "In this scene mojej becomes ich naturally.", "retrieve", "feedback", "review"),
        )
        val content = SkillStyleContent(
            nativeParallel = listOf(NativeParallelPair("native mojej ich", "target mojej ich", "note", true, emptyList())),
            examples = listOf("Use mojej here.", "Use ich there."),
        )
        val fakeExercise = Exercise(
            "fake-1", "core.made-up.axis", "source sentence", "prompt", "expected sentence",
            explanation = "explanation", tags = emptyList(), nounId = "n", adjectiveId = "a",
            possessive = PossessiveId.MY, number = NumberGram.SG, changes = listOf(FormChange("x", "y", "because")),
        )

        val formula = StyleComposer.compose(registry.getValue(StyleId.RuleFirst), StylePhase.Front, fakeExercise, fakeSkill, fakePresentation, content)
            .filterIsInstance<Block.Formula>().single()
        assertEquals(fakeSkill.formula, formula.parts.joinToString("") { it.text })
        assertEquals(listOf("mojej", "ich"), formula.parts.filter(EndingPart::isChanged).map(EndingPart::text))
        // W3 correction (blocker 2): "mojej" is the skill's own `focus.before` and must carry the
        // "before" role (warm/dashed), "ich" is `focus.after` and must carry "after" (cool/solid)
        // — even though both sit in the same running text, a host cannot tell them apart from a
        // single caller-supplied class for the whole block.
        assertEquals(listOf(ChangeSide.Before, ChangeSide.After), formula.parts.filter(EndingPart::isChanged).map(EndingPart::side))

        val rule = StyleComposer.compose(registry.getValue(StyleId.RuleFirst), StylePhase.Back, fakeExercise, fakeSkill, fakePresentation, content)
            .filterIsInstance<Block.Rule>().single()
        assertEquals(fakeSkill.theory, rule.parts.joinToString("") { it.text })
        assertEquals(listOf("mojej", "ich"), rule.parts.filter(EndingPart::isChanged).map(EndingPart::text))
        assertEquals(listOf(ChangeSide.Before, ChangeSide.After), rule.parts.filter(EndingPart::isChanged).map(EndingPart::side))

        val scene = StyleComposer.compose(registry.getValue(StyleId.SituationFirst), StylePhase.Front, fakeExercise, fakeSkill, fakePresentation, content)
            .filterIsInstance<Block.Scene>().single()
        assertEquals(fakePresentation.situations.introduce, scene.parts.joinToString("") { it.text })
        assertEquals(listOf("mojej", "ich"), scene.parts.filter(EndingPart::isChanged).map(EndingPart::text))
        assertEquals(listOf(ChangeSide.Before, ChangeSide.After), scene.parts.filter(EndingPart::isChanged).map(EndingPart::side))

        val nativeParallel = StyleComposer.compose(registry.getValue(StyleId.NativeContrast), StylePhase.Front, fakeExercise, fakeSkill, fakePresentation, content)
            .filterIsInstance<Block.NativeParallel>().single().pairs.single()
        assertEquals("target mojej ich", nativeParallel.targetParts.joinToString("") { it.text })
        assertEquals(listOf("mojej", "ich"), nativeParallel.targetParts.filter(EndingPart::isChanged).map(EndingPart::text))
        assertEquals(listOf(ChangeSide.Before, ChangeSide.After), nativeParallel.targetParts.filter(EndingPart::isChanged).map(EndingPart::side))
        // Native (L1) prose is never highlighted (EmphasisUXAudit E7) — the model gives it no parts field at all.

        val examplesBlock = StyleComposer.compose(registry.getValue(StyleId.MinimalTheory), StylePhase.Front, fakeExercise, fakeSkill, fakePresentation, content)
            .filterIsInstance<Block.Examples>().single()
        assertEquals(listOf("Use mojej here.", "Use ich there."), examplesBlock.items)
        assertEquals(listOf("mojej"), examplesBlock.itemParts[0].filter(EndingPart::isChanged).map(EndingPart::text))
        assertEquals(listOf(ChangeSide.Before), examplesBlock.itemParts[0].filter(EndingPart::isChanged).map(EndingPart::side))
        assertEquals(listOf("ich"), examplesBlock.itemParts[1].filter(EndingPart::isChanged).map(EndingPart::text))
        assertEquals(listOf(ChangeSide.After), examplesBlock.itemParts[1].filter(EndingPart::isChanged).map(EndingPart::side))

        val why = StyleComposer.compose(registry.getValue(StyleId.MinimalTheory), StylePhase.Back, fakeExercise, fakeSkill, fakePresentation, content)
            .filterIsInstance<Block.WhyOnDemand>().single()
        assertEquals(fakeSkill.theory, why.parts.joinToString("") { it.text })
        assertEquals(listOf("mojej", "ich"), why.parts.filter(EndingPart::isChanged).map(EndingPart::text))
        assertEquals(listOf(ChangeSide.Before, ChangeSide.After), why.parts.filter(EndingPart::isChanged).map(EndingPart::side))
    }

    // S4 (EmphasisUXAudit E7, Emphasis contract §5 no-leak): a front block's highlight comes only
    // from the skill's own `focus` pair, never from the current exercise's actual answer — so two
    // exercises with different changes/expected on the same skill/content compose byte-identical
    // Formula/Scene/NativeParallel/Examples parts. Nothing exercise-specific ever reaches a front
    // block before reveal.
    @Test fun frontBlockPartsNeverDependOnTheCurrentExercisesOwnAnswer() {
        val skill = skillById("case.gen.neg")
        val focus = presentationBySkillId("case.gen.neg")
        val content = SkillStyleContent(
            nativeParallel = listOf(NativeParallelPair("native", "target text", "note", true, emptyList())),
            examples = listOf("An example sentence."),
        )
        val exerciseA = exercise(changes = listOf(FormChange("psa", "psa", "A")))
        val exerciseB = exercise(changes = listOf(FormChange("mam", "nie mam", "B"))).copy(expected = "totally different expected sentence")

        for (recipe in listOf(StyleId.RuleFirst, StyleId.SituationFirst, StyleId.NativeContrast, StyleId.MinimalTheory)) {
            val effective = registry.getValue(StyleComposer.resolveEffectiveStyle(registry.getValue(recipe), content, registry))
            val frontA = StyleComposer.compose(effective, StylePhase.Front, exerciseA, skill, focus, content)
                .filterNot { it is Block.Table } // Table is exempt (reference material, §4) and out of S4's scope
            val frontB = StyleComposer.compose(effective, StylePhase.Front, exerciseB, skill, focus, content)
                .filterNot { it is Block.Table }
            assertEquals(frontA, frontB, "front blocks for $recipe must not depend on the exercise's own changes/expected")
        }
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
