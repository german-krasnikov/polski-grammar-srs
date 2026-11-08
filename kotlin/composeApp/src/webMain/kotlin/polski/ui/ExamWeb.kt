package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import polski.exam.ExamModule

/** Available exams shown on the selection screen. */
internal data class ExamEntry(val id: String, val label: String, val level: String, val year: String, val grammarCount: Int, val readingCount: Int, val listeningCount: Int)

private val exams = listOf(
    ExamEntry("b1-sample", "B1 Przykładowy", "B1", "sample", 20, 0, 0),
    ExamEntry("b1-2019", "B1 Certyfikat 2019", "B1", "2019", 45, 35, 42),
    ExamEntry("b2-2019", "B2 Certyfikat 2019", "B2", "2019", 75, 15, 53),
)

internal enum class ExamScreen { List, Modules, Grammar, Reading, Listening, Results }

internal class ExamWebController {
    private var screen = ExamScreen.List
    private var selectedExam: ExamEntry? = null
    private var grammarCtrl: ExamGrammarController? = null
    private var readingCtrl: ExamReadingController? = null
    private var root: HTMLElement? = null

    fun render(container: HTMLElement) {
        root = container
        redraw()
    }

    private fun redraw() {
        val container = root ?: return
        container.innerHTML = ""
        when (screen) {
            ExamScreen.List -> renderExamList(container)
            ExamScreen.Modules -> renderModuleList(container)
            ExamScreen.Grammar -> grammarCtrl?.render(container)
            ExamScreen.Reading -> readingCtrl?.render(container)
            ExamScreen.Listening -> renderPlaceholder(container, "Аудирование (в разработке)")
            ExamScreen.Results -> Unit
        }
    }

    private fun renderExamList(container: HTMLElement) {
        container.appendChild(node("h2", "exam-title", "Выберите экзамен"))
        container.appendChild(node("div", "exam-list").apply {
            exams.forEach { exam ->
                appendChild(node("div", "exam-card").apply {
                    appendChild(node("div", "exam-card-header").apply {
                        appendChild(node("span", "exam-level-badge", exam.level))
                        appendChild(node("span", "exam-card-year", exam.year))
                    })
                    appendChild(node("div", "exam-card-title", exam.label))
                    val counts = buildList {
                        if (exam.grammarCount > 0) add("Грамматика: ${exam.grammarCount}")
                        if (exam.readingCount > 0) add("Чтение: ${exam.readingCount}")
                        if (exam.listeningCount > 0) add("Аудирование: ${exam.listeningCount}")
                    }
                    appendChild(node("div", "exam-card-counts", counts.joinToString(" · ")))
                    appendChild((document.createElement("button") as HTMLButtonElement).apply {
                        type = "button"; className = "exam-nav-btn primary"; textContent = "Начать"
                        addEventListener("click", { selectExam(exam) })
                    })
                })
            }
        })
    }

    private fun selectExam(exam: ExamEntry) {
        selectedExam = exam
        screen = ExamScreen.Modules
        redraw()
    }

    private fun renderModuleList(container: HTMLElement) {
        val exam = selectedExam ?: return
        container.appendChild(node("h2", "exam-title", exam.label))
        container.appendChild(node("div", "exam-module-list").apply {
            val modules = listOf(
                Triple(ExamModule.Grammar, "Poprawność gramatyczna", exam.grammarCount),
                Triple(ExamModule.Reading, "Rozumienie tekstów pisanych", exam.readingCount),
                Triple(ExamModule.Listening, "Rozumienie ze słuchu", exam.listeningCount),
                Triple(ExamModule.Writing, "Pisanie", 0),
            )
            modules.forEach { (module, label, count) ->
                appendChild(node("div", "exam-module-card").apply {
                    appendChild(node("div", "exam-module-name", label))
                    if (count > 0) appendChild(node("div", "exam-module-count", "$count вопросов"))
                    val available = count > 0 && module != ExamModule.Writing
                    appendChild((document.createElement("button") as HTMLButtonElement).apply {
                        type = "button"
                        className = if (available) "exam-nav-btn primary" else "exam-nav-btn"
                        textContent = if (available) "Начать" else "Скоро"
                        disabled = !available
                        addEventListener("click", { startModule(exam, module) })
                    })
                })
            }
        })
        container.appendChild(node("div", "exam-nav").apply {
            appendChild((document.createElement("button") as HTMLButtonElement).apply {
                type = "button"; className = "exam-nav-btn"; textContent = "← Назад к списку"
                addEventListener("click", { screen = ExamScreen.List; redraw() })
            })
        })
    }

    private fun startModule(exam: ExamEntry, module: ExamModule) {
        when (module) {
            ExamModule.Grammar -> {
                grammarCtrl = ExamGrammarController(exam.id) { screen = ExamScreen.Modules; redraw() }
                screen = ExamScreen.Grammar
            }
            ExamModule.Reading -> {
                readingCtrl = ExamReadingController(exam.id) { screen = ExamScreen.Modules; redraw() }
                screen = ExamScreen.Reading
            }
            ExamModule.Listening -> screen = ExamScreen.Listening
            ExamModule.Writing -> return
        }
        redraw()
    }

    private fun renderPlaceholder(container: HTMLElement, text: String) {
        container.appendChild(node("h2", "exam-title", text))
        container.appendChild(node("div", "exam-nav").apply {
            appendChild((document.createElement("button") as HTMLButtonElement).apply {
                type = "button"; className = "exam-nav-btn"; textContent = "← Назад"
                addEventListener("click", { screen = ExamScreen.Modules; redraw() })
            })
        })
    }
}
