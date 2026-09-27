package dev.polski.grammarmatrix

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.junit.Assert.assertEquals
import org.junit.Test
import polski.presentation.EndingPart
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
}
