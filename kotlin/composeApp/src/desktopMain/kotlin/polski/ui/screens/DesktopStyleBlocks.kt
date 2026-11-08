package polski.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import polski.presentation.Block
import polski.ui.contrastAnnotatedText

/**
 * W2.2: this preview's own render of whatever [Block]s `TrainingCardModel.trainingCardModel`
 * composed for a [polski.presentation.CardSection.Styled] entry — one visual per
 * [polski.presentation.BlockKind], mirroring `AndroidStyleBlocks.kt`'s split but with this preview's
 * own [contrastAnnotatedText] highlighter (single flat color per call) instead of Android's
 * side-aware emphasis text. [Block.Formula] keeps the pre-W2.2 "ЗАПОМНИ" surface's own shape/colors
 * exactly; [Block.Contrast] is the focus row that used to live inside that same surface — now its
 * own section since a recipe may place it separately (W2.1 `TrainingCardModel.kt`).
 */
@Composable
internal fun DesktopBlockView(block: Block) {
    when (block) {
        is Block.Formula -> Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("ЗАПОМНИ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text(contrastAnnotatedText(block.parts, MaterialTheme.colorScheme.primary), fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
        }
        is Block.Rule -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Правило", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(contrastAnnotatedText(block.parts, MaterialTheme.colorScheme.primary))
            Text(block.detail)
        }
        is Block.Table -> Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (block.caption.isNotEmpty()) Text(block.caption, style = MaterialTheme.typography.labelMedium)
                block.rows.forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (row.label.isNotEmpty()) Text(row.label, style = MaterialTheme.typography.labelLarge)
                        Text(contrastAnnotatedText(row.before, MaterialTheme.colorScheme.error))
                        Text("→")
                        Text(contrastAnnotatedText(row.after, MaterialTheme.colorScheme.primary), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        is Block.Scene -> Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("“", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(contrastAnnotatedText(block.parts, MaterialTheme.colorScheme.primary), style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        is Block.NativeParallel -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Родной ↔ изучаемый", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            block.pairs.forEach { pair ->
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(pair.native, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text(contrastAnnotatedText(pair.targetParts, MaterialTheme.colorScheme.primary), style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        }
                        Text(if (pair.matches) "Совпадает" else "Отличается", style = MaterialTheme.typography.labelSmall,
                            color = if (pair.matches) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        if (pair.note.isNotEmpty()) Text(pair.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        is Block.Examples -> if (block.items.isNotEmpty()) Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Примеры", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            block.items.forEachIndexed { index, _ ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("•")
                    Text(contrastAnnotatedText(block.itemParts[index], MaterialTheme.colorScheme.primary))
                }
            }
        }
        is Block.WhyOnDemand -> {
            var expanded by remember(block) { mutableStateOf(false) }
            val label = block.collapsedLabel.ifEmpty { "Почему так?" }
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { expanded = !expanded }
                        .semantics { stateDescription = if (expanded) "развёрнуто" else "свёрнуто" },
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(if (expanded) "▾" else "▸", color = MaterialTheme.colorScheme.primary)
                    Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                if (expanded) Text(contrastAnnotatedText(block.parts, MaterialTheme.colorScheme.primary), modifier = Modifier.padding(top = 6.dp))
            }
        }
        is Block.Changes -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Что изменилось", style = MaterialTheme.typography.titleMedium)
            block.items.forEach { change ->
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(contrastAnnotatedText(change.before, MaterialTheme.colorScheme.error))
                        Text("→")
                        Text(contrastAnnotatedText(change.after, MaterialTheme.colorScheme.primary), fontWeight = FontWeight.Bold)
                    }
                    Text(change.reason, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        is Block.Contrast -> Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(contrastAnnotatedText(block.before, MaterialTheme.colorScheme.error))
            Text("→")
            Text(contrastAnnotatedText(block.after, MaterialTheme.colorScheme.primary))
        }
    }
}
