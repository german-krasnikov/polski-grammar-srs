package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLButtonElement
import polski.presentation.Lifehack
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
    lifehacks.forEachIndexed { index, hack -> renderOneLifehack(section, hack, index) }
}

private fun renderOneLifehack(section: HTMLElement, hack: Lifehack, index: Int) {
    val statusLabel = when (hack.status) {
        LifehackStatus.Editorial -> "editorial"
        LifehackStatus.Community -> "community"
    }
    val caption = "Лайфхак · источник: $statusLabel"
    val content = node("div", "lifehack-content collapsible")
    content.id = "training-lifehack-$index"
    val inner = node("div")
    content.appendChild(inner)
    inner.appendChild(node("p", "lifehack-text", hack.text))
    inner.appendChild(node("p", "lifehack-citation").apply {
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
