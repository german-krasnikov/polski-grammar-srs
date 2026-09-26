package polski.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import polski.data.VocabularyItem
import polski.srs.Rating
import polski.vocabulary.StudyDirection
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularyLoadStatus
import polski.vocabulary.VocabularySession

/**
 * Android's own vocabulary screen (D2, `Plans/Kotlin/FlipCardRivePlan.md` §16.0-B): the review
 * card is a whole-panel 3D flip ([AndroidFlipCard]) instead of the shared `VocabularyScreen`'s
 * static reveal-in-place `Card`. Mirrors how [AndroidTrainingScreen]/`AndroidContent` already
 * fully replace the shared training screen for this host; the catalog below (unaffected by the
 * flip) reuses the shared, now-`internal` [VocabularyCatalog] instead of duplicating it.
 */
@Composable
fun AndroidVocabularyScreen(
    session: VocabularySession,
    onImport: () -> Unit,
    onExport: () -> Unit,
    launchMutation: (suspend () -> Unit) -> Unit,
    enableSwipeRating: Boolean,
    reduceMotion: Boolean,
) {
    val state by session.state.collectAsState()
    val item = state.currentId?.let { VocabularyCodec.item(state.document, it) }
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Слова и выражения", style = MaterialTheme.typography.headlineMedium)
        Text("Узнавание и воспроизведение повторяются по отдельным расписаниям.")
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

        AndroidChoiceMenu(
            "Направление",
            state.direction.name,
            listOf(StudyDirection.RussianToPolish.name to "Русский → польский", StudyDirection.PolishToRussian.name to "Польский → русский"),
        ) { session.setDirection(StudyDirection.valueOf(it)) }

        if (item == null) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(if (state.document.selectedIds.isEmpty()) "Выбери слова для тренировки" else "На сейчас всё повторено",
                        style = MaterialTheme.typography.titleLarge)
                    Text("Отметь готовые карточки в каталоге. История каждого направления сохраняется отдельно.")
                }
            }
        } else {
            val ratingGate = remember(item.id) { SingleRatingGate() }
            fun rate(rating: Rating) {
                ratingGate.rate(rating, reduceMotion, riveDisabledForMeasurement = false) { launchMutation { session.rate(it) } }
            }
            AndroidFlipCard(
                itemId = item.id,
                revealed = state.revealed,
                reduceMotion = reduceMotion,
                onRate = ::rate,
                front = { VocabularyFrontFace(item, state.direction, state.typed, state.draft, session) },
                back = { VocabularyBackFace(item, state.direction, state.typed, state.draft, enableSwipeRating) },
            )
        }

        VocabularyCatalog(session, launchMutation)
    }
}

/**
 * D2: the unrevealed face — no "Показать ответ" button; the prompt itself is the accessible
 * control (`role = Role.Button`, labelled "Показать ответ"), matching the web reference's
 * `promptBlock`. Typed mode keeps its own explicit "Проверить" button; taps inside the
 * [OutlinedTextField] never reach the prompt's `clickable` (the field consumes its own taps
 * first, the same nested-control precedent already verified for [AndroidTrainingScreen]'s
 * question card).
 */
@Composable
private fun VocabularyFrontFace(item: VocabularyItem, direction: StudyDirection, typed: Boolean, draft: String, session: VocabularySession) {
    val recallPolish = direction == StudyDirection.RussianToPolish
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(
                Modifier.fillMaxWidth().clickable(onClickLabel = "Показать ответ", role = Role.Button) { session.reveal() },
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(if (recallPolish) "Вспомни по-польски" else "Вспомни по-русски", style = MaterialTheme.typography.labelLarge)
                Text(if (recallPolish) item.translation else item.lemma, style = MaterialTheme.typography.headlineMedium)
            }
            AndroidChoiceMenu(
                "Ответ",
                if (typed) "typed" else "oral",
                listOf("oral" to "Вслух / про себя", "typed" to "Напечатать ответ"),
            ) { session.setTyped(it == "typed") }
            if (typed) {
                OutlinedTextField(draft, session::setDraft, label = { Text("Твой ответ") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { session.reveal() }, modifier = Modifier.fillMaxWidth()) { Text("Проверить") }
            }
        }
    }
}

/**
 * D2/D3: the revealed face — same panel chrome as [VocabularyFrontFace] (the whole rounded panel
 * is what flips *and* drags, `FlipCardRivePlan.md` §17.1/§17.3 applied to Android too), plus the
 * answer and (when [enableSwipeRating]) the swipe hint. No rating buttons (D3: "phones/tablets: no
 * rating buttons") — [AndroidFlipCard]'s [AndroidRatingDragSurface] owns both the drag-to-rate
 * gesture and its TalkBack equivalent actions, and (once revealed) the tap-to-flip-back gesture; a
 * further tap anywhere here that isn't a drag commit only flips the panel back visually.
 */
@Composable
private fun VocabularyBackFace(item: VocabularyItem, direction: StudyDirection, typed: Boolean, draft: String, enableSwipeRating: Boolean) {
    val recallPolish = direction == StudyDirection.RussianToPolish
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(if (recallPolish) "Эталон · польский" else "Эталон · русский", style = MaterialTheme.typography.labelLarge)
            Text(if (recallPolish) item.lemma else item.translation, style = MaterialTheme.typography.headlineSmall)
            Text("Перевод: ${item.translation}")
            Text("Форма: ${item.form}")
            Text("В предложении: ${item.example}")
            if (typed) Text("Твой ответ: ${draft.ifBlank { "не введён" }}. Сравни сам и выбери оценку.")
            if (enableSwipeRating) Text("Свайп влево — повторить · вправо — вспомнил",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
