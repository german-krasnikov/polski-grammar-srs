package polski.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import polski.presentation.ContrastPair
import polski.presentation.ChangeSide
import polski.presentation.EndingPart

/** Keeps the spoken Polish text intact while making the changed fragment visible beyond colour. */
fun contrastAnnotatedText(parts: List<EndingPart>, changedColor: Color): AnnotatedString = buildAnnotatedString {
    parts.forEach { part ->
        if (part.isChanged) withStyle(SpanStyle(color = changedColor, fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline)) { append(part.text) }
        else append(part.text)
    }
}

/** One spoken comparison; fragment styling does not split or repeat the accessible Polish forms. */
@Composable
fun ContrastPairText(pair: ContrastPair, modifier: Modifier = Modifier) {
    Column(modifier.clearAndSetSemantics {
        contentDescription = "Было: ${pair.from}. Стало: ${pair.to}"
    }, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Column {
            Text("Было", style = MaterialTheme.typography.labelMedium)
            MarkedContrastText(pair.parts(ChangeSide.Before), MaterialTheme.colorScheme.error, dashed = true)
        }
        Text("→", style = MaterialTheme.typography.bodyLarge)
        Column {
            Text("Стало", style = MaterialTheme.typography.labelMedium)
            MarkedContrastText(pair.parts(ChangeSide.After), MaterialTheme.colorScheme.primary, dashed = false)
        }
    }
}

/**
 * This exercise's own target row before reveal (Emphasis contract §5, EmphasisUXAudit E8):
 * only the already-known "Было" form is shown — the answer ("Стало") is withheld from visible
 * text, semantics and every other host surface alike, mirroring `matrixContrastMasked` in the
 * web target (804f6c6). [before] itself is never the leak — it is the exercise's own nominative
 * form, already visible elsewhere on the card — so the contentDescription may name it freely.
 */
@Composable
fun ContrastPairTextMasked(before: String, modifier: Modifier = Modifier) {
    Column(modifier.clearAndSetSemantics {
        contentDescription = "Было: $before. Ответ скрыт до проверки."
    }, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("Было: $before", style = MaterialTheme.typography.bodyMedium)
        Text("→", style = MaterialTheme.typography.bodyLarge)
        Text("Стало: ?", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MarkedContrastText(parts: List<EndingPart>, color: Color, dashed: Boolean) {
    var layout by androidx.compose.runtime.remember { mutableStateOf<TextLayoutResult?>(null) }
    val changedIndices = parts.flatMapIndexed { index, part ->
        if (!part.isChanged) emptyList() else {
            val start = parts.take(index).sumOf { it.text.length }
            (start until start + part.text.length).toList()
        }
    }
    Text(
        text = buildAnnotatedString {
            parts.forEach { part ->
                if (part.isChanged) withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) { append(part.text) }
                else append(part.text)
            }
        },
        style = MaterialTheme.typography.bodyMedium,
        onTextLayout = { layout = it },
        modifier = Modifier.drawWithContent {
            drawContent()
            val textLayout = layout ?: return@drawWithContent
            val effect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx())) else null
            changedIndices.forEach { index ->
                if (index >= textLayout.layoutInput.text.length) return@forEach
                val box = textLayout.getBoundingBox(index)
                drawLine(color, Offset(box.left, box.bottom), Offset(box.right, box.bottom),
                    strokeWidth = 1.dp.toPx(), pathEffect = effect)
            }
        },
    )
}
