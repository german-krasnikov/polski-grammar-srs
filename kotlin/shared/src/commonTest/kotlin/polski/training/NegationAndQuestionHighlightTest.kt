package polski.training

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import polski.model.SentenceSeed
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.presentation.ChangeSide
import polski.presentation.sentenceHighlightParts

/**
 * pl-ru golden/parity (EnRuAcceptance-2026-09-28.md §7 fix list, P02): `case.gen.neg` and
 * `sentence.question`'s `changes` must stay exactly what the real React generator produces
 * (`tests/fixtures/kotlin-parity/exercises.json`'s `E-case.gen.neg-*`/`E-sentence.question-*`
 * and `C-*-03`), because `.change-list` renders one row per `changes` entry on both sides
 * (`src/ui/App.tsx`, `StyleComposer.kt`'s `Block.Changes`). A dedicated `"" -> "Nie"/"Czy"`
 * insertion `FormChange` (`a9f9575`, S3 EmphasisUXAudit E5) was never part of that generator
 * output, so it rendered as an extra row Kotlin showed and the live React reference did not —
 * `kotlin-parity-chain.spec.ts`'s P02 caught it on chain step 3. Restored to golden: "Nie" is
 * plain text next to the one real change (React never highlights it either); "Czy" is
 * highlighted through the shared `changeHighlightParts`' own single-word-insertion detection
 * (`singleWordInsertionOrDeletionParts`), which already isolates "Czy" from "Widzisz" -> "Czy
 * widzisz" without any dedicated insertion `FormChange`.
 */
class NegationAndQuestionHighlightTest {
    private class FixedDraw : RandomSource { override fun nextDouble(): Double = 0.0 }
    private fun factory() = PlExerciseEngine(FixedDraw(), ExerciseIdFactory { "generated" })

    @Test
    fun negationParticleIsPlainTextForCaseGenNeg() {
        val exercise = factory().generateForSkill("case.gen.neg", SentenceSeed("wife", "beautiful"))
        assertEquals(1, exercise.changes.size, "golden fixture has exactly one change, not a separate Nie insertion")

        val back = sentenceHighlightParts(exercise.expected, exercise.changes, ChangeSide.After)
        assertEquals(exercise.expected, back.joinToString("") { it.text })
        assertFalse(back.any { it.text == "Nie" && it.isChanged }, "Nie is not part of any change; React never highlights it either")
    }

    @Test
    fun negationParticleStaysPlainInTheChainStepToo() {
        val step = factory().generateChain(SentenceSeed("wife", "beautiful"))[2]
        assertEquals("case.gen.neg", step.primarySkill)
        assertEquals(1, step.changes.size, "golden fixture's C-*-03 chain step has exactly one change")

        val back = sentenceHighlightParts(step.expected, step.changes, ChangeSide.After)
        assertFalse(back.any { it.text == "Nie" && it.isChanged })
    }

    @Test
    fun questionParticleIsHighlightedViaSingleWordInsertionDetection() {
        val exercise = factory().generateForSkill("sentence.question", SentenceSeed("wife", "beautiful"))
        assertEquals(1, exercise.changes.size, "golden fixture has exactly one change, not a separate Czy insertion")

        val front = sentenceHighlightParts(exercise.source, exercise.changes, ChangeSide.Before)
        assertEquals(exercise.source, front.joinToString("") { it.text })
        assertFalse(front.any { it.isChanged }, "front must not leak the inserted particle before reveal")

        val back = sentenceHighlightParts(exercise.expected, exercise.changes, ChangeSide.After)
        assertEquals(exercise.expected, back.joinToString("") { it.text })
        assertEquals("Czy", back.first { it.isChanged }.text)
    }
}
