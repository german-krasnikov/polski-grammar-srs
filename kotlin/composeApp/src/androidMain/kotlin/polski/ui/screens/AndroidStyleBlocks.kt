package polski.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import polski.presentation.Block
import polski.ui.contrastAnnotatedText

/**
 * UC-10/S2 (`Plans/Kotlin/StylesBlueprint.md`§6): renders a [Block] list exactly in the order
 * [polski.presentation.StyleComposer] returns it for one phase — one visual per
 * [polski.presentation.BlockKind]. This file never branches on [polski.presentation.StyleId]
 * itself; switching style only changes which blocks the composer includes, so the same `when`
 * below renders every recipe. Colors come from `MaterialTheme.colorScheme`, so light/dark follow
 * the host theme with no branch here; layout is plain `fillMaxWidth`/`weight` Rows and Columns, so
 * it reflows from a 320px phone up to tablet/desktop widths without a breakpoint.
 */
@Composable
fun AndroidBlockList(blocks: List<Block>, reduceMotion: Boolean, modifier: Modifier = Modifier) {
    if (blocks.isEmpty()) return
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        blocks.forEach { block -> AndroidBlock(block, reduceMotion) }
    }
}

@Composable
private fun AndroidBlock(block: Block, reduceMotion: Boolean) {
    when (block) {
        is Block.Formula -> AndroidFormulaBlock(block)
        is Block.Rule -> AndroidRuleBlock(block)
        is Block.Table -> AndroidTableBlock(block)
        is Block.Scene -> AndroidSceneBlock(block)
        is Block.NativeParallel -> AndroidNativeParallelBlock(block)
        is Block.Examples -> AndroidExamplesBlock(block)
        is Block.WhyOnDemand -> AndroidWhyOnDemandBlock(block, reduceMotion)
        is Block.Changes -> AndroidChangesBlock(block)
        is Block.Contrast -> AndroidContrastBlock(block)
    }
}

/** As today's "ЗАПОМНИ" memo box: the skill's formula, bold, on a tertiary surface. */
@Composable
private fun AndroidFormulaBlock(block: Block.Formula) {
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "ЗАПОМНИ", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                block.text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

/** As today: a plain rule/theory paragraph, now its own labelled section instead of buried inside the formula box. */
@Composable
private fun AndroidRuleBlock(block: Block.Rule) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "Правило", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.semantics { heading() },
        )
        Text(block.text, style = MaterialTheme.typography.bodyMedium)
        Text(block.detail, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Compact endings table: one row per [Block.Table.rows], before→after using the same highlighted-span text as the rest of the card. */
@Composable
private fun AndroidTableBlock(block: Block.Table) {
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (block.caption.isNotEmpty()) Text(
                block.caption, style = MaterialTheme.typography.labelMedium, modifier = Modifier.semantics { heading() },
            )
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
}

/** Quote-like scene card for a short situational lead-in. */
@Composable
private fun AndroidSceneBlock(block: Block.Scene) {
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("“", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(
                block.text, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

/** Two-column native ↔ target row per pair, with a match/differs badge (`pair.matches`). */
@Composable
private fun AndroidNativeParallelBlock(block: Block.NativeParallel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Родной ↔ изучаемый", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.semantics { heading() },
        )
        block.pairs.forEach { pair ->
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(pair.native, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(pair.target, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    }
                    Text(
                        if (pair.matches) "Совпадает" else "Отличается",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (pair.matches) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    )
                    if (pair.note.isNotEmpty()) Text(
                        pair.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Extra examples, no breakdown — the card itself is already the one worked example. */
@Composable
private fun AndroidExamplesBlock(block: Block.Examples) {
    if (block.items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "Примеры", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.semantics { heading() },
        )
        block.items.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
    }
}

/**
 * Collapsed-by-default disclosure. [reduceMotion] only drops [AndroidCollapsible]'s expand
 * animation (same contract as the reference-table toggle) — the toggle itself, and the
 * `stateDescription` a screen reader announces, work identically either way.
 */
@Composable
private fun AndroidWhyOnDemandBlock(block: Block.WhyOnDemand, reduceMotion: Boolean) {
    var expanded by remember { mutableStateOf(false) }
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
        AndroidCollapsible(visible = expanded, reduceMotion = reduceMotion) {
            Text(block.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

/** As today: a single before→after focus row, sharing the highlight styling used everywhere else on the card. */
@Composable
private fun AndroidContrastBlock(block: Block.Contrast) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(contrastAnnotatedText(block.before, MaterialTheme.colorScheme.error))
        Text("→")
        Text(contrastAnnotatedText(block.after, MaterialTheme.colorScheme.primary), fontWeight = FontWeight.Bold)
    }
}

/** As today's "Что изменилось" list. */
@Composable
private fun AndroidChangesBlock(block: Block.Changes) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Что изменилось", style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
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
}
