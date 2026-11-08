package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import polski.presentation.UiCopy
import org.w3c.dom.HTMLInputElement
import polski.exam.QuestionType
import polski.exam.ExamAnswerStatus
import polski.exam.formatQuarterPoints
import polski.exam.grammar.*

internal class ExamGrammarController(examId: String, private val onBack: () -> Unit) {
    private val tests = examGrammarData.getValue(examId)
    private val instructionsByItemId = tests.flatMap { test ->
        test.items.map { item -> item.itemId to test.instruction }
    }.toMap()
    private val bankByItemId = tests.flatMap { test ->
        test.items.map { item -> item.itemId to test.bankTokens }
    }.toMap()
    private val selectedBankTokenByItemId = mutableMapOf<String, String>()
    private var state = run {
        val mergedBank = tests.flatMap { it.bankTokens }.mapIndexed { index, token -> token.copy(position = index + 1) }
        val merged = GrammarTest(examId, tests.firstOrNull()?.instruction ?: "", tests.flatMap { it.items }, mergedBank)
        GrammarExamState(test = merged)
    }
    private var root: HTMLElement? = null
    private var inputRef: HTMLInputElement? = null

    fun render(container: HTMLElement) { root = container; redraw() }

    private fun dispatch(action: GrammarExamAction) {
        val oldIndex = state.currentIndex
        state = reduceGrammarExam(state, action)
        if (state.submitted && state.result != null) { root?.let { it.innerHTML = ""; renderResult(it) }; return }
        if (state.currentIndex != oldIndex) { redraw(); return }
        // Same question — update without full redraw to keep focus
        updateInPlace()
    }

    private fun updateInPlace() {
        val c = root ?: return
        val card = grammarExamCardModel(state)
        // Update progress
        c.querySelector(".exam-progress-text")?.textContent = card.progress
        (c.querySelector(".exam-progress-fill") as? HTMLElement)?.style?.setProperty(
            "width", "${((state.currentIndex + 1).toDouble() / state.test.items.size * 100).toInt()}%"
        )
        // Update option buttons selection
        c.querySelectorAll(".exam-option").let { nodes ->
            for (i in 0 until nodes.length) {
                val btn = nodes.item(i) as? HTMLButtonElement ?: continue
                val bankId = btn.getAttribute("data-bank-token-id")
                val isSelected = if (bankId != null) selectedBankTokenByItemId[card.item.itemId] == bankId
                    else card.item.options.getOrNull(i)?.let { it == card.currentAnswer } == true
                btn.className = if (isSelected) "exam-option selected" else "exam-option"
            }
        }
    }

    private fun redraw() {
        val c = root ?: return; c.innerHTML = ""; inputRef = null
        if (state.submitted && state.result != null) renderResult(c) else renderQuestion(c)
    }

    private fun renderQuestion(c: HTMLElement) {
        val card = grammarExamCardModel(state)
        c.appendChild(node("h2", "exam-title", instructionsByItemId[card.item.itemId] ?: card.instruction))
        c.appendChild(node("div", "exam-progress").apply {
            appendChild(node("span", "exam-progress-text", card.progress))
            appendChild(node("div", "exam-progress-bar").apply {
                appendChild(node("div", "exam-progress-fill").apply {
                    style.setProperty("width", "${((state.currentIndex + 1).toDouble() / state.test.items.size * 100).toInt()}%")
                })
            })
        })
        c.appendChild(node("div", "exam-context").apply {
            setAttribute("lang", "pl")
            val parts = card.item.context.split("___")
            if (parts.size == 2) {
                appendChild(node("span", text = parts[0]))
                appendChild(node("span", "exam-gap", "___"))
                appendChild(node("span", text = parts[1]))
            } else textContent = card.item.context
        })
        card.item.questionTarget?.let { target ->
            c.appendChild(node("mark", "exam-question-target", target).apply { setAttribute("lang", "pl") })
        }
        when (card.item.questionType) {
        QuestionType.WordBank -> {
            val bank = bankByItemId.getValue(card.item.itemId)
            require(bank.isNotEmpty()) { "Missing bank for ${card.item.itemId}" }
            c.appendChild(node("div", "exam-options exam-word-bank").apply {
                bank.forEach { token ->
                    val occurrence = bank.count { it.position <= token.position && it.text == token.text }
                    appendChild((document.createElement("button") as HTMLButtonElement).apply {
                        type = "button"
                        className = if (selectedBankTokenByItemId[card.item.itemId] == token.tokenId) "exam-option selected" else "exam-option"
                        textContent = token.text
                        setAttribute("data-bank-token-id", token.tokenId)
                        setAttribute("aria-label", "${token.text}, $occurrence")
                        addEventListener("click", {
                            selectedBankTokenByItemId[card.item.itemId] = token.tokenId
                            dispatch(GrammarExamAction.Answer(card.item.itemId, token.text))
                        })
                    })
                }
            })
        }
        QuestionType.ChooseForm -> {
            require(card.item.options.isNotEmpty()) { "Missing options for ${card.item.itemId}" }
            c.appendChild(node("div", "exam-options").apply {
                card.item.options.forEachIndexed { i, opt ->
                    appendChild((document.createElement("button") as HTMLButtonElement).apply {
                        type = "button"
                        className = if (card.currentAnswer == opt) "exam-option selected" else "exam-option"
                        textContent = "${'A' + i}. $opt"
                        addEventListener("click", { dispatch(GrammarExamAction.Answer(card.item.itemId, opt)) })
                    })
                }
            })
        }
        QuestionType.FillGap, QuestionType.Transform, QuestionType.WordFormation, QuestionType.QuestionFormation -> {
            val input = (document.createElement("input") as HTMLInputElement).apply {
                type = "text"; id = "exam-grammar-input"; className = "exam-input"; placeholder = "Wpisz odpowiedź..."
                value = card.currentAnswer
                addEventListener("input", {
                    dispatch(GrammarExamAction.Answer(card.item.itemId, this.value))
                })
            }
            inputRef = input
            c.appendChild(node("div", "exam-input-wrap").apply { appendChild(input) })
        }
        else -> error("Unsupported grammar control ${card.item.questionType} for ${card.item.itemId}")
        }
        c.appendChild(node("div", "exam-nav").apply {
            if (!card.isFirst) appendChild(navBtn("← Назад") { dispatch(GrammarExamAction.Previous) })
            appendChild(node("span", "exam-nav-spacer"))
            if (!card.isLast) appendChild(navBtn(UiCopy.Next) { dispatch(GrammarExamAction.Next) })
            if (card.isLast) appendChild(navBtn(UiCopy.Finish, primary = true) { dispatch(GrammarExamAction.Submit) })
        })
    }

    private fun renderResult(c: HTMLElement) {
        val result = state.result ?: return
        c.appendChild(node("h2", "exam-title", "${UiCopy.Results} — ${UiCopy.Grammar}"))
        c.appendChild(node("div", "exam-score").apply {
            if (result.isFinal) {
                appendChild(node("div", "exam-score-big", "${result.percentage.toInt()}%"))
                appendChild(node("div", "exam-score-detail", "${formatQuarterPoints(result.earnedQuarterPoints)} / ${formatQuarterPoints(result.possibleQuarterPoints)} баллов"))
            } else {
                appendChild(node("div", "exam-score-big", "${UiCopy.ExamConfirmedPoints}: ${formatQuarterPoints(result.earnedQuarterPoints)} / ${formatQuarterPoints(result.possibleQuarterPoints)}"))
                appendChild(node("div", "exam-score-detail", "${UiCopy.ExamPendingPoints}: ${formatQuarterPoints(result.manualPendingQuarterPoints)}"))
            }
        })
        c.appendChild(node("div", "exam-review").apply {
            state.test.items.forEachIndexed { i, item ->
                val answer = state.answers[item.itemId].orEmpty()
                val evaluation = GrammarExamScorer.gradeInAttempt(state.test, item, state.answers)
                val status = when (evaluation.status) {
                    ExamAnswerStatus.ConfirmedFull -> "correct"
                    ExamAnswerStatus.ConfirmedPartial -> "partial"
                    ExamAnswerStatus.Wrong -> "wrong"
                    ExamAnswerStatus.PendingManual -> "pending"
                }
                appendChild(node("div", "exam-review-item $status").apply {
                    appendChild(node("span", "exam-review-num", "${i + 1}."))
                    appendChild(node("span", "exam-review-context", item.context.replace("___", if (answer.isNotEmpty()) "[$answer]" else "[—]")))
                    if (evaluation.status == ExamAnswerStatus.ConfirmedPartial) appendChild(node("span", "exam-review-correct", "+${formatQuarterPoints(evaluation.confirmedQuarter)} / ${formatQuarterPoints(item.maximumQuarter)}"))
                    if (evaluation.status == ExamAnswerStatus.PendingManual) appendChild(node("span", "exam-review-manual", UiCopy.ExamManualReview))
                    if (evaluation.status != ExamAnswerStatus.ConfirmedFull) {
                        val examples = (listOf(item.correctAnswer) + item.scoredVariants.map { it.answer }).joinToString(" / ")
                        appendChild(node("span", "exam-review-correct", "${UiCopy.ExamOfficialExamples}: $examples"))
                    }
                })
            }
        })
        c.appendChild(node("div", "exam-nav").apply {
            appendChild(navBtn("← К модулям") { onBack() })
            appendChild(node("span", "exam-nav-spacer"))
            appendChild(navBtn(UiCopy.TryAgain, primary = true) {
                state = GrammarExamState(test = state.test); selectedBankTokenByItemId.clear(); inputRef = null; redraw()
            })
        })
    }
}

private fun navBtn(label: String, primary: Boolean = false, onClick: () -> Unit) =
    (document.createElement("button") as HTMLButtonElement).apply {
        type = "button"; className = if (primary) "exam-nav-btn primary" else "exam-nav-btn"
        textContent = label; addEventListener("click", { onClick() })
    }
