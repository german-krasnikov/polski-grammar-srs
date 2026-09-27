package polski.ui.screens

import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import polski.data.courseMatrixIntroduction
import polski.data.adjectives
import polski.data.nounById
import polski.data.nouns
import polski.data.personalPronouns
import polski.data.referencePronounTeaching
import polski.data.possessives
import polski.data.verbs
import polski.data.referenceChainRows
import polski.data.referenceSystemCards
import polski.data.ReferenceSystemCard
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
import polski.grammar.nounPhrase
import polski.grammar.verbForm
import polski.model.Aspect
import polski.model.GramCase
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.model.SentenceSeed
import polski.model.Tense
import polski.presentation.AppAction
import polski.presentation.AppUiState
import polski.presentation.MatrixSection
import polski.presentation.ContrastPair

@Composable
internal fun AndroidMatrixScreen(state: AppUiState, dispatch: (AppAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Грамматическая матрица", style = MaterialTheme.typography.headlineSmall)
        Text(courseMatrixIntroduction)
        AndroidChoiceMenu(
            "Раздел",
            state.matrixSelection.section.name,
            listOf(
                MatrixSection.Map.name to "Карта системы",
                MatrixSection.Cases.name to "Падежи и окончания",
                MatrixSection.Verbs.name to "Времена и лица",
                MatrixSection.Pronouns.name to "Местоимения",
            ),
        ) { dispatch(AppAction.SelectMatrixSection(MatrixSection.valueOf(it))) }
        when (state.matrixSelection.section) {
            MatrixSection.Map -> AndroidMapSection(dispatch)
            MatrixSection.Cases -> AndroidCasesSection(state, dispatch)
            MatrixSection.Verbs -> AndroidVerbsSection(state, dispatch)
            MatrixSection.Pronouns -> AndroidPronounsSection(dispatch)
        }
    }
}

@Composable
private fun AndroidMapSection(dispatch: (AppAction) -> Unit) {
    AndroidInfoCard(referencePipeline.title) {
        Text(referencePipeline.compactSummary)
        Text(referencePipeline.compactExample)
        referenceSystemCards.forEach { card -> AndroidSystemMapCard(card) }
    }
    AndroidInfoCard("Одна мысль, пять преобразований") {
        referenceChainRows.forEach { row ->
            ReferenceChainComparison(row)
        }
        OutlinedButton(
            onClick = { dispatch(AppAction.ChooseSkill("chain", SentenceSeed("wife", "beautiful"))) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Тренировать эту цепочку") }
    }
    AndroidInfoCard("Мужской Biernik: дерево решений") {
        maleAccRows.forEach { MaleAccComparison(it) }
        OutlinedButton(
            onClick = { dispatch(AppAction.ChooseSkill("case.acc.m", SentenceSeed("friendM", "good"))) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Тренировать мужской род") }
    }
    AndroidInfoCard(referenceRussianSupport.compactTitle) {
        referenceRussianSupport.rows.forEach { row ->
            Text(row.mobileLine)
            row.comparisons.forEach { ContrastPairText(it) }
        }
    }
}

/** E6 (EmphasisUXAudit-2026-09-27.md, C2 steps): the map-overview card used to flatten
 *  `card.example` into one prose string. `card.steps` is the same chain, split; render each step
 *  as its own node with a visible arrow between (`E6` "разметка у явных стрелок"), one merged
 *  semantics node speaking the chain as one unit (same pattern as `ContrastPairText`). */
@Composable
fun AndroidSystemMapCard(card: ReferenceSystemCard) {
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(card.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(card.explanation, style = MaterialTheme.typography.bodyMedium)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.clearAndSetSemantics { contentDescription = card.steps.joinToString(" → ") },
            ) {
                card.steps.forEachIndexed { index, step ->
                    Text(step, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    if (index < card.steps.lastIndex) Text("→", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun AndroidCasesSection(state: AppUiState, dispatch: (AppAction) -> Unit) {
    val selected = state.matrixSelection
    AndroidInfoCard("Выбери группу слов") {
        AndroidChoiceMenu("Слово", selected.nounId, nouns.map { it.id to "${it.lemma} — ${it.meaning}" }) {
            dispatch(AppAction.SetMatrixSelection(selected.copy(nounId = it)))
        }
        AndroidChoiceMenu("Прилагательное", selected.adjectiveId, adjectives.map { it.id to "${it.lemma} — ${it.meaning}" }) {
            dispatch(AppAction.SetMatrixSelection(selected.copy(adjectiveId = it)))
        }
        AndroidChoiceMenu("Владелец", selected.ownerId, possessives.map { it.id.id to it.label }) {
            dispatch(AppAction.SetMatrixSelection(selected.copy(ownerId = it)))
        }
        AndroidChoiceMenu("Число", selected.numberId, listOf("sg" to "Единственное", "pl" to "Множественное")) {
            dispatch(AppAction.SetMatrixSelection(selected.copy(numberId = it)))
        }
    }
    val number = NumberGram.fromId(selected.numberId)
    val owner = PossessiveId.fromId(selected.ownerId)
    val seed = SentenceSeed(selected.nounId, selected.adjectiveId)
    val basePhrase = nounPhrase(selected.nounId, GramCase.NOM, number, selected.adjectiveId, owner)
    val baseSentence = caseSentence(seed, GramCase.NOM, owner, number)
    caseRows.forEach { row ->
        AndroidInfoCard("${row.pl} · ${row.ru}") {
            Text("${row.question} · ${row.trigger}", style = MaterialTheme.typography.labelLarge)
            ContrastPairText(ContrastPair.generated(basePhrase,
                nounPhrase(selected.nounId, row.id, number, selected.adjectiveId, owner)))
            ContrastPairText(ContrastPair.generated(baseSentence, caseSentence(seed, row.id, owner, number)))
        }
    }
    Text(referenceCaseTeaching.compactNote)
    OutlinedButton(onClick = { dispatch(AppAction.ChooseSkill("case.gen.neg", seed)) }, modifier = Modifier.fillMaxWidth()) {
        Text("Тренировать отрицание")
    }
    var comparisonCase by remember { mutableStateOf(caseRows.first().id) }
    AndroidInfoCard("Сравнение типов склонения") {
        AndroidChoiceMenu("Падеж для сравнения", comparisonCase.name, caseRows.map { it.id.name to it.pl }) {
            comparisonCase = GramCase.valueOf(it)
        }
        comparisonNounIds.forEach { id ->
            val noun = nounById(id)
            Text(noun.lemma)
            ContrastPairText(ContrastPair.generated(noun.forms.getValue(number).getValue(GramCase.NOM),
                noun.forms.getValue(number).getValue(comparisonCase)))
        }
    }
}

@Composable
private fun AndroidVerbsSection(state: AppUiState, dispatch: (AppAction) -> Unit) {
    val selected = state.matrixSelection
    AndroidInfoCard("Лицо × число × время") {
        AndroidChoiceMenu("Глагол", selected.verbId, verbs.filter { it.aspect == Aspect.IMPERFECTIVE }.map { it.id to "${it.lemma} — ${it.meaning}" }) {
            dispatch(AppAction.SetMatrixSelection(selected.copy(verbId = it)))
        }
        AndroidChoiceMenu(referenceVerbTeaching.genderControlLabel.compact, if (selected.feminineGroup) "f" else "m",
            referenceVerbTeaching.genderOptions.map { it.id to it.label.compact }) {
            dispatch(AppAction.SetMatrixSelection(selected.copy(feminineGroup = it == "f")))
        }
        Text(referenceVerbTeaching.compactFutureExplanation)
    }
    referenceVerbTeaching.subjects.forEach { subject ->
        AndroidInfoCard(subject.label.compact) {
            listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE).forEach { tense ->
                Text(referenceVerbTeaching.tenseLabels.getValue(tense).compact)
                ContrastPairText(ContrastPair.generated(verbs.first { it.id == selected.verbId }.lemma,
                    verbForm(selected.verbId, tense, subject.person, subject.number, subject.gender(selected.feminineGroup))))
            }
        }
    }
    AndroidInfoCard("Время меняется, предложение остаётся целым") {
        referenceTenseRows.forEach { ReferenceTenseComparison(it) }
        OutlinedButton(
            onClick = { dispatch(AppAction.ChooseSkill("verb.past", SentenceSeed("wife", "beautiful"))) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Тренировать времена") }
    }
    AndroidInfoCard("Вид: процесс или результат") {
        referenceAspectRows.forEach { ReferenceAspectComparison(it) }
    }
}

@Composable
private fun AndroidPronounsSection(dispatch: (AppAction) -> Unit) {
    val teaching = referencePronounTeaching
    Text(teaching.compactIntro)
    teaching.pronounIds.forEach { id ->
        val forms = personalPronouns.getValue(id)
        AndroidInfoCard(id) {
            teaching.contexts.forEach { context ->
                Text(context.cue.compact)
                ContrastPairText(ContrastPair.generated(id,
                    if (context.id == GramCase.LOC) forms.getValue(GramCase.LOC) else context.value(id, forms)))
            }
        }
    }
    Text(teaching.nativeFooter)
    Text(teaching.possessiveTitle, style = MaterialTheme.typography.titleLarge)
    possessives.forEach { possessive ->
        AndroidInfoCard(possessive.label) {
            teaching.demo.cases.forEach { row ->
                Text(row.id.name)
                ContrastPairText(ContrastPair.generated(teaching.demo.phrase(possessive.id, GramCase.NOM),
                    teaching.demo.phrase(possessive.id, row.id)))
            }
            Text(teaching.demo.rule(possessive.id))
        }
    }
    OutlinedButton(
        onClick = { dispatch(AppAction.ChooseSkill("agreement.my", SentenceSeed(teaching.demo.nounId, teaching.demo.adjectiveId))) },
        modifier = Modifier.fillMaxWidth(),
    ) { Text("Тренировать смену владельца") }
}
