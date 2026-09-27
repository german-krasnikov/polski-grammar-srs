package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSelectElement
import polski.data.courseMatrixIntroduction
import polski.data.courseWebCaseCompositionHeader
import polski.data.courseContextHelp
import polski.data.courseMaleAccIntro
import polski.data.courseAspectNoPresent
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
import polski.presentation.AppUiState
import polski.presentation.CardPhase
import polski.presentation.MatrixSection
import polski.presentation.ChangeSide
import polski.presentation.ContrastPair

/** Browser-semantic matrix: real selectors and tables, with the same seven case rows as the shared engine. */
internal fun renderMatrixWeb(root: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
    root.className = "matrix-page"
    root.matrixAdd("h2", "Грамматическая матрица")
    root.matrixAdd("p", courseMatrixIntroduction)
    val nav = root.matrixAdd("div", cls = "subnav")
    nav.setAttribute("aria-label", "Разделы матрицы")
    listOf(
        MatrixSection.Map to "Карта системы",
        MatrixSection.Cases to "Падежи и окончания",
        MatrixSection.Verbs to "Времена и лица",
        MatrixSection.Pronouns to "Местоимения",
    ).forEach { (section, label) ->
        nav.matrixButton(label, "matrix-section-${section.name.lowercase()}") {
            dispatch(AppAction.SelectMatrixSection(section))
        }.apply {
            setAttribute("aria-pressed", (state.matrixSelection.section == section).toString())
            if (state.matrixSelection.section == section) className = "active"
        }
    }
    when (state.matrixSelection.section) {
        MatrixSection.Map -> renderMap(root, dispatch)
        MatrixSection.Cases -> renderCases(root, state, dispatch)
        MatrixSection.Verbs -> renderVerbs(root, state, dispatch)
        MatrixSection.Pronouns -> renderPronouns(root, dispatch)
    }
}

/**
 * The current card's seven forms remain visible without changing the exercise or matrix
 * selection. Emphasis contract §5: before reveal, the row that matches this exercise's own
 * target case is the answer, so it stays unhighlighted and its "Стало" form stays hidden; every
 * other row is unrelated reference material and shows normally.
 */
internal fun renderCaseReferenceWeb(root: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
    val exercise = state.exercise ?: return
    val revealed = state.phase == CardPhase.Revealed
    val noun = nounById(exercise.nounId)
    root.matrixAdd("h3", "Таблица этого предложения")
    root.matrixAdd("p", "Можно подсматривать · ${noun.lemma} · ${genderNames.getValue(noun.gender)} · ${if (exercise.number == NumberGram.SG) "ед. ч." else "мн. ч."}")
    val scroll = root.matrixAdd("div", cls = "table-scroll")
    scroll.setAttribute("data-scroll-key", "card-reference")
    val table = scroll.matrixAdd("table", cls = "compact-table")
    val header = table.matrixAdd("thead").matrixAdd("tr")
    header.matrixAdd("th", "Падеж").setAttribute("scope", "col")
    header.matrixAdd("th", "Вся группа слов").setAttribute("scope", "col")
    val body = table.matrixAdd("tbody")
    caseRows.forEach { row ->
        val tr = body.matrixAdd("tr")
        val isTarget = row.id.id in exercise.tags
        if (isTarget && revealed) tr.className = "highlight-row"
        tr.matrixAdd("th", "${row.pl} · ${row.ru}").setAttribute("scope", "row")
        tr.matrixAdd("td").apply {
            setAttribute("lang", "pl")
            val from = nounPhrase(exercise.nounId, GramCase.NOM, exercise.number, exercise.adjectiveId, exercise.possessive)
            if (isTarget && !revealed) {
                matrixContrastMasked(this, from)
            } else {
                matrixContrast(this, from, nounPhrase(exercise.nounId, row.id, exercise.number, exercise.adjectiveId, exercise.possessive))
            }
        }
    }
    root.matrixAdd("p", courseContextHelp.compact, "muted small")
    root.matrixButton("Все таблицы и схема") { dispatch(AppAction.SelectTab(polski.presentation.AppTab.Matrix)) }
}

private fun renderMap(root: HTMLElement, dispatch: (AppAction) -> Unit) {
    val pipeline = root.matrixSection(referencePipeline.title)
    pipeline.matrixAdd("p", referencePipeline.compactSummary)
    pipeline.matrixAdd("p", referencePipeline.compactExample)
    val system = pipeline.matrixAdd("div", cls = "system-grid")
    referenceSystemCards.forEach { (_, title, explanation, example) ->
        val article = system.matrixAdd("article")
        article.matrixAdd("h4", title)
        article.matrixAdd("p", explanation)
        article.matrixAdd("code", example)
    }

    val chain = root.matrixSection("Одна мысль, пять преобразований")
    chain.matrixTable(
        listOf("Операция", "Целое предложение", "Что изменилось"),
        referenceChainRows.map { listOf(it.label, it.to, it.change) },
        contrastFrom = { row, column -> if (column == 1) referenceChainRows[row].from else null },
    )
    chain.matrixButton("Тренировать эту цепочку") {
        dispatch(AppAction.ChooseSkill("chain", SentenceSeed("wife", "beautiful")))
    }

    val accusative = root.matrixSection("Мужской Biernik: дерево решений")
    accusative.matrixAdd("p", courseMaleAccIntro)
    val grid = accusative.matrixAdd("div", cls = "decision-grid")
    maleAccRows.forEach { row ->
        val article = grid.matrixAdd("article")
        article.matrixAdd("span", row.label)
        article.matrixAdd("h4", row.title)
        row.examples.forEach { example ->
            val paragraph = article.matrixAdd("p")
            paragraph.setAttribute("lang", "pl")
            matrixContrast(paragraph, example.from, example.to)
            paragraph.matrixAdd("br")
            paragraph.appendChild(document.createTextNode(example.sentence))
        }
        article.matrixAdd("small", row.rule)
    }
    accusative.matrixButton("Тренировать мужской род") {
        dispatch(AppAction.ChooseSkill("case.acc.m", SentenceSeed("friendM", "good")))
    }

    val support = root.matrixSection(referenceRussianSupport.fullTitle)
    support.matrixTable(
        referenceRussianSupport.columns,
        referenceRussianSupport.rows.map { row -> listOf(row.cue, row.web.construction, row.web.check) },
        renderCell = { rowIndex, column, cell ->
            if (column == 1) {
                val comparisons = cell.matrixAdd("div", cls = "support-comparisons")
                referenceRussianSupport.rows[rowIndex].comparisons.forEach { pair ->
                    matrixContrast(comparisons, pair)
                }
            }
        },
    )
}

private fun renderCases(root: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
    val selected = state.matrixSelection
    val section = root.matrixSection("Все семь падежей на одной группе слов")
    val controls = section.matrixAdd("div", cls = "table-controls")
    controls.matrixSelect("Эталонное слово", "matrix-noun", selected.nounId,
        nouns.map { it.id to "${it.lemma} — ${it.meaning}" }) {
        dispatch(AppAction.SetMatrixSelection(selected.copy(nounId = it)))
    }
    controls.matrixSelect("Прилагательное", "matrix-adjective", selected.adjectiveId,
        adjectives.map { it.id to "${it.lemma} — ${it.meaning}" }) {
        dispatch(AppAction.SetMatrixSelection(selected.copy(adjectiveId = it)))
    }
    controls.matrixSelect("Владелец", "matrix-owner", selected.ownerId,
        possessives.map { it.id.id to it.label }) {
        dispatch(AppAction.SetMatrixSelection(selected.copy(ownerId = it)))
    }
    controls.matrixSelect("Число", "matrix-number", selected.numberId,
        listOf("sg" to "Единственное", "pl" to "Множественное")) {
        dispatch(AppAction.SetMatrixSelection(selected.copy(numberId = it)))
    }
    val number = NumberGram.fromId(selected.numberId)
    val owner = PossessiveId.fromId(selected.ownerId)
    val seed = SentenceSeed(selected.nounId, selected.adjectiveId)
    section.matrixTable(
        listOf("Падеж · русская опора", "Вопрос / конструкция", courseWebCaseCompositionHeader, "Целое предложение"),
        caseRows.map { row ->
            listOf(
                "${row.pl} · ${row.ru}",
                "${row.question} · ${row.trigger}",
                nounPhrase(selected.nounId, row.id, number, selected.adjectiveId, owner),
                caseSentence(seed, row.id, owner, number),
            )
        },
        contrastFrom = { _, column -> when (column) {
            2 -> nounPhrase(selected.nounId, GramCase.NOM, number, selected.adjectiveId, owner)
            3 -> caseSentence(seed, GramCase.NOM, owner, number)
            else -> null
        } },
    )
    section.matrixAdd("p", referenceCaseTeaching.compactNote)
    section.matrixButton("Тренировать отрицание с этим словом") {
        dispatch(AppAction.ChooseSkill("case.gen.neg", seed))
    }

    val comparison = root.matrixSection("Сравнение типов склонения · ${if (number == NumberGram.SG) "единственное" else "множественное"} число")
    comparison.matrixAdd("p", referenceCaseTeaching.comparisonReadingHint)
    val comparisonIds = comparisonNounIds
    comparison.matrixTable(
        listOf("Падеж") + comparisonIds.map { id ->
            val noun = nounById(id)
            "${noun.lemma} · ${genderNames.getValue(noun.gender)}"
        },
        caseRows.map { row -> listOf(row.pl) + comparisonIds.map { nounById(it).forms.getValue(number).getValue(row.id) } },
        "comparison-table",
        contrastFrom = { row, column -> if (column > 0) nounById(comparisonIds[column - 1]).forms.getValue(number).getValue(GramCase.NOM) else null },
    )
}

private fun renderVerbs(root: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
    val selected = state.matrixSelection
    val section = root.matrixSection("Лицо × число × время")
    val controls = section.matrixAdd("div", cls = "table-controls")
    controls.matrixSelect("Глагол", "matrix-verb", selected.verbId,
        verbs.filter { it.aspect == Aspect.IMPERFECTIVE }.map { it.id to "${it.lemma} — ${it.meaning}" }) {
        dispatch(AppAction.SetMatrixSelection(selected.copy(verbId = it)))
    }
    val teaching = referenceVerbTeaching
    controls.matrixSelect(teaching.genderControlLabel.full, "matrix-gender", if (selected.feminineGroup) "f" else "m",
        teaching.genderOptions.map { it.id to it.label.full }) {
        dispatch(AppAction.SetMatrixSelection(selected.copy(feminineGroup = it == "f")))
    }
    section.matrixTable(
        listOf("Кто") + listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE).map { teaching.tenseLabels.getValue(it).full },
        teaching.subjects.map { subject ->
            listOf(subject.label.full) + listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE).map { tense ->
                verbForm(selected.verbId, tense, subject.person, subject.number, subject.gender(selected.feminineGroup))
            }
        },
        contrastFrom = { _, column -> if (column > 0) verbs.first { it.id == selected.verbId }.lemma else null },
    )
    section.matrixAdd("p", teaching.compactFutureExplanation)

    val sentence = root.matrixSection("Время меняется, предложение остаётся целым")
    sentence.matrixTable(listOf("Операция", "Предложение"),
        referenceTenseRows.map { listOf(it.label, it.to) },
        contrastFrom = { row, column -> if (column == 1) referenceTenseRows[row].from else null })
    sentence.matrixButton("Тренировать времена предложениями") {
        dispatch(AppAction.ChooseSkill("verb.past", SentenceSeed("wife", "beautiful")))
    }
    val aspect = root.matrixSection("Вид: процесс или результат")
    aspect.matrixTable(listOf("Смысл", "Настоящее", "Прошедшее", "Будущее"),
        referenceAspectRows.map { listOf(it.label, it.present ?: courseAspectNoPresent.compact, it.past, it.future) },
        contrastFrom = { row, column ->
            if (column > 0 && !(column == 1 && referenceAspectRows[row].present == null)) referenceAspectRows[row].from else null
        })
}

private fun renderPronouns(root: HTMLElement, dispatch: (AppAction) -> Unit) {
    val teaching = referencePronounTeaching
    val personal = root.matrixSection(teaching.personalTitle)
    personal.matrixAdd("p", teaching.compactIntro)
    personal.matrixTable(
        listOf("Кто") + teaching.contexts.map { "${it.cue.full} · ${it.caseName}" },
        teaching.pronounIds.map { id -> listOf(id) + teaching.contexts.map { it.value(id, personalPronouns.getValue(id)) } },
        contrastFrom = { row, column -> if (column > 0) teaching.pronounIds[row] else null },
    )
    personal.matrixAdd("p", teaching.webFooter)

    val owners = root.matrixSection(teaching.possessiveTitle)
    owners.matrixTable(
        listOf("Кому принадлежит") + teaching.demo.cases.map { it.web } + "Правило",
        possessives.map { possessive ->
            listOf(possessive.label) + teaching.demo.cases.map { teaching.demo.phrase(possessive.id, it.id) } +
                listOf(teaching.demo.rule(possessive.id))
        },
        contrastFrom = { row, column -> if (column in 1..3) {
            val id = possessives[row].id
            teaching.demo.phrase(id, GramCase.NOM)
        } else null },
    )
    owners.matrixButton("Тренировать смену владельца") {
        dispatch(AppAction.ChooseSkill("agreement.my", SentenceSeed(teaching.demo.nounId, teaching.demo.adjectiveId)))
    }
}

private fun HTMLElement.matrixSection(title: String): HTMLElement = matrixAdd("section", cls = "card matrix-section").apply {
    matrixAdd("h3", title)
}

private fun HTMLElement.matrixAdd(tag: String, text: String? = null, cls: String? = null): HTMLElement =
    (document.createElement(tag) as HTMLElement).also {
        if (text != null) it.textContent = text
        if (cls != null) it.className = cls
        appendChild(it)
    }

private fun HTMLElement.matrixButton(label: String, id: String? = null, action: () -> Unit): HTMLElement =
    matrixAdd("button", label).apply {
        if (id != null) this.id = id
        addEventListener("click", { action() })
    }

private fun HTMLElement.matrixSelect(
    label: String,
    id: String,
    selected: String,
    options: List<Pair<String, String>>,
    onChange: (String) -> Unit,
) {
    val wrapper = matrixAdd("label", label)
    wrapper.setAttribute("for", id)
    val select = wrapper.matrixAdd("select") as HTMLSelectElement
    select.id = id
    options.forEach { (value, title) ->
        select.matrixAdd("option", title).setAttribute("value", value)
    }
    select.value = selected
    select.addEventListener("change", { onChange(select.value) })
}

private fun HTMLElement.matrixTable(
    headers: List<String>, rows: List<List<String>>, tableClass: String = "",
    contrastFrom: ((row: Int, column: Int) -> String?)? = null,
    renderCell: ((row: Int, column: Int, cell: HTMLElement) -> Unit)? = null,
) {
    val scroll = matrixAdd("div", cls = "table-scroll")
    val table = scroll.matrixAdd("table", cls = tableClass)
    val headerRow = table.matrixAdd("thead").matrixAdd("tr")
    headers.forEach { title -> headerRow.matrixAdd("th", title).setAttribute("scope", "col") }
    val body = table.matrixAdd("tbody")
    rows.forEachIndexed { rowIndex, cells ->
        val row = body.matrixAdd("tr")
        cells.forEachIndexed { index, value ->
            val before = contrastFrom?.invoke(rowIndex, index)
            val cell = row.matrixAdd(if (index == 0) "th" else "td", if (before == null) value else null)
            if (index == 0) cell.setAttribute("scope", "row")
            else if (value.any { it in "ąćęłńóśźżĄĆĘŁŃÓŚŹŻ" }) cell.setAttribute("lang", "pl")
            if (before != null) matrixContrast(cell, before, value)
            renderCell?.invoke(rowIndex, index, cell)
        }
    }
}

private fun matrixContrast(cell: HTMLElement, before: String, after: String) =
    matrixContrast(cell, ContrastPair.generated(before, after))

/**
 * Emphasis contract §5 ("панелей «под рукой»: целевая строка не подсвечивается, её «стало»
 * скрыто"): this exercise's own target row before reveal. Only the already-known "Было" form
 * shows; the answer ("Стало") is withheld from visible text, `aria-label` and the screen-reader
 * span alike, so no host surface (visual, a11y tree or DOM text) leaks it ahead of reveal.
 */
private fun matrixContrastMasked(cell: HTMLElement, before: String) {
    val pair = cell.matrixAdd("span", cls = "form-contrast form-contrast-masked")
    pair.setAttribute("role", "group")
    pair.setAttribute("aria-label", "Было: $before. Ответ скрыт до проверки.")
    pair.matrixAdd("span", "Было:", "form-contrast-label").setAttribute("aria-hidden", "true")
    val old = pair.matrixAdd("span", before, "form-contrast-before")
    old.setAttribute("lang", "pl")
    old.setAttribute("aria-hidden", "true")
    pair.matrixAdd("span", "→", "form-contrast-arrow").setAttribute("aria-hidden", "true")
    pair.matrixAdd("span", "Стало: ?", "form-contrast-label").setAttribute("aria-hidden", "true")
}

private var contrastSemanticSerial = 0

private fun matrixContrast(cell: HTMLElement, comparison: ContrastPair) {
    val pair = cell.matrixAdd("span", cls = "form-contrast")
    val semanticId = "matrix-contrast-${contrastSemanticSerial++}"
    pair.setAttribute("role", "group")
    pair.setAttribute("aria-label", "Было: ${comparison.from}. Стало: ${comparison.to}")
    pair.setAttribute("aria-labelledby", semanticId)
    val semantic = pair.matrixAdd("span", cls = "contrast-sr-only")
    semantic.id = semanticId
    semantic.setAttribute("aria-hidden", "true")
    semantic.appendChild(document.createTextNode("Было: "))
    semantic.matrixAdd("span", comparison.from).setAttribute("lang", "pl")
    semantic.appendChild(document.createTextNode(". Стало: "))
    semantic.matrixAdd("span", comparison.to).setAttribute("lang", "pl")
    pair.matrixAdd("span", "Было:", "form-contrast-label").setAttribute("aria-hidden", "true")
    val old = pair.matrixAdd("span", cls = "form-contrast-before")
    old.setAttribute("lang", "pl")
    old.setAttribute("aria-hidden", "true")
    comparison.parts(ChangeSide.Before).forEach { part ->
        old.matrixAdd("span", part.text, if (part.isChanged) "change-before" else null)
    }
    pair.matrixAdd("span", "→", "form-contrast-arrow").setAttribute("aria-hidden", "true")
    pair.matrixAdd("span", "Стало:", "form-contrast-label").setAttribute("aria-hidden", "true")
    val next = pair.matrixAdd("strong", cls = "form-contrast-after")
    next.setAttribute("lang", "pl")
    next.setAttribute("aria-hidden", "true")
    comparison.parts(ChangeSide.After).forEach { part ->
        next.matrixAdd("span", part.text, if (part.isChanged) "change-after" else null)
    }
}
