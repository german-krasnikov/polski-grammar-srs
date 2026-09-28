package polski.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import polski.presentation.Lifehack
import polski.presentation.LifehackStatus

/**
 * EN-21 (`Plans/Kotlin/EnRuPackPlan.md` §4.2/§4.3): the desktop JVM Compose preview's own render of
 * `StaticPackLifehackProvider.forSkill(skillId)` — same as every other host, this is deliberately
 * outside `TrainingScreen`'s per-style answer layout (this preview predates UC-10's
 * `StyleComposer`/`BlockKind`, see `TrainingScreen.kt`'s hardcoded RuleFirst/SituationFirst
 * branches): a lifehack shows the same way regardless of style, and is absent entirely (no empty
 * frame) when the skill has no authored one. Mirrors the collapsed-by-default disclosure
 * `AndroidWhyOnDemandBlock` (`AndroidStyleBlocks.kt`) and the web's `LifehackWeb.kt` already use,
 * just in this preview's plain Material3 vocabulary.
 */
@Composable
internal fun DesktopLifehackBlock(lifehacks: List<Lifehack>) {
    if (lifehacks.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        lifehacks.forEach { hack -> DesktopLifehackEntry(hack) }
    }
}

@Composable
private fun DesktopLifehackEntry(hack: Lifehack) {
    var expanded by remember(hack.id) { mutableStateOf(false) }
    val caption = "Лайфхак · источник: " + when (hack.status) {
        LifehackStatus.Editorial -> "editorial"
        LifehackStatus.Community -> "community"
    }
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { expanded = !expanded }
                .semantics { stateDescription = if (expanded) "развёрнуто" else "свёрнуто" },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(if (expanded) "▾" else "▸", color = MaterialTheme.colorScheme.primary)
            Text(caption, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        if (expanded) {
            Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(hack.text, style = MaterialTheme.typography.bodyMedium)
                Text(
                    hack.source.url?.let { url -> "${hack.source.citation} — $url" } ?: hack.source.citation,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
