package polski.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import polski.cloze.AudioClozeSessionAction
import polski.presentation.AppAction
import polski.presentation.AppUiState

@Composable
fun AndroidAudioClozeScreen(state: AppUiState, dispatch: (AppAction) -> Unit) {
    val session = state.audioCloze ?: run {
        LaunchedEffect(Unit) { dispatch(AppAction.StartAudioCloze(limit = 10)) }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (session.exercises.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Нет доступных упражнений", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val exercise = session.exercises[session.currentIndex]
    val answered = session.answers[exercise.id]
    val isCorrect = answered == exercise.correctForm

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Progress
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "${session.currentIndex + 1} / ${session.exercises.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { (session.currentIndex + 1).toFloat() / session.exercises.size },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Audio segment placeholder (TODO: replace with ExoPlayer via ClippingMediaSource)
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Audio: ${exercise.startMs} ms – ${exercise.endMs} ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                OutlinedButton(
                    onClick = { dispatch(AppAction.AudioClozeAct(AudioClozeSessionAction.RecordReplay(exercise.id))) },
                    enabled = answered != null,
                ) {
                    Text("↺ Переслушать")
                }
            }
        }

        // Sentence with gap highlighted
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                buildAnnotatedString {
                    val parts = exercise.displaySentence.split("___")
                    parts.forEachIndexed { i, part ->
                        append(part)
                        if (i < parts.lastIndex) {
                            withStyle(SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )) {
                                append(answered ?: "___")
                            }
                        }
                    }
                },
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        // Options
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            exercise.options.forEach { option ->
                val isAnswer = answered == option
                val buttonColor = when {
                    !isAnswer -> ButtonDefaults.filledTonalButtonColors()
                    isCorrect -> ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    else -> ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
                FilledTonalButton(
                    onClick = {
                        if (answered == null) dispatch(AppAction.AudioClozeAct(AudioClozeSessionAction.Choose(exercise.id, option)))
                    },
                    enabled = answered == null || isAnswer,
                    colors = buttonColor,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(option)
                }
            }
        }

        // Feedback
        if (answered != null) {
            Text(
                if (isCorrect) "Правильно!" else "Неправильно. Правильный ответ: ${exercise.correctForm}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
        }

        // Navigation row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedButton(
                onClick = { dispatch(AppAction.AudioClozeAct(AudioClozeSessionAction.Previous)) },
                enabled = session.currentIndex > 0,
                modifier = Modifier.weight(1f),
            ) { Text("←") }

            OutlinedButton(
                onClick = { dispatch(AppAction.AudioClozeAct(AudioClozeSessionAction.Restart)) },
                modifier = Modifier.weight(1f),
            ) { Text("↺") }

            Button(
                onClick = { dispatch(AppAction.AudioClozeAct(AudioClozeSessionAction.Next)) },
                enabled = session.currentIndex < session.exercises.lastIndex,
                modifier = Modifier.weight(1f),
            ) { Text("→") }
        }
    }
}
