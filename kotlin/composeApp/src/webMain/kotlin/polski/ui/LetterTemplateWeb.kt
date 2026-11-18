package polski.ui

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import polski.data.rows.plru.lettertemplate.LetterVerificationStatus
import polski.exam.template.LetterTemplate
import polski.presentation.UiCopy

internal fun renderLetterTemplatePanel(
    container: HTMLElement,
    templates: List<LetterTemplate>,
    getTextarea: () -> HTMLTextAreaElement?,
) {
    var selected = templates.first()
    val details = node("details", "exam-writing-template")
    details.appendChild(document.createElement("summary").apply { textContent = UiCopy.LetterTemplateToggle })
    val body = node("div", "exam-writing-template-body")
    details.appendChild(body)

    fun renderBody() {
        body.innerHTML = ""

        if (templates.size > 1) {
            val row = node("div", "exam-template-picker-row").apply {
                appendChild(node("span", "exam-template-picker-label", UiCopy.LetterTemplatePicker))
            }
            templates.forEach { tpl ->
                row.appendChild((document.createElement("button") as HTMLButtonElement).apply {
                    type = "button"
                    className = "exam-template-type-btn" + if (tpl === selected) " selected" else ""
                    textContent = tpl.titleNative
                    setAttribute("data-template-type", tpl.letterType.tag)
                    addEventListener("click", { selected = tpl; renderBody() })
                })
            }
            body.appendChild(row)
        }

        for ((idx, section) in selected.sections.withIndex()) {
            val sectionEl = (document.createElement("details") as HTMLElement).apply {
                className = "exam-template-section"
                setAttribute("data-section-key", section.sectionKey.tag)
                if (idx == 0) setAttribute("open", "")
            }
            sectionEl.appendChild(document.createElement("summary").apply { textContent = section.labelNative })
            section.noteNative?.let { sectionEl.appendChild(node("p", "exam-template-section-note", it)) }
            for (phrase in section.phrases) {
                val row = node("div", "exam-template-phrase-row")
                row.appendChild((document.createElement("button") as HTMLButtonElement).apply {
                    type = "button"
                    className = "exam-template-phrase" + if (phrase.isPreferred) " preferred" else ""
                    setAttribute("lang", "pl")
                    setAttribute("data-phrase-text", phrase.targetText)
                    textContent = phrase.targetText
                    if (phrase.verificationStatus == LetterVerificationStatus.NEEDS_REVIEW)
                        appendChild(node("span", "exam-template-unverified", " ${UiCopy.LetterTemplateNeedsReview}"))
                    addEventListener("click", { insertPhrase(phrase.targetText, getTextarea) })
                })
                phrase.nativeNote?.let { row.appendChild(node("span", "exam-template-phrase-note", it)) }
                sectionEl.appendChild(row)
            }
            body.appendChild(sectionEl)
        }

        if (selected.mistakes.isNotEmpty()) {
            val mistakesDetails = (document.createElement("details") as HTMLElement).apply {
                className = "exam-template-mistakes"
            }
            mistakesDetails.appendChild(document.createElement("summary").apply {
                textContent = UiCopy.LetterTemplateMistakes
            })
            for (m in selected.mistakes) {
                mistakesDetails.appendChild(node("div", "exam-template-mistake").apply {
                    appendChild(node("span", "exam-template-wrong").apply { setAttribute("lang", "pl"); textContent = m.wrongForm })
                    appendChild(document.createTextNode(" → "))
                    appendChild(node("span", "exam-template-correct").apply { setAttribute("lang", "pl"); textContent = m.correctForm })
                    if (m.ruleNative.isNotBlank()) appendChild(node("p", "exam-template-mistake-rule", m.ruleNative))
                })
            }
            body.appendChild(mistakesDetails)
        }

        selected.modelLetter?.let { letter ->
            val letterDetails = node("details", "exam-template-model-letter")
            letterDetails.appendChild(document.createElement("summary").apply { textContent = UiCopy.LetterTemplateModelLetter })
            letterDetails.appendChild(node("div", "exam-template-model-title", letter.titleNative))
            letterDetails.appendChild(node("pre", "exam-template-model-text").apply { setAttribute("lang", "pl"); textContent = letter.fullText })
            letter.annotationNative?.let { letterDetails.appendChild(node("p", "exam-template-model-annotation", it)) }
            body.appendChild(letterDetails)
        }
    }

    renderBody()
    container.appendChild(details)
}

private fun insertPhrase(text: String, getTextarea: () -> HTMLTextAreaElement?) {
    val ta = getTextarea() ?: return
    val start = ta.selectionStart ?: ta.value.length
    val end = ta.selectionEnd ?: ta.value.length
    val before = ta.value.substring(0, start)
    val after = ta.value.substring(end)
    val sep = if (before.isNotEmpty() && !before.endsWith(" ") && !before.endsWith("\n")) " " else ""
    ta.value = "$before$sep$text$after"
    val newPos = start + sep.length + text.length
    ta.selectionStart = newPos
    ta.selectionEnd = newPos
    ta.focus()
    ta.dispatchEvent(Event("input"))
    val rect = ta.getBoundingClientRect()
    if (rect.bottom > window.innerHeight || rect.top < 0.0)
        ta.scrollIntoView()
}
