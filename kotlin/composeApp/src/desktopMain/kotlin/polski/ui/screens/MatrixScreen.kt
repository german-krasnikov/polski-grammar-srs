package polski.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import polski.data.courseMatrixIntroduction
import polski.data.courseContextHelp
import polski.data.adjectives
import polski.data.nounById
import polski.data.nouns
import polski.data.personalPronouns
import polski.data.referencePronounTeaching
import polski.data.possessives
import polski.data.verbs
import polski.data.referenceChainRows
import polski.data.referenceSystemCards
import polski.data.referencePipeline
import polski.data.referenceRussianSupport
import polski.data.referenceCaseTeaching
import polski.data.referenceVerbTeaching
import polski.data.comparisonNounIds
import polski.data.referenceTenseRows
import polski.data.referenceAspectRows
import polski.data.maleAccRows
import polski.ui.ReferenceChainComparison
import polski.ui.ReferenceTenseComparison
import polski.ui.ReferenceAspectComparison
import polski.ui.MaleAccComparison
import polski.ui.ContrastPairText
import polski.grammar.caseRows
import polski.grammar.caseSentence
import polski.grammar.genderNames
import polski.grammar.nounPhrase
import polski.grammar.possessiveForm
import polski.grammar.verbForm
import polski.model.Aspect
import polski.model.Gender
import polski.model.GramCase
import polski.model.NumberGram
import polski.model.Person
import polski.model.PossessiveId
import polski.model.SentenceSeed
import polski.model.Tense
import polski.presentation.AppAction
import polski.presentation.AppTab
import polski.presentation.AppUiState
import polski.presentation.MatrixSection
import polski.presentation.ContrastPair

@Composable
internal fun MatrixScreen(state: AppUiState, dispatch: (AppAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Грамматическая матрица", style = MaterialTheme.typography.headlineSmall)
        Text(courseMatrixIntroduction)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                MatrixSection.Map to "Карта системы",
                MatrixSection.Cases to "Падежи и окончания",
                MatrixSection.Verbs to "Времена и лица",
                MatrixSection.Pronouns to "Местоимения",
            ).forEach { (section, label) ->
                OutlinedButton(onClick = { dispatch(AppAction.SelectMatrixSection(section)) }) {
                    Text(if (state.matrixSelection.section == section) "• $label" else label)
                }
            }
        }
        when (state.matrixSelection.section) {
            MatrixSection.Map -> MapDesktop(dispatch)
            MatrixSection.Cases -> CasesDesktop(state, dispatch)
            MatrixSection.Verbs -> VerbsDesktop(state, dispatch)
            MatrixSection.Pronouns -> PronounsDesktop(dispatch)
        }
    }
}

@Composable
internal fun CaseReferenceScreen(state: AppUiState, dispatch: (AppAction) -> Unit) {
    val exercise = state.exercise ?: return
    val noun = nounById(exercise.nounId)
    MatrixCard("Таблица этого предложения") {
        Text("${noun.lemma} · ${genderNames.getValue(noun.gender)} · ${if (exercise.number == NumberGram.SG) "ед. ч." else "мн. ч."}")
        MatrixTable(
            listOf("Падеж", "Вся группа слов"),
            caseRows.map { row -> listOf(
                "${row.pl} · ${row.ru}",
                nounPhrase(exercise.nounId, row.id, exercise.number, exercise.adjectiveId, exercise.possessive),
            ) },
            contrastFrom = { _, column -> if (column == 1) nounPhrase(exercise.nounId, GramCase.NOM,
                exercise.number, exercise.adjectiveId, exercise.possessive) else null },
        )
        Text(courseContextHelp.compact)
        OutlinedButton(onClick = { dispatch(AppAction.SelectTab(AppTab.Matrix)) }) { Text("Все таблицы и схема") }
    }
}

@Composable
private fun MapDesktop(dispatch: (AppAction) -> Unit) {
    MatrixCard(referencePipeline.title) {
        Text(referencePipeline.compactSummary)
        Text(referencePipeline.compactExample)
        referenceSystemCards.forEach { card -> Text("${card.title} · ${card.explanation} · ${card.example}") }
    }
    MatrixCard("Одна мысль, пять преобразований") {
        referenceChainRows.forEach { ReferenceChainComparison(it) }
        OutlinedButton(onClick = { dispatch(AppAction.ChooseSkill("chain", SentenceSeed("wife", "beautiful"))) }) {
            Text("Тренировать эту цепочку")
        }
    }
    MatrixCard("Мужской Biernik: дерево решений") {
        maleAccRows.forEach { MaleAccComparison(it) }
        OutlinedButton(onClick = { dispatch(AppAction.ChooseSkill("case.acc.m", SentenceSeed("friendM", "good"))) }) {
            Text("Тренировать мужской род")
        }
    }
    MatrixCard(referenceRussianSupport.compactTitle) {
        MatrixTable(
            referenceRussianSupport.columns,
            referenceRussianSupport.rows.map { row -> listOf(row.cue, row.desktop.construction, row.desktop.check) },
            authoredPairs = { row, column -> if (column == 1) referenceRussianSupport.rows[row].comparisons else emptyList() },
        )
    }
}

@Composable
private fun CasesDesktop(state: AppUiState, dispatch: (AppAction) -> Unit) {
    val selected = state.matrixSelection
    MatrixCard("Все семь падежей на одной группе слов") {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OptionSelector("Слово", selected.nounId, nouns.map { it.id to "${it.lemma} — ${it.meaning}" }) {
                dispatch(AppAction.SetMatrixSelection(selected.copy(nounId = it)))
            }
            OptionSelector("Прилагательное", selected.adjectiveId, adjectives.map { it.id to "${it.lemma} — ${it.meaning}" }) {
                dispatch(AppAction.SetMatrixSelection(selected.copy(adjectiveId = it)))
            }
            OptionSelector("Владелец", selected.ownerId, possessives.map { it.id.id to it.label }) {
                dispatch(AppAction.SetMatrixSelection(selected.copy(ownerId = it)))
            }
            OptionSelector("Число", selected.numberId, listOf("sg" to "Единственное", "pl" to "Множественное")) {
                dispatch(AppAction.SetMatrixSelection(selected.copy(numberId = it)))
            }
        }
        val number = NumberGram.fromId(selected.numberId)
        val owner = PossessiveId.fromId(selected.ownerId)
        val seed = SentenceSeed(selected.nounId, selected.adjectiveId)
        MatrixTable(
            listOf("Падеж · русская опора", "Вопрос / конструкция", "Вся группа слов", "Целое предложение"),
            caseRows.map { row -> listOf(
                "${row.pl} · ${row.ru}",
                "${row.question} · ${row.trigger}",
                nounPhrase(selected.nounId, row.id, number, selected.adjectiveId, owner),
                caseSentence(seed, row.id, owner, number),
            ) },
            contrastFrom = { _, column -> when (column) {
                2 -> nounPhrase(selected.nounId, GramCase.NOM, number, selected.adjectiveId, owner)
                3 -> caseSentence(seed, GramCase.NOM, owner, number)
                else -> null
            } },
        )
        Text(referenceCaseTeaching.compactNote)
        OutlinedButton(onClick = { dispatch(AppAction.ChooseSkill("case.gen.neg", seed)) }) { Text("Тренировать отрицание") }
    }
    MatrixCard("Сравнение типов склонения") {
        val number = NumberGram.fromId(selected.numberId)
        val ids = comparisonNounIds
        MatrixTable(
            listOf("Падеж") + ids.map { nounById(it).lemma },
            caseRows.map { row -> listOf(row.pl) + ids.map { nounById(it).forms.getValue(number).getValue(row.id) } },
            contrastFrom = { _, column -> if (column > 0) nounById(ids[column - 1]).forms.getValue(number).getValue(GramCase.NOM) else null },
        )
    }
}

@Composable
private fun VerbsDesktop(state: AppUiState, dispatch: (AppAction) -> Unit) {
    val selected = state.matrixSelection
    MatrixCard("Лицо × число × время") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OptionSelector("Глагол", selected.verbId, verbs.filter { it.aspect == Aspect.IMPERFECTIVE }.map { it.id to "${it.lemma} — ${it.meaning}" }) {
                dispatch(AppAction.SetMatrixSelection(selected.copy(verbId = it)))
            }
            OptionSelector(referenceVerbTeaching.genderControlLabel.compact, if (selected.feminineGroup) "f" else "m",
                referenceVerbTeaching.genderOptions.map { it.id to it.label.compact }) {
                dispatch(AppAction.SetMatrixSelection(selected.copy(feminineGroup = it == "f")))
            }
        }
        MatrixTable(
            listOf("Кто") + listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE).map { referenceVerbTeaching.tenseLabels.getValue(it).compact },
            referenceVerbTeaching.subjects.map { subject -> listOf(subject.label.compact) + listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE).map { tense ->
                verbForm(selected.verbId, tense, subject.person, subject.number, subject.gender(selected.feminineGroup))
            } },
            contrastFrom = { _, column -> if (column > 0) verbs.first { it.id == selected.verbId }.lemma else null },
        )
        Text(referenceVerbTeaching.compactFutureExplanation)
    }
    MatrixCard("Время меняется, предложение остаётся целым") {
        referenceTenseRows.forEach { ReferenceTenseComparison(it) }
        OutlinedButton(onClick = { dispatch(AppAction.ChooseSkill("verb.past", SentenceSeed("wife", "beautiful"))) }) {
            Text("Тренировать времена")
        }
    }
    MatrixCard("Вид: процесс или результат") {
        referenceAspectRows.forEach { ReferenceAspectComparison(it) }
    }
}

@Composable
private fun PronounsDesktop(dispatch: (AppAction) -> Unit) {
    val teaching = referencePronounTeaching
    MatrixCard(teaching.personalTitle) {
        Text(teaching.compactIntro)
        MatrixTable(
            listOf("Кто") + teaching.contexts.map { it.cue.compact },
            teaching.pronounIds.map { id -> listOf(id) + teaching.contexts.map { it.value(id, personalPronouns.getValue(id)) } },
            contrastFrom = { row, column -> if (column > 0) teaching.pronounIds[row] else null },
        )
        Text(teaching.nativeFooter)
    }
    MatrixCard(teaching.possessiveTitle) {
        MatrixTable(
            listOf("Кому принадлежит") + teaching.demo.cases.map { it.desktop } + "Правило",
            possessives.map { possessive ->
                listOf(possessive.label) + teaching.demo.cases.map { teaching.demo.phrase(possessive.id, it.id) } +
                    listOf(teaching.demo.rule(possessive.id))
            },
            contrastFrom = { row, column -> if (column in 1..teaching.demo.cases.size)
                teaching.demo.phrase(possessives[row].id, GramCase.NOM) else null },
        )
        OutlinedButton(onClick = { dispatch(AppAction.ChooseSkill("agreement.my", SentenceSeed(teaching.demo.nounId, teaching.demo.adjectiveId))) }) {
            Text("Тренировать смену владельца")
        }
    }
}

@Composable
private fun MatrixCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@Composable
private fun OptionSelector(label: String, selected: String, options: List<Pair<String, String>>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("$label: ${options.firstOrNull { it.first == selected }?.second ?: selected}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, title) ->
                DropdownMenuItem(text = { Text(title) }, onClick = { expanded = false; onSelect(id) })
            }
        }
    }
}

@Composable
private fun MatrixTable(
    headers: List<String>,
    rows: List<List<String>>,
    contrastFrom: (Int, Int) -> String? = { _, _ -> null },
    authoredPairs: (Int, Int) -> List<ContrastPair> = { _, _ -> emptyList() },
) {
    val widths = headers.mapIndexed { index, _ -> if (index == 0) 170.dp else 220.dp }
    Column(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        Row {
            headers.forEachIndexed { index, header ->
                Text(header, modifier = Modifier.width(widths[index]).padding(8.dp), style = MaterialTheme.typography.labelLarge)
            }
        }
        HorizontalDivider()
        rows.forEachIndexed { rowIndex, cells ->
            Row {
                cells.forEachIndexed { index, value ->
                    Column(Modifier.width(widths[index]).padding(8.dp)) {
                        val before = contrastFrom(rowIndex, index)
                        if (before == null) Text(value)
                        else ContrastPairText(ContrastPair.generated(before, value))
                        authoredPairs(rowIndex, index).forEach { ContrastPairText(it) }
                    }
                }
            }
            HorizontalDivider()
        }
    }
}
