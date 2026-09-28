package polski.training

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import polski.model.SentenceSeed
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.presentation.ChangeSide
import polski.presentation.EndingPart
import polski.presentation.sentenceHighlightParts

/**
 * S3 (EmphasisUXAudit E5): the inserted negation particle "Nie" and question particle "Czy"
 * must project through the shared highlight model as an `after`-only whole-word insertion —
 * never as a fabricated `before` fragment (no answer leak on the front).
 */
class NegationAndQuestionHighlightTest {
    private class FixedDraw : RandomSource { override fun nextDouble(): Double = 0.0 }
    private fun factory() = PlExerciseEngine(FixedDraw(), ExerciseIdFactory { "generated" })

    @Test
    fun negationParticleIsHighlightedAfterRevealOnlyForCaseGenNeg() {
        val exercise = factory().generateForSkill("case.gen.neg", SentenceSeed("wife", "beautiful"))

        val front = sentenceHighlightParts(exercise.source, exercise.changes, ChangeSide.Before)
        assertEquals(exercise.source, front.joinToString("") { it.text })
        assertTrue("Nie" !in front.filter(EndingPart::isChanged).map(EndingPart::text), "front leaked the inserted particle")

        val back = sentenceHighlightParts(exercise.expected, exercise.changes, ChangeSide.After)
        assertEquals(exercise.expected, back.joinToString("") { it.text })
        val nieSpan = back.first { it.text == "Nie" }
        assertTrue(nieSpan.isChanged, "Nie must be marked as an inserted change")
    }

    @Test
    fun negationParticleIsHighlightedInTheChainStepToo() {
        val step = factory().generateChain(SentenceSeed("wife", "beautiful"))[2]
        assertEquals("case.gen.neg", step.primarySkill)
        val back = sentenceHighlightParts(step.expected, step.changes, ChangeSide.After)
        assertTrue(back.first { it.text == "Nie" }.isChanged)
    }

    @Test
    fun questionParticleIsHighlightedAfterRevealOnly() {
        val exercise = factory().generateForSkill("sentence.question", SentenceSeed("wife", "beautiful"))

        val front = sentenceHighlightParts(exercise.source, exercise.changes, ChangeSide.Before)
        assertEquals(exercise.source, front.joinToString("") { it.text })
        assertTrue("Czy" !in front.filter(EndingPart::isChanged).map(EndingPart::text), "front leaked the inserted particle")

        val back = sentenceHighlightParts(exercise.expected, exercise.changes, ChangeSide.After)
        assertEquals(exercise.expected, back.joinToString("") { it.text })
        assertTrue(back.first { it.text == "Czy" }.isChanged)
    }
}
