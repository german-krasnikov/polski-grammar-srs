package polski.ui

import kotlin.math.floor
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import polski.data.skills
import polski.platform.browserFormatDate
import polski.presentation.AppAction
import polski.presentation.AppUiState
import polski.presentation.LoadStatus
import polski.presentation.UiCopy

/** Current, in-memory progress view. Storage errors never hide its export action. */
internal fun renderProgressWeb(root: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
    root.className = "progress-page"
    val progress = state.progress
    if (progress == null) {
        root.progressAdd("h2", UiCopy.ProgressTab)
        root.progressAdd("p", "Сначала загрузи или восстанови сохранённый прогресс.")
        return
    }
    val summary = root.progressAdd("section", cls = "card matrix-section")
    summary.progressAdd("h2", UiCopy.ProgressTab)
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
    summary.progressAdd("p", "При ответе вслух «${UiCopy.Again}» считается ошибкой, «${UiCopy.Good}» — успешным воспоминанием. При печати точность определяется проверкой текста, а время следующего повтора выбираешь сам.")
    val actions = summary.progressAdd("div", cls = "actions")
    actions.progressButton(UiCopy.ExportJson, "progress-export") { dispatch(AppAction.RequestExport) }
    if (state.loadStatus == LoadStatus.Ready) {
        actions.progressButton(UiCopy.ResetProgress, "progress-reset") { dispatch(AppAction.RequestReset) }
    }

    renderAchievementsSection(root, state)

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

/**
 * M6 blueprint: "раньше с таблицей, теперь сам" — one entry per skill currently at
 * `tileScaffoldingRung == 2` (SB-8's RUNG_NONE: least scaffolding help). Skills not yet there are
 * simply absent, never a placeholder row; the whole section is likewise absent while no skill
 * qualifies yet — a brand-new learner's Progress page shows no empty achievements card.
 */
private fun renderAchievementsSection(root: HTMLElement, state: AppUiState) {
    val progress = state.progress ?: return
    val achieved = skills.filter { progress.stats[it.id]?.tileScaffoldingRung == 2 }
    if (achieved.isEmpty()) return
    val section = root.progressAdd("section", cls = "card matrix-section")
    section.setAttribute("aria-label", "Мои достижения")
    section.progressAdd("h2", "Мои достижения")
    achieved.forEach { skill ->
        val stats = progress.stats.getValue(skill.id)
        val item = section.progressAdd("div", cls = "achievement")
        item.progressAdd("b", skill.title)
        // independentSince can still be null for a skill that reached rung 2 before M6 shipped and
        // hasn't been promoted again since (ReviewReducer's own sticky-while-already-None rule) —
        // fail-soft: the streak alone is still shown, just without a date.
        val since = stats.independentSince?.let { " · раньше с таблицей, теперь сам — с $it" }
            ?: " · раньше с таблицей, теперь сам"
        item.progressAdd("span", "$since · подтверждено ${stats.tileScaffoldingStreak} раз без таблицы")
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
