package polski.ui.screens

import androidx.compose.foundation.focusable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import polski.data.nounById
import polski.data.courseChainPresentation
import polski.data.skillById
import polski.data.skills
import polski.presentation.AppAction
import polski.presentation.AppUiState
import polski.presentation.CardPhase
import polski.presentation.HotKey
import polski.presentation.LoadStatus
import polski.presentation.TrainingMode
import polski.presentation.StyleId
import polski.presentation.dailySummaryText
import polski.presentation.trainingCardModel
import polski.training.sentenceSeeds

@Composable
internal fun TabButton(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) FilledTonalButton(onClick = onClick) { Text(label) }
    else OutlinedButton(onClick = onClick) { Text(label) }
}

@Composable
internal fun RecoveryScreen(state: AppUiState, export: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(when (state.loadStatus) {
                LoadStatus.Loading -> "Загружаем прогресс"
                LoadStatus.Unavailable -> "Хранилище недоступно"
                else -> "Нужна копия прогресса"
            }, style = MaterialTheme.typography.headlineSmall)
            Text("Выберите сохранённый JSON через «Импорт JSON». Повреждённый файл не будет перезаписан.")
            if (state.loadStatus == LoadStatus.RecoveryRequired || state.loadStatus == LoadStatus.MigrationAvailable) {
                OutlinedButton(onClick = export) { Text("Экспортировать исходный JSON") }
            }
        }
    }
}

/** W2.2: renders `TrainingCardModel.trainingCardModel(state)` — see `CardSectionView` for the
 *  front/back section renderer and `DesktopBlockView` (`DesktopStyleBlocks.kt`) for the
 *  per-[polski.presentation.Block] visuals a [CardSection.Styled] entry carries. */
@Composable
internal fun TrainingScreen(state: AppUiState, dispatch: (AppAction) -> Unit, focusReveal: FocusRequester, formatDate: (Long) -> String) {
    val cardFocus = remember { FocusRequester() }
    val model = trainingCardModel(state)
    LaunchedEffect(state.phase, state.exerciseId) {
        if (state.phase == CardPhase.Revealed) runCatching { cardFocus.requestFocus() }
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            model.header.modes.forEach { option ->
                val label = if (option.mode == TrainingMode.Schedule) "${option.label} · ${model.header.dueCount}" else option.label
                TabButton(label, model.header.selected == option.mode) { dispatch(option.action) }
            }
            if (state.caseReferenceAvailable) {
                OutlinedButton(onClick = { dispatch(AppAction.ToggleReference) }, enabled = model.referenceToggle.enabled) {
                    Text(model.referenceToggle.label)
                }
            }
        }
        if (state.showSkillPicker) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Выбор навыка", style = MaterialTheme.typography.titleMedium)
                    skills.chunked(4).forEach { row ->
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { skill -> OutlinedButton(onClick = { dispatch(AppAction.ChooseSkill(skill.id)) }) { Text(skill.title) } }
                        }
                    }
                }
            }
        }
        if (state.mode == TrainingMode.Chain) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sentenceSeeds.forEachIndexed { index, seed ->
                    val noun = nounById(seed.nounId)
                    TabButton("${noun.lemma} · ${index + 1}", state.seedIndex == index) {
                        dispatch(AppAction.SelectChainSeed(index))
                    }
                }
            }
            model.chainProgress?.let { progress ->
                Text("${progress.displayCount} / ${progress.total} · ${progress.summary}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                LinearProgressIndicator(
                    progress = { progress.displayCount.toFloat() / progress.total.coerceAtLeast(1) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Card(Modifier.fillMaxWidth().focusRequester(cardFocus).focusable().onKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown || event.isAltPressed || event.isCtrlPressed || event.isMetaPressed) {
                return@onKeyEvent false
            }
            val hotKey = when (event.key) {
                Key.Spacebar -> HotKey.Space
                Key.One -> HotKey.Digit1
                Key.Two -> HotKey.Digit2
                Key.DirectionLeft -> HotKey.ArrowLeft
                Key.DirectionRight -> HotKey.ArrowRight
                else -> null
            }
            val action = hotKey?.let(model.hotkeys::get) ?: return@onKeyEvent false
            dispatch(action)
            true
        }, shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
            Column(Modifier.padding(26.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                when (state.phase) {
                    CardPhase.ChainComplete -> {
                        Text(courseChainPresentation.completion.title, style = MaterialTheme.typography.headlineSmall)
                        state.chain.forEachIndexed { index, exercise -> Text("${index + 1}. ${exercise.expected}") }
                        Button(onClick = { dispatch(AppAction.StartChain((state.seedIndex + 1) % sentenceSeeds.size)) }) { Text("Следующий набор слов") }
                        OutlinedButton(onClick = { dispatch(AppAction.StartSchedule) }) { Text("К повторениям") }
                    }
                    CardPhase.NoDue -> {
                        Text("Повторения на сейчас завершены", style = MaterialTheme.typography.headlineSmall)
                        Text(state.nextDue?.let { "Следующее: ${formatDate(it.toEpochMilliseconds())}" } ?: "Новых повторений пока нет.")
                        Button(onClick = { dispatch(AppAction.StartChain()) }) { Text("Потренировать цепочку") }
                    }
                    // M3 blueprint: web-first (composeApp/webMain) — this branch only keeps the
                    // desktop preview compiling and functional; no dedicated desktop design pass yet.
                    CardPhase.DailyComplete -> {
                        Text("Сессия на сегодня готова", style = MaterialTheme.typography.headlineSmall)
                        Text(dailySummaryText(state.daily?.history ?: emptyList()))
                        Button(onClick = { dispatch(AppAction.ContinueDaily) }) { Text("Ещё одно задание") }
                        OutlinedButton(onClick = { dispatch(AppAction.StartSchedule) }) { Text("К повторениям") }
                    }
                    CardPhase.Question, CardPhase.Revealed -> {
                        val exercise = state.exercise
                        if (exercise != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TabButton("Схемы и логика", state.styleId != StyleId.SituationFirst) {
                                    dispatch(AppAction.SetStyle(StyleId.RuleFirst))
                                }
                                TabButton("Живые ситуации", state.styleId == StyleId.SituationFirst) {
                                    dispatch(AppAction.SetStyle(StyleId.SituationFirst))
                                }
                            }
                            Text("${skillById(exercise.primarySkill).level} · ${skillById(exercise.primarySkill).title}", color = MaterialTheme.colorScheme.primary)
                            // EnRuPackPlan.md §4.2: a cheap, non-spoiling teaser — the lifehack's own text/citation
                            // stays gated behind reveal, on CardSection.LifehackSection.
                            model.lifehackBadge?.let { badge ->
                                Text(
                                    badge.text, style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.semantics { contentDescription = badge.contentDescription },
                                )
                            }
                            if (model.introducing) Text("Знакомство с навыком", style = MaterialTheme.typography.titleMedium)
                            model.front.forEach { section -> CardSectionView(section, state, dispatch, focusReveal) }
                            if (state.phase == CardPhase.Revealed) {
                                model.back.forEach { section -> CardSectionView(section, state, dispatch, focusReveal) }
                                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    model.ratingButtons.forEachIndexed { index, rb ->
                                        OutlinedButton(onClick = { dispatch(AppAction.Rate(exercise.id, rb.rating)) }) {
                                            Column {
                                                Text("${index + 1} ${rb.label}")
                                                Text(rb.intervalLabel, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (model.referenceToggle.expanded) CaseReferenceScreen(state, dispatch)
    }
}
