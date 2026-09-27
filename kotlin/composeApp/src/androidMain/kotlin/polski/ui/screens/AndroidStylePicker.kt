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
import polski.data.styleContentBySkillId
import polski.presentation.StyleComposer
import polski.presentation.StyleId
import polski.presentation.StyleRegistry

/**
 * UC-10/S1 settings picker for [StyleId] — the 4 recipes from [StyleRegistry], never a literal
 * list owned by this composable. [currentSkillId] (the exercise on screen, if any) drives the
 * fallback hint: a style whose `requires` isn't met for that one skill stays selectable — it is a
 * preference across all 16 skills, not just this one — but shows which style it currently falls
 * back to, so picking "Через сравнение с родным" is never a silent no-op.
 */
@Composable
fun AndroidStylePicker(
    selected: StyleId,
    currentSkillId: String?,
    enabled: Boolean,
    onSelect: (StyleId) -> Unit,
) {
    val registry = StyleRegistry.recipes
    val content = currentSkillId?.let(::styleContentBySkillId)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        registry.keys.forEach { styleId ->
            val recipe = registry.getValue(styleId)
            val fallbackId = content?.let { StyleComposer.resolveEffectiveStyle(recipe, it, registry) }?.takeIf { it != styleId }
            Row(
                Modifier.fillMaxWidth()
                    .clickable(enabled = enabled) { onSelect(styleId) }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RadioButton(selected = selected == styleId, onClick = { onSelect(styleId) }, enabled = enabled)
                Column {
                    Text(styleLabel(recipe))
                    Text(styleDescription(recipe), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (fallbackId != null) Text(
                        "Пока не хватает материалов для этого навыка — показывается «${styleLabel(registry.getValue(fallbackId))}».",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
