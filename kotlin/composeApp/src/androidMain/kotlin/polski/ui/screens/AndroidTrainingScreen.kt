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
import androidx.compose.material3.Surface
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import polski.data.courseContextHelp
import polski.data.nounById
import polski.data.courseChainPresentation
import polski.presentation.chainDisplayCount
import polski.data.skillById
import polski.data.presentationBySkillId
import polski.data.styleContentBySkillId
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
import polski.presentation.CardEffect
import polski.presentation.CardPhase
import polski.presentation.TrainingMode
import polski.presentation.ChangeSide
import polski.presentation.StyleComposer
import polski.presentation.StyleId
import polski.presentation.builtInStyleIds
import polski.presentation.StylePhase
import polski.presentation.StyleRegistry
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
    swipeRatingEnabled: Boolean = true,
    reduceMotion: Boolean = false,
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
        AndroidCollapsible(visible = state.showSkillPicker, reduceMotion = reduceMotion) {
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
                // FC2-10/R3 Pick C: a one-shot "Tada" celebration, gated the same way the rating
                // cue is (reduced motion / measurement variant B) — never on the card, only here.
                if (!reduceMotion && !RiveMeasurementVariant.riveDisabled) AndroidChainCompleteOverlay()
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
                    val method = if (state.styleId == StyleId.SituationFirst) presentation.situations else presentation.logic
                    // S2: the composer decides which blocks the card shows — never a literal
                    // if/else on styleId here. A style whose `requires` isn't met for this skill
                    // (e.g. NativeContrast with no authored nativeParallel) resolves to its
                    // declared fallback, same as the settings picker's own hint (AndroidStylePicker.kt).
                    val skill = skillById(exercise.primarySkill)
                    val styleContent = styleContentBySkillId(exercise.primarySkill)
                    val effectiveStyle = StyleRegistry.recipes.getValue(
                        StyleComposer.resolveEffectiveStyle(StyleRegistry.recipes.getValue(state.styleId), styleContent, StyleRegistry.recipes)
                    )
                    val frontBlocks = StyleComposer.compose(effectiveStyle, StylePhase.Front, exercise, skill, presentation, styleContent)
                    val backBlocks = StyleComposer.compose(effectiveStyle, StylePhase.Back, exercise, skill, presentation, styleContent)
                    // S1: quick switch at the training card's existing method-toggle location, now
                    // all 4 styles — dispatching SetStyle never creates a review or clears the
                    // typed draft (TrainingStore.SetStyle is a plain state copy, see AppAction.kt).
                    AndroidChoiceMenu("Подача", state.styleId.value,
                        builtInStyleIds.map { it.value to styleLabel(StyleRegistry.recipes.getValue(it)) }) {
                        dispatch(AppAction.SetStyle(StyleId(it)))
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
                    // D1: no flip — the question stays visible and the answer expands downward
                    // below it (AndroidAnswerReveal/AndroidStaggeredReveal in AndroidFlipCard.kt).
                    // A single rating gate is shared by the buttons and the answer panel's swipe
                    // gesture so exactly one gesture/tap ever rates this card (FC-07/20).
                    var cardEffect by remember(exercise.id) { mutableStateOf<CardEffect?>(null) }
                    val ratingGate = remember(exercise.id) { SingleRatingGate() }
                    fun rate(rating: Rating) {
                        val effect = ratingGate.rate(rating, reduceMotion, RiveMeasurementVariant.riveDisabled, dispatch = {
                            dispatch(AppAction.Rate(exercise.id, it))
                        })
                        if (effect != null) cardEffect = effect
                    }
                    Box(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            Column(
                                Modifier.fillMaxWidth().then(
                                    // D1: a tap anywhere on the still-visible question reveals the
                                    // answer exactly once — only while there is a reveal to trigger;
                                    // once Revealed this modifier is gone, so a stray tap here does
                                    // nothing. Nested interactive descendants (the mode chooser, the
                                    // textarea, the button itself) consume their own taps first, so
                                    // this never double-fires alongside them.
                                    if (state.phase == CardPhase.Question) Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) { dispatch(AppAction.Reveal(exercise.id)) } else Modifier
                                ),
                                verticalArrangement = Arrangement.spacedBy(18.dp),
                            ) {
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
                                // S2: Front blocks stay visible across both phases, same as the
                                // question above them — they are this style's "with the question"
                                // content (Formula/Table/Scene/NativeParallel/Examples), not tied
                                // to reveal.
                                AndroidBlockList(frontBlocks, reduceMotion)
                                if (state.phase == CardPhase.Question) {
                                    Text(method.retrieve)
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
                                }
                            }
                            if (state.phase == CardPhase.Revealed) {
                                AndroidAnswerReveal(exercise.id, reduceMotion, onRate = ::rate) {
                                    AndroidStaggeredReveal(0, reduceMotion) {
                                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                            Text("Эталон", style = MaterialTheme.typography.labelLarge)
                                            Text(contrastAnnotatedText(sentenceHighlightParts(exercise.expected, exercise.changes, ChangeSide.After),
                                                MaterialTheme.colorScheme.primary), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                                            if (exercise.accepted.isNotEmpty()) Text("Также: ${exercise.accepted.joinToString(" / ")}")
                                            if (state.answerMode == AnswerMode.Typed) {
                                                Text(if (state.evaluation?.correct == true) "Совпадает с правильным вариантом" else "Сравни свой ответ с эталоном")
                                                Text(state.frozenAnswer?.takeIf(String::isNotEmpty) ?: "Ответ не введён")
                                            }
                                        }
                                    }
                                    AndroidStaggeredReveal(1, reduceMotion) {
                                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                            // S2: the lead-in feedback/explanation stay the one
                                            // non-block string per phase (Plans/Kotlin/StylesBlueprint.md
                                            // §1) — same text for every style, unlike the old
                                            // SituationFirst-only branch this replaces. Everything
                                            // below is the composer's own Back blocks for the
                                            // effective (post-fallback) style.
                                            Text(method.feedback)
                                            Text(exercise.explanation)
                                            AndroidBlockList(backBlocks, reduceMotion)
                                            Text(method.review)
                                        }
                                    }
                                    if (swipeRatingEnabled) AndroidStaggeredReveal(2, reduceMotion) {
                                        Text("Свайп влево — повторить · вправо — вспомнил",
                                            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        // D5: mounting is itself gated — reduceMotion (animations off, or system
                        // Motion.Reduced) means Rive.init/RiveAnimationView never run, not merely
                        // that no trigger fires (cardEffect is already null in that case via
                        // cardEffectToPlay, but the view must never be constructed either).
                        if (!reduceMotion) AndroidRiveOverlay(cardEffect) { cardEffect = null }
                    }
                    }
                }
            }
        }

        OutlinedButton(onClick = { dispatch(AppAction.ToggleReference) }, enabled = !introducing,
            modifier = Modifier.fillMaxWidth()) {
            Text(if (!introducing && state.showReference) "Скрыть таблицу" else "Таблица под рукой")
        }
        AndroidCollapsible(visible = state.showReference && !introducing, reduceMotion = reduceMotion) {
            AndroidCaseReference(state, dispatch)
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
