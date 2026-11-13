package polski.ui.screens

import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import polski.data.courseChainPresentation
import polski.data.nounLemma
import polski.data.skillById
import polski.presentation.AppAction
import polski.presentation.AppUiState
import polski.presentation.CardEffect
import polski.presentation.CardPhase
import polski.presentation.CardSection
import polski.presentation.StyleId
import polski.presentation.TrainingMode
import polski.presentation.UiCopy
import polski.presentation.builtInStyleIds
import polski.presentation.StyleRegistry
import polski.presentation.dailySummaryText
import polski.presentation.trainingCardModel
import polski.srs.Rating
import polski.training.sentenceSeeds

@Composable
internal fun AndroidTrainingScreen(
    state: AppUiState,
    dispatch: (AppAction) -> Unit,
    focusReveal: FocusRequester,
    formatDate: (Long) -> String,
    swipeRatingEnabled: Boolean = true,
    reduceMotion: Boolean = false,
) {
    val model = trainingCardModel(state)
    val swipeNudgeGate = remember { SwipeNudgeGate() }
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("ЕЖЕДНЕВНАЯ ПРАКТИКА", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Тренировка", style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text("${model.header.dueCount} к повторению", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("${model.header.todayCount} сегодня", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        AndroidChoiceMenu(
            "Режим",
            state.mode.name,
            model.header.modes.map { it.mode.name to it.label },
        ) { selected ->
            model.header.modes.firstOrNull { it.mode.name == selected }?.action?.let(dispatch)
        }
        AndroidCollapsible(visible = state.showSkillPicker, reduceMotion = reduceMotion) {
            AndroidChoiceMenu("Выбрать навык", "", polski.data.skills.map { it.id to it.title }) {
                dispatch(AppAction.ChooseSkill(it))
            }
        }
        if (state.mode == TrainingMode.Chain) {
            AndroidChoiceMenu(
                "Набор слов",
                state.seedIndex.toString(),
                sentenceSeeds.mapIndexed { index, seed -> index.toString() to nounLemma(seed.nounId) },
            ) { dispatch(AppAction.SelectChainSeed(it.toInt())) }
            model.chainProgress?.let { progress ->
                Text("${progress.displayCount} / ${progress.total} · ${progress.summary}",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LinearProgressIndicator(
                    progress = { progress.displayCount.toFloat() / progress.total.coerceAtLeast(1) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        when (state.phase) {
            CardPhase.ChainComplete -> Box(Modifier.fillMaxWidth()) {
                AndroidInfoCard(courseChainPresentation.completion.title) {
                    state.chain.forEachIndexed { index, exercise -> Text("${index + 1}. ${exercise.expected}") }
                    Button(onClick = { dispatch(AppAction.StartChain((state.seedIndex + 1) % sentenceSeeds.size)) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Следующий набор слов")
                    }
                    OutlinedButton(onClick = { dispatch(AppAction.StartSchedule) }, modifier = Modifier.fillMaxWidth()) {
                        Text("К повторениям")
                    }
                }
                if (!reduceMotion && !RiveMeasurementVariant.riveDisabled) AndroidChainCompleteOverlay()
            }
            CardPhase.NoDue -> AndroidInfoCard("Повторения на сейчас завершены") {
                Text(state.nextDue?.let { "Следующее: ${formatDate(it.toEpochMilliseconds())}" } ?: UiCopy.NoDueTitle)
                Button(onClick = { dispatch(AppAction.StartChain()) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Потренировать цепочку")
                }
            }
            // M3 blueprint: web-first (composeApp/webMain) — this branch only keeps the Android
            // preview compiling and functional; no dedicated Android design pass yet.
            CardPhase.DailyComplete -> AndroidInfoCard("Сессия на сегодня готова") {
                Text(dailySummaryText(state.daily?.history ?: emptyList()))
                Button(onClick = { dispatch(AppAction.ContinueDaily) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Ещё одно задание")
                }
                OutlinedButton(onClick = { dispatch(AppAction.StartSchedule) }, modifier = Modifier.fillMaxWidth()) {
                    Text("К повторениям")
                }
            }
            CardPhase.Question, CardPhase.Revealed -> {
                val exercise = state.exercise
                if (exercise != null) AndroidInfoCard("${skillById(exercise.primarySkill).level} · ${skillById(exercise.primarySkill).title}") {
                    AndroidChoiceMenu("Подача", state.styleId.value,
                        builtInStyleIds.map { it.value to styleLabel(StyleRegistry.recipes.getValue(it)) }) {
                        dispatch(AppAction.SetStyle(StyleId(it)))
                    }
                    model.lifehackBadge?.let { badge ->
                        Text(badge.text, style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (model.introducing) {
                        Text("Знакомство с навыком", style = MaterialTheme.typography.titleMedium)
                        for (section in model.front) AndroidCardSection(section, state, dispatch, focusReveal, reduceMotion)
                    } else {
                        var cardEffect by remember(exercise.id) { mutableStateOf<CardEffect?>(null) }
                        val ratingGate = remember(exercise.id) { SingleRatingGate() }
                        fun rate(rating: Rating) {
                            val effect = ratingGate.rate(rating, reduceMotion, RiveMeasurementVariant.riveDisabled, dispatch = {
                                dispatch(AppAction.Rate(exercise.id, it))
                            })
                            if (effect != null) cardEffect = effect
                        }
                        val frontBlocks = model.front.filterIsInstance<CardSection.Styled>().map { it.block }
                        Box(Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                Column(
                                    Modifier.fillMaxWidth().then(
                                        if (state.phase == CardPhase.Question) Modifier.clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                        ) { dispatch(AppAction.Reveal(exercise.id)) } else Modifier
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(18.dp),
                                ) {
                                    for (section in model.front) when (section) {
                                        is CardSection.Styled -> {} // rendered below via AndroidBlockList
                                        else -> AndroidCardSection(section, state, dispatch, focusReveal, reduceMotion)
                                    }
                                    AndroidBlockList(frontBlocks, reduceMotion)
                                }
                                if (state.phase == CardPhase.Revealed) {
                                    val backBlocks = model.back.filterIsInstance<CardSection.Styled>().map { it.block }
                                    AndroidAnswerReveal(exercise.id, reduceMotion, onRate = ::rate) {
                                        AndroidStaggeredReveal(0, reduceMotion) {
                                            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                                for (section in model.back) when (section) {
                                                    is CardSection.Expected, is CardSection.EvaluationFeedback ->
                                                        AndroidCardSection(section, state, dispatch, focusReveal, reduceMotion)
                                                    else -> {}
                                                }
                                            }
                                        }
                                        AndroidStaggeredReveal(1, reduceMotion) {
                                            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                                for (section in model.back) when (section) {
                                                    is CardSection.MethodFeedback -> {
                                                        Text(section.feedback)
                                                        Text(section.explanation)
                                                    }
                                                    is CardSection.Styled -> {} // rendered below
                                                    is CardSection.LifehackSection -> AndroidLifehackBlock(exercise.primarySkill, reduceMotion)
                                                    is CardSection.ReviewHint -> Text(section.text)
                                                    else -> {}
                                                }
                                                AndroidBlockList(backBlocks, reduceMotion)
                                            }
                                        }
                                        if (swipeRatingEnabled) AndroidStaggeredReveal(2, reduceMotion) {
                                            AndroidSwipeHint(swipeNudgeGate, reduceMotion)
                                        }
                                    }
                                }
                            }
                            if (!reduceMotion) AndroidRiveOverlay(cardEffect) { cardEffect = null }
                        }
                    }
                }
            }
        }

        OutlinedButton(onClick = { dispatch(AppAction.ToggleReference) }, enabled = model.referenceToggle.enabled,
            modifier = Modifier.fillMaxWidth()) {
            Text(model.referenceToggle.label)
        }
        AndroidCollapsible(visible = model.referenceToggle.expanded, reduceMotion = reduceMotion) {
            AndroidCaseReference(state, dispatch)
        }
    }
}

