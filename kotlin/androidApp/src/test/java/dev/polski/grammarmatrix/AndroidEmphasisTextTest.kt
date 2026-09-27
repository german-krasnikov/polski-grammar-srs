package dev.polski.grammarmatrix

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import polski.presentation.EndingPart
import polski.ui.screens.androidEmphasisAfterColor
import polski.ui.screens.androidEmphasisAnnotatedText

/**
 * Emphasis contract (`ContrastHighlightPlan.md` "Контракт выделения"): the changed fragment's
 * span style is the one place both hosts and tests read the before/after distinction from.
 * `before` (warm red) never gets a native solid [TextDecoration] — its dashed line is drawn
 * separately by [polski.ui.screens.AndroidEmphasisText] because Compose has no dashed decoration.
 * `after` (cool accent) gets a native solid underline. Both bold the changed fragment; unchanged
 * text carries no span style at all (E1, EmphasisUXAudit-2026-09-27.md).
 */
class AndroidEmphasisTextTest {
    private val red = Color(0xFFB3261E)
    private val teal = Color(0xFF32685A)
    private val parts = listOf(EndingPart("kup", false), EndingPart("iłem", true, true))

    @Test fun beforeChangedSpanIsBoldWithNoNativeUnderline() {
        val text = androidEmphasisAnnotatedText(parts, before = true, color = red)
        assertEquals("kupiłem", text.text)
        val changed = text.spanStyles.single { it.start == 3 && it.end == 7 }
        assertEquals(SpanStyle(color = red, fontWeight = FontWeight.Bold, textDecoration = null), changed.item)
    }

    @Test fun afterChangedSpanIsBoldWithSolidUnderline() {
        val text = androidEmphasisAnnotatedText(parts, before = false, color = teal)
        val changed = text.spanStyles.single { it.start == 3 && it.end == 7 }
        assertEquals(
            SpanStyle(color = teal, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline),
            changed.item,
        )
    }

    @Test fun unchangedTextCarriesNoSpanStyle() {
        val text = androidEmphasisAnnotatedText(parts, before = true, color = red)
        assertEquals(0, text.spanStyles.count { it.start == 0 && it.end == 3 })
    }

    // `after`'s color must be a fixed constant, never MaterialTheme.colorScheme.secondary: under
    // Material You (API 31+) that role is wallpaper-derived and can drift to red/orange/yellow,
    // the same mechanism that made the old `primary`-based bug warm. androidEmphasisAfterColor
    // takes no ColorScheme, so it structurally cannot read a dynamic theme role
    // (EmphasisUXAudit-2026-09-27.md blocker on A1/08a2f4b).
    @Test fun afterColorIsFixedNotThemeSourced() {
        assertEquals(Color(0xFF32685A), androidEmphasisAfterColor(dark = false))
        assertEquals(Color(0xFFB8D8CE), androidEmphasisAfterColor(dark = true))
    }

    @Test fun afterColorMeetsContrastOnBothAppBackgrounds() {
        val lightBackground = Color(0xFFF7F4EC)
        val darkBackground = Color(0xFF101B21)
        assertTrue(contrastRatio(androidEmphasisAfterColor(dark = false), lightBackground) >= 4.5)
        assertTrue(contrastRatio(androidEmphasisAfterColor(dark = true), darkBackground) >= 4.5)
    }

    private fun contrastRatio(a: Color, b: Color): Double {
        val la = a.luminance().toDouble()
        val lb = b.luminance().toDouble()
        val lighter = maxOf(la, lb)
        val darker = minOf(la, lb)
        return (lighter + 0.05) / (darker + 0.05)
    }
}
