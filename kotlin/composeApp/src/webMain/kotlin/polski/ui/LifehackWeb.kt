package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLButtonElement
import polski.presentation.Lifehack
import polski.presentation.LifehackGroup
import polski.presentation.LifehackStatus
import polski.presentation.StaticPackLifehackProvider

/**
 * EN-21 (`Plans/Kotlin/EnRuPackPlan.md` §4.3): a lifehack is not a 10th [polski.presentation.BlockKind]
 * — it shows the same way in every style, so it renders as its own block *after* the resolved
 * style's back-phase blocks ([CardBlocksWeb.kt]'s `renderCardBlocks`), never inside them. Empty
 * list → nothing appended at all (no empty frame). One collapsible entry per [Lifehack] (a skill
 * can have more than one) — collapsed by default, same `.collapsible` grid-rows mechanic
 * [renderWhyOnDemandBlock] already uses. The attribution line is the toggle button's own text, so
 * it is visible whether collapsed or expanded and is exactly what a screen reader announces as
 * the control's name — the short citation (and source URL, if any) only appears once expanded.
 */
internal fun renderLifehackBlock(container: HTMLElement, skillId: String) {
    val lifehacks = StaticPackLifehackProvider.forSkill(skillId)
    if (lifehacks.isEmpty()) return
    val section = node("div", "block lifehack-block")
    container.appendChild(section)
    lifehacks.forEachIndexed { index, hack -> renderOneLifehack(section, hack, "training-lifehack-$index") }
}

/**
 * Front-of-card companion to [renderLifehackBlock] (badge + section together implement the
 * "Есть лайфхак" UX, EnRuPackPlan.md §4.3 follow-up): a small, non-spoiling indicator that a tip
 * exists for this skill — visible the moment the card shows its front, never the tip's own text.
 * Deliberately not a real `<button>`/link (there is nothing to activate — the tip itself only
 * exists on the revealed back); [installTapGesture]'s reveal-on-tap listens on the whole front via
 * event bubbling, so stopping propagation right here, on whichever element the pointer actually
 * hit, is what keeps a tap on the badge from also revealing the card, regardless of tag.
 */
internal fun renderLifehackBadge(container: HTMLElement, skillId: String) {
    if (!StaticPackLifehackProvider.hasLifehacks(skillId)) return
    val badge = node("span", "lifehack-badge")
    badge.setAttribute("aria-label", "Есть лайфхак — подсказка появится на обороте карточки")
    badge.appendChild(node("span", text = "💡").apply { setAttribute("aria-hidden", "true") })
    badge.appendChild(document.createTextNode(" Есть лайфхак"))
    val stop: (org.w3c.dom.events.Event) -> Unit = { it.stopPropagation() }
    badge.addEventListener("pointerdown", stop)
    badge.addEventListener("pointerup", stop)
    badge.addEventListener("click", stop)
    container.appendChild(badge)
}

/**
 * The "Лайфхаки" sub-section of the Matrix page: every lifehack the active pack has, grouped by
 * skill in curriculum order ([StaticPackLifehackProvider.listAll]) — one collapsed-by-default group
 * per skill/topic, reusing [MatrixWeb.kt]'s own card/section DOM helpers so this reads as one more
 * ordinary Matrix sub-section, not a bolted-on page. Empty pack -> a calm notice, not an empty card.
 */
internal fun renderLifehackMatrixSection(root: HTMLElement) {
    val groups = StaticPackLifehackProvider.listAll()
    val section = root.matrixSection("Лайфхаки")
    if (groups.isEmpty()) {
        section.matrixAdd("p", "Для активного курса лайфхаков пока нет.", "muted")
        return
    }
    section.matrixAdd("p", "Приёмы, которые облегчают эту тему носителю русского — по навыкам, в порядке программы.", "muted")
    groups.forEachIndexed { index, group -> renderLifehackGroup(section, group, index) }
}

private fun renderLifehackGroup(container: HTMLElement, group: LifehackGroup, index: Int) {
    val content = node("div", "lifehack-content collapsible")
    content.id = "matrix-lifehack-group-$index"
    val inner = node("div")
    content.appendChild(inner)
    group.lifehacks.forEach { hack -> renderLifehackEntry(inner, hack) }
    applyCollapsible(content, expanded = false, isToggleEvent = false)
    lateinit var toggle: HTMLButtonElement
    toggle = button("${group.title} · ${group.lifehacks.size}", extraClass = "lifehack-toggle") {
        val expanded = !content.classList.contains("expanded")
        toggle.setAttribute("aria-expanded", expanded.toString())
        applyCollapsible(content, expanded, isToggleEvent = true)
    }
    toggle.setAttribute("aria-expanded", "false")
    toggle.setAttribute("aria-controls", content.id)
    container.appendChild(toggle)
    container.appendChild(content)
}

/** One [Lifehack] inside an already-expanded matrix group — no per-entry toggle of its own (the
 *  group's own collapse already gates visibility), just the same source caption + body as the
 *  per-entry card-back block below. */
private fun renderLifehackEntry(container: HTMLElement, hack: Lifehack) {
    val entry = node("div", "lifehack-entry")
    container.appendChild(entry)
    entry.appendChild(node("p", "lifehack-source-label muted small", "Лайфхак · источник: ${statusLabel(hack.status)}"))
    renderLifehackBody(entry, hack)
}

private fun renderOneLifehack(section: HTMLElement, hack: Lifehack, contentId: String) {
    val caption = "Лайфхак · источник: ${statusLabel(hack.status)}"
    val content = node("div", "lifehack-content collapsible")
    content.id = contentId
    val inner = node("div")
    content.appendChild(inner)
    renderLifehackBody(inner, hack)
    applyCollapsible(content, expanded = false, isToggleEvent = false)
    lateinit var toggle: HTMLButtonElement
    toggle = button(caption, extraClass = "lifehack-toggle") {
        val expanded = !content.classList.contains("expanded")
        toggle.setAttribute("aria-expanded", expanded.toString())
        applyCollapsible(content, expanded, isToggleEvent = true)
    }
    toggle.setAttribute("aria-expanded", "false")
    toggle.setAttribute("aria-controls", content.id)
    section.appendChild(toggle)
    section.appendChild(content)
}

private fun renderLifehackBody(container: HTMLElement, hack: Lifehack) {
    container.appendChild(node("p", "lifehack-text", hack.text))
    container.appendChild(node("p", "lifehack-citation").apply {
        appendChild(document.createTextNode(hack.source.citation))
        hack.source.url?.let { url ->
            appendChild(document.createTextNode(" — "))
            appendChild((document.createElement("a") as HTMLAnchorElement).apply {
                href = url
                target = "_blank"
                rel = "noopener noreferrer"
                textContent = "источник"
            })
        }
    })
}

private fun statusLabel(status: LifehackStatus): String = when (status) {
    LifehackStatus.Editorial -> "editorial"
    LifehackStatus.Community -> "community"
}
