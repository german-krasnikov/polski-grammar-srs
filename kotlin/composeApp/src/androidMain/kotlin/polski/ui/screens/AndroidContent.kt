package polski.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import polski.presentation.AppAction
import polski.presentation.AppTab
import polski.presentation.AppUiState
import polski.presentation.LoadStatus

/** Android renders its own phone interface over the shared session state. */
@Composable
fun AndroidContent(
    state: AppUiState,
    dispatch: (AppAction) -> Unit,
    focusReveal: FocusRequester,
    formatDate: (Long) -> String,
) {
    when {
        state.loadStatus != LoadStatus.Ready -> AndroidRecoveryScreen(state, dispatch)
        state.tab == AppTab.Training -> AndroidTrainingScreen(state, dispatch, focusReveal, formatDate)
        state.tab == AppTab.Matrix -> AndroidMatrixScreen(state, dispatch)
        else -> AndroidProgressScreen(state, formatDate, dispatch)
    }
}

@Composable
private fun AndroidRecoveryScreen(state: AppUiState, dispatch: (AppAction) -> Unit) {
    AndroidInfoCard(when (state.loadStatus) {
        LoadStatus.Loading -> "Загружаем прогресс"
        LoadStatus.Unavailable -> "Хранилище недоступно"
        else -> "Нужна копия прогресса"
    }) {
        Text("Выберите сохранённый JSON через «Импорт». Повреждённый файл не будет перезаписан.")
        if (state.loadStatus == LoadStatus.RecoveryRequired || state.loadStatus == LoadStatus.MigrationAvailable) {
            OutlinedButton(onClick = { dispatch(AppAction.RequestExport) }, modifier = Modifier.fillMaxWidth()) {
                Text("Экспортировать исходный JSON")
            }
        }
    }
}
