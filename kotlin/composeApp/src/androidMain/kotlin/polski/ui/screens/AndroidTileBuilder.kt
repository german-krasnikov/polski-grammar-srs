package polski.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import polski.core.model.TileSegment
import polski.presentation.AppAction
import polski.presentation.TileAction
import polski.presentation.TileBuilderState
import polski.presentation.UiCopy
import polski.presentation.togglePanel

private val TileMinHeight = 48.dp
private val TileShape = RoundedCornerShape(12.dp)

/**
 * SB-9: Question-phase tile row + inline picker.
 *
 * Local [openPicker] tracks which [TileSegment.Choice] position has its candidates expanded;
 * auto-opens the first unfilled slot on first composition (mirrors web's own auto-open,
 * SB6-AC2). Picking a candidate closes this slot and opens the next unfilled one.
 */
@Composable
internal fun AndroidTileBuilderSection(
    exerciseId: String,
    builder: TileBuilderState,
    dispatch: (AppAction) -> Unit,
) {
    val segments = remember(exerciseId) { builder.exercise.segments.sortedBy { it.position } }
    val choices = remember(segments) { segments.filterIsInstance<TileSegment.Choice>() }
    var openPicker by remember(exerciseId) { mutableStateOf<Int?>(null) }

    fun nextUnfilled(afterPosition: Int): TileSegment.Choice? =
        choices.filter { it.position > afterPosition }
            .firstOrNull { c -> builder.placed.none { it.position == c.position } }
            ?: choices.firstOrNull { c -> builder.placed.none { it.position == c.position } }

    // Auto-open first unfilled choice when the exercise loads.
    LaunchedEffect(exerciseId) {
        val first = choices.firstOrNull { c -> builder.placed.none { it.position == c.position } }
        openPicker = first?.position
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            segments.forEach { segment ->
                when (segment) {
                    is TileSegment.Locked -> Surface(
                        shape = TileShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            segment.text,
                            Modifier.defaultMinSize(minHeight = TileMinHeight).padding(horizontal = 10.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    is TileSegment.Choice -> {
                        val placed = builder.placed.firstOrNull { it.position == segment.position }?.selectedForm
                        OutlinedButton(
                            onClick = { openPicker = togglePanel(openPicker, segment.position) },
                            modifier = Modifier.defaultMinSize(minHeight = TileMinHeight),
                            shape = TileShape,
                        ) { Text(placed ?: "___") }
                    }
                }
            }
        }

        // Inline picker for the currently open choice
        val openChoice = choices.firstOrNull { it.position == openPicker }
        AnimatedVisibility(openChoice != null) {
            if (openChoice != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier.padding(10.dp).horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        (listOf(openChoice.correctForm) + openChoice.alternatives).forEach { form ->
                            Button(
                                onClick = {
                                    dispatch(AppAction.Tile(exerciseId, TileAction.Place(openChoice.position, form)))
                                    openPicker = nextUnfilled(openChoice.position)?.position
                                },
                                shape = TileShape,
                                modifier = Modifier.defaultMinSize(minHeight = TileMinHeight),
                            ) { Text(form) }
                        }
                    }
                }
            }
        }
    }
}

/**
 * SB-9: Revealed-phase tile row: correct tiles in primaryContainer, error tiles in error color
 * (tappable to dispatch [TileAction.TapError] and load [TileBuilderState.errorExplanation]).
 * Result banner follows the tile row; error explanation renders inline on tap.
 */
@Composable
internal fun AndroidTileBuilderResult(
    exerciseId: String,
    builder: TileBuilderState,
    reduceMotion: Boolean,
    dispatch: (AppAction) -> Unit,
) {
    val result = builder.result ?: return
    val segments = remember(exerciseId) { builder.exercise.segments.sortedBy { it.position } }
    var openError by remember(exerciseId) { mutableStateOf<Int?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            segments.forEach { segment ->
                when (segment) {
                    is TileSegment.Locked -> Surface(
                        shape = TileShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            segment.text,
                            Modifier.defaultMinSize(minHeight = TileMinHeight).padding(horizontal = 10.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    is TileSegment.Choice -> {
                        val placed = builder.placed.firstOrNull { it.position == segment.position }?.selectedForm
                        val error = result.errors.firstOrNull { it.position == segment.position }
                        when {
                            error == null && placed != null -> Surface(
                                shape = TileShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Text(
                                    placed,
                                    Modifier.defaultMinSize(minHeight = TileMinHeight).padding(horizontal = 10.dp, vertical = 12.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                            error != null -> OutlinedButton(
                                onClick = {
                                    val next = togglePanel(openError, segment.position)
                                    openError = next
                                    if (next == segment.position) {
                                        dispatch(AppAction.Tile(exerciseId, TileAction.TapError(segment.position)))
                                    }
                                },
                                modifier = Modifier.defaultMinSize(minHeight = TileMinHeight),
                                shape = TileShape,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            ) { Text(placed.orEmpty()) }
                            else -> Surface(
                                shape = TileShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                            ) {
                                Text(
                                    "___",
                                    Modifier.defaultMinSize(minHeight = TileMinHeight).padding(horizontal = 10.dp, vertical = 12.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Result banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (result.correct) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (result.correct) UiCopy.Matches else UiCopy.CompareAnswer,
                Modifier.padding(12.dp),
                color = if (result.correct) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.labelLarge,
            )
        }

        // Error explanation when an error tile is tapped
        if (openError != null && builder.errorExplanation != null) {
            AndroidBlockList(builder.errorExplanation!!, reduceMotion)
        }
    }
}
