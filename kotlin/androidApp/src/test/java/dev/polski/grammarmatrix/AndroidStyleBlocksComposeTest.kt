package dev.polski.grammarmatrix

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.style.TextDecoration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import polski.data.presentationBySkillId
import polski.data.skillById
import polski.data.styleContentBySkillId
import polski.model.Exercise
import polski.model.FormChange
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.presentation.Block
import polski.presentation.ChangeSide
import polski.presentation.EndingPart
import polski.presentation.NativeParallelPair
import polski.presentation.StyleComposer
import polski.presentation.StyleId
import polski.presentation.StylePhase
import polski.presentation.StyleRegistry
import polski.ui.screens.AndroidBlockList

/**
 * UC-10/S2 (`Plans/Kotlin/StylesBlueprint.md`§6): [AndroidBlockList] renders whatever
 * [polski.presentation.StyleComposer] hands it for one phase, in order — this proves switching
 * `styleId` actually changes which blocks reach the screen, using real course data. `case.gen.neg`
 * has no authored `styleContent` yet (CONTENT lands in a later merge), so this also exercises
 * today's production path where NativeContrast resolves through its declared RuleFirst fallback.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidStyleBlocksComposeTest {
    @get:Rule val composeRule = createComposeRule()

    private val skillId = "case.gen.neg"
    private val skill = skillById(skillId)
    private val focus = presentationBySkillId(skillId)
    private val content = styleContentBySkillId(skillId)
    private val exercise = Exercise(
        id = "style-blocks-fixture", primarySkill = skillId, source = "Mam psa.", prompt = "Nie...",
        expected = "Nie mam psa.", explanation = "explanation", tags = emptyList(),
        nounId = "dog", adjectiveId = "beautiful", possessive = PossessiveId.MY, number = NumberGram.SG,
        changes = listOf(FormChange("psa", "psa", "падеж без изменений")),
    )

    /** Same resolve-then-compose sequence the training card itself runs (`AndroidTrainingScreen.kt`). */
    private fun blocksFor(styleId: StyleId, phase: StylePhase) = StyleComposer.compose(
        StyleRegistry.recipes.getValue(
            StyleComposer.resolveEffectiveStyle(StyleRegistry.recipes.getValue(styleId), content, StyleRegistry.recipes),
        ),
        phase, exercise, skill, focus, content,
    )

    @Test fun switchingStyleChangesWhichFrontBlocksAppear() {
        var styleId by mutableStateOf(StyleId.RuleFirst)
        composeRule.setContent {
            MaterialTheme { AndroidBlockList(blocksFor(styleId, StylePhase.Front), reduceMotion = true) }
        }
        composeRule.onNodeWithText(skill.formula).assertExists()

        styleId = StyleId.SituationFirst
        composeRule.waitForIdle()
        composeRule.onNodeWithText(skill.formula).assertDoesNotExist()
        composeRule.onNodeWithText(focus.situations.introduce).assertExists()
    }

    @Test fun switchingStyleChangesWhichBackBlocksAppear() {
        var styleId by mutableStateOf(StyleId.RuleFirst)
        composeRule.setContent {
            MaterialTheme { AndroidBlockList(blocksFor(styleId, StylePhase.Back), reduceMotion = true) }
        }
        // C3 (EmphasisUXAudit E10): rule-first's back no longer repeats Formula ("ЗАПОМНИ") —
        // it already showed on front, which stays visible after reveal. "Правило" (Rule's own
        // heading) is the marker unique to rule-first's back among these two styles instead.
        composeRule.onNodeWithText("Правило").assertExists()
        composeRule.onNodeWithText(skill.theory).assertExists()

        styleId = StyleId.MinimalTheory
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Правило").assertDoesNotExist()
        composeRule.onNodeWithText("Почему так?").assertExists()
    }

    /** D2 (`StylesIntegrationTest-2026-09-27.md`): rule-first back must show both `Rule.text` and `Rule.detail`. */
    @Test fun ruleFirstBackShowsBothRuleTextAndDetail() {
        composeRule.setContent {
            MaterialTheme { AndroidBlockList(blocksFor(StyleId.RuleFirst, StylePhase.Back), reduceMotion = true) }
        }
        composeRule.onNodeWithText(skill.theory).assertExists()
        composeRule.onNodeWithText(exercise.explanation).assertExists()
    }

    // S4/E7: Formula/Rule/Scene/NativeParallel/Examples/WhyOnDemand must highlight their `parts`
    // through the same before/after rendering path as the sentence (AndroidEmphasisText) — a
    // fabricated mixed-role parts list (one changed fragment per role, in the same block) proves
    // each block kind actually reaches that path with per-part role intact, not just its own text.
    private val mixedText = "kupiłem i kupiłam"
    private val mixedParts = listOf(
        EndingPart("kupiłem", isEnding = false, isChanged = true, side = ChangeSide.Before),
        EndingPart(" i ", isEnding = false),
        EndingPart("kupiłam", isEnding = false, isChanged = true, side = ChangeSide.After),
    )

    /** [before] span carries no native [TextDecoration] (its dash is hand-drawn); [after] carries a solid one. */
    private fun assertBeforeAfterDecorations(nodeText: String) {
        val annotated = composeRule.onNodeWithText(nodeText).fetchSemanticsNode()
            .config[SemanticsProperties.Text].single()
        val beforeStart = nodeText.indexOf("kupiłem")
        val afterStart = nodeText.indexOf("kupiłam")
        val beforeSpan = annotated.spanStyles.single { it.start == beforeStart && it.end == beforeStart + 7 }
        val afterSpan = annotated.spanStyles.single { it.start == afterStart && it.end == afterStart + 7 }
        assertEquals(null, beforeSpan.item.textDecoration)
        assertEquals(TextDecoration.Underline, afterSpan.item.textDecoration)
        assertTrue(beforeSpan.item.color != afterSpan.item.color)
    }

    @Test fun formulaBlockHighlightsItsParts() {
        composeRule.setContent {
            MaterialTheme { AndroidBlockList(listOf(Block.Formula(mixedText, mixedParts)), reduceMotion = true) }
        }
        assertBeforeAfterDecorations(mixedText)
    }

    @Test fun ruleBlockHighlightsItsParts() {
        composeRule.setContent {
            MaterialTheme { AndroidBlockList(listOf(Block.Rule(mixedText, "detail", mixedParts)), reduceMotion = true) }
        }
        assertBeforeAfterDecorations(mixedText)
    }

    @Test fun sceneBlockHighlightsItsParts() {
        composeRule.setContent {
            MaterialTheme { AndroidBlockList(listOf(Block.Scene(mixedText, mixedParts)), reduceMotion = true) }
        }
        assertBeforeAfterDecorations(mixedText)
    }

    @Test fun nativeParallelBlockHighlightsTargetPartsOnly() {
        val pair = NativeParallelPair(native = "native lead-in", target = mixedText, note = "", matches = true, targetParts = mixedParts)
        composeRule.setContent {
            MaterialTheme { AndroidBlockList(listOf(Block.NativeParallel(listOf(pair))), reduceMotion = true) }
        }
        // Native (L1) side never highlights (Emphasis contract §4) — only the target node exists
        // with `mixedText`, and it must carry the same before/after decorations as every other block.
        composeRule.onNodeWithText("native lead-in").assertExists()
        assertBeforeAfterDecorations(mixedText)
    }

    @Test fun examplesBlockHighlightsItemParts() {
        composeRule.setContent {
            MaterialTheme { AndroidBlockList(listOf(Block.Examples(listOf(mixedText), listOf(mixedParts))), reduceMotion = true) }
        }
        assertBeforeAfterDecorations(mixedText)
    }

    @Test fun whyOnDemandBlockHighlightsItsParts() {
        composeRule.setContent {
            MaterialTheme { AndroidBlockList(listOf(Block.WhyOnDemand(mixedText, mixedParts, "Почему так?")), reduceMotion = true) }
        }
        composeRule.onNodeWithText("Почему так?").performClick()
        composeRule.waitForIdle()
        assertBeforeAfterDecorations(mixedText)
    }
}
