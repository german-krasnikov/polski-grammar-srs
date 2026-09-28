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
import polski.core.engine.MatrixColumn
import polski.core.engine.MatrixTableEngine
import polski.data.enPersonalPronouns
import polski.data.enVerbForm
import polski.data.enVerbs
import polski.model.Aspect
import polski.model.Gender
import polski.model.GramCase
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.model.SentenceSeed
import polski.model.Tense
import polski.presentation.AppAction
import polski.presentation.AppUiState
import polski.presentation.CardPhase
import polski.presentation.MatrixSection
import polski.presentation.ChangeSide
import polski.presentation.ContrastPair
import polski.presentation.MatrixTableCell
import polski.presentation.MatrixTableViewModel
import polski.presentation.toViewModel

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
    referenceSystemCards.forEach { (_, title, explanation, _, steps) ->
        val article = system.matrixAdd("article")
        article.matrixAdd("h4", title)
        article.matrixAdd("p", explanation)
        renderSystemCardSteps(article.matrixAdd("code").apply { setAttribute("lang", "pl") }, steps)
    }

    val chain = root.matrixSection("Одна мысль, пять преобразований")
    chain.matrixTable(
        MatrixTableEngine.build(
            rowAxis = referenceChainRows,
            rowHeaderLabel = "Операция",
            rowHeader = { it.label },
            columns = listOf(
                MatrixColumn("Целое предложение", { it.to }, contrastFrom = { it.from }),
                MatrixColumn("Что изменилось", { it.change }),
            ),
        ).toViewModel(),
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
        MatrixTableEngine.build(
            rowAxis = referenceRussianSupport.rows,
            rowHeaderLabel = referenceRussianSupport.columns[0],
            rowHeader = { it.cue },
            columns = listOf(
                MatrixColumn(referenceRussianSupport.columns[1], { it.web.construction }),
                MatrixColumn(referenceRussianSupport.columns[2], { it.web.check }),
            ),
        ).toViewModel(),
        renderCell = { rowIndex, column, cell ->
            if (column == 0) {
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
        MatrixTableEngine.build(
            rowAxis = caseRows,
            rowHeaderLabel = "Падеж · русская опора",
            rowHeader = { "${it.pl} · ${it.ru}" },
            columns = listOf(
                MatrixColumn("Вопрос / конструкция", { "${it.question} · ${it.trigger}" }),
                MatrixColumn(
                    courseWebCaseCompositionHeader,
                    { nounPhrase(selected.nounId, it.id, number, selected.adjectiveId, owner) },
                    contrastFrom = { nounPhrase(selected.nounId, GramCase.NOM, number, selected.adjectiveId, owner) },
                ),
                MatrixColumn(
                    "Целое предложение",
                    { caseSentence(seed, it.id, owner, number) },
                    contrastFrom = { caseSentence(seed, GramCase.NOM, owner, number) },
                ),
            ),
        ).toViewModel(),
    )
    section.matrixAdd("p", referenceCaseTeaching.compactNote)
    section.matrixButton("Тренировать отрицание с этим словом") {
        dispatch(AppAction.ChooseSkill("case.gen.neg", seed))
    }

    val comparison = root.matrixSection("Сравнение типов склонения · ${if (number == NumberGram.SG) "единственное" else "множественное"} число")
    comparison.matrixAdd("p", referenceCaseTeaching.comparisonReadingHint)
    val comparisonIds = comparisonNounIds
    comparison.matrixTable(
        MatrixTableEngine.build(
            rowAxis = caseRows,
            rowHeaderLabel = "Падеж",
            rowHeader = { it.pl },
            columns = comparisonIds.map { id ->
                val noun = nounById(id)
                MatrixColumn(
                    "${noun.lemma} · ${genderNames.getValue(noun.gender)}",
                    { row -> nounById(id).forms.getValue(number).getValue(row.id) },
                    contrastFrom = { nounById(id).forms.getValue(number).getValue(GramCase.NOM) },
                )
            },
        ).toViewModel(),
        "comparison-table",
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
        MatrixTableEngine.build(
            rowAxis = teaching.subjects,
            rowHeaderLabel = "Кто",
            rowHeader = { it.label.full },
            columns = listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE).map { tense ->
                MatrixColumn(
                    teaching.tenseLabels.getValue(tense).full,
                    { subject -> verbForm(selected.verbId, tense, subject.person, subject.number, subject.gender(selected.feminineGroup)) },
                    contrastFrom = { verbs.first { it.id == selected.verbId }.lemma },
                )
            },
        ).toViewModel(),
    )
    section.matrixAdd("p", teaching.compactFutureExplanation)

    val sentence = root.matrixSection("Время меняется, предложение остаётся целым")
    sentence.matrixTable(
        MatrixTableEngine.build(
            rowAxis = referenceTenseRows,
            rowHeaderLabel = "Операция",
            rowHeader = { it.label },
            columns = listOf(MatrixColumn("Предложение", { it.to }, contrastFrom = { it.from })),
        ).toViewModel(),
    )
    sentence.matrixButton("Тренировать времена предложениями") {
        dispatch(AppAction.ChooseSkill("verb.past", SentenceSeed("wife", "beautiful")))
    }
    val aspect = root.matrixSection("Вид: процесс или результат")
    aspect.matrixTable(
        MatrixTableEngine.build(
            rowAxis = referenceAspectRows,
            rowHeaderLabel = "Смысл",
            rowHeader = { it.label },
            columns = listOf(
                MatrixColumn(
                    "Настоящее",
                    { it.present ?: courseAspectNoPresent.compact },
                    contrastFrom = { row -> if (row.present == null) null else row.from },
                ),
                MatrixColumn("Прошедшее", { it.past }, contrastFrom = { it.from }),
                MatrixColumn("Будущее", { it.future }, contrastFrom = { it.from }),
            ),
        ).toViewModel(),
    )
    renderEnglishVerbMatrix(root)
}

/**
 * EN-24 (UC-09 part 2/2 minimum, Plans/Kotlin/EnRuPackPlan.md §5 gap H / §6): the one live English
 * matrix table — Present/Past/Future × person, plus a do-support table for the same persons — read
 * from `lang/en/forms.generated.json` through [polski.data.enVerbForm] (a shared `:shared`
 * commonMain lookup — EN-24 ios lane promoted it out of this file once the iOS matrix host needed
 * the identical form, so both hosts read one implementation) and the exact same
 * [MatrixTableEngine]/[MatrixTableViewModel] every pl table above already uses, not a new ad hoc
 * rendering path. Fixed to one example verb (no selector): a minimum slice proving the engine is
 * language-agnostic, not a full English matrix UI (that is the same follow-up as the other 3
 * hosts, out of this task's scope).
 */
private fun renderEnglishVerbMatrix(root: HTMLElement) {
    val exampleVerbId = "see"
    val exampleLemma = enVerbs.first { it.id == exampleVerbId }.lemma
    val tenseLabel = mapOf(Tense.PRESENT to "Настоящее", Tense.PAST to "Прошедшее", Tense.FUTURE to "Будущее")

    val section = root.matrixSection("English: лицо × время (\"$exampleLemma\")")
    section.matrixAdd("p", "Формы читаются из forms.generated.json(en) тем же MatrixTableViewModel, что и польские таблицы выше — движок не знает, что это английский.")
    section.matrixTable(
        MatrixTableEngine.build(
            rowAxis = enPersonalPronouns,
            rowHeaderLabel = "Кто",
            rowHeader = { it.subject },
            columns = Tense.entries.map { tense ->
                MatrixColumn(tenseLabel.getValue(tense), { pronoun -> enVerbForm(exampleVerbId, tense, pronoun.id) }, contrastFrom = { exampleLemma })
            },
        ).toViewModel(),
        lang = "en",
    )

    val doSupport = root.matrixSection("do-support: вопрос и отрицание")
    doSupport.matrixAdd("p", "«do/does/did» встаёт перед подлежащим (Do you see…?) или перед «not» (I do not see…). У будущего своего do-support нет — вопрос и отрицание строятся через «will» само по себе.")
    doSupport.matrixTable(
        MatrixTableEngine.build(
            rowAxis = enPersonalPronouns,
            rowHeaderLabel = "Кто",
            rowHeader = { it.subject },
            columns = listOf(
                MatrixColumn(tenseLabel.getValue(Tense.PRESENT), { pronoun -> enVerbForm("do", Tense.PRESENT, pronoun.id) }, contrastFrom = { "do" }),
                MatrixColumn(tenseLabel.getValue(Tense.PAST), { pronoun -> enVerbForm("do", Tense.PAST, pronoun.id) }, contrastFrom = { "do" }),
                MatrixColumn(tenseLabel.getValue(Tense.FUTURE), { "не нужен — только will" }),
            ),
        ).toViewModel(),
        lang = "en",
    )
}

private fun renderPronouns(root: HTMLElement, dispatch: (AppAction) -> Unit) {
    val teaching = referencePronounTeaching
    val personal = root.matrixSection(teaching.personalTitle)
    personal.matrixAdd("p", teaching.compactIntro)
    personal.matrixTable(
        MatrixTableEngine.build(
            rowAxis = teaching.pronounIds,
            rowHeaderLabel = "Кто",
            rowHeader = { it },
            columns = teaching.contexts.map { context ->
                MatrixColumn(
                    "${context.cue.full} · ${context.caseName}",
                    { id -> context.value(id, personalPronouns.getValue(id)) },
                    contrastFrom = { id -> id },
                )
            },
        ).toViewModel(),
    )
    personal.matrixAdd("p", teaching.webFooter)

    val owners = root.matrixSection(teaching.possessiveTitle)
    owners.matrixTable(
        MatrixTableEngine.build(
            rowAxis = possessives,
            rowHeaderLabel = "Кому принадлежит",
            rowHeader = { it.label },
            columns = teaching.demo.cases.map { case ->
                MatrixColumn<polski.data.Possessive>(case.web, { p -> teaching.demo.phrase(p.id, case.id) }, contrastFrom = { p -> teaching.demo.phrase(p.id, GramCase.NOM) })
            } + MatrixColumn<polski.data.Possessive>(
                "Правило",
                { p -> teaching.demo.rule(p.id) },
            ),
        ).toViewModel(),
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

/**
 * UniversalCorePlan.md §5.3.3 UC-09: renders any [MatrixTableViewModel] — this is the one table
 * renderer the whole matrix page shares. Every section builds its row/column shape and highlight
 * base with `:core-engine`'s `MatrixTableEngine` (language/pack-agnostic) and attaches contrast
 * with `toViewModel()` (ContrastHighlightPlan.md §4); this function only turns the already-resolved
 * [MatrixTableCell]s into DOM, the same as it always did for hand-built header/row lists.
 */
private fun HTMLElement.matrixTable(
    vm: MatrixTableViewModel, tableClass: String = "", lang: String = "pl",
    renderCell: ((row: Int, column: Int, cell: HTMLElement) -> Unit)? = null,
) {
    val scroll = matrixAdd("div", cls = "table-scroll")
    val table = scroll.matrixAdd("table", cls = tableClass)
    val headerRow = table.matrixAdd("thead").matrixAdd("tr")
    headerRow.matrixAdd("th", vm.rowHeaderLabel).setAttribute("scope", "col")
    vm.columnHeaders.forEach { title -> headerRow.matrixAdd("th", title).setAttribute("scope", "col") }
    val body = table.matrixAdd("tbody")
    vm.rows.forEachIndexed { rowIndex, row ->
        val tr = body.matrixAdd("tr")
        tr.matrixAdd("th", row.header).setAttribute("scope", "row")
        row.cells.forEachIndexed { colIndex, cellVm ->
            val contrast = cellVm.contrast
            val cell = tr.matrixAdd("td", if (contrast == null) cellVm.value else null)
            // en's cells are plain ASCII (no diacritic to detect by), so a non-pl table is tagged
            // unconditionally; pl's own detection (only pl-diacritic cells get tagged, e.g. not a
            // bare gloss/number cell) stays exactly as before.
            if (lang != "pl" || cellVm.value.any { it in "ąćęłńóśźżĄĆĘŁŃÓŚŹŻ" }) cell.setAttribute("lang", lang)
            if (contrast != null) matrixContrast(cell, contrast, lang)
            renderCell?.invoke(rowIndex, colIndex, cell)
        }
    }
}

private fun matrixContrast(cell: HTMLElement, before: String, after: String) =
    matrixContrast(cell, ContrastPair.generated(before, after))

/**
 * EmphasisUXAudit E6/C2, contract §4 ("стрелки карты системы: в данных цепочка шагов, а не
 * строка"): [steps] is the pack's own explicit chain (never re-parsed from the joined arrow
 * string). The first step is the chain's origin and stays plain; every later step highlights only
 * its own change from the step right before it, with the same `change-after` token every other
 * "Стало" surface already uses — no second, simplified highlight path for this one surface.
 */
private fun renderSystemCardSteps(code: HTMLElement, steps: List<String>) {
    code.matrixAdd("span", steps.first())
    steps.zipWithNext().forEach { (from, to) ->
        code.appendChild(document.createTextNode(" → "))
        val after = code.matrixAdd("span")
        ContrastPair.generated(from, to).parts(ChangeSide.After).forEach { part ->
            after.matrixAdd("span", part.text, if (part.isChanged) "change-after" else null)
        }
    }
}

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

private fun matrixContrast(cell: HTMLElement, comparison: ContrastPair, lang: String = "pl") {
    val pair = cell.matrixAdd("span", cls = "form-contrast")
    val semanticId = "matrix-contrast-${contrastSemanticSerial++}"
    pair.setAttribute("role", "group")
    pair.setAttribute("aria-label", "Было: ${comparison.from}. Стало: ${comparison.to}")
    pair.setAttribute("aria-labelledby", semanticId)
    val semantic = pair.matrixAdd("span", cls = "contrast-sr-only")
    semantic.id = semanticId
    semantic.setAttribute("aria-hidden", "true")
    semantic.appendChild(document.createTextNode("Было: "))
    semantic.matrixAdd("span", comparison.from).setAttribute("lang", lang)
    semantic.appendChild(document.createTextNode(". Стало: "))
    semantic.matrixAdd("span", comparison.to).setAttribute("lang", lang)
    pair.matrixAdd("span", "Было:", "form-contrast-label").setAttribute("aria-hidden", "true")
    val old = pair.matrixAdd("span", cls = "form-contrast-before")
    old.setAttribute("lang", lang)
    old.setAttribute("aria-hidden", "true")
    comparison.parts(ChangeSide.Before).forEach { part ->
        old.matrixAdd("span", part.text, if (part.isChanged) "change-before" else null)
    }
    pair.matrixAdd("span", "→", "form-contrast-arrow").setAttribute("aria-hidden", "true")
    pair.matrixAdd("span", "Стало:", "form-contrast-label").setAttribute("aria-hidden", "true")
    val next = pair.matrixAdd("strong", cls = "form-contrast-after")
    next.setAttribute("lang", lang)
    next.setAttribute("aria-hidden", "true")
    comparison.parts(ChangeSide.After).forEach { part ->
        next.matrixAdd("span", part.text, if (part.isChanged) "change-after" else null)
    }
}
