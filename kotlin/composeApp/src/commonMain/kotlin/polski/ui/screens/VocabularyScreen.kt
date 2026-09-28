package polski.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import polski.data.activeCoursePackOption
import polski.data.courseVocabularyInstructions
import polski.data.courseVocabularyUnavailableLabel
import polski.data.VocabularyItem
import polski.data.frequencyItems
import polski.data.vocabularyItems
import polski.srs.Rating
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularyLoadStatus
import polski.vocabulary.VocabularySession
import polski.vocabulary.answerLanguageLabel
import polski.vocabulary.recallCaption
import polski.vocabulary.recallsTarget
import polski.vocabulary.studyDirectionOptions

private data class CatalogEntry(val rank: Int?, val lemma: String, val item: VocabularyItem?)

/** Shared Material vocabulary content; native hosts own persistence, pickers and navigation. */
@Composable
fun VocabularyScreen(session: VocabularySession, onImport: () -> Unit, onExport: () -> Unit,
                     launchMutation: (suspend () -> Unit) -> Unit, enableSwipeRating: Boolean = false) {
    val state by session.state.collectAsState()
    val item = state.currentId?.let { VocabularyCodec.item(state.document, it) }
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Слова и выражения", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Узнавание и воспроизведение повторяются по отдельным расписаниям.")
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (enableSwipeRating) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onImport, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Импорт словаря JSON") }
                OutlinedButton(onClick = onExport, enabled = !state.busy && session.exportJson() != null,
                    modifier = Modifier.fillMaxWidth()) { Text("Экспорт словаря JSON") }
            }
        } else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onImport, enabled = !state.busy) { Text("Импорт словаря JSON") }
            OutlinedButton(onClick = onExport, enabled = !state.busy && session.exportJson() != null) { Text("Экспорт словаря JSON") }
        }
        if (state.loadStatus != VocabularyLoadStatus.Ready) {
            Text(when (state.loadStatus) {
                VocabularyLoadStatus.Loading -> "Загружаем словарь"
                VocabularyLoadStatus.RecoveryRequired -> "Сохраните исходный JSON и импортируйте исправленный словарь."
                VocabularyLoadStatus.Unavailable -> "Хранилище словаря недоступно"
                VocabularyLoadStatus.Ready -> ""
            })
            return@Column
        }

        // EnRuAcceptance-2026-09-28.md §7 item 4: the active pack's own 2 directions, never a
        // hardcoded pl-ru pair — en-ru offers its own real "Русский → английский"/"Английский →
        // русский" choice here the same way.
        val pack = activeCoursePackOption
        val directionOptions = remember(pack.target, pack.native) { studyDirectionOptions(pack.target, pack.native) }
        val directionModifier = if (enableSwipeRating) Modifier.fillMaxWidth() else Modifier
        val directionContent: @Composable () -> Unit = {
            directionOptions.forEach { option ->
                VocabularyChoice(option.label, state.direction == option.direction, directionModifier) {
                    session.setDirection(option.direction)
                }
            }
        }
        if (enableSwipeRating) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { directionContent() }
        else Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { directionContent() }
        val swipeModifier = if (enableSwipeRating && state.revealed && item != null && !state.busy)
            Modifier.pointerInput(item.id, state.direction, state.revealed) {
                var horizontalDistance = 0f
                detectHorizontalDragGestures(
                    onDragStart = { horizontalDistance = 0f },
                    onDragEnd = {
                        when {
                            horizontalDistance < -72f -> launchMutation { session.rate(Rating.Again) }
                            horizontalDistance > 72f -> launchMutation { session.rate(Rating.Good) }
                        }
                    },
                ) { change, dragAmount ->
                    horizontalDistance += dragAmount
                    change.consume()
                }
            } else Modifier
        Card(Modifier.fillMaxWidth().then(swipeModifier)) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (item == null) {
                    Text(if (state.document.selectedIds.isEmpty()) "Выбери слова для тренировки" else "На сейчас всё повторено",
                        style = MaterialTheme.typography.titleLarge)
                    Text("Отметь готовые карточки в каталоге. История каждого направления сохраняется отдельно.")
                } else {
                    val recallTarget = state.direction.recallsTarget(pack.target, pack.native)
                    Text(state.direction.recallCaption(pack.target, pack.native), style = MaterialTheme.typography.labelLarge)
                    Text(if (recallTarget) item.translation else item.lemma, style = MaterialTheme.typography.headlineMedium)
                    if (!state.revealed) {
                        val answerModeContent: @Composable () -> Unit = {
                            VocabularyChoice("Ответ вслух / про себя", !state.typed,
                                if (enableSwipeRating) Modifier.fillMaxWidth() else Modifier) { session.setTyped(false) }
                            VocabularyChoice("Напечатать ответ", state.typed,
                                if (enableSwipeRating) Modifier.fillMaxWidth() else Modifier) { session.setTyped(true) }
                        }
                        if (enableSwipeRating) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { answerModeContent() }
                        else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { answerModeContent() }
                        if (state.typed) OutlinedTextField(state.draft, session::setDraft,
                            label = { Text("Твой ответ") }, modifier = Modifier.fillMaxWidth())
                        Button(onClick = { session.reveal() }, enabled = !state.busy) { Text("Показать ответ") }
                    } else {
                        Text("Эталон · ${state.direction.answerLanguageLabel(pack.target, pack.native)}",
                            style = MaterialTheme.typography.labelLarge)
                        Text(if (recallTarget) item.lemma else item.translation,
                            style = MaterialTheme.typography.headlineSmall)
                        Text("Перевод: ${item.translation}")
                        Text("Форма: ${item.form}")
                        Text("В предложении: ${item.example}")
                        if (state.typed) Text("Твой ответ: ${state.draft.ifBlank { "не введён" }}. Сравни сам и выбери оценку.")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { launchMutation { session.rate(Rating.Again) } }, enabled = !state.busy) {
                                Text("Повторить")
                            }
                            Button(onClick = { launchMutation { session.rate(Rating.Good) } }, enabled = !state.busy) {
                                Text("Вспомнил")
                            }
                        }
                    }
                }
            }
        }

        VocabularyCatalog(session, launchMutation)
    }
}

/** `internal` (not `private`) only so the Android host's own vocabulary screen (D2 whole-card
 * flip, `AndroidVocabularyScreen.kt`) can reuse the catalog unchanged instead of duplicating it;
 * no behavior change. */
@Composable
internal fun VocabularyCatalog(session: VocabularySession, launchMutation: (suspend () -> Unit) -> Unit) {
    val state by session.state.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<String?>(null) }
    var lemma by remember { mutableStateOf("") }
    var translation by remember { mutableStateOf("") }
    var form by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("—") }
    var levelMenu by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<String?>(null) }
    val entries = remember(state.document, state.filter) { catalogEntries(state.document.custom, state.filter) }
    val uriHandler = LocalUriHandler.current

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Мой словарь · ${state.document.selectedIds.size}", style = MaterialTheme.typography.titleLarge)
            Row {
                OutlinedButton(onClick = { menuExpanded = true }) { Text("Подборка: ${state.filter}") }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    listOf("A1", "A2", "B1", "100", "500", "1000", "mine").forEach { filter ->
                        DropdownMenuItem(text = { Text(if (filter == "mine") "Мои слова" else filter) }, onClick = {
                            session.setFilter(filter); menuExpanded = false
                        })
                    }
                }
            }
            Text(courseVocabularyInstructions.native,
                style = MaterialTheme.typography.bodySmall)
            if (state.filter in listOf("100", "500", "1000")) {
                val ready = entries.count { it.item?.custom == false }
                Text("Готово $ready/${entries.size} · недоступно ${entries.size - ready}",
                    style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = {
                uriHandler.openUri("https://github.com/KubaCiolo/leksjo-dane/blob/01782aa92cc842d0d3199079eba47ecbf05879e1/dane/nkjp-frekwencja.csv")
            }) { Text("Leksjo / NKJP · CC BY 4.0 · изменения: первые 1000 лемм, рангов и counts") }
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 440.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(entries, key = { it.rank?.let { rank -> "rank-$rank" } ?: it.item?.id ?: it.lemma }) { entry ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Checkbox(checked = entry.item?.id?.let { it in state.document.selectedIds } == true,
                            modifier = Modifier.semantics {
                                contentDescription = if (entry.item == null) "${entry.lemma}: карточка ещё не готова"
                                    else "${entry.lemma}: ${entry.item.translation}"
                            },
                            enabled = entry.item != null && !state.busy,
                            onCheckedChange = { checked ->
                                entry.item?.let { launchMutation { session.select(it.id, checked) } }
                            })
                        Column(Modifier.weight(1f)) {
                            Text(entry.lemma, fontWeight = FontWeight.SemiBold)
                            Text(entry.item?.translation ?: courseVocabularyUnavailableLabel,
                                style = MaterialTheme.typography.bodySmall)
                        }
                        entry.rank?.let { Text("№ $it", style = MaterialTheme.typography.bodySmall) }
                        if (entry.item?.custom == true) TextButton(onClick = {
                            editingId = entry.item.id; lemma = entry.item.lemma; translation = entry.item.translation
                            form = entry.item.form; example = entry.item.example; level = entry.item.level
                        }) { Text("Изменить") }
                    }
                }
            }

            Text(if (editingId == null) "Добавить своё слово" else "Изменить своё слово",
                style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(lemma, { lemma = it }, label = { Text("Польское слово") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(translation, { translation = it }, label = { Text("Перевод") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(form, { form = it }, label = { Text("Форма") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(example, { example = it }, label = { Text("Пример в предложении") }, modifier = Modifier.fillMaxWidth())
            Row {
                OutlinedButton(onClick = { levelMenu = true }) { Text("Уровень: $level") }
                DropdownMenu(expanded = levelMenu, onDismissRequest = { levelMenu = false }) {
                    listOf("—", "A1", "A2", "B1", "B2", "C1", "C2").forEach { value ->
                        DropdownMenuItem(text = { Text(value) }, onClick = { level = value; levelMenu = false })
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    launchMutation {
                        if (session.saveCustom(lemma, translation, form, example, level, editingId)) {
                            editingId = null; lemma = ""; translation = ""; form = ""; example = ""; level = "—"
                        }
                    }
                }, enabled = !state.busy) { Text(if (editingId == null) "Добавить слово" else "Сохранить изменения") }
                if (editingId != null) {
                    OutlinedButton(onClick = {
                        editingId = null; lemma = ""; translation = ""; form = ""; example = ""; level = "—"
                    }) { Text("Отмена") }
                    OutlinedButton(onClick = { deleteCandidate = editingId }) { Text("Удалить слово") }
                }
            }
        }
    }

    deleteCandidate?.let { id ->
        AlertDialog(onDismissRequest = { deleteCandidate = null },
            title = { Text("Удалить своё слово?") },
            text = { Text("Слово исчезнет из каталога, история оценок останется в экспортируемых данных.") },
            confirmButton = { TextButton(onClick = {
                launchMutation {
                    if (session.deleteCustom(id)) {
                        editingId = null; lemma = ""; translation = ""; form = ""; example = ""; level = "—"
                    }
                    deleteCandidate = null
                }
            }) { Text("Удалить") } },
            dismissButton = { TextButton(onClick = { deleteCandidate = null }) { Text("Отмена") } })
    }
}

@Composable
private fun VocabularyChoice(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (selected) Button(onClick = onClick, modifier = modifier) { Text(label) }
    else OutlinedButton(onClick = onClick, modifier = modifier) { Text(label) }
}

private fun catalogEntries(custom: List<VocabularyItem>, filter: String): List<CatalogEntry> {
    val all = vocabularyItems + custom
    val byLemma = vocabularyItems.associateBy(VocabularyItem::lemma)
    return when (filter) {
        "100", "500", "1000" -> frequencyItems.take(filter.toInt()).map { CatalogEntry(it.rank, it.lemma, byLemma[it.lemma]) }
        "mine" -> custom.map { CatalogEntry(null, it.lemma, it) }
        else -> all.filter { it.level == filter }.map { CatalogEntry(it.frequencyRank, it.lemma, it) }
    }
}
