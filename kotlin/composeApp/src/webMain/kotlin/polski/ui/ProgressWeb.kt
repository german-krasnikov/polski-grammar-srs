package polski.ui

import kotlin.math.floor
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import polski.data.skills
import polski.platform.browserFormatDate
import polski.presentation.AppAction
import polski.presentation.AppUiState
import polski.presentation.LoadStatus

/** Current, in-memory progress view. Storage errors never hide its export action. */
internal fun renderProgressWeb(root: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
    root.className = "progress-page"
    val progress = state.progress
    if (progress == null) {
        root.progressAdd("h2", "Прогресс")
        root.progressAdd("p", "Сначала загрузи или восстанови сохранённый прогресс.")
        return
    }
    val summary = root.progressAdd("section", cls = "card matrix-section")
    summary.progressAdd("h2", "Прогресс")
    val stats = summary.progressAdd("div", cls = "bigstats")
    listOf(
        progress.totalReviews.toString() to "всего карточек",
        state.todayCount.toString() to "сегодня",
        state.dueCount.toString() to "к повторению",
    ).forEach { (count, title) ->
        val block = stats.progressAdd("div")
        block.progressAdd("b", count)
        block.progressAdd("span", title)
    }
    summary.progressAdd("p", "При ответе вслух «Повторить» считается ошибкой, «Вспомнил» — успешным воспоминанием. При печати точность определяется проверкой текста, а время следующего повтора выбираешь сам.")
    val actions = summary.progressAdd("div", cls = "actions")
    actions.progressButton("Экспорт JSON", "progress-export") { dispatch(AppAction.RequestExport) }
    if (state.loadStatus == LoadStatus.Ready) {
        actions.progressButton("Сбросить прогресс", "progress-reset") { dispatch(AppAction.RequestReset) }
    }

    val detail = root.progressAdd("section", cls = "card matrix-section")
    val scroll = detail.progressAdd("div", cls = "table-scroll")
    scroll.setAttribute("data-scroll-key", "progress-skills")
    val table = scroll.progressAdd("table")
    val header = table.progressAdd("thead").progressAdd("tr")
    listOf("Навык", "Повторений", "Точность", "Следующее повторение").forEach {
        header.progressAdd("th", it).setAttribute("scope", "col")
    }
    val body = table.progressAdd("tbody")
    skills.forEach { skill ->
        val card = progress.cards.firstOrNull { it.skillId == skill.id }
        val counts = progress.stats[skill.id]
        val row = body.progressAdd("tr")
        val title = row.progressAdd("th")
        title.setAttribute("scope", "row")
        title.progressButton(skill.title, "progress-skill-${skill.id}") {
            dispatch(AppAction.ChooseSkill(skill.id))
        }.className = "text-button"
        row.progressAdd("td", (counts?.reviews ?: 0).toString())
        val accuracy = if (counts == null || counts.reviews == 0) "—" else
            "${floor(counts.correct * 100.0 / counts.reviews + 0.5).toInt()}%"
        row.progressAdd("td", accuracy)
        val next = when {
            card == null -> "—"
            state.now?.let { card.card.due <= it } == true -> "Сейчас"
            else -> browserFormatDate(card.card.due.toEpochMilliseconds().toDouble())
        }
        row.progressAdd("td", next)
    }
}

private fun HTMLElement.progressAdd(tag: String, text: String? = null, cls: String? = null): HTMLElement =
    (document.createElement(tag) as HTMLElement).also {
        if (text != null) it.textContent = text
        if (cls != null) it.className = cls
        appendChild(it)
    }

private fun HTMLElement.progressButton(label: String, id: String, action: () -> Unit): HTMLElement =
    progressAdd("button", label).apply {
        this.id = id
        addEventListener("click", { action() })
    }
