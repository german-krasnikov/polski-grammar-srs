package polski.ui.screens

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import polski.presentation.ChangeSide
import polski.presentation.EndingPart

/**
 * Emphasis contract (`ContrastHighlightPlan.md` "Контракт выделения"): the changed fragment's
 * span style is the one place both [AndroidEmphasisText] and its tests read the before/after
 * distinction from. `after` gets a native solid underline; `before`'s dashed line has no Compose
 * [TextDecoration] equivalent, so its span carries none — [AndroidEmphasisText] draws it itself.
 * Both bold the changed fragment; unchanged text carries no span style (E1, EmphasisUXAudit-2026-09-27.md).
 *
 * Each changed part's own [EndingPart.side] decides its role, not the call-wide [before] — a style
 * block's prose can carry both roles in one running list (`styleTextHighlightParts`, S4/E7:
 * "mixed-role list ... cannot be classed as a single role by its caller alone"). [before] is only
 * the fallback for a changed part with no [EndingPart.side] of its own.
 */
fun androidEmphasisAnnotatedText(parts: List<EndingPart>, before: Boolean, beforeColor: Color, afterColor: Color): AnnotatedString =
    buildAnnotatedString {
        parts.forEach { part ->
            if (part.isChanged) {
                val side = part.side ?: if (before) ChangeSide.Before else ChangeSide.After
                val color = if (side == ChangeSide.Before) beforeColor else afterColor
                withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold,
                    textDecoration = if (side == ChangeSide.Before) null else TextDecoration.Underline)) { append(part.text) }
            } else append(part.text)
        }
    }

/**
 * Fixed cool-accent (teal) colors for the `after` role, validated ≥4.5:1 against both app
 * backgrounds ([AndroidEmphasisTextTest]). These are **not** sourced from
 * [MaterialTheme.colorScheme.secondary]: on API 31+, Material You dynamic color derives
 * `secondary` from the device wallpaper, so it is not guaranteed to stay a cool accent — it can
 * drift to red/orange/yellow, which the contract forbids for `after`
 * (EmphasisUXAudit-2026-09-27.md blocker on A1/08a2f4b). `error`, used for `before`, is unaffected:
 * Material 3's dynamic color spec keeps the error palette fixed regardless of wallpaper.
 */
private val emphasisAfterLight = Color(0xFF32685A)
private val emphasisAfterDark = Color(0xFFB8D8CE)

/** Resolves the `after` role's color. Takes no [ColorScheme][androidx.compose.material3.ColorScheme], so it cannot read a dynamic theme role. */
fun androidEmphasisAfterColor(dark: Boolean): Color = if (dark) emphasisAfterDark else emphasisAfterLight

/**
 * The one Android rendering path for a before/after contrast fragment, used for the main sentence
 * (`AndroidTrainingScreen.kt`) and every style-block kind that highlights prose
 * (`AndroidStyleBlocks.kt`: Formula/Rule/Scene/NativeParallel/Examples/WhyOnDemand, S4/E7) — no
 * second, simplified path draws this role anywhere else on this host. `before` = warm red
 * ([MaterialTheme.colorScheme.error], unaffected by dynamic color) with a hand-drawn dashed
 * underline; `after` = fixed cool accent ([androidEmphasisAfterColor]) with a native solid
 * underline. Compose has no dashed [TextDecoration], so `before`'s line is drawn with
 * [PathEffect.dashPathEffect] instead of relying on the span style. Each part's role comes from its
 * own [EndingPart.side] when set (a style block's prose can carry both roles in one [parts] list);
 * [before] is only the fallback default and the initial role for the whole call's un-sided parts.
 * [color]/[fontStyle] style the *unchanged* text the same way `Text(block.text, ...)` used to.
 */
@Composable
fun AndroidEmphasisText(
    parts: List<EndingPart>,
    before: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = null,
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val beforeColor = MaterialTheme.colorScheme.error
    val afterColor = androidEmphasisAfterColor(dark)
    fun roleOf(part: EndingPart) = part.side ?: if (before) ChangeSide.Before else ChangeSide.After
    val hasBeforeRole = parts.any { it.isChanged && roleOf(it) == ChangeSide.Before }
    if (!hasBeforeRole) {
        Text(text = androidEmphasisAnnotatedText(parts, before, beforeColor, afterColor), style = style,
            fontWeight = fontWeight, color = color, fontStyle = fontStyle, modifier = modifier)
        return
    }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val dashedIndices = remember(parts, before) {
        parts.flatMapIndexed { index, part ->
            if (!part.isChanged || roleOf(part) != ChangeSide.Before) emptyList() else {
                val start = parts.take(index).sumOf { it.text.length }
                (start until start + part.text.length).toList()
            }
        }
    }
    Text(
        text = androidEmphasisAnnotatedText(parts, before, beforeColor, afterColor),
        style = style,
        fontWeight = fontWeight,
        color = color,
        fontStyle = fontStyle,
        onTextLayout = { layout = it },
        modifier = modifier.drawWithContent {
            drawContent()
            val textLayout = layout ?: return@drawWithContent
            // Contract §3: stroke ≥1.5dp/2px, ≥3px below the baseline so the dash never merges
            // with Polish diacritics (ą, ę).
            val effect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx()))
            val offset = 3.dp.toPx()
            dashedIndices.forEach { index ->
                if (index >= textLayout.layoutInput.text.length) return@forEach
                val box = textLayout.getBoundingBox(index)
                val y = box.bottom + offset
                drawLine(beforeColor, Offset(box.left, y), Offset(box.right, y),
                    strokeWidth = 2.dp.toPx(), pathEffect = effect)
            }
        },
    )
}
