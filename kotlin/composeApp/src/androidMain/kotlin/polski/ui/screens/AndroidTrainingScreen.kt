package polski.ui.screens

import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import polski.data.courseContextHelp
import polski.data.nounById
import polski.data.courseChainPresentation
import polski.presentation.chainDisplayCount
import polski.data.skillById
import polski.data.presentationBySkillId
import polski.data.skills
import polski.grammar.caseRows
import polski.grammar.genderNames
import polski.grammar.nounPhrase
import polski.model.NumberGram
import polski.model.GramCase
import polski.presentation.AnswerMode
import polski.presentation.AppAction
import polski.presentation.AppTab
import polski.presentation.AppUiState
import polski.presentation.CardPhase
import polski.presentation.TrainingMode
import polski.presentation.ChangeSide
import polski.presentation.ExplanationMethod
import polski.presentation.changeHighlightParts
import polski.presentation.ContrastPair
import polski.presentation.sentenceHighlightParts
import polski.srs.Rating
import polski.training.sentenceSeeds
import polski.ui.contrastAnnotatedText
import polski.ui.ContrastPairText

@Composable
internal fun AndroidTrainingScreen(
    state: AppUiState,
    dispatch: (AppAction) -> Unit,
    focusReveal: FocusRequester,
    formatDate: (Long) -> String,
) {
    val introducing = state.phase == CardPhase.Question && state.introPending
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
                    Text("${state.dueCount} к повторению", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("${state.todayCount} сегодня", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        AndroidChoiceMenu(
            "Режим",
            state.mode.name,
            listOf(
                TrainingMode.Chain.name to "Цепочка предложений",
                TrainingMode.Schedule.name to "По расписанию",
                TrainingMode.Focused.name to "Отдельный навык",
            ),
        ) { selected ->
            when (selected) {
                TrainingMode.Chain.name -> dispatch(AppAction.StartChain())
                TrainingMode.Schedule.name -> dispatch(AppAction.StartSchedule)
                else -> dispatch(AppAction.OpenSkillPicker)
            }
        }
        if (state.showSkillPicker) {
            AndroidChoiceMenu("Выбрать навык", "", skills.map { it.id to it.title }) {
                dispatch(AppAction.ChooseSkill(it))
            }
        }
        if (state.mode == TrainingMode.Chain) {
            AndroidChoiceMenu(
                "Набор слов",
                state.seedIndex.toString(),
                sentenceSeeds.mapIndexed { index, seed -> index.toString() to nounById(seed.nounId).lemma },
            ) { dispatch(AppAction.SelectChainSeed(it.toInt())) }
            val displayCount = state.chainDisplayCount
            Text("$displayCount / ${state.chain.size} · ${courseChainPresentation.summary}",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LinearProgressIndicator(
                progress = { displayCount.toFloat() / state.chain.size.coerceAtLeast(1) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        when (state.phase) {
            CardPhase.ChainComplete -> AndroidInfoCard(courseChainPresentation.completion.title) {
                state.chain.forEachIndexed { index, exercise -> Text("${index + 1}. ${exercise.expected}") }
                Button(onClick = { dispatch(AppAction.StartChain((state.seedIndex + 1) % sentenceSeeds.size)) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Следующий набор слов")
                }
                OutlinedButton(onClick = { dispatch(AppAction.StartSchedule) }, modifier = Modifier.fillMaxWidth()) {
                    Text("К повторениям")
                }
            }
            CardPhase.NoDue -> AndroidInfoCard("Повторения на сейчас завершены") {
                Text(state.nextDue?.let { "Следующее: ${formatDate(it.toEpochMilliseconds())}" } ?: "Новых повторений пока нет.")
                Button(onClick = { dispatch(AppAction.StartChain()) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Потренировать цепочку")
                }
            }
            CardPhase.Question, CardPhase.Revealed -> {
                val exercise = state.exercise
                if (exercise != null) AndroidInfoCard("${skillById(exercise.primarySkill).level} · ${skillById(exercise.primarySkill).title}") {
                    val presentation = presentationBySkillId(exercise.primarySkill)
                    val method = if (state.explanationMethod == ExplanationMethod.Logic) presentation.logic else presentation.situations
                    AndroidChoiceMenu("Подача", state.explanationMethod.name,
                        listOf(ExplanationMethod.Logic.name to "Схемы и логика",
                            ExplanationMethod.Situations.name to "Живые ситуации")) {
                        dispatch(AppAction.SetExplanationMethod(ExplanationMethod.valueOf(it)))
                    }
                    if (state.phase == CardPhase.Question && state.introPending) {
                        Text("Знакомство с навыком", style = MaterialTheme.typography.titleMedium)
                        Text("ИСХОДНОЕ ПРЕДЛОЖЕНИЕ", style = MaterialTheme.typography.labelSmall)
                        Text(contrastAnnotatedText(sentenceHighlightParts(exercise.source, exercise.changes, ChangeSide.Before),
                            MaterialTheme.colorScheme.error), style = MaterialTheme.typography.headlineSmall)
                        Text(method.introduce)
                        Button(onClick = { dispatch(AppAction.ContinueIntroduction) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Перейти к заданию")
                        }
                    } else {
                    Text("ИСХОДНОЕ ПРЕДЛОЖЕНИЕ", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(contrastAnnotatedText(sentenceHighlightParts(exercise.source, exercise.changes, ChangeSide.Before),
                        MaterialTheme.colorScheme.error), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()) {
                        Text("${exercise.prompt}\n${method.promptLead}", modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    if (state.phase == CardPhase.Question) Text(method.retrieve)
                    if (state.phase == CardPhase.Question) {
                        AndroidChoiceMenu(
                            "Ответ",
                            state.answerMode.name,
                            listOf(AnswerMode.Oral.name to "Вслух / про себя", AnswerMode.Typed.name to "Напечатать"),
                        ) { dispatch(AppAction.SetAnswerMode(AnswerMode.valueOf(it))) }
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
                            modifier = Modifier.fillMaxWidth().focusRequester(focusReveal),
                            shape = RoundedCornerShape(16.dp),
                        ) { Text(if (state.answerMode == AnswerMode.Typed) "Проверить ответ" else "Показать ответ",
                            modifier = Modifier.padding(vertical = 7.dp)) }
                    } else {
                        Text("Эталон", style = MaterialTheme.typography.labelLarge)
                        Text(contrastAnnotatedText(sentenceHighlightParts(exercise.expected, exercise.changes, ChangeSide.After),
                            MaterialTheme.colorScheme.primary), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        if (exercise.accepted.isNotEmpty()) Text("Также: ${exercise.accepted.joinToString(" / ")}")
                        if (state.answerMode == AnswerMode.Typed) {
                            Text(if (state.evaluation?.correct == true) "Совпадает с правильным вариантом" else "Сравни свой ответ с эталоном")
                            Text(state.frozenAnswer?.takeIf(String::isNotEmpty) ?: "Ответ не введён")
                        }
                        if (state.explanationMethod == ExplanationMethod.Situations) {
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
                                    Text(contrastAnnotatedText(changeHighlightParts(change.from, change.to, ChangeSide.After),
                                        MaterialTheme.colorScheme.primary), fontWeight = FontWeight.Bold)
                                }
                                Text(change.reason, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("ЗАПОМНИ", style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer)
                                Text(skillById(exercise.primarySkill).formula, style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                if (state.explanationMethod == ExplanationMethod.Logic) {
                                    Text(method.feedback, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                    Text(exercise.explanation, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(contrastAnnotatedText(changeHighlightParts(presentation.focusBefore, presentation.focusAfter, ChangeSide.Before),
                                        MaterialTheme.colorScheme.error))
                                    Text("→")
                                    Text(contrastAnnotatedText(changeHighlightParts(presentation.focusBefore, presentation.focusAfter, ChangeSide.After),
                                        MaterialTheme.colorScheme.primary))
                                }
                            }
                        }
                        Text(method.review)
                        AndroidRatingActions(exercise.id, state, dispatch)
                    }
                    }
                }
            }
        }

        OutlinedButton(onClick = { dispatch(AppAction.ToggleReference) }, enabled = !introducing,
            modifier = Modifier.fillMaxWidth()) {
            Text(if (!introducing && state.showReference) "Скрыть таблицу" else "Таблица под рукой")
        }
        if (state.showReference && !introducing) AndroidCaseReference(state, dispatch)
    }
}

@Composable
private fun AndroidRatingActions(exerciseId: String, state: AppUiState, dispatch: (AppAction) -> Unit) {
    val thresholdPx = with(LocalDensity.current) { 72.dp.toPx() }
    var dragX by remember(exerciseId) { mutableFloatStateOf(0f) }
    var rated by remember(exerciseId) { mutableStateOf(false) }
    fun rate(rating: Rating) {
        if (!rated) {
            rated = true
            dispatch(AppAction.Rate(exerciseId, rating))
        }
    }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth().pointerInput(exerciseId, thresholdPx) {
            detectHorizontalDragGestures(
                onDragStart = { dragX = 0f },
                onHorizontalDrag = { change, amount ->
                    dragX += amount
                    change.consume()
                },
                onDragEnd = {
                    if (dragX <= -thresholdPx) rate(Rating.Again)
                    if (dragX >= thresholdPx) rate(Rating.Good)
                    dragX = 0f
                },
                onDragCancel = { dragX = 0f },
            )
        },
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Когда повторить?", style = MaterialTheme.typography.titleMedium)
            Text("Свайп влево — повторить · вправо — вспомнил",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Rating.Again to "Повторить", Rating.Good to "Вспомнил").forEach { (rating, label) ->
                    FilledTonalButton(
                        onClick = { rate(rating) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column {
                            Text(label)
                            Text(state.intervals?.get(rating)?.let {
                                intervalLabel(it.toEpochMilliseconds(), state.now?.toEpochMilliseconds() ?: it.toEpochMilliseconds())
                            }.orEmpty(), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AndroidCaseReference(state: AppUiState, dispatch: (AppAction) -> Unit) {
    val exercise = state.exercise ?: return
    val noun = nounById(exercise.nounId)
    AndroidInfoCard("Таблица этого предложения") {
        Text("${noun.lemma} · ${genderNames.getValue(noun.gender)} · ${if (exercise.number == NumberGram.SG) "ед. ч." else "мн. ч."}")
        caseRows.forEach { row ->
            Text("${row.pl} · ${row.ru}", style = MaterialTheme.typography.labelLarge)
            ContrastPairText(ContrastPair.generated(
                nounPhrase(exercise.nounId, GramCase.NOM, exercise.number, exercise.adjectiveId, exercise.possessive),
                nounPhrase(exercise.nounId, row.id, exercise.number, exercise.adjectiveId, exercise.possessive),
            ))
        }
        Text(courseContextHelp.compact)
        OutlinedButton(onClick = { dispatch(AppAction.SelectTab(AppTab.Matrix)) }, modifier = Modifier.fillMaxWidth()) {
            Text("Все таблицы и схема")
        }
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
