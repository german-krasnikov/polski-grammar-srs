package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import polski.presentation.UiCopy
import org.w3c.dom.HTMLTextAreaElement

internal data class WritingTask(
    val id: String,
    val instruction: String,
    val promptText: String,
    val wordLimit: Int?,
    val setId: String? = null,
    val genre: String = "letter",
    val promptImageUrl: String? = null,
    val promptImageAlt: String? = null,
)
internal data class WritingExam(val tasks: List<WritingTask>) {
    fun tasksForSet(setId: String?): List<WritingTask> =
        if (setId == null) tasks.filter { it.setId == null } else tasks.filter { it.setId == setId }
}

internal class ExamWritingController(examId: String, private val onBack: () -> Unit) {
    private val exam = examWritingData[examId] ?: WritingExam(emptyList())
    private var currentTaskIdx = 0
    private var drafts = mutableMapOf<String, String>()
    private var submitted = false
    private var selectedSet: String? = null
    private var root: HTMLElement? = null

    private val sets = exam.tasks.mapNotNull { it.setId }.distinct()
    private val activeTasks: List<WritingTask> get() = if (sets.isEmpty()) exam.tasks else exam.tasksForSet(selectedSet)

    fun render(container: HTMLElement) { root = container; redraw() }

    private fun redraw() {
        val c = root ?: return; c.innerHTML = ""
        if (submitted) renderDone(c)
        else if (exam.tasks.isEmpty()) { c.appendChild(node("h2", "exam-title", "Нет заданий на письмо")); c.appendChild(navBtn("← Назад") { onBack() }) }
        else if (sets.isNotEmpty() && selectedSet == null) renderSetChoice(c)
        else renderTask(c)
    }

    private fun renderSetChoice(c: HTMLElement) {
        c.appendChild(node("h2", "exam-title", UiCopy.SelectWritingSet))
        sets.forEach { setId ->
            c.appendChild(navBtn("${UiCopy.WritingSet} $setId") {
                selectedSet = setId
                currentTaskIdx = 0
                redraw()
            })
        }
        c.appendChild(navBtn("← Назад") { onBack() })
    }

    private fun renderTask(c: HTMLElement) {
        val tasks = activeTasks
        val task = tasks[currentTaskIdx]
        c.appendChild(node("div", "exam-progress").apply {
            appendChild(node("span", "exam-progress-text", "${UiCopy.Task} ${currentTaskIdx + 1} / ${tasks.size}"))
        })
        c.appendChild(node("h3", "exam-title", task.instruction))
        if (sets.isNotEmpty()) c.appendChild(navBtn(UiCopy.ChangeWritingSet) { selectedSet = null; currentTaskIdx = 0; redraw() })
        c.appendChild(node("div", "exam-writing-prompt").apply {
            setAttribute("lang", "pl")
            task.promptText.split("\n").forEach { line ->
                if (line.isNotBlank()) appendChild(node("p", text = line.trim()))
            }
        })
        task.promptImageUrl?.let { imageUrl ->
            c.appendChild(node("img", "exam-writing-image").apply {
                setAttribute("src", imageUrl)
                setAttribute("alt", requireNotNull(task.promptImageAlt) { "Missing image description for ${task.id}" })
            })
        }
        if (task.wordLimit != null) {
            c.appendChild(node("div", "exam-word-limit", "Limit: ${task.wordLimit} słów"))
        }
        val draft = drafts[task.id].orEmpty()
        val wordCount = draft.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        val textarea = (document.createElement("textarea") as HTMLTextAreaElement).apply {
            className = "exam-textarea"; rows = 12; placeholder = "Napisz tutaj..."
            value = draft
            addEventListener("input", { drafts[task.id] = this.value; updateWordCount(this) })
        }
        c.appendChild(textarea)
        c.appendChild(node("div", "exam-word-count").apply {
            id = "word-count"
            textContent = "Słów: $wordCount" + (task.wordLimit?.let { " / $it" } ?: "")
        })
        c.appendChild(node("div", "exam-nav").apply {
            if (currentTaskIdx > 0) appendChild(navBtn("← Назад") { currentTaskIdx--; redraw() })
            appendChild(node("span", "exam-nav-spacer"))
            if (currentTaskIdx < tasks.lastIndex) appendChild(navBtn("Dalej →") { currentTaskIdx++; redraw() })
            if (currentTaskIdx == tasks.lastIndex) appendChild(navBtn("Zakończ", true) { submitted = true; redraw() })
        })
    }

    private fun updateWordCount(textarea: HTMLTextAreaElement) {
        val task = activeTasks[currentTaskIdx]
        val count = textarea.value.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        document.getElementById("word-count")?.textContent =
            "Słów: $count" + (task.wordLimit?.let { " / $it" } ?: "")
    }

    private fun renderDone(c: HTMLElement) {
        c.appendChild(node("h2", "exam-title", "Pisanie zakończone"))
        c.appendChild(node("div", "exam-writing-done").apply {
            appendChild(node("p", text = "Twoje odpowiedzi zostały zapisane. Część pisemna egzaminu jest oceniana przez egzaminatora — nie ma automatycznej oceny."))
        })
        activeTasks.forEachIndexed { i, task ->
            c.appendChild(node("div", "exam-writing-review").apply {
                appendChild(node("h4", text = "${UiCopy.Task} ${i + 1}: ${task.instruction}"))
                appendChild(node("pre", "exam-writing-draft", drafts[task.id].orEmpty().ifEmpty { "[пусто]" }))
            })
        }
        c.appendChild(node("div", "exam-nav").apply {
            appendChild(navBtn("← К модулям") { onBack() })
        })
    }

    private fun navBtn(label: String, primary: Boolean = false, onClick: () -> Unit) =
        (document.createElement("button") as HTMLButtonElement).apply {
            type = "button"; className = if (primary) "exam-nav-btn primary" else "exam-nav-btn"
            textContent = label; addEventListener("click", { onClick() })
        }
}
