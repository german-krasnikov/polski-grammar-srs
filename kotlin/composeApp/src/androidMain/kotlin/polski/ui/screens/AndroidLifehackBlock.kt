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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import polski.presentation.Lifehack
import polski.presentation.LifehackStatus
import polski.presentation.StaticPackLifehackProvider

/**
 * EN-21 android (`Plans/Kotlin/EnRuPackPlan.md` §4.3), mirroring `LifehackWeb.kt`'s
 * `renderLifehackBlock`: a lifehack is not a 10th [polski.presentation.BlockKind] — it shows the
 * same way in every style, so it composes as its own block *after* the resolved style's Back-phase
 * blocks ([AndroidBlockList]'s `backBlocks`), never inside them. Empty list -> nothing composed at
 * all (no empty frame). One collapsible entry per [Lifehack] (a skill can have more than one),
 * collapsed by default, sharing [AndroidCollapsible]'s reduced-motion contract with
 * [AndroidWhyOnDemandBlock]. The attribution line is the toggle row's own visible text, so it is
 * both what a sighted user sees and what TalkBack announces as the row's name whether collapsed or
 * expanded — the citation (and source link) only appear once expanded.
 */
@Composable
fun AndroidLifehackBlock(skillId: String, reduceMotion: Boolean) {
    val lifehacks = remember(skillId) { StaticPackLifehackProvider.forSkill(skillId) }
    if (lifehacks.isEmpty()) return
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        lifehacks.forEach { hack -> AndroidOneLifehack(hack, reduceMotion) }
    }
}

@Composable
private fun AndroidOneLifehack(hack: Lifehack, reduceMotion: Boolean) {
    var expanded by remember(hack.id) { mutableStateOf(false) }
    val statusLabel = when (hack.status) {
        LifehackStatus.Editorial -> "editorial"
        LifehackStatus.Community -> "community"
    }
    val caption = "Лайфхак · источник: $statusLabel"
    val uriHandler = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
        AndroidCollapsible(visible = expanded, reduceMotion = reduceMotion) {
            Surface(
                shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(hack.text, style = MaterialTheme.typography.bodyMedium)
                    // A citation is often long enough on its own to wrap onto several lines
                    // (`AndroidStyleBlocksComposeTest`'s fixture width), so the source link sits
                    // on its own following line — sharing one `Row` with the citation left it
                    // almost no width and wrapped it one character per line.
                    Text(
                        hack.source.citation, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    hack.source.url?.let { url ->
                        Text(
                            "Источник", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { uriHandler.openUri(url) },
                        )
                    }
                }
            }
        }
    }
}
