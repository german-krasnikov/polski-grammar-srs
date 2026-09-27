package dev.polski.grammarmatrix

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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
        composeRule.onNodeWithText("ЗАПОМНИ").assertExists()
        composeRule.onNodeWithText(skill.theory).assertExists()

        styleId = StyleId.MinimalTheory
        composeRule.waitForIdle()
        composeRule.onNodeWithText("ЗАПОМНИ").assertDoesNotExist()
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
}
