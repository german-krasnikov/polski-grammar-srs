package polski.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import polski.data.skills
import polski.presentation.AppAction
import polski.presentation.AppUiState

@Composable
internal fun AndroidProgressScreen(
    state: AppUiState,
    formatDate: (Long) -> String,
    dispatch: (AppAction) -> Unit,
) {
    val progress = state.progress ?: return
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Прогресс", style = MaterialTheme.typography.headlineSmall)
        AndroidInfoCard("Обзор") {
            Text("${progress.totalReviews} всего карточек · ${state.todayCount} сегодня · ${state.dueCount} к повторению")
            Text("При ответе вслух точность считается по самооценке; при печати — по проверке ответа.")
            Button(onClick = { dispatch(AppAction.RequestExport) }, modifier = Modifier.fillMaxWidth()) {
                Text("Экспорт JSON")
            }
            OutlinedButton(onClick = { dispatch(AppAction.RequestReset) }, modifier = Modifier.fillMaxWidth()) {
                Text("Сбросить прогресс")
            }
        }
        Text("Навыки", style = MaterialTheme.typography.titleLarge)
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
            AndroidInfoCard(skill.title) {
                Text("Повторений: ${counts?.reviews ?: 0} · Точность: $accuracy")
                Text("Следующее повторение: $next")
                OutlinedButton(onClick = { dispatch(AppAction.ChooseSkill(skill.id)) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Тренировать навык")
                }
            }
        }
    }
}
