package polski.cloze

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ErrorHuntSessionTest {
    // tokens: ["Widzę", "żona", "tutaj."], errorTokenIndex=1 (żona is the error)
    private val ex = ErrorHuntExercise(
        id = "hunt-1",
        tokens = listOf("Widzę", "żona", "tutaj."),
        errorTokenIndex = 1,
        correctToken = "żonę",
        errorForm = "żona",
        correctForm = "żonę",
        options = listOf("żonę", "żona", "żony"),
        gapLexemeKey = "noun:wife",
        skillId = "case.acc",
        ruleText = null,
        sourceExerciseId = "ex-1",
    )
    private val ex2 = ex.copy(id = "hunt-2")

    private fun state(vararg exercises: ErrorHuntExercise = arrayOf(ex)) =
        ErrorHuntSessionState(exercises = exercises.toList())

    // RED: Choose ignored before error is found or revealed
    @Test fun chooseIgnoredBeforeFound() {
        val s = state()
        val after = reduceErrorHuntSession(s, ErrorHuntAction.Choose("hunt-1", "żonę"))
        assertEquals(emptyMap(), after.answers)
    }

    // RED: shouldReveal false before 2 misses
    @Test fun notRevealedBeforeTwoMisses() {
        val s = state()
        val s1 = reduceErrorHuntSession(s, ErrorHuntAction.TapToken("hunt-1", 0))
        assertFalse(s1.shouldReveal("hunt-1"))
    }

    // RED: shouldReveal true after 2 misses
    @Test fun revealAfterTwoMisses() {
        val s = state()
        val s1 = reduceErrorHuntSession(s, ErrorHuntAction.TapToken("hunt-1", 0))
        val s2 = reduceErrorHuntSession(s1, ErrorHuntAction.TapToken("hunt-1", 2))
        assertTrue(s2.shouldReveal("hunt-1"))
    }

    @Test fun tapErrorAddsToFound() {
        val s = state()
        val after = reduceErrorHuntSession(s, ErrorHuntAction.TapToken("hunt-1", 1))
        assertTrue("hunt-1" in after.found)
        assertEquals(0, after.missCounts["hunt-1"] ?: 0)
    }

    @Test fun tapCorrectWordIncrementsMissCount() {
        val s = state()
        val s1 = reduceErrorHuntSession(s, ErrorHuntAction.TapToken("hunt-1", 0))
        assertEquals(1, s1.missCounts["hunt-1"])
        val s2 = reduceErrorHuntSession(s1, ErrorHuntAction.TapToken("hunt-1", 0))
        assertEquals(2, s2.missCounts["hunt-1"])
    }

    @Test fun tapErrorTwiceIdempotent() {
        val s = state()
        val s1 = reduceErrorHuntSession(s, ErrorHuntAction.TapToken("hunt-1", 1))
        val s2 = reduceErrorHuntSession(s1, ErrorHuntAction.TapToken("hunt-1", 1))
        assertEquals(s1.found, s2.found)
    }

    @Test fun chooseAcceptedAfterFound() {
        val s = state()
        val s1 = reduceErrorHuntSession(s, ErrorHuntAction.TapToken("hunt-1", 1))
        val s2 = reduceErrorHuntSession(s1, ErrorHuntAction.Choose("hunt-1", "żonę"))
        assertEquals("żonę", s2.answers["hunt-1"])
    }

    @Test fun chooseAcceptedAfterRevealed() {
        val s = state()
        val s1 = reduceErrorHuntSession(s, ErrorHuntAction.TapToken("hunt-1", 0))
        val s2 = reduceErrorHuntSession(s1, ErrorHuntAction.TapToken("hunt-1", 2))
        // Now revealed; tap the error so Choose can proceed (but revealed means Choose is also OK)
        val s3 = reduceErrorHuntSession(s2, ErrorHuntAction.Choose("hunt-1", "żonę"))
        assertEquals("żonę", s3.answers["hunt-1"])
    }

    @Test fun chooseIdempotentWhenAlreadyAnswered() {
        val s = state()
        val s1 = reduceErrorHuntSession(s, ErrorHuntAction.TapToken("hunt-1", 1))
        val s2 = reduceErrorHuntSession(s1, ErrorHuntAction.Choose("hunt-1", "żonę"))
        val s3 = reduceErrorHuntSession(s2, ErrorHuntAction.Choose("hunt-1", "żony"))
        assertEquals("żonę", s3.answers["hunt-1"]) // unchanged
    }

    @Test fun tapAfterAnsweredIsIdempotent() {
        val s = state()
        val s1 = reduceErrorHuntSession(s, ErrorHuntAction.TapToken("hunt-1", 1))
        val s2 = reduceErrorHuntSession(s1, ErrorHuntAction.Choose("hunt-1", "żonę"))
        val s3 = reduceErrorHuntSession(s2, ErrorHuntAction.TapToken("hunt-1", 0))
        assertEquals(s2.missCounts, s3.missCounts) // no extra miss
    }

    @Test fun nextAndPreviousBounds() {
        val s = state(ex, ex2)
        val sNext = reduceErrorHuntSession(s, ErrorHuntAction.Next)
        assertEquals(1, sNext.currentIndex)
        val sNextAgain = reduceErrorHuntSession(sNext, ErrorHuntAction.Next)
        assertEquals(1, sNextAgain.currentIndex) // at last index, no-op
        val sPrev = reduceErrorHuntSession(sNext, ErrorHuntAction.Previous)
        assertEquals(0, sPrev.currentIndex)
        val sPrevAgain = reduceErrorHuntSession(sPrev, ErrorHuntAction.Previous)
        assertEquals(0, sPrevAgain.currentIndex) // at 0, no-op
    }

    @Test fun restartClearsEverything() {
        val s = state()
        val s1 = reduceErrorHuntSession(s, ErrorHuntAction.TapToken("hunt-1", 0))
        val s2 = reduceErrorHuntSession(s1, ErrorHuntAction.TapToken("hunt-1", 1))
        val s3 = reduceErrorHuntSession(s2, ErrorHuntAction.Choose("hunt-1", "żonę"))
        val restarted = reduceErrorHuntSession(s3, ErrorHuntAction.Restart)
        assertEquals(0, restarted.currentIndex)
        assertEquals(emptyMap(), restarted.missCounts)
        assertEquals(emptySet(), restarted.found)
        assertEquals(emptyMap(), restarted.answers)
    }

    @Test fun scoreClassPerfect() {
        val s = state()
        val s1 = reduceErrorHuntSession(s, ErrorHuntAction.TapToken("hunt-1", 1)) // found, 0 misses
        val s2 = reduceErrorHuntSession(s1, ErrorHuntAction.Choose("hunt-1", "żonę")) // correct
        assertEquals("perfect", s2.scoreClass("hunt-1"))
    }

    @Test fun scoreClassFoundLate() {
        val s = state()
        val s1 = reduceErrorHuntSession(s, ErrorHuntAction.TapToken("hunt-1", 0)) // miss
        val s2 = reduceErrorHuntSession(s1, ErrorHuntAction.TapToken("hunt-1", 1)) // found, 1 miss
        val s3 = reduceErrorHuntSession(s2, ErrorHuntAction.Choose("hunt-1", "żonę"))
        assertEquals("found_late", s3.scoreClass("hunt-1"))
    }

    @Test fun scoreClassRevealedCorrect() {
        val s = state()
        val s1 = reduceErrorHuntSession(s, ErrorHuntAction.TapToken("hunt-1", 0))
        val s2 = reduceErrorHuntSession(s1, ErrorHuntAction.TapToken("hunt-1", 2)) // 2 misses → revealed
        val s3 = reduceErrorHuntSession(s2, ErrorHuntAction.Choose("hunt-1", "żonę")) // correct
        assertEquals("revealed_correct", s3.scoreClass("hunt-1"))
    }

    @Test fun scoreClassWrongForm() {
        val s = state()
        val s1 = reduceErrorHuntSession(s, ErrorHuntAction.TapToken("hunt-1", 1)) // found
        val s2 = reduceErrorHuntSession(s1, ErrorHuntAction.Choose("hunt-1", "żony")) // wrong
        assertEquals("wrong_form", s2.scoreClass("hunt-1"))
    }

    @Test fun scoreClassNullWhenNotAnswered() {
        assertNull(state().scoreClass("hunt-1"))
    }

    @Test fun summaryCountsPerBucket() {
        val s = state(ex, ex2)
        // ex: perfect (found on first tap, correct answer)
        val s1 = reduceErrorHuntSession(s, ErrorHuntAction.TapToken("hunt-1", 1))
        val s2 = reduceErrorHuntSession(s1, ErrorHuntAction.Choose("hunt-1", "żonę"))
        // ex2: not answered yet
        val summary = s2.summary()
        assertEquals(1, summary["perfect"])
        assertNull(summary["found_late"])
    }
}
