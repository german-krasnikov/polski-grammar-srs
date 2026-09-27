package dev.polski.grammarmatrix

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import polski.preferences.Motion
import polski.preferences.UserPreferencesV2

/**
 * D5: the "Анимации" switch (`UserPreferencesV2.animationsEnabled`) and the system `Motion.Reduced`
 * setting both collapse into the same [motionReduced] gate — whichever composable reads it treats
 * "animations off" and "system reduced motion" identically (instant motion, Rive never mounted).
 */
class MotionGateTest {
    @Test fun animationsEnabledAndSystemMotionMeansNotReduced() {
        assertFalse(motionReduced(UserPreferencesV2(animationsEnabled = true, motion = Motion.System)))
    }

    @Test fun animationsDisabledIsReducedEvenWithSystemMotion() {
        assertTrue(motionReduced(UserPreferencesV2(animationsEnabled = false, motion = Motion.System)))
    }

    @Test fun systemReducedMotionIsReducedEvenWithAnimationsEnabled() {
        assertTrue(motionReduced(UserPreferencesV2(animationsEnabled = true, motion = Motion.Reduced)))
    }

    @Test fun bothOffIsStillReduced() {
        assertTrue(motionReduced(UserPreferencesV2(animationsEnabled = false, motion = Motion.Reduced)))
    }
}
