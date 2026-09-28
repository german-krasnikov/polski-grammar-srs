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
import polski.presentation.toViewModel
import polski.core.engine.MatrixColumn
import polski.core.engine.MatrixTableEngine
import polski.core.enMorphology
import polski.data.enPersonalPronouns
import polski.data.enVerbs
import polski.model.NumberFeature
import polski.model.Person
import polski.model.PersonFeature
import polski.model.TenseFeature
import polski.model.toFeatureValue

@Composable
internal fun AndroidMatrixScreen(state: AppUiState, dispatch: (AppAction) -> Unit, reduceMotion: Boolean = false) {
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
                // EnRuPackPlan.md §4.3/ADR-46: a sub-section here, not a new top-level `AppTab` —
                // the listing (StaticPackLifehackProvider.listAll) is Matrix reference material,
                // same status as Cases/Verbs/Pronouns, not its own destination.
                MatrixSection.Lifehacks.name to "Лайфхаки",
            ),
        ) { dispatch(AppAction.SelectMatrixSection(MatrixSection.valueOf(it))) }
        when (state.matrixSelection.section) {
            MatrixSection.Map -> AndroidMapSection(dispatch)
            MatrixSection.Cases -> AndroidCasesSection(state, dispatch)
            MatrixSection.Verbs -> AndroidVerbsSection(state, dispatch)
            MatrixSection.Pronouns -> AndroidPronounsSection(dispatch)
            MatrixSection.Lifehacks -> AndroidLifehacksSection(reduceMotion)
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

// EnRuAcceptance-2026-09-28.md §7 (Android lane, live repro on emulator-5554): `nouns`
// (packRegistry.active.nouns) is honestly empty for a caseless/genderless pack like en-ru — same
// reasoning as [AndroidVerbsSection]'s `verbs.isEmpty()` guard and MatrixWeb.kt's `hasCaseSystem`
// gate. This function used to build the whole pl declension grid unconditionally, so with en-ru
// active it crashed with `IllegalStateException` ("Unknown noun wife") inside
// `nounPhrase` -> `nounById` the moment the screen composed. Gate the pl-declension portion
// behind `nouns.isNotEmpty()`, same pattern and same calm-notice text as the verbs fix.
/** Public (not `private`), like [AndroidCaseReference]/[AndroidSystemMapCard], so UC-09's table
 *  parity can be exercised directly in Compose tests from `androidApp`. */
@Composable
fun AndroidCasesSection(state: AppUiState, dispatch: (AppAction) -> Unit) {
    if (nouns.isEmpty()) {
        AndroidNoCaseSystemNotice()
        return
    }
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
    // UC-09 part 2/2: this table's cells now come from MatrixTableEngine (rowAxis = caseRows)
    // instead of calling nounPhrase/caseSentence inline per card — same values, one engine.
    val casesTable = MatrixTableEngine.build(
        rowAxis = caseRows,
        rowHeaderLabel = "Падеж · русская опора",
        rowHeader = { row -> "${row.pl} · ${row.ru}" },
        columns = listOf(
            MatrixColumn("Вопрос / конструкция", { row -> "${row.question} · ${row.trigger}" }),
            MatrixColumn("Фраза", { row -> nounPhrase(selected.nounId, row.id, number, selected.adjectiveId, owner) },
                contrastFrom = { basePhrase }),
            MatrixColumn("Предложение", { row -> caseSentence(seed, row.id, owner, number) }, contrastFrom = { baseSentence }),
        ),
    ).toViewModel()
    casesTable.rows.forEach { row ->
        AndroidInfoCard(row.header) {
            Text(row.cells[0].value, style = MaterialTheme.typography.labelLarge)
            ContrastPairText(requireNotNull(row.cells[1].contrast))
            ContrastPairText(requireNotNull(row.cells[2].contrast))
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
        val comparisonTable = MatrixTableEngine.build(
            rowAxis = comparisonNounIds,
            rowHeaderLabel = "Слово",
            rowHeader = { id -> nounById(id).lemma },
            columns = listOf(
                MatrixColumn(comparisonCase.name, { id -> nounById(id).forms.getValue(number).getValue(comparisonCase) },
                    contrastFrom = { id -> nounById(id).forms.getValue(number).getValue(GramCase.NOM) }),
            ),
        ).toViewModel()
        comparisonTable.rows.forEach { row ->
            Text(row.header)
            ContrastPairText(requireNotNull(row.cells[0].contrast))
        }
    }
}

// EnRuAcceptance-2026-09-28.md §7 (Android lane, live repro on emulator-5554): `verbs`
// (packRegistry.active.verbs) is honestly empty for a caseless/aspect-less pack like en-ru — same
// reasoning as MatrixWeb.kt's `renderVerbs` (`verbs.isEmpty() -> renderNoCaseSystemNotice`). This
// function used to build the whole pl aspect/tense grid unconditionally, so with en-ru active it
// crashed with `NoSuchElementException` inside `verbs.first { it.id == selected.verbId }` the
// moment the screen composed. Gate the pl-conjugation portion behind `verbs.isNotEmpty()`, same
// pattern and same calm-notice text as the web fix, and fall through to it instead of crashing.
// `AndroidEnglishVerbMatrix` stays unconditional — EN-24 already reads its own pack-independent
// data (`lang/en/forms.generated.json`) and works regardless of which pack is active.
/** Public — see [AndroidCasesSection]. */
@Composable
fun AndroidVerbsSection(state: AppUiState, dispatch: (AppAction) -> Unit) {
    val selected = state.matrixSelection
    if (verbs.isEmpty()) {
        AndroidNoCaseSystemNotice()
    } else {
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
        val lemma = verbs.first { it.id == selected.verbId }.lemma
        // UC-09 part 2/2: rowAxis = subjects, one column per tense — same shape as MatrixTableEngineTest.
        val verbsTable = MatrixTableEngine.build(
            rowAxis = referenceVerbTeaching.subjects,
            rowHeaderLabel = "Кто",
            rowHeader = { subject -> subject.label.compact },
            columns = listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE).map { tense ->
                MatrixColumn(referenceVerbTeaching.tenseLabels.getValue(tense).compact,
                    { subject -> verbForm(selected.verbId, tense, subject.person, subject.number, subject.gender(selected.feminineGroup)) },
                    contrastFrom = { lemma })
            },
        ).toViewModel()
        verbsTable.rows.forEach { row ->
            AndroidInfoCard(row.header) {
                row.cells.forEachIndexed { i, cell ->
                    Text(verbsTable.columnHeaders[i])
                    ContrastPairText(requireNotNull(cell.contrast))
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
    AndroidEnglishVerbMatrix()
}

/**
 * Same calm-placeholder text as `MatrixWeb.kt`'s `renderNoCaseSystemNotice` (EnRuAcceptance-2026-09-28.md
 * §7 item 3): a caseless/aspect-less pack like en-ru genuinely has no pl declension/conjugation
 * data (ADR-36 — [verbs]/[polski.data.nouns] are honestly empty, not fabricated), so this is a
 * calm notice instead of a crash, not a full en-ru-specific reference screen (separate, unstarted
 * work).
 */
@Composable
private fun AndroidNoCaseSystemNotice() {
    Text(
        "Для текущего курса эта таблица недоступна: в этом языке нет падежей/рода. Открой «Времена и лица» — таблица глаголов и do-support работает для любого курса.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * EN-24 android (UC-09 part 2/2 minimum, Plans/Kotlin/EnRuPackPlan.md §5 gap H / §6): the same live
 * English matrix as the web slice (ADR-28) — Present/Past/Future × person for one example verb,
 * plus a do-support table for the same persons — read from `lang/en/forms.generated.json` through
 * [enMorphology] and the exact same [MatrixTableEngine]/`MatrixTableViewModel` every pl table above
 * already uses, not a new ad hoc rendering path. Fixed to one example verb (no selector): a minimum
 * slice proving the engine is language-agnostic on this host too, not a full English matrix UI.
 */
@Composable
private fun AndroidEnglishVerbMatrix() {
    val exampleVerbId = "see"
    val exampleLemma = enVerbs.first { it.id == exampleVerbId }.lemma
    val tenseLabel = mapOf(Tense.PRESENT to "Настоящее", Tense.PAST to "Прошедшее", Tense.FUTURE to "Будущее")
    AndroidInfoCard("English: лицо × время (\"$exampleLemma\")") {
        Text("Формы читаются из forms.generated.json(en) тем же MatrixTableViewModel, что и польские таблицы выше — движок не знает, что это английский.")
    }
    val verbsTable = MatrixTableEngine.build(
        rowAxis = enPersonalPronouns,
        rowHeaderLabel = "Кто",
        rowHeader = { it.subject },
        columns = Tense.entries.map { tense ->
            MatrixColumn(tenseLabel.getValue(tense), { pronoun -> enVerbForm(exampleVerbId, tense, pronoun.id) }, contrastFrom = { exampleLemma })
        },
    ).toViewModel()
    verbsTable.rows.forEach { row ->
        AndroidInfoCard(row.header) {
            row.cells.forEachIndexed { i, cell ->
                Text(verbsTable.columnHeaders[i])
                ContrastPairText(requireNotNull(cell.contrast))
            }
        }
    }

    AndroidInfoCard("do-support: вопрос и отрицание") {
        Text("«do/does/did» встаёт перед подлежащим (Do you see…?) или перед «not» (I do not see…). У будущего своего do-support нет — вопрос и отрицание строятся через «will» само по себе.")
    }
    val doSupportTable = MatrixTableEngine.build(
        rowAxis = enPersonalPronouns,
        rowHeaderLabel = "Кто",
        rowHeader = { it.subject },
        columns = listOf(
            MatrixColumn(tenseLabel.getValue(Tense.PRESENT), { pronoun -> enVerbForm("do", Tense.PRESENT, pronoun.id) }, contrastFrom = { "do" }),
            MatrixColumn(tenseLabel.getValue(Tense.PAST), { pronoun -> enVerbForm("do", Tense.PAST, pronoun.id) }, contrastFrom = { "do" }),
            MatrixColumn(tenseLabel.getValue(Tense.FUTURE), { "не нужен — только will" }),
        ),
    ).toViewModel()
    doSupportTable.rows.forEach { row ->
        AndroidInfoCard(row.header) {
            row.cells.forEachIndexed { i, cell ->
                Text(doSupportTable.columnHeaders[i])
                val contrast = cell.contrast
                if (contrast != null) ContrastPairText(contrast) else Text(cell.value)
            }
        }
    }
}

private val enPersonNumberByPronounId = mapOf(
    "I" to (Person.FIRST to NumberGram.SG),
    "you" to (Person.SECOND to NumberGram.SG),
    "he" to (Person.THIRD to NumberGram.SG),
    "she" to (Person.THIRD to NumberGram.SG),
    "it" to (Person.THIRD to NumberGram.SG),
    "we" to (Person.FIRST to NumberGram.PL),
    "they" to (Person.THIRD to NumberGram.PL),
)

private fun enVerbForm(verbId: String, tense: Tense, pronounId: String): String {
    val (person, number) = enPersonNumberByPronounId.getValue(pronounId)
    return enMorphology.form(
        "verb:$verbId",
        mapOf(TenseFeature to tense.toFeatureValue(), PersonFeature to person.toFeatureValue(), NumberFeature to number.toFeatureValue()),
    )
}

/** Public — see [AndroidCasesSection]. */
@Composable
fun AndroidPronounsSection(dispatch: (AppAction) -> Unit) {
    val teaching = referencePronounTeaching
    Text(teaching.compactIntro)
    // UC-09 part 2/2: rowAxis = pronoun ids, one column per teaching context.
    val personalTable = MatrixTableEngine.build(
        rowAxis = teaching.pronounIds,
        rowHeaderLabel = "Кто",
        rowHeader = { id -> id },
        columns = teaching.contexts.map { context ->
            MatrixColumn(context.cue.compact, { id ->
                val forms = personalPronouns.getValue(id)
                if (context.id == GramCase.LOC) forms.getValue(GramCase.LOC) else context.value(id, forms)
            }, contrastFrom = { id -> id })
        },
    ).toViewModel()
    personalTable.rows.forEach { row ->
        AndroidInfoCard(row.header) {
            row.cells.forEachIndexed { i, cell ->
                Text(personalTable.columnHeaders[i])
                ContrastPairText(requireNotNull(cell.contrast))
            }
        }
    }
    Text(teaching.nativeFooter)
    Text(teaching.possessiveTitle, style = MaterialTheme.typography.titleLarge)
    // UC-09 part 2/2: rowAxis = possessives, one column per demo case.
    val possessiveTable = MatrixTableEngine.build(
        rowAxis = possessives,
        rowHeaderLabel = "Кому принадлежит",
        rowHeader = { possessive -> possessive.label },
        columns = teaching.demo.cases.map { demoCase ->
            MatrixColumn(demoCase.id.name, { possessive -> teaching.demo.phrase(possessive.id, demoCase.id) },
                contrastFrom = { possessive -> teaching.demo.phrase(possessive.id, GramCase.NOM) })
        },
    ).toViewModel()
    possessiveTable.rows.forEachIndexed { rowIndex, row ->
        AndroidInfoCard(row.header) {
            row.cells.forEachIndexed { i, cell ->
                Text(possessiveTable.columnHeaders[i])
                ContrastPairText(requireNotNull(cell.contrast))
            }
            Text(teaching.demo.rule(possessives[rowIndex].id))
        }
    }
    OutlinedButton(
        onClick = { dispatch(AppAction.ChooseSkill("agreement.my", SentenceSeed(teaching.demo.nounId, teaching.demo.adjectiveId))) },
        modifier = Modifier.fillMaxWidth(),
    ) { Text("Тренировать смену владельца") }
}
