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
 * pl-ru golden/parity (EnRuAcceptance-2026-09-28.md §7 fix list, P02), re-verified and kept as
 * of the 2026-09-28 correction: `case.gen.neg`'s `changes` must stay exactly what the *frozen*
 * React oracle produces. That oracle is not "the current `src/training/generator.ts`" in the
 * abstract — it is byte-pinned to commit `df59774e` by `tests/fixtures/kotlin-parity/capture.ts`'s
 * `sourceRevision` constant and enforced by `tests/parity-fixtures.test.ts`'s
 * `records exact captured source and fixture hashes` (hashes `git show df59774e:<path>` for
 * every captured source file, `generator.ts` included, independent of the working tree).
 * Editing `generator.ts` to add the insertion `FormChange` regenerates the fixture from
 * *today's* tree but leaves `sourceRevision` unmoved — which fails that hash-pin test
 * immediately, confirmed by trying it (`git show df59774e:src/data/nouns.ts` already differs
 * from the working tree for unrelated reasons, so the pin is a real, load-bearing invariant,
 * not a stale copy). Moving the pin forward is a deliberate act belonging to whoever owns the
 * legacy React baseline (parallel to `tests/fixtures/core-golden/README.md`'s "these files do
 * not change again ... until a new pin is deliberately taken"), not something this fix can do
 * unilaterally without breaking the HARD pl-ru-byte-identical requirement.
 *
 * So: S3's insertion-`FormChange` model for "Nie" (`a9f9575`, EmphasisUXAudit E5) is *correct*
 * UX and matches `ContrastHighlightPlan.md`'s Emphasis contract for insertion/deletion, but
 * cannot be reproduced by Kotlin alone without diverging from the pinned oracle — `ffab9b4`'s
 * revert was the right outcome even though its stated rationale (misreading the plan's
 * "generator does not change for this feature" line as covering the `changes` list) was wrong.
 * E5 for `case.gen.neg`/chain step 3 is REOPENED and deferred pending an explicit decision to
 * move the `df59774e` pin (see `Plans/Kotlin/EmphasisUXAudit-2026-09-27.md`'s dated addendum).
 * "Czy" needs no dedicated `FormChange`: `changeHighlightParts`'s own single-word-insertion
 * detection (`singleWordInsertionOrDeletionParts`) already isolates it from "Widzisz" -> "Czy
 * widzisz" without diverging from the pinned oracle.
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
        assertFalse(back.any { it.text == "Nie" && it.isChanged }, "Nie is not part of any change; the pinned React oracle never highlights it either (E5 reopened, see class doc)")
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
