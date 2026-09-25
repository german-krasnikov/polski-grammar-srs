package dev.polski.grammarmatrix

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import polski.preferences.Appearance

class AppearanceResolutionTest {
    @Test fun systemChoiceTracksCurrentSystemMode() {
        assertFalse(resolveDarkAppearance(Appearance.System, false))
        assertTrue(resolveDarkAppearance(Appearance.System, true))
    }

    @Test fun manualChoiceOverridesSystemMode() {
        assertFalse(resolveDarkAppearance(Appearance.Light, true))
        assertTrue(resolveDarkAppearance(Appearance.Dark, false))
    }
}
