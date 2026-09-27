package polski.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun AndroidChoiceMenu(
    label: String,
    selected: String,
    options: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: ${options.firstOrNull { it.first == selected }?.second ?: selected}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, title) ->
                DropdownMenuItem(text = { Text(title) }, onClick = { expanded = false; onSelect(id) })
            }
        }
    }
}

@Composable
internal fun AndroidInfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

/**
 * D4 (`Plans/Kotlin/Lane-android.md`): a panel that shows/hides itself with a height+opacity
 * animation instead of popping in/out — the training screen's reference table and skill picker
 * (mirrors the web reference's `.collapsible` CSS grid-rows animation). [AnimatedVisibility]
 * already removes [content] from the composition (and so from the accessibility tree) while
 * hidden, so no extra semantics are needed here. `reduceMotion` (system setting or the app's own
 * Settings toggle) shows/hides instantly, matching every other reduced-motion path in this app.
 */
@Composable
fun AndroidCollapsible(visible: Boolean, reduceMotion: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = if (reduceMotion) EnterTransition.None
            else expandVertically(tween(220), expandFrom = Alignment.Top) + fadeIn(tween(220)),
        exit = if (reduceMotion) ExitTransition.None
            else shrinkVertically(tween(220), shrinkTowards = Alignment.Top) + fadeOut(tween(220)),
    ) { content() }
}
