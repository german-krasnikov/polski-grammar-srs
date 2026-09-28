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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import polski.presentation.Lifehack
import polski.presentation.LifehackGroup
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

/**
 * Front-of-card task item (1): a small, non-spoiling "есть лайфхак" marker, shown next to the
 * skill title before [AndroidLifehackBlock] ever reveals anything — [StaticPackLifehackProvider.hasLifehacks]
 * is the cheap existence check ADR-46 added exactly for this, so no [Lifehack] text/citation is
 * ever built or read here. Composes nothing for a skill with no authored tip, same "empty -> no
 * frame" contract as [AndroidLifehackBlock]. Not `clickable` — there is nothing to toggle, so a
 * tap on it can never reveal the card by construction, not by convention. [clearAndSetSemantics]
 * collapses the emoji + label into the one name TalkBack announces, instead of reading the emoji
 * glyph and the label as two separate nodes.
 */
@Composable
fun AndroidLifehackBadge(skillId: String) {
    if (!StaticPackLifehackProvider.hasLifehacks(skillId)) return
    Row(
        Modifier.clearAndSetSemantics { contentDescription = "Есть лайфхак для этого навыка" },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("💡", style = MaterialTheme.typography.labelLarge)
        Text("Есть лайфхак", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
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

/**
 * Matrix task item (2): every lifehack of the active pack ([StaticPackLifehackProvider.listAll]),
 * one collapsible group per skill (curriculum order, real skill title) or cross-skill topic,
 * collapsed by default — a level above [AndroidLifehackBlock]'s single-skill card block, reusing
 * [AndroidOneLifehack] for each group's own entries so source/status attribution and the
 * collapsed-citation contract stay exactly one implementation. Empty pack -> a calm notice, the
 * same pattern [AndroidNoCaseSystemNotice] uses for a caseless pack's Cases/Pronouns sections,
 * never an empty frame.
 */
@Composable
fun AndroidLifehacksSection(reduceMotion: Boolean) {
    val groups = remember { StaticPackLifehackProvider.listAll() }
    if (groups.isEmpty()) {
        Text(
            "Для текущего курса лайфхаков пока нет.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        groups.forEach { group -> AndroidLifehackGroupCard(group, reduceMotion) }
    }
}

@Composable
private fun AndroidLifehackGroupCard(group: LifehackGroup, reduceMotion: Boolean) {
    var expanded by remember(group.skillId, group.topic) { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { expanded = !expanded }
                    .semantics { stateDescription = if (expanded) "развёрнуто" else "свёрнуто" },
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(if (expanded) "▾" else "▸", color = MaterialTheme.colorScheme.primary)
                Text(
                    group.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text("${group.lifehacks.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AndroidCollapsible(visible = expanded, reduceMotion = reduceMotion) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    group.lifehacks.forEach { hack -> AndroidOneLifehack(hack, reduceMotion) }
                }
            }
        }
    }
}
