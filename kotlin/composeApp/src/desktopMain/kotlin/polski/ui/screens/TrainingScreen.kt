package polski.ui.screens

import androidx.compose.foundation.focusable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import polski.data.nounById
import polski.data.courseChainPresentation
import polski.presentation.chainDisplayCount
import polski.data.skillById
import polski.data.presentationBySkillId
import polski.data.skills
import polski.presentation.AnswerMode
import polski.presentation.AppAction
import polski.presentation.AppUiState
import polski.presentation.CardPhase
import polski.presentation.LoadStatus
import polski.presentation.TrainingMode
import polski.presentation.ChangeSide
import polski.presentation.StyleId
import polski.presentation.changeHighlightParts
import polski.presentation.sentenceHighlightParts
import polski.srs.Rating
import polski.training.sentenceSeeds
import polski.ui.contrastAnnotatedText


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

@Composable
internal fun TrainingScreen(state: AppUiState, dispatch: (AppAction) -> Unit, focusReveal: FocusRequester, formatDate: (Long) -> String) {
    val cardFocus = remember { FocusRequester() }
    val introducing = state.phase == CardPhase.Question && state.introPending
    LaunchedEffect(state.phase, state.exerciseId) {
        if (state.phase == CardPhase.Revealed) runCatching { cardFocus.requestFocus() }
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TabButton("Цепочка предложений", state.mode == TrainingMode.Chain) { dispatch(AppAction.StartChain()) }
            TabButton("По расписанию · ${state.dueCount}", state.mode == TrainingMode.Schedule) { dispatch(AppAction.StartSchedule) }
            TabButton("Отдельный навык", state.mode == TrainingMode.Focused) { dispatch(AppAction.OpenSkillPicker) }
            OutlinedButton(onClick = { dispatch(AppAction.ToggleReference) }, enabled = !introducing) {
                Text(if (!introducing && state.showReference) "Скрыть таблицу" else "Таблица под рукой")
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
            val displayCount = state.chainDisplayCount
            Text("$displayCount / ${state.chain.size} · ${courseChainPresentation.summary}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            LinearProgressIndicator(
                progress = { displayCount.toFloat() / state.chain.size.coerceAtLeast(1) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Card(Modifier.fillMaxWidth().focusRequester(cardFocus).focusable().onKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown || event.isAltPressed || event.isCtrlPressed || event.isMetaPressed) {
                return@onKeyEvent false
            }
            val exerciseId = state.exerciseId ?: return@onKeyEvent false
            if (state.phase == CardPhase.Question && state.introPending && event.key == Key.Spacebar) {
                dispatch(AppAction.ContinueIntroduction)
                true
            } else if (state.phase == CardPhase.Question && !state.introPending && state.answerMode == AnswerMode.Oral && event.key == Key.Spacebar) {
                dispatch(AppAction.Reveal(exerciseId))
                true
            } else if (state.phase == CardPhase.Revealed) {
                val rating = when (event.key) {
                    Key.One -> Rating.Again
                    Key.Two -> Rating.Good
                    else -> null
                }
                if (rating == null) false else {
                    dispatch(AppAction.Rate(exerciseId, rating))
                    true
                }
            } else false
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
                    CardPhase.Question, CardPhase.Revealed -> {
                        val exercise = state.exercise
                        if (exercise != null) {
                            val presentation = presentationBySkillId(exercise.primarySkill)
                            val method = if (state.styleId == StyleId.SituationFirst) presentation.situations else presentation.logic
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TabButton("Схемы и логика", state.styleId != StyleId.SituationFirst) {
                                    dispatch(AppAction.SetStyle(StyleId.RuleFirst))
                                }
                                TabButton("Живые ситуации", state.styleId == StyleId.SituationFirst) {
                                    dispatch(AppAction.SetStyle(StyleId.SituationFirst))
                                }
                            }
                            Text("${skillById(exercise.primarySkill).level} · ${skillById(exercise.primarySkill).title}", color = MaterialTheme.colorScheme.primary)
                            if (state.phase == CardPhase.Question && state.introPending) {
                                Text("Знакомство с навыком", style = MaterialTheme.typography.titleMedium)
                                Text("ИСХОДНОЕ ПРЕДЛОЖЕНИЕ", style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(contrastAnnotatedText(sentenceHighlightParts(exercise.source, exercise.changes, ChangeSide.Before),
                                    MaterialTheme.colorScheme.error), style = MaterialTheme.typography.headlineMedium)
                                Text(method.introduce)
                                Button(onClick = { dispatch(AppAction.ContinueIntroduction) }) { Text("Перейти к заданию") }
                            } else {
                            Text("ИСХОДНОЕ ПРЕДЛОЖЕНИЕ", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(contrastAnnotatedText(sentenceHighlightParts(exercise.source, exercise.changes, ChangeSide.Before),
                                MaterialTheme.colorScheme.error), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                            Text("${exercise.prompt}\n${method.promptLead}",
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(16.dp)).padding(18.dp),
                                style = MaterialTheme.typography.titleLarge)
                            if (state.phase == CardPhase.Question) Text(method.retrieve)
                            if (state.phase == CardPhase.Question) {
                                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TabButton("Ответ вслух / про себя", state.answerMode == AnswerMode.Oral) { dispatch(AppAction.SetAnswerMode(AnswerMode.Oral)) }
                                    TabButton("Напечатать ответ", state.answerMode == AnswerMode.Typed) { dispatch(AppAction.SetAnswerMode(AnswerMode.Typed)) }
                                }
                                if (state.answerMode == AnswerMode.Typed) {
                                    OutlinedTextField(
                                        value = state.draft,
                                        onValueChange = { dispatch(AppAction.EditAnswer(it)) },
                                        label = { Text("Ответ по-польски") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2,
                                    )
                                } else Text("Произнеси целое предложение, затем покажи ответ.")
                                Button(
                                    onClick = { dispatch(AppAction.Reveal(exercise.id)) },
                                    modifier = Modifier.focusRequester(focusReveal),
                                ) { Text(if (state.answerMode == AnswerMode.Typed) "Проверить и показать ответ" else "Показать ответ") }
                            } else {
                                Text("Эталон", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(contrastAnnotatedText(sentenceHighlightParts(exercise.expected, exercise.changes, ChangeSide.After), MaterialTheme.colorScheme.primary),
                                    style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                                if (exercise.accepted.isNotEmpty()) Text("Также: ${exercise.accepted.joinToString(" / ")}")
                                if (state.answerMode == AnswerMode.Typed) {
                                    Text(if (state.evaluation?.correct == true) "Совпадает с правильным вариантом" else "Сравни свой ответ с эталоном")
                                    Text(state.frozenAnswer?.takeIf(String::isNotEmpty) ?: "Ответ не введён")
                                }
                                if (state.styleId == StyleId.SituationFirst) {
                                    Text(method.feedback)
                                    Text(exercise.explanation)
                                }
                                Text("Что изменилось", style = MaterialTheme.typography.titleMedium)
                                exercise.changes.forEach { change ->
                                    Column {
                                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                            Text(contrastAnnotatedText(changeHighlightParts(change.from, change.to, ChangeSide.Before),
                                                MaterialTheme.colorScheme.error))
                                            Text("→")
                                            Text(contrastAnnotatedText(changeHighlightParts(change.from, change.to, ChangeSide.After), MaterialTheme.colorScheme.primary),
                                                fontWeight = FontWeight.Bold)
                                        }
                                        Text(change.reason, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.tertiaryContainer,
                                    modifier = Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                        Text("ЗАПОМНИ", style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer)
                                        Text(skillById(exercise.primarySkill).formula, fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer)
                                        if (state.styleId != StyleId.SituationFirst) {
                                            Text(method.feedback, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                            Text(exercise.explanation, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                            Text(contrastAnnotatedText(changeHighlightParts(presentation.focusBefore, presentation.focusAfter, ChangeSide.Before),
                                                MaterialTheme.colorScheme.error))
                                            Text("→")
                                            Text(contrastAnnotatedText(changeHighlightParts(presentation.focusBefore, presentation.focusAfter, ChangeSide.After), MaterialTheme.colorScheme.primary))
                                        }
                                    }
                                }
                                Text("Когда повторить?", style = MaterialTheme.typography.titleMedium)
                                Text(method.review)
                                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(Rating.Again to "1 Повторить", Rating.Good to "2 Вспомнил")
                                        .forEach { (rating, label) ->
                                            OutlinedButton(onClick = { dispatch(AppAction.Rate(exercise.id, rating)) }) {
                                                Column {
                                                    Text(label)
                                                    Text(state.intervals?.get(rating)?.let { intervalLabel(it.toEpochMilliseconds(), state.now?.toEpochMilliseconds() ?: it.toEpochMilliseconds()) }.orEmpty(), fontSize = 11.sp)
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
        }
        if (state.showReference && !introducing) CaseReferenceScreen(state, dispatch)
    }
}

private fun intervalLabel(dueMillis: Long, nowMillis: Long): String {
    val minutes = maxOf(1L, (dueMillis - nowMillis + 30_000L) / 60_000L)
    return when {
        minutes < 60 -> "$minutes мин"
        minutes < 2_880 -> "${(minutes + 30) / 60} ч"
        else -> "${(minutes + 720) / 1_440} дн"
    }
}
