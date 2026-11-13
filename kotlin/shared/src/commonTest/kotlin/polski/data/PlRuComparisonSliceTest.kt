package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.training.PlExerciseEngine
import polski.presentation.AppUiState
import polski.presentation.CardPhase
import polski.presentation.StyleComposer
import polski.presentation.StaticPackLifehackProvider
import polski.presentation.StyleId
import polski.presentation.StyleRegistry
import polski.presentation.trainingCardModel

class PlRuComparisonSliceTest {
    @Test fun authoredComparisonPracticeHasSixWholeSentenceTransformationsAndNoCaseTable() {
        val previous = activeCoursePackId
        try {
            selectCoursePack("pl-ru")
            val generator = PlExerciseEngine(RandomSource { 0.0 }, ExerciseIdFactory { "comparison" })
            val exercises = (1..6).map { generator.generateForSkill("degree.comparison") }
            assertEquals(setOf(
                "Ten dom jest stary.", "To zadanie jest łatwe.", "Ta książka jest dobra.",
                "Ten film jest interesujący.", "To jest duży pokój.", "Ten rower jest szybki.",
            ), exercises.map { it.source }.toSet())
            assertEquals(setOf(
                "Ten dom jest starszy.", "To zadanie jest łatwiejsze.", "Ta książka jest lepsza.",
                "Ten film jest bardziej interesujący.", "To jest największy pokój.",
                "Ten rower jest szybszy od tamtego roweru.",
            ), exercises.map { it.expected }.toSet())
            assertTrue(exercises.all { it.prompt.isNotBlank() && it.explanation.isNotBlank() && it.changes.size == 1 })
            assertNotEquals(exercises.last().source, generator.generateForSkill("degree.comparison").source)
            val state = AppUiState(exercise = exercises.first(), phase = CardPhase.Question, showReference = true)
            assertFalse(trainingCardModel(state).referenceToggle.enabled)
            assertFalse(trainingCardModel(state).referenceToggle.expanded)
            assertTrue(trainingCardModel(state.copy(exercise = null)).referenceToggle.enabled)
        } finally {
            selectCoursePack(previous)
        }
    }

    @Test fun everyPresentationPreferenceHasAuthoredComparisonContent() {
        val previous = activeCoursePackId
        try {
            selectCoursePack("pl-ru")
            val content = styleContentBySkillId("degree.comparison")
            assertTrue(content.nativeParallel.isNotEmpty())
            val exercise = PlExerciseEngine(RandomSource { 0.0 }, ExerciseIdFactory { "comparison" }).generateForSkill("degree.comparison")
            val frontBlocks = mutableSetOf<String>()
            for (style in listOf(StyleId.RuleFirst, StyleId.SituationFirst, StyleId.NativeContrast, StyleId.MinimalTheory)) {
                val recipe = StyleRegistry.recipes.getValue(style)
                assertEquals(style, StyleComposer.resolveEffectiveStyle(recipe, content, StyleRegistry.recipes))
                val front = trainingCardModel(AppUiState(exercise = exercise, styleId = style, phase = CardPhase.Question)).front.toString()
                assertFalse(front.contains(exercise.expected), "$style leaked the expected sentence on the front")
                frontBlocks += front
            }
            assertEquals(4, frontBlocks.size)
            assertTrue(referenceSystemCards.any { it.id == "degree.comparison" })
            assertTrue(StaticPackLifehackProvider.forSkill("degree.comparison").isNotEmpty())
        } finally {
            selectCoursePack(previous)
        }
    }
}
