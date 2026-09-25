package polski.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import polski.data.skills
import polski.presentation.AppAction
import polski.presentation.AppUiState

@Composable
internal fun ProgressScreen(state: AppUiState, formatDate: (Long) -> String, dispatch: (AppAction) -> Unit) {
    val progress = state.progress ?: return
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Прогресс", style = MaterialTheme.typography.headlineSmall)
                Text("${progress.totalReviews} всего карточек    ${state.todayCount} сегодня    ${state.dueCount} к повторению")
                Text("При ответе вслух точность считается по самооценке; при печати — по проверке ответа.")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { dispatch(AppAction.RequestExport) }) { Text("Экспорт JSON") }
                    OutlinedButton(onClick = { dispatch(AppAction.RequestReset) }) { Text("Сбросить прогресс") }
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Навыки", style = MaterialTheme.typography.titleLarge)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.width(1000.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Навык", modifier = Modifier.weight(1f))
                            Text("Повторений", modifier = Modifier.weight(0.5f))
                            Text("Точность", modifier = Modifier.weight(0.5f))
                            Text("Следующее повторение", modifier = Modifier.weight(1f))
                        }
                        skills.forEach { skill ->
                            val counts = progress.stats[skill.id]
                            val card = progress.cards.firstOrNull { it.skillId == skill.id }
                            val accuracy = if (counts == null || counts.reviews == 0) "—" else
                                "${floor(counts.correct * 100.0 / counts.reviews + 0.5).toInt()}%"
                            val next = when {
                                card == null -> "—"
                                state.now?.let { card.card.due <= it } == true -> "Сейчас"
                                else -> formatDate(card.card.due.toEpochMilliseconds())
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { dispatch(AppAction.ChooseSkill(skill.id)) }, modifier = Modifier.weight(1f)) { Text(skill.title) }
                                Text((counts?.reviews ?: 0).toString(), modifier = Modifier.weight(0.5f))
                                Text(accuracy, modifier = Modifier.weight(0.5f))
                                Text(next, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
