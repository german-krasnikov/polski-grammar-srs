package polski.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import polski.presentation.AnswerMode
import polski.presentation.AppAction
import polski.presentation.AppTab
import polski.presentation.AppUiState
import polski.presentation.CardPhase
import polski.presentation.CardSection
import polski.presentation.ContrastPair
import polski.presentation.UiCopy
import polski.ui.ContrastPairText
import polski.ui.ContrastPairTextMasked

@Composable
internal fun AndroidCardSection(section: CardSection, state: AppUiState, dispatch: (AppAction) -> Unit, focusReveal: FocusRequester, reduceMotion: Boolean) {
    when (section) {
        is CardSection.SourceSentence -> {
            Text("ИСХОДНОЕ ПРЕДЛОЖЕНИЕ", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            AndroidEmphasisText(section.parts, before = true, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        }
        is CardSection.PromptBox -> Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()) {
            Text("${section.prompt}\n${section.lead}", modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        is CardSection.MethodIntro -> Text(section.text)
        is CardSection.RetrieveHint -> {
            Text(section.text)
            AndroidChoiceMenu("Ответ", state.answerMode.name,
                listOf(AnswerMode.Oral.name to "Вслух / про себя", AnswerMode.Typed.name to "Напечатать"),
            ) { dispatch(AppAction.SetAnswerMode(AnswerMode.valueOf(it))) }
        }
        is CardSection.AnswerInput -> OutlinedTextField(
            value = state.draft,
            onValueChange = { dispatch(AppAction.EditAnswer(it)) },
            label = { Text("Ответ по-польски") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
        is CardSection.Action -> Button(
            onClick = { dispatch(section.action) },
            modifier = Modifier.fillMaxWidth().focusRequester(focusReveal),
            shape = RoundedCornerShape(16.dp),
        ) { Text(section.label, modifier = Modifier.padding(vertical = 7.dp)) }
        is CardSection.Expected -> {
            Text("Эталон", style = MaterialTheme.typography.labelLarge)
            AndroidEmphasisText(section.parts, before = false, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            if (section.accepted.isNotEmpty()) Text("Также: ${section.accepted.joinToString(" / ")}")
        }
        is CardSection.EvaluationFeedback -> {
            Text(if (section.matched) "Совпадает с правильным вариантом" else UiCopy.CompareAnswer)
            Text(section.answerText.takeIf(String::isNotEmpty) ?: "Ответ не введён")
        }
        is CardSection.MethodFeedback -> { Text(section.feedback); Text(section.explanation) }
        is CardSection.LifehackSection -> {}
        is CardSection.ReviewHint -> Text(section.text)
        is CardSection.Styled -> {}
    }
}

@Composable
fun AndroidCaseReference(state: AppUiState, dispatch: (AppAction) -> Unit) {
    val exercise = state.exercise ?: return
    val ref = polski.presentation.caseReferenceModel(exercise, state.phase == CardPhase.Revealed)
    AndroidInfoCard(ref.title) {
        Text(ref.nounInfo)
        ref.rows.forEach { row ->
            Text(row.caseLabel, style = MaterialTheme.typography.labelLarge)
            if (row.isTarget && row.contrast == null) {
                ContrastPairTextMasked(row.phrase)
            } else {
                ContrastPairText(row.contrast ?: ContrastPair.generated(row.phrase, row.phrase))
            }
        }
        Text(ref.contextHelp)
        OutlinedButton(onClick = { dispatch(AppAction.SelectTab(AppTab.Matrix)) }, modifier = Modifier.fillMaxWidth()) {
            Text(UiCopy.AllTablesAndSchema)
        }
    }
}
