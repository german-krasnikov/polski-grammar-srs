package polski.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import polski.data.usableCourseSelections

private val languageLabels = mapOf("pl" to "Польский", "en" to "Английский", "ru" to "Русский")
private fun languageLabel(code: String): String = languageLabels[code] ?: code

/**
 * EN-22: target/native pickers next to the existing style picker (AndroidStylePicker.kt) — driven
 * by [usableCourseSelections], the packs this build both embeds *and* can safely make active today
 * (see that val's own KDoc). Never a hand-written language list: a pack starts showing here the
 * moment it passes that probe, no picker change needed — pl-ru and en-ru both do today
 * (EnRuAcceptance-2026-09-28.md §7 item 1). [selected] is the persisted choice even if it's since
 * fallen out of [usableCourseSelections] (a build downgrade) — it just then renders selected but
 * unlisted. A pack listed here can still fail to build a real training chain the moment it's
 * chosen (item 2's own separate content gap) — `AndroidSessionViewModel` degrades that gracefully
 * (rolls back, self-corrects Settings), it never reaches this composable as a crash.
 */
@Composable
fun AndroidCoursePicker(selectedTarget: String, selectedNative: String, enabled: Boolean, onSelect: (target: String, native: String) -> Unit) {
    val pairs = usableCourseSelections
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("Целевой язык", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        pairs.forEach { (target, native) ->
            Row(
                Modifier.fillMaxWidth()
                    .clickable(enabled = enabled) { onSelect(target, native) }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RadioButton(selected = selectedTarget == target && selectedNative == native,
                    onClick = { onSelect(target, native) }, enabled = enabled)
                Column {
                    Text(languageLabel(target))
                    Text("Родной: ${languageLabel(native)}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
