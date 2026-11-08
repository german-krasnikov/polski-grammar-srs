package polski.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import polski.presentation.AnswerMode
import polski.presentation.AppAction
import polski.presentation.AppUiState
import polski.presentation.CardSection
import polski.ui.contrastAnnotatedText

/** Renders one [CardSection] from `TrainingCardModel.trainingCardModel`'s `front`/`back` lists —
 *  the Desktop-only chrome each branch needs (answer-mode tabs, draft field, rating hint prefix)
 *  stays here rather than on the shared, Compose-free [CardSection] contract. [CardSection.Styled]
 *  delegates to [DesktopBlockView] (`DesktopStyleBlocks.kt`). */
@Composable
internal fun CardSectionView(section: CardSection, state: AppUiState, dispatch: (AppAction) -> Unit, focusReveal: FocusRequester) {
    when (section) {
        is CardSection.Styled -> DesktopBlockView(section.block)
        is CardSection.SourceSentence -> {
            Text("ИСХОДНОЕ ПРЕДЛОЖЕНИЕ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(contrastAnnotatedText(section.parts, MaterialTheme.colorScheme.error), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        }
        is CardSection.PromptBox -> Text(
            "${section.prompt}\n${section.lead}",
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(16.dp)).padding(18.dp),
            style = MaterialTheme.typography.titleLarge,
        )
        is CardSection.MethodIntro -> Text(section.text)
        is CardSection.RetrieveHint -> {
            Text(section.text)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TabButton("Ответ вслух / про себя", state.answerMode == AnswerMode.Oral) { dispatch(AppAction.SetAnswerMode(AnswerMode.Oral)) }
                TabButton("Напечатать ответ", state.answerMode == AnswerMode.Typed) { dispatch(AppAction.SetAnswerMode(AnswerMode.Typed)) }
            }
            if (state.answerMode == AnswerMode.Oral) Text("Произнеси целое предложение, затем покажи ответ.")
        }
        is CardSection.AnswerInput -> if (section.mode == AnswerMode.Typed) OutlinedTextField(
            value = section.draft,
            onValueChange = { dispatch(AppAction.EditAnswer(it)) },
            label = { Text("Ответ по-польски") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
        is CardSection.Action -> Button(
            onClick = { dispatch(section.action) },
            modifier = if (section.action is AppAction.Reveal) Modifier.focusRequester(focusReveal) else Modifier,
        ) { Text(section.label) }
        is CardSection.Expected -> {
            Text("Эталон", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(contrastAnnotatedText(section.parts, MaterialTheme.colorScheme.primary), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            if (section.accepted.isNotEmpty()) Text("Также: ${section.accepted.joinToString(" / ")}")
        }
        is CardSection.EvaluationFeedback -> {
            Text(if (section.matched) "Совпадает с правильным вариантом" else "Сравни свой ответ с эталоном")
            Text(section.answerText.ifEmpty { "Ответ не введён" })
        }
        is CardSection.MethodFeedback -> {
            Text(section.feedback)
            Text(section.explanation)
        }
        is CardSection.LifehackSection -> DesktopLifehackBlock(section.lifehacks)
        is CardSection.ReviewHint -> {
            Text("Когда повторить?", style = MaterialTheme.typography.titleMedium)
            Text(section.text)
        }
    }
}
