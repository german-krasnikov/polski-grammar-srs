// Generated from dsl/learning/error-hunt.examples by biome generate. Do not edit.
package polski.cloze.policy

import kotlin.test.Test
import kotlin.test.assertEquals

/** OK: 18 cases */
class ErrorHuntPolicyExamplesTest {
    @Test fun tapOutcome() {
        assertEquals("hit", Policy.tapOutcome(Policy.TapOutcomeInput(tapIsError = true)), "tap-error-word")
        assertEquals("miss", Policy.tapOutcome(Policy.TapOutcomeInput(tapIsError = false)), "tap-correct-word")
    }

    @Test fun shouldReveal() {
        assertEquals("hidden", Policy.shouldReveal(Policy.ShouldRevealInput(missCount = 0.0, found = false)), "zero-misses-not-found")
        assertEquals("hidden", Policy.shouldReveal(Policy.ShouldRevealInput(missCount = 1.0, found = false)), "one-miss-not-found")
        assertEquals("revealed", Policy.shouldReveal(Policy.ShouldRevealInput(missCount = 2.0, found = false)), "two-misses-not-found")
        assertEquals("revealed", Policy.shouldReveal(Policy.ShouldRevealInput(missCount = 3.0, found = false)), "three-misses")
        assertEquals("revealed", Policy.shouldReveal(Policy.ShouldRevealInput(missCount = 0.0, found = true)), "found-zero-misses")
        assertEquals("revealed", Policy.shouldReveal(Policy.ShouldRevealInput(missCount = 1.0, found = true)), "found-one-miss")
    }

    @Test fun scoreClass() {
        assertEquals("perfect", Policy.scoreClass(Policy.ScoreClassInput(found = true, missCount = 0.0, answerCorrect = true)), "perfect")
        assertEquals("found_late", Policy.scoreClass(Policy.ScoreClassInput(found = true, missCount = 1.0, answerCorrect = true)), "found-late-one")
        assertEquals("found_late", Policy.scoreClass(Policy.ScoreClassInput(found = true, missCount = 2.0, answerCorrect = true)), "found-late-two")
        assertEquals("revealed_correct", Policy.scoreClass(Policy.ScoreClassInput(found = false, missCount = 2.0, answerCorrect = true)), "revealed-correct")
        assertEquals("wrong_form", Policy.scoreClass(Policy.ScoreClassInput(found = true, missCount = 0.0, answerCorrect = false)), "wrong-form-found")
        assertEquals("wrong_form", Policy.scoreClass(Policy.ScoreClassInput(found = false, missCount = 2.0, answerCorrect = false)), "wrong-form-revealed")
    }

    @Test fun distractorValid() {
        assertEquals("valid", Policy.distractorValid(Policy.DistractorValidInput(distractorEqualsCorrect = false, distractorInAccepted = false)), "valid-different-form")
        assertEquals("invalid", Policy.distractorValid(Policy.DistractorValidInput(distractorEqualsCorrect = true, distractorInAccepted = false)), "equals-correct")
        assertEquals("invalid", Policy.distractorValid(Policy.DistractorValidInput(distractorEqualsCorrect = false, distractorInAccepted = true)), "in-accepted")
        assertEquals("invalid", Policy.distractorValid(Policy.DistractorValidInput(distractorEqualsCorrect = true, distractorInAccepted = true)), "both-true")
    }
}
