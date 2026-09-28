package polski.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.IntOffset
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
    swipeRatingEnabled: Boolean = true,
    reduceMotion: Boolean = false,
) {
    when {
        state.loadStatus != LoadStatus.Ready -> AndroidRecoveryScreen(state, dispatch)
        state.tab == AppTab.Training -> AndroidTrainingScreen(state, dispatch, focusReveal, formatDate, swipeRatingEnabled, reduceMotion)
        state.tab == AppTab.Matrix -> AndroidMatrixScreen(state, dispatch, reduceMotion)
        else -> AndroidProgressScreen(state, formatDate, dispatch)
    }
}

/** The bottom nav's left-to-right order (`AppNavigationBar`) — used only to pick a slide direction. */
private val tabNavOrder = listOf(AppTab.Training, AppTab.Matrix, AppTab.Progress, AppTab.Vocabulary)

private val tabSlideEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

/** +1 (new content enters from the right) when [to] sits at or right of [from] in the nav bar, else -1. */
fun tabSlideDirection(from: AppTab, to: AppTab): Int =
    if (tabNavOrder.indexOf(to) >= tabNavOrder.indexOf(from)) 1 else -1

/**
 * D4 (`Plans/Kotlin/Lane-android.md`): phone-like paging between the bottom-nav tabs — the old and
 * new screens slide together horizontally, ~300ms, in the direction implied by their nav-bar order
 * (mirrors the web reference's `RouteSlider`, without the DOM-specific plumbing that needs).
 * `AnimatedContent`'s own `initialState`/`targetState` — not the caller's ambient tab state, which
 * has already moved to the new value by the time this even starts animating — decide which screen
 * each of the two simultaneously-composed branches renders: [content] receives that exact
 * per-branch [AppTab], so the outgoing screen keeps showing what it showed a moment ago instead of
 * flipping to the new one mid-slide (a caller that instead re-reads its own ambient tab state
 * inside [content] would render the SAME screen for both branches). `.using(null)` drops the
 * default size-morph so the container just sizes to whichever branch is taller for the transition,
 * rather than visibly stretching/squashing between two very differently sized screens. `reduceMotion`
 * (system setting or the app's own Settings toggle) switches instantly.
 */
@Composable
fun AndroidTabContent(tab: AppTab, reduceMotion: Boolean, content: @Composable (AppTab) -> Unit) {
    AnimatedContent(
        targetState = tab,
        transitionSpec = {
            if (reduceMotion) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                val direction = tabSlideDirection(initialState, targetState)
                val move = tween<IntOffset>(300, easing = tabSlideEasing)
                val fade = tween<Float>(300, easing = tabSlideEasing)
                (slideInHorizontally(move) { it * direction } + fadeIn(fade))
                    .togetherWith(slideOutHorizontally(move) { -it * direction } + fadeOut(fade))
                    .using(null)
            }
        },
        label = "tab-content",
    ) { branchTab -> content(branchTab) }
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
