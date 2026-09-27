package polski.training

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import polski.presentation.ChangeSide
import polski.presentation.EndingPart
import polski.presentation.sentenceHighlightParts

/**
 * C1 (EmphasisUXAudit E4): `aspect` and `mixed` must carry a single literal `FormChange.to`
 * (gender alternatives belong in `accepted`, never joined with " / " into the change itself).
 * A slash-joined value never occurs verbatim in the rendered sentence, so `sentenceHighlightParts`
 * silently found nothing to highlight in the эталон — this locks in that a highlighted span
 * exists once the change is a real substring of `expected`.
 */
class AspectMixedHighlightTest {
    private class FixedDraw : RandomSource { override fun nextDouble(): Double = 0.0 }
    private fun factory() = ExerciseFactory(FixedDraw(), ExerciseIdFactory { "generated" })

    @Test
    fun aspectChangeHasNoAlternativesAndHighlightsInTheAnswer() {
        val exercise = factory().generateForSkill("aspect")
        exercise.changes.forEach { change ->
            assertFalse(" / " in change.to, "FormChange.to must be one literal: ${change.to}")
            assertTrue(change.to in exercise.expected, "change.to must occur verbatim in expected, or the highlight silently disappears: ${change.to}")
        }

        val back = sentenceHighlightParts(exercise.expected, exercise.changes, ChangeSide.After)
        assertEquals(exercise.expected, back.joinToString("") { it.text })
        assertTrue(back.any(EndingPart::isChanged), "the change must be found and highlighted in the эталон")
    }

    @Test
    fun mixedChangesHaveNoAlternativesAndBothHighlightInTheAnswer() {
        val exercise = factory().generateForSkill("mixed")
        exercise.changes.forEach { change ->
            assertFalse(" / " in change.to, "FormChange.to must be one literal: ${change.to}")
            assertTrue(change.to in exercise.expected, "change.to must occur verbatim in expected, or the highlight silently disappears: ${change.to}")
        }

        val back = sentenceHighlightParts(exercise.expected, exercise.changes, ChangeSide.After)
        assertEquals(exercise.expected, back.joinToString("") { it.text })
        assertTrue(back.any(EndingPart::isChanged), "both changes must be found and highlighted in the эталон")
    }
}
