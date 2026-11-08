package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import polski.exam.ExamModule
import polski.presentation.UiCopy

internal data class ExamEntry(
    val id: String, val label: String, val level: String, val year: String,
) {
    val grammarCount: Int get() = examGrammarData[id]?.sumOf { it.items.size } ?: 0
    val readingCount: Int get() = examReadingData[id]?.questions?.size ?: 0
    val listeningCount: Int get() = examListeningData[id]?.tasks?.sumOf { task -> task.questions.sumOf { it.answerSlotCount } } ?: 0
    val writingCount: Int get() = examWritingData[id]?.tasks?.size ?: 0
}

private val exams = listOf(
    ExamEntry("b1-sample", "B1 Przykładowy", "B1", "sample"),
    ExamEntry("b1-2017", "B1 Certyfikat 2017", "B1", "2017"),
    ExamEntry("b1-2019", "B1 Certyfikat 2019", "B1", "2019"),
    ExamEntry("b2-2019", "B2 Certyfikat 2019", "B2", "2019"),
)

internal enum class ExamScreen { List, Modules, Grammar, Reading, Listening, Writing }

internal class ExamWebController {
    private var screen = ExamScreen.List
    private var selectedExam: ExamEntry? = null
    private var grammarCtrl: ExamGrammarController? = null
    private var readingCtrl: ExamReadingController? = null
    private var listeningCtrl: ExamListeningController? = null
    private var writingCtrl: ExamWritingController? = null
    private var root: HTMLElement? = null

    fun close() {
        listeningCtrl?.close()
        listeningCtrl = null
        screen = ExamScreen.List
    }

    fun render(container: HTMLElement) { root = container; redraw() }

    private fun redraw() {
        val c = root ?: return; c.innerHTML = ""
        when (screen) {
            ExamScreen.List -> renderExamList(c)
            ExamScreen.Modules -> renderModuleList(c)
            ExamScreen.Grammar -> grammarCtrl?.render(c)
            ExamScreen.Reading -> readingCtrl?.render(c)
            ExamScreen.Listening -> listeningCtrl?.render(c)
            ExamScreen.Writing -> writingCtrl?.render(c)
        }
    }

    private fun renderExamList(c: HTMLElement) {
        c.appendChild(node("h2", "exam-title", UiCopy.SelectExam))
        c.appendChild(node("div", "exam-list").apply {
            exams.forEach { exam ->
                appendChild(node("div", "exam-card").apply {
                    appendChild(node("div", "exam-card-header").apply {
                        appendChild(node("span", "exam-level-badge", exam.level))
                        appendChild(node("span", "exam-card-year", exam.year))
                    })
                    appendChild(node("div", "exam-card-title", exam.label))
                    val counts = buildList {
                        if (exam.grammarCount > 0) add("${UiCopy.Grammar}: ${exam.grammarCount}")
                        if (exam.readingCount > 0) add("${UiCopy.Reading}: ${exam.readingCount}")
                        if (exam.listeningCount > 0) add("${UiCopy.Listening}: ${exam.listeningCount}")
                        if (exam.writingCount > 0) add("${UiCopy.Writing}: ${exam.writingCount}")
                    }
                    appendChild(node("div", "exam-card-counts", counts.joinToString(" · ")))
                    appendChild((document.createElement("button") as HTMLButtonElement).apply {
                        type = "button"; className = "exam-nav-btn primary"; textContent = UiCopy.Start
                        addEventListener("click", { selectExam(exam) })
                    })
                })
            }
        })
    }

    private fun selectExam(exam: ExamEntry) { selectedExam = exam; screen = ExamScreen.Modules; redraw() }

    private fun renderModuleList(c: HTMLElement) {
        val exam = selectedExam ?: return
        c.appendChild(node("h2", "exam-title", exam.label))
        c.appendChild(node("div", "exam-module-list").apply {
            moduleRow(this, "I. Rozumienie ze słuchu", exam.listeningCount, exam.listeningCount > 0) {
                close()
                listeningCtrl = ExamListeningController(exam.id) { close(); screen = ExamScreen.Modules; redraw() }
                screen = ExamScreen.Listening; redraw()
            }
            moduleRow(this, "II. Rozumienie tekstów pisanych", exam.readingCount, exam.readingCount > 0) {
                readingCtrl = ExamReadingController(exam.id) { screen = ExamScreen.Modules; redraw() }
                screen = ExamScreen.Reading; redraw()
            }
            moduleRow(this, "III. Poprawność gramatyczna", exam.grammarCount, exam.grammarCount > 0) {
                grammarCtrl = ExamGrammarController(exam.id) { screen = ExamScreen.Modules; redraw() }
                screen = ExamScreen.Grammar; redraw()
            }
            moduleRow(this, "IV. Pisanie", exam.writingCount, exam.writingCount > 0) {
                writingCtrl = ExamWritingController(exam.id) { screen = ExamScreen.Modules; redraw() }
                screen = ExamScreen.Writing; redraw()
            }
        })
        c.appendChild(node("div", "exam-nav").apply {
            appendChild((document.createElement("button") as HTMLButtonElement).apply {
                type = "button"; className = "exam-nav-btn"; textContent = "← ${UiCopy.SelectExam}"
                addEventListener("click", { screen = ExamScreen.List; redraw() })
            })
        })
    }

    private fun moduleRow(parent: HTMLElement, label: String, count: Int, available: Boolean, onClick: () -> Unit) {
        parent.appendChild(node("div", "exam-module-card").apply {
            appendChild(node("div", "exam-module-name", label))
            if (count > 0) appendChild(node("div", "exam-module-count", "$count"))
            appendChild((document.createElement("button") as HTMLButtonElement).apply {
                type = "button"
                className = if (available) "exam-nav-btn primary" else "exam-nav-btn"
                textContent = if (available) UiCopy.Start else UiCopy.ComingSoon
                disabled = !available
                addEventListener("click", { if (available) onClick() })
            })
        })
    }
}
