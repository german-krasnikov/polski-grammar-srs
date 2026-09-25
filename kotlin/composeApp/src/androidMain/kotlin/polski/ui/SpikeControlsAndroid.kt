package polski.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import polski.spike.SpikeRating

@Composable
internal actual fun SpikeHtmlInput(
    value: String,
    onValueChange: (String) -> Unit,
    onReveal: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier) {
        OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text("Ответ по-польски") })
        Button(onClick = onReveal) { Text("Показать ответ") }
    }
}

@Composable
internal actual fun SpikeHtmlTable(modifier: Modifier) {
    Column(modifier) {
        Text("Падеж")
        Text("Mianownik · Dopełniacz · Celownik · Biernik · Narzędnik · Miejscownik · Wołacz")
    }
}

@Composable
internal actual fun SpikeHtmlActions(
    revealed: Boolean,
    rating: SpikeRating?,
    onReveal: () -> Unit,
    onRate: (SpikeRating) -> Unit,
    modifier: Modifier,
) {
    Row(modifier) {
        if (!revealed) Button(onClick = onReveal) { Text("Показать ответ") }
        else SpikeRating.entries.forEach { value ->
            Button(onClick = { onRate(value) }) { Text(value.name) }
        }
    }
}
