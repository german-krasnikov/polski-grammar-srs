package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import polski.exam.QuestionType
import polski.exam.grammar.*

private val sampleTest = GrammarTest(
    testId = "B1-sample-grammar",
    instruction = "Poprawność gramatyczna — B1 Certyfikat Polski (test przykładowy)",
    items = listOf(
        GrammarItem("q26", QuestionType.ChooseForm, "'Nasza planeta' to najnowsza produkcja Netfliksa, która zabierze ___ w wyjątkową podróż", null, listOf("nas", "nami", "ich"), "nas", 1),
        GrammarItem("q27", QuestionType.ChooseForm, "w wyjątkową podróż ___ najdalszych i zapierających dech w piersiach miejsc na Ziemi", null, listOf("w", "po", "do"), "do", 1),
        GrammarItem("q28", QuestionType.ChooseForm, "Ten serial to nie tylko przyjemna podróż dookoła świata, ___ także ostrzeżenie", null, listOf("i", "lub", "ale"), "ale", 1),
        GrammarItem("q29", QuestionType.ChooseForm, "jak zmiany klimatyczne wpływają na ___ na naszej planecie", null, listOf("życiu", "życie", "życiem"), "życie", 1),
        GrammarItem("q30", QuestionType.ChooseForm, "W produkcji 'Nasza planeta' ___, dzięki najnowszej technice, po raz pierwszy podziwiać wspaniałe zdjęcia", null, listOf("mogą", "możecie", "może"), "możecie", 1),
        GrammarItem("q31", QuestionType.ChooseForm, "dzięki ___ technice, po raz pierwszy podziwiać wspaniałe zdjęcia", null, listOf("najnowszej", "nowej", "nowszej"), "najnowszej", 1),
        GrammarItem("q32", QuestionType.ChooseForm, "podziwiać wspaniałe zdjęcia nieznanych do tej pory ___ Ziemi", null, listOf("zakątki", "zakątkami", "zakątków"), "zakątków", 1),
        GrammarItem("q33", QuestionType.ChooseForm, "ponad 600 członków ekipy wspólnie ___ materiał", null, listOf("nagrali", "nagrany", "nagrało"), "nagrało", 1),
        GrammarItem("q34", QuestionType.ChooseForm, "Serial będzie poświęcony ___ różnorodności ekosystemów", null, listOf("niezwykłej", "niezwykłemu", "niezwykłą"), "niezwykłej", 1),
        GrammarItem("q35", QuestionType.ChooseForm, "aż po rozległe ___ Afryki", null, listOf("krajobrazów", "krajobrazy", "krajobrazami"), "krajobrazy", 1),
        GrammarItem("q36", QuestionType.FillGap, "tablety zastępują spacer, a smartfony są pomocne rodzicom w układaniu ___ do snu", null, emptyList(), "dzieci", 2),
        GrammarItem("q37", QuestionType.FillGap, "skutki tych popularnych 'metod wychowawczych' ___ być fatalne", null, emptyList(), "mogą", 2),
        GrammarItem("q38", QuestionType.FillGap, "nowa piramida żywieniowa, która ___ pierwszym miejscu stawia ruch i sen", null, emptyList(), "na", 2),
        GrammarItem("q39", QuestionType.FillGap, "liczba otyłych dzieci zwiększyła się na ___ aż jedenastokrotnie", null, emptyList(), "świecie", 2),
        GrammarItem("q40", QuestionType.FillGap, "eksperci z Instytutu Żywności i Żywienia ___ nową piramidę żywieniową", null, emptyList(), "opracowali", 2),
        GrammarItem("q41", QuestionType.FillGap, "Zgodnie z ___ zaleceniami dzieci powinny ćwiczyć przynajmniej godzinę dziennie", null, emptyList(), "najnowszymi", 2),
        GrammarItem("q42", QuestionType.FillGap, "zarówno w szkole, ___ i poza nią", null, emptyList(), "jak", 2),
        GrammarItem("q43", QuestionType.FillGap, "Jednocześnie ___ telewizji, korzystanie z komputera należy ograniczyć", null, emptyList(), "oglądanie", 2),
        GrammarItem("q44", QuestionType.FillGap, "___ nowych wytycznych podstawą zdrowego odżywiania są warzywa i owoce", null, emptyList(), "Według", 2),
        GrammarItem("q45", QuestionType.FillGap, "warzywa i owoce, które warto spożywać jak ___", null, emptyList(), "najczęściej", 2),
    ),
)

internal class ExamWebController {
    private var state = GrammarExamState(test = sampleTest)
    private var root: HTMLElement? = null

    fun render(container: HTMLElement) {
        root = container
        redraw()
    }

    private fun dispatch(action: GrammarExamAction) {
        state = reduceGrammarExam(state, action)
        redraw()
    }

    private fun redraw() {
        val container = root ?: return
        container.innerHTML = ""
        if (state.submitted && state.result != null) renderResult(container)
        else renderQuestion(container)
    }

    private fun renderQuestion(container: HTMLElement) {
        val card = grammarExamCardModel(state)
        container.appendChild(node("h2", "exam-title", card.instruction))
        container.appendChild(node("div", "exam-progress").apply {
            appendChild(node("span", "exam-progress-text", card.progress))
            appendChild(node("div", "exam-progress-bar").apply {
                appendChild(node("div", "exam-progress-fill").apply {
                    val pct = ((state.currentIndex + 1).toDouble() / state.test.items.size * 100).toInt()
                    style.setProperty("width", "${pct}%")
                })
            })
        })
        val ctx = card.item.context
        val parts = ctx.split("___")
        container.appendChild(node("div", "exam-context").apply {
            setAttribute("lang", "pl")
            if (parts.size == 2) {
                appendChild(node("span", text = parts[0]))
                appendChild(node("span", "exam-gap", "___"))
                appendChild(node("span", text = parts[1]))
            } else {
                textContent = ctx
            }
        })
        if (card.item.options.isNotEmpty()) {
            container.appendChild(node("div", "exam-options").apply {
                card.item.options.forEachIndexed { i, option ->
                    val letter = ('A' + i).toString()
                    val selected = card.currentAnswer == option
                    appendChild((document.createElement("button") as HTMLButtonElement).apply {
                        type = "button"
                        className = if (selected) "exam-option selected" else "exam-option"
                        textContent = "$letter. $option"
                        addEventListener("click", { dispatch(GrammarExamAction.Answer(card.item.itemId, option)) })
                    })
                }
            })
        } else {
            container.appendChild(node("div", "exam-input-wrap").apply {
                val input = (document.createElement("input") as HTMLInputElement).apply {
                    type = "text"
                    className = "exam-input"
                    placeholder = "Wpisz odpowiedź..."
                    value = card.currentAnswer
                    addEventListener("input", {
                        dispatch(GrammarExamAction.Answer(card.item.itemId, this.value))
                    })
                }
                appendChild(input)
            })
        }
        container.appendChild(node("div", "exam-nav").apply {
            if (!card.isFirst) appendChild((document.createElement("button") as HTMLButtonElement).apply {
                type = "button"; className = "exam-nav-btn"; textContent = "← Назад"
                addEventListener("click", { dispatch(GrammarExamAction.Previous) })
            })
            appendChild(node("span", "exam-nav-spacer"))
            if (!card.isLast) appendChild((document.createElement("button") as HTMLButtonElement).apply {
                type = "button"; className = "exam-nav-btn"; textContent = "Далее →"
                addEventListener("click", { dispatch(GrammarExamAction.Next) })
            })
            if (card.isLast) appendChild((document.createElement("button") as HTMLButtonElement).apply {
                type = "button"; className = "exam-nav-btn primary"; textContent = "Завершить"
                addEventListener("click", { dispatch(GrammarExamAction.Submit) })
            })
        })
    }

    private fun renderResult(container: HTMLElement) {
        val result = state.result ?: return
        container.appendChild(node("h2", "exam-title", "Результаты"))
        container.appendChild(node("div", "exam-score").apply {
            appendChild(node("div", "exam-score-big", "${result.percentage.toInt()}%"))
            appendChild(node("div", "exam-score-detail", "${result.correct} / ${result.total} баллов"))
        })
        container.appendChild(node("div", "exam-review").apply {
            state.test.items.forEachIndexed { i, item ->
                val answer = state.answers[item.itemId].orEmpty()
                val correct = GrammarExamScorer.score(item, answer)
                appendChild(node("div", if (correct) "exam-review-item correct" else "exam-review-item wrong").apply {
                    appendChild(node("span", "exam-review-num", "${i + 1}."))
                    appendChild(node("span", "exam-review-context", item.context.replace("___", if (answer.isNotEmpty()) "[$answer]" else "[—]")))
                    if (!correct) appendChild(node("span", "exam-review-correct", "→ ${item.correctAnswer}"))
                })
            }
        })
        container.appendChild(node("div", "exam-nav").apply {
            appendChild((document.createElement("button") as HTMLButtonElement).apply {
                type = "button"; className = "exam-nav-btn primary"; textContent = "Пройти ещё раз"
                addEventListener("click", {
                    state = GrammarExamState(test = sampleTest)
                    redraw()
                })
            })
        })
    }
}
