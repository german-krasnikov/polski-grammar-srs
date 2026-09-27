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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import polski.presentation.EndingPart

/**
 * Emphasis contract (`ContrastHighlightPlan.md` "Контракт выделения"): the changed fragment's
 * span style is the one place both [AndroidEmphasisText] and its tests read the before/after
 * distinction from. `after` gets a native solid underline; `before`'s dashed line has no Compose
 * [TextDecoration] equivalent, so its span carries none — [AndroidEmphasisText] draws it itself.
 * Both bold the changed fragment; unchanged text carries no span style (E1, EmphasisUXAudit-2026-09-27.md).
 */
fun androidEmphasisAnnotatedText(parts: List<EndingPart>, before: Boolean, color: Color): AnnotatedString =
    buildAnnotatedString {
        parts.forEach { part ->
            if (part.isChanged) withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold,
                textDecoration = if (before) null else TextDecoration.Underline)) { append(part.text) }
            else append(part.text)
        }
    }

/**
 * The one Android rendering path for a before/after contrast fragment, used for both the main
 * sentence (`AndroidTrainingScreen.kt`) and style-block rows (`AndroidStyleBlocks.kt`) — no
 * second, simplified path draws this role anywhere else on this host. `before` = warm red
 * ([MaterialTheme.colorScheme.error]) with a hand-drawn dashed underline; `after` = cool accent
 * ([MaterialTheme.colorScheme.secondary], teal — `colorScheme.primary` is this app's warm
 * brown/orange and the contract forbids red/orange/yellow for `after`) with a native solid
 * underline. Compose has no dashed [TextDecoration], so `before`'s line is drawn with
 * [PathEffect.dashPathEffect] instead of relying on the span style.
 */
@Composable
fun AndroidEmphasisText(
    parts: List<EndingPart>,
    before: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    fontWeight: FontWeight? = null,
) {
    val color = if (before) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
    if (!before) {
        Text(text = androidEmphasisAnnotatedText(parts, before, color), style = style, fontWeight = fontWeight, modifier = modifier)
        return
    }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val changedIndices = remember(parts) {
        parts.flatMapIndexed { index, part ->
            if (!part.isChanged) emptyList() else {
                val start = parts.take(index).sumOf { it.text.length }
                (start until start + part.text.length).toList()
            }
        }
    }
    Text(
        text = androidEmphasisAnnotatedText(parts, before, color),
        style = style,
        fontWeight = fontWeight,
        onTextLayout = { layout = it },
        modifier = modifier.drawWithContent {
            drawContent()
            val textLayout = layout ?: return@drawWithContent
            // Contract §3: stroke ≥1.5dp/2px, ≥3px below the baseline so the dash never merges
            // with Polish diacritics (ą, ę).
            val effect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx()))
            val offset = 3.dp.toPx()
            changedIndices.forEach { index ->
                if (index >= textLayout.layoutInput.text.length) return@forEach
                val box = textLayout.getBoundingBox(index)
                val y = box.bottom + offset
                drawLine(color, Offset(box.left, y), Offset(box.right, y),
                    strokeWidth = 2.dp.toPx(), pathEffect = effect)
            }
        },
    )
}
