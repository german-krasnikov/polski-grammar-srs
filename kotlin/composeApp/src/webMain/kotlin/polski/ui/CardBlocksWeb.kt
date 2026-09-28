package polski.ui

import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import polski.presentation.Block

/**
 * UC-10 web S2: renders whatever [Block]s [polski.presentation.StyleComposer.compose] returns for
 * one phase, in the resolved recipe's own order — switching style changes which blocks appear
 * (and in what order) purely because the composed list changes, never a hardcoded style branch
 * here. Formula/Rule/Contrast share one visual box (today's `.rule-focus`/`.rule-contrast`, byte-
 * identical to the pre-UC-10 markup those classes already had) since a recipe emits them as the
 * card's single "key rule" area; every other kind gets its own accessible section.
 *
 * [langCode] is the active pack's own target-language BCP-47 code (EnRuAcceptance-2026-09-28.md
 * §7 item 2/3): every block below renders the active pack's real target-language text, so its
 * `lang` attribute must follow suit instead of the literal "pl" this used to hardcode — same fix
 * as [TrainingWebApp]'s `activeTargetLangCode()`, threaded through since this renderer has no
 * pack access of its own.
 */
internal fun renderCardBlocks(container: HTMLElement, blocks: List<Block>, phaseClass: String, langCode: String) {
    if (blocks.isEmpty()) return
    val wrap = node("div", "card-blocks $phaseClass")
    container.appendChild(wrap)
    var ruleFocus: HTMLElement? = null
    fun ruleFocusBox(): HTMLElement = ruleFocus ?: node("section", "rule-focus").also {
        it.setAttribute("aria-label", "Ключевое правило")
        wrap.appendChild(it)
        ruleFocus = it
    }
    blocks.forEach { block ->
        when (block) {
            is Block.Formula -> ruleFocusBox().apply {
                appendChild(node("small", text = "ЗАПОМНИ"))
                appendChild(node("strong").also { appendContrastParts(it, block.parts, "change-after") })
            }
            is Block.Rule -> ruleFocusBox().apply {
                appendChild(node("p").also { appendContrastParts(it, block.parts, "change-after") })
                appendChild(node("p", text = block.detail))
            }
            is Block.Contrast -> ruleFocusBox().appendChild(renderContrastMarkup(block, langCode))
            is Block.Table -> renderTableBlock(wrap, block, langCode)
            is Block.Scene -> renderSceneBlock(wrap, block, langCode)
            is Block.NativeParallel -> renderNativeParallelBlock(wrap, block, langCode)
            is Block.Examples -> renderExamplesBlock(wrap, block, langCode)
            is Block.WhyOnDemand -> renderWhyOnDemandBlock(wrap, block)
            is Block.Changes -> renderChangesBlock(wrap, block)
        }
    }
}

private fun renderContrastMarkup(block: Block.Contrast, langCode: String): HTMLElement {
    val beforeText = block.before.joinToString("") { it.text }
    val afterText = block.after.joinToString("") { it.text }
    return node("div", "rule-contrast").apply {
        setAttribute("lang", langCode)
        appendChild(node("span", "form-contrast").apply {
            setAttribute("aria-label", "Было: $beforeText. Стало: $afterText")
            appendChild(node("span", "form-contrast-before").apply {
                setAttribute("aria-hidden", "true")
                appendContrastParts(this, block.before, "change-before")
            })
            appendChild(node("span", "form-contrast-arrow", "→").apply { setAttribute("aria-hidden", "true") })
            appendChild(node("strong", "form-contrast-after").apply {
                setAttribute("aria-hidden", "true")
                appendContrastParts(this, block.after, "change-after")
            })
        })
    }
}

// UC-10 web S2 new visual: a compact endings table, own container (not `.table-scroll`, which is
// tuned for the much wider Matrix/reference tables) so a 2-3 column table stays legible at 320px.
private fun renderTableBlock(container: HTMLElement, block: Block.Table, langCode: String) {
    val caption = block.caption.takeIf(String::isNotBlank)
    val section = node("section", "block block-table")
    section.setAttribute("aria-label", caption ?: "Таблица окончаний")
    container.appendChild(section)
    if (caption != null) section.appendChild(node("h3", "block-table-caption", caption))
    val tableWrap = node("div", "block-table-wrap")
    section.appendChild(tableWrap)
    val table = node("table")
    tableWrap.appendChild(table)
    val head = node("thead")
    table.appendChild(head)
    val headRow = node("tr")
    head.appendChild(headRow)
    listOf("", "Было", "Стало").forEach { label ->
        headRow.appendChild(node("th", text = label).apply { setAttribute("scope", "col") })
    }
    val body = node("tbody")
    table.appendChild(body)
    block.rows.forEach { row ->
        val tr = node("tr")
        body.appendChild(tr)
        tr.appendChild(node("th", text = row.label).apply { setAttribute("scope", "row") })
        tr.appendChild(node("td").apply { setAttribute("lang", langCode); appendContrastParts(this, row.before, "change-before") })
        tr.appendChild(node("td").apply { setAttribute("lang", langCode); appendContrastParts(this, row.after, "change-after") })
    }
}

// UC-10 web S2 new visual: a quote-like card for the situation-first "scene" text.
private fun renderSceneBlock(container: HTMLElement, block: Block.Scene, langCode: String) {
    val section = node("section", "block block-scene")
    section.setAttribute("aria-label", "Сцена")
    container.appendChild(section)
    val quote = node("blockquote", "block-scene-quote").apply { setAttribute("lang", langCode) }
    section.appendChild(quote)
    appendContrastParts(quote, block.parts, "change-after")
}

// UC-10 web S2 new visual: a native ↔ target row per pair, with a match/differs badge — the
// content a native-contrast style needs to actually justify itself over rule-first.
private fun renderNativeParallelBlock(container: HTMLElement, block: Block.NativeParallel, langCode: String) {
    if (block.pairs.isEmpty()) return
    val section = node("section", "block block-native-parallel")
    section.setAttribute("aria-label", "Сравнение с родным")
    container.appendChild(section)
    block.pairs.forEach { pair ->
        val row = node("div", "native-parallel-row")
        section.appendChild(row)
        row.appendChild(node("span", "native-parallel-native", pair.native))
        val target = node("span", "native-parallel-target").apply { setAttribute("lang", langCode) }
        row.appendChild(target)
        appendContrastParts(target, pair.targetParts, "change-after")
        row.appendChild(node(
            "span",
            if (pair.matches) "native-parallel-badge match" else "native-parallel-badge differs",
            if (pair.matches) "Совпадает" else "Отличается",
        ))
        if (pair.note.isNotBlank()) row.appendChild(node("p", "native-parallel-note", pair.note))
    }
}

// UC-10 web S2 new visual: a plain list — the card itself is already the primary example, these
// are just extra ones, so no highlighting/analysis chrome.
private fun renderExamplesBlock(container: HTMLElement, block: Block.Examples, langCode: String) {
    if (block.items.isEmpty()) return
    val section = node("section", "block block-examples")
    section.setAttribute("aria-label", "Примеры")
    container.appendChild(section)
    val list = node("ul", "block-examples-list")
    section.appendChild(list)
    block.items.forEachIndexed { index, _ ->
        val item = node("li").apply { setAttribute("lang", langCode) }
        list.appendChild(item)
        appendContrastParts(item, block.itemParts[index], "change-after")
    }
}

// UC-10 web S2 new visual: collapsed by default (native <button>/aria-expanded disclosure, same
// 0fr/1fr grid-rows trick `applyCollapsible` already uses for the vocabulary catalog and the case-
// reference panel — so it's inert while collapsed and animates only when [motionInstantActive] is
// false, exactly like those). Always starts collapsed on a (re)build, matching "Почему так?"'s own
// name: it never shows before someone asks for it.
private fun renderWhyOnDemandBlock(container: HTMLElement, block: Block.WhyOnDemand) {
    val label = block.collapsedLabel.takeIf(String::isNotBlank) ?: "Почему так?"
    val section = node("div", "block block-why")
    container.appendChild(section)
    val content = node("div", "block-why-content collapsible")
    content.id = "training-why-on-demand"
    val inner = node("div")
    content.appendChild(inner)
    inner.appendChild(node("p").also { appendContrastParts(it, block.parts, "change-after") })
    applyCollapsible(content, expanded = false, isToggleEvent = false)
    lateinit var toggle: HTMLButtonElement
    toggle = button(label, extraClass = "block-why-toggle") {
        val expanded = !content.classList.contains("expanded")
        toggle.setAttribute("aria-expanded", expanded.toString())
        applyCollapsible(content, expanded, isToggleEvent = true)
    }
    toggle.setAttribute("aria-expanded", "false")
    toggle.setAttribute("aria-controls", content.id)
    section.appendChild(toggle)
    section.appendChild(content)
}

// Same markup `.change-list` has always had — Block.Changes is an exercise invariant (ST-05:
// identical across all 4 styles), only now read from the composed list instead of built inline.
private fun renderChangesBlock(container: HTMLElement, block: Block.Changes) {
    val section = node("div", "change-list")
    container.appendChild(section)
    section.appendChild(node("h3", text = "Что изменилось"))
    block.items.forEach { item ->
        section.appendChild(node("div").apply {
            appendChild(node("div", "change-pair").apply {
                appendChild(node("span").apply { appendContrastParts(this, item.before, "change-before") })
                appendChild(node("b", text = "→"))
                appendChild(node("strong").apply { appendContrastParts(this, item.after, "change-after") })
            })
            appendChild(node("p", text = item.reason))
        })
    }
}
